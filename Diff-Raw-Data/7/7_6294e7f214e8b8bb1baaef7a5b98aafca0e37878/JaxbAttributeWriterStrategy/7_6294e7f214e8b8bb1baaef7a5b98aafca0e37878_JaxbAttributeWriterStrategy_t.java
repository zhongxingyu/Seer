 package org.commacq.client.csvtobean.xml;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import javax.annotation.concurrent.NotThreadSafe;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLEventFactory;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Attribute;
 import javax.xml.stream.events.EndElement;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 
 import org.apache.commons.csv.CSVParser;
 import org.apache.commons.lang3.StringUtils;
 import org.commacq.CsvLine;
 import org.commacq.client.CsvToBeanStrategy;
 
 /**
  * Unmarshals CSV to beans that have Jaxb annotations and are expecting a single XML element with attributes. 
  */
 @NotThreadSafe //Meant to be used by a single thread responsible for performing the initial load of bean cache data and performing the updates.
 public class JaxbAttributeWriterStrategy implements CsvToBeanStrategy {
     
     protected final Map<Class<?>, AttributeEventReader> attributeEventReaderMap = new HashMap<Class<?>, AttributeEventReader>();
     protected final Map<Class<?>, Unmarshaller> unmarshallerMap = new HashMap<Class<?>, Unmarshaller>();
     
     private <BeanType> AttributeEventReader getAttributeEventReader(Class<BeanType> beanType) {
     	AttributeEventReader cached = attributeEventReaderMap.get(beanType);
     	if(cached != null) {
     		return cached;
     	}
     	Unmarshaller unmarshaller;
         try {
             JAXBContext context = JAXBContext.newInstance(beanType);
             unmarshaller = context.createUnmarshaller();
         } catch (JAXBException ex) {
             throw new RuntimeException("Could not create JAXB Unmarshaller for bean: " + beanType, ex);
         }
         XmlRootElement annotation = beanType.getAnnotation(XmlRootElement.class);
         String name = annotation.name();
         if (name.equals("##default")) {
             String variableName = beanType.getName();
             variableName = variableName.substring(variableName.lastIndexOf(".") + 1);
             name = convertClassNameToTagName(variableName);
         }
         
         AttributeEventReader instance = new AttributeEventReader(new QName(name));
         
         attributeEventReaderMap.put(beanType, instance);
         unmarshallerMap.put(beanType, unmarshaller);
         return instance;
     }
 
     static String convertClassNameToTagName(String name) {
         // Make the first character lower case in all instances
         StringBuilder camelCaseStringBuilder = new StringBuilder(32);
 
         camelCaseStringBuilder.append(name.substring(0, 1).toLowerCase());
 
         // Loop for the length of the variableName - 1
         for (int i = 1; i < name.length() - 1; i++) {
             // If we have 2 upper case letters adjacent to each other
             // then we have to convert the first letter of the pair
             // to lower case, append it to the string builder and continue.
             if (StringUtils.isAllUpperCase(name.substring(i, i + 2))) {
                 camelCaseStringBuilder.append(name.substring(i, i + 1).toLowerCase());
                 continue;
             }
 
             // If we don't have 2 adjacent upper case letters then just copy the letter
             // and move on to the next letter.
             camelCaseStringBuilder.append(name.substring(i, i + 1));
         }
 
         // Copy the last letter from the variable name to the temp name.
         camelCaseStringBuilder.append(name.substring(name.length() - 1).toLowerCase());
 
         return camelCaseStringBuilder.toString();
     }
     
	private String[] splitCsv(String csv) {
		CSVParser parser = new CSVParser(new StringReader(csv));
 		try {
 			return parser.getLine();
 		} catch(IOException ex) {
			throw new RuntimeException("Error parsing CSV: " + csv, ex);
 		}
 	}
 
     @Override
     public <BeanType> BeanType getBean(Class<BeanType> beanType, String columnNamesCsv, CsvLine csvLine) {
 		Map<String, BeanType> beans = getBeans(beanType, columnNamesCsv, Collections.singleton(csvLine));
 		return beans.get(csvLine.getId());
     }
     
 	@SuppressWarnings("unchecked")
 	@Override
 	public <BeanType> Map<String, BeanType> getBeans(Class<BeanType> beanType, String columnNamesCsv, Collection<CsvLine> csvLines) {
 		String[] columnNames = splitCsv(columnNamesCsv);
 		
 		Map<String, BeanType> beansMap = new HashMap<>(csvLines.size());
 		for(CsvLine csvLine : csvLines) {
 			
 	        AttributeEventReader attributeEventReader = getAttributeEventReader(beanType);
 	        Unmarshaller unmarshaller = unmarshallerMap.get(beanType);
 	        
 	        String[] rowValues = splitCsv(csvLine.getCsvLine());
 
             attributeEventReader.setData(columnNames, rowValues);
 
             BeanType item;
             try {
                 item = (BeanType) unmarshaller.unmarshal(attributeEventReader);
             } catch (JAXBException ex) {
                 throw new RuntimeException("Error while unmarshalling: " + beanType + " - " + rowValues, ex);
             }
 			
 			beansMap.put(csvLine.getId(), item);
 		}
 		return beansMap;
 	}
 
     /**
      * Takes key/value pairs and pretends to create an XML tag like: <BeanClassname id="a" name="b" .../> The Bean must have Jaxb annotations. The XMLEventReader pumps events into the Jaxb unmarshaller and Jaxb produces an instance of the bean.
      * 
      * This class is designed to be reusable, with new data plugged in for each instance of the bean we want to create. This is just to avoid creating an extra object each time we want to create a bean.
      */
     static class AttributeEventReader implements XMLEventReader {
 
         private final QName tag;
         private final XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
 
         String[] header;
         String[] row;
         // Decides whether we send the tag event (event 0) or the end tag event (event 1)
         // We only ever deliver 2 events before this object is reset with the next
         // bean of data.
         int currentEventIndex;
         StartElement startElement;
 
         /**
          * A reusable working list which gets cleared down between creating each tag. Saves allocating a new array list for each bean.
          */
         private final List<Attribute> attributes = new ArrayList<>();
         private final EndElement endElement;
 
         public AttributeEventReader(QName tag) {
             this.tag = tag;
             this.endElement = xmlEventFactory.createEndElement(tag, Collections.emptyIterator());
         }
 
         public void setData(String[] header, String[] row) {
             this.header = header;
             this.row = row;
             this.currentEventIndex = 0;
 
             attributes.clear();
 
             for (int i = 0; i < header.length; i++) {
                 String value = row[i];
                 // TODO tokenise the CSV row before unescaping it so that we can tell
                 // the difference between a truly blank string (null) and a quoted
                 // blank string ("") which should evaluate to empty string.
                 if (value != null && !value.equals("")) {
                     // TODO Excel has a habit of saving down values of true and false
                     // as capitalised because it recognises them as special values.
                     // Opening up and saving a CSV file in excel can introduce
                     // a problem. Ideally we'd modify JAXB so that it could handle
                     // unmarshalling TRUE and FALSE elegantly: http://java.net/jira/browse/JAXB-913
                     if (value.equals("TRUE")) {
                         value = "true";
                     } else if (value.equals("FALSE")) {
                         value = "false";
                     }
 
                     attributes.add(xmlEventFactory.createAttribute(header[i], value));
                 }
             }
 
             startElement = xmlEventFactory.createStartElement(tag, attributes.iterator(), Collections.emptyIterator());
         }
 
         @Override
         public Object getProperty(String name) throws IllegalArgumentException {
             return null;
         }
 
         @Override
         public boolean hasNext() {
             return currentEventIndex < 2;
         }
 
         @Override
         public String getElementText() throws XMLStreamException {
             return "";
         }
 
         @Override
         public XMLEvent nextEvent() throws XMLStreamException {
             if (hasNext()) {
                 XMLEvent element = createElement();
                 currentEventIndex++;
                 return element;
             }
             throw new NoSuchElementException();
         }
 
         @Override
         public XMLEvent peek() throws XMLStreamException {
             if (hasNext()) {
                 return createElement();
             }
             return null;
         }
 
         private XMLEvent createElement() {
             if (currentEventIndex == 1) {
                 return endElement;
             }
 
             return startElement;
         }
 
         @Override
         public Object next() {
             try {
                 return nextEvent();
             } catch (XMLStreamException e) {
                 throw new RuntimeException(e);
             }
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException("Remove is not supported");
         }
 
         @Override
         public XMLEvent nextTag() throws XMLStreamException {
             return nextEvent();
         }
 
         @Override
         public void close() throws XMLStreamException {
 
         }
 
     }
 
 }
