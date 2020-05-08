 package org.bails;
 
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Attribute;
 import javax.xml.stream.events.EndElement;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import static javax.xml.stream.XMLStreamConstants.*;
 
 /**
  * @author Karl Bennett
  */
 public class BailsStreamSTAX implements IBailsStream {
 
     private static final String SYSTEM_NEW_LINE = System.getProperty("line.separator");
     private static final String UNIX_NEW_LINE = "\n";
 
     private static final String SYSTEM_NEW_LINE_REGEXP = "(\\s*" + SYSTEM_NEW_LINE + "+\\s*)+";
     private static final String UNIX_NEW_LINE_REGEXP = "(\\s*" + UNIX_NEW_LINE + "+\\s*)+";
 
     private XMLInputFactory factory;
     private XMLEventReader parser;
 
     private XMLEvent currentEvent;
 
     private String preWhiteSpace = "";
     private String postWhiteSpace = "";
 
     private StringBuilder currentEventString = new StringBuilder(0);
 
     private Map<String, Object> attributes;
 
     public BailsStreamSTAX(InputStream stream) {
         this.factory = XMLInputFactory.newInstance();
 
         try {
             this.parser = this.factory.createXMLEventReader(stream);
             next();
         } catch (XMLStreamException e) {
             System.out.println("SORT THIS OUT!: " + e.getMessage());
         }
     }
 
     /*
        Convenience methods.
     */
 
     /**
      * Check to see if the next even is just a new line character. If so add it to the end of the last events string.
      */
     private void checkForNewLine() {
 
         preWhiteSpace = "";
         postWhiteSpace = "";
 
         if (hasNext()) {
             try {
                 XMLEvent event = parser.peek();
                 String eventString = event.toString();
 
                 if (eventString.matches(UNIX_NEW_LINE_REGEXP) || eventString.matches(SYSTEM_NEW_LINE_REGEXP)) {
                     preWhiteSpace = eventString.substring(eventString.indexOf('\n') + 1);
                     postWhiteSpace = eventString.substring(0, eventString.indexOf('\n') + 1);
                     parser.nextEvent();
                 }
             } catch (XMLStreamException e) {
                 System.out.println("SORT THIS OUT!: " + e.getMessage());
             }
         }
     }
 
     /*
        Override methods.
     */
 
     @Override
     public boolean hasNext() {
         return currentEvent.getEventType() != END_DOCUMENT;
     }
 
     @Override
     public void next() {
         try {
             currentEventString.setLength(0);
             currentEvent = this.parser.nextEvent();
 
             while (currentEvent.getEventType() != START_ELEMENT
                     && currentEvent.getEventType() != CHARACTERS
                     && currentEvent.getEventType() != END_ELEMENT) {
                 currentEvent = this.parser.nextEvent();
             }
 
             currentEventString.append(preWhiteSpace);
             currentEventString.append(currentEvent);
 
             checkForNewLine();
 
             currentEventString.append(postWhiteSpace);
 
             if (currentEvent.getEventType() == START_ELEMENT) {
                 attributes = new HashMap<String, Object>();
 
                 Iterator<Attribute> attributeIterator = ((StartElement) currentEvent).getAttributes();
                 Attribute attribute = null;
 
                 while (attributeIterator.hasNext()) {
                     attribute = attributeIterator.next();
 
                    attributes.put(attribute.getName().toString(), attribute.getValue());
                 }
 
             } else {
                 attributes = null;
             }
         } catch (XMLStreamException e) {
             System.out.println("SORT THIS OUT!: " + e.getMessage());
         }
     }
 
     @Override
     public void close() {
         try {
             parser.close();
         } catch (XMLStreamException e) {
             System.out.println("SORT THIS OUT!: " + e.getMessage());
         }
     }
 
     @Override
     public boolean isOpenTag() {
         return currentEvent.getEventType() == START_ELEMENT;
     }
 
     @Override
     public boolean isCloseTag() {
         return currentEvent.getEventType() == END_ELEMENT;
     }
 
     @Override
     public boolean isOpenCloseTag() {
         return false;
     }
 
     @Override
     public boolean isCharacters() {
         return currentEvent.getEventType() == CHARACTERS;
     }
 
     @Override
     public CharSequence getCharSequence() {
         return currentEventString.toString();
     }
 
     @Override
     public String getName() {
         String name = "";
 
         switch (currentEvent.getEventType()) {
             case START_ELEMENT:
                 name = ((StartElement) currentEvent).getName().toString();
                 break;
             case END_ELEMENT:
                 name = ((EndElement) currentEvent).getName().toString();
                 break;
         }
 
         return name;
     }
 
     @Override
     public Map<String, Object> getAttributes() {
         return attributes;
     }
 }
