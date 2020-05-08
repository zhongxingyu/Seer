 /**
  * Copyright (C) 2011 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.ui.config;
 
 import com.od.jtimeseries.ui.visualizer.chart.creator.ChartType;
 
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 03-Jun-2009
  * Time: 08:10:22
  *
  * A bean to represent all visualizer configuration information
  */
 public class VisualizerConfiguration implements ExportableConfig {
 
     public static final int DEFAULT_DIVIDER_LOCATION = 245; //default FixedColumn widths + 15
     public static final int DEFAULT_WIDTH = 850;  //wide enough to stop horizontal scroll bars showing
     public static final int DEFAULT_HEIGHT = 600;
 
     private String title = "Visualizer";
     private boolean tableSelectorVisible = true;  //show table by default
     private List<UiTimeSeriesConfig> chartConfigs = new ArrayList<UiTimeSeriesConfig>();
     private int dividorLocation = DEFAULT_DIVIDER_LOCATION;
     private boolean showLegendOnChart = true;
    private String chartRangeMode = ChartRangeMode.SingleRange.name(); //1.5.x bean persistence does not support enums, unfortunately!
     private DomainTimeSelection domainStartTimeSelection = new DomainTimeSelection();
    private ChartType chartType;
     private Color chartBackgroundColor = Color.BLACK;
     private List<ColumnSettings> tableColumns = new ArrayList<ColumnSettings>();
     private Rectangle frameLocation;
     private boolean isIcon;
     private boolean isShown = true;
     private int zPosition; //the z position of JInternalFrame containing the visualizer
 
     public VisualizerConfiguration() {
     }
 
     public VisualizerConfiguration(String title) {
         this.title = title;
     }
 
     public VisualizerConfiguration(String chartsTitle, boolean tableSelectorVisible, List<UiTimeSeriesConfig> chartConfigs,
                                    ChartRangeMode chartRangeMode, DomainTimeSelection domainStartTimeSelection, int dividorLocation, boolean showLegendOnChart, Color chartBackgroundColor, List<ColumnSettings> columnSettings, ChartType chartType ) {
         this.title = chartsTitle;
         this.tableSelectorVisible = tableSelectorVisible;
         this.chartConfigs = chartConfigs;
         this.domainStartTimeSelection = domainStartTimeSelection;
         this.chartType = chartType;
         this.chartRangeMode = chartRangeMode.name();
         this.dividorLocation = dividorLocation;
         this.showLegendOnChart = showLegendOnChart;
         this.chartBackgroundColor = chartBackgroundColor;
         this.tableColumns = columnSettings;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String chartsTitle) {
         this.title = chartsTitle;
     }
 
     public DomainTimeSelection getDomainStartTimeSelection() {
         return domainStartTimeSelection;
     }
 
     public void setDomainStartTimeSelection(DomainTimeSelection domainStartTimeSelection) {
         this.domainStartTimeSelection = domainStartTimeSelection;
     }
 
     public boolean isTableSelectorVisible() {
         return tableSelectorVisible;
     }
 
     public void setTableSelectorVisible(boolean tableSelectorVisible) {
         this.tableSelectorVisible = tableSelectorVisible;
     }
 
     public List<UiTimeSeriesConfig> getChartConfigs() {
         return chartConfigs;
     }
 
     public void setChartConfigs(List<UiTimeSeriesConfig> chartConfigs) {
         this.chartConfigs = chartConfigs;
     }
 
     public String getChartRangeMode() {
         return chartRangeMode;
     }
 
     public void setChartRangeMode(String chartRangeMode) {
         this.chartRangeMode = chartRangeMode;
     }
 
     public int getDividorLocation() {
         return dividorLocation;
     }
 
     public void setDividorLocation(int dividorLocation) {
         this.dividorLocation = dividorLocation;
     }
 
     public boolean isShowLegendOnChart() {
         return showLegendOnChart;
     }
 
     public void setShowLegendOnChart(boolean showLegendOnChart) {
         this.showLegendOnChart = showLegendOnChart;
     }
 
     public Color getChartBackgroundColor() {
         return chartBackgroundColor;
     }
 
     public void setChartBackgroundColor(Color chartBackgroundColor) {
         this.chartBackgroundColor = chartBackgroundColor;
     }
 
     public List<ColumnSettings> getTableColumns() {
         return tableColumns;
     }
 
     public void setTableColumns(List<ColumnSettings> columnSettings) {
         this.tableColumns = columnSettings;
     }
 
     public Rectangle getFrameLocation() {
         return frameLocation;
     }
 
     public void setFrameLocation(Rectangle frameBounds) {
         this.frameLocation = frameBounds;
     }
 
     public boolean isIcon() {
         return isIcon;
     }
 
     public void setIsIcon(boolean frameExtendedState) {
         this.isIcon = frameExtendedState;
     }
 
     public boolean isShown() {
         return isShown;
     }
 
     public void setShown(boolean shown) {
         isShown = shown;
     }
 
     public ChartType getChartType() {
         return chartType;
     }
 
     public void setChartType(ChartType chartType) {
         this.chartType = chartType;
     }
 
     public int getZPosition() {
         return zPosition;
     }
 
     public void setZPosition(int zPosition) {
         this.zPosition = zPosition;
     }
 
     //the readResolve method allows us to handle migrations where we add fields which need to
     //be initialised - xstream sets the fields null even if a default is
     //assigned when the field is defined
     private Object readResolve() {
         if ( domainStartTimeSelection == null) {
             domainStartTimeSelection = new DomainTimeSelection();
         }
 
         if ( chartType == null ) {
             chartType = ChartType.DEFAULT_CHART_TYPE;
         }
         return this;
     }
 
 }
