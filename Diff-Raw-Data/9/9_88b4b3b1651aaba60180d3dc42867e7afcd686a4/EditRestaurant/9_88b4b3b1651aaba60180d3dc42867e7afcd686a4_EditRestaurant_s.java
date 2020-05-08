 package csci498.strupper.munchlist;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 
 public class EditRestaurant extends Activity {
 
   private RestaurantHelper helper;
   private String restaurantId;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.edit_restaurant);
 
     helper = new RestaurantHelper(this);
 
     restaurantId = getIntent().getStringExtra(MunchList.ID_EXTRA);
 

     // restore form
     if (savedInstanceState != null) {
       String s;
      if (restaurantId == null && (s = savedInstanceState.getString("name")) != null) {
         restaurantId = s;
       }
       if ((s = savedInstanceState.getString("name")) != null) {
         ((EditText)findViewById(R.id.name)).setText(s);
       }
       if ((s = savedInstanceState.getString("address")) != null) {
         ((EditText)findViewById(R.id.address)).setText(s);
       }
       if ((s = savedInstanceState.getString("type")) != null) {
         if (s.equals("sit_down")) {
           ((RadioButton)findViewById(R.id.sit_down)).setChecked(true);
         } else if (s.equals("take_out")) {
           ((RadioButton)findViewById(R.id.take_out)).setChecked(true);
         } else if (s.equals("delivery")) {
           ((RadioButton)findViewById(R.id.delivery)).setChecked(true);
         }
       }
     }
 
     if (restaurantId != null) {
       load();
     }
 
     /// attach save listener
     Button save = (Button)findViewById(R.id.save);
     save.setOnClickListener(new OnClickListener() {
       public void onClick(View v) {
         Restaurant r = new Restaurant();
         r.setName(((EditText)findViewById(R.id.name))
                   .getText()
                   .toString());
         r.setAddress(((EditText)findViewById(R.id.address))
                      .getText()
                      .toString());
 
         RadioGroup types = (RadioGroup)findViewById(R.id.types);
         switch (types.getCheckedRadioButtonId()) {
         case R.id.sit_down:
           r.setType("sit_down");
           break;
         case R.id.take_out:
           r.setType("take_out");
           break;
         case R.id.delivery:
           r.setType("delivery");
           break;
         }
 
         if (restaurantId == null) {
           helper.insert(r);
         } else {
           helper.update(restaurantId, r);
         }
 
         finish();
       }
     });
   }
 
   private void setFrom(Restaurant r) {
     ((EditText)findViewById(R.id.name)).setText(r.getName());
     ((EditText)findViewById(R.id.address)).setText(r.getAddress());
     RadioGroup types = (RadioGroup)findViewById(R.id.types);
     if (r.getType().equals("sit_down")) {
       types.check(R.id.sit_down);
     }
     else if (r.getType().equals("take_out")) {
       types.check(R.id.take_out);
     }
     else {
       types.check(R.id.delivery);
     }
   }
 
   private void load() {
     Cursor c = helper.getById(restaurantId);
     c.moveToFirst();
     setFrom(RestaurantHelper.restaurantOf(c));
     c.close();
   }
 
   @Override public void onSaveInstanceState(Bundle outState) {
    outState.putString("id", restaurantId);
     outState
       .putString("name",
                  ((EditText)findViewById(R.id.name)).getText().toString());
     outState
       .putString("address",
                  ((EditText)findViewById(R.id.address)).getText().toString());
     RadioGroup types = (RadioGroup)findViewById(R.id.types);
     switch (types.getCheckedRadioButtonId()) {
     case R.id.sit_down:
       outState.putString("type", "sit_down");
       break;
     case R.id.take_out:
       outState.putString("type", "take_out");
       break;
     case R.id.delivery:
       outState.putString("type", "delivery");
       break;
     }
   }
 
   @Override
   public void onDestroy() {
     super.onDestroy();
     helper.close();
   }
 
 }
