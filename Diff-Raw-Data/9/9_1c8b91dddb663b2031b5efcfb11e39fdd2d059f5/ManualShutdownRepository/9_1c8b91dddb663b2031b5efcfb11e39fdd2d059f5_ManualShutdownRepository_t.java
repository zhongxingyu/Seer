 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.podd.utils;
 
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.base.RepositoryWrapper;
 
 /**
  * A wrapper for a {@link Repository} that does not get shutdown when calling shutDown. Instead,
  * realShutDown must be called to shut the repository down.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class ManualShutdownRepository extends RepositoryWrapper
 {
     public ManualShutdownRepository(final Repository delegate)
     {
         super(delegate);
     }
     
     @Override
     public void shutDown() throws RepositoryException
     {
         // Do nothing, to avoid accidental shutdowns by shutting down the federation
     }
     
     public void realShutDown() throws RepositoryException
     {
        if(this.getDelegate() instanceof ManualShutdownRepository)
        {
            ((ManualShutdownRepository)this.getDelegate()).realShutDown();
        }
        else
        {
            this.getDelegate().shutDown();
        }
     }
 }
