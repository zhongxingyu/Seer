 /*
  * Copyright 2012 the original author or authors.
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
 
 package org.brekka.stillingar.test.intg;
 
 import org.brekka.stillingar.annotations.ConfigurationListener;
 import org.brekka.stillingar.annotations.Configured;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.context.ApplicationContext;
 
 @Configured
 public class ContextBean implements InitializingBean  {
     
     
    @Configured("/c:Configuration/c:Context/beans:beans")
    private ApplicationContext applicationContext;
    
    @Configured("/c:Configuration/c:Context/beans:beans")
    public void setContext(ApplicationContext applicationContext) {
        System.out.println(applicationContext);
    }
     
     public ApplicationContext getContext() {
         return null;
     }
    
     public void afterPropertiesSet() throws Exception {
         System.out.println(applicationContext);
     }
     
     @ConfigurationListener
    public void configure(@Configured("/c:Configuration/c:Context/beans:beans") ApplicationContext applicationContext) {
         System.out.println(applicationContext);
     }
 }
