 package tasktracker.view;
 
 import java.awt.Color;
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import tasktracker.controller.DatabaseAdapter;
 import tasktracker.model.Preferences;
 import tasktracker.model.elements.Notification;
 import tasktracker.model.elements.RequestCreateNotification;
 import tasktracker.model.elements.RequestCreateTask;
 import tasktracker.model.elements.RequestModifyTask;
 import tasktracker.model.elements.Task;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.*;
 import android.view.View.OnClickListener;
 import android.widget.*;
 
 /**
  * And activity that displays an existing task. From here, a user may fulfill a
  * task after completing the requirements.
  * 
  * @author Jeanine Bonot
  * 
  */
 public class TaskView extends Activity {
 
 	// The current user
 	private String _user;
 
 	// Activity Items
 	private Button _fulfillmentButton;
 	private TextView _expandButton;
 	private TextView _collapseButton;
 	private Button _photoButton;
 	private TextView _voteLink;
 	private TextView _voteInfo;
 	private EditText _textFulfillment;
 	private LinearLayout _fulfillmentList;
 	private ScrollView _scrollview;
 
 	private TextView _name;
 	private TextView _description;
 	private TextView _creationInfo;
 	private TextView _privacy;
 
 	private TextView _text;
 	private TextView _date;
 
 	// Task Info
 	private String _taskID;
 	private String _taskName;
 	private String _taskCreator;
 	private boolean _requiresText;
 	private boolean _requiresPhoto;
 	private boolean _voted;
 	private int _voteCount;
 	private int _photolist;
 
 	private String[] _photoFilePaths;
 	private boolean _validTextFulfillment;
 
 	private Task task;
 	// DB stuff
 	private DatabaseAdapter _dbHelper;
 	private Cursor _cursor;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_task_view);
 
 		_taskID = getIntent().getStringExtra("TASK_ID"); // TODO change sent
 															// intent to String
 															// ID
 		// Log.d("TaskView", "TASK_ID = " + Integer.toString(_taskID));
 		Log.d("TaskView", "TASK_ID = " + _taskID);
 
 		/*
 		 * if (_taskID == -1) { Log.e("TaskView", "Did not receive task id");
 		 * finish(); }
 		 */
 
 		_dbHelper = new DatabaseAdapter(this);
 		_user = Preferences.getUsername(this);
 
 		_fulfillmentButton = (Button) findViewById(R.id.fulfillButton);
 		_photoButton = (Button) findViewById(R.id.button_photo);
 		_expandButton = (TextView) findViewById(R.id.button_expand);
 		_collapseButton = (TextView) findViewById(R.id.button_collapse);
 
 		_fulfillmentList = (LinearLayout) findViewById(R.id.list_fulfillments);
 		_scrollview = (ScrollView) findViewById(R.id.scrollview);
 		_textFulfillment = (EditText) findViewById(R.id.edit_textFulfillment);
 
 		_voteInfo = (TextView) findViewById(R.id.vote_info);
 		_voteLink = (TextView) findViewById(R.id.text_vote);
 		_voteLink.setOnClickListener(new VoteButtonSetup());
 
 		_expandButton.setOnClickListener(new ExpandButtonSetup());
 		_collapseButton.setOnClickListener(new CollapseButtonSetup());
 		_fulfillmentButton.setOnClickListener(new FulfillButtonSetup());
 
 		setupToolbarButtons();
 
 		_textFulfillment.addTextChangedListener(new TextFulfillmentSetup());
 
 		_photoButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 
 				getImages();
 			}
 
 		});
 
 	}
 
 	protected void onStart() {
 
 		super.onStart();
 
 		_dbHelper.open();
 
 		setTaskInfo();
 		setMembersList();
 		setFulfillmentsList();
 		setVoteInfo();
 	}
 
 	protected void onStop() {
 
 		super.onStop();
 
 		_dbHelper.close();
 		_cursor.close();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.account_menu, menu);
 
 		MenuItem account = menu.findItem(R.id.Account_menu);
 		account.setTitle(_user);
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.logout:
 
 			Intent intent = new Intent(getApplicationContext(), Login.class);
 			startActivity(intent);
 			return true;
 		default:
 			ToastCreator.showShortToast(this, "Not Yet Implemented");
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	// Receives the images from photo picker
 	public void getImages() {
 
 		Intent intent = new Intent(this, PhotoPicker.class);
 		intent.putExtra("sampleData", 0);
 		ArrayList<byte[]> byteArrays  = new ArrayList<byte[]>();
 		byteArrays.addAll(task.getPhotos());
 		int numPhotos = byteArrays.size();
 		System.out.println("TVsend"+numPhotos);
 		intent.putExtra("numPhotos", numPhotos);
 		
 		for(int i = 0; numPhotos>i; i++){
 			intent.putExtra("photo"+i,byteArrays.get(i));	
 			//bytes[i] = photoCompression;
 		}
 		startActivityForResult(intent, 500);
 
 	}
 
 	private void setVoteInfo() {
 
 		_cursor = _dbHelper.countAllVotes(_taskID);
 
 		if (_cursor.moveToFirst()) {
 			_voteCount = _cursor.getInt(0);
 			_voteInfo.setText(_voteCount + " likes");
 			Log.d("TaskView", "Vote count = " + _voteCount);
 		}
 
 		_cursor = _dbHelper.fetchVote(_taskID, _user);
 		if (_cursor.moveToFirst()) {
 			_voted = true;
 			_voteLink.setText("Unlike");
 		} else {
 			_voted = false;
 			_voteLink.setText("Like");
 		}
 	}
 
 	private void setupToolbarButtons() {
 
 		// Assign Buttons
 		Button buttonMyTasks = (Button) findViewById(R.id.buttonMyTasks);
 		Button buttonCreate = (Button) findViewById(R.id.buttonCreateTask);
 		Button buttonNotifications = (Button) findViewById(R.id.buttonNotifications);
 
 		buttonMyTasks.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 
 				startActivity(TaskListView.class);
 			}
 		});
 
 		buttonCreate.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 
 				startActivity(CreateTaskView.class);
 			}
 		});
 
 		buttonNotifications.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 
 				startActivity(NotificationListView.class);
 			}
 		});
 	}
 
 	/**
 	 * Fills the layout views with information on the task.
 	 */
 	private void setTaskInfo() {
 
 		// _cursor = _dbHelper.fetchUserViaID(_taskID);
 		_cursor = _dbHelper.fetchTask(_taskID);
 
 		if (!_cursor.moveToFirst())
 			return;
 
 		_name = (TextView) findViewById(R.id.taskName);
 		_description = (TextView) findViewById(R.id.description);
 		_creationInfo = (TextView) findViewById(R.id.creationInfo);
 		_privacy = (TextView) findViewById(R.id.private_task);
 
 		CheckBox textRequirement = (CheckBox) findViewById(R.id.checkbox_text);
 		CheckBox photoRequirement = (CheckBox) findViewById(R.id.checkbox_photo);
 
 		_taskCreator = _cursor.getString(_cursor
 				.getColumnIndex(DatabaseAdapter.USER));
 		String date = _cursor.getString(_cursor
 				.getColumnIndex(DatabaseAdapter.DATE));
 
 		_requiresText = _cursor.getInt(_cursor
 				.getColumnIndex(DatabaseAdapter.REQS_TEXT)) == 1;
 		_requiresPhoto = _cursor.getInt(_cursor
 				.getColumnIndex(DatabaseAdapter.REQS_PHOTO)) == 1;
 		_taskName = _cursor.getString(_cursor
 				.getColumnIndex(DatabaseAdapter.TASK));
 
 		if (_cursor.getInt(_cursor.getColumnIndex(DatabaseAdapter.PRIVATE)) == 1)
 			_privacy.setVisibility(View.VISIBLE);
 
 		_name.setText(_taskName);
 		_description.setText(_cursor.getString(_cursor
 				.getColumnIndex(DatabaseAdapter.TEXT)));
 		_creationInfo.setText("Created on " + date + " by " + _taskCreator
 				+ ".");
 
 		textRequirement.setChecked(_requiresText);
 		photoRequirement.setChecked(_requiresPhoto);
 		if (task==null)
 			task = new Task(_taskCreator);
 		createTask();
 	}
 
 	/**
 	 * Sets the fulfillment list with data from the SQL database.
 	 */
 	private void setFulfillmentsList() {
 		_fulfillmentList.removeAllViews();
 		_cursor = _dbHelper.fetchFulfillment(_taskID);
 
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		int fulfillerIndex = _cursor.getColumnIndex(DatabaseAdapter.USER);
 		int textIndex = _cursor.getColumnIndex(DatabaseAdapter.TEXT);
 		int dateIndex = _cursor.getColumnIndex(DatabaseAdapter.DATE);
 
 		while (_cursor.moveToNext()) {
 			View view = inflater.inflate(R.layout.list_item, _fulfillmentList,
 					false);
 
 			view.findViewById(R.id.item_date_bottom).setVisibility(View.GONE);
 			view.findViewById(R.id.item_vote_count).setVisibility(View.GONE);
 			TextView fulfiller = (TextView) view.findViewById(R.id.item_title);
 			TextView text = (TextView) view.findViewById(R.id.item_text);
 			TextView date = (TextView) view.findViewById(R.id.item_top_right);
 
 			fulfiller.setText("Fulfilled by "
 					+ _cursor.getString(fulfillerIndex));
 			text.setText(_cursor.getString(textIndex));
 			text.setTextSize(12);
 			date.setText(_cursor.getString(dateIndex));
 			_fulfillmentList.addView(view);
 		}
 
 	}
 
 	/**
 	 * Sets the member list with data from the SQL database.
 	 */
 	@SuppressWarnings("deprecation")
 	private void setMembersList() {
 
 		ListView members = (ListView) findViewById(R.id.membersList);
 		_cursor = _dbHelper.fetchTaskMembers(_taskID);
 
 		startManagingCursor(_cursor);
 
 		String[] from = new String[] { DatabaseAdapter.USER };
 		int[] to = new int[] { R.id.text };
 
 		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
 				R.layout.simple_list_item, _cursor, from, to);
 		int count = adapter.getCount();
 		members.setAdapter(adapter);
 		stopManagingCursor(_cursor);
 
 		TextView headline = (TextView) findViewById(R.id.heading_Members);
 
 		if (count == 1) {
 			headline.setText(count + " Member:");
 		} else {
 			headline.setText(count + " Members:");
 		}
 	}
 
 	/**
 	 * Send a notification report of the task fulfillment to the creator.
 	 */
 	private void sendFulfillmentNotification(String message) {
 		Notification notification = new Notification(message);
 		notification.setRecipients(new String[]{_taskCreator});
 		RequestCreateNotification request = new RequestCreateNotification(this, notification);
 //		_dbHelper.createNotification(_taskID, _taskCreator, message);
 	}
 
 	/**
 	 * Send an email report of the task fulfillment to the creator.
 	 */
 	private void sendFulfillmentEmail(String message, String textFulfillment) {
 
 		// TODO uncomment when email has been put into database
 		// _cursor = _dbHelper.fetchUserViaName(_taskCreator);
 		//
 		// if (!_cursor.moveToFirst()) {
 		// ToastCreator.showLongToast(this,
 		// "Could not find creator information");
 		// return;
 		// }
 
 		// String email = _cursor.getString(_cursor
 		// .getColumnIndexOrThrow(DatabaseAdapter.EMAIL));
 
 		String email = "cmput301f12t08@gmail.com"; // TODO remove hardcode
 
 		Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
 		i.setType("text/plain");
 		i.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
 		i.putExtra(Intent.EXTRA_SUBJECT,
 				"TaskTracker : Task Fulfillment Report");
 		i.putExtra(Intent.EXTRA_TEXT, message + "\n\n" + textFulfillment);
 
		
 		ArrayList<Uri> uris = new ArrayList<Uri>();
		if (_photoFilePaths!=null){
		    for (String file : _photoFilePaths) {
 			uris.add(Uri.parse("file://" + file));
		    }
 		}
 		i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
 
 		try {
 			startActivity(Intent.createChooser(i, "Send mail..."));
 		} catch (android.content.ActivityNotFoundException ex) {
 			ToastCreator.showShortToast(this,
 					"There are no email clients installed.");
 		}
 	}
 
 	/**
 	 * Soon to be deleted. Makes sure that the requirements have been completed
 	 * before fulfilling a task.
 	 * 
 	 * @return True if the requirements we fulfilled; otherwise false.
 	 */
 	private boolean requirementsFulfilled() {
 
 		if (_requiresPhoto
 				&& (_photoFilePaths == null || _photoFilePaths.length == 0)) {
 //			ToastCreator
 //					.showLongToast(this,
 //							"You must add a photo before marking this task as fulfilled.");
 			return false;
 
 		}
 
 		if (_requiresText && !_validTextFulfillment) {
 //			ToastCreator.showLongToast(this, "You must supply some text before marking this task as fulfilled");
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Start a new activity.
 	 * 
 	 * @param destination
 	 *            The activity class destination.
 	 */
 	private <T extends Activity> void startActivity(Class<T> destination) {
 
 		Intent intent = new Intent(getApplicationContext(), destination);
 		startActivity(intent);
 	}
 
 	class TextFulfillmentSetup implements TextWatcher {
 
 		public void afterTextChanged(Editable s) {
 
 			_validTextFulfillment = (s.length() > 0) && !s.toString().matches("\\s+");
 			_fulfillmentButton.setEnabled(requirementsFulfilled());
 		}
 
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 
 			// Do nothing.
 
 		}
 
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {
 
 			// Do nothing.
 
 		}
 	}
 
 	/**
 	 * OnClickListener for the expand button. When the user clicks the expand
 	 * button, the fields that are required for task fulfillment are displayed,
 	 * as well as the fulfillment button.
 	 */
 	class ExpandButtonSetup implements OnClickListener {
 
 		/**
 		 * Sets views a visible if they are required for fulfillment.
 		 */
 		public void onClick(View v) {
 
 			_collapseButton.setVisibility(View.VISIBLE);
 			_expandButton.setVisibility(View.GONE);
 
 			_fulfillmentList.setVisibility(View.VISIBLE);
 
 			if (_requiresText) {
 				_textFulfillment.setVisibility(View.VISIBLE);
 				_textFulfillment.requestFocus();
 			}
 
 			if (_requiresPhoto)
 				_photoButton.setVisibility(View.VISIBLE);
 
 			if (!(_requiresText || _requiresPhoto))
 				_fulfillmentButton.setEnabled(true);
 
 			_fulfillmentButton.setVisibility(View.VISIBLE);
 
 			// Scroll down so the user can quickly complete the task
 			// requirements.
 			_scrollview.post(new Runnable() {
 
 				public void run() {
 
 					_scrollview.fullScroll(ScrollView.FOCUS_DOWN);
 				}
 
 			});
 		}
 	}
 
 	/**
 	 * OnClickListener for the collapse button. Hides all views that are related
 	 * to task fulfillment.
 	 */
 	class CollapseButtonSetup implements OnClickListener {
 
 		public void onClick(View v) {
 
 			_expandButton.setVisibility(View.VISIBLE);
 			_collapseButton.setVisibility(View.GONE);
 			_fulfillmentList.setVisibility(View.GONE);
 			_fulfillmentButton.setVisibility(View.GONE);
 			_textFulfillment.setVisibility(View.GONE);
 			_photoButton.setVisibility(View.GONE);
 		}
 	}
 
 	class FulfillButtonSetup implements OnClickListener {
 
 		public void onClick(View v) {
 			Log.d("TaskView", "Fulfillbutton Clicked");
 			if (requirementsFulfilled()) {
 
 				String textFulfillment = _textFulfillment.getText().toString();
 				String date = new SimpleDateFormat("MMM dd, yyyy | HH:mm")
 						.format(Calendar.getInstance().getTime());
 				long id = _dbHelper.createFulfillment(_taskID, date, _user,
 						textFulfillment);
 
 				Log.d("Task Fulfillment",
 						"fulfillment id: " + Long.toString(id));
 				// if (!_user.equals(_taskCreator)) {
 				String message = Notification.getMessage(_user, _taskName,
 						Notification.Type.FulfillmentReport);
 				sendFulfillmentNotification(message);
 				sendFulfillmentEmail(message, textFulfillment);
 				// }
 
 				ToastCreator.showLongToast(TaskView.this, "\"" + _taskName
 						+ "\" was fulfilled!");
 				// TODO: Does task need to be updated on web?
 				 RequestModifyTask updateTask = new RequestModifyTask(
 				 getBaseContext(), task);
 				finish();
 			}
 		}
 	}
 
 	class VoteButtonSetup implements OnClickListener {
 
 		public void onClick(View v) {
 
 			if (_voted) {
 				boolean success = _dbHelper.deleteVote(_taskID, _user);
 				Log.d("TaskView", "Deleted Like: " + success);
 				_voteCount--;
 				_voteLink.setText("Like");
 				_voteLink.setTextColor(getResources().getColor(R.color.link));
 			} else {
 				_dbHelper.createVote(_taskID, _user);
 				_voteCount++;
 				_voteLink.setText("Unlike");
 				_voteLink.setTextColor(getResources().getColor(R.color.liked));
 			}
 
 			_voted = !_voted;
 
 			_voteInfo.setText(_voteCount + " likes");
 		}
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// Check which request we're responding to
 		if (requestCode == 500) {
 			// Make sure the request was successful
 
 			// Toast.makeText(TaskView.this, data.getIntExtra("sampleData", -1),
 			// 2000).show();
 
 			// Toast.makeText(TaskView.this,
 			// data.getStringExtra("returnedData"), 2000).show();
 			if (resultCode == RESULT_OK) {
 				_photoFilePaths = data.getStringArrayExtra("PhotoPaths");
 				_fulfillmentButton.setEnabled(requirementsFulfilled());
 				
 				ArrayList<byte[]> byteArrays  = new ArrayList<byte[]>();
 				int numPhotos = data.getIntExtra("numPhotos", 0);
 
 				System.out.println("TVrec"+numPhotos);
 				for(int i = 0; numPhotos>i; i++){
 					byte[] compressedPhoto = data.getByteArrayExtra("photo"+i);	
 					//bytes[i] = photoCompression;
 					byteArrays.add(compressedPhoto);
 				}
 				
 				task.setPhotos(byteArrays);
 				
 				// Toast.makeText(TaskView.this,
 				// data.getStringArrayExtra("PhotoPaths")[0], 2000).show();
 				_scrollview.post(new Runnable() {
 
 					public void run() {
 
 						_scrollview.fullScroll(ScrollView.FOCUS_DOWN);
 					}
 
 				});
 			}
 		}
 	}
 
 	private Task createTask() {
 
 		// TODO: Find out how to quickly access user information
 		task.setCreatorID(_taskCreator);
 		task.setID(_taskID);
 		task.setDescription(_description.getText().toString());
 		task.setName(_taskName);
 		task.setPhotoRequirement(_requiresPhoto);
 		task.setTextRequirement(_requiresText);
 		// task.setDate(_date.getText().toString());
 		// task.setOtherMembers(_otherMembers.getText().toString());
 
 		boolean privacyBoolean = (_privacy.getVisibility() == 1) ? true : false;
 		task.setIsPrivate(privacyBoolean);
 		// task.setIsDownloaded("Yes"); // Since it was created on this phone,
 		// it's
 		// already in the SQL table
 		task.setLikes(_voteCount);
 		// private int _voteCount;
 		// private int _photolist;
 		// task.setCreatorID(Preferences.getUserID(getBaseContext()));
 
 		return task;
 	}
 
 }
