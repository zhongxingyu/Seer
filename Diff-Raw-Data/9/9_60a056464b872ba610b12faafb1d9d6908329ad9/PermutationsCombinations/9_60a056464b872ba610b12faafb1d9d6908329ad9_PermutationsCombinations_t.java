 package com.aeidesign.statscalc;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.google.ads.*;
 
 import com.aeidesign.statscalc.stats.PermComb;
 
 public class PermutationsCombinations extends Activity {
 	private EditText setSize;
 	private EditText groupSize;
 	private EditText permVal;
 	private EditText permWithRep;
 	private EditText combVal;
 	private EditText combWithRep;
 	private EditText numSubset;
 	private EditText pigeonhole;
 	private Button calc;
 	
 	private OnClickListener clickCalc = new OnClickListener(){
 		public void onClick(View v) {
 		      calculateData();
 		}
 	};
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.permutations_combinations);
         
      // Look up the AdView as a resource and load a request.
         AdView adView = (AdView)this.findViewById(R.id.adView);
         adView.loadAd(new AdRequest());
         
         setSize = (EditText) findViewById(R.id.setSize);
         groupSize = (EditText) findViewById(R.id.groupSize);
         permVal = (EditText) findViewById(R.id.permVal);
         permWithRep = (EditText) findViewById(R.id.permWithRep);
         combVal = (EditText) findViewById(R.id.combVal);
         combWithRep = (EditText) findViewById(R.id.combWithRep);
         numSubset = (EditText) findViewById(R.id.numSubset);
         pigeonhole = (EditText) findViewById(R.id.pigeonhole);
         calc = (Button) findViewById(R.id.perComCalc);
         calc.setOnClickListener(clickCalc);       
     }
     
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
     
     public boolean onPrepareOptionsMenu(Menu menu){
     	menu.removeItem(R.id.mManageData);
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch ( item.getItemId() ) {
         	case R.id.mHelp:
         		Intent helpIntent = new Intent(this, Appendix.class);
         		helpIntent.putExtra("appendix_text_id", R.string.permcomb_help);
         		startActivity(helpIntent);
         		break;
         }
 		return true;
     }
     
     private void calculateData() {
     	long set = 0;
     	long group = 0;
    	
    	if ( setSize.getText().toString().equals("")){
    		Toast.makeText(getApplicationContext(), "Please enter a value for the set", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
     	if(groupSize.getText().toString().equals("")){
     		set = Long.parseLong(setSize.getText().toString());
         	group = Long.parseLong(setSize.getText().toString());
     	} else {
         	set = Long.parseLong(setSize.getText().toString());
         	group = Long.parseLong(groupSize.getText().toString());
     	}
     	
     	if ( group > set){
     		Toast.makeText(getApplicationContext(), "The group cannot be larger than the set.", Toast.LENGTH_SHORT).show();
     		return;
     	}
     	
    	
    	
     	permVal.setText(String.valueOf(PermComb.calcPermutations(set, group)));
     	permWithRep.setText(String.valueOf(PermComb.calcPermutationsWithRep(set, group)));
     	combVal.setText(String.valueOf(PermComb.calcCombinations(set, group)));
     	combWithRep.setText(String.valueOf(PermComb.calcCombinationsWithRep(set, group)));
     	numSubset.setText(String.valueOf(PermComb.calcNumSubset(set)));
     	pigeonhole.setText(String.valueOf(PermComb.calcPigeonhole(set, group)));
 	}
 
 }
