 package org.mule.galaxy.impl;
 
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
import org.mule.galaxy.Artifact;
 import org.mule.galaxy.ArtifactResult;
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.Link;
 import org.mule.galaxy.LinkType;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.test.AbstractGalaxyTest;
 
 public class LinkTest extends AbstractGalaxyTest {
     
     public void testLinkTypes() throws Exception {
 	List<LinkType> all = linkTypeManager.listAll();
 	assertTrue(all.size() > 0);
 	
 	LinkType type = linkTypeManager.get(LinkType.DEPENDS);
 	
 	assertNotNull(type);
     }
     public void testWsdlDependencies() throws Exception {
 
         Collection<Workspace> workspaces = registry.getWorkspaces();
         assertEquals(1, workspaces.size());
         Workspace workspace = workspaces.iterator().next();
         
         ArtifactResult schema = registry.createArtifact(workspace, 
                                                         "application/xml", 
                                                         "hello.xsd", 
                                                         "0.1", 
                                                         getResourceAsStream("/wsdl/imports/hello.xsd"), 
                                                         getAdmin());
         
         Set<Link> deps = schema.getArtifactVersion().getLinks();
         assertEquals(0, deps.size());
         
         ArtifactResult portType = registry.createArtifact(workspace, 
                                                           "application/wsdl+xml", 
                                                           "hello-portType.wsdl", 
                                                           "0.1", 
                                                           getResourceAsStream("/wsdl/imports/hello-portType.wsdl"), 
                                                           getAdmin());
         deps = portType.getArtifactVersion().getLinks();
         assertEquals(1, deps.size());
         
         Link dep = deps.iterator().next();
         assertEquals(schema.getArtifact().getId(), dep.getItem().getId());
         assertNotNull(dep.getType());
         assertEquals("Depends On", dep.getType().getRelationship());
         
         // figure out if the we can figure out which things link *to* the schema. 
         // We should find the portType
         Set<Link> reciprocal = registry.getReciprocalLinks(schema.getArtifact());
         assertEquals(1, reciprocal.size());
         Link l = reciprocal.iterator().next();
        Artifact parent = (Artifact) l.getParent().getParent();
        assertEquals(portType.getArtifact().getId(), parent.getId());
         assertTrue(dep.isAutoDetected());
         
         ArtifactResult svcWsdl = registry.createArtifact(workspace, 
                                                          "application/wsdl+xml", 
                                                          "hello.wsdl", 
                                                          "0.1", 
                                                          getResourceAsStream("/wsdl/imports/hello.wsdl"), 
                                                          getAdmin());
         deps = svcWsdl.getArtifactVersion().getLinks();
         assertEquals(1, deps.size());
         
         dep = deps.iterator().next();
         assertEquals(portType.getArtifact().getId(), dep.getItem().getId());
         assertTrue(dep.isAutoDetected());
         
         // yeah, this doesn't make sense dependency-wise, but we're just seeing if user specified dependencies work
         ArtifactVersion schemaV = schema.getArtifactVersion();
         registry.addLinks(schemaV, linkTypeManager.get(LinkType.DEPENDS), svcWsdl.getArtifact());
         
         deps = schemaV.getLinks();
         assertEquals(1, deps.size());
         dep = deps.iterator().next();
         assertEquals(svcWsdl.getArtifact().getId(), dep.getItem().getId());
         assertFalse(dep.isAutoDetected());
         
         registry.removeLinks(deps.iterator().next());
         deps = schemaV.getLinks();
         assertEquals(0, deps.size());
     }
     
     public void testSchemaDependencies() throws Exception {
 
         Collection<Workspace> workspaces = registry.getWorkspaces();
         assertEquals(1, workspaces.size());
         Workspace workspace = workspaces.iterator().next();
         
         ArtifactResult schema = registry.createArtifact(workspace, 
                                                         "application/xml", 
                                                         "hello.xsd", 
                                                         "0.1", 
                                                         getResourceAsStream("/schema/hello.xsd"), 
                                                         getAdmin());
         
         Set<Link> deps = schema.getArtifactVersion().getLinks();
         assertEquals(0, deps.size());
         
         ArtifactResult schema2 = registry.createArtifact(workspace, 
                                                          "application/xml", 
                                                          "hello-import.xsd", 
                                                          "0.1", 
                                                          getResourceAsStream("/schema/hello-import.xsd"), 
                                                          getAdmin());
          
         deps = schema2.getArtifactVersion().getLinks();
         assertEquals(1, deps.size());
         Link dep = deps.iterator().next();
         assertEquals(schema.getArtifact().getId(), dep.getItem().getId());
         assertTrue(dep.isAutoDetected());
     }
     
     public void testMissingWsdlDependencies() throws Exception {
 
         Collection<Workspace> workspaces = registry.getWorkspaces();
         assertEquals(1, workspaces.size());
         Workspace workspace = workspaces.iterator().next();
 
         ArtifactResult svcWsdl = registry.createArtifact(workspace, 
                                                          "application/wsdl+xml", 
                                                          "hello.wsdl", 
                                                          "0.1", 
                                                          getResourceAsStream("/wsdl/imports/hello-missing.wsdl"), 
                                                          getAdmin());
         Set<Link> deps = svcWsdl.getArtifactVersion().getLinks();
         assertEquals(0, deps.size());
     }
 
 }
