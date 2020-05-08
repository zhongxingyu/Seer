 /**
  * Main UI for the application. Displays a list of questions.
  * 
  * @author Cody Andrews, Phillip Leland, Justin Robb 05/01/2013
  * 
  */
 
 package com.huskysoft.interviewannihilator.ui;
 
 import java.util.List;
 
 import com.huskysoft.interviewannihilator.R;
 import com.huskysoft.interviewannihilator.model.*;
 import com.huskysoft.interviewannihilator.runtime.*;
 import com.huskysoft.interviewannihilator.util.UIConstants;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.ActionBar.LayoutParams;
 import android.app.Dialog;
 import android.content.Intent;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.style.TextAppearanceSpan;
 import android.util.DisplayMetrics;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.ViewGroup;
 import android.widget.Adapter;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends AbstractPostingActivity {
 
 	/**
 	 * Used to pass the String question to the child activity.
 	 * Will pass a Question object.
 	 */
 	public final static String EXTRA_MESSAGE =
 			"com.huskysoft.interviewannihilator.QUESTION";
 	
 	/** 
 	 * Number of questions currently being displayed,used to index
 	 * into the db
 	 */
 	private int questionOffset = 0;
 	
 	/**
 	 * Method that populates the app when the MainActivity is created.
 	 * Initializes the questions and questionll fields. Also calls
 	 * the displayQuestions function.
 	 */
 	@SuppressLint("NewApi")
 	@Override
 	public synchronized void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		setBehindContentView(R.layout.activity_menu);
 		getActionBar().setHomeButtonEnabled(true);
 		
 		
 		if (!initializedUser && tryInitialize){
 			this.initializeUserInfo();
 		}
 		
 		
 		// Get info from transfer class
 		slideMenuInfo = SlideMenuInfo.getInstance();
 		Difficulty diff = slideMenuInfo.getDiff();
 		Category cat = slideMenuInfo.getCat();
 		
 		
 		
 		buildSlideMenu();
 		
 		if(diff == null){
 			setSpinnerToSelectedValue("Difficulty", "");
 		}else{
			setSpinnerToSelectedValue("Difficulty", diff.toString().toUpperCase());
 		}
 		
 		if(cat == null){
 			setSpinnerToSelectedValue("Category", "");
 		}else{
			setSpinnerToSelectedValue("Category", cat.toString().toUpperCase());
 		}
 
 		hideMainView();
 		showLoadingView1();
 		loadQuestions();
 	}
 	
 	/**
 	 * Function that will make set the currently selected spinner
 	 * value to the passed in string. Used when the difficulty
 	 * menu is changed from a SolutionActivity or PostSolutionActivity.
 	 * 
 	 * @param value Selected Spinner value
 	 */
 	public void setSpinnerToSelectedValue(String type, String value){
 		Spinner spinner = null;
 		
 		if(type.equals("Difficulty")){
 			spinner = (Spinner) findViewById(R.id.diff_spinner);
 		} else{
 			spinner = (Spinner) findViewById(R.id.category_spinner);
 		}
 		
 		Adapter a = spinner.getAdapter();
 		for (int i = 0; i < a.getCount(); i++){
 			if (a.getItem(i).toString().toUpperCase().equals(value)){
 				spinner.setSelection(i);
 				return;
 			}
 		}
 	}
 
 	/**
 	 * Method that returns the Difficulty Enum that is 
 	 * currently selected in the Difficulty spinner input
 	 * on the slide menu.
 	 * 
 	 * @return Difficulty Enum
 	 */
 	public Difficulty getCurrentDifficultySetting(){
 		Spinner spinner = (Spinner) findViewById(R.id.diff_spinner);
 
 		String diff = spinner.getSelectedItem().toString();
 		if (diff == null || diff.isEmpty() || diff.equals(UIConstants.ALL)) {
 			return null;
 		}
 		return Difficulty.valueOf(diff.toUpperCase());
 	}
 	
 	/**
 	 * Method that returns the Category Enum that is
 	 * currently selected in the Category spinner input
 	 * on the slide menu
 	 * 
 	 * @ return Category Enum
 	 */
 	public Category getCurrentCategorySetting(){
 		Spinner spinner = (Spinner) findViewById(R.id.category_spinner);
 		String category = spinner.getSelectedItem().toString();
 		if(category.equals(UIConstants.ALL)){
 			return null;
 		}
 		
 		category = category.replaceAll("\\s", "");
 		return Category.valueOf(category.toUpperCase());
 	}
 	
 	/**
 	 * Helper method that builds the slide menu on the current activity.
 	 */
 	@Override
 	public void buildSlideMenu(){
 		SlidingMenu menu = getSlidingMenu();
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		int width = (int) ((double) metrics.widthPixels);
 		menu.setBehindOffset((int)
 				(width * SlideMenuInfo.SLIDE_MENU_WIDTH));
 		
 		Spinner diffSpinner = (Spinner) findViewById(R.id.diff_spinner);
 		ArrayAdapter<CharSequence> adapter = 
 				ArrayAdapter.createFromResource(this,
 				R.array.difficulty, 
 				android.R.layout.simple_spinner_item);
 		
 		// Specify the layout to use when the list of choices appears
 		adapter.setDropDownViewResource(
 				android.R.layout.simple_spinner_dropdown_item);
 		
 		// Apply the adapter to the spinner
 		diffSpinner.setAdapter(adapter);
 		
 		Spinner categorySpinner = 
 			(Spinner) findViewById(R.id.category_spinner);
 		ArrayAdapter<CharSequence> catAdapter = 
 				ArrayAdapter.createFromResource(this,
 				R.array.category, 
 				android.R.layout.simple_spinner_item);
 		
 		catAdapter.setDropDownViewResource(
 				android.R.layout.simple_spinner_dropdown_item);
 		
 		categorySpinner.setAdapter(catAdapter);
 	}
 	
 	/**
 	 * Click handler for the slide-in menu difficulty selection.
 	 * Will repopulate the list of questions with new questions
 	 * that have the selected difficulty.
 	 * 
 	 * @param v Button View
 	 */
 	public void adjustSettings(View v){
 		toggle();
 		
 		// Clear current Questions
 		ViewGroup questionView =
 				(ViewGroup) findViewById(R.id.question_layout);
 		questionView.removeAllViews();
 		questionOffset = 0;
 		
 		hideMainView();
 		showLoadingView1();
 		loadQuestions();
 	}
 	
 	/**
 	 * Shows loading text
 	 */
 	public void showLoadingView1(){
 		View loadingText = findViewById(R.id.layout_loading);
 		loadingText.setVisibility(View.VISIBLE);
 	}
 	
 	/**
 	 * Hides loading text
 	 */
 	public void hideLoadingView1(){
 		View loadingText = findViewById(R.id.layout_loading);
 		loadingText.setVisibility(View.GONE);
 	}
 	
 	/**
 	 * Shows loading text
 	 */
 	public void showLoadingView2(){
 		View loadingText = findViewById(R.id.layout_loading_more);
 		loadingText.setVisibility(View.VISIBLE);
 	}
 	
 	/**
 	 * Hides loading text
 	 */
 	public void hideLoadingView2(){
 		View loadingText = findViewById(R.id.layout_loading_more);
 		loadingText.setVisibility(View.GONE);
 	}
 
 	/**
 	 * Shows main question list and buttons
 	 */
 	public void showMainView(){
 		View mainView = findViewById(R.id.main_view);
 		mainView.setVisibility(View.VISIBLE);
 	}
 	
 	/**
 	 * Hides main question list and buttons
 	 */
 	public void hideMainView(){
 		View mainView = findViewById(R.id.main_view);
 		mainView.setVisibility(View.GONE);
 	}
 	
 	public void loadQuestions(){
 		// Populate questions list. This makes a network call.
 		new FetchQuestionsTask(this,
 				getCurrentCategorySetting(),
 				getCurrentDifficultySetting(),
 				UIConstants.DEFAULT_QUESTIONS_TO_LOAD,
 				questionOffset).execute();
 	}
 	
 	/**
 	 * This is called when the "Show me more" button is pressed. 
 	 * 
 	 * @param v button being pressed
 	 */
 	public void loadMoreQuestions(View v){
 		showLoadingView2();
 		loadQuestions();
 	}
 	
 	/**
 	 * Displays a formatted list of questions
 	 * 
 	 * @param questions
 	 */
 	public void appendQuestionsToView(List<Question> questionList) {
 		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.75f);
 		
 		//TODO: Move to XML or constants file - haven't yet figured out how
 		llp.setMargins(40, 10, 40, 10);
 		llp.gravity = 1;  // Horizontal Center
 		
 		ViewGroup questionView =
 				(ViewGroup) findViewById(R.id.question_layout);
 		
 		if(questionList == null || questionList.size() <= 0){
 			// No new questions
 			if(questionView.getChildCount() == 0){
 				// No existing questions
 				TextView t = new TextView(this);
 	
 				t.setText("There doesn't seem to be any questions.");
 				// special look?
 				t.setLayoutParams(llp);
 				questionView.addView(t);
 			}
 		}else{
 			// Increase the question offset so that next time we access the db, we
 			// get the next set of questions
 			questionOffset += questionList.size();
 			
 			for(int i = 0; i < questionList.size(); i++){
 				Question question = questionList.get(i);
 				if(question != null && question.getText() != null){
 					
 					//build text
 					String questionTitle = question.getTitle();
 					String questionBody = question.getText();
 					String questionDiff = question.getDifficulty().toString();
 					String questionCat = question.getCategory().toString();
 					String questionDate = question.getDateCreated().toString();
 					
 					// abbreviate
 					if (questionBody.length() > 
 							UIConstants.TEXT_PREVIEW_LENGTH){
 						questionBody = questionBody.substring(
 								0, UIConstants.TEXT_PREVIEW_LENGTH);
 						questionBody += "...";
 					}
 					int pos = 0;
 					SpannableStringBuilder sb = new SpannableStringBuilder();
 					// title
 					sb.append(questionTitle);
 					sb.setSpan(new  TextAppearanceSpan(this, 
 							R.style.question_title_appearance), pos, 
 							sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 					pos += questionTitle.length();
 					
 					// descriptors
 					sb.append('\n');
 					sb.append(questionCat);
 					sb.append("\t\t\t");
 					sb.append(questionDiff);
 					sb.setSpan(new  TextAppearanceSpan(
 							this, R.style.question_descriptors_appearance),
 							pos, sb.length(), 
 							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 					sb.append("\n\n");
 					pos += questionDiff.length() + questionCat.length() + 5;
 					
 					// body
 					sb.append(questionBody);
 					sb.setSpan(new  TextAppearanceSpan(
 							this, R.style.question_body_appearance), pos, 
 							sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 					sb.append('\n');
 					pos += questionBody.length() + 1;
 					// date
 					sb.append('\n');
 					sb.append(questionDate);
 					sb.setSpan(new  TextAppearanceSpan(
 							this, R.style.question_date_appearance), pos, 
 							sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 					
 					// done
 					TextView t = new TextView(this);
 					t.setLayoutParams(llp);
 					t.setId(question.getQuestionId());
 					t.setTag(question);
 					t.setText(sb);	
 					// to make it work on older versions use this instead of
 					// setBackground
 					t.setBackground(getResources().
 							getDrawable(R.drawable.listitem));
 
 					t.setOnClickListener(new View.OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							openQuestion(v);
 						}
 					});
 					questionView.addView(t);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Pops up a dialog menu with "Retry" and "Cancel" options when a network
 	 * operation fails.
 	 * 
 	 * EDIT: looks the same as all other dialog boxes now.
 	 * It's more cumbersome to make but consistency is important.
 	 * 
 	 */
 	public void onNetworkError(){		
 		final Dialog dialog = new Dialog(this);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		dialog.setContentView(R.layout.retrydialogcustom);
 		// set the custom dialog components - text, buttons
 		TextView text = (TextView) dialog.findViewById(R.id.dialog_text);
 		text.setText(getString(R.string.retryDialog_title));
 		Button dialogButton = (Button) 
 				dialog.findViewById(R.id.button_retry);
 		// if button is clicked, send the solution
 		dialogButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Toast.makeText(getApplicationContext(), 
 						R.string.toast_retry, Toast.LENGTH_LONG).show();
 				loadQuestions();
 				dialog.dismiss();
 			}
 		});
 		dialogButton = (Button) dialog.findViewById(R.id.button_cancel);
 		// if button is clicked, close the custom dialog
 		dialogButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Toast.makeText(getApplicationContext(), 
 						R.string.toast_return, Toast.LENGTH_LONG).show();
 				dialog.dismiss();
 			}
 		});
 		dialog.show();
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/**
 	 * Function used as the onClickHandler of the Question tiles
 	 * on the main menu of the application.
 	 * 
 	 * @param view The TextView that holds the selected question.
 	 */
 	public void openQuestion(View view){
 		Intent intent = new Intent(this, QuestionActivity.class);
 		intent.putExtra(EXTRA_MESSAGE, (Question) view.getTag());
 		startActivity(intent);
 	}
 
 }
 
