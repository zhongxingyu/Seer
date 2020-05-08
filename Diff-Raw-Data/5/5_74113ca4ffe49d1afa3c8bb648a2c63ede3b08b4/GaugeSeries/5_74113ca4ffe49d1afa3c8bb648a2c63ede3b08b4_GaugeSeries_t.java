 package org.icefaces.ace.model.chart;
 
 import org.icefaces.ace.util.JSONBuilder;
 
 import javax.faces.component.UIComponent;
 
 /**
  * The 'series' modeling the value and configuration of the gauge chart type.
  * User: Nils
  * Date: 10/19/12
  * Time: 9:50 AM
  */
 public class GaugeSeries extends ChartSeries {
     public String label;
     public LabelPosition labelPosition;
     public String background;
     public String ringColor;
     public String tickColor;
     public Integer ringWidth;
     public Integer labelHeightAdjust;
     public Integer diameter;
     public Number padding;
     public Number value;
     public Number min;
     public Number max;
     public Number intervalOuterRadius;
     public Number intervalInnerRadius;
     public Integer hubRadius;
     public Integer tickSpacing;
     public Integer tickPadding;
     public Integer needleThickness;
     public Integer needlePad;
     public Number[] ticks;
     public Number[] intervals;
     public String[] intervalColors;
     public Boolean showTicks;
     public Boolean showTickLabels;
     public Boolean pegNeedle;
 
     public GaugeSeries(Number value) {
         this.value = value;
     }
 
     public GaugeSeries() {}
 
     @Override
     public JSONBuilder getDataJSON(UIComponent component) {
         JSONBuilder data = super.getDataJSON(component);
         data.item(value.doubleValue());
         data.endArray();
         return data;
     }
 
     @Override
     public JSONBuilder getConfigJSON(UIComponent component) {
         JSONBuilder cfg = super.getConfigJSON(component);
         cfg.entry("renderer", "ice.ace.jq.jqplot.MeterGaugeRenderer", true);
         cfg.beginMap("rendererOptions");
 
         if (label != null)
             cfg.entry("label", label);
 
         if (labelPosition != null)
             cfg.entry("labelPosition", labelPosition.name().toLowerCase());
 
         if (background != null)
             cfg.entry("background", background);
 
         if (ringColor != null)
             cfg.entry("ringColor", ringColor);
 
         if (tickColor != null)
             cfg.entry("tickColor", tickColor);
 
         if (ringWidth != null)
             cfg.entry("ringWidth", ringWidth.doubleValue());
 
         if (labelHeightAdjust != null)
             cfg.entry("labelHeightAdjust", labelHeightAdjust.doubleValue());
 
         if (diameter != null)
             cfg.entry("diameter", diameter.doubleValue());
 
         if (padding != null)
             cfg.entry("padding", padding.doubleValue());
 
         if (min != null)
             cfg.entry("min", min.doubleValue());
 
         if (max != null)
             cfg.entry("max", max.doubleValue());
 
         if (intervalOuterRadius != null)
             cfg.entry("intervalOuterRadius", intervalOuterRadius.doubleValue());
 
         if (intervalInnerRadius != null)
             cfg.entry("intervalInnerRadius", intervalInnerRadius.doubleValue());
 
         if (hubRadius != null)
             cfg.entry("hubRadius", hubRadius.doubleValue());
 
         if (tickSpacing != null)
             cfg.entry("tickSpacing", tickSpacing.doubleValue());
 
         if (needleThickness != null)
             cfg.entry("needleThickness", needleThickness.doubleValue());
 
         if (needlePad != null)
             cfg.entry("needlePad", needlePad.doubleValue());
 
         if (ticks != null) {
             cfg.beginArray("ticks");
             for (Number tick : ticks)
                 cfg.item(tick.doubleValue());
             cfg.endArray();
         }
 
         if (intervals != null) {
             cfg.beginArray("intervals");
             for (Number interval : intervals)
                 cfg.item(interval.doubleValue());
             cfg.endArray();
         }
 
         if (intervalColors != null) {
             cfg.beginArray("intervalColors");
             for (String color : intervalColors)
                 cfg.item(color);
             cfg.endArray();
         }
 
         if (showTicks != null)
             cfg.entry("showTicks", showTicks);
 
         if (showTickLabels != null)
             cfg.entry("showTickLabels", showTickLabels);
 
         if (pegNeedle != null)
             cfg.entry("pegNeedle", pegNeedle);
 
         cfg.endMap();
         cfg.endMap();
         return cfg;
     }
 
     /**
      * @return the value of displayed on the gauge
      */
     public Number getValue() {
         return value;
     }
 
     /**
      * @param value the value of displayed on the gauge
      */
     public void setValue(Number value) {
         this.value = value;
     }
 
     /**
      * @return the label displayed on the gauge
      */
     public String getLabel() {
         return label;
     }
 
     /**
      * @param label String the label displayed on the gauge
      */
     public void setLabel(String label) {
         this.label = label;
     }
 
     /**
     * @return does gauge stop needle just below/above the min/max values if data is below/above min/max, as if the meter is 'pegged'.
      */
     public Boolean getPegNeedle() {
         return pegNeedle;
     }
 
     /**
     * @param pegNeedle does gauge stop needle just below/above the min/max values if data is below/above min/max, as if the meter is 'pegged'.
      */
     public void setPegNeedle(Boolean pegNeedle) {
         this.pegNeedle = pegNeedle;
     }
 
     /**
      * @return does gauge show labels on ticks around the gauge
      */
     public Boolean getShowTickLabels() {
         return showTickLabels;
     }
 
     /**
      * @param showTickLabels does gauge show labels on ticks around the gauge
      */
     public void setShowTickLabels(Boolean showTickLabels) {
         this.showTickLabels = showTickLabels;
     }
 
     /**
      * @return does gauge show ticks around the gauge
      */
     public Boolean getShowTicks() {
         return showTicks;
     }
 
     /**
      * @param showTicks does gauge show ticks around the gauge
      */
     public void setShowTicks(Boolean showTicks) {
         this.showTicks = showTicks;
     }
 
     /**
      * @return array of CSS color definitions used for the intervals of the gauge
      */
     public String[] getIntervalColors() {
         return intervalColors;
     }
 
     /**
      * @param intervalColors array of CSS color definitions used for the intervals of the gauge
      */
     public void setIntervalColors(String[] intervalColors) {
         this.intervalColors = intervalColors;
     }
 
     /**
      * @return array of Numbers delineating the intervals of the gauge
      */
     public Number[] getIntervals() {
         return intervals;
     }
 
     /**
      * @param intervals array of Numbers delineating the intervals of the gauge
      */
     public void setIntervals(Number[] intervals) {
         this.intervals = intervals;
     }
 
     /**
      * @return array of number around the gauge at which to render ticks. Auto computed on client by default.
      */
     public Number[] getTicks() {
         return ticks;
     }
 
     /**
      * @param ticks array of number around the gauge at which to render ticks. Auto computed on client by default.
      */
     public void setTicks(Number[] ticks) {
         this.ticks = ticks;
     }
 
     /**
      * @return pixels of padding between the needle and the inner edge of the ring when the needle is at the min or max of the gauge.
      */
     public Integer getNeedlePad() {
         return needlePad;
     }
 
     /**
      * @param needlePad pixels of padding between the needle and the inner edge of the ring when the needle is at the min or max of the gauge.
      */
     public void setNeedlePad(Integer needlePad) {
         this.needlePad = needlePad;
     }
 
     /**
      * @return pixels of thickness at the widest point of the needle
      */
     public Integer getNeedleThickness() {
         return needleThickness;
     }
 
     /**
      * @param needleThickness pixels of thickness at the widest point of the needle
      */
     public void setNeedleThickness(Integer needleThickness) {
         this.needleThickness = needleThickness;
     }
 
     /**
      * @return pixels of padding of the tick marks to the outer ring and the tick labels to marks
      */
     public Integer getTickPadding() {
         return tickPadding;
     }
 
     /**
      * @param tickPadding pixels of padding of the tick marks to the outer ring and the tick labels to marks
      */
     public void setTickPadding(Integer tickPadding) {
         this.tickPadding = tickPadding;
     }
 
     /**
      * @return degrees between ticks (if not using explicit ticks)
      */
     public Integer getTickSpacing() {
         return tickSpacing;
     }
 
     /**
      * @param tickSpacing degrees between ticks (if not using explicit ticks)
      */
     public void setTickSpacing(Integer tickSpacing) {
         this.tickSpacing = tickSpacing;
     }
 
     /**
      * @return pixel radius of the hub at the bottom center of gauge which the needle attaches to. Auto computed on the client by default.
      */
     public Integer getHubRadius() {
         return hubRadius;
     }
 
     /**
      * @param hubRadius pixel radius of the hub at the bottom center of gauge which the needle attaches to. Auto computed on the client by default.
      */
     public void setHubRadius(Integer hubRadius) {
         this.hubRadius = hubRadius;
     }
 
     /**
      * @return pixel radius of the inner circle of the interval ring. Auto computed on the client by default.
      */
     public Number getIntervalInnerRadius() {
         return intervalInnerRadius;
     }
 
     /**
      * @param intervalInnerRadius pixel radius of the inner circle of the interval ring. Auto computed on the client by default.
      */
     public void setIntervalInnerRadius(Number intervalInnerRadius) {
         this.intervalInnerRadius = intervalInnerRadius;
     }
 
     /**
      * @return pixel radius of the outer circle of the interval ring. Auto computed on the client by default.
      */
     public Number getIntervalOuterRadius() {
         return intervalOuterRadius;
     }
 
     /**
      * @param intervalOuterRadius pixel radius of the outer circle of the interval ring. Auto computed on the client by default.
      */
     public void setIntervalOuterRadius(Number intervalOuterRadius) {
         this.intervalOuterRadius = intervalOuterRadius;
     }
 
     /**
      * @return maximum value on the gauge.
      */
     public Number getMax() {
         return max;
     }
 
     /**
      * @param max maximum value on the gauge.
      */
     public void setMax(Number max) {
         this.max = max;
     }
 
     /**
      * @return minimum value on the gauge.
      */
     public Number getMin() {
         return min;
     }
 
     /**
      * @param min minimum value on the gauge.
      */
     public void setMin(Number min) {
         this.min = min;
     }
 
     /**
      * @return pixels of padding between the meterGauge and plot edges, auto calculated on the client by default.
      */
     public Number getPadding() {
         return padding;
     }
 
     /**
      * @param padding pixels of padding between the meterGauge and plot edges, auto calculated on the client by default.
      */
     public void setPadding(Number padding) {
         this.padding = padding;
     }
 
     /**
      * @return outer diameter of the gauge in pixels, auto calculated on the client by default.
      */
     public Integer getDiameter() {
         return diameter;
     }
 
     /**
      * @param diameter outer diameter of the gauge in pixels, auto calculated on the client by default.
      */
     public void setDiameter(Integer diameter) {
         this.diameter = diameter;
     }
 
     /**
      * @return pixels of offset of the label up (-) or down (+) from its default position.
      */
     public Integer getLabelHeightAdjust() {
         return labelHeightAdjust;
     }
 
     /**
      * @param labelHeightAdjust pixels of offset of the label up (-) or down (+) from its default position.
      */
     public void setLabelHeightAdjust(Integer labelHeightAdjust) {
         this.labelHeightAdjust = labelHeightAdjust;
     }
 
     /**
      * @return pixel width of the ring around the guage. Auto computed by default.
      */
     public Integer getRingWidth() {
         return ringWidth;
     }
 
     /**
      * @param ringWidth pixel width of the ring around the guage. Auto computed by default.
      */
     public void setRingWidth(Integer ringWidth) {
         this.ringWidth = ringWidth;
     }
 
     /**
      * @return CSS color definition of the tick marks around the gauge
      */
     public String getTickColor() {
         return tickColor;
     }
 
     /**
      * @param tickColor CSS color definition of the tick marks around the gauge
      */
     public void setTickColor(String tickColor) {
         this.tickColor = tickColor;
     }
 
     /**
      * @return CSS color definition of the ring around the gauge and the needle
      */
     public String getRingColor() {
         return ringColor;
     }
 
     /**
      * @param ringColor CSS color definition of the ring around the gauge and the needle
      */
     public void setRingColor(String ringColor) {
         this.ringColor = ringColor;
     }
 
     /**
      * @return CSS color definition of the background inside the gauge
      */
     public String getBackground() {
         return background;
     }
 
     /**
      * @param background CSS color definition of the background inside the gauge
      */
     public void setBackground(String background) {
         this.background = background;
     }
 
     /**
      * @return where to position the label- either 'inside' or 'bottom'
      */
     public LabelPosition getLabelPosition() {
         return labelPosition;
     }
 
     /**
      * @param labelPosition where to position the label- either 'inside' or 'bottom'
      */
     public void setLabelPosition(LabelPosition labelPosition) {
         this.labelPosition = labelPosition;
     }
 
     public static enum GaugeType implements ChartType {
         GAUGE
     }
 
     public ChartType getDefaultType() {
         return GaugeType.GAUGE;
     }
 
     private enum LabelPosition {
         INSIDE, BOTTOM
     }
 }
