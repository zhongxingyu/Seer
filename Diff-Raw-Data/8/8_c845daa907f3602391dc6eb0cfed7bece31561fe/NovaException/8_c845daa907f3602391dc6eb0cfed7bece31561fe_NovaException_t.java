 /**
  * Copyright (C) 2009-2013 Dell, Inc.
  * See annotations for authorship information
  *
  * ====================================================================
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ====================================================================
  */
 
 package org.dasein.cloud.openstack.nova.os;
 
 import org.dasein.cloud.CloudErrorType;
 import org.dasein.cloud.CloudException;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class NovaException extends CloudException {
     private static final long serialVersionUID = -9188123825416917437L;
 
     static public class ExceptionItems {
         public CloudErrorType type;
         public int code;
         public String message;
         public String details;
     }
 
     //    //{"badRequest": {"message": "AddressLimitExceeded: Address quota exceeded. You cannot allocate any more addresses", "code": 400}}
     static public ExceptionItems parseException(int code, String json) {
         ExceptionItems items = new ExceptionItems();
         
         items.code = code;
         items.type = CloudErrorType.GENERAL;
         items.message = "unknown";
         items.details = "The cloud provided an error code without explanation";
         if( json != null ) {
             try {
                 JSONObject ob = new JSONObject(json);
 
                 if( code == 400 && ob.has("badRequest") ) {
                     ob = ob.getJSONObject("badRequest");
                 }
                 if( code == 413 && ob.has("overLimit") ) {
                     ob = ob.getJSONObject("overLimit");
                 }
                 if( ob.has("message") ) {
                     items.message = ob.getString("message");
                     if( items.message == null ) {
                         items.message = "unknown";
                     }
                     else {
                         items.message = items.message.trim();
                     }
                 }
                if (items.message.equals("unknown")) {
                    String[] names = JSONObject.getNames(ob);
                    for (String key : names) {
                        if (key.contains("Error")) {
                            items.message = ob.getString(key);
                        }
                    }
                }
                 if( ob.has("details") ) {
                     items.details = ob.getString("details");
                 }
                 else {
                     items.details = "[" + code + "] " + items.message;
                 }
                 String t = items.message.toLowerCase().trim();
 
                 if( code == 413 ) {
                     items.type = CloudErrorType.THROTTLING;
                 }
                 else if( t.startsWith("addresslimitexceeded") || t.startsWith("ramlimitexceeded")) {
                     items.type = CloudErrorType.QUOTA;
                 }
                 else if( t.equals("unauthorized") ) {
                     items.type = CloudErrorType.AUTHENTICATION;
                 }
                 else if( t.equals("serviceunavailable") ) {
                     items.type = CloudErrorType.CAPACITY;
                 }
                 else if( t.equals("badrequest") || t.equals("badmediatype") || t.equals("badmethod") || t.equals("notimplemented") ) {
                     items.type = CloudErrorType.COMMUNICATION;
                 }
                 else if( t.equals("overlimit") ) {
                     items.type = CloudErrorType.QUOTA;
                 }
                 else if( t.equals("servercapacityunavailable") ) {
                     items.type = CloudErrorType.CAPACITY;
                 }
                 else if( t.equals("itemnotfound") ) {
                     return null;
                 }
             }
             catch( JSONException e ) {
                 NovaOpenStack.getLogger(NovaException.class, "std").warn("parseException(): Invalid JSON in cloud response: " + json);
                 items.details = json;
             }
         }
         return items;
     }
     
     public NovaException(ExceptionItems items) {
         super(items.type, items.code, items.message, items.details);
     }
     
     public NovaException(CloudErrorType type, int code, String message, String details) {
         super(type, code, message, details);
     }
 }
