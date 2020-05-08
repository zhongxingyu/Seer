 /*
  * The contents of this file are subject to the Open Software License
  * Version 3.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.opensource.org/licenses/osl-3.0.txt
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  */
 package org.mulgara.resolver.distributed;
 
 import org.mulgara.query.rdf.BlankNodeImpl;
 import org.jrdf.graph.AbstractBlankNode;
 import java.net.URI;
 
 /**
  * A BlankNode that represents nodes from a foreign server.
  *
  * @created 2007-04-18
  * @author Paul Gearon
  * @version $Revision: $
  * @modified $Date: $ @maintenanceAuthor $Author: $
  * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
  * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
  */
 @SuppressWarnings("serial")
 public class ForeignBlankNode extends AbstractBlankNode {
 
   /** The internal ID for the node. */
   long nodeId;
   
   /** The URI of the foreign server. */
   final URI serverUri;
   
   /** The hashcode of the foreign server. */
   final int serverHashCode;
   
   public ForeignBlankNode(URI serverUri, BlankNodeImpl remoteNode) {
     this.nodeId = remoteNode.getNodeId();
     this.serverUri = serverUri;
     this.serverHashCode = serverUri.hashCode();
   }
 
 
   /**
    * Provide a representation that is unique for this node.
    * @return A string containing all the unique features of the node.
    */
   public String toString() {
    return serverUri.toString() + ":_" + nodeId;
   }
 
 
   /**
    * Compare node for equality.
    *
    * @param obj The object to compare against.
    * @return True if the object evaluates as an equivalent blank node.
    */
   public boolean equals(Object obj) {
     if (obj == null) return false;
     if (obj == this) return true;
     if (obj instanceof ForeignBlankNode) {
       ForeignBlankNode fbn = (ForeignBlankNode)obj;
       return (serverUri.equals(fbn.serverUri) && nodeId == fbn.nodeId);
     }
     return false;
   }
 
 
   /**
    * Reproducable hashcode for the object.
    * @return Hashcode of the nodeid.
    */
   public int hashCode() {
    return serverHashCode ^ (int)(nodeId ^ (nodeId >>>32));
   }
 
 }
