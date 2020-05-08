 package de.thischwa.pmcmstest.spring.properties;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.Properties;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.BasicConfigurator;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.context.support.GenericApplicationContext;
 import org.springframework.context.support.GenericXmlApplicationContext;
 import org.springframework.core.io.ByteArrayResource;
 
 public class TestSpringInit {
 
 	private static File propsFile;
 	private static Properties props;
 	
 	@BeforeClass
 	public static void init() throws Exception {
 		BasicConfigurator.configure(); 
 		propsFile = new File("/tmp/test.properties");
 		if(propsFile.exists())
 			propsFile.delete();
 				
 		props = new Properties();
 		props.put("text", "hello");
 		props.put("properties.file", propsFile.getAbsolutePath());
 		props.store(new FileOutputStream(propsFile), "");
 	}
 	
 	@Test
 	public void testPropertiesXml() throws Exception {
 		String springConfig = IOUtils.toString(TestSpringInit.class.getResourceAsStream("spring.xml"));
 		springConfig = springConfig.replace("[props.file]", propsFile.getAbsolutePath());		
 		
 		GenericApplicationContext ctx = new GenericXmlApplicationContext(new ByteArrayResource(springConfig.getBytes()));
 		
 		TestBean b = ctx.getBean(TestBean.class);
 		assertEquals("hello", b.getText());
 		assertEquals(propsFile.getAbsolutePath(), b.getFilename());
 		
 		ctx.close();
 	}
 
 	@Test
 	public void testProperties() throws Exception {
 		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 		ctx.scan(TestSpringInit.class.getPackage().getName());
 		PropertyPlaceholderConfigurer config = new PropertyPlaceholderConfigurer();
 		config.setProperties(props);
 		config.postProcessBeanFactory(ctx.getDefaultListableBeanFactory());
 		ctx.refresh();
 		
 		TestBean b = ctx.getBean(TestBean.class);
 		assertEquals("hello", b.getText());
 		assertEquals(propsFile.getAbsolutePath(), b.getFilename());
		
		ctx.close();
 	}
 	
 }
