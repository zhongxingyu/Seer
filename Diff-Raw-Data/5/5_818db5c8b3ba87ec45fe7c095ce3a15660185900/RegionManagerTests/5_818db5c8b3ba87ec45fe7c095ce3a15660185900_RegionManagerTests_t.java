 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.osgi.region;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.isA;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 import static org.junit.Assert.assertEquals;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Test;
 import org.osgi.framework.ServiceFactory;
 import org.osgi.framework.launch.Framework;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventAdmin;
 import org.osgi.service.framework.CompositeBundle;
 import org.osgi.service.framework.CompositeBundleFactory;
 import org.osgi.service.framework.SurrogateBundle;
 
 import org.eclipse.virgo.kernel.osgi.region.RegionManager;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.eclipse.virgo.kernel.core.Shutdown;
 import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
 import org.eclipse.virgo.teststubs.osgi.framework.StubServiceRegistration;
 
 /**
  */
 @SuppressWarnings("deprecation")
 public class RegionManagerTests {
 
     @Test
     public void testStartAndStop() throws Exception {
         StubBundleContext bundleContext = new StubBundleContext();
         StubBundleContext surrogateBundleContext = new StubBundleContext();
         
         Framework user = createMock(Framework.class);
         SurrogateBundle surrogate = createMock(SurrogateBundle.class);
        ServiceFactory<EventLogger> serviceFactory = createMock(ServiceFactory.class);
         
         CompositeBundleFactory factory = createMock(CompositeBundleFactory.class);
         CompositeBundle bundle = createMock(CompositeBundle.class);
         
         expect(factory.installCompositeBundle(isA(Map.class), isA(String.class), isA(Map.class))).andReturn(bundle);
         expect(bundle.getCompositeFramework()).andReturn(user);
         bundle.start();
         expect(bundle.getSurrogateBundle()).andReturn(surrogate);
         
         expect(surrogate.getBundleContext()).andReturn(surrogateBundleContext);
         
         EventAdmin eventAdmin = createMock(EventAdmin.class);
         eventAdmin.sendEvent(isA(Event.class));
         
         Dictionary<String, String> properties = new Hashtable<String, String>();
         Configuration config = createMock(Configuration.class);
         expect(config.getProperties()).andReturn(properties);
         
         ConfigurationAdmin configAdmin = createMock(ConfigurationAdmin.class);
         expect(configAdmin.getConfiguration(isA(String.class))).andReturn(config);
         
         EventLogger eventLogger = createMock(EventLogger.class);       
         Shutdown shutdown = createMock(Shutdown.class);
         
         replay(factory, bundle, surrogate, eventAdmin, configAdmin, config);
         RegionManager manager = new RegionManager(bundleContext, factory, eventAdmin, serviceFactory, configAdmin, eventLogger, shutdown);
         manager.start();
         
        List<StubServiceRegistration<Object>> serviceRegistrations = bundleContext.getServiceRegistrations();
         assertEquals("Regions not registered", 2, serviceRegistrations.size());
         
         manager.stop();
         verify(factory, bundle, surrogate, eventAdmin, configAdmin, config);
         
     }
 }
