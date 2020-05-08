 package net.xaethos.lib.activeprovider.models;
 
 import net.xaethos.lib.activeprovider.annotations.Getter;
 import net.xaethos.lib.activeprovider.annotations.ModelInfo;
 import net.xaethos.lib.activeprovider.annotations.Setter;
 
 import java.lang.reflect.*;
 
 public class ModelHandler implements InvocationHandler {
 
    public static <T> T newModelInstance(Class<T> modelType, ModelHandler handler) {
         if (!modelType.isAnnotationPresent(ModelInfo.class)) {
             throw new IllegalArgumentException(
                     "model interface must have the annotation @" + ModelInfo.class.getSimpleName());
         }
 
         return modelType.cast(Proxy.newProxyInstance(modelType.getClassLoader(),
                 new Class[]{modelType},
                 handler));
     }
 
     protected static String getGetterName(Class<?> cls) {
         return "get" + (cls.isArray() ?
                 cls.getComponentType().getSimpleName() + "Array" :
                 cls.getSimpleName());
     }
 
     protected final boolean isReadable() {
         return ReadableModelHandler.class.isInstance(this);
     }
 
     protected final boolean isWritable() {
         return WritableModelHandler.class.isInstance(this);
     }
 
     @Override
     public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 
         if (isReadable() && method.isAnnotationPresent(Getter.class)) {
             Class<?>[] params = method.getParameterTypes();
             if (params.length != 0) {
                 throw new IllegalArgumentException("@Getter methods must take no parameters");
             }
             return invokeGetter(method.getAnnotation(Getter.class).value(), method.getReturnType());
         }
         if (isWritable() && method.isAnnotationPresent(Setter.class)) {
             Class<?>[] params = method.getParameterTypes();
             if (params.length != 1) {
                 throw new IllegalArgumentException("@Setter methods must take a single parameter");
             }
             invokeSetter(method.getAnnotation(Setter.class).value(), args[0], params[0]);
             return null;
         }
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
 
         throw new UnsupportedOperationException("Unhandled method: " + method.getName());
     }
 
     public Object invokeGetter(String field, Class<?> valueType) throws Throwable {
         try{
             return this.getClass().getMethod(getGetterName(valueType), String.class).invoke(this, field);
         }
         catch (NoSuchMethodException e) {
             throw new UnsupportedOperationException("Unhandled field type: " + valueType.getName());
         }
         catch (InvocationTargetException e) {
             throw e.getCause();
         }
     }
 
     public void invokeSetter(String field, Object value, Class<?> valueType) throws Throwable {
         if (value != null) {
             try{
                 this.getClass().getMethod("set", String.class, valueType).invoke(this, field, value);
             }
             catch (NoSuchMethodException e) {
                 throw new UnsupportedOperationException("Unhandled field type: " + valueType.getName());
             }
             catch (InvocationTargetException e) {
                 throw e.getCause();
             }
         }
         else {
             ((WritableModelHandler)this).setNull(field);
         }
     }
 
 }
