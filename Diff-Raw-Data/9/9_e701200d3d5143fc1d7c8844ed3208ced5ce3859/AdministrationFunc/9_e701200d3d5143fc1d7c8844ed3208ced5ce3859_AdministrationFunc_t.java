 package dk.dbc.opensearch.common.fedora;
 
 
 import dk.dbc.opensearch.common.fedora.FedoraAdministration;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.IndexingAlias;
 import dk.dbc.opensearch.common.types.DataStreamType;
 
 import fedora.server.types.gen.ObjectFields;
 import fedora.server.types.gen.ComparisonOperator;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.rpc.ServiceException;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamWriter;
 
 import fedora.server.errors.ObjectNotInLowlevelStorageException;
 
 import org.apache.axis.types.NonNegativeInteger;
 import org.apache.commons.configuration.ConfigurationException;
 
 
 /**
  *class for testing the functionality of the FedoraAdminstration class
  */
 public class AdministrationFunc 
 {
     static FedoraAdministration fa;
 
     
     public static void main( String[] args ) throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {
         runTests();
     }
     
 
     static void runTests() throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {
         try
         {
             fa = new FedoraAdministration();
         }
         catch( Exception e)
         {
             e.printStackTrace();
         }
 
         /*System.out.println( "*** kalder teestStoreCC ***" );
         String pid = testStoreCC();
 
         System.out.println( "*** kalder get ***" );
         testGetObject( pid );
 
         System.out.println( "*** kalder testFindObjects ***" );
         testFindObjectPids();*/
         
         //System.out.println( "*** kalder testFindObjectFields ***" );
         //testFindObjectFields();
         
         System.out.println( "*** kalder testDeleteObjects ***" );
        String[] labels = { "ebrary", "danmarcxchange" };
         testDeleteObjectPids( labels, 50 );
 
         /*System.out.println( "*** kalder getDataStreamsOfType første gang ***" );
         testGetDataStreamsOfType( pid );
 
         System.out.println( "*** kalder add ***" );
         testAddDataStreamToObject( pid );
 
         System.out.println( "*** kalder getDataStreamsOfType anden gang ***" );
         testGetDataStreamsOfType( pid );
 
         System.out.println( "*** kalder modify ***" );
         testModifyDataStream( pid );
 
         System.out.println( "*** kalder remove ***" );
         testRemoveDataStream( pid );
         
         System.out.println( "*** kalder getDataStreamsOfType for anden gang" );
         testGetDataStreamsOfType( pid );
         testDeleteObject( pid );*/
     }
 
 
     static void testFindObjectPids() throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {
         String[] pids = null;
         try
         {
         	pids = fa.findObjectPids( "label", "eq", "testObject for testing FedoraAdministration" );
         }
         catch( RemoteException re )
         {
             re.printStackTrace();
         }
         
         for( int i = 0; i < pids.length; i++ )
         {
             System.out.println( pids[ i ] );
         }
     }
     
     
     static void testFindObjectFields() throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {    	
     	boolean ret = fa.addIsMbrOfCollRelationship( "harry:1", "title", "Harry Potter", "work" );
     	System.out.println( "Everything ok: " + ret );
     }
     
     
     static void testFindObjectFieldsDouble() throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {    	
     	boolean ret = fa.addIsMbrOfCollRelationship( "harry:1", "title", "Harry Potter og Fønixordenen", "creator", "Joanne K. Rowling", "work" );
     	System.out.println( "Everything ok: " + ret );
     }
     
     
     static void testDeleteObjectPids( String[] labels, int runsPerLabel ) throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {	
     	for ( String str : labels )
     	{
     		for ( int i = 0; i < runsPerLabel; i++ )
     		{
 		        String[] pids = null;
 		        try
 		        {
 		            //pids = fa.findObjectPids( "label", "eq", "materialevurderinger" );
 		        	pids = fa.findObjectPids( "label", "eq", str );
 		        }
 		        catch( RemoteException re )
 		        {
 		            re.printStackTrace();
 		        }
 		        System.out.println( "testDeleteObjectPids - pids.length: " + pids.length );
 		        for( int j = 0; j < pids.length; j++ )
 		        {
 		            System.out.println( pids[ j ] );
 		            testDeleteObject( pids[ j ] );
 		        }
     		}
     	}
     }
 
 
     static void testRemoveDataStream( String pid )
     {
         boolean success = false;
         String sID = DataStreamType.OriginalData.getName() + ".0";
         try
         {
             success = fa.removeDataStream( pid, sID, null, null, false );
         }
         catch( Exception e )
         {
             e.printStackTrace();
         }
         if( !success )
         {
             System.out.println( "testRemoveDataStream failed" );
         }
         else
         {
             System.out.println( "testRemoveDataStream succeded" );
         }
 
     }
 
 
     static void testModifyDataStream( String pid )
         {
              CargoObject cargo = null;
         try
         {
             cargo  = createCargoObject( 2 );
         }
         catch( Exception e )
         {
             e.printStackTrace();
         }
             String sID = DataStreamType.OriginalData.getName() + ".0";
 
             String checksum = null;
             try
             {
                 checksum = fa.modifyDataStream( cargo, sID, pid, false, true );
             }
             catch( Exception e )
             {
                 e.printStackTrace();
             }
 
             System.out.println( String.format( "the returned checksum: ", checksum ) );
 
         }
 
 
     static void testAddDataStreamToObject( String pid )
     {
         CargoObject cargo = null;
         try
         {
             cargo  = createCargoObject( 1 );
         }
         catch( Exception e )
         {
             e.printStackTrace();
         }
         String retID = "";
 
         try
         {
             retID = fa.addDataStreamToObject( cargo, pid, false, true );
         }
         catch( Exception e )
         {
             e.printStackTrace();
         }
 
         System.out.println( String.format( "got the id: %s from the addDataStreamToObject method", retID ) );
 
     }
 
 
     static void testGetDataStream( String pid )
     {
         String streamID = (DataStreamType.OriginalData).getName() + ".0";
         CargoContainer cc = new CargoContainer();
         try
         {
             cc = fa.getDataStream( streamID, pid );
         }
         catch( Exception e )
         {
             e.printStackTrace();
         }
         int count = cc.getCargoObjectCount();
         if( count < 1 )
         {
             System.out.println( String.format( "kunne ikke finde nogen strømme med id %s", streamID ) );
         }
         else
         {
             System.out.println( String.format( "fandt %s strøm(me) med id %s", count, streamID ) );
         }
     }
 
 
     static void testGetDataStreamsOfType( String pid )
     {
         CargoContainer cc = new CargoContainer();
         DataStreamType dst = DataStreamType.OriginalData;
         try
         {
             cc = fa.getDataStreamsOfType( pid, dst );
         }
         catch( Exception e )
         {
             e.printStackTrace();
         }
 
         int count = cc.getCargoObjectCount();
         if( count < 1 )
         {
             System.out.println( "kunne ikke finde nogen af korrekt type!!!!!!!" );
         }
         else
         {
             System.out.println( String.format( "fandt %s strømme med typen %s", count, dst.getName() ) );
         }
     }
 
 
     static void testMarkObjectWithDelete( String pid )
     {
         boolean result = false;
 
         result = fa.markObjectAsDeleted( pid );
         System.out.println( result );
 
     }
 
 
     static void testDeleteObject( String pid ) throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {
         try
         {
             fa.deleteObject( pid, false );
         }
         catch( RemoteException re )
         {
             System.out.println( "printing trace:" );
             re.printStackTrace();
             return;
         }
 
         System.out.println( String.format( "Object with pid: %s deleted", pid ) );
     }
 
 
     //method for testing the storeCC method
     static String testStoreCC()
     {
         System.out.println( "testStoreCC called" );
 
         CargoContainer cc = new CargoContainer();
         try
         {
             cc = createContainer();
         }
         catch( Exception e )
         {
             e.printStackTrace();
         }
         List<CargoObject> coList = cc.getCargoObjects();
         String submitter = coList.get( 0 ).getSubmitter();
         System.out.println( String.format( "trying to store CargoContainer with submitter: %s", submitter ) );
 
         String format = "testObject for testing FedoraAdministration";
         String pid = "";
         try
         {
             pid = fa.storeCargoContainer( cc, submitter, format );
         }
         catch( Exception e)
         {
             e.printStackTrace();
         }
 
 
         System.out.println( String.format( "Got the pid: %s back", pid ) );
         return pid;
     }
 
     
     //method for testing the getObject method
     static void testGetObject( String pid)
     {
         System.out.println( String.format( "testGetObject called with pid: %s", pid ) );
         List<CargoObject> coList = new ArrayList<CargoObject>();
         CargoContainer cc = new CargoContainer();
 
         try
         {
         	FedoraAdministration fa = new FedoraAdministration();
             //cc = FedoraAdministration.retrieveCargoContainer( pid );
         	cc = fa.retrieveCargoContainer( pid );
         }
         catch( Exception e )
         {
             e.printStackTrace();
         }
 
         coList = cc.getCargoObjects();
         String submitter = coList.get( 0 ).getSubmitter();
         System.out.println( String.format( "got CargoContainer with submitter: %s", submitter ) );
     }
 
 
     /**
      * Helper methods
      */
     //method for creating a CargoObject
     static CargoObject createCargoObject( int job )throws IllegalArgumentException, NullPointerException, IOException
     {
         String testStr;
         if( job == 1 )
         {
             testStr = "added stream";
         }
         else
         {
             testStr = "modified stream";
         }
         byte[] cargoBytes =  testStr.getBytes( "UTF-8" );
 
         CargoContainer cc = new CargoContainer();
         cc.add( DataStreamType.getDataStreamTypeFrom( "originalData" ), "test", "dbc", "eng", "text/xml", IndexingAlias.getIndexingAlias( "article" ) , cargoBytes);
 
         return cc.getCargoObject( DataStreamType.OriginalData );
     }
 
 
     //method for creating test data, a CargoContainer
     static CargoContainer createContainer() throws IllegalArgumentException, NullPointerException, IOException
     {
         String testStr = "test";
         byte[] cargoBytes =  testStr.getBytes( "UTF-8" );
 
         CargoContainer ret = new CargoContainer();
         ret.add( DataStreamType.getDataStreamTypeFrom( "originalData" ), "test", "dbc", "eng", "text/xml", IndexingAlias.getIndexingAlias( "article" ) , cargoBytes);
 
         return ret;
     }
 
 
     //method for creating an XML file
     static File createTestXMLFile()
     {
         XMLOutputFactory factory = XMLOutputFactory.newInstance();
         XMLStreamWriter writer;
         //building theFile
         File theFile = new File( "theFile" );
         theFile.deleteOnExit();
         //build and xml string and make it into a File
 
         try
         {
             writer = factory.createXMLStreamWriter( new FileOutputStream( theFile ), "UTF-8" );
             writer.writeStartDocument("UTF-8", "1.0"); //(encoding, version)
             writer.writeStartElement("Text");
             writer.writeCharacters( "created test stream" );
             writer.writeEndElement();
             writer.writeEndDocument();
         }
         catch( Exception e )
         {
             e.printStackTrace();
         }
 
         return theFile;
     }
 }
