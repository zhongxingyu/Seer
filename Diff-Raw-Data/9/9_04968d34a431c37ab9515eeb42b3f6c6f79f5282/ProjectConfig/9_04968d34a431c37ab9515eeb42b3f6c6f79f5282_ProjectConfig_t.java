 package de.everald.extdirectspring.config;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.joda.time.DateTime;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
 
 import de.everald.extdirectspring.content.ContentService;
 import de.everald.extdirectspring.objects.GridObject;
 
 @Configuration
 @EnableWebMvc
 @ComponentScan(basePackages = { "ch.ralscha.extdirectspring", "de.everald.extdirectspring" })
 public class ProjectConfig extends WebConfig {
 	private static final int	MAX_RETRIES				= 10;
 	private static final int	TIMEOUT					= 3600000;
 
 	private Boolean				sendExceptionMessage	= true;
 	private Boolean				sendStacktrace			= false;
 
 	@Bean
 	public ch.ralscha.extdirectspring.controller.Configuration getConfig() {
 		final ch.ralscha.extdirectspring.controller.Configuration result = new ch.ralscha.extdirectspring.controller.Configuration();
//		result.setEnableBuffer(false);
 		result.setMaxRetries(MAX_RETRIES);
 		result.setTimeout(TIMEOUT);
 		result.setJsContentType("application/x-javascript");
 		result.setExceptionToMessage(getExceptionToMessageMapping());
 		result.setSendExceptionMessage(sendExceptionMessage);
 		result.setSendStacktrace(sendStacktrace);
 		return result;
 	}
 
 	protected Map<Class<?>, String> getExceptionToMessageMapping() {
 		Map<Class<?>, String> exceptionToMessageMap = new HashMap<Class<?>, String>();
 		return exceptionToMessageMap;
 	}
 
 	@Override
 	public void addViewControllers(final ViewControllerRegistry registry) {
 		registry.addViewController("/index*").setViewName("index");
 		registry.addViewController("/").setViewName("index");
 	}
 
 	@Bean
 	public ContentService configureContentService() {
 		ContentService cS = new ContentService();
 		List<GridObject> list = new ArrayList<GridObject>();
 		cS.setList(list);
 		for (int i = 0; i < 1000; i++) {
 			list.add(new GridObject(i + 1, "Item #" + (i + 1), new DateTime().plusMinutes(i).toDate()));
 		}
 		return cS;
 	}
 }
