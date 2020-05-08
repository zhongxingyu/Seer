 package li.rudin.rt.test.web.client.longpoll;
 
 import java.util.concurrent.Callable;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.CloseableHttpClient;
 import org.apache.http.impl.client.HttpClientBuilder;
 import org.junit.Assert;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class LongPollClient implements Callable<Boolean>
 {
 
 	private static final ObjectMapper mapper = new ObjectMapper();
 
 	public LongPollClient(String url, int iterations)
 	{
 		this.url = url;
 		this.iterations = iterations;
 	}
 
 	private final String url;
 	private final int iterations;
 
 	@Override
 	public Boolean call() throws Exception
 	{
 		CloseableHttpClient httpclient = HttpClientBuilder.create().build();
 		HttpGet httpGet = new HttpGet(url);
 		
 		long clientValue = -1;
 
 		for (int i=0; i<iterations; i++)
 		{
 			HttpResponse response = httpclient.execute(httpGet);
 
 			HttpEntity entity = response.getEntity();
 
 			Data[] data = mapper.readValue(entity.getContent(), Data[].class);
 
 			Assert.assertTrue(data.length > 0);
 			
 			long value = 0;
 			Long lastObsTime = null;
 			
 			for (Data d: data)
 			{
 				//System.err.println(url + " @ " + d.type + " @ " + d.data); //XXX
 				
 				if (d.type.equals("fastcounter"))
 				{
 					if (value > 0)
 					{
 						//Check increment
 						Assert.assertEquals("global increment", value + 1, d.data);
 					}
 					
 					value = d.data;
 				}
 				else if (d.type.equals("clientfastcounter"))
 				{
 					//Check increment
 					//System.err.println(url + " @ " + this + " @ " + clientValue + " @ " + d.data);//XXX
 					Assert.assertEquals("client increment @ " + url, clientValue + 1, d.data);
 					
 					clientValue = d.data;
 				}
 				else if (d.type.equals("myObservable"))
 				{
 					Assert.assertTrue(d.data > 0);
 					
 					if (lastObsTime != null)
 						//check increment
 						Assert.assertTrue(lastObsTime < d.data);
 
 					lastObsTime = d.data;
 					
					Assert.assertEquals(Long.class.getName(), d.className);
 				}
 			}
 			
 			httpGet.reset();
 		}
 
 		return true;
 	}
 
 	/**
 	 * Expected data
 	 *
 	 */
 	static class Data
 	{
 		public String type;
 		public long data;
		public String className;
 	}
 
 }
