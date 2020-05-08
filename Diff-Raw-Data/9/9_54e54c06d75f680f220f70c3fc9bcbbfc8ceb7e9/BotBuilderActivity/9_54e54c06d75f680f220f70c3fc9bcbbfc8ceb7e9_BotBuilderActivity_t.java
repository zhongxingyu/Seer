 package edu.sru.andgate.bitbot.ide.botbuilder;
 
 import edu.sru.andgate.bitbot.Bot;
 import edu.sru.andgate.bitbot.R;
 import edu.sru.andgate.bitbot.graphics.BotLayer;
 import edu.sru.andgate.bitbot.graphics.DrawableBot;
 import edu.sru.andgate.bitbot.graphics.GameActivity;
 import edu.sru.andgate.bitbot.interpreter.SourceCode;
 import edu.sru.andgate.bitbot.tools.Constants;
 import edu.sru.andgate.bitbot.tools.FileManager;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class BotBuilderActivity extends Activity
 {
 	private Bot _currentBot;
 	private Constants constant;
 	private BotComponentView c, t;
 	private TextView tv;
 	private Spinner spinner;
 	private String[] code_files;
 	ArrayAdapter<String> adapter;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ide_botbuilder_main);
 		
 		FileManager.setContext(getBaseContext());
 		c = (BotComponentView)findViewById(R.id.bb_chassis);
 		t = (BotComponentView)findViewById(R.id.bb_turret);
			
 		c.setTitle("Square Chassis");
 		c.setSummary("A stable base that is fast and sturdy.");
 		c.setPicID(R.drawable.spinnerbase);
 		
 		t.setTitle("Basic Turret");
 		t.setSummary("A turret for shooting stuff.");
 		t.setPicID(R.drawable.spinnerturret);
 		
 		code_files = FileManager.getFileNamesInDir(getDir("Code",Context.MODE_PRIVATE).getPath());
 		
 		spinner = (Spinner) findViewById(R.id.program_title);
 		adapter = new ArrayAdapter<String>(this, R.layout.spinner_line, code_files);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);  
 		
 		tv = (TextView) findViewById(R.id.program_text);
 		spinner.setOnItemSelectedListener(new OnItemSelectedListener()
 		{
 			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) 
 			{
 				tv.setText(FileManager.readTextFileFromDirectory("Code",((String)((TextView) view).getText().toString())));
 			}
 			
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0)
 			{
 				// TODO Auto-generated method stub
 			}
 		});
 		
 		final Button b = (Button) findViewById(R.id.bb_soft1);
 		b.setTag("Square Chassis");
 		b.setText(b.getTag().toString());
 		b.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.spinnerbase), null, null,null);
 		b.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Log.v("BitBot", b.getTag().toString());
 			}
 		});
 		
 		
 	}
 	
 	/**
 	 * Sets the code associated with the bot
 	 * @param sc a SourceCode object.
 	 */
 	public void setProgram(SourceCode sc)
 	{
 		if (_currentBot != null )
 			_currentBot.attachSourceCode(sc);
 	}
 	
 	/**
 	 * Start the mission
 	 * @param v the view that called this.
 	 */
 	public void begin(View v)
 	{
 		//Save bot to xml before going to graphics - Possible load from xml inside graphics
 		_currentBot = new Bot();
 		_currentBot.setName("Bot"); //for now, need to get the name from the textview?
 		constant = new Constants();
 		_currentBot.setBase(constant.reverse_base_table.get(c.getPicID()));
 		Log.v("BitBot", _currentBot.getBase());
 		_currentBot.setTurret(constant.reverse_turret_table.get(t.getPicID()));
 		Log.v("BitBot", _currentBot.getTurret());
 		_currentBot.setCode(tv.getText().toString());
 		Log.v("BitBot", _currentBot.getCode().getCode());
 		_currentBot.saveBotToXML(this.getBaseContext(), "test_bot.xml");
 				
 		// Start up the game engine
 		Intent engineIntent = new Intent(BotBuilderActivity.this, GameActivity.class);
 		engineIntent.putExtra("Bot", "test_bot.xml");
 		startActivity(engineIntent);
 		
 	}
 	
 }
