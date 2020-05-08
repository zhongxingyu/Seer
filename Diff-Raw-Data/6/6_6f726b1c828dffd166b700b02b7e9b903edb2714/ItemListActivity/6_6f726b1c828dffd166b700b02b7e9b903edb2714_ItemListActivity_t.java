 /*
  * ShoppingList for Android
  * (C)2011 by Christian Lønaas
  *    <christian dot lonaas at discombobulator dot org>
  *
  * This file is part of ShoppingList.
  *
  * ShoppingList is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ShoppingList is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with ShoppingList.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 
 package no.opentech.shoppinglist.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 import no.opentech.shoppinglist.*;
 import no.opentech.shoppinglist.adapters.ItemAdapter;
 import no.opentech.shoppinglist.entities.Item;
 import no.opentech.shoppinglist.entities.ShoppingList;
 import no.opentech.shoppinglist.utils.Utils;
 
 import java.util.ArrayList;
 
 /**
  * Created by: Christian Lønaas
  * Date: 08.04.11
  * Time: 20:45
  */
 public class ItemListActivity extends ListActivity
 {
     private static final String TAG = "ShoppingList";
     private ArrayList<Item> shoppingItems;
     private Context context = no.opentech.shoppinglist.ShoppingList.getContext();
     private long shoppingListId;
     private boolean noList = false;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
 
         shoppingItems = new ArrayList<Item>();
         shoppingItems = Utils.getItemRepository().getItemsOrderedByUsages();
         shoppingListId = getIntent().getLongExtra("shoppingListId", 0);
         noList = getIntent().getBooleanExtra("noList", false);
         setTitle((!noList) ? "Select items" : "Manage items");
         setListAdapter(new ItemAdapter(context, R.layout.list_item, shoppingItems));
         ListView lv = getListView();
         registerForContextMenu(lv);
         lv.setTextFilterEnabled(true);
 
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 // toggle checked
                 Item selectedItem = shoppingItems.get(position);
                 selectedItem.setChecked(!selectedItem.isChecked());
                 ((ItemAdapter)getListAdapter()).notifyDataSetChanged();
             }
         });
     }
     
     @Override
     public void onBackPressed() {
         Intent resultIntent = new Intent();
         resultIntent.putExtra("shoppingListId", shoppingListId);
         setResult(Activity.RESULT_CANCELED, resultIntent);
         finish();
     }
     
    @Override
    public boolean onSearchRequested() {
        Toast.makeText(context, "Search not available yet", Toast.LENGTH_SHORT).show();
        return super.onSearchRequested();
    }
    
     /* create the menu */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inf = getMenuInflater();
         if(!noList)
             inf.inflate(R.menu.itemlist_options, menu);
         else inf.inflate(R.menu.itemlist_options_nolist, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.newitem:
                 addNewItem();
                 break;
             case R.id.savelist:
                 Toast.makeText(context, "Saving list..", Toast.LENGTH_SHORT).show();
                 saveList();
                 break;
             case R.id.clearlist:
                 clearList();
                 break;
             case R.id.deleteselected:
                 deleteSelected();
                 break;
 
         }
         return true;
     }
 
     @Override
     public void onUserInteraction() {
         
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inf = getMenuInflater();
         inf.inflate(R.menu.items_context_menu, menu);
         menu.setHeaderTitle(R.string.context_menu_title);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         Item selectedItem = shoppingItems.get(info.position);
 
         switch(item.getItemId()) {
             case R.id.removeitem:
                 removeItem(selectedItem);
                 break;
             case R.id.showitemdetails:
                 Intent intent = new Intent(this, ItemDetailsActivity.class);
                 intent.putExtra("itemId", selectedItem.getId());
                 this.startActivity(intent);
                 break;
         }
         return true;
     }
     
     public void saveList() {
         ShoppingList sl = Utils.getShoppingListRepository().getShoppingListById(shoppingListId);
         for(Item item : shoppingItems) {
             if(item.isChecked()) {
                 item.incrementUsageCounter();
                 Utils.getItemRepository().update(item);
                 Utils.getShoppingListRepository().addItemToShoppingList(item, sl);
             }
         }
 
         Intent resultIntent = new Intent();
         setResult(Activity.RESULT_OK, resultIntent);
         this.finish();
     }
 
     /* show dialog with input box, for adding item to shopping list */
     public void addNewItem() {
         final AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		final EditText input = new EditText(this);
         input.setWidth(200); // TODO: hard coded width is bad
         input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
 		alert.setView(input);
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String newItem = input.getText().toString().trim();
                 Item item = new Item();
                 if(!newItem.equals("")) {
                     item.setName(newItem);
                     long id = Utils.getItemRepository().insert(item);
                     item.setId(id);
                     shoppingItems.add(item);
                 }
                 ((ItemAdapter) getListAdapter()).notifyDataSetChanged();
 			}
 		});
 
 		alert.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						dialog.cancel();
 					}
 				});
 		alert.show();
 
     }
 
     public void removeItem(Item item) {
         Utils.getItemRepository().delete(item);
         shoppingItems.remove(item);
         ((ItemAdapter)getListAdapter()).notifyDataSetChanged();
     }
     
     public void deleteSelected() {
         int deleted=0;
         for(int i=0; i<shoppingItems.size(); i++) {
             Item item = shoppingItems.get(i);
             if(item.isChecked()) {
                 shoppingItems.remove(item);
                 Utils.getItemRepository().delete(item);
                 deleted++;
             }
         }
         ((ItemAdapter)getListAdapter()).notifyDataSetChanged();
         Toast.makeText(context,((deleted != 0) ? "Deleted " + deleted + " items" : "No items deleted"), Toast.LENGTH_SHORT).show();
     }
 
     public void clearList() {
         for(Item item : shoppingItems) item.setChecked(false);
         ((ItemAdapter)getListAdapter()).notifyDataSetChanged();
     }
 }
