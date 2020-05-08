 package org.synyx.minos.core.web.event;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.beans.factory.support.BeanDefinitionBuilder;
 import org.springframework.beans.factory.support.DefaultListableBeanFactory;
 import org.springframework.beans.factory.support.ManagedList;
 import org.synyx.minos.core.module.support.ModulePostProcessor;
 
 
 
 /**
  * This BeanFactoryPostProcessor registers an {@code Orchestrator} aspect to
  * propagate events thrown by controllers. It scans the bean factory for
  * registered {@code EventHandler} beans and injects them into the orchestrator.
  * <p>
  * Additionally an {@code AnnotationAwareAspectJAutoProxyCreator} is registered
  * if none can be found already.
  * 
  * @author Oliver Gierke - gierke@synyx.de
  */
 public class EventHandlingActivator extends ModulePostProcessor {
 
     private static final Log log = LogFactory
             .getLog(EventHandlingActivator.class);
 
     private static final String DEFAULT_ORCHESTRATOR_NAME = "orchestrator";
 
     private String orchestratorName = DEFAULT_ORCHESTRATOR_NAME;
 
     private String eventContextKey;
     private String eventKey;
 
 
     /**
      * Setter to inject the bean name the orchestrator shall be registered
      * under;
      * 
      * @param orchestratorName the orchestratorName to set
      */
     public void setOrchestratorName(String orchestratorName) {
 
         this.orchestratorName = orchestratorName;
     }
 
 
     /**
      * Sets the key under which the orchestrator will try to find {@code Event}s
      * being published.
      * 
      * @param eventKey the eventKey to set
      */
     public void setEventKey(String eventKey) {
 
         this.eventKey = eventKey;
     }
 
 
     /**
      * Sets the key under which the orchestrator will place the
      * {@code EventContext}.
      * 
      * @param eventContextKey the eventContextKey to set
      */
     public void setEventContextKey(String eventContextKey) {
 
         this.eventContextKey = eventContextKey;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
      */
     @Override
     public void postProcessBeanFactory(
             ConfigurableListableBeanFactory beanFactory) throws BeansException {
 
         // Apply module decoration
         super.postProcessBeanFactory(beanFactory);
 
         // Skip if we can not add further beans
         if (!(beanFactory instanceof DefaultListableBeanFactory)) {
 
             if (log.isWarnEnabled()) {
                 log
                         .warn("Could not add orchestrator as"
                                 + " the given bean "
                                 + "factory does not allow registration of additional beans!");
             }
 
             return;
         }
 
         DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
 
         String[] eventHandlerNames = beanFactory
                 .getBeanNamesForType(AbstractEventHandler.class);
 
         // No handlers found - return
         if (0 == eventHandlerNames.length) {
             return;
         }
 
         // Register auto proxy creator
         registerAopAutoProxyCreator(factory);
 
         // Create bean definition for Orchestrator
         BeanDefinitionBuilder builder = BeanDefinitionBuilder
                 .rootBeanDefinition(EventOrchestrator.class);
 
        ManagedList<BeanDefinition> eventHandlers =
                new ManagedList<BeanDefinition>();
 
         // Create and add event handlers
         for (String eventHandlerName : eventHandlerNames) {
 
             BeanDefinition beanDefinition = factory
                     .getBeanDefinition(eventHandlerName);
 
             eventHandlers.add(beanDefinition);
 
         }
 
         builder.addPropertyValue("eventHandlers", eventHandlers);
 
         propagateProperties(builder);
 
         // Register Orchestrator bean in the factory
         factory.registerBeanDefinition(orchestratorName, builder
                 .getBeanDefinition());
     }
 
 
     /**
      * Propagates the properties configured in the PostProcessor the
      * orchestrator bean.
      * 
      * @param builder
      */
     private void propagateProperties(BeanDefinitionBuilder builder) {
 
         // Modify orchestrator properties if set
         if (null != eventKey) {
             builder.addPropertyValue("eventKey", eventKey);
         }
 
         if (null != eventContextKey) {
             builder.addPropertyValue("eventContextKey", eventContextKey);
         }
     }
 
 
     /**
      * Registers an {@code AnnotationAwareAspectJAutoProxyCreator} if there is
      * currently none registered. This does not require you to explicitly
      * declare an &lt;aop:aspectj-auto-proxy /&gt;.
      * 
      * @param factory
      */
     private void registerAopAutoProxyCreator(DefaultListableBeanFactory factory) {
 
         String[] aopAutoProxyNames = factory
                 .getBeanNamesForType(AnnotationAwareAspectJAutoProxyCreator.class);
 
         if (0 == aopAutoProxyNames.length) {
 
             if (log.isDebugEnabled()) {
                 log.debug("No AnnotationAwareAspectJAutoProxyCreator "
                         + "found! Creating one.");
             }
 
             BeanDefinitionBuilder builder = BeanDefinitionBuilder
                     .rootBeanDefinition(AnnotationAwareAspectJAutoProxyCreator.class);
 
             factory.registerBeanDefinition("aopAutoProxyFactory", builder
                     .getBeanDefinition());
         }
     }
 }
