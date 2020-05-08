 package com.sourcefish.projectmanagement;
 
 import java.io.UnsupportedEncodingException;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.entity.StringEntity;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.DatePickerDialog;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TimePicker;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.SharedPreferences;
 import android.content.Intent;
 import android.content.SharedPreferences.Editor;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.view.MenuItem;
 import com.sourcefish.tools.AsyncGet;
 import com.sourcefish.tools.AsyncServerPosts;
 import com.sourcefish.tools.Entry;
 import com.sourcefish.tools.Project;
 import com.sourcefish.tools.SourceFishConfig;
 import com.sourcefish.tools.Tasks;
 import com.sourcefish.tools.User;
 
 public class EntryActivity extends NormalLayoutActivity implements ActionBar.TabListener, ServerListenerInterface {
 
 	//private ArrayAdapter adapter = null;
 	EntryAdapter entryAdapter=null;
 	private boolean hasOpenProject = false;
 	private Project p = null;
 	private Entry openEntry = null;
 	private Entry closedEntry = null;
 	private ListView list = null;
 	
 	private UserAdapter ua;
 	private ArrayAdapter<String> au;
 
 	private List<String> usersOutProject;
 	
 	DateFormat formatDateTime=DateFormat.getDateTimeInstance();
 	Calendar dateTimeStart=Calendar.getInstance();
 	Calendar dateTimeEnd=Calendar.getInstance();
 	static final int DATE_DIALOG_ID = 0;
 	
 	public boolean onOptionsItemSelected(MenuItem menuItem)
 	{
 		if(menuItem.getItemId() == android.R.id.home)
 		{
 			onBackPressed();
 		}
 		else
 		{
 			super.onOptionsItemSelected(menuItem);
 		}
 		return false;
 	}
 	
 	@Override
 	public void onBackPressed() {
 		SharedPreferences prefs = getSharedPreferences("projectmanagement", Activity.MODE_PRIVATE);
 		Editor e = prefs.edit();
 		e.putBoolean("pressedback", true);
 		e.commit();
 		super.onBackPressed();
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.openentrylayout);
 
 		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		
 		//to pass :
 		//   intent.putExtra("MyClass", obj);  
 
 		// to retrieve object in second Activity
 		p = (Project) getIntent().getSerializableExtra("project");
 		
 		if(p == null)
 		{
 			// Geen project meegegeven met de intent... Error.
 			Toast t = Toast.makeText(getApplicationContext(), "Geen project kunnen laden.", Toast.LENGTH_LONG);
 			t.show();
 		
 			finish();
 		}
 		
 		//project info & team management tab
 		ActionBar.Tab tab = getSupportActionBar().newTab();
 		tab.setTabListener(this);
 		tab.setText("Overview");
 		tab.setTag(4);
 		getSupportActionBar().addTab(tab);
 		
 		//view tab
 		 tab = getSupportActionBar().newTab();
 		 tab.setTabListener(this);		 
 		 tab.setText("Open Entry");
 		 tab.setTag(0);
 		 getSupportActionBar().addTab(tab);
 		 
 		 //new tab
 		 tab = getSupportActionBar().newTab();
 		 tab.setTabListener(this);
 		 tab.setText("Closed Entries");
 		 tab.setTag(1);
 		 getSupportActionBar().addTab(tab);
 		 
 		 //edit tab
 		 tab = getSupportActionBar().newTab();
 		 tab.setTabListener(this);
 		 tab.setText("Start New Entry");
 		 tab.setTag(2);
 		 getSupportActionBar().addTab(tab);
 		 
 		 //add manual
 		 tab = getSupportActionBar().newTab();
 		 tab.setTabListener(this);
 		 tab.setText("Manual Entry");
 		 tab.setTag(3);
 		 getSupportActionBar().addTab(tab);
 		 
 		 getOpenEntry();
 		 
 	}
 	
 	private void getOpenEntry()
 	{
 		if(p != null)
 		{
 			boolean found = false;
 			int i = 0;
 			while(!found && i < p.entries.size())
 			{
 				Entry e = p.entries.get(i);
				String username = SourceFishConfig.getUserName(getApplicationContext());
				if(e.isOpen() && e.u.username.equals(username))
 				{
 					openEntry = p.entries.get(i);
 					found = true;
 					getSupportActionBar().setTitle("Entry: " + openEntry.entryid);
 				}
 				i++;
 			}
 		}
 	}
 	
 	public void startEntry(View v)
 	{
 		LinearLayout ll = (LinearLayout) findViewById(R.id.startTimeContainer);
 		EditText et = (EditText) findViewById(R.id.newentrydescription);
 		String description = et.getText().toString();
 		if(ll.getVisibility() != LinearLayout.GONE)
 		{			
 			new AsyncServerPosts(getApplicationContext(), Tasks.MANUALENTRY, this).execute(startEntry(description, new Timestamp(dateTimeStart.getTimeInMillis()), new Timestamp(dateTimeEnd.getTimeInMillis())));
 			dateTimeEnd = Calendar.getInstance();
 			dateTimeStart = Calendar.getInstance();
 			et.setText("");
 			updateEnd(); updateStart();
 			Toast t = Toast.makeText(getApplicationContext(), "Entry added!", Toast.LENGTH_LONG);
 			t.show();
 		}
 		else
 		{
 			new AsyncServerPosts(getApplicationContext(), Tasks.NEWENTRY, this).execute(startEntry(description, null, null));
 			getSupportActionBar().getTabAt(1).select();
 		}
 	}
 	
 	private void fillUserAdapter()
 	{
 		
 		if(p!=null)
 		{
 			UserAdapter ua=new UserAdapter(this, android.R.layout.simple_expandable_list_item_1, p.users);
 			ListView userList=(ListView) findViewById(R.id.userListView);
 			userList.setAdapter(ua);
 		}
 	}
 	
 	private void fillEntryAdapter()
 	{
 		if(p != null)
 		{
 			/*ArrayList<String> entryTitles = new ArrayList<String>();
 			for(Entry e : p.entries)
 			{
 				if(! e.isOpen())
 					entryTitles.add(e.toString());
 			}
 			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, entryTitles);
 			*/
 			
 			entryAdapter=new EntryAdapter(this, android.R.layout.simple_expandable_list_item_1,p.getClosedEntries());
 			
 			
 		}
 		list = (ListView) findViewById(R.id.entryList);
 		
 		//if(adapter != null)
 		//{
 			//list.setAdapter(adapter);
 			/**list.setOnItemClickListener(new OnItemClickListener() {
 				@Override
 				public void onItemClick(AdapterView<?> arg0, View arg1,
 						int itemId, long arg3) {
 					Intent i = new Intent(getApplicationContext(), SpecificEntryActivity.class);
 					Entry e = p.entries.get(itemId);
 					i.putExtra("entry", e);
 					startActivity(i);
 				}
 			});**/
 		//}
 		
 		if(entryAdapter!=null)
 		{
 			list.setAdapter(entryAdapter);
 			list.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 				@Override
 				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 						int position, long arg3) {
 					
 	            	AlertDialog alert = (AlertDialog) onCreateDialog(position);
 	            	alert.show();
 					return false;
 				}
 			});
 		}
 	}
 	
 	Activity a = this;
 	
 	public Dialog onCreateDialog(final int elementId) {
 		if(elementId == SourceFishConfig.THEMEDIALOG)
 		{
 			return super.onCreateDialog(elementId);
 		}
 		else{
			ArrayList<Entry> entries = p.entries;
 			Entry e = p.entries.get(elementId);
 			String username = SourceFishConfig.getUserName(getApplicationContext());
 			if(e.u.username.equals(username) || e.u.rechten > p.rechtenId)
 			{
 				String[] opties = {"Delete"};
 			    AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			    builder.setTitle("Project");
 			    builder.setItems(opties, new OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						StringEntity remove;
 						try {
 							remove = new StringEntity("{\"trid\":\"" + p.entries.get(elementId).entryid  + "\"}");
 					    	
 					    	remove.setContentType("application/json");
 					    	new AsyncServerPosts(a.getApplicationContext(), Tasks.DELETEENTRY, a).execute(remove);
 					    	entryAdapter.remove(p.entries.get(elementId));
					    	p.entries.remove(elementId);
 					    	
 						} catch (UnsupportedEncodingException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				});
 			    return builder.create();
 			}
 			else
 			{
 				return super.onCreateDialog(elementId);
 			}
 		}
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		int tabInt = (Integer) tab.getTag();		
 		LinearLayout ll; 
 		
 		switch (tabInt) {
 		case 0:	
 			setContentView(R.layout.openentrylayout);
 			if(openEntry != null)
 				getSupportActionBar().setTitle("Entry: " + openEntry.entryid);
 			else
 				getSupportActionBar().setTitle("No open entry");
 			
 			setDescription();
 			break;
 		case 1:
 			setContentView(R.layout.entrylayout);
 			getSupportActionBar().setTitle("All Entries");
 			fillEntryAdapter();
 			break;
 		case 2:
 			setContentView(R.layout.newentrylayout);
 			getSupportActionBar().setTitle("New entry");
 			Button btn = (Button) findViewById(R.id.makeentry);
 			if(openEntry != null)
 			{
 				Toast t = Toast.makeText(getApplicationContext(), "You can only have one active entry!", Toast.LENGTH_LONG);
 				t.show();
 				btn.setEnabled(false);
 			}
 			else
 			{
 				btn.setEnabled(true);
 			}
 			ll = (LinearLayout) findViewById(R.id.startTimeContainer);
 			ll.setVisibility(LinearLayout.GONE);
 			ll = (LinearLayout) findViewById(R.id.endTimeContainer);
 			ll.setVisibility(LinearLayout.GONE);
 			break;
 		case 3:
 			setContentView(R.layout.newentrylayout);
 			getSupportActionBar().setTitle("Manual Entry");
 			ll = (LinearLayout) findViewById(R.id.startTimeContainer);
 			ll.setVisibility(LinearLayout.HORIZONTAL);
 			ll = (LinearLayout) findViewById(R.id.endTimeContainer);
 			ll.setVisibility(LinearLayout.HORIZONTAL);
 			setTime(new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
 			break;
 		case 4:
 			setContentView(R.layout.projectoverviewlayout);
 			getSupportActionBar().setTitle("Overview");
 			ua=new UserAdapter(this,android.R.layout.simple_expandable_list_item_1, p.users);
 			list = (ListView) findViewById(R.id.userListView);
 			list.setAdapter(ua);
 			ua.setNotifyOnChange(true);
 				
 				if(p.rechtenId<3)
 				{
 					list.setOnItemLongClickListener(new OnItemLongClickListener() {
 						
 							public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int elementId,long arg3) {
 								
 								AlertDialog dg=(AlertDialog) onCreateUserDialog(elementId);
 								dg.show();
 								return true;
 							}
 					});
 				}
 			
 			TextView tv=(TextView) findViewById(R.id.textViewProjectmetadata);
 			tv.setText(p.name + " owned by:" + p.owner);
 			
 			
 			//if(rid<3){}
 			Spinner spnAddUsers=(Spinner) findViewById(R.id.spinnerAddUsers);
 			if(p.rechtenId<3)
 			{
 			
 			AsyncGet get=new AsyncGet(this);
 			get.execute("http://projecten3.eu5.org/webservice/getUsersOutProject/"+p.id);
 
 			usersOutProject=new ArrayList<String>();
 			
 			
 			try {
 				JSONObject obj=new JSONObject(get.get());
 				JSONArray arrUsers=obj.getJSONArray("users");
 				
 				for(int i=0;i<arrUsers.length();i++)
 				{
 					JSONObject user=arrUsers.getJSONObject(i);
 					usersOutProject.add(user.getString("uname"));
 					
 				}
 				
 			} catch (Exception e){
 				e.printStackTrace();
 			}
 			
 			au=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,usersOutProject);
 			spnAddUsers.setAdapter(au);
 			au.setNotifyOnChange(true);
 			}
 
 			
 			break;
 		}
 		
 	}
 	
 
 	public Dialog onCreateUserDialog(final int elementId) {
 		if(elementId == SourceFishConfig.THEMEDIALOG)
 		{
 			return super.onCreateDialog(elementId);
 		}
 		else
 		{
 			String[] options={"Remove user"};
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		    builder.setTitle(ua.getItem(elementId).username);
 		    builder.setItems(options, new DialogInterface.OnClickListener() {
 	               public void onClick(DialogInterface dialog, int which) {
 		               switch (which) {
 		               		case 0:removeProjectUser(ua.getItem(elementId).username);
 		               		break;
 		               }
 	               }
 		    });
 		    return builder.create();
 	               
 		}
 	}
 	
 	private void removeProjectUser(String username)
 	{
 		
 		AsyncServerPosts apost=new AsyncServerPosts(this, Tasks.REMOVEUSERFROMPROJECT, this);
 		try {
 			String exec="{\"username\":\"" + username + "\",\"pid\":\"" + p.id+"\"}";
 			Log.i("Poststring:",exec);
 			apost.execute(new StringEntity(exec));
 			JSONObject obj=new JSONObject(apost.get());
 			if(obj.getString("msg")!=null)
 			{
 				au.add(username);
 				boolean found=false;
 				int i=0;
 				while(!found&&i<ua.getCount())
 				{
 					if(ua.getItem(i).username==username)
 					{
 						ua.remove(ua.getItem(i));
 						found=true;
 					}
 					i++;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void addUserToProject(View view)
 	{
 		Spinner spin=(Spinner) findViewById(R.id.spinnerAddUsers);
 		AsyncServerPosts apost=new AsyncServerPosts(this, Tasks.ADDUSERTOPROJECT, this);
 		try {
 			apost.execute(new StringEntity("{\"username\":\""+ spin.getSelectedItem().toString() +"\",\"pid\":\"" + p.id+"\"}"));
 			
 			if(new JSONObject(apost.get()).getString("msg")!=null)
 			{
 				String result=spin.getSelectedItem().toString();
 				au.remove(result);
 				ua.add(new User(result, 3));
 				
 				/*finish();
 				startActivity(getIntent());*/
 			}
 			
 			//Log.i("result:",apost.get());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void setTime(Timestamp tstart, Timestamp tend)
 	{
 		updateEnd();
 		updateStart();
 	}
 	
 	private void setDescription()
 	{
 		if(openEntry != null)
 		{
 			hasOpenProject = true;
 			ImageButton ib = (ImageButton) findViewById(R.id.stop_image_button);
 			ib.setImageResource(R.drawable.stop_icon);
 			TextView tv = (TextView) findViewById(R.id.open_entry_description);
 			String description = "Description: \n";
 			description += openEntry.description;
 			description += "\n\nOwner: ";
 			description += openEntry.u.username;
 			description += "\n\nStart: ";
 			Timestamp ts = openEntry.start;
 			description += ts.toString();
 			tv.setText(description);
 		}
 		else
 		{
 			hasOpenProject = false;
 			ImageButton ib = (ImageButton) findViewById(R.id.stop_image_button);
 			ib.setImageResource(R.drawable.start_icon);
 		}
 	}
 	
 	public void stopEntry(View v)
 	{
 		if(hasOpenProject)
 		{
 			Timestamp end = new Timestamp(System.currentTimeMillis());
 			new AsyncServerPosts(getApplicationContext(), Tasks.STOPENTRY, this).execute(closeEntry(end));
 			Entry newEntry = openEntry;
 			newEntry.end = end;
			ArrayList<Entry> entries = p.entries;
 			p.entries.add(newEntry);
 			openEntry = null;
 			Toast t = Toast.makeText(getApplicationContext(), "Entry stopped", Toast.LENGTH_LONG);
 			t.show();
 			getSupportActionBar().getTabAt(2).select();
 		}
 		else
 		{
 			getSupportActionBar().getTabAt(3).select();
 		}
 	}
 	
 	public StringEntity startEntry(String description, Timestamp end, Timestamp start)
 	{
 		StringEntity create = null;
 		Timestamp now = new Timestamp(System.currentTimeMillis());
 		try {
 			if(end == null)
 			{
 				create = new StringEntity("{\"begin\":\"" + now  + "\",\"notities\":\"" + description + "\",\"pid\":\"" + p.id + "\"}");
 				openEntry = new Entry(now, description, new User(SourceFishConfig.getUserName(getApplicationContext()), 0), "20");
 			}
 			else
 			{
 				create = new StringEntity("{\"begin\":\"" + now  + "\",\"notities\":\"" + description + "\",\"pid\":\"" + p.id + "\",\"eind\":\"" + end + "\"}");
 				closedEntry = new Entry(start, description, end, new User(SourceFishConfig.getUserName(getApplicationContext()), 0), "20");
 				p.entries.add(closedEntry);
 			}
 			create.setContentType("application/json");
 	    } catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return create;
 	}
 	
 	public StringEntity closeEntry(Timestamp end)
 	{
 		StringEntity create = null;
 		try {
 			create = new StringEntity("{\"eind\":\"" + end + "\",\"pid\":\"" + p.id + "\",\"notities\":\""+ openEntry.description +"\"}");
 	    	create.setContentType("application/json");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return create;
 	}
 
 	public void setStartTime(View v)
 	{
 		new TimePickerDialog(EntryActivity.this, tstart, dateTimeStart.get(Calendar.HOUR_OF_DAY), dateTimeStart.get(Calendar.MINUTE), true).show();
 	}
 	
 	public void setEndTime(View v)
 	{
 		new TimePickerDialog(EntryActivity.this, tend, dateTimeEnd.get(Calendar.HOUR_OF_DAY), dateTimeEnd.get(Calendar.MINUTE), true).show();
 	}
 	
 	public void setStartDate(View v)
 	{
 		new DatePickerDialog(EntryActivity.this, dstart, dateTimeStart.get(Calendar.YEAR),dateTimeStart.get(Calendar.MONTH), dateTimeStart.get(Calendar.DAY_OF_MONTH)).show();
 	}
 	
 	public void setEndDate(View v)
 	{
 		new DatePickerDialog(EntryActivity.this, dend, dateTimeEnd.get(Calendar.YEAR),dateTimeEnd.get(Calendar.MONTH), dateTimeEnd.get(Calendar.DAY_OF_MONTH)).show();
 	}
 	
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	protected void onResume() {
 		
 		SharedPreferences prefs = getSharedPreferences("projectmanagement", Activity.MODE_PRIVATE);
 		int tab = 0;
 
 		boolean isBackPress = prefs.getBoolean("pressedback", false);
 		
 		if(! isBackPress)
 		{
 			 tab = prefs.getInt("selectedentrytab", 0);
 		}
 		
 		getSupportActionBar().getTabAt(tab).select();
 		Editor e = prefs.edit();
 		e.putBoolean("pressedback", false);
 		e.commit();
 		super.onResume();
 	} 
 	
 	protected void onPause() {
 		SharedPreferences prefs = getSharedPreferences("projectmanagement", Activity.MODE_PRIVATE);
 		Editor e = prefs.edit();
 		e.putInt("selectedentrytab", getSupportActionBar().getSelectedTab().getPosition());
 		e.commit();
         super.onPause();
     }
 
 	@Override
 	public void getServerResponse(String s) {
 		JSONObject jsonObject = null;
 		try {
 			jsonObject = new JSONObject(s);
 		} catch (JSONException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		try {
 			if(jsonObject.has("trid") && openEntry != null)
 				openEntry.entryid = Integer.toString(jsonObject.getInt("trid"));
 
 			if(jsonObject.has("trid") && closedEntry != null)
 				closedEntry.entryid = Integer.toString(jsonObject.getInt("trid"));
 			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	TimePickerDialog.OnTimeSetListener tstart = new TimePickerDialog.OnTimeSetListener() {
 		
 		@Override
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			dateTimeStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			dateTimeStart.set(Calendar.MINUTE, minute);
 			updateStart();
 		}
 	};
 	
 	TimePickerDialog.OnTimeSetListener tend = new TimePickerDialog.OnTimeSetListener() {
 		
 		@Override
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			dateTimeEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			dateTimeEnd.set(Calendar.MINUTE, minute);
 			updateEnd();
 		}
 	};
 	
 	DatePickerDialog.OnDateSetListener dstart =new DatePickerDialog.OnDateSetListener() {
 		 
 			@Override
 			public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
 				dateTimeStart.set(Calendar.YEAR,year);
 				dateTimeStart.set(Calendar.MONTH, monthOfYear);
 				dateTimeStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
 				updateStart();
 			}
 		};
 		
 		
 	DatePickerDialog.OnDateSetListener dend =new DatePickerDialog.OnDateSetListener() {
 			 
 				@Override
 				public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
 					dateTimeEnd.set(Calendar.YEAR,year);
 					dateTimeEnd.set(Calendar.MONTH, monthOfYear);
 					dateTimeEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);
 					updateEnd();
 				}
 			};
 			
 	private void updateStart()
 			{
 				TextView tv = (TextView)findViewById(R.id.startTimeLabel);
 				tv.setText(formatDateTime.format(dateTimeStart.getTime()));
 			}
 			
 	private void updateEnd()
 			{
 				TextView tv = (TextView)findViewById(R.id.endTimeLabel);
 				tv.setText(formatDateTime.format(dateTimeEnd.getTime()));
 			}
 }
