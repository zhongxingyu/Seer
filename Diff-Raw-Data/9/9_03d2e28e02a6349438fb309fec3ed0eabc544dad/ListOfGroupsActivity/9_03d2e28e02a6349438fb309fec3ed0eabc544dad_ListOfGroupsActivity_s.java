 package org.SdkYoungHeads.DoIKnowYou;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.content.Context;
 import android.content.Intent;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ListOfGroupsActivity extends Activity implements OnItemClickListener {
 
 
 	protected ListView groups;
 	
 	Group[] listItems;
 	protected ArrayAdapter<Group> adapter;
 		
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO: Tady vypsat seznam skupin...
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.listofgroups);
 		
 		groups = (ListView) this.findViewById(R.id.list_of_groups);
 		
 		((Application)getApplication()).getDatabase().createExampleData();
 		
 		groups.setAdapter(new MyGroupAdapter(this.getBaseContext(), ((Application)getApplication()).getDatabase()));
 		groups.setOnItemClickListener(this);
 		
 		/*
		 * listener pro tlatko pidn nov osoby
 		 */
 		 Button next = (Button) findViewById(R.id.addNewPersonButton);
 	        next.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View view) {
 	                Intent myIntent = new Intent(view.getContext(), AddNewPersonActivity.class);
 	                startActivityForResult(myIntent, 0);
 	            }
 
 	        });
 	}
 	
 	class MyGroupAdapter extends ArrayAdapter<Group> {
 		
 		protected Context context;
 		protected GroupContainer gc;
 		
 
 		public MyGroupAdapter(Context context, GroupContainer gc) {
 			super(ListOfGroupsActivity.this, R.layout.listofgroups_row, gc.getGroups());
 			this.context = context;
 			this.gc = gc;
 		}
 		
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View rowView = inflater.inflate(R.layout.listofgroups_row, parent, false);
 			
 			TextView groupName = (TextView) rowView.findViewById(R.id.group_name);
 			TextView groupDescription = (TextView) rowView.findViewById(R.id.group_description);
 			TextView groupCount = (TextView) rowView.findViewById(R.id.group_count);
 //			ImageView groupIcon = (ImageView) rowView.findViewById(R.id.group_icon);
 //			ImageView groupArrow = (ImageView) rowView.findViewById(R.id.group_arrow);
 			
 			Group g = gc.getGroups()[position];
 			groupName.setText(g.getName());
 			groupDescription.setText("[ Description ]");
 			groupCount.setText(Integer.toString(g.getCount()));
 			
 		
 			// Change the icon for Windows and iPhone
 //			String s = values[position];
 //			if (s.startsWith("iPhone")) {
 //				imageView.setImageResource(R.drawable.no);
 //			} else {
 //				imageView.setImageResource(R.drawable.ok);
 //			}
 
 			return rowView;
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
 		Application app = (Application)getApplication();
 		app.selectedGroup = app.getDatabase().getGroups()[position];
 		Intent i = new Intent(this, GroupActivity.class);
 		startActivity(i);
 	}
 }
