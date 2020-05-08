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
 
 import javax.persistence.EntityManager;
 
 import com.google.inject.Binder;
 import com.google.inject.Module;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 
 /**
  * {@link Module} which binds the {@link PersistenceService} interface
  * to its default implementation and registers as a {@link Provider} for {@link EntityManager}s.
  *
  * @author Willi Schoenborn
  */
 public final class PersistenceModule implements Module {
 
     @Override
     public void configure(Binder binder) {
         binder.bind(PersistenceService.class).to(DefaultPersistenceService.class).in(Singleton.class);
        binder.bind(EntityManager.class).toProvider(PersistenceService.class);
     }
 
 }
