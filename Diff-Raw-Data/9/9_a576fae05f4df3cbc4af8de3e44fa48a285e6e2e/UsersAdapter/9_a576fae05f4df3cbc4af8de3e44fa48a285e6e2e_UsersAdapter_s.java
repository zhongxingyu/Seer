 package com.chalmers.feedlr.adapters;
 
 import java.util.ArrayList;
 
 import com.chalmers.feedlr.R;
 import com.chalmers.feedlr.model.User;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckedTextView;
 
 public class UsersAdapter extends ArrayAdapter<User> {
 
 	private ArrayList<User> users;
 	private Context context;
 
 	public UsersAdapter(Context context, int textViewResourceId,
 			ArrayList<User> users) {
 		super(context, textViewResourceId, users);
 		this.users = users;
 		this.context = context;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View v = convertView;
 		if (v == null) {
 			LayoutInflater vi = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.list_item, null);
 		}
 
 		User user = users.get(position);
 		if (user != null) {
 			CheckedTextView ctv = (CheckedTextView) v.findViewById(R.id.checked_text_view);
 			if (ctv != null)
 				ctv.setText(user.getUserName());	
 		}
 		return v;
 	}
 }
