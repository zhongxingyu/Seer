 package html2windows.dom;
 import java.util.List;
 
 import org.w3c.dom.DOMException;
 
 public interface Node extends EventTarget{
     // NodeType
     final  short      ELEMENT_NODE                   = 1;
     final  short      ATTRIBUTE_NODE                 = 2;
     final  short      TEXT_NODE                      = 3;
     final  short      CDATA_SECTION_NODE             = 4;
     final  short      ENTITY_REFERENCE_NODE          = 5;
     final  short      ENTITY_NODE                    = 6;
     final  short      PROCESSING_INSTRUCTION_NODE    = 7;
     final  short      COMMENT_NODE                   = 8;
     final  short      DOCUMENT_NODE                  = 9;
     final  short      DOCUMENT_TYPE_NODE             = 10;
     final  short      DOCUMENT_FRAGMENT_NODE         = 11;
     final  short      NOTATION_NODE                  = 12;
     
     public String nodeName();
     public String nodeValue();
     
     public short nodeType();
     public Node parentNode();
     public NodeList childNodes();
     public Node firstChild();
     public Node lastChild();
     public Node previousSibling();
     public Node nextSibling();
     public NamedNodeMap attributes();
     public Document ownerDocument();
     
     public Node insertBefore(Node newChild, Node refChild) throws DOMException;
     public Node replaceChild(Node newChilde, Node oldChild) throws DOMException;
     public Node removeChild(Node oldChild) throws DOMException;
     public Node appendChild(Node newChild) throws DOMException;

     public boolean hasChildNodes();
     //public Node cloneNode(boolean deep);
     //public void normalize();
     //public boolean isSupported(String feature, String version);
     
     //public String namespaceURI();
     //public String prefix() throws DOMException;
     //public String localName();
     public boolean hasAttributes();

    private Node setParent(Node parentNode);
 }
