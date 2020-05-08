 package com.quanleimu.view;
 
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.content.Context;
 import java.util.LinkedHashMap;
 import java.util.List;
 import com.quanleimu.activity.BaseActivity;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.PostGoodsBean;
 
 public class FillMoreDetailView extends BaseView {
 
 	private LinkedHashMap<String, PostGoodsBean> beans = null;
 	private List<String> details = null;
 	private LinkedHashMap<String, String> postMap = new LinkedHashMap<String, String>();
 	private int message = -1;
 	private LinearLayout llDetails = null;
 	public FillMoreDetailView(Context context, 
 			LinkedHashMap<String, PostGoodsBean> beans, 
 			List<String> details, 
 			int message, 
 			LinkedHashMap<String, String> existingValues){
 		super(context);
 		this.beans = beans;
 		this.details = details;
 		this.message = message;
 		if(existingValues != null){
 			postMap.putAll(existingValues);
 		}
 		Init(context);
 	}
 	
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_title = "填写更多细节";
 		title.m_leftActionHint = "返回";
 		title.m_rightActionHint = "完成";
 		
 		return title;
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tabDef = new TabDef();
 		tabDef.m_visible = false;
 		
 		return tabDef;
 	}	
 	
 	@Override
 	public void onAttachedToWindow(){
 		super.onAttachedToWindow();
 		if(this.getChildCount() > 0) return;
 		LayoutInflater inflator = LayoutInflater.from(this.getContext());		
 		ViewGroup v = (ViewGroup)inflator.inflate(R.layout.fillmoredetail, null);
 		this.addView(v);
 
 		if(beans == null || details == null) return;
 		llDetails = (LinearLayout)findViewById(R.id.layoutdetails);
 		for(int i = 0; i < details.size(); ++ i){
 			PostGoodsBean bean = beans.get(details.get(i));
 			if(bean == null) continue;
 			ViewGroup layout = PostGoodsView.createItemByPostBean(bean, (BaseActivity)getContext(), m_viewInfoListener);
 			if(layout != null){
 				llDetails.addView(layout);
 				TextView border = new TextView(getContext());
 				border.setLayoutParams(new LayoutParams(
 						LayoutParams.FILL_PARENT, 1, 1));
 				border.setBackgroundResource(R.drawable.list_divider);
 				llDetails.addView(border);
 			}
 			if(postMap.containsKey(bean.getDisplayName())){
 				String value = postMap.get(bean.getDisplayName());
 				if(value == null || value.equals(""))continue;
 				if(bean.getUnit() != null && !bean.getUnit().equals("")){
 					value = value.replace(bean.getUnit(), "");
 				}
 				View control = (View)layout.getTag(PostGoodsView.HASH_CONTROL);
 				if(control instanceof CheckBox){
 					if(value.contains(((CheckBox)control).getText())){
 						((CheckBox)control).setChecked(true);
 					}
 					else{
 						((CheckBox)control).setChecked(false);
 					}
 				}else if(control instanceof EditText){					
 					((EditText)control).setText(value);
 				}else if(control instanceof TextView){
 					String[] values = value.split(",");
 					String displayName = "";
 					if(bean.getLabels() != null){
						for(int j = 0; j < bean.getLabels().size(); ++ j){
 							for(int m = 0; m < values.length; ++ m){
								if(bean.getLabels().get(j).equals(values[m])){
									displayName += "," + bean.getValues().get(j);
 								}
 							}
 						}
 					}
 					if(displayName.length() > 0 && displayName.charAt(0) == ','){
 						displayName = displayName.substring(1);
 					}
 					((TextView)control).setText(displayName);
 				}
 			}
 		}		
 	}
 	
 	protected void Init(Context context) {
 	}
 	
 	@Override
 	public void onPreviousViewBack(int message, Object obj){
 		Pair<String, String> ret = PostGoodsView.fetchResultFromViewBack(message, obj, llDetails);
 		if(ret != null){
 			postMap.put(ret.first, ret.second);
 		}
 	}
 	
 	@Override
 	public boolean onRightActionPressed(){
 		postMap.putAll(PostGoodsView.extractInputData(llDetails));
 		m_viewInfoListener.onBack(message, postMap);
 		return true;
 	}
 }
