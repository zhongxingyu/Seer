 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLDecoder;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Random;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
 import org.apache.solr.client.solrj.response.UpdateResponse;
 import org.apache.solr.common.SolrInputDocument;
 
 /**
  * Read sample
  * @author brad_liu
  *
  */
 public class FFMTest {
 
 	private String getUrl() {
 		String[] urls = {"10.31.66.59:9999"};
 		
 		Random generator = new Random();    		
 		int r = generator.nextInt(urls.length);
 		// return this format http://10.31.66.58:8983/solr
 		return "http://" + urls[r] + "/solr/ffm";
 	}
 	
 	public void run(String[] args) throws InterruptedException, URISyntaxException {
 		
     	URI uri = new URI(this.getUrl());
     	DefaultHttpClient httpClient = new DefaultHttpClient();
 
     	httpClient.getCredentialsProvider().setCredentials(
 				new AuthScope(uri.getHost(), uri.getPort(),AuthScope.ANY_SCHEME),
 				new UsernamePasswordCredentials("solrcloud_trial", "pass"));
     	
 //	      ModifiableSolrParams params = new ModifiableSolrParams();
 //	      params.set(HttpClientUtil.PROP_MAX_CONNECTIONS, 128);
 //	      params.set(HttpClientUtil.PROP_MAX_CONNECTIONS_PER_HOST, 32);
 //	      params.set(HttpClientUtil.PROP_BASIC_AUTH_USER, "solrcloud_trial");
 //	      params.set(HttpClientUtil.PROP_BASIC_AUTH_PASS, "pass");
 //	      HttpClient httpClient =  HttpClientUtil.createClient(params);
 	      
 	      
         // init solr server
        SolrServer solrServer = new ConcurrentUpdateSolrServer(this.getUrl(), 100, 1);
 		//SolrServer solrServer = new HttpSolrServer(this.getUrl());
        
         //File file = new File("./out.txt");
         File file = new File(args[0]);
         FileInputStream fis = null;
         BufferedInputStream bis = null;
 
         try {
           fis = new FileInputStream(file);
 
           // Here BufferedInputStream is added for fast reading.
           bis = new BufferedInputStream(fis);
           BufferedReader d = new BufferedReader(new InputStreamReader(bis));
 
           String valueStr;
           while ( (valueStr = d.readLine()) != null ) {
 			//System.out.println(valueStr);
 			StringTokenizer tokenizer = new StringTokenizer(valueStr);
 			SolrInputDocument doc = new SolrInputDocument();        
 
 			while (tokenizer.hasMoreTokens()) {
 				Pattern datePatt = Pattern.compile("(.*)=(.*)");
 				Matcher m = datePatt.matcher(tokenizer.nextToken());
 				if (m.matches()) {
 				  //System.out.println("key=" + m.group(1) + ", value=" + m.group(2));		            
 		          doc.addField(m.group(1), URLDecoder.decode(m.group(2), "UTF-8"));
 				}
 			}
 			
 			//Thread.sleep(200);
         	//UpdateResponse res = solrServer.add(doc, 10000);
 			UpdateResponse res = solrServer.add(doc);
     	    //System.out.println("res=" + res.toString());
     	    //solrServer.commit();
           }
 
           // dispose all the resources after using them.
           fis.close();
           bis.close();
           
           System.out.println("before commit");
           //Thread.sleep(10000);
           solrServer.commit();
           System.out.println("after commit");
         }
     	catch (FileNotFoundException e) {
     		e.printStackTrace();
     	} catch (IOException e) {
 	        e.printStackTrace();
     	}
     	catch (SolrServerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}        
 	}
 	
 	public static void main(String[] args) throws Exception {
		FFMTest test = new FFMTest();
 		test.run(args);
 		System.out.println("bye~~");
 	    Calendar cal = Calendar.getInstance();
 	    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL,
 	        DateFormat.MEDIUM);
 
 	    System.out.println(df.format(cal.getTime()));
 	}
 }
