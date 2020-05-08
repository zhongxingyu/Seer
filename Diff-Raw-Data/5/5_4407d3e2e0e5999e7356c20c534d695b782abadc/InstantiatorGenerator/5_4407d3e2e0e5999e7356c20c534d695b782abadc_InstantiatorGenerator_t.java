 package cz.cvut.fit.hybljan2.apitestingcg.cmgenerator;
 
 import com.sun.codemodel.*;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.*;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.GeneratorConfiguration;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.WhitelistRule;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Jan Hýbl
  * Date: 11.2.12
  * Time: 17:00
  */
 public class InstantiatorGenerator extends ClassGenerator {
 
     public InstantiatorGenerator(GeneratorConfiguration configuration) {
         super(configuration);
     }
 
     @Override
     public void visit(APIClass apiClass) {
 
         // instantiator can be generated for classes and interfaces
         if((!apiClass.getType().equals(APIItem.Kind.CLASS)) && (!apiClass.getType().equals(APIItem.Kind.INTERFACE))) return;
 
         // check if extender for this class is enabled in jobConfiguration.
         if(!isEnabled(apiClass.getFullName(), WhitelistRule.RuleItem.INSTANTIATOR)) return;
 
         try {        
             visitingClass = apiClass;
             
             // declare new class
             cls = cm._class(currentPackageName + '.' + generateName(configuration.getInstantiatorClassIdentifier(), apiClass.getName()));
 
             // visit all constructors
             for(APIMethod constructor : apiClass.getConstructors()) {
                 visitConstructor(constructor);
             }
 
             // genetate test of extending - cant be performed if tested class has no constructors
             if(apiClass.getExtending() != null && !apiClass.getConstructors().isEmpty()) {
                 addCreateInstanceMethod(apiClass.getExtending(),generateName(configuration.getCreateSuperInstanceIdentifier(), apiClass.getExtending()),apiClass.getConstructors().first().getParameters(), false);
             }
 
             // genetate test of implementing - cant be performed if tested class has no constructors
             if(!apiClass.getImplementing().isEmpty() && !apiClass.getConstructors().isEmpty()) {
                 for(String implementing : apiClass.getImplementing()) {
                     addCreateInstanceMethod(implementing,generateName(configuration.getCreateSuperInstanceIdentifier(), implementing),apiClass.getConstructors().first().getParameters(), false);
                 }
             }
             
             // visit all methods
             for(APIMethod method : apiClass.getMethods()) {
                 method.accept(this);
             }
 
             if(!apiClass.getFields().isEmpty()) {
                 JMethod fieldsMethod = cls.method(JMod.PUBLIC,cm.VOID,configuration.getFieldTestIdentifier());
                 fieldsMethodBlock = fieldsMethod.body();
             }
             // visit all fields
             for(APIField field : apiClass.getFields()) {
                 field.accept(this);
             }        
             
         } catch (JClassAlreadyExistsException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     /**
      * Generates tests for given constructors.
      * Two createInstance methods are generated. First simply creates a new instance using tested constructor.
      * Second method do same thing but with null parameters. Null constructor can't be generated in every
      * case. Only in cases when constructor is unique.
      * @param constructor
      */
     public void visitConstructor(APIMethod constructor) {
         // Check if constructor is enabled in job configuration.
         if(!isEnabled(methodSignature(constructor,visitingClass.getFullName()), WhitelistRule.RuleItem.INSTANTIATOR)) return;
 
         // create basic create new instance method
         addCreateInstanceMethod(visitingClass.getFullName(), generateName(configuration.getCreateInstanceIdentifier(), constructor.getName()), constructor.getParameters(), false);
 
         // if it is possible, create null version of previous constructor
         // nonparam constructor can't be tested with null values.
         if(constructor.getParameters().isEmpty()) return;
 
         // Check if there is no other same constructor
         boolean unique = true;           
         for(APIMethod c : visitingClass.getConstructors()) {
             // if the tested constructor is equal to c constructor, it's not unique.
             unique = !equalsNullParams(c.getParameters(), constructor.getParameters());
             if(!unique) return; // if it's not unique constructor, skip generating of null constructor.
         }
 
         // generate null constructor (same as previous, but params are NULLs).
         addCreateInstanceMethod(constructor.getReturnType(), generateName(configuration.getCreateNullInstanceIdentifier(), constructor.getName()), constructor.getParameters(), true);
     }
 
     /**
      * Generates test of the field.
      * Final fields are tested by assigning their value to new field of same type. Ex: {@code int x = x;}. New
      * local variable x hides original super field x, but it doesn't mind.
      * Non-final fields are tested by assigning some value to them. Ex: {@codeFile f = null; fileField = f;}
      * @param apiField
      */
     @Override
     public void visit(APIField apiField) {
         if(apiField.getModifiers().contains(APIModifier.Modifier.PUBLIC)) {
             if(apiField.getModifiers().contains(APIModifier.Modifier.FINAL)) {
                 // create new local variable and assing original value to it
                 fieldsMethodBlock.decl(getClassRef(apiField.getVarType()), apiField.getName(), JExpr.ref(apiField.getName()));
             } else {
                 // create new field of same type as original
                 String fldName = generateName(configuration.getFieldTestVariableIdentifier(), apiField.getName());
                 JVar var = fieldsMethodBlock.decl(getClassRef(apiField.getVarType()),fldName,getDefaultPrimitiveValue(apiField.getVarType()));
                 fieldsMethodBlock.assign(JExpr.ref(apiField.getName()), var);
             }
             fieldsMethodBlock.directStatement(" ");
         }
     }
 
     @Override
     public void visit(APIMethod method) {
         // is method enabled in configuration
         if(!isEnabled(methodSignature(method,visitingClass.getFullName()), WhitelistRule.RuleItem.INSTANTIATOR)) return;
 
         // if it is possible, create null version of previous method caller
         // method without parameters cant be called with null parameters.
 
         // Check if there is no other same method caller
         boolean unique = true;
 
         if(method.getParameters().isEmpty()) unique = false;
         else for(APIMethod m : visitingClass.getMethods()) {
             // if the tested method is equal to m method, it's not unique.
             if(!method.equals(m)) { // don't compare object to itself
                 unique = !((method.getName().equals(m.getName()))&&(equalsNullParams(m.getParameters(), method.getParameters())));
                 if(!unique) break;
             }
         }
 
         addMethodCaller(method,unique);
     }
 
     private boolean equalsNullParams(List<String> paramsA, List<String> paramsB) {
         if(paramsA.size() != paramsB.size()) return false;
         Iterator<String> itA = paramsA.iterator();
         Iterator<String> itB = paramsB.iterator();
         while(itA.hasNext()) {
             String paramA = getDefaultPrimitiveValueString(itA.next());
             String paramB = getDefaultPrimitiveValueString(itB.next());
             if(!paramA.equals(paramB)) return false;
         }
         return true;
     }
 
     /**
      * Create new metod like this template:
      *
      * public instanceClassName methodName(args[0] a, args[1] b, ...) {
      *     return new visitingClassName(a, b);
      * }
      *
      * @param instanceClassName
      * @param methodName
      * @param args
      * @param nullParams
      */
     private void addCreateInstanceMethod(String instanceClassName, String methodName, List<String> args, boolean nullParams) {
         JClass returnCls = getClassRef(instanceClassName);
         JMethod result = cls.method(JMod.PUBLIC, returnCls, methodName);
         JInvocation newInstance = JExpr._new(getClassRef(visitingClass.getFullName()));
         char argName = 'a';
         for(String arg : args) {
             result.param(getClassRef(arg), String.valueOf(argName));
             if(nullParams) newInstance.arg(getDefaultPrimitiveValue(arg));
             else newInstance.arg(JExpr.ref(String.valueOf(argName)));
             argName++;
         }
         result.body()._return(newInstance);
     }
 
     
     private void addMethodCaller(APIMethod method, boolean generateNullCall) {
         
         int methodMods = JMod.PUBLIC;
         JType returnType = getClassRef(method.getReturnType());
         String callerName = generateName(configuration.getMethodCallIdentifier(),method.getName());
         String nullCallerName = generateName(configuration.getMethodNullCallIdentifier(),method.getName());
 
         JMethod caller = cls.method(methodMods,returnType,callerName);
         JMethod nullCaller = cls.method(methodMods,returnType,nullCallerName);
 
         // define method invocation
         JInvocation invocation;
         JInvocation nullInvocation;
 
         // set invocation
         if(method.getModifiers().contains(APIModifier.Modifier.STATIC)) {  // Static method - instance = Class name
             invocation = getClassRef(visitingClass.getFullName()).staticInvoke(method.getName());
             nullInvocation = getClassRef(visitingClass.getFullName()).staticInvoke(method.getName());
         } else { // instance is first parameter
             JExpression instance = caller.param(getClassRef(visitingClass.getFullName()), configuration.getInstanceIdentifier());
             JExpression nullInstance = nullCaller.param(getClassRef(visitingClass.getFullName()),configuration.getInstanceIdentifier());
             invocation = instance.invoke(method.getName());
             nullInvocation = nullInstance.invoke(method.getName());
         }
 
         // add parameter to the method and invocation - new method has same parameters as original method
         char pName = 'a';
         for(String pType : method.getParameters()) {
             String name = String.valueOf(pName++);
             JClass type = getClassRef(pType);
             JVar param = caller.param(type,name);
             nullCaller.param(type,name);
             invocation.arg(param);
             nullInvocation.arg(getDefaultPrimitiveValue(pType));
         }
 
         JBlock callerBody = caller.body();
         JBlock nullCallerBody = nullCaller.body();
         
         // add try-catch block if method trows any exception
         if(!method.getThrown().isEmpty()) {
             JTryBlock tryBlock = caller.body()._try();
             JTryBlock nullTryBlock = nullCaller.body()._try();
             callerBody = tryBlock.body();
             nullCallerBody = nullTryBlock.body();
             char eName = 'E';
             for(String exceptionType : method.getThrown()) {
                 JClass exception = getClassRef(exceptionType);
                 String exceptionParam = "ex" + String.valueOf(eName++);
                 tryBlock._catch(exception).param(exceptionParam);
                 nullTryBlock._catch(exception).param(exceptionParam);
             }

            if(!method.getReturnType().equals("void")) {
                caller.body()._return(getDefaultPrimitiveValue(method.getReturnType()));
                nullCaller.body()._return(getDefaultPrimitiveValue(method.getReturnType()));
            }
         }
 
         if(method.getReturnType().equals("void")) {
             callerBody.add(invocation);
             nullCallerBody.add(nullInvocation);
         } else {
             callerBody._return(invocation);
             nullCallerBody._return(invocation);
         }
 
 
 
         if(!generateNullCall) {
             cls.methods().remove(nullCaller);
         }
     }
     
     private void oldaddMethodCaller(APIMethod method, boolean nullParams) {
         JClass returnCls = getClassRef(method.getReturnType());
 
         JMethod caller = cls.method(JMod.PUBLIC, returnCls, generateName(configuration.getMethodCallIdentifier(),method.getName()));
         
         JMethod nullCaller = null;
         if(nullParams) nullCaller = cls.method(JMod.PUBLIC, returnCls, generateName(configuration.getMethodNullCallIdentifier(),method.getName()));
         
         JInvocation methodCall;
 
         JInvocation nullMethodCall;
 
         // call static method?
         if(method.getModifiers().contains(APIModifier.Modifier.STATIC)) {
             JClass instance = getClassRef(visitingClass.getFullName());
             methodCall = instance.staticInvoke(method.getName());
             nullMethodCall = instance.staticInvoke(method.getName());
         } else {
             caller.param(getClassRef(visitingClass.getFullName()),configuration.getInstanceIdentifier());
             if(nullParams) nullCaller.param(getClassRef(visitingClass.getFullName()),configuration.getInstanceIdentifier());
             JExpression instance = JExpr.ref(configuration.getInstanceIdentifier());
             methodCall = JExpr.invoke(instance,method.getName());
             nullMethodCall = JExpr.invoke(instance,method.getName());
         }
 
         char argName = 'a';
         for(String argType : method.getParameters()) {
             caller.param(getClassRef(argType), String.valueOf(argName));
             if(nullParams) nullCaller.param(getClassRef(argType), String.valueOf(argName));
             methodCall.arg(getDefaultPrimitiveValue(argType));
             nullMethodCall.arg(JExpr.ref(String.valueOf(argName)));
             argName++;
         }
 
         if(method.getReturnType().equals("void")) caller.body().add(methodCall);
         else caller.body()._return(methodCall);
 
         if(nullParams) {
             //JMethod nullCaller = cls.method(JMod.PUBLIC, returnCls, generateName(configuration.getMethodNullCallIdentifier(),method.getName()));
             if(method.getReturnType().equals("void")) nullCaller.body().add(nullMethodCall);
             else nullCaller.body()._return(nullMethodCall);
         }
     }    
 }
