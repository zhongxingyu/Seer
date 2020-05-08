 package web;
 
 
 
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 
 import edu.stanford.nlp.tagger.maxent.MaxentTagger;
 
 /**
  * A simple example that uses HttpClient to execute an HTTP request against
  * a target site that requires user authentication.
  */
 public class AxessConnect {
 	
 	
 	private static DBConnection dbc;
 	public static Object dbLock;
 	
 	private String uname;
 	private String pass;
 	public static String SEPARATOR = " %%% ";
 	public AxessConnect(String uname, String pass) throws Exception {
 		this.uname = uname;
 		this.pass = pass;
 		dbc = new DBConnection();
 		dbLock = new Object();
 	}
 	
 	
     public List<String> getCourses() throws Exception {
     	
    	/*List<String> checkResults = dbc.readJavaObjectIfPresent(uname);
     	if (checkResults!=null) {
     		System.out.println("SUCCESS, don't need to login remotely");
     		return checkResults;
     	}
    	*/
     	
     	
     	List<String> coursesTaken = new ArrayList<String>();
     	HttpClient httpclient = new DefaultHttpClient();
         ((DefaultHttpClient)httpclient).setRedirectStrategy(new spaceRedirectStrategy());
         httpclient = WebClientDevWrapper.wrapClient(httpclient);
         ((DefaultHttpClient)httpclient).setRedirectStrategy(new spaceRedirectStrategy());
         try {
         	/*
             httpclient.getCredentialsProvider().setCredentials(
                     new AuthScope(null, -1),
                     new UsernamePasswordCredentials("eadrian", "eactresp1"));
 */
         	
         	
             HttpGet httpget = new HttpGet("https://axessauth.stanford.edu/secure_login/index.cgi?httpPort=&timezoneOffset=420&Version=&Submit=Login");
             List<NameValuePair> pairs = new ArrayList<NameValuePair>();
             
             
             System.out.println("executing request" + httpget.getRequestLine());
             HttpResponse response = httpclient.execute(httpget);
             HttpEntity entity = response.getEntity();
 
             System.out.println("----------------------------------------");
             System.out.println(response.getStatusLine());
             String page = "";
             if (entity != null) {
                 System.out.println("Response content length: " + entity.getContentLength());
                 InputStream i = entity.getContent();
                 BufferedReader br = new BufferedReader(new InputStreamReader(i));
                 String line;
                 while ((line = br.readLine()) != null) {
                 	page +=line;
                 	//System.out.println(line);
                 }
                 br.close();
                 i.close();
             }
             EntityUtils.consume(entity);
             
             String RT = getToken(page, "\"RT\" value=\"", "\" />");
             String ST = getToken(page, "\"ST\" value=\"", "\" />");
             String name = uname;
             String pword = pass;
             ////////////////////////////////////////////////////
             
             
             httpget = new HttpGet("https://weblogin.stanford.edu/login/?RT="+RT+";ST="+ST);
             System.out.println("executing request" + httpget.getRequestLine());
             response = httpclient.execute(httpget);
             entity = response.getEntity();
 
             System.out.println("----------------------------------------");
             System.out.println(response.getStatusLine());
             page = "";
             if (entity != null) {
                 System.out.println("Response content length: " + entity.getContentLength());
                 InputStream i = entity.getContent();
                 BufferedReader br = new BufferedReader(new InputStreamReader(i));
                 String line;
                 while ((line = br.readLine()) != null) {
                 	page +=line;
                 	//System.out.println(line);
                 }
                 br.close();
                 i.close();
             }
             EntityUtils.consume(entity);
             
             ////////////////////////////////////////////////////
             String url = "https://weblogin.stanford.edu/login?RT=";
             RT = RT.replaceAll("/", "%2F");
             RT = RT.replaceAll("\\+", "%20");
             RT = RT.replaceAll("=", "%3D");
             ST = ST.replaceAll("/", "%2F");
             ST = ST.replaceAll("\\+", "%20");
             ST = ST.replaceAll("=", "%3D");
             httpget = new HttpGet("https://weblogin.stanford.edu/login/?RT="+RT+";ST="+ST+";test_cookie=1");
             System.out.println("executing request" + httpget.getRequestLine());
             response = httpclient.execute(httpget);
             entity = response.getEntity();
 
             System.out.println("----------------------------------------");
             System.out.println(response.getStatusLine());
             page = "";
             if (entity != null) {
                 System.out.println("Response content length: " + entity.getContentLength());
                 InputStream i = entity.getContent();
                 BufferedReader br = new BufferedReader(new InputStreamReader(i));
                 String line;
                 while ((line = br.readLine()) != null) {
                 	page +=line;
                 	//System.out.println(line);
                 }
                 br.close();
                 i.close();
             }
             EntityUtils.consume(entity);
             
             ////////////////////////////////////////////////////
             
             
             ////////////////////////////////////////////////////
             //POST REQUEST LOGIN
             
             
             HttpPost post = new HttpPost("https://weblogin.stanford.edu/login");
             
             pairs = new ArrayList<NameValuePair>(2);
             
             
             RT = getToken(page, "\"RT\" value=\"", "\" />");
             ST = getToken(page, "\"ST\" value=\"", "\" />");
             
             pairs.add(new BasicNameValuePair("RT", RT));
             pairs.add(new BasicNameValuePair("ST", ST));
             pairs.add(new BasicNameValuePair("Submit", "Login"));
             pairs.add(new BasicNameValuePair("login", "yes"));
             pairs.add(new BasicNameValuePair("password", pword));
             pairs.add(new BasicNameValuePair("username", name));
             post.setEntity(new UrlEncodedFormEntity(pairs));
             System.out.println("executing request" + post.getRequestLine());
             HttpResponse resp = httpclient.execute(post);
             entity = resp.getEntity();
             
             System.out.println("----------------------------------------");
             System.out.println(resp.getStatusLine());
             page = "";
             if (entity != null) {
                 System.out.println("Response content length: " + entity.getContentLength());
                 InputStream i = entity.getContent();
                 BufferedReader br = new BufferedReader(new InputStreamReader(i));
                 String line;
                 while ((line = br.readLine()) != null) {
                 	page +=line;
                 	//System.out.println(line);
                 }
                 br.close();
                 i.close();
             }
             EntityUtils.consume(entity);
             
             if (page.indexOf(">Hello,") == -1)  {
             	System.out.println("Failure");
             	return null;
             } else {
             	System.out.println("Success");
             }
             
             ////////////////////////////////////////////////////////////
             //Get History
             /////////////////////////////////////////////////////////////
             
             httpget = new HttpGet("https://axess.stanford.edu/psc/pscsprd_2/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES.SSS_MY_CRSEHIST.GBL?ICElementNum=2&ICStateNum=3&ICResubmit=1&PErrKey=0.5435117224857602");
             System.out.println("executing request" + httpget.getRequestLine());
             response = httpclient.execute(httpget);
             entity = response.getEntity();
 
             System.out.println("----------------------------------------");
             System.out.println(response.getStatusLine());
             page = "";
             if (entity != null) {
                 System.out.println("Response content length: " + entity.getContentLength());
                 InputStream i = entity.getContent();
                 BufferedReader br = new BufferedReader(new InputStreamReader(i));
                 String line;
                 while ((line = br.readLine()) != null) {
                 	page +=line;
                 	//System.out.println(line);
                 }
                 br.close();
                 i.close();
             }
             EntityUtils.consume(entity);
             
             String[] courses = page.split("id='CRSE_NAME");
             for (int i=1; i<courses.length; i++) {
             	String cname = getToken(courses[i], "'>", "</span");
             	String title = getToken(courses[i],"PSHYPERLINK\' >", "<");
             	String years = getToken(courses[i],"id=\'CRSE_TERM$","-");
             	years = new String(years.substring(years.indexOf(">")+1));
             	
             	System.out.println(cname+" : "+title);
             	System.out.println(years);
             	coursesTaken.add(cname+" : "+title+SEPARATOR+years);
             }
            //dbc.writeJavaObject(uname,(ArrayList<String>) coursesTaken);
             return coursesTaken;
             
         } finally {
             // When HttpClient instance is no longer needed,
             // shut down the connection manager to ensure
             // immediate deallocation of all system resources
             httpclient.getConnectionManager().shutdown();
         }
     }
         
 	
     public static void main(String[] args) throws Exception {
         /*AxessConnect a = new AxessConnect("eaconte","mttresp1");
         
         keywordSearch k = new keywordSearch(a.getCourses(), "CS");
         k.search("cs106a", "ALL");*/
     }
     
     public static String getToken(String str, String start, String end) {
     	String Patt = start;
     	int index = str.indexOf(Patt);
     	if (index == -1)
     		return "NOT FOUND";
     	index += Patt.length();
     	String token = new String(str.substring(index, index+str.substring(index).indexOf(end)));
     	return token.trim();
     }
 }
 
