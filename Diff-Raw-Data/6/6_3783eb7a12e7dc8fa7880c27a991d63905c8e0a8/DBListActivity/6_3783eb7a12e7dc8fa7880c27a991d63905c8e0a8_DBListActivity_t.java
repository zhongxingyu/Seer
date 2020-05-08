 package org.raegdan.bbstalker;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.HashMap;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.raegdan.bbstalker.MyLocation.LocationResult;
 
 import android.text.Editable;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.text.format.Time;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.RelativeLayout;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 public class DBListActivity extends ActivityEx implements OnItemClickListener, OnClickListener {
 	
 	BlindbagDB database;
 	DBList dblist;
 	String CurrentBBUniqID = "";
 	Integer CurrentDBListID = 0;
 	String query;
 	int mode;
 	SharedPreferences sp;
 
 	ListView lvDBList;
 	SimpleAdapter saDBList;
 	TextView tvDBHeader;
 
 	PopupWindow pw;
 	View vPWBBInfo;
 	RelativeLayout rlPWBBInfo;
 	TextView tvPWBBInfoName;
 	TextView tvPWBBInfoMisc;
 	ImageView ivPWBBInfoPonyPic;
 	ImageButton ibPWBBWiki;
 	ImageButton ibPWBBShareCommon;
 	ImageButton ibPWBBCart;
 	ImageButton ibPWBBUncart;
 	EditText etPWBBShareShopname;
 	
 	ProgressDialog mDialog;
 	HashMap<String, Object> LocationCache;
 	
 	final static int HM_BY_COUNT 		= 1;
 	final static int HM_BY_PRIORITY 	= 2;
 	
 	final static int MODE_LOOKUP 	 	= 1;
 	final static int MODE_ALL_DB 	 	= 2;
 	final static int MODE_COLLECTION 	= 3;
 	final static int MODE_WAVE 		 	= 4;
 		
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dblist);
 		
 		// Init
 		sp = this.getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
 		
 		mDialog = new ProgressDialog(this);
         mDialog.setCancelable(false);
 		
 		tvDBHeader = (TextView) findViewById(R.id.tvDBHeader);		  
 		
 		lvDBList = (ListView) findViewById(R.id.lvDBList);
 		lvDBList.setOnItemClickListener(this);	
 		
 		LocationCache = new HashMap<String, Object>();
 		LocationCache.put("time", new Time().toMillis(true));
 		LocationCache.put("location", String.valueOf(""));
 		LocationCache.put("timeout", Long.valueOf(300000));
 		
 		try {
 			database = ((BBStalkerApplication) this.getApplication()).database.clone();
 		} catch (CloneNotSupportedException e) {
 			e.printStackTrace();
 		}
 		dblist = new DBList();
 		
 		// Query
 		query = getIntent().getStringExtra("query");
 		mode = getIntent().getIntExtra("mode", 0);	
 		
 		HashMap<String, Object> hm = new HashMap<String, Object>();
 		hm.put("mode", Integer.valueOf(mode));
 		hm.put("query", query);
 		hm.put("context", this);
 		
         mDialog.setMessage(getString(R.string.looking_up));
         mDialog.show();
 		
 		new QueryDatabase().execute(hm);
 	}
 	
 	protected void DBQueryFinished(String TitleMsg)
 	{        
 		switch (mode)
 		{
 			case MODE_COLLECTION:
 			{
 				tvDBHeader.setText(TitleMsg + " (" + Integer.toString(dblist.total_count) + ")");
 				break;
 			}
 			
 			default:
 			{
 				tvDBHeader.setText(TitleMsg);				
 			}
 		}
 		
 		saDBList = new SimpleAdapter(this, dblist.data, R.layout.lvdblist, dblist.fields, dblist.views);
 		lvDBList.setAdapter(saDBList);
 		
 		mDialog.dismiss();
 		
 		if (dblist.data.size() == 0)
 		{
 			tvDBHeader.setText(TitleMsg + "\n\n" + getString(R.string.empty_list));
 			return;
 		} else if (dblist.data.size() == 1)
 		{
 			lvDBListItemClicked(0);
 		}
 	}
 	
 	protected class QueryDatabase extends AsyncTask<HashMap<String, Object>, Integer, String>
 	{
 		protected void PrepareDBList (BlindbagDB database, Context context)
 		{ 
 			dblist.fields = new String[] {"name", "misc", "img1"};
 			dblist.views = new int[] {R.id.tvLVDBListName, R.id.tvLVDBListMisc, R.id.ivVLDBListWavePic};
 			dblist.total_count = 0;
 			
 			for (int i = 0; i < database.blindbags.size(); i++)
 			{
 				if (database.blindbags.get(i).priority == 0)
 				{
 					continue;
 				}
 
 				HashMap<String, Object> hmDBList = new HashMap<String, Object>();
 
 				if (Integer.parseInt(database.blindbags.get(i).waveid) <= 100)
 				{
 					String BBIDsSlash = "";
 					for (int j = 0; j < database.blindbags.get(i).bbids.size(); j++)
 					{
 						BBIDsSlash += database.blindbags.get(i).bbids.get(j);
 						if (j < database.blindbags.get(i).bbids.size() - 1)
 						{
 							BBIDsSlash += " / ";
 						}
 					}
 					
 					hmDBList.put("bbids_slash", BBIDsSlash);
 					hmDBList.put("misc", context.getString(R.string.code) + BBIDsSlash + ", " + context.getString(R.string.in_collection) + database.blindbags.get(i).count.toString());
 				} else {
 					hmDBList.put("misc", context.getString(R.string.set) + database.GetWaveByWaveID(database.blindbags.get(i).waveid).name + ", " + context.getString(R.string.in_collection) + database.blindbags.get(i).count.toString());					
 					hmDBList.put("wave_name", database.GetWaveByWaveID(database.blindbags.get(i).waveid).name);					
 				}
 
 				
 				Integer wavepic = context.getResources().getIdentifier("w" + database.blindbags.get(i).waveid, "drawable", context.getPackageName());
 				hmDBList.put("name", database.blindbags.get(i).name);
 				hmDBList.put("img1", wavepic);
 				hmDBList.put("uniqid", database.blindbags.get(i).uniqid);
 				hmDBList.put("count_int", database.blindbags.get(i).count);
 				hmDBList.put("waveid", database.blindbags.get(i).waveid);	
 				dblist.total_count += database.blindbags.get(i).count;
 				dblist.data.add(hmDBList);
 			}
 		}
 		
 		@Override
 		protected String doInBackground(HashMap<String, Object>... arg0)
 		{
 			BlindbagDB db = new BlindbagDB();
 			
 			String TitleMsg = "";
 			int mode = ((Integer) arg0[0].get("mode")).intValue();
 			String query = ((String) arg0[0].get("query"));
 			Context context = (Context) arg0[0].get("context");
 			
 			switch (mode)
 			{
 				case MODE_ALL_DB:
 				{
 					TitleMsg = context.getString(R.string.all_db);
 					db = database;
 					break;
 				}
 			
 				case MODE_LOOKUP:
 				{
 					TitleMsg = context.getString(R.string.results_for) + query + "";
 					db = database.LookupDB(query, sp.getBoolean("smart_search", true));				
 					break;
 				}
 				
 				case MODE_COLLECTION:
 				{
 					TitleMsg = context.getString(R.string.my_collection);
 					db = database.GetCollection(context);
 					
 					break;
 				}
 				
 				case MODE_WAVE:
 				{
 					if (Integer.parseInt(query) <= 100)
 					{
 						TitleMsg = context.getString(R.string.wave) + query;
 					} else {
 						TitleMsg = database.GetWaveByWaveID(query).name;
 					}
 					db = database.GetWaveBBs(query);
 					break;
 				}
 			
 			}
 
 			PrepareDBList(db, context);
 			
 			return TitleMsg;
 		}
 		
 		@Override
 		protected void onPostExecute (String result)
 		{
 				DBQueryFinished(result);				
 		}
 	}
 	
 	protected void CartUncart(String uniqid, Integer value)
 	{
 		String errmsg = getString(R.string.json_saving_err);
 		Blindbag bb = new Blindbag();
 		
 		int i;
 		
 		for (i = 0; i < database.blindbags.size(); i++)
 		{
 			bb = database.blindbags.get(i);
 			
 			if (bb.uniqid.equalsIgnoreCase(uniqid))
 			{
 				if (bb.count + value < 0)
 				{
 					return;
 				}
 				
 				bb.count += value;
 				dblist.total_count += value;
 				break;
 			}
 		}
 		
 		database.blindbags.set(i, bb);
 		
 		if (!database.CommitDB(this))
 		{
 			Toast.makeText(getApplicationContext(), errmsg, Toast.LENGTH_LONG).show();
 			bb.count -= value;
 			dblist.total_count -= value;
 			database.blindbags.set(i, bb);
 		}
 		
 		dblist.data.get(CurrentDBListID).put("count_int", bb.count);
		if (Integer.parseInt((String) dblist.data.get(CurrentDBListID).get("waveid")) <= 100)
		{
			dblist.data.get(CurrentDBListID).put("misc", getString(R.string.code) + (String) dblist.data.get(CurrentDBListID).get("bbids_slash") + ", " + getString(R.string.in_collection) + ((Integer) dblist.data.get(CurrentDBListID).get("count_int")).toString());
		} else {
			dblist.data.get(CurrentDBListID).put("misc", getString(R.string.set) + (String) dblist.data.get(CurrentDBListID).get("wave_name") + ", " + getString(R.string.in_collection) + ((Integer) dblist.data.get(CurrentDBListID).get("count_int")).toString());					
		}
 
 		tvPWBBInfoMisc.setText(GeneratePWBBMiscText());
 		
 		if (mode == MODE_COLLECTION)
 		{
 			if (bb.count == 0)
 			{
 				dblist.data.remove(CurrentDBListID.intValue());
 				pw.dismiss();
 			}
 			
 			tvDBHeader.setText(getString(R.string.my_collection) + " (" + Integer.toString(dblist.total_count) + ")");
 			
 			if (dblist.total_count == 0)
 			{
 				tvDBHeader.setText(getString(R.string.my_collection) + " (" + Integer.toString(dblist.total_count) + ")\n\n" + getString(R.string.empty_list));				
 			}
 		}
 		
 		saDBList.notifyDataSetChanged();
 	}
 	
 	protected String GeneratePWBBMiscText()
 	{
 		if (Integer.parseInt((String) dblist.data.get(CurrentDBListID).get("waveid")) <= 100)
 		{
 			return getString(R.string.wave) + (String) dblist.data.get(CurrentDBListID).get("waveid") + "\n" + getString(R.string.code) + (String) dblist.data.get(CurrentDBListID).get("bbids_slash") + "\n" + getString(R.string.pcs_in_collection) + ((Integer) dblist.data.get(CurrentDBListID).get("count_int")).toString();			
 		} else {
 			return getString(R.string.set) + (String) dblist.data.get(CurrentDBListID).get("wave_name") + "\n" + getString(R.string.pcs_in_collection) + ((Integer) dblist.data.get(CurrentDBListID).get("count_int")).toString();			
 		}		
 	}
 	
 	protected void lvDBListItemClicked(Integer index)
 	{
 		CurrentBBUniqID = (String) dblist.data.get(index).get("uniqid");
 		CurrentDBListID = index;
 		ShowBBInfo();
 	}
 	
 	protected void ShowBBInfo ()
 	{
 		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		vPWBBInfo = inflater.inflate(R.layout.pwbbinfo, null);
 		rlPWBBInfo = (RelativeLayout) vPWBBInfo.findViewById(R.id.rlPWBBInfo);
 		tvPWBBInfoName = (TextView) rlPWBBInfo.findViewById(R.id.tvPWBBInfoName);
 		tvPWBBInfoMisc = (TextView) rlPWBBInfo.findViewById(R.id.tvPWBBInfoMisc);
 		ivPWBBInfoPonyPic = (ImageView) rlPWBBInfo.findViewById(R.id.ivPWBBInfoPonyPic);
 		ibPWBBWiki = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBWiki);
 		ibPWBBShareCommon = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBShareCommon);
 		etPWBBShareShopname = (EditText) rlPWBBInfo.findViewById(R.id.etPWBBSocialShareShopname);
 		ibPWBBCart = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBCart);
 		ibPWBBUncart = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBUncart);
 		
 		tvPWBBInfoName.setText((String) dblist.data.get(CurrentDBListID).get("name"));
 		tvPWBBInfoMisc.setText(GeneratePWBBMiscText());
 		ivPWBBInfoPonyPic.setImageResource(this.getResources().getIdentifier("bb" + CurrentBBUniqID, "drawable", this.getPackageName()));
 		etPWBBShareShopname.setEllipsize(TextUtils.TruncateAt.END);
 		
 		ibPWBBWiki.setOnClickListener(this);
 		ibPWBBShareCommon.setOnClickListener(this);
 		ibPWBBCart.setOnClickListener(this);
 		ibPWBBUncart.setOnClickListener(this);
 		
 		if (sp.getBoolean("save_shop_name", true))
 		{
 			etPWBBShareShopname.setText(sp.getString("shopname", ""));
 		}
 		etPWBBShareShopname.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				sp.edit().putString("shopname", etPWBBShareShopname.getText().toString()).commit();
 				
 				if (s.toString().charAt(s.toString().length() - 1) == '\n')
 				{
 					SocialShare();
 				}
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 		});
 		
 		etPWBBShareShopname.setOnEditorActionListener(new OnEditorActionListener() {
 
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		    	if (actionId == EditorInfo.IME_ACTION_GO)
 		    	{ 
 		    		SocialShare();
 		    	}
 		    	   
 		    	return true;
 			}
 		});
 		
 		pw = new PopupWindow(this);
 		
 		pw.setContentView(rlPWBBInfo);
 		pw.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
 		pw.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
 		pw.setFocusable(true);
 		
 		pw.showAtLocation(vPWBBInfo, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
 	}
 	
 	
 	///////////// SOCIAL SHARING METHODS LOGIC //////////////
 	//
 	// To share, call SocialShare.
 	//
 	// - SocialShare
 	//   - Geolocation allowed in config?
 	//     - no: ActuallyShare w/o geotag
 	//   - Cached location exists?
 	//     - yes: ActuallyShare with cached location
 	//     - no: 
 	//       - show waiting popup
 	//       - LocationRequest
 	//         - no location providers?
 	//           - hide popup
 	//           - cache null geolocation data
 	//		     - ActuallyShare w/o geotag
 	//         - no location data available? (no GPS satellites visible, no network signal)
 	//  		 - hide popup
 	//           - cache null geolocation data
 	//           - ActuallyShare w/o geotag
 	//         - location data obtained successfully?
 	//           - BitlyRequest
 	//             - change popup text
 	//             - request link compaction
 	//  		   - hide popup
 	//             - Link successfully compacted via Bitly?
 	//               - yes:
 	//                 - cache link
 	//                 - ActuallyShare w/geotag
 	//               - no:
 	//				   - cache null geolocation data	
 	//                 - ActuallyShare w/o geotag
 	//
 	//////////////////////////////////////////////////////////
 	
 	protected void SocialShare()
 	{		
 		if (etPWBBShareShopname.getText().toString().trim().equalsIgnoreCase(""))
 		{
 			Toast.makeText(getApplicationContext(), getString(R.string.no_shop_name), Toast.LENGTH_LONG).show();
 			return;
 		}
 		
 		if (!this.getSharedPreferences(this.getPackageName(), MODE_PRIVATE).getBoolean("allow_geoloc", true))
 		{
 			ActuallyShare(null);
 			return;
 		}
 		
 		Time t = new Time();
 		t.setToNow();
 		
 		if ((t.toMillis(true) - (Long) LocationCache.get("time")) < ((Long) LocationCache.get("timeout")))
 		{
 			ActuallyShare(((String) LocationCache.get("location")));
 		} else {
 	        mDialog.setMessage(getString(R.string.trying_to_locate));
 	        mDialog.setCancelable(false);
 	        mDialog.show();
 	        
 	    	MyLocation myLocation;
 			myLocation = new MyLocation();
 			LocationResult locationResult = new LocationRequest();
 			myLocation.getLocation(this, locationResult);			
 		}
 	}
 	
 	protected class LocationRequest extends LocationResult
 	{
 		
 		@SuppressWarnings("unchecked")
 		@Override
 		public void gotLocation(Location location, int ErrCode) {
 			switch (ErrCode)
 			{
 				case MyLocation.EC_NO_PROVIDERS:
 				{
 					mDialog.dismiss();
 					Time t = new Time();
 					t.setToNow();
 					LocationCache.put("time", t.toMillis(true));
 					LocationCache.put("location", null);
 					ActuallyShare(null);
 					
 					break;
 				}
 				
 				case MyLocation.EC_NO_DATA:
 				{
 					mDialog.dismiss();
 					Time t = new Time();
 					t.setToNow();
 					LocationCache.put("time", t.toMillis(true));
 					LocationCache.put("location", null);
 					ActuallyShare(null);
 					
 					break;
 				}
 				
 				case MyLocation.EC_NO_ERR:
 				{
 					HashMap<String, Object> query = new HashMap<String, Object>();
 					
 					if (sp.getBoolean("allow_geoloc_by_shop", true))
 					{
 						query.put("link", "http://maps.google.com/maps?q=" + etPWBBShareShopname.getText().toString() + " loc:" + Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude()));
 					} else {
 						query.put("link", "http://maps.google.com/maps?q=" + Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude()));
 					}
 					
 					new BitlyRequest().execute(query);
 					
 					break;
 				}
 			}		
 		}	
 	}
 	
 	protected class BitlyRequest extends AsyncTask<HashMap<String, Object>, Integer, HashMap<String, Object>>
 	{
 		@Override
 	    protected void onPreExecute()
 		{
 			super.onPreExecute();
 			mDialog.setMessage(getString(R.string.trying_to_get_link));
 	    }
 
 		@Override
 		protected HashMap<String, Object> doInBackground(HashMap<String, Object>... params)
 		{
 			HashMap<String, Object> result = new HashMap<String, Object>();
 			result.put("link", null);
 				
 			try 
 			{
 				String API_KEY = "a6d306ec04569d71baa6459738622fde5d37262f";
 				String BITLY_API_URL = "https://api-ssl.bitly.com/v3/shorten?access_token=" + API_KEY + "&longUrl=";
 				
 			    HttpClient hc = new DefaultHttpClient(); 
 			    HttpGet hg = new HttpGet(BITLY_API_URL + URLEncoder.encode((String) params[0].get("link"), "UTF-8"));
 			    HttpResponse hr = hc.execute(hg);
 			    HttpEntity he = hr.getEntity();  
 
 			    if (he == null)
 			    {  
 			    	return null;
 			    }
 			    
 			    String response = EntityUtils.toString(he);
 			    
 			    result.put("link", new JSONObject(response).getJSONObject("data").getString("url").replaceAll("http://", "").replaceAll("https://", ""));
 			} 
 			catch (ParseException e) {}
 			catch (JSONException e) {} 
 			catch (UnsupportedEncodingException e) {}
 			catch (IOException e) {}
 			
 			return result;
 		}
 			
 		@Override
 		protected void onPostExecute (HashMap<String, Object> result)
 		{
 			Time t = new Time();
 			t.setToNow();
 			LocationCache.put("time", t.toMillis(true));
 			LocationCache.put("location", (String) result.get("link"));
 				
 			mDialog.dismiss();
 			ActuallyShare((String) result.get("link"));		
 		}
 	}
 	
 	protected void ActuallyShare(String GeoLink)
 	{		
 		String message = "";
 		
 		if (Integer.parseInt((String) dblist.data.get(CurrentDBListID).get("waveid")) <= 100)
 		{
 			message = getString(R.string.social_msg_p1) + (String) dblist.data.get(CurrentDBListID).get("waveid") + getString(R.string.social_msg_p2) + (String) dblist.data.get(CurrentDBListID).get("name") + getString(R.string.social_msg_p3) + etPWBBShareShopname.getText().toString();			
 		} else {
 			message = getString(R.string.social_msg_set_p1) + (String) dblist.data.get(CurrentDBListID).get("wave_name") + getString(R.string.social_msg_set_p2) + etPWBBShareShopname.getText().toString();						
 		}
 		if (GeoLink != null)
 		{
 			message += getString(R.string.social_msg_p4) + GeoLink;
 		}
 		message += getString(R.string.social_msg_p5);
 		
 		Share(message);
 	}
 	
 	void Share(String text)
 	{
 		Intent ss = new Intent(Intent.ACTION_SEND);
 		ss.putExtra(Intent.EXTRA_TEXT, text);
 		ss.setType("text/plain");
 
 		this.startActivity(Intent.createChooser(ss, getString(R.string.share)));
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		switch (arg0.getId())
 		{
 			case R.id.lvDBList:
 				lvDBListItemClicked(arg2);
 				break;
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		Blindbag CurrentBlindbag = database.GetBlindbagByUniqID(CurrentBBUniqID);
 		
 		switch (v.getId())
 		{
 			case R.id.ibPWBBWiki:
 				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(CurrentBlindbag.wikiurl)));
 				break;
 				
 			case R.id.ibPWBBShareCommon:
 				SocialShare();			
 				break;
 				
 			case R.id.ibPWBBCart:
 				CartUncart(CurrentBBUniqID, +1);
 				break;
 
 			case R.id.ibPWBBUncart:
 				CartUncart(CurrentBBUniqID, -1);
 				break;
 
 		}
 	}
 }
