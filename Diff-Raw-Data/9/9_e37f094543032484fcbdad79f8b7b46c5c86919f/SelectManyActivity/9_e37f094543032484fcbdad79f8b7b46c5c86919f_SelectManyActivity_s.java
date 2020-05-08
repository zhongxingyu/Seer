 package com.phdroid.smsb.activity;
 
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.graphics.PorterDuff;
 import android.os.Bundle;
 import android.util.SparseBooleanArray;
 import android.view.View;
 import android.widget.*;
 import com.phdroid.smsb.R;
 import com.phdroid.smsb.SmsPojo;
 import com.phdroid.smsb.activity.base.ActivityBase;
 import com.phdroid.smsb.application.ApplicationController;
 import com.phdroid.smsb.application.NewSmsEvent;
 import com.phdroid.smsb.application.NewSmsEventListener;
 import com.phdroid.smsb.storage.IMessageProvider;
 import com.phdroid.smsb.storage.MessageProviderHelper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class SelectManyActivity extends ActivityBase {
 	View listFooter;
 	private ListView listView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.select_many);
 		listFooter = findViewById(R.id.listFooter);
 	}
 
 	@Override
 	public void dataBind() {
 		super.dataBind();
 		List<SmsPojo> messages = getMessageProvider().getMessages();
 		listView.setAdapter(new SmsPojoArrayAdapter(this, R.layout.select_many_list_item, messages));
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		listView = (ListView)findViewById(R.id.messagesListView);
 
 		dataBind();
 
 		ApplicationController app = (ApplicationController)this.getApplicationContext();
 		app.attachNewSmsListener(new NewSmsEventListener() {
 			@Override
 			public void onNewSms(NewSmsEvent newSmsEvent) {
 				SelectManyActivity.this.dataBind();
 			}
 		});
 
 		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				updateButtonsVisibility(listView);
 			}
 		});
 
 		setTitle(R.string.app_name);
 		setTitle(String.format(
 					"%s (%s)",
 					getTitle().toString(),
 					Integer.toString(getMessageProvider().getUnreadCount())));
 	}
 
 	protected void onSaveInstanceState(Bundle bundle){
 		super.onSaveInstanceState(bundle);
 		bundle.putBoolean("buttons", listFooter.getVisibility() == View.VISIBLE);
 	}
 
 	protected void onRestoreInstanceState(Bundle bundle){
 		super.onRestoreInstanceState(bundle);
 
 		boolean b = bundle.getBoolean("buttons", false);
 		if(b){
 			listFooter.setVisibility(View.VISIBLE);
 		} else {
 			listFooter.setVisibility(View.GONE);
 		}
 	}
 
 	private void updateButtonsVisibility(ListView lv) {
 		int size = 0;
 		for(int i =0; i < lv.getChildCount() ; i++){
 			if (lv.isItemChecked(i)){
 				size++;
 			}
 		}
 
 		if(size > 0){
 			listFooter.setVisibility(View.VISIBLE);
 		} else {
 			listFooter.setVisibility(View.GONE);
 		}
 	}
 
 	protected IMessageProvider getMessageProvider() {
 		 return MessageProviderHelper.getMessageProvider(getContentResolver());
 	}
 
 	public void deleteMessages(View view) {
 		ListView lv = (ListView)findViewById(R.id.messagesListView);
		long[] ids = lv.getCheckedItemIds();
 		getMessageProvider().delete(ids);
 		finish();
 	}
 
 	public void markMessagesAsNotSpam(View view) {
 		ListView lv = (ListView)findViewById(R.id.messagesListView);
		long[] ids = lv.getCheckedItemIds();
 		getMessageProvider().notSpam(ids);
 		finish();
 	}
 }
