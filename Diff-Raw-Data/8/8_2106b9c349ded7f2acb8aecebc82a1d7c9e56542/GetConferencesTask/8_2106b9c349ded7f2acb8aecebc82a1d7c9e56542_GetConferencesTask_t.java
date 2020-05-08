 /*******************************************************************************
  * Copyright (c) 2012 Matt Barringer <matt@incoherent.de>.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  *     Matt Barringer <matt@incoherent.de> - initial API and implementation
  ******************************************************************************/
 
 package de.incoherent.suseconferenceclient.tasks;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.SocketException;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import de.incoherent.suseconferenceclient.SUSEConferences;
 import de.incoherent.suseconferenceclient.app.Database;
 import de.incoherent.suseconferenceclient.app.HTTPWrapper;
 import de.incoherent.suseconferenceclient.models.Conference;
 
 import android.os.AsyncTask;
import android.util.Log;
 
 public class GetConferencesTask extends AsyncTask<Void, Void, ArrayList<Conference>> {
 	public interface ConferenceListListener {
 		public void conferencesDownloaded(ArrayList<Conference> conferences);
 	}
 	
 	private ConferenceListListener mListener;
 	private String mUrl;
 	private Database mDb;
 
 	public GetConferencesTask(String url, ConferenceListListener listener) {
 		this.mListener = listener;
		this.mUrl = url + "/conferences.json";
 		this.mDb = SUSEConferences.getDatabase();
		Log.d("NIKHATZI","Wrapper.get mUrl: " + mUrl);
 	}
 	
 	@Override
 	protected ArrayList<Conference> doInBackground(Void... params) {
 		JSONObject reply = null;
 		ArrayList<Conference> ret = new ArrayList<Conference>();
 		
 		try {
 			reply = HTTPWrapper.get(mUrl);
 			JSONArray conferences = reply.getJSONArray("conferences");
			
 			int len = conferences.length();
 			for (int i = 0; i < len; i++) {
 				JSONObject jsonCon = conferences.getJSONObject(i);
 				Conference newCon = new Conference();
 				newCon.setGuid(jsonCon.getString("guid"));
 				newCon.setName(jsonCon.getString("name"));
 				newCon.setDescription(jsonCon.getString("description"));
 				newCon.setYear(jsonCon.getInt("year"));
 				newCon.setName(jsonCon.getString("name"));
 				newCon.setDateRange(jsonCon.getString("date_range"));
 				newCon.setUrl(jsonCon.getString("url"));
 				newCon.setSocialTag(jsonCon.getString("socialtag"));
 				newCon.setIsCached(false);
 				long sqlId = mDb.getConferenceIdFromGuid(newCon.getGuid());
 				if (sqlId == -1) {
 					sqlId = mDb.addConference(newCon);
 					if (jsonCon.has("revision"))
 						mDb.setLastUpdateValue(sqlId, jsonCon.getInt("revision"));
 				}
 				newCon.setSqlId(sqlId);
 				
 				ret.add(newCon);
 			}
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (SocketException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
     	try {Thread.sleep(13000);} catch(Exception e){};
 
 		return ret;
 	}
 
 	protected void onPostExecute(ArrayList<Conference> conferences) {
 		mListener.conferencesDownloaded(conferences);
 	}
 }
