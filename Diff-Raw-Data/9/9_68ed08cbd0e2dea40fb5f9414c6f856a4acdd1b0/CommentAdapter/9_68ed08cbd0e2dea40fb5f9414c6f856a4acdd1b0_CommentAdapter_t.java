 /**
  * 
  */
 package com.xenon.greenup;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import android.app.Activity;
 import android.graphics.Matrix;
 import android.graphics.Shader.TileMode;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.xenon.greenup.api.Comment;
 
 /**
  * @author Ethan Joachim Eldridge
  *This is an array adapter class designed for performance using the 
  *guidelines specified by http://www.vogella.com/articles/AndroidListView/article.html#listsactivity_performance
  */
 public class CommentAdapter extends ArrayAdapter<Comment> {
 	private final Activity context;
 	private ArrayList<Comment> comments;
 	
 	static class ViewHolder {
 		public TextView text; //icon is center
 		public ImageView bottom;
 		public ImageView top;
 	}
 	
 	public CommentAdapter(Activity feedSectionFragment, ArrayList<Comment> comments){
 		super(feedSectionFragment, R.layout.comment,comments);
 		this.context = feedSectionFragment;
 		this.comments  = comments;
 	}
 	
 	/**
 	 * Returns an array of the top,center, and bottom. In that order
 	 * @param typeOfComment
 	 * @return
 	 */
 	private int[] getResourceByType(String typeOfComment){
 		//TODO Match types to colors for drawables
 		if("FORUM".equalsIgnoreCase(typeOfComment)){
 			return new int[]{R.drawable.bubble_blue_top, R.drawable.bubble_blue_center,R.drawable.bubble_blue_bottom};
 		}
 		if("GENERAL MESSAGE".equalsIgnoreCase(typeOfComment)){
 			return new int[]{R.drawable.bubble_green_top, R.drawable.bubble_green_center,R.drawable.bubble_green_bottom};
 		}
 		if("TRASH PICKUP".equalsIgnoreCase(typeOfComment)){
 			return new int[]{R.drawable.bubble_orange_top, R.drawable.bubble_orange_center,R.drawable.bubble_orange_bottom};
 		}
 		//Default/Help needed
 		return new int[]{R.drawable.bubble_blue_top, R.drawable.bubble_blue_center,R.drawable.bubble_blue_bottom};
 	}
 	
 	private int getReverseOf(int resource){
 		switch(resource){
 		case R.drawable.bubble_orange_bottom:
 			return R.drawable.bubble_orange_bottom_reverse;
 		case R.drawable.bubble_green_bottom:
 			return R.drawable.bubble_green_bottom_reverse;
 		case R.drawable.bubble_blue_bottom:
 		default:
 			return R.drawable.bubble_blue_bottom_reverse;
 		}
 	}
 	
 	@Override
 	  public View getView(int position, View convertView, ViewGroup parent) {
 	    View rowView = convertView;
 	    if (rowView == null) {
 	      LayoutInflater inflater = context.getLayoutInflater();
 	      rowView = inflater.inflate(R.layout.comment, null);
 	      ViewHolder viewHolder = new ViewHolder();
 	      viewHolder.text = (TextView) rowView.findViewById(R.id.comment_grid_center_text);
 	      viewHolder.bottom= (ImageView) rowView.findViewById(R.id.comment_grid_bottom);
 	      viewHolder.top = (ImageView) rowView.findViewById(R.id.comment_grid_top);
 	      rowView.setTag(viewHolder);
 	    }
 
 	    ViewHolder holder = (ViewHolder) rowView.getTag();
 	    
 	    Comment comment= comments.get(position);
 	    
 	    StringBuilder sb = new StringBuilder();
 	    sb.append(comment.getMessage());
 	    sb.append( System.getProperty("line.separator"));
 	    //Determine the twitter-esk stamp
 	    SimpleDateFormat sdf = new SimpleDateFormat("E LLL d kk:mm:ss yyyy",Locale.US);
 	    //The app engines timestamps are GMT
 	    sdf.setTimeZone(TimeZone.getTimeZone("GMT+4"));
 	    Date fromStamp = null;
 	    try {
	    	String ts = comment.getTimestamp();
	    	//Make sure that we have a timestamp or we'll have an NPE
	    	if(ts != null)
	    		fromStamp = sdf.parse(ts);
 		} catch (ParseException e) {
 			// TODO handle the  bad parse of the date?
 			e.printStackTrace();
 		}
 	    //Get the difference
 	    String difference; 
 	    if(fromStamp != null) {
 	    	long timeDiffInMilli = abs(new Date().getTime() -  fromStamp.getTime());
 	    	Log.i("time",""+timeDiffInMilli);
 	    	
 	    	long daysAgo = timeDiffInMilli/86400000;
 	    	long hoursAgo = ((timeDiffInMilli/(1000*60*60)) % 24);
 	    	long minutesAgo = (long) (timeDiffInMilli/(1000*60)) % 60;
     		long secondsAgo = (long) (timeDiffInMilli/1000) % 60;
     		Log.i("time", "Hours ago: "+hoursAgo);
     		Log.i("time", "Minutes ago: " + minutesAgo);
     	
     		if(daysAgo > 0) {
 	    		difference = String.format(Locale.US,"%d days ago",daysAgo);
 	    	} else if(hoursAgo > 0){
 	    		if (hoursAgo == 1) {
 	    			difference = String.format(Locale.US,"%d hour, %d minutes ago", hoursAgo,minutesAgo);
 	    		} else {
 	    			difference = String.format(Locale.US,"%d hours, %d minutes ago", hoursAgo,minutesAgo);
 	    		}
 	    	} else {
 	    		difference = String.format(Locale.US, "%d minutes and %d seconds ago",minutesAgo,secondsAgo);
 	    	}
 	    }else{
 	    	//For a default if we can't figure it out we'll just give em the timestamp
	    	//Also check for null in the case one was never set above
	    	difference = comment.getTimestamp() == null ? "" : comment.getTimestamp();
 	    }	    
 	    sb.append(difference);
 	    holder.text.setText(sb.toString());
 	   
 	    //Use the type of the comment to determine what color it shall be
 	    int [] topCenterBottomResourceIds = getResourceByType(comment.getType());
 
 	    holder.top.setBackgroundResource(topCenterBottomResourceIds[0]);
 	    //Tile the center background
 	    BitmapDrawable background = (BitmapDrawable)this.context.getResources().getDrawable(topCenterBottomResourceIds[1]);
         background.setTileModeXY(TileMode.REPEAT,TileMode.REPEAT);
         holder.text.setBackground(background);
 	    //holder.text.setBackgroundResource(();
 	    if(position % 2 == 0) {  
 	    	holder.bottom.setBackgroundResource(getReverseOf(topCenterBottomResourceIds[2]));
 	    } else {
 	    	holder.bottom.setBackgroundResource(topCenterBottomResourceIds[2]);
 	    }
 	    
 	    
 	    return rowView;
 	  }
 
 	private long abs(long l) {
 		if(l < 0){
 			return l*-1;
 		}
 		return l;
 	}
 	
 }
