 package net.panda.backend;
 
 import net.panda.backend.config.Caches;
 import net.panda.core.security.SecurityRoles;
 import net.panda.service.AdminService;
 import net.panda.service.ConfigurationService;
 import net.panda.service.model.ConfigurationKey;
 import net.panda.service.model.GeneralConfiguration;
 import net.panda.service.model.LDAPConfiguration;
 import net.panda.service.model.MailConfiguration;
 import net.panda.service.validation.LDAPConfigurationValidation;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.cache.annotation.CacheEvict;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.cache.annotation.Caching;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 @Service
 public class AdminServiceImpl implements AdminService {
 
     private final ValidatorService validatorService;
     private final ConfigurationService configurationService;
     private final AtomicInteger ldapConfigurationSequence = new AtomicInteger(0);
 
     @Autowired
     public AdminServiceImpl(ValidatorService validatorService, ConfigurationService configurationService) {
         this.validatorService = validatorService;
         this.configurationService = configurationService;
     }
 
     @Override
     @Transactional(readOnly = true)
     @Cacheable(value = Caches.CONFIGURATION, key = "'general'")
     public GeneralConfiguration getGeneralConfiguration() {
         GeneralConfiguration c = new GeneralConfiguration();
         c.setBaseUrl(configurationService.get(ConfigurationKey.GENERAL_BASE_URL, false, "http://localhost:8080/panda/"));
         return c;
     }
 
     @Override
     @Transactional(readOnly = true)
     @Cacheable(value = Caches.CONFIGURATION, key = "'ldap'")
     public LDAPConfiguration getLDAPConfiguration() {
         LDAPConfiguration c = new LDAPConfiguration();
         boolean enabled = configurationService.getBoolean(ConfigurationKey.LDAP_ENABLED, false, false);
         c.setEnabled(enabled);
         if (enabled) {
             c.setHost(configurationService.get(ConfigurationKey.LDAP_HOST, true, null));
             c.setPort(configurationService.getInteger(ConfigurationKey.LDAP_PORT, true, 0));
             c.setSearchBase(configurationService.get(ConfigurationKey.LDAP_SEARCH_BASE, true, null));
             c.setSearchFilter(configurationService.get(ConfigurationKey.LDAP_SEARCH_FILTER, true, null));
             c.setUser(configurationService.get(ConfigurationKey.LDAP_USER, true, null));
             c.setPassword(configurationService.get(ConfigurationKey.LDAP_PASSWORD, true, null));
             c.setFullNameAttribute(configurationService.get(ConfigurationKey.LDAP_FULLNAME_ATTRIBUTE, false, ""));
             c.setEmailAttribute(configurationService.get(ConfigurationKey.LDAP_EMAIL_ATTRIBUTE, false, ""));
         } else {
             // Default values
             c.setPort(389);
         }
         // OK
         c.setSequence(ldapConfigurationSequence.incrementAndGet());
         return c;
     }
 
     @Override
     @Transactional(readOnly = true)
     @Cacheable(value = Caches.CONFIGURATION, key = "'mail'")
     public MailConfiguration getMailConfiguration() {
         MailConfiguration c = new MailConfiguration();
         c.setHost(configurationService.get(ConfigurationKey.MAIL_HOST, false, null));
         c.setReplyToAddress(configurationService.get(ConfigurationKey.MAIL_REPLY_TO_ADDRESS, false, null));
         c.setAuthentication(configurationService.getBoolean(ConfigurationKey.MAIL_AUTHENTICATION, false, false));
         c.setStartTls(configurationService.getBoolean(ConfigurationKey.MAIL_START_TLS, false, false));
         c.setUser(configurationService.get(ConfigurationKey.MAIL_USER, false, null));
         c.setPassword(configurationService.get(ConfigurationKey.MAIL_PASSWORD, false, null));
         return c;
     }
 
     @Override
     @Transactional
     @Secured(SecurityRoles.ADMINISTRATOR)
     @CacheEvict(value = Caches.CONFIGURATION, key = "'general'")
     public void saveGeneralConfiguration(GeneralConfiguration configuration) {
         String baseUrl = configuration.getBaseUrl();
         if (!baseUrl.endsWith("/")) {
             baseUrl += "/";
         }
         configurationService.set(ConfigurationKey.GENERAL_BASE_URL, baseUrl);
     }
 
     @Override
     @Transactional
     @Secured(SecurityRoles.ADMINISTRATOR)
     @Caching(evict = {
             @CacheEvict(value = Caches.CONFIGURATION, key = "'ldap'"),
             @CacheEvict(value = Caches.LDAP, key = "'0'")
     })
     public void saveLDAPConfiguration(LDAPConfiguration configuration) {
         // Saving...
         configurationService.set(ConfigurationKey.LDAP_ENABLED, configuration.isEnabled());
         if (configuration.isEnabled()) {
            // Validation
            validatorService.validate(configuration, LDAPConfigurationValidation.class);
            // OK
             configurationService.set(ConfigurationKey.LDAP_HOST, configuration.getHost());
             configurationService.set(ConfigurationKey.LDAP_PORT, configuration.getPort());
             configurationService.set(ConfigurationKey.LDAP_SEARCH_BASE, configuration.getSearchBase());
             configurationService.set(ConfigurationKey.LDAP_SEARCH_FILTER, configuration.getSearchFilter());
             configurationService.set(ConfigurationKey.LDAP_USER, configuration.getUser());
             configurationService.set(ConfigurationKey.LDAP_PASSWORD, configuration.getPassword());
             configurationService.set(ConfigurationKey.LDAP_FULLNAME_ATTRIBUTE, configuration.getFullNameAttribute());
             configurationService.set(ConfigurationKey.LDAP_EMAIL_ATTRIBUTE, configuration.getEmailAttribute());
         }
     }
 
     @Override
     @Transactional
     @Secured(SecurityRoles.ADMINISTRATOR)
     @Caching(evict = {
             @CacheEvict(value = Caches.CONFIGURATION, key = "'mail'"),
             @CacheEvict(value = Caches.MAIL, key = "'0'")
     })
     public void saveMailConfiguration(MailConfiguration configuration) {
         configurationService.set(ConfigurationKey.MAIL_HOST, configuration.getHost());
         configurationService.set(ConfigurationKey.MAIL_REPLY_TO_ADDRESS, configuration.getReplyToAddress());
         configurationService.set(ConfigurationKey.MAIL_USER, configuration.getUser());
         configurationService.set(ConfigurationKey.MAIL_PASSWORD, configuration.getPassword());
         configurationService.set(ConfigurationKey.MAIL_AUTHENTICATION, configuration.isAuthentication());
         configurationService.set(ConfigurationKey.MAIL_START_TLS, configuration.isStartTls());
     }
 }
