 package it.giacomos.android.osmer.widgets.map;
 
 import it.giacomos.android.osmer.R;
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
 import com.google.android.gms.maps.model.Marker;
 
 public class WebcamBaloonInfoWindowAdapter implements InfoWindowAdapter {
 	
 	private View mView;
 	private boolean mImageValid;
 		
 	public void finalize()
 	{
 		ImageView imageView = (ImageView )mView.findViewById(R.id.baloon_icon);
 		if(imageView == null)
 			return;
 		Drawable drawable = imageView.getDrawable();
 		if(drawable == null)
 			return;
 		Bitmap currentBitmap = ((BitmapDrawable) drawable).getBitmap();
 		if(currentBitmap != null)
 		{
			/* try to avoid recycling: issues reported in Play store */
//			if(!currentBitmap.isRecycled())
//				currentBitmap.recycle();	
//			currentBitmap = null;
 		}
 		if(drawable != null)
 		{
 			drawable.setCallback(null);
 		}
 	}
 	
 	public WebcamBaloonInfoWindowAdapter(Activity activity) 
 	{
 		mView = activity.getLayoutInflater().inflate(R.layout.webcam_mapbaloon, null);
 		mImageValid = false;
 	}
 	
 	/* invoked by WebcamOverlay when the BitmapTask (async task) has been completed 
 	 * successfully.
 	 */
 	public void setData(String text, Bitmap bmp, boolean validWebcamImage)
 	{
 		mImageValid = validWebcamImage;
 		((TextView) mView.findViewById(R.id.baloon_text)).setText(text);
 		((ImageView) mView.findViewById(R.id.baloon_icon)).setImageBitmap(bmp);
 	}
 	
 	public boolean isImageValid()
 	{
 		return mImageValid;
 	}
 	
 	@Override
 	public View getInfoContents(Marker marker) 
 	{
 		return mView;
 	}
 
 	@Override
 	public View getInfoWindow(Marker marker) 
 	{
 		return null;
 	}
 
 }
