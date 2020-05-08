 package com.github.hean01.workflowassistant;
 
 import java.lang.Thread;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.content.Intent;
 import android.content.Context;
 import android.content.ServiceConnection;
 import android.content.ComponentName;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.LayoutInflater;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.TextView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ArrayAdapter;
 import android.widget.AdapterView;
 
 import android.util.Log;
 
 public class WFAManagerActivity extends Activity
 {
     private final static String TAG = "WFAManagerActivity";
     private final static int MENU_OPTION_PREFERENCES = 0;
     private final static int MENU_OPTION_PROGRESS = 1;
     private final static int MENU_OPTION_NEW = 2;
 
     private final static int MENU_ITEM_START = 0;
     private final static int MENU_ITEM_EDIT = 1;
     private final static int MENU_ITEM_DELETE = 2;
 
     private WFAService _service;
     private boolean _serviceIsBound;
 
     private ServiceConnection _serviceConn = new ServiceConnection()
     {
 	public void onServiceConnected(ComponentName className, IBinder binder) {
 	    _service = ((WFAService.WFAServiceBinder) binder).getService();
 	    Log.i(TAG, "Connected to service.");
 	    setContentView(R.layout.manager);
 
 	    Workflow[] workflows = _service.manager().workflows();
 	    WorkflowAdapter adapter = new WorkflowAdapter(WFAManagerActivity.this,
 							  R.layout.workflow_list_item, workflows);
 	    ListView lv = (ListView)findViewById(R.id.workflow_list);
 	    lv.setAdapter(adapter);
 	    registerForContextMenu(lv);
 	}
 
 	public void onServiceDisconnected(ComponentName className) {
 	    _service = null;
 	    Log.i(TAG, "Disconnected from service.");
 	}
     };
 
     /** Workflow adapter */
     public class WorkflowAdapter extends ArrayAdapter<Workflow>
     {
 	private Context _context;
 	private int _layoutResourceId;
 	private Workflow _data[] = null;
 
 	public WorkflowAdapter(Context context, int layoutResourceId, Workflow[] data)
 	{
 	    super(context, layoutResourceId, data);
 	    _layoutResourceId = layoutResourceId;
 	    _context = context;
 	    _data = data;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent)
 	{
 	    View row = convertView;
 	    WorkflowHolder holder = null;
 
 	    if (row == null)
 	    {
 		LayoutInflater inflater = ((Activity)_context).getLayoutInflater();
 		row = inflater.inflate(_layoutResourceId, parent, false);
 		holder = new WorkflowHolder();
 		holder.name = (TextView)row.findViewById(R.id.title);
 		holder.description = (TextView)row.findViewById(R.id.description);
 		row.setTag(holder);
 	    }
 	    else
 		holder = (WorkflowHolder)row.getTag();
 
 	    holder.name.setText(_data[position].name());
 	    holder.description.setText(_data[position].description());
 
 	    return row;
 	}
 
 	private class WorkflowHolder
 	{
 	    TextView name;
 	    TextView description;
 	}
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 	setContentView(R.layout.splash);
 	_serviceIsBound = false;
 
 	Intent i = new Intent(this, WFAService.class);
 	startService(i);
     }
 
     @Override
     protected void onDestroy()
     {
 	super.onDestroy();
     }
 
     @Override
     protected void onPause()
     {
 	super.onPause();
 	if (_serviceIsBound)
 	{
 	    unbindService(_serviceConn);
 	    _serviceIsBound = false;
 	}
     }
 
     @Override
     protected void onResume()
     {
 	super.onResume();
 	if (!_serviceIsBound)
 	    _serviceIsBound = bindService(new Intent(this, WFAService.class),
 					  _serviceConn, Context.BIND_AUTO_CREATE);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
     {
	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	Workflow workflow = _service.manager().get(info.position);
	menu.setHeaderTitle(workflow.name());
 	if (_service.workflow() == null)
 	    menu.add(Menu.NONE, MENU_ITEM_START, 0, "Start");
 	menu.add(Menu.NONE, MENU_ITEM_EDIT, 1, "Edit");
 	menu.add(Menu.NONE, MENU_ITEM_DELETE, 2, "Delete");
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item)
     {
 	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 	switch(item.getItemId())
 	{
 	case MENU_ITEM_START:
 	    _service.runWorkflow(info.position);
 	    startActivity(new Intent(this, WFAProgressActivity.class));
 	    break;
 	case MENU_ITEM_EDIT:
 	    startActivity(new Intent(this, WFAEditorActivity.class));
 	    break;
 	case MENU_ITEM_DELETE:
 	    break;
 	default:
 	    break;
 	}
 	return true;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
 	menu.add(Menu.NONE, MENU_OPTION_NEW, 0, "New");
 	menu.add(Menu.NONE, MENU_OPTION_PROGRESS, 1, "Progress");
 	menu.add(Menu.NONE, MENU_OPTION_PREFERENCES, 2, "Preferences");
 	return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
 	switch(item.getItemId())
 	{
 	case MENU_OPTION_NEW:
 	    startActivity(new Intent(this, WFAEditorActivity.class));
 	    return true;
 
 	case MENU_OPTION_PREFERENCES:
 	    startActivity(new Intent(this, WFAPreferencesActivity.class));
 	    return true;
 
 	case MENU_OPTION_PROGRESS:
 	    startActivity(new Intent(this, WFAProgressActivity.class));
 	    return true;
 
 	default:
 	    Log.w(TAG, "Unhandled menu option " + item.getItemId() + ".");
 	}
 
 	return false;
     }
 }
