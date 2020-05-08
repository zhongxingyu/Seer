 package com.shingrus.myplayer;
 
 import com.shingrus.myplayer.R;
 
 import java.io.File;
 import java.lang.annotation.Target;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.net.Uri;
 import android.os.Handler;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.LinearLayout;
import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class TrackList {
 
 	enum Direction {
 		PREVIOUS, NEXT
 	}
 
 	public static final int LIMIT_TRACKS = 1024;
 	public static final String DATABASE_NAME = "TrackList";
 	public static final int DATABASE_VERSION = 1;
 	public static final String TABLE_NAME = "track";
 	private static final String TRACK_ARTIST = "Artist";
 	private static final String TRACK_TITLE = "Title";
 	private static final String TRACK_FILENAME = "Filename";
 	private static final String TRACK_ID = "Id";
 	private static final String TRACK_URL = "Url";
 	private static final String CREATE_DB = "CREATE TABLE " + TABLE_NAME + "(" + TRACK_ID + " INTEGER PRIMARY KEY autoincrement default 0," + TRACK_ARTIST
 			+ " TEXT not null," + TRACK_TITLE + " TEXT not null, " + TRACK_FILENAME + " TEXT , " + TRACK_URL + " TEXT NOT NULL)";
 	private static final String TRACK_INSERT_STMNT = "INSERT INTO " + TABLE_NAME + " (" + TRACK_ARTIST + "," + TRACK_TITLE + "," + TRACK_URL + ","
 			+ TRACK_FILENAME + ") VALUES (?,?, ?, ?)";
 
 	private static TrackList trackListInstance;
 	private TrackListAdapter adapter;
 	DBHelper dbHelper;
 	private boolean isLoaded = false;
 	List<MusicTrack> trackList;
 	private int iteratePosition = 0;
 	private boolean isPlaying = false;
 
 	// private final Context context;
 
 	// TODO - move basedapter to activity
 	// TODO - give handler from activity
 	// TODO - and don't use runonuithread!
 
 	class TrackListAdapter extends BaseAdapter {
 		private final Activity activity;
 
 		public TrackListAdapter(Activity activity) {
 			super();
 			this.activity = activity;
 		}
 
 		@Override
 		public synchronized int getCount() {
 			return trackList.size();
 		}
 
 		@Override
 		public synchronized Object getItem(int position) {
 			return trackList.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public synchronized View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = this.activity.getLayoutInflater();
			RelativeLayout rowView = (RelativeLayout) inflater.inflate(R.layout.tracklist_item, null, true);
 			TextView text = (TextView) rowView.findViewById(R.id.trackrow_textid);
 			// (TextView) inflater.inflate(R.layout.tracklist_item, null, true);
 			MusicTrack mt = trackList.get(position);
 			text.setText(mt.toString());
 			if (isPlaying && position == iteratePosition) {
 				text.setTextColor(0xAAFF0000);
 			}
 			text = (TextView) rowView.findViewById(R.id.trackrow_statusid);
 			text.setText(mt.getFilename().length() > 0 ? "+" : "-");
 			return rowView;
 		}
 
 	}
 
 	// Create Only static
 	private TrackList() {
 		// trackList = new LinkedHashSet<MusicTrack>();
 		trackList = new CopyOnWriteArrayList<MusicTrack>();
 		// bind to UpdateService
 
 	}
 
 	class DBHelper extends SQLiteOpenHelper {
 
 		public DBHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(CREATE_DB);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 			onCreate(db);
 		}
 	}
 
 	// public class TrackListHandler extends Handler {
 	//
 	// }
 
 	/**
 	 * Loads track list from internal storage
 	 */
 	public synchronized void loadTracks(Context ctx) {
 
 		if (!isLoaded) {
 			dbHelper = new DBHelper(ctx);
 			// Because of ctx we have some warranty it's main thread
 			SQLiteDatabase db = dbHelper.getWritableDatabase();
 			if (db != null) {
 				Cursor c = db.query(TABLE_NAME, new String[] { TRACK_ID, TRACK_ARTIST, TRACK_TITLE, TRACK_URL, TRACK_FILENAME, }, null, new String[] {}, null, null, null);
 				if (c != null && c.moveToFirst()) {
 					do {
 						String filename = c.getString(4);
 						File f = new File(Uri.parse(filename).getPath());
 						if (!f.exists())
 							filename = "";
 						MusicTrack mt = new MusicTrack(c.getString(0), c.getString(1), c.getString(2), c.getString(3), filename);
 						trackList.add(mt);
 					} while (c.moveToNext());
 					c.close();
 				}
 				db.close();
 			}
 			dataChanged();
 			isLoaded = true;
 		}
 	}
 
 	/**
 	 * 
 	 */
 
 	public synchronized void addTrack(final MusicTrack mt) {
 		if (!trackList.contains(mt) && mt.getTitle().length() > 0 && mt.getUrl().length() > 0 && trackList.size() < LIMIT_TRACKS) {
 			Log.d("shingrus", "Adding new track: " + mt + ", trackList size: " + trackList.size());
 			Runnable r = new Runnable() {
 				@Override
 				public void run() {
 					trackList.add(mt);
 					if (dbHelper != null) {
 						SQLiteDatabase db = dbHelper.getWritableDatabase();
 						if (db != null) {
 							SQLiteStatement insertStmt = db.compileStatement(TRACK_INSERT_STMNT);
 							insertStmt.bindString(1, mt.getArtist());
 							insertStmt.bindString(2, mt.getTitle());
 							insertStmt.bindString(3, mt.getUrl());
 							insertStmt.bindString(4, mt.getFilename());
 							long rowid = insertStmt.executeInsert();
 							if (rowid == -1) {
 								Log.i("shingrus", "Can't insert new value to db");
 							} else {
 								mt.setId(Long.toString(rowid));
 							}
 							insertStmt.clearBindings();
 						}
 					}
 					dataChanged();
 
 				}
 			};
 			if (this.adapter != null) {
 				this.adapter.activity.runOnUiThread(r);
 			} else
 				r.run();
 		}
 	}
 
 	public synchronized void setFileName(MusicTrack mt, String filename) {
 		for (MusicTrack track : trackList) {
 			if (track.equals(mt)) {
 				track.setFilename(filename);
 				if (dbHelper != null) {
 					SQLiteDatabase db = dbHelper.getWritableDatabase();
 					if (db != null) {
 						ContentValues values = new ContentValues();
 						values.put(this.TRACK_FILENAME, filename);
 						db.update(TABLE_NAME, values, TRACK_ID + "=?", new String[] { mt.getId() });
 					}
 				}
 			}
 		}
 		// TODO: store into storage
 		Runnable r = new Runnable() {
 
 			@Override
 			public void run() {
 				dataChanged();
 			}
 		};
 		if (this.adapter != null) {
 			this.adapter.activity.runOnUiThread(r);
 		} else
 			r.run();
 	}
 
 	public synchronized boolean contains(MusicTrack mt) {
 		return trackList.contains(mt);
 	}
 
 	public synchronized void removeAll() {
 
 		Runnable r = new Runnable() {
 
 			@Override
 			public void run() {
 				trackList.clear();
 				// TODO store into storage
 				dataChanged();
 			}
 		};
 		if (this.adapter != null) {
 			this.adapter.activity.runOnUiThread(r);
 		} else
 			r.run();
 
 	}
 
 	static public synchronized TrackList getInstance() {
 		if (trackListInstance == null) {
 			trackListInstance = new TrackList();
 		}
 		return trackListInstance;
 	}
 
 	public synchronized MusicTrack getNextForDownLoad() {
 		synchronized (this.trackList) {
 			for (MusicTrack mt : trackList) {
 				if (mt.getFilename() == null || mt.getFilename().length() < 1) {
 					return mt;
 				}
 			}
 		}
 		return null;
 	}
 
 	public final synchronized MusicTrack getTrackAt(int position) {
 		return trackList.get(position);
 	}
 
 	private final MusicTrack getTrack(Direction direction) {
 		int counter = trackList.size();
 		MusicTrack mt = null;
 		for (; counter > 0; counter--) {
 
 			if (direction == Direction.NEXT) {
 				iteratePosition++;
 				if (iteratePosition >= trackList.size())
 					iteratePosition = 0;
 			} else {
 				--iteratePosition;
 				if (iteratePosition < 0)
 					iteratePosition = trackList.size() - 1;
 			}
 
 			if (trackList.get(iteratePosition).filename.length() > 0) {
 				mt = trackList.get(iteratePosition);
 				break;
 			}
 		}
 		return mt;
 	}
 
 	public final synchronized MusicTrack getPreviuosTrack() {
 		return getTrack(Direction.PREVIOUS);
 	}
 
 	public final synchronized MusicTrack getNextTrack() {
 		return getTrack(Direction.NEXT);
 	}
 
 	public final synchronized MusicTrack startIterateFrom(int position) {
 		// TODO check size and check that we got mt with Bfilename
 		iteratePosition = (position > trackList.size() - 1) ? 0 : position;
 		return trackList.get(iteratePosition);
 	}
 
 	public int getIteratePosition() {
 		return iteratePosition;
 	}
 
 	public final void notifyPlayStarted() {
 		isPlaying = true;
 		dataChanged();
 	}
 
 	public final void notifyPlayStopped() {
 		isPlaying = false;
 		dataChanged();
 	}
 
 	public TrackListAdapter getAdapter(Activity actvty) {
 		adapter = new TrackListAdapter(actvty);
 		return adapter;
 	}
 
 	public void dropAdapter() {
 		adapter = null;
 	}
 
 	private synchronized void dataChanged() {
 		if (adapter != null) {
 			adapter.notifyDataSetChanged();
 		}
 	}
 
 }
