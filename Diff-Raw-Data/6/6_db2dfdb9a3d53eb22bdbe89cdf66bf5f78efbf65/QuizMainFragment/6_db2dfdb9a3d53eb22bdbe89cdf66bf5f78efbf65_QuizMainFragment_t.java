 package com.quizmania.fragments;
 
 import android.content.Context;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.MenuItemCompat;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 import com.quizmania.activities.ItemStore;
 import com.quizmania.entities.NameHints;
 import com.quizmania.entities.QuizElement;
 import com.quizmania.fruits.R;
 import com.quizmania.utils.AnswerService;
 import com.quizmania.utils.HintStyle;
 import com.quizmania.utils.QuizElementUtil;
 import com.quizmania.utils.StaticGlobalVariables;
 import com.quizmania.utils.UserConfig;
 import com.quizmania.utils.ViewUtils;
 
 public class QuizMainFragment extends Fragment implements OnKeyListener, OnClickListener, OnTouchListener {
 	
 	QuizElement element;
     private  OnClickListener showKeyboardEventListener;
 
     @Override
 	public String toString() {
 		return "QuizMainFragment [element=" + element + ", thisView="
 				+ thisView + ", hintButton=" + hintButton + "]";
 	}
 
 
 
 	View thisView;
 	MenuItem hintButton;
 	MenuItem shareButton;
 	
 	public static final int NUMBER_OF_HINTS_THAT_FIT_IN_SCREEN = 9;
 	public QuizElement getElement() {
 		return element;
 	}
 
 
 
 	public void setElement(QuizElement element) {
 		this.element = element;
 	}
 
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater){	    
 	    inflater.inflate(R.menu.action_bar_hint, menu);
 	    addClickListenersToActionBarItems(menu);	    	    
 		refreshHintButton();		
 	}
 
 
 	
 	private void addClickListenersToActionBarItems(Menu menu) {
 		hintButton = (MenuItem) menu.findItem(R.id.actionsBarHintButton);
 	    setClickListenerToHintButton(hintButton);
 	    
 	    shareButton = (MenuItem) menu.findItem(R.id.actionsBarShare);
 	    setClickListenerToShareButton(shareButton);
 		
 	}
 
 
 
 	private void setClickListenerToHintButton(MenuItem hintButton) {
 		ViewGroup actionView= (ViewGroup) MenuItemCompat.getActionView(hintButton);
 		actionView.setOnClickListener(new OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				hintButtonClick();
 			}
 		});
 		
 	}
 	
 	private void setClickListenerToShareButton(MenuItem shareButton) {
 		ViewGroup actionView= (ViewGroup) MenuItemCompat.getActionView(shareButton);
 		actionView.setOnClickListener(new OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				share();
 			}
 		});
 		
 	}
 
 
 	private void share() {
 		
 		Intent shareIntent = new Intent();
 		shareIntent.setAction(Intent.ACTION_SEND);		
 		int elementImageResId = QuizElementUtil.getResourceIdFromQuizElement(this, element);		
 		Uri screenshotUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + elementImageResId);
 		Log.d(StaticGlobalVariables.packageName,"****** uri To image: " + screenshotUri.toString());
 		Log.d(StaticGlobalVariables.packageName,"****** resourceId to Image: " + elementImageResId);
 		shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
 		shareIntent.setType("text/plain");
 		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.shareMessage));
 		shareIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
 		
 		startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.shareViaText)));
 		
 	}
 
 
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState){
         showKeyboardEventListener = new OnClickListener() {
             @Override
             public void onClick(View view) {
                 Log.d(StaticGlobalVariables.packageName,"Click en hint!");
                 showKeyboard();
             }
         };
 
 
 		constructViewFromQuizElement(inflater, container);				
 		setEventListeners(thisView);
 		drawAnswerIconIfAnswered();
 		setHasOptionsMenu(true);
         return thisView;
 
 	}
 	
 
 
 
 
 	public void refreshHintButton() {
 				
 		if(hintButton == null)
 			return;
 		
 		ViewGroup actionView= (ViewGroup) MenuItemCompat.getActionView(hintButton);
 		TextView hintNumberLabel = (TextView) actionView.findViewById(R.id.hintNumber);
 		Log.d(StaticGlobalVariables.packageName,"*****el textview" + hintNumberLabel);				
 		hintNumberLabel.setText("" + UserConfig.getInstance(getActivity()).getHintsLeft());
 		
 	}
 
 
 
 	private void drawAnswerIconIfAnswered() {
 		boolean isCorrectAnswered = AnswerService.getAnswerService().isAwnsered(element, UserConfig.getInstance(getActivity()).getLanguage());
 		if(isCorrectAnswered){
 			showCorrectAnsweredIcon();
 		}else{
 			hideAnswerIcon();
 		}
 		
 	}
 
 
 	@Override
 	public void onResume(){
 		super.onResume();
 		if(!AnswerService.getAnswerService().isAwnsered(element, UserConfig.getInstance(getActivity()).getLanguage())){
 			hideAnswerIcon();
 		}
 		refreshHintButton();
 	}
 
 
 	private void hideAnswerIcon() {
 		ImageView answerIcon = (ImageView) thisView.findViewById(R.id.answerIcon);
 		answerIcon.setVisibility(View.GONE);  
 
 		
 	}
 
 
 
 	private void setEventListeners(View quizElementView) {
 		addAnswerTextboxEvents();
 		addAnswerButtonEvents();		
 		addElementImageEvents();
 
 		//addHintButtonEvents();
 		
 	}
 
 	public void hintButtonClick() {
 		
 		if(UserConfig.getInstance(getActivity()).getHintsLeft() > 0){
 			
 			AnswerService.getAnswerService().revealRandomLetter(element, getActivity());
 			if(AnswerService.getAnswerService().areAllLettersRevealed(element)){
 				AnswerService.getAnswerService().markAsAnswered(element,getActivity());						
				triggerCorrectAnswerEvents(false);
				boolean isFirstTimeCompletingLevel = AnswerService.getAnswerService().isLevelComplete(StaticGlobalVariables.currentLevel) ? false : true;
				if(AnswerService.getAnswerService().isLevelComplete(StaticGlobalVariables.currentLevel) && isFirstTimeCompletingLevel){
					ViewUtils.showAlertMessage(getActivity(), getActivity().getResources().getString(R.string.levelCompleteMessage), null);
				}
 			}
 			AnswerService.getAnswerService().saveToSDCard(getActivity());
 			drawHintLetters();	
 			refreshHintButton();
 		}else{
 						
 			Intent intent = new Intent(getActivity(),ItemStore.class);
 			getActivity().startActivity(intent);
 		}
 		
 	}
 	
 
 
 	
 
 	private void addElementImageEvents() {
 		ImageView imageView = getElementImageView();
 		imageView.setOnTouchListener(this);
 	}
 
 
 	private void addAnswerButtonEvents() {
 		Button answerButton =  (Button) thisView.findViewById(R.id.answerButton);
 		answerButton.setOnClickListener(this);
 	}
 	
 	private void addAnswerTextboxEvents() {
 		TextView answerTextbox = (TextView) thisView.findViewById(R.id.answerTextbox);
 		answerTextbox.setOnKeyListener(this);
 	}
 
 
 
 	private ImageView getElementImageView() {
 		ImageView imageView =  (ImageView) thisView.findViewById(R.id.elementImage);
 		return imageView;
 	}
 
 
 	private View constructViewFromQuizElement(LayoutInflater inflater,
 			ViewGroup container) {
 		thisView = inflater.inflate(R.layout.fragment_quiz_main, container, false);		
 		drawElementImage(thisView);
 		
 		drawHintLetters();
 	
 		
 		return thisView;
 	}
 
 
 
 	private void drawHintLetters() {
 				
 		
 		char[] elementNameLetters = element.getLanguageToNamesMap().get(UserConfig.getInstance(getActivity()).getLanguage()).getNames().get(0).toCharArray();		 		
 		int screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
 		int hintLetterWidth = screenWidth/NUMBER_OF_HINTS_THAT_FIT_IN_SCREEN;
 		int hintLetterHeight = hintLetterWidth;		
 		HintStyle hintStlye = new HintStyle(getActivity(), hintLetterWidth,hintLetterHeight);		
 		LinearLayout hintHolder = (LinearLayout) thisView.findViewById(R.id.centralizedHintHolder);
 		NameHints elementsRevealedHints = AnswerService.getAnswerService().getRevealedHintsForQuizElement(element);		
 		drawLineOfHints(hintHolder,elementNameLetters,elementsRevealedHints,0,hintStlye);
 		if(elementNameLetters.length  > NUMBER_OF_HINTS_THAT_FIT_IN_SCREEN){
 			hintHolder = (LinearLayout) thisView.findViewById(R.id.secondLineHintHolder);
 			drawLineOfHints(hintHolder,elementNameLetters,elementsRevealedHints,NUMBER_OF_HINTS_THAT_FIT_IN_SCREEN,hintStlye);
 		}
 			
 		
 		//addLettersInOtherLines(elementNameLetters,numberOfLettersThatFitInTheFirstLine,hintStlye);
 		
 		
 	}
 
 
 
 	private void drawLineOfHints(LinearLayout hintContainer, char[] elementNameLetters,NameHints elementsRevealedHints, int offset, HintStyle hintStlye) {
 		hintContainer.removeAllViews();
 		LayoutParams layoutParams = new LayoutParams(hintStlye.getWidth(),hintStlye.getHeight());
 		
 		for(int i=0 + offset; i < elementNameLetters.length; i++){
 			if(i >= offset + NUMBER_OF_HINTS_THAT_FIT_IN_SCREEN)
 				break;
 			char letter = elementNameLetters[i];			
 			TextView hintLetter = ViewUtils.createHintLetter(getActivity());
 			hintLetter.setOnClickListener(this.showKeyboardEventListener);
 			hintLetter.setLayoutParams(layoutParams);
 			if(elementsRevealedHints != null && elementsRevealedHints.hasLetterRevealed(i)){
 				String letterAsString = "" + letter;								
 				hintLetter.setText(letterAsString.toUpperCase());
 			}
 			if(letter == StaticGlobalVariables.BLANK_SPACE){
 				hintLetter.setBackgroundResource(0);
 			}
 			
 			hintContainer.addView(hintLetter);
 		}
 	}
 
 
 
 	/*private void addLettersInOtherLines(char[] elementNameLetters,HintStyle hintStlye) {
 		LinearLayout multipleLineHintHolder = (LinearLayout) thisView.findViewById(R.id.multipleLineHintHolder);
 		HintLetterAdapter adapter = new HintLetterAdapter(getActivity(),elementNameLetters,numberOfLettersThatFitInTheFirstLine,hintStlye);
 		multipleLineHintHolder.setAdapter(adapter);
 		
 		
 	}*/
 
 	@Override
 	public void onDestroyView(){
 		super.onDestroyView();
 		ImageView elementImage = (ImageView) thisView.findViewById(R.id.elementImage);
 		
 		elementImage.setImageBitmap(null);
 	}
 
 	private void drawElementImage(View quizElementView) {
 		
 		int resID = QuizElementUtil.getResourceIdFromQuizElement(this, element);
 		ImageView quizElementImage =  getElementImageView();
 		quizElementImage.setImageResource(resID);
 	}
 
 
 
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		if(keyCode != KeyEvent.KEYCODE_ENTER)
 			return false;
 				
 		tryToAnswer();
 		return false;
 	}
 
 
 
 	public void tryToAnswer() {
 		TextView textView = (TextView) thisView.findViewById(R.id.answerTextbox);
 		boolean isFirstTimeCompletingLevel = AnswerService.getAnswerService().isLevelComplete(StaticGlobalVariables.currentLevel) ? false : true;
 		boolean wasAlreadyAnswered = AnswerService.getAnswerService().isAwnsered(element, UserConfig.getInstance(getActivity()).getLanguage());
 		boolean showHintsAddedToast = !wasAlreadyAnswered;
 		boolean correctAnswer = AnswerService.getAnswerService().tryToAnswer(element, textView.getText().toString());
 		int hintsToAdd = 0;
 		Log.d("******!","numberOfHints before: " + UserConfig.getInstance(getActivity()).getHintsLeft());
 		if(correctAnswer){
 			
 			AnswerService.getAnswerService().revealAllLetters(element);
 			drawHintLetters();
 			triggerCorrectAnswerEvents(showHintsAddedToast);
 			AnswerService.getAnswerService().saveToSDCard(getActivity());
 			if(AnswerService.getAnswerService().isLevelComplete(StaticGlobalVariables.currentLevel) && isFirstTimeCompletingLevel){
 				ViewUtils.showAlertMessage(getActivity(), getActivity().getResources().getString(R.string.levelCompleteMessage), null);
 			}
 			hintsToAdd = 1;
 			
 		}else{
 			hintsToAdd = -1;	
 			triggerIncorrectAnswerEvents(showHintsAddedToast);
 		}
 		if(!wasAlreadyAnswered)
 			addNumberOfHints(hintsToAdd);
 		
 		Log.d("******!","numberOfHints after: " + UserConfig.getInstance(getActivity()).getHintsLeft());
 		refreshHintButton();		
 	}
 
 
 
 	private void addNumberOfHints(int toAdd) {
 		UserConfig config = UserConfig.getInstance(getActivity());
 		int currentHintsAvailable =  config.getHintsLeft();
 		
 		currentHintsAvailable += currentHintsAvailable > 0 || toAdd > 0 ? toAdd : 0;
 		config.setHintsLeft(currentHintsAvailable);
 		config.saveToSDCard(getActivity());
 		
 	}
 
 
 
 	private void triggerIncorrectAnswerEvents(boolean showToast) {
 
 		showIncorrectImage();
 		playIncorrectSound();
 		if(showToast){
 			String minusOneHintMessage = getString(R.string.minusOneHintMessage);
 			ViewUtils.showToast(getActivity(),minusOneHintMessage,Gravity.CENTER);
 		}		
 		vibrate();
 		hideKeyboard();
 		ViewGroup parentActivityRootView = (ViewGroup) getActivity().findViewById(android.R.id.content);
 		
 		
 	}
 	
 
 
 
 	private void vibrate() {
 		if(UserConfig.getInstance(getActivity()).isVibrationActivated()){
 			Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);		 		
 			v.vibrate(500);
 		}
 		
 	}
 
 
 
 	private void playIncorrectSound() {
 		if(UserConfig.getInstance(getActivity()).isSoundActivated()){
 			MediaPlayer playerMedia = MediaPlayer.create(getActivity(), R.raw.fail);
 			playerMedia.start();
 		}
 	
 		
 	}
 
 
 
 	private void showIncorrectImage() {
 		ImageView answerIcon =  (ImageView) thisView.findViewById(R.id.answerIcon);
 		answerIcon.setImageResource(R.drawable.incorrect);
 		answerIcon.setVisibility(View.VISIBLE);
 		
 	}
 
 
 
 	private void triggerCorrectAnswerEvents(boolean showToast) {
 		showCorrectAnsweredIcon();
 		playCorrectSound();
 		if(showToast){
 			String plusOneHintMessage = getString(R.string.plusOneHintMessage);
 			ViewUtils.showToast(getActivity(),plusOneHintMessage,Gravity.CENTER);
 		}
 		hideKeyboard();
 		ViewGroup parentActivityRootView= (ViewGroup)getActivity().findViewById(android.R.id.content);
 		
 		
 	}
 
 
 
 	private void playCorrectSound() {
 		if(UserConfig.getInstance(getActivity()).isSoundActivated()){
 			MediaPlayer playerMedia = MediaPlayer.create(getActivity(), R.raw.correct);
 			playerMedia.start();	
 		}
 		
 		
 	}
 
 
 
 	private void showCorrectAnsweredIcon() {
 		
 		ImageView answerIcon = (ImageView) thisView.findViewById(R.id.answerIcon);
 		answerIcon.setImageResource(R.drawable.correct);
 		answerIcon.setVisibility(View.VISIBLE);
 		
 	}
 
 	
 
 	@Override
 	public void onClick(View button) {		
 		tryToAnswer();
 	}
 
 
 
 	@Override
 	public boolean onTouch(View arg0, MotionEvent arg1) {
 		return hideKeyboard();
 		
 	}
 
 
 
 	private boolean hideKeyboard() {
 		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
 			      Context.INPUT_METHOD_SERVICE);
 		EditText answerTextBox = (EditText) thisView.findViewById(R.id.answerTextbox);
 			imm.hideSoftInputFromWindow(answerTextBox.getWindowToken(), 0);
 		return true;
 	}
 
 
 
     private boolean showKeyboard() {
         InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                 Context.INPUT_METHOD_SERVICE);
         EditText answerTextBox = (EditText) thisView.findViewById(R.id.answerTextbox);
         imm.showSoftInput(answerTextBox,0);
         return true;
     }
 
 
 
 }
