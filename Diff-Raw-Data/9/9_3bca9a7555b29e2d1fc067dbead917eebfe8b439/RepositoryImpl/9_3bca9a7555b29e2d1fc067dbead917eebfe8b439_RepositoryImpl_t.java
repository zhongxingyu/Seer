 package org.osehra.das.repo.bluebutton;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.Queue;
 import javax.jms.Session;
 
 import org.osehra.das.C32Document;
 import org.osehra.das.wrapper.nwhin.C32DocumentEntity;
 import org.springframework.jms.core.JmsTemplate;
 import org.springframework.jms.core.MessageCreator;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.web.context.ContextLoader;
 import org.springframework.web.context.WebApplicationContext;
 
 import org.joda.time.LocalDate;
 
 public class RepositoryImpl extends AbstractAsyncMsgFormatAware implements Repository {
 	protected String completeStatusString = "COMPLETE";
 	protected String incompleteStatusString = "INCOMPLETE";
 	protected JmsTemplate jmsTemplate;
 	Log logger = LogFactory.getLog(this.getClass());
 	protected Queue queue;
 	protected C32DocumentDao repositoryDAO;
 
 	public C32DocumentDao getRepositoryDAO() {
 		if (repositoryDAO==null) {
 			WebApplicationContext cc = ContextLoader.getCurrentWebApplicationContext();
 			repositoryDAO = cc.getBean(C32DocumentDao.class);
 			if (repositoryDAO==null) {
 				logger.warn("repositoryDAO is null");
 			}
 		}
 		return repositoryDAO;
 	}
 
 	public void setRepositoryDAO(C32DocumentDao c32s) {
 		this.repositoryDAO = c32s;
 	}
 	public void setJmsTemplate(JmsTemplate template) {
         this.jmsTemplate = template;
     }
 
 	public String getIncompleteStatusString() {
 		return incompleteStatusString;
 	}
 	
 	public void setIncompleteStatusString(String incompleteString) {
 		incompleteStatusString = incompleteString;
 	}
 
 	public String getCompleteStatusString() {
 		return completeStatusString;
 	}
 	
 	public void setCompleteStatusString(String completeString) {
 		completeStatusString = completeString;
 	}
 
 	@Override
 	public List<DocStatus> getStatus(String patientId, String patientName) {
 		List<C32DocumentEntity> docList = getAllStoredDocuments(patientId);
 
 		Date today = new Date();
 		
 		if (getDocumentByDate(today, docList)==null) {
 			sendMessageToRetrieve(today, patientId, patientName);
 			return getDocStatusNotThere(patientId, today, docList);
 		}
 		return buildStatusList(docList);
 	}
 
 	@Override
 	public C32Document getDocument(Date docDate, String patientId) {
 		List<C32DocumentEntity> docList = getAllStoredDocuments(patientId); 
 		C32DocumentEntity doc = getDocumentByDate(docDate, docList);
 		if (doc==null) {
 			throw new RuntimeException("document for " + docDate + " " + patientId + " does not exist");
 		}
 		return getC32Doc(doc);
 	}
 
 	protected C32Document getC32Doc(C32DocumentEntity doc) {
 		C32Document newDoc = new C32Document();
 		newDoc.setDocument(doc.getDocument());
 		newDoc.setPatientId(doc.getIcn());
 		return newDoc;
 	}
 
 	protected C32DocumentEntity getDocumentByDate(Date today, List<C32DocumentEntity> docList) {
 		if (docList!=null && !docList.isEmpty()) {
 			for (int i=0;i<docList.size();i++) {
 
 				LocalDate fileDate = LocalDate.fromDateFields(docList.get(i).getCreateDate());
 				LocalDate todayDate = LocalDate.fromDateFields(today);
 
 				if (todayDate.equals(fileDate)) {
 					return docList.get(i);
 				}
 			}
 		}
 		return null;
 	}
 
 	protected List<C32DocumentEntity> getAllStoredDocuments(String patientId) {
 		return getRepositoryDAO().getAllDocuments(patientId);
 	}
 
 	protected List<DocStatus> buildStatusList(List<C32DocumentEntity> docList) {
 		if (docList==null || docList.isEmpty()) {
 			return null;
 		}
 		List<DocStatus> statusList = new ArrayList<DocStatus>(docList.size());
 		for (int i=0;i<docList.size();i++) {
 			statusList.add(new DocStatus());
 			statusList.get(i).setDateGenerated(docList.get(i).getCreateDate());
 			statusList.get(i).setPatientId(docList.get(i).getIcn());
 			statusList.get(i).setStatus(getCompleteStatusString());
 		}
 		return statusList;
 	}
 
 	protected List<DocStatus> getDocStatusNotThere(String ptId, Date today, List<C32DocumentEntity> docList) {
 		if (docList==null || docList.isEmpty()) {
			List<DocStatus> statusList = new ArrayList<DocStatus>(1);
			statusList.add(new DocStatus());
			statusList.get(0).setDateGenerated(today);
			statusList.get(0).setPatientId(ptId);
			statusList.get(0).setStatus(getIncompleteStatusString());
			
			return statusList;
			
 		}
 		int arraySize = docList.size() + 1;
 		List<DocStatus> statusList = new ArrayList<DocStatus>(arraySize);
 	
 
 
 		for (int i=0;i<docList.size();i++) {
 			statusList.add(new DocStatus());
 			statusList.get(i).setDateGenerated(docList.get(i).getCreateDate());
 			statusList.get(i).setPatientId(docList.get(i).getIcn());
 			statusList.get(i).setStatus(getCompleteStatusString());
 		}
 		
 		//Append today's entry.
 		statusList.add(new DocStatus());
 		statusList.get(docList.size()).setDateGenerated(today);
 		statusList.get(docList.size()).setPatientId(ptId);
 		statusList.get(docList.size()).setStatus(getIncompleteStatusString());
 		
 
 		return statusList;		
 
 	}
 
 	protected void sendMessageToRetrieve(final Date today, final String patientId, final String ptName) {
 		this.jmsTemplate.send(new MessageCreator() {
 			@Override
 			public Message createMessage(Session session) throws JMSException {
 				return session.createTextMessage(getAsyncMessageFormat().formatObject(new AsyncRetrieveMessage(today, patientId, ptName)));
 			}
 		});
 	}
 
 }
