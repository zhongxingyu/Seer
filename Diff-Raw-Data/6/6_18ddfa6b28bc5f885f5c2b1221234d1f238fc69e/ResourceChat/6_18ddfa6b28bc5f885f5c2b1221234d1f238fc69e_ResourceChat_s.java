 /*
  * Copyright 2012 Jeanfrancois Arcand
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.atmosphere.samples.chat.jersey;
 
import com.sun.jersey.api.json.JSONWithPadding;
 import org.atmosphere.annotation.Broadcast;
 import org.atmosphere.annotation.Suspend;
 import org.atmosphere.cpr.Broadcaster;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 
 /**
  * Simple chat resource demonstrating the power of Atmosphere. This resource supports transport like WebSocket, Streaming, JSONP and Long-Polling.
  *
  * @author Jeanfrancois Arcand
  */
 @Path("/")
 @Produces("application/json")
 public class ResourceChat {
 
     /**
      * Suspend the response without writing anything back to the client.
      * @return a white space
      */
     @Suspend
     @GET
     public String suspend() {
         return "";
     }
 
     /**
      * Broadcast the received message object to all suspended response. Do not write back the message to the calling connection.
      * @param message a {@link Message}
      * @return a {@link Response}
      */
     @Broadcast(writeEntity = false)
     @POST
    public JSONWithPadding broadcast(Message message) {
        return new JSONWithPadding(new Response(message.author, message.message));
     }
 
 }
