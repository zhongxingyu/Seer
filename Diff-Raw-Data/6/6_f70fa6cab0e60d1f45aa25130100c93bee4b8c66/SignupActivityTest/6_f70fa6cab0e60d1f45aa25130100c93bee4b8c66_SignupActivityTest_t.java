 package uw.cse403.minion.test;
 
 import uw.cse403.minion.R;
 import uw.cse403.minion.SignupActivity;
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.jayway.android.robotium.solo.Solo;
 
 public class SignupActivityTest extends ActivityInstrumentationTestCase2<SignupActivity> {
 	private static final int SPINNER_INDEX = 0;
 	private static final String SPINNER_QUESTION_1 = "What is your favorite color?";
 	private static final String USERNAME = "username";
 	private static final String PASSWORD = "password";
 	private static final String PASSWORD_CONFIRMATION = "passwordConfirmation";
 	private static final String QUESTION = "question";
 	private static final String ANSWER = "answer";
 	private static final String IS_VALID_PASSWORD = "isValidPassword";
 	private static final String PASSWORDS_MATCH = "passwordsMatch";
 	private static final String USERNAME_IN_USE = "usernameInUse";
 	private static final String VALID_USERNAME = "test";
 	private static final String VALID_PASSWORD = "abcDEF123@";
 	
 	public SignupActivityTest() {
 		super(SignupActivity.class);
 	}
 	
 	@Override
 	protected void setUp() {
 		
 	}
 	
 	public void testCanSelectInSpinner() {
 		Solo solo = new Solo(getInstrumentation(), getActivity());
 		solo.pressSpinnerItem(SPINNER_INDEX, 1);
		//Spinner securityQuestions = (Spinner) solo.ge;
		//String actualQuestion = securityQuestions.getSelectedItem().toString();
		assertTrue(solo.isSpinnerTextSelected(SPINNER_QUESTION_1));
 
 		getActivity().finish();
 	}
 	
 	public void testCanEnterText() {
 		Solo solo = new Solo(getInstrumentation(), getActivity());
 		EditText username = (EditText) solo.getView(uw.cse403.minion.R.id.username_input); 
 		EditText password = (EditText) solo.getView(uw.cse403.minion.R.id.password_input);
 		EditText passwordMatch = (EditText) solo.getView(uw.cse403.minion.R.id.confirm_password_input);
 		solo.typeText(username, VALID_USERNAME);
 		solo.typeText(password, VALID_PASSWORD);
 		solo.typeText(passwordMatch, VALID_PASSWORD);
 		String usernameString = username.getText().toString();
 		String passwordString = password.getText().toString();
 		String passwordMatchString = passwordMatch.getText().toString();
 		assertTrue(VALID_USERNAME.equals(usernameString));
 		assertTrue(VALID_PASSWORD.equals(passwordString));
 		assertTrue(VALID_PASSWORD.equals(passwordMatchString));
 		getActivity().finish();
 	}
 	
 	public void testUsernameInUse() {
 		Intent i = new Intent();
 		i.putExtra(USERNAME_IN_USE, true);
 		setActivityIntent(i);
 		TextView errorView = (TextView) getActivity().findViewById(uw.cse403.minion.R.id.username_error);
 		assertTrue(errorView.isShown());
 		assertNotNull(errorView);
 		getActivity().finish();
 	}
 	
 	public void testInvalidPassword() {
 		Intent i = new Intent();
 		i.putExtra(IS_VALID_PASSWORD, false);
 		setActivityIntent(i);
 		Solo solo = new Solo(getInstrumentation(), getActivity());
 		TextView errorView = (TextView) getActivity().findViewById(uw.cse403.minion.R.id.invalid_password_error);
 		assertTrue(errorView.isShown());
 		assertNotNull(errorView);
 		getActivity().finish();
 	}
 	
 	public void testNonMatchingPassword() {
 		Intent i = new Intent();
 		i.putExtra(PASSWORDS_MATCH, false);
 		setActivityIntent(i);
 		TextView errorView = (TextView) getActivity().findViewById(uw.cse403.minion.R.id.nonmatching_password_error);
 		assertTrue(errorView.isShown());
 		assertNotNull(errorView);
 		getActivity().finish();
 	}
 	
 	
 	@Override
 	protected void tearDown() {
 		getActivity().finish();
 	}
 }
