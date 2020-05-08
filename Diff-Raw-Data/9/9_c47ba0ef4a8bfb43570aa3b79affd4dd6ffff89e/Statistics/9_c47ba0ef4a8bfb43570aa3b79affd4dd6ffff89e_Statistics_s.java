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
 	        try {
 	        	this.metrics = new Metrics(plugin);
 	        	metrics.start();
 	        }
 	        catch (IOException e) { Message.log(Level.SEVERE, "Unable to start PluginMetrics"); }
     	} else {
     		Message.log("PluginMetrics disabled by the configuration");
     	}
     }
      
     public void gatherData() {
     	Message.debug("Gathering data");
         if(metrics.isOptOut() || !PrisonSuite.getSettings().METRICS) return;
         
         Message.debug("Op-out check passed");
         Graph pluginsRunning = metrics.createGraph("Number of PrisonSuite plugins running");
         Message.debug("New graph created");
         
         pluginsRunning.addPlotter(new Metrics.Plotter("plugins") {
             @Override
             public int getValue() {
             	Message.debug("Sending data to Metrics. " + PrisonSuite.getPlugins().size() + " plugins");
                 return PrisonSuite.getPlugins().size();
             }
         });
     }
      
     public Metrics getMetrics() {
         return metrics;
     }
 }
