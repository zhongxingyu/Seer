 package com.github.spring.mvc.util.handler.config;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.aop.Pointcut;
 import org.springframework.aop.PointcutAdvisor;
 import org.springframework.aop.config.AopNamespaceUtils;
 import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
 import org.springframework.beans.MutablePropertyValues;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.RuntimeBeanReference;
 import org.springframework.beans.factory.support.BeanDefinitionRegistry;
 import org.springframework.beans.factory.support.RootBeanDefinition;
 import org.springframework.beans.factory.xml.BeanDefinitionParser;
 import org.springframework.beans.factory.xml.ParserContext;
 import org.springframework.beans.factory.xml.XmlReaderContext;
 import org.w3c.dom.Element;
 
import com.github.spring.mvc.util.handler.UriMatchingHandlerInterceptorInterceptor;
 import com.github.spring.mvc.util.handler.UriMatchingStaticMethodMatcherPointcut;
 
 public class UriMatchingAnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {
 
     private static final Log log = LogFactory.getLog(UriMatchingAnnotationDrivenBeanDefinitionParser.class);
 
     static final String CACHING_ADVISOR_BEAN_NAME = UriMatchingAnnotationDrivenBeanDefinitionParser.class.getPackage().getName() + ".internalUriMatchingCachingAdvisor";
 
     /**
      * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
      */
     @Override
     public BeanDefinition parse(Element element, ParserContext parserContext) {
 
         AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);
 
         if (!parserContext.getRegistry().containsBeanDefinition(CACHING_ADVISOR_BEAN_NAME)) {
             log.info("configuring annotation-driven throttling");
             final Object elementSource = parserContext.extractSource(element);
             final RuntimeBeanReference pointcutReference = setupPointcut(element, parserContext, elementSource);
             final RuntimeBeanReference interceptorReference = setupInterceptor(element, parserContext, elementSource);
             setupPointcutAdvisor(element, parserContext, elementSource, pointcutReference, interceptorReference);
         }
 
         return null;
     }
 
     /**
      * Create the {@link Pointcut} used to apply the caching interceptor
      * @return Reference to the {@link Pointcut}. Should never be null.
      */
     protected RuntimeBeanReference setupPointcut(Element element, ParserContext parserContext, Object elementSource) {
         final RootBeanDefinition pointcut = new RootBeanDefinition(UriMatchingStaticMethodMatcherPointcut.class);
         pointcut.setSource(elementSource);
         pointcut.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
 
         final XmlReaderContext readerContext = parserContext.getReaderContext();
         final String pointcutBeanName = readerContext.registerWithGeneratedName(pointcut);
         return new RuntimeBeanReference(pointcutBeanName);
     }
 
     /**
      * Create {@link MethodInterceptor} that is applies the caching logic to advised methods.
      * @return Reference to the {@link MethodInterceptor}. Should never be null.
      */
     protected RuntimeBeanReference setupInterceptor(Element element, ParserContext parserContext, Object elementSource) {
        final RootBeanDefinition interceptor = new RootBeanDefinition(UriMatchingHandlerInterceptorInterceptor.class);
         interceptor.setSource(elementSource);
         interceptor.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
 
         final XmlReaderContext readerContext = parserContext.getReaderContext();
         final String interceptorBeanName = readerContext.registerWithGeneratedName(interceptor);
         return new RuntimeBeanReference(interceptorBeanName);
     }
 
     /**
      * Create {@link PointcutAdvisor} that puts the {@link Pointcut} and {@link MethodInterceptor} together.
      * @return Reference to the {@link PointcutAdvisor}. Should never be null.
      */
     protected RuntimeBeanReference setupPointcutAdvisor(Element element, ParserContext parserContext, Object elementSource, RuntimeBeanReference pointcutReference,  RuntimeBeanReference interceptorReference) {
         final RootBeanDefinition pointcutAdvisor = new RootBeanDefinition(DefaultBeanFactoryPointcutAdvisor.class);
         pointcutAdvisor.setSource(elementSource);
         pointcutAdvisor.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
 
         final MutablePropertyValues propertyValues = pointcutAdvisor.getPropertyValues();
         propertyValues.addPropertyValue("adviceBeanName", interceptorReference.getBeanName());
         propertyValues.addPropertyValue("pointcut", pointcutReference);
         if (element.hasAttribute("order")) {
             propertyValues.addPropertyValue("order", element.getAttribute("order"));
         }
 
         final BeanDefinitionRegistry registry = parserContext.getRegistry();
         registry.registerBeanDefinition(CACHING_ADVISOR_BEAN_NAME, pointcutAdvisor);
         return new RuntimeBeanReference(CACHING_ADVISOR_BEAN_NAME);
     }
 
 }
