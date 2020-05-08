 package com.phdroid.smsb.activity;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 import android.content.Intent;
 import android.view.Menu;
 import com.phdroid.smsb.R;
 import com.phdroid.smsb.SmsPojo;
 import com.phdroid.smsb.activity.base.ActivityBase;
 import com.phdroid.smsb.application.ApplicationController;
 import com.phdroid.smsb.application.NewSmsEvent;
 import com.phdroid.smsb.application.NewSmsEventListener;
 import com.phdroid.smsb.exceptions.ApplicationException;
 import com.phdroid.smsb.storage.IMessageProvider;
 import com.phdroid.smsb.storage.MessageProviderHelper;
 import com.phdroid.smsb.storage.SmsAction;
 
 import java.util.Hashtable;
 import java.util.List;
 
 public class BlockedSmsListActivity extends ActivityBase {
 
 	private SmsPojoArrayAdapter smsPojoArrayAdapter;
 	private ListView lv;
 
 	/**
 	 * Called when the activity is first created.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		lv = (ListView) findViewById(R.id.messagesListView);
 
 		ApplicationController app = (ApplicationController)this.getApplicationContext();
 		app.attachNewSmsListener(new NewSmsEventListener() {
 			@Override
 			public void onNewSms(NewSmsEvent newSmsEvent) {
 				Log.v(this.getClass().getSimpleName(), "onNewSms");
 				MessageProviderHelper.invalidCache();
 				dataBind();
 			}
 		});
 
 		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Intent intent = new Intent(BlockedSmsListActivity.this, ViewMessageActivity.class);
 				Bundle b = new Bundle();
 				//final int itemId = position;
 				b.putInt("position", position);
 				b.putLong("id", id);
 				intent.putExtras(b);
 				getMessageProvider().performActions();
 				startActivity(intent);
 			}
 		});
 	}
 
 	protected IMessageProvider getMessageProvider() {
 		 return MessageProviderHelper.getMessageProvider(getContentResolver());
 	}
 
 	@Override
 	public void dataBind() {
 		super.dataBind();
 		List<SmsPojo> messages = getMessageProvider().getMessages();
 		smsPojoArrayAdapter = new SmsPojoArrayAdapter(this, R.layout.main_list_item, messages);
 		lv.setAdapter(smsPojoArrayAdapter);
 		smsPojoArrayAdapter.notifyDataSetChanged();
 		updateNoMessagesTextView();
 		updateTitle();
 		Log.v(this.getClass().getSimpleName(), "DataBind");
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		dataBind();
 		processUndoButton();
 		updateTitle();
 		updateNoMessagesTextView();
 		Log.v(this.getClass().getSimpleName(), "Start");
 	}
 
 	private void processUndoButton() {
 		Hashtable<SmsPojo, SmsAction> actions = getMessageProvider().getActionMessages();
 		if(actions.size() > 0){
 			Button b = (Button)findViewById(R.id.undoButton);
 			String action = "edited";
 			if(!actions.contains(SmsAction.MarkedAsNotSpam)){
 				action = "deleted";
 			}
 			if(!actions.contains(SmsAction.Deleted)){
 				action = "marked as not spam";
 			}
 			b.setText(
 				String.format(
 						"%s message%s %s. (Undo)",
 						Integer.toString(actions.size()),
 						actions.size() > 1 ? "s were" : " was",
 						action
 					));
 			LinearLayout l = (LinearLayout)findViewById(R.id.buttonLayout);
 			l.setVisibility(View.VISIBLE);
 		}
 		else{
 			LinearLayout l = (LinearLayout)findViewById(R.id.buttonLayout);
 			l.setVisibility(View.GONE);
 		}
 	}
 
 	private void updateTitle() {
 		setTitle(R.string.app_name);
 		setTitle(String.format(
 					"%s%s",
 					getTitle().toString(),
 					getMessageProvider().getUnreadCount() > 0 ?
 						String.format(" (%s)", Integer.toString(getMessageProvider().getUnreadCount())) : ""));
 	}
 
 	private void updateNoMessagesTextView(){
 		List<SmsPojo> messages = getMessageProvider().getMessages();
 		TextView noMessages = (TextView)findViewById(R.id.no_messages_info);
 		if(messages.size() == 0){
 			noMessages.setVisibility(View.VISIBLE);
 		} else {
 			noMessages.setVisibility(View.GONE);
 		}
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		if(getMessageProvider().getMessages().size() == 0){
 			MenuItem item = menu.findItem(R.id.delete_all_item);
 			item.setEnabled(false);
 			item = menu.findItem(R.id.select_many_item);
 			item.setEnabled(false);
 		}
 		else {
 			MenuItem item = menu.findItem(R.id.delete_all_item);
 			item.setEnabled(true);
 			item = menu.findItem(R.id.select_many_item);
 			item.setEnabled(true);
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.list_menu, menu);
 		setMenuBackground();
 		return true;
 	}
 
 	protected void setMenuBackground(){
 		getLayoutInflater().setFactory( new LayoutInflater.Factory() {
 
 			@Override
 			public View onCreateView ( String name, Context context, AttributeSet attrs ) {
 				if ( name.equalsIgnoreCase( "com.android.internal.view.menu.IconMenuItemView" ) ) {
 					try {
 						LayoutInflater f = getLayoutInflater();
 						final View view = f.createView( name, null, attrs );
 						new Handler().post( new Runnable() {
 							public void run () {
 								view.setBackgroundResource( R.drawable.menu_item);
 							}
 						} );
 						return view;
 					}
 					catch ( InflateException e ) { /*ignore*/ }
 					catch ( ClassNotFoundException e ) { /*ignore*/ }
 				}
 				return null;
 			}
 		});
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 			case R.id.settings_item:
 				Intent intent = new Intent(BlockedSmsListActivity.this, SettingsActivity.class);
 				getMessageProvider().performActions();
 				startActivity(intent);
 				return true;
 			case R.id.select_many_item:
 				Intent smIntent = new Intent(BlockedSmsListActivity.this, SelectManyActivity.class);
 				getMessageProvider().performActions();
 				startActivity(smIntent);
 				return true;
 			case R.id.delete_all_item:
 				getMessageProvider().performActions();
 				getMessageProvider().deleteAll();
 				smsPojoArrayAdapter.notifyDataSetChanged();
 				processUndoButton();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void undo(View view) {
 		getMessageProvider().undo();
 		LinearLayout l = (LinearLayout)findViewById(R.id.buttonLayout);
 		l.setVisibility(View.GONE);
		smsPojoArrayAdapter.notifyDataSetChanged();
 		updateTitle();
 	}
 }
