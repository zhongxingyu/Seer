 /*
   This file is part of opensearch.
   Copyright © 2009, Dansk Bibliotekscenter a/s,
   Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
   opensearch is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
 
   opensearch is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /**
  * \file
  * \brief
  */
 
 
 package dk.dbc.opensearch.common.fedora;
 
 
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.IObjectIdentifier;
 import dk.dbc.opensearch.common.metadata.IPredicate;
 import dk.dbc.opensearch.common.types.Pair;
 
 import java.util.List;
 
 
 
 /**
  * Represents the operation interface on the underlying digital object
  * repository. The interface differentiates between two types of input data to
  * the digital repository; CargoContainers, representing digital objects, and
  * CargoObjects, representing single streams in objects. If multiple streams are
  * retrieved from the digital repository from different digital objects, they
  * will be packaged in a CargoContainer, whose identifier for the metadata will
  * not point to a single pid.
  *
  */
 public interface IObjectRepository
 {
     /** 
      * Queries the object repository for the existence of the object
      * based on the identifier of the object
      * 
      * @param objectIdentifier identifies the object in the scope of
      * the object repository
      * 
      * @return true if the object exists in the repository, false otherwise
      */
     public boolean hasObject( IObjectIdentifier objectIdentifier ) throws ObjectRepositoryException;
 
 
     /** 
      * Stores the object {@code cargo} in the object repository, using
      * {@code logmessage} to describe the nature of the object
      * storage.  
      * The returned String must provide the client with a unique
      * (within the object repository) identification of the stored
      * object.
      *
      * @param cargo A {@link CargoContainer} containing the data to be stored.
      * @param logmessage Message describing the storage operation
      * @param defaultNamespace PID namespace 
      * 
      * @return A unique identifer for the object in the object repository
      * @throws ObjectRepositoryException if the object cannot be stored
      */    
     public String storeObject( CargoContainer cargo, String logmessage, String defaultNamespace ) throws ObjectRepositoryException;
 
 
     /** 
      * Retrieves the object identified by {@code identifier}. The
      * identifier must be unique for the object repository and thereby
      * designate one single object or none at all within the
      * repository.
      * 
      * @param identifier identifying the object in the scope of the object repository
      * 
      * @return A {@link CargoContainer} containing the object data from the repository
      * @throws ObjectRepositoryException if the object cannot be retrieved
      */
     public CargoContainer getObject( String identifier ) throws ObjectRepositoryException;
 
 
     /** 
      * Deletes the object identified by {@code identifier} from the
      * repository. The implementation can choose whether the object
      * should be permanently deleted or just hidden from the client.
      * 
      * @param identifier identifying the object in the repository
      * @param logmessage describing the reasons for the deletion
      * 
      * @throws ObjectRepositoryException if the object cannot be deleted
      */
     public void deleteObject( String identifier, String logmessage ) throws ObjectRepositoryException;
    
 
     /** 
      * Searches the object repository using the a {@link List} of
      * {@code conditions} as query, limiting {@link List} of returned 
      * identifiers with {@code maximumResult}
      * 
      * @param conditions {@link List} of {@link OpenSearchCondition}s.
      * @param maximumResults integer limiting the returned {@link List}
      * of identifiers
      * 
      * @return a {@link List} of identifiers that matched {@code searchStrings} in {@code searchableFields}
      */
     public List< String > getIdentifiers( List< OpenSearchCondition > conditions, int maximumResults );
 
 
     /**
     * Gets the PIDs of targets of a certain type of relation an object in the repository has
     * @param subject, a {@link String} representing the object we want to get relations for
     * @param predicate, a {@link String} specifying which relation to get
      * @return a {@link List} of {@link Pair} of {@link String} and {@link String} containing the objects 
      * of the relations that subject has relation predicate to
      */


    public List< Pair<IPredicate, String> > getObjectRelations( String subject, String predicate ) throws ObjectRepositoryException;
 
 
     /**
      *  Adds 
      *  
      * 
      * @param objectIdentifier 
      * @param relation
      * @param subject
      * @throws ObjectRepositoryException
      */     
      public void addObjectRelation( IObjectIdentifier objectIdentifier, IPredicate relation, String subject ) throws ObjectRepositoryException;
 
 
     /**
      * @param objectIdentifier 
      * @param relation
      * @param subject
      * @throws ObjectRepositoryException
      */     
      public void addUncheckedObjectRelation( IObjectIdentifier objectIdentifier, String relation, String subject ) throws ObjectRepositoryException;
 
 
      /**
       * 
       * @param objectIdentifier i
       * @param relation
       * @param subject
       * @throws ObjectRepositoryException
       */
      public void removeObjectRelation( IObjectIdentifier objectIdentifier, IPredicate relation, String subject ) throws ObjectRepositoryException;
 
 }
