 package net.contextfw.web.application.component;
 
 import java.io.StringWriter;
 
 import net.contextfw.application.TstAttributeHandler;
 import net.contextfw.web.application.WebApplicationException;
 import net.contextfw.web.application.dom.AttributeHandler;
 import net.contextfw.web.application.dom.DOMBuilder;
 import net.contextfw.web.application.internal.component.ComponentBuilder;
 import net.contextfw.web.application.internal.component.ComponentBuilderImpl;
 import net.contextfw.web.application.internal.component.ComponentRegister;
 import net.contextfw.web.application.internal.component.WebApplicationComponent;
 
 import org.dom4j.Node;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 import org.junit.Before;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
public abstract class BaseComponentTest {
     
     protected Logger log = LoggerFactory.getLogger(BaseComponentTest.class); 
     
     protected ComponentRegister componentRegister;
     
     protected ComponentBuilder componentBuilder;
     
     protected WebApplicationComponent webApplicationComponent;
     
     protected DOMBuilder domBuilder;
     
     protected AttributeHandler attributes = new TstAttributeHandler();
     
     protected DomAssert assertDom(String xpath) {
         Node node = domBuilder.toDocument().getRootElement().selectSingleNode(xpath);
         return new DomAssert(xpath, node);
     }
     
     @Before
     public void before() {
         componentRegister = new ComponentRegister();
         componentBuilder = new ComponentBuilderImpl();
         domBuilder = new DOMBuilder("WebApplication", attributes, componentBuilder);
         webApplicationComponent = new WebApplicationComponent(componentRegister);
     }
     
     public void logXML(DOMBuilder b) {
         try {
             OutputFormat format = OutputFormat.createPrettyPrint();
             XMLWriter writer;
 
             StringWriter xml = new StringWriter();
             writer = new XMLWriter(xml, format);
             writer.write(b.toDocument());
             log.info("Logged xml:\n"+xml.toString());
             
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 }
