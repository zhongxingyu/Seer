 /*
  * SCI-Flex: Flexible Integration of SOA and CEP
  * Copyright (C) 2008, 2009  http://sci-flex.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.sciflex.plugins.synapse.esper.osgi;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 import org.sciflex.plugins.synapse.esper.service.SEPluginAdminService;
 
 /**
  * Activator for SCI-Flex Synapse Esper Plugin OSGi Bundle.
  * This implements the {@link BundleActivator} interface.
  */
 public class SEPluginActivator implements BundleActivator {
 
     /**
      * Logger associated with the Bundle Activator.
      */
     private static final Log log = LogFactory.getLog(SEPluginActivator.class);
 
     /**
      * Called when this bundle is started so the Framework can 
      * perform the bundle-specific activities necessary to start this bundle.
      *
      * @param context    the execution context associated with bundles.
      * @throws Exception throws an exception if anything goes wrong. If this 
      *                   method throws an exception, this bundle is marked as
      *                   stopped and the Framework will remove this bundle's
      *                   listeners, unregister all services registered by this 
      *                   bundle, and release all services used by this bundle.
      */
     public void start(BundleContext context) throws Exception {
         log.info("Starting SCI-Flex Synapse Esper Plugin");
         try {
             context.registerService(SEPluginAdminService.class.getName(), new SEPluginAdminService(), null);
         } catch (Exception e) {
             log.error("Error initializing SCI-Flex Synapse Esper Plugin", e);
             throw new Exception("initializationError", e);
         }
     }
 
     /**
      * Called when this bundle is stopped so the Framework can
      * perform the bundle-specific activities necessary to stop the bundle.
      *
     * @param bundleContext    the excution context associated with bundles.
      * @throws Exception throws an exception if anything goes wrong. If this 
      *                   method throws an exception, this bundle is marked as
      *                   stopped and the Framework will remove this bundle's
      *                   listeners, unregister all services registered by this 
      *                   bundle, and release all services used by this bundle.
      */
     public void stop(BundleContext bundleContext) throws Exception {
         log.info("Stopping SCI-Flex Synapse Esper Plugin");
     }
 }
