 package com.mdumrauf.android.examples.gradle;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import android.widget.TextView;
 
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 
 @RunWith(RobolectricTestRunner.class)
 public class MainActivityTest {
 
 	private TextView textView;
 	private MainActivity activity;
 	
 	@Before
 	public void before() {
 		activity = new MainActivity();
 		activity.onCreate(null);
 		textView = (TextView) activity.findViewById(R.id.text_view);
 	}
 
 	@Test
	public void testGetAppNameFromResourceString() {
		assertEquals("Gradle Android Example", activity.getString(R.string.app_name));
	}

	@Test
 	public void testTextViewContent() {
		assertEquals("4,2,8", textView.getText().toString());
 	}
 
 }
