 package chameleon.core.tag;
 
 import chameleon.core.element.Element;
 
 /**
  * An interface for metadata tags that can be attached to elements.
  * 
 * There is a bidirectional association between this interface an Element. A change in this
 * association must always be initiated by the metadata. The remove and set methods in Element should not
 * be used. The reason for this is that metadata may have to manage additional associations that depend on the 
 * element to which it is associated. That data however, may not always be computable by the metadata. For example, 
 * to attach the model to the Eclipse plugin, the EclipseEditorTag must keep a reference to the Eclipse document where it
 * marks the position of the element. The document cannot always be computed because it is not available while the element
 * is still being parsed, or when the Eclipse project is being initialized. Therefore, this information must be provided explicitly
 * to the  
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
