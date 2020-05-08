 package se.chalmers.h_sektionen;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import se.chalmers.h_sektionen.adapters.ContactCardArrayAdapter;
 import se.chalmers.h_sektionen.containers.ContactCard;
 import se.chalmers.h_sektionen.utils.CacheCompass;
 import se.chalmers.h_sektionen.utils.Constants;
 import se.chalmers.h_sektionen.utils.LoadData;
 import se.chalmers.h_sektionen.utils.MenuItems;
 import se.chalmers.h_sektionen.utils.PicLoaderThread;
 
 /**
  * InfoActivity takes care about the info view
  */
 public class InfoActivity extends BaseActivity {
 	
 	private List<ContactCard> contactCards = null;
 	private StringBuilder htmlLinks = null;
 	private StringBuilder openingHoursString = null;
 	
 	/**
 	 * Sets the static "currentView" variable in the super class, BaseActivity.
 	 * The method also start the AsyncTask that fetches the information data.
 	 */
 	@Override
 	protected void onResume() {
 		
 		setCurrentView(MenuItems.INFO);
 		new GetInfoTask().execute(Constants.INFO);
 		
 		super.onResume();
 	}
 	
 	
 	/**
 	 * GetInfoTask loads the info data and sets up the view.
 	 */
 	private class GetInfoTask extends AsyncTask<String, String, Boolean> {
 
 		/**
 		 * Runs super method and starts the load animation.
 		 */
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			
 			runLoadAnimation();
 		}
 		
 		/**
 		 * Requests a JSONObject, tries to parse it and puts the data into arrays.
 		 * @return false if the parsing did not succeed, else true
 		 */
 		@Override
 		protected Boolean doInBackground(String... params) {
 			
 			if(!connectedToInternet())
 				return false;
 				
 			// Try to download and prepare all info data.
 			try {
 				JSONObject json = new JSONObject(LoadData.getJSON(params[0]));
 				
 				// Board members
 				JSONArray members = json.getJSONArray("members");
 				contactCards = new ArrayList<ContactCard>();
 	    		
 	    		for (int i=0; i<members.length(); i++) {
 	    			String name = members.getJSONObject(i).getString("name");
 	    			String position = members.getJSONObject(i).getString("position");
 	    			String email = members.getJSONObject(i).getString("email");
 	    			String picAddr = members.getJSONObject(i).getString("picture");
 	    			String phoneNumber = members.getJSONObject(i).getString("phone");
 	    			Bitmap pic;
 	    			
 	    			// Download the picture if it does not exists in the cache.
 	    			if(CacheCompass.getInstance(InfoActivity.this).getBitmapCache().get(picAddr)==null){
 	    				
 						PicLoaderThread pcl = new PicLoaderThread(picAddr);
 						pcl.start();
 						
 						try {
 							pcl.join();
 							pic = pcl.getPicture();
 							pic.prepareToDraw();
 							CacheCompass.getInstance(InfoActivity.this).getBitmapCache().put(picAddr, pic);
 						
						} catch (Exception e) {
 							pic = null;
 						}
 						
 					} else {
 						pic = CacheCompass.getInstance(InfoActivity.this).getBitmapCache().get(picAddr);
 					}
 					
 	    			contactCards.add(new ContactCard(name, position, email, phoneNumber, pic));
 	    		}
 	    		
 	    		// Create a HTML-string containing links. The TextView can handle HTML
 	    		JSONArray links = json.getJSONArray("links");
 	    		htmlLinks = new StringBuilder();
 	    		
 	    		for (int i=0; i<links.length(); i++) {
 	    			htmlLinks.append("<a href=\"");
 	    			htmlLinks.append(links.getJSONObject(i).getString("href"));
 	    			htmlLinks.append("\">");
 	    			htmlLinks.append(links.getJSONObject(i).getString("name"));
 	    			htmlLinks.append("</a><br />");
 	    		}
 	    		
 	    		// Create a HTML-string containing opening hours. 
 	    		JSONArray openingHours = json.getJSONArray("openinghours");
 	    		openingHoursString = new StringBuilder();
 	    		
 	    		for (int i=0; i<openingHours.length(); i++) {
 	    			openingHoursString.append("<b>");
 	    			openingHoursString.append(openingHours.getJSONObject(i).getString("name"));
 	    			openingHoursString.append("</b>:<br />");
 	    			openingHoursString.append(openingHours.getJSONObject(i).getString("opentime"));
 	    			openingHoursString.append("<br />");
 	    		}
 	    		
 				return true;
 				
 			} catch (Exception e) {
 				// Incorrect JSON string.
 				return false;
 			}
 		}
 		
 		/**
 		 * Sets up the view with data from doInBackground method.
 		 * If doInBackground returns false, an error view is going to show up.
 		 * @param done True if doInBackground could parse and prepare the data correct, else false.
 		 */
 		@Override
 		protected void onPostExecute(Boolean done) {
 			super.onPostExecute(done);
 			
 			// If there was no error (connection or JSON string error), set up the view.
 			if (done) {
 				getFrameLayout().removeAllViews();
 				getFrameLayout().addView(getLayoutInflater().inflate(R.layout.view_info, null));
 				
 				ListView contactListView = (ListView) findViewById(R.id.contact_info_list);
 	    		contactListView.setCacheColorHint(Color.WHITE);
 	    		
 	    		LinearLayout linksLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.info_links, null);
 	    		TextView linksTextView = (TextView)linksLayout.findViewById(R.id.links);
 	    		TextView openingHoursTextView = (TextView)linksLayout.findViewById(R.id.opening_hours);
 	    		
 	    		linksTextView.setMovementMethod(LinkMovementMethod.getInstance());
 	    		linksTextView.setText(Html.fromHtml(htmlLinks.toString()));
 	    		
 	    		openingHoursTextView.setText(Html.fromHtml(openingHoursString.toString()));
 	    		contactListView.addHeaderView(linksLayout);
 	    		contactListView.setAdapter(new ContactCardArrayAdapter(InfoActivity.this, R.layout.contact_list_item, contactCards));
 			
 			} else {
 				setErrorView(getString(R.string.INFO_ERROR));
 			}
 		}
 		
 	}
 	
 }
