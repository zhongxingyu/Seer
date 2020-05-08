 package edu.mit.mobile.android.livingpostcards;
 
 import android.app.Activity;
 import android.content.ContentUris;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 
 import com.scvngr.levelup.views.gallery.AdapterView;
 import com.scvngr.levelup.views.gallery.AdapterView.OnItemSelectedListener;
 import com.scvngr.levelup.views.gallery.Gallery;
 
 import edu.mit.mobile.android.imagecache.ImageCache;
 import edu.mit.mobile.android.imagecache.ImageCache.OnImageLoadListener;
 import edu.mit.mobile.android.imagecache.ImageLoaderAdapter;
 import edu.mit.mobile.android.imagecache.SimpleThumbnailCursorAdapter;
 import edu.mit.mobile.android.livingpostcards.DeleteDialogFragment.OnDeleteListener;
 import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
 import edu.mit.mobile.android.livingpostcards.data.CardMedia;
 import edu.mit.mobile.android.locast.data.Authorable;
 
 public class CardMediaEditFragment extends Fragment implements LoaderCallbacks<Cursor>,
         OnItemSelectedListener, OnImageLoadListener {
     public static final String ARGUMENT_URI = "uri";
 
     private static final String TAG = CardMediaEditFragment.class.getSimpleName();
 
     private Uri mUri;
 
     private SimpleThumbnailCursorAdapter mAdapter;
     private Gallery mGallery;
     private ImageView mFrame;
 
     private ImageCache mImageCache;
 
     public static CardMediaEditFragment newInstance(Uri cardMedia) {
         final CardMediaEditFragment cmf = new CardMediaEditFragment();
 
         final Bundle args = new Bundle();
         args.putParcelable(ARGUMENT_URI, cardMedia);
         cmf.setArguments(args);
         return cmf;
     }
 
     @Override
     public void onAttach(Activity activity) {
 
         super.onAttach(activity);
 
         mImageCache = ImageCache.getInstance(activity);
 
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
 
         if (getArguments() != null) {
             mUri = getArguments().getParcelable(ARGUMENT_URI);
 
             if (mUri != null) {
                 getLoaderManager().initLoader(0, null, this);
             }
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
         final View v = inflater.inflate(R.layout.card_media_edit_fragment, container, false);
 
         mAdapter = new SimpleThumbnailCursorAdapter(getActivity(),
                 R.layout.card_media_thumb_square, null, new String[] { CardMedia.COL_LOCAL_URL,
                         CardMedia.COL_MEDIA_URL }, new int[] { R.id.card_media_thumbnail,
                         R.id.card_media_thumbnail }, new int[] { R.id.card_media_thumbnail }, 0);
 
         mGallery = (Gallery) v.findViewById(R.id.gallery);
 
         mGallery.setAdapter(new ImageLoaderAdapter(mAdapter, mImageCache,
                 new int[] { R.id.card_media_thumbnail }, 100, 100));
 
         mGallery.setWrap(true);
         mGallery.setInfiniteScroll(true);
 
         mGallery.setOnItemSelectedListener(this);
 
         registerForContextMenu(mGallery);
 
         mFrame = (ImageView) v.findViewById(R.id.frame);
 
         return v;
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mGallery.pause();
 
         mImageCache.unregisterOnImageLoadListener(this);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         mImageCache.registerOnImageLoadListener(this);
 
         mGallery.resume();
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 
         return new CursorLoader(getActivity(), mUri, new String[] { CardMedia._ID,
                 CardMedia.COL_LOCAL_URL, CardMedia.COL_MEDIA_URL, CardMedia.COL_AUTHOR_URI }, null,
                 null, null);
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
         mAdapter.swapCursor(c);
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         mAdapter.swapCursor(null);
     }
 
     @Override
     public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
         final Cursor item = (Cursor) mAdapter.getItem(position);
         if (item != null) {
             String uri = item.getString(item.getColumnIndex(CardMedia.COL_LOCAL_URL));
             if (uri == null) {
                 uri = item.getString(item.getColumnIndex(CardMedia.COL_MEDIA_URL));
             }
             if (uri == null) {
                 return;
             }
             mImageCache.scheduleLoadImage(mImageCache.getNewID(), Uri.parse(uri), 320, 240);
         }
     }
 
     @Override
     public void onNothingSelected(AdapterView<?> adapter) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         switch (v.getId()) {
             case R.id.gallery: {
                 getActivity().getMenuInflater().inflate(R.menu.context_card_media, menu);
                 final Cursor c = mAdapter.getCursor();
                 if (c == null) {
                     return;
                 }
                 AdapterView.AdapterContextMenuInfo info;
                 try {
                     info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                 } catch (final ClassCastException e) {
                     Log.e(TAG, "bad menuInfo", e);
                     return;
                 }

                // the below is a special case due to a bug in the infinite gallery spinner.

                c.moveToPosition(info.position % mAdapter.getCount());
 
                 final String myUserUri = Authenticator.getUserUri(getActivity());
 
                 final boolean isEditable = Authorable.canEdit(myUserUri, c);
 
                 menu.findItem(R.id.delete).setVisible(isEditable);
 
             }
                 break;
             default:
                 super.onCreateContextMenu(menu, v, menuInfo);
         }
 
     }
 
     private void showDeleteDialog(Uri item) {
         final DeleteDialogFragment del = DeleteDialogFragment.newInstance(item, getActivity()
                 .getString(R.string.delete_postcard_image),
                 getString(R.string.delete_postcard_image_confirm_message));
         del.registerOnDeleteListener(mOnDeleteListener);
         del.show(getFragmentManager(), "delete-item-dialog");
     }
 
     private final OnDeleteListener mOnDeleteListener = new OnDeleteListener() {
 
         @Override
         public void onDelete(Uri item, boolean deleted) {
 
         }
     };
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info;
         try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         } catch (final ClassCastException e) {
             Log.e(TAG, "bad menuInfo", e);
             return false;
         }
 
         final Uri itemUri = ContentUris.withAppendedId(mUri, info.id);
 
         switch (item.getItemId()) {
             case R.id.delete:
                 showDeleteDialog(itemUri);
                 return true;
             default:
                 return super.onContextItemSelected(item);
         }
 
     }
 
     @Override
     public void onImageLoaded(long id, Uri imageUri, Drawable image) {
         image.setAlpha(255);
         mFrame.setImageDrawable(image);
     }
 
     public void setAnimationTiming(int timing) {
         mGallery.setInterframeDelay(timing);
     }
 
     public int getAnimationTiming() {
         return (int) mGallery.getInterframeDelay();
     }
 }
