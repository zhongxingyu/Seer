 package com.clearboxmedia.couchspring.it;
 
import org.junit.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.jcouchdb.db.Database;
 import org.jcouchdb.db.Options;
 import org.jcouchdb.document.ViewResult;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.clearboxmedia.couchspring.couch.PersistanceService;
 import com.clearboxmedia.couchspring.domain.Event;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/root-context.xml")
 public class CouchViewTest {
 	
 	@Autowired
     private Database database;
 	
     @Test
 	public void testListingQuery() {
 		String viewName = "event/list";
 		ViewResult<Event> results = database.queryView(viewName, Event.class, null, null);
 	}
 	
     @Test
 	public void testQueryByVenueId() {
 		String viewName = "event/allByVenueId";
 		
 		ViewResult<Event> results = database.queryViewByKeys(viewName, Event.class, Arrays.asList("id"), null, null);
 		
 	}
 	
     @Test
 	public void testQueryByDate() {
 		String viewName = "event/allByDate";
 		List<String> keys = new ArrayList<String>();
 		keys.add("year");
 		keys.add("month");
 		keys.add("day");
 		Options opts = new Options();
 		opts.startKey(keys);
 		opts.endKey("\u9999");
 		
 		ViewResult<Event> results = database.queryViewByKeys(viewName, Event.class, null, opts, null);
 		
 	}
 
 }
