 package com.griddynamics.jagger.xml.beanParsers.workload.queryProvider;
 
 import com.griddynamics.jagger.invoker.SimpleProvider;
 import com.griddynamics.jagger.xml.beanParsers.CustomBeanDefinitionParser;
 import com.griddynamics.jagger.xml.beanParsers.XMLConstants;
 import org.springframework.beans.factory.support.BeanDefinitionBuilder;
 import org.springframework.beans.factory.support.ManagedList;
 import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
 import org.springframework.beans.factory.xml.ParserContext;
 import org.springframework.util.xml.DomUtils;
 import org.w3c.dom.Element;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: kgribov
  * Date: 1/24/13
  * Time: 3:03 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SimpleQueryProviderDefinitionParser extends CustomBeanDefinitionParser{
 
     @Override
     protected Class getBeanClass(Element element) {
         return SimpleProvider.class;
     }
 
     @Override
     protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        setBeanListProperty(XMLConstants.LIST, false, element, parserContext, builder.getBeanDefinition());
     }
 }
