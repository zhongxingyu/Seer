 package nu.danielsundberg.persistence;
 /* 
  *   ___ ___    _____  ________  ________   ________  _________  ____  __.
  *  /   |   \  /  _  \ \______ \ \______ \  \_____  \ \_   ___ \|    |/ _|
  * /    ~    \/  /_\  \ |    |  \ |    |  \  /   |   \/    \  \/|      <  
  * \    Y    /    |    \|    `   \|    `   \/    |    \     \___|    |  \ 
  *  \___|_  /\____|__  /_______  /_______  /\_______  /\______  /____|__ \
  *        \/         \/        \/        \/         \/        \/        \/
  * 
  * Copyright 2012 Daniel Sundberg
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.runner.RunWith;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.util.Properties;
 
 /**
  * Abstract testclass for Haddock.
  */
 @RunWith(MockitoJUnitRunner.class)
public abstract class AbstractPersistenceTest {
 
     private static EntityManagerFactory entityManagerFactory;
     private static Connection connection;
     protected static EntityManager entityManager;
 
     @BeforeClass
     public static void setUpDataBase() throws Exception {
         Class.forName("org.hsqldb.jdbcDriver");
         connection = DriverManager.getConnection("jdbc:hsqldb:mem:unit-testing-jpa", "sa", "");
         Properties testPersistenceProperties = new Properties();
         testPersistenceProperties.put("haddock.connection.url", "jdbc:hsqldb:mem:unit-testing-jpa");
         testPersistenceProperties.put("haddock.connection.driver_class", "org.hsqldb.jdbcDriver");
         testPersistenceProperties.put("haddock.dialect", "nu.danielsundberg.persistence.haddock.dialect.HSQLDialect");
         testPersistenceProperties.put("haddock.hbm2ddl.auto", "create-drop");
         testPersistenceProperties.put("haddock.connection.username", "sa");
         testPersistenceProperties.put("haddock.connection.password", "");
         testPersistenceProperties.put("haddock.show_sql", "false");
         testPersistenceProperties.put("haddock.cache.provider_class","nu.danielsundberg.persistence.haddock.cache.NoCacheProvider");
         testPersistenceProperties.put("haddock.cache.use_second_level_cache","false");
         testPersistenceProperties.put("haddock.cache.use_query_cache","false");
         testPersistenceProperties.put("haddock.max_fetch_depth","3");
         testPersistenceProperties.put("haddock.default_batch_fetch_size","8");
         testPersistenceProperties.put("haddock.query.substitutions","true Y, false N");
         testPersistenceProperties.put("haddock.order_inserts","true");
         testPersistenceProperties.put("haddock.order_updates","true");
         testPersistenceProperties.put("haddock.bytecode.use_reflection_optimizer","false");
         testPersistenceProperties.put("haddock.use_sql_comments","true");
         entityManagerFactory = Persistence.createEntityManagerFactory("haddockTest", testPersistenceProperties);
         entityManager = entityManagerFactory.createEntityManager();
     }
 
     @AfterClass
     public static void tearDownDataBase() throws Exception {
         if (entityManager != null) {
             entityManager.close();
         }
         if (entityManagerFactory != null) {
             entityManagerFactory.close();
         }
         try {
             connection.createStatement().execute("SHUTDOWN");
         } catch (Exception ex) {}
     }
 
 }
