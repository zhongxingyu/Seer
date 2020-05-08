 package com.edify.config;
 
 import com.googlecode.flyway.core.Flyway;
 import com.jolbox.bonecp.BoneCPDataSource;
 import org.hibernate.ejb.HibernatePersistence;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.*;
 import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 import org.springframework.mail.javamail.JavaMailSenderImpl;
 import org.springframework.orm.jpa.JpaDialect;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.spi.PersistenceProvider;
 import javax.sql.DataSource;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * @author jarias
  * @since 9/2/12 11:25 AM
  */
 @Configuration
 @ComponentScan(basePackages = "com.edify")
 @ImportResource({"classpath:META-INF/spring/applicationContext-security.xml", "classpath:META-INF/spring/applicationContext.xml"})
 @EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
 public class ApplicationConfig {
     @Value("${database.url}")
     private String databaseUrl;
     @Value("${database.driver.classname}")
     private String databaseDriverClassname;
     @Value("${database.username}")
     private String databaseUsername;
     @Value("${database.password}")
     private String databasePassword;
     @Value("${hibernate.dialect}")
     private String hibernateDialect;
 
     //DataSource properties
     @Value("${datasource.bonecp.IdleConnectionTestPeriodInMinutes}")
     private Integer idleConnectionTestPeriodInMinutes;
     @Value("${datasource.bonecp.IdleMaxAgeInMinutes}")
     private Integer idleMaxAgeInMinutes;
     @Value("${datasource.bonecp.MaxConnectionsPerPartition}")
     private Integer maxConnectionsPerPartition;
     @Value("${datasource.bonecp.MinConnectionsPerPartition}")
     private Integer minConnectionsPerPartition;
     @Value("${datasource.bonecp.PartitionCount}")
     private Integer partitionCount;
     @Value("${datasource.bonecp.AcquireIncrement}")
     private Integer acquireIncrement;
     @Value("${datasource.bonecp.StatementsCacheSize}")
     private Integer statementsCacheSize;
     @Value("${datasource.bonecp.ReleaseHelperThreads}")
     private Integer releaseHelperThreads;
 
     @Value("${mail.smtp.host}")
     private String mailSMTPHost;
     @Value("${mail.smtp.port}")
     private Integer mailSMTPPort;
     @Value("${mail.smtp.auth}")
     private Boolean mailSMTPAuth;
     @Value("${mail.smtp.username}")
     private String mailSMTPUsername;
     @Value("${mail.smtp.password}")
     private String mailSMTPPassword;
 
     @Bean
     static public PropertySourcesPlaceholderConfigurer placeholderConfigurer() throws IOException {
         PropertySourcesPlaceholderConfigurer p = new PropertySourcesPlaceholderConfigurer();
         PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

         List<Resource> resourceLocations = new ArrayList<Resource>();
         //Read from class path properties first
         resourceLocations.addAll(Arrays.asList(resourcePatternResolver.getResources("classpath*:META-INF/spring/*.properties")));
         //Read from a file location (user for servers)
         resourceLocations.addAll(Arrays.asList(resourcePatternResolver.getResources("file:/srv/config/*.properties")));
         //This resource allows developers to override any app property for their development environment
         resourceLocations.add(resourcePatternResolver.getResource(String.format("file:%s/.gradle/changeme.properties", System.getProperty("user.home"))));

        p.setLocations((Resource[]) resourceLocations.toArray());
         return p;
     }
 
     @Bean
     public DataSource dataSource() {
         BoneCPDataSource ds = new BoneCPDataSource();
         ds.setDriverClass(databaseDriverClassname);
         ds.setJdbcUrl(databaseUrl);
         ds.setUsername(databaseUsername);
         ds.setPassword(databasePassword);
         ds.setIdleConnectionTestPeriodInMinutes(idleConnectionTestPeriodInMinutes);
         ds.setIdleMaxAgeInMinutes(idleMaxAgeInMinutes);
         ds.setMaxConnectionsPerPartition(maxConnectionsPerPartition);
         ds.setMinConnectionsPerPartition(minConnectionsPerPartition);
         ds.setPartitionCount(partitionCount);
         ds.setAcquireIncrement(acquireIncrement);
         ds.setStatementsCacheSize(statementsCacheSize);
         ds.setReleaseHelperThreads(releaseHelperThreads);
         ds.setDefaultAutoCommit(false);
         return ds;
     }
 
     @Bean
     public PersistenceProvider persistenceProvider() {
         return new HibernatePersistence();
     }
 
     @Bean
     public JpaDialect jpaDialect() {
         return new HibernateJpaDialect();
     }
 
     @Bean(name = "entityManagerFactory")
     @DependsOn("flyway")
     public EntityManagerFactory entityManagerFactory() {
         LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
         localContainerEntityManagerFactoryBean.setDataSource(dataSource());
         localContainerEntityManagerFactoryBean.setPersistenceProvider(persistenceProvider());
         localContainerEntityManagerFactoryBean.setPackagesToScan("change.me.model");
         localContainerEntityManagerFactoryBean.setJpaDialect(jpaDialect());
         Properties jpaProperties = new Properties();
         jpaProperties.setProperty("hibernate.dialect", hibernateDialect);
         jpaProperties.setProperty("hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
         jpaProperties.setProperty("hibernate.connection.charSet", "UTF-8");
         localContainerEntityManagerFactoryBean.setJpaProperties(jpaProperties);
         localContainerEntityManagerFactoryBean.afterPropertiesSet();
         return localContainerEntityManagerFactoryBean.getObject();
     }
 
     @Bean(name = "transactionManager")
     public JpaTransactionManager transactionManager() {
         JpaTransactionManager transactionManager = new JpaTransactionManager();
         transactionManager.setEntityManagerFactory(entityManagerFactory());
         return transactionManager;
     }
 
     @Bean(name = "mailSender")
     public JavaMailSenderImpl mailSender() {
         Properties javaMailProperties = new Properties();
         JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
         mailSender.setHost(mailSMTPHost);
         mailSender.setPort(mailSMTPPort);
         if (mailSMTPAuth) {
             mailSender.setUsername(mailSMTPUsername);
             mailSender.setPassword(mailSMTPPassword);
             javaMailProperties.setProperty("mail.smtp.auth", "true");
         } else {
             javaMailProperties.setProperty("mail.smtp.auth", "false");
         }
         mailSender.setJavaMailProperties(javaMailProperties);
         return mailSender;
     }
 
     @Bean(name = "flyway", initMethod = "migrate")
     public Flyway flyway() {
         Flyway flyway = new Flyway();
         flyway.setDataSource(dataSource());
         return flyway;
     }
 }
