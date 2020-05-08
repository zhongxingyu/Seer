 package com.example.test1;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.TimeZone;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 
 public class TodayPage extends Activity
 {
 	private SimpleDateFormat date;
 	int[] emotions = new int[]{0, 0, 0};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.todaypagelayout);
 		
 		//Button MorningSet = (Button) findViewById(R.id.button1);
 		Button MorningChange = (Button) findViewById(R.id.morningChange);
 		
 		//Button AfternoonSet = (Button) findViewById(R.id.button2);
 		Button AfternoonChange = (Button) findViewById(R.id.afternoonChange);
 		
 		//Button EveningSet = (Button) findViewById(R.id.button3);
 		Button EveningChange = (Button) findViewById(R.id.eveningChange);
 		
 		ArrayList<Button> buttonsToFeelingSelect = new ArrayList<Button>();
 		//buttonsToFeelingSelect.add(MorningSet);
 		buttonsToFeelingSelect.add(MorningChange);
 		//buttonsToFeelingSelect.add(AfternoonSet);
 		buttonsToFeelingSelect.add(AfternoonChange);
 		//buttonsToFeelingSelect.add(EveningSet);
 		buttonsToFeelingSelect.add(EveningChange);
 		
 		OnClickListener toFeelingSelect = new OnClickListener(){
 			public void onClick(View v)
 			{
 				Intent nextIntent = new Intent(TodayPage.this, Set_Emotions.class);
 				startActivityForResult(nextIntent, v.getId());
 			}
 		};
 		
 		for (Button b : buttonsToFeelingSelect)
 		{ b.setOnClickListener(toFeelingSelect); }
 			
 		date = new SimpleDateFormat("E - mm-dd", Locale.US);
 		date.setTimeZone(TimeZone.getTimeZone("PST"));
 		TextView dateText = new TextView(this);
 		dateText.setGravity(Gravity.CENTER);
 		dateText.setText("Today - " + date);
 		setContentView(dateText);		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.today_page, menu);
 		return true;
 	}
	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		Button pressed = (Button)findViewById(requestCode);
 		int emotion = (Integer)data.getExtras().get("emotionSelected");	
 	}
 	
 	protected void onStart()
 	{
 		super.onStart();
 	}
 	
 	
 	protected void onRestart()
 	{
 		super.onRestart();
 	}
 	
 	protected void onStop()
 	{
 		super.onStop();
 	}
 	
 	protected void onPause()//save data!
 	{
 		super.onResume();
 		String FILENAME = date.toString();
 		String string = "information to add";
 
 		FileOutputStream fos;
 		try {
 			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
 			fos.write(string.getBytes());
 			fos.close();
 		} catch (FileNotFoundException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();}
 	}
 	
 	protected void onResume()//load what changed
 	{
 		super.onResume();
 	}
 	
 	protected void onDestroy()
 	{
 		super.onDestroy();
 	}
 	//a change
 	protected void reply_click(ImageButton b)
 	{
 	     //Set_Emotions.ImageButtonToChange = b;
 	}
 	
 	public static int findImageToUse(int i )
 	{
 		//0 = startIcon, 1 = happy, 2 = sad, 3 = angry, 4 = shy, 5 = sick, 6 = excited
 		//in order to add new emotions just set an integer value with the new image
 		switch(i)
 		{
 		case 0: return (R.drawable.ic_launcher);
 		case 1: return (R.drawable.happy);
 		case 2: return (R.drawable.sad);
 		case 3: return (R.drawable.angry);
 		case 4: return (R.drawable.shy);
 		case 5: return (R.drawable.sick);
 		case 6: return (R.drawable.excited);
 		}
 		
 		return -1;
 		
 		
 	
 	}
 
 }
