 /**
  * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package edu.ucla.cens.andwellness.feedback.visualization;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.chart.PointStyle;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 import android.net.Uri;
 import edu.ucla.cens.andwellness.Utilities.KVLTriplet;
 import edu.ucla.cens.andwellness.feedback.FeedbackContract;
 import edu.ucla.cens.andwellness.feedback.FeedbackContract.FeedbackPromptResponses;
 import edu.ucla.cens.andwellness.feedback.FeedbackContract.FeedbackResponses;
 import edu.ucla.cens.andwellness.prompt.AbstractPrompt;
 import edu.ucla.cens.andwellness.prompt.Prompt;
 import edu.ucla.cens.andwellness.prompt.singlechoice.SingleChoicePrompt;
 
 /**
  * Project status demo chart.
  */
 public class FeedbackTimeChart extends AbstractChart {
 	static final String TAG = "FeedbackTimeChartLog"; 
 	protected String mPromptID;
 	protected String mCampaignUrn;
 	protected String mSurveyID;
 	protected List<Prompt> mPrompts;
 
 	public FeedbackTimeChart(String title, String campaignUrn, String surveyID, String promptID, List<Prompt> prompts) {
 		super(title);
 		mCampaignUrn = campaignUrn;
 		mSurveyID = surveyID;
 		mPromptID = promptID;
 		mPrompts = prompts;
 	}
 
 	/**
 	 * Returns the chart name.
 	 * 
 	 * @return the chart name
 	 */
 	public String getName() {
 		return "FeedbackTimeChart";
 	}
 
 	/**
 	 * Returns the chart description.
 	 * 
 	 * @return the chart description
 	 */
 	public String getDesc() {
 		return "Feedback Time Chart Description";
 	}
 
 	/**
 	 * Executes the chart demo.
 	 * 
 	 * @param context
 	 *            the context
 	 * @return the built intent
 	 */
 	public Intent execute(Context context) {
 		
 		// titles for each of the series (in this case we have only one)
 		String[] titles = new String[] { "" };
 		// list of labels for each series (only one, since we only have one series)
 		List<Date[]> dates = new ArrayList<Date[]>();
 		// list of values for each series (only one, since we only have one series)
 		List<double[]> values = new ArrayList<double[]>();
 		
 		// call up the contentprovider and feed it the campaign, survey, and prompt ID
 		// we should get a series of prompt values for the survey that we can plot
 		ContentResolver cr = context.getContentResolver();
 		// URI to match "<campaignUrn>/<surveyID>/responses/prompts/<promptID>"
 		Uri queryUri = FeedbackPromptResponses.getPromptsByCampaignAndSurvey(mCampaignUrn, mSurveyID, mPromptID);
 
 		// columns to return; in this case, we just need the date and the value at that date point
 		String[] projection = new String[] { FeedbackResponses.TIME, FeedbackPromptResponses.PROMPT_VALUE };
 		
 		// nab that data!
 		Cursor cursor = cr.query(queryUri, projection, null, null, FeedbackResponses.TIME);
 		if(cursor.getCount() == 0){
 			return null;
 		}
 		
 		// now we iterate through the cursor and insert each column of each row
 		// into the appropriate list
 		ArrayList<Date> singleDates = new ArrayList<Date>();
 		ArrayList<Double> singleValues = new ArrayList<Double>();
 		
 		double maxValue = 0;
 		while (cursor.moveToNext()) {
 			// extract date/value from each row and put it in our series
 			// 0: time field, as a long
 			// 1: prompt value, as text
 			singleDates.add(new Date(cursor.getLong(0)));
 			singleValues.add(cursor.getDouble(1));
 			
 			if (cursor.getDouble(1) > maxValue)
 				maxValue = cursor.getDouble(1);
 		}
 		
 		cursor.close();
 		
 		// convert ArrayList<Double> to double[], because java is silly
 		double[] singleValuesArray = new double[singleValues.size()];
 		for (int i = 0; i < singleValues.size(); ++i)
 			singleValuesArray[i] = singleValues.get(i);
 		
 		// and add our date/value series to the respective containers
 		dates.add(singleDates.toArray(new Date[singleDates.size()]));
 		values.add(singleValuesArray);
 
 		// int[] colors = new int[] { Color.BLUE, Color.GREEN };
 		int[] colors = new int[] { Color.RED };
 		PointStyle[] styles = new PointStyle[] { PointStyle.X };
 		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
 		renderer.setShowGrid(true);
 		
 		int topMargin = 0;
 		int bottomMargin = 100;
 		int leftMargin = 10;
 		int rightMargin = 0;
 		int margins[] = {topMargin, leftMargin, bottomMargin, rightMargin};
 		renderer.setMargins(margins);
 		
 		renderer.setAxisTitleTextSize(23);
 		renderer.setLabelsTextSize(20);
 		renderer.setXLabelsAlign(Align.LEFT);
 		renderer.setShowLegend(false);
 		renderer.setLegendTextSize(20);
 		renderer.setPointSize(10);
 		renderer.setXLabelsAngle(330);
 		renderer.setZoomButtonsVisible(true);
 		renderer.setShowAxes(true);
 		
 		//Set Y Label
 		//TODO Need to organize all prompt types instead of handling this with NULL 
 		renderer.setYLabelsAlign(Align.LEFT);
 		List<KVLTriplet> propertiesList = getPropertiesList(mPromptID);
 		if(propertiesList != null){
 			for(KVLTriplet i : propertiesList){
 				renderer.addYTextLabel(Double.valueOf(i.key).doubleValue(), i.label);
 			}
 		}
 		
 		//Set Chart
 		setChartSettings(
 				renderer,
 				"", 
 				"Date", 
 				"",
 				dates.get(0)[0].getTime() - 86400000, 
 				dates.get(0)[dates.get(0).length-1].getTime() + 86400000, 
 				0, 
 				maxValue+1,
 				Color.GRAY, 
 				Color.LTGRAY
 		);
 
 		int length = renderer.getSeriesRendererCount();
 		for (int i = 0; i < length; i++) {
 			SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
 			seriesRenderer.setDisplayChartValues(true);
 		}
 		return ChartFactory.getTimeChartIntent(
 				context, 
 				buildDateDataset(titles, dates, values), 
 				renderer, 
 				"MM/dd hha", 
 				mChartTitle
 				);
 	}
 	
 	private List<KVLTriplet> getPropertiesList(String promptId){		
 		Iterator<Prompt> ite = mPrompts.iterator();
 		while(ite.hasNext()){
 			AbstractPrompt allPromptList = (AbstractPrompt)ite.next();			
 			if(promptId.equals(allPromptList.getId())){
 				if(allPromptList instanceof SingleChoicePrompt){
 					SingleChoicePrompt prompt = (SingleChoicePrompt)allPromptList;
 					List<KVLTriplet> choiceKVLTriplet = prompt.getChoices();
 					return choiceKVLTriplet;
 				}
 			}
 		}
 		return null;
 	}
 }
