 package uk.ac.cam.db538.dexter.hierarchy;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.Util.AccessFlags;
 
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
 import uk.ac.cam.db538.dexter.dex.type.DexMethodId;
 
 public abstract class BaseClassDefinition implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @Getter private final DexClassType type;
     private final int accessFlags;
     @Getter private final boolean internal;
 
     @Getter private BaseClassDefinition superclass;
     private final List<BaseClassDefinition> _children;
     @Getter private final List<BaseClassDefinition> children;
 
     private final List<MethodDefinition> _methods;
     @Getter private final List<MethodDefinition> methods;
 
     private final List<StaticFieldDefinition> _staticFields;
     @Getter private final List<StaticFieldDefinition> staticFields;
 
     private final List<InterfaceDefinition> _interfaces;
     @Getter private final List<InterfaceDefinition> interfaces;
 
     BaseClassDefinition(DexClassType type, int accessFlags, boolean isInternal) {
         this.type = type;
         this.accessFlags = accessFlags;
         this.internal = isInternal;
 
         this.superclass = null;
         this._children = new ArrayList<BaseClassDefinition>();
         this.children = Collections.unmodifiableList(this._children);
 
         this._methods = new ArrayList<MethodDefinition>();
         this.methods = Collections.unmodifiableList(this._methods);
 
         this._staticFields = new ArrayList<StaticFieldDefinition>();
         this.staticFields = Collections.unmodifiableList(this._staticFields);
 
         this._interfaces = new ArrayList<InterfaceDefinition>();
         this.interfaces = Collections.unmodifiableList(this._interfaces);
     }
 
     public void setSuperclass(BaseClassDefinition superclass) {
     	if (this.superclass != null)
     		this.superclass._children.remove(this);
         this.superclass = superclass;
         this.superclass._children.add(this);
     }
     
     protected void refineSuperclassLink(BaseClassDefinition newSuperclass) {
     	if (!newSuperclass.equals(superclass)) {
     		assert newSuperclass.isChildOf(superclass);
     		setSuperclass(newSuperclass);
     	}
     }
 
     public void addDeclaredMethod(MethodDefinition method) {
         assert method.getParentClass() == this;
 
         this._methods.add(method);
     }
 
     public void replaceDeclaredMethod(MethodDefinition oldDef, MethodDefinition newDef) {
         assert oldDef.getParentClass() == this;
         assert newDef.getParentClass() == this;
 
         this._methods.remove(oldDef);
         this._methods.add(newDef);
     }
 
     public void addDeclaredStaticField(StaticFieldDefinition field) {
         assert field.isStatic();
         assert field.getParentClass() == this;
 
         this._staticFields.add(field);
     }
 
     public void addImplementedInterface(InterfaceDefinition iface) {
         this._interfaces.add(iface);
         iface._implementors.add(this);
     }
 
     public EnumSet<AccessFlags> getAccessFlags() {
         AccessFlags[] flags = AccessFlags.getAccessFlagsForClass(accessFlags);
         if (flags.length == 0)
             return EnumSet.noneOf(AccessFlags.class);
         else
             return EnumSet.of(flags[0], flags);
     }
 
     public boolean isAbstract() {
         return getAccessFlags().contains(AccessFlags.ABSTRACT);
     }
 
     public boolean isInterface() {
         return getAccessFlags().contains(AccessFlags.INTERFACE);
     }
 
     public boolean isAnnotation() {
         return getAccessFlags().contains(AccessFlags.ANNOTATION);
     }
 
     public boolean isRoot() {
         return this.getSuperclass() == null;
     }
 
     public boolean isChildOf(BaseClassDefinition parent) {
         BaseClassDefinition inspected = this;
         while (true) {
             if (inspected.equals(parent))
                 return true;
             else if (inspected.isRoot())
                 return false;
             else
                 inspected = inspected.getSuperclass();
         }
     }
     
     public void checkUsedAs(BaseClassDefinition refType) {
     	if (!this.isChildOf(refType))
     		throw new RuntimeException("Class " + this + " cannot be used " + refType);
     }
 
     public boolean implementsInterface(InterfaceDefinition iface) {
         return null != iterateThroughParentsAndInterfaces(iface, extractorImplementedInterface, acceptorAlwaysTrue, false);
     }
 
     boolean hasInternalNonAbstractChildren() {
         if (isInternal() && !isAbstract())
             return true;
 
         for (val child : this.children)
             if (child.hasInternalNonAbstractChildren())
                 return true;
         
         return false;
     }
 
     public MethodDefinition getMethod(DexMethodId methodId) {
         for (val methodDef : this.methods)
             if (methodDef.getMethodId().equals(methodId))
                 return methodDef;
         return null;
     }
 
     public StaticFieldDefinition getStaticField(DexFieldId fieldId) {
         for (val fieldDef : this.staticFields)
             if (fieldDef.getFieldId().equals(fieldId))
                 return fieldDef;
         return null;
     }
 
     public StaticFieldDefinition getStaticField(String name) {
         for (val fieldDef : this.staticFields)
             if (fieldDef.getFieldId().getName().equals(name))
                 return fieldDef;
         return null;
     }
 
     public static enum CallDestinationType {
         Internal,
         External,
         Undecidable
     }
 
     public CallDestinationType getMethodDestinationType(DexMethodId methodId, Opcode_Invoke opcode) {
         boolean foundExternal = false;
         boolean foundInternal = false;
 
         try {
            for (MethodDefinition callableImplementation : getCallableMethodImplementations(methodId, opcode))
                if (callableImplementation.getParentClass().isInternal() && !callableImplementation.isNative())
                     foundInternal = true;
                 else
                     foundExternal = true;
         } catch (HierarchyException e) {
             return CallDestinationType.External;
         }
         
         if (foundInternal && foundExternal)
             return CallDestinationType.Undecidable;
         else if (foundInternal)
             return CallDestinationType.Internal;
         else
             return CallDestinationType.External;
     }
 
     public MethodDefinition getSomeMethodImplementation(DexMethodId methodId, Opcode_Invoke opcode) {
         List<MethodDefinition> implementations = getCallableMethodImplementations(methodId, opcode);
         if (implementations.isEmpty())
             throw new AssertionError("No implementation was found for given method call");
         else
             return implementations.get(0);
     }
 
     private List<MethodDefinition> getCallableMethodImplementations(DexMethodId methodId, Opcode_Invoke opcode) {
         switch (opcode) {
         case Direct:
             return callableMethodImplementations_Direct(methodId);
         case Static:
             return callableMethodImplementations_Static(methodId);
         case Super:
             return callableMethodImplementations_Super(methodId);
         case Virtual:
         case Interface:
             return callableMethodImplementations_VirtualInterface(methodId);
         default:
             throw new Error("Unknown opcode");
         }
     }
 
     private List<MethodDefinition> callableMethodImplementations_Direct(DexMethodId methodId) {
         // Direct calls are for private methods and constructors
         // They are always made on the implementing class, i.e. it must be a proper class
 
         val methodDef = getMethod(methodId);
         if (methodDef != null &&
                 this instanceof ClassDefinition &&
                 !methodDef.isStatic() &&
                 (methodDef.isPrivate() || methodDef.isConstructor()))
             return Arrays.asList(methodDef);
         else
             throw new HierarchyException("Invalid method call (direct call destination not found): " + type + "->" + methodId);
     }
 
     private List<MethodDefinition> callableMethodImplementations_Static(final DexMethodId methodId) {
         // Static calls invoke a static method in the class itself
         // or the closest parent that implements it
         // They can be applied on any kind of class
 
         MethodDefinition methodDef = iterateThroughParents(methodId, extractorMethod, acceptorStaticCalls, false); // start iterating with this class
         if (methodDef != null)
             return Arrays.asList(methodDef);
         else
             throw new HierarchyException("Invalid method call (static call destination not found): " + type + "->" + methodId);
     }
 
     private List<MethodDefinition> callableMethodImplementations_Super(final DexMethodId methodId) {
         // Super calls invoke a non-private method
         // in the closest parent that implements it
         // They can be applied on any kind of class
 
         MethodDefinition methodDef = iterateThroughParents(methodId, extractorMethod, acceptorVirtualCall, false); // this is the superclass already, so don't skip first
         if (methodDef != null)
             return Arrays.asList(methodDef);
         else
             throw new HierarchyException("Invalid method call (super call destination not found): " + type + "->" + methodId);
     }
 
     private List<MethodDefinition> callableMethodImplementations_VirtualInterface(DexMethodId methodId) {
         if (this instanceof ClassDefinition) {
             // Virtual calls invoke a non-private, non-static method
             // in the class itself, in the closest parent or in any of the children
 
             List<MethodDefinition> fromChildren = iterateThroughChildren(methodId, extractorMethod, acceptorVirtualCall);
             MethodDefinition fromParents = iterateThroughParents(methodId, extractorMethod, acceptorVirtualCall, true); // no need to scan this class twice
 
             if (fromParents != null)
                 fromChildren.add(fromParents);
 
             return fromChildren;
         } else if (this instanceof InterfaceDefinition) {
             // Interface calls invoke a non-private, non-static method
             // in one of the classes that implement the given interface,
             // or any of its children
 
             val list = new ArrayList<MethodDefinition>();
             for (val implementor : ((InterfaceDefinition) this).getImplementors())
                 list.addAll(implementor.callableMethodImplementations_VirtualInterface(methodId));
             return list;
         } else
             throw new HierarchyException("Invalid method call");
     }
 
     public StaticFieldDefinition getAccessedStaticField(DexFieldId fieldId) {
         // Application can access a static field on class X, but
         // the field might actually be defined in one of X's parents
         // This method will return the definition of the field
         // in itself or the closest parent or implemented interface.
 
         return iterateThroughParentsAndInterfaces(fieldId, extractorStaticField, acceptorAlwaysTrue, false);
     }
 
     public BaseClassDefinition getCommonParent(BaseClassDefinition otherClass) {
         // Iterate through parents of this class (including itself) and return the first
         // class that is the parent of the otherClass passed in as a parameter
         // Note that the work is done in the extractor
 
         return iterateThroughParents(otherClass, extractorParentClass, acceptorAlwaysTrue, false);
     }
 
     protected <Id, T> T iterateThroughParents(Id id, Extractor<Id, T> extractor, Acceptor<? super T> acceptor, boolean skipFirst) {
         if (this.isRoot() && skipFirst)
             return null;
 
         BaseClassDefinition inspectedClass = skipFirst ? this.getSuperclass() : this;
 
         while (true) {
             T def = extractor.extract(inspectedClass, id);
             if (def != null) {
                 if (acceptor.accept(def))
                     return def;
             }
 
             if (inspectedClass.isRoot())
                 return null;
             else
                 inspectedClass = inspectedClass.getSuperclass();
         }
     }
 
     protected <Id, T> T iterateThroughParentsAndInterfaces(Id id, Extractor<Id, T> extractor, Acceptor<? super T> acceptor, boolean skipFirst) {
         Queue<BaseClassDefinition> queue = new LinkedList<BaseClassDefinition>();
         if (skipFirst) {
             if (!this.isRoot())
                 queue.add(this.superclass);
             queue.addAll(this.interfaces);
         } else
             queue.add(this);
 
         while (!queue.isEmpty()) {
             BaseClassDefinition inspectedClass = queue.remove();
             T def = extractor.extract(inspectedClass, id);
             if (def != null) {
                 if (acceptor.accept(def))
                     return def;
             }
 
             if (!inspectedClass.isRoot())
                 queue.add(inspectedClass.superclass);
             queue.addAll(inspectedClass.interfaces);
         }
 
         return null;
     }
 
     protected <Id, T> List<T> iterateThroughChildren(Id id, Extractor<Id, T> extractor, Acceptor<? super T> acceptor) {
         List<T> list = new ArrayList<T>();
 
         T def = extractor.extract(this, id);
         if (def != null) {
             if (acceptor.accept(def))
                 list.add(def);
         }
 
         for (val child : getChildren())
             list.addAll(child.iterateThroughChildren(id, extractor, acceptor));
 
         return list;
     }
 
     @Override
 	public String toString() {
     	return type.toString();
 	}
 
 	protected static interface Acceptor<T> {
         public boolean accept(T item);
     }
 
     protected static interface Extractor<Id, T> {
         public T extract(BaseClassDefinition clazz, Id id);
     }
 
     private static final Extractor<BaseClassDefinition, BaseClassDefinition> extractorParentClass = new Extractor<BaseClassDefinition, BaseClassDefinition>() {
         @Override
         public BaseClassDefinition extract(BaseClassDefinition parentClass, BaseClassDefinition otherClass) {
             if (otherClass.isChildOf(parentClass))
                 return parentClass;
             else
                 return null;
         }
     };
 
     private static final Extractor<DexMethodId, MethodDefinition> extractorMethod = new Extractor<DexMethodId, MethodDefinition>() {
         @Override
         public MethodDefinition extract(BaseClassDefinition clazz, DexMethodId methodId) {
             MethodDefinition methodDef = clazz.getMethod(methodId);
             if (methodDef != null && methodDef.isAbstract())
                 return null;
             else
                 return methodDef;
         }
     };
 
     protected static final Extractor<DexFieldId, StaticFieldDefinition> extractorStaticField = new Extractor<DexFieldId, StaticFieldDefinition>() {
         @Override
         public StaticFieldDefinition extract(BaseClassDefinition clazz, DexFieldId fieldId) {
             return clazz.getStaticField(fieldId);
         }
     };
 
     protected static final Extractor<InterfaceDefinition, InterfaceDefinition> extractorImplementedInterface = new Extractor<InterfaceDefinition, InterfaceDefinition>() {
         @Override
         public InterfaceDefinition extract(BaseClassDefinition clazz, InterfaceDefinition ifaceSought) {
             for (InterfaceDefinition iface : clazz.getInterfaces())
                 if (iface.equals(ifaceSought))
                     return iface;
             return null;
         }
     };
 
     protected static final Acceptor<Object> acceptorAlwaysTrue = new Acceptor<Object>() {
         @Override
         public boolean accept(Object item) {
             return true;
         }
     };
 
     private static final Acceptor<MethodDefinition> acceptorStaticCalls = new Acceptor<MethodDefinition>() {
         @Override
         public boolean accept(MethodDefinition method) {
             return method.isStatic();
         }
     };
 
     private static final Acceptor<MethodDefinition> acceptorVirtualCall = new Acceptor<MethodDefinition>() {
         @Override
         public boolean accept(MethodDefinition method) {
             return !method.isStatic() &&
                    !method.isPrivate();
         }
     };
 }
