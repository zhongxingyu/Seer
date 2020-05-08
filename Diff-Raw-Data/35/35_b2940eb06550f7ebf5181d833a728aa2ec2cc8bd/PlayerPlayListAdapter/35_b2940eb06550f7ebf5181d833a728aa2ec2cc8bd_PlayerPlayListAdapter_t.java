 package com.music.fmv.adapters;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.music.fmv.R;
 import com.music.fmv.models.notdbmodels.InternetSong;
 import com.music.fmv.utils.TimeUtils;
 
 import java.util.List;
 
 /**
  * User: vitaliylebedinskiy
  * Date: 9/27/13
  * Time: 1:19 PM
  */
 public class PlayerPlayListAdapter extends FixedBaseAdapter<InternetSong> {
     public PlayerPlayListAdapter(List<InternetSong> mData, Context context) {
         super(mData, context);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent, InternetSong song) {
         ViewHolder holder;
         if (convertView == null) {
             holder = new ViewHolder();
             convertView = inflateView(R.layout.player_playlist_row, parent);
             holder.artist = (TextView) convertView.findViewById(R.id.song_owner);
             holder.name = (TextView) convertView.findViewById(R.id.song_name);
             holder.duration = (TextView) convertView.findViewById(R.id.duration);
             holder.isCached = (ImageView) convertView.findViewById(R.id.is_cached);
             convertView.setTag(holder);
         } else holder = (ViewHolder) convertView.getTag();
 
         holder.name.setText(song.getName());
         holder.artist.setText(song.getArtist());
        holder.duration.setText(TimeUtils.extractTimeFromSong(song));
 
         if (core.getCacheManager().isSongExists(song)) {
             holder.isCached.setVisibility(View.VISIBLE);
         } else holder.isCached.setVisibility(View.GONE);
 
         return convertView;
     }
 
     private class ViewHolder {
         TextView name, artist, duration;
         ImageView isCached;
     }
 }
