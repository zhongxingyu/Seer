 package pl.playbit.di;
 
 import pl.playbit.di.annotations.Init;
 import pl.playbit.di.annotations.Inject;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 
 public class Context {
 
     //TODO inject everything in the inheritance (currently super classes @Inject and @Init are omitted).
 
     //TODO improve this method to inject superclasses' fields and invoke superclasses' inits.
     @SuppressWarnings("unchecked")
     public static <T> T create(Class<T> clazz) throws IllegalAccessException, InstantiationException, NoSuchFieldException, InvocationTargetException {
         T instance = clazz.newInstance();
        injectFields(instance);
        initMethods(instance);
        return instance;
    }

    private static <T> void injectFields(T instance) throws InvocationTargetException, NoSuchFieldException, IllegalAccessException, InstantiationException {
        LinkedList<Class<?>> hierarchy = getClassHierarchy(instance);
        while (hierarchy.size() > 0) {
            Class<?> clazz = hierarchy.pollLast();
            injectClassFields(instance, clazz);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void injectClassFields(T instance, Class<?> clazz) throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
         Collection<Field> injectedList = getInjectFields(clazz);
         for (Field fieldToBeInjected : injectedList) {
             Class classToBeInjected;// = null;
             Inject annotation = fieldToBeInjected.getAnnotation(Inject.class);
             if (annotation.value() != Inject.DEFAULT.class) {
                 //Get type of class to be injected (field (with @Inject) type)
                 classToBeInjected = annotation.value();
             } else {
                 //Get type of class to be injected (type passed to @Inject)
                 classToBeInjected = fieldToBeInjected.getType();
             }
             //Create underlying object
             Object injectedObject = create(classToBeInjected);
             //Check is field is accessible and raise privileges if not.
             boolean isAccessible = fieldToBeInjected.isAccessible();
             if (!isAccessible) { //Raise privileges
                 fieldToBeInjected.setAccessible(true);
             }
             //Set field for the given instance with given value.
             fieldToBeInjected.set(instance, injectedObject);
             if (!isAccessible) { //Restore privileges
                 fieldToBeInjected.setAccessible(false);
             }
         }
     }
 
    private static <T> LinkedList<Class<?>> getClassHierarchy(T instance) {
        LinkedList<Class<?>> hierarchy = new LinkedList<>();
        Class<?> clazz = instance.getClass();
        while (clazz != null && clazz != Object.class) {
             hierarchy.add(clazz);
             clazz = clazz.getSuperclass();
         }
         return hierarchy;
     }
 
     private static <T> void initMethods(T instance) throws InvocationTargetException, IllegalAccessException {
         Collection<Method> methods = getInitMethods(instance.getClass());
         for (Method method : methods) {
             boolean isAccessible = method.isAccessible();
             if (!isAccessible) {
                 method.setAccessible(true);
             }
             method.invoke(instance);
             if (!isAccessible) {
                 method.setAccessible(false);
             }
         }
     }
 
     private static <T> Collection<Field> getInjectFields(Class<T> clazz) {
         Field[] fields = clazz.getDeclaredFields();
         Collection<Field> result = new ArrayList<>();
         for (Field field : fields) {
             if (field.isAnnotationPresent(Inject.class)) {
                 result.add(field);
             }
         }
         return result;
     }
 
     private static <T> Collection<Method> getInitMethods(Class<T> clazz) {
         Method[] methods = clazz.getDeclaredMethods();
         Collection<Method> result = new ArrayList<>();
         for (Method method : methods) {
             if (method.isAnnotationPresent(Init.class)) {
                 result.add(method);
             }
         }
         return result;
     }
 
 }
