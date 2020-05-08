 package org.rotarysource.core.sep.task;
 
 import java.util.Date;
 import java.util.HashMap;
 
 import org.rotarysource.core.CepEngine;
 import org.rotarysource.events.BasicEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class TaskTemplate implements SepTask {
 
 
     /** 
      * Apache commons login logger instance
      */	
 	private static Logger log = LoggerFactory.getLogger(TaskTemplate.class);
     
     /** 
      * Reference to Cep Engine that events will be sent
      */ 
     private CepEngine cepEngine;
 
     /**
      * Sets CEP Engine to be able to send events to it
      * @param cepEngine
      */
 	public void setCepEngine(CepEngine cepEngine) {
 		this.cepEngine = cepEngine;
 	}
  
 	/**
 	 * Create and send a BasicEvent to cep engine to be managed
 	 * by it.
 	 * @param eventCode Event Code
 	 * @param eventType Event Type
 	 * @param compDataMap Additional Data to be attached to event
 	 */
 	protected void sendEvent( String eventCode, 
 						   String eventType, 
 						   HashMap<String, String> compDataMap){
 		
 		BasicEvent event = new BasicEvent();
         Date       date       = new Date();  
 		
         event.setEventId(Integer.toString((int)date.getTime()));
         event.setEventCode(eventCode);
         event.setEventTimestamp(date);
         event.setEventType(eventType);
         event.setSystemId("sepTask");
         
         event.setCompData( compDataMap);
 		
        if(cepEngine != null){
	        log.debug("Sending event to CEP: {}", event);
	        cepEngine.getCepEngine().getEPRuntime().sendEvent(event);
        }
        else{
        	log.warn("Event could not be sended to CEP; cause: null CepEngine. Event: {}", event.toString());
        }
 	}
 }
