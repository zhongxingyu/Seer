 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.webframework.view;
 
 import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
 import cx.ath.mancel01.webframework.WebFramework;
 import cx.ath.mancel01.webframework.http.Response;
 import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
 import javax.ws.rs.core.MediaType;
 
 /**
  *
  * @author mathieuancelin
  */
 public class JSON extends Renderable {
 
     private final Object jsonObject;
 
     public JSON(Object jsonObject) {
         this.jsonObject = jsonObject;
         this.contentType = MediaType.APPLICATION_JSON;
     }
 
     public Object getJsonObject() {
         return jsonObject;
     }
 
     @Override
     public Response render() {
         long start = System.currentTimeMillis();
         Response res = new Response();
         res.out = new ByteArrayOutputStream();
         res.contentType = this.getContentType();
        //Gson gson = new Gson();
        Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
            .create();
         String jsonRepresentation = gson.toJson(this.getJsonObject());
         res.out.write(jsonRepresentation.getBytes(), 0, jsonRepresentation.length());
         WebFramework.logger.trace("JSON object rendering : {} ms.", (System.currentTimeMillis() - start));
         return res;
     }
 }
