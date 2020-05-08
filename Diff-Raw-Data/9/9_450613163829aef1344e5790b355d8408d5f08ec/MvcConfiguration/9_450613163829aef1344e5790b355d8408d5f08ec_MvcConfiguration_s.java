 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package}.config;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.web.servlet.ViewResolver;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceConfigurer;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
 import org.springframework.web.servlet.view.InternalResourceViewResolver;
 
 @Configuration
 @ComponentScan(basePackages="${package}")
 @EnableWebMvc
 public class MvcConfiguration extends WebMvcConfigurerAdapter{
 
 	@Bean
 	public ViewResolver getViewResolver(){
 		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
 		resolver.setPrefix("/WEB-INF/views/");
 		resolver.setSuffix(".jsp");
 		return resolver;
 	}
 	
 	@Override
    public void configureResourceHandling(ResourceConfigurer configurer) {
        configurer.addPathMapping("/resources/**").addResourceLocation("/resources/");
    }
 
 	
 }
