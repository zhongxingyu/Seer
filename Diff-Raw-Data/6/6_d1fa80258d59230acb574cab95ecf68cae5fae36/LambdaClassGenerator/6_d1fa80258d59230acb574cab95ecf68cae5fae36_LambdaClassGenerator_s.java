 package org.wixpress.hoopoe.lambda;
 
 import javassist.*;
 
 import java.lang.reflect.Constructor;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
 * @author Yoav
 * @since 10/6/11
 */
 class LambdaClassGenerator<F> {
     private static AtomicInteger lambdaCounter = new AtomicInteger();
     String invokeCode;
     String invokeInternalCode;
     String retTypeCode;
     String varTypesCode;
     String[] fieldsCode;
     String lambdaName;
     String constructorCode;
     Class<?> functionInterface;
     Class<F> lambdaClass;
     Constructor<F> lambdaConstructor;
     private LambdaSignature<F> lambdaSignature;
 
     public LambdaClassGenerator(LambdaSignature<F> lambdaSignature) {
         this.lambdaSignature = lambdaSignature;
     }
 
     public void generateClass(String code, Val[] vals, LambdaClassKey key) {
         generateClassCode(code, vals, key);
         try {
             ClassPool pool = ClassPool.getDefault();
             pool.insertClassPath(new ClassClassPath(this.getClass()));
             CtClass ctClass = pool.makeClass(lambdaName);
             ctClass.addInterface(toCtClass(functionInterface));
 
             for (String fieldCode: fieldsCode)
                 ctClass.addField(CtField.make(fieldCode, ctClass));
 
             ctClass.addMethod(CtNewMethod.make(
                     invokeInternalCode,
                     ctClass));
 
             ctClass.addMethod(CtNewMethod.make(
                     invokeCode,
                     ctClass));
 
             ctClass.addMethod(CtNewMethod.make(
                     retTypeCode,
                     ctClass));
 
             ctClass.addMethod(CtNewMethod.make(
                     varTypesCode,
                     ctClass));
 
             if (constructorCode != null)
                 ctClass.addConstructor(CtNewConstructor.make(constructorCode, ctClass));
 
             //noinspection unchecked
             lambdaClass = (Class<F>)ctClass.toClass();
             lambdaConstructor = lambdaClass.getConstructor(getValTypes(vals));
             lambdaConstructor.setAccessible(true);
         } catch (CannotCompileException e) {
             throw new LambdaException("failed creating Lambda - \n" + toString(), e);
         } catch (NoSuchMethodException e) {
             throw new LambdaException("failed creating Lambda - \n" + toString(), e);
         }
     }
 
     private Class<?>[] getValTypes(Val[] vals) {
         Class<?>[] valTypes = new Class<?>[vals.length];
         for (int i=0; i < vals.length; i++)
             valTypes[i] = vals[i].boxedType();
         return valTypes;
     }
 
     private CtClass toCtClass(final Class inputClass) {
         try {
             ClassPool pool = ClassPool.getDefault();
             return pool.get(toClassName(inputClass));
         } catch (NotFoundException ex) {
             throw new LambdaException("Failed getting CtClass for [%s] as it is not found", inputClass, ex);
         }
     }
 
     private String toClassName(final Class inputClass) {
         if (inputClass.isArray())
             return toClassName(inputClass.getComponentType()) + "[]";
 
         return inputClass.getName();
     }
 
     private void generateClassCode(String code, Val[] vals, LambdaClassKey key) {
         this.invokeCode = writeInvokeCode();
         this.invokeInternalCode = writeInvokeInternalCode(code);
         this.retTypeCode = writeRetTypeCode();
         this.varTypesCode = writeVarTypesCode();
         this.fieldsCode = writeVariablesCode(vals);
         this.lambdaName = generateLambdaName(key);
         this.constructorCode = writeConstructorCode(lambdaName, vals);
         this.functionInterface = getFunctionInterface();
     }
 
     private String writeConstructorCode(String lambdaName, Val[] vals) {
         if (vals.length >0 ) {
             StringBuilder sb = new StringBuilder();
             sb.append("\tpublic ").append(lambdaName).append("(");
             for (int i=0; i < vals.length; i++) {
                 Val val = vals[i];
                 sb.append(val.boxedType().getName()).append(" ").append(val.getName()).append((i==vals.length-1)?") {\n":", ");
             }
             for (Val val : vals) {
                 sb.append("\t\tthis.").append(val.getName()).append(" = ").append(val.unboxCode(val.getName())).append(";\n");
             }
             sb.append("\t}");
             return sb.toString();
         }
         else
             return null;
     }
 
     private String[] writeVariablesCode(Val[] vals) {
         String[] valsCode = new String[vals.length];
         for (int i=0; i < vals.length; i++)
             valsCode[i] = String.format("%s %s;", vals[i].primitiveType(), vals[i].getName());
         return valsCode;
     }
 
     private String writeVarTypesCode() {
         StringBuilder methodCode = new StringBuilder();
         methodCode.append("\tpublic Class[] varTypes() {\n")
                 .append("\t\treturn new Class[] {");
         for (int i=0; i < lambdaSignature.vars.length; i++) {
             methodCode.append(lambdaSignature.vars[i].boxedType().getName()).append(".class").append((i < lambdaSignature.vars.length - 1) ? ", " : "");
         }
         methodCode.append("};\n")
                 .append("\t}");
         return methodCode.toString();
     }
 
     private String writeRetTypeCode() {
         return new StringBuilder()
                 .append("\tpublic Class retType() {\n")
                 .append("\t\treturn ").append(lambdaSignature.retType.boxedType().getName()).append(".class;\n")
                 .append("\t}")
                 .toString();
     }
 
     private String writeInvokeCode() {
         StringBuilder methodCode = new StringBuilder();
         methodCode.append("\tpublic Object apply(");
         for (int i = 0, varsLength = lambdaSignature.vars.length; i < varsLength; i++) {
             Var<?> var = lambdaSignature.vars[i];
             methodCode.append("Object ").append(var.getName()).append((i==varsLength-1)?") {\n":", ");
         }
         methodCode.append("\t\treturn ").append(lambdaSignature.retType.boxCode(formatCallToInvokeInternal())).append(";\n\t}");
 
         return methodCode.toString();
     }
 
     private String formatCallToInvokeInternal() {
         StringBuilder invokeInternalParameters = new StringBuilder();
         for (int i = 0, varsLength = lambdaSignature.vars.length; i < varsLength; i++) {
             Var<?> var = lambdaSignature.vars[i];
             invokeInternalParameters.append(var.unboxCode(var.getName())).append((i==varsLength-1)?"":", ");
         }
         return String.format("invokeInternal(%s)", invokeInternalParameters.toString());
     }
 
     private String writeInvokeInternalCode(String code) {
         StringBuilder sb = new StringBuilder();
         sb.append("\t").append(lambdaSignature.retType.primitiveType().getName()).append(" invokeInternal(");
         for (int i = 0, varsLength = lambdaSignature.vars.length; i < varsLength; i++) {
             Var<?> var = lambdaSignature.vars[i];
             sb.append(var.primitiveType().getName()).append(" ").append(var.getName()).append((i==varsLength-1)?") {\n":", ");
         }
         sb.append("\t\treturn ").append(code).append(";\n\t}");
         return sb.toString();
     }
 
     private String generateLambdaName(LambdaClassKey key) {
         return "Lambda$$" + key.uniqueName();
     }
 
     private Class getFunctionInterface() {
         switch (lambdaSignature.vars.length) {
             case 1: return Function1.class;
             case 2: return Function2.class;
             case 3: return Function3.class;
             default: throw new LambdaException("unsupported number of lambda parameters [%d]", lambdaSignature.vars.length);
         }
     }
 
     public String toString() {
         StringBuilder sb = new StringBuilder().append("class ").append(lambdaName).append(" implements ").append(functionInterface.getName()).append(" {\n");
         for (String fieldCode: fieldsCode)
             sb.append("\t").append(fieldCode).append("\n");
        sb.append(constructorCode).append("\n");
         sb.append(invokeCode).append("\n");
         sb.append(invokeInternalCode).append("\n");
         sb.append(retTypeCode).append("\n");
         sb.append(varTypesCode).append("\n");
         sb.append("}");
         return sb.toString();
     }
 
 
 }
