 package de.fhb.maus.android.todolist;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 import de.fhb.maus.android.todolist.database.TodoDatabaseAdapter;
 
 /**
  * @author Curtis & Sebastian
  * @version v0.0.8
  */
 public class TodoListActivity extends ListActivity {
 	private TodoDatabaseAdapter dbHelper;
 	private static final int ACTIVITY_CREATE = 0;
 	private static final int ACTIVITY_EDIT = 1;
 	private static final int DELETE_ID = Menu.FIRST + 1;
 	private Cursor cursor;
 	private Button ok;
 	private Button about;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.todo_list);
 
 		// TODO sort by date or other things
 		// TODO berfllige Todos  d.h. Todos mit abgelaufenem
 		// Flligkeitsdatum  sollen visuell besonders hervorgehoben werden
 		// TODO add a menu item "sort by ..." to the View
 
 		// Sets up Button for adding a ToDo
 		ok = (Button) findViewById(R.id.add);
 		ok.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				startActivity(new Intent(TodoListActivity.this,
 						TodoEditActivity.class));
 			}
 		});
 
 		// Sets up Button showing the logout Toast
 		about = (Button) findViewById(R.id.logout);
 		about.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Toast.makeText(TodoListActivity.this,
 						getResources().getString(R.string.additionalLoggedOut),
 						Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		// Divides the ToDo with a line
 		this.getListView().setDividerHeight(2);
 
 		// Helps to get our data from a database
 		dbHelper = new TodoDatabaseAdapter(this);
 		dbHelper.open();
 		fillData();
 		registerForContextMenu(getListView());
 	}
 
 	/** Create the menu based on the XML defintion */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	/** Reaction to the menu selection */
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.insert:
 			createTodo();
 			return true;
 		case R.id.about:
 			createAbout();
 			return true;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.insert:
 			createTodo();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	// Delete a Todo
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case DELETE_ID:
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 					.getMenuInfo();
 			dbHelper.deleteTodo(info.id);
 			fillData();
 			return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	private void createTodo() {
 		Intent i = new Intent(this, TodoEditActivity.class);
 		startActivityForResult(i, ACTIVITY_CREATE);
 	}
 
 	private void createAbout() {
 		Toast.makeText(TodoListActivity.this,
 				getResources().getString(R.string.additionalWrittenBy),
 				Toast.LENGTH_SHORT).show();
 	}
 
 	// A ToDo was clicked
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		Intent i = new Intent(this, TodoEditActivity.class);
 		i.putExtra(TodoDatabaseAdapter.KEY_ROWID, id);
 		// Activity returns an result if called with startActivityForResult
 
 		startActivityForResult(i, ACTIVITY_EDIT);
 	}
 
 	// Called with the result of the other activity
 	// requestCode was the origin request code send to the activity
 	// resultCode is the return code, 0 is everything is ok
 	// intend can be use to get some data from the caller
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		super.onActivityResult(requestCode, resultCode, intent);
 		fillData();
 	}
 
 	// Always called when the ToDos are shown
 	private void fillData() {
 
 		cursor = dbHelper.fetchAllTodos();
 		startManagingCursor(cursor);
 
 		String[] from = new String[] { TodoDatabaseAdapter.KEY_CATEGORY,
 				TodoDatabaseAdapter.KEY_DONE, TodoDatabaseAdapter.KEY_SUMMARY };
		int[] to = new int[] { R.id.icon, R.id.todo_row_checkBox, R.id.label };
 
 		// Now create an array adapter and set it to display using our row
 		SimpleCursorAdapter notes = new SimpleCursorAdapter(this,
 				R.layout.todo_row, cursor, from, to);
 
 		// Updating Checkbox and Icon
 		notes.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
 			// Go through Cursor Adapter and watch
 			public boolean setViewValue(View view, Cursor cursor,
 					int columnIndex) {
 
 				// Change Icon
 				int nCheckedIndex1 = (cursor
 						.getColumnIndex(TodoDatabaseAdapter.KEY_CATEGORY));
 
 				if (columnIndex == nCheckedIndex1) {
 					ImageView ico = (ImageView) view;
 					String category_type = cursor.getString((cursor
 							.getColumnIndex(TodoDatabaseAdapter.KEY_CATEGORY)));
 					// getting the actual string from the priority values
 					if (category_type.equals(getResources().getStringArray(
 							R.array.priorities)[0])) {
 						ico.setImageResource(R.drawable.ic_todoimportant);
 						return true;
 					} else {
 						ico.setImageResource(R.drawable.ic_todo);
 						return true;
 					}
 				}
 
 				// Change Checkbox
 				int nCheckedIndex2 = (cursor
 						.getColumnIndex(TodoDatabaseAdapter.KEY_DONE));
 				if (columnIndex == nCheckedIndex2) {
 					CheckBox cb = (CheckBox) view;
 					boolean bChecked = (cursor.getInt(nCheckedIndex2) != 0);
 
 					final long id = cursor.getLong(cursor
 							.getColumnIndex(TodoDatabaseAdapter.KEY_ROWID));
 					final String category = cursor.getString(cursor
 							.getColumnIndex(TodoDatabaseAdapter.KEY_CATEGORY));
 					final String summary = cursor.getString(cursor
 							.getColumnIndex(TodoDatabaseAdapter.KEY_SUMMARY));
 					final String description = cursor.getString(cursor
 							.getColumnIndex(TodoDatabaseAdapter.KEY_DESCRIPTION));
 
 					cb.setOnClickListener(new OnClickListener() {
 
 						public void onClick(View v) {
 							CheckBox mCheckBox = (CheckBox) v;
 
 							if (mCheckBox.isChecked())
 								dbHelper.updateTodo(id, category, true,
 										summary, description);
 							else
 								dbHelper.updateTodo(id, category, false,
 										summary, description);
 						}
 					});
 
 					cb.setChecked(bChecked);
 					return true;
 				}
 				return false;
 			}
 		});
 		setListAdapter(notes);
 	}
 
 	// When a ToDo Delete Menu is gonna be shown
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, DELETE_ID, 0, R.string.todo_list_delete);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (dbHelper != null) {
 			dbHelper.close();
 		}
 	}
 }
