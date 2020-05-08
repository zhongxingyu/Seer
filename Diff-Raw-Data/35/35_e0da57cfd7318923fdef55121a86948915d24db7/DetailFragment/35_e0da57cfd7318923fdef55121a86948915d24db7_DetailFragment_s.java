 package de.da_sense.moses.client;
 
 import java.util.ArrayList;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.method.ScrollingMovementMethod;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
 import de.da_sense.moses.client.abstraction.apks.HistoryExternalApplicationsManager;
 import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
 import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
 import de.da_sense.moses.client.service.MosesService;
 import de.da_sense.moses.client.util.Log;
 import de.da_sense.moses.client.util.Toaster;
 
 /**
  * Shows the details for a user study.
  * 
  * @author Sandra Amend, Wladimir Schmidt
  * @author Zijad Maksuti
  */
 public class DetailFragment extends Fragment {
 
 	/**
 	 * Request code that {@link SurveyActivity} should use for passing back
 	 * information about survey status:<br>
 	 * {@link Activity#RESULT_OK} if it is successfully sent to server.
 	 */
 	private static final int REQUEST_CODE_NOTIFY_ABOUT_SEND = 17;
 
 	/** Belongs to Available. Constant for creating the view. */
 	protected final static int AVAILABLE = 0;
 	/** Belongs to Running. Constant for creating the view. */
 	protected final static int RUNNING = 1;
 	/** Belongs to History. Constant for creating the view. */
 	protected final static int HISTORY = 2;
 	/** a log tag for this class */
 	private final static String LOG_TAG = DetailFragment.class.getName();
 
 	/**
 	 * The Activity containing this fragment
 	 */
 	private static Activity mActivity = null;
 
 	private static View mDetailFragmentView;
 
 	private static DetailFragment mThisInstance;
 
 	private static int mBelongsTo;
 
 	private static int mIndex;
 
 	private static String mAPKID;
 
 	/**
 	 * 
 	 * @return the index from the arguments
 	 */
 	public int getShownIndex() {
 		return getArguments().getInt("index", 0);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		mThisInstance = this;
 		if (container == null) {
 			return null;
 		} else {
 			container.setBackgroundColor(getResources().getColor(
 					android.R.color.background_light));
 
 			// get the arguments
 			final String appname;
 			String description;
 			String startDate, endDate, apkVersion;
 			ArrayList<Integer> sensors;
 
 			// if this gets started as a fragment
 			if (savedInstanceState == null) {
 				Log.d(LOG_TAG, "savedInstance == null");
 				savedInstanceState = getArguments();
 				Log.d(LOG_TAG, "NOW savedInstance = " + savedInstanceState);
 			}
 
 			// supposed to show a placeholder?
 			String placeholder = savedInstanceState
 					.getString("de.da_sense.moses.client.placeholder");
 			if (placeholder != null && placeholder.equals("yes")) {
 				// inflate the placeholder
 				Log.d(LOG_TAG, "onCreateView about to inflate PLACEHOLDER");
 				mDetailFragmentView = inflater.inflate(
 						R.layout.app_info_placeholder, container, false);
 			} else { // normal display of details
 				// retrieve the arguments
 				mIndex = savedInstanceState
 						.getInt("de.da_sense.moses.client.index");
 				mBelongsTo = savedInstanceState
 						.getInt(WelcomeActivity.KEY_BELONGS_TO);
 				appname = savedInstanceState
 						.getString("de.da_sense.moses.client.appname");
 				description = savedInstanceState
 						.getString("de.da_sense.moses.client.description");
 				sensors = savedInstanceState
 						.getIntegerArrayList("de.da_sense.moses.client.sensors");
 				mAPKID = savedInstanceState
 						.getString(ExternalApplication.KEY_APK_ID);
 				apkVersion = savedInstanceState
 						.getString("de.da_sense.moses.client.apkVersion");
 				startDate = savedInstanceState
 						.getString("de.da_sense.moses.client.startDate");
 				endDate = savedInstanceState
 						.getString("de.da_sense.moses.client.endDate");
 
 				Log.d(LOG_TAG, "\nretrieved index = " + mIndex
 						+ "\nretrieved belongsTo = " + mBelongsTo
 						+ "\nretrieved appname = " + appname
 						+ "\nretireved description = " + description
 						+ "\nretireved sensors = " + sensors
 						+ "\nretireved apkid = " + mAPKID
 						+ "\nretireved startDate = " + startDate
 						+ "\nretireved endDate = " + endDate
 						+ "\nretireved apkVersion = " + apkVersion);
 
 				if (appname != null) {
 					// inflate the detail view
 					Log.d(LOG_TAG, "onCreateView about to inflate View");
 					mDetailFragmentView = inflater.inflate(R.layout.app_info,
 							container, false);
 					// insert app name
 					TextView t = (TextView) mDetailFragmentView
 							.findViewById(R.id.usname);
 					t.setText(appname);
 					// insert description
 					t = (TextView) mDetailFragmentView
 							.findViewById(R.id.description);
 					t.setMovementMethod(ScrollingMovementMethod.getInstance());
 					t.setText(description);
 					t = (TextView) mDetailFragmentView
 							.findViewById(R.id.tv_us_startdate);
 					t.setText(startDate);
 					t = (TextView) mDetailFragmentView
 							.findViewById(R.id.tv_us_enddate);
 					t.setText(endDate);
 					t = (TextView) mDetailFragmentView
 							.findViewById(R.id.tv_us_apkversion);
 					t.setText(apkVersion);
 				} else {
 					Log.e(LOG_TAG, "User study's informations are missing");
 					return null;
 				}
 			}
 
 			return mDetailFragmentView;
 		}
 	}
 
 	/**
 	 * Initializes the button logic
 	 */
 	private void initializeButtons() {
 		ActionBar ab = mActivity.getActionBar();
 		if (mBelongsTo == AVAILABLE) {
 			ab.setTitle(getString(R.string.userStudy_available));
 			// get start button
 			Button button = (Button) mDetailFragmentView
 					.findViewById(R.id.startapp);
 			// change the text of it to install
 			button.setText(getString(R.string.install));
 			// make an action listener for it
 			button.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					ExternalApplication app = AvailableFragment.getInstance()
 							.getExternalApps().get(mIndex);// getShownIndex());
 					Log.d(LOG_TAG, "installing app ( " + app.getName()
 							+ " ) with apkid = " + app.getID());
 					AvailableFragment.getInstance().handleInstallApp(app);
 				}
 			});
 			// get update button
 			button = (Button) mDetailFragmentView.findViewById(R.id.update);
 			button.setVisibility(View.GONE); // there is no update
 												// for
 												// this new app
 			// get questionnaire button
 			button = (Button) mDetailFragmentView
 					.findViewById(R.id.btn_questionnaire);
 			button.setVisibility(View.GONE); // there is no
 												// questionnaire for
 												// this new app
 		} else if (mBelongsTo == RUNNING) {
 			ab.setTitle(getString(R.string.userStudy_running));
 			// get start button
 			Button button = (Button) mDetailFragmentView
 					.findViewById(R.id.startapp);
 			Button updateButton = (Button) mDetailFragmentView
 					.findViewById(R.id.update);
 			updateButton.setVisibility(RunningFragment.getInstance()
 					.getInstalledApps().get(getShownIndex())
 					.getUpdateAvailable() ? View.VISIBLE : View.GONE);
 			// change the text of it to install
 			button.setText(getString(R.string.open));
 			// make an action listener for it
 			button.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					InstalledExternalApplication app = RunningFragment
 							.getInstance().getInstalledApps().get(mIndex);// getShownIndex());
 					Log.d(LOG_TAG, "open app ( " + app.getName()
 							+ " ) with apkid = " + app.getID());
 					RunningFragment.getInstance().handleStartApp(app);
 				}
 			});
 			// get questionnaire button, if the questionnaire is not
 			// yet
 			// sent
 			button = (Button) mDetailFragmentView
 					.findViewById(R.id.btn_questionnaire);
 			// check if it has Questionnaire and if it's sent
 			if (InstalledExternalApplicationsManager.getInstance() == null)
 				InstalledExternalApplicationsManager.init(MosesService
 						.getInstance());
 			InstalledExternalApplication app = InstalledExternalApplicationsManager
 					.getInstance().getAppForId(mAPKID);
 			Log.d(LOG_TAG, "app = " + app);
 			if (app != null) {
 				final boolean hasSurveyLocal = app.hasSurveyLocally();
 				boolean isQuestionnaireSent = hasSurveyLocal ? app.getSurvey()
 						.hasBeenSent() : false;
 				Log.d(LOG_TAG, "hasQuestLocal" + hasSurveyLocal + "isQuestSent"
 						+ isQuestionnaireSent);
 				// set button according to the booleans
 				if (isQuestionnaireSent) {
 					button.setText(getString(R.string.details_running_questionnairesent));
 					button.setClickable(false);
 					button.setEnabled(false);
 				} else {
 
 					if (hasSurveyLocal) {
 						button.setText(getString(R.string.btn_survey));
 						button.setClickable(true);
 						button.setEnabled(true);
 					} else {
 						button.setText(getString(R.string.download_survey));
 						if (AsyncGetSurvey.isRunning()) {
 							// disable the button, the async task is
 							// still waiting for the survey
 							button.setEnabled(false);
 						}
 					}
 
 					button.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							if (hasSurveyLocal) {
 								startSurveyActivity(mAPKID);
 							} else {
 								Log.d(LOG_TAG,
 										"Getting Questionnaire from Server");
 								v.setEnabled(false); // disable the
 														// button
 								Toaster.showToast(
 										mActivity,
 										getString(R.string.notification_downloading_survey));
 								AsyncGetSurvey getSurveyAsyncTask = new AsyncGetSurvey();
 								InstalledExternalApplicationsManager
 										.getInstance().getAppForId(mAPKID)
 										.getQuestionnaireFromServer();
 								getSurveyAsyncTask.execute(mAPKID);
 							}
 						}
 					});
 				}
 				// get update button
 				boolean updateAvailable = InstalledExternalApplicationsManager
 						.getInstance().getAppForId(mAPKID).isUpdateAvailable();
 				button = (Button) mDetailFragmentView.findViewById(R.id.update);
 				if (updateAvailable) {
 					button.setVisibility(View.VISIBLE);
 					button.setOnClickListener(new OnClickListener() {
 						
 						@Override
 						public void onClick(View v) {
 							AvailableFragment.getInstance().handleInstallApp(InstalledExternalApplicationsManager.getInstance().getAppForId(mAPKID));
 						}
 					});
 					// make the label of survey button shorter
 					Button surveyButton = (Button) mDetailFragmentView.findViewById(R.id.btn_questionnaire);
 					surveyButton.setText(getString(R.string.btn_survey_short));
 				} else {
 					button.setVisibility(View.GONE);
 				}
 			}
 		} else if (mBelongsTo == HISTORY) {
 			ab.setTitle(getString(R.string.userStudy_past));
 			// get start button
 			Button button = (Button) mDetailFragmentView
 					.findViewById(R.id.startapp);
 			button.setVisibility(View.GONE); // hide open / install
 												// button
 			// get update button
 			button = (Button) mDetailFragmentView.findViewById(R.id.update);
 			button.setVisibility(View.GONE); // there is no update
 												// for
 												// this old app
 			// get questionnaire button, if the questionnaire is not
 			// yet
 			// sent
 			button = (Button) mDetailFragmentView
 					.findViewById(R.id.btn_questionnaire);
 			button.setVisibility(View.GONE);
 			// check if it has Questionnaire and if it's sent
 			if (HistoryExternalApplicationsManager.getInstance() == null)
 				HistoryExternalApplicationsManager.init(MosesService
 						.getInstance());
 			boolean hasQuestionnaire = HistoryExternalApplicationsManager
 					.getInstance().getAppForId(mAPKID).hasSurveyLocally();
 //			boolean isQuestionnaireSent = hasQuestionnaire ? HistoryExternalApplicationsManager
 //					.getInstance().getAppForId(mAPKID).getSurvey()
 //					.hasBeenSent()
 //					: true;
 			// set button according to the booleans
 			if (!hasQuestionnaire) {
 				button.setText(getString(R.string.details_running_noquestionnaire));
 				button.setClickable(false);
 				button.setEnabled(false);
 			}
 //			else if (isQuestionnaireSent) {
 //				button.setText(getString(R.string.details_running_questionnairesent));
 //				button.setClickable(false);
 //				button.setEnabled(false);
 //			} 
 			else {
 				// we have Survey
 				button.setVisibility(View.VISIBLE);
 				button.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						Intent intent = new Intent();
 						intent.setClass(mActivity, SurveyActivity.class);
 						intent.putExtra(ExternalApplication.KEY_APK_ID, mAPKID);
 						intent.putExtra(WelcomeActivity.KEY_BELONGS_TO,
 								HISTORY);
 						startActivity(intent);
 					}
 				});
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
 	 */
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		mActivity = activity;
 	}
 
 	@Override
 	public void onResume() {
 		initializeButtons();
 		super.onResume();
 		Log.d(LOG_TAG, "onResume index=" + getShownIndex());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onActivityResult(int, int,
 	 * android.content.Intent)
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == REQUEST_CODE_NOTIFY_ABOUT_SEND) {
 			if (resultCode == Activity.RESULT_OK) {
 				// the survey has been sent to server, forward the result and
 				// finish this activity
 				mActivity.setResult(Activity.RESULT_OK);
 				mActivity.finish();
 			}
 		} else
 			super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	/**
 	 * Starts the {@link SurveyActivity} for viewing the survey with the
 	 * specified apkid.
 	 * 
 	 * @param apkid
 	 *            the id of the apk associated with the survey to be viewed
 	 */
 	private void startSurveyActivity(String apkid) {
 		Intent intent = new Intent();
 		intent.setClass(mActivity, SurveyActivity.class);
 		intent.putExtra(ExternalApplication.KEY_APK_ID, apkid);
 		intent.putExtra(WelcomeActivity.KEY_BELONGS_TO, RUNNING);
 		startActivityForResult(intent, REQUEST_CODE_NOTIFY_ABOUT_SEND);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onPause()
 	 */
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
 	/**
 	 * Sends a request to server for a survey. Periodically checks if the server
 	 * has responded for 3 seconds. If so, takes proper actions. If not, it
 	 * informs the user.
 	 * 
 	 * @author Zijad Maksuti
 	 * 
 	 */
 	public static class AsyncGetSurvey extends AsyncTask<String, Void, Void> {
 
 		private static String mapkID;
 		private long mTimeStarted;
 		private static boolean mSurveyForApkArrived;
 		private static boolean mUnknownReason;
 		private static boolean mNoSurvey;
 		private boolean mToLongToRespond;
 		private static boolean mIsRunning = false;
 
 		/**
 		 * @return the mIsRunning
 		 */
 		public static boolean isRunning() {
 			return mIsRunning;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.os.AsyncTask#onPreExecute()
 		 */
 		@Override
 		protected void onPreExecute() {
 			mSurveyForApkArrived = false;
 			mUnknownReason = false;
 			mNoSurvey = false;
 			mToLongToRespond = false;
 			super.onPreExecute();
 		}
 
 		@Override
 		protected Void doInBackground(String... params) {
 			mTimeStarted = System.nanoTime();
 			mapkID = params[0];
 			mIsRunning = true;
 			while (true) {
 				if (isCancelled()) {
 					mIsRunning = false;
 					break;
 				} else {
 					if (mSurveyForApkArrived) {
 						// survey has arrived, kill the pigeons
 						break;
 					} else {
 						if (System.nanoTime() - mTimeStarted > 3000000000L) { // 3
 																				// seconds
 							mToLongToRespond = true;
 							break; // do not wait to long
 						}
 					}
 				}
 			}
 			return null;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
 		 */
 		@Override
 		protected void onPostExecute(Void result) {
 			mIsRunning = false;
 			if (mDetailFragmentView != null && mThisInstance.isVisible()) {
 				Button button = (Button) mDetailFragmentView
 						.findViewById(R.id.btn_questionnaire);
 				boolean enableButton = false;
 				if (mToLongToRespond) {
 					enableButton = true;
 					Toaster.showToast(
 							mActivity,
 							mActivity
 									.getString(R.string.notification_server_took_too_long_to_respond));
 				} else if (mUnknownReason) {
 					enableButton = true;
 					Toaster.showToast(
 							mActivity,
 							mActivity
 									.getString(R.string.notification_unknown_error_has_occurred));
 				} else if (mNoSurvey) {
 					enableButton = true;
 					Toaster.showToast(
 							mActivity,
 							mActivity
 									.getString(R.string.notification_no_survey_for_this_apk));
 				} else {
 					mThisInstance.startSurveyActivity(mapkID);// everything was
 																// ok
 				}
 				if (enableButton) {
 					if (button != null) {
 						button.setClickable(true);
 						button.setEnabled(true);
 						button.invalidate();
 					} else {
 						Log.w(LOG_TAG, "onPostExecute() the button was null");
 					}
 				}
 			}
 		}
 
 		/**
 		 * Tells this {@link AsyncGetSurvey} task that a survey for an apk with
 		 * the given id has arrived. He should take care of the rest.
 		 * 
 		 * @param apkid
 		 *            the apkid of the survey that has arrived
 		 * @param noSurvey
 		 *            the caller should set this boolean to true if the survey
 		 *            for the given apkid does not exist
 		 */
 		public static void surveyArrived(String apkid, boolean noSurvey) {
 			if (apkid == null) {
 				// the survey could not be obtained because of an unknown reason
 				mUnknownReason = true;
 				mSurveyForApkArrived = true;
 			} else if (mapkID.equals(apkid)) { // check if it is the survey for
 												// the apk we need
 				mNoSurvey = noSurvey;
 				mSurveyForApkArrived = true;
 			}
 		}
 
 	}
 
 }
