 package net.premereur.mvp.core.guice;
 
 import static java.util.Arrays.asList;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.premereur.mvp.core.EventBus;
 import net.premereur.mvp.core.base.AbstractEventBusFactory;
 import net.premereur.mvp.core.base.EventInterceptor;
 import net.premereur.mvp.core.base.EventHandlerManager;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 
 /**
  * A factory for event busses that uses guice for dependency injection of the instances it creates. There will be typically only one event bus factory for a
  * whole application, but the factory will create instances for every user session.
  * 
  * @param <EB> the type of the main event bus.
  * @author gpremer
  * 
  */
 public final class GuiceEventBusFactory<EB extends EventBus> extends AbstractEventBusFactory<EB> {
 
     private final Injector guiceInjector;
     private final EventBusModule eventBusModule;
     private final Collection<Class<? extends EventInterceptor>> interceptorClasses;
 
     /**
      * Specification for {@link GuiceEventBusFactory} instances.
      * 
      * @author gpremer
      * 
      * @param <E> an EventBus interface
      */
     public static final class Configuration<E extends EventBus> extends AbstractEventBusFactory.Configuration<E, Configuration<E>> {
 
         private final Set<Module> modules = new HashSet<Module>();
 
         private final Set<Class<? extends EventInterceptor>> interceptorClasses = new HashSet<Class<? extends EventInterceptor>>();
 
         private Configuration(final Class<E> mainEventBusInterface) {
             super(mainEventBusInterface);
         }
 
         /**
          * Configuring a {@link GuiceEventBusFactory} by declaring which modules it uses to satisfy dependencies.
          * 
          * @param additionalModules the modules to add
          * @return a Configuration instance.
          */
         public Configuration<E> using(final Module... additionalModules) {
             this.modules.addAll(asList(additionalModules));
             return this;
         }
 
         /**
          * The interceptor will be provided by Guice when an event bus instance is created.
          * 
          * @param interceptorClass the class of the interceptor to be
          * @return a Configuration instance
          */
         public Configuration<E> interceptedBy(final Class<? extends EventInterceptor> interceptorClass) {
             interceptorClasses.add(interceptorClass);
             return this;
         }
 
         /**
          * The modules that were specified.
          * 
          * @return the modules
          */
         private Set<Module> getModules() {
             return modules;
         }
 
         private Set<Class<? extends EventInterceptor>> getInterceptorClasses() {
             return interceptorClasses;
         }
 
         /**
          * Finally creates the factory.
          * 
          * @return an event bus factory that can be used to create event bus instances
          */
         public GuiceEventBusFactory<E> build() {
             return new GuiceEventBusFactory<E>(getModules(), getEventBusInterfaces(), getInterceptors(), getInterceptorClasses());
         }
 
     }
 
     /**
      * Starts specifying an EventBusFactory by assigning the type of the main segments. The main segment is not different from the other segments, but it does
      * determine the type with which the event bus is instantiated.
      * 
      * @param <E> the main type of the event bus
      * @param mainEventBusInterface the main type of the event bus
      * @return a specification that can be configured further
      */
     public static <E extends EventBus> Configuration<E> withMainSegment(final Class<E> mainEventBusInterface) {
         return new Configuration<E>(mainEventBusInterface);
     }
 
     /**
      * Starts specifying an EventBusFactory by assigning the type of the main segments. The main segment is not different from the other segments, but it does
      * determine the type with which the event bus is instantiated.
      * 
      * @param <E> the main type of the event bus
      * @param mainEventBusInterface the main type of the event bus
      * @param additionalEventBusInterfaces Additional event bus interfaces to implement
      * @return a specification that can be configured further
      */
     public static <E extends EventBus> Configuration<E> withSegments(final Class<E> mainEventBusInterface,
             final Class<? extends EventBus>... additionalEventBusInterfaces) {
         final Configuration<E> configuration = new Configuration<E>(mainEventBusInterface);
         configuration.withAdditionalSegments(additionalEventBusInterfaces);
         return configuration;
     }
 
     /**
      * Creates a GuiceEventBusFactory.
      * 
      * @param modules the modules containing configuration information for objects that need to be injected.
      * @param eventBusInterfaces the interfaces the event bus will implement
      * @param interceptors Interceptors to be called before invoking an event
      * @param interceptorClasses Interceptors to be provided by Guice
      */
     protected GuiceEventBusFactory(final Collection<Module> modules, final Collection<Class<? extends EventBus>> eventBusInterfaces,
             final Collection<EventInterceptor> interceptors, final Collection<Class<? extends EventInterceptor>> interceptorClasses) {
         super(eventBusInterfaces, interceptors);
         final List<Module> allModules = new ArrayList<Module>(modules);
         eventBusModule = new EventBusModule(getEventBusInterfaces());
         allModules.add(eventBusModule);
         this.guiceInjector = Guice.createInjector(allModules);
         this.interceptorClasses = interceptorClasses;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected InvocationHandler createInvocationHandler(final Class<? extends EventBus>[] eventBusIntfs, final Collection<EventInterceptor> interceptors) {
         final Collection<EventInterceptor> allInterceptors = new ArrayList<EventInterceptor>(interceptors);
         for (final Class<? extends EventInterceptor> interceptorClass : interceptorClasses) {
             allInterceptors.add(guiceInjector.getInstance(interceptorClass));
         }
         return new GuiceEventBusInvocationHandler(eventBusIntfs, guiceInjector.getInstance(EventHandlerManager.class), allInterceptors);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected Object createProxy(final Class<? extends EventBus>[] eventBusIntfs, final InvocationHandler handler) {
         final Object proxy = Proxy.newProxyInstance(GuiceEventBusFactory.class.getClassLoader(), eventBusIntfs, handler);
        EventBusModule.setThreadEventBus((EventBus) proxy);
         return proxy;
     }
 
 }
