 package net.premereur.mvp.core.basic;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.premereur.mvp.core.Event;
 import net.premereur.mvp.core.EventBus;
 import net.premereur.mvp.core.Presenter;
 import net.premereur.mvp.core.View;
 
 public class EventMethodMapper {
 
     public static class HandlerMethodPair {
         private final Class<?> handlerClass;
         private final Method method;
 
         HandlerMethodPair(Class<?> presenterClass, Method method) {
             this.handlerClass = presenterClass;
             this.method = method;
         }
 
         public Class<?> getHandlerClass() {
             return handlerClass;
         }
 
         public Method getMethod() {
             return method;
         }
     }
 
     private final Map<String, List<HandlerMethodPair>> handlingMethodsByEventMethod = new HashMap<String, List<HandlerMethodPair>>();
 
     public void addHandlerMethods(final Iterable<Method> eventBusEventMethods, final Collection<String> verificationErrors) {
         for (final Method eventMethod : eventBusEventMethods) {
             final Event eventAnt = eventMethod.getAnnotation(Event.class);
             final String eventMethodSignature = eventMethodSignature(eventMethod);
             final List<HandlerMethodPair> handlingMethods = createHandlerMethodPairsForEvent(eventMethod, eventAnt, verificationErrors);
             if (handlingMethodsByEventMethod.containsKey(eventMethodSignature)) {
                 handlingMethodsByEventMethod.get(eventMethodSignature).addAll(handlingMethods);
             } else {
                 handlingMethodsByEventMethod.put(eventMethodSignature, handlingMethods);
             }
         }
     }
 
     private static List<HandlerMethodPair> createHandlerMethodPairsForEvent(final Method eventMethod, final Event eventAnt,
             final Collection<String> verificationErrors) {
         final List<HandlerMethodPair> handlingMethods = new ArrayList<HandlerMethodPair>();
         for (final Class<? extends Presenter<? extends View, ? extends EventBus>> presenter : eventAnt.value()) {
             handlingMethods.add(new HandlerMethodPair(presenter, correspondingPresenterMethod(presenter, eventMethod, verificationErrors)));
         }
         return handlingMethods;
     }
 
     private static Method correspondingPresenterMethod(final Class<? extends Presenter<? extends View, ? extends EventBus>> presenter, final Method ebm,
             final Collection<String> verificationErrors) {
         for (final Method pm : presenter.getMethods()) {
             if (presenterMethodCorrespondsWithEventBusMethod(pm, ebm)) {
                 return pm;
             }
         }
         verificationErrors.add("Did not find corresponding public event handler on " + presenter.getName() + " for event bus method " + ebm.getName());
         return null; // TODO change this so that a Method object is returned always
     }
 
     private static boolean presenterMethodCorrespondsWithEventBusMethod(final Method pm, final Method ebm) {
         return eventMethodSignature(ebm).equals(eventMethodSignature(pm));
     }
 
     private static String eventMethodSignature(final Method m) {
         final StringBuilder sb = new StringBuilder();
         addNameWithoutPrefix(sb, m.getName());
         sb.append('+');
         addArgumentTypes(m, sb);
         return sb.toString();
     }
 
     private static void addArgumentTypes(final Method m, final StringBuilder sb) {
         for (Type t : m.getGenericParameterTypes()) {
             sb.append(t.toString());
         }
     }
 
     private static void addNameWithoutPrefix(final StringBuilder sb, final String name) {
         if (name.startsWith("on") && name.length() > 2) {
             sb.append(name.substring(2, 3).toLowerCase()).append(name.substring(3, name.length()));
         } else {
             sb.append(name);
         }
     }
 
     private static final List<HandlerMethodPair> NO_METHODS = Collections.emptyList();
 
     public Iterable<HandlerMethodPair> getHandlerEvents(final Method eventMethod) {
         final Iterable<HandlerMethodPair> methods = this.handlingMethodsByEventMethod.get(eventMethodSignature(eventMethod));
         return methods == null ? NO_METHODS : methods;
     }
 
 }
