 package com.vampireneoapp.passiontimes.ui;
 
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 
 import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
 import com.vampireneoapp.passiontimes.R;
 import com.vampireneoapp.passiontimes.core.Article;
 import com.vampireneoapp.passiontimes.core.Channel;
 import com.vampireneoapp.passiontimes.core.ThumbnailLoader;
 
 import java.util.List;
 
 /**
  * Adapter to display a list of traffic items
  */
 public class ChannelListAdapter extends SingleTypeAdapter<Channel> {
 
     private final ThumbnailLoader avatars;
 
     /**
      * @param inflater
      * @param items
      */
     public ChannelListAdapter(LayoutInflater inflater, List<Channel> items, ThumbnailLoader avatars) {
         super(inflater, R.layout.channel_list_item);
 
         this.avatars = avatars;
         setItems(items);
     }
 
     /**
      * @param inflater
      */
     public ChannelListAdapter(LayoutInflater inflater, ThumbnailLoader avatars) {
         this(inflater, null, avatars);
 
     }
 
     @Override
     public long getItemId(final int position) {
         final String id = getItem(position).getId();
         return !TextUtils.isEmpty(id) ? id.hashCode() : super
                 .getItemId(position);
     }
 
     @Override
     protected int[] getChildViewIds() {
        return new int[] { R.id.iv_icon, R.id.tv_title, R.id.tv_author, R.id.tv_desc };
     }
 
     @Override
     protected void update(int position, Channel channel) {
 
        avatars.bind(imageView(0), channel);
 
         //setText(1, String.format("%1$s %2$s", article.getTitle(), article.getAuthor()));
         setText(1, channel.getTitle());
         //setText(2, channel.getAuthor());
         setText(3, channel.getDesc());
     }
 
 }
