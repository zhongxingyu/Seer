 package com.example.educationalapp;
 
 import android.os.Bundle;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MainActivity extends Activity  {
 	Button calc1Button;
 	Button calc1InfoButton;
 	Button videosButton;
 	Button quizzesButton;
 	private final Context context = this;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		setupActionBar();
 		
 		ActionBar bar = getActionBar();
 		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#008fd5")));
 		
 		calc1Button = (Button)findViewById(R.id.calc1_button);
 		calc1InfoButton = (Button)findViewById(R.id.calc1_info_button);
 		videosButton = (Button)findViewById(R.id.videos_button);
 		quizzesButton = (Button)findViewById(R.id.quizzes_button);
 
 		videosButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(MainActivity.this, TopicsTabs.class);
 				startActivity(i);
 			}
 		});
 
 		quizzesButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(MainActivity.this, Quiz.class);
 				
 				i.putExtra("quiz", "limitsQuiz.txt");
 				i.putExtra("quiz_description", "This Quiz is on Limits. Pick your answers and hit submit for immediate feedback. You can take the quiz as many times as you like. Good Luck!");
 				
 				startActivity(i);
 			}
 		});
 
 		calc1Button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(MainActivity.this, TopicsTabs.class);
 			
 				startActivity(i);
 			}
 		});
 		
 		calc1InfoButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
 	        	alertBuilder.setTitle("Calculus 1 Topics");
 	        	alertBuilder.setMessage(
 	        			"Calculus: The branch of mathematics studying the rate of change of quantities (which can be interpreted as slopes of curves) and the length, area, and volume of objects.\n\n" +
 	        			"Chain Rule: A formula for the derivative of the composition of two functions in terms of their derivatives.\n\n" +
 	        			"Continuous Function: A function with no jumps, gaps, or undefined points.\n\n" +
 	        			"Critical Point: A point of a functions graph where the derivative is either zero or undefined.\n\n" +
 	        			"Definite Integral:	An integral with upper and lower limits.\n\n" +
 	        			"Derivative: The infinitesimal rate of change in a function with respect to one of its parameters. The derivative is one of the key concepts in calculus.\n\n" +
 	        			"Discontinuity: A point at which a function jumps suddenly in value, blows up, or is undefined. The opposite of continuity.\n\n" +
 	        			"Extreme Value Theorem: The theorem that a continuous function on a closed interval has both a maximum and minimum value.\n\n" +
 	        			"First Derivative Test: A method for determining the maximum and minimum values of a function using its first derivative.\n\n" +
 	        			"Fundamental Theorems of Calculus: Deep results that express definite integrals of continuous functions in terms of antiderivatives.\n\n" +
 	        			"Implicit Differentiation: The procedure of differentiating an implicit equation (one which has not been explicitly solved for one of the variables) with respect to the desired variable, treating other variables as unspecified functions of it.\n\n" +
 	        			"Indefinite Integral: An integral without upper and lower limits.\n\n" +
 	        			"Inflection Point: A point on a curve at which the concavity changes.\n\n" +
 	        			"Integral: A mathematical object that can be interpreted as an area or a generalization of area. Integrals and derivatives are the fundamental objects of calculus.\n\n" +
 	        			"Intermediate Value Theorem: The theorem that if f is continuous on a closed interval [a, b], and c is any number between f(a) and f(b) inclusive, then there is at least one number x in [a, b] such that f(x) = c.\n\n" +
 	        			"Limit: The value a function approaches as the variable approaches some point. If the function is not continuous, the limit could be different from the value of the function at that point.\n\n" +
 	        			"Maximum: The largest value of a set, function, etc.\n\n" +
 	        			"Mean-Value Theorem: The theorem that if f(x) is differentiable on the open interval (a, b) and continuous on the closed interval [a, b], there is at least one point c in (a, b) such that (a - b) f(c) = f(a) - f(b).\n\n" +
 	        			"Minimum: The smallest value of a set, function, etc.\n\n" +
 	        			"Newtons Method: An iterative method for numerically finding a root of a function.\n\n" +
 	        			"Riemann Sum: An estimate, using rectangles, of the area under a curve. An definite integral is defined as a limit of Riemann sums.\n\n" +
 	        			"Second Derivative Test: A method for determining a functions maxima, minima, and points of inflection by using its first and second derivatives.");
 				AlertDialog results = alertBuilder.create();
 				results.show();
 			}
 		});
 	}
 
 	private void setupActionBar() {
         ActionBar ab = getActionBar();
         ab.setDisplayShowCustomEnabled(true);
         ab.setDisplayShowTitleEnabled(false);
  //       ab.setIcon(R.drawable.icon);
         LayoutInflater inflator = (LayoutInflater) this
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View v = inflator.inflate(R.layout.action_bar_layout, null);
 
         ab.setCustomView(v);
 
 //      ab.setDisplayHomeAsUpEnabled(true);
     }
 	
 //	@Override
 //	public boolean onCreateOptionsMenu(Menu menu) {
 //		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.main, menu);
 //		return true;
 //	}
 
 }
