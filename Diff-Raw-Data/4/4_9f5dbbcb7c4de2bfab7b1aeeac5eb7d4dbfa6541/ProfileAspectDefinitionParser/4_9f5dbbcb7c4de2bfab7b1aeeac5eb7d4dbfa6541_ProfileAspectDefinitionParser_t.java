 /*
  * Copyright 2008-2010 Xebia and the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.xebia.management.config;
 
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.support.AbstractBeanDefinition;
 import org.springframework.beans.factory.support.BeanDefinitionBuilder;
 import org.springframework.beans.factory.support.RootBeanDefinition;
 import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
 import org.springframework.beans.factory.xml.ParserContext;
 import org.springframework.jmx.support.WebSphereMBeanServerFactoryBean;
 import org.springframework.jndi.JndiObjectFactoryBean;
 import org.springframework.util.ClassUtils;
 import org.springframework.util.StringUtils;
 import org.w3c.dom.Element;
 
 import fr.xebia.management.statistics.ProfileAspect;
 
 public class ProfileAspectDefinitionParser extends AbstractBeanDefinitionParser {
 
     private static final String PROFILE_ASPECT_BEAN_NAME = "profileAspect";
 
     private static final String SERVER_ATTRIBUTE = "server";
     
     private static final String JMX_DOMAIN_ATTRIBUTE = "jmxDomain";
 
     @Override
     protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
         String id = element.getAttribute(ID_ATTRIBUTE);
         if (!StringUtils.hasText(id)) {
             id = PROFILE_ASPECT_BEAN_NAME;
         }
         return id;
     }
 
     @Override
     protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
         BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ProfileAspect.class);
 
         // Mark as infrastructure bean and attach source location.
         builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
         builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
 
         String serverBeanName = element.getAttribute(SERVER_ATTRIBUTE);
         if (StringUtils.hasText(serverBeanName)) {
             builder.addPropertyReference("server", serverBeanName);
         } else {
             AbstractBeanDefinition specialServer = findServerForSpecialEnvironment();
             if (specialServer != null) {
                 builder.addPropertyValue("server", specialServer);
             }
         }
 
         String jmxDomain = element.getAttribute(JMX_DOMAIN_ATTRIBUTE);
        if (StringUtils.hasLength(jmxDomain)) {
            builder.addPropertyValue("jmxDomain", jmxDomain);
         }
         return builder.getBeanDefinition();
     }
 
     /**
      * Logic taken from
      * <code>org.springframework.context.config.MBeanServerBeanDefinitionParser.findServerForSpecialEnvironment()</code>
      */
     static AbstractBeanDefinition findServerForSpecialEnvironment() {
         boolean weblogicPresent = ClassUtils.isPresent("weblogic.management.Helper", ProfileAspectDefinitionParser.class.getClassLoader());
 
         boolean webspherePresent = ClassUtils.isPresent("com.ibm.websphere.management.AdminServiceFactory",
                 ProfileAspectDefinitionParser.class.getClassLoader());
 
         if (weblogicPresent) {
             RootBeanDefinition bd = new RootBeanDefinition(JndiObjectFactoryBean.class);
             bd.getPropertyValues().add("jndiName", "java:comp/env/jmx/runtime");
             return bd;
         } else if (webspherePresent) {
             return new RootBeanDefinition(WebSphereMBeanServerFactoryBean.class);
         } else {
             return null;
         }
     }
 }
