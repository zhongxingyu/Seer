 package ca.ilanguage.oprime.activity;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import ca.ilanguage.oprime.R;
 import ca.ilanguage.oprime.content.OPrime;
 import ca.ilanguage.oprime.content.Stimulus;
 import ca.ilanguage.oprime.content.SubExperimentBlock;
 import ca.ilanguage.oprime.content.Touch;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.drawable.Drawable;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.DisplayMetrics;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SubExperiment extends Activity {
 
 	protected ArrayList<? extends Stimulus> mStimuli;
 	protected SubExperimentBlock mSubExperiment;
 	MediaPlayer mAudioStimuli;
 	MediaPlayer mTouchAudio;
 	protected Locale language;
 	protected int mStimuliIndex = -1;
 	protected long mLastTouchTime = 0;
 	protected boolean mTakePicture = false;
 	protected String TAG = "OPrime SubExperiment";
 	protected int width = 1;
 	protected int height = 1;
 	
 	Animation animationSlideInRight;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		DisplayMetrics displaymetrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
 		height = displaymetrics.heightPixels;
 		width = displaymetrics.widthPixels;
 		animationSlideInRight = AnimationUtils.loadAnimation(this,
 				R.anim.slide_in_right);
 		animationSlideInRight.setDuration(1000);
 		animationSlideInRight
 				.setAnimationListener(animationSlideInRightListener);
 		/*
 		 * Prepare Stimuli
 		 */
 		mSubExperiment = (SubExperimentBlock) getIntent().getExtras()
 				.getSerializable(OPrime.EXTRA_SUB_EXPERIMENT);
 		this.setTitle(mSubExperiment.getTitle());
 		mStimuli = mSubExperiment.getStimuli();
 		mTakePicture = (boolean) getIntent().getExtras().getBoolean(
 				OPrime.EXTRA_TAKE_PICTURE_AT_END, false);
 		if (mStimuli == null || mStimuli.size() == 0) {
 			loadDefaults();
 		}
 		/*
 		 * Prepare touch audio
 		 */
 		mTouchAudio = MediaPlayer.create(getApplicationContext(),
 				R.raw.gammatone);
 
 		/*
 		 * Prepare language of Stimuli
 		 */
 		String lang = mSubExperiment.getLanguage();
 		forceLocale(lang);
 
 		initalizeLayout();
 
 	}
 
 	AnimationListener animationSlideInRightListener = new AnimationListener() {
 
 		@Override
 		public void onAnimationEnd(Animation animation) {
 			// TODO Auto-generated method stub
 		}
 
 		@Override
 		public void onAnimationRepeat(Animation animation) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onAnimationStart(Animation animation) {
 			// TODO Auto-generated method stub
 
 		}
 	};
 
 	public void initalizeLayout() {
 		setContentView(R.layout.one_image);
 		nextStimuli();
 	}
 
 	public void loadDefaults() {
 		ArrayList<Stimulus> ids = new ArrayList<Stimulus>();
 		ids.add(new Stimulus(R.drawable.androids_experimenter_kids));
 		mStimuli = ids;
 	}
 
 	public void nextStimuli() {
 		if (mStimuliIndex < 0) {
 			mStimuliIndex = 0;
 		} else {
 			mStimuliIndex += 1;
 		}
 		if (mStimuliIndex >= mStimuli.size()) {
 			finishSubExperiment();
 			return;
 		}
 
 		TextView t = (TextView) findViewById(R.id.stimuli_number);
 		String displayStimuliLabel = mStimuli.get(mStimuliIndex).getLabel();
 		if("".equals(displayStimuliLabel)){
 			int stimnumber = mStimuliIndex+1;
 			int stimtotal = mStimuli.size();
 			displayStimuliLabel = stimnumber+"/"+stimtotal;
 		}
 		t.setText(displayStimuliLabel);
 		
 		ImageView image = (ImageView) findViewById(R.id.onlyimage);
 		Drawable d = getResources().getDrawable(
 				mStimuli.get(mStimuliIndex).getImageFileId());
 		image.setImageDrawable(d);
 		image.startAnimation(animationSlideInRight);
 		mStimuli.get(mStimuliIndex).setStartTime(System.currentTimeMillis());
 		
 		playAudioStimuli();
 	}
 
 	public void onNextClick(View v) {
 		nextStimuli();
 	}
 
 	public void previousStimuli() {
 		mStimuliIndex -= 1;
 
 		if (mStimuliIndex < 0) {
 			mStimuliIndex = 0;
 			return;
 		}
 
 		ImageView image = (ImageView) findViewById(R.id.onlyimage);
 		Drawable d = getResources().getDrawable(
 				mStimuli.get(mStimuliIndex).getImageFileId());
 		image.setImageDrawable(d);
 
 		playAudioStimuli();
 
 	}
 
 	public void onPreviousClick(View v) {
 		previousStimuli();
 	}
 
 	/**
 	 * Forces the locale for the duration of the app to the language needed for
 	 * that version of the Bilingual Aphasia Test
 	 * 
 	 * @param lang
 	 * @return
 	 */
 	public String forceLocale(String lang) {
 		if (lang.equals(Locale.getDefault().getLanguage())) {
 			language = Locale.getDefault();
 			return Locale.getDefault().getDisplayLanguage();
 		}
 		Configuration config = getBaseContext().getResources()
 				.getConfiguration();
 		Locale locale = new Locale(lang);
 		Locale.setDefault(locale);
 		config.locale = locale;
 		getBaseContext().getResources().updateConfiguration(config,
 				getBaseContext().getResources().getDisplayMetrics());
 		language = Locale.getDefault();
 
 		return Locale.getDefault().getDisplayLanguage();
 	}
 
 	public void finishSubExperiment() {
 		mSubExperiment.setDisplayedStimuli(mStimuliIndex);
 		if (mStimuli.size() <= 1) {
 			mSubExperiment.setDisplayedStimuli(mStimuli.size());
 		}
 		mSubExperiment.setStimuli(mStimuli);
 		Intent video = new Intent(OPrime.INTENT_STOP_VIDEO_RECORDING);
 		sendBroadcast(video);
 		Intent audio = new Intent(OPrime.INTENT_START_AUDIO_RECORDING);
 		stopService(audio);
 
 		Intent intent = new Intent(OPrime.INTENT_FINISHED_SUB_EXPERIMENT);
 		intent.putExtra(OPrime.EXTRA_SUB_EXPERIMENT, mSubExperiment);
 		setResult(OPrime.EXPERIMENT_COMPLETED, intent);
 
 		finish();
 	}

 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 			finishSubExperiment();
 		}
 		return super.onKeyDown(keyCode, event);
 
 	}
 
 	public boolean onTouchEvent(MotionEvent me) {
 		long timeBetweenTouches = System.currentTimeMillis() - mLastTouchTime;
 		if (timeBetweenTouches < 300) {
 			return super.onTouchEvent(me);
 		}
 		// if in the top of the screen, ignore touch it was probably
 		// an attempt to hit the button
 		if (me.getY() < 60) {
 			return super.onTouchEvent(me);
 		}
 		Touch t = new Touch();
 		t.x = me.getX();
 		t.y = me.getY();
 		t.width = width;
 		t.height = height;
 		t.time = System.currentTimeMillis();
 		recordTouchPoint(t, mStimuliIndex);
 		playTouch();
 		mLastTouchTime = t.time;
 		/*
 		 * Auto advance to the next stimuli after recording the touch point. the
 		 * user can use teh arrows if they didnt mean to auto advacne.
 		 */
 		nextStimuli();
 		return super.onTouchEvent(me);
 	}
 
 	public void recordStimuliReactionTime(int stimuli) {
 		if (mStimuliIndex >= mStimuli.size()) {
 			return;
 		}
 		long endtime = System.currentTimeMillis();
 		mStimuli.get(stimuli).setTotalReactionTime(
 				endtime - mStimuli.get(stimuli).getStartTime());
 		mStimuli.get(stimuli).setReactionTimePostOffset(
 				endtime - mStimuli.get(stimuli).getAudioOffset());
 
 	}
 
 	public void recordTouchPoint(Touch touch, int stimuli) {
 		if (stimuli >= mStimuli.size()) {
 			return;
 		}
 		mStimuli.get(stimuli).touches.add(touch);
 		recordStimuliReactionTime(mStimuliIndex);
 		// Toast.makeText(getApplicationContext(), touch.x + ":" + touch.y,
 		// Toast.LENGTH_LONG).show();
 	}
 
 	public void playAudioStimuli() {
 		if (mAudioStimuli != null) {
 			mAudioStimuli.release();
 			mAudioStimuli = null;
 		}
 		mAudioStimuli = MediaPlayer.create(getApplicationContext(), mStimuli
 				.get(mStimuliIndex).getAudioFileId());
 		try {
 			mAudioStimuli.prepare();
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		mAudioStimuli
 				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
 					@Override
 					public void onCompletion(MediaPlayer mp) {
 						if (mStimuliIndex < mStimuli.size()) {
 							mStimuli.get(mStimuliIndex).setAudioOffset(
 									System.currentTimeMillis());
 						}
 						mp.release();
 					}
 				});
 		mAudioStimuli.start();
 
 	}
 
 	public void playTouch() {
 		mTouchAudio.start();
 
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (mAudioStimuli != null) {
 			mAudioStimuli.release();
 			mAudioStimuli = null;
 		}
 		if (mTouchAudio != null) {
 			mTouchAudio.release();
 			mTouchAudio = null;
 		}
 
 		super.onDestroy();
 	}
 
 }
