 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.javaloader.blog;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 import static org.apache.myfaces.javaloader.blog.JSFUtil.*;
 
 /**
  * @author werpu2
  * @date: 01.09.2009
  */
 

 public class Blog {
 
     String title        =   "Hello to the myfaces dynamic blogging example";
     String title1       =   "You can alter the code for this small blogging application on the fly, " +
                            "you even can add new classes on the fly and Java will pick it up";
 
     String title3 = "bla";
     String title4 = "bla2";
 
     String title5 = "test from title5";
 
 
     String firstName    = "";
     String lastName     = "";
     String topic        = "";
 
     String content      = "";
 
     
 
 
     private Log getLog() {
         return LogFactory.getLog(this.getClass());
     }
 
 
     public String addEntry2 () {
         getLog().info("adding entry2");
 
         BlogService service = (BlogService) JSFUtil.resolveVariable("javaBlogService");
 
         
         if (service == null) {
             getLog().error("service not found");
         } else {
             getLog().info("service found");
         }
 
         BlogEntry entry = new BlogEntry();
         //we now map it in the verbose way, the lean way would be to do direct introspection attribute mapping
 
         entry.setFirstName(firstName);
         entry.setLastName(lastName);
         entry.setTopic(topic);
         entry.setContent(content);
 
 
         if (service != null) {
             /*convenience method to call a method on an object dynamically
             * executeMethod and cast are static imports which encapsulates the
             * ugly stuff the java introspection provides and reduce
             * the loc down to sane levels
             *
             * note the behavior in case of calling errors
             * is changed from the default managed behavior
             * to an unmanaged behavior. This is mostly
             * the same behavior you get from scripting engines!
             * 
             */
             //service.addEntry(entry);
             //executeMethod(service, "addEntry",  entry);
             service.addEntry(entry);
         }
 
       
         
         //we stay on the same page
         return null;
     }
 
 
     public String getTitle() {
         return title;
     }
 
 
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getTitle1() {
         return title1;
     }
 
     public void setTitle1(String title1) {
         this.title1 = title1;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public String getTopic() {
         return topic;
     }
 
     public void setTopic(String topic) {
         this.topic = topic;
     }
 
     public String getContent() {
         return content;
     }
 
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public String getTitle3() {
         return "title 3 from java blog";
     }
 
     public void setTitle3(String title3) {
         this.title3 = title3;
     }
 
     public String getTitle4() {
         return title4;
     }
 
     public void setTitle4(String title4) {
         this.title4 = title4;
     }
 
 
     public String getTitle5() {
         return title5;
     }
 
     public void setTitle5(String title5) {
 
     }


 }
