 package com.guoli.hotel.activity.hotel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.google.gson.annotations.SerializedName;
 import com.guoli.hotel.R;
 import com.guoli.hotel.activity.BaseActivity2;
 import com.guoli.hotel.bean.HotelPicInfo;
 import com.guoli.hotel.net.Action;
 import com.guoli.hotel.net.GuoliRequest;
 import com.guoli.hotel.net.response.bean.PicInfo;
 import com.guoli.hotel.parse.HotelPicInfoParse;
 import com.guoli.hotel.utils.DialogUtils;
 import com.guoli.hotel.utils.ImageUtil;
 import com.guoli.hotel.widget.AbstractAdapter;
 import com.msx7.core.Controller;
 import com.msx7.core.Manager;
 import com.msx7.core.command.IResponseListener;
 import com.msx7.core.command.model.Response;
 
 public class PicGridActivity extends BaseActivity2 implements OnItemClickListener {
 
     GridView mGridView;
     private String mPicPath;
     private Dialog mLoadingDialog;
     private ArrayList<PicInfo> mPicInfos;
     private PicGridAdapter mGridAdapter;
 
     public static final String KEY_HOTEL_ID = "hotelId";
     public static final String KEY_PIC_PATH = "picPath";
     private static final String TAG = PicGridActivity.class.getSimpleName();
 
     @Override
     public void onAfterCreate(Bundle savedInstanceState) {
         setTitle(R.string.hotel_pic_grid_title);
         mGridView = (GridView) findViewById(R.id.gridView1);
         mGridView.setOnItemClickListener(this);
         showLeftReturnBtn(false, -1);
         loadImages();
     }
 
     @Override
     public int getContentId() {
         return R.layout.pic_grid_activity;
     }
 
     public class PicGridAdapter extends AbstractAdapter<PicInfo> {
 
         public PicGridAdapter(Context context, List<PicInfo> data) {
             super(context, data);
         }
 
         @Override
         public View CreateView(int position, View convertView, LayoutInflater inflater) {
             if (convertView == null) {
                 convertView = inflater.inflate(R.layout.pic_grid_cell, null);
             }
             ((TextView) convertView.findViewById(R.id.name)).setText(getItem(position).getName());
             ImageView imageView = (ImageView)convertView.findViewById(R.id.icon);
             String key = ImageUtil.getThumbnailImageUrl(mPicPath, getItem(position).getPicName());
             Controller.getApplication().loadThumbnailImage(key, imageView, R.drawable.default_big_pic);
             return convertView;
         }
 
     }
 
     @Override
     public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
         Intent intent = new Intent(this, BigPicActivity.class);
         intent.putParcelableArrayListExtra("data", mPicInfos);
         intent.putExtra(KEY_PIC_PATH, mPicPath);
         intent.putExtra("index", arg2);
         startActivity(intent);
     }
     
     private void showLoadingDialog(){
         mLoadingDialog = mLoadingDialog == null ? DialogUtils.showProgressDialog(this, R.string.loading_msg) : mLoadingDialog;
     }
     
     private void dismissLoadingDialog(){
         if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
             mLoadingDialog.dismiss();
         }
     }
     
     /***
      * 
      * ClassName: HotelPicRequest <br/>
      * date: 2013-4-2 下午5:07:45 <br/>
      * 
      * @Description:    酒店图片集合请求
      * @author maple
      * @version PicGridActivity
      * @since JDK 1.6
      */
     private class HotelPicRequest{
         /***/
         @SerializedName("shopid")
         private String id;
         /***/
         @SerializedName("picpath")
         private String picPath;
         public void setId(String id) {
             this.id = id;
         }
         public void setPicPath(String picPath) {
             this.picPath = picPath;
         }
     }
 
     private void loadImages(){
         showLoadingDialog();
         HotelPicRequest requestInfo = new HotelPicRequest();
         requestInfo.setId(getIntent().getStringExtra(KEY_HOTEL_ID));
         mPicPath = getIntent().getStringExtra(KEY_PIC_PATH);
         requestInfo.setPicPath(mPicPath);
         GuoliRequest request = new GuoliRequest(Action.Hotel.HOTEL_PIC, requestInfo);
         Log.i(TAG, "request=" + request.Params.toParams());
         Manager.getInstance().executePoset(request, mListener);
     }
     
     private void updateListView(){
         Log.i(TAG, "updateListView()--->//////.......");
         if (mGridAdapter == null) {
             mGridAdapter = new PicGridAdapter(this, mPicInfos);
             mGridView.setAdapter(mGridAdapter);
             return;
         }
         mGridAdapter.clear();
         mGridAdapter.addMore(mPicInfos);
     }
     
     private IResponseListener mListener = new IResponseListener() {
         
         @Override
         public void onSuccess(Response response) {
             dismissLoadingDialog();
             Log.i(TAG, "response=" + (response == null ? null : response.result));
             HotelPicInfo hotelPicInfo = new HotelPicInfoParse().parseResponse(response);
             if (hotelPicInfo == null) {
                 return;
             }
             mPicInfos = (ArrayList<PicInfo>) hotelPicInfo.getPicInfos();
             updateListView();
         }
         
         @Override
         public void onError(Response response) {
             dismissLoadingDialog();
             Log.i(TAG, "response=" + (response == null ? null : response.result));
         }
     };
 }
