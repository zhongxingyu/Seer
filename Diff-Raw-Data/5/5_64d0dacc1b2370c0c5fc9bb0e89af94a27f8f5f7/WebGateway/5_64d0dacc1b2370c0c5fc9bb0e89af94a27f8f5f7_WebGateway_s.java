 package ch.netgeek.easypollclient.web;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.jdom.Document;
 
 import android.content.Context;
 import android.widget.Toast;
 import ch.netgeek.easypollclient.polls.Option;
 import ch.netgeek.easypollclient.polls.Poll;
 import ch.netgeek.easypollclient.polls.Question;
 import ch.netgeek.easypollclient.settings.Setting;
 import ch.netgeek.easypollclient.settings.SettingsManager;
 
 public class WebGateway {
     
     private Context context;
     private String serverUrl;
     private String username;
     private String password;
     private DefaultHttpClient httpclient;
     private CookieStore cookieStore;
     private HttpContext localContext;
     
     public WebGateway(Context context) {
         this.context = context;
         SettingsManager settingsManager = new SettingsManager(context);
         Setting setting = settingsManager.readSettings();
         serverUrl = setting.getServerUrl();
         username = setting.getUsername();
         password = setting.getPassword();
         
         httpclient = new DefaultHttpClient();
         cookieStore = new BasicCookieStore();
         localContext = new BasicHttpContext();
         localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
         
     }
     
     private boolean login() {
 
         try {
             
             HttpPost httppost = new HttpPost(serverUrl + "users/sign_in.xml");
             
             // Adding the user data
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
             nameValuePairs.add(new BasicNameValuePair("user[email]", username));
             nameValuePairs.add(new BasicNameValuePair("user[password]", password));
             httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
             // Execute HTTP post request
             HttpResponse response = httpclient.execute(httppost, localContext);
             String xmlResponse = EntityUtils.toString(response.getEntity());
             
             if (xmlResponse.contains("Invalid email or password")) {
                 return false;
             }
 
         } catch (ClientProtocolException e) {
             e.printStackTrace();
             return false;
             
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         }
         
         return true;
         
     }
     
     public ArrayList<Poll> getPolls() {
         
         ArrayList<Poll> polls = new ArrayList<Poll>();
         
         if (login() == false) {
             Toast.makeText(context, "Login Failure!", Toast.LENGTH_LONG).show();
             return null;
         }
         
         // Getting the polls list via HTTP
         String xmlResponse;
         
         try {
             HttpGet httpget = new HttpGet(serverUrl + "polls.xml");
             HttpResponse response = httpclient.execute(httpget, localContext);
             xmlResponse = EntityUtils.toString(response.getEntity());
 
         } catch (ClientProtocolException e) {
             e.printStackTrace();
             return null;
             
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
         
         // Parsing the XML response
         Element root = null;
         
         try {
             SAXBuilder builder = new SAXBuilder();
             Document doc = builder.build(new StringReader(xmlResponse));
             root = doc.getRootElement();
 
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
         
         // iterating over the xml <poll> elements
         String pollTag = "poll";
         String pollIdTag = "id";
         String pollTitleTag = "title";
         String pollPublishedAtTag = "published_at";
         String pollCategoryTag = "category";
         String pollUserNameTag = "user_name";
         String pollQuestionsCountTag = "questions_count";
         String pollParticipationsCountTag = "participations_count";
         
         List<?> pollElementList = root.getChildren(pollTag);
         if (pollElementList == null) {
             return null;
         }
         Iterator<?> pollIterator = pollElementList.iterator();
         while (pollIterator.hasNext()) {
             Element pollElement = (Element) pollIterator.next();
             
             /* 
              * getting the id, title, published_at, category, user_name, 
              * questions_count and participations_count Elements
              */
             int pollId = Integer.valueOf(pollElement.getChildText(pollIdTag));
             String title = pollElement.getChildText(pollTitleTag);
             Date publishedAt = Date.valueOf(pollElement.getChildText(pollPublishedAtTag));
             String category = pollElement.getChildText(pollCategoryTag);
             String userName = pollElement.getChildText(pollUserNameTag);
             int questionsCount = Integer.valueOf(pollElement.getChildText(pollQuestionsCountTag));
             int participationsCount = Integer.valueOf(pollElement.getChildText(pollParticipationsCountTag));
             
             polls.add(new Poll(pollId, title, publishedAt, category, userName, 
                     questionsCount, participationsCount));
         }
         
         if (polls.isEmpty()) {
             return null;
         }
         
         return polls;
     }
     
     public Poll getPoll(int pollId) {
         
         if (login() == false) {
             Toast.makeText(context, "Login Failure!", Toast.LENGTH_LONG).show();
             return null;
         }
         
         // Getting the polls list via HTTP
         String xmlResponse;
         
         try {
             HttpGet httpget = new HttpGet(serverUrl + "polls/" + String.valueOf(pollId) + ".xml");
             HttpResponse response = httpclient.execute(httpget, localContext);
             xmlResponse = EntityUtils.toString(response.getEntity());
 
         } catch (ClientProtocolException e) {
             e.printStackTrace();
             return null;
             
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
         
         if (xmlResponse.contains("invalid poll id")) {
             return null;
         }
         
         // Parsing the XML response
         Element root = null;
         
         try {
             SAXBuilder builder = new SAXBuilder();
             Document doc = builder.build(new StringReader(xmlResponse));
             root = doc.getRootElement();
 
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
         
         // iterating over the child elements of xml <poll>
         String idTag = "id";
         String titleTag = "title";
         String publishedAtTag = "published_at";
         String categoryTag = "category";
         String userNameTag = "user_name";
         String questionsCountTag = "questions_count";
         String participationsCountTag = "participations_count";
         String questionsTag = "questions";
         String questionTag = "question";
         String questionIdTag = "id";
         String questionTextTag = "text";
         String questionKindTag = "kind";
         String optionsTag = "options";
         String optionTag = "option";
         String optionIdTag = "id";
         String optionTextTag = "text";
         
         // Parsing the poll details
         int id = Integer.valueOf(root.getChildText(idTag));
         String title = root.getChildText(titleTag);
         Date publishedAt = Date.valueOf(root.getChildText(publishedAtTag));
         String category = root.getChildText(categoryTag);
         String userName = root.getChildText(userNameTag);
         int questionsCount = Integer.valueOf(root.getChildText(questionsCountTag));
         int participationsCount = Integer.valueOf(root.getChildText(participationsCountTag));
         
         Poll poll = new Poll(id, title, publishedAt, category, userName, questionsCount, participationsCount);
         
         // Parsing the questions of the poll
         Element questionsElement = root.getChild(questionsTag);
 
         if (questionsElement == null) {
             return null;
         }
         List<?> questionElementList = questionsElement.getChildren(questionTag);
         
         if (questionElementList == null) {
             return null;
         }
         Iterator<?> questionIterator = questionElementList.iterator();
         
         while (questionIterator.hasNext()) {
             Element questionElement = (Element) questionIterator.next();
             
             int questionId = Integer.valueOf(questionElement.getChildText(questionIdTag));
             String questionText = questionElement.getChildText(questionTextTag);
             String questionKind = questionElement.getChildText(questionKindTag);
             
             Question question = new Question(questionId, questionText, questionKind);
             
             Element optionsElement = questionElement.getChild(optionsTag);
             
             if (optionsElement == null) {
                 return null;
             }
             List<?> optionElementList = optionsElement.getChildren(optionTag);
             
             if (optionElementList == null) {
                 return null;
             }
             Iterator<?> optionIterator = optionElementList.iterator();
             
             // Parsing the options of the question
             while (optionIterator.hasNext()) {
               Element optionElement = (Element) optionIterator.next();
               
               int optionId = Integer.valueOf(optionElement.getChildText(optionIdTag));
               String optionText = optionElement.getChildText(optionTextTag);
               
               question.addOption(new Option(optionId, optionText));
             }
             
             poll.addQuestion(question);
         }
         
         return poll;
     }
     
     public boolean postParticipation(Poll poll) {
         
         if (login() == false) {
             Toast.makeText(context, "Login Failure!", Toast.LENGTH_LONG).show();
             return false;
         }
         
         try {
             
             HttpPost httppost = new HttpPost(serverUrl + "participations");
             
             // Adding the participation data
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
             
             // Poll ID
             nameValuePairs.add(new BasicNameValuePair("participation[poll][poll_id]", String.valueOf(poll.getPollId())));
             
             //TODO User id
             
             ArrayList<Question> questions = poll.getQuestions();
             
             for (int i = 0; i < questions.size(); i++) {
                 
                 Question question = questions.get(i);
                 
                 // Question ID
                 nameValuePairs.add(new BasicNameValuePair("participation[poll][questions_attributes][" + i + "][id]", String.valueOf(question.getQuestionId())));
                 
                 ArrayList<Option> options = question.getOptions();
                int selections = 0;
                 
                 for (int k = 0; k < options.size(); k++) {
                     
                     Option option = options.get(k);
                     
                     // Option ID
                     nameValuePairs.add(new BasicNameValuePair("participation[poll][questions_attributes][" + i + "][options_attributes][" + k + "][id]", String.valueOf(option.getOptionId())));
                     if (option.isChecked()) {
                         
                         // If Option is checked
                        nameValuePairs.add(new BasicNameValuePair(String.valueOf(question.getQuestionId()) + "[" + selections + "]", String.valueOf(option.getOptionId())));
                        selections++;
                     }
                 }
                 
             }
             
             // Answer button
             nameValuePairs.add(new BasicNameValuePair("answer_button", "Answer"));
 
             httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
             // Execute HTTP post request
             httpclient.execute(httppost, localContext);
 
         } catch (ClientProtocolException e) {
             e.printStackTrace();
             return false;
             
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         }
         
         
         return true;
     }
     
 }
