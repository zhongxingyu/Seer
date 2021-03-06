 //$HeadURL$
 /*----------------------------------------------------------------------------
  This file is part of deegree, http://deegree.org/
  Copyright (C) 2001-2009 by:
  - Department of Geography, University of Bonn -
  and
  - lat/lon GmbH -
 
  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option)
  any later version.
  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  details.
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation, Inc.,
  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
  Contact information:
 
  lat/lon GmbH
  Aennchenstr. 19, 53177 Bonn
  Germany
  http://lat-lon.de/
 
  Department of Geography, University of Bonn
  Prof. Dr. Klaus Greve
  Postfach 1147, 53001 Bonn
  Germany
  http://www.geographie.uni-bonn.de/deegree/
 
  e-mail: info@deegree.org
  ----------------------------------------------------------------------------*/
 package org.deegree.services.wms;
 
 import static org.apache.commons.io.IOUtils.closeQuietly;
 import static org.deegree.protocol.wms.ops.SLDParser.getStyles;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TimerTask;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.deegree.commons.annotations.LoggingNotes;
 import org.deegree.commons.config.DeegreeWorkspace;
 import org.deegree.commons.utils.Pair;
 import org.deegree.commons.xml.XMLAdapter;
 import org.deegree.filter.Filter;
 import org.deegree.services.jaxb.wms.DirectStyleType;
 import org.deegree.services.jaxb.wms.SLDStyleType;
 import org.deegree.services.jaxb.wms.SLDStyleType.LegendGraphicFile;
 import org.deegree.style.StyleRef;
 import org.deegree.style.persistence.StyleStore;
 import org.deegree.style.persistence.StyleStoreManager;
 import org.deegree.style.se.parser.SymbologyParser;
 import org.deegree.style.se.unevaluated.Style;
 import org.slf4j.Logger;
 
 /**
  * <code>StyleRegistry</code>
  * 
  * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  */
 @LoggingNotes(info = "logs when style files cannot be loaded", trace = "logs stack traces", debug = "logs information about loading/reloading of style files")
 public class StyleRegistry extends TimerTask {
 
     private static final Logger LOG = getLogger( StyleRegistry.class );
 
     private HashMap<String, HashMap<String, Style>> registry = new HashMap<String, HashMap<String, Style>>();
 
     private HashMap<String, HashMap<String, Style>> legendRegistry = new HashMap<String, HashMap<String, Style>>();
 
     private HashMap<File, Pair<Long, String>> monitoredFiles = new HashMap<File, Pair<Long, String>>();
 
     private HashMap<File, Pair<Long, String>> monitoredLegendFiles = new HashMap<File, Pair<Long, String>>();
 
     private HashSet<String> soleStyleFiles = new HashSet<String>();
 
     private HashSet<String> soleLegendFiles = new HashSet<String>();
 
     private StyleStoreManager styleManager;
 
     private DeegreeWorkspace workspace;
 
     public StyleRegistry( DeegreeWorkspace workspace ) {
         this.workspace = workspace;
         this.styleManager = workspace.getSubsystemManager( StyleStoreManager.class );
     }
 
     /**
      * @param layerName
      * @param style
      * @param clear
      *            if true, all other styles will be removed for the layer
      */
     public void put( String layerName, Style style, boolean clear ) {
         HashMap<String, Style> styles = registry.get( layerName );
         if ( styles == null ) {
             styles = new HashMap<String, Style>();
             registry.put( layerName, styles );
             styles.put( "default", style );
         } else if ( clear ) {
             styles.clear();
             styles.put( "default", style );
         }
         if ( style.getName() == null ) {
             LOG.debug( "Overriding default style since new style does not have name." );
             styles.put( "default", style );
         } else {
             styles.put( style.getName(), style );
         }
     }
 
     /**
      * @param layerName
      * @param style
      * @param clear
      */
     public void putLegend( String layerName, Style style, boolean clear ) {
         HashMap<String, Style> styles = legendRegistry.get( layerName );
         if ( styles == null ) {
             styles = new HashMap<String, Style>();
             legendRegistry.put( layerName, styles );
             styles.put( "default", style );
         } else if ( clear ) {
             styles.clear();
             styles.put( "default", style );
         }
         if ( style.getName() == null ) {
             LOG.debug( "Overriding default style since new style does not have name." );
             styles.put( "default", style );
         } else {
             styles.put( style.getName(), style );
         }
     }
 
     /**
      * @param layerName
      * @param style
      */
     public void putAsDefault( String layerName, Style style ) {
         HashMap<String, Style> styles = registry.get( layerName );
         if ( styles == null ) {
             styles = new HashMap<String, Style>();
             registry.put( layerName, styles );
         }
         styles.put( "default", style );
         styles.put( style.getName(), style );
     }
 
     /**
      * @param layerName
      * @param styleName
      *            may be null, in which case the default style will be searched for
      * @return null, if not available
      */
     public Style getLegendStyle( String layerName, String styleName ) {
         if ( styleName == null ) {
             styleName = "default";
         }
         if ( legendRegistry.get( layerName ) != null && legendRegistry.get( layerName ).containsKey( styleName ) ) {
             return legendRegistry.get( layerName ).get( styleName );
         }
         return get( layerName, styleName );
     }
 
     /**
      * @param layerName
      * @param styleName
      * @return true, if the layer has a style with the name
      */
     public boolean hasStyle( String layerName, String styleName ) {
         HashMap<String, Style> styles = registry.get( layerName );
         return styles != null && styles.get( styleName ) != null;
     }
 
     /**
      * @param layerName
      * @param styleName
      *            may be null, in which case the default style will be searched for
      * @return null, if not available
      */
     public Style get( String layerName, String styleName ) {
         HashMap<String, Style> styles = registry.get( layerName );
         if ( styles == null ) {
             return null;
         }
         if ( styleName == null ) {
             styleName = "default";
         }
         return styles.get( styleName );
     }
 
     /**
      * @param layerName
      * @return all styles for the layer
      */
     public ArrayList<Style> getAll( String layerName ) {
         HashMap<String, Style> map = registry.get( layerName );
         if ( map == null ) {
             return new ArrayList<Style>();
         }
         ArrayList<Style> styles = new ArrayList<Style>( map.size() );
         styles.addAll( map.values() );
         return styles;
     }
 
     private Style loadNoImport( String layerName, File file, boolean legend ) {
         XMLInputFactory fac = XMLInputFactory.newInstance();
         FileInputStream in = null;
         try {
 
             LOG.debug( "Trying to load{} style from '{}'", legend ? "" : " legend", file );
             in = new FileInputStream( file );
             Style sty = SymbologyParser.INSTANCE.parse( fac.createXMLStreamReader( file.toString(), in ) );
 
             if ( legend ) {
                 monitoredLegendFiles.put( file, new Pair<Long, String>( file.lastModified(), layerName ) );
             } else {
                 monitoredFiles.put( file, new Pair<Long, String>( file.lastModified(), layerName ) );
             }
             return sty;
         } catch ( FileNotFoundException e ) {
             LOG.info( "Style file '{}' for layer '{}' could not be found: '{}'",
                       new Object[] { file, layerName, e.getLocalizedMessage() } );
         } catch ( XMLStreamException e ) {
             LOG.trace( "Stack trace:", e );
             LOG.info( "Style file '{}' for layer '{}' could not be loaded: '{}'",
                       new Object[] { file, layerName, e.getLocalizedMessage() } );
         } finally {
             closeQuietly( in );
         }
         return null;
     }
 
     /**
      * @param layerName
      * @param file
      * @return true, if actually loaded
      */
     public boolean load( String layerName, File file ) {
         Style sty = loadNoImport( layerName, file, false );
         if ( sty != null ) {
             put( layerName, sty, soleStyleFiles.contains( file.getName() ) );
             return true;
         }
         return false;
     }
 
     /**
      * @param layerName
      * @param file
      * @return true, if actually loaded
      */
     public boolean loadLegend( String layerName, File file ) {
         Style sty = loadNoImport( layerName, file, true );
         if ( sty != null ) {
             putLegend( layerName, sty, soleLegendFiles.contains( file.getName() ) );
             return true;
         }
         return false;
     }
 
     /**
      * @param layerName
      * @param styles
      * @param adapter
      */
     public void load( String layerName, List<DirectStyleType> styles, XMLAdapter adapter ) {
         File stylesDir = new File( workspace.getLocation(), "styles" );
         for ( DirectStyleType sty : styles ) {
             try {
                 File file = new File( adapter.resolve( sty.getFile() ).toURI() );
 
                 Style style;
                 if ( file.getParentFile().equals( stylesDir ) ) {
                     // already loaded from style store
                     String styleId = file.getName().substring( 0, file.getName().length() - 4 );
                     StyleStore store = styleManager.get( styleId );
                     if ( store != null ) {
                         style = store.getStyle( null );
                     } else {
                         LOG.warn( "Style store {} was not available, trying to load directly.", styleId );
                         style = loadNoImport( layerName, file, false );
                     }
                 } else {
                     // outside of workspace - load it manually
                     style = loadNoImport( layerName, file, false );
                 }
                 if ( style != null ) {
                     String name = sty.getName();
                     if ( name != null ) {
                         style.setName( name );
                     }
                     if ( styles.size() == 1 ) {
                         soleStyleFiles.add( file.getName() );
                     }
                     put( layerName, style, false );
                     if ( sty.getLegendGraphicFile() != null ) {
                         URL url = adapter.resolve( sty.getLegendGraphicFile().getValue() );
                         if ( url.toURI().getScheme().equals( "file" ) ) {
                             File legend = new File( url.toURI() );
                             style.setLegendFile( legend );
                         } else {
                             style.setLegendURL( url );
                         }
                         style.setPrefersGetLegendGraphicUrl( sty.getLegendGraphicFile().isOutputGetLegendGraphicUrl() );
                     }
                 }
             } catch ( MalformedURLException e ) {
                 LOG.trace( "Stack trace", e );
                 LOG.info( "Style file '{}' for layer '{}' could not be resolved.", sty.getFile(), layerName );
             } catch ( URISyntaxException e ) {
                 LOG.trace( "Stack trace", e );
                 LOG.info( "Style file '{}' for layer '{}' could not be resolved.", sty.getFile(), layerName );
             }
             try {
                 if ( sty.getLegendConfigurationFile() != null ) {
                     File file = new File( adapter.resolve( sty.getLegendConfigurationFile() ).toURI() );
                     Style style;
                     if ( file.getParentFile().equals( stylesDir ) ) {
                         // already loaded from style store
                         String styleId = file.getName().substring( 0, file.getName().length() - 4 );
                         StyleStore store = styleManager.get( styleId );
                         if ( store != null ) {
                             style = store.getStyle( null );
                         } else {
                             LOG.warn( "Style store {} was not available, trying to load directly.", styleId );
                             style = loadNoImport( layerName, file, true );
                         }
                     } else {
                         // outside of workspace - load it manually
                         style = loadNoImport( layerName, file, true );
                     }
 
                     String name = sty.getName();
                     if ( style != null ) {
                         if ( name != null ) {
                             style.setName( name );
                         }
                         if ( styles.size() == 1 ) {
                             soleLegendFiles.add( file.getName() );
                         }
                         putLegend( layerName, style, false );
                     }
                 }
             } catch ( MalformedURLException e ) {
                 LOG.trace( "Stack trace", e );
                 LOG.info( "Style file '{}' for layer '{}' could not be resolved.", sty.getFile(), layerName );
             } catch ( URISyntaxException e ) {
                 LOG.trace( "Stack trace", e );
                 LOG.info( "Style file '{}' for layer '{}' could not be resolved.", sty.getFile(), layerName );
             }
         }
     }
 
     /**
      * @param layerName
      * @param styles
      * @param adapter
      */
     public void load( String layerName, XMLAdapter adapter, List<SLDStyleType> styles ) {
         File stylesDir = new File( workspace.getLocation(), "styles" );
 
         for ( SLDStyleType sty : styles ) {
             FileInputStream is = null;
             try {
                 File file = new File( adapter.resolve( sty.getFile() ).toURI() );
 
                 String namedLayer = sty.getNamedLayer();
                 LOG.debug( "Will read styles from SLD '{}', for named layer '{}'.", file, namedLayer );
                 Map<String, String> map = new HashMap<String, String>();
                 Map<String, Pair<File, URL>> legends = new HashMap<String, Pair<File, URL>>();
                 Map<String, Boolean> glgUrls = new HashMap<String, Boolean>();
                 String name = null, lastName = null;
                 for ( JAXBElement<?> elem : sty.getNameAndUserStyleAndLegendConfigurationFile() ) {
                     if ( elem.getName().getLocalPart().equals( "Name" ) ) {
                         name = elem.getValue().toString();
                     } else if ( elem.getName().getLocalPart().equals( "LegendConfigurationFile" ) ) {
                         File legendFile = new File( adapter.resolve( elem.getValue().toString() ).toURI() );
                         Style style;
 
                         if ( legendFile.getParentFile().equals( stylesDir ) ) {
                             // already loaded from style store
                             String styleId = legendFile.getName().substring( 0, legendFile.getName().length() - 4 );
                             StyleStore store = styleManager.get( styleId );
                             if ( store != null ) {
                                 style = store.getStyle( null );
                             } else {
                                 LOG.warn( "Style store {} was not available, trying to load directly.", styleId );
                                 style = loadNoImport( layerName, legendFile, true );
                             }
                         } else {
                             style = loadNoImport( layerName, legendFile, true );
                         }
 
                         if ( style != null ) {
                             if ( name != null ) {
                                 style.setName( name );
                             }
                             putLegend( layerName, style, false );
                         }
                     } else if ( elem.getName().getLocalPart().equals( "LegendGraphicFile" ) ) {
                         LegendGraphicFile lgf = (LegendGraphicFile) elem.getValue();
                         URL url = adapter.resolve( lgf.getValue() );
                         if ( url.toURI().getScheme().equals( "file" ) ) {
                             File legend = new File( url.toURI() );
                             legends.put( lastName, new Pair<File, URL>( legend, null ) );
                         } else {
                             legends.put( lastName, new Pair<File, URL>( null, url ) );
                         }
                         glgUrls.put( lastName, lgf.isOutputGetLegendGraphicUrl() );
                     } else if ( elem.getName().getLocalPart().equals( "UserStyle" ) ) {
                         if ( name == null ) {
                             name = elem.getValue().toString();
                         }
                         LOG.debug( "Will load user style with name '{}', it will be known as '{}'.", elem.getValue(),
                                    name );
                         map.put( elem.getValue().toString(), name );
                         lastName = name;
                         name = null;
                     }
                 }
 
                 if ( file.getParentFile().equals( stylesDir ) ) {
                     // already loaded from workspace
                     String styleId = file.getName().substring( 0, file.getName().length() - 4 );
                     StyleStore store = styleManager.get( styleId );
 
                     if ( store != null ) {
                         LOG.info( "Using SLD file loaded from style store." );
                         for ( Style s : store.getAll( namedLayer ) ) {
                            s.setName( map.get( s.getName() ) );
                             put( layerName, s, false );
                             Pair<File, URL> p = legends.get( s.getName() );
                             if ( p != null && p.first != null ) {
                                 s.setLegendFile( p.first );
                             } else if ( p != null ) {
                                 s.setLegendURL( p.second );
                             }
                             s.setPrefersGetLegendGraphicUrl( glgUrls.get( s.getName() ) != null
                                                              && glgUrls.get( s.getName() ) );
                         }
                         return;
                     }
                 }
 
                 LOG.info( "Parsing SLD style file unavailable from style stores." );
                 XMLInputFactory fac = XMLInputFactory.newInstance();
                 is = new FileInputStream( file );
                 XMLStreamReader in = fac.createXMLStreamReader( file.toURI().toURL().toString(), is );
                 Pair<LinkedList<Filter>, LinkedList<StyleRef>> parsedStyles = getStyles( in, namedLayer, map );
                 for ( StyleRef s : parsedStyles.second ) {
                     if ( !s.isResolved() ) {
                         continue;
                     }
                     put( layerName, s.getStyle(), false );
                     Pair<File, URL> p = legends.get( s.getName() );
                     if ( p != null && p.first != null ) {
                         s.getStyle().setLegendFile( p.first );
                     } else if ( p != null ) {
                         s.getStyle().setLegendURL( p.second );
                     }
                     s.getStyle().setPrefersGetLegendGraphicUrl( glgUrls.get( s.getStyle().getName() ) != null
                                                                                         && glgUrls.get( s.getStyle().getName() ) );
                 }
             } catch ( MalformedURLException e ) {
                 LOG.trace( "Stack trace", e );
                 LOG.info( "Style file '{}' for layer '{}' could not be resolved.", sty.getFile(), layerName );
             } catch ( FileNotFoundException e ) {
                 LOG.trace( "Stack trace", e );
                 LOG.info( "Style file '{}' for layer '{}' could not be found.", sty.getFile(), layerName );
             } catch ( XMLStreamException e ) {
                 LOG.trace( "Stack trace", e );
                 LOG.info( "Style file '{}' for layer '{}' could not be parsed: '{}'.",
                           new Object[] { sty.getFile(), layerName, e.getLocalizedMessage() } );
             } catch ( URISyntaxException e ) {
                 LOG.trace( "Stack trace", e );
                 LOG.info( "Style file '{}' for layer '{}' could not be resolved.", sty.getFile(), layerName );
             } finally {
                 closeQuietly( is );
             }
         }
     }
 
     /**
      * @param layerName
      * @param file
      * @param isSoleStyle
      *            if true, all styles will be cleared upon update
      * @return true, if actually loaded
      */
     public boolean register( String layerName, File file, boolean isSoleStyle ) {
         if ( !monitoredFiles.containsKey( file ) ) {
             if ( isSoleStyle ) {
                 soleStyleFiles.add( file.getName() );
             }
             return load( layerName, file );
         }
 
         return false;
     }
 
     @Override
     public void run() {
         for ( File f : monitoredFiles.keySet() ) {
             Pair<Long, String> pair = monitoredFiles.get( f );
             if ( f.lastModified() != pair.first ) {
                 LOG.debug( "Reloading style file '{}'", f );
                 load( pair.second, f );
             }
         }
         for ( File f : monitoredLegendFiles.keySet() ) {
             Pair<Long, String> pair = monitoredLegendFiles.get( f );
             if ( f.lastModified() != pair.first ) {
                 LOG.debug( "Reloading legend style file '{}'", f );
                 loadLegend( pair.second, f );
             }
         }
     }
 
 }
