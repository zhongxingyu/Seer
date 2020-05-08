 package com.epam.memegen;
 
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
 
 import java.io.IOException;
 import java.io.StringWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.stream.JsonWriter;
 
 /**
  * Serves users votes.
  *
  * @author jauhen@gmail.com
  */
 public class VoteServlet extends HttpServlet {
 
   private static final long serialVersionUID = 5706828606914220112L;
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
   protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
       IOException {
     JsonElement jsonElement = new JsonParser().parse(req.getReader());
     JsonObject jsonObject = jsonElement.getAsJsonObject();
 
     int id = jsonObject.get("id").getAsInt();
     int choice = jsonObject.get("choice").getAsInt();
     String user = UserServiceFactory.getUserService().getCurrentUser().getUserId();
 
     int rating = calculateRating(id, choice, user);
     saveVote(id, choice, user);
     updateMemeWithRating(id, rating);
 
     resp.getWriter().write(getJsonResponse(rating));
   }
 
   /** Calculating rating that should be after save of vote. */
   private int calculateRating(int id, int choice, String user) {
     int rating = getRating(id);
 
     try {
       Entity vote = datastore.get(KeyFactory.createKey("Vote", user + "-" + String.valueOf(id)));
       long oldChoice = (Long) vote.getProperty("choice");
       rating += choice - oldChoice;
     } catch(EntityNotFoundException e) {
       rating += choice;
     }
 
     return rating;
   }
 
   /** Calculate rating */
   public int getRating(int id) {
     Query qLike = new Query("Vote").setFilter(
         CompositeFilterOperator.and(
             FilterOperator.EQUAL.of("id", id),
             FilterOperator.EQUAL.of("choice", 1)));
 
     Query qDislike = new Query("Vote").setFilter(
         CompositeFilterOperator.and(
             FilterOperator.EQUAL.of("id", id),
             FilterOperator.EQUAL.of("choice", -1)));
 
     int supposedNumberOfEpamers = 10000;
     int like = datastore.prepare(qLike).countEntities(withLimit(supposedNumberOfEpamers));
     int dislike = datastore.prepare(qDislike).countEntities(withLimit(supposedNumberOfEpamers));
 
     return like - dislike;
   }
 
   /** Saves vote to datastore. */
   private void saveVote(int id, int choice, String user) {
     Entity vote = new Entity("Vote", user + "-" + String.valueOf(id));
     vote.setProperty("id", id);
     vote.setProperty("choice", choice);
     vote.setProperty("user", user);
 
     datastore.put(vote);
   }
 
   /** Set new rating to meme. */
   private void updateMemeWithRating(int id, int rating) throws ServletException {
     try {
       Key key = KeyFactory.createKey(KeyFactory.createKey("Meme", "ALL"), "Meme", id);
       Entity meme = datastore.get(key);
       meme.setProperty("rating", rating);
       meme.setProperty("isPositive", rating >= 0);
       datastore.put(meme);
 
      MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();

      memcache.delete(MemeDao.Sort.RATING);
 
     } catch(EntityNotFoundException e) {
       throw new ServletException(e.getMessage());
     }
   }
 
   private String getJsonResponse(int rating) throws IOException {
     StringWriter out = new StringWriter();
     JsonWriter w = new JsonWriter(out);
     w.beginObject();
     w.name("rating").value(rating);
     w.endObject();
     w.close();
 
     return out.toString();
   }
 }
