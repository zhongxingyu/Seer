 /*
 * Copyright 2010 the original author or authors.
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
 package org.springframework.context.groovy;
 
 import junit.framework.TestCase;
 
 import org.springframework.context.ApplicationContext;
 
 public class BeanBuilderApplicationContextTests extends TestCase {
     
    public void testLodingMultipleConfigFiles() {
         String[] resources = new String[] {            
             "org/springframework/context/groovy/applicationContext2.groovy",
             "org/springframework/context/groovy/applicationContext.groovy" };
         ApplicationContext ctx = (ApplicationContext) new BeanBuilderApplicationContext(resources);
 
         Object framework = ctx.getBean("framework");
         assertNotNull("could not find framework bean", framework);
         assertEquals("Grails", framework);
 
         Object company = ctx.getBean("company");
         assertNotNull("could not find company bean", company);
         assertEquals("SpringSource", company);
     }
 
     public void testLoadingConfigFile() {
         ApplicationContext ctx = (ApplicationContext) new BeanBuilderApplicationContext(
             "org/springframework/context/groovy/applicationContext.groovy");
 
         Object framework = ctx.getBean("framework");
         assertNotNull("could not find framework bean", framework);
         assertEquals("Grails", framework);
     }
 
     public void testLoadingConfigFileWithFileReference() {
         ApplicationContext ctx = (ApplicationContext) new BeanBuilderApplicationContext(
             "file:src/test/resources/org/springframework/context/groovy/applicationContext.groovy");
 
         Object framework = ctx.getBean("framework");
         assertNotNull("could not find framework bean", framework);
         assertEquals("Grails", framework);
     }
 
    public void testLodingMultipleConfigFilesWithFileReferences() {
         String[] resources = new String[] {
             "file:src/test/resources/org/springframework/context/groovy/applicationContext2.groovy",
             "file:src/test/resources/org/springframework/context/groovy/applicationContext.groovy" };
         ApplicationContext ctx = (ApplicationContext) new BeanBuilderApplicationContext(resources);
 
         Object framework = ctx.getBean("framework");
         assertNotNull("could not find framework bean", framework);
         assertEquals("Grails", framework);
 
         Object company = ctx.getBean("company");
         assertNotNull("could not find company bean", company);
         assertEquals("SpringSource", company);
     }
 }
