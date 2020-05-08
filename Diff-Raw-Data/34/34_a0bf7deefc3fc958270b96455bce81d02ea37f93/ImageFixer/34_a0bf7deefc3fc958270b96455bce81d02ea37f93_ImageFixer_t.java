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
 package novelang.part ;
 
 import com.google.common.base.Preconditions;
 import novelang.common.*;
 import novelang.common.tree.Treepath;
 import novelang.common.tree.TreepathTools;
 import novelang.parser.NodeKind;
 import novelang.loader.ResourceLoader;
 import novelang.loader.ClasspathResourceLoader;
 import novelang.loader.ResourceName;
 import novelang.configuration.ConfigurationTools;
 import org.apache.commons.lang.ClassUtils;
 import org.apache.commons.lang.StringUtils;
 import novelang.system.LogFactory;
 import novelang.system.Log;
 import org.dom4j.io.SAXReader;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Node;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import java.io.File;
 import java.io.IOException;
 import java.awt.image.BufferedImage;
 import javax.imageio.ImageIO;
 
 /**
  * Transforms the path of embeddable resources (like {@link NodeKind#RASTER_IMAGE} or
  * {@link NodeKind#VECTOR_IMAGE}) that are initially relative to the Part they are referenced from,
  * into a path relative to the base directory of the content.
  * This is done by changing the text of the {@link NodeKind#RESOURCE_LOCATION} node.
  * Also reads resolution of bitmap images and adds corresponding nodes.
  *
  * @author Laurent Caillette
  */
 public class ImageFixer {
   
   private static final Log LOG = LogFactory.getLog( ImageFixer.class ) ;
   
   private final File baseDirectory;
   private final File referrerDirectory ;
   private final ProblemCollector problemCollector ;
 
   public ImageFixer(
       File baseDirectory, 
       File referrerDirectory, 
       ProblemCollector problemCollector 
   ) {
     Preconditions.checkNotNull( baseDirectory ) ;
     Preconditions.checkArgument( baseDirectory.exists(), "Does not exist: '%s'", baseDirectory ) ;
     Preconditions.checkArgument( baseDirectory.isDirectory() ) ;
     Preconditions.checkNotNull( referrerDirectory ) ;
     Preconditions.checkArgument( referrerDirectory.exists() ) ;
     Preconditions.checkArgument( referrerDirectory.isDirectory() ) ;
     
     Preconditions.checkArgument( 
         FileTools.isParentOfOrSameAs( baseDirectory, referrerDirectory ),
         "Base directory '%s' should be parent of referrer directory '%s'",
         baseDirectory, 
         referrerDirectory 
     ) ;
     this.baseDirectory = baseDirectory;
     this.referrerDirectory = referrerDirectory ;
     this.problemCollector = problemCollector ;
     LOG.debug(
         "Created %s contentRoot: '%s', referrerDirectory: '%s'",
         ClassUtils.getShortClassName( getClass() ),
         baseDirectory.getAbsolutePath(),
         referrerDirectory.getAbsolutePath()
 
     ) ;
   }
 
   public SyntacticTree relocateResources( final SyntacticTree tree ) {
     return relocateAllResources( Treepath.create( tree ) ).getTreeAtEnd() ;
   }
   
   private Treepath< SyntacticTree > relocateAllResources( Treepath< SyntacticTree > treepath ) {
     final SyntacticTree tree = treepath.getTreeAtEnd() ;
     if( tree.isOneOf( NodeKind.RASTER_IMAGE, NodeKind.VECTOR_IMAGE ) ) {
       treepath = fixImage( treepath ) ;
     } else {
       final int childCount = tree.getChildCount() ;
       for( int i = 0 ; i < childCount ; i++ ) {
         treepath = relocateAllResources( Treepath.create( treepath, i ) ).getPrevious() ;        
       }
     }
     return treepath ;
   }
 
   private Treepath< SyntacticTree > fixImage(
       Treepath< SyntacticTree > treepathToImage
   ) {
     final SyntacticTree imageTree = treepathToImage.getTreeAtEnd() ;
     for( int i = 0 ; i < imageTree.getChildCount() ; i++ ) {
       final SyntacticTree child = imageTree.getChildAt( i ) ;
       if( child.isOneOf( NodeKind.RESOURCE_LOCATION ) ) {
         final String oldLocation = child.getChildAt( 0 ).getText() ;
         final String newLocation ;
         try {
           newLocation = relocate( oldLocation ) ;
         } catch ( ImageFixerException e ) {
           LOG.debug( "%s got exception: %s", // Just debug level, exception will raise later.
               ClassUtils.getShortClassName( getClass() ), e.getMessage() ) ;
           problemCollector.collect( Problem.createProblem( e ) ) ;          
           return treepathToImage ; // Leave unchanged.
         }
         LOG.debug( "Replacing '%s' by '%s'", oldLocation, newLocation ) ;
         final Treepath< SyntacticTree > treepathToResourceLocation = 
             Treepath.create( treepathToImage, i, 0 ) ;
         treepathToImage = TreepathTools.replaceTreepathEnd(
             treepathToResourceLocation, 
             new SimpleTree( newLocation ) 
         ).getPrevious().getPrevious() ;
         treepathToImage = addImageMetadata( treepathToImage, newLocation ) ;
         return treepathToImage ;
       }
     }    
     throw new IllegalArgumentException( 
         "Missing child " + NodeKind.RESOURCE_LOCATION + " in " + imageTree.toStringTree() ) ;
   }
 
   private Treepath< SyntacticTree > addImageMetadata(
       Treepath< SyntacticTree > treepathToImage,
       String imageLocation
   ) {
       final File imageFile = new File( baseDirectory, imageLocation ) ;
       final NodeKind nodeKind = NodeKind.valueOf( treepathToImage.getTreeAtEnd().getText() ) ;
       try {
         switch( nodeKind ) {
           case RASTER_IMAGE :
             treepathToImage = addRasterImageMetadata( treepathToImage, imageFile ) ;
             break ;
           case VECTOR_IMAGE :
             treepathToImage = addVectorImageMetadata( treepathToImage, imageFile ) ;
             break ;
           default :
             break ;
         }
       } catch( Exception e ) {
         final String message = "Could not read '" + imageLocation + "'";
         problemCollector.collect( Problem.createProblem( message ) ) ;
         LOG.warn( message, e ) ;
       }
     return treepathToImage ;
   }
 
   private static Treepath< SyntacticTree > addRasterImageMetadata(
       Treepath< SyntacticTree > treepathToImage,
       File imageFile
   ) throws IOException {
     LOG.debug( "Extracting raster image metadata from '%s'...", imageFile.getAbsolutePath() ) ;
     final BufferedImage bufferedImage = ImageIO.read( imageFile ) ;
 
     treepathToImage = addImageMetadata(
         treepathToImage,
         NodeKind._IMAGE_WIDTH,
         bufferedImage.getWidth() + "px"
     ) ;
 
     treepathToImage = addImageMetadata(
         treepathToImage,
         NodeKind._IMAGE_HEIGHT,
         bufferedImage.getHeight() + "px"
     ) ;
     return treepathToImage;
   }
 
   /**
    * Extracts {@code /svg/@width} and {@code /svg/@height} from SVG file and puts them verbatim.
    * This implementation is highly unefficient. It parsers the whole SVG document and requires
    * bundled SVG-related DTD (meaning a <em>lot</em> of files). 
    * A <a href="http://www.extreme.indiana.edu/xgws/xsoap/xpp/" >pull</a> parser would do a 
    * much better job.
    * TODO use a pull parser.
    */
   private static Treepath< SyntacticTree > addVectorImageMetadata(
       Treepath< SyntacticTree > treepathToImage,
       File imageFile
   ) throws IOException, DocumentException {
     LOG.debug( "Extracting vector image metadata from '%s'...", imageFile.getAbsolutePath() ) ;
 
     final SAXReader reader = new SAXReader() ;
     reader.setEntityResolver( ENTITY_RESOLVER ) ;
     final Document document = reader.read( imageFile.toURI().toURL() ) ;
     final Node svgNode = document.selectSingleNode( "/svg" ) ;
 
     if( null != svgNode ) {
       final String width = svgNode.valueOf( "@width" ) ;
       final String height = svgNode.valueOf( "@height" ) ;
 
       LOG.debug( "Found: width:'%s', height:'%s'", width, height ) ;
 
       if( ! StringUtils.isBlank( width ) && ! StringUtils.isBlank( height ) ) {
         treepathToImage = addImageMetadata(
             treepathToImage,
             NodeKind._IMAGE_WIDTH,
             width
         ) ;  
         treepathToImage = addImageMetadata(
             treepathToImage,
             NodeKind._IMAGE_HEIGHT,
             height
         ) ;
       }
     }
     return treepathToImage;
   }
 
   private static Treepath<SyntacticTree> addImageMetadata(
       Treepath< SyntacticTree > treepathToImage,
       NodeKind sizeNodeKind,
       String value
   ) {
     final SyntacticTree withTree = new SimpleTree( sizeNodeKind.name(), new SimpleTree( value ) ) ;
     treepathToImage = TreepathTools.addChildLast( treepathToImage, withTree ).getPrevious() ;
     return treepathToImage;
   }
 
 
   /**
    * Returns an absolute file, given a directory and a relative name.
    * 
    * @param nameRelativeToReferrer a non-null, non-empty String which may start by a {@code ./}.
    *     File separator is a solidus.
    * @return a non-null object representing an existing resource file.
    * 
    * @throws IllegalArgumentException if one of the preconditions on arguments is violated.
    * @throws ImageFixerException if the resource does not exist, or if the resulting file
    *     is not located under given {@code directory}. 
    */
   protected String relocate( String nameRelativeToReferrer ) 
       throws ImageFixerException
   {  
     Preconditions.checkArgument( 
         ! StringUtils.isBlank( nameRelativeToReferrer ), 
         "Not expecting: '%s'", nameRelativeToReferrer 
     ) ;
     
     final File absoluteResourceFile ;
     {
       final File relocated ;
       if( nameRelativeToReferrer.startsWith( "/" ) ) {
         relocated = new File( baseDirectory, nameRelativeToReferrer ) ;
       } else {
         relocated = new File( referrerDirectory, nameRelativeToReferrer ) ;
       }
       try {
         absoluteResourceFile = relocated.getCanonicalFile() ;
       } catch ( IOException e ) {
         throw new RuntimeException( "Should not happen: " + e.getMessage(), e ) ;
       }
     }
 
     if( ! FileTools.isParentOf( baseDirectory, absoluteResourceFile ) ) {
       throw new ImageFixerException(
           "Given resource '" + nameRelativeToReferrer + "' " + 
           "resolved outside of '" + baseDirectory + "'"  
       ) ;
     }
     if( ! absoluteResourceFile.exists() ) {
       throw new ImageFixerException(
           "Does not exist: '" + absoluteResourceFile.getAbsolutePath() + "'" ) ;
     }
     
     final String resourceNameRelativeToBase = "/" +
        FileTools.urlifyPath( FileTools.relativizePath( baseDirectory, absoluteResourceFile ) ) ;
     
     return resourceNameRelativeToBase ;
   }
 
 
   protected final static class ImageDimension {
     private final String width ;
     private final String height ;
 
     public ImageDimension( String width, String height ) {
       this.width = width ;
       this.height = height ;
     }
 
     public String getWidth() {
       return width ;
     }
 
     public String getHeight() {
       return height ;
     }
   }
 
   /**
    * This global variable is dirty.
    * TODO propagate the {@code ResourceLoader} up to here.
    */
   private static final ResourceLoader ENTITY_RESOURCE_LOADER = 
       new ClasspathResourceLoader( ConfigurationTools.BUNDLED_STYLE_DIR ) ;
 
   private static final String SVG11_PUBLICID_PREFIX_1 = "-//W3C//ENTITIES SVG 1.1" ;
   private static final String SVG11_PUBLICID_PREFIX_2 = "-//W3C//DTD SVG 1.1" ;
   private static final String SVG11_PUBLICID_PREFIX_3 = "-//W3C//ELEMENTS SVG 1.1" ;
 
   /**
    * This is the path under default {@value ConfigurationTools#BUNDLED_STYLE_DIR}.
    */
   private static final String SVG_1_1_DTD_RESOURCE_PREFIX = "svg11-dtd";
 
   /**
    * Dirty implementation only supporting DTD for SVG 1.1 in bundled style directory.
    */
   private static final EntityResolver ENTITY_RESOLVER = new EntityResolver() {
     public InputSource resolveEntity( String publicId, String systemId ) 
         throws SAXException, IOException 
     {
       if( publicId.startsWith( SVG11_PUBLICID_PREFIX_1 ) 
        || publicId.startsWith( SVG11_PUBLICID_PREFIX_2 )  
        || publicId.startsWith( SVG11_PUBLICID_PREFIX_3 )  
       ) {
         final String dtdResourceName = systemId.substring( systemId.lastIndexOf( "/" ) + 1 ) ;
         LOG.debug(
             "Attempting to load definition for publicIdentifier='%s', systemIdentifier='%s"  +
             "', resourceName='%s'",
             publicId,
             systemId,
             dtdResourceName
         ) ;
         return new InputSource( 
             ENTITY_RESOURCE_LOADER.getInputStream( 
                 new ResourceName( SVG_1_1_DTD_RESOURCE_PREFIX + "/" + dtdResourceName ) ) ) ;
       } else {
         throw new IllegalArgumentException( 
             "Unsupported yet: public identifier='" + publicId + "', systemId='" + systemId + "'" ) ;
       }
     }
   } ;
   
 }
