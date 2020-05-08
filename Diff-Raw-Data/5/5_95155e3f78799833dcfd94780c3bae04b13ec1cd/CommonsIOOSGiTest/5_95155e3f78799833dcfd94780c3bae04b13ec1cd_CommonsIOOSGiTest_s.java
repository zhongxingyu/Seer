 /*
  * Copyright 2012-2013 Steven Swor.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.sworisbreathing.sfmf4j.osgi.test.impl;
 
 import com.github.sworisbreathing.sfmf4j.osgi.test.AbstractOSGiTest;
 import java.io.IOException;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import static org.ops4j.pax.exam.CoreOptions.*;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.util.PathUtils;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author sswor
  */
 public class CommonsIOOSGiTest extends AbstractOSGiTest {
 
     @Override
     protected Option implementationOption() {
         return composite(mavenBundle("commons-io", "commons-io", "2.4")
                 ,bundle("reference:file:" + PathUtils.getBaseDir() + "/target/classes")
                 );
     }
 
     @Override
     protected void configure(ConfigurationAdmin configAdmin) {
         try {
            Configuration config = configAdmin.getConfiguration("sfmf4j-commonsio");
             Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
            properties.put("pollingInterval", "50");
             properties.put("pollingTimeUnit", "MILLISECONDS");
             config.update(properties);
         } catch (IOException ex) {
             LoggerFactory.getLogger(CommonsIOOSGiTest.class).error("Unable to get configration.  Reason: " + ex.getMessage(), ex);
         }
     }
 }
