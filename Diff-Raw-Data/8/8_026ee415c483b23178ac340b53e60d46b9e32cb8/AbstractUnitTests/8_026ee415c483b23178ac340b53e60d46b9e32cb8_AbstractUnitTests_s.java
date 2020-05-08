 /*
  * SafeOnline project.
  *
  * Copyright 2006-2009 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 package net.link.util.test;
 
 import com.google.common.base.Throwables;
 import com.lyndir.lhunath.lib.system.logging.Logger;
 import java.util.*;
 import javax.naming.NamingException;
 import javax.persistence.EntityManager;
 import net.link.util.j2ee.EJBUtils;
 import net.link.util.j2ee.FieldNamingStrategy;
 import net.link.util.test.j2ee.EJBTestUtils;
 import net.link.util.test.j2ee.JNDITestUtils;
 import net.link.util.test.jpa.EntityTestManager;
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.Before;
 
 
 /**
  * <h1>How to use this class</h1>
  * <p>When you create a unit test, have it extend this class (or an abstract implementation of it).</p>
  * <p>Override {@link #setUp()} to initialize data that is generic to all your tests.  This code will be executed <b>once</b> for each
  * test,
  * before it is executed.</p>
  * <p>Override {@link #setUpMocks()} to express mock expectations that are generic to all your tests.  This code will be executed
  * <b>once</b>
  * for each test  <b>AND once after each invocation of {@link #resetUpMocks()}</b> within your tests.</p>
  * <p>When you implement each test, follow these rules:</p>
  * <p>If you write test-specific mock expectations, begin by {@link EasyMock#reset(Object...)}ing the mocks you have custom expectations
  * for.
  * You should always assume that your mocks are in replay state unless you manually reset them.  After your custom expectations, but them
  * back in replay state by calling {@link #replayMocks()}.</p>
  * <p>After you've invoked the logic you want to test, call {@link #verifyAndResetUpMocks()} to make sure all mocks have been fully
  * replayed
  * in the
  * test.
  * <p/>
  * <p>
  * <i>Nov 26, 2009</i>
  * </p>
  *
  * @author lhunath
  */
 public abstract class AbstractUnitTests<T> {
 
     protected final Logger logger = Logger.get( getClass() );
 
     protected static final Random random = new Random();
 
     protected static final JNDITestUtils     jndiTestUtils     = new JNDITestUtils();
     protected static final EntityTestManager entityTestManager = new EntityTestManager();
 
     protected static final List<Object> mocks = new LinkedList<Object>();
 
     static {
         jndiTestUtils.setUp();
         jndiTestUtils.setNamingStrategy( new FieldNamingStrategy() );
     }
 
     protected       EntityManager      entityManager;
     protected       T                  testedBean;
     protected final Class<? extends T> testedClass;
 
     public AbstractUnitTests() {
 
         testedClass = null;
     }
 
     public AbstractUnitTests(Class<? extends T> testedClass) {
 
         this.testedClass = testedClass;
     }
 
     /**
      * Creates an entity manager, loads the entities from {@link #getEntities()} (unless that returns <code>null</code>) and loads all the
      * service beans from {@link #getServiceBeans()} (unless that returns <code>null</code>) into the JNDI.
      * <p/>
      * <p>
      * If you want to use a non-HSQL data source, use {@link EntityTestManager#configureMySql(String, int, String, String, String,
      * boolean)}
      * or before calling this method.
      * </p>
      */
     @Before
     public final void _setUp()
             throws Exception {
 
         logger.dbg( "=== <SET-UP> ===" );
 
         // Set up an HSQL entity manager.
         Class<?>[] entities = getEntities();
         if (entities != null)
             try {
                 entityTestManager.setUp( entities );
             }
             catch (Exception err) {
                 logger.err( err, "Couldn't set up entity manager" );
                 throw new IllegalStateException( err );
             }
         entityManager = entityTestManager.getEntityManager();
 
         // Bind our service beans into the JNDI.
         Class<?>[] serviceBeanArray = getServiceBeans();
         if (serviceBeanArray == null)
             serviceBeanArray = new Class<?>[0];
         LinkedList<Class<?>> serviceBeans = new LinkedList<Class<?>>( Arrays.asList( serviceBeanArray ) );
         if (null != testedClass && !testedClass.isInterface() && !serviceBeans.contains( testedClass ))
             serviceBeans.add( testedClass );
         for (Class<?> beanClass : serviceBeans) {
 
             Object serviceBean = EJBTestUtils.newInstance( beanClass, serviceBeanArray, entityManager );
             jndiTestUtils.bindComponent( beanClass, serviceBean );
 
             if (null != testedClass)
                 if (testedClass.isAssignableFrom( beanClass ))
                     testedBean = testedClass.cast( serviceBean );
         }
 
         setUp();
 
         logger.dbg( "=== </SET-UP> ===" );
     }
 
     /**
      * Called during unit test setUp, after setting up mocks.
      */
     protected void setUp()
             throws Exception {
 
         setUpMocks();
     }
 
     /**
      * Called during unit test setUp, and after resetUpMocks.  Call super.setUpMocks() at the END.
      */
     protected void setUpMocks()
             throws Exception {
 
         //replayMocks(); -- should be done manually before replaying in unit tests.
     }
 
     /**
      * Tears down the {@link EntityTestManager} and JNDI context.
      */
     @After
     public final void _tearDown()
             throws Exception {
 
         logger.dbg( "=== <TEAR-DOWN> ===" );
 
         tearDown();
 
         if (entityTestManager != null)
             entityTestManager.tearDown();
         if (jndiTestUtils != null)
             jndiTestUtils.tearDown();
 
         logger.dbg( "=== </TEAR-DOWN> ===" );
     }
 
     protected void tearDown()
             throws Exception {
 
         EasyMock.reset( mocks.toArray() );
     }
 
     // Utilities
 
     /**
      * @return The EJB from the JNDI that implements the given interface and is bound on that interface's JNDI_BINDING.
      */
     protected static <B> B ejb(Class<B> beanInterface) {
 
         return EJBUtils.getEJB( beanInterface );
     }
 
     /**
      * Bind the given mock object in the JNDI on its mock interface's JNDI_BINDING.
      */
     protected static <M> M createAndBindMock(Class<M> mockType) {
 
         return bindMock( createMock( mockType ) );
     }
 
     /**
      * Bind the given mock object in the JNDI on its mock interface's JNDI_BINDING.
      */
     protected static <M> M createMock(Class<M> mockType) {
 
         M mock = EasyMock.createMock( mockType );
         mocks.add( mock );
 
         return mock;
     }
 
     protected static List<Object> getMocks() {
 
         return mocks;
     }
 
     public static void reset(Void... args) {
 
         // Not allowed!  Use #resetUpMocks instead.
     }
 
     protected void resetUpMocks() {
 
         EasyMock.reset( mocks.toArray() );
 
         try {
             setUpMocks();
         }
         catch (Exception e) {
             throw Throwables.propagate( e );
         }
     }
 
     public static void replay(Void... args) {
 
         // Not allowed!  Use #replayMocks instead.
     }
 
     protected static void replayMocks() {
 
         for (Object mock : mocks)
             try {
                 EasyMock.replay( mock );
             }
             catch (IllegalStateException ignored) {
                 // Ignore when mock was already replayed
             }
     }
 
     public static void verify(Void... args) {
 
         // Not allowed!  Use #verifyAndResetUpMocks instead.
     }
 
     protected void verifyAndResetUpMocks() {
 
        EasyMock.verify( mocks.toArray() );
        resetUpMocks();
     }
 
     /**
      * Bind the given mock object in the JNDI on its mock interface's JNDI_BINDING.
      */
     protected static <M> M bindMock(M mock) {
 
         try {
             jndiTestUtils.bindComponent( mock.getClass().getInterfaces()[0], mock );
         }
         catch (NamingException e) {
             throw new RuntimeException( e );
         }
 
         return mock;
     }
 
     // Data
 
     /**
      * Implement this method if your artifact provides service beans that need to be loaded into the JNDI.
      *
      * @return All the service beans that are used by the tests.
      */
     protected Class<?>[] getServiceBeans() {
 
         return null;
     }
 
     /**
      * Implement this method if your artifact uses persistence.
      *
      * @return All the entity beans that are used by the tests.
      */
     protected Class<?>[] getEntities() {
 
         return null;
     }
 }
