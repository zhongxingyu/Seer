 package de.da_sense.moses.client;
 
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
 import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
 import de.da_sense.moses.client.service.MosesService;
 import de.da_sense.moses.client.userstudy.Form;
 import de.da_sense.moses.client.userstudy.PossibleAnswer;
 import de.da_sense.moses.client.userstudy.Question;
 import de.da_sense.moses.client.userstudy.Survey;
 import de.da_sense.moses.client.util.Log;
 
 /**
  * questionnaires for a user study
  * 
  * @author Ibrahim Alyahya, Sandra Amend, Florian Schnell, Wladimir Schmidt
  * @author Zijad Maksuti
  */
 public class QuestionnaireFragment extends Fragment {
 	/**
 	 * Defining a log tag to this class
 	 */
 	private static final String TAG = "QuestionnaireFragment";
 	
 	/**
 	 * To send (and to save on local) user's answers of a questionnaire to
 	 * server
 	 */
 	private Button btnSend;
 
 	/**
 	 * The Single Questionnaire to display
 	 */
 	private Survey usQuestionnaire;
 	
 	/**
 	 * The APKID
 	 */
 	private String apkid = "";
 	
 	/**
 	 * Index of the current Questionnaire
 	 */
 	private int currentIndex;
 	
 	/**
 	 * Index of the last Questionnaire in the Multi_Questionnaire
 	 */
 	private int lastIndex;
 
 	/**
 	 * The "Next Questionnaire" Button
 	 */
 	private Button btnNext;
 
 	/**
 	 * Remove the send button from the layout.
 	 */
 	private void removingButtonsFromThisLayout() {
 		Log.d(TAG, "removingButtonsFormThisLayout: btnSend = " + btnSend);
 		if (btnSend != null && btnSend.getVisibility() == View.VISIBLE)
 			btnSend.setVisibility(View.INVISIBLE);
 		Log.d(TAG, "removingButtonsFormThisLayout: btnNext = " + btnNext);
 		if (btnNext != null && btnNext.getVisibility() == View.VISIBLE)
 			btnNext.setVisibility(View.INVISIBLE);
 	}
 
 	/**
 	 * Adds Buttons to this Layout. If the current questionnaire's index
 	 * is not the last index it adds a Next Button, else an Send Button 
 	 * @param layout
 	 *            the Linear layout to be added on
 	 */
 	private void addingButtonsToThisLayout(LinearLayout ll) {
 //		final LinearLayout layout = (LinearLayout) ll
 //				.findViewById(R.id.ll_quest);
 		LinearLayout bLayout = (LinearLayout) ll
 				.findViewById(R.id.bottom_quest);
 		if (currentIndex == lastIndex) {
 		// Adding save button
 		btnSend = new Button(getActivity().getApplicationContext());
 		btnSend.setText(getString(R.string.q_send));
 		btnSend.setTag("SendButton");
 		btnSend.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 //				usQuestionnaire.setAnswers(layout);
 				Toast.makeText(getActivity().getApplicationContext(), getString(R.string.q_answersSent), Toast.LENGTH_LONG).show();
 //				InstalledExternalApplicationsManager.getInstance().getAppForId(apkid).getSurvey().sendAnswersToServer();
 				// XXX Test ob es speichern kann
 				try {
 					InstalledExternalApplicationsManager.getInstance().saveToDisk(MosesService.getInstance());
 				} catch (IOException e) {
 					Log.d("Flo", "Konnte nicht speichern");
 					e.printStackTrace();
 				}
 				// XXX Test ob es speichern kann
 				v.setEnabled(false); // disable buttons
 				}
 			}
 		);
 		bLayout.addView(btnSend);
 		} else {
 		btnNext= new Button(getActivity().getApplicationContext());
 		btnNext.setText(getString(R.string.q_next));
 		btnNext.setTag("NextButton");
 		btnNext.setEnabled(true);
 		btnNext.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Sets he answers of the current Questionnaire in order
 				// to swap to the next one				
 //				usQuestionnaire.setAnswers(layout);
 				FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
 				QuestionnaireFragment nextFragment = new QuestionnaireFragment();
 				nextFragment.setRetainInstance(true);
 				Bundle args = getArguments();
 				args.putInt("de.da_sense.moses.client.current", currentIndex + 1);
 				nextFragment.setArguments(args);
 				ft.replace(android.R.id.content, nextFragment);
 				ft.commit();
 			}
 		});
 		bLayout.addView(btnNext);
 		}
 	}
 
 	/**
 	 * Adds a form with all its questions to the ScrollView so that it is visible to the user.
 	 * 
 	 * @param form the form to be displayed
 	 * @param ll the contained of the scrollview
 	 */
 	private void addFormToLayout(Form form, LinearLayout scrollViewContainer) {
 
 		LinearLayout linearLayoutInsideAScrollView = (LinearLayout) scrollViewContainer.findViewById(R.id.ll_quest);
 		List<Question> questions = form.getQuestions();
 
 		// Check if there is at least a question to display
 		if (questions.size() > 0) {
 			// add the label of the form to the scroll view
 			TextView formLabel = new TextView(getActivity());
 			formLabel.setText(form.getTitle());
 			linearLayoutInsideAScrollView.addView(formLabel);
 			
 			for (int i = 0; i < questions.size(); i++) {
 				Question question = questions.get(i);
 				switch (questions.get(i).getType()) {
 				case (Question.TYPE_SINGLE_CHOICE): {
 					makeSingleChoice(questions.get(i), linearLayoutInsideAScrollView, i);
 					break;
 				}
 				case (Question.TYPE_MULTIPLE_CHOICE): {
 					makeMultipleChoice(questions.get(i), linearLayoutInsideAScrollView, i);
 					break;
 				}
 				case (Question.TYPE_TEXT_QUESTION): {
 					makeTextQuestion(questions.get(i), linearLayoutInsideAScrollView, i);
 					break;
 				}
 				case (Question.TYPE_LIKERT_SCALE):{
 					makeSingleChoice(question, linearLayoutInsideAScrollView, i);
 					break;
 				}
 				case (Question.TYPE_YES_NO_QUESTION):{
 					makeSingleChoice(question, linearLayoutInsideAScrollView, i);
 					break;
 				}
 				default:
 					break;
 				}
 			}
 		} else {
 			Log.d(TAG, "No Questions found in questionnaire["+currentIndex+"]");
 		}
 
 	}
 
 	/**
 	 * Displays a single choice question to the user.
 	 * @param question the question to be displayed
 	 * @param linearLayoutInsideAScrollView the view to add the question to
 	 * @param ordinal the ordinal number of the question i.e. 1, 2, 3, 4 or 5
 	 */
 	private void makeSingleChoice(Question question, LinearLayout linearLayoutInsideAScrollView, int ordinal) {
 		String questionText = question.getTitle();
 		List<PossibleAnswer> possibleAnswers = question.getPossibleAnswers();
 
 		TextView questionView = new TextView(getActivity());
 		questionView.setText(ordinal + ". " + questionText);
 
 		linearLayoutInsideAScrollView.addView(questionView);
 
 		final RadioButton[] rb = new RadioButton[possibleAnswers.size()];
 		RadioGroup rg = new RadioGroup(getActivity()); // create the
 																// RadioGroup
 
 
 		rg.setOrientation(RadioGroup.VERTICAL);// or RadioGroup.VERTICAL
 		for (int i = 0; i < rb.length; i++) {
 			rb[i] = new RadioButton(getActivity());
 			rg.addView(rb[i]); // the RadioButtons are added to the radioGroup
 								// instead of the layout
 			PossibleAnswer possibleAnswer = possibleAnswers.get(i);
 			rb[i].setText(possibleAnswer.getTitle());
 			rb[i].setVisibility(View.VISIBLE);
 		}
 		rg.setVisibility(View.VISIBLE);
 		Log.i(TAG, "last rg = " + rg);
 		linearLayoutInsideAScrollView.addView(rg);
 	}
 
 	/**
 	 * Displays a multiple choice question to the user.
 	 * @param question the question to be displayed
 	 * @param linearLayoutInsideAScrollView the view to add the question to
 	 * @param ordinal the ordinal number of the question i.e. 1, 2, 3, 4 or 5
 	 */
 	private void makeMultipleChoice(Question question, LinearLayout linearLayoutInsideAScrollView, int ordinal) {
 
 		String questionText = question.getTitle();
 		List<PossibleAnswer> possibleAnswers = question.getPossibleAnswers();
 
 		TextView questionView = new TextView(getActivity());
 		questionView.setText(ordinal + ". " + questionText);
 		linearLayoutInsideAScrollView.addView(questionView);
 
 		Log.i(TAG, "questionView = " + questionView.getText());
 
 		final CheckBox[] checkBoxs = new CheckBox[possibleAnswers.size()];
 		for (int i = 0; i < checkBoxs.length; i++) {
 			checkBoxs[i] = new CheckBox(getActivity());
 			checkBoxs[i].setText(possibleAnswers.get(i).getTitle());
 			checkBoxs[i].setVisibility(View.VISIBLE);
 			linearLayoutInsideAScrollView.addView(checkBoxs[i]);
 		}
 	}
 
 	/**
 	 * Displays a text question to the user.
 	 * @param question the question to be displayed
 	 * @param linearLayoutInsideAScrollView the view to add the question to
 	 * @param ordinal the ordinal number of the question i.e. 1, 2, 3, 4 or 5
 	 */
 	private void makeTextQuestion(Question question, LinearLayout linearLayoutInsideAScrollView, int ordinal) {
 		TextView questionView = new TextView(getActivity());
 		questionView.setText(ordinal + ". " + question.getTitle());
 		linearLayoutInsideAScrollView.addView(questionView);
 
 		EditText editText = new EditText(getActivity());
 		editText.setVisibility(View.VISIBLE);
 		if (question.getAnswer() != null) {
 			editText.setText(question.getAnswer());
 		}
 		linearLayoutInsideAScrollView.addView(editText);
 	}
 
 	/**
 	 * Creates a Layout for a Single_Questionnaire
 	 * 
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		container.setBackgroundColor(getResources().getColor(
 				android.R.color.background_light));
 
 		apkid = "";
 		if (savedInstanceState == null) {
 			Log.d(TAG, "savedInstance == null");
 			savedInstanceState = getArguments();
 			Log.d(TAG, "NOW savedInstance = " + savedInstanceState);
 		}
 
 		// retrieve the arguments
 		apkid = savedInstanceState
 				.getString("de.da_sense.moses.client.apkid");
 		savedInstanceState
 				.getInt("de.da_sense.moses.client.belongsTo");
 		Log.d(TAG, "\nretireved apkid = " + apkid);
 		currentIndex = savedInstanceState.getInt("de.da_sense.moses.client.current");
 
 		if (apkid != null) {
 			Log.d(TAG, "savedInstanceState.apkid = " + apkid);
 		} else {
 			Log.d(TAG, "Error while retrieving APKID");
 		}
 
 		// get the corresponding installedApp
 		InstalledExternalApplication app = InstalledExternalApplicationsManager
 				.getInstance().getAppForId(apkid);
 		
 		if (app != null) {
 			usQuestionnaire = app.getSurvey();
 		}
 		Log.d(TAG, "usQuestionnaire = " + usQuestionnaire);
 		if (usQuestionnaire == null) {
 			Log.d(TAG, "Error while trying to get the "+currentIndex+"-th Single_Questionnaire");
 		}
 
 		LinearLayout ll = (LinearLayout) inflater.inflate(
 				R.layout.questionnaire, container, false);
 		
 		// retrieve the surveys for this User Study
 		for(Form form : usQuestionnaire.getForms())
 			addFormToLayout(form, ll);
 
 		// TODO Disable Saving if it was already sent to the server
 		if (!app.getSurvey().hasBeenSent()) {
 			Log.i(TAG, "this questionnaire can be sent");
 			addingButtonsToThisLayout(ll);
 		} else {
 			Log.i(TAG, "this questionnaire was sent");
 			removingButtonsFromThisLayout();
 		}
 		return (View) ll;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 //		usQuestionnaire.setAnswers(ll);
 		super.onSaveInstanceState(outState);
 	}
 	
 }
