 package com.touchmenotapps.Graph3r.bar;
 
 import java.util.ArrayList;
 
 import com.touchmenotapps.Graph3r.Graph;
 import com.touchmenotapps.Graph3r.bar.BarGraphView.BarGraphInterface;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.View;
 import android.widget.HorizontalScrollView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.LinearLayout.LayoutParams;
 
 /**
  * @version Graph3r Alpha 3
  * @author Arindam Nath (strider2023@gmail.com)
  * @Description	The BarGraphRenderer class is used to render the bar graph view on screen
  * and customize its appearance.
  * TODO Create a better defined graph object.
  */
 public class BarGraphRenderer {
 	
 	private final int ID_LEGENDS_HOLDER = 72387;
 	
 	private Context mContext;
 	
 	private float mScaleFactor = 1.0f;
 
 	private ArrayList<Double> data = new ArrayList<Double>(0);
 
 	private ArrayList<int[]> color = new ArrayList<int[]>(0);
 
 	private ArrayList<String> stackGroupLabels = new ArrayList<String>(0);
 	
 	private ArrayList<String> graphLegendsLabels = new ArrayList<String>(0);
 
 	private int graphWidth = 240; // Default Width of graph
 
 	private int graphHeight = 240; // Default graph height
 
 	private int graphOriginX = 60; // Default Origin coordinates X and Y
 
 	private int graphOriginY = 50;
 
 	private boolean drawGridLines = true;
 	
 	private boolean drawYAxis = true;
 	
 	private boolean drawXAxis = true;
 	
 	private int xAxisColor = Color.BLACK;
 	
 	private int yAxisColor = Color.BLACK;
 
 	private String yAxisLabel = "Y Axis"; // Default label of Y axis
 
 	private int yAxisLabelSize = 8;
 
 	private int xAxisLabelSize = 8;
 	
 	private int xAxisLabelRotation = 0;
 
 	private ArrayList<String> barLabels = new ArrayList<String>();
 
 	private int style = Graph.STYLE_BAR_NORMAL;
 
 	private int graphGrouping = 0;
 	
 	private int graphNumXAxesLabels = 10;
 	
 	private int xAxisLabelDistance = 10;
 	
 	private float MAX_ZOOM = 1.5f;
 	
 	private float MIN_ZOOM = 1;
 	
 	private boolean graphIsClickable = false;
 	
 	private boolean graphIsZoomable = false;
 	
 	private boolean graphIsPannable = false;
 	
 	private boolean runningOnTablet = false;
 	
 	private boolean mShowGraphLegends = false;
 	
 	/**
 	 * 
 	 * @param parentView
 	 * @param graphData
 	 * @param graphColors
 	 * @param axesLabels
 	 */
 	public BarGraphRenderer(View parentView, ArrayList<Double> graphData, ArrayList<int[]> graphColors) {
 		this.mContext = parentView.getContext();
 		this.data = graphData;
 		this.color = graphColors;
 		this.graphWidth = parentView.getWidth();
 		this.graphHeight = parentView.getHeight();
 		this.graphOriginX = parentView.getWidth()/10;
 		this.graphOriginY = parentView.getHeight()/6;
 	}
 	
 	/**
 	 * 
 	 * @param width
 	 * @param height
 	 */
 	public void setGraphWidthAndHeight(int width, int height) {
 		this.graphWidth = width;
 		this.graphHeight = height;
 		this.graphOriginX = width/10;
 		this.graphOriginY = height/6;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public View renderGraph() throws Exception {
 		if(mShowGraphLegends) {
 			/** Create the view holder **/
 			RelativeLayout graphLayout = new RelativeLayout(mContext);
 			LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 			graphLayout.setLayoutParams(params);
 			/** Create the legends holder **/
 			HorizontalScrollView legendsLayout = new HorizontalScrollView(mContext);
 			legendsLayout.setHorizontalScrollBarEnabled(false);
 			RelativeLayout.LayoutParams legendsHolderParams = new RelativeLayout.LayoutParams(
 					RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			legendsHolderParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 			legendsLayout.setLayoutParams(legendsHolderParams);
 			legendsLayout.setId(ID_LEGENDS_HOLDER);
 			/** Add legends content **/
 			//TODO Optimize code to show gradient
 			if(graphLegendsLabels.size() > 0) {
 				LinearLayout legendContent = new LinearLayout(mContext);
 				legendContent.setOrientation(LinearLayout.HORIZONTAL);
 				for(int i = 0; i < graphLegendsLabels.size(); i++) {
 					TextView legendText = new TextView(mContext);
 					LayoutParams contentParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 					contentParams.setMargins(5, 5, 5, 5);
 					legendText.setText(graphLegendsLabels.get(i));
 					legendText.setBackgroundColor(color.get(i)[0]);
 					legendText.setTextColor(Color.WHITE);
 					legendText.setLayoutParams(contentParams);
 					legendText.setPadding(5, 5, 5, 5);
 					legendContent.addView(legendText);
 				}
 				legendsLayout.addView(legendContent);
 			}
 			/** Add legends to the view holder **/
 			graphLayout.addView(legendsLayout);
 			/** Add graph to the view holder **/
 			RelativeLayout.LayoutParams graphHolderParams = new RelativeLayout.LayoutParams(
 					RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
 			graphHolderParams.addRule(RelativeLayout.ABOVE, ID_LEGENDS_HOLDER);
 			/** Reset the graph height based on user legends layout height **/
 			setGraphWidthAndHeight(getGraphWidth(), getGraphHeight() - legendsLayout.getHeight());
 			View graphView = new BarGraphView().getGraphView(mContext, this); 
 			graphView.setLayoutParams(graphHolderParams);
 			graphLayout.addView(graphView);
 			return graphLayout;
 		} else
 			return new BarGraphView().getGraphView(mContext, this);
 	}
 		
 	/**
 	 * @param barLabels the barLabels to set
 	 */
 	public void setBarLabels(ArrayList<String> barLabels) {
 		this.barLabels = barLabels;
 	}
 
 	/**
 	 * @param mScaleFactor the mScaleFactor to set
 	 */
 	public void setScaleFactor(float mScaleFactor) {
 		this.mScaleFactor = mScaleFactor;
 	}
 
 	/**
 	 * @param stackGroupLabels the stackGroupLabels to set
 	 */
 	public void setStackGroupLabels(ArrayList<String> stackGroupLabels) {
 		this.stackGroupLabels = stackGroupLabels;
 	}
 
 	/**
 	 * @param drawGridLines the drawGridLines to set
 	 */
 	public void setDrawGridLines(boolean drawGridLines) {
 		this.drawGridLines = drawGridLines;
 	}
 
 	/**
 	 * @param drawYAxis the drawYAxis to set
 	 */
 	public void setDrawYAxis(boolean drawYAxis) {
 		this.drawYAxis = drawYAxis;
 	}
 
 	/**
 	 * @param drawXAxis the drawXAxis to set
 	 */
 	public void setDrawXAxis(boolean drawXAxis) {
 		this.drawXAxis = drawXAxis;
 	}
 
 	/**
 	 * @param xAxisColor the xAxisColor to set
 	 */
 	public void setXAxisColor(int xAxisColor) {
 		this.xAxisColor = xAxisColor;
 	}
 
 	/**
 	 * @param yAxisColor the yAxisColor to set
 	 */
 	public void setYAxisColor(int yAxisColor) {
 		this.yAxisColor = yAxisColor;
 	}
 
 	/**
 	 * @param yAxisLabel the yAxisLabel to set
 	 */
 	public void setYAxisLabel(String yAxisLabel) {
 		this.yAxisLabel = yAxisLabel;
 	}
 
 	/**
 	 * @param yAxisLabelSize the yAxisLabelSize to set
 	 */
 	public void setYAxisLabelSize(int yAxisLabelSize) {
 		this.yAxisLabelSize = yAxisLabelSize;
 	}
 
 	/**
 	 * @param xAxisLabelSize the xAxisLabelSize to set
 	 */
 	public void setXAxisLabelSize(int xAxisLabelSize) {
 		this.xAxisLabelSize = xAxisLabelSize;
 	}
 
 	/**
 	 * @param xAxisLabelRotation the xAxisLabelRotation to set
 	 */
 	public void setXAxisLabelRotation(int xAxisLabelRotation) {
 		this.xAxisLabelRotation = xAxisLabelRotation;
 	}
 
 	/**
 	 * @param style the style to set
 	 */
 	public void setStyle(int style) {
 		this.style = style;
 	}
 
 	/**
 	 * @param graphGrouping the graphGrouping to set
 	 */
 	public void setGraphGrouping(int graphGrouping) {
 		this.graphGrouping = graphGrouping;
 	}
 
 	/**
 	 * @param xAxisLabelDistance the xAxisLabelDistance to set
 	 */
 	public void setXAxisLabelDistance(int xAxisLabelDistance) {
 		this.xAxisLabelDistance = xAxisLabelDistance;
 	}
 
 	/**
 	 * @param mAX_ZOOM the mAX_ZOOM to set
 	 */
 	public void setMaxZoom(int mAX_ZOOM) {
 		MAX_ZOOM = mAX_ZOOM;
 	}
 
 	/**
 	 * @param mIN_ZOOM the mIN_ZOOM to set
 	 */
 	public void setMinZoom(int mIN_ZOOM) {
 		MIN_ZOOM = mIN_ZOOM;
 	}
 
 	/**
	 * @param graphIsClickable the graphIsClickable to set
 	 */
	public void setGraphIsClickable(boolean graphIsClickable) {
 		this.graphIsClickable = graphIsClickable;
 	}
 
 	/**
 	 * @param graphIsZoomable the graphIsZoomable to set
 	 */
 	public void setGraphIsZoomable(boolean graphIsZoomable) {
 		this.graphIsZoomable = graphIsZoomable;
 	}
 
 	/**
 	 * @param graphIsPannable the graphIsPannable to set
 	 */
 	public void setGraphIsPannable(boolean graphIsPannable) {
 		this.graphIsPannable = graphIsPannable;
 	}
 	
 	/**
 	 * @return the runningOnTablet
 	 */
 	public boolean isRunningOnTablet() {
 		return runningOnTablet;
 	}
 	
 	/**
 	 * @param runningOnTablet the runningOnTablet to set
 	 */
 	public void setRunningOnTablet(boolean runningOnTablet) {
 		this.runningOnTablet = runningOnTablet;
 	}
 	
 	/*****************************************************************************************************************************
 	 * Getter Methods
 	 *****************************************************************************************************************************/	
 	public float getScaleFactor() {
 		return mScaleFactor;
 	}
 
 	public ArrayList<Double> getPlotData() {
 		return data;
 	}
 
 	public ArrayList<int[]> getPlotColor() {
 		return color;
 	}
 
 	public ArrayList<String> getStackGroupLabels() {
 		return stackGroupLabels;
 	}
 
 	public int getGraphWidth() {
 		return graphWidth;
 	}
 
 	public int getGraphHeight() {
 		return graphHeight;
 	}
 
 	public int getGraphOriginX() {
 		return graphOriginX;
 	}
 
 	public int getGraphOriginY() {
 		return graphOriginY;
 	}
 
 	public boolean getDrawGridLines() {
 		return drawGridLines;
 	}
 
 	public boolean getDrawYAxis() {
 		return drawYAxis;
 	}
 
 	public boolean getDrawXAxis() {
 		return drawXAxis;
 	}
 
 	public int getXAxisColor() {
 		return xAxisColor;
 	}
 
 	public int getYAxisColor() {
 		return yAxisColor;
 	}
 
 	public String getYAxisLabel() {
 		return yAxisLabel;
 	}
 
 	public int getYAxisLabelSize() {
 		return yAxisLabelSize;
 	}
 
 	public int getXAxisLabelSize() {
 		return xAxisLabelSize;
 	}
 
 	public int getXAxisLabelRotation() {
 		return xAxisLabelRotation;
 	}
 
 	public ArrayList<String> getBarLabels() {
 		return barLabels;
 	}
 
 	public int getStyle() {
 		return style;
 	}
 
 	public int getGraphGrouping() {
 		return graphGrouping;
 	}
 
 	public int getXAxisLabelDistance() {
 		return xAxisLabelDistance;
 	}
 
 	public float getMaxZoom() {
 		return MAX_ZOOM;
 	}
 
 	public float getMinZoom() {
 		return MIN_ZOOM;
 	}
 
 	public boolean getGraphIsClickable() {
 		return graphIsClickable;
 	}
 
 	public boolean getGraphIsZoomable() {
 		return graphIsZoomable;
 	}
 
 	public boolean getGraphIsPannable() {
 		return graphIsPannable;
 	}
 
 	/**
 	 * @return the mShowGraphLegends
 	 */
 	public boolean isShowGraphLegends() {
 		return mShowGraphLegends;
 	}
 
 	/**
 	 * @param mShowGraphLegends the mShowGraphLegends to set
 	 */
 	public void setShowGraphLegends(boolean mShowGraphLegends) {
 		this.mShowGraphLegends = mShowGraphLegends;
 	}
 
 	/**
 	 * @param graphLegendsLabels the graphLegendsLabels to set
 	 */
 	public void setGraphLegendsLabels(ArrayList<String> graphLegendsLabels) {
 		this.graphLegendsLabels = graphLegendsLabels;
 	}
 
 	/**
 	 * @return the graphNumXAxesLabels
 	 */
 	public int getGraphNumXAxesLabels() {
 		return graphNumXAxesLabels;
 	}
 
 	/**
 	 * @param graphNumXAxesLabels the graphNumXAxesLabels to set
 	 */
 	public void setGraphNumXAxesLabels(int graphNumXAxesLabels) {
 		this.graphNumXAxesLabels = graphNumXAxesLabels;
 	}
	
	public void onBarGraphClickedListener(BarGraphInterface barGraphInterface) { }

 }
 
