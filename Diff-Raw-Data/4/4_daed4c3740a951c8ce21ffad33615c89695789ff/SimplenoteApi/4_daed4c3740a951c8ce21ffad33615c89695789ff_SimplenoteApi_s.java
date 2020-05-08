 package com.bryanjswift.simplenote.net;
 
 import com.bryanjswift.simplenote.Constants;
 import com.bryanjswift.simplenote.model.Credentials;
 import com.bryanjswift.simplenote.model.Note;
 import com.bryanjswift.simplenote.model.NoteList;
 import com.google.common.collect.ImmutableList;
 import org.apache.http.HttpStatus;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** @author bryanjswift */
 public class SimplenoteApi {
     private static final Logger logger = LoggerFactory.getLogger(SimplenoteApi.class);
     private final String userAgent;
     private final Credentials creds;
 
     /**
      * Create API instance with useragent identifier
      * @param ua User-Agent header string to identify client
      */
     public SimplenoteApi(final String ua) {
         this(ua, null);
     }
 
     /**
      * Create API instance with useragent and account information
      * @param ua User-Agent header string to identify client
      * @param credentials to use when accessing the API
      */
     public SimplenoteApi(final String ua, final Credentials credentials) {
         this.userAgent = ua;
         this.creds = credentials;
     }
 
     /**
      * Retrieve an update API instance with passed in creds
      * @param credentials to use when accessing the API
      * @return a SimplenoteApi instance
      */
     public SimplenoteApi using(final Credentials credentials) {
         return new SimplenoteApi(userAgent, credentials);
     }
 
     /**
      * Get auth token from API for given creds
      * @param email of user
      * @param password of user
      * @return An ApiResponse with Credentials information containing the returned auth token
      */
     public ApiResponse<Credentials> login(final String email, final String password) {
         final String params = String.format("email=%s&password=%s", email, password);
         final String data = Api.encode(params);
         final String url = Constants.API_LOGIN_URL;
         final ApiResponse<String> response = Api.Post(userAgent, url, data);
         return new ApiResponse<Credentials>(response.status, new Credentials(response.payload, email));
     }
 
     /**
      * Send a note to the API to create it for the user identified by instance's credentials
      * @param toSave Note to create
      * @return ApiResponse containing Note contained in server response
      */
     public ApiResponse<Note> create(final Note toSave) {
         final String url = String.format(Constants.API_NOTE_CREATE_URL, creds.auth, creds.email);
         final String data = toSave.json().toString();
         final ApiResponse<String> response = Api.Post(userAgent, url, data);
         Note note = new Note();
         try {
             note = new Note(response.payload);
         } catch (JSONException jsone) {
             logger.error("Unable to create Note from response {}", response.payload, jsone);
         }
         return new ApiResponse<Note>(response.status, note, response.headers);
     }
 
     /**
      * Get the index of note data using passed parameters
      * @param params IndexParams used to limit the data from the API
      * @return list of Note objects with no content
      */
     public ApiResponse<NoteList> index(final IndexParams params) {
         final String url = String.format(Constants.API_LIST_URL, creds.auth, creds.email);
         String data = "";
         if (params.mark != null && !params.mark.equals(Constants.DEFAULT_INDEX_MARK)) {
             data = data + "&mark=" + params.mark;
         }
         if (params.since != null && !params.since.equals(Constants.DEFAULT_INDEX_SINCE)) {
             data = data + "&since=" + params.since.getMillis();
         }
         if (params.length > 0) {
             data = data + "&length=" + params.length;
         }
         final ApiResponse<String> response = Api.Get(userAgent, url + data);
        ApiResponse<NoteList> result;
         if (response.status != HttpStatus.SC_OK) {
             result = new ApiResponse<NoteList>(response.status, NoteList.EMPTY, response.headers);
         } else {
            result = new ApiResponse<NoteList>(HttpStatus.SC_INTERNAL_SERVER_ERROR, NoteList.EMPTY);
             try {
                 final ImmutableList.Builder<Note> builder = ImmutableList.builder();
                 final JSONObject json = new JSONObject(response.payload);
                 final JSONArray notes = json.optJSONArray("data");
                 final int length = notes.length();
                 for (int i = 0; i < length; i++) {
                     builder.add(new Note(notes.getJSONObject(i)));
                 }
                 final String mark = json.optString("mark", null);
                 final int count = json.optInt("count", 0);
                 final NoteList noteList = new NoteList(mark, count, builder.build());
                 result = new ApiResponse<NoteList>(response.status, noteList, response.headers);
             } catch (JSONException jsone) {
                 logger.error("Unable to parse response into JSON", jsone);
             }
         }
         return result;
     }
 
     /**
      * Get the index of note data using default parameters
      * @return list of Note objects with no content
      */
     public ApiResponse<NoteList> index() {
         return index(IndexParams.DEFAULT);
     }
 }
