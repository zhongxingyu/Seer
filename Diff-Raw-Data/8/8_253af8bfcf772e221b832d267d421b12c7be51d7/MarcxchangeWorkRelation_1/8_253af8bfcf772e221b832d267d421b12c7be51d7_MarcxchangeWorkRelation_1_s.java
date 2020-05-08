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
 
 
 package dk.dbc.opensearch.plugins;
 
 
 import dk.dbc.opensearch.common.config.FileSystemConfig;
 import dk.dbc.opensearch.common.fedora.FedoraHandle;
 import dk.dbc.opensearch.common.fedora.FedoraObjectRelations;
 import dk.dbc.opensearch.common.fedora.IObjectRepository;
 import dk.dbc.opensearch.common.fedora.ObjectRepositoryException;
 import dk.dbc.opensearch.common.fedora.PID;
 import dk.dbc.opensearch.common.metadata.DBCBIB;
 import dk.dbc.opensearch.common.metadata.DublinCore;
 import dk.dbc.opensearch.common.metadata.DublinCoreElement;
 import dk.dbc.opensearch.common.pluginframework.IRelation;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.pluginframework.PluginType;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.IndexingAlias;
 
 import dk.dbc.opensearch.common.types.InputPair;
 import fedora.server.types.gen.RelationshipTuple;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import javax.script.Invocable;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.xml.rpc.ServiceException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 
 
 /**
  * Plugin for annotating docbook carcoContainers
  */
 public class MarcxchangeWorkRelation_1 implements IRelation
 {
     private static Logger log = Logger.getLogger( MarcxchangeWorkRelation_1.class );
 
 
     private PluginType pluginType = PluginType.RELATION;
     private Vector<String> types;
     private FedoraObjectRelations fedor;
     private final FedoraHandle fedoraHandle;
     private IObjectRepository objectRepository;
     private String workNamespace = "work:";
 
     private final ScriptEngineManager manager = new ScriptEngineManager();
 
     /**
      * Constructor for the MarcxchangeWorlkRelation plugin.
      */
     public MarcxchangeWorkRelation_1() throws PluginException
     {
         log.debug( "MarcxchangeWorkRelation constructor called" );
     
         types = new Vector<String>();
         types.add( "Anmeldelse" );
         types.add( "Artikel" );
         types.add( "Avis" );
         types.add( "Avisartikel" );
         types.add( "Tidsskrift" );
         types.add( "Tidsskriftsartikel" );
         
        try
         {
             this.fedoraHandle = new FedoraHandle();
         }
         catch( ObjectRepositoryException ex )
         {
             String error = String.format( "Failed to get connection to fedora base" );
             log.error( error );
             throw new PluginException( error, ex );
         }
 
         try
         {
             fedor = new FedoraObjectRelations( objectRepository );
         }
         catch( ObjectRepositoryException ex )
         {
             String error = String.format( "Failed to obtain connection to fedora repository" );
             log.error( error );
             throw new PluginException( error, ex );
         }
     }
 
 
     /**
      * The "main" method of this plugin. Request a relation from
      * a webservice. If a relation is available it is added to the
      * cargocontainer in a new stream typed RelsExtData
      *
      * @param CargoContainer The CargoContainer to add relations to
      *
      * @returns A CargoContainer containing relations
      * 
      * @throws PluginException thrown if anything goes wrong during annotation.
      */
     @Override
     public CargoContainer getCargoContainer( CargoContainer cargo ) throws PluginException
     {
         log.trace( "getCargoContainer() called" );
 
         if ( cargo == null )
         {
             log.error( "MarcxchangeWorkRelation getCargoContainer cargo is null" );
             throw new PluginException( new NullPointerException( "MarcxchangeWorkRelation getCargoContainer throws NullPointerException" ) );
         }
 
         boolean ok = false;
         try
         {
             ok = addWorkRelationForMaterial( cargo );
         }
         catch( ObjectRepositoryException ex )
         {
             String error = String.format( "Failed to add work relation for %s: %s", cargo.getIdentifier(), ex.getMessage() );
             log.error( error , ex);
             throw new PluginException( error, ex );
         }
         catch( ConfigurationException ex )
         {
             String error = String.format( "Failed to add work relation for %s: %s", cargo.getIdentifier(), ex.getMessage() );
             log.error( error );
             throw new PluginException( error, ex );
         }
         catch( MalformedURLException ex )
         {
             String error = String.format( "Failed to add work relation for %s: %s", cargo.getIdentifier(), ex.getMessage() );
             log.error( error );
             throw new PluginException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to add work relation for %s: %s", cargo.getIdentifier(), ex.getMessage() );
             log.error( error );
             throw new PluginException( error, ex );
         }
         catch( ServiceException ex )
         {
             String error = String.format( "Failed to add work relation for %s: %s", cargo.getIdentifier(), ex.getMessage() );
             log.error( error );
             throw new PluginException( error, ex );
         }
 
         if ( ! ok )
         {
             log.error( String.format( "could not add work relation on pid %s", cargo.getIdentifier() ) );
         }
 
         return cargo;
     }
 
 
     public static String normalizeString(String s)
     {
         s = s.toLowerCase();
         String killchars = "~'-";
         
         StringBuffer res = new StringBuffer();
         for (int i = 0; i < s.length(); ++i)
         {
             if (-1 == killchars.indexOf(s.charAt(i)))
             {
                 res.append(s.charAt(i));
             }
         }
         return res.toString();
       
     }
 
     private boolean addWorkRelationForMaterial( CargoContainer cargo ) throws PluginException, ObjectRepositoryException, ConfigurationException, MalformedURLException, IOException, ServiceException
     {
         DublinCore dc = cargo.getDublinCoreMetaData();
 
         if( dc == null )
         {
             String error = String.format( "CargoContainer with identifier %s contains no DublinCore data", cargo.getIdentifier() );
             log.error( error );
             throw new PluginException( error );
         }
 
        String dcTitle = dc.getDCValue( DublinCoreElement.ELEMENT_TITLE );
        String dcType = dc.getDCValue( DublinCoreElement.ELEMENT_TYPE );
        String dcCreator = dc.getDCValue( DublinCoreElement.ELEMENT_CREATOR );
        String dcSource = dc.getDCValue( DublinCoreElement.ELEMENT_SOURCE );
         String pid = cargo.getIdentifier();
         
         List< String > fedoraPids = new ArrayList< String >();
         //List< String > searchFields = new ArrayList< String >( 1 );
         List< InputPair< String, String > > resultSearchFields = new ArrayList< InputPair< String, String > >();
         int maximumResults = 10000;
 
         if( ! types.contains( dcType ) )
         {
             log.debug( String.format( "finding work relations for dcType %s", dcType ) );
             if ( ! dcSource.equals( "" ) )
             {
                 log.debug( String.format( "1 WR with dcSource '%s' and dcTitle '%s'", dcSource, dcTitle ) );
                 //searchFields.add( "source" );
                 InputPair< String, String > searchPair = new InputPair< String, String >( "source", dcSource );                
                 resultSearchFields.add( searchPair );
                 fedoraPids = objectRepository.getIdentifiers( resultSearchFields, pid, maximumResults, workNamespace );
                 //fedoraPids = objectRepository.getIdentifiers( dcSource, searchFields, pid, maximumResults );
 
                 if ( fedoraPids.size() == 0 && ! dcTitle.equals( "" ) )
                 {
                     //searchFields.clear();
                     resultSearchFields.clear();
                     //searchFields.add( "title" );
                     searchPair = new InputPair< String, String >( "title", dcSource );
                     resultSearchFields.add( searchPair );
                     fedoraPids = objectRepository.getIdentifiers( resultSearchFields, pid, maximumResults, workNamespace );
                 }
             }
 
             if ( ( fedoraPids == null || fedoraPids.size() == 0 ) && ! dcTitle.equals( "" ) )
             {
                 log.debug( String.format( "2 WR with dcSource '%s' and dcTitle '%s'", dcSource, dcTitle ) );
                 if ( ! dcSource.equals( "" ) )
                 {
                     //searchFields.clear();
                     resultSearchFields.clear();
                     //searchFields.add( "source" );
                     InputPair< String, String > searchPair = new InputPair< String, String >( "source", dcTitle );
                     resultSearchFields.add( searchPair );
                     fedoraPids = objectRepository.getIdentifiers( resultSearchFields, pid, maximumResults, workNamespace );
                 }
                 else
                 {
                     //searchFields.clear();
                     resultSearchFields.clear();
                     //searchFields.add( "title" );
                     InputPair< String, String > searchPair = new InputPair<String, String>( "title", dcTitle );
                     resultSearchFields.add( searchPair );
                     fedoraPids = objectRepository.getIdentifiers( resultSearchFields, pid, maximumResults, workNamespace );
                 }
             }
 
             if ( fedoraPids == null || fedoraPids.size() == 0 )
             {
                 log.debug( String.format( "No matching posts found for '%s' or '%s'", dcTitle, dcSource ) );
             }
         }
         else
         {
             if ( ! ( dcTitle.equals( "" ) || dcCreator.equals( "" ) ) )
             {
                 log.debug( String.format( "WR with dcTitle '%s' and dcCreator '%s'", dcTitle, dcCreator ) );
                 //searchFields.clear();
                 resultSearchFields.clear();
                 InputPair< String, String > searchTitlePair = new InputPair<String, String>( "title", dcTitle );
                 InputPair< String, String > searchCreatorPair = new InputPair<String, String>( "creator", dcCreator );
                 resultSearchFields.add( searchTitlePair );
                 resultSearchFields.add( searchCreatorPair );
                 //searchFields.add( "title" );
                 //searchFields.add( "creator" );
 
                 /*List< InputPair< String, String > > searchList = new ArrayList< InputPair< String, String > >();
                 InputPair< String, String > pair = new InputPair< String, String >( dcTitle, dcSource );
                 searchList.add( pair );
                 searchList.add( pair );
                 fedoraPids = objectRepository.getIdentifiers( searchList, searchFields, pid, 10000 );*/
                 fedoraPids = objectRepository.getIdentifiers( resultSearchFields, pid, maximumResults, workNamespace );
             }
             else
             {
                 log.debug( String.format( "No matching posts found for '%s' or '%s'", dcTitle, dcCreator ) );
             }
         }
 
         if ( fedoraPids != null && fedoraPids.size() > 0 )
         {
             log.debug( String.format( "Pid with matching title, source, or creator = %s", fedoraPids.get( 0 ) ) );
         }
 
         PID workPid = null;       
 
         if ( fedoraPids == null || fedoraPids.size() == 0 )
         {
             log.debug( String.format("ja7w: No Work found creating new work ") );
             
             workPid = new PID( fedoraHandle.getNextPID( 1, "work" )[0] );
             log.debug( String.format( "nextWorkPid found: %s", workPid.getIdentifier()) );
             CreateWorkObject( workPid, dc );
         }
         else // fedoraPids.size() > 0
         {
             workPid = new PID(fedoraPids.get( 0 )); 
         }
                  
 
         log.debug( String.format( "Trying to add %s to the collection %s", cargo.getIdentifier(), workPid ) );
 
 
         this.objectRepository.addObjectRelation( workPid, DBCBIB.HAS_MANIFESTATION , cargo.getIdentifier() );
         this.objectRepository.addObjectRelation( new PID( cargo.getIdentifier()), DBCBIB.IS_MEMBER_OF_WORK, workPid.getIdentifier() );
         
         return true;
     }
 
 
     private void CreateWorkObject( PID nextWorkPid, DublinCore oldDc )
     {      
         try
         {
             // todo: Clean up work object xml and language.
             CargoContainer cargo = new CargoContainer( nextWorkPid.getIdentifier() );
             DublinCore workDC = new DublinCore( nextWorkPid.getIdentifier()  );
        
             ScriptEngine engine = manager.getEngineByName( "JavaScript" );            
             engine.put( "log", log );            
             engine.put( "objectRepository", objectRepository );
             
             String path = FileSystemConfig.getScriptPath();
             String jsFileName = path + "marcxchange_workrelation.js";
             
             log.debug( String.format( "ja7w: url = %s", jsFileName ) );
             engine.eval( new java.io.FileReader( jsFileName ) );
             
             Invocable inv = (Invocable)engine;
             
             engine.put( "plugin", this );
             Object res = inv.invokeFunction( "generate_new_work", cargo, oldDc, workDC );
             
             log.debug( String.format( "ja7w: res of type %s", res.toString() ) );
             String fakexml = (String)res;
             if (fakexml == null)
             {
                 throw new PluginException( "Internal error generate_new_work did not return a String" );
                 
             }
 
             cargo.add( DataStreamType.OriginalData, "format", "internal", "da", "text/xml", IndexingAlias.None, fakexml.getBytes() );
             cargo.addMetaData( workDC );
             
             try
             {
                 this.objectRepository.storeObject( cargo, "internal" );
                 log.debug(String.format( "ja7: added work object %s", nextWorkPid ) );
             }
             catch (Exception e)
             {
                 log.error( "ja7w:error in objectRepository.storeCargocontiner for new work item", e );
             }
         }
         catch ( Exception e) {
             log.error( "ja7w:error in fs.storeCargocontiner for new work item", e );
         }
     }
     
 
     @Override
     public PluginType getPluginType()
     {
         return pluginType;
     }
 
 
     @Override
     public void setObjectRepository( IObjectRepository objectRepository )
     {
         this.objectRepository = objectRepository;
     }
 }
