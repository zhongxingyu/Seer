 package de.thischwa.pmcmstest.spring;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.BasicConfigurator;
 import org.springframework.beans.factory.xml.XmlBeanFactory;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.core.io.ByteArrayResource;
 
 public class SpringExample {
 
 	public static void main(String[] args) {
 		BasicConfigurator.configure();
 
 		// init spring;
 		try {
 			InputStream in = SpringExample.class.getResourceAsStream("spring-config.xml");
			AnnotationConfigApplicationContext annoCtx = new AnnotationConfigApplicationContext("de.thischwa.pmcms.spring");
 			XmlBeanFactory ctx = new XmlBeanFactory(new ByteArrayResource(IOUtils.toByteArray(in)));
 			ctx.setParentBeanFactory(annoCtx);
 			
 			for(String bean : ctx.getBeanDefinitionNames())
 				System.out.println("  - known bean: " + bean);
 						
 			Masterbean mb = (Masterbean) ctx.getBean("master");
 			System.out.println(mb.getInnerMessage());
 			
 			IMessage aloneBean = (IMessage) ctx.getBean("alone");
 			System.out.println(aloneBean.getMessage());
 		} catch (IOException ioe) {
 			throw new RuntimeException(ioe);
 		}
 	}
 }
