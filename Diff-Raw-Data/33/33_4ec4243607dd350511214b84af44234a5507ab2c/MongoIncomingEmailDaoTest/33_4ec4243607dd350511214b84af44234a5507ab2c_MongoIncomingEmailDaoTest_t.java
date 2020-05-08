 package fulbot.persistence.mongo;
 
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
import com.mongodb.DBObject;
 
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
 		
		verify(dbCollection, times(2)).insert(any(DBObject.class));
 	}
 
 }
