 /*
  * Copyright 2013 Netflix, Inc.
  *
  *      Licensed under the Apache License, Version 2.0 (the "License");
  *      you may not use this file except in compliance with the License.
  *      You may obtain a copy of the License at
  *
  *          http://www.apache.org/licenses/LICENSE-2.0
  *
  *      Unless required by applicable law or agreed to in writing, software
  *      distributed under the License is distributed on an "AS IS" BASIS,
  *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *      See the License for the specific language governing permissions and
  *      limitations under the License.
  */
 
 package com.liaison.helloworld.server;
 
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import javax.script.*;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.TreeMap;
 
 
 @Path("/hello")
 public class HelloworldResource {
 
     private static final Logger logger = LoggerFactory.getLogger(HelloworldResource.class);
 
     // curl -H "Content-Type: application/json" --data-asc80/hello-world-1.0.16-SNAPSHOT/rest/v1/hello/dyn
 
     @Path("dyn/{service}")
     @POST
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces({MediaType.APPLICATION_JSON})
     public Response dyn(@PathParam("service") String service, JSONObject json) {
         JSONObject response = new JSONObject();
         try {
             response.put("Service", service);
             response.put("JSON", json);
             response.put("Foo", "bar");
 
             // read js
             InputStream in = this.getClass().getResourceAsStream("/hello.js");
            InputStreamReader reader = new InputStreamReader(in);
 
             // setup engine
             ScriptEngineManager mgr = new ScriptEngineManager();
             ScriptEngine engine = mgr.getEngineByName("JavaScript");
 
             // inject bindings
             //Bindings bindings = jsEngine.createBindings();
             //bindings.put("_returnValue", null);
 
 
 
             try {
 
                 // evaluate the script
                 engine.eval (reader);
 
                 // TODO inject bfg
 
                 logger.error("Running JS...");
 
                 Object ret = ((Invocable) engine).invokeFunction("run", "3", "1");
 
                 logger.error("retVal is " + ret);
 
                 response.put("ret", ret);
 
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
 
             return Response.ok(response.toString()).build();
         } catch (JSONException e) {
             logger.error("Error creating json response.", e);
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 
     @Path("to/{name}")
     @GET
     @Produces({MediaType.APPLICATION_JSON})
     public Response helloTo(@PathParam("name") String name) {
         JSONObject response = new JSONObject();
         try {
             response.put("Message", "Hello " + name + " from Liaison");
             return Response.ok(response.toString()).build();
         } catch (JSONException e) {
             logger.error("Error creating json response.", e);
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 
     @GET
     @Produces({MediaType.APPLICATION_JSON})
     public Response hello() {
         JSONObject response = new JSONObject();
         try {
             response.put("Message", "Hello Liaison component!");
             return Response.ok(response.toString()).build();
         } catch (JSONException e) {
             logger.error("Error creating json response.", e);
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 }
