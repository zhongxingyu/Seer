 package cs.ttu.vvclass.activities;
 
 import java.math.BigInteger;
 import ttu.cs.vvclass.R;
 import cs.ttu.vvclass.algorithms.FactorizationAlgo;
 import cs.ttu.vvclass.algorithms.FermatMethod;
 import cs.ttu.vvclass.algorithms.PollardRo;
 import cs.ttu.vvclass.algorithms.PrimitiveDivision;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
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
 	private ProgressBar progressBars[];
 	/**
 	 * Flag which becomes true when the reset button is pressed Set back to
 	 * false after submit is clicked
 	 */
 	boolean resetPressed = false;
 	/**
 	 * Flag which becomes true when the exit button is pressed
 	 */
 	boolean exitPressed = false;
 
 	/**
 	 * Timeout used for stopping threads which are running too long
 	 */
 	static private final int timeout=30000;
 	/** Called when the activity is first created. */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// initialize main window
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
 		// all progress bars are hidden initially
 		for (ProgressBar progressBar : progressBars) {
 			progressBar.setVisibility(View.INVISIBLE);
 		}
 
 	}
 
 	/**
 	 * Create algorithm object corresponding to id
 	 * 
 	 * @param id
 	 * @return algorithm object corresponding to given id
 	 */
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
 
 	/**
 	 * There is no menu in current version, but let's leave this initialization
 	 * for future purposes
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/**
 	 * Array of synchronized tasks (threads the possibility of running in
 	 * background, i.e, not conflicting with the main GUI thread).
 	 */
 	FactorizationTask[] factorizationTasks;
 
 	/**
 	 * Click event handler
 	 * 
 	 * @param View
 	 *            contains the id the button which was clicked
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 
 		switch (v.getId()) {
 
 		case R.id.exit_button:
			exitPressed = true;
			// finalize current activity and all the factorization tasks running
			MainActivity.this.finish();
			// Each number which has not been factored yet is being factored
			// in a separate thread, which is going to be stopped now.
			stopTasks();
 			break;
 
 		case R.id.reset_button:
 			resetPressed = true;
 			// clear edit boxes
 			for (EditText input : inputs) {
 				input.setText("");
 			}
 
 			// clear algorithm selections
 			for (Spinner algorithmSelection : algorithmSelections) {
 				algorithmSelection.setSelection(0);
 			}
 			stopTasks();
 			break;
 
 		case R.id.submit_button:
 			resetPressed = false;
 			// create an intend for displaying the results
			final Intent final_results_intent = new Intent();
 			final_results_intent.setClass(MainActivity.this,
 					ResultDisplayActivity.class);
 			final String[] results = new String[inputs.length];
 			factorizationTasks = new FactorizationTask[inputs.length];
 
 			// create the variable shared between all the threads
 			// it contains the number of running threads together with the
 			// number
 			// of threads which were started (and possibly terminated)
 			// the values will be used for synchronization and starting the
 			// activity which displays the results
 			final TaskCounter taskCounter = new TaskCounter();
 
 			// create the factorization tasks(separate threads) for each of the inputs
 			try {
 
 				final BigInteger[] integerInputs = parseInputs();
 				for (int i = 0; i < inputs.length; i++) {
 					final int index = i;// final is needed to pass the number to
 										// a newly
 										// created runnable
 					final int algorithmId = algorithmSelections[i]
 							.getSelectedItemPosition();
 					final ProgressBar currentProgressBar = progressBars[i];
 					currentProgressBar.setVisibility(View.VISIBLE);
 					factorizationTasks[i] = new FactorizationTask(
 					// create the call back which is called when the thread is
 					// complete
 							new OnFactorizationTaskCompleteListener() {
 								@Override
 								public void onTaskComplete(String result) {
 									// store the results
 									results[index] = result;
 
 									runOnUiThread(new Runnable() {
 										public void run() {
 											// hide appropriate progress bar for
 											// the completed thread,
 											currentProgressBar.setVisibility(View.INVISIBLE);
 											// synchronization: show the result
 											// if and only if
 											// all the 5 threads were completed
 											synchronized (taskCounter) {
 												if (taskCounter
 														.getRecievedTasksCount() == inputs.length
 														&& taskCounter
 																.getRunningTasksCount() == 0) {
 													// pass the results to a new activity
 													for (int i = 0; i < inputs.length; i++) {
 														final_results_intent
 																.putExtra(
 																		CommunicationConstants.extraDescriptions[i],
 																		results[i] == null ? "Time Out"
 																				: results[i]);
 													}
 													// if the execution wasn't stopped, show the result:
 													if (!resetPressed
															&& !exitPressed)
 														startActivity(final_results_intent);
 												}
 
 											}
 
 										}
 									});
 
 								}
 							}, taskCounter);
 					// create the parameters of the algorithms: numbers to factor and
 					// selected algorithm
 					Pair<BigInteger, FactorizationAlgo> taskParams = new Pair<BigInteger, FactorizationAlgo>(
 							integerInputs[i], getAlgorithmById(algorithmId));
 
                     // NOW RUN THE THREADS!
 					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 						// execute the threads on the pull executor
 						// to make them all run in parallel:
 						factorizationTasks[i].executeOnExecutor(
 								AsyncTask.THREAD_POOL_EXECUTOR, taskParams);
 					} else {
 						// the parallel version of executing AsyncTasks
 						// was not supported
 						// on the versions released before HONEYCOMP
 						// all we have to do in case we have such a version 
 						// is to run them  in a normal way:
 						factorizationTasks[i].execute(taskParams);
 					}
 
 					//create the handler for stopping threads by timeout
 					Handler handler = new Handler();
 					handler.postDelayed(new Runnable() {
 						@Override
 						public void run() {
 							// stop the thread if it was timed out!
 							if (factorizationTasks[index].getStatus() == AsyncTask.Status.RUNNING)
 								factorizationTasks[index].completeTask();
 							// and hide the corresponding progress bar
 							runOnUiThread(new Runnable() {
 								public void run() {
 									currentProgressBar
 											.setVisibility(View.INVISIBLE);
 								}
 							});
 						}
 					}, timeout);
 
 				}
 			} catch (NumberFormatException ex) {
 				showAlertDialog("You should input numbers into all the edit boxes");
 			}
 		}
 
 	}
 
 	/**
 	 * the method stops running factorization tasks
 	 */
 	private void stopTasks() {
 		// hide progress bars:
 		for (ProgressBar progressBar : progressBars) {
 			progressBar.setVisibility(View.INVISIBLE);
 		}
        // complete the tasks:
 		for (FactorizationTask task : factorizationTasks) {
			task.completeTask();
 		}
 	}
 
 	/**
 	 * Show a dialog (message box)
 	 * @param message the message to be shown
 	 */
 	private void showAlertDialog(String message) {
 		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
 		dlgAlert.setMessage(message);
 		dlgAlert.setTitle("Factorization");
 		dlgAlert.setPositiveButton("OK", null);
 		dlgAlert.setCancelable(true);
 		dlgAlert.create().show();
 	}
 
 	/**
 	 * Parse the input into the edit boxes into the BigInteger array
 	 * @return parsing result
 	 * @throws a number format exception if one of the edit boxes does not contain a number 
 	 */
 	private BigInteger[] parseInputs() throws NumberFormatException {
 		BigInteger[] integerInputs = new BigInteger[inputs.length];
 		for (int i = 0; i < inputs.length; i++) {
 			integerInputs[i] = new BigInteger(inputs[i].getText().toString());
 		}
 		return integerInputs;
 	}
 }
