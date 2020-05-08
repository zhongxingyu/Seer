 package de.laxu.apps.sharedurllist;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 import android.support.v4.view.ViewPager;
 import android.widget.LinearLayout;
 
 class LoadException extends Exception{
 	private static final long serialVersionUID = 1L;
 	private String error;
 	public LoadException(String error) {
 		this.error = error;
 	}
 	public String getError(){
 		return this.error;
 	}
 }
 
 class LoadUrlListTask extends AsyncTask<Void, Void, String> {
 
 	private final MainActivity mainActivity;
 	private String url; 
 	/**
 	 * @param mainActivity
 	 */
 	LoadUrlListTask(MainActivity mainActivity) {
 		this.mainActivity = mainActivity;
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
 		String serverurl = sharedPrefs.getString("pref_serverurl", "");
 		String token = sharedPrefs.getString("pref_token", "");
 		this.url=serverurl + "?api=true&token="+Uri.encode(token);
 	}
 
 	private String oldHostname;
 
 	@Override
 	protected String doInBackground(Void... params) {
 		int currentPage = mainActivity.mViewPager.getCurrentItem();
 		if (MainActivity.hostnames.size() - 1 >= currentPage) {
 			oldHostname = MainActivity.hostnames.get(currentPage);
 		} else {
 			oldHostname = "";
 		}
 		String json_input;
 		try{
 			json_input = Util.loadFromURL(mainActivity, url);
 			JSONObject json = new JSONObject(json_input);
 			if (!json.get("status").equals("success")) {
 				String errormessage = json.getString("errormessage");
 				return "Server Error: " + errormessage;
 			}
 			JSONArray hosts = (json.getJSONArray("hosts"));
			MainActivity.hostnames = new ArrayList<String>();
 			for (int i = 0; i < hosts.length(); i++) {
 				JSONObject host = hosts.getJSONObject(i);
 				String hostname = host.getString("hostname");
 				MainActivity.hostnames.add(hostname);
 				JSONArray json_urls = host.getJSONArray("urls");
 				ArrayList<UrlListEntry> url_list = new ArrayList<UrlListEntry>();
 				for (int j = 0; j < json_urls.length(); j++) {
 					JSONObject json_url = (JSONObject) json_urls.get(j);
 					String link = json_url.getString("link");
 					String created = json_url.getString("created");
 					url_list.add(new UrlListEntry(link, created));
 				}
 				MainActivity.hostURLList.put(hostname, url_list);
 			}
 			java.util.Collections.sort(MainActivity.hostnames);
 			return null;
 		} catch (JSONException e) {
 			return "JSON Error";
 		}catch (LoadException e) {
 			return e.getError();
 		}
 	}
 
 	@Override
 	protected void onPreExecute() {
 		mainActivity.infoMessage("updating ...");
 	};
 
 	@Override
 	protected void onPostExecute(String errormessage) {
 		if (errormessage == null) {
 			LinearLayout layout = (LinearLayout) mainActivity
 					.findViewById(R.id.mainLayout);
 			layout.invalidate();
 			// mSectionsPagerAdapter = new
 			// SectionsPagerAdapter(getSupportFragmentManager());
 			mainActivity.mViewPager = (ViewPager) mainActivity
 					.findViewById(R.id.pager);
 			mainActivity.mViewPager
 					.setAdapter(mainActivity.mSectionsPagerAdapter);
 			mainActivity.mSectionsPagerAdapter.notifyDataSetChanged();
 			int newPage = MainActivity.hostnames.indexOf(oldHostname);
 			if (newPage != -1) {
 				mainActivity.mViewPager.setCurrentItem(newPage);
 			}
 			mainActivity.hideMessage();
 		} else {
 			mainActivity.errorMessage(errormessage);
 		}
 	}
 
 }
