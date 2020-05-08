 /* 
  * 
  * PROJECT
  *     Name
  *         APS APIs
  *     
  *     Code Version
  *         0.9.1
  *     
  *     Description
  *         Provides the APIs for the application platform services.
  *         
  * COPYRIGHTS
  *     Copyright (C) 2012 by Natusoft AB All rights reserved.
  *     
  * LICENSE
  *     Apache 2.0 (Open Source)
  *     
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *     
  *       http://www.apache.org/licenses/LICENSE-2.0
  *     
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  *     
  * AUTHORS
  *     Tommy Svensson (tommy@natusoft.se)
  *         Changes:
  *         2011-12-31: Created!
  *         
  */
 package se.natusoft.osgi.aps.api.external.model.type;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Describes a data type for a service return value or parameters.
  */
 public class DataTypeDescription {
     //
     // Private Members
     //
 
     /** The description of the owner of this description. */
     private DataTypeDescription owner = null;
 
     /** The type of the data of this description. */
     private DataType dataType = null;
 
     /** The original class that this description came from. */
     private Class dataTypeClass = null;
 
     /** The members of this data description. */
     private Map<String, DataTypeDescription> members = new HashMap<String, DataTypeDescription>();
     
     /** Object real type qname */
     private String objectQName = null;
 
     //
     // Constructors
     //
 
     /**
      * Creates a new DataDescription
      */
     public DataTypeDescription() {}
     
     /**
      * Creates a new DataDescription.
      *
      * @param owner The description of the owner of this description.
      * @param dataType The type of the data of this description.
      * @param objectQName The fully qualified name of the object when this represents an object, null otherwise.
      */
     public DataTypeDescription(DataTypeDescription owner, DataType dataType, String objectQName) {
         this.owner = owner;
         this.dataType = dataType;
         this.objectQName = objectQName;
     }
 
     //
     // Methods
     //
 
     /**
      * Copies data from another DataTypeDescription.
      * <p/>
      * This is relevant when this instance is a subclass of DataTypeDescription (ParameterDataTypeDescription) and
      * there already exists a plain DataTypeDescription representing the type. Then this method can be used
      * to copy that type information into this instance.
      *
      * @param toCopy
      */
     public void copyFrom(DataTypeDescription toCopy) {
         this.owner = toCopy.owner;
         this.dataType = toCopy.dataType;
         this.members = toCopy.members;
         this.objectQName = toCopy.objectQName;
     }
 
     /**
      * Sets the owner of the data description.
      * 
      * @param owner The owner of this data description.
      */
     public void setOwner(DataTypeDescription owner) {
         this.owner = owner;
     }
     
     /**
      * @return true if this data description has an owner.
      */
     public boolean hasOwner() {
         return this.owner != null;
     }
 
     /**
      * @return The owner of this data description.
      */
     public DataTypeDescription getOwner() {
         return this.owner;
     }
 
     /**
      * Sets the data type of this data description.
      * 
      * @param dataType The data type to set.
      */
     public void setDataType(DataType dataType) {
         this.dataType = dataType;
     }
     
     /**
      * @return The type of this data description.
      */
     public DataType getDataType() {
         return this.dataType;
     }
 
     /**
      * Sets the fully qualified name of the object when this represents an object.
      * @param objectQName The object name to set.
      */
     public void setObjectQName(String objectQName) {
         this.objectQName = objectQName;
     }
 
     /**
      * @return The fully qualified name of the object when this represents an object, null otherwise.
      */
     public String getObjectQName() {
         return this.objectQName;
     }
     
     /**
      * Adds a member to this data description.
      *
      * @param memberName The name of the member to add.
      * @param memberDataDescription The description of the added member.
      */
     public void addMember(String memberName, DataTypeDescription memberDataDescription) {
         this.members.put(memberName, memberDataDescription);
     }
     
     /**
      * @return true if this data description have member data descriptions.
      */
     public boolean hasMembers() {
         return !this.members.isEmpty();
     }
 
     /**
      * @return The name of the member data descriptions.
      */
     public Set<String> getMemberNames() {
         return this.members.keySet();
     }
 
     /**
      * @param name The name of the member to get the data description for.
      *
      * @return The data description of the named member.
      */
     public DataTypeDescription getMemberDataDescriptionByName(String name) {
         return this.members.get(name);
     }
 
     /**
      * Sets the original class this data type description came from.
      *
     * @param clazz The class to set.
      */
    public void setDataTypeClass(Class clazz) {
         this.dataTypeClass = dataTypeClass;
     }
 
     /**
      * @return The original class this data type description came from.
      */
     public Class getDataTypeClass() {
         return this.dataTypeClass;
     }
 }
