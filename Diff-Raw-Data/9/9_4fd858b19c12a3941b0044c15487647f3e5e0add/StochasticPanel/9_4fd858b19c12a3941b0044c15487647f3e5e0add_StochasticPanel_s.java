 package org.bh.plugin.stochasticResultAnalysis;
 
 import info.clearthought.layout.TableLayout;
 
 import javax.swing.JPanel;
 
 import org.apache.log4j.Logger;
 import org.bh.gui.chart.BHChartFactory;
 import org.bh.gui.chart.BHChartPanel;
 import org.bh.plugin.resultAnalysis.BH_APV_ResultPanel;
 
 public class StochasticPanel extends JPanel{
 
	 private static final Logger log = Logger.getLogger(BH_APV_ResultPanel.class);
 	    //Chart
 	    private BHChartPanel distributionChart;
 	    
 	    public StochasticPanel(){
 	        this.initialize();
 	    }
 
 	    public void initialize() {
 	        double border = 10;
 	        double size[][] = {{border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border}, // Columns
 	            {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border,TableLayout.PREFERRED, 
 	        	border,TableLayout.PREFERRED}}; // Rows
 
 
 	        this.setLayout(new TableLayout(size));
 
 	        distributionChart = BHChartFactory.getXYBarChart(BHStochasticResultController.ChartKeys.DISTRIBUTION_CHART);
 	        
 
 	        this.add(distributionChart, "3,9");        
 	        
 	    }
 	}
