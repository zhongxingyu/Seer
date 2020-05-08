 //   FluksoViz 
 //   Copyright (C) 2012  Maciej Eckstein sherlock@vsat.pl
 //
 //    This program is free software: you can redistribute it and/or modify
 //    it under the terms of the GNU General Public License as published by
 //    the Free Software Foundation, either version 3 of the License, or
 //    (at your option) any later version.
 //
 //    This program is distributed in the hope that it will be useful,
 //    but WITHOUT ANY WARRANTY; without even the implied warranty of
 //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //    GNU General Public License for more details.
 
 //    You should have received a copy of the GNU General Public License
 //    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 //    http://www.gnu.org/licenses/gpl-3.0.txt
 
 package com.FluksoViz;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.SocketTimeoutException;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.FieldPosition;
 import java.text.Format;
 import java.text.ParsePosition;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.params.ConnManagerPNames;
 import org.apache.http.conn.params.ConnPerRouteBean;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.SingleClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Color;
 import android.graphics.DashPathEffect;
 import android.graphics.LinearGradient;
 import android.graphics.Paint;
 import android.graphics.Shader;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.preference.PreferenceManager;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.androidplot.Plot;
 import com.androidplot.xy.BoundaryMode;
 import com.androidplot.xy.FillDirection;
 import com.androidplot.xy.LineAndPointFormatter;
 import com.androidplot.xy.SimpleXYSeries;
 import com.androidplot.xy.XLayoutStyle;
 import com.androidplot.xy.XPositionMetric;
 import com.androidplot.xy.XYPlot;
 import com.androidplot.xy.XYStepMode;
 import com.androidplot.xy.YValueMarker;
 
 public class FluksoVizActivity extends Activity {
 	/** Called when the activity is first created. */
 	TextView Napis, Napis2, napis_delta, Napis01, Napis3, Napis4, W;
 	TextView tv_p1, tv_p2, tv_p3, tv_today_kwh, tv_today_cost, tv_today_percent, tv_today_avg;
 	TextView tv_week_kwh, tv_week_cost, tv_week_percent, tv_week_avg;
 	TextView tv_month_kwh, tv_month_cost, tv_month_percent, tv_month_avg;
 	TextView tv_curr1, tv_curr2, tv_curr3;
 
 	ImageView iv1,iv2,iv3;
 
 	int i;
 	int screen_width;
 	int delta_value = 0;
 	int plot1_mode = 0;
 	int getAPIfailscounter = 0;
 	int startof_series1 = 0;
 	int sensor_number;
 	double cost_fixedpart, cost_perkwh;
 
 	//String DATE_FORMAT_NOW = "HH:mm:ss"; //
 	String ip_addr, api_key_1, api_key_2, api_key_3, api_token_1, api_token_2, api_token_3;
 	String cost_currencycode;
 	String network_checks_results = null;
 	String api_server_ip;
 
 	DateFormat sdf = DateFormat.getDateTimeInstance();
 	DateFormat sdf2 = DateFormat.getTimeInstance();
 
 	Handler handler = new Handler();
 	Handler handler2 = new Handler();
 	HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
      
 	
 	Boolean thread1_running = true;
 	Boolean thread2_running = true;
 	Boolean local_p1_ok = true;
 	Boolean local_p2_ok = true;
 	Boolean local_p3_ok = true;
 	Boolean delta_mode = false;
 	Boolean busy_in_api = false;
 
 	Boolean skip_initial_sensor_checks = false;
 	SharedPreferences my_app_prefs;
 	SharedPreferences.Editor edit;
 
 	Context context;
 
 	XYPlot Plot1, Plot2;
 	LinkedList<Number> series1linkedlist, series2linkedlist_neg, series2linkedlist, series3linkedlist, seriesSUM12linkedlist, seriesSUM123linkedlist;
 	ArrayList<Number> responseArrayListNumber;
 	List<Number> series1mnormallist, series2, series;
 	List<Number> series2mnormallist, series3mnormallist, seriesSUM12normallist, seriesSUM123normallist;
 
 	LinkedList<Number> series_day1_linkedlist, series_day2_linkedlist, series_day3_linkedlist, series_daySUM_linkedlist;
 	LinkedList<Number> series_month1_linkedlist, series_month2_linkedlist, series_month3_linkedlist, series_monthSUM_linkedlist;
 	List<Number> series_day1_list, series_month1_list;
 
 	SimpleXYSeries series1m, series2m, series3m;
 	SimpleXYSeries series_p2_1, series_p2_2;
 
 	LineAndPointFormatter series1mFormat, series2mFormat, series3mFormat, series4mFormat ;
 	Paint line1mFill, line2mFill, line3mFill, line4mFill;
 	YValueMarker marker1;
 	String  versionName;
 
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		Display display = getWindowManager().getDefaultDisplay(); 
 		screen_width = display.getWidth();
 	    if (screen_width==320){
 	    	setContentView(R.layout.main_lowres );
 	    } else setContentView(R.layout.main);
 	
 		context = getApplicationContext();
 		SharedPreferences my_app_prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		try {
 			 versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0 ).versionName;
 		} catch (NameNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		sensor_number =  Integer.parseInt(my_app_prefs.getString("sensor_number","1"));
 		api_server_ip = my_app_prefs.getString("api_server","178.79.177.6");
 		skip_initial_sensor_checks = (Boolean) my_app_prefs.getBoolean("skip_initial_sensor_checks", false);
 		ip_addr = (String) my_app_prefs.getString("flukso_ip_addr", "10.10.10.10");
 		api_key_1 = (String) my_app_prefs.getString("sensor_1_api_key", "0.0.0.0");
 		api_key_2 = (String) my_app_prefs.getString("sensor_2_api_key", "0.0.0.0");
 		api_key_3 = (String) my_app_prefs.getString("sensor_3_api_key", "0.0.0.0");
 		api_token_1 = (String) my_app_prefs.getString("sensor_1_token", "0.0.0.0");
 		api_token_2 = (String) my_app_prefs.getString("sensor_2_token", "0.0.0.0");
 		api_token_3 = (String) my_app_prefs.getString("sensor_3_token", "0.0.0.0");
 		cost_fixedpart = Double.parseDouble(my_app_prefs.getString("cost_perkwh", "0"));
 		cost_perkwh = Double.parseDouble(my_app_prefs.getString("cost_perkwh", "0"));
 		cost_currencycode = (String) my_app_prefs.getString("cost_currencycode", "PLN");
 	
 		Napis = (TextView) findViewById(R.id.textView1);
 		Napis2 = (TextView) findViewById(R.id.textView2);
 		Napis01 = (TextView) findViewById(R.id.textView01);
 		Napis3 = (TextView) findViewById(R.id.textView_r1);
 		Napis4 = (TextView) findViewById(R.id.textView_rd1);
 		tv_p1 = (TextView) findViewById(R.id.textView_p1);
 		tv_p1.setTextColor(Color.WHITE);
 		tv_p1.setVisibility(TextView.INVISIBLE);
 		tv_p2 = (TextView) findViewById(R.id.textView_p2);
 		tv_p2.setTextColor(Color.WHITE);
 		tv_p2.setVisibility(TextView.INVISIBLE);
 		tv_p3 = (TextView) findViewById(R.id.textView_p3);
 		tv_p3.setTextColor(Color.WHITE);
 		tv_p3.setVisibility(TextView.INVISIBLE);
 		tv_today_kwh = (TextView) findViewById(R.id.TextView_r2);
 		tv_today_cost = (TextView) findViewById(R.id.TextView_r4);
 		tv_today_percent = (TextView) findViewById(R.id.TextView_r6);
 		tv_today_avg = (TextView) findViewById(R.id.TextView_r22);
 		tv_week_kwh = (TextView) findViewById(R.id.TextView_rd2);
 		tv_week_avg = (TextView) findViewById(R.id.TextView_rd22);
 		tv_week_cost = (TextView) findViewById(R.id.TextView_rd4);
 		tv_week_percent = (TextView) findViewById(R.id.TextView_rd6);
 		
 		tv_month_kwh = (TextView) findViewById(R.id.TextView_rt2);
 		tv_month_avg = (TextView) findViewById(R.id.TextView_rt22);
 		tv_month_cost = (TextView) findViewById(R.id.TextView_rt4);
 		tv_month_percent = (TextView) findViewById(R.id.TextView_rt6);
 		
 		tv_curr1 = (TextView) findViewById(R.id.TextView_r5);
 		tv_curr2 = (TextView) findViewById(R.id.TextView_rd5);
 		tv_curr3 = (TextView) findViewById(R.id.TextView_rt5);
 		tv_curr1.setText(cost_currencycode);
 		tv_curr2.setText(cost_currencycode);
 		tv_curr3.setText(cost_currencycode);
 		
 		Napis01.setText("" + sensor_number);
 	
 		iv1 = (ImageView) findViewById(R.id.arrow_image1);
 		iv2 = (ImageView) findViewById(R.id.arrow_image2);
 		iv3 = (ImageView) findViewById(R.id.arrow_image3);
 		
 		W = (TextView) findViewById(R.id.textView4);
 		napis_delta = (TextView) findViewById(R.id.textView_delta);
 		napis_delta.setText("" + (char) 0x0394);
 		napis_delta.setTextColor(Color.WHITE);
 		napis_delta.setVisibility(TextView.INVISIBLE);
 	
 		Plot1 = (XYPlot) findViewById(R.id.Plot1);
 		Plot2 = (XYPlot) findViewById(R.id.Plot2);
 	
 		series1m = new SimpleXYSeries("seria 1m");
 		series2m = new SimpleXYSeries("seria 2m");
 		series3m = new SimpleXYSeries("seria 3m");
 	
 		series_p2_1 = new SimpleXYSeries("plot 2 - 1");
 	
 		series1mFormat = new LineAndPointFormatter(
 				Color.rgb(0, 180, 0), // line
 				Color.rgb(50, 100, 0), // point color
 				null);
 		line1mFill = new Paint();
 		line1mFill.setAlpha(100);
 		line1mFill.setShader(new LinearGradient(0, 0, 0, 200, Color.rgb(0, 100, 0), Color.BLACK, Shader.TileMode.MIRROR));
 		series1mFormat.getLinePaint().setStrokeWidth(3);
 		series1mFormat.getVertexPaint().setStrokeWidth(0);
 		series1mFormat.setFillPaint(line1mFill);
 	
 		series2mFormat = new LineAndPointFormatter( // FAZA 2 formater
 				Color.rgb(0, 200, 0), // line color
 				Color.rgb(0, 100, 50), // point color
 				null);
 		line2mFill = new Paint();
 		line2mFill.setAlpha(100);
 		line2mFill.setShader(new LinearGradient(0, 0, 0, 200, Color.rgb(0, 100, 0), Color.BLACK, Shader.TileMode.MIRROR));
 		series2mFormat.getLinePaint().setStrokeWidth(3);
 		series2mFormat.getVertexPaint().setStrokeWidth(0);
 		series2mFormat.setFillPaint(line2mFill);
 	
 		series3mFormat = new LineAndPointFormatter( // FAZA 3 formater
 				Color.rgb(0, 220, 0), // line color
 				Color.rgb(0, 150, 0), // point color
 				null);
 		line3mFill = new Paint();
 		line3mFill.setAlpha(100);
 		line3mFill.setShader(new LinearGradient(0, 0, 0, 200, Color.rgb(0, 200, 0), Color.BLACK, Shader.TileMode.MIRROR));
 		series3mFormat.getLinePaint().setStrokeWidth(3);
 		//series3mFormat.getVertexPaint().setStrokeWidth(0);
 		series3mFormat.setFillPaint(line3mFill);
 		
 		series4mFormat = new LineAndPointFormatter(Color.rgb(0, 140, 220), // line
 				Color.rgb(0, 120, 190), // point color
 				null);
 		line4mFill = new Paint();
 		line4mFill.setAlpha(190);
 		line4mFill.setShader(new LinearGradient(0, 0, 0, 200, Color.rgb(0, 140, 220), Color.BLACK, Shader.TileMode.MIRROR));
 		series4mFormat.getLinePaint().setStrokeWidth(5);
 		series4mFormat.setFillPaint(line4mFill);
 	
 		make_graph_pretty(Plot1); // All formating of the graph goes into sperate method
 		make_graph_pretty(Plot2);
 	
 		Napis.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (delta_mode) {
 					napis_delta.setVisibility(TextView.INVISIBLE);
 					delta_mode = false;
 					delta_value = 0;
 					//Plot1.removeMarker(marker1);
 				} else {
 					napis_delta.setVisibility(TextView.VISIBLE);
 					delta_mode = true;
 					try {
 						delta_value = seriesSUM123linkedlist.getLast().intValue();
 						//marker1 = new YValueMarker(delta_value, "" + (char) 0x0394, new XPositionMetric(3,XLayoutStyle.ABSOLUTE_FROM_LEFT), Color.GREEN, Color.WHITE);
 						//Plot1.addMarker(marker1);
 					} catch (NullPointerException e) {
 						delta_value = 0;
 					}
 				}
 				;
 	
 			}
 		});
 	
 		W.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Napis01.setText("restarted");
 				thread1_running = true;
 				thread2_running = true;
 
 			}
 		});
 	
 		Plot1.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 	
 				switch (plot1_mode) {
 				case 0:
 					plot1_mode = 1;
 					break;
 				case 1:
 					plot1_mode = 2;
 					break;
 				case 2:
 					plot1_mode = 0;
 					break;
 				default:
 					plot1_mode = 0;
 					break;
 				}
 				;
 				switch (sensor_number) {
 				case 1: case 2: case 3: {
 					switch (plot1_mode) {
 					case 0: {
 						Plot1.setTitle("Power (W) - last minute -  stacked");
 						Plot1.removeSeries(series1m);
 						Plot1.addSeries(series2m, series2mFormat);
 						Plot1.addSeries(series3m, series3mFormat);
 						Plot1.addSeries(series1m, series1mFormat);
 						Plot1.redraw();
 						break;
 					}
 					case 1: {
 						Plot1.setTitle("Power (W) - last minute -  with details");
 						tv_p1.setVisibility(TextView.VISIBLE);
 						tv_p2.setVisibility(TextView.VISIBLE);
 						tv_p3.setVisibility(TextView.VISIBLE);
 						break;
 					}
 					case 2: {
 						Plot1.setTitle("Power (W) - last minute -  Total only");
 						Plot1.removeSeries(series2m);
 						Plot1.removeSeries(series1m);
 						Plot1.redraw();
 						tv_p1.setVisibility(TextView.INVISIBLE);
 						tv_p2.setVisibility(TextView.INVISIBLE);
 						tv_p3.setVisibility(TextView.INVISIBLE);
 						break;
 					}
 					}
 				break;	
 				}
 				case 4:	{
 					switch (plot1_mode) {
 					case 0: {
 						Plot1.setTitle("Power (W) - last minute -  stacked");
 						Plot1.redraw();
 						break;
 					}
 					case 1: {
 						Plot1.setTitle("Power (W) - last minute -  with details");
 						Plot1.redraw();
 						tv_p1.setVisibility(TextView.VISIBLE);
 						tv_p2.setVisibility(TextView.VISIBLE);
 						tv_p3.setVisibility(TextView.VISIBLE);
 						break;
 					}
 					case 2: {
 						Plot1.setTitle("Power (W) - last minute ");
 						//Plot1.removeSeries(series2m);
 						//Plot1.removeSeries(series1m);
 						Plot1.redraw();
 						tv_p1.setVisibility(TextView.INVISIBLE);
 						tv_p2.setVisibility(TextView.INVISIBLE);
 						tv_p3.setVisibility(TextView.INVISIBLE);
 						break;
 					}
 					}
 				break;	
 				}
 				}
 				
 			}
 		});
 	
 		series1m.setModel(series1linkedlist, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 		series2m.setModel(series2linkedlist, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 		series3m.setModel(series3linkedlist, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 	
 		Plot1.addSeries(series2m, series2mFormat);
 		Plot1.addSeries(series3m, series3mFormat);
 		Plot1.addSeries(series1m, series1mFormat);
 	
 		series_p2_1.setModel(series_day1_linkedlist, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 		Plot2.addSeries(series_p2_1, series4mFormat);
 		
 		
 	
 
 		
 		
 		if(skip_initial_sensor_checks){
 			thread_updater1s.start();
 			thread_updater2.start();
 		} else {
 			run_network_token_test();
 			new AlertDialog.Builder( this )
 			.setTitle( "Network check results:" )
 			.setMessage(network_checks_results)
 			.setIcon(android.R.drawable.ic_menu_agenda)
 			.setPositiveButton( "Run Both Threads\n Local & Remote ", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					thread_updater1s.start();
 					thread_updater2.start();
 				}
 			})
 			.setNeutralButton( "Run just Local Thread", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					thread_updater1s.start();
 					Plot2.setTitle("Disabled");
 					tv_today_kwh.setVisibility(TextView.INVISIBLE);
 					tv_today_cost.setVisibility(TextView.INVISIBLE);
 					tv_today_percent.setVisibility(TextView.INVISIBLE);
 					tv_today_avg.setVisibility(TextView.INVISIBLE);
 					tv_week_kwh.setVisibility(TextView.INVISIBLE);
 					tv_week_avg.setVisibility(TextView.INVISIBLE);
 					tv_week_cost.setVisibility(TextView.INVISIBLE);
 					tv_week_percent.setVisibility(TextView.INVISIBLE);
 					tv_month_kwh.setVisibility(TextView.INVISIBLE); 
 					tv_month_avg.setVisibility(TextView.INVISIBLE);
 					tv_month_cost.setVisibility(TextView.INVISIBLE);
 					tv_month_percent.setVisibility(TextView.INVISIBLE);
 				}
 			})
 			.setNegativeButton( "Let me fix the prefs first", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					
 				}
 			} )
 			.show();
 		} //end of if for skip initial tests 
 		 
 		
 	}//end of OnCreate()
 
 	@Override
 	public void onRestart() {
 		super.onRestart();
 						
 		my_app_prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		sensor_number =  Integer.parseInt(my_app_prefs.getString("sensor_number","1"));
 		api_server_ip = my_app_prefs.getString("api_server","178.79.177.6");
 		skip_initial_sensor_checks = (Boolean) my_app_prefs.getBoolean("skip_initial_sensor_checks", false);
 		ip_addr = (String) my_app_prefs.getString("flukso_ip_addr", "10.10.10.10");
 		api_key_1 = (String) my_app_prefs.getString("sensor_1_api_key", "0.0.0.0");
 		api_key_2 = (String) my_app_prefs.getString("sensor_2_api_key", "0.0.0.0");
 		api_key_3 = (String) my_app_prefs.getString("sensor_3_api_key", "0.0.0.0");
 		api_token_1 = (String) my_app_prefs.getString("sensor_1_token", "0.0.0.0");
 		api_token_2 = (String) my_app_prefs.getString("sensor_2_token", "0.0.0.0");
 		api_token_3 = (String) my_app_prefs.getString("sensor_3_token", "0.0.0.0");
 		cost_fixedpart = Double.parseDouble(my_app_prefs.getString("cost_perkwh", "0"));
 		cost_perkwh = Double.parseDouble(my_app_prefs.getString("cost_perkwh", "0"));
 		cost_currencycode = (String) my_app_prefs.getString("cost_currencycode", "PLN");
 		
 		
 		thread1_running = true;
 		thread2_running = true;
 	   //    updateFromPreferences();
 	   //    refreshAplication();
 		
 	}
 	
     @Override
     public void onResume() {
         super.onResume();
         // The activity has become visible (it is now "resumed").
     }
 	
 	final Runnable r1s = new Runnable() {
 		public void run() {
 			showTime(Napis2);
 			thread1_running = false;
 			
 			
 			try {
 				
 				series1linkedlist = new LinkedList<Number>(series1mnormallist);
 				series1linkedlist.removeLast();
 				series1linkedlist.removeLast();
 				
 				switch (sensor_number) {
 				
 				case 1: {
 					if (local_p1_ok!=false) Napis01.setText("1 OK!"); else Napis01.setText("1-BAD!");
 					Napis.setText("" + (series1linkedlist.getLast().intValue() - delta_value)); // main readout posting tos screen
 					Napis01.append(" 2-OFF! 3-OFF!");
 					series3mFormat.getLinePaint().setStrokeWidth(8);
 					tv_p1.setText("" + (series1linkedlist.getLast().intValue()) + "w");
 					series3m.setModel(series1linkedlist, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 				break;	
 				}
 				
 				case 2: { 
 					if (local_p1_ok!=false) Napis01.setText("1 OK!"); else Napis01.setText("1-BAD!");
 					if (local_p2_ok!= false) Napis01.append(" 2 OK!"); else Napis01.append(" 2-BAD!");
 					Napis01.append(" 3-OFF!");
 					series2linkedlist = new LinkedList<Number>(series2mnormallist);
 					series2linkedlist.removeLast();
 					series2linkedlist.removeLast();
 					while (series1linkedlist.getFirst().intValue() < series2linkedlist.getFirst().intValue()) { // align the late API call
 						series2linkedlist.addFirst(series2linkedlist.get(1));
 						series2linkedlist.addFirst(series2linkedlist.get(1).intValue() - 1);
 						series2linkedlist.removeLast();
 						series2linkedlist.removeLast();
 					}	
 					seriesSUM12linkedlist = new LinkedList<Number>();
 					for (int num = 0; num < series1linkedlist.size(); num++) {
 						if (num % 2 == 0) {
 							seriesSUM12linkedlist.add(series1linkedlist.get(num));
 						} else {
 							seriesSUM12linkedlist.add(Integer.valueOf(series1linkedlist.get(num).intValue() + series2linkedlist.get(num).intValue()));
 						}
 					};				
 					
 					series3m.setModel(seriesSUM12linkedlist, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 					tv_p2.setText("" + (series2linkedlist.getLast().intValue()) + "w");
 					
 				
 					Napis.setText("" + (seriesSUM12linkedlist.getLast().intValue() - delta_value)); // main readout posting to screen
 					series3mFormat.getLinePaint().setStrokeWidth(8);
 					
 					tv_p1.setText("" + (series1linkedlist.getLast().intValue()) + "w");
 					series2m.setModel(series1linkedlist, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 					
 				break;	
 				}
 				
 				case 4: { 
 					if (local_p1_ok!=false) Napis01.setText("1 OK!"); else Napis01.setText("1-BAD!");
 					if (local_p2_ok!= false) Napis01.append(" 2 OK!"); else Napis01.append(" 2-BAD!");
 					Napis01.append(" 3-OFF!");
 					series2linkedlist = new LinkedList<Number>(series2mnormallist);
 					series2linkedlist.removeLast();
 					series2linkedlist.removeLast();
 					while (series1linkedlist.getFirst().intValue() < series2linkedlist.getFirst().intValue()) { // align the late API call
 						series2linkedlist.addFirst(series2linkedlist.get(1));
 						series2linkedlist.addFirst(series2linkedlist.get(1).intValue() - 1);
 						series2linkedlist.removeLast();
 						series2linkedlist.removeLast();
 					}	
 					seriesSUM12linkedlist = new LinkedList<Number>();
 					series2linkedlist_neg = new LinkedList<Number>();
 					
 					for (int num = 0; num < series2linkedlist.size(); num++) {
 						if (num % 2 == 0) {
 							series2linkedlist_neg.add(series2linkedlist.get(num));
 						} else {
 							series2linkedlist_neg.add(Integer.valueOf(series2linkedlist.get(num).intValue()*-1));
 						}
 					};
 					
 					
 					
 					for (int num = 0; num < series1linkedlist.size(); num++) {
 						if (num % 2 == 0) {
 							seriesSUM12linkedlist.add(series1linkedlist.get(num));
 						} else {
 							seriesSUM12linkedlist.add(Integer.valueOf(series1linkedlist.get(num).intValue() + series2linkedlist_neg.get(num).intValue()));
 						}
 					};				
 					
 					series3m.setModel(seriesSUM12linkedlist, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 					
 					
 				
 					Napis.setText("" + (seriesSUM12linkedlist.getLast().intValue() - delta_value)); // main readout posting tos screen
 					
 					tv_p1.setText("" + (series1linkedlist.getLast().intValue()) + "w");
 					tv_p2.setText("" + ((series2linkedlist_neg.getLast().intValue())) + "ws");
 					 
 					Plot1.removeSeries(series1m);
 					Plot1.removeSeries(series2m);
 					Plot1.removeSeries(series3m);
 					Plot1.addSeries(series3m, series3mFormat);
 					Plot1.addSeries(series2m, series2mFormat);
 					Plot1.addSeries(series1m, series1mFormat);	
 					
 					series1m.setModel(series1linkedlist, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 					series2m.setModel(series2linkedlist_neg, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 					
 					
 				break;	
 				}
 								
 				case 3: { 
 					if (local_p1_ok!=false) Napis01.setText("1 OK!"); else Napis01.setText("1-BAD!");
 					if (local_p2_ok!= false) Napis01.append(" 2 OK!"); else Napis01.append(" 2-BAD!");
 					if (local_p3_ok!= false) Napis01.append(" 3 OK!"); else Napis01.append(" 3-BAD!");
 					
 					series2linkedlist = new LinkedList<Number>(series2mnormallist);
 					series2linkedlist.removeLast();
 					series2linkedlist.removeLast();
 					while (series1linkedlist.getFirst().intValue() < series2linkedlist.getFirst().intValue()) { // align the late API call
 						series2linkedlist.addFirst(series2linkedlist.get(1));
 						series2linkedlist.addFirst(series2linkedlist.get(1).intValue() - 1);
 						series2linkedlist.removeLast();
 						series2linkedlist.removeLast();
 					}	
 					seriesSUM12linkedlist = new LinkedList<Number>();
 					for (int num = 0; num < series1linkedlist.size(); num++) {
 						if (num % 2 == 0) {
 							seriesSUM12linkedlist.add(series1linkedlist.get(num));
 						} else {
 							seriesSUM12linkedlist.add(Integer.valueOf(series1linkedlist.get(num).intValue() + series2linkedlist.get(num).intValue()));
 						}
 					};				
 					
 					series2m.setModel(seriesSUM12linkedlist, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 					tv_p2.setText("" + (series2linkedlist.getLast().intValue()) + "w");
 					
 					
 					series3linkedlist = new LinkedList<Number>(series3mnormallist);
 					series3linkedlist.removeLast();
 					series3linkedlist.removeLast();
 					while (series1linkedlist.getFirst().intValue() < series3linkedlist.getFirst().intValue()) { // align the late API call
 						series3linkedlist.addFirst(series3linkedlist.get(1));
 						series3linkedlist.addFirst(series3linkedlist.get(1).intValue() - 1);
 						series3linkedlist.removeLast();
 						series3linkedlist.removeLast();
 					}
 					
 					seriesSUM123linkedlist = new LinkedList<Number>();
 
 
 					for (int num = 0; num < seriesSUM12linkedlist.size(); num++) {
 						if (num % 2 == 0) {
 							seriesSUM123linkedlist.add(seriesSUM12linkedlist.get(num));
 						} else {
 							seriesSUM123linkedlist.add(Integer.valueOf(seriesSUM12linkedlist.get(num).intValue() + series3linkedlist.get(num).intValue()));
 						}
 					};
 					series3m.setModel(seriesSUM123linkedlist, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 					series3mFormat.getLinePaint().setStrokeWidth(8);
 					tv_p3.setText("" + (series3linkedlist.getLast().intValue()) + "w");
 					Napis.setText("" + (seriesSUM123linkedlist.getLast().intValue() - delta_value)); // main readout posting tos screen
 					
 					tv_p1.setText("" + (series1linkedlist.getLast().intValue()) + "w");
 					series1m.setModel(series1linkedlist, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 				break;	
 				}
 				}	
 				
 				
 				Plot1.setDomainValueFormat(new DateFormat_p1()); // sets the domain dates to nice 2 values of hh:mm format
 				fix_graph_Y_font(Plot1);
 				Plot1.redraw();
 
 				
 				
 				
 				
 			} catch (NoSuchElementException e) {
 				Napis01.setText(e.toString());
 			}// koniec
 
 			thread1_running = true;
 
 		}
 	};
 
 	final Runnable r2 = new Runnable() {
 		public void run() {
 			
 			double today_avg_watt;
 			series_p2_1.setModel(series_daySUM_linkedlist, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
 			
 			Plot2.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 3600);
 			Plot2.setTicksPerDomainLabel(4);
 			Plot2.setDomainValueFormat(new DateFormat_p1());
 
 			Calendar cal = Calendar.getInstance();
 			String current_time = sdf2.format(cal.getTime());
 
 			Plot2.setTitle("Power (W) - last day - updated:" + current_time);
 
 
 			double suma = 0;
 			for (int num = 0; num < series_daySUM_linkedlist.size(); num++) {
 				if (num % 2 != 0)
 					suma = suma + series_daySUM_linkedlist.get(num).intValue();
 			}
 			;
 			today_avg_watt = suma / ((series_daySUM_linkedlist.size()) / 2);
 			
 			int yesterday_watt = series_monthSUM_linkedlist.getLast().intValue();
 
 			double today_percent = 100 * ((today_avg_watt / yesterday_watt) - 1);
 			if (today_percent < 0) {
 				today_percent = today_percent * -1;
 				tv_today_percent.setTextColor(Color.GREEN);
 				iv1.setImageResource(R.drawable.green_arrow);
 			} else {
 				tv_today_percent.setTextColor(Color.RED);
 				iv1.setImageResource(R.drawable.red_arrow);
 			}
 			
 			DecimalFormat df = setDecimalFormatProcent(today_percent);
 			tv_today_percent.setText("" + df.format(today_percent) + "%");
 			
 
 			DecimalFormat df2 = setDecimalFormat((today_avg_watt * 24 / 1000));
 			tv_today_kwh.setText(" " + df2.format((today_avg_watt * 24 / 1000)));
 			DecimalFormat df_avg = new DecimalFormat("####");
 			tv_today_avg.setText("" + df_avg.format(today_avg_watt));
 			
 			DecimalFormat df21 = setDecimalFormat(((today_avg_watt * 24 / 1000) * (cost_perkwh)) + (cost_fixedpart / 30));
 			tv_today_cost.setText(" " + df21.format(((today_avg_watt * 24 / 1000) * (cost_perkwh)) + (cost_fixedpart / 30)));
 			
 			
 			suma = 0;   // getting last 7 days form the monthly readout.
 			for (int num = series_monthSUM_linkedlist.size() - 14; num < series_monthSUM_linkedlist.size(); num++) {
 				if (num % 2 != 0){
 					suma = suma + series_monthSUM_linkedlist.get(num).intValue();
 					//tv_week_avg.append(" "+ num);
 				}
 			};
 			//suma = suma + today_avg_watt; // plus adding what is calculated for today so far.
 			double week_avg_watt = suma / 7;
 			tv_week_avg.setText("" + df_avg.format(week_avg_watt));
 
 			DecimalFormat df3 = setDecimalFormat((week_avg_watt*24*7/1000));
 			tv_week_kwh.setText(" " + df3.format((week_avg_watt*24*7/1000)) );
 			
 			DecimalFormat df4 = setDecimalFormat(((week_avg_watt*24*7 / 1000) * (cost_perkwh)) + ((cost_fixedpart/30)*7));
 			tv_week_cost.setText(" " + df4.format(((week_avg_watt*24*7 / 1000) * (cost_perkwh)) + ((cost_fixedpart/30)*7)));
 			 
 			suma = 0;   // getting last previous 7 days form the monthly readout.
 			for (int num = series_monthSUM_linkedlist.size() - 26; num < series_monthSUM_linkedlist.size() -12 ; num++) {
 				if (num % 2 != 0){
 					suma = suma + series_monthSUM_linkedlist.get(num).intValue();
 				//	tv_week_avg.append(" "+ num);
 				}
 			};
 			double week_previous_avg_watt = suma / 7;
 			
 			double week_percent = 100 * ((week_avg_watt / week_previous_avg_watt) - 1);
 			if (week_percent < 0) {
 				week_percent = week_percent * -1;
 				tv_week_percent.setTextColor(Color.GREEN);
 				iv2.setImageResource(R.drawable.green_arrow);
 			} else {
 				tv_week_percent.setTextColor(Color.RED);
 				iv2.setImageResource(R.drawable.red_arrow);
 			}
 			DecimalFormat df5 = setDecimalFormatProcent(week_percent);
 			tv_week_percent.setText("" + df5.format(week_percent) + "%");
 			
 			suma = 0;   // getting last 30 days form the monthly readout.
 			for (int num = series_monthSUM_linkedlist.size() - 60; num < series_monthSUM_linkedlist.size(); num++) {
 				if (num % 2 != 0){
 					suma = suma + series_monthSUM_linkedlist.get(num).intValue();
 				}
 			};
 			double month_avg_watt = suma / 30;
 			tv_month_avg.setText("" + df_avg.format(month_avg_watt));
 			
 			DecimalFormat df6 = setDecimalFormat((month_avg_watt*24*30/1000));
 			tv_month_kwh.setText(" " + df6.format((month_avg_watt*24*30/1000)) );
 			
 			DecimalFormat df7 = setDecimalFormat(((month_avg_watt*24*30 / 1000) * (cost_perkwh)) + (cost_fixedpart));
 			tv_month_cost.setText(" " + df7.format(((month_avg_watt*24*30 / 1000) * (cost_perkwh)) + (cost_fixedpart)));
 			
 			suma = 0;   // getting last previous 30 days form the monthly readout.
 			for (int num = series_monthSUM_linkedlist.size() - 120; num < series_monthSUM_linkedlist.size() - 60 ; num++) {
 				if (num % 2 != 0){
 					suma = suma + series_monthSUM_linkedlist.get(num).intValue();
 				//	tv_week_avg.append(" "+ num);
 				}
 			};
 			double month_previous_avg_watt = suma / 30;
 			
 			double month_percent = 100 * ((month_avg_watt / month_previous_avg_watt) - 1);
 			if (month_percent < 0) {
 				month_percent = month_percent * -1;
 				tv_month_percent.setTextColor(Color.GREEN);
 				iv3.setImageResource(R.drawable.green_arrow);
 			} else {
 				tv_month_percent.setTextColor(Color.RED);
 				iv3.setImageResource(R.drawable.red_arrow);
 			}
 			DecimalFormat df8 = setDecimalFormatProcent(month_percent);
 			tv_month_percent.setText("" + df8.format(month_percent) + "%");
 			
 			
 			
 
 			fix_graph_Y_font(Plot2);
 			Plot2.redraw();
 			
 			
 			
 		}
 	};
 
 	Thread thread_updater1s = new Thread() {
 		@Override
 		public void run() {
 			try {
 				while (true) {
 					
 					switch (sensor_number) {
 					case 1: {
 						try {
 							series1mnormallist = getAPIdata(ip_addr, api_key_1);
 							local_p1_ok=true;
 							getAPIfailscounter = 0;
 						} catch (IOException e) {
 							local_p1_ok=false;
 							e.printStackTrace();
 						} catch (Exception e) {
 							local_p1_ok=false;
 							getAPIfailscounter++;
 							e.printStackTrace();
 						}
 					break;	
 					}
 					
 					case 2: {
 						try {
 							series1mnormallist = getAPIdata(ip_addr, api_key_1);
 							local_p1_ok=true;
 							getAPIfailscounter = 0;
 						} catch (IOException e) {
 							local_p1_ok=false;
 							e.printStackTrace();
 						} catch (Exception e) {
 							local_p1_ok=false;
 							getAPIfailscounter++;
 							e.printStackTrace();
 						}
 						try {
 							series2mnormallist = getAPIdata(ip_addr, api_key_2);
 							local_p2_ok=true;
 							getAPIfailscounter = 0;
 						} catch (IOException e) {
 							local_p2_ok=false;
 							e.printStackTrace();
 						} catch (Exception e) {
 							local_p2_ok=false;
 							getAPIfailscounter++;
 							e.printStackTrace();
 						}
 					break;	
 					}
 					
 					case 4: {
 						try {
 							series1mnormallist = getAPIdata(ip_addr, api_key_1);
 							local_p1_ok=true;
 							getAPIfailscounter = 0;
 						} catch (IOException e) {
 							local_p1_ok=false;
 							e.printStackTrace();
 						} catch (Exception e) {
 							local_p1_ok=false;
 							getAPIfailscounter++;
 							e.printStackTrace();
 						}
 						try {
 							series2mnormallist = getAPIdata(ip_addr, api_key_2);
 							local_p2_ok=true;
 							getAPIfailscounter = 0;
 						} catch (IOException e) {
 							local_p2_ok=false;
 							e.printStackTrace();
 						} catch (Exception e) {
 							local_p2_ok=false;
 							getAPIfailscounter++;
 							e.printStackTrace();
 						}
 					break;	
 					}
 					
 					case 3: {
 						try {
 							series1mnormallist = getAPIdata(ip_addr, api_key_1);
 							local_p1_ok=true;
 							getAPIfailscounter = 0;
 						} catch (IOException e) {
 							local_p1_ok=false;
 							e.printStackTrace();
 						} catch (Exception e) {
 							local_p1_ok=false;
 							getAPIfailscounter++;
 							e.printStackTrace();
 						}
 						try {
 							series2mnormallist = getAPIdata(ip_addr, api_key_2);
 							local_p2_ok=true;
 							getAPIfailscounter = 0;
 						} catch (IOException e) {
 							local_p2_ok=false;
 							e.printStackTrace();
 						} catch (Exception e) {
 							local_p2_ok=false;
 							getAPIfailscounter++;
 							e.printStackTrace();
 						}
 						try {
 							series3mnormallist = getAPIdata(ip_addr, api_key_3);
 							local_p3_ok=true;
 							getAPIfailscounter = 0;
 						} catch (IOException e) {
 							local_p3_ok=false;
 							e.printStackTrace();
 						} catch (Exception e) {
 							local_p3_ok=false;
 							getAPIfailscounter++;
 							e.printStackTrace();
 						}	
 					break;
 					}
 					}	
 					
 					
 					if (thread1_running){
 						handler.post(r1s);
 					}
 					
 					if (getAPIfailscounter > 2)
 						sleep(3000);
 					sleep(1000);
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 		}
 	};
 
 	Thread thread_updater2 = new Thread() {
 		@Override
 		public void run() {
 			try {
 				while (true) {
 
 					try {
 						
 						series_daySUM_linkedlist = new LinkedList<Number>();
 						
 						switch (sensor_number) {
 						case 1: {
 							List<Number> series_day1_list = getserwerAPIdata(api_key_1, api_token_1, "day");
 							series_day1_linkedlist = new LinkedList<Number>(series_day1_list);
 							for (int num = 0; num < series_day1_linkedlist.size() - 2; num++) {
 								if (num % 2 == 0) {
 									series_daySUM_linkedlist.add(series_day1_linkedlist.get(num));
 								} else {
 									series_daySUM_linkedlist.add(Integer.valueOf(
 											series_day1_linkedlist.get(num).intValue()
 											));
 								}
 							}
 						break;	
 						}
 						
 						case 2: {
 							List<Number> series_day1_list = getserwerAPIdata(api_key_1, api_token_1, "day");
 							series_day1_linkedlist = new LinkedList<Number>(series_day1_list);
 							List<Number> series_day2_list = getserwerAPIdata(api_key_2, api_token_2, "day");
 							series_day2_linkedlist = new LinkedList<Number>(series_day2_list);
 							
 							for (int num = 0; num < series_day1_linkedlist.size() - 2; num++) {
 								if (num % 2 == 0) {
 									series_daySUM_linkedlist.add(series_day1_linkedlist.get(num));
 								} else {
 									series_daySUM_linkedlist.add(Integer.valueOf(
 											series_day1_linkedlist.get(num).intValue()
 											+ series_day2_linkedlist.get(num).intValue()
 											));
 								}
 							}
 						break;	
 						}
 						
 						case 4: {
 							List<Number> series_day1_list = getserwerAPIdata(api_key_1, api_token_1, "day");
 							series_day1_linkedlist = new LinkedList<Number>(series_day1_list);
 							List<Number> series_day2_list = getserwerAPIdata(api_key_2, api_token_2, "day");
 							series_day2_linkedlist = new LinkedList<Number>(series_day2_list);
 							
 							for (int num = 0; num < series_day1_linkedlist.size() - 2; num++) {
 								if (num % 2 == 0) {
 									series_daySUM_linkedlist.add(series_day1_linkedlist.get(num));
 								} else {
 									series_daySUM_linkedlist.add(Integer.valueOf(
 											series_day1_linkedlist.get(num).intValue()
 											- series_day2_linkedlist.get(num).intValue()
 											));
 								}
 							}
 						break;	
 						}
 						
 						case 3: {
 							List<Number> series_day1_list = getserwerAPIdata(api_key_1, api_token_1, "day");
 							series_day1_linkedlist = new LinkedList<Number>(series_day1_list);
 							List<Number> series_day2_list = getserwerAPIdata(api_key_2, api_token_2, "day");
 							series_day2_linkedlist = new LinkedList<Number>(series_day2_list);
 							List<Number> series_day3_list = getserwerAPIdata(api_key_3, api_token_3, "day");
 							series_day3_linkedlist = new LinkedList<Number>(series_day3_list);
 							
 							for (int num = 0; num < series_day1_linkedlist.size() - 2; num++) {
 								if (num % 2 == 0) {
 									series_daySUM_linkedlist.add(series_day1_linkedlist.get(num));
 								} else {
 									series_daySUM_linkedlist.add(Integer.valueOf(
 											series_day1_linkedlist.get(num).intValue()
 											+ series_day2_linkedlist.get(num).intValue()
 											+ series_day3_linkedlist.get(num).intValue()));
 								};
 							}
 						break;
 						}
 						}
 						
 					} catch (IOException e) {
 						e.printStackTrace();
 						Plot2.setTitle("TUTAJ "+e.toString());
 					} catch (Exception e) {
 						e.printStackTrace();
 						Plot2.setTitle("lub tu"+e.toString());
 					} 
 
 					try {
 						series_monthSUM_linkedlist = new LinkedList<Number>();
 						switch (sensor_number) {
 						case 1: {
 							List<Number> series_month1_list = getserwerAPIdata_last2month(api_key_1, api_token_1);
 							series_month1_linkedlist = new LinkedList<Number>(series_month1_list);
 							for (int num = 0; num < series_month1_linkedlist.size() - 2; num++) { //obcinamy ostatni z�y
 								if (num % 2 == 0) {
 									series_monthSUM_linkedlist.add(series_month1_linkedlist.get(num));
 								} else {
 									series_monthSUM_linkedlist.add(Integer.valueOf(series_month1_linkedlist.get(num).intValue() 
 											));
 								}
 							} //end for
 						break;	
 						}
 						
 						case 2: {
 							List<Number> series_month1_list = getserwerAPIdata_last2month(api_key_1, api_token_1);
 							series_month1_linkedlist = new LinkedList<Number>(series_month1_list);
 							List<Number> series_month2_list = getserwerAPIdata_last2month(api_key_2, api_token_2);
 							series_month2_linkedlist = new LinkedList<Number>(series_month2_list);
 							
 							for (int num = 0; num < series_month1_linkedlist.size() - 2; num++) { //obcinamy ostatni z�y i sumujemy
 								if (num % 2 == 0) {
 									series_monthSUM_linkedlist.add(series_month1_linkedlist.get(num));
 								} else {
 									series_monthSUM_linkedlist.add(Integer.valueOf(series_month1_linkedlist.get(num).intValue() 
 											+ series_month2_linkedlist.get(num).intValue()
 											));
 								}
 							}				
 					
 							
 						break;	
 						}
 						
 						case 4: {
 							List<Number> series_month1_list = getserwerAPIdata_last2month(api_key_1, api_token_1);
 							series_month1_linkedlist = new LinkedList<Number>(series_month1_list);
 							List<Number> series_month2_list = getserwerAPIdata_last2month(api_key_2, api_token_2);
 							series_month2_linkedlist = new LinkedList<Number>(series_month2_list);
 							
 							for (int num = 0; num < series_month1_linkedlist.size() - 2; num++) { //obcinamy ostatni z�y
 								if (num % 2 == 0) {
 									series_monthSUM_linkedlist.add(series_month1_linkedlist.get(num));
 								} else {
 									series_monthSUM_linkedlist.add(Integer.valueOf(series_month1_linkedlist.get(num).intValue() 
 											- series_month2_linkedlist.get(num).intValue()
 											));
 								}
 							}				
 					
 							
 						break;	
 						}
 						
 						case 3: {
 							List<Number> series_month1_list = getserwerAPIdata_last2month(api_key_1, api_token_1);
 							series_month1_linkedlist = new LinkedList<Number>(series_month1_list);
 							List<Number> series_month2_list = getserwerAPIdata_last2month(api_key_2, api_token_2);
 							series_month2_linkedlist = new LinkedList<Number>(series_month2_list);	
 							List<Number> series_month3_list = getserwerAPIdata_last2month(api_key_3, api_token_3);
 							series_month3_linkedlist = new LinkedList<Number>(series_month3_list);
 						
 							for (int num = 0; num < series_month1_linkedlist.size() - 2; num++) { //obcinamy ostatni z�yi sumujemy
 								if (num % 2 == 0) {
 									series_monthSUM_linkedlist.add(series_month1_linkedlist.get(num));
 								} else {
 									series_monthSUM_linkedlist.add(Integer.valueOf(series_month1_linkedlist.get(num).intValue() + series_month2_linkedlist.get(num).intValue()
 											+ series_month3_linkedlist.get(num).intValue()));
 								}
 							}
 							
 						break;
 						}
 						}
 						
 
 					} catch (IOException e) {
 						e.printStackTrace();
 						Plot2.setTitle("trzeci"+e.toString());
 					} catch (Exception e) {
 						e.printStackTrace();
 						Plot2.setTitle("czwarty"+e.toString());
 					}
 
 					
 					
 					thread2_running = true;
 					handler2.post(r2);	
 					sleep(60000);
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 		}
 	};
 
 	private void showTime(TextView tv) {
 		Calendar cal = Calendar.getInstance();
 		tv.setText(sdf.format(cal.getTime())); // + " " +
 												// System.currentTimeMillis());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, 0, 0, "Preferences").setIcon(android.R.drawable.ic_menu_manage);
 		menu.add(0, 1, 1, "Created by Maciej Eckstein \n Version:" + versionName).setIcon(android.R.drawable.ic_menu_help);
 		;
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case 0:
 			thread1_running = false;
 			thread2_running = false;
 
 			getAPIfailscounter = 0;
 			Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
 			startActivity(settingsActivity);
 			break;
 		case 1:
 			Toast.makeText(FluksoVizActivity.this, "Soon :)", Toast.LENGTH_LONG).show();
 			break;
 		}
 		return true;
 	}
 	
 	
 
 	private void make_graph_pretty(XYPlot p) {
 
 		p.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 56); // Reduce the number
 															// of range labels
 		// Plot1.setTicksPerDomainLabel(1);
 		p.setDomainValueFormat(new DateFormat_p1());
 
 		p.setRangeStep(XYStepMode.SUBDIVIDE, 5);// Skala Y pionowa
 		// Plot1.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);
 		// Plot1.setTicksPerRangeLabel(1);
 		p.getTitleWidget().setClippingEnabled(false);
 		
 		p.getTitleWidget().pack();
 		int axis_font_size = 15;
 		int title_font_size = 15;
 		int domain_font_size = 12;
 		
 	    if (screen_width==320){
 	    	 axis_font_size = 12;
 			 title_font_size = 9;
 			 domain_font_size = 10;
 	    } 
 	    
 	    		
 		
 		
 		p.getTitleWidget().getLabelPaint().setTextSize(title_font_size);
 		p.getGraphWidget().getDomainLabelPaint().setTextSize(domain_font_size);
 		p.getGraphWidget().getDomainLabelPaint().setColor(Color.WHITE);
 		p.getGraphWidget().getRangeLabelPaint().setColor(Color.WHITE);
 		p.getGraphWidget().getRangeLabelPaint().setTextSize(axis_font_size);
 		p.getGraphWidget().getDomainOriginLabelPaint().setTextSize(domain_font_size);
 		p.getGraphWidget().getRangeOriginLabelPaint().setTextSize(axis_font_size);
 		p.getGraphWidget().setClippingEnabled(false);
 
 		p.setDomainValueFormat(new DecimalFormat("#"));
 
 		p.getLegendWidget().setVisible(false);
 		p.getDomainLabelWidget().setVisible(false);
 		p.getRangeLabelWidget().setVisible(false);
 		p.getGraphWidget().getGridLinePaint().setPathEffect(new DashPathEffect(new float[] { 1, 2, 1, 2 }, 0));
 		p.getBackgroundPaint().setColor(Color.TRANSPARENT);
 		p.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
 		p.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
 		p.setGridPadding(0, 10, 0, 0); // left top right bottom
 		p.getGraphWidget().getGridLinePaint().setColor(Color.TRANSPARENT);
 		
 		if(sensor_number!=4) {
 			p.setRangeLowerBoundary(0, BoundaryMode.GROW);// to ustawia
 		}
 		
 		if(sensor_number==4) {
 			p.addMarker(new YValueMarker(0, "0",new XPositionMetric(-11,XLayoutStyle.ABSOLUTE_FROM_LEFT), Color.WHITE, Color.WHITE ));
 			p.setRangeStep(XYStepMode.SUBDIVIDE, 2);
 			p.getGraphWidget().getRangeOriginLinePaint().setAlpha(0);
 			
 			
 			series1mFormat = new LineAndPointFormatter( // FAZA 
 					Color.rgb(0, 220, 0), // line color
 					Color.rgb(0, 150, 0), // point color
 					null);
 			
 			line1mFill.setShader(new LinearGradient(0, 0, 0, 200, Color.rgb(0, 200, 0), Color.BLACK, Shader.TileMode.MIRROR));
 			series1mFormat.getLinePaint().setStrokeWidth(4);
 			series1mFormat.setFillPaint(line1mFill);
 
 			series2mFormat = new LineAndPointFormatter( //faza 2 solar
 					Color.rgb(200, 200, 0), // line
 					Color.rgb(100, 100, 0), // point color
 					null);
 			line2mFill.setShader(new LinearGradient(0, 150, 0, 120,  Color.rgb(250, 250, 0), Color.BLACK, Shader.TileMode.CLAMP));
 			series2mFormat.setFillDirection(FillDirection.TOP);
 			series2mFormat.setFillPaint(line2mFill);
 			series2mFormat.getLinePaint().setStrokeWidth(5);
 			
 			
 			
 			series3mFormat = new LineAndPointFormatter( // FAZA 3 formater
 					Color.rgb(0, 220, 0), // line color
 					Color.rgb(0, 150, 0), // point color
 					null);
 			line3mFill.setAlpha(255);
 			line3mFill.setShader(new LinearGradient(0, 0, 0, 50, Color.BLACK , Color.BLACK, Shader.TileMode.MIRROR));
 			series3mFormat.getLinePaint().setStrokeWidth(7);
 			series3mFormat.setFillPaint(line3mFill);
 			
 			
 			
 			
 			series4mFormat = new LineAndPointFormatter(Color.rgb(0, 140, 220), // line
 					Color.rgb(0, 120, 190), // point color
 					null);
 			line4mFill = new Paint();
 			line4mFill.setAlpha(190);
 			line4mFill.setShader(new LinearGradient(0, 0, 0, 50, Color.BLACK, Color.BLACK, Shader.TileMode.MIRROR));
 			series4mFormat.getLinePaint().setStrokeWidth(5);
 			series4mFormat.setFillPaint(line4mFill);
 			series4mFormat.setFillDirection(FillDirection.TOP);
 			
 	     // XYRegionFormatter region4Formatter = new XYRegionFormatter(Color.BLUE);
 	     // series4mFormat.addRegion(new RectRegion(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0, -1000, "R1"), region4Formatter);
 	        
 			
 		}
 		
 		 
 		//p.setRangeLowerBoundary(0, BoundaryMode.GROW);// to ustawia
 		// min i max
 		// Plot1.setRangeUpperBoundary(11, BoundaryMode.FIXED);
 
 		p.setRangeValueFormat(new DecimalFormat("#"));
 		p.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
 		p.setBorderPaint(null);
 		p.disableAllMarkup(); // To get rid of them call disableAllMarkup():
 
 	}
 
 	private void fix_graph_Y_font(XYPlot p) {
 		
 		
 		if (p.getCalculatedMaxY().intValue()<999 & p.getCalculatedMaxY().intValue()>10 ) {
 			p.getGraphWidget().getRangeOriginLabelPaint().setTextSize(18);
 			p.getGraphWidget().getRangeLabelPaint().setTextSize(18);
 		} else {
 			p.getGraphWidget().getRangeOriginLabelPaint().setTextSize(15);
 			p.getGraphWidget().getRangeLabelPaint().setTextSize(15);
 		}
 		
 		if (p.getCalculatedMinY().intValue()<-999)  {
 			p.getGraphWidget().getRangeOriginLabelPaint().setTextSize(15);
 			p.getGraphWidget().getRangeLabelPaint().setTextSize(15);
 		}
 			
 	    if (screen_width==320){
 			if (p.getCalculatedMaxY().intValue()>999) {
 				p.getGraphWidget().getRangeOriginLabelPaint().setTextSize(10);
 				p.getGraphWidget().getRangeLabelPaint().setTextSize(10);
 			} else {
 				p.getGraphWidget().getRangeOriginLabelPaint().setTextSize(12);
 				p.getGraphWidget().getRangeLabelPaint().setTextSize(12);
 			}
 			if (p.getCalculatedMinY().intValue()<-999)  {
 				p.getGraphWidget().getRangeOriginLabelPaint().setTextSize(10);
 				p.getGraphWidget().getRangeLabelPaint().setTextSize(10);
 			}
 	    } 
 	 
 	    
 	}
 
 	private List<Number> getAPIdata(String IPA, String SENSOR_KEY) throws Exception, IOException {
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpParams httpParams = httpclient.getParams();
 		HttpConnectionParams.setConnectionTimeout(httpParams, 1000);
 		HttpConnectionParams.setSoTimeout(httpParams, 1000);
 		StatusLine statusLine = null;
 		HttpResponse response = null;
 
 		try {
 			response = httpclient.execute(new HttpGet("http://" + IPA + ":8080/sensor/" + SENSOR_KEY + "?version=1.0&interval=minute&unit=watt&callback=realtime"));
 			statusLine = response.getStatusLine();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			// Napis3.setText(e.toString());
 		} catch (SocketTimeoutException ste) {
 			// Napis3.setText(ste.toString());
 			ste.printStackTrace();
 		} catch (IOException e) {
 			// Napis3.setText(e.toString());
 			e.printStackTrace();
 		}
 
 		if (statusLine != null) {
 			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
 				ByteArrayOutputStream out = new ByteArrayOutputStream();
 				response.getEntity().writeTo(out);
 				out.close();
 				String responseString = out.toString().replace("realtime(", "").replace(")", "").replace("]","").replace("[", "").replace("nan", "0").replace("\"", "");
 
 				String[] responseArray = responseString.split(",");
 				Number[] responseArrayNumber = new Number[responseArray.length];
 				for (int numb = 0; numb < (responseArray.length) - 1; numb++) {
 					responseArrayNumber[numb] = Integer.parseInt(responseArray[numb]);
 				}
 				series = Arrays.asList(responseArrayNumber);
 				return series;
 			} else {
 				// Closes the connection.
 				response.getEntity().getContent().close();
 				throw new IOException(statusLine.getReasonPhrase());
 			}
 		} else {
 			// response.getEntity().getContent().close();
 			throw new IOException();
 		}
 
 	}
 
 	private List<Number> getserwerAPIdata(String SENSOR_KEY, String SENSOR_TOKEN, String INTERVAL) throws Exception, IOException {
 		SchemeRegistry schemeRegistry = new SchemeRegistry();
 		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
 		HttpParams params = new BasicHttpParams();
 		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
 		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
 		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
 
 		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 		ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
 		HttpClient httpclient2 = new DefaultHttpClient(cm, params);
 		HttpParams httpParams = httpclient2.getParams();
 		HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
 		HttpConnectionParams.setSoTimeout(httpParams, 5000);
 
 		HttpResponse response = null;
 		StatusLine statusLine2 = null;
 		try {
 			response = httpclient2.execute(new HttpGet("https://"+ api_server_ip + "/sensor/" + SENSOR_KEY + "?version=1.0&token=" + SENSOR_TOKEN + "&interval=" + INTERVAL + "&unit=watt"));
 			statusLine2 = response.getStatusLine();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			throw new IOException("failed ClientProtocolException");
 		} catch (SocketTimeoutException ste) {
 			ste.printStackTrace();
 			throw new IOException("failed SocketTimeoutExeption");
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new IOException("IO failed API Server down?");
 		}
 
 		if (statusLine2.getStatusCode() == HttpStatus.SC_OK) {
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			response.getEntity().writeTo(out);
 			out.close();
 			String responseString = out.toString().replace("]", "").replace("[", "").replace("nan", "0").replace("\"", "");
 
 			String[] responseArray = responseString.split(",");
 			Number[] responseArrayNumber = new Number[responseArray.length];
 			for (int numb = 0; numb < (responseArray.length) - 1; numb++) {
 				responseArrayNumber[numb] = Integer.parseInt(responseArray[numb]);
 			}
 
 			List<Number> series = Arrays.asList(responseArrayNumber);
 
 			return series;
 
 		} else {
 			// Closes the connection.
 			response.getEntity().getContent().close();
 			throw new IOException(statusLine2.getReasonPhrase());
 		}
 
 	}
 	
 	private List<Number> getserwerAPIdata_last2month(String SENSOR_KEY, String SENSOR_TOKEN) throws Exception, IOException {
 		SchemeRegistry schemeRegistry = new SchemeRegistry();
 		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
 		HttpParams params = new BasicHttpParams();
 		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
 		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
 		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
 
 		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 		ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
 		HttpClient httpclient2 = new DefaultHttpClient(cm, params);
 		HttpParams httpParams = httpclient2.getParams();
 		HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
 		HttpConnectionParams.setSoTimeout(httpParams, 5000);
 
 		Date d = new Date();
     	long moja_data = d.getTime() +(d.getTimezoneOffset()*60*1000);
     	d = new Date(moja_data) ;
 		
 		
     	d.setHours(00);
     	d.setSeconds(00);
     	d.setMinutes(00);
     	
 		HttpResponse response = null;
 		StatusLine statusLine2 = null;
 		try {
 			response = httpclient2.execute(new HttpGet("https://" + api_server_ip + "/sensor/" + SENSOR_KEY + "?version=1.0&token=" + SENSOR_TOKEN + 
 					"&start="+((d.getTime()/1000)-5184000)+"&resolution=day&unit=watt"));
 
 			statusLine2 = response.getStatusLine();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			throw new IOException("failed ClientProtocolException");
 		} catch (SocketTimeoutException ste) {
 			ste.printStackTrace();
 			throw new IOException("failed SocketTimeoutExeption");
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new IOException("IO failed API Server down?");
 		}
 
 		if (statusLine2.getStatusCode() == HttpStatus.SC_OK) {
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			response.getEntity().writeTo(out);
 			out.close();
 			String responseString = out.toString().replace("]", "").replace("[", "").replace("nan", "0").replace("\"", "");
 
 			String[] responseArray = responseString.split(",");
 			Number[] responseArrayNumber = new Number[responseArray.length];
 			for (int numb = 0; numb < (responseArray.length) - 1; numb++) {
 				responseArrayNumber[numb] = Integer.parseInt(responseArray[numb]);
 			}
 
 			List<Number> series = Arrays.asList(responseArrayNumber);
 
 			return series;
 
 		} else {
 			// Closes the connection.
 			response.getEntity().getContent().close();
 			throw new IOException(statusLine2.getReasonPhrase());
 		}
 
 	}
 
 	private DecimalFormat setDecimalFormat(double input_double) {
 		DecimalFormat df = new DecimalFormat("#####");
 		if  (input_double < 1000) df = new DecimalFormat("####");
 		if (input_double < 100)	df = new DecimalFormat("###.00");
 		if (input_double < 10)	df = new DecimalFormat("##.00");
 		return df;
 	};
 	private DecimalFormat setDecimalFormatProcent(double input_double) {
 		DecimalFormat df = new DecimalFormat("#####");
 		if (input_double < 100)	df = new DecimalFormat("##");
 		if (input_double < 10)	df = new DecimalFormat("##.0");
 		return df;
 	};
 	
 	
 	private class DateFormat_p1 extends Format {
 
		/**
		 * Silence the warning with default solution
		 */
		private static final long serialVersionUID = 1L;
		private DateFormat dateFormat = DateFormat.getDateTimeInstance();
 
 		@Override
 		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
 			long timestamp = 1000 * ((Number) obj).longValue();
 			Date date = new Date(timestamp);
 			return dateFormat.format(date, toAppendTo, pos);
 		}
 
 		@Override
 		public Object parseObject(String source, ParsePosition pos) {
 			return null;
 
 		}
 
 	}
 	
 	private boolean isNetworkAvailable() {
 	    ConnectivityManager connectivityManager 
 	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 	    return activeNetworkInfo != null;
 	}
 	
 	private boolean isFluksoRechableOverHTTP() {
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpParams httpParams = httpclient.getParams();
 		HttpConnectionParams.setConnectionTimeout(httpParams, 1000);
 		HttpConnectionParams.setSoTimeout(httpParams, 1000);
 		StatusLine statusLine = null;
 		HttpResponse response = null;
 		try {
 			response = httpclient.execute(new HttpGet("http://" + ip_addr + ":8080/"));
 			statusLine = response.getStatusLine();
 			if (statusLine != null) {
 				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
 					
 					return true;
 					} else {
 					Toast.makeText(FluksoVizActivity.this, "bad IP?", Toast.LENGTH_LONG).show();
 					response.getEntity().getContent().close();
 					throw new IOException(statusLine.getReasonPhrase());
 					
 					
 				}
 			} else {
 				throw new IOException();
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			Toast.makeText(FluksoVizActivity.this,"Exception \n" + e.toString() , Toast.LENGTH_LONG).show();
 			return false;
 			
 		} catch (SocketTimeoutException e) {
 			Toast.makeText(FluksoVizActivity.this,"LOOKS like local Flukso IP address is wrong! \n" + e.toString() , Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			Toast.makeText(FluksoVizActivity.this,"Exception \n" + e.toString() , Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 			return false;
 		}
 	    
 	}
 	
 
 	
 	private void run_network_token_test(){
 		if(isNetworkAvailable()){
 			network_checks_results = "Wifi or Networking enabled\n ";
 			if(isFluksoRechableOverHTTP()){
 				network_checks_results = network_checks_results + "Local Flukso " + ip_addr + " reachable\n ";
 				Boolean phase1_testok = false;
 				Boolean phase2_testok = false;
 				Boolean phase3_testok = false;
 				switch (sensor_number) {
 				case 1: {
 					try {
 						series1mnormallist = getAPIdata(ip_addr, api_key_1);
 						network_checks_results = network_checks_results + "Sensor 1 Local TEST OK\n ";
 						phase1_testok = true;
 					} catch (IOException e) {
 						network_checks_results = network_checks_results + "Sensor 1 local api key wrong \n " + e.toString() +"\n" ;
 					} catch (Exception e) {
 						network_checks_results = network_checks_results + "Sensor 1 local api key wrong \n " + e.toString() +"\n" ;
 					}
 					phase2_testok = true;
 					network_checks_results = network_checks_results + "Sensor 2 disabled in prefs\n ";
 					phase3_testok = true;
 					network_checks_results = network_checks_results + "Sensor 3 disabled in prefs\n ";
 				break;	
 				}
 				
 				case 2: {
 					try {
 						series1mnormallist = getAPIdata(ip_addr, api_key_1);
 						network_checks_results = network_checks_results + "Sensor 1 Local TEST OK\n ";
 						phase1_testok = true;
 					} catch (IOException e) {
 						network_checks_results = network_checks_results + "Sensor 1 local api key wrong \n " + e.toString() +"\n" ;
 					} catch (Exception e) {
 						network_checks_results = network_checks_results + "Sensor 1 local api key wrong \n " + e.toString() +"\n" ;
 					}
 					
 					try {
 						series2mnormallist = getAPIdata(ip_addr, api_key_2);
 						network_checks_results = network_checks_results + "Sensor 2 Local TEST OK\n ";
 						phase2_testok = true;
 					} catch (IOException e) {
 						network_checks_results = network_checks_results + "Sensor 2 local api key wrong \n " + e.toString() +"\n" ;
 					} catch (Exception e) {
 						network_checks_results = network_checks_results + "Sensor 2 local api key wrong \n " + e.toString() +"\n" ;
 					}
 					phase3_testok = true;
 					network_checks_results = network_checks_results + "Sensor 3 disabled in prefs\n ";
 				break;	
 				}
 				
 				case 4: {
 					try {
 						series1mnormallist = getAPIdata(ip_addr, api_key_1);
 						network_checks_results = network_checks_results + "Sensor 1 Local TEST OK\n ";
 						phase1_testok = true;
 					} catch (IOException e) {
 						network_checks_results = network_checks_results + "Sensor 1 local api key wrong \n " + e.toString() +"\n" ;
 					} catch (Exception e) {
 						network_checks_results = network_checks_results + "Sensor 1 local api key wrong \n " + e.toString() +"\n" ;
 					}
 					
 					try {
 						series2mnormallist = getAPIdata(ip_addr, api_key_2);
 						network_checks_results = network_checks_results + "Sensor 2 Local TEST OK\n ";
 						phase2_testok = true;
 					} catch (IOException e) {
 						network_checks_results = network_checks_results + "Sensor 2 local api key wrong \n " + e.toString() +"\n" ;
 					} catch (Exception e) {
 						network_checks_results = network_checks_results + "Sensor 2 local api key wrong \n " + e.toString() +"\n" ;
 					}
 					phase3_testok = true;
 					network_checks_results = network_checks_results + "Sensor 3 disabled in prefs\n ";
 				break;	
 				}
 				
 				case 3: {
 					try {
 						series1mnormallist = getAPIdata(ip_addr, api_key_1);
 						network_checks_results = network_checks_results + "Sensor 1 Local TEST OK\n ";
 						phase1_testok = true;
 					} catch (IOException e) {
 						network_checks_results = network_checks_results + "Sensor 1 local api key wrong \n " + e.toString() +"\n" ;
 					} catch (Exception e) {
 						network_checks_results = network_checks_results + "Sensor 1 local api key wrong \n " + e.toString() +"\n" ;
 					}
 					
 					try {
 						series2mnormallist = getAPIdata(ip_addr, api_key_2);
 						network_checks_results = network_checks_results + "Sensor 2 Local TEST OK\n ";
 						phase2_testok = true;
 					} catch (IOException e) {
 						network_checks_results = network_checks_results + "Sensor 2 local api key wrong \n " + e.toString() +"\n" ;
 					} catch (Exception e) {
 						network_checks_results = network_checks_results + "Sensor 2 local api key wrong \n " + e.toString() +"\n" ;
 					}
 					try {
 						series1mnormallist = getAPIdata(ip_addr, api_key_3);
 						network_checks_results = network_checks_results + "Sensor 3 Local TEST OK\n ";
 						phase3_testok = true;
 					} catch (IOException e) {
 						network_checks_results = network_checks_results + "Sensor 3 local api key wrong \n " + e.toString() +"\n" ;
 					} catch (Exception e) {
 						network_checks_results = network_checks_results + "Sensor 3 local api key wrong \n " + e.toString() +"\n" ;
 					}
 				break;
 				}
 				}
 	
 				if (phase1_testok==true & phase2_testok==true & phase3_testok==true ) {
 					///now I start remote api tests
 					Boolean phase1_remoteTestOk = false;
 					Boolean phase2_remoteTestOk = false;
 					Boolean phase3_remoteTestOk = false;
 					
 					switch (sensor_number) {
 					case 1: {
 						try {
 							series_day1_list = getserwerAPIdata(api_key_1, api_token_1, "day");
 							network_checks_results = network_checks_results + "Sensor 1 Remote Server API OK\n ";
 							phase1_remoteTestOk = true;
 						} catch (IOException e) {
 							network_checks_results = network_checks_results + "Sensor 1 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 						} catch (Exception e) {
 							network_checks_results = network_checks_results + "Sensor 1 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 						}
 						phase2_remoteTestOk = true;
 						network_checks_results = network_checks_results + "Sensor 2 disabled in prefs\n ";
 						phase3_remoteTestOk = true;
 						network_checks_results = network_checks_results + "Sensor 3 disabled in prefs\n ";
 		
 					break;	
 					}
 					
 					case 2: {
 						try {
 							series_day1_list = getserwerAPIdata(api_key_1, api_token_1, "day");
 							network_checks_results = network_checks_results + "Sensor 1 Remote Server API OK\n ";
 							phase1_remoteTestOk = true;
 						} catch (IOException e) {
 							network_checks_results = network_checks_results + "Sensor 1 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 						} catch (Exception e) {
 							network_checks_results = network_checks_results + "Sensor 1 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 						}
 						try {
 							series_day1_list = getserwerAPIdata(api_key_2, api_token_2, "day");
 							network_checks_results = network_checks_results + "Sensor 2 Remote Server API OK\n ";
 							phase2_remoteTestOk = true;
 						} catch (IOException e) {
 							network_checks_results = network_checks_results + "Sensor 2 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 							Plot2.setTitle("Sensor 2 token test failed");
 						} catch (Exception e) {
 							network_checks_results = network_checks_results + "Sensor 2 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 						}
 						phase3_remoteTestOk = true;
 						network_checks_results = network_checks_results + "Sensor 3 disabled in prefs\n ";
 					break;	
 					}
 					
 					case 4: {
 						try {
 							series_day1_list = getserwerAPIdata(api_key_1, api_token_1, "day");
 							network_checks_results = network_checks_results + "Sensor 1 Remote Server API OK\n ";
 							phase1_remoteTestOk = true;
 						} catch (IOException e) {
 							network_checks_results = network_checks_results + "Sensor 1 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 						} catch (Exception e) {
 							network_checks_results = network_checks_results + "Sensor 1 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 						}
 						try {
 							series_day1_list = getserwerAPIdata(api_key_2, api_token_2, "day");
 							network_checks_results = network_checks_results + "Sensor 2 Remote Server API OK\n ";
 							phase2_remoteTestOk = true;
 						} catch (IOException e) {
 							network_checks_results = network_checks_results + "Sensor 2 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 							Plot2.setTitle("Sensor 2 token test failed");
 						} catch (Exception e) {
 							network_checks_results = network_checks_results + "Sensor 2 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 						}
 						phase3_remoteTestOk = true;
 						network_checks_results = network_checks_results + "Sensor 3 disabled in prefs\n ";
 					break;	
 					}
 					
 					case 3: {
 						try {
 							series_day1_list = getserwerAPIdata(api_key_1, api_token_1, "day");
 							network_checks_results = network_checks_results + "Sensor 1 Remote Server API OK\n ";
 							phase1_remoteTestOk = true;
 						} catch (IOException e) {
 							network_checks_results = network_checks_results + "Sensor 1 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 						} catch (Exception e) {
 							network_checks_results = network_checks_results + "Sensor 1 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 						}
 						try {
 							series_day1_list = getserwerAPIdata(api_key_2, api_token_2, "day");
 							network_checks_results = network_checks_results + "Sensor 2 Remote Server API OK\n ";
 							phase2_remoteTestOk = true;
 						} catch (IOException e) {
 							network_checks_results = network_checks_results + "Sensor 2 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 							Plot2.setTitle("Sensor 2 token test failed");
 						} catch (Exception e) {
 							network_checks_results = network_checks_results + "Sensor 2 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 						}
 						try {
 							series_day1_list = getserwerAPIdata(api_key_3, api_token_3, "day");
 							network_checks_results = network_checks_results + "Sensor 2 Remote Server API OK\n ";
 							phase3_remoteTestOk = true;
 						} catch (IOException e) {
 							network_checks_results = network_checks_results + "Sensor 3 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 						} catch (Exception e) {
 							network_checks_results = network_checks_results + "Sensor 3 Remote Server API Token wrong! \n" + e.toString() + "\n ";
 							Toast.makeText(FluksoVizActivity.this, "Sensor 1 Remote Server \nAPI Token wrong \n" + e.toString(), Toast.LENGTH_LONG).show();
 						}
 					break;
 					}
 					}
 					
 					
 				}//end of if local tests sucessfull - testing api
 			} else network_checks_results = network_checks_results + "Local Flukso HTTP unrechable\n "; //end of isFlusoReachable over HTTP
 		} // end of if isNetwork Available
 	  else network_checks_results = network_checks_results + "No networks at all\n ";
 	}
 	
 	
 
 }
