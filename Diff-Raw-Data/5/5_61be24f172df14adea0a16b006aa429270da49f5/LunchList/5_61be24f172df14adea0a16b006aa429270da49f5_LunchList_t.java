 package csci498.cokembel.lunchlist;
 
 import csci498.cokembel.lunshlist.R;
 import android.os.Bundle;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 
 
 @SuppressWarnings("deprecation")
 public class LunchList extends ListActivity {
 	
 	Cursor model = null;
 	RestaurantAdapter adapter = null;
 	RestaurantHelper restaurantHelper = null;
 	
 	RadioButton sit_down, take_out, delivery;
 	EditText name = null;
 	EditText address = null;
 	EditText notes = null;
 	String restaurantType = null;
 	RadioGroup typesRadioGroup;
 	
 	public final static String ID_EXTRA = "csci498.cokembel.lunchlist._ID";
 
 	@Override
 	public void onListItemClick(ListView list, View view, int position, long id) {
 		Intent i=new Intent(LunchList.this, DetailForm.class);
 		i.putExtra(ID_EXTRA, String.valueOf(id));
 		startActivity(i);
 	}
 	
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	setTitle("LunchList");
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         restaurantHelper = new RestaurantHelper(this);
           
         model = restaurantHelper.getAll();
         startManagingCursor(model);
         adapter = new RestaurantAdapter(model);
         setListAdapter(adapter);		
         
         Log.d("1", "here");
     }
 
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 		restaurantHelper.close();
 	}
 	
     public class RestaurantAdapter extends CursorAdapter {
 	   	
     	RestaurantAdapter(Cursor c) {
     	super(LunchList.this, c);
     	}
     	
     	@Override
     	public void bindView(View row, Context ctxt, Cursor c) {
 	    	RestaurantHolder holder = (RestaurantHolder)row.getTag();
 	    	holder.populateFrom(c, restaurantHelper);
     	}
     	
     	@Override
     	public View newView(Context ctxt, Cursor c, ViewGroup parent) {
 	    	LayoutInflater inflater = getLayoutInflater();
 	    	View row=inflater.inflate(R.layout.row, parent, false);
 	    	RestaurantHolder holder = new RestaurantHolder(row);
 	    	row.setTag(holder);
 	    	return(row);
     	}
    }
     
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
 	   new MenuInflater(this).inflate(R.menu.options, menu);
 	   
 	   return(super.onCreateOptionsMenu(menu));
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
 	   if(item.getItemId() == R.id.add) {
 		   startActivity(new Intent(LunchList.this, DetailForm.class));
		   return true;
	   } else if (item.getItemId() == R.id.prefs) {
		   startActivity(new Intent(this, EditPreferences.class));
		   return true;
 	   }
 	   return(super.onOptionsItemSelected(item));
    }
        
    public static class RestaurantHolder {
     	
     	private TextView name = null;
     	private TextView address  =null;
     	private ImageView icon = null;
     	
     	RestaurantHolder(View row) {
     		
     		name = (TextView)row.findViewById(R.id.title);
     		address =(TextView)row.findViewById(R.id.address);
     		icon = (ImageView)row.findViewById(R.id.icon);
     	}
     	
 		void populateFrom(Cursor c, RestaurantHelper helper) {
     		name.setText(helper.getName(c));
     		address.setText(helper.getAddress(c));
     		
     		if(helper.getType(c).equals("sit_down")) {
     			icon.setImageResource(R.drawable.ball_red);
     			name.setTextColor(Color.RED);
 			}else if(helper.getType(c).equals("take_out")) {
 				icon.setImageResource(R.drawable.ball_yellow);
 				name.setTextColor(Color.YELLOW);
 			}else {
 				icon.setImageResource(R.drawable.ball_green);
 				name.setTextColor(Color.GREEN);
 			}
 		}
     }  
 }
