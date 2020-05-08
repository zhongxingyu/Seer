 package com.t2.vas.activity.editor;
 
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import com.t2.vas.R;
 import com.t2.vas.ReminderService;
 import com.t2.vas.SharedPref;
 import com.t2.vas.activity.ABSNavigationActivity;
 import com.t2.vas.db.tables.Group;
 
 public class GroupListActivity extends ABSNavigationActivity implements OnItemClickListener, android.content.DialogInterface.OnClickListener {
 	private static final String TAG = GroupListActivity.class.getSimpleName();
 	
 	private SimpleCursorAdapter groupsAdapter;
 	private ListView listView;
 	private Cursor groupsCursor;
 	private List<Long> hiddenGroupIds;
 
 	private EditText addEditText;
 	private AlertDialog addGroupDialog;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		addEditText = new EditText(this);
 		addGroupDialog = new AlertDialog.Builder(this)
 			.setTitle(R.string.add_group_title)
 			.setView(addEditText)
 			.setPositiveButton(R.string.save, this)
 			.setNegativeButton(R.string.cancel, this)
 			.create();
 		
 		this.setContentView(R.layout.list_layout);
 		
 		this.hiddenGroupIds = SharedPref.getHiddenGroups(sharedPref);
 		
 		this.setRightButtonImage(android.R.drawable.ic_menu_add);
 		
 		Group group = new Group(dbAdapter);
 		groupsCursor = group.getGroupsCursor();
 		groupsAdapter = new SimpleCursorAdapter(
 				this,
 				R.layout.list_item_1_toggle,
 				groupsCursor,
 				new String[] {
 						"title",
 						"_id",
 				},
 				new int[] {
 						R.id.text1,
 						R.id.toggleButton,
 				}
 		);
 		groupsAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
 			@Override
 			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 				int viewId = view.getId();
 				
 				if(viewId == R.id.text1) {
 					int immutable = cursor.getInt(cursor.getColumnIndex("immutable"));
 					view.setEnabled(!(immutable > 0));
 					
 				} else if(viewId == R.id.toggleButton) {
 					final long id = cursor.getLong(columnIndex);
 					
 					ToggleButton tb = (ToggleButton)view;
 					tb.setFocusable(false);
 					tb.setChecked(!hiddenGroupIds.contains(id));
 					
 					tb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 						@Override
 						public void onCheckedChanged(CompoundButton buttonView,
 								boolean isChecked) {
 							onGroupToggled(id, isChecked);
 						}});
 					return true;
 				}
 				
 				return false;
 			}
 		});
 		
 		listView = (ListView)this.findViewById(R.id.list);
 		listView.setOnItemClickListener(this);
 		listView.setAdapter(groupsAdapter);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		groupsCursor.requery();
 		groupsAdapter.notifyDataSetChanged();
 	}
 	
 	
 
 	@Override
 	protected void onBackButtonPressed() {
 		// restart the reminder service to account for the possible changes in 
 		// group visibility.
 		ReminderService.stopRunning(this);
 		ReminderService.startRunning(this);
 		
 		super.onBackButtonPressed();
 	}
 
 	@Override
 	protected void onRightButtonPresed() {
 		addEditText.setText("");
 		addGroupDialog.show();
 	}
 	
 	private void onGroupToggled(long groupId, boolean isChecked) {
 		hiddenGroupIds.remove(groupId);
 		if(!isChecked) {
 			hiddenGroupIds.add(groupId);
 		}
 		SharedPref.setHiddenGroups(sharedPref, hiddenGroupIds);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		Group group = new Group(dbAdapter);
 		group._id = arg3;
 		group.load();
 		
 		if(group.immutable > 0) {
 			Toast.makeText(this, R.string.group_immutable_message, Toast.LENGTH_LONG).show();
 			return;
 		}
 		
 		Intent i = new Intent(this, GroupActivity.class);
 		i.putExtra(GroupActivity.EXTRA_BACK_BUTTON_TEXT, getString(R.string.back_button));
 		i.putExtra(GroupActivity.EXTRA_GROUP_ID, arg3);
 		this.startActivityForResult(i, 123);
 	}
 
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		if(dialog == addGroupDialog) {
 			if(which == AlertDialog.BUTTON_POSITIVE) {
 				Group g = new Group(dbAdapter);
				g.title = addEditText.getText().toString();
 				g.save();
 				groupsCursor.requery();
 				groupsAdapter.notifyDataSetChanged();
 			}
 		}
 	}
 }
