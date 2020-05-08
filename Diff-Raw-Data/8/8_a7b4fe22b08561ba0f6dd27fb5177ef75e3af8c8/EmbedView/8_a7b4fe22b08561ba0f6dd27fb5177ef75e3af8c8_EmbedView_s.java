 /*
  *  Copyright 2012 Axel Winkler, Daniel Dunér
  * 
  *  This file is part of Daxplore Presenter.
  *
  *  Daxplore Presenter is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 2.1 of the License, or
  *  (at your option) any later version.
  *
  *  Daxplore Presenter is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with Daxplore Presenter.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.daxplore.presenter.embed;
 
 import org.daxplore.presenter.chart.display.ExternalHeader;
 import org.daxplore.presenter.chart.display.ExternalLegend;
 import org.daxplore.presenter.chart.display.GChartChart;
 import org.daxplore.presenter.shared.EmbedDefinition;
 import org.daxplore.presenter.shared.EmbedDefinition.EmbedFlag;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * The EmbedView is the base widget for the Daxplore embed mode.
  * 
  * <p>It contains all the components that make up the chart and
 * (possible) other UI-elements. It handles resizing of all
  * it's sub-widgets if the window is resized.</p>
  */
 public class EmbedView extends Composite {
	
 	private static EmbedBinderUiBinder uiBinder = GWT.create(EmbedBinderUiBinder.class);
 	
 	@UiField(provided=true)
 	protected final HorizontalPanel main = new HorizontalPanel();
 	@UiField(provided=true)
 	protected final VerticalPanel chartArea = new VerticalPanel();
 	
 	@UiField(provided=true)
 	protected final ExternalHeader header;
 	@UiField(provided = true)
 	protected final SimplePanel chartContainerPanel = new SimplePanel();
 	
 	@UiField
 	EmbedStyle style;
 	
 	protected final GChartChart chart;
 	
 	/**
 	 * Fields used to figure out and adjust the size of the chart and chartPanel.
 	 */
 	protected int resizeRecursions;
 	protected int maxWidth, maxHeight, chartWidth, chartHeight;
 	private ScrollPanel chartScrollPanel = new ScrollPanel();
 	protected boolean scrolling = false, forceScrolling = false;
 	
	interface EmbedBinderUiBinder extends UiBinder<Widget, EmbedView> {}
 	interface EmbedStyle extends CssResource {
 		String transparent();
 		String opaque();
 	}
 
 	/**
 	 * Instantiates a new embed view.
 	 * 
 	 * @param chart
 	 *            the chart
 	 * @param width
 	 *            the width
 	 * @param height
 	 *            the height
 	 * @param embedDefinition
 	 *            the embed definition
 	 */
 	public EmbedView(GChartChart chart, int width, int height, EmbedDefinition embedDefinition) {
 		this.chart = chart;
 		maxWidth = width - 10;
 		maxHeight = height - 10;
 		this.header = chart.getExternalHeader();
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		if(embedDefinition.hasFlag(EmbedFlag.LEGEND)){
 			/* 
 			 * Make the legend vertically aligned centrally using "Method 1"
 			 * from http://blog.themeforest.net/tutorials/vertical-centering-with-css/
 			 */
 			ExternalLegend legend = chart.getExternalLegend();
 			legend.addStyleName("EmbedView-legend");
 			
 			SimplePanel cell = new SimplePanel();
 			cell.setStyleName("EmbedView-cell");
 			cell.add(legend);
 			
 			FlowPanel sidebar = new FlowPanel();
 			sidebar.addStyleName("EmbedView-sidebarArea");
 			sidebar.setHeight(height+"px");
 			sidebar.add(cell);
 
 			main.add(sidebar);
 		}
 		
 		if (embedDefinition.hasFlag(EmbedFlag.TRANSPARENT)) {
 			main.getElement().addClassName(style.transparent());
 		} else {
 			main.getElement().addClassName(style.opaque());
 		}
 
 		adjustSizeRecursively();
 	}
 
 	/**
 	 * Try to adjust the size of the embed view, based on width and height.
 	 * 
 	 * <p>
 	 * The size will automatically be updated and adjusted in a number of
 	 * iterations. This is needed because the browser must draw the content
 	 * before its size can be calculated.
 	 * </p>
 	 */
 	protected void adjustSizeRecursively() {
 		resizeRecursions = 0;
 		forceScrolling = false;
 		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 			@Override
 			public void execute() {
 				adjustSizeRecursivelySub();
 			}
 		});
 	}
 	
 	/**
 	 * A recursive subprocess started by adjustSizeRecursively. <b>Do not call
 	 * directly!</b>
 	 * 
 	 * <p>Adjusts the size of the chart based on the embed view's actual size
 	 * (reported by the browser). Puts the chart in a scrollPanel if the chart
 	 * is too wide.</p>
 	 * 
 	 * <p>The method calls itself repeatedly until a correct size has been
 	 * found.</p>
 	 */
 	protected void adjustSizeRecursivelySub() {
 		int actualWidth = getOffsetWidth();
 		int actualHeight = chartArea.getOffsetHeight();
 		int diffWidth = maxWidth-actualWidth; 
 		int diffHeight = maxHeight-actualHeight;
 
 		if (chart==null ||
 				(resizeRecursions!=0 && actualWidth==maxWidth && actualHeight==maxHeight) ||
 				(resizeRecursions!=0 && diffWidth==0 && diffHeight==0) ||
 				(resizeRecursions>=10 && actualWidth<maxWidth && actualHeight<maxHeight) ||
 				(resizeRecursions>=15)){
 			return;
 		}
 
 		chartWidth += diffWidth;
 		chartWidth = Math.max(0, chartWidth);
 		
 		chartHeight += diffHeight;
 		chartHeight = Math.max(0, chartHeight);
 
 		if (chartWidth < chart.getMinWidth() || forceScrolling) {
 			if(!scrolling) {
 				chartContainerPanel.setWidget(chartScrollPanel);
 				chartScrollPanel.setWidget(chart);
 				scrolling = true;
 				forceScrolling = true;
 			}
 			chartScrollPanel.setSize(chartWidth + "px", chartHeight + "px");
 			chart.setChartSizeSmart(chart.getMinWidth(), chartHeight-40);
 		} else if (chartWidth >= chart.getMinWidth()) {
 			chartContainerPanel.setWidget(chart);
 			scrolling = false;
 			chart.setChartSizeSmart(chartWidth, chartHeight);
 		}
 		
 		resizeRecursions++;
 		
 		Scheduler.get().scheduleFinally(new ScheduledCommand() {
 			@Override
 			public void execute() {
 				adjustSizeRecursivelySub();
 			}
 		});
 	}
 }
