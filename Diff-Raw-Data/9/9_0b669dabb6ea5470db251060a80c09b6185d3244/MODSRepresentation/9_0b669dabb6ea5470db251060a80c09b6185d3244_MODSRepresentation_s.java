 package au.com.miskinhill.rdf;
 
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.XMLEvent;
 import javax.xml.stream.util.XMLEventConsumer;
 
 import com.hp.hpl.jena.rdf.model.Resource;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Component;
 
 import au.id.djc.rdftemplate.TemplateInterpolator;
 import au.id.djc.rdftemplate.XMLStream;
 
 import au.com.miskinhill.rdf.vocabulary.MHS;
 
 @Component
 public class MODSRepresentation implements XMLStreamRepresentation {
     
     private final MediaType contentType = new MediaType("application", "mods+xml");
     private final EnumSet<ShownIn> shownIn = EnumSet.of(ShownIn.HTMLAnchors, ShownIn.HTMLLinks, ShownIn.AtomLinks, ShownIn.Unapi, ShownIn.OAIPMH);
     private final URI namespace = URI.create("http://www.loc.gov/mods/v3");
    private final URI xsd = URI.create("http://www.loc.gov/standards/mods/v3/mods-3-3.xsd");
     private final Map<Resource, String> typeTemplates = new HashMap<Resource, String>();
     private final TemplateInterpolator templateInterpolator;
     private final XMLOutputFactory outputFactory;
     
     @Autowired
     public MODSRepresentation(TemplateInterpolator templateInterpolator, XMLOutputFactory outputFactory) {
         this.templateInterpolator = templateInterpolator;
         this.outputFactory = outputFactory;
         
         typeTemplates.put(MHS.Journal, "template/mods/Journal.xml");
         typeTemplates.put(MHS.Article, "template/mods/Article.xml");
     }
     
     @Override
     public boolean canRepresent(Resource resource) {
         return resource.getURI().startsWith("http://miskinhill.com.au/journals/") &&
                 RDFUtil.hasAnyType(resource, typeTemplates.keySet());
     }
 
     @Override
     public MediaType getContentType() {
         return contentType;
     }
     
     @Override
     public String getFormat() {
         return "mods";
     }
     
     @Override
     public int getOrder() {
         return 3;
     }
     
     @Override
     public String getLabel() {
         return "MODS";
     }
     
     @Override
     public String getDocs() {
         return "http://www.loc.gov/standards/mods/mods-userguide.html";
     }
     
     @Override
     public boolean isShownIn(ShownIn place) {
         return shownIn.contains(place);
     }
     
     @Override
     public URI getXMLNamespace() {
         return namespace;
     }
     
     @Override
     public URI getXSD() {
         return xsd;
     }
 
     @Override
     public String render(Resource resource) {
         StringWriter writer = new StringWriter();
         final XMLEventWriter eventWriter;
         try {
             eventWriter = outputFactory.createXMLEventWriter(writer);
         } catch (XMLStreamException e) {
             throw new RuntimeException(e);
         }
         XMLEventConsumer destination = new XMLEventConsumer() {
             @Override
             public void add(XMLEvent event) throws XMLStreamException {
                 eventWriter.add(event);
             }
         };
         render(resource, destination);
         return writer.toString();
     }
     
     @Override
     public XMLStream renderXMLStream(Resource resource) {
         final ArrayList<XMLEvent> events = new ArrayList<XMLEvent>();
         render(resource, new XMLEventConsumer() {
             @Override
             public void add(XMLEvent event) throws XMLStreamException {
                 events.add(event);
             }
         });
         return new XMLStream(events);
     }
     
     private void render(Resource resource, XMLEventConsumer destination) {
         for (Resource type: RDFUtil.getTypes(resource)) {
             String templatePath = typeTemplates.get(type);
             if (templatePath != null) {
                 templateInterpolator.interpolate(
                         new InputStreamReader(this.getClass().getResourceAsStream(templatePath)),
                         resource, destination);
                 return;
             }
         }
         throw new IllegalArgumentException("No template found for " + resource);
     }
 
 }
