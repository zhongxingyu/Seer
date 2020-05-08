 package org.bpunit.assertions;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Logger;
 
 /**
  * A utility class for boilerplate assertions
  */
 public class AssertUtils {
     private static final Logger log = Logger.getLogger(AssertUtils.class.getName());
 
     private static final String SET_PREFIX = "set";
     private static final String GET_PREFIX = "get";
     private static final String BOOLEAN_GET_PREFIX = "is";
     private static final String RANDOM_PREFIX = "next";
 
     /** Hack to map between primitive types and boxing classes */
     private static final Map<Class<?>, Class<?>> primitiveToBoxing = new HashMap<>();
     static {
         primitiveToBoxing.put(Boolean.TYPE, Boolean.class);
         primitiveToBoxing.put(Byte.TYPE, Byte.class);
         primitiveToBoxing.put(Short.TYPE, Short.class);
         primitiveToBoxing.put(Integer.TYPE, Integer.class);
         primitiveToBoxing.put(Long.TYPE, Long.class);
         primitiveToBoxing.put(Float.TYPE, Float.class);
         primitiveToBoxing.put(Double.TYPE, Double.class);
     }
 
     /** Should not be initialized. */
     private AssertUtils() {
     }
 
     public static void testProperties(Object o, Random random) {
         Class<?> objectClass = o.getClass();
         for (Method setMethod : objectClass.getMethods()) {
             String setMethodName = setMethod.getName();
 
             // Skip non setters
             if (!setMethodName.startsWith(SET_PREFIX)) {
                 continue;
             }
 
             Class<?>[] paramTypes = setMethod.getParameterTypes();
             if (paramTypes.length != 1) {
                 continue;
             }
 
             String propertyName = setMethodName.substring(SET_PREFIX.length());
             Class<?> type = paramTypes[0];
             Method getMethod = getGetMethod(objectClass, propertyName, type);
             if (getMethod == null) {
                 log.info("Cannot find getter and setter pair for property " + propertyName);
                 continue;
             }
             
             Object randomValue = getRandomValue(random, type);
             if (randomValue == null) {
                 continue;
             }
 
 
             try {
                 setMethod.invoke(o, randomValue);
                 Object returnedValue = getMethod.invoke(o);
                 assertEquals("Wrong value for property " + propertyName, randomValue, returnedValue);
             } catch (IllegalAccessException | InvocationTargetException e) {
                 fail("Can't test property " + propertyName + " due to exception: " + e);                
             }
         }
     }
 
     private static Method getGetMethod(Class<?> objectClass, String propertyName, Class<?> expectedType) {
         Method m = getGetMethod(objectClass, propertyName, GET_PREFIX, expectedType);
         if (m == null && (expectedType.equals(Boolean.TYPE) || expectedType.equals(Boolean.class))) {
             log.info("Property " + propertyName + " is a boolean, trying a different prefix");
             m = getGetMethod(objectClass, propertyName, BOOLEAN_GET_PREFIX, expectedType);
         }
         return m;
     }
 
     private static Method getGetMethod(Class<?> objectClass,
             String propertyName,
             String methodPrefix,
             Class<?> expectedType) {
         String getMethodName = methodPrefix + propertyName;
         Method getMethod;
         try {
             getMethod = objectClass.getMethod(getMethodName);
             if (!getMethod.isAccessible() && !Modifier.isPublic(getMethod.getModifiers())) {
                 throw new NoSuchMethodException();
             }
             Class<?> retrunType = getMethod.getReturnType();
             if (!retrunType.equals(expectedType)) {
                 throw new NoSuchMethodException();
             }
         } catch (NoSuchMethodException e) {
             log.info("No appropriate getter " + getMethodName + " for " + propertyName);
             return null;
         }
         return getMethod;
     }
 
     private static <T> T getRandomValue(Random random, Class<T> type) {
        String typeName = type.getSimpleName();
        if (type.isPrimitive()) {
            typeName = capitalizeFirst(typeName);
        }
        String randomMethodName = RANDOM_PREFIX + typeName;
         Class<? extends Random> randomClass = random.getClass();
         try {
             Method randomMethod = randomClass.getMethod(randomMethodName);
             Class<?> returnType = randomMethod.getReturnType();
             if (randomMethod.getParameterTypes().length != 0 ||
                     (!returnType.equals(type) && !type.equals(primitiveToBoxing.get(returnType)))) {
                 throw new NoSuchMethodException();
             }
 
             //noinspection unchecked
             return (T) randomMethod.invoke(random);
         } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
             log.info("Can't execute random method: " + randomClass.getSimpleName() + "." + randomMethodName);
             return null;
         }
     }

    private static String capitalizeFirst(String s) {
        return String.valueOf(Character.toTitleCase(s.charAt(0))) + s.substring(1);
    }
 }
