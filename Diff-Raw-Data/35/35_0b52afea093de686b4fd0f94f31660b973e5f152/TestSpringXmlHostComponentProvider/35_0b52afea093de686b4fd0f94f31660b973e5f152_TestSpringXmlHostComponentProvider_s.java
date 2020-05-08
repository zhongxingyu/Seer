 package com.atlassian.plugin.spring.pluginns;
 
 import junit.framework.TestCase;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.xml.XmlBeanFactory;
 import org.springframework.core.io.ClassPathResource;
 import com.atlassian.plugin.spring.Fooable;
 import com.atlassian.plugin.osgi.hostcomponents.impl.DefaultComponentRegistrar;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentRegistration;
 
 import java.util.List;
 
 public class TestSpringXmlHostComponentProvider extends TestCase
 {
     public void testProvide()
     {
         XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("com/atlassian/plugin/spring/pluginns/plugins-spring-test.xml"));
 
         SpringXmlHostComponentProvider provider = (SpringXmlHostComponentProvider) factory.getBean(SpringXmlHostComponentProvider.HOST_COMPONENT_PROVIDER);
         assertNotNull(provider);
 
 
         DefaultComponentRegistrar registrar = new DefaultComponentRegistrar();
         provider.provide(registrar);
 
         List<HostComponentRegistration> list = registrar.getRegistry();
         assertNotNull(list);
         assertEquals(1, list.size());
         assertEquals("foo", list.get(0).getProperties().get("bean-name"));
         assertEquals(1, list.get(0).getMainInterfaces().length);
         assertEquals(Fooable.class.getName(), list.get(0).getMainInterfaces()[0]);
     }
 
     public void testProvideWithCustomInterface()
     {
         XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("com/atlassian/plugin/spring/pluginns/plugins-spring-test-interface.xml"));
 
         SpringXmlHostComponentProvider provider = (SpringXmlHostComponentProvider) factory.getBean(SpringXmlHostComponentProvider.HOST_COMPONENT_PROVIDER);
         assertNotNull(provider);
 
 
         DefaultComponentRegistrar registrar = new DefaultComponentRegistrar();
         provider.provide(registrar);
 
         List<HostComponentRegistration> list = registrar.getRegistry();
         assertNotNull(list);
        assertEquals(1, list.size());
         assertEquals("foo", list.get(0).getProperties().get("bean-name"));
         assertEquals(1, list.get(0).getMainInterfaces().length);
         assertEquals(BeanFactoryAware.class.getName(), list.get(0).getMainInterfaces()[0]);
     }
 
     public void testProvideWithNestedContexts()
     {
         XmlBeanFactory parentFactory = new XmlBeanFactory(new ClassPathResource("com/atlassian/plugin/spring/pluginns/plugins-spring-test.xml"));
         XmlBeanFactory childFactory = new XmlBeanFactory(new ClassPathResource("com/atlassian/plugin/spring/pluginns/plugins-spring-test-child.xml"), parentFactory);
 
         SpringXmlHostComponentProvider provider = (SpringXmlHostComponentProvider) childFactory.getBean(SpringXmlHostComponentProvider.HOST_COMPONENT_PROVIDER);
         assertNotNull(provider);
         assertTrue(parentFactory.containsBeanDefinition(SpringXmlHostComponentProvider.HOST_COMPONENT_PROVIDER));
         assertTrue(childFactory.containsBeanDefinition(SpringXmlHostComponentProvider.HOST_COMPONENT_PROVIDER));
 
 
         DefaultComponentRegistrar registrar = new DefaultComponentRegistrar();
         provider.provide(registrar);
 
         List<HostComponentRegistration> list = registrar.getRegistry();
         assertNotNull(list);
         assertEquals(2, list.size());
     }
 
 }
