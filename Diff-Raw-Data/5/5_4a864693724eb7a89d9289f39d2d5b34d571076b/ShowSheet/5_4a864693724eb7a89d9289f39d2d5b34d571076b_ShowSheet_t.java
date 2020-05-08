 package com.pathfinder.character.sheet;
 
 import java.util.ArrayList;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Dialog;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.pathfinder.character.sheet.spellList.Spell;
 import com.pathfinder.character.sheet.spellList.SpellLevel;
 import com.pathfinder.character.sheet.spellList.SpellListAdapter;
 
 
 
 public class ShowSheet extends FragmentActivity implements ActionBar.TabListener {
 	Character myCharacter;
 	 private SpellListAdapter spellListAdapter;
 	 private ArrayList<SpellLevel> spellLevelList;
 	 private ExpandableListView spellList;
 	
     /**
      * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
      * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
      * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
      * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
      */
     SectionsPagerAdapter mSectionsPagerAdapter;
 
     /**
      * The {@link ViewPager} that will host the section contents.
      */
     ViewPager mViewPager;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_show_sheet);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         // Create the adapter that will return a fragment for each of the three primary sections
         // of the app.
         mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
         Display display = getWindowManager().getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
         if(size.x < 800)
         {
         	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         }
         
         
         ////////////////////
         myCharacter = new Character();
         /////////////////////
         
        spellLevelList = setSpellList();
        spellListAdapter = new SpellListAdapter(ShowSheet.this, spellLevelList);
         
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Set up the ViewPager with the sections adapter.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
 
         // When swiping between different sections, select the corresponding tab.
         // We can also use ActionBar.Tab#select() to do this if we have a reference to the
         // Tab.
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 actionBar.setSelectedNavigationItem(position);
             }
         });
 
         // For each of the sections in the app, add a tab to the action bar.
         for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
             // Create a tab with text corresponding to the page title defined by the adapter.
             // Also specify this Activity object, which implements the TabListener interface, as the
             // listener for when this tab is selected.
             actionBar.addTab(
                     actionBar.newTab()
                             .setText(mSectionsPagerAdapter.getPageTitle(i))
                             .setTabListener(this));
         }
     }
 
 	
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_show_sheet, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 
     /**
      * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
      * sections of the app.
      */
     public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int i) {
             Fragment fragment = new DummySectionFragment();
             Bundle args = new Bundle();
             args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
             fragment.setArguments(args);
             return fragment;
         }
      // TODO: wrap around on slides
         @Override
         public int getCount() {
             return 7;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             switch (position) {
                 case 0: return getString(R.string.sheet_section1).toUpperCase();
                 case 1: return getString(R.string.sheet_section2).toUpperCase();
                 case 2: return getString(R.string.sheet_section7).toUpperCase();
                 case 3: return getString(R.string.sheet_section3).toUpperCase();
                 case 4: return getString(R.string.sheet_section4).toUpperCase();
                 case 5: return getString(R.string.sheet_section5).toUpperCase();
                 case 6: return getString(R.string.sheet_section6).toUpperCase();
                 
             }
             return null;
         }
     }
 
     /**
      * A dummy fragment representing a section of the app, but that simply displays dummy text.
      */
     public static class DummySectionFragment extends Fragment {
         public DummySectionFragment() {
         }
 
         public static final String ARG_SECTION_NUMBER = "section_number";
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
         	
         	int tabLayout = 0;
         	Bundle args = getArguments();
         	switch(args.getInt(ARG_SECTION_NUMBER))
         	{
         		case 1:
         		{
         			 tabLayout = R.layout.summary;
         			 return inflater.inflate(tabLayout, container, false);
         		}
         		case 2:
         		{
         			tabLayout = R.layout.attacks_overview;
         			return inflater.inflate(tabLayout, container, false);
         		}
 	        	case 3:
 	        	{
 	        		tabLayout = R.layout.defences;
 	        		return inflater.inflate(tabLayout, container, false);
 	        	}
         		case 4:
 	        	{
 	        		tabLayout = R.layout.spells;
 	        		return inflater.inflate(tabLayout, container, false);
 	        	}
 	        	case 5:
 	        	{
 	        		tabLayout = R.layout.equipment;
 	        		return inflater.inflate(tabLayout, container, false);
 	        	}
 	        	case 6:
 	        	{
 	        		tabLayout = R.layout.feat_traits;
 	        		return inflater.inflate(tabLayout, container, false);
 	        	}
 	        	case 7:
 	        	{
 	        		tabLayout = R.layout.notes;
 	        		return inflater.inflate(tabLayout, container, false);
 	        	}
 
 	        	
         	}
         	return container;
         }
     }
 ////////////////////////////////////////////////////////////////////////////	
 ///////////////////////self written code////////////////////////////////////
 
     public ArrayList<SpellLevel> setSpellList() {
     	ArrayList<SpellLevel> spellLevels = new ArrayList<SpellLevel>();
     	ArrayList<Spell> spells = new ArrayList<Spell>();
     	
     	for(int i = 0; i<10; i++){
     		SpellLevel level = new SpellLevel();
     		level.setName("Level " + i);
     		
     		for(int j = 0; j < 10; j++){
     			Spell spell = new Spell();
     			spell.setName("Spell " + j);
     			spells.add(spell);
     		}
     		level.setSpells(spells);
     		spellLevels.add(level);
     		spells = new ArrayList<Spell>();    		
     	}
     	
     	return spellLevels;
     }
     
     /**
      * 
      * updateSheet() upadets values from Character class to display
      * 
      */
 
 	public void updateSheet()
 	{
 		int[]abilityScores = new int[6];
 		int[]abilityModifiers = new int[6];
 		TextView textView;	
 		
 
 		
 		myCharacter.getAbilityScore(abilityScores, abilityModifiers);
 		
 		//set ability modifier
 		textView = (TextView) findViewById(R.id.STRmod);
 		textView.setText(Integer.toString(abilityModifiers[0]));
 		textView = (TextView) findViewById(R.id.DEXmod);
 		textView.setText(Integer.toString(abilityModifiers[1]));
 		textView = (TextView) findViewById(R.id.CONmod);
 		textView.setText(Integer.toString(abilityModifiers[2]));
 		textView = (TextView) findViewById(R.id.INTmod);
 		textView.setText(Integer.toString(abilityModifiers[3]));
 		textView = (TextView) findViewById(R.id.WISmod);
 		textView.setText(Integer.toString(abilityModifiers[4]));
 		textView = (TextView) findViewById(R.id.CHAmod);
 		textView.setText(Integer.toString(abilityModifiers[5]));
 		
 		//set ability score
     	textView = (TextView) findViewById(R.id.STRscore);
     	textView.setText(Integer.toString(abilityScores[0]));
     	textView = (TextView) findViewById(R.id.DEXscore);
     	textView.setText(Integer.toString(abilityScores[1]));
     	textView = (TextView) findViewById(R.id.CONscore);
     	textView.setText(Integer.toString(abilityScores[2]));
     	textView = (TextView) findViewById(R.id.INTscore);
     	textView.setText(Integer.toString(abilityScores[3]));	
     	textView = (TextView) findViewById(R.id.WISscore);
     	textView.setText(Integer.toString(abilityScores[4]));	
     	textView = (TextView) findViewById(R.id.CHAscore);
     	textView.setText(Integer.toString(abilityScores[5]));
     	
     	displayNewSkillVal();
 	}
 	
 	public void displayNewSkillVal()
 	{
 		int[]skillTotal = new int[39];
 		TextView textView;
 		myCharacter.getSkillsTotal(skillTotal);
     	final int[] skillRanks = {
 	    		R.id.AcrobaticsScore, R.id.AppraiseScore, R.id.BuffScore, R.id.ClimbScore, R.id.CraftScore1, R.id.CraftScore2, R.id.CraftScore3, 
 	    		R.id.DiplomacyScore, R.id.DisableDeviceScore, R.id.DisguiseScore, R.id.EscapeArtistScore, R.id.FlyScore,
 	    		R.id.HandleAnimalScore, R.id.HealScore, R.id.IntimidateScore, R.id.KnowledgeArcanaScore, R.id.KnowledgeDungeoneringScore, 
 	    		R.id.KnowledgeEngineringScore, R.id.KnowledgeGeographyScore, R.id.KnowledgeHistoryScore, R.id.KnowledgeLocalScore, R.id.KnowledgeNatureScore,
 	    		R.id.KnowledgeNobillityScore, R.id.KnowledgePlanesScore, R.id.KnowledgeReligionScore, R.id.LinguisticsScore, R.id.PreceptionScore, 
 	    		R.id.PreformScore1,R.id.PreformScore2, R.id.ProfessionScore1, R.id.ProfessionScore2, R.id.RideScore, R.id.SenseMotiveScore, R.id.SleightOfHandScore,
 	    		R.id.SpellcraftScore, R.id.StealthScore, R.id.SurvivalScore, R.id.SwimScore, R.id.UseMagicDeviceScore
 	    	};
 		
     	for (int i = 0; i < 39; i++)
     	{
         	textView = (TextView) findViewById(skillRanks[i]);
         	
         	textView.setText(Integer.toString(skillTotal[i]));	
     	}
 	}
 	
 	
 ///////////////////////////////////////////////////////////////////////////////	
 /////////////////////////On Click//////////////////////////////////////////////	
 ///////////////////////////////////////////////////////////////////////////////
 	
 	//Changes Ability values
 	public void changsAbilitys (View v)
 	{
 		EditText editText;
 		TextView textView;
 		String oldScore;
 		
 		  // Sets up the custom dialog
 		  final Dialog changsAbilitysDialog = new Dialog(this);
 		  changsAbilitysDialog.setContentView(R.layout.abilitys);
 		  changsAbilitysDialog.setTitle("changsAbilitys");
 	
 			textView = (TextView) findViewById(R.id.STRscore);
 			editText = (EditText) changsAbilitysDialog.findViewById(R.id.newSTRscore);
 			oldScore =textView.getText().toString();
 			editText.setText(oldScore); 
 			textView = (TextView) findViewById(R.id.DEXscore);
 			editText = (EditText) changsAbilitysDialog.findViewById(R.id.newDEXscore);
 			oldScore =textView.getText().toString();
 			editText.setText(oldScore); 
 			textView = (TextView) findViewById(R.id.CONscore);
 			editText = (EditText) changsAbilitysDialog.findViewById(R.id.newCONscore);
 			oldScore =textView.getText().toString();
 			editText.setText(oldScore); 
 			textView = (TextView) findViewById(R.id.INTscore);
 			editText = (EditText) changsAbilitysDialog.findViewById(R.id.newINTscore);
 			oldScore =textView.getText().toString();
 			editText.setText(oldScore); 
 			textView = (TextView) findViewById(R.id.WISscore);
 			editText = (EditText) changsAbilitysDialog.findViewById(R.id.newWISscore);
 			oldScore =textView.getText().toString();
 			editText.setText(oldScore); 
 			textView = (TextView) findViewById(R.id.CHAscore);
 			editText = (EditText) changsAbilitysDialog.findViewById(R.id.newCHAscore);
 			oldScore =textView.getText().toString();
 			editText.setText(oldScore); 
 		
 		  Button changsAbilitysDialogButton = (Button) changsAbilitysDialog
 		    .findViewById(R.id.button1);
 		
 		  // Exits the dialog if button is clicked
 		  changsAbilitysDialogButton.setOnClickListener(new View.OnClickListener() 
 		  {
 	
 			     public void onClick(View v) 
 			     { 
 			    	EditText editText;
 			    	String newScore;
 			    	int[] scores;
 			    	scores = new int[6];
 			    	 
 			    	editText =(EditText) changsAbilitysDialog.findViewById(R.id.newSTRscore);
 			    	newScore = editText.getText().toString();
 			    	scores[0]= Integer.parseInt(newScore);
 			    	
 			    	editText =(EditText) changsAbilitysDialog.findViewById(R.id.newDEXscore);
 			    	newScore = editText.getText().toString();
 			    	scores[1] = Integer.parseInt(newScore);
 			    	
 			    	editText =(EditText) changsAbilitysDialog.findViewById(R.id.newCONscore);
 			    	newScore = editText.getText().toString();
 			    	scores[2] = Integer.parseInt(newScore);
 			    	
 			    	editText =(EditText) changsAbilitysDialog.findViewById(R.id.newINTscore);
 			    	newScore = editText.getText().toString();
 			    	scores[3] = Integer.parseInt(newScore);
 			    	
 			    	editText =(EditText) changsAbilitysDialog.findViewById(R.id.newWISscore);
 			    	newScore = editText.getText().toString();
 			    	scores[4] = Integer.parseInt(newScore);
 			    	
 			    	editText =(EditText) changsAbilitysDialog.findViewById(R.id.newCHAscore);
 			    	newScore = editText.getText().toString();
 			    	scores[5] = Integer.parseInt(newScore);
 			    	
 			    	myCharacter.setAbilityScore(scores);
 			    	 
 			    	updateSheet();
 			    	
 			    	changsAbilitysDialog.dismiss();
 			     }
 		  });
 		
 		  changsAbilitysDialog.show();
 	
 	}
 	
 	//Changes Skill values
 	
 	public void changsSkill (View v)
 	{
 		  // Sets up the custom dialog
 		  final Dialog changsSkillDialog = new Dialog(this);
 		  changsSkillDialog.setContentView(R.layout.skill_changes);
 		  changsSkillDialog.setTitle("Changs Skill");
 		  
 	    	int numOfSkills = 39;
 	    	int []oldRank = new int[numOfSkills];
 	    	int []oldMagic = new int[numOfSkills];
 	    	int []oldMisc = new int[numOfSkills];
 		    EditText editText;
 	    	final int[] skillRanks = 
 	    		{
 		    		R.id.RankSkill0, R.id.RankSkill1, R.id.RankSkill2, R.id.RankSkill3, R.id.RankSkill4, R.id.RankSkill4_2, R.id.RankSkill4_3,
 		    		R.id.RankSkill5, R.id.RankSkill6, R.id.RankSkill7, R.id.RankSkill8, R.id.RankSkill9, 
 		    		R.id.RankSkill10, R.id.RankSkill11, R.id.RankSkill12, R.id.RankSkill13, R.id.RankSkill14, 
 		    		R.id.RankSkill15, R.id.RankSkill16, R.id.RankSkill17, R.id.RankSkill18, R.id.RankSkill19,
 		    		R.id.RankSkill20, R.id.RankSkill21, R.id.RankSkill22, R.id.RankSkill23, R.id.RankSkill24, 
 		    		R.id.RankSkill25,R.id.RankSkill25_2, R.id.RankSkill26, R.id.RankSkill26_2, R.id.RankSkill27, R.id.RankSkill28, R.id.RankSkill29, 
 		    		R.id.RankSkill30, R.id.RankSkill31, R.id.RankSkill32, R.id.RankSkill33, R.id.RankSkill34,
 		    	};
 	    	final int[] skillMagic = 
 	    		{
 	    			R.id.MagicSkill0, R.id.MagicSkill1, R.id.MagicSkill2, R.id.MagicSkill3, R.id.MagicSkill4, R.id.MagicSkill4_2, R.id.MagicSkill4_3, R.id.MagicSkill5, 
 		    		R.id.MagicSkill6, R.id.MagicSkill7, R.id.MagicSkill8, R.id.MagicSkill9, R.id.MagicSkill10,
 		    		R.id.MagicSkill11, R.id.MagicSkill12, R.id.MagicSkill13, R.id.MagicSkill14, R.id.MagicSkill15, 
 		    		R.id.MagicSkill16, R.id.MagicSkill17, R.id.MagicSkill18, R.id.MagicSkill19, R.id.MagicSkill20,
 		    		R.id.MagicSkill21, R.id.MagicSkill22, R.id.MagicSkill23, R.id.MagicSkill24, R.id.MagicSkill25, R.id.MagicSkill25_2, 
 		    		R.id.MagicSkill26, R.id.MagicSkill26_2, R.id.MagicSkill27, R.id.MagicSkill28, R.id.MagicSkill29, R.id.MagicSkill30,
 		    		R.id.MagicSkill31, R.id.MagicSkill32, R.id.MagicSkill33, R.id.MagicSkill34,
 		    	};
 	    	final int[] skillmisc = 
 	    		{
 		    		R.id.MiscSkill0, R.id.MiscSkill1, R.id.MiscSkill2, R.id.MiscSkill3, R.id.MiscSkill4, R.id.MiscSkill4_2, R.id.MiscSkill4_3, R.id.MiscSkill5, 
 		    		R.id.MiscSkill6, R.id.MiscSkill7, R.id.MiscSkill8, R.id.MiscSkill9, R.id.MiscSkill10,
 		    		R.id.MiscSkill11, R.id.MiscSkill12, R.id.MiscSkill13, R.id.MiscSkill14, R.id.MiscSkill15, 
 		    		R.id.MiscSkill16, R.id.MiscSkill17, R.id.MiscSkill18, R.id.MiscSkill19, R.id.MiscSkill20,
 		    		R.id.MiscSkill21, R.id.MiscSkill22, R.id.MiscSkill23, R.id.MiscSkill24, R.id.MiscSkill25, R.id.MiscSkill25_2, 
 		    		R.id.MiscSkill26, R.id.MiscSkill26_2, R.id.MiscSkill27, R.id.MiscSkill28, R.id.MiscSkill29, R.id.MiscSkill30,
 		    		R.id.MiscSkill31, R.id.MiscSkill32, R.id.MiscSkill33, R.id.MiscSkill34,
 		    	};
 
 		    myCharacter.getSkillsRank(oldRank);
 		    myCharacter.getSkillsMagic(oldMagic);
 		    myCharacter.getSkillsMisc(oldMisc);
 	    	
 	    	for (int i = 0; i < numOfSkills; i++)
 	    	{
 			   editText =(EditText) changsSkillDialog.findViewById(skillRanks[i]);
 			   editText.setText(Integer.toString(oldRank[i]));
 			  
 			   editText =(EditText) changsSkillDialog.findViewById(skillMagic[i]);
 			   editText.setText(Integer.toString(oldMagic[i]));
 			   
 			   editText =(EditText) changsSkillDialog.findViewById(skillmisc[i]);
 			   editText.setText(Integer.toString(oldMisc[i]));
 	    	}
 	    	
 	    	
 			
 		  Button changsSkillDialogDialogButton = (Button) changsSkillDialog
 		    .findViewById(R.id.button1);
 		
 		  // Exits the dialog if button is clicked
 		  changsSkillDialogDialogButton.setOnClickListener(new View.OnClickListener() 
 		  {
 	
 			     public void onClick(View v) 
 			     { 
 			    	 
 				    EditText editText;
 				    String newScore;
 				    int numOfSkills = 39;
 			    	int []newRank = new int[numOfSkills];
 			    	int []newMagic = new int[numOfSkills];
 			    	int []newMisc = new int[numOfSkills];
 			    	
 
 			    	 
 			    	for (int i = 0; i < numOfSkills; i++)
 			    	{
 					    editText =(EditText) changsSkillDialog.findViewById(skillRanks[i]);
 					    newScore = editText.getText().toString();
 					    if (newScore.length() > 0)
 					    {
 					    	newRank[i] = Integer.parseInt(newScore);
 					    }
 					    else
 					    {
 					    	newRank[i] = 0;
 					    }
 					    
 					    editText =(EditText) changsSkillDialog.findViewById(skillMagic[i]);
 					    newScore = editText.getText().toString();
 					    if (newScore.length() > 0)
 					    {
 					    	newMagic[i] = Integer.parseInt(newScore);
 					    }
 					    else
 					    {
 					    	newMagic[i] = 0;
 					    }
 					    
 					    editText =(EditText) changsSkillDialog.findViewById(skillmisc[i]);
 					    newScore = editText.getText().toString();
 					    if (newScore.length() > 0)
 					    {
 					    	newMisc[i] = Integer.parseInt(newScore);
 					    }
 					    else
 					    {
 					    	newMisc[i] = 0;
 					    }
 			    	}
 			    	
 			    	myCharacter.setSkills(newRank,newMagic,newMisc);		    	 
 			    	displayNewSkillVal();
 			    	
 			    	changsSkillDialog.dismiss();
 			     }
 		  });
 	
 	  changsSkillDialog.show();
 	
 	}	
 	
 	public void hitPoints (View v)
 	{
 		  // Sets up the custom dialog
 		  final Dialog hitPointsDialog = new Dialog(this);
 		  hitPointsDialog.setContentView(R.layout.new_hitpoint);
 		  hitPointsDialog.setTitle("Hit Points");
 		  
 		  int curent = 0;
 		  int nonlethal = 0;
 		  EditText editText;
 		  
 		  myCharacter.getHitPoint(curent, nonlethal);
 		  
 		  editText = (EditText) hitPointsDialog.findViewById(R.id.Curent);
 		  editText.setText(Integer.toString(curent));
 		  editText = (EditText) hitPointsDialog.findViewById(R.id.Nonlethal);
 		  editText.setText(Integer.toString(nonlethal));
 
 		  Button changsSkillDialogDialogButton = (Button) hitPointsDialog
 		    .findViewById(R.id.button1);
 		
 		  // Exits the dialog if button is clicked
 		  changsSkillDialogDialogButton.setOnClickListener(new View.OnClickListener() 
 		  {
 	
 			     public void onClick(View v) 
 			     { 
 			    	 
 			    	 hitPointsDialog.dismiss();
 			     }
 		  });
 	
 		  hitPointsDialog.show();
 	
 	}
 	public void changesName (View v)
 	{
 		  // Sets up the custom dialog
 		  final Dialog changesNameDialog = new Dialog(this);
 		  changesNameDialog.setContentView(R.layout.edit_single_text);
 		  changesNameDialog.setTitle("Name");
 		  
 		  
 
 
 		  Button changesNameDialogDialogButton = (Button)changesNameDialog
 		    .findViewById(R.id.button1);
 		
 		  // Exits the dialog if button is clicked
 		  changesNameDialogDialogButton.setOnClickListener(new View.OnClickListener() 
 		  {
 	
 			     public void onClick(View v) 
 			     { 
 			    	 String characterName;
 			    	 EditText editText;
 			    	 TextView textView;
 			    	 editText = (EditText) changesNameDialog.findViewById(R.id.editText1);
 			    	 characterName = editText.getText().toString();
 			    	 
 			    	 //savs name and set it on the charcater sheet
 			    	 myCharacter.setName(characterName);
 			    	 textView = (TextView)findViewById(R.id.textName2);
 			    	 textView.setText(characterName);
 			    	 
 			    	 
 			    	 changesNameDialog.dismiss();
 			   
 			     }
 		  });
 	
 		  changesNameDialog.show();
 	
 	}
 	public void changesRace (View v)
 	{
 		  // Sets up the custom dialog
 		  final Dialog changesRaceDialog = new Dialog(this);
 		  changesRaceDialog.setContentView(R.layout.edit_single_text);
 		  changesRaceDialog.setTitle("Race");
 
 		  Button changesRaceDialogDialogButton = (Button)changesRaceDialog
 		    .findViewById(R.id.button1);
 		
 		  // Exits the dialog if button is clicked
 		  changesRaceDialogDialogButton.setOnClickListener(new View.OnClickListener() 
 		  {
 	
 			     public void onClick(View v) 
 			     { 
 			    	 String characterRace;
 			    	 EditText editText;
 			    	 TextView textView;
 			    	 editText = (EditText) changesRaceDialog.findViewById(R.id.editText1);
 			    	 characterRace = editText.getText().toString();
 			    	 
 			    	 //savs Race and set it on the character sheet
 			    	 myCharacter.setRace(characterRace);
 			    	 textView = (TextView)findViewById(R.id.textRace2);
 			    	 textView.setText(characterRace);
 			    	 
 			    	 
 			    	 changesRaceDialog.dismiss();
 			   
 			     }
 		  });
 	
 		  changesRaceDialog.show();
 	
 	}
 	public void changesDiety (View v)
 	{
 		  // Sets up the custom dialog
 		  final Dialog changesDietyDialog = new Dialog(this);
 		  changesDietyDialog.setContentView(R.layout.edit_single_text);
 		  changesDietyDialog.setTitle("Race");
 
 
 		  Button changesDietyDialogDialogButton = (Button)changesDietyDialog
 		    .findViewById(R.id.button1);
 		
 		  // Exits the dialog if button is clicked
 		  changesDietyDialogDialogButton.setOnClickListener(new View.OnClickListener() 
 		  {
 	
 			     public void onClick(View v) 
 			     { 
 			    	 String charactersDeity;
 			    	 EditText editText;
 			    	 TextView textView;
 			    	 editText = (EditText) changesDietyDialog.findViewById(R.id.editText1);
 			    	 charactersDeity = editText.getText().toString();
 			    	 
 			    	 //savs Diety and set it on the character sheet
 			    	 myCharacter.setDiety(charactersDeity);
 			    	 textView = (TextView)findViewById(R.id.textDeity2);
 			    	 textView.setText(charactersDeity);
 			    	 
 			    	 
 			    	 changesDietyDialog.dismiss();
 			   
 			     }
 		  });
 	
 		  changesDietyDialog.show();
 	
 	}
 	public void addItem (View v)
 	{
 		  // Sets up the custom dialog
 		  final Dialog addItemDialog = new Dialog(this);
 		  addItemDialog.setContentView(R.layout.edit_items);
 		  addItemDialog.setTitle("Items");
 
 
 		  Button addItemDialogDialogButton = (Button)addItemDialog
 		    .findViewById(R.id.button1);
 		
 		  // Exits the dialog if button is clicked
 		  addItemDialogDialogButton.setOnClickListener(new View.OnClickListener() 
 		  {
 	
 			     public void onClick(View v) 
 			     { 
 			    	LinearLayout layout;
 			    	EditText textFromEditing;
 			    	Equipment item;
 			    	TextView text;
 			    	TextView text2;
 			    	TextView text3;
 			    	TextView text4;
 			    	 
 			    	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
 			    	lp.setMargins(0, 1, 0, 0);
 
 			    	 text = new TextView(getApplicationContext());
 			    	 textFromEditing = (EditText)addItemDialog.findViewById(R.id.editText1);
 			    	 text.setText(textFromEditing.getText().toString());
 			    	 text.setTextColor(Color.rgb(0,0,0));
 			    	 text.setLayoutParams(lp);
 			    	 text.setClickable(true);
 			    	 text.setOnClickListener(equipmentEditor);
 			    	 text.setId(text.hashCode());
  
 			    	 text2 = new TextView(getApplicationContext());
 			    	 textFromEditing = (EditText)addItemDialog.findViewById(R.id.editText2);
 			    	 text2.setText(textFromEditing.getText().toString());
 			    	 text2.setTextColor(Color.rgb(0,0,0));
 			    	 text2.setLayoutParams(lp);
 			    	 text2.setOnClickListener(equipmentEditor);
 			    	 text2.setClickable(true);
 			    	 text2.setId(text2.hashCode());
 			    	 
 			    	 text3 = new TextView(getApplicationContext());
 			    	 textFromEditing = (EditText)addItemDialog.findViewById(R.id.editText3);
 			    	 text3.setText(textFromEditing.getText().toString());
 			    	 text3.setTextColor(Color.rgb(0,0,0));
 			    	 text3.setLayoutParams(lp);
 			    	 text3.setClickable(true);
 			    	 text3.setOnClickListener(equipmentEditor);
 			    	 text3.setId(text3.hashCode());
 			    	 
 			    	 text4 = new TextView(getApplicationContext());
 			    	 textFromEditing = (EditText)addItemDialog.findViewById(R.id.editText4);
 			    	 text4.setText(textFromEditing.getText().toString());
 			    	 text4.setTextColor(Color.rgb(0,0,0));
 			    	 text4.setLayoutParams(lp);
 			    	 text4.setClickable(true);
 			    	 text4.setOnClickListener(equipmentEditor);
 			    	 text4.setId(text4.hashCode());
 			    	 
 			    	 layout = (LinearLayout) findViewById(R.id.itemNameBox);
 			    	 layout.addView(text);
 			    	
 			    	 layout = (LinearLayout) findViewById(R.id.itemWeightBox);
 			    	 layout.addView(text2);
 			    	 
 			    	 layout = (LinearLayout) findViewById(R.id.itemAmountBox);
 			    	 layout.addView(text3);
 			    	 
 			    	 layout = (LinearLayout) findViewById(R.id.itemPrisBox);
 			    	 layout.addView(text4);
 			    	 addItemDialog.dismiss();
 			    	 
 			    	 item = new Equipment();
 			    	 item.name = text.getText().toString();//spellList = (ExpandableListView) findViewById(R.id.ExpList);
         //spellLevelList = setSpellList();
         //spellListAdapter = new SpellListAdapter(ShowSheet.this, spellLevelList);
         //spellList.setAdapter(spellListAdapter);
 			    	 item.nameID = text.hashCode();
 			    	 item.weight = Integer.parseInt(text2.getText().toString());
 			    	 item.weightID = text2.hashCode();
 			    	 item.amount = Integer.parseInt(text3.getText().toString());
 			    	 item.amountID = text3.hashCode();
 			    	 item.price = Integer.parseInt(text4.getText().toString());
 			    	 item.priceID = text4.hashCode();
 			    	 myCharacter.addItem(item);
 			   
 			     }
 		  });
 		  addItemDialog.show();	
 	}
 	
 
 	/**
 	 * RJAN BYTT NAVN P TING NR DU KOPIERER FRA MEG
 	 * OG DU MANGLER EN FUNKSJON FR  F DET DU PRVER TIL  FUNGERE UNDER HER
 	*/
 	
 	/*View.OnClickListener equipmentEditor  = new View.OnClickListener() 
 
 	public void addAttack (View v)
 	{
 		  // Sets up the custom dialog
 		  final Dialog addAttackDialog = new Dialog(this);
 		  addAttackDialog.setContentView(R.layout.attacks_button_layout);
 		  addAttackDialog.setTitle("Race");
 
 
 		  Button addattackDialogDialogButton = (Button)addAttackDialog
 		    .findViewById(R.id.button1);
 		
 		  // Exits the dialog if button is clicked
 		  addattackDialogDialogButton.setOnClickListener(new View.OnClickListener() 
 		  {
 	
 			     public void onClick(View v) 
 			     { 
 			    	 LinearLayout layout;
 			    	 TextView attackNameText;
 			    	 TextView attackBonusText;
 			    	 TextView attackDamageText;
 			    	 TextView attackCritText;
 			    	// EditText editText;
 			    	 
 			    	// editText = (EditText) addattackDialog.findViewById(R.id.attackName);
 			    	 attackNameText = new TextView(getApplicationContext());
 			    	 attackNameText.setText("derp");
 			    	 
 			    	 attackBonusText = new TextView(getApplicationContext());
 			    	 attackBonusText.setText("derp");
 			    	 
 			    	 attackDamageText = new TextView(getApplicationContext());
 			    	 attackDamageText.setText("derp");
 			    	 
 			    	 attackCritText = new TextView(getApplicationContext());
 			    	 attackCritText.setText("derp");
 			    	 
 			    	 layout = (LinearLayout) findViewById(R.id.attackNameBox);
 			    	 layout.addView(attackNameText);
 			    	
 			    	 layout = (LinearLayout) findViewById(R.id.attackWeightBox);
 			    	 layout.addView(attackBonusText);
 			    	 
 			    	 layout = (LinearLayout) findViewById(R.id.attackAmountBox);
 			    	 layout.addView(attackDamageText);
 			    	 
 			    	 layout = (LinearLayout) findViewById(R.id.attackPrisBox);
 			    	 layout.addView(attackCritText);
 			    	 addAttackDialog.dismiss();
 			   
 			     }
 		  });
 		  addAttackDialog.show();	
 	}*/
 	
 	View.OnClickListener equipmentEditor  = new View.OnClickListener() 
 	{
 		Equipment item;
 
 		  public void onClick(View v) 
 		  {
 			  //Equipment item = new Equipment();
 
 			 item = myCharacter.getItem(v.getId());
 			 
 			// Sets up the custom dialog
 			  final Dialog addItemDialog = new Dialog(ShowSheet.this);
 			  addItemDialog.setContentView(R.layout.edit_items);
 			  addItemDialog.setTitle("Edit item");
 			  
 			  EditText editText;
 			  if (item != null)
 			  {
 				  editText = (EditText) addItemDialog.findViewById(R.id.editText1);
 				  editText.setText(item.name);
 				  editText = (EditText) addItemDialog.findViewById(R.id.editText2);
 				  editText.setText(Integer.toString(item.weight));
 				  editText = (EditText) addItemDialog.findViewById(R.id.editText3);
 				  editText.setText(Integer.toString(item.amount));
 				  editText = (EditText) addItemDialog.findViewById(R.id.editText4);
 				  editText.setText(Integer.toString(item.price));
 			  }
 			  else
 			  {
 				  addItemDialog.setTitle("Sorry you are out of luck");
 				  editText = (EditText) addItemDialog.findViewById(R.id.editText1);
 				  editText.setText("some is wrong with the code");
 				  editText = (EditText) addItemDialog.findViewById(R.id.editText2);
 				  editText.setText("0");
 				  editText = (EditText) addItemDialog.findViewById(R.id.editText3);
 				  editText.setText("0");
 				  editText = (EditText) addItemDialog.findViewById(R.id.editText4);
 				  editText.setText("0");
 			  }
 
 			  Button addItemDialogDialogButton = (Button)addItemDialog
 			    .findViewById(R.id.button1);
 			  Button addItemDialogDialogButton2 = (Button)addItemDialog
 				.findViewById(R.id.button2);
 			
 			  // Exits the dialog if button is clicked
 			  addItemDialogDialogButton.setOnClickListener(new View.OnClickListener() 
 			  {
 		
 				     public void onClick(View v) 
 				     { 
 
 					    EditText textFromEditing;
 					    TextView text;
 					    	 
 				    	textFromEditing = (EditText)addItemDialog.findViewById(R.id.editText1);
 				    	item.name = textFromEditing.getText().toString();
 				    	textFromEditing = (EditText)addItemDialog.findViewById(R.id.editText2);
 				    	item.weight = Integer.parseInt(textFromEditing.getText().toString());
 				    	textFromEditing = (EditText)addItemDialog.findViewById(R.id.editText3);
 				    	item.amount = Integer.parseInt(textFromEditing.getText().toString());
 				    	textFromEditing = (EditText)addItemDialog.findViewById(R.id.editText4);
 				    	item.price = Integer.parseInt(textFromEditing.getText().toString());
 				    	 
 				    	text = (TextView)findViewById(item.nameID);
 				    	text.setText(item.name);
 				    	text = (TextView)findViewById(item.weightID);
 				    	text.setText(Integer.toString(item.weight));
 				    	text = (TextView)findViewById(item.amountID);
 				    	text.setText(Integer.toString(item.amount));
 				    	text = (TextView)findViewById(item.priceID);
 				    	text.setText(Integer.toString(item.price));
 				    	 
 				    	 myCharacter.replaceItem(item, item.nameID);
 				    	
 
 				    	 addItemDialog.dismiss();
 
 				     }
 			  });
 			  
 			  addItemDialogDialogButton2.setOnClickListener(new View.OnClickListener() 
 			  {
 		
 				     public void onClick(View v) 
 				     {
 				    	
 				    	TextView text;
 				    	LinearLayout layout;
 				    	
 				    	
 				    	text = (TextView)findViewById(item.nameID);
 				    	text.setVisibility(View.GONE);
 				    	layout = (LinearLayout) findViewById(R.id.itemNameBox);
 				    	layout.removeViewInLayout(text);
 				    	//layout.removeViewAt(text.getId());
 				    	layout.invalidate();
 				    	
 				    	text = (TextView)findViewById(item.weightID);
 				    	text.setVisibility(View.GONE);
 				    	layout = (LinearLayout) findViewById(R.id.itemWeightBox);
 				    	//layout.removeViewAt(text.getId());
 				    	layout.removeViewInLayout(text);
 				    	layout.invalidate();
 				    	
 				    	text = (TextView)findViewById(item.amountID);
 				    	text.setVisibility(View.GONE);
 				    	layout = (LinearLayout) findViewById(R.id.itemAmountBox);
 				    	//layout.removeViewAt(text.getId());
 				    	layout.removeViewInLayout(text);
 				    	layout.invalidate();
 				    	
 				    	text = (TextView)findViewById(item.priceID);
 				    	text.setVisibility(View.GONE);
 				    	layout = (LinearLayout) findViewById(R.id.itemPrisBox);
 				    	//layout.removeViewAt(text.getId());
 				    	layout.removeViewInLayout(text);
 				    	layout.invalidate();
 				    	
 				    	myCharacter.removeItem(item.nameID);
 				    	addItemDialog.dismiss();
 				     }
 			  });
 
 			  addItemDialog.show();	
 			 
 			 
 			 
 		  }
 		};
 
 	@Override
 	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 	@Override
 	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
         // When the given tab is selected, switch to the corresponding page in the ViewPager.
         mViewPager.setCurrentItem(tab.getPosition());
                
         if(tab.getPosition()== 3){
         	spellList = (ExpandableListView) findViewById(R.id.SpellList);
             spellList.setAdapter(spellListAdapter);        	
         }		
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 	////////////////////////////////////////////////////
 }
 
 
 
 
 
 
 
