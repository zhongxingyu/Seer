 package com.quanleimu.view.fragment;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Stack;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.BaseFragment.TabDef;
 import com.quanleimu.activity.BaseFragment.TitleDef;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.BXLocation;
 import com.quanleimu.entity.CityDetail;
 import com.quanleimu.jsonutil.LocateJsonData;
 import com.quanleimu.util.Helper;
 
 public class CityChangeFragment extends BaseFragment  implements QuanleimuApplication.onLocationFetchedListener, View.OnClickListener {
 	// 定义控件名
 	public ScrollView parentView;
 	
 	public EditText searchField;
 	public LinearLayout linearListInfo;
 	public LinearLayout linearProvinces;
 	private RelativeLayout relativeProvinces;
 	public ImageView ivGPSChoose;
 	
 	public String cityName = "";
 	public String cityEnglishName = "";
 	
 	public String backPageName = "返回";
 	public String title = "选择城市";
 	public List<String> listCityName = new ArrayList<String>();
 	public List<CityDetail> listHotCity = new ArrayList<CityDetail>();
 	
 	protected class chooseStage extends Object {
 		public View effectiveView;
 		public String titleString;
 		public String backString;
 	};
 	protected Stack<chooseStage> stackStage = new Stack<chooseStage>();
 	protected View activeView;
 	
 	
 	
 	public void initTitle(TitleDef title){
 		title.m_visible = true;
		title.m_leftActionHint = "返回";//backPageName;
		title.m_title = "选择城市";//this.title;		
 	}
 	
 	public void initTab(TabDef tab){
 		tab.m_visible = false;
 	}
 	
 	@Override
 	public boolean handleBack(){
 		if(stackStage.size() == 0){
 			if (null == cityName || "".equals(cityName))
 			{
 				Builder builder = new AlertDialog.Builder(getActivity());
 				builder.setTitle(R.string.dialog_title_info)
 					.setMessage(R.string.dialog_message_confirm_exit)
 					.setNegativeButton(R.string.no, null)
 					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() 
 				{
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						System.exit(0);
 					}
 				});
 				builder.create().show();
 				return true;
 			}
 			
 			return false;
 		}else{
 			parentView.removeView(activeView);
 			
 			chooseStage stage = stackStage.pop();
 			activeView = stage.effectiveView;
 			parentView.addView(activeView);
 			backPageName = stage.backString;
 			title = stage.titleString;
 			
 			TitleDef title = getTitleDef();
 			title.m_leftActionHint = "返回";
 			title.m_title = this.title;
 			this.refreshHeader();
 			
 			if (stackStage.size() == 0)
 				searchField.setVisibility(View.VISIBLE);
 		}
 		return true;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.citychange, null);
 		
 		// 获取热门城市列表数据
 		listHotCity = LocateJsonData.hotCityList();
 		QuanleimuApplication.getApplication().setListHotCity(listHotCity);
  
 		cityName = QuanleimuApplication.getApplication().getCityName();
 		
 		// 通过或ID获取控件
 		parentView = (ScrollView)rootView.findViewById(R.id.llParentView);
 		
 		linearListInfo = (LinearLayout) rootView.findViewById(R.id.linearList);
 		activeView = linearListInfo;
 
 		TextView tvGPSCityName = (TextView) rootView.findViewById(R.id.tvGPSCityName);
 		tvGPSCityName.setText("定位中...");
 		
 		ivGPSChoose = (ImageView) rootView.findViewById(R.id.ivGPSChoose);
 		ivGPSChoose.setVisibility(View.INVISIBLE);
 		
 		final LinearLayout linearHotCities = (LinearLayout)rootView.findViewById(R.id.linearHotCities); 
 		for (int i = 0; i < listHotCity.size(); i++) {
 			CityDetail city = listHotCity.get(i);
 			
 			View v = null;
 			v = inflater.inflate(R.layout.item_citychange, null);
 
 			TextView tvCityName = (TextView) v.findViewById(R.id.tvCateName);
 			ImageView ivChoose = (ImageView) v.findViewById(R.id.ivChoose);
 			ivChoose.setImageResource(R.drawable.gou);
 
 			tvCityName.setText(city.getName());
 			ivChoose.setVisibility(View.INVISIBLE);
 			v.setTag(city);
 			v.setOnClickListener(this);
 			if (i == listHotCity.size() - 1) {
 				v.findViewById(R.id.citychange_border).setVisibility(View.GONE);
 			}
 			linearHotCities.addView(v);
 		}
 
 		for (int i = 0; i < listHotCity.size(); i++) {
 			if (cityName.equals(listHotCity.get(i).getName())) {
 				linearHotCities.getChildAt(i).findViewById(R.id.ivChoose).setVisibility(View.VISIBLE);
 			} else {
 				linearHotCities.getChildAt(i).findViewById(R.id.ivChoose).setVisibility(View.GONE);
 			}
 		}
 		
 		// other cities
 		((RelativeLayout)rootView.findViewById(R.id.linear2Other)).setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				searchField.setVisibility(View.GONE);
 				
 				parentView.scrollTo(0, 0);
 				
 				chooseStage stage = new chooseStage();
 				stage.effectiveView = activeView;
 				stage.titleString = title;
 				stage.backString = backPageName;
 				stackStage.push(stage);
 				parentView.removeView(activeView);
 				
 //				backPageName = "选择城市";
 				title = "选择省份";
 				TitleDef titleDef = getTitleDef();
 				titleDef.m_leftActionHint = backPageName;
 				titleDef.m_title = CityChangeFragment.this.title;
 				refreshHeader();				
 				
 				if(null == linearProvinces){
 					LayoutInflater inflater = LayoutInflater.from(getActivity());
 					relativeProvinces = (RelativeLayout)inflater.inflate(R.layout.citylist, null);
 					linearProvinces = (LinearLayout)relativeProvinces.findViewById(R.id.llcitylist);
 				
 					initShengMap();
 					
 					HashMap<String,  List<CityDetail>> shengMap = QuanleimuApplication.getApplication().getShengMap();
 					String[] shengArray= shengMap.keySet().toArray(new String[0]);
 					for (int i = 0; i < shengMap.size(); i++) {
 						// 添加新的视图，循环添加到ScrollView中
 						View vTemp = null;
 						vTemp = inflater.inflate(R.layout.item_citychange, null);
 						
 						TextView tvCityName = (TextView) vTemp.findViewById(R.id.tvCateName);
 						ImageView ivChoose = (ImageView) vTemp.findViewById(R.id.ivChoose);
 						ivChoose.setImageResource(R.drawable.arrow);
 						tvCityName.setText(shengArray[i]);
 
 						// 设置标志位
 						vTemp.setTag(shengArray[i]);
 						vTemp.setOnClickListener(new View.OnClickListener() {
 
 							@Override
 							public void onClick(View v) {								
 								parentView.scrollTo(0, 0);
 								
 								chooseStage stage = new chooseStage();
 								stage.effectiveView = activeView;
 								stage.titleString = title;
 								stage.backString = backPageName;
 								stackStage.push(stage);
 								parentView.removeView(activeView);
 								
 								title = "选择城市";
 								
 								TitleDef title = getTitleDef();
 								title.m_leftActionHint = backPageName;
 								title.m_title = CityChangeFragment.this.title;
 								refreshHeader();
 								
 								LayoutInflater inflater = LayoutInflater.from(getActivity());
 								RelativeLayout relativeCitys = (RelativeLayout)inflater.inflate(R.layout.citylist, null);
 								LinearLayout linearCities = (LinearLayout)relativeCitys.findViewById(R.id.llcitylist);
 																
 								String province = v.getTag().toString();
 								final List<CityDetail> list2Sheng = QuanleimuApplication.getApplication().getShengMap().get(province);
 								for (int i = 0; i < list2Sheng.size(); i++) {
 									CityDetail city = list2Sheng.get(i);
 									// 添加新的视图，循环添加到ScrollView中
 									View vCity = null;
 									vCity = inflater.inflate(R.layout.item_citychange, null);
 
 									TextView tvCityName = (TextView) vCity.findViewById(R.id.tvCateName);
 									tvCityName.setText(city.getName());
 									
 									ImageView ivChoose = (ImageView) vCity.findViewById(R.id.ivChoose);
 									ivChoose.setVisibility(View.GONE);						
 
 									// 设置标志位
 									vCity.setTag(city);
 									vCity.setOnClickListener(CityChangeFragment.this);
 									linearCities.addView(vCity);
 								}
 								
 								activeView = relativeCitys;
 								parentView.addView(activeView);
 							}
 						});
 						
 						linearProvinces.addView(vTemp);
 					}					
 				}
 				
 				activeView = relativeProvinces;
 				parentView.addView(activeView);
 			}
 		});
 		
 		
 		// filter cities by search
 		searchField = (EditText) ( rootView.findViewById(R.id.etSearchCity) );
 		parentView.findViewById(R.id.filteredList).setVisibility(View.GONE);
 		searchField.addTextChangedListener(new TextWatcher(){
 
 			@Override
 			public void afterTextChanged(Editable s) 
 			{
 				ViewGroup filteredList = (ViewGroup)parentView.findViewById(R.id.filteredList);
 				View unfilteredList = parentView.findViewById(R.id.unfilteredList);
 				String filterKeyword = s.toString().trim();
 				filteredList.removeAllViews();
 				
 				if (filterKeyword.length() == 0) 
 				{
 					unfilteredList.setVisibility(View.VISIBLE);
 					filteredList.setVisibility(View.GONE);
 				}
 				else
 				{
 					unfilteredList.setVisibility(View.GONE);
 					filteredList.setVisibility(View.VISIBLE);
 					
 					LayoutInflater inflater = LayoutInflater.from(getActivity());					
 					List<CityDetail> filteredCityDetails = getFilteredCityDetails(filterKeyword);
 					for (int i = 0; i < filteredCityDetails.size(); i++)
 					{
 						CityDetail city = filteredCityDetails.get(i);
 						
 						View v = null;
 						v = inflater.inflate(R.layout.item_citychange, null);
 	
 						TextView tvCityName = (TextView) v.findViewById(R.id.tvCateName);
 						ImageView ivChoose = (ImageView) v.findViewById(R.id.ivChoose);
 						ivChoose.setImageResource(R.drawable.gou);
 						tvCityName.setText(city.getName());
 						ivChoose.setVisibility(View.INVISIBLE);
 						v.setTag(city);
 						v.setOnClickListener(CityChangeFragment.this);
 						filteredList.addView(v);						
 					}
 				}
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) 
 			{
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) 
 			{
 			}
 			
 		});
 		QuanleimuApplication.getApplication().getCurrentLocation(this);
 		
 		return rootView;
 	}
 	
 	@Override
 	public void onLocationFetched(BXLocation location) {
 		if(null == location || !location.geocoded)
 			return;
 		
 		if (!cityName.equals(location.cityName)) {
 			ivGPSChoose.setVisibility(View.INVISIBLE);
 		} else {
 			ivGPSChoose.setVisibility(View.VISIBLE);
 		}
 
 		final View rootView = getView();
 		if (rootView != null)
 		{
 			TextView tvGPSCityName = (TextView) rootView.findViewById(R.id.tvGPSCityName);
 			if(null == tvGPSCityName) return;
 			tvGPSCityName.setText(location.cityName);
 			
 			ivGPSChoose.setVisibility(View.VISIBLE);
 
 			RelativeLayout linearGpsCity = (RelativeLayout)rootView.findViewById(R.id.linearGpsCityItem);
 
 			linearGpsCity.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 
 					for(int i=0;i<QuanleimuApplication.getApplication().getListCityDetails().size();i++){
 						if(((TextView) rootView.findViewById(R.id.tvGPSCityName)).getText().toString().equals(QuanleimuApplication.getApplication().getListCityDetails().get(i).getName()))
 						{
 							QuanleimuApplication.getApplication().setCityEnglishName(QuanleimuApplication.getApplication().getListCityDetails().get(i).getEnglishName());
 							break;
 						}
 					}
 					QuanleimuApplication.getApplication().setCityName(((TextView) rootView.findViewById(R.id.tvGPSCityName)).getText().toString());
 					Helper.saveDataToLocate(getActivity(), "cityName", ((TextView) rootView.findViewById(R.id.tvGPSCityName)).getText().toString());
 					
 					finishFragment();
 				}
 			});	
 		}
 		
 	}
 
 	/**
 	 * 
 	 */
 	private static void initShengMap() {
 		if(null == QuanleimuApplication.getApplication().getShengMap() ||
 				QuanleimuApplication.getApplication().getShengMap().size() == 0){
 			List<String> listShengName = new ArrayList<String>();
 			HashMap<String, List<CityDetail>> shengMap = new HashMap<String, List<CityDetail>>();
 			
 			List<CityDetail> cityDetails = QuanleimuApplication.getApplication().getListCityDetails();
 			// 获取所有省份列表
 			for (int i = 0; i < cityDetails.size(); i++) {
 				String sheng = cityDetails.get(i).getSheng();
 				if (!(sheng.equals("直辖市"))) {
 					if (listShengName == null || listShengName.size() == 0) {
 						listShengName.add(sheng);
 					} else {
 						if (!listShengName.contains(sheng)) {
 							listShengName.add(sheng);
 						}
 					}
 				}
 			}
 			
 			// 将对应城市添加到对应的省里面去 shengMap
 			for (int j = 0; j < listShengName.size(); j++) {
 				List<CityDetail> listCD = new ArrayList<CityDetail>();
 				for (int i = 0; i < cityDetails.size(); i++) {
 					if (cityDetails.get(i).getSheng()
 							.equals(listShengName.get(j))) {
 						listCD.add(cityDetails.get(i));
 					}
 				}
 				shengMap.put(listShengName.get(j), listCD);
 			}
 	
 			QuanleimuApplication.getApplication().setShengMap(shengMap);
 		}
 	}
 	
 	private static boolean checkContainsShortPinyin(String englishName, String shortPinyin)
 	{
 		int shortPos = 0;
 		for (int i = 0; i < englishName.length(); i++)
 		{
 			if (englishName.charAt(i) == shortPinyin.charAt(shortPos))
 			{
 				if (++shortPos >= shortPinyin.length())
 					return true;
 			}
 		}
 		return false;
 	}
 	
 	private List<CityDetail> getFilteredCityDetails (String filterKeyword)
 	{
 		List<CityDetail> allCities = QuanleimuApplication.getApplication().getListCityDetails();
 		List<CityDetail> filteredCities = new ArrayList<CityDetail>(16);
 		List<CityDetail> shortFilteredCities = new ArrayList<CityDetail>(8);
 		for (CityDetail city : allCities)
 		{
 			if (city.name.startsWith(filterKeyword) || city.englishName.startsWith(filterKeyword))
 			{
 				filteredCities.add(city);
 				continue;
 			}
 			
 			if (filterKeyword.length() < 6)
 			{
 				if (checkContainsShortPinyin(city.englishName, filterKeyword))
 				{
 					shortFilteredCities.add(city);
 				}
 			}
 		}
 		
 		filteredCities.addAll(shortFilteredCities);
 		
 		return filteredCities;
 	}
 
 	@Override
 	public void onClick(View v) {
 		CityDetail city = (CityDetail) v.getTag();
 		if (city.getClass().equals(CityDetail.class))
 		{
 			QuanleimuApplication.getApplication().setCityEnglishName(city.getEnglishName());
 			QuanleimuApplication.getApplication().setCityName(city.getName());		
 			Helper.saveDataToLocate(getActivity(), "cityName", city.getName());		
 
 			this.finishFragment();
 		}
 	}
 	
 }
