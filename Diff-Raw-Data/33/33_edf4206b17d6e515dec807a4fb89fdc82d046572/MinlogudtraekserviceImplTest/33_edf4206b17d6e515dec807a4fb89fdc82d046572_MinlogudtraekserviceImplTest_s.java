 package dk.nsi.minlog.web;
 
 import static java.util.Arrays.asList;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.*;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
 
 import dk.nsi.minlog._2012._05._24.ListLogStatementsRequest;
 import dk.nsi.minlog._2012._05._24.ListLogStatementsResponse;
 import dk.nsi.minlog.domain.LogEntry;
 import dk.nsi.minlog.server.dao.LogEntryDao;
 
 @RunWith(MockitoJUnitRunner.class)
 @SuppressWarnings("restriction")
 public class MinlogudtraekserviceImplTest {
 
 	@Mock
 	LogEntryDao logEntryDao;
 	
 	@InjectMocks
 	MinlogudtraekserviceImpl service;
 
 	private DateTime to = DateTime.now();
 	private DateTime from = to.minusYears(2);
 	private XMLGregorianCalendar fromXml = new XMLGregorianCalendarImpl(from.toGregorianCalendar()); 
 	private XMLGregorianCalendar toXml = new XMLGregorianCalendarImpl(to.toGregorianCalendar()); 
 
 	private final static String[] nameFormat = new String[]{"YNUMBER", "PNUMBER", "SKSCODE", "CVRNUMBER", "COMMUNALNUMBER", "SOR"};
 
 	@Before
 	public void setupDao(){
 		int size = 10;
 		LogEntry[] entries = new LogEntry[size];
 		
 		for(int i = 0; i < 10; i++){
 			final long id = i;
 			entries[i] = (new LogEntry(){{
 				setAnsvarlig("ansvarlig" + id);
 				setBruger("bruger" + id);
 				setCprNrBorger("1234");
 				setHandling("handling" + id);
 				setId(id);
 				setOrgUsingID(nameFormat[(int)id % nameFormat.length] + ":test" + id);
 				setRegKode("regKode" + id);
 				setSessionId("abc" + id);
 				setSystemName("System" + id);
 				setTidspunkt(new DateTime());
 			}});
 		}	
 		
 		LogEntry error = (new LogEntry(){{
 			setAnsvarlig("ansvarlig");
 			setBruger("bruger");
 			setCprNrBorger("1234");
 			setHandling("handling");
 			setId(10l);
 			setOrgUsingID("error:test");
 			setRegKode("regKode");
 			setSessionId("abc");
 			setSystemName("System");
 			setTidspunkt(new DateTime());
 		}});
 		
 		
 		when(logEntryDao.findLogEntriesByCPRAndDates(eq("1234"), (DateTime)isNull(), (DateTime)isNull())).thenReturn(asList(new LogEntry[]{
 			entries[0], entries[1]
 		}));
 	
 		when(logEntryDao.findLogEntriesByCPRAndDates(eq("1234"), (DateTime)any(), (DateTime)isNull())).thenReturn(asList(new LogEntry[]{
 				entries[2], entries[3]
 		}));
 
 		when(logEntryDao.findLogEntriesByCPRAndDates(eq("1234"), (DateTime)isNull(), (DateTime)any())).thenReturn(asList(new LogEntry[]{
 				entries[4], entries[5]
 		}));
 
 		when(logEntryDao.findLogEntriesByCPRAndDates(eq("1234"), (DateTime)notNull(), (DateTime)notNull())).thenReturn(asList(new LogEntry[]{
 				entries[6], entries[7]
 		}));
 		
 		when(logEntryDao.findLogEntriesByCPRAndDates(eq("error"), (DateTime)any(), (DateTime)any())).thenReturn(asList(new LogEntry[]{
 				error
 		}));
 	}
 	
 	@Test
 	public void cpr() {
 		ListLogStatementsRequest request = new ListLogStatementsRequest();
 		request.setCprNR("1234");
 		
 		ListLogStatementsResponse response = service.listLogStatements(request, null);
 		assertEquals("1234", response.getCprNrBorger());
 	}
 	
	@Test
	public void otherFields(){
		
	}
 	
 	@Test
 	public void multipleEntries() {
 		ListLogStatementsRequest request = new ListLogStatementsRequest();
 		request.setCprNR("1234");
 		
 		ListLogStatementsResponse response = service.listLogStatements(request, null);
 		assertEquals(2, response.getLogEntry().size());
 	}
 	
 	@Test
 	public void fromDate() {
 		ListLogStatementsRequest request = new ListLogStatementsRequest();
 		request.setCprNR("1234");
 		request.setFraDato(fromXml);		
 		ListLogStatementsResponse response = service.listLogStatements(request, null);
 		assertEquals("bruger2", response.getLogEntry().get(0).getBruger());
 		assertEquals("bruger3", response.getLogEntry().get(1).getBruger());
 	}	
 
 	@Test
 	public void toDate() {
 		ListLogStatementsRequest request = new ListLogStatementsRequest();
 		request.setCprNR("1234");
		request.setTilDato(fromXml);
 		ListLogStatementsResponse response = service.listLogStatements(request, null);
 		assertEquals("bruger4", response.getLogEntry().get(0).getBruger());
 		assertEquals("bruger5", response.getLogEntry().get(1).getBruger());
 	}	
 
 	@Test
 	public void bothDate() {
 		ListLogStatementsRequest request = new ListLogStatementsRequest();
 		request.setCprNR("1234");		
 		request.setFraDato(fromXml);
 		request.setTilDato(toXml);
 		ListLogStatementsResponse response = service.listLogStatements(request, null);
 		assertEquals("bruger6", response.getLogEntry().get(0).getBruger());
 		assertEquals("bruger7", response.getLogEntry().get(1).getBruger());
 	}
 
 	@Test(expected=RuntimeException.class)
 	public void formatError() {
 		ListLogStatementsRequest request = new ListLogStatementsRequest();
 		request.setCprNR("error");
 		service.listLogStatements(request, null);		
 	}
 }
