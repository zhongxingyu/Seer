 package chameleon.core.tag;
 
 import chameleon.core.element.Element;
 
 /**
  * An interface for metadata tags that can be attached to elements.
  * 
  * @author Marko van Dooren
  */
 public interface Tag {
 
 	/**
 	 * Return the element to which this tag is attached.
 	 * @return
 	 */
 	public Element getElement();
 	
 	public void setElement(Element element, String name);
 
 }
