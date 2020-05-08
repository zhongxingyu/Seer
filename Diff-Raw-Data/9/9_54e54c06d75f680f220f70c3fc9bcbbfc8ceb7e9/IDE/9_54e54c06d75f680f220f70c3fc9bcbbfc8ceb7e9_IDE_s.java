 package edu.sru.andgate.bitbot.ide;
 
 import edu.sru.andgate.bitbot.R;
 import edu.sru.andgate.bitbot.interpreter.BotInterpreter;
 import edu.sru.andgate.bitbot.interpreter.InstructionLimitedVirtualMachine;
 import edu.sru.andgate.bitbot.tools.FileManager;
 import edu.sru.andgate.bitbot.tutorial.ActionItem;
 import edu.sru.andgate.bitbot.tutorial.QuickAction;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
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
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 
 public class IDE extends Activity {
     /** Called when the activity is first created. */
 	
 	/**
 	 * Used for sliding the ViewFlipper
 	 */
 	private Animation sIn_left, sOut_left, sIn_right, sOut_right;
 	private EditText editor;
 	private TextView botOutput;
 	private String file, file_data;
 	private SlidingDrawer slidingDrawer;
 	private ActionItem[] quick_tools, bot_functions, selection_shells, sequence_shells, iteration_shells;
 	private String[] quick_tools_titles, quick_tools_strings, bot_function_titles, bot_function_strings,
 					 sequence_shell_titles, sequence_shell_strings, selection_shell_titles, selection_shell_strings,
 					 iteration_shell_titles, iteration_shell_strings;
 	private Button slideHandleButton, sequence_btn, iteration_btn, selection_btn, tools_btn, bot_code,
 					back_to_code;
 	private ImageButton highlight_right, highlight_left, move_right, move_left, move_up, move_down, tab_over,
 						send_btn;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main_ide);
             
         file = getIntent().getExtras().getString("File");
         file_data = getIntent().getExtras().getString("Data");
        
 		//create the text editor and cabinet button
 		editor = (EditText) findViewById(R.id.editor);
 		editor.setText(file_data);
 			
 		botOutput = (TextView) findViewById(R.id.ide_std_out);
 		
 		slidingDrawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);
 		slideHandleButton = (Button) findViewById(R.id.slideHandleButton);
 		
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
 		 * Action Items for Bot Functions button
 		 */
 		bot_function_strings = getResources().getStringArray(R.array.bot_function_strings);
 		bot_function_titles = getResources().getStringArray(R.array.bot_function_titles);
 		bot_functions = new ActionItem[bot_function_titles.length];
 		for(int i = 0; i < bot_functions.length; i++){
 			bot_functions[i] = new ActionItem();
 			setActionItem(bot_functions[i], editor, bot_function_titles[i], bot_function_strings[i]);
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
 		 * Set all the QuickAction buttons onClick() methods 
 		 */
 		sequence_btn = (Button) this.findViewById(R.id.sequence_btn);
 		sequence_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				QuickAction qa = new QuickAction(v);
 				for(int i = 0; i < sequence_shells.length; i++){
 					qa.addActionItem(sequence_shells[i]);
 				}
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 								
 		selection_btn = (Button) this.findViewById(R.id.selection_btn);
 		selection_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				QuickAction qa = new QuickAction(v);
 				for(int i = 0; i < selection_shells.length; i++){
 					qa.addActionItem(selection_shells[i]);
 				}
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 		
 		iteration_btn = (Button) this.findViewById(R.id.iteration_btn);
 		iteration_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				QuickAction qa = new QuickAction(v);
 				for(int i = 0; i < iteration_shells.length; i++){
 					qa.addActionItem(iteration_shells[i]);
 				}
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 		
 		tools_btn = (Button) this.findViewById(R.id.tools_btn);
 		tools_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				QuickAction qa = new QuickAction(v);
 				for(int i = 0; i < quick_tools.length; i++){
 					qa.addActionItem(quick_tools[i]);
 				}
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 		
 		bot_code = (Button) this.findViewById(R.id.bot_btn);
 		bot_code.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				QuickAction qa = new QuickAction(v);
 				for(int i = 0; i < bot_functions.length; i++){
 					qa.addActionItem(bot_functions[i]);
 				}
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 		
 
 		sIn_left = AnimationUtils.loadAnimation(this, R.anim.slidein_left);
 		sOut_left = AnimationUtils.loadAnimation(this, R.anim.slideout_left);
 		sIn_right = AnimationUtils.loadAnimation(this, R.anim.slidein_right);
 		sOut_right = AnimationUtils.loadAnimation(this, R.anim.slideout_right);
 		
 		highlight_right = (ImageButton) this.findViewById(R.id.select_right);
 		highlight_right.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try{
 					editor.setSelection(editor.getSelectionStart(), editor.getSelectionEnd()+1);				
 				}catch (Exception e){
 						
 				}
 			}
 		});
 		
 		highlight_left = (ImageButton) this.findViewById(R.id.select_left);
 		highlight_left.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try{
 				editor.setSelection(editor.getSelectionStart()- 1, editor.getSelectionEnd());				
 				}catch (Exception e){
 					
 				}
 			}
 		});
 		
 		move_up = (ImageButton) this.findViewById(R.id.move_up);
 		move_up.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try{
 					
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		move_down = (ImageButton) this.findViewById(R.id.move_down);
 		move_down.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try{
 					//move down one line
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		move_left = (ImageButton) this.findViewById(R.id.move_left);
 		move_left.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try{
 					editor.setSelection(editor.getSelectionStart() -1);
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		move_right = (ImageButton) this.findViewById(R.id.move_right);
 		move_right.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try{
 					editor.setSelection(editor.getSelectionStart() +1);
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		tab_over = (ImageButton) this.findViewById(R.id.tab_over);
 		tab_over.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				expandEditText("\t");				
 			}
 		});
 		
 		send_btn = (ImageButton) this.findViewById(R.id.send_btn);
 		send_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				InterpreteCode();
 				
 				ViewFlipper vf = (ViewFlipper) findViewById(R.id.ide_view_flipper);
 				
 				vf.setInAnimation(sIn_right);
 				vf.setOutAnimation(sOut_right);
 				
 				vf.showNext();
 				
 			}
 		});
 		
 		back_to_code = (Button) this.findViewById(R.id.ide_back_to_code_btn);
 		back_to_code.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				ViewFlipper vf = (ViewFlipper) findViewById(R.id.ide_view_flipper);
 
 				vf.setInAnimation(sIn_left);
 				vf.setOutAnimation(sOut_left);
 				
 				vf.showPrevious();
 			}
 		});
 		
 		/*
 		 * set the sliding drawer open/closed listeners and handlers
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
 			}
 		});
 
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
     
     /**
      * This overrides the default back button behavior to flip back to the first
      * view of the ViewFlipper before backing out of this activity.
      */
     @Override
     public void onBackPressed()
     {
     	ViewFlipper vf = (ViewFlipper) findViewById(R.id.ide_view_flipper);
     	
     	if (vf.getDisplayedChild() == 0)
     		//check if change has been made
     		if(!file_data.equals(editor.getText().toString())){
     			promptSave();
     		}else{
     			Intent engineIntent = new Intent(IDE.this, CodeBuilderActivity.class);
 				startActivity(engineIntent);
 				finish();
     		}
     	else
     		vf.showPrevious();
     }
     
     // Temp variable declaration
     private InstructionLimitedVirtualMachine ilvm;
     
     /**
      * Temporary code to test the VM
      */
     public void StepCode(View v)
     {
     	try
     	{
     		ilvm.resume(1);
     	}
     	catch(Exception e)
     	{
     		e.printStackTrace();
     	}
     }
     
     /**
      * A way to hard-stop the interpreter.  Good for ending infinite while loops.
      * @param v Button that activated this.
      */
     public void PauseCode(View v)
     {
     	if (ilvm != null)
     		ilvm.pause();
     }
     
     /**
      * A way to hard-stop the interpreter.  Good for ending infinite while loops.
      * @param v Button that activated this.
      */
     public void StopCode(View v)
     {
     	if (ilvm != null)
     		ilvm.stop();
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
 	    	
 	    	bi = new BotInterpreter(null, editor.getText().toString() + "\n");
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
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.ide_menu, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.clear:
 				editor.setText("");
 				break;
 			
 			case R.id.save:
 				FileManager.saveCodeFile(editor.getText().toString(), file);
 				break;
 			
 			case R.id.saveas:
 				promptUser("Save File As: ", "New File Name: ");
 				break;
 		}
 		return true;
 	}
 	
 	public void promptUser(String title, String mesg){
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle(title);
 		alert.setMessage(mesg);
 
 		// Set an EditText view to get user input 
 		final EditText input = new EditText(this);
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 			  String value = input.getText().toString();
 			  if(value == ""){
 				  value = "New File.txt";
 			  }
 			  FileManager.saveCodeFile(editor.getText().toString(), value);
 		}
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 		    // Canceled.
 		  }
 		});
 		
 		alert.show();
 	}	
 	
 	public void promptSave(){
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("Save File");
 		alert.setMessage("Do you want to save this file?");
 		alert.setPositiveButton("Save",new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
				FileManager.saveCodeFile(FileManager.readTextFileFromDirectory("Code",file), file);
 				Intent engineIntent = new Intent(IDE.this, CodeBuilderActivity.class);
 				startActivity(engineIntent);
 				finish();
 			}
 		});
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub	
 			}
 		});
 		alert.setNeutralButton("Discard", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
 				Intent engineIntent = new Intent(IDE.this, CodeBuilderActivity.class);
 				startActivity(engineIntent);
 				finish();
 			}
 		});
 		
 		alert.show();
 	}
 }
