 package com.baixing.widget;
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.MediaStore;
 import android.support.v4.app.DialogFragment;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.Toast;
 
 import com.baixing.broadcast.CommonIntentAction;
 import com.baixing.imageCache.SimpleImageLoader;
 import com.baixing.util.Communication;
 import com.baixing.util.Util;
 import com.baixing.util.ViewUtil;
 import com.baixing.view.fragment.PostGoodsFragment;
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 
 public class ImageSelectionDialog extends DialogFragment implements OnClickListener {
 	public static final String KEY_BITMAP_URL = "bitmapurl";
 	public static final String KEY_CACHED_BPS = "cachedbps";
 	public static final String KEY_THUMBNAIL_URL = "thumbnailurl";
 	public static final String KEY_HANDLER = "handler";
 	private static final int NONE = 0;
 	private static final int MSG_START_UPLOAD = 5;
 	private static final int MSG_FAIL_UPLOAD = 6;
 	private static final int MSG_SUCCEED_UPLOAD = 7;
 	public static final int MSG_IMG_SEL_DISMISSED = 0x11110001;
 	private static final int PHOTORESOULT = 3;
 	
 	private int uploadCount = 0;
     private int currentImgView = -1;
     private ArrayList<String> bitmap_url;
     private ArrayList<String> thumbnail_url;
     private int imgHeight = 0;
     private List<ImageView> imgs;
     private ArrayList<WeakReference<Bitmap> > cachedBps;
     private Bundle bundle;
     private Handler outHandler;
     private boolean pickDlgShown = false;
 
 	enum ImageStatus{
 		ImageStatus_Normal,
 		ImageStatus_Unset,
 		ImageStatus_Failed
 	}
     
     @SuppressWarnings("unchecked")
 	public ImageSelectionDialog(Bundle bundle){
     	this.bundle = bundle;
     	if(bundle != null){
     		bitmap_url = (ArrayList<String>)bundle.getSerializable(KEY_BITMAP_URL);
     		if(bitmap_url != null && bitmap_url.size() > imgIds.length){
     			List<String> list = bitmap_url.subList(0, imgIds.length);
     			bitmap_url = new ArrayList<String>(list);
     		}
     		cachedBps = Util.wrapBitmapWithWeakRef((ArrayList<Bitmap>)bundle.getSerializable(KEY_CACHED_BPS));
     		if(cachedBps != null && cachedBps.size() > imgIds.length){
     			List<WeakReference<Bitmap> > list = cachedBps.subList(0, imgIds.length);
     			cachedBps = new ArrayList<WeakReference<Bitmap> >(list);
     		}
     		if(cachedBps == null){
     			thumbnail_url = (ArrayList<String>)bundle.getSerializable(KEY_THUMBNAIL_URL);
     			if(thumbnail_url != null && thumbnail_url.size() > imgIds.length){
     				List<String> list = thumbnail_url.subList(0, imgIds.length);
     				thumbnail_url = new ArrayList<String>(list);
     			}
     		}
     	}
     }
     
     public void clearResource(){
     	if(bitmap_url != null){
 	        for(int i = 0; i < bitmap_url.size(); ++ i){
 	        	QuanleimuApplication.getImageLoader().forceRecycle(bitmap_url.get(i));
 	        }
 	        bitmap_url.clear();
     	}
         if(thumbnail_url != null){
         	for(int i = 0; i < thumbnail_url.size(); ++ i){
         		QuanleimuApplication.getImageLoader().forceRecycle(thumbnail_url.get(i));
         	}
         	thumbnail_url.clear();
         }
         if(cachedBps != null){
         	for(int i = 0; i < cachedBps.size(); ++ i){
         		if(cachedBps.get(i) != null && cachedBps.get(i).get() != null){
         			cachedBps.get(i).get().recycle();
         		}
         	}
         	cachedBps.clear();
         }
     }
     
     public void setMsgOutHandler(Handler handler){
     	outHandler = handler;
     }
     
     static final private int[] imgIds = {R.id.iv_1, R.id.iv_2, R.id.iv_3, R.id.iv_4, R.id.iv_5, R.id.iv_6};
     
     private void adjustImageCountAfterNew(View v){
     	if(this.currentImgView >= imgIds.length - 1) return;
     	if(currentImgView < 0) return;
     	if(imgs.size() <= currentImgView + 1){
     		imgs.add((ImageView)v.findViewById(imgIds[currentImgView + 1]));
     		v.findViewById(imgIds[currentImgView + 1]).setVisibility(View.VISIBLE);
     	}
 		imgs.get(0).setVisibility(View.VISIBLE);
 		imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.VISIBLE);
     	adjustImageLines();
     }
     
     private void adjustImageCountAfterDel(View v){
 		bitmap_url.remove(currentImgView);
 		cachedBps.remove(currentImgView);
     	if(currentImgView == imgIds.length - 1){
     		((ImageView)imgs.get(currentImgView)).setImageResource(R.drawable.btn_add_picture);
     	}else{
     		for(int i = currentImgView; i < bitmap_url.size(); ++ i){
     			imgs.get(i).setImageBitmap(cachedBps.get(i).get());
     		}
     		if(imgs.size() < imgIds.length || bitmap_url.size() + 1 < imgIds.length){
     			imgs.remove(imgs.size() - 1);
     		}
     		if(imgs.size() > 0){
     			imgs.get(imgs.size() - 1).setImageResource(R.drawable.btn_add_picture);
     		}
     		for(int i = imgs.size(); i < imgIds.length; ++ i){
     			v.findViewById(imgIds[i]).setVisibility(View.INVISIBLE);
     		}
     	}
 		imgs.get(0).setVisibility(View.VISIBLE);
 		imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.VISIBLE);
     	adjustImageLines();
     }    
     
     private Uri uri = null;
     
 	private void getBitmap(Uri uri, int id) {
 		String path = uri == null ? "" : uri.toString();
 		if (uri != null) {
 			if (imgs != null){
 				imgs.get(currentImgView).setFocusable(true);
 			}
 			new Thread(new UpLoadThread(path, currentImgView)).start();
 		}
 	}
     
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		pickDlgShown = false;
 		
 		if (resultCode == NONE) {
 			return;
 		}
 		imgs.get(0).setVisibility(View.VISIBLE);
 		imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.VISIBLE);
 //		this.showPost();
 		
 		// 拍照 
 		if (requestCode == CommonIntentAction.PhotoReqCode.PHOTOHRAPH) {
 			// 设置文件保存路径这里放在跟目录下
 			File picture = new File(Environment.getExternalStorageDirectory(), "temp" + this.currentImgView + ".jpg");
 			uri = Uri.fromFile(picture);
 			getBitmap(uri, requestCode); // 直接返回图片
 			//startPhotoZoom(uri); //截取图片尺寸
 		}
 
 		if (data == null) {
 			return;
 		}
 
 		// 读取相册缩放图片
 		if (requestCode == CommonIntentAction.PhotoReqCode.PHOTOZOOM) {
 			uri = data.getData();
 			//startPhotoZoom(uri);
 			getBitmap(uri, requestCode);
 		}
 		// 处理结果
 		if (requestCode == PHOTORESOULT) {
 			File picture = new File("/sdcard/cropped.jpg");
 			
 			uri = Uri.fromFile(picture);
 			getBitmap(uri, CommonIntentAction.PhotoReqCode.PHOTOHRAPH);
 			File file = new File(Environment.getExternalStorageDirectory(), "temp" + this.currentImgView + "jpg");
 			try {
 				if(file.isFile() && file.exists()){
 					file.delete();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@Override
 	public void onResume(){
 		if(pickDlgShown){
 			if(!imgs.get(0).isShown()){
 				this.dismiss();
 			}			
 		}
 		pickDlgShown = false;
 		super.onResume();
 	}
 	
 	@Override
 	public void onDismiss(DialogInterface dialog){
 		if(outHandler != null){
 			if(this.bundle != null){
 				bundle.putSerializable(KEY_BITMAP_URL, bitmap_url);
 				bundle.putSerializable(KEY_CACHED_BPS, Util.discardWrappedWeakRef(cachedBps));
 			}
 			outHandler.sendEmptyMessage(MSG_IMG_SEL_DISMISSED);
 		}
 		super.onDismiss(dialog);
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		if(savedInstanceState != null){
    		bitmap_url = (ArrayList<String>)bundle.getSerializable(KEY_BITMAP_URL);
    		cachedBps = Util.wrapBitmapWithWeakRef((ArrayList<Bitmap>)bundle.getSerializable(KEY_CACHED_BPS));
 		}
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState){
 		super.onSaveInstanceState(outState);
 		if(outState != null){
 			outState.putSerializable(KEY_BITMAP_URL, bitmap_url);
 			outState.putSerializable(KEY_CACHED_BPS, Util.discardWrappedWeakRef(cachedBps));
 		}
 	}
 
 	public boolean handleBack(){
 		if(this.getDialog() != null && this.getDialog().isShowing()){
 			if(this.getView() != null){
 				View v = getView().findViewById(R.id.post_big);
 				if(v != null && v.isShown()){
 					v.setVisibility(View.INVISIBLE);
 					return true;
 				}
 			}
 			getDialog().dismiss();
 			return true;
 		}
 		return false;
 	}
 	
 	private void adjustImageLines(){
 		if(imgs == null) return;
 		if(imgs.size() <= imgIds.length / 2){
 			for(int i = imgIds.length / 2; i < imgIds.length; ++ i){
 				imgs.get(0).getRootView().findViewById(imgIds[i]).setVisibility(View.GONE);
 			}
 			LinearLayout ll = (LinearLayout)imgs.get(0).getRootView().findViewById(imgIds[imgIds.length / 2]).getParent();
 			ll.setVisibility(View.GONE);
 		}else{
 			LinearLayout ll = (LinearLayout)imgs.get(0).getRootView().findViewById(imgIds[imgIds.length / 2]).getParent();
 			ll.setVisibility(View.VISIBLE);
 
 			for(int i = imgs.size(); i < imgIds.length; ++ i){
 				imgs.get(0).getRootView().findViewById(imgIds[i]).setVisibility(View.INVISIBLE);
 			}			
 		}
 	}
 	
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
     	if(bitmap_url == null){
     		bitmap_url = new ArrayList<String>();
     	}
    		imgs = new ArrayList<ImageView>(imgIds.length);
 		if(cachedBps == null){
 			cachedBps = new ArrayList<WeakReference<Bitmap> >(imgIds.length);
 			if(thumbnail_url != null && thumbnail_url.size() > 0){
 				for(int i = 0; i < thumbnail_url.size(); ++ i){
 					setListContent(cachedBps, null, i);
 				}
 			}
 		}
 		
 		currentImgView = -1;
 		uploadCount = 0;
         
         Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
         LayoutInflater inflater = getActivity().getLayoutInflater();
         View v = inflater.inflate(R.layout.dialog_image_selection, null);
         dialog.setContentView(v);
         for(int i = 0; i < imgIds.length; ++ i){
         	v.findViewById(imgIds[i]).setOnClickListener(this);
         }
         v.findViewById(R.id.btn_finish_sel).setOnClickListener(this);
         int realSize = bitmap_url.size() > imgIds.length ? imgIds.length : bitmap_url.size();
         for(int i = 0; i < realSize; ++ i){
         	ImageView iv = (ImageView)v.findViewById(imgIds[i]);
         	if(cachedBps.get(i) != null){
         		iv.setImageBitmap(cachedBps.get(i).get());
         	}else{
         		iv.setImageResource(R.drawable.icon_post_loading);
         	}
         	iv.setVisibility(View.VISIBLE);
         	imgs.add(iv);
         }
         if(realSize < imgIds.length && realSize > 0){
         	ImageView iv = (ImageView)v.findViewById(imgIds[realSize]);
         	iv.setVisibility(View.VISIBLE);
         	imgs.add(iv);
         }
         
         if(realSize == 0){
         	ImageView iv = (ImageView)v.findViewById(imgIds[0]);
         	iv.setVisibility(View.VISIBLE);
         	imgs.add(iv);
         }
         
         adjustImageLines();
 
         v.setOnClickListener(this);
         dialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
 
 			@Override
 			public boolean onKey(DialogInterface dialog, int keyCode,
 					KeyEvent event) {
 				// TODO Auto-generated method stub
 				if(imgs.get(0).getRootView() != null){
 					View vv = imgs.get(0).getRootView().findViewById(R.id.post_big);
 					if(vv != null && vv.isShown()){
 						if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
 							vv.setVisibility(View.GONE);
 							imgs.get(0).getRootView().findViewById(R.id.img_sel_content).setVisibility(View.VISIBLE);								
 							return true;
 						}
 					}
 				}
 				return false;
 			}
 			
 		});
         v.findViewById(R.id.post_big).setVisibility(View.GONE);
         if(thumbnail_url != null && thumbnail_url.size() > 0){
         	(new DownloadThumbnailsThread(thumbnail_url)).start();
         }
         
         if(bitmap_url.size() == 0){
         	imgs.get(0).post(new Runnable(){
         		@Override
         		public void run(){
         			imgs.get(0).performClick();
         		}
         	});
 
         	imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.INVISIBLE);
         	imgs.get(0).setVisibility(View.INVISIBLE);
         }
 
         return dialog;
     }
         
 	private ImageStatus getCurrentImageStatus(int index){
 		if(bitmap_url.size() <= index || bitmap_url.get(index) == null)return ImageStatus.ImageStatus_Unset;
 		if(bitmap_url.get(index).contains("http:")) return ImageStatus.ImageStatus_Normal; 
 		return ImageStatus.ImageStatus_Failed;
 	}
 	
 	private void pickupPhoto(final int tmpFileIndex){
 		final String[] names = {"拍照","相册"};
 		new AlertDialog.Builder(getActivity()).setTitle("请选择")//.setMessage("无法确定当前位置")
 		.setItems(names, new DialogInterface.OnClickListener(){
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which){
 				
 				Intent backIntent = new Intent();
 				backIntent.setClass(getActivity(), getActivity().getClass());
 				
 				Intent goIntent = new Intent();
 				goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_INTENT, backIntent);
 				switch(which){
 					case 0:
 						goIntent.setAction(CommonIntentAction.ACTION_IMAGE_CAPTURE);
 						goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_REQUST_CODE, CommonIntentAction.PhotoReqCode.PHOTOHRAPH);
 						goIntent.putExtra(CommonIntentAction.EXTRA_IMAGE_SAEV_PATH, "temp" + tmpFileIndex + ".jpg");
 						getActivity().startActivity(goIntent);
 //						Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 //						intent2.putExtra(MediaStore.EXTRA_OUTPUT,
 //								Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp" + tmpFileIndex + ".jpg")));
 //						context.startActivityForResult(intent2, CommonIntentAction.PhotoReqCode.PHOTOHRAPH);
 						break;						
 					case 1:
 						goIntent.setAction(CommonIntentAction.ACTION_IMAGE_SELECT);
 						goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_REQUST_CODE, CommonIntentAction.PhotoReqCode.PHOTOZOOM);
 						getActivity().startActivity(goIntent);
 //						Intent intent3 = new Intent(Intent.ACTION_GET_CONTENT);
 //						intent3.addCategory(Intent.CATEGORY_OPENABLE);
 //						intent3.setType("image/*");
 //						context.startActivityForResult(Intent.createChooser(intent3, "选择图片"), CommonIntentAction.PhotoReqCode.PHOTOZOOM);
 						break;
 
 				}				
 			}
 		})
 		.setNegativeButton("取消", new DialogInterface.OnClickListener(){
 			@Override
 			public void onClick(DialogInterface dialog, int which){
 				dialog.dismiss();
 				if(!imgs.get(0).isShown()){
 					ImageSelectionDialog.this.dismiss();
 				}		
 				pickDlgShown = false;
 			}
 		}).show();			
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		for (int i = 0; i < imgIds.length; i++) {
 			if (imgIds[i] == v.getId()) {
 				currentImgView = i;
 				ImageStatus status = getCurrentImageStatus(i);
 				if(ImageStatus.ImageStatus_Unset == status){
 //					showDialog();
 					pickupPhoto(this.currentImgView);
 					pickDlgShown = true;
 				}
 				else if(ImageStatus.ImageStatus_Failed == status){
 					String[] items = {"重试", "换一张"};
 					new AlertDialog.Builder(getActivity())
 					.setTitle("选择操作")
 					.setItems(items, new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							if(0 == which){
 								new Thread(new UpLoadThread(bitmap_url.get(currentImgView), currentImgView)).start();
 							}
 							else{
 								if (cachedBps.size() > currentImgView && cachedBps.get(currentImgView) != null){
 									if(cachedBps.get(currentImgView) != null && cachedBps.get(currentImgView).get() != null){
 										cachedBps.get(currentImgView).get().recycle();
 									}
 									cachedBps.set(currentImgView, null);
 								}
 								setListContent(bitmap_url, null, currentImgView);
 //								bitmap_url.set(currentImgView, null);
 								imgs.get(currentImgView).setImageResource(R.drawable.btn_add_picture);
 //								showDialog();
 								pickupPhoto(currentImgView);
 								//((BXDecorateImageView)imgs[currentImgView]).setDecorateResource(-1, BXDecorateImageView.ImagePos.ImagePos_LeftTop);
 							}
 							
 						}
 					})
 					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					}).show();
 				}
 				else{
 					imgs.get(0).getRootView().findViewById(R.id.img_sel_content).setVisibility(View.GONE);
 					imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.GONE);
 					imgs.get(0).getRootView().findViewById(R.id.post_big).setVisibility(View.VISIBLE);
 					((ImageView)imgs.get(0).getRootView().findViewById(R.id.iv_post_big_img)).setImageBitmap(null);
 					SimpleImageLoader.showImg(imgs.get(0).getRootView().findViewById(R.id.iv_post_big_img), 
 							bitmap_url.get(currentImgView),
 							"",
 							getActivity());
 					imgs.get(0).getRootView().findViewById(R.id.post_big).setOnClickListener(new OnClickListener(){
 						@Override
 						public void onClick(View v){
 							imgs.get(0).getRootView().findViewById(R.id.post_big).setVisibility(View.GONE);
 							imgs.get(0).getRootView().findViewById(R.id.img_sel_content).setVisibility(View.VISIBLE);
 							imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.VISIBLE);
 						}
 					});
 					imgs.get(0).getRootView().findViewById(R.id.btn_post_del_img).setOnClickListener(new OnClickListener(){
 						@Override
 						public void onClick(View v){
 							imgs.get(0).getRootView().findViewById(R.id.post_big).setVisibility(View.GONE);
 							imgs.get(0).getRootView().findViewById(R.id.img_sel_content).setVisibility(View.VISIBLE);
 							adjustImageCountAfterDel(imgs.get(0).getRootView());
 						}
 					});
 //					new AlertDialog.Builder(this.getActivity())
 //					.setMessage("删除当前图片?")
 //					.setPositiveButton("删除", new DialogInterface.OnClickListener(){
 //						
 //						@Override
 //						public void onClick(DialogInterface dialog, int which) {
 //							bitmap_url.set(currentImgView, null);
 //							imgs.get(currentImgView).setImageResource(R.drawable.btn_add_picture);
 //							cachedBps.set(currentImgView, null);
 //						}
 //					})
 //					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 //						
 //						@Override
 //						public void onClick(DialogInterface dialog, int which) {
 //							dialog.dismiss();
 //						}
 //					}).show();
 				}
 				return;
 			}
 		}
 		if(v.getId() == R.id.btn_finish_sel){
 			this.dismiss();	
 		}		
 	}
 	
 	
 	private String getRealPathFromURI(Uri contentUri) {
 		String[] proj = { MediaStore.Images.Media.DATA };
 		Cursor cursor = getActivity().managedQuery(contentUri, proj, null, null, null);
 
 		if (cursor == null)
 			return null;
 
 		int column_index = cursor
 				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 
 		cursor.moveToFirst();
 
 		String ret = cursor.getString(column_index);
 //		cursor.close();
 		return ret;
 	}
 	
 	Handler handler = new Handler(){
 		@Override
 		public void handleMessage(Message msg) {
 			Activity activity = getActivity();
 //			View rootView = getView();
 			if (activity == null) {
 				return;
 			}
 			switch(msg.what){
 			case MSG_START_UPLOAD:{
 				Integer index = (Integer) msg.obj;
 				if (imgs != null){
 					imgs.get(index.intValue()).setImageResource(R.drawable.icon_post_loading);
 					imgs.get(index).setClickable(false);
 					imgs.get(index.intValue()).invalidate();
 					
 					adjustImageCountAfterNew(imgs.get(0).getRootView());
 				}
 				break;		
 			}
 			case MSG_FAIL_UPLOAD:{
 				if (imgs != null){			
 					Integer index = (Integer) msg.obj;
 					imgs.get(index.intValue()).setImageResource(R.drawable.f);
 					imgs.get(index).setClickable(true);
 					imgs.get(index.intValue()).invalidate();
 				}
 				break;			
 			}
 			case MSG_SUCCEED_UPLOAD:{
 				Integer index = (Integer) msg.obj;
 				if (imgs != null){
 					imgs.get(index).setImageBitmap(cachedBps.get(index).get());
 					imgs.get(index).setClickable(true);
 					imgs.get(index).invalidate();
 				}
 				break;
 			}
 			}
 		}
 		
 	};
 	
 	
 	private static int getClosestResampleSize(int cx, int cy, int maxDim){
         int max = Math.max(cx, cy);
         
         int resample = 1;
         for (resample = 1; resample < Integer.MAX_VALUE; resample++)
         {
             if (resample * maxDim > max)
             {
                 resample--;
                 break;
             }
         }
         
         if (resample > 0)
         {
             return resample;
         }
         return 1;
     }
 	
 	static private Bitmap createThumbnail(Bitmap srcBmp, int thumbHeight){
 		Float width  = new Float(srcBmp.getWidth());
 		Float height = new Float(srcBmp.getHeight());
 		Float ratio = width/height;
 //		Bitmap thumbnail = Bitmap.createScaledBitmap(srcBmp, (int)(thumbHeight*ratio), thumbHeight, true);
 		Bitmap thumbnail = Bitmap.createScaledBitmap(srcBmp, thumbHeight, thumbHeight, true);
 		return thumbnail;
 	}
 	
 	static <T> void setListContent(List<T> container, T obj, int index){
 		if(container.size() > index){
 			container.set(index, obj);
 		}else{
 			container.add(obj);
 		}	
 	}
 	
 	class UpLoadThread implements Runnable {
 		private String bmpPath;
 		private int currentIndex = -1;
 		private WeakReference<Bitmap> thumbnailBmp = null;
 
 		public UpLoadThread(String path, int index) {
 			super();
 			this.bmpPath = path;
 			currentIndex = index;
 		}
 
 		public void run() {
 
 			final Activity activity = getActivity();
 			if (activity == null)
 			{
 				return;
 			}
 			activity.runOnUiThread(new Runnable(){
 				public void run(){
 					if(handler != null){
 						Message msg = Message.obtain();
 						msg.what = MSG_START_UPLOAD;
 						msg.obj = currentIndex;
 						handler.sendMessage(msg);
 					}
 				}
 			});	
 			
 			synchronized(ImageSelectionDialog.this){
 			++ uploadCount;
 			if(bmpPath == null || bmpPath.equals("")) return;
 
 			Uri uri = Uri.parse(bmpPath);
 			String path = getRealPathFromURI(uri); // from Gallery
 			if (path == null) {
 				path = uri.getPath(); // from File Manager
 			}
 			Bitmap currentBmp = null;
 			if (path != null) {
 				try {
 				       
 				    BitmapFactory.Options bfo = new BitmapFactory.Options();
 			        bfo.inJustDecodeBounds = true;
 			        BitmapFactory.decodeFile(path, bfo);
 			        
 				    BitmapFactory.Options o =  new BitmapFactory.Options();
 	                o.inPurgeable = true;
 	                
 	                int maxDim = 600;
 	                
 	                o.inSampleSize = getClosestResampleSize(bfo.outWidth, bfo.outHeight, maxDim);
 	                
 	                
 	                currentBmp = BitmapFactory.decodeFile(path, o);
 					//photo = Util.newBitmap(tphoto, 480, 480);
 					//tphoto.recycle();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}			
 			if(currentBmp == null) {
 				-- uploadCount;
 				return;
 			}
 				
 			String result = Communication.uploadPicture(currentBmp);	
 			-- uploadCount;
 			if(imgs != null && imgs.get(currentIndex) != null && imgs.get(currentIndex).getHeight() != 0){
 				imgHeight = imgs.get(currentIndex).getHeight();
 			}
 			thumbnailBmp = new WeakReference<Bitmap>(createThumbnail(currentBmp, imgHeight == 0 ? 90 : imgHeight));//imgs[currentIndex].getHeight());
 			setListContent(cachedBps, thumbnailBmp, currentIndex);
 //			if(cachedBps.size() > currentIndex){
 //				cachedBps.set(currentIndex, thumbnailBmp);
 //			}else{
 //				cachedBps.add(thumbnailBmp);
 //			}
 			currentBmp.recycle();
 			currentBmp = null;
 	
 			if (result != null) {
 				setListContent(bitmap_url, result, currentIndex);
 //				if(bitmap_url.size() > currentIndex){
 //					bitmap_url.set(currentIndex, result);
 //				}else{
 //					bitmap_url.add(result);
 //				}				
 
 				activity.runOnUiThread(new Runnable(){
 					public void run(){
 						if(handler == null) return;
 						Message msg = Message.obtain();
 						msg.what = MSG_SUCCEED_UPLOAD;
 						msg.obj = currentIndex;
 						handler.sendMessage(msg);
 						Toast.makeText(activity, "上传图片成功", 0).show();
 					}
 				});	                
 			} else {
 //				PostGoods.BXImageAndUrl imgAn dUrl = new PostGoods.BXImageAndUrl();
 				activity.runOnUiThread(new Runnable(){
 					public void run(){
 						setListContent(bitmap_url, bmpPath, currentIndex);
 //						bitmap_url.set(currentIndex, bmpPath);
 						//((BXDecorateImageView)imgs[PostGoods.this.currentImgView]).setDecorateResource(R.drawable.alert_red, BXDecorateImageView.ImagePos.ImagePos_RightTop);
 						if(handler == null) return;
 						Message msg = Message.obtain();
 						msg.what = MSG_FAIL_UPLOAD;
 						msg.obj = currentIndex;
 						handler.sendMessage(msg);
 						Toast.makeText(activity, "上传图片失败", 0).show();
 					}
 				});						
 			}
 //			uploadMutex.notifyAll();
 			}
 		}
 	}
 	
 	class DownloadThumbnailsThread extends Thread{
 		private List<String> urls = new ArrayList<String>();
 		public DownloadThumbnailsThread(List<String> urls){
 			this.urls.addAll(urls);
 		}
 		@Override
 		public void run(){
 			if(urls.size() == 0) return;
 			synchronized(ImageSelectionDialog.this){
 				int size = cachedBps.size() < urls.size() ? cachedBps.size() : urls.size();
 				for(int i = 0; i < size; ++ i){
 					if(cachedBps.get(i) == null){
 						WeakReference<Bitmap> bmp = new WeakReference<Bitmap>(Util.getImage(urls.get(i)));
 						setListContent(cachedBps, bmp, i);
 					}
 				}
 				getActivity().runOnUiThread(new Runnable(){
 					@Override
 					public void run(){
 						for(int i = 0; i < cachedBps.size(); ++ i){
 							((ImageView)imgs.get(i)).setImageBitmap(cachedBps.get(i).get());
 						}
 					}
 				});
 			}
 		}
 	}
 }
