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
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.graphics.Rect;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.*;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.*;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 import org.npr.android.util.DisplayUtils;
 import org.npr.android.util.PlaylistEntry;
 import org.npr.android.util.PlaylistProvider;
 import org.npr.android.util.PlaylistRepository;
 import org.npr.android.widget.DragListener;
 import org.npr.android.widget.DragNDropListView;
 import org.npr.android.widget.DropListener;
 
 public class PlaylistView extends FrameLayout implements OnClickListener,
     OnSeekBarChangeListener, OnDrawerOpenListener, OnDrawerCloseListener,
     AdapterView.OnItemClickListener, DragListener, DropListener {
 
   private static final String LOG_TAG = PlaylistView.class.getName();
 
   private final Context context;
   private PlaylistAdapter playlistAdapter;
   private PlaylistRepository playlist;
 
   private SlidingDrawer drawer;
   private RelativeLayout handle;
 
   private RelativeLayout playerContracted;
   private RelativeLayout playerExpanded;
 
   private TextView playerStatus;
   private TextView contractedPlayerStatus;
 
   private TextView newsItemText;
   private TextView contractedNewsItemText;
 
   private SeekBar progressBar;
   private boolean changingProgress;
   private TextView lengthText;
 
   private boolean playPauseShowsPlay;
   private ImageButton rewindButton;
   private ImageButton rewind30Button;
   private ImageButton playPauseButton;
   private ImageButton fastForwardButton;
   private ImageButton contractedPlayButton;
 
   private DragNDropListView listView;
   private Button clearPlayedSegments;
   private Button clearPlaylist;
 
   private boolean playEnabled;
   private boolean moveControlsEnabled;
   private boolean rewindEnabled;
   private boolean fastForwardEnabled;
 
   private int touchSlop;
   private int startX;
   private int startY;
   private boolean cancelDown;
 
 
   private enum ClickedItem {
     rewind, rewind30, playPause, fastForward, contractedPlay, progressbar
   }
 
   private ClickedItem clickedItem;
 
   private BroadcastReceiver changeReceiver;
   private BroadcastReceiver updateReceiver;
   private BroadcastReceiver closeReceiver;
   private BroadcastReceiver errorReceiver;
   private BroadcastReceiver playlistChangedReceiver;
 
   private GestureDetector gestureDetector;
   private final Handler handler = new Handler() {
     @Override
     public void handleMessage(final Message msg) {
       switch (msg.what) {
         case ListItemGestureListener.MSG_FLING:
 
           final PlaylistEntry entry = getPlaylistEntryAtPosition(msg.arg1);
           View listItem = listView.getChildAt(msg.arg1 -
               listView.getFirstVisiblePosition());
 
           Animation fling = AnimationUtils.loadAnimation(
               context,
               msg.arg2 < 0 ? R.anim.playlist_remove_left :
                   R.anim.playlist_remove_right
           );
           fling.setAnimationListener(new Animation.AnimationListener() {
             @Override
             public void onAnimationEnd(Animation animation) {
               playlist.delete(entry);
               updatePlaylist();
             }
 
             @Override
             public void onAnimationStart(Animation animation) {
             }
 
             @Override
             public void onAnimationRepeat(Animation animation) {
             }
           });
 
           listItem.startAnimation(fling);
           break;
       }
     }
   };
 
   @SuppressWarnings({"UnusedDeclaration"})
   public PlaylistView(Context context) {
     super(context);
     this.context = context;
   }
 
   @SuppressWarnings({"UnusedDeclaration"})
   public PlaylistView(Context context, AttributeSet attrs) {
     super(context, attrs);
     this.context = context;
   }
 
   @SuppressWarnings({"UnusedDeclaration"})
   public PlaylistView(Context context, AttributeSet attrs, int defStyle) {
     super(context, attrs, defStyle);
     this.context = context;
   }
 
   /**
    * Returns a pointer to the SlidingDrawer for the
    * player window.
    *
    * @return The player's SlidingDrawer
    */
   public SlidingDrawer getPlayerDrawer() {
     return drawer;
   }
 
   @Override
   protected void onAttachedToWindow() {
     super.onAttachedToWindow();
     init();
   }
 
   private void init() {
 
     ViewGroup.inflate(context, R.layout.playlist, this);
 
     playlist = new PlaylistRepository(context.getApplicationContext(),
         context.getContentResolver());
 
     drawer = (SlidingDrawer) findViewById(R.id.drawer);
     drawer.setOnDrawerOpenListener(this);
     drawer.setOnDrawerCloseListener(this);
     touchSlop = ViewConfiguration.getTouchSlop();
     handle = (RelativeLayout) findViewById(R.id.handle);
 
     playerContracted = (RelativeLayout) findViewById(R.id.player_contracted);
     playerExpanded = (RelativeLayout) findViewById(R.id.player_expanded);
 
     playerStatus = (TextView) findViewById(R.id.status);
     contractedPlayerStatus = (TextView) findViewById(R.id.contracted_status);
 
     newsItemText = (TextView) findViewById(R.id.news_item_text);
     contractedNewsItemText = (TextView) findViewById(R.id
         .contracted_news_item_text);
 
     progressBar = (SeekBar) findViewById(R.id.stream_progress_bar);
     progressBar.setOnSeekBarChangeListener(this);
     changingProgress = false;
 
     lengthText = (TextView) findViewById(R.id.stream_length_display_text);
     lengthText.setText("0:00 / 0:00");
 
     playPauseShowsPlay = true;
     contractedPlayButton = (ImageButton) findViewById(R.id
         .contracted_play_pause);
     rewindButton = (ImageButton) findViewById(R.id.stream_rewind_button);
     rewind30Button = (ImageButton) findViewById(R.id.stream_rewind_30_button);
     playPauseButton = (ImageButton) findViewById(R.id.stream_play_pause_button);
     fastForwardButton = (ImageButton) findViewById(R.id
         .stream_fastforward_button);
 
     clearPlayedSegments = (Button) findViewById(R.id.clear_played_segments);
     clearPlayedSegments.setOnClickListener(this);
 
     clearPlaylist = (Button) findViewById(R.id.clear_playlist);
     clearPlaylist.setOnClickListener(this);
 
     Cursor cursor = context.getContentResolver().query(PlaylistProvider
         .CONTENT_URI, null, null, null, PlaylistProvider.Items.PLAY_ORDER);
     playlistAdapter = new PlaylistAdapter(context, cursor);
 
     changeReceiver = new PlaybackChangeReceiver();
     Intent intent = context.registerReceiver(changeReceiver,
         new IntentFilter(PlaybackService.SERVICE_CHANGE_NAME));
     if (intent != null) {
       changeReceiver.onReceive(context, intent);
     } else {
       Log.d(LOG_TAG, "Call clearPlayer from init");
       clearPlayer();
     }
 
     updateReceiver = new PlaybackUpdateReceiver();
     intent = context.registerReceiver(updateReceiver,
         new IntentFilter(PlaybackService.SERVICE_UPDATE_NAME));
     if (intent != null) {
       updateReceiver.onReceive(context, intent);
     }
     closeReceiver = new PlaybackCloseReceiver();
     context.registerReceiver(closeReceiver,
         new IntentFilter(PlaybackService.SERVICE_CLOSE_NAME));
     errorReceiver = new PlaybackErrorReceiver();
     context.registerReceiver(errorReceiver,
         new IntentFilter(PlaybackService.SERVICE_ERROR_NAME));
 
     playlistChangedReceiver = new PlaylistChangedReceiver();
     context.registerReceiver(playlistChangedReceiver,
         new IntentFilter(PlaylistRepository.PLAYLIST_CHANGED));
 
     listView = (DragNDropListView) findViewById(R.id.playlist);
     listView.setAdapter(playlistAdapter);
     listView.setOnItemClickListener(this);
     listView.setDragListener(this);
     listView.setDropListener(this);
 
     // Gesture detection
     gestureDetector = new GestureDetector(
         new ListItemGestureListener(listView, handler)
     );
     View.OnTouchListener gestureListener = new View.OnTouchListener() {
       public boolean onTouch(View v, MotionEvent event) {
         return gestureDetector.onTouchEvent(event);
       }
     };
     listView.setOnTouchListener(gestureListener);
 
     if (context instanceof Activity) {
       ((Activity) context).registerForContextMenu(listView);
     }
 
     refreshList();
   }
 
 
   private void refreshList() {
     playlistAdapter.getCursor().requery();
     playlistAdapter.notifyDataSetChanged();
   }
 
   @Override
   protected void onDetachedFromWindow() {
     super.onDetachedFromWindow();
     // Emulator calls detach twice, so clear receiver
     if (changeReceiver != null) {
       context.unregisterReceiver(changeReceiver);
       changeReceiver = null;
     }
     if (updateReceiver != null) {
       context.unregisterReceiver(updateReceiver);
       updateReceiver = null;
     }
     if (closeReceiver != null) {
       context.unregisterReceiver(closeReceiver);
       closeReceiver = null;
     }
     if (errorReceiver != null) {
       context.unregisterReceiver(errorReceiver);
       errorReceiver = null;
     }
     if (playlistChangedReceiver != null) {
       context.unregisterReceiver(playlistChangedReceiver);
       playlistChangedReceiver = null;
     }
     if (playlistAdapter != null) {
       playlistAdapter.close();
       playlistAdapter = null;
     }
   }
 
   @Override
   public void onClick(View v) {
     switch (v.getId()) {
       case R.id.clear_playlist:
         Intent intent = new Intent(context, PlaybackService.class);
         intent.setAction(PlaybackService.SERVICE_CLEAR_PLAYER);
         context.startService(intent);
 
         playlist.clearAll();
         refreshList();
         configurePlayerControls();
         break;
 
       case R.id.clear_played_segments:
         playlist.clearPlayed();
         refreshList();
         configurePlayerControls();
         break;
     }
   }
 
   protected void playEntryNow(final PlaylistEntry entry) {
     playNow(
         Playable.PlayableFactory.fromPlaylistEntry(entry),
         PlaybackService.SERVICE_PLAY_ENTRY
     );
   }
 
   protected void playSingleNow(final Playable playable) {
     playNow(playable, PlaybackService.SERVICE_PLAY_SINGLE);
   }
 
   private void playNow(final Playable playable, String action) {
     startPlaylistSpinners();
     Intent intent = new Intent(context, PlaybackService.class);
     intent.setAction(action);
     intent.putExtra(Playable.PLAYABLE_TYPE, playable);
     context.startService(intent);
 
     newsItemText.setText(playable.getTitle());
     contractedNewsItemText.setText(playable.getTitle());
     playlistAdapter.setActiveId(Long.toString(playable.getId()));
     refreshList();
     configurePlayerControls();
   }
 
 
   private void configurePlayerControls() {
 
     if (playlist.getItemCount() > 0) {
       clearPlaylist.setEnabled(true);
       clearPlayedSegments.setEnabled(playlist.getReadCount() > 0);
     } else {
       clearPlaylist.setEnabled(false);
       clearPlayedSegments.setEnabled(false);
     }
 
     String activeId = playlistAdapter.getActiveId();
     if (activeId != null) {
       playerStatus.setText(R.string.msg_player_now_playing);
       contractedPlayerStatus.setText(R.string.msg_player_now_playing);
       if (!activeId.equals("-1")) {
         playEnabled = true;
         moveControlsEnabled = true;
         rewindEnabled = !playlist.isFirstEntry(activeId);
         fastForwardEnabled = !playlist.isLastEntry(activeId);
       } else {
         playEnabled = true;
         moveControlsEnabled = true;
         rewindEnabled = false;
         fastForwardEnabled = (playlist.getFirstUnreadEntry() != null);
       }
     } else {
       Playable firstUnreadEntry = playlist.getFirstUnreadEntry();
       if (firstUnreadEntry != null) {
         playerStatus.setText(R.string.msg_player_ready_to_play);
         contractedPlayerStatus.setText(R.string.msg_player_ready_to_play);
         String title = firstUnreadEntry.getTitle();
         newsItemText.setText(title);
         contractedNewsItemText.setText(title);
       } else {
         playerStatus.setText(R.string.msg_player_nothing_to_play);
         contractedPlayerStatus.setText(R.string.msg_player_nothing_to_play);
         newsItemText.setText("");
         contractedNewsItemText.setText("");
       }
       playEnabled = (firstUnreadEntry != null);
       moveControlsEnabled = false;
       rewindEnabled = false;
       fastForwardEnabled = false;
     }
   }
 
   private void clearPlayer() {
     playlistAdapter.setActiveId(null);
     configurePlayerControls();
     progressBar.setProgress(0);
     progressBar.setSecondaryProgress(0);
     lengthText.setText("0:00 / 0:00");
     stopPlaylistSpinners();
   }
 
 
   private class PlaybackChangeReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
       String title = intent.getStringExtra(PlaybackService.EXTRA_TITLE);
       newsItemText.setText(title);
       contractedNewsItemText.setText(title);
       playlistAdapter.setActiveId(Long.toString(intent.getLongExtra(
           PlaybackService.EXTRA_ID, -1)));
       refreshList();
       configurePlayerControls();
     }
   }
 
   private class PlaybackUpdateReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
       int duration = intent.getIntExtra(PlaybackService.EXTRA_DURATION, 1);
       // Drop out if no duration is given (flicker?)
       if (duration == 1) {
         Log.v(LOG_TAG, "Playback update; no duration dropout");
         return;
       }
 
       int position = intent.getIntExtra(PlaybackService.EXTRA_POSITION, 0);
       int downloaded = intent.getIntExtra(PlaybackService.EXTRA_DOWNLOADED, 1);
       Log.v(LOG_TAG, "Playback update; position = " + position + " millsecs; " +
           "downloaded = " + duration + " millsecs");
       boolean isPlaying = intent.getBooleanExtra(PlaybackService
           .EXTRA_IS_PLAYING, false);
       if (!changingProgress) {
         progressBar.setMax(duration);
         progressBar.setProgress(position);
       }
       progressBar.setSecondaryProgress(downloaded);
 
       // StringBuilder much faster than String.Format
       StringBuilder length = new StringBuilder(13);
       length.append(position / 60000);
       length.append(':');
       int secs = position / 1000 % 60;
       if (secs < 10) {
         length.append('0');
       }
       length.append(secs);
       length.append(" / ");
       length.append(duration / 60000);
       length.append(':');
       secs = duration / 1000 % 60;
       if (secs < 10) {
         length.append('0');
       }
       length.append(secs);
       lengthText.setText(length.toString());
 
       if (position > 0) {
         // Streams have no 'downloaded' amount
         if (downloaded == 0 || downloaded >= position) {
           stopPlaylistSpinners();
         } else if (isPlaying) {
           startPlaylistSpinners();
         }
       }
 
       if (isPlaying == playPauseShowsPlay) {
         if (isPlaying) {
           playPauseButton.setImageResource(R.drawable.pause_button_normal);
           contractedPlayButton.setImageResource(R.drawable.pause_button_normal);
           playPauseShowsPlay = false;
         } else {
           playPauseButton.setImageResource(R.drawable.play_button_normal);
           contractedPlayButton.setImageResource(R.drawable.play_button_normal);
           playPauseShowsPlay = true;
         }
       }
     }
   }
 
   private class PlaybackCloseReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
       Log.d(LOG_TAG, "Playback close received - calling clear player");
       clearPlayer();
       refreshList();
     }
   }
 
   private class PlaybackErrorReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
       Log.d(LOG_TAG, "Playback error received - toasting message");
      String message = context.getString(R.string.msg_unknown_error);
 
       int error = intent.getIntExtra(PlaybackService.EXTRA_ERROR, -1);
       if (error == PlaybackService.PLAYBACK_SERVICE_ERROR.Playback.ordinal()) {
         message = context.getString(R.string.msg_playback_error);
       } else if (error == PlaybackService.PLAYBACK_SERVICE_ERROR.Connection.ordinal()) {
        message = context.getString(R.string.msg_playback_connection_error);
       }
       Toast.makeText(context, message, Toast.LENGTH_LONG).show();
     }
   }
 
   private class PlaylistChangedReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
       configurePlayerControls();
     }
   }
 
   @Override
   public void onProgressChanged(SeekBar seekBar, int progress,
                                 boolean fromUser) {
     seekBar.setProgress(progress);
     if (fromUser) {
       Intent intent = new Intent(context, PlaybackService.class);
       intent.setAction(PlaybackService.SERVICE_SEEK_TO);
       intent.putExtra(PlaybackService.EXTRA_SEEK_TO, progress);
       context.startService(intent);
     }
   }
 
   @Override
   public void onStartTrackingTouch(SeekBar seekBar) {
     changingProgress = true;
   }
 
   @Override
   public void onStopTrackingTouch(SeekBar seekBar) {
     changingProgress = false;
   }
 
   @Override
   public void onDrawerOpened() {
     handle.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
         DisplayUtils.convertToDIP(context, 150)));
     playerContracted.setVisibility(View.GONE);
     playerExpanded.setVisibility(View.VISIBLE);
     refreshList();
   }
 
   @Override
   public void onDrawerClosed() {
     handle.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
         DisplayUtils.convertToDIP(context, 95)));
     playerContracted.setVisibility(View.VISIBLE);
     playerExpanded.setVisibility(View.GONE);
   }
 
   public boolean isExpanded() {
     return drawer.isOpened();
   }
 
   public void setExpanded(boolean expanded) {
     if (expanded) {
       drawer.animateOpen();
     } else {
       drawer.animateClose();
     }
   }
 
   private boolean ViewContainsXY(View v, int x, int y) {
     Rect r = new Rect();
     v.getDrawingRect(r);
     offsetDescendantRectToMyCoords(v, r);
     return r.contains(x, y);
   }
 
   private void closeDrawerIfPastThreshold(int y) {
     Rect r = new Rect();
     drawer.getDrawingRect(r);
     if ((y - startY) > (r.height() / 3)) {
       drawer.close();
     }
   }
 
   @Override
   public boolean onInterceptTouchEvent(MotionEvent event) {
     if (event.getAction() == MotionEvent.ACTION_DOWN) {
       final int x = (int) event.getX(), y = (int) event.getY();
       if (isExpanded()) {
         if (ViewContainsXY(playPauseButton, x, y)) {
           clickedItem = ClickedItem.playPause;
           return true;
         } else if (ViewContainsXY(rewindButton, x, y)) {
           clickedItem = ClickedItem.rewind;
           return true;
         } else if (ViewContainsXY(rewind30Button, x, y)) {
           clickedItem = ClickedItem.rewind30;
           return true;
         } else if (ViewContainsXY(fastForwardButton, x, y)) {
           clickedItem = ClickedItem.fastForward;
           return true;
         } else if (ViewContainsXY(progressBar, x, y)) {
           clickedItem = ClickedItem.progressbar;
           return true;
         }
       } else {
         if (ViewContainsXY(contractedPlayButton, x, y)) {
           clickedItem = ClickedItem.contractedPlay;
           return true;
         }
       }
     }
 
     return false;
   }
 
   private void showPlayPause(boolean showPressed) {
     if (playPauseShowsPlay) {
       if (showPressed) {
         playPauseButton.setImageResource(R.drawable.play_button_pressed);
         contractedPlayButton.setImageResource(R.drawable.play_button_pressed);
       } else {
         playPauseButton.setImageResource(R.drawable.play_button_normal);
         contractedPlayButton.setImageResource(R.drawable.play_button_normal);
       }
     } else {
       if (showPressed) {
         playPauseButton.setImageResource(R.drawable.pause_button_pressed);
         contractedPlayButton.setImageResource(R.drawable.pause_button_pressed);
       } else {
         playPauseButton.setImageResource(R.drawable.pause_button_normal);
         contractedPlayButton.setImageResource(R.drawable.pause_button_normal);
       }
     }
   }
 
   @Override
   public boolean onTouchEvent(MotionEvent event) {
 
     if (clickedItem != null) {
 
       final int x = (int) event.getX(), y = (int) event.getY();
 
       switch (clickedItem) {
 
         case playPause:
 
           switch (event.getAction()) {
 
             case MotionEvent.ACTION_DOWN:
 
               if (ViewContainsXY(playPauseButton, x, y)) {
                 if (playEnabled) {
                   showPlayPause(true);
                 }
                 startY = y;
                 cancelDown = false;
                 return true;
               }
               break;
 
             case MotionEvent.ACTION_MOVE:
 
               if (!ViewContainsXY(playPauseButton, x, y)) {
                 if (playEnabled) {
                   showPlayPause(false);
                 }
                 cancelDown = true;
                 return true;
               }
               break;
 
             case MotionEvent.ACTION_UP:
 
               if (playEnabled && !cancelDown) {
                 playPauseShowsPlay = !playPauseShowsPlay;
                 if (playPauseShowsPlay) {
                   stopPlaylistSpinners();
                 } else {
                   startPlaylistSpinners();
                 }
 
                 showPlayPause(false);
                 Intent intent = new Intent(context, PlaybackService.class);
                 intent.setAction(PlaybackService.SERVICE_TOGGLE_PLAY);
                 context.startService(intent);
               } else {
                 closeDrawerIfPastThreshold(y);
               }
               clickedItem = null;
               return true;
           }
           break;
 
         case rewind:
 
           switch (event.getAction()) {
 
             case MotionEvent.ACTION_DOWN:
 
               if (ViewContainsXY(rewindButton, x, y)) {
                 if (rewindEnabled) {
                   rewindButton.setImageResource(R.drawable.rew_pressed);
                 }
                 startY = y;
                 cancelDown = false;
                 return true;
               }
               break;
 
             case MotionEvent.ACTION_MOVE:
 
               if (!ViewContainsXY(rewindButton, x, y)) {
                 if (rewindEnabled) {
                   rewindButton.setImageResource(R.drawable.rew_normal);
                 }
                 cancelDown = true;
                 return false;
               }
               break;
 
             case MotionEvent.ACTION_UP:
 
               if (rewindEnabled && !cancelDown) {
                 rewindButton.setImageResource(R.drawable.rew_normal);
                 Intent intent = new Intent(context, PlaybackService.class);
                 intent.setAction(PlaybackService.SERVICE_PLAY_PREVIOUS);
                 context.startService(intent);
               } else {
                 closeDrawerIfPastThreshold(y);
               }
               clickedItem = null;
               return true;
           }
           break;
 
         case rewind30:
 
           switch (event.getAction()) {
 
             case MotionEvent.ACTION_DOWN:
 
               if (ViewContainsXY(rewind30Button, x, y)) {
                 if (moveControlsEnabled) {
                   rewind30Button.setImageResource(R.drawable.rew_30_pressed);
                 }
                 startY = y;
                 cancelDown = false;
                 return true;
               }
               break;
 
             case MotionEvent.ACTION_MOVE:
 
               if (!ViewContainsXY(rewind30Button, x, y)) {
                 if (moveControlsEnabled) {
                   rewind30Button.setImageResource(R.drawable.rew_30_normal);
                 }
                 cancelDown = true;
                 return false;
               }
               break;
 
             case MotionEvent.ACTION_UP:
 
               if (moveControlsEnabled && !cancelDown) {
                 rewind30Button.setImageResource(R.drawable.rew_30_normal);
                 Intent intent = new Intent(context, PlaybackService.class);
                 intent.setAction(PlaybackService.SERVICE_BACK_30);
                 context.startService(intent);
               } else {
                 closeDrawerIfPastThreshold(y);
               }
               clickedItem = null;
               return true;
           }
           break;
 
         case fastForward:
 
           switch (event.getAction()) {
 
             case MotionEvent.ACTION_DOWN:
 
               if (ViewContainsXY(fastForwardButton, x, y)) {
                 if (fastForwardEnabled) {
                   fastForwardButton.setImageResource(R.drawable.ffwd_pressed);
                 }
                 startY = y;
                 cancelDown = false;
                 return true;
               }
               break;
 
             case MotionEvent.ACTION_MOVE:
 
               if (!ViewContainsXY(fastForwardButton, x, y)) {
                 if (fastForwardEnabled) {
                   fastForwardButton.setImageResource(R.drawable.ffwd_normal);
                 }
                 cancelDown = true;
                 return false;
               }
               break;
 
             case MotionEvent.ACTION_UP:
 
               if (fastForwardEnabled && !cancelDown) {
                 fastForwardButton.setImageResource(R.drawable.ffwd_normal);
                 Intent intent = new Intent(context, PlaybackService.class);
                 intent.setAction(PlaybackService.SERVICE_PLAY_NEXT);
                 context.startService(intent);
               } else {
                 closeDrawerIfPastThreshold(y);
               }
               clickedItem = null;
               return true;
           }
 
           break;
 
         case contractedPlay:
           switch (event.getAction()) {
 
             case MotionEvent.ACTION_DOWN:
 
               if (ViewContainsXY(contractedPlayButton, x, y)) {
                 if (playEnabled) {
                   showPlayPause(true);
                 }
                 startY = y;
                 cancelDown = false;
                 return true;
               }
               break;
 
             case MotionEvent.ACTION_MOVE:
 
               if (!ViewContainsXY(contractedPlayButton, x, y)) {
                 if (playEnabled) {
                   showPlayPause(false);
                 }
                 cancelDown = true;
                 return false;
               }
               break;
 
             case MotionEvent.ACTION_UP:
 
               if (playEnabled && !cancelDown) {
                 playPauseShowsPlay = !playPauseShowsPlay;
                 if (playPauseShowsPlay) {
                   stopPlaylistSpinners();
                 } else {
                   startPlaylistSpinners();
                 }
                 showPlayPause(false);
                 Intent intent = new Intent(context, PlaybackService.class);
                 intent.setAction(PlaybackService.SERVICE_TOGGLE_PLAY);
                 context.startService(intent);
               } else {
                 Rect r = new Rect();
                 drawer.getDrawingRect(r);
                 if ((startY - y) > (r.height() / 3)) {
                   drawer.open();
                 }
               }
               clickedItem = null;
               return true;
           }
           break;
 
         case progressbar:
 
           switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
 
               if (ViewContainsXY(progressBar, x, y)) {
                 startX = x;
                 startY = y;
                 return true;
               }
               break;
 
             case MotionEvent.ACTION_MOVE:
 
               boolean xMovement = Math.abs(x - startX) >= touchSlop;
               if (moveControlsEnabled && xMovement) {
                 progressBar.onTouchEvent(event);
               } else if (Math.abs(y - startY) >= touchSlop && !xMovement) {
                 cancelDown = true;
               }
               return true;
 
             case MotionEvent.ACTION_UP:
 
               if (moveControlsEnabled && !cancelDown) {
                 progressBar.onTouchEvent(event);
               } else {
                 closeDrawerIfPastThreshold(y);
               }
               clickedItem = null;
               return true;
           }
           break;
       }
     }
     return false;
   }
 
   @Override
   public void onItemClick(AdapterView<?> parent, View view, int position,
                           long id) {
     PlaylistEntry entry = getPlaylistEntryAtPosition(position);
     playEntryNow(entry);
   }
 
   /**
    * Returns a PlaylistEntry for the item at the given position in the
    * playlist.
    *
    * @param position The position of the item starting at 0
    * @return A PlaylistEntry or null of the position is invalid or cannot be
    *         found.
    */
   public PlaylistEntry getPlaylistEntryAtPosition(int position) {
     PlaylistEntry entry = null;
     DragNDropListView listView = (DragNDropListView) findViewById(R.id.playlist);
     Cursor c = (Cursor) listView.getItemAtPosition(position);
     if (c != null) {
       c.moveToPosition(position);
       long playlistId = c.getLong(c.getColumnIndex(PlaylistProvider.Items._ID));
       Log.d(LOG_TAG, "clicked on position " + position + ", id " + playlistId);
       entry = playlist.getPlaylistItemFromId(playlistId);
       if (entry == null) {
         Log.e(LOG_TAG, "Couldn't find playlist item " + playlistId +
             " at position" + position);
       }
     }
     return entry;
   }
 
   private void startPlaylistSpinners() {
     ProgressBar loadingIndicator =
         (ProgressBar) findViewById(R.id.player_loading_indicator);
     if (loadingIndicator != null) {
       loadingIndicator.setVisibility(View.VISIBLE);
     } else {
       Log.w(LOG_TAG, "Can't find loading indicator. Expanded? " + isExpanded());
     }
     loadingIndicator =
         (ProgressBar) findViewById(R.id.player_loading_indicator_contracted);
     if (loadingIndicator != null) {
       loadingIndicator.setVisibility(View.VISIBLE);
     }
   }
 
   private void stopPlaylistSpinners() {
     ProgressBar loadingIndicator =
         (ProgressBar) findViewById(R.id.player_loading_indicator);
     if (loadingIndicator != null) {
       loadingIndicator.setVisibility(View.INVISIBLE);
     }
     loadingIndicator =
         (ProgressBar) findViewById(R.id.player_loading_indicator_contracted);
     if (loadingIndicator != null) {
       loadingIndicator.setVisibility(View.INVISIBLE);
     }
   }
 
 
   @Override
   public void onStartDrag(View itemView) {
     drawer.lock();
     itemView.setVisibility(View.INVISIBLE);
   }
 
   @Override
   public void onDrag(int x, int y, ListView listView) {
   }
 
   @Override
   public void onStopDrag(View itemView) {
     itemView.setVisibility(View.VISIBLE);
     drawer.unlock();
   }
 
   @Override
   public void onDrop(int from, int to) {
     playlist.move(from, to);
     refreshList();
   }
 
   public void updatePlaylist() {
     refreshList();
     configurePlayerControls();
 
     if (playlist.getItemCount() == 0) {
       Intent intent = new Intent(context, PlaybackService.class);
       intent.setAction(PlaybackService.SERVICE_CLEAR_PLAYER);
       context.startService(intent);
     }
   }
 
   public String getActiveId() {
     return playlistAdapter.getActiveId();
   }
 
 }
