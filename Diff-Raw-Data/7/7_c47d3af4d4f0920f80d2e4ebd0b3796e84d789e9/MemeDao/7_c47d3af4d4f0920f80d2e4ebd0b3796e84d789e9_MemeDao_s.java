 package com.epam.memegen;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 import com.google.appengine.api.datastore.Query.SortDirection;
 import com.google.appengine.api.memcache.ConsistentErrorHandler;
 import com.google.appengine.api.memcache.Expiration;
 import com.google.appengine.api.memcache.InvalidValueException;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
 import com.google.appengine.api.memcache.MemcacheServiceException;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 import com.google.gson.stream.JsonWriter;
 
 /**
  * Memcache contains JSONs for:
  * <li>Every meme by its long id.
  * <li>All memes by key "ALL".
  * <li>Last meme Date by key "LAST_TS".
  */
 public class MemeDao {
   private static final Logger logger = Logger.getLogger(MemeDao.class.getName());
 
   private static final String KIND = "Meme";
 
   private static final String LAST_TS = "LAST_TS";
   private static final String ALL = "ALL";
 
   private final MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
   private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
   Expiration expiration = Expiration.byDeltaSeconds(666); // 11 minutes
 
   public MemeDao() {
     memcache.setErrorHandler(new ConsistentErrorHandler() {
       @Override
       public void handleServiceError(MemcacheServiceException ex) {
         logger.log(Level.WARNING, "MemcacheServiceException", ex);
       }
 
       @Override
       public void handleDeserializationError(InvalidValueException ivx) {
         throw ivx;
       }
     });
   }
 
   public static String getMemcacheKey(HttpServletRequest req) {
     String key = req.getRequestURI() + req.getQueryString();
     return key;
   }
 
   public String getAllAsJson(HttpServletRequest req) throws IOException {
     String sinceS = req.getParameter("since");
     Date since = sinceS == null ? null : new Date(Long.parseLong(sinceS));
     String top = req.getParameter("top");
 
     // Lookup memcache
     if (since != null) {
       Date lastTs = (Date) memcache.get(LAST_TS);
       if (lastTs != null && !lastTs.after(since)) {
         // User asked for memes younger than the youngest.
         return "[]";
       }
     } else if (top == null) {
       String json = (String) memcache.get(ALL);
       if (json != null) {
         return json;
       }
     }
 
     Date youngest = null;
     Query q = new Query(KIND);
     q.addSort("date", SortDirection.DESCENDING);
 
     if (since != null) {
       q.setFilter(new FilterPredicate("date", FilterOperator.GREATER_THAN, since));
     }
 
     FetchOptions options = FetchOptions.Builder.withPrefetchSize(1000);
     if (top != null) {
       int t = Integer.parseInt(top);
       options.limit(t);
     }
 
     PreparedQuery prepared = datastore.prepare(q);
     Iterable<Entity> iterable = prepared.asIterable(options);
 
     StringWriter out = new StringWriter();
     JsonWriter w = new JsonWriter(out);
    w.setIndent("  ");
     w.beginArray();
     for (Entity entity : iterable) {
       if (entity.getProperty("deleted") != null) {
         continue;
       }
       Date date = (Date) entity.getProperty("date");
       if (youngest == null || youngest.before(date)) {
         youngest = date;
       }
       toJson(entity, w);
     }
     w.endArray();
     w.close();
     String value = out.toString();
     if (top == null && since == null) {
       memcache.put(ALL, value, expiration);
     }
     memcache.put(LAST_TS, youngest, expiration);
     return value;
   }
 
 
   public String getAsJson(long id) throws IOException {
     // Lookup memcache
     String json = (String) memcache.get(id);
     if (json != null) {
       return json;
     }
 
     Key key = KeyFactory.createKey(KIND, id);
     Entity entity;
     try {
       entity = datastore.get(key);
       StringWriter out = new StringWriter(1000);
       JsonWriter w = new JsonWriter(out);
      w.setIndent("  ");
       toJson(entity, w);
       json = out.toString();
     } catch (EntityNotFoundException e) {
       return null;
     }
     memcache.put(id, json);
     return json;
   }
 
   private String toJson(Entity meme) throws IOException {
     StringWriter sw = new StringWriter(1000);
     JsonWriter w = new JsonWriter(sw);
    w.setIndent("  ");
     toJson(meme, w);
     return sw.toString();
   }
 
   private void toJson(Entity meme, JsonWriter w) throws IOException {
     w.beginObject();
 
     long id = meme.getKey().getId();
     w.name("id").value(id);
 
     String fileName = (String) meme.getProperty("fileName");
     String src = "/image/" + id;
     if (fileName != null) {
       fileName = Util.sanitize(fileName);
       src = src + "/" + fileName;
     }
     w.name("src").value(src);
 
     BlobKey blobKey = (BlobKey) meme.getProperty("blobKey");
     if (blobKey == null) {
       throw new IllegalStateException();
     }
     w.name("blobKey").value(blobKey.getKeyString());
     w.name("src").value("/image/meme" + id + "?blobKey=" + blobKey.getKeyString());
 
     Date date = (Date) meme.getProperty("date");
     if (date != null) w.name("timestamp").value(date.getTime());
 
     w.name("messages").beginArray();
     String topText = (String) meme.getProperty("topText");
     String centerText = (String) meme.getProperty("centerText");
     String bottomText = (String) meme.getProperty("bottomText");
     if (topText != null) {
       w.beginObject();
       w.name("text").value(topText);
       w.name("css").value("top-center");
       w.endObject();
     }
     if (bottomText != null) {
       w.beginObject();
       w.name("text").value(bottomText);
       w.name("css").value("bottom-center");
       w.endObject();
     }
     if (centerText != null) {
       w.beginObject();
       w.name("text").value(centerText);
       w.name("css").value("center-center");
       w.endObject();
     }
     w.endArray();
     w.endObject();
   }
 
   public void create(String blobKey, String topText, String centerText, String bottomText)
       throws IOException {
     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     Entity entity = new Entity(KIND);
     entity.setProperty("blobKey", new BlobKey(blobKey));
     Date justCreatedDate = new Date();
     entity.setProperty("date", justCreatedDate);
     if (!Util.isNullOrEmpty(topText)) {
       entity.setProperty("topText", topText);
     }
     if (!Util.isNullOrEmpty(centerText)) {
       entity.setProperty("centerText", centerText);
     }
     if (!Util.isNullOrEmpty(bottomText)) {
       entity.setProperty("bottomText", bottomText);
     }
 
     Key key = datastore.put(entity);
 
     // Put to memcache.
     String json = toJson(entity);
     memcache.put(key.getId(), json);
 
     memcache.delete(ALL);
 
     // Set LAST_TS, taking care of for race conditions.
     boolean result = false;
     int i = 0;
     while (!result) {
       if (i++ > 50) {
         logger.severe("Infinite loop");
         break;
       }
       IdentifiableValue ident = memcache.getIdentifiable(LAST_TS);
       Date lastDateInMemcache = (Date) ident.getValue();
       if (!lastDateInMemcache.before(justCreatedDate)) {
         break;
       }
       result = memcache.putIfUntouched(LAST_TS, ident, justCreatedDate);
     }
   }
 
   public void delete(long id) throws EntityNotFoundException, IOException {
     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     Key key = KeyFactory.createKey(KIND, id);
     Entity entity;
     entity = datastore.get(key);
     entity.setProperty("deleted", true);
     datastore.put(entity);
     memcache.delete(id);
     memcache.delete(LAST_TS);
     memcache.delete(ALL);
   }
 }
