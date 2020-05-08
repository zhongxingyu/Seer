 package de.fhb.maus.android.todolist;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 import de.fhb.maus.android.todolist.R;
 import de.fhb.maus.android.todolist.database.TodoDatabaseAdapter;
 
 public class TodoEditActivity extends Activity {
 	private EditText mTitleText;
 	private EditText mBodyText;
 	private CheckBox mCheckBox;
 	private Long mRowId;
 	private TodoDatabaseAdapter mDbHelper;
 	private Spinner mCategory;
 
 	private boolean backButtonOverClicked = false;
 
 	@Override
 	protected void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		mDbHelper = new TodoDatabaseAdapter(this);
 		mDbHelper.open();
 		setContentView(R.layout.todo_edit);
 		mCategory = (Spinner) findViewById(R.id.category);
 		mTitleText = (EditText) findViewById(R.id.summary);
 		mBodyText = (EditText) findViewById(R.id.description);
 		mCheckBox = (CheckBox) findViewById(R.id.checkBox);
 
 		Button confirmButton = (Button) findViewById(R.id.button_save);
 		mRowId = null;
 		Bundle extras = getIntent().getExtras();
 		mRowId = (bundle == null) ? null : (Long) bundle
 				.getSerializable(TodoDatabaseAdapter.KEY_ROWID);
 		if (extras != null) {
 			mRowId = extras.getLong(TodoDatabaseAdapter.KEY_ROWID);
 		}
 		populateFields();
 		confirmButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				setResult(RESULT_OK);
 				finish();
 			}
 		});
 	}
 
 	// When showing the Edit Screen for a ToDo
 	private void populateFields() {
 		if (mRowId != null) {
 			Cursor todo = mDbHelper.fetchTodo(mRowId);
 			startManagingCursor(todo);
 			String category = todo.getString(todo
 					.getColumnIndexOrThrow(TodoDatabaseAdapter.KEY_CATEGORY));
 
 			for (int i = 0; i < mCategory.getCount(); i++) {
 
 				String s = (String) mCategory.getItemAtPosition(i);
 				Log.e(null, s + " " + category);
 				if (s.equalsIgnoreCase(category)) {
 					mCategory.setSelection(i);
 				}
 			}
 
 			if (todo.getInt(todo
 					.getColumnIndexOrThrow(TodoDatabaseAdapter.KEY_DONE)) == 1) {
 				mCheckBox.setChecked(true);
 			} else {
 				mCheckBox.setChecked(false);
 			}
 
 			mTitleText.setText(todo.getString(todo
 					.getColumnIndexOrThrow(TodoDatabaseAdapter.KEY_SUMMARY)));
 			mBodyText
 					.setText(todo.getString(todo
 							.getColumnIndexOrThrow(TodoDatabaseAdapter.KEY_DESCRIPTION)));
 		}
 	}
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		saveState();
 		outState.putSerializable(TodoDatabaseAdapter.KEY_ROWID, mRowId);
 	}
 
 	// Just Save the Edit if not the back button was clicked
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (!backButtonOverClicked) {
 			saveState();
 		}
 	}
 
 	// Looking for the pressevent of the back Button
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO http://developer.android.com/reference/android/app/Activity.html#onBackPressed%28%29
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 			Log.d(this.getClass().getName(), "back button pressed");
 			backButtonOverClicked = true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		populateFields();
 	}
 
 	private void saveState() {
 		String category = (String) mCategory.getSelectedItem();
 		String summary = mTitleText.getText().toString();
 		String description = mBodyText.getText().toString();
 		boolean done = mCheckBox.isChecked();
 		Toast.makeText(
 				this,
 				getResources().getString(R.string.additionalTodo)
 						+ " "
 						+ summary
 						+ " "
 						+ getResources()
 								.getString(R.string.additionalTodoSaved),
 				Toast.LENGTH_LONG).show();
 
 		if (mRowId == null) {
 			long id = mDbHelper
 					.createTodo(category, done, summary, description);
 			if (id > 0) {
 				mRowId = id;
 			}
 		} else {
 			mDbHelper.updateTodo(mRowId, category, done, summary, description);
 		}
 	}
 }
