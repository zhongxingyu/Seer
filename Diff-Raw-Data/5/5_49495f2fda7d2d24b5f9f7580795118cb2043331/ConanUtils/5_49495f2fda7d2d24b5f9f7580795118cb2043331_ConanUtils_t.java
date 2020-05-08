 package uk.ac.ebi.fgpt.sampletab.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Properties;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class ConanUtils {
     private final static Logger log = LoggerFactory.getLogger("uk.ac.ebi.fgpt.sampletab.utils.ConanUtils");
     
     private static Properties properties = null;
 
     private synchronized static void setup(){
         if (properties == null){
             properties = new Properties();
             try {
                 InputStream is = ConanUtils.class.getResourceAsStream("/conan.properties");
                 properties.load(is);
             } catch (IOException e) {
                 log.error("Unable to read resource mysql.properties", e);
             }
         }
     }
 
     public synchronized static void submit(String submissionIdentifier, String pipeline) throws IOException{
         submit(submissionIdentifier, pipeline, 0);
     }
     
     public synchronized static void submit(String submissionIdentifier, String pipeline, int startingProcessIndex) throws IOException{
         setup();
         
         ObjectMapper objectMapper = new ObjectMapper();
         ObjectNode userOb = objectMapper.createObjectNode();
         
         userOb.put("priority", "MEDIUM");
         userOb.put("pipelineName", pipeline);
         userOb.put("startingProcessIndex", startingProcessIndex);
        userOb.put("restApiKey", properties.getProperty("apikey"));
         ObjectNode inputParameters = userOb.putObject("inputParameters");
         inputParameters.put("SampleTab Accession", submissionIdentifier);
         
         log.info(userOb.toString());
 
         // Send data
         DefaultHttpClient httpClient = new DefaultHttpClient();
         HttpPost postRequest = new HttpPost(properties.getProperty("url")+"/api/submissions/");
         StringEntity input = new StringEntity(userOb.toString());
         input.setContentType("application/json");
         postRequest.setEntity(input);
  
         //get response
         HttpResponse response = httpClient.execute(postRequest);
         BufferedReader br = new BufferedReader(
                 new InputStreamReader((response.getEntity().getContent())));
         String line;
         while ((line = br.readLine()) != null) {
             log.info(line);
         }
         //TODO parse response and raise exception if submit failed
         
         //close the connections
         httpClient.getConnectionManager().shutdown();
     }
 }
