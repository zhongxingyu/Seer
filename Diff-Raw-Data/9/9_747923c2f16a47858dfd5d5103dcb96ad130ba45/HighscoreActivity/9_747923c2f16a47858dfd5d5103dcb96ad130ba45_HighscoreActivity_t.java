 package org.interdictor;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.interdictor.util.Database;
 import org.interdictor.util.Log;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class HighscoreActivity extends ListActivity {
 
 	private List<Score> scores;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Log.print("Highscores");
 
 		Database db = Database.getInstance(this);
 		TextTwistApplication tta = (TextTwistApplication) getApplication();
 		scores = db.getHighScores(tta.getLocale());
 		Log.print("Scores for adapter: " + scores.size());
 		setListAdapter(new ScoreAdapter(this, scores));
 		setTitle(getResources().getString(R.string.highscores_for_lang, tta.getLocale().getDisplayLanguage()));
 	}
 
 	class ScoreAdapter extends ArrayAdapter<Score> {
 		private final Context context;
 		private List<Score> scores;
 		private SimpleDateFormat shortDate;
 
 		public ScoreAdapter(Context context, List<Score> scores) {
 			super(context, R.layout.highscore_item);
 			this.context = context;
 			this.scores = scores;
 			shortDate = new SimpleDateFormat(context.getResources().getString(R.string.i18n_date_short));
 			Log.print("HS con: " + scores.size());
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View rowView = inflater.inflate(R.layout.highscore_item, parent, false);
 
 			TextView place = (TextView) rowView.findViewById(R.id.position);
 			place.setText((position+1) + ""); // scores are not sensibly 0 based
 
 			Score score = scores.get(position);
			
 			TextView textView = (TextView) rowView.findViewById(R.id.scoretext);
			textView.setText(context.getResources().getString(R.string.score_value, score.score));

			String date = shortDate.format(new Date((long)score.timestamp * 1000L)); // timestamps are epoch seconds, Dates are millis, and timestamps*1000 are longs
			TextView dateText = (TextView) rowView.findViewById(R.id.scoredate);
			dateText.setText(date);
 
 			return rowView;
 		}
 		
 		@Override
 		public int getCount() {
 			return scores.size();
 		}
 	}
 }
