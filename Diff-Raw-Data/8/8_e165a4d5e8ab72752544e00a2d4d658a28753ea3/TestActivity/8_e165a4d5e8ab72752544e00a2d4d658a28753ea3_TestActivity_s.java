 /*
  * Copyright 2012 Alberto Salmerón Moreno
  * 
  * This file is part of SimpleAndroidTest - https://github.com/berti/SimpleAndroidTest
  * 
  * SimpleAndroidTest is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SimpleAndroidTest is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SimpleAndroidTest.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.primoberti.simpleandroidtest;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 /**
  * Activity for performing a test. The questions file is parsed to load all the
  * available questions. Each question is shown with the corresponding options.
  * After selecting an option, the next question is shown. If there are no more
  * questions, the results activity is launched.
  * 
  * @author berti
  */
 public class TestActivity extends Activity {
 
 	/* Private fields ************************** */
 
 	private List<Question> questions;
 
 	private Question question;
 
 	private int currentQuestion = -1;
 
 	private int totalQuestions;
 
 	private int score = 0;
 
 	private int correctAnswers = 0;
 
 	private Random random = new Random();
 
 	/* Public static fields ******************** */
 
 	public final static String EXTRA_TOTAL_QUESTIONS = "EXTRA_NUM_QUESTIONS";
 	public final static String EXTRA_CORRECT_ANSWERS = "EXTRA_CORRECT_ANSWERS";
 	public final static String EXTRA_SCORE = "EXTRA_SCORE";
 
 	/* Private static fields ******************* */
 
 	// TODO use preferences or other sort of configuration
 	private final static boolean SELECT_RANDOM = true;
 	private final static int MAX_QUESTIONS = 2;
 
 	/* Activity methods ************************ */
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.test);
 
 		try {
 			InputStream inputStream = getAssets().open("questions.txt");
 			questions = parseQuestions(inputStream);
 
 			if (MAX_QUESTIONS > 0) {
 				totalQuestions = Math.min(questions.size(), MAX_QUESTIONS);
 			}
 			else {
 				totalQuestions = questions.size();
 			}
 
 			ProgressBar progressBar = (ProgressBar) findViewById(R.id.testProgressBar);
			progressBar.setMax(questions.size());
 		}
 		catch (IOException e) {
 			Log.e("EnglishTest", e.getMessage(), e);
 		}
 
 		nextQuestion();
 	}
 
 	/* Private methods ************************* */
 
 	private List<Question> parseQuestions(InputStream inputStream)
 			throws IOException {
 		List<Question> questions = new LinkedList<Question>();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				inputStream));
 
 		String line = reader.readLine();
 		while (line != null) {
 			Question question = Question.parseQuestion(line);
 			if (question != null) {
 				questions.add(question);
 			}
 			line = reader.readLine();
 		}
 
 		return questions;
 	}
 
 	private void nextQuestion() {
 		currentQuestion++;
 		if (currentQuestion < totalQuestions) {
 			if (SELECT_RANDOM) {
 				question = questions.remove(random.nextInt(questions.size()));
 			}
 			else {
 				question = questions.get(currentQuestion);
 			}
 			showQuestion(question);
 
 			TextView progressLabel = (TextView) findViewById(R.id.testProgressLabel);
 			progressLabel.setText(String.format(
 					getResources().getString(R.string.test_progress),
					currentQuestion + 1, questions.size()));
 
 			ProgressBar progressBar = (ProgressBar) findViewById(R.id.testProgressBar);
 			progressBar.setProgress(currentQuestion + 1);
 		}
 		else {
 			showResults();
 		}
 	}
 
 	private void showResults() {
 		Intent intent = new Intent(this, ResultsActivity.class);
		intent.putExtra(EXTRA_TOTAL_QUESTIONS, questions.size());
 		intent.putExtra(EXTRA_CORRECT_ANSWERS, correctAnswers);
 		intent.putExtra(EXTRA_SCORE, score);
 		startActivity(intent);
 	}
 
 	private void showQuestion(Question question) {
 		TextView phrase = (TextView) findViewById(R.id.phrase);
 		phrase.setText(question.getPhrase());
 
 		OnClickListener listener = new OptionButtonOnClickListener();
 		String[] options = randomizeOptions(question.getOptions());
 
 		setButton(R.id.buttonOptionA, options[0], listener);
 		setButton(R.id.buttonOptionB, options[1], listener);
 		setButton(R.id.buttonOptionC, options[2], listener);
 		setButton(R.id.buttonOptionD, options[3], listener);
 	}
 
 	private void setButton(int id, String text, OnClickListener listener) {
 		Button button = (Button) findViewById(id);
 		button.setText(text);
 		button.setOnClickListener(listener);
 	}
 
 	private String[] randomizeOptions(String[] options) {
 		List<String> remaining = new LinkedList<String>(Arrays.asList(options));
 		List<String> randomized = new LinkedList<String>();
 		Random random = new Random();
 
 		while (remaining.size() > 1) {
 			int pos = random.nextInt(remaining.size());
 			randomized.add(remaining.remove(pos));
 		}
 		randomized.add(remaining.get(0));
 
 		return randomized.toArray(new String[options.length]);
 	}
 
 	/* Private inner classes ******************* */
 
 	private class OptionButtonOnClickListener implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			if (question.isCorrect(((Button) v).getText())) {
 				score++;
 				correctAnswers++;
 			}
 			nextQuestion();
 		}
 
 	}
 
 }
