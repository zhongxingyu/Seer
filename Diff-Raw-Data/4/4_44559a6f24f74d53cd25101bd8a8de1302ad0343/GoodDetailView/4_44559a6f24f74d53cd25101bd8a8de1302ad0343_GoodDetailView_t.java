 package com.quanleimu.view;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.lang.ref.WeakReference;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.ColorDrawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Base64;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.QuickContactBadge;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.activity.BaiduMapActivity;
 import com.quanleimu.activity.BaseActivity;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.QuanleimuMainActivity;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.AuthDialogListener;
 import com.quanleimu.entity.BXLocation;
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsDetail.EDATAKEYS;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.entity.WeiboAccessTokenWrapper;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ErrorHandler;
 import com.quanleimu.util.GoodsListLoader;
 import com.quanleimu.util.Helper;
 import com.quanleimu.util.Util;
 import com.quanleimu.util.ViewUtil;
 import com.tencent.mm.sdk.openapi.WXAppExtendObject;
 import com.tencent.mm.sdk.openapi.WXMediaMessage;
 import com.tencent.mm.sdk.platformtools.Log;
 import com.weibo.net.AccessToken;
 import com.weibo.net.Oauth2AccessTokenHeader;
 import com.weibo.net.Utility;
 import com.weibo.net.Weibo;
 import com.weibo.net.WeiboException;
 import com.weibo.net.WeiboParameters;
 
 public class GoodDetailView extends BaseView implements View.OnTouchListener,View.OnClickListener, OnItemSelectedListener/*, PullableScrollView.PullNotifier, View.OnTouchListener*/, GoodsListLoader.HasMoreListener{
 	
 	public interface IListHolder{
 		public void startFecthingMore();
 		public boolean onResult(int msg, GoodsListLoader loader);//return true if getMore succeeded, else otherwise
 	};
 	
 	
 	final private String strCollect = "收藏";
 	final private String strCancelCollect = "取消收藏";
 	final private int msgRefresh = 5;
 	final private int msgUpdate = 6;
 	final private int msgDelete = 7;
 	
 	public static final int MSG_ADINVERIFY_DELETED = 0x00010000;
 	public static final int MSG_MYPOST_DELETED = 0x00010001;
 
 	public GoodsDetail detail = new GoodsDetail();
 	private View titleControlView = null;
 	private AuthController authCtrl;
 	
 	private String json = "";
 	
 	private Bundle mBundle;
 	
 	private Bitmap mb_loading = null;
 	
 	private int type = 240;//width of screen
 	private int paddingLeftMetaPixel = 16;//meta, right part, value
 	
 	private boolean keepSilent = false;
 	
 	private GoodsListLoader mListLoader;
 //	private int mCurIndex = 0;
 	
 	private IListHolder mHolder = null;
 	
 	private Dialog manageDlg = null;
 	
 	private WeakReference<View> loadingMorePage;
 	
 	enum REQUEST_TYPE{
 		REQUEST_TYPE_REFRESH,
 		REQUEST_TYPE_UPDATE,
 		REQUEST_TYPE_DELETE
 	}
 	
 	public GoodDetailView(Context content, Bundle bundle, GoodsListLoader listLoader, final int curIndex, IListHolder holder){
 		super(content, bundle);
 		
 		mListLoader = listLoader;
 		detail = listLoader.getGoodsList().getData().get(curIndex);
 		listLoader.setSelection(curIndex);
 		mBundle = bundle;
 		mHolder = holder;
 		
 		listLoader.setHasMoreListener(this);
 		init(curIndex);
 	}
 	
 	@Override
 	public void onDestroy(){
 		this.keepSilent = true;
 		
 //		if(null != listUrl && listUrl.size() > 0)
 //			SimpleImageLoader.Cancel(listUrl);
 		
 		super.onDestroy();
 	}
 	
 	@Override
 	public boolean onBack(){
 		this.keepSilent = false;
 
 		return false;
 	}
 
 	@Override
 	public void onPause() {
 		this.keepSilent = true;
 		super.onPause();
 	}
 	
 	@Override
 	public void onResume(){
 		//the ad is viewed again
 		QuanleimuApplication.addViewCounter(this.detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 		this.keepSilent = false;
 		super.onPause();
 	}
 	
 	@Override
 	protected void onAttachedToWindow(){
         
 		if(isMyAd()){
 			btnStatus = 1;
 		}
 		else{
 			if(isInMyStore()){
 				btnStatus = 0;
 			}
 			else{
 				btnStatus = -1;
 			}
 		}
 		
 		(new Thread(new Runnable(){
 			@Override
 			public void run(){
 				GoodDetailView.this.saveToHistory();
 			}
 		})).start();
 		
 		if (authCtrl != null)
 		{
 			authCtrl.checkAfterAuth(getContext());
 		}
 		
 		super.onAttachedToWindow();
 	}
 	
 	private void saveToHistory(){
 		List<GoodsDetail> listLookHistory = QuanleimuApplication.getApplication().getListLookHistory();
 		if(listLookHistory != null){
 			for(int i=0;i<listLookHistory.size();i++)
 			{
 				if(listLookHistory.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 						.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)))
 				{
 					return;
 				}
 			}
 		}
 		if(null == listLookHistory){
 			listLookHistory = new ArrayList<GoodsDetail>();
 		}
 		listLookHistory.add(0, detail);
 		QuanleimuApplication.getApplication().setListLookHistory(listLookHistory);
 		Helper.saveDataToLocate(this.getContext(), "listLookHistory", listLookHistory);		
 	}
 
 	private boolean isMyAd(){
 		if(detail == null) return false;
 		List<GoodsDetail> myPost = QuanleimuApplication.getApplication().getListMyPost();
 		if(null != myPost){
 			for(int i = 0; i < myPost.size(); ++ i){
 				if(myPost.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 						.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	private boolean isInMyStore(){
 		if(detail == null) return false;
 		List<GoodsDetail> myStore = QuanleimuApplication.getApplication().getListMyStore();
 		if(myStore == null) return false;
 		for(int i = 0; i < myStore.size(); ++ i){
 			if(myStore.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 					.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
 				return true;
 			}
 		}
 		return false;		
 	}
 //	
 //	private int mLastY = 0;
 //	private int mLastExceedingY = 0;
 	public boolean onTouch (View v, MotionEvent event){
 //		if(!keepSilent){
 //			switch(event.getAction()){
 //			case MotionEvent.ACTION_MOVE:
 //				
 //				break;
 //			case MotionEvent.ACTION_CANCEL:
 //			case MotionEvent.ACTION_UP:
 //				//mLastExceedingY = 0;
 //				break;
 //			}
 //		}		
 		
 		return this.keepSilent;
 	}
 	
 	private String getMyId()
 	{
 		UserBean user = (UserBean) Util.loadDataFromLocate(getContext(), "user");
 		if (user == null)
 		{
 			return null;
 		}
 		return user.getId();
 	}
 
 	
 	protected void init(final int mCurIndex) {
 		
 		this.keepSilent = false;//magic flag to refuse unexpected touch event
 		
 		WindowManager wm = 
 				(WindowManager)QuanleimuApplication.getApplication().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
 		type = wm.getDefaultDisplay().getWidth();
 		
 		//different padding for meta value to avoid overlapping display
 		if (type < 480) {
 			this.paddingLeftMetaPixel = 0;
 		}else{
 			this.paddingLeftMetaPixel = 16;
 		}
 		
 		
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		View v = inflater.inflate(R.layout.gooddetailview, null);
 		addView(v);
 
 		BitmapFactory.Options o =  new BitmapFactory.Options();
         o.inPurgeable = true;
         mb_loading = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.moren1, o);
         
 		//the ad is viewed once
 //        QuanleimuApplication.addViewCounter(this.detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));	
 		
         
         final ViewPager vp = (ViewPager) v.findViewById(R.id.svDetail);
         vp.setAdapter(new PagerAdapter() {
 			
 			public Object instantiateItem(View arg0, int position) 
 			{
 				Integer posObj = Integer.valueOf(position);
 				View detail = LayoutInflater.from(vp.getContext()).inflate(R.layout.gooddetailcontent, null);
 				detail.setTag(posObj);
 				((ViewPager) arg0).addView(detail, 0);
 				if (position == mListLoader.getGoodsList().getData().size())
 				{
 					detail.findViewById(R.id.loading_more_progress_parent).setVisibility(View.VISIBLE);
 					detail.findViewById(R.id.llDetail).setVisibility(View.GONE);
 					loadMore(detail);
 				}
 				else
 				{
 					initContent(detail, mListLoader.getGoodsList().getData().get(position), position);
 				}
 				return detail;
 			}
 			
             public void destroyItem(View arg0, int index, Object arg2)
             {
                 ((ViewPager) arg0).removeView((View) arg2);
                 
                 final Integer pos = (Integer) ((View) arg2).getTag();
                 if (pos < mListLoader.getGoodsList().getData().size())
                 {
                 	List<String> listUrl = getImageUrls(mListLoader.getGoodsList().getData().get(pos));
                 	if(null != listUrl && listUrl.size() > 0)
                 		SimpleImageLoader.Cancel(listUrl);
                 }
                 
                 
             }
 
 			public boolean isViewFromObject(View arg0, Object arg1) {
 				return arg0 == arg1;
 			}
 			
 			public int getCount() {
 				return mListLoader.getGoodsList().getData().size() + (mListLoader.hasMore() ? 1 : 0);
 			}
 		});
         
         
         vp.setOnPageChangeListener(new OnPageChangeListener() {
 			
 			public void onPageSelected(int pos) {
 				keepSilent = false;//magic flag to refuse unexpected touch event
 				
 				Log.d("PAGER", "current page is changed to " + pos);
 				if (pos != mListLoader.getGoodsList().getData().size())
 				{
 					detail = mListLoader.getGoodsList().getData().get(pos);
 					updateContactBar(false);
 					updateTitleInfo();
 					
 					//the ad is viewed once
 			        QuanleimuApplication.addViewCounter(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 				}
 				else
 				{
 					updateContactBar(true);
 				}
 			}
 			
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				
 			}
 			
 			public void onPageScrollStateChanged(int arg0) {
 				
 			}
 		});
         
         vp.setCurrentItem(mCurIndex);
         
         mListLoader.setSelection(mCurIndex);
         mListLoader.setHandler(new Handler(){
 				@Override
 				public void handleMessage(Message msg) {
 					if(null != mHolder){
 						if(mHolder.onResult(msg.what, mListLoader)){
 							onGotMore();
 						}else{
 							onNoMore();
 						}
 						
 						if(msg.what == ErrorHandler.ERROR_NETWORK_UNAVAILABLE){
 							onLoadMoreFailed();
 						}
 					}else{
 						switch (msg.what) {
 						case GoodsListLoader.MSG_FINISH_GET_FIRST:				 
 							GoodsList goodsList = JsonUtil.getGoodsListFromJson(mListLoader.getLastJson());
 							mListLoader.setGoodsList(goodsList);
 							if (goodsList == null || goodsList.getData().size() == 0) {
 								Message msg1 = Message.obtain();
 								msg1.what = ErrorHandler.ERROR_COMMON_FAILURE;
 								Bundle bundle = new Bundle();
 								bundle.putString("popup_message", "没有符合的结果，请稍后并重试！");
 								msg1.setData(bundle);
 								QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg1);
 							} else {
 								//QuanleimuApplication.getApplication().setListGoods(goodsList.getData());
 							}
 							mListLoader.setHasMore(true);
 							
 							break;
 						case GoodsListLoader.MSG_NO_MORE:					
 	//						Message msg1 = Message.obtain();
 	//						msg1.what = ErrorHandler.ERROR_COMMON_FAILURE;
 	//						Bundle bundle = new Bundle();
 	//						bundle.putString("popup_message", "数据下载失败，请稍后重试！");
 	//						msg1.setData(bundle);
 	//						QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg1);
 							
 							onNoMore();
 							
 							mListLoader.setHasMore(false);
 							
 							break;
 						case GoodsListLoader.MSG_FINISH_GET_MORE:	
 							GoodsList goodsList1 = JsonUtil.getGoodsListFromJson(mListLoader.getLastJson());
 							if (goodsList1 == null || goodsList1.getData().size() == 0) {
 								Message msg2 = Message.obtain();
 								msg2.what = ErrorHandler.ERROR_COMMON_WARNING;
 								Bundle bundle1 = new Bundle();
 								bundle1.putString("popup_message", "后面没有啦！");
 								msg2.setData(bundle1);
 								QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 								
 								onNoMore();
 								
 								mListLoader.setHasMore(false);
 							} else {
 								List<GoodsDetail> listCommonGoods =  goodsList1.getData();
 								for(int i=0;i<listCommonGoods.size();i++)
 								{
 									mListLoader.getGoodsList().getData().add(listCommonGoods.get(i));
 								}
 								//QuanleimuApplication.getApplication().setListGoods(mListLoader.getGoodsList().getData());	
 								
 								mListLoader.setHasMore(true);
 								
 								onGotMore();
 							}
 							break;
 						case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
 							Message msg2 = Message.obtain();
 							msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 							QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 							
 //							ImageView imageView = (ImageView)findViewById(R.id.pull_to_next_image);
 //							imageView.setImageResource(R.drawable.ic_pulltorefresh_arrow_upsidedown);
 //							
 //							filloutHeader();
 //							filloutFooter();
 //							scrollParent.onNewViewFailed(true);
 							onLoadMoreFailed();
 							
 							break;
 						}
 					}
 					
 					super.handleMessage(msg);
 				}
 
 				private void onGotMore() {
 					final View page = loadingMorePage == null ? null : (View) loadingMorePage.get();
 					if (page != null)
 					{
 						page.postDelayed(new Runnable() {
 
 							@Override
 							public void run() {
 								page.findViewById(R.id.loading_more_progress_parent).setVisibility(View.GONE);
 								page.findViewById(R.id.llDetail).setVisibility(View.VISIBLE);
 								final Integer tag = (Integer)page.getTag();
 								initContent(page, mListLoader.getGoodsList().getData().get(tag.intValue()), tag.intValue());
 							}
 							
 						}, 10);
 					}
 					Log.d("PAGER", "more goods return.");
 				}
 
 				private void onNoMore() {
 //					ImageView imageView = (ImageView)findViewById(R.id.pull_to_next_image);
 //					imageView.setImageResource(R.drawable.ic_pulltorefresh_arrow_upsidedown);
 //					imageView.setVisibility(View.GONE);
 //					
 //					TextView textView = (TextView)findViewById(R.id.pull_to_next_text);
 //					textView.setText("后面没有啦！");
 //					
 //					scrollParent.onNewViewFailed(false);
 					ViewUtil.postShortToastMessage(GoodDetailView.this, "后面没有啦！", 0);
 				}
 			});
 	}
 	
 	private void initContent(View contentView, final GoodsDetail detail, final int pageIndex)
 	{
 		Log.d("PAGER", "init content view with detail " + detail.getValueByKey("title"));
 		
 		WindowManager wm = 
 				(WindowManager)QuanleimuApplication.getApplication().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
 		type = wm.getDefaultDisplay().getWidth();
 		
 		//different padding for meta value to avoid overlapping display
 		if (type < 480) {
 			this.paddingLeftMetaPixel = 0;
 		}else{
 			this.paddingLeftMetaPixel = 16;
 		}
 		
 		RelativeLayout llgl = (RelativeLayout) contentView.findViewById(R.id.llgl);
 		
 		if(!detail.getValueByKey("status").equals("4") && !detail.getValueByKey("status").equals("20")){
 			contentView.findViewById(R.id.ll_appeal).setVisibility(View.GONE);
 			contentView.findViewById(R.id.graymask).setVisibility(View.GONE);
 			contentView.findViewById(R.id.verifyseperator).setVisibility(View.GONE);
 		}
 		else{
 			if(detail.getValueByKey("tips").equals("")){
 				((TextView)contentView.findViewById(R.id.verifyreason)).setText("该信息不符合《百姓网公约》");
 			}
 			else{
 				((TextView)contentView.findViewById(R.id.verifyreason)).setText(detail.getValueByKey("tips"));
 			}
 			contentView.findViewById(R.id.fenxianglayout).setEnabled(false);
 			contentView.findViewById(R.id.showmap).setEnabled(false);
 			contentView.findViewById(R.id.jubaolayout).setEnabled(false);
			findViewById(R.id.sms).setEnabled(false);
			findViewById(R.id.call).setEnabled(false);
 			contentView.findViewById(R.id.appealbutton).setOnClickListener(this);
 		}
 		
 		if(detail.getImageList() != null){
 			List<String>listUrl = getImageUrls(detail);
 			
 			if(listUrl.size() == 0){
 				llgl = (RelativeLayout) contentView.findViewById(R.id.llgl);
 				llgl.setVisibility(View.GONE);
 			}else{
 				Gallery glDetail = (Gallery) contentView.findViewById(R.id.glDetail);
 				glDetail.setOnItemSelectedListener(this);
 				glDetail.setFadingEdgeLength(10);
 				glDetail.setSpacing(40);
 				
 				MainAdapter adapter = new MainAdapter(this.getContext(), listUrl);
 				glDetail.setAdapter(adapter);
 				glDetail.setOnTouchListener(this);
 				glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 					@Override
 					public void onItemClick(AdapterView<?> arg0, View arg1,
 							int arg2, long arg3) {
 						Bundle bundle = new Bundle();
 						bundle.putInt("postIndex", arg2);
 						bundle.putSerializable("goodsDetail", detail);
 						
 						if(null != m_viewInfoListener){
 							m_viewInfoListener.onNewView(new BigGalleryView(getContext(), bundle));
 						}
 					}
 				});
 				
 			}
 		}else{
 			llgl = (RelativeLayout) contentView.findViewById(R.id.llgl);
 			llgl.setVisibility(View.GONE);
 		}
 //		rl_test = (RelativeLayout) findViewById(R.id.detailLayout);
 		
 
 		TextView txt_tittle = (TextView) contentView.findViewById(R.id.goods_tittle);
 		TextView txt_message1 = (TextView) contentView.findViewById(R.id.sendmess1);
 		LinearLayout rl_address = (LinearLayout) contentView.findViewById(R.id.showmap);
 //		rl_address.setOnTouchListener(this);
 
 		LinearLayout ll_meta = (LinearLayout) contentView.findViewById(R.id.meta);
 		
 		View fenxiang = contentView.findViewById(R.id.fenxianglayout);
 		fenxiang.setOnClickListener(this);
 
 		
 		if(QuanleimuApplication.wxapi.isWXAppInstalled() && QuanleimuApplication.wxapi.isWXAppSupportAPI()){
 			contentView.findViewById(R.id.wxlayout).setOnClickListener(this);
 		}
 		else{
 			contentView.findViewById(R.id.wxlayout).setVisibility(View.GONE);
 		}
 		
 		View jubao = contentView.findViewById(R.id.jubaolayout);
 		if(isMyAd()){
 			jubao.setVisibility(View.GONE);
 		}
 		else{			
 			jubao.setOnClickListener(this);			
 		}
 
 		this.setMetaObject(contentView, detail);
 		
 		txt_message1.setText(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DESCRIPTION));
 		txt_tittle.setText(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_TITLE));
 
 		String areaNamesV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
 		if (areaNamesV != null && !areaNamesV.equals("")) 
 		{		
 			rl_address.setOnClickListener(this);
 //			String latV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT);
 //			String lonV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON);
 //			if(latV != null && !latV.equals("false") && !latV.equals("") && lonV != null && !lonV.equals("false") && !lonV.equals(""))
 //			{
 //				
 //			}
 		} 
 		
 		final ViewPager vp = (ViewPager) findViewById(R.id.svDetail);
 		if (pageIndex == vp.getCurrentItem())
 		{
 			updateContactBar(false);
 		}
 		
 	}
 	
 	private void updateContactBar(boolean forceHide)
 	{
 		LinearLayout rl_phone = (LinearLayout)findViewById(R.id.phonelayout);
 		if (forceHide || isMyAd())
 		{
 			rl_phone.setVisibility(View.GONE);
 			return;
 		}
 		
 		TextView txt_phone = (TextView) findViewById(R.id.number);
 		QuickContactBadge iv_contact = (QuickContactBadge) findViewById(R.id.contact);
 		View iv_call = (View)findViewById(R.id.call);
 		ImageView iv_sms = (ImageView)findViewById(R.id.sms);
 		View iv_buzz = findViewById(R.id.buzz);
 		String mobileV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_MOBILE);
 		
 		rl_phone.setVisibility(View.VISIBLE);
 		txt_phone.setOnClickListener(this);
 		iv_sms.setOnClickListener(this);
 		iv_call.setOnClickListener(this);
 		iv_buzz.setOnClickListener(this);
 		
 		if (mobileV != null
 				&& !mobileV.equals("")
 				&& !mobileV.equals("无")) {
 			txt_phone.setVisibility(View.VISIBLE);
 			iv_call.setVisibility(View.VISIBLE);
 			iv_sms.setVisibility(View.VISIBLE);
 			txt_phone.setText(mobileV);
 			iv_contact.assignContactFromPhone(mobileV, false);
 		} else {
 			txt_phone.setVisibility(View.INVISIBLE);
 			iv_call.setVisibility(View.INVISIBLE);
 			iv_sms.setVisibility(View.INVISIBLE);
 		}
 	}
 	
 	private void requireAuth4Talk()
 	{
 		if(null != m_viewInfoListener){
 			m_viewInfoListener.onNewView(new LoginView(getContext(), "返回"));
 			
 			if (authCtrl != null)
 			{
 				authCtrl.cancelAuth();
 			}
 			
 			authCtrl = new AuthController();
 			authCtrl.startWaitingAuth(new Runnable() {
 				public void run() {
 					startChat();
 				}
 				
 			}, null);
 			
 		}
 	}
 	
 	private void startChat()
 	{
 		if (m_viewInfoListener != null && this.detail != null)
 		{
 			Bundle bundle = new Bundle();
 //			bundle.putString("senderId", myUserId);
 			bundle.putString("receiverId", detail.getValueByKey("userId"));
 			bundle.putString("adId", detail.getValueByKey("id"));
 			bundle.putString("adTitle", detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_TITLE));
 			
 			m_viewInfoListener.onNewView(new TalkView(getContext(), bundle));
 		}
 	}
 	
 	
 	private int btnStatus = -1;//-1:strCollect, 0: strCancelCollect, 1:strManager
 	
 	private boolean handleRightBtnIfInVerify(){
 		if(!detail.getValueByKey("status").equals("0")){
 			pd = ProgressDialog.show(GoodDetailView.this.getContext(), "提示", "请稍候...");
 			pd.setCancelable(true);
 			new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_DELETE)).start();
 
 			return true;	
 		}
 		return false;
 	}
 	
 	private void handleStoreBtnClicked(){
 		if(handleRightBtnIfInVerify()) return;
 		if(-1 == btnStatus){
 			btnStatus = 0;
 			List<GoodsDetail> myStore = QuanleimuApplication.getApplication().getListMyStore();
 			
 			TitleDef title = getTitleDef();
 			title.m_rightActionHint = strCancelCollect;
 			m_viewInfoListener.onTitleChanged(title);
 			
 			if (myStore == null){
 				myStore = new ArrayList<GoodsDetail>();
 				myStore.add(0, detail);
 			} else {
 				if (myStore.size() >= 100) {
 					myStore.remove(0);
 				}
 				myStore.add(0, detail);
 			}		
 			QuanleimuApplication.getApplication().setListMyStore(myStore);
 			Helper.saveDataToLocate(this.getContext(), "listMyStore", myStore);
 			Toast.makeText(this.getContext(), "收藏成功", 3).show();
 		}
 		else if (0 == btnStatus) {
 			btnStatus = -1;
 			List<GoodsDetail> myStore = QuanleimuApplication.getApplication().getListMyStore();
 			for (int i = 0; i < myStore.size(); i++) {
 				if (detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 						.equals(myStore.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))) {
 					myStore.remove(i);
 					break;
 				}
 			}
 			QuanleimuApplication.getApplication().setListMyStore(myStore);
 			Helper.saveDataToLocate(this.getContext(), "listMyStore", myStore);
 			TitleDef title = getTitleDef();
 			title.m_rightActionHint = strCollect;
 			m_viewInfoListener.onTitleChanged(title);
 			Toast.makeText(this.getContext(), "取消收藏", 3).show();
 		}
 		else if(1 == btnStatus){
 			final String[] names = {"编辑","刷新","删除"};
 			new AlertDialog.Builder(this.getContext()).setTitle("选择操作")
 					.setItems(names, new DialogInterface.OnClickListener(){
 						public void onClick(DialogInterface dialog, int which){
 							switch(which){
 								case 0:
 									if(null != m_viewInfoListener){
 										m_viewInfoListener.onNewView(new PostGoodsView((BaseActivity)GoodDetailView.this.getContext(),
 												mBundle, 
 												detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME),
 												detail));			
 									}
 //									Bundle bundle = new Bundle();
 //									bundle.putSerializable("goodsDetail", detail);
 //									bundle.putString("categoryEnglishName",detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME));
 //									intent.putExtras(bundle);									
 //									intent.setClass(GoodDetail.this, PostGoods.class);
 //									startActivity(intent);									
 //									dialog.dismiss();
 									break;
 								case 1:
 									pd = ProgressDialog.show(GoodDetailView.this.getContext(), "提示", "请稍候...");
 									pd.setCancelable(true);
 									new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_REFRESH)).start();
 									dialog.dismiss();
 									break;									
 								case 2:
 									pd = ProgressDialog.show(GoodDetailView.this.getContext(), "提示", "请稍候...");
 									pd.setCancelable(true);
 									new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_DELETE)).start();
 									dialog.dismiss();
 									break;
 								default:
 									break;
 							}
 						}
 					})
 					.setNegativeButton(
 				     "取消", new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.cancel();							
 						}
 					}).show();
 		}
 	}
 	
 	@Override
 	public boolean onRightActionPressed(){
 		handleStoreBtnClicked();
 		return true;
 	}
 	
 	class ManagerAlertDialog extends AlertDialog{
 		public ManagerAlertDialog(Context context, int theme){
 			super(context, theme);
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.number:{
 			findViewById(R.id.contact).performClick();
 			break;
 		}
 		case R.id.retry_load_more:
 			retryLoadMore();
 			break;
 		case R.id.appealbutton:
 			m_viewInfoListener.onNewView(new OpinionBackView(this.getContext(), mBundle, 1, detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID)));
 			break;
 		case R.id.call:{
 			TextView txt_phone = (TextView) findViewById(R.id.number);
 			Uri uri = Uri.parse("tel:" + txt_phone.getText().toString());
 			Intent intent = new Intent(Intent.ACTION_DIAL, uri);
 			this.getContext().startActivity(intent);
 			break;	
 		}
 		case R.id.buzz: {
 			String userId = getMyId();
 			if (userId == null)
 			{
 				requireAuth4Talk();
 			}
 			else
 			{
 				startChat();
 			}
 			break;
 		}
 		case R.id.sms:{
 			TextView txt_phone = (TextView) findViewById(R.id.number);
 			Uri uri = Uri.parse("smsto:" + txt_phone.getText().toString());
 			Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
 			this.getContext().startActivity(intent);			
 			break;
 		}
 		case R.id.showmap:
 			String latV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT);
 			String lonV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON);
 			BXLocation location = QuanleimuApplication.getApplication().getCurrentPosition(true);
 //			if(null != location){
 //				latV = ""+location.fLat;
 //				lonV = ""+location.fLon;
 //				
 //				Toast.makeText(getContext(), "现在用的是百度api位置，不是帖子位置！！", Toast.LENGTH_LONG);
 //			}
 			if(latV != null && !latV.equals("false") && !latV.equals("") && lonV != null && !lonV.equals("false") && !lonV.equals(""))
 			{
 				final double lat = Double.valueOf(latV);
 				final double lon = Double.valueOf(lonV);
 				Thread convertThread = new Thread(new Runnable(){
 					@Override
 					public void run(){
 						String baiduUrl = String.format("http://api.map.baidu.com/ag/coord/convert?from=2&to=4&x=%s&y=%s", 
 								String.valueOf(lat), String.valueOf(lon));
 						try{
 							String baiduJsn = Communication.getDataByUrlGet(baiduUrl);
 							JSONObject js = new JSONObject(baiduJsn);
 							Object errorCode = js.get("error");
 							if(errorCode instanceof Integer && (Integer)errorCode == 0){
 								String x = (String)js.get("x");
 								String y = (String)js.get("y");
 								byte[] bytes = Base64.decode(x, Base64.DEFAULT);
 								x = new String(bytes, "UTF-8");
 								
 								bytes = Base64.decode(y, Base64.DEFAULT);
 								y = new String(bytes, "UTF-8");
 								
 								Double dx = Double.valueOf(x);
 								Double dy = Double.valueOf(y);
 								
 								int ix = (int)(dx * 1E6);
 								int iy = (int)(dy * 1E6);
 								
 								x = String.valueOf(ix);
 								y = String.valueOf(iy);
 								
 								Bundle bundle = new Bundle();
 								bundle.putString("detailPosition", x +"," + y);
 								bundle.putString("title", detail.getValueByKey(EDATAKEYS.EDATAKEYS_AREANAME));
 								//TODO:
 								BaseActivity baseActivity = (BaseActivity)getContext();
 								baseActivity.getIntent().putExtras(bundle);
 								
 								baseActivity.getIntent().setClass(baseActivity, BaiduMapActivity.class);
 								baseActivity.startActivity(baseActivity.getIntent());
 								return;
 							}
 
 						}catch(UnsupportedEncodingException e){
 							e.printStackTrace();
 						}catch(Exception e){
 							e.printStackTrace();
 						}
 						String positions = Integer.toString((int)(lat*1E6)) + "," + Integer.toString((int)(lon*1E6));
 						Bundle bundle = new Bundle();
 						bundle.putString("detailPosition", positions);
 						bundle.putString("title", detail.getValueByKey(EDATAKEYS.EDATAKEYS_AREANAME));
 						//TODO:
 						BaseActivity baseActivity = (BaseActivity)getContext();
 						baseActivity.getIntent().putExtras(bundle);
 						
 						baseActivity.getIntent().setClass(baseActivity, BaiduMapActivity.class);
 						baseActivity.startActivity(baseActivity.getIntent());
 
 					}
 				});
 				convertThread.start();
 			}
 			else{
 				Thread getCoordinate = new Thread(new Runnable(){
 		            @Override
 		            public void run() {
 		            	if(!GoodDetailView.this.isShown()) return;
 						String city = QuanleimuApplication.getApplication().cityName;
 						if(!city.equals("")){
 							String googleUrl = String.format("http://maps.google.com/maps/geo?q=%s&output=csv", city);
 							try{
 								String googleJsn = Communication.getDataByUrlGet(googleUrl);
 								String[] info = googleJsn.split(",");
 								if(info != null && info.length == 4){
 									String positions = 
 											Integer.toString((int)(Double.parseDouble(info[2]) * 1E6))
 											+ "," + Integer.toString((int)(Double.parseDouble(info[3]) * 1E6));
 									Bundle bundle = new Bundle();
 									bundle.putString("detailPosition", positions);
 									bundle.putString("title", detail.getValueByKey(EDATAKEYS.EDATAKEYS_AREANAME));
 									//TODO:
 									BaseActivity baseActivity = (BaseActivity)getContext();
 									baseActivity.getIntent().putExtras(bundle);
 									
 									baseActivity.getIntent().setClass(baseActivity, BaiduMapActivity.class);
 									baseActivity.startActivity(baseActivity.getIntent());
 								}
 							}catch(UnsupportedEncodingException e){
 								e.printStackTrace();
 							}catch(Exception e){
 								e.printStackTrace();
 							}
 						}	
 		            }
 				});
 				getCoordinate.start();
 
 			}
 			break;
 			
 		case R.id.wxlayout:{
 			doShare2WX();
 			break;
 		}
 		
 		case R.id.fenxianglayout:{
 			Weibo weibo = Weibo.getInstance();
 			weibo.setupConsumerConfig(QuanleimuApplication.kWBBaixingAppKey, QuanleimuApplication.kWBBaixingAppSecret);
 			weibo.setRedirectUrl("http://www.baixing.com");
 
 			if(QuanleimuApplication.getWeiboAccessToken() == null){
 				WeiboAccessTokenWrapper tokenWrapper = (WeiboAccessTokenWrapper)Helper.loadDataFromLocate(this.getContext(), "weiboToken");
 				AccessToken token = null;
 				if(tokenWrapper != null && tokenWrapper.getToken() != null){
 					token = new AccessToken(tokenWrapper.getToken(), QuanleimuApplication.kWBBaixingAppSecret);
 //					token.setExpiresIn(tokenWrapper.getExpires());
 					Utility.setAuthorization(new Oauth2AccessTokenHeader());
 					QuanleimuApplication.setWeiboAccessToken(token);
 				}
 			}
 			
 			if(QuanleimuApplication.getWeiboAccessToken() != null){
 				Weibo.getInstance().setAccessToken(QuanleimuApplication.getWeiboAccessToken());
 				doShare2Weibo(QuanleimuApplication.getWeiboAccessToken());
 			}else{
 	            WeiboParameters parameters=new WeiboParameters();
 	            parameters.add("forcelogin", "true");
 	            Utility.setAuthorization(new Oauth2AccessTokenHeader());
                 com.quanleimu.entity.AuthDialogListener lsn = 
                 		new AuthDialogListener(this.getContext(), new AuthDialogListener.AuthListener() {
 	                	@Override
 	                	public void onComplete(){
 	        				WeiboAccessTokenWrapper tokenWrapper = (WeiboAccessTokenWrapper)Helper.loadDataFromLocate(GoodDetailView.this.getContext(), "weiboToken");
 	        				AccessToken token = null;
 	        				if(tokenWrapper != null && tokenWrapper.getToken() != null){
 	        					token = new AccessToken(tokenWrapper.getToken(), QuanleimuApplication.kWBBaixingAppSecret);
 	        					token.setExpiresIn(tokenWrapper.getExpires());
 	        					QuanleimuApplication.setWeiboAccessToken(token);
 	        					Weibo.getInstance().setAccessToken(token);
 	        					doShare2Weibo(token);
 	        				}
 	                	}
                 }); 
 	            weibo.dialog(this.getContext(), parameters, lsn);	 
 	            lsn.setInAuthrize(true);
 			}
 
 			break;
 		}
 		case R.id.jubaolayout:{
 			if(this.m_viewInfoListener != null){
 				this.m_viewInfoListener.onNewView(new OpinionBackView(this.getContext(), mBundle, 0, detail.getValueByKey("id")));
 			}
 			break;
 		}
 		case R.id.managebtn:
 			showManageDialog();
 			break;
 		case R.id.manager_refresh:{
 //		case R.id.iv_refresh:{
 			manageDlg.dismiss();
 			pd = ProgressDialog.show(GoodDetailView.this.getContext(), "提示", "请稍候...");
 			pd.setCancelable(true);
 			new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_REFRESH)).start();
 			
 			break;
 		}
 //		case R.id.
 		case R.id.manager_edit:{
 			manageDlg.dismiss();
 //		case R.id.iv_edit:{
 			if(null != m_viewInfoListener){
 				m_viewInfoListener.onNewView(new PostGoodsView((BaseActivity)GoodDetailView.this.getContext(),
 						mBundle, 
 						detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME),
 						detail));			
 			}
 			
 			break;
 		}
 		case R.id.manager_delete:{
 			manageDlg.dismiss();
 //		case R.id.iv_del:{
 			new AlertDialog.Builder(GoodDetailView.this.getContext()).setTitle("提醒")
 			.setMessage("是否确定删除")
 			.setPositiveButton("确定", new DialogInterface.OnClickListener() {							
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					pd = ProgressDialog.show(GoodDetailView.this.getContext(), "提示", "请稍候...");
 					pd.setCancelable(true);
 					new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_DELETE)).start();			
 				}
 			})
 			.setNegativeButton(
 		     "取消", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();							
 				}
 			})
 		     .show();
 			break;
 		}
 		}
 	}
 	
 	private void showManageDialog(){
 		if(manageDlg != null && manageDlg.isShowing()){
 			manageDlg.dismiss();
 			return;
 		}
 		manageDlg = new Dialog(this.getContext(), android.R.style.Theme_Translucent_NoTitleBar);
 		manageDlg.setContentView(R.layout.managerpost);
 		manageDlg.findViewById(R.id.manager_refresh).setOnClickListener(this);
 		manageDlg.findViewById(R.id.manager_edit).setOnClickListener(this);
 		manageDlg.findViewById(R.id.manager_delete).setOnClickListener(this);
 
 		manageDlg.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
 	    
 	    int location[] = new int[]{0, 0};
 	    this.getChildAt(0).getLocationOnScreen(location);
 	    WindowManager.LayoutParams lp = manageDlg.getWindow().getAttributes();
 	    lp.y = location[1];
 	    manageDlg.getWindow().setAttributes(lp);
 	    manageDlg.getWindow().setGravity(Gravity.TOP);
 	    manageDlg.getWindow().setBackgroundDrawable(new ColorDrawable(0));
 	    
 	    manageDlg.setCancelable(true);
 	    manageDlg.setCanceledOnTouchOutside(true);
 	    manageDlg.show();
 	}
 	
 	private String convert2JSONString(GoodsDetail detail){
 		JSONObject obj = new JSONObject();
 		Set<String> keys = detail.getKeys();
 		Object[] keyAry = keys.toArray();
 		JSONArray jsonAry = new JSONArray();
 		JSONObject subObj = new JSONObject();
 		try{
 			for(int i = 0; i < keyAry.length; ++ i){				
 				String key = (String)keyAry[i];
 				String value = detail.getValueByKey((String)keyAry[i]); 
 				if(value == null) value = "";
 				subObj.put(key, value);
 			}			
 			
 			if(detail.getImageList() != null){
 				JSONObject jsonImgs = new JSONObject();
 				if(detail.getImageList().getBig() != null && !detail.getImageList().getBig().equals("")){
 					JSONArray imgAry = new JSONArray();
 					String big = detail.getImageList().getBig();
 					String[] bigs = big.split(",");
 					for(int m = 0; m < bigs.length; ++ m){
 						imgAry.put(bigs[m].substring(1, bigs[m].length() - 1));
 					}
 //					jsonImgs.put("big", detail.getImageList().getBig());
 					jsonImgs.put("big", imgAry);
 				}
 				if(detail.getImageList().getResize180() != null && !detail.getImageList().getResize180().equals("")){
 //					jsonImgs.put("resize180", detail.getImageList().getResize180());
 					JSONArray imgAry = new JSONArray();
 					String resize = detail.getImageList().getResize180();
 					String[] resizes = resize.split(",");
 					for(int m = 0; m < resizes.length; ++ m){
 						imgAry.put(resizes[m].substring(1, resizes[m].length() - 1));
 					}
 					jsonImgs.put("resize180", imgAry);
 					
 				}
 				subObj.put("images", jsonImgs);
 			}
 			
 			if(detail.getMetaData() != null && detail.getMetaData().size() > 0){
 				JSONArray jsonMetaAry = new JSONArray();
 				for(int t = 0; t < detail.getMetaData().size(); ++ t){
 					jsonMetaAry.put(detail.getMetaData().get(t));
 				}
 				subObj.put("metaData", jsonMetaAry);
 			}
 			jsonAry.put(subObj);
 			obj.put("data", jsonAry);
 			obj.put("count", 1);
 		} catch(JSONException e){
 			e.printStackTrace();
 		}
 		
 //		GoodsList gl = JsonUtil.getGoodsListFromJson(obj.toString());
 		return obj.toString();
 	}
 	
 	private void doShare2WX(){
 		QuanleimuApplication.wxapi.registerApp(QuanleimuMainActivity.WX_APP_ID);
 		String detailJson = convert2JSONString(this.detail);
 		String title = isMyAd() ? "我在百姓网发布：" + detail.getValueByKey("title") :
 			"我在百姓网看到：" + detail.getValueByKey("title");
 		
 //		WXWebpageObject webObj = new WXWebpageObject();
 //		webObj.webpageUrl = detail.getValueByKey("link");
 		WXAppExtendObject appObj = new WXAppExtendObject();
 		appObj.fileData = detailJson.getBytes();
 		
 		List<String> listUrl = getImageUrls(this.detail);
 		String imgPath = (listUrl == null ? "" : SimpleImageLoader.getFileInDiskCache(listUrl.get(0)));
 
 		WXMediaMessage obj = new WXMediaMessage();
 		
 		String description = "";
 		if(detail.getMetaData() != null){
 			for(int i = 0; i < detail.getMetaData().size(); ++ i){
 				String meta = detail.getMetaData().get(i);
 				String [] ms = meta.split(" ");
 				if(ms != null && ms.length == 2){
 					description += "，" + ms[1];
 				}
 			}
 		}
 		if(description.charAt(0) == '，'){
 			description = description.substring(1);
 		}
 		obj.description = description;
 		obj.title = title;
 		obj.mediaObject = appObj;
 		Bitmap thumbnail = imgPath == null ? null : BitmapFactory.decodeFile(imgPath);
 		if(thumbnail != null){
 			obj.setThumbImage(thumbnail);
 		}
 		QuanleimuApplication.sendWXRequest(obj);
 	}
 	
 	private void doShare2Weibo(AccessToken accessToken){
 		try{ 
 			List<String> listUrl = getImageUrls(this.detail);
 		Weibo.getInstance().share2weibo((BaseActivity)GoodDetailView.this.getContext(),
 				accessToken.getToken(),
 				accessToken.getSecret(), 
 				isMyAd() ? "我在#百姓网#发布" + detail.getValueByKey("title") + ",求扩散！" + detail.getValueByKey("link") :
 						"我在#百姓网#看到" + detail.getValueByKey("title") + ",求扩散！" + detail.getValueByKey("link"), 
 				listUrl == null ? "" : SimpleImageLoader.getFileInDiskCache(listUrl.get(0)));
 		}
 		catch(WeiboException e){
 			e.printStackTrace();
 		}
 	}
 	
 	private void setMetaObject(View currentPage, GoodsDetail detail){
 		LinearLayout ll_meta = (LinearLayout) currentPage.findViewById(R.id.meta);
 		if(ll_meta == null) return;
 		ll_meta.removeAllViews();
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		for (int i = 0; i < detail.getMetaData().size(); i++) {
 			String[] s = detail.getMetaData().get(i).split(" ");
 			if(s.length < 2) continue;
 
 			View v = null;
 			v = inflater.inflate(R.layout.item_meta, null);
 
 			TextView tvmetatxt = (TextView) v.findViewById(R.id.tvmetatxt);
 			TextView tvmeta = (TextView) v.findViewById(R.id.tvmeta);
 			tvmeta.setPadding(this.paddingLeftMetaPixel, 0, 0, 0);
 
 			tvmetatxt.setText(detail.getMetaData().get(i).split(" ")[0].toString() + "：");
 			tvmeta.setText(detail.getMetaData().get(i).split(" ")[1].toString());
 
 			v.setTag(i);
 			ll_meta.addView(v);
 		}
 
 		Date date = new Date(Long.parseLong(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DATE)) * 1000);
 		SimpleDateFormat df = new SimpleDateFormat("MM月dd日 HH:mm:ss",
 				Locale.SIMPLIFIED_CHINESE);
 		String strTime = df.format(date);
 		View time = inflater.inflate(R.layout.item_meta, null);
 		TextView timetxt = (TextView) time.findViewById(R.id.tvmetatxt);
 		TextView timevalue = (TextView) time.findViewById(R.id.tvmeta);
 		timevalue.setPadding(this.paddingLeftMetaPixel, 0, 0, 0);
 		timetxt.setText("更新时间： ");
 		timevalue.setText(strTime);
 		ll_meta.addView(time);
 	}
 	
 	public Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case msgRefresh:
 				if(json == null){
 					Toast.makeText(GoodDetailView.this.getContext(), "刷新失败，请稍后重试！", 0).show();
 					break;
 				}
 				try {
 					JSONObject jb = new JSONObject(json);
 					JSONObject js = jb.getJSONObject("error");
 					String message = js.getString("message");
 					int code = js.getInt("code");
 					if (code == 0) {
 						new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_UPDATE)).start();
 						Toast.makeText(GoodDetailView.this.getContext(), message, 0).show();
 					}else if(2 == code){
 						if(pd != null){
 							pd.dismiss();
 						}
 						new AlertDialog.Builder(GoodDetailView.this.getContext()).setTitle("提醒")
 						.setMessage(message)
 						.setPositiveButton("确定", new DialogInterface.OnClickListener() {							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								// TODO Auto-generated method stub
 								pd = ProgressDialog.show(GoodDetailView.this.getContext(), "提示", "请稍候...");
 								pd.setCancelable(true);
 
 								new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_REFRESH, 1)).start();
 								dialog.dismiss();
 							}
 						})
 						.setNegativeButton(
 					     "取消", new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								dialog.cancel();							
 							}
 						})
 					     .show();
 
 					}else {
 						if(pd != null){
 							pd.dismiss();
 						}
 						Toast.makeText(GoodDetailView.this.getContext(), message, 0).show();
 					}
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				break;			
 			case msgUpdate:
 				if(pd!=null){
 					pd.dismiss();
 				}
 				GoodsList goods = JsonUtil.getGoodsListFromJson(json);
 				List<GoodsDetail> goodsDetails = goods.getData();
 				if(goodsDetails != null && goodsDetails.size() > 0){
 					for(int i = 0; i < goodsDetails.size(); ++ i){
 						if(goodsDetails.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 								.equals(GoodDetailView.this.detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
 							GoodDetailView.this.detail = goodsDetails.get(i);
 							break;
 						}
 					}
 					List<GoodsDetail>listMyPost = QuanleimuApplication.getApplication().getListMyPost();
 					if(listMyPost != null){
 						for(int i = 0; i < listMyPost.size(); ++ i){
 							if(listMyPost.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 									.equals(GoodDetailView.this.detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
 								listMyPost.set(i, GoodDetailView.this.detail);
 								break;
 							}
 						}
 					}
 					//QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 				}
 
 //				setMetaObject(); FIXME: should update current UI.
 				break;
 			case msgDelete:
 				if(pd!=null){
 					pd.dismiss();
 				}
 				try {
 					JSONObject jb = new JSONObject(json);
 					JSONObject js = jb.getJSONObject("error");
 					String message = js.getString("message");
 					int code = js.getInt("code");
 					if (code == 0) {
 						if(detail.getValueByKey("status").equals("0")){
 							List<GoodsDetail> listMyPost = QuanleimuApplication.getApplication().getListMyPost();
 							if(null != listMyPost){
 								for(int i = 0; i < listMyPost.size(); ++ i){
 									if(listMyPost.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 											.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
 										listMyPost.remove(i);
 										break;
 									}
 								}
 							}
 	//						listMyPost.remove(pos);
 							//QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 							if(m_viewInfoListener != null){
 								m_viewInfoListener.onBack(MSG_MYPOST_DELETED, null);
 							}
 						}
 						else{
 							m_viewInfoListener.onBack(MSG_ADINVERIFY_DELETED, detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
 						}
 //						finish();
 						Toast.makeText(GoodDetailView.this.getContext(), message, 0).show();
 					} else {
 						// 删除失败
 						Toast.makeText(GoodDetailView.this.getContext(), "删除失败,请稍后重试！", 0).show();
 					}
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				break;
 			default:
 				break;
 			}
 			super.handleMessage(msg);
 		}
 	};
 
 	class RequestThread implements Runnable{
 		private REQUEST_TYPE type;
 		private int pay = 0;
 		public RequestThread(REQUEST_TYPE type){
 			this.type = type;
 		}
 		public RequestThread(REQUEST_TYPE type, int pay) {
 			this.type = type;
 			this.pay = pay;
 		}
 		@Override
 		public void run(){
 			synchronized(GoodDetailView.this){
 				ArrayList<String> requests = null;
 				String apiName = null;
 				int msgToSend = -1;
 				if(REQUEST_TYPE.REQUEST_TYPE_DELETE == type){
 					requests = doDelete();
 					apiName = "ad_delete";
 					msgToSend = msgDelete;
 				}
 				else if(REQUEST_TYPE.REQUEST_TYPE_REFRESH == type){
 					requests = doRefresh(this.pay);
 					apiName = "ad_refresh";
 					msgToSend = msgRefresh;
 				}
 				else if(REQUEST_TYPE.REQUEST_TYPE_UPDATE == type){
 					requests = doUpdate();
 					apiName = "ad_list";
 					msgToSend = msgUpdate;
 				}
 				if(requests != null){
 					String url = Communication.getApiUrl(apiName, requests);
 					try {
 						json = Communication.getDataByUrl(url, true);
 					} catch (UnsupportedEncodingException e) {
 						QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 						if(pd != null){
 							pd.dismiss();
 						}
 					} catch (IOException e) {
 						QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 						if(pd != null){
 							pd.dismiss();
 						}						
 					} catch (Communication.BXHttpException e){
 						
 					}
 					myHandler.sendEmptyMessage(msgToSend);
 				}
 			}
 		}
 	}
 	
 	private ArrayList<String> doRefresh(int pay){
 		json = "";
 		ArrayList<String> list = new ArrayList<String>();
 
 		UserBean user = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
 		String mobile = user.getPhone();
 		String password = user.getPassword();
 
 		list.add("mobile=" + mobile);
 		String password1 = Communication.getMD5(password);
 		password1 += Communication.apiSecret;
 		String userToken = Communication.getMD5(password1);
 		list.add("userToken=" + userToken);
 		list.add("adId=" + detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 		list.add("rt=1");
 		if(pay != 0){
 			list.add("pay=1");
 		}
 
 		return list;
 	}
 	
 	private ArrayList<String> doUpdate(){
 		json = "";
 		ArrayList<String> list = new ArrayList<String>();
 		
 		UserBean user = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
 		String mobile = user.getPhone();
 		String password = user.getPassword();
 
 		list.add("mobile=" + mobile);
 		String password1 = Communication.getMD5(password);
 		password1 += Communication.apiSecret;
 		String userToken = Communication.getMD5(password1);
 		list.add("userToken=" + userToken);
 		list.add("query=id:" + detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 		list.add("rt=1");
 		return list;		
 	}
 	
 	private ArrayList<String> doDelete(){
 		// TODO Auto-generated method stub
 		UserBean user = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
 		String mobile = user.getPhone();
 		String password = user.getPassword();
 
 		json = "";
 //		String apiName = "ad_delete";
 		ArrayList<String> list = new ArrayList<String>();
 		list.add("mobile=" + mobile);
 		String password1 = Communication.getMD5(password);
 		password1 += Communication.apiSecret;
 		String userToken = Communication.getMD5(password1);
 		list.add("userToken=" + userToken);
 		list.add("adId=" + detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 		list.add("rt=1");
 		
 		return list;		
 	}
 
 	class MainAdapter extends BaseAdapter {
 		Context context;
 		List<String> listUrl;
 
 		public MainAdapter(Context context, List<String> listUrl) {
 			this.context = context;
 			this.listUrl = listUrl;
 		}
 
 		@Override
 		public int getCount() {
 			return listUrl.size();
 		}
 		
 		public List<String> getImages()
 		{
 			return listUrl;
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 			return listUrl.get(arg0);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = LayoutInflater.from(context);
 			View v = null;
 //			if (convertView != null) {
 //				v = (ImageView) convertView;
 //			} else {
 				v = inflater.inflate(R.layout.item_detailview, null);
 //			}
 			ImageView iv = (ImageView) v.findViewById(R.id.ivGoods);
 			
 			if (type <= 240) {
 				iv.setLayoutParams(new Gallery.LayoutParams(86, 86));
 			} else if (type <= 320) {
 				iv.setLayoutParams(new Gallery.LayoutParams(145, 145));
 			} else if (type <= 480) {
 				iv.setLayoutParams(new Gallery.LayoutParams(210, 210));
 			} else if (type <= 540) {
 				iv.setLayoutParams(new Gallery.LayoutParams(235, 235));
 			} else if (type <= 640) {
 				iv.setLayoutParams(new Gallery.LayoutParams(240, 240));
 			}else{
 				iv.setLayoutParams(new Gallery.LayoutParams(245,245));
 			}
 
 			iv.setImageBitmap(mb_loading);
 			
 			if (listUrl.size() != 0 && listUrl.get(position) != null) {
 				iv.setTag(listUrl.get(position));
 				SimpleImageLoader.showImg(iv, listUrl.get(position), GoodDetailView.this.getContext());
 			}
 			
 			//Log.d("GoodDetailView: ", "getView for position-" + position);
 			
 			return iv;
 		}
 	}
 	
 	@Override
 	public boolean onLeftActionPressed(){
 		return false;
 	}
 	
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_leftActionHint = "返回";
 		title.m_rightActionHint = detail.getValueByKey("status").equals("0") ? "收藏" : null;
 		title.m_title = "详细信息";
 		title.m_visible = true;
 		
 		if(isMyAd()){
 			if(titleControlView == null){
 				LayoutInflater inflater = LayoutInflater.from(this.getContext());
 //				titleControlView = inflater.inflate(R.layout.myad_title, null);
 //				View refresh = titleControlView.findViewById(R.id.iv_refresh);
 //				refresh.setOnClickListener(this);
 //				View edit = titleControlView.findViewById(R.id.iv_edit);
 //				edit.setOnClickListener(this);
 //				View del = titleControlView.findViewById(R.id.iv_del);
 //				del.setOnClickListener(this);
 				titleControlView = inflater.inflate(R.layout.managebtn, null);
 				titleControlView.setOnClickListener(this);
 			}
 			
 			title.m_rightActionHint = "";
 			title.m_titleControls = titleControlView;
 		}
 		else{
 			if(isInMyStore()){
 				title.m_rightActionHint = strCancelCollect;
 			}
 			else{
 				title.m_rightActionHint = detail.getValueByKey("status").equals("0") ? strCollect : "删除";
 			}
 		}
 		
 		return title;
 	}
 	
 	private void updateTitleInfo()
 	{
 		TitleDef def =  getTitleDef();
 		if (m_viewInfoListener != null)
 		{
 			m_viewInfoListener.onTitleChanged(def);
 		}
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = false;
 		return tab;
 	}
 	
 	
     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
     {
     	if (parent.getAdapter() instanceof MainAdapter)
     	{
     		MainAdapter mainAdapter = (MainAdapter) parent.getAdapter();
     		
     		List<String> listUrl = mainAdapter.getImages();
     		ArrayList<String> urls = new ArrayList<String>();
     		urls.add(listUrl.get(position));
     		for(int index = 0; (index + position < listUrl.size() || position - index >= 0); ++index){
     			if(index + position < listUrl.size())
     				urls.add(listUrl.get(index+position));
     			
     			if(position - index >= 0)
     				urls.add(listUrl.get(position-index));				
     		}
     		SimpleImageLoader.AdjustPriority(urls);
     	}
     }
 
     @Override
     public void onNothingSelected(AdapterView<?> arg0)
     {
         // TODO Auto-generated method stub
     }	
 
 
 	@Override
 	public void onHasMoreStatusChanged() {
 	}
 	
 	private void onLoadMoreFailed()
 	{
 		final View page = loadingMorePage == null ? null : (View) loadingMorePage.get();
 		if (page != null)
 		{
 			page.findViewById(R.id.retry_load_more).setOnClickListener(GoodDetailView.this);
 			page.postDelayed(new Runnable() {
 				@Override
 				public void run() {
 					page.findViewById(R.id.loading_more_progress_parent).setVisibility(View.GONE);
 					page.findViewById(R.id.retry_more_parent).setVisibility(View.VISIBLE);
 					page.findViewById(R.id.llDetail).setVisibility(View.GONE);
 				}
 				
 			}, 10);
 		}
 		Log.d("PAGER", "fail to load more.");
 	}
 	
 	private void retryLoadMore()
 	{
 		//We assume that this action always on UI thread.
 		final View page = loadingMorePage == null ? null : (View) loadingMorePage.get();
 		if (page != null)
 		{
 			page.findViewById(R.id.loading_more_progress_parent).setVisibility(View.VISIBLE);
 			page.findViewById(R.id.retry_more_parent).setVisibility(View.GONE);
 			page.findViewById(R.id.llDetail).setVisibility(View.GONE);
 		}
 		
 		if (null != mHolder) {
 			mHolder.startFecthingMore();
 		} else {
 			mListLoader
 					.startFetching(
 							false,
 							((GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE == mListLoader
 									.getDataStatus()) ? Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE
 									: Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL));
 		}
 	}
 	
 	private void loadMore(View page) {
 		loadingMorePage = new WeakReference(page);
 		
 		retryLoadMore();
 	}
 	
 	private static List<String> getImageUrls(GoodsDetail goodDetail)
 	{
 		List<String> listUrl = null;
 		
 		if (goodDetail.getImageList() != null)
 		{
 			listUrl = new ArrayList<String>();
 			String b = (goodDetail.getImageList().getResize180()).substring(1, (goodDetail.getImageList().getResize180()).length()-1);
 			b = Communication.replace(b);
 			String[] c = b.split(",");
 			for(int i=0;i<c.length;i++) 
 			{
 				listUrl.add(c[i]);
 			}
 		}
 		
 		return listUrl;
 	}
 }
