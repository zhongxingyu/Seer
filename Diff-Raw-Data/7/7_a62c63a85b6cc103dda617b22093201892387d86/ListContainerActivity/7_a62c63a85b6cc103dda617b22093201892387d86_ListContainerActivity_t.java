 package com.whatstodo.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.whatstodo.R;
 import com.whatstodo.models.List;
 import com.whatstodo.models.ListContainer;
 import com.whatstodo.persistence.ChangeListener;
 
 public class ListContainerActivity extends Activity implements OnClickListener {
 
 	ListContainer container;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_listcontainer);
 
 		Button createList = (Button) findViewById(R.id.newList);
 		createList.setOnClickListener(this);
 
 		container = ListContainer.getInstance();
 		showLists();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_list, menu);
 		return true;
 	}
 
 	// @Override
 	public void onClick(View view) {
 
 		if (view.getId() == R.id.newList) {
 			EditText editText = (EditText) findViewById(R.id.list);
 			container.addList(editText.getText().toString());
 			showLists();
 		}
 	}
 
 	/**
 	 * Shows all existing lists in a linear layout. The method gets the lists
 	 * from the class List Container.
 	 */
 	private void showLists() {
 
 		ListView listList = (ListView) findViewById(R.id.list1);
 
 		ListAdapter adapter = new ListAdapter(this, R.layout.listitem,
 				container.getLists());
 
 		listList.setAdapter(adapter);
 		listList.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				String item = ((TextView) (((FrameLayout) view).getChildAt(0)))
 						.getText().toString();
 
 				List list = container.getList(item);
 				if (item == list.getName()) {
 					Intent intent = new Intent(view.getContext(),
 							ListActivity.class);
 					Bundle bundle = new Bundle();
 					bundle.putLong("ListId", list.getId()); // List ID
 					intent.putExtras(bundle); // Put your id to your next Intent
 					startActivity(intent);
 				}
 				Toast.makeText(getBaseContext(), item, Toast.LENGTH_LONG)
 						.show();
 			}
 		});
 
 		registerForContextMenu(listList);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View view,
 			ContextMenuInfo menuInfo) {
 		if (view.getId() == R.id.list1) {
 			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 			List list = container.getLists().get(info.position);
 			menu.setHeaderTitle(list.getName());
 			String[] menuItems = getResources().getStringArray(R.array.menu);
 			for (int i = 0; i < menuItems.length; i++) {
 				menu.add(Menu.NONE, i, i, menuItems[i]);
 			}
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
 				.getMenuInfo();
 		String[] menuItems = getResources().getStringArray(R.array.menu);
 		String menuItemName = menuItems[item.getItemId()];
 		final List list = container.getLists().get(info.position);
 
 		if (menuItemName == menuItems[0]) { // Edit the chosen list
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Name");
 			builder.setMessage(list.getName());
 
 			final EditText input = new EditText(this);
 			builder.setView(input);
 
 			builder.setPositiveButton("Ok",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 
 							list.setName(input.getText().toString());
 							showLists();
 							ChangeListener.onListChange(list);
 						}
 					});
 
 			builder.setNegativeButton("Abbrechen",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 						}
 					});
 
 			AlertDialog alert = builder.create();
 			alert.show();
 
 		} else if (menuItemName == menuItems[1]) { // Delete the chosen list
 			container.deleteList(list.getId());
 			showLists();
 
 		} else if (menuItemName == menuItems[2]) { // Copy the name of the list
 			// TODO
 		}

 		TextView text = (TextView) findViewById(R.id.footer);
 		text.setText(String.format("Selected %s for item %s", menuItemName,
 				list.getName()));
 		return true;
 	}
 }
