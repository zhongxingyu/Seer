 package translator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import xtc.tree.GNode;
 import xtc.tree.Visitor;
 
 public class ClassDeclaration extends Declaration implements Translatable {
   
   private HashMap<String, List<Object>> vtable;
   private ClassDeclaration parent;
   
   private ClassBody body;
   private Extension extension;
   private List<Implementation> implementation;
   private boolean isAbstract;
   private boolean isFinal;
   private boolean isStatic;
   private String name;
   private Visibility visibility;
   
   /**
    * Constructs the ClassDeclaration.
    * 
    * @param n the ClassDeclaration node.
    */
   public ClassDeclaration(GNode n) {
     parent = null;
     name = n.getString(1);
     extension = null;
     implementation = new ArrayList<Implementation>();
     isAbstract = false;
     isFinal = false;
     isStatic = false;
     visit(n);
   }
   
   /**
    * Gets the superclass.
    *
    * @return the extension.
    */
   public Extension getExtension() {
     return extension;
   }
   
   /**
    * Gets the list of implemented interfaces.
    *
    * @return the implementation list.
    */
   public List<Implementation> getImplementation() {
     return implementation;
   }
   
   public String getName() {
     return name;
   }
   
   /**
    * Gets the visibility of the class.
    *
    * @return the visibility.
    */
   public Visibility getVisibility() {
     return visibility;
   }
   
   public void setParent(ClassDeclaration parent) {
     this.parent = parent;
   }
   
   public boolean hasExtension() {
     return extension != null;
   }
   
   public boolean isAbstract() {
     return isAbstract;
   }
   
   public boolean isFinal() {
     return isFinal;
   }
   
   public void visitClassBody(GNode n) {
     body = new ClassBody(n);
   }
   
   public void visitExtension(GNode n) {
     extension = new Extension(n);
   }
   
   public void visitImplementation(GNode n) {
     implementation.add(new Implementation(n));
   }
   
   public void visitModifiers(GNode n) {
     Modifiers modifiers = new Modifiers(n);
     for (String m : modifiers) {
       if (m.equals("public"))
         visibility = Visibility.PUBLIC;
       else if (m.equals("private"))
         visibility = Visibility.PRIVATE;
       else if (m.equals("protected"))
         visibility = Visibility.PROTECTED;
       else if (m.equals("final"))
         isFinal = true;
       else if (m.equals("abstract"))
         isAbstract = true;
       else if (m.equals("static"))
         isStatic = true;
     }
   }
   
   
   
   public void createVTable() {
     vtable = new HashMap<String, List<Object>>();
     HashMap<String, List<Object>> parentVT = null;
     if (parent != null)
        parentVT = parent.getVTable();
     List<MethodDeclaration> l = body.getMethods(Visibility.PUBLIC);
     if (l != null) {
       for (MethodDeclaration m : l) {
         if (m.isFinal() || m.isStatic() || m.isAbstract())
           continue;
         if (m.getName().equals("hashCode") && 
             m.getReturnType().getType().equals("int") &&
             m.getParameters().size() == 0) {
           List<Object> o = new ArrayList<Object>();
           o.add(name);
           o.add(m);
           vtable.put("hashCode", o);
         } else if (m.getName().equals("equals") && 
                    m.getReturnType().getType().equals("boolean") && 
                    m.getParameters().size() == 1 &&
                    m.getParameters().get(0).getType().equals("Object")) {
           List<Object> o = new ArrayList<Object>();
           o.add(name);
           o.add(m);
           vtable.put("equals", o);
         } else if (m.getName().equals("toString") &&
                    m.getReturnType().getType().equals("String") && 
                    m.getParameters().size() == 0) {
           List<Object> o = new ArrayList<Object>();
           o.add(name);
           o.add(m);
           vtable.put("toString", o);
         } else {
           List<Object> o = new ArrayList<Object>();
           o.add(name);
           o.add(m);
           vtable.put(m.getName(), o);
         }
       }
     }
     
     if (!vtable.containsKey("hashCode")) {
       if (parentVT != null && parentVT.containsKey("hashCode")) {
         vtable.put("hashCode", parentVT.get("hashCode"));
       }
     }
     if (!vtable.containsKey("equals")) {
       if (parentVT != null && parentVT.containsKey("equals")) {
         vtable.put("equals", parentVT.get("equals"));
       }
     }
     if (!vtable.containsKey("toString")) {
       if (parentVT != null && parentVT.containsKey("toString")) {
         vtable.put("toString", parentVT.get("toString"));
       }
     }
     if (parentVT != null) {
       Set<String> keys = parentVT.keySet();
       for (String key : keys) {
         if (!vtable.containsKey(key))
           vtable.put(key, parentVT.get(key));
       }
     }
   }
   
   public HashMap<String, List<Object>> getVTable() {
     if (vtable == null)
       createVTable();
     return vtable;
   }
   
   public String getHeaderStruct(int indent) {
     if (vtable == null)
       createVTable();
     StringBuilder s = new StringBuilder();
     String in = getIndent(indent);
     s.append(in + "struct __" + name  + " {\n");
     in = getIndent(++indent);
     s.append(in + "__" + name + "_VT* __vptr;\n");
     for (Visibility v : Visibility.values()) {
       List<FieldDeclaration> fields = body.getFields(v);
       if (fields != null) {
         for (FieldDeclaration f : fields) {
           s.append(in + f.getDeclaration() + ";\n");
         }
       }
     }
    s.append("\n" + in + body.getConstructorDeclaration() + "\n\n");
     List<MethodDeclaration> l = body.getMethods(Visibility.PUBLIC);
     if (l != null)
       for (MethodDeclaration m : l)
         if (!m.isStatic() && !m.isAbstract() && !m.isFinal())
           s.append(in + m.getHeaderDeclaration(name) + "\n");
     s.append("\n" + in + "static Class __class();\n\n");
     s.append(in + "static __" + name + "_VT __vtable;\n");
     in = getIndent(--indent);
     s.append(in + "};\n");
     return s.toString();
   }
 
   public String getHeaderVTStruct(int indent) {
     HashMap<String, List<Object>> parentVT = null;
     StringBuilder s = new StringBuilder();
     String in = getIndent(indent);
     s.append(in + "struct __" + name + "_VT {\n");
     in = getIndent(++indent);    
     List<MethodDeclaration> l = body.getMethods(Visibility.PUBLIC);
     
     s.append(in + "Class __isa;\n");
     
     if (vtable.containsKey("hashCode")) {
       s.append(in + ((MethodDeclaration)vtable.get("hashCode").get(1)).getHeaderVTDeclaration(name) + "\n");
     } else {
       s.append(in + "int32_t (*hashCode)(Object);\n");
     }
     if (vtable.containsKey("equals")) {
       s.append(in + ((MethodDeclaration)vtable.get("equals").get(1)).getHeaderVTDeclaration(name) + "\n");
     } else {
       s.append(in + "bool (*equals)(Object, Object);\n");
     }
     s.append(in + "Class (*getClass)(" + name + ");\n");
     if (vtable.containsKey("toString")) {
       s.append(in + ((MethodDeclaration)vtable.get("toString").get(1)).getHeaderVTDeclaration(name) + "\n");
     } else {
       s.append(in + "String (*toString)(Object);\n");
     }
 
     Set<String> methods = vtable.keySet();
     for (String method : methods) {
       if (!method.equals("hashCode") &&
           !method.equals("equals") &&
           !method.equals("toString")) {
         s.append(in + ((MethodDeclaration)vtable.get(method).get(1)).getHeaderVTDeclaration(name) + "\n");
       }
     }
 
     s.append("\n" + in + "__" + name + "_VT()\n" + in + ": ");
     s.append("__isa(__" + name + "::__class())");
     
     if (vtable.containsKey("hashCode")) {
       List<Object> o = vtable.get("hashCode");
       String t = (String)o.get(0);
       MethodDeclaration m = (MethodDeclaration)o.get(1);
       if (!t.equals(name))
         s.append(",\n" + in + m.getHeaderVTConstructor(name, t));
       else
         s.append(",\n" + in + m.getHeaderVTConstructor(name, null));
     } else {
       s.append(",\n" + in + "hashCode(&__Object::hashCode)");
     }
     if (vtable.containsKey("equals")) {
       List<Object> o = vtable.get("equals");
       String t = (String)o.get(0);
       MethodDeclaration m = (MethodDeclaration)o.get(1);
       if (!t.equals(name))
         s.append(",\n" + in + m.getHeaderVTConstructor(name, t));
       else
         s.append(",\n" + in + m.getHeaderVTConstructor(name, null));
     } else {
       s.append(",\n" + in + "equals(&__Object::equals)");
     }
     s.append(",\n" + in + "getClass((Class(*)(" + name + "))&__Object::getClass)");
     if (vtable.containsKey("toString")) {
       List<Object> o = vtable.get("toString");
       String t = (String)o.get(0);
       MethodDeclaration m = (MethodDeclaration)o.get(1);
       if (!t.equals(name))
         s.append(",\n" + in + m.getHeaderVTConstructor(name, t));
       else
         s.append(",\n" + in + m.getHeaderVTConstructor(name, null));
     } else {
       s.append(",\n" + in + "toString(&__Object::toString)");
     }
     
     for (String method : methods) {
       if (!method.equals("hashCode") &&
           !method.equals("equals") &&
           !method.equals("toString")) {
         List<Object> o = vtable.get(method);
         String t = (String)o.get(0);
         MethodDeclaration m = (MethodDeclaration)o.get(1);
         if (!t.equals(name))
           s.append(",\n" + in + m.getHeaderVTConstructor(name, t));
         else
           s.append(",\n" + in + m.getHeaderVTConstructor(name, null));
       }
     }
     
     s.append(" {}\n");
 
     in = getIndent(--indent);
     s.append(in + "};\n");
     return s.toString();
   }
   
   public String getHeader(int indent) {
     StringBuilder s = new StringBuilder();
     List<MethodDeclaration> l1 = body.getMethods(Visibility.PRIVATE);
     if (l1 != null) {
       for (MethodDeclaration m : l1) {
         s.append(m.getHeaderDeclaration(name) + "\n");
       }
     }
     List<MethodDeclaration> l2 = body.getMethods(Visibility.PUBLIC);
     if (l2 != null) {
       for (MethodDeclaration m : l2) {
         if (m.isStatic()) {
           s.append(m.getHeaderDeclaration(name) + "\n");
         }
       }
     }
     return s.toString();
   }
 
   public String getCC(int indent, String className, List<Variable> variables) {
     return body.getCC(indent, name, variables);
   }
   
 }
