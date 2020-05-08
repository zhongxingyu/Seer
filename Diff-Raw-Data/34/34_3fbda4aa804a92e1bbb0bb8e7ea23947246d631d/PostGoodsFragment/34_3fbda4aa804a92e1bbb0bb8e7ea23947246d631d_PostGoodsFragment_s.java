 package com.baixing.view.fragment;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.text.InputFilter;
 import android.text.InputType;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.baixing.activity.BaseActivity;
 import com.baixing.activity.BaseFragment;
 import com.baixing.activity.QuanleimuApplication;
 import com.baixing.broadcast.CommonIntentAction;
 import com.baixing.entity.BXLocation;
 import com.baixing.entity.GoodsDetail;
 import com.baixing.entity.GoodsDetail.EDATAKEYS;
 import com.baixing.entity.PostGoodsBean;
 import com.baixing.entity.UserBean;
 import com.baixing.imageCache.SimpleImageLoader;
 import com.baixing.jsonutil.JsonUtil;
 import com.baixing.tracking.Tracker;
 import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
 import com.baixing.tracking.TrackConfig.TrackMobile.Key;
 import com.baixing.tracking.TrackConfig.TrackMobile.PV;
 import com.baixing.util.Communication;
 import com.baixing.util.LocationService;
 import com.baixing.util.LocationService.BXRgcListener;
 import com.baixing.widget.ImageSelectionDialog;
 import com.baixing.util.Util;
 import com.baixing.widget.CustomDialogBuilder;
 import com.quanleimu.activity.R;
 
 public class PostGoodsFragment extends BaseFragment implements BXRgcListener, OnClickListener, QuanleimuApplication.onLocationFetchedListener{
 	private static final int MSG_GETLOCATION_TIMEOUT = 8;
 	private static final int VALUE_LOGIN_SUCCEEDED = 9;
 	private static final int MSG_GEOCODING_FETCHED = 0x00010010;
 	private static final int MSG_GEOCODING_TIMEOUT = 0x00010011;
 	static final int HASH_POST_BEAN = "postBean".hashCode();
 	static final int HASH_CONTROL = "control".hashCode();
 	static final private int MSG_MORE_DETAIL_BACK = 0xF0000001;
 	static final public String KEY_INIT_CATEGORY = "cateNames";
 	static final String KEY_LAST_POST_CONTACT_USER = "lastPostContactIsRegisteredUser";
 	static final String KEY_IS_EDITPOST = "isEditPost"; 
 	static final String KEY_CATE_ENGLISHNAME = "cateEnglishName";
 	static final private String KEY_IMG_BUNDLE = "key_image_bundle";
 	static final private String STRING_DETAIL_POSITION = "具体地点";
 	static final private String STRING_AREA = "地区";
 	static final private String FILE_LAST_CATEGORY = "lastCategory";
 	static final private String STRING_DESCRIPTION = "description";
 	static final int MSG_POST_SUCCEED = 0xF0000010; 
 
 	private String categoryEnglishName = "";
 	private String categoryName = "";
 	private String json = "";
 	private LinearLayout layout_txt;
 	private LinkedHashMap<String, PostGoodsBean> postList;		//发布模板每一项的集合
 	private static final int NONE = 0;
 	private static final int PHOTORESOULT = 3;
 	private static final int MSG_CATEGORY_SEL_BACK = 11;
 	private static final int MSG_DIALOG_BACK_WITH_DATA = 12;
 	private PostParamsHolder params;
 	private PostParamsHolder originParams;
 	private String mobile, password;
 	private UserBean user;
 	private GoodsDetail goodsDetail;
 	private static boolean isPost = true;
 	private ArrayList<String> listUrl;
 	private Bundle imgSelBundle = null;
 	private ImageSelectionDialog imgSelDlg = null;	
 	private View locationView = null;
 	private BXLocation detailLocation = null;
     private BXLocation cacheLocation = null;
     private List<String> bmpUrls = new ArrayList<String>();
     private EditText etDescription = null;
     private EditText etContact = null;
     
     @Override
 	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
 		if (resultCode == NONE) {
 			return;
 		}
 		
 		FragmentManager fm = getActivity().getSupportFragmentManager();
 
 		Fragment fg = fm.getFragment(this.imgSelBundle, "imageFragment");
 		if(fg != null && (fg instanceof ImageSelectionDialog)){
 			this.imgSelDlg = (ImageSelectionDialog)fg;
 		}
 		if(this.imgSelDlg != null &&
 				(requestCode == CommonIntentAction.PhotoReqCode.PHOTOHRAPH
 				|| requestCode == CommonIntentAction.PhotoReqCode.PHOTOZOOM
 				|| requestCode == PHOTORESOULT)){
 			imgSelDlg.setMsgOutHandler(handler);
 			if(imgSelBundle == null){
 				imgSelBundle = new Bundle();
 			}
 			imgSelDlg.setMsgOutBundle(this.imgSelBundle);
 			imgSelDlg.onActivityResult(requestCode, resultCode, data);
 		}
     }
     
     private static final String []texts = {"物品交易", "车辆买卖", "房屋租售", "全职招聘", 
 		   "兼职招聘", "求职简历", "交友活动", "宠物", 
 		   "生活服务", "教育培训"};
     
     private void initWithCategoryNames(String categoryNames) {
     	if(categoryNames == null || categoryNames.length() == 0){
 			categoryNames = (String)Util.loadDataFromLocate(this.getActivity(), FILE_LAST_CATEGORY, String.class);
 		}
 		if(categoryNames != null && !categoryNames.equals("")){
 			String[] names = categoryNames.split(",");
 			if(names.length == 2){
 				this.categoryEnglishName = names[0];
 				this.categoryName = names[1];
 				
 			}else if(names.length == 1){
 				this.categoryEnglishName = names[0];
 			}
 			Util.saveDataToLocate(this.getActivity(), FILE_LAST_CATEGORY, categoryNames);
 		}
     }
     
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		String categoryNames = this.getArguments().getString(KEY_INIT_CATEGORY);
 		initWithCategoryNames(categoryNames);
 				
 		this.goodsDetail = (GoodsDetail) getArguments().getSerializable("goodsDetail");
 		postList = new LinkedHashMap<String, PostGoodsBean>();		
 		params = new PostParamsHolder();
 		originParams = new PostParamsHolder();		
 		listUrl = new ArrayList<String>();
 
 		if (savedInstanceState != null)
 		{
 			postList.putAll( (HashMap<String, PostGoodsBean>)savedInstanceState.getSerializable("postList"));
 			params = (PostParamsHolder) savedInstanceState.getSerializable("params");
 			listUrl.addAll((List<String>) savedInstanceState.getSerializable("listUrl"));
 			imgHeight = savedInstanceState.getInt("imgHeight");
 			this.imgSelBundle = savedInstanceState.getBundle(KEY_IMG_BUNDLE);
 		}
 		
 		if(imgSelBundle == null){
 			imgSelBundle =  new Bundle();
 		}
 
 		if(imgSelDlg == null){
 			imgSelDlg = new ImageSelectionDialog(imgSelBundle);
 			imgSelDlg.setMsgOutHandler(handler);
 		}
 		
 		user = Util.getCurrentUser();//(UserBean) Util.loadDataFromLocate(this.getActivity(), "user", UserBean.class);
 		if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
 			mobile = user.getPhone();
 			password = user.getPassword();
 		}
 		String appPhone = QuanleimuApplication.getApplication().getPhoneNumber();
 		if(goodsDetail == null && (appPhone == null || appPhone.length() == 0)){
 			QuanleimuApplication.getApplication().setPhoneNumber(mobile);
 		}
 	}
 		
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		extractInputData(layout_txt, params);
 		synchronized(this){
 			outState.putSerializable("params", params);
 			outState.putSerializable("postList", postList);
 			outState.putSerializable("listUrl", listUrl);
 			outState.putInt("imgHeight", imgHeight);
 			outState.putBundle(KEY_IMG_BUNDLE, imgSelBundle);
 		}
 	}
 
 	@Override
 	public boolean handleBack() {
 		if(imgSelDlg != null)
 			if(imgSelDlg.handleBack()){
 				return true;
 		}		
 		return super.handleBack();
 	}	
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		inreverse = false;
 		QuanleimuApplication.getApplication().addLocationListener(this);
 		if (goodsDetail!=null) {//edit
 			this.pv = PV.EDITPOST;
 			Tracker.getInstance()
 			.pv(this.pv)
 			.append(Key.SECONDCATENAME, categoryEnglishName)
 			.append(Key.ADID, goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
 			.end();
 		}
 		else {//new post
 			this.pv = PV.POST;
 			Tracker.getInstance()
 			.pv(this.pv)
 			.append(Key.SECONDCATENAME, categoryEnglishName)
 			.end();
 		}		
 	}
 	
 	public void onPause() {
 		QuanleimuApplication.getApplication().removeLocationListener(this);		
 		extractInputData(layout_txt, params);
 		setPhoneAndAddress();
 		super.onPause();
 	}
 
 	@Override
 	public void onStackTop(boolean isBack) {
 		if(isBack){
 			final ScrollView scroll = (ScrollView) this.getView().findViewById(R.id.goodscontent);
 			scroll.post(new Runnable() {            
 			    @Override
 			    public void run() {
 			           scroll.fullScroll(View.FOCUS_DOWN);              
 			    }
 			});
 		}
 	}	
 
 	@Override
 	public void onViewCreated (View view, Bundle savedInstanceState){
 		super.onViewCreated(view, savedInstanceState);
 		showPost();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {		
 		ViewGroup v = (ViewGroup) inflater.inflate(R.layout.postgoodsview, null);		
 		layout_txt = (LinearLayout) v.findViewById(R.id.layout_txt);		
 		Button button = (Button) v.findViewById(R.id.iv_post_finish);
 		button.setOnClickListener(this);
 		if (goodsDetail == null)
 			button.setText("立即免费发布");
 		else
 			button.setText("立即更新信息");
 		return v;
 	}
 	
 	private static String getDisplayValue(PostGoodsBean bean, GoodsDetail detail, String detailKey){
 		if(bean == null || detail == null || detailKey == null || detailKey.equals(""))return "";
 		String value = detail.getValueByKey(detailKey);
 		String displayValue = "";
 		if(bean.getControlType().equals("input") || bean.getControlType().equals("textarea")){
 			displayValue = detail.getValueByKey(detailKey);
 			if(displayValue != null && !bean.getUnit().equals("")){
 				int pos = displayValue.indexOf(bean.getUnit());
 				if(pos != -1){
 					displayValue = displayValue.substring(0, pos);
 				}
 			}
 			return displayValue;
 		}
 		else if(bean.getControlType().equals("select") || bean.getControlType().equals("checkbox")){
 			List<String> beanVs = bean.getValues();
 			if(beanVs != null){
 				for(int t = 0; t < beanVs.size(); ++ t){
 					if(bean.getControlType().equals("checkbox") && bean.getLabels() != null && bean.getLabels().size() > 1){
 						if(value.contains(beanVs.get(t))){
 							displayValue += (displayValue.equals("") ? "" : ",") + bean.getLabels().get(t);
 							continue;
 						}
 					}
 					if(beanVs.get(t).equals(value)){
 						displayValue = bean.getLabels().get(t);
 						break;
 					}
 				}
 			}
 			if(displayValue.equals("")){
 				String _sValue = detail.getValueByKey(detailKey + "_s"); 
 				if(_sValue != null && !_sValue.equals("")){
 					return _sValue;
 				}
 			}
 		}
 		return displayValue;
 	}
 	
 	private void startImgSelDlg(ImageSelectionDialog.ImageContainer[] container){
 		if(container != null){
 			imgSelBundle.putSerializable(ImageSelectionDialog.KEY_IMG_CONTAINER, container);
 		}
 		imgSelDlg.setMsgOutBundle(imgSelBundle);
 		imgSelDlg.show(getFragmentManager(), null);
 	}
 	
 	private void editpostUI() {
 		if(goodsDetail == null) return;
 		for(int i = 0; i < layout_txt.getChildCount(); ++ i){
 			View v = layout_txt.getChildAt(i);
 			PostGoodsBean bean = (PostGoodsBean)v.getTag(HASH_POST_BEAN);
 			if(bean == null) continue;
 			String detailValue = goodsDetail.getValueByKey(bean.getName());
 			if(detailValue == null || detailValue.equals(""))continue;
 			String displayValue = getDisplayValue(bean, goodsDetail, bean.getName());
 			View control = (View)v.getTag(HASH_CONTROL);
 			if(control instanceof CheckBox){
 				if(displayValue.contains(((CheckBox)control).getText())){
 					((CheckBox)control).setChecked(true);
 				}
 				else{
 					((CheckBox)control).setChecked(false);
 				}
 			}else if(control instanceof TextView){
 				((TextView)control).setText(displayValue);
 			}
 			this.params.put(bean.getDisplayName(), 
 					displayValue, 
 					detailValue,
 					bean.getName());			
 		
 			if(bean.getDisplayName().equals(STRING_AREA)){
 				String strArea = goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
 				String[] areas = strArea.split(",");
 				if(areas.length >= 2){
 					if(control instanceof TextView){
 						((TextView)control).setText(areas[areas.length - 1]);
 					}
 					if(bean.getValues() != null && bean.getLabels() != null){
 						List<String> areaLabels = bean.getLabels();
 						for(int t = 0; t < areaLabels.size(); ++ t){
 							if(areaLabels.get(t).equals(areas[1])){
 //								postMap.put("地区", bean.getValues().get(t));
 								params.getData().put(STRING_AREA, bean.getValues().get(t));
 //								params.put("地区", areas[areas.length - 1], bean.getValues().get(t));
 								break;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		if (goodsDetail.getImageList() != null) {
 			String b = (goodsDetail.getImageList().getResize180());
 //					.substring(1, (goodsDetail.getImageList()
 //							.getResize180()).length() - 1);
 			if(b == null || b.equals("")) return;
 			b = Communication.replace(b);
 			if (b.contains(",")) {
 				String[] c = b.split(",");
 				for (int k = 0; k < c.length; k++) {
 					listUrl.add(c[k]);
 				}
 			}else{
 				listUrl.add(b);
 			}
 			
 			if(listUrl.size() > 0){
 				SimpleImageLoader.showImg(layout_txt.findViewById(R.id.myImg), listUrl.get(0), "", getActivity());
 				((TextView)layout_txt.findViewById(R.id.imgCout)).setText(String.valueOf(listUrl.size()));
 				layout_txt.findViewById(R.id.imgCout).setVisibility(View.VISIBLE);
 			}else{
 				layout_txt.findViewById(R.id.imgCout).setVisibility(View.INVISIBLE);
 			}
 			
 			String big = (goodsDetail.getImageList().getBig());
 			if(big != null && big.length() > 0){
 				big = Communication.replace(big);
 				String[] cbig = big.split(",");
 				for(int i = 0; i < cbig.length; ++ i){
 					this.bmpUrls.add(cbig[i]);
 				}
 			}
 		}
 	}
 	
 	private boolean inPosting = false;
 	
 	private void doPost(boolean registered, BXLocation location){
 		if(inPosting) return;
 //		showSimpleProgress();
 		showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
 		new Thread(new UpdateThread(registered, location)).start();		
 	}
 	private boolean usercheck() {
 		return (user != null && user.getPhone() != null && !user.getPhone().equals(""));
 	}
 		
 	private void deployDefaultLayout(){
 		addCategoryItem();
 		if(getView() != null){
 			View imgV = getView().findViewById(R.id.myImg);
 			if(imgV != null){
 				imgV.setOnClickListener(this);
 			}
 			
 			View descriptionV = getView().findViewById(R.id.description_input);
 			if (descriptionV != null) {
 				descriptionV.setOnTouchListener(new OnTouchListener() {
 					@Override
 					public boolean onTouch(View v, MotionEvent event) {
 						if (event.getAction() == MotionEvent.ACTION_DOWN) {
 							Tracker.getInstance().event((goodsDetail==null)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, STRING_DESCRIPTION).end();
 						}
 						return false;
 					}
 				});
 			}
 			
 			View textArea = getView().findViewById(R.id.img_description);
 			if(textArea != null){
 				textArea.setOnClickListener(this);
 			}
 			
 			PostGoodsBean bean = new PostGoodsBean();
 			bean.setControlType("input");
 			bean.setDisplayName(fixedItemDisplayNames[1]);
 			bean.setName(fixedItemNames[1]);
 
 			textArea.setTag(HASH_CONTROL, descriptionV);
 			textArea.setTag(HASH_POST_BEAN, bean);
 
 		}
 		LayoutInflater inflater = LayoutInflater.from(getActivity());
 		for(int i = 1; i < fixedItemNames.length; ++ i){
 			if(fixedItemNames[i].equals(STRING_DESCRIPTION)){//上面已经特殊处理了description
 				continue;
 			}
 			View v = fixedItemDisplayNames[i].equals(STRING_DETAIL_POSITION) ? 
 					inflater.inflate(R.layout.item_post_location, null) : 
 						inflater.inflate(R.layout.item_post_edit, null);	
 			((TextView)v.findViewById(R.id.postshow)).setText(fixedItemDisplayNames[i]);
 			EditText text = (EditText)v.findViewById(R.id.postinput);
 			final String fixedItemDisplayName = fixedItemDisplayNames[i];
 			text.setOnTouchListener(new OnTouchListener() {
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					if (event.getAction()==MotionEvent.ACTION_DOWN) {
 						//goodsDetail==null decide post or editpost
 						Tracker.getInstance().event(goodsDetail==null?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, fixedItemDisplayName).end();
 					}
 					return false;
 				}
 			});
 			
 			PostGoodsBean bean = new PostGoodsBean();
 			bean.setControlType("input");
 			bean.setDisplayName(fixedItemDisplayNames[i]);
 			bean.setName(fixedItemNames[i]);
 
 			v.setTag(HASH_CONTROL, text);
 			v.setTag(HASH_POST_BEAN, bean);					
 			
 			if(fixedItemNames[i].equals("价格")){
 				text.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
 			}else if(fixedItemNames[i].equals("contact")) {
 				String phone = QuanleimuApplication.getApplication().getPhoneNumber();
 				text.setInputType(InputType.TYPE_CLASS_PHONE);
 				text.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
 				if(this.goodsDetail != null){
 					text.setText(goodsDetail.getValueByKey(EDATAKEYS.EDATAKEYS_CONTACT));
 				}else{	
 					if(phone != null && phone.length() > 0){
 						text.setText(phone);
 					}
 				}
 			}else if(fixedItemNames[i].equals(STRING_DETAIL_POSITION)){
 				v.findViewById(R.id.location).setOnClickListener(this);
 				locationView = v;
 			}
 			LinearLayout.LayoutParams layoutParams = (LayoutParams) v.getLayoutParams();
 			if (layoutParams == null)
 				layoutParams = new LinearLayout.LayoutParams(
 				     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
 			layoutParams.bottomMargin = v.getContext().getResources().getDimensionPixelOffset(R.dimen.post_marginbottom);
 			if(!fixedItemDisplayNames[i].equals(STRING_DETAIL_POSITION)){
 				layoutParams.height = getResources().getDimensionPixelOffset(R.dimen.post_item_height);
 			}
 			v.setLayoutParams(layoutParams);
 			layout_txt.addView(v);
 		}
 		loadCachedData();
 	}
 	
 	public void updateNewCategoryLayout(String cateNames){
 		if(cateNames == null) return;
 		String[] names = cateNames.split(",");
 		if(names != null){
 			if(categoryEnglishName.equals(names[0])) return;
 		}
 		initWithCategoryNames(cateNames);
 		resetData(true);
 		Util.saveDataToLocate(getActivity(), FILE_LAST_CATEGORY, cateNames);
 		this.showPost();
 	}
 	
 	private void showPost(){
 		if(this.categoryEnglishName == null || categoryEnglishName.length() == 0){
 			deployDefaultLayout();
 			return;
 		}
 
 		String cityEnglishName = QuanleimuApplication.getApplication().cityEnglishName;
 		if(goodsDetail != null && goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CITYENGLISHNAME).length() > 0){
 			cityEnglishName = goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CITYENGLISHNAME);
 		}
 		
 		Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(this.getActivity(), categoryEnglishName + cityEnglishName);
 		json = pair.second;
		if (json != null && json.length() > 0) {
			addCategoryItem();
 			if (pair.first + (24 * 3600) < System.currentTimeMillis()/1000) {
 				showSimpleProgress();
 				new Thread(new GetCategoryMetaThread(cityEnglishName)).start();
 			} else {
 				buildPostLayout();
 				loadCachedData();
 			}
 		} else {
 			showSimpleProgress();
 			new Thread(new GetCategoryMetaThread(cityEnglishName)).start();
 		}
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if(v.getId() == R.id.iv_post_finish){
 			postFinish();
 		}else if(v.getId() == R.id.location){
 			Tracker.getInstance().event((goodsDetail==null)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, STRING_DETAIL_POSITION).end();
 			
 			if(this.detailLocation != null && locationView != null){
 				setDetailLocationControl(detailLocation);
 			}else if(detailLocation == null){
 				Toast.makeText(this.getActivity(), "无法获得当前位置", 0).show();
 			}
 		}else if(v.getId() == R.id.myImg){
 			Tracker.getInstance().event((goodsDetail==null)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, "image").end();
 			
 			if(goodsDetail != null){
 				if(this.imgSelBundle.containsKey(ImageSelectionDialog.KEY_IMG_CONTAINER)){
 					startImgSelDlg(null);
 				}else{					
 					ArrayList<String> smalls = new ArrayList<String>();
 					ArrayList<String> bigs = new ArrayList<String>();
 					String big = (goodsDetail.getImageList().getBig());
 					if(big != null && big.length() > 0){
 						big = Communication.replace(big);
 						String[] cbig = big.split(",");
 						for (int j = 0; j < listUrl.size(); j++) {
 							String bigUrl = (cbig == null || cbig.length <= j) ? null : cbig[j];
 							smalls.add(listUrl.get(j));
 							bigs.add(bigUrl);
 						}
 					}
 					if(bigs != null){
 						List<ImageSelectionDialog.ImageContainer> container = new ArrayList<ImageSelectionDialog.ImageContainer>();
 						for(int i = 0; i < bigs.size(); ++ i){
 							ImageSelectionDialog.ImageContainer ic = new ImageSelectionDialog.ImageContainer();
 							ic.bitmapUrl = bigs.get(i);
 							ic.status = ImageSelectionDialog.ImageStatus.ImageStatus_Normal;
 							ic.thumbnailPath = smalls.get(i);
 							container.add(ic);
 						}
 						ImageSelectionDialog.ImageContainer[] ic = new ImageSelectionDialog.ImageContainer[container.size()];
 						for(int i = 0; i < container.size(); ++ i){
 							ic[i] = new ImageSelectionDialog.ImageContainer();
 							ic[i].set(container.get(i));
 						}
 						startImgSelDlg(ic);
 					}
 				}							
 			}else{
 				startImgSelDlg(null);
 			}
 		}else if(v.getId() == R.id.img_description){
 			final View et = v.findViewById(R.id.description_input);
 			if(et != null){
 				et.postDelayed(new Runnable(){
 					@Override
 					public void run(){
 						if (et != null)
 						{
 							Tracker.getInstance().event((goodsDetail==null)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, STRING_DESCRIPTION).end();
 							et.requestFocus();
 							InputMethodManager inputMgr = 
 									(InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 							inputMgr.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
 						}
 					}			
 				}, 100);
 			}
 		}
 	}
 	
 	private void postFinish() {
 		Tracker.getInstance()
 		.event(goodsDetail==null?BxEvent.POST_POSTBTNCONTENTCLICKED:BxEvent.EDITPOST_POSTBTNCONTENTCLICKED)
 		.append(Key.SECONDCATENAME, categoryEnglishName)
 		.end();
 		
 		this.postAction();
 	}
 	
 	private boolean gettingLocationFromBaidu = false;
 	@Override
 	public void handleRightAction(){
 		if (this.getView().findViewById(R.id.goodscontent).isShown())
 		{
 			this.postAction();
 		}
 	}
 	
 	private void setPhoneAndAddress(){
 		String contactDisplayName = "";
 		String addressDisplayName = "";
 		if(postList != null){
 			Collection<PostGoodsBean> beans = postList.values();
 			if(beans != null){
 				Iterator<PostGoodsBean> ite = beans.iterator();
 				while(ite.hasNext()){
 					PostGoodsBean bean = ite.next();
 					if(bean.getName().equals("contact")){
 						contactDisplayName = bean.getDisplayName();
 						if(addressDisplayName.length() > 0){
 							break;
 						}
 					}else if(bean.getName().equals(STRING_DETAIL_POSITION)){
 						addressDisplayName = bean.getDisplayName();
 						if(contactDisplayName.length() > 0){
 							break;
 						}
 					}
 				}
 			}
 		}
 		String phone = params.getData(contactDisplayName);
 		if(phone != null && phone.length() > 0 && goodsDetail == null){
 			QuanleimuApplication.getApplication().setPhoneNumber(phone);
 		}
 		String address = params.getData(addressDisplayName);
 		if(address != null && address.length() > 0){
 			QuanleimuApplication.getApplication().setAddress(address);
 		}
 		
 	}
 	
 	private void postAction() {
 		//定位成功的情况下，发布时保存当前经纬度和地理位置
         if (/*inLocating == false &&*/ locationView != null && cacheLocation != null) {
             String inputAddress = ((TextView)locationView.findViewById(R.id.postinput)).getText().toString();
             BXLocation lastLocation = new BXLocation(cacheLocation);
             lastLocation.detailAddress = inputAddress;
             Util.saveDataToLocate(getActivity(), "lastLocation", lastLocation);
         }
 		extractInputData(layout_txt, params);
 		setPhoneAndAddress();
 		if(!this.checkInputComplete()){
 			return;
 		}
 		String detailLocationValue = params.getUiData(STRING_DETAIL_POSITION);
 		if(this.detailLocation != null && (detailLocationValue == null || detailLocationValue.length() == 0)){
 			doPost(usercheck(), detailLocation);
 		}else{
 			this.sendMessageDelay(MSG_GEOCODING_TIMEOUT, null, 5000);
 			retreiveLocation();
 		}
 	}
 	
 	static void extractInputData(ViewGroup vg, PostParamsHolder params){
 		if(vg == null) return;
 		for(int i = 0; i < vg.getChildCount(); ++ i){
 			PostGoodsBean postGoodsBean = (PostGoodsBean)vg.getChildAt(i).getTag(HASH_POST_BEAN);
 			if(postGoodsBean == null) continue;
 			
 			if (postGoodsBean.getControlType().equals("input") 
 					|| postGoodsBean.getControlType().equals("textarea")) {
 				EditText et = (EditText)vg.getChildAt(i).getTag(HASH_CONTROL);
 				if(et != null){
 //					String displayValue = et.getText().toString();
 //					displayValue = displayValue.endsWith(postGoodsBean.getUnit()) ? 
 					params.put(postGoodsBean.getDisplayName(),  
 							et.getText().toString(), 
 							et.getText().toString(),
 							postGoodsBean.getName());
 				}
 			}
 			else if(postGoodsBean.getControlType().equals("checkbox")){
 				if(postGoodsBean.getValues().size() == 1){
 					CheckBox box = (CheckBox)vg.getChildAt(i).getTag(HASH_CONTROL);
 					if(box != null){
 						if(box.isChecked()){
 							params.put(postGoodsBean.getDisplayName(),//key 
 									postGoodsBean.getValues().get(0), //uivalue
 									postGoodsBean.getValues().get(0),
 									postGoodsBean.getName());//key for ui value
 						}
 						else{
 							params.remove(postGoodsBean.getDisplayName(),postGoodsBean.getName());
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private boolean checkInputComplete() {
 		if(this.categoryEnglishName == null || this.categoryEnglishName.equals("")){
 			Toast.makeText(this.getActivity(), "请选择分类" ,0).show();
 			popupCategorySelectionDialog();
 			return false;
 		}
 		
 		LinkedHashMap<String, String> postMap = params.getData();
 		for (int i = 0; i < postList.size(); i++) {
 			String key = (String) postList.keySet().toArray()[i];
 			PostGoodsBean postGoodsBean = postList.get(key);
 			if (postGoodsBean.getName().equals(STRING_DESCRIPTION) || 
 					(postGoodsBean.getRequired().endsWith("required") && ! this.isHiddenItem(postGoodsBean) && !postGoodsBean.getName().equals(STRING_AREA))) {
 				if(!postMap.containsKey(postGoodsBean.getDisplayName()) 
 						|| postMap.get(postGoodsBean.getDisplayName()).equals("")
 						|| (postGoodsBean.getUnit() != null && postMap.get(postGoodsBean.getDisplayName()).equals(postGoodsBean.getUnit()))){
 					if(postGoodsBean.getName().equals("images"))continue;
 					postResultFail("please entering " + postGoodsBean.getDisplayName() + "!");
 					Toast.makeText(this.getActivity(), "请填写" + postGoodsBean.getDisplayName() + "!", 0).show();
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	private String getFilledLocation(){
 		String toRet = "";
 		for(int m = 0; m < layout_txt.getChildCount(); ++ m){
 			View v = layout_txt.getChildAt(m);
 			PostGoodsBean bean = (PostGoodsBean)v.getTag(HASH_POST_BEAN);
 			if(bean == null) continue;
 			if(bean.getName().equals(STRING_DETAIL_POSITION)){
 				TextView tv = (TextView)v.getTag(HASH_CONTROL);
 				if(tv != null && !tv.getText().toString().equals("")){
 					toRet = tv.getText().toString();
 				}
 				break;
 			}
 		}
 		return toRet;
 	}
 
 	private boolean retreiveLocation(){
 		String city = QuanleimuApplication.getApplication().cityName;
 		String addr = getFilledLocation();
 
 		this.showSimpleProgress();
 		this.gettingLocationFromBaidu = true;
 		return LocationService.getInstance().geocode(addr, city, this);
 	}
 	
 	private Pair<Double, Double> retreiveCoorFromGoogle(){
 		String city = getFilledLocation();
 		if(city == null || city.equals("")){
 			return new Pair<Double, Double>((double)0, (double)0);
 		}
 		String googleUrl = String.format("http://maps.google.com/maps/geo?q=%s&output=csv", city);
 		try{
 			String googleJsn = Communication.getDataByUrlGet(googleUrl);
 			String[] info = googleJsn.split(",");
 			if(info != null && info.length == 4){
 				return new Pair<Double, Double>(Double.parseDouble(info[2]), Double.parseDouble(info[3]));
 			}
 		}catch(UnsupportedEncodingException e){
 			e.printStackTrace();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		return new Pair<Double, Double>((double)0, (double)0);
 	}
 
 	private class UpdateThread implements Runnable {
 		private boolean registered = false;
 		private BXLocation location = null;
 		private UpdateThread(boolean registered, BXLocation location){
 			this.registered = registered;
 			this.location = location;
 		}
 		public void run() {
 			inPosting = true;
 			String apiName = "ad_add";
 			ArrayList<String> list = new ArrayList<String>();
 
 			if(registered){
 				list.add("mobile=" + mobile);
 				list.add("userToken=" + Util.generateUsertoken(password));	
 			}
 			
 			list.add("categoryEnglishName=" + categoryEnglishName);
 			list.add("cityEnglishName=" + QuanleimuApplication.getApplication().cityEnglishName);
 			list.add("rt=1");
 			//根据goodsDetail判断是发布还是修改发布
 			if (goodsDetail != null) {
 				list.add("adId=" + goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 				apiName = "ad_update";
 			}
 						
 			setDistrictByLocation(location);			
 			Pair<Double, Double> coorGoogle = retreiveCoorFromGoogle();
 			list.add("lat=" + coorGoogle.first);
 			list.add("lng=" + coorGoogle.second);
 			
 			LinkedHashMap<String, String> postMap = params.getData();
 			//发布发布集合
 			for (int i = 0; i < postMap.size(); i++) {
 				String key = (String) postMap.keySet().toArray()[i];
 				String values = postMap.get(key);
 				
 				if (values != null && values.length() > 0 && postList.get(key) != null) {
 					try{
 						list.add(URLEncoder.encode(postList.get(key).getName(), "UTF-8")
 								+ "=" + URLEncoder.encode(values, "UTF-8").replaceAll("%7E", "~"));//ugly, replace, what's that? 
 						if(postList.get(key).getName().equals(STRING_DESCRIPTION)){//generate title from description
 							list.add("title"
 									+ "=" + URLEncoder.encode(values.substring(0, Math.min(25, values.length())), "UTF-8").replaceAll("%7E", "~"));
 						}
 					}catch(UnsupportedEncodingException e){
 						e.printStackTrace();
 					}
 				}
 			}
 			//发布图片
 			String images = "";
 			for (int i = 0; i < bmpUrls.size(); i++) {				
 				if(bmpUrls.get(i) != null && bmpUrls.get(i).contains("http:")){
 					images += "," + bmpUrls.get(i);
 				}
 			}
 			if(images != null && images.length() > 0 && images.charAt(0) == ','){
 				images = images.substring(1);
 			}
 			if(images != null && images.length() > 0){
 				list.add("images=" + images);
 			}
 			
 			String errorMsg = "内部错误，发布失败";
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url, true);
 				if (json != null) {
 					JSONObject jsonObject = new JSONObject(json);
 					JSONObject json = jsonObject.getJSONObject("error");
 					code = json.getInt("code");
 					message = replaceTitleToDescription(json.getString("message"));
 					sendMessage(3, null);
 					errorMsg = message;
 				}else {
 					errorMsg = "解析错误";
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 				errorMsg = "解析错误";
 			} catch (Communication.BXHttpException e) {
 				if(e.errorCode == 414){
 					errorMsg = "内容超出规定长度，请修改后重试";
 				}
 				else{
 					errorMsg = replaceTitleToDescription(e.msg);
 				}
 				
 			} catch(Exception e){
 				e.printStackTrace();
 				errorMsg = "发布失败";
 			}
 			
 			if (errorMsg.equals("发布成功"))
 				postResultSuccess();
 			else
 				postResultFail(errorMsg);
 						
 			hideProgress();
 			final String fmsg = errorMsg;
 			if(getActivity() != null){
 				((BaseActivity)getActivity()).runOnUiThread(new Runnable(){
 					@Override
 					public void run(){
 						if(getActivity() != null && fmsg != null){
 							Toast.makeText(getActivity(), fmsg, 0).show();
 						}
 					}
 				});
 			}
 			inPosting = false;
 		}
 	}
 	
 	private int getLineCount() {
 		return etDescription != null ? etDescription.getLineCount() : 1;
 	}
 	
 	private int getDescLength() {
 		return etDescription != null ? etDescription.getText().length() : 0;
 	}
 	
 	private int getContactLength() {
 		return etContact != null ? etContact.getText().length() : 0;
 	}
 	
 	private int getImgCount() {
 		int imgCount = 0;
 		for (int i = 0; i < bmpUrls.size(); i++) {				
 			if(bmpUrls.get(i) != null && bmpUrls.get(i).contains("http:")){
 				imgCount++;
 			}
 		}
 		return imgCount;
 	}
 	
 	private void postResultSuccess() {
 		BxEvent event = goodsDetail != null ? BxEvent.EDITPOST_POSTRESULT : BxEvent.POST_POSTRESULT;
 
 		Tracker.getInstance().event(event)
 		.append(Key.SECONDCATENAME, categoryEnglishName)
 		.append(Key.POSTSTATUS, 1)
 		.append(Key.POSTPICSCOUNT, getImgCount())
 		.append(Key.POSTDESCRIPTIONLINECOUNT, getLineCount())
 		.append(Key.POSTDESCRIPTIONTEXTCOUNT, getDescLength())
 		.append(Key.POSTCONTACTTEXTCOUNT, getContactLength())
 		.append(Key.POSTDETAILPOSITIONAUTO, autoLocated)
         .append(Key.POSTENTRY, QuanleimuApplication.postEntryFlag)
 		.end();
 	}
 	
 	private void postResultFail(String errorMsg) {
 		BxEvent event = goodsDetail != null ? BxEvent.EDITPOST_POSTRESULT : BxEvent.POST_POSTRESULT;
 		Tracker.getInstance().event(event)
 				.append(Key.SECONDCATENAME, categoryEnglishName)
 				.append(Key.POSTSTATUS, 0)
 				.append(Key.POSTFAILREASON, errorMsg)
 				.append(Key.POSTPICSCOUNT, getImgCount())
 				.append(Key.POSTDESCRIPTIONLINECOUNT, getLineCount())
 				.append(Key.POSTDESCRIPTIONTEXTCOUNT, getDescLength())
 				.append(Key.POSTCONTACTTEXTCOUNT, getContactLength())
 				.append(Key.POSTDETAILPOSITIONAUTO, autoLocated)
                 .append(Key.POSTENTRY, QuanleimuApplication.postEntryFlag)
 				.end();
 	}
 	
 	private int code = -1;
 	private String message = "";
 
 	private class GetCategoryMetaThread implements Runnable {
 		private String cityEnglishName = null;
 
 		private GetCategoryMetaThread(String cityEnglishName) {
 			this.cityEnglishName = cityEnglishName;
 		}
 
 		@Override
 		public void run() {
 
 			String apiName = "category_meta_post";
 			ArrayList<String> list = new ArrayList<String>();
 			this.cityEnglishName = (this.cityEnglishName == null ? QuanleimuApplication.getApplication().cityEnglishName : this.cityEnglishName);
 			list.add("categoryEnglishName=" + categoryEnglishName);
 			list.add("cityEnglishName=" + this.cityEnglishName);
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url, false);
 				if (json != null) {
 					postList = JsonUtil.getPostGoodsBean(json);
 					if(postList == null || postList.size() == 0){
 						sendMessage(10, null);
 						return;
 					}
 					// 获取数据成功
 					Activity activity = getActivity();
 					if (activity != null)
 					{
 						//保存模板
 						Util.saveJsonAndTimestampToLocate(activity, categoryEnglishName
 								+ this.cityEnglishName, json, System.currentTimeMillis()/1000);
 //						if (isUpdate) {
 							sendMessage(1, null);
 //						}
 					}
 				} else {
 					// {"error":{"code":0,"message":"\u66f4\u65b0\u4fe1\u606f\u6210\u529f"},"id":"191285466"}
 					sendMessage(2, null);
 				}
 				return;
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				sendMessage(10, null);
 				e.printStackTrace();
 			} catch (Communication.BXHttpException e){
 				
 			}
 			sendMessage(10, null);
 		}
 	}
 		
 	private void loadCachedData()
 	{
 		LinkedHashMap<String, String> uiMap = params.getUiData();
 		if (uiMap == null)
 		{
 			return;
 		}
 		Iterator<String> it = uiMap.keySet().iterator();
 		while (it.hasNext()){
 			String name = it.next();
 			for (int i=0; i<layout_txt.getChildCount(); i++)
 			{
 				View v = layout_txt.getChildAt(i);
 				PostGoodsBean bean = (PostGoodsBean)v.getTag(HASH_POST_BEAN);
 				if(bean == null || 
 						!bean.getName().equals(name)//check display name 
 						) continue;
 				View control = (View)v.getTag(HASH_CONTROL);
 				String displayValue = uiMap.get(name);
 				
 				if(control instanceof CheckBox){
 					if(displayValue.contains(((CheckBox)control).getText())){
 						((CheckBox)control).setChecked(true);
 					}
 					else{
 						((CheckBox)control).setChecked(false);
 					}
 				}else if(control instanceof TextView){
 					((TextView)control).setText(displayValue);
 				}
 			}
 		}
 		
 	}
 	
 	static boolean fetchResultFromViewBack(int message, Object obj, ViewGroup vg, PostParamsHolder params){//??
 		if(vg == null) return false;
 		
 		boolean match = false;
 		for(int i = 0; i < vg.getChildCount(); ++ i){
 			View v = vg.getChildAt(i);
 			PostGoodsBean bean = (PostGoodsBean)v.getTag(HASH_POST_BEAN);
 			if(bean == null) continue;
 			if(bean.getName().hashCode() == message){
 				if(obj instanceof Integer){
 					TextView tv = (TextView)v.getTag(HASH_CONTROL);
 					String txt = bean.getLabels().get((Integer)obj);
 					String txtValue = bean.getValues().get((Integer)obj);
 //					postMap.put(bean.getDisplayName(), txtValue);
 					if(tv != null){
 						tv.setText(txt);
 					}
 					match = true;
 					params.put(bean.getDisplayName(), txt, txtValue, bean.getName());
 				}
 				else if(obj instanceof String){
 					TextView tv = (TextView)v.getTag(HASH_CONTROL);
 					String check = (String)obj;
 					String[] checks = check.split(",");
 					String value = "";
 					String txt = "";
 					for(int t = 0; t < checks.length; ++ t){
 						if(checks[t].equals(""))continue;
 		 				txt += "," + bean.getLabels().get(Integer.parseInt(checks[t]));
 						value += "," + bean.getValues().get(Integer.parseInt(checks[t]));
 					}
 					if(txt.length() > 0){
 						txt = txt.substring(1);
 					}
 					if(value.length() > 0){
 						value = value.substring(1);
 					}
 					if(tv != null){
 //						tv.setWidth(vg.getWidth() * 2 / 3);
 						tv.setText(txt);
 					}
 					match = true;
 					params.put(bean.getDisplayName(), txt, value,bean.getName());
 				}
 				else if(obj instanceof MultiLevelSelectionFragment.MultiLevelItem){
 					TextView tv = (TextView)v.getTag(HASH_CONTROL);
 					if(tv != null){
 //						tv.setWidth(vg.getWidth() * 2 / 3);
 						tv.setText(((MultiLevelSelectionFragment.MultiLevelItem)obj).txt);
 					}
 					match = true;
 					params.put(bean.getDisplayName(), 
 							((MultiLevelSelectionFragment.MultiLevelItem)obj).txt, 
 							((MultiLevelSelectionFragment.MultiLevelItem)obj).id,
 							bean.getName());
 				}
 			}
 		}
 		
 		return match;
 	}
 	
 	private boolean inArray(String item, String [] array){
 		for(int i = 0;i<array.length;i++){
 			if(item.equals(array[i])){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private void clearCategoryParameters(){//keep fixed(common) parameters there
 		LinkedHashMap<String, String> uiData = params.getUiData();
 		LinkedHashMap<String, String> data = params.getData();
 		Object [] uikeys = uiData.keySet().toArray();
 		Object [] datakeys = data.keySet().toArray();
 		
 	    	for(int i = 0; i < uikeys.length; i++)
 	    	{
 	    		String uikey = uikeys[i].toString();
 	    		String datakey = datakeys[i].toString();
 	    		if(!inArray(uikey, this.fixedItemNames) ){
 	    			params.remove(datakey,uikey);
 	    		}
 	    	}
 	}
 	
 	private void resetData(boolean clearImgs){
 		if(this.layout_txt != null){
 			View v = layout_txt.findViewById(R.id.img_description);
 			layout_txt.removeAllViews();
 			layout_txt.addView(v);
 		}
 		postList.clear();
 		
 		
 		if(null != Util.loadDataFromLocate(getActivity(), FILE_LAST_CATEGORY, String.class)){
 			clearCategoryParameters();
 			if(clearImgs){
 				listUrl.clear();
 				this.bmpUrls.clear();
 				if(this.imgSelDlg != null){
 					imgSelDlg.clearResource();
 		//					imgSelDlg = null;
 				}
 				this.imgSelBundle.clear();// = null;
 				
 				layout_txt.findViewById(R.id.imgCout).setVisibility(View.INVISIBLE);
 				
 				params.remove("描述", STRING_DESCRIPTION);
 				params.remove("价格", "价格");
 			}
 		}
 	}
 
 	private void handleBackWithData(int message, Object obj) {
 
 		if(message == PostGoodsFragment.VALUE_LOGIN_SUCCEEDED){
 			this.handleRightAction();
 			return;
 		}else if(message == MSG_CATEGORY_SEL_BACK && obj != null){
 			String[] names = ((String)obj).split(",");
 			if(names.length == 2){
 				if(names[0].equals(this.categoryEnglishName)){
 					return;
 				}
 				this.categoryEnglishName = names[0];
 				this.categoryName = names[1];
 				
 			}else if(names.length == 1){
 				if(names[0].equals(this.categoryEnglishName)){
 					return;
 				}
 				this.categoryEnglishName = names[0];
 			}
 			
 			resetData(false);
 			Util.saveDataToLocate(getActivity(), FILE_LAST_CATEGORY, obj);
 			this.showPost();
 		}
 		boolean match = fetchResultFromViewBack(message, obj, layout_txt, params);
 		if(match){
 //			postMap.put(result.first, result.second);
 			return;
 		}
 		switch(message){
 		case MSG_MORE_DETAIL_BACK:
 			params.merge((PostParamsHolder) obj);
 			break;
 		default:
 			break;
 		}
 	}
 	
 	@Override
 	public void onFragmentBackWithData(int message, Object obj){	
 		handleBackWithData(message, obj);
 	}
 
 	private void appendBeanToLayout(PostGoodsBean postBean)
 	{
 		if (postBean.getName().equals("contact") &&
 			(postBean.getValues() == null || postBean.getValues().isEmpty()) &&
 			(user != null && user.getPhone() != null && user.getPhone().length() > 0))
 		{
 			List<String> valueList = new ArrayList<String>(1);
 			valueList.add(user.getPhone());
 			postBean.setValues(valueList);
 			postBean.setLabels(valueList);
 		}	
 		
 	
 //		Activity activity = getActivity();
 		isPost = (goodsDetail==null);
 		ViewGroup layout = createItemByPostBean(postBean, this);//FIXME:
 
 		if(layout != null && !postBean.getName().equals(STRING_DETAIL_POSITION)){
 			ViewGroup.LayoutParams lp = layout.getLayoutParams();
 			lp.height = getResources().getDimensionPixelOffset(R.dimen.post_item_height);
 			layout.setLayoutParams(lp);
 		}
 
 		if(postBean.getName().equals(STRING_DETAIL_POSITION)){
 			layout.findViewById(R.id.location).setOnClickListener(this);
 			((TextView)layout.findViewById(R.id.postinput)).setHint("请输入");
 			locationView = layout;
 			
 			String address = QuanleimuApplication.getApplication().getAddress();
 			if(address != null && address.length() > 0){
 				((TextView)layout.findViewById(R.id.postinput)).setText(address);
 			}
 		}else if(postBean.getName().equals("contact") && layout != null){
 			etContact = ((EditText)layout.getTag(HASH_CONTROL));
 			etContact.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
 			String phone = QuanleimuApplication.getApplication().getPhoneNumber();
 			if(this.goodsDetail != null){
 				etContact.setText(goodsDetail.getValueByKey(EDATAKEYS.EDATAKEYS_CONTACT));
 			}else{
 				if(phone != null && phone.length() > 0){
 					etContact.setText(phone);
 				}
 			}
 		}
 		else if (postBean.getName().equals(STRING_DESCRIPTION) && layout != null){
 			etDescription = (EditText) layout.getTag(HASH_CONTROL);
 		}
 		
 		if(layout != null){
 			layout_txt.addView(layout);
 		}
 	}
 
 	
 
 	
 	private void popupCategorySelectionDialog(){
 		Bundle bundle = createArguments(null, null);
 		bundle.putSerializable("items", (Serializable) Arrays.asList(texts));
 		bundle.putInt("maxLevel", 1);
 		bundle.putInt(ARG_COMMON_REQ_CODE, MSG_CATEGORY_SEL_BACK);
 		if(categoryEnglishName != null && !categoryEnglishName.equals("") && categoryName != null) {
 			bundle.putString("selectedValue", categoryName);
 		}
 		extractInputData(layout_txt, params);
 		CustomDialogBuilder cdb = new CustomDialogBuilder(getActivity(), PostGoodsFragment.this.getHandler(), bundle);
 		cdb.start();
 	}
 	
 	private void addCategoryItem(){
 		Activity activity = getActivity();
 		if(this.goodsDetail != null)return;
 		if(layout_txt != null){
 			if(layout_txt.findViewById(R.id.arrow_down) != null) return;
 		}
 		LayoutInflater inflater = LayoutInflater.from(activity);
 		View categoryItem = inflater.inflate(R.layout.item_post_select, null);
 		
 		categoryItem.setTag(HASH_CONTROL, categoryItem.findViewById(R.id.posthint));//tag
 		((TextView)categoryItem.findViewById(R.id.postshow)).setText("分类");
 		categoryItem.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				Tracker.getInstance().event(BxEvent.POST_INPUTING).append(Key.ACTION, "类目").end();
 				popupCategorySelectionDialog();
 			}				
 		});//categoryItem.setOnClickListener
 		
 		if(categoryEnglishName != null && !categoryEnglishName.equals("") && categoryName != null){
 			 ((TextView)categoryItem.findViewById(R.id.posthint)).setText(categoryName);
 		}
 		
 		LinearLayout.LayoutParams layoutParams = (LayoutParams) categoryItem.getLayoutParams();
 		if (layoutParams == null)
 			layoutParams = new LinearLayout.LayoutParams(
 			     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
 		layoutParams.bottomMargin = categoryItem.getContext().getResources().getDimensionPixelOffset(R.dimen.post_marginbottom);		
 		layoutParams.height = getResources().getDimensionPixelOffset(R.dimen.post_item_height);
 		categoryItem.setLayoutParams(layoutParams);
 		
 		layout_txt.addView(categoryItem);
 	}
 	
 	private String[] fixedItemNames = {"images", STRING_DESCRIPTION, "价格", "contact", STRING_DETAIL_POSITION};
 	private String[] fixedItemDisplayNames = {"", "描述", "价格", "联系电话", STRING_DETAIL_POSITION};
 	private String[] hiddenItemNames = {"wanted", "faburen"};
 	private boolean autoLocated;
 
 	
 	private void buildFixedPostLayout(){
 		if(this.postList == null || this.postList.size() == 0) return;
 		
 		HashMap<String, PostGoodsBean> pm = new HashMap<String, PostGoodsBean>();
 		Object[] postListKeySetArray = postList.keySet().toArray();
 		for(int i = 0; i < postList.size(); ++ i){
 			for(int j = 0; j < fixedItemNames.length; ++ j){
 				PostGoodsBean bean = postList.get(postListKeySetArray[i]);
 				if(bean.getName().equals(fixedItemNames[j])){					
 					pm.put(fixedItemNames[j], bean);
 					break;
 				}
 			}
 		}
 		
 		if(pm.containsKey(STRING_DESCRIPTION)){
 			PostGoodsBean bean = pm.get(STRING_DESCRIPTION);
 			if(bean != null){
 				View v = layout_txt.findViewById(R.id.img_description);
 				EditText text = (EditText)v.findViewById(R.id.description_input);
 				text.setText("");
 				text.setOnTouchListener(new OnTouchListener() {
 					@Override
 					public boolean onTouch(View v, MotionEvent event) {
 						if (event.getAction() == MotionEvent.ACTION_DOWN) {
 							Tracker.getInstance().event((goodsDetail==null)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, STRING_DESCRIPTION).end();
 						}
 						return false;
 					}
 				});
 
 				text.setHint("请输入" + bean.getDisplayName());
 
 				v.setTag(HASH_POST_BEAN, bean);
 				v.setTag(HASH_CONTROL, text);
 				v.setOnClickListener(this);
 //				TextView tv = (TextView)layout_txt.findViewById(R.id.description);
 //				tv.setText(bean.getDisplayName());
 				
 				v.findViewById(R.id.myImg).setOnClickListener(this);
 				((ImageView)v.findViewById(R.id.myImg)).setImageResource(R.drawable.btn_add_picture);
 				if(imgSelBundle != null){
 		    		Object[] container = (Object[])imgSelBundle.getSerializable(ImageSelectionDialog.KEY_IMG_CONTAINER);
 					if(container != null && container.length > 0
 							&& ((ImageSelectionDialog.ImageContainer)container[0]).status == ImageSelectionDialog.ImageStatus.ImageStatus_Normal){
 						Bitmap bp = ImageSelectionDialog.getThumbnailWithPath(((ImageSelectionDialog.ImageContainer)container[0]).thumbnailPath);
 						if(bp != null){
 							((ImageView)v.findViewById(R.id.myImg)).setImageBitmap(bp);
 							((TextView)v.findViewById(R.id.imgCout)).setVisibility(View.VISIBLE);
 							int count = 0;
 							for(int i = 0; i < container.length; ++ i){
 								if(((ImageSelectionDialog.ImageContainer)container[i]).status
 										== ImageSelectionDialog.ImageStatus.ImageStatus_Unset){
 									break;
 								}
 								++ count;
 							}
 							((TextView)v.findViewById(R.id.imgCout)).setText(String.valueOf(count));
 						}
 					}
 				}				
 			}			
 		}
 		
 		for(int i = 0; i < fixedItemNames.length; ++ i){
 			if(pm.containsKey(fixedItemNames[i]) && !fixedItemNames[i].equals(STRING_DESCRIPTION)){
 				this.appendBeanToLayout(pm.get(fixedItemNames[i]));
 			}
 		}
 	}
 	
 	private boolean isFixedItem(PostGoodsBean bean){
 		for(int i = 0; i < fixedItemNames.length; ++ i){
 			if(bean.getName().equals(fixedItemNames[i])) return true;
 		}
 		return false;
 	}
 	
 	private void addHiddenItemsToParams()
 	{
 		if (postList == null || postList.isEmpty())
 			return ;
 		Set<String> keySet = postList.keySet();
 		for (String key : keySet)
 		{
 			PostGoodsBean bean = postList.get(key);
 			for (int i = 0; i<  hiddenItemNames.length; i++)
 			{
 				if (bean.getName().equals(hiddenItemNames[i]))
 				{
 					String defaultValue = bean.getDefaultValue();
 					if (defaultValue != null && defaultValue.length() > 0) {
 						//String key, String uiValue, String data
 						this.params.put(bean.getDisplayName(), 
 								defaultValue,
 								defaultValue,
 								bean.getName());
 					} else {
 						this.params.put(bean.getDisplayName(), 
 								bean.getLabels().get(0), 
 								bean.getValues().get(0),
 								bean.getName());
 					}
 					break;
 				}
 			}
 		}
 	}
 	
 	private boolean isHiddenItem(PostGoodsBean bean)
 	{
 		for (int i = 0; i < hiddenItemNames.length; ++i)
 		{
 			if (bean.getName().equals(hiddenItemNames[i]))
 			{
 				return true;
 			}else if(bean.getName().equals("title")){//特殊处理
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private void buildPostLayout(){
 		this.getView().findViewById(R.id.goodscontent).setVisibility(View.VISIBLE);
 		this.getView().findViewById(R.id.networkErrorView).setVisibility(View.GONE);
 		this.reCreateTitle();
 		this.refreshHeader();
 		if(null == json || json.equals("")) return;
 		if(postList == null || postList.size() == 0){
 			postList = JsonUtil.getPostGoodsBean(json);
 		}
 		buildFixedPostLayout();//添加固定item的layout
 		addHiddenItemsToParams();//params中加入隐藏元素的default值
 		
 		Object[] postListKeySetArray = postList.keySet().toArray();
 		for (int i = 0; i < postList.size(); i++) {
 			String key = (String) postListKeySetArray[i];
 			PostGoodsBean postBean = postList.get(key);
 			
 			if(isFixedItem(postBean) || isHiddenItem(postBean))//排除固定和隐藏元素
 				continue;
 			
 			if(postBean.getName().equals(STRING_AREA)){
 //				this.appendBeanToLayout(postBean);
 				continue;
 			}
 			this.appendBeanToLayout(postBean);//加入元素
 		}
 		editpostUI();
 		originParams.merge(params);
 		extractInputData(layout_txt, originParams);	
 	}//buildPostLayout
 	
 
 
 	@Override
 	protected void handleMessage(Message msg, final Activity activity, View rootView) {
 
 		if(msg.what != MSG_GETLOCATION_TIMEOUT){
 			hideProgress();
 		}
 		
 		switch (msg.what) {
 		case MSG_DIALOG_BACK_WITH_DATA:{
 			Bundle bundle = (Bundle)msg.obj;
 			handleBackWithData(bundle.getInt(ARG_COMMON_REQ_CODE), bundle.getSerializable("lastChoise"));
 			break;
 		}
 		
 		case ImageSelectionDialog.MSG_IMG_SEL_DISMISSED:{
 			if(imgSelBundle != null){
 				ImageSelectionDialog.ImageContainer[] container = 
 						(ImageSelectionDialog.ImageContainer[])imgSelBundle.getSerializable(ImageSelectionDialog.KEY_IMG_CONTAINER);
 //				ArrayList<Bitmap> bps = (ArrayList<Bitmap>)imgSelBundle.getSerializable(ImageSelectionDialog.KEY_CACHED_BPS);
 				if(getView() != null && container != null){
 					ImageView iv = (ImageView)this.getView().findViewById(R.id.myImg);
 					if(iv != null){						
 						if(container != null 
 								&& container.length > 0
 								&& container[0].status == ImageSelectionDialog.ImageStatus.ImageStatus_Normal
 								&& container[0].bitmapPath != null){
 							Bitmap thumbnail = ImageSelectionDialog.getThumbnailWithPath(container[0].thumbnailPath);
 							if(iv != null && thumbnail != null){
 								iv.setImageBitmap(thumbnail);
 							}else{
 								iv.setImageResource(R.drawable.btn_add_picture);
 							}
 						}else{
 							iv.setImageResource(R.drawable.btn_add_picture);
 						}
 					}
 					
 					TextView tv = (TextView)getView().findViewById(R.id.imgCout);
 					if(iv != null){
 						int containerCount = 0;
 						for(int i = 0; i < container.length; ++ i){
 							if(container[i].status == ImageSelectionDialog.ImageStatus.ImageStatus_Unset){
 								break;
 							}else if(container[i].status == ImageSelectionDialog.ImageStatus.ImageStatus_Normal){
 								++ containerCount;
 							}
 						}
 						if(containerCount > 0){
 							tv.setText(String.valueOf(containerCount));
 							tv.setVisibility(View.VISIBLE);
 						}else{
 							tv.setVisibility(View.INVISIBLE);
 						}
 					}
 					
 					bmpUrls.clear();
 					if(container != null){
 						for(int i = 0; i < container.length; ++ i){
 							if(container[i].status == ImageSelectionDialog.ImageStatus.ImageStatus_Normal){
 								bmpUrls.add(container[i].bitmapUrl);
 							}
 						}
 					}
 					
 				}
 			}
 		}
 			break;
 		case -2:{
 			loadCachedData();
 			break;
 		}
 		case 1:
 			addCategoryItem();
 			buildPostLayout();
 			loadCachedData();
 			break;
 
 		case 2:
 			hideProgress();
 			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 			builder.setTitle("提示:")
 					.setMessage(message)
 					.setPositiveButton("确定",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dialog.dismiss();
 								}
 							});
 			builder.create().show();
 			this.getView().findViewById(R.id.goodscontent).setVisibility(View.GONE);
 			this.getView().findViewById(R.id.networkErrorView).setVisibility(View.VISIBLE);
 			this.reCreateTitle();
 			this.refreshHeader();
 
 			break;
 		case 3:
 			try {
 				hideProgress();
 				JSONObject jsonObject = new JSONObject(json);
 				String id;
 				boolean isRegisteredUser = false;
 				try {
 					id = jsonObject.getString("id");
 					isRegisteredUser = jsonObject.getBoolean("contactIsRegisteredUser");
 				} catch (Exception e) {
 					id = "";
 					e.printStackTrace();
 				}
 				JSONObject json = jsonObject.getJSONObject("error");
 				String message = replaceTitleToDescription(json.getString("message"));			
 				if (!id.equals("") && code == 0) {
 					Toast.makeText(activity, message, 0).show();
 					final Bundle args = createArguments(null, null);
 					args.putInt("forceUpdate", 1);
 					// 发布成功
 					// Toast.makeText(PostGoods.this, "未显示，请手动刷新",
 					// 3).show();
 					resetData(goodsDetail == null);
 					
 //					cxt.sendBroadcast(intent);
 
 
 					if(goodsDetail == null){
 						showPost();
 						String lp = getArguments().getString("lastPost");
 						if(lp != null && !lp.equals("")){
 							lp += "," + id;
 						}else{
 							lp = id;
 						}
 						args.putString("lastPost", lp);
 						
 						args.putString("cateEnglishName", categoryEnglishName);
 						args.putBoolean(KEY_IS_EDITPOST, goodsDetail!=null);
 						
 						args.putBoolean(KEY_LAST_POST_CONTACT_USER,  isRegisteredUser);
 //						PostGoodsFragment.this.finishFragment(PostGoodsFragment.MSG_POST_SUCCEED, null);
 						if(activity != null){							
 							args.putInt(PersonalPostFragment.TYPE_KEY, PersonalPostFragment.TYPE_MYPOST);
 							
 							Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_POST_FINISH);
 							intent.putExtras(args);
 							activity.sendBroadcast(intent);
 //							((BaseActivity)activity).pushFragment(new PersonalPostFragment(), args, false);
 						}						
 					}else{
 						PostGoodsFragment.this.finishFragment(PostGoodsFragment.MSG_POST_SUCCEED, null);
 					}
 				}else{
 					if(code == 505){
 						AlertDialog.Builder bd = new AlertDialog.Builder(this.getActivity());
 		                bd.setTitle("")
 		                        .setMessage(message)
 		                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
 		                            @Override
 		                            public void onClick(DialogInterface dialog, int which) {
 		                                dialog.dismiss();
 		        						if(activity != null){
 		        							resetData(true);
 		        							showPost();
 		        							Bundle args = createArguments(null, null);
 		        							args.putInt(PersonalPostFragment.TYPE_KEY, PersonalPostFragment.TYPE_MYPOST);
 //		        							args.putString("505id", id);
 //		        							((BaseActivity)activity).pushFragment(new PersonalPostFragment(), args, false);
 		        							Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_POST_FINISH);
 		        							intent.putExtras(args);
 		        							activity.sendBroadcast(intent);							
 		        						}
 		                            }
 		                        });
 		                AlertDialog alert = bd.create();
 		                alert.show();	
 					}
 				}
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			break;
 		case 4:
 
 			break;
 		case 10:
 			hideProgress();
 			Toast.makeText(activity, "网络连接失败，请检查设置！", 3).show();
 			this.getView().findViewById(R.id.goodscontent).setVisibility(View.GONE);
 			this.getView().findViewById(R.id.networkErrorView).setVisibility(View.VISIBLE);		
 			this.reCreateTitle();
 			this.refreshHeader();
 			break;
 		case MSG_GEOCODING_TIMEOUT:
 		case MSG_GEOCODING_FETCHED:			
 			if(gettingLocationFromBaidu){
 				showSimpleProgress();
 				(new Thread(new UpdateThread(usercheck(), msg.obj == null ? null : (BXLocation)msg.obj))).start();
 				gettingLocationFromBaidu = false;
 			}
 			break;
 		}
 	}
 
 	private String replaceTitleToDescription(String msg) {
 		// replace title to description in message
 		PostGoodsBean titleBean = null, descriptionBean = null;
 		for (String key : postList.keySet()) {
 			PostGoodsBean bean = postList.get(key);
 			if (bean.getName().equals("title")) 
 				titleBean = bean;
 			if (bean.getName().equals(STRING_DESCRIPTION))
 				descriptionBean = bean;
 		}
 		if (titleBean != null && descriptionBean != null)
 			msg = msg.replaceAll(titleBean.getDisplayName(), descriptionBean.getDisplayName());
 		return msg;
 	}
 	
 	
 	private int imgHeight = 0;
 	
 	////to fix stupid system error. all text area will be the same content after app is brought to front when activity not remain is checked
 	private void setInputContent(){		
 		if(layout_txt == null) return;
 		for(int i = 0; i < layout_txt.getChildCount(); ++ i){
 			View v = layout_txt.getChildAt(i);
 			PostGoodsBean bean = (PostGoodsBean)v.getTag(HASH_POST_BEAN);
 			if(bean == null) continue;
 			View control = (View)v.getTag(HASH_CONTROL);
 			if(control != null && control instanceof TextView){
 				if(params != null && params.containsKey(bean.getDisplayName())){
 					String value = params.getUiData(bean.getDisplayName());
 					if(value == null){
 						value = params.getUiData(bean.getName());
 					}
 					if(bean.getName().equals("contact")){
 						if(this.goodsDetail != null){
 							((TextView)control).setText(goodsDetail.getValueByKey(EDATAKEYS.EDATAKEYS_CONTACT));
 						}else{
 							String phone = QuanleimuApplication.getApplication().getPhoneNumber();
 							if(this.goodsDetail != null){
 								((TextView)control).setText(goodsDetail.getValueByKey(EDATAKEYS.EDATAKEYS_CONTACT));
 							}else{
 								if(phone != null && phone.length() > 0){
 									((TextView)control).setText(phone);
 									continue;
 								}
 							}
 						}
 					}
 					((TextView)control).setText(value);
 				}
 			}
 		}
 	}
 	private void updateUserData(){
 		user = Util.getCurrentUser();
 		if(user != null){
 			this.mobile = Util.getCurrentUser().getPhone();
 			this.password = Util.getCurrentUser().getPassword();
 		}
 	}
 	@Override
 	public void onStart(){
 		super.onStart();
 		setInputContent();
 		updateUserData();
 	}
 	
 	@Override
 	public void initTitle(TitleDef title){
 		title.m_visible = true;
 		title.m_title = "免费发布";//(categoryName == null || categoryName.equals("")) ? "发布" : categoryName;
 		if(this.goodsDetail != null){
 			title.m_leftActionHint = "返回";
 		}
 	}
 	
 	static ViewGroup createItemByPostBean(PostGoodsBean postBean, final BaseFragment fragment){
 		ViewGroup layout = null;
 //		if (goodsDetail==null) return true;
 		Activity activity = fragment.getActivity();
 		if (postBean.getControlType().equals("input")) {
 			LayoutInflater inflater = LayoutInflater.from(activity);
 			View v = postBean.getName().equals(STRING_DETAIL_POSITION) ? 
 					inflater.inflate(R.layout.item_post_location, null) : 
 						inflater.inflate(R.layout.item_post_edit, null);
 
 			((TextView)v.findViewById(R.id.postshow)).setText(postBean.getDisplayName());
 
 			EditText text = (EditText)v.findViewById(R.id.postinput);
 			v.setTag(HASH_POST_BEAN, postBean);
 			v.setTag(HASH_CONTROL, text);
 			if(postBean.getNumeric() != 0){
 				text.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
 			}
 			
 			if (postBean.getName().equals("contact")) {
 				text.setInputType(InputType.TYPE_CLASS_PHONE);
 			}
 			
 			if (!postBean.getUnit().equals("")) {
 				((TextView)v.findViewById(R.id.postunit)).setText(postBean.getUnit());
 			}
 			layout = (ViewGroup)v;
 		} else if (postBean.getControlType().equals("select")) {//select的设置
 			LayoutInflater inflater = LayoutInflater.from(activity);
 			View v = inflater.inflate(R.layout.item_post_select, null);	
 			((TextView)v.findViewById(R.id.postshow)).setText(postBean.getDisplayName());
 			v.setTag(HASH_POST_BEAN, postBean);
 			v.setTag(HASH_CONTROL, v.findViewById(R.id.posthint));
 			layout = (ViewGroup)v;
 		}
 		else if (postBean.getControlType().equals("checkbox")) {
 			LayoutInflater inflater = LayoutInflater.from(activity);
 
 			if(postBean.getLabels().size() > 1){
 				View v = inflater.inflate(R.layout.item_post_select, null);
 				((TextView)v.findViewById(R.id.postshow)).setText(postBean.getDisplayName());
 				v.setTag(HASH_POST_BEAN, postBean);
 				v.setTag(HASH_CONTROL, v.findViewById(R.id.posthint));
 				layout = (ViewGroup)v;
 			}
 			else{
 				View v = inflater.inflate(R.layout.item_text_checkbox, null);
 				v.findViewById(R.id.divider).setVisibility(View.GONE);
 				((TextView)v.findViewById(R.id.checktext)).setText(postBean.getDisplayName());
 				v.findViewById(R.id.checkitem).setTag(postBean.getDisplayName());
 				v.setTag(HASH_POST_BEAN, postBean);
 				v.setTag(HASH_CONTROL, v.findViewById(R.id.checkitem));	
 				layout = (ViewGroup)v;				
 			}
 		} else if (postBean.getControlType().equals("textarea")) {
 			LayoutInflater inflater = LayoutInflater.from(activity);
 			View v = inflater.inflate(R.layout.item_post_description, null);
 			((TextView)v.findViewById(R.id.postdescriptionshow)).setText(postBean.getDisplayName());
 
 			EditText descriptionEt = (EditText)v.findViewById(R.id.postdescriptioninput);
 
 			if(postBean.getName().equals(STRING_DESCRIPTION))//description is builtin keyword
 			{
 				String personalMark = QuanleimuApplication.getApplication().getPersonMark();
 				if(personalMark != null && personalMark.length() > 0){
 					personalMark = "\n\n" + personalMark;
 					descriptionEt.setText(personalMark);
 				}
 			}
 			
 			v.setTag(HASH_POST_BEAN, postBean);
 			v.setTag(HASH_CONTROL, descriptionEt);
 			layout = (ViewGroup)v;
 		}//获取到item的layout
 		
 		if (layout == null)
 			return null;
 
 		if(postBean.getControlType().equals("select") || postBean.getControlType().equals("checkbox")){
 			final String actionName = ((PostGoodsBean)layout.getTag(HASH_POST_BEAN)).getDisplayName();
 			layout.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					Tracker.getInstance().event(isPost?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, actionName).end();
 
 					PostGoodsBean postBean = (PostGoodsBean) v.getTag(HASH_POST_BEAN);
 
 					if (postBean.getControlType().equals("select") || postBean.getControlType().equals("tableSelect")) {
 							if(postBean.getLevelCount() > 0){
 									ArrayList<MultiLevelSelectionFragment.MultiLevelItem> items = 
 											new ArrayList<MultiLevelSelectionFragment.MultiLevelItem>();
 									for(int i = 0; i < postBean.getLabels().size(); ++ i){
 										MultiLevelSelectionFragment.MultiLevelItem t = new MultiLevelSelectionFragment.MultiLevelItem();
 										t.txt = postBean.getLabels().get(i);
 										t.id = postBean.getValues().get(i);
 										items.add(t);
 									}
 									Bundle bundle = createArguments(null, null);
 									bundle.putInt(ARG_COMMON_REQ_CODE, postBean.getName().hashCode());
 									bundle.putSerializable("items", items);
 									bundle.putInt("maxLevel", postBean.getLevelCount() - 1);
 									String selectedValue = null;
 									if (fragment instanceof PostGoodsFragment)
 									{
 										PostGoodsFragment postGoodsFragment = (PostGoodsFragment) fragment;
 										selectedValue = postGoodsFragment.params.getData(postBean.getDisplayName());
 									}else if (fragment instanceof FillMoreDetailFragment)
 									{
 										FillMoreDetailFragment fillMoreDetailFragment = (FillMoreDetailFragment) fragment;
 										selectedValue = fillMoreDetailFragment.params.getData(postBean.getDisplayName());
 									}
 									
 									if (selectedValue != null)
 										bundle.putString("selectedValue", selectedValue);
 
 									//以下代码为使用dialog的方式切换
 									extractInputData(((PostGoodsFragment)fragment).layout_txt, ((PostGoodsFragment)fragment).params);
 									CustomDialogBuilder cdb = new CustomDialogBuilder(fragment.getActivity(), fragment.getHandler(), bundle);
 									cdb.start();
 									
 									//以下代码为使用MultiLevelSelectionFragment切换
 //									((BaseActivity)fragment.getActivity()).pushFragment(new MultiLevelSelectionFragment(), bundle, false);
 									
 //								}
 							}//postBean.getLevelCount() > 0
 							else{
 								Bundle bundle = createArguments(postBean.getDisplayName(), null);
 								bundle.putInt(ARG_COMMON_REQ_CODE, postBean.getName().hashCode());
 								bundle.putBoolean("singleSelection", false);
 								bundle.putSerializable("properties",(ArrayList<String>) postBean.getLabels());
 								TextView txview = (TextView)v.getTag(HASH_CONTROL);
 								if (txview !=  null)
 								{
 									bundle.putString("selected", txview.getText().toString());
 								}
 								((BaseActivity)fragment.getActivity()).pushFragment(new OtherPropertiesFragment(), bundle, false);
 							}//postBean.getLevelCount() <= 0
 					}
 					else if(postBean.getControlType().equals("checkbox")){
 						if(postBean.getLabels().size() > 1){
 							Bundle bundle = createArguments(postBean.getDisplayName(), null);
 							bundle.putInt(ARG_COMMON_REQ_CODE, postBean.getName().hashCode());
 							bundle.putBoolean("singleSelection", false);
 							bundle.putSerializable("properties",(ArrayList<String>) postBean.getLabels());
 							TextView txview = (TextView)v.getTag(HASH_CONTROL);
 							if (txview !=  null)
 							{
 								bundle.putString("selected", txview.getText().toString());
 							}
 							((BaseActivity)fragment.getActivity()).pushFragment(new OtherPropertiesFragment(), bundle, false);
 						}
 						else{
 							View checkV = v.findViewById(R.id.checkitem);
 							if(checkV != null && checkV instanceof CheckBox){
 								((CheckBox)checkV).setChecked(!((CheckBox)checkV).isChecked());
 							}
 						}
 					}
 				}
 			});//layout.setOnClickListener:select or checkbox
 		} else {//not select or checkbox
 			final String actionName = ((PostGoodsBean)layout.getTag(HASH_POST_BEAN)).getDisplayName();
 			((View)layout.getTag(HASH_CONTROL)).setOnTouchListener(new OnTouchListener() {
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					if (event.getAction() == MotionEvent.ACTION_DOWN) {
 						Tracker.getInstance().event(isPost?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, actionName).end();
 					}
 					return false;
 				}
 			});
 			
 			layout.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					View ctrl = (View) v.getTag(HASH_CONTROL);
 					ctrl.requestFocus();
 					InputMethodManager inputMgr = 
 							(InputMethodManager) ctrl.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 					inputMgr.showSoftInput(ctrl, InputMethodManager.SHOW_IMPLICIT);
 				}
 			});
 			
 		}
 
 		LinearLayout.LayoutParams layoutParams = (LayoutParams) layout.getLayoutParams();
 		if (layoutParams == null)
 			layoutParams = new LinearLayout.LayoutParams(
 			     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
 		layoutParams.bottomMargin = layout.getContext().getResources().getDimensionPixelOffset(R.dimen.post_marginbottom);
 		layout.setLayoutParams(layoutParams);
 		
 		return layout;
 	}
 	
 	private void setDistrictByLocation(BXLocation location){
 		if(location == null || location.subCityName == null) return;
 		if(this.postList != null && postList.size() > 0){
 			Object[] postListKeySetArray = postList.keySet().toArray();
 			for(int i = 0; i < postList.size(); ++ i){
 				PostGoodsBean bean = postList.get(postListKeySetArray[i]);
 				if(bean.getName().equals(STRING_AREA)){
 					if(bean.getLabels() != null){
 						for(int t = 0; t < bean.getLabels().size(); ++ t){
 							if(location.subCityName.contains(bean.getLabels().get(t))){
 //								((TextView)districtView.findViewById(R.id.posthint)).setText(bean.getLabels().get(t));
 								params.put(bean.getDisplayName(), 
 										bean.getLabels().get(t), 
 										bean.getValues().get(t),
 										bean.getName());
 								originParams.put(bean.getDisplayName(), 
 										bean.getLabels().get(t), 
 										bean.getValues().get(t),
 										bean.getName());
 								return;
 							}
 						}
 					}						
 				}
 			}
 		}		
 	}
 	
 	private void setDetailLocationControl(BXLocation location){
 		if(location == null) return;
 		if(locationView != null && locationView.findViewById(R.id.postinput) != null){
 			String address = (location.detailAddress == null || location.detailAddress.equals("")) ? 
             		((location.subCityName == null || location.subCityName.equals("")) ?
 							"" 
 							: location.subCityName)
 					: location.detailAddress;
             if(address == null || address.length() == 0) return;
             if(location.adminArea != null && location.adminArea.length() > 0){
             	address = address.replaceFirst(location.adminArea, "");
             }
             if(location.cityName != null && location.cityName.length() > 0){
             	address = address.replaceFirst(location.cityName, "");
             }
 			((TextView)locationView.findViewById(R.id.postinput)).setText(address);
 		}		
 	}
 
 
 	@Override
 	public void onLocationFetched(BXLocation location) {
 		// TODO Auto-generated method stub
 	}
 	
 	@Override
 	public void onGeocodedLocationFetched(BXLocation location) {
 		// TODO Auto-generated method stub
 		if(location == null) return;
 		if(handler != null){
 			handler.removeMessages(MSG_GETLOCATION_TIMEOUT);
 		}
 		detailLocation = location;
 	}
 
 	private boolean inreverse = false;
 
 	@Override
 	public void onRgcUpdated(BXLocation location) {
 		if(!this.gettingLocationFromBaidu) return;
 		// TODO Auto-generated method stub
 		if(!inreverse && location != null && (location.subCityName == null || location.subCityName.equals(""))){
 			LocationService.getInstance().reverseGeocode(location.fLat, location.fLon, this);
 			inreverse = true;
 		}else{
 			sendMessage(MSG_GEOCODING_FETCHED, location);
 		}
 	}
 }
