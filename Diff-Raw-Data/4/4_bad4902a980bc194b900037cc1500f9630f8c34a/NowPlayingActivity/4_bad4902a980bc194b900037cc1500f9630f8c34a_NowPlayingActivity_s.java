 /* Copyright (C) 2013 David Li <li.davidm96@gmail.com>
 
    This file is part of Fiction Music.
 
    Fiction Music is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version.
 
    Fiction Music is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.
 
    You should have received a copy of the GNU General Public License along with
    Fiction Music.  If not, see <http://www.gnu.org/licenses/>. */
 
 
 package com.lithiumli.fiction;
 
 import android.animation.AnimatorSet;
 import android.animation.ObjectAnimator;
 import android.app.Activity;
 import android.app.ActivityOptions;
 import android.app.ActionBar;
 import android.app.Fragment;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.PorterDuff;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.Looper;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.util.Log;
 
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 
 import com.lithiumli.fiction.ui.AlbumSwiper;
 import com.lithiumli.fiction.util.BitmapLruCache;
 
 public class NowPlayingActivity
     extends FictionActivity
     implements ArtistImageCache.CacheCallback
 {
     static final String ECHO_NEST_URL = "http://developer.echonest.com/api/v4/artist/images?api_key=ETDSSZR6RAMYOU4SI&results=1&name=";
     TextView mSongName;
     TextView mSongAlbum;
     TextView mSongArtist;
     AlbumSwiper mCoverPager;
     ArtistImageCache mCache;
     AnimatorSet mCurrentAnim;
     Looper mLooper;
     Handler mHandler;
     FadeOut mFadeOut;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.now_playing);
         initializeDrawer(false);
 
         mCoverPager = (AlbumSwiper) findViewById(R.id.cover_pager);
         mCoverPager.setListener(this);
 
         mSongName = (TextView) findViewById(R.id.np_song_name);
         mSongAlbum = (TextView) findViewById(R.id.np_song_album);
         mSongArtist = (TextView) findViewById(R.id.np_song_artist);
         mSongArtist.setSelected(true);
 
         ActionBar ab = getActionBar();
         ab.setDisplayHomeAsUpEnabled(true);
         ab.setTitle("Now Playing");
 
         mCache = ArtistImageCache.getInstance(this);
 
         setQueueMargin();
 
         HandlerThread thread = new HandlerThread("thread");
         thread.start();
 
         mLooper = thread.getLooper();
         mHandler = new Handler(mLooper);
 
         mFadeOut = new FadeOut();
         mHandler.postDelayed(mFadeOut, 2000);
     }
 
     // @Override
     // public void onPause() {
     //     super.onPause();
     //     mLooper.quit();
     // }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.now_playing, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         boolean drawerOpen = isDrawerOpen();
         menu.findItem(R.id.select_artist_image).setVisible(!drawerOpen);
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
             Intent parentIntent = new Intent(this, LibraryActivity.class);
             parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             ActivityOptions options =
                 ActivityOptions.makeCustomAnimation(this,
                                                     R.anim.activity_slide_down,
                                                     R.anim.activity_slide_up);
             startActivity(parentIntent, options.toBundle());
             finish();
             return true;
         case R.id.select_artist_image:
             selectArtistImage();
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onBackPressed() {
         super.onBackPressed();
         overridePendingTransition(R.anim.activity_slide_down, R.anim.activity_slide_up);
     }
 
     @Override
     public void onServiceConnected(PlaybackService service) {
         mCoverPager.setQueue(service.getQueue());
         mCoverPager.updateCovers();
     }
 
     @Override
     public void onSongChange(Song song) {
         mSongName.setText(song.getTitle());
         mSongAlbum.setText(song.getAlbum());
         mSongArtist.setText(song.getArtist());
 
         mCoverPager.updateCovers();
         mCoverPager.postInvalidate();
 
         if (song.getArtist().equals("<unknown>")) {
             ((ImageView) findViewById(R.id.background_image)).setImageDrawable(new ColorDrawable(0xFF000000));
             return;
         };
 
         mCache.cancelAll();
         android.util.Log.d("fiction", "getting " + song.getArtist());
         mCache.getImage(song.getArtist(), this);
     }
 
     @Override
     public void onPlayStateChange(PlaybackService.PlayState state) {
         ImageButton button = (ImageButton) findViewById(R.id.np_play_pause);
 
         switch (state) {
         case PLAYING:
             button.setImageResource(R.drawable.ic_menu_pause);
             break;
         case STOPPED:
             finish();
         case PAUSED:
             button.setImageResource(R.drawable.ic_menu_play);
             break;
         default:
             break;
         }
     }
 
     @Override
     public boolean dispatchTouchEvent(MotionEvent ev) {
        mCurrentAnim.end();
 
         mSongName.setAlpha(1.0f);
         mSongAlbum.setAlpha(1.0f);
         mSongArtist.setAlpha(1.0f);
         mCoverPager.setAlpha(1.0f);
 
         if (mFadeOut != null) {
             mHandler.removeCallbacks(mFadeOut);
         }
         mFadeOut = new FadeOut();
         mHandler.postDelayed(mFadeOut, 2000);
 
         return super.dispatchTouchEvent(ev);
     }
 
     public void shuffleButton(View view) {
         ImageButton button = (ImageButton) view;
 
         if (isServiceBound()) {
             PlaybackService service = getService();
             PlaybackQueue queue = service.getQueue();
 
             if (queue.isShuffling()) {
                 button.setColorFilter(0xFFFFFFFF);
                 queue.restoreShuffle();
             }
             else {
                 button.setColorFilter(0xFF0099CC);
                 queue.shuffle();
             }
 
             mCoverPager.updateCovers();
             mCoverPager.postInvalidate();
         }
     }
 
     public void onImageFound(BitmapDrawable bitmap) {
         ImageView view = (ImageView) findViewById(R.id.background_image);
         if (bitmap != null) {
             view.setImageDrawable(bitmap);
         }
         else {
             view.setImageDrawable(new ColorDrawable(0xFF000000));
         }
     }
 
     private static final int SELECT_PHOTO = 42;
     // TODO XXX ugh (causes NPE trying to getService())
     private String editingArtist;
     public void selectArtistImage() {
         Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
         photoPickerIntent.setType("image/*");
         editingArtist = getService().getQueue().getCurrent().getArtist();
         startActivityForResult(photoPickerIntent, SELECT_PHOTO);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode,
                                     Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
 
         switch(requestCode) {
         case SELECT_PHOTO:
             if (resultCode == RESULT_OK) {
                 // TODO move to background thread
                 android.util.Log.d("fiction", editingArtist);
                 String key = mCache.getCacheKey(mCache.escapeArtist(editingArtist));
                 Uri image = intent.getData();
 
                 try {
                     InputStream imageStream = getContentResolver()
                         .openInputStream(image);
                     Bitmap b = BitmapFactory.decodeStream(imageStream);
 
                     mCache.storeImage(key, b);
                     mCache.getImage(editingArtist, this);
                 }
                 catch (FileNotFoundException e) {
                 }
             }
         }
     }
 
     class FadeOut implements Runnable {
         @Override
         public void run() {
             runOnUiThread(new FadeOutInner());
         }
 
         class FadeOutInner implements Runnable {
             @Override
             public void run() {
                 mCurrentAnim = new AnimatorSet();
                 ObjectAnimator a1 = ObjectAnimator.ofFloat(mSongName, "alpha", 1.0f, 0.0f);
                 ObjectAnimator a2 = ObjectAnimator.ofFloat(mSongAlbum, "alpha", 1.0f, 0.0f);
                 ObjectAnimator a3 = ObjectAnimator.ofFloat(mSongArtist, "alpha", 1.0f, 0.0f);
                 ObjectAnimator a4 = ObjectAnimator.ofFloat(mCoverPager, "alpha", 1.0f, 0.5f);
                 a1.setDuration(300);
                 a2.setDuration(400);
                 a3.setDuration(500);
                 a4.setDuration(700);
 
                 mCurrentAnim.play(a1);
                 mCurrentAnim.play(a2).after(100);
                 mCurrentAnim.play(a3).after(200);
                 mCurrentAnim.play(a4);
                 mCurrentAnim.start();
             }
         }
     }
 }
