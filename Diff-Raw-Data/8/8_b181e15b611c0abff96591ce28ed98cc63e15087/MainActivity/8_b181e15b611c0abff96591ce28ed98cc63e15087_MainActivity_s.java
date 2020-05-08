 package fi.testbed2.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import fi.testbed2.R;
 import fi.testbed2.app.Logging;
 import fi.testbed2.app.MainApplication;
 import fi.testbed2.result.TaskResult;
 import roboguice.inject.ContentView;
 import roboguice.inject.InjectView;
 
 @ContentView(R.layout.main)
 public class MainActivity extends AbstractActivity implements OnClickListener {
 
     public static final int PARSING_SUB_ACTIVITY = 1;
     public static final int ANIMATION_SUB_ACTIVITY = 2;
 
     @InjectView(R.id.button_refresh)
     ImageButton refreshButton;
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         refreshButton.setOnClickListener(this);
        //startMainParsingActivity();
     }
 
     /** Called when resuming, BUT after onActivityResult! */
     @Override
     protected void onResume() {
         super.onResume();
     }
 
     @Override
 	public void onClick(View v) {
 		if(v.getId() == R.id.button_refresh) {
             startMainParsingActivity();
 		}
 	}
 
     private void startMainParsingActivity() {
         Logging.debug("startMainParsingActivity");
         Intent intent = new Intent(this, ParsingActivity.class);
         startActivityForResult(intent, PARSING_SUB_ACTIVITY);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
         switch (requestCode) {
             case PARSING_SUB_ACTIVITY:
                 handleParsingResult(resultCode, data);
                 break;
             case ANIMATION_SUB_ACTIVITY:
                 handleAnimationResult(resultCode, data);
                 break;
             default:
                 super.onActivityResult(requestCode, resultCode, data);
                 break;
         }
 
     }
 
     private void handleParsingResult(int resultCode, Intent data) {
 
         switch(resultCode) {
             case MainApplication.RESULT_REFRESH:
                 startMainParsingActivity();
                 break;
             case MainApplication.RESULT_OK:
                 startActivityForResult(new Intent(this, AnimationActivity.class), ANIMATION_SUB_ACTIVITY);
                 break;
             case Activity.RESULT_CANCELED:
                 showShortMessage(this.getString(R.string.notice_cancelled));
                 break;
             case MainApplication.RESULT_ERROR:
                 String errorMsg = this.getString(R.string.error_message_detailed,
                     data.getStringExtra(TaskResult.MSG_CODE));
                 showErrorDialog(errorMsg);
                 break;
         }
 
     }
 
     private void handleAnimationResult(int resultCode, Intent data) {
 
         switch(resultCode) {
             case MainApplication.RESULT_REFRESH:
                 startMainParsingActivity();
                 break;
             case Activity.RESULT_CANCELED:
                 showShortMessage(this.getString(R.string.notice_cancelled));
                 break;
             case MainApplication.RESULT_ERROR:
                 String errorMsg = this.getString(R.string.error_message_detailed,
                         data.getStringExtra(TaskResult.MSG_CODE));
                 showErrorDialog(errorMsg);
                 break;
             default:
                 break;
         }
 
     }
 
     @Override
     public void onRefreshButtonSelected() {
         startMainParsingActivity();
     }
 
 }
