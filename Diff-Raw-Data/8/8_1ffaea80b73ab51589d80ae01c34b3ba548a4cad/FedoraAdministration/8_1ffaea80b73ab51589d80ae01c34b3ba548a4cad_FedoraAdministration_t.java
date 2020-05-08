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
  * \file   FedoraAdministration.java
  * \brief  The class for administrating the Fedora Commons repository
  */
 
 package dk.dbc.opensearch.common.fedora;
 
 import java.io.IOException;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.StringWriter;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 
 import java.net.MalformedURLException;
 import java.rmi.RemoteException;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.rpc.ServiceException;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Source;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.Result;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerConfigurationException;
 
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.fedora.PIDManager;
 import dk.dbc.opensearch.common.helpers.XMLFileReader;
 import dk.dbc.opensearch.common.types.IndexingAlias;
 
 import dk.dbc.opensearch.xsd.Datastream;
 import dk.dbc.opensearch.xsd.DatastreamVersion;
 import dk.dbc.opensearch.xsd.DatastreamVersionTypeChoice;
 import dk.dbc.opensearch.xsd.types.DatastreamTypeCONTROL_GROUPType;
 import dk.dbc.opensearch.xsd.types.StateType;
 
 import fedora.server.types.gen.RelationshipTuple;
 import fedora.server.types.gen.MIMETypedStream;
 import org.exolab.castor.xml.MarshalException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.exolab.castor.xml.ValidationException;
 import java.text.ParseException;
 import javax.xml.transform.TransformerException;
 
 public class FedoraAdministration extends FedoraHandle implements IFedoraAdministration
 {
 
     Logger log = Logger.getLogger( FedoraAdministration.class );
     //private FedoraCommunication fedoraCommunication;
     private PIDManager pidManager;
 
     /**
      * The constructor initalizes the super class FedoraHandle which
      * handles the initiation of the connection with the fedora base
      *
      * @throws ConfigurationException  Thrown if the FedoraHandler Couldn't be initialized. @see FedoraHandle
      * @throws IOException Thrown if the FedoraHandler Couldn't be initialized properly. @see FedoraHandle
      * @throws MalformedURLException Thrown if the FedoraHandler Couldn't be initialized properly. @see FedoraHandle
      * @throws ServiceException Thrown if the FedoraHandler Couldn't be initialized properly. @see FedoraHandle
      */
 
     public FedoraAdministration()throws ConfigurationException, IOException, MalformedURLException, ServiceException
     {
         super();
         pidManager = new PIDManager();
         log.debug( "constructed" );
     }
     /**
      * method to delete an object for good, based on the pid
      * @param pid, the identifier of the object to be removed
      * @param force, tells whether to purge the object even if it
      * breaks dependencies to other objects
      * @throws RemoteException if something on the serverside goes wrong.
      */
     public void deleteObject( String pid, boolean force ) throws RemoteException
     {
         String logm = "";
         super.fem.purgeObject( pid, logm, force );
 
     }
 
     /**
      * method for setting the delete flag on an object
      * @param pid, the identifier of the object to be marked as delete
      * @return true if the DigitalObject is marked
      */
    public boolean markObjectAsDeleted( String pid )
     {
         return true;
     }
 
     /**
      * method for getting an object in a CargoContainer based on its pid
      * @param pid, the identifier of the object to get
      * @return the CargoContainer representing the DigitalObject
      * @throws RemoteException if something on the serverside goes wrong.
      */
    public CargoContainer getDigitalObject( String pid ) throws IOException, ParserConfigurationException, RemoteException, SAXException
     {
 
         log.debug( String.format( "entering getDO( '%s' )", pid ) );
 
         MIMETypedStream ds = super.fea.getDatastreamDissemination( pid, DataStreamType.AdminData.getName(), null );
         byte[] adminStream = ds.getStream();
         log.debug( String.format( "Got adminstream from fedora == %s", new String( adminStream ) ) );
 
         CargoContainer cc = new CargoContainer();
 
         ByteArrayInputStream bis = new ByteArrayInputStream( adminStream );
         log.debug( String.format( "Trying to get root element from adminstream with length %s", bis.available() ) );
         Element root = XMLFileReader.getDocumentElement( new InputSource( bis ) );
 
         log.debug( String.format( "root element from adminstream == %s", root ) );
 
         //Element indexingAliasElem = (Element)root.getElementsByTagName( "indexingalias" );
 
         NodeList indexingAliasElem = root.getElementsByTagName( "indexingalias" );
         if( indexingAliasElem == null )
         {
             /**
              * \Todo: this if statement doesnt skip anything. What should we do? bug: 8878
              */
             log.error( String.format( "Could not get indexingalias from adminstream, skipping " ) );
         }
         log.debug( String.format( "indexingAliasElem == %s", indexingAliasElem.item(0) ) );
         String indexingAliasName = ((Element)indexingAliasElem.item( 0 )).getAttribute( "name" );
         log.debug( String.format( "Got indexingAlias = %s", indexingAliasName ) );
         //Element filePathElem = (Element)root.getElementsByTagName( "filepath" ).item( 0 );
         //String filePath = filePathElem.getAttribute( "name" );
         //cc.setFilePath( filePath );
         //log.info( String.format( "The filepath of the file to index: %s ", filePath ) );
 
         NodeList streamsNL = root.getElementsByTagName( "streams" );
         Element streams = (Element)streamsNL.item(0);
         NodeList streamNL = streams.getElementsByTagName( "stream" );
         log.debug( String.format( "Iterating streams in nodelist" ) );
 
         int streamNLLength = streamNL.getLength();
         for(int i = 0; i < streamNLLength; i++ )
         {
             Element stream = (Element)streamNL.item(i);
             String streamID = stream.getAttribute( "id" );
             MIMETypedStream dstream = super.fea.getDatastreamDissemination( pid, streamID, null);
             cc.add( DataStreamType.getDataStreamNameFrom( stream.getAttribute( "streamNameType" ) ),
                     stream.getAttribute( "format" ),
                     stream.getAttribute( "submitter" ),
                     stream.getAttribute( "lang" ),
                     stream.getAttribute( "mimetype" ),
                     IndexingAlias.getIndexingAlias( indexingAliasName ),
                     dstream.getStream() );
         }
         return cc;
     }
 
     /**
      * method for storing an object in the Fedora base
      * @param theCC the CargoContainer to store
      * @param label, the label to put on the object
      * @return the pid of the object in the repository, null if unsuccesfull
      */
    public String storeCargoContainer( CargoContainer theCC, String label ) throws MalformedURLException, RemoteException, IOException, SAXException, ServiceException, MarshalException, ValidationException, ParseException, ParserConfigurationException, TransformerException
     {
         log.debug( "entering storeCC" );
 
         String returnVal = null;
         //get a submitter from the cc
         String submitter = theCC.getCargoObject( DataStreamType.OriginalData ).getSubmitter();
         //get a pid for the object
         String pid = pidManager.getNextPID( submitter );
         byte[] foxml = FedoraTools.constructFoxml( theCC, pid, label );
         String logm = String.format( "%s inserted", label );
 
         String returnPid = super.fem.ingest( foxml, "info:fedora/fedora-system:FOXML-1.1", logm );
         if( pid.equals( returnPid ) )
         {
             returnVal = pid;
         }
 
         return returnVal;
 
     }
 
     /**
      * method to retrive all DataStreams of a DataStreamType from an object
      * @param pid, identifies the object
      * @param streamtype, the name of the type of DataStream to get
      * @return a CargoContainer of CargoObjects each containing a DataStream,
      * is null if there are no DataStreams of the streamtype.
      */
     public CargoContainer getDataStreamsOfType( String pid, DataStreamType streamtype ) throws MalformedURLException, IOException, RemoteException, ParserConfigurationException, SAXException
     {
         CargoContainer cc = new CargoContainer();
         //get the adminstream
         MIMETypedStream ds = super.fea.getDatastreamDissemination( pid,DataStreamType.AdminData.getName(), null );
         byte[] adminStream = ds.getStream();
         log.debug( String.format( "Got adminstream from fedora == %s", new String( adminStream ) ) );
         ByteArrayInputStream bis = new ByteArrayInputStream( adminStream );
         log.debug( String.format( "Trying to get root element from adminstream with length %s", bis.available() ) );
         Element root = XMLFileReader.getDocumentElement( new InputSource( bis ) );
 
         log.debug( String.format( "root element from adminstream == %s", root ) );
 
         //Element indexingAliasElem = (Element)root.getElementsByTagName( "indexingalias" );
 
         NodeList indexingAliasElem = root.getElementsByTagName( "indexingalias" );
         if( indexingAliasElem == null )
         {
             /**
              * \Todo: this if statement doesnt skip anything. What should we do? bug: 8878
              */
             System.out.println("no indexing alias");
 
             log.error( String.format( "Could not get indexingalias from adminstream, skipping " ) );
         }
         String indexingAliasName = ((Element)indexingAliasElem.item( 0 )).getAttribute( "name" );
         NodeList streamsNL = root.getElementsByTagName( "streams" );
         Element streams = (Element)streamsNL.item(0);
         NodeList streamNL = streams.getElementsByTagName( "stream" );
         log.debug( "iterating streams in nodelist to get the right streamtype" );
 
         int length = streamNL.getLength();
         System.out.println( String.format( "NodeList is %s long", length ) );
         for( int i = 0; i < length; i++ )
         {
             Element stream = (Element)streamNL.item(i);
             String typeOfStream = stream.getAttribute( "streamNameType" );
             System.out.println( String.format( "got streamType: %s", typeOfStream ) );
             if( typeOfStream.equals( streamtype.getName() ) )
             {
 
                 System.out.println( "got the right streamType" );
                 //build the CargoObject and add it to the list
                 String streamID = stream.getAttribute( "id");
                 System.out.println( String.format( "streamid: %s", streamID ) );
                 MIMETypedStream dstream = super.fea.getDatastreamDissemination( pid, streamID, null );
                 byte[] bytestream = dstream.getStream();
 
                 cc.add( streamtype,
                         stream.getAttribute( "format" ),
                         stream.getAttribute( "lang" ),
                         stream.getAttribute( "submitter" ),
                         stream.getAttribute( "mimetype" ),
                         IndexingAlias.getIndexingAlias( indexingAliasName ),
                         bytestream );
             }
         }
 
 
         return cc;
     }
 
     //create another method for getting a datastream form a DO identified by the streamID.
 
     /**
      * method for getting a datastream identified by its streamID
      * @param streamID, the identifier of the datastream to be retrieved
      * @param pid, the identifier of the object to get the stream from
      * @return CargoContainer with the datastream
      */
 
     public CargoContainer getDataStream( String streamID, String pid ) throws MalformedURLException, IOException, RemoteException, ParserConfigurationException, SAXException
     {
         CargoContainer cc = new CargoContainer();
         //get the adminstream
         MIMETypedStream ds = super.fea.getDatastreamDissemination( pid,DataStreamType.AdminData.getName(), null );
         byte[] adminStream = ds.getStream();
         log.debug( String.format( "Got adminstream from fedora == %s", new String( adminStream ) ) );
         ByteArrayInputStream bis = new ByteArrayInputStream( adminStream );
         log.debug( String.format( "Trying to get root element from adminstream with length %s", bis.available() ) );
         Element root = XMLFileReader.getDocumentElement( new InputSource( bis ) );
 
         log.debug( String.format( "root element from adminstream == %s", root ) );
 
         //Element indexingAliasElem = (Element)root.getElementsByTagName( "indexingalias" );
 
         NodeList indexingAliasElem = root.getElementsByTagName( "indexingalias" );
         if( indexingAliasElem == null )
         {
             /**
              * \Todo: this if statement doesnt skip anything. What should we do? bug: 8878
              */
             log.error( String.format( "Could not get indexingalias from adminstream, skipping " ) );
         }
         String indexingAliasName = ((Element)indexingAliasElem.item( 0 )).getAttribute( "name" );
         NodeList streamsNL = root.getElementsByTagName( "streams" );
         Element streams = (Element)streamsNL.item(0);
         NodeList streamNL = streams.getElementsByTagName( "stream" );
         log.debug( "iterating streams in nodelist to get info" );
 
         int length = streamNL.getLength();
         for( int i = 0; i < length; i++ )
         {
             Element stream = (Element)streamNL.item(i);
             String idOfStream = stream.getAttribute( "id" );
             if( streamID.equals( idOfStream ) )
             {
                 //build the CargoObject and add it to the list
                 MIMETypedStream dstream = super.fea.getDatastreamDissemination( pid, streamID, null );
                 byte[] bytestream = dstream.getStream();
 
                 cc.add( DataStreamType.getDataStreamNameFrom( stream.getAttribute( "streamNameType" ) ),
                         stream.getAttribute( "format" ),
                         stream.getAttribute( "lang" ),
                         stream.getAttribute( "submitter" ),
                         stream.getAttribute( "mimetype" ),
                         IndexingAlias.getIndexingAlias( indexingAliasName ),
                         bytestream );
             }
         }
         return cc;
     }
     /**
      * method for adding a Datastream to an object
      * @param theFile, the file to save as a DataStream in a specified object
      * @param pid, the identifier of the object to save the datastream to
      * @param label the label to give the stream
      * @param versionable, tells whether to keep track of old versions or not
      * @param overwrite, tells whether to overwrite if the datastream exists
      * @return the dataStreamID of the added stream
      */
     public String addDataStreamToObject( File theFile, String pid, String label, boolean versionable, String mimetype, boolean overwrite, String format, String lang, String submitter, DataStreamType dsn ) throws RemoteException, MalformedURLException, ParserConfigurationException, TransformerConfigurationException, TransformerException, SAXException, IOException
     {
         String sID = null;
         String logm = "";
         String adminLogm = "";
         String dsnName = dsn.getName();
       
         //10: get the adminstream
         MIMETypedStream ds = super.fea.getDatastreamDissemination( pid,DataStreamType.AdminData.getName(), null );
         byte[] adminStream = ds.getStream();
         log.debug( String.format( "Got adminstream from fedora == %s", new String( adminStream ) ) );
         ByteArrayInputStream bis = new ByteArrayInputStream( adminStream );
         log.debug( String.format( "Trying to get root element from adminstream with length %s", bis.available() ) );
         Element root = XMLFileReader.getDocumentElement( new InputSource( bis ) );
 
         log.debug( String.format( "root element from adminstream == %s", root ) );
 
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document admStream = builder.newDocument();
 
         Element newRoot = (Element)admStream.importNode( (Node)root, true );
         //Element newRoot = admStream.getDocumentElement();
 
         NodeList streamsNL = newRoot.getElementsByTagName( "streams" );
         Element streams = (Element)streamsNL.item( 0 );
         NodeList streamNL = streams.getElementsByTagName( "stream" );
         //iterate streamNL to get num of streams with dsnName as streamNameType
 
         Element oldStream;
         int count = 0;
         int streamNLLength = streamNL.getLength();
         
         //need to loop streams to get num of this type to create valid id
         for( int i = 0; i < streamNLLength; i++ )
         {
             oldStream = (Element)streamNL.item( i );
             if( oldStream.getAttribute( "streamNameType" ).equals( dsnName ) )
             {
                 count++;
             }
         }
 
         // 14: create a streamId
         if( sID == null )
         {
             sID = dsnName + "." + count;
         }
         Element stream = admStream.createElement( "stream" );
         
         stream.setAttribute( "id", sID );
         stream.setAttribute( "lang",lang );
         stream.setAttribute( "format", format );
         stream.setAttribute( "mimetype", mimetype );
         stream.setAttribute( "submitter",submitter );
         stream.setAttribute( "index", String.valueOf( count ) );
         stream.setAttribute( "streamNameType" , dsnName );
         
         // 15:add data for the new stream
         streams.appendChild( (Node) stream );
 
         // 18: make it into a String
         Source source = new DOMSource((Node) newRoot );
         StringWriter stringWriter = new StringWriter();
         File admFile = new File( "admFile" );
         admFile.deleteOnExit();
 
         Result result = new StreamResult( admFile );
         Result stringResult = new StreamResult( stringWriter );//debug
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         Transformer transformer = transformerFactory.newTransformer();
         transformer.transform(source, result);
         transformer.transform(source, stringResult);
         //debug
         String admStreamString = stringWriter.getBuffer().toString();
         System.out.println( admStreamString );
 
         // 20:use modify by reference
         String adminLabel= "admin [text/xml]";
         String adminMime = "text/xml";
         SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.S" );
         String timeNow = dateFormat.format( new Date( System.currentTimeMillis() ) );
         adminLogm = "admin stream updated with added stream data" + timeNow;
 
         //upload the admFile
         String admLocation = super.fc.uploadFile( admFile );
 
         super.fem.modifyDatastreamByReference( pid, DataStreamType.AdminData.getName(), new String[] {}, adminLabel, adminMime, null, admLocation, null, null, adminLogm, true );
 
         //upload the content 
 
         String dsLocation = super.fc.uploadFile( theFile );
  
         logm = String.format( "added %s to the object with pid: %s", dsLocation, pid );
 
         String returnedSID = super.fem.addDatastream( pid, sID, new String[] {}, label, versionable, mimetype, null, dsLocation, "M", "A", null, null, logm );
 
         return returnedSID;
 
     }
     /**
      * method for modifying an existing dataStream in an object
      * @param theFile, the file to be added as a stream to the specified object
      * @param sID the id of the datastream to be modified
      * @param pid the id of the object to get a datastream updated
      * @param the label of the updated stream
      * @param versionable, tells whether to keep track of old version of the stream
      * @param mimetype, the mimetype of the stream
      * @param breakDependencies tells whether to update the datastream or not
      * if the operation breaks dependencies with other objects
      * @return the checksum of the datastream...
      */
 
     public String modifyDataStream(File theFile, String sID, String pid, String label, boolean versionable, String mimetype, boolean breakDependencies ) throws RemoteException, MalformedURLException, IOException
     {
         String logm = String.format( "modified the object with pid: %s", pid );
         String[] altIDs;
 
         String dsLocation = super.fc.uploadFile( theFile );
 
         super.fem.modifyDatastreamByReference( pid, sID, new String[] {}, label,  mimetype, null, dsLocation, null, null, logm, breakDependencies );
         return "";
 
     }
 
     /**
      * method for removing a datastream form an object in the Fedora base
      * @param pid, the indentifier of the object to remove from
      * @param sID, the identifier of the stream to remove
      * @param breakDependencies tells whether to break data contracts/dependencies
      * @param startDate, the earlyist date to remove stream versions from, can be null
      * @param endDate, the latest date to remove stream versions to, can be null
      * @return true if the stream was removed
      */
     public boolean removeDataStream( String pid, String sID, String startDate, String endDate, boolean breakDependencies ) throws RemoteException, ParserConfigurationException, TransformerConfigurationException, TransformerException, IOException, SAXException
     {    
         String adminLogm = "";
 
         //10: get the adminstream to modify
         MIMETypedStream ds = super.fea.getDatastreamDissemination( pid,DataStreamType.AdminData.getName(), null );
         byte[] adminStream = ds.getStream();
         log.debug( String.format( "Got adminstream from fedora == %s", new String( adminStream ) ) );
         ByteArrayInputStream bis = new ByteArrayInputStream( adminStream );
         log.debug( String.format( "Trying to get root element from adminstream with length %s", bis.available() ) );
         Element rootOld = XMLFileReader.getDocumentElement( new InputSource( bis ) );
 
         log.debug( String.format( "root element from adminstream == %s", rootOld ) );
 
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document admStream = builder.newDocument();
         //get indexingAlias
         NodeList indexingAliasElemOld = rootOld.getElementsByTagName( "indexingalias" );
         Node indexingAliasNodeOld = indexingAliasElemOld.item( 0 );
         
         //make root and indexingAlias part of the new document
         Element root = (Element)admStream.importNode( (Node)rootOld, false );
         Node indexingAliasNode = admStream.importNode( indexingAliasNodeOld, true ); 
 
         //append the indexingAlias to the root
         root.appendChild( indexingAliasNode );
 
         NodeList streamsNLOld = rootOld.getElementsByTagName( "streams" );
         Element streamsOld = (Element)streamsNLOld.item( 0 );
         Element streams = (Element)admStream.importNode( (Node)streamsOld, false );
         root.appendChild( (Node)streams );       
 
         NodeList streamNLOld = streamsOld.getElementsByTagName( "stream" );
         //need to loop streams to get the type and index of the stream to be purged
         int purgeIndex = -1;
         String purgeStreamTypeName = "";
         Element oldStream;
         int streamNLLength = streamNLOld.getLength();
         for( int i = 0; i < streamNLLength; i++ )
         {
             oldStream = (Element)streamNLOld.item( i );
             if( oldStream.getAttribute( "id" ).equals( sID ) )
             {
                 purgeIndex = Integer.valueOf( oldStream.getAttribute( "index" ) );
                 purgeStreamTypeName = oldStream.getAttribute( "streamTypeName" );
             }
         }
 
         //need to loop streams again to get the streams to be moved to the admStream
         String currentStreamTypeName;
         int currentIndex;
         int newVal;
         for( int i = 0; i < streamNLLength; i++ )
         {
             oldStream = (Element)streamNLOld.item( i );
             //if not the stream to purge, import to admStream
             if( !oldStream.getAttribute( "id" ).equals( sID) )
             {
                 Element stream = (Element)admStream.importNode( (Node)oldStream, true );
                 //modify the index of the stream, if of same StreamType and index has higher value
                 currentStreamTypeName = stream.getAttribute( "streamTypeName" );
                 if( currentStreamTypeName.equals( purgeStreamTypeName ) )
                 {
                     currentIndex = Integer.valueOf( stream.getAttribute( "index" ) );
                     if( currentIndex > purgeIndex )
                     {
                         newVal = currentIndex - 1;
                         stream.setAttribute( "index", Integer.toString( newVal) );
                     }
                 }
 
                 streams.appendChild( (Node) stream );
             }
         }
 
         // 18: make the admin info into a File ( and a String for current debug)
         Source source = new DOMSource((Node) root );
         StringWriter stringWriter = new StringWriter();
         File admFile = new File( "admFile" );
         admFile.deleteOnExit();
 
         Result result = new StreamResult( admFile );
         Result stringResult = new StreamResult( stringWriter );//debug
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         Transformer transformer = transformerFactory.newTransformer();
         transformer.transform(source, result);
         transformer.transform(source, stringResult);
         //debug
         String admStreamString = stringWriter.getBuffer().toString();
         System.out.println( admStreamString );
 
         // 20:use modify by reference
         String adminLabel= "admin [text/xml]";
         String adminMime = "text/xml";
         SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.S" );
         String timeNow = dateFormat.format( new Date( System.currentTimeMillis() ) );
         adminLogm = "admin stream updated with added stream data" + timeNow;
 
         //upload the admFile
         String admLocation = super.fc.uploadFile( admFile );
 
         super.fem.modifyDatastreamByReference( pid, DataStreamType.AdminData.getName(), new String[] {}, adminLabel, adminMime, null, admLocation, null, null, adminLogm, true );
 
         boolean retval = false;
         String logm = String.format( "removed stream %s from object %s", sID, pid );
 
         /**
          * \Todo: find out why the return val is String[] and not String. bug 9046
          */
         String[] stamp = super.fem.purgeDatastream( pid, sID, startDate, endDate, logm, breakDependencies );
         if( stamp != null )
         {
             retval = true;
         }
         return retval;
     
     }
 
  /**
      * method for adding a relation to an object
      * @param pid, the identifier of the object to add the realtion to
      * @param predicate, the predicate of the relation to add
      * @param targetPid, the object to relate the object to, can be a literal
      * @param literal, true if the targetPid is a literal
      * @param datatype, the datatype of the literal, optional
      * @return true if the relation was added
      */
  public boolean addRelation( String pid, String predicate, String targetPid, boolean literal, String datatype ) throws RemoteException
     {
         return super.fem.addRelationship( pid, predicate, targetPid, literal, datatype );
     }
  
     /**
      * method to check whether an object has a RELS-EXT Datastream
      * @param pid, the identifier of the object in question
      * @return true only if the object has a RELS-EXT Datastream
      */
     public boolean objectHasRELSEXT( String pid) throws RemoteException
     {
         RelationshipTuple[] reltup = super.fem.getRelationships( pid, null );
         
         for( int i = 0; i < reltup.length; i++ )
         {
             System.out.println( reltup[ i ].getSubject() );
             System.out.println( reltup[ i ].getPredicate() );
             System.out.println( reltup[ i ].getObject() );
         }
 
         if( reltup.length > 0 )
         {
             return true;
         }
         else
         {
             return false;
         }
     }
     public boolean removeRelation( String pid, String predicate, String targetPid, boolean isLiteral, String datatype ) throws RemoteException
     {
         return super.fem.purgeRelationship( pid, predicate, targetPid, isLiteral, datatype );
     }
 }
