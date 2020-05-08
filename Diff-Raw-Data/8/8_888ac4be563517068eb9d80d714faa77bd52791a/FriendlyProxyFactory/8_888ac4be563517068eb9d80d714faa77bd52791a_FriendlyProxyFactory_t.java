 /*
  * Written by Peter Levart <peter.levart@gmail.com>
  * and released to the public domain, as explained at
  * http://creativecommons.org/publicdomain/zero/1.0/
  */
 package si.pele.friendly;
 
 import jdk.internal.org.objectweb.asm.ClassWriter;
 import jdk.internal.org.objectweb.asm.FieldVisitor;
 import jdk.internal.org.objectweb.asm.Opcodes;
 import jdk.internal.org.objectweb.asm.Type;
 import jdk.internal.org.objectweb.asm.commons.GeneratorAdapter;
 import sun.security.action.GetPropertyAction;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.invoke.MethodHandle;
 import java.lang.invoke.MethodType;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Proxy;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * A factory for friendly proxy classes. Each proxy class is generated to implement the given interface and
  * is defined by the interface's class loader. This happens in the constructor of the proxy factory.
  * After constructor is successfully finished, factory object serves as a holder for the
  * {@code Class} object (accessed by {@link #getProxyClass()}) representing generated proxy class and an array
  * of target methods (accessed by {@link #getTargetMethods()}) that the proxy forwards requests to.
  * Both are retrieved by {@link Friendly#proxy(Class)} method which also obtains the
  * singleton proxy instance by reading the private static final field of the proxy class and governs
  * access to this instance by it's internal policy taking into account all target methods.
  */
 final class FriendlyProxyFactory<I> {
 
     private final Class<? extends I> proxyClass;
     private final Method[] targetMethods;
 
     FriendlyProxyFactory(Class<I> intf) throws IllegalArgumentException {
 
         if (!intf.isInterface())
             throw new IllegalArgumentException(intf + " is not an interface.");
 
         // take just abstract instance methods (ignore default/static JDK8 methods)
         Method[] methods = intf.getMethods();
         int abstrInstCount = 0;
         for (Method method : methods) {
             int mod = method.getModifiers();
             if (Modifier.isAbstract(mod) && !Modifier.isStatic(mod))
                 abstrInstCount++;
         }
         if (abstrInstCount != methods.length) {
             Method[] filteredMethods = new Method[abstrInstCount];
             int i = 0;
             for (Method method : methods) {
                 int mod = method.getModifiers();
                 if (Modifier.isAbstract(mod) && !Modifier.isStatic(mod))
                     filteredMethods[i++] = method;
             }
             methods = filteredMethods;
         }
 
         // collect methods' exception types into an array of arrays
         @SuppressWarnings("unchecked")
         Class<?>[][] methodsExceptionTypes = new Class[methods.length][];
 
         // deduce target methods from interface methods
         targetMethods = new Method[methods.length];
         for (int i = 0; i < methods.length; i++) {
             Method method = methods[i];
             String name = method.getName();
             Class<?>[] paramTypes = method.getParameterTypes();
             if (paramTypes.length == 0)
                 throw new IllegalArgumentException(
                     "Invalid proxy method: " + method + " (missing target parameter)"
                 );
             Class<?> targetClass = paramTypes[0];
             Class<?>[] targetParamTypes = new Class<?>[paramTypes.length - 1];
             System.arraycopy(paramTypes, 1, targetParamTypes, 0, targetParamTypes.length);
             Method targetMethod;
             try {
                 targetMethod = targetClass.getDeclaredMethod(name, targetParamTypes);
             }
             catch (NoSuchMethodException e) {
                 throw new IllegalArgumentException(
                     "Can't find target method for proxy method: " + method
                 );
             }
             if (method.getReturnType() != targetMethod.getReturnType()) {
                 throw new IllegalArgumentException(
                     "Return types of target method: " + targetMethod +
                     " and proxy method: " + method + " don't match"
                 );
             }
             Class<?>[] exceptionTypes = method.getExceptionTypes();
             methodsExceptionTypes[i] = exceptionTypes;
             // validate assign-ability of declared checked exception types
             next_target_exc_type:
             for (Class<?> targetExceptionType : targetMethod.getExceptionTypes()) {
                 // skip unchecked exception types
                 if (RuntimeException.class.isAssignableFrom(targetExceptionType) ||
                     Error.class.isAssignableFrom(targetExceptionType))
                     continue next_target_exc_type;
                 // checked target method exception type should be assign-able to at least one
                 // of proxy method's exception types...
                 for (Class<?> exceptionType : exceptionTypes) {
                     if (exceptionType.isAssignableFrom(targetExceptionType))
                         continue next_target_exc_type;
                 }
                 throw new IllegalArgumentException(
                     "Target method: " + targetMethod + " declares checked exceptions" +
                     " that are not declared by proxy method: " + method
                 );
             }
             // Ok, validated
             targetMethods[i] = targetMethod;
         }
 
         ClassFile classFile = spinProxyClass(intf, methods, methodsExceptionTypes, targetMethods);
 
         if (saveGeneratedFilesDir != null) {
             File dir = new File(saveGeneratedFilesDir);
             File file = new File(dir, classFile.className + ".class");
             try {
                 File parentDir = file.getParentFile();
                 if (!parentDir.isDirectory()) parentDir.mkdirs();
                 try (FileOutputStream fos = new FileOutputStream(file)) {
                     fos.write(classFile.classBytes);
                 }
             }
             catch (IOException e) {
                 throw new Error("I/O exception saving generated file: " + file, e);
             }
         }
 
         @SuppressWarnings("unchecked")
         Class<? extends I> proxyClass = (Class<? extends I>) defineClass0(
             intf.getClassLoader(),
             classFile.className.replace('/', '.'),
             classFile.classBytes,
             0,
             classFile.classBytes.length
         );
 
         this.proxyClass = proxyClass;
     }
 
     Class<? extends I> getProxyClass() {
         return proxyClass;
     }
 
     Method[] getTargetMethods() {
         return targetMethods;
     }
 
     // proxy class spinning
 
     static final String PROXY_INSTANCE_FIELD_NAME = "INSTANCE";
 
     private static final String saveGeneratedFilesDir =
         java.security.AccessController.doPrivileged(
             new GetPropertyAction("si.pele.friendly.FriendlyProxyFactory.saveGeneratedFilesDir")
         );
 
     private static final String proxyClassNamePrefix = "$FriendlyProxy";
     private static final String mhFieldNamePrefix = "mh";
     private static final AtomicLong nextUniqueNumber = new AtomicLong();
     private static final int classFileVersion = 51;
     private static final Type MethodHandle_Type = Type.getType(MethodHandle.class);
     private static final Type Friendly_Type = Type.getType(Friendly.class);
     private static final jdk.internal.org.objectweb.asm.commons.Method noArgConstructor =
         jdk.internal.org.objectweb.asm.commons.Method.getMethod("void <init> ()");
     private static final jdk.internal.org.objectweb.asm.commons.Method staticInitializer =
         jdk.internal.org.objectweb.asm.commons.Method.getMethod("void <clinit> ()");
    private static final jdk.internal.org.objectweb.asm.commons.Method Friendly_findVirtual;
 
     static {
         try {
            Friendly_findVirtual =
                 jdk.internal.org.objectweb.asm.commons.Method.getMethod(
                     Friendly.class.getDeclaredMethod("findVirtual", Class.class, String.class, String.class)
                 );
         }
         catch (NoSuchMethodException e) {
             throw new Error(e);
         }
     }
 
     static final class ClassFile {
         final String className;
         final byte[] classBytes;
 
         ClassFile(String className, byte[] classBytes) {
             this.className = className;
             this.classBytes = classBytes;
         }
     }
 
     private static ClassFile spinProxyClass(
         Class<?> intf,
         Method[] methods,
         Class<?>[][] methodsExceptionTypes,
         Method[] targetMethods
     ) {
 
         String intfName = intf.getName().replace('.', '/');
         int lastSlash = intfName.lastIndexOf('/');
         String pkgPath = lastSlash >= 0 ? intfName.substring(0, lastSlash + 1) : "";
         if (Modifier.isPublic(intf.getModifiers())) pkgPath = "";
         String proxyClassName = pkgPath + proxyClassNamePrefix + nextUniqueNumber.getAndIncrement();
         Type proxyClass_Type = Type.getObjectType(proxyClassName);
 
         ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
 
         // generate proxy class
         {
             cw.visit(classFileVersion, Opcodes.ACC_SUPER | Opcodes.ACC_FINAL, proxyClassName, null, "java/lang/Object", new String[]{intfName});
 
             // generate private static final fields with names: mh0, mh1, ... and type java.lang.invoke.MethodHandle
             for (int i = 0; i < targetMethods.length; i++) {
                 FieldVisitor fv = cw.visitField(
                     Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                     mhFieldNamePrefix + i,
                     MethodHandle_Type.getDescriptor(),
                     null,
                     null
                 );
                 fv.visitEnd();
             }
 
             // generate private static final field INSTANCE to hold the singleton instance
             {
                 FieldVisitor fv = cw.visitField(
                     Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                     PROXY_INSTANCE_FIELD_NAME,
                     proxyClass_Type.getDescriptor(),
                     null,
                     null
                 );
                 fv.visitEnd();
             }
 
             // generate static initializer
             {
                 GeneratorAdapter clinit = new GeneratorAdapter(
                     Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                     staticInitializer,
                     null,
                     null,
                     cw
                 );
                 // initialize static mh0, mh1, ... fields
                 for (int i = 0; i < targetMethods.length; i++) {
                     Method targetMethod = targetMethods[i];
                     // push target method's declaring class
                     clinit.push(Type.getType(targetMethod.getDeclaringClass()));
                     // push method name
                     clinit.push(targetMethod.getName());
                     // push method type descriptor
                     clinit.push(
                         MethodType.methodType(
                             targetMethod.getReturnType(),
                             targetMethod.getParameterTypes()
                         ).toMethodDescriptorString()
                     );
                     // invoke the Friendly.findVirtual static method
                    clinit.invokeStatic(Friendly_Type, Friendly_findVirtual);
                     // store the result into mh0, mh1, ... field
                     clinit.putStatic(proxyClass_Type, mhFieldNamePrefix + i, MethodHandle_Type);
                 }
                 // initialize static INSTANCE field
                 {
                     // create new proxy instance
                     clinit.newInstance(proxyClass_Type);
                     // duplicate reference to newly created instance
                     clinit.dup();
                     // invoke no-arg constructor
                     clinit.invokeConstructor(proxyClass_Type, noArgConstructor);
                     // assign the instance to "INSTANCE" static field
                     clinit.putStatic(proxyClass_Type, PROXY_INSTANCE_FIELD_NAME, proxyClass_Type);
                 }
                 // return
                 clinit.returnValue();
                 // end of static initializer
                 clinit.endMethod();
             }
 
             // generate private no-arg constructor
             {
                 GeneratorAdapter init = new GeneratorAdapter(Opcodes.ACC_PRIVATE, noArgConstructor, null, null, cw);
                 // invoke super (Object) constructor
                 init.loadThis();
                 init.invokeConstructor(Type.getType(Object.class), noArgConstructor);
                 // return
                 init.returnValue();
                 // end of constructor
                 init.endMethod();
             }
 
             // generate proxy methods
             for (int i = 0; i < methods.length; i++) {
                 Method method = methods[i];
                 jdk.internal.org.objectweb.asm.commons.Method m =
                     jdk.internal.org.objectweb.asm.commons.Method.getMethod(method);
                 GeneratorAdapter gen = new GeneratorAdapter(Opcodes.ACC_PUBLIC, m, null, getExceptionTypes(method), cw);
                 // push the value of mh0, mh1, ... field on the stack
                 gen.getStatic(
                     proxyClass_Type,
                     mhFieldNamePrefix + i,
                     MethodHandle_Type
                 );
                 // push the method parameters on the stack
                 gen.loadArgs();
                 // invoke the MethodHandle.invokeExact method with correct signature for invoking target method
                 Method targetMethod = targetMethods[i];
                 jdk.internal.org.objectweb.asm.commons.Method invokerM =
                     new jdk.internal.org.objectweb.asm.commons.Method(
                         "invokeExact",
                         MethodType.methodType(
                             targetMethod.getReturnType(),
                             targetMethod.getParameterTypes()
                         ).insertParameterTypes(0, targetMethod.getDeclaringClass())
                             .toMethodDescriptorString()
                     );
                 gen.invokeVirtual(
                     MethodHandle_Type,
                     invokerM
                 );
                 // return the result
                 gen.returnValue();
                 // end of method
                 gen.endMethod();
             }
 
             cw.visitEnd();
         }
 
         return new ClassFile(proxyClassName, cw.toByteArray());
     }
 
     private static Type[] getExceptionTypes(Method method) {
         Class<?>[] exceptionClasses = method.getExceptionTypes();
         Type[] exceptionTypes = new Type[exceptionClasses.length];
         for (int i = 0; i < exceptionClasses.length; i++) {
             exceptionTypes[i] = Type.getType(exceptionClasses[i]);
         }
         return exceptionTypes;
     }
 
     // access to private native method in j.l.r.Proxy
 
     private static final MethodHandle defineClass0MH = Friendly.method(
         Proxy.class, "defineClass0", ClassLoader.class, String.class, byte[].class, int.class, int.class
     );
 
     private static Class<?> defineClass0(
         ClassLoader loader, String name,
         byte[] b, int off, int len
     ) {
         try {
             return (Class<?>) defineClass0MH.invokeExact(loader, name, b, off, len);
         }
         catch (Throwable t) {
             throw MHThrows.unchecked(t);
         }
     }
 }
