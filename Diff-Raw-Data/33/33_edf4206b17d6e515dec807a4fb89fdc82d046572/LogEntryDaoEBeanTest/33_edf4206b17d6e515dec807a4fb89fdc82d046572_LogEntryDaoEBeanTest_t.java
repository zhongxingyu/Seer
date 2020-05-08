 package dk.nsi.minlog.server.dao.ebean;
 
 import static org.mockito.Mockito.*;
 
 import java.util.ArrayList;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Answers;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.avaje.ebean.EbeanServer;
 
 import dk.nsi.minlog.domain.LogEntry;
 import dk.nsi.minlog.server.dao.LogEntryDao;
 
 @RunWith(MockitoJUnitRunner.class)
 public class LogEntryDaoEBeanTest {
 
 	@InjectMocks
 	LogEntryDao logEntryDao = new LogEntryDaoEBean();
 	
 	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
 	EbeanServer ebeanServer;
 		
	
	//TODO assert on returns from ebean
 	@Test
 	public void findByCpr() throws Exception {
 		when(ebeanServer.find(LogEntry.class).where().eq("cprNrBorger", "1234").findList()).thenReturn(new ArrayList<LogEntry>());
 		logEntryDao.findLogEntriesByCPRAndDates("1234", null, null);
 	}
 
 	@Test
 	public void findByCprAndFrom() throws Exception {
 		when(ebeanServer.find(LogEntry.class).where().eq("cprNrBorger", "1234").ge(eq("tidspunkt"), any()).findList()).thenReturn(new ArrayList<LogEntry>());		
 		logEntryDao.findLogEntriesByCPRAndDates("1234", DateTime.now(), null);
 	}
 
 	@Test
 	public void findByCprAndTo() throws Exception {
 		when(ebeanServer.find(LogEntry.class).where().eq("cprNrBorger", "1234").le(eq("tidspunkt"), any()).findList()).thenReturn(new ArrayList<LogEntry>());		
 		logEntryDao.findLogEntriesByCPRAndDates("1234", null, DateTime.now());
 	}
 
 }
