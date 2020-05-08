 /*
  * The Tokyo Project is hosted on Sourceforge:
  * http://sourceforge.net/projects/tokyo/
  * 
  * Copyright (c) 2005-2007 Eric Bréchemier
  * http://eric.brechemier.name
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
  */
 package net.sf.tokyo;
 
 /**
  * ITokyoNaut is the single interface making up Tokyo API, 
  * allowing both XML and non-XML data transformations.<br/>
  *
  * <p>
  * The name "TokyoNaut" is composed of "Tokyo", a mind-changing city where this project started,
  * while "Naut" is a shorthand reference to Nautilus, Captain Nemo's submarine in Jules Verne's
  * "Twenty Thousand Leagues Under the Sea".
  * More inspiration comes from Captain Nemo's motto: "Mobilis in Mobili".
  * </p>
  *
  * <p>
  * Plug and unplug allow to initialize a chain of collaborating TokyoNauts, and unchain them
  * to let them free any allocated ressources at the end of the processing.
  * </p>
  *
  * <p>
  * Methods areWeThereYet() and filter() are called in turn to check whether processing is complete, 
  * and have meta and data buffers filtered and transmitted along the chain of TokyoNauts.
  * </p>
  *
  * @author Eric Br&eacute;chemier
  * @version Harajuku
  */
 public interface ITokyoNaut
 {
   
   /**
    * Check whether TokyoNaut is available for communication.<br/>
    *
    * <p>
    * This method provide a simple means to control the reading process step by step.
    * The following loop will run until reading is complete:
 <pre>
 int[] meta = {1,0,0,0,0};
 byte[] data = new byte[500];
 
 while( tokyoNaut.areWeThereYet()==false )
 {
   tokyoNaut.filter(meta,data);
 } 
 </pre>
    * </p>
    *
    * @return false until this TokyoNaut has reached the end of its own processing 
    *         (e.g. following the end of input meta/data)
    */
   public boolean areWeThereYet();
   
   /**
    * Filter meta/data through this TokyoNaut and send to the following.<br/>
    *
    * <p>
    * Allows to process an event corresponding to the start, continuation or end of a data item.
    * The event carries a chunk of data (a buffer of bytes) and associated meta information 
    * (an array of integer attributes including the offset and length of the buffer and the item type).
    * </p>
    * 
    * <p>
    * As a default behavior to allow chains of actions, TokyoNauts are expected to forward received 
    * meta/data to the following TokyoNaut (if any) previously defined with the plug() method. 
    * </p>
    *
    * <p>
    * Besides this default behavior, each TokyoNaut can provide a basic service by executing
    * an atomic action (some side effect) before forwarding meta/data.
    * </p>
    *
    * <p>
    * The current specification of the TokyoNaut interface is signalled by
    * VERSION 0x01 set in leading byte of the "meta" buffer.
    * The list of data item types in this version is based on the 
    * <a href="http://www.w3.org/TR/2004/REC-xml-infoset-20040204" alt="XML Infoset (Second Edition)">
    * W3C XML Information Set Recommendation (Second Edition)
    * </a>
    * </p>
    *
    * <p>
   * Versions 0x00-0xFF are "reserved" for current and future versions of this specification, 
   * while versions 0x100-0xFFFF are "user-defined". Versions 0x10000-0xFFFFFFFF are "reserved" 
   * for future use. Implementors wishing to define their own set of fields MUST signal it with
    * a leading version in the "user-defined" range.
    * </p>
    *
    * <p>
    * <table border="1">
    *   <tr>
    *     <th>0 - Version</th>
    *     <th>1 - Event Type</th>
    *     <th>2 - Item Type</th>
    *     <th>3 - Offset</th>
    *     <th>4 - Length</th>
    *   </tr>
    *   <tr>
    *     <td>0x01</td>
    *     <td>Event type corresponding to current data item
    *       <ul>
    *         <li>+1 for START of item data</li>
    *         <li>-1 for END of item data (empty)</li>
    *         <li>0 for CONTINUATION of current item data</li>
    *         <li>14 (0xE) for ERRORS</li>
    *       </ul>
    *      <p>Each data item corresponds to at least a START event with item's data
    *      and an END event carrying no data (length of 0 bytes).
    *      </p>
    *      <p>The insertion of one or more CONTINUATION events between START and END allows the 
    *      transmission of long data items using a smaller (fixed size) buffer. This is typically
    *      relevant only for long runs of character data, but could happen, rarely, for element 
    *      or attribute names. If the data buffer in the START event is empty or smaller than 
    *      the maximum size allowed in the buffer, TokyoNaut instances may assume that the data 
    *      inside the START event is complete.
    *      </p>
    *      <p>Errors can also be signalled as events. For error events, the "Item type"
    *      corresponds to an error code, and the data buffer contains an optional error description.
    *      </p>
    *     </td>
    *     <td>type of data item based on the XML Infoset.</td>
    *     <td>starting position of data item (number of leading bytes to be ignored), typically 0</td>
    *     <td>number of bytes of the data item, starting at 0-based offset</td>
    *   </tr>
    * </table>
    * </p>
    *
    * <p>
    * The list of data items correspond to the XML Information Set; the constants derive from the
    * numbering in the table of Contents of the XML Information Set, and the numbering of properties
    * for each Information item, e.g. <br/>
    * taking major "3" from "2.3. Attribute Information Items"<br/>
    * and minor "2" from "2. [local name]"<br/>
    * gives 0x32 the ATTRIBUTE_LOCAL_NAME data item.<br/>
    * The minor "0" is set for the item itself, e.g. 0x30 corresponds to ATTRIBUTE_ITEM.
    * </p>
    * 
    * <p>
    * In the descriptions below, an Extended Backus-Naur Form (EBNF) grammar is used to define 
    * the meaningful sequences of item definitions, with the syntax defined in 
    * <a href="http://www.w3.org/TR/xml/#sec-notation">XML 1.0 Recommendation EBNF Notation</a>.
    * The order of events is based on the document order defined in 
    * <a href="http://www.w3.org/TR/xml/#sec-documents">XML 1.0 Documents</a>.
    * In order to avoid cyclic inclusion of the same information items, references to information 
    * items excepted as children (e.g. parent, owner, references...) are left out of current scope.
    * </p>
    
    * <p>
    * <table border="1">
    *   <tr>
    *     <th>
    *     Data Item Types
    *     </th>
    *   </tr>
    *   <tr>
    *     <td>
    *       <ul>
    *         <li><b>0x10 - Document Information Item</b><br/>
    *         <code>Document ::= DocumentBaseUri? (XmlVersion CharacterEncoding? Standalone?)? UnparsedEntities? Notations? AllDeclarationsProcessed? DocumentChildren</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x11 - Document Children<br/>
    *         <code>DocumentChildren ::= (Comment|PI)* DocumentTypeDeclaration? (Comment|PI)* Element (Comment|PI)*</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x12 - Document Element (unused, replaced with Element in DocumentChildren)</li>
    *         <li>&nbsp;&nbsp;0x13 - Notations<br/>
    *         <code>Notations ::= Notation*</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x14 - UnparsedEntities<br/>
    *         <code>UnparsedEntities ::= UnparsedEntity*</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x15 - DocumentBaseUri</li>
    *         <li>&nbsp;&nbsp;0x16 - CharacterEncoding</li>
    *         <li>&nbsp;&nbsp;0x17 - Standalone<br/>
    *         <code>Standalone ::= 'yes' | 'no'</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x18 - XmlVersion</li>
    *         <li>&nbsp;&nbsp;0x19 - AllDeclarationsProcessed (default 'false')<br/>
    *         <code>AllDeclarationsProcessed ::= 'true' | 'false'</code>
    *         </li>
    
    *         <li><b>0x20 - Element Information Item</b><br/>
    *         <code>Element ::= ElementBaseURI? ElementPrefix? ElementNamespaceName? ElementLocalName NamespaceAttributes? Attributes? ElementChildren?</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x21 - ElementNamespaceName</li>
    *         <li>&nbsp;&nbsp;0x22 - ElementLocalName</li>
    *         <li>&nbsp;&nbsp;0x23 - ElementPrefix</li>
    *         <li>&nbsp;&nbsp;0x24 - Element Children<br/>
    *         <code>ElementChildren ::= (Characters | Element | UnexpandedEntityReference | PI | Comment)*</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x25 - Attributes<br/>
    *         <code>ElementChildren ::= Attribute*</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x26 - NamespaceAttributes<br/>
    *         <code>ElementChildren ::= Namespace*</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x27 - ElementInScopeNamespaces (unused)</li>
    *         <li>&nbsp;&nbsp;0x28 - ElementBaseURI</li>
    *         <li>&nbsp;&nbsp;0x29 - ElementParent (unused)</li>
    
    *         <li><b>0x30 - Attribute Information Item</b><br/>
    *         <code>Attribute ::= AttributeSpecified? AttributeType? AttributePrefix? AttributeNamespaceName? AttributeLocalName AttributeNormalizedValue</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x31 - AttributeNamespaceName</li>
    *         <li>&nbsp;&nbsp;0x32 - AttributeLocalName</li>
    *         <li>&nbsp;&nbsp;0x33 - AttributePrefix</li>
    *         <li>&nbsp;&nbsp;0x34 - AttributeNormalizedValue</li>
    *         <li>&nbsp;&nbsp;0x35 - AttributeSpecified</li>
    *         <li>&nbsp;&nbsp;0x36 - AttributeType</li>
    *         <li>&nbsp;&nbsp;0x37 - AttributeReferences(unused)</li>
    *         <li>&nbsp;&nbsp;0x38 - AttributeOwnerElement(unused)</li>
 
    *         <li><b>0x40 - Processing Instruction Information Item</b><br/>
    *         <code>PI ::= PIBaseURI? PITarget PIContent</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x41 - PITarget</li>
    *         <li>&nbsp;&nbsp;0x42 - PIContent</li>
    *         <li>&nbsp;&nbsp;0x43 - PIBaseURI</li>
    *         <li>&nbsp;&nbsp;0x44 - PINotation (unused)</li>
    *         <li>&nbsp;&nbsp;0x44 - PIParent (unused)</li>
 
    *         <li><b>0x50 - Unexpanded Entity Reference Information Item</b><br/>
    *         <code>UnexpandedEntityReference ::= UnexpandedEntityReferenceBaseURI? UnexpandedEntityReferenceSystemIdentifier? UnexpandedEntityReferencePublicIdentifier? UnexpandedEntityReferenceName</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x51 - UnexpandedEntityReferenceName</li>
    *         <li>&nbsp;&nbsp;0x52 - UnexpandedEntityReferenceSystemIdentifier</li>
    *         <li>&nbsp;&nbsp;0x53 - UnexpandedEntityReferencePublicIdentifier</li>
    *         <li>&nbsp;&nbsp;0x54 - UnexpandedEntityReferenceBaseURI</li>
    *         <li>&nbsp;&nbsp;0x55 - UnexpandedEntityReferenceParent (unused)</li>
 
    *         <li><b>0x60 - Character Information Item (grouped in chunks of characters)</b><br/>
    *         <code>Characters ::= CharactersElementContentWhitespace? CharactersCode</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x61 - CharactersCode (using document character encoding, default UTF-8)</li>
    *         <li>&nbsp;&nbsp;0x62 - CharactersElementContentWhitespace<br/>
    *         <code>CharactersElementContentWhitespace ::= 'true' | 'false'</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x63 - CharactersParent (unused)</li>
 
    *         <li><b>0x70 - Comment Information Item</b><br/>
    *         <code>Comment ::= CommentContent</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x71 - CommentContent</li>
    *         <li>&nbsp;&nbsp;0x72 - CommentParent (unused)</li>
 
    *         <li><b>0x80 - Document Type Declaration Information Item</b><br/>
    *         <code>DocumentTypeDeclaration ::= DTDPublicIdentifier? DTDSystemIdentifier? DTDContent?</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x81 - DTDSystemIdentifier</li>
    *         <li>&nbsp;&nbsp;0x82 - DTDPublicIdentifier</li>
    *         <li>&nbsp;&nbsp;0x83 - DTDContent<br/>
    *         <code>DTDContent ::= PI*</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x84 - DTDParent (unused)</li>
 
    *         <li><b>0x90 - Unparsed Entity Information Item</b><br/>
    *         <code>UnparsedEntity ::= UnparsedEntityName UnparsedEntityNotationName? UnparsedEntityPublicIdentifier? UnparsedEntitySystemIdentifier?</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0x91 - UnparsedEntityName</li>
    *         <li>&nbsp;&nbsp;0x92 - UnparsedEntitySystemIdentifier</li>
    *         <li>&nbsp;&nbsp;0x93 - UnparsedEntityPublicIdentifier</li>
    *         <li>&nbsp;&nbsp;0x94 - UnparsedEntityNotationName</li>
    *         <li>&nbsp;&nbsp;0x95 - UnparsedEntityNotation (unused)</li>
 
    *         <li><b>0xA0 - Notation Information Item</b><br/>
    *         <code>Notation ::= NotationBaseURI? NotationName NotationPublicIdentifier? NotationSystemIdentifier?</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0xA1 - NotationName</li>
    *         <li>&nbsp;&nbsp;0xA2 - NotationSystemIdentifier</li>
    *         <li>&nbsp;&nbsp;0xA3 - NotationPublicIdentifier</li>
    *         <li>&nbsp;&nbsp;0xA4 - NotationBaseURI</li>
    
    *         <li><b>0xB0 - Namespace Information Item</b><br/>
    *         <code>Namespace ::= NamespacePrefix? NamespaceName</code>
    *         </li>
    *         <li>&nbsp;&nbsp;0xB1 - NamespacePrefix</li>
    *         <li>&nbsp;&nbsp;0xB2 - NamespaceName</li>
    *       </ul>
    *     </td>
    *   </tr>
    * </table>
    * </p>
    * 
    * <p>
    * The sequence below represents a very short XML document, with a single empty element &lt;book/&gt;:<br/>
 <PRE>
 // Document
 filter({1,1,0x10,0,0},{});
   // DocumentChildren
   filter({1,1,0x11,0,0},{});
     // Element
     filter({1,1,0x20,0,0},{});
       // Element Local Name
       filter({1,1,0x22,0,4},{'b','o','o','k'});
       filter({1,-1,0x22,0,0},{});
     filter({1,-1,0x20,0,0},{});
   filter({1,-1,0x11,0,0},{});
 filter({1,-1,0x10,0,0},{});
 </PRE>
    *
    * <p>
    * The sample below illustrates the data continuation; it is equivalent to above examples:
 <PRE>
 // Document
 filter({1,1,0x10,0,0},{});
   // DocumentChildren
   filter({1,1,0x11,0,0},{});
     // Element
     filter({1,1,0x20,0,0},{});
       // Element Local Name
       filter({1,1,0x22,0,1},{'b'});
       filter({1,0,0x22,0,1},{'o'});
       filter({1,0,0x22,0,2},{'o','k'});
       filter({1,-1,0x22,0,0},{});
     filter({1,-1,0x20,0,0},{});
   filter({1,-1,0x11,0,0},{});
 filter({1,-1,0x10,0,0},{});
 </PRE>   
    * </p>
    *
    * @param meta information about data, with fields defined by leading version number.
    * @param data the buffer into which the data is written.
    */
   public void filter(int[]meta, byte[] data);
   
  
   /**
    * Plug the TokyoNaut to a destination TokyoNaut.<br/>
    *
    * <p>
    * Following an exclusive mode of relationship, a single destination TokyoNaut can be plugged 
    * with a TokyoNaut at a given time. Subsequent calls to plug() will unplug and replace 
    * previously plugged destination TokyoNaut.
    * </p>
    *
    * @param destination TokyoNaut.
    * @return destination parameter to allow a chain of plug calls.
    */
   public ITokyoNaut plug(ITokyoNaut destination);
   
   /**
    * Unplug the TokyoNaut from previously plugged destination TokyoNaut.<br/>
    *
    * <p>
    * This method unplug recursively all source TokyoNauts chained from this point.
    * If no destination has been set by a previous call to plug(), nothing happens.
    * </p>
    *
    */
   public void unplug();
   
 }
