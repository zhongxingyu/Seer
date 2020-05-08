 package com.quanleimu.view;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.Helper;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import com.quanleimu.adapter.CommonItemAdapter;
 public class SearchView extends BaseView implements View.OnClickListener{
 	
 	//定义控件
 	public Button btnCancel;
 	public EditText etSearch;
 	private TextView tvClear;
 	public ListView lvSearchHistory;
 	private CommonItemAdapter adapter;
 	public ProgressDialog pd;
 	//参数
 	private String fields = "mobile,id,link,title,description,date,areaNames,categoryEnglishName,lat,lng,images_big,images_resize180,metaDat";
 	
 	/** 
 	  * 设置布局显示为目标有多大就多大 
 	  */  
 	 private LayoutParams WClayoutParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT); 
 	 /** 
 	 * 设置布局显示目标最大化 
 	 */  
 	 private LayoutParams FFlayoutParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);  
 	
 	//定义变量
 	public String searchType = "";
 	public String json = "";
 	public int startRow = 0;
 	public List<Bitmap> listBm = new ArrayList<Bitmap>();
 	public List<GoodsDetail> listSearchGoods = new ArrayList<GoodsDetail>();
 	public GoodsList goodsList = new GoodsList();
 	public String searchContent = "";
 	
 	private List<String> listRemark = new ArrayList<String>();
 	
 	protected void Init(){
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		this.addView(inflater.inflate(R.layout.search, null));
 		
 		
 		//通过ID获取控件
 		//btnSearch = (Button)findViewById(R.id.btnSearch);
 		btnCancel = (Button)findViewById(R.id.btnCancel);
 		
 		etSearch = (EditText)findViewById(R.id.etSearch);
 		etSearch.setFocusableInTouchMode(true);
 
 		
 		lvSearchHistory = (ListView) findViewById(R.id.lvSearchHistory);
 		
 		btnCancel.setOnClickListener(this);
 		
 		listRemark = QuanleimuApplication.getApplication().getListRemark();
 		
 		adapter = new CommonItemAdapter(getContext(), listRemark);
 		adapter.setHasArrow(false);
 		
 		//添加自定义布局
 		 LinearLayout layout = new LinearLayout(getContext());  
 		 layout.setOrientation(LinearLayout.HORIZONTAL);  
 		 tvClear = new TextView(getContext());
 		 tvClear.setTextSize(22);
 		 tvClear.setText("清除历史记录");
 		 tvClear.setGravity(Gravity.CENTER_VERTICAL);  
 		 
 		 layout.addView(tvClear,FFlayoutParams);  
 		 layout.setGravity(Gravity.CENTER);  
 		 
 		 LinearLayout loadingLayout = new LinearLayout(getContext());  
 		 loadingLayout.addView(layout,WClayoutParams);  
 		 loadingLayout.setGravity(Gravity.CENTER);  
 		 lvSearchHistory.addFooterView(loadingLayout);
 		 
 		 lvSearchHistory.setVisibility(View.GONE);
 		 
 		 tvClear.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				listRemark.clear();
 				QuanleimuApplication.getApplication().setListRemark(listRemark);
 				lvSearchHistory.setVisibility(View.GONE);
 				v.setVisibility(View.GONE);
 				
 				//将搜索记录保存本地
 				Helper.saveDataToLocate(getContext(), "listRemark", listRemark);
 			}
 		 });
 		 
 		 if(listRemark != null && listRemark.size() != 0)
 		 {
 			 lvSearchHistory.setVisibility(View.VISIBLE);
 			 tvClear.setVisibility(View.VISIBLE);
 			 lvSearchHistory.setAdapter(adapter);
 				lvSearchHistory.setOnItemClickListener(new OnItemClickListener() {
 
 					@Override
 					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 							long arg3) {
 						// TODO Auto-generated method stub
 						if(arg2 <=  (listRemark.size()-1))
 						{
 							 searchContent = listRemark.get(arg2);
 							 //调用搜索接口获取搜索结果，跳转搜索界面
 							 
 							 if(null != m_viewInfoListener){
 								 Bundle bundle = new Bundle();
 								 bundle.putString("backPageName", "首页");
 								 bundle.putString("searchContent", searchContent);
 								 bundle.putString("actType", "search");
 								 bundle.putString("name", "");
 								 m_viewInfoListener.onExit(SearchView.this);								 
 								 m_viewInfoListener.onNewView(new SearchGoodsView(getContext(), bundle));
 								 m_viewInfoListener.onPopView(SearchGoodsView.class.getName());
 							 }
 							 
 						}
 						else
 						{
 							listRemark.clear();
 							QuanleimuApplication.getApplication().setListRemark(listRemark);
 							lvSearchHistory.setVisibility(View.GONE);
 							tvClear.setVisibility(View.GONE);
 						}
 					}
 				});
 		 }
 	}
 	
 	public SearchView(Context context, String searchType_){
 		super(context);
 		searchType = searchType_;
 		Init();
 	}
 	public SearchView(Context context, Bundle bundle){
 		super(context);
 		
 		searchType = bundle.getString("searchType");
 		
 		Init();
 	}
 	
 	//public Bundle extracBundle(){return new Bundle();}//return a bundle that could be used to re-build the very BaseView
 	
 	//public void onDestroy(){}//called before destruction
 	//public void onPause(){}//called before put into stack
 	public void onResume(){
 		QuanleimuApplication.getApplication().setActivity_type("search");
 	}
 	
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = false;
 		return title;
 	}
 	
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = false;
 		return tab;
 	}
 	
 	private void doSearch(){
 		if(etSearch.getText().toString().equals(""))
 		{
 			Toast.makeText(getContext(), "搜索内容不能为空", 3).show();
 		}
 		else
 		{
 			searchContent = etSearch.getText().toString();
 			if(listRemark == null || listRemark.size() == 0)
 			{
 				listRemark = new ArrayList<String>();
 				listRemark.add(searchContent);
 			}
 			else if(!listRemark.contains(searchContent))
 			{
 				listRemark.add(searchContent);
 			}
 			QuanleimuApplication.getApplication().setListRemark(listRemark);
 			//将搜索记录保存本地
 			Helper.saveDataToLocate(getContext(), "listRemark", listRemark);
 			
 			 if(null != m_viewInfoListener){
 				 Bundle bundle = new Bundle();
 				 bundle.putString("backPageName", "首页");
 				 bundle.putString("searchContent", searchContent);
 				 bundle.putString("actType", "search");
 				 bundle.putString("name", "");
 				 
 				 m_viewInfoListener.onExit(SearchView.this);						 
 				 m_viewInfoListener.onNewView(new SearchGoodsView(getContext(), bundle));
 				 m_viewInfoListener.onPopView(SearchGoodsView.class.getName());
 			 }
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId())
 		{
 			case R.id.btnCancel:
 				doSearch();
 				break;
 		}
 	}
 	
 	@Override
 	public void onAttachedToWindow(){
 		super.onAttachedToWindow();
 		etSearch.postDelayed(new Runnable(){
 			public void run(){
 				
 			}
 		}, 100);
 		etSearch.postDelayed(new Runnable(){
 			@Override
 			public void run(){
 				etSearch.requestFocus();
 				InputMethodManager inputMgr = 
 						(InputMethodManager) SearchView.this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMgr.showSoftInput(etSearch, InputMethodManager.SHOW_FORCED);
//				if(!inputMgr.isActive())
//					inputMgr.toggleSoftInput(0, 0);
 			}			
 		}, 100);
 	}
 	
 	// 管理线程的Handler
 	Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case 1:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				goodsList = JsonUtil.getGoodsListFromJson(json);
 				
 				int totalCount = goodsList.getCount();
 				if (goodsList == null || goodsList.getCount() == 0) {
 					Toast.makeText(getContext(), "没有符合条件的结果，请重新输入！", 3).show();
 				} else {
 					
 					//总数存入全局
 					QuanleimuApplication.getApplication().setSearchCount(totalCount);
 					
 					listSearchGoods = goodsList.getData();
 					QuanleimuApplication.getApplication().setListSearchGoods(listSearchGoods);
 					
 					
 				}
 
 				break;
 			case 2:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				Toast.makeText(SearchView.this.getContext(), "没有符合条件的结果，请重新输入！", 3).show();
 				break;
 			}
 			
 			super.handleMessage(msg);
 		}
 	};
 
 	class GetGoodsListThread implements Runnable {
 		@Override
 		public void run() {
 			String apiName = "ad_list";
 			ArrayList<String> list = new ArrayList<String>();
 
 			list.add("fields=" + URLEncoder.encode(fields));
 			list.add("query="+"cityEnglishName:"+QuanleimuApplication.getApplication().getCityEnglishName()+" AND "+ URLEncoder.encode(searchContent));
 			list.add("start=" + startRow);
 			list.add("rows=" + 30);
 
 			String url = Communication.getApiUrl(apiName, list);
 			System.out.println("url ------ >" + url);
 			try {
 				json = Communication.getDataByUrl(url);
 
 				System.out.println("json --- >" + json);
 				if (json != null) {
 					myHandler.sendEmptyMessage(1);
 
 				} else {
 					myHandler.sendEmptyMessage(2);
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 	}
 }
