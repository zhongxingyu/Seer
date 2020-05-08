 package no.niths.common.config;
 
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.io.ClassPathResource;
 
 @Configuration
 @ComponentScan({ AppConfig.SERVICES_PACKAGE, AppConfig.REST_PACKAGE })
 public class TestAppConfig {
    public static final String PERSISTENCE_PROPS =
            "test-persistence.properties";
    public static final String TRANSACTION_MANAGER = "transactionManager";

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
