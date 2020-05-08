 package no.niths.common.config;
 
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.io.ClassPathResource;
 
 @Configuration
 @ComponentScan({ AppConfig.SERVICES_PACKAGE, AppConfig.REST_PACKAGE })
 public class TestAppConfig {
//"no.niths.services","no.niths.common.config"
    public static final String BASE_PACKAGE      = "no.niths",
                               PERSISTENCE_PROPS = "test-persistence.properties";
     @Bean
     public static PropertyPlaceholderConfigurer properties(){
     	final PropertyPlaceholderConfigurer ppc =
                 new PropertyPlaceholderConfigurer();
         final ClassPathResource[] resources = new ClassPathResource[] {
                 new ClassPathResource(PERSISTENCE_PROPS)};
         ppc.setLocations(resources);
         ppc.setIgnoreUnresolvablePlaceholders(true);
 
         return ppc;
     }
 }
