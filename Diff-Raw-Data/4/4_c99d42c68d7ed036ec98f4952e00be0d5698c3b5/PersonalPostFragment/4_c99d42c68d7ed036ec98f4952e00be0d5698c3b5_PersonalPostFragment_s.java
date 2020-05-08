 package com.quanleimu.view.fragment;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.quanleimu.activity.BaseActivity;
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.GoodsListAdapter;
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsDetail.EDATAKEYS;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ErrorHandler;
 import com.quanleimu.util.GoodsListLoader;
 import com.quanleimu.util.TrackConfig.TrackMobile.BxEvent;
 import com.quanleimu.util.TrackConfig.TrackMobile.Key;
 import com.quanleimu.util.TrackConfig.TrackMobile.PV;
 import com.quanleimu.util.Tracker;
 import com.quanleimu.util.Util;
 import com.quanleimu.widget.PullToRefreshListView;
 
 public class PersonalPostFragment extends BaseFragment  implements PullToRefreshListView.OnRefreshListener{
 	private final int MSG_MYPOST = 1;
 	private final int MSG_INVERIFY = 2;
 	private final int MSG_DELETED = 3;
 	private final int MCMESSAGE_DELETE = 5;
 	private final int MSG_DELETE_POST_SUCCESS = 6;
 	private final int MSG_DELETE_POST_FAIL = 7;
 	private final int MSG_RESTORE_POST_SUCCESS = 8;
 	private final int MSG_RESTORE_POST_FAIL = 9;
     private final int MSG_ITEM_OPERATE = 10;
     private final int MSG_SHOW_BIND_DIALOG = 11;
 
 	public PullToRefreshListView lvGoodsList;
 //	public ImageView ivMyads, ivMyfav, ivMyhistory;
 
 	private List<GoodsDetail> listMyPost = null;
 	private List<GoodsDetail> listInVerify = null;
 	private List<GoodsDetail> listDeleted = null;
 	
 	public GoodsListAdapter adapter = null;
 //	private String json;
 	UserBean user;
 
     /**
      * 用这几个 static value 区分不同类别“我的信息”
      */
     public final static String TYPE_KEY = "PersonalPostFragment_type_key";
     public final static int TYPE_MYPOST = 0;   //0:mypost, 2:inverify, 2:deleted
     public final static int TYPE_INVERIFY = 1;
     public final static int TYPE_DELETED = 2;
 
     private int currentType = TYPE_MYPOST;
 
 	private Bundle bundle;
 	private GoodsListLoader glLoader = null;
 	
 	private String json = "";
 
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
         final Bundle arguments = getArguments();
         if (arguments != null && arguments.containsKey(PersonalPostFragment.TYPE_KEY)) {
             this.currentType = arguments.getInt(PersonalPostFragment.TYPE_KEY,
                     PersonalPostFragment.TYPE_MYPOST);
         }
 
 
 		user = (UserBean) Util.loadDataFromLocate(this.getActivity(), "user");
 	}
 
 	@Override
 	public void onStackTop(boolean isBack) {
 		if(!isBack){
 			lvGoodsList.fireRefresh();
 		}
 	}
 
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
         View v = inflater.inflate(R.layout.personalcenterview, null);
         v.findViewById(R.id.linearType).setVisibility(View.GONE);  // 禁用掉 已发布、审核中、已删除 tabView，后续删除
 		
 		try {
 			if (Util.JadgeConnection(this.getActivity()) == false) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		lvGoodsList = (PullToRefreshListView) v.findViewById(R.id.lvGoodsList);
 
 //		ivMyads = (ImageView) v.findViewById(R.id.ivMyads);
 //		ivMyfav = (ImageView) v.findViewById(R.id.ivMyfav);
 //		ivMyhistory = (ImageView) v.findViewById(R.id.ivMyhistory);
 //
 //		ivMyads.setOnClickListener(this);
 //		ivMyfav.setOnClickListener(this);
 //		ivMyhistory.setOnClickListener(this);
 		if (currentType == TYPE_MYPOST)
 			listMyPost = QuanleimuApplication.getApplication().getListMyPost();
 		
 		adapter = new GoodsListAdapter(this.getActivity(), this.listMyPost);
         adapter.setHasDelBtn(true);
 		adapter.setOperateMessage(handler, MSG_ITEM_OPERATE);
 		lvGoodsList.setAdapter(adapter);
 
 		GoodsList gl = new GoodsList();
 		gl.setData(listMyPost == null ? new ArrayList<GoodsDetail>() : listMyPost);
 	
 		glLoader = new GoodsListLoader(null, handler, null, null);
 		glLoader.setHasMore(false);
 		glLoader.setGoodsList(gl);
 		glLoader.setSearchUserList(true);
 		
 		lvGoodsList.setOnRefreshListener(this);	
 		
 		Bundle bundle = this.getArguments();
 		if(bundle != null && bundle.containsKey(PostGoodsFragment.KEY_LAST_POST_CONTACT_USER)){
 			if(bundle.getBoolean(PostGoodsFragment.KEY_LAST_POST_CONTACT_USER, false)){
 				this.handler.sendEmptyMessageDelayed(MSG_SHOW_BIND_DIALOG, 3000);
 			}
 			bundle.remove(PostGoodsFragment.KEY_LAST_POST_CONTACT_USER);
 		}
 		
 		return v;
 	}
 	
 	
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		
 		if(adapter != null){
 //			adapter.setHasDelBtn(false);
 			adapter.notifyDataSetChanged();
 			lvGoodsList.invalidateViews();
 		}
 		
 		for(int i = 0; i < lvGoodsList.getChildCount(); ++i){
 			ImageView imageView = (ImageView)lvGoodsList.getChildAt(i).findViewById(R.id.ivInfo);
 			
 			if(	null != imageView	
 					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
 					/*&& null != imageView.getDrawable()
 					&& imageView.getDrawable() instanceof AnimationDrawable*/){
 				SimpleImageLoader.Cancel(imageView.getTag().toString(), imageView);
 			}
 		}	
 	}
 
 
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		this.rebuildPage(getView(), false);
 		
 		for(int i = 0; i < lvGoodsList.getChildCount(); ++i){
 			ImageView imageView = (ImageView)lvGoodsList.getChildAt(i).findViewById(R.id.ivInfo);
 			
 			if(	null != imageView	
 					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
 					/*&& null != imageView.getDrawable()
 					&& imageView.getDrawable() instanceof AnimationDrawable*/){
 				SimpleImageLoader.showImg(imageView, imageView.getTag().toString(), null, getActivity());
 			}
 		}
 		
 		glLoader.setHasMoreListener(null);
 		glLoader.setHandler(handler);
 		adapter.setList(glLoader.getGoodsList().getData());
 		lvGoodsList.setSelectionFromHeader(glLoader.getSelection());
 	}
 
 
 
 	private void rebuildPage(View rootView, boolean onResult){
 		
 		if(glLoader != null){
 			glLoader.setHandler(handler);
 		}
 		LinearLayout lView = (LinearLayout)rootView.findViewById(R.id.linearListView);
 
         BxEvent bxEvent = BxEvent.SENT_RESULT;
         int adsCountValue = 0;
 
 		if(TYPE_MYPOST == currentType){
             bxEvent = BxEvent.SENT_RESULT;
 
 //			ivMyads.setImageResource(R.drawable.bg_segment_sent_selected);
 //			ivMyfav.setImageResource(R.drawable.bg_segment_approving);
 //			ivMyhistory.setImageResource(R.drawable.bg_segment_deleted);
 //			if(m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_title = "已发布的信息";
 //				title.m_rightActionHint = (-1 == buttonStatus ? "编辑" : "完成");
 				refreshHeader();
 //				m_viewInfoListener.onTitleChanged(title);
 //			}
 			GoodsList gl = new GoodsList();
 			gl.setData(listMyPost);
 			glLoader.setGoodsList(gl);
 			adapter.setList(listMyPost);
 			adapter.notifyDataSetChanged();
 			lvGoodsList.invalidateViews();
             if (listMyPost != null) {
                 adsCountValue = listMyPost.size();
             }
 
 		}
 		else if(TYPE_INVERIFY == currentType){
             bxEvent = BxEvent.APPROVING_RESULT;
 
 			lvGoodsList.setVisibility(View.VISIBLE);
 //			ivMyads.setImageResource(R.drawable.bg_segment_sent);
 //			ivMyfav.setImageResource(R.drawable.bg_segment_approving_selected);
 //			ivMyhistory.setImageResource(R.drawable.bg_segment_deleted);
 			
 //			if(m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_title = "审核中";
 //				title.m_rightActionHint = (-1 == buttonStatus ? "编辑" : "完成");
 				refreshHeader();
 //				m_viewInfoListener.onTitleChanged(title);
 //			}
 			if(listInVerify == null){
 				adapter.setList(new ArrayList<GoodsDetail>());
 				adapter.notifyDataSetChanged();
 				lvGoodsList.invalidateViews();
 
 				showSimpleProgress();
 				this.onRefresh();
 			}
 			else{
                 adsCountValue = listInVerify.size();
 				adapter.setList(listInVerify);
 				adapter.notifyDataSetChanged();
 				lvGoodsList.invalidateViews();
 				GoodsList gl = new GoodsList();
 				gl.setData(listInVerify);
 				glLoader.setGoodsList(gl);
 
 			}
 		}
 		else{
             bxEvent = BxEvent.DELETED_RESULT;
 			lvGoodsList.setVisibility(View.VISIBLE);
 //			ivMyads.setImageResource(R.drawable.bg_segment_sent);
 //			ivMyfav.setImageResource(R.drawable.bg_segment_approving);
 //			ivMyhistory.setImageResource(R.drawable.bg_segment_deleted_selected);
 //			if(m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_title = "已删除";
 //				title.m_rightActionHint = "编辑";
 				refreshHeader();
 //				m_viewInfoListener.onTitleChanged(title);
 //			}
 			if(listDeleted == null){
 				adapter.setList(new ArrayList<GoodsDetail>());
 				adapter.notifyDataSetChanged();
 				lvGoodsList.invalidateViews();
 				showSimpleProgress();
 				this.onRefresh();
 			}
 			else{
                 adsCountValue = listDeleted.size();
 				adapter.setList(listDeleted);
 				adapter.notifyDataSetChanged();
 				lvGoodsList.invalidateViews();
 			}
 		}
 
         Tracker.getInstance().event(bxEvent).append(Key.ADSCOUNT, adsCountValue).end();
 
 		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {			
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				final int index = arg2 - lvGoodsList.getHeaderViewsCount();
 				if(index < 0)
 					return;
 				
 				if(TYPE_MYPOST == currentType && null != listMyPost && index < listMyPost.size() ){
 //					m_viewInfoListener.onNewView(new GoodDetailView(getContext(), bundle, glLoader, index, null));
 					Bundle bundle = createArguments(null, null);
 					bundle.putSerializable("loader", glLoader);
 					bundle.putInt("index", index);
 					pushFragment(new GoodDetailFragment(), bundle);
 					
 				}
 				else if(null !=  listInVerify && index < listInVerify.size() && TYPE_INVERIFY == currentType){
 					Bundle bundle = createArguments(null, null);
 					bundle.putSerializable("loader", glLoader);
 					bundle.putInt("index", index);
 					pushFragment(new GoodDetailFragment(), bundle);
 				}
 				else if(null != listDeleted && index < listDeleted.size() && TYPE_DELETED == currentType){
 					final String[] names = {"彻底删除", "恢复"};
 					new AlertDialog.Builder(getActivity()).setTitle("选择操作")
 					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					})
 					.setItems(names, new DialogInterface.OnClickListener(){
 						public void onClick(DialogInterface dialog, int which){
 							switch(which){
 								case 0:
 									String id = listDeleted.get(index).getValueByKey(EDATAKEYS.EDATAKEYS_ID);
 									showSimpleProgress();
 									(new Thread(new MyMessageDeleteThread(id))).start();
 									break;
 								case 1:
 									String id2 = listDeleted.get(index).getValueByKey(EDATAKEYS.EDATAKEYS_ID);
 									showSimpleProgress();
 									(new Thread(new MyMessageRestoreThread(id2))).start();
 									break;
 							}
 						}
 					}).show();
 
 				}
 			}
 		});
 		lvGoodsList.invalidateViews();
 		lvGoodsList.setOnRefreshListener(this);	
 		lvGoodsList.setSelectionFromHeader(glLoader.getSelection());
 	}
 	
 	
 	
 	
 	@Override
 	protected void handleMessage(final Message msg, Activity activity, View rootView) {
 		switch (msg.what) {
 		case MSG_MYPOST:
 		case MSG_INVERIFY:
 		case MSG_DELETED:
 			hideProgress();
 			GoodsList gl = JsonUtil.getGoodsListFromJson(glLoader.getLastJson());
 			this.pv = (currentType==TYPE_MYPOST?PV.MYADS_SENT:(currentType==TYPE_INVERIFY?PV.MYADS_APPROVING:PV.MYADS_DELETED));
 			//tracker
 			if (gl == null || gl.getData() == null) {//no ads count
 				Tracker.getInstance()
 				.pv(this.pv)
 				.end();
 			} else {//ads count
 				Tracker.getInstance()
 				.pv(this.pv)
 				.append(Key.ADSCOUNT, gl.getData().size() + "")
 				.end();
 			}
 			
 			if (gl == null || gl.getData().size() == 0) {
 				if(msg.what == MSG_MYPOST) {
 
 					if(null != listMyPost) listMyPost.clear();
 				}
 				else if(msg.what == MSG_INVERIFY) {
 					if(listInVerify == null){
 						listInVerify = new ArrayList<GoodsDetail>();
 					}
 					listInVerify.clear();
 				}
 				else if(msg.what == MSG_DELETED){
 					if(listDeleted == null){
 						listDeleted = new ArrayList<GoodsDetail>();
 					}
 					listDeleted.clear();
 				}
 				glLoader.setGoodsList(new GoodsList());
 			}
 			else{
 				if(msg.what == MSG_MYPOST){
 					listMyPost = gl.getData();
 					if(listMyPost != null){
 						for(int i = listMyPost.size() - 1; i >= 0; -- i){
 							if(!listMyPost.get(i).getValueByKey("status").equals("0")){
 								listMyPost.remove(i);
 							}
 						}
 					}
 					GoodsList gl2 = new GoodsList();
 					gl2.setData(listMyPost);
 					glLoader.setGoodsList(gl2);
 				}
 				else if(msg.what == MSG_INVERIFY) {
 					listInVerify = gl.getData();
 					if(listInVerify != null){
 						for(int i = listInVerify.size() - 1; i >= 0; -- i){
 							if(!listInVerify.get(i).getValueByKey("status").equals("4") 
 									&& !listInVerify.get(i).getValueByKey("status").equals("20")){
 								listInVerify.remove(i);
 							}
 						}
 					}
 					GoodsList gl2 = new GoodsList();
 					gl2.setData(listInVerify);
 					glLoader.setGoodsList(gl2);
 				}
 				else if(msg.what == MSG_DELETED){
 					listDeleted = gl.getData();
 					
 					if(listDeleted != null){
 						for(int i = listDeleted.size() - 1; i >= 0; -- i){
 							if(!listDeleted.get(i).getValueByKey("status").equals("3")){
 								listDeleted.remove(i);
 							}
 						}
 					}
 					GoodsList gl2 = new GoodsList();
 					gl2.setData(listDeleted);
 					glLoader.setGoodsList(gl2);
 				}
 			}
 			if(msg.what == MSG_MYPOST){
 				QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 			}
 			rebuildPage(rootView, true);
 			lvGoodsList.onRefreshComplete();
 			break;
 		case GoodsListLoader.MSG_FIRST_FAIL:
 		case GoodsListLoader.MSG_EXCEPTION:{
 			hideProgress();
 			lvGoodsList.onRefreshComplete();
 			break;
 		}
 
 		case MCMESSAGE_DELETE:
 			int pos = msg.arg2;
 //			pos = pos - lvGoodsList.getHeaderViewsCount();
 			String id = glLoader.getGoodsList().getData().get(pos).getValueByKey(EDATAKEYS.EDATAKEYS_ID);
 			showSimpleProgress();
 			new Thread(new MyMessageDeleteThread(id)).start();
 			break;
 		case MSG_DELETE_POST_FAIL:
 			hideProgress();
 			Toast.makeText(activity, "删除失败,请稍后重试！", 0).show();
 			break;
 		case MSG_DELETE_POST_SUCCESS:
 			hideProgress();
 			
 			Object deletedId = msg.obj;
 			try {
 				JSONObject jb = new JSONObject(json);
 				JSONObject js = jb.getJSONObject("error");
 				String message = js.getString("message");
 				int code = js.getInt("code");
 				List<GoodsDetail> refList = null;
 				if(msg.arg1 == TYPE_MYPOST){
 					refList = listMyPost;
 				}
 				else if(msg.arg1 == TYPE_INVERIFY){
 					refList = listInVerify;
 				}
 				else if(msg.arg1 == TYPE_DELETED){
 					refList = listDeleted;
 				}
 				if(refList == null) break;
 				if (code == 0) {
 					for(int i = 0; i < refList.size(); ++ i){
 						if(refList.get(i).getValueByKey(EDATAKEYS.EDATAKEYS_ID).equals((String)deletedId)){
 							refList.remove(i);
 							break;
 						}
 					}
 					if(msg.arg1 == -1){
 						QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 					}
 					adapter.setList(refList);						
 					adapter.notifyDataSetChanged();
 					lvGoodsList.invalidateViews();
 					Toast.makeText(activity, message, 0).show();
 				} else {
 					Toast.makeText(activity, "删除失败,请稍后重试！", 0).show();
 				}
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			adapter.setUiHold(false);
 			break;	
 		case MSG_RESTORE_POST_FAIL:
 			hideProgress();
 			Toast.makeText(activity, "恢复失败,请稍后重试！", 0).show();
 			break;
 		case MSG_RESTORE_POST_SUCCESS:
 			hideProgress();
 			if(listDeleted == null) break;
 			try{
 				JSONObject jb = new JSONObject(json);
 				JSONObject js = jb.getJSONObject("error");
 				String message = js.getString("message");
 				int code = js.getInt("code");
 				if(code == 0){
 					for(int i = 0; i < listDeleted.size(); ++ i){
 						if(listDeleted.get(i).getValueByKey(EDATAKEYS.EDATAKEYS_ID).equals((String)msg.obj)){
 							listDeleted.remove(i);
 							break;
 						}
 					}
 					if(TYPE_DELETED == currentType){
 						adapter.setList(listDeleted);
 						adapter.notifyDataSetChanged();
 						lvGoodsList.invalidateViews();
 					}
 					Toast.makeText(activity, message, 0).show();
 				}
 				else{
 					Toast.makeText(activity, "恢复失败,请稍后重试！", 0).show();
 				}
 			}catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			break;
 		case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
 			hideProgress();
 			//tracker
 			Tracker.getInstance()
 			.pv((currentType==MSG_MYPOST?PV.MYADS_SENT:(currentType==MSG_INVERIFY?PV.MYADS_APPROVING:PV.MYADS_DELETED)) )
 			.end();
 			
 			Message msg2 = Message.obtain();
 			msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 			QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			lvGoodsList.onRefreshComplete();
 			lvGoodsList.onFail();
 			
 			break;
         case MSG_ITEM_OPERATE:
             showItemOperateMenu(msg);
             break;
         case MSG_SHOW_BIND_DIALOG:
         	showBindDialog();
         	break;
 		}
 	}
 	
 	@Override
 	public boolean handleBack(){
 		Bundle bundle = createArguments(null, null);
		bundle.putInt("defaultPageIndex", 1);
		((BaseActivity)this.getActivity()).pushFragment(new HomeFragment(), bundle, true);
 		return true;
 	}
 	
 	private boolean isEditPost() {
 		Bundle bundle = this.getArguments();
 		if(bundle != null && bundle.containsKey(PostGoodsFragment.KEY_IS_EDITPOST)){
 			return bundle.getBoolean(PostGoodsFragment.KEY_IS_EDITPOST);
 		}
 		return false;
 	}
 	
 	private String getPostCateEnglishName() {
 		Bundle bundle = this.getArguments();
 		if(bundle != null && bundle.containsKey(PostGoodsFragment.KEY_IS_EDITPOST)){
 			return bundle.getString(PostGoodsFragment.KEY_CATE_ENGLISHNAME);
 		}
 		return "";
 	}
 	
 	private void showBindDialog(){
 //		String[] items = {"绑定百姓网帐号，网站手机统一管理", "继续发布信息"};
 		new AlertDialog.Builder(this.getActivity())
 		.setMessage(R.string.personalpost_bind_baixing_account)
 //		.setItems(items, new DialogInterface.OnClickListener() {
 //			@Override
 //			public void onClick(DialogInterface dialog, int which) {
 //				if(0 == which){
 //					BaseActivity activity = (BaseActivity)getActivity();
 ////					finishFragment(PostGoodsFragment.MSG_POST_SUCCEED, null);
 //					if(activity != null){
 //						activity.pushFragment(new LoginFragment(), PersonalPostFragment.createArguments(null, null), false);
 //					}
 //				}
 //				else{
 //					PersonalPostFragment.this.finishFragment();
 //				}
 //			}
 //		})
 		.setPositiveButton("是", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 //				Log.d("person","isEditPost:"+isEditPost()+",cateName:"+getPostCateEnglishName());
 				//tracker
 				Tracker.getInstance()
 				.event(BxEvent.POST_POSTWITHLOGIN)
 				.append(Key.SECONDCATENAME, getPostCateEnglishName())
 				.end();
 				
 				dialog.dismiss();
 				BaseActivity activity = (BaseActivity)getActivity();
 				if(activity != null){
 					activity.pushFragment(new LoginFragment(), PersonalPostFragment.createArguments(null, null), false);
 				}				
 			}
 		})
 		.setNegativeButton("否", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {	
 				//tracker
 				Tracker.getInstance()
 				.event(BxEvent.POST_POSTWITHOUTLOGIN)
 				.append(Key.SECONDCATENAME, getPostCateEnglishName())
 				.end();
 				dialog.dismiss();
 			}
 		}).show();
 		
 	}
 
     /**
      *
      * @param msg 根据 msg.arg2 定位 ad
      */
     private void showItemOperateMenu(final Message msg) {
         final int pos = msg.arg2;
         final String adId = glLoader.getGoodsList().getData().get(pos).getValueByKey(EDATAKEYS.EDATAKEYS_ID);
 
         // 弹出 menu 确认操作
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setTitle("操作");
 
         int r_array_item_operate = R.array.item_operate_mypost;
         if (currentType == TYPE_MYPOST) {
             r_array_item_operate = R.array.item_operate_mypost;
             Tracker.getInstance().event(BxEvent.SENT_MANAGE).end();
         } else if (currentType == TYPE_INVERIFY) {
             r_array_item_operate = R.array.item_operate_inverify;
             Tracker.getInstance().event(BxEvent.APPROVING_MANAGE).end();
         } else if (currentType == TYPE_DELETED) {
             r_array_item_operate = R.array.item_operate_deleted;
             Tracker.getInstance().event(BxEvent.DELETED_MANAGE).end();
         }
 
         builder.setItems(r_array_item_operate, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int clickedIndex) {
                 if (currentType == TYPE_MYPOST) {
                     switch (clickedIndex) {
                         case 0://刷新
                             doRefresh(0, adId);
                             Tracker.getInstance().event(BxEvent.SENT_REFRESH).end();
                             break;
                         case 1://修改
                             GoodsDetail detail = listMyPost.get(pos);
                             Bundle args = createArguments(null, null);
                             args.putSerializable("goodsDetail", detail);
                             args.putString("cateNames", detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME));
                             pushFragment(new PostGoodsFragment(), args);
                             Tracker.getInstance().event(BxEvent.SENT_EDIT).end();
                             break;
                         case 2://删除
                             showSimpleProgress();
                             new Thread(new MyMessageDeleteThread(adId)).start();
                             Tracker.getInstance().event(BxEvent.SENT_DELETE).end();
                             break;
                     }
                 } else if (currentType == TYPE_INVERIFY) {
                     switch (clickedIndex) {
                         case 0://申诉
                             Bundle bundle = createArguments(null, null);
                             bundle.putInt("type", 1);
                             bundle.putString("adId", adId);
                             pushFragment(new FeedbackFragment(), bundle);
                             Tracker.getInstance().event(BxEvent.APPROVING_APPEAL).end();
                             break;
                         case 1://删除
                             showSimpleProgress();
                             Tracker.getInstance().event(BxEvent.APPROVING_DELETE).end();
                             new Thread(new MyMessageDeleteThread(adId)).start();
                             break;
                     }
                 } else if (currentType == TYPE_DELETED) {
                     switch (clickedIndex) {
                         case 0://恢复
                             showSimpleProgress();
                             new Thread(new MyMessageRestoreThread(adId)).start();
                             Tracker.getInstance().event(BxEvent.DELETED_RECOVER).end();
                             break;
                         case 1://彻底删除
                             showSimpleProgress();
                             new Thread(new MyMessageDeleteThread(adId)).start();
                             Tracker.getInstance().event(BxEvent.DELETED_DELETE).end();
                             break;
                     }
                 }
 
             }
         }).setNegativeButton(
                 "取消", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     private void doRefresh(int pay, final String adId){
         String tmpjson = null;
         ArrayList<String> requests = new ArrayList<String>();
 
         UserBean user = (UserBean) Util.loadDataFromLocate(this.getActivity(), "user");
         if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
         	Util.makeupUserInfoParams(user, requests);
         }
         requests.add("adId=" + adId);
         requests.add("rt=1");
         if(pay != 0){
             requests.add("pay=1");
         }
         String url = Communication.getApiUrl("ad_refresh", requests);
         try {
             json = Communication.getDataByUrl(url, true);
         } catch (UnsupportedEncodingException e) {
             QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
             hideProgress();
         } catch (IOException e) {
             QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
             hideProgress();
         } catch (Communication.BXHttpException e){
 
         }
 
         if(json == null){
             Toast.makeText(getActivity(), "刷新失败，请稍后重试！", 0).show();
         }
         try {
             JSONObject jb = new JSONObject(json);
             JSONObject js = jb.getJSONObject("error");
             String message = js.getString("message");
             int code = js.getInt("code");
             if (code == 0) {
                 doRefresh(0, adId);
                 Toast.makeText(getActivity(), message, 0).show();
             }else if(2 == code){
                 hideProgress();
                 new AlertDialog.Builder(getActivity()).setTitle("提醒")
                         .setMessage(message)
                         .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 showSimpleProgress();
                                 doRefresh(1, adId);
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
                 hideProgress();
                 Toast.makeText(getActivity(), message, 0).show();
             }
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
 
     /**
      * 恢复 ad
      */
 	class MyMessageRestoreThread implements Runnable{
 		private String id;
 		public MyMessageRestoreThread(String id){
 			this.id = id;
 		}
 		
 		@Override
 		public void run(){
 			json = "";
 			String apiName = "ad_undelete";
 			ArrayList<String> list = new ArrayList<String>();
 			if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
 				Util.makeupUserInfoParams(user, list);
 			}
 			list.add("adId=" + id);
 			list.add("rt=1");
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url, true);
 				if (json != null) {
 					sendMessage(MSG_RESTORE_POST_SUCCESS, id);
 				} else {
 					sendMessage(MSG_RESTORE_POST_FAIL, null);
 				} 
 				return;
 
 			} catch (UnsupportedEncodingException e) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 			} catch (IOException e) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 			} catch (Communication.BXHttpException e){
 				
 			}
 			hideProgress();
 		}
 	}
 
     /**
      * 删除 ad
      */
 	class MyMessageDeleteThread implements Runnable {
 		private String id;
 		private int currentType = TYPE_MYPOST;
 
 		public MyMessageDeleteThread(String id){
 			this.id = id;
 			this.currentType = PersonalPostFragment.this.currentType;
 		}
 
 		@Override
 		public void run() {
 			json = "";
 			String apiName = "ad_delete";
 			ArrayList<String> list = new ArrayList<String>();
 			if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
 				Util.makeupUserInfoParams(user, list);
 			}
 			list.add("adId=" + id);
 			list.add("rt=1");
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url, true);
 				if (json != null) {
 					Message msg = handler.obtainMessage();
 					msg.obj = id;
 					msg.arg1 = currentType;
 					msg.what = MSG_DELETE_POST_SUCCESS;
 					handler.sendMessage(msg);
 				} else {
 					sendMessage(MSG_DELETE_POST_FAIL, null);
 				} 
 				return;
 
 			} catch (UnsupportedEncodingException e) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 			} catch (IOException e) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 			} catch (Communication.BXHttpException e){
 				
 			}
 			hideProgress();
 		}
 	}
 
 //	@Override
 //	public void handleRightAction(){
 //		if(-1 == buttonStatus){
 ////		btnEdit.setBackgroundResource(R.drawable.btn_clearall);
 ////			if(this.m_viewInfoListener != null){
 //				TitleDef title = getTitleDef();
 //				title.m_rightActionHint = "完成";
 //
 //				refreshHeader();
 ////				m_viewInfoListener.onTitleChanged(title);
 ////			}
 //			if(adapter != null){
 //				adapter.setHasDelBtn(true);
 //			}
 //			buttonStatus = 0;
 //		}
 //		else{
 ////			btnEdit.setBackgroundResource(R.drawable.btn_search);
 ////			if(this.m_viewInfoListener != null){
 //				TitleDef title = getTitleDef();
 //				title.m_rightActionHint = "编辑";
 //				refreshHeader();
 ////				title.m_leftActionHint = "更新";
 ////				m_viewInfoListener.onTitleChanged(title);
 ////			}
 //			adapter.setHasDelBtn(false);
 //			buttonStatus = -1;
 //		}
 //		if(adapter != null)
 //		{
 //			adapter.notifyDataSetChanged();
 //			lvGoodsList.invalidateViews();
 //		}
 //	}
 	
 
 
 	@Override
 	public void initTitle(TitleDef title){
 		title.m_leftActionHint = "返回";
 		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
 //		title.m_rightActionHint = "编辑";
 		if(currentType == TYPE_MYPOST){
 			title.m_title = "已发布的信息";
 		}else if(currentType == TYPE_INVERIFY){
 			title.m_title = "审核中";
 		}else if(currentType == TYPE_DELETED){
 			title.m_title = "已删除";
 		}		
 		title.m_visible = true;
 	}
 	
 	@Override
 	public void initTab(TabDef tab){
 		tab.m_visible = true;
 		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MINE;
 	}
 
 	@Override
 	public void onRefresh() {
 		List<String> params = new ArrayList<String>();
 		if(user != null){
 			params.add("userId=" + user.getId());
 		}		
 		if(currentType == TYPE_MYPOST){
 			if(bundle != null && bundle.getString("lastPost") != null){
 				params.add("newAdIds=" + bundle.getString("lastPost"));
 			}
 			params.add("status=0");
 		}
 		else if(currentType == TYPE_INVERIFY){
 			params.add("status=1");
 		}
 		else if(currentType == TYPE_DELETED){
 			params.add("status=2");
 		}
 		glLoader.setRows(1000);
 		glLoader.setParams(params);
 		int msg = (currentType == TYPE_MYPOST) ? MSG_MYPOST : (this.currentType == TYPE_INVERIFY ? MSG_INVERIFY : MSG_DELETED);
 		glLoader.startFetching(true, msg, msg, msg, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_UNCACHEABLE);
 	}
 	
 	@Override
 	public void onFragmentBackWithData(int message, Object obj){
 		if(GoodDetailFragment.MSG_ADINVERIFY_DELETED == message){
 			if(obj != null){
 				if(this.listInVerify != null){
 					for(int i = 0; i < listInVerify.size(); ++ i){
 						if(listInVerify.get(i).getValueByKey(EDATAKEYS.EDATAKEYS_ID).equals((String)obj)){
 							listInVerify.remove(i);
 							break;
 						}
 					}
 					if(currentType == TYPE_INVERIFY){
 						adapter.setList(listInVerify);
 						adapter.notifyDataSetChanged();
 						lvGoodsList.invalidateViews();
 					}
 				}
 			}
 		}else if(GoodDetailFragment.MSG_MYPOST_DELETED == message){
 			if(glLoader.getGoodsList() != null 
 					&& glLoader.getGoodsList().getData() != null 
 					&& glLoader.getGoodsList().getData().size() > 0){
 				if(QuanleimuApplication.getApplication().getListMyPost() == null ||
 						QuanleimuApplication.getApplication().getListMyPost().size() != glLoader.getGoodsList().getData().size()){
 					GoodsList gl = new GoodsList();
 					gl.setData(QuanleimuApplication.getApplication().getListMyPost());
 					glLoader.setGoodsList(gl);
 					adapter.setList(QuanleimuApplication.getApplication().getListMyPost());
 					adapter.notifyDataSetChanged();
 					lvGoodsList.invalidateViews();
 				}
 			}
 		}else if(PostGoodsFragment.MSG_POST_SUCCEED == message){
 				if(this.lvGoodsList != null){
 					lvGoodsList.fireRefresh();
 			}
 		}
 
 	}
 }
