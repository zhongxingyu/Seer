 package teamwork.goodVibrations;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class TriggerEditActivity extends Activity
 {
   private static final String TAG  = "TriggerEditActivity";
   Intent mIntent;
   
   public void onCreate(Bundle savedInstanceState)
   {
     super.onCreate(savedInstanceState);
     Log.d(TAG, "onCreate()");
     setContentView(R.layout.add_trigger);
   }
   protected void onStart()
   {
     super.onStart();
     Log.d(TAG, "onStart()");
     mIntent = new Intent();
     
     final Button buttonAddTimeTrigger = (Button) findViewById(R.id.buttonAddTimeTrigger);
     buttonAddTimeTrigger.setOnClickListener(new View.OnClickListener()
     {
       
       public void onClick(View v)
       {
         Intent TimeTriggerEditIntent = new Intent(getApplicationContext(), TimeTriggerEditActivity.class);
         startActivityForResult(TimeTriggerEditIntent,0);
       }
     });
   }
   
   @Override
  protected void onActivity Result(int requestCode, int resultCode, Intent data)
   {
    super.onActivtyResult(requestCode, resultCode, data);
     if(resultCode == RESULT_OK)
     {
       Bundle b = data.getExtras();
       //send back to service
       
     }
   }
 }
