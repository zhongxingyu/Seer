 import android.test.ActivityInstrumentationTestCase2;
 import android.view.View;
 import android.widget.ListView;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import epfl.sweng.showquestions.ShowQuestionsActivity;
 import epfl.sweng.tasks.LoadRandomQuestion;
 
 /**
  * First test case...
  */
 public class ShowQuestionsActivityTest extends
 		ActivityInstrumentationTestCase2<ShowQuestionsActivity> {
 	
 	private Solo solo;
 	private static final int NUMBER_OF_QUESTIONS = 5;
 	private static final int SLEEP_LISTCHECK = 2000;
 	private static final int SLEEP_CHARACTERSCHECK = 500;
 	
 	public ShowQuestionsActivityTest() {
 		super(ShowQuestionsActivity.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 	/* Begin list of the different tests to be performed */
 
 	public void testDisplayQuestion() {
 		solo.assertCurrentActivity("A question is being displayed",
                 ShowQuestionsActivity.class);
 		ListView l = solo.getCurrentListViews().get(0);
 		
 		assertNotNull("No list views!", l);
 		solo.sleep(SLEEP_LISTCHECK);
 		assertTrue("No items in list view!", l.getChildCount()>0);
 		
 		for (int i = 0; i < NUMBER_OF_QUESTIONS; i++) {
 			for (int childIndex = 0; childIndex < l.getAdapter().getCount(); childIndex++) {
 				View childView = l.getChildAt(childIndex);
 				
 				if (childView != null) {
 					solo.clickOnView(childView);
 				}
 			}
 			
 			assertTrue(solo.searchText("\u2718") || solo.searchText("\u2714"));
 			
 			solo.clickOnButton("Next question");
 			solo.sleep(SLEEP_CHARACTERSCHECK);
 			assertFalse(solo.searchText("\u2718") && solo.searchText("\u2714"));
 		}
 
 		
 		
 	}
 	
 	public void testNoNetwork() {
 		
 		solo.assertCurrentActivity("A question is being displayed",
                 ShowQuestionsActivity.class);
     	new LoadRandomQuestion(getActivity()).execute("http://www.google.com");
    	assertTrue(solo.searchText("There was an error retrieving the question"));
     	new LoadRandomQuestion(getActivity()).execute("http://0.0.0.0");
    	assertTrue(solo.searchText("There was an error retrieving the question"));
 	}
 	
 	/* End list of the different tests to be performed */
 	
 	@Override
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 	}
 
 }
