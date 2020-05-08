 /**
  * 
  */
 package org.wicketstuff.jsr303;
 
import com.google.common.base.Preconditions;
 import de.flower.common.annotation.Patched;
 import org.apache.wicket.Application;
 import org.apache.wicket.MetaDataKey;
 import org.apache.wicket.Session;
 import org.apache.wicket.injection.Injector;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import javax.validation.*;
 import java.util.Locale;
 
 /**
  * Patched version to use cached validator factory instead of re-creating validator factory for every #getValidator call.
  *
  * @author flowerrrr
  */
 
 @Patched
 public class JSR303Validation
 {
 	public static class WicketSessionLocaleMessageInterpolator implements MessageInterpolator
 	{
 		private final MessageInterpolator delegate;
 
 		public WicketSessionLocaleMessageInterpolator(final MessageInterpolator delegate)
 		{
 			this.delegate = delegate;
			Preconditions.checkNotNull(delegate, "delegate");
 		}
 
 		public String interpolate(final String message, final Context context)
 		{
 			return delegate.interpolate(message, context, Session.get().getLocale());
 		}
 
 		public String interpolate(final String message, final Context context, final Locale locale)
 		{
 			return delegate.interpolate(message, context, Session.get().getLocale());
 		}
 	}
 
 	private ValidatorFactory createFactory()
 	{
 
 		final Configuration<?> configuration = Validation.byDefaultProvider().configure();
 		// FIXME seems like needed for hib-val 4.0.2.? strange enough it does
 		// not respect the locale passed on interpolate call. Working on it.
 
 		// geez. they screwed it up.
 		// http://opensource.atlassian.com/projects/hibernate/browse/HV-306
 		// fixed in 4.1.0.beta2 ... Locale.setDefault(Session.get().getLocale());
 
 		final ValidatorFactory validationFactory = configuration.messageInterpolator(
 			new WicketSessionLocaleMessageInterpolator(
 				configuration.getDefaultMessageInterpolator())).buildValidatorFactory();
 
 		return validationFactory;
 	}
 
     private synchronized ValidatorFactory getFactory() {
         if (validatorFactory == null) {
             validatorFactory = createFactory();
         }
         return validatorFactory;
     }
 
 	private static final JSR303Validation INSTANCE = new JSR303Validation();
     private ValidatorFactory validatorFactory;
 	private static final MetaDataKey<ViolationMessageRenderer> violationMessageRendererKey = new MetaDataKey<ViolationMessageRenderer>()
 	{
 		private static final long serialVersionUID = 1L;
 	};
 
 	public static final JSR303Validation getInstance()
 	{
 		return INSTANCE;
 	}
 
     @SpringBean
     private Validator validator;
 
 	public Validator getValidator()
 	{
 		// return getInstance().getFactory().getValidator();
         return validator;
 	}
 
 
 	private JSR303Validation()
 	{
          Injector.get().inject(this);
 	}
 
 	synchronized static ViolationMessageRenderer getViolationMessageRenderer()
 	{
 		final Application app = Application.get();
 		ViolationMessageRenderer renderer = app.getMetaData(violationMessageRendererKey);
 		if (renderer == null)
 		{
 			renderer = new ViolationMessageRenderer.Default();
 			setViolationMessageRenderer(renderer);
 		}
 		return renderer;
 	}
 
 	public synchronized static void setViolationMessageRenderer(
 		final ViolationMessageRenderer renderer)
 	{
		Preconditions.checkNotNull(renderer, "renderer");
 		final Application app = Application.get();
 		app.setMetaData(violationMessageRendererKey, renderer);
 	}
 
 }
