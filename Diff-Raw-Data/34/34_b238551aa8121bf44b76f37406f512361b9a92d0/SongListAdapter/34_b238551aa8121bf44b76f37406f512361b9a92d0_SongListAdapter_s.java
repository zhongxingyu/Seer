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
 
    ((TextView) view.findViewById(R.id.song_title)).setText(song.getTitle());
 
    MPDArtist artist = song.getArtist();
    MPDAlbum album = song.getAlbum();

    if (artist != null) ((TextView) view.findViewById(R.id.song_artist)).setText(artist.toString());
    if (album != null) ((TextView) view.findViewById(R.id.song_album)).setText(album.toString());
 
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
