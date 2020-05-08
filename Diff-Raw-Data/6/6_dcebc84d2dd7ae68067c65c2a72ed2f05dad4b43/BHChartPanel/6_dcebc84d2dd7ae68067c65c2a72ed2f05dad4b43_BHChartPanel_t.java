 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.bh.gui.chart;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import org.bh.gui.swing.IBHComponent;
 import org.bh.platform.Services;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 
 /**
  *
  * @author Marco Hammel
  */
 public class BHChartPanel extends JPanel implements IBHComponent, IBHAddValue{
     private static final long serialVersionUID = -8018664370176080809L;
 
     private String key;
     private String inputHint;
     private Class<? extends IBHAddValue> chartClass;
     private IBHAddValue chartInstance;
    private JFreeChart chart;
 
     public BHChartPanel(Object key, JFreeChart chart, Class<? extends IBHAddValue> chartClass, IBHAddValue chartInstance){
     	this.key = key.toString();
         this.chartClass = chartClass;
         this.chartInstance = chartInstance;
     	this.setLayout(new BorderLayout());
         this.add(new ChartPanel(chart), BorderLayout.CENTER);
         JTextArea description = new JTextArea(Services.getTranslator().translate(key + BHChart.DESC));
         description.setEditable(false);
         description.setAutoscrolls(true);
         description.setBorder(BorderFactory.createLoweredBevelBorder());
         description.setLineWrap(true);
         description.setWrapStyleWord(true);
         description.setPreferredSize(new Dimension(250, 50));
     	this.add(description, BorderLayout.EAST);
    	this.chart = chart;
     }
 
     /**
      * get the semantic object for the chart by changing dataset at runtime
      * @return
      */
     public Class<? extends IBHAddValue> getChartClass(){
         return this.chartClass;
     }
     
     public JFreeChart getChart(){
    	return chart;
     }
     public IBHAddValue getChartInstance(){
         return this.chartInstance;
     }
 
     public String getHint() {
         return this.inputHint;
     }
 
     public String getKey() {
         return this.key;
     }
 
     protected void reloadText() {
 //	inputHint = Services.getTranslator().translate(key, ITranslator.LONG);
 //	setToolTipText(inputHint);
     }
 
     public void addSeries(Comparable<String> key, double[] values, int bins, double minimum, double maximum) {
         this.chartInstance.addSeries(key, values, bins, minimum, maximum);
     }
 
     public void addSeries(Comparable<String> seriesKey, double[][] data) {
         this.chartInstance.addSeries(key, data);
     }
 
     public void addValue(Number value, Comparable<String> columnKey) {
         this.chartInstance.addValue(value, columnKey);
     }
 
     public void addValues(List<?> list) {
         this.chartInstance.addValues(list);
     }
 
 	@Override
 	public void addValue(Number value, Comparable rowKey,
 			Comparable<String> columnKey) {
 		 this.chartInstance.addValue(value, rowKey, columnKey);
 	}
 
 
 
 
 }
