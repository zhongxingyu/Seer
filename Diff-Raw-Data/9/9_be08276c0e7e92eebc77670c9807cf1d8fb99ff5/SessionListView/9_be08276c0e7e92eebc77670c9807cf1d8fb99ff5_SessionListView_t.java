 package com.quanleimu.view;
 
 import java.util.List;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Adapter;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.ChatSession;
 import com.quanleimu.widget.PullToRefreshListView;
 import com.quanleimu.adapter.SessionListAdapter;
 
 public class SessionListView extends BaseView implements View.OnClickListener, PullToRefreshListView.OnRefreshListener, OnItemClickListener{
 	private List<ChatSession> sessions = null;
 	public SessionListView(Context ctx, List<ChatSession> sessions){
 		super(ctx);
 		this.sessions = sessions;
 		init();
 	}
 	
 	private void init(){
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		this.addView(inflater.inflate(R.layout.sessionlist, null));
 
 //		PullToRefreshListView plv = (PullToRefreshListView)this.findViewById(R.id.lv_sessionlist);
 		ListView plv = (ListView)this.findViewById(R.id.lv_sessionlist);
 		SessionListAdapter adapter = new SessionListAdapter(this.getContext(), sessions);
 		plv.setAdapter(adapter);
 		plv.setOnItemClickListener(this);
 //		plv.setPullToRefreshEnabled(false);
 	}
 	
	
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		ListView plv = (ListView)this.findViewById(R.id.lv_sessionlist);
		plv.requestFocus();
	}

 	@Override
 	public void onClick(View v) {
 		switch(v.getId())
 		{
 			case R.id.btnCancel:
 				break;
 		}
 	}
 	
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_leftActionHint = "返回";
 		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
 //		title.m_rightActionHint = "编辑";
 		title.m_title = "私信";
 		title.m_visible = true;
 		return title;
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = true;
 		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MINE;
 		return tab;
 	}
 
 	@Override
 	public void onRefresh() {
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View item, int pos, long id) 
 	{
 		ChatSession session = (ChatSession) arg0.getAdapter().getItem(pos);
 		Bundle bundle = new Bundle();
 		bundle.putString("sessionId", session.getSessionId());
 		bundle.putString("receiverId", session.getOppositeId());
 		bundle.putString("adId", session.getAdId());
 		bundle.putString("adTitle", session.getAdTitle());
 		bundle.putBoolean("forceSync", true);
 		
 		m_viewInfoListener.onNewView(new TalkView(getContext(), bundle));
 	}
 }
