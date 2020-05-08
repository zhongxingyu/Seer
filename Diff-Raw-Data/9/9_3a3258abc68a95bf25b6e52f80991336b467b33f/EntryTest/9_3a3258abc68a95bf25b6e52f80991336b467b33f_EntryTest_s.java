 package org.mule.galaxy.impl;
 
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.Entry;
 import org.mule.galaxy.EntryResult;
 import org.mule.galaxy.EntryVersion;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.lifecycle.Phase;
 import org.mule.galaxy.query.OpRestriction;
 import org.mule.galaxy.query.Query;
 import org.mule.galaxy.query.SearchResults;
 import org.mule.galaxy.test.AbstractGalaxyTest;
 
 public class EntryTest extends AbstractGalaxyTest {
     public void testEntries() throws Exception {
         Workspace root = registry.getWorkspaces().iterator().next();
     
         EntryResult r = root.newEntry("MyService", "1.0");
         assertNotNull(r);
     
         Entry e = r.getEntry();
         assertNotNull(e);
     
         EntryVersion ev = e.getDefaultOrLastVersion();
         assertNotNull(ev);
         ev.setProperty("endpoint", "http://localhost:9000/foo");
         ev.setProperty("serviceType", "HTTP");
     
         assertEquals("1.0", ev.getVersionLabel());
         
         Phase phase = getPhase(ev);
         assertNotNull(phase);
         
         r = e.newVersion("2.0");
         assertNotNull(r);
         
         ev = r.getEntryVersion();
         assertNotNull(ev);
         
         assertEquals("2.0", ev.getVersionLabel());
         
         e = (Entry)registry.getItemByPath(e.getPath());
     }
     
     public void testQueries() throws Exception {
         Workspace root = registry.getWorkspaces().iterator().next();
     
         EntryResult r = root.newEntry("MyService", "1.0");
         assertNotNull(r);
     
         Entry e = r.getEntry();
         assertNotNull(e);
     
         EntryVersion ev = e.getDefaultOrLastVersion();
         assertNotNull(ev);
         String address = "http://localhost:9000/foo";
         ev.setProperty("endpoint", address);
    
         Query q = new Query(Entry.class).add(OpRestriction.eq("endpoint", address));
         
         SearchResults results = registry.search(q);
         
         assertEquals(1, results.getTotal());
         
         q = new Query(Entry.class, Artifact.class);
         
         importHelloWsdl();
         
         results = registry.search(q);
         assertEquals(2, results.getTotal());
         
         Entry entry = null;
         for (Object o : results.getResults()) {
             if ("MyService".equals(((Entry) o).getName())) {
                 entry = (Entry) o;
             }
         }
         
         assertNotNull(entry);
     }
 }
