 package net.three_headed_monkey.ui;
 
 import android.content.Context;
 import android.widget.Button;
 import android.widget.ListView;
 import com.googlecode.androidannotations.annotations.BeforeTextChange;
 import net.three_headed_monkey.custom_shadows.ShadowTelephonyManager;
 import net.three_headed_monkey.ui.PhoneNumbersSettingsActivity;
 import org.junit.*;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.runner.RunWith;
 
 import android.app.Activity;
 import android.telephony.TelephonyManager;
 import android.widget.TextView;
 
 import net.three_headed_monkey.R;
 
 
 import org.robolectric.Robolectric;
 import org.robolectric.RobolectricTestRunner;
 import org.robolectric.annotation.Config;
 
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static org.robolectric.Robolectric.application;
 import static org.robolectric.Robolectric.newInstanceOf;
 import static org.robolectric.Robolectric.shadowOf;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.CoreMatchers.*;
 
 import net.three_headed_monkey.test_utils.*;
 
 @RunWith(RobolectricGradleTestRunner.class)

 public class PhoneNumbersSettingsActivityTest {
     private TextView text_serial_number;
     private TextView text_country_code;
     private TextView text_operator;
     private Activity activity;
     private ListView authorized_simcards_list;
     private Button button_authorize_card;
     private TextView text_currently_authorized;
     private Button button_add_phonenumber;
     private ListView phonenumbers_list;
 
     @Before
     public void setUp(){
        activity = Robolectric.buildActivity(PhoneNumbersSettingsActivity.class).create().get();
         phonenumbers_list = (ListView) activity.findViewById(R.id.phonenumbers_list);
         button_add_phonenumber = (Button) activity.findViewById(R.id.action_add);
     }
 
     @Test
     public void testAdd(){
         assertThat(phonenumbers_list.getCount(), equalTo(0));
     }
 
 }
