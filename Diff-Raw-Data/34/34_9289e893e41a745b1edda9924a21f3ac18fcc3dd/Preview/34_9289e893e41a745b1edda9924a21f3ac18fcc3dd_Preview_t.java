 package com.digitalnatura.dialogmanager;
 
 import com.digitalnatura.R;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.Fragment;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebSettings.ZoomDensity;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 public final class Preview extends Fragment {
 
 	private static WebView viewer;
 
 	public static Preview newInstance() {
 		Preview fragment = new Preview();
 
 		return fragment;
 	}
 
 	private WebSettings settings;
 	private static int scrollX;
 	private static int scrollY;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		viewer = (WebView) inflater
 				.inflate(R.layout.tut_view, container, false);
 		settings = viewer.getSettings();
 		
 	
 		viewer.setInitialScale(98);
 
 
 //		settings.setDefaultZoom(ZoomDensity.FAR);
 		settings.setBuiltInZoomControls(true);
 		settings.setJavaScriptEnabled(true);
 
 		return (viewer);
 
 		// return layout;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
		outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
 		super.onSaveInstanceState(outState);
 		scrollX = viewer.getScrollX();
 		scrollY = viewer.getScrollY();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		viewer.loadUrl("file://" + Environment.getExternalStorageDirectory()
 				+ "/myScreenplays/" + "partial_preview" + ".xhtml");
 		
 		
 		
 
 		viewer.reload();
 
 	}
 
 	public static void recargar() {
 		viewer.loadUrl("file://" + Environment.getExternalStorageDirectory()
 				+ "/myScreenplays/" + "partial_preview" + ".xhtml");
 		// viewer.loadUrl( "javascript:window.location.reload( true )" );
 		viewer.reload();
 		viewer.scrollTo(scrollX, scrollY);
 
 	}
 }
