 package org.jfree.chart.editor;
 
 import org.jfree.chart.editor.themes.*;
 import org.jfree.chart.editor.components.*;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.Plot;
 import org.jfree.chart.axis.Axis;
 
 import java.awt.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Dan
  * Date: 17-Dec-2009
  * Time: 16:25:21
  * Supplies GUI components for chart editors. The static Controller class can be used to obtain different implementations
  * of this interface, should uses of this library require extended functionality.
  */
 public interface ChartEditorComponentFactory {
 
     static final String DEFAULT_IMPLEMENTATION = "org.jfree.chart.editor.ChartEditorComponentFactoryImpl";
 
     DefaultChartEditor createChartEditor(ExtendedChartTheme theme, JFreeChart chart, boolean immediateUpdate);
 
     /**
      * A static method that returns a panel that is appropriate for the axis
      * type.
      *
      * @param theme The axis theme that will be edited
      * @param axis  the axis whose properties are to be displayed/edited in
      *              the panel.
      * @param chart The chart the axis belongs to.
      * @param immediateUpdate Whether changes to GUI controls should immediately alter the chart
      *
      * @return A panel or <code>null</code< if axis is <code>null</code>.
      */
     DefaultAxisEditor createAxisEditor(AxisTheme theme, JFreeChart chart, Axis axis, boolean immediateUpdate);
 
     DefaultPlotEditor createPlotEditor(PlotTheme theme, JFreeChart chart, Plot plot, boolean immediateUpdate);
 
     DefaultLegendEditor createLegendEditor(LegendTheme theme, JFreeChart chart, boolean immediateUpdate);
 
     DefaultChartTitleEditor createChartTitleEditor(ChartTitleTheme theme, JFreeChart chart, boolean immediateUpdate);
 
     ItemLabelFormatPanel createItemLabelFormatPanel(LegendTheme theme, Plot p);
 
     ItemLabelFormatPanel createItemLabelFormatPanel(PlotTheme theme, Plot p );
 
     BorderPanel createBorderPanel(String title, boolean visible, BasicStroke stroke, Paint paint);
 
     BackgroundEditingPanel createBackgroundEditingPanel(ExtendedChartTheme theme);
 
     BackgroundEditingPanel createBackgroundEditingPanel(PlotTheme theme);
 
     PaintControl createPaintControl(Paint p);
 
     PaintControl createPaintControl(Paint p, boolean allowNulls);
 
     StrokeControl createStrokeControl(BasicStroke s);
 
     FontControl createFontControl(Font f);
 
     NumberFormatDisplay createNumberFormatDisplay(String formatString);
 
     static class Controller {
         private static String implementationClass = DEFAULT_IMPLEMENTATION;
 
         private static ChartEditorComponentFactory instance = null;
 
         public synchronized static void setImplementationClass(String s)
                 throws ClassNotFoundException, IllegalAccessException, InstantiationException {
             // make sure this doesn't throw any nasties.
             ChartEditorComponentFactory newInstance = getInstanceInternal(s);
             // reaching this point means the given class was instantiated with the right interface.
             implementationClass = s;
             instance = newInstance;
         }
 
         public synchronized static ChartEditorComponentFactory getInstance() {
             try {
                 if(instance == null) {
                     instance = getInstanceInternal(implementationClass);
                 }
                 return instance;
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             } catch (InstantiationException e) {
                 e.printStackTrace();
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             }
             return null;
         }
 
         private synchronized static ChartEditorComponentFactory getInstanceInternal(String s)
                 throws ClassNotFoundException, InstantiationException, IllegalAccessException {
             Class c = Class.forName(s);
 
             return (ChartEditorComponentFactory) c.newInstance();
         }
     }
 }
