 package eecs.berkeley.edu.cs294;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.util.Log;
 
 public class PushServerTodo extends Activity {
 
 	/*
 	 * Create a new todo in the server. Called when a user create a corresponding one locally.
 	 */
 	public static int create(List<String> entry) {
 		String groupID = entry.get(DatabaseHelper.GROUP_ID_INDEX_T);
 		String groupRailsID = ToDo_Replica.dh.select_group("name", groupID).get(
 				DatabaseHelper.GROUP_RAILS_ID_INDEX_G);
 		
 		String url = ServerConnection.homeurl + ServerConnection.tmp_groups_link + 
 		groupRailsID + ServerConnection.todolink;
 		
 		Log.d("ServerDEBUG", "POST to " + url);
 		
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpPost postRequest = new HttpPost(url);
 		
 		JSONObject tododetail = new JSONObject();
 		JSONObject details = new JSONObject();
 
 		/* Setting up the packet to be sent to server */
 		try {
 			details.put("title", entry.get(DatabaseHelper.TITLE_INDEX_T));
 			details.put("place", entry.get(DatabaseHelper.PLACE_INDEX_T));
 			details.put("note", entry.get(DatabaseHelper.NOTE_INDEX_T));
 			details.put("tag", entry.get(DatabaseHelper.TAG_INDEX_T));
 			details.put("status", entry.get(DatabaseHelper.STATUS_INDEX_T));
 			details.put("priority", entry.get(DatabaseHelper.PRIORITY_INDEX_T));
 			details.put("deadline", entry.get(DatabaseHelper.DEADLINE_INDEX_T));
 
 			tododetail.put("tododetail", details);
 
 			Log.d("ServerDEBUG", "Todo JSON = "+ tododetail.toString());
 
 			StringEntity se = new StringEntity(tododetail.toString());
 
 			postRequest.setEntity(se);
 			postRequest.setHeader("Content-Type","application/json");
 			postRequest.setHeader("Accept", "application/json");
 			
 		} catch (UnsupportedEncodingException e) {
 			Log.e("Error",""+e);
 			e.printStackTrace();	
 			return -1;
 		
 		} catch (JSONException js) {
 			js.printStackTrace();	
 			return -1;
 		}
 
 		/* Sending... */
 		HttpResponse response = null;
 		try {
 			response = client.execute(postRequest);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			Log.e("ClientProtocol",""+e);
 			return -1;
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.e("IO",""+e);
 			return -1;
 		}
 
 		/* Parsing the response from the server */
 		HttpEntity entity = response.getEntity();
 		String stringResponse = ServerConnection.getResponse(entity);
 		
 		if (entity != null) {
 			try {
 				entity.consumeContent();
 			} catch (IOException e) {
 				Log.e("IO E",""+e);
 				e.printStackTrace();
 				return -1;
 			}
 		}
 
 		Log.d("ServerDEBUG", "response: " + stringResponse);
 		
 		try {
 			JSONObject jObject = new JSONObject(stringResponse);
 			JSONObject tododetailObject = jObject.getJSONObject("tododetail");
 			String railsID = tododetailObject.getString("id");
 			
 			int pk = Integer.parseInt(entry.get(DatabaseHelper.TD_ID_INDEX_T));
			// ToDo_Replica.dh.update_to_do(pk, null, null, null, null, null, null, null, null, null, railsID);
 		} catch (Exception e) {
 			Log.e("JSON E", ""+e);
 			e.printStackTrace();
 			return -1;
 		}
 
 		return 0;
 	}
 	
 	/*
 	 * Updates a todo in the server. Called when a user updates a corresponding one locally.
 	 */
 	public static int update(List<String> entry) {		
 		String groupID = entry.get(DatabaseHelper.GROUP_ID_INDEX_T);
 		String groupRailsID = ToDo_Replica.dh.select_group("name", groupID).get(
 				DatabaseHelper.GROUP_RAILS_ID_INDEX_G);
 		String url = ServerConnection.homeurl + ServerConnection.tmp_groups_link + 
 		groupRailsID + ServerConnection.todolink + 
 		entry.get(DatabaseHelper.TO_DO_RAILS_ID_INDEX_T); 
 		
 		Log.d("ServerDEBUG", "PUT to " + url);
 		
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpPut putRequest = new HttpPut(url);
 		
 		JSONObject tododetail= new JSONObject();
 		JSONObject details = new JSONObject();
 
 		/* Setting up the packet to be sent to server */
 		try {
 			String group_id = ToDo_Replica.dh.select_group("name", 
 					entry.get(DatabaseHelper.GROUP_ID_INDEX_T)).
 					get(DatabaseHelper.GROUP_RAILS_ID_INDEX_G);
 			
 			details.put("title", entry.get(DatabaseHelper.TITLE_INDEX_T));
 			details.put("place", entry.get(DatabaseHelper.PLACE_INDEX_T));
 			details.put("note", entry.get(DatabaseHelper.NOTE_INDEX_T));
 			details.put("tag", entry.get(DatabaseHelper.TAG_INDEX_T));
 			details.put("group-id", group_id);
 			details.put("status", entry.get(DatabaseHelper.STATUS_INDEX_T));
 			details.put("priority", entry.get(DatabaseHelper.PRIORITY_INDEX_T));
 			details.put("deadline", entry.get(DatabaseHelper.DEADLINE_INDEX_T));
 
 			tododetail.put("tododetail", details);
 
 			Log.d("ServerDEBUG", "Event JSON = "+ tododetail.toString());
 
 			StringEntity se = new StringEntity(tododetail.toString());
 
 			putRequest.setEntity(se);
 			putRequest.setHeader("Content-Type","application/json");
 			putRequest.setHeader("Accept", "application/json");
 			
 		} catch (UnsupportedEncodingException e) {
 			Log.e("Error",""+e);
 			e.printStackTrace();	
 			return -1;
 		
 		} catch (JSONException js) {
 			js.printStackTrace();	
 			return -1;
 		}
 
 		/* Sending... */
 		HttpResponse response = null;
 		try {
 			response = client.execute(putRequest);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			Log.e("ClientProtocol",""+e);
 			return -1;
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.e("IO",""+e);
 			return -1;
 		}
 
 		/* Parsing the response from the server */
 		HttpEntity entity = response.getEntity();
 		String stringResponse = ServerConnection.getResponse(entity);
 		if (entity != null) {
 			try {
 				entity.consumeContent();
 			} catch (IOException e) {
 				Log.e("IO E",""+e);
 				e.printStackTrace();
 				return -1;
 			}
 		}
 
 		Log.d("ServerDEBUG", "response: " + stringResponse);
 
 		return 0;
 	}
 
 	/*
 	 * Deletes a todo in the server. Called when a user deletes a corresponding one locally.
 	 */
 	public static int delete(List<String> entry) {
 		String groupID = entry.get(DatabaseHelper.GROUP_ID_INDEX_T);
 		String groupRailsID = ToDo_Replica.dh.select_group("name", groupID).get(
 				DatabaseHelper.GROUP_RAILS_ID_INDEX_G);
 		String url = ServerConnection.homeurl + ServerConnection.tmp_groups_link + 
 		groupRailsID + ServerConnection.todolink + 
 		entry.get(DatabaseHelper.TO_DO_RAILS_ID_INDEX_T);
 		
 		Log.d("ServerDEBUG", "DELETE to " + url);
 		
 		DefaultHttpClient client = new DefaultHttpClient();
 		
 		HttpDelete deleteRequest = new HttpDelete(url);
 		HttpResponse response = null;
 		
 		/* Sending... */
 		try {
 			response = client.execute(deleteRequest);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			Log.e("ClientProtocol",""+e);
 			return -1;
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.e("IO",""+e);
 			return -1;
 		}
 
 		/* Parsing the response from the server */
 		HttpEntity entity = response.getEntity();
 		String stringResponse = ServerConnection.getResponse(entity);
 		if (entity != null) {
 			try {
 				entity.consumeContent();
 			} catch (IOException e) {
 				Log.e("IO E",""+e);
 				e.printStackTrace();
 				return -1;
 			}
 		}
 
 		Log.d("ServerDEBUG", "response: " + stringResponse);
 		return 0;
 	}		
 }
