 package com.gallatinsystems.survey.device.activity;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import android.app.AlertDialog;
 import android.app.TabActivity;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 import com.gallatinsystems.survey.device.R;
 import com.gallatinsystems.survey.device.dao.SurveyDao;
 import com.gallatinsystems.survey.device.dao.SurveyDbAdapter;
 import com.gallatinsystems.survey.device.domain.Dependency;
 import com.gallatinsystems.survey.device.domain.Question;
 import com.gallatinsystems.survey.device.domain.QuestionGroup;
 import com.gallatinsystems.survey.device.domain.QuestionHelp;
 import com.gallatinsystems.survey.device.domain.QuestionResponse;
 import com.gallatinsystems.survey.device.domain.Survey;
 import com.gallatinsystems.survey.device.event.QuestionInteractionEvent;
 import com.gallatinsystems.survey.device.event.QuestionInteractionListener;
 import com.gallatinsystems.survey.device.util.ConstantUtil;
 import com.gallatinsystems.survey.device.util.FileUtil;
 import com.gallatinsystems.survey.device.util.LanguageData;
 import com.gallatinsystems.survey.device.util.LanguageUtil;
 import com.gallatinsystems.survey.device.util.ViewUtil;
 import com.gallatinsystems.survey.device.view.QuestionView;
 import com.gallatinsystems.survey.device.view.SubmitTabContentFactory;
 import com.gallatinsystems.survey.device.view.SurveyQuestionTabContentFactory;
 
 /**
  * main activity for the Field Survey application. It will read in the current
  * survey definition and render the survey UI based on the questions defined.
  * 
  * @author Christopher Fagiani
  * 
  */
 public class SurveyViewActivity extends TabActivity implements
 		QuestionInteractionListener {
 
 	private static final String TAG = "Survey View Activity";
 
 	private static final String ACTIVITY_NAME = "SurveyViewActivity";
 	private static final int PHOTO_ACTIVITY_REQUEST = 1;
 	private static final int VIDEO_ACTIVITY_REQUEST = 2;
 	private static final int SCAN_ACTIVITY_REQUEST = 3;
 	private static final int ACTIVITY_HELP_REQ = 4;
 	private static final int TXT_SIZE = 1;
 	private static final int SURVEY_LANG = 3;
 	private static final int SAVE_SURVEY = 4;
 	private static final int CLEAR_SURVEY = 5;
 	private static final String SUBMIT_TAB_TAG = "subtag";
 
 	private static final float LARGE_TXT_SIZE = 22;
 	private static final float NORMAL_TXT_SIZE = 14;
 	private static final String TEMP_PHOTO_NAME_PREFIX = "/wfpPhoto";
 	private static final String TEMP_VIDEO_NAME_PREFIX = "/wfpVideo";
 	private static final String VIDEO_PREFIX = "file:////";
 	private static final String HTTP_PREFIX = "http://";
 	private static final String VIDEO_TYPE = "video/*";
 	private static final String IMAGE_SUFFIX = ".jpg";
 	private static final String VIDEO_SUFFIX = ".mp4";
 	private ArrayList<SurveyQuestionTabContentFactory> tabContentFactories;
 	private QuestionView eventQuestionSource;
 	private SurveyDbAdapter databaseAdapter;
 	private String surveyId;
 	private Long respondentId;
 	private String userId;
 	private float currentTextSize;
 	private boolean[] selectedLanguages;
 	private String[] selectedLanguageCodes;
 	private HashMap<QuestionGroup, SurveyQuestionTabContentFactory> factoryMap;
 	private SubmitTabContentFactory submissionTab;
 	private Survey survey;
 	private boolean readOnly;
 	private boolean isTrackRecording;
 	private TabHost tabHost;
 	private int tabCount;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		currentTextSize = NORMAL_TXT_SIZE;
 		readOnly = false;
 		factoryMap = new HashMap<QuestionGroup, SurveyQuestionTabContentFactory>();
 		databaseAdapter = new SurveyDbAdapter(this);
 		databaseAdapter.open();
 		isTrackRecording = false;
 		setContentView(R.layout.main);
 		tabCount = 0;
 
 		String langSelection = databaseAdapter
 				.findPreference(ConstantUtil.SURVEY_LANG_SETTING_KEY);
 		LanguageData langData = LanguageUtil.loadLanguages(this, langSelection);
 
 		selectedLanguages = langData.getSelectedLanguages();
 		selectedLanguageCodes = LanguageUtil.getSelectedLangageCodes(this,
 				selectedLanguages);
 
 		Bundle extras = getIntent().getExtras();
 		userId = extras != null ? extras.getString(ConstantUtil.USER_ID_KEY)
 				: null;
 		if (userId == null) {
 			userId = savedInstanceState != null ? savedInstanceState
 					.getString(ConstantUtil.USER_ID_KEY) : null;
 		}
 
 		if (extras != null && extras.containsKey(ConstantUtil.READONLY_KEY)) {
 			readOnly = extras.getBoolean(ConstantUtil.READONLY_KEY);
 		}
 		if (savedInstanceState != null
 				&& savedInstanceState.containsKey(ConstantUtil.READONLY_KEY)) {
 			readOnly = savedInstanceState.getBoolean(ConstantUtil.READONLY_KEY);
 		}
 
 		surveyId = extras != null ? extras
 				.getString(ConstantUtil.SURVEY_ID_KEY) : null;
 		if (surveyId == null) {
 			surveyId = savedInstanceState != null ? savedInstanceState
 					.getString(ConstantUtil.SURVEY_ID_KEY) : "1";
 		}
 
 		respondentId = extras != null ? extras
 				.getLong(ConstantUtil.RESPONDENT_ID_KEY) : null;
 		if (respondentId == null || respondentId == 0L) {
 			respondentId = savedInstanceState != null ? savedInstanceState
 					.getLong(ConstantUtil.RESPONDENT_ID_KEY) : null;
 		}
 
 		try {
 			survey = SurveyDao.loadSurvey(databaseAdapter.findSurvey(surveyId),
 					getResources());
 
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, "Could not load survey xml file");
 		}
 
 		if (respondentId == null) {
 			respondentId = databaseAdapter.createOrLoadSurveyRespondent(
 					surveyId.toString(), userId.toString());
 		}
 
 		if (survey != null) {
 			TextView title = (TextView) findViewById(R.id.titletext);
 			title.setText(survey.getName());
 			tabContentFactories = new ArrayList<SurveyQuestionTabContentFactory>();
 			// if the device has an active survey, create a tab for each
 			// question group
 			tabHost = getTabHost();
 			String screenOn = databaseAdapter
 					.findPreference(ConstantUtil.SCREEN_ON_KEY);
 			if (screenOn != null && Boolean.parseBoolean(screenOn.trim())) {
 				tabHost.setKeepScreenOn(true);
 			}
 			for (QuestionGroup group : survey.getQuestionGroups()) {
 				if (group.getQuestions() != null
 						&& group.getQuestions().size() > 0) {
 					SurveyQuestionTabContentFactory factory = new SurveyQuestionTabContentFactory(
 							this, group, databaseAdapter, currentTextSize,
 							selectedLanguageCodes, readOnly);
 					factoryMap.put(group, factory);
 					tabHost.addTab(tabHost.newTabSpec(group.getHeading())
 							.setIndicator(group.getHeading()).setContent(
 									factory));
 					tabContentFactories.add(factory);
 					tabCount++;
 				}
 			}
 			if (!readOnly) {
 				// if we're not in read-only mode, we need to add the submission
 				// tab
 				submissionTab = new SubmitTabContentFactory(this,
 						databaseAdapter, currentTextSize, selectedLanguageCodes);
 				tabCount++;
 				tabHost.addTab(tabHost.newTabSpec(SUBMIT_TAB_TAG).setIndicator(
 						getString(R.string.submitbutton)).setContent(
 						submissionTab));
 				tabHost
 						.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
 							@Override
 							public void onTabChanged(String tabId) {
 								if (SUBMIT_TAB_TAG.equals(tabId)) {
 									submissionTab.refreshView();
 								}
 							}
 						});
 			}
 		}
 	}
 
 	/**
 	 * sets up question dependencies across question groups and registers
 	 * questionInteractionListeners on the dependent views. This should be
 	 * called each time a new tab is hydrated. It will iterate over all
 	 * questions in the survey and install dependencies and the
 	 * questionInteractionListeners. After installation, it will check to see if
 	 * the parent question contains a response. If so, it will fire a
 	 * questionInteractionEvent to ensure dependent questions are put into the
 	 * correct state
 	 * 
 	 * @param group
 	 */
 	public void establishDependencies(QuestionGroup group) {
 		// iterate over all groups we've processed
 		for (Entry<QuestionGroup, SurveyQuestionTabContentFactory> factoryEntry : factoryMap
 				.entrySet()) {
 			ArrayList<Question> groupQuestions = factoryEntry.getKey()
 					.getQuestions();
 			for (int i = 0; i < groupQuestions.size(); i++) {
 				Question q = groupQuestions.get(i);
 				ArrayList<Dependency> dependencies = q.getDependencies();
 				if (dependencies != null) {
 					for (int j = 0; j < dependencies.size(); j++) {
 						Dependency dep = dependencies.get(j);
 						QuestionView parentQ = findQuestionView(dep
 								.getQuestion());
 						QuestionView depQ = factoryEntry.getValue()
 								.getQuestionMap().get(q.getId());
 						if (depQ != null && parentQ != null) {
 							parentQ.addQuestionInteractionListener(depQ);
 							if (parentQ.getResponse(true) != null
 									&& parentQ.getResponse(true).hasValue()
 									&& parentQ != depQ) {
 								QuestionInteractionEvent event = new QuestionInteractionEvent(
 										QuestionInteractionEvent.QUESTION_ANSWER_EVENT,
 										parentQ);
 								depQ.onQuestionInteraction(event);
 							}
 						} else if (depQ != null) {
 							// if we're here, it's possible that the parent view
 							// hasn't been hydrated yet. So check the master
 							// question map for a response and use that to
 							// inform the child
 							QuestionResponse resp = databaseAdapter
 									.findSingleResponse(respondentId, dep
 											.getQuestion());
 							if (resp != null) {
 								depQ.handleDependencyParentResponse(dep, resp);
 							}
 
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * looks across all question factories for a question with the ID passed in
 	 * and returns the first match.
 	 * 
 	 * @param questionId
 	 * @return
 	 */
 	private QuestionView findQuestionView(String questionId) {
 		QuestionView view = null;
 		for (SurveyQuestionTabContentFactory factory : factoryMap.values()) {
 			if (factory.getQuestionMap() != null) {
 				view = factory.getQuestionMap().get(questionId);
 				if (view != null) {
 					break;
 				}
 			}
 		}
 		return view;
 	}
 
 	/**
 	 * moves focus to the next tab in the tab host
 	 */
 	public void advanceTab() {
 		int curTab = tabHost.getCurrentTab();
 		if (curTab < tabCount) {
 			tabHost.setCurrentTab(curTab + 1);					
 		}
 	}
 
 	/**
 	 * this is called when external activities launched by this activity return
 	 * and we need to do something. Right now the only activity we care about is
 	 * the media (photo/video/audio) activity (when the user is done recording
 	 * the media). When we get control back from the camera, we just need to
 	 * capture the details about the file that was just stored and stuff it into
 	 * the question response.
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// on activity return
 		try {
 
 			if (requestCode == PHOTO_ACTIVITY_REQUEST
 					|| requestCode == VIDEO_ACTIVITY_REQUEST) {
 				if (resultCode == RESULT_OK) {
 					String filePrefix = null;
 					String fileSuffix = null;
 					if (requestCode == PHOTO_ACTIVITY_REQUEST) {
 						filePrefix = TEMP_PHOTO_NAME_PREFIX;
 						fileSuffix = IMAGE_SUFFIX;
 					} else {
 						filePrefix = TEMP_VIDEO_NAME_PREFIX;
 						fileSuffix = VIDEO_SUFFIX;
 					}
 
 					File f = new File(Environment.getExternalStorageDirectory()
 							.getAbsolutePath()
 							+ filePrefix + fileSuffix);
 					String newName = Environment.getExternalStorageDirectory()
 							.getAbsolutePath()
 							+ filePrefix + System.nanoTime() + fileSuffix;
 					f.renameTo(new File(newName));
 					try {
 						if (eventQuestionSource != null) {
 							Bundle photoData = new Bundle();
 							photoData.putString(ConstantUtil.MEDIA_FILE_KEY,
 									newName);
 							eventQuestionSource.questionComplete(photoData);
 						}else{
 							Log.e(ACTIVITY_NAME,"eventQuestionSource is somehow null");
 						}
 					} catch (Exception e) {
 						Log.e(ACTIVITY_NAME, e.getMessage());
 					} finally {
 						eventQuestionSource = null;
 					}
				}else{
					Log.e(ACTIVITY_NAME,"Result of camera op was not ok: "+resultCode);
 				}
 			} else if (requestCode == SCAN_ACTIVITY_REQUEST) {
 				if (resultCode == RESULT_OK) {
 					eventQuestionSource.questionComplete(data.getExtras());
 				}
 			} else if (requestCode == ACTIVITY_HELP_REQ) {
 				if (resultCode == RESULT_OK) {
 					QuestionResponse resp = new QuestionResponse(null,
 							respondentId, eventQuestionSource.getQuestion()
 									.getId(),
 							data.getStringExtra(ConstantUtil.CALC_RESULT_KEY),
 							ConstantUtil.VALUE_RESPONSE_TYPE);
 
 					resp = databaseAdapter
 							.createOrUpdateSurveyResponseForQuestion(resp);
 					eventQuestionSource.setResponse(resp);
 				}
 			}
 		} catch (Exception e) {
			Log.e(ACTIVITY_NAME, "Error handling activity return", e);
 		}
 	}
 
 	/**
 	 * iterates over all tabs and calls their RESET method to blank out the
 	 * questions
 	 */
 	public void resetAllQuestions() {
 		for (int i = 0; i < tabContentFactories.size(); i++) {
 			tabContentFactories.get(i).resetTabQuestions();			
 		}
 		tabHost.setCurrentTab(0);		
 		if (submissionTab != null) {
 			submissionTab.refreshView();
 		}
 	}
 
 	/**
 	 * event handler that can be used to handle events fired by individual
 	 * questions at the Activity level. Because we can't launch the photo
 	 * activity from a view (we need to launch it from the activity), the photo
 	 * question view fires a QuestionInteractionEvent (to which this activity
 	 * listens). When we get the event, we can then spawn the camera activity.
 	 * 
 	 * Currently, this method supports handing TAKE_PHOTO_EVENT and
 	 * VIDEO_TIP_EVENT types
 	 */
 	public void onQuestionInteraction(QuestionInteractionEvent event) {
 		if (QuestionInteractionEvent.TAKE_PHOTO_EVENT.equals(event
 				.getEventType())) {
 			// fire off the intent
 			Intent i = new Intent(
 					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 			i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri
 					.fromFile(new File(Environment
 							.getExternalStorageDirectory().getAbsolutePath()
 							+ TEMP_PHOTO_NAME_PREFIX + IMAGE_SUFFIX)));
 			eventQuestionSource = event.getSource();
 
 			startActivityForResult(i, PHOTO_ACTIVITY_REQUEST);
 		} else if (QuestionInteractionEvent.VIDEO_TIP_VIEW.equals(event
 				.getEventType())) {
 			Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
 			Uri uri = null;
 			String src = event.getSource().getQuestion().getHelpByType(
 					ConstantUtil.VIDEO_HELP_TYPE).get(0).getValue().trim();
 			if (src.toLowerCase().startsWith(HTTP_PREFIX)) {
 				// first see if we have a precached copy of the file
 				String localFile = FileUtil.convertRemoteToLocalFile(src,
 						surveyId);
 				if (FileUtil.doesFileExist(localFile)) {
 					uri = Uri.parse(VIDEO_PREFIX + localFile);
 				} else {
 					uri = Uri.parse(src);
 				}
 			} else {
 				// if the source doesn't start with http, assume it's
 				// referencing device storage
 				uri = Uri.parse(VIDEO_PREFIX + src);
 			}
 			intent.setDataAndType(uri, VIDEO_TYPE);
 
 			startActivity(intent);
 		} else if (QuestionInteractionEvent.PHOTO_TIP_VIEW.equals(event
 				.getEventType())) {
 			Intent intent = new Intent(this, ImageBrowserActivity.class);
 
 			intent.putExtra(ConstantUtil.SURVEY_ID_KEY, surveyId);
 			ArrayList<QuestionHelp> helpList = event.getSource().getQuestion()
 					.getHelpByType(ConstantUtil.IMAGE_HELP_TYPE);
 			ArrayList<String> urls = new ArrayList<String>();
 			ArrayList<String> captions = new ArrayList<String>();
 			for (int i = 0; i < helpList.size(); i++) {
 				urls.add(helpList.get(i).getValue());
 				captions.add(helpList.get(i).getText());
 			}
 			intent.putExtra(ConstantUtil.IMAGE_URL_LIST_KEY, urls);
 			intent.putExtra(ConstantUtil.IMAGE_CAPTION_LIST_KEY, captions);
 
 			startActivity(intent);
 		} else if (QuestionInteractionEvent.ACTIVITY_TIP_VIEW.equals(event
 				.getEventType())) {
 			eventQuestionSource = event.getSource();
 			try {
 				Intent i = new Intent(this, ConstantUtil.HELP_ACTIVITIES
 						.get(event.getSource().getQuestion().getHelpByType(
 								ConstantUtil.ACTIVITY_HELP_TYPE).get(0)
 								.getValue()));
 				i.putExtra(ConstantUtil.MODE_KEY,
 						ConstantUtil.SURVEY_RESULT_MODE);
 
 				startActivityForResult(i, ACTIVITY_HELP_REQ);
 			} catch (Exception e) {
 				Log.e(TAG, "Could not start activity help", e);
 			}
 		} else if (QuestionInteractionEvent.TAKE_VIDEO_EVENT.equals(event
 				.getEventType())) {
 			// fire off the intent
 			Intent i = new Intent(
 					android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
 			i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri
 					.fromFile(new File(Environment
 							.getExternalStorageDirectory().getAbsolutePath()
 							+ TEMP_VIDEO_NAME_PREFIX + VIDEO_SUFFIX)));
 			eventQuestionSource = event.getSource();
 
 			startActivityForResult(i, VIDEO_ACTIVITY_REQUEST);
 		} else if (QuestionInteractionEvent.SCAN_BARCODE_EVENT.equals(event
 				.getEventType())) {
 			Intent intent = new Intent(ConstantUtil.BARCODE_SCAN_INTENT);
 			try {
 
 				startActivityForResult(intent, SCAN_ACTIVITY_REQUEST);
 				eventQuestionSource = event.getSource();
 			} catch (ActivityNotFoundException ex) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setMessage(R.string.barcodeerror);
 				builder.setPositiveButton(R.string.okbutton,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 				builder.show();
 			}
 		} else if (QuestionInteractionEvent.START_TRACK.equals(event
 				.getEventType())) {
 			isTrackRecording = true;
 			toggleTabButtons(false);
 		} else if (QuestionInteractionEvent.END_TRACK.equals(event
 				.getEventType())) {
 			isTrackRecording = false;
 			toggleTabButtons(true);
 		}
 	}
 
 	/**
 	 * iterates over all tabConentFactories and toggles their submission/menu
 	 * buttons
 	 * 
 	 * @param isEnabled
 	 */
 	private void toggleTabButtons(boolean isEnabled) {
 		if (tabContentFactories != null) {
 			for (int i = 0; i < tabContentFactories.size(); i++) {
 				tabContentFactories.get(i).toggleButtons(isEnabled);
 			}
 		}
 
 	}
 
 	/**
 	 * checks if all the mandatory questions (on all tabs) have responses
 	 * 
 	 * @return
 	 */
 	public ArrayList<Question> checkMandatory() {
 		ArrayList<Question> missingQuestions = new ArrayList<Question>();
 		if (tabContentFactories != null) {
 			ArrayList<Question> candidateMissingQuestions = new ArrayList<Question>();
 			for (int i = 0; i < tabContentFactories.size(); i++) {
 				candidateMissingQuestions.addAll(tabContentFactories.get(i)
 						.checkMandatoryQuestions());
 			}
 
 			// now make sure that the candidate missing questions are really
 			// missing by seeing if their dependencies are fulfilled
 			for (int i = 0; i < candidateMissingQuestions.size(); i++) {
 				ArrayList<Dependency> dependencies = candidateMissingQuestions
 						.get(i).getDependencies();
 				if (dependencies != null) {
 					int satisfiedCount = 0;
 					for (int j = 0; j < dependencies.size(); j++) {
 						for (int k = 0; k < tabContentFactories.size(); k++) {
 							if (tabContentFactories.get(k)
 									.isDependencySatisfied(dependencies.get(j))) {
 								satisfiedCount++;
 								break;
 							}
 						}
 						if(satisfiedCount == dependencies.size()){
 							missingQuestions.add(candidateMissingQuestions
 									.get(i));
 						}
 
 					}
 				} else {
 					missingQuestions.add(candidateMissingQuestions.get(i));
 				}
 			}
 		}
 		return missingQuestions;
 	}
 
 	/**
 	 * presents the survey options menu when the user presses the menu key
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, TXT_SIZE, 0, R.string.largetxtoption);
 		menu.add(0, SAVE_SURVEY, 1, R.string.savestartnew);
 		menu.add(0, SURVEY_LANG, 2, R.string.langoption);
 		menu.add(0, CLEAR_SURVEY, 3, R.string.clearbutton);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuOpened(int featureId, Menu menu) {
 		super.onMenuOpened(featureId, menu);
 		if (currentTextSize == LARGE_TXT_SIZE) {
 			menu.getItem(0).setTitle(R.string.normaltxtoption);
 		} else {
 			menu.getItem(0).setTitle(R.string.largetxtoption);
 		}
 		menu.getItem(1).setEnabled(!isTrackRecording);
 		menu.getItem(3).setEnabled(!isTrackRecording);
 
 		return true;
 	}
 
 	/**
 	 * handles the button press for the "add" button on the menu
 	 */
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case TXT_SIZE:
 			if (currentTextSize == LARGE_TXT_SIZE) {
 				updateTextSize(NORMAL_TXT_SIZE);
 			} else {
 				updateTextSize(LARGE_TXT_SIZE);
 			}
 			return true;
 		case SURVEY_LANG:
 			ViewUtil.displayLanguageSelector(this, selectedLanguages,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int clicked) {
 							dialog.dismiss();
 							selectedLanguageCodes = LanguageUtil
 									.getSelectedLangageCodes(
 											SurveyViewActivity.this,
 											selectedLanguages);
 							databaseAdapter
 									.savePreference(
 											ConstantUtil.SURVEY_LANG_SETTING_KEY,
 											LanguageUtil
 													.formLanguagePreferenceString(selectedLanguages));
 							for (int i = 0; i < tabContentFactories.size(); i++) {
 								tabContentFactories.get(i)
 										.updateQuestionLanguages(
 												selectedLanguageCodes);
 							}
 						}
 					});
 			return true;
 		case CLEAR_SURVEY:
 			if (!readOnly) {
 				ViewUtil.showConfirmDialog(R.string.cleartitle,
 						R.string.cleardesc, this, true,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								resetAllQuestions();
 								dialog.dismiss();
 							}
 						});
 
 			}
 			return true;
 		case SAVE_SURVEY:
 			if (!readOnly) {
 				// make sure we don't lose anything that was already written
 				for (int i = 0; i < tabContentFactories.size(); i++) {
 					tabContentFactories.get(i).saveState(respondentId);
 				}
 				databaseAdapter.updateSurveyStatus(respondentId.toString(),
 						ConstantUtil.SAVED_STATUS);
 				ViewUtil.showConfirmDialog(R.string.savecompletetitle,
 						R.string.savecompletetext, this, false,
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								dialog.dismiss();
 								startNewSurvey();
 
 							}
 						});
 			}
 			return true;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	/**
 	 * creates a new response object/record and sets the id in the context then
 	 * resets the question view.
 	 */
 	private void startNewSurvey() {
 		// create a new response object so we're ready for the
 		// next instance
 		setRespondentId(databaseAdapter
 				.createSurveyRespondent(surveyId, userId));
 		resetAllQuestions();
 	}
 
 	/**
 	 * iterates over all questions in the tab factories and changes their text
 	 * size
 	 * 
 	 * @param size
 	 */
 	private void updateTextSize(float size) {
 		if (tabContentFactories != null) {
 			for (int i = 0; i < tabContentFactories.size(); i++) {
 				tabContentFactories.get(i).updateTextSize(size);
 			}
 		}
 		currentTextSize = size;
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if (outState != null) {
 			if (surveyId != null) {
 				outState.putString(ConstantUtil.SURVEY_ID_KEY, surveyId);
 			}
 			if (respondentId != null) {
 				outState.putLong(ConstantUtil.RESPONDENT_ID_KEY, respondentId);
 			}
 			if (userId != null) {
 				outState.putString(ConstantUtil.USER_ID_KEY, userId);
 			}
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (tabContentFactories != null) {
 			for (SurveyQuestionTabContentFactory tab : tabContentFactories) {
 				tab.saveState(respondentId);
 			}
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		try {
 			super.onResume();
 			if (tabContentFactories != null) {
 				for (SurveyQuestionTabContentFactory tab : tabContentFactories) {
 					tab.loadState(respondentId);
 				}
 			}
 		} catch (Exception e) {
 			Log.w(TAG, "Error while restoring", e);
 		}
 	}
 
 	protected void onDestroy() {
 		super.onDestroy();
 		// make sure we're not keeping the screen on once we're destroyed
 		if (tabHost != null) {
 			tabHost.setKeepScreenOn(false);
 		}
 		if (databaseAdapter != null) {
 			databaseAdapter.close();
 		}
 	}
 
 	public String getSurveyId() {
 		return surveyId;
 	}
 
 	public void setRespondentId(Long respondentId) {
 		this.respondentId = respondentId;
 	}
 
 	public Long getRespondentId() {
 		return respondentId;
 	}
 
 	public String getUserId() {
 		return userId;
 	}
 
 	public boolean isTrackRecording() {
 		return isTrackRecording;
 	}
 
 }
