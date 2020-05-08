 package org.osehra.das.repo.bluebutton;
 
 import java.sql.Date;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.TextMessage;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osehra.das.BeanUtils;
 import org.osehra.das.IFormatTS;
 import org.osehra.das.wrapper.nwhin.C32DocumentEntity;
 import org.osehra.das.wrapper.nwhin.C32DocumentEntityFactory;
 import org.osehra.das.wrapper.nwhin.IWrapperResource;
 
 public class NwhinDataRetriever extends AbstractC32DaoAware implements MessageListener {
 	protected IWrapperResource _nwhinResource;
 	protected IFormatTS asyncMessageFormat;
 	protected static Log logger = LogFactory.getLog(NwhinDataRetriever.class);
 	protected C32DocumentEntityFactory documentFactory;
 	
 	public IFormatTS getAsyncMessageFormat() {
 		return asyncMessageFormat;
 	}
 
 	public void setAsyncMessageFormat(IFormatTS asyncMessageFormat) {
 		this.asyncMessageFormat = asyncMessageFormat;
 	}
 
 	public C32DocumentEntityFactory getDocumentFactory() {
 		return documentFactory;
 	}
 
 	public void setDocumentFactory(C32DocumentEntityFactory documentFactory) {
 		this.documentFactory = documentFactory;
 	}
 
 	public void setWrapperResource(IWrapperResource resource) {
 		_nwhinResource = resource;
 	}
 	
 	@Override
 	public void onMessage(Message msg) {
 		TextMessage tMsg = (TextMessage)msg;
 		AsyncRetrieveMessage aMsg;
		System.out.println("Whaaa, a message?");
 		try {
 			aMsg = (AsyncRetrieveMessage)getAsyncMessageFormat().parse(tMsg.getText());
 			if (logger.isDebugEnabled()) {
 				logger.debug("calling getDomainXml for " + aMsg);
 			}
 			String xml = (String)_nwhinResource.getDomainXml(aMsg.getPatientId(), aMsg.getPatientName());
 			logger.error("attempting to persist domain XML: " + xml);
 			updateDocumentWithNewDocument(getDocumentFactory().createDocument(aMsg.getPatientId(), xml));
 		} catch (JMSException e) {
 			logger.error("JMS exception for " + msg, e);
 		} catch (Exception e) {
 			logger.error("general exception for " + msg, e);
 		}
 	}
 	
 	protected void updateDocumentWithNewDocument(C32DocumentEntity newDoc) {
 		boolean debugging = logger.isDebugEnabled();
 		C32DocumentEntity oldDocument = getOldDocument(newDoc);
 		if (oldDocument==null) {
 			if (debugging) {
 				logger.debug("no old document, so inserting new:" + newDoc);
 			}
 			getC32DocumentDao().insert(newDoc);
 			return;
 		}
 		
 		if (newDoc.getDocument() != null && newDoc.getDocument().isEmpty() == false) {
 		oldDocument.setCreateDate(newDoc.getCreateDate());
 		oldDocument.setDocument(newDoc.getDocument());
 		oldDocument.setDocumentPatientId(newDoc.getDocumentPatientId());
 		if (debugging) {
 			logger.debug("merging (updating) document:" + oldDocument);
 		}
 		getC32DocumentDao().update(oldDocument);
 		}
 	}
 
 	protected C32DocumentEntity getOldDocument(C32DocumentEntity newDoc) {
     	List<C32DocumentEntity> results = getC32DocumentDao().getAllDocuments(newDoc.getIcn());
     	return getOldDocFromList(results, newDoc);
     }
 
 	protected C32DocumentEntity getOldDocFromList(List<C32DocumentEntity> results, C32DocumentEntity newDoc) {
 		if (results!=null && results.size()>0) {
 			GregorianCalendar cal = new GregorianCalendar();
 			if (results.size()>1) {
 				Collections.sort(results);
 			}
 			for (int i=results.size()-1;i>-1;i--) {
 				if (newDoc.getIcn().equals(results.get(i).getIcn()) &&
 						BeanUtils.equalsNullSafe(results.get(i).getDocument(), BlueButtonConstants.INCOMPLETE_STATUS_STRING) &&
 						datesEqual(cal, results.get(i).getCreateDate(), newDoc.getCreateDate())) {
 					return results.get(i);
 				}
 			}
 		}
 		return null;
 	}
 
 	protected boolean datesEqual(Calendar cal, Date date1, Date date2) {
 		if (date1!=null && date2!=null) {
 			cal.setTime(date1);
 			int year1 = cal.get(Calendar.YEAR);
 			int month1 = cal.get(Calendar.MONTH);
 			int day1 = cal.get(Calendar.DATE);
 
 			cal.setTime(date2);
 			int year2 = cal.get(Calendar.YEAR);
 			int month2 = cal.get(Calendar.MONTH);
 			int day2 = cal.get(Calendar.DATE);
 			return year1==year2 && month1==month2 && day1==day2;
 		}
 		return date1==null && date2==null;
 	}
     
 
 }
