 package org.pepit.p3.maths.additclassique;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class Exercise implements org.pepit.plugin.Interface {
 
 	private ExerciseView view = null;
 
 	/**
 	 * Plugin metadata
 	 */
 	public org.pepit.plugin.Info getInfo() {
 		org.pepit.plugin.Info info = new org.pepit.plugin.Info();
 		info.level = org.pepit.plugin.Level.P3;
 		info.subject = org.pepit.plugin.Subject.MATHEMATICS;
 		info.theme = "Additions classiques";
 		info.version = 1;
 		String pepitPage = "http://www.pepit.be/exercices/primaire3/mathematiques/additclassique/page.html";
 		try {
 			info.pepitPage = new URL(pepitPage);
 		} catch (MalformedURLException e) {
 			Log.e("Pepit", "Bad URL: " + pepitPage);
 		}
 		return (info);
 	}
 
 	/**
 	 * Layout for the exercise screen in master activity
 	 */
 	public LinearLayout getExercisePresentationLayout(Context ctx,
 			File rootResource) {
 		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.MATCH_PARENT);
 		params.gravity = Gravity.CENTER_HORIZONTAL;
 
 		LinearLayout lil = new LinearLayout(ctx);
 		lil.setLayoutParams(params);
 		lil.setOrientation(LinearLayout.VERTICAL);
 		lil.setBackgroundColor(0xFF99CC66);
 
 		TextView tv1 = new TextView(ctx);
 		tv1.setGravity(Gravity.CENTER_HORIZONTAL);
 		tv1.setTextColor(Color.BLACK);
 		tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
 		tv1.setText("ADDITIONS CLASSIQUES");
 		lil.addView(tv1);
 
 		TextView tv2 = new TextView(ctx);
 		tv2.setGravity(Gravity.CENTER_HORIZONTAL);
 		tv2.setTextColor(Color.BLACK);
 		tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
 		tv2.setText("Complétez les additions");
 		lil.addView(tv2);
 
 		return (lil);
 	}
 
 	/**
 	 * Return the list of exercises
 	 */
 	public String[] getExerciseList() {
 		String[] l = { "2 nombres sans report - 1ère partie",
 				"2 nombres sans report - 2ème partie", "2 nombres avec report",
 				"3 nombres sans report", "3 nombres avec report" };
 		return (l);
 	}
 
 	/**
 	 * Layout to explain the exercise in master activity
 	 */
 	public LinearLayout getExplanationPresentationLayout(Context ctx,
 			File rootResource, int selectedExercise) {
 		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.MATCH_PARENT);
 		params.gravity = Gravity.CENTER_HORIZONTAL;
 
 		LinearLayout lil = new LinearLayout(ctx);
 		lil.setLayoutParams(params);
 		lil.setOrientation(LinearLayout.VERTICAL);
 		lil.setBackgroundColor(0xFF99CC66);
 
 		TextView tv1 = new TextView(ctx);
 		tv1.setGravity(Gravity.CENTER_HORIZONTAL);
 		tv1.setTextColor(Color.BLACK);
 		tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
 		tv1.setText("ADDITIONS CLASSIQUES");
 		lil.addView(tv1);
 
 		TextView tv2 = new TextView(ctx);
 		tv2.setGravity(Gravity.CENTER_HORIZONTAL);
 		tv2.setTextColor(Color.BLACK);
 		tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
 		String[] t = this.getExerciseList();
 		tv2.setText("\n" + t[selectedExercise] + "\n");
 		lil.addView(tv2);
 
 		TextView tv3 = new TextView(ctx);
 		tv3.setGravity(Gravity.CENTER_HORIZONTAL);
 		tv3.setTextColor(Color.BLACK);
 		tv3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
 		tv3.setText("Complétez l'opération avec un des nombres");
 		lil.addView(tv3);
 
 		return (lil);
 	}
 
 	/**
 	 * Return the list of modules for an exercise
 	 */
 	public String[] getModuleList(int exercise) {
 		String[] l = { "Module 1", "Module 2", "Module 3", "Module 4",
 				"Module 5" };
 		return (l);
 	}
 
 	/**
 	 * Layout for a question set
 	 */
 	public LinearLayout getQuestionLayout(Context ctx, File rootResource,
 			int selectedExercise, int selectedModule, int numQuestion) {
 		// numQuestion is not used because each question is randomly built
 		int nbNumbers = 0;
 		int nbRows = 0;
 		boolean withCarry = false;
 		switch (selectedExercise) {
 		case 0:
 		case 1:
 			nbNumbers = 2;
 			nbRows = 5;
 			break;
 		case 2:
 			nbNumbers = 2;
			nbRows = 5;
 			withCarry = true;
 			break;
 		case 3:
 			nbNumbers = 3;
			nbRows = 6;
 			break;
 		case 4:
 			nbNumbers = 3;
 			nbRows = 6;
 			withCarry = true;
 			break;
 		default:
 			assert false; // should never reach this line!
 		}
 		ExerciseModel m = null;
 		if (withCarry) {
 			m = new ExerciseModelWithCarry(nbNumbers,
 					ExerciseModel.Operator.PLUS, 4, 999, 1);
 		} else {
 			m = new ExerciseModelWithoutCarry(nbNumbers,
 					ExerciseModel.Operator.PLUS, 4, 999, 1);
 		}
 		m.game();
 		ExerciseView v = new ExerciseView(m, ctx, 4, nbRows);
 		v.init();
 		v.refresh();
 		this.view = v;
 		return (v.getLayout());
 	}
 
 	/**
 	 * The number of questions is the same for every exercises and modules.
 	 */
 	public int getQuestionCount(int selectedExercise, int selectedModule) {
 		return (5);
 	}
 
 	/**
 	 * This plugin doesn't need to do something at the start/end of the
 	 * questions.
 	 */
 	public void startQuestionSequence() {
 	}
 
 	public void finishQuestionSequence() {
 	}
 
 	/**
 	 * Text displayed in the button for the next question.
 	 */
 	public String getNextQuestionButtonText() {
 		return ("QUESTION SUIVANTE");
 	}
 
 	/**
 	 * Check if the current answer is the right one.
 	 */
 	public boolean currentAnswerIsRight() {
 		boolean r = this.view.getModel().checkAnswerComplete();
 		return (r);
 	}
 
 	/**
 	 * Ask the plugin to show to the user that her current answer is right.
 	 */
 	public void showAnswerIsRight() {
 		// update the number of right answer
 		this.view.getModel().incNbRightAnswer();
 		// and display the message
 		this.view.displayRightAnswer();
 	}
 
 	/**
 	 * Ask the plugin to show to the user that her current answer is wrong.
 	 */
 	public void showAnswerIsWrong() {
 		this.view.displayWrongAnswer();
 	}
 
 	/**
 	 * Return the global score
 	 */
 	public int getScore() {
 		// 1 right answer = 4 points
 		// because we have (5 exercises * 5 modules) / 100 max points = 4
 		int score = this.view.getModel().getNbRightAnswer() * 4;
 		return (score);
 	}
 
 }
