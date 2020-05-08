 package me.horzwxy.app.pfm.android.activity;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import me.horzwxy.app.pfm.android.R;
 import me.horzwxy.app.pfm.model.AddDiningInfoRequest;
 import me.horzwxy.app.pfm.model.AddDiningInfoResponse;
 import me.horzwxy.app.pfm.model.Dining;
 import me.horzwxy.app.pfm.model.ListContactsRequest;
 import me.horzwxy.app.pfm.model.Response;
 import me.horzwxy.app.pfm.model.User;
 
 /**
  * Created by horz on 9/8/13.
  */
 public class NewDiningActivity extends LoggedInActivity {
 
     private final static int REQUEST_FOR_PARTICIPANTS = 987;
 
     private Dining diningInfo;
     private EditText restaurantInput;
     private EditText costInput;
     private Button dateButton;
     private Button timeButton;
     private ProgressDialog pDialog;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_new_dining);
 
         diningInfo = new Dining();
         restaurantInput = ( EditText ) findViewById( R.id.new_dining_restuarant_input );
         costInput = ( EditText ) findViewById( R.id.new_dining_cost );
         dateButton = ( Button ) findViewById(R.id.new_dining_show_date_picker);
         timeButton = ( Button ) findViewById(R.id.new_dining_show_time_picker);
 
         Calendar calendar = Calendar.getInstance();
         dateButton.setText( calendar.get( Calendar.YEAR ) + "/"
                 + ( calendar.get( Calendar.MONTH ) + 1 ) + "/"
                 + calendar.get( Calendar.DAY_OF_MONTH ) );
         int hour = calendar.get( Calendar.HOUR_OF_DAY );
         int minute = calendar.get( Calendar.MINUTE );
         String hourString = hour + "";
         if( hour < 10 ) {
             hourString = "0" + hour;
         }
         String minuteString = minute + "";
         if( minute < 10 ) {
             minuteString = "0" + minute;
         }
        timeButton.setText( hourString + ":"
                + minuteString );
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if( resultCode == Activity.RESULT_OK ) {
             switch ( requestCode ) {
                 case REQUEST_FOR_PARTICIPANTS:
                     diningInfo.participants = ( ArrayList< User > )data.getSerializableExtra( "participants" );
                     break;
                 default:
                     super.onActivityResult(requestCode, resultCode, data);
                     break;
             }
         }
     }
 
     public void showDatePicker( View v ) {
         Calendar calendar = Calendar.getInstance();
         DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
             @Override
             public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                 dateButton.setText( year + "/" + ( month + 1 ) + "/" + day );
             }
         };
         new DatePickerDialog( this,
                 listener,
                 calendar.get( Calendar.YEAR ),
                 calendar.get( Calendar.MONTH ),
                 calendar.get( Calendar.DAY_OF_MONTH ) ).show();
     }
 
     public void showTimePicker( View v ) {
         Calendar calendar = Calendar.getInstance();
         TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
             @Override
             public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                 String hourString = hour + "";
                 if( hour < 10 ) {
                     hourString = "0" + hour;
                 }
                 String minuteString = minute + "";
                 if( minute < 10 ) {
                     minuteString = "0" + minute;
                 }
                 timeButton.setText( hourString + " : " + minuteString );
             }
         };
         new TimePickerDialog( this,
                 listener,
                 calendar.get( Calendar.HOUR_OF_DAY ),
                 calendar.get( Calendar.MINUTE ),
                 true ).show();
     }
 
     public void chooseParticipants( View v ) {
         Intent intent = new Intent( this, ChooseParticipantsActivity.class );
         intent.putExtra( "participants", diningInfo.participants );
         startActivityForResult(intent, REQUEST_FOR_PARTICIPANTS);
     }
 
     public void submit( View v ) {
         String restaurant = restaurantInput.getText() + "";
         String costString = costInput.getText() + "";
         String dateString = dateButton.getText().toString();
         String timeString = timeButton.getText().toString();
         if( restaurant.equals( "" ) ) {
             Toast.makeText( this, getResources().getText( R.string.new_dining_failed_no_restaurant ), Toast.LENGTH_SHORT ).show();
             return;
         }
         if( costString.equals( "" ) ) {
             Toast.makeText( this, getResources().getText( R.string.new_dining_failed_no_cost ), Toast.LENGTH_SHORT ).show();
             return;
         }
         if( diningInfo.participants.size() == 0 ) {
             Toast.makeText( this, getResources().getText( R.string.new_dining_failed_no_participants ), Toast.LENGTH_SHORT ).show();
             return;
         }
         diningInfo.restaurant = restaurant;
         diningInfo.cost = Integer.parseInt( costString );
         DateFormat format = new SimpleDateFormat( "yyyy/MM/dd/HH:mm" );
         try {
             diningInfo.date = format.parse( dateString + "/" + timeString );
         } catch (ParseException e) {
             e.printStackTrace();
         }
         diningInfo.author = currentUser;
         final AddDiningInfoTask task = new AddDiningInfoTask();
         pDialog = new ProgressDialog(this);
         pDialog.setCancelable(true);
         pDialog.setOnCancelListener( new DialogInterface.OnCancelListener() {
             @Override
             public void onCancel(DialogInterface dialogInterface) {
                 task.cancel( true );
             }
         });
         pDialog.setMessage(getResources().getString(R.string.new_dining_submitting));
         pDialog.show();
         task.execute( new AddDiningInfoRequest( diningInfo ) );
     }
 
     class AddDiningInfoTask extends PFMHttpAsyncTask {
 
         @Override
         protected void onPostExecute(Response response) {
             AddDiningInfoResponse adResponse = ( AddDiningInfoResponse ) response;
             if( adResponse.getType() == AddDiningInfoResponse.AddDiningInfoType.SUCCESS ) {
                 pDialog.dismiss();
                 NewDiningActivity.this.finish();
             }
             else {
                 pDialog.dismiss();
                 Toast.makeText( NewDiningActivity.this, getResources().getString( R.string.new_dining_failed_submit ), Toast.LENGTH_SHORT ).show();
             }
         }
     }
 }
