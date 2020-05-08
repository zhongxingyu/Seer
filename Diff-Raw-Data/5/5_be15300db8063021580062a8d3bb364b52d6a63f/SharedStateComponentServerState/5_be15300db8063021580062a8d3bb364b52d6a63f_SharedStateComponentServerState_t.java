 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.sharedstate.common.state;
 
 import java.io.Serializable;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
 import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
 import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
 
 /**
  * The component setup
  * @author jprovino
  */
 @XmlRootElement(name = "shared-state-component")
 @ServerState
 public class SharedStateComponentServerState extends CellComponentServerState
         implements Serializable
 {
     private MapEntry[] maps = new MapEntry[0];
 
     public SharedStateComponentServerState() {
     }
 
     public String getServerComponentClassName() {
         return "org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO";
     }
     
     @XmlElement
     public MapEntry[] getMaps() {
         return maps;
     }
     
     public void setMaps(MapEntry[] maps) {
         this.maps = maps;
     }
     
    public static class MapEntry implements Serializable {
         private String name;
         private SharedDataEntry[] data;
         
         public MapEntry() {
         }
         
         public MapEntry(String name) {
             this.name = name;
         }
         
         @XmlElement
         public String getName() {
             return name;
         }
         
         public void setName(String name) {
             this.name = name;
         }
 
         @XmlElement
         public SharedDataEntry[] getData() {
             return data;
         }
 
         public void setData(SharedDataEntry[] data) {
             this.data = data;
         }
     }
 
    public static class SharedDataEntry implements Serializable {
         private String key;
         private SharedData value;
 
         public SharedDataEntry() {
         }
 
         public SharedDataEntry(String key, SharedData value) {
             this.key = key;
             this.value = value;
         }
 
         @XmlElement
         public String getKey() {
             return key;
         }
 
         public void setKey(String key) {
             this.key = key;
         }
 
         @XmlElement
         @XmlJavaTypeAdapter(SharedDataXmlAdapter.class)
         public SharedData getValue() {
             return value;
         }
 
         public void setValue(SharedData value) {
             this.value = value;
         }
     }
 }
