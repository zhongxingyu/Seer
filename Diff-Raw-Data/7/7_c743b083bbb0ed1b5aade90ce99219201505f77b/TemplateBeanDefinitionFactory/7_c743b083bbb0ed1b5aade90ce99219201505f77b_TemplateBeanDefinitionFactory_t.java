 package com.mtgi.analytics.aop.config;
 
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
 import org.springframework.beans.factory.config.ConfigurableBeanFactory;
 
 /**
  * <p>Bootstraps a bean from one {@link BeanFactory} into another.  The intent is that we can
  * "embed" one Spring bean factory inside another, and use instances of this class to promote
  * public beans out of the embedded factory.</p>
  * 
  * <p>Not intended to be used directly in spring configuration files, but rather indirectly via
  * {@link TemplateBeanDefinitionParser} subclasses.</p>
  * 
  * @see TemplateBeanDefinitionParser
  */
 public class TemplateBeanDefinitionFactory implements FactoryBean, DisposableBean {
 
 	private BeanFactory beanFactory;
 	private String beanName;
 	
 	public Object getObject() throws Exception {
		if (beanFactory == null || beanName == null)
			throw new FactoryBeanNotInitializedException();
 		return beanFactory.getBean(beanName);
 	}
 
 	public Class<?> getObjectType() {
		return beanFactory == null ? null : beanFactory.getType(beanName);
 	}
 
 	public boolean isSingleton() {
 		return beanFactory.isSingleton(beanName);
 	}
 
 	public void setBeanFactory(BeanFactory beanFactory) {
 		this.beanFactory = beanFactory;
 	}
 
 	public void setBeanName(String beanName) {
 		this.beanName = beanName;
 	}
 
 	public void destroy() throws Exception {
 		if (beanFactory instanceof DisposableBean)
 			((DisposableBean)beanFactory).destroy();
 		else if (beanFactory instanceof ConfigurableBeanFactory)
 			((ConfigurableBeanFactory)beanFactory).destroySingletons();
 	}
 
 }
