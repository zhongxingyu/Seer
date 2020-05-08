 package com.quanleimu.view;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.Filterss;
 import com.quanleimu.entity.PostMu;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.Util;
 import com.quanleimu.entity.values;
 public class SiftView extends BaseView {
 	public List<String> listsize = new ArrayList<String>();
 
 	// 定义变量
 	public String backPageName = "";
 	private EditText ed_sift;
 
 	public int temp;
 	public String res = "";
 	public String value_resl = "";
 	public int idselected;
 	TextView tvmeta = null;
 
 	private Map<Integer, TextView> selector = new HashMap<Integer, TextView>();
 	private Map<String, EditText> editors = new HashMap<String, EditText>();
 
 	public List<Filterss> listFilterss = new ArrayList<Filterss>();
 
 	private Map<Integer, String> savemap = new HashMap<Integer, String>();
 
 	public Map<String, String> valuemap = new HashMap<String, String>();
 
 	public String categoryEnglishName = "";
 	public String json = "";
 
 	Bundle bundle = null;
 	
 	protected void Init(){
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		this.addView(inflater.inflate(R.layout.sifttest, null));
 		
 		ed_sift = (EditText) findViewById(R.id.edsift);
 		ed_sift.clearFocus();
 		
 		// AND 地区_s:m7259
 		PostMu postMu = (PostMu) Util.loadDataFromLocate(getContext(), "saveFilterss"+categoryEnglishName+QuanleimuApplication.getApplication().cityEnglishName);
 		if (postMu == null || postMu.getJson().equals("")) {
 			System.out.println("下载");
 			new Thread(new GetGoodsListThread(true)).start();
 		} else {
 			System.out.println("缓存");
 			json = postMu.getJson();
 			long time = postMu.getTime();
 			if(time + 24*3600*1000 < System.currentTimeMillis()){
 				myHandler.sendEmptyMessage(1);
 				new Thread(new GetGoodsListThread(false)).start();
 			}else{
 				myHandler.sendEmptyMessage(1);
 			}
 		}
 	}
 	
 //	public SiftTest(Context context){
 //		super(context); 
 //		
 //		categoryEnglishName = bundle.getString(
 //				"categoryEnglishName");
 //		backPageName = bundle.getString("backPageName");
 //		
 //		Init();
 //	}
 	
 	
 	public SiftView(Context context, Bundle bundle_){
 		super(context);
 		
 		categoryEnglishName = bundle_.getString("categoryEnglishName");
 		backPageName = bundle_.getString("backPageName");		
 		this.bundle = bundle_;
 		
 		Init();
 	}
 	
 	//public Bundle extracBundle(){return new Bundle();}//return a bundle that could be used to re-build the very BaseView
 //	
 //	public void onDestroy(){}//called before destruction
 //	public void onPause(){}//called before put into stack
 //	public void onResume(){}
 	
 //	public boolean onBack(){return false;}//called when back button/key pressed
 //	public boolean onLeftActionPressed(){return false;}//called when left button on title bar pressed, return true if handled already, false otherwise
 	public boolean onRightActionPressed(){
 		if(null != m_viewInfoListener){
 			//pop last GetGoodsView
 			m_viewInfoListener.onPopView(GetGoodsView.class.getName());
 			
 			//compose the sift result
 			String result = "";
 
 			String str = ed_sift.getText().toString().trim();
 			if( valuemap != null && valuemap.size() != 0)
 			{
 				for (int i = 0; i < listFilterss.size(); i++) {
 
 					String key = listFilterss.get(i).getName();
 					if (valuemap.get(key) != null && !valuemap.get(key).equals("")) {
 						result += " AND "
 								+ URLEncoder.encode(key) + ":"
 								+ URLEncoder.encode(valuemap.get(key));
 					}
 				}
 			}
 			
 			for(int i = 0; i < editors.size(); ++i){
 				String key = editors.keySet().toArray()[i].toString();
 				
 				EditText txtEditor = (EditText)editors.get(key);
 				String textInput = txtEditor.getText().toString();
 				if(textInput.length() > 0){
 					result += " AND "
 							+ URLEncoder.encode(key) + ":"
 							+ URLEncoder.encode(textInput);
 				}
 			}
 
 			
 			if (!str.equals("")) {
				if(result.length() > 0){
					result += " AND ";
				}
				result += URLEncoder.encode(str);
 			}
 			
 //			if (!str.equals("")) {
 //				result += URLEncoder.encode(str);
 //			}
 			
 			if(result.length() > 0)
 			{
 				bundle.putString("siftresult", result);
 				bundle.putString("backPageName", backPageName);
 				if(null != m_viewInfoListener){
 					m_viewInfoListener.onExit(this);
 					m_viewInfoListener.onNewView(new GetGoodsView(getContext(), bundle, categoryEnglishName, result));			
 				}				
 			}else{
 				if(bundle.getString("siftresult") != null && !bundle.getString("siftresult").equals("")){
 					bundle.putString("siftresult", "");
 					if(null != m_viewInfoListener){
 						m_viewInfoListener.onExit(this);
 						m_viewInfoListener.onNewView(new GetGoodsView(getContext(), bundle, categoryEnglishName, result));			
 					}				
 				}
 				else{
 					CharSequence text = "请输入或选择筛选条件。";
 					int duration = Toast.LENGTH_SHORT;
 					Toast toast = Toast.makeText(getContext(), text, duration);
 					toast.show();
 				}
 			}
 //			
 //			if(bundle.getString("siftresult") != null)
 //			{
 //				bundle.putString("siftresult", result);
 //				bundle.putString("backPageName", "选择类目");
 //				
 //				if(null != m_viewInfoListener){
 //					m_viewInfoListener.onExit(this);
 //					m_viewInfoListener.onNewView(new GetGoodsView(getContext(), bundle, categoryEnglishName, result));			
 //				}				
 //			}else{
 //				CharSequence text = "请输入或选择筛选条件。";
 //				int duration = Toast.LENGTH_SHORT;
 //				Toast toast = Toast.makeText(getContext(), text, duration);
 //				toast.show();
 //			}
 		}		
 		
 		
 		
 		return true;
 	}//called when right button on title bar pressed, return true if handled already, false otherwise
 	
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_title = "筛选";
 		title.m_leftActionHint = "返回";
 		title.m_rightActionHint = "确定";
 		return title;
 	}
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = false;
 		return tab;
 	}
 
 	@Override
 	public void onPreviousViewBack(int message, Object obj) {
 		if (message == 1234) {
 			Bundle data = (Bundle)obj;
 			
 			String s = data.getString("all"); 
 			if(s==null || s.equals("")){
 				res = data.getString("label");
 				value_resl = data.getString("value");
 				
 				if(temp < listFilterss.size() && listFilterss.get(temp).toString().length() > 0){
 					valuemap.put(listFilterss.get(temp).getName(), value_resl);
 				}
 				selector.get(temp).setText(res);
 				savemap.put(temp, res);
 			}else{
 				//res = datas.getString("label");
 				//value_resl = datas.getString("value");
 				
 				if(temp < listFilterss.size() && listFilterss.get(temp).toString().length() > 0){
 					valuemap.remove(listFilterss.get(temp).getName());
 				}
 				//savemap.put(temp, res);				
 				selector.get(temp).setText(s);
 			}
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
 			System.out.println("url ------ >" + url);
 			try {
 				json = Communication.getDataByUrl(url);
 				if (json != null) {
 					PostMu postMu = new PostMu();
 					postMu.setJson(json);
 					postMu.setTime(System.currentTimeMillis());
 					Util.saveDataToLocate(SiftView.this.getContext(), "saveFilterss"+categoryEnglishName+QuanleimuApplication.getApplication().cityEnglishName, postMu);
 					if(isUpdate){
 						myHandler.sendEmptyMessage(1);
 					}
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
 
 	// 管理线程的Handler
 	Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case 1:
 				if (pd != null) { 
 					pd.dismiss();
 				}
 
 				listFilterss = JsonUtil.getFilters(json).getFilterssList();
 				QuanleimuApplication.getApplication().setListFilterss(listFilterss);
 				LinearLayout ll_meta = (LinearLayout) findViewById(R.id.meta);
 				LayoutInflater inflater = LayoutInflater.from(SiftView.this.getContext());
 				if (listFilterss == null) {
 					ll_meta.setVisibility(View.GONE);
 				} else {
 
 					HashMap<String, String> preValues = null;
 					if(bundle != null){
 						String preEncResult = bundle.getString("siftresult");
 						if(null != preEncResult){
 							String decResult = URLDecoder.decode(preEncResult);
 							String[] pairs = decResult.split("AND ");
 							if(pairs != null){
 								preValues = new HashMap<String, String>();
 							}
 							for(int x = 0; x < pairs.length; ++ x){
 								String[] subPairs = pairs[x].split(":");
 								if(subPairs.length != 2)continue;
 								subPairs[0] = subPairs[0].trim();
 								subPairs[1] = subPairs[1].trim();
 								preValues.put(subPairs[0], subPairs[1]);
 							}
 						}
 					}
 					
 					for (int i = 0; i < listFilterss.size();++i) {
 						View v = null;
 						ImageView tvim = null;
 						EditText tved = null;
 						TextView tvmetatxt = null;
 						v = inflater.inflate(R.layout.item_sift, null);
 						v.setLayoutParams(new LinearLayout.LayoutParams(
 								LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 
 						if (i == 0) {
 							v.setBackgroundResource(R.drawable.btn_top_bg);
 							if(listFilterss.size() == 1){
 								v.setBackgroundResource(R.drawable.btn_s_bg);
 							}
 						} else if (i == listFilterss.size() - 1) {
 							// v.setBackgroundResource(R.drawable.btn_m_bg);
 							v.setBackgroundResource(R.drawable.btn_down_bg);
 						} else {
 							v.setBackgroundResource(R.drawable.btn_m_bg);
 						}
 
 						v.setPadding(10, 10, 10, 10);
 
 						if (listFilterss.get(i).getControlType().equals("select")) {
 							// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 							valuemap.put(listFilterss.get(i).getName(), "");
 							// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 							savemap.put(i, "");
 							tvmetatxt = (TextView) v.findViewById(R.id.tvmetatxt);
 							tvmetatxt.setLayoutParams(new LinearLayout.LayoutParams(
 									LayoutParams.FILL_PARENT,
 									LayoutParams.WRAP_CONTENT, 1));
 							tvmetatxt.setText(listFilterss.get(i).getDisplayName());
 
 							tvmeta = (TextView) v.findViewById(R.id.tvmeta);
 							tvmeta.setGravity(Gravity.RIGHT);
 							tvmeta.setLayoutParams(new LinearLayout.LayoutParams(
 									LayoutParams.FILL_PARENT,
 									LayoutParams.WRAP_CONTENT, 1));
 							if(preValues != null && preValues.containsKey(listFilterss.get(i).getName())){
 								String preValue = preValues.get(listFilterss.get(i).getName());
 								List<values>values = listFilterss.get(i).getValuesList();
 								boolean valid = false;
 								for(int z = 0; z < values.size(); ++ z){
 									if(values.get(z).getValue().equals(preValue)){
 										tvmeta.setText(listFilterss.get(i).getLabelsList().get(z).getLabel());
 										valuemap.put(listFilterss.get(i).getName(), preValue);
 										valid = true;
 										break;
 									}
 								}
 								
 								if(!valid){
 									tvmeta.setText("请选择");
 								}
 							}
 							else{
 								tvmeta.setText("请选择");
 							}
 
 							tvim = (ImageView) v.findViewById(R.id.tvimage);
 							tvim.setLayoutParams(new LinearLayout.LayoutParams(
 									LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
 							tvim.setPadding(10, 10, 10, 10);
 							tvim.setImageResource(R.drawable.arrow);
 							tvim.setBackgroundDrawable(null);
 
 							tved = (EditText) v.findViewById(R.id.tved);
 							tved.setVisibility(View.GONE);
 							selector.put(i, tvmeta);
 							
 							v.setTag(i);
 
 							v.setOnClickListener(new OnClickListener() {
 								@Override
 								public void onClick(View v) {
 									temp = Integer.parseInt(v.getTag().toString());
 
 									bundle.putInt("temp", temp);
 									bundle.putString("title", listFilterss.get(temp).getDisplayName());
 									bundle.putString("back", "筛选");
 									
 									if(null != m_viewInfoListener){
 										m_viewInfoListener.onNewView(new SiftOptionListView(getContext(), bundle));
 									}
 								}
 							});
 							
 						}
 						// else
 						// if(listFilterss.get(i).getControlType().equals(""))
 						else {
 							tvmetatxt = (TextView) v.findViewById(R.id.tvmetatxt);
 							tvmetatxt.setText(listFilterss.get(i).getDisplayName());
 							tvmetatxt.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
 							tvmetatxt.setLayoutParams(new LinearLayout.LayoutParams(
 									LayoutParams.WRAP_CONTENT,
 									LayoutParams.FILL_PARENT));
 							tvmetatxt.setPadding(0, 0, 10, 0);
 							
 							if(null != preValues && preValues.containsKey(listFilterss.get(i).getName())){
 								String preValue = preValues.get(listFilterss.get(i).getName());
 								List<values>values = listFilterss.get(i).getValuesList();
 								for(int z = 0; z < values.size(); ++ z){
 									if(values.get(z).getValue().equals(preValue)){
 										tvmeta.setText(listFilterss.get(i).getLabelsList().get(z).getLabel());
 										valuemap.put(listFilterss.get(i).getName(), preValue);
 										break;
 									}
 								}
 								
 							}							tvmeta = (TextView) v.findViewById(R.id.tvmeta);
 							tvmeta.setVisibility(View.GONE);
 							
 							tvim = (ImageView) v.findViewById(R.id.tvimage);
 							tvim.setVisibility(View.GONE);
 							
 							tved = (EditText) v.findViewById(R.id.tved);
 							tved.clearFocus();
 							tved.setLayoutParams(new LinearLayout.LayoutParams(
 									LayoutParams.FILL_PARENT,
 									LayoutParams.WRAP_CONTENT, 1));
 						}
 						
 						ll_meta.addView(v);
 					}
 
 				}
 
 				break;
 			case 2:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				Toast.makeText(getContext(), "服务当前不可用，请稍后重试！", 3).show();
 				break;
 
 			}
 			super.handleMessage(msg);
 		}
 	};
 }
