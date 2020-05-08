 package com.core.buga.adapter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 
 import com.core.buga.models.*;
import com.icemobile.workshop.daytwo.R;
 
 public class BugListAdapter extends BaseAdapter {
 
 	private List<Bug> bugitems = new ArrayList<Bug>(); 
 	private Context context;
 	
 	public BugListAdapter(final Context context) {
 		this.context = context;
 	}
 	
 	public void setList(final List<Bug> bugItems) {
 		this.bugitems = bugItems;
 		notifyDataSetChanged();
 	}
 	
 	@Override
 	public int getCount() {
 		if(bugitems != null){
 			return bugitems.size();
 		}
 		return 0;
 	}
 
 	@Override
 	public Object getItem(int position) {
 		if(bugitems != null){
 			return bugitems.get(position);
 		}
 		return null;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int postion, View convertView, ViewGroup arg2) {
		if(convertView == null){
			convertView = View.inflate(context, R.layout.rows_bug, null);
		}
 		
 		return null;
 	}
 
 }
