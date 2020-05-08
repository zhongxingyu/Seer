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
  * \file IndexerXSEM.java
  * \brief maps and indexes the cargoContainer
  */
 
 
 package dk.dbc.opensearch.plugins;
 
 import dk.dbc.opensearch.common.compass.CPMAlias;
 import dk.dbc.opensearch.common.fedora.IObjectRepository;
 import dk.dbc.opensearch.common.pluginframework.IPluggable;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.pluginframework.PluginType;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.xml.XMLUtils;
 import dk.dbc.opensearch.components.pti.PTIJobsMap;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.compass.core.CompassException;
 import org.compass.core.CompassSession;
 import org.compass.core.CompassTransaction;
 import org.compass.core.xml.AliasedXmlObject;
 import org.compass.core.xml.RawAliasedXmlObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 
 /**
  * The IndexerXSEM maps the given indexable datastreams in the
  * cargocontainer, and writes the resulting values in a document to
  * the index.
  */
 public class IndexerXSEM implements IPluggable
 {
     private static Logger log = Logger.getLogger( IndexerXSEM.class );
 
 
     PluginType pluginType = PluginType.INDEX;
 
     private IndexerXSEMEnvironment env = null;
 
    public IndexerXSEM( IObjectRepository repository )
     {
 	Map< String, String> tmpMap = new HashMap< String, String >();
 	env = (IndexerXSEMEnvironment)this.createEnvironment( repository, tmpMap );
     }
 
     /**
      * The main method of the IndexerXSEM plugin
      */
     public boolean index(CargoContainer cargo, CompassSession session, String fedoraHandle ) throws PluginException, ConfigurationException
     {
         boolean success = false;
 
         try
         {
             success = index( session, cargo, fedoraHandle );
         }
         catch( CompassException ce )
         {
             String error = String.format( "Could not commit index on CompassSession: %s", ce.getMessage() );
             log.error( error );
             throw new PluginException( error, ce );
         }
         return success;
     }
 
     private boolean index( CompassSession session, CargoContainer cc, String fedoraHandle ) throws PluginException, CompassException, ConfigurationException
     {
         boolean success = false;
 
         log.debug( String.format( "Trying to read CargoContainer (pid= %s ) data from .getData into a dom4j.Document type", fedoraHandle ) );
 
         CPMAlias cpmAlias = null;
         log.debug( String.format( "number of streams in cc: %s", cc.getCargoObjectCount() ) );
         List< CargoObject > list = cc.getCargoObjects();
         try
         {
             cpmAlias = new CPMAlias();
         }
         catch( ParserConfigurationException pce )
         {
             String error = String.format(String.format( "Could not construct CPMAlias object for reading/parsing xml.cpm file -- values used for checking cpm aliases: %s", pce.getMessage() ) );
             log.error(  error, pce );
             throw new PluginException( error, pce );
         }
         catch (SAXException se)
         {
             String error = String.format( "Could not parse XSEM mappings file: %s", se.getMessage() );
             log.error( error, se );
             throw new PluginException( error, se );
         }
         catch ( IOException ioe )
         {
             String error = String.format( "Could open or read XSEM mappings file: %s", ioe.getMessage() );
             log.error( error, ioe );
             throw new PluginException( error, ioe );
         }
 
         log.info( "cpmAlias constructed" );
 
         for( CargoObject co : list )
         {
             if( ! ( co.getDataStreamType() == DataStreamType.OriginalData ) )
             {
                 log.debug( String.format( "Not indexing data with datastreamtype '%s'",co.getDataStreamType() ) );
             }
             else {
 
                 //String indexingAlias = co.getIndexingAlias(); 
              
                 String indexingAlias = ""; 
                 try
                 {
                     indexingAlias = PTIJobsMap.getAlias( co.getSubmitter(), co.getFormat() );               
                 }
                 catch( IOException ioe )
                 { 
                     String error = String.format( "Could not get the alias: %s", ioe.getMessage() );
                     log.error( error, ioe );
                     throw new PluginException( error, ioe );   
                 }
                 catch( SAXException se )
                 { 
                     String error = String.format( "Could not get the alias: %s", se.getMessage() );
                     log.error( error, se );
                     throw new PluginException( error, se );   
                 }
                 catch( ParserConfigurationException pce )
                 { 
                     String error = String.format( "Could not get the alias: %s", pce.getMessage() );
                     log.error( error, pce );
                     throw new PluginException( error, pce );   
                 }
                 boolean isValidAlias = false;
                 try 
                 {
                     isValidAlias = cpmAlias.isValidAlias( indexingAlias );
                 }
                 catch ( ParserConfigurationException pce ) 
                 {
                     String error = String.format( "Could not contruct the objects for reading/parsing the configuration file for the XSEM mappings: %s" , pce.getMessage() );
                     log.error( error, pce );
                     throw new PluginException( error, pce );
                 }
                 catch ( SAXException se ) 
                 {
                     String error = String.format( "Could not parse XSEM mappings file: %s", se.getMessage() );
                     log.error( error, se );
                     throw new PluginException( error, se );
                 }
                 catch (IOException ioe) 
                 {
                     String error = String.format( "Could open or read XSEM mappings file: %s", ioe.getMessage() );
                     log.error( error, ioe );
                     throw new PluginException( error, ioe );
                 }
 
                 if( ! isValidAlias )
                 {
                     String error = String.format( "The format %s (from pid %s) has no alias in the XSEM mapping file", indexingAlias, cc.getIdentifier() );
                     log.error( error );
                     throw new PluginException( String.format( "The format %s has no alias in the XSEM mapping file", indexingAlias ) );
                 }
                 else
                 {
                     byte[] bytes = co.getBytes();
                     log.debug( String.format( "altered xml: %s", new String( bytes ) ) );
                     ByteArrayInputStream is = new ByteArrayInputStream( bytes );
                     Document doc = null;
                     DocumentBuilder docBuilder = null;
                     try
                     {
                         docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                         doc = docBuilder.parse( is );
                     } 
                     catch (SAXException de) 
                     {
                         String error = String.format( "Could not parse InputStream as an XML Instance from alias=%s, mimetype=%s, pid=%s. Error: %s", indexingAlias, co.getMimeType(), cc.getIdentifier(), de.getMessage() );
                         log.error( error, de );
                         throw new PluginException( error, de );
                     } 
                     catch( IOException ioe)
                     {
                         String error = String.format( "Error reading xml stream: %s", ioe.getMessage() );
                         log.error( error, ioe );
                         throw new PluginException( error, ioe );
                     } 
                     catch( ParserConfigurationException pce )
                     {
                         String error = String.format( "Error reading xml stream: %s", pce.getMessage() );
                         log.error( error, pce );
                         throw new PluginException( error, pce );
                     }
 
                     /** \todo: when doing this the right way, remember to modify the initial value of the HashMap*/
 
                     // Add aditional values to document
                     HashMap< String, String> fieldMap = new HashMap< String, String >( 3 );
                     log.debug( String.format( "Initializing new fields for the index" ) );
                     fieldMap.put( "ting:fedoraPid", fedoraHandle );
                     fieldMap.put( "ting:fedoraNormPid", fedoraHandle.replaceAll( ":", "_" ) );
                     fieldMap.put( "ting:original_format", co.getFormat() );
                     fieldMap.put( "ting:submitter", co.getSubmitter() );
 
                     Element root = doc.getDocumentElement();
 
                     for( String key : fieldMap.keySet() )
                     {
                         log.debug( String.format( "Setting new index field '%s' to '%s'", key, fieldMap.get( key ) ) );
                         Element newElement = doc.createElement( key );
                         newElement.setTextContent( fieldMap.get( key ) );
                         root.appendChild( newElement );
                     }
 
                     // this log line is _very_ verbose, but useful in a tight situation
                     // try{
                     //     log.trace( String.format( "Constructing AliasedXmlObject from Document (pid = %s) with alias = %s. RootElement:\n%s", fedoraHandle, indexingAlias, XMLUtils.xmlToString( doc ) ) );
                     // }catch(TransformerException te){ System.err.println( String.format( "Caught Exception: %s", te.getMessage() ) );}
 
                     AliasedXmlObject xmlObject = null;
                     try 
                     {
                         xmlObject = new RawAliasedXmlObject( indexingAlias,  XMLUtils.xmlToString( doc )  );
                     }
                     catch(TransformerException te)
                     {
                         String error = String.format( "Could not transform xml data %s", te.getMessage() );
                         log.error( error, te );
                         throw new PluginException( error, te );
                     }
 
                     log.info( String.format( "Constructed AliasedXmlObject with alias %s", xmlObject.getAlias() ) );
 
                     
                     // getting transaction object and saving index
                     log.trace( String.format( "Getting transaction object" ) );
                     CompassTransaction trans = null;
 
                     try
                     {
                         log.trace( "Beginning transaction" );
                         trans = session.beginTransaction();
                     }catch( CompassException ce )
                     {
                         String error = String.format( "Could not initiate transaction on the CompassSession: %s", ce.getMessage() );
                         log.error( error, ce );
                         throw new PluginException( error, ce );
                     }
                     
                     try
                     {
                         log.debug( String.format( "Saving Compass Resource '%s' with new fields to index", xmlObject.getAlias() ) );
 
                         session.save( xmlObject );
                         
                     }
                     catch( CompassException ce ){
                         try
                         {
                             String error = String.format( "Could not save index object (alias=%s, pid=%s) to index. Cause: %s, message: %s, xml='''%s''' ", xmlObject.getAlias(), fedoraHandle, ce.getCause(), ce.getMessage(), XMLUtils.xmlToString( doc ) );
                             log.error( error, ce );
                             throw new PluginException( error, ce );
                         }
                         catch( TransformerException ex )
                         {
                             String error = String.format( "Could not log exception %s: %s",ce.getMessage(), ex.getMessage() );
                             log.error( error, ce );
                             throw new PluginException( error, ce );
                         }
                     }
                     
                     log.trace( "Committing index on transaction" );
                     trans.commit();
                     log.debug( "Transaction was committed successfully" );
                     /** todo: does trans.wasCommitted have any side-effects? Such as waiting for the transaction to finish before returning?*/
                     log.debug( String.format( "Transaction wasCommitted() == %s", trans.wasCommitted() ) );
                     session.close();
                     
                     log.info( String.format( "Document indexed and stored with Compass" ) );
                     success = true;
                 }
             }
         }
         return success;
     }
 
 
     /**
      *
      */
     @Override
     public PluginType getPluginType()
     {
         return pluginType;
     }
 
     /**
      * A dummy method to satisfy the interface until we refactor this 
      * class that is bound to e obsolete soon 
      * \Todo: bug 10479
      */
     @Override
     public synchronized CargoContainer runPlugin( CargoContainer cargo, Map<String, String> argsMap ) throws PluginException
     {
         return null;
     }
 
 
     public static IPluginEnvironment createEnvironment( IObjectRepository repository, Map< String, String > args ) throws PluginException
     {
     	return new IndexerXSEMEnvironment( repository, args );
     }
 }
