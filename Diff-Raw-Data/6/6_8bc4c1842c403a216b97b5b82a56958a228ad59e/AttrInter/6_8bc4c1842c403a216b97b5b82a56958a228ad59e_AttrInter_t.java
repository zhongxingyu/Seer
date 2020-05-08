 package html2windows.dom;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventException;
 import org.w3c.dom.events.EventListener;
 
 
 class AttrInter implements Attr, NodeInter{
     public String name(){
         return null;
     }
     
     public boolean specified(){
         return false;
     }
     
     public String value(){
         return null;
     }
  
 	public AttrInter(String name){
 	}
 
     public void setValue(String value){
         
     }
     
     public Element ownerElement(){
         return null;
     }
    
    public void setOwnerElement(Element newOwnerElement){
    	
    }
    
     @Override
     public String nodeName() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public String nodeValue() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public short nodeType() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public Node parentNode() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public NodeList childNodes() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node firstChild() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node lastChild() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node previousSibling() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node nextSibling() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public NamedNodeMap attributes() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Document ownerDocument() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node insertBefore(Node newChild, Node refChild) throws DOMException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node replaceChild(Node newChilde, Node oldChild) throws DOMException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node removeChild(Node oldChild) throws DOMException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node appendChild(Node newChild) throws DOMException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public boolean hasChildNodes() {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean hasAttributes() {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public void addEventListener(String type, EventListener listener,
             boolean useCapture) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void removeEventListener(String type, EventListener listener,
             boolean useCapture) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public boolean dispatchEvent(Event evt) throws EventException {
         // TODO Auto-generated method stub
         return false;
     }
 
 	@Override
 	public void setParentNode(Node newParent) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setOwnerDocument(Document newOwnerDocument) {
 		// TODO Auto-generated method stub
 		
 	}
 }
