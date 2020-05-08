 /*
  * Copyright (C) 2012  Pauli Kauppinen
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see <http://www.gnu.org/licenses/>.
  */
 package org.javnce.upnp.server;
 
 import java.io.IOException;
 import org.fourthline.cling.UpnpService;
 import org.fourthline.cling.UpnpServiceImpl;
 import org.fourthline.cling.binding.*;
 import org.fourthline.cling.binding.annotations.*;
 import org.fourthline.cling.model.*;
 import org.fourthline.cling.model.meta.*;
 import org.fourthline.cling.model.types.*;
 import org.javnce.eventing.EventLoop;
 
 /**
  * The Class UpnPServer provides Javnce UPnP device and service.
  */
 class UpnPServer extends Thread {
 
     /**
      * The UPnP service.
      */
     private final UpnpService upnpService;
     /**
      * The server name.
      */
     private final String name;
     /**
      * The server port.
      */
     private final int port;
 
     /**
      * Instantiates a new UPnP server.
      *
      * @param name the name
      * @param port the port
      */
     UpnPServer(String name, int port) {

         this.name = name;
         this.port = port;
        upnpService = new UpnpServiceImpl();
 
     }
 
     /* (non-Javadoc)
      * @see java.lang.Thread#run()
      */
     @Override
     public void run() {
 
         try {

             // Add the bound local device to the registry
             upnpService.getRegistry().addDevice(createDevice());
         } catch (Throwable ex) {
             EventLoop.fatalError(this, ex);
         }
 
     }
 
     /**
      * Creates the UPnP device.
      *
      * @return the local device
      * @throws ValidationException the validation exception
      * @throws LocalServiceBindingException the local service binding exception
      * @throws IOException Signals that an I/O exception has occurred.
      */
     private LocalDevice createDevice() throws ValidationException, LocalServiceBindingException, IOException {
 
         DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("JaVNCe"));
 
         DeviceType type = new UDADeviceType("JaVNCe", 1);
 
         DeviceDetails details = new DeviceDetails(name,
                 new ManufacturerDetails(""),
                 new ModelDetails("JaVNCe", "Java VNC remote Application", "0.0.0.1"));
         Icon icon = null;
 
         LocalService<VncService> service = new AnnotationLocalServiceBinder().read(VncService.class);
         service.setManager(new DefaultServiceManager(service, VncService.class));
        service.getManager().getImplementation().setPort(port);
         return new LocalDevice(identity, type, details, icon, service);
     }
 
     /**
      * Shutdown.
      */
     void shutdown() {
         upnpService.shutdown();
     }
 }
