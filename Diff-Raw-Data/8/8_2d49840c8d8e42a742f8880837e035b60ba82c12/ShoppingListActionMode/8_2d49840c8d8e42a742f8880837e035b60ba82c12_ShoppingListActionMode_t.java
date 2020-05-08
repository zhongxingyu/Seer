 package ch.unibe.ese.activities;
 
 import ch.unibe.ese.core.Item;
 import ch.unibe.ese.core.ListManager;
 import ch.unibe.ese.core.ShoppingList;
 import ch.unibe.ese.share.RegisterRequest;
 import ch.unibe.ese.share.Request;
 import ch.unibe.ese.share.RequestSender;
 import ch.unibe.ese.shoppinglist.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.ActionMode;
 import android.view.ActionMode.Callback;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 
 public class ShoppingListActionMode implements Callback {
 
 	private ListManager manager;
 	private ShoppingList selectedList;
 	private ArrayAdapter<ShoppingList> shoppingListAdapter;
 	private Activity activity;
 	private Item selectedItem;
 	private ArrayAdapter<Item> itemAdapter;
 	private boolean isList;
 	
 	public ShoppingListActionMode(ListManager manager, ShoppingList selectedList, ArrayAdapter<ShoppingList> shoppingListAdapter, Activity homeActivity) {
 		this.manager = manager;
 		this.selectedList = selectedList;
 		this.shoppingListAdapter = shoppingListAdapter;
 		this.activity = homeActivity;
 		isList = true;
 	}
 	
 	public ShoppingListActionMode(ListManager manager, Item selectedItem, ShoppingList selectedList, ArrayAdapter<Item> itemAdapter, Activity activity) {
 		this.manager = manager;
 		this.selectedItem = selectedItem;
 		this.selectedList = selectedList;
 		this.itemAdapter = itemAdapter;
 		this.activity = activity;
 		isList = false;
 	}
 	
     // Called when the action mode is created; startActionMode() was called
     @Override
     public boolean onCreateActionMode(ActionMode mode, Menu menu) {
         // Inflate a menu resource providing context menu items
         MenuInflater inflater = mode.getMenuInflater();
         inflater.inflate(R.menu.actionbar_longpress_shoppinglist, menu);
         return true;
     }
 
     // Called each time the action mode is shown. Always called after onCreateActionMode, but
     // may be called multiple times if the mode is invalidated.
     @Override
     public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
         return false; // Return false if nothing is done
     }
 
     // Called when the user selects a contextual menu item
     @Override
     public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
     	int listIndex = manager.getShoppingLists().indexOf(selectedList);
     	
         switch (item.getItemId()) {
             case R.id.action_edit:
             	if (isList) {
 	                shoppingListAdapter.notifyDataSetChanged();
 	                mode.finish(); 
 	                // open list edit screen
 		        	Intent intent = new Intent(activity, CreateListActivity.class);
 		        	intent.putExtra("selectedList", listIndex);
 		            activity.startActivity(intent);
             	}
             	else {
 	                itemAdapter.notifyDataSetChanged();
 	                mode.finish(); 
 	                // open item edit screen
 		        	Intent intent = new Intent(activity, CreateItemActivity.class);
 		        	intent.putExtra("selectedItem",selectedItem.getId());
 		        	intent.putExtra("editItem", true);
 		            activity.startActivity(intent);
             	}
                 return true;
             case R.id.action_remove:
             	if (isList) {
 	            	manager.removeShoppingList(selectedList);
 	            	shoppingListAdapter.notifyDataSetChanged();
 	            	mode.finish(); // Action picked, so close the CAB
             	}
             	else {
 	            	manager.removeItemFromList(selectedItem, selectedList);
 	            	itemAdapter.notifyDataSetChanged();
 	            	mode.finish(); // Action picked, so close the CAB
             	}
            	return true;
            case R.id.action_share:
            	Intent intentShare = new Intent(activity, ShareListActivity.class);
            	intentShare.putExtra("selectedList", listIndex);
                activity.startActivity(intentShare);
                mode.finish(); // Action picked, so close the CAB
            	return true;
 
             default:
                 return false;
         }
     }
 
 	@Override
 	public void onDestroyActionMode(ActionMode mode) {
 		// nothing to do
 	}
 }
