 package com.ouchadam.fang.presentation.panel;
 
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.ViewSwitcher;
 
 import com.novoda.notils.caster.Views;
 import com.ouchadam.fang.FangDuration;
 import com.ouchadam.fang.R;
 import com.ouchadam.fang.domain.FullItem;
 import com.ouchadam.fang.domain.item.Item;
 import com.ouchadam.fang.view.SlidingUpPanelLayout;
 import com.squareup.picasso.Picasso;
 
 import java.text.MessageFormat;
 
 class PanelViewHolder {
 
     private final PositionController positionController;
     private final MediaViewController mediaController;
     private final DownloadController downloadController;
     private final MainPanelController mainPanelController;
 
     public static PanelViewHolder from(SlidingUpPanelLayout panel) {
         ViewSwitcher topMediaSwitcher = Views.findById(panel, R.id.media_switcher);
         ViewSwitcher bottomMediaSwitcher = Views.findById(panel, R.id.bottom_media_switcher);
 
         View playTop = Views.findById(panel, R.id.play_top);
         View playBottom = Views.findById(panel, R.id.play_bottom);
         View pauseTop = Views.findById(panel, R.id.pause_top);
         View pauseBottom = Views.findById(panel, R.id.pause_bottom);
 
         MediaViewController mediaController = new MediaViewController(playTop, playBottom, pauseTop, pauseBottom, topMediaSwitcher, bottomMediaSwitcher);
 
         ImageButton downloadButton = Views.findById(panel, R.id.download);
         DownloadController downloadController = new DownloadController(downloadButton);
 
         SeekBar seekBar = Views.findById(panel, R.id.seek_bar);
         TextView currentTime = Views.findById(panel, R.id.current_position);
         TextView endTime = Views.findById(panel, R.id.total_time);
         PositionController position = new PositionController(seekBar, currentTime, endTime);
 
         MainPanelController mainPanelController = new MainPanelController(panel);
 
         return new PanelViewHolder(position, mediaController, downloadController, mainPanelController);
     }
 
     PanelViewHolder(PositionController positionController, MediaViewController mediaController, DownloadController downloadController, MainPanelController mainPanelController) {
         this.mediaController = mediaController;
         this.positionController = positionController;
         this.downloadController = downloadController;
         this.mainPanelController = mainPanelController;
     }
 
     public MediaViewController mediaController() {
         return mediaController;
     }
 
     public void showCollapsed(boolean isDownloaded) {
         mediaController.showCollapsed(isDownloaded);
         downloadController.panelScopeChange(isDownloaded);
         mainPanelController.makeTopBarSolid();
     }
 
     public void showExpanded(boolean downloaded) {
         mediaController.showExpanded(downloaded);
         positionController.panelScopeChange(downloaded);
         downloadController.panelScopeChange(downloaded);
         mainPanelController.makeTopBarTransparent();
         mainPanelController.showBottomBar(downloaded);
     }
 
     public DownloadController downloadController() {
         return downloadController;
     }
 
     public PositionController positionController() {
         return positionController;
     }
 
     public void updatePlayingState(boolean playing) {
         if (playing) {
             mediaController.showPause();
         } else {
             mediaController.showPlay();
         }
     }
 
     public void updatePanel(FullItem fullItem) {
         mainPanelController.updateFrom(fullItem);
        downloadController.panelScopeChange(fullItem.isDownloaded());
         mediaController.setMediaVisibility(mainPanelController.isShowing(), fullItem);
         positionController.setInitialPosition(fullItem.getInitialPlayPosition());
     }
 
     public void close() {
         mainPanelController.close();
     }
 
     public void expand() {
         mainPanelController.expand();
     }
 
     public boolean isShowing() {
         return mainPanelController.isShowing();
     }
 
     public void setPanelSlideListener(SlidingUpPanelLayout.PanelSlideListener panelSlideListener) {
         mainPanelController.setPanelSlideListener(panelSlideListener);
     }
 
     public void show() {
         mainPanelController.showPanel();
     }
 
     private static class MainPanelController {
 
         private final SlidingUpPanelLayout panelLayout;
 
         private MainPanelController(SlidingUpPanelLayout panelLayout) {
             this.panelLayout = panelLayout;
             hidePanel();
         }
 
         private void hidePanel() {
             panelLayout.hidePanel();
         }
 
         private void showPanel() {
             panelLayout.showPanel();
         }
 
         public void updateFrom(FullItem fullItem) {
             Item item = fullItem.getItem();
             setBarTitle(item.getTitle());
             setDescription(item.getSummary());
             setDuration(item.getDuration());
             setBarSubtitle(fullItem.getChannelTitle());
             setBackgroundImage(getImageUrl(fullItem));
         }
 
         private String getImageUrl(FullItem fullItem) {
             String heroImage = fullItem.getItem().getHeroImage();
             String channelImage = fullItem.getImageUrl();
             return heroImage == null ? channelImage : heroImage;
         }
 
         private void setBackgroundImage(String url) {
             if (url != null) {
                 ImageView imageView = Views.findById(panelLayout, R.id.drawer_content_image);
                 Picasso.with(panelLayout.getContext()).load(url).centerCrop().resize(imageView.getWidth(), imageView.getHeight()).into(imageView);
             }
             // TODO load a default image or something
         }
 
         private void setBarTitle(CharSequence text) {
             setTextViewText(text, R.id.bar_title);
         }
 
         private void setBarSubtitle(CharSequence text) {
             setTextViewText(text, R.id.bar_sub_title);
         }
 
         private void setDescription(CharSequence summary) {
             setTextViewText(summary, R.id.item_description);
         }
 
         private void setDuration(FangDuration duration) {
             String formattedDuration = formatDuration(duration);
             setTextViewText(formattedDuration, R.id.item_duration);
         }
 
         private String formatDuration(FangDuration duration) {
             int hours = duration.getHours();
             int minutes = duration.getMinutes();
             String hoursString = "";
             if (hours > 0) {
                 hoursString = panelLayout.getResources().getQuantityString(R.plurals.hour, hours, hours) + " ";
             }
             return hoursString + minutes + " " + "minutes";
         }
 
         private void setTextViewText(CharSequence text, int viewId) {
             TextView textView = Views.findById(panelLayout, viewId);
             textView.setText(text);
         }
 
         public void close() {
             panelLayout.collapsePane();
         }
 
         public boolean isShowing() {
             return panelLayout.isExpanded();
         }
 
         public void expand() {
             showPanel();
             panelLayout.expandPane();
         }
 
         public void setPanelSlideListener(SlidingUpPanelLayout.PanelSlideListener panelSlideListener) {
             panelLayout.setPanelSlideListener(panelSlideListener);
         }
 
         public void makeTopBarTransparent() {
             int color = getColour(R.color.trans_white);
             setTopBarColor(color);
         }
 
         private int getColour(int colourId) {
             return panelLayout.getResources().getColor(colourId);
         }
 
         public void makeTopBarSolid() {
             setTopBarColor(getColour(R.color.white));
         }
 
         private void setTopBarColor(int color) {
             View topBarContainer = Views.findById(panelLayout, R.id.top_bar_container);
             Views.findById(topBarContainer, R.id.player_top_bar).setBackgroundColor(color);
         }
 
         public void showBottomBar(boolean downloaded) {
             if (downloaded) {
                 setBottomBarVisibility(View.VISIBLE);
             } else {
                 setBottomBarVisibility(View.INVISIBLE);
             }
         }
 
         private void setBottomBarVisibility(int visibility) {
             Views.findById(panelLayout, R.id.player_main).setVisibility(visibility);
         }
     }
 
 }
