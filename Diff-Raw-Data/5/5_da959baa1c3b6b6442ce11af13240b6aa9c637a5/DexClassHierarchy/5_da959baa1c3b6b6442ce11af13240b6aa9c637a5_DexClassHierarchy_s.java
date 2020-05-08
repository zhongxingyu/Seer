 package uk.ac.cam.db538.dexter.dex.type.hierarchy;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.apache.bcel.classfile.Annotations;
 import org.apache.bcel.classfile.ClassParser;
 
 import lombok.AllArgsConstructor;
 import lombok.Getter;
 import lombok.val;
 import uk.ac.cam.db538.dexter.dex.DexAnnotation;
 import uk.ac.cam.db538.dexter.dex.DexParsingCache;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
 import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 public class DexClassHierarchy {
 
   private final Map<DexClassType, ClassEntry> classes;
   private final DexClassType rootClass;
 
   public DexClassHierarchy(DexClassType rootClass) {
     this.classes = new HashMap<DexClassType, ClassEntry>();
     this.rootClass = rootClass;
   }
 
   public void addClass(DexClassType classType, DexClassType superclassType) {
     Set<DexClassType> emptyInterfaces = Collections.emptySet();
     Set<DexAnnotation> emptyAnnotations = Collections.emptySet();
     addMember(classType, superclassType, emptyInterfaces, emptyAnnotations, false);
   }
 
   public void addClass(DexClassType classType, DexClassType superclassType, Set<DexClassType> interfaces) {
     Set<DexAnnotation> emptyAnnotations = Collections.emptySet();
     addMember(classType, superclassType, interfaces, emptyAnnotations, false);
   }
 
   public void addInterface(DexClassType classType) {
     Set<DexClassType> emptyInterfaces = Collections.emptySet();
     Set<DexAnnotation> emptyAnnotations = Collections.emptySet();
     addMember(classType, rootClass, emptyInterfaces, emptyAnnotations, true);
   }
 
   public void addMember(DexClassType classType, DexClassType superclassType, Set<DexClassType> interfaces, Set<DexAnnotation> annotations, boolean flagInterface) {
     if (classes.containsKey(classType)) {
       System.err.println("warning: class " + classType.getDescriptor() + " is defined multiple times");
       return;
     }
 
     if (classType != rootClass && wouldIntroduceLoop(classType, superclassType))
       throw new ClassHierarchyException("Class " + classType.getPrettyName() + " introduces a loop in the class hierarchy");
 
     if (interfaces == null)
       interfaces = new HashSet<DexClassType>();
     else
       interfaces = new HashSet<DexClassType>(interfaces);
 
     if (annotations == null)
       annotations = new HashSet<DexAnnotation>();
     else
       annotations = new HashSet<DexAnnotation>(annotations);
 
     val classEntry = new ClassEntry(classType, superclassType, interfaces, annotations, new HashSet<MethodEntry>(), new HashSet<FieldEntry>(), flagInterface, new HashSet<DexClassType>());
     classes.put(classType, classEntry);
   }
 
   public void addImplementedInterface(DexClassType clazz, DexClassType interfaceClazz) {
     classes.get(clazz).getInterfaces().add(interfaceClazz);
   }
 
   public void addImplementedMethod(DexClassType classType, String methodName, DexPrototype methodPrototype, boolean isPrivate, boolean isNative, boolean isPublic) {
     for (val method : classes.get(classType).implementedMethods)
       if (method.getName().equals(methodName) &&
           method.getPrototype().equals(methodPrototype)) {
         System.err.println("warning: method " + classType.getDescriptor() + "->" + methodName + " is defined multiple times");
         return;
       }
 
     classes.get(classType).implementedMethods.add(new MethodEntry(methodName, methodPrototype, isPrivate, isNative, isPublic));
   }
 
   public void addDeclaredField(DexClassType classType, String fieldName, DexRegisterType fieldType, boolean isStatic, boolean isPrivate) {
     for (val field : classes.get(classType).declaredFields)
       if (field.getName().equals(fieldName) &&
           field.getType().equals(fieldType)) {
         System.err.println("warning: field " + classType.getDescriptor() + "->" + fieldName + " is defined multiple times");
         return;
       }
 
     classes.get(classType).declaredFields.add(new FieldEntry(fieldName, fieldType, isStatic, isPrivate));
   }
 
   public void addClassAnnotation(DexClassType clazz, DexAnnotation anno) {
     classes.get(clazz).annotations.add(anno);
   }
 
   private String createDescriptor(String desc) {
     return "L" + desc.replace(".", "/") + ";";
   }
 
   public void addAllClassesFromJAR(File file, DexParsingCache cache) throws IOException {
     System.out.println("Loading " + file.getPath());
     val jarFile = new JarFile(file);
     try {
       for (Enumeration<JarEntry> jarEntryEnum = jarFile.entries(); jarEntryEnum.hasMoreElements();) {
         val jarEntry = jarEntryEnum.nextElement();
 
         if (!jarEntry.isDirectory() && jarEntry.getName().endsWith(".class")) {
 
           val jarClass = new ClassParser(jarFile.getInputStream(jarEntry), jarEntry.getName()).parse();
 
           val setInterfaces = new HashSet<DexClassType>();
           for (val i : jarClass.getInterfaceNames())
             setInterfaces.add(DexClassType.parse(createDescriptor(i), cache));
 
           val setAnnotations = new HashSet<DexAnnotation>();
           for (val attr : jarClass.getAttributes())
             if (attr instanceof Annotations)
               for (val anno : ((Annotations) attr).getAnnotationEntries())
                 setAnnotations.add(new DexAnnotation(anno, cache));
 
           val classType = DexClassType.parse(createDescriptor(jarClass.getClassName()), cache);
 
           addMember(
             classType,
             DexClassType.parse(createDescriptor(jarClass.getSuperclassName()), cache),
             setInterfaces,
             setAnnotations,
             jarClass.isInterface()
           );
 
           for (val method : jarClass.getMethods())
             if (!method.isAbstract())
               addImplementedMethod(classType, method.getName(), new DexPrototype(method.getSignature(), cache), method.isPrivate(), method.isNative(), method.isPublic());
 
           for (val field : jarClass.getFields())
             addDeclaredField(classType, field.getName(), DexRegisterType.parse(field.getSignature(), cache), field.isStatic(), field.isPrivate());
         }
       }
     } finally {
       jarFile.close();
     }
   }
 
   public DexClassType getSuperclassType(DexClassType clazz) {
     if (classes.get(clazz) == null) {
       System.err.println("warning: missing class hierarchy entry for " + clazz.getDescriptor());
       return rootClass;
     } else
       return classes.get(clazz).getSuperclassType();
   }
 
   public Set<DexClassType> getInterfaces(DexClassType clazz) {
     return Collections.unmodifiableSet(classes.get(clazz).getInterfaces());
   }
 
   public Set<DexAnnotation> getAnnotations(DexClassType clazz) {
     return Collections.unmodifiableSet(classes.get(clazz).getAnnotations());
   }
 
   public void checkConsistency() {
     for (val entry : classes.entrySet()) {
       val clazz = entry.getKey();
       val clazzEntry = entry.getValue();
       val superclazz = clazzEntry.getSuperclassType();
       val isInterface = clazzEntry.isFlaggedInterface();
       val clazzInterfaces = clazzEntry.getInterfaces();
 
       // hierarchy is consistent if all classes have their parents in the hierarchy as well
       if (!classes.containsKey(superclazz))
         System.err.println("warning: Class hierarchy not consistent (" +
                            clazz.getPrettyName() + " needs its parent " +
                            superclazz.getPrettyName() + ")");
       else
         classes.get(superclazz).children.add(clazz);
 
       if (isInterface) {
         if (superclazz != rootClass)
           throw new ClassHierarchyException("Class hierarchy not consistent (interface " +
                                             clazz.getPrettyName() + " must extend root class)");
       } else {
         for (val i : clazzInterfaces) {
           if (!classes.containsKey(i))
             System.err.println("warning: Class hierarchy not consistent (" +
                                clazz.getPrettyName() + " needs its interface " +
                                i.getPrettyName() + ")");
           else if (!classes.get(i).isFlaggedInterface())
             throw new ClassHierarchyException("Class hierarchy not consistent (class " +
                                               clazz.getPrettyName() + " implements non-interface " +
                                               i.getPrettyName() + ")");
         }
       }
     }
 
     // root class can't be an interface
     if (classes.get(rootClass).isFlaggedInterface())
       throw new ClassHierarchyException("Root class cannot be an interface");
 
   }
 
   public boolean isAncestor(DexReferenceType refType, DexClassType ancestor) {
     DexClassType clazz = getTrueCalledClass(refType);
 
     // start at clazz and work our way up the hierarchy tree
     // checking equality at each level
 
     DexClassType prevClazz = null;
     do {
       if (clazz == ancestor)
         return true;
 
       prevClazz = clazz;
       clazz = getSuperclassType(clazz);
     } while (clazz != prevClazz);
 
     return false;
   }
 
   public boolean implementsInterface(DexReferenceType refType, DexClassType intrface) {
     DexClassType clazz = getTrueCalledClass(refType);
 
     // start at clazz and work our way up the hierarchy tree
     // searching through implemented interfaces at each level
 
     DexClassType prevClazz = null;
     do {
       if (classes.get(clazz) == null) {
         System.err.println("warning: missing class hierarchy entry for " + clazz.getDescriptor());
         return false;
       } else if (classes.get(clazz).getInterfaces().contains(intrface))
         return true;
 
       prevClazz = clazz;
       clazz = getSuperclassType(clazz);
     } while (clazz != prevClazz);
 
     return false;
   }
 
   public boolean implementsMethod(DexReferenceType refType, String name, DexPrototype prototype) {
     DexClassType clazz = getTrueCalledClass(refType);
 
     for (val method : classes.get(clazz).implementedMethods)
       if (method.getName().equals(name) && method.getPrototype().equals(prototype))
         return true;
 
     return false;
   }
 
   public boolean isMethodNative(DexReferenceType refType, String name, DexPrototype prototype) {
     DexClassType clazz = getTrueCalledClass(refType);
 
     for (val method : classes.get(clazz).implementedMethods)
       if (method.getName().equals(name) && method.getPrototype().equals(prototype))
         return method.isDeclaredNative();
 
     return false;
   }
 
   public boolean isMethodPublic(DexReferenceType refType, String name, DexPrototype prototype) {
     DexClassType clazz = getTrueCalledClass(refType);
 
     for (val method : classes.get(clazz).implementedMethods)
       if (method.getName().equals(name) && method.getPrototype().equals(prototype))
         return method.isDeclaredPublic();
 
     return false;
   }
 
   public DexClassType getAccessedFieldDeclaringClass(DexClassType accessedClazz, String fieldName, DexRegisterType fieldType, boolean isStatic) {
     for (val ancestor : getAllParents(accessedClazz, true))
       for (val field : classes.get(ancestor).getDeclaredFields())
         if (field.getName().equals(fieldName) && field.getType().equals(fieldType) && field.isDeclaredStatic() == isStatic) {
           // if the field signature matches, return it if it can be accessed
           // this is true if the field is not private, or if it is, but it is declared directly in the accessed class
           // (we assume that the instruction is valid, i.e. that it can actually access the field)
           if (!field.isDeclaredPrivate() || (ancestor == accessedClazz))
             return ancestor;
           else
             return null;
         }
     return null;
   }
 
   private boolean wouldIntroduceLoop(DexClassType clazz, DexClassType superclazz) {
     DexClassType currClazz = superclazz;
 
     do {
       if (currClazz == clazz)
         return true;
 
       val entryClazz = classes.get(currClazz);
       if (entryClazz == null)
         return false;
       else
         currClazz = entryClazz.getSuperclassType();
     } while (currClazz != rootClass);
 
     return false;
   }
 
 //  private static String createDescriptor(String className) {
 //    return "L" + className.replace('.', '/') + ";";
 //  }
 
   public Set<DexClassType> getAllChildren(DexClassType clazz) {
     val set = new HashSet<DexClassType>();
     val queue = new LinkedList<DexClassType>();
     queue.add(clazz);
 
     while(!queue.isEmpty()) {
       val clz = queue.removeFirst();
       set.add(clz);
      queue.addAll(classes.get(clz).getChildren());
     }
 
     return set;
   }
 
   public List<DexClassType> getAllParents(DexClassType clazz, boolean includeItself) {
     // needs to return a list
     // and elements must be in order, starting with the closest parent
     val list = new ArrayList<DexClassType>();
     if (includeItself)
       list.add(clazz);
 
     if (clazz != rootClass) {
       while (true) {
         clazz = this.getSuperclassType(clazz);
         if (list.contains(clazz))
           return list;
         else
           list.add(clazz);
       }
     } else
       return list;
   }
 
   public Set<DexClassType> getAllClassesImplementingInterface(DexClassType intrface) {
     val set = new HashSet<DexClassType>();
 
     for (val clazz : classes.keySet())
       if (implementsInterface(clazz, intrface))
         set.add(clazz);
 
     return set;
   }
 
   private DexClassType getTrueCalledClass(DexReferenceType refType) {
     if (refType instanceof DexClassType)
       return (DexClassType) refType;
     else if (refType instanceof DexArrayType)
       return rootClass;
     else
       throw new Error();
   }
 
   private static final Pair<Boolean, Boolean> CALL_INTERNAL = new Pair<Boolean, Boolean>(true, false);
   private static final Pair<Boolean, Boolean> CALL_EXTERNAL = new Pair<Boolean, Boolean>(false, true);
 
   /*
    * Returns a pair of booleans. The first is true if and only if
    * the method call can be internal. Second is true if it can
    * be an external call.
    */
   public Pair<Boolean, Boolean> decideMethodCallDestination(Opcode_Invoke callType, DexReferenceType refType, String methodName, DexPrototype methodPrototype) {
     DexClassType callClass = getTrueCalledClass(refType);
 
     if (callType == Opcode_Invoke.Direct) {
       if (callClass.isDefinedInternally() && !isMethodNative(callClass, methodName, methodPrototype))
         return CALL_INTERNAL;
       else
         return CALL_EXTERNAL;
     } else if (callType == Opcode_Invoke.Super || callType == Opcode_Invoke.Static) {
       // with super/static call we can always deduce the destination
       // by going through the parents (DexClassHierarchy will
       // return them ordered from the closest parent
       // to Object) and deciding based on the first implementation
       // we encounter
 
       // need to put TRUE here, because classType is already a parent
       for (val parentClass : this.getAllParents(callClass, true)) {
         if (implementsMethod(parentClass, methodName, methodPrototype)) {
           if (parentClass.isDefinedInternally() && !isMethodNative(parentClass, methodName, methodPrototype))
             return CALL_INTERNAL;
           else
             return CALL_EXTERNAL;
         }
       }
 
       throw new ClassHierarchyException("Cannot determine the destination of super/static method call: " + callClass.getPrettyName() + "." + methodName);
 
     } else if (callType == Opcode_Invoke.Virtual || callType == Opcode_Invoke.Interface) {
 
       Set<DexClassType> potentialDestinationClasses;
       if (callType == Opcode_Invoke.Virtual) {
         // call destination class can be a child which implements the given method,
         // or it can be a parent which implements the given method
         potentialDestinationClasses = new HashSet<DexClassType>();
         potentialDestinationClasses.addAll(this.getAllChildren(callClass));
         potentialDestinationClasses.addAll(this.getAllParents(callClass, true));
       } else {
         // in the case of an interface, we need to look at all the classes
         // that implement it; class hierarchy will automatically return
         // all the ancestors of such classes as well
         potentialDestinationClasses = this.getAllClassesImplementingInterface(callClass);
       }
 
       boolean canBeInternal = false;
       boolean canBeExternal = false;
 
       for (val destClass : potentialDestinationClasses)
         if (implementsMethod(destClass, methodName, methodPrototype)) {
           if (destClass.isDefinedInternally() && !isMethodNative(destClass, methodName, methodPrototype))
             canBeInternal = true;
           else
             canBeExternal = true;
         }
 
       return new Pair<Boolean, Boolean>(canBeInternal, canBeExternal);
 
     } else
       throw new Error("Wrong call type");
   }
 
   @AllArgsConstructor
   @Getter
   private static class ClassEntry {
     private final DexClassType classType;
     private final DexClassType superclassType;
     private final Set<DexClassType> interfaces;
     private final Set<DexAnnotation> annotations;
     private final Set<MethodEntry> implementedMethods;
     private final Set<FieldEntry> declaredFields;
     private final boolean flaggedInterface;
     private final Set<DexClassType> children;
   }
 
   @AllArgsConstructor
   @Getter
   private static class MethodEntry {
     // must update hashCode and equals if new fields are added
     private final String name;
     private final DexPrototype prototype;
     private final boolean declaredPrivate;
     private final boolean declaredNative;
     private final boolean declaredPublic;
 
     @Override
     public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + (this.declaredPrivate ? 1231 : 1237);
       result = prime * result + (this.declaredNative ? 1231 : 1237);
       result = prime * result + (this.declaredPublic ? 1231 : 1237);
       result = prime * result
                + ((this.name == null) ? 0 : this.name.hashCode());
       result = prime * result
                + ((this.prototype == null) ? 0 : this.prototype.hashCode());
       return result;
     }
 
     @Override
     public boolean equals(Object obj) {
       if (this == obj)
         return true;
       if (obj == null)
         return false;
       if (!(obj instanceof MethodEntry))
         return false;
       MethodEntry other = (MethodEntry) obj;
       if (this.declaredPrivate != other.declaredPrivate)
         return false;
       if (this.declaredNative != other.declaredNative)
         return false;
       if (this.name == null) {
         if (other.name != null)
           return false;
       } else if (!this.name.equals(other.name))
         return false;
       if (this.prototype == null) {
         if (other.prototype != null)
           return false;
       } else if (!this.prototype.equals(other.prototype))
         return false;
       return true;
     }
   }
 
   @AllArgsConstructor
   @Getter
   private static class FieldEntry {
     // must update hashCode and equals if new fields are added
     private final String name;
     private final DexRegisterType type;
     private final boolean declaredStatic;
     private final boolean declaredPrivate;
 
     @Override
     public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + (this.declaredPrivate ? 1231 : 1237);
       result = prime * result + (this.declaredStatic ? 1231 : 1237);
       result = prime * result
                + ((this.name == null) ? 0 : this.name.hashCode());
       result = prime * result
                + ((this.type == null) ? 0 : this.type.hashCode());
       return result;
     }
 
     @Override
     public boolean equals(Object obj) {
       if (this == obj)
         return true;
       if (obj == null)
         return false;
       if (!(obj instanceof FieldEntry))
         return false;
       FieldEntry other = (FieldEntry) obj;
       if (this.declaredPrivate != other.declaredPrivate)
         return false;
       if (this.declaredStatic != other.declaredStatic)
         return false;
       if (this.name == null) {
         if (other.name != null)
           return false;
       } else if (!this.name.equals(other.name))
         return false;
       if (this.type == null) {
         if (other.type != null)
           return false;
       } else if (!this.type.equals(other.type))
         return false;
       return true;
     }
   }
 }
