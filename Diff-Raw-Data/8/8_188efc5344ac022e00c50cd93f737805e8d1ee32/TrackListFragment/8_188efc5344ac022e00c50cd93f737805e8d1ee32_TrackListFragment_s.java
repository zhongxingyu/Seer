 package org.muckebox.android.ui.fragment;
 
 import java.util.concurrent.TimeUnit;
 
 import org.muckebox.android.R;
 import org.muckebox.android.db.MuckeboxContract.TrackEntry;
 import org.muckebox.android.db.Provider;
 import org.muckebox.android.net.RefreshTracksTask;
 import org.muckebox.android.net.RefreshTask;
 import org.muckebox.android.ui.widgets.ImageViewRotater;
 
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.animation.IntEvaluator;
 import android.animation.ValueAnimator;
 import android.annotation.SuppressLint;
 import android.app.ListFragment;
 import android.app.LoaderManager;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Loader;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.MeasureSpec;
 import android.view.animation.AccelerateDecelerateInterpolator;
 import android.widget.ListView;
import android.widget.RelativeLayout;
 import android.widget.SimpleCursorAdapter;
 import android.widget.SearchView.OnCloseListener;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 import android.widget.TextView;
 
 public class TrackListFragment extends ListFragment
 	implements OnCloseListener,
 	LoaderManager.LoaderCallbacks<Cursor>,
 	RefreshTask.Callbacks {
 	private static final String LOG_TAG = "TrackListFragment";
 	
 	private static final String ALBUM_ID_ARG = "album_id";
 	
 	SimpleCursorAdapter mAdapter;
 	MenuItem mRefreshItem;
 	boolean mListLoaded = false;
 	
 	private class ListItemState {
 		public int index = 0;
 		public boolean extended = false;
 		public int texts_height = 0;
 		public int total_height = 0;
 		public ListView list = null;
 		
 		public ListItemState(int newIndex)
 		{
 			index = newIndex;
 		}
 	}
 	
 	private class HeightEvaluator extends IntEvaluator {
 		private View mView;
 		
 		public HeightEvaluator(View view) {
 			mView = view;
 		}
 		
 		@Override
 		public Integer evaluate(float fraction, Integer startValue, Integer endValue)
 		{
 			int num = super.evaluate(fraction, startValue, endValue);
 			
 			ViewGroup.LayoutParams p = mView.getLayoutParams();
 			p.height = num;
 			mView.setLayoutParams(p);
 			
 			return num;
 		}
 	}
 	
 	private class ContextCursorAdapter extends SimpleCursorAdapter {
 
 		public ContextCursorAdapter(Context context, int layout, Cursor c,
 				String[] from, int[] to, int flags) {
 			super(context, layout, c, from, to, flags);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent)
 		{
 			View ret = super.getView(position, convertView, parent);
 			
 			if (ret.getTag() == null)
 			{
 				ListItemState state = new ListItemState(position);
 				int spec = MeasureSpec.makeMeasureSpec(4096, MeasureSpec.AT_MOST);			
				RelativeLayout texts =
						(RelativeLayout) ret.findViewById(R.id.track_list_texts);
 				ViewGroup.LayoutParams params = ret.getLayoutParams();
 				
 				ret.measure(spec, spec);
 				texts.measure(spec, spec);
 	
 				state.texts_height = texts.getMeasuredHeight();
 				state.total_height = ret.getMeasuredHeight();
 				state.list = (ListView) parent;
 	
 				params.height = state.texts_height;
 				
 				ret.setLayoutParams(params);
 				ret.setTag(state);
 			}
 			
 			return ret;
 		}
 	}
 	
 	private class DurationViewBinder implements ViewBinder {
         @SuppressLint("DefaultLocale")
 		public boolean setViewValue(View view, Cursor cursor, int columnIndex)
         {
 			if (columnIndex == cursor.getColumnIndex(TrackEntry.ALIAS_LENGTH))
 			{
 				TextView textview = (TextView) view;
 				String text;
 
 				long duration = cursor.getInt(columnIndex);
 				long hours = TimeUnit.SECONDS.toHours(duration);
 				long minutes = TimeUnit.SECONDS.toMinutes(duration - hours * 60);
 				long seconds = duration - ((hours * 60) + minutes) * 60;
 				
 				if (hours > 0)
 					text = String.format("%d:%02d:%02d", hours, minutes, seconds);
 				else
 					text = String.format("%d:%02d", minutes, seconds);
 				
 				textview.setText(text);
 				
 				return true;
     		}
 			
 			return false;
     	}
 	}
 	
 	public static TrackListFragment newInstanceFromAlbum(long artist_id) {
 		TrackListFragment f = new TrackListFragment();
 		Bundle args = new Bundle();
 		
 		args.putLong(ALBUM_ID_ARG, artist_id);
 		f.setArguments(args);
 		
 		return f;
 	}
 	
 	public long getAlbumId() {
 		Bundle args = getArguments();
 		
 		if (args == null)
 			return -1;
 		
 		return args.getLong(ALBUM_ID_ARG, -1);
 	}
 	
 	public boolean hasAlbumId() {
 		return getAlbumId() != -1;
 	}
 	
 	@Override public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         
         setEmptyText("No Tracks");
 
         // We have a menu item to show in action bar.
         setHasOptionsMenu(true);
 
         // Create an empty adapter we will use to display the loaded data.
         mAdapter = new ContextCursorAdapter(getActivity(),
                 R.layout.list_row_track, null,
                 new String[] {
         			TrackEntry.ALIAS_TITLE,
         			TrackEntry.ALIAS_DISPLAY_ARTIST,
         			TrackEntry.ALIAS_LENGTH
         			},
                 new int[] {
         			R.id.track_list_title,
         			R.id.track_list_artist,
         			R.id.track_list_duration
         			}, 0);
         
         mAdapter.setViewBinder(new DurationViewBinder());
  
         setListAdapter(mAdapter);
         setListShown(false);
         getLoaderManager().initLoader(0, null, this);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         mRefreshItem = menu.add("Refresh");
         mRefreshItem.setIcon(R.drawable.ic_menu_refresh);
         mRefreshItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         
         if (! mListLoaded) {
         	new RefreshTracksTask().setCallbacks(this).execute(getAlbumId());
         	mListLoaded = true;
         }
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	if (item == mRefreshItem)
     	{
     		new RefreshTracksTask().setCallbacks(this).execute(getAlbumId());
     		return true;
     	}
     	
     	return false;
     }
 
     @Override
     public boolean onClose() {
         return true;
     }
     
     private void hideItem(View item)
     {
     	ListItemState state = (ListItemState) item.getTag();
     	
     	if (state.extended)
     	{
 			ValueAnimator anim = ValueAnimator.ofObject(new HeightEvaluator(item),
 					state.total_height, state.texts_height);
 			
 			Log.d(LOG_TAG, "animate " + state.total_height + " -> " + state.texts_height);
 			
 			anim.setInterpolator(new AccelerateDecelerateInterpolator());
 			anim.start();
 			
 			state.extended = false;
 			item.setTag(state);   	
     	}
     }
     
     private void showItem(View item)
     {
     	ListItemState state = (ListItemState) item.getTag();
 
     	for (int i = 0; i < state.list.getCount(); ++i)
     	{
     		View child = state.list.getChildAt(i);
     		
     		if (child != null)
     		{
 	    		if (i != state.index)
 	    		{
 	    			hideItem(child);
 	    		} else
 	    		{
 
 	    	    	if (! state.extended)
 	    	    	{
 		    	    	ValueAnimator anim = ValueAnimator.ofObject(new HeightEvaluator(item),
 		        				state.texts_height, state.total_height);
 		
 		        		Log.d(LOG_TAG, "animate " + state.texts_height + " -> " + state.total_height);
 		
 		        		anim.setInterpolator(new AccelerateDecelerateInterpolator());
 		        		anim.start();
 		        		
 		        		state.extended = true;
 		        		
 		        		item.setTag(state);  	
 	    	    	}
 	    		}
     		}
     	}
     }
     
     public void toggleButtons(View item)
     {
     	View parent = (View) item.getParent();
     	ListItemState state = (ListItemState) parent.getTag();
     	
     	if (state.extended)
     	{
     		hideItem(parent);
     	} else
     	{
     		showItem(parent);
     	}
     }
 
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         // This is called when a new Loader needs to be created.  This
         // sample only has one Loader, so we don't care about the ID.
         // First, pick the base URI to use depending on whether we are
         // currently filtering.
         Uri baseUri;
         if (hasAlbumId()) {
         	baseUri = Uri.parse(Provider.TRACK_ALBUM_BASE + Long.toString(getAlbumId()));
         } else {
             baseUri = Provider.URI_TRACKS;
         }
 
         return new CursorLoader(getActivity(), baseUri,
                 TrackEntry.PROJECTION, null, null,
                 TrackEntry.SORT_ORDER);
     }
 
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
         // Swap the new cursor in.  (The framework will take care of closing the
         // old cursor once we return.)
         mAdapter.swapCursor(data);
 
         // The list should now be shown.
         if (isResumed()) {
             setListShown(true);
         } else {
             setListShownNoAnimation(true);
         }
     }
 
     public void onLoaderReset(Loader<Cursor> loader) {
         // This is called when the last Cursor provided to onLoadFinished()
         // above is about to be closed.  We need to make sure we are no
         // longer using it.
         mAdapter.swapCursor(null);
     }
     
 	@Override
 	public void onRefreshStarted() {
 		mRefreshItem.setActionView(
 				ImageViewRotater.getRotatingImageView(
 						getActivity(), R.layout.action_view_refresh));
 	}
 
 	@Override
 	public void onRefreshFinished(boolean success) {
 		mRefreshItem.getActionView().clearAnimation();
 		mRefreshItem.setActionView(null);
 	}
 }
