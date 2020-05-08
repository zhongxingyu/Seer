 package cinnamon.interfaces;
 
 import org.dom4j.Element;
 
 /**
  * Applies to all classes which can add themselves to an XML Element.
  * @author ingo
  *
  */
 public interface XmlConvertable extends Comparable<XmlConvertable> {
 
    /**
     * Serialize the object to a dom4j tree and add it to an element.
     * Return the serialized object node (which is now a child of the root parameter element).
     * @param root the root element to which this object will be added.
     * @return the object node, now a child of the root parameter element.
     */
	Element toXmlElement(Element root);
     // getId() would be better, but runs into problems with dynamically generated GORM getId() method.
     Long myId();
 
 }
