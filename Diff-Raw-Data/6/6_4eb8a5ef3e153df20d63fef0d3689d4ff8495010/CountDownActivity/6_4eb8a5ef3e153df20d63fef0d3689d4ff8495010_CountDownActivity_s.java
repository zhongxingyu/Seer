 package com.quiz.flag;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.TextView;
 /**
 *
 * Count down screen shows just before the game starts.
 * @author Muammil
 */
 
 public class CountDownActivity extends Activity {
   private final long ONE_SECOND = 1000;
   private int count;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.count_down_screen);
     final TextView countDownText = (TextView) findViewById(R.id.tv_countdown);
     final int buttonId = getIntent().getExtras().getInt("buttonId");
     count = 3;
     new Timer().scheduleAtFixedRate(new TimerTask() {
       @Override
       public void run() {
         runOnUiThread(new Runnable() {
           @Override
           public void run() {
             if(count > 0) {
               countDownText.setText(String.valueOf(count));
               --count;
             } else if(count == 0) {
               countDownText.setText("GO");
               finish();
               Intent intent;
               switch(buttonId) {
               case R.id.b_nametoflag:
                 intent = new Intent(CountDownActivity.this, NameToFlagActivity.class);
                 startActivity(intent);
                 break;
               case R.id.b_flagtoname:
                 intent = new Intent(CountDownActivity.this, FlagToNameActivity.class);
                 startActivity(intent);
                 break;
               }
               cancel();
             }
           }
         });
       }
     }, 0, ONE_SECOND);
   }
 }
