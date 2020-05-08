 /**
  *
  * SIROCCO
  * Copyright (C) 2012 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  *  $Id$
  *
  */
 package org.ow2.sirocco.apis.rest.cimi.sdk;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiAddress;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiAddressCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiAddressCollectionRoot;
 
 /**
  * IP address, and its associated metadata, for a particular Network
  */
 public class Address extends Resource<CimiAddress> {
     public static final String TYPE_URI = "http://schemas.dmtf.org/cimi/1/Address";
 
     Address(final CimiClient cimiClient, final String id) {
         super(cimiClient, new CimiAddress());
         this.cimiObject.setHref(id);
     }
 
     Address(final CimiClient cimiClient, final CimiAddress cimiAddress) {
         super(cimiClient, cimiAddress);
     }
 
     public String getIp() {
        return this.cimiObject.getIp();
     }
 
     public void setIp(final String ip) {
        this.cimiObject.setIp(ip);
     }
 
     public String getHostname() {
         return this.cimiObject.getHostname();
     }
 
     public void setHostname(final String hostname) {
         this.cimiObject.setHostname(hostname);
     }
 
     public String getAllocation() {
         return this.cimiObject.getAllocation();
     }
 
     public void setAllocation(final String allocation) {
         this.cimiObject.setAllocation(allocation);
     }
 
     public String getDefaultGateway() {
         return this.cimiObject.getDefaultGateway();
     }
 
     public void setDefaultGateway(final String defaultGateway) {
         this.cimiObject.setDefaultGateway(defaultGateway);
     }
 
     public String[] getDns() {
         return this.cimiObject.getDns();
     }
 
     public void setDns(final String[] dns) {
         this.cimiObject.setDns(dns);
     }
 
     public String getProtocol() {
         return this.cimiObject.getProtocol();
     }
 
     public void setProtocol(final String protocol) {
         this.cimiObject.setProtocol(protocol);
     }
 
     public String getMask() {
         return this.cimiObject.getMask();
     }
 
     public void setMask(final String mask) {
         this.cimiObject.setMask(mask);
     }
 
     public Network getNetwork() {
         if (this.cimiObject.getNetwork() != null) {
             return new Network(this.cimiClient, this.cimiObject.getNetwork());
         } else {
             return null;
         }
     }
 
     public void setNetwork(final Network network) {
         this.cimiObject.setNetwork(network.cimiObject);
     }
 
     public static List<Address> getAddresses(final CimiClient client, final QueryParams queryParams) throws CimiException {
         if (client.cloudEntryPoint.getAddresses() == null) {
             throw new CimiException("Unsupported operation");
         }
         CimiAddressCollection addressCollection = client.getRequest(
             client.extractPath(client.cloudEntryPoint.getAddresses().getHref()), CimiAddressCollectionRoot.class, queryParams);
         List<Address> result = new ArrayList<Address>();
 
         if (addressCollection.getCollection() != null) {
             for (CimiAddress cimiAddress : addressCollection.getCollection().getArray()) {
                 result.add(new Address(client, cimiAddress));
             }
         }
         return result;
     }
 
     public static Address getAddressByReference(final CimiClient client, final String id) throws CimiException {
         return new Address(client, client.getCimiObjectByReference(id, CimiAddress.class));
     }
 }
