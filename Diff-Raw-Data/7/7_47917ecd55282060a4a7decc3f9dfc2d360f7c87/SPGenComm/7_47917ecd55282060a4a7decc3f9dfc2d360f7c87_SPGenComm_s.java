 package il.ac.tau.team3.spcomm;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.http.MediaType;
 import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
 import org.springframework.http.converter.HttpMessageConverter;
 import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
 import org.springframework.web.client.RestClientException;
 import org.springframework.web.client.RestTemplate;
 
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 
 public class SPGenComm {
 	
     
     private RestTemplate restTemplate;
    
     private HandlerThread sendThread;
     private Handler		  comm;
     private Thread 		 recvThread;
     
     static final String SERVER_URL = "http://share-a-prayer.appspot.com/resources/prayerjersy/";
     //static final String SERVER_URL = "http://10.0.2.2:8888/resources/prayerjersy/";
     
     public RestTemplate getRestTemplate()
     {
         return restTemplate;
     }
 
     public void setRestTemplate(RestTemplate restTemplate)
     {
         this.restTemplate = restTemplate;
     }
     
     
     
     public SPGenComm()
     {
         // rest
         restTemplate = new RestTemplate();
         restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
         
         List<HttpMessageConverter<?>>      mc   = restTemplate.getMessageConverters();
         
         MappingJacksonHttpMessageConverter json = new MappingJacksonHttpMessageConverter();
  
         List<MediaType>     supportedMediaTypes = new ArrayList<MediaType>();
         supportedMediaTypes.add(new MediaType("text", "javascript"));
         json.setSupportedMediaTypes(supportedMediaTypes);
         
         mc.add(json);
         restTemplate.setMessageConverters(mc);   
                    
         
         sendThread = new HandlerThread("send thread");
         	
         
         sendThread.start();
         
         comm = new Handler(sendThread.getLooper());
         
         // run threads
         //this.responseThread.run(); //@imp: I think it must be first (maybe even a timeout).
         //this.requestThread.run();
     }
     
     
     
     public <T> void requestGet(final Map<String, String> parameters, final Class<T> responseType, final String request, final ICommHandler<T> retObj)
     {
     	comm.post(new Runnable()	{
 
 			public void run() {
 				T response = null;
 					String url = "";
 			       try 
 			       {
 			    	   
 			    	   if (request.startsWith("http"))	{
 			    		    url = request + "?"; 
 			    	   } else {
 			    		   url = SERVER_URL + request + "?";
 			    	   }
 			    	   for (String key : parameters.keySet())	{
 			    		   url += key + "={" + key + "}&";
 			    	   }
 			    	   url = url.substring(0, url.length() - 1);
 			           response = restTemplate.getForObject(url, responseType, parameters);
 			       }
 			       catch (RestClientException rce)
 			       {
 			    	   if (null != retObj)
 			    		   retObj.onError(null);
 			           Log.e("ShareAPrayer", "Exception in call to requestGet(...) of URL" + url + " Message:" + rce.getMessage(), rce);
 			       } catch (NullPointerException e)	{
 			        	Log.e("ShareAPrayer", "NULL Exception in call to requestGet(...) of URL" + url + " Message:"  + e.getMessage(), e);
 			        }
 			       
 			       if (null != retObj)
 			    	   retObj.onRecv(response);
 				
 			}
     		
     	});
                
     }
     
     public <T, O> void requestPost(final O object, final Class<T> responseType, final String request, final ICommHandler<T> retObj)
     {   
     	comm.post(new Runnable()	{
 
 			public void run() {
 
 		        T response = null;
 		        
 		        try 
 		        {
 		        	String url;
 			    	   if (request.startsWith("http"))	{
 			    		    url = request; 
 			    	   } else {
 			    		   url = SERVER_URL + request;
 			    	   }
 		            response = restTemplate.postForObject(url, object, responseType);
 		        }
 		        catch (RestClientException rce)
 		        {
 		        	if (null != retObj)
 		     		   retObj.onError(null);
 		            Log.e("ShareAPrayer", "Exception in call to requestGet(...)" + rce.getMessage(), rce);
 		        } catch (NullPointerException e)	{
 		        	Log.e("ShareAPrayer", "NULL Exception in call to requestGet(...)" + e.getMessage(), e);
 		        }
 		     
 		        try	{
 			        if (null != retObj)
 			        	retObj.onRecv(response);
 		        } catch (Exception e)	{
 		        	Log.e("Share", e.getMessage());
 		        }
 		    }
     	});
     }
     
     
 }
