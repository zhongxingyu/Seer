 package edu.rit.cs.distrivia.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Main utility class which can talk to the Distrivia web service over
  * HTTP/HTTPS.
  */
 public class DistriviaAPI {
 
     private static HttpClient httpClient = new DefaultHttpClient();
     private static HttpContext localContext = new BasicHttpContext();
     final private static String API_URL = "http://distrivia.com";
     final private static String API_ERROR = "err";
    final private static String API_SUCCESS = "suc";
 
     /**
      * API call to login to Distrivia with a specific user name.
     * 
      * @param userName
      *            The user name to attempt to login to distrivia with.
     * 
      * @return The authorization users token on success, otherwise null on
      *         authentication failure.
      */
     public static String login(String userName) {
         String url = new String(API_URL);
        url += "/login/" + userName;
 
         HttpPut op = new HttpPut(url);
         HttpResponse response = executeRequest(op);
         String data = responseToString(response);
 
        if (data == API_ERROR) {
             return null;
         } else {
             return data;
         }
     }
 
     /**
      * API Call to register a new user
     * 
      * @param username
      *            The user name to register with the service.
      * @return True on register success, false on register failure.
      */
     public static boolean register(final String username) {
         String url = new String(API_URL);
 
         url += "/register/" + username;
 
         HttpPut op = new HttpPut(url);
         HttpResponse response = executeRequest(op);
 
         String data = responseToString(response);
 
         return data == API_SUCCESS;
     }
 
     /**
      * API call to get the next multiple point question in a game.
     * 
      * @param authToken
      *            The authorization token for this session, obtained at user
      *            login.
      * @param gameId
      *            The identification string for this game.
     * 
      * @return The next question in the game, or null if the game is over.
      */
     public static Question nextQuestion(final String authToken,
             final String gameId) {
 
         String url = new String(API_URL);
 
         url += "/game/" + gameId + "/next";
 
         url += "?authToken=" + authToken;
 
         HttpGet op = new HttpGet(url);
         HttpResponse response = executeRequest(op);
 
         // Decompose JSON response into a Question object
         Question nextQ = null;
         try {
             JSONObject json = new JSONObject(responseToString(response));
             nextQ = Question.create(json);
         } catch (JSONException e) {
         }
 
         return nextQ;
     }
 
     /**
      * API call to answer a given question that has been posed to a user.
     * 
      * @param authToken
      *            The authorization token for this session, obtained at user
      *            login.
      * @param gameId
      *            The unique identification string for the current game.
      * @param answer
      *            The answer to the question the user is currently work on.
      *            Should be "a", "b", "c", or "d"
      * @param answerTime_ms
      *            The time in mili-seconds it took the user to answer the
      *            question.
     * 
      * @return Return true if answer went through successfully, otherwise return
      *         false on error. Error could be, wrong authorization token,
      *         incorrect gameId, impossible answerTIme etc...
      */
     public static boolean answerQuestion(final String authToken,
             final String gameId, final String answer, final int answerTime_ms) {
         String url = new String(API_URL);
         url += "/game/" + gameId;
         url += "/answer/" + answer;
         url += "/time/" + answerTime_ms;
         url += "?authToken=" + authToken;
 
         HttpGet op = new HttpGet(url);
         HttpResponse response = executeRequest(op);
         String data = responseToString(response);
 
         return data == "suc";
     }
 
     /**
      * API call to obtain the current leader board for a given game.
     * 
      * @param authToken
      *            The authorization token for this session, obtained at user
      *            login.
      * @param gameId
      *            The unique identification string for the current game.
     * 
      * @return The game leader board at the time of the query, or null if the
      *         gameId does not exist, or the user is not properly logged in.
      */
     public static Leaderboard leaderBoard(final String authToken,
             final String gameId) {
 
         String url = new String(API_URL);
 
         url += "/game/" + gameId + "/leaderboard";
 
         url += "?authToken=" + authToken;
 
         HttpGet op = new HttpGet(url);
         HttpResponse response = executeRequest(op);
 
         // Decompose JSON response into a leader board object
         Leaderboard board = null;
         try {
             JSONObject json = new JSONObject(responseToString(response));
             board = Leaderboard.create(json);
         } catch (JSONException e) {
         }
 
         return board;
     }
 
     private static HttpResponse executeRequest(HttpRequestBase op) {
         HttpResponse response = null;
         try {
             response = httpClient.execute(op, localContext);
         } catch (ClientProtocolException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return response;
     }
 
     private static String responseToString(HttpResponse res) {
 
         InputStream is = null;
         try {
             is = res.getEntity().getContent();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         if (is != null) {
 
             Writer writer = new StringWriter();
             char[] buffer = new char[1024];
 
             try {
                 Reader reader = new BufferedReader(new InputStreamReader(is,
                         "UTF-8"));
                 int n;
                 while ((n = reader.read(buffer)) != -1) {
                     writer.write(buffer, 0, n);
                 }
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 try {
                     is.close();
                 } catch (IOException e) {
                     // Nothing we can do now!
                 }
             }
             return writer.toString();
         } else {
             return "";
         }
     }
 }
