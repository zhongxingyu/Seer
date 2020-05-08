 package ru.devg.dem.inclass.binding;
 
 import ru.devg.dem.bounding.BoundedHandler;
 import ru.devg.dem.bounding.TypeFilter;
 import ru.devg.dem.inclass.ClassIsUnbindableException;
 import ru.devg.dem.inclass.Handles;
 import ru.devg.dem.inclass.binding.exceptions.ElementIsUnbindableException;
 import ru.devg.dem.inclass.binding.exceptions.MethodIsUnbindableException;
 import ru.devg.dem.quanta.Event;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.List;
 
 /**
  * @author Devgru &lt;java@devg.ru&gt;
  * @since 0.176
  */
 final class MethodWorker extends AbstractBinder {
 
     public MethodWorker(Object target) {
         super(target);
     }
 
     public void tryBindMembers(List<FilterWithPriority> grabbed, Class<?> targetClass) throws ClassIsUnbindableException {
        for (Method method : targetClass.getMethods()) {
             FilterWithPriority filter = tryBindMethod(method);
             if (filter != null) grabbed.add(filter);
         }
     }
 
     private FilterWithPriority tryBindMethod(Method method) throws ClassIsUnbindableException {
         try {
             if (method.isAnnotationPresent(Handles.class)) {
                 return bindMethod(method);
             } else {
                 return null;
             }
         } catch (ElementIsUnbindableException e) {
             throw new ClassIsUnbindableException(e);
         }
     }
 
     private FilterWithPriority bindMethod(Method method) throws ElementIsUnbindableException {
         TypeFilter halfResult;
 
         Class<?>[] types = method.getParameterTypes();
         int argsCount = types.length;
         Handles annotation = method.getAnnotation(Handles.class);
         if (argsCount > 1) {
             throw new MethodIsUnbindableException("method shouldn't have more than 1 parameter.");
             //todo here I should implement the event-to-array translation
         } else if (argsCount == 1) {
             Class<?> argClass = types[0];
            if (argClass != annotation.value()) {
                 throw new MethodIsUnbindableException("declared parameter's type must be equal to annotated class.");
             }
             halfResult = new MethodInvoker(argClass, method, true);
         } else {
             halfResult = new MethodInvoker(annotation.value(), method, false);
         }
 
         return wrap(annotation, halfResult);
     }
 
     private class MethodInvoker extends BoundedHandler {
         private final Method method;
         private final boolean passEventToMethod;
 
         @SuppressWarnings("unchecked")
         private MethodInvoker(Class<?> bound, Method method, boolean passEventToMethod) {
             super(bound);
             assert method != null;
             this.method = method;
             this.passEventToMethod = passEventToMethod;
         }
 
         public void handle(Event event) {
             try {
                 if (passEventToMethod) {
                     method.invoke(target, event);
                 } else {
                     method.invoke(target);
                 }
             } catch (IllegalAccessException ignored) {
             } catch (InvocationTargetException ignored) {
             }
         }
 
         @Override
         public String toString() {
             return "method " + method.getName();
         }
     }
 
 }
