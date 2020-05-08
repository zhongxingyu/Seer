 /**
  * <copyright>
  *
  * Copyright (c) 2009, 2010 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: ReferersTest.java,v 1.4 2011/08/26 07:58:32 mtaal Exp $
  */
 package org.eclipse.emf.texo.server.test.issues;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import junit.framework.Assert;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.texo.component.ComponentProvider;
 import org.eclipse.emf.texo.json.JSONTexoResource;
 import org.eclipse.emf.texo.server.store.EntityManagerProvider;
 import org.eclipse.emf.texo.server.test.store.TexoResourceTest;
 import org.eclipse.emf.texo.store.TexoResource;
 import org.eclipse.emf.texo.test.emfmodel.bz403743.Bz403743Factory;
 import org.eclipse.emf.texo.test.emfmodel.bz403743.Test403743_Main;
 import org.eclipse.emf.texo.test.emfmodel.bz403743.impl.Bz403743PackageImpl;
 import org.eclipse.emf.texo.test.model.issues.bz403743.Bz403743ModelPackage;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test case for issue: https://bugs.eclipse.org/bugs/show_bug.cgi?id=401968
  * 
  * Re-uses the test model from 403743.
  * 
  * @author <a href="mailto:mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.4 $
  */
 public class Bz401968JSONResourceTest extends TexoResourceTest {
 
   public Bz401968JSONResourceTest() {
     super("bz403743");
     Bz403743PackageImpl.init();
     Bz403743ModelPackage.initialize();
   }
 
   @Before
   @Override
   public void setUp() throws Exception {
     super.setUp();
     doServerSetUp();
   }
 
   @Override
   @After
   public void tearDown() throws Exception {
     super.tearDown();
     doServerTearDown();
   }
 
   @Test
   public void testResource() {
 
     // Persist
     {
       final Bz403743Factory factory = Bz403743Factory.eINSTANCE;
       Test403743_Main root = factory.createTest403743_Main();
       root.setName("1");
       final TexoResource resource = createResource();
       resource.getContents().add(root);
       try {
         resource.save(Collections.emptyMap());
       } catch (IOException e) {
         throw new IllegalStateException(e);
       }
     }
 
     try {
       // read the object using 2 resources
       final TexoResource resource1 = createResource(getBaseURL() + "?types=bz403743_Test403743_Main");
       resource1.load(Collections.emptyMap());
       Test403743_Main o1 = (Test403743_Main) resource1.getContents().get(0);
       Assert.assertTrue(o1.getName().equals("1"));
 
       // use a different url then resource1
       final TexoResource resource2 = createResource(getBaseURL());
       Assert.assertTrue(resource1 != resource2);
       resource2.query("select e from bz403743_Test403743_Main e", new HashMap<String, Object>(), 0, 100);
       Test403743_Main o2 = (Test403743_Main) resource2.getContents().get(0);
       Assert.assertTrue(o2.getName().equals("1"));
 
       // different instances
       Assert.assertTrue(o1 != o2);
       Assert.assertTrue(o1.getDb_Id().equals(o2.getDb_Id()));
 
       o2.setName("2");
       resource2.save(Collections.emptyMap());
       Assert.assertTrue(o1.getName().equals("1"));
 
       // refresh the resource 1
       resource1.refresh(o1);
 
       // name should have changed
       Assert.assertTrue(o1.getName().equals("2"));
 
       // now change in a separate entitymanager directly
       changeObjectDirectly();
 
       // check both resources, should both be unchanged
       Assert.assertTrue(o2.getName().equals("2"));
       Assert.assertTrue(o1.getName().equals("2"));
 
       // refresh the resources
       resource1.refresh(o1);
       Assert.assertTrue(o1.getName().equals("3"));
       Assert.assertTrue(o2.getName().equals("2"));
       resource2.refresh(o1);
       Assert.assertTrue(o2.getName().equals("3"));
 
      // finally check using a new resource
      final TexoResource resource3 = createResource(getBaseURL() + "?test=test");
      Assert.assertTrue(resource3 != resource2);
      resource3.query("select e from bz403743_Test403743_Main e", new HashMap<String, Object>(), 0, 100);
      Test403743_Main o3 = (Test403743_Main) resource3.getContents().get(0);
      Assert.assertTrue(o3.getName().equals("3"));
      Assert.assertTrue(o3 != o1);
     } catch (IOException e) {
       throw new IllegalStateException(e);
     }
   }
 
   private void changeObjectDirectly() {
     final EntityManager em = EntityManagerProvider.getInstance().createEntityManager();
     em.getTransaction().begin();
     @SuppressWarnings("unchecked")
     final List<Object> data = em.createQuery("select e from bz403743_Test403743_Main e").getResultList();
     ((org.eclipse.emf.texo.test.model.issues.bz403743.Test403743_Main) data.get(0)).setName("3");
     em.getTransaction().commit();
   }
 
   @Override
   protected TexoResource createResource(String uriString) {
     final ResourceSetImpl resourceSet = new ResourceSetImpl();
     resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("http", new JSONTexoResourceFactory());
     return (TexoResource) resourceSet.createResource(URI.createURI(uriString));
   }
 
   private static class JSONTexoResourceFactory implements Resource.Factory {
 
     public Resource createResource(URI uri) {
       final JSONTexoResource resource = ComponentProvider.getInstance().newInstance(JSONTexoResource.class);
       resource.setURI(uri);
       return resource;
     }
   }
 }
