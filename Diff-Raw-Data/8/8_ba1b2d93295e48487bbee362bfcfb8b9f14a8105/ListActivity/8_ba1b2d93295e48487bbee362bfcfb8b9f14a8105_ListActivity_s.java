 package com.github.groupENIGMA.journalEgocentrique;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 
 import com.github.groupENIGMA.journalEgocentrique.model.DB;
 import com.github.groupENIGMA.journalEgocentrique.model.Entry;
 import com.github.groupENIGMA.journalEgocentrique.model.Note;
 
 public class ListActivity extends Activity {
 	
 	public final static String EXTRA_MESSAGE = "com.github.groupENIGMA.journalEgocentrique.MESSAGE";
 	private List<Calendar> menu;
 	private DB dataBase;
 	private Entry selectedEntry = null;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main);
 	    dataBase = new DB(getApplicationContext());
 	    dataBase.open();
 	    menu = dataBase.getDays();
 	    if(menu == null){
 	    	menu = new ArrayList<Calendar>();
 	    }
 	    if (savedInstanceState != null){
 	    	long idValue = savedInstanceState.getLong("ID");
 	    	selectedEntry = dataBase.getEntry(idValue);
 	    }
 
 	    ListView list = (ListView)findViewById(R.id.list);
 	    ListView notes = (ListView)findViewById(R.id.notes);
 	    
 	    setListView(list, menu);
 	    setImages(selectedEntry);
 	    Intent received;
 	    if(selectedEntry != null){
 	    	if((received = getIntent()) != null){
 	    		final String msg = received.getStringExtra(WriteNote.EXTRA_MESSAGE);
 				dataBase.insertNote(selectedEntry, msg);
 	    	}
 	    	setNotes(notes, selectedEntry);
 	    }
 	    }
 	
 	public void onSaveInstanceState(Bundle savedInstanceState){
 		if(selectedEntry != null){
 			long entryId = selectedEntry.getId();
 			savedInstanceState.putLong("ID", entryId);
 		}
 		super.onSaveInstanceState(savedInstanceState);
 	}
 	
 /**
  * Displays the correct notes for the entry selected by the user.
  * @param selected Entry. The entry selected by the user.
  */
 	private void setNotes(ListView list, Entry selected) {
         boolean editable = selected.canBeUpdated();
         List<Note> tmp = selected.getNotes();
         List<String> notes = new ArrayList<String>();
         for(int i = 0;i < tmp.size();i++){
         	notes.add(tmp.get(i).getText());
         }
 		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.row, notes);
         list.setAdapter(arrayAdapter);
         if(editable){
 	        OnItemLongClickListener clickListener = new OnItemLongClickListener() {
 	
 	            @Override
 	            public boolean onItemLongClick(AdapterView<?> adapter, View view,
 	                int position, long id) {
 	                Note modify = (Note)adapter.getItemAtPosition(position);
 	                Intent intent = new Intent(getApplicationContext(), WriteNote.class);//ho messo WriteNote.class
 	                intent.putExtra(EXTRA_MESSAGE, modify.getText());
 	                startActivity(intent);
 	                return true;
 	            }
 	        };
 	        list.setOnItemLongClickListener(clickListener);
         }
      }
 
 
 	/**
 	 * The ListView will be populated with the data given by the database.
 	 * It is also created a OnItemClickListener that at the click will display the details of the day.
 	 * @param list ListView. The list to populate.
 	 * @param entry List<Calendar> With this List we will populate the ListView 
 	 */
 	private void setListView(ListView list, List<Calendar> entry){
         ArrayAdapter<Calendar> arrayAdapter = new ArrayAdapter<Calendar>(this, R.layout.row, R.id.textViewList, entry);
         list.setAdapter(arrayAdapter);
         OnItemClickListener clickListener = new OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> adapter, View view,
                 int position, long id) {
                 selectedEntry = (Entry)adapter.getItemAtPosition(position);
             }
         };
         list.setOnItemClickListener(clickListener);
 	}
 	
 	
 	/**
 	 * Sets the correct image for photo and mood selected by the user.
 	 * @param selected Entry. The entry selected by the user, its details will be displayed.
 	 */
 	private void setImages(Entry selected){
 	   /*
 	    * Caso iniziale:nessun entry selezionata 
 	    */
 		if(selected == null){
     	    ImageView img = (ImageView) findViewById(R.id.dailyPhoto); 
     	    img.setImageResource(R.drawable.ic_launcher);
     	    img = (ImageView)findViewById(R.id.emoticon);
     	    img.setImageResource(R.drawable.ic_launcher);
         }
 	    /*
 	     * Caso entry selezionata dall'utente:
 	     * vengono aggiornate la foto, il mood e le note.
 	     * Controllando se e' possibile la modifica si permetta la stessa o meno
 	     */
         else{
     	    boolean editable = selected.canBeUpdated();
     	    ImageView img = (ImageView) findViewById(R.id.dailyPhoto); 
     	    img.setImageURI(Uri.parse(selected.getPhoto().getPath()));
     	    ImageView mood = (ImageView)findViewById(R.id.emoticon);
     	    mood.setImageResource((selected.getMood().getEmoteId(getApplicationContext())));
     	    if(editable){
     	    	 img.setOnTouchListener(new OnTouchListener()
     	         {
     	             @Override
     	             public boolean onTouch(View v, MotionEvent event)
     	             {
     	            	 // qui carica la vista per la fotoCamera
     	            	 Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);//ho messo PhotoActivity.class
     	            	 intent.putExtra(EXTRA_MESSAGE, selectedEntry.getId());
     	            	 startActivity(intent);
     	                 return false;
     	             }
     	        });
     	    	 mood.setOnTouchListener(new OnTouchListener()
     	    	 {
     	    		@Override
     	    		public boolean onTouch(View v, MotionEvent event)
     	    		{
     	    			// qui carica la vista per il moood
     	    			Intent intent = new Intent(getApplicationContext(), MoodActivity.class);//ho messo MoodActivity.class
     	    			intent.putExtra(EXTRA_MESSAGE, selectedEntry.getId());
     	    			startActivity(intent);
     	    			return false;
     	    		}
     	    	 });
     	    }
         }
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.option, menu);
 		return true;
 		
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.newEntry:
	            dataBase.createEntry();
 	            menu = dataBase.getDays();
 	    	    ListView list = (ListView)findViewById(R.id.list);
 	    	    setListView(list, menu);
 	            return true;
 	        case R.id.newNote:
 	            Intent intent = new Intent(getApplicationContext(), WriteNote.class);
 	            if(selectedEntry != null)
 	            	intent.putExtra(EXTRA_MESSAGE, selectedEntry.getId());
 	            startActivity(intent);
 	            return true;
 	        case R.id.settings:
 	        	Intent settings = new Intent(getApplicationContext(), Settings.class);
 	        	startActivity(settings);
 	        	return true;
 	    }
 		return false;
 	}
 }
