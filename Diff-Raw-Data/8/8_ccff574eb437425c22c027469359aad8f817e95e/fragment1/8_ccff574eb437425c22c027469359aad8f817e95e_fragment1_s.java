 package com.group15.djhero;
 
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Bitmap.Config;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.RotateAnimation;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.ImageView.ScaleType;
 
 public class fragment1 extends Fragment implements OnClickListener {
 
 	MyApplication myApp;
 	ImageButton imageButton_add;
 	ImageButton imageButton_ff;
 	ImageButton	imageButton_rew;
 	ImageButton imageButton_forward;
 	ImageButton imageButton_rewind;
 	View V;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 	        Bundle savedInstanceState) {
 		// Inflate the layout for this fragment
 		V = inflater.inflate(R.layout.fragment_test, container, false);
 		
 		imageButton_add = (ImageButton) V.findViewById(R.id.imageButton_add);
 		imageButton_ff = (ImageButton) V.findViewById(R.id.imageButton_ff);
 		imageButton_rew = (ImageButton) V.findViewById(R.id.imageButton_rew);
 		imageButton_forward = (ImageButton) V.findViewById(R.id.imageButton_forward);
 		imageButton_rewind = (ImageButton) V.findViewById(R.id.imageButton_rewind);
 		
 		imageButton_add.setOnClickListener(this);
 		imageButton_ff.setOnClickListener(this);
 		imageButton_rew.setOnClickListener(this);
 		imageButton_forward.setOnClickListener(this);
 		imageButton_rewind.setOnClickListener(this);
		
 		SwipeDetector gesture = new SwipeDetector(this);
 		ImageView currentLayout = (ImageView) V.findViewById(R.id.imageView1);
 		currentLayout.setOnTouchListener(gesture);
 		return V;
 	}
 
 	public void turncw(){
 		Animation rotate = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
 		V.findViewById(R.id.imageView1).startAnimation(rotate);
 		rotate.reset();
 		rotate.start();
 	}
 	
 	public void turnccw(){
 		Animation rotate = AnimationUtils.loadAnimation(getActivity(), R.anim.reverserotate);
 		V.findViewById(R.id.imageView1).startAnimation(rotate);
 		rotate.reset();
 		rotate.start();
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		myApp = (MyApplication) getActivity().getApplication();
 		TextView textViewforSongPosition = (TextView) getView().findViewById(R.id.songNameFrag);
 		textViewforSongPosition.setText(myApp.songSelectedRight.Title);
 	}
 
 	@Override
 	public void onClick(View V) {
 
 		switch (V.getId()) {
 			case R.id.imageButton_add:
 				FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
 				fragment_list fl = new fragment_list();
 				fragmentTransaction.replace(R.id.fragment_container2, fl);
 				fragmentTransaction.addToBackStack(null);
 				fragmentTransaction.commit();
 				break;
 			
 			case R.id.imageButton_ff:
 				myApp.leftSpeed = (myApp.leftSpeed +1) % 3;
 				SendMessage.sendMessage("t "+ String.valueOf(myApp.leftSpeed)+" "+String.valueOf(myApp.rightSpeed), myApp.sock);
 				break;
 				
 			case R.id.imageButton_rew:
 				if(myApp.leftSpeed > 0){
 				myApp.leftSpeed = myApp.leftSpeed -1;
 				}
 				SendMessage.sendMessage("t "+ String.valueOf(myApp.leftSpeed)+" "+String.valueOf(myApp.rightSpeed), myApp.sock);
 				break;
 			
 			case R.id.imageButton_forward:
 				SendMessage.sendMessage("y 1 0", myApp.sock);
 				break;
 				
 			case R.id.imageButton_rewind:
 				SendMessage.sendMessage("w 1 0", myApp.sock);
 				break;
 
 		}
 	}
 	
 	public void rewindFrag1(){
 		SendMessage.sendMessage("w 1 0", myApp.sock);
 	}
 	
 	public  void forwardFrag1(){
 		SendMessage.sendMessage("y 1 0", myApp.sock);
 	}
 	public void makeMaskImage(ImageView mImageView, int mContent)
 	{
 		Bitmap original = BitmapFactory.decodeResource(getResources(), mContent);
 		Bitmap mask = BitmapFactory.decodeResource(getResources(),R.drawable.mask);
 		Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Config.ARGB_8888);
 		Canvas mCanvas = new Canvas(result);
 		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
 		mCanvas.drawBitmap(original, 0, 0, null);
 		mCanvas.drawBitmap(mask, 0, 0, paint);
 		paint.setXfermode(null);
 		mImageView.setImageBitmap(result);
 		mImageView.setScaleType(ScaleType.CENTER);
 		mImageView.setBackgroundResource(R.drawable.frame);
 	}
 
 }
