 package au.com.miskinhill.web.decorator;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.logging.Logger;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.sax.SAXSource;
 import javax.xml.transform.stream.StreamResult;
 
import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Component;
import org.springframework.util.xml.SimpleTransformErrorListener;
 import org.xml.sax.InputSource;
 
 import au.com.miskinhill.web.util.HttpResponseBufferingFilter;
 
 @Component("decoratingFilter")
 public class DecoratingFilter extends HttpResponseBufferingFilter {
     
     private static final Logger LOG = Logger.getLogger(DecoratingFilter.class.getName());
     private static final long serialVersionUID = -379349824773494219L;
     
     private final TransformerFactory transformerFactory = TransformerFactory.newInstance(
             // ensure we get the builtin JDK6 one, since xalan is broken apparently
             "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
             this.getClass().getClassLoader());
     private final ThreadLocal<Transformer> transformerHolder = new ThreadLocal<Transformer>() {
         @Override
         protected Transformer initialValue() {
             LOG.fine("Constructing transformer");
             InputStream xsltStream = this.getClass().getResourceAsStream("commonwrapper.xml");
             try {
                 return transformerFactory.newTransformer(
                         new SAXSource(new InputSource(xsltStream)));
             } catch (TransformerConfigurationException e) {
                 throw new RuntimeException(e);
             } finally {
                 try {
                     xsltStream.close();
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }
         };
     };
     
    public DecoratingFilter() {
        transformerFactory.setErrorListener(new SimpleTransformErrorListener(LogFactory.getLog(DecoratingFilter.class)));
    }
    
     @Override
     protected String postprocessResponse(String responseBody) {
         try {
             Transformer transformer = transformerHolder.get();
             StringWriter writer = new StringWriter();
             transformer.transform(
                     new SAXSource(new InputSource(new StringReader(responseBody))),
                     new StreamResult(writer));
             return writer.toString();
         } catch (TransformerException e) {
             throw new RuntimeException(e);
         }
     }
     
 }
