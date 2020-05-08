 /**
  * 
  */
 package org.vimeoid.adapter.guest;
 
 import org.vimeoid.R;
 import org.vimeoid.adapter.EasyCursorsAdapter;
 import org.vimeoid.dto.simple.Video;
 import org.vimeoid.util.Utils;
 
 import com.fedorvlasov.lazylist.ImageLoader;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid.adapter.guest</dd>
  * </dl>
  *
  * <code>VideosListAdapter</code>
  *
  * <p>Description</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Sep 5, 2010 8:35:01 PM 
  *
  */
 public class VideosListAdapter extends EasyCursorsAdapter<Video> {
     
     private final LayoutInflater layoutInflater;
     private final ImageLoader imagesLoader;    
 
     public VideosListAdapter(Context context, LayoutInflater inflater) {
         super(Video.FieldsKeys._ID);
 
         this.layoutInflater = inflater;        
         
         this.imagesLoader = new ImageLoader(context, R.drawable.thumb_loading_small, R.drawable.video_unknown_item);
     }
     
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         VideoItemViewHolder itemHolder = null;
         
         final Video video = (Video)getItem(position); 
         
         if (convertView == null) {
             
             convertView = layoutInflater.inflate(R.layout.item_video, parent, false);
             itemHolder = new VideoItemViewHolder();
                 
             itemHolder.ivThumb = (ImageView) convertView.findViewById(R.id.videoItemImage);
             itemHolder.tvTitle = (TextView) convertView.findViewById(R.id.videoItemTitle);
             itemHolder.tvAuthor = (TextView) convertView.findViewById(R.id.videoItemAuthor);
             itemHolder.tvDuration = (TextView) convertView.findViewById(R.id.videoItemDuration);
             itemHolder.llTags = (LinearLayout) convertView.findViewById(R.id.videoItemTags);
             
             itemHolder.tvLikes = (TextView) convertView.findViewById(R.id.videoItemNumOfLikes);
             itemHolder.tvPlays = (TextView) convertView.findViewById(R.id.videoItemNumOfPlays);
             itemHolder.tvComments = (TextView) convertView.findViewById(R.id.videoItemNumOfComments);
                 
             convertView.setTag(itemHolder);
             
         } else {
             
             itemHolder = (VideoItemViewHolder) convertView.getTag();
             
         }
         
         itemHolder.ivThumb.setTag(video.smallThumbnailUrl);
         imagesLoader.displayImage(video.smallThumbnailUrl, itemHolder.ivThumb);
         
         itemHolder.tvTitle.setText(video.title);
         itemHolder.tvAuthor.setText(video.uploaderName);
         itemHolder.tvDuration.setText(Utils.adaptDuration(video.duration));
         injectTags(video.tags, itemHolder.llTags, position);
         
         itemHolder.tvLikes.setText(String.valueOf(video.likesCount));
         itemHolder.tvPlays.setText(String.valueOf(video.playsCount));
         itemHolder.tvComments.setText(String.valueOf(video.commentsCount));
         
         return convertView;
     }
     
    protected void injectTags(final String[] tags, final ViewGroup group, final int curPosition) {
    	group.removeAllViews();
     	if (tags.length == 0) {
     		return;
     	}
    	for (final String tag: tags) {
     		if (tag.trim().length() == 0) continue;
     		final LinearLayout tagStruct = (LinearLayout)layoutInflater.inflate(R.layout.tag_for_the_item, null);
     		// FIXME: redraws another videos' tags 
     		((TextView)tagStruct.findViewById(R.id.tagItem)).setText(tag.trim());
     		group.addView(tagStruct);
     	}
     }
     
     @Override
     public void finalize() {
         super.finalize();
         imagesLoader.clearCache();
     }
     
     private class VideoItemViewHolder {
         
         ImageView ivThumb;
         
         TextView tvTitle;
         TextView tvAuthor;
         TextView tvDuration;
         LinearLayout llTags;
         
         TextView tvLikes;
         TextView tvPlays;
         TextView tvComments;
     }
 
 	@Override
 	public Video extractItem(Cursor cursor, int position) {
 		return Video.shortFromCursor(cursor, position);
 	}
 
 }
