 package edu.berkeley.cs160.theccertservice.splist;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 
 public class FriendsActivity extends Activity {
 
 	ExpandableListView exv;
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.friends);
 		displayFeeds();
 	}
 	
 	private void displayFeeds(){
 		exv = (ExpandableListView)findViewById(R.id.expandableListView2);
 		exv.setAdapter(new FriendAdapter(this));
 		exv.setOnChildClickListener(new OnChildClickListener(){
 
 			@Override
 			public boolean onChildClick(ExpandableListView parent, View v,
 					int groupPosition, int childPosition, long id) {
 				// TODO Auto-generated method stub
 				if(groupPosition==0){
 					String name = FriendAdapter.friendsRequest.get(childPosition);
 					if(!FriendAdapter.friends.contains(name)){
 						FriendAdapter.friends.add(name);
 					}
					
 				}
 				return false;
 			}
 			
 		});
 	}
 }
