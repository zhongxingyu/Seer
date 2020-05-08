 /*
  * Chris Card
  * Nathan Harvey
  * 10/24/12
  */
 package csci422.CandN.to_dolist;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.webkit.WebView.FindListener;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 public class ToDo extends ListActivity {
 
 	public final static String ID_EXTRA = "csci422.CandN.to_dolist._ID";
 	private Cursor model=null;
 
 	private ToDoAdapter adapter=null;
 
 	private ToDoHelper helper=null;
 
 	private EditText newTypeTask;
 	
 	private SharedPreferences prefs;
 	
 	private Handler delay;
 	
 	private static final int timeDelay = 650;
 	
 	public static final int DONE=95;//If task is more than this complete, it is done.
 	
 	private ProgressDialog pd;
 	
 	private WaitForSync syncing;
 	private SyncHelper syncHelp;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_to_do);
 		
 		delay = new Handler();
 
 		helper = new ToDoHelper(this);
 		syncHelp = new SyncHelper(this);
 		
 		FileSync.getInstance().load(syncHelp);
 		
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		initList();
 		prefs.registerOnSharedPreferenceChangeListener(prefListener); 
 		
 		initPD();
 		newTypeTask = (EditText)findViewById(R.id.newTypeTask);
 		newTypeTask.setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);
 		newTypeTask.setOnEditorActionListener(new OnEditorActionListener(){
 
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_DONE)
 				{
 					helper.insert(newTypeTask.getText().toString(), "", "", "", 0, -1);
 					newTypeTask.setText("");
 					initList();
 					findViewById(R.id.mainLayout).requestFocus();
 					return true;
 				}
 				return false;
 			}
 
 		});
 	}
 	
 	public void initPD()
 	{	
 		//inits the progress dialog with title message
 		pd = new ProgressDialog(this);
 		pd.setTitle("Syncing");
 		pd.setMessage("Please wait...");
 		pd.setIndeterminate(true);//this sets the spinning animation instead of progress
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		delay.postDelayed(new Runnable(){
 			public void run() {
 				initList();
 			}
 		}, timeDelay);
 	}
 	
 	/**
 	 * this initializes the list from the cursor
 	 */
 	private void initList()
 	{
 		if(model != null)
 		{
 			stopManagingCursor(model);
 			model.close();
 		}
 
 		model = helper.getAll(prefs.getString("sort_order", "title"));
 		startManagingCursor(model);
 
 		//sets adapter with this activity passed in a simple list item
 		adapter = new ToDoAdapter(model);
 
 		setListAdapter(adapter);
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 		helper.close();
 		FileSync.getInstance().save(syncHelp);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_to_do, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		if(item.getItemId() == R.id.add)
 		{
 			Intent i = new Intent(ToDo.this, DetailForm.class);
 			i.putExtra("csci422.CandN.to_dolist.curItem", "");
 			startActivity(i);
 			return true;
 			/*TODO Double-check*/
 		}
 		else if(item.getItemId() == R.id.settings)
 		{
 			startActivity(new Intent(this, Preferences.class));
 			return true;
 		}
 		else if(item.getItemId() == R.id.sync)
 		{
 			pd.show();
 			syncing  = new WaitForSync();
 			syncing.execute("");
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu)
 	{
 		if (FileSync.getInstance().isSyncCal() || FileSync.getInstance().isSaveFile()) 
 		{
 			menu.findItem(R.id.sync).setEnabled(true);
 		}
 		else
 		{
 			menu.findItem(R.id.sync).setEnabled(false);
 		}
 		
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public void onListItemClick(ListView list, View view, int position, long id) 
 	{
 		Intent i = new Intent(ToDo.this, DetailForm.class);
 		i.putExtra("csci422.CandN.to_dolist.curItem", String.valueOf(id));
 		startActivity(i);
 	}
 
 	 /**
 	* This is a listener for a preference change from preferences
 	*/
 	    private SharedPreferences.OnSharedPreferenceChangeListener prefListener= new SharedPreferences.OnSharedPreferenceChangeListener()
 	    {
 	    	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key)
 	    	{
 	    		if(key.equals("sort_order"))
 	    		{
 	    			initList();
 	    		}
 	    	}
 	    };
 	
 	/**
 	 * This class holds the ToDoAdapter for populating the listview with the ToDo Items.
 	 * @author Chris
 	 *
 	 */
 	class ToDoAdapter extends CursorAdapter
 	{
 
 		ToDoAdapter(Cursor c)
 		{
 			super(ToDo.this, c);
 		}
 
 		@Override
 		public void bindView(View row, Context ctxt, Cursor c)
 		{
 			ItemHolder holder = (ItemHolder)row.getTag();
 
 			holder.populateForm(c, helper);
 		}
 
 		@Override
 		public View newView(Context ctxt, Cursor c, ViewGroup parent)
 		{
 			LayoutInflater inflater = getLayoutInflater();
 			View row = inflater.inflate(R.layout.row, parent, false);
 			ItemHolder holder = new ItemHolder(row);
 
 			row.setTag(holder);
 
 			return row;
 		}
 	}
 
 	/**
 	 * This static class is used to populate the ToDoAdapter rows
 	 * @author Chris
 	 *
 	 */
 	static class ItemHolder
 	{
 
 		private TextView title = null;
 		private TextView date = null;
 		private CheckBox check = null;
 		private String ID;
 		private ToDoHelper help;
 		private ImageView view = null;
 		private Drawable progress = null;
 		
 		ItemHolder(View row)
 		{
 			title = (TextView)row.findViewById(R.id.title);
 			date = (TextView)row.findViewById(R.id.date);
 			view = (ImageView)row.findViewById(R.id.priority);
 			check = (CheckBox)row.findViewById(R.id.check);
 			check.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 				public void onCheckedChanged(CompoundButton buttonView,
 						boolean isChecked) {
 					Cursor save = help.getById(ID);
 					save.moveToFirst();
 					if (isChecked)
 					{
 						help.update(ID, help.getTitle(save), help.getAddress(save), help.getNotes(save), help.getDate(save), 100, help.getPriority(save));
 					}
 					else
 					{
 						help.update(ID, help.getTitle(save), help.getAddress(save), help.getNotes(save), help.getDate(save), 0, help.getPriority(save));
 					}
 				}
 			});
 		}
 
 		void populateForm(Cursor c, ToDoHelper helper)
 		{
 			ID = c.getString(0);
 			help = helper;
 			c = help.getById(ID);
 			c.moveToFirst();
 			title.setText(helper.getTitle(c));
 			date.setText(helper.getDate(c));
 
 			if(helper.getState(c) >= DONE)
 			{
 				check.setChecked(true);
 			}
 			else
 			{
 				check.setChecked(false);
				//BitmapFactory.decodeResource(, R.id.)
				progress = Drawable.createFromPath("/home/nathan/Documents/csci422FinalProject/To_DoList/res/drawable/highlight.png");//TODO change image
				progress.setBounds(0, (100-helper.getState(c))*40/100, 40, 40);
 				check.setBackgroundDrawable(progress);
 			}
 
 			switch(help.getPriority(c))
 			{
 				case -1:
 					view.setImageResource(R.drawable.priorityq);
 					break;
 				case 0:
 					view.setImageResource(R.drawable.prioritydot);
 					break;
 				case 1:
 					view.setImageResource(R.drawable.priority1);
 					break;
 				case 2:
 					view.setImageResource(R.drawable.priority2);
 					break;
 				default:
 					view.setImageResource(R.drawable.priorityblank);
 					break;
 			}
 		}
 	}
 	
 	/**
 	 * This async task will do waits until the user cancels or the location is found
 	 * @author Chris Card
 	 *
 	 */
 	private class WaitForSync extends AsyncTask<String, String, String>
 	{
 		@Override
 		protected String doInBackground(String... params)
 		{
 			if (FileSync.getInstance().isSyncCal())
 			{
 				FileSync.getInstance().syncWithCal(helper);
 			}
 			if (FileSync.getInstance().isSaveFile())
 			{
 				FileSync.getInstance().saveToFile(helper);
 			}
 			return "Finished";
 		}
 		
 		@Override
 		protected void onPostExecute(String result)
 		{
 			pd.dismiss();//dismiss the progress dialog
 			
 			new AlertDialog.Builder(ToDo.this).setTitle("Finished").setMessage("Sync has Finished").setNeutralButton("Ok", null).create().show();
 		}
 		
 	}
 }
