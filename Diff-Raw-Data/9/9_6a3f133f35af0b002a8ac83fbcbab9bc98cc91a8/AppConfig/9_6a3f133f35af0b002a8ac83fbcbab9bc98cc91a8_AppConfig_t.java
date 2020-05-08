 package dk.drb.blacktiger.config;
 
 import dk.drb.blacktiger.service.CallInformationService;
 import dk.drb.blacktiger.service.ConferenceService;
 import dk.drb.blacktiger.service.PhonebookService;
 import dk.drb.blacktiger.service.UserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Import;
 import org.springframework.context.annotation.ImportResource;
 import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
 import org.springframework.core.env.Environment;
 
 @Configuration
 @PropertySource({"classpath:blacktiger.properties","file:${user.home}/blacktiger.properties"})
 @Import(RepositoryConfig.class)
 @ImportResource("classpath:springsecurity.xml")
 public class AppConfig {
 
     @Autowired
     Environment env;
 
     @Bean
     public CallInformationService callInformationService() {
         return new CallInformationService();
     }
 
     @Bean
     public ConferenceService conferenceService() {
         return new ConferenceService();
     }
 
     @Bean
     public PhonebookService phonebookService() {
         return new PhonebookService();
     }
 
     @Bean
     public UserService userService() {
         return new UserService();
     }
     
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("dk.drb.blacktiger.i18n.Messages");
        return messageSource;
    }
    
 }
