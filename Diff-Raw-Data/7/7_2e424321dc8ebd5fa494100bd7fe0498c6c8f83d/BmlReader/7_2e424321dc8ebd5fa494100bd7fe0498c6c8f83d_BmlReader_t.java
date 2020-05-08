 //#condition api.bml
 /*
  * Copyright (C) 2010 France Telecom
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package memoplayer;
 
 //#ifdef api.bmlDebug
 class BmlPrinter implements XmlVisitor {
     String decal = "";
 
     public void setLeave (String l, boolean startingSpace, boolean endingSpace) {
         Logger.println (decal+l);
     }
 
     public void open (String t) {
         Logger.print (decal+"<"+t);
         decal += "    ";
     }
 
     public void endOfAttributes (boolean selfClosing) {
         if (selfClosing) {
             decal = decal.substring (4);
             Logger.println (" />");
         } else {
             Logger.println (" >");
         }
     }
 
     public void close (String t) {
         decal = decal.substring (4);
         Logger.println (decal+"</"+t+">");
     }
 
     public void addAttribute (String name, String value) {
         Logger.print (" "+name+" = \""+value+"\"");
     }
 
 }
 //#endif
 
 
 class BmlReader {
     
     XmlNode m_root;
 
     int m_nbTags;
     String [] m_tags;
     //StringBuffer m_sb; 
     byte [] m_buffer;
     int m_pos;
 
     // init the reader with a buffer to read and start build the DOM
     BmlReader (byte [] buffer) {
         m_nbTags = 0;
         m_pos = 4;
         m_buffer = buffer;
         //m_sb = new StringBuffer ();
         if (buffer[0] != 'B' &&
             buffer[1] != 'M' &&
             buffer[2] != 'L' &&
             buffer[3] != '1') {
             Logger.println ("BmlReader bad magic: "+buffer[0]+""+buffer[1]+""+buffer[2]+""+buffer[3]);
         }
         parseTable ();
         m_root = parseNode ();
         if (m_root != null) {
 //#ifdef api.bmlDebug
             m_root.visit (new BmlPrinter ());
 //#endif            
         }
     }
 
     XmlNode getRootNode () {
         return m_root;
     }
     
     int decodeSize () {
        int n = m_buffer[m_pos++] & 0xFF;
         if (n == 255) {
            n *= (int) (m_buffer[m_pos++] & 0xFF);
            n += (int) (m_buffer[m_pos++] & 0xFF);
         }
         return n;
     }
     String decodeString () {
         int start = m_pos;
         int end = m_pos;
         char c = (char) m_buffer[m_pos++];
         while (c != 0) {
             end = m_pos;
             c = (char) m_buffer[m_pos++];
         }
         try {
             return new String (m_buffer, start, end-start, "UTF-8");
         } catch (Exception e) {
             return new String (m_buffer, start, end-start);
         }
 //         m_sb.setLength (0);
 //         char c = (char) m_buffer[m_pos++];
 //         while (c != 0) {
 //             m_sb.append (c);
 //             c = (char) m_buffer[m_pos++];
 //         }
 //         return m_sb.toString ();
     }
 
     void parseTable () {
         m_nbTags = decodeSize ();
         m_tags = new String [m_nbTags];
         for (int i = 0; i < m_nbTags; i++) {
             m_tags[i] = decodeString ();
         }
     }
 
     XmlNode parseNode () {
         int type = m_buffer[m_pos++];
         if (type == 3) {
             return new XmlNode (decodeString (), XmlNode.CDATA);
         } else if (type > 0) {
             XmlNode n = new XmlNode (m_tags[decodeSize ()], type == 1 ? XmlNode.OPEN_TAG : XmlNode.SELF_TAG);
             // parse attribute
             int attrIdx = decodeSize (); // index of first attibute
             while (attrIdx != 0) {
                 n.addAttribute (new XmlAttribute (m_tags[attrIdx-1], decodeString ()));
                 attrIdx = decodeSize (); // index of next attibute
             }
             XmlNode child = parseNode ();
             while (child != null) {
                 n.addChild (child);
                 child = parseNode ();
             }
             return n;
         } 
         return null;
     }
     
 //#ifdef api.bmlDebug
     void visit (XmlVisitor v) {
         if (m_root != null) {
             m_root.visit (v);
         }
     }
 //#endif
 
     void close () {
         m_root = null;
     }
 }
