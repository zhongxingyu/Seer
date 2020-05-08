 /*
  * Copyright (c) 2010 David Kellum
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
 
 package iudex.html;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import iudex.core.ContentSource;
 import iudex.http.ContentType;
 
 import org.cyberneko.html.parsers.SAXParser;
 import org.xml.sax.SAXException;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.gravitext.util.Charsets;
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
 
     public void setSkipBanned( boolean skipBanned )
     {
         _skipBanned = skipBanned;
     }
 
     public Element parse( ContentSource content )
         throws SAXException, IOException
     {
         Element root = null;
         try {
              root = parse( content, content.defaultEncoding() );
         }
         catch( WrongEncoding wenc ) {
             root = parse( content, wenc.newEncoding() );
         }
         return root;
     }
 
     private Element parse( ContentSource content, Charset encoding )
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
 
             parser.setProperty(
         "http://cyberneko.org/html/properties/default-encoding",
                                 encoding );
             parser.setProperty(
         "http://cyberneko.org/html/properties/names/elems",
                                 "lower" );
         }
         // SAXNotRecognizedException, SAXNotSupportedException
         catch( SAXException e ) {
             throw new RuntimeException( e );
         }
 
         HTMLHandler handler = new HTMLHandler( encoding );
 
         parser.setContentHandler( handler );
 
         InputSource input = null;
         InputStream inStream = content.stream();
         if( inStream != null ) {
              input = new InputSource( inStream );
         }
         else {
            //FIXME: Crappy copy: Reader?
            input = new InputSource( content.characters().toString() );
         }
 
         parser.parse( input );
 
         return handler.root();
     }
 
     static final class WrongEncoding
         extends RuntimeException
     {
         private WrongEncoding( Charset newEncoding )
         {
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
 
         public HTMLHandler( Charset encoding )
         {
             _inputEncoding = encoding;
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
 
             if( ( tag == HTML.META ) &&
                 ( _current.tag() == HTML.HEAD ) &&
                 "Content-Type".equalsIgnoreCase(
                     attributes.getValue( "http-equiv" ) ) ) {
 
                 String ctype = attributes.getValue( "content" );
                 if( ctype != null ) {
                     throwOnCharsetChange( ctype );
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
 
         private void throwOnCharsetChange( String type )
         {
             ContentType ctype = ContentType.parse( type );
             Charset newCharset = Charsets.lookup( ctype.charset() );
             if( ( newCharset != null ) &&
                 ! _inputEncoding.equals( newCharset ) ) {
                 throw new WrongEncoding( newCharset );
             }
         }
 
         @Override
         public void endElement( String iri, String localName, String qName )
         {
             if( _skipDepth > 0 ) {
                 --_skipDepth;
             }
             else {
                 bufferToChars();
                 _current = _current.parent();
             }
         }
 
         @Override
         public void characters( char[] ch, int start, int length )
         {
             if( _skipDepth <= 0 ) {
                 if( _buffer == null ) {
                     _buffer = new StringBuilder( length + 16 );
                 }
                 _buffer.append( ch, start, length );
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
                 _current.addChild( new Characters( _buffer ) );
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
                 final Attribute attr =
                     HTML.ATTRIBUTES.get( attributes.getLocalName( i ) );
                 if( attr != null ) {
                     atts.add(
                         new AttributeValue( attr, attributes.getValue( i ) ) );
                 }
             }
 
             element.setAttributes( atts );
         }
 
         private final Charset _inputEncoding;
         private final Element _root = new Element( HTML.DIV );
         private Element _current = _root;
         private StringBuilder _buffer = null;
 
         private int _skipDepth = 0;
     }
 
     private boolean _parseAsFragment = false;
     private boolean _skipBanned = true;
 }
