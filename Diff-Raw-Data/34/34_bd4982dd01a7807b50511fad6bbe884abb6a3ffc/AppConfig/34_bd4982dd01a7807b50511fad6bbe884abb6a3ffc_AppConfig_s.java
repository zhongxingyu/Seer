 package com.jrock.bulkwait.config;
 
 import com.jrock.bulkwait.activemq.Consumer;
 import com.jrock.bulkwait.activemq.Producer;
 import com.jrock.bulkwait.repository.BubbleRepository;
 import com.jrock.bulkwait.repository.internal.BubbleRepositoryHibernateImpl;
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.apache.activemq.command.ActiveMQQueue;
 import org.hibernate.dialect.H2Dialect;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Profile;
 import org.springframework.jdbc.datasource.DataSourceTransactionManager;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
 import org.springframework.jms.core.JmsTemplate;
 import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
 import org.springframework.orm.hibernate4.HibernateTransactionManager;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 import javax.jms.ConnectionFactory;
 import javax.jms.Queue;
 import javax.sql.DataSource;
 import java.util.Properties;
 
 /**
  * @author berinle
  */
 @Configuration
 @ComponentScan({"com.jrock.bulkwait.service"})
 @EnableTransactionManagement
 public class AppConfig {
 
     @Autowired
     private DataSource dataSource;
     @Autowired
     private ConnectionFactory connectionFactory;
 
     @Bean
     public Queue luceneQueue(){
         return new ActiveMQQueue("ERAS.Lucene.Queue");
     }
 
     @Bean
     public Producer producer(){
         Producer producer = new Producer();
         producer.setJmsTemplate(jmsTemplate());
         return producer;
     }
 
     @Bean
     public Consumer consumer(){
         return new Consumer(jmsTemplate());
     }
 
     @Bean
     public JmsTemplate jmsTemplate(){
         JmsTemplate jmsTemplate = new JmsTemplate();
         jmsTemplate.setDefaultDestination(luceneQueue());
         jmsTemplate.setConnectionFactory(connectionFactory);
         return jmsTemplate;
     }
 
     @Bean
     public AnnotationSessionFactoryBean sessionFactory(){
         AnnotationSessionFactoryBean bean = new AnnotationSessionFactoryBean();
         bean.setDataSource(dataSource);
         bean.setPackagesToScan(new String[]{"com.jrock.bulkwait.domain.**"});
         bean.setHibernateProperties(getHibernateProperties());
         return bean;
     }
 
     @Bean
     public BubbleRepository bubbleRepository(){
         return new BubbleRepositoryHibernateImpl();
     }
 
     private Properties getHibernateProperties() {
         Properties props = new Properties();
         props.setProperty("hibernate.dialect", H2Dialect.class.getName());
           //Don't use with spring as it overrides spring's tx management
           //(see http://forum.springsource.org/showthread.php?55871-Hibernate-session-in-SpringDAO-does-not-work-for-me)
 //        props.setProperty("hibernate.current_session_context_class", "thread");
 
 //        props.setProperty("hibernate.provider_class", NoCacheProvider.class.getName());
         return props;
     }
 
     @Bean
     public PlatformTransactionManager txManager() {
         return new DataSourceTransactionManager(dataSource);
 
         //Caused by: java.lang.NoClassDefFoundError: org/hibernate/engine/spi/SessionFactoryImplementor
 //        return new HibernateTransactionManager(sessionFactory().getObject()); //not working, investigate
     }
 
    @Bean
     public PlatformTransactionManager annotationDrivenTransactionManager() {
         return txManager();
    }
 
 
 
 }
