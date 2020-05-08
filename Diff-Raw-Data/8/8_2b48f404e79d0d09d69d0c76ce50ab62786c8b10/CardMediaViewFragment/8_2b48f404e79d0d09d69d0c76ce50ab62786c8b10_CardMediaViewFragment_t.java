 package edu.mit.mobile.android.livingpostcards;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.view.LayoutInflater;
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
 import edu.mit.mobile.android.livingpostcards.data.CardMedia;
 
 public class CardMediaViewFragment extends Fragment implements LoaderCallbacks<Cursor>,
         OnItemSelectedListener, OnImageLoadListener {
     public static final String ARGUMENT_URI = "uri";
 
     private static final String TAG = CardMediaViewFragment.class.getSimpleName();
 
     private Uri mUri;
 
     private SimpleThumbnailCursorAdapter mAdapter;
     private Gallery mGallery;
     private ImageView mFrame;
 
     private ImageCache mImageCache;
 
     public static CardMediaViewFragment newInstance(Uri cardMedia) {
         final CardMediaViewFragment cmf = new CardMediaViewFragment();
 
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
 
         final View v = inflater.inflate(R.layout.card_media_view_fragment, container, false);
 
         mAdapter = new SimpleThumbnailCursorAdapter(getActivity(),
                 R.layout.card_media_thumb_square, null, new String[] { CardMedia.MEDIA_LOCAL_URL },
                 new int[] { R.id.card_media_thumbnail }, new int[] { R.id.card_media_thumbnail }, 0);
 
         mGallery = (Gallery) v.findViewById(R.id.gallery);
 
         mGallery.setAdapter(new ImageLoaderAdapter(mAdapter, mImageCache,
                 new int[] { R.id.card_media_thumbnail }, 100, 100));
 
         mGallery.setWrap(true);
         mGallery.setInfiniteScroll(true);
 
         mGallery.setOnItemSelectedListener(this);
 
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
                 CardMedia.MEDIA_LOCAL_URL }, null, null, null);
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
             mImageCache.scheduleLoadImage(mImageCache.getNewID(),
                     Uri.parse(item.getString(item.getColumnIndex(CardMedia.MEDIA_LOCAL_URL))), 640,
                     480);
         }
     }
 
     @Override
     public void onNothingSelected(AdapterView<?> adapter) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void onImageLoaded(long id, Uri imageUri, Drawable image) {
        image.setAlpha(255);
         mFrame.setImageDrawable(image);
     }
 }
