 package perassoft.multiplicationtables;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 
 import perassoft.multiplicationtables.R.string;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.text.Html;
 import android.text.Spanned;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnInitListener {
 
 	private static final String TTS = "TTS";
 	private static final String SCORE = "score";
 	private static final String NEUTRAL = "2";
 	private static final String OK = "1";
 	private static final int RESULT_SPEECH_CHECK_CODE = 0;
 	private static final int RESULT_SETTINGS = 1;
 	private static final String A = "a";
 	private static final String B = "b";
 	private static final String ANSWERS = "answers";
 	private int a;
 	private int b;
 	private TextToSpeech tts;
 	private List<Integer> tables;
 	private int maxButtons = 5;
 	private View.OnClickListener yesClickListener;
 	private OnClickListener noClickListener;
 	private List<Button> buttons = new ArrayList<Button>();
 	private Random random;
 	private int score = 0;
 	private ArrayList<Answer> answers;
 	private boolean restoredFromInstanceState;
 	private String[] messages;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		Intent checkIntent = new Intent();
 		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
 		startActivityForResult(checkIntent, RESULT_SPEECH_CHECK_CODE);
 		random = new Random(System.currentTimeMillis());
 		setDifficulties();
 		messages = getResources().getStringArray(R.array.joke_messages);
 		yesClickListener = new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				score++;
 				StringBuilder sb = new StringBuilder();
 				sb.append(getString(R.string.right));
 				sb.append(" ");
 				sb.append(a);
 				sb.append(" ");
 				sb.append(getString(R.string.times));
 				sb.append(" ");
 				sb.append(b);
 				sb.append(" = ");
 				sb.append(a * b);
 				sb.append(".");
 				message(sb.toString(), NEUTRAL, getCurrentLocale());
 				message(getRandomMessage(), OK, getCurrentJokeMessageLocale());
 				updateScoreView();
 			}
 
 		};
 
 		noClickListener = new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				score -= 2;
 				message(getString(R.string.wrong), NEUTRAL, getCurrentLocale());
 				updateScoreView();
 			}
 
 		};
 
 		if (savedInstanceState != null) {
 			score = savedInstanceState.getInt(SCORE, 0);
 			a = savedInstanceState.getInt(A);
 			b = savedInstanceState.getInt(B);
			Object serializable = savedInstanceState.getSerializable(ANSWERS);
 			answers = serializable == null ? new ArrayList<Answer>() : (ArrayList<Answer>)serializable;
 			restoredFromInstanceState = true;
 			setQuestionText();
 			generateButtons();
 		} else {
 			restoredFromInstanceState = false;
 			answers = new ArrayList<Answer>();
 		}
 
 		updateScoreView();
 	}
 
 	private String getRandomMessage() {
 		return messages[random.nextInt(messages.length)];
 	}
 
 	private void updateScoreView() {
 		TextView scoreText = (TextView) findViewById(R.id.textViewScore);
 		scoreText.setText(String.format(getString(R.string.score), score));
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.action_settings :
 				Intent intent = new Intent(this, SettingsActivity.class);
 				startActivityForResult(intent, RESULT_SETTINGS);
 				break;
 			case R.id.action_about :
 				showAboutDialog();
 				break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void showAboutDialog() {
 		Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(string.app_name);
 		Spanned msg = Html.fromHtml(getString(R.string.about_msg));
 		builder.setMessage(msg);
 		builder.setCancelable(true);
 		builder.setIcon(android.R.drawable.ic_dialog_info);
 		builder.setPositiveButton(android.R.string.ok,
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 
 					}
 				});
 		AlertDialog dialog = builder.create();
 		dialog.show();
 		TextView messageView = (TextView) dialog
 				.findViewById(android.R.id.message);
 		messageView.setLinksClickable(true);
 		messageView.setMovementMethod(LinkMovementMethod.getInstance());
 
 	}
 
 	private void message(String text, String messageId, Locale locale) {
 		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
 		if (tts != null) {
 			HashMap<String, String> params = new HashMap<String, String>();
 
 			params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, messageId);
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
 					WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
 			tts.setLanguage(locale);
 			tts.speak(text, TextToSpeech.QUEUE_ADD, params);
 		} else
 			onSpeechEnded(messageId);
 
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putInt(SCORE, score);
 		outState.putInt(A, a);
 		outState.putInt(B, b);
 		outState.putSerializable(ANSWERS, answers);
 		super.onSaveInstanceState(outState);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == RESULT_SPEECH_CHECK_CODE) {
 			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL) {
 				startActivityForInstall();
 			} else {
 				// success, create the TTS instance
 				tts = new TextToSpeech(this, this);
 
 			}
 		} else if (requestCode == RESULT_SETTINGS) {
 			setDifficulties();
 			generateQuestion();
 		}
 	}
 
 	private void startActivityForInstall() {
 		Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(string.app_name);
 
 		builder.setMessage(getString(R.string.need_mode_components,
 				getNeededLanguages()));
 		builder.setCancelable(true);
 		builder.setIcon(android.R.drawable.ic_dialog_info);
 		builder.setPositiveButton(android.R.string.ok,
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						// missing data, install it
 						Intent installIntent = new Intent();
 						installIntent
 								.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
 						startActivity(installIntent);
 						finish();
 						dialog.cancel();
 
 					}
 				});
 		AlertDialog dialog = builder.create();
 		dialog.show();
 	}
 
 	private String getNeededLanguages() {
 		StringBuilder sb = new StringBuilder();
 		String displayLanguage = getCurrentLocale().getDisplayLanguage();
 		sb.append(displayLanguage);
 		String displayLanguage2 = getCurrentJokeMessageLocale()
 				.getDisplayLanguage();
 
 		if (!displayLanguage2.equals(displayLanguage))
 			sb.append(", ");
 		sb.append(displayLanguage2);
 		return sb.toString();
 	}
 
 	private void setDifficulties() {
 		SharedPreferences sharedPrefs = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		tables = new ArrayList<Integer>();
 		maxButtons = Integer.parseInt(sharedPrefs.getString("answer_number",
 				"4"));
 		for (int i = 1; i < 11; i++)
 			if (sharedPrefs.getBoolean(i + "_checkbox", true))
 				tables.add(i);
 	}
 
 	private void generateQuestion() {
 		if (tables.size() == 0) {
 			Toast.makeText(this, R.string.select_table, Toast.LENGTH_LONG)
 					.show();
 			Intent intent = new Intent(this, SettingsActivity.class);
 			startActivityForResult(intent, RESULT_SETTINGS);
 			return;
 		}
 		int index = random.nextInt(tables.size());
 		a = tables.get(index);
 		b = random.nextInt(10) + 1;
 		String question = setQuestionText();
 
 		if (tts != null) {
 			tts.setLanguage(getCurrentLocale());
 			tts.speak(question, TextToSpeech.QUEUE_ADD, null);
 		}
 
 		generateAnswers();
 		generateButtons();
 	}
 
 	private String setQuestionText() {
 		TextView textQuestion = (TextView) findViewById(R.id.textViewQuestion);
 		String question = a + " " + getString(R.string.times) + " " + b + " ?";
 		textQuestion.setText(question);
 		return question;
 	}
 
 	private void generateAnswers() {
 		int rightIdx = random.nextInt(maxButtons);
 
 		List<Integer> numbers = new ArrayList<Integer>();
 		numbers.add(a * b);
 		answers.clear();
 		for (int i = 0; i < maxButtons; i++) {
 			boolean isRight = i == rightIdx;
 			answers.add(new Answer(i == rightIdx, isRight
 					? a * b
 					: getWrongAnswer(numbers)));
 		}
 	}
 
 	private void generateButtons() {
 		PredicateLayout rl = (PredicateLayout) findViewById(R.id.answerContainer);
 		for (Button b : buttons)
 			rl.removeView(b);
 
 		for (int i = 0; i < answers.size(); i++) {
 			Answer answer = answers.get(i);
 			Button btn = new Button(this);
 			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
 					LayoutParams.WRAP_CONTENT);
 
 			btn.setText(answer.getResponse().toString());
 
 			rl.addView(btn, lp);
 			btn.setOnClickListener(answer.isCorrect()
 					? yesClickListener
 					: noClickListener);
 			buttons.add(btn);
 		}
 	}
 
 	private int getWrongAnswer(List<Integer> answers) {
 
 		int answer = 0;
 		do {
 			switch (random.nextInt(3)) {
 				case 0 :
 					answer = a * b + random.nextInt(20) - 10;
 					break;
 				case 1 :
 					answer = (a + random.nextInt(4) - 2) * b;
 					break;
 				case 2 :
 					answer = (b + random.nextInt(4) - 2) * a;
 					break;
 			}
 			answer = Math.max(1, answer);
 			answer = Math.min(100, answer);
 		} while (answers.contains(answer));
 		answers.add(answer);
 		return answer;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public void onInit(int status) {
 		// TODO Auto-generated method stub
 		// TTS is successfully initialized
 		if (status == TextToSpeech.SUCCESS) {
 			// Setting speech language
 			Locale current = getCurrentLocale();
 			if (tts.isLanguageAvailable(current) < TextToSpeech.LANG_AVAILABLE
 					|| tts.isLanguageAvailable(getCurrentJokeMessageLocale()) < TextToSpeech.LANG_AVAILABLE) {
 				startActivityForInstall();
 				return;
 			}
 			int result = tts.setLanguage(current);
 			tts.setPitch(1.9f);
 			tts.setSpeechRate(1.1f);
 			tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
 
 				@Override
 				public void onUtteranceCompleted(String utteranceId) {
 					final String id = utteranceId;
 					runOnUiThread(new Runnable() {
 
 						@Override
 						public void run() {
 							onSpeechEnded(id);
 						}
 
 					});
 
 				}
 
 			});
 			// If your device doesn't support language you set above
 			if (result == TextToSpeech.LANG_MISSING_DATA
 					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
 				// Cook simple toast message with message
 				Toast.makeText(this, R.string.language_not_supported,
 						Toast.LENGTH_LONG).show();
 				Log.e(TTS, getString(R.string.language_not_supported));
 			}
 			if (!restoredFromInstanceState) {
 				tts.speak(getString(R.string.welcome_message),
 						TextToSpeech.QUEUE_ADD, null);
 			}
 
 			// TTS is not initialized properly
 		} else {
 			Toast.makeText(this, R.string.tts_initilization_failed,
 					Toast.LENGTH_LONG).show();
 			Log.e(TTS, getString(R.string.tts_initilization_failed));
 		}
 		if (!restoredFromInstanceState) {
 			generateQuestion();
 		}
 
 	}
 
 	private Locale getCurrentLocale() {
 		String locale = getString(R.string.speech_locale);
 		return new Locale(locale);
 	}
 	private Locale getCurrentJokeMessageLocale() {
 		String locale = getString(R.string.speech_joke_message_locale);
 		return new Locale(locale);
 	}
 	private void onSpeechEnded(String utteranceId) {
 		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
 		if (utteranceId.equals(OK))
 			generateQuestion();
 
 	}
 
 	@Override
 	protected void onPause() {
 		if (tts != null) {
 			tts.stop();
 		}
 		super.onPause();
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (tts != null) {
 			tts.stop();
 		}
 		super.onDestroy();
 	}
 }
