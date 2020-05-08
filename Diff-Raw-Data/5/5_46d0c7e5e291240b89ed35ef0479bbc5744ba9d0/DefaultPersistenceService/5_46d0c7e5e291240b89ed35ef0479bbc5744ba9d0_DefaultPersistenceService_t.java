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
 
 package de.cosmocode.palava.jpa;
 
 import java.util.Map;
 import java.util.Properties;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 
 import de.cosmocode.palava.core.lifecycle.Disposable;
 import de.cosmocode.palava.core.lifecycle.Initializable;
 import de.cosmocode.palava.core.lifecycle.LifecycleException;
 import de.cosmocode.palava.ipc.IpcConnectionScoped;
 
 /**
  * Default implementation of the {@link PersistenceService} interface.
  * 
  * @author Willi Schoenborn
  */
 final class DefaultPersistenceService implements PersistenceService, Initializable, Disposable {
 
     private static final Logger LOG = LoggerFactory.getLogger(DefaultPersistenceService.class);
     
     private final String unitName;
 
     private EntityManagerFactory factory;
     
     private Properties properties;
     
     @Inject
     public DefaultPersistenceService(@Named(PersistenceConfig.UNIT_NAME) String unitName) {
         this.unitName = Preconditions.checkNotNull(unitName, "UnitName");
     }
     
     @Inject(optional = true)
     void setProperties(@Named(PersistenceConfig.PROPERTIES) Properties properties) {
         this.properties = Preconditions.checkNotNull(properties, "Properties");
     }
     
     @Override
     public void initialize() throws LifecycleException {
         if (properties == null) {
            LOG.info("Creating entity manager factory");
             this.factory = Persistence.createEntityManagerFactory(unitName);
         } else {
            LOG.info("Creating entity manager factory using {}", properties);
             this.factory = Persistence.createEntityManagerFactory(unitName, properties);
         }
     }
     
     @Override
     @IpcConnectionScoped
     public EntityManager get() {
         return createEntityManager();
     }
 
     @Override
     public EntityManager createEntityManager() {
         return new DestroyableEntityManager(factory.createEntityManager());
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public EntityManager createEntityManager(Map map) {
         return new DestroyableEntityManager(factory.createEntityManager(map));
     }
 
     @Override
     public boolean isOpen() {
         return factory.isOpen();
     }
 
     @Override
     public void close() {
         LOG.info("Closing {}", factory);
         factory.close();
     }
     
     @Override
     public void dispose() {
         close();
     }
 
 }
