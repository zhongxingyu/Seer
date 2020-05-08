 package com.terradue.dsi.model;
 
 /*
  *  Copyright 2012 Terradue srl
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 import static javax.xml.bind.annotation.XmlAccessType.FIELD;
 
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * @since 0.2
  */
 @XmlAccessorType( FIELD )
 @XmlRootElement( name = "networkStorage" )
 public final class NetworkStorage
 {
 
     @XmlElement( name = "networkStorageId" )
     private String id;
 
     @XmlElement( name = "networkStorageName" )
     private String name;
 
     @XmlElement( name = "networkStorageDescription" )
     private String description;
 
     @XmlElement( name = "networkStorageProvider" )
     private String provider;
 
    @XmlElement( name = "networkStorageStorageSizeGb" )
     private int size;
 
    @XmlElement( name = "networkStorageStorageExportProtocol" )
     private String exportProtocol;
 
    @XmlElement( name = "networkStorageStorageExportUrl" )
     private String exportUrl;
 
     @XmlElement( name = "network" )
     private Network network;
 
     public String getId()
     {
         return id;
     }
 
     public void setId( String id )
     {
         this.id = id;
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName( String name )
     {
         this.name = name;
     }
 
     public String getDescription()
     {
         return description;
     }
 
     public void setDescription( String description )
     {
         this.description = description;
     }
 
     public String getProvider()
     {
         return provider;
     }
 
     public void setProvider( String provider )
     {
         this.provider = provider;
     }
 
     public int getSize()
     {
         return size;
     }
 
     public void setSize( int size )
     {
         this.size = size;
     }
 
     public String getExportProtocol()
     {
         return exportProtocol;
     }
 
     public void setExportProtocol( String exportProtocol )
     {
         this.exportProtocol = exportProtocol;
     }
 
     public String getExportUrl()
     {
         return exportUrl;
     }
 
     public void setExportUrl( String exportUrl )
     {
         this.exportUrl = exportUrl;
     }
 
     public Network getNetwork()
     {
         return network;
     }
 
     public void setNetwork( Network network )
     {
         this.network = network;
     }
 
 }
