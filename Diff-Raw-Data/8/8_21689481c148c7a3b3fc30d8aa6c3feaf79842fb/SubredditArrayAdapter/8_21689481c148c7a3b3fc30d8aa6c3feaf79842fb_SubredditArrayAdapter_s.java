 package net.acuttone.reddimg;
 
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class SubredditArrayAdapter extends ArrayAdapter<String> {
 	private final Activity context;
 	private final List<String> mySubreddits;
 	private List<String> subredditsFromPref;
 	private int drawableId;
 	private boolean hideSelected;
 
 	public SubredditArrayAdapter(Activity context, List<String> names, int drawableId, boolean hideSelected) {
 		super(context, R.layout.subredditlistview, names);
 		this.context = context;
 		this.mySubreddits = names;
 		this.drawableId = drawableId;
 		this.hideSelected = hideSelected;
 	}
 
 	@Override
 	public void add(String object) {
 		if (mySubreddits.contains(object) == false) {
 			mySubreddits.add(object);
			Collections.sort(mySubreddits, String.CASE_INSENSITIVE_ORDER);
 		}
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		LayoutInflater inflater = context.getLayoutInflater();
 		View itemView = inflater.inflate(R.layout.subredditlistview, null, true);
 		TextView textView = (TextView) itemView.findViewById(R.id.subredditlistview_textview);
 		String selected = mySubreddits.get(position);
 		textView.setText(selected);
 		ImageView imgview = (ImageView) itemView.findViewById(R.id.subredditlistview_imageview);
 		imgview.setImageResource(drawableId);
 		
 		if(hideSelected && SubredditsPickerActivity.getSubredditsFromPref().contains(selected)) {
 			imgview.setVisibility(View.INVISIBLE);
 		}
 		return itemView;
 	}
 
 	public List<String> getSubreddits() {
 		return mySubreddits;
 	}
 	
 	@Override
 	public void notifyDataSetChanged() {
 		super.notifyDataSetChanged();
 	}
 }
