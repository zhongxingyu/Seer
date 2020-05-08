 package com.mtd.textwhen;
 
 import java.util.Calendar;
 import java.util.Locale;
 
 import com.mtd.textwhen.R;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.view.Surface;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TextEditor extends Activity {
	TextDbAdapter							db;
 	TextView									dateView;
 	TextView									timeView;
 	Button										newBtn;
 	Button										contactPickerBtn;
 	Button										sendBtn;
 	Button										cancelBtn;
 	EditText									editRecipient;
 	EditText									editSubject;
 	EditText									editBody;
 	Intent										incomingIntent;
 	Bundle										savedInstanceState;
 
 	private static final int	CONTACT_PICKER_RESULT	= 1001;
 	private Calendar					scheduledDate;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.SECOND, 0);
 		this.scheduledDate = cal;
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.editview);
 		this.incomingIntent = this.getIntent();
 		if (db == null) {
 			db = new TextDbAdapter(this);
 			db.open();
 		}
 		int rotation = ((WindowManager) getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay().getOrientation();
 		if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
 			setContentView(R.layout.editview);
 		} else {
 			setContentView(R.layout.editview_landscape);
 		}
 
 		dateView = (TextView) findViewById(R.id.DateView);
 		timeView = (TextView) findViewById(R.id.TimeView);
 
 		editRecipient = (EditText) findViewById(R.id.EditTextRecipient);
 		editBody = (EditText) findViewById(R.id.EditTextBody);
 		contactPickerBtn = (Button) findViewById(R.id.ContactPicker);
 		newBtn = (Button) findViewById(R.id.ButtonAddNew);
 		cancelBtn = (Button) findViewById(R.id.ButtonCancel);
 		sendBtn = (Button) findViewById(R.id.ButtonSendNow);
 
 		if (incomingIntent.getBooleanExtra("update", false)) {
 			editRecipient.setText(incomingIntent.getStringExtra("recipient"));
 			editBody.setText(incomingIntent.getStringExtra("body"));
 			cal.setTimeInMillis(incomingIntent.getLongExtra("date", 0));
 		}
 
 		this.refreshDate(true, true);
 
 		initializeButtons();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		Calendar cal = this.scheduledDate;
 		cal.setTimeInMillis(savedInstanceState.getLong("date"));
 		cal.set(Calendar.SECOND, 0);
 		this.refreshDate(true, true);
 		editBody.setText(savedInstanceState.getString("body"));
 		String recipient = editRecipient.getText().toString();
 		if (recipient.length() == 0) {
 			editRecipient.setText(savedInstanceState.getString("recipient"));
 		}
 
 	}
 
 	private void initializeButtons() {
 		cancelBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				TextEditor.this.finish();
 			}
 		});
 
 		sendBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (incomingIntent.getBooleanExtra("update", false)) {
 					OutgoingText text = OutgoingText.fromIntent(incomingIntent);
 					db.removeEntry(text.getKey());
 					Intent intent = text.toAlarmIntent(TextEditor.this, false, true);
 					startService(intent);
 					Calendar c = Calendar.getInstance();
 					text.updateText(text.recipient, text.subject, text.messageContent, c,
 							c.getTimeZone().getOffset(c.getTimeInMillis()));
 					intent = text.toAlarmIntent(TextEditor.this, true, false);
 					startService(intent);
 				} else {
 					if (editRecipient.getText().toString().length() >= 5) {
 						OutgoingText text = new OutgoingText(editRecipient.getText()
 								.toString(), null, editBody.getText().toString(), Calendar
 								.getInstance(), 0);
 						Intent intent = text.toAlarmIntent(TextEditor.this, true, false);
 						startService(intent);
 					} else {
 						Toast.makeText(v.getContext(),
 								"Please check the recipient field again.", Toast.LENGTH_LONG)
 								.show();
 					}
 				}
 
 				TextEditor.this.finish();
 			}
 		});
 
 		contactPickerBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
 						Contacts.CONTENT_URI);
 				startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
 			}
 		});
 
 		newBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (editRecipient.getText() != null
 						&& editRecipient.getText().toString().length() >= 5) {
 					Calendar cal = scheduledDate;
 					cal.set(Calendar.SECOND, 0);
 					int gmtOffset = cal.getTimeZone().getOffset(cal.getTimeInMillis());
 					if (cal.after(Calendar.getInstance())) {
 						OutgoingText text = new OutgoingText(editRecipient.getText()
 								.toString(), null, editBody.getText().toString(), cal,
 								gmtOffset);
 
 						Intent passedIntent;
 						boolean update = incomingIntent.getBooleanExtra("update", false);
 						boolean setAlarm = incomingIntent.getBooleanExtra("setAlarm", true);
 						if (update) {
 							text.setKey(incomingIntent.getLongExtra("key", 0));
 							passedIntent = text.toAlarmIntent(TextEditor.this, false, update);
 							db.updateEntry(text, text.getKey());
 							TextEditor.this.startService(passedIntent);
 
 							passedIntent = text.toAlarmIntent(TextEditor.this, true, update);
 							TextEditor.this.startService(passedIntent);
 						} else {
 							long key = db.insertEntry(text);
 							text.setKey(key);
 							passedIntent = text.toAlarmIntent(TextEditor.this, setAlarm,
 									update);
 							TextEditor.this.startService(passedIntent);
 						}
 
 						Toast.makeText(v.getContext(), "Saved Message: " + text.toString(),
 								Toast.LENGTH_LONG).show();
 
 						TextEditor.this.finish();
 
 					} else {
 
 						Toast.makeText(v.getContext(),
 								"Sending immediately--time or date has passed already.",
 								Toast.LENGTH_LONG).show();
 
 						if (incomingIntent.getBooleanExtra("update", false)) {
 							OutgoingText text = OutgoingText.fromIntent(incomingIntent);
 							db.removeEntry(text.getKey());
 							Intent intent = text.toAlarmIntent(TextEditor.this, false, true);
 							startService(intent);
 							Calendar c = Calendar.getInstance();
 							text.updateText(text.recipient, text.subject,
 									text.messageContent, c,
 									c.getTimeZone().getOffset(c.getTimeInMillis()));
 							intent = text.toAlarmIntent(TextEditor.this, true, false);
 							TextEditor.this.startService(intent);
 						} else {
 							OutgoingText text = new OutgoingText(editRecipient.getText()
 									.toString(), null, editBody.getText().toString(), Calendar
 									.getInstance(), 0);
 							Intent intent = text.toAlarmIntent(TextEditor.this, true, false);
 							TextEditor.this.startService(intent);
 						}
 
 						TextEditor.this.finish();
 					}
 
 				} else {
 
 					Toast
 							.makeText(
 									v.getContext(),
 									"Message cannot be saved as is.  Please ensure date, time, recipient, and message are filled in.",
 									Toast.LENGTH_LONG).show();
 				}
 			}
 
 		});
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_OK) {
 			switch (requestCode) {
 				case CONTACT_PICKER_RESULT:
 					Cursor cursor = null;
 					String phone = "";
 					try {
 						Uri result = data.getData();
 
 						// get the contact id from the Uri
 						String id = result.getLastPathSegment();
 
 						// query for mobile phone
 						cursor = getContentResolver().query(Phone.CONTENT_URI, null,
 								Phone.CONTACT_ID + "=?" + "AND " + Phone.TYPE + "=?",
 								new String[] { id, String.valueOf(Phone.TYPE_MOBILE) }, null);
 
 						int phoneIdx = cursor.getColumnIndex(Phone.NUMBER);
 
 						if (cursor.moveToFirst()) {
 							phone = cursor.getString(phoneIdx);
 						} else {
 						}
 					} catch (Exception e) {
 					} finally {
 						if (cursor != null) {
 							cursor.close();
 						}
 						if (phone.length() == 0) {
 							Toast.makeText(this, "No phone number found for contact.",
 									Toast.LENGTH_LONG).show();
 						} else {
 							if (editRecipient.getText().toString().length() > 0) {
 								editRecipient.setText(editRecipient.getText().toString() + ","
 										+ phone);
 							} else {
 								editRecipient.setText(phone);
 							}
 						}
 					}
 					break;
 			}
 		}
 	}
 
 	@Override
 	public void finish() {
 		super.finish();
 		if (!(db == null)) {
 			db.close();
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		TextEditor.this.finish();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		super.onSaveInstanceState(savedInstanceState);
 		Calendar cal = this.scheduledDate;
 		savedInstanceState.putLong("date", cal.getTimeInMillis());
 		savedInstanceState.putString("recipient", editRecipient.getText()
 				.toString());
 		savedInstanceState.putString("body", editBody.getText().toString());
 		long key = incomingIntent.getLongExtra("key", 0);
 		if (key != 0) {
 			savedInstanceState.putLong("key", key);
 		}
 	}
 
 	public void showTimePickerDialog(View v) {
 		DialogFragment newFragment = new TimePickerFragment();
 		newFragment.show(this.getFragmentManager(), "timePicker");
 	}
 
 	public void showDatePickerDialog(View v) {
 		DialogFragment newFragment = new DatePickerFragment();
 		newFragment.show(this.getFragmentManager(), "datePicker");
 	}
 
 	public Calendar getSchedule() {
 		return this.scheduledDate;
 	}
 
 	public void refreshDate(boolean updateTime, boolean updateDate) {
 		if (updateTime) {
 			updateTimeText();
 		}
 
 		if (updateDate) {
 			updateDateText();
 		}
 	}
 
 	private void updateTimeText() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(this.scheduledDate.get(Calendar.HOUR));
 		sb.append(":");
 		sb.append(this.scheduledDate.get(Calendar.MINUTE));
 		sb.append(" ");
 		sb.append(this.scheduledDate.getDisplayName(Calendar.AM_PM, Calendar.SHORT,
 				Locale.US));
 		this.timeView.setText(sb.toString());
 	}
 
 	private void updateDateText() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(this.scheduledDate.getDisplayName(Calendar.MONTH, Calendar.SHORT,
 				Locale.US));
 		sb.append(" ");
 		sb.append(this.scheduledDate.get(Calendar.DATE));
 		sb.append(", ");
 		sb.append(this.scheduledDate.get(Calendar.YEAR));
 		this.dateView.setText(sb.toString());
 	}
 
 }
