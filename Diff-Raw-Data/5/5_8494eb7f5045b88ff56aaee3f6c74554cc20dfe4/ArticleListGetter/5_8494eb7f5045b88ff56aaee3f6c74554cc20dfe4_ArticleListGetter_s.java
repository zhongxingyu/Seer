 package ch.adorsaz.loungeDroid.servercom;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import ch.adorsaz.loungeDroid.article.Article;
 import ch.adorsaz.loungeDroid.article.ToDisplay;
 import ch.adorsaz.loungeDroid.exception.AuthenticationFailLoungeException;
 import ch.adorsaz.loungeDroid.exception.GetArticleListException;
 import ch.adorsaz.loungeDroid.exception.ParseArticleException;
 import ch.adorsaz.loungeDroid.gui.ArticleListActivity;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class ArticleListGetter extends
         AsyncTask<ToDisplay, Object, List<Article>> {
     private SessionManager mSessionManager = null;
     private ArticleListActivity mActivity = null;
 
     /* Some urls needed to get feeds */
     private final static String ARTICLES_PAGE_RSSLOUNGE = "/item/list";
     private final static String DISPLAY_ALL_PARAMS = "unread=0&starred=0";
     private final static String DISPLAY_UNREAD_PARAMS = "unread=1&starred=0";
     private final static String DISPLAY_STARRED_PARAMS = "unread=0&starred=1";
 
     public ArticleListGetter(ArticleListActivity activity) {
         mActivity = activity;
     }
 
     @Override
     protected void onPreExecute() {
         mSessionManager = SessionManager.getInstance(mActivity.getApplicationContext());
     }
 
     @Override
     protected List<Article> doInBackground(ToDisplay... toDisplay) {
         List<Article> articles = null;
         try {
             articles = getArticles(toDisplay[0]);
         } catch (AuthenticationFailLoungeException e) {
             // TODO Pass to offline mode
             Log.e(SessionManager.LOG_DEBUG_LOUNGE,
                     "Cannot log in. Check your connection. We'll check if we have saved data before.");
         } catch (GetArticleListException e) {
             // TODO Pass to offline mode
             Log.e(SessionManager.LOG_DEBUG_LOUNGE,
                     "Cannot get article list, are you correctly logged ? Using saved data if available.");
         } catch (ParseArticleException e) {
             // TODO Make Toast
             Log.e(SessionManager.LOG_DEBUG_LOUNGE,
                     "Cannot parse JSON response. Trying to display already parsed articles. Please contact developpers.");
         }
         return articles;
     }
 
     @Override
     protected void onPostExecute(List<Article> allArticles) {
         // TODO : Fill the list activity
         mActivity.updateArticleList(allArticles);
         Log.d("loungeDroid", "Finish to update Activity");
     }
 
     private List<Article> getArticles(ToDisplay toDisplay)
         throws GetArticleListException,
         AuthenticationFailLoungeException,
         ParseArticleException {
         List<Article> articleList = new LinkedList<Article>();
         JSONArray messages = null;
 
         String httpParams = SessionManager.JSON_GET_RSSLOUNGE;
         switch (toDisplay) {
             case ALL:
                 httpParams += "&" + DISPLAY_ALL_PARAMS;
                 break;
             case UNREAD:
                 httpParams += "&" + DISPLAY_UNREAD_PARAMS;
                 break;
             case STARRED:
                 httpParams += "&" + DISPLAY_STARRED_PARAMS;
                 break;
         }
         JSONObject jsonResponse = mSessionManager.serverRequest(
                 ARTICLES_PAGE_RSSLOUNGE, httpParams);
 
         // TODO : check if message object array exists if all feeds are read and
         // show only unread.
         try {
             messages = jsonResponse.getJSONArray("messages");
         } catch (JSONException e) {
             throw new GetArticleListException();
         }
 
         try {
             for (int i = 0; i < messages.length(); i++) {
                 JSONObject thisMessage = messages.getJSONObject(i);
                 int id = thisMessage.getInt("id");
                 String datetime = thisMessage.getString("datetime");
                 int day = Character.getNumericValue(datetime.charAt(8)) * 10
                         + Character.getNumericValue(datetime.charAt(9));
                 int month = Character.getNumericValue(datetime.charAt(5)) * 10
                         + Character.getNumericValue(datetime.charAt(6));
                 String subject = thisMessage.getString("title");
                 String content = thisMessage.getString("content");
                 String author = thisMessage.getString("name");
                 String link = thisMessage.getString("link");
                 String icon = thisMessage.getString("icon");
                 Boolean isRead = thisMessage.getInt("unread") == 1;
                 Boolean isStarred = thisMessage.getInt("starred") == 1;
 
                 Article article = new Article(id, day, month, subject, content,
                         author, link, icon, isRead, isStarred);
                 articleList.add(article);
             }
         } catch (JSONException e) {
             throw new ParseArticleException();
         }
 
        if (articleList.get(0) != null) {
             Log.d(SessionManager.LOG_DEBUG_LOUNGE, "First article : "
                     + articleList.get(0).toString());
        }
         return articleList;
     }
 }
