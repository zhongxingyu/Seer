 package ca.on.rom.romsearch;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class AchievementListArrayAdapter extends ArrayAdapter<Achievement> {
 	private final Context context;
 	private final Achievement[] achievements;
 	private final int completed;
 	private final int items_complete;
 	private final int exhibits_complete;
 	
 	public AchievementListArrayAdapter(Context context, Achievement[] achievements,
 			int completed, int items_complete, int exhibits_complete) {
 		super(context, R.layout.achievement_row_layout, achievements);
 		this.context = context;
 		this.achievements = achievements;
 		this.completed = completed; //number of completed achievements
 		this.items_complete = items_complete;
 		this.exhibits_complete = exhibits_complete;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View rowView = inflater.inflate(R.layout.achievement_row_layout, parent, false);
 		TextView nameview = (TextView) rowView.findViewById(R.id.achievement_name);
 		TextView descview = (TextView) rowView.findViewById(R.id.achievement_description);
 		ImageView imageview = (ImageView) rowView.findViewById(R.id.achievement_image);
 		ProgressBar progress = (ProgressBar) rowView.findViewById(R.id.achievement_progress);
 		TextView completion = (TextView) rowView.findViewById(R.id.achievement_completion);
 		//set the views to have the correct values
 		Achievement achievement = achievements[position];
 		String name = achievement.getName();
 		nameview.setText(name);
 		progress.setMax(achievement.getRequirement());
 		//Since completed items are at head of list, completed if position < completed
 		if (position < completed) {
 			imageview.setImageResource(achievement.getImage());
 			//remove the progress bar and completion
 			progress.setVisibility(View.GONE);
 			completion.setVisibility(View.GONE);
 		} else {
 			imageview.setImageResource(R.drawable.achievement_locked);
 		}
 		//set the description of the achievement
 		String desc;
 		if (achievement.getType().equals("i")) {
 			desc = "Find " + Integer.toString(achievement.getRequirement()) + " items";
 			completion.setText(items_complete + "/" + achievement.getRequirement());
 			progress.setProgress(items_complete);
 		} else if (achievement.getType().equals("ne")) {
 			desc = "Complete " + Integer.toString(achievement.getRequirement()) + " exhibits";
 			completion.setText(exhibits_complete + "/" + achievement.getRequirement());
 			progress.setProgress(exhibits_complete);
 		} else {
 			desc = "Complete the " + achievement.getExhibit() + " exhibit";
 			//remove the progress bar and completion
 			progress.setVisibility(View.GONE);
 			completion.setVisibility(View.GONE);
			//adjust margins
			 LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			 lp.setMargins(0, -3, 0, -2);
			 descview.setLayoutParams(lp);
 		}
 		//drop "exhibit" from the end if too long
 		if (desc.length() > 50) {
 			desc = desc.split(" exhibit")[0];
 		}
 		descview.setText(desc);
 		
 		return rowView;
 	}
 	
 	@Override
 	public boolean isEnabled(int position) {
 	    return false;
 	}
 }
