 package org.mosaic.web.handler.impl;
 
 import com.google.common.net.MediaType;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.Arrays;
 import java.util.Collection;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.eclipse.jetty.http.MimeTypes;
 import org.eclipse.jetty.util.B64Code;
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.mosaic.modules.Service;
 import org.mosaic.web.application.Application;
 import org.mosaic.web.handler.RequestHandler;
 import org.mosaic.web.request.WebRequest;
 import org.mosaic.web.request.WebResponse;
 
 import static java.nio.file.Files.copy;
 import static org.mosaic.web.request.HttpStatus.*;
 
 /**
  * @author arik
  */
 @Service
 public class StaticResourcesRequestHandler implements RequestHandler
 {
     @Nonnull
     private final MimeTypes mimeTypes = new MimeTypes();
 
     @Override
     public boolean canHandle( @Nonnull WebRequest request )
     {
         String path = request.getUri().getDecodedPath();
         Application.ApplicationResources.Resource resource = request.getApplication().getResources().getResource( path );
         if( resource != null )
         {
             request.getAttributes().put( "resource", resource );
             return true;
         }
         else
         {
             return false;
         }
     }
 
     @Override
     public void handle( @Nonnull WebRequest request ) throws Throwable
     {
         Application.ApplicationResources.Resource resource = request.getAttributes().require( "resource", Application.ApplicationResources.Resource.class );
         if( resource.isDirectory() )
         {
             serveDirectory( request, resource );
         }
         else
         {
             serveFile( request, resource );
         }
     }
 
     private void serveDirectory( @Nonnull WebRequest request,
                                  @Nonnull Application.ApplicationResources.Resource resource )
     {
         // TODO: handle directory listing (if enabled)
         request.getResponse().setStatus( NOT_IMPLEMENTED );
         request.getResponse().disableCaching();
     }
 
     private void serveFile( @Nonnull WebRequest request, @Nonnull Application.ApplicationResources.Resource resource )
             throws IOException
     {
         Path file = resource.getPath();
         WebResponse response = request.getResponse();
 
         applyCaching( resource, response );
         applyContentType( file, response );
         applyContentLength( file, response );
         DateTime lastModified = applyLastModified( file, response );
         String etag = applyETag( file, response );
 
         if( request.getMethod().equalsIgnoreCase( "OPTIONS" ) )
         {
             response.getHeaders().setAllow( Arrays.asList( "GET", "HEAD", "OPTIONS" ) );
         }
         else if( request.getMethod().equalsIgnoreCase( "GET" ) )
         {
             Collection<String> ifMatch = request.getHeaders().getIfMatch();
             if( !ifMatch.isEmpty() )
             {
                 if( !ifMatch.contains( "*" ) && !ifMatch.contains( etag ) )
                 {
                     response.setStatus( PRECONDITION_FAILED );
                     return;
                 }
             }
 
             Collection<String> ifNoneMatch = request.getHeaders().getIfNoneMatch();
             if( !ifNoneMatch.isEmpty() && etag != null )
             {
                 if( ifNoneMatch.contains( "*" ) || ifNoneMatch.contains( etag ) )
                 {
                     response.setStatus( NOT_MODIFIED );
                     return;
                 }
             }
 
             DateTime ifModifiedSince = request.getHeaders().getIfModifiedSince();
            if( ifModifiedSince != null && lastModified != null && lastModified.isBefore( ifModifiedSince ) )
             {
                 // TODO: according to RFC-2616 14.26, "If-Modified-Since" can override "If-None-Match", we need to support that
                 response.setStatus( NOT_MODIFIED );
                 return;
             }
 
             DateTime ifUnmodifiedSince = request.getHeaders().getIfUnmodifiedSince();
            if( ifUnmodifiedSince != null && lastModified != null && lastModified.isAfter( ifUnmodifiedSince ) )
             {
                 response.setStatus( PRECONDITION_FAILED );
                 return;
             }
             copy( file, response.stream() );
         }
         else if( !request.getMethod().equalsIgnoreCase( "HEAD" ) )
         {
             response.setStatus( METHOD_NOT_ALLOWED );
         }
     }
 
     private void applyCaching( @Nonnull Application.ApplicationResources.Resource resource,
                                @Nonnull WebResponse response )
     {
         Period cachePeriod = resource.getCachePeriod();
         if( cachePeriod != null )
         {
             response.getHeaders().setCacheControl( "must-revalidate, private, max-age=" + cachePeriod.toStandardSeconds().getSeconds() );
             response.getHeaders().setExpires( DateTime.now().withPeriodAdded( cachePeriod, 1 ) );
         }
         else
         {
             response.disableCaching();
         }
     }
 
     @Nullable
     private MediaType applyContentType( @Nonnull Path file, @Nonnull WebResponse response )
     {
         String mimeType = this.mimeTypes.getMimeByExtension( file.getFileName().toString() );
         if( mimeType != null )
         {
             MediaType mediaType = MediaType.parse( mimeType );
             response.getHeaders().setContentType( mediaType );
             return mediaType;
         }
         else
         {
             return null;
         }
     }
 
     @Nullable
     private Long applyContentLength( @Nonnull Path file, @Nonnull WebResponse response )
     {
         try
         {
             long contentLength = Files.size( file );
             response.getHeaders().setContentLength( contentLength );
             return contentLength;
         }
         catch( IOException ignore )
         {
             return null;
         }
     }
 
     @Nullable
     private DateTime applyLastModified( @Nonnull Path file, @Nonnull WebResponse response )
     {
         try
         {
             DateTime lastModified = new DateTime( Files.getLastModifiedTime( file ).toMillis() ).withMillisOfSecond( 0 );
             response.getHeaders().setLastModified( lastModified );
             return lastModified;
         }
         catch( IOException ignore )
         {
             return null;
         }
     }
 
     @Nullable
     private String applyETag( @Nonnull Path file, @Nonnull WebResponse response )
     {
         try
         {
             StringBuilder b = new StringBuilder( 32 );
             b.append( "W/\"" );
 
             String name = file.getFileName().toString();
             int length = name.length();
             long lhash = 0;
             for( int i = 0; i < length; i++ )
             {
                 lhash = 31 * lhash + name.charAt( i );
             }
 
             DateTime lastModified = response.getHeaders().getLastModified();
             Long contentLength = response.getHeaders().getContentLength();
             B64Code.encode( ( lastModified == null ? -1 : lastModified.getMillis() ) ^ lhash, b );
             B64Code.encode( ( contentLength == null ? -1 : contentLength ) ^ lhash, b );
             b.append( '"' );
 
             String etag = b.toString();
             response.getHeaders().setETag( etag );
             return etag;
         }
         catch( Exception ignore )
         {
             return null;
         }
     }
 }
