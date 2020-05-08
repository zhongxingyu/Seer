 package net.premereur.mvp.core.base;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.premereur.mvp.core.Event;
 import net.premereur.mvp.core.EventBus;
 import net.premereur.mvp.core.EventHandler;
 import net.premereur.mvp.util.reflection.ReflectionUtil;
 
 /**
  * Base class with common functionality for event bus invocation handlers.
  * 
  * @author gpremer
  * 
  */
 public abstract class AbstractEventBusInvocationHandler implements InvocationHandler {
     private final EventMethodMapper methodMapper;
 
     private final EventHandlerManager handlerManager;
 
     private final Collection<EventInterceptor> interceptors;
 
     private final HandlerFetchStrategy dispatchHandlerFetchStrategy;
 
     private final HandlerFetchStrategy createHandlerFetchStrategy;
 
     private final HandlerFetchStrategy existingHandlerFetchStrategy;
 
     private static final Logger LOG = Logger.getLogger("net.premereur.mvp.core");
 
     private static final Method DETACH_METHOD;
     private static final Method ATTACH_METHOD;
 
     static {
         try {
             DETACH_METHOD = EventBus.class.getMethod("detach", EventHandler.class);
             ATTACH_METHOD = EventBus.class.getMethod("attach", EventHandler.class);
         } catch (final NoSuchMethodException e) {
             throw new RuntimeException("Bug in " + AbstractEventBusInvocationHandler.class, e); // This is a bug
         }
     }
 
     /**
      * Constructor.
      * 
      * @param eventBusClasses the classes to proxy
      * @param handlerMgr the factory to create handler instances with
      * @param verifier the verifier the concrete InvocationHandler needs
      * @param interceptors The interceptors to call before the event dispatch
      */
     public AbstractEventBusInvocationHandler(final Class<? extends EventBus>[] eventBusClasses, final EventHandlerManager handlerMgr,
             final EventBusVerifier verifier, final Collection<EventInterceptor> interceptors) {
         this.handlerManager = handlerMgr;
         this.interceptors = interceptors;
         this.methodMapper = new EventMethodMapper();
         final Collection<String> verificationErrors = new ArrayList<String>();
         registerAllEventMethods(eventBusClasses, verifier, verificationErrors);
         throwVerificationIfNeeded(verificationErrors);
         this.dispatchHandlerFetchStrategy = new DispatchHandlerFetchStrategy(handlerMgr, methodMapper);
         this.createHandlerFetchStrategy = new CreateHandlerFetchStrategy(handlerMgr, methodMapper);
         this.existingHandlerFetchStrategy = new ExistingHandlerFetchStrategy(handlerMgr, methodMapper);
     }
 
     private void registerAllEventMethods(final Class<? extends EventBus>[] eventBusClasses, final EventBusVerifier verifier,
             final Collection<String> verificationErrors) {
         for (final Class<? extends EventBus> eventBusIntf : eventBusClasses) {
             verifier.verify(eventBusIntf, verificationErrors);
             methodMapper.addHandlerMethods(ReflectionUtil.annotatedMethods(eventBusIntf, Event.class), verificationErrors);
         }
     }
 
     private void throwVerificationIfNeeded(final Collection<String> verificationErrors) {
         if (!verificationErrors.isEmpty()) {
             throw new VerificationException(verificationErrors);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final Object invoke(final Object proxy, final Method eventMethod, final Object[] args) throws Throwable {
         if (isSpecialMethod(eventMethod)) {
             return handleSpecialMethods(proxy, eventMethod, args);
         }
         if (LOG.isLoggable(Level.FINE)) {
             LOG.fine("Receiving event " + methodName(eventMethod) + LogHelper.formatArguments(" with ", args));
         }
         if (executeInterceptorChain((EventBus) proxy, eventMethod, args)) {
             dispatchEventToHandlers(proxy, eventMethod, args, dispatchHandlerFetchStrategy);
             dispatchEventToHandlers(proxy, eventMethod, args, createHandlerFetchStrategy);
             dispatchEventToHandlers(proxy, eventMethod, args, existingHandlerFetchStrategy);
         }
         return null;
     }
 
     /**
      * Can be overridden to do necessary set up with the event bus before it can receive events.
      * 
      * @param eventBus the event bus that is to be prepared for calling.
      */
     protected void prepareEventBusForCalling(final EventBus eventBus) {
     }
 
     private boolean executeInterceptorChain(final EventBus bus, final Method eventMethod, final Object[] args) {
         for (final EventInterceptor interceptor : interceptors) {
             if (!interceptor.beforeEvent(bus, eventMethod, args)) {
                 return false;
             }
         }
         return true;
     }
 
     private void dispatchEventToHandlers(final Object proxy, final Method eventMethod, final Object[] args, final HandlerFetchStrategy handlerFetchStrategy)
             throws IllegalAccessException, InvocationTargetException {
         for (final EventMethodMapper.HandlerMethodPair handlerMethodPair : handlerFetchStrategy.getHandlerMethodPairs(eventMethod)) {
             final Iterable<EventHandler> handlers = handlerFetchStrategy.getHandlers(handlerMethodPair.getHandlerClass(), (EventBus) proxy);
             final Method method = handlerMethodPair.getMethod();
             for (EventHandler handler : handlers) {
                 try {
                     if (LOG.isLoggable(Level.FINE)) {
                         LOG.fine("Dispatching to " + handler.getClass() + " -> " + methodName(method));
                     }
                    prepareEventBusForCalling((EventBus) proxy);
                     method.invoke(handler, args);
                 } catch (InvocationTargetException e) {
                     throw new InvocationTargetException(e, "While invoking " + methodName(method) + " on " + handler.getClass());
                 }
             }
         }
     }
 
     private boolean isSpecialMethod(final Method method) {
         final String methodName = methodName(method);
         return methodName.equals("hashCode") || method.equals(DETACH_METHOD) || method.equals(ATTACH_METHOD);
     }
 
     private Object handleSpecialMethods(final Object proxy, final Method method, final Object[] args) {
         final String methodName = methodName(method);
         if (methodName.equals("hashCode")) {
             return hashCode(); // The hash code of the handler
         }
         if (method.equals(DETACH_METHOD)) {
             final EventHandler handler = (EventHandler) args[0];
             handlerManager.detachPresenter(handler, (EventBus) proxy);
             return null; // void
         }
         if (method.equals(ATTACH_METHOD)) {
             final EventHandler handler = (EventHandler) args[0];
             handlerManager.attachPresenter(handler, (EventBus) proxy);
             return null; // void
         }
         return null;
     }
 
     private String methodName(final Method method) {
         return method.getName();
     }
 
 }
