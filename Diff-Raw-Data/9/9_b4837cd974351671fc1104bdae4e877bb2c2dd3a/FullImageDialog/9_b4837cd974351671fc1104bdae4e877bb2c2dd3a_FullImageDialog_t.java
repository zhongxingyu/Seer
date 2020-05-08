 package com.google.code.easyshopper.activities.product;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 
 import com.google.code.easyshopper.R;
 import com.google.code.easyshopper.domain.CartProduct;
 
 public class FullImageDialog extends Dialog {
 
 	private final CartProduct cartProduct;
 	private ImageView imageView;
 	private final GrabImageLauncher grabImageLauncher;
 	private final Activity activity;
 
 	public FullImageDialog(CartProduct cartProduct, Activity activity, GrabImageLauncher grabImageLauncher) {
 		super(activity);
 		this.cartProduct = cartProduct;
 		this.activity = activity;
 		this.grabImageLauncher = grabImageLauncher;
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.full_image_dialog);
 		setTitle(cartProduct.getProduct().getName());
 		Bitmap productBitmap = cartProduct.getProduct().getImage().getFullBitmap();
 		
 		imageView = (ImageView) findViewById(R.id.FullImageView);
 		imageView.setImageBitmap(productBitmap);
	}
	@Override
	protected void onStart() {
		super.onStart();
 		((Button) findViewById(R.id.CloseImageDialogButton)).setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				FullImageDialog.this.dismiss();
 			}
 		});
 		((Button) findViewById(R.id.TakeNewPictureButton)).setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				grabImageLauncher.launchFor(cartProduct, activity);
 				FullImageDialog.this.dismiss();
 			}
 			
 		});
 	}
 	
 	@Override
 	protected void onStop() {
 		super.onStop();
 		((BitmapDrawable)imageView.getDrawable()).getBitmap().recycle();
 		imageView.setImageBitmap(null);
 	}
 }
