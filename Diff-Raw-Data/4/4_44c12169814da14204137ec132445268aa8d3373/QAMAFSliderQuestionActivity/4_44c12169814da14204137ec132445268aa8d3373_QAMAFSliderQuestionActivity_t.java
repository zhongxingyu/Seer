 package org.t2health.mtbi.activity;
 
 
 import android.content.Context;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.accessibility.AccessibilityEvent;
 import android.view.accessibility.AccessibilityManager;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 
 import org.t2health.lib.qa.Answer;
 import org.t2health.lib.qa.BaseQAQuestionActivity;
 import org.t2health.mtbi.R;
 
 public class QAMAFSliderQuestionActivity extends BaseQAQuestionActivity implements OnTouchListener, OnClickListener, OnCheckedChangeListener, OnSeekBarChangeListener {
 	private View nextButton;
 	private Answer dontCountAnswer;
 	private Answer selectedAnswer;
 	private SeekBar seekBar;
 	private EditText valueSelectedEditText;
 	private View sliderWrapper;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.setContentView(R.layout.qa_maf_slider_question_activity);
 
 		sliderWrapper = this.findViewById(R.id.sliderWrapper);
 
 		CheckBox dontCountAnswerCheckBox = (CheckBox)this.findViewById(R.id.dontCountAnswer);
 		dontCountAnswerCheckBox.setOnCheckedChangeListener(this);
 		if(this.answers[0].value == 0) {
 			dontCountAnswer = this.answers[0];
 			dontCountAnswerCheckBox.setText(dontCountAnswer.title);
 
 			Answer[] newAnswers = new Answer[this.answers.length-1];
 			for(int i = 1; i < this.answers.length; ++i) {
 				newAnswers[i-1] = this.answers[i];
 			}
 			this.answers = newAnswers;
 		} else {
 			dontCountAnswerCheckBox.setVisibility(View.GONE);
 		}
 
 		((TextView)this.findViewById(R.id.text1)).setText(this.question.title);
 		((TextView)this.findViewById(R.id.text2)).setText(this.question.desc);
 
 		valueSelectedEditText = (EditText)this.findViewById(R.id.valueSelectedEditText);
 
 		nextButton = this.findViewById(R.id.nextButton);
 		nextButton.setOnClickListener(this);
 
 		String desc = getString(R.string.qa_maf_slider_desc);
 		desc = desc.replace("{0}", answers[0].value+"");
 		desc = desc.replace("{0}", answers[answers.length-1].value+"");
 
 		seekBar = (SeekBar)this.findViewById(R.id.seekBar);
 		seekBar.setMax(this.answers.length-1);
 		seekBar.setOnSeekBarChangeListener(this);
 		seekBar.setContentDescription(desc);
 
 		LinearLayout answerValueLabels = (LinearLayout)this.findViewById(R.id.answerValueLabels);
 		LinearLayout answerLabels = (LinearLayout)this.findViewById(R.id.answerLabels);
 		for(int i = 0; i < this.answers.length; ++i) {
 			Answer ans = this.answers[i];
 
 			TextView tv = new TextView(this);
 			tv.setText(ans.value+"");
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new LinearLayout.LayoutParams(
 					LinearLayout.LayoutParams.WRAP_CONTENT,
 					LinearLayout.LayoutParams.WRAP_CONTENT,
 					1.0f
 			));
 			answerValueLabels.addView(tv);
 
 			if(ans.desc != null && ans.desc.length() > 0) {
 				TextView tv2 = new TextView(this);
 				tv2.setText(ans.desc);
 				if(i == 0) {
 					tv2.setGravity(Gravity.LEFT);
 				} else if(i == this.answers.length - 1) {
 					tv2.setGravity(Gravity.RIGHT);
 				} else {
 					tv2.setGravity(Gravity.CENTER);
 				}
 				tv2.setLayoutParams(new LinearLayout.LayoutParams(
 						LinearLayout.LayoutParams.WRAP_CONTENT,
 						LinearLayout.LayoutParams.WRAP_CONTENT,
 						1.0f
 				));
 				answerLabels.addView(tv2);
 			}
 		}
 	}
 
 	@Override
 	public boolean onTouch(View arg0, MotionEvent arg1) {
 		switch(arg0.getId()) {
 		case R.id.seekBar:
 			nextButton.setEnabled(true);
 			break;
 		}
 		return false;
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()) {
 		case R.id.nextButton:
 			this.finish(question, this.selectedAnswer);
 			break;
 		}
 	}
 
 	@Override
 	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
 		switch(arg0.getId()) {
 		case R.id.dontCountAnswer:
 			nextButton.setEnabled(true);
 			this.selectedAnswer = dontCountAnswer;
 			setSliderWrapperVisible(!arg1);
 		}
 	}
 
 	private void setSliderWrapperVisible(boolean b) {
 		if(b) {
 			sliderWrapper.setVisibility(View.VISIBLE);
 		} else {
 			sliderWrapper.setVisibility(View.GONE);
 		}
 	}
 
 	@Override
 	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
 		switch(arg0.getId()) {
 		case R.id.seekBar:
 			//Log.v(TAG, "prog:"+arg1);
 			nextButton.setEnabled(true);
 			this.selectedAnswer = this.answers[arg1];
 			valueSelectedEditText.setText(this.selectedAnswer.value+"");
 
 			AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_FOCUSED);
 			event.setPackageName(arg0.getClass().getPackage().toString());
 			event.setClassName(arg0.getClass().getSimpleName());
 			event.setContentDescription(this.selectedAnswer.value+"");
 			event.setEventTime(System.currentTimeMillis());
 
 			AccessibilityManager aManager = (AccessibilityManager)this.getSystemService(Context.ACCESSIBILITY_SERVICE);
			if(aManager.isEnabled()) {
				aManager.sendAccessibilityEvent(event);
			}
 		}
 	}
 
 	@Override
 	public void onStartTrackingTouch(SeekBar arg0) {
 
 	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar arg0) {
 
 	}
 
 }
