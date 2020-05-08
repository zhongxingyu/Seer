 package net.premereur.mvp.core.basic;
 
 import static net.premereur.mvp.util.reflection.ReflectionUtil.uncheckedNewInstance;
 import net.premereur.mvp.core.NeedsPresenter;
 import net.premereur.mvp.core.Presenter;
 import net.premereur.mvp.core.UsesView;
 import net.premereur.mvp.core.View;
 
/**
 * Creates {@link View}s.
 * 
 * @author gpremer
 * 
 */
public final class ViewFactory {
    /**
     * Creates a new {@link View}.
     * 
     * @param handlerClazz the class of the {@link Presenter} managing the view.
     * @param presenter the actual {@link Presenter} managing the view.
     * @return a {@link View}.
     */
     @SuppressWarnings("unchecked")
    public View newView(final Class<?> handlerClazz, final Presenter presenter) {
         View view = uncheckedNewInstance(getViewClass(handlerClazz));
         setPresenterIfRequested(view, presenter);
         return view;
     }
 
     @SuppressWarnings("unchecked")
    private void setPresenterIfRequested(final View view, final Presenter presenter) {
         if (view instanceof NeedsPresenter<?>) {
             ((NeedsPresenter) view).setPresenter(presenter);
         }
     }
 
    private Class<? extends View> getViewClass(final Class<?> handlerClazz) {
         return handlerClazz.getAnnotation(UsesView.class).value();
     }
 
 }
