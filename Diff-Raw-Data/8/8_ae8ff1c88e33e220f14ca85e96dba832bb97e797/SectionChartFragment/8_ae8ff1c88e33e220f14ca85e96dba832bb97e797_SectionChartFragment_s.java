 package com.zehjot.smartday;
 
 import java.util.Arrays;
 import java.util.Random;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.model.CategorySeries;
 import org.achartengine.model.SeriesSelection;
 import org.achartengine.renderer.DefaultRenderer;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.zehjot.smartday.R;
 import com.zehjot.smartday.TabListener.OnUpdateListener;
 import com.zehjot.smartday.data_access.DataSet;
 import com.zehjot.smartday.data_access.DataSet.onDataAvailableListener;
 import com.zehjot.smartday.helper.Utilities;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class SectionChartFragment extends Fragment implements onDataAvailableListener, OnUpdateListener{
 	private MyChart chart1=null;
 	private static double minTimeinPercent = 0.05;
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		return inflater.inflate(R.layout.section_chart_fragment, container, false);
 	}
 	
 	@Override
 	public void onResume(){
 		super.onResume();
 		DataSet.getInstance(getActivity()).getApps(this);	
 	}
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 	}/*
 	private void draw(String[] apps, double[] time, int[] colors){
 		draw3();
 		draw1(apps, time, colors);
 		draw2();	
 		//draw4();
 		draw5();
 	}*/
 	/*
 	private void draw1(String[] apps, double[] time, int[] colors){
 		double totaltime = 0;
 		DataSet dataset = DataSet.getInstance(getActivity());
 		JSONObject selectedApps = dataset.getSelectedApps();
 		for(int i=0; i < apps.length; i++){
 			if(selectedApps.optBoolean(apps[i], true)){
 				totaltime += time[i];
 			}
 		}
 		totaltime = Math.round(totaltime*100.f);
 		totaltime /=100;
 		if(chartView1==null){
 			double otherTime = 0;
 			categories1 = new CategorySeries("Number1");
 			renderer1 = new DefaultRenderer();
 			for(int i=0; i < apps.length; i++){
 				if(selectedApps.optBoolean(apps[i], true)){
 					if(time[i]/totaltime>minTimeinPercent){
 					SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 					categories1.add(apps[i], time[i]);
 					r.setColor(colors[i]);
 					renderer1.addSeriesRenderer(r);
 					}else{
 						otherTime += time[i];
 					}
 				}
 			}
 			if(otherTime > 0){
 				SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 				otherTime = Math.round(otherTime*100.f);
 				otherTime /=100;
 				categories1.add("other", otherTime);
 				r.setColor(DataSet.getInstance(getActivity()).getColorsOfApps().optInt("other"));
 				renderer1.addSeriesRenderer(r);				
 			}
 			renderer1.setFitLegend(true);			
 			renderer1.setDisplayValues(true);
 			renderer1.setPanEnabled(false);
 			renderer1.setClickEnabled(true);
 			renderer1.setInScroll(true);
 			renderer1.setChartTitle(""+totaltime);
 			chartView1 = ChartFactory.getPieChartView(getActivity(), categories1, renderer1);	
 			
 			chartView1.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 			          SeriesSelection seriesSelection = chartView1.getCurrentSeriesAndPoint();
 			          if (seriesSelection == null) {
 			        	  
 			          } else {
 			        	  SimpleSeriesRenderer[] renederers = renderer1.getSeriesRenderers();
 			        	  for(SimpleSeriesRenderer renderer : renederers){
 			        		  renderer.setHighlighted(false);
 			        	  }
 			        	  //renderer1.getSeriesRendererAt(seriesSelection.getPointIndex()).get
 			        	  //addDetails();
 			        	  renderer1.getSeriesRendererAt(seriesSelection.getPointIndex()).setHighlighted(true);
 			        	  chartView1.repaint();
 			          }
 					
 				}
 			});
 			
 			
 			LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.chart_1);
 			layout.addView(chartView1);
 		}else{
 			double otherTime = 0;
 			categories1.clear();
 			renderer1.removeAllRenderers();	
 			for(int i=0; i < apps.length; i++){
 				if(selectedApps.optBoolean(apps[i], true)){
 					if(time[i]/totaltime>minTimeinPercent){
 					SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 					categories1.add(apps[i], time[i]);
 					r.setColor(colors[i]);
 					renderer1.addSeriesRenderer(r);
 					}else{
 						otherTime += time[i];
 					}
 				}
 			}
 			if(otherTime > 0){
 				SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 				otherTime = Math.round(otherTime*100.f);
 				otherTime /=100;
 				categories1.add("other", otherTime);
 				r.setColor(DataSet.getInstance(getActivity()).getColorsOfApps().optInt("other"));
 				renderer1.addSeriesRenderer(r);				
 			}
 			renderer1.setChartTitle(""+totaltime);
 			chartView1.repaint();
 		}
 	}
 	
 */
 	
 /*
 	private void draw4(){
 		CategorySeries categories = new CategorySeries("Number12");
 		categories.add("Social", 12.0);
 		categories.add("Productiv", 6.0);
 		categories.add("Undefined", 8);
 		
 		int[] colors = {Color.CYAN, Color.MAGENTA, Color.BLACK};
 		
 		DefaultRenderer renderer = new DefaultRenderer();
 		for(int color : colors){
 			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 			r.setColor(color);
 			renderer.addSeriesRenderer(r);
 		}
 		
 		renderer.setPanEnabled(false);
 		renderer.setInScroll(true);
 		
 		GraphicalView chartView = ChartFactory.getPieChartView(getActivity(), categories, renderer);
 					
 		LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.chart_4);
 		layout.addView(chartView);
 	}
 	private void draw5(){
 		CategorySeries categories = new CategorySeries("Number13");
 		categories.add("Social", 12.0);
 		categories.add("Productiv", 6.0);
 		categories.add("Undefined", 8);
 		
 		int[] colors = {Color.CYAN, Color.MAGENTA, Color.BLACK};
 		
 		DefaultRenderer renderer = new DefaultRenderer();
 		for(int color : colors){
 			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 			r.setColor(color);
 			renderer.addSeriesRenderer(r);
 		}
 		
 		renderer.setPanEnabled(false);
 		renderer.setInScroll(true);
 		
 		GraphicalView chartView = ChartFactory.getPieChartView(getActivity(), categories, renderer);
 					
 		LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.chart_5);
 		layout.addView(chartView);
 	}*/
 	@Override
 	public void onSaveInstanceState(Bundle outState){
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onDataAvailable(JSONObject jObj, String request) {
 		if(chart1 == null)
 			chart1 = new MyChart();
 		chart1.draw(jObj, R.id.chart_1,R.id.chart_1_details);
 	}
 
 	public void onUpdate() {
 		DataSet.getInstance(getActivity()).getApps(this);		
 	}
 	/*
 	private void addDetails(){
 		LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.chart_4);
 	    //LinearLayout layout = (LinearLayout) findViewById(R.id.info);
 
 
 	    TextView valueTV = new TextView(getActivity());
 	    valueTV.setText("hallo hallo");
 	    valueTV.setLayoutParams(new LayoutParams(
 	            LayoutParams.WRAP_CONTENT,
 	            LayoutParams.WRAP_CONTENT));
 
 	    linearLayout.addView(valueTV);
 	}
 	*/
 	
 	private class MyChart{
 		private CategorySeries categories; 
 		private DefaultRenderer renderer;
 		private String[] apps = {"No Data available"};
 		private JSONObject data;
 		private double[] time = {1.0};
 		private int[] colors = {0xA4A4A4FF};
 		private GraphicalView chartView;
 		private JSONObject rendererToArrayIndex;
 		private JSONArray otherRendererToArrayIndex;
 		private int otherColor;
 		
 		private void processData(JSONObject jObj){
 			data = jObj;
 			JSONArray jArray = null;
 			try {
 				jArray = jObj.getJSONArray("result");
 				apps = new String[jArray.length()];
 				time = new double[jArray.length()];
 				for(int i=0; i<jArray.length(); i++){
 					JSONObject app = jArray.getJSONObject(i);
 					apps[i] = app.getString("app");
 					time[i] = 0;
 					JSONArray usages = app.getJSONArray("usage");
 					for(int j=0; j<usages.length();j++){
 						JSONObject usage = usages.getJSONObject(j);
 						long start = usage.optLong("start", -1);
 						long end = usage.optLong("end", -1);
 						if(start!=-1 && end!=-1)
 							time[i] += end-start;
 					}
 					time[i] /=60.f;
 					time[i] = Math.round(time[i]*100.f);
 					time[i] /=100;
 				}	
 			} catch (JSONException e) {
 				apps = new String[]{"No Data available"};
 				time = new double[]{1.0};
 				e.printStackTrace();
 				return;
 			}
 			
 			JSONArray colorsOfApps = DataSet.getInstance(getActivity()).getColorsOfApps().optJSONArray("colors");
 			if(colorsOfApps==null)
 				colorsOfApps = new JSONArray();
 			/**
 			 * {
 			 * 	"colors":
 			 * 		[
 			 * 			{
 			 * 				"app":String,
 			 * 				"color": int
 			 * 			}
 			 * 			...
 			 * 		]
 			 * }
 			 */
 			Random rnd = new Random();
 			colors = new int[apps.length];
 			boolean found = false;
 			try{
 				for(int i=0;i<apps.length;i++){
 					found = false;
 					for(int j=0; j<colorsOfApps.length();j++){
 						JSONObject color =  colorsOfApps.getJSONObject(j);
 						if(color.getString("app").equals(apps[i])){
 							colors[i] = color.getInt("color");
 							found = true;
 							break;
 						}
 					}
 					if(!found){
 						colors[i]=rnd.nextInt();
 						colorsOfApps.put(new JSONObject()
 							.put("app",apps[i])
 							.put("color",colors[i])							
 						);
 						
 					}
 				}
 				
 				
 				found = false;
 				for(int j=0; j<colorsOfApps.length();j++){
 					JSONObject color =  colorsOfApps.getJSONObject(j);
 					if(color.getString("app").equals("Other")){
 						otherColor = color.getInt("color");
 						found = true;
 						break;
 					}
 				}
 				if(!found){
 					otherColor=rnd.nextInt();
 					colorsOfApps.put(new JSONObject()
 						.put("app","Other")
 						.put("color",otherColor)							
 					);
 					
 				}
 				
 				
 				DataSet.getInstance(getActivity()).storeColorsOfApps(new JSONObject()
 																	.put("colors", colorsOfApps));
 			}catch(JSONException e){
 			}
 		}
 		
 		public void draw(JSONObject jObj, int drawContainer, final int detailContainer){
 			processData(jObj);
 			((LinearLayout)((LinearLayout) getActivity().findViewById(detailContainer)).getChildAt(0)).removeAllViews();
 			((LinearLayout)((LinearLayout) getActivity().findViewById(detailContainer)).getChildAt(1)).removeAllViews();
 			rendererToArrayIndex = new JSONObject();
 			otherRendererToArrayIndex = new JSONArray();
 			double totaltime = 0;
 			JSONObject selectedApps = DataSet.getInstance(getActivity()).getSelectedApps();
 			for(int i=0; i < apps.length; i++){
 				if(selectedApps.optBoolean(apps[i], true)){
 					totaltime += time[i];
 				}
 			}
 			totaltime = Math.round(totaltime*100.f);
 			totaltime /=100;
 			if(chartView==null){
 				double otherTime = 0;
 				categories = new CategorySeries("Number1");
 				renderer = new DefaultRenderer();
 				for(int i=0; i < apps.length; i++){
 					if(selectedApps.optBoolean(apps[i], true)){
 						if(time[i]/totaltime>minTimeinPercent){
 						SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 						categories.add(apps[i], time[i]);
 						r.setColor(colors[i]);
 						renderer.addSeriesRenderer(r);						
 						try {
 							rendererToArrayIndex.put(""+(renderer.getSeriesRendererCount()-1), i);
 						} catch (JSONException e) {
 							e.printStackTrace();
 						}
 						}else{
 							otherTime += time[i];
 							otherRendererToArrayIndex.put(i);
 						}
 					}
 				}
 				if(otherTime > 0){
 					SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 					otherTime = Math.round(otherTime*100.f);
 					otherTime /=100;
 					categories.add("Other", otherTime);
 					r.setColor(otherColor);
 					renderer.addSeriesRenderer(r);				
 				}
 				renderer.setFitLegend(true);	
 				renderer.setDisplayValues(true);
 				renderer.setPanEnabled(false);
 				renderer.setZoomEnabled(false);
 				renderer.setClickEnabled(true);
 				renderer.setInScroll(true);
 				renderer.setChartTitle("Total time "+totaltime+" min");
 				chartView = ChartFactory.getPieChartView(getActivity(), categories, renderer);	
 				
 				chartView.setOnClickListener(new View.OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 				          SeriesSelection seriesSelection = chartView.getCurrentSeriesAndPoint();
 				          if (seriesSelection == null) {
 				        	  
 				          } else {
 				        	  SimpleSeriesRenderer[] renederers = renderer.getSeriesRenderers();
 				        	  for(SimpleSeriesRenderer renderer : renederers){
 				        		  renderer.setHighlighted(false);
 				        	  }
 				        	  //renderer1.getSeriesRendererAt(seriesSelection.getPointIndex()).get
 				        	  addDetail(seriesSelection.getPointIndex(),detailContainer);
 				        	  renderer.getSeriesRendererAt(seriesSelection.getPointIndex()).setHighlighted(true);
 				        	  chartView.repaint();
 				          }
 					}
 				});
 				
 				
 				LinearLayout layout = (LinearLayout) getActivity().findViewById(drawContainer);
 				layout.addView(chartView);
 			}else{
 				double otherTime = 0;
 				categories.clear();
 				renderer.removeAllRenderers();	
 				for(int i=0; i < apps.length; i++){
 					if(selectedApps.optBoolean(apps[i], true)){
 						if(time[i]/totaltime>minTimeinPercent){
 						SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 						categories.add(apps[i], time[i]);
 						r.setColor(colors[i]);
 						renderer.addSeriesRenderer(r);						
 						try {
 							rendererToArrayIndex.put(""+(renderer.getSeriesRendererCount()-1), i);
 						} catch (JSONException e) {
 							e.printStackTrace();
 						}
 						}else{
 							otherTime += time[i];
 							otherRendererToArrayIndex.put(i);
 						}
 					}
 				}
 				if(otherTime > 0){
 					SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 					otherTime = Math.round(otherTime*100.f);
 					otherTime /=100;
 					categories.add("Other", otherTime);
 					r.setColor(otherColor);
 					renderer.addSeriesRenderer(r);				
 				}
 				renderer.setChartTitle("Total time "+totaltime+" min");
 				chartView.repaint();
 			}
 		}
 		private void addDetail(int selectedSeries,int detailViewContainer){
 			((LinearLayout)((LinearLayout) getActivity().findViewById(detailViewContainer)).getChildAt(0)).removeAllViews();
 			((LinearLayout)((LinearLayout) getActivity().findViewById(detailViewContainer)).getChildAt(1)).removeAllViews();
 			LinearLayout appNames = (LinearLayout) ((LinearLayout) getActivity().findViewById(detailViewContainer)).getChildAt(0);		
 			if(selectedSeries==renderer.getSeriesRendererCount()-1&&otherRendererToArrayIndex.length()>0){
 				String[] sortedArray = new String[otherRendererToArrayIndex.length()];
 				for(int i = 0; i<otherRendererToArrayIndex.length(); i++){
 					sortedArray[i]=apps[otherRendererToArrayIndex.optInt(i)];
 				}
 				Arrays.sort(sortedArray);
 				for(int i = 0; i<otherRendererToArrayIndex.length(); i++){
 				    TextView valueTV = getView(sortedArray[i]);//getView(apps[otherRendererToArrayIndex.optInt(i)]);
 				    /*
 				    valueTV.setText(apps[otherRendererToArrayIndex.optInt(i)]);
 				    valueTV.setLayoutParams(new LayoutParams(
 				            LayoutParams.MATCH_PARENT,
 				            LayoutParams.WRAP_CONTENT));
 				    valueTV.setTextSize(18);*/
 				    valueTV.setPadding(10, 5, 10, 5);
 				    valueTV.setOnClickListener(new View.OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							String appName = ((TextView)v).getText().toString();
 							LinearLayout apps = (LinearLayout)v.getParent();
 							for(int i=0;i<apps.getChildCount();i++){
 								apps.getChildAt(i).setBackgroundResource(0);
 							}
 							v.setBackgroundResource(android.R.color.holo_blue_dark);
 							LinearLayout details = (LinearLayout) ((LinearLayout)v.getParent().getParent()).getChildAt(1);
 							details.removeAllViews();
 							JSONObject appTime = getTimesOfApp(appName);
 							JSONArray appUsages = appTime.optJSONArray("times");
 							if(appUsages==null)
 								return;
 							/*
 							TextView header = new TextView(getActivity());
 							header.setText("Total time "+ appTime.optInt("total"));
 							header.setLayoutParams(new LayoutParams(
 						            LayoutParams.MATCH_PARENT,
 						            LayoutParams.WRAP_CONTENT));
 							header.setTextSize(18);
 							header.setPadding(10, 5, 10, 5);*/
 							TextView header = getView("Total time:"+"\n"+"    "+Utilities.getTimeString(appTime.optInt("total")));
 							header.setPadding(10, 5, 10, 5);
 						    details.addView(header);
 						    
 							for(int i = 0; i<appUsages.length();i++){
 								JSONObject appUsage = appUsages.optJSONObject(i);
 								long start = appUsage.optLong("start");
 								long duration = appUsage.optLong("duration");
 								TextView view = getView("Used at "+ Utilities.getTimeFromTimeStamp(start));
 								/*		
 										new TextView(getActivity());
 								view.setText("Used at "+ Utilities.getTimeFromTimeStamp(start));
 								view.setLayoutParams(new LayoutParams(
 							            LayoutParams.MATCH_PARENT,
 							            LayoutParams.WRAP_CONTENT));
 								view.setTextSize(18);*/
 								view.setPadding(10, 5, 10, 0);
 							    details.addView(view);
 							    
 							    view = getView("    for "+Utilities.getTimeString(duration));/*
 							    String durationAsString = Utilities.getTimeString(duration);
 							    view.setText("    for "+durationAsString);
 							    view.setLayoutParams(new LayoutParams(
 							            LayoutParams.MATCH_PARENT,
 							            LayoutParams.WRAP_CONTENT));
 								view.setTextSize(18);	*/
 								view.setPadding(10, 0, 10, 5);						    
 							    details.addView(view);
 							}
 							
 						}
 					});
 				    appNames.addView(valueTV);
 				}
 			}else{
 			    TextView valueTV = getView(apps[rendererToArrayIndex.optInt(""+selectedSeries)]);
 			   /* valueTV.setText(apps[rendererToArrayIndex.optInt(""+selectedSeries)]);
 			    valueTV.setLayoutParams(new LayoutParams(
 			            LayoutParams.WRAP_CONTENT,
 			            LayoutParams.WRAP_CONTENT));
 			    valueTV.setTextSize(18);			*/
 			    valueTV.setPadding(10, 5, 10, 5);	
 			    appNames.addView(valueTV);
 			    
 			    
 
 				String appName = ((TextView)valueTV).getText().toString();
 				valueTV.setBackgroundResource(android.R.color.holo_blue_dark);
 				LinearLayout details = (LinearLayout) ((LinearLayout)valueTV.getParent().getParent()).getChildAt(1);
 				details.removeAllViews();
 				JSONObject appTime = getTimesOfApp(appName);
 				if(appTime == null)
 					return;
 				JSONArray appUsages = appTime.optJSONArray("times");				
 				if(appUsages==null)
 					return;
 				/*
 				TextView header = new TextView(getActivity());
 				header.setText("Total time "+ appTime.optInt("total"));
 				header.setLayoutParams(new LayoutParams(
 			            LayoutParams.MATCH_PARENT,
 			            LayoutParams.WRAP_CONTENT));
 				header.setTextSize(18);
 				header.setPadding(10, 5, 10, 5);*/
 				TextView header = getView("Total time:"+"\n"+"    "+Utilities.getTimeString(appTime.optInt("total")));
 				header.setPadding(10, 5, 10, 5);
 			    details.addView(header);
 			    
 				for(int i = 0; i<appUsages.length();i++){
 					JSONObject appUsage = appUsages.optJSONObject(i);
 					long start = appUsage.optLong("start");
 					long duration = appUsage.optLong("duration");
 					TextView view = getView("Used at "+ Utilities.getTimeFromTimeStamp(start));
 					/*		
 							new TextView(getActivity());
 					view.setText("Used at "+ Utilities.getTimeFromTimeStamp(start));
 					view.setLayoutParams(new LayoutParams(
 				            LayoutParams.MATCH_PARENT,
 				            LayoutParams.WRAP_CONTENT));
 					view.setTextSize(18);*/
 					view.setPadding(10, 5, 10, 0);
 				    details.addView(view);
 				    
 				    view = getView("    for "+Utilities.getTimeString(duration));/*
 				    String durationAsString = Utilities.getTimeString(duration);
 				    view.setText("    for "+durationAsString);
 				    view.setLayoutParams(new LayoutParams(
 				            LayoutParams.MATCH_PARENT,
 				            LayoutParams.WRAP_CONTENT));
 					view.setTextSize(18);	*/
 					view.setPadding(10, 0, 10, 5);						    
 				    details.addView(view);
 				}
 				
 			}
 		}
 		private JSONObject getTimesOfApp(String appName){
 			/**
 			 * returns
 			 * {
 			 * 	"times":[
 			 * 		{
 			 * 		"start":long
 			 * 		"duration":long
 			 * 		}
 			 * 		...
 			 * 			],
 			 * 	"total":int
 			 * }
 			 */
 			JSONObject result = new JSONObject();
 			JSONArray jArray = data.optJSONArray("result");
 			int totalTime = 0;
 			if(jArray == null)
 				return null;
 			for(int i=0; i<jArray.length();i++){
 				JSONObject app = jArray.optJSONObject(i);
 				if(app.optString("app").equals(appName)){
 					JSONArray usages = app.optJSONArray("usage");
 					JSONArray output = new JSONArray();
 					try {
 						for(int j = 0 ; j<usages.length();j++){
 							JSONObject usage = usages.optJSONObject(j);
 							long start = usage.optLong("start",-1);
 							long end = usage.optLong("end",-1);
 							if(start!=-1 && end!=-1){
 								output.put(
 									new JSONObject()
 										.put("start", start)
 										.put("duration", end-start)
 								);
 								totalTime += end-start;
 							}
 						}
 						result.put("times", output);
 						result.put("total", totalTime);
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}	
 					return result;
 				}					
 			}
 			return null;
 		}
 		
 		private TextView getView(String headerString){			
 			TextView header = new TextView(getActivity());
 			header.setText(headerString);
 			header.setLayoutParams(new LayoutParams(
 		            LayoutParams.MATCH_PARENT,
 		            LayoutParams.WRAP_CONTENT));
 			header.setTextSize(18);
 			return header;
 		}
 	}
 }
