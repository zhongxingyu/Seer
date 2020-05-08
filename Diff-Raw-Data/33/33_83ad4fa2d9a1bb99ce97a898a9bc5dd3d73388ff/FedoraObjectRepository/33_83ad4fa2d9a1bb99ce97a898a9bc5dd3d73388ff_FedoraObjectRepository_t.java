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
 
 
 import dk.dbc.opensearch.common.metadata.AdministrationStream;
 import dk.dbc.opensearch.common.metadata.IPredicate;
 import dk.dbc.opensearch.common.metadata.MetaData;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.Pair;
 import dk.dbc.opensearch.common.types.IObjectIdentifier;
 import dk.dbc.opensearch.common.types.ITargetField;
 
 import fedora.common.Constants;
 import fedora.server.types.gen.Condition;
 import fedora.server.types.gen.ComparisonOperator;
 import fedora.server.types.gen.Datastream;
 import fedora.server.types.gen.FieldSearchQuery;
 import fedora.server.types.gen.FieldSearchResult;
 import fedora.server.types.gen.ListSession;
 import fedora.server.types.gen.ObjectFields;
 import fedora.server.types.gen.RelationshipTuple;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.rmi.RemoteException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.xml.rpc.ServiceException;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.apache.axis.types.NonNegativeInteger;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 
 /**
  * FedoraObjectRepository is an implementation of the
  * IObjectRepository against a fedora digital object repository.
  */
 public class FedoraObjectRepository implements IObjectRepository
 {
     private static Logger log = Logger.getLogger( FedoraObjectRepository.class );
 
 
     /**
      * Dateformat conforming to the fedora requirements.
      */
     protected static final SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" );
     private final String hasStr = "has";
     private FedoraHandle fedoraHandle;
 
 
     /**
      * Initializes the FedoraObjectRepository; tries to connect to the
      * underlying objectrepository. If this fails in some way, an
      * ObjectRepositoryException is thrown.
      */
     public FedoraObjectRepository() throws ObjectRepositoryException
     {
         this.fedoraHandle = new FedoraHandle();
     }
 
 
     @Override
     public boolean hasObject( IObjectIdentifier identifier ) throws ObjectRepositoryException
     {
         try
         {
             return this.fedoraHandle.hasObject( identifier.getIdentifier() );
         }
         catch ( RemoteException e )
         {
             String error = String.format( "RemoteException Error Connecting to fedora: %s", e.getMessage() );
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
         }
     }
 
 
     @Override
     public void deleteObject( String identifier, String logmessage ) throws ObjectRepositoryException
     {
         String timestamp = null;
         try
         {
             // note that we're never forcing a deletion
             timestamp = this.fedoraHandle.purgeObject( identifier, logmessage, false );
         }
         catch( ConfigurationException ex )
         {
             String error = String.format( "ConfigurationException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( ServiceException ex )
         {
             String error = String.format( "ServiceException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( MalformedURLException ex )
         {
             String error = String.format( "MalformedURLException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( RemoteException ex )
         {
             String error = String.format( "RemoteException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "IOException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         if ( timestamp == null )
         {
             String error = String.format( "Could not delete object reference by pid %s (timestamp == null)", identifier );
             log.error( error );
             throw new ObjectRepositoryException( error );
         }
     }
 
 
     public boolean purgeRelationship( String identifier, String predicate, String object, String dataType ) throws ObjectRepositoryException
     {
         boolean literal = true;
         //System.out.println( String.format( "pid: '%s'; predicate: '%s'; object: '%s'; dataType: '%s'", pid, predicate, object, dataType ) );
         try
         {
             return fedoraHandle.purgeRelationship( identifier, predicate, object, literal, dataType );
         }
         catch( ConfigurationException ex )
         {
             String error = String.format( "ConfigurationException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( ServiceException ex )
         {
             String error = String.format( "ServiceException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( MalformedURLException ex )
         {
             String error = String.format( "MalformedURLException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( RemoteException ex )
         {
             String error = String.format( "RemoteException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "IOException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
     }
 
 
     /**
      * Stores data in {@code cargo} in the fedora repository
      *
      * @param cargo the data to store
      * @param logmessage message to log the operation with
      * @return the objectIdentifier of the stored object
      * @throws ObjectRepositoryException if the cargo could not be transformed
      * into foxml or the foxml could not be stored
      */
     @Override
     public String storeObject( CargoContainer cargo, String logmessage, String defaultPidNamespace ) throws ObjectRepositoryException
     {
         if( cargo.getCargoObjectCount() == 0 )
         {
             log.error( String.format( "No data in CargoContainer, refusing to store nothing" ) );
             throw new IllegalStateException( String.format( "No data in CargoContainer, refusing to store nothing" ) );
         }
 
         IObjectIdentifier identifier = getOrGenerateIdentifier( cargo, defaultPidNamespace );
         cargo.setIdentifier( identifier );
         String identifierAsString = identifier.getIdentifier();
 
         cargo.addMetaData( constructAdministrationStream( cargo ) );
 
         byte[] foxml;
         try
         {
             foxml = FedoraUtils.CargoContainerToFoxml( cargo );
 
         }
         catch ( XMLStreamException ex )
         {
             String error = String.format( "Failed in serializing CargoContainer with id '%s': %s", identifierAsString, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch ( TransformerConfigurationException ex )
         {
             String error = String.format( "Failed in serializing CargoContainer with id '%s': %s", identifierAsString, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch ( TransformerException ex )
         {
             String error = String.format( "Failed in serializing CargoContainer with id '%s': %s", identifierAsString, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch ( SAXException ex )
         {
             String error = String.format( "Failed in serializing CargoContainer with id '%s': %s", identifierAsString, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch ( IOException ex )
         {
             String error = String.format( "Failed in serializing CargoContainer with id '%s': %s", identifierAsString, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
 
         String returnedobjectIdentifier = null;
         try
         {
             returnedobjectIdentifier = this.fedoraHandle.ingest( foxml, Constants.FOXML1_1.uri, logmessage );
         }
         catch( ConfigurationException ex )
         {
             String error = String.format( "Failed to ingest object with pid '%s' into repository: %s", identifierAsString, ex.getMessage() );
             log.error( error, ex );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( MalformedURLException ex )
         {
             String error = String.format( "Failed to ingest object with pid '%s' into repository: %s", identifierAsString, ex.getMessage() );
             log.error( error, ex );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( ServiceException ex )
         {
             String error = String.format( "Failed to ingest object with pid '%s' into repository: %s. Foxml: %s", identifierAsString, ex.getMessage(), new String( foxml ) );
             log.error( error, ex );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to ingest object with pid '%s' into repository: %s. Foxml: %s", identifierAsString, ex.getMessage(), new String( foxml ) );
             log.error( error, ex );
             throw new ObjectRepositoryException( error, ex );
         }
 
         if ( returnedobjectIdentifier.equals( "" ) )
         {
             log.info( String.format( "For empty identifier, we recieved '%s' from the ingest", returnedobjectIdentifier ) );
         }
         else if ( ! returnedobjectIdentifier.equals( identifierAsString ) )
         {
             log.warn( String.format( "I expected pid '%s' to be returned from the repository, but got '%s'", identifierAsString, returnedobjectIdentifier ) );
         }
 
         return returnedobjectIdentifier;
     }
 
 
     /**
      * Retrieves an object encoded as a {@link CargoContainer} from the fedora
      * object repository. The method also handles the information given in
      * the administration stream of the object
      *
      * @param identifier the fedora pid identifying the object in the repository
      * @return the object encoded as a {@link CargoContainer}
      * @throws ObjectRepositoryException containing an exception explaining why
      * the object could not be retrieved
      */
     @Override
     public CargoContainer getObject( String identifier ) throws ObjectRepositoryException
     {
         byte[] adminbytes = getDataStreamDissemination( identifier, DataStreamType.AdminData.getName() );
 
         if ( adminbytes == null || adminbytes.length < 1 )
         {
             String error = String.format( "Failed to obtain data for administration stream, cannot retrieve data from '%s'", identifier );
             log.error( error );
             throw new IllegalStateException( error );
         }
 
         AdministrationStream adminStream;
         try
         {
             //log.trace( String.format( "Administration stream: %s", new String( adminbytes ) ) );
             adminStream = new AdministrationStream( new ByteArrayInputStream( adminbytes ), true );
         }
         catch( XMLStreamException ex )
         {
             String error = String.format( "Failed to contruct administration stream from retrieved xml [%s]: %s", new String( adminbytes ), ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( SAXException ex )
         {
             String error = String.format( "Failed to contruct administration stream from retrieved xml [%s]: %s", new String( adminbytes ), ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to contruct administration stream from retrieved xml [%s]: %s", new String( adminbytes ), ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         List< Pair< Integer, Pair< String, CargoObject > > > adminstreamlist;
         try
         {
             adminstreamlist = adminStream.getStreams();
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to retrieve streams from administrationstream" );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         CargoContainer cargo = new CargoContainer();
         cargo.setIdentifier( new PID( identifier ) );
 
         for ( Pair< Integer, Pair< String, CargoObject > > cargoobjects : adminstreamlist )
         {
             String streamId = cargoobjects.getSecond().getFirst();
 
             if ( streamId.equals( DataStreamType.DublinCoreData.getName() + ".0" ) )
             {
                 streamId = "DC";
             }
             log.debug( String.format( "id: %s, streamId: %s", identifier, streamId ) );
 
             byte[] datastream;
             try
             {
                 datastream = this.fedoraHandle.getDatastreamDissemination( identifier, streamId, null );
             }
             catch( ConfigurationException ex )
             {
                 String error = String.format( "Failed to retrieve datastream with id '%s' from pid '%s': %s", streamId, identifier, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( MalformedURLException ex )
             {
                 String error = String.format( "Failed to retrieve datastream with id '%s' from pid '%s': %s", streamId, identifier, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( IOException ex )
             {
                 String error = String.format( "Failed to retrieve datastream with id '%s' from pid '%s': %s", streamId, identifier, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( ServiceException ex )
             {
                 String error = String.format( "Failed to retrieve datastream with id '%s' from pid '%s': %s", streamId, identifier, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
 
             CargoObject co = cargoobjects.getSecond().getSecond();
 
             try
             {
                 cargo.add( co.getDataStreamType(), co.getFormat(), co.getSubmitter(), co.getLang(), co.getMimeType(),  datastream );
             }
             catch( IOException ex )
             {
                 String error = String.format( "Failed to add data with administrationstream id '%s' to CargoContainer: %s", streamId, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
         }
 
         if ( cargo.getCargoObjectCount() < 1 && adminStream.getCount() > 0 )
         {
             throw new ObjectRepositoryException( "CargoContainer is empty, even though adminstream says it gave data" );
         }
 
         return cargo;
     }
 
 
     /**
      * Retrieves all identifiers mathcing {@code conditions}
      *
      * @param conditions A list of {@link OpenSearchCondition}s 
      * @param maximumResults the largest number of results to retrieve
      *
      * @return A {@link List} containing all matching identifiers.
      *
      * @throws IllegalArgumentException if {@code maximumResults} is
      * less than one, since neither a negative number of results or
      * zero results makes any sense.
      */
     @Override
     public List< String > getIdentifiers( List< OpenSearchCondition > conditions, int maximumResults ) throws IllegalArgumentException
     { 
 	if ( maximumResults < 1 )
 	{
 	    throw new IllegalArgumentException( String.format( "The maximum number of results retrived cannot be null or less than one. Argument given: %s", maximumResults ) );
 	}
 	
 	String[] resultFields = { "pid" }; // we will only return pids!
 	// \todo: Perhaps NonNegativeInteger should be replaced with PositiveInteger,
 	// since this function disallows values < 1.
 	NonNegativeInteger maxRes = new NonNegativeInteger( Integer.toString( maximumResults ) );
 
         ObjectFields[] objectFields = searchRepository( resultFields, conditions, maximumResults );
 	
         int ofLength = objectFields.length;
         List< String > pids = new ArrayList< String >( ofLength );
         for( int j = 0; j < ofLength; j++ )
         {
             String pidValue = objectFields[j].getPid();
             pids.add( pidValue );
         }
 
         return pids;
     }
 
 
     /***
      * Searches repository with conditions specified by propertiesAndValues using comparisonOperator, e.g. 'has', 'eq'.
      * The parameter 'namespace' if not null is used to limit the result set on pid containing namespace. Beware that
      * the comparison operator in this case cannot be 'eq'.
      *
      * @param resultFields
      * @param conditions
      * @param maximumResults
      *
      * @return An array of ObjectFields.
      */
     ObjectFields[] searchRepository( String[] resultFields, List< OpenSearchCondition > conditions, int maximumResults )
     {
 	// We will convert from OpenSearchCondition to Condition in two steps:
 	// 1) converting operators and test validity of search-value
 	// 2) put valid conditions into a Condition[].
 	// 
 
 	// Convert operators and test validity of search values
 	List< Condition > validConditions = new ArrayList< Condition >( conditions.size() );
         for( OpenSearchCondition condition : conditions )
         {
 	    ITargetField field = (ITargetField)condition.getField();
 	    String value = condition.getValue();
 
 	    // Set the fedora ComparisonOperator:
 	    // Notice: ComparisonOperator from Fedora is not an enum, and as such has no function valueOf().
 	    //         It do, however, has a function fromString() which seems to do the same - hopefully.
 	    String compStr = "eq";
 	    switch ( condition.getOperator() )
 	    {
 	    case EQUALS:
 		compStr = "eq";
 		break;
 	    case CONTAINS:
 		compStr = "has";
 		break;
 	    case GREATER_THAN:
 		compStr = "gt";
 		break;
 	    case GREATER_OR_EQUAL:
 		compStr = "ge";
 		break;
 	    case LESS_THAN:
 		compStr = "lt";
 		break;
 	    case LESS_OR_EQUAL:
 		compStr = "le";
 		break;
 	    default:
 		compStr = "eq";
 	    }
 	    ComparisonOperator comp = ComparisonOperator.fromString( compStr );
 	    log.info( String.format( "Setting fedora-condition: field[%s] comp[%s] value[%s]",
 				      field.fieldname(), comp.toString(), value) );
 
 	    if ( value.isEmpty() ) 
 	    {
 		log.warn( "Ignoring condition: We do not allow searches with empty search values." );
 		continue;
 	    }
 	    
 	    validConditions.add( new Condition( field.fieldname(), comp, value ) );
         }
 
 	// If there are no valid conditions, there is no need to perform the search:
 	if ( validConditions.size() == 0 ) 
 	{
 	    // \todo: I do not like to return in a middle of a function!
 	    return new ObjectFields[ 0 ];
 	}
 
 	// Convert validConditions-list into a Condition-array:
         Condition[] cond = new Condition[ validConditions.size() ];
 	int i=0;
 	for ( Condition condition : validConditions )
 	{
 	    cond[i++] = condition;
 	}
        
 	// Create query end result:
         FieldSearchQuery fsq = new FieldSearchQuery( cond, null );
         FieldSearchResult fsr = null;
 
         // A list to contain arrays of ObjectFields.
         // Whenever a new array of ObjectFields are found, either from findObjects or
         // resumeFindObjects, it is added to the list.
         // The Arrays are later collected into a single array.
         LinkedList< ObjectFields[] > objFieldsList = new LinkedList< ObjectFields[] >();
         int numberOfResults = 0;
         try
         {
             NonNegativeInteger maxResults = new NonNegativeInteger( Integer.toString( maximumResults ) );
             fsr = this.fedoraHandle.findObjects( resultFields, maxResults, fsq );
 
             numberOfResults += fsr.getResultList().length;
             objFieldsList.push( fsr.getResultList() );
 
             ListSession listSession = fsr.getListSession();
             while ( listSession != null )
             {
                 String token = listSession.getToken();
                 fsr = this.fedoraHandle.resumeFindObjects( token );
                 if ( fsr != null )
                 {
                     numberOfResults += fsr.getResultList().length;
                     objFieldsList.push( fsr.getResultList() );
                     listSession = fsr.getListSession();
                 }
                 else
                 {
                     listSession = null;
                 }
             }
 
             log.debug( String.format( "Result length of resultlist: %s", numberOfResults ) );
 
         }
         catch( ConfigurationException ex )
         {
             String warn = String.format( "ConfigurationException -> Could not conduct query: %s", ex.getMessage() );
             log.warn( warn );
         }
         catch( ServiceException ex )
         {
             String warn = String.format( "ServiceException -> Could not conduct query: %s", ex.getMessage() );
             log.warn( warn );
         }
         catch( MalformedURLException ex )
         {
             String warn = String.format( "MalformedURLException -> Could not conduct query: %s", ex.getMessage() );
             log.warn( warn );
         }
         catch( IOException ex )
         {
             String warn = String.format( "IOException -> Could not conduct query: %s", ex.getMessage() );
             log.warn( warn );
         }
 
         if( fsr == null )
         {
             log.warn( "Retrieved no hits from search, returning empty List<String>" );
             return new ObjectFields[]{ };
         }
 
         ObjectFields[] objectFields = new ObjectFields[ numberOfResults ];
 
         // Collecting ObjectFields arrays into a single array:
         int destPos = 0; // destination position
         for ( ObjectFields[] of : objFieldsList )
         {
             System.arraycopy( of, 0, objectFields, destPos, of.length );
             destPos += of.length;
         }
 
         return objectFields;
     }
 
 
     private AdministrationStream constructAdministrationStream( CargoContainer cargo ) throws ObjectRepositoryException
     {
 
         AdministrationStream adminStream = new AdministrationStream();
 
         if( 0 == cargo.getCargoObjectCount() )
         {
             String error = String.format( "Refusing to construct AdministrationStream when CargoContainer (%s) has no data in it", cargo.getIdentifier() );
             log.error( error );
             throw new ObjectRepositoryException( error );
         }
         int streamtypecounter = 0;
 
         log.debug( String.format( "Constructing administration stream for %s cargo objects", cargo.getTotalObjectCount() - 1 ) );
         for( CargoObject co : cargo.getCargoObjects() )
         {
             streamtypecounter = adminStream.getCount( co.getDataStreamType() );
             String id = co.getDataStreamType().getName() + "." + Integer.toString( streamtypecounter );
             log.debug( String.format( "Adding stream '%s' with id '%s' to adminstream", co.getDataStreamType().getName(), id ) );
             adminStream.addStream( co, id );
         }
         streamtypecounter = 0;
         for( MetaData meta : cargo.getMetaData() )
         {
             streamtypecounter = adminStream.getCount( meta.getType() );
             String id = meta.getType().getName() + "." + Integer.toString( streamtypecounter );
             log.debug( String.format( "Adding stream '%s' with id '%s' to adminstream", meta.getType().getName(), id ) );
             adminStream.addStream( meta, id );
         }
         return adminStream;
     }
 
 
     /**
      * Wrapper around {@link #getDataStream(java.lang.String, dk.dbc.opensearch.common.types.DataStreamType)}
      * that retrieves an {@link AdministrationStream} and handles intermediary
      * exceptions that surface from the lower layers.
      *
      * @param objectIdentifier the identifies the object in the repository
      * @return the AdministrationStream for the object identified by
      * {@code objectIdentifier}
      * @throws ObjectRepositoryException if the administrationstream could not
      * be retrieved
      */
     private AdministrationStream getAdministrationStream( String objectIdentifier ) throws ObjectRepositoryException
     {
         AdministrationStream adminStream;
         byte[] adminbytes = null;
         try
         {
             adminbytes = getDataStreamDissemination( objectIdentifier, DataStreamType.AdminData.getName() );
             adminStream = new AdministrationStream( new ByteArrayInputStream( adminbytes ), true );
         }
         catch( XMLStreamException ex )
         {
             String error = String.format( "Failed to contruct administration stream from retrieved xml [%s]: %s", new String( adminbytes ), ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( SAXException ex )
         {
             String error = String.format( "Failed to contruct administration stream from retrieved xml [%s]: %s", new String( adminbytes ), ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to contruct administration stream from retrieved xml [%s]: %s", new String( adminbytes ), ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         return adminStream;
     }
 
 
     private boolean addDataUpdateAdminstream( String objectIdentifier, String dataIdentifier, CargoObject obj ) throws ObjectRepositoryException
     {
         AdministrationStream adminStream = getAdministrationStream( objectIdentifier );
 
         boolean added = adminStream.addStream( obj, dataIdentifier );
 
         if( !added )
         {
             log.warn( "Could not add information on added stream to administration stream" );
         }
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try
         {
             adminStream.serialize( baos, null );
         }
         catch( XMLStreamException ex )
         {
             String error = String.format( "Could not retrieve administration stream serialization" );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         String label = "admin [text/xml]";
         String mime = "text/xml";
         String timeNow = dateFormat.format( new Date( System.currentTimeMillis() ) );
         String adminLogm = "admin stream updated with added stream data" + timeNow;
         String location = uploadDatastream( baos );
         String[] alternatedsids = new String[]
             {
             };
 
         try
         {
             this.fedoraHandle.modifyDatastreamByReference( objectIdentifier, DataStreamType.AdminData.getName(), alternatedsids, label, mime, "", location, null, null, adminLogm, true );
         }
         catch( RemoteException ex )
         {
             String error = String.format( "Failed to replace administration stream on object with pid '%s': %s", objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         return added;
     }
 
 
     public Datastream getDatastream( String objectIdentifier, String dataStreamID ) throws ObjectRepositoryException
     {
         Datastream ds = null;
 
         try
         {
             ds = this.fedoraHandle.getDatastream( objectIdentifier, dataStreamID );
         }
         catch( RemoteException re )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': %s", dataStreamID, objectIdentifier, re.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, re );
         }
 
         if ( ds == null )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': Got nothing back from the object repository", dataStreamID, objectIdentifier );
             log.error( error );
             throw new ObjectRepositoryException( error );
         }
 
         return ds;
     }
 
 
     private byte[] getDataStreamDissemination( String objectIdentifier, String dataStreamTypeName ) throws ObjectRepositoryException
     {
         if ( null == objectIdentifier || null == dataStreamTypeName || objectIdentifier.isEmpty() || dataStreamTypeName.isEmpty() )
         {
             String error = String.format( "Necessary parameters for retrieving datastream was null" );
             log.error( error );
             throw new IllegalStateException( error );
         }
 
         byte[] ds = null;
         try
         {
             //System.out.println( "object id: " + objectIdentifier );
             ds = this.fedoraHandle.getDatastreamDissemination( objectIdentifier, dataStreamTypeName, null );
         }
         catch( ConfigurationException ex )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': %s", dataStreamTypeName, objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( MalformedURLException ex )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': %s", dataStreamTypeName, objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': %s", dataStreamTypeName, objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( ServiceException ex )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': %s", dataStreamTypeName, objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         if ( null == ds )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': Got nothing back from the object repository", dataStreamTypeName, objectIdentifier );
             log.error( error );
             throw new ObjectRepositoryException( error );
         }
 
         return ds;
     }
 
 
     private String uploadDatastream( ByteArrayOutputStream datastream ) throws ObjectRepositoryException
     {
         File tmpFile;
         try
         {
             String timeStamp = Long.toString( System.currentTimeMillis() ) + Integer.toString( datastream.hashCode() );
             tmpFile = File.createTempFile( "datastream" + timeStamp, ".tmp" );
         }
         catch( IOException ex )
         {
             String error = "Could not create temporary file";
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         tmpFile.deleteOnExit();
         FileOutputStream faos;
         try
         {
             faos = new FileOutputStream( tmpFile );
         }
         catch( FileNotFoundException ex )
         {
             String error = "Could not write to temporary file";
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         try
         {
             datastream.writeTo( faos );
         }
         catch( IOException ex )
         {
             String error = "Failed to write administration stream to temporary file";
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         String location;
         try
         {
             location = this.fedoraHandle.uploadFile( tmpFile );
         }
         catch( IOException ex )
         {
             String error = "Failed to upload temporary file to fedora server";
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         return location;
     }
 
 
     @Override
     public void addObjectRelation( IObjectIdentifier objectIdentifier, IPredicate relation, String subject ) throws ObjectRepositoryException
     {
         String relationString = relation.getPredicateString();
         this.addUncheckedObjectRelation( objectIdentifier, relationString, subject );
     }
 
     @Override
     public void addUncheckedObjectRelation( IObjectIdentifier objectIdentifier, String relationString, String subject ) throws ObjectRepositoryException
     {
         try
         {
             log.info( String.format( "trying to add %s - %s -> %s", objectIdentifier.getIdentifier(), relationString, subject ) );
             this.fedoraHandle.addRelationship( objectIdentifier.getIdentifier(), relationString, subject, true, null );
         }
         catch( IOException ex )
         {
             String error = "Failed to add Relation to fedora Object";
             log.error( error, ex );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( ConfigurationException e )
         {
             String error = "Failed to add Relation to fedora Object";
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
 
         }
         catch( ServiceException e )
         {
             String error = "Failed to add Relation to fedora Object";
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
         }
     }
 
     /**
      * This method finds the targets of the relations identified by the predicate argument for the object 
     * identified by the subject argument.
     *
     * @param subject {@link String} identifying the object to get relations for.
     * @param predicate {@link String} telling what relation to get targets for.
     * @return a {@link List} of {@link Pair}s of {@link IPredicate} and {@link String}.
     * In each {@link Pair} is the {@link IPredicate} representing the relation and the {@link String} the object.
      * 
      * \Todo: Why do we return the IPredicate telleing what relation we found, when we in the predicate param 
      * specifies what relation type to search for?
      */
     public List< Pair< IPredicate, String > > getObjectRelations( String subject, String predicate ) throws ObjectRepositoryException
     {
         try
         {
             log.debug( String.format( "calling with subject '%s' and predicate '%s'", subject, predicate ) );
             RelationshipTuple[] tuple = this.fedoraHandle.getRelationships( subject, predicate );
             if ( tuple != null )
             {
                 List< Pair< IPredicate, String > > ret = new ArrayList< Pair< IPredicate, String> >();
                 for ( RelationshipTuple relationship : tuple )
                 {
                     String object = relationship.getObject();
                     Pair< IPredicate, String > pair = new Pair( predicate, object ); //we put the predicate param into the return value, why?
                     ret.add( pair );
                 }
 
                 return ret;
             }
         }
         catch ( ConfigurationException ce )
         {
             String error = "Failed to get relation from fedora object";
             log.error( error, ce );
             throw new ObjectRepositoryException( error, ce );
         }
         catch( ServiceException se )
         {
             String error = "Failed to get relation from fedora object";
             log.error( error, se );
             throw new ObjectRepositoryException( error, se );
         }
         catch( MalformedURLException mue )
         {
             String error = "Failed to get relation from fedora object";
             log.error( error, mue );
             throw new ObjectRepositoryException( error, mue );
         }
         catch( IOException ioe )
         {
             String error = "Failed to get relation from fedora object";
             log.error( error, ioe );
             throw new ObjectRepositoryException( error, ioe );
         }
 
         log.debug( "returning null" );
         return null;
     }
 
 
     @Override
     public void removeObjectRelation( IObjectIdentifier objectIdentifier, IPredicate relation, String subject ) throws ObjectRepositoryException
     {
         try
         {
             String relationString = relation.getPredicateString();
             String pid = objectIdentifier.getIdentifier();
 
             log.debug( String.format( "trying to remove object %s with relation %s from pid %s", subject, relationString, pid ) );
             boolean purgeRelationship = this.fedoraHandle.purgeRelationship( pid, relationString, subject, true, null );
             if ( purgeRelationship )
             {
                 log.debug("purged");
                 log.info( String.format( "Ignored error from purgeRelationeship : on %s-%s->%s", pid, relationString, subject ) );
             }
             else
             {
                 log.debug("not purged");
             }
         }
         catch( IOException ex )
         {
             String error = "Failed to add Relation to fedora Object";
             log.error( error, ex );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( ConfigurationException e )
         {
             String error = "Failed to add Relation to fedora Object";
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
 
         }
         catch (ServiceException e)
         {
             String error = "Failed to add Relation to fedora Object";
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
         }
     }
 
 
     /**
      * Internal helper to store
      *
      * @param cargo
      * @return
      * @throws ObjectRepositoryException
      */
     private IObjectIdentifier getOrGenerateIdentifier( CargoContainer cargo, String defaultPidNameSpace ) throws ObjectRepositoryException
     {
         IObjectIdentifier identifier = cargo.getIdentifier();
 
         if (identifier != null)
         {
             return identifier;
         }
 
         if (defaultPidNameSpace == null)
         {
             defaultPidNameSpace = new String( "auto" );
         }
 
         String newPid = "";
         try
         {
             newPid = fedoraHandle.getNextPID( 1, defaultPidNameSpace )[0];
         }
         catch (ConfigurationException e)
         {
             throw new ObjectRepositoryException( String.format( "Unable to get next pid for Prefix ", defaultPidNameSpace ), e );
         }
         catch (MalformedURLException e)
         {
             throw new ObjectRepositoryException( String.format( "Unable to get next pid for Prefix ", defaultPidNameSpace ), e );
         }
         catch (ServiceException e)
         {
             throw new ObjectRepositoryException( String.format( "Unable to get next pid for Prefix ", defaultPidNameSpace ), e );
         }
         catch (IOException e)
         {
             throw new ObjectRepositoryException( String.format( "Unable to get next pid for Prefix ", defaultPidNameSpace ), e );
         }
 
         return new PID( newPid );
     }
 }
