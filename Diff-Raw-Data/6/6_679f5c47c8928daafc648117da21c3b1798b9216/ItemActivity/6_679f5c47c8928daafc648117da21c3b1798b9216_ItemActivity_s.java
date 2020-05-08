 package edu.pitt.cs1635group3;
 
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class ItemActivity extends Activity {
 	
 	private static final int SWIPE_MIN_DISTANCE = 120;
 	private static final int SWIPE_MAX_OFF_PATH = 250;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 	private GestureDetector gestureDetector;
 	View.OnTouchListener gestureListener;
 	
 	private Item item, prevItem, nextItem;
 	private DBHelper db;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.item);
 		
 		// Gesture detection
         gestureDetector = new GestureDetector(new MyGestureDetector());
         gestureListener = new View.OnTouchListener() {
             public boolean onTouch(View v, MotionEvent event) {
                 return gestureDetector.onTouchEvent(event);
             }
         };
 
 		TextView name, quantity, creation_details, assignee, notes;
 		name = (EditText) findViewById(R.id.item_name);
 		quantity = (EditText) findViewById(R.id.item_quantity);
 		creation_details = (TextView) findViewById(R.id.item_creation);
 		assignee = (TextView) findViewById(R.id.item_assignee);
 		notes = (EditText) findViewById(R.id.item_notes);
 		
 		ToggleButton itemCompletion = (ToggleButton) findViewById(R.id.item_completion);
 		
 		//swiper = (View) findViewById(R.id.swiper);
 		notes.setOnTouchListener(gestureListener);
 
 		Intent i = getIntent();
 		Bundle extras = i.getExtras();
 		int itemID = extras.getInt("ItemID");
 
 		db = new DBHelper(this);
 		db.open();
 
 		Log.d("ITEM RECEIVED", "Item ID = " + itemID);
 		item = db.getItem(itemID);
 
 		prevItem = db.getItem(item.getPrev());
 		nextItem = db.getItem(item.getNext());
 
 		itemCompletion.setChecked(item.isCompleted());
 		
 		name.setText(item.getName());
 		quantity.setText("" + item.getQuantity());
 
 		String creator;
 		if (item.getCreator() > 0)
 			creator = db.getUserNameByID(item.getCreator());
 		else
 			creator = "";
 		creation_details.setText("Added on " + item.getCreationDate() + " by "
 				+ creator);
 
 		if (item.getAssignee() > 0) {
 			Log.d("ASSIGNEE", item.getAssignee() + "");
 			assignee.setText(db.getUserByID(item.getAssignee()).getName());
 		} else {
 			assignee.setHint("Click to assign");
 		}
 
 		notes.setText(item.getNotes());
 
 		db.close();
 
 		assignee.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				selectAssignee(v);
 			}
 		});
 
 	}
 	
 	public void onToggleClicked(View v) {
 	    // Perform action on clicks
 	    if (((ToggleButton) v).isChecked()) {
 	        item.setCompleted(true);
 	        Log.d("TOGGLE", "Item completed");
 	    } else {
 	        item.setCompleted(false);
 	        Log.d("TOGGLE", "Item completed");
 	    }
 	}
 	
 	public void assignItemTo(String user) {
 		
 		TextView assignee = (TextView) findViewById(R.id.item_assignee);
 		assignee.setText(user);
 		assignee.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				selectAssignee(v);
 			}
 		});
 		// item.assignTo(userID); -- Don't do this here. Do on save
 	}
 
 	public void selectAssignee(View v) {
 		db.open();
 		final CharSequence[] users = db.getUsersForDialog();
 		db.close();
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Assign To");
 		builder.setItems(users, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int pos) {
 				assignItemTo((String) users[pos]);
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public void saveItem(View v) {
 
 		// regather the item details, open the db, use the updateItem method.
 
 		TextView name, quantity, assignee, notes;
 		name = (EditText) findViewById(R.id.item_name);
 		quantity = (EditText) findViewById(R.id.item_quantity);
 		assignee = (TextView) findViewById(R.id.item_assignee);
 		notes = (EditText) findViewById(R.id.item_notes);
 
 		//toggle is handled onClick for item completion altering
 
 		item.setName(name.getText().toString().trim());
 		item.setQuantity(Integer.parseInt(quantity.getText().toString().trim()));
 		item.setNotes(notes.getText().toString().trim());
 
 		String rawAssignee = assignee.getText().toString().trim();
 		int assigneeID;
		if (rawAssignee != "" && rawAssignee != null) {
 			db.open();
 			assigneeID = db.getUserByName(rawAssignee);
 			item.assignTo(assigneeID);
 			db.updateItem(item);
 			db.close();
 		}
 		
 		Intent in = new Intent();
 	    setResult(1,in);//Requestcode 1. Tell parent activity to refresh items.
 	    finish();
 
 	}
 
 	public void deleteItem(View v) {
 		Toast.makeText(this, "TODO; deleteItem method in ItemActivity.java",
 				Toast.LENGTH_LONG).show();
 
 		// just gather the item ID, open the db, use the deleteItem method,
 		// reset wiring for linked list,
 		// close the db.
 	}
 	
 	public void goToPrev() {
 
 		Intent intent = new Intent(this, ItemActivity.class);
 		intent.putExtra("ItemID", prevItem.getID());
 		startActivity(intent);
 		finish();
 	}
 
 	public void prevItem(View v) {
 		// calling like this because it's an onclick but we also use it for swipe
 		goToPrev();
 	}
 	
 	public void goToNext() {
 		Intent intent = new Intent(this, ItemActivity.class);
 		intent.putExtra("ItemID", nextItem.getID());
 		startActivity(intent);
 		finish();
 	}
 
 	public void nextItem(View v) {
 		goToNext();
 	}
 	
 
     class MyGestureDetector extends SimpleOnGestureListener {
         @Override
         public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
             try {
                 if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                     return false;
                 // right to left swipe
                 if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                     Toast.makeText(getBaseContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
                 	goToPrev();
                 }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                 	Toast.makeText(getBaseContext(), "Right Swipe", Toast.LENGTH_SHORT).show();
                 	goToNext();
                 }
             } catch (Exception e) {
                 // nothing
             }
             return false;
         }
     }
 
 }
