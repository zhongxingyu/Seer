 package com.example.payback;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.example.payback.Notification;
 import com.example.payback.PageKillReceiver;
 import com.example.payback.TitleActivity;
 
 
 public class NotificationActivity extends TitleActivity
 {
 
 	static Activity activityInstance;	//these are variables
 	static PageKillReceiver pkr;		//used for PageKillReceiver.java
 	static IntentFilter filter;
 	
	static final boolean DEMO = false;
 	static ArrayList<CheckBox> cbs;
 	ListView listview;
 	ArrayList<Notification> nots;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		modifyTitle("Notifications",R.layout.activity_notification);
 		
 		activityInstance = this;
 		pkr = new PageKillReceiver(); pkr.setActivityInstance(activityInstance);
 		filter = new IntentFilter();
 		filter.addAction("com.Payback.Logout_Intent");
 		registerReceiver(pkr, filter);
 
 		try {
 			user.setNotifications(user.parseNotifs(AccessNet.lookupAllNotifs(user.getEmail(),user.getPassword()),user.getEmail()));
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		nots = user.getNotifications();
 
 		listview = (ListView) findViewById(R.id.listview);
 		
 		if(DEMO)
 		{
 			nots.add(new Notification("Santa C.", "Will", "Santa added you as a friend."));
 			nots.add(new Notification("Nigerian P.", "Will", "Nigerian has sent you notice of a pending transaction."));
 			nots.add(new Notification("Nigerian P.", "Will", "Nigerian has sent you notice of a pending transaction."));
 			nots.add(new Notification("Nigerian P.", "Will", "Nigerian has sent you notice of a pending transaction."));
 			nots.add(new Notification("Nigerian P.", "Will", "Nigerian has sent you notice of a pending transaction."));
 
 		}
 		
 			//final StableArrayAdapter adapter = new StableArrayAdapter(this,android.R.layout.simple_list_item_1, al);
 		
 		View v = getLayoutInflater().inflate(R.layout.activity_noti_footer, null);
 		listview.addFooterView(v); //for the delete button
 		
 		Collections.sort(nots);
 		Collections.reverse(nots);
 		cbs = new ArrayList<CheckBox>();
 		NotiAdapter na = new NotiAdapter(this, android.R.layout.simple_list_item_1, nots, cbs);
 		listview.setAdapter(na);
 		setContentView(listview);
 	}
 	public void deleteThese(View v)
 	{
 		for(int i = cbs.size() - 1; i >= 0; i--) //backwards so that our ordering doesn't get screwed up
 		{
 			if(cbs.get(i).isChecked())
 				nots.remove(i);
 		}
 		cbs = new ArrayList<CheckBox>();
 		NotiAdapter na = new NotiAdapter(this, android.R.layout.simple_list_item_1, nots, cbs);
 		listview.setAdapter(na);
 		setContentView(listview);
 		//TODO: delete all of the checked notifications server-side
 	}
 
 }
 
 class NotiAdapter extends ArrayAdapter<Notification>
 {
 	private final Context context;
 	private final ArrayList<Notification> al;
 	private ArrayList<CheckBox> cbs;
 	View rowView;
 	public NotiAdapter (Context context, int textViewResourceId, ArrayList<Notification> al, ArrayList<CheckBox> cbs)
 	{
 		super(context, R.layout.activity_notification, al);
 		this.context = context;
 		this.al = al;
 		this.cbs = cbs;
 	}
 	
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent)
 	{
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		rowView = inflater.inflate(R.layout.activity_noti_row_layout, parent, false);
 	    TextView from = (TextView) rowView.findViewById(R.id.from);
 	    TextView date = (TextView) rowView.findViewById(R.id.date);
 	    TextView message = (TextView) rowView.findViewById(R.id.secondLine);
 	    cbs.add((CheckBox) rowView.findViewById(R.id.check));
 	    
 	    if(!al.get(position).isRead()) //unread messages are yellow
 	    {
 	    	from.setTextColor(Color.YELLOW);
 	    	date.setTextColor(Color.YELLOW);
 	    	message.setTextColor(Color.YELLOW);
 	    }
 	    
 	    //TODO: set the following text to the person's first name and last name with a server call, instead of just their email
 	    from.setText(al.get(position).getFromEmail());
 	    date.setText(al.get(position).getDate());
 	    message.setText(al.get(position).getMessage());
 	    
 	    if(!al.get(position).isRead()) //add a listener if it hasn't been read
 	    {
 	    	rowView.setOnClickListener(new View.OnClickListener() {
 	    		@Override
 	    		public void onClick(View v) //mark as read
 	    		{
 	    			
 	    			TextView fromInner = (TextView) v.findViewById(R.id.from);
 	    			fromInner.setTextColor(Color.WHITE);
 
 	    			TextView dateInner = (TextView) v.findViewById(R.id.date);
 	    			dateInner.setTextColor(Color.WHITE);
 
 	    			TextView messageInner = (TextView) v.findViewById(R.id.secondLine);
 	    			messageInner.setTextColor(Color.WHITE);
 	    			
 	    			//TODO: update the server so that this notification is now read
 	    		}
 	    	});
 	    }
         
 	    return rowView;
 	}
 }
