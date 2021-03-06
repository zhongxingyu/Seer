 package org.deegree.services.wms.controller.plugins;
 
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.OutputStream;
 import java.net.URL;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamWriter;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.deegree.feature.FeatureCollection;
 import org.deegree.feature.types.ApplicationSchema;
 import org.deegree.gml.GMLOutputFactory;
 import org.deegree.gml.GMLStreamWriter;
 import org.deegree.gml.GMLVersion;
 import org.slf4j.Logger;
 
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;

 public class XSLTFeatureInfoSerializer implements FeatureInfoSerializer {
 
     private static final Logger LOG = getLogger( FeatureInfoSerializer.class );
 
     private final GMLVersion gmlVersion;
 
     private final URL xslt;
 
     public XSLTFeatureInfoSerializer( GMLVersion version, URL xslt ) {
         this.gmlVersion = version;
         this.xslt = xslt;
     }
 
     @Override
     public void serialize( ApplicationSchema schema, FeatureCollection col, OutputStream outputStream ) {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         try {
             XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter( bos );
             if ( LOG.isDebugEnabled() ) {
                 out = new IndentingXMLStreamWriter( out );
             }
             GMLStreamWriter writer = GMLOutputFactory.createGMLStreamWriter( gmlVersion, out );
             writer.setNamespaceBindings( schema.getNamespaceBindings() );
             writer.write( col );
             writer.close();
             bos.flush();
             bos.close();
             if ( LOG.isDebugEnabled() ) {
                 LOG.debug( "GML before XSLT:\n{}", new String( bos.toByteArray(), "UTF-8" ) );
             }
             Source source = new StreamSource( new ByteArrayInputStream( bos.toByteArray() ) );
             Source xslt = new StreamSource( new File( this.xslt.toURI() ) );
            Transformer t = TransformerFactory.newInstance().newTransformer( xslt );
             Result result = new StreamResult( outputStream );
             t.transform( source, result );
         } catch ( Throwable e ) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
 }
