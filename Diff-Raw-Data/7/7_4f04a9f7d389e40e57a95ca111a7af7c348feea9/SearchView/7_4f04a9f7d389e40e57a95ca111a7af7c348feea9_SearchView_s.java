 package com.fsu.kevinfriedpig;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.EditText;
 
public class SearchView extends LoadingView {
 
 	EditText searchEditText;
 	Button calculateButton;
 	
 	int distance = -1;
 	String [] trace = new String [20]; // evens should be actors and odds movies
 	String baseActor = "Kevin Bacon";
	int baseActorNum = string2number(baseActor);
 	
 	@Override
 	   protected void onCreate(Bundle savedInstanceState) {
 	      super.onCreate(savedInstanceState);
 	 
 	      this.requestWindowFeature(Window.FEATURE_NO_TITLE);    // Removes title bar
 	      this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);    // Removes notification bar
 	 
 	      searchEditText = (EditText)findViewById(R.id.editTextSearch);
 	      calculateButton = (Button)findViewById(R.id.buttonCalculate);
 	      
 	      setContentView(R.layout.search_view);
 
 	   }
 	
 	public void Reset () { // used to reset distance to -1 and reset other variables
 		distance = -1;
 	}
 	
 	public void DistanceCalc () { // function called on button click
 		String actor = new String();
 		actor = searchEditText.getText().toString();
 		
 		MovieDistance(actor);
 		
 	}
 	
 	public void MovieDistance (String actor) { //function to find the moviedistance from actor to the baseActor (Kevin Bacon)
 		int currNum = 0; // = s2n.get(actor);
 		int cnt = 0;
 		
 		while(currNum != baseActorNum)
 		{
 			//trace[cnt] = n2s.get(currNum);
 			++cnt;
 			
 			//currNum == parent.get(currNum);
 		}
 		if (currNum == baseActorNum)
 			trace[cnt] = "0";
 			//trace[cnt] = n2s.get(currNum);
 		else
 			Log.w("ELSE", "trace didn't follow correctly");
 		
 		openResults(cnt, trace);
 	}
 	
 	public void openResults (int totDistance, String[] trace) {
 		
 	}
 	
 }
