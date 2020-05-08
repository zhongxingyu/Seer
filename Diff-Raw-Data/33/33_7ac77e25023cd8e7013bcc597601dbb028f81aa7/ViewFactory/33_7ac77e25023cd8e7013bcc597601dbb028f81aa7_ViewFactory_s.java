 package net.premereur.mvp.core.basic;
 
 import static net.premereur.mvp.util.reflection.ReflectionUtil.uncheckedNewInstance;
import net.premereur.mvp.core.EventBus;
 import net.premereur.mvp.core.NeedsPresenter;
 import net.premereur.mvp.core.Presenter;
 import net.premereur.mvp.core.UsesView;
 import net.premereur.mvp.core.View;
 
public class ViewFactory {
     @SuppressWarnings("unchecked")
    public View newView(Class<?> handlerClazz, EventBus bus, Presenter presenter) {
         View view = uncheckedNewInstance(getViewClass(handlerClazz));
         setPresenterIfRequested(view, presenter);
         return view;
     }
 
     @SuppressWarnings("unchecked")
    private void setPresenterIfRequested(View view, Presenter presenter) {
         if (view instanceof NeedsPresenter<?>) {
             ((NeedsPresenter) view).setPresenter(presenter);
         }
     }
 
    private Class<? extends View> getViewClass(Class<?> handlerClazz) {
         return handlerClazz.getAnnotation(UsesView.class).value();
     }
 
 }
