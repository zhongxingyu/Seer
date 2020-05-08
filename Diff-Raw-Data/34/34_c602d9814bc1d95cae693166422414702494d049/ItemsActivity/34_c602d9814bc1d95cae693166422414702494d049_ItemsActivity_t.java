 package edu.pitt.cs1635group3.Activities;
 
 import java.util.ArrayList;
 
import service.CustomListTask;

 import zebrafish.util.UIUtil;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import edu.pitt.cs1635group3.R;
 import edu.pitt.cs1635group3.Activities.Classes.Item;
 import edu.pitt.cs1635group3.Activities.Classes.User;
 import edu.pitt.cs1635group3.Adapters.InviteAdapter;
 import edu.pitt.cs1635group3.Adapters.ItemAdapter;
 import edu.pitt.cs1635group3.Adapters.MyListItemAdapter;
 
 public class ItemsActivity extends SherlockListActivity {
 	/* copy pasta from InsideListActivity */
 	// private CustomList list = null;
 	private static ArrayList<Item> items = null;
 	private static MyListItemAdapter adapter;
 	private RelativeLayout parentLayout;
 
 	private static final String TAG = "ItemsActivity";
 
 	private int totalItems;
 	protected Context context;
 
 	protected Button assignButton, completeButton;//,inviteButton;
 	private View buttonsHelper;
 	private static ListView lv;
 	private boolean newItems = false;
 	private static int userID;
 	private ArrayList<Integer> newItemPKs;
 
 	private CharSequence users[];
 	private static ArrayList<Item> selected;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.my_items_layout);
 		context = this;
 		userID = User.getCurrUser(context);
 		parentLayout = (RelativeLayout) findViewById(R.id.insidelist_parent); // used
 																				// for
 																				// deletions
 		newItemPKs = new ArrayList<Integer>(); // used for additions
 
 		Intent i = getIntent();
 		Bundle extras = i.getExtras();
 
 		/*
 		 * CHANGE THIS* / list = CustomList.getListByID(context,
 		 * extras.getInt("ListID")); items = CustomList
 		 * .getItemsForListByID(context, extras.getInt("ListID")); totalItems =
 		 * items.size(); / *
 		 */
 
 		/*
 		 * FIRST IMPLEMENTATION ArrayList<CustomList> listOfLists =
 		 * CustomList.getAllLists(context, userID); items = new
 		 * ArrayList<Item>(); for (CustomList l : listOfLists) {
 		 * items.addAll(CustomList.getItemsForListByID(context, l.getID())); }
 		 */
 
 		items = Item.getAllItemsForUser(context);
 
 		getSupportActionBar();
 		setTitle("My Items"); // TODO ADD TO strings.xml
 
 		selected = new ArrayList<Item>(); // track the items checked. used for
 											// button click events
 
 		/*
 		 * assignButton = (Button) findViewById(R.id.assign_button);
 		 * assignButton.setOnClickListener(new Button.OnClickListener() { public
 		 * void onClick(View v) { assign(); } }); completeButton = (Button)
 		 * findViewById(R.id.complete_button);
 		 * completeButton.setOnClickListener(new Button.OnClickListener() {
 		 * public void onClick(View v) { complete(); } }); inviteButton =
 		 * (Button) findViewById(R.id.invite_button);
 		 * inviteButton.setOnClickListener(new Button.OnClickListener() { public
 		 * void onClick(View v) { invite(); } });
 		 */
 		
 		
 		assignButton = (Button) findViewById(R.id.assign_button);
 		assignButton.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 			//	assign();
 				//UNCLAIM????
				unclaim();
 			}
 		});
 		completeButton = (Button) findViewById(R.id.complete_button);
 		completeButton.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				complete();
 			}
 		});
 		
 		buttonsHelper = (View) findViewById(R.id.buttons_helper);
 		lv = getListView();
 
 		// users = User.getUsersForDialog(context, list.getID());
 
 		adapter = new MyListItemAdapter(this, R.layout.item_row, items,
 				assignButton, completeButton);
 		lv.setTextFilterEnabled(true);
 		lv.setClickable(true);
 		setListAdapter(adapter);
 
 		lv.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
 					long id) {
 			//	completeButton.setSelected(false);
 				Item item = items.get(pos);
 
 				if (items.size() == 1) {
 					item.setPrev(context, item.getID());
 					item.setNext(context, item.getID());
 				}
 
 				Intent intent = new Intent(context, ItemActivity.class);
 				intent.putExtra("ItemID", item.getID());
 				intent.putExtra("pos", pos + 1); // Display item X of Y
 				intent.putExtra("totalItems", totalItems);
 				startActivityForResult(intent, 1);
 			}
 		});
 
 		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
 			public boolean onItemLongClick(AdapterView<?> arg0, View v,
 					int pos, long id) {
 				final View parentView = v;
 				final int position = pos;
 				final Item item = items.get(position);
 				final String itemName = item.getName();
 
 				final Button b = (Button) parentView
 						.findViewById(R.id.delete_item_button);
 				b.setVisibility(View.VISIBLE);
 
 				b.setOnClickListener(new OnClickListener() {
 
 					public void onClick(View v) {
 
 						totalItems--;
 
 						int prevPos, nextPos;
 						if (position == items.size() - 1)
 							nextPos = 0;
 						else
 							nextPos = position + 1;
 
 						if (position == 0)
 							prevPos = items.size() - 1;
 						else
 							prevPos = position - 1;
 
 						Item nextItem, prevItem;
 						nextItem = items.get(nextPos);
 						prevItem = items.get(prevPos);
 
 						int next = nextItem.getID();
 						int prev = prevItem.getID();
 
 						prevItem.setNext(context, next);
 						nextItem.setPrev(context, prev);
 
 						if (totalItems == 1) { // always this issue with one
 							// item in list
 							item.setNext(context, item.getID());
 							item.setPrev(context, item.getID());
 						}
 
 						Item.deleteItem(context, item);
 
 						UIUtil.showMessage(context, "Item deleted");
 						b.setVisibility(View.GONE);
 						parentLayout.removeView(parentView);
 						adapter.remove(item);
 						adapter.notifyDataSetChanged();
 
 					}
 				});
 
 				return true;
 			}
 		});
 
 	}// end onCreate
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == 1) { // force refresh the view
 			startActivity(getIntent());
 			finish();
 		}
 	}
 
 	/*
 	 * public void saveRename(String newName) {
 	 * 
 	 * list.updateName(context, newName); setTitle(newName);
 	 * 
 	 * }
 	 * 
 	 * public void rename() {
 	 * 
 	 * AlertDialog.Builder alert = new AlertDialog.Builder(this);
 	 * alert.setTitle("Rename List");
 	 * 
 	 * final EditText input = new EditText(this); // Set an EditText view to //
 	 * get user input alert.setView(input);
 	 * 
 	 * alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 	 * public void onClick(DialogInterface dialog, int whichButton) { String
 	 * value = input.getText().toString().trim(); saveRename(value); } });
 	 * 
 	 * alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 	 * public void onClick(DialogInterface dialog, int whichButton) { //
 	 * Canceled. } });
 	 * 
 	 * alert.show();
 	 * 
 	 * }
 	 */
 	/*
 	 * @Override public void onBackPressed() {
 	 * 
 	 * if (newItems) { list.attachItems(items); Log.i(TAG,
 	 * "Back button pressed"); int listSize = list.getItems().size(); Log.d(TAG,
 	 * "listSize = " + listSize); list.setLinks(context); } Intent in = new
 	 * Intent(); setResult(1, in);// Requestcode 1. Tell parent activity to
 	 * refresh // items.
 	 * 
 	 * finish(); super.onBackPressed(); }
 	 */
 	/*
 	 * public void assignItemsTo(String user) { selected = getSelectedItems();
 	 * 
 	 * int assignee = User.getUserByName(context, user); // this is weak - Kim
 	 * 
 	 * for (Item item : selected) { Log.i(TAG, "Assigning " +item.getName() +
 	 * " to " +assignee); item.assignTo(context, assignee); }
 	 * 
 	 * adapter.notifyDataSetChanged(); }
 	 */
 	/*
 	 * public void assign() {
 	 * 
 	 * AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	 * builder.setTitle("Assign To"); builder.setItems(users, new
 	 * DialogInterface.OnClickListener() { public void onClick(DialogInterface
 	 * dialog, int pos) { assignItemsTo((String) users[pos]); } }); AlertDialog
 	 * alert = builder.create(); alert.show(); }
 	 */
 	public void uncomplete() {
 		// TODO - test to see if all selected items are uncompleted
 		selected = getSelectedItems();
 
 		for (Item item : selected) {
 			// item.setUnCompleted(context); TODO (easy)
 		}
 
 		adapter.notifyDataSetChanged();
 	}
 
 	public void complete() {
 		selected = getSelectedItems();
 
 		for (Item item : selected) {
 			item.setCompleted(context);
 		}
 		adapter.notifyDataSetChanged();
 	}
	
	public void unclaim()
	{
		selected = getSelectedItems();
		for (Item item : selected)
		{
			item.assignTo(context, -1);
			adapter.remove(item);
		}
		adapter.notifyDataSetChanged();
	//	lv.invalidate();
	}
 
 	public ArrayList<Item> getSelectedItems() {
 		// called multiple times because there are many actions that require use
 		// of selected items
 		selected = ((MyListItemAdapter) adapter).getSelected();
 		return selected;
 	}
 
 	public static MyListItemAdapter getAdapter() { // pass to the async task so it will be updated on postExecute
 		return adapter;
 	}
 	
 	public static void selectAll() {
 		for (int i=0; i < lv.getChildCount(); i++) {
 			LinearLayout itemLayout = (LinearLayout) lv.getChildAt(i);
 			CheckBox cb = (CheckBox)itemLayout.findViewById(R.id.check);
 			cb.setChecked(true);
 		}
 		
 		adapter.selectAll();
 	}
 	
 	public static void deselectAll() {
 		for (int i=0; i < lv.getChildCount(); i++) {
 			LinearLayout itemLayout = (LinearLayout) lv.getChildAt(i);
 			CheckBox cb = (CheckBox)itemLayout.findViewById(R.id.check);
 			cb.setChecked(false);
 		}
 		
 		adapter.deselectAll();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.my_items_menu, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featuredId, MenuItem item) {
		if (item.getItemId() == R.id.menu_select_all)
		{
 			if (items.size() > 0) {
 				if (adapter.allSelected()) deselectAll();
 				else selectAll();
 			}
 			return true;
 		}
		else if (item.getItemId() == R.id.menu_refresh)
		{
			new CustomListTask().refreshLists(context);
			return false;
		}
 		/*
 		 * Intent intent; if (item.getItemId() == 0) { intent = new Intent(this,
 		 * HomeActivity.class); startActivity(intent); return true; } else if
 		 * (item.getItemId() == R.id.menu_add) { addItem(); return false; } else
 		 * if (item.getItemId() == R.id.menu_rename) { rename(); return true; }
 		 * else if (item.getItemId() == R.id.menu_manage_users) { intent = new
 		 * Intent(this, ManageListUsersActivity.class);
 		 * intent.putExtra("listID", list.getID()); startActivity(intent);
 		 * return true; } else if (item.getItemId() == R.id.menu_invite) {
 		 * intent = new Intent(this, InviteActivity.class);
 		 * startActivity(intent); return true; } else { return false; }
 		 */
 		return false;
 	}
 }
