 package org.pentaho.chart.plugin.jfreechart.chart;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Paint;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.AxisLocation;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.labels.ItemLabelAnchor;
 import org.jfree.chart.labels.ItemLabelPosition;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.renderer.category.AreaRenderer;
 import org.jfree.chart.renderer.category.BarRenderer;
 import org.jfree.chart.renderer.category.CategoryItemRenderer;
 import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
 import org.jfree.chart.renderer.category.LayeredBarRenderer;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.jfree.ui.StandardGradientPaintTransformer;
 import org.jfree.ui.TextAnchor;
 import org.pentaho.chart.ChartDocumentContext;
 import org.pentaho.chart.core.AxisSeriesLinkInfo;
 import org.pentaho.chart.core.ChartDocument;
 import org.pentaho.chart.core.ChartElement;
 import org.pentaho.chart.core.ChartSeriesDataLinkInfo;
 import org.pentaho.chart.css.keys.ChartStyleKeys;
 import org.pentaho.chart.css.styles.ChartAxisLocationType;
 import org.pentaho.chart.css.styles.ChartOrientationStyle;
 import org.pentaho.chart.data.ChartTableModel;
 import org.pentaho.chart.plugin.api.ChartItemLabelGenerator;
 import org.pentaho.chart.plugin.jfreechart.dataset.DatasetGeneratorFactory;
 import org.pentaho.chart.plugin.jfreechart.utils.CylinderRenderer;
 import org.pentaho.chart.plugin.jfreechart.utils.JFreeChartUtils;
 import org.pentaho.reporting.libraries.css.dom.LayoutStyle;
 import org.pentaho.reporting.libraries.css.values.CSSValue;
 import org.pentaho.reporting.libraries.css.keys.color.ColorStyleKeys;
 
 /**
  * Top Level class that is extended by different chart type creators.
  *
 * <pre>
  *                            JFreeChartGenerator
  *                                   |
  *                     -----------------------------------------------------------------------------
  *                     |                                                            |               |
  *                JFreeBarChartGenerator                                           ....            ...
  *                     |
  *       -------------------------------------------------------
  *       |                 |                |                   |
  *   JFreeStackedGen   JFreeLayeredGen    JFreeCylinderGen   JFreeDefault
  *       |
  * ---------------------------
  * |                         |
  * JFreeStackedPercent    JFreeStacked100Percent
 * </pre>
  *
  * Author: Ravi Hasija
  * Date: May 14, 2008
  * Time: 4:24:46 PM
  * To change this template use File | Settings | File Templates.
  */
 public abstract class JFreeChartGenerator implements IJFreeChartGenerator {
 
   protected final DatasetGeneratorFactory datasetGeneratorFactory = new DatasetGeneratorFactory();
 
   /**
    * Gets the title of the chart defined in the chartDocument
    * </p>
    * @param chartDocument - ChartDocument that defines what the series should look like
    * @return String - the title
    */
   public static String getTitle(final ChartDocument chartDocument) {
     final ChartElement[] children = chartDocument.getRootElement().findChildrenByName("title"); //$NON-NLS-1$
     if (children != null && children.length > 0) {
       return children[0].getText();
     }
     return null;
   }
 
   /**
    * Returns a boolean value that indicates if the chart should generate tooltips
    * </p>
    * @param chartDocument - ChartDocument that defines what the series should look like
    * @return true if we want to show tool tips
    */
   public static boolean getShowToolTips(final ChartDocument chartDocument) {
     // TODO determine this from the chartDocument
     return true;
   }
 
   /**
    * Returns the ValueCategoryLabel of the chart.
    * </p>
    * @param chartDocument - ChartDocument that defines what the series should look like
    * @return String - the value category label
    */
   public static String getValueCategoryLabel(final ChartDocument chartDocument) {
     final ChartElement[] children = chartDocument.getRootElement().findChildrenByName("domainLabel"); //$NON-NLS-1$
     if (children != null && children.length > 0) {
       return children[0].getText();
     }
     return null;
   }
 
   /**
    * Returns the ValueAxisLabel of the chart.
    *
    * @param chartDocument - ChartDocument that defines what the series should look like
    * @return String - the value axis label
    */
   public static String getValueAxisLabel(final ChartDocument chartDocument) {
     final ChartElement[] children = chartDocument.getRootElement().findChildrenByName("rangeLabel"); //$NON-NLS-1$
     if (children != null && children.length > 0) {
       return children[0].getText();
     }
     return null;
   }
 
   /**
    * @param chartDocument - ChartDocument that defines what the series should look like
    * @return a boolean that indicates of if a legend should be included in the chart
    */
   public static boolean getShowLegend(final ChartDocument chartDocument) {
     // TODO determine this from the chartDocument
     return true;
   }
 
   /**
    * Returns the plot orientation (horizontal or vertical) for the current chart.
    * </p> 
    * @param chartDocument that contains a orientation on the Plot element
    * @return PlotOrientation.VERTICAL or .HORIZONTAL or Null if not defined.
    */
   public PlotOrientation getPlotOrientation(final ChartDocument chartDocument) {
     PlotOrientation plotOrient = null;
     final ChartElement plotElement = chartDocument.getPlotElement();
 
     if (plotElement != null) {
       final LayoutStyle layoutStyle = plotElement.getLayoutStyle();
       final CSSValue value = layoutStyle.getValue(ChartStyleKeys.ORIENTATION);
 
       if (ChartOrientationStyle.VERTICAL.equals(value)) {
         plotOrient = PlotOrientation.VERTICAL;
       } else if (ChartOrientationStyle.HORIZONTAL.equals(value)) {
         plotOrient = PlotOrientation.HORIZONTAL;
       }
     }
     return plotOrient;
   }
 
   /**
    * Returns custom dataset based on certain column positions. The column positions are retrieved by iterating
    * over series elements looking for a specific/given axis id.
    * </p>
    * @param chartDocContext Chart document context object for the current chart document.
    * @param axisElement Current axis element.
    * @param axisSeriesLinkInfo Holds information that links the axis id to series element(s).
    * @return DefaultCategoryDataset that has information from specific column positions.
   */
  public Integer[] getColumPositions(final ChartDocumentContext chartDocContext,
                                     final ChartElement axisElement,
                                     final AxisSeriesLinkInfo axisSeriesLinkInfo) {
    /*
     * First we get the column pos information for each range axis from the columns array list.
     * And then we create the default category dataset based on the columns positions retrieved above.
     */
    // Get current axis element's axis id.
    final Object axisID = axisElement.getAttribute("id");//$NON-NLS-1$
    // Get the column positions for current axis element by looking into each series for given axis id.
    Integer[] columnPosArr = null;
 
    if (axisSeriesLinkInfo != null && axisID != null) {
      final ChartSeriesDataLinkInfo seriesDataLinkInfo = chartDocContext.getDataLinkInfo();
 
      if (seriesDataLinkInfo != null) {
        final ArrayList<ChartElement> seriesElementsList = axisSeriesLinkInfo.getSeriesElements(axisID);
 
          if (seriesElementsList != null) {
            final int size = seriesElementsList.size();
            final ArrayList<Integer> columnPosList = new ArrayList<Integer>();
            for (int i=0; i<size; i++) {
              final ChartElement seriesElement = seriesElementsList.get(i);
              final Integer columnPos = seriesDataLinkInfo.getColumnNum(seriesElement);
              columnPosList.add(columnPos);
            }
            final int listLength = columnPosList.size();
            columnPosArr = new Integer[listLength];
            System.arraycopy(columnPosList.toArray(),0, columnPosArr, 0, listLength);
            Arrays.sort(columnPosArr);
          }
        }
      }
     return columnPosArr;
   }
 
   /**
    * Sets the current range axis location based on axis location style key (specified in the chart document).
    *
    * NOTE: Plot's range axis location is updated
    *
    * @param plot        Plot for the current chart
    * @param axisElement Current axis element being proccessed
    * @param axisCounter Set the location and tie it to a index.
    */
   private void setRangeAxisLocation (final CategoryPlot plot,
                                      final ChartElement axisElement,
                                      final int axisCounter) {
     final CSSValue cssValue = axisElement.getLayoutStyle().getValue(ChartStyleKeys.AXIS_LOCATION);
     final String side = cssValue.getCSSText();
     if (side != null && (cssValue.equals(ChartAxisLocationType.PRIMARY))){
       plot.setRangeAxisLocation(axisCounter, AxisLocation.BOTTOM_OR_LEFT);
     } else {
       plot.setRangeAxisLocation(axisCounter, AxisLocation.TOP_OR_RIGHT);
     }
   }
 
   /**
    * Sets the axis label and tick label color.
    * </p>
    * @param axisElement Current axis element from the chart document
    * @param valueAxis   Current value axis. NOTE: This object will be updated in this method.
    * @param labelType   Tag name label or tag name tick label.
    */
   private void setAxisColor(final ChartElement axisElement,
                             final ValueAxis valueAxis,
                             final String labelType) {
     final ChartElement [] labelElements = axisElement.findChildrenByName(labelType);
     if (labelElements != null && labelElements.length > 0) {
       final CSSValue colorCSSValue = labelElements[0].getLayoutStyle().getValue(ColorStyleKeys.COLOR);
       final Color axisLabelColor = JFreeChartUtils.getColorFromCSSValue(colorCSSValue);
       if (axisLabelColor != null) {
         if (ChartElement.TAG_NAME_LABEL.equalsIgnoreCase(labelType)) {
           valueAxis.setLabelPaint(axisLabelColor);
         } else if (ChartElement.TAG_NAME_TICK_LABEL.equalsIgnoreCase(labelType)) {
           valueAxis.setTickLabelPaint(axisLabelColor);
         }
       }
     }
   }
 
   /**
    * Create range axis for the current chart and update the chart object with it.
    * </p>
    * @param chartDocContext  Chart documument context for the current chart.
    * @param data             Data for the current chart.
    * @param chart            The chart object to be updated with range axis info.
    */
   public void createRangeAxis(final ChartDocumentContext chartDocContext,
                               final ChartTableModel data,
                               final JFreeChart chart) {
     /*
      * Assumption:
      * #1. User has to provide axis type for each axis (range/domain)
      * #2. User have to specify the axis id for each series
      * The code for handling multiple axis goes here.
      * 1. Create multiple datasets only if there are more than one range axis.
      * 2. Update certain axis attributes on the given plot.
      */
     final ChartDocument chartDocument = chartDocContext.getChartDocument();
     final AxisSeriesLinkInfo axisSeriesLinkInfo = chartDocument.getAxisSeriesLinkInfo();
     final ArrayList<ChartElement> rangeAxisArrayList = axisSeriesLinkInfo.getRangeAxisElements();
     final int rangeAxisCount = rangeAxisArrayList.size();
 
     if (chart != null && rangeAxisCount > 0) {
       final CategoryPlot plot = (CategoryPlot)chart.getPlot();
 
       for (int i=0; i<rangeAxisCount; i++) {
         final ChartElement axisElement = rangeAxisArrayList.get(i);
         // If there is only one range axis then we do not need to create a new jfreeDataset (that uses certain column data)
         // Instead we just need to update certain attributes like label text, tick label color etc.
         if (rangeAxisCount > 1) {
           // Create new jfreeDataset since there are more than one range axis and get the data corresponding to
           // certain columns
           final Integer[] columnPosArr = getColumPositions(chartDocContext,axisElement, axisSeriesLinkInfo);
           final DefaultCategoryDataset currDataset = datasetGeneratorFactory.createDefaultCategoryDataset(chartDocContext, data, columnPosArr);
           plot.setDataset(i, currDataset);
           plot.mapDatasetToRangeAxis(i, i);
         }
 
         final ValueAxis valueAxis = createRangeAxis(axisElement);
         if(valueAxis != null) {
           plot.setRangeAxis(i, valueAxis);
           setAxisColor(axisElement, valueAxis, ChartElement.TAG_NAME_LABEL);
           setAxisColor(axisElement, valueAxis, ChartElement.TAG_NAME_TICK_LABEL);
           setRangeAxisLocation(plot, axisElement, i);
           setRenderer(plot, i);
         }
       }
     }
   }
 
   /**
    * Creates a Range Axis
    * @param axisElement -- Current axis element
    * @return Returns the new range axis.
    */
   private ValueAxis createRangeAxis(final ChartElement axisElement) {
     final String axisLabel = (String)axisElement.getAttribute("label");//$NON-NLS-1$
     final ValueAxis valueAxis;
     if (axisLabel != null) {
       valueAxis = new NumberAxis(axisLabel);
     } else {
       valueAxis = new NumberAxis();
     }
     return valueAxis;
   }
 
   /**
    * Set the renderer for the chart.
    * </p>
    * @param plot   Plot element from the current chart document.
    * @param index  Set the renderer for the index.
    */
   //TODO: Look into this method for remaining implementations.
   private void setRenderer(final CategoryPlot plot,
                            final int index ) {
     if (plot.getRenderer() instanceof GroupedStackedBarRenderer) {
     } else if (plot.getRenderer() instanceof CylinderRenderer) {
     } else if (plot.getRenderer() instanceof LayeredBarRenderer) {
     } else if (plot.getRenderer() instanceof BarRenderer) {
       final BarRenderer barRenderer = new BarRenderer();
       plot.setRenderer(index, barRenderer);
     } else if (plot.getRenderer() instanceof AreaRenderer) {
       final AreaRenderer areaRenderer = new AreaRenderer();
       plot.setRenderer(index, areaRenderer);
     }
   }
 
   /**
    * Sets the paint(gradient color) on all the series listed by the
    * chartDocument.
    *
    * @param categoryPlot   - the active plot
    * @param seriesElements - Array of series elements that contains series tags
    * @param data           - The actual chart data
    */
   protected void setSeriesPaint(final CategoryPlot categoryPlot,
                                 final ChartElement[] seriesElements,
                                 final ChartTableModel data) {
     StandardGradientPaintTransformer st = null;
 
     final int length = seriesElements.length;
     for (int i = 0; i < length; i++) {
       final ChartElement seriesElement = seriesElements[i];
       final Paint paint = getPaintFromSeries(seriesElement);
       if (paint != null) {
         final int column = JFreeChartUtils.getSeriesColumn(seriesElement, data, i);
         final int datasetCount = categoryPlot.getDatasetCount();
         for (int datasetCounter=0; datasetCounter<datasetCount; datasetCounter++) {
           categoryPlot.getRenderer(datasetCounter).setSeriesPaint(column, paint);
         }
         /*
         * JFreeChart engine cannot currently implement more than one gradient type except
         * when it's none. That is we cannot have one gradient as CENTER_VERTICAL and another
         * one as CENTER_HORIZONTAL.
         *
         * For example:
         *    series1: none
         *    series2: VERTICAL
         *    series3: none
         *    series4: HORIZONTAL
         * JfreeChart can render none as none, but can implement only one of the gradient
         * styles defined for bar2 and bar4. In our implementation we are accepting the first
         * not-none gradient type and then rendering bar2 and bar4 with VERTICAL.
         *
         *  Check for specific renderer instances since only certain specific renderers allow
         *  setting gradient paint transform.
         */
         if (st == null) {
           st = JFreeChartUtils.getStandardGradientPaintTrans(seriesElements[i]);
           final float barWidthPercent = JFreeChartUtils.getMaximumBarWidth(seriesElements[i]);
           for (int datasetCounter = 0; datasetCounter < datasetCount; datasetCounter++) {
             final CategoryItemRenderer itemRenderer = categoryPlot.getRenderer(datasetCounter);
 
             // If the renderer is of type BarRenderer then set the gradient paint transform
             // and also set the maximum bar width
             if (itemRenderer instanceof BarRenderer) {
               final BarRenderer barRender = (BarRenderer) categoryPlot.getRenderer(datasetCounter);
               barRender.setGradientPaintTransformer(st);
 
               if (barWidthPercent > 0) {
                 barRender.setMaximumBarWidth(barWidthPercent);
               }
             }
           }
         }
       }
     }
   } //setSeriesPaint() ends here
 
   /**
    * Sets the series item label(s) defined in the chartDocument
    *
    * @param categoryPlot   - Plot for the current chart
    * @param seriesElements - Array of Series elements
    * @param data           - the data
    */
   protected void setSeriesItemLabel(final CategoryPlot categoryPlot,
                                     final ChartElement[] seriesElements,
                                     final ChartTableModel data) {
     final int numOfDatasets = categoryPlot.getDatasetCount();
     for (int datasetCounter =0 ; datasetCounter < numOfDatasets; datasetCounter++) {
       categoryPlot.getRenderer(datasetCounter).setBaseItemLabelGenerator(new ChartItemLabelGenerator(seriesElements, data));
 
       final int numOfSeriesElements = seriesElements.length;
       for (int seriesCounter = 0; seriesCounter < numOfSeriesElements; seriesCounter++) {
       // Get and set font information only if the item label's visibility is set to true
         if (JFreeChartUtils.showItemLabel(seriesElements[seriesCounter])) {
           final Font font = JFreeChartUtils.getFont(seriesElements[seriesCounter]);
           final CategoryItemRenderer categoryItemRenderer = categoryPlot.getRenderer(datasetCounter);
           if (categoryItemRenderer instanceof BarRenderer) {
             final BarRenderer barRender = (BarRenderer) categoryItemRenderer;
             barRender.setSeriesItemLabelFont(seriesCounter, font, true);
             barRender.setSeriesItemLabelsVisible(seriesCounter, Boolean.TRUE, true);
           } else if (categoryItemRenderer instanceof AreaRenderer) {
             final AreaRenderer areaRender = (AreaRenderer) categoryItemRenderer;
             areaRender.setSeriesItemLabelFont(seriesCounter, font, true);
             areaRender.setSeriesPositiveItemLabelPosition(seriesCounter, new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.TOP_CENTER));
             areaRender.setSeriesNegativeItemLabelPosition(seriesCounter, new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.BOTTOM_CENTER));
             areaRender.setSeriesItemLabelsVisible(seriesCounter, Boolean.TRUE, true);
           }
         }
       }
     }
   }
 
   /**
    * This method checks to see if there is a gradient type other than "none" set on seriesElement.
    * If the series element has a gradient set on it then it returns that.  Otherwise if a color
    * is defined then that is returned.  If no color or gradient is set then this method returns
    * a null
    *
    * @param seriesElement - Current series element
    * @return a Paint object defined by the seriesElement
    */
   public Paint getPaintFromSeries(final ChartElement seriesElement) {
     CSSValue cssValue = seriesElement.getLayoutStyle().getValue(ChartStyleKeys.GRADIENT_TYPE);
     final String gradientType = cssValue != null ? cssValue.getCSSText() : null;
     final Paint paint;
     if (gradientType != null && !gradientType.equalsIgnoreCase("none")) { //$NON-NLS-1$
       paint = JFreeChartUtils.getGradientPaint(seriesElement);
     } else {
       paint = (Paint) seriesElement.getLayoutStyle().getValue(ColorStyleKeys.COLOR);
     }
     return paint;
   }
 }
