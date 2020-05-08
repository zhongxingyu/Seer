package com.github.joukojo.testgame;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class GraphicEngineWorker implements Runnable {
 
 	private GraphicEngine graphicEngine;
 	private volatile boolean isRunning;
 	private final Logger LOG = LoggerFactory.getLogger(GraphicEngineWorker.class);
 
 	public GraphicEngineWorker(GraphicEngine graphicEngine) {
 		this.graphicEngine = graphicEngine;
 		setRunning(true);		
 	}
 	
 	@Override
 	public void run() {
 
 		while (isRunning()) {
 			LOG.debug("drawing objects");
 			graphicEngine.drawObjects();
 			// Let the OS have a little time...
 			
 			LOG.debug("thread yield");
 			Thread.yield();
 
 		}
 		
 	}
 
 	public boolean isRunning() {
 		return isRunning;
 	}
 
 	public void setRunning(boolean isRunning) {
 		this.isRunning = isRunning;
 	}
 
 }
