 
 package com.wemakestuff.d3builder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.os.ParcelUuid;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ListView;
 
 import com.wemakestuff.d3builder.model.D3Application;
 import com.wemakestuff.d3builder.model.Skill;
 import com.wemakestuff.d3builder.sectionlist.EntrySkill;
 import com.wemakestuff.d3builder.sectionlist.EntrySkillAdapter;
 import com.wemakestuff.d3builder.sectionlist.Item;
 
 public class SkillListFragment extends ListFragment
 {
 
     private Context             context;
     private String              skillType;
     private String              selectedClass;
     private int                 maxLevel    = 60;
    private OnClickListener     listener;
     private List<ParcelUuid>    excludeSkills;
 
     private ArrayList<Item>     items       = new ArrayList<Item>();
     private static final String KEY_CONTENT = "TestFragment:Content";
 
     public static SkillListFragment newInstance(String content, Context c, String skillType, String selectedClass, int maxLevel, List<ParcelUuid> excludeSkills)
     {
 
         SkillListFragment fragment = new SkillListFragment();
 
         fragment.context = c;
         fragment.skillType = skillType;
         fragment.selectedClass = selectedClass;
         fragment.maxLevel = maxLevel;
         fragment.excludeSkills = excludeSkills;
 
         return fragment;
     }
 
     private String mContent = "???";
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         
         if (savedInstanceState != null)
         {
             if (savedInstanceState.containsKey("skillType"))
             {
                 skillType = savedInstanceState.getString("skillType");
             }
 
             if (savedInstanceState.containsKey("selectedClass"))
             {
                 selectedClass = savedInstanceState.getString("selectedClass");
             }
 
             if (savedInstanceState.containsKey("maxLevel"))
             {
                 maxLevel = savedInstanceState.getInt("maxLevel");
             }
 
         }
 
         setRetainInstance(true);
         for (Skill s : D3Application.getInstance().getClassByName(selectedClass).getActiveSkillsByTypeAndRequiredLevel(skillType, maxLevel))
         {
             // Exclude already selected skills
             if (excludeSkills == null || !excludeSkills.contains(new ParcelUuid(s.getUuid())))
                 items.add(new EntrySkill(s));
         }
         EntrySkillAdapter adapter = new EntrySkillAdapter(getActivity(), items);
 
         setListAdapter(adapter);
         super.onCreate(savedInstanceState);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState)
     {
 
         super.onSaveInstanceState(outState);
         outState.putString("skillType", skillType);
         outState.putString("selectedClass", selectedClass);
         outState.putInt("maxLevel", maxLevel);
     }
 
     public void setOnListItemClickListener(OnClickListener listener)
     {
 
         this.listener = listener;
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id)
     {
 
         listener.onClick(v);
         super.onListItemClick(l, v, position, id);
     }
 
 }
