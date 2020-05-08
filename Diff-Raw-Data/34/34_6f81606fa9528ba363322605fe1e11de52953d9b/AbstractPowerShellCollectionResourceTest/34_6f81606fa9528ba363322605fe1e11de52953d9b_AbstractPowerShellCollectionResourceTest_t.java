 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.powershell.resource;
 
 import java.net.URI;
 import java.text.MessageFormat;
 import java.util.List;
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.BaseResource;
 import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
 import com.redhat.rhevm.api.common.resource.AbstractUpdatableResource;
 import com.redhat.rhevm.api.common.util.ReflectionHelper;
 
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.runner.RunWith;
 
 import static org.easymock.classextension.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.isA;
 
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.mockStatic;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 import static org.powermock.api.easymock.PowerMock.verifyAll;
 
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( { PowerShellCmd.class })
 public abstract class AbstractPowerShellCollectionResourceTest<R extends BaseResource,
                                                                U extends AbstractUpdatableResource<R>,
                                                                T extends AbstractPowerShellCollectionResource<R, U>>
     extends Assert {
 
     private static final String URI_ROOT = "http://localhost:8099";
 
     protected static final String[] NAMES = {"sedna", "eris", "orcus"};
     protected static final String NEW_NAME = "ceres";
     private static final String SELECT_COMMAND = "select-{0}";
     private static final String SELECT_RETURN =
         "{0}id: " + "sedna".hashCode() + " \n name: sedna {1}\n\n" +
         "{0}id: " + "eris".hashCode() + " \n name: eris {1}\n\n" +
         "{0}id: " + "orcus".hashCode() + " \n name: orcus {1}";
 
     private static final String ADD_COMMAND = "add-{0} -name ceres ";
     private static final String ADD_RETURN =
         "{0}id: " + "ceres".hashCode() + " \n name: ceres {1}\n\n";
 
     private static final String REMOVE_COMMAND = "remove-{0} -{0}id " + "eris".hashCode();
 
     protected static final String[] NOTHING = null;
 
     protected T resource;
     protected U updatable;
     protected String collectionName;
     protected String individualName;
     protected Executor executor;
 
     protected AbstractPowerShellCollectionResourceTest(U updatable, String collectionName, String individualName) {
         this.updatable = updatable;
         this.collectionName = collectionName;
         this.individualName = individualName;
     }
 
     @Before
     public void setUp() {
         executor = new ControllableExecutor();
         resource = getResource();
         resource.setExecutor(executor);
     }
 
     @After
     public void tearDown() {
         verifyAll();
     }
 
     protected UriInfo setUpResourceExpectations(String command, String ret, String ... names) throws Exception {
         return setUpResourceExpectations(command, ret, 0, names);
     }
 
     protected UriInfo setUpResourceExpectations(String command, String ret, int baseUris, String ... names) throws Exception {
         return setUpResourceExpectations(asArray(command), asArray(ret), baseUris, names);
     }
 
     protected UriInfo setUpResourceExpectations(String[] commands, String[] rets, int baseUris, String ... names) throws Exception {
         if (commands != null) {
             mockStatic(PowerShellCmd.class);
             for (int i = 0 ; i < Math.min(commands.length, rets.length) ; i++) {
                 expect(PowerShellCmd.runCommand(commands[i])).andReturn(rets[i]);
             }
         }
         UriInfo uriInfo = createMock(UriInfo.class);
         for (String name : names) {
             UriBuilder uriBuilder = createMock(UriBuilder.class);
             expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder);
             expect(uriBuilder.path(Integer.toString(name.hashCode()))).andReturn(uriBuilder);
             expect(uriBuilder.build()).andReturn(new URI(URI_ROOT + collectionName + name.hashCode())).anyTimes();
         }
         for (int i = 0 ; i < baseUris ; i++) {
             UriBuilder uriBuilder = createMock(UriBuilder.class);
             expect(uriInfo.getBaseUriBuilder()).andReturn(uriBuilder);
             expect(uriBuilder.clone()).andReturn(uriBuilder).anyTimes();
             expect(uriBuilder.path(isA(String.class))).andReturn(uriBuilder).anyTimes();
             expect(uriBuilder.build()).andReturn(new URI(URI_ROOT + "/foo")).anyTimes();
         }
         replayAll();
 
         return uriInfo;
     }
 
     protected String getSelectCommand() {
         return MessageFormat.format(SELECT_COMMAND, individualName);
     }
 
     protected String getSelectReturn() {
         return getSelectReturn("");
     }
 
     protected String getSelectReturn(String epilog) {
         return MessageFormat.format(SELECT_RETURN, individualName, epilog);
     }
 
     protected String getAddCommand() {
         return MessageFormat.format(ADD_COMMAND, individualName);
     }
 
     protected String getAddReturn() {
         return getAddReturn("");
     }
 
     protected String getAddReturn(String epilog) {
         return MessageFormat.format(ADD_RETURN, individualName, epilog);
     }
 
     protected String getRemoveCommand() {
         return MessageFormat.format(REMOVE_COMMAND, individualName);
     }
 
     protected R getModel(String name) {
         R model = ReflectionHelper.newModel(updatable);
         model.setId(Integer.toString(name.hashCode()));
         model.setName(name);
         populateModel(model);
         return model;
     }
 
     protected void verifyResponse(Response r, String name) {
         assertEquals("unexpected status", 201, r.getStatus());
         Object entity = r.getEntity();
         assertTrue("expect response entity", entity instanceof BaseResource);
         BaseResource model = (BaseResource)entity;
         assertEquals(model.getId(), Integer.toString(name.hashCode()));
         assertEquals(model.getName(), name);
         assertNotNull(r.getMetadata().get("Location"));
         assertTrue("expected location header",
                    r.getMetadata().get("Location").size() > 0);
         assertEquals("unexpected location header",
                      URI_ROOT + collectionName + name.hashCode(),
                      r.getMetadata().get("Location").get(0).toString());
     }
 
     protected void verifyCollection(List<R> collection, String ... names) {
         assertNotNull(collection);
         assertEquals("unexpected collection size", collection.size(), names.length);
         for (String name: names) {
             R model = collection.remove(0);
             assertEquals(model.getId(), Integer.toString(name.hashCode()));
             assertEquals(model.getName(), name);
         }
     }
 
     protected void verifyResource(AbstractActionableResource<R> resource, String name) {
         assertNotNull(resource);
         assertEquals(resource.getId(), Integer.toString(name.hashCode()));
         assertSame(resource.getExecutor(), executor);
     }
 
     protected static String[] asArray(String s) {
         return new String[] { s };
     }
 
     protected abstract T getResource();
 
     protected abstract void populateModel(R model);
 }
 
