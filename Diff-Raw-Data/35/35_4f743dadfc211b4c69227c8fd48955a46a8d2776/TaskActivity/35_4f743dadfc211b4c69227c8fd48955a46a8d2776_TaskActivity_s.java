 package com.whatstodo.activities;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.TimePickerDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.text.format.DateFormat;
 import android.util.TypedValue;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.whatstodo.R;
 import com.whatstodo.models.List;
 import com.whatstodo.models.ListContainer;
 import com.whatstodo.models.Task;
 import com.whatstodo.utils.Priority;
 
 public class TaskActivity extends FragmentActivity implements OnClickListener,
 		AddressDialogFragment.NoticeDialogListener {
 
 	private ListContainer container;
 	private List list;
 	private List userList;
 	private Task task;
 	private Priority userPriority;
 	private String userNotice;
 	private Date userDate;
 	private Date userReminder;
 	private String userAddress;
 
 	private static final int DATE_DIALOG_ID = 0;
 	private static final int REMINDER_DATE_DIALOG_ID = 1;
 	private static final int REMINDER_TIME_DIALOG_ID = 2;
 	private static final int LIST_DIALOG_ID = 3;
 
 	// private static final int ADDRESS_DIALOG_ID = 4;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_task);
 
 		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 		Bundle bundle = getIntent().getExtras();
 
 		nm.cancel((int) bundle.getLong("TaskId"));
 
 		container = ListContainer.getInstance();
		list = container.getList(bundle.getLong("ListId"));
		task = list.getTask(bundle.getLong("TaskId"));
 		userPriority = task.getPriority();
 		userNotice = (String) task.getNotice();
 		userDate = task.getDate();
 		userReminder = task.getReminder();
 		userList = list;
 		userAddress = task.getAddress();
 
 		setTitle("WhatsToDo");
 
 		Button save = (Button) findViewById(R.id.taskSave);
 		save.setOnClickListener(this);
 
 		Button cancel = (Button) findViewById(R.id.taskCancel);
 		cancel.setOnClickListener(this);
 
 		EditText editText = (EditText) findViewById(R.id.taskName);
 		editText.setText(task.getName());
 
 		CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox1);
 		checkBox.setChecked(task.isDone());
 
 		showNotice(task.getNotice());
 		FrameLayout editNotice = (FrameLayout) findViewById(R.id.taskNotice);
 		editNotice.setOnClickListener(this);
 
 		showDate(task.getDate());
 		FrameLayout editDate = (FrameLayout) findViewById(R.id.taskDate);
 		editDate.setOnClickListener(this);
 
 		showReminder(task.getReminder());
 		FrameLayout editReminder = (FrameLayout) findViewById(R.id.taskReminder);
 		editReminder.setOnClickListener(this);
 
 		showList(list.getName());
 		FrameLayout editList = (FrameLayout) findViewById(R.id.taskList);
 		editList.setOnClickListener(this);
 
 		showPriority(task.getPriority().toString());
 		FrameLayout editPriority = (FrameLayout) findViewById(R.id.taskPriority);
 		editPriority.setOnClickListener(this);
 
 		showAddress(task.getAddress() == null ? "" : task.getAddress()
 				.toString());
 		FrameLayout editAddress = (FrameLayout) findViewById(R.id.taskAddress);
 		editAddress.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View view) {
 
 		switch (view.getId()) {
 
 		case R.id.taskSave:
 			EditText editText = (EditText) findViewById(R.id.taskName);
 			task.setName(editText.getText().toString());
 			task.setDone(getCheckBox());
 			task.setNotice(userNotice);
 			task.setPriority(userPriority);
 			task.setDate(userDate);
 			task.setReminder(userReminder);
 			task.setAddress(userAddress);
 
 			if (list != userList) {
 				userList.add(task);
 				list.remove(task);
 				list = userList;
 			}
 
 			// startListActivity();
 			setResult(Activity.RESULT_OK);
 			finish();
 
 			break;
 
 		case R.id.taskCancel:
 			// startListActivity();
 			setResult(Activity.RESULT_CANCELED);
 			finish();
 			break;
 
 		case R.id.taskNotice:
 			changeNotice();
 			break;
 
 		case R.id.taskPriority:
 			changePriority();
 			break;
 
 		case R.id.taskDate:
 			changeDate();
 			break;
 
 		case R.id.taskReminder:
 			changeReminder();
 			break;
 
 		case R.id.taskList:
 			changeList();
 			break;
 		case R.id.taskAddress:
 			changeAddress();
 			break;
 		}
 	}
 
 	private boolean getCheckBox() {
 		CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox1);
 		if (checkBox.isChecked()) {
 			return true;
 		}
 		return false;
 	}
 
 	private void changeNotice() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Notiz");
 		builder.setMessage(userNotice);
 
 		final EditText input = new EditText(this);
 		builder.setView(input);
 
 		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 
 				userNotice = input.getText().toString();
 				TextView taskNotice = (TextView) findViewById(R.id.textViewNotice);
 				if (userNotice != null) {
 					taskNotice.setText(userNotice);
 				} else {
 					taskNotice.setText("Keine Notiz");
 				}
 			}
 		});
 
 		builder.setNegativeButton("Abbrechen",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 					}
 				});
 
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	private void changePriority() {
 		switch (userPriority) {
 		case LOW:
 			userPriority = Priority.NORMAL;
 			break;
 		case NORMAL:
 			userPriority = Priority.HIGH;
 			break;
 		case HIGH:
 			userPriority = Priority.LOW;
 			break;
 		default:
 			Toast.makeText(getBaseContext(), "Priority not found",
 					Toast.LENGTH_LONG).show();
 		}
 		showPriority(userPriority.toString());
 	}
 
 	private void changeDate() {
 		showDialog(DATE_DIALOG_ID);
 	}
 
 	private void changeReminder() {
 		showDialog(REMINDER_DATE_DIALOG_ID);
 	}
 
 	private void changeList() {
 		showDialog(LIST_DIALOG_ID);
 	}
 
 	private void changeAddress() {
 		AddressDialogFragment dialogFragment = new AddressDialogFragment();
 		dialogFragment.show(getSupportFragmentManager(), "??");
 		dialogFragment.setAddress(task.getAddress());
 	}
 
 	// private void startListActivity() {
 	// Intent intent = new Intent(this, ListActivity.class);
 	// intent.putExtras(getBundle()); // Put your id to your next Intent
 	// startActivity(intent);
 	// }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_task, menu);
 		return true;
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 
 		switch (id) {
 		case DATE_DIALOG_ID:
 			DatePickerDialog dateDialog = new DatePickerDialog(this,
 					new DatePickerDialog.OnDateSetListener() {
 						public void onDateSet(DatePicker view, int year,
 								int monthOfYear, int dayOfMonth) {
 
 							Calendar cal = Calendar.getInstance();
 							cal.set(year, monthOfYear, dayOfMonth);
 							userDate = cal.getTime();
 							showDate(userDate);
 						}
 					}, 2011, 0, 1);
 
 			dateDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Ohne Datum",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							if (which == DialogInterface.BUTTON_NEUTRAL) {
 								userDate = null;
 								showDate(userDate);
 								dialog.dismiss();
 							}
 						}
 					});
 
 			dateDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Einstellen",
 					dateDialog);
 			dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Abbrechen",
 					dateDialog);
 			dateDialog.setMessage("Datum der Aufgabe");
 			return dateDialog;
 
 		case REMINDER_DATE_DIALOG_ID:
 			final DatePickerDialog remDateDialog = new DatePickerDialog(this,
 					new DatePickerDialog.OnDateSetListener() {
 						public void onDateSet(DatePicker view, int year,
 								int monthOfYear, int dayOfMonth) {
 
 							Calendar calendar = Calendar.getInstance();
 							calendar.set(year, monthOfYear, dayOfMonth);
 							userReminder = calendar.getTime();
 							showDialog(REMINDER_TIME_DIALOG_ID);
 						}
 					}, 2011, 0, 1);
 
 			remDateDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
 					"Ohne Erinnerung", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							if (which == DialogInterface.BUTTON_NEUTRAL) {
 								cancelReminderAlarm((int) task.getId());
 								userReminder = null;
 								showReminder(userReminder);
 								dialog.dismiss();
 							}
 						}
 					});
 
 			remDateDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
 					"Abbrechen", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							if (which == DialogInterface.BUTTON_NEGATIVE) {
 								dialog.dismiss();
 							}
 						}
 					});
 
 			remDateDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Zeit",
 					remDateDialog);
 			remDateDialog.setMessage("Datum der Erinnerung:");
 			
 			remDateDialog.setOnShowListener(new DialogInterface.OnShowListener() {
 
 			    @Override
 			    public void onShow(DialogInterface dialog) {
 
 			        float textSize = 14.0f;
 
 			        Button positive = remDateDialog.getButton(AlertDialog.BUTTON_POSITIVE);
 			        positive.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
 
 			        Button neutral = remDateDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
 			        neutral.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
 
 			        Button negative = remDateDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
 			        negative.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
 			    }
 			});
 			
 			return remDateDialog;
 
 		case REMINDER_TIME_DIALOG_ID:
 			final TimePickerDialog remTimeDialog = new TimePickerDialog(this,
 					new TimePickerDialog.OnTimeSetListener() {
 
 						@Override
 						public void onTimeSet(TimePicker view, int hourOfDay,
 								int minute) {
 
 							userReminder.setHours(hourOfDay);
 							userReminder.setMinutes(minute);
 							userReminder.setSeconds(0);
 							showReminder(userReminder);
 							Calendar calendar = Calendar.getInstance();
 							if (calendar.getTimeInMillis() < userReminder
 									.getTime()) {
 								setReminderAlarm(userReminder);
 							}
 						}
 					}, 12, 0, true);
 
 			remTimeDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
 					"Ohne Erinnerung", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							if (which == DialogInterface.BUTTON_NEUTRAL) {
 								cancelReminderAlarm((int) task.getId());
 								userReminder = null;
 								showReminder(userReminder);
 								dialog.dismiss();
 							}
 						}
 					});
 
 			remTimeDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
 					"Abbrechen", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							if (which == DialogInterface.BUTTON_NEGATIVE) {
 								dialog.dismiss();
 							}
 						}
 					});
 
 			remTimeDialog.setButton(DialogInterface.BUTTON_POSITIVE,
 					"Einstellen", remTimeDialog);
 			remTimeDialog.setMessage("Zeit der Erinnerung: ");
 			
 			remTimeDialog.setOnShowListener(new DialogInterface.OnShowListener() {
 
 			    @Override
 			    public void onShow(DialogInterface dialog) {
 
 			        float textSize = 14.0f;
 
 			        Button positive = remTimeDialog.getButton(AlertDialog.BUTTON_POSITIVE);
 			        positive.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
 
 			        Button neutral = remTimeDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
 			        neutral.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
 
 			        Button negative = remTimeDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
 			        negative.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
 			    }
 			});
 			return remTimeDialog;
 
 		case LIST_DIALOG_ID:
 			final String[] listNames = new String[container.getLists().size()];
 			int listPos = 0,
 			i = 0;
 			for (List lists : container.getLists()) {
 				if (lists.getId() == list.getId()) {
 					listPos = i;
 				}
 				listNames[i++] = lists.getName();
 			}
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Wähle Liste");
 			builder.setSingleChoiceItems(listNames, listPos,
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 						}
 					});
 
 			builder.setPositiveButton("Ok",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							if (which == DialogInterface.BUTTON_POSITIVE) {
 								int checkedPosition = ((AlertDialog) dialog)
 										.getListView().getCheckedItemPosition();
 								userList = container
 										.getList(listNames[checkedPosition]
 												.toString());
 								showList(userList.getName());
 								dialog.dismiss();
 							}
 						}
 					});
 
 			builder.setNegativeButton("Abbrechen",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							if (which == DialogInterface.BUTTON_POSITIVE) {
 								dialog.dismiss();
 							}
 						}
 					});
 			return builder.create();
 
 			// case ADDRESS_DIALOG_ID:
 			// return new AddressDialogFragment().onCreateDialog(null);
 
 		}
 		return null;
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 
 		super.onPrepareDialog(id, dialog);
 		Calendar calendar = Calendar.getInstance();
 
 		switch (id) {
 		case DATE_DIALOG_ID:
 			DatePickerDialog dateDialog = (DatePickerDialog) dialog;
 			int day,
 			month,
 			year;
 
 			if (task.getDate() != null) {
 				day = task.getDate().getDate();
 				month = task.getDate().getMonth();
 				year = task.getDate().getYear() + 1900;
 			} else {
 				day = calendar.get(Calendar.DAY_OF_MONTH);
 				month = calendar.get(Calendar.MONTH);
 				year = calendar.get(Calendar.YEAR);
 			}
 			dateDialog.updateDate(year, month, day);
 			break;
 
 		case REMINDER_DATE_DIALOG_ID:
 			DatePickerDialog reminderDateDialog = (DatePickerDialog) dialog;
 			int remDay,
 			remMonth,
 			remYear;
 
 			if (task.getReminder() != null) {
 				remDay = task.getReminder().getDate();
 				remMonth = task.getReminder().getMonth();
 				remYear = task.getReminder().getYear() + 1900;
 			} else {
 				remDay = calendar.get(Calendar.DAY_OF_MONTH);
 				remMonth = calendar.get(Calendar.MONTH);
 				remYear = calendar.get(Calendar.YEAR);
 			}
 			reminderDateDialog.updateDate(remYear, remMonth, remDay);
 			break;
 
 		case REMINDER_TIME_DIALOG_ID:
 			TimePickerDialog reminderTimeDialog = (TimePickerDialog) dialog;
 			int remHour,
 			remMinute;
 			if (task.getReminder() != null) {
 				remHour = task.getReminder().getHours();
 				remMinute = task.getReminder().getMinutes() + 1;
 			} else {
 				remHour = calendar.get(Calendar.HOUR_OF_DAY);
 				remMinute = calendar.get(Calendar.MINUTE);
 			}
 			reminderTimeDialog.updateTime(remHour, remMinute);
 			break;
 		}
 	}
 
 	private void showNotice(String string) {
 		TextView taskNotice = (TextView) findViewById(R.id.textViewNotice);
 		if (string != null) {
 			taskNotice.setText(string);
 		} else {
 			taskNotice.setText("Keine Notiz");
 		}
 	}
 
 	private void showDate(Date date) {
 		TextView taskDate = (TextView) findViewById(R.id.textViewDate);
 		if (date != null) {
 			String stringDate = DateFormat.format("dd.MM.yyyy", date)
 					.toString();
 			taskDate.setText(stringDate);
 		} else {
 			taskDate.setText("Ohne Datum");
 		}
 	}
 
 	private void showReminder(Date date) {
 		TextView taskReminder = (TextView) findViewById(R.id.textViewReminder);
 		if (date != null) {
 			String stringDate = DateFormat.format("kk:mm dd.MM.yyyy", date)
 					.toString();
 			taskReminder.setText(stringDate);
 		} else {
 			taskReminder.setText("Keine Erinnerung");
 		}
 	}
 
 	private void showList(String name) {
 		TextView taskList = (TextView) findViewById(R.id.textViewList);
 		taskList.setText(name);
 	}
 
 	private void showPriority(String priority) {
 		TextView taskPriority = (TextView) findViewById(R.id.textViewPriority);
 		taskPriority.setText(priority);
 	}
 
 	private void showAddress(String address) {
 		TextView taskAddress = (TextView) findViewById(R.id.textViewAdress);
 		taskAddress.setText(address);
 	}
 
 	private void setReminderAlarm(Date date) {
 
 		Intent intent = new Intent(this, MyAlarmService.class);
 		intent.putExtras(getBundle());
 		PendingIntent pendingIntent = PendingIntent.getService(this,
 				(int) task.getId(), intent, 0);
 
 		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 		alarmManager
 				.set(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
 
 	}
 
 	private void cancelReminderAlarm(int id) {
 
 		Intent intent = new Intent(this, AlarmActivity.class);
 		PendingIntent pendingIntent = PendingIntent.getService(
 				getBaseContext(), id, intent, 0);
 
 		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 		alarmManager.cancel(pendingIntent);
 	}
 
 	private Bundle getBundle() {
 		Bundle bundle = new Bundle();
 		bundle.putLong("ListId", list.getId());
 		bundle.putLong("TaskId", task.getId());
 		return bundle;
 	}
 
 	@Override
 	public void onAddressDialogPositiveClick(AddressDialogFragment dialog) {
 		userAddress = dialog.getAddress();
 		showAddress(userAddress);
 	}
 
 	@Override
 	public void onAddressDialogNegativeClick(AddressDialogFragment dialog) {
 		//TODO???
 
 	}
 }
