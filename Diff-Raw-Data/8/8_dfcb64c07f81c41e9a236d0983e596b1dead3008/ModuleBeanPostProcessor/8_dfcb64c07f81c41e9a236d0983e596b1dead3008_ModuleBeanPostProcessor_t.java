 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.web;
 
 import org.apache.commons.lang.Validate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 
 /**<p>A spring bean post processor that looks for implementations of the Module
  * interface in the application context and registers them.
  * </p>
  */
 public class ModuleBeanPostProcessor implements BeanFactoryPostProcessor {
 
   /** The class logger. */
   private static Logger log = LoggerFactory
       .getLogger(ModuleBeanPostProcessor.class);
 
   /** The registrar we will be getting the contexts from.
    *
    * It is never null.
    */
   private ModuleContextRegistrar contextRegistrar;
 
   /** Build a ModuleBeanPostProcessor.
    *
    * @param theContextRegistrar The context registrar where all the module
    * contexts are registered. It cannot be null.
    */
   public ModuleBeanPostProcessor(final ModuleContextRegistrar
       theContextRegistrar) {
     Validate.notNull(theContextRegistrar, "The context registrar cannot "
         + "be null.");
     contextRegistrar = theContextRegistrar;
   }
 
   /** Post process the bean factory and looks for all beans that implements the
    * Module interface.
    *
    * This method simply records the map of bean names to url. This list will be
    * used in ModuleInitializer to call init on each of the registered modules.
    *
    * @param beanFactory the bean factory where the modules are sought. Cannot
    * be null.
    */
   public void postProcessBeanFactory(final ConfigurableListableBeanFactory
       beanFactory) {
     Validate.notNull(beanFactory, "The beanFactory cannot be null");
 
     log.trace("Entering postProcessBeanFactory");
 
     String[] beanNames = beanFactory.getBeanNamesForType(Module.class);
 
     for (String beanName : beanNames) {
       String name = ModuleUtils.getModuleNameFromBeanName(beanName);
       contextRegistrar.addModuleName(beanName, "module/" + name);
     }
     log.trace("Leaving postProcessBeanFactory");
   }
 }
 
