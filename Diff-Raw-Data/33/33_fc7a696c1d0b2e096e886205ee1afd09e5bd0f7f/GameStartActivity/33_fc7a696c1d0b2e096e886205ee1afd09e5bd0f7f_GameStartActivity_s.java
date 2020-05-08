 package com.ktx.game.dealornodeal;
 
 import java.util.ArrayList;
<<<<<<< HEAD
=======
 import java.util.HashMap;
>>>>>>> origin/master
 
 import com.ktx.game.dealornodeal.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.CompoundButton;
 import android.widget.ImageButton;
 import android.widget.ToggleButton;
 
<<<<<<< HEAD
public class GameStartActivity extends Activity {
	private ImageButton case1, case2, case3, case4, case5, case6, case7, case8, case9, case10, case11, case12, case13, case14, case15, case16, case17, case18, case19, case20, case21, case22, case23, case24, case25, case26;

=======
 
 public class GameStartActivity extends Activity {
 	
 	private HashMap<Integer, ImageButton> buttonMap = new HashMap<Integer, ImageButton>();
 	static private HashMap<Integer, Integer> buttonIdMap;
 	static {
 		buttonIdMap = new HashMap<Integer, Integer>();
 		buttonIdMap.put(1, R.id.case1);
 		buttonIdMap.put(2, R.id.case2);
 		buttonIdMap.put(3, R.id.case3);
 		buttonIdMap.put(4, R.id.case4);
 		buttonIdMap.put(5, R.id.case5);
 		buttonIdMap.put(6, R.id.case6);
 		buttonIdMap.put(7, R.id.case7);
 		buttonIdMap.put(8, R.id.case8);
 		buttonIdMap.put(9, R.id.case9);
 		buttonIdMap.put(10, R.id.case10);
 		buttonIdMap.put(11, R.id.case11);
 		buttonIdMap.put(12, R.id.case12);
 		buttonIdMap.put(13, R.id.case13);
 		buttonIdMap.put(14, R.id.case14);
 		buttonIdMap.put(15, R.id.case15);
 		buttonIdMap.put(16, R.id.case16);
 		buttonIdMap.put(17, R.id.case17);
 		buttonIdMap.put(18, R.id.case18);
 		buttonIdMap.put(19, R.id.case19);
 		buttonIdMap.put(20, R.id.case20);
 		buttonIdMap.put(21, R.id.case21);
 		buttonIdMap.put(22, R.id.case22);
 		buttonIdMap.put(23, R.id.case23);
 		buttonIdMap.put(24, R.id.case24);
 		buttonIdMap.put(25, R.id.case25);
 		buttonIdMap.put(26, R.id.case26);
 	}
 	
>>>>>>> origin/master
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.gamestart);
		
<<<<<<< HEAD
		
		
		case1 = (ImageButton) findViewById(R.id.case1);
		case1.setOnClickListener(new View.OnClickListener() {
 
			@Override
			public void onClick(View v) {
				case1.setImageResource(R.drawable.open_case2);
			}
		});
		
		
		
		
=======
 		for (HashMap.Entry<Integer, Integer> entry : buttonIdMap.entrySet()) {
 		    Integer suitCaseId = entry.getKey();
 		    Integer imageButtonId = entry.getValue();
 		    final ImageButton suitcase = (ImageButton) findViewById(imageButtonId.intValue());
 		    buttonMap.put(suitCaseId, suitcase);
 		    
 			suitcase.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					suitcase.setImageResource(R.drawable.open_case2);
 				}
 			});
 		}

>>>>>>> origin/master
 	}
 }
