 package epfl.sweng.editquestions;
 
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import epfl.sweng.R;
 import epfl.sweng.quizquestions.QuizQuestion;
 import epfl.sweng.quizquestions.QuizQuestion.QuizQuestionParam;
 
 /**
  * Activity enabling the user to edit a question
  *
  */
 public class EditQuestionActivity extends Activity {
 
 	private QuizQuestion mEditedQuestion;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_edit_question);
         
         mEditedQuestion = new QuizQuestion();
         
         ButtonListener buttonListener = new ButtonListener(this);
         List<Button> listButtons = buttonListener.findAllButtons(
         		(ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content));
         for (Button button : listButtons) {
         	button.setOnClickListener(new ButtonListener(this));
         }
         
         EditTextWatcher editTextWatcher = new EditTextWatcher(this);
         List<EditText> listEditTexts = editTextWatcher.findAllEditTexts(
         		(ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content));
         for (EditText editText : listEditTexts) {
         	editText.addTextChangedListener(new EditTextWatcher(this, editText));
         }
         System.out.println("EditQuestionActivity()");
     }
 
     /**
      * Add a field to mEditedQuestion from the user input in a View
      * Calls EditText.setError() if invalid param
      * @param view The View from which value comes from
      * @param param enum of the parameters
      * @param value value of the field to add
      * @return true if mEditedQuestion is valid according to mEditedQuestion.auditErrors()
      */
     public boolean buildQuestionFromView(View view, QuizQuestionParam param, String value) {
     	
     	switch (param) {
     	
 	    	case QUESTION:
 	    		if (!mEditedQuestion.checkString(value)) {
 	    			((EditText) view).setError("The question must be non-empty or have less than 500 characters");
 	    		} else {
 	    			mEditedQuestion.setQuestion(value);
 	    			((EditText) view).setError(null);
 	    		}
 	    		return mEditedQuestion.auditErrors(0) == 0;
 	    		
 	    	case ANSWER:
 	    		if (!mEditedQuestion.checkString(value)) {
 	    			((EditText) view).setError("An answer must be non-empty or have less than 500 characters");
 	    		} else {
 	    			mEditedQuestion.addAnswerAtIndex(value, 
 	    					((ViewGroup) view.getParent().getParent()).indexOfChild((View) view.getParent()));
 	    			((EditText) view).setError(null);
 	    		}
 	    		return mEditedQuestion.auditErrors(0) == 0;
 	    		
 	    	case SOLUTION_INDEX:
 	    		mEditedQuestion.setSolutionIndex(Integer.parseInt(value));
 	    		if (value.equals("-1")) {
 	    			Toast toast = Toast.makeText(this, "One answer should be marked as correct", Toast.LENGTH_SHORT);
 	    			toast.show();
 	    		}
 	    		return mEditedQuestion.auditErrors(0) == 0;
 	    		
 	    	case TAGS:    		
 	    		String[] tags = value.split("[^a-zA-Z0-9']");
 	    		boolean flag = true;
 	    		for (String tag : tags) {
 	    			flag = flag && mEditedQuestion.checkTag(tag);
 	    			if (!flag) {
 	    				((EditText) view).setError("A tag must be less than 20 alphanumeric characters");
 	    				break;
 	    			}
 	    		}
 				if (flag) {
 					Set<String> tagsSet = new HashSet<String>(Arrays.asList(tags));
 					mEditedQuestion.setTags(tagsSet);
 					((EditText) view).setError(null);
 				}
 	    		return mEditedQuestion.auditErrors(0) == 0;
 	    		
 	    	default:
 	    		return mEditedQuestion.auditErrors(0) == 0;
     	}
     }
     
 
 
 	public QuizQuestion getQuestion() {
 		// TODO Auto-generated method stub
 		return mEditedQuestion;
 	}
 
 	
     public void displaySubmitError() {
    	Toast.makeText(this, getString(R.string.submit_question_error_text), Toast.LENGTH_LONG).show();
 		
     }
     
 	public void displaySuccess(QuizQuestion question) {
		Toast.makeText(this, String.format(getString(R.string.submit_question_success_text), question.getId()), 
 				Toast.LENGTH_SHORT).show();		
 	}
 }
