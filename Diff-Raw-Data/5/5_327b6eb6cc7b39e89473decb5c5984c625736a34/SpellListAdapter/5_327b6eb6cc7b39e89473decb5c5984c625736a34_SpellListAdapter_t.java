 package com.pathfinder.character.sheet.spellList;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.pathfinder.character.sheet.R;
 
 public class SpellListAdapter {
 	private Context context;
 	private ArrayList<SpellLevel> spellLevels;
 	
 	public SpellListAdapter(Context context, ArrayList<SpellLevel> spellLevels) {
 		this.context = context;
 		this.spellLevels = spellLevels;
 	}
 	
 	public void addSpell(Spell spell, SpellLevel spellLevel) {
 		if(!spellLevels.contains(spellLevel)){
 			spellLevels.add(spellLevel);
 		}
 		
 		int index = spellLevels.indexOf(spellLevel);
 		ArrayList<Spell> spells = spellLevels.get(index).getSpells();
 		spells.add(spell);
 		spellLevels.get(index).setSpells(spells);
 	}
 	
 	public Object getSpell(int spellLevelPosition, int spellPosition) {
 		ArrayList<Spell> spell = spellLevels.get(spellLevelPosition).getSpells();
 		return spell.get(spellPosition);
 	}
 	
 	public View getSpellView(int spellListPosition, int spellPosition, View view) {
 		Spell spell = (Spell)getSpell(spellListPosition, spellPosition);
 		
 		if(view == null) {
 			LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.spell,null);
 		}
 		
 		TextView tv = (TextView) view.findViewById(R.id.tvSpell);
 		tv.setText(spell.getName().toString());
 		return view;
 	}
 	
 	public int getSpellCount(int spellLevelPosition) {
 		ArrayList<Spell> spells = spellLevels.get(spellLevelPosition).getSpells();
 		return spells.size();
 	}
 	
 	public Object getSpellLevel(int spellLevelPosition) {
 		return spellLevels.get(spellLevelPosition);
 	}
 	
 	public View getSpellLevelView(int spellLevelPosition, View view) {
 		SpellLevel spellLevel = (SpellLevel)getSpellLevel(spellLevelPosition);
 		
 		if (view == null) {
 			LayoutInflater infater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
			view = infater.inflate(R.layout.spell_level, null);
 		}
 		
 		TextView tv = (TextView) view.findViewById(R.id.tvSpellLevel);
 		tv.setText(spellLevel.getName());
 		return view;
 	}
 	
 	public int getSpellLevelCount() {
 		return spellLevels.size();		
 	}
 }
