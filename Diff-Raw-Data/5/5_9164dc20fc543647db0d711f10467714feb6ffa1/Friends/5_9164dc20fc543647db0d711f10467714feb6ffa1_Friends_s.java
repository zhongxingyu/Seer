 package de.hska.shareyourspot.android.activites;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import de.hska.shareyourspot.android.R;
 import de.hska.shareyourspot.android.domain.User;
 import de.hska.shareyourspot.android.domain.Users;
 import de.hska.shareyourspot.android.restclient.RestClient;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 public class Friends extends Activity {
 
 	private RestClient restClient = new RestClient();
 	final Context context = this;
 	
 	private List<User> friends;
 	
 	private List<User> foundUsers;
 	
 	private User lookFor;
 		
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.friends);
 	}
 
 	public void showGroups(View view) {
 		Intent intent = new Intent(this, Groups.class);
 		startActivity(intent);
 	}
 
 	public void onClickSearch(View view) {
 		// TODO: search for users and get list, add friends	
 		this.lookFor = new User();
 		
		EditText username = (EditText) findViewById(R.id.lookForUser);
 		this.lookFor.setName(username.getText().toString());
 			
 		Users users = this.restClient.searchUser(this.lookFor);
 	
 //		TODO: add users to listUsers on UI
 		
 		this.foundUsers = new ArrayList<User>();
 		this.foundUsers.addAll(users.getAllUser());
 		
 		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, this.foundUsers);
 		
		ListView listUsers = (ListView)findViewById(R.id.listUsers);
 		listUsers.setAdapter(adapter);
 				
 		Intent intent = new Intent(this, Friends.class);
 		startActivity(intent);
 	}
 
 	public List<User> getFriends() {
 		return friends;
 	}
 
 	public void setFriends(List<User> friends) {
 		this.friends = friends;
 	}
 
 	public User getLookFor() {
 		return lookFor;
 	}
 
 	public void setLookFor(User lookFor) {
 		this.lookFor = lookFor;
 	}
 
 	public List<User> getFoundUsers() {
 		return foundUsers;
 	}
 
 	public void setFoundUsers(List<User> foundUsers) {
 		this.foundUsers = foundUsers;
 	}
 
 	private class StableArrayAdapter extends ArrayAdapter<String> {
 
 	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
 
 	    public StableArrayAdapter(Context context, int textViewResourceId,
 	        List<String> objects) {
 	      super(context, textViewResourceId, objects);
 	      for (int i = 0; i < objects.size(); ++i) {
 	        mIdMap.put(objects.get(i), i);
 	      }
 	    }
 
 	    @Override
 	    public long getItemId(int position) {
 	      String item = getItem(position);
 	      return mIdMap.get(item);
 	    }
 
 	    @Override
 	    public boolean hasStableIds() {
 	      return true;
 	    }
 
 	  }
 	
 }
