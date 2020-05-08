 package csci498.lunchlist;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * The LunchList activity provides a form for users to enter and store
  * restaurant information and displays that information in a list
  */
 public class LunchList extends ListActivity {
 
 	public final static String ID_EXTRA = "apt.tutorial._ID";
 	
 	private Cursor               model;
 	private RestaurantAdapter    adapter;
 	private RestaurantHelper     helper;
 	
 	public void onListItemClick(ListView list, View view, int position, long id) {
 		Intent i = new Intent(LunchList.this, DetailForm.class);
 		i.putExtra(ID_EXTRA, String.valueOf(id));
 		startActivity(i);
     };
 	
 	/**
 	 * A container for restaurant name, address, and icon resources
 	 */
 	private static class RestaurantHolder {
 		
 		private TextView  name;
 		private TextView  address;
 		private ImageView icon;
 		
 		RestaurantHolder(View row) {
 			name    = (TextView) row.findViewById(R.id.title);
 			address = (TextView) row.findViewById(R.id.address);
 			icon    = (ImageView) row.findViewById(R.id.icon);
 		}
 
 		public void populateFrom(Cursor c, RestaurantHelper helper) {
 			name.setText(helper.getName(c));
 			address.setText(helper.getAddress(c));
 			
 			switch (helper.getType(c)) {
 				case SIT_DOWN:
 					icon.setImageResource(R.drawable.ball_green);
 					break;
 				case TAKE_OUT:
 					icon.setImageResource(R.drawable.ball_yellow);
 					break;
 				default:
 					icon.setImageResource(R.drawable.ball_red);
 					break;
 			}
 		}
 	}
 	
 	/**
 	 * A custom adapter for displaying restaurant information stored
 	 * in RestaurantHolder objects
 	 */
 	private class RestaurantAdapter extends CursorAdapter {
 		
 		public RestaurantAdapter(Cursor c) {
 			super(LunchList.this, c);
 		}
 		
 		@Override
 		public void bindView(View row, Context context, Cursor c) {
 			RestaurantHolder holder = (RestaurantHolder) row.getTag();
 			holder.populateFrom(c, helper);
 		}
 		
 		@Override
 		public View newView(Context context, Cursor c, ViewGroup parent) {
 			View row = getLayoutInflater().inflate(R.layout.restaurant_layout, parent, false);
 			RestaurantHolder holder = new RestaurantHolder(row);
 			row.setTag(holder);
 			
 			return row;
 		}
 	}
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_lunch_list);
         
         setupDatabaseHelper();
         setupRestaurantList();
     }
     
     private void setupDatabaseHelper() {
     	helper = new RestaurantHelper(this);
 	}
     
     private void setupRestaurantList() {
     	model = helper.getAll();
     	startManagingCursor(model);
     	adapter = new RestaurantAdapter(model);
     	setListAdapter(adapter);
     }
     
     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	if (item.getItemId() == R.id.add) {
     		startActivity(new Intent(LunchList.this, DetailForm.class));
     		return true;
     	}
     	
     	return super.onOptionsItemSelected(item);
     }
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		helper.close();
 	}
 }
