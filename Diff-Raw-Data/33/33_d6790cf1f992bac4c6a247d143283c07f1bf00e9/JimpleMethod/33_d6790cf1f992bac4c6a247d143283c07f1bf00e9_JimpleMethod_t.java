 /*
  * 03/25/2013 - 1.0
  * 
  * this class defines a JimpleMethod which receives JimpleElement as its body
  * and should be added to JimpleClass
  * 
  */
 package edu.syr.bytecast.jimple.beans.jimpleBean;
 
 import java.util.ArrayList;
 import java.util.List;
 import soot.*;
 import soot.jimple.IdentityStmt;
 import soot.jimple.Jimple;
 import soot.jimple.JimpleBody;
 import soot.util.Chain;
 
 /**
  *
  * @author col
  */
 public class JimpleMethod {
 
   private ArrayList<String> parameters_type;
   private String methodName;
   private String returnType;
   private SootClass declaringClass;
   private int modifier;
   private JimpleBody jBody;
   private PatchingChain<Unit> units;
   private Chain<Local> locals;
   private SootMethod myMethod;
   /**
    *
    * @param methodName
    * @param returnType
    * @param modifier (public:1 , private:2 , static:8 ( if u want to use
    * (public+static) :value will be 9))
    * @param parameters_type
    */
   public JimpleMethod(int modifier, String returnType,
           String methodName, ArrayList<String> parameters_type, JimpleClass declaringClass) {
 
     this.methodName = methodName;
     this.modifier = modifier;
     this.returnType = returnType;
     this.parameters_type = parameters_type;
     this.declaringClass = declaringClass.getSClass();
     createMethod();
   }
 
   private void createMethod() {
 
     List<Type> parameters = new ArrayList<Type>();
     if (parameters_type != null) {
       for (String tp : parameters_type) {
         parameters.add(JimpleUtil.getTypeByString(tp));
       }
     }
 
     myMethod = new SootMethod(methodName, parameters,
             JimpleUtil.getTypeByString(returnType), modifier);
     //mySootclass.addMethod(myMethod);
 
     //create jimple body
     jBody = Jimple.v().newBody(myMethod);
     myMethod.setActiveBody(jBody);
 
     units = jBody.getUnits();
     locals = jBody.getLocals();
     initMethod();
   }
 
   private void initMethod() {
     if (methodName.equals("main")) {
       // java.lang.String[] p0
       Local para0 = Jimple.v().newLocal("p0", 
               JimpleUtil.getTypeByString("String[]"));
       locals.add(para0);
       // p0 := @parameter0: java.lang.String[];
       Value para_assi = Jimple.v().newParameterRef(JimpleUtil.getTypeByString("String[]"), 0);
       IdentityStmt parastmt = Jimple.v().newIdentityStmt(para0, para_assi);
       units.add(parastmt);
     } else {
       // Class r0;
       Local thisref = Jimple.v().newLocal("r0", declaringClass.getType());
       locals.add(thisref);
       
       // r0 := @this: Class;
       Value this_rhs = Jimple.v().newThisRef(declaringClass.getType());
       IdentityStmt thistmt = Jimple.v().newIdentityStmt(thisref, this_rhs);
       units.add(thistmt);
 
     }
     declaringClass.addMethod(myMethod);
   }
   
   public void initMain() {
       
   }
 
   public List<String> getParameterTypes() {
     if (this.parameters_type != null) {
       return this.parameters_type;
 
     } else {
       return null;
     }
 
   }
 
   protected SootMethod getMethod() {
 
     return myMethod;
   }
 
   public String getMethodName(){
       
       return methodName;
   }
   
   public void addElement(JimpleElement jm) {
     if (jm.getVariable() != null) {
       myMethod.getActiveBody().getLocals().add(jm.getVariable());
     }
     if (jm.getElement() != null) {
       units.add(jm.getElement());
     }
   }
 
   public String getReturnType() {
     return this.returnType;
   }
 
  /**
   * 
   * @param returnvariable create void return statment if null
   */
   public void setReturn(JimpleVariable returnvariable) {
    if (returnvariable == null) {
      Unit returnstmt = Jimple.v().newReturnVoidStmt();
      units.add(returnstmt);
    } else {
      Unit returnstmt = Jimple.v().newReturnStmt(returnvariable.getVariable());
      units.add(returnstmt);
    }
   }
 }
