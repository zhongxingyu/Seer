 package com.atlassian.plugin.spring.pluginns;
 
 import static com.atlassian.plugin.spring.pluginns.SpringXmlHostComponentProvider.HOST_COMPONENT_PROVIDER;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.BeanDefinitionHolder;
 import org.springframework.beans.factory.support.BeanDefinitionRegistry;
 import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
 import org.springframework.beans.factory.xml.ParserContext;
 import org.w3c.dom.Node;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Matches the <plugin:interface> element and registers it against the bean for later processing.
  */
 public class PluginInterfaceBeanDefinitionDecorator implements BeanDefinitionDecorator
 {
     /**
      * Called when the Spring parser encounters an "interface" element.
      * @param source The interface element
      * @param holder The containing bean definition
      * @param ctx The parser context
      * @return The containing bean definition
      */
     public BeanDefinitionHolder decorate(
             Node source, BeanDefinitionHolder holder, ParserContext ctx)
     {
 
         String inf = source.getTextContent();
         if (inf != null)
         {
             inf = inf.trim();
         }
 
         BeanDefinitionRegistry registry = ctx.getRegistry();
         BeanDefinition providerDef = registry.getBeanDefinition(HOST_COMPONENT_PROVIDER);
         List<String> interfaces = loadBeanInterfaces(providerDef, holder.getBeanName());
         interfaces.add(inf);
         return holder;
     }
 
     /**
      * Loads the interface map into the definition for the component provider
      *
      * @param providerDef The definition of the component provider
      * @param beanName The bean to assign the interface against
      * @return The list of registered interfaces, will never be null.
      */
     private List<String> loadBeanInterfaces(BeanDefinition providerDef, String beanName)
     {
         Map<String, List<String>> interfaces = loadInterfaceMap(providerDef);
         if (!interfaces.containsKey(beanName))
         {
             interfaces.put(beanName, new ArrayList<String>());
         }
         return interfaces.get(beanName);
     }
 
     /**
      * Ensures the interfaces map is registered against the host component provider definition
      * @param providerDef The host component provider defintion
      * @return The map of bean names to list of interfaces
      */
     private Map<String, List<String>> loadInterfaceMap(BeanDefinition providerDef)
     {
         Map<String,List<String>> interfaces;
         if (providerDef.getPropertyValues().contains("interfaces"))
         {
            interfaces = (Map<String,List<String>>) providerDef.getPropertyValues().getPropertyValue("interfaces").getValue();
         }
         else
         {
             interfaces = new HashMap<String,List<String>>();
             providerDef.getPropertyValues().addPropertyValue("interfaces", interfaces);
         }
         return interfaces;
     }
 }
