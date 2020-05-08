 /*
  * This is a utility project for wide range of applications
  * 
  * Copyright (C) 8  Imran M Yousuf (imyousuf@smartitengineering.com)
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  10-1  USA
  */
 package com.smartitengineering.util.bean.spring;
 
import com.smartitengineering.util.bean.BeanFactoryRegistrar;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * Unit test for simple App.
  */
 public class PropertiesLocatorConfigurerTest
     extends TestCase {
 
     private ApplicationContext applicationContext;
     private TestBean bean;
 
     /**
      * Create the test case
      *
      * @param testName name of the test case
      */
     public PropertiesLocatorConfigurerTest(String testName) {
         super(testName);
     }
 
     /**
      * @return the suite of tests being tested
      */
     public static Test suite() {
         return new TestSuite(PropertiesLocatorConfigurerTest.class);
     }
 
     public void testBeanClasspath() {
         getBean();
         assertEquals("default", bean.getPropertyDefault());
         assertEquals("classpath", bean.getPropertyClassPath());
         assertEquals("current dir...sample", bean.getPropertyCurrentDir());
         assertEquals("user home dir...sample", bean.getPropertyUserHome());
     }
 
     public void testBeanCurrentDir() {
         Properties properties = new Properties();
         properties.setProperty("testbean.current_dir", "current dir");
         File file = new File(System.getProperty("user.dir"),
             "test-config.properties");
         try {
             FileOutputStream fos = new FileOutputStream(file);
             properties.store(fos, "");
             fos.close();
         }
         catch (IOException ex) {
             fail(ex.getMessage());
         }
         getBean();
         assertEquals("default", bean.getPropertyDefault());
         assertEquals("classpath", bean.getPropertyClassPath());
         assertEquals("current dir", bean.getPropertyCurrentDir());
         assertEquals("user home dir...sample", bean.getPropertyUserHome());
         file.delete();
     }
 
     public void testBeanHomeDir() {
         Properties properties = new Properties();
         properties.setProperty("testbean.user_home", "user home dir");
         File file = new File(System.getProperty("user.home"),
             "test-config.properties");
         try {
             FileOutputStream fos = new FileOutputStream(file);
             properties.store(fos, "");
             fos.close();
         }
         catch (IOException ex) {
             fail(ex.getMessage());
         }
         getBean();
         assertEquals("default", bean.getPropertyDefault());
         assertEquals("classpath", bean.getPropertyClassPath());
         assertEquals("current dir...sample", bean.getPropertyCurrentDir());
         assertEquals("user home dir", bean.getPropertyUserHome());
     }
 
     public void testAll() {
         Properties properties = new Properties();
         properties.setProperty("testbean.current_dir", "current dir");
         File fileCurrentDir = new File(System.getProperty("user.dir"),
             "test-config.properties");
         try {
             FileOutputStream fos = new FileOutputStream(fileCurrentDir);
             properties.store(fos, "");
             fos.close();
         }
         catch (IOException ex) {
             fail(ex.getMessage());
         }
         properties = new Properties();
         properties.setProperty("testbean.current_dir", "current dir again");
         properties.setProperty("testbean.user_home", "user home dir");
         File fileUserHome = new File(System.getProperty("user.home"),
             "test-config.properties");
         try {
             FileOutputStream fos = new FileOutputStream(fileUserHome);
             properties.store(fos, "");
             fos.close();
         }
         catch (IOException ex) {
             fail(ex.getMessage());
         }
         getBean();
         assertEquals("default", bean.getPropertyDefault());
         assertEquals("classpath", bean.getPropertyClassPath());
         assertEquals("current dir again", bean.getPropertyCurrentDir());
         assertEquals("user home dir", bean.getPropertyUserHome());
         fileCurrentDir.delete();
         fileUserHome.delete();
     }
 
     public void testBean2() {
         getBean2();
         assertTrue(bean.getPropertyDefault().endsWith("2"));
         assertTrue(bean.getPropertyClassPath().endsWith("2"));
         assertTrue(bean.getPropertyCurrentDir().endsWith("2"));
         assertTrue(bean.getPropertyUserHome().endsWith("2"));
     }
 
     public void testDefaultResourceSuffix() {
         getBean3();
         assertEquals("default", bean.getPropertyDefault());
         assertEquals("classpath", bean.getPropertyClassPath());
         assertEquals("current dir...sample", bean.getPropertyCurrentDir());
         assertEquals("user home dir...sample", bean.getPropertyUserHome());
     }
 
     public void testContextAndPath() {
         getBean3(true);
         assertEquals("default", bean.getPropertyDefault());
         assertEquals("classpath", bean.getPropertyClassPath());
         assertEquals("current dir 3", bean.getPropertyCurrentDir());
         assertEquals("user home dir 3", bean.getPropertyUserHome());
     }
     
     public void testAggregator() {
         TestBeanDummyAggregatorLevel1 aggregator = new TestBeanDummyAggregatorLevel1();
         getBean3();
         BeanFactoryRegistrar.aggregate(aggregator);
         assertNull(aggregator.getTestBeanN());
         assertNull(aggregator.getTestBeanNull());
         assertEquals(applicationContext.getBean("testBean"), aggregator.getTestBean1());
         assertEquals(applicationContext.getBean("testBean2"), aggregator.getTestBean2());
         assertEquals(applicationContext.getBean("testBean3"), aggregator.getTestBean3());
     }
 
     private void getBean()
         throws BeansException {
         initPaths(new StringBuilder());
         applicationContext =
             new ClassPathXmlApplicationContext("test-app-context.xml");
         bean =
             (TestBean) applicationContext.getBean("testBean");
     }
 
     private void getBean2()
         throws BeansException {
         StringBuilder paths = new StringBuilder();
         initPaths(paths);
         applicationContext =
             new ClassPathXmlApplicationContext("test-app-context.xml");
         bean =
             (TestBean) applicationContext.getBean("testBean2");
     }
 
     private void getBean3()
         throws BeansException {
         getBean3(false);
     }
 
     private void getBean3(boolean createBean3Rsrc) {
         StringBuilder paths = new StringBuilder();
         initPaths(paths, createBean3Rsrc);
         applicationContext =
             new ClassPathXmlApplicationContext("test-app-context.xml");
         bean =
             (TestBean) applicationContext.getBean("testBean3");
     }
 
     private void initPaths(StringBuilder paths) {
         initPaths(paths, false);
     }
 
     private void initPaths(StringBuilder paths,
                            boolean createBean3Rsrc) {
         String path = "./target/a/";
         File dir = new File(path);
         if (!dir.exists()) {
             dir.mkdirs();
         }
         Properties properties = new Properties();
         properties.setProperty("testbean.default", "default 2");
         File fileCurrentDir = new File(dir, "test-config-custom.properties");
         try {
             FileOutputStream fos = new FileOutputStream(fileCurrentDir);
             properties.store(fos, "");
             fos.close();
         }
         catch (IOException ex) {
             fail(ex.getMessage());
         }
         paths.append(path);
         path = "./target/b/";
         dir = new File(path);
         if (!dir.exists()) {
             dir.mkdirs();
         }
         properties = new Properties();
         properties.setProperty("testbean.cp", "cp 2");
         fileCurrentDir = new File(dir, "test-config-custom.properties");
         try {
             FileOutputStream fos = new FileOutputStream(fileCurrentDir);
             properties.store(fos, "");
             fos.close();
         }
         catch (IOException ex) {
             fail(ex.getMessage());
         }
         paths.append(',');
         paths.append(path);
         path = "./target/c/";
         dir = new File(path);
         if (!dir.exists()) {
             dir.mkdirs();
         }
         properties = new Properties();
         properties.setProperty("testbean.current_dir", "current dir");
         fileCurrentDir = new File(dir, "test-config-custom.properties");
         try {
             FileOutputStream fos = new FileOutputStream(fileCurrentDir);
             properties.store(fos, "");
             fos.close();
         }
         catch (IOException ex) {
             fail(ex.getMessage());
         }
         paths.append(',');
         paths.append(path);
         path = "./target/d/";
         dir = new File(path);
         if (!dir.exists()) {
             dir.mkdirs();
         }
         properties = new Properties();
         properties.setProperty("testbean.current_dir", "current dir 2");
         properties.setProperty("testbean.user_home", "user home dir 2");
         fileCurrentDir = new File(dir, "test-config-custom.properties");
         try {
             FileOutputStream fos = new FileOutputStream(fileCurrentDir);
             properties.store(fos, "");
             fos.close();
         }
         catch (IOException ex) {
             fail(ex.getMessage());
         }
         if (createBean3Rsrc) {
             properties = new Properties();
             properties.setProperty("testbean.current_dir", "current dir 3");
             properties.setProperty("testbean.user_home", "user home dir 3");
             dir = new File(dir, "custom-context/custom-path/");
             dir.mkdirs();
             fileCurrentDir = new File(dir, "test-config.properties");
             try {
                 FileOutputStream fos = new FileOutputStream(fileCurrentDir);
                 properties.store(fos, "");
                 fos.close();
             }
             catch (IOException ex) {
                 fail(ex.getMessage());
             }
         }
         paths.append(',');
         paths.append(path);
     }
 }
