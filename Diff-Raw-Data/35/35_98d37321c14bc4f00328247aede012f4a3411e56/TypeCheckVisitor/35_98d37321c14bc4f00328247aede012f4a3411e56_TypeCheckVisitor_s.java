 package minijava.typechecker.implementation;
 
 import java.util.HashSet;
 import java.util.Iterator;
 
 import minijava.ast.*;
 import minijava.typechecker.ErrorReport;
 import minijava.typechecker.TypeChecked;
 import minijava.util.FunTable;
 import minijava.util.FunTable.Entry;
 import minijava.visitor.Visitor;
 
 public class TypeCheckVisitor implements Visitor<TypeChecked>
 {
   private FunTable<Info> table;
   private ErrorReport error;
   
   private String currentClass,
                  currentMethod;
   
   public TypeCheckVisitor(FunTable<Info> table, ErrorReport error)
   {
     this.table = table;
     this.error = error;
   }
   
   @Override
   public <T extends AST> TypeChecked visit(NodeList<T> ns)
   {
     for(int i = 0; i < ns.size(); ++i)
     {
       ns.elementAt(i).accept(this);
     }
     
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = null;
     return t;
   }
 
   @Override
   public TypeChecked visit(Program p)
   {
     p.mainClass.accept(this);
     p.classes.accept(this);
     
     // Reset context
     this.currentClass = null;
     this.currentMethod = null;
     
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = null;
     return t;
   }
 
   @Override
   public TypeChecked visit(MainClass c)
   {
     // Set main context
     this.currentClass = c.className;
     this.currentMethod = "main";
     
     return c.statement.accept(this);
   }
 
   @Override
   public TypeChecked visit(ClassDecl d)
   {
     ClassInfo info = this.lookupClassInfo(d.name);
     boolean hasSuper = (d.superName != null),
             b1 = true,
             b2 = (info != null),
             b3 = true,
             b4 = true;
     
     if(hasSuper)
     {
       // Check superclass type
       ClassInfo superInfo = this.lookupClassInfo(d.superName);
       b1 = (superInfo != null);
       
       // Check if there are fields that are already declared and inherited from 
       // the superclass
       if(b1)
       {
         // Iterate entire parent hierarchy
         ClassInfo currentInfo = superInfo;
         do
         {
           Iterator<Entry<Info>> it = info.fields.iterator();
           while(it.hasNext())
           {
             String id = it.next().getId();
             if(currentInfo.fields.lookup(id) != null)
             {
               this.error.fieldOverriding(new ObjectType(d.name), id);
               b3 = false;
             }
           }
           
           currentInfo = this.lookupClassInfo(currentInfo.superClass);
         }
         while(currentInfo != null);
       }
     }
     
     // Check duplicate class names
     Iterator<Entry<Info>> it = this.table.iterator();
     int counter = 0;
     while(it.hasNext())
     {
       Entry<Info> entry = it.next();
       String className = entry.getId();
       if(className.equals(d.name))
       {
         counter++;
       }
     }
     b4 = (counter == 1) ? true : false;
     
     
     if(!b1 || !b2 || !b3 || !b4)
     {
       if(!b1)
       {
         this.error.undefinedId(d.superName);
       }
       
       if(!b2)
       {
         this.error.undefinedId(d.name);
       }
       
       if(!b4)
       {
         this.error.duplicateDefinition(d.name);
       }
       
       return null;
     }
     
     // Set class context
     this.currentClass = d.name;
     
     // Check duplicate field names
     HashSet<String> map = new HashSet<String>();
     for(int i = 0; i < d.vars.size(); i++)
     {
       String fieldName = d.vars.elementAt(i).name;
       if(map.contains(fieldName))
       {
         this.error.duplicateDefinition(fieldName);
       }
       else
       {
         map.add(fieldName);
       }
     }
     
     // Check duplicate method names
     map = new HashSet<String>();
     for(int i = 0; i < d.methods.size(); i++)
     {
       String methodName = d.methods.elementAt(i).name;
       if(map.contains(methodName))
       {
         this.error.duplicateDefinition(methodName);
       }
       else
       {
         map.add(methodName);
       }
     }
     
     d.methods.accept(this);
     d.vars.accept(this);
     
     // Reset class context
     this.currentClass = null;
     
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = null;
     return t;
   }
 
   @Override
   public TypeChecked visit(VarDecl n)
   {
     Type type = n.type;
     
     // Check identifier and declared object type (if necessary)
     ClassInfo c = this.lookupClassInfo(this.currentClass);
     MethodInfo m = (MethodInfo) c.methods.lookup(this.currentMethod);
     boolean b1 = ((m != null && ( m.locals.lookup(n.name) != null ||
                                   m.formals.lookup(n.name) != null)) ||
                   c.fields.lookup(n.name) != null),
             b2 = (!(type instanceof ObjectType) ||
                   this.lookupClassInfo(((ObjectType) type).name) != null);
     
     if(!b1 || !b2)
     {
       if(!b1)
       {
         this.error.undefinedId(n.name);
       }
       
       if(!b2)
       {
         this.error.undefinedId(((ObjectType) type).name);
       }
       
       return null;
     }
     
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = type;
     return t;
   }
 
   @Override
   public TypeChecked visit(MethodDecl n)
   {
     Type type = n.returnType;
     
     // Check method identifier and object return type (if necessary)
     ClassInfo c = (ClassInfo) this.lookupClassInfo(this.currentClass);
     MethodInfo m = (MethodInfo) c.methods.lookup(n.name);
     boolean b1 = (m != null),
             b2 = (!(type instanceof ObjectType) ||
                   this.lookupClassInfo(((ObjectType) type).name) != null);
     
     if(!b1 || !b2)
     {
       if(!b1)
       {
         this.error.undefinedId(n.name);
       }
       
       if(!b2)
       {
         this.error.undefinedId(((ObjectType) type).name);
       }
       
       return null;
     }
     
     this.currentMethod = n.name;
     
     // Compare method with superclass' declaration (if it exists)
     if(c.superClass != null)
     {
       ClassInfo currentInfo = this.lookupClassInfo(c.superClass);
       while(currentInfo != null)
       {
         // Check for method in superclass
         MethodInfo superMethod = (MethodInfo) currentInfo.methods.lookup(this.currentMethod);
         if(superMethod != null)
         {
           // Compare method signatures
           // NOTE: This language does not support method overloading. Arguments 
           // length is expected to be the same.
           int length = m.formalsList.size();
           if(length != superMethod.formalsList.size())
           {
             this.error.badMethodOverriding(new ObjectType(this.currentClass), this.currentMethod);
             return null;
           }
           
           // Check that the method signature is exactly the same
           for(int i = 0; i < length; ++i)
           {
             if(!m.formalsList.get(i).type.equals(superMethod.formalsList.get(i).type))
             {
               this.error.badMethodOverriding(new ObjectType(this.currentClass), this.currentMethod);
               return null;
             }
           }
           
           // Check return type is the same
           if(!m.returnType.equals(superMethod.returnType))
           {
             this.error.badMethodOverriding(new ObjectType(this.currentClass), this.currentMethod);
             return null;
           }
         }
         
         currentInfo = this.lookupClassInfo(currentInfo.superClass);
       }
     }
     
     // Check duplicate formals names
     HashSet<String> map = new HashSet<String>();
     for(int i = 0; i < n.formals.size(); i++)
     {
       String formalName = n.formals.elementAt(i).name;
       if(map.contains(formalName))
       {
         this.error.duplicateDefinition(formalName);
       }
       else
       {
         map.add(formalName);
       }
     }
     
     // Check duplicate local names adding to previous map from formals
     for(int i = 0; i < n.vars.size(); i++)
     {
       String varName = n.vars.elementAt(i).name;
       if(map.contains(varName))
       {
         this.error.duplicateDefinition(varName);
       }
       else
       {
         map.add(varName);
       }
     }
     
     n.formals.accept(this);
     n.vars.accept(this);
     n.statements.accept(this);
     
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.returnExp.accept(this);
     
     this.currentMethod = null;
     
     // Check return type
     if(t != null && !t.type.equals(m.returnType))
     {
       this.error.typeError(n.returnExp, m.returnType, t.type);
       return null;
     }
     
     return t;
   }
 
   @Override
   public TypeChecked visit(IntArrayType n)
   {
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = n;
     return t;
   }
 
   @Override
   public TypeChecked visit(BooleanType n)
   {
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = n;
     return t;
   }
 
   @Override
   public TypeChecked visit(IntegerType n)
   {
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = n;
     return t;
   }
 
   @Override
   public TypeChecked visit(ObjectType n)
   {
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = n;
     return t;
   }
 
   @Override
   public TypeChecked visit(Block b)
   {
     return b.statements.accept(this);
   }
 
   @Override
   public TypeChecked visit(If n)
   {
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.tst.accept(this);
     if(t != null && !t.type.equals(new BooleanType()))
     {
       this.error.typeError(n.tst, new BooleanType(), t.type);
       return null;
     }
     
     n.thn.accept(this);
     n.els.accept(this);
     
     return t;
   }
 
   @Override
   public TypeChecked visit(While n)
   {
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.tst.accept(this);
     if(t != null && !t.type.equals(new BooleanType()))
     {
       this.error.typeError(n.tst, new BooleanType(), t.type);
       return null;
     }
     
     n.body.accept(this);
     
     return t;
   }
 
   @Override
   public TypeChecked visit(Print n)
   {
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.exp.accept(this);
     if(t != null && !t.type.equals(new IntegerType()))
     {
       this.error.typeError(n.exp, new IntegerType(), t.type);
       return null;
     }
     
     return t;
   }
 
   @Override
   public TypeChecked visit(Assign n)
   {
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.value.accept(this);
     if(t == null)
     {
       return null;
     }
     
     VarInfo v = this.lookupVarInfo(n.name);
     if(v == null)
     {
       return null;
     }
     
     if(!t.type.equals(v.type))
     {
       this.error.typeError(n.value, v.type, t.type);
       return null;
     }
     
     return t;
   }
 
   @Override
   public TypeChecked visit(ArrayAssign n)
   {
     VarInfo v = this.lookupVarInfo(n.name);
     if(v == null)
     {
       return null;
     }
     
     if(!v.type.equals(new IntArrayType()))
     {
       this.error.typeError(new IdentifierExp(n.name), new IntArrayType(), v.type);
       return null;
     }
     
     TypeCheckedImplementation t1 = (TypeCheckedImplementation) n.value.accept(this),
                               t2 = (TypeCheckedImplementation) n.index.accept(this);
     boolean b1 = t1.type.equals(new IntegerType()),
             b2 = t2.type.equals(new IntegerType());
     
     if(!b1 || !b2)
     {
       if(!b1)
       {
         this.error.typeError(n.value, new IntegerType(), t1.type);
       }
       
       if(!b2)
       {
         this.error.typeError(n.index, new IntegerType(), t2.type);
       }
       
       return null;
     }
     
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = new IntegerType();
     return t;
   }
 
   @Override
   public TypeChecked visit(And n)
   {
     TypeCheckedImplementation t1 = (TypeCheckedImplementation) n.e1.accept(this),
                               t2 = (TypeCheckedImplementation) n.e2.accept(this);
     boolean b1 = t1.type.equals(new BooleanType()),
             b2 = t2.type.equals(new BooleanType());
     
     if(!b1 || !b2)
     {
       if(!b1)
       {
         this.error.typeError(n.e1, new BooleanType(), t1.type);
       }
       
       if(!b2)
       {
         this.error.typeError(n.e2, new BooleanType(), t2.type);
       }
       
       return null;
     }
     
     return t1;
   }
 
   @Override
   public TypeChecked visit(LessThan n)
   {
     TypeCheckedImplementation t1 = (TypeCheckedImplementation) n.e1.accept(this),
                               t2 = (TypeCheckedImplementation) n.e2.accept(this);
     boolean b1 = t1.type.equals(new IntegerType()),
             b2 = t2.type.equals(new IntegerType());
     
     if(!b1 || !b2)
     {
       if(!b1)
       {
         this.error.typeError(n.e1, new IntegerType(), t1.type);
       }
       
       if(!b2)
       {
         this.error.typeError(n.e2, new IntegerType(), t2.type);
       }
       
       return null;
     }
     
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = new BooleanType();
     return t;
   }
 
   @Override
   public TypeChecked visit(Plus n)
   {
     TypeCheckedImplementation t1 = (TypeCheckedImplementation) n.e1.accept(this),
                               t2 = (TypeCheckedImplementation) n.e2.accept(this);
     boolean b1 = (t1 == null || t1.type.equals(new IntegerType())),
             b2 = (t2 == null || t2.type.equals(new IntegerType()));
     
     if(!b1 || !b2)
     {
       if(!b1)
       {
         this.error.typeError(n.e1, new IntegerType(), t1.type);
       }
       
       if(!b2)
       {
         this.error.typeError(n.e2, new IntegerType(), t2.type);
       }
       
       return null;
     }
     
     return t1;
   }
 
   @Override
   public TypeChecked visit(Minus n)
   {
     TypeCheckedImplementation t1 = (TypeCheckedImplementation) n.e1.accept(this),
                               t2 = (TypeCheckedImplementation) n.e2.accept(this);
     boolean b1 = t1.type.equals(new IntegerType()),
             b2 = t2.type.equals(new IntegerType());
     
     if(!b1 || !b2)
     {
       if(!b1)
       {
         this.error.typeError(n.e1, new IntegerType(), t1.type);
       }
       
       if(!b2)
       {
         this.error.typeError(n.e2, new IntegerType(), t2.type);
       }
       
       return null;
     }
     
     return t1;
   }
 
   @Override
   public TypeChecked visit(Times n)
   {
     TypeCheckedImplementation t1 = (TypeCheckedImplementation) n.e1.accept(this),
                               t2 = (TypeCheckedImplementation) n.e2.accept(this);
     boolean b1 = t1.type.equals(new IntegerType()),
             b2 = t2.type.equals(new IntegerType());
     
     if(!b1 || !b2)
     {
       if(!b1)
       {
         this.error.typeError(n.e1, new IntegerType(), t1.type);
       }
       
       if(!b2)
       {
         this.error.typeError(n.e2, new IntegerType(), t2.type);
       }
       
       return null;
     }
     
     return t1;
   }
 
   @Override
   public TypeChecked visit(ArrayLookup n)
   {
     TypeCheckedImplementation t1 = (TypeCheckedImplementation) n.array.accept(this),
                               t2 = (TypeCheckedImplementation) n.index.accept(this);
     
     if(!t1.type.equals(new IntArrayType()))
     {
       this.error.typeError(n.array, new IntArrayType(), t1.type);
       return null;
     }
     
     if(!t2.type.equals(new IntegerType()))
     {
       this.error.typeError(n.index, new IntegerType(), t2.type);
       return null;
     }
     
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = new IntegerType();
     return t;
   }
 
   @Override
   public TypeChecked visit(ArrayLength n)
   {
     TypeCheckedImplementation t1 = (TypeCheckedImplementation) n.array.accept(this);
     if(!t1.type.equals(new IntArrayType()))
     {
       this.error.typeError(n.array, new IntArrayType(), t1.type);
       return null;
     }
     
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = new IntegerType();
     return t;
   }
 
   @Override
   public TypeChecked visit(Call n) {
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.receiver.accept(this);
     
     if(t == null)
     {
       return null;
     }
     
     if(!(t.type instanceof ObjectType))
     {
       this.error.typeErrorExpectObjectType(n.receiver, t.type);
       return null;
     }
     
     ObjectType objectType = (ObjectType) t.type;
     ClassInfo c = this.lookupClassInfo(objectType.name);
     if(c == null)
     {
       this.error.undefinedId(objectType.name);
       return null;
     }
     
     // Check for method declaration
     // Iterate entire parent hierarchy in case method is inherited
     MethodInfo m = null;
     ClassInfo currentInfo = c;
     do
     {
       m = (MethodInfo) currentInfo.methods.lookup(n.name);
       if(m != null)
       {
         // Method found in class hierarchy
         break;
       }
       
       currentInfo = this.lookupClassInfo(currentInfo.superClass);
     }
     while(currentInfo != null);
     
     if(m == null)
     {
       this.error.undefinedId(n.name);
       return null;
     }
     
     if(m.formalsList.size() != n.rands.size())
     {
       this.error.wrongNumberOfArguments(n, m.formalsList.size());
       return null;
     }
     
     for(int i = 0; i < m.formalsList.size(); i++)
     {
       TypeCheckedImplementation randType = (TypeCheckedImplementation) n.rands.elementAt(i).accept(this);
       if(randType != null && !m.formalsList.get(i).type.equals(randType.type))
       {
        this.error.typeError(n.rands.elementAt(i), m.formalsList.get(i).type, randType.type);
       }
     }
     
     t.type = m.returnType;
     return t;
   }
 
   @Override
   public TypeChecked visit(IntegerLiteral n)
   {
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = new IntegerType();
     return t;
   }
 
   @Override
   public TypeChecked visit(BooleanLiteral n)
   {
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = new BooleanType();
     return t;
   }
 
   @Override
   public TypeChecked visit(IdentifierExp n)
   {
     VarInfo v = this.lookupVarInfo(n.name);
     if(v == null)
     {
       return null;
     }
     
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = v.type;
     return t;
   }
 
   @Override
   public TypeChecked visit(This n) {
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = new ObjectType(this.currentClass);
     return t;
   }
 
   @Override
   public TypeChecked visit(NewArray n)
   {
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = new IntArrayType();
     return t;
   }
 
   @Override
   public TypeChecked visit(NewObject n)
   {
     ClassInfo c = this.lookupClassInfo(n.typeName);
     if(c == null)
     {
       this.error.undefinedId(n.typeName);
       return null;
     }
     
     TypeCheckedImplementation t = new TypeCheckedImplementation();
     t.type = new ObjectType(n.typeName);
     return t;
   }
 
   @Override
   public TypeChecked visit(Not n)
   {
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.e.accept(this);
     if(t != null && !t.type.equals(new BooleanType()))
     {
       this.error.typeError(n.e, new BooleanType(), t.type);
     }
     
     return t;
   }
   
   private ClassInfo lookupClassInfo(String id)
   {
     return (ClassInfo) this.table.lookup(id);
   }
 
   private VarInfo lookupVarInfo(String id)
   {
     ClassInfo c = this.lookupClassInfo(this.currentClass);
     MethodInfo m = (MethodInfo) c.methods.lookup(this.currentMethod);
     Info v = null;
     
     if(m != null)
     {
       v = m.locals.lookup(id);
       if(v == null)
       {
         v = m.formals.lookup(id);
       }
     }
     
     if(v == null)
     {
       // Check for identifier in class declaration
       // If not found, check if it is inherited
       // Iterate entire parent hierarchy
       do
       {
         v = c.fields.lookup(id);
         if(v != null)
         {
           // Identifier found in class hierarchy
           break;
         }
         
         c = this.lookupClassInfo(c.superClass);
       }
       while(c != null);
       
       if(v == null)
       {
         this.error.undefinedId(id);
         return null;
       }
     }
     
     return (VarInfo) v;
   }
 }
