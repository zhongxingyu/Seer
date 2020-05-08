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
 
 
 package dk.dbc.opensearch.components.datadock;
 
 
 import dk.dbc.opensearch.common.config.DatadockConfig;
 import dk.dbc.opensearch.common.db.Processqueue;
 import dk.dbc.opensearch.common.fedora.FedoraAdministration;
 import dk.dbc.opensearch.common.fedora.PIDManager;
 import dk.dbc.opensearch.common.pluginframework.PluginResolverException;
 import dk.dbc.opensearch.common.pluginframework.PluginResolver;
 import dk.dbc.opensearch.common.statistics.Estimate;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoMimeType;
 import dk.dbc.opensearch.common.types.CompletedTask;
 import dk.dbc.opensearch.components.datadock.DatadockJob;
 import dk.dbc.opensearch.components.datadock.DatadockPool;
 import dk.dbc.opensearch.components.datadock.DatadockThread;
 import dk.dbc.opensearch.components.harvest.IHarvest;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.InterruptedException;
 import java.lang.Throwable;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Vector;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.RejectedExecutionHandler;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.AbstractExecutorService;
 import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
 
 import javax.xml.rpc.ServiceException;
 import javax.xml.parsers.ParserConfigurationException;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.easymock.internal.ExpectedInvocation;
 
 import static org.easymock.classextension.EasyMock.*;
 import static org.junit.Assert.*;
 import org.junit.*;
 import org.xml.sax.SAXException;
 
 import mockit.Mock;
 import mockit.MockClass;
 import mockit.Mockit;
 
 
 /**
  * This unittest does not test the getTask method, since this methods has
  * conditionals and are creating new complex objects. The method is mocked
  * through a mockclass for this test.
  */
 public class DatadockPoolTest extends TestCase
 {
     /**
      * The (mock)objects we need for the most of the tests
      */
     Estimate mockEstimate;
     Processqueue mockProcessqueue;
     CargoContainer mockCargoContainer;
     FutureTask mockFutureTask;
     ThreadPoolExecutor mockThreadPoolExecutor;
     DatadockJob mockDatadockJob;
     DatadockPool datadockPool;
     DatadockThread datadockThread;
     FedoraAdministration mockFedoraAdministration;
     IHarvest mockHarvester;
     PluginResolver mockPluginResolver;
 
     /**
      * After each test the mock are reset
      */
     static FutureTask mockFuture = createMock( FutureTask.class );
 
     @MockClass( realClass = DatadockPool.class )
     public static class MockDatadockPool
     {
         @Mock(invocations = 1)
         public static FutureTask getTask( DatadockJob datadockjob )
         {
             return mockFuture;
         }
     }
 
     @Before
     public void setUp()
     {
         mockThreadPoolExecutor = createMock( ThreadPoolExecutor.class );
         mockEstimate = createMock( Estimate.class);
         mockProcessqueue = createMock( Processqueue.class );
         mockHarvester = createMock( IHarvest.class );
         mockFedoraAdministration = createMock( FedoraAdministration.class );
         mockDatadockJob = createMock( DatadockJob.class );
         mockPluginResolver = createMock( PluginResolver.class );
     }
 
 
     @After
     public void tearDown()
     {
         Mockit.tearDownMocks();
         reset( mockThreadPoolExecutor );
         reset( mockEstimate );
         reset( mockProcessqueue );
         reset( mockFedoraAdministration );
         reset( mockDatadockJob );
         reset( mockFuture );
         reset( mockHarvester );
         reset( mockPluginResolver );
     }
 
 
     @Test
 
     public void testConstructor() throws ConfigurationException
     {
         /**
          * setup
          */
 
 
         Mockit.setUpMocks( MockDatadockPool.class );
 
         /**
          * expectations
          */
 
         mockThreadPoolExecutor.setRejectedExecutionHandler( isA( RejectedExecutionHandler.class ) );
         /**
          * replay
          */
         replay( mockThreadPoolExecutor );
         replay( mockEstimate );
         replay( mockProcessqueue );
         replay( mockFedoraAdministration );
         replay( mockPluginResolver );
         
         /**
          * do stuff
          */
         datadockPool = new DatadockPool( mockThreadPoolExecutor, mockEstimate, mockProcessqueue, mockFedoraAdministration, mockHarvester, mockPluginResolver );
 
         /**
          * verify
          */
         verify( mockThreadPoolExecutor );
         verify( mockEstimate );
         verify( mockProcessqueue );
         verify( mockFedoraAdministration );
         verify( mockPluginResolver );
     }
 
 
     @Test
     public void testSubmit() throws IOException, ConfigurationException, ClassNotFoundException, ServiceException, PluginResolverException, ParserConfigurationException, SAXException
     {
         /**
          * setup
          */
         Mockit.setUpMocks( MockDatadockPool.class );
         File tmpFile = File.createTempFile("opensearch-unittest","" );
         FileWriter fstream = new FileWriter( tmpFile );
         BufferedWriter out = new BufferedWriter(fstream);
         out.write("Hello Java");
         out.close();
 
         tmpFile.deleteOnExit();
         URI testURI = tmpFile.toURI();
         /**
          * expectations
          */
         mockThreadPoolExecutor.setRejectedExecutionHandler( isA( RejectedExecutionHandler.class ) );
         //expect( mockDatadockJob.getUri() ).andReturn( testURI );
         expect( mockDatadockJob.getSubmitter() ).andReturn( "test" );
         expect( mockDatadockJob.getFormat() ).andReturn( "test" );
         //calling getTask with the getTask method mocked
         expect( mockThreadPoolExecutor.submit( mockFuture ) ).andReturn( mockFuture );
 
         /**
          * replay
          */
         replay( mockThreadPoolExecutor );
         replay( mockEstimate );
         replay( mockProcessqueue );
         replay( mockFedoraAdministration );
         replay( mockDatadockJob );
         replay( mockFuture );
         replay( mockPluginResolver );
 
         /**
          * do stuff
          */
         datadockPool = new DatadockPool( mockThreadPoolExecutor, mockEstimate, mockProcessqueue, mockFedoraAdministration, mockHarvester, mockPluginResolver );
         datadockPool.submit( mockDatadockJob );
         /**
          * verify
          */
         verify( mockThreadPoolExecutor );
         verify( mockEstimate );
         verify( mockProcessqueue );
         verify( mockFedoraAdministration );
         verify( mockDatadockJob );
         verify( mockFuture );
         verify( mockPluginResolver );
     }
 
 
     @Test
     public void testCheckJobs_isDoneFalse() throws IOException, ConfigurationException, ClassNotFoundException, ServiceException, PluginResolverException, ParserConfigurationException, SAXException, InterruptedException
     {
         /**
          * setup
          */
         Mockit.setUpMocks( MockDatadockPool.class );
         File tmpFile = File.createTempFile("opensearch-unittest","" );
         FileWriter fstream = new FileWriter( tmpFile );
         BufferedWriter out = new BufferedWriter(fstream);
         out.write("Hello Java");
         out.close();
 
         tmpFile.deleteOnExit();
         //        URI testURI = tmpFile.toURI();
         /**
          * expectations
          */
         mockThreadPoolExecutor.setRejectedExecutionHandler( isA( RejectedExecutionHandler.class ) );
         //expect( mockDatadockJob.getUri() ).andReturn( testURI );
         expect( mockDatadockJob.getSubmitter() ).andReturn( "test" );
         expect( mockDatadockJob.getFormat() ).andReturn( "test" );
         //getTask is called and the method is mocked to return mockFuture
         expect( mockThreadPoolExecutor.submit( mockFuture ) ).andReturn( mockFuture );
         //calling checkJobs
         expect( mockFuture.isDone() ).andReturn( false );
 
         /**
          * replay
          */
         replay( mockThreadPoolExecutor );
         replay( mockEstimate );
         replay( mockProcessqueue );
         replay( mockFedoraAdministration );
         replay( mockDatadockJob );
         replay( mockFuture );
         replay( mockPluginResolver );
 
         /**
          * do stuff
          */
         datadockPool = new DatadockPool( mockThreadPoolExecutor, mockEstimate, mockProcessqueue, mockFedoraAdministration, mockHarvester, mockPluginResolver );
         datadockPool.submit( mockDatadockJob );
         Vector< CompletedTask > checkedJobs = datadockPool.checkJobs();
         assertTrue( checkedJobs.size() == 0 );
 
         /**
          * verify
          */
         verify( mockThreadPoolExecutor );
         verify( mockEstimate );
         verify( mockProcessqueue );
         verify( mockFedoraAdministration );
         verify( mockDatadockJob );
         verify( mockFuture );
         verify( mockPluginResolver );
     }
 
 
     @Test
     public void testCheckJobs_isDoneTrue() throws IOException, ConfigurationException, ClassNotFoundException, ServiceException, PluginResolverException, ParserConfigurationException, SAXException, InterruptedException, ExecutionException
     {
         /**
          * setup
          */
         Mockit.setUpMocks( MockDatadockPool.class );
         File tmpFile = File.createTempFile("opensearch-unittest","" );
         FileWriter fstream = new FileWriter( tmpFile );
         BufferedWriter out = new BufferedWriter(fstream);
         out.write("Hello Java");
         out.close();
 
         tmpFile.deleteOnExit();
         URI testURI = tmpFile.toURI();
 
         /**
          * expectations
          */
         mockThreadPoolExecutor.setRejectedExecutionHandler( isA( RejectedExecutionHandler.class ) );
         //expect( mockDatadockJob.getUri() ).andReturn( testURI );
         expect( mockDatadockJob.getSubmitter() ).andReturn( "test" );
         expect( mockDatadockJob.getFormat() ).andReturn( "test" );
         //getTask is called and the method is mocked to return mockFuture
         expect( mockThreadPoolExecutor.submit( mockFuture ) ).andReturn( mockFuture );
         //calling checkJobs
         expect( mockFuture.isDone() ).andReturn( true );
         expect( mockFuture.get() ).andReturn( 10f );
 
         /**
          * replay
          */
         replay( mockThreadPoolExecutor );
         replay( mockEstimate );
         replay( mockProcessqueue );
         replay( mockFedoraAdministration );
         replay( mockDatadockJob );
         replay( mockFuture );
         replay( mockPluginResolver );
 
         /**
          * do stuff
          */
         datadockPool = new DatadockPool( mockThreadPoolExecutor, mockEstimate, mockProcessqueue, mockFedoraAdministration, mockHarvester, mockPluginResolver );
         datadockPool.submit( mockDatadockJob );
         Vector< CompletedTask > checkedJobs = datadockPool.checkJobs();
         assertTrue( checkedJobs.size() == 1 );
 
         /**
          * verify
          */
         //verify( mockThreadPoolExecutor );
         verify( mockEstimate );
         verify( mockProcessqueue );
         verify( mockFedoraAdministration );
         verify( mockDatadockJob );
         verify( mockFuture );
         verify( mockPluginResolver );
     }
 
 
     @Test
     public void testCheckJobs_isDoneError() throws IOException, ConfigurationException, ClassNotFoundException, ServiceException, PluginResolverException, ParserConfigurationException, SAXException, InterruptedException, ExecutionException
     {
         /**
          * setup
          */
         Mockit.setUpMocks( MockDatadockPool.class );
         File tmpFile = File.createTempFile("opensearch-unittest","" );
         FileWriter fstream = new FileWriter( tmpFile );
         BufferedWriter out = new BufferedWriter(fstream);
         out.write("Hello Java");
         out.close();
 
         tmpFile.deleteOnExit();
         //URI testURI = tmpFile.toURI();
         /**
          * expectations
          */
         mockThreadPoolExecutor.setRejectedExecutionHandler( isA( RejectedExecutionHandler.class ) );
         //submit method
         //expect( mockDatadockJob.getUri() ).andReturn( testURI );
         expect( mockDatadockJob.getSubmitter() ).andReturn( "test" );
         expect( mockDatadockJob.getFormat() ).andReturn( "test" );
         //getTask is called and the method is mocked to return mockFuture
         expect( mockThreadPoolExecutor.submit( mockFuture ) ).andReturn( mockFuture );
         //submit method 2nd call
         //expect( mockDatadockJob.getUri() ).andReturn( testURI );
         expect( mockDatadockJob.getSubmitter() ).andReturn( "test" );
         expect( mockDatadockJob.getFormat() ).andReturn( "test" );
         //getTask is called and the method is mocked to return mockFuture
         expect( mockThreadPoolExecutor.submit( mockFuture ) ).andReturn( mockFuture );
         //calling checkJobs
         expect( mockFuture.isDone() ).andReturn( true );
         expect( mockFuture.get() ).andThrow( new ExecutionException( new Throwable( "test exception" ) ) );
         expect( mockFuture.isDone() ).andReturn( true );
         expect( mockFuture.get() ).andReturn( 10f );
         /**
          * replay
          */
         replay( mockThreadPoolExecutor );
         replay( mockEstimate );
         replay( mockProcessqueue );
         replay( mockFedoraAdministration );
         replay( mockDatadockJob );
         replay( mockFuture );
         replay( mockPluginResolver );
 
         /**
          * do stuff
          */
         datadockPool = new DatadockPool( mockThreadPoolExecutor, mockEstimate, mockProcessqueue, mockFedoraAdministration, mockHarvester, mockPluginResolver );
         datadockPool.submit( mockDatadockJob );
         datadockPool.submit( mockDatadockJob );
         Vector< CompletedTask > checkedJobs = datadockPool.checkJobs();
         assertTrue( checkedJobs.size() == 2 );
 
         /**
          * verify
          */
         verify( mockThreadPoolExecutor );
         verify( mockEstimate );
         verify( mockProcessqueue );
         verify( mockFedoraAdministration );
         verify( mockDatadockJob );
         verify( mockFuture );
         verify( mockPluginResolver );
     }
 
 
     @Test
     public void testShutdown() throws IOException, ConfigurationException, ClassNotFoundException, ServiceException, PluginResolverException, ParserConfigurationException, SAXException, InterruptedException, ExecutionException
     {
         /**
          * setup
          */
         Mockit.setUpMocks( MockDatadockPool.class );
         File tmpFile = File.createTempFile("opensearch-unittest","" );
         FileWriter fstream = new FileWriter( tmpFile );
         BufferedWriter out = new BufferedWriter(fstream);
         out.write("Hello Java");
         out.close();
         tmpFile.deleteOnExit();
         URI testURI = tmpFile.toURI();
 
         /**
          * expectations
          */
         mockThreadPoolExecutor.setRejectedExecutionHandler( isA( RejectedExecutionHandler.class ) );
         expect( mockDatadockJob.getSubmitter() ).andReturn( "test" );
         expect( mockDatadockJob.getFormat() ).andReturn( "test" );
         //getTask is called and the method is mocked to return mockFuture
         expect( mockThreadPoolExecutor.submit( mockFuture ) ).andReturn( mockFuture );
        //calling shutdown        
        mockThreadPoolExecutor.shutdown();
        expect( mockThreadPoolExecutor.awaitTermination( 1 , TimeUnit.DAYS)).andReturn(true);
 
         /**
          * replay
          */
         replay( mockThreadPoolExecutor );
         replay( mockEstimate );
         replay( mockProcessqueue );
         replay( mockFedoraAdministration );
         replay( mockDatadockJob );
         replay( mockFuture );
         replay( mockPluginResolver );
 
         /**
          * do stuff
          */
         datadockPool = new DatadockPool( mockThreadPoolExecutor, mockEstimate, mockProcessqueue, mockFedoraAdministration, mockHarvester, mockPluginResolver );
         datadockPool.submit( mockDatadockJob );
         datadockPool.shutdown();
 
         /**
          * verify
          */
         verify( mockThreadPoolExecutor );
         verify( mockEstimate );
         verify( mockProcessqueue );
         verify( mockFedoraAdministration );
         verify( mockDatadockJob );
         verify( mockFuture );
         verify( mockPluginResolver );
     }
 }
