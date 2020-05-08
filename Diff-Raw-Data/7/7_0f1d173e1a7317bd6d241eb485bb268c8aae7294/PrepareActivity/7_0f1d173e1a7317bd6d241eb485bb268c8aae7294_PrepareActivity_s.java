 package org.fruct.oss.tourme;
 
 import android.annotation.SuppressLint;
 import android.app.Fragment;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class PrepareActivity extends FragmentActivity {
 
 	static View.OnClickListener nextButtonListener = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_prepare);
 		getActionBar().hide();
 		
 		if (savedInstanceState == null) {
 			PrepareOneFragment f = new PrepareOneFragment();
 			getFragmentManager().beginTransaction().replace(R.id.fragment_container_prepare, f, "TESTTAG").commit();
 		} else {
 			
 		}
 		
 		// OnClickListener for 'Next' button in each fragment
 		nextButtonListener = new View.OnClickListener() {
 						
 			@Override
 			public void onClick(View v) {
 				Fragment f = null;
 				
 				switch(v.getId()) {
 				case(R.id.prepare_1_reject):
 					// If user rejects downloading offline data, proceed to MainActivity with online mode
 					SharedPreferences sh = getSharedPreferences(ConstantsAndTools.SHARED_PREFERENCES, 0);
 					SharedPreferences.Editor ed = sh.edit();
 					ed.putBoolean(ConstantsAndTools.ONLINE_MODE, true);					
 					finish();
 					break;
 				case(R.id.prepare_1_next):
 					f = new PrepareTwoFragment();
 					break;
 				case(R.id.prepare_2_next):
 					f = new PrepareThreeFragment();
 					break;
 				case(R.id.prepare_3_next):
 					finish(); // TODO
 					break;
 				default:
 					break;
 				}
 				
 				if (f != null)
 					getFragmentManager().beginTransaction().replace(R.id.fragment_container_prepare, f).commit();
 			}
 		};
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		//getMenuInflater().inflate(R.menu.prepare, menu);
 		return true;
 	}
 	
 	
 	/**
 	 * Second screen of Welcome\prepare
 	 * Welcome text, Internet connection check
 	 * @author alexander
 	 *
 	 */
 	public static class PrepareOneFragment extends Fragment {
 		
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 			
 			View view = inflater.inflate(R.layout.fragment_prepare_1, container, false);
 			return view;
 		}
 		
 		@Override
 		public void onViewCreated(View view, Bundle savedInstanceState) {
 			
 			// Set value of network availability
 			Boolean hasNetwork = ConstantsAndTools.isOnline(getActivity());
 			TextView networkState = (TextView) view.findViewById(R.id.prepare_1_network);
 			Button btnNext = (Button) view.findViewById(R.id.prepare_1_next);
 			Button btnReject = (Button) view.findViewById(R.id.prepare_1_reject);
 			btnReject.setOnClickListener(nextButtonListener);
 			
 			if (hasNetwork) {
 				networkState.setText(
 						getResources().getString(R.string.prepare_1_network) + " " +
 								getResources().getString(R.string.available));
 				
 				btnNext.setEnabled(true);
 				btnNext.setOnClickListener(nextButtonListener);
 			} else {
 				networkState.setText(
 						getResources().getString(R.string.prepare_1_network) + " " +
 								getResources().getString(R.string.unavailable) + "\n\n" +
 								getResources().getString(R.string.no_network));				
 				
 				btnNext.setEnabled(false);
 			}	
 			
 		}
 	}
 	
 	
 	/**
 	 * Second screen of Welcome\prepare
 	 * Region selector
 	 * @author alexander
 	 *
 	 */
 	public static class PrepareTwoFragment extends Fragment {
 		
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 			
 			View view = inflater.inflate(R.layout.fragment_prepare_2, container, false);
 			return view;
 		}
 		
 		@SuppressLint("SetJavaScriptEnabled")
 		@Override
 		public void onViewCreated(View view, Bundle savedInstanceState) {
 			
 			// TODO: provide button to delete all markers
 			
 			// TODO: prepare mode selection
 			final WebView webView = (WebView) view.findViewById(R.id.prepare_2_webview);
 			webView.getSettings().setJavaScriptEnabled(true);
 			webView.loadUrl("file:///android_asset/map.html");
 			
 			webView.setWebViewClient(new WebViewClient() {
 
 				   public void onPageFinished(WebView view, String url) {
 					   webView.loadUrl("javascript:setOnlineLayer();"); // Online mode ONLY (preparing mode, no cache)
 					   webView.loadUrl("javascript:setPrepareMode(true);"); // Enable touch-to-add-point
 				    }
 			});
 			
			
 			// Seekbar only has maximum value (see layout for definition, e.g. android:max="300" (kms))
 			SeekBar s = (SeekBar) view.findViewById(R.id.prepare_2_seekbar);
 			s.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
 				@Override
 				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				}
 
 				@Override
 				public void onStartTrackingTouch(SeekBar seekBar) {
 				}
 
 				@Override
 				public void onStopTrackingTouch(SeekBar seekBar) {
 					// 'Progress' varies from 0 to value in layout
 					// We need to provide radius from 10km to 300 km
 					int progress = seekBar.getProgress();
 					if (progress < 20)
 						progress = 20;
 					webView.loadUrl("javascript:setRadius(" + progress + ");");
				}
				
 			});
 			
 			// Set up the webView size (depends on screen height)
 			WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
 			Display display = wm.getDefaultDisplay();
 			Point size = new Point();
 			display.getSize(size);
 			int height = size.y;
 			
 			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) webView.getLayoutParams();
 			params.height = Math.round(height/2); // 0.5 from the screen size
 			webView.setLayoutParams(params);
 			
 			// TODO: add JS-interface (see MapFragment.java)
 			
 			// TODO: minimum 1 region must be presented to show button
 			Button btnNext = (Button) view.findViewById(R.id.prepare_2_next);
 			btnNext.setEnabled(true);
 			btnNext.setOnClickListener(nextButtonListener);
 		}
 		
 		// TODO: a method to count approx. size of archives to download NEEDED?
 	}
 	
 	/**
 	 * Third screen of Welcome\prepare
 	 * Downloading, unpacking and preparing items
 	 * @author alexander
 	 *
 	 */
 	public static class PrepareThreeFragment extends Fragment {
 		
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 			
 			View view = inflater.inflate(R.layout.fragment_prepare_3, container, false);
 			return view;
 		}
 		
 		@Override
 		public void onViewCreated(View view, Bundle savedInstanceState) {
 			
 			// TODO: show (activate) Done button after downloading
 			Button btnNext = (Button) view.findViewById(R.id.prepare_3_next);
 			btnNext.setEnabled(true);
 			btnNext.setOnClickListener(nextButtonListener);
 			
 			//TODO: show TextView after downloading
 			TextView txtFinished = (TextView) view.findViewById(R.id.prepare_3_finished);
 			txtFinished.setVisibility(View.VISIBLE);
 		}
 	}
 
 }
