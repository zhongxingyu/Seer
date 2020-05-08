 package com.bradmcevoy.http.values;
 
 import com.bradmcevoy.http.XmlWriter;
 import com.bradmcevoy.http.XmlWriter.Element;
 import java.util.Map;
 
 /**
  * Supports HrefList objects, and writes them out as a list of <href>...</href> elements
  *
  * Currently readonly, but should support writing
  *
  * @author brad
  */
 public class HrefListValueWriter implements ValueWriter {
     public boolean supports( String nsUri, String localName, Class c ) {
         return HrefList.class.isAssignableFrom( c );
     }
 
     public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
       writer.open(prefix, localName);
       HrefList list = (HrefList) val;
       if( list != null ) {
           for( String s : list) {
               Element hrefEl = writer.begin( "D:href" ).open();
               hrefEl.writeText( s );
               hrefEl.close();
           }
       }
       writer.close(prefix, localName);
     }
 
     public Object parse( String namespaceURI, String localPart, String value ) {
         throw new UnsupportedOperationException( "Not supported yet." );
     }
 }
