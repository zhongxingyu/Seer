 
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
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.Consts;
 import org.apache.http.NameValuePair;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.message.BasicNameValuePair;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fm.audiobox.AudioBox;
 import fm.audiobox.configurations.Response;
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.interfaces.IAuthenticationHandle;
 import fm.audiobox.interfaces.IConfiguration;
 import fm.audiobox.interfaces.IConnector;
 import fm.audiobox.interfaces.IConnector.IConnectionMethod;
 import fm.audiobox.interfaces.IEntity;
 import fm.audiobox.interfaces.IResponseHandler;
 
 
 /**
  * This is the main class representing the AudioBox.fm User entity
  */
 public final class User extends AbstractEntity implements Serializable {
 
   private static final Logger log = LoggerFactory.getLogger(User.class);
 
   private static final long serialVersionUID = 1L;
 
   public static final String NAMESPACE = "user";
   public static final String TAGNAME = NAMESPACE;
 
   public static final String ID = "id";
   public static final String USERNAME = "username";
   public static final String PASSWORD = "password";
   public static final String REAL_NAME = "real_name";
   public static final String EMAIL = "email";
   public static final String AUTH_TOKEN = "auth_token";
   public static final String TIME_ZONE = "time_zone";
   public static final String ACCEPTED_EXTENSIONS = "accepted_extensions";
   public static final String ACCEPTED_FORMATS = "accepted_formats";
   public static final String COUNTRY = "country";
   public static final String PLAYLISTS_COUNT = "playlists_count";
   public static final String TOTAL_PLAY_COUNT = "total_play_count";
   public static final String MEDIA_FILES_COUNT = "media_files_count";
   public static final String SUBSCRIPTION_STATE = "subscription_state";
   public static final String COMET_CHANNEL = "comet_channel";
   public static final String CREATED_AT = "created_at";
   public static final String UPDATED_AT = "updated_at";
 
   public static enum SubscriptionState {
     nothing,
     active,
     trialing,
     past_due,
     canceled,
     unpaid
   }
 
   public static final String ALLOWED_EXTENSIONS_SEPARATOR = ",";
 
   private String id;
   private String username;
   private String password;
   private String real_name;
   private String email;
   private String auth_token;
   private String time_zone;
   private String accepted_extensions;
   private String accepted_formats;
   private String country;
   private int playlists_count;
   private long total_play_count;
   private long media_files_count;
   private SubscriptionState subscription_state = SubscriptionState.nothing;
   private String comet_channel;
   private String createdAt;
   private String updatedAt;
 
   private Playlists playlists;
   private Permissions permissions;
   private AccountStats accountStats;
   private Preferences preferences;
   private ExternalTokens externalTokens;
   
   
   
   private static Map<String, Method> setterMethods = new HashMap<String, Method>();
   
   static {
     try {
       setterMethods.put( ID, User.class.getMethod("setId", String.class) );
       setterMethods.put( USERNAME, User.class.getMethod("setUsername", String.class) );
       setterMethods.put( REAL_NAME, User.class.getMethod("setRealName", String.class) );
       setterMethods.put( COMET_CHANNEL, User.class.getMethod("setCometChannel", String.class) );
       setterMethods.put( EMAIL, User.class.getMethod("setEmail", String.class) );
       setterMethods.put( AUTH_TOKEN, User.class.getMethod("setAuthToken", String.class) );
       setterMethods.put( TIME_ZONE, User.class.getMethod("setTimeZone", String.class) );
       setterMethods.put( ACCEPTED_EXTENSIONS, User.class.getMethod("setAcceptedExtensions", String.class) );
       setterMethods.put( ACCEPTED_FORMATS, User.class.getMethod("setAcceptedFormats", String.class) );
       setterMethods.put( CREATED_AT, User.class.getMethod("setCreatedAt", String.class) );
       setterMethods.put( UPDATED_AT, User.class.getMethod("setUpdatedAt", String.class) );
       setterMethods.put( COUNTRY, User.class.getMethod("setCountry", String.class) );
       setterMethods.put( PLAYLISTS_COUNT, User.class.getMethod("setPlaylistsCount", int.class) );
       setterMethods.put( TOTAL_PLAY_COUNT, User.class.getMethod("setTotalPlayCount", long.class) );
       setterMethods.put( MEDIA_FILES_COUNT, User.class.getMethod("setMediaFilesCount", long.class) );
       setterMethods.put( SUBSCRIPTION_STATE, User.class.getMethod("setSubscriptionState", String.class) );
       setterMethods.put( Permissions.TAGNAME, User.class.getMethod("setPermissions", Permissions.class) );
       setterMethods.put( AccountStats.TAGNAME, User.class.getMethod("setAccountStats", AccountStats.class) );
       setterMethods.put( ExternalTokens.TAGNAME, User.class.getMethod("setExternalTokens", ExternalTokens.class) );
       setterMethods.put( Preferences.TAGNAME, User.class.getMethod("setPreferences", Preferences.class) );
       
     } catch (SecurityException e) {
       log.error("Security error", e);
     } catch (NoSuchMethodException e) {
       log.error("No method found", e);
     }
     
   }
   
 
 
   public User(IConfiguration config) {
     super(config);
   }
 
   public String getTagName(){
     return TAGNAME;
   }
 
   public String getNamespace(){
     return NAMESPACE;
   }
 
 
   /**
    * @return the {@code email} used for logging in
    */
   public String getUsername() {
     return username;
   }
 
   /**
    * Sets the {@code email} used for logging in
    */
   @Deprecated
   public void setUsername(String username) {
     this.username = username;
   }
 
   /**
    * @return the {@code id} of the User
    */
   public String getId() {
     return id;
   }
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setId(String id) {
     this.id = id;
   }
 
 
   /**
    * This method should not be used.
    * <br />
    * It returns the password manually set to User while logging in
    */
   @Deprecated
   public String getPassword() {
     return password;
   }
 
 
 
   /**
    * Sets the password used for logging in
    * @param password the User's password used for logging in
    */
   @Deprecated
   public void setPassword(String password) {
     this.password = password;
   }
 
 
 
   /**
    * @return the User's real name
    */
   public String getRealName() {
     return real_name;
   }
 
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setRealName(String real_name) {
     this.real_name = real_name;
   }
 
 
   /**
    * @return the User's email (also used as {@code username})
    */
   public String getEmail() {
     return email;
   }
 
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setEmail(String email) {
     this.email = email;
   }
 
   
   /**
    * @return the User's creation date
    */
   public String getCreatedAt() {
     return createdAt;
   }
 
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setCreatedAt(String createdAt) {
     this.createdAt = createdAt;
   }
 
 
 
   /**
    * @return last User's modification date 
    */
   public String getUpdatedAt() {
     return updatedAt;
   }
 
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setUpdatedAt(String updatedAt) {
     this.updatedAt = updatedAt;
   }
 
 
 
   /**
    * @return the User's country
    */
   public String getCountry() {
     return country;
   }
 
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setCountry(String country) {
     this.country = country;
   }
 
 
   /**
    * @return the User's time zone
    */
   public String getTimeZone() {
     return time_zone;
   }
 
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setTimeZone(String time_zone) {
     this.time_zone = time_zone;
   }
 
 
 
   /**
    * Use this method for knowing which kind of media files are allowed for this user
    * 
    * @return the User's accepted file extensions
    */
   public String getAcceptedExtensions() {
     return accepted_extensions;
   }
 
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setAcceptedExtensions(String accepted_extensions) {
     this.accepted_extensions = accepted_extensions;
   }
 
 
   /**
    * Use this method for knowing which {@code MimeType} of media files are allowed for this user
    * @return the User's accepted mime types
    */
   public String getAcceptedFormats() {
     return accepted_formats;
   }
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setAcceptedFormats(String accepted_formats) {
     this.accepted_formats = accepted_formats;
   }
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setCometChannel(String cometChl) {
     this.comet_channel = cometChl;
   }
 
   
   /**
    * Use this method for connecting to Cloud Web Player {@code Push Server} channel
    * 
    * @return the User's {@code push server channel}
    */
   public String getCometChannel() {
     return this.comet_channel;
   }
 
 
   /**
    * @return the number of playlists User has in his collection
    */
   public int getPlaylistsCount() {
     return playlists_count;
   }
 
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setPlaylistsCount(int playlists_count) {
     this.playlists_count = playlists_count;
   }
 
 
   /**
    * @return the number of media files User has in his collection
    */
   public long getMediaFilesCount() {
     return media_files_count;
   }
 
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setMediaFilesCount(long media_files_count) {
     this.media_files_count = media_files_count;
   }
 
 
   /**
    * @return the total play count amount
    */
   public long getTotalPlayCount() {
     return total_play_count;
   }
 
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setTotalPlayCount(long total_play_count) {
     this.total_play_count = total_play_count;
   }
 
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setSubscriptionState(String flag) {
    SubscriptionState state = null;
    if ( flag != null && ( ( state = SubscriptionState.valueOf( flag ) ) != null )  ) {
       this.subscription_state = state;
     } else {
      this.subscription_state = SubscriptionState.nothing;
      log.info( "no subscription_state is given, use 'nothing'");
     }
   }
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setSubscriptionState(SubscriptionState state) {
     this.subscription_state = state;
   }
 
   
   /**
    * @return the state of the subscription of the User
    */
   public SubscriptionState getSubscriptionState() {
     return this.subscription_state;
   }
 
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setPermissions(Permissions permissions) {
     this.permissions = permissions;
   }
 
   
   /**
    * @return the {@link Permissions} associated with this User
    */
   public Permissions getPermissions() {
     return this.permissions;
   }
 
   
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setAccountStats(AccountStats accountStats) {
     this.accountStats = accountStats;
   }
 
   /**
    * @return the {@link AccountStats} associated with this User
    */
   public AccountStats getAccountStats() {
     return this.accountStats;
   }
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setPreferences(Preferences preferences) {
     this.preferences = preferences;
   }
 
   
   /**
    * @return the {@link Preferences} associated with this User
    */
   public Preferences getPreferences() {
     return this.preferences;
   }
 
   /**
    * This method is used by response parser
    */
   @Deprecated
   public void setExternalTokens(ExternalTokens externalTokens) {
     this.externalTokens = externalTokens;
   }
 
   
   /**
    * @return the {@link ExternalTokens} associated with this User
    */
   public ExternalTokens getExternalTokens() {
     return this.externalTokens;
   }
 
 
   /**
    * @return the {@code authentication token} of this User
    */
   public String getAuthToken() {
     return auth_token;
   }
 
   
   public void setAuthToken(String auth_token) {
     this.auth_token = auth_token;
   }
 
 
 
   /**
    * Instantiates a new {@link MediaFile}.
    * <br />
    * This method can help while uploading media file
    *
    * @return a new {@link MediaFile} instance
    */
   public MediaFile newMediaFile() {
     return (MediaFile) getConfiguration().getFactory().getEntity( MediaFile.TAGNAME, getConfiguration() );
   }
   
   
   /**
    * Creates a new {@code CustomPlaylist} with the specified name
    * 
    * @param name the name of the {@code Playlist}
    * @return the created {@link Playlist}. {@code null} if an error occurrs
    * @throws ServiceException if any connection error occurrs
    * @throws LoginException if any login error occurrs
    */
   public Playlist createPlaylist(String name)  throws ServiceException, LoginException {
     Playlist playlist = (Playlist) getConfiguration().getFactory().getEntity( Playlist.TAGNAME, getConfiguration() );
     String action = IConnector.URI_SEPARATOR + Playlists.NAMESPACE;
 
     List<NameValuePair> params = new ArrayList<NameValuePair>();
     params.add(new BasicNameValuePair("playlist[name]", name) );
     
     IConnectionMethod req = this.getConfiguration().getFactory().getConnector().post(playlist, action, null);
     Response res = req.send(false, params);
     
     if ( ! res.isOK() ) {
       // Server has returned an error response code. Playlist has not been created
       playlist = null;
     } else {
       this.getPlaylists().addEntity( playlist );
     }
     
     return playlist;
   }
 
   
   
   public boolean isDaemonRunning() throws LoginException {
     IConnectionMethod req = this.getConfiguration().getFactory().getConnector().get(this, IConnector.URI_SEPARATOR.concat("daemon"), "keepalive", null);
     
     Response res;
     try {
       res = req.send(false);
     } catch (ServiceException e) {
       log.warn("Daemon is not running and returned error: " + e.getMessage() );
       return false;
     }
     
     return res.isOK();
   }
   
   
   
   /**
    * @return a {@code Playlists} object associated with this User
    */
   public Playlists getPlaylists() {
     if ( this.playlists == null ){
       this.playlists = (Playlists) getConfiguration().getFactory().getEntity(Playlists.TAGNAME, getConfiguration());
     }
     return playlists;
   }
 
 
   /**
    * This method invokes {@link User#load(boolean)}
    */
   public void load() throws ServiceException, LoginException {
     this.load(false);
   }
 
 
 
   protected void copy(IEntity entity) { }
 
 
   public Method getSetterMethod(String tagName) {
     if ( setterMethods.containsKey( tagName) ) {
       return setterMethods.get( tagName );
     }
     return null;
   }
 
   public String getApiPath() {
     return IConnector.URI_SEPARATOR + NAMESPACE;
   }
 
   
   public IConnectionMethod load(boolean async) throws ServiceException, LoginException {
     return this.load(async, null);
   }
 
 
   /**
    * Use this method for manually logging in the User to AudioBox.fm
    * <br />
    * Before invoking this method, you should set {@code username} and {@code password}
    * <br />
    * <b><i>Use {@link AudioBox#login(String, String)} method instead</i></b>
    */
   @Deprecated
   public IConnectionMethod load(boolean async, IResponseHandler responseHandler) throws ServiceException, LoginException {
     IConnectionMethod req = this.getConfiguration().getFactory().getConnector().get(this, null, null);
 
     if ( this.getUsername() != null && this.getPassword() != null ) {
       req.setAuthenticationHandle(new IAuthenticationHandle() {
         public void handle(IConnectionMethod request) {
           UsernamePasswordCredentials mCredentials = new UsernamePasswordCredentials( username, password );
           request.addHeader( BasicScheme.authenticate(mCredentials, Consts.UTF_8.name(), false ) );
         }
       });
     }
 
     req.send(async);
 
     return req;
   }
   
   protected List<NameValuePair> toQueryParameters(boolean all) {
     String prefix = TAGNAME + "[";
     String suffix = "]";
     
     List<NameValuePair> params = new ArrayList<NameValuePair>();
     
     params.add(new BasicNameValuePair(prefix + ID + suffix, this.id )  );
     params.add(new BasicNameValuePair(prefix + USERNAME + suffix, this.username )  );
     params.add(new BasicNameValuePair(prefix + PASSWORD + suffix, this.password )  );
     params.add(new BasicNameValuePair(prefix + REAL_NAME + suffix, this.real_name )  );
     params.add(new BasicNameValuePair(prefix + EMAIL + suffix, this.email )  );
     params.add(new BasicNameValuePair(prefix + AUTH_TOKEN + suffix, this.auth_token )  );
     params.add(new BasicNameValuePair(prefix + TIME_ZONE + suffix, this.time_zone )  );
     params.add(new BasicNameValuePair(prefix + ACCEPTED_EXTENSIONS + suffix, this.accepted_extensions )  );
     params.add(new BasicNameValuePair(prefix + ACCEPTED_FORMATS + suffix, this.accepted_formats )  );
     params.add(new BasicNameValuePair(prefix + COUNTRY + suffix, this.country )  );
     params.add(new BasicNameValuePair(prefix + PLAYLISTS_COUNT + suffix, String.valueOf( this.playlists_count ) )  );
     params.add(new BasicNameValuePair(prefix + TOTAL_PLAY_COUNT + suffix, String.valueOf( this.total_play_count ) )  );
     params.add(new BasicNameValuePair(prefix + MEDIA_FILES_COUNT + suffix, String.valueOf( this.media_files_count ) )  );
     params.add(new BasicNameValuePair(prefix + SUBSCRIPTION_STATE + suffix, String.valueOf( this.subscription_state ) )  );
     params.add(new BasicNameValuePair(prefix + COMET_CHANNEL + suffix, this.comet_channel )  );
     
     return params;
   }
 
 }
