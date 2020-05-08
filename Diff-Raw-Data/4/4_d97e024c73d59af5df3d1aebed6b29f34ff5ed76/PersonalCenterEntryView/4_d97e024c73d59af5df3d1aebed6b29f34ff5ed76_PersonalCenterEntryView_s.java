 package com.quanleimu.view;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ErrorHandler;
 import com.quanleimu.util.Helper;
 import com.quanleimu.util.Util;
 import com.quanleimu.adapter.GoodsListAdapter;
 import com.quanleimu.view.BaseView;
 import com.quanleimu.view.BaseView.EBUTT_STYLE;
 import com.quanleimu.view.BaseView.ETAB_TYPE;
 import com.quanleimu.view.BaseView.TabDef;
 import com.quanleimu.view.BaseView.TitleDef;
 import com.quanleimu.widget.PullToRefreshListView;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import android.widget.LinearLayout;
 
 public class PersonalCenterEntryView extends BaseView implements
 		View.OnClickListener {
 	private Bundle bundle = null;
 	private UserBean user = null;
 	private String json = null;
 	static final int MSG_GETPERSONALADS = 1;
 
 	public PersonalCenterEntryView(Context context, Bundle bundle) {
 		super(context);
 		this.bundle = bundle;
 		init();
 	}
 
 	private void init() {
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		View v = inflater.inflate(R.layout.personalentryview, null);
 		this.addView(v);
 		this.findViewById(R.id.rl_wofav).setOnClickListener(this);
 		this.findViewById(R.id.rl_wohistory).setOnClickListener(this);
 		this.findViewById(R.id.rl_wosent).setOnClickListener(this);
 	}
 
 	@Override
 	public void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		user = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
 		
 		List<GoodsDetail> history = QuanleimuApplication.getApplication().getListLookHistory();
 		TextView tvHistory = (TextView)this.findViewById(R.id.tv_historycount);
 		tvHistory.setText(String.valueOf(history == null ? 0 : history.size()));
 		
 		List<GoodsDetail> favs = QuanleimuApplication.getApplication().getListMyStore();
 		TextView tvFav = (TextView)this.findViewById(R.id.tv_favcount);
 		tvFav.setText(String.valueOf(favs == null ? 0 : favs.size()));
 
 		if(user != null && ((this.bundle != null && bundle.getInt("forceUpdate") == 1)
 			|| QuanleimuApplication.getApplication().getListMyPost() == null
 			|| QuanleimuApplication.getApplication().getListMyPost().size() == 0)){
 			if (bundle != null) {
 				bundle.remove("forceUpdate");
 			}
 			pd = ProgressDialog.show(this.getContext(), "提示", "正在下载数据，请稍候...");
 				pd.setCancelable(true);
 				new Thread(new GetPersonalAdsThread()).start();
 		}
 		else{
 			TextView tvPersonalAds = (TextView) PersonalCenterEntryView.this.findViewById(R.id.tv_sentcount);
 			tvPersonalAds.setText(String.valueOf(QuanleimuApplication.getApplication().getListMyPost() == null ?
 					0 : QuanleimuApplication.getApplication().getListMyPost().size()));					
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()){
 		case R.id.rl_wofav:
 			if(QuanleimuApplication.getApplication().getListMyStore() != null 
 				&& QuanleimuApplication.getApplication().getListMyStore().size() > 0){
 				m_viewInfoListener.onNewView(new FavoriteAndHistoryView(this.getContext(), this.bundle, true));
 			}
 			break;
 		case R.id.rl_wohistory:
 			if(QuanleimuApplication.getApplication().getListLookHistory() != null
 				&& QuanleimuApplication.getApplication().getListLookHistory().size() > 0){
 				m_viewInfoListener.onNewView(new FavoriteAndHistoryView(this.getContext(), this.bundle, false));
 			}
 			break;
 		case R.id.rl_wosent:
 			if(user == null){
 				m_viewInfoListener.onNewView(new LoginView(this.getContext(), "用户中心"));
 			}else{
 				m_viewInfoListener.onNewView(new PersonalPostView(this.getContext(), bundle));
 			}			
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public boolean onLeftActionPressed() {
 		m_viewInfoListener.onNewView(new SetMainView(getContext()));
 		return true;
 	}
 
 	@Override
 	public TitleDef getTitleDef() {
 		TitleDef title = new TitleDef();
 		title.m_leftActionHint = "设置";
 		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
 		title.m_title = "用户中心";
 		title.m_visible = true;
 		return title;
 	}
 
 	@Override
 	public TabDef getTabDef() {
 		TabDef tab = new TabDef();
 		tab.m_visible = true;
 		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MINE;
 		return tab;
 	}
 
 	Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_GETPERSONALADS:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				if (json != null) {
 					GoodsList gl = JsonUtil.getGoodsListFromJson(json);
 					TextView tvPersonalAds = (TextView) PersonalCenterEntryView.this.findViewById(R.id.tv_sentcount);
 					tvPersonalAds.setText(String.valueOf((gl == null || gl.getData() == null) ? 0 : gl.getData().size()));
 					QuanleimuApplication.getApplication().setListMyPost(gl.getData());
 				}
 				break;
 			}
 		}
 	};
 
 	class GetPersonalAdsThread implements Runnable {
 		@Override
 		public void run() {
 			String apiName = "ad_list";
 			ArrayList<String> list = new ArrayList<String>();
 			 
 			list.add("query=userId:" + user.getId() + " AND status:0");
 			list.add("start=0");
 			list.add("rt=1");
 			list.add("rows=30");
 			
			if(bundle != null && bundle.getString("lastpost") != null){
				list.add("newAdIds=" + bundle.getString("lastpost"));
 			}
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url);
 				myHandler.sendEmptyMessage(MSG_GETPERSONALADS);
 				return;
 			} catch (UnsupportedEncodingException e) {
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_SERVICE_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			} catch (IOException e) {
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			} catch (Communication.BXHttpException e) {
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			}
 			
 			if(pd != null){
 				pd.dismiss();
 			}
 		}
 	}
 }
