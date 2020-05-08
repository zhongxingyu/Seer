 package com.cellcity.citiguide.screen;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.cellcity.citiguide.info.MerchantInfo1;
 import com.cellcity.citiguide.info.MerchantInfo2;
 import com.cellcity.citiguide.util.Constants;
 import com.cellcity.citiguide.util.Util;
 
 public class MerchantListingScreen extends CitiGuideListActivity {
 
 	public static MerchantListingScreen instance;
 	public static ArrayList<MerchantInfo2> merchantList = null;
 	public static String catKeyword = "";
 	private ProgressDialog progressDialog = null;
 	private Runnable initR;
 	
 	public static Bitmap bmp;
 	
 	private ListViewAdapter m_adapter;
 
 	public static int page = 1;
 	public static int totalPage = 1;
 	private int startItem, endItem, totalItems;
 
 	// share data
 	private String headerTxt;
 	public static MerchantInfo1 merchantInfo;
 	public static String querySearch = null;
 	
 	private String URL = "";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTheme(R.style.Theme_Translucent);
 		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.listingmerchant);
 
 		instance = this;
 
 		initActivity(instance, "");
 		
 		ARScreen.isMerchantList = true;
 
 		page = 1;
 
 		init();
 	}
 
 	private void init() {
 		settingLayout();
 
 		// display progress dialog
 		try {
 			progressDialog = ProgressDialog.show(this, "", getString(R.string.retrieving_data), true);
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 
 		// start thread
 		initR = new Runnable() {
 			@Override
 			public void run() {
 				getJSON();
 				runOnUiThread(returnRes);
 			}
 		};
 		Thread thread = new Thread(null, initR, "initR");
 		thread.start();
 	}
 	
 	private Runnable returnRes = new Runnable() {
 		
 		@Override
 		public void run() {
 			if (merchantList != null && merchantList.size() > 0) {
 				m_adapter.notifyDataSetChanged();
 				for (int i = 0; i < merchantList.size(); i++) {
 					m_adapter.add(merchantList.get(i));
 				}
 			}
 			else {
 				
 			}
 			try {
 				if (progressDialog.isShowing()) {
 					progressDialog.dismiss();
 				}
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 			settingNextBackButton();
 		}
 	};
 
 	protected void getJSON() {
 		System.out.println(URL);
 		
 		try {
 			String result = "";
 			result = Util.getHttpData(URL + page);
 			if(result == null || result.equalsIgnoreCase("408") || result.equalsIgnoreCase("404")) {
 				Util.showAlert(instance, "f.y.i. Singapore", "Please make sure Internet connection is available.", "OK", true);
 				try {
 					if (progressDialog.isShowing()) {
 						progressDialog.dismiss();
 					}					
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			else {
 				result = Util.toJSONString(result);
 				merchantList = new ArrayList<MerchantInfo2>();
 				
 				try {
 					JSONObject jsonObject1 = new JSONObject(result);
 					JSONArray nameArray = jsonObject1.getJSONArray("data");
 					
 					try {
 						for(int i = 0; i < nameArray.length(); i++) {
 							JSONObject jsonObject2 = nameArray.getJSONObject(i);
 							
 							int id = 0;
 							String image = "";
 							String outletName = "";
 							String address = "";
 							double latitude = 0;
 							double longitude = 0;
 							
 							id = Integer.parseInt(jsonObject2.getString("ID"));
 							image = jsonObject2.getString("Image");
 							outletName = jsonObject2.getString("OutletName");
 							address = jsonObject2.getString("Address");
 							latitude = Double.parseDouble(jsonObject2.getString("Latitude"));
 							longitude = Double.parseDouble(jsonObject2.getString("Longitude"));
 							
 							MerchantInfo2 mInfo = new MerchantInfo2(id, image, outletName, address, latitude, longitude);
 							merchantList.add(mInfo);
 						}
 						
 						totalItems = jsonObject1.getInt("totalResults");
 						totalPage = totalItems / Constants.ITEMS_PER_PAGE;
 						if (totalItems % Constants.ITEMS_PER_PAGE != 0) {
 							totalPage += 1;
 						}
 						
 						startItem = (page * 20) - 19;
 						endItem = page * 20;
 						
 						if(endItem > totalItems) {
 							endItem = totalItems;
 						}
 					}
 					catch(Exception e) {
 						Util.showAlert(instance, "f.y.i. Singapore", "No items found.", "OK", true);
 						try {
 							if (progressDialog.isShowing()) {
 								progressDialog.dismiss();
 							}					
 						}
 						catch (Exception ex) {
 							ex.printStackTrace();
 						}
 						e.printStackTrace();
 					}
 				}
 				catch(Exception e) {
 					Util.showAlert(instance, "f.y.i. Singapore", "No items found.", "OK", true);
 					try {
 						if (progressDialog.isShowing()) {
 							progressDialog.dismiss();
 						}
 					}
 					catch (Exception ex) {
 						ex.printStackTrace();
 					}
 					e.printStackTrace();
 				}
 			}
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected void settingNextBackButton() {
 		TextView result = (TextView) findViewById(R.id.tabText);
 		result.setText("Results (" + startItem + " - " + endItem + ") of "
 				+ totalItems);
 
 		ImageView tabBack = (ImageView) findViewById(R.id.tabBack);
 		tabBack.setImageResource(R.drawable.button_cust_tab_back);
 		tabBack.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (page <= totalPage) {
 					page--;
 					init();
 				}
 			}
 		});
 		ImageView tabNext = (ImageView) findViewById(R.id.tabNext);
 		tabNext.setImageResource(R.drawable.button_cust_tab_next);
 		tabNext.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (page < totalPage) {
 					page++;
 					init();
 				}
 			}
 		});
 
 		if (totalPage == 1) {
 			tabBack.setVisibility(ImageView.GONE);
 			tabNext.setVisibility(ImageView.GONE);
 			LinearLayout ll = (LinearLayout)findViewById(R.id.tabhostButtonLayout);
 			ll.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
 		}
 		if (page == 1 && totalPage > 1) {
 			tabNext.setVisibility(ImageView.VISIBLE);
 			tabBack.setVisibility(ImageView.INVISIBLE);
 		}
 		if (page == totalPage && totalPage > 1) {
 			tabBack.setVisibility(ImageView.VISIBLE);
 			tabNext.setVisibility(ImageView.INVISIBLE);
 		}
 		if (page > 1 && page < totalPage) {
 			tabBack.setVisibility(ImageView.VISIBLE);
 			tabNext.setVisibility(ImageView.VISIBLE);
 		}
 	}
 
 	private void settingLayout() {
 		SharedPreferences preferences = getSharedPreferences(Constants.DEFAUL_SHARE_DATA, 0);
 		headerTxt = preferences.getString("name", "");
 		TextView titleView = (TextView) findViewById(R.id.templateTopTitleTView);
 		titleView.setText(headerTxt);
 		
 		determineURL(headerTxt);
 		
 		Button mapButton = (Button)findViewById(R.id.mapButton);
 		mapButton.setVisibility(Button.GONE);
 		Button arButton = (Button)findViewById(R.id.arButton);
 		arButton.setVisibility(Button.GONE);
 		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.widget35);
 		linearLayout.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
 
 		Button footerSearch = (Button)findViewById(R.id.search_Button);
 		footerSearch.setOnClickListener(new MenuListener());
 		Button footerNearby = (Button)findViewById(R.id.nearbyButton);
 		footerNearby.setOnClickListener(new MenuListener());
 		Button footerShare = (Button)findViewById(R.id.shareButton);
 		footerShare.setOnClickListener(new MenuListener());
 		Button footerMap = (Button)findViewById(R.id.map_Button);
 		footerMap.setOnClickListener(new MenuListener());
 		Button homeButton = (Button)findViewById(R.id.homeButton);
 		homeButton.setOnClickListener(new MenuListener());
 		
 		HorizontalScrollView scrollView = (HorizontalScrollView)findViewById(R.id.scrollView);
 		scrollView.setVisibility(HorizontalScrollView.GONE);
 		
 		merchantList = new ArrayList<MerchantInfo2>();
 		m_adapter = new ListViewAdapter(instance, R.layout.row_list, merchantList);
 		setListAdapter(m_adapter);
 	}
 
 	private void determineURL(String headerTxt) {
 		if(headerTxt.equalsIgnoreCase(getString(R.string.dining)) || headerTxt.equalsIgnoreCase(getString(R.string.pubs))) {
 			URL = Constants.RESTAURANT_CUISINE_LISTING + catKeyword + "&pageNum=";
 		}
 		if(headerTxt.equalsIgnoreCase(getString(R.string.search))) {
 			URL = Constants.RESTAURANT_SEARCH + querySearch + "&pageNum=";
 		}
 	}
 	
 	private class ListViewAdapter extends ArrayAdapter<MerchantInfo2> {
 		private ArrayList<MerchantInfo2> items;
 
 		public ListViewAdapter(Context context, int textViewResourceId,
 			ArrayList<MerchantInfo2> items) {
 			super(context, textViewResourceId, items);
 			this.items = items;
 		}
 		
 		@SuppressWarnings("static-access")
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.row_merchant_list, null);
 
 			}
 			final MerchantInfo2 info = items.get(position);
 			if (info != null) {
 				final ImageView imView = (ImageView) v.findViewById(R.id.rowlistIconIView);
 
 				try {
 					Bitmap bitmap = Util.getBitmap(info.getImage());
 					if(bitmap == null) {
 						bitmap = new BitmapFactory().decodeResource(MainCitiGuideScreen.instance.getResources(), R.drawable.listicon);
 					}
 					bitmap = Util.resizeImage(bitmap, 60, 60);
 					bmp = Util.getRoundedCornerBitmap(bitmap);
 					imView.setImageBitmap(bmp);
 				}
 				catch(Exception ex) {
 					ex.printStackTrace();
 				}
 				
 				TextView tTView = (TextView) v.findViewById(R.id.topTView);
 				TextView mTView = (TextView) v.findViewById(R.id.middleTView);
 				TextView bTView = (TextView) v.findViewById(R.id.bottomTView);
 				if (tTView != null) {
 					tTView.setText(info.getOutletName());
 				}
 				if (mTView != null) {
 					mTView.setText(info.getAddress());
 				}
 				if (bTView != null) {
 					bTView.setText("");
 				}
 				bTView.setVisibility(TextView.GONE);
 			}
 			return v;
 		}
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		NewDescriptionScreen.merchantInfo = merchantList.get(position);
 		NewDescriptionScreen.catID = merchantList.get(position).getId();
 		Intent intent = new Intent(instance, NewDescriptionScreen.class);
 		startActivityForResult(intent, 0);
 	}
 }
