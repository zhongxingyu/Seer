 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package heartsim.gui.component;
 
 import heartsim.CellularAutomaton;
 import heartsim.SimulatorListener;
 import java.util.ArrayList;
 import java.util.List;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 /**
  *
  * @author Lee Boynton
  */
 public class ActionPotentialChart extends ChartPanel implements SimulatorListener
 {
     private final XYSeriesCollection chartData = new XYSeriesCollection();
     private final JFreeChart chart;
     private int visibleTimeSteps = 2000;
     private int min;
     private int max;
     private List<TissueSeries> tissues = new ArrayList<TissueSeries>();
     private List<TissueSeriesListener> listeners = new ArrayList<TissueSeriesListener>();
     private int seriesNumber = 0;
 
     public ActionPotentialChart()
     {
         // set the chart later
         super(null);
 
         chart = ChartFactory.createXYLineChart(
                 "Action Potential", // chart title
                 "Time", // x axis label
                 "Potential", // y axis label
                 chartData, // data
                 PlotOrientation.VERTICAL,
                 true, // include legend
                 true, // tooltips
                 false // urls
                 );
 
         reset();
 
         chart.setBackgroundPaint(null);
 
         this.setChart(chart);
     }
 
     public void addListener(TissueSeriesListener listener)
     {
         listeners.add(listener);
     }
 
     public void removeListener(TissueSeriesListener listener)
     {
         listeners.remove(listener);
     }
 
     public void fireTissueSeriesAdded(TissueSeries tissue)
     {
         for (TissueSeriesListener l : listeners)
         {
             l.tissueAdded(tissue);
         }
     }
 
     public void fireTissueSeriesRemoved(TissueSeries tissue)
     {
         for (TissueSeriesListener l : listeners)
         {
             l.tissueRemoved(tissue);
         }
     }
 
     public void addTissue(int row, int col, String name)
     {
         TissueSeries tissue = new TissueSeries(row, col, name);
 
         if (!tissues.contains(tissue))
         {
             tissues.add(tissue);
 
             chartData.addSeries(tissue.getVoltage());
             chartData.addSeries(tissue.getRecovery());
 
             tissue.setVoltageSeries(seriesNumber);
             tissue.setVoltageColor(((XYLineAndShapeRenderer) chart.getXYPlot().getRenderer()).getSeriesPaint(seriesNumber++));
             
 
             tissue.setRecoverySeries(seriesNumber);
             tissue.setRecoveryColor(((XYLineAndShapeRenderer) chart.getXYPlot().getRenderer()).getSeriesPaint(seriesNumber++));
             
 
             fireTissueSeriesAdded(tissue);
         }
     }
 
     public void removeTissue(TissueSeries tissue)
     {
         if (tissues.contains(tissue))
         {
             tissues.remove(tissue);
             fireTissueSeriesRemoved(tissue);
         }
     }
 
     public List<TissueSeries> getTissues()
     {
         return tissues;
     }
 
     public void setRange(int min, int max)
     {
         this.min = min;
         this.max = max + 10;
 
         setRangeRange();
     }
 
     public void setRangeRange()
     {
         ((XYPlot) chart.getPlot()).getRangeAxis().setRange(this.min, this.max);
     }
 
     public void setDomainRange()
     {
         ((XYPlot) chart.getPlot()).getDomainAxis().setRange(0, visibleTimeSteps);
     }
 
     public void setRanges()
     {
         setRangeRange();
         setDomainRange();
     }
 
     public void setRecoveryEnabled(boolean enabled)
     {
         for (TissueSeries s : tissues)
         {
             chart.getXYPlot().getRenderer().setSeriesVisible(s.getRecoverySeries(), enabled);
         }
     }
 
     public void setVoltageEnabled(boolean enabled)
     {
         for (TissueSeries s : tissues)
         {
             chart.getXYPlot().getRenderer().setSeriesVisible(s.getVoltageSeries(), enabled);
         }
     }
 
     public void reset()
     {
         setRanges();
 
         for (Object obj : chartData.getSeries())
         {
             if (obj instanceof XYSeries)
             {
                 ((XYSeries) obj).clear();
             }
         }
     }
 
     public void simulationStarted()
     {
     }
 
     public void simulationPaused()
     {
     }
 
     public void simulationCompleted()
     {
     }
 
     public void simulationStopped()
     {
     }
 
     public void simulationUpdated(int time, CellularAutomaton ca)
     {
         for (TissueSeries s : tissues)
         {
             s.getRecovery().add(time, ca.getV(s.getRow(), s.getCol()));
             s.getVoltage().add(time, ca.getU(s.getRow(), s.getCol()));
         }
     }
 }
