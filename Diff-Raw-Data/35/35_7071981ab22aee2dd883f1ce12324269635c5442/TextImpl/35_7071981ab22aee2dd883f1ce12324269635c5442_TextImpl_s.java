 /*
  * TextImpl.java -
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
 import org.w3c.dom.Text;
 
 /**
  * @author marion@users.sourceforge.net
 * @version $Revision: 1.4 $
  */
 public class TextImpl extends NodeImpl implements Text {
 
     /**
      * @param parent
      * @param id
      * @param v
      */
     public TextImpl(NodeImpl parent, int id, int v) {
         super(parent, id, v);
     }
 
     /**
      * @see org.w3c.dom.Text#splitText(int)
      */
     public Text splitText(int offset) throws DOMException {
         throw notImplemented();
     }
 
     /**
      * @see org.w3c.dom.CharacterData#appendData(java.lang.String)
      */
     public void appendData(String arg) throws DOMException {
         throw notImplemented();
     }
 
     /**
      * @see org.w3c.dom.CharacterData#deleteData(int, int)
      */
     public void deleteData(int offset, int count) throws DOMException {
         throw notImplemented();
     }
 
     /**
      * @see org.w3c.dom.CharacterData#getData()
      */
     public String getData() throws DOMException {
         try {
             return getNodeFactory().getPlainTextFactory().getPlainText(getV());
         } catch (SQLException ex) {
             throw domException(ex);
         }
     }
 
     /**
      * @see org.w3c.dom.CharacterData#getLength()
      */
     public int getLength() {
         return getData().length();
     }
 
     /**
      * @see org.w3c.dom.CharacterData#insertData(int, java.lang.String)
      */
     public void insertData(int offset, String arg) throws DOMException {
         throw notImplemented();
     }
 
     /**
      * @see org.w3c.dom.CharacterData#replaceData(int, int, java.lang.String)
      */
     public void replaceData(int offset, int count, String arg)
         throws DOMException {
         throw notImplemented();
     }
 
     /**
      * @see org.w3c.dom.CharacterData#setData(java.lang.String)
      */
     public void setData(String data) throws DOMException {
         throw notImplemented();
     }
 
     /**
      * @see org.w3c.dom.CharacterData#substringData(int, int)
      */
     public String substringData(int offset, int count) throws DOMException {
         return getData().substring(offset, count);
     }
 
     /**
      * @see org.w3c.dom.Node#getNodeType()
      */
     public short getNodeType() {
         return TEXT_NODE;
     }
 
     /**
      * @see org.w3c.dom.Node#cloneNode(boolean)
      */
     public Node cloneNode(boolean deep) {
         return new TextImpl((NodeImpl)getParentNode(), getId(), getV());
     }
 
 }
