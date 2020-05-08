 /*******************************************************************************
  * Copyright (C) 2011  John Casey
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public
  * License along with this program.  If not, see 
  * <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package org.commonjava.couch.change;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.enterprise.inject.Alternative;
 import javax.inject.Named;
 
 import org.commonjava.couch.change.dispatch.CouchChangeDispatcher;
 import org.commonjava.couch.conf.CouchDBConfiguration;
 import org.commonjava.couch.conf.DefaultCouchDBConfiguration;
 import org.commonjava.couch.db.CouchManager;
 import org.commonjava.couch.io.CouchAppReader;
 import org.commonjava.couch.io.CouchHttpClient;
 import org.commonjava.couch.io.Serializer;
 import org.commonjava.couch.model.AbstractCouchDocument;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class CouchChangeListenerTest
 {
 
    private static final String URL = "http://localhost:5984/test-change-listener";
 
     private CouchChangeListener listener;
 
     private CouchHttpClient client;
 
     private CouchManager couch;
 
     private CaptureDispatcher dispatcher;
 
     @Before
     public void setupTest()
         throws Exception
     {
         CouchDBConfiguration config = new DefaultCouchDBConfiguration( URL );
         Serializer serializer = new Serializer();
 
         client = new CouchHttpClient( config, serializer );
 
         couch = new CouchManager( config, client, serializer, new CouchAppReader() );
         couch.dropDatabase();
         couch.createDatabase();
 
         dispatcher = new CaptureDispatcher();
 
         listener = new CouchChangeListener( dispatcher, client, config, couch, serializer );
         listener.startup();
     }
 
     @After
     public void teardownTest()
         throws Exception
     {
         listener.shutdown();
         while ( listener.isRunning() )
         {
             synchronized ( listener )
             {
                 System.out.println( "Waiting 2s for change listener to shutdown..." );
                 listener.wait( 2000 );
             }
         }
 
         couch.dropDatabase();
     }
 
     @Test
     public void addDocumentAndDetectCorrespondingChange()
         throws Exception
     {
         boolean stored = couch.store( new TestDocument( "addedDocument" ), false );
 
         assertThat( stored, equalTo( true ) );
 
         dispatcher.waitForChanges( 20 );
 
         assertThat( dispatcher.changes.size(), equalTo( 1 ) );
     }
 
     @Test
     public void addDocumentThenDeleteIt_DetectCorrespondingChangesInBetween()
         throws Exception
     {
         TestDocument doc = new TestDocument( "addedDocument" );
         boolean stored = couch.store( doc, false );
 
         assertThat( stored, equalTo( true ) );
 
         dispatcher.waitForChanges( 20 );
 
         assertThat( dispatcher.changes.size(), equalTo( 1 ) );
 
         dispatcher.changes.clear();
 
         couch.delete( doc );
 
         dispatcher.waitForChanges( 20 );
 
         assertThat( dispatcher.changes.size(), equalTo( 1 ) );
     }
 
     @Named( "non-jee" )
     @Alternative
     public static final class CaptureDispatcher
         implements CouchChangeDispatcher
     {
 
         public final List<CouchDocChange> changes = new ArrayList<CouchDocChange>();
 
         @Override
         public void documentChanged( final CouchDocChange change )
         {
             System.out.println( "Adding change: " + change );
             changes.add( change );
             synchronized ( this )
             {
                 notify();
             }
         }
 
         synchronized void waitForChanges( final double seconds )
         {
             long start = System.currentTimeMillis();
             double total = 0;
 
             while ( changes.isEmpty() )
             {
                 total = ( System.currentTimeMillis() - start ) / 1000;
                 if ( total > ( seconds * 1000 ) )
                 {
                     System.out.println( "CaptureDispatcher wait (" + seconds
                         + " seconds) expired without changes." );
                     break;
                 }
 
                 System.out.println( "Waiting for changes in CaptureDispatcher..." );
                 try
                 {
                     wait( 1000 );
                 }
                 catch ( InterruptedException e )
                 {
                     System.out.println( "CaptureDispatcher wait interrupted. Returning." );
                     break;
                 }
             }
 
             System.out.println( "Total CaptureDispatcher wait time: " + total + " seconds." );
         }
 
     }
 
     public static final class TestDocument
         extends AbstractCouchDocument
     {
 
         private String name;
 
         TestDocument()
         {}
 
         public TestDocument( final String name )
         {
             this.name = name;
             setCouchDocId( "test:" + name );
         }
 
         public String getName()
         {
             return name;
         }
 
         void setName( final String name )
         {
             this.name = name;
         }
 
     }
 
 }
