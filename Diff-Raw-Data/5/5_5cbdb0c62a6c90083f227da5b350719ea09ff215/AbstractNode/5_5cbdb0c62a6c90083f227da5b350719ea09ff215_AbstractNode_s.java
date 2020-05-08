 /**
  * Elastic Grid
  * Copyright (C) 2008-2009 Elastic Grid, LLC.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.elasticgrid.model.internal;
 
 import com.elasticgrid.model.Node;
 import com.elasticgrid.model.NodeProfile;
 import java.net.InetAddress;
 
 /**
  * @author Jerome Bernard
  */
 public abstract class AbstractNode implements Node {
     private NodeProfile profile;
     private InetAddress address;
 
     public AbstractNode() {}
 
     public AbstractNode(NodeProfile profile) {
         this.profile = profile;
     }
 
     public NodeProfile getProfile() {
         return profile;
     }
 
     public InetAddress getAddress() {
         return address;
     }
 
     public Node address(InetAddress address) {
         setAddress(address);
         return this;
     }
 
     public void setProfile(NodeProfile profile) {
         this.profile = profile;
     }
 
     public void setAddress(InetAddress address) {
         this.address = address;
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append("AbstractNode");
         sb.append("{profile=").append(profile);
         sb.append(", address=").append(address);
         sb.append('}');
         return sb.toString();
     }
 }
