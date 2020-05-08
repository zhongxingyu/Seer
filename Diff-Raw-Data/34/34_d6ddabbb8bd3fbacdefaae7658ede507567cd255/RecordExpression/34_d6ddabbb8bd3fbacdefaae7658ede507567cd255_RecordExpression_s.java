 package com.doyouspeak;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class RecordExpression extends Activity implements OnItemSelectedListener {
 	Expression exp = new Expression();
 	ImageButton actionHome;
 	ImageButton actionList;
 	ImageButton actionRecord;
 	ImageButton recordButton;
 	ImageButton recordPlayButton;
 	ImageButton chooseCategoryButton;
 	Button setButton;
 	Button cancelButton;
 	EditText editExpression;
 	TextView recordText;
 	TextView recordPlayText;
 	
 	Intent i = null;
 	int flagRecord = 0;
 	int flagPlay = 0;
 	
 	Context context;
 	
 	long id_tmp;
 	int counter_record;
 	Cursor allRecords;
 	
 	String [] yourCategories = new String[9];
 	
 	String expression;
 	int category;
 	String path;
 	
 	MediaPlayer mp;
 	AudioRecorder recorder;
 	Model model;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.record_exp);
         
         context = getApplicationContext();
         model = new Model(context);
         
         allRecords = model.getAllRecords();
         if(allRecords.getCount()==0)
         	counter_record = 0;
         else {
         	allRecords.moveToLast();
         	counter_record = allRecords.getInt(0);
         }
         
         actionHome = (ImageButton)findViewById(R.id.actionHome);
         actionList = (ImageButton)findViewById(R.id.actionMyList);
         actionRecord = (ImageButton)findViewById(R.id.actionRecord);
         
         recordButton = (ImageButton)findViewById(R.id.recordButton);
         recordPlayButton = (ImageButton)findViewById(R.id.recordPlayButton);
        //chooseCategoryButton = (ImageButton)findViewById(R.id.chooseCategoryButton);
         setButton = (Button)findViewById(R.id.setButton);
         cancelButton = (Button)findViewById(R.id.cancelButton);
         editExpression = (EditText)findViewById(R.id.editExpression);
         recordText = (TextView)findViewById(R.id.recordText);
         recordPlayText = (TextView)findViewById(R.id.recordPlayText);
 
         actionHome.setOnClickListener(lHome);
         actionList.setOnClickListener(lList);
         actionRecord.setOnClickListener(lRecord);
         
         recordButton.setOnClickListener(lRecordButton);
         recordPlayButton.setOnClickListener(lRecordPlayButton);
        //chooseCategoryButton.setOnClickListener(lChooseCategoryButton);
         setButton.setOnClickListener(lSetButton);
         cancelButton.setOnClickListener(lCancelButton);

        /*Button save=(Button)findViewById(R.id.setButton);  
        save.setOnClickListener(onSave);*/
        
        /*model.insertCategory("Zakupy");
        model.insertCategory("Jedzenie");
        model.insertCategory("Czas");
        model.insertCategory("Rodzina");
        model.insertCategory("W hotelu");
        model.insertCategory("Na dworcu");
        model.insertCategory("Komunikacja");
        model.insertCategory("Pytania");
        model.insertCategory("Podoga");*/
         
         load();
         
         Spinner catSpinner = (Spinner) findViewById(R.id.chooseCategorySpinner);
         ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, yourCategories);
         catSpinner.setAdapter(spinnerArrayAdapter);
         
         catSpinner.setOnItemSelectedListener(this);
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		model.close();
 	}
 
 	
 	private View.OnClickListener lHome = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			i = new Intent(context, DoYouSpeak.class);
 			startActivity(i);
 		}
 	};
 
 	private View.OnClickListener lList = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			i = new Intent(context, MyList.class);
 			startActivity(i);
 		}
 	};
 
 	private View.OnClickListener lRecord = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			i = new Intent(context, RecordExpression.class);
 			startActivity(i);
 		}
 	};
 	
 	
 	//*********************************************************************************
 	
 	
 	private View.OnClickListener lRecordButton = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if(flagRecord==0) {
 				flagRecord = 1;	
 				recordText.setText("Nagrywanie...");
 				expression = editExpression.getText().toString();
 				counter_record++;
 				path = "/mnt/sdcard/doyouspeak/main/_"+counter_record+".3gp";
 				recorder = new AudioRecorder(path);
 
 				try {
 					recorder.start();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			else {
 				flagRecord = 0;
 				recordText.setText("Nagraj Siebie");
 				try {
 					recorder.stop();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 			}
 		}
 	};
 	
 	private View.OnClickListener lRecordPlayButton = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Cursor c = model.getRecordById(""+id_tmp); // na razie jest na stałe id_tmp
 			c.moveToFirst();
 			String path = model.getPath(c);
 			
 			play(context, path);
 		}
 	};
 	
 	
 	private View.OnClickListener lSetButton = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			id_tmp = model.insertRecord(expression, path, category); // na razie kategoria id 1
 			finish();
 		}
 	};
 	
 	private View.OnClickListener lCancelButton = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			finish();
 		}
 	};
 	
 	//**************************************************************************************
 	
 	
 	public void play(Context context, String path) {
 
 		if(flagPlay==0) {
 			flagPlay = 1;
 			String newPath = "file://"+path;
 			Log.d("PATH", newPath);
 			mp = MediaPlayer.create(context, Uri.parse(newPath));
 			try {
 				mp.prepare();
 			} catch (IllegalStateException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			mp.start();
 		}
 		else {
 			flagPlay = 0;
 			if (mp.isPlaying()) {
 				mp.stop();
 			}
 		}
 	}
 
 	public void recordMy(Context context, int id_rec, String btnText1, String btnText2, Button v) {
 		if(v.getText().toString().equals(btnText1)) {
 			v.setText(btnText2);	
 			Cursor c = model.getRecordById(""+id_rec);
 			c.moveToFirst();
 			String path = model.getPath(c);
 			Log.d("path", path);
 
 			path = path.substring(0, path.length()-4); //usunięcie .3gp
 			Log.d("path", path);
 
 			String newPath1;
 			String newPath2;
 			String newPath3;
 			String newPath;
 
 			c = model.getMyRecordByIdRec(""+id_rec);
 			c.moveToFirst();
 
 			if(c.getString(2).isEmpty()) {	
 				newPath = newPath1 = path+"_1.3gp";
 				newPath2 = "";
 				newPath3 = "";
 			}
 			else if (c.getString(3).isEmpty()) {
 				newPath1 = c.getString(2);
 				newPath = newPath2 = path+"_2.3gp";
 				newPath3 = "";
 			}
 			else if (c.getString(4).isEmpty()) {
 				newPath1 = c.getString(2);
 				newPath2 = c.getString(3);
 				newPath = newPath3 = path+"_3.3gp";
 			}
 			else {
 				newPath1 = c.getString(3);
 				newPath2 = c.getString(4);
 				newPath = newPath3 = c.getString(2);
 			}
 
 			Log.d("newPath", newPath);
 			Log.d("newPath1", newPath1);
 			Log.d("newPath2", newPath2);
 			Log.d("newPath3", newPath3);
 			Log.d("id", ""+c.getInt(0));
 			Log.d("id_rec", ""+id_rec);
 
 			recorder = new AudioRecorder(newPath);
 			id_tmp = model.updateMyRecord(""+c.getInt(0), id_rec, newPath1, newPath2, newPath3);
 
 			try {
 				recorder.start();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else {
 			v.setText(btnText1);
 			try {
 				recorder.stop();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void load() {
 		Cursor c = model.getAllCategories();
 		int i = 0;
 		c.moveToFirst();
 		
 		while(!c.isAfterLast()) {
 			yourCategories[i++] = model.getCategory(c);
 			c.moveToNext();
 		}	
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos,
 			long id) {
 		parent.getItemAtPosition(pos);
 		category = pos + 1;
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
