 package sk.peterjurkovic.dril;
 
 import sk.peterjurkovic.dril.db.LectureDBAdapter;
 import sk.peterjurkovic.dril.db.WordDBAdapter;
 import sk.peterjurkovic.dril.fragments.AddWordFragment;
 import sk.peterjurkovic.dril.fragments.EditWordFragment;
 import sk.peterjurkovic.dril.fragments.ViewWordFragment;
 import sk.peterjurkovic.dril.fragments.WordListFragment;
 import sk.peterjurkovic.dril.listener.OnAddWordListener;
 import sk.peterjurkovic.dril.listener.OnChangeWordStatusListener;
 import sk.peterjurkovic.dril.listener.OnDeleteWordListener;
 import sk.peterjurkovic.dril.listener.OnEditWordClickedListener;
 import sk.peterjurkovic.dril.listener.OnEditWordListener;
 import sk.peterjurkovic.dril.listener.OnShowWordListener;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WordActivity extends FragmentActivity 
 							implements OnAddWordListener,
 									   OnEditWordClickedListener,
 									   OnEditWordListener,
 									   OnShowWordListener,
 									   OnDeleteWordListener,
 									   OnChangeWordStatusListener{
 		
 	private static final int REQUEST_ADD_WORD = 0;
 	
 	private static final int REQUEST_EDIT_WORD = 1;
 	
 	private static final int REQUEST_VIEW = 2;
 	
 	public static final String LECTURE_ID_EXTRA = "fk_lecture_id";
 	
 	private boolean dualPane;
 	
 	public static final String TAG = "WordActivity";
 	
 	private long lectureId = -1;
 	
 	private String lectureName;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.word_activity);
         
         lectureId = getIntent().getLongExtra(LECTURE_ID_EXTRA, -1);
         
         if(lectureId == -1) throw new Error("Lecture ID is not set.");
         
         dualPane = findViewById(R.id.right_column) != null;
         
         lectureName = getLectureName(lectureId);
         
 	    ((TextView)findViewById(R.id.wordListLabel)).setText( lectureName );
         
         ((Button)findViewById(R.id.addNewWord)).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				onAddNewWordClicked();
 			}
 		});
         
         ImageButton goHome = (ImageButton) findViewById(R.id.home);
         goHome.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 startActivity( new Intent(WordActivity.this, DashboardActivity.class) );
             }
         });
 	}
 	
 	
 	
 	/** ------------------------------------------------------------
 	 * Router of activities results.
 	 * 
 	 * implemented operation:
 	 * 
 	 *  - REQUEST_ADD_WORD (0), save new word into lection 
 	 *  - REQUEST_EDIT_WORD (1), save edited word into lection
 	 * 
 	 * @throws Error otherwise
 	 */
 	@Override
     protected void onActivityResult (int requestCode, int resultCode, Intent data){
 	 	if(resultCode != RESULT_OK) return;
 	 	switch(requestCode){
 	 		case REQUEST_ADD_WORD :
 		 		saveNewWord(	
 		 				data.getStringExtra(AddWordActivity.EXTRA_QUESTION),
		 				data.getStringExtra(AddWordActivity.EXTRA_ANSWER)
 		 				);
 	 		break;
 		 	case REQUEST_EDIT_WORD :
 		 		saveEditedWord(
 		 				data.getLongExtra(EditWordActivity.EXTRA_WORD_ID, -1),
 		 				data.getStringExtra(EditWordActivity.EXTRA_QUESTION),
		 				data.getStringExtra(EditWordActivity.EXTRA_ANSWER)
 	 				);
 	 		break;
 		 	case REQUEST_VIEW :
 		 			int action = data.getIntExtra(ViewWordActivity.ACTION, -1);
 		 			long wordId = data.getLongExtra(EditWordActivity.EXTRA_WORD_ID, -1);
 		 					
 		 			switch(action){
 		 				case ViewWordActivity.EVENT_EDIT :
 		 					onEditWordClicked(wordId);
 		 				break;
 		 				case ViewWordActivity.EVENT_DELETE :
 		 					onDeleteClicked(wordId);
 		 				break;
 		 		
 		 			}
 		 			
 	 		break;
 	 		default : 
 	 			throw new Error("Unknown activity requestCode: " + requestCode);
 	 	
 	 	}
         super.onActivityResult(requestCode, resultCode, data);
     }
 	
 
 	
 	/** ------------------------------------------------------------
 	 * Create new word and update items of fragment: WordListFragment. 
 	 * 
 	 * @param String question
 	 * @param String answer
 	 */
 	@Override
 	public void saveNewWord(String question, String answer) {		
 		WordDBAdapter wordDBAdapter = new WordDBAdapter(this);
 		
 		long id = -1;
 		try {
 			id = wordDBAdapter.insertWord(lectureId, question, answer);
 		} catch (Exception e) {
 			Log.d(TAG, "ERROR: " + e.getMessage());
 		} finally {
 			wordDBAdapter.close();
 		}
 		
 		if(id > -1){
 			getWordListFragment().updateList();
 			showToastMessage(this,  R.string.word_added);
 		    if(dualPane){
 		    	showWord( id );
 		    }
 		}else{
 			showToastMessage(this,  R.string.word_not_added);
 		}
 		
 	}
 
 	
 	/** ------------------------------------------------------------
 	 * Select current lecture name from database 
 	 * 
 	 * @param long ID of given lecture
 	 * @return name of current lecture
 	 */
 	public String getLectureName( long lectureId ){
 		LectureDBAdapter lectureDbAdapter = new LectureDBAdapter( this );
 		Cursor c;
 		String lectureName = null;
 		try{
 			c = lectureDbAdapter.getLecture(lectureId);
 			c.moveToFirst();
 			int bookNameIndex = c.getColumnIndex( LectureDBAdapter.LECTURE_NAME );
 			lectureName = c.getString(bookNameIndex);
 			c.close();
 	    } catch (Exception e) {
 			Log.d("getLectureName", "ERROR: " + e.getMessage());
 		} finally {
 			lectureDbAdapter.close();
 		}
 		return lectureName;
 	}
 	
 	
 	
 	private void onAddNewWordClicked() {
 		if(dualPane){
 			Bundle data = new Bundle();
 			data.putString(AddWordActivity.EXTRA_LECTURE_NAME, lectureName);
             Fragment f = new AddWordFragment();
             f.setArguments(data);
             FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
             ft.replace(R.id.right_column, f);
             ft.addToBackStack(null);
             ft.commit();
 	    }else{
 	        Intent i = new Intent(this, AddWordActivity.class);
 	        i.putExtra(AddWordActivity.EXTRA_LECTURE_NAME, lectureName);
 	        startActivityForResult(i, REQUEST_ADD_WORD);
         }
 	}
 
 	
 
 	@Override
 	public void onEditWordClicked(long wordId) {
 		if(dualPane){
 			Bundle data = new Bundle();
 			data.putLong(EditWordActivity.EXTRA_WORD_ID, wordId);
             Fragment f = new EditWordFragment();
             f.setArguments(data);
             FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
             ft.replace(R.id.right_column, f);
             ft.addToBackStack(null);
             ft.commit();
 		}else{
 	        Intent i = new Intent(this, EditWordActivity.class);
 	        i.putExtra(EditWordActivity.EXTRA_WORD_ID, wordId);
 	        startActivityForResult(i, REQUEST_EDIT_WORD);
 	    }
 		
 	}
 
 	
 	
 	@Override
 	public void saveEditedWord(long wordId, String question, String answer) {	
 		WordDBAdapter wordDBAdapter = new WordDBAdapter(this);
 		
 		boolean updated = false;
 		try {
 			updated = wordDBAdapter.updateWord(wordId, question, answer);
 		} catch (Exception e) {
 			Log.d(TAG, "ERROR: " + e.getMessage());
 		} finally {
 			wordDBAdapter.close();
 		}
 		
 		if(updated){
 			getWordListFragment().updateList();
 			showToastMessage(this,  R.string.saved_ok);
 		    if(dualPane){
 		    	showWord( wordId );
 		    }
 		}else{
 			showToastMessage(this,  R.string.saved_no);
 		}
 		
 	}
 
 	
 
 	
 	public long getLectureId(){
 		return lectureId;
 	}
 
 
 	
 	
 	@Override
 	public void showWord(long wordId) {
 		if(dualPane){
 			Bundle data = new Bundle();
 			data.putLong(ViewWordActivity.EXTRA_WORD_ID, wordId);
             Fragment f = new ViewWordFragment();
             f.setArguments(data);
             FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
             ft.replace(R.id.right_column, f, "rcTag");
             ft.addToBackStack(null);
             ft.commit();
 		}else{
 	        Intent i = new Intent(this, ViewWordActivity.class);
 	        i.putExtra(ViewWordActivity.EXTRA_WORD_ID, wordId);
 	        startActivityForResult(i, REQUEST_VIEW);
 	    }
 		
 	}
 
 
 	
 	
 	@Override
 	public void onDeleteClicked(long wordId) {
 		WordListFragment wlf = getWordListFragment();
 		wlf.deleteWord(wordId);
 		wlf.updateList();
 		if(dualPane){
 			ViewWordFragment f = getViewWordFragment();
 			if(f == null) throw new Error("ViewWordFragment not found.");
 			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 			ft.remove(f);
 			ft.commit();
 		}
 			
 	}
 
 
 	@Override
 	public void activeWord(long wordId) {
 		
 		changeWordStatus(wordId, ViewWordFragment.STATUS_ACTIVE);
 		
 	}
 
 	@Override
 	public void deactiveWord(long wordId) {
 		changeWordStatus(wordId,ViewWordFragment.STATUS_ACTIVE );
 		if(dualPane) 
 			getWordListFragment().updateList();
 		
 	}
 	
 	
 	
 	public void changeWordStatus(long wordId, int newStatusVal){
 		getViewWordFragment().setWordStatus(wordId, newStatusVal);
 		if(dualPane) 
 			getWordListFragment().updateList();
 	}
 	
 	
 	
 	public WordListFragment getWordListFragment(){
 		return ((WordListFragment) getSupportFragmentManager().findFragmentById(
 	            R.id.WordListFragment));
 	}
 	
 	
 	
 	public ViewWordFragment getViewWordFragment(){
 		return (ViewWordFragment) getSupportFragmentManager().findFragmentByTag("rcTag");
 	}
 	
 	
 	
 	public static void showToastMessage(Context ctx, int resourceId){
 		Toast.makeText(ctx, resourceId, Toast.LENGTH_LONG).show();
 	}
 	
 	/* OPTION MENU ---------------------------------------- */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	MenuInflater inflater = getMenuInflater();
 	inflater.inflate(R.menu.main_menu, menu);
 	return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    case R.id.menu_about:
 	        startActivity(new Intent(this, AboutActivity.class));
 	    	Log.d("MAINACTIVITY", "starting abotu...");
 	        return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
     /* ENDOPTION MENU ---------------------------------------- */
 }
