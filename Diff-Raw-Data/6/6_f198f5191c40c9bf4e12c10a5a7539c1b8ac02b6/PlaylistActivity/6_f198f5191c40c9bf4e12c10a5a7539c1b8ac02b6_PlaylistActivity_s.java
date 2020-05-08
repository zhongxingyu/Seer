 package cc.rainwave.android;
 
 import java.io.IOException;
 import java.util.Comparator;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import cc.rainwave.android.api.Session;
 import cc.rainwave.android.api.types.Album;
 import cc.rainwave.android.api.types.Artist;
 import cc.rainwave.android.api.types.RainwaveException;
 import cc.rainwave.android.api.types.Song;
 import cc.rainwave.android.views.CountdownView;
 
 public class PlaylistActivity extends ListActivity {
 	
 	private static final String TAG = "PlaylistActivity";
 	
 	private Artist mArtists[];
 	
 	private Album mAlbums[];
 	
 	private Song mSongs[];
 	
 	private Session mSession;
 	
 	private FetchAlbumsTask mFetchAlbums;
 	
 	private FetchArtistsTask mFetchArtists;
 	
 	private FetchDetailedAlbumTask mFetchAlbum;
 	
 	private FetchDetailedArtistTask mFetchArtist;
 	
 	private RequestTask mRequest;
 	
 	private int mMode = MODE_TOP_LEVEL;
 	
 	private Comparator<Song> mAlbumSongComparator = new Comparator<Song>() {
 		@Override
 		public int compare(Song lhs, Song rhs) {
 			if(lhs.isCooling() ^ rhs.isCooling()) {
 				return (lhs.isCooling()) ? 1 : -1;
 			}
 			
 			return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
 		}
 	};
 	
 	private Comparator<Song> mArtistSongComparator = new Comparator<Song>() {
 		@Override
 		public int compare(Song lhs, Song rhs) {
 			final String lhsAlbumName = lhs.getDefaultAlbum().getName();
 			final String rhsAlbumName = rhs.getDefaultAlbum().getName();
 			if(!lhsAlbumName.equals(rhsAlbumName)) {
 				return lhsAlbumName.compareTo(rhsAlbumName);
 			}
 			
 			return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
 		}
 	};
 	
 	private Comparator<Artist> mArtistComparator = new Comparator<Artist>() {
 		@Override
 		public int compare(Artist lhs, Artist rhs) {
 			return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
 		}
 	};
 	
 	private Comparator<Album> mAlbumComparator = new Comparator<Album>() {
 		@Override
 		public int compare(Album lhs, Album rhs) {
 			if(lhs.isCooling() ^ rhs.isCooling()) {
 				return (lhs.isCooling()) ? 1 : -1;
 			}
 			
 			return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
 		}
 	};
 	
 	
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		setContentView(R.layout.activity_playlist);
 		setListeners();
 	}
 	
 	public void onResume() {
 		super.onResume();
 		mSession = Session.getInstance();
 		fetchDataIfNeeded();
 	}
 	
 	public boolean onKeyDown(int keyCode, KeyEvent ev) {
 		switch(keyCode) {
 		case KeyEvent.KEYCODE_BACK:
 			if(mMode == MODE_DETAIL_ALBUM || mMode == MODE_DETAIL_ARTIST) {
 				mMode = MODE_TOP_LEVEL;
 				setTheData();
 				return true;
 			}
 		}
 		return super.onKeyDown(keyCode, ev);
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		
 		switch(mMode) {
 		case MODE_DETAIL_ALBUM:
 		case MODE_DETAIL_ARTIST:
 			break;
 		default:
 			return;
 		}
 		
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.playlist_context_menu, menu);
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 		Song s = (Song) getListView().getItemAtPosition(info.position);
 		menu.setHeaderTitle(s.getTitle());
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		switch (item.getItemId()) {
 		case R.id.request:
 			Song s = (Song) getListView().getItemAtPosition(info.position);
 			request(s.getId());
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 	
    /** Shows the menu */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.playlist_menu, menu);
 		return true;
 	}
 
	/** Responds to menu selection */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.menu_refresh:
 			// clear result list and hide the filter
 			setListAdapter(null);
 			hideFilter();
 			
 			// check if we're looking at albums or artists and refresh
 			if(isByAlbum()) {
 				mAlbums = null;
 				mFetchAlbums = null;
 				fetchAlbums(true);
 			}
 			else {
 				mArtists = null;
 				mFetchArtists = null;
 				fetchArtists(true);
 			}
			break;
 		}
 		
 		return false;
 	}
 	
     private void setListeners() {
     	RadioButton a = (RadioButton) findViewById(R.id.by_album);
     	RadioButton b = (RadioButton) findViewById(R.id.by_artist);
     	EditText filterText = (EditText) findViewById(R.id.filterText);
     	
     	OnClickListener tmp = new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				mMode = MODE_TOP_LEVEL;
 				setTheData();
 			}
     	};
     	
     	getListView().setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				if(mMode == MODE_TOP_LEVEL && isByAlbum()) {
 					ArrayAdapter<Album> adapter = (ArrayAdapter<Album>) getListAdapter();
 					hideFilter();
 					setListAdapter(null);
 					mMode = MODE_DETAIL_ALBUM;
 					Album choice = adapter.getItem(position);
 					fetchAlbum(choice.getId());
 				}
 				else if(mMode == MODE_TOP_LEVEL) {
 					ArrayAdapter<Artist> adapter = (ArrayAdapter<Artist>) getListAdapter();
 					hideFilter();
 					setListAdapter(null);
 					mMode = MODE_DETAIL_ARTIST;
 					Artist choice = adapter.getItem(position);
 					fetchArtist(choice.getId());
 				}
 			}
     	});
     	
     	a.setOnClickListener(tmp);
     	b.setOnClickListener(tmp);
     	
     	filterText.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				if(mMode != MODE_TOP_LEVEL) return;
 				if(isByAlbum()) {
 					ArrayAdapter<Album> adapter = (ArrayAdapter<Album>) getListAdapter();
 					if(adapter == null) return;
 					adapter.getFilter().filter(s);
 				}
 				else {
 					ArrayAdapter<Artist> adapter = (ArrayAdapter<Artist>) getListAdapter();
 					if(adapter == null) return;
 					adapter.getFilter().filter(s);
 				}
 			}
     		
     	});
     	
     	registerForContextMenu(getListView());
     }
     
     private EditText hideFilter() {
     	EditText filterText = (EditText) findViewById(R.id.filterText);
     	filterText.setVisibility(View.GONE);
     	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
     	imm.hideSoftInputFromWindow(filterText.getWindowToken(), 0);
     	return filterText;
     }
     
     private void setTheData() {
     	EditText filterText = hideFilter();
     	if(isByAlbum() && mMode == MODE_TOP_LEVEL) {
     		if(mAlbums != null) {
     			ArrayAdapter<Album> adapter = new ArrayAdapter<Album>(this, android.R.layout.simple_list_item_1, mAlbums) {
     				public View getView(int position, View convertView, ViewGroup parent) {
     					View v = super.getView(position, convertView, parent);
     					Album a = getItem(position);
     					if(a.isCooling()) {
     						v.setBackgroundResource(R.drawable.gradient_cooldown);
     					}
     					else {
     						v.setBackgroundDrawable(null);
     					}
     					return v;
     				}
     			};
     			adapter.sort(mAlbumComparator);
     			filterText.setText("");
     			filterText.setHint(R.string.msg_filterAlbum);
     			setListAdapter(adapter);
     			filterText.setVisibility(View.VISIBLE);
     		}
     		else {
     			setListAdapter(null);
     			fetchAlbums(false);
     		}
     	}
     	else if(mMode == MODE_TOP_LEVEL){
     		if(mArtists != null) {
     			ArrayAdapter<Artist> adapter = new ArrayAdapter<Artist>(this, android.R.layout.simple_list_item_1, mArtists);
     			adapter.sort(mArtistComparator);
     			filterText.setHint(R.string.msg_filterArtist);
     			filterText.setText("");
     			setListAdapter(adapter);
     			filterText.setVisibility(View.VISIBLE);
     		}
     		else {
     			setListAdapter(null);
     			fetchArtists(false);
     		}
     	}
     	else if(mMode == MODE_DETAIL_ALBUM || mMode == MODE_DETAIL_ARTIST) {
     		if(mSongs != null) {
     			SongArrayAdapter adapter = new SongArrayAdapter(this, R.layout.item_song_playlist, mSongs, mMode);
     			adapter.sort((mMode == MODE_DETAIL_ALBUM) ? mAlbumSongComparator : mArtistSongComparator);
     			setListAdapter(adapter);
     		}
     	}
     }
 
 	private void fetchDataIfNeeded() {
 		if(isByAlbum() && mAlbums == null) {
 			fetchAlbums(false);
 		}
 		else if(mArtists == null) {
 			fetchArtists(false);
 		}
 	}
 	
 	private void stopAlbumFetch() {
 		if(mFetchAlbum != null) {
 			mFetchAlbum.cancel(true);
 			mFetchAlbum = null;
 		}
 	}
 	
 	private void stopArtistFetch() {
 		if(mFetchArtist != null) {
 			mFetchArtist.cancel(true);
 			mFetchArtist = null;
 		}
 	}
 	
 	private void request(int song_id) {
 		if(mRequest != null) return;
 		mRequest = new RequestTask();
 		mRequest.execute(song_id);
 	}
 	
 	private void fetchAlbums(final boolean forceRefresh) {
 		if(mFetchAlbums != null) return;
 		mFetchAlbums = new FetchAlbumsTask(forceRefresh);
 		mFetchAlbums.execute();
 	}
 	
 	private void fetchArtists(final boolean forceRefresh) {
 		if(mFetchArtists != null) return;
 		mFetchArtists = new FetchArtistsTask(forceRefresh);
 		mFetchArtists.execute();
 	}
 	
 	private void fetchAlbum(int album_id) {
 		stopArtistFetch();
 		if(mFetchAlbum != null) return;
 		mFetchAlbum = new FetchDetailedAlbumTask();
 		mFetchAlbum.execute(album_id);
 	}
 	
 	private void fetchArtist(int artist_id) {
 		stopAlbumFetch();
 		if(mFetchArtist != null) return;
 		mFetchArtist = new FetchDetailedArtistTask();
 		mFetchArtist.execute(artist_id);
 	}
 	
 	private boolean isByAlbum() {
 		RadioButton b = (RadioButton) findViewById(R.id.by_album);
 		return b.isChecked();
 	}
 	
 	private class FetchAlbumsTask extends AsyncTask<String,String,Album[]> {
 		
 		private boolean mForceRefresh;
 		
 		public FetchAlbumsTask(final boolean forceRefresh) {
 			super();
 			mForceRefresh = forceRefresh;
 		}
 		
 		@Override
 		protected Album[] doInBackground(String... args) {
 			Log.d(TAG, "Fetching albumsin background...");
 			try {
 				return mSession.getAlbums(mForceRefresh);
 			} catch (IOException e) {
 				Rainwave.showError(PlaylistActivity.this, e);
 				Log.e(TAG, "IO Error: " + e);
 			} catch (RainwaveException e) {
 				Rainwave.showError(PlaylistActivity.this, e);
 				Log.e(TAG, "API Error: " + e);
 			}
 			return null;
 		}
 		
 		protected void onPostExecute(Album result[]) {
 			if(result == null) return;
 			mAlbums = result;
 			updateView();
 		}
 	}
 	
 	private class FetchArtistsTask extends AsyncTask<String,String,Artist[]> {
 		private boolean mForceRefresh;
 		
 		public FetchArtistsTask(final boolean forceRefresh) {
 			super();
 			mForceRefresh = forceRefresh;
 		}
 		
 		@Override
 		protected Artist[] doInBackground(String... args) {
 			try {
 				return mSession.getArtists(mForceRefresh);
 			} catch (IOException e) {
 				Rainwave.showError(PlaylistActivity.this, e);
 				Log.e(TAG, "IO Error: " + e);
 			} catch (RainwaveException e) {
 				Rainwave.showError(PlaylistActivity.this, e);
 				Log.e(TAG, "API Error: " + e);
 			}
 			return null;
 		}
 		
 		protected void onPostExecute(Artist result[]) {
 			if(result == null) return;
 			mArtists = result;
 			updateView();
 		}
 	}
 	
 	private class FetchDetailedArtistTask extends AsyncTask<Integer,String,Artist> {
 		@Override
 		protected Artist doInBackground(Integer ... args) {
 			int artist_id = args[0];
 			try {
 				return mSession.getDetailedArtist(artist_id);
 			} catch (IOException e) {
 				Rainwave.showError(PlaylistActivity.this, e);
 				Log.e(TAG, "IO Error: " + e);
 			} catch (RainwaveException e) {
 				Rainwave.showError(PlaylistActivity.this, e);
 				Log.e(TAG, "API Error: " + e);
 			}
 			return null;
 		}
 		
 		protected void onPostExecute(Artist result) {
 			if(result == null) {
 				mFetchArtist = null;
 				return;
 			}
 			mSongs = result.cloneSongs();
 			updateView();
 			mFetchArtist = null;
 		}
 	}
 	
 	private class FetchDetailedAlbumTask extends AsyncTask<Integer,String,Album> {
 		@Override
 		protected Album doInBackground(Integer ... args) {
 			int album_id = args[0];
 			try {
 				Log.d(TAG, "Fetching album...");
 				return mSession.getDetailedAlbum(album_id);
 			} catch (IOException e) {
 				Rainwave.showError(PlaylistActivity.this, e);
 				Log.e(TAG, "IO Error: " + e);
 			} catch (RainwaveException e) {
 				Rainwave.showError(PlaylistActivity.this, e);
 				Log.e(TAG, "API Error: " + e);
 			}
 			Log.d(TAG, "Error fetching album!");
 			return null;
 		}
 		
 		protected void onPostExecute(Album result) {
 			if(result == null){
 				Log.d(TAG, "Album fetch failed!");
 				mFetchAlbum = null;
 				return;
 			}
 			mSongs = result.cloneSongs();
 			updateView();
 			mFetchAlbum = null;
 		}
 	}
 	
 	private void updateView() {
 		mHandler.obtainMessage(SET_THE_DATA).sendToTarget();
 	}
 	
     private Handler mHandler = new Handler() {
     	public void handleMessage(Message msg) {
     		Bundle data = msg.getData();
     		switch(msg.what) {
     		case SET_THE_DATA:
     			setTheData();
     			break;
     			
     		case SUCCESSFUL_REQUEST:
     			Toast.makeText(PlaylistActivity.this, R.string.msg_requested, Toast.LENGTH_SHORT).show();
     			break;
     		}
     	}
     };
     
     private class RequestTask extends AsyncTask<Integer, Integer, Song[]> {
 		@Override
 		protected Song[] doInBackground(Integer... args) {
 			int song_id = args[0];
 			
 			try {
 				return mSession.submitRequest(song_id);
 			} catch (IOException e) {
 				Rainwave.showError(PlaylistActivity.this, e);
 				Log.e(TAG, "IO Error: " + e);
 			} catch (RainwaveException e) {
 				Rainwave.showError(PlaylistActivity.this, e);
 				Log.e(TAG, "API Error: " + e);
 			}
 			return null;
 		}
 		
 		protected void onPostExecute(Song[] songs) {
 			if(songs == null){
 				mRequest = null;
 				return;
 			}
 			mHandler.obtainMessage(SUCCESSFUL_REQUEST).sendToTarget();
 			mRequest = null;
 		}
     	
     }
     
     private class SongArrayAdapter extends ArrayAdapter<Song> {
 		class ViewHolder {
 			TextView text1, text2, time, cooldown;
 			CountdownView circle;
 		}
 		
 		private int mLayout;
 		private int mMode;
 		
 		public SongArrayAdapter(Context context, int layout, Song songs[], int mode) {
 			super(context,layout,songs);
 			mLayout = layout;
 			mMode = mode;
 		}
 		
 		public View getView(int position, View convertView, ViewGroup parent) {
 			Song s = getItem(position);
 			ViewHolder holder;
 			if(convertView == null) {
 				Context ctx = getContext();
 				LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				
 				convertView = inflater.inflate(R.layout.item_song_playlist, null);
 				holder = new ViewHolder();
 				
 				holder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
 				holder.text2 = (TextView) convertView.findViewById(android.R.id.text2);
 				holder.circle = (CountdownView) convertView.findViewById(R.id.circle);
 				holder.time = (TextView) convertView.findViewById(R.id.time);
 				holder.cooldown = (TextView) convertView.findViewById(R.id.cooldown);
 				convertView.setTag(holder);
 			}
 			else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 			
 			
 			// We should have at least this much for both views.
 			holder.text1.setText(s.getTitle());
 			holder.time.setText(s.getLengthString());
 			
 			if(s.isCooling()) {
 				long time = s.getCooldown();
 				holder.cooldown.setText(Rainwave.getTimeTemplate(getContext(), time));
 				holder.cooldown.setVisibility(View.VISIBLE);
 				
 				Drawable d = getContext().getResources().getDrawable(R.drawable.gradient_cooldown);
 				convertView.setBackgroundDrawable(d);
 			}
 			else {
 				holder.cooldown.setVisibility(View.GONE);
 				convertView.setBackgroundDrawable(null);
 			}
 			
 			if(mMode == MODE_DETAIL_ALBUM) {
 				holder.circle.setVisibility(View.VISIBLE);
 				holder.circle.setBoth(s.getUserRating(), s.getCommunityRating());
 				holder.text2.setText(s.collapseArtists());
 			}
 			else {
 				holder.circle.setVisibility(View.GONE);
 				holder.text2.setText(s.getDefaultAlbum().getName());
 			}
 			
 			return convertView;
 		}
     }
 	
     public static final int
     	MODE_TOP_LEVEL = 1,
     	MODE_DETAIL_ALBUM = 2,
     	MODE_DETAIL_ARTIST = 4;
     
 	public static final int
 		SUCCESSFUL_REQUEST = 0x43C357,
 		SET_THE_DATA = 0x5E7DA7A;
 }
