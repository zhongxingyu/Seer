 package eu.straider.web.gwt.gauges.client.components;
 
 import com.google.gwt.animation.client.Animation;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.canvas.dom.client.CssColor;
 import eu.straider.web.gwt.gauges.client.ColorRange;
 
 public class SimpleGauge<T extends Number> extends AbstractGauge<T> {
 
     private double lineWidth;
     private int gaugeDegrees;
     private int startDegrees = 150;
 
     public SimpleGauge(T minValue, T maxValue, T value) {
         this(20, 240, minValue, maxValue, value);
     }
 
     public SimpleGauge(double lineWidth, int gaugeDegrees, T minValue, T maxValue, T value) {
         super();
         setGaugeDegrees(gaugeDegrees);
         this.lineWidth = lineWidth;
         setFont("bold 32px Lucida Console");
         setMinValue(minValue);
         setMaxValue(maxValue);
         setAnimationEnabled(true);
         setValue(value, false);
     }
 
     public final void setGaugeDegrees(int degrees) {
         if (degrees > 1) {
             gaugeDegrees = degrees;
             startDegrees = 270 - (gaugeDegrees / 2);
         }
     }
 
     @Override
     public final void setValue(T value, boolean fireEvents) {
         T oldValue = getValue();
         super.setValue(value, fireEvents);
 
         if (value.doubleValue() > getMaxValue().doubleValue()) {
             value = getMaxValue();
         } else if (value.doubleValue() < getMinValue().doubleValue()) {
             value = getMinValue();
         }
         if(oldValue == null) {
             oldValue = getMinValue();
         }
 
         if (isAnimationEnabled()) {
             SimpleGaugeValueAnimation animation = new SimpleGaugeValueAnimation();
             animation.goToValue(value, oldValue, getAnimationDuration());
         } else {
             drawArc(value.doubleValue());
             drawValueText(value);
         }
     }
 
     void drawArc(double arcValue) {
         int width = getCanvas().getCanvasElement().getWidth();
         int height = getCanvas().getCanvasElement().getHeight();
         getContext().restore();
         getContext().clearRect(0, 0, width, height);
         getContext().setStrokeStyle(getCurrentGaugeColor(arcValue));
         
         getContext().setLineWidth(lineWidth);
         getContext().setLineCap(Context2d.LineCap.ROUND);
        double degrees = ((double) gaugeDegrees / (double) 100) * (getMaxValue().doubleValue() / (double) 100 * arcValue);
 
         int toDegrees = startDegrees + (int) degrees;
         if (toDegrees > 360) {
             toDegrees -= 360;
         }
         getContext().beginPath();
         getContext().closePath();
         getContext().arc(width / 2, height / 2, width / 2 - lineWidth, Math.toRadians(startDegrees), Math.toRadians(toDegrees));
         getContext().stroke();
     }
 
     private CssColor getCurrentGaugeColor(double arcValue) {
         CssColor color = null;
         for(ColorRange range : getColorRanges()) {
             if(arcValue>=range.getMin().doubleValue() && arcValue<=range.getMax().doubleValue()) {
                 color = range.getColor();
             }
         }
         if(color == null) {
             color = getGaugeColor();
         }
         return color;
     }
     
     private void drawValueText(T valueText) {
         getContext().setFont(getFont());
         getContext().setTextAlign(Context2d.TextAlign.CENTER);
         getContext().fillText(getValueFormat().format(valueText), getCanvas().getCanvasElement().getWidth() / 2, getCanvas().getCanvasElement().getHeight() / 2);
     }
 
     class SimpleGaugeValueAnimation extends Animation {
 
         private T oldValue;
         private T step;
 
         void goToValue(T toValue, T oldValue, int milliseconds) {
             this.oldValue = oldValue;
             step = (T) Double.valueOf(toValue.doubleValue() - oldValue.doubleValue());
             run(milliseconds);
         }
 
         @Override
         protected void onUpdate(double progress) {
             double tmp = oldValue.doubleValue() + (step.doubleValue() * progress);
             drawArc(tmp);
             drawValueText((T) new Double(tmp));
         }
     }
     
 }
