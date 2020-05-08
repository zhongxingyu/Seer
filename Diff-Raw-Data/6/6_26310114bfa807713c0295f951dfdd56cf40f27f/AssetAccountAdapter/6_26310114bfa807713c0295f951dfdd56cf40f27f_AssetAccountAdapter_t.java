 /*
  *  Copyright (c) 2012 Chute Corporation
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package com.chute.android.photopickerplus.ui.adapter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import android.content.Context;
 import android.support.v4.app.FragmentActivity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.araneaapps.android.libs.logger.ALog;
 import com.chute.android.photopickerplus.R;
 import com.chute.android.photopickerplus.ui.activity.AssetActivity;
 import com.chute.android.photopickerplus.ui.activity.ServicesActivity;
 import com.chute.android.photopickerplus.util.AppUtil;
 import com.chute.sdk.v2.model.AccountAlbumModel;
 import com.chute.sdk.v2.model.AccountBaseModel;
 import com.chute.sdk.v2.model.AccountMediaModel;
 import com.chute.sdk.v2.model.enums.AccountMediaType;
 import com.chute.sdk.v2.model.interfaces.AccountMedia;
 
 import darko.imagedownloader.ImageLoader;
 
 public class AssetAccountAdapter extends BaseAdapter implements AssetSelectListener {
 
   @SuppressWarnings("unused")
   private static final String TAG = AssetAccountAdapter.class.getSimpleName();
 
   private static final int TYPE_MAX_COUNT = 2;
 
   private static LayoutInflater inflater;
   public ImageLoader loader;
   public HashMap<Integer, AccountMediaModel> tick;
   private final FragmentActivity context;
   private List<AccountMedia> rows;
   private AdapterItemClickListener adapterItemClickListener;
 
   public interface AdapterItemClickListener {
 
     public void onFolderClicked(int position);
 
     public void onFileClicked(int position);
   }
 
   public AssetAccountAdapter(FragmentActivity context, AccountBaseModel baseModel,
       AdapterItemClickListener adapterItemClicklistener) {
     this.context = context;
     this.adapterItemClickListener = adapterItemClicklistener;
     inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     loader = ImageLoader.getLoader(context.getApplicationContext());
     tick = new HashMap<Integer, AccountMediaModel>();
     rows = new ArrayList<AccountMedia>();
 
     if (baseModel.getFiles() != null) {
       for (AccountMediaModel file : baseModel.getFiles()) {
         rows.add(file);
       }
     }
     if (baseModel.getFolders() != null) {
       for (AccountAlbumModel folder : baseModel.getFolders()) {
         rows.add(folder);
       }
     }
     if (context.getResources().getBoolean(R.bool.has_two_panes)) {
       ((ServicesActivity) context).setAssetSelectListener(this);
     } else {
       ((AssetActivity) context).setAssetSelectListener(this);
     }
   }
 
   @Override
   public int getViewTypeCount() {
     return TYPE_MAX_COUNT;
   }
 
   @Override
   public int getItemViewType(int position) {
     return rows.get(position).getViewType().ordinal();
   }
 
   public int getCount() {
     return rows.size();
   }
 
   public AccountMedia getItem(int position) {
     return rows.get(position);
   }
 
   public long getItemId(int position) {
     return position;
   }
 
   public static class ViewHolder {
 
     public ImageView imageViewThumb;
     public ImageView imageViewTick;
     public TextView textViewFolderTitle;
   }
 
   @SuppressWarnings("deprecation")
   @Override
   public View getView(final int position, View convertView, final ViewGroup parent) {
     ViewHolder holder;
     int type = getItemViewType(position);
     if (convertView == null) {
       convertView = inflater.inflate(R.layout.adapter_assets, null);
       holder = new ViewHolder();
       holder.imageViewThumb = (ImageView) convertView.findViewById(R.id.imageViewThumb);
       holder.imageViewTick = (ImageView) convertView.findViewById(R.id.imageViewTick);
       holder.imageViewTick.setTag(position);
       holder.textViewFolderTitle = (TextView) convertView.findViewById(R.id.textViewFolderTitle);
       convertView.setTag(holder);
     } else {
       holder = (ViewHolder) convertView.getTag();
     }
 
     holder.imageViewThumb.setTag(position);
     if (type == AccountMediaType.FOLDER.ordinal()) {
       holder.imageViewTick.setVisibility(View.GONE);
       holder.textViewFolderTitle.setVisibility(View.VISIBLE);
       String folderName = getItem(position).getName();
       holder.textViewFolderTitle.setText(folderName != null ? folderName : "");
       holder.imageViewThumb.setBackgroundDrawable(context.getResources().getDrawable(
           R.drawable.album_default));
       holder.imageViewThumb.setOnClickListener(new OnFolderClickedListener());
     } else if (type == AccountMediaType.FILE.ordinal()) {
       holder.imageViewTick.setVisibility(View.VISIBLE);
       ALog.e(getItem(position).getThumbnail());
        loader.displayImage(getItem(position).getThumbnail(),
          holder.imageViewThumb, null);
       holder.imageViewThumb.setOnClickListener(new OnFileClickedListener());
     }
     AppUtil.configureAssetImageViewDimensions(context, holder.imageViewThumb);
 
     if (tick.containsKey(position)) {
       holder.imageViewTick.setVisibility(View.VISIBLE);
       convertView.setBackgroundColor(context.getResources().getColor(R.color.sky_blue));
     } else {
       holder.imageViewTick.setVisibility(View.GONE);
       convertView.setBackgroundColor(context.getResources().getColor(R.color.gray_light));
     }
     return convertView;
   }
 
   public ArrayList<AccountMediaModel> getPhotoCollection() {
     final ArrayList<AccountMediaModel> photos = new ArrayList<AccountMediaModel>();
     final Iterator<AccountMediaModel> iterator = tick.values().iterator();
     while (iterator.hasNext()) {
       photos.add(iterator.next());
     }
     return photos;
   }
 
   public boolean hasSelectedItems() {
     return tick.size() > 0;
   }
 
   public int getSelectedItemsCount() {
     return tick.size();
   }
 
   public void toggleTick(final int position) {
     if (getCount() >= position) {
       if (getItemViewType(position) == AccountMediaType.FILE.ordinal()) {
         if (tick.containsKey(position)) {
           tick.remove(position);
         } else {
           tick.put(position, (AccountMediaModel) getItem(position));
         }
       }
     }
     notifyDataSetChanged();
   }
 
   private final class OnFolderClickedListener implements OnClickListener {
 
     @Override
     public void onClick(View v) {
       Integer position = (Integer) v.getTag();
       adapterItemClickListener.onFolderClicked(position);
 
     }
 
   }
 
   private final class OnFileClickedListener implements OnClickListener {
 
     @Override
     public void onClick(View v) {
       Integer position = (Integer) v.getTag();
       adapterItemClickListener.onFileClicked(position);
 
     }
 
   }
 
   @Override
   public ArrayList<Integer> getSelectedItemPositions() {
     final ArrayList<Integer> positions = new ArrayList<Integer>();
     final Iterator<Integer> iterator = tick.keySet().iterator();
     while (iterator.hasNext()) {
       positions.add(iterator.next());
     }
     return positions;
   }
 }
