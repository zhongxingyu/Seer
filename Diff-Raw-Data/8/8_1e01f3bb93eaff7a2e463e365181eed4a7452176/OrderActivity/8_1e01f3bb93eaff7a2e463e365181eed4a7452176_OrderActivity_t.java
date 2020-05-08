 package com.gourmet6;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Build;
 
 public class OrderActivity extends Activity {
 	
 	private Restaurant current = null;
 	private String[] dishesname = null;
 	private boolean fromRestaurant = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_order);
 		// Show the Up button in the action bar.
 		setupActionBar();
 
 		setTitle(R.string.activity_order_title);		
 		
 		Bundle extra = getIntent().getExtras();
 		this.fromRestaurant = extra.getBoolean("from");
 		
		/*ListView list = (ListView) findViewById(R.id.listView1);
 		this.dishesname = this.current.getDishesNames();
 		list.setAdapter(new DishAdapter(this,R.layout.dish_list_element,dishesname));
 		
 		list.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 				//TODO make the list of the selected dishes				
 			}
		});*/
 		
 		Button submit = (Button) findViewById(R.id.button1);
 		submit.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO check order
 				if(fromRestaurant){
 					AlertDialog.Builder builder = new AlertDialog.Builder(OrderActivity.this);
 					builder.setTitle(R.string.activity_reservation_title);
 					builder.setMessage(R.string.do_you_res);
 					
 					builder.setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO add a reservation
 							
 						}
 					});
 					builder.setNegativeButton(R.string.no_button, new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO quit
 							
 						}
 					});
 					
 					AlertDialog dialog = builder.create();
 					dialog.show();
 				}
 			}
 		});
 		
 	}
 	
 	private class DishAdapter extends ArrayAdapter<String>{
 		
 		private String[] dishlist;
 		boolean[] checkBoxState;
 
 		public DishAdapter(Context context, int textViewResourceId,String[] dishlist) {
 			super(context, textViewResourceId, dishlist);
 			checkBoxState=new boolean[dishlist.length];
 		}
 		
 		private class ViewHolder{
 			CheckBox box;
 			TextView name;
 		}
 		
 		@Override
 		public View getView(final int position, View convertView, ViewGroup parent){
 			
 			ViewHolder holder = null;
 			
 			if (convertView == null) {
 				LayoutInflater vi = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE);
 				convertView = vi.inflate(R.layout.dish_list_element, null);
 				
 				 holder = new ViewHolder();
 				 holder.name = (TextView) convertView.findViewById(R.id.dishname);
				 //holder.box = (CheckBox) convertView.findViewById(R.id.checkBox1);
 				 convertView.setTag(holder);
 				 
 			}
 			else {
 			     holder = (ViewHolder) convertView.getTag();
 			}	 
 			
 			holder.name.setText(dishlist[position]);
 			holder.box.setChecked(checkBoxState[position]);
 			
 			holder.box.setOnClickListener(new View.OnClickListener() {
 			     
 				   public void onClick(View v) {
 				    if(((CheckBox)v).isChecked())
 				     checkBoxState[position]=true;
 				    else
 				     checkBoxState[position]=false;				     
 				    }
 		   });
 			
 			return convertView;
 		}
 		
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.order, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
