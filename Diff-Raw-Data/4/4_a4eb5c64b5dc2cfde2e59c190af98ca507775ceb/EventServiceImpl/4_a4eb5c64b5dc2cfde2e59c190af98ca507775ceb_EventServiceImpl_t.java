 /**
  * Copyright (c) APVMA, 2013.
  */
 package au.gov.apvma.event.processor.service.impl;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.integration.annotation.ServiceActivator;
 
 import au.gov.apvma.event.processor.model.gen.CcTxRequest;
 import au.gov.apvma.event.processor.service.EventService;
 
 /**
  * A service implementation that handles an event.
  * 
  * @author peter
  *
  */
 public class EventServiceImpl implements EventService {
 
	private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);
 	
 	/* (non-Javadoc)
 	 * @see au.gov.apvma.event.processor.service.EventService#registerEvent()
 	 */
 	@Override
 	@ServiceActivator
 	public void registerEvent(CcTxRequest ccTx) {
 		// TODO Auto-generated method stub
 
 		log.info("-------------------------------------->>>>>>>>>>>  fired the event!!!!!!!!!");
 		log.info("ccTx txnId: " + ccTx.getTxnId());
 	}
 }
