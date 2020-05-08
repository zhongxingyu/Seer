 package epfl.sweng.test;
 
 import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
 import epfl.sweng.quizzes.ShowQuizActivity;
 import epfl.sweng.servercomm.SwengHttpClientFactory;
 import epfl.sweng.test.mocking.MockHttpClient;
 
 import com.jayway.android.robotium.solo.Solo;
 
 /**
  * Test class for the rating system 
  */
 public class QuizTest extends ActivityInstrumentationTestCase2<ShowQuizActivity> {
 	
 	private Solo solo;
 	
 	public QuizTest() {
 		super(ShowQuizActivity.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
         SwengHttpClientFactory.setInstance(new MockHttpClient());
 		solo = new Solo(getInstrumentation(), getActivity());
 
 	}
 	
 	public void testAlertDialog() {
		getActivity().displayScoreAlertDialog(13.58);
 		assertTrue("Could not find the dialog!", solo.searchText("13.58"));
 	}
 }
