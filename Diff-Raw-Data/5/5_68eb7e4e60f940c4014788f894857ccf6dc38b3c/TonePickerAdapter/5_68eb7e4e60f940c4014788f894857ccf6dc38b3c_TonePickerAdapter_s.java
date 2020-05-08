 package com.hlidskialf.android.tonepicker;
 
 
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.CheckedTextView;
 import android.widget.TextView;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import android.util.Log;
 
 public class TonePickerAdapter extends BaseExpandableListAdapter {
   private Context mContext;
   private LayoutInflater mInflater;
   private ContentResolver mContentResolver;
   private boolean mShowSlots;
   private long mSelectedId=-1;
   private boolean mStorageMounted=false;
 
   private String[] BUILTIN_NAMES;
 
   private static int INDEX_SLOTS;
   private static int INDEX_FIRST_BUILTIN;
   private static int INDEX_RINGTONES;
   private static int INDEX_NOTIFICATIONS;
   private static int INDEX_ALARMS;
   private static int INDEX_FIRST_ALBUM;
   private static int INDEX_CONTENTS;
   private static int INDEX_PICKERS;
 
   private Cursor mCursor_album;
   private ToneCursor mCursor_ring;
   private ToneCursor mCursor_notify;
   private ToneCursor mCursor_alarm;
   private LinkedHashMap<Integer,ToneCursor> mCursor_tracks;
   private LinkedHashMap<Integer,String> mAlbumNames;
 
   private int mColIdx_album_artist;
   private int mColIdx_album_album;
   private int mColIdx_album_album_id;
   private int mColIdx_track_title;
   private int mColIdx_track_track;
   private int mColIdx_track_id;
 
   public class ViewHolder {
     CheckedTextView label;
   }
 
  abstract class ToneCursor
   {
     protected Cursor mCursor;
     private LinkedHashMap<Integer,Tone> mTones;
 
     public class Tone {
       public String name;
       public Uri uri;
       public Tone() {}
     };
 
     public ToneCursor() { init(); }
     public ToneCursor(Cursor c) { 
       init();
       mCursor = c;
     }
     protected void init() {
       mTones = new LinkedHashMap<Integer,Tone>();
     }
     public Tone getTone(int position) {
       if (! mTones.containsKey(position)) {
         Tone t = this.cacheTone(position);
         mTones.put(position, t);
         return t;
       }
       return mTones.get(position);
     }
     abstract protected Tone cacheTone(int position);
 
     public int getCount() { return mCursor.getCount(); }
   }
 
   class BuiltinToneCursor extends ToneCursor
   {
     private RingtoneManager mRingtoneManager;
     public BuiltinToneCursor(int ringtone_type) { 
       init();
       mRingtoneManager = new RingtoneManager(mContext);
       mRingtoneManager.setType(ringtone_type);
       mRingtoneManager.setIncludeDrm(true);
       mCursor = mRingtoneManager.getCursor();
     }
     public Tone cacheTone(int position) {
       mCursor.moveToPosition(position);
       Tone tone = new Tone();
       tone.name = mCursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
       tone.uri = mRingtoneManager.getRingtoneUri(position);
       return tone;
     }
   }
 
   class MediaToneCursor extends ToneCursor
   {
     public MediaToneCursor(Cursor c) { super(c); }
     public Tone cacheTone(int position) {
       mCursor.moveToPosition(position);
       Tone tone = new Tone();
       long id = mCursor.getLong(mColIdx_track_id);
       tone.uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
       String track = mCursor.getString(mColIdx_track_track);
       String name = mCursor.getString(mColIdx_track_title);
       if (track != null && track.length() > 0 && Integer.valueOf(track) > 0) {
         tone.name = track + ". " + name;
       }
       else
         tone.name = name;
       return tone;
     }
   }
 
   public TonePickerAdapter(Context context)
   {
     mContext = context;
     mContentResolver = mContext.getContentResolver();
     mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 
     BUILTIN_NAMES = new String[] { 
       mContext.getString(R.string.ringtones), 
       mContext.getString(R.string.notifications), 
       mContext.getString(R.string.alarms) 
     };
     
     
   
 
     mAlbumNames = new LinkedHashMap<Integer,String>();
     mCursor_tracks = new LinkedHashMap<Integer,ToneCursor>();
 
     INDEX_SLOTS=-1;
     INDEX_FIRST_BUILTIN=INDEX_SLOTS+1;
     INDEX_RINGTONES=INDEX_FIRST_BUILTIN+0;
     INDEX_NOTIFICATIONS=INDEX_FIRST_BUILTIN+1;
     INDEX_ALARMS=INDEX_FIRST_BUILTIN+2;
     INDEX_FIRST_ALBUM=INDEX_FIRST_BUILTIN+3;
 
     mCursor_ring = _builtin_cursor(RingtoneManager.TYPE_RINGTONE);
     mCursor_notify = _builtin_cursor(RingtoneManager.TYPE_NOTIFICATION);
     mCursor_alarm = _builtin_cursor(RingtoneManager.TYPE_ALARM);
 
     //mCursor_album = _album_cursor();
     refreshStorage();
   }
 
   public boolean isChildSelectable(int groupPosition, int childPosition) 
   {
     return true;
   }
 
   public boolean hasStableIds()
   {
     return true;
   }
 
   public long getGroupId(int groupPosition)
   {
     return groupPosition;
   }
 
   public int getGroupCount()
   {
     int count = INDEX_FIRST_ALBUM; //slots + builtins
     if (mCursor_album != null)
       count += mCursor_album.getCount();
 
     return count;
   }
 
   public Object getGroup(int groupPosition)
   {
     /* note: high -> low */
     if (groupPosition >= INDEX_FIRST_ALBUM) {
       return _get_track_cursor(groupPosition - INDEX_FIRST_ALBUM);
     }
     if (groupPosition == INDEX_ALARMS) {
       return mCursor_alarm;
     }
     if (groupPosition == INDEX_NOTIFICATIONS) {
       return mCursor_notify;
     }
     if (groupPosition == INDEX_RINGTONES) {
       return mCursor_ring;
     }
     /*
     if (mShowSlots && groupPosition == INDEX_SLOTS) {
       return mSlotCache;
     }
     */
 
     return null;
   }
 
   public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
   {
     if (convertView == null) {
       convertView = mInflater.inflate(R.layout.tonepicker_group, null);
       TextView tv = (TextView)convertView.findViewById(android.R.id.text1);
       convertView.setTag(tv);
     }
     TextView tv = (TextView)convertView.getTag();
     String text = null;
 
     if (groupPosition >= INDEX_FIRST_ALBUM)
       text = _get_album_name(groupPosition - INDEX_FIRST_ALBUM);
     else
     if (groupPosition >= INDEX_FIRST_BUILTIN)
       text = BUILTIN_NAMES[groupPosition - INDEX_FIRST_BUILTIN];
     tv.setText(text);
 
     return convertView;
   }
 
   public long getChildId(int groupPosition, int childPosition) 
   {
     return ((long)groupPosition<<32) | childPosition;
   }
 
   public int getChildrenCount(int groupPosition)
   {
     ToneCursor cursor = (ToneCursor)getGroup(groupPosition);
     return cursor.getCount();
   }
 
   public Object getChild(int groupPosition, int childPosition)
   {
     ToneCursor cursor = (ToneCursor)getGroup(groupPosition);
     return cursor.getTone(childPosition);
   }
 
   public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
   {
     ToneCursor.Tone tone = (ToneCursor.Tone)getChild(groupPosition, childPosition);
     ViewHolder holder;
     if (convertView == null) {
       convertView = mInflater.inflate(R.layout.tonepicker_child, null);
       holder = new ViewHolder();
       holder.label = (CheckedTextView)convertView.findViewById(android.R.id.text1);
       convertView.setTag(holder);
     } else {
       holder = (ViewHolder)convertView.getTag();
     }
 
     holder.label.setText(tone.name);
     holder.label.setChecked(getChildId(groupPosition, childPosition) == mSelectedId);
     return convertView;
   }
 
   public void setSelectedId(long id)
   {
     mSelectedId = id;
   }
 
   private BuiltinToneCursor _builtin_cursor(int ringtone_type)
   {
     return new BuiltinToneCursor(ringtone_type);
   }
 
   private Cursor _album_cursor()
   {
     Cursor cursor = mContentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[] {
       MediaStore.Audio.Albums._ID,
       MediaStore.Audio.Albums.ARTIST,
       MediaStore.Audio.Albums.ALBUM,
     }, null, null, MediaStore.Audio.Albums.ARTIST+","+MediaStore.Audio.Albums.ALBUM);
     if (cursor != null) {
       mColIdx_album_artist = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
       mColIdx_album_album = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
       mColIdx_album_album_id = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
     }
     return cursor;
   }
 
   private MediaToneCursor _get_track_cursor(int position)
   {
     MediaToneCursor tc = null;
 
     if (mCursor_tracks.containsKey(position)) {
       tc = (MediaToneCursor)mCursor_tracks.get(position);
     } else {
       mCursor_album.moveToPosition(position);
       long album_id = mCursor_album.getLong(mColIdx_album_album_id);
 
       StringBuilder where = new StringBuilder();
       where.append(MediaStore.Audio.Media.ALBUM_ID + " = ?");
       Cursor cursor = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
         MediaStore.Audio.Media._ID,
         MediaStore.Audio.Media.TITLE,
         MediaStore.Audio.Media.TRACK
       }, where.toString(), new String[] { String.valueOf(album_id) }, MediaStore.Audio.Media.TRACK);
       if (cursor != null) {
         tc = new MediaToneCursor(cursor);
         mCursor_tracks.put(position, tc);
 
         mColIdx_track_title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
         mColIdx_track_track = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
         mColIdx_track_id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
       }
     }
 
     return tc;
   }
 
   private String _get_album_name(int position)
   {
     String name;
 
     if (mAlbumNames.containsKey(position)) {
       name = mAlbumNames.get(position);
     }
     else {
       mCursor_album.moveToPosition(position);
       String artist = mCursor_album.getString(mColIdx_album_artist);
       String album = mCursor_album.getString(mColIdx_album_album);
       name = artist + " / " + album;
     }
     return name;
   }
 
   public void refreshStorage() 
   {
     _check_mounted_state();
     mCursor_album = null;
     if (mStorageMounted) {
       mCursor_album = _album_cursor();
     }
   }
   private void _check_mounted_state()
   {
     String state = Environment.getExternalStorageState();
     if (Environment.MEDIA_MOUNTED.equals(state)) {
       //mount
       mStorageMounted = true;
     }
     else {
       //unmount
       mStorageMounted = false;
     }
   }
 
 }
