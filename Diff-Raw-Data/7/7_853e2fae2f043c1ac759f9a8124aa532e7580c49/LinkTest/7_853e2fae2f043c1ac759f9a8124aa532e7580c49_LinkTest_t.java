 package org.mule.galaxy.impl;
 
 
 import java.io.ByteArrayInputStream;
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.Entry;
 import org.mule.galaxy.EntryResult;
 import org.mule.galaxy.EntryVersion;
 import org.mule.galaxy.Link;
 import org.mule.galaxy.Links;
 import org.mule.galaxy.PropertyInfo;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.impl.link.LinkExtension;
 import org.mule.galaxy.query.OpRestriction;
 import org.mule.galaxy.query.Query;
 import org.mule.galaxy.query.SearchResults;
 import org.mule.galaxy.test.AbstractGalaxyTest;
 
 public class LinkTest extends AbstractGalaxyTest {
     
     public void testLinkTypes() throws Exception {
         typeManager.getPropertyDescriptorByName(LinkExtension.DEPENDS);
     }
     
     public void testWsdlDependencies() throws Exception {
         Collection<Workspace> workspaces = registry.getWorkspaces();
         assertEquals(1, workspaces.size());
         Workspace workspace = workspaces.iterator().next();
         
         EntryResult schema = workspace.createArtifact("application/xml", 
                                                          "hello.xsd", 
                                                          "0.1", 
                                                          getResourceAsStream("/wsdl/imports/hello.xsd"));
          
         Links links = (Links) schema.getEntryVersion().getProperty(LinkExtension.DEPENDS);
         Collection<Link> deps = links.getLinks();
         assertEquals(0, deps.size());
         
         EntryResult portType = workspace.createArtifact("application/wsdl+xml", 
                                                         "hello-portType.wsdl", 
                                                         "0.1", 
                                                         getResourceAsStream("/wsdl/imports/hello-portType.wsdl"));
         links = (Links) portType.getEntryVersion().getProperty(LinkExtension.DEPENDS);
         deps = links.getLinks();
         assertEquals(1, deps.size());
         
         Link dep = deps.iterator().next();
         Entry schemaEntry = schema.getEntry();
         
         assertEquals("hello.xsd", dep.getLinkedToPath());
         assertNotNull(dep.getLinkedTo());
         assertEquals(schemaEntry.getId(), dep.getLinkedTo().getId());
 
         // figure out if the we can figure out which things link *to* the schema. 
         // We should find the portType
         links = (Links) schemaEntry.getProperty(LinkExtension.DEPENDS);
         Collection<Link> reciprocal = links.getReciprocalLinks();
         assertEquals(1, reciprocal.size());
         Link l = reciprocal.iterator().next();
         Artifact parent = (Artifact) l.getItem().getParent();
         assertEquals(portType.getEntry().getId(), parent.getId());
         assertTrue(dep.isAutoDetected());
         
         EntryResult svcWsdl = workspace.createArtifact("application/wsdl+xml", 
                                                           "hello.wsdl", 
                                                           "0.1", 
                                                           getResourceAsStream("/wsdl/imports/hello.wsdl"));
         links = (Links) svcWsdl.getEntryVersion().getProperty(LinkExtension.DEPENDS);
         deps = links.getLinks();
         assertEquals(1, deps.size());
         
         dep = deps.iterator().next();
         assertEquals(portType.getEntry().getId(), dep.getLinkedTo().getId());
         assertTrue(dep.isAutoDetected());
         
         // yeah, this doesn't make sense dependency-wise, but we're just seeing if user specified dependencies work
         EntryVersion schemaV = schema.getEntryVersion();
         links = (Links) schemaV.getProperty(LinkExtension.DEPENDS);
         
         links.addLinks(new Link(schemaV, svcWsdl.getEntry(), null, false));
         
         deps = links.getLinks();
         assertEquals(1, deps.size());
         dep = deps.iterator().next();
         assertEquals(svcWsdl.getEntry().getId(), dep.getLinkedTo().getId());
         assertFalse(dep.isAutoDetected());
         
         links.removeLinks(deps.iterator().next());
         deps = links.getLinks();
         assertEquals(0, deps.size());
     }
     
     public void testSchemaDependencies() throws Exception {
 
         Collection<Workspace> workspaces = registry.getWorkspaces();
         assertEquals(1, workspaces.size());
         Workspace workspace = workspaces.iterator().next();
         
         EntryResult schema = workspace.createArtifact(
                                                         "application/xml", 
                                                         "hello.xsd", 
                                                         "0.1", 
                                                         getResourceAsStream("/schema/hello.xsd"));
         
         Links links = (Links) schema.getEntryVersion().getProperty(LinkExtension.DEPENDS);
         Collection<Link> deps = links.getLinks();
         assertNotNull(deps);
         assertEquals(0, deps.size());
         
         EntryResult schema2 = workspace.createArtifact("application/xml", 
                                                           "hello-import.xsd", 
                                                           "0.1", 
                                                           getResourceAsStream("/schema/hello-import.xsd"));
         
         EntryVersion s2version = schema2.getEntryVersion();
         
         // reload so the cache is fresh
         s2version = (EntryVersion) registry.getItemById(s2version.getId());
         
         links = (Links) s2version.getProperty(LinkExtension.DEPENDS);
         deps = links.getLinks();
         assertEquals(1, deps.size());
         Link dep = deps.iterator().next();
         assertEquals(schema.getEntry().getId(), dep.getLinkedTo().getId());
         assertTrue(dep.isAutoDetected());
     }
     
     public void testAbsoluteSchemaDependencies() throws Exception {
 
         Collection<Workspace> workspaces = registry.getWorkspaces();
         assertEquals(1, workspaces.size());
         Workspace workspace = workspaces.iterator().next();
         
         EntryResult schema = workspace.createArtifact("application/xml", 
                                                       "hello-import-absolute.xsd", 
                                                       "0.1", 
                                                       getResourceAsStream("/schema/hello-import-absolute.xsd"));

         Links links = (Links) schema.getEntryVersion().getProperty(LinkExtension.DEPENDS);
         Collection<Link> deps = links.getLinks();
         assertNotNull(deps);
        assertEquals(2, deps.size());
         
         Link next = deps.iterator().next();
         assertNull(next.getLinkedTo());
         
     }
     
     public void testMissingWsdlDependencies() throws Exception {
 
         Collection<Workspace> workspaces = registry.getWorkspaces();
         assertEquals(1, workspaces.size());
         Workspace workspace = workspaces.iterator().next();
 
         EntryResult svcWsdl = workspace.createArtifact("application/wsdl+xml", 
                                                        "hello.wsdl", 
                                                        "0.1", 
                                                        getResourceAsStream("/wsdl/imports/hello-missing.wsdl"));
         Links links = (Links) svcWsdl.getEntryVersion().getProperty(LinkExtension.DEPENDS);
         Collection<Link> deps = links.getLinks();
         assertEquals(1, deps.size());
         
         Link link = deps.iterator().next();
         assertNull(link.getLinkedTo());
         assertNotNull(link.getLinkedToPath());
         
     }
     
     public void testConflicts() throws Exception {
         Collection<Workspace> workspaces = registry.getWorkspaces();
         assertEquals(1, workspaces.size());
         Workspace workspace = workspaces.iterator().next();
         
         EntryResult schema = workspace.createArtifact("application/xml", 
                                                       "hello.xsd", 
                                                       "0.1", 
                                                       getResourceAsStream("/wsdl/imports/hello.xsd"));
          
         Links links = (Links) schema.getEntryVersion().getProperty(LinkExtension.CONFLICTS);
         Collection<Link> deps = links.getLinks();
         assertEquals(0, deps.size());
         
         deps = links.getReciprocalLinks();
         assertEquals(0, deps.size());
         
         EntryResult portType = workspace.createArtifact("application/wsdl+xml", 
                                                         "hello-portType.wsdl", 
                                                         "0.1", 
                                                         getResourceAsStream("/wsdl/imports/hello-portType.wsdl"));
         
         EntryVersion version = portType.getEntryVersion();
         
         version.setProperty(LinkExtension.CONFLICTS, Arrays.asList(new Link(version, schema.getEntry(), null, false)));
         
         Links ptLinks = (Links) version.getProperty(LinkExtension.CONFLICTS);
         assertNotNull(ptLinks);
         
         deps = ptLinks.getLinks();
         assertEquals(1, deps.size());
         
         deps = ptLinks.getReciprocalLinks();
         assertEquals(0, deps.size());
         
         links = (Links) schema.getEntry().getProperty(LinkExtension.CONFLICTS);
         
         deps = links.getLinks();
         assertEquals(0, deps.size());
         
         deps = links.getReciprocalLinks();
         assertEquals(1, deps.size());
         
         boolean conflicts = false;
         boolean supercedes = false;
         Collection<PropertyInfo> properties = schema.getEntry().getProperties();
         for (PropertyInfo pi : properties) {
             if (pi.getName().equals(LinkExtension.CONFLICTS)) {
                 conflicts = true;
             } else if (pi.getName().equals(LinkExtension.SUPERCEDES)) {
                 supercedes = true;
             }
         }
         
         assertTrue(conflicts);
         assertFalse(supercedes);
         
         version.setProperty(LinkExtension.CONFLICTS, null);
         
         ptLinks = (Links) version.getProperty(LinkExtension.CONFLICTS);
         assertNotNull(ptLinks);
         
         deps = ptLinks.getLinks();
         assertEquals(0, deps.size());
         
         deps = ptLinks.getReciprocalLinks();
         assertEquals(0, deps.size());
     }
     
     public void testDelete() throws Exception{
         Collection<Workspace> workspaces = registry.getWorkspaces();
         assertEquals(1, workspaces.size());
         Workspace workspace = workspaces.iterator().next();
         
         EntryResult r1 = workspace.createArtifact("application/xml", 
                                                   "a1.xml", 
                                                   "0.1", 
                                                   getResourceAsStream("/mule/hello-config.xml"));
         Entry a1 = r1.getEntry();
 
         EntryResult r2 = workspace.createArtifact("application/xml", 
                                                   "a2.xml", 
                                                   "0.1", 
                                                   getResourceAsStream("/mule/hello-config.xml"));
         EntryVersion v2 = r2.getEntryVersion();
         
         v2.setProperty(LinkExtension.CONFLICTS, Arrays.asList(new Link(v2, a1, null, false)));
         
         a1.delete();
         
         Links ptLinks = (Links) v2.getProperty(LinkExtension.CONFLICTS);
         assertNotNull(ptLinks);
         
         Collection<Link> deps = ptLinks.getLinks();
         assertEquals(0, deps.size());
         
         deps = ptLinks.getReciprocalLinks();
         assertEquals(0, deps.size());
     }
     
     public void testQuery() throws Exception{
         Collection<Workspace> workspaces = registry.getWorkspaces();
         assertEquals(1, workspaces.size());
         Workspace workspace = workspaces.iterator().next();
         
         EntryResult r1 = workspace.newEntry("a1", "0.1");
         Entry a1 = r1.getEntry();
 
         EntryResult r2 = workspace.newEntry("a2", "0.1");
         EntryVersion v2 = r2.getEntryVersion();
         
         v2.setProperty(LinkExtension.CONFLICTS, Arrays.asList(new Link(v2, a1, null, false)));
         
         // test forward link
 //        Query query = new Query(Entry.class).add(OpRestriction.eq(LinkExtension.CONFLICTS, "a2"));
 //        
 //        SearchResults results = registry.search(query);
 //        assertEquals(1, results.getTotal());
 //        
         // test reciprocal
 //        query = new Query().add(OpRestriction.eq(LinkExtension.CONFLICTS + ".reciprocal", "a1"));
 //        
 //        results = registry.search(query);
 //        assertEquals(1, results.getTotal());
         
         // test IN
 //        query = new Query(Entry.class).add(OpRestriction.in(LinkExtension.CONFLICTS, Arrays.asList("a2", "a1", "foo.xml")));
 //        results = registry.search(query);
 //        assertEquals(1, results.getTotal());
 
         // test reciprocal IN
 //        query = new Query().add(OpRestriction.in(LinkExtension.CONFLICTS + ".reciprocal", Arrays.asList("a2", "a1", "foo.xml")));
 //        results = registry.search(query);
 //        assertEquals(1, results.getTotal());
     }
 }
