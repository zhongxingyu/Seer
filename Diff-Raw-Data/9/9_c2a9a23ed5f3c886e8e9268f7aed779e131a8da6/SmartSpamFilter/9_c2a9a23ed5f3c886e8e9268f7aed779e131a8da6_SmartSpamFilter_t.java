 package com.phdroid.smsb.filter;
 
 import com.phdroid.smsb.SmsPojo;
 import com.phdroid.smsb.exceptions.ApplicationException;
 import com.phdroid.smsb.storage.dao.Session;
 import com.phdroid.smsb.storage.dao.SmsMessageSenderEntry;
 
 /**
  * Chain of spam filters. Pretends to be smart.
  */
 public class SmartSpamFilter implements ISpamFilter {
 	private Session session;
 
 	public SmartSpamFilter(Session session) {
 		this.session = session;
 	}
 
 	public Session getSession() {
 		return this.session;
 	}
 
 	@Override
 	public boolean isSpam(SmsPojo message) throws ApplicationException {
		ISpamFilter contactSpamFilter = new ContactSpamFilter(getSession());
		ISpamFilter whiteListSpamFilter = new WhiteListSpamFilter(getSession());
 
		return whiteListSpamFilter.isSpam(message) ? contactSpamFilter.isSpam(message) : false;
 	}
 }
