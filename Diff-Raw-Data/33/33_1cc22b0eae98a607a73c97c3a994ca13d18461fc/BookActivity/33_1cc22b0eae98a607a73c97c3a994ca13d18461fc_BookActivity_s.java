 package org.csie.mpp.buku;
 
 import org.csie.mpp.buku.db.BookEntry;
 import org.csie.mpp.buku.db.DBHelper;
 import org.csie.mpp.buku.helper.BookUpdater;
 import org.csie.mpp.buku.helper.BookUpdater.OnUpdateFinishedListener;
 
 import com.flurry.android.FlurryAgent;
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.AbstractAction;
 import com.markupartist.android.widget.ActionBar.Action;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class BookActivity extends Activity implements OnUpdateFinishedListener {
 	public static final int REQUEST_CODE = 1437;
 	public static final String CHECK_DUPLICATE = "duplicate";
 	
 	private DBHelper db;
 	private BookEntry entry;
 	private ActionBar actionBar;
 	private Action actionAdd, actionDelete;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.book);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         
         db = new DBHelper(this);
 
         Intent intent = getIntent();
         String isbn = intent.getStringExtra(App.ISBN);
         entry = BookEntry.get(db.getReadableDatabase(), isbn);
         
         actionBar = ((ActionBar)findViewById(R.id.actionbar));
         
         boolean updateAll = false;
         
         if(entry != null) {
         	if(intent.getBooleanExtra(CHECK_DUPLICATE, false))
         		Toast.makeText(this, R.string.book_already_exists, 3000).show();
         	
 			actionDelete = new AbstractAction(R.drawable.ic_delete) {
 				@Override
 				public void performAction(View view) {
 					Intent data = new Intent();
 					data.putExtra(App.ISBN, entry.isbn);
 					setResult(RESULT_FIRST_USER, data);
 					finish();
 				}
 			};
 			actionBar.addAction(actionDelete);
         	updateView();
         }
         else {
         	entry = new BookEntry();
         	entry.isbn = isbn;
         	updateAll = true;
         	
 			actionAdd = new AbstractAction(R.drawable.ic_bookshelf) {
 				@Override
 				public void performAction(View view) {
 					if(entry.insert(db.getWritableDatabase()) == false)
 						Log.e(App.TAG, "Insert failed \"" + entry.isbn + "\".");
 					
 					Intent data = new Intent();
 					data.putExtra(App.ISBN, entry.isbn);
 					setResult(RESULT_OK, data);
 
 					Toast.makeText(BookActivity.this, getString(R.string.added), App.TOAST_TIME).show();
 					actionBar.removeAction(this);
 				}
 			};
 			actionBar.addAction(actionAdd);
         }	
         
         BookUpdater updater = new BookUpdater(entry);
         updater.setOnUpdateFinishedListener(this);
         
<<<<<<< Updated upstream
=======
<<<<<<< Updated upstream
        if(updateAll)
        	updater.update();
        else
=======
         updater.updateEntryByBooks();
>>>>>>> Stashed changes
         if(updateAll) {
         	if(updater.updateEntry())
         		updater.updateInfo();
         } else {
<<<<<<< Updated upstream
=======
>>>>>>> Stashed changes
>>>>>>> Stashed changes
         	updater.updateInfo();
         }
     }
     
     @Override
     public void onStart() {
     	super.onStart();
 
 		FlurryAgent.onStartSession(this, App.FLURRY_APP_KEY);
     }
     
     @Override
     public void onStop() {
     	super.onStop();
     	
     	FlurryAgent.onEndSession(this);
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	
     	db.close();
     }
 
     /* --- OnUpdateFinishedListener	(start) --- */
 	@Override
 	public void OnUpdateFinished() {
 		updateView();
 	}
 
 	@Override
 	public void OnUpdateFailed() {
 		if(actionAdd != null)
 			actionBar.removeAction(actionAdd);
 		showError();
 	}
 	/* --- OnUpdateFinishedListener	(end) --- */
     
     private void updateView() {
     	if(entry.cover!=null)
     		((ImageView)findViewById(R.id.image)).setImageBitmap(entry.cover);
     	else
     		((ImageView)findViewById(R.id.image)).setImageResource(R.drawable.book);
     	
         ((TextView)findViewById(R.id.title)).setText(entry.title);
         ((TextView)findViewById(R.id.author)).setText(entry.author);
         
         ((RatingBar)findViewById(R.id.rating)).setRating(entry.info.rating);
         ((TextView)findViewById(R.id.description)).setText(entry.info.description);
         ((TextView)findViewById(R.id.description)).setMovementMethod(new ScrollingMovementMethod());
     }
     
     private void showError() {
     	FlurryAgent.logEvent(App.FlurryEvent.BOOK_NOT_FOUND.toString());
     	
     	((TextView)findViewById(R.id.title)).setText(R.string.book_not_found);
     	Toast.makeText(this, R.string.book_not_found_long, App.TOAST_TIME).show();
     }
 }
