 package com.bytopia.oboobs.fragments;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.ImageView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.internal.widget.IcsProgressBar;
 import com.bytopia.oboobs.DownloadService;
 import com.bytopia.oboobs.ImageReceiver;
 import com.bytopia.oboobs.OboobsApp;
 import com.bytopia.oboobs.R;
 import com.bytopia.oboobs.model.Boobs;
 
 public class BoobsFragment extends SherlockFragment {
 
 	private ImageView imageView;
 	private IcsProgressBar progressBar;
 	OboobsApp app;
 
 	boolean fs = false;
 
 	private static final int SENDER_TYPE = 23;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		app = (OboobsApp) activity.getApplication();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.boobs_fragment_layout, container,
 				false);
 
 		imageView = (ImageView) view.findViewById(R.id.image);
 		progressBar = (IcsProgressBar) view.findViewById(R.id.progressBar);
 
 		imageView.setOnClickListener(new OnClickListener() {
 
 			@TargetApi(11)
 			@Override
 			public void onClick(View v) {
 				fs = !fs;
 				updateFullscreen();
 			}
 
 		});
 
 		updateFullscreen();
 
 		return view;
 	}
 
 	private void updateFullscreen() {
 		if (fs) {
 			getActivity().getWindow().setFlags(
 					WindowManager.LayoutParams.FLAG_FULLSCREEN,
 					WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			getSherlockActivity().getSupportActionBar().hide();
 			if (Build.VERSION.SDK_INT > 10) {
 				imageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
 			}
 		} else {
 			getActivity().getWindow().clearFlags(
 					WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			getSherlockActivity().getSupportActionBar().show();
 			if (Build.VERSION.SDK_INT > 10) {
 				imageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
 			}
 		}
 	}
 
 	public void setBoobs(Boobs boobs) {
 		DownloadService.requestImage(getActivity(), SENDER_TYPE, boobs, false,
 				0, 0);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		app.setCurentReceiver(mImageReceiver);
 	}
 
 	private ImageReceiver mImageReceiver = new ImageReceiver() {
 
 		@Override
 		public void receiveImage(int imageId, Bitmap bitmap) {
 			imageView.setImageBitmap(bitmap);
 			progressBar.setVisibility(View.GONE);
 		}
 
 		@Override
 		public int getSenderType() {
 			return SENDER_TYPE;
 		}
 	};
 
 }
