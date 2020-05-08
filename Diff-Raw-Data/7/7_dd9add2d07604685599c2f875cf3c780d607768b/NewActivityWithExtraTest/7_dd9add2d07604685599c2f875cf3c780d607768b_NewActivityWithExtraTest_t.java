 package com.shinetech.robotium;
 
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.suitebuilder.annotation.Smoke;
 import android.widget.TextView;
 
 import com.jayway.android.robotium.solo.Solo;
 
 public class NewActivityWithExtraTest extends ActivityInstrumentationTestCase2<NewActivity>{
 	private Solo solo;
	private static final String EXTRA = "this activity came with baggage";

 	public NewActivityWithExtraTest() {
 		super(NewActivity.class);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 	@Override
 	public NewActivity getActivity() {
 		Intent intent = new Intent();
 		intent.putExtra("example05", EXTRA);
 		setActivityIntent(intent);
 		return super.getActivity();
 	}

 	@Smoke
 	public void testExample05() {
 		TextView example05TextView = (TextView) solo.getCurrentActivity().findViewById(R.id.example_05_text_view);
 		assertEquals(EXTRA, example05TextView.getText().toString());
 	}
 }
