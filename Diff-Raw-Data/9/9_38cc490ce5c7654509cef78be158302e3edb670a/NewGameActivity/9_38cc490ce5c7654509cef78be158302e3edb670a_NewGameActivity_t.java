 package edu.selu.android.classygames;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.ActivityManager;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.util.LruCache;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.facebook.android.Util;
 import com.koushikdutta.urlimageviewhelper.DiskLruCache;
 
 import edu.selu.android.classygames.data.Person;
 
 
 public class NewGameActivity extends SherlockListActivity
 {
 	private DiskLruCache diskCache;
 	private LruCache<Long, Bitmap> memoryCache;
 	private PeopleAdapter peopleAdapter;
 	private Person personCreator;
 	
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.new_game_activity);
 		Utilities.styleActionBar(getResources(), getSupportActionBar());
 
 		final Bundle bundle = getIntent().getExtras();
 
 		if (bundle == null)
 		// bundle should NOT equal null
 		{
 			activityHasError();
 		}
 		else
 		{
 			final long id = bundle.getLong(CheckersGameActivity.INTENT_DATA_PERSON_CREATOR_ID);
 			final String name = bundle.getString(CheckersGameActivity.INTENT_DATA_PERSON_CREATOR_NAME);
 
 			if (id < 0 || name == null || name.equals(""))
 			{
 				activityHasError();
 			}
 			else
 			{
 				personCreator = new Person(id, name);
 			}
 		}
 
 		// setup cache size for loading drawable images
 		final int memClass = ((ActivityManager) (NewGameActivity.this).getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
 		
 		// currently use 1/8 of available memory for the cache
 		final int cacheSize = 1024 * 1024 * memClass / 8;
 		
 		memoryCache = new LruCache<Long, Bitmap> (cacheSize)
 		{
 			protected int sizeOf(Long key, Bitmap bitmap)
 			{
				if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= 12)
				{
		            return bitmap.getByteCount();
				}
		        else
		        {
		            return (bitmap.getRowBytes() * bitmap.getHeight());
		        }
 			}
 		};
 		
 		//Try loading diskCache
 		
 		try
 		{
 			File cacheDir = getCacheDir(this, Utilities.DISK_CACHE_SUBDIR);
 			diskCache = DiskLruCache.open(cacheDir, Utilities.APP_VERSION, Utilities.VALUE_COUNT, Utilities.DISK_CACHE_SIZE);
 		} 
 		catch (IOException e) 
 		{
 			Log.e(Utilities.LOG_TAG, "DiskCache instantiate failed: " + e);
 		}
 		
 		new AsyncPopulateFacebookFriends().execute();
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
 		Utilities.easyToastAndLogError(NewGameActivity.this, NewGameActivity.this.getString(R.string.new_game_activity_data_error));
 		finish();
 	}
 
 	
 	private final class AsyncPopulateFacebookFriends extends AsyncTask<Void, Long, ArrayList<Person>>
 	{
 
 
 		private ProgressDialog progressDialog;
 
 
 		@Override
 		protected ArrayList<Person> doInBackground(final Void... v)
 		{
 			ArrayList<Person> people = new ArrayList<Person>();
 
 			try
 			{
 				final String request = Utilities.getFacebook().request("me/friends");
 				final JSONObject response = Util.parseJson(request);
 				final JSONArray friends = response.getJSONArray("data");
 				final int friendsLength = friends.length();
 				publishProgress((long) friendsLength);
 
 				for (int i = 0; i < friendsLength; ++i)
 				{
 					final JSONObject friend = friends.getJSONObject(i);
 					final long id = friend.getLong("id");
 					people.add(new Person(id, friend.getString("name")));
 
 					publishProgress((long) i, id);
 				}
 
 				people.trimToSize();
 				Collections.sort(people, new FacebookFriendsSorter());
 			}
 			catch (final Exception e)
 			{
 				Log.e(Utilities.LOG_TAG, e.getMessage());
 			}
 
 			return people;
 		}
 
 
 		@Override
 		protected void onPostExecute(final ArrayList<Person> people)
 		{
 			peopleAdapter = new PeopleAdapter(NewGameActivity.this, R.layout.new_game_activity_listview_item, people);
 			setListAdapter(peopleAdapter);
 			peopleAdapter.notifyDataSetChanged();
 
 			if (progressDialog.isShowing())
 			{
 				progressDialog.dismiss();
 			}
 		}
 
 
 		@Override
 		protected void onPreExecute()
 		{
 			progressDialog = new ProgressDialog(NewGameActivity.this);
 			progressDialog.setMessage(NewGameActivity.this.getString(R.string.new_game_activity_progressdialog_message));
 			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			progressDialog.setTitle(R.string.new_game_activity_progressdialog_title);
 			progressDialog.setCancelable(false);
 			progressDialog.setCanceledOnTouchOutside(false);
 			
 			progressDialog.show();
 		}
 
 
 		@Override
 		protected void onProgressUpdate(final Long... l)
 		{
 			switch (l.length)
 			{
 				case 1:
 					progressDialog.setMax(l[0].intValue());
 					break;
 
 				case 2:
 					progressDialog.setProgress(l[0].intValue());
 					break;
 			}
 		}
 		
 		
 	}
 
 
 	private class PeopleAdapter extends ArrayAdapter<Person>
 	{
 		private ArrayList<Person> people;
 
 		public PeopleAdapter(final Context context, final int textViewResourceId, final ArrayList<Person> people)
 		{
 			super(context, textViewResourceId, people);
 			this.people = people;
 		}
 
 
 		@Override
 		public View getView(final int position, View convertView, final ViewGroup parent)
 		{
 			if (convertView == null)
 			{
 				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = layoutInflater.inflate(R.layout.new_game_activity_listview_item, null);
 			}
 
 			final Person person = people.get(position);
 
 			if (person != null)
 			{
 				ViewHolder viewHolder = new ViewHolder();
 				viewHolder.picture = (ImageView) convertView.findViewById(R.id.new_game_activity_listview_item_picture);
 				viewHolder.picture.setImageResource(R.drawable.fb_placeholder);
 				{
 					Bitmap diskImage = Utilities.getBitmapFromDiskCache(person.getId(), diskCache);
 					Bitmap memoryImage = memoryCache.get(person.getId());
 					viewHolder.picture.setTag(person.getId());
 					
 					if(memoryImage != null)
 					{
 						viewHolder.picture.setImageBitmap(memoryImage);
 					}
 					else if(diskImage != null)
 					{
 						viewHolder.picture.setImageBitmap(diskImage);
 					}
 					else
 					{
 						new AsyncPopulatePictures(viewHolder).execute(person);
 					}
 				}
 
 				viewHolder.name = (TextView) convertView.findViewById(R.id.new_game_activity_listview_item_name);
 				if (viewHolder.name != null)
 				{
 					viewHolder.name.setText(person.getName());
 					viewHolder.name.setTypeface(Utilities.getTypeface(getAssets(), Utilities.TYPEFACE_BLUE_HIGHWAY_D));
 				}
 
 				viewHolder.onClickListener = new OnClickListener()
 				{
 					@Override
 					public void onClick(final View v)
 					{
 						Intent intent = new Intent(NewGameActivity.this, ConfirmGameActivity.class);
 						intent.putExtra(CheckersGameActivity.INTENT_DATA_PERSON_CREATOR_ID, personCreator.getId());
 						intent.putExtra(CheckersGameActivity.INTENT_DATA_PERSON_CREATOR_NAME, personCreator.getName());
 						intent.putExtra(CheckersGameActivity.INTENT_DATA_PERSON_CHALLENGED_ID, person.getId());
 						intent.putExtra(CheckersGameActivity.INTENT_DATA_PERSON_CHALLENGED_NAME, person.getName());
 
 						// start the ConfirmGameActivity with a bit of extra data. We're passing it both
 						// the id and the name of the facebook person that the user clicked on
 						startActivity(intent);
 					}
 				};
 
 				convertView.setOnClickListener(viewHolder.onClickListener);
 				convertView.setTag(viewHolder);
 			}
 
 			return convertView;
 		}
 
 
 		private final class AsyncPopulatePictures extends AsyncTask<Person, Long, Drawable>
 		{
 
 
 			private Drawable drawable;
 			private Bitmap bitmap;
 			private ViewHolder viewHolder;
 			private String path;
 
 
 			AsyncPopulatePictures(final ViewHolder viewHolder)
 			{
 				super();
 				this.viewHolder = viewHolder;
 				path = this.viewHolder.picture.getTag().toString();
 			}
 
 
 			@Override
 			protected Drawable doInBackground(final Person... person) 
 			{
 				if (!viewHolder.picture.getTag().toString().equals(path))
 				{
 					this.cancel(true);
 					return null;
 				}
 				else
 				{
 					try
 					{
 							drawable = Utilities.loadImageFromWebOperations(Utilities.FACEBOOK_GRAPH_API_URL + person[0].getId() + Utilities.FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_SQUARE_SSL);
 							bitmap = ((BitmapDrawable)drawable).getBitmap();
 							Utilities.addBitmapToCache(person[0].getId(),bitmap, memoryCache, diskCache);
 					}
 					catch (final Exception e)
 					{
 						Log.e("Classy Games", "Image Load Failed: " + e);
 					}
 
 					return drawable;
 				}
 			}
 
 
 			@Override
 			protected void onPostExecute(final Drawable result)
 			{
 				viewHolder.picture.setImageDrawable(result);
 			}
 
 
 		}
 
 
 		/**
 		 * made this li'l class while trying to optimize our listview. apparently using
 		 * something like this helps performance
 		 * https://developer.android.com/training/improving-layouts/smooth-scrolling.html
 		 *
 		 */
 		private class ViewHolder
 		{
 			public ImageView picture;
 			public OnClickListener onClickListener;
 			public TextView name;
 		}
 
 	}
 
 
 	private class FacebookFriendsSorter implements Comparator<Person>
 	{
 		@Override
 		public int compare(final Person geo, final Person jarrad)
 		{
 			return geo.getName().compareToIgnoreCase(jarrad.getName());
 		}
 	}
 	
 	
 	public static File getCacheDir(Context context, String uniqueName) 
 	{
 		//Check if storage is built in or mounted from sd, try to use mounted first
 	    final String cachePath = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED || !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();
 
 	    return new File(cachePath + File.separator + uniqueName);
 	}
 	
 	
 }
