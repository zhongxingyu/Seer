 /**
  *
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.guiceyfruit.test.osgi;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import javax.annotation.Resource;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.guiceyfruit.jsr250.Jsr250Module;
 import org.guiceyfruit.support.GuiceyFruitModule;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import org.junit.Test;
 import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.equinox;
 import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
 import static org.ops4j.pax.exam.CoreOptions.options;
 import static org.ops4j.pax.exam.CoreOptions.systemProperty;
 import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
 import org.ops4j.pax.exam.Inject;
 import org.ops4j.pax.exam.Option;
 import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.logProfile;
 import org.ops4j.pax.exam.junit.Configuration;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 
 /**
  * Tests GuiceyFruit using OSGi
  *
  * @version $Revision: 1.1 $
  */
 @RunWith(JUnit4TestRunner.class)
 public class GuiceyFruitOSGiTest {
   private static final transient Log LOG = LogFactory.getLog(GuiceyFruitOSGiTest.class);
 
   @Inject
   BundleContext bundleContext;
 
   @Test
   public void listBundles() {
     System.out.println("************ Hello from OSGi ************");
 
     for (Bundle b : bundleContext.getBundles()) {
       System.out.println("Bundle " + b.getBundleId() + " : " + b.getSymbolicName());
     }
 
     Guice.createInjector(new GuiceyFruitModule() {
     });
 
     Injector injector = Guice.createInjector(new Jsr250Module() {
       protected void configure() {
         super.configure();
 
         bind(MyBean.class);
 
         bindInstance("foo", new AnotherBean("Foo"));
         bindInstance("xyz", new AnotherBean("XYZ"));
       }
     });
 
     MyBean bean = injector.getInstance(MyBean.class);
     assertNotNull("Should have instantiated the bean", bean);
     assertNotNull("Should have injected a foo", bean.foo);
     assertNotNull("Should have injected a bar", bean.bar);
 
     assertEquals("Should have injected correct foo", "Foo", bean.foo.name);
     assertEquals("Should have injected correct bar", "XYZ", bean.bar.name);
 
   }
 
   @Configuration
   public static Option[] configure() {
     return options(
         // install log service using pax runners profile abstraction (there are more profiles, like DS)
         logProfile(),
         // this is how you set the default log level when using pax logging (logProfile)
         systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),
 
         // TODO why can't we find these from the maven pom.xml with transitive dependency?
         mavenBundle().groupId("org.guiceyfruit").artifactId("guiceyfruit-core").version(
             "2.0-SNAPSHOT"),
         mavenBundle().groupId("org.guiceyfruit").artifactId("guice-all").version("2.0-SNAPSHOT"),
         mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.aopalliance").version("1.0_1"),
 
         // TODO what OSGi bundle should we be using?
         // we can move to the one from servicemix when its released
         //mavenBundle().groupId("org.apache.servicemix.specs").artifactId("org.apache.servicemix.specs.jsr250-1.0").version("1.4.0"),
 
         wrappedBundle(
            mavenBundle().groupId("javax.annotation").artifactId("jsr250-api").version("1.0")),
 
        equinox());
   }
 
   public static class MyBean {
     @Resource
     public AnotherBean foo;
 
     public AnotherBean bar;
 
     @Resource(name = "xyz")
     public void bar(AnotherBean bar) {
       this.bar = bar;
     }
   }
 
   static class AnotherBean {
     public String name = "undefined";
 
     AnotherBean(String name) {
       this.name = name;
     }
   }
 
 }
