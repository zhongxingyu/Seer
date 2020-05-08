 package edu.sru.andgate.bitbot.tutorial;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.SlidingDrawer;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 import edu.sru.andgate.bitbot.Bot;
 import edu.sru.andgate.bitbot.R;
 import edu.sru.andgate.bitbot.customdialogs.IntroCustomDialog;
 import edu.sru.andgate.bitbot.graphics.NickGameActivity;
 import edu.sru.andgate.bitbot.interpreter.BotInterpreter;
 import edu.sru.andgate.bitbot.interpreter.InstructionLimitedVirtualMachine;
 import edu.sru.andgate.bitbot.interpreter.Test;
 import edu.sru.andgate.bitbot.tools.Constants;
 import edu.sru.andgate.bitbot.tools.FileManager;
 
 public class Main_Tutorial extends Activity
 {
 	private EditText editor; 
 	private TextView help_text;
 	private String tutorialID;
 	private int simulateFlag, numOfBots;
 	private InputStream xml;
 	private Tutorial myTutorial;
 	private ActionItem[] quick_tools, selection_shells, sequence_shells, iteration_shells, bot_functions;
 	private String[] quick_tools_titles, quick_tools_strings, sequence_shell_titles, 
 					 sequence_shell_strings, selection_shell_titles, selection_shell_strings,
 					 iteration_shell_titles, iteration_shell_strings, bot_function_titles, bot_function_strings;
 	private SlidingDrawer slidingDrawer;
 	private Animation sIn_left, sOut_left, sIn_right, sOut_right;
 	private TextView botOutput, main_text;
 	private Button slideHandleButton, to_code_button, back_to_code;
 	private ImageButton sequence_btn, selection_btn, iteration_btn, tools_btn, lock_btn, simulate_btn, bot_code;
 	private ViewFlipper vf;
 	private IntroCustomDialog icd;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tutorial_main);
 		
 		FileManager.setContext(getBaseContext());
 		
 		/*
 		 * recieves content sent from previous view for re-use
 		 */
 		tutorialID = getIntent().getExtras().getString("File_ID");
 		simulateFlag = getIntent().getExtras().getInt("Sim_Flag",0);
 		numOfBots = getIntent().getExtras().getInt("BotNum", 0);
 
 		xml = FileManager.readFile(tutorialID);
    	   	myTutorial = new Tutorial(xml);	
 			
 		botOutput = (TextView) findViewById(R.id.ide_std_out);
 		main_text = (TextView) findViewById(R.id.tutorial_text);
 		main_text.setText(FileManager.readAssetsXML(tutorialID,"text"));
 	
 		/*
 		 * create the text editor and cabinet button
 		 */
 		editor = (EditText) this.findViewById(R.id.editor);
 		editor.setTextSize(12.0f);
 		slidingDrawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);
 		slideHandleButton = (Button) findViewById(R.id.slideHandleButton);
 				
 		/*
 		 * sets the view flipper sider animations
 		 */
 		sIn_left = AnimationUtils.loadAnimation(this, R.anim.slidein_left);
 		sOut_left = AnimationUtils.loadAnimation(this, R.anim.slideout_left);
 		sIn_right = AnimationUtils.loadAnimation(this, R.anim.slidein_right);
 		sOut_right = AnimationUtils.loadAnimation(this, R.anim.slideout_right);
 		
 		/*
 		 * Action Items for Quick Tools
 		 */
 		quick_tools_titles = getResources().getStringArray(R.array.quick_tools_titles);
 		quick_tools_strings = getResources().getStringArray(R.array.quick_tools);
 		quick_tools = new ActionItem[quick_tools_titles.length];
 		for(int i  = 0; i < quick_tools.length; i++){
 			quick_tools[i] = new ActionItem();
 			setActionItem(quick_tools[i], editor, quick_tools_titles[i], quick_tools_strings[i]);
 		}
 		
 		/*
 		 * Action Items for Sequence, Selection, Iteration buttons
 		 */
 		sequence_shell_titles = getResources().getStringArray(R.array.sequence_shell_titles);
 		sequence_shell_strings = getResources().getStringArray(R.array.sequence_shell_strings);
 		sequence_shells = new ActionItem[sequence_shell_titles.length];
 		for(int i = 0; i < sequence_shells.length; i++){
 			sequence_shells[i] = new ActionItem();
 			setActionItem(sequence_shells[i], editor, sequence_shell_titles[i], sequence_shell_strings[i]);
 		}
 		
 		selection_shell_titles = getResources().getStringArray(R.array.selection_shell_titles);
 		selection_shell_strings = getResources().getStringArray(R.array.selection_shell_strings);
 		selection_shells = new ActionItem[selection_shell_titles.length];
 		for(int i = 0; i < selection_shells.length; i++){
 			selection_shells[i] = new ActionItem();
 			setActionItem(selection_shells[i], editor, selection_shell_titles[i], selection_shell_strings[i]);
 		}
 		
 		iteration_shell_titles = getResources().getStringArray(R.array.iteration_shell_titles);
 		iteration_shell_strings = getResources().getStringArray(R.array.iteration_shell_strings);
 		iteration_shells = new ActionItem[iteration_shell_titles.length];
 		for(int i = 0; i < iteration_shells.length; i++){
 			iteration_shells[i] = new ActionItem();
 			setActionItem(iteration_shells[i], editor, iteration_shell_titles[i], iteration_shell_strings[i]);
 		}
 		
 		/*
 		 * Action Items for Bot Functions button
 		 */
 		bot_function_strings = getResources().getStringArray(
 				R.array.bot_function_strings);
 		bot_function_titles = getResources().getStringArray(
 				R.array.bot_function_titles);
 		bot_functions = new ActionItem[bot_function_titles.length];
 		for (int i = 0; i < bot_functions.length; i++) {
 			bot_functions[i] = new ActionItem();
 			setActionItem(bot_functions[i], editor, bot_function_titles[i],
 					bot_function_strings[i]);
 		}
 		
 		/*
 		 * Set all the QuickAction buttons onClick() methods 
 		 */
 		sequence_btn = (ImageButton) this.findViewById(R.id.sequence_btn);
 		sequence_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				QuickAction qa = new QuickAction(v);
 				qa.setQuickTitle("Sequence");
 				for(int i = 0; i < sequence_shells.length; i++){
 					qa.addActionItem(sequence_shells[i]);
 				}
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 								
 		selection_btn = (ImageButton) this.findViewById(R.id.selection_btn);
 		selection_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				QuickAction qa = new QuickAction(v);
 				qa.setQuickTitle("Selection");
 				for(int i = 0; i < selection_shells.length; i++){
 					qa.addActionItem(selection_shells[i]);
 				}
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 		
 		iteration_btn = (ImageButton) this.findViewById(R.id.iteration_btn);
 		iteration_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				QuickAction qa = new QuickAction(v);
 				qa.setQuickTitle("Iteration");
 				for(int i = 0; i < iteration_shells.length; i++){
 					qa.addActionItem(iteration_shells[i]);
 				}
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 		
 		tools_btn = (ImageButton) this.findViewById(R.id.tools_btn);
 		tools_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				QuickAction qa = new QuickAction(v);
 				qa.setQuickTitle("Quick Tools");
 				for(int i = 0; i < quick_tools.length; i++){
 					qa.addActionItem(quick_tools[i]);
 				}
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 		
 		bot_code = (ImageButton) this.findViewById(R.id.bot_btn);
 		bot_code.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				QuickAction qa = new QuickAction(v);
 				qa.setQuickTitle("Bot Functions");
 				for (int i = 0; i < bot_functions.length; i++) {
 					qa.addActionItem(bot_functions[i]);
 				}
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 				
 		lock_btn = (ImageButton) this.findViewById(R.id.lock_btn);
 		lock_btn.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				if(tutorialID.equals("getting_started.xml")){
 					Toast.makeText(Main_Tutorial.this, "Not available in this Tutorial", Toast.LENGTH_SHORT).show();
 				}else{
 					try {
 						checkAnswer(editor.getText().toString(), myTutorial);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			
 			}
 		});
 		
 		simulate_btn = (ImageButton) this.findViewById(R.id.sim_btn);
 		simulate_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				if(simulateFlag == 0){
 					Toast.makeText(Main_Tutorial.this, "No Simulation Available" , Toast.LENGTH_SHORT).show();
 				}else if(simulateFlag == 1){
 					InterpreteCode();
 					
 					vf = (ViewFlipper) findViewById(R.id.tutorial_view_flipper);
 					
 					vf.setInAnimation(sIn_right);
 					vf.setOutAnimation(sOut_right);
 					
 					vf.showNext();
 				}else if (simulateFlag == 2){
 					//Graphical Simulation here
 					try{
 						Bot tutorialBot = new Bot();
 						tutorialBot.setName("Tutorial Bot");
 						tutorialBot.setBase(R.drawable.adambot);
 						tutorialBot.setTurret(R.drawable.adamturret);
 						tutorialBot.setBullet(R.drawable.bulletnew);
						if(editor.getText().toString() != null){
							tutorialBot.setCode(editor.getText().toString());
						}else{
							tutorialBot.setCode("//No Code Entered");
						}
 						tutorialBot.saveBotToXML(Main_Tutorial.this,"tutorial_bot.xml");
 						
 						Intent intent = new Intent(Main_Tutorial.this, NickGameActivity.class);
 						intent.putExtra("Bot", "tutorial_bot.xml");
 						intent.putExtra("BotNum", numOfBots);
 						intent.putExtra("GameType", "Tutorial");
 						startActivity(intent);
 					}catch(Exception e){
 						Toast.makeText(Main_Tutorial.this, "Please Enter Some Code Before Simulating", Toast.LENGTH_SHORT).show();
 					}
 					
 				}
 			}
 		});
 		
 		to_code_button = (Button) this.findViewById(R.id.code_btn);
 		to_code_button.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				vf = (ViewFlipper) findViewById(R.id.tutorial_view_flipper);
 				
 				vf.setInAnimation(sIn_right);
 				vf.setOutAnimation(sOut_right);
 				
 				vf.showNext();
 				
 				if(!Constants.hasShownBefore){
 					icd = new IntroCustomDialog("tutorial_intro.xml", Main_Tutorial.this, R.style.CustomDialogTheme);
 					icd.show();	
 					Constants.hasShownBefore = true;
 				}			
 			}
 		});
 		
 		back_to_code = (Button) this.findViewById(R.id.ide_back_to_code_btn);
 		back_to_code.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				vf = (ViewFlipper) findViewById(R.id.tutorial_view_flipper);
 
 				vf.setInAnimation(sIn_left);
 				vf.setOutAnimation(sOut_left);
 				
 				vf.showPrevious();
 			}
 		});
 		
 		/*
 		 * set the sliding drawer open listeners/handlers
 		 */
 		slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() 
 		{
 			@Override
 			public void onDrawerOpened() 
 			{
 				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 					slideHandleButton.setBackgroundResource(R.drawable.vertical_close_arrow);
 		        }else{
 		        	slideHandleButton.setBackgroundResource(R.drawable.closearrow);
 		        }
 					help_text = (TextView) findViewById(R.id.help_text);
 				try{
 					help_text.setText(myTutorial.getHint());
 				}catch(Exception e){
 					Log.v("BitBot", "Error setting hint text");
 				}
 			}
 		});
 
 		/*
 		 *  set sliding drawer closed listerner/handlers
 		 */
 		slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() 
 		{
 			@Override
 			public void onDrawerClosed() 
 			{
 				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 					slideHandleButton.setBackgroundResource(R.drawable.vertical_open_arrow);
 		        }else{
 		        	slideHandleButton.setBackgroundResource(R.drawable.openarrow);
 		        }
 			}
 		});
 		
 	}
 	
 	// Temp variable declaration
 	private InstructionLimitedVirtualMachine ilvm;
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.tutorial_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.clear:
 				editor.setText("");
 				break;
 				
 		}
 		return true;
 	}
 	
 	/*
 	 * creates the Action Item with the defined attributes: 
 	 * 		title, message string, text to be added when clicked
 	 */
 	private void setActionItem(ActionItem item, final EditText editor, String title, final String declaration)
 	{
 		item.setTitle(title);
 		item.setIcon(getResources().getDrawable(R.drawable.icon));
 		item.setOnClickListener(new OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				expandEditText(declaration);
 			}
 		});
 	}
 	
 	public void expandEditText(String append){
 		int start = editor.getSelectionStart();
 		int end = editor.getSelectionEnd();
 		editor.getText().replace(Math.min(start, end), Math.max(start, end),
 		       append);
 	}
 		
 	/*
 	 * inputs: File, Resource ID of Tutorial File
 	 * Output: User input to file, Toast to let user know if they were correct or not
 	 * Method to check if the user input matches the correct tutorial answer
 	 */
 	protected void checkAnswer(String userAnswer, Tutorial currTutorial) throws IOException 
 	{
 		/*
 		 * Temporary - Need to send strings(s) to interpreter and compare abstract 
 		 * 				Syntax Tree's
 		 */
 		userAnswer += "\n";
 		String temp2 = "";
 		
 		temp2 = currTutorial.getAnswer()+ "\n";
 		
 		//Let the user know if they are right or not.
 		if(Test.CompareCode(userAnswer, temp2))
 		{
 			int currStage = currTutorial.getStage();
 			currStage++;
 			String nextStage = "Correct Answer Stage: " + currStage + " Completed, Next Stage Available";
 			String lastStage = "Correct Anwser, All stages of this Tutorial are finished. Simulation Ready";
 			if(currTutorial.nextStage() == -1){
 				Toast.makeText(Main_Tutorial.this, lastStage,Toast.LENGTH_SHORT).show();
 				Constants.finished_tutorials.add(tutorialID);
 				if(simulateFlag ==  2){
 					FileManager.saveCodeFile("//" + FileManager.readAssetsXML(tutorialID, "title") + "\n" + userAnswer, "Tutorial: " + FileManager.readAssetsXML(tutorialID, "title"));
 				}
 			}else{
 				Toast.makeText(Main_Tutorial.this, nextStage,Toast.LENGTH_SHORT).show();
 			}
 			/*
 			 * call function to simulate code here if not using sim button...
 			 */
 		}else
 		{
 			Toast.makeText(Main_Tutorial.this,"Wrong Answer",Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	
 	
 	 /**
      * Temporary code to test the VM
      */
     private void InterpreteCode()
     {
     	Log.i("BitBot Interpreter", "----------------------------------------------------------");
     	Log.i("BitBot Interpreter", "--------------------- Begin Interpreter ------------------");
     	Log.i("BitBot Interpreter", Thread.currentThread().toString());
     	Log.i("BitBot Interpreter", "" + Thread.currentThread().getPriority());
     	Log.i("BitBot Interpreter", "----------------------------------------------------------");
     	BotInterpreter bi = null;
     	
 //    	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
     	
     	try
 		{
 	    	ilvm = new InstructionLimitedVirtualMachine();
 	    	
 	    	bi = new BotInterpreter(null, editor.getText().toString()+"\n");
 	    	bi.setOutputTextView(botOutput);
 	    	botOutput.setText(bi.getBotLog());
 	    	
 	    	ilvm.addInterpreter(bi);
 	    	ilvm.resume(2000);
 		}
     	catch (Exception e)
 		{
 			// TODO Auto-generated catch block
 			Log.e("BitBot Interpreter", e.getStackTrace().toString());
 //			botOutput.setText(e.toString());
 		}
     	catch (Error e)
     	{
     		Log.e("BitBot Interpreter", "ERROR: " + e.getStackTrace().toString());
 //    		botOutput.setText(e.toString());
     	}
     	finally
     	{
     		if (botOutput != null && bi != null)
     			botOutput.setText(bi.getBotLog());
     	}
     	
     	Log.i("BitBot Interpreter", "----------------------------------------------------------");
     	Log.i("BitBot Interpreter", "---------------------- End Interpreter -------------------");
     	Log.i("BitBot Interpreter", Thread.currentThread().toString());
     	Log.i("BitBot Interpreter", "----------------------------------------------------------");
     }
 }
