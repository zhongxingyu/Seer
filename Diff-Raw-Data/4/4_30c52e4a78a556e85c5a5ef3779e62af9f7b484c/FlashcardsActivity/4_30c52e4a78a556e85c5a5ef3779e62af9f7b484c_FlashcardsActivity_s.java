 package se.flashcards;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FlashcardsActivity extends SherlockListActivity {
 	
 	private static final int DIALOG_MAKE_NEW = 0;
 
 	private List<String> cardLists;
 	private ArrayAdapter<String> cardListsAdapter;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);   
         setTheme(R.style.Theme_Sherlock);
         
         cardLists = new ArrayList<String>();
         cardListsAdapter = new ArrayAdapter<String>(this, R.layout.list_item, cardLists);
         setListAdapter(cardListsAdapter);
         
         cardListsAdapter.add("hipster"); 
         
         ActionBar bar = getSupportActionBar();
         bar.setTitle("RAYBAN");
         bar.setSubtitle("Only $999");
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.layout.actionbar_menu, menu);
         return true;
     }
     
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     		case R.id.menu_save:
     			showDialog(DIALOG_MAKE_NEW);
     			break;
     	}
         return true;
     }
     
     public void onListItemClick(ListView listView, View view, int position, long id) {
     	Intent intent = new Intent(this, CardsListActivity.class);
     	intent.putExtra("temp", cardLists.get(position));
     	startActivity(intent);
     }
     
     protected Dialog onCreateDialog(int id) {
     	switch (id) {
     		case DIALOG_MAKE_NEW:
                 LayoutInflater factory = LayoutInflater.from(this);
                 final View textEntryView = factory.inflate(R.layout.make_new_dialog, null);
                 final TextView textView = (TextView)textEntryView.findViewById(R.id.text);
                 return new AlertDialog.Builder(this)
                     .setIconAttribute(android.R.attr.alertDialogIcon)
                     .setTitle("Make new entry")
                     .setView(textEntryView)
                     .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                         	String text = textView.getText().toString();
                         	textView.setText("");
                        	cardLists.add(text);
                         }
                     })
                     .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                         	textView.setText("");
                         }
                     })
                     .create();
     		default:
     			return null;
     	}
     }
 }
