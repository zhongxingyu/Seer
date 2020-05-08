 package edu.grinnell.sandb.img;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.view.View;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 
 import com.nostra13.universalimageloader.core.DisplayImageOptions;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 import com.nostra13.universalimageloader.core.assist.FailReason;
 import com.nostra13.universalimageloader.core.assist.ImageScaleType;
 import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
 import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
 
 import edu.grinnell.grinnellsandb.R;
 import edu.grinnell.sandb.data.Article;
 import edu.grinnell.sandb.data.ImageTable;
 
 public class UniversalLoaderUtility {
 
 	protected ImageLoader imageLoader = ImageLoader.getInstance();
 	protected ProgressBar spinner = null;
 
 	public UniversalLoaderUtility() {
 	}
 
 	public ImageLoader getImageLoader() {
 		return imageLoader;
 	}
 
 	protected SimpleImageLoadingListener listener = new SimpleImageLoadingListener() {
 
 		@Override
 		public void onLoadingFailed(String imageUri, View view,
 				FailReason failReason) {
 			@SuppressWarnings("unused")
 			String message = null;
 			switch (failReason.getType()) {
 			case IO_ERROR:
 				message = "Input/Output error";
 				break;
 			case DECODING_ERROR:
 				message = "Image can't be decoded";
 				break;
 			case NETWORK_DENIED:
 				message = "Downloads are denied";
 				break;
 			case OUT_OF_MEMORY:
 				message = "Out Of Memory error";
 				break;
 			case UNKNOWN:
 				message = "Unknown error";
 				break;
 			}
 		}
 	};
 
 	// load image based on URL
 	public void loadImage(String imgUrl, ImageView imgView, Context context) {
 
 		DisplayImageOptions options;
 
 		options = new DisplayImageOptions.Builder()
 				.imageScaleType(ImageScaleType.EXACTLY)
 				.showImageForEmptyUri(R.drawable.sandblogo)
 				.showImageForEmptyUri(R.drawable.sandblogo)
 				.showImageOnFail(R.drawable.sandblogo).resetViewBeforeLoading()
 				.cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY)
 				.bitmapConfig(Bitmap.Config.RGB_565)
 				.displayer(new FadeInBitmapDisplayer(300)).build();
 
 		spinner = new ProgressBar(context, null,
 				android.R.attr.progressBarStyleSmall);
 
 		ImageLoaderConfiguration configuration = ImageLoaderConfiguration
 				.createDefault(imgView.getContext().getApplicationContext());
 		imageLoader.init(configuration);
 		imageLoader.displayImage(imgUrl, imgView, options, listener);
 	}
 
 	// load first image from an article, in low res
 	public void loadArticleImage(Article a, ImageView imgView, Context context) {
 		ImageTable imgTable = new ImageTable(imgView.getContext());
 		imgTable.open();
 
 		int id = a.getId();
 		String[] URLS = imgTable.findURLSbyArticleId(id);
 		imgTable.close();
 
 		// try {
 		if (URLS != null) {
 			// throw exception if no image
 			String imgUrl = URLS[0];
 
 			DisplayImageOptions options;
 
 			options = new DisplayImageOptions.Builder()
 					.imageScaleType(ImageScaleType.EXACTLY)
 					// change these images to error messages
 					.showImageOnFail(R.drawable.sandblogo)
 					.resetViewBeforeLoading().cacheOnDisc()
 					.imageScaleType(ImageScaleType.EXACTLY)
 					.bitmapConfig(Bitmap.Config.RGB_565)
 					.displayer(new FadeInBitmapDisplayer(300)).build();
 
 			ImageLoaderConfiguration configuration = ImageLoaderConfiguration
 					.createDefault(imgView.getContext().getApplicationContext());
 			imageLoader.init(configuration);
 			imageLoader.displayImage(imgUrl, imgView, options, listener);
 			imgView.setVisibility(View.VISIBLE);
 
 		}
 		// catch (NullPointerException e) {
 		else {
 			imgTable.close();
 			// imageLoader.displayImage(null, imgView, null, listener);
 			imgView.setVisibility(View.GONE);
 		}
 	}
 
 	// load first image from an article, in low res
 	public void loadHiResArticleImage(Article a, ImageView imgView,
 			Context context) {
 
 		ImageTable imgTable = new ImageTable(imgView.getContext());
 		imgTable.open();
 
 		int id = a.getId();
 		String[] URLS = imgTable.findURLSbyArticleId(id);
 
 		try {
 			// throw exception if no image
 			String imgUrl = URLS[0];
 			String hiResImgUrl = getHiResImage(imgUrl);
 
 			DisplayImageOptions options;
 
 			options = new DisplayImageOptions.Builder()
 					.imageScaleType(ImageScaleType.EXACTLY)
 					// change these images to error messages
 					.showStubImage(R.drawable.loading)
 					.showImageOnFail(R.drawable.sandblogo)
 					.resetViewBeforeLoading().cacheOnDisc()
 					.imageScaleType(ImageScaleType.EXACTLY)
 					.bitmapConfig(Bitmap.Config.RGB_565)
 					.displayer(new FadeInBitmapDisplayer(300)).build();
 
 			imgView.startAnimation(AnimationUtils.loadAnimation(context,
 					R.anim.loading));
 
 			spinner = new ProgressBar(context, null,
 					android.R.attr.progressBarStyleSmall);
 
 			ImageLoaderConfiguration configuration = ImageLoaderConfiguration
 					.createDefault(imgView.getContext().getApplicationContext());
 			imageLoader.init(configuration);
 			imageLoader.displayImage(hiResImgUrl, imgView, options, listener);
 			imgView.setVisibility(View.VISIBLE);
 			imgTable.close();
 
 		} catch (NullPointerException e) {
 			imgTable.close();
 			// imageLoader.displayImage(null, imgView, null, listener);
 			imgView.setVisibility(View.GONE);
 		}
 	}
 
 	// remove the ends of each image URL to download full sized images
 	public String getHiResImage(String lowResImg) {
 		// add "contains" for error testing
 
 		if (lowResImg == null) {
 			return null;
 		}
 
 		int readTo = lowResImg.lastIndexOf("-");
 
 		if (readTo != -1) {
 			String hiResImg = lowResImg.substring(0, readTo);
 			hiResImg = hiResImg.concat(".jpg");
 			return hiResImg;
 		} else
 			return lowResImg;
 	}
 }
