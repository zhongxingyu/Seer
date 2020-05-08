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
 
 package de.cosmocode.palava.entity;
 
 import java.util.List;
 
 import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
 import javax.persistence.NonUniqueResultException;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 
 import de.cosmocode.palava.model.base.EntityBase;
 
 /**
  * A service which allows readonly-operations on entites of a specific type.
  *
  * @author Willi Schoenborn
  * @param <T> the generic entity type
  */
 public interface ReadOnlyEntityService<T extends EntityBase> {
 
     /**
      * Retrieves an entity from the database.
      * 
      * @param identifier the entity's identifier
      * @return the entity associated with the given identifier
      *         or null if there is no such entity
      */
     T get(long identifier);
     
     /**
      * Retrieves an entity from the database.
      * 
      * @param identifier the entity's identifier
      * @return the entity associated with the given identifier
      *         or null if there is no such entity
      * @throws PersistenceException if there is no entity with the given identifier
      */
     T read(long identifier);
     
     /**
      * Retrievs an entity from the database.
      * 
      * @param query the query being executed
      * @param parameters the parameters the query needs
      * @return the single entity found using the given query
     * @throws NoResultException if there is no matching entity
      * @throws NonUniqueResultException if there is more than one matching entity
      */
     T read(Query query, Object... parameters);
 
     /**
      * Retrievs an entity from the database.
      * 
      * @param queryName the name of the query being executed
      * @param parameters the parameters the query needs
      * @return the single entity found using the given query
     * @throws NoResultException if there is no matching entity
      * @throws NonUniqueResultException if there is more than one matching entity
      */
     T read(String queryName, Object... parameters);
     
     /**
      * Retrieves a reference to an entity without
      * actually loading it to allow managing relationships
      * without the need to query every relative.
      * 
      * @param identifier the entity's identifer
      * @return a reference to the entity with the given identifer
      * @throws EntityNotFoundException if there is no matching entity
      */
     T reference(long identifier);
 
     /**
      * Retrievs a list of entities from the database.
      * 
      * @param query the query being executed
      * @param parameters the parameters the query needs
      * @return the single entity found using the given query
      */
     List<T> list(Query query, Object... parameters);
 
     /**
      * Retrievs a list of entities from the database.
      * 
      * @param queryName the name of the query being executed
      * @param parameters the parameters the query needs
      * @return the single entity found using the given query
      */
     List<T> list(String queryName, Object... parameters);
     
     /**
      * Retrievs a list of all entities of the associated type from the database.
      * 
      * @return a list of all entites
      */
     List<T> all();
     
     /**
      * Retrieves a projection, a single column, single row value.
      * 
      * @param <P> the generic value type
      * @param query the query being executed
      * @param parameters the parameters the query needs
      * @return the value found by the given query
      */
     <P> P projection(Query query, Object... parameters);
     
     /**
      * Retrieves a projection, a single column, single row value.
      * 
      * @param <P> the generic value type
      * @param queryName the name of the query being executed
      * @param parameters the parameters the query needs
      * @return the value found by the given query
      */
     <P> P projection(String queryName, Object... parameters);
     
     /**
      * Retrieves projections, multiple columns, single row values.
      * 
      * @param <P> the generic value type
      * @param query the query being executed
      * @param parameters the parameters the query need
      * @return the values found by the given query
      */
     <P> P[] projections(Query query, Object... parameters);
 
     /**
      * Retrieves projections, multiple columns, single row values.
      * 
      * @param <P> the generic value type
      * @param queryName the name of the query being executed
      * @param parameters the parameters the query need
      * @return the values found by the given query
      */
     <P> P[] projections(String queryName, Object... parameters);
     
     /**
      * Retrieves a list of projections, single column, multiple row values.
      * 
      * @param <P> the generic value type
      * @param query the query being executed
      * @param parameters the parameters the query needs
      * @return the values found by the given query
      */
     <P> List<P> projectionList(Query query, Object... parameters);
 
     /**
      * Retrieves a list of projections, single column, multiple row values.
      * 
      * @param <P> the generic value type
      * @param queryName the name of the query being executed
      * @param parameters the parameters the query needs
      * @return the values found by the given query
      */
     <P> List<P> projectionList(String queryName, Object... parameters);
     
     /**
      * Retrieves a list of projections, multiple columns, multiple row values.
      * 
      * @param <P> the generic value type
      * @param query the query being executed
      * @param parameters the parameters the query needs
      * @return the values found by the given query
      */
     <P> List<P[]> projectionsList(Query query, Object... parameters);
 
     /**
      * Retrieves a list of projections, multiple columns, multiple row values.
      * 
      * @param <P> the generic value type
      * @param queryName the name of the query being executed
      * @param parameters the parameters the query needs
      * @return the values found by the given query
      */
     <P> List<P[]> projectionsList(String queryName, Object... parameters);
     
     /**
      * Prepares the specified query with the given parameters by calling
      * {@link Query#setParameter(int, Object)} for every parameter.
      * 
      * @param query the query being prepared
      * @param parameters the parameters the query requires
      * @return the specified query
      */
     Query prepare(Query query, Object... parameters);
     
     /**
      * Prepares the query associated with the specified name with the given parameters
      * by calling {@link Query#setParameter(int, Object)} for every parameter.
      * 
      * @param queryName the name of the query being prepared
      * @param parameters the parameters the query requires
      * @return the prepared query
      */
     Query prepare(String queryName, Object... parameters);
 
 }
