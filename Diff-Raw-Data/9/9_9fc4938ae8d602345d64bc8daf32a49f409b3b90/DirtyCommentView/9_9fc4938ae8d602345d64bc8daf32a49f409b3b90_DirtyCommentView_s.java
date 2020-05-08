 package com.shaubert.dirty;
 
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.drawable.StateListDrawable;
 import android.graphics.drawable.TransitionDrawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.AsyncTask.Status;
 import android.preference.PreferenceManager;
 import android.provider.Browser;
 import android.support.v4.util.LruCache;
 import android.text.Html;
 import android.text.Spanned;
 import android.text.TextUtils;
 import android.view.*;
 import android.widget.Checkable;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import com.shaubert.dirty.client.DirtyRecord.Image;
 import com.shaubert.dirty.db.CommentsCursor;
 import com.shaubert.dirty.db.DirtyContract.DirtyCommentEntity;
 import com.shaubert.dirty.net.DataLoadRequest;
 import com.shaubert.util.*;
 import com.shaubert.util.Bitmaps.Size;
 
 import java.io.File;
 
 public class DirtyCommentView extends LinearLayout implements Checkable {
 
     private static final Shlog SHLOG = new Shlog(DirtyCommentView.class.getSimpleName());
     
     private static LruCache<Long, Spanned> spanCache = new LruCache<Long, Spanned>(100);
 
     private ViewGroup frameBody;
     private TextView message;
     private TextView gifDescription;
     private ImageView image;
     private TextView summary;
     
     private String imageUrl;
     private String videoUrl;
     
     private long commentServerId;
     
     private AsyncTask<Void, Void, Void> imageLoadTask;
     private AsyncTask<Void, Void, Void> spanTask;
     
     private SummaryFormatter summaryFormatter;
 
     private DirtyPreferences dirtyPreferences;
     private final LruCache<String,Bitmap> imageCache;
 
     public DirtyCommentView(Activity context) {
         super(context);
         setGravity(Gravity.RIGHT);
         setWeightSum(1f);
 
         this.imageCache = ((DirtyApp) context.getApplication()).getImageCache();
         this.dirtyPreferences = new DirtyPreferences(
                 PreferenceManager.getDefaultSharedPreferences(context), context);
         
         this.summaryFormatter = new SummaryFormatter(context);
         LayoutInflater inflater = LayoutInflater.from(context);
         inflater.inflate(R.layout.l_dirty_comment, this, true);
 
         frameBody = (ViewGroup) findViewById(R.id.frame_body);
         message = (TextView) findViewById(R.id.message);
         message.setMovementMethod(SelectableLinkMovementMethod.getInstance());
         message.setClickable(false);
         message.setLongClickable(false);
         gifDescription = (TextView) findViewById(R.id.gif_description);
         image = (ImageView) findViewById(R.id.image);
         summary = (TextView) findViewById(R.id.summary);
         summary.setMovementMethod(SelectableLinkMovementMethod.getInstance());
         summary.setClickable(false);
         summary.setLongClickable(false);
 
         image.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (!TextUtils.isEmpty(imageUrl)) {
                     Uri uri = Uri.parse(videoUrl != null ? videoUrl : imageUrl);
                     Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                     intent.putExtra(Browser.EXTRA_APPLICATION_ID, getContext().getPackageName());
                     getContext().startActivity(intent);
                 }
             }
         });
         
         setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
     }
     
     @Override
     public void setChecked(boolean checked) {
         setSelected(checked);
     }
     
     @Override
     public boolean isChecked() {
         return isSelected();
     }
     
     @Override
     public void toggle() {
         if (isChecked()) {
             setChecked(true);
         } else {
             setChecked(false);
         }
     }
     
     @Override
     public void setPressed(boolean pressed) {
         super.setPressed(pressed);
         StateListDrawable stateListDrawable = (StateListDrawable)frameBody.getBackground();
         if (stateListDrawable.getCurrent() instanceof TransitionDrawable) {
             TransitionDrawable transition = (TransitionDrawable)stateListDrawable.getCurrent();
             if (pressed) {
                 transition.startTransition(ViewConfiguration.getLongPressTimeout());
             } else {
                 transition.resetTransition();
             }
         }
     }
     
     public void swapData(CommentsCursor cursor) {
         cancelSpanCreation();
         cancelImageLoading();
         
         commentServerId = cursor.getServerId();
         
         summary.setText(summaryFormatter.formatSummaryText(cursor));
         summary.setTextSize(dirtyPreferences.getSummarySize());
         LinearLayout.LayoutParams params = (LayoutParams) frameBody.getLayoutParams();
         params.weight = Math.max(0.5f, 1 - cursor.getIndentLevel() / 50f);
         frameBody.setLayoutParams(params);
   
         if (frameBody.getChildCount() > 3) {
             frameBody.removeViewAt(3);
         }
         Spanned messageText = spanCache.get(commentServerId);
         if (messageText == null) {
             messageText = Html.fromHtml(cursor.getFormattedMessage());
             spanCache.put(commentServerId, messageText);
         }
         if (!TextUtils.isEmpty(messageText)) {
             message.setText(messageText);
             message.setVisibility(View.VISIBLE);
         } else {
             message.setText(null);
             message.setVisibility(View.GONE);
         }
         message.setTextSize(dirtyPreferences.getFontSize());
         
         gifDescription.setVisibility(GONE);
         Image[] images = cursor.getImages();
         if (images != null && images.length > 0) {
            image.setVisibility(View.VISIBLE);
             Image commentImage = images[0];
 
             if (commentImage.src.endsWith(".gif")) {
                 gifDescription.setVisibility(VISIBLE);
             }
             if (commentImage.widht != -1 && commentImage.height != -1) {
                 int max = Sizes.dpToPx(128, getContext());
                 Size size = Bitmaps.getScaledSize(max, max, commentImage.widht, commentImage.height);
                 image.getLayoutParams().width = size.width; 
                 image.getLayoutParams().height = size.height;
             } else {
                 image.getLayoutParams().width = 
                         image.getLayoutParams().height = Sizes.dpToPx(128, getContext());
             }
             if (image instanceof FixedSizeImageView) {
                 ((FixedSizeImageView) image).callRequestLayout();
             }
             
             imageUrl = commentImage.src;
             videoUrl = cursor.getVideoUrl();
             Bitmap imageBitmap = imageCache.get(imageUrl);
             if (imageBitmap != null) {
                 image.setImageBitmap(imageBitmap);
             } else {
                 image.setImageBitmap(null);
                 startImageLoading(imageUrl);
             }
         } else {
             image.setImageBitmap(null);
            image.setVisibility(View.GONE);
             imageUrl = videoUrl = null;
         }
     }
 
     private void cancelSpanCreation() {
         if (spanTask != null && spanTask.getStatus() != Status.FINISHED) {
             spanTask.cancel(true);
         }
     }
 
     private void cancelImageLoading() {
         if (imageLoadTask != null && imageLoadTask.getStatus() != Status.FINISHED) {
             imageLoadTask.cancel(true);
         }
     }
     
     private void startImageLoading(final String url) {
         final long serverId = commentServerId;
         final int maxWidth = Sizes.dpToPx(256, message.getContext());
         imageLoadTask = new AsyncTask<Void, Void, Void>() {
 
             private Bitmap bitmap;
             private File cache;
             
             @Override
             protected Void doInBackground(Void... params) {
             	if (dirtyPreferences.shouldLoadImagesOnlyWithWiFi()) {
             		if (!Networks.hasWiFiConnection(getContext())) {
             			return null;
             		}
             	}
             	
                 cache = Files.getCommentImageCache(getContext(), commentServerId, url);
                 if (!decodeImage() && !isCancelled()) {
                     DataLoadRequest loadRequest = new DataLoadRequest(url, cache.getAbsolutePath());
                     try {
                         if (!isCancelled()) {
                             loadRequest.execute();
                         }
                         if (!isCancelled()) {
                             decodeImage();
                         }
                     } catch (Exception e) {
                         SHLOG.w(e);
                     }
                 }
                 return null;
             }
             
             private boolean decodeImage() {
                 if (cache.exists()) {
                     try {
                         SHLOG.d("decoding image " + cache.getAbsolutePath());
                         if (!isCancelled()) {
                             bitmap = Bitmaps.loadScaledImage(cache, maxWidth, maxWidth);
                         }
                         return true;
                     } catch (Exception ex) {
                         SHLOG.w(ex);
                     }
                 }
                 return false;
             }
             
             protected void onPostExecute(Void result) {
                 if (bitmap != null) {
                     Bitmap existing = imageCache.get(url);
                     if (existing == null) {
                         imageCache.put(url, bitmap);
                     } else {
                         bitmap.recycle();
                     }
                     if (serverId == DirtyCommentView.this.commentServerId) {
                         image.setImageBitmap(existing == null ? bitmap : existing);
                     }
                 } else {
                     if (serverId == DirtyCommentView.this.commentServerId) {
                         image.setImageResource(R.drawable.error);
                     }
                 }
             };
             
         };
         imageLoadTask.execute();
     }
     
     @SuppressWarnings("unused")
     private void startSpanCreation(CommentsCursor comment) {
         final long serverId = commentServerId;
         final long commentLocalId = comment.getId();
         final Context context = getContext();
         spanTask = new AsyncTask<Void, Void, Void>() {
 
             private Spanned spanned;
             
             @Override
             protected Void doInBackground(Void... params) {
                 Uri commentUri = ContentUris.withAppendedId(DirtyCommentEntity.URI, commentLocalId);
                 Cursor textCursor = context.getContentResolver().query(commentUri, 
                         new String[] { DirtyCommentEntity.FORMATTED_MESSAGE }, null, null, null);
                 try {
                     if (textCursor.moveToFirst()) {
                         String formattedText = textCursor.getString(0);
                         if (!isCancelled()) {
                             spanned = Html.fromHtml(formattedText);
                         }
                     }
                 } finally {
                     textCursor.close();
                 }
                 return null;
             }
             
             protected void onPostExecute(Void result) {
                 if (spanned != null && spanned.length() > 0) {
                     spanCache.put(serverId, spanned);
                     if (serverId == DirtyCommentView.this.commentServerId) {
                         SHLOG.resetTimer("setText " + commentServerId);
                         message.setText(spanned);
                         message.setVisibility(View.VISIBLE);
                         SHLOG.logTimer("setText " + commentServerId);
                     }
                 }
             };
             
         };
         spanTask.execute();
     }
     
 }
