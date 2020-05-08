 package net.contextfw.web.commons.cloud.session;
 
 import java.util.UUID;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 
 import net.contextfw.web.application.HttpContext;
 import net.contextfw.web.application.configuration.Configuration;
 import net.contextfw.web.application.configuration.SettableProperty;
 import net.contextfw.web.commons.cloud.CloudDatabase;
 import net.contextfw.web.commons.cloud.mongo.MongoBase;
 import net.contextfw.web.commons.cloud.mongo.MongoExecution;
 import net.contextfw.web.commons.cloud.serializer.Serializer;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 @Singleton
 public class MongoCloudSession extends MongoBase implements CloudSession {
 
     private Provider<CloudSessionHolder> holderProvider;
     
     private final Serializer serializer;
     
     public static final SettableProperty<String> COLLECTION_NAME = 
             Configuration.createProperty(String.class, 
                     MongoCloudSession.class.getCanonicalName() + ".collectionName");
     
     private final Provider<HttpContext> httpContext;
     private static final long HALF_HOUR = 30*60*1000;
     
     private final String cookieName;
     private final String sessionCollection;
     private final long maxInactivity;
     
     @Inject
     public MongoCloudSession(@CloudDatabase DB db, 
                              Configuration configuration, 
                              Provider<HttpContext> httpContext,
                              Provider<CloudSessionHolder> holder,
                              Serializer serializer) {
         super(db, configuration.get(Configuration.REMOVAL_SCHEDULE_PERIOD));
         
         this.httpContext = httpContext;
         this.holderProvider = holder;
         cookieName = configuration.getOrElse(CloudSession.COOKIE_NAME, "cloudSession");
         sessionCollection = configuration.getOrElse(COLLECTION_NAME, "session");
         this.maxInactivity = configuration.getOrElse(CloudSession.MAX_INACTIVITY, HALF_HOUR);
         this.serializer = serializer;
     }
     
     @Override
     public void set(final String key, final Object value) {
         
         if (StringUtils.isBlank(key)) {
             throw new IllegalArgumentException("Key cannot be empty or null.");
         }
         
         final String handle = getSessionHandle(true);
         
         if (handle != null) {
             
             if (value == null) {
                 unset(handle, key);
             }
             
             executeExclusive(getSessionCollection(), handle, null, 
                     new MongoExecution<Void>() {
                         public Void execute(DBObject object) {
                             DBObject query = o(KEY_HANDLE, handle);
                             DBObject update = o("$set", o(key, 
                                     serializer.serialize(value)));
                             getSessionCollection().update(query, update);
                             return null;
                         }
                     });
         }
     }
 
     private boolean isSessionValid(String handle) {
         DBObject query = b()
                 .add(KEY_HANDLE, handle)
                 .push(KEY_VALID_THROUGH)
                 .add("$gte", System.currentTimeMillis()).get();
         
         return getSessionCollection().count(query) > 0;
     }
 
     private String createSession() {
         String handle = UUID.randomUUID().toString();
         DBObject session = new BasicDBObject();
         session.put(KEY_HANDLE, handle);
        session.put(KEY_LOCKED, false);
         session.put(KEY_VALID_THROUGH, System.currentTimeMillis() + maxInactivity);
         getSessionCollection().insert(session);
         return handle;
     }
     
     private String getSessionHandle(boolean create) {
 
         CloudSessionHolder holder = this.holderProvider.get();
         
         if (holder.getHandle() != null && holder.isOpen()) {
             return holder.getHandle();
         }
         
         if (holder.getHandle() == null) {
             holder.setHandle(findHandleFromCookie());
         }
         
         if (holder.getHandle() != null && !isSessionValid(holder.getHandle())) {
             holder.setHandle(null);
         }
         
         if (holder.getHandle() == null && create) {
             if (httpContext.get().getResponse() != null) {
                 holder.setHandle(createSession());
                 setSessionCookie(holder.getHandle(), false);
             } else {
                 throw new NoSessionException(
                         "Cannot create new session! " + 
                         "HttpResponse not bound");
             }
         }
         
         return holder.getHandle();
     }
     
     private String typeToKey(Class<?> type) {
         return type.getName().replaceAll("\\.", "");
     }
     
     @Override
     public void set(Object value) {
         if (value == null) {
             throw new IllegalArgumentException("Value cannot be null.");
         }
         set(typeToKey(value.getClass()), value);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> T get(final String key, Class<T> type) {
         
         if (StringUtils.isBlank(key)) {
             throw new IllegalArgumentException("Key cannot be empty or null.");
         }
         if (type == null) {
             throw new IllegalArgumentException("Type cannot be empty or null.");
         }
         
         String handle = getSessionHandle(false);
         
         if (handle != null) {
             byte[] data = executeExclusive(getSessionCollection(), handle, null, 
                     new MongoExecution<byte[]>() {
                         public byte[] execute(DBObject object) {
                             return (byte[]) object.get(key);
                         }
                     });
             return data == null ? null : (T) serializer.unserialize(data);
         } else {
             return null;
         }
     }
 
     @Override
     public <T> T get(Class<T> type) {
         if (type == null) {
             throw new IllegalArgumentException("Type cannot be empty or null.");
         }
         return get(typeToKey(type), type);
     }
 
     @Override
     public void unset(String key) {
         if (StringUtils.isBlank(key)) {
             throw new IllegalArgumentException("Key cannot be empty or null.");
         }
         
         String handle = getSessionHandle(false);
         if (handle != null) {
             unset(handle, key);
         }
     }
     
     private void unset(final String handle, final String key) {
         executeExclusive(getSessionCollection(), handle, null, 
                 new MongoExecution<Void>() {
                     public Void execute(DBObject object) {
                             DBObject query = o(KEY_HANDLE, handle);
                             DBObject update = o("$unset", o(key, 1));
                             getSessionCollection().update(query, update);
                             return null;
                         }
                     });
     }
 
     @Override
     public void unset(Class<?> type) {
         if (type == null) {
             throw new IllegalArgumentException("Type cannot be empty or null.");
         }
         unset(typeToKey(type));
     }
 
     @Override
     public void expireSession() {
         String handle = getSessionHandle(false);
         if (handle != null) {
             setSessionCookie(handle, true);
             removeSession(handle);
         }
     }
 
     private void removeSession(String handle) {
         getSessionCollection().remove(o(KEY_HANDLE, handle));
     }
     
     private String findHandleFromCookie() {
         HttpServletRequest request = httpContext.get().getRequest();
         if (request != null) {
             Cookie[] cookies = request.getCookies();
             if (cookies != null) {
                 for (Cookie cookie : cookies) {
                     if (cookie.getName().equals(cookieName)) {
                         return cookie.getValue();
                     }
                 }
             }
         }
         return null;
     }
     
     @Override
     public void openSession(OpenMode mode) {
         
         removeExpiredSessions();
         CloudSessionHolder holder = this.holderProvider.get();
         
         if (holder.getHandle() == null && mode == OpenMode.EXISTING) {
             throw new NoSessionException(
                     "Cannot open session in EXISTING-mode! " +
             	  "Session has not been initialized");
         }
         
         if (httpContext.get().getRequest() == null && mode != OpenMode.EXISTING) {
             throw new NoSessionException(
                     "Cannot open session in " + mode + "-mode! " +
                     "No request bound to HttpContext. Use EXISTING-mode instead");
         }
         
         String handle = getSessionHandle(mode == OpenMode.EAGER);
         
         if (handle != null) {
             holder.setOpen(true);
             setSessionCookie(handle, false);
             refreshSession(handle);
         }
     }
 
     private void setSessionCookie(String handle, boolean remove) {
         if (httpContext.get().getResponse() != null) {
             Cookie cookie = new Cookie(cookieName, handle);
             cookie.setPath("/");
            cookie.setMaxAge(remove ? 0 : (int) (maxInactivity/1000));
             httpContext.get().getResponse().addCookie(cookie);
         }
     }
 
     private void refreshSession(String handle) {
         getSessionCollection().update(o(KEY_HANDLE, handle),
                 o("$set", 
                         o(KEY_VALID_THROUGH, 
                         System.currentTimeMillis() + maxInactivity)));
     }
     
     private DBCollection getSessionCollection() {
         return getDb().getCollection(sessionCollection);
     }
     
     private void removeExpiredSessions() {
         removeExpiredObjects(getSessionCollection());
     }
 
     @Override
     public void closeSession() {
         holderProvider.get().setOpen(false);
     }
 }
