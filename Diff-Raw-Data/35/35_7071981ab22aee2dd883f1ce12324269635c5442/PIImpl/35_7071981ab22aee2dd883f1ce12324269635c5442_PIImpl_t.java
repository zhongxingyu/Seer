 /*
  * PIImpl.java -
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
 
 import java.sql.SQLException;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Node;
 import org.w3c.dom.ProcessingInstruction;
 
 /**
  * @author marion@users.sourceforge.net
 * @version $Revision: 1.6 $
  */
 public class PIImpl extends NodeImpl implements ProcessingInstruction {
 
     /**
      * @param parent
      * @param id
      * @param v
      */
     public PIImpl(NodeImpl parent, int id, int v) {
         super(parent, id, v);
         children = NamedNodeMapImpl.MT;
     }
 
     /**
      * @see org.w3c.dom.ProcessingInstruction#getData()
      */
     public String getData() {
         try {
             return getNodeFactory().getPlainTextFactory().getPlainText(getV());
         } catch (SQLException ex) {
             throw domException(ex);
         }
     }
 
     /**
      * @see org.w3c.dom.ProcessingInstruction#getTarget()
      */
     public String getTarget() {
         try {
             return getNodeFactory().getPlainTextFactory().getPlainText(getId());
         } catch (SQLException ex) {
             throw domException(ex);
         }
     }
 
     /**
      * @see org.w3c.dom.ProcessingInstruction#setData(java.lang.String)
      */
     public void setData(String data) throws DOMException {
         throw notImplemented();
     }
 
     /**
      * @see org.w3c.dom.Node#getNodeType()
      */
     public short getNodeType() {
         return PROCESSING_INSTRUCTION_NODE;
     }
 
     /**
      * @see org.w3c.dom.Node#getNodeName()
      */
     public String getNodeName() {
         return getTarget();
     }
 
     /**
      * @see org.w3c.dom.Node#getNodeValue()
      */
     public String getNodeValue() throws DOMException {
         return getData();
     }
 
     /**
      * @see org.w3c.dom.Node#cloneNode(boolean)
      */
     public Node cloneNode(boolean deep) {
         return new PIImpl((NodeImpl)getParentNode(), getId(), getV());
     }
 
     /**
      * @see org.w3c.dom.Node#hasChildNodes()
      */
     public boolean hasChildNodes() {
         return false;
     }
 
    /**
     * @see org.w3c.dom.Node#getLocalName()
     */
    public String getLocalName() {
        return getTarget();
    }

    /**
     * @see org.w3c.dom.Node#getNamespaceURI()
     */
    public String getNamespaceURI() {
        return "";
    }

 }
