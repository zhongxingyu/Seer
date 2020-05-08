 package com.eps_hioa_2013.JointAttentionResearchApp;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.NumberPicker;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 //ModuleSettingsActivity gets called, when you create a new Module or like to edit an existing one
 public class ModuleSettingsActivity extends Activity {
 	public final static String MODULENUMBER = "com.eps_hioa_2013.JointAttentionResearchApp.MODULENUMBER";	
 	public final static String EXTRA_SESSION = "com.eps_hioa_2013.JointAttentionResearchApp.EXTRA_SESSION";
 	public final static String EXTRA_ROUNDSTOPLAY = "com.eps_hioa_2013.JointAttentionResearchApp.EXTRA_ROUNDSTOPLAY";
 	public final static String EXTRA_TIME = "com.eps_hioa_2013.JointAttentionResearchApp.EXTRA_TIME";
 
 	Bundle bundle;
 	Session mysession;	
 
 	NumberPicker npMinutes;
 	NumberPicker npSeconds;
 	NumberPicker npRoundsToPlay;
 	
 	String nameInTheBeginning = "";
 
 	//Example to explain the oncoming 4 lines: When you have an donald.jpg in the Elementsfolder on your tablet:
 	//	There will be an Element on position 0 with the name donald in preactions
 	//	The appropriate Button is in buttonPreactions on position 0
 	//	The appropriate Checkbox is in checkboxPreactions also on position 0
 	//In that way, the Element, Button and the CheckBox can be connected to each other.
 	//The donald Element will of course also be in Signals and Actions because its a picture
 	private List<CheckBox> checkboxPreactions = new ArrayList<CheckBox>();
 	private List<Button> buttonPreactions = new ArrayList<Button>();	//locationbutton
 	private List<Button> buttonPreactions3 = new ArrayList<Button>(); //modulestarterbutton
 	private List<Element> preactions = new ArrayList<Element>();
 	private int preactionsCounter = 0;
 	
 	private List<CheckBox> checkboxActions = new ArrayList<CheckBox>();
 	private List<Button> buttonActions = new ArrayList<Button>(); //locationbutton
 	private List<Element> actions = new ArrayList<Element>();
 	private int actionsCounter = 0;
 	
 	private List<CheckBox> checkboxSignals = new ArrayList<CheckBox>();
 	private List<Button> buttonSignals = new ArrayList<Button>(); //locationbutton
 	private List<Button> buttonSignals2 = new ArrayList<Button>(); //duration0button
 	private List<Button> buttonSignals4 = new ArrayList<Button>(); //duration1button
 	private List<Element> signals = new ArrayList<Element>();
 	private int signalsCounter = 0;
 
 	private List<CheckBox> checkboxRewards = new ArrayList<CheckBox>();
 	private List<Button> buttonRewards = new ArrayList<Button>(); //locationbutton
 	private List<Button> buttonRewards2 = new ArrayList<Button>(); //duration0button
 	private List<Element> rewards = new ArrayList<Element>();
 	private int rewardsCounter = 0;
 
 	private int currentModuleNumber = -1;
 	private Boolean ElementHasLocation = true;
 
 	private String currentElement;
 	private String currentLocation;
 	private String currentDurationForPopUp;
 	private String currentStartModule;
 	private String popupMode;
 	private String[] locationArray = {"topleft", "topmid", "topright", "midleft", "midmid", "midright", "bottomleft", "bottommid", "bottomright"};
 	private String[] durationArray = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "2-10", "4-8", "1-5", "3-6", "6-10"};
 	private String[] modulenamesArray = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
 	protected void onCreate(Bundle savedInstanceState) {
 		System.out.println("ModuleSettingsActivity started");
 		super.onCreate(savedInstanceState);
 
 		//set full screen
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		//addPreferencesFromResource(R.xml.settings); 
 		setContentView(R.layout.activity_module_settings);
 
 		Intent intent = getIntent();
 		mysession = (Session) intent.getSerializableExtra(ModuleActivity.EXTRA_SESSION);
 		String modulenumber = (intent.getStringExtra(ModuleActivity.MODULENUMBER));
 		this.currentModuleNumber = Integer.parseInt(modulenumber);
 		
 		refreshModulelist();
 				
 		//shows the Elements at the right place and saves them in the Membervariables
 		setupDynamicElementList(mysession.getElementlist());
 
 		configureNumberPickers(); //just some settings for the NumberPickers; nothing special
 
 		loadModuleSettings(modulenumber); //loads and shows all the saved Settings of a module
 
 		//hides keyboard until user presses a field
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		
 		TextView title = (TextView) findViewById(R.id.Title);
 		
 		String module_name = getNameOfModule(modulenumber);
 		if((module_name == null) || (module_name.equals("")) || module_name.equals("accessibility")) module_name = "NEW MODULE";
 		title.setText("Settings of Module: " + module_name);
 	}
 
 	protected void onResume() {
 		refreshModulelist();
 		super.onResume();
 	}
 
 	//Refreshes the modulenames in mysession and the ModuleArrayList
 	private void refreshModulelist() {
 		//refresh modulenames in mysession
 		List<Module> myModules = createModules();
 		List<String> modulenames = new ArrayList<String>();
 		for(int i = 0; i < myModules.size(); i++)
 		{
 			modulenames.add(i, myModules.get(i).getName());
 		}
 		mysession.setModulenames(modulenames);
 		
 		for(int i = 0; i < modulenames.size()-1; i++)
 		{
 			modulenamesArray[i] = i + ": " + modulenames.get(i+1); //the i is the Number of the SharedPreferences; hopefully
 		}
 	}
 	
 	public boolean modulenameTaken(String modulename)
 	{		
		if(nameInTheBeginning.equals(modulename)) return false;
 		List<String> modulenames = new ArrayList<String>();
 		modulenames = mysession.getModulenames();
 		
 		for(int i = 0; i < modulenames.size(); i++)
 		{
 			if(modulenames.get(i).equals(modulename))
 			{
 			return true;
 			}
 		}
 		return false;
 	}
 
 	public void loadModuleSettings(String modulenumber) {
 
 		refreshModulelist();
 		if(Integer.parseInt(modulenumber) != -1) 
 		{
 			EditText editText2 = (EditText) findViewById(R.id.editText2); //name gets loaded
 			editText2.setText(getNameOfModule(modulenumber));
 			
 			nameInTheBeginning = getNameOfModule(modulenumber);
			
 
 			EditText editText1 = (EditText) findViewById(R.id.editText1); //descrition gets loaded
 			editText1.setText(getDescriptionOfModule(modulenumber));
 			
 			npRoundsToPlay.setValue(getRoundsToPlayOfModule(modulenumber));
 			
 			int secondsOverall = getTimeToPlayOfModule(modulenumber);
 			int minutes = secondsOverall/60;
 			npMinutes.setValue(minutes);
 			int seconds = secondsOverall%60;
 			npSeconds.setValue(seconds);
 			
 			
 
 			for(int i = 0; i < preactionsCounter; i++) //the states of all checkboxes get loaded
 			{	    		
 				Boolean b = getBooleanOfModule(modulenumber, preactions.get(i).getName() + "preaction");
 				checkboxPreactions.get(i).setChecked(b);
 			}
 
 			for(int i = 0; i < signalsCounter; i++) //the states of all checkboxes get loaded
 			{	    		
 				Boolean b = getBooleanOfModule(modulenumber, signals.get(i).getName() + "signal");
 				checkboxSignals.get(i).setChecked(b);
 			}
 
 			for(int i = 0; i < actionsCounter; i++) //the states of all checkboxes get loaded
 			{	    		
 				Boolean b = getBooleanOfModule(modulenumber, actions.get(i).getName() + "action");
 				checkboxActions.get(i).setChecked(b);
 			}
 
 			for(int i = 0; i < rewardsCounter; i++) //the states of all checkboxes get loaded
 			{	    		
 				Boolean b = getBooleanOfModule(modulenumber, rewards.get(i).getName() + "reward");
 				checkboxRewards.get(i).setChecked(b);
 			}
 			
 			Toast.makeText(getApplicationContext(), "Module" + this.currentModuleNumber + ": " + getNameOfModule(modulenumber) + " loaded", Toast.LENGTH_SHORT).show();
 
 		}
 		else {/*nothing to do here*/}
 	}
 
 	public void onclick_start_game(View view)
 	{	
 		EditText editText2 = (EditText) findViewById(R.id.editText2);
 		String module_name = editText2.getText().toString();
 
 		refreshModulelist();
 		if((module_name == null) || (module_name.equals("")) || (modulenameTaken(module_name)))
 		{
 			if((module_name == null) || (module_name.equals(""))) Toast.makeText(getApplicationContext(), "Modulename empty", Toast.LENGTH_SHORT).show();
 			if(modulenameTaken(module_name)) Toast.makeText(getApplicationContext(), "Modulename already taken", Toast.LENGTH_SHORT).show();
 		}
 		else 
 		{
 			onclick_save(view);
 			if (ElementHasLocation == false); //do nothing	 		
 			else
 			{	
 				mysession.updateStatistics("Clicked on Save & Start Module");
 				Intent intent = new Intent(this, GameActivity.class);
 				intent.putExtra(MODULENUMBER, Integer.toString(currentModuleNumber));
 				intent.putExtra(EXTRA_ROUNDSTOPLAY, npRoundsToPlay.getValue());
 				intent.putExtra(EXTRA_TIME, calculateTimeToPlayInSeconds());
 				bundle = new Bundle();
 				bundle.putSerializable(EXTRA_SESSION, (Serializable) mysession);
 				intent.putExtras(bundle);
 				startActivity(intent);
 				finish();
 			}
 		}
 	}
 	
 	public void onclick_cancel(View view)
 	{
 		finish();
 	}
 	
 	public void onclick_save(View view)
 	{	
 
 		EditText editText2 = (EditText) findViewById(R.id.editText2);
 		String module_name = editText2.getText().toString();
 
 		EditText editText1 = (EditText) findViewById(R.id.editText1);
 		String module_description = editText1.getText().toString();
 		if((module_description == null) || (module_description.equals(""))) module_description = "empty";
 		
 		int roundsToPlay = npRoundsToPlay.getValue();
 		int timeToPlay = calculateTimeToPlayInSeconds();
 		
 		//checking if Settings are ok to start the game
 		refreshModulelist();
 		if((module_name == null) || (module_name.equals("")) || (modulenameTaken(module_name)))
 		{
 			if((module_name == null) || (module_name.equals(""))) Toast.makeText(getApplicationContext(), "Modulename empty", Toast.LENGTH_SHORT).show();
 			if(modulenameTaken(module_name)) Toast.makeText(getApplicationContext(), "Modulename already taken", Toast.LENGTH_SHORT).show();
 		}
 		else
 		{	
 			//if the modulecounter is smaller than -1 it gets set to -1
 			if(getModulecounterOutOfPreferences() < -1) resetModulecounterInPreferences();
 	
 			
 			String nameForModule = null;
 			if(this.currentModuleNumber == -1) //jumps in here if new modules gets added
 			{
 				//modulecounter gets iterated
 				incrementModulecounterInPreferences();     	
 				//the Name for the new Module gets created. It is like MODULE0, MODULE1, MODULE2, ...
 				nameForModule = "MODULE"+getModulecounterOutOfPreferences();
 				this.currentModuleNumber = getModulecounterOutOfPreferences();
 			}
 			else //jumps in here if existing module gets saved
 			{
 				nameForModule = "MODULE"+this.currentModuleNumber;
 			}
 	
 	
 			//saves the variables into the ShardPreferences for the current Module 
 			SharedPreferences pref_modulesettings = getSharedPreferences(nameForModule, 0);
 			SharedPreferences.Editor editor = pref_modulesettings.edit();       
 			// editor is cleared for boxes that were checked once but are not anymore.
 			editor.clear(); //editor clears empty boxes
 			editor.putString("module_name", module_name);
 			editor.putString("module_description", module_description);
 			editor.putInt("timeToPlay", timeToPlay);
 			editor.putInt("roundsToPlay", roundsToPlay);
 	
 			ElementHasLocation = true;
 			//ask all Preaction TextBoxes if checked or unchecked
 			for(int i = 0; i < preactionsCounter; i++)
 			{
 				Boolean checked = false;
 				if(checkboxPreactions.get(i).isChecked() && ElementHasLocation == true)
 				{
 					if((buttonPreactions.get(i).getText().equals("location")))
 					{
 						ElementHasLocation = false;
 						Toast.makeText(getApplicationContext(), "At least one Element is missing its location", Toast.LENGTH_SHORT).show();						
 					}
 					{	
 						checked = true;	   					
 						editor.putBoolean(preactions.get(i).getName() + "preaction", checked);
 						//saves true (checked) or false (unchecked) as a Boolean named after the Element in the Shared Pref
 					}
 				}		
 			}
 			//ask all Signals TextBoxes if checked or unchecked
 			for(int i = 0; i < signalsCounter; i++)
 			{
 				Boolean checked = false;
 				if(checkboxSignals.get(i).isChecked() && ElementHasLocation == true)
 				{
 					if((buttonSignals.get(i).getText().equals("location")))
 					{
 						ElementHasLocation = false;
 						Toast.makeText(getApplicationContext(), "At least one Element is missing its location", Toast.LENGTH_SHORT).show();						
 					}
 					else
 					{
 					checked = true;
 					editor.putBoolean(signals.get(i).getName() + "signal", checked);	    		
 					//saves true (checked) or false (unchecked) as a Boolean named after the Element in the Shared Pref
 					}
 				}
 			}
 			//ask all Action TextBoxes if checked or unchecked
 			for(int i = 0; i < actionsCounter; i++)
 			{
 				Boolean checked = false;
 				if(checkboxActions.get(i).isChecked() && ElementHasLocation == true)
 				{
 					if((buttonActions.get(i).getText().equals("location")))
 					{
 						ElementHasLocation = false;
 						Toast.makeText(getApplicationContext(), "At least one Element is missing its location", Toast.LENGTH_SHORT).show();						
 					}
 					else
 					{
 						checked = true;
 						editor.putBoolean(actions.get(i).getName() + "action", checked);
 						//saves true (checked) or false (unchecked) as a Boolean named after the Element in the Shared Pref
 					}
 				}
 			}
 			//ask all Rewards TextBoxes if checked or unchecked
 			for(int i = 0; i < rewardsCounter; i++)
 			{
 				Boolean checked = false;
 				if(checkboxRewards.get(i).isChecked() && ElementHasLocation == true)
 				{
 					if((buttonRewards.get(i).getText().equals("location")))
 					{
 						ElementHasLocation = false;
 						Toast.makeText(getApplicationContext(), "At least one Element is missing its location", Toast.LENGTH_SHORT).show();				
 					}
 					else
 					{
 						checked = true;
 						editor.putBoolean(rewards.get(i).getName() + "reward", checked);
 						//saves true (checked) or false (unchecked) as a Boolean named after the Element in the Shared Pref
 					}
 				}	    	
 			}
 			
 			//saving location for preactions			
 			for(int i = 0; i < buttonPreactions.size(); i++)
 			{
 				String buttontext = buttonPreactions.get(i).getText().toString();
 				if(buttontext.equals("location")) ;//do nothing
 				else editor.putString(buttonPreactions.get(i).getTag() + "location", buttontext);
 			}
 			
 			//saving modulestarter for preactions	
 			for(int i = 0; i < buttonPreactions3.size(); i++)
 			{
 				String buttontext = buttonPreactions3.get(i).getText().toString();
 				if(buttontext.equals("startModule")) ;//do nothing
 				else editor.putString(buttonPreactions3.get(i).getTag() + "startModule", buttontext);
 			}
 			
 			//saving location for actions			
 			for(int i = 0; i < buttonActions.size(); i++)
 			{
 				String buttontext = buttonActions.get(i).getText().toString();
 				if(buttontext.equals("location")) ;//do nothing
 				else editor.putString(buttonActions.get(i).getTag() + "location", buttontext);
 			}
 			
 			//saving location for signals			
 			for(int i = 0; i < buttonSignals.size(); i++)
 			{
 				String buttontext = buttonSignals.get(i).getText().toString();
 				if(buttontext.equals("location")) ;//do nothing
 				else editor.putString(buttonSignals.get(i).getTag() + "location", buttontext);
 			}
 			
 			//saving duration0 for signals	
 			for(int i = 0; i < buttonSignals2.size(); i++)
 			{
 				String buttontext = buttonSignals2.get(i).getText().toString();				
 				if(buttontext.equals("duration0")) ;//do nothing
 				else editor.putString(buttonSignals2.get(i).getTag() + "duration0", buttontext);
 			}
 			
 			//saving duration1 for signals	
 			for(int i = 0; i < buttonSignals4.size(); i++)
 			{
 				String buttontext = buttonSignals4.get(i).getText().toString();				
 				if(buttontext.equals("duration1")) ;//do nothing
 				else editor.putString(buttonSignals4.get(i).getTag() + "duration1", buttontext);
 			}
 			
 			//saving location for rewards			
 			for(int i = 0; i < buttonRewards.size(); i++)
 			{
 				String buttontext = buttonRewards.get(i).getText().toString();
 				if(buttontext.equals("location")) ;//do nothing
 				else editor.putString(buttonRewards.get(i).getTag() + "location", buttontext);
 			}
 			
 			//saving duration0 for rewards	
 			for(int i = 0; i < buttonRewards2.size(); i++)
 			{
 				String buttontext = buttonRewards2.get(i).getText().toString();
 				if(buttontext.equals("duration0")) ;//do nothing
 				else editor.putString(buttonRewards2.get(i).getTag() + "duration0", buttontext);
 			}
 
         
 			
 			
 			if(ElementHasLocation == true)
 			{
 				editor.commit();
 				Toast.makeText(getApplicationContext(), "Saved as Module" + this.currentModuleNumber + ": " + getNameOfModule(Integer.toString(currentModuleNumber)), Toast.LENGTH_SHORT).show();
 				finish();
 				//goes back to last Activity (ModuleActivity)
 			}
 			 
 		}
 
 	}
 
 	private void setupDynamicElementList(List<Element> elements)
 	{
 		for(int i = 0; i < elements.size(); i++)
 		{
 			if(elements.get(i) instanceof ElementPicture)
 			{
 				//creates CheckBox and Button and shows it in the right place and adds both in the Membervariables
 				addElementToList(elements.get(i).getName(), "Preactions", true, false, false, true);
 				preactions.add(preactionsCounter, elements.get(i)); //adds the Element in the Membervariables
 				preactionsCounter++; //counter how many elements are in this array
 				addElementToList(elements.get(i).getName(), "Actions", true, false, false, false);
 				actions.add(actionsCounter, elements.get(i));
 				actionsCounter++;
 				addElementToList(elements.get(i).getName(), "Signals", true, true, true, false);
 				signals.add(signalsCounter, elements.get(i));
 				signalsCounter++;
 				addElementToList(elements.get(i).getName(), "Rewards", true, true, false, false);
 				rewards.add(rewardsCounter, elements.get(i));
 				rewardsCounter++;
 			}
 			if(elements.get(i) instanceof ElementVideo)
 			{
 				addElementToList(elements.get(i).getName(), "Rewards", true, false, false, false);
 				rewards.add(rewardsCounter, elements.get(i));
 				rewardsCounter++;
 			}
 			if(elements.get(i) instanceof ElementSound)
 			{
 				addElementToList(elements.get(i).getName(), "Rewards", false, false, false, false);
 				rewards.add(rewardsCounter, elements.get(i));
 				rewardsCounter++;
 			}
 		}
 	}
 
 	
 
 	//adds a Checkbox for an Element to the Settingsscreen
 	//WARNING: elementType must be either "Preactions", "Signals", "Actions" or "Rewards"
 	public void addElementToList(String elementName, String elementType, boolean locationNeeded, boolean duration0Needed, boolean duration1Needed, boolean startModuleNeeded)
 	{		
 		TableLayout table = null;
 		String currentLocation = "";
 		String currentDuration0 = "";
 		String currentDuration1 = "";
 
 		if(elementType == "Preactions")
 		{
 			currentLocation = getElementLocation(Integer.toString(currentModuleNumber), elementName + "Preaction");
 			currentStartModule = getElementStartModule(Integer.toString(currentModuleNumber), elementName + "Preaction3");
 			table = (TableLayout) findViewById(R.id.Preactions);
 			//creates new Tablerow and sets it into the new Tablelayout
 			TableRow newTablerow = new TableRow(this);
 			table.addView(newTablerow);
 
 			//creates new Checkbox with the name of the element and sets it into the Tablerow
 			checkboxPreactions.add(preactionsCounter, new CheckBox(this));
 			checkboxPreactions.get(preactionsCounter).setText(elementName);
 
 			newTablerow.addView(checkboxPreactions.get(preactionsCounter));
 
 			//creates new Button for choosing the LOCATION of the Element and sets it also into the Tablerow 
 			buttonPreactions.add(preactionsCounter, new Button(this));
 			buttonPreactions.get(preactionsCounter).setTag( elementName + "Preaction");				
 			//creates new Button for choosing the Module wich should get started after pressing on the Action
 			buttonPreactions3.add(actionsCounter, new Button(this));
 			buttonPreactions3.get(actionsCounter).setTag( elementName + "Preaction3");
 			if(locationNeeded == true) //shows the buttons
 			{
 				buttonPreactions.get(preactionsCounter).setText(currentLocation);
 				buttonPreactions.get(preactionsCounter).setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					currentElement = (String) v.getTag(); //gets used in the popup; looks like DonaldPreaction or MickeyPreaction for example
 					popupMode = "preactionLocation"; //determines the popupMode;
 					showElementLocationDialog(); //shows Popup					
 					}
 				});		
 			}
 			else //makes the buttons disappear
 			{
 				buttonPreactions.get(preactionsCounter).setVisibility(View.GONE);
 			}
 			
 			if(startModuleNeeded == true) //shows the buttons
 			{
 				buttonPreactions3.get(preactionsCounter).setText(currentStartModule);
 				buttonPreactions3.get(preactionsCounter).setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						currentElement = (String) v.getTag();
 						
 						showElementStartModuleDialog();
 					}
 				});
 			}
 			else //makes the buttons disappear
 			{				
 				buttonPreactions3.get(actionsCounter).setVisibility(View.GONE);
 			}
 			newTablerow.addView(buttonPreactions.get(preactionsCounter));	
 			newTablerow.addView(buttonPreactions3.get(actionsCounter));
 		}
 
 		
 
 		if(elementType == "Actions"){
 			currentLocation =  getElementLocation(Integer.toString(currentModuleNumber), elementName + "Action");
 
 
 			table = (TableLayout) findViewById(R.id.Signals); //Changed things around this is called Signals because the view was named Signals.
 			//creates new Tablerow and sets it into the new Tablelayout
 			TableRow newTablerow = new TableRow(this);
 			table.addView(newTablerow);
 
 			//creates new Checkbox with the name of the element and sets it into the Tablerow
 			checkboxActions.add(actionsCounter, new CheckBox(this));
 			checkboxActions.get(actionsCounter).setText(elementName);
 			//	checkboxPreactions.add(preactionsCounter, (checkboxPreactions.get(preactionsCounter)));
 
 			newTablerow.addView(checkboxActions.get(actionsCounter));
 
 				//creates new Button and sets it also into the Tablerow
 			buttonActions.add(actionsCounter, new Button(this));
 			buttonActions.get(actionsCounter).setTag(elementName + "Action");
 			//creates new Button for choosing the DURATION of the Element and sets it also into the Tablerow 
 			if(locationNeeded == true)
 			{
 				buttonActions.get(actionsCounter).setText(currentLocation);
 				buttonActions.get(actionsCounter).setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						currentElement = (String) v.getTag();
 						popupMode = "actionLocation"; //determines the popupMode;
 						showElementLocationDialog();
 					}
 				});		
 			}
 			else
 			{
 				buttonActions.get(actionsCounter).setVisibility(View.GONE);
 			}
 			newTablerow.addView(buttonActions.get(actionsCounter));					
 		}
 		
 		if(elementType == "Signals"){
 			currentLocation =  getElementLocation(Integer.toString(currentModuleNumber), elementName + "Signal");
 			currentDuration0 = getElementDuration0(Integer.toString(currentModuleNumber), elementName + "Signal2");
 			currentDuration1 = getElementDuration1(Integer.toString(currentModuleNumber), elementName + "Signal4");
 			table = (TableLayout) findViewById(R.id.Actions);//Changed things around this is called Actions because the view was named Actions.
 			//creates new Tablerow and sets it into the new Tablelayout
 			TableRow newTablerow = new TableRow(this);
 			table.addView(newTablerow);
 
 			//creates new Checkbox with the name of the element and sets it into the Tablerow
 			checkboxSignals.add(signalsCounter, new CheckBox(this));
 			checkboxSignals.get(signalsCounter).setText(elementName);
 			//	checkboxPreactions.add(preactionsCounter, (checkboxPreactions.get(preactionsCounter)));
 
 			newTablerow.addView(checkboxSignals.get(signalsCounter));
 
 				//creates new Button for location and sets it also into the Tablerow
 			buttonSignals.add(signalsCounter, new Button(this));	
 			buttonSignals.get(signalsCounter).setTag( elementName + "Signal");
 			//creates new Button for choosing the duration0 of the Element and sets it also into the Tablerow 
 			buttonSignals2.add(signalsCounter, new Button(this));
 			buttonSignals2.get(signalsCounter).setTag( elementName + "Signal2");
 			//creates new Button for choosing the duration1 of the Element and sets it also into the Tablerow 
 			buttonSignals4.add(signalsCounter, new Button(this));
 			buttonSignals4.get(signalsCounter).setTag( elementName + "Signal4");
 			if(locationNeeded == true)
 			{
 				buttonSignals.get(signalsCounter).setText(currentLocation);
 				buttonSignals.get(signalsCounter).setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						currentElement = (String) v.getTag();
 						popupMode = "signalLocation"; //determines the popupMode;
 						showElementLocationDialog();
 					}
 				});
 			}
 			else
 			{
 				buttonSignals.get(signalsCounter).setVisibility(View.GONE);
 			}
 			
 			if(duration0Needed == true)
 			{
 				buttonSignals2.get(signalsCounter).setText(currentDuration0);
 				buttonSignals2.get(signalsCounter).setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					currentElement = (String) v.getTag(); //gets used in the popup; looks like DonaldPreaction or MickeyPreaction for example
 					popupMode = "signalDuration0"; //determines the popupMode;
 					showElementDurationDialog(); //shows Popup
 					}
 				});	
 
 			}
 			else
 			{
 				buttonSignals2.get(signalsCounter).setVisibility(View.GONE);
 			}
 			
 			if(duration1Needed == true)
 			{
 				System.out.println("2"+currentDuration1);
 				buttonSignals4.get(signalsCounter).setText(currentDuration1);
 				System.out.println("3");
 				buttonSignals4.get(signalsCounter).setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					currentElement = (String) v.getTag(); //gets used in the popup; looks like DonaldPreaction or MickeyPreaction for example
 					popupMode = "signalDuration1"; //determines the popupMode
 					showElementDurationDialog(); //shows Popup
 					}
 				});
 				System.out.println("1");
 			}
 			
 			else
 			{
 				buttonSignals4.get(signalsCounter).setVisibility(View.GONE);
 			}
 			
 			newTablerow.addView(buttonSignals.get(signalsCounter)); 
 			newTablerow.addView(buttonSignals2.get(signalsCounter));	
 			newTablerow.addView(buttonSignals4.get(signalsCounter));	
 		
 		}
 
 		if(elementType == "Rewards"){
 			currentLocation =  getElementLocation(Integer.toString(currentModuleNumber), elementName + "Reward");
 			currentDuration0 = getElementDuration0(Integer.toString(currentModuleNumber), elementName + "Reward2");
 			table = (TableLayout) findViewById(R.id.Rewards);
 			//creates new Tablerow and sets it into the new Tablelayout
 			TableRow newTablerow = new TableRow(this);
 			table.addView(newTablerow);
 
 			//creates new Checkbox with the name of the element and sets it into the Tablerow
 			checkboxRewards.add(rewardsCounter, new CheckBox(this));
 			checkboxRewards.get(rewardsCounter).setText(elementName);
 			//	checkboxPreactions.add(preactionsCounter, (checkboxPreactions.get(preactionsCounter)));
 
 			newTablerow.addView(checkboxRewards.get(rewardsCounter));
 			
 				//creates new Button and sets it also into the Tablerow
 			buttonRewards.add(rewardsCounter, new Button(this));
 			buttonRewards.get(rewardsCounter).setTag(elementName + "Reward");
 			//creates new Button for choosing the DURATION of the Element and sets it also into the Tablerow 
 			buttonRewards2.add(rewardsCounter, new Button(this));
 			buttonRewards2.get(rewardsCounter).setTag( elementName + "Reward2");
 			if(locationNeeded == true)
 			{
 				buttonRewards.get(rewardsCounter).setText(currentLocation);
 				buttonRewards.get(rewardsCounter).setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						currentElement = (String) v.getTag();
 						popupMode = "rewardLocation"; //determines the popupMode;
 						showElementLocationDialog();
 					}
 				});
 			}
 			else
 			{
 				buttonRewards.get(rewardsCounter).setVisibility(View.GONE);
 			}
 			if(duration0Needed == true)
 			{
 				buttonRewards2.get(rewardsCounter).setText(currentDuration0);
 				buttonRewards2.get(rewardsCounter).setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					currentElement = (String) v.getTag(); //gets used in the popup; looks like DonaldPreaction or MickeyPreaction for example
 					popupMode = "rewardDuration"; //determines the popupMode;
 					showElementDurationDialog(); //shows Popup
 					}
 				});
 			}
 			else
 			{				
 				buttonRewards2.get(rewardsCounter).setVisibility(View.GONE);
 			}
 			newTablerow.addView(buttonRewards.get(rewardsCounter)); 
 			newTablerow.addView(buttonRewards2.get(rewardsCounter));
 			
 		}
 
 		if(table == null){ //error
 			Toast.makeText(getApplicationContext(),		
 					"Error in: public void addElementToList(String elementName, String elementType); WRONG elementType",
 					Toast.LENGTH_LONG).show();
 		}       
 	}
 
 	private void configureNumberPickers() {
 		npMinutes = (NumberPicker) findViewById(R.id.npMinutes);
 		npMinutes.setMaxValue(999);
 		npMinutes.setMinValue(0);
 
 		npSeconds = (NumberPicker) findViewById(R.id.npSeconds);
 		npSeconds.setMaxValue(59);
 		npSeconds.setMinValue(0);
 
 		npRoundsToPlay = (NumberPicker) findViewById(R.id.npRoundsToPlay);
 		npRoundsToPlay.setMaxValue(999);
 		npRoundsToPlay.setMinValue(1);		
 	}
 
 	private int calculateTimeToPlayInSeconds() {
 		int minutes = npMinutes.getValue();
 		int seconds = npSeconds.getValue();	
 		return ((minutes*60)+seconds);
 	}
 
 	//returns the counter for the modules
 	//this Method is also present in ModuleActivity.java; This should be solved in a better way
 	public int getModulecounterOutOfPreferences() {
 		SharedPreferences pref_modulecounter = getSharedPreferences("counter", 0); 
 		int modulecounter = pref_modulecounter.getInt("modulecounter", 0);
 		return modulecounter;
 	}
 
 	//increments the modulecounter
 	public void incrementModulecounterInPreferences() {		
 		SharedPreferences pref_modulecounter = getSharedPreferences("counter", 0);
 		SharedPreferences.Editor editor = pref_modulecounter.edit();
 		int count = getModulecounterOutOfPreferences() + 1;
 		editor.putInt("modulecounter", count);
 		editor.commit();
 	}
 
 	//decrements the modulecounter
 	public void decrementModulecounterInPreferences() {		
 		SharedPreferences pref_modulecounter = getSharedPreferences("counter", 0);
 		SharedPreferences.Editor editor = pref_modulecounter.edit();
 		int count = getModulecounterOutOfPreferences() - 1;
 		editor.putInt("modulecounter", count);
 		editor.commit();
 	}
 
 	//sets the modulecounter to -1. When a new module gets added, it has the number 0
 	//this Method is also present in ModuleActivity.java; This should be solved in a better way
 	public void resetModulecounterInPreferences() {
 		SharedPreferences pref_modulecounter = getSharedPreferences("counter", 0);
 		SharedPreferences.Editor editor = pref_modulecounter.edit();
 		int count = -1;
 		editor.putInt("modulecounter", count);
 		editor.commit();
 	}
 
 	public String getNameOfModule(String i)
 	{	
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String nameOfModule = pref_modulesettings.getString("module_name", ACCESSIBILITY_SERVICE);
 		return nameOfModule;				
 	}
 
 	public String getDescriptionOfModule(String i)
 	{	
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String nameOfDescrition = pref_modulesettings.getString("module_description", ACCESSIBILITY_SERVICE);
 		return nameOfDescrition;		
 	}
 	
 	public int getTimeToPlayOfModule(String i)
 	{	
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		int timeToPlay = pref_modulesettings.getInt("timeToPlay", 0); 
 		return timeToPlay;		
 	}
 	
 	public int getRoundsToPlayOfModule(String i)
 	{	
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		int roundsToPlay = pref_modulesettings.getInt("roundsToPlay", 1); 
 		return roundsToPlay;
 	}
 	
 
 
 	public Boolean getBooleanOfModule(String i, String elementName)
 	{	
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		Boolean nameOfDescription = pref_modulesettings.getBoolean(elementName, false);        
 		return nameOfDescription;
 	}
 
 	public String getElementLocation(String i, String elementName)
 	{
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String location = pref_modulesettings.getString(elementName + "location", "location");
 		return location;
 	}
 	
 	public String getElementDuration0(String i, String elementName)
 	{
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String duration = pref_modulesettings.getString(elementName + "duration0", "duration0");
 		return duration;
 	}
 	
 	public String getElementDuration1(String i, String elementName)
 	{
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String duration = pref_modulesettings.getString(elementName + "duration1", "duration1");
 		return duration;
 	}
 		
 	public String getElementStartModule(String i, String elementName)
 	{
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String startModule = pref_modulesettings.getString(elementName + "startModule", "startModule");
 		return startModule;
 	}
 	
 	//shows the popup when you click on the Edit-button
 	public void showElementLocationDialog() {
 		DialogFragment newFragment = new ElementPositionDialog();
 		newFragment.show(getFragmentManager(), "dialogsettings");
 	}
 	
 	public void showElementDurationDialog() {
 		DialogFragment newFragment = new ElementDurationDialog();
 		newFragment.show(getFragmentManager(), "dialogsettings");
 	}
 	
 	public void showElementStartModuleDialog() {
 		DialogFragment newFragment = new ElementStartModuleDialog();
 		newFragment.show(getFragmentManager(), "dialogsettings");
 	}
 	
 	@SuppressLint("ValidFragment")
 	//Position of an Element Popup
 	public class ElementPositionDialog extends DialogFragment {
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle("Select the location the Element on the screen")
 			.setItems(locationArray, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					// The 'which' argument contains the index position
 					// of the selected item
 					
 					currentLocation = locationArray[which];
 					if(popupMode.equals("preactionLocation")) SetLocationButtonTextPreaction();
 					if(popupMode.equals("signalLocation")) SetLocationButtonTextSignal();
 					if(popupMode.equals("actionLocation")) SetLocationButtonTextAction();
 					if(popupMode.equals("rewardLocation")) SetLocationButtonTextReward();
 				}
 			});
 			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// User cancelled the dialog
 				}
 			});
 			return builder.create();
 		}
 	}
 	
 	void SetLocationButtonTextPreaction()
 	{
 		int i = 0;
 		for(; i < preactionsCounter; i++)
 		{
 			if((preactions.get(i).getName() + "Preaction").equals(currentElement)) break;
 		}
 		buttonPreactions.get(i).setText(currentLocation); //refreshes buttontext at once!
 	}
 	void SetLocationButtonTextSignal()
 	{
 		int i = 0;
 		for(; i < signalsCounter; i++)
 		{
 			if((signals.get(i).getName() + "Signal").equals(currentElement)) break;
 		}
 		buttonSignals.get(i).setText(currentLocation); //refreshes buttontext at once!
 	}
 	void SetLocationButtonTextAction()
 	{
 		int i = 0;
 		for(; i < actionsCounter; i++)
 		{
 			if((actions.get(i).getName() + "Action").equals(currentElement)) break;
 		}
 		buttonActions.get(i).setText(currentLocation); //refreshes buttontext at once!
 	}
 	void SetLocationButtonTextReward()
 	{
 		int i = 0;
 		for(; i < rewardsCounter; i++)
 		{
 			if((rewards.get(i).getName() + "Reward").equals(currentElement)) break;
 		}
 		buttonRewards.get(i).setText(currentLocation); //refreshes buttontext at once!
 	}
 	
 	@SuppressLint("ValidFragment")
 	//Duration of an Element Popup
 	public class ElementDurationDialog extends DialogFragment {
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle("Select the duration the Element should appear on the screen")
 			.setItems(durationArray, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					// The 'which' argument contains the index position
 					// of the selected item					
 					currentDurationForPopUp = durationArray[which];
 					if(popupMode.equals("signalDuration0")) SetDuration0ButtonTextSignal();
 					if(popupMode.equals("signalDuration1")) SetDuration1ButtonTextSignal();
 					if(popupMode.equals("rewardDuration")) SetDurationButtonTextReward();
 				}
 			});
 			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// User cancelled the dialog
 				}
 			});
 			return builder.create();
 		}
 	}
 	
 
 	void SetDuration0ButtonTextSignal()
 	{
 		int i = 0;
 		for(; i < signalsCounter; i++)
 		{
 			if((signals.get(i).getName() + "Signal2").equals(currentElement)) break;
 		}
 		buttonSignals2.get(i).setText(currentDurationForPopUp); //refreshes buttontext at once!
 	}
 	
 	void SetDuration1ButtonTextSignal()
 	{
 		int i = 0;
 		for(; i < signalsCounter; i++)
 		{
 			if((signals.get(i).getName() + "Signal4").equals(currentElement)) break;
 		}
 		buttonSignals4.get(i).setText(currentDurationForPopUp); //refreshes buttontext at once!
 	}
 
 	void SetDurationButtonTextReward()
 	{
 		int i = 0;
 		for(; i < rewardsCounter; i++)
 		{
 			if((rewards.get(i).getName() + "Reward2").equals(currentElement)) break;
 		}
 		buttonRewards2.get(i).setText(currentDurationForPopUp); //refreshes buttontext at once!
 	}
 	
 	
 	@SuppressLint("ValidFragment")
 	//Duration of an Element Popup	
 	public class ElementStartModuleDialog extends DialogFragment {
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle("Select Module which to start after pressing on this Preaction")
 			.setItems(modulenamesArray, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					// The 'which' argument contains the index position
 					// of the selected item					
 					currentStartModule = modulenamesArray[which];
 					int i = 0;
 					for(; i < preactionsCounter; i++)
 					{
 						if((preactions.get(i).getName() + "Preaction3").equals(currentElement)) break;
 					}
 					buttonPreactions3.get(i).setText(currentStartModule); //refreshes buttontext at once!
 				}
 			});
 			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// User cancelled the dialog
 				}
 			});
 			return builder.create();
 		}
 	}
 	
 	private List<Module> createModules() {
 		// create container for new modules
 		List<Module> moduleContainer = new ArrayList<Module>();
 		
 		// loop through preference file for all module names
 		for(int i = 0; i <= getModulecounterOutOfPreferences(); i++)
 		{
 			//start values for error checking
 			Module currentModule = null;
 			String name = "";
 			String description = "";
 			int number = -1;
 			
 			//get the preference with currentModule information
 			String nameOfModulePref = "MODULE" + i;
 	    	SharedPreferences pref_currentModule = getSharedPreferences(nameOfModulePref, 0);  
 	        
 			// for each name found create new Module object with preference name and description name and number
 			name = pref_currentModule.getString("module_name", ACCESSIBILITY_SERVICE);
 			description = pref_currentModule.getString("module_description", ACCESSIBILITY_SERVICE);
 			number = i;
 			currentModule = new Module(number, name, description);
 			
 			// add new Module object to container
 			moduleContainer.add(currentModule);
 		}
 		// Return Container
 		return moduleContainer;		
 	}
 }
