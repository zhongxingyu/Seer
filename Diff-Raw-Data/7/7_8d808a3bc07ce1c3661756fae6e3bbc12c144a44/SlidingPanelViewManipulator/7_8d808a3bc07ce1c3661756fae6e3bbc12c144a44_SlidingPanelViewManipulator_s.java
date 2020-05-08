 package com.ouchadam.fang.presentation.panel;
 
 import android.view.View;
 
 import com.novoda.notils.caster.Views;
 import com.ouchadam.fang.R;
 import com.ouchadam.fang.audio.SeekbarReceiver;
 import com.ouchadam.fang.domain.FullItem;
 import com.ouchadam.fang.domain.PodcastPosition;
 import com.ouchadam.fang.presentation.ActionBarManipulator;
 import com.ouchadam.fang.view.SlidingUpPanelLayout;
 
 public class SlidingPanelViewManipulator implements OnPanelChangeListener {
 
     private final ActionBarManipulator actionBarManipulator;
     private final PanelViewHolder panelViewHolder;
     private final DownloadFoo downloadFoo;
     private final PositionManager positionManager;
 
     public void setPlayingState(boolean playing) {
         panelViewHolder.updatePlayingState(playing);
         positionManager.upatePlayingState(playing);
     }
 
     public void update(PodcastPosition position) {
         positionManager.update(position);
     }
 
     public PodcastPosition getPosition() {
         return positionManager.getLatestPosition();
     }
 
     public void setMediaClickedListener(MediaClickManager.OnMediaClickListener onMediaClickListener) {
         // TODO : not a fan of this
         MediaClickManager mediaClickManager = new MediaClickManager(onMediaClickListener, panelViewHolder.mediaController());
     }
 
     public void show() {
         panelViewHolder.show();
     }
 
     public interface OnDownloadClickListener {
         void onDownloadClicked(FullItem fullItem);
     }
 
     public interface OnSeekChanged {
         void onSeekChanged(PodcastPosition position);
     }
 
     public static SlidingPanelViewManipulator from(ActionBarManipulator actionBarManipulator, OnSeekChanged onSeekChanged, View root) {
         SlidingUpPanelLayout slidingPanel = Views.findById(root, R.id.sliding_layout);
         slidingPanel.setShadowDrawable(root.getResources().getDrawable(R.drawable.above_shadow));
         PanelViewHolder panelViewHolder = PanelViewHolder.from(slidingPanel);
         return new SlidingPanelViewManipulator(actionBarManipulator, onSeekChanged, panelViewHolder);
     }
 
     SlidingPanelViewManipulator(ActionBarManipulator actionBarManipulator, OnSeekChanged onSeekChanged, PanelViewHolder panelViewHolder) {
         this.actionBarManipulator = actionBarManipulator;
         this.panelViewHolder = panelViewHolder;
         this.downloadFoo = new DownloadFoo(panelViewHolder.downloadController());
         positionManager = new PositionManager(onSeekChanged, new SeekbarReceiver(seekUpdate), panelViewHolder.positionController());
 
         setOnPanelExpandListener(this);
     }
 
     private final SeekbarReceiver.OnSeekUpdate seekUpdate = new SeekbarReceiver.OnSeekUpdate() {
         @Override
         public void onUpdate(PodcastPosition position) {
             positionManager.update(position);
         }
     };
 
     private void setOnPanelExpandListener(final OnPanelChangeListener onPanelChangeListener) {
         panelViewHolder.setPanelSlideListener(new PanelChangeHandler(actionBarManipulator, onPanelChangeListener));
     }
 
     public void setOnDownloadClickedListener(final OnDownloadClickListener onDownloadClickedListener) {
         downloadFoo.setListener(onDownloadClickedListener);
     }
 
     public void expand() {
         panelViewHolder.expand();
     }
 
     public void fromItem(FullItem fullItem) {
         downloadFoo.itemChange(fullItem);
         panelViewHolder.updatePanel(fullItem);
     }
 
     public void close() {
         panelViewHolder.close();
     }
 
     public boolean isShowing() {
         return panelViewHolder.isShowing();
     }
 
     @Override
     public void onPanelExpanded(View panel) {
         showExpanded(downloadFoo.isDownloaded());
         positionManager.registerForUpdates(panel.getContext());
     }
 
     private void showExpanded(boolean isDownloaded) {
         panelViewHolder.showExpanded(isDownloaded);
     }
 
     @Override
     public void onPanelCollapsed(View panel) {
         showCollapsed(downloadFoo.isDownloaded());
         positionManager.unregisterForUpdates(panel.getContext());
     }
 
     private void showCollapsed(boolean isDownloaded) {
         panelViewHolder.showCollapsed(isDownloaded);
     }
 
 }
