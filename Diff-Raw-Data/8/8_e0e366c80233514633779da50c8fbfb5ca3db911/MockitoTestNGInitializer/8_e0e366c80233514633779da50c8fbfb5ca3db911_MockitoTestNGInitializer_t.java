 /*
  * Copyright (C) 2012 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.mockitong;
 
 import org.mockito.Captor;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 import org.mockito.Spy;
 import org.mockito.exceptions.base.MockitoException;
 import org.mockito.internal.util.MockUtil;
 import org.testng.IInvokedMethod;
 import org.testng.IInvokedMethodListener;
 import org.testng.ITestResult;
 
 import java.lang.reflect.Field;
 
 /**
  * Mockito annotation initializer for TestNG test.
  * 
  * To enable annotation initialization you should add MockitoTestNGInitializer
  * as a listener to you TestNG test. </p> Example of usage
  * 
  * 
  * @ Listeners(MockitoTestNGInitializer.class) public class MyTestClass {
  * 
  * }
  */
 public class MockitoTestNGInitializer implements IInvokedMethodListener
 {
    private final MockUtil mockUtil = new MockUtil();
 
    /**
     * Check field for mock annotation.
     * 
     * @param field
     * @return true if field annotated with Mock or Spy or Captor.
     */
   public static boolean isFieldHaveMockAnnotation(Field field)
    {
       return field.isAnnotationPresent(Mock.class) || field.isAnnotationPresent(Spy.class)
          || field.isAnnotationPresent(Captor.class);
    }
 
    /**
     * @see org.testng.IInvokedMethodListener#beforeInvocation(org.testng.IInvokedMethod,
     *      org.testng.ITestResult)
     */
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult)
    {
       Object[] instances = method.getTestMethod().getTestClass().getInstances(false);
       for (Object object : instances)
       {
          // true - if this is a first object initialization. Will be performed  MockitoAnnotations.initMocks
          // false - Mock's already created. will reset existed.
          boolean needInitialization = true;
          Field[] fields = object.getClass().getDeclaredFields();
          for (Field field : fields)
          {
             //field is a mock
            if (isFieldHaveMockAnnotation(field))
             {
                field.setAccessible(true);
                try
                {
 
                   //field's already initialized
                   Object mock = field.get(object);
                   if (mock != null && mockUtil.isMock(mock))
                   {
                      needInitialization = false;
                      break;
                   }
                }
                catch (IllegalAccessException e)
                {
                   throw new MockitoException(e.getLocalizedMessage(), e);
                }
             }
          }
          // this is a first class initialization no mock's before
          if (needInitialization)
          {
             MockitoAnnotations.initMocks(object);
          }
       }
    }
 
    /**
     * @see org.testng.IInvokedMethodListener#afterInvocation(org.testng.IInvokedMethod,
     *      org.testng.ITestResult)
     */
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult)
    {
       if (method.isTestMethod())
       {
          Mockito.validateMockitoUsage();
          //reset mocks now
          Object[] instances = method.getTestMethod().getTestClass().getInstances(false);
          for (Object object : instances)
          {
             Field[] fields = object.getClass().getDeclaredFields();
             for (Field field : fields)
             {
               if (isFieldHaveMockAnnotation(field))
                {
                   field.setAccessible(true);
                   try
                   {
                      //field's already initialized
                      Object mock = field.get(object);
                      if (mock != null && mockUtil.isMock(mock))
                      {
                         Mockito.reset(mock);
                      }
                   }
                   catch (IllegalAccessException e)
                   {
                      throw new MockitoException(e.getLocalizedMessage(), e);
                   }
                }
             }
          }
       }
    }
 }
