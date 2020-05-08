 package edu.selu.android.classygames;
 
 
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.MenuItem;
 
 import edu.selu.android.classygames.data.Person;
 import edu.selu.android.classygames.utilities.ServerUtilities;
 import edu.selu.android.classygames.utilities.Utilities;
 
 
 public class GameOverActivity extends SherlockActivity
 {
 
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.game_over_activity);
 		Utilities.styleActionBar(getResources(), getSupportActionBar());
 
 		final Bundle bundle = getIntent().getExtras();
 
 		if (bundle == null || bundle.isEmpty())
 		{
 			activityHasError();
 		}
 		else
 		{
 			final byte winOrLose = bundle.getByte(ServerUtilities.POST_DATA_TYPE);
 			final long challengedId = bundle.getLong(CheckersGameActivity.INTENT_DATA_PERSON_CHALLENGED_ID);
 			final String challengedName = bundle.getString(CheckersGameActivity.INTENT_DATA_PERSON_CHALLENGED_NAME);
 
 			if (!ServerUtilities.validWinOrLoseValue(winOrLose) || challengedId < 0 || challengedName == null || challengedName.isEmpty())
 			{
 				activityHasError();
 			}
 			else
 			{
 				final Person personChallenged = new Person(challengedId, challengedName);
 
 				ImageView personPicture = (ImageView) findViewById(R.id.game_over_activity_person_picture);
 				personPicture.setImageResource(R.drawable.fb_placeholder);
 				new AsyncPopulatePicture(personPicture).execute(personChallenged);
 
 				TextView personName = (TextView) findViewById(R.id.game_over_activity_person_name);
 				personName.setText(personChallenged.getName());
 				personName.setTypeface(Utilities.getTypeface(getAssets(), Utilities.TYPEFACE_SNELL_ROUNDHAND_BLKSCR));
 
 				TextView text = (TextView) findViewById(R.id.game_over_activity_text);
 
 				switch (winOrLose)
 				{
 					case ServerUtilities.POST_DATA_TYPE_GAME_OVER_LOSE:
 						text.setText(GameOverActivity.this.getString(R.string.game_over_activity_description, GameOverActivity.this.getString(R.string.lost)));
 						break;
 
 					case ServerUtilities.POST_DATA_TYPE_GAME_OVER_WIN:
 						text.setText(GameOverActivity.this.getString(R.string.game_over_activity_description, GameOverActivity.this.getString(R.string.won)));
 						break;
 				}
 
 				Button buttonReturn = (Button) findViewById(R.id.game_over_activity_button_return);
 				buttonReturn.setTypeface(Utilities.getTypeface(getAssets(), Utilities.TYPEFACE_BLUE_HIGHWAY_D));
 				buttonReturn.setOnClickListener(new OnClickListener()
 				{
 					@Override
 					public void onClick(final View v)
 					{
 						finish();
 					}
 				});
 			}
 		}
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case android.R.id.home:
 				finish();
 				return true;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 	private void activityHasError()
 	{
 		finish();
 	}
 
 
 	private final class AsyncPopulatePicture extends AsyncTask<Person, Void, Drawable>
 	{
 
 
 		private ImageView imageView;
 
 
 		AsyncPopulatePicture(final ImageView imageView)
 		{
 			super();
 			this.imageView = imageView;
 		}
 
 
 		@Override
 		protected Drawable doInBackground(final Person... person) 
 		{
 			return Utilities.loadImageFromWebOperations(Utilities.FACEBOOK_GRAPH_API_URL + person[0].getId() + Utilities.FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_LARGE_SSL);
 		}
 
 
 		@Override
 		protected void onPostExecute(final Drawable result)
 		{
 			imageView.setImageDrawable(result);
 		}
 
 
 	}
 
 
 }
