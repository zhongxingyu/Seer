 package com.quanleimu.view.fragment;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.EditText;
 import android.widget.HeaderViewListAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.ChatMessageAdapter;
 import com.quanleimu.broadcast.CommonIntentAction;
 import com.quanleimu.database.ChatMessageDatabase;
 import com.quanleimu.entity.ChatMessage;
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsDetail.EDATAKEYS;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.entity.UserProfile;
 import com.quanleimu.entity.compare.MsgTimeComparator;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.GoodsListLoader;
 import com.quanleimu.util.TrackConfig;
 import com.quanleimu.util.Tracker;
 import com.quanleimu.util.Util;
 import com.quanleimu.util.TrackConfig.TrackMobile.BxEvent;
 import com.quanleimu.util.TrackConfig.TrackMobile.Key;
 import com.quanleimu.util.TrackConfig.TrackMobile.PV;
 
 public class TalkFragment extends BaseFragment {
 	//This is only a temp solution for checking current IM session, will remove within next release. add on version 2.6
 		public static String CURRENT_RECEIVER_TRICKY = null;
 		
 		public static final int MAX_REQ_COUNT = 100;
 		private static final int MSG_GETPROFILE = 1;
 		private static final int MSG_GETTARGETICON = 2;
 		private static final int MSG_GETMYICON = 3;
 		private static final int MSG_CLOSE_PROGRESS = 4;
 		private static final int MSG_RECEIVE_MESSAGE = 5;
 		private static final int MSG_MERGE_MESSAGE = 6;
 		private static final int MSG_SEND_MESSAGE = 7;
 		private static final int MSG_REPLACE_MESSAGES = 8;
 		private static final int MSG_SCROLL_BOTTOM = 9;
 		private static final int MSG_GOODS_DETAIL_LOADED = 10;
 		
 		private String targetUserId;
 		private String adId;
 		private String adTitle = "对话";
 		private BroadcastReceiver msgListener;
 		private String sessionId;
 		private String myUserId;
 		private long lastupdateTime = 0;
 		private boolean alwaysSync;
 		private boolean isAttachedToWindow;
 		
 		private GoodsDetail goodsDetail;		
 
 		private TextView tvDesc;
 
 		private TextView tvPrice;
 
 		private TextView tvDateAndAddress;
 
 		private ImageView ivInfo;
 
 		private ViewGroup headerView;
 
 		private View lineView;
 		
 		private boolean fromAd = false;
 		
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
 			// TODO Auto-generated method stub
 			super.onCreate(savedInstanceState);
 			
 			Bundle bundle = getArguments();
 			
 			ChatMessage msg = null;
 			if (bundle != null)
 			{
 				targetUserId = bundle.getString("receiverId");
 				//FIXME: this is load from file, may cost times to load it on main thread.				
 				myUserId = Util.getMyId(getActivity()); 
 				
 				adId = bundle.getString("adId");
 				if(bundle.containsKey("receiverNick")){
 					adTitle = bundle.getString("receiverNick");
 				}
				(new Thread(new GetPersonalProfileThread(targetUserId))).start();
				(new Thread(new GetPersonalProfileThread(myUserId))).start();
 				if (bundle.containsKey("message"))
 				{
 					msg = (ChatMessage) bundle.getSerializable("message");
 					lastupdateTime = msg.getTimestamp();
 				}
 				
 				this.fromAd = bundle.getBoolean("fromAd");
 				
 				if (bundle.containsKey("sessionId"))
 				{
 					this.sessionId = bundle.getString("sessionId");
 				}
 				else if (msg != null)
 				{
 					this.sessionId = msg.getSession();//bundle.getString("session");
 				}
 				else
 				{
 					ChatMessageDatabase.prepareDB(getActivity());
 					String cachedSession = ChatMessageDatabase.getSessionId(myUserId, targetUserId, adId);
 					sessionId = cachedSession == null ? sessionId : cachedSession;
 				}
 				
 				alwaysSync = bundle.getBoolean("forceSync", false);
 				
 //				if (bundle.containsKey("adTitle"))
 //				{
 //					adTitle = bundle.getString("adTitle");
 //				}
 				
 			}
 		}
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			
 			LinearLayout root = (LinearLayout) inflater.inflate(R.layout.im_session, null);
 			
 			
 			root.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 			
 			UIControl ctrl = new UIControl();
 			final View sendBtn = (View) root.findViewById(R.id.im_send_btn);
 			sendBtn.setOnClickListener(ctrl);
 			sendBtn.setOnTouchListener(new OnTouchListener() {
 				
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					if (event.getAction() == MotionEvent.ACTION_DOWN)
 						v.setBackgroundResource(R.drawable.btn_send_on);
 					else
 						v.setBackgroundResource(R.drawable.btn_send);
 					return false;
 				}
 			});
 			
 			root.findViewById(R.id.im_input_box).setOnClickListener(ctrl);
 			
 			ChatMessageAdapter msgAdapter = new ChatMessageAdapter(Util.getMyId(getActivity()));
 			ListView listView = (ListView) root.findViewById(R.id.char_history_p);			
 			
 			// bind viewad info area
 			headerView = (ViewGroup) inflater.inflate(R.layout.item_goodslist, null);
 			tvDesc = ((TextView) headerView.findViewById(R.id.tvDes));
 			tvPrice = ((TextView) headerView.findViewById(R.id.tvPrice));
 			tvDateAndAddress = ((TextView) headerView.findViewById(R.id.tvDateAndAddress));
 			ivInfo = ((ImageView) headerView.findViewById(R.id.ivInfo));
 			//TextView tvUpdateDate = (TextView) headerView.findViewById(R.id.tvUpdateDate);
 
 			headerView.findViewById(R.id.rlListOperate).setVisibility(View.GONE);
 			
 			headerView.setPadding(16, 11, 11, 16);
 			
 			listView.addHeaderView(headerView);
 			
 			lineView = inflater.inflate(R.layout.list_line, null);
 			listView.addHeaderView(lineView, null, false);
 			lineView.setPadding(16, 0, 16, 11);
 			listView.setAdapter(msgAdapter);				
 			
 			lineView.setVisibility(View.GONE);
 			headerView.setVisibility(View.GONE);
 			
 			headerView.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					if (TalkFragment.this.fromAd)
 					{
 						TalkFragment.this.finishFragment();
 						return;
 					}
 					
 			        ArrayList<GoodsDetail> goodsArray = new ArrayList<GoodsDetail>();
 			        goodsArray.add(goodsDetail);
 			        GoodsList tempGoodsList = new GoodsList(goodsArray);
 			        
 					GoodsListLoader glLoader = new GoodsListLoader(null, handler, null, tempGoodsList);
 			        glLoader.setHasMore(false);
 			        
 					Bundle bundle = new Bundle();
 					bundle.putInt("index", 0);
 					bundle.putSerializable("loader", glLoader);
 					bundle.putBoolean("fromChat", true);
 					TalkFragment.this.pushFragment(new GoodDetailFragment(), bundle);				
 				}
 			});
 					
 			
 			initInputBox(root);
 			
 			//This message should be load from local cache.
 //			if (msg != null)
 //			{
 //				receiveAndUpdateUI(msg);
 //			}
 //			else 
 			if (alwaysSync) //No message to show and need to sync with server.
 			{
 				View loadingTip = root.findViewById(R.id.tip_loading_message);
 				loadingTip.setVisibility(View.VISIBLE);
 				TextView text = (TextView) loadingTip.findViewById(R.id.loading_more_tips);
 				text.setText(R.string.tip_loading_im_history);
 			}
 			
 			return root;
 
 		}
 		
 		public void onResume()
 		{
 			super.onResume();
 			isAttachedToWindow = true;
 			this.pv = PV.BUZZ;
 			Tracker.getInstance().pv(PV.BUZZ).append(Key.ADID, adId).end();
 			
 			//Load history or load msg from server.
 			if (sessionId == null)
 			{
 				Thread t = new Thread(new LoadSvrMsgCmd());
 				t.start();
 			}
 			else
 			{
 				Thread t = new Thread(new LoadLocalMsgCmd());
 				t.start();
 			}
 			
 			if (adId != null)
 			{
 				new Thread(new LoadGoodsInfoCmd()).start();
 			}
 			
 			registerMsgListener();
 		}
 		
 		private void registerMsgListener()
 		{
 			if (msgListener == null)
 			{
 				msgListener = new BroadcastReceiver() {
 
 					public void onReceive(Context outerContext, Intent outerIntent) {
 						if (outerIntent != null && outerIntent.hasExtra(CommonIntentAction.EXTRA_MSG_MESSAGE))
 						{
 							ChatMessage msg = (ChatMessage) outerIntent.getSerializableExtra(CommonIntentAction.EXTRA_MSG_MESSAGE);
 							if (msg.getTo().equals(myUserId))
 							{
 								receiveAndUpdateUI(msg);
 							}
 						}
 					}
 					
 				};
 			}
 			
 			getActivity().registerReceiver(msgListener, new IntentFilter(CommonIntentAction.ACTION_BROADCAST_NEW_MSG));
 			CURRENT_RECEIVER_TRICKY = targetUserId;
 		}
 		
 		protected void unregisterReceiver()
 		{
 			CURRENT_RECEIVER_TRICKY = null;
 			
 			if (msgListener != null)
 			{
 				getActivity().unregisterReceiver(msgListener);
 			}
 		}
 		
 		public void onPause()
 		{
 			super.onPause();
 			
 			this.isAttachedToWindow = false;
 			
 			unregisterReceiver();
 		}
 		
 		@Override
 		public void initTitle(TitleDef title){
 			title.m_visible = true;
 			title.m_title = adTitle;//"对话";
 			
 			title.m_leftActionHint = "返回";
 		}
 		
 		@Override
 		public void initTab(TabDef tab){
 			tab.m_visible = false;
 		}	
 		
 		
 		private ChatMessageAdapter getAdapter(View currentRoot)
 		{
 			return (ChatMessageAdapter)((HeaderViewListAdapter) ((ListView) currentRoot.findViewById(R.id.char_history_p)).getAdapter()).getWrappedAdapter();
 		}
 		
 		private void initInputBox(View parent)
 		{
 			final EditText inputBox = (EditText) parent.findViewById(R.id.im_input_box);
 			inputBox.setPadding(inputBox.getPaddingLeft(), 2, inputBox.getPaddingRight(), 2);//For nine-patch.
 			
 			final View sendBtn = (View) parent.findViewById(R.id.im_send_btn);
 			sendBtn.setEnabled(false);//Disable send by default.
 			
 			inputBox.setOnEditorActionListener(new OnEditorActionListener() {
 
 				@Override
 				public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
 					if (arg2 != null && arg2.getAction() == KeyEvent.ACTION_DOWN
 							&& arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER
 							&& arg0.getText().length() > 0)
 					{
 						sendAndUpdateUI(arg0.getText().toString());
 						arg0.setText("");
 						return true;
 					}
 					return false;
 				}});
 			
 			
 			inputBox.addTextChangedListener(new TextWatcher(){
 
 				@Override
 				public void afterTextChanged(Editable edit) {
 					
 					if (edit == null || edit.length() == 0)
 					{
 						sendBtn.setEnabled(false);
 					}
 					else
 					{
 						sendBtn.setEnabled(true);
 					}
 				}
 
 				public void beforeTextChanged(CharSequence s, int start, int count,
 						int after) {
 				}
 				
 				public void onTextChanged(CharSequence s, int start, int before,
 						int count) {
 					
 				}
 				
 			});
 			
 		}
 		
 		private void sendAndUpdateUI(final String message)
 		{
 			//First step to update UI.
 			ChatMessage msg = createMessage(message);
 			sendMessage(MSG_SEND_MESSAGE, msg);
 			
 			//Send the text to server.
 			Thread t = new Thread(new SendMsgCmd(message));
 			t.start();
 		}
 		
 		private ChatMessage createMessage(final String message)
 		{
 			ChatMessage msg = new ChatMessage();
 			msg.setMessage(message);
 			msg.setSession(this.sessionId);
 			msg.setAdId(this.adId);
 			msg.setFrom(this.myUserId);
 			msg.setTo(this.targetUserId);
 			msg.setId(System.currentTimeMillis() + "");
 			msg.setTimestamp(System.currentTimeMillis()/1000);
 			
 			return msg;
 		}
 		
 		private void receiveAndUpdateUI(final ChatMessage msg)
 		{
 			if(msg == null || !targetUserId.equals(msg.getFrom()))
 			{
 				return;
 			}
 
 			if (sessionId == null)
 			{
 				sessionId = msg.getSession();
 			}
 			
 			ChatMessageDatabase.prepareDB(getAppContext());
 			ChatMessageDatabase.updateReadStatus(msg.getId(), true);
 			
 			sendMessage(MSG_RECEIVE_MESSAGE, msg);
 		}
 		
 		private void mergeAndUpdateUI(final List<ChatMessage> list, final boolean isLocal)
 		{
 			Collections.sort(list, new MsgTimeComparator());
 			lastupdateTime = list.get(list.size()-1).getTimestamp();
 			
 			if (isLocal)
 			{
 				sendMessage(MSG_REPLACE_MESSAGES, list);
 			}
 			else
 			{
 				sendMessage(MSG_MERGE_MESSAGE, list);
 			}
 			
 			ChatMessageDatabase.prepareDB(getAppContext());
 			ChatMessageDatabase.storeMessage(list, true);
 		}
 		
 //		private void postScrollDelay()
 //		{
 //			getView().postDelayed(new Runnable() {
 //				
 //				@Override
 //				public void run() {
 //					if (getAdapter().getCount() > 0 && getView() != null)
 //					{
 //						ListView scroll = (ListView) findViewById(R.id.char_history_p);
 ////					scroll.fullScroll(ScrollView.FOCUS_DOWN);
 //						scroll.setSelection(getAdapter().getCount()-1);
 //					}
 //					
 //				}
 //			}, 200);
 //		}
 		
 		private void updateSendStatus(boolean succed)
 		{
 		}
 		
 		
 		class LoadGoodsInfoCmd implements Runnable {
 
 
 			@Override
 			public void run() {
 				String apiName = "ad_list";
 				List<String> parameters = new ArrayList<String>();
 				parameters.add("query=id:" + URLEncoder.encode(TalkFragment.this.adId));
 				parameters.add("activeOnly=0");
 				String apiUrl = Communication.getApiUrl(apiName, parameters);
 				try {
 					String jsonData = Communication.getDataByUrl(apiUrl, false);
 					GoodsList goodsList = JsonUtil.getGoodsListFromJson(jsonData);
 					if (!goodsList.getData().isEmpty()){
 						goodsDetail = goodsList.getData().get(0);
 						TalkFragment.this.sendMessage(MSG_GOODS_DETAIL_LOADED, null);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 				
 			}
 		}
 		
 		class LoadLocalMsgCmd implements Runnable {
 
 			@Override
 			public void run() {
 				List<ChatMessage> msgList = null;
 				ChatMessageDatabase.prepareDB(getAppContext());
 				
 				if (sessionId != null)
 				{
 					msgList = ChatMessageDatabase.queryMessageBySession(sessionId); 
 				}
 				
 				if (msgList != null && msgList.size() > 0)
 				{
 //					myHandler.sendEmptyMessage(MSG_CLOSE_PROGRESS);
 					sendMessage(MSG_CLOSE_PROGRESS, null);
 					mergeAndUpdateUI(msgList, true);
 				}
 				
 				if (msgList == null || msgList.size() == 0 || alwaysSync)
 				{
 					new LoadSvrMsgCmd().run();
 				}
 				
 			}
 		}
 		
 		class LoadSvrMsgCmd implements Runnable 
 		{
 			final String apiName = "read_message";
 			
 			public void run() 
 			{
 				if (myUserId == null)
 					return;
 				
 				ArrayList<String> cmdOpts = new ArrayList<String>();
 				cmdOpts.add("u_id=" + URLEncoder.encode(myUserId));
 				if (sessionId != null)
 				{
 					cmdOpts.add("session_id=" + URLEncoder.encode(sessionId));
 				}
 				else
 				{
 					cmdOpts.add("u_id_other=" + URLEncoder.encode(targetUserId));
 					cmdOpts.add("ad_id=" + URLEncoder.encode(adId));
 				}
 				
 				//FIXME: only load messages within several days. we use 10 at present.
 				if (lastupdateTime != 0)
 				{
 					cmdOpts.add("last_update_timestamp=" + URLEncoder.encode(lastupdateTime + ""));
 				}
 				cmdOpts.add("limit=" + MAX_REQ_COUNT);
 				
 				String url = Communication.getApiUrl(apiName, cmdOpts);
 				
 				try {
 					String result = Communication.getDataByUrlGet(url);
 //					JSONObject obj = new JSONObject(result);
 //					if (obj.getInt("count") > 0)
 //					{
 //						JSONArray tmp = obj.getJSONArray("data");
 //						mergeAndUpdateUI(JsonUtil.parseChatMessages(tmp), false);
 //					}
 					
 					List<ChatMessage> list2 = JsonUtil.parseChatMessagesByJackson(result);
 					if(list2 != null && list2.size() > 0){
 						mergeAndUpdateUI(list2, false);
 					}
 				}
 				catch(Throwable t)
 				{
 				}
 				finally
 				{
 //					myHandler.sendEmptyMessage(MSG_CLOSE_PROGRESS);
 					sendMessage(MSG_CLOSE_PROGRESS, null);
 				}
 			}
 		}
 		
 		class SendMsgCmd implements Runnable 
 		{
 			private final String apiName = "send_message";
 			private String message;
 			public SendMsgCmd(String messageToSend)
 			{
 				this.message = messageToSend;
 			}
 
 			@Override
 			public void run() {
 				if (myUserId == null) 
 					return;
 				
 				ArrayList<String> cmdOpts = new ArrayList<String>();
 				cmdOpts.add("u_id_from=" + URLEncoder.encode(myUserId));
 				cmdOpts.add("u_id_to=" + URLEncoder.encode(targetUserId));
 				if (adId != null)
 				{
 					cmdOpts.add("ad_id=" + URLEncoder.encode(adId));
 				}
 				cmdOpts.add("message=" + URLEncoder.encode(message));
 				if (sessionId != null)
 				{
 					cmdOpts.add("session_id=" + URLEncoder.encode(sessionId));
 				}
 				
 				String url = Communication.getApiUrl(apiName, cmdOpts);
 				
 				try {
 					String result = Communication.getDataByUrl(url,true);
 					try {
 						JSONObject json = new JSONObject(result);
 						if (sessionId == null)
 						{
 							sessionId = json.getString("session_id");
 						}
 						
 						lastupdateTime = json.getLong("timestamp");
 						
 						json.remove("u_id");
 						json.put("ad_id", adId);
 						json.put("u_id_from", myUserId);
 						json.put("u_id_to", targetUserId);
 						json.put("id", json.get("msg_id"));
 						json.remove("msg_id");
 						json.put("u_nick_from", "");
 						json.put("u_nick_to", "");
 						json.put("ad_title", "");
 						json.put("message", message);
 						
 						ChatMessage chatMsgInst = ChatMessage.fromJson(json);
 						
 						ChatMessageDatabase.prepareDB(getAppContext());
 						ChatMessageDatabase.storeMessage(chatMsgInst);
 						ChatMessageDatabase.updateReadStatus(chatMsgInst.getId(), true);
 //						messageList.add(chatMsgInst);
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 					
 					updateSendStatus(true);
 				} catch (Throwable e) {
 					updateSendStatus(false);
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		
 		class UIControl implements View.OnClickListener, View.OnTouchListener
 		{
 
 			public void onClick(View v) {
 				switch (v.getId())
 				{
 				case R.id.im_input_box:
 //					postScrollDelay();
 					sendMessage(MSG_SCROLL_BOTTOM, null);
 					break;
 				case R.id.im_send_btn:
 					if (myUserId == null)
 						return;
 					
 					EditText text = (EditText) getView().findViewById(R.id.im_input_box);
 					if (text.length() != 0)
 					{
 						sendAndUpdateUI(text.getText().toString());
 						text.setText("");
 					}
 					break;
 					
 					default:
 						break;
 				}
 			}
 
 			public boolean onTouch(View v, MotionEvent event) {
 				//TODO: check if we need hide scroll bar when scroll message list.
 //				if (v.getId() == R.id.char_history_p && event.getAction() == MotionEvent.ACTION_MOVE)
 //				{
 //				}
 				return false;
 			}
 		}
 		
 		private void scrollToBottom(View rootView)
 		{
 			ListView scroll = (ListView) rootView.findViewById(R.id.char_history_p);
 			scroll.setSelection(getAdapter(rootView).getCount()-1);
 		}
 		
 		protected final void handleMessage(Message msg, Activity activity, View rootView)
 		{
 			ChatMessageAdapter adapter = getAdapter(rootView);
 			switch (msg.what) {
 			case MSG_REPLACE_MESSAGES:
 				adapter.refreshData((List<ChatMessage>) msg.obj);				
 				scrollToBottom(rootView);
 				break;
 			case MSG_MERGE_MESSAGE:
 				getAdapter(rootView).appendData((List<ChatMessage>) msg.obj, false);
 				scrollToBottom(rootView);
 				break;
 			case MSG_RECEIVE_MESSAGE:
 			case MSG_SEND_MESSAGE:
 				getAdapter(rootView).appendData((ChatMessage) msg.obj);
 				scrollToBottom(rootView);
 				break;
 			case MSG_SCROLL_BOTTOM:
 				scrollToBottom(rootView);
 				break;
 			case MSG_GETPROFILE:
 				if(msg.obj != null){
 					TitleDef title = this.getTitleDef();
 					title.m_visible = true;
 					title.m_title = msg.obj.toString();				
 					title.m_leftActionHint = "返回";
 					if (rootView != null)
 					{
 						refreshHeader();
 					}
 
 				}
 				break;
 			case MSG_CLOSE_PROGRESS:
 			{
 				if (rootView != null)
 				{
 					rootView.findViewById(R.id.tip_loading_message).setVisibility(View.GONE);
 				}
 				break;
 			}
 			case MSG_GOODS_DETAIL_LOADED:
 			{
 				if (goodsDetail == null)
 					break;
 				
 				headerView.setVisibility(View.VISIBLE);
 				lineView.setVisibility(View.VISIBLE);
 				
 				String title = goodsDetail.getValueByKey(EDATAKEYS.EDATAKEYS_TITLE);
 				if (title == null || title.length() == 0)
 					title = goodsDetail.getValueByKey(EDATAKEYS.EDATAKEYS_DESCRIPTION);
 				String price = goodsDetail.getValueByKey(EDATAKEYS.EDATAKEYS_PRICE);
 				Date date = new Date(Long.parseLong(goodsDetail.getValueByKey(EDATAKEYS.EDATAKEYS_DATE))*1000);
 				CharSequence dateStr = DateFormat.format("MM-dd hh:mm", date);
 				String addr = goodsDetail.getValueByKey(EDATAKEYS.EDATAKEYS_AREANAME);
 				tvDesc.setText(title);
 				tvPrice.setText(price);
 				tvDateAndAddress.setText(String.format("%s %s", dateStr, addr));
 
 				String urls = goodsDetail.imageList.getSmall();
 				if (urls == null || urls.length() == 0)
 					urls = goodsDetail.imageList.getResize180();
 				if (urls == null || urls.length() == 0)
 					urls = goodsDetail.imageList.getSquare();
 				if (urls == null || urls.length() == 0)
 					urls = goodsDetail.imageList.getBig();
 				
 				if (urls != null && urls.length() > 0)
 				{
 					String[] urlList = urls.split(",");
 					String url = urlList[0];
 					
 					if (url != null && url.length() > 0)
 						SimpleImageLoader.showImg(ivInfo, url, null, getActivity(), R.drawable.home_bg_thumb_2x);	
 				}
 				break;
 			}
 			}
 		
 		}
 		
 		class GetPersonalProfileThread implements Runnable {
 			private String usrId = null;
 			public GetPersonalProfileThread(String usrId){
 				this.usrId = usrId;
 			}
 			@Override
 			public void run() {
 				if (usrId == null || usrId.equals(""))
 				{
 					return;
 				}
 				String upJson = Util.requestUserProfile(usrId);
 				if(upJson != null){
 					UserProfile up = UserProfile.from(upJson);
 					if(up != null){
 						if(usrId.equals(targetUserId)){
 							SimpleProfile profile = new SimpleProfile();
 							profile.icon = up.squareImage;
 							profile.isBoy = true;
 							if(up.gender != null && up.gender.equals("女")){
 								profile.isBoy = false;
 							}
 //							Message msg1 = myHandler.obtainMessage(MSG_GETTARGETICON, profile);
 //							myHandler.sendMessage(msg1);
 							sendMessage(MSG_GETTARGETICON, profile);
 							
 //							Message msg2 = myHandler.obtainMessage();
 //							msg2.what = MSG_GETPROFILE;
 //							msg2.obj = up.nickName;
 //							myHandler.sendMessage(msg2);
 							sendMessage(MSG_GETPROFILE, up.nickName);
 						}else{
 							SimpleProfile profile = new SimpleProfile();
 							profile.icon = up.squareImage;
 							profile.isBoy = true;
 							if(up.gender != null && up.gender.equals("女")){
 								profile.isBoy = false;
 							}	
 //							Message msg1 = myHandler.obtainMessage(MSG_GETMYICON, profile);
 							sendMessage(MSG_GETMYICON, profile);
 						}
 					}
 				}
 			}
 		}
 		
 		class SimpleProfile {
 			public String icon;
 			public boolean isBoy;
 		}
 }
