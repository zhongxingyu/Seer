 package com.mitfahr;
 
 import com.mitfahr.R.layout;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class ActivityTrip extends Activity {
 	
 	private String from;
 	private String to;
 	private String date;
 	private String time;
 	private Integer seats;
 	private String desc;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_trip);
 		
 		
 		Button button_weiter = (Button) findViewById(R.id.btn_weiter);
 		
 		button_weiter.setOnClickListener(new OnClickListener()
 	    {
 	      public void onClick(View v)
 	      {
 <<<<<<< .merge_file_3HtkG4
 	    	 Intent inte = new Intent(ActivityTrip.this, ActivityCheckTrip.class);
 	    	 try{
 	    	  final EditText m_edit_from = (EditText) findViewById(R.id.txt_trip_von);
 	    	  from = m_edit_from.getText().toString();
 	    	 
 	    	  final EditText m_edit_to = (EditText) findViewById(R.id.txt_bis);
 	    	  to = m_edit_to.getText().toString();
 	    	 
 	    	  final EditText m_edit_date = (EditText) findViewById(R.id.txt_date);
 	    	  date = m_edit_date.getText().toString();
 	    	  
 	    	  final EditText m_edit_time = (EditText) findViewById(R.id.txt_time);
 	    	  time = m_edit_time.getText().toString();
 	    	  
 	    	  final EditText m_edit_seats = (EditText) findViewById(R.id.txt_size);
 	    	  seats = Integer.parseInt(m_edit_seats.getText().toString());
 	    	  
 	    	  final EditText m_edit_desc = (EditText) findViewById(R.id.txt_beschreibung);
 	    	  desc = m_edit_desc.getText().toString();
 	    	  
 	    	  
 	    	Log.d("DEBUG ActivityTrip: ", from);
 	    	Log.d("DEBUG ActivityTrip: ", to);
 	    	Log.d("DEBUG ActivityTrip: ", date);
 	    	Log.d("DEBUG ActivityTrip: ", time);
 	    	Log.d("DEBUG ActivityTrip: ", seats.toString());
 	    	Log.d("DEBUG ActivityTE.toString()rip: ", desc);
 	    	
 	    	if(from.length() < 1|| to.length() < 1 || date.length() < 1 ||time.length() < 1  || seats.toString().length() < 1 )
 	    	{
 	    		Toast.makeText(v.getContext(),"Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
 	    	}
 	    	else
 	    	{
 	    		inte.putExtra("from", from);
 	    		inte.putExtra("to", to);
 	    		inte.putExtra("date", date);
 	    		inte.putExtra("time", time);
 	    		inte.putExtra("seats", seats.toString());
 	    		inte.putExtra("desc", desc);
 	    		
 	    		ActivityTrip.this.startActivity(inte);
 	    	}
 	    	  
 	    	}
 	    	 catch(Exception E){
 	              Toast.makeText(v.getContext(),"Sitzplätze dürfen nicht leer sein", Toast.LENGTH_SHORT).show();
 	          }
 =======
 	    	  
 	    	  try{
 	    	 //Intent inte = new Intent(ActivityTrip.this, ActivityCheckInput.class);
 	    	 
 	    	  final EditText edit_text_von = (EditText) findViewById(R.id.txt_trip_von);
 	    	  from = edit_text_von.getText().toString();
 	    	  final EditText edit_text_bis = (EditText) findViewById(R.id.txt_bis);
 	    	  to = edit_text_bis.getText().toString();
 	    	
 	    	  final EditText edit_text_date = (EditText) findViewById(R.id.txt_date);
 	    	  date = edit_text_date.getText().toString();
 	    	  
 	    	  final EditText edit_text_time= (EditText) findViewById(R.id.txt_time);
 	    	  time = edit_text_time.getText().toString();
 	    	  
 	    	  final EditText edit_text_seats = (EditText) findViewById(R.id.txt_size);
 	    	  seats = Integer.parseInt(edit_text_seats.getText().toString());
 	    	  
 	    	  final EditText edit_text_desc = (EditText) findViewById(R.id.txt_beschreibung);
 	    	  desc = edit_text_desc.getText().toString();
 	    	  
 	    	  Log.d("DebugActivityTrip_von: ", from);
 	    	  Log.d("DebugActivityTrip_bis: ", to);
 	    	  Log.d("DebugActivityTrip_date: ", date);
 	    	  Log.d("DebugActivityTrip_time: ", time);
 	    	  Log.d("DebugActivityTrip_seats: ", seats.toString());
 	    	  Log.d("DebugActivityTrip_desc: ", desc);
 	    	  
 	    	  
 	    	  
 	    	 Toast.makeText(getApplicationContext(),to, Toast.LENGTH_LONG).show();
 >>>>>>> .merge_file_9pJ634
 	    	 
 	    	 //ActivityTrip.this.startActivity(inte);
 	    	 
 	    	  }
 	    	  
 	    	  catch(Exception E){
 	              Toast.makeText(v.getContext(),E.toString(), Toast.LENGTH_SHORT).show();
 	          }
 	      }
 	    });
 		
 	 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_trip, menu);
 		return true;
 	}
 
 	
 	
 
 }
