 package org.eatabrick.radio;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import java.util.List;
 import org.bff.javampd.objects.*;
 
 public class SongListAdapter extends ArrayAdapter<MPDSong> {
   private static final String TAG = "SongListAdapter";
 
   private int mCurrentSong = -1;
 
   public SongListAdapter(Context context) {
     super(context, android.R.id.empty);
   }
 
   @Override public View getView(int pos, View view, ViewGroup group) {
     if (view == null) {
       view = View.inflate(getContext(), R.layout.song, null);
     }
 
     MPDSong song = getItem(pos);
 
    String title  = song.getTitle()  == null ? getContext().getString(R.string.missing_title)  : song.getTitle();
    String artist = song.getArtist() == null ? getContext().getString(R.string.missing_artist) : song.getArtist().toString();
    String album  = song.getAlbum()  == null ? getContext().getString(R.string.missing_album)  : song.getAlbum().toString();
 
    ((TextView) view.findViewById(R.id.song_title)).setText(title);
    ((TextView) view.findViewById(R.id.song_artist)).setText(artist);
    ((TextView) view.findViewById(R.id.song_album)).setText(album);
 
     ((ImageView) view.findViewById(R.id.song_status)).setImageResource(pos == mCurrentSong ? R.drawable.ic_menu_play : 0);
 
     return view;
   }
 
   public void setCurrentSong(int pos) {
     mCurrentSong = pos;
   }
 
   public int getCurrentSong() {
     return mCurrentSong;
   }
 }
