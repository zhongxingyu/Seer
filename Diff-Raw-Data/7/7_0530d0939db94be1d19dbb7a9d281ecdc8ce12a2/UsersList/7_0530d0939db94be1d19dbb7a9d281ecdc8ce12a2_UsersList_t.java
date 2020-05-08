 package io.meetme.adapter;
 
 import io.meetme.R;
 import io.meetme.database.User;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.View;
 import android.widget.TextView;
 
 public class UsersList extends BaseAdapterSnippet {
 
     private ArrayList<User> users = new ArrayList<User>();
 
     public UsersList(Context context) {
 	super(context);
     }
 
     @Override
     public int getCount() {
 	return users.size();
     }
 
     @Override
     public Object getItem(int position) {
 	return users.get(position);
     }
 
     @Override
     public View getInflatedRow(int position) {
 	return getLayoutInflater().inflate(R.layout.row_leaderboard, null);
     }
 
     @Override
     protected Object getNewHolder() {
 	return new Holder();
     }
 
     @Override
     protected void initializeHolder(Object yourHolder, View view, int position) {
 	Holder holder = (Holder) yourHolder;
 	User user = users.get(position);
 	holder.no.setText(Integer.toString(position + 1) + ".");
 	holder.username.setText(user.getName());
 	holder.points.setText(Integer.toString(user.getPoints()));
     }
 
     @Override
     protected void inflateHolder(Object yourHolder, View view) {
 	Holder holder = (Holder) yourHolder;
 	holder.no = (TextView) view.findViewById(R.id.no);
 	holder.username = (TextView) view.findViewById(R.id.username);
 	holder.points = (TextView) view.findViewById(R.id.points);
     }
 
     private static class Holder {
 	public TextView no, username, points;
     }
 
     public void addAll(ArrayList<User> result) {
	users.clear();
 	users.addAll(result);
 	notifyDataSetChanged();
     }
 
 }
