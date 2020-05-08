 package net.premereur.mvp.core.basic;
 
 import static net.premereur.mvp.util.reflection.ReflectionUtil.uncheckedNewInstance;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.premereur.mvp.core.EventBus;
 import net.premereur.mvp.core.Presenter;
 import net.premereur.mvp.core.View;
 
 /**
  * A {@link net.premereur.mvp.core.base.PresenterFactory} that uses only standard J2SE libraries and has limited injection possibilities (only the view).
  * 
  * @author gpremer
  * 
  */
 public final class PresenterFactory implements net.premereur.mvp.core.base.PresenterFactory {
 
     private final Map<Class<?>, Presenter<View, ? extends EventBus>> handlerInstancesByClass = new HashMap<Class<?>, Presenter<View, ? extends EventBus>>();
     private static final ViewFactory VIEW_FACTORY = new ViewFactory();
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Presenter<View, ? extends EventBus> getPresenter(final Class<?> presenterClass, final EventBus eventBus) {
         return getHandlerInstance(presenterClass, eventBus);
     }
 
     private Presenter<View, ? extends EventBus> getHandlerInstance(final Class<?> handlerClazz, final EventBus eventBus) {
         synchronized (handlerInstancesByClass) {
             Presenter<View, ? extends EventBus> handlerInstance = (Presenter<View, ? extends EventBus>) handlerInstancesByClass.get(handlerClazz);
             if (handlerInstance == null) {
                 handlerInstance = newHandler(handlerClazz, eventBus);
                 handlerInstancesByClass.put(handlerClazz, handlerInstance);
             }
             return handlerInstance;
         }
     }
 
     @SuppressWarnings("unchecked")
     private static Presenter<View, ? extends EventBus> newHandler(final Class<?> handlerClazz, final EventBus eventBus) {
         Presenter handler = (Presenter) uncheckedNewInstance(handlerClazz);
         handler.setEventBus(eventBus);
        handler.setView(VIEW_FACTORY.newView(handlerClazz, eventBus, handler));
         return handler;
     }
 
 }
