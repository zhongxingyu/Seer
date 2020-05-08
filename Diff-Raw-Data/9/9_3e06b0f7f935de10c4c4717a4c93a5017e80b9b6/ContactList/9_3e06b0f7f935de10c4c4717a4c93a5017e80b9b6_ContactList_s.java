 package edu.upenn.cis350;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
import android.widget.Toast;
 
 import com.parse.FindCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 public class ContactList extends ListActivity {
 
 	/**
 	 *  Called when the activity is first created.
 	 *  Makes query to fetch and populate contact list
 	 *  
 	 **/
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		Parse.initialize(this, Settings.APPLICATION_ID, Settings.CLIENT_ID);
                 
         final ProgressDialog dialog = ProgressDialog.show(this, "", 
 				"Loading. Please wait...", true);
 		dialog.setCancelable(true);
 		
         ParseQuery query = new ParseQuery("_User");
         query.orderByAscending("lname");
        
    	final Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT); 
    	
     	query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
     	query.findInBackground(new FindCallback() {
 
 			@Override
 			public void done(List<ParseObject> contactList, ParseException e) {
 				if (e == null) {
 					List<String> names = new ArrayList<String>();
 					final HashMap<String, String> identifiers = new HashMap<String, String>();
 					for (ParseObject c : contactList) {
 						String formattedName = c.getString("lname") + ", " + c.getString("fname");
 						names.add(formattedName);
 						identifiers.put(formattedName, c.getObjectId());
 					}
 			        setListAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, names));
 
 			        ListView lv = getListView();
 			        lv.setTextFilterEnabled(true);
 			        
 			        lv.setOnItemClickListener(new OnItemClickListener() {
 						@Override
 						public void onItemClick(AdapterView<?> parent, View view,
 								int position, long id) {
 							Intent i = new Intent(getApplicationContext(), ShowContact.class);
 							i.putExtra("contactID", identifiers.get(((TextView) view).getText()));
 							startActivity(i);
 						}
 			          });
 				} else {
					toast.setText("Error: " + e.getMessage());
					toast.show();
 					return;
 				}
 				dialog.cancel();
 			}
     		
     	});
     }
 }
