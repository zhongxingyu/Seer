 package com.hanaden.demo.jpaspringmvc.persistence.test.hibernate;
 
 import org.apache.log4j.Logger;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author hanasaki
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {
     "/conf/spring/test/spring-BasicTestHibernate.xml"
 })
 @Transactional(value = "beanTransactionManagerBasicTest")
 @TransactionConfiguration(defaultRollback = true,
 transactionManager = "beanTransactionManagerBasicTest")
 public class CarHibernateTest extends CarBaseTestClass {
 
     private static final Logger logger = Logger.getLogger(CarHibernateTest.class);
 }
