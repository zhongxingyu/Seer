 // Copyright 2009 Google Inc.
 // Copyright 2011 NPR
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package org.npr.android.news;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Html;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import org.npr.android.util.PlaylistRepository;
 import org.npr.api.Client;
 import org.npr.api.Story;
 import org.npr.api.Story.Audio;
 import org.npr.api.Story.StoryFactory;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.util.List;
 
 
 public class NewsListAdapter extends ArrayAdapter<Story> {
   private static final String LOG_TAG = NewsListAdapter.class.getName();
   private final LayoutInflater inflater;
   private final ImageThreadLoader imageLoader;
   private RootActivity rootActivity = null;
   private final PlaylistRepository repository;
   private long lastUpdate = -1;
   private StoriesLoadedListener storiesLoadedListener;
 
   public NewsListAdapter(Context context) {
     super(context, R.layout.news_item);
     if (context instanceof RootActivity) {
       rootActivity = (RootActivity) context;
     }
     inflater = LayoutInflater.from(getContext());
     imageLoader = ImageThreadLoader.getOnDiskInstance(context);
     repository = new PlaylistRepository(getContext().getApplicationContext(),
         context.getContentResolver());
   }
 
   private List<Story> moreStories;
   private boolean endReached = false;
 
   private final Handler handler = new Handler() {
     @Override
     public void handleMessage(Message msg) {
       if (msg.what >= 0) {
         if (moreStories != null) {
           lastUpdate = System.currentTimeMillis();
           remove(null);
           for (Story s : moreStories) {
             if (getPosition(s) < 0) {
               add(s);
             }
           }
           if (!endReached) {
             add(null);
           }
         }
         if (storiesLoadedListener != null) {
           storiesLoadedListener.storiesLoaded();
         }
       } else {
         Toast.makeText(rootActivity,
             rootActivity.getResources()
                 .getText(R.string.msg_check_connection),
             Toast.LENGTH_LONG).show();
       }
       if (rootActivity != null) {
         rootActivity.stopIndeterminateProgressIndicator();
       }
 
     }
   };
 
   private boolean isPlayable(Story story) {
     for (Audio a : story.getAudios()) {
       if (a.getType().equals("primary")) {
         for (Audio.Format f : a.getFormats()) {
           if (f.getMp3() != null) {
             return true;
           }
         }
       }
     }
     return false;
   }
 
   @Override
   public View getView(final int position, View convertView, final ViewGroup parent) {
 
     if (convertView == null) {
       convertView = inflater.inflate(R.layout.news_item, parent, false);
     }
 
     Story story = getItem(position);
 
     ImageView icon = (ImageView) convertView.findViewById(R.id.NewsItemIcon);
     TextView topic = (TextView) convertView.findViewById(R.id.NewsItemTopicText);
     TextView name = (TextView) convertView.findViewById(R.id.NewsItemNameText);
     final ImageView image = (ImageView) convertView.findViewById(R.id.NewsItemImage);
 
     if (story != null) {
       if (isPlayable(story)) {
         if (repository.getPlaylistItemFromStoryId(story.getId()) == null) {
           icon.setImageDrawable(
               getContext().getResources().getDrawable(R.drawable.speaker)
           );
         } else {
           icon.setImageDrawable(
               getContext().getResources().getDrawable(R.drawable
                   .news_item_in_playlist)
           );
         }
         icon.setVisibility(View.VISIBLE);
       } else {
         // Because views are re-used we have to set this each time
         icon.setVisibility(View.INVISIBLE);
       }
 
       name.setText(Html.fromHtml(story.toString()));
 
       // Need to (re)set this because the views are reused. If we don't then
       // while scrolling, some items will replace the old "Load more stories"
       // view and will be in italics
       name.setTypeface(name.getTypeface(), Typeface.BOLD);
 
       String topicText = story.getSlug();
       for (Story.Parent p : story.getParentTopics()) {
         if (p.isPrimary()) {
           topicText = p.getTitle();
         }
       }
      topic.setText(topicText.toLowerCase());
       topic.setVisibility(View.VISIBLE);
 
       String imageUrl = null;
       if (story.getThumbnails().size() > 0) {
         imageUrl = story.getThumbnails().get(0).getMedium();
       } else if (story.getImages().size() > 0) {
         imageUrl = story.getImages().get(0).getSrc();
       }
       if (imageUrl != null) {
         Drawable cachedImage = imageLoader.loadImage(
             imageUrl,
             new ImageLoadListener(position, (ListView) parent)
         );
 
         image.setImageDrawable(cachedImage);
 
         image.setVisibility(View.VISIBLE);
       } else {
         image.setVisibility(View.GONE);
       }
     } else {
       // null marker means it's the end of the list.
       icon.setVisibility(View.INVISIBLE);
       topic.setVisibility(View.GONE);
       image.setVisibility(View.GONE);
       name.setTypeface(name.getTypeface(), Typeface.ITALIC);
       name.setText(R.string.msg_load_more);
     }
 
     return convertView;
   }
 
   public void addMoreStories(final String url, final int count) {
     if (rootActivity != null) {
       rootActivity.startIndeterminateProgressIndicator();
     }
     new Thread(new Runnable() {
       @Override
       public void run() {
         if (getMoreStories(url, count)) {
           handler.sendEmptyMessage(0);
         } else {
           handler.sendEmptyMessage(-1);
         }
       }
     }).start();
   }
 
   private boolean getMoreStories(String url, int count) {
 
     Node stories = null;
     try {
       stories = new Client(url).execute();
     } catch (IOException e) {
       Log.e(LOG_TAG, "", e);
       return false;
     } catch (SAXException e) {
       Log.e(LOG_TAG, "", e);
     } catch (ParserConfigurationException e) {
       Log.e(LOG_TAG, "", e);
     }
 
     if (stories == null) {
       Log.d(LOG_TAG, "stories: none");
     } else {
       Log.d(LOG_TAG, "stories: " + stories.getNodeName());
 
       moreStories = StoryFactory.parseStories(stories);
       if (moreStories != null) {
         if (moreStories.size() < count) {
           endReached = true;
         }
         NewsListActivity.addAllToStoryCache(moreStories);
       }
     }
 
     return true;
   }
 
   /**
    * @return a comma-separated list of story ID's
    */
   public String getStoryIdList() {
     StringBuilder ids = new StringBuilder();
     for (int i = 0; i < getCount(); i++) {
       Story story = getItem(i);
       if (story != null) {
         ids.append(story.getId()).append(",");
       }
     }
     String result = ids.toString();
     if (result.endsWith(",")) {
       result = result.substring(0, result.length() - 1);
     }
     return result;
   }
 
   /**
    * Returns the time (in milliseconds since the epoch) of when
    * the last update was.
    *
    * @return A time unit in milliseconds since the epoch
    */
   public long getLastUpdate() {
     return lastUpdate;
   }
 
 
   /**
    * A call back that can be used to be notified when stories are done
    * loading.
    */
   public interface StoriesLoadedListener {
     void storiesLoaded();
   }
 
   /**
    * Sets a listener to be notified when stories are done loading
    * @param listener A {@link StoriesLoadedListener}
    */
   public void setStoriesLoadedListener(StoriesLoadedListener listener) {
     storiesLoadedListener = listener;
   }
 
   private class ImageLoadListener implements ImageThreadLoader.ImageLoadedListener {
 
     private int position;
     private ListView parent;
 
     public ImageLoadListener(int position, ListView parent) {
       this.position = position;
       this.parent = parent;
     }
 
     public void imageLoaded(Drawable imageBitmap) {
       // Offset 1 for header
       int storyPosition = position + 1;
       View itemView = parent.getChildAt(storyPosition -
           parent.getFirstVisiblePosition());
       if (itemView == null) {
         Log.w(LOG_TAG, "Could not find list item at position " +
             storyPosition);
         return;
       }
       ImageView img = (ImageView)
           itemView.findViewById(R.id.NewsItemImage);
       if (img == null) {
         Log.w(LOG_TAG, "Could not find image for list item at " +
             "position " + storyPosition);
         return;
       }
       Log.d(LOG_TAG, "Drawing image at position " + storyPosition);
       img.setImageDrawable(imageBitmap);
     }
   }
 }
