 package com.quanleimu.view.fragment;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Currency;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Message;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.HeaderViewListAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListAdapter;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.GoodsListAdapter;
 import com.quanleimu.entity.BXLocation;
 import com.quanleimu.entity.Filterss;
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.entity.ImageList;
 import com.quanleimu.entity.PostMu;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ErrorHandler;
 import com.quanleimu.util.GoodsListLoader;
 import com.quanleimu.util.ParameterHolder;
 import com.quanleimu.util.Util;
 import com.quanleimu.view.FilterUtil;
 import com.quanleimu.view.FilterUtil.CustomizeItem;
 import com.quanleimu.view.FilterUtil.FilterSelectListener;
 import com.quanleimu.view.fragment.MultiLevelSelectionFragment.MultiLevelItem;
 import com.quanleimu.widget.PullToRefreshListView;
 import com.quanleimu.widget.PullToRefreshListView.E_GETMORE;
 
 public class GetGoodFragment extends BaseFragment implements View.OnClickListener, OnScrollListener, PullToRefreshListView.OnRefreshListener, PullToRefreshListView.OnGetmoreListener {
 
 //	public static final int SEARCH_NEARBY = 1;
 //	public static final int SEARCH_RECENT = 0;
 	
 	
 	public static final int MSG_UPDATE_FILTER = 1000;
 	
 	
 	private static final int REQ_SIFT = 1;
 	
 	private PullToRefreshListView lvGoodsList;
 	private ProgressBar progressBar;
 //	private int searchType = 0;
 	
 	private String categoryEnglishName = "";
 	private String searchContent = "";
 //	private String siftResult = "";
 	private PostParamsHolder filterParamHolder;
 	
 //	private List<String> basicParams = null;
 	
 	private GoodsListLoader goodsListLoader = null;
 	
 	private boolean mRefreshUsingLocal = false;
 	private BXLocation curLocation = null;
 	
 	private List<Filterss> listFilterss;
 	
 	
 	@Override
 	protected void initTitle(TitleDef title) {
 		title.m_visible = true;
 		title.m_leftActionHint = "返回";//this.getArguments().getString("backPageName");
 		title.m_title = getArguments().getString("categoryName");//getArguments().getString("name");
 		title.m_rightActionHint = "发布";
 		title.m_rightActionBg = R.drawable.bg_post_selector;
 	}
 	
 	public void initTab(TabDef tab){
 		tab.m_visible = false;
 	}
 	
 	public GoodsListAdapter findGoodListAdapter()
 	{
 		ListAdapter adapter = lvGoodsList.getAdapter();
 		if (adapter instanceof HeaderViewListAdapter)
 		{
 			HeaderViewListAdapter realAdapter = (HeaderViewListAdapter) adapter;
 			return (GoodsListAdapter) realAdapter.getWrappedAdapter();
 		}
 		else
 		{
 			return (GoodsListAdapter) adapter;
 		}
 	}
 	
 	public void handleRightAction(){
 		String categoryName = getArguments().getString("categoryName");
 		categoryName = categoryEnglishName + "," + categoryName;
 		Bundle bundle = createArguments(null, null);
 		bundle.putSerializable("cateNames", categoryName);
 		pushFragment(new PostGoodsFragment(), bundle);
 	}
 	
 	
 	
 	@Override
 	protected void onFragmentBackWithData(int requestCode, Object result) {
 		if (requestCode == REQ_SIFT && result instanceof PostParamsHolder)
 		{
 			this.filterParamHolder.clear(); //Replace old params with new.
 			this.filterParamHolder.merge((PostParamsHolder) result);
 //			this.updateSearchParams();
 //			searchType = PostParamsHolder.INVALID_VALUE.equals(filterParamHolder.getData("地区_s")) ? SEARCH_NEARBY : SEARCH_RECENT;
 			this.showFilterBar(getView(), listFilterss);
 			this.resetSearch(this.curLocation);
 			
 			lvGoodsList.fireRefresh();
 		}
 	}
 
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		this.categoryEnglishName = getArguments().getString("categoryEnglishName");
 		this.searchContent = getArguments().getString("searchContent");
 //		if (getArguments().containsKey("siftresult")) //FIXME:CHONG siftresult is removed by chong.
 //		{
 //			this.siftResult = getArguments().getString("siftresult");
 //		}
 		filterParamHolder = (PostParamsHolder)getArguments().getSerializable("filterResult");
 		if (filterParamHolder == null)
 		{
 			filterParamHolder = new PostParamsHolder();
 			getArguments().putSerializable("filterResult", filterParamHolder);
 			filterParamHolder.put("cityEnglishName", QuanleimuApplication.getApplication().getCityEnglishName(), QuanleimuApplication.getApplication().getCityEnglishName());
 			filterParamHolder.put("categoryEnglishName", categoryEnglishName, categoryEnglishName);
 			filterParamHolder.put("status", "" + 0, "" + 0);
 			if (searchContent != null)
 			{
 				filterParamHolder.put("", searchContent, searchContent);
 			}
 		}
 		
 //		updateSearchParams();
 		
 //		if (siftResult != null && !siftResult.equals("")) {
 //		} else {
 //			basicParams.add("query="
 //					+ "cityEnglishName:"+QuanleimuApplication.getApplication().getCityEnglishName()+" AND categoryEnglishName:"
 //					+ categoryEnglishName + " AND status:0");
 //		}
 		
 		
 		goodsListLoader = new GoodsListLoader(getSearchParams(), handler, null, new GoodsList());
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		goodsListLoader.setHandler(handler);
 	}
 	
 	@Override
 	public void onStackTop(boolean isBack) {
 		super.onStackTop(isBack);
 		if (goodsListLoader.getGoodsList().getData() != null && goodsListLoader.getGoodsList().getData().size() > 0)
 		{
 			GoodsListAdapter adapter = new GoodsListAdapter(getActivity(), goodsListLoader.getGoodsList().getData());
 			updateData(adapter, goodsListLoader.getGoodsList().getData());
 			lvGoodsList.setAdapter(adapter);
 			lvGoodsList.setSelectionFromHeader(goodsListLoader.getSelection());
 		}
 		else
 		{
 			showSimpleProgress();
 			goodsListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL);
 		}
 		
 		
 		if (listFilterss == null)
 		{
 			PostMu postMu = (PostMu) Util
 					.loadDataFromLocate(
 							getActivity(),
 							"saveFilterss"
 									+ categoryEnglishName
 									+ QuanleimuApplication.getApplication().cityEnglishName);
 			if (postMu != null && !"".equals(postMu.getJson()))
 			{
 				listFilterss = JsonUtil.getFilters(postMu.getJson()).getFilterssList();
 			}
 		}
 		//Update filter bar.
 		
 		if (listFilterss == null)
 		{
 			new Thread(new GetGoodsListThread(true)).start();
 		}
 		else
 		{
 			showFilterBar(getView().findViewById(R.id.filter_bar_root), listFilterss);
 		}
 		
 	}
 	
 	class GetGoodsListThread implements Runnable {
 		private boolean isUpdate;
 		public GetGoodsListThread(boolean isUpdate){
 			this.isUpdate = isUpdate;
 		}
 		@Override
 		public void run() {
 			String apiName = "category_meta_filter";
 			ArrayList<String> list = new ArrayList<String>();
 
 			list.add("categoryEnglishName=" + categoryEnglishName);
 			list.add("cityEnglishName=" + QuanleimuApplication.getApplication().cityEnglishName);
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				String json = Communication.getDataByUrl(url, false);
 				if (json != null) {
 					PostMu postMu = new PostMu();
 					postMu.setJson(json);
 					postMu.setTime(System.currentTimeMillis());
 					Util.saveDataToLocate(getAppContext(), "saveFilterss"+categoryEnglishName+QuanleimuApplication.getApplication().cityEnglishName, postMu);
 					
 					listFilterss = JsonUtil.getFilters(json).getFilterssList();
 					if(isUpdate){
 						sendMessage(MSG_UPDATE_FILTER, json);
 					}
 				} 
 //				else {
 //					sendMessage(2, null);
 //				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (Communication.BXHttpException e){
 				
 			}
 			
 //			hideProgress();
 		}
 	}
 	
 	private ArrayList<String> getSearchParams()
 	{
 		ArrayList basicParams = new ArrayList<String>();
 		basicParams.add("query=" + filterParamHolder.toUrlString());
 		if (isSerchNearBy() && this.curLocation != null)
 		{
 			basicParams.add("lat="+curLocation.fLat);
 			basicParams.add("lng="+curLocation.fLon);	
 		}
 		
 		return basicParams;
 	}
 	
 	private boolean isSerchNearBy()
 	{
 		return PostParamsHolder.INVALID_VALUE.equals(this.filterParamHolder.getData("地区_s"));
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		logCreateView(savedInstanceState);
 		
 		View v = inflater.inflate(R.layout.goodslist, null);
 		
 //		View titleControl = inflater.inflate(R.layout.recent_or_nearby, null);
 //		titleControl.findViewById(R.id.btnNearby).setOnClickListener(this);
 //		titleControl.findViewById(R.id.btnRecent).setOnClickListener(this);
 //		this.getTitleDef().m_titleControls = titleControl;
 		
 		lvGoodsList = (PullToRefreshListView) v.findViewById(R.id.lvGoodsList);
 		lvGoodsList.setOnRefreshListener(this);
 		lvGoodsList.setOnGetMoreListener(this);
 
         LinearLayout layout = new LinearLayout(this.getActivity());  
         layout.setOrientation(LinearLayout.HORIZONTAL);  
         progressBar = new ProgressBar(this.getActivity(), null, android.R.attr.progressBarStyleSmall);
          //进度条显示位置  
         progressBar.setVisibility(View.GONE);
         
         LayoutParams WClayoutParams =
         		new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
         layout.addView(progressBar, WClayoutParams);  
         layout.setGravity(Gravity.CENTER);  
 		
 		lvGoodsList.setOnScrollListener(this);
 
 	
 		curLocation = QuanleimuApplication.getApplication().getCurrentPosition(true);
 //		List<String> addParams = new ArrayList<String>(basicParams);
 		if(curLocation == null && isSerchNearBy()/* || searchType == 0*/){
 //			((Button)titleControl.findViewById(R.id.btnNearby)).setBackgroundResource(R.drawable.bg_nav_seg_left_normal);
 //			((Button)titleControl.findViewById(R.id.btnRecent)).setBackgroundResource(R.drawable.bg_nav_seg_right_pressed);
 //			((TextView)v.findViewById(R.id.tvSpaceOrTimeNumber)).setText("0");
 //			((TextView)v.findViewById(R.id.tvSpaceOrTimeUnit)).setText("小时");
 //			this.searchType = SEARCH_RECENT;
 			filterParamHolder.remove("地区_s");
 		}
 //		else{
 ////			((TextView)v.findViewById(R.id.tvSpaceOrTimeNumber)).setText("0");
 ////			((TextView)v.findViewById(R.id.tvSpaceOrTimeUnit)).setText("公里");
 //			addParams.add("lat="+curLocation.fLat);
 //			addParams.add("lng="+curLocation.fLon);			
 //		}
 		
 		goodsListLoader.setParams(getSearchParams()); //= new GoodsListLoader(addParams, myHandler, null, new GoodsList());
 		if(curLocation != null && /*searchType != SEARCH_RECENT*/ isSerchNearBy()){
 			goodsListLoader.setNearby(true);
 			goodsListLoader.setRuntime(false);
 		}
 		
 		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int pos, long arg3) {
 				
 				int index = (int) arg3;//(int) (arg3 - lvGoodsList.getHeaderViewsCount());
 				if(index < 0 || index > goodsListLoader.getGoodsList().getData().size() - 1)
 					return;
 
 					Bundle bundle = createArguments(null, null);
 					bundle.putSerializable("loader", goodsListLoader);
 					bundle.putInt("index", index);
 					pushFragment(new GoodDetailFragment(), bundle);
 //				}				
 			}
 		});
 		
 //		((TextView)v.findViewById(R.id.tvSubCateName)).setText(getArguments().getString("name"));
 		
 		
 		String categoryName = getArguments().getString("categoryName");
 		if(categoryName == null || categoryName.equals("")){
 			if(categoryEnglishName != null){
 				categoryName = QuanleimuApplication.getApplication().queryCategoryDisplayName(categoryEnglishName);
 				if(categoryName != null) getArguments().putString("categoryName", categoryName);
 			}
 		}
 		
 		return v;
 	
 	}
 
 
 
 	@Override
 	public void onClick(View v) {
 		GoodsListAdapter adapter = this.findGoodListAdapter();
 		switch(v.getId()){
 
 		}
 	}
 	
 	@Override
 	public void onDestroy(){		
 		this.lvGoodsList = null;
 //		this.adapter = null;
 		GoodsList goodData = this.goodsListLoader.getGoodsList();
 		this.goodsListLoader.reset();
 		this.goodsListLoader = null;
 		
 		if(goodData != null){
 			 List<GoodsDetail> list = goodData.getData();
 			 if(list != null){
 				 for(int i = 0; i < list.size(); ++ i){
 					 GoodsDetail gd = list.get(i);
 					 if(gd != null){
 						 ImageList il = gd.getImageList();
 						 if(il != null){
 							 if(il.getResize180() != null){
 								 String b = il.getResize180();
 								 if (b.contains(",")) {
 									String[] c = b.split(",");
 									if (c[0] != null && !c[0].equals("")) {
 //										Log.d("ondestroy of getgoodsview", "hahahaha recycle in getgoodsview ondestroy");
 										QuanleimuApplication.getImageLoader().prepareRecycle(c[0]);
 //										Log.d("ondestroy of getgoodsview", "hahahaha end recycle in getgoodsview ondestroy");
 									}
 								 }
 							 }
 						 }
 					 }
 				 }
 			 }
 		}
 
 		QuanleimuApplication.getImageLoader().startRecycle();
 		System.gc();
 		super.onDestroy();
 	}
 	
 	
 	@Override
 	public void onPause(){
 		this.lvGoodsList.setOnScrollListener(null);
 		super.onPause();
 
 		for(int i = 0; i < lvGoodsList.getChildCount(); ++i){
 			ImageView imageView = (ImageView)lvGoodsList.getChildAt(i).findViewById(R.id.ivInfo);
 			
 			if(	null != imageView){	
 				if(null != imageView.getTag() && imageView.getTag().toString().length() > 0
 				/*&& null != imageView.getDrawable()
 				&& imageView.getDrawable() instanceof AnimationDrawable*/){
 					SimpleImageLoader.Cancel(imageView.getTag().toString(), imageView);
 				}
 			}
 		}		
 	}
 	
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 	}
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		if(scrollState == SCROLL_STATE_IDLE)
 		{
 			ArrayList<String> urls = new ArrayList<String>();
 			for(int index = 0; index < view.getChildCount(); ++index){
 				View curView = view.getChildAt(+index);
 				if(null != curView){
 					View curIv = curView.findViewById(R.id.ivInfo);
 					
 					if(null != curIv && null != curIv.getTag())	urls.add(curIv.getTag().toString());
 				}			
 			}
 			
 			SimpleImageLoader.AdjustPriority(urls);			
 		}		
 	}
 
 	@Override
 	public void onGetMore() {
 		goodsListLoader.startFetching(false, ((GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE == goodsListLoader.getDataStatus()) ? 
 												Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE :
 												Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL));
 	}
 
 	@Override
 	public void onRefresh() {
 		goodsListLoader.startFetching(true, mRefreshUsingLocal ? Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL : Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE);	
 		mRefreshUsingLocal = false;
 	}
 
 	private void updateData(GoodsListAdapter adapter, List<GoodsDetail> data)
 	{
 		adapter.setList(data, isSerchNearBy() ? FilterUtil.createDistanceGroup(data, this.curLocation, new int[] {500, 1500}) : 
 			FilterUtil.createFilterGroup(listFilterss, filterParamHolder, data) );
 	}
 	
 	@Override
 	protected void handleMessage(Message msg, Activity activity, View rootView) {
 
 		switch (msg.what) {
 		case GoodsListLoader.MSG_FIRST_FAIL:
 			if(goodsListLoader == null) break;
 			if(GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE == goodsListLoader.getRequestDataStatus())
 				goodsListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE);
 			else{
 				Toast.makeText(getActivity(), "没有符合条件的结果，请重新输入！", Toast.LENGTH_LONG).show();
 				hideProgress();
 			}
 			break;
 		case GoodsListLoader.MSG_FINISH_GET_FIRST:
 			if(goodsListLoader == null) break;
 			GoodsList goodsList = JsonUtil.getGoodsListFromJson(goodsListLoader.getLastJson());
 			goodsListLoader.setGoodsList(goodsList);
 			if (goodsList == null || goodsList.getData() == null || goodsList.getData().size() == 0) {
 				Message msg1 = Message.obtain();
 				msg1.what = ErrorHandler.ERROR_COMMON_FAILURE;
 				Bundle bundle = new Bundle();
 				bundle.putString("popup_message", "没有符合的结果，请更改条件并重试！");
 				msg1.setData(bundle);
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg1);

				GoodsListAdapter adapter = findGoodListAdapter();
				adapter.setList(new ArrayList<GoodsDetail>());
				adapter.updateGroups(null);
				adapter.notifyDataSetChanged();
 			} else {
 				//QuanleimuApplication.getApplication().setListGoods(goodsListLoader.getGoodsList().getData());
 				GoodsListAdapter adapter = new GoodsListAdapter(getActivity(), goodsListLoader.getGoodsList().getData());
 				updateData(adapter, goodsListLoader.getGoodsList().getData());
 				lvGoodsList.setAdapter(adapter);
 				goodsListLoader.setHasMore(true);
 			}
 			
 			lvGoodsList.onRefreshComplete();
 			
 			//if currently using offline data, start fetching online data
 			if(GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE == goodsListLoader.getDataStatus())
 				lvGoodsList.fireRefresh();
 				//goodsListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE);

 			hideProgress();
 			break;
 		case GoodsListLoader.MSG_NO_MORE:
 			if(goodsListLoader == null) break;
 			progressBar.setVisibility(View.GONE);
 			
 //			Message msg1 = Message.obtain();
 //			msg1.what = ErrorHandler.ERROR_COMMON_FAILURE;
 //			Bundle bundle = new Bundle();
 //			bundle.putString("popup_message", "数据下载失败，请重试！");
 //			msg1.setData(bundle);
 //			QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg1);
 			
 			lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_NO_MORE);
 			goodsListLoader.setHasMore(false);
 			
 			hideProgress();			
 			break;
 		case GoodsListLoader.MSG_FINISH_GET_MORE:
 			if(goodsListLoader == null) break;
 			progressBar.setVisibility(View.GONE);
 			
 			GoodsList moreGoodsList = JsonUtil.getGoodsListFromJson(goodsListLoader.getLastJson());
 			if (moreGoodsList == null || moreGoodsList.getData() == null || moreGoodsList.getData().size() == 0) {
 //				Message msg2 = Message.obtain();
 //				msg2.what = ErrorHandler.ERROR_COMMON_WARNING;
 //				Bundle bundle1 = new Bundle();
 //				bundle1.putString("popup_message", "没有更多啦！");
 //				msg2.setData(bundle1);
 //				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 				
 				lvGoodsList.onGetMoreCompleted(E_GETMORE.E_GETMORE_NO_MORE);
 				goodsListLoader.setHasMore(false);
 			} else {
 				List<GoodsDetail> listCommonGoods =  moreGoodsList.getData();
 				for(int i=0;i<listCommonGoods.size();i++)
 				{
 					goodsListLoader.getGoodsList().getData().add(listCommonGoods.get(i));
 				}
 				//QuanleimuApplication.getApplication().setListGoods(goodsListLoader.getGoodsList().getData());
 				
 				GoodsListAdapter adapter = findGoodListAdapter();
 				updateData(adapter, goodsListLoader.getGoodsList().getData());
 				adapter.notifyDataSetChanged();		
 				
 				lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_OK);
 				goodsListLoader.setHasMore(true);
 			}
 			
 			hideProgress();			
 			break;
 		case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
 			if(goodsListLoader == null) break;
 			progressBar.setVisibility(View.GONE);
 
 			Message msg2 = Message.obtain();
 			msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 			QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			
 			lvGoodsList.onFail();
 			
 			hideProgress();			
 			break;
 		case MSG_UPDATE_FILTER:
 			showFilterBar(rootView, listFilterss);
 			break;
 		}
 		
 	}
 	
 	public void showFilterBar(View root, final List<Filterss> fss)
 	{
 		View[] actionViews = findAllFilterView();
 		
 		if (actionViews == null)
 		{
 			return;
 		}
 		
 		
 		View.OnClickListener listener = new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (v.getId() == R.id.filter_item_more)
 				{
 					if(categoryEnglishName == null || categoryEnglishName.equals("")){
 						pushAndFinish(new SearchFragment(), createArguments(null, null));
 					}else{
 						Bundle args = createArguments(null, getArguments().getString(ARG_COMMON_BACK_HINT));
 						args.putAll(getArguments());
 				//		bundle.putString("backPageName", bundle.getString("backPageName"));
 						args.putInt(ARG_COMMON_REQ_CODE, REQ_SIFT);
 						args.putString("searchType", "goodslist");
 						args.putString("categoryEnglishName", categoryEnglishName);
 				
 						pushFragment(new FilterFragment(), args);
 					}
 				}
 				else
 				{
 					final Filterss fss = (Filterss) v.getTag();
 					final boolean isLocation = fss.getName().equals("地区_s");
 					
 					CustomizeItem cItem = new CustomizeItem();
 					cItem.id = PostParamsHolder.INVALID_VALUE; //FIXME:  this is special for current location.
 					cItem.txt = "附近500米";
 					
 					CustomizeItem[] cs = isLocation && curLocation != null ? new CustomizeItem[] {cItem} : null; 
 					
 					FilterUtil.startSelect(getActivity(), cs, fss, new FilterSelectListener() {
 						
 						@Override
 						public void onItemSelect(MultiLevelItem item) {
 							FilterUtil.updateFilter(filterParamHolder, item, fss.getName());
 							
 							FilterUtil.updateFilterLabel(findAllFilterView(), item.txt, fss);
 //							updateSearchParams();
 							
 							resetSearch(curLocation);
 							lvGoodsList.fireRefresh();
 						}
 						
 						@Override
 						public void onCancel() {
 							//
 						}
 					});
 				}
 			}
 		};
 		
 		for (View v : actionViews)
 		{
 			v.setOnClickListener(listener);
 		}
 		
 		if (fss != null)
 		{
 			FilterUtil.loadFilterBar(fss, filterParamHolder, actionViews);
 		}
 		else
 		{
 			for (View view : actionViews)
 			{
 				view.setVisibility(View.GONE);
 			}
 		}
 		
 		getView().findViewById(R.id.filter_item_more).setOnClickListener(listener);
 		
 	}
 	
 	private View[] findAllFilterView()
 	{
 		View filterParent =getView() == null ? null : getView().findViewById(R.id.filter_bar_root);
 		if (filterParent == null)
 		{
 			return null;
 		}
 		
 		View[] actionViews = new View[] {filterParent.findViewById(R.id.filter_item_1), 
 				filterParent.findViewById(R.id.filter_item_2),
 				filterParent.findViewById(R.id.filter_item_3)};
 		
 		return actionViews;
 	}
 	
 	private void resetSearch(BXLocation location)
 	{
 		boolean isNeryBy = this.isSerchNearBy();
 		
 		goodsListLoader.cancelFetching();
 		if (!isNeryBy)
 		{
 			goodsListLoader.setNearby(false);
 			goodsListLoader.setParams(getSearchParams());
 			goodsListLoader.setRuntime(true);
 		}
 		else
 		{
 //			goodsListLoader.cancelFetching();
 			
 //			List<String> params = new ArrayList<String>();
 //			params.addAll(getS);
 //			params.add("nearby=true");
 			goodsListLoader.setNearby(true);
 //			curLocation = QuanleimuApplication.getApplication().getCurrentPosition(false);
 			//Log.d("kkkkkk", "get goods nearby: ("+curLocation.fLat+", "+curLocation.fLon+") !!");
 //			params.add("lat="+location.fLat);
 //			params.add("lng="+location.fLon);
 			goodsListLoader.setParams(getSearchParams());
 			goodsListLoader.setRuntime(false);
 		}
 	}
 
 }
