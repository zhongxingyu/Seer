 package com.example.multithread;
 
 import java.math.BigInteger;
 
 import com.example.algorithms.FactorizationAlgo;
 import com.example.algorithms.FermatMethod;
 import com.example.algorithms.PollardRo;
 import com.example.algorithms.PrimitiveDivision;
 
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
import android.util.Log;
 import android.view.Menu;
 import android.view.View.OnClickListener;
 import android.view.View;
 import android.view.Window;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.Spinner;
 
 /**
  * Class implements main activity which will be running when the application
  * starts
  */
 
 public class MainActivity extends Activity implements OnClickListener {
 
 	/**
 	 * Array of input boxes which store the numbers to be factored
 	 */
 	private EditText inputs[];
 
 	/**
 	 * Array of spinners which store the selections of the algorithms to be
 	 * used. algorithmsSelections[i] contains the algorithm id which will be
 	 * used to factor the number stored in inputs[i]
 	 */
 	private Spinner algorithmSelections[];
 
 	/**
 	 * Array of progress bars ProgressBar[i] is active if factorization of the
 	 * number inputs[i] is active
 	 */
 	boolean resetPressed=false;
 	boolean exitPressed=false;
 	private ProgressBar progressBars[];
 
 	/** Called when the activity is first created. */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_main);
 
 		// set up listeners:
 		View reset_button = findViewById(R.id.reset_button);
 		reset_button.setOnClickListener(this);
 
 		View exit_button = findViewById(R.id.exit_button);
 		exit_button.setOnClickListener(this);
 
 		View submit_button = findViewById(R.id.submit_button);
 		submit_button.setOnClickListener(this);
 
 		// initialize array of inputs (numbers to be factored)
 		inputs = new EditText[] { (EditText) findViewById(R.id.number_1),
 				(EditText) findViewById(R.id.number_2),
 				(EditText) findViewById(R.id.number_3),
 				(EditText) findViewById(R.id.number_4),
 				(EditText) findViewById(R.id.number_5) };
 
 		// initialize array of the spinners containing algorithm selections:
 		algorithmSelections = new Spinner[] {
 				(Spinner) findViewById(R.id.algorithm_1),
 				(Spinner) findViewById(R.id.algorithm_2),
 				(Spinner) findViewById(R.id.algorithm_3),
 				(Spinner) findViewById(R.id.algorithm_4),
 				(Spinner) findViewById(R.id.algorithm_5), };
 
 		// initialize array of progessBars:
 		
 		progressBars = new ProgressBar[] {
 				(ProgressBar) findViewById(R.id.pb_1),
 				(ProgressBar) findViewById(R.id.pb_2),
 				(ProgressBar) findViewById(R.id.pb_3),
 				(ProgressBar) findViewById(R.id.pb_4),
 				(ProgressBar) findViewById(R.id.pb_5) };
 		for (ProgressBar progressBar : progressBars) {
 			progressBar.setVisibility(View.INVISIBLE);
 		}
 		
 	}
 
 	private FactorizationAlgo getAlgorithmById(int id) {
 		switch (id) {
 		case 0:
 			return new PrimitiveDivision();
 		case 1:
 			return new PollardRo();
 		case 2:
 			return new FermatMethod();
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	FactorizationTask[] factorizationTasks;
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 
 		switch (v.getId()) {
 
 		case R.id.exit_button:
 			//System.exit(0);
 			exitPressed=true;
 			MainActivity.this.finish();
 			stopTasks();
 			break;
 
 		case R.id.reset_button:
 			// clear edit boxes
 			resetPressed=true;
 			for (EditText input : inputs) {
 				input.setText("");
 			}
 
 			// clear algorithm selections
 			for (Spinner algorithmSelection : algorithmSelections) {
 				algorithmSelection.setSelection(0);
 			}
 			
 			//System.exit(0);
 			
 			stopTasks();
 			
 			break;
 
 		case R.id.submit_button:
 			resetPressed=false;
 			final Intent final_results_intent = new Intent();
 			final_results_intent.setClass(MainActivity.this,
 					ResultDisplayActivity.class);
 
 			final String[] results = new String[inputs.length];
 			factorizationTasks = new FactorizationTask[inputs.length];
 			final TaskCounter taskCounter = new TaskCounter(inputs.length);
 			try {
 				final BigInteger[] integerInputs = parseInputs();
 				for (int i = 0; i < inputs.length; i++) {
 					final int index = i;
 					final int algorithmId = algorithmSelections[i]
 							.getSelectedItemPosition();
 					final ProgressBar currentProgressBar = progressBars[i];
 					currentProgressBar.setVisibility(View.VISIBLE);
 					factorizationTasks[i] = new FactorizationTask(
 							new OnFactorizationTaskCompleteListener() {
 								@Override
 								public void onTaskComplete(String result) {
 									results[index] = result;
 
 									runOnUiThread(new Runnable() {
 										public void run() {
 											currentProgressBar.setVisibility(View.INVISIBLE);
 											synchronized (taskCounter) {
 												if (taskCounter
 														.getRecievedTasksCount() == inputs.length
 														&& taskCounter
 																.getRunningTasksCount() == 0) {
 													for (int i = 0; i < inputs.length; i++) {
 														final_results_intent
 																.putExtra(
 																		CommunicationConstants.extraDescriptions[i],
 																		results[i] == null ? "Time Out"
 																				: results[i]);
 													}
 													if(!resetPressed && !exitPressed)
 													   startActivity(final_results_intent);
 												}
 
 											}
 
 										}
 									});
 
 								}
 							}, taskCounter);
 					Pair<BigInteger, FactorizationAlgo> taskParams = new Pair<BigInteger, FactorizationAlgo>(
 							integerInputs[i], getAlgorithmById(algorithmId));
 
 					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 						factorizationTasks[i].executeOnExecutor(
 								AsyncTask.THREAD_POOL_EXECUTOR, taskParams);
 					} else {
 						factorizationTasks[i].execute(taskParams);
 					}
 
 					Handler handler = new Handler();
 					handler.postDelayed(new Runnable() {
 						@Override
 						public void run() {
 							if (factorizationTasks[index].getStatus() == AsyncTask.Status.RUNNING)
 								factorizationTasks[index].completeTask(false);
 							runOnUiThread(new Runnable() {
 								public void run() {
 									currentProgressBar.setVisibility(View.INVISIBLE);
 								}
 							});
 						}
 					}, 30000);
 
 				}
 			} catch (NumberFormatException ex) {
 				showAlertDialog("You should input numbers into all the edit boxes");
 			}
 		}
 
 	}
 
 	private void stopTasks() {
 		for (ProgressBar progressBar : progressBars) {
 			progressBar.setVisibility(View.INVISIBLE);
 		}
 		
 		for(FactorizationTask task: factorizationTasks) {
 			task.completeTask(true);
 		}
 	}
 
 	private void showAlertDialog(String message) {
 		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
 		dlgAlert.setMessage(message);
 		dlgAlert.setTitle("Factorization");
 		dlgAlert.setPositiveButton("OK", null);
 		dlgAlert.setCancelable(true);
 		dlgAlert.create().show();
 	}
 
 	private BigInteger[] parseInputs() {
 		BigInteger[] integerInputs = new BigInteger[inputs.length];
 		for (int i = 0; i < inputs.length; i++) {
 			integerInputs[i] = new BigInteger(inputs[i].getText().toString());
 		}
 		return integerInputs;
 	}
 }
