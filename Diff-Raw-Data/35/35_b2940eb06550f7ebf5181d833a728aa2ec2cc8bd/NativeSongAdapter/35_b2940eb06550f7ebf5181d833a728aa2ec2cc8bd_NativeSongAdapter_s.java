 package com.music.fmv.adapters;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import com.fortysevendeg.swipelistview.SwipeListView;
 import com.music.fmv.R;
 import com.music.fmv.models.notdbmodels.FileSystemSong;
 import com.music.fmv.utils.TimeUtils;
 
 import java.util.ArrayList;
 
 /**
  * User: vitaliylebedinskiy
  * Date: 8/1/13
  * Time: 2:43 PM
  */
 
 public class NativeSongAdapter extends FixedBaseAdapter<FileSystemSong> {
     private AdapterCallback callback;
     private SwipeListView listView;
 
     public NativeSongAdapter(Context c, ArrayList<FileSystemSong> list, SwipeListView listView) {
         super(list, c);
         this.listView = listView;
     }
 
     public void setCallback(AdapterCallback callback) {
         this.callback = callback;
     }
 
     @Override
     public FileSystemSong getItem(int position) {
         return mData.get(position);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent, FileSystemSong song) {
         ViewHolder holder;
         if (convertView == null) {
             holder = new ViewHolder();
             convertView = inflateView(R.layout.native_songs_row, parent);
             holder.owner = (TextView) convertView.findViewById(R.id.song_owner);
             holder.duration = (TextView) convertView.findViewById(R.id.duration);
             holder.name = (TextView) convertView.findViewById(R.id.song_name);
             holder.play = convertView.findViewById(R.id.song_play);
             holder.addToQueue = convertView.findViewById(R.id.song_add_to_queu);
             convertView.setTag(holder);
         } else holder = (ViewHolder) convertView.getTag();
 
         holder.addToQueue.setOnClickListener(new ButtonListener(song, position));
         holder.play.setOnClickListener(new ButtonListener(song, position));
 
         holder.owner.setText(song.getArtist());
         holder.name.setText(song.getName());
        holder.duration.setText(TimeUtils.getNiceTimeForSongs(song.getDuration()));
         return convertView;
     }
 
     private class ViewHolder {
         TextView name;
         TextView owner;
         TextView duration;
 
         View play;
         View addToQueue;
     }
 
     private class ButtonListener implements View.OnClickListener {
         private FileSystemSong model;
         private int position;
 
         private ButtonListener(FileSystemSong model, int position) {
             this.model = model;
             this.position = position;
         }
 
         @Override
         public void onClick(View v) {
             listView.closeAnimate(position + 1);
             switch (v.getId()) {
                 case R.id.song_play:
                     if (callback != null) callback.playClicked(model, position);
                     break;
                 case R.id.song_add_to_queu:
                     if (callback != null) callback.addToQueueClicked(model);
             }
         }
     }
 
     public interface AdapterCallback {
         public void playClicked(FileSystemSong model, int pos);
         public void addToQueueClicked(FileSystemSong model);
     }
 }
