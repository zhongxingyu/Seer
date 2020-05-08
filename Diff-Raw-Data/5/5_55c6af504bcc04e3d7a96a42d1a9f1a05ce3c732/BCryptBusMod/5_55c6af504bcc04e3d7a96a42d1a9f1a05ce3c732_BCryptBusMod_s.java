 package org.larson.bcrypt;
 /*
  * Copyright 2013 Red Hat, Inc.
  *
  * Red Hat licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  *
  * @author <a href="http://tfox.org">Tim Fox</a>
  */
 
 import org.vertx.java.busmods.BusModBase;
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.eventbus.Message;
 import org.vertx.java.core.json.JsonObject;
 
 public class BCryptBusMod extends BusModBase {
   private Handler<Message<JsonObject>> hashHandler;
   private Handler<Message<JsonObject>> checkHandler;
 
   private String address;
 
   public void start() {
     super.start();
 
     this.address = getOptionalStringConfig("address", "bcrypt");
 
     hashHandler = new Handler<Message<JsonObject>>() {
       public void handle(Message<JsonObject> message) {
         doHash(message);
       }
     };
     eb.registerHandler(address + ".hash", hashHandler);
     checkHandler = new Handler<Message<JsonObject>>() {
       public void handle(Message<JsonObject> message) {
         doCheck(message);
       }
     };
     eb.registerHandler(address + ".check", checkHandler);
   }
 
   public void stop() {
   }
 
   private void doHash(Message<JsonObject> message){
     String password = getMandatoryString("password", message);
     if (password == null | password == ""){
       sendStatus("error", message);
       return;
     }
     String hashed = BCrypt.hashpw("password", BCrypt.gensalt());
     JsonObject reply = new JsonObject().putString("hashed", hashed);
     sendOK(message, reply);
   }
   private void doCheck(Message<JsonObject> message){
     String password = getMandatoryString("password", message);
     String hashed = getMandatoryString("hashed", message);
     JsonObject reply = new JsonObject();
     if (password == null | hashed == null | password == "" | hashed == ""){
       sendStatus("error", message);
       return;
     }
     if (BCrypt.checkpw(password, hashed)){
	    reply.putString("match", true);
     } else {
	    reply.putString("match", false);
     }
     sendOK(message, reply);
   }
 }
