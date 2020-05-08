 package com.tabbie.android.radar;
 
 import java.net.URL;
 import java.util.List;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.animation.AnimationUtils;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class EventListAdapter extends BaseAdapter {
 	
 	private final Context context;
 	private final EventClickListener eventClickListener;
 	private final EventLocationClickListener eventLocationClickListener;
 	
 	private List<Event> events;
 	
 	
 	public ImageLoader imageLoader; // I don't know why this is public, but it is in the source
 
     public EventListAdapter(Context context, List<Event> events) {
     	this.events = events;
     	this.context = context;
     	this.eventClickListener = (EventClickListener) context;
     	this.eventLocationClickListener = (EventLocationClickListener) context;
     	imageLoader = new ImageLoader(context);
     }
 
     @Override
     public View getView(final int position,
     		View convertView, final ViewGroup parent) {
     	
       if (null == convertView) {
     	Log.i("EventListAdapter", "Instantiating a new list view element");
         convertView = LayoutInflater.from(context).inflate(R.layout.event_list_element, null);
       }
       
       final Event e = (Event) getItem(position);
       
       
       // Populate TextViews with data
       ((TextView) convertView
     		  .findViewById(R.id.event_text))
     		  .setText(e.name);
 
       ((TextView) convertView
     		  .findViewById(R.id.event_list_time))
     		  .setText(e.time.makeYourTime());
 
       ((TextView) convertView
           .findViewById(R.id.event_location))
           .setText(e.venueName);
 
       ((TextView) convertView
     		  .findViewById(R.id.upvotes))
     		  .setText(Integer.toString(e.radarCount));
       
      imageLoader.displayImage(e.image.toString(),
    		  (ImageView) convertView.findViewById(R.id.event_image));
       
       convertView.findViewById(R.id.list_list_element_layout).setOnClickListener(new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			eventClickListener.onEventClicked(e, position, imageLoader.getBitmap(e.image.toString()));
 		}
       });
       
       convertView.findViewById(R.id.location_image_layout).setOnClickListener(
           new OnClickListener() {
             public void onClick(View v) {
             	eventLocationClickListener.onEventLocationClicked(e);
             }
           });
       
       return convertView;
     }
 
 	@Override
 	public int getCount() {
 		return events.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return events.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 	
 	public interface EventClickListener {
 		public void onEventClicked(final Event e, final int position, final Bitmap image);
 	}
 	
 	public interface EventLocationClickListener {
 		public void onEventLocationClicked(final Event e);
 	}
   }
