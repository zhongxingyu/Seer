 package at.ac.tuwien.service.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.builder.api.LinkedInApi;
 import org.scribe.builder.api.TwitterApi;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 import org.scribe.model.Verifier;
 import org.scribe.oauth.OAuthService;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import at.ac.tuwien.service.APIService;
 import at.ac.tuwien.service.DBService;
 
 import com.restfb.DefaultFacebookClient;
 import com.restfb.Facebook;
 import com.restfb.FacebookClient;
 
 public class APIServiceImpl implements APIService {
 
     @SpringBean(name = "DBService")
     public DBService dbService;
 
     private OAuthService linkedInService;
     private Token linkedInRequestToken;
     private Token linkedInAccessToken = null;
 
     private OAuthService twitterService;
     private Token twitterRequestToken;
     private Token twitterAccessToken = null;
 
     private String facebookToken;
 
     public APIServiceImpl() {
         linkedInService = new ServiceBuilder()
             .provider(LinkedInApi.class)
             .apiKey("KbvaulneD9ML6w4hDfI16cx58LJx3vEudgiC_NWtLSkq6WpkhpINeZVrrKwVZKDE")
             .apiSecret("kFJXV98FOMibMfHSFk4vc_3wSA4YzXVVYLu9afXXfhsoqRe7FtUkmTqcYlY5c5hA")
             .build();
 
         twitterService = new ServiceBuilder()
             .provider(TwitterApi.class)
             .apiKey("oDbrOUXFZz7Nc1MsHPtsbg")
             .apiSecret("ABWpSXT52gnVz9vagTvJhHvwJO1H2Ox6GzTRBZr0")
             .build();
 
         restoreToken("twitter");
         restoreToken("linkedin");
 
         twitterRequestToken = twitterService.getRequestToken();
         linkedInRequestToken = linkedInService.getRequestToken();
 
     }
 
     @Override
     public String getLinkedInRequestURL() {
         return linkedInService.getAuthorizationUrl(linkedInRequestToken);
     }
 
     @Override
     public void verifyLinkedIn(String code) {
         Verifier verifier = new Verifier(code);
         linkedInAccessToken = linkedInService.getAccessToken(linkedInRequestToken, verifier);
         storeToken("linkedin", linkedInAccessToken, verifier);
     }
 
     @Override
     public List<String[]> executeLinkedInQuery(String uuid) {
 
         OAuthRequest request = new OAuthRequest(Verb.GET, getLinkedinQuery(uuid));
         linkedInService.signRequest(linkedInAccessToken, request);
         Response response = request.send();
 
         return parseLinkedInXML(response.getBody());
     }
 
     public void setDbService(DBService dbService) {
         this.dbService = dbService;
     }
 
     private List<String[]> parseLinkedInXML(String XML) {
 
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
         List<String[]> result = new ArrayList<String[]>();
 
         try {
             InputSource is = new InputSource();
             is.setCharacterStream(new StringReader(XML));
 
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document doc = builder.parse(is);
 
             NodeList persons = doc.getElementsByTagName("person");
             for (int i = 0; i < persons.getLength(); i++) {
 
                 String[] buffer = new String[4];
 
                 Element person = (Element) persons.item(i);
                 NodeList id = person.getElementsByTagName("id");
                 NodeList prename = person.getElementsByTagName("first-name");
                 NodeList surname = person.getElementsByTagName("last-name");
                 NodeList pictureURL = person.getElementsByTagName("picture-url");
 
                 buffer[0] = id.item(0).getFirstChild().getTextContent();
                 buffer[1] = prename.item(0).getFirstChild().getTextContent();
                 buffer[2] = surname.item(0).getFirstChild().getTextContent();
                 if (pictureURL.item(0) != null) {
                     buffer[3] = pictureURL.item(0).getFirstChild().getTextContent();
                 }
 
                 result.add(buffer);
             }
 
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return result;
     }
 
     @Override
     public boolean alreadySet() {
         if ((linkedInAccessToken == null) || (twitterAccessToken == null)) {
             return false;
         }
         return true;
     }
 
     @Override
     public String excecuteXingQuery(String uuid) {
         Map<String, String> data = dbService.fetchProfileData(uuid);
 
         return "https://www.xing.com/search/people?search%5Bq%5D=" + data.get("prename") + "+" + data.get("surname")
                 + "&send=1";
     }
 
     @Override
     public List<String[]> executeFacebookQuery(String uuid) {
         List<String[]> result = new ArrayList<String[]>();
 
         FacebookClient facebookClient = new DefaultFacebookClient(facebookToken);
 
         List<FqlUser> users = facebookClient.executeQuery(getFacebookQuery(uuid), FqlUser.class);
 
         for (FqlUser user : users) {
             String[] buffer = new String[4];
             buffer[0] = user.uid;
             buffer[1] = user.name;
             buffer[2] = user.pic_small;
             result.add(buffer);
         }
 
         return result;
     }
 
     @Override
     public String getTwitterRequestURL() {
         return twitterService.getAuthorizationUrl(twitterRequestToken);
     }
 
     @Override
     public void verifyTwitter(String code) {
         Verifier verifier = new Verifier(code);
         twitterAccessToken = twitterService.getAccessToken(twitterRequestToken, verifier);
         storeToken("twitter", linkedInAccessToken, verifier);
     }
 
     @Override
     public List<String[]> executeTwitterQuery(String uuid) {
         Map<String, String> data = dbService.fetchProfileData(uuid);
 
         String resource =
             "http://api.twitter.com/1/users/search.xml?q=" + data.get("prename") + "." + data.get("surname")
                     + "&per_page=10";
         OAuthRequest request = new OAuthRequest(Verb.GET, resource);
         twitterService.signRequest(twitterAccessToken, request);
         Response response = request.send();
 
         return parseTwitterXML(response.getBody());
     }
 
     private List<String[]> parseTwitterXML(String XML) {
 
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
         List<String[]> result = new ArrayList<String[]>();
 
         try {
             InputSource is = new InputSource();
             is.setCharacterStream(new StringReader(XML));
 
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document doc = builder.parse(is);
 
             NodeList persons = doc.getElementsByTagName("user");
             for (int i = 0; i < persons.getLength(); i++) {
 
                 String[] buffer = new String[4];
 
                 Element person = (Element) persons.item(i);
                 NodeList id = person.getElementsByTagName("id");
                 NodeList name = person.getElementsByTagName("name");
                 NodeList pictureURL = person.getElementsByTagName("profile_image_url");
 
                 buffer[0] = id.item(0).getFirstChild().getTextContent();
                 buffer[1] = name.item(0).getFirstChild().getTextContent();
                 if (pictureURL.item(0) != null) {
                     buffer[2] = pictureURL.item(0).getFirstChild().getTextContent();
                 }
 
                 result.add(buffer);
             }
 
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return result;
     }
 
     private void storeToken(String name, Token token, Verifier verifier) {
 
         if (!(new File("appdata/token")).exists()) {
             new File("appdata/token").mkdirs();
         }
 
         try {
             FileOutputStream tokenFile = new FileOutputStream("appdata/token/" + name);
             ObjectOutputStream tokenStream = new ObjectOutputStream(tokenFile);
             tokenStream.writeObject(token);
             tokenStream.flush();
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private boolean restoreToken(String name) {
 
         if (!(new File("appdata/token/" + name)).exists()) {
             return false;
         }
 
         try {
             FileInputStream tokenFile = new FileInputStream("appdata/token/" + name);
             ObjectInputStream tokenStream = new ObjectInputStream(tokenFile);
 
             if (name.equals("twitter")) {
                 twitterAccessToken = (Token) tokenStream.readObject();
             }
 
             if (name.equals("linkedin")) {
                 linkedInAccessToken = (Token) tokenStream.readObject();
             }
 
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     public static class FqlUser {
         @Facebook
         String uid;
 
         @Facebook
         String name;
 
         @Facebook
         String pic_small;
 
         @Override
         public String toString() {
             return String.format("%s (%s)", name, uid);
         }
     }
 
     @Override
     public String getFacebookQuery(String uuid) {
         Map<String, String> data = dbService.fetchProfileData(uuid);
         String facebookQuery =
             "SELECT uid, name, pic_small FROM user WHERE name=\"" + data.get("prename") + " " +
                     data.get("surname") + "\"";
 
         facebookQuery += "LIMIT 1,10";
 
         return facebookQuery;
     }
 
     @Override
     public String getLinkedinQuery(String uuid) {
         Map<String, String> data = dbService.fetchProfileData(uuid);
 
         String linkedInQuery =
             "http://api.linkedin.com/v1/people-search";
         linkedInQuery += ":(people:(id,first-name,last-name,picture-url))";
         linkedInQuery += "?first-name=" + data.get("prename") + "&last-name="
                 + data.get("surname") + "&count=10";
 
         if (data.containsKey("country-code")) {
             linkedInQuery += "&country-code=" + data.get("country-code");
         }
 
         if (data.containsKey("company-name")) {
             linkedInQuery += "&company-name=" + data.get("company-name");
         }
 
         if (data.containsKey("school-name")) {
             linkedInQuery += "&school-name=" + data.get("school-name");
         }
 
         return linkedInQuery;
     }
 
     @Override
     public void setFacebookToken(String token) {
         this.facebookToken = token;
     }
 
     @Override
     public boolean checkNetworkId(String network, String uuid) {
         Map<String, String> data = dbService.fetchProfileData(uuid);
         return data.containsKey(network);
     }
 
 }
