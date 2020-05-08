 package com.hudomju.imagesdemo;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.hudomju.imagesdemo.app.ImagesDemoApplication;
 import com.hudomju.imagesdemo.io.DownloadManagerApi;
 import com.hudomju.imagesdemo.io.DownloadManagerApi.DownloadManagerApiCallbacks;
 
 import javax.inject.Inject;
 
 import butterknife.InjectView;
 import butterknife.OnClick;
 import butterknife.Views;
 
 public class MainActivity extends Activity implements DownloadManagerApiCallbacks,
         AdapterView.OnItemClickListener, ImageZoomer.ZoomInCallback {
 
     @Inject GridLayoutAdapter mGridLayoutAdapter;
     @Inject DownloadManagerApi mDownloadManagerApi;
     @InjectView(R.id.container) View mContainer;
     @InjectView(R.id.grid_view) GridView mGridView;
     @InjectView(R.id.expanded_image) ImageView mExpandedImageView;
     @InjectView(R.id.btn_img_downloader) Button mBtnImageDownloader;
     private ImageZoomer mImageZoomer;
     private String mCurrentUrl = "";
 
     @Override public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main_activity);
         ((ImagesDemoApplication) getApplication()).inject(this);
         Views.inject(this);
         mGridView.setAdapter(mGridLayoutAdapter);
         mGridView.setOnItemClickListener(this);
     }
 
     @Override public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_show_downloads:
                 mDownloadManagerApi.showAllDownloads();
         }
         return super.onOptionsItemSelected(item);
     }
 
    @Override protected void onStart() {
        super.onStart();
         mDownloadManagerApi.init();
         mDownloadManagerApi.registerCallbacks(this);
     }
 
     @Override protected void onPause() {
         super.onPause();
         mDownloadManagerApi.stop();
         mDownloadManagerApi.unregisterCallbacks(this);
     }
 
     @Override public void onCompleteDownload(String url) {
         Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
         if (url.equals(mCurrentUrl))
             mBtnImageDownloader.setText(R.string.cancel_image_download);
     }
 
     @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         mCurrentUrl = mGridLayoutAdapter.getItem(position);
         mImageZoomer = new ImageZoomer(mContainer, view, mExpandedImageView);
         mExpandedImageView.setImageDrawable(((ImageView) view).getDrawable());
         mImageZoomer.zoomImageFromThumb(this);
     }
 
     @Override public void onZoomInCompleted() {
         mExpandedImageView.setBackgroundColor(getResources().getColor(android.R.color.black));
         mBtnImageDownloader.setVisibility(View.VISIBLE);
         if (mDownloadManagerApi.isDownloading(mCurrentUrl))
             mBtnImageDownloader.setText(R.string.cancel_image_download);
         else
             mBtnImageDownloader.setText(R.string.download_image);
     }
 
 
     @OnClick(R.id.expanded_image) public void onExpandedViewClick() {
         mExpandedImageView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
         mImageZoomer.zoomImageOut();
         mBtnImageDownloader.setVisibility(View.INVISIBLE);
     }
 
     @OnClick(R.id.btn_img_downloader) public void onButtonImageDownloaderClick() {
         if (mBtnImageDownloader.getText().equals(getText(R.string.download_image))) {
             mDownloadManagerApi.startDownload(mCurrentUrl);
             mBtnImageDownloader.setText(R.string.cancel_image_download);
         } else {
             mDownloadManagerApi.stopDownload(mCurrentUrl);
             mBtnImageDownloader.setText(R.string.download_image);
         }
     }
 }
