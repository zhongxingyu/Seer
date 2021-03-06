 /*
  * Copyright 2012 calabash-driver committers.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 package sh.calaba.driver.server.command.impl;
 
 import java.util.Date;
 
 import org.json.JSONObject;
 
 import sh.calaba.driver.net.WebDriverLikeRequest;
 import sh.calaba.driver.net.WebDriverLikeResponse;
 import sh.calaba.driver.server.CalabashProxy;
 import sh.calaba.driver.server.command.BaseCommandHandler;
 
 public class GetStatus extends BaseCommandHandler {
   public GetStatus(CalabashProxy proxy, WebDriverLikeRequest request) {
     super(proxy, request);
 
   }
 
   public WebDriverLikeResponse handle() throws Exception {
     JSONObject build = new JSONObject();
    build.put("version", "1.1-snapshot");
     build.put("revision", "01");
     build.put("time", new Date().getTime());
 
     JSONObject os = new JSONObject();
     os.put("arch", System.getProperty("os.arch"));
     os.put("name", System.getProperty("os.name"));
     os.put("version", System.getProperty("os.version"));
 
     JSONObject json = new JSONObject();
     json.put("build", build);
     json.put("os", os);
    return new WebDriverLikeResponse(null, 200, json);
 
   }
 }
