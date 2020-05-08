 package epfl.sweng.editquestions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 import epfl.sweng.R;
 import epfl.sweng.globals.Globals;
 import epfl.sweng.quizquestions.QuizQuestion.QuizQuestionParam;
 import epfl.sweng.tasks.SubmitQuestion;
 import android.content.Intent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.EditText;
 
 /**
  * A class to handle all the Buttons of an activity
  */
 public class ButtonListener implements OnClickListener {
 
 	private EditQuestionActivity mActivity;
 	
 	public ButtonListener(EditQuestionActivity activity) {
 		super();
 		mActivity = activity;
 	}
 
 	/**
 	 * A generic onClick method to manage the different buttons of the activity
 	 */
 	@Override
 	public void onClick(View v) {
 		String buttonTag = ((Button) v).getTag().toString();
 		LinearLayout answersContainer = (LinearLayout) mActivity.findViewById(R.id.edit_answers_container);
 		System.out.println("Button " + buttonTag + " clicked");
 		// On click '+' button: add a new answer and set its Listeners
 		if (buttonTag == mActivity.getResources().getText(R.string.plus_sign)) {
 			View newAnswer = mActivity.getLayoutInflater().inflate(R.layout.edit_new_answer, null);
 			answersContainer.addView(newAnswer, answersContainer.getChildCount());
 			// Set OnClickListeners
 			List<Button> listNewButton = findAllButtons(
 					(ViewGroup) answersContainer.getChildAt(answersContainer.getChildCount()-1));
 			for (Button button : listNewButton) {
 				button.setOnClickListener(new ButtonListener(mActivity));
 			}
 			// Set EditTextWatcher
 			EditTextWatcher editTextWatcher = new EditTextWatcher(mActivity);
 			List<EditText> listNewEditTexts= editTextWatcher.findAllEditTexts(
 					(ViewGroup) answersContainer.getChildAt(answersContainer.getChildCount()-1));
 			for (EditText editText : listNewEditTexts) {
 				editText.addTextChangedListener(new EditTextWatcher(mActivity, editText));
 			}
 			
 			// Prevent user from entering more than MAX_NUMBER_OF_ANSWERS answers
 			if (answersContainer.getChildCount() >= Globals.MAX_NUMBER_OF_ANSWERS) {  
 				v.setEnabled(false);
 			}
 		
 		
 		// On click '-' button: remove corresponding answer and enable '+'
 		} else if (buttonTag == mActivity.getResources().getText(R.string.hyphen_minus)) {
 			answersContainer.removeView((View) v.getParent());
 			Button newAnswerButton = (Button) mActivity.findViewById(R.id.edit_button_new_answer);
 			newAnswerButton.setEnabled(true);
 		
 		
 		// On click 'X' button: check all other right answers as wrong and this one as right and update question
 		} else if (buttonTag == mActivity.getResources().getText(R.string.heavy_ballot_x)) {
 			List<Button> listWrongButton = findAllButtons(
 					(ViewGroup) answersContainer, mActivity.getResources().getText(R.string.heavy_check_mark));
 			for (Button button : listWrongButton) {
 				button.setText(mActivity.getResources().getText(R.string.heavy_ballot_x));
 				button.setTag(mActivity.getResources().getText(R.string.heavy_ballot_x));
 			}
 			
 			((Button) v).setText(mActivity.getResources().getText(R.string.heavy_check_mark));
 			((Button) v).setTag(mActivity.getResources().getText(R.string.heavy_check_mark));
 			mActivity.buildQuestionFromView(v, QuizQuestionParam.SOLUTION_INDEX,
 					Integer.toString(((ViewGroup) v.getParent().getParent()).indexOfChild((View) v.getParent())));
 		
 		
 		// On click 'V' button: check corresponding answer as wrong and update question
 		} else if (buttonTag == mActivity.getResources().getText(R.string.heavy_check_mark)) {
 			((Button) v).setText(mActivity.getResources().getText(R.string.heavy_ballot_x));
 			((Button) v).setTag(mActivity.getResources().getText(R.string.heavy_ballot_x));
 			mActivity.buildQuestionFromView(v, QuizQuestionParam.SOLUTION_INDEX, "-1");
 		
 		
 		// On click 'Submit' button
 		} else if (buttonTag == mActivity.getResources().getText(R.string.edit_button_submit)) {
 			new SubmitQuestion(mActivity).execute(mActivity.getQuestion());
 			if (mActivity.getQuestion().auditErrors(0) != 0) {
 			} else {
				Intent intent = new Intent(mActivity, EditQuestionActivity.class);
				mActivity.finish();
				mActivity.startActivity(intent);
 			}
 
 		}
 	}
 	
     /**
      * Finds recursively all the buttons inside a ViewGroup
      * @param v ViewGroup in which to search
      * @return List<Button> The list of all the Buttons
      */
     public List<Button> findAllButtons(ViewGroup v) {
     	List<Button> list = new ArrayList<Button>();
     	int nbChildren = v.getChildCount();
     	for (int i=0; i<nbChildren; i++) {
     		if (v.getChildAt(i) instanceof Button) {
     			list.add((Button) v.getChildAt(i));
     		} else if (v.getChildAt(i) instanceof ViewGroup) {
     			list.addAll(findAllButtons((ViewGroup) v.getChildAt(i)));
     		}
     	}
     	return list;
     }
     
     /**
      * Finds recursively all the tagged buttons inside a ViewGroup
      * @param v ViewGroup in which to search
      * @param tag The tag that buttons must have
      * @return List<Button> The list of all the Buttons
      */
     public List<Button> findAllButtons(ViewGroup v, Object tag) {
     	List<Button> list = new ArrayList<Button>();
     	int nbChildren = v.getChildCount();
     	for (int i=0; i<nbChildren; i++) {
     		if (v.getChildAt(i) instanceof Button && v.getChildAt(i).getTag() == tag) {
     			list.add((Button) v.getChildAt(i));
     		} else if (v.getChildAt(i) instanceof ViewGroup) {
     			list.addAll(findAllButtons((ViewGroup) v.getChildAt(i), tag));
     		}
     	}
     	return list;
     }
 
 }
