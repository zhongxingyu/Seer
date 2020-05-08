 package org.waynak.hackathon;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class EventsAdapater extends ArrayAdapter<Event> {
 	
 	private final Activity context;
 	private Event[] eventList;
 	private int layoutResourceId;
 	
 	public static final String LOGTAG = "EVENTSADAPTER";
 	
 	public EventsAdapater(Activity context, int layoutResourceId, Event[] eventList) {
 		super(context, layoutResourceId, eventList);
 		this.layoutResourceId = layoutResourceId;
 		this.eventList = eventList;
 		this.context=context;
 	}
 	
 	// static to save the reference to the outer class and to avoid access to
 	// any members of the containing class
 	static class ViewHolder {
 		public ImageView imageView ; 
 		public TextView eventView;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 	
 		// ViewHolder will buffer the assess to the individual fields of the row
 		// layout
 		ViewHolder holder;
 		// Recycle existing view if passed as parameter
 		// This will save memory and time on Android
 		// This only works if the base layout for all classes are the same
 		View rowView = convertView;
 		
 		if (rowView == null) {
 			LayoutInflater inflater =  context.getLayoutInflater();
 			rowView = inflater.inflate(layoutResourceId, null, true);
 			holder = new ViewHolder();
 			holder.eventView = (TextView) rowView.findViewById(R.id.eventsTv);
 			holder.imageView = (ImageView) rowView.findViewById(R.id.eventsImg);
 			rowView.setTag(holder);
 		} else {
 			holder = (ViewHolder) rowView.getTag();
 		}
 		
 		//String t = eventName[position];
 		//String d= imageList[position];
 	
 		holder.eventView.setText(eventList[position].eventName);
 		Log.v(LOGTAG,eventList[position].eventName);
 		//holder.imageView.setImageURI(Uri.parse(eventList[position].imageUrl));
		
		BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
		bmpFactoryOptions.inSampleSize = 4;
		Bitmap realBmp = BitmapFactory.decodeResource(context.getResources(), eventList[position].imageId,bmpFactoryOptions);

		
		//holder.imageView.setImageResource(eventList[position].imageId);
		holder.imageView.setImageBitmap(realBmp);
 		//URL url = new URL(imageList[position]);
 		
 		//InputStream is=null;
 		
 		//String sss = "http://www.cars-directory.net/pics/opel/omega/1988/opel_omega_1017239.jpg";
 		
 		//Bitmap x = imageFromUrl(sss);
         //holder.imageView.setImageBitmap(x);
 		//holder.descView.setText(d);
 		
 		return rowView;
 	}
 	
 	public Bitmap imageFromUrl(String url) {
 		Bitmap bitmapImage;
 		URL imageUrl = null;
 		try {
 		imageUrl = new URL(url);
 		} catch (MalformedURLException e) {
 		e.printStackTrace();
 		}
 		try {
 		HttpURLConnection httpConnection =
 		(HttpURLConnection) imageUrl.openConnection();
 		httpConnection.setDoInput(true);
 		httpConnection.connect();
 		InputStream is = httpConnection.getInputStream();
 		bitmapImage = BitmapFactory.decodeStream(is);
 		} catch (IOException e) {
 		e.printStackTrace();
 		bitmapImage = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
 		}
 		return bitmapImage;
 	}
 }
 		
 	
 
