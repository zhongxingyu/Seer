 /*
  * This program is free software; you can redistribute it and/or modify it under the 
  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
  * Foundation.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this 
  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
  * or from the Free Software Foundation, Inc., 
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * Copyright 2008 Pentaho Corporation.  All rights reserved.
  */
 package org.pentaho.chart.plugin.openflashchart;
 
 import java.awt.Color;
 import java.awt.Paint;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import ofc4j.model.Chart;
 import ofc4j.model.Text;
 import ofc4j.model.axis.XAxis;
 import ofc4j.model.axis.YAxis;
 import ofc4j.model.axis.Label.Rotation;
 import ofc4j.model.elements.AreaHollowChart;
 import ofc4j.model.elements.BarChart;
 import ofc4j.model.elements.HorizontalBarChart;
 import ofc4j.model.elements.LineChart;
 import ofc4j.model.elements.PieChart;
 import ofc4j.model.elements.ScatterChart;
 import ofc4j.model.elements.SketchBarChart;
 import ofc4j.model.elements.StackedBarChart;
 import ofc4j.model.elements.BarChart.Bar;
 import ofc4j.model.elements.BarChart.Style;
 import ofc4j.model.elements.LineChart.Dot;
 import ofc4j.model.elements.PieChart.Slice;
 import ofc4j.model.elements.StackedBarChart.Stack;
 import ofc4j.model.elements.StackedBarChart.StackKey;
 import ofc4j.model.elements.StackedBarChart.StackValue;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.pentaho.chart.ChartDocumentContext;
 import org.pentaho.chart.ChartUtils;
 import org.pentaho.chart.core.ChartDocument;
 import org.pentaho.chart.core.ChartElement;
 import org.pentaho.chart.css.keys.ChartStyleKeys;
 import org.pentaho.chart.css.styles.ChartOrientationStyle;
 import org.pentaho.chart.css.styles.ChartSeriesType;
 import org.pentaho.chart.data.CategoricalDataModel;
 import org.pentaho.chart.data.ChartTableModel;
 import org.pentaho.chart.data.IChartDataModel;
 import org.pentaho.chart.data.MultiSeriesXYDataModel;
 import org.pentaho.chart.data.NamedValue;
 import org.pentaho.chart.data.NamedValuesDataModel;
 import org.pentaho.chart.data.XYDataModel;
 import org.pentaho.chart.data.XYDataPoint;
 import org.pentaho.chart.data.CategoricalDataModel.Category;
 import org.pentaho.chart.data.CategoricalDataModel.Series;
 import org.pentaho.chart.model.AreaPlot;
 import org.pentaho.chart.model.BarPlot;
 import org.pentaho.chart.model.ChartModel;
 import org.pentaho.chart.model.LinePlot;
 import org.pentaho.chart.model.NumericAxis;
 import org.pentaho.chart.model.Palette;
 import org.pentaho.chart.model.PiePlot;
 import org.pentaho.chart.model.Plot;
 import org.pentaho.chart.model.ScatterPlot;
 import org.pentaho.chart.model.StyledText;
 import org.pentaho.chart.model.TwoAxisPlot;
 import org.pentaho.chart.model.Axis.LabelOrientation;
 import org.pentaho.chart.model.BarPlot.BarPlotFlavor;
 import org.pentaho.chart.model.Plot.Orientation;
 import org.pentaho.chart.plugin.IChartPlugin;
 import org.pentaho.chart.plugin.api.ChartResult;
 import org.pentaho.chart.plugin.api.IOutput;
 import org.pentaho.chart.plugin.jfreechart.utils.ColorFactory;
 import org.pentaho.chart.plugin.openflashchart.outputs.OpenFlashChartOutput;
 import org.pentaho.reporting.libraries.css.dom.LayoutStyle;
 import org.pentaho.reporting.libraries.css.keys.border.BorderStyleKeys;
 import org.pentaho.reporting.libraries.css.keys.color.ColorStyleKeys;
 import org.pentaho.reporting.libraries.css.values.CSSConstant;
 import org.pentaho.reporting.libraries.css.values.CSSNumericValue;
 import org.pentaho.reporting.libraries.css.values.CSSValue;
 import org.pentaho.util.messages.Messages;
 
 public class OpenFlashChartFactoryEngine implements Serializable {
 
   private static final Log logger = LogFactory.getLog(OpenFlashChartFactoryEngine.class);
 
   private static final long serialVersionUID = -1079376910255750394L;
   
   private static final int STACK_MIN_INDEX = 0;
   private static final int STACK_MAX_INDEX = 1;
   
   private class AxisConfiguration {
     private AxisConfiguration(int minValue, int maxValue, int stepSize) {
       this.maxValue= maxValue;
       this.minValue = minValue;
       this.stepSize = stepSize;
     }
     
     int minValue;
     int maxValue;
     int stepSize;
   }
 
   public OpenFlashChartFactoryEngine() {
   }
 
   public IOutput makeChart(ChartModel chartModel, IChartDataModel chartTableModel) {
     IOutput chartOutput = null;
     if (chartModel.getPlot() instanceof BarPlot) {
       chartOutput = new OpenFlashChartOutput(makeBarChart(chartModel, (CategoricalDataModel)chartTableModel));
     } else if (chartModel.getPlot() instanceof LinePlot) {
       chartOutput = new OpenFlashChartOutput(makeLineChart(chartModel, (CategoricalDataModel)chartTableModel));
     } else if (chartModel.getPlot() instanceof AreaPlot) {
       chartOutput = new OpenFlashChartOutput(makeAreaChart(chartModel, (CategoricalDataModel)chartTableModel));
     } else if (chartModel.getPlot() instanceof ScatterPlot) {
       if (chartTableModel instanceof MultiSeriesXYDataModel) {
         chartOutput = new OpenFlashChartOutput(makeScatterChart(chartModel, (MultiSeriesXYDataModel)chartTableModel));
       } else {
         chartOutput = new OpenFlashChartOutput(makeScatterChart(chartModel, (XYDataModel)chartTableModel));
       }
     } else if (chartModel.getPlot() instanceof PiePlot) {
       chartOutput = new OpenFlashChartOutput(makePieChart(chartModel, (NamedValuesDataModel)chartTableModel));
     }
     return chartOutput;
   }
   
   public Text getText(StyledText styledText) {
     Text text = null;
     if ((styledText != null) && (styledText.getText() != null) && (styledText.getText().trim().length() > 0)) {
       if (styledText.getStyle().getStyleString().length() > 0) {
         text = new Text(styledText.getText(), styledText.getStyle().getStyleString());
       } else {
         text = new Text(styledText.getText());
       }
     }
     return text;
   }
 
   private Chart createBasicGraphChart(ChartModel chartModel) {
     Chart chart = createBasicChart(chartModel);
 
     Text horizontalAxisLabel = getText(((TwoAxisPlot)chartModel.getPlot()).getHorizontalAxis().getLegend());
     Text verticalAxisLabel = getText(((TwoAxisPlot)chartModel.getPlot()).getVerticalAxis().getLegend());
 
     if (horizontalAxisLabel != null) {
       chart.setXLegend(horizontalAxisLabel);
     }
     if (verticalAxisLabel != null) {
       chart.setYLegend(verticalAxisLabel);
     }
     return chart;
   }
 
   private Chart createBasicChart(ChartModel chartModel) {
     Chart chart = null;
     if ((chartModel.getTitle() != null) && (chartModel.getTitle().getText() != null) && chartModel.getTitle().getText().trim().length() > 0) {
       String cssFontStyleString = chartModel.getTitle().getStyle().getStyleString();
       if (cssFontStyleString.trim().length() > 0) {
         chart = new Chart(chartModel.getTitle().getText(), cssFontStyleString);
       } else {
         chart = new Chart(chartModel.getTitle().getText());
       }
     } else {
       chart = new Chart();
     }
 
     if (chartModel.getBackground() instanceof Integer) {
       chart.setBackgroundColour("#" + Integer.toHexString(0x00FFFFFF & (Integer)chartModel.getBackground()));
     } else {
       chart.setBackgroundColour("#" + Integer.toHexString(0x00FFFFFF & Color.WHITE.getRGB()));
     }
 
     if (chartModel.getPlot().getBackground() instanceof Integer) {
       chart.setInnerBackgroundColour("#" + Integer.toHexString(0x00FFFFFF & (Integer)chartModel.getPlot().getBackground()));
     }    
     return chart;
   }
   
   private YAxis createYAxis(AxisConfiguration rangeDescription) {
     
     YAxis ya = new YAxis();
     ya.setRange(rangeDescription.minValue, rangeDescription.maxValue, rangeDescription.stepSize);
     return ya;
   }
   
   private XAxis createXAxis(LabelOrientation labelOrientation, AxisConfiguration rangeDescription) {
     Rotation rotation = Rotation.HORIZONTAL;
     if ((labelOrientation != null) && (labelOrientation != LabelOrientation.HORIZONTAL)) {
       switch (labelOrientation) {
         case DIAGONAL:
           rotation = Rotation.DIAGONAL;
           break;
         case VERTICAL:
           rotation = Rotation.VERTICAL;
           break;
       }
     }
     
     XAxis xa = new XAxis();
     xa.setRange(rangeDescription.minValue, rangeDescription.maxValue, rangeDescription.stepSize);
     xa.getLabels().setRotation(rotation);
     return xa;
   }
   
   private XAxis createXAxis(TwoAxisPlot graphPlot, List<Category> categories) {
     Rotation rotation = Rotation.HORIZONTAL;
     LabelOrientation labelOrientation = graphPlot.getHorizontalAxis().getLabelOrientation();
     if ((labelOrientation != null) && (labelOrientation != LabelOrientation.HORIZONTAL)) {
       switch (labelOrientation) {
         case DIAGONAL:
           rotation = Rotation.DIAGONAL;
           break;
         case VERTICAL:
           rotation = Rotation.VERTICAL;
           break;
       }
     }
     
     List<String> labels = new ArrayList<String>();
     for (Category category : categories) {
       labels.add(category.getCategoryName());
     }
     XAxis xa = new XAxis();
     xa.setLabels(labels);
     xa.getLabels().setRotation(rotation);
 //    xa.setMax(labels.size() - 1);
     return xa;
   }
   
   private YAxis createYAxis(List<Category> categories) {
     YAxis ya = new YAxis();
     ArrayList<String> labels = new ArrayList<String>();
     for (Category category : categories) {
       labels.add(category.getCategoryName());
     }
     ya.setLabels(labels.toArray(new String[0]));
     
     // HACK: If you try to set the max size, OFC2 will add one to max; otherwise, allow 
     // him to calculate the max, and the axis will render the appropriate steps.
     
     //ya.setMax(labels.size());
     
     // The category labels set up aligned properly if offset = true.
     ya.setOffset(true);
     
     return ya;
   }
   
   public Chart makeAreaChart(ChartModel chartModel, CategoricalDataModel chartTableModel) {
     Chart chart = createBasicGraphChart(chartModel);
     AreaPlot areaPlot = (AreaPlot)chartModel.getPlot();
 
     chart.setXAxis(createXAxis(areaPlot, chartTableModel.getCategories()));
     Palette palette = getPalette(areaPlot);
     
     int idx = 0;
     for (Series series : chartTableModel.getSeries()) {
       
       AreaHollowChart areaChart = new AreaHollowChart();
       areaChart.setHaloSize(0);
       areaChart.setWidth(2);
       areaChart.setDotSize(4);
 
       if (areaPlot.getOpacity() != null) {
         areaChart.setAlpha(areaPlot.getOpacity());
       }
       if ((chartModel.getLegend() != null) && chartModel.getLegend().getVisible()) {
         areaChart.setText(series.getSeriesName());
         Integer legendSize = chartModel.getLegend().getFontSize();
         if ((legendSize != null) && (legendSize > 0)) {
           areaChart.setFontSize(legendSize);
         }
       }
       areaChart.setTooltip("#val#");
       if (palette.size() > idx) {
         String colorString = "#" + Integer.toHexString(0x00FFFFFF & palette.get(idx));
         areaChart.setFill(colorString);
         areaChart.setColour(colorString);
       }
       
       ArrayList<Dot> dots = new ArrayList<Dot>();
       
       for (NamedValue namedValue : series) {
         Number value = namedValue.getValue();
         if (value == null) {
           dots.add(null);
         } else {
           dots.add(new Dot(scaleNumber(value, chartTableModel.getScalingFactor())));
         }
       }
 
       areaChart.addDots(dots);
       chart.addElements(areaChart);
       
       idx++;
     }
     
     AxisConfiguration rangeDescription = getAxisConfiguration(areaPlot, chartTableModel);
     if (rangeDescription != null) {
       chart.setYAxis(createYAxis(rangeDescription));
     }
 
     return chart;
   }
   
   public Chart makePieChart(ChartModel chartModel, NamedValuesDataModel chartTableModel) {
     
     PieChart pieChart = new PieChart();
     PiePlot piePlot = (PiePlot)chartModel.getPlot();
     pieChart.setAnimate(piePlot.getAnimate());
     pieChart.setBorder(2);
     if (piePlot.getLabels().getVisible() && (piePlot.getLabels().getFontSize() != null)) {
       pieChart.setFontSize(piePlot.getLabels().getFontSize());
     }
     
     if (piePlot.getStartAngle() != null) {
       pieChart.setStartAngle(piePlot.getStartAngle());
     }
 
     if (piePlot.getOpacity() != null) {
       pieChart.setAlpha(piePlot.getOpacity());
     }
 
     ArrayList<Slice> slices = new ArrayList<Slice>();
     for (NamedValue chartDataPoint : chartTableModel) {
       Number value = chartDataPoint.getValue();
       if (value != null) {
         Slice slice = null;
         if (piePlot.getLabels().getVisible()) {
           slice = new Slice(scaleNumber(value, chartTableModel.getScalingFactor()), "", chartDataPoint.getName());
         } else {
           slice = new Slice(scaleNumber(value, chartTableModel.getScalingFactor()), "");
           slice.setTooltip(chartDataPoint.getName() + " - " + value.toString());
         }
         slices.add(slice);
       }
     }
     pieChart.addSlices(slices);
 
     Palette palette = getPalette(piePlot);
     
     ArrayList<String> strColors = new ArrayList<String>();
     for (Integer color : palette) {
       strColors.add("#" + Integer.toHexString(0x00FFFFFF & color));
     }
     pieChart.setColours(strColors);
 
     Chart chart = createBasicChart(chartModel);
     chart.addElements(pieChart);
     return chart;
   }
   
   private Palette getPalette(Plot plot) {
     Palette palette = new Palette();
     if (plot.getPalette() != null) {
       palette.addAll(plot.getPalette());
     }
     
     ArrayList<Integer> defaultColors = new ArrayList<Integer>(Plot.DEFAULT_PALETTE);
     defaultColors.removeAll(palette);
     palette.addAll(defaultColors);
     
     return palette;
   }
   
   private AxisConfiguration getAxisConfiguration(NumericAxis axis, List<Number> axisValues) {
 
     Number minValue = axis.getMinValue();
     Number maxValue = axis.getMaxValue();
 
     boolean calculateMinValue = (minValue == null);
     boolean calculateMaxValue = (maxValue == null);
 
     boolean hasChartData = false;
 
     for (Number value : axisValues) {
       if (calculateMaxValue) {
         if (maxValue == null) {
           maxValue = value;
         } else if (value != null) {
           maxValue = Math.max(maxValue.doubleValue(), value.doubleValue());
         }
       }
 
       if (calculateMinValue) {
         if (minValue == null) {
           minValue = value;
         } else if (value != null) {
           minValue = Math.min(minValue.doubleValue(), value.doubleValue());
         }
       }
 
       hasChartData = hasChartData || (value != null);
     }
     
     AxisConfiguration rangeDescription = null;
     
     if (hasChartData) {
       if (calculateMinValue) {
         minValue = Math.min(0, minValue.doubleValue());
       } 
       minValue = Math.floor(minValue.doubleValue());
       maxValue = Math.ceil(maxValue.doubleValue());
       
       if (maxValue.equals(minValue)) {
         maxValue = maxValue.intValue() + 1;
       }
       
       Number spread = maxValue.doubleValue() - minValue.doubleValue();
       
       int exponent = Integer.toString(Math.abs(spread.intValue())).length() - 1;
 
       int stepSize = (int) (((long) (spread.intValue() / Math.pow(10, exponent))) * Math.pow(10, exponent - 1)) * 2;
       if (stepSize < 1) {
         stepSize = 1;
       }
       
       if ((maxValue.doubleValue() % stepSize) != 0) {
         maxValue = (maxValue.doubleValue() - (maxValue.doubleValue() % stepSize)) + stepSize;
       }
       
       rangeDescription = new AxisConfiguration(minValue.intValue(), maxValue.intValue(), stepSize);
     }
     
     return rangeDescription;
   }
   
   private AxisConfiguration getAxisConfiguration(TwoAxisPlot graphPlot, CategoricalDataModel chartTableModel) {
     Number minValue = graphPlot.getRangeAxis().getMinValue();
     Number maxValue = graphPlot.getRangeAxis().getMaxValue();
     
     boolean calculateMinValue = (minValue == null);
     boolean calculateMaxValue = (maxValue == null);
     
     boolean hasChartData = false;
     
     List<Series> seriesList = chartTableModel.getSeries();
     
     if ((graphPlot instanceof BarPlot) && (((BarPlot)graphPlot).getFlavor() == BarPlotFlavor.STACKED)) {
       int numCategories = chartTableModel.getCategories().size();
       Number[][] stackRanges = new Number[numCategories][];
       for (Series series : seriesList) {
         int index = 0;
         for (NamedValue namedValue : series) {
           Number value = namedValue.getValue();
           hasChartData = hasChartData || (value != null);
           if (value != null) {
             value = scaleNumber(value, chartTableModel.getScalingFactor());
             Number[] stackRange = stackRanges[index];
             if (stackRange == null) {
               if (value.doubleValue() < 0) {
                 stackRanges[index] = new Number[] { value, 0 };
               } else {
                 stackRanges[index] = new Number[] { 0, value };
               }
             } else {
               if (value.doubleValue() < 0) {
                 stackRange[STACK_MIN_INDEX] = stackRange[STACK_MIN_INDEX].doubleValue() + value.doubleValue();
               } else {
                 stackRange[STACK_MAX_INDEX] = stackRange[STACK_MAX_INDEX].doubleValue() + value.doubleValue();
               }
             }
           }
           index++;
         }
       }
       if (hasChartData) {
         for (int column = 0; column < numCategories; column++) {
           Number[] stackRange = stackRanges[column];
           
           if (calculateMaxValue) {
             if (maxValue == null) {
               maxValue = stackRange[STACK_MAX_INDEX];
             } else if (stackRange[STACK_MAX_INDEX] != null) {
               maxValue = Math.max(maxValue.doubleValue(), stackRange[STACK_MAX_INDEX].doubleValue());
             }
           }
           
           if (calculateMinValue) {
             if (minValue == null) {
               minValue = stackRange[STACK_MIN_INDEX];
             } else if (stackRange[STACK_MIN_INDEX] != null) {
               minValue = Math.min(minValue.doubleValue(), stackRange[STACK_MIN_INDEX].doubleValue());
             }
           }
         }
       }
     } else {
       for (Series series : seriesList) {
         for (NamedValue namedValue : series) {
           Number value = namedValue.getValue();
           hasChartData = hasChartData || (value != null);
           if (value != null) {
             value = scaleNumber(value, chartTableModel.getScalingFactor());
           }
           if (calculateMaxValue) {
             if (maxValue == null) {
               maxValue = value;
             } else if (value != null) {
               maxValue = Math.max(maxValue.doubleValue(), value.doubleValue());
             }
           }
           
           if (calculateMinValue) {
             if (minValue == null) {
               minValue = value;
             } else if (value != null) {
               minValue = Math.min(minValue.doubleValue(), value.doubleValue());
             }
           }
           
         }
       }
     }
 
     AxisConfiguration rangeDescription = null;
     
     if (hasChartData) {
     	if (calculateMinValue) {
     		minValue = Math.min(0, minValue.doubleValue());
     	}	
       minValue = Math.floor(minValue.doubleValue());
       maxValue = Math.ceil(maxValue.doubleValue());
       
       if (maxValue.equals(minValue)) {
         maxValue = maxValue.intValue() + 1;
       }
       
       Number spread = maxValue.doubleValue() - minValue.doubleValue();
       
       int exponent = Integer.toString(Math.abs(spread.intValue())).length() - 1;
 
       int stepSize = (int) (((long) (spread.intValue() / Math.pow(10, exponent))) * Math.pow(10, exponent - 1)) * 2;
       if (stepSize < 1) {
         stepSize = 1;
       }
       
       if ((maxValue.doubleValue() % stepSize) != 0) {
         maxValue = (maxValue.doubleValue() - (maxValue.doubleValue() % stepSize)) + stepSize;
       }
       
       rangeDescription = new AxisConfiguration(minValue.intValue(), maxValue.intValue(), stepSize);
     }
     
     return rangeDescription;
   }
   
   private StackedBarChart makeStackedBarChart(ChartModel chartModel, CategoricalDataModel chartTableModel) {    
     StackedBarChart stackedBarChart = new StackedBarChart();
     
 
     BarPlot barPlot = (BarPlot)chartModel.getPlot();
     Palette palette = getPalette(barPlot);
     
     if (barPlot.getOpacity() != null) {
       stackedBarChart.setAlpha(barPlot.getOpacity());
     }
     
     boolean firstCategory = true;
     for (Category category : chartTableModel.getCategories()) {
       Stack stack = stackedBarChart.newStack();
       int index = 0;
       for (NamedValue namedValue : category) {
         String color = "#" + Integer.toHexString(0x00FFFFFF & palette.get(index));
         if (firstCategory && (chartModel.getLegend() != null) && chartModel.getLegend().getVisible()) {
           StackKey key = new StackKey();
           key.setText(namedValue.getName());
           key.setColour(color);
           Integer legendSize = chartModel.getLegend().getFontSize();
           if ((legendSize != null) && (legendSize > 0)) {
             stackedBarChart.setFontSize(legendSize);
           }
           stackedBarChart.addKeys(key);
         }
         
         Number value = namedValue.getValue();
         if ((value != null) && !value.equals(0)) {
           StackValue stackValue = new StackValue(scaleNumber(value, chartTableModel.getScalingFactor()), color);
           stack.addStackValues(stackValue);
         }
         index++;
       }
       firstCategory = false;
     }
     
     return stackedBarChart;
   }
   
   private Chart makeScatterChart(ChartModel chartModel, XYDataModel chartTableModel) {        
     ScatterPlot scatterPlot = (ScatterPlot)chartModel.getPlot();
     Chart chart = null;
     
     List<Number> axisValues = new ArrayList<Number>();
     for (XYDataPoint xyDataPoint : chartTableModel) {
       axisValues.add(xyDataPoint.getX());
     }
     AxisConfiguration xAxisConfiguration = getAxisConfiguration(scatterPlot.getXAxis(), axisValues);
     
     axisValues.clear();
     for (XYDataPoint xyDataPoint : chartTableModel) {
       axisValues.add(xyDataPoint.getY());
     }    
     AxisConfiguration yAxisConfiguration = getAxisConfiguration(scatterPlot.getYAxis(), axisValues);
     
     if ((xAxisConfiguration != null) && (yAxisConfiguration != null)) {
       chart = createBasicGraphChart(chartModel);
       chart.setXAxis(createXAxis(scatterPlot.getXAxis().getLabelOrientation(), xAxisConfiguration));
       chart.setYAxis(createYAxis(yAxisConfiguration));
       
       
       ScatterChart sc = new ScatterChart(""); //$NON-NLS-1$
       String color = "#000000";
       if (scatterPlot.getPalette().size() > 0) {
         color = "#" + Integer.toHexString(0x00FFFFFF & scatterPlot.getPalette().get(0));
       }
       sc.setColour(color);
       sc.setDotSize(3);
       if (scatterPlot.getOpacity() != null) {
         sc.setAlpha(scatterPlot.getOpacity());
       }
       
       for (XYDataPoint xyDataPoint : chartTableModel) {
         sc.addPoint(xyDataPoint.getX(), xyDataPoint.getY());      
       }
       
       chart.addElements(sc);
     }
     
     return chart;
   }
   
   private Chart makeScatterChart(ChartModel chartModel, MultiSeriesXYDataModel chartTableModel) {        
     ScatterPlot scatterPlot = (ScatterPlot)chartModel.getPlot();
     Chart chart = null;
     
     List<Number> xAxisValues = new ArrayList<Number>();
     List<Number> yAxisValues = new ArrayList<Number>();
     for (org.pentaho.chart.data.MultiSeriesXYDataModel.Series series : chartTableModel.getSeries()) {
       for (XYDataPoint xyDataPoint : series) {
         xAxisValues.add(xyDataPoint.getX());
         yAxisValues.add(xyDataPoint.getY());
       }
     }
     AxisConfiguration xAxisConfiguration = getAxisConfiguration(scatterPlot.getXAxis(), xAxisValues);
     AxisConfiguration yAxisConfiguration = getAxisConfiguration(scatterPlot.getXAxis(), yAxisValues);
     
     if ((xAxisConfiguration != null) && (yAxisConfiguration != null)) {
       chart = createBasicGraphChart(chartModel);
       
       int index = 0;
       for (org.pentaho.chart.data.MultiSeriesXYDataModel.Series series : chartTableModel.getSeries()) {
         chart.setXAxis(createXAxis(scatterPlot.getXAxis().getLabelOrientation(), xAxisConfiguration));
         chart.setYAxis(createYAxis(yAxisConfiguration));
         
         
         ScatterChart sc = new ScatterChart(""); //$NON-NLS-1$
         String color = "#000000";
         if (scatterPlot.getPalette().size() > index) {
           color = "#" + Integer.toHexString(0x00FFFFFF & scatterPlot.getPalette().get(index));
         }
         sc.setColour(color);
         sc.setDotSize(3);
         if (scatterPlot.getOpacity() != null) {
           sc.setAlpha(scatterPlot.getOpacity());
         }
         
         for (XYDataPoint xyDataPoint : series) {
           sc.addPoint(xyDataPoint.getX(), xyDataPoint.getY());      
         }
         
         chart.addElements(sc);
         index++;
       }
     }
     
     return chart;
   }
   
   private HorizontalBarChart makeHorizontalBarChart(ChartModel chartModel, Series dataSeries, int seriesIdx, Number scalingFactor) {
     HorizontalBarChart horizontalBarChart = new HorizontalBarChart();
     
     if ((chartModel.getLegend() != null) && chartModel.getLegend().getVisible()) {
       horizontalBarChart.setText(dataSeries.getSeriesName());
       Integer legendSize = chartModel.getLegend().getFontSize();
       if ((legendSize != null) && (legendSize > 0)) {
         horizontalBarChart.setFontSize(legendSize);
       }
     }
     horizontalBarChart.setTooltip("#val#");
     
     BarPlot barPlot = (BarPlot) chartModel.getPlot();    
     Palette palette = getPalette(barPlot);
     if (barPlot.getOpacity() != null) {
       horizontalBarChart.setAlpha(barPlot.getOpacity());
     }
     if (palette.size() > seriesIdx) {
       horizontalBarChart.setColour("#" + Integer.toHexString(0x00FFFFFF & palette.get(seriesIdx)));
     }
     
     ArrayList<ofc4j.model.elements.HorizontalBarChart.Bar> bars = new ArrayList<ofc4j.model.elements.HorizontalBarChart.Bar>();
     for (NamedValue namedValue : dataSeries) {
       Number value = namedValue.getValue();
       if (value == null) {
         bars.add(null);
       } else {
         bars.add(new ofc4j.model.elements.HorizontalBarChart.Bar(scaleNumber(value, scalingFactor)));
       }
     }
     
     horizontalBarChart.addBars(bars);
     return horizontalBarChart;
   }
   
   private BarChart makeVerticalBarChart(ChartModel chartModel, Series dataSeries, int seriesIdx, Number scalingFactor) {
     BarChart verticalBarChart = null;
     BarPlot barPlot = (BarPlot) chartModel.getPlot();
     Palette palette = getPalette(barPlot);
     if (barPlot.getFlavor() != null) {
       switch (barPlot.getFlavor()) {
         case THREED:
           verticalBarChart = new BarChart(Style.THREED);
           break;
         case GLASS:
           verticalBarChart = new BarChart(Style.GLASS);
           break;
         case SKETCH:
           verticalBarChart = new SketchBarChart();
           break;
         default:
           verticalBarChart = new BarChart();
           break;
       }
     } else {
       verticalBarChart = new BarChart();
     }
     
     if ((chartModel.getLegend() != null) && chartModel.getLegend().getVisible()) {
       verticalBarChart.setText(dataSeries.getSeriesName());
       Integer legendSize = chartModel.getLegend().getFontSize();
       if ((legendSize != null) && (legendSize > 0)) {
         verticalBarChart.setFontSize(legendSize);
       }
     }
     
     verticalBarChart.setTooltip("#val#");
     if (barPlot.getOpacity() != null) {
       verticalBarChart.setAlpha(barPlot.getOpacity());
     }
     
     
     if (palette.size() > seriesIdx) {
       verticalBarChart.setColour("#" + Integer.toHexString(0x00FFFFFF & palette.get(seriesIdx)));
     }
     
     ArrayList<Bar> bars = new ArrayList<Bar>();
     for (NamedValue namedValue : dataSeries) {
       Number value = namedValue.getValue();
       if (value == null) {
         bars.add(null);
       } else {
         Bar bar = new Bar(scaleNumber(value, scalingFactor));
         bars.add(bar);
       }
     }
     
     verticalBarChart.addBars(bars);
     return verticalBarChart;
   }
   
   public Chart makeBarChart(ChartModel chartModel, CategoricalDataModel dataModel) {
     Chart chart = createBasicGraphChart(chartModel);
     BarPlot barPlot = (BarPlot) chartModel.getPlot();
 
     if (Orientation.HORIZONTAL.equals(barPlot.getOrientation())) {
 
       int index = 0;
       for (Series series : dataModel.getSeries()) {
         chart.addElements(makeHorizontalBarChart(chartModel, series, index, dataModel.getScalingFactor()));
         index++;
       }
 
       List<Category> categories = dataModel.getCategories();
       // BISERVER-3075 hack for bug in OFC2 where categories are rendered backwards...
       Collections.reverse(categories);
       chart.setYAxis(createYAxis(categories));
       
       AxisConfiguration rangeDescription = getAxisConfiguration(barPlot, dataModel);
       if (rangeDescription != null) {
         chart.setXAxis(createXAxis(barPlot.getHorizontalAxis().getLabelOrientation(), rangeDescription));
       }
     } else {
       if (barPlot.getFlavor() == BarPlotFlavor.STACKED) {
         chart.addElements(makeStackedBarChart(chartModel, dataModel));
       } else {
         int index = 0;
         for (Series series : dataModel.getSeries()) {
           chart.addElements(makeVerticalBarChart(chartModel, series, index, dataModel.getScalingFactor()));
           index++;
         }
       }
       
       chart.setXAxis(createXAxis(barPlot, dataModel.getCategories()));
       if (barPlot.getFlavor() == BarPlotFlavor.THREED) {
         chart.getXAxis().set3D(3);
       }
       
       AxisConfiguration rangeDescription = getAxisConfiguration(barPlot, dataModel);
       if (rangeDescription != null) {
         chart.setYAxis(createYAxis(rangeDescription));
       }
     }
 
     return chart;
   }
   
   public Chart makeLineChart(ChartModel chartModel, CategoricalDataModel chartTableModel) {
 
     Chart chart = createBasicGraphChart(chartModel);
     LinePlot linePlot = (LinePlot)chartModel.getPlot();
 
     chart.setXAxis(createXAxis(linePlot, chartTableModel.getCategories()));
 
     Palette palette = getPalette(linePlot);
 
     int index = 0;
     for (Series dataSeries : chartTableModel.getSeries()) {
       LineChart lineChart = new LineChart(LineChart.Style.DOT);
       lineChart.setHaloSize(0);
       
       Integer lineWidth = linePlot.getLineWidth();
       lineChart.setWidth((lineWidth == null || lineWidth <= 0) ? 1 : lineWidth);
       lineChart.setDotSize(lineChart.getWidth() + 2);
       if (linePlot.getOpacity() != null) {
         lineChart.setAlpha(linePlot.getOpacity());
       }
 
       if ((chartModel.getLegend() != null) && chartModel.getLegend().getVisible()) {
         lineChart.setText(dataSeries.getSeriesName());
         Integer legendSize = chartModel.getLegend().getFontSize();
         if ((legendSize != null) && (legendSize > 0)) {
           lineChart.setFontSize(legendSize);
         }
       }
       
       lineChart.setTooltip("#val#");
       
       if (palette.size() > index) {
         lineChart.setColour("#" + Integer.toHexString(0x00FFFFFF & palette.get(index)));
       }
       
       ArrayList<Dot> dots = new ArrayList<Dot>();
       for (NamedValue namedValue : dataSeries) {
         Number value = namedValue.getValue();
         if (value == null) {
           dots.add(null);
         } else {
           dots.add(new Dot(scaleNumber(value, chartTableModel.getScalingFactor())));
         }
       }
 
       lineChart.addDots(dots);
       chart.addElements(lineChart);
       
       index++;
     }
 
     AxisConfiguration rangeDescription = getAxisConfiguration(linePlot, chartTableModel);
     if (rangeDescription != null) {
       chart.setYAxis(createYAxis(rangeDescription));
     }
 
     return chart;
   }
   
   /**
    * @deprecated
    */
   public IOutput makeChart(ChartTableModel chartTableModel, ChartDocumentContext chartDocumentContext, ChartResult chartResult) {
     final CSSConstant currentChartType = determineChartType(chartDocumentContext.getChartDocument());
     IOutput chartOutput = null;
     try {
       if (currentChartType == ChartSeriesType.BAR) {
         chartOutput = new OpenFlashChartOutput(makeBarChart(chartTableModel, chartDocumentContext));
       } else if (currentChartType == ChartSeriesType.LINE) {
         chartOutput = new OpenFlashChartOutput(makeLineChart(chartTableModel, chartDocumentContext));
       } else if (currentChartType == ChartSeriesType.AREA) {
         chartOutput = new OpenFlashChartOutput((makeAreaChart(chartTableModel, chartDocumentContext)));
       } else if (currentChartType == ChartSeriesType.PIE) {
         chartOutput = new OpenFlashChartOutput((makePieChart(chartTableModel, chartDocumentContext)));
       } else if (currentChartType == ChartSeriesType.DIAL) {
         chartOutput = new OpenFlashChartOutput((makeDialChart(chartTableModel, chartDocumentContext)));
       } else {
         chartResult.setErrorCode(IChartPlugin.ERROR_INDETERMINATE_CHART_TYPE);
         chartResult.setDescription(Messages.getErrorString("JFreeChartPlugin.ERROR_0001_CHART_TYPE_INDETERMINABLE")); //$NON-NLS-1$
       }
     } catch (Exception e) {
       chartResult.setErrorCode(IChartPlugin.RESULT_ERROR);
       chartResult.setDescription(e.getLocalizedMessage());
     }
     return chartOutput;
   }
 
   /**
    * @deprecated
    */
   public Chart makeAreaChart(final ChartTableModel chartTableModel, final ChartDocumentContext chartDocumentContext) {
     ChartDocument chartDocument = chartDocumentContext.getChartDocument();
     Chart chart = createBasicGraphChart(chartDocument);
     boolean showLegend = showLegend(chartDocument);
 
     ArrayList<String> domainValues = new ArrayList<String>();
     for (int column = 0; column < chartTableModel.getColumnCount(); column++) {
       domainValues.add(chartTableModel.getColumnName(column));
     }
     if (domainValues.size() > 0) {
       XAxis xa = new XAxis();
       xa.setLabels(domainValues);
       xa.setMax(domainValues.size() - 1);
       chart.setXAxis(xa);
     }
 
     final ChartElement[] seriesElements = chartDocument.getRootElement().findChildrenByName(
         ChartElement.TAG_NAME_SERIES);
     CSSNumericValue opacity = (CSSNumericValue) chartDocument.getPlotElement().getLayoutStyle().getValue(
         ColorStyleKeys.OPACITY);
 
     Number maxValue = null;
     Number minValue = null;
     for (int row = 0; row < chartTableModel.getRowCount(); row++) {
       AreaHollowChart areaChart = new AreaHollowChart();
       areaChart.setHaloSize(0);
       areaChart.setWidth(2);
       areaChart.setDotSize(4);
 
       if (opacity != null) {
         areaChart.setAlpha((float) opacity.getValue());
       }
 
       if (showLegend) {
         areaChart.setText(chartTableModel.getRowName(row));
       }
       areaChart.setTooltip("#val#");
       if ((seriesElements != null) && (seriesElements.length > row)) {
         LayoutStyle layoutStyle = seriesElements[row].getLayoutStyle();
         Paint color = (layoutStyle != null ? (Paint) layoutStyle.getValue(ColorStyleKeys.COLOR) : null);
         if (color instanceof Color) {
           String colorString = "#" + Integer.toHexString(0x00FFFFFF & ((Color) color).getRGB());
           areaChart.setFill(colorString);
           areaChart.setColour(colorString);
         }
       }
       ArrayList<Dot> dots = new ArrayList<Dot>();
       for (int column = 0; column < chartTableModel.getColumnCount(); column++) {
         Number value = (Number) chartTableModel.getValueAt(row, column);
         if (maxValue == null) {
           maxValue = value;
         } else if (value != null) {
           maxValue = Math.max(maxValue.doubleValue(), value.doubleValue());
         }
         if (minValue == null) {
           minValue = value;
         } else if (value != null) {
           minValue = Math.min(minValue.doubleValue(), value.doubleValue());
         }
         dots.add(new Dot(value == null ? 0 : value));
       }
 
       areaChart.addDots(dots);
       chart.addElements(areaChart);
     }
 
     if ((maxValue != null) && (minValue != null)) {
       int exponent = Integer.toString(Math.abs(maxValue.intValue())).length() - 1;
 
       YAxis ya = new YAxis();
       int stepSize = (int) (((long) (maxValue.intValue() / Math.pow(10, exponent))) * Math.pow(10, exponent - 1));
       ya.setSteps(stepSize);
 
       ya.setMax((int) (maxValue.doubleValue() - (maxValue.doubleValue() % stepSize)) + stepSize);
       chart.setYAxis(ya);
     }
 
     return chart;
   }
 
   /**
    * @deprecated
    */
   public Chart makePieChart(final ChartTableModel chartTableModel, final ChartDocumentContext chartDocumentContext) {
     ChartDocument chartDocument = chartDocumentContext.getChartDocument();
 
     PieChart pieChart = new PieChart();
     pieChart.setAnimate(getAnimate(chartDocument));
     pieChart.setBorder(2);
     
     CSSNumericValue startAngle = (CSSNumericValue) chartDocument.getPlotElement().getLayoutStyle().getValue(ChartStyleKeys.PIE_START_ANGLE);
     if (startAngle != null) {
       pieChart.setStartAngle((int)startAngle.getValue());
     }
 
     CSSNumericValue opacity = (CSSNumericValue) chartDocument.getPlotElement().getLayoutStyle().getValue(
         ColorStyleKeys.OPACITY);
     if (opacity != null) {
       pieChart.setAlpha((float) opacity.getValue());
     }
 
     ArrayList<Slice> slices = new ArrayList<Slice>();
     for (int row = 0; row < chartTableModel.getRowCount(); row++) {
       Number value = (Number) chartTableModel.getValueAt(row, 0);
       Slice slice = new Slice(value, "#val#", chartTableModel.getRowName(row));
       slices.add(slice);
     }
     pieChart.addSlices(slices);
 
     ArrayList<String> colors = new ArrayList<String>();
     for (ChartElement seriesElement : chartDocument.getRootElement().findChildrenByName(ChartElement.TAG_NAME_SERIES)) {
       LayoutStyle layoutStyle = seriesElement.getLayoutStyle();
       Paint color = (layoutStyle != null ? (Paint) layoutStyle.getValue(ColorStyleKeys.COLOR) : null);
       if (color instanceof Color) {
         colors.add("#" + Integer.toHexString(0x00FFFFFF & ((Color) color).getRGB()));
       }
     }
     pieChart.setColours(colors);
 
     Chart chart = createBasicChart(chartDocument);
     chart.addElements(pieChart);
     return chart;
   }
 
   /**
    * @deprecated
    */
   public Chart makeDialChart(final ChartTableModel chartTableModel, final ChartDocumentContext chartDocumentContext) {
     Chart chartData = new Chart("The Title", "font-size: 14px; font-family: Verdana; text-align: center;");
     return chartData;
   }
 
   /**
    * @deprecated
    */
   private String createCssFontStyleString(ChartElement element) {
     StringBuffer cssStyleString = new StringBuffer();
     String fontFamily = ChartUtils.getFontFamily(element);
     float fontSize = ChartUtils.getFontSize(element);
     CSSValue fontStyle = ChartUtils.getFontStyle(element);
     CSSValue fontWeight = ChartUtils.getFontWeight(element);
     if ((fontFamily != null) && (fontFamily.trim().length() > 0)) {
       cssStyleString.append("font-family: " + fontFamily + ";");
     }
     if (fontSize > 0) {
       cssStyleString.append("font-size: " + fontSize + "px;");
     }
     if (fontStyle != null) {
       cssStyleString.append("font-style: " + fontStyle.getCSSText() + ";");
     }
     if (fontWeight != null) {
       cssStyleString.append("font-weight: " + fontWeight.getCSSText() + ";");
     }
     LayoutStyle layoutStyle = element.getLayoutStyle();
     Paint color = (layoutStyle != null ? (Paint) layoutStyle.getValue(ColorStyleKeys.COLOR) : null);
     if (color instanceof Color) {
       cssStyleString.append("color: #" + Integer.toHexString(0x00FFFFFF & ((Color) color).getRGB()));
     }
     return cssStyleString.length() > 0 ? cssStyleString.toString() : null;
   }
 
   /**
    * @deprecated
    */
   private Chart createBasicGraphChart(ChartDocument chartDocument) {
     Chart chart = createBasicChart(chartDocument);
 
     Text rangeLabel = getText(chartDocument, ChartElement.TAG_NAME_RANGE_LABEL);
     Text domainLabel = getText(chartDocument, ChartElement.TAG_NAME_DOMAIN_LABEL);
 
     if (domainLabel != null) {
       chart.setXLegend(domainLabel);
     }
     if (rangeLabel != null) {
       chart.setYLegend(rangeLabel);
     }
     return chart;
   }
   
   
   /**
   * @deprecated
   */
   private Chart createBasicChart(ChartDocument chartDocument) {
     String chartTitle = null;
     String cssFontStyleString = null;
 
     ChartElement rootElement = chartDocument.getRootElement();
     ChartElement plot = chartDocument.getPlotElement();
 
     final ChartElement[] children = rootElement.findChildrenByName(ChartElement.TAG_NAME_TITLE); //$NON-NLS-1$
     if (children != null && children.length > 0) {
       chartTitle = children[0].getText();
       cssFontStyleString = createCssFontStyleString(children[0]);
     }
     Chart chart = null;
     if ((chartTitle != null) && chartTitle.trim().length() > 0) {
       if (cssFontStyleString != null) {
         chart = new Chart(chartTitle, cssFontStyleString);
       } else {
         chart = new Chart(chartTitle);
       }
     } else {
       chart = new Chart();
     }
 
     Color chartBackgroundPaint = ColorFactory.getInstance().getColor(rootElement, BorderStyleKeys.BACKGROUND_COLOR);
     if (chartBackgroundPaint == null) {
       chartBackgroundPaint = Color.white;
     }
     chart.setBackgroundColour("#" + Integer.toHexString(0x00FFFFFF & chartBackgroundPaint.getRGB()));
 
     Color plotBackgroundColor = ColorFactory.getInstance().getColor(plot, BorderStyleKeys.BACKGROUND_COLOR);
     if (plotBackgroundColor != null) {
       chart.setInnerBackgroundColour("#" + Integer.toHexString(0x00FFFFFF & plotBackgroundColor.getRGB()));
     }
     return chart;
   }
 
   /**
    * @deprecated
    */
   private boolean showLegend(ChartDocument chartDocument) {
     ChartElement[] children = chartDocument.getRootElement().findChildrenByName(ChartElement.TAG_NAME_LEGEND); //$NON-NLS-1$
     return (children != null) && (children.length > 0);
   }
 
   /**
    * @deprecated
    */
   public Chart makeBarChart(final ChartTableModel chartTableModel, final ChartDocumentContext chartDocumentContext)
       throws Exception {
 
     ChartDocument chartDocument = chartDocumentContext.getChartDocument();
     Chart chart = createBasicGraphChart(chartDocument);
     boolean showLegend = showLegend(chartDocument);
 
     CSSValue orientation = getPlotOrientation(chartDocumentContext.getChartDocument());
     CSSNumericValue opacity = (CSSNumericValue) chartDocument.getPlotElement().getLayoutStyle().getValue(
         ColorStyleKeys.OPACITY);
 
     final ChartElement[] seriesElements = chartDocument.getRootElement().findChildrenByName(
         ChartElement.TAG_NAME_SERIES);
     BarPlotFlavor flavor = null;
     for (final ChartElement element : seriesElements) {
       CSSValue cssValue = element.getLayoutStyle().getValue(ChartStyleKeys.BAR_STYLE);
       if (cssValue != null) {
         String text = cssValue.getCSSText();
         for (BarPlotFlavor barPlotFlavor : BarPlotFlavor.values()) {
           if (barPlotFlavor.toString().equalsIgnoreCase(text)) {
             flavor = barPlotFlavor;
           }
         }
       }
     }
 
     if (ChartOrientationStyle.HORIZONTAL.equals(orientation)) {
       ArrayList<String> categories = new ArrayList<String>();
       for (int i = 0; i < chartTableModel.getColumnCount(); i++) {
         categories.add(chartTableModel.getColumnName(i));
       }
       if (categories.size() > 0) {
         YAxis ya = new YAxis();
         ya.setLabels(categories.toArray(new String[0]));
         ya.setMax(categories.size());
         chart.setYAxis(ya);
       }
 
       Number maxValue = null;
       Number minValue = null;
 
       for (int row = 0; row < chartTableModel.getRowCount(); row++) {
         HorizontalBarChart horizontalBarChart = new HorizontalBarChart();
         if (showLegend) {
           horizontalBarChart.setText(chartTableModel.getRowName(row));
         }
         horizontalBarChart.setTooltip("#val#");
         if (opacity != null) {
           horizontalBarChart.setAlpha((float) opacity.getValue());
         }
         if ((seriesElements != null) && (seriesElements.length > row)) {
           LayoutStyle layoutStyle = seriesElements[row].getLayoutStyle();
           Paint color = (layoutStyle != null ? (Paint) layoutStyle.getValue(ColorStyleKeys.COLOR) : null);
           if (color instanceof Color) {
             horizontalBarChart.setColour("#" + Integer.toHexString(0x00FFFFFF & ((Color) color).getRGB()));
           }
         }
         ArrayList<Number> values = new ArrayList<Number>();
         for (int column = 0; column < chartTableModel.getColumnCount(); column++) {
           Number value = (Number) chartTableModel.getValueAt(row, column);
           if (maxValue == null) {
             maxValue = value;
           } else if (value != null) {
             maxValue = Math.max(maxValue.doubleValue(), value.doubleValue());
           }
           if (minValue == null) {
             minValue = value;
           } else if (value != null) {
             minValue = Math.min(minValue.doubleValue(), value.doubleValue());
           }
           values.add(value == null ? 0 : value);
         }
 
         horizontalBarChart.addValues(values.toArray(new Number[0]));
         chart.addElements(horizontalBarChart);
       }
 
       if ((maxValue != null) && (minValue != null)) {
         int exponent = Integer.toString(Math.abs(maxValue.intValue())).length() - 1;
 
         XAxis xa = new XAxis();
         int stepSize = (int) (((long) (maxValue.intValue() / Math.pow(10, exponent))) * Math.pow(10, exponent - 1)) * 2;
         xa.setSteps(stepSize);
 
         xa.setMax((int) (maxValue.doubleValue() - (maxValue.doubleValue() % stepSize)) + stepSize);
         chart.setXAxis(xa);
       }
     } else {
 
       ArrayList<String> categories = new ArrayList<String>();
       for (int i = 0; i < chartTableModel.getColumnCount(); i++) {
         categories.add(chartTableModel.getColumnName(i));
       }
       if (categories.size() > 0) {
         XAxis xa = new XAxis();
         xa.setLabels(categories);
         xa.setMax(categories.size());
         chart.setXAxis(xa);
       }
 
       Number maxValue = null;
       Number minValue = null;
       for (int row = 0; row < chartTableModel.getRowCount(); row++) {
         BarChart verticalBarChart = null;
         if (flavor != null) {
           switch (flavor) {
             case THREED:
               verticalBarChart = new BarChart(Style.THREED);
               break;
             case GLASS:
               verticalBarChart = new BarChart(Style.GLASS);
               break;
             default:
               verticalBarChart = new BarChart();
               break;
           }
         } else {
           verticalBarChart = new BarChart();
         }
         if (showLegend) {
           verticalBarChart.setText(chartTableModel.getRowName(row));
         }
         verticalBarChart.setTooltip("#val#");
         if (opacity != null) {
           verticalBarChart.setAlpha((float) opacity.getValue());
         }
         if ((seriesElements != null) && (seriesElements.length > row)) {
           LayoutStyle layoutStyle = seriesElements[row].getLayoutStyle();
           Paint color = (layoutStyle != null ? (Paint) layoutStyle.getValue(ColorStyleKeys.COLOR) : null);
           if (color instanceof Color) {
             verticalBarChart.setColour("#" + Integer.toHexString(0x00FFFFFF & ((Color) color).getRGB()));
           }
         }
         ArrayList<Bar> bars = new ArrayList<Bar>();
         for (int column = 0; column < chartTableModel.getColumnCount(); column++) {
           Number value = (Number) chartTableModel.getValueAt(row, column);
           if (maxValue == null) {
             maxValue = value;
           } else if (value != null) {
             maxValue = Math.max(maxValue.doubleValue(), value.doubleValue());
           }
           if (minValue == null) {
             minValue = value;
           } else if (value != null) {
             minValue = Math.min(minValue.doubleValue(), value.doubleValue());
           }
           bars.add(new Bar(value == null ? 0 : value));
         }
 
         verticalBarChart.addBars(bars);
         chart.addElements(verticalBarChart);
       }
 
       if ((maxValue != null) && (minValue != null)) {
         int exponent = Integer.toString(Math.abs(maxValue.intValue())).length() - 1;
 
         YAxis ya = new YAxis();
         int stepSize = (int) (((long) (maxValue.intValue() / Math.pow(10, exponent))) * Math.pow(10, exponent - 1));
         ya.setSteps(stepSize);
 
         ya.setMax((int) (maxValue.doubleValue() - (maxValue.doubleValue() % stepSize)) + stepSize);
         chart.setYAxis(ya);
       }
     }
 
     return chart;
   }
 
   /**
    * @deprecated
    */
   public boolean getAnimate(ChartDocument chartDocument) {
     final ChartElement[] children = chartDocument.getRootElement().findChildrenByName("animate"); //$NON-NLS-1$
     return children != null && (children.length > 0) && Boolean.valueOf(children[0].getText());
   }
 
   /**
    * @deprecated
    */
   private CSSValue getPlotOrientation(final ChartDocument chartDocument) {
     CSSValue plotOrient = null;
     final ChartElement plotElement = chartDocument.getPlotElement();
 
     if (plotElement != null) {
       final LayoutStyle layoutStyle = plotElement.getLayoutStyle();
       plotOrient = layoutStyle.getValue(ChartStyleKeys.ORIENTATION);
     }
 
     return plotOrient;
   }
 
   /**
    * @deprecated
    */
   public Chart makeLineChart(final ChartTableModel chartTableModel, final ChartDocumentContext chartDocumentContext) {
 
     ChartDocument chartDocument = chartDocumentContext.getChartDocument();
     boolean showLegend = showLegend(chartDocument);
 
     Chart chart = createBasicGraphChart(chartDocument);
 
     ArrayList<String> domainValues = new ArrayList<String>();
     for (int column = 0; column < chartTableModel.getColumnCount(); column++) {
       domainValues.add(chartTableModel.getColumnName(column));
     }
     if (domainValues.size() > 0) {
       XAxis xa = new XAxis();
       xa.setLabels(domainValues);
       xa.setMax(domainValues.size());
       chart.setXAxis(xa);
     }
 
     CSSNumericValue opacity = (CSSNumericValue) chartDocument.getPlotElement().getLayoutStyle().getValue(
         ColorStyleKeys.OPACITY);
 
     final ChartElement[] seriesElements = chartDocument.getRootElement().findChildrenByName(
         ChartElement.TAG_NAME_SERIES);
 
     Number maxValue = null;
     Number minValue = null;
     for (int row = 0; row < chartTableModel.getRowCount(); row++) {
       LineChart lineChart = new LineChart(LineChart.Style.DOT);
       lineChart.setHaloSize(0);
       lineChart.setWidth(2);
       lineChart.setDotSize(4);
       if (opacity != null) {
         lineChart.setAlpha((float) opacity.getValue());
       }
 
       if (showLegend) {
         lineChart.setText(chartTableModel.getRowName(row));
       }
       lineChart.setTooltip("#val#");
       if ((seriesElements != null) && (seriesElements.length > row)) {
         LayoutStyle layoutStyle = seriesElements[row].getLayoutStyle();
         Paint color = (layoutStyle != null ? (Paint) layoutStyle.getValue(ColorStyleKeys.COLOR) : null);
         if (color instanceof Color) {
           lineChart.setColour("#" + Integer.toHexString(0x00FFFFFF & ((Color) color).getRGB()));
         }
       }
       ArrayList<Dot> dots = new ArrayList<Dot>();
       for (int column = 0; column < chartTableModel.getColumnCount(); column++) {
         Number value = (Number) chartTableModel.getValueAt(row, column);
         if (maxValue == null) {
           maxValue = value;
         } else if (value != null) {
           maxValue = Math.max(maxValue.doubleValue(), value.doubleValue());
         }
         if (minValue == null) {
           minValue = value;
         } else if (value != null) {
           minValue = Math.min(minValue.doubleValue(), value.doubleValue());
         }
         dots.add(new Dot(value == null ? 0 : value));
       }
 
       lineChart.addDots(dots);
       chart.addElements(lineChart);
     }
 
     if ((maxValue != null) && (minValue != null)) {
       int exponent = Integer.toString(Math.abs(maxValue.intValue())).length() - 1;
 
       YAxis ya = new YAxis();
       int stepSize = (int) (((long) (maxValue.intValue() / Math.pow(10, exponent))) * Math.pow(10, exponent - 1));
       ya.setSteps(stepSize);
 
       ya.setMax((int) (maxValue.doubleValue() - (maxValue.doubleValue() % stepSize)) + stepSize);
       chart.setYAxis(ya);
     }
 
     return chart;
   }
 
   /**
    * @deprecated
    */
   public CSSConstant determineChartType(final ChartDocument chartDocument) {
     final ChartElement[] elements = chartDocument.getRootElement().findChildrenByName(ChartElement.TAG_NAME_SERIES);
     for (final ChartElement element : elements) {
       final CSSValue value = element.getLayoutStyle().getValue(ChartStyleKeys.CHART_TYPE);
       if (value != null) {
         if (value.equals(ChartSeriesType.BAR)) {
           return ChartSeriesType.BAR;
         } else if (value.equals(ChartSeriesType.LINE)) {
           return ChartSeriesType.LINE;
         } else if (value.equals(ChartSeriesType.AREA)) {
           return ChartSeriesType.AREA;
         } else if (value.equals(ChartSeriesType.PIE)) {
           return ChartSeriesType.PIE;
         } else if (value.equals(ChartSeriesType.DIAL)) {
           return ChartSeriesType.DIAL;
         }
       }
     }
     return ChartSeriesType.UNDEFINED;
   }
 
   /**
    * @deprecated
    */
   public Text getText(final ChartDocument chartDocument, String elementName) {
     Text text = null;
     ChartElement[] children = chartDocument.getRootElement().findChildrenByName(elementName); //$NON-NLS-1$
     if (children != null && children.length > 0) {
       String label = children[0].getText().trim();
       if (label.length() > 0) {
         String cssFontStyleString = createCssFontStyleString(children[0]);
         if (cssFontStyleString != null) {
           text = new Text(label, cssFontStyleString);
         } else {
           text = new Text(label);
         }
       }
     }
     return text;
   }
   
   protected Number scaleNumber(Number number, Number scale) {
     Number scaledNumber = number;
     if ((number != null) && (scale != null) && !scale.equals(1) && !scale.equals(0)) {
       
       int startingSignificantDigits = 0;
       if (!(number instanceof Integer)) {
         String fractionalPart = Double.toString(number.doubleValue());
         fractionalPart = fractionalPart.substring(fractionalPart.indexOf(".") + 1);
         if ((fractionalPart.length() > 1) || Integer.parseInt(fractionalPart) > 0) {
           startingSignificantDigits = fractionalPart.length();
         }
       }
       
       int preferredSignificantDigits = Math.max(2, Math.min(startingSignificantDigits, 6));
       
       scaledNumber = number.doubleValue() / scale.doubleValue();
       int scaledSignificantDigits = 0;
       String fractionalPart = Double.toString(scaledNumber.doubleValue());
       fractionalPart = fractionalPart.substring(fractionalPart.indexOf(".") + 1);
       if ((fractionalPart.length() > 1) || Integer.parseInt(fractionalPart) > 0) {
         scaledSignificantDigits = fractionalPart.length();
       }
       
       if (scaledSignificantDigits > preferredSignificantDigits) {
         double multiplier = Math.pow(10, preferredSignificantDigits);
         scaledNumber = Math.round(scaledNumber.doubleValue() * multiplier) / multiplier;
       }
     }  
     return scaledNumber;
   }
 }
