 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane2.server.config;
 
 import com.janrain.servlet.InvalidRequestException;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.message.MessageField;
 import com.janrain.oauth2.OAuth2;
 import com.janrain.oauth2.ValidationException;
 
 import java.io.Externalizable;
 import java.util.*;
 
 /**
  * @author Tom Raney
  */
 public class Client extends User implements Externalizable {
 
    private static final long serialVersionUID = -7216424765846672928L;

     /**
      * Empty default constructor for AWS to use
      */
     public Client() {}
 
     public Client(String client_id, String client_secret, String source_url, String redirect_uri) throws SimpleDBException {
         Map<String,String> d = new LinkedHashMap<String, String>();
         d.put(Field.USER.getFieldName(), client_id);
         d.put(Field.PWDHASH.getFieldName(), client_secret);
         d.put(ClientField.SOURCE_URL.getFieldName(), source_url);
         d.put(ClientField.REDIRECT_URI.getFieldName(), redirect_uri);
         super.init(client_id, d);
     }
 
     /**
      * Copy constructor (to help with migration).
      */
     public Client(Map<String,String> data) throws SimpleDBException {
         super.init(data.get(Field.USER.getFieldName()), data);
     }
 
     @Override
     public Set<? extends MessageField> getFields() {
         Set<MessageField> fields = new HashSet<MessageField>();
         fields.addAll(super.getFields());
         fields.addAll(EnumSet.allOf(ClientField.class));
         return fields;
     }
 
     public String getClientId() {
         return this.get(Field.USER);
     }
 
     public String getClientSecret() {
         return this.get(Field.PWDHASH);
     }
 
     public String getRedirectUri() {
         return this.get(ClientField.REDIRECT_URI);
     }
 
     public String getSourceUrl() {
         return this.get(ClientField.SOURCE_URL);
     }
 
     public static enum ClientField implements MessageField {
 
         // - PUBLIC
 
         SOURCE_URL,
         
         REDIRECT_URI {
             @Override
             public void validate(String value) throws SimpleDBException {
                 super.validate(value);
                 try {
                     OAuth2.validateRedirectUri(value);
                 } catch (ValidationException e) {
                     throw new InvalidRequestException(e.getMessage());
                 }
             }
         };
 
         @Override
         public String getFieldName() {
             return name();
         }
 
         @Override
         public boolean isRequired() {
             return true;
         }
 
         @Override
         public void validate(String value) throws SimpleDBException {
             if (isRequired()) validateNotBlank(name(), value);
         }
     }
 }
