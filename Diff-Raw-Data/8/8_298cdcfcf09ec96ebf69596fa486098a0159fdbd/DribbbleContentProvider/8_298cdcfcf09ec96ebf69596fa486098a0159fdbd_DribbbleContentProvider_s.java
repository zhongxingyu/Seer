 package de.hsbremen.android.dribl.provider;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.database.MatrixCursor.RowBuilder;
 import android.net.Uri;
 import android.util.Log;
 
 public class DribbbleContentProvider extends ContentProvider {
 
 	private static final int STREAM = 1;
 	private static final int STREAM_ID = 2;
 	
 	// We need a Hashtable instead of a HashMap to be thread-safe
 	private Map<String, Cursor> responseCache = new Hashtable<String, Cursor>();
 	
 	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 	static {
 		sURIMatcher.addURI(DribbbleContract.AUTHORITY, "*", STREAM);
         sURIMatcher.addURI(DribbbleContract.AUTHORITY, "*/#", STREAM_ID);
 	}
 	
 	@Override
 	public boolean onCreate() {
 		Log.d("Dribl", "New DribbbleContentProvider instance");
 		return true;
 	}
 	
 	/**
 	 * This method accesses the response cache in a thread-safe way
 	 *  
 	 * @param key
 	 * @param response
 	 */
 	public void addResponseToMemoryCache(String key, Cursor response) {
 		synchronized(responseCache) {
 			if (responseCache.get(key) == null) {
 	            responseCache.put(key, response);
 	        }			
 		}
 	}
 	
 	
 	/**
 	 * Returns the MIME-Type that a query with this URI would return.
 	 * @return MIME-Type
 	 */
 	@Override
 	public String getType(Uri uri) {
 		switch (sURIMatcher.match(uri)) {
 		case STREAM:
 			return DribbbleContract.Image.CONTENT_TYPE;
 		case STREAM_ID:
 			return DribbbleContract.Image.CONTENT_ITEM_TYPE;
 		default:
 			// Unrecognized URI
 			throw new IllegalArgumentException("Unrecognized URI");
 		}
 	}
 
 	/**
 	 * Loads data from the Dribbble API.
 	 * 
 	 * @param uri that points to the content
 	 * @param projection is unsupported
 	 * @param selection is unsupported
 	 * @param selectionArgs is unsupported
 	 * @param sortOrder is unsupported 
 	 * @return Cursor that points to a list of Dribbble images
 	 */
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 		switch (sURIMatcher.match(uri)) {
 		case STREAM:
 		{
 			String listName = uri.getPathSegments().get(0); 
 			Cursor cursor = responseCache.get(listName);
 			
 			// Cache miss
 			if (cursor == null) {
 				cursor = loadStreamAsMatrixCursor("http://api.dribbble.com/shots/" + listName);
 				addResponseToMemoryCache(listName, cursor);
 			}
 			
 			return cursor;
 		}	
 		case STREAM_ID:
 		{
 			long id = Long.parseLong(uri.getLastPathSegment());
 			String listName = uri.getPathSegments().get(0);
 			Cursor cursor = responseCache.get(listName);
 			
 			// Cache miss
 			if (cursor == null) {
 				cursor = loadStreamAsMatrixCursor("http://api.dribbble.com/shots/" + listName);
 				addResponseToMemoryCache(listName, cursor);
 			}
 			
 			// Read through the cursor from the beginning
 			cursor.moveToPosition(-1);
 			while (cursor.moveToNext()) {
 				long currentId = cursor.getLong(cursor.getColumnIndex(DribbbleContract.Image._ID));
 				if (id == currentId) {
 					// Return the cursor with its position set to the requested element
 					return cursor;
 				}
 			}
 			return null;
 		}
 		default:
 			// Unrecognized URI
 			throw new IllegalArgumentException("Unrecognized URI");
 		}
 	}
 	
 	/**
 	 * Downloads data from the Dribbble api and parses the JSON response into a MatrixCursor of shots.
 	 * An example of the response structure can be found here:
 	 * http://api.dribbble.com/shots/popular
 	 *
 	 * projection, selection and sortOrder are unsupported
 	 * 
 	 * @param url
 	 * @return MatrixCursor with data on success, or null on failure
 	 */
 	private static MatrixCursor loadStreamAsMatrixCursor(String url) {
 		String response = loadStringFromUrl(url);
 		if (response == null) {
 			return null;
 		}
 		
 		// Setup the MatrixCursor
 		MatrixCursor out = new MatrixCursor(new String[] {
 				DribbbleContract.Image._ID,
 				DribbbleContract.Image.URL,
				DribbbleContract.Image.IMAGE_URL,
 				DribbbleContract.Image.TITLE,
				DribbbleContract.Image.AUTHOR,
 				DribbbleContract.Image.LIKES_COUNT,
 				DribbbleContract.Image.COMMENTS_COUNT,
				DribbbleContract.Image.REBOUNDS_COUNT
 			});
 		
 		// Parse the JSON response
 		try {
 			JSONObject object = new JSONObject(response);
 			JSONArray shots = object.getJSONArray("shots");
 			for (int i = 0, len = shots.length(); i < len; ++i) {
 				// Get nodes
 				JSONObject shot = shots.getJSONObject(i);
 				JSONObject player = shot.getJSONObject("player");
 				
 				// Add a new row to the MatrixCursor
 				RowBuilder row = out.newRow();
 				
 				// ID, URL, Title, likes, comments, rebounds, author
 				row.add(shot.getLong("id"));
 				row.add(shot.getString("url"));
 				row.add(shot.getString("title"));
 				row.add(shot.getInt("likes_count"));
 				row.add(shot.getInt("comments_count"));
 				row.add(shot.getInt("rebounds_count"));
 				row.add(player.getString("name"));
 				
 				// Always load the non-retina version (400x300)
 				row.add(shot.getString(shot.has("image_400_url") ? "image_400_url" : "image_url"));
 			}
 			
 			return out;
 		} catch (JSONException e) {
 			// The JSON response unexpected, return nothing
 			Log.e("Dribl", e.getClass().getSimpleName() + ": " + e.getMessage());
 			return null;
 		}
 	}
 	
 	private static String loadStringFromUrl(String url) {
 		// Load JSON data (using Apache's http client library)
 		StringBuilder builder = new StringBuilder();
 		HttpClient client = new DefaultHttpClient();
 		HttpGet httpGet = new HttpGet(url);
 		try {
 			HttpResponse response = client.execute(httpGet);
 			StatusLine statusLine = response.getStatusLine();
 			int statusCode = statusLine.getStatusCode();
 			if (statusCode == 200) {
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					builder.append(line);
 				}
 			} else {
 				Log.e("Dribl", "Failed to download the data");
 				return null;
 			}
 		} catch (ClientProtocolException e) {
 			Log.e("Dribl", e.getClass().getSimpleName() + ": " + e.getMessage());
 			return null;
 		} catch (IOException e) {
 			Log.e("Dribl", e.getClass().getSimpleName() + ": " + e.getMessage());
 			return null;
 		}
 		
 		return builder.toString();
 	}
 	
 	@Override
 	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		throw new UnsupportedOperationException();
 	}
 	
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		throw new UnsupportedOperationException();
 	}
 	
 }
