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
 
 package novelang.rendering;
 
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 
 import org.xml.sax.ContentHandler;
 import novelang.configuration.RenderingConfiguration;
 import novelang.common.Nodepath;
 import novelang.common.metadata.TreeMetadata;
 import novelang.parser.Escape;
 import novelang.loader.ResourceName;
 
 /**
  * @author Laurent Caillette
  */
 public class HtmlWriter extends XslWriter {
 
   protected static final ResourceName DEFAULT_HTML_STYLESHEET =  new ResourceName( "html.xsl" ) ;
 
   public HtmlWriter( RenderingConfiguration configuration, ResourceName stylesheet ) {
     super(
         configuration,
         null == stylesheet ? DEFAULT_HTML_STYLESHEET : stylesheet,
         RenditionMimeType.HTML,
         ESCAPE_ISO_ENTITIES
     ) ;
   }
 
   public void writeLiteral( Nodepath kinship, String word ) throws Exception {
       super.write( kinship, Escape.escapeHtmlText( word ) ); ;
   }
 
 
   protected ContentHandler createSinkContentHandler(
       OutputStream outputStream,
       TreeMetadata treeMetadata,
       Charset encoding
   ) throws Exception {
     return new HtmlSink( outputStream ) ; 
   }
 
   private static final EntityEscapeSelector ESCAPE_ISO_ENTITIES = new EntityEscapeSelector() {
     public boolean shouldEscape( String publicId, String systemId ) {
       return publicId.startsWith( "ISO 8879:1986//ENTITIES" ) ;
     }
   };
 }
