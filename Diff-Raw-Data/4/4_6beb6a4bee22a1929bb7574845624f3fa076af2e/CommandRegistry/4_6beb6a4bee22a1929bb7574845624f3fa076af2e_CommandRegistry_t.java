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
 
 package org.eclipse.virgo.kernel.shell.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 
 /**
  * A <code>CommandRegistry</code> maintains a list of {@link CommandDescriptor CommandDescriptors} based on the contents
  * of the OSGi service registry. Descriptors are created using a {@link CommandResolver}.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Thread-safe.
  * 
  */
 public final class CommandRegistry {
 
     private final CommandResolver commandResolver;
 
     private final BundleContext bundleContext;
 
     private final List<CommandDescriptor> commandDescriptors = new ArrayList<CommandDescriptor>();
 
     private final Map<ServiceReference<?>, List<CommandDescriptor>> commandDescriptorsByService = new HashMap<ServiceReference<?>, List<CommandDescriptor>>();
 
     private final Object monitor = new Object();
 
     private final CommandRegistryServiceListener commandRegistryServiceListener = new CommandRegistryServiceListener();
 
     /**
      * @param commandResolver
      * @param bundleContext 
      */
     public CommandRegistry(CommandResolver commandResolver, BundleContext bundleContext) {
         this.commandResolver = commandResolver;
         this.bundleContext = bundleContext;
     }
 
     void initialize() {
         // TODO Limit with a filter
         this.bundleContext.addServiceListener(this.commandRegistryServiceListener);
         try {
             // TODO Limit with a filter
             ServiceReference<?>[] serviceReferences = this.bundleContext.getServiceReferences((String)null, null);
             if (serviceReferences != null) {
                 for (ServiceReference<?> serviceReference : serviceReferences) {
                     serviceRegistered(serviceReference);
                 }
             }
         } catch (InvalidSyntaxException e) {
             throw new RuntimeException("Unexpected InvalidSyntaxException", e);
         }
     }
     
     public List<CommandDescriptor> getCommandDescriptors() {
         synchronized(this.monitor) { 
             return new ArrayList<CommandDescriptor>(this.commandDescriptors);
         }
     }
     
 
     private void serviceRegistered(ServiceReference<?> serviceReference) {
         Object service = bundleContext.getService(serviceReference);
         if (service != null) {
             List<CommandDescriptor> commands = commandResolver.resolveCommands(serviceReference, service);
             if (!commands.isEmpty()) {
                 synchronized (this.monitor) {
                     this.commandDescriptors.addAll(commands);
                     this.commandDescriptorsByService.put(serviceReference, commands);
                 }
             }
         }
     }
 
     private void serviceUnregistering(ServiceReference<?> serviceReference) {
         synchronized (this.monitor) {
             List<CommandDescriptor> commandDescriptorsForService = this.commandDescriptorsByService.remove(serviceReference);
            if (commandDescriptorsForService != null) {
                this.commandDescriptors.removeAll(commandDescriptorsForService);
            }
         }
     }
 
     private final class CommandRegistryServiceListener implements ServiceListener {
 
         /**
          * {@inheritDoc}
          */
         public void serviceChanged(ServiceEvent event) {
             if (ServiceEvent.REGISTERED == event.getType()) {
                 serviceRegistered(event.getServiceReference());
             } else if (ServiceEvent.UNREGISTERING == event.getType()) {
                 serviceUnregistering(event.getServiceReference());
             }
         }
     }
 }
