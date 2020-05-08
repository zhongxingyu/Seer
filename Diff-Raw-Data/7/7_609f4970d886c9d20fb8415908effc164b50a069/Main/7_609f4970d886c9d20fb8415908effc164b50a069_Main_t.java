 package com.pate.diablo;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.pate.diablo.model.Class;
 import com.pate.diablo.model.DataModel;
 import com.pate.diablo.model.Skill;
 import com.pate.diablo.sectionlist.EntrySkill;
 import com.pate.diablo.sectionlist.EntrySkillAdapter;
 import com.pate.diablo.sectionlist.Item;
 import com.pate.diablo.sectionlist.SectionItem;
 
 public class Main extends SherlockListActivity {
     SpinnerAdapter  adapter;
     ArrayList<Item> items = new ArrayList<Item>();
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         adapter = ArrayAdapter.createFromResource(this, R.array.classes, android.R.layout.simple_dropdown_item_1line);
 
         ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayShowCustomEnabled(true);
         actionBar.setDisplayShowTitleEnabled(false);
         
         actionBar.setCustomView(R.layout.actionbar_custom_view);
         
         final Spinner selectedClass = (Spinner) findViewById(R.id.spinner_class);
         final Spinner requiredLevel = (Spinner) findViewById(R.id.spinner_level);
         
         selectedClass.setOnItemSelectedListener(new OnItemSelectedListener() {
 
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 // TODO Auto-generated method stub
                if (view != null)
                    Toast.makeText(view.getContext(),selectedClass.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
                 
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> parent) {
                 // TODO Auto-generated method stub
                 
             }
 
         });
         requiredLevel.setOnItemSelectedListener(new OnItemSelectedListener() {
             
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 // TODO Auto-generated method stub
                if (view != null)
                    Toast.makeText(view.getContext(),requiredLevel.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
                 
             }
             
             @Override
             public void onNothingSelected(AdapterView<?> parent) {
                 // TODO Auto-generated method stub
                 
             }
             
         });
         Log.i("SelectedClass", selectedClass.getSelectedItem().toString());
 		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
 				.create();
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				getResources().openRawResource(R.raw.classes)));
 
 		String fakeJson = "";
 		String line;
 		try {
 			line = reader.readLine();
 			while (line != null) {
 				fakeJson = fakeJson + line;
 				line = reader.readLine();
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		DataModel dm = gson.fromJson(fakeJson, DataModel.class);
 		List<Class> classes = dm.getClasses();
 
         items.add(new SectionItem("Primary"));
         
         for (Skill s : dm.getClassByName(selectedClass.getSelectedItem().toString()).getActiveSkills())
         {
             items.add(new EntrySkill(s));
         }
         /*items.add(new EntryItem("Left Click Skill", "Attack"));
         items.add(new EntryItem("Left Click Rune", "Some Rune"));
         
         items.add(new SectionItem("Secondary"));
         items.add(new EntryItem("Right Click", "Fireball"));
         items.add(new EntryItem("Right Click Rune", "Another rune"));
 
         items.add(new SectionItem("Defensive"));
         items.add(new EntryItem("Skill 1", "Skill 1"));
         items.add(new EntryItem("Skill 1 Rune", "Skill 1 Rune"));
         
         items.add(new SectionItem("Might"));
         items.add(new EntryItem("Skill 2", "Skill 2"));
         items.add(new EntryItem("Skill 2 Rune", "Skill 2 Rune"));
         
         items.add(new SectionItem("Tactics"));
         items.add(new EntryItem("Skill 3", "Skill 3"));
         items.add(new EntryItem("Skill 3 Rune", "Skill 3 Rune"));
         
         items.add(new SectionItem("Rage"));
         items.add(new EntryItem("Skill 4", "Skill 4"));
         items.add(new EntryItem("Skill 4 Rune", "Skill 4 Rune"));
         
         items.add(new SectionItem("Passive Skills"));
         items.add(new EntryItem("Passive 1", "Passive 1"));
         items.add(new EntryItem("Passive 2", "Passive 2"));
         items.add(new EntryItem("Passive 3", "Passive 3"));*/
 
         EntrySkillAdapter adapter = new EntrySkillAdapter(this, items);
 
         setListAdapter(adapter);
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
 
         if (!items.get(position).isSection()) {
 
             EntrySkill item = (EntrySkill) items.get(position);
             
             Intent intent = new Intent(v.getContext(), SelectSkill.class);
             startActivity(intent);
             //Toast.makeText(this, "You clicked " + item.title, Toast.LENGTH_SHORT).show();
 
         }
 
         super.onListItemClick(l, v, position, id);
     }
 }
