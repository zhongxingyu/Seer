 /*
  * AttrImpl.java -
  * Copyright (C) 2003 Klaus Rennecke, all rights reserved.
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use, copy,
  * modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package net.sourceforge.fraglets.zeig.dom;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * @author marion@users.sourceforge.net
 * @version $Revision: 1.4 $
  */
 public class AttrImpl extends NodeImpl implements Attr {
     
     public AttrImpl(NodeImpl owner, int nm, int v) {
         super(owner, nm, v);
        children = NamedNodeMapImpl.MT;
     }
     
     public int getNm() {
         return getId();
     }
     
     /**
      * @see org.w3c.dom.Attr#getName()
      */
     public String getName() {
         return getNodeName();
     }
 
     /**
      * @see org.w3c.dom.Attr#getOwnerElement()
      */
     public Element getOwnerElement() {
         return (Element)getParentNode();
     }
 
     /**
      * Always
      * @see org.w3c.dom.Attr#getSpecified()
      */
     public boolean getSpecified() {
         return true;
     }
 
     /**
      * @see org.w3c.dom.Attr#getValue()
      */
     public String getValue() {
         return getNodeValue();
     }
 
     /**
      * @see org.w3c.dom.Attr#setValue(java.lang.String)
      */
     public void setValue(String value) throws DOMException {
         throw notImplemented();
     }
 
     /**
      * @see org.w3c.dom.Node#getNodeType()
      */
     public short getNodeType() {
         return ATTRIBUTE_NODE;
     }
     
     /**
      * @see org.w3c.dom.Node#cloneNode(boolean)
      */
     public Node cloneNode(boolean deep) {
         return new AttrImpl((NodeImpl)getParentNode(), getId(), getV());
     }
 
 }
