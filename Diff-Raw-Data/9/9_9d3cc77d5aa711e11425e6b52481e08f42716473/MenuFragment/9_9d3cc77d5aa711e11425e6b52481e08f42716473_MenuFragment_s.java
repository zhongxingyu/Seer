 package com.quizz.places.fragments;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.AccelerateInterpolator;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 import com.actionbarsherlock.internal.nineoldandroids.animation.AnimatorSet;
 import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
 import com.quizz.core.fragments.BaseMenuFragment;
 import com.quizz.core.listeners.VisibilityAnimatorListener;
 import com.quizz.core.utils.AnimatorUtils;
 import com.quizz.places.R;
 import com.quizz.places.widgets.MenuBackground;
 
 public class MenuFragment extends BaseMenuFragment {
 	
 	private Button mButtonPlay;
 	private Button mButtonFreeHints;
 	private Button mButtonStats;
 	private Button mButtonSettings;
 	private LinearLayout mMenuButtonsContainer;
 	
 	private ImageView mTitleSign;
 	private ImageView mFooter;
 	private MenuBackground mHaloBackground;
 	
 	private AnimatorSet mHideUiAnimatorSet;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		
 		View view = inflater.inflate(R.layout.fragment_menu, container, false);
 
 		mButtonPlay = (Button) view.findViewById(R.id.buttonPlay);
 		mButtonFreeHints = (Button) view.findViewById(R.id.buttonFreeHints);
 		mButtonStats = (Button) view.findViewById(R.id.buttonStats);
 		mButtonSettings = (Button) view.findViewById(R.id.buttonSettings);
 		mTitleSign = (ImageView) view.findViewById(R.id.titleSign);
 		mFooter = (ImageView) view.findViewById(R.id.footer);
 		mMenuButtonsContainer = (LinearLayout) view.findViewById(R.id.menuButtonsContainer);
 		//mHaloBackground = (MenuBackground) view.findViewById(R.id.haloBackground);
 		
 		mHideUiAnimatorSet = createHideUiAnimation();
 		initMenuButton(mButtonPlay, ListSectionsFragment.class, mHideUiAnimatorSet);
 		initMenuButton(mButtonStats, GridLevelsFragment.class, mHideUiAnimatorSet);
 		mButtonSettings.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 			}
 		});
 		return view;
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		showUi();
 	}
 	
 	private void showUi() {
 		float[] signMovementValues = new float[] { -200, 0 };
 		ObjectAnimator signPopup = ObjectAnimator.ofFloat(mTitleSign, "translationY", signMovementValues);
 		signPopup.setDuration(300);
 		signPopup.setStartDelay(700);
 		signPopup.setInterpolator(new AccelerateInterpolator());
 		signPopup.addListener(new VisibilityAnimatorListener(mTitleSign));
 		
 		float[] footerMovementValues = new float[] { 500, 0 };
 		ObjectAnimator footerPopup = ObjectAnimator.ofFloat(mFooter, "translationY", footerMovementValues);
 		footerPopup.setDuration(700);
 		footerPopup.setInterpolator(new AccelerateInterpolator());
 		footerPopup.addListener(new VisibilityAnimatorListener(mFooter));
 		
 		AnimatorUtils.bounceAnimator(signPopup, signMovementValues, 5, 100);
 		AnimatorUtils.bounceAnimator(footerPopup, footerMovementValues, 5, 100);
 		
 		ObjectAnimator buttonsDisplay = ObjectAnimator.ofFloat(mMenuButtonsContainer, "alpha", 0f, 1f);
 		buttonsDisplay.setDuration(500);
 		buttonsDisplay.setStartDelay(700);
 		buttonsDisplay.addListener(new VisibilityAnimatorListener(mMenuButtonsContainer));
 		buttonsDisplay.start();
 		
 		/*
 		ValueAnimator animator = ValueAnimator.ofFloat(0, 360);
 		animator.addUpdateListener(new AnimatorUpdateListener() {
 			@Override
 			public void onAnimationUpdate(ValueAnimator arg0) {
 
 				mHaloBackground.rotationDegrees = (Float) arg0.getAnimatedValue();
 				mHaloBackground.invalidate();
 			}
 		});
 		animator.setInterpolator(new LinearInterpolator());
 		animator.setRepeatCount(ValueAnimator.INFINITE);
 		animator.setDuration(8000);
 		animator.start();*/
 	}
 	
 	private AnimatorSet createHideUiAnimation() {
 		ObjectAnimator signHiding = ObjectAnimator.ofFloat(mTitleSign, "translationY", 0, -200);
 		signHiding.setDuration(300);
 		signHiding.start();
 		
 		ObjectAnimator footerHiding = ObjectAnimator.ofFloat(mFooter, "translationY", 0, 500);
 		footerHiding.setDuration(700);
 		footerHiding.start();
 		
 		ObjectAnimator buttonsHiding = ObjectAnimator.ofFloat(mMenuButtonsContainer, "alpha", 1f, 0f);
 		buttonsHiding.setDuration(500);
 		buttonsHiding.start();
 		
 		AnimatorSet uiHidingAnimation = new AnimatorSet();
 		uiHidingAnimation.playTogether(signHiding, footerHiding, buttonsHiding);
 		return uiHidingAnimation;
 		
 		/*
 		ValueAnimator animator = ValueAnimator.ofFloat(0, 360);
 		animator.addUpdateListener(new AnimatorUpdateListener() {
 			@Override
 			public void onAnimationUpdate(ValueAnimator arg0) {
 
 				mHaloBackground.rotationDegrees = (Float) arg0.getAnimatedValue();
 				mHaloBackground.invalidate();
 			}
 		});
 		animator.setInterpolator(new LinearInterpolator());
 		animator.setRepeatCount(ValueAnimator.INFINITE);
 		animator.setDuration(8000);
 		animator.start();*/
 	}
 }
