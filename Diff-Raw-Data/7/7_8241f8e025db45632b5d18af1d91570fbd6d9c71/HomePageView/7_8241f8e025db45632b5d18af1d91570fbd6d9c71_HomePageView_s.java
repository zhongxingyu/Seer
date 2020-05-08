 package com.quanleimu.view;
 
 import java.io.BufferedInputStream;
 
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.location.Location;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.ResultReceiver;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.inputmethod.BaseInputConnection;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputConnection;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ImageView.ScaleType;
 
 import com.quanleimu.activity.BaseActivity;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.FirstStepCate;
 import com.quanleimu.entity.HotList;
 import com.quanleimu.entity.SecondStepCate;
 import com.quanleimu.imageCache.ImageLoaderCallback;
 import com.quanleimu.imageCache.LazyImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.jsonutil.LocateJsonData;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.Helper;
 import com.quanleimu.util.LocationService;
 import com.quanleimu.util.Util;
 import com.quanleimu.view.BaseView;
 import com.quanleimu.widget.CircleFlowIndicator;
 import com.quanleimu.widget.TextEditInputConnected;
 import com.quanleimu.widget.ViewFlow;
 
 public class HomePageView extends BaseView implements LocationService.BXLocationServiceListener, DialogInterface.OnClickListener,  CategorySelectionView.ICateSelectionListener{
 	private ViewFlow glDetail;
 	private CircleFlowIndicator indicator;
 	LinearLayout hotlistView = null;
 	RelativeLayout rlHotList = null;
 	LinearLayout footer = null;
 	RelativeLayout feedback = null;
 	private CategorySelectionView catesView;
 	private List<HotList> listHot = new ArrayList<HotList>();
 	private String cityName;
 	private String json;
 	private HotListAdapter adapter;
 	private List<HotList> tempListHot = new ArrayList<HotList>();
 	private ProgressDialog pd = null;
 	private List<Boolean> tempUpdated = new ArrayList<Boolean>();
 	//private List<SecondStepCate> listUsualCates = new ArrayList<SecondStepCate>();
 	static private String locationAddr = "";
 	TextView editSearch = null;
 	
 	String title_str;
 	String back_str;
 
 	
 	public HomePageView(Context context){
 		super(context);
 		
 		init();
 	}
 	
 	public HomePageView(Context activity, Bundle bundle){
 		super(activity, bundle);
 
 		init();
 	}
 	
 	@Override
 	public void onResume(){
 		cityName = QuanleimuApplication.getApplication().getCityName();
 	}
 	
 	@Override
 	public void onClick(DialogInterface dialog, int id) {    
 		cityName = locationAddr;
 		QuanleimuApplication.getApplication().setCityName(cityName);
 		
 		boolean found = false;
 		for(int i=0;i<QuanleimuApplication.getApplication().getListCityDetails().size();i++)
 		{
 			if(cityName.equals(QuanleimuApplication.getApplication().getListCityDetails().get(i).getName()))
 			{
 				found = true; 
 				QuanleimuApplication.getApplication().setCityEnglishName(QuanleimuApplication.getApplication().getListCityDetails().get(i).getEnglishName());
 				Helper.saveDataToLocate(getContext(), "cityName", cityName);
 				break;
 			}
 		}
 		if(!found){
 			for(int i=0;i<QuanleimuApplication.getApplication().getListCityDetails().size();i++)
 			{
 				if(cityName.contains(QuanleimuApplication.getApplication().getListCityDetails().get(i).getName()))
 				{
 					QuanleimuApplication.getApplication().setCityEnglishName(QuanleimuApplication.getApplication().getListCityDetails().get(i).getEnglishName());
 					Helper.saveDataToLocate(getContext(), "cityName", cityName);
 					break;
 				}
 			}
 			
 		}		
 	}
 
 	
 	@Override
 	public void onLocationUpdated(final Location location){
 		if(location == null || (locationAddr != null && !locationAddr.equals(""))) return;
 		(new Thread(new Runnable(){
 			@Override
 			public void run(){
 				final String preLocation = locationAddr;
 				locationAddr = LocationService.geocodeAddr(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
 				if(null == locationAddr) return;
 				int index = locationAddr.indexOf("市");
 				locationAddr = (-1 == index ? locationAddr : locationAddr.substring(0, index));
 				QuanleimuApplication.getApplication().setGpsCityName(locationAddr);
 				LocationService.getInstance().stop();
 				if(HomePageView.this.cityName != null && !QuanleimuApplication.getApplication().cityName.equals(locationAddr)){
 					final AlertDialog.Builder builder = new AlertDialog.Builder((BaseActivity)HomePageView.this.getContext());  
 					builder.setMessage("检测到您在" + locationAddr + "，" + "需要切换吗?")
 					.setCancelable(false)  
 					.setPositiveButton("是", HomePageView.this)  
 					.setNegativeButton("否", new DialogInterface.OnClickListener() {  
 						public void onClick(DialogInterface dialog, int id) {  
 							dialog.cancel();  
 							LocationService.getInstance().stop();
 						}  
 					});
 					
 					((BaseActivity)HomePageView.this.getContext()).runOnUiThread(new Runnable(){
 						@Override
 						public void run(){
 							if(HomePageView.this.isShown()){
 								AlertDialog alert = builder.create();
 								alert.show();
 							}else{
 								locationAddr = preLocation;
 							}
 						}
 					});
 				}				
 			}
 		})).start();
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onAttachedToWindow(){
 //		if (QuanleimuApplication.listUsualCates == null) {
 //			
 //			listUsualCates = (List<SecondStepCate>)Util.loadDataFromLocate(getContext(), "listUsualCates");
 //			if (listUsualCates == null) {
 //				// 常用类目赋值
 //				listUsualCates = LocateJsonData.getUsualCatesJson();
 //				QuanleimuApplication.listUsualCates = listUsualCates;
 //				Util.saveDataToLocate(getContext(), "listUsualCates", listUsualCates);
 //			} else {
 //				QuanleimuApplication.listUsualCates = listUsualCates;
 //			}
 //		} else {
 //			listUsualCates = QuanleimuApplication.listUsualCates;
 //			Util.saveDataToLocate(getContext(), "listUsualCates", listUsualCates);
 //		}
 
 		final Location lastLocation = LocationService.getInstance().getLastKnownLocation();
 		if(lastLocation != null){
 			(new Thread(new Runnable(){
 				@Override
 				public void run(){
 					String lastAddr = LocationService.geocodeAddr(Double.toString(lastLocation.getLatitude()), Double.toString(lastLocation.getLongitude()));
 					lastAddr = lastAddr == null ? "" : lastAddr;
 					int index = lastAddr.indexOf("市");
 					lastAddr = (-1 == index ? lastAddr : lastAddr.substring(0, index));
 					if(!lastAddr.equals(locationAddr)){
 						((BaseActivity)(HomePageView.this.getContext())).runOnUiThread(new Runnable(){
 							@Override
 							public void run(){
 								LocationService.getInstance().start(getContext(), HomePageView.this);
 							}
 						});
 					}
 				}					
 			})).start();
 		}
 		else{
 			LocationService.getInstance().start(getContext(), this);
 		}	
 		
 		((TextView)findViewById(R.id.tvCityName)).setText(cityName);
 		
 		super.onAttachedToWindow();
 	}
 	
 	private void init(){
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		View v = inflater.inflate(R.layout.homepageview, null);
 		this.addView(v);
 		
 		
 		hotlistView = (LinearLayout)inflater.inflate(R.layout.hotlist, null);
 		rlHotList = (RelativeLayout)hotlistView.findViewById(R.id.rlHotList);
 		glDetail = (ViewFlow) hotlistView.findViewById(R.id.glDetail);
 		glDetail.setFadingEdgeLength(10);
 		indicator = (CircleFlowIndicator) hotlistView.findViewById(R.id.viewflowindic);
 		glDetail.setFlowIndicator(indicator);
 
 		glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				if (listHot.get(arg2).getType() == 0) {
 					Bundle bundle = new Bundle();
 					bundle.putString("actType", "homepage");
 					bundle.putString("name",
 							(listHot.get(arg2).getHotData().getTitle()));
 					bundle.putString("searchContent", (listHot.get(arg2)
 							.getHotData().getKeyword()));
 					bundle.putString("backPageName", "首页");
 					m_viewInfoListener.onNewView(new SearchGoodsView(getContext(), bundle));
 				} else if (listHot.get(arg2).getType() == 1) {
 					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(listHot
 							.get(arg2).getHotData().getWeburl()));
 					((BaseActivity)getContext()).startActivity(i);
 				}else if(listHot.get(arg2).getType() == 2){
 					Bundle bundle = new Bundle();
 					bundle.putString("actType", "homepage");
 					bundle.putString("name",
 							(listHot.get(arg2).getHotData().getTitle()));
 					bundle.putString("searchContent", (listHot.get(arg2)
 							.getHotData().getKeyword()));
 					bundle.putString("backPageName", "首页");
 					m_viewInfoListener.onNewView(new GetGoodsView(getContext(), bundle, listHot.get(arg2).getHotData().getKeyword()));
 				}
 			}
 		});
 
 		if (QuanleimuApplication.getApplication().getCityName() == null || QuanleimuApplication.getApplication().getCityName().equals("")) {
 			cityName = "上海";
 			QuanleimuApplication.getApplication().setCityName(cityName);
 			QuanleimuApplication.getApplication().setCityEnglishName("shanghai");
 		} else {
 			cityName = QuanleimuApplication.getApplication().getCityName();
 		}
 		
 		listHot = QuanleimuApplication.listHot;
 		if(listHot == null){	
 			//try to load from last-saved hot-list file
 			boolean lastSavedValid = true;
 			try {
 				FileInputStream jsonFile = getContext().openFileInput("hotlist.json");
 				BufferedInputStream bufferedStream = new BufferedInputStream(jsonFile);
 				int bytesJson = bufferedStream.available();
 				byte[] json = new byte[bytesJson];
 				int readBytes = bufferedStream.read(json, 0, bytesJson);
 				
 				String jsonRaw = new String(json, 0, readBytes);
 				String jsonDecoded = Communication.decodeUnicode(jsonRaw);
 				listHot = JsonUtil.parseCityHotFromJson(jsonDecoded);
 				
 				for(int i = 0; i < listHot.size(); ++i){
 					if(!QuanleimuApplication.lazyImageLoader.checkWithImmediateIO(listHot.get(i).imgUrl)){
 						lastSavedValid = false;
 					}
 				}
 				
 				if(listHot == null || listHot.size() == 0)	lastSavedValid = false;
 				
 			} catch (FileNotFoundException e) {
 				lastSavedValid = false;
 			}catch(IOException e){
 				lastSavedValid = false;
 			}
 			
 			//if last-saved is not ready, parse from initial hot-list in asset 
 			if(!lastSavedValid){
 				try {
 					InputStream jsonFile = getContext().getAssets().open("hotlist.json");
 					BufferedInputStream bufferedStream = new BufferedInputStream(jsonFile);
 					int bytesJson = bufferedStream.available();
 					byte[] json = new byte[bytesJson];
 					int readBytes = bufferedStream.read(json, 0, bytesJson);
 					
 					String jsonDecoded = Communication.decodeUnicode(new String(json, 0, readBytes));
 					listHot = JsonUtil.parseCityHotFromJson(jsonDecoded);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 
 			if(listHot == null)	listHot = new ArrayList<HotList>();
 			
 			new Thread(new HotListThread()).start(); 
 		}
 		
 		adapter = new HotListAdapter(getContext(), 
 				listHot, 
 				tempListHot, 
 				QuanleimuApplication.lazyImageLoader);
 		glDetail.setAdapter(adapter);		
 		
 		
 		footer = (LinearLayout)inflater.inflate(R.layout.feedback_homepage, null);
 		footer.findViewById(R.id.feedback).setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				HomePageView.this.m_viewInfoListener.onNewView(new OpinionBackView(getContext(), new Bundle()));
 			}
 		});
 		feedback = (RelativeLayout)footer.findViewById(R.id.llFeedback);
 		
 		catesView = (CategorySelectionView)findViewById(R.id.cateSelection);
 		catesView.setHeaderFooterView(hotlistView, footer);
 		catesView.setSelectionListener(this);
 		
 		title_str = "选择浏览类目";
 		back_str = "";
 		
 		LinearLayout changeCity = (LinearLayout) findViewById(R.id.llChangeCity);
 		changeCity.setOnClickListener(new View.OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(null != m_viewInfoListener){
 					m_viewInfoListener.onNewView(new CityChangeView(getContext(), "首页"));
 				}
 			}
 			
 		});
 		
 		editSearch = (TextView)findViewById(R.id.etSearch);
 		
 		editSearch.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				Bundle bundle = new Bundle();
 				bundle.putString("backPageName", "首页");
 				bundle.putString("searchContent", HomePageView.this.editSearch.getText().toString());
 				bundle.putString("actType", "search");
 
 				m_viewInfoListener.onNewView(new SearchView(getContext(), bundle));			
 			}
 		});
 		
 //		setEnabled(true);
 //		requestFocus();
 //		InputMethodManager input = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 //		ResultReceiver receiver = new ResultReceiver(new Handler() {
 //			 
 //			public void handleMessage(Message msg) {
 //			 
 //			}
 //			 
 //			});
 //		input.showSoftInput(v, 0, receiver);	
 	}
 	
 	class HotListThread implements Runnable {
 
 		@Override
 		public void run() {
 			String apiName = "city_hotlist";
 			ArrayList<String> list = new ArrayList<String>();
 			list.add("v=_v2");
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url);
 				if (json != null) {
 					myHandler.sendEmptyMessage(1);
 				} else {
 					myHandler.sendEmptyMessage(2);
 				}
 			} catch (UnsupportedEncodingException e) {
 				myHandler.sendEmptyMessage(4);
 			} catch (IOException e) {
 				myHandler.sendEmptyMessage(4);
 			}
 
 		}
 	}
 	
 	Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case 1:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				tempListHot = JsonUtil.parseCityHotFromJson(Communication
 						.decodeUnicode(json));
 				if(tempListHot != null){
 					for(int i = 0; i < tempListHot.size(); ++i){
 						tempUpdated.add(false);
 					}
 	
 					if(adapter != null)	adapter.SetLoadingHotList(tempListHot);
 					QuanleimuApplication.listHot = tempListHot;
 					
 					//save to context data
 					getContext().deleteFile("hotlist.json");
 					try {
 						BufferedOutputStream outFileStream = new BufferedOutputStream(getContext().openFileOutput("hotlist.json", Context.MODE_PRIVATE));
 						outFileStream.write(json.getBytes(), 0, json.length());
 						outFileStream.flush();
 						outFileStream.close();
 					} catch (FileNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}catch(IOException e){
 						e.printStackTrace();
 					}
 				}
 				
 
 				break;
 			case 2:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				break;
 			case 4:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				 Toast.makeText(getContext(), "网络连接失败，请检查设置！", 3).show();
 				//tvInfo.setVisibility(View.VISIBLE);
 				break;
 			}
 			super.handleMessage(msg);
 		}
 	};
 
 	
 	class HotListAdapter extends BaseAdapter {
 		Context context;
 		List<HotList> curList = new ArrayList<HotList>();
 		List<HotList> loadingList = new ArrayList<HotList>();
 		
 		private int nNotifyInstance = 0;
 		
 		final LazyImageLoader imgLoader;
 
 		private class AdapterNotifyChange extends AsyncTask<Boolean, Boolean, Boolean> { 
 			private HotListAdapter adapter = null;
 			
 			public AdapterNotifyChange(HotListAdapter adapter_){
 				this.adapter = adapter_;
 				nNotifyInstance++;
 				//Log.d("HomePage async task count:", " " + nNotifyInstance);
 			}
 			
 			protected Boolean doInBackground(Boolean... bs) {   
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				return true;
 			}
 			
 			protected void onProgressUpdate(Boolean... bs) {
 				}    
 			
 			protected void onPostExecute(Boolean bool) {  
 				this.adapter.notifyDataSetChanged();
 //				indicator.setPadding(0, glDetail.getHeight(), 0, 0);
 				nNotifyInstance--;
 				//Log.d("HomePage async task count:", " " + nNotifyInstance);
 			}
 		};
 		
 		public HotListAdapter(Context context, 
 				List<HotList> listHot,
 				List<HotList> loadingListHot,
 				LazyImageLoader imgLoader_) {
 			this.context = context;
 			this.curList = listHot;
 			this.loadingList = loadingListHot;
 			this.imgLoader = imgLoader_;
 		}
 
 		@Override
 		public int getCount() {
 			return curList.size() > 0 ?	curList.size() : loadingList.size() > 0 ? 1 : 0;
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 		
 		public void SetLoadingHotList(List<HotList> loadingListHot_){
 			this.loadingList = loadingListHot_;
 			(new AdapterNotifyChange(this)).execute(true);
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			
 			LayoutInflater inflater = LayoutInflater.from(context);
 			
 			View v = null;
 			if (convertView != null) {
 				v = convertView;
 			} else {
 				v = inflater.inflate(R.layout.hotdetail, null);
 			}
 			
 			ImageView iv = null;
 			
 			if(position < curList.size()){
 				iv = (ImageView) v.findViewById(R.id.ivHotDetail);
				iv.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT));
				iv.setPadding(0, 0, 0, 0);
				// 设置图片填充布局
				iv.setScaleType(ScaleType.FIT_XY);
 				iv.setTag(curList.get(position).getImgUrl());
 			}
 			
 			if(imgLoader != null){
 				//check whether real image has been valid
 
 				boolean needNotify = false;
 				Bitmap bitmapReal =  null;
 				final HotListAdapter thisAdapter = this;
 				
 				if(position < loadingList.size()){
 					final int position_f = position;					
 					final ImageView iv_f = iv;
 					
 					bitmapReal = imgLoader.getWithImmediateIO(loadingList.get(position).getImgUrl(), new ImageLoaderCallback(){					
 						
 						public void refresh(final String url, final Bitmap bitmap){										
 							
 							if(position_f < curList.size()){
 								curList.set(position_f, loadingList.get(position_f));
 								tempUpdated.set(position_f, true);
 							}
 							else if(position_f == curList.size()){
 								curList.add(loadingList.get(position_f));
 								tempUpdated.set(position_f, true);
 							}
 							if(iv_f != null)	iv_f.setImageBitmap(bitmap);
 	
 							(new AdapterNotifyChange(thisAdapter)).execute(true);
 						}					
 					});
 				}
 				
 				if(bitmapReal != null){
 					
 					if(position < curList.size() && !tempUpdated.get(position)){
 						curList.set(position, loadingList.get(position));
 						tempUpdated.set(position, true);
 						needNotify = true;		
 					}
 					else if(position == curList.size()){
 						curList.add(loadingList.get(position));
 						tempUpdated.set(position, true);
 						needNotify = true;
 						}
 					if(iv != null)	iv.setImageBitmap(bitmapReal);
 						
 					
 					//if this is the last seen item, try to load next
 					int position_cur = position + 1;
 					while(position_cur == curList.size() && position_cur < loadingList.size()){										
 						
 						final int position_next = position_cur;
 						Bitmap bitmapNext = imgLoader.getWithImmediateIO(loadingList.get(position_next).getImgUrl(), new ImageLoaderCallback(){
 										
 							public void refresh(final String url, final Bitmap bitmap){	
 															
 								if(position_next == curList.size()){
 									curList.add(loadingList.get(position_next));
 									tempUpdated.set(position_next, true);
 								}								
 								notifyChange();
 							}
 						});
 						
 						if(bitmapNext != null){
 							curList.add(loadingList.get(position_next));
 							tempUpdated.set(position_next, true);
 							needNotify = true;
 							position_cur++;
 						}
 					}
 				}else if(position < curList.size()){				
 				
 					final Bitmap bitmap =  imgLoader.getWithImmediateIO(curList.get(position).getImgUrl(), new ImageLoaderCallback(){
 					
 						public void refresh(final String url, final Bitmap bitmap){	
 								Log.d( "HotList original image loader 1", "original hotlist picture missing!!");
 					    }					
 					});
 					
 					if(bitmap != null){
 						if(iv != null)	iv.setImageBitmap(bitmap);		
 					}else{
 						Log.d( "HotList original image loader 2", "original hotlist picture missing!!");
 					}
 				}
 				
 				if(needNotify)	notifyChange();
 			}
 			
 			return v;			
 		}
 		
 		private void notifyChange(){
 			if(HotListAdapter.this.nNotifyInstance < 1){
 				(new AdapterNotifyChange(this)).execute(true);
 			}
 		}
 	}
 	
 	@Override
 	public void onDestroy(){
 		LocationService.getInstance().stop();
 	}
 	
 	@Override
 	public void onPause(){
 		LocationService.getInstance().stop();
 	}
 	
 	protected void SwitchCateLevel(boolean toSubCate){
 		int visibility = toSubCate ? View.GONE : View.VISIBLE;
 		
 		hotlistView.setVisibility(visibility);
 		rlHotList.setVisibility(visibility);
 		footer.setVisibility(visibility);
 		feedback.setVisibility(visibility);
 		
 		findViewById(R.id.lvLogoAndChangeCity).setVisibility(visibility);
 		findViewById(R.id.lvSearch).setVisibility(visibility);		
 	}
 	
 	@Override
 	public boolean onBack(){
 		if(catesView == null || !catesView.OnBack()){
 			return false;
 		}else
 		{
 			if(null != m_viewInfoListener){
 				this.title_str = back_str;
 				back_str = "";
 				
 				m_viewInfoListener.onTitleChanged(getTitleDef());
 			}		
 		}
 		
 		SwitchCateLevel(catesView.getLevel() == CategorySelectionView.ECATE_LEVEL.ECATE_LEVEL_SUB );
 		
 		return true;
 	}//called when back button/key pressed
 	
 	@Override
 	public boolean onLeftActionPressed(){
 		return onBack();
 	}//called when left button on title bar pressed, return true if handled already, false otherwise
 	
 	@Override
 	public boolean onRightActionPressed(){
 		if(null != m_viewInfoListener){
 			m_viewInfoListener.onNewView(new SearchView(getContext(), "homePage"));
 		}
 		return true;
 	}//called when right button on title bar pressed, return true if handled already, false otherwise
 	
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 
 		title.m_title = title_str;
 		title.m_leftActionHint = back_str;
 		
 		title.m_visible = (catesView.getLevel() != CategorySelectionView.ECATE_LEVEL.ECATE_LEVEL_MAIN);
 		
 		return title;
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = true;
 		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MAINPAGE;
 		return tab;
 	}
 
 	@Override
 	public void OnMainCategorySelected(FirstStepCate selectedMainCate){
 		if(null != m_viewInfoListener){
 			back_str = title_str;
 			title_str = selectedMainCate.getName();			
 			m_viewInfoListener.onTitleChanged(getTitleDef());
 		}
 		
 		SwitchCateLevel(true);
 	}
 	
 	@Override
 	public void OnSubCategorySelected(SecondStepCate selectedSubCate){
 
 		Bundle bundle = new Bundle();
 		bundle.putString("name", selectedSubCate.getName());
 		bundle.putString("categoryEnglishName",	selectedSubCate.getEnglishName());
 		bundle.putString("siftresult", "");
 		bundle.putString("backPageName", "返回");
 		if(null != m_viewInfoListener){
 			m_viewInfoListener.onNewView(new GetGoodsView(getContext(), bundle, selectedSubCate.getEnglishName()));			
 		}
 		
 		SwitchCateLevel(true);
 	}
 };
