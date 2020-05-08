 package html2windows.dom;
 import java.awt.Component;
 
 import javax.swing.JFrame;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventException;
 import org.w3c.dom.events.EventListener;
 
 
 /**
  * Implementation of DOM Document and Top level window.
  *
  * Document is instance of Window so it can be used as top level window.
  *
  * The Document is not standard DOM Document. It removes some methods which
  * handle namespace and prefix.
  * 
  * One Document contains only one Node, whose type is Element.
  *
  * @author Southernbear
  */
  
 @SuppressWarnings("serial")
 
 public class Document extends JFrame implements Node{
 	
 	/**
 	 * Return the document element, the root
 	 *
 	 * @return thie document element, the root
 	 */
 	public Element documentElement(){
 		try{
 			return (Element)getContentPane().getComponent(0);
 		}
 		catch(Exception ex){
 			return null;
 		}
 	}
 	
 	/**
 	 * Creates an element of the type specified. 
 	 * Note that the instance returned implements the Element interface, so 
 	 * attributes can be specified directly on the returned object.
 	 *
 	 * @param tagName The name of the element type to instantiate.
 	 * @return A new Element object with the nodeName attribute set to tagName
 	 */
 	public Element createElement(String tagName) throws DOMException{
 		ElementInter element = new ElementInter(tagName);
 		element.setOwnerDocument(this);
 		return element;
 	}
 	
 	/**
 	 * Creates an empty DocumentFragment object.
 	 *
 	 * @return A new DocumentFragment.
 	 */
 	public DocumentFragment createDocumentFragment(){
 		DocumentFragmentInter documentFragement = new DocumentFragmentInter();
 		documentFragement.setOwnerDocument(this);
 		return documentFragement;
 	}
 	
 	/**	
 	 * Creates a Text node given the specified string.
 	 *
 	 * @param data The data for the node.
 	 * @return The new Text object.
 	 */
 	public Text createTextNode(String data){
 		TextInter text = new TextInter(data);
 		text.setOwnerDocument(this);
 		return text;
 	}
 	
 	/**
 	 * Creates an Attr of the given name. 
 	 * Note that the Attr instance can then be set on an Element using the 
 	 * setAttributeNode method.
 	 *
 	 * @param name The name of the attribute.
 	 * @return A new Attr object with the nodeName attribute set to name. 
 	 *         The value of the attribute is the empty string.
 	 */
 	public Attr createAttribute(String name) throws DOMException{
 		AttrInter attr = new AttrInter(name);
 		attr.setOwnerDocument(this);
 		return attr;
 	}
 	
 	/**
 	 * Returns a NodeList of all the Elements in document order with a given 
 	 * tag name and are contained in the document. 
 	 *
 	 * Now we do not support live NodeList but a static NodeList so you need to
 	 * call this function right before you use it to get the correct snapshot.
 	 *
 	 * @param tagname The name of the tag to match on.
 	 * @return A new NodeList object containing all the matched Elements.
 	 */
 	public NodeList getElementsByTagName(String tagname){
 		if(documentElement() != null)
 			return documentElement().getElementsByTagName(tagname);
 		else
 			return new NodeList();
 	}
 	
 	/**
 	 * Imports a node from another document to this document, without altering
 	 * or removing the source node from the original document; this method 
 	 * creates a new copy of the source node. The returned node has 
 	 * no parent; (parentNode is null).
 	 *
 	 * @param importedNode The node to import.
 	 * @param deep If true, recursively import the subtree under the 
 	 *             specified node; if false, import only the node itself. 
 	 *             This has no effect on nodes that cannot have any children, 
 	 *             and on Attr.
 	 * @return The imported node that belongs to this Document.
 	 */
 	public Node importNode(Node importedNode, boolean deep) throws DOMException{
 		NodeInter newNode = null;
 	
 		switch(importedNode.nodeType()){
 		case ATTRIBUTE_NODE :
 			newNode = importNode((Attr)importedNode, deep);
 			break;
 			
 		case DOCUMENT_FRAGMENT_NODE :
 			newNode = importNode((DocumentFragment)importedNode, deep);
 			break;
 			
 		case ELEMENT_NODE :
 			newNode = importNode((Element)importedNode, deep);
 			break;
 			
 		case TEXT_NODE :
 			newNode = importNode((Text)importedNode, deep);
 			break;
 			
 		default :
 			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "The type of node cannot be imported");
 		}
 		
 		newNode.setOwnerDocument(this);
 		
 		return newNode;
 	}
 	
 	/**
 	 * Returns the Element that has an ID attribute with the given value. 
 	 * If no such element exists, this returns null. If more than one element 
 	 * has an ID attribute with that value, return the first found. 
 	 *
 	 * @param elementId The unique id value for an element.
 	 * @return The matching element or null if there is none.
 	 */	
 	public Element getElementById(String elementId){
 		return getElementByIdInternal(documentElement(), elementId);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public String nodeName() {
 		return "#document";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public String nodeValue() {
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public short nodeType() {
 		return DOCUMENT_NODE;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public Node parentNode() {
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public NodeList childNodes() {
 		NodeList list = new NodeList();
 		Node child = documentElement();
 		if(child != null)
 			list.add(child);
 		return list;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public Node firstChild() {
 		return documentElement();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public Node lastChild() {
 		return documentElement();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public Node previousSibling() {
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public Node nextSibling() {
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public NamedNodeMap attributes() {
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public Document ownerDocument() {
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param newChild {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public Node appendChild(Node newChild) throws DOMException {
 		if(newChild != null){
 			setDocumentElement(newChild);
 		}
 		else{
 			throw new NullPointerException("newChild is null");
 		}
 		return newChild;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param newChild {@inheritDoc}
 	 * @param refChild {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
 		if(refChild == null){
 			appendChild(newChild);
 		}
 		else{
 			if(refChild != documentElement()){
 				throw new DOMException(DOMException.NOT_FOUND_ERR, "refChild is not found");
 			}
 			else{
 				if(newChild != null){
 					if(newChild != documentElement()){
 						throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Document has only one child node");
 					}
 					else{
 						setDocumentElement(newChild);
 					}
 				}
 				else{
 					throw new NullPointerException("newChild is null");
 				}
 			}
 		}
 		return newChild;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param newChild {@inheritDoc}
 	 * @param oldChild {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
 		if(oldChild != documentElement()){
 			throw new DOMException(DOMException.NOT_FOUND_ERR, "oldChild is not found");
 		}
 		if(newChild == null){
 			throw new NullPointerException("newChild is null");
 		}
 		
 		setDocumentElement(newChild);
 		return newChild;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param oldChild {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public Node removeChild(Node oldChild) throws DOMException {
 		if(oldChild != documentElement()){
 			throw new DOMException(DOMException.NOT_FOUND_ERR, "oldChild is not found");
 		}
 		setDocumentElement((Element)null);
 		
 		return oldChild;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public boolean hasChildNodes() {
 		return documentElement() != null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public boolean hasAttributes() {
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param type {@inheritDoc}
 	 * @param listener {@inheritDoc}
 	 * @param useCapture {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public void addEventListener(String type, EventListener listener,
 			boolean useCapture) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param type {@inheritDoc}
 	 * @param listener {@inheritDoc}
 	 * @param useCapture {@inheritDoc}
 	 */
 	@Override
 	public void removeEventListener(String type, EventListener listener,
 			boolean useCapture) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param evt {@inheritDoc}
 	 * @return {@inheritDoc}
 	 */
 	@Override
 	public boolean dispatchEvent(Event evt) throws EventException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param comp {@inheritDoc}
 	 * @param index {@inheritDoc}
 	 */
 	@Override
 	protected void addImpl(Component comp, Object constraints, int index){
 		if(documentElement() == null){
			if(comp instanceof Element){
 				super.addImpl(comp, constraints, index);
 			}
 		}
 	}
 
 	/**
 	 * Internal implementation of getElementById.
 	 * In order traverse the document tree recursively. 
 	 *
 	 * @param parent The root element of the subtree.
 	 * @param elementId The unique id value for an element.
 	 * @return The matching element or null if there is none.
 	 */
 	private Element getElementByIdInternal(Element parent, String elementId){
 		if(parent != null){
 			String id = parent.getAttribute("id");
 			if(id != null && id.equals(elementId)){
 				return parent;
 			}
 			else{
 				for(Node childNode : childNodes()){
 					if(childNode.nodeType() == ELEMENT_NODE){
 						Element match = getElementByIdInternal((Element)childNode, elementId);
 						if(match != null){
 							return (Element) childNode;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Set document element of the document
 	 *
 	 * It take different action according to type of node
 	 *
 	 * @param node The node to be set to document element.
 	 */
 	private void setDocumentElement(Node node) throws DOMException{
 		switch(node.nodeType()){
 			case ELEMENT_NODE : 
 				setDocumentElement((Element)node);
 				break;
 			case DOCUMENT_FRAGMENT_NODE : 
 				setDocumentElement((DocumentFragment)node);
 				break;
 			default :
 				throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Unacceptable node type");
 		}
 	}
 
 	/**
 	 * Set document element of the document
 	 *
 	 * @param element The element to be set to document element.
 	 */
 	private void setDocumentElement(Element element) throws DOMException{
 		if(element.ownerDocument() != this){
 			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "Need import the node first");
 		}
 		Element oldChild = documentElement();
 		if(oldChild != null)
			getContentPane().remove(oldChild);
 		if(element != null){
			getContentPane().add(element);
 		}
 	}
 
 	/**
 	 * Set document element of the document
 	 *
 	 * @param documentFragment A document fragment containing only one child.
 	 *                         The child will be set to document element.
 	 */
 	private void setDocumentElement(DocumentFragment documentFragement) throws DOMException{
 		NodeList children = documentFragement.childNodes();
 		if(children.length() > 1){
 			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Document has only one child node");
 		}
 		setDocumentElement(children.item(0));
 	}
 	
 	/**
 	 * Import Attribute node
 	 *
 	 * @param importedNode The attribute node to be imported.
 	 * @param deep Take no effect.
 	 */
 	private NodeInter importNode(Attr importedNode, boolean deep){
 		AttrInter attr = (AttrInter)createAttribute(importedNode.nodeName());
 		for(Node child : attr.childNodes()){
 			Node newChild = importNode(child, deep);
 			attr.appendChild(newChild);
 		}
 		return attr;
 	}
 	
 	/**
 	 * Import DocumentFragment Node
 	 *
 	 * @param importedNode The document fragment whose children will be 
 	 *                     imported.
 	 * @param deep If true, importe its children also.
 	 */
 	private NodeInter importNode(DocumentFragment importedNode, boolean deep){
 		DocumentFragmentInter df = (DocumentFragmentInter)createDocumentFragment();
 		if(deep){
 			for(Node child : importedNode.childNodes()){
 				Node newChild = importNode(child, deep);
 				df.appendChild(newChild);
 			}
 		}
 		return df;
 	}
 	
 	/**
 	 * Import Element node
 	 *
 	 * @param importedNode The element to be imported.
 	 * @param deep If true, its children will bw imported too.
 	 */
 	private NodeInter importNode(Element importedNode, boolean deep){
 		ElementInter element = (ElementInter)createElement(importedNode.nodeName());
 		
 		// Import attributes
 		NamedNodeMap attributes = element.attributes();
 		for(int i = 0; i < attributes.length(); i++){
 			Attr newAttr = (Attr)importNode(attributes.item(i), deep);
 			element.setAttributeNode(newAttr);
 		}
 		
 		// Import children
 		if(deep){
 			for(Node child : element.childNodes()){
 				Node newChild = importNode(child, deep);
 				element.appendChild(newChild);
 			}
 		}
 		
 		return element;
 	}
 	
 	/**
 	 * Import Node according to its node type.
 	 *
 	 * @param importedNode The node to be imported.
 	 * @param deep Depends on node type.
 	 */
 	private NodeInter importNode(Text importedNode, boolean deep){
 		TextInter text = (TextInter)createTextNode(importedNode.data());
 		return text;
 	}
 }
