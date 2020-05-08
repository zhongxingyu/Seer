 package ugtug.lab3;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends ListActivity {
 	
 	private static final String TAG = MainActivity.class.getSimpleName();
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         Resources resources = getResources();
         String[] numbers = resources.getStringArray(R.array.list);
         
         this.getListView().setTextFilterEnabled(true);
         this.getListView().setOnItemClickListener(new OnItemClickListener() {
         	
 			public void onItemClick(AdapterView<?> parent, View target, int position, long id) {
 
 				CharSequence text = ((TextView) target).getText();
 				Toast t = Toast.makeText(getApplicationContext(), "Displaying " + text, Toast.LENGTH_SHORT);
 				t.show();
 
 				Log.v(TAG, "displaying: " + text + ", position: " + position + ", id:= " + id);
 
 				Intent otherIntent = new Intent(MainActivity.this, OtherActivity.class);
 				otherIntent.putExtra("key", text);
 				
 				startActivity(otherIntent);
 			}        	
         });
                 
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, numbers);
         this.getListView().setAdapter(adapter);
         
         // allow for long-click context menu
         registerForContextMenu(this.getListView());
 
        setContentView(R.layout.main);
     }
     
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0,1,0,"One");
 		menu.add(0,2,0,"Two");
 		menu.add(0,3,0,"Three");
 		return true;
     }
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		Toast t = Toast.makeText(getApplicationContext(), "Clicked " + item.getTitle(), Toast.LENGTH_SHORT);
 		t.show();
 
 		Intent otherIntent = new Intent(MainActivity.this, OtherActivity.class);
 
 		switch(item.getItemId()) {
 			
 			// one
 			case 1:
 				otherIntent.putExtra("key", "one");
 				startActivity(otherIntent);
 				break;
 
 			// two
 			case 2:
 				otherIntent.putExtra("key", "two");
 				startActivity(otherIntent);
 				break;
 
 			// three
 			case 3:
 				otherIntent.putExtra("key", "three");
 				startActivity(otherIntent);
 				break;
 
 			default:
 				break;
 		}
 
 	   return true;
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		Log.v("MainActivity", v.getClass().getName());
 		
		TextView tv = (TextView) ((ListView) v).getSelectedView();
		CharSequence text = tv.getText();
 		
 		menu.setHeaderTitle("Context Menu");
 		menu.add(200, 200, 200, "Context One");
 		menu.add(200, 201, 201, "Context Two");
 		menu.add(200, 202, 202, "Context Three");
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		
 		Toast t = Toast.makeText(getApplicationContext(), "Clicked " + item.getTitle(), Toast.LENGTH_SHORT);
 		t.show();
 
 		Intent otherActivity = new Intent(MainActivity.this, OtherActivity.class);
 
 		switch(item.getItemId()) {
 			case 200:
 				otherActivity.putExtra("title", item.getTitle());
 				startActivity(otherActivity);
 				break;
 				
 			case 201:
 				otherActivity.putExtra("title", item.getTitle());
 				startActivity(otherActivity);
 				break;
 
 			case 202:
 				otherActivity.putExtra("title", item.getTitle());
 				startActivity(otherActivity);
 				break;			
 		}
 		return true;
 	}
 
 }
