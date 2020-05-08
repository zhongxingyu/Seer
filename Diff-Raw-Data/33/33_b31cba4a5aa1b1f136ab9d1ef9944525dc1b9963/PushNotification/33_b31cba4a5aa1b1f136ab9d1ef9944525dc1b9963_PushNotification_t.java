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
 
 import java.util.Map;
 
 public class PushNotification extends PushConfiguration {
     /** The PushNotification attribute name for subject. */
     public static String SUBJECT = "subject";
 
     /** The PushNotification attribute name for detail. */
     public static String DETAIL = "detail";
 
     protected PushNotification() {
         super();
     }
 
     /**
      * <p>
     *     Constructs a new PushNotification with the specified <code>subject</code>.
      * </p>
      *
      * @param      subject
     *                 The subject of the new PushNotification to be constructed.
      * @see        #PushNotification(String, String)
      * @see        #PushNotification(Map)
      */
     public PushNotification(String subject)  {
         getAttributes().put(SUBJECT, subject);
     }
 
     /**
      * <p>
     *     Constructs a new PushNotification with the specified <code>subject</code> and <code>detail</code>.
      * </p>
      *
      * @param      subject
     *                 The subject of the new PushNotification to be constructed.
      * @param      detail
     *                 The detail of the new PushNotification to be constructed.
      * @see        #PushNotification(String)
      * @see        #PushNotification(Map)
      */
     public PushNotification(String subject, String detail)  {
         getAttributes().put(SUBJECT, (String) subject);
         getAttributes().put(DETAIL, detail);
     }
 
     /**
      * <p>
     *     Constructs a new PushNotification with the specified <code>attributes</code>.
      * </p>
      *
      * @param      attributes
     *                 The attributes of the new PushNotification to be constructed.
      * @see        #PushNotification(String)
      * @see        #PushNotification(String, String)
      */
     public PushNotification(Map<String, Object> attributes)  {
         super(attributes);
     }
 
     /**
      * <p>
      *     Gets the subject of this PushNotification.
      * </p>
      *
      * @return     The subject.
      */
     public String getSubject() {
         return (String) getAttributes().get(SUBJECT);
     }
 
     /**
      * <p>
      *     Gets the detail of this PushNotification.
      * </p>
      *
      * @return     The detail.
      */
     public String getDetail() {
         return (String) getAttributes().get(DETAIL);
     }
 }
