 package uk.co.huntersix.android.audiowalks;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import uk.co.huntersix.android.audiowalks.model.TravelWalk;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class WalksListViewActivity extends ListActivity {
 	private ProgressBar progressBar;
 	private EfficientAdapter adapter;
 	private static List<TravelWalk> travelWalks;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list);
 		
         progressBar = (ProgressBar)findViewById(R.id.progressbar_Horizontal);
         progressBar.setProgress(0);
         
         new LoadAPIContentTask().execute("http://content.guardianapis.com/search?q=london-walks&section=travel&format=json&show-fields=standfirst%2Clongitude%2Clatitude%2Cthumbnail&api-key=pbpw3gnyu3kcna9eqe736aj6");
 	}
 
 	public void showList(List<TravelWalk> results) {
 		travelWalks = results;
 		
         adapter = new EfficientAdapter(this);
 		setListAdapter(adapter);
 		
 		progressBar.setVisibility(View.INVISIBLE);
 	}
 	
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 
 		Intent intent = new Intent(this, WalkViewActivity.class);
 		TravelWalk travelWalk = travelWalks.get(position);
 		intent.putExtra("travelWalk.title", travelWalk.title);
 		intent.putExtra("travelWalk.description", travelWalk.description);
 		intent.putExtra("travelWalk.imageUrl", travelWalk.imageUrl);
 		intent.putExtra("travelWalk.latitude", travelWalk.latitude);
 		intent.putExtra("travelWalk.longitude", travelWalk.longitude);
 		intent.putExtra("travelWalk.mapUrl", travelWalk.mapUrl);
     	this.startActivity(intent);
 	}
 
 	public static class EfficientAdapter extends BaseAdapter implements Filterable {
 		private LayoutInflater inflater;
 		private Bitmap icon;
 		private Context context;
 
 		public EfficientAdapter(Context context) {
 			// Cache the LayoutInflate to avoid asking for a new one each time.
 			inflater = LayoutInflater.from(context);
 			this.context = context;
 		}
 
 		/**
 		 * Make a view to hold each row.
 		 * 
 		 * @see android.widget.ListAdapter#getView(int, android.view.View,
 		 *      android.view.ViewGroup)
 		 */
 		public View getView(final int position, View convertView, final ViewGroup parent) {
 			// A ViewHolder keeps references to children views to avoid
 			// unnecessary calls to findViewById() on each row.
 			ViewHolder holder;
 			
 			// When convertView is not null, we can reuse it directly, there is no need
 			// to re-inflate it. We only inflate a new View when the convertView supplied
 			// by ListView is null.
 			if (convertView == null) {
 				convertView = inflater.inflate(R.layout.listrow, null);
 				// Creates a ViewHolder and store references to the two children
 				// views we want to bind travelWalks to.
 				holder = new ViewHolder();
 				holder.textLine = (TextView) convertView.findViewById(R.id.textLine);
 				holder.description = (TextView) convertView.findViewById(R.id.description);
 				holder.walkThumbnail = (ImageView) convertView.findViewById(R.id.iconLine);
 //
 //				convertView.setOnClickListener(new OnClickListener() {
 //					public void onClick(View v) {
 //						Toast.makeText(context, "Selected: " + travelWalks.get(position).title, Toast.LENGTH_SHORT).show();
 ////						Intent intent = new Intent(parent.getContext(), WalkViewActivity.class);
 ////						parent.getContext().startActivity(intent);
 //					}
 //				});
 
 				convertView.setTag(holder);
 			} 
 			else {
 				// Get the ViewHolder back to get fast access to the TextView
 				// and the ImageView.
 				holder = (ViewHolder) convertView.getTag();
 			}
 			
 			// Bind the travelWalks efficiently with the holder.
 			holder.textLine.setText(travelWalks.get(position).title);
 			holder.description.setText(Html.fromHtml(travelWalks.get(position).description));
 			if ((travelWalks.get(position).imageUrl != null) && (travelWalks.get(position).imageUrl.trim().length() != 0)) {
 //				holder.walkThumbnail.setBackgroundDrawable(drawableManager.fetchDrawable(travelWalks.get(position).imageUrl));
 			}
 
 			return convertView;
 		}
 
 		static class ViewHolder {
 			TextView textLine;
 			TextView description;
 			ImageView walkThumbnail;
 		}
 
 		public Filter getFilter() {
 			return null;
 		}
 
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		public int getCount() {
 			return travelWalks.size();
 		}
 
 		public Object getItem(int position) {
 			return travelWalks.get(position);
 		}
 	}
 
     private class LoadAPIContentTask extends AsyncTask<String, Integer, List<TravelWalk>> {    	
 		@Override
 		protected List<TravelWalk> doInBackground(String... args) {
 	    	int myProgress = 0;
 	    	
 	    	List<TravelWalk> travelWalksFound = new ArrayList<TravelWalk>();
 
         	try {
 				String uri = args[0];
 				HttpClient client = new DefaultHttpClient();
 		        HttpGet request = new HttpGet();
 		        request.setURI(new URI(uri));
 		        
 		        String json = client.execute(request, new BasicResponseHandler());
 		        
 		        System.out.println(json);
 		        
 		        JSONObject response = new JSONObject(json).getJSONObject("response");
 		        String status = response.getString("status");
 		        if ("ok".equalsIgnoreCase(status)) {
 		        	JSONArray results = response.getJSONArray("results");
 		        	int incrementor = 100 / results.length();
 		        	for (int i=0; i<results.length(); i++) {
 		        		boolean useThisResult = true;
 		        		JSONObject result = results.getJSONObject(i);
 		        		TravelWalk travelWalk = new TravelWalk();
 		        		travelWalk.title = result.getString("webTitle");
 		        		JSONObject fields = result.getJSONObject("fields");
 		        		travelWalk.description = (fields.getString("standfirst") == null) ? "" : fields.getString("standfirst");
 		        		if (travelWalk.description.contains("http://maps.google.co.uk")) {
 		        			String mapUrl = travelWalk.description.substring(travelWalk.description.indexOf("http://maps.google.co.uk"));
 		        			travelWalk.mapUrl = mapUrl.substring(0, mapUrl.indexOf("\""));
 		        		}
 		        		else {
 		        			useThisResult = false;
 		        		}
		        		travelWalk.imageUrl = (fields.getString("thumbnail") == null) ? "" : fields.getString("thumbnail");
 		        		try {
 			        		travelWalk.longitude = (fields.getString("longitude") == null) ? "" : fields.getString("longitude");
 			        		travelWalk.latitude = (fields.getString("latitude") == null) ? "" : fields.getString("latitude");
 		        		}
 		        		catch (Exception e) {
 		        			// Ignore results that do not have geo-location details
 		        			useThisResult = false;
 		        		}
 		        		
 		        		if (useThisResult) {
 		        			travelWalksFound.add(travelWalk);
 		        		}
 
 		        		myProgress += incrementor;
 		    			publishProgress(myProgress);
 		        	}
 					
 					return travelWalksFound;
 			    }
 			}
 			catch (Exception e) {
 				System.out.println("****************");
 				System.out.println(e.getMessage());
 			}
         	
         	return travelWalksFound;
 		}
 
 		@Override
 		protected void onPostExecute(List<TravelWalk> results) {
 			publishProgress(100);
 			
 			showList(results);
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			progressBar.setProgress(values[0]);
 		}
     }
 }
