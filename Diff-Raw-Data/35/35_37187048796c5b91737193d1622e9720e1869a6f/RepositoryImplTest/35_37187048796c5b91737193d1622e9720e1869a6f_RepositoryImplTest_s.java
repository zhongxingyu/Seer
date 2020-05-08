 package org.osehra.das.repo.bluebutton;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.apache.commons.codec.binary.Base64;
 import org.junit.Before;
 import org.junit.Test;
 import org.osehra.das.C32Document;
 import org.osehra.das.wrapper.nwhin.C32DocumentEntity;
 import org.springframework.jms.core.JmsTemplate;
 
 public class RepositoryImplTest extends AbstractDateAwareTests implements IC32DocumentDao, IMessageSendable {
 	RepositoryImpl repo;
 	List<C32DocumentEntity> docsInsertedList;
 	List<C32DocumentEntity> docsUpdatedList;
 	List<C32DocumentEntity> docsRetrieved;
 	List<AsyncRetrieveMessage> asyncMessageList;
 	JmsTemplate jmsTemplate;
 	
 	@Before
 	public void setup() {
 		repo = new RepositoryImpl();
 		repo.setC32DocumentDao(this);
 		docsInsertedList = new ArrayList<C32DocumentEntity>();
 		docsUpdatedList = new ArrayList<C32DocumentEntity>();
 		asyncMessageList = new ArrayList<AsyncRetrieveMessage>();
 		repo.setMessageSender(this);
 	}
 
 	@Test
	public void getStatus_nothingStoredOrStarted() {
		List<DocStatus> list = repo.getStatus("1234", "fred");
		Assert.assertEquals(1, list.size());
		assertTodayIncomplete(list.get(0), "1234");
		
		assertMessageSent("1234", "fred");
		assertOnePersisted("1234");
	}

	@Test
	public void getStatus_oneOldDocStored() {
 		docsRetrieved = new ArrayList<C32DocumentEntity>(1);
		docsRetrieved.add(new C32DocumentEntity("112233", "112233v10", getDate(2012, 10, 10, 9, 8, 7), "<doc>hi there</doc>"));
 		List<DocStatus> list = repo.getStatus("112233", "jones");
		Assert.assertEquals(2, list.size());
		assertTodayIncomplete(list.get(1), "112233");
 		assertDateEquals(list.get(0).getDateGenerated(), 2012, 10, 10, 9, 8, 7);
 		Assert.assertEquals("COMPLETE", list.get(0).getStatus());
 		Assert.assertEquals("112233", list.get(0).getPatientId());
 		
 		assertMessageSent("112233", "jones");
 		assertOnePersisted("112233");
 	}
 	
 	@Test
 	public void getStatus_todayAlreadyRequested() {
 		docsRetrieved = new ArrayList<C32DocumentEntity>(1);
 		docsRetrieved.add(new C32DocumentEntity("223344", "223344v10", new java.sql.Date(new Date().getTime()), "incomplete"));
 		List<DocStatus> list = repo.getStatus("223344", "jill");
 		Assert.assertEquals(1, list.size());
 		
 		Assert.assertEquals(0, asyncMessageList.size());
 		Assert.assertEquals(0, docsUpdatedList.size());
 		Assert.assertEquals(0, docsInsertedList.size());
 	}
 
 	@Test
 	public void getDocument_errors() {
 		try {
 			repo.getDocument(new Date(), "12321");
 			Assert.fail("exception not thrown");
 		}
 		catch (RuntimeException ex) {
 			Assert.assertTrue(ex.getMessage().startsWith("document for date:"));
 		}
 	}
 
 	@Test
 	public void getDocument() {
 		String xml = "<hiThere></hiThere>";
 		docsRetrieved = new ArrayList<C32DocumentEntity>();
 		docsRetrieved.add(new C32DocumentEntity("12321", "12321", new java.sql.Date(new Date().getTime()), xml));
 		C32Document doc = repo.getDocument(new Date(), "12321");
 		Assert.assertNotNull(doc);
 		Assert.assertEquals("12321", doc.getPatientId());
 		Assert.assertEquals(xml, new String(Base64.decodeBase64(doc.getDocument())));
 	}
 	
 	protected void assertOnePersisted(String ptId) {
 		Assert.assertEquals(1, docsInsertedList.size());
 		Assert.assertEquals(ptId, docsInsertedList.get(0).getIcn());
 		Assert.assertEquals(ptId, docsInsertedList.get(0).getDocumentPatientId());
 		Assert.assertEquals(BlueButtonConstants.INCOMPLETE_STATUS_STRING, docsInsertedList.get(0).getDocument());
 		assertDatePartEqualsToday(docsInsertedList.get(0).getCreateDate());
 	}
 	
 	protected void assertMessageSent(String ptId, String ptName) {
 		Assert.assertEquals(1, asyncMessageList.size());
 		assertDatePartEqualsToday(asyncMessageList.get(0).getDate());
 		Assert.assertEquals(ptId, asyncMessageList.get(0).getPatientId());
 		Assert.assertEquals(ptName, asyncMessageList.get(0).getPatientName());
 	}
 	
 	protected void assertTodayIncomplete(DocStatus docStatus, String expectedPtId) {
 		Assert.assertNotNull(docStatus);
 		assertDatePartEqualsToday(docStatus.getDateGenerated());
 		Assert.assertEquals(BlueButtonConstants.INCOMPLETE_STATUS_STRING, docStatus.getStatus());
 		Assert.assertEquals(expectedPtId, docStatus.getPatientId());
 	}
 	
 	protected void assertDatePartEqualsToday(Date aDate) {
 		GregorianCalendar calExpected = new GregorianCalendar();
 		GregorianCalendar calTestDate = new GregorianCalendar();
 		calTestDate.setTime(aDate);
 		Assert.assertEquals(calExpected.get(GregorianCalendar.YEAR), calTestDate.get(GregorianCalendar.YEAR));
 		Assert.assertEquals(calExpected.get(GregorianCalendar.MONTH), calTestDate.get(GregorianCalendar.MONTH));
 		Assert.assertEquals(calExpected.get(GregorianCalendar.DATE), calTestDate.get(GregorianCalendar.DATE));
 	}
 
 	//==========================
 	
 	@Override
 	public void insert(C32DocumentEntity document) {
 		docsInsertedList.add(document);
 	}
 	
 	@Override
 	public void update(C32DocumentEntity document) {
 		docsUpdatedList.add(document);
 	}
 
 	@Override
 	public List<C32DocumentEntity> getAllDocuments(String patientId) {
 		return docsRetrieved;
 	}
 
 	//==========================
 
 	@Override
 	public void sendRetrieveMessage(String patientId, String patientName,
 			Date documentDate) {
 		asyncMessageList.add(new AsyncRetrieveMessage(documentDate, patientId, patientName));
 	}
 	
 }
