 package com.ohhaiku;
 
 import java.sql.SQLException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.j256.ormlite.dao.Dao;
 import com.ohhaiku.database.DatabaseHelper;
 import com.ohhaiku.models.Haiku;
 import com.ohhaiku.models.Poem;
 import com.ohhaiku.Constants;
 
 /*
  * Main activity: shows the Haiku composition window.
  */
 
 
 public class HaikuCompositionActivity extends OrmLiteBaseActivity<DatabaseHelper> {
   private static final String logTag = "HaikuComposition";
   private static final int DEFAULT_VALUE = -1;
   
   private Poem poem;
 
   /** Called when the activity is first created. */	
 	@Override
   public void onCreate(Bundle savedInstanceState) {
   	super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
   }
 
   public void goToMenu(View view) {
     startActivityForResult(new Intent(this, MenuActivity.class), Constants.LOAD_HAIKU);
 	}
   
   public void onCheck(View view) {
     Poem p = new Poem(getLines());
     Haiku h = new Haiku(p);
     boolean valid = h.isValid();
     if (valid) {
       setStatus(getString(R.string.certified_text));
     } else {
       setStatus(getString(R.string.not_certified_haiku_text));
     }
   }
   
   private TextView[] getTextViews() {
     TextView[] views = new TextView[3];
     
     // Fetch textviews containing the poem lines
     views[0] = (TextView) this.findViewById(R.id.editTextRow1);
     views[1] = (TextView) this.findViewById(R.id.editTextRow2);
     views[2] = (TextView) this.findViewById(R.id.editTextRow3);
     
     return views;
   }
   
   public String[] getLines() {
     TextView[] views = getTextViews();
     
     String[] lines = new String[3];
     
     for(int i = 0; i < 3; i++) {
       lines[i] = views[i].getText().toString();
     }
     
     return lines;
   }
   
   public void setLines(String[] lines) {
     TextView[] views = getTextViews();
     for (int i = 0; i < 3; i++) {
      views[i].setText(lines[i]);
     }
   }
 	
 	// Click handlers
 	public void saveHaiku(View view) {
 	  Poem p = new Poem(getLines());
 	  
 	  // Try to persist poem
 	  try {
 	    Dao<Poem, Integer> poemPersister = getHelper().getPoemDao();
       poemPersister.create(p);
       setStatus(getString(R.string.save_succeeded_text));
     } catch (SQLException e) {
       Log.e(logTag, "Could not save Haiku", e);
       setStatus(getString(R.string.save_failed_text));
     } 
 	}
 
   private void setStatus(String status) {
     TextView statusView = (TextView) this.findViewById(R.id.statusText);
     statusView.setText(status);
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     if (resultCode == Activity.RESULT_OK && requestCode == Constants.LOAD_HAIKU) {
       int id = data.getIntExtra(Constants.HAIKU_ID, DEFAULT_VALUE);
       if (id != DEFAULT_VALUE) {
         loadHaiku(id);
       } else {
         setStatus(getString(R.string.haiku_load_failed_text));
       }
     }
   }
 
   private void loadHaiku(int id) {
     try {
       Dao<Poem, Integer> dao = getHelper().getPoemDao();
       Poem p = dao.queryForId(id);
       setPoem(p);
     } catch (SQLException e) {
       setStatus(getString(R.string.haiku_load_failed_text));
     }
   }
   
   private void setPoem(Poem poem) {
     this.poem = poem;
    Log.i(logTag, poem.toString());
     setLines(poem.getLinesAsArray());
     setStatus(getString(R.string.haiku_loaded_text));
     setPersistButtonText(getString(R.string.update_button_title));
   }
   
  @SuppressWarnings("unused")
   private void onClear(View v) {
    poem = null;
     setLines(new String[] {"", "", ""});
     setPersistButtonText(getString(R.string.save_button_title));
   }
 
   private void setPersistButtonText(String text) {
     Button b = (Button) findViewById(R.id.SaveButton);
     if (b != null) {
       b.setText(text);
     }
   }
 }//HaikuCompositionActivity
