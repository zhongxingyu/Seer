 /*
  * Copyright (C) 2010 Laurent Caillette
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
 package org.novelang.configuration;
 
 import java.io.File;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Function;
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableSet;
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.DefaultConfiguration;
 import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
 import org.apache.avalon.framework.configuration.MutableConfiguration;
 import org.apache.fop.apps.FOPException;
 import org.apache.fop.apps.FOUserAgent;
 import org.apache.fop.apps.FopFactory;
 import org.apache.fop.fonts.EmbedFontInfo;
 import org.apache.fop.fonts.FontCache;
 import org.apache.fop.fonts.FontEventListener;
 import org.apache.fop.fonts.FontResolver;
 import org.apache.fop.render.DefaultFontResolver;
 import org.apache.fop.render.pdf.PDFRendererConfigurator;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 import org.fest.reflect.core.Reflection;
 import org.fest.reflect.reference.TypeRef;
 import org.novelang.logger.Logger;
 import org.novelang.logger.LoggerFactory;
 
 /**
  * Utility class for generating FOP configuration with hyphenation files and custom fonts.
  * 
  * @author Laurent Caillette
  */
 public class FopTools {
 
   private static final Logger LOGGER = LoggerFactory.getLogger( FopTools.class );
 
   private static final String CONFIGURATION_NOT_SERIALIZED =
       "Could not serialize configuration to string" ;
 
   public static final Function< ? super EmbedFontInfo,? extends String >
       EXTRACT_EMBEDFONTINFO_FUNCTION = new Function< EmbedFontInfo, String >() {
         @Override
         public String apply( final EmbedFontInfo embedFontInfo ) {
           return embedFontInfo.getEmbedFile() ;
         }
       }
   ;
 
   private FopTools() {}
 
   /**
    * Creates a {@code Configuration} object with {@code <renderer>} as element root,
    * using font directories as set by system property.
    */
   public static Configuration createPdfRendererConfiguration(
       final Iterable < File > fontDirectories
   ) {
     final MutableConfiguration renderer = new DefaultConfiguration( "renderer" ) ;
     renderer.setAttribute( "mime", "application/pdf" ) ;
     final MutableConfiguration fonts = new DefaultConfiguration( "fonts" ) ;
 
     for( final File fontDirectory : fontDirectories ) {
       final MutableConfiguration directory = new DefaultConfiguration( "directory" ) ;
       directory.setAttribute( "recurse", "true" ) ;
       directory.setValue( fontDirectory.getAbsolutePath() ) ;
       fonts.addChild( directory ) ;
     }
     renderer.addChild( fonts ) ;
     return renderer ;
   }
 
   /**
    * Creates a {@code Configuration} object with {@code <renderers>} element as root.
    */
   public static Configuration createRenderersConfiguration(
       final Iterable< File > fontDirectories
   ) {
     final MutableConfiguration renderers = new DefaultConfiguration( "renderers" ) ;
     renderers.addChild( createPdfRendererConfiguration( fontDirectories ) ) ;
     return renderers ;
   }
 
   public static Configuration createHyphenationConfiguration( final File hyphenationDirectory ) {
     final URL hyphenationBaseUrl ;
     try {
       hyphenationBaseUrl = hyphenationDirectory.toURI().toURL() ;
     } catch( MalformedURLException e ) {
       throw new RuntimeException( e ) ;
     }
     final MutableConfiguration hyphenationBase = new DefaultConfiguration( "hyphenation-base" ) ;
     hyphenationBase.setValue( hyphenationBaseUrl.toExternalForm() ) ;
     return hyphenationBase ;
   }
 
   public static String configurationAsString( final Configuration configuration ) {
     try {
       final StringWriter stringWriter = new StringWriter() ;
       final OutputFormat format = OutputFormat.createPrettyPrint() ;
       final XMLWriter xmlWriter = new XMLWriter( stringWriter, format ) ;
       new DefaultConfigurationSerializer().serialize( xmlWriter, configuration ) ;
       xmlWriter.close() ;
 
       return stringWriter.toString() ;
     } catch( Exception e ) {
       LOGGER.error( e, CONFIGURATION_NOT_SERIALIZED ) ;
       throw Throwables.propagate( e ) ;
     }
   }
 
   private static ImmutableSet< String > extractFailedFontMap( final FOUserAgent foUserAgent ) {
     final FontCache fontCache = foUserAgent.getFactory().getFontManager().getFontCache() ;
     final Map< String, Long > fieldValue = Reflection.field( "failedFontMap" )
         .ofType( new TypeRef< Map< String, Long > >() {} ).in( fontCache ).get() ;
     return ImmutableSet.copyOf( fieldValue.keySet() ) ;
   }
 
   public static FopFontStatus createGlobalFontStatus(
       final FopFactory fopFactory,
       final Iterable< File > fontDirectories
   ) throws FOPException {
     final Configuration pdfRendererConfiguration =
         createPdfRendererConfiguration( fontDirectories ) ;
     final FOUserAgent foUserAgent = fopFactory.newFOUserAgent() ;
     final FontResolver fontResolver = new DefaultFontResolver( foUserAgent ) ;
     final FontCache fontCache = new FontCache() ;
 
     @SuppressWarnings( { "unchecked" } )
     final List< EmbedFontInfo > fontList = ( List< EmbedFontInfo > )
         new PDFRendererConfigurator( foUserAgent ) {
           @Override
           protected List buildFontList(
               final Configuration configuration,
               final FontResolver resolver,
               final FontEventListener listener
           ) throws FOPException {
             return super.buildFontList( configuration, resolver, listener ) ;
           }
         }.buildFontList(
             pdfRendererConfiguration,
             fontResolver,
             null
         )
     ;
     final ImmutableSet< String > failedFontMap = extractFailedFontMap( foUserAgent ) ;
     return new FopFontStatus( fontList, failedFontMap ) ;
   }
 
 
   /*package*/ static FopFactory createFopFactory(
       final Iterable< File > fontsDirectories,
       final File hyphenationDirectory
   )
       throws FOPException
   {
     final FopFactory fopFactory = FopFactory.newInstance() ;
     Configuration renderers = null ;
     Configuration hyphenationBase = null ;
     boolean configure = false ;
 
     if( fontsDirectories.iterator().hasNext() ) {
       renderers = createRenderersConfiguration( fontsDirectories ) ;
       configure = true ;
     }
 
     if( null != hyphenationDirectory ) {
       hyphenationBase = createHyphenationConfiguration( hyphenationDirectory ) ;
       configure = true ;
     }
 
     if( configure ) {
 
       final MutableConfiguration configuration = new DefaultConfiguration( "fop" ) ;
       configuration.setAttribute( "version", "1.0" ) ;
 
       if( null != renderers ) {
         configuration.addChild( renderers ) ;
       }
 
       if( null != hyphenationBase ) {
         configuration.addChild( hyphenationBase ) ;
       }
 
       LOGGER.debug( "Created configuration: \n", configurationAsString( configuration ) ) ;
 
       fopFactory.setUserConfig( configuration ) ;
     }
 
     return fopFactory ;
   }
 }
