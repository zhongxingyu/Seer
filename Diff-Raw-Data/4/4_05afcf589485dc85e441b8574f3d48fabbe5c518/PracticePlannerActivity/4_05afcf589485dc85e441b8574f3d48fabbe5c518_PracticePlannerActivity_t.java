 package com.jeremy.basketballclipboard;
 
 import java.util.Date;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.NavUtils;
 import android.text.InputType;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 
 public class PracticePlannerActivity extends Activity {
 	EditText oldStartTime;
 	EditText oldDuration;
 	String strOldDuration;
 	int properFormEntry = 0; // 0 if initial or no drill duration
 								// 1 if drill line filled completely
 								// 2 if drill duration entered incorrectly
 	boolean correctDurationEntry = true;
 	int numTableRows = 0;
 	List<TableRow> tableRows = new ArrayList<TableRow>();
 	FileInputStream fis;
 	// variable needed for opening up saved practices
 	boolean practiceOpened = false;
 	String fileName;
 	int numRows;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		try {
 			super.onCreate(savedInstanceState);
 			setContentView(R.layout.activity_practice_planner);
 			// Show the Up button in the action bar.
 			setupActionBar();
 			
 			/*******Generate the practice opened by the user*******/
 			//get the data sent from the PracticeHubActivity
 			Bundle extras = getIntent().getExtras();
 			if (extras != null) {
 				practiceOpened = extras.getBoolean("isFileOpened");
 				fileName = extras.getString("fileName");
 			}
 			//set the title of the practice
 			String fileNameWithoutExtension;
 			fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
 			EditText practiceTitle = (EditText) findViewById(R.id.practiceTitle);
 			practiceTitle.setText(fileNameWithoutExtension);
 			//check that a practice has actually been opened
 			if (practiceOpened == true) {
 				// open a file stream to read from the file
 				File sdCard = Environment.getExternalStorageDirectory();
 				File directory = new File(sdCard.getAbsolutePath()
 						+ "/BasketballAssistant/Practices/"+fileName);
 				BufferedReader br = new BufferedReader(new FileReader(directory));
 				String strLine = null;
 				//read the first line of the file to know how many rows to 
 				//generate
 				strLine = br.readLine();
 				numRows = Integer.parseInt(strLine);
 				for (int i = 0; i < numRows; i++) {
 					// TODO - code to generate a practice
 					// - generate a row and then its EditText fields and fill
 					// them in one by one.
 					// gets the table layout
 					TableLayout tl = (TableLayout) findViewById(R.id.tableLayout);
 					// assign values to margins of table row
 					int top = 20;
 					int left, right, bottom;
 					left = right = bottom = 0; // set top, right and bottom to 0
 					// creates a new row to be added to the TableView
 					TableRow tr = new TableRow(this);
 					numTableRows++;
 					tableRows.add(tr);
 					// set the table row layout params
 					TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(
 							TableLayout.LayoutParams.WRAP_CONTENT,
 							TableLayout.LayoutParams.WRAP_CONTENT);
 					trParams.setMargins(left, top, right, bottom);
 					tr.setLayoutParams(trParams);
 					
 					/* create a new start time field */
 					EditText newStartTime = new EditText(this);
 					// set the start time field layout params
 					TableRow.LayoutParams startTimeParams = new TableRow.LayoutParams(
 							TableRow.LayoutParams.WRAP_CONTENT,
 							TableRow.LayoutParams.WRAP_CONTENT, 1);
 					startTimeParams.setMargins(5, 5, 0, 0); // left, top, right, bottom
 					tr.setBackgroundColor(Color.parseColor("#FFFFFF"));
 					newStartTime.setLayoutParams(startTimeParams);
 					// set attributes
 					newStartTime.setEms(10);
 					newStartTime.setTextColor(Color.parseColor("#000000"));
 					newStartTime.setHint("Start Time");
 					newStartTime.setRawInputType(InputType.TYPE_CLASS_DATETIME);
 					newStartTime.setTypeface(null, Typeface.BOLD);
					oldStartTime = newStartTime;
 					
 					/* create a new drill field */
 					EditText newDrill = new EditText(this);
 					// set the drill field layout params
 					TableRow.LayoutParams drillParams = new TableRow.LayoutParams(
 							TableRow.LayoutParams.WRAP_CONTENT,
 							TableRow.LayoutParams.WRAP_CONTENT, 1);
 					drillParams.setMargins(10, 5, 0, 0); // left, top, right, bottom
 					newDrill.setLayoutParams(drillParams);
 					// set attributes
 					newDrill.setEms(10);
 					newDrill.setTextColor(Color.parseColor("#000000"));
 					newDrill.setHint("Enter your drill here!");
 					newDrill.setTypeface(null, Typeface.BOLD);
 
 					/* create a new duration field */
 					EditText newDuration = new EditText(this);
 					// set the duration field layout params
 					TableRow.LayoutParams durationParams = new TableRow.LayoutParams(
 							TableRow.LayoutParams.WRAP_CONTENT,
 							TableRow.LayoutParams.WRAP_CONTENT, 1);
 					durationParams.setMargins(10, 5, 5, 0); // left, top, right, bottom
 					newDuration.setLayoutParams(durationParams);
 					// set attributes
 					newDuration.setEms(10);
 					newDuration.setTextColor(Color.parseColor("#000000"));
 					newDuration.setHint("Duration");
 					newDuration.setInputType(InputType.TYPE_CLASS_DATETIME);
 					newDuration.setTypeface(null, Typeface.BOLD);
					oldDuration = newDuration;
 
 					// add the text field to the table row
 					tr.addView(newStartTime);
 					tr.addView(newDrill);
 					tr.addView(newDuration);
 					
 					//add the table row to the table layout
 					tl.addView(tr);
 					
 					//add the text to the EditText fields
 					strLine = br.readLine();
 					int index = strLine.indexOf("$");
 					String startTime = strLine.substring(0, index);
 					strLine = strLine.substring(index+1);
 					index = strLine.indexOf("$");
 					String drill = strLine.substring(0, index);
 					strLine = strLine.substring(index+1);
 					index = strLine.indexOf("$");
 					String duration = strLine.substring(0, index);
 					
 					//set the text of each EditText
 					newStartTime.setText(startTime);
 					newDrill.setText(drill);
 					newDuration.setText(duration);
 				}
 				br.close();
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.practice_planner, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 			// add new practice option to options menu
 		case R.id.action_newPractice:
 			// newPractice();
 			return true;
 			// add save practice to options menu
 		case R.id.action_savePractice:
 			try {
 				save();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@SuppressLint("SimpleDateFormat")
 	private void setNewStartTime(String prevStartTime, EditText startTime)
 			throws ParseException {
 		// set up the Date formatter
 		SimpleDateFormat formatter = new SimpleDateFormat("h:mm");
 		// change prevStartTime from String to Date
 		Date prevStartTimeDate = (Date) formatter.parse(prevStartTime);
 		int prevDurationInt = Integer
 				.parseInt(oldDuration.getText().toString());
 		// instantiate Calendar
 		Calendar cl = new GregorianCalendar();
 		cl.setTime(prevStartTimeDate);
 		// add the duration onto the drill start time
 		cl.add(Calendar.MINUTE, prevDurationInt);
 		Date clDrillTime = cl.getTime();
 		// make the new time a String
 		String newStartTimeStr = formatter.format(clDrillTime);
 		startTime.setText(newStartTimeStr);
 	}
 
 	// adds a table row filled with a start time field, drill field and a
 	// duration field
 	@SuppressLint("SimpleDateFormat")
 	public void addDrill(View view) throws ParseException {
 		// gets the table layout
 		TableLayout tl = (TableLayout) findViewById(R.id.tableLayout);
 		// assign values to margins of table row
 		int top = 20;
 		int left, right, bottom;
 		left = right = bottom = 0; // set top, right and bottom to 0
 		// creates a new row to be added to the TableView
 		TableRow tr = new TableRow(this);
 		numTableRows++;
 		tableRows.add(tr);
 		// set the table row layout params
 		TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(
 				TableLayout.LayoutParams.WRAP_CONTENT,
 				TableLayout.LayoutParams.WRAP_CONTENT);
 		trParams.setMargins(left, top, right, bottom);
 		tr.setLayoutParams(trParams);
 
 		/* create a new start time field */
 		EditText newStartTime = new EditText(this);
 		// set the start time field layout params
 		TableRow.LayoutParams startTimeParams = new TableRow.LayoutParams(
 				TableRow.LayoutParams.WRAP_CONTENT,
 				TableRow.LayoutParams.WRAP_CONTENT, 1);
 		startTimeParams.setMargins(5, 5, 0, 0); // left, top, right, bottom
 		tr.setBackgroundColor(Color.parseColor("#FFFFFF"));
 		newStartTime.setLayoutParams(startTimeParams);
 		// set attributes
 		newStartTime.setEms(10);
 		newStartTime.setTextColor(Color.parseColor("#000000"));
 		newStartTime.setHint("Start Time");
 		newStartTime.setRawInputType(InputType.TYPE_CLASS_DATETIME);
 		newStartTime.setTypeface(null, Typeface.BOLD);
 
 		if (oldStartTime != null) {
 			String strOldStartTime = oldStartTime.getText().toString();
 			strOldDuration = oldDuration.getText().toString();
 			String threeNumRegex = "\\d{3}";
 			String fourNumRegex = "\\d{4}";
 			if (strOldDuration.contains(":") || strOldDuration.contains("/")
 					|| strOldDuration.contains(" ")
 					|| strOldDuration.contains(".")
 					|| strOldDuration.contains("-")) {
 				// create an alert dialog box
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setMessage(
 						"Enter only numbers in the Drill Duration field."
 								+ " Thanks!").setTitle("Warning!");
 				builder.setPositiveButton("OK",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int which) {
 							}
 						});
 				AlertDialog dialog = builder.create();
 				dialog.show();
 				oldDuration.setText("");
 				oldDuration.requestFocus();
 				correctDurationEntry = false;
 				properFormEntry = 2;
 			}
 			if (!(strOldDuration.length() == 0) && correctDurationEntry == true) {
 				if (strOldStartTime.contains(":")) {
 					String prevStartTimeStr = oldStartTime.getText().toString();
 					setNewStartTime(prevStartTimeStr, newStartTime);
 					// replace the "." with a ":"
 				} else if (strOldStartTime.contains(".")) {
 					strOldStartTime = strOldStartTime.replace(".", ":");
 					setNewStartTime(strOldStartTime, newStartTime);
 					oldStartTime.setText(strOldStartTime);
 				}
 				// replace the "/" with a ":"
 				else if (strOldStartTime.contains("/")) {
 					strOldStartTime = strOldStartTime.replace("/", ":");
 					setNewStartTime(strOldStartTime, newStartTime);
 					oldStartTime.setText(strOldStartTime);
 				}
 				// replace the " " with a ":"
 				else if (strOldStartTime.contains(" ")) {
 					strOldStartTime = strOldStartTime.replace(" ", ":");
 					setNewStartTime(strOldStartTime, newStartTime);
 					oldStartTime.setText(strOldStartTime);
 				}
 				// replace the "-" with a ":"
 				else if (strOldStartTime.contains("-")) {
 					strOldStartTime = strOldStartTime.replace("-", ":");
 					setNewStartTime(strOldStartTime, newStartTime);
 					oldStartTime.setText(strOldStartTime);
 				}
 				// accounts for if there is no colon and 3 digits
 				else if (strOldStartTime.matches(threeNumRegex)) {
 					strOldStartTime = strOldStartTime.substring(0, 1)
 							+ ":"
 							+ strOldStartTime.substring(1,
 									strOldStartTime.length());
 					setNewStartTime(strOldStartTime, newStartTime);
 					oldStartTime.setText(strOldStartTime);
 					// accounts for if there is no colon and 4 digits
 				} else if (strOldStartTime.matches(fourNumRegex)) {
 					strOldStartTime = strOldStartTime.substring(0, 2)
 							+ ":"
 							+ strOldStartTime.substring(2,
 									strOldStartTime.length());
 					setNewStartTime(strOldStartTime, newStartTime);
 					oldStartTime.setText(strOldStartTime);
 				}
 				properFormEntry = 1;
 			}
 		}
 		oldStartTime = newStartTime;
 
 		/* create a new drill field */
 		EditText newDrill = new EditText(this);
 		// set the drill field layout params
 		TableRow.LayoutParams drillParams = new TableRow.LayoutParams(
 				TableRow.LayoutParams.WRAP_CONTENT,
 				TableRow.LayoutParams.WRAP_CONTENT, 1);
 		drillParams.setMargins(10, 5, 0, 0); // left, top, right, bottom
 		newDrill.setLayoutParams(drillParams);
 		// set attributes
 		newDrill.setEms(10);
 		newDrill.setTextColor(Color.parseColor("#000000"));
 		newDrill.setHint("Enter your drill here!");
 		newDrill.setTypeface(null, Typeface.BOLD);
 
 		/* create a new duration field */
 		EditText newDuration = new EditText(this);
 		// set the duration field layout params
 		TableRow.LayoutParams durationParams = new TableRow.LayoutParams(
 				TableRow.LayoutParams.WRAP_CONTENT,
 				TableRow.LayoutParams.WRAP_CONTENT, 1);
 		durationParams.setMargins(10, 5, 5, 0); // left, top, right, bottom
 		newDuration.setLayoutParams(durationParams);
 		// set attributes
 		newDuration.setEms(10);
 		newDuration.setTextColor(Color.parseColor("#000000"));
 		newDuration.setHint("Duration");
 		newDuration.setInputType(InputType.TYPE_CLASS_DATETIME);
 		newDuration.setTypeface(null, Typeface.BOLD);
 		// oldDuration = newDuration;
 
 		// add the text field to the table row
 		tr.addView(newStartTime);
 		tr.addView(newDrill);
 		tr.addView(newDuration);
 		// add the table row to the table layout
 		tl.addView(tr);
 
 		// set focus on the newly created start time field
 		if (properFormEntry == 0 || strOldDuration.length() == 0) {
 			newStartTime.requestFocus();
 			properFormEntry++;
 		} else if (properFormEntry == 1) {
 			newDrill.requestFocus();
 		} else if (properFormEntry == 2) {
 			oldDuration.requestFocus();
 		}
 		oldDuration = newDuration;
 		correctDurationEntry = true;
 	}
 
 	public void save() throws IOException {
 		boolean mExternalStorageAvailable = false;
 		boolean mExternalStorageWriteable = false;
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			// We can read and write the media
 			mExternalStorageAvailable = mExternalStorageWriteable = true;
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			// We can only read the media
 			mExternalStorageAvailable = true;
 			mExternalStorageWriteable = false;
 		} else {
 			// Something else is wrong. It may be one of many other states, but
 			// all we need
 			// to know is we can neither read nor write
 			mExternalStorageAvailable = mExternalStorageWriteable = false;
 		}
 
 		if (mExternalStorageAvailable == true
 				&& mExternalStorageWriteable == true) {
 			EditText practiceTitle = (EditText) findViewById(R.id.practiceTitle);
 			// This will get the SD Card directory and create a folder named
 			// BasketballAssistant in it.
 			File sdCard = Environment.getExternalStorageDirectory();
 			File directory = new File(sdCard.getAbsolutePath()
 					+ "/BasketballAssistant/Practices");
 			directory.mkdirs();
 			// Now create the file in the above directory and write the contents
 			// into it
 			File file = new File(directory, practiceTitle.getText().toString()
 					+ ".txt");
 			FileOutputStream fos = new FileOutputStream(file);
 			OutputStreamWriter osw = new OutputStreamWriter(fos);
 			// String fileName = file.getName();
 			if (!file.exists()) {
 				file.createNewFile();
 			}
 			// write the number of TableRows so that we will know how many to
 			// generate
 			osw.write(numTableRows + "\n");
 			for (int i = 0; i < tableRows.size(); i++) {
 				TableRow tableRow = tableRows.get(i);
 				// get each EditText within the TableRow: StartTime, Drill,
 				// Duration
 				EditText startTime = (EditText) tableRow.getChildAt(0);
 				EditText drill = (EditText) tableRow.getChildAt(1);
 				EditText duration = (EditText) tableRow.getChildAt(2);
 				// get the text from each EditText
 				String sTime = startTime.getText().toString();
 				String sDrill = drill.getText().toString();
 				String sDuration = duration.getText().toString();
 				// write the StartTime, Drill and Duration to the new file
 				osw.write(sTime + "$");
 				osw.write(sDrill + "$");
 				osw.write(sDuration + "$\n");
 			}
 			osw.flush();
 			osw.close();
 		}
 	}
 }
