 package org.xbrlapi.impl;
 
 import org.w3c.dom.NodeList;
 import org.xbrlapi.OpenContextComponent;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 class OpenContextComponentImpl extends ContextComponentImpl implements OpenContextComponent {
 	
     /**
      * Gets the complex content
      *
      * @return the complex content contained by the open
      * context component as a node list of complex content
      * XML nodes
      * @throws XBRLException
      * @see org.xbrlapi.OpenContextComponent#getComplexContent()
      */
     public NodeList getComplexContent() throws XBRLException {
     	return getDataRootElement().getChildNodes();
     }
 
 
     
 	/**
 	 * Test c-equality of this open context component and another.
 	 * @param other The other open context component.
 	 * @return true if the two components are c-equal and false otherwise.
 	 * @see org.xbrlapi.OpenContextComponent#equals(OpenContextComponent)
 	 */
     public boolean equals(OpenContextComponent other) throws XBRLException {
     	
     	if (! this.getType().equals(other.getType())) return false;
     	
    	NodeList thisContent = this.getComplexContent();
     	NodeList otherContent = other.getComplexContent();
    	if (thisContent.getLength() != otherContent.getLength()) return false;
    	for (int i=0; i<thisContent.getLength(); i++) {
    		if (! thisContent.item(i).isEqualNode(otherContent.item(i))) return false;
     	}
     	return true;
     }
 }
