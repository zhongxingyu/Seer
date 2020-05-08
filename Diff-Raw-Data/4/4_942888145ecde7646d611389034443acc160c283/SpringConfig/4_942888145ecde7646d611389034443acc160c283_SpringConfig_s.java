 package ch.rasc.musicsearch.config;
 
 import org.slf4j.bridge.SLF4JBridgeHandler;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.core.env.Environment;
 import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
 import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
 import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
 import org.springframework.web.servlet.view.InternalResourceViewResolver;
 
 @Configuration
 @ComponentScan(basePackages = { "ch.ralscha.extdirectspring", "ch.rasc.musicsearch" })
 @EnableWebMvc
@PropertySource({ "version.properties" })
 public class SpringConfig extends WebMvcConfigurerAdapter {
 
 	@Autowired
 	private Environment environment;
 
 	public SpringConfig() {
 		SLF4JBridgeHandler.removeHandlersForRootLogger();
 		SLF4JBridgeHandler.install();
 	}
 
 	@Override
 	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
 		configurer.enable();
 	}
 
 	@Override
 	public void addResourceHandlers(ResourceHandlerRegistry registry) {
 		ResourceHandlerRegistration registration = registry.addResourceHandler("/resources/**").addResourceLocations(
 				"/resources/");
 		if (environment.acceptsProfiles("production")) {
 			registration.setCachePeriod(31556926);
 		}
 	}
 
 	@Override
 	public void addViewControllers(ViewControllerRegistry registry) {
 		registry.addViewController("/").setViewName("index");
 		registry.addViewController("/index.html").setViewName("index");
 	}
 
 	@Bean
 	public InternalResourceViewResolver viewResolver() {
 		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/jsp/");
 		resolver.setSuffix(".jsp");
 		return resolver;
 	}
 
 }
