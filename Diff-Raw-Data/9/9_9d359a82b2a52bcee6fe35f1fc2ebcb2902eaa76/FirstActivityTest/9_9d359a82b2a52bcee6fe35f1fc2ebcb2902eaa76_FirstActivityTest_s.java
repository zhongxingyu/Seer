 package com.tw;
 
 import android.content.Intent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import com.xtremelabs.robolectric.Robolectric;
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 
 @RunWith(RobolectricTestRunner.class)
 public class FirstActivityTest {
 
     private FirstActivity activity;
 
     @Before
     public void setUp() {
         activity = new FirstActivity();
     }
 
     @Test
     public void shouldDisplayTitle() {
         activity.onCreate(null);
 
         TextView contentView = (TextView) activity.findViewById(R.id.content_view);
        assertThat(conxtentView.getText().toString(), is("Android Series Chennai - First Activity"));
     }
 
     @Test
     public void shouldLaunchSecondActivityWhenButtonIsClicked() {
         activity.onCreate(null);
 
         Button launchActivityButton = (Button) activity.findViewById(R.id.launch_activity);
         launchActivityButton.performClick();
 
         assertThat(Robolectric.getShadowApplication().getNextStartedActivity().getComponent().getClassName(),
                 is(SecondActivity.class.getName()));
     }
 
     @Test
     public void shouldIncludeTextAsPartOfTheIntentThatLaunchesTheSecondActivity() {
         activity.onCreate(null);
 
         Button launchActivityButton = (Button) activity.findViewById(R.id.launch_activity);
         launchActivityButton.performClick();
 
         Intent intentOfNextActivity = Robolectric.getShadowApplication().getNextStartedActivity();
         assertThat(intentOfNextActivity.getStringExtra("content"), is("Second Activity started - click to view contact"));
     }
 
 }
