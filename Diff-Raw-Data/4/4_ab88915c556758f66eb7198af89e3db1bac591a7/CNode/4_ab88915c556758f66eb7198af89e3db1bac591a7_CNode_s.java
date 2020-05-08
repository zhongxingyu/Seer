 /**
 Copyright (c) 2012 Delcyon, Inc.
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.delcyon.capo.xml.cdom;
 
 import java.lang.reflect.Modifier;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.w3c.dom.UserDataHandler;
 
 import com.delcyon.capo.util.CloneControl;
 import com.delcyon.capo.util.ControlledClone;
 import com.delcyon.capo.util.EqualityProcessor;
 import com.delcyon.capo.util.ToStringControl;
 import com.delcyon.capo.util.CloneControl.Clone;
 import com.delcyon.capo.util.ToStringControl.Control;
 import com.delcyon.capo.xml.cdom.CDOMEvent.EventType;
 
 /**
  * @author jeremiah
  *
  */
 @CloneControl(filter=CloneControl.Clone.exclude,modifiers=Modifier.STATIC+Modifier.FINAL)
 @ToStringControl(control=Control.exclude,modifiers=Modifier.STATIC+Modifier.FINAL)
 public abstract class CNode implements Node, ControlledClone
 {
 
     @CloneControl(filter=Clone.exclude)
     @ToStringControl(control=Control.exclude)
     protected CNode parentNode;
     
     @CloneControl(filter=Clone.exclude)
     @ToStringControl(control=Control.exclude)
     protected CDocument ownerDocument = null;
     
     @CloneControl(filter=Clone.exclude)
     protected Vector<CDOMEventListener> cdomEventListenerVector = new Vector<CDOMEventListener>();
     
     protected CNodeList nodeList = new CNodeList();
     protected CNamedNodeMap attributeList = new CNamedNodeMap();
     protected String nodeName = null;
     protected String nodeValue = null;
     protected String namespaceURI = null;
     
     @CloneControl(filter=Clone.exclude)
     private transient CDOMEvent preparedEvent = null;
     
     
     
     public void setParent(CNode parentNode)
     {
         this.parentNode = parentNode;
         
     }
     
     public boolean hasEventListeners()
     {
     	if(cdomEventListenerVector.size() > 0)
     	{
     		return true;
     	}
     	if(parentNode != null)
     	{
     		return parentNode.hasEventListeners();
     	}
     	return false;
     }
     
     /**
      * We only want one event to happen per change, so this method should act as a singleton for event creation.
      * @param eventType
      * @param sourceNode
      * @return
      */
     protected CDOMEvent prepareEvent(EventType eventType, CNode sourceNode)
     {
     	if(ownerDocument != null && ownerDocument.isSilenceEvents() == true)
     	{
     		return null;
     	}
     	if(hasEventListeners() == false)
     	{
     		return null;
     	}
     	
     	if (preparedEvent == null)
     	{
     		preparedEvent = new CDOMEvent(eventType, sourceNode);
     		return preparedEvent;
     	}
     	else
     	{
     		return null;
     	}
     }
     
    
     public void cascadeDOMEvent(CDOMEvent cdomEvent)
     {
     	if(cdomEvent == null)
     	{
     		return;
     	}
     	//remove prepared event
     	preparedEvent = null;
     	//walk local list of event listeners
     	for (CDOMEventListener cdomEventListener : getCDOMEventListeners())
 		{
 			cdomEventListener.processEvent(cdomEvent);
 			if(cdomEvent.isHandled())
 			{
 				break;
 			}
 		}
     	if(cdomEvent.isHandled() == false && parentNode instanceof CDOMEventListener && this instanceof CDocument == false)
     	{
     		if(parentNode.hasEventListeners())
     		{
     			parentNode.cascadeDOMEvent(cdomEvent);
     		}
     	}
     	
     }
     
     public Vector<CDOMEventListener> getCDOMEventListeners()
 	{
 		return cdomEventListenerVector;
 	}
 
 	public void addCDOMEventListener(CDOMEventListener eventListener)
     {
     	cdomEventListenerVector.add(eventListener);
     }
     
     public void removeCDOMEventListener(CDOMEventListener eventListener)
     {
     	cdomEventListenerVector.remove(eventListener);
     }
     
     
     public void setNodeName(String nodeName)
     {
         this.nodeName = nodeName;
         cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
     }
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getNodeName()
      */
     @Override
     public String getNodeName()
     {
         return this.nodeName;
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getNodeValue()
      */
     @Override
     public String getNodeValue() throws DOMException
     {   
         try
         {
         if(this.nodeValue != null && ownerDocument != null && ownerDocument.getVariableProcessor() != null)
         {
             return ownerDocument.getVariableProcessor().processVars(nodeValue);
         }
         return this.nodeValue;
         }
         catch (Exception exception)
         {
             throw new DOMException(DOMException.VALIDATION_ERR, exception.getMessage());
         }
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#setNodeValue(java.lang.String)
      */
     @Override
     public void setNodeValue(String nodeValue) throws DOMException
     {
        this.nodeValue = nodeValue;
        cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
     }
 
     
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getParentNode()
      */
     @Override
     public Node getParentNode()
     {
        return this.parentNode;
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getChildNodes()
      */
     @Override
     public NodeList getChildNodes()
     {
         return nodeList;
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getFirstChild()
      */
     @Override
     public Node getFirstChild()
     {
         if(nodeList.size() > 0)
         {
             return nodeList.item(0);
         }
         else
         {
             return null;
         }
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getLastChild()
      */
     @Override
     public Node getLastChild()
     {
         if(nodeList.size() > 0)
         {
             return nodeList.item(nodeList.size()-1);
         }
         else
         {
             return null;
         }
     }
 
     private int getPosition()
     {
         NodeList siblingList = parentNode.getChildNodes();
         int myPosition = 0;
         for(int index = 0; index < siblingList.getLength(); index++)
         {
             Node siblingNode = siblingList.item(index);
             if (siblingNode.isSameNode(this))
             {
                 myPosition = index;
                 break;
             }
         }
         return myPosition;
     }
     
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getPreviousSibling()
      */
     @Override
     public Node getPreviousSibling()
     {
         NodeList siblingList = parentNode.getChildNodes();
         int myPosition = getPosition();
         if (myPosition > 0)
         {
             return siblingList.item(myPosition-1);
         }
         else
         {
             return null;
         }
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getNextSibling()
      */
     @Override
     public Node getNextSibling()
     {
         NodeList siblingList = parentNode.getChildNodes();
         int myPosition = getPosition();
         if (myPosition+1 < siblingList.getLength())
         {
             return siblingList.item(myPosition+1);
         }
         else
         {
             return null;
         }
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getAttributes()
      */
     @Override
     public NamedNodeMap getAttributes()
     {
         return attributeList;
     }
 
     
     public void removeAttributes()
     {
         while(attributeList.isEmpty() == false)
         {
             attributeList.remove(0);
         }
     }
     
     public void setAttributes(CNamedNodeMap attributes)
     {
         this.attributeList = attributes;
         cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
     }
     
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getOwnerDocument()
      */
     @Override
     public Document getOwnerDocument()
     {
         return this.ownerDocument ; 
     }
 
     public void setOwnerDocument(Document ownerDocument)
     {
         this.ownerDocument = (CDocument) ownerDocument;
         cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
     }
     
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#insertBefore(org.w3c.dom.Node, org.w3c.dom.Node)
      */
    
     @Override
     public Node insertBefore(Node newChild, Node refChild) throws DOMException
     {
     	CDOMEvent cdomEvent = prepareEvent(EventType.INSERT, this);
         CNodeList children = (CNodeList) getChildNodes();
         if (refChild == null)
         {
             children.add(newChild);
             ((CNode) newChild).setParent(this);
             cascadeDOMEvent(cdomEvent);
             return newChild;
         }
         
         int index = 0;
         for(; index < children.getLength(); index++)
         {
             if (children.item(index).equals(refChild))
             {
                 break;
             }
         }
         children.add(index, newChild);
         ((CNode) newChild).setParent(this);
         cascadeDOMEvent(cdomEvent);
         return newChild;
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#replaceChild(org.w3c.dom.Node, org.w3c.dom.Node)
      */
     @Override
     public Node replaceChild(Node newChild, Node oldChild) throws DOMException
     {
     	CDOMEvent cdomEvent = prepareEvent(EventType.UPDATE, this);
         CNodeList children = (CNodeList) getChildNodes();
         int index = 0;
         for (Node node : children)
         {
             if(node.equals(oldChild))
             {
                 
                 if(newChild.getParentNode() != null)
                 {
                     try
                     {
                         newChild.getParentNode().removeChild(newChild);
                     }
                     catch (DOMException domException){}//ignore if we don't find it, this is just for safty
                 }
                 
                 children.set(index, newChild);
                 ((CNode) newChild).setParent(this);
                 
                 //remove old child's parent, as it's now lightly detached
                 ((CNode) oldChild).setParent(null);
                 cascadeDOMEvent(cdomEvent);
                 return oldChild;
             }
             index++;
         }
         preparedEvent = null;
         return null;
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#removeChild(org.w3c.dom.Node)
      */
     @Override
     public Node removeChild(Node oldChild) throws DOMException
     {        
        if(nodeList.remove(oldChild) == true)
        {
     	   if(oldChild instanceof CNode)
     	   {
     		   ((CNode) oldChild).setParent(null);
     	   }
     	   cascadeDOMEvent(prepareEvent(EventType.DELETE, this));
            return oldChild;    
        }
        else
        {
            throw new DOMException(DOMException.NOT_FOUND_ERR,"couldn't find "+oldChild.getLocalName() +" in "+ getLocalName());
        }
        
        
     }
 
     public void removeChildrenAll()
     {
     	for (Node node : nodeList)
 		{
     		((CNode) node).setParent(null);
 		}
         nodeList.clear();
         cascadeDOMEvent(prepareEvent(EventType.DELETE, this));
     }
     
     public void removeNodeTypeChildrenAll(short nodeType)
     {
         for(int index = 0; index < nodeList.getLength(); index++)
         {
             if(nodeList.item(index).getNodeType() == nodeType)
             {
                 nodeList.remove(index);
                 index--;
             }
         }
         
         cascadeDOMEvent(prepareEvent(EventType.DELETE, this));
     }
     
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#appendChild(org.w3c.dom.Node)
      */
     @Override
     public Node appendChild(Node newChild) throws DOMException
     {
     	CDOMEvent cdomEvent = prepareEvent(EventType.INSERT, this);
         if(newChild instanceof CNode)
         {
             if(newChild.getParentNode() != null)
             {   
             	if(newChild.getParentNode().equals(this))
             	{
             		return newChild;
             	}
                 newChild.getParentNode().removeChild(newChild);               
             }
             nodeList.add(newChild);
             ((CNode) newChild).setParent(this);
             ((CNode) newChild).setOwnerDocument(getOwnerDocument());
             cascadeDOMEvent(cdomEvent);
             return newChild;
         }
         else
         {
         	preparedEvent = null;
             Thread.dumpStack();
             throw new UnsupportedOperationException();
         }
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#hasChildNodes()
      */
     @Override
     public boolean hasChildNodes()
     {
         return nodeList.size() > 0;
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#cloneNode(boolean)
      */
     @Override
     public Node cloneNode(boolean deep)
     {
         if(deep == false)
         {
             Thread.dumpStack();
             throw new UnsupportedOperationException();
         }
         
         CNode clonedNode = null;
         try
         {
             clonedNode = EqualityProcessor.clone(this);
             final CDOMEvent cdomEvent = new CDOMEvent(EventType.INSERT,this);
             NodeProcessor nodeProcessor = new NodeProcessor()
             {
                 //Set all of the correct parent nodes
                 @Override
                 public void process(Node parentNode,Node node) throws Exception
                 {
                     CNode cNode = (CNode) node;
                     cNode.preparedEvent = cdomEvent;
                     cNode.setParent((CNode) parentNode);
                     cNode.preparedEvent = null;
                 }
             };
             walkTree(null,clonedNode, nodeProcessor, false);
         }
         catch (Exception exception)
         {
             Logger.global.log(Level.SEVERE, "Couldn't clone "+this, exception);
         }
         return clonedNode;
     }
 
     @Override
     public void preClone(Object parentObject, Object clonedObject) throws Exception
     {
         
         
     }
     
     
     public void postClone(Object parentObject, Object clonedObject)
     {
         CNode clonednode = (CNode) clonedObject;
         //we treat these differently, because we don't want them to recurse
         clonednode.ownerDocument = ownerDocument;        
     }
     
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#normalize()
      */
     @Override
     public void normalize()
     {
     	
         Text textNode = null;
         for(int index = 0; index < nodeList.size(); index++)
         {
             Node node = nodeList.get(index);
             if(node.getNodeType() == Node.TEXT_NODE)
             {
                 if(textNode == null)
                 {
                     textNode = (Text) node;
                 }
                 else
                 {
                     textNode.appendData(node.getTextContent());
                     nodeList.remove(index);
                     index--;
                 }
             }
             else
             {
                 textNode = null;
                 node.normalize();
             }
         }
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#isSupported(java.lang.String, java.lang.String)
      */
     @Override
     public boolean isSupported(String feature, String version)
     {
         Thread.dumpStack();
         throw new UnsupportedOperationException();
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getNamespaceURI()
      */
     @Override
     public String getNamespaceURI()
     {
         return this.namespaceURI;
     }
 
     public void setNamespaceURI(String namespaceURI)
     {
         this.namespaceURI = namespaceURI;
     }
     
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getPrefix()
      */
     @Override
     public String getPrefix()
     {
         if(getNodeName().contains(":"))
         {
             return getNodeName().split(":")[0];
         }
         else
         {
             return null;
         }
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#setPrefix(java.lang.String)
      */
     @Override
     public void setPrefix(String prefix) throws DOMException
     {
     	if(getNamespaceURI() == null)
     	{
     		throw new DOMException(DOMException.NAMESPACE_ERR, "Can't set prefix on an element w/o a namespaceURI");
     	}
     	
         if (getNodeName().contains(":"))
         {
         	nodeName = prefix+":"+nodeName.split(":")[1];
         }
        else
        {
     	   nodeName = prefix+":"+nodeName;
        }
         cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getLocalName()
      */
     @Override
     public String getLocalName()
     {
         if(getNodeName() == null)
         {
             System.out.println("WTF");
         }
         if(getNodeName().contains(":"))
         {
             try
             {
                 //System.out.println(getNodeName().split(":")[1]);
                 return getNodeName().split(":")[1];
             } catch (Exception exception)
             {
                 exception.printStackTrace();
             }
             return getNodeName();
         }
         else
         {
             return getNodeName();
         }
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#hasAttributes()
      */
     @Override
     public boolean hasAttributes()
     {
         return attributeList.size() > 0;
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getBaseURI()
      */
     @Override
     public String getBaseURI()
     {
         // TODO Auto-generated method stub
         Thread.dumpStack();
         throw new UnsupportedOperationException();
     }
     
     private int[] getPositionIndexArray(Node node)
     {
         int[] positionIndexArray = null;
         Node parentNode = node;       
         int depth = 0;
         while(true)
         {
             if(parentNode == null || parentNode.getNodeType() == Node.DOCUMENT_NODE)
             {
                 break;
             }
             
             depth++;
             parentNode = parentNode.getParentNode();
         }
         
         //make the new array
         positionIndexArray = new int[depth];
         
         parentNode = node;        
        
         while(true)
         {
             if(parentNode == null || parentNode.getNodeType() == Node.DOCUMENT_NODE)
             {
                 break;
             }
             int precedingSiblingCount = 0;
             Node precedingSiblingNode = parentNode;
             while(precedingSiblingNode != null)
             {
                 precedingSiblingCount++;
                 precedingSiblingNode = precedingSiblingNode.getPreviousSibling();
             }
             depth--;
             positionIndexArray[depth] = precedingSiblingCount;           
             parentNode = parentNode.getParentNode();
         }
         
         return positionIndexArray;
     }
     
     
     /*
      * (non-Javadoc)
      * @see org.w3c.dom.Node#compareDocumentPosition(org.w3c.dom.Node)
      * This could be made more efficient, but it's designed to work with any DOMImplementation instead.
      */
     @Override
     public short compareDocumentPosition(Node other) throws DOMException
     {        
         int[] localPositionIndexArray = getPositionIndexArray(this);
         //System.out.println(Arrays.toString(localPositionIndexArray));
         int[] otherPositionIndexArray = getPositionIndexArray(other);
         //System.out.println(Arrays.toString(otherPositionIndexArray));
         
         int matchIndex = 0;
         
         for(; matchIndex < localPositionIndexArray.length && matchIndex < otherPositionIndexArray.length; matchIndex++)
         {
             //mark all matching with a neg one
             if(localPositionIndexArray[matchIndex] != otherPositionIndexArray[matchIndex])
             {
                 matchIndex--;
                 break;
             }
         }
         
         
         
         if(localPositionIndexArray.length == otherPositionIndexArray.length)
         {
             int endPosition = localPositionIndexArray.length-1;
             if(endPosition == matchIndex)
             {
                 return 0;
             }
             else if (endPosition-1 == matchIndex)
             {
                 if(localPositionIndexArray[endPosition] < otherPositionIndexArray[endPosition])
                 {
                     return Node.DOCUMENT_POSITION_FOLLOWING;
                 }
                 else
                 {
                     return Node.DOCUMENT_POSITION_PRECEDING;
                 }
             }
             else
             {
                 return Node.DOCUMENT_POSITION_DISCONNECTED;
             }
             
         }
         else //one might contain the other or they are detached
         {
             
             //test for contains
             if(localPositionIndexArray.length < otherPositionIndexArray.length)
             {
                 if(matchIndex < localPositionIndexArray.length-2)
                 {
                     return Node.DOCUMENT_POSITION_CONTAINS;
                 }
                 else
                 {
                     return Node.DOCUMENT_POSITION_DISCONNECTED;
                 }
             }
             //test for contained
             else //(localPositionIndexArray.length > otherPositionIndexArray.length)
             {
                 if(matchIndex < otherPositionIndexArray.length-2)
                 {
                     return Node.DOCUMENT_POSITION_CONTAINS;
                 }
                 else
                 {
                     return Node.DOCUMENT_POSITION_DISCONNECTED;
                 }
             }
             
         }
         
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getTextContent()
      */
     @Override
     public String getTextContent() throws DOMException
     {
         StringBuilder stringBuilder = new StringBuilder();
         
         for (Node node : nodeList)
         {
             if(node instanceof Text)
             {
                 stringBuilder.append(((Text) node).getData());
             }
             else
             {
                 stringBuilder.append(node.getTextContent());
             }
         }
         return stringBuilder.toString();
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#setTextContent(java.lang.String)
      */
     @Override
     public void setTextContent(String textContent) throws DOMException
     {
     	 
         CText text = new CText();
         text.setData(textContent);
         removeChildrenAll();
         appendChild(text);
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#isSameNode(org.w3c.dom.Node)
      */
     @Override
     public boolean isSameNode(Node other)
     {
         return this.equals(other);
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#lookupPrefix(java.lang.String)
      */
     @Override
     public String lookupPrefix(String namespaceURI)
     {
         // TODO Auto-generated method stub
         Thread.dumpStack();
         throw new UnsupportedOperationException();
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#isDefaultNamespace(java.lang.String)
      */
     @Override
     public boolean isDefaultNamespace(String namespaceURI)
     {
         return namespaceURI.equals(ownerDocument.getDefaultNamespace());
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#lookupNamespaceURI(java.lang.String)
      */
     @Override
     public String lookupNamespaceURI(String prefix)
     {
         if(prefix == null)
         {
             return ownerDocument.getDefaultNamespace();
         }
         else
         {
             if(prefix.equals(getPrefix()) && getNamespaceURI() != null)
             {
                 return getNamespaceURI();
             }
             else if(parentNode != null)
             {
                 return parentNode.lookupNamespaceURI(prefix);
             }
             else
             {
                 return null;
             }
         }
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#isEqualNode(org.w3c.dom.Node)
      */
     @Override
     public boolean isEqualNode(Node arg)
     {
         // TODO Auto-generated method stub
         Thread.dumpStack();
         throw new UnsupportedOperationException();
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getFeature(java.lang.String, java.lang.String)
      */
     @Override
     public Object getFeature(String feature, String version)
     {
         // TODO Auto-generated method stub
         Thread.dumpStack();
         throw new UnsupportedOperationException();
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#setUserData(java.lang.String, java.lang.Object, org.w3c.dom.UserDataHandler)
      */
     @Override
     public Object setUserData(String key, Object data, UserDataHandler handler)
     {
         // TODO Auto-generated method stub
         Thread.dumpStack();
         throw new UnsupportedOperationException();
     }
 
     /* (non-Javadoc)
      * @see org.w3c.dom.Node#getUserData(java.lang.String)
      */
     @Override
     public Object getUserData(String key)
     {
         // TODO Auto-generated method stub
         Thread.dumpStack();
         throw new UnsupportedOperationException();
     }
 
     @Override
     public String toString()
     {
         return getNodeName()+" @"+attributeList.toString();
     }
     
     /**
      * This will run a nodeProcessor against the entire subtree of a node. 
      * @param node start node
      * @param processor processor to run
      * @param startWithChildren just recurse over the children of this element, but don't process the start node.
      * @throws Exception
      */
     public static void walkTree(Node parentNode, Node node, NodeProcessor processor,boolean startWithChildren) throws Exception
     {
         if(startWithChildren == false)
         {
             processor.process(parentNode, node);
             NamedNodeMap attributes = node.getAttributes();
             for(int index = 0; index < attributes.getLength(); index++)
             {
                 processor.process(node,attributes.item(index));
             }
         }
         
         NodeList children = node.getChildNodes();
         for(int index = 0; index < children.getLength(); index++)
         {            
             walkTree(node,children.item(index), processor,false);
         }
     }
 
 
     public void detach()
     {
        if(getParentNode() != null)
        {
            ((CNodeList) getParentNode().getChildNodes()).remove(this);
        }
        ownerDocument = null;
        parentNode = null;
        cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
     }
 
 }
