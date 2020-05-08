 /**
  * Elastic Grid
  * Copyright (C) 2008-2009 Elastic Grid, LLC.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.elasticgrid.storage.spi;
 
 import com.elasticgrid.storage.Container;
 import com.elasticgrid.storage.StorageException;
 import com.elasticgrid.storage.ContainerNotFoundException;
 import java.util.List;
 import java.rmi.RemoteException;
 
 /**
  * The Storage Engine enables virtualization of underlying storage providers.
  * @author Jerome Bernard
  */
public interface StorageEngine {
 
     /**
      * Return the name of the underlying storage technology used.
      * This is typically the name of the product doing the real "storage".
      * @return the name of the underlying storage technology
      * @throws RemoteException
      */
     String getStorageName() throws RemoteException;
 
     /**
      * Return the list of containers:
      * <ul>
      *   <li>Amazon: similar to S3 list of buckets</li>
      *   <li>Eucalyptus: similar to S3-like list of buckets</li>
      *   <li>Mosso: similar to containers of Cloud Files</li>
      * </ul>
      * @return the list of containers
      * @throws StorageException if the list of containers can't be retrieved
      * @throws RemoteException
      */
     List<Container> getContainers() throws StorageException, RemoteException;
 
     /**
      * Create a new container.
      * @param name the name of the container to create
      * @return the created container
      * @throws StorageException if the container can't be created
      * @throws RemoteException
      */
     Container createContainer(String name) throws StorageException, RemoteException;
 
     /**
      * Lookup a container by its name.
      * @param name the name of the container to lookup
      * @return the container
      * @throws ContainerNotFoundException if there is no container named <tt>name</tt>
      * @throws StorageException if the container can't be created
      * @throws RemoteException
      */
     Container findContainerByName(String name) throws StorageException, RemoteException;
 
     /**
      * Recursively delete a container.
      * @param name the name of the container to delete
      * @throws ContainerNotFoundException if there is no container named <tt>name</tt>
      * @throws StorageException if the container can't be deleted
      * @throws RemoteException
      */
     void deleteContainer(String name) throws StorageException, RemoteException;
 }
