 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package novelang.daemon;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.File;
 import java.net.URL;
 import java.net.MalformedURLException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.mortbay.jetty.Request;
 import novelang.system.LogFactory;
 import novelang.system.Log;
 import novelang.configuration.ProducerConfiguration;
 import novelang.loader.ResourceLoader;
 import novelang.loader.ResourceName;
 import novelang.loader.ResourceNotFoundException;
 import novelang.loader.ResourceLoaderTools;
 import novelang.loader.UrlResourceLoader;
 import novelang.produce.PolymorphicRequest;
 import novelang.produce.RequestTools;
 
 /**
  * Holds resources which don't require rendering.
  *
  * @author Laurent Caillette
  */
 public class ResourceHandler extends GenericHandler {
 
   private static final Log LOG = LogFactory.getLog( ResourceHandler.class ) ;
 
   private final ResourceLoader resourceLoader ;
 
   public ResourceHandler( final ProducerConfiguration serverConfiguration ) {
     this(
         ResourceLoaderTools.compose(
             serverConfiguration.getRenderingConfiguration().getResourceLoader(),
             new UrlResourceLoader( createUrlQuiet(
                 serverConfiguration.getContentConfiguration().getContentRoot() ) )
         )
     ) ;
   }
 
   /**
    * Dirty hack.
    */
   private static URL createUrlQuiet( final File file ) {
     try {
       return file.toURL() ;
     } catch( MalformedURLException e ) {
       throw new RuntimeException( e ) ;
     }
   }
 
   protected ResourceHandler( final ResourceLoader resourceLoader ) {
     this.resourceLoader = resourceLoader ;
     LOG.debug( "Using resourceLoader %s", resourceLoader ) ;
   }
 
   protected void doHandle(
       final String target,
       final HttpServletRequest request,
       final HttpServletResponse response,
       final int dispatch
   )
       throws IOException, ServletException
   {
     LOG.debug( "Attempting to handle %s", request.getRequestURI() ) ;
 
     final PolymorphicRequest documentRequest = 
         RequestTools.createPolymorphicRequest( request.getPathInfo() ) ;
 
     if( null != documentRequest ) {
 
       try {
         final String resourceName = removeLeadingSolidus(
             documentRequest.getDocumentSourceName() + "." +
             documentRequest.getResourceExtension()
         ) ;
         final InputStream inputStream = resourceLoader.getInputStream(
             new ResourceName( resourceName ) ) ;
 
        IOUtils.copy( inputStream, response.getOutputStream() ) ;
 
         response.setStatus( HttpServletResponse.SC_OK ) ;
 
         final String contentType =
             ResourceMimeTypes.getMimeType( documentRequest.getResourceExtension() ) ;
         if( null != contentType ) {
           response.setContentType( contentType ) ;
         }
 
         ( ( Request ) request ).setHandled( true ) ;
         LOG.debug(
             "Handled request '%s' with content-type '%s'",
             request.getRequestURI(), 
             contentType 
         ) ;
         
       } catch( ResourceNotFoundException e ) {
         LOG.debug( "Could not serve %s", request.getRequestURI() ) ;
         // Then do nothing, we just don't handle that request.
       }
 
     }
   }
 
   private static String removeLeadingSolidus( final String s ) {
     if( s.startsWith( "/" ) ) {
       return s.substring( 1 ) ;
     } else {
       return s ;
     }
   }
 }
