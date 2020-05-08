 /*
  * Copyright (c) 2012 TouK
  * All rights reserved
  */
 package pl.touk.ormtest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.h2.engine.Mode;
 import org.junit.rules.MethodRule;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.Statement;
 import org.springframework.core.annotation.AnnotationUtils;
 import org.springframework.jdbc.datasource.DataSourceTransactionManager;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.transaction.TransactionStatus;
 
 import javax.sql.DataSource;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 /**
  * Base abstract class for JUnit 4.8+ testing of Jdbc and Ibatis Spring-based DAOs.
  *
  * @author <a href="mailto:msk@touk.pl">Michał Sokołowski</a>
  */
 abstract public class SpringTxMethodRule implements MethodRule {
 
     private static final Log log = LogFactory.getLog(SpringTxMethodRule.class);
 
     protected final static ConcurrentMap<Thread, TransactionStatus> txStatuses =
             new ConcurrentHashMap<Thread, TransactionStatus>();
     protected final static ConcurrentMap<Thread, DataSourceTransactionManager> txManagers =
             new ConcurrentHashMap<Thread, DataSourceTransactionManager>();
     protected final static ConcurrentMap<String, Set<Thread>> threadsPerTestClass =
             new ConcurrentHashMap<String, Set<Thread>>();
 
     private final String h2ModeOption;
 
     public SpringTxMethodRule() {
         this(null);
     }
 
     public SpringTxMethodRule(String h2Mode) {
         String invokerClassName = findInvokingTestClass().getName();
         threadsPerTestClass.putIfAbsent(invokerClassName, new HashSet<Thread>());
         threadsPerTestClass.get(invokerClassName).add(Thread.currentThread());
         this.h2ModeOption = getH2ModeOption(h2Mode);
     }
 
     public static Class findInvokingTestClass() {
         StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
         int indexOfStackTraceElementCorrespondingToCurrentMethod = 1;
         for (int i = indexOfStackTraceElementCorrespondingToCurrentMethod; i < stackTrace.length; i++) {
             StackTraceElement el = stackTrace[i];
             Class classOfElement = getStackTraceElementClass(el);
             if (!SpringTxMethodRule.class.isAssignableFrom(classOfElement)) {
                 return classOfElement;
             }
         }
         throw new RuntimeException("first test class name not found");
     }
 
     public static Set<Thread> getThreads(Class testClassOrSuiteClass) {
         Set<Thread> threads = new HashSet<Thread>();
         for (Class c: getTestClasses(testClassOrSuiteClass)) {
             if (threadsPerTestClass.get(c.getName()) != null) {
                 threads.addAll(threadsPerTestClass.get(c.getName()));
             }
         }
         return threads;
     }
 
     private static Set<Class> getTestClasses(Class testClassOrSuiteClass) {
         HashSet<Class> testClasses = new HashSet<Class>();
         RunWith annotation = AnnotationUtils.findAnnotation(testClassOrSuiteClass, RunWith.class);
         if (annotation != null && Suite.class.isAssignableFrom(annotation.value())) {
             Suite.SuiteClasses suiteClasses =
                     AnnotationUtils.findAnnotation(testClassOrSuiteClass, Suite.SuiteClasses.class);
             if (suiteClasses != null) {
                 if (suiteClasses.value() != null && suiteClasses.value().length > 0) {
                     testClasses.addAll(Arrays.asList(suiteClasses.value()));
                 }
                 return testClasses;
             }
         }
         testClasses.add(testClassOrSuiteClass);
         return testClasses;
     }
 
     private static Class getStackTraceElementClass(StackTraceElement el) {
         try {
             return Class.forName(el.getClassName());
         } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Can be overridden in subclasses and should return a data source. The default implementation of this method
      * returns a data source for in-memory H2 database. This method retuns different data sources if it is invoked
      * in different threads (database name contains hash code of the current thread).
      *
      * @return data source to be used during tests
      */
     protected DataSource dataSource() {
         DriverManagerDataSource ds = new DriverManagerDataSource();
 
         ds.setDriverClassName("org.h2.Driver");
         // If tests are run in parallel, then each thread should have its own database:
         ds.setUrl("jdbc:h2:mem:db" + Thread.currentThread().hashCode() +
                 ";DB_CLOSE_DELAY=-1;AUTOCOMMIT=OFF" + h2ModeOption);
         ds.setUsername("sa");
         ds.setPassword("");
 
         log.debug(getThreadPrefix() + "creating datasource to " + ds.getUrl());
 
         return ds;
     }
 
     private String getH2ModeOption(String h2Mode) {
         String h2ModeOption;
         if (h2Mode != null && h2Mode.length() > 0) {
             if (h2Mode.indexOf(';') == -1) {
                 h2ModeOption = ";MODE=" + h2Mode;
                if (Mode.getInstance(h2Mode) == null) {
                    log.warn("h2 compatibility mode " + h2Mode + " not found");
                }
             } else {
                 throw new IllegalArgumentException("h2Mode must not contain ';' character");
             }
         } else {
             h2ModeOption = "";
         }
         return h2ModeOption;
     }
 
     abstract protected void ensureTemplateInitialized();
 
     private void doBeginTransaction() {
         ensureTemplateInitialized();
         if (txStatuses.get(Thread.currentThread()) == null) {
             txStatuses.put(Thread.currentThread(), txManagers.get(Thread.currentThread()).getTransaction(null));
         } else {
             throw new IllegalStateException("transaction already started");
         }
     }
 
     /**
      * Rollbacks the transaction started in {@link #doBeginTransaction()}.
      */
     private void doRollBackTransaction() {
         if (txStatuses.get(Thread.currentThread()) != null) {
             txManagers.get(Thread.currentThread()).rollback(txStatuses.get(Thread.currentThread()));
             txStatuses.remove(Thread.currentThread());
         } else {
             throw new IllegalStateException("there is no transaction to rollback");
         }
     }
 
     /**
      * Commits the transaction started in {@link #doBeginTransaction()}.
      */
     private void doCommitTransaction() {
         if (txStatuses.get(Thread.currentThread()) != null) {
             txManagers.get(Thread.currentThread()).commit(txStatuses.get(Thread.currentThread()));
             txStatuses.remove(Thread.currentThread());
         } else {
             throw new IllegalStateException("there is no transaction to commit");
         }
     }
 
     public final Statement apply(final Statement base, FrameworkMethod method, Object target) {
         return new Statement() {
             public void evaluate() throws Throwable {
                 log.debug(getThreadPrefix() + "method rule begins");
                 beginTransaction();
                 try {
                     base.evaluate();
                 } finally {
                     rollBackTransaction();
                 }
                 log.debug(getThreadPrefix() + "method rule ends");
             }
         };
     }
 
     private void beginTransaction() {
         try {
             doBeginTransaction();
         } catch (RuntimeException e) {
             String s = "failed to begin a transaction";
             log.error(s, e);
             throw new RuntimeException(s, e);
         }
     }
 
     private void commitTransaction() {
         try {
             doCommitTransaction();
         } catch (RuntimeException e) {
             String s = "failed to commit the transaction";
             log.error(s, e);
             throw new RuntimeException(s, e);
         }
     }
 
     private void rollBackTransaction() {
         try {
             doRollBackTransaction();
         } catch (RuntimeException e) {
             String s = "failed to rollback transaction";
             log.error(s, e);
             throw new RuntimeException(s, e);
         }
     }
 
     public void rollBackTransactionAndBeginNewOne() {
         rollBackTransaction();
         beginTransaction();
     }
 
     public void commitTransactionAndBeginNewOne() {
         commitTransaction();
         beginTransaction();
     }
 
     private String getThreadPrefix() {
         return "thread " + Thread.currentThread().hashCode() + ": ";
     }
 }
