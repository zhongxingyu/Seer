 package com.thesis.emostatus;
 
 /**
  * Created by vito on 25-11-13.
  */
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.ExpandableListActivity;
 import android.os.Bundle;
 import android.widget.ExpandableListAdapter;
 import android.widget.SimpleExpandableListAdapter;
 
 public class MonitorInfoActivity extends ExpandableListActivity {
     private static final String NAME = "NAME";
     private static final String IS_EVEN = "IS_EVEN";
 
     private ExpandableListAdapter mAdapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
         List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
 
         Map<String, String> curGroupMap1 = new HashMap<String, String>();
         groupData.add(curGroupMap1);
         curGroupMap1.put(NAME, "Skype");
 
         List<Map<String, String>> children1 = new ArrayList<Map<String, String>>();
 
         Map<String, String> curChildMap11 = new HashMap<String, String>();
         children1.add(curChildMap11);
        curChildMap11.put(IS_EVEN, "EL sistema detectará el estado anímico a partir de las videollamadas que se hagan en el dispositivo del usuario.");
         childData.add(children1);
 
         Map<String, String> curGroupMap2 = new HashMap<String, String>();
         groupData.add(curGroupMap2);
         curGroupMap2.put(NAME, "Grabación");
 
         List<Map<String, String>> children2 = new ArrayList<Map<String, String>>();
 
         Map<String, String> curChildMap21 = new HashMap<String, String>();
         children2.add(curChildMap21);
         curChildMap21.put(IS_EVEN, "El sistema detectará el estado anímico a partir de las grabaciones de voz que se hagan utilizando el micrófono del dispositivo del usuario.");
 
         Map<String, String> curChildMap22 = new HashMap<String, String>();
         children2.add(curChildMap22);
        curChildMap22.put(IS_EVEN, "Las grabaciones se harán con una determinada frecuencia(ej: cada 15 min). Tú solo debese preocuparte de configurar los días, y el horario de grabación.");
 
 
         childData.add(children2);
 
 
         // Set up our adapter
         mAdapter = new SimpleExpandableListAdapter(
                 this,
                 groupData,
                 android.R.layout.simple_expandable_list_item_1,
                 new String[] { NAME},
                 new int[] { android.R.id.text1},
                 childData,
                 R.layout.info_info,
                 new String[] {IS_EVEN },
                 new int[] { R.id.title}
         );
         setListAdapter(mAdapter);
     }
 
 }
