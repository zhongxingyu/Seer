 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.hpi_web.cloudSim.monitor;
 
 import de.hpi_web.cloudSim.model.StringConstants;
 import info.monitorenter.gui.chart.ITrace2D;
 import info.monitorenter.gui.chart.traces.Trace2DLtd;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.JPanel;
 
 /**
  *
  * @author christoph
  */
 public class ChartPanel {
     public final static int TRACE_SIZE = 2000;
     private Chart cpuChart;
     private Chart memChart;
     private Chart bwInChart;
     private Chart bwOutChart;
     private Chart hdReadChart;
     private Chart hdWriteChart;
     
     private Chart selectedChart;
     private String selectedTier;
     private JPanel canvas;
     private Map<Integer, ITrace2D> traces;
     
     ChartPanel() {
         super();
         initializeCharts();
         traces = new HashMap<>();
         for (int i = 0; i < 3; i++) {
             // when the limit is exceeded, real strange behavior happens
             // mb use other trace class?
             ITrace2D trace = new Trace2DLtd(TRACE_SIZE);
             selectedChart.addTrace(trace);
             traces.put(i, trace);
         }
     }
     
     private void initializeCharts() {
         cpuChart = new Chart();
         memChart = new Chart();
         bwInChart = new Chart();
         bwOutChart = new Chart();
         hdReadChart = new Chart();
         hdWriteChart = new Chart();
         selectedChart = cpuChart;
     }
     
     public void render(JPanel canvas) {
         this.canvas = canvas;
         render();
     }
     
     public void render() {
         canvas.removeAll();
         selectedChart.render(canvas, selectedTier);
     }
     
     public void selectTier(String tier) {
         selectedTier = tier;
     }
     public void selectCpuChart() {
         selectAndUpdate(cpuChart);
        System.out.println("cpuChart");
     }
     public void selectMemChart() {
         selectAndUpdate(memChart);
     }
     public void selectBwInChart() {
         selectAndUpdate(bwInChart);
        System.out.println("bwInChart");
     }
     public void selectBwOutChart() {
         selectAndUpdate(bwOutChart);
        System.out.println("bwOutChart");
     }
     public void selectHdReadChart() {
         selectAndUpdate(hdReadChart);
     }
     public void selectHdWriteChart() {
         selectAndUpdate(hdWriteChart);
     }
 
     public void addCpuValue(String tier, Double cpuUtil) {
         cpuChart.addValue(tier, cpuUtil);
     }
 
     public void addMemValue(String tier, Double memUtil) {
         memChart.addValue(tier, memUtil);
     }
 
     public void addBwInValue(String tier, Double bwInUtil) {
         bwInChart.addValue(tier, bwInUtil);
     }
 
     public void addBwOutValue(String tier, Double bwOutUtil) {
         bwOutChart.addValue(tier, bwOutUtil);
     }
 
     public void addHdReadValue(String tier, Double hdReadUtil) {
         hdReadChart.addValue(tier, hdReadUtil);
     }
 
     public void addHdWriteValue(String tier, Double hdWriteUtil) {
         hdWriteChart.addValue(tier, hdWriteUtil);
     }
 
     private void selectAndUpdate(Chart chart) {
         if (selectedChart == chart)
             return;
         selectedChart = chart;
         render();
     }
 
     public void selectChart(String chartType) {
         switch (chartType) {
             case StringConstants.Resource.CPU:           selectCpuChart();       break;
             case StringConstants.Resource.MEMORY:        selectMemChart();       break;
             case StringConstants.Resource.BANDWIDTH_IN:  selectBwInChart();      break;
            case StringConstants.Resource.BANDWIDTH_OUT: selectBwOutChart();       break;
             case StringConstants.Resource.HD_READ:       selectHdReadChart();    break;
             case StringConstants.Resource.HD_WRITE:      selectHdWriteChart();   break;
             default:                                     selectCpuChart();       break;
         }
     }
     
 }
