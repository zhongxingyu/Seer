 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  *
  */
 package org.icepush;
 
import java.util.HashMap;
 import java.util.Map;
 
 public class PushNotification extends PushConfiguration {
     public static String SUBJECT = "subject";
     public static String DETAIL = "detail";
 
     public PushNotification(String subject)  {
         getAttributes().put(SUBJECT, subject);
     }
     
     public PushNotification(String subject, String detail)  {
         getAttributes().put(SUBJECT, (String) subject);
         getAttributes().put(DETAIL, detail);
     }
 
     public PushNotification(Map<String, Object> attributes)  {
         super(attributes);
     }
 
     public String getSubject() {
         return (String) getAttributes().get(SUBJECT);
     }
 
     public String getDetail() {
         return (String) getAttributes().get(DETAIL);
     }
 }
