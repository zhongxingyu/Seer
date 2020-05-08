 package com.example.cubicmetercommunity.chart;
 
 import java.util.HashMap;
 
 import org.afree.chart.AFreeChart;
 import org.afree.chart.ChartFactory;
 import org.afree.data.general.DefaultPieDataset;
 import org.afree.data.general.PieDataset;
 
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 
@SuppressLint("ViewConstructor")
 public class PieChartView extends DemoView{
 		
 		
 	  public PieChartView(Context context, String title, HashMap<String, Double> data ) {
 	        super(context);
 	        PieDataset dataset = createDataset(data);
 	        AFreeChart chart = createChart(dataset, title);
 
 	        setChart(chart);
 	    }
 	  
 	  @SuppressLint("UseValueOf")
 	private static PieDataset createDataset(HashMap<String, Double> data) {
 		          DefaultPieDataset dataset = new DefaultPieDataset();
 		          for (String k : data.keySet()){
 		        	 dataset.setValue(k, data.get(k));		        	  
 		          }		       
 		      
 		          
 		          return dataset;
 		      }
 	  
 	  private static AFreeChart createChart(PieDataset dataset, String title) {
 		  
 		  AFreeChart chart = ChartFactory.createPieChart(
 		              title + " Chart",  // chart title
 		             dataset,            // data
 		              true,              // no legend
 		              true,               // tooltips
 		             false               // no URL generation
 		          );
 
 		          return chart;
 		  
 		      }
 }
