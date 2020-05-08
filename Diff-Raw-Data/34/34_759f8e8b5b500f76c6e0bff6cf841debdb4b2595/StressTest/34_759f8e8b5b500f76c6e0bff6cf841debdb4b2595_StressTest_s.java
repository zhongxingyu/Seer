 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URLDecoder;
 import java.util.Random;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.apache.solr.client.solrj.response.UpdateResponse;
 import org.apache.solr.common.SolrInputDocument;
 
 /**
  * Read sample
  * @author brad_liu
  *
  */
 public class StressTest {
 	private String getUrl() {
 		// POC
		//String[] urls = {"10.31.66.58:8983","10.31.66.59:8983","10.31.66.71:8983","10.31.66.72:8983"};
 		
 		// office lab
		String[] urls = {"10.1.112.93:8983","10.1.112.99:8983"};
 		
 		Random generator = new Random();    		
 		int r = generator.nextInt(urls.length);
 		// return this format http://10.31.66.58:8983/solr
 		return "http://" + urls[r] + "/solr";
 	}
 	
 	public void run(String[] args) {
//        if (args.length != 3) {
//            System.err.format("Usage: %s input_file solrnodeip:port[,...]\n", this.getClass().getName());
//            return;
//        }
               
         // init solr server
         SolrServer solrServer = new HttpSolrServer(this.getUrl());
        // while loop
        
        //File file = new File("./out.txt");
        File file = new File("d:\\out.txt");
         FileInputStream fis = null;
         BufferedInputStream bis = null;
        DataInputStream dis = null;
 
         try {
           fis = new FileInputStream(file);
 
           // Here BufferedInputStream is added for fast reading.
           bis = new BufferedInputStream(fis);
          dis = new DataInputStream(bis);
           BufferedReader d = new BufferedReader(new InputStreamReader(bis));
 
          // dis.available() returns 0 if the file does not have more lines.
          while (dis.available() != 0) {
        	String valueStr = d.readLine();
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
 			
         	UpdateResponse res = solrServer.add(doc);
    	    System.out.println("res=" + res.toString());
     	    solrServer.commit();
           }
 
           // dispose all the resources after using them.
           fis.close();
           bis.close();
          dis.close();
         }
     	catch (FileNotFoundException e) {
     		e.printStackTrace();
     	} catch (IOException e) {
 	        e.printStackTrace();
     	}
         // init solr document
         // send update command
         // commit
  catch (SolrServerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
 	}
 	
 	public static void main(String[] args) throws Exception {
 		StressTest test = new StressTest();
 		test.run(args);
 		
 	}
 }
