 package com.kaist.crescendo.activity;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import com.kaist.crescendo.R;
 import com.kaist.crescendo.data.PlanData;
 import com.kaist.crescendo.data.PlanListAdapter;
 import com.kaist.crescendo.utils.MyStaticValue;
 
 public class PlanListActivity extends UpdateActivity {
 	
 	private static PlanListAdapter adapter;
 	private ListView listView;
 	
 	private final int DELETE_ID = 0;
 	private final int UPDATE_ID = 1;
 	
 	public final static PlanListAdapter getAdapterInstance() 
 	{
 		return adapter;
 	}
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		
 		super.onCreateContextMenu(menu, v, menuInfo);
 		
 		menu.add(0, DELETE_ID, 0, R.string.str_delete_plan);
 		menu.add(0, UPDATE_ID, 0, R.string.str_modify_plan2);
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterView.AdapterContextMenuInfo menuInfo;
 		menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 		int index = 0;
		boolean result = false;
 		
 		switch(item.getItemId())
 		{
 		case DELETE_ID:
 			
 			index = menuInfo.position;
 			result = deletePlan(((PlanData) adapter.getItem(index)).uId);
 			if(result == true)
 			{
 				/* 
 				 * TODO should update list.
 				 */
 			}
 			return true;
 		case UPDATE_ID:
 			index = menuInfo.position;
			//result = updatePlan(((PlanData) adapter.getItem(index)).uId);
 			// ADD NEW PLAN
 			Intent intent = new Intent();
 			intent.putExtra(MyStaticValue.MODE, MyStaticValue.MODE_UPDATE);
 			intent.putExtra(MyStaticValue.NUMBER, index);
 			
 			startActivityForResult(intent.setClass(getApplicationContext(), PlanEditorActivity.class), MyStaticValue.REQUESTCODE_UPDATEPLAN);
 			
 			if(result == true)
 			{
 				/* 
 				 * TODO should update list.
 				 */
 			}
 			return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_planslist);
 		setTitle(R.string.str_managing_plans);
 		
 		
 		listView = (ListView) findViewById(R.id.plans_list);
 		
 		
 		OnClickListener mClickListener = new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// ADD NEW PLAN
 				Intent intent = new Intent();
 				intent.putExtra(MyStaticValue.MODE, MyStaticValue.MODE_NEW);
 				
 				startActivityForResult(intent.setClass(getApplicationContext(), PlanEditorActivity.class), MyStaticValue.REQUESTCODE_ADDNEWPLAN);
 			}
 		};
 		
 //		OnItemLongClickListener mItemLongClickListener = new OnItemLongClickListener() {
 //
 //			@Override
 //			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 //					int arg2, long arg3) {
 //				// TODO Auto-generated method stub
 //				return true;
 //			}
 //		};
 		
 //		listView.setOnItemLongClickListener(mItemLongClickListener);
 		registerForContextMenu(listView);
 		
 		
 		findViewById(R.id.button_add_new_plan).setOnClickListener(mClickListener);
 		
 		/*
 		 *  TODO Get Plans List from server
 		 *  How to update my list?
 		 *  After get list, 
 		 */
 		getPlanList();
 		adapter = new PlanListAdapter(this);
 		
 		/* 
 		 *  temp code 
 		 */
 		SimpleDateFormat Formatter = new SimpleDateFormat("yyyy-MM-dd");
 		String date = Formatter.format(new Date());
 		
 		PlanData plan = new PlanData(MyStaticValue.PLANTYPE_DIET, "Test1", date, date, 0);
 		PlanData plan1 = new PlanData(MyStaticValue.PLANTYPE_DIET, "Test2", date, date, 0);
 		PlanData plan2 = new PlanData(MyStaticValue.PLANTYPE_DIET, "Test3", date, date, MyStaticValue.FRIDAY);
 		adapter.addItem(plan);
 		adapter.addItem(plan1);
 		adapter.addItem(plan2);
 		
 		/* TODO ̰  ϸ  ٵ.. */
 		//adapter.setListItems(lit);
 		listView.setAdapter(adapter);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		switch(requestCode){
 			case MyStaticValue.REQUESTCODE_ADDNEWPLAN: 
 				if(resultCode == RESULT_OK){ 
 					boolean result = data.getExtras().getBoolean("sucess");
 					if(result == true) /* user add new plan sucessfully */
 					{
 						/* 
 						 *  TODO  update list once more
 						 */
 					}
 				}
 				break;
 			case MyStaticValue.REQUESTCODE_UPDATEPLAN: 
 				if(resultCode == RESULT_OK){ 
 					boolean result = data.getExtras().getBoolean("sucess");
 					if(result == true) /* user add new plan sucessfully */
 					{
 						/* 
 						 *  TODO  update list once more
 						 */
 					}
 				}
 				break;
 		}
 	}
 	
 }
