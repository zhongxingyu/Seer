 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2011, Red Hat, Inc., and individual contributors
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
 package com.george.hint;
 
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.forge.test.AbstractShellTest;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class HintPluginTest extends AbstractShellTest
 {
 
    private static ResourceBundle bundle;
 
    @Deployment
    public static JavaArchive getDeployment()
    {
       return AbstractShellTest.getDeployment().addPackages(true, HintPlugin.class.getPackage());
    }
 
    @BeforeClass
    public static void before()
    {
       bundle = ResourceBundle.getBundle("hint.messages.hint_messages", Locale.getDefault(), Thread.currentThread()
                .getContextClassLoader());
    }
 
    @Test
    public void testNoProject() throws Exception
    {
      getShell().execute("cd ..");
       getShell().execute("hint");
       Assert.assertTrue(getOutput().contains(bundle.getString("start_noproject")));
    }
 
    @Test
    public void testProjectNoPersistence() throws Exception
    {
       initializeJavaProject();
       getShell().execute("hint");
       Assert.assertTrue(getOutput().contains(bundle.getString("persistence")));
    }
 
    @Test
    public void testProjectPersistenceNoScaffold() throws Exception
    {
       initializeJavaProject();
       queueInputLines("", "");
       getShell().execute("persistence setup --provider HIBERNATE --container JBOSS_AS7");
       getShell().execute("hint");
       Assert.assertTrue(getOutput().contains(bundle.getString("scaffold")));
    }
 
    @Test
    public void testProjectPersistenceScaffold() throws Exception
    {
       initializeJavaProject();
       queueInputLines("", "");
       getShell().execute("persistence setup --provider HIBERNATE --container JBOSS_AS7");
       queueInputLines("", "", "");
       getShell().execute("scaffold setup --scaffoldType faces");
       getShell().execute("hint");
       System.out.println(getOutput());
 
       Assert.assertTrue(getOutput().contains(bundle.getString("done")));
    }
 }
