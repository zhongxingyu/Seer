 //liuchong@baixing.com
 package com.baixing.view.vad;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.jivesoftware.smack.util.StringUtils;
 
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.ViewParent;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.baixing.adapter.VadImageAdapter;
 import com.baixing.data.GlobalDataManager;
 import com.baixing.entity.Ad;
 import com.baixing.entity.Ad.EDATAKEYS;
 import com.baixing.imageCache.ImageCacheManager;
 import com.baixing.imageCache.ImageLoaderManager;
 import com.baixing.util.Communication;
 import com.baixing.util.TextUtil;
 import com.baixing.widget.HorizontalListView;
 import com.quanleimu.activity.R;
 
 public class VadPageController implements OnTouchListener, VadImageAdapter.IImageProvider, View.OnClickListener{
 	
 	public static interface ActionCallback {
 		//Pull
 		public int totalPages();
 		public Ad getAd(int pos);
 		public boolean hasMore();
 		
 		//Push
 		public void onPageInitDone(ViewPager pager, final int pageIndex);
 		public void onLoadMore();
 		public void onPageSwitchTo(int pos);
 		public void onRequestBigPic(int pos, Ad detail);
 		public void onRequestMap();
 		public void onRequestUserAd(int userId, String userNick);
 	}
 
 	private static final String[] INVISIBLE_META = new String[] {"价格", "地点", "地区", "查看", "来自", "具体地点", "分类", "发布人"};
 
 	private WeakReference<View> viewRoot;
 	private Ad detail;
 	private ActionCallback callback;
 	private List<View> pages = new ArrayList<View>();
 	private WeakReference<View> loadingMorePage;
 	private int originalIndex;
 	
 	public VadPageController(View rootView, Ad currentAd, ActionCallback actionCallback, int originalSelect) {
 		this.viewRoot = new WeakReference<View>(rootView);
 		this.detail = currentAd;
 		this.callback = actionCallback;
 		this.originalIndex = originalSelect;
 		
 		initPage();
 	}
 	
 	private void initPage() {
 		View v = viewRoot.get();
 		if (v == null) {
 			return;
 		}
 		
 		final ViewPager vp = (ViewPager) v.findViewById(R.id.svDetail);
         vp.setAdapter(new PagerAdapter() {
 			
 			public Object instantiateItem(View arg0, int position) 
 			{
 				View detail = getNewPage(LayoutInflater.from(arg0.getContext()), position);//LayoutInflater.from(vp.getContext()).inflate(R.layout.gooddetailcontent, null);
 				
 				detail.setTag(R.id.accountEt, detail);
 				((ViewPager) arg0).addView(detail, 0);
 				if (position == callback.totalPages())
 				{
 					updateLoadingPage(detail, true, false);
 					loadingMorePage = new WeakReference(detail);
 					callback.onLoadMore();
 				}
 				else
 				{
 					Ad detaiObj = callback.getAd(position);//;
 					initContent(detail, detaiObj, position, ((ViewPager) arg0), false);
 				}
 				return detail;
 			}
 			
             public void destroyItem(View arg0, int index, Object arg2)
             {
                 ((ViewPager) arg0).removeView((View) arg2);
                 
                 final Integer pos = (Integer) ((View) arg2).getTag();
                 if (pos < callback.totalPages())
                 {
 //                	Log.d("imagecount", "imagecount, destroyItem: " + pos + "  " + mListLoader.getGoodsList().getData().get(pos).toString());
                 	List<String> listUrl = getImageUrls(callback.getAd(pos));
                 	if(null != listUrl && listUrl.size() > 0){
                 		ImageLoaderManager.getInstance().Cancel(listUrl);
 	            		for(int i = 0; i < listUrl.size(); ++ i){
 	            			decreaseImageCount(listUrl.get(i), pos);
 	            		}
                 	}
                 }
                 removePage(pos);
                 
                 
             }
 
 			public boolean isViewFromObject(View arg0, Object arg1) {
 				return arg0 == arg1;
 			}
 			
 			public int getCount() {
 				return callback.totalPages() + (callback.hasMore() ? 1 : 0);
 			}
 		});
 //        if(mCurIndex == 0) return v;
         vp.setCurrentItem(originalIndex);
         vp.setOnPageChangeListener(new OnPageChangeListener() {
 			private int currentPage = 0;
 			public void onPageSelected(int pos) {
 				currentPage = pos;
 				if (pos != callback.totalPages())
 				{
 					detail = callback.getAd(pos);
 				}
 				callback.onPageSwitchTo(pos);
 			}
 			
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				currentPage = arg0;
 			}
 			
 			public void onPageScrollStateChanged(int arg0) {
 				if(arg0 != ViewPager.SCROLL_STATE_IDLE) return;
 				
 				List<String>listUrl = getImageUrls(detail);
 				if(listUrl == null || listUrl.size() == 0){
 					ViewGroup currentVG = (ViewGroup)getPage(currentPage);
 					if(currentVG != null){
 						View noimage = currentVG.findViewById(R.id.vad_no_img_tip);
 						if(noimage != null){
 							noimage.setVisibility(View.VISIBLE);
 						}
 						View detail = currentVG.findViewById(R.id.glDetail);
 						if(detail != null){
 							detail.setVisibility(View.GONE);
 						}
 					}
 				}
 				else{
 					ViewGroup currentVG = (ViewGroup)getPage(currentPage);
 					if(currentVG != null){
 						HorizontalListView glDetail = (HorizontalListView) currentVG.findViewById(R.id.glDetail);
 						VadImageAdapter adapter = (VadImageAdapter)glDetail.getAdapter();
 						if(adapter != null){
 							List<String> curLists = adapter.getImages();
 							boolean sameList = true;
 							if(curLists != null && curLists.size() == listUrl.size()){
 								for(int i = 0; i < curLists.size(); ++ i){
 									String cstr = curLists.get(i);
 									String lstr = listUrl.get(i);
 									if(cstr != null && cstr.length() > 0 && lstr != null && lstr.length() > 0){
 										if(!cstr.equals(lstr)){
 											sameList = false;
 											break;
 										}
 									}
 								}
 							}else{
 								sameList = false;
 							}
 							if(!sameList){
 								adapter.setContent(listUrl);
 								adapter.notifyDataSetChanged();
 							}
 						}else{
 							glDetail.setAdapter(new VadImageAdapter(currentVG.getContext(), listUrl, currentPage, VadPageController.this));
 						}
 	//					
 						glDetail.setOnTouchListener(VadPageController.this);
 						
 						glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 		
 							@Override
 							public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 								callback.onRequestBigPic(arg2, detail);
 							}
 						});
 					}
 				}				
 			}
 		});
 
        
         vp.setOnTouchListener(new OnTouchListener(){
 			@Override
 			public boolean onTouch(View arg0, MotionEvent event) {
 	                return false;  
 			}
         	
         } );
 	}
 	
 	private View getPage(int index){
 		for(int i = 0; i < pages.size(); ++ i){
 			if(pages.get(i).getTag() != null && (Integer)pages.get(i).getTag() == index){
 				return pages.get(i);
 			}
 		}
 		return null;
 	}
 	
 	private View getNewPage(LayoutInflater layoutInflater, int index){
 		for(int i = 0; i < pages.size(); ++ i){
 			if(pages.get(i).getTag() == null){
 				pages.get(i).setTag(index);
 				ViewParent parent = pages.get(i).getParent();
 				if(parent != null){
 					if(parent instanceof ViewGroup){
 						((ViewGroup)parent).removeView(pages.get(i));
 					}else{
 						break;
 					}
 				}
 				return pages.get(i);
 			}
 		}
 		View detail = layoutInflater.inflate(R.layout.gooddetailcontent, null);
 		detail.setTag(index);
 		pages.add(detail);
 		return detail;
 	}
 	
 	private void removePage(int index){
 		for(int i = 0; i < pages.size(); ++ i){
 			if(pages.get(i) != null && pages.get(i).getTag() != null && (Integer)pages.get(i).getTag() == index){
 				HorizontalListView glDetail = (HorizontalListView) pages.get(i).findViewById(R.id.glDetail);
 //				glDetail.setVisibility(View.GONE);
 				glDetail.setAdapter(null);
 				pages.get(i).setTag(null);
 				if(pages.get(i) instanceof ScrollView){
 					((ScrollView)pages.get(i)).scrollTo(0, 0);
 				}
 			}
 		}
 	}
 
 	public boolean onTouch (View v, MotionEvent event){
 		View detailV = viewRoot.get() == null ? null : viewRoot.get().findViewById(R.id.svDetail);
 	    switch (event.getAction()) {
 	    case MotionEvent.ACTION_DOWN:
 	    case MotionEvent.ACTION_MOVE: 
 	    	if(detailV != null){
 	    		((ViewPager)detailV).requestDisallowInterceptTouchEvent(true);
 	    	}
 	        break;
 	    case MotionEvent.ACTION_OUTSIDE:
 	    case MotionEvent.ACTION_UP:
 	    	if(detailV != null){
 	    		((ViewPager)detailV).requestDisallowInterceptTouchEvent(false);
 	    	}
 	        break;		
 	    }
 		return true;
 	}
 	
 	private void initContent(View contentView, final Ad detail, final int pageIndex, ViewPager pager, boolean useRoot)
 	{
 		if (detail == null) {
 			return;
 		}
 		
 		if(useRoot)
 			contentView = contentView.getRootView();
 		
 		RelativeLayout llgl = (RelativeLayout) contentView.findViewById(R.id.llgl);
 		
 //		if(detail.getImageList() != null){
 			List<String>listUrl = getImageUrls(detail);
 			
 			llgl = (RelativeLayout) contentView.findViewById(R.id.llgl);
 			int cur = pager != null ? pager.getCurrentItem() : -1;
 			if(listUrl == null || listUrl.size() == 0){
 //				llgl.setVisibility(View.GONE);
 				if(pageIndex == originalIndex || pageIndex == cur){
 					llgl.findViewById(R.id.vad_no_img_tip).setVisibility(View.VISIBLE);
 				}else{
 					llgl.findViewById(R.id.vad_no_img_tip).setVisibility(View.GONE);
 				}
 				llgl.findViewById(R.id.glDetail).setVisibility(View.GONE);
 				
 			}else{
 				llgl.findViewById(R.id.vad_no_img_tip).setVisibility(View.GONE);
 				llgl.findViewById(R.id.glDetail).setVisibility(View.VISIBLE);
 				HorizontalListView glDetail = (HorizontalListView) contentView.findViewById(R.id.glDetail);
 				if(pageIndex == originalIndex || pageIndex == cur){
 					glDetail.setAdapter(new VadImageAdapter(llgl.getContext(), listUrl, pageIndex, this));
 					glDetail.setOnTouchListener(this);
 					glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 	
 						@Override
 						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 							callback.onRequestBigPic(arg2, detail);
 						}
 					});
 				}
 			}
 
 		TextView txt_tittle = (TextView) contentView.findViewById(R.id.goods_tittle);
 		TextView txt_message1 = (TextView) contentView.findViewById(R.id.sendmess1);
 		TextView txt_user = (TextView) contentView.findViewById(R.id.user_info);
 		this.setMetaObject(contentView, detail);
 		
 		String title = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_TITLE);
 		String description = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_DESCRIPTION);
 		final String userNick = TextUtils.isEmpty(detail.getValueByKey("userNick")) ? "匿名" : detail.getValueByKey("userNick");
		String userInfo ="发布人：" + userNick + "(" + detail.getMetaValueByKey("发布人") + ")";
 		
 		if ((title == null || title.length() == 0) && description != null)
 		{
 			title = description.length() > 40 ? description.substring(0, 40) : description;
 		}
 		
 		description += "\n打电话给我时，请一定说明在百姓网看到的，谢谢！";
 		description = appendPostFromInfo(detail, description);
 		description = appendExtralMetaInfo(detail, description);
 		
 		txt_message1.setText(description);
 		txt_tittle.setText(title);
 		txt_user.setText(userInfo);
 		
 		
 		txt_user.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				int userId = -1;
 				
 				try {
 					userId = Integer.parseInt(detail.getValueByKey("userId"));
 				}
 				catch (Throwable t) {
 					//Ignor
 				}
 				if (userId != -1) {
 					callback.onRequestUserAd(userId, userNick);
 				}
 			}
 		});
 		
 		callback.onPageInitDone(pager, pageIndex);
 	}
 
 	public void loadMoreFail() {
 		final View page = loadingMorePage == null ? null : (View) loadingMorePage.get();
 		if (page != null)
 		{
 			page.findViewById(R.id.retry_load_more).setOnClickListener(this);
 			page.postDelayed(new Runnable() {
 				@Override
 				public void run() {
 					updateLoadingPage(page, false, true);
 				}
 				
 			}, 10);
 		}
 	}
 	
 	public void loadMoreSucced() {
 		final View page = loadingMorePage == null ? null : (View) loadingMorePage.get();
 		if (page != null)
 		{
 			page.postDelayed(new Runnable() {
 
 				@Override
 				public void run() {
 					updateLoadingPage(page, false, false);
 					final Integer tag = (Integer)page.getTag();
 					if(tag != null){
 						initContent(page, callback.getAd(tag.intValue()), tag.intValue(), null, false);
 					}
 				}
 				
 			}, 10);
 		}
 	}
 	
 	private void updateLoadingPage(View page, boolean loading, boolean retry) {
 		page.findViewById(R.id.loading_more_progress_parent).setVisibility(loading ? View.VISIBLE : View.GONE);
 		page.findViewById(R.id.retry_more_parent).setVisibility(retry ? View.VISIBLE : View.GONE);
 		page.findViewById(R.id.llDetail).setVisibility(loading || retry ? View.GONE : View.VISIBLE);
 	}
 	
 	
 	public void resetLoadingPage(boolean hasMore) {
 		PagerAdapter adapter = getContentPageAdapter();
 		if (adapter != null)
 		{
 			adapter.notifyDataSetChanged();
 		}
 		View rootView = viewRoot.get();
 		if (rootView == null)
 		{
 			return;
 		}
 		
 		View page = loadingMorePage == null ? null : loadingMorePage.get();
 		final ViewPager vp = (ViewPager) rootView.findViewById(R.id.svDetail);
 		if (!hasMore && page != null && vp != null)
 		{
 			vp.removeView(page);
 		}
 	}
 	
 	
 	
 	private static List<String> getImageUrls(Ad goodDetail)
 	{
 		List<String> listUrl = null;
 		
 		if (goodDetail.getImageList() != null)
 		{
 			listUrl = new ArrayList<String>();
 			String b = (goodDetail.getImageList().getResize180());//.substring(1, (goodDetail.getImageList().getResize180()).length()-1);
 			if(b == null) return listUrl;
 			b = Communication.replace(b);
 			String[] c = b.split(",");
 			for(int i=0;i<c.length;i++) 
 			{
 				listUrl.add(c[i]);
 			}
 		}
 		
 		return listUrl;
 	}
 	
 	private boolean isVisible(String metaKey) {
 		for (String key : INVISIBLE_META) {
 			if (metaKey.startsWith(key)) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	private String appendExtralMetaInfo(Ad detail, String description)
 	{
 		if (detail == null)
 		{
 			return description;
 		}
 		
 		StringBuffer extralInfo = new StringBuffer();
 		ArrayList<String> allMeta = detail.getMetaData();
 		for (String meta : allMeta)
 		{
 			if (isVisible(meta))
 			{
 				final int splitIndex = meta.indexOf(" ");
 				if (splitIndex != -1)
 				{
 					extralInfo.append(meta.substring(splitIndex).trim()).append("，");
 				}
 			}
 		}
 		
 		if (extralInfo.length() > 0)
 		{
 			extralInfo.deleteCharAt(extralInfo.length() -1 );
 			return extralInfo.append("\n\n").append(description).toString(); 
 		}
 		
 		return description;
 	}
 	
 	private String appendPostFromInfo(Ad detail, String description)
 	{
 		if (detail == null)
 		{
 			return description;
 		}
 		
 		String postFrom = detail.getValueByKey("postMethod");
 		if ("api_mobile_android".equals(postFrom))
 		{
 			return description + "\n来自android客户端";
 		}
 		else if ("baixing_ios".equalsIgnoreCase(postFrom))
 		{
 			return description + "\n来自iPhone客户端";
 		}
 		
 		return description;
 	}
 	
 	@Override
 	public void onShowView(ImageView imageView, String url, String previousUrl, final int index) {
 		ImageLoaderManager.getInstance().showImg(imageView, url, previousUrl, imageView.getContext());
 		increaseImageCount(url, index);
 	}
 	
 	private HashMap<String, List<Integer> > imageMap = new HashMap<String, List<Integer> >();
 	private void increaseImageCount(String url, int pos){
 		if(url == null) return;
 		if(imageMap.containsKey(url)){
 			List<Integer> values = imageMap.get(url);
 			for(int i = 0; i < values.size(); ++ i){
 				if(values.get(i) ==  pos){
 					return;
 				}
 			}
 			values.add(pos);
 			imageMap.put(url, values);
 		}else{
 			List<Integer> value = new ArrayList<Integer>();
 			value.add(pos);
 			imageMap.put(url, value);
 		}
 	}
 	
 	private void decreaseImageCount(String url, int pos){
 		if(url == null) return;
 		if(imageMap.containsKey(url)){
 			List<Integer> values = imageMap.get(url);
 			for(int i = 0; i < values.size(); ++ i){
 				if(values.get(i) == pos){
 					values.remove(i);
 					break;
 				}
 			}
 			if(values.size() == 0){
 				ImageCacheManager.getInstance().forceRecycle(url, true);
 				imageMap.remove(url);
 			}else{
 				imageMap.put(url, values);
 			}
 		}
 	}
 	
 	private void setMetaObject(View currentPage, Ad detail){
 		LinearLayout ll_meta = (LinearLayout) currentPage.findViewById(R.id.meta);
 		if(ll_meta == null) return;
 		ll_meta.removeAllViews();
 		
 		LayoutInflater inflater = LayoutInflater.from(currentPage.getContext());
 		
 		String price = detail.getValueByKey(EDATAKEYS.EDATAKEYS_PRICE);
 		if (price != null && !"".equals(price))
 		{
 			View item = createMetaView(inflater, R.drawable.vad_icon_price, "价格:", price, null);
 			ll_meta.addView(item);
 			((TextView) item.findViewById(R.id.tvmeta)).setTextColor(currentPage.getResources().getColor(R.color.vad_meta_price));
 		}
 		
 		
 		
 		String area = detail.getValueByKey(EDATAKEYS.EDATAKEYS_AREANAME);
 		String address = detail.getMetaValueByKey("具体地点");
 		if (address != null && address.trim().length() > 0)
 		{
 			area = address;
 		}
 		
 		View areaV = createMetaView(inflater, R.drawable.vad_icon_location, "地区:", area, new View.OnClickListener() {
 			public void onClick(View v) {
 				callback.onRequestMap();
 			}
 		});
 		ll_meta.addView(areaV);
 	}
 	
 	
 	
 	private View createMetaView(LayoutInflater inflater, int iconRes, String label, String value, View.OnClickListener clickListener)
 	{
 		View v = inflater.inflate(R.layout.item_meta, null);
 		
 		TextView tvmetatxt = (TextView) v.findViewById(R.id.tvmetatxt);
 		TextView tvmeta = (TextView) v.findViewById(R.id.tvmeta);
 		ImageView icon = (ImageView) v.findViewById(R.id.meta_icon);
 		
 		if (iconRes != -1 && icon != null) {
 			icon.setImageResource(iconRes);
 			icon.setVisibility(View.VISIBLE);
 			tvmetatxt.setVisibility(View.GONE);
 		}
 		
 		tvmetatxt.setText(label);
 		tvmeta.setText(value);
 		
 		if (clickListener != null)
 		{
 			v.findViewById(R.id.action_indicator_img).setVisibility(View.VISIBLE);
 			v.setOnClickListener(clickListener);
 		}
 		else
 		{
 			v.findViewById(R.id.action_indicator_img).setVisibility(View.INVISIBLE);
 		}
 		
 		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
 		if (layoutParams== null)  layoutParams =  new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 		layoutParams.height = (int) v.getResources().getDimension(R.dimen.vad_meta_item_height);
 		v.setLayoutParams(layoutParams);
 		
 		return v;
 	}
 	
 	private PagerAdapter getContentPageAdapter()
 	{
 		View root = viewRoot.get(); 
 		if (root == null)
 		{
 			return null;
 		}
 		
 		final ViewPager vp = (ViewPager) root.findViewById(R.id.svDetail);
 		return vp == null ? null : vp.getAdapter();
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()) {
 		case R.id.retry_load_more:
 			retryLoadMore();
 			break;
 		}
 	}
 	
 	private void retryLoadMore()
 	{
 		//We assume that this action always on UI thread.
 		final View page = loadingMorePage == null ? null : (View) loadingMorePage.get();
 		if (page != null)
 		{
 			updateLoadingPage(page, true, false);
 		}
 		
 		callback.onLoadMore();
 	}
 }
