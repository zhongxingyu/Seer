 package com.cthos.pfpt;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ComponentName;
 import android.content.ContentProvider;
 import android.content.ContentProviderClient;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.cthos.pfpt.core.CharacterClass;
 import com.cthos.pfpt.core.chr.CharacterClassFactory;
 import com.cthos.pfpt.equipment.SlottedItem;
 import com.cthos.util.Registry;
 
 public class ViewCharacter extends Activity
 {
 	protected com.cthos.pfpt.core.Character character;
 	
 	long characterId;
 	
 	public static final int MENU_ITEM_INVENTORY = 0;
 	public static final int MENU_ITEM_ABILITIES = 1;
 	public static final int MENU_ITEM_SKILLS = 2;
 	public static final int MENU_ITEM_EFFECTS = 3;
 	public static final int MENU_ITEM_MANAGE_CHARACTER = 4;
 	
 	public static final int DIALOG_CHANGE_HP = 1;
 	
 	protected String addOrRemoveStr;
 	protected int addRemoveHPAmount = 0;
 	
 	protected boolean _keepScreenAwake = false;
 	
 	protected PowerManager.WakeLock wl;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.view_character);
         
         long charId = getIntent().getLongExtra("characterId", 0);
         characterId = charId;
         
         Log.d("", String.valueOf(characterId));
         
         loadCharacterData(characterId);
         
         SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
         _keepScreenAwake = sharedPrefs.getBoolean("keep_screen_awake", true);
         
         PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		this.wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "View Character Lock");
     }
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		if (_keepScreenAwake) {
 			this.wl.acquire();
 		}
 	}
 	
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		if (_keepScreenAwake) {
 			this.wl.release();
 		}
 	}
 	
 	/**
 	 * Summons up the current character data. Tries to do it
 	 * in background threads for the more complicated bits.
 	 * 
 	 * Though the main character table loads on the UI thread,
 	 * so you have something to show initially.
 	 * 
 	 * @param characterId
 	 */
 	protected void loadCharacterData(long characterId)
 	{
 		Uri chUri = ContentUris.withAppendedId(
 			Uri.parse("content://com.cthos.pfpt.core/character"),
 			characterId
 		);
 		
 		ContentProviderClient client = getContentResolver().acquireContentProviderClient(chUri);
         ContentProvider provider = client.getLocalContentProvider();
         
         Cursor c = provider.query(chUri, null, null, null, null);
         
         this.character = new com.cthos.pfpt.core.Character(c, this);
         
         Cursor slottedC = managedQuery(
     		Uri.parse("content://com.cthos.pfpt.core.slotteditemprovider/slotted_item"),
 			null,
 		 	"character_id = ?",
 		 	new String[]{String.valueOf(characterId)},
 		    "location ASC"
         );
         
         new LoadGearTask().execute(new Long(characterId));
         new LoadCharacterClassTask().execute(new Long(characterId));
         
         startManagingCursor(c);
         startManagingCursor(slottedC);
         
         buildInterface();
 	}
 	
 	protected void buildInterface()
 	{
 		TextView nameLabel = (TextView) findViewById(R.id.character_name);
 		nameLabel.setText(this.character.name);
 		
 		populateAttributes();
 		populateAC();
 	}
 	
 	/**
 	 * Sets the on click actions for the damage and
 	 * healing buttons.
 	 * 
 	 * @return void
 	 */
 	protected void _initHPButtons()
 	{
 		Button addHP = (Button) findViewById(R.id.healing_button);
 		addHP.setOnClickListener(handleAddHPClicked);
 		
 		Button removeHP = (Button) findViewById(R.id.damage_button);
 		removeHP.setOnClickListener(handleRemoveHPClicked);
 	}
 	
 	/**
 	 * Populates everything in the attributes fields.
 	 * 
 	 * @return void
 	 */
 	protected void populateAttributes()
 	{
 		TextView strval = (TextView) findViewById(R.id.str_value);
 		long strBonus = this.character.calculateBonus(this.character.modifiedAttributes.get("strength"));
 		strval.setText(String.valueOf(this.character.modifiedAttributes.get("strength"))
 				+ " (" + String.valueOf(strBonus) + ")");
 		
 		TextView dexval = (TextView) findViewById(R.id.dex_value);
 		Number dex_value = this.character.modifiedAttributes.get("dexterity");
 		long dexBonus = this.character.calculateBonus(dex_value);
 		dexval.setText(dex_value.toString() + " (" + String.valueOf(dexBonus) + ")");
 		
 		TextView conval = (TextView) findViewById(R.id.con_value);
 		Number con_value = this.character.modifiedAttributes.get("constitution");
 		long conBonus = this.character.calculateBonus(con_value);
 		conval.setText(con_value.toString() + " (" + String.valueOf(conBonus) + ")");
 		
 		TextView wisval = (TextView) findViewById(R.id.wis_value);
 		Number wis_value = this.character.modifiedAttributes.get("wisdom");
 		long wisBonus = this.character.calculateBonus(wis_value);
 		wisval.setText(wis_value.toString() + " (" + String.valueOf(wisBonus) + ")");
 		
 		TextView intval = (TextView) findViewById(R.id.int_value);
 		Number int_value = this.character.modifiedAttributes.get("intelligence");
 		long intBonus = this.character.calculateBonus(int_value);
 		intval.setText(int_value.toString() + " (" + String.valueOf(intBonus) + ")");
 		
 		TextView chaval = (TextView) findViewById(R.id.cha_value);
 		Number cha_value = this.character.modifiedAttributes.get("charisma");
 		long chaBonus = this.character.calculateBonus(cha_value);
 		chaval.setText(cha_value.toString() + " (" + String.valueOf(chaBonus) + ")");
 	}
 	
 	/**
 	 * Updates the Text view for AC value.
 	 * 
 	 * @return void
 	 */
 	protected void populateAC()
 	{
 		TextView acval = (TextView) findViewById(R.id.ac_value);
 		acval.setText(String.valueOf(this.character.ac));
 	}
 	
 	/**
 	 * Updates the text View for the HP value. Should be called when
 	 * HP changes.
 	 * 
 	 * @return void
 	 */
 	protected void populateHP()
 	{
 		Log.d("HP Changes", "Setting HP to " + String.valueOf(this.character.currentHp));
 		
 		TextView hpval = (TextView) findViewById(R.id.hp_value);
 		hpval.setText(String.valueOf(this.character.currentHp));
 		
 		if (this.character.currentHp < 0) {
 			hpval.setTextColor(Color.RED);
 		} else if (this.character.currentHp == 0) {
 			hpval.setTextColor(Color.YELLOW);
 		} else if (this.character.currentHp >= this.character.hp) {
 			hpval.setTextColor(Color.GREEN);
 		} else {
 			hpval.setTextColor(Color.WHITE);
 		}
 	}
 	
 	/**
 	 * Updates the text views for all of the attack values.
 	 * 
 	 * @return void
 	 */
 	protected void populateAttacks()
 	{
 		TextView babval = (TextView) findViewById(R.id.bab_value);
 		babval.setText(String.valueOf(this.character.bab));
 		
 		TextView meleeval = (TextView) findViewById(R.id.melee_value);
 		meleeval.setText(String.valueOf(this.character.meleeToHit));
 		
 		TextView rangedval = (TextView) findViewById(R.id.ranged_value);
 		rangedval.setText(String.valueOf(this.character.rangedToHit));
 		
 		TextView cmdval = (TextView) findViewById(R.id.cmd_value);
 		cmdval.setText(String.valueOf(this.character.cmd));
 		
 		TextView cmbval = (TextView) findViewById(R.id.cmb_value);
 		cmbval.setText(String.valueOf(this.character.cmb));
 	}
 	
 	protected void populateSaves()
 	{
 		TextView fortVal = (TextView) findViewById(R.id.fort_save_value);
 		fortVal.setText(String.valueOf(this.character.modifiedSaves.get("fort")));
 		
 		TextView reflexVal = (TextView) findViewById(R.id.reflex_save_value);
 		reflexVal.setText(String.valueOf(this.character.modifiedSaves.get("reflex")));
 		
 		TextView willVal = (TextView) findViewById(R.id.will_save_value);
 		willVal.setText(String.valueOf(this.character.modifiedSaves.get("will")));
 	}
 	
 	/**
 	 * Create the options menu. Add things like Manage Gear, Spell Book, Effects
 	 * etc.
 	 * 
 	 */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         
         menu.add(0, MENU_ITEM_INVENTORY, 0, "Inventory")
                 .setShortcut('1', 'i')
                 .setIcon(android.R.drawable.ic_menu_compass);
         
         menu.add(1, MENU_ITEM_ABILITIES, 0, "Abilities")
         		.setIcon(android.R.drawable.ic_menu_search);
         
         menu.add(2, MENU_ITEM_SKILLS, 0, "Skills")
         	.setIcon(android.R.drawable.ic_menu_directions);
         
         menu.add(3, MENU_ITEM_EFFECTS, 0, "Effects")
         .setIcon(android.R.drawable.ic_menu_add);
         
         menu.add(4, MENU_ITEM_MANAGE_CHARACTER, 0, "Manage Character")
     		.setIcon(android.R.drawable.ic_menu_save);
         
         Intent intent = new Intent(null, getIntent().getData());
         intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
         menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                 new ComponentName(this, CreateCharacter.class), null, intent, 0, null);
 
         return true;
     }
     
     /**
      * Called whenever a context item is selected from the menu.
      * 
      * @param MenuItem item
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
     	Intent intent = new Intent();
     	
     	switch (item.getItemId()) {
     		case MENU_ITEM_INVENTORY:
     			intent.setClassName("com.cthos.pfpt", "com.cthos.pfpt.InventoryActivity");
     			break;
     		case MENU_ITEM_MANAGE_CHARACTER:
     			intent.setClass(this, ManageCharacterActivity.class);
     			break;
     		case MENU_ITEM_ABILITIES:
     			intent.setClass(this, AbilitiesActivity.class);
     			break;
     		case MENU_ITEM_EFFECTS:
     			intent.setClass(this, ActiveEffectsActivity.class);
     			break;
     		default:
     			return false;
     	}
     	
     	intent.putExtra("characterId", characterId);
     	
     	startActivity(intent);
     	
     	return true;
     }
     
     /**
      * Called after the HP dialog is changed, updates the 
      * interface on its own.
      * 
      * @return void
      */
     protected void handleHPChanged(boolean positive, int amount)
     {
     	long newHP = 0;
     	
     	if (positive) {
     		newHP = this.character.currentHp + amount;
     	} else {
     		newHP = this.character.currentHp - amount;
     	}
     	
     	Log.d("Hp changes", String.valueOf(newHP));
     	
     	this.character.setCurrentHP(newHP);
     	populateHP();
     }
     
     /**
      * Called when the background thread loading the gear is
      * completed. This handles updating the view interface.
      * 
      * @param gear An ArrayList returned from the background
      *             thread
      * 
      * @return
      */
     protected void gearLoaded(ArrayList<SlottedItem> gear)
     {
     	Log.d("gear", "Gear Loaded");
     	this.character.setGear(gear);
     	this.character.gearUpdateAttributes();
     	this.character.calculateAC();
     	populateAC();
     	populateAttributes();
     }
     
     /**
      * Handles what happens when the background thread is finished
      * loading the character's classes. Mostly it'll calculate HP
      * and base attack bonuses for you.
      * 
      * @param clss
      */
     protected void classesLoaded(ArrayList<CharacterClass> clss)
     {
     	Log.d("classes", "Character Classes Loaded");
     	this.character.setClasses(clss);
     	this.character.calculateHP();
     	this.character.calculateAttacks();
    	this.character.calculateSaves();
     	
     	this.character.loadSavedHP();
     	
     	populateHP();
     	populateAttacks();
    	populateSaves();
     	
     	// Turn on the HP tracking buttons.
     	_initHPButtons();
     }
     
     /**
      * Called when a dialog is called to be summoned.
      * 
      * @param int did
      * 
      * @return Dialog
      */
     protected Dialog onCreateDialog(int did)
 	{
 		AlertDialog dialog;
 		
 		switch (did) {
 			case DIALOG_CHANGE_HP:
 			default:
 				dialog = _initHPChangeDialog();
 				break;
 		}
 		
 		return dialog;
 	}
     
     /**
      * Inits the dialog to be able to alter your current HP
      * 
      * @return Dialog
      */
     protected AlertDialog _initHPChangeDialog()
 	{
 		LayoutInflater inflater = LayoutInflater.from(this);
 		final View custom_view = inflater.inflate(R.layout.add_remove_health, null);
 		
 		final EditText howMuch = (EditText) custom_view.findViewById(R.id.add_remove_health_amount);
 		
 		AlertDialog dialog = new AlertDialog.Builder(this).setTitle(this.addOrRemoveStr + " HP")
                               .setView(custom_view)
                               .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 								
 								public void onClick(DialogInterface dialog, int which) {
 									addRemoveHPAmount = Integer.parseInt(howMuch.getText().toString());
 									howMuch.setText("");
 									boolean addHP = false;
 									if (addOrRemoveStr.toLowerCase().equals("add")) {
 										addHP = true;
 									}
 									handleHPChanged(addHP, addRemoveHPAmount);
 									
 									dialog.cancel();
 								}
 							})
 							.setNegativeButton("Cancel", null).create();
 		
 		return dialog;
 	}
     
     private class LoadGearTask extends AsyncTask<Number, Void, ArrayList<SlottedItem>>
     {
     	@Override
     	protected ArrayList<SlottedItem> doInBackground(Number... characterId)
     	{
     		ArrayList<SlottedItem> items = new ArrayList<SlottedItem>();
     		
     		Cursor cursor = managedQuery(
 	    		Uri.parse("content://com.cthos.pfpt.core.slotteditemprovider/slotted_item"),
 				null,
 			 	"character_id = ?",
 			 	new String[]{String.valueOf(characterId[0])},
 			    "location ASC"
 	        );
     		
     		cursor.moveToFirst();
     		
             while (!cursor.isAfterLast()) {
             	SlottedItem theItem = new SlottedItem(cursor);
             	items.add(theItem);
             	cursor.moveToNext();
             }
             
             return items;
     	}
     	
     	@Override
     	protected void onPostExecute(ArrayList<SlottedItem> items)
     	{
     		gearLoaded(items);
     	}
     }
     
 	private class LoadCharacterClassTask extends AsyncTask<Number, Void, ArrayList<CharacterClass>>
     {
     	@Override
     	protected ArrayList<CharacterClass> doInBackground(Number... characterId)
     	{
     		ArrayList<CharacterClass> charClasses = new ArrayList<CharacterClass>();
     		
     		Cursor cursor = managedQuery(
 	    		Uri.parse("content://com.cthos.pfpt.core.characterclasslevelprovider/character_class_level"),
 				null,
 			 	"character_id = ?",
 			 	new String[]{String.valueOf(characterId[0])},
 			    "_id ASC"
 	        );
     		
     		cursor.moveToFirst();
     		
     		String className;
     		String numLevels;
     		
             while (!cursor.isAfterLast()) {            	
             	className = cursor.getString(cursor.getColumnIndex("class_name"));
             	numLevels = cursor.getString(cursor.getColumnIndex("num_levels"));
             	
             	CharacterClass cl = CharacterClassFactory.factory(className);
             	cl.setNumLevels(Integer.parseInt(numLevels));
             	charClasses.add(cl);
             	
             	cursor.moveToNext();
             }
             
             return charClasses;
     	}
 	
     	@Override
     	protected void onPostExecute(ArrayList<CharacterClass> characterClasses)
     	{
     		classesLoaded(characterClasses);
     	}
     }
     
     @Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	    if (keyCode == KeyEvent.KEYCODE_BACK) {
 	        Intent backIntent = new Intent().setClass(this, PlayerToolkitMain.class);
 	        
 	        startActivity(backIntent);
 	        return true;
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
     
     /**
      * Called when the add hp button is clicked.
      * 
      */
     private OnClickListener handleAddHPClicked = new OnClickListener() {
     	public void onClick(View v)
     	{
     		handleHPChanged(true, 1);
     	}
     };
     
     private OnClickListener handleRemoveHPClicked = new OnClickListener() {
     	public void onClick(View v)
     	{
     		handleHPChanged(false, 1);
     	}
     };
 }
