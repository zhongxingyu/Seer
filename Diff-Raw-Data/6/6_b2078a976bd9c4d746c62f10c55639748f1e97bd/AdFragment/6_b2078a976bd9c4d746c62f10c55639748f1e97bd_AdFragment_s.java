 package com.saltlakegreekfestival.utahgreekfestival;
 
 import java.io.File;
 
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 
 public class AdFragment extends Fragment {
 	
 	Drawable myImage = null;
 	File imageFile = null;
 	String URL = null;
 	//int myImageId = R.drawable.roofers;
 	int myImageId = 0;
 	String TAG = "AdFragment";
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.adfragmentlayout, null);
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		Log.v(TAG,"Created ImageId: "+String.valueOf(myImageId));
 		ImageView image = (ImageView)this.getView().findViewById(R.id.advertimage);
 		if(imageFile != null)
 			image.setImageBitmap(BitmapFactory.decodeFile(imageFile.getPath()));
 		else {
			this.myImage = this.getActivity().getResources().getDrawable(myImageId);
			image.setImageDrawable(this.myImage);
 		}
 		if(URL != null){
 			image.setOnClickListener(new View.OnClickListener(){
 			    public void onClick(View v){
 			        Intent intent = new Intent();
 			        intent.setAction(Intent.ACTION_VIEW);
 			        intent.addCategory(Intent.CATEGORY_BROWSABLE);
 			        intent.setData(Uri.parse(URL));
 			        startActivity(intent);
 			    }
 			});
 		}
 		
 		
 	}
 
 	public AdFragment() {
 		
 	}
 	
 	public ImageView getImageView(){
 		if(this.getView() == null)
 			return null;
 		else
 			return (ImageView)this.getView().findViewById(R.id.advertimage);
 	}
 	public void setDrawable(Drawable myImage){
 		//ImageView image = (ImageView)myLayout.findViewById(R.id.advertimage);
 		//image.setImageDrawable(myImage);
 	}
 	
 	public void setDrawable(int myImage){
 		Log.v(TAG,"Image Id:"+String.valueOf(myImage));
 		this.myImageId = myImage;
 
 		Log.v(TAG,"Image Id:"+String.valueOf(this.myImageId));
 	}
 	
 	public Drawable getDrawable(){
 		return this.myImage;
 	}
 
 	public void setDrawableFile(File adImage) {
 		this.imageFile = adImage;
 		
 	}
 	
 	public void setUrl(String url){
 		this.URL = url;
 	}
 	
 	public String getUrl(){
 		return URL;
 	}
 
 }
