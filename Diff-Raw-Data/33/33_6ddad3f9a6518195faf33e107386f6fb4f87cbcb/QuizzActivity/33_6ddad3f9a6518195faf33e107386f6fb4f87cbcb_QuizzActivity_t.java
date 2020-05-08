 package com.quizz.places.activities;
 
 import android.os.Bundle;
 import android.view.animation.LinearInterpolator;
 
 import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
 import com.quizz.core.activities.BaseQuizzActivity;
 import com.quizz.core.utils.NavigationUtils;
import com.quizz.core.widgets.QuizzActionBar;
 import com.quizz.places.R;
 import com.quizz.places.fragments.MenuFragment;
 
 public class QuizzActivity extends BaseQuizzActivity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
		if (savedInstanceState == null) {
			NavigationUtils.directNavigationTo(MenuFragment.class, getSupportFragmentManager(), 
					this, false);
		}
		
 		/*
 		View confirmQuitDialog = getLayoutInflater().inflate(R.layout.dialog_confirm_quit, null);
 		setConfirmQuitDialogView(confirmQuitDialog);
 		*/
 		
		getQuizzActionBar().hide(QuizzActionBar.MOVE_DIRECT);
		getQuizzActionBar().getBackButton().setImageResource(R.drawable.back_but_1);
		getQuizzActionBar().getMiddleText().setText("22 / 30");
		getQuizzActionBar().getRightText().setText("345 pts");
		
		initBackground();
	}
	
	private void initBackground() {
		getQuizzLayout().setBackgroundResource(R.drawable.sky_clean);
		getBackgroundAnimatedImage().setBackgroundResource(R.drawable.clouds);
 		
		ObjectAnimator bgAnimation = ObjectAnimator.ofFloat(getBackgroundAnimatedImage(), 
				"translationX", 500, -1700);
 		bgAnimation.setDuration(30000);
 		bgAnimation.setInterpolator(new LinearInterpolator());
 		bgAnimation.setRepeatCount(ObjectAnimator.INFINITE);
 		bgAnimation.start();
 	}
 }
