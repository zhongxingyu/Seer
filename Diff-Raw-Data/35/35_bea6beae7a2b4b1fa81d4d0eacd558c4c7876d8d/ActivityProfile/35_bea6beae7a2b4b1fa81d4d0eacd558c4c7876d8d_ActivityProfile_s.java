 //
 // Copyright (C) 2013, Maxim Osipov
 //
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without modification,
 // are permitted provided that the following conditions are met:
 //
 //  - Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 //  - Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 //  - Neither the name of the University of Oxford nor the names of its
 //    contributors may be used to endorse or promote products derived from this
 //    software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 // IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 // DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 // LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 // OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 
 package com.ibme.android.actopsy;
 
 import com.ibme.android.actopsy.R;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.model.TimeSeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.os.StrictMode;
 import android.preference.PreferenceManager;
 import android.annotation.TargetApi;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.graphics.Color;
 import android.widget.LinearLayout;
 
 public class ActivityProfile extends SherlockActivity implements OnSharedPreferenceChangeListener {
 
 	private static final String TAG = "Actopsy";
 
 //	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 //		if (BuildConfig.DEBUG) {
 //			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
 //	                 .detectAll()
 //	                 .penaltyLog()
 //	                 .build());
 //		}
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.profile);
 
 		// Initialize preferences
 		PreferenceManager.setDefaultValues(this, R.layout.preferences, false);
 
 		// Start services
 		Intent service = new Intent(this, ServiceCollect.class);
 		this.startService(service);
 		service = new Intent(this, ServiceUpload.class);
 		this.startService(service);
 
 		// Initialize charts
 		chartInit();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.activity_actopsy, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int itemId = item.getItemId();
 		if (itemId == R.id.menu_profile) {
 			return super.onOptionsItemSelected(item);
 		} else if (itemId == R.id.menu_settings) {
 			Intent settingsActivity = new Intent(getBaseContext(), ActivitySettings.class);
 			startActivity(settingsActivity);
 			return true;
 		} else {
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
 		prefs.registerOnSharedPreferenceChangeListener(this);
 		doBindService();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
 		prefs.unregisterOnSharedPreferenceChangeListener(this);
 		doUnbindService();
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 	}
 
 	///////////////////////////////////////////////////////////////////////////
 	// Service interaction
 	///////////////////////////////////////////////////////////////////////////
 	boolean mBound;
 	Messenger mService = null;
 
 	class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			//			case ActopsyService.MSG_ACC_DATA:
 			//				float x, y, z;
 			//				long ts;
 			//				Bundle b = msg.getData();
 			//				x = b.getFloat("X");
 			//				y = b.getFloat("Y");
 			//				z = b.getFloat("Z");
 			//				ts = b.getLong("TS");
 			//				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 
 	final Messenger mMessenger = new Messenger(new IncomingHandler());
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			mService = new Messenger(service);
 			try {
 				Message msg = Message.obtain(null, ServiceCollect.MSG_REGISTER_CLIENT);
 				msg.replyTo = mMessenger;
 				mService.send(msg);
 			} catch (RemoteException e) {
 				new ClassEvents(TAG, "ERROR", "Register client failed " + e.getMessage());
 			}
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			mService = null;
 		}
 	};
 
 	void doBindService() {
 		bindService(new Intent(ActivityProfile.this, ServiceCollect.class), mConnection, Context.BIND_AUTO_CREATE);
 		mBound = true;
 	}
 
 	void doUnbindService() {
 		if (mBound) {
 			if (mService != null) {
 				try {
 					Message msg = Message.obtain(null, ServiceCollect.MSG_UNREGISTER_CLIENT);
 					msg.replyTo = mMessenger;
 					mService.send(msg);
 				} catch (RemoteException e) {
 					new ClassEvents(TAG, "ERROR", "Un-register client failed " + e.getMessage());
 				}
 			}
 
 			// Detach our existing connection.
 			unbindService(mConnection);
 			mBound = false;
 		}
 	}
 
 	///////////////////////////////////////////////////////////////////////////
 	// Activity profile plot
 	///////////////////////////////////////////////////////////////////////////
 	private GraphicalView[] mChartView;
 	private XYMultipleSeriesRenderer[] mRenderer;
 	private XYMultipleSeriesDataset[] mDataset;
 	private XYSeriesRenderer[] mActRenderer;
 	private TimeSeries[] mActSeries;
 	private XYSeriesRenderer[] mRefRenderer;
 	private TimeSeries[] mRefSeries;
 	private int weekday;
 
 	private void chartInit() {
 		LinearLayout[] layout = new LinearLayout[7];
 		mChartView = new GraphicalView[7];
 		mRenderer = new XYMultipleSeriesRenderer[7];
 		mDataset = new XYMultipleSeriesDataset[7];
 		mActRenderer = new XYSeriesRenderer[7];
 		mActSeries = new TimeSeries[7];
 		mRefRenderer = new XYSeriesRenderer[7];
 		mRefSeries = new TimeSeries[7];
 
 		layout[0] = (LinearLayout) findViewById(R.id.linearLayoutActMON);
 		layout[1] = (LinearLayout) findViewById(R.id.linearLayoutActTUE);
 		layout[2] = (LinearLayout) findViewById(R.id.linearLayoutActWED);
 		layout[3] = (LinearLayout) findViewById(R.id.linearLayoutActTHU);
 		layout[4] = (LinearLayout) findViewById(R.id.linearLayoutActFRI);
 		layout[5] = (LinearLayout) findViewById(R.id.linearLayoutActSAT);
 		layout[6] = (LinearLayout) findViewById(R.id.linearLayoutActSUN);
 
 		// initialize reference values
 		float[] ref = chartGetRef();
 
 		// initialise charts
 		long ts = System.currentTimeMillis();
 		SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd-MMM");
 		for (int i=0; i<7; i++) {
 			long daynum = ts-i*ClassConsts.MILLIDAY;
			long offset = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();
 			String daystr = new String(fmt.format(new Date(daynum)));
 
 			mDataset[i] = new XYMultipleSeriesDataset();
 			mRenderer[i] = new XYMultipleSeriesRenderer();
 			mActRenderer[i] = new XYSeriesRenderer();
 			mActSeries[i] = new TimeSeries(Integer.toString(i));
 			mRefRenderer[i] = new XYSeriesRenderer();
 			mRefSeries[i] = new TimeSeries(Integer.toString(i));
 
 			// activity profile
 			ClassProfileAccelerometry.Values[] vals = new ClassProfileAccelerometry(this).get(daynum);
 			for(int j=0; j<vals.length; j++) {
				long t = (vals[j].t + offset) % ClassConsts.MILLIDAY;
 				double v = vals[j].l;
 				mActSeries[i].add(t, v);
 			}
 			// reference (light?) profile
 			for(int j=0; j<REF_LENGTH; j++) {
 				mRefSeries[i].add(j*ClassConsts.MILLIDAY/REF_LENGTH, ref[j]);
 			}
 			mActRenderer[i].setColor(Color.TRANSPARENT);
 			mActRenderer[i].setDisplayChartValues(false);
 			mActRenderer[i].setFillBelowLine(true);
 			mRefRenderer[i].setColor(Color.TRANSPARENT);
 			mRefRenderer[i].setDisplayChartValues(false);
 			mRefRenderer[i].setFillBelowLine(true);
 			// Highlight current day
 			if (i == weekday) {
 				mActRenderer[i].setColor(Color.argb(0xff, 0, 0x21, 0x47));
 				mActRenderer[i].setFillBelowLineColor(Color.argb(0xd0, 0, 0x41, 0x67));
 				mRefRenderer[i].setFillBelowLineColor(Color.argb(0x60, 0xf0, 0xe0, 0x00));
 				mRenderer[i].setLabelsColor(Color.argb(0xff, 0, 0x21, 0x47));
 				mRenderer[i].setXLabelsColor(Color.argb(0xff, 0, 0x21, 0x47));
 			} else {
 				mActRenderer[i].setColor(Color.argb(0x80, 0, 0x21, 0x47));
 				mActRenderer[i].setFillBelowLineColor(Color.argb(0x60, 0, 0x41, 0x67));
 				mRefRenderer[i].setFillBelowLineColor(Color.argb(0x20, 0xf0, 0xe0, 0x00));
 				mRenderer[i].setLabelsColor(Color.argb(0x80, 0, 0x21, 0x47));
 				mRenderer[i].setXLabelsColor(Color.argb(0x80, 0, 0x21, 0x47));
 			}
 
 			mRenderer[i].setChartTitleTextSize(getResources().getDimension(R.dimen.chart_font_large));
 			mRenderer[i].setChartTitle(daystr);
 			mRenderer[i].setShowLabels(true);
 			mRenderer[i].setLabelsTextSize(getResources().getDimension(R.dimen.chart_font_small));
 			mRenderer[i].setYLabels(0);
 			mRenderer[i].setYLabelsColor(0, Color.TRANSPARENT);
 			mRenderer[i].setXLabels(0);
 			mRenderer[i].setXAxisMin(0);
 			mRenderer[i].setXAxisMax(ClassConsts.MILLIDAY);
 			mRenderer[i].addXTextLabel(0, "00:00");
 			mRenderer[i].addXTextLabel(ClassConsts.MILLIHOUR*6, "6:00");
 			mRenderer[i].addXTextLabel(ClassConsts.MILLIHOUR*12, "12:00");
 			mRenderer[i].addXTextLabel(ClassConsts.MILLIHOUR*18, "18:00");
 			mRenderer[i].addXTextLabel(ClassConsts.MILLIHOUR*24, "24:00");
 			mRenderer[i].setShowLegend(false);
 			mRenderer[i].setShowAxes(false);
 			mRenderer[i].setShowGrid(false);
 			mRenderer[i].setZoomEnabled(false);
 			mRenderer[i].setExternalZoomEnabled(false);
 			mRenderer[i].setAntialiasing(true);
 			mRenderer[i].setInScroll(true);
 			mRenderer[i].setPanEnabled(false, false);
 			mRenderer[i].setApplyBackgroundColor(true);
 			mRenderer[i].setBackgroundColor(Color.WHITE);
 			mRenderer[i].setMarginsColor(Color.WHITE);
 			mRenderer[i].setPointSize(4);
 			mRenderer[i].setMargins(new int[] {
 					0, (int)getResources().getDimension(R.dimen.chart_margin),
 					0, (int)getResources().getDimension(R.dimen.chart_margin) });
 
 			mDataset[i].addSeries(mActSeries[i]);
 			mDataset[i].addSeries(mRefSeries[i]);
 			mRenderer[i].addSeriesRenderer(mActRenderer[i]);
 			mRenderer[i].addSeriesRenderer(mRefRenderer[i]);
 			mChartView[i] = ChartFactory.getTimeChartView(this, mDataset[i], mRenderer[i], "HH:mm");
 			layout[i].addView(mChartView[i]);
 		}
 	}
 	
 	public static final int REF_LENGTH = 24*4;		// 15 minutes intervals
 	float[] chartGetRef() {
 		float[] ref = new float[REF_LENGTH];
 		float max_level = 3;
 		int i = 0;
 		int j;
 		// morning (0:00 - 6:00)
 		for (; i < REF_LENGTH/24 * 6; i++) {
 			ref[i] = 0;
 		}
 		// raise (6:00 - 8:00)
 		j = i;
 		for (; i < REF_LENGTH/24 * 9; i++) {
 			// scale 6..8 to pi..2pi
 			float arg = (float) ((float) ((i-j)/((float)(REF_LENGTH/24*3)) * Math.PI) + Math.PI);
 			ref[i] = (float) ((Math.cos(arg)+1)*max_level/2);
 		}
 		// day (8:00 - 21:00)
 		for (; i < REF_LENGTH/24 * 21; i++) {
 			ref[i] = max_level;
 		}
 		// fall (21:00 - 23:00)
 		j = i;
 		for (; i < REF_LENGTH; i++) {
 			// scale 21..23 to 0..pi
 			float arg = (float) ((float) ((i-j)/((float)(REF_LENGTH/24*3)) * Math.PI));
 			ref[i] = (float) ((Math.cos(arg)+1)*max_level/2);
 		}
 		return ref;
 	}
 }
