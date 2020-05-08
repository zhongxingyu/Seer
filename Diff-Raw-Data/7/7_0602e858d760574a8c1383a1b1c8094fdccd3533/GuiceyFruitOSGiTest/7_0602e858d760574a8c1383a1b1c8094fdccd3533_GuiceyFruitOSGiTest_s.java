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
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Test;
 import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.equinox;
 import static org.ops4j.pax.exam.CoreOptions.options;
 import static org.ops4j.pax.exam.CoreOptions.systemProperty;
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
   }
 
   @Configuration
   public static Option[] configure() {
     return options(
         // install log service using pax runners profile abstraction (there are more profiles, like DS)
         logProfile(),
         // this is how you set the default log level when using pax logging (logProfile)
         systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),
 
         // a maven dependency. This must be a bundle already.
         // TODO
        // mavenBundle().groupId("org.guiceyfruit").artifactId("guiceyfruit-core").version("2.0-SNAPSHOT")
 
         equinox());
   }
 }
