 package com.project.app;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.project.app.model.Party;
 import com.project.app.model.interfaces.Model;
 
 import java.util.List;
 
 /**
  * Created by raulete on 16/09/13.
  */
 public class TestsActivity extends Activity{
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_tests);
 
         runTests();
     }
 
     public void addTestView(boolean pass, String text){
         LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         View view;
         view = inflater.inflate(R.layout.test_view, null);
 
         TextView labelView = (TextView)view.findViewById(R.id.test_label_check);
         if(pass){
             labelView.setText("PASS");
         }else{
             labelView.setText("FAIL");
         }
 
         TextView textView = (TextView)view.findViewById(R.id.test_text_check);
         textView.setText(text);
 
         LinearLayout parentRoot = (LinearLayout)findViewById(R.id.activity_tests_layout);
         parentRoot.addView(view);
     }
 
     public void runTests(){
         testCheckAddTestView();
         testPartyCreateTableString();
         testPartySetValue();
         testPopulateDatabase();
         testFindPartyCiudadanosAfterPopulateSixParties();
     }
 
     public void testCheckAddTestView(){
         addTestView(true, "testCheckAddTestView");
     }
 
     public void testPartyCreateTableString(){
         Party party = new Party(this);
         if(party.getCreateSql().equals("CREATE TABLE parties (id integer, resource_id text, logo text, web text, _id integer primary key autoincrement, validate text, name text );")){
             addTestView(true, "testPartyCreateTableString");
         }else{
             addTestView(false, "testPartyCreateTableString");
         }
     }
 
     public void testPartySetValue(){
         Party party = new Party(this);
         party.setValue("name", "Ciudadanos");
         if(party.getValue("name").equals("Ciudadanos")){
             addTestView(true, "testPartySetValue");
         }else{
             addTestView(false, "testPartySetValue");
         }
     }
 
     public void testPopulateDatabase(){
         populateParties(4);
 
         Party party = new Party(this);
         List<Model> list = party.findAll();
 
         if(list.size() == 4){
             addTestView(true, "testPopulateDatabase");
         }else{
             addTestView(false, "testPopulateDatabase");
         }
     }
 
     public void testFindPartyCiudadanosAfterPopulateSixParties(){
         populateParties(6);
         Party staticParty = new Party(this);
 
         Model ciudadanosModel = staticParty.read(5);
 
         if(ciudadanosModel.getValue("name").equals("Ciudadanos")){
            addTestView(true, "findPartyCiudadanosAfterPopulateSixParties");
         }else{
            addTestView(false, "findPartyCiudadanosAfterPopulateSixParties");
         }
     }
 
     private void populateParties(int numOfPartiesToPopulate){
         Party party = new Party(this);
         party.clearTable();
         String[] parties = {"Ciudadanos", "PP", "PSOE", "IU"};
 
         for(int index = 0; index < numOfPartiesToPopulate; index++){
             party.setValue("_id", (index+1));
             party.setValue("name", parties[index%parties.length]);
             party.save(party);
         }
     }
 
 
 }
