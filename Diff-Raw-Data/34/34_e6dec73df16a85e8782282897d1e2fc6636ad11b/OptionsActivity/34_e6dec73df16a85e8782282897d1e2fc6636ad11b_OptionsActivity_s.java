 package no.hist.aitel.android.tictactoe;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 public class OptionsActivity extends Activity {
 
     public static final String PREFS_NAME = "Prefs";
     private Spinner boardsizeSpinner;
    private Spinner inarowSpinner;
     private int boardSize;
    private int inarow;
    private ArrayAdapter<Integer> inarowAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.options);
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
         boardSize = settings.getInt("boardSize", 3);
         boardsizeSpinner = (Spinner) findViewById(R.id.spinner_boardsize);
         ArrayAdapter<CharSequence> boardSizeAdapter = ArrayAdapter.createFromResource(this, R.array.boardsize_array, android.R.layout.simple_spinner_item);
         boardSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         boardsizeSpinner.setAdapter(boardSizeAdapter);
         boardsizeSpinner.setOnItemSelectedListener(new BoardSizeSelectionListener());
         boardsizeSpinner.setSelection(boardSize - 3);
        inarow = settings.getInt("inarow", boardSize);
        inarowSpinner = (Spinner) findViewById(R.id.spinner_inarow);
        inarowAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item);
        inarowAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inarowSpinner.setAdapter(inarowAdapter);
        inarowSpinner.setOnItemSelectedListener(new InarowSelectionListener());
        inarowSpinner.setSelection(inarow - 3);
     }
 
     private class BoardSizeSelectionListener implements AdapterView.OnItemSelectedListener {
 
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
             boardSize = pos + 3;
             updateInarowAdapter();
         }
 
         public void onNothingSelected(AdapterView parent) {
         }
     }
 
     private class InarowSelectionListener implements AdapterView.OnItemSelectedListener {
 
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            inarow = (Integer) parent.getItemAtPosition(pos);
         }
 
         public void onNothingSelected(AdapterView parent) {
         }
     }
 
     private void updateInarowAdapter() {
        inarowAdapter.clear();
         for (int i = 3; i <= boardSize; i++) {
            inarowAdapter.add(new Integer(i));
         }
     }
 
     protected void onStop() {
         super.onStop();
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         SharedPreferences.Editor editor = settings.edit();
         editor.putInt("boardSize", boardSize);
        editor.putInt("inarow", inarow);
         editor.commit();
     }
 }
