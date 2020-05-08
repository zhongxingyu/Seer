 package com.slobodastudio.discussions.ui.activities;
 
 import com.slobodastudio.discussions.R;
 import com.slobodastudio.discussions.data.PreferenceHelper;
 import com.slobodastudio.discussions.data.model.ArgPointChanged;
 import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
 import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
 import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
 import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
 import com.slobodastudio.discussions.photon.DiscussionUser;
 import com.slobodastudio.discussions.photon.PhotonServiceCallback;
 import com.slobodastudio.discussions.ui.ExtraKey;
 import com.slobodastudio.discussions.ui.IntentAction;
 import com.slobodastudio.discussions.ui.PointsListPagerAdaptor;
 import com.slobodastudio.discussions.ui.fragments.AllOtherUserPointListFragment;
 import com.slobodastudio.discussions.ui.fragments.OtherUserPointListFragment;
 import com.slobodastudio.discussions.ui.fragments.UserPointListFragment;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.PagerTitleStrip;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import java.util.List;
 import java.util.Vector;
 
 public class PointsActivity extends BaseActivity implements PhotonServiceCallback {
 
 	private static final String TAG = PointsActivity.class.getSimpleName();
 	private int mDiscussionId;
 	private PagerAdapter mPagerAdapter;
 	private int mPersonId;
 	private String mPersonName;
 	private int mTopicId;
 	private ViewPager pager;
 
 	@Override
 	public void onArgPointChanged(final ArgPointChanged argPointChanged) {
 
 		if (DEBUG) {
 			Log.d(TAG, "[onArgPointChanged] Empty point id: " + argPointChanged.getPointId());
 		}
 	}
 
 	@Override
 	public void onConnect() {
 
 		if (DEBUG) {
 			Log.d(TAG, "[onConnect] Empty. ");
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {
 
 		MenuInflater menuInflater = getSupportMenuInflater();
 		menuInflater.inflate(R.menu.actionbar_points, menu);
 		// Calling super after populating the menu is necessary here to ensure that the
 		// action bar helpers have a chance to handle this event.
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public void onErrorOccured(final String message) {
 
 		Log.e(TAG, "[onErrorOccured] Empty. message: " + message);
 	}
 
 	@Override
 	public void onEventJoin(final DiscussionUser newUser) {
 
 		if (DEBUG) {
 			Log.d(TAG, "[onEventJoin] Empty. user come: " + newUser.getUserName());
 		}
 	}
 
 	@Override
 	public void onEventLeave(final DiscussionUser leftUser) {
 
 		if (DEBUG) {
 			Log.d(TAG, "[onEventLeave] Empty. user left: " + leftUser.getUserName());
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item) {
 
 		if (DEBUG) {
 			Log.d(TAG, "[onOptionsItemSelected] item id: " + item.getItemId());
 		}
 		switch (item.getItemId()) {
 			case R.id.menu_new:
 				Intent intent = createNewPointIntent();
 				startActivity(intent);
 				return true;
 			case R.id.menu_refresh:
 				onRefreshCurrentTopic();
 				return true;
 			case R.id.menu_discussion_info:
 				startDiscussionInfoActivity();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onRefreshCurrentTopic() {
 
 		if (DEBUG) {
 			Log.d(TAG, "[onRefreshCurrentTopic] topic id: " + mTopicId);
 		}
 		mServiceHelper.downloadPointsFromTopic(mTopicId);
 	}
 
 	@Override
 	public void onStructureChanged(final int changedTopicId) {
 
 		if (DEBUG) {
 			Log.d(TAG, "[onStructureChanged] Empty. topic id: " + changedTopicId);
 		}
 	}
 
 	@Override
 	protected void onControlServiceConnected() {
 
 		connectPhoton();
 	}
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		initFromIntentExtra();
		setContentView(R.layout.activity_new_points);
 		pager = (ViewPager) super.findViewById(R.id.viewpager);
 		PagerTitleStrip pagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pagerTitleStrip);
 		getSupportLoaderManager().initLoader(PersonsCursorLoader.LOADER_TOPIC_PERSONS, null,
 				new PersonsCursorLoader());
 	}
 
 	private void connectPhoton() {
 
 		if (mBound && !mService.getPhotonController().isConnected()) {
 			mService.getPhotonController().connect(this, mDiscussionId,
 					PreferenceHelper.getPhotonDbAddress(this), mPersonName, mPersonId);
 			mService.getPhotonController().getCallbackHandler().addCallbackListener(PointsActivity.this);
 		}
 	}
 
 	private Intent createNewPointIntent() {
 
 		Intent intent = new Intent(IntentAction.NEW, Points.CONTENT_URI);
 		intent.putExtra(ExtraKey.PERSON_ID, mPersonId);
 		intent.putExtra(ExtraKey.TOPIC_ID, mTopicId);
 		intent.putExtra(ExtraKey.DISCUSSION_ID, mDiscussionId);
 		intent.putExtra(ExtraKey.POINT_ID, Integer.MIN_VALUE);
 		return intent;
 	}
 
 	private void initFromIntentExtra() {
 
 		if (!getIntent().hasExtra(ExtraKey.PERSON_ID)) {
 			throw new IllegalStateException("Activity intent was without person id");
 		}
 		if (!getIntent().hasExtra(ExtraKey.TOPIC_ID)) {
 			throw new IllegalStateException("Activity intent was without topic id");
 		}
 		if (!getIntent().hasExtra(ExtraKey.DISCUSSION_ID)) {
 			throw new IllegalStateException("Activity intent was without discussion id");
 		}
 		if (!getIntent().hasExtra(ExtraKey.PERSON_NAME)) {
 			throw new IllegalStateException("Activity intent was without person name");
 		}
 		mPersonName = getIntent().getExtras().getString(ExtraKey.PERSON_NAME);
 		mPersonId = getIntent().getExtras().getInt(ExtraKey.PERSON_ID);
 		mTopicId = getIntent().getExtras().getInt(ExtraKey.TOPIC_ID);
 		if (mTopicId == -1) {
 			throw new IllegalStateException("Activity intent has illegal topic id -1");
 		}
 		mDiscussionId = getIntent().getExtras().getInt(ExtraKey.DISCUSSION_ID);
 		if (DEBUG) {
 			Log.d(TAG, "[initFromIntentExtras] personId: " + mPersonId + ", topicId: " + mTopicId
 					+ ", discussionId: " + mDiscussionId + ", personName: " + mPersonName);
 		}
 	}
 
 	private void startDiscussionInfoActivity() {
 
 		int discussionId = getIntent().getExtras().getInt(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
 		Uri discussionUri = Discussions.buildTableUri(discussionId);
 		Intent discussionInfoIntent = new Intent(Intent.ACTION_VIEW, discussionUri, this,
 				DiscussionInfoActivity.class);
 		startActivity(discussionInfoIntent);
 	}
 
 	private class PersonsCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {
 
 		private static final int LOADER_TOPIC_PERSONS = 1;
 
 		@Override
 		public Loader<Cursor> onCreateLoader(final int id, final Bundle arguments) {
 
 			switch (id) {
 				case LOADER_TOPIC_PERSONS:
 					return new CursorLoader(PointsActivity.this, Topics.buildPersonsUri(mTopicId), null,
 							null, null, null);
 				default:
 					throw new IllegalArgumentException("Unknown loader id: " + id);
 			}
 		}
 
 		@Override
 		public void onLoaderReset(final Loader<Cursor> loader) {
 
 			switch (loader.getId()) {
 				case LOADER_TOPIC_PERSONS:
 					// pager.setAdapter(null);
 					break;
 				default:
 					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
 			}
 		}
 
 		@Override
 		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
 
 			switch (loader.getId()) {
 				case LOADER_TOPIC_PERSONS:
 					initializePaging(data);
 					pager.setCurrentItem(1, true);
 					break;
 				default:
 					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
 			}
 		}
 
 		private void initializePaging(final Cursor cursor) {
 
 			List<Fragment> fragments = new Vector<Fragment>();
 			Bundle otherUsersArguments = new Bundle(3);
 			otherUsersArguments.putInt(ExtraKey.PERSON_ID, mPersonId);
 			otherUsersArguments.putInt(ExtraKey.ORIGIN_PERSON_ID, mPersonId);
 			otherUsersArguments.putString(ExtraKey.LIST_HEADER, getString(R.string.text_other_users_points));
 			fragments.add(Fragment.instantiate(PointsActivity.this, AllOtherUserPointListFragment.class
 					.getName(), otherUsersArguments));
 			Bundle currentUserPointsArguments = new Bundle(1);
 			currentUserPointsArguments.putString(ExtraKey.LIST_HEADER,
 					getString(R.string.text_current_user_points));
 			fragments.add(Fragment.instantiate(PointsActivity.this, UserPointListFragment.class.getName(),
 					currentUserPointsArguments));
 			int personIdIndex = cursor.getColumnIndexOrThrow(Persons.Columns.ID);
 			int personNameIndex = cursor.getColumnIndexOrThrow(Persons.Columns.NAME);
 			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
 				int personId = cursor.getInt(personIdIndex);
 				if (personId != mPersonId) {
 					Bundle arguments = new Bundle(1);
 					arguments.putInt(ExtraKey.PERSON_ID, personId);
 					arguments.putInt(ExtraKey.ORIGIN_PERSON_ID, mPersonId);
 					String personName = cursor.getString(personNameIndex);
 					String listHeader = getString(R.string.text_other_users_points_format, personName);
 					arguments.putString(ExtraKey.LIST_HEADER, listHeader);
 					fragments.add(Fragment.instantiate(PointsActivity.this, OtherUserPointListFragment.class
 							.getName(), arguments));
 				}
 			}
 			mPagerAdapter = new PointsListPagerAdaptor(getSupportFragmentManager(), fragments);
 			pager.setAdapter(mPagerAdapter);
 		}
 	}
 }
