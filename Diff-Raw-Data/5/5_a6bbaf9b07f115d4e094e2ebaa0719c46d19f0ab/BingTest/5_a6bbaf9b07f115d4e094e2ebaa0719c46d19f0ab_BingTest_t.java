 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import org.json.simple.*;
 import org.json.simple.parser.ContainerFactory;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 import org.apache.commons.codec.binary.Base64;
 
 public class BingTest {
 	public static String getMatchResultNum(String query) throws IOException {
 		
         String bingURL = "https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Composite?Query=%27site%3a"+query+"%27&$top=10&$format=Json";
 		//Provide your account key here.
 		String accountKey = "wRccq1TMy476bqFdC1GrKeHeJ33Fm+hmzSwYWgmtSrM=";
 		
 		byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
 		String accountKeyEnc = new String(accountKeyBytes);
         
		URL url = new URL(bingURL);
 		URLConnection urlConnection = url.openConnection();
 		urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
         
 		InputStream inputStream = (InputStream) urlConnection.getContent();
 		byte[] contentRaw = new byte[urlConnection.getContentLength()];
 		inputStream.read(contentRaw);
 		String content = new String(contentRaw);
         
 		return content;
 	}
     
     
     
 		public static Integer parseJSON(String jsonStr) {
         
         String result = "";
 		JSONParser parser = new JSONParser();
 		KeyFinder finder = new KeyFinder();
 		finder.setMatchKey("WebTotal");
 		try{
 			
 		    while(!finder.isEnd()){
                 parser.parse(jsonStr, finder, true);
                 if(finder.isFound()){
                     finder.setFound(false);
                     result = finder.getValue().toString();
                     break;
                 }
 		    }
             
         }
 	    catch(ParseException pe){
 		    pe.printStackTrace();
         }
         
 				
         return Integer.parseInt(result);
 	}
 	
     
    
 /* main thread */
     
 	public static void main(String[] args) throws IOException {
         
         System.out.println("Please input site and query:");
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         String query = null;
 		try {
 			query = br.readLine();
 			
 		} catch (IOException ioe) {
 			System.out.println("IO error trying to read your Query!");
 			System.exit(1);
 		}
         
         
        String key = java.net.URLEncoder.encode(query, "utf8");
         String content = getMatchResultNum(key);
         int match_num = parseJSON(content);
         System.out.println("We get "+match_num+" matching results.");
         System.exit(0);
             
 	}
     
         
 }
