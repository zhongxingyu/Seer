 package edu.bonn.mobilegaming.geoquest.mission;
 
 import static com.qeevee.util.StringTools.trim;
 import static edu.bonn.mobilegaming.geoquest.Variables.registerMissionResult;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.dom4j.Element;
 
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.qeevee.gq.xml.XMLUtilities;
 
 import edu.bonn.mobilegaming.geoquest.Globals;
 import edu.bonn.mobilegaming.geoquest.R;
 import edu.bonn.mobilegaming.geoquest.Variables;
 import edu.bonn.mobilegaming.geoquest.ui.abstrakt.MissionOrToolUI;
 
 /**
  * Text question and answer mission. It shows the player a question and lets him
  * enter free text into an answering text area. Then it checks the entered
  * answer against a list of accepted values and either accepts or rejects the
  * answer.
  * 
  * @author Holger Muegge
  */
 public class TextQuestion extends InteractiveMission {
 	/** text view for displaying text */
 	private TextView textView;
 	private EditText answerEditText;
 	private Button button;
 	private OnClickListener questionModeButtonOnClickListener,
 			replyModeButtonOnClickListener;
 
 	private CharSequence replyTextOnCorrect;
 	private CharSequence replyTextOnWrong;
 	private CharSequence questionText;
 
 	private int mode = 0;
 	private static final int MODE_QUESTION = 1;
 	private static final int MODE_REPLY_TO_CORRECT_ANSWER = 2;
 	private static final int MODE_REPLY_TO_WRONG_ANSWER = 3;
 
 	public List<String> answers = new ArrayList<String>();
 	private String storeVariable;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		init();
 		setMode(MODE_QUESTION);
 	}
 
 	/**
 	 * Clears and (re-) populates the complete view.
 	 * 
 	 * @param newMode
 	 */
 	private void setMode(int newMode) {
 		if (mode == newMode)
 			return;
 		// real change in mode:
 		mode = newMode;
 
 		String givenAnswer = answerEditText.getText().toString();
 
 		switch (mode) {
 		case MODE_QUESTION:
 			textView.setText(questionText);
 			answerEditText.setVisibility(View.VISIBLE);
 			answerEditText.setText("");
 			button.setText(R.string.button_text_accept);
 			button.setOnClickListener(questionModeButtonOnClickListener);
 			break;
 		case MODE_REPLY_TO_CORRECT_ANSWER:
 			textView.setText(replyTextOnCorrect);
 			answerEditText.setVisibility(View.INVISIBLE);
 			button.setText(R.string.button_text_proceed);
 			registerMissionResult(mission.id, givenAnswer);
 			if (storeVariable != null) {
 				Variables.setValue(storeVariable, givenAnswer);
 			}
 			invokeOnSuccessEvents();
 			button.setOnClickListener(replyModeButtonOnClickListener);
 			break;
 		case MODE_REPLY_TO_WRONG_ANSWER:
 			textView.setText(replyTextOnWrong);
 			answerEditText.setVisibility(View.INVISIBLE);
 			if (loopUntilSuccess && answers.size() > 0)
 				button.setText(R.string.button_text_repeat);
 			else
 				button.setText(R.string.button_text_proceed);
 			registerMissionResult(mission.id, givenAnswer);
 			invokeOnFailEvents();
 			button.setOnClickListener(replyModeButtonOnClickListener);
 			break;
 		}
 
 		answerEditText.invalidate();
 	}
 
 	/**
 	 * This is only called once, when the activity is initialized.
 	 */
 	private void init() {
 		setContentView(R.layout.textquestion);
 		textView = (TextView) findViewById(R.id.textquestion_questionTV);
 		initButton();
 		initAnswerEditText();
 		initContent();
 	}
 
 	public void initAnswerEditText() {
 		answerEditText = (EditText) findViewById(R.id.textquestion_answerET);
 		answerEditText.setHint(getMissionAttribute("prompt",
 				R.string.textquestion_answerET_hint_default));
 		answerEditText.addTextChangedListener(new TextWatcher() {
 
 			public void afterTextChanged(Editable s) {
 			}
 
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				button.setEnabled(count > 0);
 			}
 
 		});
 		answerEditText
 				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 
 					public boolean onEditorAction(TextView v, int actionId,
 							KeyEvent event) {
 						if (actionId == EditorInfo.IME_ACTION_DONE) {
 							evaluateAnswer();
 						}
 						return false;
 					}
 				});
 		answerEditText.setText("");
 	}
 
 	public void initButton() {
 		button = (Button) findViewById(R.id.textquestion_acceptBT);
 		button.setEnabled(false);
 
 		questionModeButtonOnClickListener = new OnClickListener() {
 
 			public void onClick(View v) {
 				evaluateAnswer();
 			}
 
 		};
 
 		replyModeButtonOnClickListener = new OnClickListener() {
 
 			public void onClick(View v) {
 				if (mode == MODE_REPLY_TO_WRONG_ANSWER)
 					if (loopUntilSuccess && answers.size() > 0)
 						setMode(MODE_QUESTION);
 					else
 						finish(Globals.STATUS_FAIL);
 				else
 					finish(Globals.STATUS_SUCCEEDED);
 			}
 		};
 	}
 
 	private boolean answerAccepted() {
 		String givenAnswer = answerEditText.getText().toString();
 		boolean found = false;
 		for (Iterator<String> iterator = answers.iterator(); iterator.hasNext();) {
 			String answer = (String) iterator.next();
 			found |= givenAnswer.matches(answer);
 		}
 		return found;
 	}
 
 	private void evaluateAnswer() {
 		if (answerAccepted())
 			setMode(MODE_REPLY_TO_CORRECT_ANSWER);
 		else
 			setMode(MODE_REPLY_TO_WRONG_ANSWER);
 	}
 
 	private void initContent() {
 		questionText = getMissionAttribute("question",
 				XMLUtilities.NECESSARY_ATTRIBUTE);
 		replyTextOnCorrect = getMissionAttribute("replyOnCorrect",
 				R.string.question_reply_correct_default);
 		replyTextOnWrong = getMissionAttribute("replyOnWrong",
 				R.string.question_reply_wrong_default);
 		storeVariable = (String) getMissionAttribute(
 				"storeAcceptedAnswerInVariable",
 				XMLUtilities.OPTIONAL_ATTRIBUTE);
 
 		@SuppressWarnings("unchecked")
 		List<Element> xmlAnswers = ((Element) mission.xmlMissionNode)
 				.selectNodes("answers/answer");
 		for (Iterator<Element> j = xmlAnswers.iterator(); j.hasNext();) {
 			Element xmlAnswer = j.next();
 			answers.add(trim(xmlAnswer.getText()));
 		}
 	}
 
 	public void onBlockingStateUpdated(boolean blocking) {
 		button.setEnabled(!blocking);
 	}
 
 	public MissionOrToolUI getUI() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
