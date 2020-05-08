 package com.quizz.places.fragments;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AccelerateInterpolator;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 
 import com.actionbarsherlock.internal.nineoldandroids.animation.AnimatorSet;
 import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
 import com.quizz.core.dialogs.ConfirmQuitDialog;
 import com.quizz.core.dialogs.ConfirmQuitDialog.Closeable;
 import com.quizz.core.fragments.BaseMenuFragment;
 import com.quizz.core.listeners.VisibilityAnimatorListener;
 import com.quizz.core.managers.DataManager;
 import com.quizz.core.models.Section;
 import com.quizz.core.utils.AnimatorUtils;
 import com.quizz.places.R;
 import com.quizz.places.activities.QuizzActivity;
 import com.quizz.places.db.GameDataLoading.GameDataLoadingListener;
 
 public class MenuFragment extends BaseMenuFragment implements Closeable, GameDataLoadingListener {
 
 	private Button mButtonPlay;
 	//private Button mButtonRateThisApp;
 	private Button mButtonStats;
 	private Button mButtonSettings;
 	private LinearLayout mMenuButtonsContainer;
 
 	private ImageView mTitleSign;
 	private ImageView mFooter;
 
 	private ProgressBar mDataLoadingProgressBar;
 	private AnimatorSet mHideUiAnimatorSet;
 	private ConfirmQuitDialog mConfirmQuitDialog;
 	
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		if (activity instanceof QuizzActivity) {
 			((QuizzActivity) activity).setGameDataLoadingListener(this);
 		}
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 
 		View view = inflater.inflate(R.layout.fragment_menu, container, false);
 
 		mButtonPlay = (Button) view.findViewById(R.id.buttonPlay);
 		//mButtonRateThisApp = (Button) view.findViewById(R.id.buttonRateThisApp);
 		mButtonStats = (Button) view.findViewById(R.id.buttonStats);
 		mButtonSettings = (Button) view.findViewById(R.id.buttonSettings);
 		mTitleSign = (ImageView) view.findViewById(R.id.titleSign);
 		mFooter = (ImageView) view.findViewById(R.id.footer);
 		mMenuButtonsContainer = (LinearLayout) view.findViewById(R.id.menuButtonsContainer);
 		mDataLoadingProgressBar = (ProgressBar) view.findViewById(R.id.dataLoadingProgress);
 		
 		mHideUiAnimatorSet = createHideUiAnimation();
 		FragmentTransaction fadeTransaction = getActivity()
 				.getSupportFragmentManager().beginTransaction();
 		fadeTransaction.setCustomAnimations(R.anim.none, R.anim.none,
 				R.anim.none, R.anim.fade_out);
 
 		initMenuButton(mButtonPlay, ListSectionsFragment.class,
 				fadeTransaction, mHideUiAnimatorSet);
 		initMenuButton(mButtonStats, StatsFragment.class, fadeTransaction,
 				mHideUiAnimatorSet);
 		initMenuButton(mButtonSettings, SettingsFragment.class, fadeTransaction,
 				mHideUiAnimatorSet);
 
 		showUi();
 		
 		return view;
 	}
 
 	@Override
 	public void onGameLoadingStart() {
 		
 	}
 	
 	@Override
 	public void onGameLoadingProgress(int progress) {
 		mDataLoadingProgressBar.setProgress(progress);
 	}
 
 	@Override
 	public void onGameLoadingSuccess(List<Section> sections) {
		if (mDataLoadingProgressBar != null) {
			mDataLoadingProgressBar.setVisibility(View.GONE);
		}
 		displayButtons();
 	}
 
 	@Override
 	public void onGameLoadingFailure(Exception e) {
 		// TODO FIXME: Find a solution to let the player play anyway
 	}
 	
 	@Override
 	public void onDestroy() {
 		if (mConfirmQuitDialog != null) {
 			mConfirmQuitDialog.dismiss();
 		}
 		super.onDestroy();
 	}
 	
 	@Override
 	public void close() {
 		getActivity().finish();
 	}
 	
 	private void displayButtons() {
 		ObjectAnimator buttonsDisplay = ObjectAnimator.ofFloat(
 				mMenuButtonsContainer, "alpha", 0f, 1f);
 		buttonsDisplay.setDuration(500);
 		buttonsDisplay.setStartDelay(700);
 		buttonsDisplay.addListener(new VisibilityAnimatorListener(
 				mMenuButtonsContainer));
 		buttonsDisplay.start();
 	}
 	
 	private void showUi() {
 		float[] signMovementValues = new float[] { -200, 0 };
 		ObjectAnimator signPopup = ObjectAnimator.ofFloat(mTitleSign,
 				"translationY", signMovementValues);
 		signPopup.setDuration(300);
 		signPopup.setStartDelay(700);
 		signPopup.setInterpolator(new AccelerateInterpolator());
 		signPopup.addListener(new VisibilityAnimatorListener(mTitleSign));
 
 		float[] footerMovementValues = new float[] { 500, 0 };
 		ObjectAnimator footerPopup = ObjectAnimator.ofFloat(mFooter,
 				"translationY", footerMovementValues);
 		footerPopup.setDuration(700);
 		footerPopup.setInterpolator(new AccelerateInterpolator());
 		footerPopup.addListener(new VisibilityAnimatorListener(mFooter));
 
 		AnimatorUtils.bounceAnimator(signPopup, signMovementValues, 5, 100);
 		AnimatorUtils.bounceAnimator(footerPopup, footerMovementValues, 5, 100);
 		
 		if (DataManager.dataLoaded) {
 			mDataLoadingProgressBar.setVisibility(View.GONE);
 			displayButtons();
 		}
 	}
 
 	private AnimatorSet createHideUiAnimation() {
 		ObjectAnimator signHiding = ObjectAnimator.ofFloat(mTitleSign,
 				"translationY", 0, -200);
 		signHiding.setDuration(300);
 
 		ObjectAnimator footerHiding = ObjectAnimator.ofFloat(mFooter,
 				"translationY", 0, 500);
 		footerHiding.setDuration(700);
 
 		ObjectAnimator buttonsHiding = ObjectAnimator.ofFloat(
 				mMenuButtonsContainer, "alpha", 1f, 0f);
 		buttonsHiding.setDuration(500);
 
 		AnimatorSet uiHidingAnimation = new AnimatorSet();
 		uiHidingAnimation.playTogether(signHiding, footerHiding, buttonsHiding);
 		return uiHidingAnimation;
 	}
 }
