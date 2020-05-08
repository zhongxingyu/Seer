 package com.t2.vas.activity.editor;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Adapter;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 
 import com.t2.vas.R;
 import com.t2.vas.activity.ABSNavigationActivity;
 import com.t2.vas.db.tables.Group;
 import com.t2.vas.db.tables.Scale;
 import com.t2.vas.view.SeparatedListAdapter;
 
 public class GroupActivity extends ABSNavigationActivity implements
 		OnItemClickListener, DialogInterface.OnClickListener {
 	public static final String TAG = GroupActivity.class.getSimpleName();
 
 	public static final String EXTRA_GROUP_ID = "group_id";
 	private static final int DELETE_GROUP = 1453;
 	private Group group;
 	private ListView listView;
 	private SeparatedListAdapter listAdapter;
 	private SimpleAdapter generalAdapter;
 	private SimpleCursorAdapter scaleAdapter;
 	private Cursor scalesCursor;
 
 	private AlertDialog addDialog;
 	private ViewGroup addScaleLayout;
 	private AlertDialog editDialog;
 
 	private EditText addMinLabel;
 	private EditText addMaxLabel;
 	private long editScaleId;
 
 	private EditText renameEditText;
 
 	private AlertDialog renameDialog;
 	private AlertDialog deleteGroupDialog;
 
 	private AlertDialog addGatewayDialog;
 
 	private AlertDialog deleteScaleDialog;
 
 	private ViewGroup editScaleLayout;
 
 	private EditText editMinLabel;
 
 	private EditText editMaxLabel;
 
 	private AlertDialog copyDialog;
 
 	private SimpleCursorAdapter copyScalesAdapter;
 
 	private Cursor copyScalesCursor;
 
 	private static final int ADD_SCALE_ACTIVITY = 365;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		copyScalesCursor = new Scale(dbAdapter).getUniqueScalesCursor();
 		this.startManagingCursor(copyScalesCursor);
 		copyScalesAdapter = new SimpleCursorAdapter(
 				this, 
 				R.layout.list_item_2_inline, 
 				copyScalesCursor,
 				new String[] {
 						"max_label",
 						"min_label",
 				}, 
 				new int[] {
 						R.id.text1,
 						R.id.text2,
 				}
 		);
 
 		addScaleLayout = (ViewGroup) this.getLayoutInflater().inflate(
 				R.layout.add_edit_scale_activity, null);
 		addMinLabel = (EditText) addScaleLayout.findViewById(R.id.minLabel);
 		addMaxLabel = (EditText) addScaleLayout.findViewById(R.id.maxLabel);
 
 		editScaleLayout = (ViewGroup) this.getLayoutInflater().inflate(
 				R.layout.add_edit_scale_activity, null);
 		editMinLabel = (EditText) editScaleLayout.findViewById(R.id.minLabel);
 		editMaxLabel = (EditText) editScaleLayout.findViewById(R.id.maxLabel);
 
 		renameEditText = new EditText(this);
 		renameDialog = new AlertDialog.Builder(this)
 				.setTitle(R.string.edit_group_title).setView(renameEditText)
 				.setPositiveButton(R.string.save, this)
 				.setNegativeButton(R.string.cancel, this).create();
 		deleteGroupDialog = new AlertDialog.Builder(this)
 				.setTitle(R.string.delete)
 				.setMessage(R.string.delete_category_confirm)
 				.setPositiveButton(R.string.yes, this)
 				.setNegativeButton(R.string.no, this).create();
 		deleteScaleDialog = new AlertDialog.Builder(this)
 				.setTitle(R.string.delete)
 				.setMessage(R.string.delete_scale_message)
 				.setPositiveButton(R.string.yes, this)
 				.setNegativeButton(R.string.no, this).create();
 		addGatewayDialog = new AlertDialog.Builder(this)
 				.setTitle(R.string.add_scale)
 				.setItems(R.array.add_scale_gateway_options, this)
 				.setNegativeButton(R.string.cancel, this).create();
 		addDialog = new AlertDialog.Builder(this).setTitle(R.string.add_scale)
 				.setPositiveButton(R.string.save, this)
 				.setNegativeButton(R.string.cancel, this)
 				.setView(addScaleLayout).create();
 		editDialog = new AlertDialog.Builder(this)
 				.setTitle(R.string.edit_scale).setView(editScaleLayout)
 				.setPositiveButton(R.string.save, this)
 				.setNegativeButton(R.string.cancel, this).create();
 		copyDialog = new AlertDialog.Builder(this)
 				.setTitle(R.string.copy_scale)
 				.setNegativeButton(R.string.cancel, this)
 				.setAdapter(copyScalesAdapter, this)
 				.create();
 
 		group = new Group(dbAdapter);
 		group._id = this.getIntent().getLongExtra(EXTRA_GROUP_ID, 0);
 		if (!group.load()) {
 			this.finish();
 			return;
 		}
 
 		this.setContentView(R.layout.list_layout);
 		this.setTitle(group.title);
 		this.setRightButtonImage(android.R.drawable.ic_menu_add);
 
 		listView = (ListView) this.findViewById(R.id.list);
 
 		ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
 		HashMap<String, Object> item;
 
 		item = new HashMap<String, Object>();
 		item.put("id", "rename");
 		item.put("title", "Rename");
 		items.add(item);
 
 		item = new HashMap<String, Object>();
 		item.put("id", "delete");
 		item.put("title", "Delete");
 		items.add(item);
 
 		generalAdapter = new SimpleAdapter(this, items, R.layout.list_item_1,
 				new String[] { "title", }, new int[] { R.id.text1, });
 
 		scalesCursor = group.getScalesCursor();
 		this.startManagingCursor(scalesCursor);
 		scaleAdapter = new SimpleCursorAdapter(
 				this,
 				R.layout.list_item_2_stacked_delete, 
 				scalesCursor,
 				new String[] { 
 						"max_label", 
 						"min_label", 
 						"_id", 
 				}, 
 				new int[] {
 						R.id.text1, 
 						R.id.text2, 
 						R.id.deleteButton, 
 				}
 		);
 		scaleAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
 			@Override
 			public boolean setViewValue(View view, Cursor cursor,
 					int columnIndex) {
 				if (view.getId() == R.id.deleteButton) {
 					final long id = cursor.getLong(columnIndex);
 					view.setFocusable(false);
 
 					view.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							onScaleDeleteButtonPressed(id);
 						}
 					});
 					return true;
 				}
 				return false;
 			}
 		});
 
 		listAdapter = new SeparatedListAdapter(this);
 		listAdapter.addSection(getString(R.string.general_group_operations), generalAdapter);
 		listAdapter.addSection(getString(R.string.scales_list), scaleAdapter);
 
 		listView.setAdapter(listAdapter);
 		listView.setOnItemClickListener(this);
 	}
 
 	private void onScaleDeleteButtonPressed(long scaleId) {
 		editScaleId = scaleId;
 		deleteScaleDialog.show();
 	}
 
 	@Override
 	protected void onRightButtonPresed() {
 		addGatewayDialog.show();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		Adapter adapter = listAdapter.getAdapterForItem(arg2);
 
 		if (adapter == generalAdapter) {
 			HashMap<String, Object> data = (HashMap<String, Object>) arg0
 					.getItemAtPosition(arg2);
 			String itemId = (String) data.get("id");
 
 			if (itemId.equals("rename")) {
 				renameEditText.setText(group.title);
 				renameDialog.show();
 				return;
 
 			} else if (itemId.equals("delete")) {
 				deleteGroupDialog.show();
 				return;
 			}
 
 		} else if (adapter == scaleAdapter) {
 			long id = listAdapter.getItemId(arg2);
 
 			editScaleId = id;
 			Scale scale = new Scale(dbAdapter);
 			scale._id = id;
 			scale.load();
 
 			editScaleId = id;
 			editMinLabel.setText(scale.min_label);
 			editMaxLabel.setText(scale.max_label);
 			editMinLabel.requestFocus();
 			editDialog.show();
 		}
 	}
 
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		Log.v(TAG, "CLICK!");
 		
 		if (dialog == addDialog) {
 			if (which == AlertDialog.BUTTON_POSITIVE) {
				String minLabel = addMinLabel.getText().toString().trim();
				String maxLabel = addMaxLabel.getText().toString().trim();
 				
 				Scale scale = new Scale(dbAdapter);
 				scale.min_label = minLabel;
 				scale.max_label = maxLabel;
 				scale.group_id = group._id;
 
 				scale.save();
 			}
 
 		} else if (dialog == editDialog) {
 			if (which == AlertDialog.BUTTON_POSITIVE) {
 				Scale scale = new Scale(dbAdapter);
 				scale._id = editScaleId;
 				scale.load();
				scale.min_label = editMinLabel.getText().toString();
				scale.max_label = editMaxLabel.getText().toString();
 				scale.group_id = group._id;
 
 				scale.save();
 			}
 
 		} else if (dialog == renameDialog) {
 			if (which == AlertDialog.BUTTON_POSITIVE) {
				group.title = renameEditText.getText().toString();
 				group.save();
 				setTitle(group.title);
 			}
 
 		} else if (dialog == deleteGroupDialog) {
 			if (which == AlertDialog.BUTTON_POSITIVE) {
 				group.delete();
 				this.finish();
 				return;
 			}
 
 		} else if (dialog == deleteScaleDialog) {
 			if (which == AlertDialog.BUTTON_POSITIVE) {
 				Scale scale = new Scale(dbAdapter);
 				scale._id = editScaleId;
 				scale.delete();
 			}
 
 		} else if (dialog == addGatewayDialog) {
 			addGatewayDialog.dismiss();
 
 			if (which == 0) {
 				copyDialog.show();
 				
 			} else if (which == 1) {
 				addMinLabel.setText("");
 				addMaxLabel.setText("");
 				addMinLabel.requestFocus();
 				addDialog.show();
 			}
 			
 		} else if(dialog == copyDialog) {
 			Log.v(TAG, "SEL:"+which);
 			if(which >= 0) {
 				long id = copyScalesAdapter.getItemId(which);
 				Log.v(TAG, "SEL ID:"+id);
 				Log.v(TAG, "id:"+id);
 				Scale scale = new Scale(dbAdapter);
 				scale._id = id;
 				scale.load();
 				scale.group_id = group._id;
 				scale._id = 0;
 				scale.insert();
 			}
 		}
 
 		scalesCursor.requery();
 		listAdapter.notifyDataSetChanged();
 	}
 }
