 /**
  * This file is a generic WikiMeta request
  * see WikiMetaRequest.java for my use
  */
 package org.zoneproject.extractor.plugin.wikimeta;
 
 /*
  * #%L
  * ZONE-plugin-WikiMeta
  * %%
  * Copyright (C) 2012 ZONE-project
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.io.JsonStringEncoder;
 import org.codehaus.jackson.map.ObjectMapper;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class WikiMetaRequest_API {
     
     
     public enum Format {
 	XML("xml"),
 	JSON("json");
         private final String value;
 	Format(String value) {this.value = value;}
 	public String getValue() {return this.value;}
     }
     
     public static String getResult(String apiKey, Format format, String content) {
         return WikiMetaRequest_API.getResult(apiKey, format, content, 10, 100, "FR",true);
     }
     
     public static String getResult(String apiKey, Format format, String content, int treshold, int span, String lng, boolean semtag) {    
        content = new String(JsonStringEncoder.getInstance().quoteAsString(content.substring(0,Math.min(content.length(),60000))));
         String result = "";
         try {
             URL url = new URL("http://www.wikimeta.com/wapi/service");
             HttpURLConnection server = (HttpURLConnection)url.openConnection();
             server.setDoInput(true);
             server.setDoOutput(true);
             server.setRequestMethod("POST");
             server.setRequestProperty("Accept",format.value);
             server.connect();
             
             BufferedWriter bw = new BufferedWriter(
                                 new OutputStreamWriter(
                                     server.getOutputStream()));
             String semtagS = "0";
             if(semtag)semtagS = "1";
             
             String request = "treshold="+treshold+"&span="+span+"&lng="+lng+"&semtag="+semtagS+"&api="+apiKey+"&contenu="+content;
             bw.write(request, 0, request.length());
             bw.flush();
             bw.close();
             
             BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
             
             String ligne;
             while ((ligne = reader.readLine()) != null) {
                 result += ligne+"\n";
             }
             
             reader.close();
             server.disconnect();
         }
         catch (Exception e)
         {
             Logger.getLogger(WikiMetaRequest_API.class.getName()).log(Level.SEVERE, null, e);
         }
         return result;
     }
     
     public static ArrayList<LinkedHashMap> getNamedEntities(String f){
         f = f.replace("\\\",", "\",");
         f = f.replace("\"\\\"", "\"\"");
         ObjectMapper mapper = new ObjectMapper();
        
         try {
             //first need to allow non-standard json
             mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
             mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
             mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
             Map<String,Object> map = mapper.readValue(f, Map.class);
             
             ArrayList<LinkedHashMap> documentElems = (ArrayList<LinkedHashMap>)map.get("document");
             for(int i=0; i < documentElems.size();i++){
                 LinkedHashMap cur = (LinkedHashMap) documentElems.get(i);
                 if(cur.containsKey("Named Entities"))
                     return (ArrayList<LinkedHashMap>)cur.values().iterator().next();
             }
         } catch (Exception ex) {
             Logger.getLogger(WikiMetaRequest_API.class.getName()).log(Level.SEVERE, null, ex);
         }
         return new ArrayList<LinkedHashMap>();
     }
}
