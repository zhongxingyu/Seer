 package com.dzebsu.acctrip;
 
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.view.ActionMode;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.dzebsu.acctrip.adapters.OperationsListViewAdapter;
 import com.dzebsu.acctrip.db.datasources.EventDataSource;
 import com.dzebsu.acctrip.db.datasources.OperationDataSource;
 import com.dzebsu.acctrip.models.Event;
 import com.dzebsu.acctrip.models.Operation;
 
 public class OperationListActivity extends Activity {
 
 	ActionMode mActionMode;
 
 	private int selectedItem;
 
 	private Object selectedViewTag;
 
 	private final static int SELECTION_COLOR = android.R.color.holo_red_dark;
 
 	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
 
 		@Override
 		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 			return false;
 		}
 
 		@Override
 		public void onDestroyActionMode(ActionMode mode) {
 			mActionMode = null;
 			final ListView list = (ListView) findViewById(R.id.op_list);
 			list.findViewWithTag(selectedViewTag).setBackgroundColor(
 					getApplication().getResources().getColor(android.R.color.transparent));
 			// list.clearChoices();
 			// list.getChildAt(selectedItem).setBackgroundColor(
 			// getApplication().getResources().getColor(android.R.color.transparent));
 
 		}
 
 		@Override
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 			// Inflate a menu resource providing context menu items
 			MenuInflater inflater = mode.getMenuInflater();
 			inflater.inflate(R.menu.context_action_bar_dictionary, menu);
 			return true;
 		}
 
 		@Override
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 			switch (item.getItemId()) {
 				case R.id.dic_edit:
 					onElementEdit(adapterZ.getItemId(selectedItem - 1));
 					mode.finish(); // Action picked, so close the CAB
 					return true;
 				case R.id.dic_del:
 					onDeleteElement(adapterZ.getItemId(selectedItem - 1));
 					mode.finish(); // Action picked, so close the CAB
 					return true;
 				default:
 					return false;
 			}
 		}
 	};
 
 	private Event event;
 
 	// Anonymous class wanted this adapter inside itself
 	private OperationsListViewAdapter adapterZ;
 
 	// for restoring list scroll position
 	private static final String LIST_STATE = "listState";
 
 	private Parcelable mListState = null;
 
 	private boolean dataChanged = false;
 
 	@Override
 	protected void onRestoreInstanceState(Bundle state) {
 		super.onRestoreInstanceState(state);
 		mListState = state.getParcelable(LIST_STATE);
 	}
 
 	protected void onDeleteElement(final long itemId) {
 		new AlertDialog.Builder(this).setTitle(R.string.delete_dialog_title).setMessage(
 				String.format(getString(R.string.confirm_del), getString(R.string.this_op))).setIcon(
 				android.R.drawable.ic_dialog_alert).setPositiveButton(R.string.dic_del,
 				new DialogInterface.OnClickListener() {
 
 					public void onClick(DialogInterface dialog, int whichButton) {
 						new OperationDataSource(OperationListActivity.this).deleteById(itemId);
 						Intent intent = new Intent(OperationListActivity.this, OperationListActivity.class);
 						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 						intent.putExtra("toast", R.string.op_deleted);
 						intent.putExtra("eventId", event.getId());
 						startActivity(intent);
 					}
 				}).setNegativeButton(android.R.string.no, null).show();
 
 	}
 
 	protected void onElementEdit(long itemId) {
 		Intent intent = new Intent(OperationListActivity.this, EditOperationActivity.class);
 		intent.putExtra("eventId", event.getId());
 		intent.putExtra("mode", "edit");
 		intent.putExtra("opID", itemId);
 		startActivity(intent);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		long eventId = getIntent().getLongExtra("eventId", 0);
 		event = new EventDataSource(this).getEventById(eventId);
 		setContentView(R.layout.activity_operation_list);
 		ListView listView = (ListView) findViewById(R.id.op_list);
 		listView.addHeaderView(createListHeader());
 		fillOperationList();
 
 		listView.setLongClickable(true);
 		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> adapter, View view, int pos, long id) {
 				if (mActionMode != null) {
 					return false;
 				}
 				selectedViewTag = view.getTag();
 				mActionMode = startActionMode(mActionModeCallback);
 				selectedItem = pos;
 				view.setBackgroundColor(getApplication().getResources().getColor(SELECTION_COLOR));
 				return true;
 			}
 		});
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
 
 				if (mActionMode != null) mActionMode.finish();
 
 			}
 		});
 
 		// listView.setOnItemClickListener(new OnItemClickListener() {
 		//
 		// public void onItemClick(AdapterView<?> parent, View view, int
 		// position, long id) {
 		// Intent intent = new Intent(OperationListActivity.this,
 		// OperationDetailsActivity.class);
 		// intent.putExtra("evName", event.getName());
 		// intent.putExtra("opID", id);
 		// startActivity(intent);
 		// }
 		// });
 
 		((Button) findViewById(R.id.op_new_btn)).setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				onNewOperation();
 
 			}
 		});
 		// add filter_Event_Edittext for events names
 		SearchView eventsFilter = (SearchView) findViewById(R.id.uni_op_searchView);
 		eventsFilter.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
 
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (mActionMode != null) mActionMode.finish();
 
 			}
 		});
 		eventsFilter.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
 
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 				return false;
 			}
 
 			@Override
 			public boolean onQueryTextChange(String newText) {
 
 				OperationListActivity.this.adapterZ.getFilter().filter(newText);
 				return true;
 			}
 		});
 		// end
 		setupActionBar();
 		Intent intent = getIntent();
 		if (intent.hasExtra("toast"))
 			Toast.makeText(getApplicationContext(), intent.getIntExtra("toast", R.string.not_message),
 					Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.operation_list, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case android.R.id.home:
 				Intent intent = new Intent(this, EventListActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(intent);
 				return true;
 			case R.id.delete_event:
 				onDeleteEvent(item.getActionView());
 				return true;
 			case R.id.event_edit:
 				onEventEdit(item.getActionView());
 				return true;
 			case R.id.open_dictionaries:
 				onOpenDictionaries();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void onOpenDictionaries() {
 		Intent intent = new Intent(this, DictionaryActivity.class);
 		startActivity(intent);
 	}
 
 	private void onEventEdit(View actionView) {
 		Event ev = new EventDataSource(this).getEventById(event.getId());
 		Intent intent = new Intent(this, EditEventActivity.class);
 		intent.putExtra("edit", 0);
 		intent.putExtra("id", event.getId());
 		intent.putExtra("eventName", ev.getName());
 		intent.putExtra("eventDesc", ev.getDesc());
 		// intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 		startActivity(intent);
 	}
 
 	public void onDeleteEvent(View view) {
 		long ops = new OperationDataSource(this).getCountByEventId(event.getId());
 		String message = ops > 0 ? String.format(getString(R.string.ev_used_by_ops), new EventDataSource(this)
 				.getEventById(event.getId()).getName(), ops) : String.format(getString(R.string.confirm_del),
 				new EventDataSource(this).getEventById(event.getId()).getName());
 		new AlertDialog.Builder(this).setTitle(R.string.delete_dialog_title).setMessage(message).setIcon(
 				android.R.drawable.ic_dialog_alert).setPositiveButton(R.string.dic_del,
 				new DialogInterface.OnClickListener() {
 
 					public void onClick(DialogInterface dialog, int whichButton) {
 						EventDataSource dataSource = new EventDataSource(OperationListActivity.this);
 						dataSource.delete(event.getId());
 						Intent intent = new Intent(OperationListActivity.this, EventListActivity.class);
 						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 						// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 						intent.putExtra("toast", R.string.event_deleted);
 						startActivity(intent);
 					}
 				}).setNegativeButton(android.R.string.no, null).show();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();

 		fillOperationList();
 		if (mListState != null) ((ListView) findViewById(R.id.op_list)).onRestoreInstanceState(mListState);
 		mListState = null;
 	}
 
 	// scroll position saving
 	@Override
 	protected void onSaveInstanceState(Bundle state) {
 		super.onSaveInstanceState(state);
 		mListState = ((ListView) findViewById(R.id.op_list)).onSaveInstanceState();
 		state.putParcelable(LIST_STATE, mListState);
 		setupActionBar();
 	}
 
 	public void onNewOperation() {
 		Intent intent = new Intent(this, EditOperationActivity.class);
 		intent.putExtra("eventId", event.getId());
 		intent.putExtra("mode", "new");
 		// intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 		startActivity(intent);
 	}
 
 	private void fillOperationList() {
 		if (adapterZ == null || dataChanged) {
 			dataChanged = false;
 			OperationDataSource dataSource = new OperationDataSource(this);
 			List<Operation> operations = dataSource.getOperationListByEventId(event.getId());
 			adapterZ = new OperationsListViewAdapter(this, operations);
 
 		}
 		ListAdapter adapter = adapterZ;
 		ListView listView = (ListView) findViewById(R.id.op_list);
 		fillEventInfo(event.getId());
 		listView.setAdapter(adapter);
 		OperationListActivity.this.adapterZ.getFilter().filter(
 				((SearchView) findViewById(R.id.uni_op_searchView)).getQuery());
 	}
 
 	public View createListHeader() {
 		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		return inflater.inflate(com.dzebsu.acctrip.R.layout.operations_header, null, false);
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 			getActionBar().setTitle(R.string.event_title);
 			getActionBar().setHomeButtonEnabled(true);
 		}
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		dataChanged = true;
 	}
 
 	@Override
 	public void onPause() {
 
 		super.onPause();
 		if (mActionMode != null) mActionMode.finish();
 	}
 
 	public void fillEventInfo(long eventId) {
 		((TextView) findViewById(R.id.op_name_tv)).setText(event.getName());
 		((TextView) findViewById(R.id.op_desc_tv)).setText(event.getDesc());
 		((TextView) findViewById(R.id.op_event_id)).setText(getString(R.string.op_event_id) + String.valueOf(eventId));
 		OperationDataSource opdata = new OperationDataSource(this);
 		((TextView) findViewById(R.id.op_total_ops)).setText(getString(R.string.op_total_ops)
 				+ opdata.getCountByEventId(eventId));
 		((TextView) findViewById(R.id.op_all_expenses)).setText(opdata.getSumByEventId(eventId) + " !glob_curr");
 	}
 
 }
