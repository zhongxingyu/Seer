 package com.ximoneighteen.android.rssreader;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.EditText;
 import android.widget.ListView;
 
 import com.ximoneighteen.android.rssreader.model.DbHelper;
 import com.ximoneighteen.android.rssreader.model.Feed;
 import com.ximoneighteen.android.rssreader.model.Identifiable;
 import com.ximoneighteen.android.rssreader.util.IdentifiableListAdapter;
 import com.ximoneighteen.android.rssreader.util.UpdateFeedTask;
 
 public class FeedListActivity extends Activity {
 	private static DbHelper db;
 
 	private IdentifiableListAdapter adapter;
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_feed_list);
 
 		db = DbHelper.getInstance(this);
 		final List<? extends Identifiable> list = db.getFeeds(); 
 
 		final Context context = this;
 
 		adapter = new IdentifiableListAdapter(this, android.R.layout.simple_list_item_1, (List<Identifiable>) list);
 
 		final ListView listview = (ListView) findViewById(R.id.feedListView);
 		listview.setAdapter(adapter);
 		listview.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 				final Feed feed = (Feed) parent.getItemAtPosition(position);
				confirmAction("Are you sure you want to delete the '" + feed.getTitle() + "' feed?", new ConfirmableAction() {
					@Override
					public void run() {
						db.removeFeed(feed);
						adapter.remove(feed);
						adapter.notifyDataSetChanged();
					}
				});
 				return true;
 			}
 		});
 		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
 				final Feed feed = (Feed) parent.getItemAtPosition(position);
 				final Intent intent = new Intent(context, ArticleListActivity.class);
 				intent.putExtra("feed", feed);
 				startActivity(intent);
 			}
 
 		});
 	}
 
	private interface ConfirmableAction {
		public void run();
	}

	private void confirmAction(final String title, final ConfirmableAction action) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				action.run();
			}
		});
		builder.setNegativeButton("No", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.show();
	}

 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu items for use in the action bar
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle presses on the action bar items
 		switch (item.getItemId()) {
 		case R.id.action_add_feed:
 			getFeedURL();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void getFeedURL() {
 		class AddFeedDialogFragment extends DialogFragment {
 			@Override
 			public Dialog onCreateDialog(Bundle savedInstanceState) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 				LayoutInflater inflater = getActivity().getLayoutInflater();
 				final View view = inflater.inflate(R.layout.dialog_add_feed, null);
 				builder.setView(view);
 				builder.setMessage(R.string.add_feed_dialog_msg);
 				builder.setPositiveButton(R.string.ok, new OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						String title = ((EditText) view.findViewById(R.id.title)).getText().toString();
 						String uri = ((EditText) view.findViewById(R.id.uri)).getText().toString();
 						addFeed(title, uri);
 					}
 				});
 				return builder.create();
 			}
 		}
 
 		DialogFragment dialog = new AddFeedDialogFragment();
 		dialog.show(getFragmentManager(), "addFeedDialog");
 	}
 
 	protected void addFeed(String title, String urlString) {
 		try {
 			URL url = new URL(urlString);
 			Feed feed = new Feed();
 			feed.setTitle(title);
 			feed.setUrl(url);
 
 			db.putFeed(feed);
 
 			adapter.add(feed);
 		
 			// TODO: Replace the thread pool with one dedicated to limiting how many feeds are updated at once
 			new UpdateFeedTask(db).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, feed);
 		} catch (MalformedURLException e) {
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 			alert.setTitle("I'm sorry");
 			alert.setMessage("Your new feed cannot be added because the string you provided is not a valid URL");
 			alert.setCancelable(false);
 			alert.setNeutralButton("Dismiss", new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// Nothing to do
 				}
 			});
 			alert.show();
 		}
 	}
 
 }
