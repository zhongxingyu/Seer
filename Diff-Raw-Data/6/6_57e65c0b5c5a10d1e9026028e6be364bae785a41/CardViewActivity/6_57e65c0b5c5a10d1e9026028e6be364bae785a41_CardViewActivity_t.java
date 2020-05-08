 package edu.mit.mobile.android.livingpostcards;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.app.NavUtils;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.AnimationUtils;
 import android.widget.Toast;
 
 import com.actionbarsherlock.ActionBarSherlock;
 import com.actionbarsherlock.ActionBarSherlock.OnCreateOptionsMenuListener;
 import com.actionbarsherlock.ActionBarSherlock.OnOptionsItemSelectedListener;
 import com.actionbarsherlock.ActionBarSherlock.OnPrepareOptionsMenuListener;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
 import edu.mit.mobile.android.livingpostcards.data.Card;
 import edu.mit.mobile.android.locast.app.LocastApplication;
 import edu.mit.mobile.android.locast.data.PrivatelyAuthorable;
 import edu.mit.mobile.android.locast.net.NetworkClient;
 
 public class CardViewActivity extends FragmentActivity implements OnCreateOptionsMenuListener,
         OnOptionsItemSelectedListener, LoaderCallbacks<Cursor>, OnPrepareOptionsMenuListener,
         OnClickListener {
 
     private static final String[] CARD_PROJECTION = new String[] { Card._ID, Card.COL_TITLE,
             Card.COL_WEB_URL, Card.COL_AUTHOR_URI, Card.COL_PRIVACY, Card.COL_VIDEO_RENDER,
             Card.COL_DELETED, Card.COL_AUTHOR };
     private static final String TAG = CardViewActivity.class.getSimpleName();
     private static final int REQUEST_DELETE = 100;
     private Uri mCard;
     private Fragment mCardViewFragment;
 
     private final ActionBarSherlock mSherlock = ActionBarSherlock.wrap(this);
     private String mUserUri;
     private boolean mIsEditable;
     private String mWebUrl = null;
     private CardDetailsFragment mCardDetailsFragment;
     private boolean mHasVideo;
 
     private static final int MSG_LOAD_CARD_MEDIA = 100;
 
     private static class CardViewHandler extends Handler {
 
         private final CardViewActivity mActivity;
 
         public CardViewHandler(CardViewActivity activity) {
             mActivity = activity;
         }
 
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case MSG_LOAD_CARD_MEDIA:
                     mActivity.loadCardMedia(mActivity.mHasVideo);
                     break;
             }
         };
     }
 
     private final CardViewHandler mHandler = new CardViewHandler(this);
     private boolean mIsOwner;
 
     @Override
     protected void onCreate(Bundle arg0) {
 
         super.onCreate(arg0);
         mSherlock.setContentView(R.layout.activity_card_view);
 
         final ActionBar ab = mSherlock.getActionBar();
 
         ab.setHomeButtonEnabled(true);
         ab.setDisplayHomeAsUpEnabled(true);
 
         mCard = getIntent().getData();
 
         final View addFrame = findViewById(R.id.add_frame);
 
         addFrame.setOnClickListener(this);
 
         // so we can show it later
         addFrame.setVisibility(View.GONE);
 
         mUserUri = Authenticator.getUserUri(this, Authenticator.ACCOUNT_TYPE);
 
         getSupportLoaderManager().initLoader(0, null, this);
 
         loadCardDetails();
     }
 
    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(MSG_LOAD_CARD_MEDIA);
    };

     private void loadCardDetails() {
 
         final FragmentManager fm = getSupportFragmentManager();
 
         final FragmentTransaction ft = fm.beginTransaction();
 
         final Fragment details = fm.findFragmentById(R.id.card_details_fragment);
         if (details != null) {
             mCardDetailsFragment = (CardDetailsFragment) details;
         } else {
             mCardDetailsFragment = CardDetailsFragment.newInstance(mCard);
 
             ft.replace(R.id.card_details_fragment, mCardDetailsFragment);
         }
 
         ft.commit();
     }
 
     private void loadCardMedia(boolean useVideo) {
 
         final FragmentManager fm = getSupportFragmentManager();
 
         final FragmentTransaction ft = fm.beginTransaction();
 
         final Fragment cardView = fm.findFragmentById(R.id.card_view_fragment);
 
         if (useVideo) {
             if (cardView != null && cardView instanceof CardViewVideoFragment) {
                 mCardViewFragment = cardView;
             } else {
                 mCardViewFragment = CardViewVideoFragment.newInstance(mCard);
                 ft.replace(R.id.card_view_fragment, mCardViewFragment);
             }
         } else {
 
             if (cardView != null && cardView instanceof CardViewFragment) {
                 mCardViewFragment = cardView;
             } else {
                 mCardViewFragment = CardViewFragment.newInstance(mCard);
                 ft.replace(R.id.card_view_fragment, mCardViewFragment);
             }
         }
 
         ft.commit();
 
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         startService(new Intent(Intent.ACTION_SYNC, mCard));
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.share:
                 send();
                 return true;
 
             case android.R.id.home: {
                 NavUtils.navigateUpFromSameTask(this);
             }
                 return true;
 
             case R.id.edit:
                 startActivity(new Intent(Intent.ACTION_EDIT, mCard));
                 return true;
 
             case R.id.delete:
                 startActivityForResult(new Intent(Intent.ACTION_DELETE, mCard), REQUEST_DELETE);
                 return true;
 
             default:
                 return false;
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case REQUEST_DELETE:
                 if (resultCode == RESULT_OK) {
                     finish();
                 }
                 break;
         }
 
     }
 
     private void send() {
         if (mWebUrl == null) {
             Toast.makeText(this, R.string.err_share_intent_no_web_url_editable, Toast.LENGTH_LONG)
                     .show();
             return;
         }
         startActivity(Card.createShareIntent(this, mWebUrl, getTitle()));
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         mSherlock.getMenuInflater().inflate(R.menu.activity_card_view, menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(android.view.Menu menu) {
         return mSherlock.dispatchPrepareOptionsMenu(menu);
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         menu.findItem(R.id.edit).setVisible(mIsOwner);
         menu.findItem(R.id.delete).setVisible(mIsOwner);
         // hide the share button if there's nothing the user can do to share it.
         // isEditable is allowed here so that the error message is displayed in order to explain
         // that postcards need to be published before sharing.
         menu.findItem(R.id.share).setVisible(mWebUrl != null || mIsEditable);
 
         return true;
     }
 
     @Override
     public boolean onCreateOptionsMenu(android.view.Menu menu) {
         return mSherlock.dispatchCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(android.view.MenuItem item) {
         return mSherlock.dispatchOptionsItemSelected(item);
     }
 
     @Override
     protected void onTitleChanged(CharSequence title, int color) {
         mSherlock.dispatchTitleChanged(title, color);
         super.onTitleChanged(title, color);
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
         return new CursorLoader(this, mCard, CARD_PROJECTION, null, null, null);
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
         if (c.moveToFirst()) {
 
             // don't show deleted cards. This just confuses things.
             if (Card.isDeleted(c)) {
                 finish();
                 return;
             }
             final int videoCol = c.getColumnIndexOrThrow(Card.COL_VIDEO_RENDER);
 
             // if the card has a video render, show that
             mHasVideo = !c.isNull(videoCol) && c.getString(videoCol).length() > 0;
 
             mHandler.sendEmptyMessage(MSG_LOAD_CARD_MEDIA);
 
             setTitle(Card.getTitle(this, c));
             mSherlock.getActionBar().setSubtitle(
                     c.getString(c.getColumnIndexOrThrow(Card.COL_AUTHOR)));
 
             mIsEditable = PrivatelyAuthorable.canEdit(mUserUri, c);
             mIsOwner = mUserUri.equals(c.getString(c.getColumnIndexOrThrow(Card.COL_AUTHOR_URI)));
 
             final View addPicture = findViewById(R.id.add_frame);
 
             if (mIsEditable) {
                 addPicture.setVisibility(View.VISIBLE);
                 addPicture.startAnimation(AnimationUtils.makeInChildBottomAnimation(this));
             } else {
                 addPicture.setVisibility(View.GONE);
             }
 
             mWebUrl = c.getString(c.getColumnIndexOrThrow(Card.COL_WEB_URL));
             // resolve to a full URL
             final NetworkClient nc = LocastApplication.getNetworkClient(this,
                     Authenticator.getFirstAccount(this));
             mWebUrl = mWebUrl != null ? nc.getFullUrlAsString(mWebUrl) : null;
             try {
                 mSherlock.dispatchInvalidateOptionsMenu();
             } catch (final OutOfMemoryError oom) {
                 // well, damn.
             }
         } else {
             finish();
         }
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> arg0) {
 
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.add_frame:
                 startActivity(new Intent(CameraActivity.ACTION_ADD_PHOTO, mCard));
                 break;
 
             default:
                 break;
         }
     }
 }
