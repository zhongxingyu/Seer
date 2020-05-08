 /*******************************************************************************
  * Copyright (c) 2013 Archomeda.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Archomeda - initial API and implementation
  ******************************************************************************/
 package archomeda.upnp;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashSet;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.teleal.cling.UpnpService;
 import org.teleal.cling.UpnpServiceImpl;
 import org.teleal.cling.controlpoint.ControlPoint;
 import org.teleal.cling.model.action.ActionInvocation;
 import org.teleal.cling.model.message.UpnpResponse;
 import org.teleal.cling.model.message.header.UDAServiceTypeHeader;
 import org.teleal.cling.model.meta.RemoteDevice;
 import org.teleal.cling.model.meta.RemoteService;
 import org.teleal.cling.model.types.UDAServiceType;
 import org.teleal.cling.registry.DefaultRegistryListener;
 import org.teleal.cling.registry.Registry;
 import org.teleal.cling.support.igd.callback.GetExternalIP;
 import org.teleal.cling.support.igd.callback.PortMappingAdd;
 import org.teleal.cling.support.igd.callback.PortMappingDelete;
 import org.teleal.cling.support.model.PortMapping;
 
 /**
  * The main UPnP API of Minecraft UPnP. Contains all supported UPnP logic in this mod.
  * 
  * @author Archomeda
  */
 public class Upnp {
     private static UpnpService upnpService;
     private static RemoteDevice device;
     private static boolean deviceTimeout = false;
 
     private static String wanIp;
     private static HashSet<Integer> registeredPorts = new HashSet<Integer>();
 
     /**
      * Initializes the UPnP service: Getting the UPnP router and detecting WAN IP if enabled.
      */
     static void init() {
         MinecraftUpnp.log.info("Initializing UPnP service");
         upnpService = new UpnpServiceImpl();
 
         final AutoResetEvent resetEvent = new AutoResetEvent(false);
 
         // Set timeout to 10 sec
         final Timer timer = new Timer();
         timer.schedule(new TimerTask() {
             @Override
             public void run() {
                 MinecraftUpnp.log
                         .warning("Could not find an UPnP compatible router, please check if the router supports UPnP");
                 deviceTimeout = true;
                 cancel();
                 resetEvent.set();
             }
         }, 10000);
 
         // Add listener
         MinecraftUpnp.log.finest("Adding UPnP listener");
         upnpService.getRegistry().addListener(new DefaultRegistryListener() {
             @Override
             public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
                 if (deviceTimeout) {
                     upnpService.getRegistry().removeListener(this);
                     return;
                 }
 
                 RemoteService service = device.findService(new UDAServiceType("WANIPConnection"));
                 if (service != null) {
                     timer.cancel();
                     upnpService.getRegistry().removeListener(this);
 
                     Upnp.device = device;
                     MinecraftUpnp.log.info("Found UPnP compatible router " + device.getDisplayString());
 
                     // Only autodetect WAN IP address if it is enabled
                     if (Upnp.isAutoWanIpEnabled())
                         Upnp.loadWanIp();
                     else
                         Upnp.wanIp = Config.externalIp;
                     resetEvent.set();
                 }
             }
         });
 
         // Get device that is connected to WAN
         MinecraftUpnp.log.fine("Start searching LAN for router");
         UDAServiceType udaType = new UDAServiceType("WANIPConnection");
         upnpService.getControlPoint().search(new UDAServiceTypeHeader(udaType));
 
         // Wait until finished
         resetEvent.waitOne();
     }
 
     /**
      * Shuts down the UPnP service. This needs to be called prior shutting down the server.
      */
     static void shutdown() {
         MinecraftUpnp.log.info("Shutting down UPnP service");
         unregisterAllPorts();
         upnpService.shutdown();
     }
 
     /**
      * Gets the LAN IP address that represents the computer on which the server is running.
      * 
      * @return The LAN IP address.
      */
     public static String getLanIp() {
         try {
             return InetAddress.getLocalHost().getHostAddress();
         } catch (UnknownHostException e) {
             return null;
         }
     }
 
     /**
      * Loads the WAN IP address from the router.
      */
     private static void loadWanIp() {
         MinecraftUpnp.log.finer("Determining external IP address");
         RemoteService service = device.findService(new UDAServiceType("WANIPConnection"));
 
         final AutoResetEvent resetEvent = new AutoResetEvent(false);
         upnpService.getControlPoint().execute(new GetExternalIP(service) {
             @Override
             protected void success(String externalIPAddress) {
                 Upnp.wanIp = externalIPAddress;
                 MinecraftUpnp.log.info("Found external IP address: " + externalIPAddress);
                 resetEvent.set();
             }
 
             @SuppressWarnings("rawtypes")
             @Override
             public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                 MinecraftUpnp.log
                         .severe("Could not find external IP address, please check if the router supports UPnP");
                 resetEvent.set();
             }
         });
 
         resetEvent.waitOne();
     }
 
     /**
      * Gets the WAN IP address on which the server can be reached from outside the LAN.
      * 
      * @return The WAN IP address.
      */
     public static String getWanIp() {
         return wanIp;
     }
 
     /**
      * Gets whether the WAN IP address is automatically detected or not (can be disabled from the config by specifying a
      * ip address manually).
      * 
      * @return True if the WAN IP address is automatically detected, otherwise false.
      */
     public static boolean isAutoWanIpEnabled() {
         return Config.externalIp == null || Config.externalIp.isEmpty();
     }
 
     /**
      * Registers a port on the UPnP router.
      * 
      * @param port
      *            The port number to register.
      */
     public static void registerPort(int port) {
        if (deviceTimeout)
             return;
 
         final AutoResetEvent resetEvent = new AutoResetEvent(false);
 
         RemoteService service = device.findService(new UDAServiceType("WANIPConnection"));
         PortMapping mapping = new PortMapping(port, getLanIp(), PortMapping.Protocol.TCP, "MinecraftUPnP");
         ControlPoint cp = upnpService.getControlPoint();
 
         cp.execute(new PortMappingAdd(service, mapping) {
             @SuppressWarnings("rawtypes")
             @Override
             public void success(ActionInvocation invocation) {
                 MinecraftUpnp.log.info("Registered port " + portMapping.getInternalPort());
                 resetEvent.set();
             }
 
             @SuppressWarnings("rawtypes")
             @Override
             public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                 MinecraftUpnp.log.severe("Could not register port " + portMapping.getInternalPort());
                 resetEvent.set();
             }
         });
         resetEvent.waitOne();
         registeredPorts.add(port);
     }
 
     /**
      * Unregisters a port on the UPnP router.
      * 
      * @param port
      *            The port number to unregister.
      */
     public static void unregisterPort(int port) {
        if (deviceTimeout)
             return;
 
         final AutoResetEvent resetEvent = new AutoResetEvent(false);
         RemoteService service = device.findService(new UDAServiceType("WANIPConnection"));
         PortMapping mapping = new PortMapping(port, getLanIp(), PortMapping.Protocol.TCP, "MinecraftUPnP");
         ControlPoint cp = upnpService.getControlPoint();
 
         cp.execute(new PortMappingDelete(service, mapping) {
             @SuppressWarnings("rawtypes")
             @Override
             public void success(ActionInvocation invocation) {
                 MinecraftUpnp.log.info("Unregistered port " + portMapping.getInternalPort());
                 resetEvent.set();
             }
 
             @SuppressWarnings("rawtypes")
             @Override
             public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                 MinecraftUpnp.log.severe("Could not unregister port " + portMapping.getInternalPort());
                 resetEvent.set();
             }
         });
         resetEvent.waitOne();
         registeredPorts.remove(port);
     }
 
     /**
      * Unregisters all ports on the UPnP router that are registered through this mod.
      */
     static void unregisterAllPorts() {
         Object[] ports = registeredPorts.toArray();
         for (Object port : ports) {
             unregisterPort((Integer) port);
         }
     }
 }
