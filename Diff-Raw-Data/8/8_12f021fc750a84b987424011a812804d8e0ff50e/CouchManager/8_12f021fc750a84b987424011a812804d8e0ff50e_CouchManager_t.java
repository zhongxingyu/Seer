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
 package org.commonjava.couch.db;
 
 import static org.apache.commons.io.IOUtils.closeQuietly;
 import static org.apache.commons.io.IOUtils.copy;
 import static org.apache.http.HttpStatus.SC_CREATED;
 import static org.apache.http.HttpStatus.SC_NOT_FOUND;
 import static org.apache.http.HttpStatus.SC_OK;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Type;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.log4j.Logger;
 import org.commonjava.couch.db.action.BulkActionHolder;
 import org.commonjava.couch.db.action.CouchDocumentAction;
 import org.commonjava.couch.db.action.DeleteAction;
 import org.commonjava.couch.db.action.StoreAction;
 import org.commonjava.couch.db.handler.ResponseHandlerWithError;
 import org.commonjava.couch.db.handler.SerializedGetHandler;
 import org.commonjava.couch.db.model.CouchObjectList;
 import org.commonjava.couch.db.model.ViewRequest;
 import org.commonjava.couch.model.CouchApp;
 import org.commonjava.couch.model.CouchDocRef;
 import org.commonjava.couch.model.CouchDocument;
 import org.commonjava.couch.model.CouchError;
 import org.commonjava.couch.model.io.CouchAppReader;
 import org.commonjava.couch.model.io.CouchObjectListDeserializer;
 import org.commonjava.couch.model.io.SerializationAdapter;
 import org.commonjava.couch.model.io.Serializer;
 import org.commonjava.couch.util.ToString;
 import org.commonjava.couch.util.UrlUtils;
 
 public class CouchManager
 {
 
     private static final Logger LOGGER = Logger.getLogger( CouchManager.class );
 
     private static final String REV = "rev";
 
     private static final String VIEW_BASE = "_view";
 
     private static final String APP_BASE = "_design";
 
     private static final String BULK_DOCS = "_bulk_docs";
 
     private final Serializer serializer;
 
     private HttpClient client;
 
     private final ExecutorService exec = Executors.newCachedThreadPool();
 
     private final CouchAppReader appReader;
 
     public CouchManager( final Serializer serializer, final CouchAppReader appReader )
     {
         this.serializer = serializer;
         this.appReader = appReader;
     }
 
     public CouchManager()
     {
         this.serializer = new Serializer();
         this.appReader = new CouchAppReader();
     }
 
     public void initialize( final String dbUrl, final String appName, final String appResource )
         throws CouchDBException
     {
         CouchApp app;
         try
         {
             app = appReader.readAppDefinition( appName, appResource );
         }
         catch ( IOException e )
         {
             throw new CouchDBException(
                                         "Failed to retrieve application definition: %s. Reason: %s",
                                         e, appResource, e.getMessage() );
         }
 
         if ( !dbExists( dbUrl ) )
         {
             createDatabase( dbUrl );
         }
 
         if ( !appExists( dbUrl, appName ) )
         {
             installApplication( app, dbUrl );
         }
     }
 
     public void store( final Collection<? extends CouchDocument> documents, final String dbUrl,
                        final boolean skipIfExists, final boolean allOrNothing )
         throws CouchDBException
     {
         Set<StoreAction> toStore = new HashSet<StoreAction>();
         for ( CouchDocument doc : documents )
         {
            if ( skipIfExists && documentRevisionExists( doc, dbUrl ) )
             {
                 continue;
             }
 
             toStore.add( new StoreAction( doc, skipIfExists ) );
         }
 
         modify( toStore, dbUrl, allOrNothing );
         // threadedExecute( toStore, dbUrl );
     }
 
     public void delete( final Collection<? extends CouchDocument> documents, final String dbUrl,
                         final boolean allOrNothing )
         throws CouchDBException
     {
         Set<DeleteAction> toDelete = new HashSet<DeleteAction>();
         for ( CouchDocument doc : documents )
         {
             if ( !documentRevisionExists( doc, dbUrl ) )
             {
                 continue;
             }
 
             toDelete.add( new DeleteAction( doc ) );
         }
 
         modify( toDelete, dbUrl, allOrNothing );
         // threadedExecute( toDelete, dbUrl );
     }
 
     public void modify( final Collection<? extends CouchDocumentAction> actions,
                         final String dbUrl, final boolean allOrNothing )
         throws CouchDBException
     {
         BulkActionHolder bulk = new BulkActionHolder( actions, allOrNothing );
         String body = getSerializer().toString( bulk );
 
         String url;
         try
         {
             url = buildUrl( dbUrl, null, BULK_DOCS );
         }
         catch ( MalformedURLException e )
         {
             throw new CouchDBException( "Failed to format bulk-update URL: %s", e, e.getMessage() );
         }
 
         HttpPost request = new HttpPost( url );
         try
         {
             request.setEntity( new StringEntity( body, "application/json", "UTF-8" ) );
         }
         catch ( UnsupportedEncodingException e )
         {
             throw new CouchDBException( "Failed to encode POST entity for bulk update: %s", e,
                                         e.getMessage() );
         }
 
         HttpResponse response = executeHttp( request, "Bulk update failed" );
         StatusLine statusLine = response.getStatusLine();
         int code = statusLine.getStatusCode();
         if ( code != SC_OK && code != SC_CREATED )
         {
             String content = null;
             HttpEntity entity = response.getEntity();
             if ( entity != null )
             {
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 InputStream in = null;
                 try
                 {
                     in = entity.getContent();
                     copy( in, baos );
                 }
                 catch ( IOException e )
                 {
                     throw new CouchDBException(
                                                 "Error reading response content for error: %s\nError was: %s",
                                                 e, statusLine, e.getMessage() );
                 }
                 finally
                 {
                     closeQuietly( in );
                 }
 
                 content = new String( baos.toByteArray() );
             }
 
             throw new CouchDBException(
                                         "Bulk operation failed. Status line: %s\nContent:\n----------\n\n%s",
                                         statusLine, content );
         }
 
         // threadedExecute( new HashSet<CouchDocumentAction>( actions ), dbUrl );
     }
 
     public <T extends CouchDocument> List<T> getViewListing( final ViewRequest req,
                                                              final String dbUrl,
                                                              final Class<T> itemType )
         throws CouchDBException
     {
         req.setParameter( ViewRequest.INCLUDE_DOCS, true );
 
         String url = buildViewUrl( dbUrl, req );
         HttpGet request = new HttpGet( url );
 
         CouchObjectListDeserializer<T> deser = new CouchObjectListDeserializer<T>( itemType );
 
         CouchObjectList<T> listing =
             executeHttpWithResponse( request,
                                      deser.typeLiteral(),
                                      new ToString(
                                                    "Failed to retrieve contents for view request: %s",
                                                    req ), deser );
 
         return listing.getItems();
     }
 
     public <V> V getView( final ViewRequest req, final String dbUrl, final Class<V> type )
         throws CouchDBException
     {
         String url = buildViewUrl( dbUrl, req );
         HttpGet request = new HttpGet( url );
         return executeHttpWithResponse( request,
                                         type,
                                         new ToString(
                                                       "Failed to retrieve contents for view request: %s",
                                                       req ) );
     }
 
     public <T> T getDocument( final CouchDocRef ref, final String dbUrl, final Class<T> docType )
         throws CouchDBException
     {
         if ( !documentRevisionExists( ref, dbUrl ) )
         {
             return null;
         }
 
         String url = buildDocUrl( dbUrl, ref, true );
         HttpGet get = new HttpGet( url );
 
         return executeHttpWithResponse( get, new SerializedGetHandler<T>( serializer, docType ),
                                         new ToString( "Failed to retrieve document: %s", ref ) );
     }
 
     public boolean store( final CouchDocument doc, final String dbUrl, final boolean skipIfExists )
         throws CouchDBException
     {
        if ( skipIfExists && documentRevisionExists( doc, dbUrl ) )
         {
             return false;
         }
 
         HttpPost request = new HttpPost( dbUrl );
         try
         {
             request.setHeader( "Referer", dbUrl );
             String src = getSerializer().toString( doc );
             request.setEntity( new StringEntity( src, "application/json", "UTF-8" ) );
 
             executeHttp( request, SC_CREATED, "Failed to store document" );
         }
         catch ( UnsupportedEncodingException e )
         {
             throw new CouchDBException( "Failed to store document: %s.\nReason: %s", e, doc,
                                         e.getMessage() );
         }
 
         return true;
     }
 
     public void delete( final CouchDocument doc, final String dbUrl )
         throws CouchDBException
     {
        if ( !documentRevisionExists( doc, dbUrl ) )
         {
             return;
         }
 
         String url = buildDocUrl( dbUrl, doc, true );
         HttpDelete request = new HttpDelete( url );
         executeHttp( request, SC_OK, "Failed to delete document" );
     }
 
     public boolean viewExists( final String baseUrl, final String appName, final String viewName )
         throws CouchDBException
     {
         try
         {
             return exists( buildUrl( baseUrl, null, APP_BASE, appName, VIEW_BASE, viewName ) );
         }
         catch ( MalformedURLException e )
         {
             throw new CouchDBException( "Cannot format view URL for: %s in: %s. Reason: %s", e,
                                         viewName, appName, e.getMessage() );
         }
         catch ( CouchDBException e )
         {
             throw new CouchDBException( "Cannot verify existence of view: %s in: %s. Reason: %s",
                                         e, viewName, appName, e.getMessage() );
         }
     }
 
     public boolean appExists( final String baseUrl, final String appName )
         throws CouchDBException
     {
         try
         {
             return exists( buildUrl( baseUrl, null, APP_BASE, appName ) );
         }
         catch ( MalformedURLException e )
         {
             throw new CouchDBException( "Cannot format application URL: %s. Reason: %s", e,
                                         appName, e.getMessage() );
         }
         catch ( CouchDBException e )
         {
             throw new CouchDBException( "Cannot verify existence of application: %s. Reason: %s",
                                         e, appName, e.getMessage() );
         }
     }
 
     public boolean documentRevisionExists( final CouchDocument doc, final String dbUrl )
         throws CouchDBException
     {
         String docUrl = buildDocUrl( dbUrl, doc, doc.getCouchDocRev() != null );
         boolean exists = false;
 
         HttpHead request = new HttpHead( docUrl );
         HttpResponse response = executeHttp( request, "Failed to ping database URL" );
 
         StatusLine statusLine = response.getStatusLine();
         if ( statusLine.getStatusCode() == SC_OK )
         {
             exists = true;
         }
         else if ( statusLine.getStatusCode() != SC_NOT_FOUND )
         {
             HttpEntity entity = response.getEntity();
             CouchError error;
 
             try
             {
                 error = getSerializer().toError( entity );
             }
             catch ( IOException e )
             {
                 throw new CouchDBException(
                                             "Failed to ping database URL: %s.\nReason: %s\nError: Cannot read error status: %s",
                                             e, docUrl, statusLine, e.getMessage() );
             }
 
             throw new CouchDBException( "Failed to ping database URL: %s.\nReason: %s\nError: %s",
                                         docUrl, statusLine, error );
         }
 
         if ( exists )
         {
             Header etag = response.getFirstHeader( "Etag" );
             String rev = etag.getValue();
             if ( rev.startsWith( "\"" ) || rev.startsWith( "'" ) )
             {
                 rev = rev.substring( 1 );
             }
 
             if ( rev.endsWith( "\"" ) || rev.endsWith( "'" ) )
             {
                 rev = rev.substring( 0, rev.length() - 1 );
             }
 
             doc.setCouchDocRev( rev );
         }
 
         return exists;
     }
 
     public boolean exists( final CouchDocument doc, final String dbUrl )
         throws CouchDBException
     {
         String docUrl = buildDocUrl( dbUrl, doc, false );
         return exists( docUrl );
     }
 
     public boolean exists( final String url )
         throws CouchDBException
     {
         boolean exists = false;
 
         HttpHead request = new HttpHead( url );
         HttpResponse response = executeHttp( request, "Failed to ping database URL" );
 
         StatusLine statusLine = response.getStatusLine();
         if ( statusLine.getStatusCode() == SC_OK )
         {
             exists = true;
         }
         else if ( statusLine.getStatusCode() != SC_NOT_FOUND )
         {
             HttpEntity entity = response.getEntity();
             CouchError error;
 
             try
             {
                 error = getSerializer().toError( entity );
             }
             catch ( IOException e )
             {
                 throw new CouchDBException(
                                             "Failed to ping database URL: %s.\nReason: %s\nError: Cannot read error status: %s",
                                             e, url, statusLine, e.getMessage() );
             }
 
             throw new CouchDBException( "Failed to ping database URL: %s.\nReason: %s\nError: %s",
                                         url, statusLine, error );
         }
 
         return exists;
     }
 
     public boolean dbExists( final String url )
         throws CouchDBException
     {
         return exists( url );
     }
 
     public void dropDatabase( final String url )
         throws CouchDBException
     {
         if ( !dbExists( url ) )
         {
             return;
         }
 
         HttpDelete request = new HttpDelete( url );
         executeHttp( request, SC_OK, "Failed to drop database" );
     }
 
     public void createDatabase( final String url )
         throws CouchDBException
     {
         HttpPut request = new HttpPut( url );
         executeHttp( request, SC_CREATED, "Failed to create database" );
     }
 
     public void installApplication( final CouchApp app, final String dbUrl )
         throws CouchDBException
     {
         String url = buildDocUrl( dbUrl, app, true );
         HttpPut request = new HttpPut( url );
         try
         {
             request.setHeader( "Referer", dbUrl );
             String appJson = getSerializer().toString( app );
             request.setEntity( new StringEntity( appJson, "application/json", "UTF-8" ) );
 
             executeHttp( request, SC_CREATED, "Failed to store application document" );
         }
         catch ( UnsupportedEncodingException e )
         {
             throw new CouchDBException( "Failed to store application document: %s.\nReason: %s", e,
                                         app, e.getMessage() );
         }
     }
 
     protected HttpResponse executeHttp( final HttpRequestBase request, final String failureMessage )
         throws CouchDBException
     {
         return executeHttp( request, null, failureMessage );
     }
 
     protected HttpResponse executeHttp( final HttpRequestBase request,
                                         final Integer expectedStatus, final Object failureMessage )
         throws CouchDBException
     {
         String url = request.getURI().toString();
 
         try
         {
             HttpResponse response = getClient().execute( request );
             StatusLine statusLine = response.getStatusLine();
             if ( expectedStatus != null && statusLine.getStatusCode() != expectedStatus )
             {
                 HttpEntity entity = response.getEntity();
                 CouchError error = getSerializer().toError( entity );
                 throw new CouchDBException( "%s: %s.\nHTTP Response: %s\nError: %s",
                                             failureMessage, url, statusLine, error );
             }
 
             return response;
         }
         catch ( UnsupportedEncodingException e )
         {
             throw new CouchDBException( "%s: %s.\nReason: %s", e, failureMessage, url,
                                         e.getMessage() );
         }
         catch ( ClientProtocolException e )
         {
             throw new CouchDBException( "%s: %s.\nReason: %s", e, failureMessage, url,
                                         e.getMessage() );
         }
         catch ( IOException e )
         {
             throw new CouchDBException( "%s: %s.\nReason: %s", e, failureMessage, url,
                                         e.getMessage() );
         }
         finally
         {
             cleanup( request );
         }
     }
 
     protected <T> T executeHttpWithResponse( final HttpRequestBase request, final Type type,
                                              final Object failureMessage,
                                              final SerializationAdapter... adapters )
         throws CouchDBException
     {
         return executeHttpWithResponse( request, new SerializedGetHandler<T>( getSerializer(),
                                                                               type, adapters ),
                                         failureMessage );
     }
 
     protected <T> T executeHttpWithResponse( final HttpRequestBase request,
                                              final ResponseHandlerWithError<T> handler,
                                              final Object failureMessage )
         throws CouchDBException
     {
         String url = request.getURI().toString();
 
         try
         {
             T result = getClient().execute( request, handler );
             if ( result == null && handler.getError() != null )
             {
                 throw handler.getError();
             }
 
             return result;
         }
         catch ( UnsupportedEncodingException e )
         {
             throw new CouchDBException( "%s: %s.\nReason: %s", e, failureMessage, url,
                                         e.getMessage() );
         }
         catch ( ClientProtocolException e )
         {
             throw new CouchDBException( "%s: %s.\nReason: %s", e, failureMessage, url,
                                         e.getMessage() );
         }
         catch ( IOException e )
         {
             throw new CouchDBException( "%s: %s.\nReason: %s", e, failureMessage, url,
                                         e.getMessage() );
         }
         finally
         {
             cleanup( request );
         }
     }
 
     protected void cleanup( final HttpRequestBase request )
     {
         request.abort();
         getClient().getConnectionManager().closeExpiredConnections();
         getClient().getConnectionManager().closeIdleConnections( 2, TimeUnit.SECONDS );
     }
 
     protected String buildViewUrl( final String baseUrl, final ViewRequest req )
         throws CouchDBException
     {
         try
         {
             return buildUrl( baseUrl, req.getRequestParameters(), APP_BASE, req.getApplication(),
                              VIEW_BASE, req.getView() );
         }
         catch ( MalformedURLException e )
         {
             throw new CouchDBException( "Failed to format view URL for: %s.\nReason: %s", e, req,
                                         e.getMessage() );
         }
     }
 
     protected String buildDocUrl( final String baseUrl, final CouchDocument doc,
                                   final boolean includeRevision )
         throws CouchDBException
     {
         try
         {
             String url;
             if ( includeRevision && doc.getCouchDocRev() != null )
             {
                 Map<String, String> params = Collections.singletonMap( REV, doc.getCouchDocRev() );
                 url = buildUrl( baseUrl, params, doc.getCouchDocId() );
             }
             else
             {
                 url = buildUrl( baseUrl, null, doc.getCouchDocId() );
             }
 
             return url;
         }
         catch ( MalformedURLException e )
         {
             throw new CouchDBException(
                                         "Failed to format document URL for id: %s [revision=%s].\nReason: %s",
                                         e, doc.getCouchDocId(), doc.getCouchDocRev(),
                                         e.getMessage() );
         }
     }
 
     protected String buildUrl( final String baseUrl, final Map<String, String> params,
                                final String... parts )
         throws MalformedURLException
     {
         return UrlUtils.buildUrl( baseUrl, params, parts );
     }
 
     protected void threadedExecute( final Set<? extends CouchDocumentAction> actions,
                                     final String dbUrl )
         throws CouchDBException
     {
         CountDownLatch latch = new CountDownLatch( actions.size() );
         for ( CouchDocumentAction action : actions )
         {
             action.prepareExecution( latch, dbUrl, this );
             exec.execute( action );
         }
 
         synchronized ( latch )
         {
             while ( latch.getCount() > 0 )
             {
                 LOGGER.info( "Waiting for " + latch.getCount() + " actions to complete." );
                 try
                 {
                     latch.await( 2, TimeUnit.SECONDS );
                 }
                 catch ( InterruptedException e )
                 {
                     break;
                 }
             }
         }
 
         List<Throwable> errors = new ArrayList<Throwable>();
         for ( CouchDocumentAction action : actions )
         {
             if ( action.getError() != null )
             {
                 errors.add( action.getError() );
             }
         }
 
         if ( !errors.isEmpty() )
         {
             throw new CouchDBException( "Failed to execute %d actions.", errors.size() ).withNestedErrors( errors );
         }
     }
 
     protected Serializer getSerializer()
     {
         return serializer;
     }
 
     protected synchronized HttpClient getClient()
     {
         if ( client == null )
         {
             ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager();
             ccm.setMaxTotal( 20 );
 
             client = new DefaultHttpClient( ccm );
         }
         return client;
     }
 
 }
