 package ecologylab.generic;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * A simple name/value pair.
  */
public class Attribute
 {
    public String name;
    public String value;
    
    public Attribute()
    {
    }
    public Attribute(String name, String value)
    {
       this.name		= name;
       this.value	= value;
    }
 
    public String getName() {
 		// TODO Auto-generated method stub
 		return name;
 	}
 
    public boolean getSpecified() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
    public String getValue() {
 		// TODO Auto-generated method stub
 		return value;
 	}
 
    public void setValue(String value) throws DOMException {
 	
    		this.value = value;
 		
 	}
 
    public Element getOwnerElement() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
    public String getNodeName() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
    public String getNodeValue() throws DOMException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
    public void setNodeValue(String nodeValue) throws DOMException {
 		// TODO Auto-generated method stub
 		
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#getNodeType()
 	 */
 	public short getNodeType() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#getParentNode()
 	 */
 	public Node getParentNode() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#getChildNodes()
 	 */
 	public NodeList getChildNodes() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#getFirstChild()
 	 */
 	public Node getFirstChild() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#getLastChild()
 	 */
 	public Node getLastChild() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#getPreviousSibling()
 	 */
 	public Node getPreviousSibling() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#getNextSibling()
 	 */
 	public Node getNextSibling() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#getAttributes()
 	 */
 	public NamedNodeMap getAttributes() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#getOwnerDocument()
 	 */
 	public Document getOwnerDocument() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#insertBefore(org.w3c.dom.Node, org.w3c.dom.Node)
 	 */
 	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#replaceChild(org.w3c.dom.Node, org.w3c.dom.Node)
 	 */
 	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#removeChild(org.w3c.dom.Node)
 	 */
 	public Node removeChild(Node oldChild) throws DOMException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#appendChild(org.w3c.dom.Node)
 	 */
 	public Node appendChild(Node newChild) throws DOMException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#hasChildNodes()
 	 */
 	public boolean hasChildNodes() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#cloneNode(boolean)
 	 */
 	public Node cloneNode(boolean deep) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#normalize()
 	 */
 	public void normalize() {
 		// TODO Auto-generated method stub
 		
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#isSupported(java.lang.String, java.lang.String)
 	 */
 	public boolean isSupported(String feature, String version) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#getNamespaceURI()
 	 */
 	public String getNamespaceURI() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#getPrefix()
 	 */
 	public String getPrefix() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#setPrefix(java.lang.String)
 	 */
 	public void setPrefix(String prefix) throws DOMException {
 		// TODO Auto-generated method stub
 		
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#getLocalName()
 	 */
 	public String getLocalName() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.w3c.dom.Node#hasAttributes()
 	 */
 	public boolean hasAttributes() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
