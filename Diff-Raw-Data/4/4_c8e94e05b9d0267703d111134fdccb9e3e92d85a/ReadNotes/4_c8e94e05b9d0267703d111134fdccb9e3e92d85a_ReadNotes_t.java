 package ydirson.notestrainer;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.LinearLayout;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 import ydirson.notestrainer.ScoreView;
 
 public class ReadNotes extends Activity {
     boolean _started = false;
     ScoreView _scoreview;
     Random _rng;
     int _currentNote = -1;
     Chronometer _chrono;
     SharedPreferences _sharedPrefs;
 
     // tunable params
     String[] _displayedNoteNames;
     int _noteMin, _noteMax;
 
     // constants
     String[] noteNames =
         new String[] { "A", "B", "C", "D", "E", "F", "G" };
     List<String> noteNamesList = Arrays.asList(noteNames);
 
     String[] noteNames_latin =
         new String[] { "la", "si", "do", "re", "mi", "fa", "sol" };
     String[] noteNames_german =
         new String[] { "A", "H", "C", "D", "E", "F", "G" };
 
     final String TAGPREFIX = "note_";
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.game);
 
         // create the score widget
         LinearLayout score = (LinearLayout) findViewById(R.id.score);
         _scoreview = new ScoreView(this);
         score.addView(_scoreview);
 
         // preferences
         PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
         _sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 
         // remainder
         _chrono = (Chronometer) findViewById(R.id.chrono);
         _rng = new Random();
     }
 
     @Override
     public void onStart() {
         super.onStart();
         // Separate from onCreate to force update when we get back
         // from the preference Activity
         setupNoteButtons();
 
         _noteMin = _sharedPrefs.getInt("pref_minnote", 14); // A3
         _noteMax = _sharedPrefs.getInt("pref_maxnote", 30); // C5
     }
 
     int _randomNote() {
         int note;
         do {
             note = _noteMin + _rng.nextInt(_noteMax - _noteMin + 1);
         } while (note == _currentNote);
         return note;
     }
 
     public void onStartStop(View view) {
         if (!_started) {
             _currentNote = _randomNote();
             _started = true;
            _chrono.setBase(SystemClock.elapsedRealtime());
             _chrono.start();
         } else {
             _currentNote = -1;
             _started = false;
             _chrono.stop();
         }
         _scoreview.setNote(_currentNote);
     }
 
     public void onChooseNote(View view) {
         Button b = (Button)view;
         String tag = (String) b.getTag();
         if (! tag.startsWith(TAGPREFIX))
             // FIXME should log error
             return;
         int note_idx = noteNamesList.indexOf(tag.substring(TAGPREFIX.length()));
         if (note_idx == _currentNote % 7) {
             _currentNote = _randomNote();
             _scoreview.setNote(_currentNote);
         }
     }
 
     public void setupNoteButtons() {
         // notation to use
         String notation = _sharedPrefs.getString("pref_notation", "english");
         if (notation.equals("english")) _displayedNoteNames = noteNames;
         else if (notation.equals("latin")) _displayedNoteNames = noteNames_latin;
         else if (notation.equals("german")) _displayedNoteNames = noteNames_german;
         // else FIXME
 
         // note button labels
         LinearLayout main = (LinearLayout) findViewById(R.id.main);
         for (int noteIdx = 0; noteIdx < noteNames.length; noteIdx++) {
             final String tag = TAGPREFIX + noteNames[noteIdx];
             Button b = (Button) main.findViewWithTag(tag);
             if (b != null)
                 b.setText(_displayedNoteNames[noteIdx]);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.action_items, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent intent = new Intent(this, Prefs.class);
         startActivity(intent);
         return true; // consumed
     }
 }
