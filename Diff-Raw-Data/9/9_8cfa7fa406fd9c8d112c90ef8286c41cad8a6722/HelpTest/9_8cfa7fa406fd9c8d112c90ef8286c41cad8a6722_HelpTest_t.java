 package awesome.app.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.TextView;
 import awesome.app.activity.HelpActivity;;
 
 public class HelpTest extends ActivityInstrumentationTestCase2<HelpActivity> {
 
 	public HelpTest() {
 		super("awesome.app.activity", HelpActivity.class);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		
 	}
 	
 	public void testButtonsWorkBundle() {
 		final HelpActivity hActivity = getActivity();
 		int scheduleId = awesome.app.R.id.scheduleLookupTextView;
 //		int studentId = awesome.app.R.id.studentLookupTextView;
 //		int feedbackId = awesome.app.R.id.feedbackTextView;
 //		int bandwidthId = awesome.app.R.id.bandwidthMonitorTextView;
 //		int araMenuId = awesome.app.R.id.araMenuTextView;
 		hActivity.makeButtonWork(scheduleId, awesome.app.R.string.scheduleHelpString);
 		TextView separator1 = (TextView) hActivity.findViewById(awesome.app.R.id.helpSeparatorTextView);
 		
 		/**
 		hActivity.makeButtonWork(studentId, awesome.app.R.string.studentHelpString);
 		TextView separator2 = (TextView) hActivity.findViewById(awesome.app.R.id.helpSeparatorTextView);
 		TextView studentTextView = (TextView) hActivity.findViewById(awesome.app.R.id.studentLookupTextView);
 		
 		hActivity.makeButtonWork(feedbackId, awesome.app.R.string.feedbackHelpString);
 		TextView separator3 = (TextView) hActivity.findViewById(awesome.app.R.id.helpSeparatorTextView);
 		TextView feedbackTextView = (TextView) hActivity.findViewById(awesome.app.R.id.feedbackTextView);
 		
 		hActivity.makeButtonWork(bandwidthId, awesome.app.R.string.bandwidthHelpString);
 		TextView separator4 = (TextView) hActivity.findViewById(awesome.app.R.id.helpSeparatorTextView);
 		TextView bandwidthTextView = (TextView) hActivity.findViewById(awesome.app.R.id.bandwidthMonitorTextView);
 		
 		hActivity.makeButtonWork(araMenuId, awesome.app.R.string.araHelpString);
 		TextView separator5 = (TextView) hActivity.findViewById(awesome.app.R.id.helpSeparatorTextView);
 		TextView araTextView = (TextView) hActivity.findViewById(awesome.app.R.id.araMenuTextView);
 		*/
 		hActivity.runOnUiThread(new Runnable() {
 			public void run() {
 				TextView scheduleTextView = (TextView) hActivity.findViewById(awesome.app.R.id.scheduleLookupTextView);
 				scheduleTextView.performClick();
 			}
 		});
 				
		assertEquals("Schedule Lookup Help", separator1.getText(), "");
 		//studentTextView.performClick();
 		//assertTrue(separator2.getText().equals(new String("Student Lookup Help")));
 		//araTextView.performClick();
 		//assertTrue(separator5.getText().equals(new String("ARA Menu Help")));
 		//feedbackTextView.performClick();
 		//assertTrue(separator3.getText().equals(new String("Feedback Help")));
 		//bandwidthTextView.performClick();
 		//assertTrue(separator4.getText().equals(new String("Bandwidth Monitor Help")));
 	}
 
 }
