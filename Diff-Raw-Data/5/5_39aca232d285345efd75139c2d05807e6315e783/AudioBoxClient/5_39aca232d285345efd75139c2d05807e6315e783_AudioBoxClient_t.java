 
 /***************************************************************************
  *   Copyright (C) 2010 iCoreTech research labs                            *
  *   Contributed code from:                                                *
  *   - Valerio Chiodino - keytwo at keytwo dot net                         *
  *   - Fabio Tunno      - fat at fatshotty dot net                         *
  *                                                                         *
  *   This program is free software: you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation, either version 3 of the License, or     *
  *   (at your option) any later version.                                   *
  *                                                                         *
  *   This program is distributed in the hope that it will be useful,       *
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
  *   GNU General Public License for more details.                          *
  *                                                                         *
  *   You should have received a copy of the GNU General Public License     *
  *   along with this program. If not, see http://www.gnu.org/licenses/     *
  *                                                                         *
  ***************************************************************************/
 
 package fm.audiobox.core.models;
 
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.net.SocketTimeoutException;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.zip.GZIPInputStream;
 
 import javax.net.ssl.KeyManager;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLException;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.SSLSocket;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpException;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpResponseInterceptor;
 import org.apache.http.HttpStatus;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.conn.params.ConnPerRouteBean;
 import org.apache.http.conn.routing.HttpRoute;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.conn.ssl.X509HostnameVerifier;
 import org.apache.http.entity.HttpEntityWrapper;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.log4j.Logger;
 
 import fm.audiobox.core.api.Model;
 import fm.audiobox.core.api.ModelsCollection;
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ModelException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.interfaces.CollectionListener;
 import fm.audiobox.core.util.Inflector;
 
 
 /**
  * AudioBoxClient is the main library class. Every request to AudioBox.fm should pass through this object.
  * This class is used mainly to configure every aspect of the library itself.<br/>
  * To populate and get informations about user library use the {@link User} (or an extended User) model instead.
  * <p>
  * 
  * Keep in mind that this library provides only the common browsing actions and some other few feature.<br/>
  * AudioBoxClient does not streams, nor play or provide a BitmapFactory for albums covers.
  * 
  * <p>
  *
  * As many other libraries out there AudioBox.fm-JavaLib allows you to extends default models and use them in place
  * of default ones.
  *
  * <p>
  *
  * In order to make AudioBoxClient load your extended models you will need to provide your {@link Model} extension
  * through the {@link AudioBoxClient#setCollectionListenerFor(String, CollectionListener)} method.<br/>
  *
  * <p>
  * 
  * Note that some of the requests, such as the ModelsCollection population requests, can be done asynchronously.<br/>
  * To keep track of the collection building process you can make use of the {@link CollectionListener} object.
  * 
  * <p>
  *
  * The usual execution flow can be demonstrated by the code snippet below:
  *
  * <pre>
  * // Creating the new AudioBoxClient instance
  * abc = new AudioBoxClient();
  *
  * // If you extended the {@link User} model AudioBoxClient should 
  * // be informed before the login take place.
  * AudioBoxClient.setModelClassFor(AudioBoxClient.USER_KEY , MyUser.class );
  * 
  * // Suppose we want to limit requests timeout to 5 seconds
  * abc.getMainConnector().setTimeout( 5000 );
  *  
  * // Now we can try to perform a login...
  * try {
  *
  *    // Should perform a login before anything else is done with 
  *    // the AudioBoxClient object
  *    MyUser user = (MyUser) abc.login( "user@email.com" , "password" );
  *
  *    // To browse user library we have some nice utils methods
  *    // We can get the user's playlists...
  *    Playlists pls = user.getPlaylists();
  *    
  *    // ...and get more details on a specific one
  *    Playlist pl = playlists.get(0);
  *    
  *    // Get playlist's tracks
  *    Tracks trs = pl.getTracks();
  *    
  *    // Track informations
  *    Track tr = trs.get(0);
  *
  * } catch (LoginException e) {
  *    // Handle {@link LoginException}
  * } catch (ServiceException e) {
  *    // Handle {@link ServiceException}
  * }
  * </pre>
  *
  * This is briefly the navigation loop. Moreover each model offer some action that can be performed. To know what a model
  * can do consult the specific model documentation.
  * 
  * @author Valerio Chiodino
  * @author Fabio Tunno
  * @version 0.0.1
  */
 public class AudioBoxClient {
 
     private static Logger log = Logger.getLogger(AudioBoxClient.class);
 
     /** Specifies the models package (default: fm.audiobox.core.models) */
     public static final String DEFAULT_MODELS_PACKAGE = AudioBoxClient.class.getPackage().getName();
 
     /** Constant <code>TRACK_ID_PLACEHOLDER="[track_id]"</code> */
     public static final String TRACK_ID_PLACEHOLDER = "[track_id]";
 
     /** Constant <code>USER_KEY="User.TAG_NAME"</code> */
     public static final String USER_KEY      = User.TAG_NAME;
 
     /** Constant <code>PROFILE_KEY="Profile.TAG_NAME"</code> */
     public static final String PROFILE_KEY   = Profile.TAG_NAME;
 
     /** Constant <code>PLAYLISTS_KEY="Playlists"</code> */
     public static final String PLAYLISTS_KEY = "Playlists";
 
     /** Constant <code>PLAYLIST_KEY="Playlist.TAG_NAME"</code> */
     public static final String PLAYLIST_KEY  = Playlist.TAG_NAME;
 
     /** Constant <code>GENRES_KEY="Genres"</code> */
     public static final String GENRES_KEY    = "Genres";
 
     /** Constant <code>GENRE_KEY="Genre.TAG_NAME"</code> */
     public static final String GENRE_KEY     = Genre.TAG_NAME;
 
     /** Constant <code>ARTISTS_KEY="Artists"</code> */
     public static final String ARTISTS_KEY   = "Artists";
 
     /** Constant <code>ARTIST_KEY="Artist.TAG_NAME"</code> */
     public static final String ARTIST_KEY    = Artist.TAG_NAME;
 
     /** Constant <code>ALBUMS_KEY="Albums"</code> */
     public static final String ALBUMS_KEY    = "Albums";
 
     /** Constant <code>ALBUM_KEY="Album.TAG_NAME"</code> */
     public static final String ALBUM_KEY     = Album.TAG_NAME;
 
     /** Constant <code>TRACKS_KEY="Tracks"</code> */
     public static final String TRACKS_KEY    = "Tracks";
 
     /** Constant <code>TRACK_KEY="Track.TAG_NAME"</code> */
     public static final String TRACK_KEY     = Track.TAG_NAME;
 
     /** Constant <code>NEW_TRACK_KEY="Track.TAG_NAME"</code> */
     public static final String NEW_TRACK_KEY     = "NewTrack";
 
     /** Prefix for properties keys */
     private static final String PROP_PREFIX = "libaudioboxfm-core.";
 
     /** Properties descriptor reader */
     private static Properties sProperties = new Properties();
 
 
     private static Inflector sI = Inflector.getInstance();
     private static Map<String, CollectionListener> sCollectionListenersMap = new HashMap<String , CollectionListener>();
     private static Map<String, Class<? extends Model>> sModelsMap;
     static {
         sModelsMap = new HashMap<String , Class<? extends Model>>();
         sModelsMap.put( USER_KEY,      User.class ); 
         sModelsMap.put( PROFILE_KEY ,  Profile.class );
         sModelsMap.put( PLAYLISTS_KEY, Playlists.class ); 
         sModelsMap.put( PLAYLIST_KEY,  Playlist.class );
         sModelsMap.put( GENRES_KEY,    Genres.class ); 
         sModelsMap.put( GENRE_KEY,     Genre.class );
         sModelsMap.put( ARTISTS_KEY,   Artists.class ); 
         sModelsMap.put( ARTIST_KEY,    Artist.class );
         sModelsMap.put( ALBUMS_KEY,    Albums.class ); 
         sModelsMap.put( ALBUM_KEY ,    Album.class );
         sModelsMap.put( TRACKS_KEY,    Tracks.class ); 
         sModelsMap.put( TRACK_KEY ,    Track.class );
         sModelsMap.put( NEW_TRACK_KEY , Track.class );
     }
 
     private User mUser;
     private AudioBoxConnector mConnector;
     private String mUserAgent;
 
     /* ------------------ */
     /* Default Interfaces */
     /* ------------------ */
 
     /** The default {@link CollectionListener}. Dummy implementation. */
     private static CollectionListener sDefaultCollectionListener = new CollectionListener() {
         public void onCollectionReady(int message, Object result) { }
         public void onItemReady(int item, Object obj) { }
     };
 
 
     /**
      * <p>Constructor for AudioBoxClient.</p>
      * 
      * When is created it instantiate an {@link AudioBoxConnector} too.
      * 
      */
     public AudioBoxClient() {
 
         String version = "unattended";
 
         try {
             sProperties.load(AudioBoxClient.class.getResourceAsStream("/fm/audiobox/core/config/env.properties"));
             version = AudioBoxClient.getProperty("version");
         } catch (FileNotFoundException e) {
             log.error("Environment properties file not found: " + e.getMessage());
             e.printStackTrace();
         } catch (IOException e) {
             log.error("Unable to access the environment properties file: " + e.getMessage());
             e.printStackTrace();
         }
 
         mUserAgent = "AudioBox.fm/" + version + " (Java; U; " +
         System.getProperty("os.name") + " " +
         System.getProperty("os.arch") + "; " + 
         System.getProperty("user.language") + "; " +
         System.getProperty("java.runtime.version") +  ") " +
         System.getProperty("java.vm.name") + "/" + 
         System.getProperty("java.vm.version") + 
         " AudioBoxClient/" + version;
 
         this.mConnector = new AudioBoxConnector();
         this.mConnector.setTimeout( 180 * 1000 );
     }
 
     /**
      * This method returns the AudioBox.fm properties file reader.
      * 
      * <p>
      * 
      * It has to be used for internal logics only.
      */
     public static String getProperty(String key) {
         return sProperties.getProperty(PROP_PREFIX + key);
     }
 
     /**
      * <p>Getter method for the default connector Object<p>
      *
      * @return the main {@link AudioBoxConnector} object.
      */
     protected AudioBoxConnector getMainConnector(){
         return this.mConnector;
     }
 
 
     /**
      * <p>{@link CollectionListener} is mainly used for async requests.</p>
      * Default implementation does nothing.
      * 
      * <p>
      * 
      * If you wish to interact with the collection while it's being build you can provide your implementation through 
      * this method.
      * 
      * <p>
      * 
      * Note that this will affect only {@link ModelsCollection} models only.
      * 
      * @param key one of the key defined as AudioBoxClient model constants.
      * @param cl your CollectionListener implementation.
      */
     public static void setCollectionListenerFor(String key, CollectionListener cl) {
         if ( cl != null )
             sCollectionListenersMap.put( key, cl );
     }
 
 
     /**
      * Use this method to get the configured {@link CollectionListener} for the <em>key</em> specified {@link ModelsCollection}.
      *
      * @param key the name of the ModelsCollection associated with the collection listener.
      * 
      * @return the current collection listener AudioBoxClient is using.
      */
     public static CollectionListener getCollectionListenerFor(String key) {
         return sCollectionListenersMap.get(key);
     }
 
 
     /**
      * <p>If you need to customize or extend the default models classes you can set your own implementation through
      * this method.</p>
      * 
      * @param key one of the key defined as AudioBoxClient model constants,
      * @param klass your extended {@link Model} {@link Class}.
      */
     public static void setModelClassFor(String key, Class<? extends Model> klass) {
         // Allow only existings keys
         if ( sModelsMap.containsKey( key ) ) {
             sModelsMap.put( key , klass );
         }
     }
 
     /**
      * <p>Create new {@link Model} object based upon the provided key.</p>
      *
      * @param key one of the key defined as AudioBoxClient model constants.
      * @param connector CollectionListener implementation or null.
      * 
      * @return a {@link Model} object.
      * 
      * @throws ModelException if provided key isn't covered from the models map.
      */
     @SuppressWarnings("unchecked")
     public static Model getModelInstance(String key, AudioBoxConnector connector) throws ModelException {
 
         Model model = null;
         Class<? extends Model> klass = sModelsMap.get( key );
 
         if ( klass == null ) {
             String className = DEFAULT_MODELS_PACKAGE + "." + sI.upperCamelCase( key, '-' );
 
             try {
                 klass = (Class<? extends Model>) Class.forName( className );
                 AudioBoxClient.setModelClassFor( key, klass ); // Reset the key
             } catch (ClassNotFoundException e) {
                 throw new ModelException("No model class found: " + className, ModelException.CLASS_NOT_FOUND );
             }
         }
 
         try {
 
             log.trace("New model instance: " + klass.getName() );
             model = klass.newInstance();
 
         } catch (InstantiationException e) {
             throw new ModelException("Instantiation Exception: " + klass.getName(), ModelException.INSTANTIATION_FAILED );
 
         } catch (IllegalAccessException e) {
             throw new ModelException("Illegal Access Exception: " + klass.getName(), ModelException.ILLEGAL_ACCESS );
 
         }
 
         model.setConnector( connector );
 
         if ( model instanceof ModelsCollection ) {
             CollectionListener cl = sCollectionListenersMap.get( model.getClass().getSimpleName() );
             ( (ModelsCollection) model ).setCollectionListener( cl == null ? sDefaultCollectionListener : cl );
         }
 
         return model;
     }
 
 
     /**
      * This method should be called before any other call to AudioBox.fm.<br/>
      * It tries to perform a login. If succeeds a {@link User} object is returned otherwise a 
      * {@link LoginException} is thrown.<br/>
      * This method also checks whether the user is active or not. If not a LoginException is thrown.
      *
      * <p>
      *
      * @param username the username to login to AudioBox.fm in form of an e-mail
      * @param password the password to use for authentication
      * 
      * @return {@link User} object
      * 
      * @throws ModelException if any of the custom model class does not exists or is not instantiable.
      * @throws LoginException if user doesn't exists or is inactive.
      * @throws ServiceException if any connection problem occurs.
      */
     public User login(String username, String password) throws LoginException, ServiceException, ModelException {
 
         log.info("Starting AudioBoxClient: " + mUserAgent);
 
         this.mUser = (User) getModelInstance( USER_KEY , this.getMainConnector() );
         this.mUser.setUsername(username);
         this.mUser.setPassword(password);
 
         this.getMainConnector().setCredential( new UsernamePasswordCredentials(username, password) );
 
         this.getMainConnector().execute( this.mUser.getEndPoint(), null, null, this.mUser, null );
 
         if ( ! User.ACTIVE_STATE.equalsIgnoreCase( this.mUser.getState() ) )
             throw new LoginException("User is not active", LoginException.INACTIVE_USER_STATE );
 
         return this.mUser;
     }
 
 
     /**
      * This method returns the User object used to perform authenticated requests to AudioBox.fm.
      * 
      * <p>
      * 
      * May return <b>null</b> if the user has not yet performed a {@link AudioBoxClient#login(String, String) login}.
      * 
      * @return {@link User} object
      */
     public User getUser() {
         return mUser;
     }
 
 
     /**
      * This method will switch SSL certificate validation on or off.
      * You will not need to use this. This method is used for testing purpose only. For this reason is
      * marked as "deprecated".
      *
      * @param force set or unset the SSL certificate validation (false validates, true skips validation).
      */
     @Deprecated
     public void setForceTrust(boolean force) {
         this.getMainConnector().setForceTrust(force);
     }
 
     /**
      * AudioBoxConnector is the AudioBoxClient http request wrapper.
      * 
      * <p>
      * 
      * Every HTTP request to AudioBox.fm is done through this object and 
      * responses are handled from {@link Model} objects.
      * 
      * <p>
      * 
      * Actually the only configurable parameter is the timeout through the {@link AudioBoxConnector#setTimeout(long)}.
      */
     public class AudioBoxConnector implements Serializable {
 
         private static final long serialVersionUID = -1947929692214926338L;
         
         private static final String PATH_PARAMETER = "${path}";
         private static final String TOKEN_PARAMETER = "${token}";
         private static final String ACTION_PARAMETER = "${action}";
         
         /** Get informations from configuration file */
         private final String PROTOCOL = AudioBoxClient.getProperty("protocol");
         private final String HOST = AudioBoxClient.getProperty("host");
         private final String PORT = AudioBoxClient.getProperty("port");
         private final String API_PREFIX = AudioBoxClient.getProperty("apiPath");
         
         public static final String TEXT_FORMAT = "txt";
         public static final String TEXT_CONTENT_TYPE = "text";
         public static final String XML_FORMAT = "xml";
 
         public static final int RESPONSE_CODE = 0;
         public static final int RESPONSE_BODY = 1;
 
         private String mApiPath;
         private HttpRoute mAudioBoxRoute;
         private ThreadSafeClientConnManager mCm;
         private DefaultHttpClient mClient;
         private UsernamePasswordCredentials mCredentials;
         private BasicScheme mScheme = new BasicScheme();
 
         private Log log = LogFactory.getLog(AudioBoxConnector.class);
 
         /** Default constructor builds {@code mApiPath} string and basic AudioBox.fm http connector */
         private AudioBoxConnector() {
 
             mApiPath = this.getApiPath() + PATH_PARAMETER + TOKEN_PARAMETER + ACTION_PARAMETER;
 
             this.mAudioBoxRoute = new HttpRoute(new HttpHost( HOST, Integer.parseInt(PORT)));
 
             buildClient();
         }
         
 
         /**
          * This method is used to build the HttpClient to use for connections
          */
         private void buildClient() {
             
             SchemeRegistry schemeRegistry = new SchemeRegistry();
             schemeRegistry.register( new Scheme("http", PlainSocketFactory.getSocketFactory(), Integer.parseInt( PORT ) ));
             schemeRegistry.register( new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
             
             HttpParams params = new BasicHttpParams();
             //params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
             HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
             HttpConnectionParams.setSoTimeout(params, 30 * 1000);
             
             this.mCm = new ThreadSafeClientConnManager(params, schemeRegistry);
             this.mClient = new DefaultHttpClient( this.mCm, params );
             
             // Increase max total connection to 200
             ConnManagerParams.setMaxTotalConnections(params, 200);
 
             // Increase default max connection per route to 20
             ConnPerRouteBean connPerRoute = new ConnPerRouteBean(20);
 
             // Increase max connections for audiobox.fm:443 to 50
             connPerRoute.setMaxForRoute(mAudioBoxRoute, 50);
             ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
 
             this.mClient.addRequestInterceptor(new HttpRequestInterceptor() {
 
                 public void process( final HttpRequest request,  final HttpContext context) throws HttpException, IOException {
                     if (!request.containsHeader("Accept-Encoding")) {
                         request.addHeader("Accept-Encoding", "gzip");
                     }
                     request.addHeader("User-Agent", mUserAgent);
                     Header hostHeader = request.getFirstHeader("HOST");
                    if ( hostHeader.getValue().equals( HOST ) )
                         request.addHeader( mScheme.authenticate(mCredentials,  request) );
                 }
 
             });
 
             this.mClient.addResponseInterceptor(new HttpResponseInterceptor() {
 
                 public void process( final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                     HttpEntity entity = response.getEntity();
                     if (entity != null) {
                         Header ceheader = entity.getContentEncoding();
                         if (ceheader != null) {
                             HeaderElement[] codecs = ceheader.getElements();
                             for (int i = 0; i < codecs.length; i++) {
                                 if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                                     response.setEntity( new HttpEntityWrapper(entity){
                                         @Override
                                         public InputStream getContent() throws IOException, IllegalStateException {
                                             // the wrapped entity's getContent() decides about repeatability
                                             InputStream wrappedin = wrappedEntity.getContent();
                                             return new GZIPInputStream(wrappedin);
                                         }
 
                                         @Override
                                         public long getContentLength() { return -1; }
 
                                     }); 
                                     return;
                                 }
                             }
                         }
                     }
                 }
             });
             
         }
         
         
         /**
          * This method is used to close all connections and reinstantiate the HttpClient.
          */
         public void abortAll() {
             this.mCm.shutdown();
             buildClient();
         }
 
 
         /**
          * Use this method to get the full API url.
          * 
          * @return the right API url
          */
         public String getApiPath(){
            return PROTOCOL + "://" + HOST + API_PREFIX;
         }
 
         
         /**
          * Set up HTTP Basic Authentication credentials for HTTP authenticated requests.
          * 
          * @param credential the basic scheme credentials object to use.
          */
         public void setCredential(UsernamePasswordCredentials credential) {
             this.mCredentials = credential;
         }
 
         
         /**
          * Use this method to configure the timeout limit for reqests made against AudioBox.fm.
          * 
          * @param timeout the milliseconds of the timeout limit
          */
         public void setTimeout(long timeout) {
             mClient.getParams().setParameter(ConnManagerParams.TIMEOUT, timeout);
         }
 
 
         /**
          * Returns the requests timeout limit.
          * 
          * @return timeout limit
          */
         public long getTimeout() {
             return (Long) mClient.getParams().getParameter(ConnManagerParams.TIMEOUT);
         }
 
          
         /**
          * Creates a HttpRequestBase
          * 
          * @param path the partial url to call. Tipically this is a Model end point ({@link Model#getEndPoint()})
          * @param token the token of the Model if any, may be null or empty ({@link Model#getToken()})
          * @param action the remote action to execute on the model that executes the action (ie. "scrobble")
          * @param target usually reffers the Model that executes the method
          * @param httpVerb the HTTP method to use for the request (ie: GET, PUT, POST and DELETE)
          * 
          * @return the HttpRequestBase 
          * 
          * @throws LoginException if user has not yet logged in.
          * @throws ServiceException if the connection to AudioBox.fm throws a {@link ClientProtocolException}, 
          * {@link SocketTimeoutException} or {@link IOException}.
          */
         public HttpRequestBase createConnectionMethod(String path, String token, String action , Model target, String httpVerb) {
         	token = ( ( token == null ) ? "" : token.startsWith("/") ? token : "/".concat(token) ).trim();
             action = ( ( action == null ) ? "" : action.startsWith("/") ? action : "/".concat(action) ).trim();
 
             // Replace the placeholder with right values
             String url = mApiPath.replace( PATH_PARAMETER , path ).replace( TOKEN_PARAMETER , token ).replace( ACTION_PARAMETER , action ); 
 
             httpVerb = httpVerb == null ? HttpGet.METHOD_NAME : httpVerb;
 
             if ( HttpGet.METHOD_NAME.equals(httpVerb) )
                 url += "." + XML_FORMAT;
             
             return this.createConnectionMethod(url, target, httpVerb);
         }
 
         
         /**
          * <strong>
          * This method is used by the {@link Model} class.<br/>
          * Avoid direct execution of this method if you don't know what you are doing.
          * </strong>
          * 
          * <p>
          * 
          * Calling this method is the same as calling 
          * {@link AudioBoxConnector#execute(String, String, String, Model, String, boolean) execute( ... , false) }
          * 
          * <p>
          * 
          * Some of the parameter may be null other cannot.
          * 
          * @param path the partial url to call. Tipically this is a Model end point ({@link Model#getEndPoint()})
          * @param token the token of the Model if any, may be null or empty ({@link Model#getToken()})
          * @param action the remote action to execute on the model that executes the action (ie. "scrobble")
          * @param target usually reffers the Model that executes the method
          * @param httpVerb the HTTP method to use for the request (ie: GET, PUT, POST and DELETE)
          * 
          * @return String array containing the response code at position 0 and the response body at position 1
          * 
          * @throws LoginException if user has not yet logged in.
          * @throws ServiceException if the connection to AudioBox.fm throws a {@link ClientProtocolException}, 
          * {@link SocketTimeoutException} or {@link IOException}.
          */
         public String[] execute(String path, String token, String action , Model target, String httpVerb) throws LoginException , ServiceException {
             return execute(path, token, action, target, httpVerb, false);
         }
         
 
         
         /**
          * <strong>
          * This method is used by the {@link Model} class.<br/>
          * Avoid direct execution of this method if you don't know what you are doing.
          * </strong>
          * 
          * <p>
          * 
          * Some of the parameter may be null other cannot.
          * 
          * @param path the partial url to call. Tipically this is a Model end point ({@link Model#getEndPoint()})
          * @param token the token of the Model if any, may be null or empty ({@link Model#getToken()})
          * @param action the remote action to execute on the model that executes the action (ie. "scrobble")
          * @param target usually reffers the Model that executes the method
          * @param httpVerb the HTTP method to use for the request (ie: GET, PUT, POST and DELETE)
          * @param followRedirects whether to follow redirects or not
          * 
          * @return String array containing the response code at position 0 and the response body at position 1
          * 
          * @throws LoginException if user has not yet logged in.
          * @throws ServiceException if the connection to AudioBox.fm throws a {@link ClientProtocolException}, 
          * {@link SocketTimeoutException} or {@link IOException}.
          */
         public String[] execute(String path, String token, String action , Model target, String httpVerb, boolean followRedirects) throws LoginException , ServiceException {
             return execute(path, token, action, target, httpVerb, XML_FORMAT, followRedirects);
         }
 
 
         /**
          * <strong>
          * This method is used by the {@link Model} class.<br/>
          * Avoid direct execution of this method if you don't know what you are doing.
          * </strong>
          * 
          * <p>
          * 
          * Some of the parameter may be null other cannot.
          * 
          * @param path the partial url to call. Tipically this is a Model end point ({@link Model#getEndPoint()})
          * @param token the token of the Model if any, may be null or empty ({@link Model#getToken()})
          * @param action the remote action to execute on the model that executes the action (ie. "scrobble")
          * @param target usually reffers the Model that executes the method
          * @param httpVerb the HTTP method to use for the request (ie: GET, PUT, POST and DELETE)
          * @param format the request format (xml or txt)
          * @param followRedirects whether to follow redirects or not
          * 
          * @return String array containing the response code at position 0 and the response body at position 1
          *
          * @throws LoginException if user has not yet logged in
          * @throws ServiceException if the connection to AudioBox.fm throws a {@link ClientProtocolException}, 
          * {@link SocketTimeoutException} or {@link IOException} occurs.
          */
         public String[] execute(String path, String token, String action , Model target, String httpVerb, String format, boolean followRedirects) throws LoginException , ServiceException {
 
             token = ( ( token == null ) ? "" : token.startsWith("/") ? token : "/".concat(token) ).trim();
             action = ( ( action == null ) ? "" : action.startsWith("/") ? action : "/".concat(action) ).trim();
 
             // Replace the placeholder with right values
             String url = mApiPath.replace( PATH_PARAMETER , path ).replace( TOKEN_PARAMETER , token ).replace( ACTION_PARAMETER , action ); 
 
             httpVerb = httpVerb == null ? HttpGet.METHOD_NAME : httpVerb;
 
             if ( HttpGet.METHOD_NAME.equals(httpVerb) )
                 url += "." + format;
             return request( url, target, httpVerb, followRedirects );
         }
 
         
 
         /**
          * This method is used to performs requests to AudioBox.fm service APIs.<br/>
          * Once AudioBox.fm responds the response is parsed through the target {@link Model}.
          * 
          * <p>
          * 
          * If a stream url is requested (tipically from a {@link Track} object), the location for audio streaming is returned.
          * 
          * <p>
          * 
          * Any other case returns a string representing the status code.
          * 
          * @param method the HTTP method to use for the request
          * @param target the model to use to parse the response
          * @param followRedirects whether to follow redirects or not
          * 
          * @return String array containing the response code at position 0 and the response body at position 1
          * 
          * @throws LoginException if user has not yet logged in
          * @throws ServiceException if the connection to AudioBox.fm throws a {@link ClientProtocolException}, 
          * {@link SocketTimeoutException} or {@link IOException} occurs.
          */
         public String[] request(HttpRequestBase method, Model target, boolean followRedirects) throws LoginException, ServiceException {
             
             if (mUser == null)
                 throw new LoginException("Cannot execute API actions without credentials.", LoginException.NO_CREDENTIALS);
             
             this.mClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, followRedirects);
 
             log.debug("Requesting resource: " + method.getURI() );
             
             try {
             
                 return mClient.execute(method, target, new BasicHttpContext());
                 
             } catch( ClientProtocolException e ) {
 
                 try {
                     // LoginException is not handled by the response handler
                     int status = Integer.parseInt(e.getMessage());
                     if ( status == HttpStatus.SC_UNAUTHORIZED ) {
                         throw new LoginException("Unauthorized user", status);
                     }
 
                 } catch(NumberFormatException ex) { /* Response is a real ClientProtocolException */ }
 
                 throw new ServiceException( "Client protocol exception: " + e.getMessage(), ServiceException.CLIENT_ERROR );
 
             } catch( SocketTimeoutException e ) {
                 throw new ServiceException( "Service does not respond: " + e.getMessage(), ServiceException.TIMEOUT_ERROR );
 
             } catch( ServiceException e ) {
                 // Bypass IOException 
                 throw e;
 
             } catch( IOException e ) {
                 throw new ServiceException( "IO exception: " + e.getMessage(), ServiceException.SOCKET_ERROR );
             }
         }
         
 
         
         
         /* ----------------- */
         /* Protected methods */
         /* ----------------- */
 
 
         /**
          * This method will switch SSL certificate validation on or off.
          * You will not need to use this. This method is used for testing purpose only. For this reason this method is 
          * marked as "deprecated".
          * 
          * @param force set or unset the SSL certificate validation (false validates, true skips validation).
          */
 
         @Deprecated
         protected void setForceTrust(boolean force){
             if (force)
                 this.forceTrustCertificate();
         }
 
 
 
         /* --------------- */
         /* Private methods */
         /* --------------- */
         
         /**
          * Creates a HttpRequestBase by a full URL path
          * 
          * @param target usually reffers the Model that executes the method
          * @param httpVerb the HTTP method to use for the request (ie: GET, PUT, POST and DELETE)
          * 
          * @return a new HttpRequestBase
          * 
          */
         private HttpRequestBase createConnectionMethod(String url, Model target, String httpVerb) {
         	HttpRequestBase method = null; 
             if ( HttpPost.METHOD_NAME.equals( httpVerb ) ) {
                 method = new HttpPost(url);
             } else if ( HttpPut.METHOD_NAME.equals( httpVerb ) ) {
                 method = new HttpPut(url);
             } else if ( HttpDelete.METHOD_NAME.equals( httpVerb ) ) {
                 method = new HttpDelete(url);
             } else {
                 method = new HttpGet(url);
             }
 
             if (method instanceof HttpPost && target instanceof Track) {
                 FileBody fb = ((Track) target).getFileBody();
                 if (fb != null ) {
                     MultipartEntity reqEntity = new MultipartEntity();
                     reqEntity.addPart(Track.HTTP_PARAM, fb);
                     ( (HttpPost) method).setEntity( reqEntity );
                 }
             }
 
             log.debug("Build new Request Method for url: " + url);
         	
         	return method;
         }
         
 
         
         /**
          * This method is used to performs requests to AudioBox.fm service APIs.<br/>
          * Once AudioBox.fm responds the response is parsed through the target {@link Model}.
          * 
          * <p>
          * 
          * If a stream url is requested (tipically from a {@link Track} object), the location for audio streaming is returned.
          * 
          * <p>
          * 
          * Any other case returns a string representing the status code.
          * 
          * @param url the full url where to make the request
          * @param target the model to use to parse the response
          * @param httpVerb the HTTP method to use for the request
          * @param followRedirects whether to follow redirects or not
          * 
          * @return String array containing the response code at position 0 and the response body at position 1
          * 
          * @throws LoginException if user has not yet logged in
          * @throws ServiceException if the connection to AudioBox.fm throws a {@link ClientProtocolException}, 
          * {@link SocketTimeoutException} or {@link IOException} occurs.
          */
         private String[] request(String url, Model target, String httpVerb, boolean followRedirects) throws LoginException, ServiceException {
 
             HttpRequestBase method = this.createConnectionMethod(url, target, httpVerb);
             
             return this.request( method, target, followRedirects);
             
         }
         
         
 
         /**
          * This method is for internal testing and debugging use only.<br/>
          * Please avoid the use of this method.
          * 
          * <p>
          * 
          * If {@link AudioBoxClient} is configured to accept all certificates this method is called to provide the SSL
          * interfaces that will skips any of the default SSL certificate verifications.
          * 
          * <p>
          * 
          * Note that if {@link NoSuchAlgorithmException} or {@link KeyManagementException} occurs this method fails silently
          * with only warn log message. 
          */
 
         @Deprecated
         private void forceTrustCertificate() {
 
             TrustManager easyTrustManager = new X509TrustManager() {
                 public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
                 public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
                 public X509Certificate[] getAcceptedIssuers() { return null; }
             };
 
             X509HostnameVerifier hnv = new X509HostnameVerifier() {
                 public void verify(String arg0, SSLSocket arg1) throws IOException { }
                 public void verify(String arg0, X509Certificate arg1) throws SSLException { }
                 public void verify(String arg0, String[] arg1, String[] arg2) throws SSLException { }
                 public boolean verify(String hostname, SSLSession session) { return false; }
             };
 
             try {
                 SSLContext ctx = SSLContext.getInstance("TLS");
                 ctx.init(new KeyManager[0], new TrustManager[] { easyTrustManager }, null);
                 SSLSocketFactory sf = new SSLSocketFactory(ctx);
                 sf.setHostnameVerifier( hnv );
                 Scheme https = new Scheme("https", sf, 443);
                 this.mClient.getConnectionManager().getSchemeRegistry().register(https);
             } catch (NoSuchAlgorithmException e) {
                 log.warn("Cannot force SSL certificate trust due to 'NoSuchAlgorithmException': " + e.getMessage());
             } catch (KeyManagementException e) {
                 log.warn("Cannot force SSL certificate trust due to 'KeyManagementException': " + e.getMessage());
             }
         }
 
     }
 
 
 }
