 package com.eshiah.base;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ListView;
 
 import com.eshiah.adapter.RuleAdapter;
 import com.eshiah.core.RuleRecord;
 import com.eshiah.db.RuleTrackerOpenHelper;
 
 public class RuleListActivity extends Activity {
 	RuleAdapter ruleAdapter;
 	public static final int RULE_ENTRY_REQUEST_CODE = 1;
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         ListView ruleListView=(ListView)findViewById(R.id.rule_list);
         ruleAdapter = new RuleAdapter();
         ruleListView.setAdapter(ruleAdapter);
         
         
        RuleTrackerOpenHelper ruleTrackerOpenHelper = new RuleTrackerOpenHelper(this);
        ruleTrackerOpenHelper.getWritableDatabase();
         
         
     }
     @Override
     public boolean onCreateOptionsMenu(Menu m) {
     	super.onCreateOptionsMenu(m);
     	MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.rulelistmenu,m);
         return true;
     }
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
     	if (item.getItemId() == R.id.add_rule_menu_item) {
     		Intent intent = new Intent(this, AddRuleActivity.class);
     		//startActivity(intent);
     		startActivityForResult(intent, RULE_ENTRY_REQUEST_CODE);
     		return true;
     	}
     	return super.onOptionsItemSelected(item);
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	if (requestCode == RULE_ENTRY_REQUEST_CODE) {
     		if (resultCode == RESULT_OK) {
     			String ruleName = data.getStringExtra("RuleName");
     			String ruleTrigger = data.getStringExtra("RuleTrigger");
     			String ruleAction = data.getStringExtra("RuleAction");
     			ruleAdapter.addRuleRecord(new RuleRecord(ruleName,ruleTrigger,ruleAction));
     			ruleAdapter.notifyDataSetChanged();
     		}
     	}
     }
     
 }
