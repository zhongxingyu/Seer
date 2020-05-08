 /**
  * Copyright 2009: Dane Summers<dsummersl@yahoo.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	 http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package biz.pinedesk.slimtimer;
 
 import biz.pinedesk.slimtimer.util.DateConverter;
 import biz.pinedesk.slimtimer.util.IsNilConverter;
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.mapper.CannotResolveClassException;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.*;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.*;
 import java.util.ArrayList;
 
 public class SlimTimerRemoteService implements RemoteService {
     private final HttpClient client = new DefaultHttpClient();
     protected XStream stream;
 
     public SlimTimerRemoteService() {
        stream = new XStream();
         stream.alias("request", LoginRequest.class);
         stream.alias("user", User.class);
         stream.aliasField("api-key", LoginRequest.class, "apiKey");
 
         stream.alias("response", LoginResponse.class);
         stream.aliasField("access-token", LoginResponse.class, "accessToken");
         stream.aliasField("user-id", LoginResponse.class, "userId");
         stream.aliasField("api-key", LoginResponse.class, "apiKey");
 
         stream.alias("tasks", ArrayList.class);
         stream.alias("owners", ArrayList.class);
         stream.alias("reporters", ArrayList.class);
         stream.alias("coworkers", ArrayList.class);
         stream.alias("time-entries", ArrayList.class);
         stream.alias("nil-class", ArrayList.class);
 
         stream.alias("person", Person.class);
         stream.aliasField("user-id", Person.class, "userId");
 
         stream.alias("task", Task.class);
         stream.aliasField("completed-on", Task.class, "completedOn");
         stream.aliasField("updated-at", Task.class, "updatedAt");
         stream.aliasField("created-at", Task.class, "createdAt");
         stream.registerConverter(new IsNilConverter(new DateConverter("yyyy-MM-dd'T'HH:mm:ss'Z'")));
         stream.alias("role", Role.class);
 
         stream.alias("time-entry", TimeEntry.class);
         stream.aliasField("updated-at", TimeEntry.class, "updatedAt");
         stream.aliasField("created-at", TimeEntry.class, "createdAt");
         stream.aliasField("in-progress", TimeEntry.class, "inProgress");
         stream.aliasField("duration-in-seconds", TimeEntry.class, "durationInSeconds");
         stream.aliasField("start-time", TimeEntry.class, "startTime");
         stream.aliasField("end-time", TimeEntry.class, "endTime");
         stream.aliasField("task-id", TimeEntry.class, "taskId");
     }
 
     public Object sendMessage(Crud action, String url, Object message) throws IOException {
         StringEntity entity = new StringEntity(stream.toXML(message));
         entity.setContentType("application/xml");
 
         HttpRequestBase command = null;
         switch (action) {
             case create:
                 HttpPost post = new HttpPost(url);
                 post.addHeader("Content-Type", "application/xml");
                 post.setEntity(entity);
                 command = post;
                 break;
             case update:
                 HttpPut put = new HttpPut(url);
                 put.addHeader("Content-Type", "application/xml");
                 put.setEntity(entity);
                 command = put;
                 break;
             case delete:
                 command = new HttpDelete(url);
                 break;
             case read:
             case list:
                 command = new HttpGet(url);
                 break;
         }
         command.addHeader("Accept", "application/xml");
         HttpResponse response = client.execute(command);
 
         if (action == Crud.delete) {
             command.abort();
             return null;
         }
 
         HttpEntity responseEntity = response.getEntity();
 
         // debugging:
         System.out.println("DEBUG:");
         System.out.println("url = "+ url);
         if (message != null) {
             System.out.println("xml is: \n"+ stream.toXML(message));
         }
         BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
         String line = null;
         StringBuffer buffer = new StringBuffer();
         while ((line = reader.readLine()) != null) {
             buffer.append(line);
             System.out.println(line);
         }
         try {
             Object results = stream.fromXML(buffer.toString());
 //            Object results = stream.fromXML(responseEntity.getContent());
             return results;
         }
         catch (CannotResolveClassException e) {
 //            e.printStackTrace();
             throw new IOException("Unabled to parse response from server");
         }
         finally {
             command.abort();
         }
     }
 }
