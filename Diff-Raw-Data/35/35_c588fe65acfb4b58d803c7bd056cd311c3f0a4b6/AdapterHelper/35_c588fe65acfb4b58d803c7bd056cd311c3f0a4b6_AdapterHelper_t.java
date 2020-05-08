 package app.xzone.storyline.helper;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import app.xzone.storyline.R;
 import app.xzone.storyline.adapter.DBAdapter;
 import app.xzone.storyline.adapter.LazyAdapter;
 import app.xzone.storyline.model.Event;
 import app.xzone.storyline.model.Story;
 
 public class AdapterHelper {
 	private static ListView list;
 	private static LinearLayout ll;
 	private static LazyAdapter adapter;
 
 	public static void buildListViewAdapter(final Activity a) {
 		DBAdapter db = new DBAdapter(a.getApplicationContext());
 
 		list = (ListView) a.findViewById(R.id.list);
 
 		// Getting adapter by passing xml data ArrayList
 		adapter = new LazyAdapter(a, db.getAllStory());
 		list.setAdapter(adapter);
 
 		// Click event handler for single row item
 		list.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				ImageButton listButton = (ImageButton) a
 						.findViewById(R.id.menuListButton);
 				listButton.performClick();
 				Helper.buildUIMain(a, (Story) adapter.getItem(position));
 
 			}
 
 		});
 	}
 
 	public static ViewGroup buildBubbleEventAdapter(final Activity a, Event event) {
 
 		LayoutInflater inflater = (LayoutInflater) a.getApplicationContext()
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		
 		View vi = inflater.inflate(R.layout.main_bubble_right, null);
 		
 		if(event.getTransportation().equals("Car") || event.getTransportation().equals("Bus") || event.getTransportation().equals("Walk"))
 			vi = inflater.inflate(R.layout.main_bubble_right_images, null);
 
		vi.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("------ masuk after click bubble event!!");
				 
			}
		});
		
		
 //		construct UI element for bubble event
 		TextView title = (TextView) vi.findViewById(R.id.titleEvent);
 		ImageView trans = (ImageView) vi.findViewById(R.id.transport);
 		TextView loc = (TextView) vi.findViewById(R.id.location);
 		TextView note = (TextView) vi.findViewById(R.id.bubble_note);
 		TextView date = (TextView) vi.findViewById(R.id.time_note);
 		
 		//set object event into element
 		title.setTag(event);
 		
 		//set transportation pointer
 		int pointerIcon = (event.getTransportation().equals("Flight")) ? R.drawable.pointer_plane : R.drawable.pointer_car;
 		trans.setImageDrawable(a.getResources().getDrawable(pointerIcon));
 		
 		//set title of event
 		title.setText(event.getName());
 		//set location name
 		loc.setText(event.getLocname());
 		//set bubble note
 		note.setText(event.getMessage());
 		//set bubble date
 		date.setText("7 Aug 2013 10:15 AM");
 		
 		
 
 		View bubble = a.findViewById(R.id.body_content);
 		ViewGroup parent = (ViewGroup) bubble.getParent();
 		int index = parent.indexOfChild(bubble);
 		
 		
 		parent.addView(vi, index, new ViewGroup.LayoutParams(
 				ViewGroup.LayoutParams.FILL_PARENT,
 				ViewGroup.LayoutParams.WRAP_CONTENT));
 
 		System.out.println("---------  index :"+index);
 		System.out.println("---------  getChildCount :"+parent.getChildCount());
 		
 		return parent;
 	}
 }
