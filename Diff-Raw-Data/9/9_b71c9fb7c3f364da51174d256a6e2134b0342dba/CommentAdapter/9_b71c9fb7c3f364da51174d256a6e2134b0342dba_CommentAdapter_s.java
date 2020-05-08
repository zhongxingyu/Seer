 /**
  * 
  */
 package com.xenon.greenup;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.graphics.Matrix;
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
 			return new int[]{R.drawable.bubble_blue_top, R.drawable.bubble_blue_center,R.drawable.bubble_blue_bottom};
 		}
 		if("TRASH PICKUP".equalsIgnoreCase(typeOfComment)){
 			return new int[]{R.drawable.bubble_blue_top, R.drawable.bubble_blue_center,R.drawable.bubble_blue_bottom};
 		}
 		//Default/Help needed
 		return new int[]{R.drawable.bubble_blue_top, R.drawable.bubble_blue_center,R.drawable.bubble_blue_bottom};
 	}
 	
 	private int getReverseOf(int resource){
 		switch(resource){
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
 	    holder.text.setText(comment.getMessage());
 	   
 	    //Use the type of the comment to determine what color it shall be
 	    int [] topCenterBottomResourceIds = getResourceByType(comment.getType());
 
 	    holder.top.setBackgroundResource(topCenterBottomResourceIds[0]);
	    //holder.text.setBackgroundResource((topCenterBottomResourceIds[1]));
 	    if(position % 2 == 0) {  
 	    	holder.bottom.setBackgroundResource(getReverseOf(topCenterBottomResourceIds[2]));
 	    } else {
 	    	holder.bottom.setBackgroundResource(topCenterBottomResourceIds[2]);
 	    }
 	    
 	    
 	    return rowView;
 	  }
 	
 }
