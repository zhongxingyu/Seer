 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package beans;
 
 import entities.*;
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 /**
  *
  * @author wgordon
  */
 @ManagedBean(name = "ModelBean")
 @SessionScoped
 public class ModelBean {
 
     private Portal portal;
     private String APIKey;
     private String oAuth;
     private String hubID;
     private boolean error;
     private boolean oAuthOK;
     private boolean apiKeyOK;
     private boolean useOAuth;
     private HashMap<String, Lead> leads;
     private ArrayList<Lead> leadList;
     private ArrayList<Lead> twitterLeads;
     private ArrayList<Tweet> tweets;
     private String filterKey;
     private ArrayList<Blog> blogs;
     private String selectedBpId;
     private String commentMessage;
     private String commentAuthor;
     private String commentEmail;
     private boolean commentSuccess;
     private boolean commentFailure;
     private String commentClass;
     private boolean blogsGotten;
     private boolean twitterAuthorized;
     private String iframeURL;
     private boolean iframeHidden;
     private String iframeClass;
     private String twitterTarget;
     private String tweet;
     private String tweetClass;
     private String bookmarkletURL;
     private String ending;
     private double polarityVal;
     private double contrastVal, decisivenessVal, offeringGuidanceVal, flamboyanceVal, slangVal, requestingGuidanceVal;
     private String progressClass;
     private String blogPostTitle;
     private String blogPostContent;
     private String textBlock;
     private boolean emailButtonDisabled;
     private String mailtoEnding;
     private ArrayList<Blog> blogPickerArray;
     private String selectedBlogId;
     private String blogAuthorEmail;
     private String newBlogPostTitle;
     private HashMap<String, String> blogMap;
     private String blogPostSuccessMessage;
     private String blogPostFailureMessage;
     private String linkToBlog;
     private ArrayList<Lead> leadsToDisplay;
     private boolean hideEmptyLeads;
     private int filterSecondaryValue;
     /**
      * Creates a new instance of ModelBean
      */
     public ModelBean() {
         this.portal = new Portal();
         leads = new HashMap<String, Lead>();
         leadList = new ArrayList<Lead>();
         twitterLeads = new ArrayList<Lead>();
         blogs = new ArrayList<Blog>();
         twitterAuthorized = false;
         iframeHidden = true;
         iframeClass = "none!important";
         polarityVal = 0;
         emailButtonDisabled = true;
         blogMap = new HashMap<String, String>();
         leadsToDisplay = new ArrayList<Lead>();
         hideEmptyLeads = false;
         filterSecondaryValue = 0;
     }
 
     public int getFilterSecondaryValue() {
         return filterSecondaryValue;
     }
 
     public void setFilterSecondaryValue(int filterSecondaryValue) {
         this.filterSecondaryValue = filterSecondaryValue;
     }
     
     
 
     public boolean isHideEmptyLeads() {
         return hideEmptyLeads;
     }
 
     public void setHideEmptyLeads(boolean hideEmptyLeads) {
         this.hideEmptyLeads = hideEmptyLeads;
     }
 
     
     
     public ArrayList<Lead> getLeadsToDisplay() {
         return leadsToDisplay;
     }
 
     public void setLeadsToDisplay(ArrayList<Lead> leadsToDisplay) {
         this.leadsToDisplay = leadsToDisplay;
     }
 
     
     public String getLinkToBlog() {
         return linkToBlog;
     }
 
     public void setLinkToBlog(String linkToBlog) {
         this.linkToBlog = linkToBlog;
     }
 
     
     
     public String getBlogPostFailureMessage() {
         return blogPostFailureMessage;
     }
 
     public void setBlogPostFailureMessage(String blogPostFailureMessage) {
         this.blogPostFailureMessage = blogPostFailureMessage;
     }
 
     public String getBlogPostSuccessMessage() {
         return blogPostSuccessMessage;
     }
 
     public void setBlogPostSuccessMessage(String blogPostSuccessMessage) {
         this.blogPostSuccessMessage = blogPostSuccessMessage;
     }
     
     
     
 
     public String getNewBlogPostTitle() {
         return newBlogPostTitle;
     }
 
     public void setNewBlogPostTitle(String newBlogPostTitle) {
         this.newBlogPostTitle = newBlogPostTitle;
     }
 
     public HashMap<String, String> getBlogMap() {
         return blogMap;
     }
 
     public void setBlogMap(HashMap<String, String> blogMap) {
         this.blogMap = blogMap;
     }
 
     
     
     public String getBlogAuthorEmail() {
         return blogAuthorEmail;
     }
 
     public void setBlogAuthorEmail(String blogAuthorEmail) {
         this.blogAuthorEmail = blogAuthorEmail;
     }
     
     
 
     public ArrayList<Blog> getBlogPickerArray() {
         return blogPickerArray;
     }
 
     public void setBlogPickerArray(ArrayList<Blog> blogPickerArray) {
         this.blogPickerArray = blogPickerArray;
     }
 
     public String getSelectedBlogId() {
         return selectedBlogId;
     }
 
     public void setSelectedBlogId(String selectedBlogId) {
         this.selectedBlogId = selectedBlogId;
     }
     
     
 
     public String getMailtoEnding() {
         return mailtoEnding;
     }
 
     public void setMailtoEnding(String mailtoEnding) {
         this.mailtoEnding = mailtoEnding;
     }
     
     
 
     public boolean isEmailButtonDisabled() {
         return emailButtonDisabled;
     }
 
     public void setEmailButtonDisabled(boolean emailButtonDisabled) {
         this.emailButtonDisabled = emailButtonDisabled;
     }
     
     
 
     public double getDecisivenessVal() {
         return decisivenessVal;
     }
 
     public void setDecisivenessVal(double decisivenessVal) {
         this.decisivenessVal = decisivenessVal;
     }
 
     public double getFlamboyanceVal() {
         return flamboyanceVal;
     }
 
     public void setFlamboyanceVal(double flamboyanceVal) {
         this.flamboyanceVal = flamboyanceVal;
     }
 
     public double getOfferingGuidanceVal() {
         return offeringGuidanceVal;
     }
 
     public void setOfferingGuidanceVal(double offeringGuidanceVal) {
         this.offeringGuidanceVal = offeringGuidanceVal;
     }
 
     public double getPolarityVal() {
         return polarityVal;
     }
 
     public void setPolarityVal(double polarityVal) {
         this.polarityVal = polarityVal;
     }
 
     public double getRequestingGuidanceVal() {
         return requestingGuidanceVal;
     }
 
     public void setRequestingGuidanceVal(double requestingGuidanceVal) {
         this.requestingGuidanceVal = requestingGuidanceVal;
     }
 
     public double getSlangVal() {
         return slangVal;
     }
 
     public void setSlangVal(double slangVal) {
         this.slangVal = slangVal;
     }
     
     
 
     public String getTextBlock() {
         return textBlock;
     }
 
     public void setTextBlock(String textBlock) {
         this.textBlock = textBlock;
     }
     
     
 
     public String getBlogPostContent() {
         return blogPostContent;
     }
 
     public void setBlogPostContent(String blogPostContent) {
         this.blogPostContent = blogPostContent;
     }
 
     public String getBlogPostTitle() {
         return blogPostTitle;
     }
 
     public void setBlogPostTitle(String blogPostTitle) {
         this.blogPostTitle = blogPostTitle;
     }
     
     
 
     public String getProgressClass() {
         return progressClass;
     }
 
     public void setProgressClass(String progressClass) {
         this.progressClass = progressClass;
     }
     
     
 
     public double getSliderVal() {
         return polarityVal;
     }
 
     public void setSliderVal(double polarityVal) {
         this.polarityVal = polarityVal;
     }
     
     
 
     public String getEnding() {
         return ending;
     }
 
     public void setEnding(String ending) {
         this.ending = ending;
     }
 
     public String getBookmarkletURL() {
         return bookmarkletURL;
     }
 
     public void setBookmarkletURL(String bookmarkletURL) {
         this.bookmarkletURL = bookmarkletURL;
     }
 
     public String getTweetClass() {
         return tweetClass;
     }
 
     public void setTweetClass(String tweetClass) {
         this.tweetClass = tweetClass;
     }
 
     public String getTweet() {
         return tweet;
     }
 
     public void setTweet(String tweet) {
         this.tweet = tweet;
     }
 
     public String getTwitterTarget() {
         return twitterTarget;
     }
 
     public void setTwitterTarget(String twitterTarget) {
         this.twitterTarget = twitterTarget;
     }
 
     public boolean isIframeHidden() {
         return iframeHidden;
     }
 
     public void setIframeHidden(boolean iframeHidden) {
         this.iframeHidden = iframeHidden;
     }
 
     public String getIframeClass() {
         return iframeClass;
     }
 
     public void setIframeClass(String iframeClass) {
         this.iframeClass = iframeClass;
     }
 
     public String getIframeURL() {
         return iframeURL;
     }
 
     public void setIframeURL(String iframeURL) {
         this.iframeURL = iframeURL;
     }
 
     public String getCommentClass() {
         return commentClass;
     }
 
     public void setCommentClass(String commentClass) {
         this.commentClass = commentClass;
     }
 
     public boolean getCommentFailure() {
         return commentFailure;
     }
 
     public void setCommentFailure(boolean commentFailure) {
         this.commentFailure = commentFailure;
     }
 
     public boolean isCommentSuccess() {
         return commentSuccess;
     }
 
     public void setCommentSuccess(boolean commentSuccess) {
         this.commentSuccess = commentSuccess;
     }
 
     public String getCommentMessage() {
         return commentMessage;
     }
 
     public void setCommentMessage(String commentMessage) {
         this.commentMessage = commentMessage;
     }
 
     public String getCommentAuthor() {
         return commentAuthor;
     }
 
     public void setCommentAuthor(String commentAuthor) {
         this.commentAuthor = commentAuthor;
     }
 
     public String getCommentEmail() {
         return commentEmail;
     }
 
     public void setCommentEmail(String commentEmail) {
         this.commentEmail = commentEmail;
     }
 
     public String getSelectedBpId() {
         return selectedBpId;
     }
 
     public void setSelectedBpId(String selectedBpId) {
         this.selectedBpId = selectedBpId;
     }
 
     public ArrayList<Blog> getBlogs() {
         return blogs;
     }
 
     public void setBlogs(ArrayList<Blog> blogs) {
         this.blogs = blogs;
     }
 
     public String getFilterKey() {
         return filterKey;
     }
 
     public void setFilterKey(String filterKey) {
         this.filterKey = filterKey;
     }
 
     public ArrayList<Tweet> getTweets(Lead lead) {
         return lead.getTweets();
     }
 
     public String getAPIKey() {
         return APIKey;
     }
 
     public void setAPIKey(String APIKey) {
         this.APIKey = APIKey;
         portal.setApiKey(APIKey);
     }
 
     public String getoAuth() {
         return oAuth;
     }
 
     public void setoAuth(String oAuth) {
         this.oAuth = oAuth;
         portal.setOauthToken(oAuth);
     }
 
     public String getHubID() {
         return hubID;
     }
 
     public void setHubID(String hubID) {
         this.hubID = hubID;
         portal.setPortalID(hubID);
     }
 
     public boolean isError() {
         return error;
     }
 
     public void setError(boolean error) {
         this.error = error;
     }
 
     public HashMap<String, Lead> getLeads() {
         return leads;
     }
 
     public void setLeads(HashMap<String, Lead> leads) {
         this.leads = leads;
     }
 
     public ArrayList<Lead> getLeadList() {
         return leadList;
     }
 
     public ArrayList<Lead> getTwitterLeads() {
         return twitterLeads;
     }
 
     public void setTwitterLeads(ArrayList<Lead> twitterLeads) {
         this.twitterLeads = twitterLeads;
     }
 
     public String checkAuth() {
 
         try {
             URL url = new URL("https://api.hubapi.com/leads/v1/list?access_token=" + portal.getOauthToken());
             HttpURLConnection oAuthConn = (HttpURLConnection) url.openConnection();
             oAuthConn.setRequestMethod("GET");
             if (oAuthConn.getResponseCode() < 300) {
                 oAuthOK = true;
                 useOAuth = true;
             } else {
                 oAuthOK = false;
                 // error = true; 
             }
             System.out.println("OAuth Response:" + oAuthConn.getResponseCode());
 
             URL url2 = new URL("https://api.hubapi.com/leads/v1/list?portalId=" + portal.getPortalID() + "&hapikey=" + portal.getApiKey());
             HttpURLConnection apiConn = (HttpURLConnection) url2.openConnection();
             apiConn.setRequestMethod("GET");
             if (apiConn.getResponseCode() < 300) {
                 apiKeyOK = true;
             } else {
                 apiKeyOK = false;
                 //error = true;
             }
             System.out.println("APIkey response:" + apiConn.getResponseCode());
 
             boolean allOK = apiKeyOK || oAuthOK;
             if (allOK) {
                 return "twitpage";
             } else {
                 error = true;
             }
             return null;
 
         } catch (Exception e) {
         }
         return "index";
     }
 
     public void getTheLeads() {
         if (leads.isEmpty()) {
             getCompanyName();
             try {
                 URL url;
                 if (useOAuth) {
                     url = new URL("https://api.hubapi.com/leads/v1/list?access_token=" + portal.getOauthToken() + "&max=25");
                 } else {
                     url = new URL("https://api.hubapi.com/leads/v1/list?portalId=" + portal.getPortalID() + "&hapikey=" + portal.getApiKey() + "&max=25");
                 }
                 System.err.println(url.toString());
                 HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                 conn.setRequestMethod("GET");
                 conn.setDoOutput(true);
                 conn.connect();
 
                 System.out.println(conn.getResponseCode());
                 InputStream response = conn.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(response));
                 StringBuilder sb = new StringBuilder();
                 String line;
 
                 while ((line = br.readLine()) != null) {
                     sb.append(line);
                 }
                 String message = sb.toString();
                 String[] strArray = message.split("\"\\},\\{");
                 strArray[0].concat("\\}");
                 strArray[0] = strArray[0].substring(1);
 
                 for (int i = 0; i < strArray.length; i++) {
                     strArray[i] = "{".concat(strArray[i]);
                     strArray[i] = strArray[i].concat("\"}");
                 }
                 strArray[0] = strArray[0].substring(1);
                 strArray[strArray.length - 1] = strArray[strArray.length - 1].substring(0, strArray[strArray.length - 1].length() - 3);
                 ArrayList<JSONObject> JSONobjs = new ArrayList();
 
                 for (int i = 0; i < strArray.length; i++) {
                     try {
                         System.out.println("Lead String:" + strArray[i]);
                         JSONTokener jtoke = new JSONTokener(strArray[i]);
                         System.out.println(jtoke.toString());
                         JSONobjs.add(new JSONObject(jtoke));
 
                         Lead leadX = new Lead();
                         System.out.println("made objs");
                         leadX.setFirstName((String) JSONobjs.get(i).get("firstName"));
                         System.out.println("assigned first name " + leadX.getFirstName());
                         leadX.setLastName((String) JSONobjs.get(i).get("lastName"));
                         leadX.setEmail((String) JSONobjs.get(i).get("email"));
                         leadX.setGuid((String) JSONobjs.get(i).get("guid"));
                         leadX.setUserToken((String) JSONobjs.get(i).get("userToken"));
                         System.out.println("assigned values besides twitter");
                         leadX.setTwitter((String) JSONobjs.get(i).get("twitterHandle"));
                         System.out.println("twitter");
                         leads.put(leadX.getGuid(), leadX);
                         System.out.println("put in map");
                         leadList.add(leadX);
                         if (!"".equals(leadX.getTwitter())) {
                             twitterLeads.add(leadX);
                             getTheTweets(leadX);
                         }
                         System.out.println("put lead in arraylist");
                         //leadArray.add(leadX);
                         System.out.println(leadX.getFirstName());
                         System.out.println(leadX.getTwitter());
                     } catch (Exception e) {
                         System.out.println("getting leads error:" + e);
                     }
                 }
                 filterTweets();
             } catch (Exception e) {
                 System.out.println("getting leads erro2:" + e);
                 e.printStackTrace();
             }
         } else {
             return;
         }
     }
 
     public void getTwitterLeadList() {
 
         if (leadList.isEmpty()) {
             getCompanyName();
             System.out.println("twitter list called");
             int correctListId = 0;
             boolean foundList = false;
             String leadJSON = "";
             URL url;
 
 
             try {
                 if (useOAuth) {
                     url = new URL(" https://api.hubapi.com/contacts/v1/lists/dynamic?" + portal.getOauthToken());
                 } else {
                     url = new URL(" https://api.hubapi.com/contacts/v1/lists/dynamic?portalId=" + portal.getPortalID() + "&hapikey=" + portal.getApiKey());
                 }
                 HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                 conn.setRequestMethod("GET");
                 conn.setDoOutput(true);
                 conn.connect();
 
                 System.out.println(conn.getResponseCode());
                 InputStream response = conn.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(response));
                 StringBuilder sb = new StringBuilder();
                 String line;
 
                 while ((line = br.readLine()) != null) {
                     sb.append(line);
                 }
                 System.err.println("list JSON: " + sb.toString());
                 JSONObject lists = new JSONObject(sb.toString());
                 System.err.println("made list json obj");
                 JSONArray listsList = lists.getJSONArray("lists");
                 System.err.println("made list json array");
                 System.out.println("all the lists: " + listsList);
                 for (int i = 0; i < listsList.length(); i++) {
                     System.err.println("checking lists: " + listsList.getJSONObject(i).getString("name"));
                     if (listsList.getJSONObject(i).getString("name").equals("Hubspot-Tweeters")) {
                         correctListId = listsList.getJSONObject(i).getInt("listId");
                         System.err.println("found list");
                         foundList = true;
                     }
                 }
 
 
 
 
                 if (!foundList) {
                     System.out.println("didn't find the list");
                     if (useOAuth) {
                         url = new URL("https://api.hubapi.com/contacts/v1/lists?" + portal.getOauthToken());
                     } else {
                         url = new URL("https://api.hubapi.com/contacts/v1/lists?portalId=" + portal.getPortalID() + "&hapikey=" + portal.getApiKey());
                     }
                     System.out.println(url);
                     String json = "{\"name\":\"Hubspot-Tweeters\","
                             + "\"dynamic\": true,"
                             + "\"portalId\":\"" + portal.getPortalID() + "\","
                             + "\"filters\":"
                             + "["
                             + "[{"
                             + "\"operator\":\"IS_NOT_EMPTY\","
                             + "\"property\":\"twitterhandle\","
                             + "\"type\":\"string\""
                             + "}]"
                             + "]"
                             + "}";
                     System.out.println(json);
                     conn = (HttpURLConnection) url.openConnection();
                     conn.setDoOutput(true);
                     conn.setDoInput(true);
                     conn.setRequestMethod("POST");
                     conn.setRequestProperty("Content-Type", "application/json");
                     //conn.setRequestProperty("Content-Length", Integer.toString(json.length()));
                     conn.setUseCaches(false);
                     System.out.println("connection made");
                     DataOutputStream osw = new DataOutputStream(conn.getOutputStream());
                     osw.writeBytes(json);
                     osw.flush();
                     osw.close();
                     System.err.println("waiting for response");
                     System.err.println(conn.getResponseCode());
 
                     response = conn.getInputStream();
                     br = new BufferedReader(new InputStreamReader(response));
                     sb = new StringBuilder();
                     line = "";
                     while ((line = br.readLine()) != null) {
                         sb.append(line);
                     }
                     String message = sb.toString();
 
                     System.out.println("about to make JSON");
                     JSONTokener toke = new JSONTokener(message);
                     System.out.println("Made tokener");
                     JSONObject fullResp = new JSONObject(toke);
                     System.out.println("Made object");
                     System.out.println("list response" + fullResp);
                     System.err.println("list ID: " + fullResp.get("listId"));
                     System.err.println("list status: " + fullResp.getJSONObject("metaData").get("processing"));
                     int listId = fullResp.getInt("listId");
                     System.err.println("made list ID int");
                     String status = (String) fullResp.getJSONObject("metaData").get("processing");
                     System.err.println("made status string");
                     System.err.println(status);
                     System.err.println(status.equals(new String("INITIALIZING")));
                     if (status.equals(new String("INITIALIZING"))) {
                         System.out.println("waiting");
                         try {
                             Thread.sleep(4000L);
                         } catch (Exception e) {
                         }
                         if (useOAuth) {
                             url = new URL("https://api.hubapi.com/contacts/v1/lists/" + listId + "?" + portal.getOauthToken());
                         } else {
                             url = new URL("https://api.hubapi.com/contacts/v1/lists/" + listId + "?portalId=" + portal.getPortalID() + "&hapikey=" + portal.getApiKey());
                         }
                         System.err.println(url.toString());
                         conn = (HttpURLConnection) url.openConnection();
                         conn.setRequestMethod("GET");
                         conn.setDoOutput(true);
                         conn.connect();
 
                         System.out.println(conn.getResponseCode());
                         response = conn.getInputStream();
                         br = new BufferedReader(new InputStreamReader(response));
                         sb = new StringBuilder();
                         System.out.println(conn.getResponseCode());
                         line = "";
                         while ((line = br.readLine()) != null) {
                             sb.append(line);
                         }
                         toke = new JSONTokener(message);
                         fullResp = new JSONObject(toke);
                         status = (String) fullResp.getJSONObject("metaData").get("processing");
                         System.out.print("updated status to: " + status);
                         System.out.println(status);
                     }
                     leadJSON = sb.toString();
                     correctListId = listId;
                     //System.out.println("Finished processing list: " + leadJSON);
 
                 } else {
                     if (useOAuth) {
                         url = new URL("https://api.hubapi.com/contacts/v1/lists/" + correctListId + "?" + portal.getOauthToken());
                     } else {
                         url = new URL("https://api.hubapi.com/contacts/v1/lists/" + correctListId + "?portalId=" + portal.getPortalID() + "&hapikey=" + portal.getApiKey());
                     }
                     System.err.println(url.toString());
                     conn = (HttpURLConnection) url.openConnection();
                     conn.setRequestMethod("GET");
                     conn.setDoOutput(true);
                     conn.connect();
 
                     System.out.println(conn.getResponseCode());
                     response = conn.getInputStream();
                     br = new BufferedReader(new InputStreamReader(response));
                     sb = new StringBuilder();
 
                     line = "";
                     while ((line = br.readLine()) != null) {
                         sb.append(line);
                     }
                     JSONTokener toke = new JSONTokener(sb.toString());
                     JSONObject fullResp = new JSONObject(toke);
                     leadJSON = sb.toString();
                 }
 
                 System.out.println("output: " + leadJSON);
                 try {
                     if (useOAuth) {
                         url = new URL(" https://api.hubapi.com/contacts/v1/lists/" + correctListId + "/contacts/all?property=twitterhandle&property=firstname&property=lastname&property=email&" + portal.getOauthToken());
                     } else {
                         url = new URL("  https://api.hubapi.com/contacts/v1/lists/" + correctListId + "/contacts/all?property=twitterhandle&property=firstname&property=lastname&property=email&portalId=" + portal.getPortalID() + "&hapikey=" + portal.getApiKey());
                     }
                     System.err.println(url.toString());
                     conn = (HttpURLConnection) url.openConnection();
                     conn.setRequestMethod("GET");
                     conn.setDoOutput(true);
                     conn.connect();
 
                     System.out.println(conn.getResponseCode());
                     response = conn.getInputStream();
                     br = new BufferedReader(new InputStreamReader(response));
                     sb = new StringBuilder();
                     System.out.println(conn.getResponseCode());
                     line = "";
                     while ((line = br.readLine()) != null) {
                         sb.append(line);
                     }
                     JSONTokener toke = new JSONTokener(sb.toString());
                     JSONObject fullResp = new JSONObject(toke);
                     System.out.println("All the contacts: " + fullResp.toString());
                     JSONArray contactArray = fullResp.getJSONArray("contacts");
                     for (int i = 0; i < contactArray.length(); i++) {
                         Lead leadx = new Lead();
                         leadx.setEmail(contactArray.getJSONObject(i).getJSONObject("properties").getJSONObject("email").getString("value"));
                         leadx.setFirstName(contactArray.getJSONObject(i).getJSONObject("properties").getJSONObject("firstname").getString("value"));
                         leadx.setLastName(contactArray.getJSONObject(i).getJSONObject("properties").getJSONObject("lastname").getString("value"));
                         leadx.setTwitter(contactArray.getJSONObject(i).getJSONObject("properties").getJSONObject("twitterhandle").getString("value"));
                         leadx.setGuid(String.valueOf(contactArray.getJSONObject(i).getInt("vid")));
                         leadList.add(leadx);
                         twitterLeads.add(leadx);
                         getTheTweets(leadx);
                     }
                     filterTweets();
 
                 } catch (Exception e) {
                     System.out.println(e.toString());
                 }
 
 
 
 
 
             } catch (Exception e) {
             }
         } else {
             return;
         }
     }
 
     public void getTheTweets(Lead lead) {
         try {
             URL url = new URL("http://search.twitter.com/search.json?q=from:" + lead.getTwitter() + "&results_type=recent&include_entities=true");
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             conn.setDoOutput(true);
             conn.connect();
 
             System.out.println(conn.getResponseCode());
             InputStream response = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(response));
             StringBuilder sb = new StringBuilder();
             String line;
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
             String message = sb.toString();
 
             JSONTokener toke = new JSONTokener(message);
             JSONObject fullResp = new JSONObject(toke);
             System.out.println(fullResp);
             JSONArray tweets = (JSONArray) fullResp.get("results");
 
 
             ArrayList<JSONObject> tweetList = new ArrayList<JSONObject>();
 
             if (!tweets.equals("")) {
                 for (int i = 0; i < tweets.length(); i++) {
                     if (!tweets.getJSONObject(i).getJSONObject("entities").getJSONArray("urls").isNull(0)) {
                         String link = tweets.getJSONObject(i).getJSONObject("entities").getJSONArray("urls").getJSONObject(0).getString("expanded_url");
                         Tweet t = new Tweet(tweets.getJSONObject(i).getString("text"), link);
                         doAnalysis(t);
                         lead.addTweet(t);
                         System.out.println("tweet:" + lead.getTweets().get(i).getMsg());
                         System.out.println("url:" + lead.getTweets().get(i).getUrl());
                     } else {
                         Tweet t = new Tweet(tweets.getJSONObject(i).getString("text"), "");
                         doAnalysis(t);
                         lead.addTweet(t);
                         System.out.println("tweet:" + lead.getTweets().get(i).getMsg());
                         System.out.println("url:" + lead.getTweets().get(i).getUrl());
                     }
                 }
             }
 
         } catch (Exception e) {
             System.err.println("getting tweets error:" + e.toString());
         }
     }
 
     public void getCompanyName() {
         try {
             URL url;
             if (useOAuth) {
                 url = new URL("https://api.hubapi.com/settings/v1/settings?access_token=" + portal.getOauthToken() + "&domains=true&sm=true&readOnly=true");
             } else {
                 url = new URL("https://api.hubapi.com/settings/v1/settings?portalId=" + portal.getPortalID() + "&hapikey=" + portal.getApiKey() + "&domains=true&sm=true&readOnly=true");
             }
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             conn.setDoOutput(true);
             conn.connect();
 
             System.out.println(conn.getResponseCode());
             InputStream response = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(response));
             StringBuilder sb = new StringBuilder();
             String line;
 
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
 
             JSONArray arr = new JSONArray(sb.toString());
             System.out.println("Settings:" + arr.toString());
             JSONArray readOnlyArray = null;
             JSONObject nameRec;
             for (int i = 0; i < arr.length(); i++) {
                 System.err.println(arr.getJSONObject(i));
                 if (new String("readOnly").equals((String) arr.getJSONObject(i).getString("name"))) {
                     System.out.println("found array");
 
                     try {
                         readOnlyArray = new JSONArray(arr.getJSONObject(i).getJSONArray("value").toString());
                         System.out.println("made obj: " + readOnlyArray.toString());
                     } catch (Exception e) {
                         System.out.println("Company name error:" + e.toString());
                     }
                 }
             }
             for (int i = 0; i < readOnlyArray.length(); i++) {
                 if (readOnlyArray.getJSONObject(i).getString("name").equals("companyName")) {
                     portal.setCompanyName(readOnlyArray.getJSONObject(i).getString("value"));
                     filterKey = portal.getCompanyName();
                     System.out.println("name:" + portal.getCompanyName());
                 }
             }
 
         } catch (Exception e) {
         }
     }
 
     public void filterTweets() {
         System.out.println("filter called: " + filterKey);
         String holder = "";
         for (int i = 0; i < twitterLeads.size(); i++) {
             ArrayList<Tweet> tempList = new ArrayList<Tweet>();
             for (int j = 0; j < twitterLeads.get(i).getTweets().size(); j++) {
                 System.err.println("tweet: " + twitterLeads.get(i).getTweets().get(j).getMsg());
                 if (twitterLeads.get(i).getTweets().get(j).getMsg().toLowerCase().contains(filterKey.toLowerCase()) || twitterLeads.get(i).getTweets().get(j).getUrl().toLowerCase().contains(filterKey.toLowerCase())) {
                     tempList.add(twitterLeads.get(i).getTweets().get(j));
                     System.out.println("Tweet CSS class: " + twitterLeads.get(i).getTweets().get(j).getCssClass());
                     holder = holder.concat(twitterLeads.get(i).getTweets().get(j).getCssClass());
 
                     System.out.println("Holder: " + holder);
                     //twitterLeads.get(i).addFilteredTweet(twitterLeads.get(i).getTweets().get(j));
                     // System.out.println("filtered for: "+twitterLeads.get(i).getFirstName());
                     //System.out.println("filt tweet: "+twitterLeads.get(i).getFilteredTweets().get(j).getMsg());
                 }
             }
             twitterLeads.get(i).setFilteredTweets(tempList);
             twitterLeads.get(i).setTweetCssClasses(holder);
             filterSecondary();
             filterLeads();
             /*
              * for(int
              * k=0;k<twitterLeads.get(i).getFilteredTweets().size();k++){
              * if(!twitterLeads.get(i).getFilteredTweets().get(k).getMsg().contains(filterKey)){
              * twitterLeads.get(i).removeFTweet(k); }
              * if(twitterLeads.get(i).getFilteredTweets().size()>1&&k>0){
              * if(twitterLeads.get(i).getFilteredTweets().get(k).getMsg().equals(twitterLeads.get(i).getFilteredTweets().get(k-1).getMsg())){
              * twitterLeads.get(i).removeFTweet(k-1); } }
              *
              * }
              */
 
         }
     }
     
     public void filterSecondary(){
         if(filterSecondaryValue>0){
             for(int i=0; i<twitterLeads.size(); i++){
                 ArrayList<Tweet> tempList = new ArrayList<Tweet>();
                 for (int j = 0; j < twitterLeads.get(i).getFilteredTweets().size(); j++) {
                     if(twitterLeads.get(i).getFilteredTweets().get(j).getRequestingGuidanceVal()>=40.0&&filterSecondaryValue==1){
                         tempList.add(twitterLeads.get(i).getFilteredTweets().get(j));
                         System.out.println("qualifying tweet");
                     }
                     else if(twitterLeads.get(i).getFilteredTweets().get(j).isIsPositive()&&filterSecondaryValue==2){
                         tempList.add(twitterLeads.get(i).getFilteredTweets().get(j));
                         System.out.println("qualifying tweet");
                     }
                     else if((!twitterLeads.get(i).getFilteredTweets().get(j).isIsPositive())&&filterSecondaryValue==3){
                         tempList.add(twitterLeads.get(i).getFilteredTweets().get(j));
                         System.out.println("qualifying tweet");
                     }
                     else
                         System.err.println("not req guid");
                 }
                 twitterLeads.get(i).setFilteredTweets(tempList);
             }
         }
     }
    
     public void filterLeads(){
         System.out.println("filter leads called");
         ArrayList<Lead> tempList = new ArrayList<Lead>();
         for(Lead lead : twitterLeads){
             System.out.println("Checking lead");
             if(lead.getTweets().size()>0&&hideEmptyLeads){
                 tempList.add(lead);
             }
             else if(!hideEmptyLeads)
                 tempList.add(lead);
         }
         leadsToDisplay = tempList;
     }
     public void getTheBlogs() {
         try {
             if (blogsGotten) {
                 return;
             }
             URL url;
             if (useOAuth) {
                 url = new URL("https://api.hubapi.com/blog/v1/list.json?access_token=" + portal.getOauthToken());
             } else {
                 url = new URL("https://api.hubapi.com/blog/v1/list.json?portalId=" + portal.getPortalID() + "&hapikey=" + portal.getApiKey());
             }
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             conn.setDoOutput(true);
             conn.connect();
 
             System.out.println(conn.getResponseCode());
             InputStream response = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(response));
             StringBuilder sb = new StringBuilder();
             String line;
 
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
             JSONArray arr = new JSONArray(sb.toString());
 
             for (int i = 0; i < arr.length(); i++) {
                 Blog b = new Blog();
                 System.out.println(arr.getJSONObject(i).toString());
                 b.setTitle(arr.getJSONObject(i).getString("blogTitle"));
                 System.out.println("title: " + b.getTitle());
                 if (b.getTitle() == null) {
                     b.setTitle(arr.getJSONObject(i).getString("webUrl"));
                 }
                 b.setId(arr.getJSONObject(i).getString("guid"));
                 b.setURL(arr.getJSONObject(i).getString("webUrl"));
                 getThePosts(b);
                 blogs.add(b);
                 blogMap.put(b.getURL(), b.getId());
             }
             blogsGotten = true;
 
         } catch (Exception e) {
             System.out.println(e.toString());
         }
     }
 
     public void getThePosts(Blog b) {
         try {
             URL url;
             if (useOAuth) {
                 url = new URL("https://api.hubapi.com/blog/v1/" + b.getId() + "/posts.json?access_token=" + portal.getOauthToken());
             } else {
                 url = new URL("https://api.hubapi.com/blog/v1/" + b.getId() + "/posts.json?portalId=" + portal.getPortalID() + "&hapikey=" + portal.getApiKey());
             }
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             conn.setDoOutput(true);
             conn.connect();
 
             System.out.println(conn.getResponseCode());
             InputStream response = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(response));
             StringBuilder sb = new StringBuilder();
             String line;
 
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
             JSONArray arr = new JSONArray(sb.toString());
             for (int i = 0; i < arr.length(); i++) {
                 if (!arr.getJSONObject(i).getBoolean("draft") && arr.getJSONObject(i).get("postAnalytics") != null) {
                     //System.out.println("bp:"+arr.getJSONObject(i).toString());
                     if (arr.getJSONObject(i).getJSONObject("postAnalytics").getInt("comments") > 0) {
                         System.out.println("bp: " + arr.getJSONObject(i).toString());
                         BlogPost bp = new BlogPost();
                         bp.setId(arr.getJSONObject(i).getString("guid"));
                         bp.setTimeStamp(arr.getJSONObject(i).getLong("createTimestamp"));
                         bp.setDate(new Date(bp.getTimeStamp()));
                         bp.setTitle(arr.getJSONObject(i).getString("title"));
                         bp.setUrl(arr.getJSONObject(i).getString("url"));
                         b.addAPost(bp);
 
                         getTheComments(bp);
                     }
                 }
             }
 
 
 
         } catch (Exception e) {
             System.out.println(e.toString());
         }
     }
 
     public void getTheComments(BlogPost bp) {
         try {
             URL url;
             if (useOAuth) {
                 url = new URL("https://api.hubapi.com/blog/v1/posts/" + bp.getId() + "/comments.json?access_token=" + portal.getOauthToken());
             } else {
                 url = new URL("https://api.hubapi.com/blog/v1/posts/" + bp.getId() + "/comments.json?portalId=" + portal.getPortalID() + "&hapikey=" + portal.getApiKey());
             }
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             conn.setDoOutput(true);
             conn.connect();
 
             System.out.println(conn.getResponseCode());
             InputStream response = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(response));
             StringBuilder sb = new StringBuilder();
             String line;
 
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
             JSONArray arr = new JSONArray(sb.toString());
             for (int i = 0; i < arr.length(); i++) {
                 System.out.println(arr.getJSONObject(i).toString());
                 Comment c = new Comment();
                 c.setMsg(arr.getJSONObject(i).getString("comment"));
                 c.setName(arr.getJSONObject(i).getString("anonyName"));
                 c.setEmail(arr.getJSONObject(i).getString("anonyEmail"));
                 c.setDate(new Date(arr.getJSONObject(i).getLong("createTimestamp")));
                 bp.addComment(c);
                 doAnalysis(c, bp);
             }
         } catch (Exception e) {
             System.err.println(e.toString());
         }
     }
 
     public void doAnalysis(Comment c, BlogPost bp) {
         try {
             String sample = c.getMsg();
             System.out.println("Analysis called");
             String url_args = URLEncoder.encode("apiKey", "UTF-8") + "=" + URLEncoder.encode("cwhjxmnymkvxec3w874fb3snx9m5rjsv", "UTF-8");
             url_args += "&" + URLEncoder.encode("analysis", "UTF-8") + "=" + URLEncoder.encode("all", "UTF-8");
             url_args += "&" + URLEncoder.encode("outputFormat", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8");
             url_args += "&" + URLEncoder.encode("inputText", "UTF-8") + "=" + URLEncoder.encode(sample, "UTF-8");
 
             URL url = new URL("http://portaltnx20.openamplify.com/AmplifyWeb_v21/AmplifyThis");
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setDoOutput(true);
             OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
             wr.write(url_args);
             wr.flush();
             InputStream response = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(response));
             StringBuilder sb = new StringBuilder();
             String line;
 
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
             JSONObject obj = new JSONObject(sb.toString());
             int tone = obj.getJSONObject("ns1:AmplifyResponse").getJSONObject("AmplifyReturn").getJSONObject("Styles").getJSONObject("Polarity").getJSONObject("Mean").getInt("Value");
             System.out.println("tone: " + tone);
             c.setSliderVal(Math.abs(tone*100));
             if (tone > 0) {
                 bp.appendCString("commentPos,");
                 c.setProgressClass("progress-success");
             } else if (tone < 0) {
                 bp.appendCString("commentNeg,");
                 c.setProgressClass("progress-danger");
             } else {
                 bp.appendCString("normColumn,");
             }
 
             System.out.println("Analysis:" + sb.toString());
 
         } catch (Exception e) {
             System.err.println(e.toString());
         }
     }
 
     public void doAnalysis(Tweet t) {
         try {
             String sample = t.getMsg();
             System.out.println("Tweet Analysis called");
             String url_args = URLEncoder.encode("apiKey", "UTF-8") + "=" + URLEncoder.encode("cwhjxmnymkvxec3w874fb3snx9m5rjsv", "UTF-8");
             url_args += "&" + URLEncoder.encode("analysis", "UTF-8") + "=" + URLEncoder.encode("all", "UTF-8");
             url_args += "&" + URLEncoder.encode("outputFormat", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8");
             url_args += "&" + URLEncoder.encode("inputText", "UTF-8") + "=" + URLEncoder.encode(sample, "UTF-8");
 
             URL url = new URL("http://portaltnx20.openamplify.com/AmplifyWeb_v21/AmplifyThis");
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setDoOutput(true);
             OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
             wr.write(url_args);
             wr.flush();
             InputStream response = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(response));
             StringBuilder sb = new StringBuilder();
             String line;
 
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
             JSONObject obj = new JSONObject(sb.toString());
             double tone = obj.getJSONObject("ns1:AmplifyResponse").getJSONObject("AmplifyReturn").getJSONObject("Styles").getJSONObject("Polarity").getJSONObject("Mean").getDouble("Value");
             double reqGuid = obj.getJSONObject("ns1:AmplifyResponse").getJSONObject("AmplifyReturn").getJSONObject("Styles").getJSONObject("RequestingGuidance").getDouble("Value")*20;
             System.out.println("tone: " + tone);
             t.setSliderVal(Math.abs(tone*100));
             t.setRequestingGuidanceVal(reqGuid);
             if (tone > 0) {
                 t.setCssClass("commentPos,");
                 t.setProgressClass("progress-success");
                 t.setIsPositive(true);
             } else if (tone < 0) {
                 t.setCssClass("commentNeg,");
                 t.setProgressClass("progress-danger");
                 t.setIsPositive(false);
             } else {
                 t.setCssClass("normColumn,");
                 t.setIsPositive(false);
             }
 
             System.out.println("Analysis:" + t.getCssClass());
 
         } catch (Exception e) {
             System.err.println(e.toString());
         }
 
     }
 
     public String commentOnBlog(String blogId) {
         selectedBpId = blogId;
         commentSuccess = false;
         commentFailure = false;
         resetProgress();
         return "blogCommentPost";
     }
 
     public void postTheComment() {
         try {
             URL url;
             if (useOAuth) {
                 url = new URL("https://api.hubapi.com/blog/v1/posts/" + selectedBpId + "/comments.atom?access_token=" + portal.getOauthToken());
             } else {
                 url = new URL("https://api.hubapi.com/blog/v1/posts/" + selectedBpId + "/comments.atom?portalId=" + portal.getPortalID() + "&hapikey=" + portal.getApiKey());
             }
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("POST");
             conn.setRequestProperty("Content-Type", "application/atom+xml");
             conn.setDoOutput(true);
             conn.setUseCaches(false);
             conn.setDoInput(true);
             //conn.connect();
             String message = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                     + "<entry xmlns=\"http://www.w3.org/2005/Atom\">"
                     + "<author>"
                     + "<name>" + commentAuthor + "</name>"
                     + "<email>" + commentEmail + "</email>"
                     + "</author>"
                     + "<content>" + commentMessage + "</content>"
                     + "</entry></xml>";
             conn.setRequestProperty("Content-Length", Integer.toString(message.length()));
             DataOutputStream osw = new DataOutputStream(conn.getOutputStream());
             osw.writeBytes(message);
             osw.flush();
             System.out.println(conn.getResponseCode());
             InputStream response = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(response));
             StringBuilder sb = new StringBuilder();
             String line;
 
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
             if (conn.getResponseCode() < 400) {
                 commentSuccess = true;
             } else {
                 commentFailure = true;
             }
             JSONArray arr = new JSONArray(sb.toString());
         } catch (Exception e) {
             System.out.println("Comment post error: " + e.toString());
         }
 
     }
 
     public void commentAnalysis() {
         try {
             String comment = commentMessage;
             System.out.println("Tweet Analysis called");
             String url_args = URLEncoder.encode("apiKey", "UTF-8") + "=" + URLEncoder.encode("cwhjxmnymkvxec3w874fb3snx9m5rjsv", "UTF-8");
             url_args += "&" + URLEncoder.encode("analysis", "UTF-8") + "=" + URLEncoder.encode("all", "UTF-8");
             url_args += "&" + URLEncoder.encode("outputFormat", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8");
             url_args += "&" + URLEncoder.encode("inputText", "UTF-8") + "=" + URLEncoder.encode(comment, "UTF-8");
 
             URL url = new URL("http://portaltnx20.openamplify.com/AmplifyWeb_v21/AmplifyThis");
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setDoOutput(true);
             OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
             wr.write(url_args);
             wr.flush();
             InputStream response = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(response));
             StringBuilder sb = new StringBuilder();
             String line;
 
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
             JSONObject obj = new JSONObject(sb.toString());
             double tone = obj.getJSONObject("ns1:AmplifyResponse").getJSONObject("AmplifyReturn").getJSONObject("Styles").getJSONObject("Polarity").getJSONObject("Mean").getDouble("Value");
             System.out.println("tone: " + tone);
             polarityVal = Math.abs(tone*100);
             if (tone > 0) {
                 commentClass = "commentPos";
                 progressClass = "progress-success";
             } else if (tone < 0) {
                 commentClass = "commentNeg";
                 progressClass = "progress-danger";
             } else {
                 commentClass = "commentNorm";
             }
 
             System.out.println("Analysis:" + commentClass);
 
         } catch (Exception e) {
             System.err.println(e.toString());
         }
     }
 
     public void tweetAt(Lead lead) {
         if (!twitterAuthorized) {
             signIntoTwitter();
         }
 
     }
 
     public void signIntoTwitter() {
         try {
             URL url = new URL("https://api.twitter.com/oauth/request_token");
 
         } catch (Exception e) {
             System.out.println(e.toString());
         }
     }
 
     public void tweetBack() {
         try {
 
             URL url = new URL("https://app.hubspot.com/social/" + portal.getPortalID() + "/publishing/compose_bookmarklet?body=test");
             //String bookmarkletURL = "https://app.hubspot.com/social/" + portal.getPortalID() + "/publishing/compose_bookmarklet?body=" + ending;
             //System.out.println("Tweet url: "+bookmarkletURL);
             //iframeURL = bookmarkletURL;
             iframeHidden = false;
             iframeClass = "block!important";
         } catch (Exception e) {
             System.out.println(e.toString());
         }
     }
 
     public String makeTweet(String twitterHandle) {
         twitterTarget = "@" + twitterHandle;
         tweet = twitterTarget;
         try {
             ending = URLEncoder.encode(tweet, "UTF-8");
             ending = ending.replaceAll("\\+", "%20");
             bookmarkletURL = "https://app.hubspot.com/social/" + portal.getPortalID() + "/publishing/compose_bookmarklet?body=" + ending;
         } catch (Exception e) {
             System.out.println(e.toString());
         }
         resetProgress();
         return "maketweet";
     }
 
     public void tweetAnalysis() {
         System.out.println("Tweet Analysis called");
         try {
             String url_args = URLEncoder.encode("apiKey", "UTF-8") + "=" + URLEncoder.encode("cwhjxmnymkvxec3w874fb3snx9m5rjsv", "UTF-8");
             url_args += "&" + URLEncoder.encode("analysis", "UTF-8") + "=" + URLEncoder.encode("all", "UTF-8");
             url_args += "&" + URLEncoder.encode("outputFormat", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8");
             url_args += "&" + URLEncoder.encode("inputText", "UTF-8") + "=" + URLEncoder.encode(tweet, "UTF-8");
 
             URL url = new URL("http://portaltnx20.openamplify.com/AmplifyWeb_v21/AmplifyThis");
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setDoOutput(true);
             OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
             wr.write(url_args);
             wr.flush();
             InputStream response = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(response));
             StringBuilder sb = new StringBuilder();
             String line;
 
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
             JSONObject obj = new JSONObject(sb.toString());
             double tone = obj.getJSONObject("ns1:AmplifyResponse").getJSONObject("AmplifyReturn").getJSONObject("Styles").getJSONObject("Polarity").getJSONObject("Mean").getDouble("Value");
             System.out.println("tone: " + tone);
             polarityVal = Math.abs(tone*100);
             if (tone > 0) {
                 tweetClass = "commentPos";
                 progressClass = "progress-success";
             } else if (tone < 0) {
                 tweetClass = "commentNeg";
                 progressClass = "progress-danger";
             } else {
                 tweetClass = "commentNorm";
             }
 
             System.out.println("Analysis:" + commentClass);
 
         } catch (Exception e) {
             System.err.println(e.toString());
         }
     }
 
     public void updateEnding() {
         System.out.println("Update Called");
         try {
             ending = URLEncoder.encode(tweet, "UTF-8");
             ending = ending.replaceAll("\\+", "%20");
         } catch (Exception e) {
             System.out.println(e.toString());
         }
 
     }
     
     public void resetProgress(){
         progressClass = "";
         polarityVal = 0;
         offeringGuidanceVal = 0;
         requestingGuidanceVal = 0;
         flamboyanceVal = 0;
         slangVal = 0;
         decisivenessVal = 0;
     }
     
     public void analyzeTextBlock(){
         try {
             blogPostSuccessMessage = "none";
             blogPostFailureMessage = "none";
             
             String url_args = URLEncoder.encode("apiKey", "UTF-8") + "=" + URLEncoder.encode("cwhjxmnymkvxec3w874fb3snx9m5rjsv", "UTF-8");
             url_args += "&" + URLEncoder.encode("analysis", "UTF-8") + "=" + URLEncoder.encode("all", "UTF-8");
             url_args += "&" + URLEncoder.encode("outputFormat", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8");
             url_args += "&" + URLEncoder.encode("inputText", "UTF-8") + "=" + URLEncoder.encode(textBlock, "UTF-8");
             
             URL url = new URL("http://portaltnx20.openamplify.com/AmplifyWeb_v21/AmplifyThis");
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setDoOutput(true);
             OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
             wr.write(url_args);
             wr.flush();
             InputStream response = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(response));
             StringBuilder sb = new StringBuilder();
             String line;
 
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
             JSONObject obj = new JSONObject(sb.toString());
             double tone = obj.getJSONObject("ns1:AmplifyResponse").getJSONObject("AmplifyReturn").getJSONObject("Styles").getJSONObject("Polarity").getJSONObject("Mean").getDouble("Value");
             System.out.println("tone: " + tone);
             polarityVal = Math.abs(tone*100);
             offeringGuidanceVal = obj.getJSONObject("ns1:AmplifyResponse").getJSONObject("AmplifyReturn").getJSONObject("Styles").getJSONObject("OfferingGuidance").getDouble("Value")*20;
             requestingGuidanceVal = obj.getJSONObject("ns1:AmplifyResponse").getJSONObject("AmplifyReturn").getJSONObject("Styles").getJSONObject("RequestingGuidance").getDouble("Value")*20;
             slangVal = obj.getJSONObject("ns1:AmplifyResponse").getJSONObject("AmplifyReturn").getJSONObject("Styles").getJSONObject("Slang").getDouble("Value")*20;
             decisivenessVal = obj.getJSONObject("ns1:AmplifyResponse").getJSONObject("AmplifyReturn").getJSONObject("Styles").getJSONObject("Decisiveness").getDouble("Value")*20;
             flamboyanceVal = obj.getJSONObject("ns1:AmplifyResponse").getJSONObject("AmplifyReturn").getJSONObject("Styles").getJSONObject("Flamboyance").getDouble("Value")*20;
             if (tone > 0) {
                tweetClass = "commentPos";
                 progressClass = "progress-success";
             } else if (tone < 0) {
                tweetClass = "commentNeg";
                 progressClass = "progress-danger";
             } else {
                 tweetClass = "commentNorm";
             }
             convertBlockToMailLink();
             emailButtonDisabled = false; 
             
             System.out.println("Analysis:" + obj);
         }
         catch(Exception e){
             System.out.println(e.toString());
         }
     }
     
     public void convertBlockToMailLink(){
          
         try{
             /*
            mailtoEnding = URLEncoder.encode(textBlock, "UTF-8");
            mailtoEnding = mailtoEnding.replaceAll("\\+", "\\%20");
            mailtoEnding = URLEncoder.encode(textBlock, "UTF-8");
            
            */
            mailtoEnding = mailtoEnding.replaceAll(" ", "%20");
             System.out.println(mailtoEnding);
         }
         catch(Exception e){
             System.out.println(e.toString());
         }
         
         
     }
     
     
    public void postNewBlog(){
        try {
             URL url;
             if (useOAuth) {
                 url = new URL("https://api.hubapi.com/blog/v1/" + selectedBlogId + "/posts.json?access_token=" + portal.getOauthToken());
             } else {
                 url = new URL("https://api.hubapi.com/blog/v1/" + selectedBlogId + "/posts.json?portalId=" + portal.getPortalID() + "&hapikey=" + portal.getApiKey());
             }
             
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("POST");
             conn.setRequestProperty("Content-Type", "application/json");
             conn.setDoOutput(true);
             conn.setUseCaches(false);
             conn.setDoInput(true);
             /*
             String message = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                     + "<entry xmlns=\"http://www.w3.org/2005/Atom\">"
                     + "<title>" +newBlogPostTitle + "</title>"
                     + "<author>"
                     + "<name>" + blogAuthorEmail + "</name>"
                     + "<email>" + blogAuthorEmail + "</email>"
                     + "</author>"
                     //+ "<content type=\"text\">\"" + textBlock + "\"</content>"
                     + "</entry>";*/
             
             JSONObject messObj = new JSONObject();
             messObj.put("title", newBlogPostTitle);
             messObj.put("authorDisplayName", blogAuthorEmail);
             messObj.put("authorEmail", blogAuthorEmail);
             messObj.put("body", textBlock);
             String message = messObj.toString();
             System.out.println(message);
             conn.setRequestProperty("Content-Length", Integer.toString(message.length()));
             DataOutputStream osw = new DataOutputStream(conn.getOutputStream());
             osw.writeBytes(message);
             osw.flush();
             int resp = conn.getResponseCode();
             System.out.println("blog post response" + conn.getResponseCode());
             InputStream response = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(response));
             StringBuilder sb = new StringBuilder();
             String line;
             //test
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
             JSONObject obj = new JSONObject(sb.toString());
             System.out.println(obj.toString());
             linkToBlog = obj.getString("url");
             if(resp<300){
                 blogPostFailureMessage = "none";
                 blogPostSuccessMessage = "block";
             }
             else if(resp>=300){
                 blogPostFailureMessage = "block";
                 blogPostSuccessMessage = "none";
                 //test
             }
        }
        catch(Exception e){
            System.out.println(e.toString());
            blogPostFailureMessage = "block";
            blogPostSuccessMessage = "none";
        }
    }
    
    public void getAllThoseBlogs(){
        
    }
 }
