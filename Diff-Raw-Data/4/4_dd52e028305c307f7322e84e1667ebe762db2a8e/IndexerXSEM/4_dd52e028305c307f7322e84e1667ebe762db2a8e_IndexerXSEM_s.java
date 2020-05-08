 /**
  * \file IndexerXSEM.java
  * \brief Indexing using xsem
  * \package dk.dbc.opensearch.plugins;
  */
 package dk.dbc.opensearch.plugins;
 
 
 /**
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
 
 
 import dk.dbc.opensearch.common.compass.CPMAlias;
 import dk.dbc.opensearch.common.pluginframework.IIndexer;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.pluginframework.PluginType;
 import dk.dbc.opensearch.common.statistics.IEstimate;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.compass.core.CompassException;
 import org.compass.core.CompassSession;
 import org.compass.core.CompassTransaction;
 import org.compass.core.Resource;
 import org.compass.core.xml.AliasedXmlObject;
 import org.compass.core.xml.dom4j.Dom4jAliasedXmlObject;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.dom.DOMElement;
 import org.dom4j.io.SAXReader;
 import org.xml.sax.SAXException;
 import dk.dbc.opensearch.common.types.DataStreamType;
 
 
 public class IndexerXSEM implements IIndexer
 {
     Logger log = Logger.getLogger( IndexerXSEM.class );
 
 
     PluginType pluginType = PluginType.INDEX;
 
 
     public PluginType getTaskName()
     {
         return pluginType;
     }
 
 
     public long getProcessTime(CargoContainer cargo, CompassSession session, String fedoraHandle, IEstimate estimate ) throws PluginException, ConfigurationException
     {
         long processTime = 0;
         try
         {
             processTime = getProcessTime( session, cargo, fedoraHandle, estimate );
         }
         catch( CompassException ce )
         {
             throw new PluginException( "Could not commit index on CompassSession", ce );
         }
 
         return processTime;
     }
 
 
     private long getProcessTime( CompassSession session, CargoContainer cc, String fedoraHandle, IEstimate estimate ) throws PluginException, CompassException, ConfigurationException
     {
         long processTime = 0;
         Date finishTime = new Date();
 
         /* \todo: right now we index all stream in a cc with the same alias. each CargoObject should have a IndexingAlias. see bug #8719 */
         //String indexingAlias = cc.getIndexingAlias().getName();
 
         // Construct doc and Start Transaction
         log.debug( "Starting transaction on running CompassSession" );
         log.debug( String.format( "Trying to read CargoContainer (pid= %s ) data from .getData into a dom4j.Document type", fedoraHandle ) );
         SAXReader saxReader = new SAXReader();
 
         CPMAlias cpmAlias = null;
         log.debug( String.format( "number of streams in cc: %s", cc.getCargoObjectCount() ) );
         List< CargoObject > list = cc.getCargoObjects();
         try 
         {
             cpmAlias = new CPMAlias();
         } 
         catch( ParserConfigurationException pce ) 
         {
             log.fatal( String.format( "Could not construct CPMAlias object for reading/parsing xml.cpm file -- values used for checking cpm aliases" + pce ) );
             throw new PluginException( String.format( "Could not construct CPMAlias object for reading/parsing xml.cpm file -- values used for checking cpm aliases" ), pce );
         } 
         catch (SAXException se) 
         {
             log.fatal( String.format( "Could not parse XSEM mappings file" + se ) );
             throw new PluginException( String.format( "Could not parse XSEM mappings file" ), se );
         } 
         catch ( IOException ioe )
         {
             log.fatal( String.format( "First: Could not open or read XSEM mappings file: " + ioe ) );
             throw new PluginException( String.format( "First exception Could not open or read XSEM mappings file" ), ioe );
         }
         log.info( "cpmAlias constructed" );
 
         for( CargoObject co : list )
         {
             if( ! ( co.getDataStreamName() == DataStreamType.OriginalData ) ) 
             {
                 log.info( String.format( "Not indexing data with datastreamtype '%s'",co.getDataStreamName() ) );
             }
             else {
 
                 String indexingAlias = co.getIndexingAlias().getName();
                 boolean isValidAlias = false;
                 try {
                     isValidAlias = cpmAlias.isValidAlias( indexingAlias );
                 } 
                 catch ( ParserConfigurationException pce ) {
                     log.fatal( String.format( "Could not contruct the objects for reading/parsing the configuration file for the XSEM mappings" ), pce );
                     throw new PluginException( String.format( "Could not contruct the objects for reading/parsing the configuration file for the XSEM mappings" ), pce );
                 } 
                 catch ( SAXException se ) {
                     log.fatal( String.format( "Could not parse XSEM mappings file" ), se );
                     throw new PluginException( String.format( "Could not parse XSEM mappings file" ), se );
                 } 
                 catch (IOException ioe) {
                     log.fatal( String.format( "Second: Could not open or read XSEM mappings file" ), ioe );
                     throw new PluginException( String.format( "Second Exception Could not open or read XSEM mappings file" ), ioe );
                 }
 
                 if( ! isValidAlias )
                 {
                     log.fatal( String.format( "The format %s has no alias in the XSEM mapping file", indexingAlias ) );
                     throw new PluginException( String.format( "The format %s has no alias in the XSEM mapping file", indexingAlias ) );
                 }
                 else
                 {
                     byte[] bytes = co.getBytes();
                     log.debug( String.format( "altered xml: %s", new String( bytes ) ) );
                     ByteArrayInputStream is = new ByteArrayInputStream( bytes );
                     Document doc = null;
                     try {
                         doc = saxReader.read( is );
                     } catch (DocumentException de) {
                         log.fatal( String.format( "While parsing xml: %s, I caught exception from SAX Parsing : %s", new String( co.getBytes() ), de.getMessage() ) );
                         throw new PluginException( String.format( "Could not parse InputStream as an XML Instance from alias=%s, mimetype=%s", indexingAlias, co.getMimeType() ), de );
                     }
                     /** \todo: when doing this the right way, remember to modify the initial value of the HashMap*/
                    HashMap< String, String> fieldMap = new HashMap< String, String >( 2 );
                     log.debug( String.format( "Initializing new fields for the index" ) );
                     fieldMap.put( "fedoraPid", fedoraHandle );
                     fieldMap.put( "original_format", co.getFormat() );
 
                     Element root = doc.getRootElement();
 
                     for( String key : fieldMap.keySet() )
                     {
                         log.debug( String.format( "Setting new index field '%s' to '%s'", key, fieldMap.get( key ) ) );
                         Element newElement = new DOMElement( key ).addText( fieldMap.get( key ) );
                         root.add( newElement );
                     }
 
                     // this log line is _very_ verbose, but useful in a tight situation
                     log.debug( String.format( "Constructing AliasedXmlObject from Document (pid = %s) with alias = %s. RootElement:\n%s", fedoraHandle, indexingAlias, doc.getRootElement().asXML() ) );
 
                     /** \todo: Dom4jAliasedXmlObject constructor might throw some unknown exception */
                     AliasedXmlObject xmlObject = new Dom4jAliasedXmlObject( indexingAlias, doc.getRootElement() );
                     //AliasedXmlObject xmlObject = new Dom4jAliasedXmlObject( co.getFormat(), doc.getRootElement() );
                     log.info( String.format( "Constructed AliasedXmlObject with alias %s", xmlObject.getAlias() ) );
 
                     // getting transaction object and saving index
                     log.debug( String.format( "Getting transaction object" ) );
                     CompassTransaction trans = null;
 
                     try
                     {
                         log.debug( "Beginning transaction" );
                         trans = session.beginTransaction();
                     }catch( CompassException ce )
                     {
                         log.fatal( String.format( "Could not initiate transaction on the CompassSession" ) );
                         throw new PluginException( "Could not initiate transaction on the CompassSession", ce );
                     }
 
                     /** \todo: when doing this the right way, remember to modify the initial value of the HashMap*/
                     //HashMap< String, String> fieldMap = new HashMap< String, String >( 2 );
                     //log.debug( String.format( "Initializing new fields for the index" ) );
                     //fieldMap.put( "fedoraPid", fedoraHandle );
                     //fieldMap.put( "original_format", co.getFormat() );
 
                     //for( String key : fieldMap.keySet() )
                     //{
                     //    log.debug( String.format( "Setting new index field '%s' to '%s'", key, fieldMap.get( key ) ) );
                     //    LuceneProperty newField = new LuceneProperty( new Field( key, new StringReader( fieldMap.get( key ) ) ) );
                     //    resource.addProperty( newField );
                     //}
 
                     try
                     {
                         log.debug( String.format( "Saving Compass Resource '%s' with new fields to index", xmlObject.getAlias() ) );
                         session.save( xmlObject );
                     }
                     catch( CompassException ce ){
                         log.fatal( String.format( "Could not save index object (alias=%s, pid=%s) to index. Cause: %s, message: %s, xml='''%s''' ",
                                                   xmlObject.getAlias(), fedoraHandle, ce.getCause(), ce.getMessage(), doc.getRootElement().asXML() ) );
                         throw new PluginException( String.format( "Could not save index object to index. Cause: %s, message: %s ",
                                                                   ce.getCause(), ce.getMessage() ), ce );
                     }
 
                     log.debug( "Committing index on transaction" );
 
                     trans.commit();
 
                     /** todo: does trans.wasCommitted have any side-effects? Such as waiting for the transaction to finish before returning?*/
                     log.debug( String.format( "Transaction wasCommitted() == %s", trans.wasCommitted() ) );
                     session.close();
 
                     log.info( String.format( "Document indexed and stored with Compass" ) );
 
                     log.debug( "Obtain processtime, and writing statistics into the database" );
 
                     processTime += finishTime.getTime() - co.getTimestamp();
 
                     updateEstimationDB(  co, processTime, estimate );
                 }
             }
 
         }
 
         return processTime;
     }
 
     /**
      * Adds fields to a Compass index. New fields are given in the
      * HashMap, which contains the field names resp. the field values.
      *
      * @param sess The compass session which we are operating on
      * @param xmlObj The AliasedXmlObject that we retrieve our index from
      * @param fieldMap HashMap containing the field names resp. field values that are to be written to the index
      *
      * @return the updated index as a Compass Resource
      *
      * Please note that the AliasedXmlObject is deleted completely
      * from the session and the returned Resource takes its place and
      * contains its information. See the todo in the code.
      */
     private Resource updateAliasedXmlObject( CompassSession sess, AliasedXmlObject xmlObj, HashMap< String, String> fieldMap )
     {
 
         String alias = xmlObj.getAlias();
 
         log.debug( String.format( "Preparing for insertion of new fields in index with alias %s", alias ) );
 
         Resource xmlObj2 = sess.loadResource( alias, xmlObj );
 
         // \todo do we need to remove the xmlObject from the index?
         log.debug( String.format( "Deleting old xml object" ) );
 
 
 
         return xmlObj2;
     }
 
     /**
      *
      */
     private void updateEstimationDB( CargoObject co, long processTime, IEstimate estimate ) throws PluginException
     {
         
 
         // updating the database with the new estimations
         try
         {
             estimate.updateEstimate( co.getMimeType(), co.getContentLength(), processTime );
 
             log.info( String.format("Updated estimate with mimetype = %s, streamlength = %s, processtime = %s",
                                     co.getMimeType(), co.getContentLength(), processTime ) );
         }
         catch( SQLException sqle )
         {
             log.fatal( String.format( "Could not update database with estimation %s", processTime ), sqle );
             throw new PluginException( String.format( "Could not update database with estimation %s", processTime ), sqle );
         }
         catch( ClassNotFoundException cnfe )
         {
             log.fatal( "Could not configure database in Estimation class", cnfe );
             throw new PluginException( "Could not configure database in Estimation class", cnfe );
         }
     }
 }
