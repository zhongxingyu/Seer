 package com.example.careapp;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.NavUtils;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.example.careapp.TodoDialog.TodoDialogListener;
 
 public class TodoActivity extends FragmentActivity implements TodoDialogListener {
 
 	ArrayList<HashMap<String, String>> todoMap; //to store hashmap ArrayList from parser
 	TodoAdapter adapter;
 	TodoDialog todoDialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_todo);
 		boolean creationSuccessful = true;
 
 		// create blank file if it doesn't exist
 		boolean todoExists = true;
 		try {
 			FileInputStream todoIn = this.openFileInput("todo.txt");
 		} catch (FileNotFoundException e) {
 			todoExists = false;
 		}
 		if (!todoExists) {
 			try {
 				writeTo("todo.txt", MODE_PRIVATE, "");	
 			} catch (IOException e) {
 				creationSuccessful = false;
 				Toast.makeText(this, "Couldn't create new todo file: " + e.getMessage(), Toast.LENGTH_LONG).show();
 			}
 		}
 
 		boolean readSuccessful = true;
 		if (creationSuccessful) {
 			//read in todo file
 			String todoListStr = "";
 			try {
 				todoListStr = readIn("todo.txt");			
 			} catch (IOException e) {
 				readSuccessful = false;
 				Toast.makeText(this, "Couldn't open todo file:  " + e.getMessage(), Toast.LENGTH_LONG).show();
 			}
 			if (readSuccessful) {
 
 				TodoParser dp = new TodoParser(todoListStr);
 				todoMap = dp.getTodoList();
 				ListView listView = (ListView) findViewById(R.id.todo_listView);				
 				adapter = new TodoAdapter(this, todoMap);	
 
 				listView.setAdapter(adapter);
 				listView.setOnItemClickListener(new OnItemClickListener() {
 
 					@Override
 					public void onItemClick(AdapterView<?> arg0, View view, int pos, long id) {
 						// on click listeners block on text views in listitems keep this function
 						// from receiving clicks
 					}
 
 				});
 			} // end if successful read in of data file
 		} // end if creation of file is successful
 
 		// Show the Up button in the action bar.
 		setupActionBar();
 
 	} //end onCreate
 
 	public void onSubmitChanges(View view) {
 		if (adapter != null) {
 			boolean writeSuccessful = true;
 			int[] deletedPos = adapter.getChecked();
 			ArrayList<HashMap<String, String>> tempList = new ArrayList<HashMap<String, String>>();
 			for (int i = 0; i < todoMap.size(); i++) {
 				tempList.add(todoMap.get(i));
 			}
 			//insert nulls into removed positions, so 
 			//deletedPos refers to the same entry.
 			for (int i = 0; i < deletedPos.length; i++) {
 				tempList.set(deletedPos[i], null);
 			}
 			//remove nulls
 			ArrayList<HashMap<String, String>> finalList = new ArrayList<HashMap<String, String>>();
 			for (int i = 0; i < tempList.size(); i++) {
 				if (tempList.get(i) != null) {
 					finalList.add(tempList.get(i));
 				}
 			}
 
 			//overwrite todo.txt with new todo items and reload
 			try {
 				//assume todo.txt exists because to get to this point onCreate()
 				//must have been called				
 				writeTo("todo.txt", MODE_PRIVATE, generateTodoString(finalList));
 			} catch (IOException e) {
 				writeSuccessful = false;
 				Toast.makeText(this, "Error saving changes.", Toast.LENGTH_LONG).show();
 			}	
 
 			//restart activity
 			if (writeSuccessful) {
 				finish();
 				startActivity(getIntent());
 			}
 		} // end if adapter != null
 	}	
 	
 	public void onAdd(View view) {	
 		
 		todoDialog = new TodoDialog();
 		todoDialog.show(getSupportFragmentManager(), "todoDialog");		
 	}	
 	
 	private String readIn(String internalStorageFileName) throws IOException {
 		String fileString = "";
 		StringBuffer sb = new StringBuffer();
 		FileInputStream fis = this.openFileInput(internalStorageFileName);
 		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 		String line = null;
 		while ((line = br.readLine()) != null) {
 			sb.append(line + "\n");
 		}
 		fileString = sb.toString();
 		br.close();	
 		return fileString;
 	}
 	private void writeTo(String internalStorageFileName, int writeMode, String content) throws IOException {
 		FileOutputStream fos = this.getApplicationContext().openFileOutput(internalStorageFileName, writeMode);
 		DataOutputStream dos = new DataOutputStream(fos);
 		dos.write(content.getBytes());
 		dos.close();	
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.todo, menu);
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
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * when user clicks ok on add dialog
 	 */
 	@Override
 	public void onDialogPositiveClick(DialogFragment dialog) {
 		//set up vars and ensure no null pointers 
 		if (dialog != null) {
 			AlertDialog topDialog = (AlertDialog) dialog.getDialog();
 			View topLayout = topDialog.findViewById(R.id.todoDialog);
 			View datePickerView = null;
 			if (topLayout != null)
 				datePickerView = topLayout.findViewById(R.id.datePicker);
 			if (datePickerView != null) {
 				DatePicker datePicker = (DatePicker) datePickerView;				
 				EditText eventNameEditText = (EditText) this.findViewById(R.id.todo_event_name);
 				String eventName = eventNameEditText.getText().toString();
 				if (eventName == null || eventName.length() == 0) {
 					Toast.makeText(this, "Not adding, blank input.", Toast.LENGTH_LONG).show();
 					
 					
 				//add new entry	
 				} else {
 					int day = datePicker.getDayOfMonth();
 					int month = datePicker.getMonth() + 1;
 					int year = datePicker.getYear();
 					try {
 						TodoParser todoParser = new TodoParser(readIn("todo.txt"));
 						ArrayList<HashMap<String, String>> todoListMap = todoParser.getTodoList();
 						HashMap<String, String> newEntry = new HashMap<String, String>();
 						newEntry.put("name", eventName);
 						//create string
 						if (todoDialog != null && todoDialog.timeSelected() && topLayout.findViewById(R.id.timePicker) != null) {
 							TimePicker timePicker = (TimePicker) topLayout.findViewById(R.id.timePicker);
 							newEntry.put("date", month + "/" + day + "/" + year +  " " + 
 									twentyFour2TwelveHourTime(timePicker.getCurrentHour(), timePicker.getCurrentMinute()));
 						} else {
 							newEntry.put("date", month + "/" + day + "/" + year);
 						}
 						todoListMap.add(newEntry);
 						boolean writeSuccessful = true;
 						try {
 							//assume todo.txt exists because to get to this point onCreate()
 							//must have been called
 							writeTo("todo.txt", MODE_PRIVATE, generateTodoString(sort(todoListMap)));
 
 						} catch (IOException e) {
 							writeSuccessful = false;
 							Toast.makeText(TodoActivity.this, "Error saving changes." + e.getMessage(), Toast.LENGTH_LONG).show();
 						}			
 						//restart activity
 						if (writeSuccessful) {
 							finish();							
 							startActivity(getIntent());
 						}
 					} catch (IOException e) {
 						Toast.makeText(TodoActivity.this, "Error reading file." + e.getMessage(), Toast.LENGTH_LONG).show();
 					}				
 				} //end else
 				
 				
 				
 				
 			}
 		}	
 	}
 
 	private ArrayList<HashMap<String, String>> sort(ArrayList<HashMap<String, String>> todoListMap) throws IOException{
 		ArrayList<SimpleDateFormat> dateFormats = new ArrayList<SimpleDateFormat>();
 
 		dateFormats.add(new SimpleDateFormat("M/d/yyyy h:mm a"));
 		dateFormats.add(new SimpleDateFormat("M/d/yyyy"));
 		
 		ArrayList<HashMap<Integer, Date>> sortMap = new ArrayList<HashMap<Integer, Date>>();
 		try {
 
 			for (int i = 0; i < todoListMap.size(); i++) {
 				int invalidCounter = 0;
 				for (int j = 0; j < dateFormats.size(); j++) {
 					invalidCounter++;
 					try {
 						HashMap<Integer, Date> newItem = new HashMap<Integer, Date>();
 						newItem.put(i, dateFormats.get(j).parse(todoListMap.get(i).get("date")));
 						newItem.put(-1, dateFormats.get(j).parse(todoListMap.get(i).get("date")));
 						sortMap.add(newItem);
 						break;
 					} catch (ParseException e) {
 						if (invalidCounter == dateFormats.size()) {
 							throw new ParseException("Couldn't match date format", i);
 						}
 					}
 				}
 			}
 			boolean flipped = true;
 			while (flipped) {
 				flipped = false;
 				for (int i = 0; i < sortMap.size() - 1; i++) {
 
 					if (sortMap.get(i).get(-1).after(sortMap.get(i + 1).get(-1))) {
 						HashMap<Integer, Date> temp = sortMap.get(i);
 						sortMap.set(i, sortMap.get(i + 1));
 						sortMap.set(i + 1, temp);
 						flipped = true;
 						break;
 					}
 				}
 			}
 		} catch (ParseException e) {
 			throw new IOException("Invalid date format");
 		}
 		ArrayList<HashMap<String, String>> returnMap = new ArrayList<HashMap<String, String>>();
 		for (int i = 0; i < sortMap.size(); i++) {
 			for (int pos : sortMap.get(i).keySet()) {
 				if (pos != -1) {
 					returnMap.add(todoListMap.get(pos));
 				}
 			}
 		}
 		return returnMap;
 	}
 
 	@Override
 	public void onDialogNegativeClick(DialogFragment dialog) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void editTodoItemName(View view, int position) {
 		final int pos = position;
 
 		LayoutInflater inflater = getLayoutInflater();	
 		View conent = inflater.inflate(R.layout.todo_name_edit_dialog, null);
 		final EditText editText = (EditText) conent.findViewById(R.id.todoNameEditText);
 		final String previousName = ((TextView)view).getText().toString();		
 		editText.setText(previousName);
 		AlertDialog.Builder builder = new AlertDialog.Builder(this)
 		.setTitle("Edit")
 		.setView(conent)
 		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				String newName = editText.getText().toString();
 				if (newName != null && newName.length() != 0) {
 					// delete selected entry from todo.txt
 					String todoListStr = "";
 					try {
 						//read in todo.txt
 						todoListStr = readIn("todo.txt");	
 
 						//parse todo list to data structure
 						TodoParser todoParser = new TodoParser(todoListStr);
 						ArrayList<HashMap<String, String>> todoListMap = todoParser.getTodoList();
 						
 						//edit entry in data structure
 						todoListMap.get(pos).put("name", newName);
 
 						//generate string of new todo items
 						String newTodoFile = generateTodoString(todoListMap);	
 
 						boolean writeSuccessful = true;
 						try {
 							//write new list to todo.txt			
 							writeTo("todo.txt", MODE_PRIVATE, newTodoFile);
 
 						} catch (IOException e) {
 							writeSuccessful = false;
 							Toast.makeText(TodoActivity.this, "Error saving changes." + e.getMessage(), Toast.LENGTH_LONG).show();
 						}			
 						//restart activity if successful
 						if (writeSuccessful) {
 							finish();
 							startActivity(getIntent());
 						}
 					} catch (IOException e) {
 						Toast.makeText(TodoActivity.this, "Couldn't open todo file:  " + e.getMessage(), Toast.LENGTH_LONG).show();
 					}
 				} else {
 					Toast.makeText(TodoActivity.this, "Not saving, blank input.", Toast.LENGTH_LONG).show();
 				} //end if-else input string isn't empty
 			}     //end onClick
 		})
 		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				// User cancelled the dialog
 			}
 		});	
 		builder.create().show();
 
 	}
 
 	public void editTodoItemDate(View view, int position) {
 		final int pos = position;
 		
 		// Use the Builder class for convenient dialog construction
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		
 		//Get the layout inflater
 		LayoutInflater inflater = getLayoutInflater();
 		
 		//get initial message
 		View dateContentView = inflater.inflate(R.layout.todo_dialog, null);
 		
 		TextView tv = (TextView) view;
 		String previousDate = tv.getText().toString();
 		DatePicker datePicker = (DatePicker) dateContentView.findViewById(R.id.datePicker);
 		String[] dateSplit = previousDate.split("\\s+");
 		String[] dateCompSplit = dateSplit[0].split("/");
 		int day = Integer.parseInt(dateCompSplit[1]);
 		int month = Integer.parseInt(dateCompSplit[0]) - 1;
 		int year = Integer.parseInt(dateCompSplit[2]);
 		datePicker.updateDate(year, month, day);
 		
 		
 		
 		String initialMsg = "";
 		
 		if (previousDate.contains(":")) {
 			initialMsg = "Date and Time";
 			LinearLayout layout = (LinearLayout) dateContentView;
 			LinearLayout topLevelTimeView = (LinearLayout) inflater.inflate(R.layout.todo_time_select, null);
 			TimePicker timePicker = (TimePicker) topLevelTimeView.findViewById(R.id.timePicker);
 			Pattern p = Pattern.compile("[0-9]+:[0-9]+");
 			Matcher m = p.matcher(previousDate);
 			String time = "";
 			if (m.find()) {
 				time = m.group();
 			}
 			String[] timeSplit = time.split(":");
 			int curHour = Integer.parseInt(timeSplit[0]);
 			int curMin = Integer.parseInt(timeSplit[1]);
 			if (previousDate.contains("pm") && curHour != 12) {
 				curHour = curHour + 12;
 			}
 			if (previousDate.contains("am") && curHour == 12) {
 				curHour = 0;
 			}
 			timePicker.setCurrentHour(curHour);
 			timePicker.setCurrentMinute(curMin);
 			layout.addView(topLevelTimeView);
 		} else {
 			initialMsg = "Date only";
 		}
 
 		// Inflate and set the layout for the dialog
 		// Pass null as the parent view because  its going in the dialog layout
 		builder.setView(dateContentView)
 
 		.setMessage(initialMsg)
 		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				Dialog d = (Dialog) dialog;
 				AlertDialog a = (AlertDialog) d;
 				View topView = a.findViewById(R.id.todoDialog);
 				
 				//determine if date or date and time
 				LinearLayout topViewLinearLayout = null;
 				if (topView != null && topView instanceof LinearLayout) {
 					String itemEntry = "";
 					int month = -1, day = -1, year = -1, hour = -1, minute = -1;
 					topViewLinearLayout = (LinearLayout) topView;
 					if (topViewLinearLayout.findViewById(R.id.datePicker) != null) {
 						
 						DatePicker datePicker = (DatePicker) topViewLinearLayout.findViewById(R.id.datePicker);
 						day = datePicker.getDayOfMonth();
 						month = datePicker.getMonth() + 1;
 						year = datePicker.getYear();
 						
 						
 					}	
 					if (topViewLinearLayout.findViewById(R.id.timePicker) != null) {
 						TimePicker timePicker = (TimePicker) topViewLinearLayout.findViewById(R.id.timePicker);
 						itemEntry = month + "/" + day + "/" + year + " " +
 								twentyFour2TwelveHourTime(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
 						
 					} else {
 						itemEntry = month + "/" + day + "/" + year;
 					}
 					
 					try {
 						//parse todo list to data structure
 						TodoParser todoParser = new TodoParser(readIn("todo.txt"));
 						ArrayList<HashMap<String, String>> todoListMap = todoParser.getTodoList();
 						todoListMap.get(pos).put("date", itemEntry);
 						boolean writeSuccessful = true;				
 						
 						try {
 							//write new list to todo.txt			
 							writeTo("todo.txt", MODE_PRIVATE, generateTodoString(sort(todoListMap)));
 
 						} catch (IOException e) {
 							writeSuccessful = false;
 							Toast.makeText(TodoActivity.this, "Error saving changes." + e.getMessage(), Toast.LENGTH_LONG).show();
 						}			
 						//restart activity if successful
 						if (writeSuccessful) {
 							finish();
 							startActivity(getIntent());
 						}
 					} catch (IOException e) {
 						Toast.makeText(TodoActivity.this, "Couldn't open todo file:  " + e.getMessage(), Toast.LENGTH_LONG).show();
 					}
 				}
 
 			}
 		})
 		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				// User cancelled the dialog
 			}
 		})
 		.setNeutralButton("Time", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				//overridden later
 			}
 		});
 
 		final AlertDialog alertDialog = builder.create();
 		alertDialog.show();
 		
 		Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
 		if (neutralButton != null) {
 			neutralButton.setOnClickListener(new OnClickListener() {				
 				@Override
 				public void onClick(View v) {
 					if (alertDialog.findViewById(R.id.todoTimeSelect) == null) {
 						LinearLayout layout = (LinearLayout) alertDialog.findViewById(R.id.todoDialog);
 						layout.addView(TodoActivity.this.getLayoutInflater().inflate(R.layout.todo_time_select, null));
 						alertDialog.setMessage("Date and Time");
 					} else {
 						LinearLayout layout = (LinearLayout) alertDialog.findViewById(R.id.todoDialog);
 						layout.removeView(alertDialog.findViewById(R.id.todoTimeSelect));	
 						alertDialog.setMessage("Date only");
 					}					
 				}
 			});
 		}
 		
 		
 	}
 
 
 	private String generateTodoString(ArrayList<HashMap<String, String>> todoDataStructure) {
 		
 		StringBuffer newTodoFile = new StringBuffer();
 		for (int i = 0; i < todoDataStructure.size(); i++) {
 			String itemEntry = "<item>\n" +
 									"<name>" + todoDataStructure.get(i).get("name") + "<name>\n" +
 									"<date>" + todoDataStructure.get(i).get("date") + "<date>\n" +
 								"<item>\n";
 			newTodoFile.append(itemEntry);											
 		}
 		return newTodoFile.toString();
 	}
 	private String twentyFour2TwelveHourTime(int hour, int minute) {
 		String amPm = "";
 		if (hour >= 12 && hour <= 23) {
 			amPm = "pm";
 		} else {
 			amPm = "am";
 		}
 		if (hour > 12) {
 			hour = hour - 12;
 		}
 		if (hour == 0) {
 			hour = 12;
 		}
		return hour + ":" + minute + " " + amPm;
 	}
 
 
 
 }
