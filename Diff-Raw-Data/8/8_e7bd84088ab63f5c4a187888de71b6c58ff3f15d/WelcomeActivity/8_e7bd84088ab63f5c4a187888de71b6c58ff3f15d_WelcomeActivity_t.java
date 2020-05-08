 package info.twiceyuan.weather;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import info.twiceyuan.weather.util.WeatherIconGetter;
 import info.twiceyuan.weather.view.NowLayout;
 
 /**
  *
  */
 public class WelcomeActivity extends Activity {
 
     private Handler handler;
     private final int ADD_VIEW = 6941;
 
     private LinearLayout welcomeLayout;
     private NowLayout currentCard;
 
     private final int[] ids = {R.id.welcome_layout_1,R.id.welcome_layout_1,R.id.welcome_layout_2,R.id.welcome_layout_2};
    private final String[] weather_texts = {"晴","云","阴","雨"};
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.welcome);
 
         handler = new Handler() {
             @Override
             public void handleMessage(Message msg) {
                 super.handleMessage(msg);
 
                 if(msg.what == ADD_VIEW) {
 
                     welcomeLayout.addView(currentCard);
                 }
             }
         };
 
         init();
     }
 
     private void init() {
         Thread thread = new Thread(new InitRunnable());
         thread.start();
     }
 
     private class InitRunnable implements Runnable {
 
         @Override
         public void run() {
 
 
             for(int i = 0;i < 4;i++) {
                 LinearLayout newCardContent = getWelcomeCard();
 
                 ImageView icon = (ImageView) newCardContent.getChildAt(0);
                 TextView  text = (TextView)  newCardContent.getChildAt(1);
 
                assert icon != null;
                 icon.setImageDrawable(WeatherIconGetter.getWatherIcon(WelcomeActivity.this,i));
 
                assert text != null;
                text.setText(weather_texts[i]);
 
                 currentCard = new NowLayout(WelcomeActivity.this);
                 currentCard.addView(newCardContent);
 
                 welcomeLayout = (LinearLayout) findViewById(ids[i]);
 
                 handler.sendEmptyMessage(ADD_VIEW);
 
                 SystemClock.sleep(300);
             }
 
             SystemClock.sleep(500);
 
             Intent intent = new Intent();
             intent.setClass(getApplicationContext(),MainActivity.class);
             startActivity(intent);
             WelcomeActivity.this.finish();
         }
     }
 
     private LinearLayout getWelcomeCard() {
         return (LinearLayout) LinearLayout.inflate(WelcomeActivity.this, R.layout.welcome_card, null);
     }
 
 }
