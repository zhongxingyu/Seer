 package edu.berkeley.cs160.theccertservice.splist;
 
 import java.util.ArrayList;
 
 import edu.berkeley.cs160.theccertservice.splist.ListActivity.CreateListDialog;
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FeedActivity extends Activity {
 
 	ExpandableListView exv;
 	//ArrayList<Item> sharedItems = new ArrayList<Item>();
 	//String[] feed;
 	
 	//Button logout;
 	//SharedPreferences settings;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.feed);
 		
 		//logout = (Button)findViewById(R.id.logoutButton);
 		//logout.setOnClickListener(new OnClickListener(){
 
 			//@Override
 			//public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				//settings = getSharedPreferences(MainActivity.SETTING_INFO, 0);  
 			   // SharedPreferences.Editor editor = settings.edit();
 			   // editor.putBoolean("hasLoggedIn", false);
 			    //editor.commit();
 			    
 				//Intent intent = new Intent(FeedActivity.this, MainActivity.class);
 				//startActivity(intent);
 			//}
         	
        // });
 		
 		displayFeeds();
 		
 	}
 	
 	private void displayFeeds(){
 
 		exv = (ExpandableListView)findViewById(R.id.expandableListView1);
 		exv.setAdapter(new FeedAdapter(this));
 		exv.setOnChildClickListener(new OnChildClickListener(){
 
 			@Override
 			public boolean onChildClick(ExpandableListView parent, View v,
 					int groupPosition, int childPosition, long id) {
 				// TODO Auto-generated method stub
 				showFeedDialog(groupPosition, childPosition);
 				return false;
 			}
 			
 		});
 		
 	}
 	
 	public void showFeedDialog(int groupPosition, int childPosition) {
 	    DialogFragment newFragment = new CreateFeedDialog(groupPosition, childPosition);
 	    newFragment.show(getFragmentManager(), "createFeedDialog");
 	}
 	
 	public class CreateFeedDialog extends DialogFragment {
 		//private EditText feedMessage;
 		//private TextView feedName;
 		//private TextView feedItem;
 		private TextView feedName;
 		private TextView feedItem;
 		private TextView feedPrice;
 		private TextView numPeopleSharing;
 		Button feedSend;
 		Button feedCancel;
 		
 		private TextView feedItemP;
 		private TextView feedPriceP;
 		private TextView numPeopleSharingP;
 		Button feedSendP;
 		Button feedCancelP;
 		
 		private TextView feedNameW;
 		private TextView feedItemW;
 		private TextView feedPriceW;
 		private TextView numPeopleSharingW;
 		private TextView feedResponse;
 		
 		int groupPosition;
 		int childPosition;
 		public CreateFeedDialog(int groupPosition, int childPosition) {
 			// Empty constructor required for DialogFragment
 			this.groupPosition = groupPosition;
 			this.childPosition = childPosition;
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View view=null;
 			if (groupPosition == 0) {
 				view = inflater.inflate(R.layout.create_feed_pick_up, container);
 				feedItemP = (TextView) view.findViewById(R.id.feedItemP);
				feedItemP.setText(FeedAdapter.itemsFriendsWillSplit.get(childPosition).toString());	
 				feedItemP.setTextColor(Color.RED);
				//System.out.println(FeedAdapter.itemsFriendsWillSplit.get(childPosition).toString());
 				
 				feedPriceP = (TextView) view.findViewById(R.id.feedPriceP);
 				feedPriceP.setText(String.valueOf(FeedAdapter.itemsFriendsWillSplit.get(childPosition).getPrice()));
 				feedPriceP.setTextColor(Color.RED);
				//System.out.println(FeedAdapter.itemsFriendsWillSplit.get(childPosition).getPrice());
 				
 				numPeopleSharingP = (TextView) view.findViewById(R.id.numPeopleSharingP);
				numPeopleSharingP.setText(String.valueOf(FeedAdapter.itemsFriendsWillSplit.get(childPosition)._numPeopleSharing));
 				numPeopleSharingP.setTextColor(Color.RED);
				//System.out.println(FeedAdapter.itemsFriendsWillSplit.get(childPosition)._numPeopleSharing);
 				
 				feedSendP = (Button) view.findViewById(R.id.feedSendP);
 				feedCancelP = (Button) view.findViewById(R.id.feedCancelP);
 
 				feedSendP.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						Toast.makeText(v.getContext(),
 								"Your Alert has been sent",
 								Toast.LENGTH_SHORT).show();
 						done();
 					}
 				});
 
 				feedCancelP.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						done();
 					}
 				});
 			}
 			if (groupPosition == 1) {
 				view = inflater.inflate(R.layout.create_feed_wait, container);
 				feedItemW = (TextView) view.findViewById(R.id.feedItemW);
 				feedItemW.setText(FeedAdapter.itemsIWillSplit.get(childPosition).toString());
 				feedItemW.setTextColor(Color.RED);
 				
 				feedPriceW = (TextView) view.findViewById(R.id.feedPriceW);
 				feedPriceW.setText(String.valueOf(FeedAdapter.itemsIWillSplit.get(childPosition).getPrice()));
 				feedPriceW.setTextColor(Color.RED);
 				
				
 				numPeopleSharingW = (TextView) view.findViewById(R.id.numPeopleSharingW);
				//numPeopleSharingW.setText(FeedAdapter.itemsIWillSplit.get(childPosition)._numPeopleSharing);
				numPeopleSharingW.setText(String.valueOf(FeedAdapter.itemsIWillSplit.get(childPosition)._numPeopleSharing));
 				numPeopleSharingW.setTextColor(Color.RED);

 				feedNameW = (TextView) view.findViewById(R.id.feedNameW);
 				feedNameW.setText(FeedAdapter.itemsIWillSplit.get(childPosition)._list._owner);
 				feedNameW.setTextColor(Color.RED);
 				
 				feedResponse = (TextView) view.findViewById(R.id.feedResponse);
 				feedResponse.setTextColor(Color.RED);
 				if(childPosition==0 ||childPosition==2){
 					feedResponse.setText("The owner has alerted you to pick up the item.");
 				}
 				else{
 					feedResponse.setText("No response!");
 				}
 				
 			}
 			if (groupPosition == 2) {
 				view = inflater.inflate(R.layout.create_feed, container);
 				feedName = (TextView) view.findViewById(R.id.feedName);
 				feedName.setText(FeedAdapter.itemsFriendsWantToSplit.get(childPosition)._list
 						.getOwner());
 				feedName.setText(FeedAdapter.itemsFriendsWantToSplit.get(childPosition)._list.getOwner());
 				feedName.setTextColor(Color.RED);
 
 				feedItem = (TextView) view.findViewById(R.id.feedItem);
 				feedItem.setText(FeedAdapter.itemsFriendsWantToSplit.get(childPosition).getName());
 				feedItem.setTextColor(Color.RED);
 
 				feedPrice = (TextView) view.findViewById(R.id.feedPrice);
 				Double price = FeedAdapter.itemsFriendsWantToSplit.get(childPosition).getPrice();
 				feedPrice.setText("$" + String.valueOf(price));
 				feedPrice.setTextColor(Color.RED);
 
 				numPeopleSharing = (TextView) view
 						.findViewById(R.id.numPeopleSharing);
 				Integer number = FeedAdapter.itemsFriendsWantToSplit.get(childPosition)._numPeopleSharing;
 				numPeopleSharing.setText(String.valueOf(number));
 				numPeopleSharing.setTextColor(Color.RED);
 
 				feedSend = (Button) view.findViewById(R.id.feedSend);
 				feedCancel = (Button) view.findViewById(R.id.feedCancel);
 
 				feedSend.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						Item item = FeedAdapter.itemsFriendsWantToSplit.get(childPosition);
 						if(!FeedAdapter.itemsIWillSplit.contains(item)){
 							item._peopleSharing.add(MainActivity.user);
 							FeedAdapter.itemsIWillSplit.add(item);
							FeedAdapter.itemsFriendsWantToSplit.remove(item);
							exv.collapseGroup(groupPosition);  
							exv.expandGroup(groupPosition);
							exv.collapseGroup(groupPosition-1);  
							exv.expandGroup(groupPosition-1);
 							Toast.makeText(v.getContext(),
 									"Your Message has been sent! You are now responsible for paying for your share of " + item._name,
 									Toast.LENGTH_SHORT).show();
 						}
 						//FeedAdapter.itemsIWillSplit.add(item);
 						//FeedAdapter.itemsFriendsWantToSplit.remove(item);
 
 
 						done();
 					}
 				});
 
 				feedCancel.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						done();
 					}
 				});
 				// getDialog().setTitle("Create List");
 			}
 			return view;
 		}
 		
 		public void done() {
 			this.dismiss();
 		}
 	}
 }
