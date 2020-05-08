 /*
   * JBoss, Home of Professional Open Source
   * Copyright 2005, JBoss Inc., and individual contributors as indicated
   * by the @authors tag. See the copyright.txt in the distribution for a
   * full listing of individual contributors.
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
 package org.jboss.test.microcontainer.test;
 
 import junit.framework.Test;
 
 import org.jboss.aop.microcontainer.junit.AOPMicrocontainerTest;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.test.microcontainer.support.InterceptorWithDependency;
 import org.jboss.test.microcontainer.support.MarkerInterface;
 import org.jboss.test.microcontainer.support.SimpleBean;
 
 public class IntroductionDependencyTestCase extends AOPMicrocontainerTest
 {
    public static Test suite()
    {
      return suite(IntroductionDependencyTestCase.class);
    }
    
    public IntroductionDependencyTestCase(String name)
    {
       super(name);
    }
    
    public void testInterceptorWithDependencyCorrectOrder() throws Exception
    {
       deploy("IntroductionDependencyTestCaseNotAutomatic0.xml");
       try
       {
          SimpleBean dependency = (SimpleBean) getBean("Dependency");
          assertNotNull(dependency);
          deploy("IntroductionDependencyTestCaseNotAutomatic1.xml");
          try
          {
             validate();
             checkPlainBean();
             checkInterceptedBean(dependency);
          }
          finally
          {
             undeploy("IntroductionDependencyTestCaseNotAutomatic1.xml");
          }
       }
       finally
       {
          undeploy("IntroductionDependencyTestCaseNotAutomatic0.xml");
       }
    }
    
    public void testInterceptorWithDependencyWrongOrder() throws Exception
    {
       deploy("IntroductionDependencyTestCaseNotAutomatic1.xml");
       try
       {
          SimpleBean bean;
          try
          {
             bean = (SimpleBean) getBean("Intercepted");
             fail("'Interceped' should not be installed yet");
          }
          catch (IllegalStateException expected)
          {
          }
          
          checkPlainBean();
          
          bean = (SimpleBean) getBean("Intercepted", ControllerState.DESCRIBED);
          assertNull("This should not be deployed until the interceptor is", bean);
          
          deploy("IntroductionDependencyTestCaseNotAutomatic0.xml");
          try
          {
             validate();
             SimpleBean dependency = (SimpleBean) getBean("Dependency");
             checkInterceptedBean(dependency);
          }
          finally
          {
             undeploy("IntroductionDependencyTestCaseNotAutomatic0.xml");
          }
          checkPlainBean();
       }
       finally
       {
          undeploy("IntroductionDependencyTestCaseNotAutomatic1.xml");
       }
    }
    
    public void testInterceptorWithDependencyRedeploy() throws Exception
    {
       deploy("IntroductionDependencyTestCaseNotAutomatic1.xml");
       try
       {
          try
          {
             getBean("Intercepted");
             fail("Bean should not be installed until the dependency is");
          }
          catch (IllegalStateException expected)
          {
             KernelControllerContext context = getControllerContext("Intercepted", ControllerState.DESCRIBED);
             assertNotNull(context);
          }
          checkPlainBean();
 
          deploy("IntroductionDependencyTestCaseNotAutomatic0.xml");
          try
          {
             validate();
             SimpleBean dependency = (SimpleBean) getBean("Dependency");
             checkInterceptedBean(dependency);
          }
          finally
          {
             undeploy("IntroductionDependencyTestCaseNotAutomatic0.xml");
          }
          
          try
          {
             getBean("Intercepted");
             fail("Bean should not be installed after the dependency is undeployed");
          }
          catch (IllegalStateException expected)
          {
             KernelControllerContext context = getControllerContext("Intercepted", ControllerState.DESCRIBED);
             assertNotNull(context);
          }
          checkPlainBean();
          
          deploy("IntroductionDependencyTestCaseNotAutomatic0.xml");
          try
          {
             validate();
             SimpleBean dependency = (SimpleBean) getBean("Dependency");
             checkInterceptedBean(dependency);
             checkPlainBean();
          }
          finally
          {
             undeploy("IntroductionDependencyTestCaseNotAutomatic0.xml");
          }
          checkPlainBean();
       }
       finally
       {
          undeploy("IntroductionDependencyTestCaseNotAutomatic1.xml");
       }
    }
    
       
    public void testInterceptorWithDependencyRedeploy2() throws Exception
    {
       deploy("IntroductionDependencyTestCaseNotAutomatic0.xml");
       try
       {
          SimpleBean dependency = (SimpleBean) getBean("Dependency");
          assertNotNull(dependency);
          deploy("IntroductionDependencyTestCaseNotAutomatic1.xml");
          try
          {
             validate();
             checkInterceptedBean(dependency);
             checkPlainBean();
          }
          finally
          {
             undeploy("IntroductionDependencyTestCaseNotAutomatic1.xml");
          }
 
          dependency = (SimpleBean) getBean("Dependency");
          assertNotNull(dependency);
          deploy("IntroductionDependencyTestCaseNotAutomatic1.xml");
          try
          {
             checkInterceptedBean(dependency);
             checkPlainBean();
          }
          finally
          {
             undeploy("IntroductionDependencyTestCaseNotAutomatic1.xml");
          }
       }
       finally
       {
          undeploy("IntroductionDependencyTestCaseNotAutomatic0.xml");
       }
    }
    
    private void checkInterceptedBean(Object dependency)
    {
       SimpleBean bean = (SimpleBean) getBean("Intercepted");
       assertNotNull(bean);
       assertTrue(MarkerInterface.class.isAssignableFrom(bean.getClass()));
       InterceptorWithDependency.intercepted = null;
       bean.someMethod();
       assertTrue(dependency == InterceptorWithDependency.intercepted);
    }
 
    private void checkPlainBean()
    {
       SimpleBean bean = (SimpleBean) getBean("Plain");
       assertNotNull(bean);
       assertFalse(MarkerInterface.class.isAssignableFrom(bean.getClass()));
       InterceptorWithDependency.intercepted = null;
       bean.someMethod();
       assertNull(InterceptorWithDependency.intercepted);
    }
    protected void configureLogging()
    {
       //enableTrace("org.jboss.kernel.plugins.dependency");
    }
 }
