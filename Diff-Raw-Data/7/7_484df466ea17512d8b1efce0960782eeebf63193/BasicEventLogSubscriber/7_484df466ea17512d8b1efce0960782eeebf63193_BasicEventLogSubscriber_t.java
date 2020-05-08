 package org.rotarysource.subscriber.basicevent;
 
 import org.rotarysource.events.BasicEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Implements a Log Subscriber for Basic Events Object.
  * This subscriber is designed for Log all data contained in a
  * BasicEvent captured by EPL. It log  data in
  * a log file or console specified in log4j.xml
  * 
  * 
  * Its specially conceived for EPL queries testing logging 
  * results of the Basic Event that trigger the query.
  * 
  * To use it correctly the EPL query must be based in:
  * select * from BasicEvent     query, in order to receive
  * a BasicEvent object
  * 
  @author J. L. Canales
  * 
  */
 public class BasicEventLogSubscriber
 {
 	private static Logger log = LoggerFactory.getLogger(BasicEventLogSubscriber.class);
 
 	/**
 	 * processEvent method implements a routine that go over generated Map
 	 * and log all its entries.
 	 * 
 	 * @param Map<String, String> Map received form MapBaseListener parent class
 	 */
 	public void update(BasicEvent event) {
 
 		StringBuffer logText = new StringBuffer();
 		logText.append("Received event: ")
                .append( event.toString());		
        	log.info(logText.toString());
 	}
 }
