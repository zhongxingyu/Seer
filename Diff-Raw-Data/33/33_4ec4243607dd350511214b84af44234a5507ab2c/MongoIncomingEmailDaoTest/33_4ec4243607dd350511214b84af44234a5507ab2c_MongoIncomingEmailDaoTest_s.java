 package fulbot.persistence.mongo;
 
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
 
 import org.junit.Before;
 import org.junit.Test;
import org.mockito.ArgumentCaptor;
 
import com.mongodb.BasicDBList;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 
 public class MongoIncomingEmailDaoTest {
 
 	private DB db;
 	private DBCollection dbCollection;
 	private MongoIncomingEmailDao dao;
 	
 	@Before
 	public void setUp() throws Exception {
 		db = mock(DB.class);
 		dbCollection = mock(DBCollection.class);
 		
 		when(db.getCollection(anyString())).thenReturn(dbCollection);
 		
 		dao = new MongoIncomingEmailDao(db);
 	}
 
 	@Test
 	public void saveShouldSaveJsonArrays() {
 		dao.save("[{name: \"John\"}, {name: \"Paul\"}]");
 		
		ArgumentCaptor<BasicDBList> captor = ArgumentCaptor.forClass(BasicDBList.class);
		verify(dbCollection).insert(captor.capture());
		assertEquals(2, captor.getValue().size());
 	}
 
 }
