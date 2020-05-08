 /**
  * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
  *  
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *  
  *      http://www.apache.org/licenses/LICENSE-2.0
  *  
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.sharedsolar.chart;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.chart.BarChart.Type;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
 import org.sharedsolar.R;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 
 public class EnergyChart extends AbstractDemoChart {
 
 	public String getName() {
 		return "Energy - Today";
 	}
 
 	public String getDesc() {
 		return "Energy Bar Chart for Today";
 	}
 
 	public Intent execute(Context context) {
 		int num = 20;
 		// title
 		String[] titles = new String[] {context.getString(R.string.today)};
 		
 		// value
 		List<double[]> values = new ArrayList<double[]>();
 		double[] dvalues = new double [num];
 		for (int i = 0; i < num; i++)
 			dvalues[i] = (int) (Math.random() * 100);
 		values.add(dvalues);
 		
 		// color
 		int[] colors = new int[] {Color.rgb(119, 155, 195)};
 		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
 		
 		// label
 		renderer.setOrientation(Orientation.HORIZONTAL);
 		renderer.setXLabels(0);
 		renderer.setYLabels(4);
 		renderer.setXLabelsAngle(-60);
 		renderer.setDisplayChartValues(true);
 		renderer.setTextTypeface("sans-serif", Typeface.NORMAL);
 		for (int i = 0; i < num; i++)
 			renderer.addTextLabel(i+1, "account" + (i+1));
 		
 		// settings
		setChartSettings(renderer, "Credit - Today", "", "kWh",
 				0.5, 21, 0, 105, Color.GRAY, Color.LTGRAY);
 		return ChartFactory.getBarChartIntent(context,
 				buildBarDataset(titles, values), renderer, Type.DEFAULT);
 	}
 }
