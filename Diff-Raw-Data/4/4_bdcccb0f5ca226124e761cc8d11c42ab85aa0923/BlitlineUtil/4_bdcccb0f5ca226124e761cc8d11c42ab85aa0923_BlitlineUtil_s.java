 package org.netvogue.server.blitline.util;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.netvogue.server.aws.core.ImageType;
 import org.netvogue.server.aws.core.Size;
 import org.netvogue.server.blitline.model.BlitlineFunction;
 import org.netvogue.server.blitline.model.BlitlineMessageRequest;
 import org.netvogue.server.blitline.model.BlitlineRequest;
 import org.netvogue.server.blitline.model.S3DestinationDetails;
 import org.netvogue.server.blitline.model.SaveParameters;
 
 public class BlitlineUtil {
 
   private static ObjectMapper mapper = new ObjectMapper();
 
  private static HttpClient httpClient = new DefaultHttpClient();
 
   static {
     ResourceLoader.loadProperties();
   }
 
   public static void sendBlitlineRequest(String srcURL, String uniqueId, ImageType imageType, String functionName)
       throws Exception {
     BlitlineFunction function = null;
     BlitlineRequest blitlineRequest = new BlitlineRequest();
     BlitlineMessageRequest blitlineMessageRequest = new BlitlineMessageRequest();
     blitlineMessageRequest.setSrc(srcURL);
     List<BlitlineFunction> list = new ArrayList<BlitlineFunction>();
     Size[] sizes = imageType.getSizes();
     for (Size size : sizes) {
       function = createBlitlineFunction(functionName, size, uniqueId);
       list.add(function);
     }
     blitlineMessageRequest.setFunctions(list);
     blitlineRequest.setJson(blitlineMessageRequest);
     String dataToSend = mapObjectToJSON(blitlineRequest);
     System.out.println("dataToSend: " + dataToSend);
     System.out.println("Time while sending: " + System.currentTimeMillis());
     performPostRequest(dataToSend);
   }
 
   private static BlitlineFunction createBlitlineFunction(String functionName, Size size, String uniqueId) {
     BlitlineFunction function = new BlitlineFunction();
     function.setName(functionName);
     Map<String, String> map = new HashMap<String, String>();
     map.put("height", String.valueOf(size.getHeight()));
     map.put("width", String.valueOf(size.getWidth()));
     function.setParams(map);
     SaveParameters parameters = createSaveParameters(uniqueId, size.toString());
     function.setSave(parameters);
     return function;
   }
 
   private static SaveParameters createSaveParameters(String uniqueId, String sizeName) {
     SaveParameters parameters = new SaveParameters();
     String key = uniqueId + "-" + sizeName;
     parameters.setImage_identifier(key);
     S3DestinationDetails destinationDetails = new S3DestinationDetails();
     destinationDetails.setKey(key);
     parameters.setS3_destination(destinationDetails);
     return parameters;
   }
 
   private static String mapObjectToJSON(Object object) {
     String dataToSend = null;
     try {
       dataToSend = mapper.writeValueAsString(object);
     } catch (Exception rfe) {
       rfe.printStackTrace();
       System.out.println(rfe.getMessage() + "Error while creating JSON object");
     }
     return dataToSend;
   }
 
   private static void performPostRequest(String postData) throws Exception {
 
     HttpPost postRequest = new HttpPost(ResourceLoader.getProperty("blitline_endpoint"));
     StringEntity input = new StringEntity(postData);
     input.setContentType("application/json");
     postRequest.setEntity(input);
     HttpResponse response = httpClient.execute(postRequest);
     BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
     StringBuffer sb = new StringBuffer();
     String output;
     System.out.println("Output from Server .... \n");
     while ((output = br.readLine()) != null) {
       sb.append(output);
       System.out.println(output);
     }
 
     String responseString = sb.toString();
     System.out.println(responseString);
 
   }
 
   /*
    * public void testMain() throws Exception { BlitlineMessageRequest blitlineMessageRequest = new
    * BlitlineMessageRequest(); // blitlineMessageRequest.setSrc("http://cdn.blitline.com/filters/boys.jpeg");
    * blitlineMessageRequest.setPostback_url("http://netvogue.ap01.aws.af.cm/blitlinetest"); BlitlineFunction function =
    * new BlitlineFunction(); function.setName("blur"); // Map<String, String> map = function.getParams(); //
    * map.put("X", "200"); // map.put("Y", "100"); // map.put("height", "400"); // function.setParams(map);
    * SaveParameters parameters = new SaveParameters(); parameters.setImage_identifier("MY_TEST_IMAGE_BLUR");
    * S3DestinationDetails details = new S3DestinationDetails();
    * details.setKey("pavan1/gallery/3a8eff61-0c97-487c-a94e-6ea844fsdfsdsd534-blur");
    * parameters.setS3_destination(details); function.setSave(parameters); List<BlitlineFunction> list = new
    * ArrayList<BlitlineFunction>(); list.add(function); blitlineMessageRequest.setFunctions(list); // BlitlineRequest
    * blitlineRequest = new BlitlineRequest(); blitlineRequest.setJson(blitlineMessageRequest); ObjectMapper mapper = new
    * ObjectMapper(); String dataToPost = mapper.writeValueAsString(blitlineRequest); System.out.println(dataToPost);
    * performPostRequest(dataToPost); }
    */
 
 }
