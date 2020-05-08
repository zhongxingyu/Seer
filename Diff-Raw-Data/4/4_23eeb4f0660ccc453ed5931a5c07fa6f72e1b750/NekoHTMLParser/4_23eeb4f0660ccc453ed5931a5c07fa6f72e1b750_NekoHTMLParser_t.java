 /*
  * Copyright (c) 2008-2013 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License.  You may
  * obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.  See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package iudex.html.neko;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import iudex.core.ContentSource;
 import iudex.html.HTML;
 import iudex.html.HTMLTag;
 import iudex.http.ContentType;
 import iudex.util.Charsets;
 
 import org.cyberneko.html.parsers.SAXParser;
 import org.xml.sax.SAXException;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.gravitext.util.CharSequences;
 import com.gravitext.util.ResizableCharBuffer;
 import com.gravitext.xml.producer.Attribute;
 import com.gravitext.xml.tree.AttributeValue;
 import com.gravitext.xml.tree.Characters;
 import com.gravitext.xml.tree.Element;
 import com.gravitext.xml.tree.Node;
 
 public class NekoHTMLParser
 {
     /**
      * Set if input should be parsed as a fragment (default: false).
      * When set true, the returned Element will always be a synthetic
      * HTML.DIV container (possibly containing only a single child element).
      */
     public final void setParseAsFragment( boolean parseAsFragment )
     {
         _parseAsFragment = parseAsFragment;
     }
 
     public final void setSkipBanned( boolean skipBanned )
     {
         _skipBanned = skipBanned;
     }
 
     public final Element parse( ContentSource content )
         throws SAXException, IOException
     {
         content = expand( content );
         WrongEncoding last = null;
         for( int attempt = 0; attempt < 2; ++attempt ) {
             try {
                 return parseInner( content );
             }
             catch( WrongEncoding wenc ) {
                 last = wenc;
             }
         }
         throw new SAXException( last );
     }
 
     private ContentSource expand( ContentSource content )
     {
         Charset curEnc = content.defaultEncoding();
         if( curEnc != null ) {
             Charset newEnc = Charsets.expand( curEnc );
             if( ! newEnc.equals( curEnc ) ) {
                 content.setDefaultEncoding( newEnc,
                                             content.encodingConfidence() +
                                             0.01F );
             }
         }
         return content;
     }
 
     /**
      * @param encoding assumed encoding for comparison, or null to
      * disable checks.
      */
     final Element parseInner( ContentSource content )
         throws SAXException, IOException
     {
         SAXParser parser = new SAXParser();
 
         // http://nekohtml.sourceforge.net/settings.html
         try {
             parser.setFeature(
         "http://cyberneko.org/html/features/scanner/ignore-specified-charset",
                                true );
             parser.setFeature(
         "http://cyberneko.org/html/features/scanner/normalize-attrs",
                                true );
 
             if( _parseAsFragment ) {
                 parser.setFeature(
         "http://cyberneko.org/html/features/balance-tags/document-fragment",
                                    true );
             }
             Charset encoding = content.defaultEncoding();
 
             if( encoding != null ) {
                 parser.setProperty(
         "http://cyberneko.org/html/properties/default-encoding",
                                     encoding );
             }
 
             parser.setProperty(
         "http://cyberneko.org/html/properties/names/elems",
                                 "lower" );
 
         // FIXME: Must be set true for CDATA sections to passed as characters
         // Feature: "http://cyberneko.org/html/features/scanner/cdata-sections"
         }
         // SAXNotRecognizedException, SAXNotSupportedException
         catch( SAXException e ) {
             throw new RuntimeException( e );
         }
 
         HTMLHandler handler = new HTMLHandler( content );
 
         parser.setContentHandler( handler );
 
         InputSource input = null;
         InputStream inStream = content.stream();
         if( inStream != null ) {
              input = new InputSource( inStream );
         }
         else {
             input = new InputSource(
                         CharSequences.reader( content.characters() ) );
         }
 
         parser.parse( input );
 
         return handler.root();
     }
 
     static final class WrongEncoding
         extends RuntimeException
     {
         private WrongEncoding( Charset newEncoding )
         {
             super( newEncoding.name() );
             _newEncoding = newEncoding;
         }
         public Charset newEncoding()
         {
             return _newEncoding;
         }
         private final Charset _newEncoding;
     }
 
     final class HTMLHandler
         extends DefaultHandler
     {
         /**
          * Given assumed encoding or null to disable check for meta-tag
          * encoding.
          */
         public HTMLHandler( ContentSource content )
         {
             _content = content;
         }
 
         /**
          * The root Element available after SAX parsing events have been
          * received.
          */
         public Element root()
         {
             List<Node> children = _root.children();
             if( ! _parseAsFragment &&
                 ( children.size() == 1 ) &&
                 children.get( 0 ).isElement() ) {
                 return children.get( 0 ).asElement();
             }
             return _root;
         }
 
         @Override
         public void startElement( String iri, String localName, String qName,
                                   Attributes attributes )
         {
             bufferToChars();
 
             HTMLTag tag = HTML.TAGS.get( localName );
 
             if( ( tag == HTML.META ) && ( _current.tag() == HTML.HEAD ) ) {
                 if( "Content-Type".equalsIgnoreCase(
                         attributes.getValue( "http-equiv" ) ) ) {
 
                     String ctype = attributes.getValue( "content" );
                     if( ctype != null ) {
                         checkContentType( ctype );
                     }
                 }
                 else {
                     // Check for HTML5 style <meta charset="">
                     String charset = attributes.getValue( "charset" );
                     if( charset != null ) {
                         _metaCharset = charset.trim();
                     }
                 }
             }
 
             if( _skipDepth > 0 ) {
                 ++_skipDepth;
             }
             else if( ( tag == null ) ||
                      ( _skipBanned && tag.isBanned() ) ) {
                 _skipDepth = 1;
             }
             else {
                 Element element = new Element( tag );
                 copyAttributes( attributes, element );
 
                 _current.addChild( element );
                 _current = element;
             }
         }
 
         private void checkContentType( String type )
         {
             ContentType ctype = ContentType.parse( type );
             _metaCharset = ctype.charset();
         }
 
         private void throwOnCharsetChange() throws WrongEncoding
         {
             Charset newEnc = null;
             if( _metaCharset != null ) newEnc = Charsets.lookup( _metaCharset );
             if( newEnc != null ) {
                 newEnc = Charsets.expand( newEnc );
 
                 if( ( _content.defaultEncoding() != null ) &&
                     ! _content.defaultEncoding().equals( newEnc ) &&
                     ( _content.setDefaultEncoding( newEnc, 0.20F ) ) ) {
                     throw new WrongEncoding( newEnc );
                 }
             }
         }
 
         @Override
         public void endElement( String iri, String localName, String qName )
         {
             if( _skipDepth > 0 ) {
                 --_skipDepth;
             }
             else {
                if( _current.tag() == HTML.HEAD ) throwOnCharsetChange();
                 bufferToChars();
                 _current = _current.parent();
             }
         }
 
         @Override
         public void characters( char[] ch, int start, int length )
         {
             if( _skipDepth <= 0 ) {
                 if( _buffer == null ) {
                     _buffer = new ResizableCharBuffer( length + 16 );
                 }
                 _buffer.put( ch, start, length );
             }
         }
 
         @Override
         public void endDocument() throws SAXException
         {
             // Add any additional character child at end of fragment
             bufferToChars();
         }
 
         private void bufferToChars()
         {
             if( _buffer != null ) {
                 _current.addChild(
                      new Characters( _buffer.flipAsCharBuffer() ) );
                 _buffer = null;
             }
         }
 
         private void copyAttributes( Attributes attributes, Element element )
         {
             final int end = attributes.getLength();
             if( end == 0 ) return;
 
             final ArrayList<AttributeValue> atts
                 = new ArrayList<AttributeValue>( end );
 
             for( int i = 0; i < end; ++i ) {
                 // Neko can yield empty localName attribute with exotic
                 // malformed input, even with (default) namespace processing
                 // enabled. (#8)
                 String lname = attributes.getLocalName( i );
                 if( lname.isEmpty() ) lname = null;
 
                 Attribute attr = HTML.ATTRIBUTES.get( lname );
 
                 // If unknown attribute, but not skipping banned, then add it
                 // anyway.
                 if( attr == null && lname != null && !_skipBanned ) {
                     attr = new Attribute( lname );
                 }
 
                 if( attr != null ) {
                     AttributeValue av =
                         new AttributeValue( attr, attributes.getValue( i ) );
 
                     // Neko will let through duplicate attributes. Last value
                     // wins.
                     int j = 0;
                     while( j < atts.size() ) {
                         if( attr.equals( atts.get( j ).attribute() ) ) {
                             atts.set( j, av );
                             break;
                         }
                         ++j;
                     }
                     if( j == atts.size() ) atts.add( av );
                 }
 
             }
 
             element.setAttributes( atts );
         }
 
         private final ContentSource _content;
         private final Element _root = new Element( HTML.DIV );
         private Element _current = _root;
         private ResizableCharBuffer _buffer = null;
         private int _skipDepth = 0;
         private String _metaCharset = null;
     }
 
     private boolean _parseAsFragment = false;
     private boolean _skipBanned = true;
 }
