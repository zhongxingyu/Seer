 /*
  * Copyright (c) 2010, Eric McIntyre
  * All rights reserved.
  * 
  * This software is under the BSD license.
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.riversoforion.acheron.test.mock;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.createNiceMock;
 import static org.easymock.EasyMock.createStrictMock;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.easymock.EasyMock;
 
 /**
  * Provides an abstract base class for JUnit or TestNG test cases that want a
  * convenient interface to EasyMock mock objects. It implements a mock object
  * registry that makes calling EasyMock control methods like
  * {@link EasyMock#replay(Object...) replay} much easier, especially if your
  * test uses several mock collaborators.
  * 
  * @author macdaddy
 * @deprecated Functionality is built in to EasyMock 3 with the
 *             {@link org.easymock.EasyMockSupport} class
  */
@Deprecated
 public abstract class BaseEasyMockTestCase {
 
     private List<Object> mockObjectRegistry = new ArrayList<Object>();
 
     /**
      * Creates a mock object of the specified type with the default niceness
      * level.
      * 
      * @param <T>
      * @param clazz
      *            The class to mock
      * @return A mock object for the specified class
      */
     protected <T> T mockOf(Class<T> clazz) {
 
         return registerMockObject(createMock(clazz));
     }
 
     /**
      * Creates a nice mock object of the specified type.
      * 
      * @param <T>
      * @param clazz
      *            The class to mock
      * @return A mock object for the specified class
      */
     protected <T> T niceMockOf(Class<T> clazz) {
 
         return registerMockObject(createNiceMock(clazz));
     }
 
     /**
      * Creates a strict mock object of the specified type.
      * 
      * @param <T>
      * @param clazz
      *            The class to mock
      * @return A mock object for the specified class
      */
     protected <T> T strictMockOf(Class<T> clazz) {
 
         return registerMockObject(createStrictMock(clazz));
     }
 
     /**
      * Invokes {@link EasyMock#replay(Object...) replay} with all registered
      * mock objects.
      */
     protected void replay() {
 
         EasyMock.replay(this.mockObjectRegistry.toArray());
     }
 
     /**
      * Invokes {@link EasyMock#verify(Object...) verify} with all registered
      * mock objects.
      */
     protected void verify() {
 
         EasyMock.verify(this.mockObjectRegistry.toArray());
     }
 
     /**
      * Invokes {@link EasyMock#reset(Object...) reset} with all registered
      * mock objects.
      */
     protected void reset() {
 
         EasyMock.reset(this.mockObjectRegistry.toArray());
     }
 
     private <T> T registerMockObject(T mock) {
 
         this.mockObjectRegistry.add(mock);
         return mock;
     }
 }
