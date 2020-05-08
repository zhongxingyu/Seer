 package com.burtbeckwith.grails.plugins.dynamiccontroller;
 
 import groovy.lang.GroovySystem;
 import groovy.lang.MetaClass;
 
 import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass;
 import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter;
 import org.codehaus.groovy.grails.commons.InjectableGrailsClass;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 import org.springframework.context.ConfigurableApplicationContext;
 
 /**
  * Controller mixin handler.
  *
  * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
  */
 public class ControllerMixinArtefactHandler extends ArtefactHandlerAdapter {
 
 	/** The artefact type. */
 	public static final String TYPE = "ControllerMixin";
 
 	/**
 	 * Default constructor.
 	 */
 	public ControllerMixinArtefactHandler() {
 		super(TYPE, ControllerMixinGrailsClass.class, DefaultControllerMixinGrailsClass.class, TYPE);
 	}
 
 	/**
 	 * GrailsClass interface for controller mixins.
 	 *
 	 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 	 */
 	public static interface ControllerMixinGrailsClass extends InjectableGrailsClass {
 		// no extra methods
 	}
 
 	/**
 	 * Default implementation of <code>ControllerMixinGrailsClass</code>.
 	 *
 	 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 	 */
	public class DefaultControllerMixinGrailsClass extends AbstractInjectableGrailsClass
	       implements ControllerMixinGrailsClass {
 
 		/**
 		 * Default constructor.
 		 * @param wrappedClass
 		 */
 		public DefaultControllerMixinGrailsClass(Class<?> wrappedClass) {
 			super(wrappedClass, ControllerMixinArtefactHandler.TYPE);
 		}
 
 		@Override
 		public MetaClass getMetaClass() {
 			// Workaround for http://jira.codehaus.org/browse/GRAILS-4542
 			return GroovySystem.getMetaClassRegistry().getMetaClass(DefaultControllerMixinGrailsClass.class);
 		}
 
 		@Override
 		public Object newInstance() {
 			return autowireBeanProperties(super.newInstance());
 		}
 
 		protected Object autowireBeanProperties(Object instance) {
 			ConfigurableApplicationContext ctx = (ConfigurableApplicationContext)grailsApplication.getMainContext();
 			if (ctx != null) { // will be null at startup when creating initial throwaway instances
 				ctx.getBeanFactory().autowireBeanProperties(instance,
 						AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
 			}
 			return instance;
 		}
 	}
 }
