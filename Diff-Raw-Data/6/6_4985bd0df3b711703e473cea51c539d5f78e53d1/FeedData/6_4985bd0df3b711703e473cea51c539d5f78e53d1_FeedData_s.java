 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2011, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 
 package com.janrain.android.quickshare;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.sax.Element;
 import android.sax.EndElementListener;
 import android.sax.EndTextElementListener;
 import android.sax.RootElement;
 import android.text.Editable;
 import android.text.Html;
 import android.util.Config;
 import android.util.Log;
 import android.util.Pair;
 import android.util.Xml;
 import android.widget.Toast;
 import com.google.android.filecache.FileResponseCache;
 import com.google.android.imageloader.BitmapContentHandler;
 import com.google.android.imageloader.ImageLoader;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.JREngageDelegate;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.net.JRConnectionManager;
 import com.janrain.android.engage.net.JRConnectionManagerDelegate;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.engage.utils.AndroidUtils;
 import com.janrain.android.engage.utils.Archiver;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.XMLReader;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 public class FeedData {
     private static final String TAG = FeedData.class.getSimpleName();
     static {
         System.setProperty("http.keepAlive", "false");
     }
 
     private final Uri FEED_URL = Uri.parse("http://www.janrain.com/feed/blogs");
     SimpleDateFormat FEED_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-ddzzzhh:mm:ss'-:00'");
     SimpleDateFormat MORE_READABLE_DATE_FORMAT = new SimpleDateFormat("EEEE, MMMM d, yyyy h:mm aa");
 
     private static final String ARCHIVE_STORIES_ARRAY = "storiesArray";
     private static final String ARCHIVE_STORY_LINKS_HASH = "storyLinksHash";
 
     private static String ENGAGE_APP_ID = QuickShareEnvironment.getAppId();
     private static String ENGAGE_TOKEN_URL = QuickShareEnvironment.getTokenUrl();
 
     private static FeedData sInstance;
 
     private HashSet<String> mStoryLinks;
     final private ArrayList<Story> mStories = new ArrayList<Story>();
 
     private JREngage mEngage;
 
     private ImageLoader mImageLoader;
 
     public static FeedData getInstance(Activity activity) {
         if (sInstance != null) {
             if (Config.LOGD) Log.d(TAG, "[getInstance] returning existing instance");
 
             return sInstance;
         }
 
         sInstance = new FeedData(activity);
 
         if (Config.LOGD) Log.d(TAG, "[getInstance] returning new instance.");
 
         return sInstance;
     }
 
     public static FeedData getInstance() {
         return sInstance;
     }
 
     public JREngage getJREngage() {
         return mEngage;
     }
 
     public void setFeedReaderListener(FeedReaderListener feedReaderListener) {
         mListener = feedReaderListener;
     }
 
     private static class JRFileResponseCache extends FileResponseCache {
         private Context mContext;
         public JRFileResponseCache(Context c) {
             mContext = c;
         }
 
         @Override
         protected File getFile(URI uri, String s, Map<String, List<String>> stringListMap, Object o) {
             try {
                 File parent = new File(mContext.getCacheDir(), "filecache");
                 MessageDigest digest = MessageDigest.getInstance("MD5");
                 digest.update(String.valueOf(uri).getBytes("UTF-8"));
                 byte[] output = digest.digest();
                 StringBuilder builder = new StringBuilder("jr_cache_");
                 for (byte anOutput : output) builder.append(Integer.toHexString(0xFF & anOutput));
                 builder.append(AndroidUtils.urlEncode(uri.toString()));
                 return new File(parent, builder.toString());
             } catch (NoSuchAlgorithmException e) {
                 throw new RuntimeException(e);
             } catch (UnsupportedEncodingException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     private FeedData(final Activity activity) {
         if (Config.LOGD) Log.d(TAG, "[ctor] creating instance");
 
         FileResponseCache frc = new JRFileResponseCache(activity);
         JRFileResponseCache.setDefault(frc);
         java.net.ContentHandler bmch = JRFileResponseCache.capture(new BitmapContentHandler(), null);
         java.net.ContentHandler pfch = JRFileResponseCache.capture(JRFileResponseCache.sink(), null);
         mImageLoader = new ImageLoader(ImageLoader.DEFAULT_TASK_LIMIT, null, bmch, pfch,
                 ImageLoader.DEFAULT_CACHE_SIZE, null);
 
         initJREngage(activity);
 
         /* If the Story class changes, then the Archiver can't load the new stories, which is fine,
             They'll just get re-downloaded/added, but we also have to clear the links hash, so that
             the new stories get added. */
         try {
             ArrayList<Story> loadedStories = Archiver.load(ARCHIVE_STORIES_ARRAY);
             mStories.clear();
             mStories.addAll(loadedStories);
             mStoryLinks = Archiver.load(ARCHIVE_STORY_LINKS_HASH);
             logd(TAG, "[ctor] loaded " + ((Integer) mStories.size()).toString() + " stories from disk");
         } catch (Archiver.LoadException e) {
             mStories.clear();
             mStoryLinks = new HashSet<String>();
             logd(TAG, "[ctor] stories reset");
         }
     }
 
     public void initJREngage(Activity activity) {
         mEngage = JREngage.initInstance(activity, ENGAGE_APP_ID, ENGAGE_TOKEN_URL, mJrEngageDelegate);
     }
 
     private void logd(String function, String message) {
         if (Config.LOGD && function != null && message != null) Log.d(TAG, "[" + function + "] " + message);
     }
 
     private void logd(String function, String message, Exception e) {
         if (Config.LOGD && function != null && message != null) {
             Log.d(TAG, "[" + function + "] " + message, e);
         }
     }
 
     public ImageLoader getImageLoader() {
         return mImageLoader;
     }
 
     public interface FeedReaderListener {
         void asyncFeedReadSucceeded();
 
         void asyncFeedReadFailed(Exception e);
     }
 
     FeedReaderListener mListener;
 
     public void loadJanrainBlog() {
         JRConnectionManager.createConnection(FEED_URL.toString(),
                 new JRConnectionManagerDelegate.SimpleJRConnectionManagerDelegate() {
                     @Override
                     public void connectionDidFinishLoading(HttpResponseHeaders headers,
                                                            byte[] payload,
                                                            String requestUrl,
                                                            Object tag) {
                         processBlogLoad(payload);
                     }
 
                     @Override
                     public void connectionDidFail(Exception ex, String requestUrl, Object tag) {
                         mListener.asyncFeedReadFailed(ex);
                     }
                 }, null);
     }
 
     @SuppressWarnings("unchecked")
     private void processBlogLoad(final byte[] payload) {
         new AsyncTask<Void, Void, Pair<Boolean, Exception>>() {
             protected Pair<Boolean, Exception> doInBackground(Void... v) {
                 logd("loadJanrainBlog", "loading blog");
 
                 final Story currentStory = new Story();
                 RootElement root = new RootElement("rss");
                 Element channel = root.getChild("channel");
                 Element item = channel.getChild("item");
                 item.setEndElementListener(new EndElementListener(){
                     public void end() {
                         if (mStoryLinks.contains(currentStory.getLink())) return;
 
                         if (Config.LOGD) Log.d(TAG, "[addStoryOnlyIfNew] story hasn't been added");
 
                         synchronized (mStories) {
                             mStories.add(currentStory.copy());
                             Collections.sort(mStories);
                             mStoryLinks.add(currentStory.getLink());
                         }
                     }
                 });
                 item.getChild("title").setEndTextElementListener(new EndTextElementListener() {
                     public void end(String body) {
                         currentStory.setTitle(body);
                     }
                 });
                 item.getChild("link").setEndTextElementListener(new EndTextElementListener(){
                     public void end(String body) {
                         currentStory.setLink(body);
                     }
                 });
                 item.getChild("description").setEndTextElementListener(new EndTextElementListener(){
                     public void end(String body) {
                         currentStory.setDescription(body);
 
                         final ArrayList<String> imageUrls = new ArrayList<String>();
                         /* The description is in HTML, so we decode it to display it as plain text,
                             and while decoding it we yoink out a link to an image if there is one. */
                         String plainText = Html.fromHtml(body, new Html.ImageGetter() {
                             public Drawable getDrawable(String s) {
                                 imageUrls.add(FEED_URL.getScheme() + "://" + FEED_URL.getHost() + s);
                                 return null;
                             }
                         }, new Html.TagHandler() {
                             public void handleTag(boolean opening, String tag, Editable output,
                                                   XMLReader xmlReader) {
                                 if (tag.equalsIgnoreCase("style")) output.clear();
                             }
                         }).toString();
 
                         /* Remove Unicode object characters */
                         plainText = plainText.replaceAll("\ufffc", "");
                         currentStory.setPlainText(plainText);
                         currentStory.setImageUrls(imageUrls);
                     }
                 });
                 item.getChild("pubDate").setEndTextElementListener(new EndTextElementListener(){
                     public void end(String body) {
                         // Parse the blog dates, and then reformat them to be more readable.
                         // Example: 2011-04-25PDT04:30:00-:00
                         try {
                             Date parsedDate = FEED_DATE_FORMAT.parse(body);
                             currentStory.setRawDate(parsedDate);
                             body = MORE_READABLE_DATE_FORMAT.format(parsedDate);
                         } catch (ParseException e) {
                             // do nothing to the formatted date, leaving it as it was
                             currentStory.setRawDate(new Date());
                         }
 
                         currentStory.setFormattedDate(body);
                     }
                 });
                item.getChild("dc:creator").setEndTextElementListener(new EndTextElementListener() {
                     public void end(String body) {
                         currentStory.setCreator(body);
                     }
                 });
 
                 try {
                     Xml.parse(new ByteArrayInputStream(payload),
                             Xml.Encoding.UTF_8,
                             root.getContentHandler());
                 } catch (SAXException e) {
                     return new Pair<Boolean, Exception>(false, e);
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
 
                 logd("loadJanrainBlog", "saving stories");
                 Archiver.save(ARCHIVE_STORIES_ARRAY, mStories);
                 Archiver.save(ARCHIVE_STORY_LINKS_HASH, mStoryLinks);
                 logd("loadJanrainBlog", "stories saved");
 
                 return new Pair<Boolean, Exception>(true, null);
             }
 
             @Override
             protected void onPostExecute(Pair<Boolean, Exception> loadSuccess) {
                logd("onPostExecute", "blog loader onPostExecute, result: " +
                        (loadSuccess.first ? "succeeded" : "failed"));
 
                 if (loadSuccess.first) {
                     mListener.asyncFeedReadSucceeded();
                 } else {
                     mListener.asyncFeedReadFailed(loadSuccess.second);
                 }
             }
         }.execute();
     }
 
     public ArrayList<Story> getFeed() {
         /* Returning a copy of the mStories array list so that adding stories while the list
             is being used as the list adapter for the Feed Summary activity doesn't crash the app. */
         synchronized (mStories) {
             return new ArrayList<Story>(mStories);
         }
     }
 
     public void deleteAllStories() {
         synchronized (mStories) {
             mStories.clear();
             mStoryLinks.clear();
         }
 
         if (Config.LOGD) {
             Log.d(TAG, "[deleteAllStories] " + ((Integer) mStories.size()).toString() + " stories remain");
         }
 
         Archiver.save(ARCHIVE_STORIES_ARRAY, mStories);
         Archiver.save(ARCHIVE_STORY_LINKS_HASH, mStoryLinks);
     }
 
     JREngageDelegate mJrEngageDelegate = new JREngageDelegate() {
         public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
             String toastText;
             if (error.hasException()) {
                 //noinspection ThrowableResultOfMethodCallIgnored
                 toastText = error.getException().toString();
             } else {
                 toastText = error.getMessage();
             }
 
             Toast.makeText(JREngage.getActivity(), toastText, Toast.LENGTH_LONG).show();
         }
 
         public void jrAuthenticationDidNotComplete() {
         }
 
         public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
         }
 
         public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
         }
 
         public void jrAuthenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, String tokenUrlPayload, String provider) {
         }
 
         public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {
         }
 
         public void jrSocialDidNotCompletePublishing() {
         }
 
         public void jrSocialDidCompletePublishing() {
         }
 
         public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
         }
 
         public void jrSocialPublishJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {
         }
     };
 }
