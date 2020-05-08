 package net.premereur.mvp.core.basic;
 
 import java.lang.reflect.Method;
 import java.util.Collection;
 
 import net.premereur.mvp.core.Event;
 import net.premereur.mvp.core.EventBus;
 import net.premereur.mvp.core.Presenter;
 import net.premereur.mvp.core.View;
 import net.premereur.mvp.core.guice.IsInterfaceVerfier;
 import net.premereur.mvp.core.verifiers.ConcreteClassVerifier;
 import net.premereur.mvp.core.verifiers.HandlerVerifier;
 import net.premereur.mvp.core.verifiers.HasDefaultConstructorVerifier;
 import net.premereur.mvp.core.verifiers.HasNoPrimitiveAgrumentsVerifier;
 import net.premereur.mvp.core.verifiers.HasUseViewAnnotationVerifier;
 import net.premereur.mvp.core.verifiers.IsVoidMethodVerifier;
 import net.premereur.mvp.core.verifiers.MethodVerifier;
 import net.premereur.mvp.util.reflection.ReflectionUtil;
 
/**
 * A verifier for the basic {@link net.premereur.mvp.core.EventBusFactory}. Verifies as much as possible at event bus construction time that the set up is
 * correct.
 * 
 * @author gpremer
 * 
 */
 public class EventBusVerifier {
 
     private static final HandlerVerifier[] HANDLER_VERIFIERS = { new HasDefaultConstructorVerifier(), new ConcreteClassVerifier(),
             new HasUseViewAnnotationVerifier() };
     private static final MethodVerifier[] METHOD_VERIFIERS = { new HasNoPrimitiveAgrumentsVerifier(), new IsVoidMethodVerifier() };
     private static final EventBusInterfaceVerifier[] EVENT_BUS_VERIFIERS = { new IsInterfaceVerfier() };
 
    /**
     * Verifies the given event bus recording the results in the supplied collection.
     * 
     * @param eventBusIntf the actual event bus class
     * @param verificationErrors to add errors to
     */
    public void verify(final Class<? extends EventBus> eventBusIntf, final Collection<String> verificationErrors) {
         for (final Method eventMethod : ReflectionUtil.annotatedMethods(eventBusIntf, Event.class)) {
             final Event eventAnt = eventMethod.getAnnotation(Event.class);
             verifyHandlers(eventAnt.value(), verificationErrors);
             verifyEventBusMethods(eventMethod, verificationErrors);
         }
         this.verifyEventBus(eventBusIntf, verificationErrors);
     }
 
     private void verifyEventBus(final Class<? extends EventBus> eventBusClass, final Collection<String> verificationErrors) {
         for (final EventBusInterfaceVerifier verifier : getEventBusInterfaceVerifiers()) {
             verificationErrors.addAll(verifier.verifyInterface(eventBusClass));
         }
     }
 
     private void verifyHandlers(final Class<? extends Presenter<? extends View, ? extends EventBus>>[] handlers, final Collection<String> verificationErrors) {
         for (final Class<? extends Presenter<? extends View, ? extends EventBus>> handlerClass : handlers) {
             for (final HandlerVerifier verifier : getHandlerVerifiers()) {
                 verificationErrors.addAll(verifier.verify(handlerClass));
             }
         }
     }
 
     private void verifyEventBusMethods(final Method m, final Collection<String> verificationErrors) {
         for (final MethodVerifier verifier : getMethodVerifiers()) {
             verificationErrors.addAll(verifier.verifyMethod(m));
         }
     }
 
    /**
     * The set of {@link HandlerVerifier}s this event bus verifier uses.
     * 
     * @return an array of {@link HandlerVerifier}s
     */
     protected HandlerVerifier[] getHandlerVerifiers() {
         return HANDLER_VERIFIERS;
     }
 
    /**
     * The set of {@link MethodVerifier}s this event bus verifier uses.
     * 
     * @return an array of {@link MethodVerifier}s
     */
     protected MethodVerifier[] getMethodVerifiers() {
         return METHOD_VERIFIERS;
     }
 
    /**
     * The set of {@link EventBusInterfaceVerifier}s this event bus verifier uses.
     * 
     * @return an array of {@link EventBusInterfaceVerifier}s
     */
     protected EventBusInterfaceVerifier[] getEventBusInterfaceVerifiers() {
         return EVENT_BUS_VERIFIERS;
     }
 }
