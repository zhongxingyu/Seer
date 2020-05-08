 package epfl.sweng.test;
 import org.json.JSONException;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.EditText;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import epfl.sweng.editquestions.EditQuestionActivity;
 import epfl.sweng.quizquestions.QuizQuestion;
 import epfl.sweng.servercomm.SwengHttpClientFactory;
 import epfl.sweng.tasks.IQuizServerCallback;
 import epfl.sweng.tasks.SubmitQuestion;
 import epfl.sweng.test.mocking.MockHttpClient;
 import epfl.sweng.test.tools.TestingTricks;
 /**
  * First test case...
  */
 public class EditQuestionActivityTest extends
 		ActivityInstrumentationTestCase2<EditQuestionActivity> {
 	
 	private Solo solo;
 	private final static int WAIT_TIME = 4000;
 	private final static String TEST_QUESTION = "Test question...";
 	private final static String TEST_FALSEANSWER = "False Answer";
 	private final static String TEST_RIGHTANSWER = "Right Answer";
 	private final static String TEST_TAGS = "test";
 	
 	
 	public EditQuestionActivityTest() {
 		super(EditQuestionActivity.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
         SwengHttpClientFactory.setInstance(new MockHttpClient());
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 	/* Begin list of the different tests to be performed */
 
 	public void testEditQuestion() {
 		TestingTricks.authenticateMe(solo);
		if(solo.searchText("Submit quiz question")) {
 			solo.clickOnButton("Submit quiz question");
 		}
 		
 		solo.assertCurrentActivity("Edit Question Form is being displayed",
                 EditQuestionActivity.class);
 
 
 		solo.clickOnButton("\\+");
 		solo.clickOnButton("\\-");
 		solo.clickOnButton("\u2718");
 		assertTrue(solo.searchText("\u2714"));
 		solo.clickOnButton("\u2714");
     	assertTrue(solo.waitForText("One answer should be marked as correct"));		
     	solo.clickOnButton("\\+");
     	solo.sleep(WAIT_TIME);
     	boolean rightAnswerEntered = false;
 		for (EditText et: solo.getCurrentEditTexts()) {
 			if (et.getTag().toString() 
 				== getActivity().getResources().getText(epfl.sweng.R.string.edit_question_hint)) {
 				solo.enterText(et, TEST_QUESTION);
 			} else if (et.getTag().toString() 
 				== getActivity().getResources().getText(epfl.sweng.R.string.edit_tags_hint)) {
 				solo.enterText(et, TEST_TAGS);
 			} else if (et.getTag().toString() 
 				== getActivity().getResources().getText(epfl.sweng.R.string.edit_answer_hint)) {
 				
 				if (rightAnswerEntered) {
 					solo.enterText(et, TEST_FALSEANSWER);
 				} else {
 					solo.enterText(et, TEST_RIGHTANSWER);
 					rightAnswerEntered = true;
 				}
 			}
 		}
 		solo.clickOnButton("\u2718");
 		solo.sleep(WAIT_TIME);
 		solo.clickOnButton("Submit");
 		assertTrue(solo.waitForText("\u2714 Question successfully submitted"));
 	}
 
 	
 	public void testNoNetwork() {
 		solo.assertCurrentActivity("Edit question form is being displayed",
                 EditQuestionActivity.class);
 		
 		
 		final EditQuestionActivity activity = getActivity();
 		
 		IQuizServerCallback callback = new IQuizServerCallback() {
         	public void onSuccess(QuizQuestion question) {
         		activity.displaySuccess(question);
         	}
         	public void onError() {
         		activity.displaySubmitError();
         	}
         };
 		
 		
 		
     	new SubmitQuestion(callback).execute(new QuizQuestion(), "http://www.google.com");
     	assertTrue(solo.waitForText("\u2718 An error occured while submitting the Question"));
     	solo.sleep(WAIT_TIME);
     	try {
 			new SubmitQuestion(callback).execute(new QuizQuestion(QuizQuestionTest.VALID_QUESTION_JSON), 
 					"http://0.0.0.0");
 		} catch (JSONException e) {
 			fail();
 		}
     	assertTrue(solo.waitForText("\u2718 An error occured while submitting the Question"));
 	}
 	
 	/* End list of the different tests to be performed */
 	
 	
 	@Override
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 	}
 
 }
