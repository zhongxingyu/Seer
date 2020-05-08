 /**
  * 
  */
 package com.chinarewards.qqgpvn.main.test;
 
 import org.junit.Before;
 
import com.chinarewards.qqgpvn.qqapi.test.BaseTest;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 
 /**
  * 
  * 
  * @author Cyril
  * @since 0.1.0
  */
 public abstract class GuiceTest extends BaseTest {
 
 	/**
 	 * Creates an instance of Guice injector with prepared modules returned by
 	 * {@link #getModules()}.
 	 * 
 	 * @return
 	 */
 	protected Injector createInjector() {
 		Module[] modules = getModules();
 		if (modules == null) {
 			// prevent NullPointerException
 			modules = new Module[0];
 		}
 		Injector i = Guice.createInjector(modules);
 		return i;
 	}
 
 	/**
 	 * Returns a list of modules for Guice injector creation.
 	 * 
 	 * @return
 	 */
 	protected Module[] getModules() {
 		return null;
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		createInjector();
 	}
 
 }
