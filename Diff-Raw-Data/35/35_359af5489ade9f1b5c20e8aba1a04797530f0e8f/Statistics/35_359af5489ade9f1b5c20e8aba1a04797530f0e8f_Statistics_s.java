 package com.wolvencraft.prison.metrics;
  
 import java.io.IOException;
 import java.util.logging.Level;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.metrics.Metrics.Graph;
 import com.wolvencraft.prison.util.Message;
  
 public class Statistics {
      
     private Metrics metrics;
      
     public Statistics(PrisonSuite plugin) {
     	if(PrisonSuite.getSettings().METRICS) {
	        try { this.metrics = new Metrics(plugin); }
 	        catch (IOException e) { Message.log(Level.SEVERE, "Unable to start PluginMetrics"); }
     	}
     }
      
     public void gatherData() {
        if(metrics.isOptOut()) return;
         
         Graph pluginsRunning = metrics.createGraph("Number of PrisonSuite plugins running");
         
         pluginsRunning.addPlotter(new Metrics.Plotter("plugins") {
             @Override
             public int getValue() {
                 return PrisonSuite.getPlugins().size();
             }
         });
     }
      
     public Metrics getMetrics() {
         return metrics;
     }
 }
