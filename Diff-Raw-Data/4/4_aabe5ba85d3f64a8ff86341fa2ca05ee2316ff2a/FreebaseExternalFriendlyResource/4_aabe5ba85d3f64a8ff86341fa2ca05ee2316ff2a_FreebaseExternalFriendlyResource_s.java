 package org.triple_brain.module.model;
 
 import com.google.api.client.http.*;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.triple_brain.module.common_utils.DataFetcher;
 import org.triple_brain.module.common_utils.Urls;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class FreebaseExternalFriendlyResource extends Observable {
     private ExternalFriendlyResource externalFriendlyResource;
 
     public static String DESCRIPTION_BASE_URI = "https://www.googleapis.com/freebase/v1/text/";
 
     public static Boolean isFromFreebase(ExternalFriendlyResource externalFriendlyResource) {
         return externalFriendlyResource
                 .uri()
                 .getHost()
                 .toLowerCase().
                         contains("freebase.com");
     }
 
     public static FreebaseExternalFriendlyResource fromExternalResource(ExternalFriendlyResource externalFriendlyResource) {
         return new FreebaseExternalFriendlyResource(
                 externalFriendlyResource
         );
     }
 
     protected FreebaseExternalFriendlyResource(ExternalFriendlyResource externalFriendlyResource) {
         this.externalFriendlyResource = externalFriendlyResource;
     }
 
     public void getImages(Observer observer) {
         new Thread(new GetImageThread(
                 this,
                 observer
         )).start();
     }
 
     public void getDescription(Observer observer) {
         new Thread(new GetDescriptionThread(
                 this,
                 observer
         )).start();
     }
 
     public String freebaseId() {
         return externalFriendlyResource.uri().toString()
                 .replace("http://rdf.freebase.com/rdf", "");
     }
 
     public ExternalFriendlyResource get() {
         return externalFriendlyResource;
     }
 
     private class GetImageThread implements Runnable{
 
         private FreebaseExternalFriendlyResource freebaseExternalFriendlyResource;
         private Observer observer;
 
         public GetImageThread(FreebaseExternalFriendlyResource freebaseExternalFriendlyResource, Observer observer){
             this.freebaseExternalFriendlyResource = freebaseExternalFriendlyResource;
             this.observer = observer;
         }
 
         @Override
         public void run() {
             observer.update(
                     freebaseExternalFriendlyResource,
                     getImages()
             );
         }
 
         private Set<Image> getImages(){
 //            HttpRequestInitializer initializer =
 //                    new CommonGoogleJsonClientRequestInitializer("MY KEY");
 //            HttpTransport httpTransport = new NetHttpTransport();
 //            JsonFactory jsonFactory = new JacksonFactory();
 //            Freebase freebase = new Freebase(
 //                    httpTransport,
 //                    jsonFactory,
 //                    initializer
 //            );
             try{
                 String imagesKey = "/common/topic/image";
                 String id = "id";
                 org.codehaus.jettison.json.JSONArray imagesQuery = new org.codehaus.jettison.json.JSONArray();
                 imagesQuery.put(new org.codehaus.jettison.json.JSONObject().put(
                         id,
                         new org.codehaus.jettison.json.JSONArray()
                 ));
                 org.codehaus.jettison.json.JSONObject query = new org.codehaus.jettison.json.JSONObject();
                 query.put(id, freebaseId());
                 query.put(
                     imagesKey,
                     imagesQuery
                 );
                 HttpTransport httpTransport = new NetHttpTransport();
                 HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
                 JSONParser parser = new JSONParser();
                 GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread");
                 url.put("query", query.toString());
                 url.put("key", "AIzaSyBHOqdqbswxnNmNb4k59ARSx-RWokLZhPA");
                 HttpRequest request = requestFactory.buildGetRequest(url);
                 HttpResponse httpResponse = request.execute();
                 JSONObject response = new JSONObject(parser.parse(httpResponse.parseAsString()).toString());
                 Set<Image> images = new HashSet<>();
                 if(response.getString("result").equals("null")){
                     return images;
                 }
                 JSONObject results = response.getJSONObject("result");
                 JSONArray imagesAsJson = results.getJSONArray("/common/topic/image");
                 for(int i = 0 ; i < imagesAsJson.length(); i++){
                     String imageId = imagesAsJson.getJSONObject(i).getJSONArray("id").getString(0);
                     images.add(
                             Image.withUrlForSmallAndBigger(
                                     Urls.get(
                                            "http://img.freebase.com/api/trans/image_thumb" + imageId
                                     ),
                                     Urls.get(
                                             "http://img.freebase.com/api/trans/raw" + imageId
                                     )
                             )
                     );
                 }
                 return images;
             }catch(Exception e){
                 throw new RuntimeException(e);
             }
         }
     }
 
     private class GetDescriptionThread implements Runnable{
 
         private FreebaseExternalFriendlyResource freebaseExternalFriendlyResource;
         private Observer observer;
 
         public GetDescriptionThread(FreebaseExternalFriendlyResource freebaseExternalFriendlyResource, Observer observer){
             this.freebaseExternalFriendlyResource = freebaseExternalFriendlyResource;
             this.observer = observer;
         }
 
         @Override
         public void run() {
             observer.update(
                     freebaseExternalFriendlyResource,
                     getDescription()
             );
         }
 
         private String getDescription(){
             try{
                 org.codehaus.jettison.json.JSONObject resultEnveloppe = DataFetcher.getJsonFromUrl(
                         new URL(DESCRIPTION_BASE_URI + freebaseExternalFriendlyResource.freebaseId())
                 );
                 return resultEnveloppe.getString("result");
             }catch(JSONException | MalformedURLException e){
                 throw new RuntimeException(e);
             }
         }
     }
 }
