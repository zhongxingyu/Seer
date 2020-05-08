 package com.nguyenmp.gauchodroid.forum;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.nguyenmp.gauchodroid.R;
 import com.nguyenmp.gauchospace.thing.Forum;
 
 public class ForumsListAdapter extends BaseAdapter {
 	private final List<Forum> mForums;
 	private final Context mContext;
 	
 	ForumsListAdapter(Context context, List<Forum> forums) {
 		mForums = forums;
 		mContext = context;
 	}
 
 	@Override
 	public int getCount() {
 		return mForums.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return mForums.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return 0;
 	}
 
 	@Override
 	public View getView(final int position, View convertView, ViewGroup container) {
 		//Get the forum we want to display
 		Forum forum = mForums.get(position);
 		
 		//Initialize the view if it isn't a recycled one
 		if (convertView == null) {
 			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_forum, container, false);
 		}
 		
 		TextView nameTextView = (TextView) convertView.findViewById(R.id.list_item_forum_name);
 		nameTextView.setText(forum.getName());
 		
 		TextView descriptionTextView = (TextView) convertView.findViewById(R.id.list_item_forum_description);
		descriptionTextView.setText(forum.getDescription());
 		
 		TextView discussionsTextView = (TextView) convertView.findViewById(R.id.list_item_forum_discussions);
 		discussionsTextView.setText("Discussions: " + forum.getNumberOfDiscussions());
 		
 		//Return the generated view
 		return convertView;
 	}
 
 }
