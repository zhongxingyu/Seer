 package org.mule.galaxy.impl;
 
 import static org.mule.galaxy.query.OpRestriction.eq;
 import static org.mule.galaxy.query.OpRestriction.like;
 import static org.mule.galaxy.query.OpRestriction.not;
 import static org.mule.galaxy.query.OpRestriction.or;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.Entry;
 import org.mule.galaxy.EntryResult;
 import org.mule.galaxy.EntryVersion;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.extension.Extension;
 import org.mule.galaxy.impl.extension.IdentifiableExtensionQueryBuilder;
 import org.mule.galaxy.impl.link.LinkExtension;
 import org.mule.galaxy.query.OpRestriction;
 import org.mule.galaxy.query.Query;
 import org.mule.galaxy.query.Restriction;
 import org.mule.galaxy.query.SearchResults;
 import org.mule.galaxy.query.OpRestriction.Operator;
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.test.AbstractGalaxyTest;
 import org.mule.galaxy.type.PropertyDescriptor;
 
 public class SearchTest extends AbstractGalaxyTest {
     
     public void testQueries() throws Exception {
         Artifact artifact = importXmlSchema();
 
         // Try out search!
         Query q = new Query(Artifact.class)
             .add(OpRestriction.in("primary.lifecycle.phase", 
                      Arrays.asList(new String[] { "Default:Created", "Default:Developed" })));
         Set results = registry.search(q).getResults();
 
         assertEquals(1, results.size());
 
         q.setStart(1);
         results = registry.search(q).getResults();
         assertEquals(0, results.size());
         
         q.setStart(2);
         results = registry.search(q).getResults();
         assertEquals(0, results.size());
 
         // search by version
         q = new Query(Artifact.class)
             .add(OpRestriction.eq("version", artifact.getDefaultOrLastVersion().getVersionLabel()));
         
         results = registry.search(q).getResults();
     
         assertEquals(1, results.size());
         
         // search by lifecycle
         q = new Query(Artifact.class).add(OpRestriction.eq("primary.lifecycle.phase", "Default:Created"));
         results = registry.search(q).getResults();
     
         assertEquals(1, results.size());
 
         q = new Query(Artifact.class).add(OpRestriction.eq("primary.lifecycle", "Default"));
         results = registry.search(q).getResults();
     
         assertEquals(1, results.size());
         
         q = new Query(Artifact.class)
             .add(OpRestriction.in("primary.lifecycle.phase", 
                                 Arrays.asList(new String[] { "Default:XXXX", "Default:Developed" })));
         results = registry.search(q).getResults();
     
         assertEquals(0, results.size());
         
         // test OR
         q = new Query(Artifact.class)
             .add(OpRestriction.or(
                   OpRestriction.eq("primary.lifecycle.phase", "Default:XXXX"),
                   OpRestriction.eq("primary.lifecycle.phase", "Default:Created")));
         results = registry.search(q).getResults();
     
         assertEquals(1, results.size());
 
         // test and
         q = new Query(Artifact.class)
             .add(OpRestriction.and(
                   OpRestriction.eq("primary.lifecycle.phase", "Default:XXXX"),
                   OpRestriction.eq("primary.lifecycle.phase", "Default:Created")));
         results = registry.search(q).getResults();
     
         assertEquals(0, results.size());
         q = new Query(Artifact.class)
             .add(OpRestriction.and(
                   OpRestriction.eq("name", artifact.getName()),
                   OpRestriction.eq("primary.lifecycle.phase", "Default:Created")));
         results = registry.search(q).getResults();
     
         assertEquals(1, results.size());
              
         q = new Query(Artifact.class)
             .add(OpRestriction.in("primary.lifecycle.phase", Collections.emptyList()));
         results = registry.search(q).getResults();
     
         assertEquals(0, results.size());
         
         q = new Query(Artifact.class)
             .add(OpRestriction.in("primary.lifecycle", 
                                 Arrays.asList(new String[] { "Default", "notinthisone" })));
         results = registry.search(q).getResults();
     
         assertEquals(1, results.size());
     }
     
     public void testWorkspaceQueries() throws Exception {
         // Try out search!
         Query q = new Query(Workspace.class)
             .add(OpRestriction.like("name", "Default"));
         Set results = registry.search(q).getResults();
 
         assertEquals(1, results.size());
 
         Workspace w = (Workspace) results.iterator().next();
         assertEquals("Default Workspace", w.getName());
         
         q = new Query(Workspace.class)
             .add(OpRestriction.eq("name", "Default Workspace"));
         results = registry.search(q).getResults();
     
         assertEquals(1, results.size());
         
         q = new Query(Workspace.class).add(OpRestriction.in("name", Arrays.asList("Default Workspace", "Foo")));
         results = registry.search(q).getResults();
     
         assertEquals(1, results.size());
         
         w.newWorkspace("Test");
         
         q = new Query(Workspace.class)
             .add(OpRestriction.like("name", "Default"));
         results = registry.search(q).getResults();
         assertEquals(1, results.size());
         
         q = new Query(Workspace.class)
             .add(OpRestriction.like("name", "Test"));
         results = registry.search(q).getResults();
         assertEquals(1, results.size());
     }
     
     public void testWorkspaceSuggest() throws Exception {
         Workspace w = registry.newWorkspace("Test1");
         Workspace t2 = w.newWorkspace("Test2");
         t2.newWorkspace("Test3");
         
         SearchResults results = registry.suggest("Work", 10, "/bar", Workspace.class);
         assertEquals(1, results.getTotal());
         
         results = registry.suggest("Test2", 10, "/bar", Workspace.class);
         assertEquals(2, results.getTotal());
         
         results = registry.suggest("Test3", 10, "/bar", Workspace.class);
         assertEquals(1, results.getTotal());
         
         results = registry.suggest("1/Test2/T", 10, "/bar", Workspace.class);
         assertEquals(1, results.getTotal());
 
         results = registry.suggest("/Test1/Test2/T", 10, "/bar", Workspace.class);
         assertEquals(1, results.getTotal());
 
         results = registry.suggest("/test1/test2/t", 10, "/bar", Workspace.class);
         assertEquals(1, results.getTotal());
     }
     
     public void testQueryPropertyListing() throws Exception {
         PropertyDescriptor pd = new PropertyDescriptor();
         pd.setDescription("Contacts");
         pd.setProperty("contacts");
         
         Extension ext = registry.getExtension("userExtension");
         pd.setExtension(ext);
         
         typeManager.savePropertyDescriptor(pd);
         
         Map<String, String> properties = ext.getQueryProperties(pd);
         assertTrue(properties.size() > 1);
         String contacts = properties.get("contacts.name");
         assertEquals("Contacts - Name", contacts);
         
         // Link extension
         pd = new PropertyDescriptor();
         pd.setDescription("Link");
         pd.setProperty("link");
         
         Map<String,String> config = new HashMap<String, String>();
         config.put(LinkExtension.RECIPROCAL_CONFIG_KEY, "Reciprocal");
         pd.setConfiguration(config);
         
         ext = (LinkExtension) applicationContext.getBean("linkExtension");
         assertNotNull(ext);
         pd.setExtension(ext);
         
         typeManager.savePropertyDescriptor(pd);
         
         properties = ext.getQueryProperties(pd);
         assertEquals(2, properties.size());
         String property = properties.get("link");
         assertEquals("Link", property);
         
         property = properties.get("link.reciprocal");
         assertEquals("Reciprocal", property);
        
         // Test registry aggregation
         properties = registry.getQueryProperties();
         property = properties.get("link.reciprocal");
         assertEquals("Reciprocal", property);
         property = properties.get("contacts.name");
         assertEquals("Contacts - Name", property);
     }
     
     public void testExtensionQueries() throws Exception {
         Workspace root = registry.getWorkspaces().iterator().next();
         
         EntryResult r = root.newEntry("MyService", "1.0");
         assertNotNull(r);
         
         PropertyDescriptor pd = new PropertyDescriptor();
         pd.setExtension((Extension) applicationContext.getBean("userExtension"));
         pd.setDescription("Primary Contact");
         pd.setProperty("contact");
         
         typeManager.savePropertyDescriptor(pd);
         assertNotNull(pd.getId());
         
         pd = typeManager.getPropertyDescriptor(pd.getId());
         assertNotNull(pd);
         assertNotNull(pd.getExtension());
         
         Entry e = r.getEntry();
         assertNotNull(e);
         
         User user = getAdmin();
         e.setProperty("contact", user);
        registry.save(e);
         
         User c2 = (User) e.getProperty("contact");
         assertNotNull(c2);
         
         IdentifiableExtensionQueryBuilder qb = (IdentifiableExtensionQueryBuilder) applicationContext.getBean("userQueryBuilder");
         assertNotNull(qb);
         
         Collection<String> props = qb.getProperties();
         
         assertTrue(props.contains("contact.name"));
         assertTrue(props.contains("contact.email"));
         
         Query q = new Query(Entry.class).add(OpRestriction.eq("contact.name", user.getName()));
         
         SearchResults result = registry.search(q);
         
         assertEquals(1, result.getTotal());
         
         q = new Query(Entry.class).add(OpRestriction.like("contact.name", "ministra"));
         result = registry.search(q);
         assertEquals(1, result.getTotal());
         
         q = new Query(Entry.class).add(OpRestriction.like("contact.username", user.getUsername()));
         result = registry.search(q);
         assertEquals(1, result.getTotal());
     }
 }
