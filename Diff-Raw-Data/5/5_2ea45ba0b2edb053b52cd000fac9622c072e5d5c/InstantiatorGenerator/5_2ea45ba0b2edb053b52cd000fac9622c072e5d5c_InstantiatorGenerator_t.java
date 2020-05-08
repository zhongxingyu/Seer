 package cz.cvut.fit.hybljan2.apitestingcg.generator;
 
 import com.sun.codemodel.*;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.*;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.GeneratorConfiguration;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.WhitelistRule;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.*;
 
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
         if ((!apiClass.getKind().equals(APIItem.Kind.CLASS))
                 && (!apiClass.getKind().equals(APIItem.Kind.INTERFACE))
                 && (!apiClass.getKind().equals(APIItem.Kind.ENUM))) {
             return;
         }
 
         // check if instantiator for this class is enabled in jobConfiguration.
         if (!isEnabled(apiClass.getFullName(), WhitelistRule.RuleItem.INSTANTIATOR)) {
             return;
         }
 
         // only public classes can be tested by instantiator.
         if (!apiClass.getModifiers().contains(APIModifier.PUBLIC)) {
             return;
         }
 
         // check if class is not deprecated. If it does and in job configuration
         // are deprecated items disabled, this class is skipped.
         if (apiClass.isDepreacated() && jobConfiguration.isSkipDeprecated()) {
             return;
         }
 
         try {
             visitingClass = apiClass;
 
             // declare new class
             String className = generateName(configuration.getInstantiatorClassIdentifier(), apiClass.getName());
             cls = declareNewClass(JMod.PUBLIC, currentPackageName, className, visitingClass.isNested());
 
             // visit all constructors, if class isn't abstract
             if (!visitingClass.getModifiers().contains(APIModifier.ABSTRACT)) {
                 for (APIMethod constructor : apiClass.getConstructors()) {
                     visitConstructor(constructor);
                 }
             }
 
             // genetate test of extending - cant be performed if tested class has no public constructors or is abstract
             if (apiClass.getExtending() != null
                     && !visitingClass.getModifiers().contains(APIModifier.ABSTRACT)) {
 
                 // find first public constructor and use it for creating new instance.
                 for (APIMethod constructor : apiClass.getConstructors()) {
                     if (constructor.getModifiers().contains(APIModifier.PUBLIC) &&
                             (!constructor.isDepreacated() || !jobConfiguration.isSkipDeprecated())) {
                         String name = generateName(configuration.getCreateSuperInstanceIdentifier(), apiClass.getExtending().getName());
                         addCreateInstanceMethod(apiClass.getExtending(), name, constructor, false);
                         break;
                     }
                 }
 
             }
 
             // genetate test of implementing - cant be performed if tested class has no constructors
             if (!apiClass.getImplementing().isEmpty()
                     && !apiClass.getConstructors().isEmpty()
                     && !visitingClass.getModifiers().contains(APIModifier.ABSTRACT)) {
 
                 // find first public constructor and use it for creating new instances of all implemented interfaces.
                 for (APIMethod constructor : apiClass.getConstructors()) {
                     if (constructor.getModifiers().contains(APIModifier.PUBLIC) &&
                             (!constructor.isDepreacated() || !jobConfiguration.isSkipDeprecated())) {
                         for (APIType implementing : apiClass.getImplementing()) {
                             String name = generateName(configuration.getCreateSuperInstanceIdentifier(), implementing.getName());
                             addCreateInstanceMethod(implementing, name, constructor, false);
                         }
                         break;
                     }
                 }
             }
 
             // visit all methods
             for (APIMethod method : apiClass.getMethods()) {
                 method.accept(this);
             }
 
             if (!apiClass.getFields().isEmpty()) {
                 JMethod fieldsMethod = cls.method(JMod.PUBLIC, cm.VOID, configuration.getFieldTestIdentifier());
                 // add generics of the instantiator to the method
                 addGenerics(fieldsMethod);
 //
 //                String instanceClassName = visitingClass.getFullName();
 //
 //                if (!visitingClass.getTypeParamsMap().isEmpty()) {
 //                    if (!visitingClass.getTypeParamsMap().isEmpty()) {
 //                        instanceClassName += "<";
 //                        boolean first = true;
 //                        for (String typeName : visitingClass.getTypeParamsMap().keySet()) {
 //                            if (first) {
 //                                instanceClassName += typeName;
 //                                first = false;
 //                            } else {
 //                                instanceClassName += ", " + typeName;
 //                            }
 //                        }
 //                        instanceClassName += "> ";
 //                    }
 //                }
 //                JClass instanceClassRef = getGenericsClassRef(instanceClassName);
                 JClass instanceClassRef = getTypeRef(visitingClass.getType());
 
                 fieldsInstance = fieldsMethod.param(instanceClassRef, configuration.getInstanceIdentifier());
 
                 fieldsMethodBlock = fieldsMethod.body();
             }
             // visit all fields
             for (APIField field : apiClass.getFields()) {
                 field.accept(this);
             }
 
             classStack.push(cls);
 
             // visit all nested classes
             for (APIClass nestedClass : apiClass.getNestedClasses()) {
                 nestedClass.accept(this);
             }
 
             cls = classStack.pop();
 
         } catch (JClassAlreadyExistsException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     /**
      * Generates tests for given constructors.
      * Two createInstance methods are generated. First simply creates a new instance using tested constructor.
      * Second method do same thing but with null parameters. Null constructor can't be generated in every
      * case. Only in cases when constructor is unique.
      *
      * @param constructor
      */
     public void visitConstructor(APIMethod constructor) {
 
         if (constructor.isDepreacated() && jobConfiguration.isSkipDeprecated()) {
             return;
         }
 
         // Check if constructor is enabled in job configuration.
         if (!isEnabled(methodSignature(constructor, visitingClass.getFullName()), WhitelistRule.RuleItem.INSTANTIATOR)) {
             return;
         }
 
         // only public constructors can be used in instantiator
         if (!constructor.getModifiers().contains(APIModifier.PUBLIC)) return;
 
         // create basic create new instance method
         addCreateInstanceMethod(visitingClass.getType(), generateName(configuration.getCreateInstanceIdentifier(), constructor.getName()), constructor, false);
 
         // if it is possible, create null version of previous constructor
         // nonparam constructor can't be tested with null values.
         if (constructor.getParameters().isEmpty()) {
             return;
         }
 
         if (constructor.isDepreacated() && jobConfiguration.isSkipDeprecated()) {
             return;
         }
 
         // Check if there is no other same constructor
         boolean unique = true;
         for (APIMethod c : visitingClass.getConstructors()) {
             // if the tested constructor is equal to c constructor, it's not unique.
             unique = !equalsNullParams(c.getParameters(), constructor.getParameters());
             if (!unique) return; // if it's not unique constructor, skip generating of null constructor.
         }
 
         // generate null constructor (same as previous, but params are NULLs).
         addCreateInstanceMethod(visitingClass.getType(), generateName(configuration.getCreateNullInstanceIdentifier(), constructor.getName()), constructor, true);
     }
 
     /**
      * Generates test of the public field.
      * Final fields are tested by assigning their value to new field of same type. Ex: {@code int x = instance.x;}. New
      * local variable x hides original super field x, but it doesn't mind.
      * Non-final fields are tested by assigning some value to them. Ex: {@codeFile f = null; fileField = f;}
      *
      * @param apiField
      */
     @Override
     public void visit(APIField apiField) {
         if (apiField.isDepreacated() && jobConfiguration.isSkipDeprecated()) {
             return;
         }
 
         // field has to be public
         if (apiField.getModifiers().contains(APIModifier.PUBLIC)) {
             // type of the field has to be public class
             if(!checkTypeAccessModifier(APIModifier.PUBLIC, apiField.getVarType(), visitingClass.getTypeParamsMap().keySet())) {
                 return;
             }
 
             // Check if field is constant or variable
             if (apiField.getModifiers().contains(APIModifier.FINAL)) {
                 // original field
                 JFieldRef fld;
                 if (apiField.getModifiers().contains(APIModifier.STATIC)) {
                     fld = getTypeRef(visitingClass.getFullName(), visitingClass.getTypeParamsMap().keySet()).staticRef(apiField.getName());
                 } else {
                     fld = fieldsInstance.ref(apiField.getName());
                 }
                 // create new local variable and assign original value to it
                 fieldsMethodBlock.decl(getTypeRef(apiField.getVarType(), visitingClass.getTypeParamsMap().keySet()), apiField.getName(), fld);
             } else {
                 // create new field of same type as original
                 String fldName = generateName(configuration.getFieldTestVariableIdentifier(), apiField.getName());
                 JVar var = fieldsMethodBlock.decl(getTypeRef(apiField.getVarType(), visitingClass.getTypeParamsMap().keySet()), fldName, getPrimitiveValue(apiField.getVarType().getName()));
                 JAssignmentTarget assignmentTarget = null;
                 if (apiField.getModifiers().contains(APIModifier.STATIC)) {
                     assignmentTarget = getTypeRef(visitingClass.getFullName(), visitingClass.getTypeParamsMap().keySet())
                             .staticRef(apiField.getName());
                 } else {
                     assignmentTarget = fieldsInstance.ref(apiField.getName());
                 }
                 fieldsMethodBlock.assign(assignmentTarget, var);
             }
         }
     }
 
     @Override
     public void visit(APIMethod method) {
         // is method enabled in configuration
         String signature = methodSignature(method, visitingClass.getFullName());
         if (!isEnabled(signature, WhitelistRule.RuleItem.INSTANTIATOR))
             return;
 
         // only public method can be tested by instantiator
         if (!method.getModifiers().contains(APIModifier.PUBLIC)) {
             return;
         }
 
         // return type have to be public class
         if (!isTypePublic(method.getReturnType(), method.getTypeParamsMap().keySet())) {
             return;
         }
 
         // all methods params has to be public classes
         for (APIMethodParameter paramType : method.getParameters()) {
             if (!isTypePublic(paramType.getType(), method.getTypeParamsMap().keySet())) {
                 return;
             }
         }
 
         if (method.isDepreacated() && jobConfiguration.isSkipDeprecated()) {
             return;
         }
 
         // if it is possible, create null version of previous method caller
         // method without parameters cant be called with null parameters.
 
         boolean generateNullCaller = isEnabled(signature, WhitelistRule.RuleItem.NULLCALL);
 
         // Check if method has some non-primitive parameters. If not, null caller won't be generated.
         if (generateNullCaller) {
             generateNullCaller = checkPrimitiveParams(method);
         }
 
         // Check if there is no other same method caller
         if (generateNullCaller) {
             generateNullCaller = checkNullCollision(method, visitingClass);
         }
 
         addMethodCaller(method, generateNullCaller);
     }
 
     /**
      * Check if method has some non-primitive parameters. Return true if it has, false if method has only primitive
      * params.
      *
      * @param method Checked method
      * @return
      */
     private boolean checkPrimitiveParams(APIMethod method) {
         for (APIMethodParameter parameter : method.getParameters()) {
             if (!parameter.isPrimitive()) return true;
         }
         return false;
     }
 
     // TODO: tahle metoda se mi moc nelibi... funguje, ale vychazi ze stareho typoveho modelu.
     // mozna by chtelo ji v budoucnu predelat. Hlavne se mi nelibi, ze pri kazdem volani se
     // vola findClass, ktere je ted docela pomale... (sekvencni hledani v API)
     private boolean checkNullCollision(APIMethod method, APIType type) {
         APIClass cls = null;
         try {
             cls = findClass(type.getName());
             return checkNullCollision(method, cls);
         } catch (ClassNotFoundException e) {
             System.err.println("Can't find class with name: \"" + type.getName() + "\"");
         }
         return false;
     }
     
     private boolean checkNullCollision(APIMethod method, String clsName) {
         APIClass cls = null;
         try {
             cls = findClass(clsName);
             return checkNullCollision(method, cls);
         } catch (ClassNotFoundException e) {
             System.err.println("Can't find class with name: \"" + clsName + "\"");
         }
         return false;
     }
 
     private boolean checkNullCollision(APIMethod method, APIClass cls) {
         boolean unique = true;
         // check collision in current class
         for (APIMethod m : cls.getMethods()) {
             // if the tested method is equal to m method, it's not unique.
             if (!method.equals(m)) { // don't compare object to itself
                 unique = !((method.getName().equals(m.getName())) && (equalsNullParams(m.getParameters(), method.getParameters())));
                 if (!unique) return false;
             }
         }
 
         // check collision in super class
         if (cls.getExtending() != null) {
             return checkNullCollision(method, cls.getExtending());
         }
 
         return unique;
     }
 
     private static boolean equalsNullParams(List<APIMethodParameter> paramsA, List<APIMethodParameter> paramsB) {
         if (paramsA.size() != paramsB.size()) {
             return false;
         }
         Iterator<APIMethodParameter> itA = paramsA.iterator();
         Iterator<APIMethodParameter> itB = paramsB.iterator();
         while (itA.hasNext()) {
             String paramA = getPrimitiveValueString(itA.next().getType().getName());
             String paramB = getPrimitiveValueString(itB.next().getType().getName());
             if (!paramA.equals(paramB)) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Checks if given type is public. Type could be simple class (<code>java.util.List</code>) or complex type
      * (<code>java.util.Map<java.lang.String, java.lang.List<java.io.File>></code>). Type is public if every class
      * used in type definition is public.
      *
      * @param type           Definition of the type
      * @param genericClasses List of the generics classes.
      * @return
      */
     protected boolean isTypePublic(String type, Collection<String> genericClasses) {
         boolean result = true;
         // Split complex type to individual classes
         Set<String> classNames = getTypesList(type);
         // check public accessibility of every single class
         for (String className : classNames) {
             // check if it's generic class or wildcard
             if (!((genericClasses != null && genericClasses.contains(className)) || (className.equals("?")))) {
                 // if class is not generic, use isClassPublic method to determine if class is public
                 if (!isClassPublic(className)) {
                     result = false;
                     break;
                 }
             }
         }
         return result;
     }
 
     protected boolean isTypePublic(APIType varType, Collection<String> genericClasses) {
         boolean result = true;
         if((!((genericClasses != null
                 && genericClasses.contains(varType.getName()))
                 || (varType.getName().equals("?"))))) {
             if (!isClassPublic(varType.getName())) {
                 return false;
             }
         }
 
 
         // TODO: chybi testovani typeargu
         return result;
     }
 
     /**
      * Finds class with given name and checks if the class has public modifier.
      *
      * @param name full class name (not complex type)
      * @return true or false if class is public or not. If class is not found (it's not part of API), returns false
      */
     protected boolean isClassPublic(String name) {
 
         // check if class is not generic type
         if (visitingClass.getTypeParamsMap().keySet().contains(name)) {
             return true;
         }
 
         // try to find the class in API or load it with reflection.
         try {
             APIClass c = findClass(name);
             if (!c.getModifiers().contains(APIModifier.PUBLIC)) {
                 return false;
             }
         } catch (ClassNotFoundException e) {
             // if class wasn't found, it could be nested class.
             try {
                 APIClass nc = visitingClass.getNestedClass(name);
                 return nc.getModifiers().contains(APIModifier.PUBLIC);
             } catch (ClassNotFoundException e2) {
                 // it's unknown class
                 System.err.println("Class not found: " + name);
             }
         }
 
         return true;
     }
 
 
     private void addGenerics(JGenerifiable item) {
         if (!visitingClass.getTypeParamsMap().isEmpty()) {
             for (String typeName : visitingClass.getTypeParamsMap().keySet()) {
 
                 boolean alreadyDefined = false;
                 for (JTypeVar typeVar : item.typeParams()) {
                     if (typeName.equals(typeVar.name())) {
                         alreadyDefined = true;
                         break;
                     }
                 }
 
                 if (!alreadyDefined) {
                     
                     JTypeVar type = item.generify(typeName);
                         for (APIType bound : visitingClass.getTypeParamsMap().get(typeName)) {
                             JClass typeBound = getTypeRef(bound, visitingClass.getTypeParamsMap().keySet());
                             if(!bound.equals(new APIType(Object.class))) {
                                 type.bound(typeBound);
                             }
                         }
                     }
             }
         }
     }
 
     /**
      * Create new metod like this template:
      * <p/>
      * public instanceType methodName(args[0] a, args[1] b, ...) {
      * return new visitingClassName(a, b);
      * }
      *
      * @param instanceType
      * @param methodName
      * @param constructor
      * @param nullParams
      */
     private void addCreateInstanceMethod(APIType instanceType, String methodName, APIMethod constructor, boolean nullParams) {
 
         JClass returnCls = getTypeRef(instanceType, constructor.getTypeParamsMap().keySet());
 
         // checks if class is inner. - Inner classes has different constructors.
         boolean innerClass = false;
 
         // only constructors of public class can be generated in instantiator.
         if (!visitingClass.getModifiers().contains(APIModifier.PUBLIC)) {
             return;
         }
         if (visitingClass.isNested() && !visitingClass.getModifiers().contains(APIModifier.STATIC)) {
             innerClass = true;
         }
 
         // new instance has to be public class
         if (!isTypePublic(instanceType, null)) {
             return;
         }
 
         // declare new createInstance method
         JMethod result = cls.method(JMod.PUBLIC, returnCls, methodName);
         JInvocation newInstance;
 
         String typeParam = visitingClass.getTypeParamsMap().isEmpty() ? "" : "< >";
         if (innerClass) {
             String outerClassName = instanceType.getSimpleName();
             result.param(getTypeRef(outerClassName,null), configuration.getInstanceIdentifier());
             newInstance = JExpr._new(cm.ref(visitingClass.getName() + typeParam));
         } else {
             //newInstance = JExpr._new(getGenericsClassRef(visitingClass.getFullName() + typeParam));
             // TODO: pridat genericke typy
             newInstance = JExpr._new(getTypeRef(visitingClass.getType(), constructor.getTypeParamsMap().keySet()));
         }
 
         // add generics
        if (!constructor.getTypeParamsMap().isEmpty()) {
             for (String typeName : constructor.getTypeParamsMap().keySet()) {
                 JTypeVar type = result.generify(typeName);
                 for (APIType bound : constructor.getTypeParamsMap().get(typeName)) {
                     JClass typeBound = getTypeRef(bound, constructor.getTypeParamsMap().keySet());
                     if (!bound.equals(new APIType("java.lang.Object"))) {
                         type.bound(typeBound);
                     }
                 }
             }
         }
 
         // add generics of the instantiator to the method
         addGenerics(result);
 
         // add params to the method
         for (APIMethodParameter arg : constructor.getParameters()) {
             result.param(getTypeRef(arg.getType(), constructor.getTypeParamsMap().keySet()), String.valueOf(arg.getName()));
             if (nullParams) {
                 newInstance.arg(getPrimitiveValue(arg.getType().getName()));
             } else {
                 newInstance.arg(JExpr.ref(arg.getName()));
             }
         }
 
         // create body of the method
         JBlock resultBody = result.body();
 
         // add try-catch block if constructor throws some exceptions
         if (!constructor.getThrown().isEmpty()) {
             JTryBlock tryBlock = result.body()._try();
             resultBody = tryBlock.body();
             char eName = 'E';
             for (String exceptionType : constructor.getThrown()) {
                 JClass exception = getTypeRef(exceptionType, constructor.getTypeParamsMap().keySet());
                 String exceptionParam = "ex" + String.valueOf(eName++);
                 tryBlock._catch(exception).param(exceptionParam);
             }
             // add return null after catch block.
             result.body()._return(getPrimitiveValue(constructor.getReturnType().getName()));
         }
 
         // add return statement to the method body.
         if (innerClass) {
             //StringBuilder returnStatement = new StringBuilder();
             StringWriter returnStatement = new StringWriter();
             returnStatement.append("return ");
             returnStatement.append(configuration.getInstanceIdentifier());
             returnStatement.append(".");
 
             newInstance.generate(new JFormatter(new PrintWriter(returnStatement)));
             returnStatement.append(';');
             resultBody.directStatement(returnStatement.toString());
         } else {
             resultBody._return(newInstance);
         }
 
 
     }
 
 
     private void addMethodCaller(APIMethod method, boolean generateNullCall) {
 
         int methodMods = JMod.PUBLIC;
         JTypeVar t = null;
 
         JType returnType = getTypeRef(method.getReturnType(), method.getTypeParamsMap().keySet());
 //        if (visitingClass.getTypeParamsMap().containsKey(method.getReturnType())) {
 //            returnType = getGenericsClassRef(visitingClass.getTypeParamsMap().get(method.getReturnType())[0]);
 //        } else if (method.getTypeParamsMap().containsKey(method.getReturnType())) {
 //            String returnTypeName = method.getTypeParamsMap().get(method.getReturnType())[0];
 //            returnType = getGenericsClassRef(returnTypeName);
 //        }
 
         String callerName = generateName(configuration.getMethodCallIdentifier(), method.getName());
         String nullCallerName = generateName(configuration.getMethodNullCallIdentifier(), method.getName());
 
         JMethod caller = cls.method(methodMods, returnType, callerName);
         JMethod nullCaller = cls.method(methodMods, returnType, nullCallerName);
 
         // add generics
        if (!method.getTypeParamsMap().isEmpty()) {
             for (String typeName : method.getTypeParamsMap().keySet()) {
                 JTypeVar type = caller.generify(typeName);
                 JTypeVar ntype = nullCaller.generify(typeName);
                 for (APIType bound : method.getTypeParamsMap().get(typeName)) {
                     JClass typeBound = getTypeRef(bound, method.getTypeParamsMap().keySet());
                     if (!bound.getName().equals("java.lang.Object")) {
                         type.bound(typeBound);
                         ntype.bound(typeBound);
                     }
                 }
             }
         }
 
         // add generics of the instantiator to the method
         addGenerics(caller);
         addGenerics(nullCaller);
 
         // define method invocation
         JInvocation invocation;
         JInvocation nullInvocation;
 
         // set invocation
         if (method.getModifiers().contains(APIModifier.STATIC)) {  // Static method - instance = Class name
             invocation = getTypeRef(visitingClass.getType()).staticInvoke(method.getName());
             nullInvocation = getTypeRef(visitingClass.getType()).staticInvoke(method.getName());
         } else { // instance is first parameter
 //            String instanceClassName = visitingClass.getFullName();
 //            if (!visitingClass.getTypeParamsMap().isEmpty()) {
 //                instanceClassName += "<";
 //                boolean first = true;
 //                for (String typeName : visitingClass.getTypeParamsMap().keySet()) {
 //                    if (first) {
 //                        instanceClassName += typeName;
 //                        first = false;
 //                    } else {
 //                        instanceClassName += ", " + typeName;
 //                    }
 //                }
 //                instanceClassName += "> ";
 //            }
 //            JClass instanceClassRef = getGenericsClassRef(instanceClassName);
             JClass instanceClassRef = getTypeRef(visitingClass.getType());
             JExpression instance = caller.param(instanceClassRef, configuration.getInstanceIdentifier());
             JExpression nullInstance = nullCaller.param(instanceClassRef, configuration.getInstanceIdentifier());
             invocation = instance.invoke(method.getName());
             nullInvocation = nullInstance.invoke(method.getName());
         }
 
         // add parameter to the method and invocation - new method has same parameters as original method
         for (APIMethodParameter parameter : method.getParameters()) {
             String name = parameter.getName();
             JClass type = getTypeRef(parameter.getType(), method.getTypeParamsMap().keySet());
             JVar arg = caller.param(type, name);
             nullCaller.param(type, name);
             invocation.arg(arg);
             nullInvocation.arg(getPrimitiveValue(parameter.getType().getName()));
         }
 
         JBlock callerBody = caller.body();
         JBlock nullCallerBody = nullCaller.body();
 
         // add try-catch block if method trows any exception
         if (!method.getThrown().isEmpty()) {
             JTryBlock tryBlock = caller.body()._try();
             JTryBlock nullTryBlock = nullCaller.body()._try();
             callerBody = tryBlock.body();
             nullCallerBody = nullTryBlock.body();
 
             for (String exceptionType : method.getThrown()) {
                 JClass exception = getTypeRef(exceptionType, method.getTypeParamsMap().keySet());
                 String exceptionParam;
                 // get exception name and check if exception name is not in conflict with name of any other parameter
                 // of the method.
                 boolean nameOk;
                 int exceptionExtension = 0;
                 do {
                     exceptionParam = configuration.getExceptionVariableName() + Integer.toString(exceptionExtension);
                     nameOk = true;
                     for (APIMethodParameter parameter : method.getParameters()) {
                         if((exceptionExtension == 0 && parameter.getName().equals(configuration.getExceptionVariableName())) 
                             || (exceptionExtension != 0 && parameter.getName().equals(exceptionParam))) {
                             exceptionExtension++;
                             nameOk = false;
                         }
                     }
                 } while (!nameOk);
 
                 if(exceptionExtension == 0) { // there is no conflict, no extension is used.
                     exceptionParam = configuration.getExceptionVariableName();
                 }
                 tryBlock._catch(exception).param(exceptionParam);
                 nullTryBlock._catch(exception).param(exceptionParam);
             }
 
             if (!method.getReturnType().equals(APIType.voidType)) {
                 caller.body()._return(getPrimitiveValue(method.getReturnType().getName()));
                 nullCaller.body()._return(getPrimitiveValue(method.getReturnType().getName()));
             }
         }
 
         if (method.getReturnType().equals(APIType.voidType)) {
             callerBody.add(invocation);
             nullCallerBody.add(nullInvocation);
         } else {
             callerBody._return(invocation);
             nullCallerBody._return(nullInvocation);
         }
 
 
         if (!generateNullCall) {
             cls.methods().remove(nullCaller);
         }
     }
 }
