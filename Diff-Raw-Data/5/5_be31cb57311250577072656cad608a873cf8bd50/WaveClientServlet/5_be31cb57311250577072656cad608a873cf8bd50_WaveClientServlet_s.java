 /**
  * Copyright 2010 Google Inc.
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
  *
  */
 
 package org.waveprotocol.wave.examples.fedone.rpc;
 
 import com.google.common.collect.Maps;
 import com.google.gxp.base.GxpContext;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.waveprotocol.wave.client.util.ClientFlagsBase;
 import org.waveprotocol.wave.common.bootstrap.FlagConstants;
 import org.waveprotocol.wave.examples.common.SessionConstants;
 import org.waveprotocol.wave.examples.fedone.gxp.WaveClientPage;
 import org.waveprotocol.wave.examples.fedone.util.Log;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.Enumeration;
 import java.util.HashMap;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * The HTTP servlet for serving a wave client along with content generated on
  * the server.
  *
  * @author kalman@google.com (Benjamin Kalman)
  */
 public class WaveClientServlet extends HttpServlet {
 
   private static final Log LOG = Log.get(WaveClientServlet.class);
 
   private static final HashMap<String, String> FLAG_MAP = Maps.newHashMap();
   static {
     // __NAME_MAPPING__ is a map of name to obfuscated id
     for (int i = 0; i < FlagConstants.__NAME_MAPPING__.length; i += 2) {
       FLAG_MAP.put(FlagConstants.__NAME_MAPPING__[i], FlagConstants.__NAME_MAPPING__[i+1]);
     }
   }
 
   private final String domain;
 
   private WaveClientServlet(String domain) {
     this.domain = domain;
   }
 
   /**
    * Creates a servlet for the wave client.
    */
   public static WaveClientServlet create(String domain) {
     return new WaveClientServlet(domain);
   }
 
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) {
     try {
       WaveClientPage.write(
           response.getWriter(),
           new GxpContext(request.getLocale()),
           getSessionJson(),
           getClientFlags(request));
       response.setContentType("text/html");
       response.setStatus(HttpServletResponse.SC_OK);
     } catch (IOException e) {
       LOG.warning("Failed to write GXP for request " + request, e);
       response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   private JSONObject getClientFlags(HttpServletRequest request) {
     try {
       JSONObject ret = new JSONObject();
 
      Enumeration iter = request.getParameterNames();
       while (iter.hasMoreElements()) {
         String name = (String) iter.nextElement();
         String value = request.getParameter(name);
 
         if (FLAG_MAP.containsKey(name)) {
           // Set using the correct type of data in the json using reflection
           try {
             Method getter = ClientFlagsBase.class.getMethod(name);
            Class retType = getter.getReturnType();
 
             if (retType.equals(String.class)) {
               ret.put(FLAG_MAP.get(name), value);
             } else if (retType.equals(Integer.class)) {
               ret.put(FLAG_MAP.get(name), Integer.parseInt(value));
             } else if (retType.equals(Boolean.class)) {
               ret.put(FLAG_MAP.get(name), Boolean.parseBoolean(value));
             } else if (retType.equals(Float.class)) {
               ret.put(FLAG_MAP.get(name), Float.parseFloat(value));
             } else if (retType.equals(Double.class)) {
               ret.put(FLAG_MAP.get(name), Double.parseDouble(value));
             } else {
               // Flag exists, but its type is unknown, so it can not be
               // properly encoded in JSON.
               LOG.warning("Ignoring flag [" + name + "] with unknown return type: " + retType);
             }
 
           // Ignore the flag on any exception
           } catch (SecurityException ex) {
           } catch (NoSuchMethodException ex) {
             LOG.warning("Failed to find the flag [" + name + "] in ClientFlagsBase.");
           } catch (NumberFormatException ex) {
           }
         }
       }
 
       return ret;
     } catch (JSONException ex) {
       LOG.severe("Failed to create flags JSON");
       return new JSONObject();
     }
   }
 
   private JSONObject getSessionJson() {
     try {
       return new JSONObject()
           .put(SessionConstants.DOMAIN, domain);
     } catch (JSONException e) {
       LOG.severe("Failed to create session JSON");
       return new JSONObject();
     }
   }
 }
