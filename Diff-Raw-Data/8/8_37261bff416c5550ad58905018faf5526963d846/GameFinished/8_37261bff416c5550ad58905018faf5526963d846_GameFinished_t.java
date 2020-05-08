 package com.example.memory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 public class GameFinished extends Activity {
 
 	String name;
 	int movements;
 	int time;
 	int difficulty;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game_finished);
 		Intent intent = getIntent();
 		movements = intent.getIntExtra("movements", 0);
 		time = intent.getIntExtra("time", 0);
 		difficulty = intent.getIntExtra("difficulty", 0);
 		String stringMovements = "Movements: " + movements;
 		String stringTime = "Time: " + time;
 		TextView mov = (TextView) findViewById(R.id.textMovements);
 		TextView tim = (TextView) findViewById(R.id.textTime);
 		mov.setText(stringMovements);
 		tim.setText(stringTime);
 
 		EditText textName = (EditText) findViewById(R.id.editName);
 		textName.setText("Name");
 		textName.setOnKeyListener(new OnKeyListener() {
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if ((event.getAction() == KeyEvent.ACTION_DOWN)
 						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
 					saveButtonClick(v);
 				}
 				return false;
 			}
 		});
 
 	}
 
	public void onBackPressed(){
		finish();
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.putExtra("temp", false);
		startActivity(intent);
	}
	
 	public void menuButtonClick(View view) {
 		finish();
 		Intent intent = new Intent(this, MainActivity.class);
 		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 		intent.putExtra("temp", false);
 		startActivity(intent);
 	}
 
 	public void saveButtonClick(View view) {
 
 		EditText textName = (EditText) findViewById(R.id.editName);
 		name = textName.getText().toString();
 		if (textName.length()>2 ) {
 			SharedPreferences settings = getSharedPreferences("hiscores", 0);
 			SharedPreferences.Editor editor = settings.edit();
 			List<String> tab1 = new ArrayList<String>();
 			List<Integer> tab2 = new ArrayList<Integer>();
 			List<Integer> tab3 = new ArrayList<Integer>();
 			int size = 3;
 			for (int i = 1; i <= 3; i++) {
 				String key1 = "" + difficulty + i + 1;
 				String key2 = "" + difficulty + i + 2;
 				String key3 = "" + difficulty + i + 3;
 				String tempName = settings.getString(key1, "");
 				int tempTime = settings.getInt(key2, 0);
 				int tempMov = settings.getInt(key3, 0);
 				if (tempName != "") {
 					tab1.add(tempName);
 					tab2.add(tempTime);
 					tab3.add(tempMov);
 				} else {
 					size = i;
 					break;
 				}
 			}
 			boolean wpisano = false;
 			for (int i = 0; i < size - 1; i++) {
 				if (tab2.get(i) > time) {
 					tab1.add(i, name);
 					tab2.add(i, time);
 					tab3.add(i, movements);
 					wpisano = true;
 					break;
 				}
 			}
 			if (!wpisano) {
 				tab1.add(name);
 				tab2.add(time);
 				tab3.add(movements);
 			}
 			for (int i = 1; i <= size; i++) {
 				String key1 = "" + difficulty + i + 1;
 				String key2 = "" + difficulty + i + 2;
 				String key3 = "" + difficulty + i + 3;
 				editor.putString(key1, tab1.get(i - 1));
 				editor.putInt(key2, tab2.get(i - 1));
 				editor.putInt(key3, tab3.get(i - 1));
 			}
 			editor.commit();
 
 			finish();
 			Intent intent = new Intent(this, MainActivity.class);
 			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 			intent.putExtra("temp", false);
 			startActivity(intent);
 		}else{
 			Toast.makeText(getApplicationContext(), "Your name is too short!", Toast.LENGTH_SHORT).show();
 		}
 	}
 }
