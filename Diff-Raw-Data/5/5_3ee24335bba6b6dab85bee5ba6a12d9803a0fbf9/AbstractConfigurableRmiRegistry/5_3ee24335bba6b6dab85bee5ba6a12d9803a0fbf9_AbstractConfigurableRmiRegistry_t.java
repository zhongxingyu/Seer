 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
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
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.rmi;
 
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import de.cosmocode.palava.core.lifecycle.Initializable;
 import de.cosmocode.palava.core.lifecycle.LifecycleException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.rmi.*;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 
 /**
  * @author Tobias Sarnowski
  */
 abstract class AbstractConfigurableRmiRegistry implements RmiRegistry, Initializable {
     private static final Logger LOG = LoggerFactory.getLogger(AbstractConfigurableRmiRegistry.class);
 
     private Registry registry;
     private String host = "localhost";
     private int port = Registry.REGISTRY_PORT;
 
     abstract Registry initializeRegistry(String host, int port) throws RemoteException;
 
     @Override
     public final void initialize() throws LifecycleException {
         try {
             registry = initializeRegistry(host, port);
         } catch (RemoteException e) {
             throw new LifecycleException(e);
         }
     }
 
     @Inject(optional = true)
     public void setHost(@Named(RmiConfig.REGISTRY_HOST) String host) {
         this.host = host;
         LOG.trace("RMI registry host set to {}", host);
     }
 
     @Inject(optional = true)
     public void setPort(@Named(RmiConfig.REGISTRY_PORT) int port) {
         this.port = port;
         LOG.trace("RMI registry port set to {}", port);
     }
 
     @Override
     public Remote lookup(String name) throws RemoteException, NotBoundException, AccessException {
        LOG.trace("looking up {}", name);
         return registry.lookup(name);
     }
 
     @Override
     public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException, AccessException {
        LOG.info("binding {} with {}", name, obj);
         registry.bind(name, obj);
     }
 
     @Override
     public void unbind(String name) throws RemoteException, NotBoundException, AccessException {
         LOG.info("unbinding {}", name);
         registry.unbind(name);
     }
 
     @Override
     public void rebind(String name, Remote obj) throws RemoteException, AccessException {
         LOG.info("rebinding {} with {}", name, obj);
         registry.rebind(name, obj);
     }
 
     @Override
     public String[] list() throws RemoteException, AccessException {
         return registry.list();
     }
 
     @Override
     public <T extends Remote> T lookup(Class<T> cls) throws RemoteException, NotBoundException {
         return cls.cast(lookup(cls.getName()));
     }
 
     @Override
     public <T extends Remote> T lookup(Class<T> cls, String name) throws RemoteException, NotBoundException {
         return cls.cast(lookup(name));
     }
 
     @Override
     public void bind(Remote obj) throws RemoteException, AlreadyBoundException {
         bind(obj.getClass().getName(), obj);
     }
 
     @Override
     public <T extends Remote> void bind(Class<? super T> cls, T obj) throws RemoteException, AlreadyBoundException {
         bind(cls.getName(), obj);
     }
 
     @Override
     public <T extends Remote> void unbind(Class<T> cls) throws RemoteException, NotBoundException {
         unbind(cls.getName());
     }
 
     @Override
     public <T extends Remote> void rebind(Class<? super T> cls, T obj) throws RemoteException {
         rebind(cls.getName(), obj);
     }
 }
