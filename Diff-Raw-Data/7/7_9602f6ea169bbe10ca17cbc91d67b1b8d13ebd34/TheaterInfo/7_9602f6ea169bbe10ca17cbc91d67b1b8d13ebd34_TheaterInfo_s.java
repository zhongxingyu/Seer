 package ua.in.leopard.androidCoocooAfisha;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 
 
 public class TheaterInfo extends Activity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.theater_info);
         
         Bundle extras = getIntent().getExtras();
         int theater_id = 0;
         if(extras != null) {
         	theater_id = extras.getInt("theater_id", 0);
         }
         
         if (theater_id != 0){
         	DatabaseHelper DatabaseHelperObject = new DatabaseHelper(this);
         	TheaterDB theater_main = DatabaseHelperObject.getTheater(theater_id);
         	if (theater_main != null){
         		setTitle(theater_main.getTitle());
         		TextView theater_address = (TextView)findViewById(R.id.theater_address);
        		theater_address.setText(theater_main.getAddress());
         		TextView theater_phone = (TextView)findViewById(R.id.theater_phone);
        		theater_phone.setText(theater_main.getPhone());
         		TextView theater_link = (TextView)findViewById(R.id.theater_link);
         		theater_link.setText(theater_main.getLink());
         	}
         }
 	}
 	
 }
