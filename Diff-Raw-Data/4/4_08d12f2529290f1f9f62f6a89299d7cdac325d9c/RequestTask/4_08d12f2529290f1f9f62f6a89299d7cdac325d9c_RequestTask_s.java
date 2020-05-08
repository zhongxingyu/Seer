 /*****************************************************
  * RequestTask
  * This method accepts a string of a valid URL request to DB.
  * It gathers the HTML request, and parses it.
  * It returns an ArrayList of Profiles of the entries.
  * ***************************************************/
 
 package edu.grinnell.appdev.grinnelldirectory;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import edu.grinnell.appdev.grinnelldirectory.dummy.Profile;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 
 
 
 public class RequestTask extends AsyncTask<String, Void, ArrayList<Profile>>{
     	
     	final public static int NO_ERROR = 0;
     	final public static int NO_ENTRIES = 1;
     	final public static int TOO_MANY_ENTRIES = 2;
     	final public static int OTHER = 3;
 
 
 	public interface ParserErrorMessage{
 	    public void setErrorMessage(int message);
 	}
 
 	String responseString; //makeRequest() stores its response here
 	ArrayList<Profile> profileList; //The final product, a list of downloaded Profile objects
 	String currentUri; //The current page content is being downloaded from
 	final String UNAVAILABLE = "This data is unavailable off-campus.";
 	
 	//In AsyncTasks, doInBackground is called first, analogous to a main method.
     protected ArrayList<Profile> doInBackground(String... uri) {
     	profileList = new ArrayList<Profile>();
     	currentUri = uri[0];
     	
     	iterativelyScrapePages();
     	return profileList;
     }
     
     	//Adds the queried entries to profileList
     private void iterativelyScrapePages(){
     	do{
     		makeRequest(); //download the next page of content
     	} while(parseResponse()); //Parse the content. If parseResponse() returns true, a next page exists.
     }
     
     //This method is a basic HTTP request. It saves the HTML response to responseString.
     private int makeRequest(String... uri){
         HttpClient httpclient = new DefaultHttpClient();
         HttpResponse response;
         try {
             response = httpclient.execute(new HttpGet(currentUri));
             StatusLine statusLine = response.getStatusLine();
             if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                 ByteArrayOutputStream out = new ByteArrayOutputStream();
                 response.getEntity().writeTo(out);
                 out.close();
                 responseString = out.toString();
                 return 0;
             } else{
                 //Closes the connection.
                 response.getEntity().getContent().close();
                 throw new IOException(statusLine.getReasonPhrase());
             }
         } catch (ClientProtocolException e) {
             //TODO Handle problems..
         } catch (IOException e) {
             //TODO Handle problems..
         }
        return -1;
     }
     
     //This method parses out entry information an HTML response, and adds Profile objects to profileList.
     //responseString must be a valid grinnell College db page
     //This method does not know how to handle the "too many entries" response and the off-campus response.
     private boolean parseResponse(){
     	
     	//Set up the tokenizer, seperating by token '\n'. You should find out what a tokenizer is.
     	StringTokenizer strTok = new StringTokenizer(responseString, "\n");
     	String curTok, picurl, firstName, lastName, username, dept, phonenum, campusaddress, boxno, stufacstatus, sgapos;
     	//boolean indicating if there exists a next page.
     	boolean anotherPage = false;
     	boolean onCampus = false;
     	
     	//skip useless information
     	for(int i=0; i<85; i++) strTok.nextToken();
     	curTok = strTok.nextToken();
     	
     	if(curTok.contains("<td")){
     	    onCampus = true;
     	    strTok.nextToken();
     	    curTok = strTok.nextToken();
     	}
     	
     	
     	//If line 88 contains these strings, there were 0 results or too many results
     	if (curTok.contains("very")) {
     	    BasicSearchFragment.tooManyResults = true;
     	    return false;
     	} else if (curTok.contains("<strong>no</strong>")) {
     	    BasicSearchFragment.noResults = true;
     	    return false;
     	}
     	else
     	{
     		
 	    	//skip useless information
 	    	for(int i=0; i<9; i++) strTok.nextToken();
 	    	curTok = strTok.nextToken();
 	    	
 	    	//If a next page button exsts, then there is a next page.
 	    	//Grab URL of next pageand set return value of method to true.
 	    	if (curTok.contains("Next Page")){
 	    		anotherPage = true;
 	    		currentUri = "https://itwebapps.grinnell.edu" + curTok.substring(53, curTok.length()-38);
 	    		for(int i=0; i<22; i++) strTok.nextToken();
 	    		curTok = strTok.nextToken();
 	    	}
 	    	else
 	    	{
 	    		anotherPage = false;
 	    		for(int i=0; i<20; i++) strTok.nextToken();
 	    		curTok = strTok.nextToken();
 	    	}
 	    	
 	    // loop, keeps adding entries to profileList until there are none.
 	    do {
 		if (onCampus) {
 		    // parse entries
 		    // parse image URL. If no image, save " ".
 		    if (curTok.contains("image1"))
 			picurl = curTok.substring(
 				curTok.indexOf("img src=\"") + 9,
 				curTok.indexOf("\" alt=\""));
 		    else
 			picurl = "";
 		    curTok = strTok.nextToken();
 		} else {
 		    picurl = "";
 		    curTok = strTok.nextToken();
 		}
 		String fullName;
 		// parse full name
 		if (onCampus) {
 		    fullName = curTok.substring(curTok.substring(40)
 			.indexOf('>') + 41,
 			curTok.substring(40).indexOf('<') + 40);
 		} else {
 		    fullName = curTok.substring(35, curTok.indexOf("</TD>"));
 		}
 		firstName = fullName.substring(0, fullName.indexOf(','));
 		lastName = fullName.substring(fullName.indexOf(',') + 2);
 		curTok = strTok.nextToken();
 
 		if (onCampus) {
 		    // parse student major or faculty department
 		    dept = curTok.substring(35, curTok.indexOf("</td>"));
 		    String smallerdeptString = curTok.substring(curTok
 			    .indexOf("tny") + 6);
 		    // some faculty/staff have multiple titles
 		    if (dept.contains("tny")) {
 			dept = facStaffTitle(dept);
 		    }
 //			dept += smallerdeptString.substring(0,
 //				smallerdeptString.indexOf("<"));
 		    curTok = strTok.nextToken();
 
 		    // parse phone number, username, campus address, box #,
 		    // student/faculty status
 		    if (curTok.charAt(37) != '<') {
 			phonenum = curTok.substring(37, 41);
 		    } else {
 			phonenum = "";
 		    }
 		} else {
 		    dept = UNAVAILABLE;
 		    phonenum = UNAVAILABLE;
 		    curTok = strTok.nextToken();
 		}
 		curTok = strTok.nextToken();
 		
 		if (!curTok.contains("&nbsp")) {
 		    username = curTok.substring(53, curTok.indexOf('@'));
 		} else {
 		    username = "";
 		}
 		strTok.nextToken();
 		curTok = strTok.nextToken();
 		
 		if (onCampus) {
 			campusaddress = curTok.substring(0,
 				curTok.indexOf("</TD>"));
 			campusaddress = campusaddress.trim();
 			curTok = strTok.nextToken();
 		    boxno = curTok.substring(36, curTok.indexOf("</TD>"));
		    if (boxno.equals("&nbs"))
			boxno = "";
 		    curTok = strTok.nextToken();
 		    stufacstatus = curTok.substring(37,
 			    curTok.indexOf(" </TD>"));
 		    strTok.nextToken();
 		} else {
 		    campusaddress = UNAVAILABLE;
 		    boxno = UNAVAILABLE;
 		    stufacstatus = UNAVAILABLE;
 		    curTok = strTok.nextToken();
 		    curTok = strTok.nextToken();
 		    curTok = strTok.nextToken();
 		}
 		curTok = strTok.nextToken();
 
 		// parse SGA status
 		sgapos = "";
 		if (curTok.equals("<tr>\r")) {
 		    // senator
 		    for (int i = 0; i < 3; i++)
 			curTok = strTok.nextToken();
 		    sgapos = curTok.substring(19, curTok.indexOf("</span>"));
 		    for (int i = 0; i < 10; i++)
 			strTok.nextToken();
 		    curTok = strTok.nextToken();
 		}
 
 		// Adds a new Profile to profileList containing all the newly
 		// parsed information
 		profileList.add(new Profile(picurl, firstName, lastName,
 			username, dept, phonenum, campusaddress, boxno,
 			stufacstatus, sgapos));
 
 	    } while (curTok.contains("&nbsp")); // determine if there is another
 						// entry to be parsed
 
 	    return anotherPage;// returns boolean indicating if there exists a
 			       // next page.
 
 	    	/*
 	    	if(anotherPage){
 	    		for(int i=0; i<6; i++) strTok.nextToken();
 	        	curTok = strTok.nextToken();
 	        	
 	        	String beginningOfURL = curTok.substring(66);
 	        	return "https://itwebapps.grinnell.edu" + beginningOfURL.substring(0, beginningOfURL.indexOf('"'));
 	        	
 	    	}
 	    	*/
     	}
     	
     	
 		
     }
     
     private String facStaffTitle(String title) {
 	    boolean inBracket = false;
 	    String tmp = "";
 	    boolean lastcharintempissemicolon = false;
 	    
 	    for (int i = 0; i < title.length(); i++) {
 		if (!inBracket && title.charAt(i) != '<') {
 		    tmp += title.charAt(i);
 		    lastcharintempissemicolon = true;
 		}
 		if (title.charAt(i) == '>') {
 		    inBracket = false;
 		    if (!lastcharintempissemicolon) tmp += ";";
 		    lastcharintempissemicolon = false;
 		}
 		if (title.charAt(i) == '<') {
 		    inBracket = true;
 		}
 		
 	    }
 	    return tmp;
 	}
     
 }
 
