 /*
 The contents of this file are subject to the Jbilling Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.jbilling.com/JPL/
 
 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.
 
 The Original Code is jbilling.
 
 The Initial Developer of the Original Code is Emiliano Conde.
 Portions created by Sapienter Billing Software Corp. are Copyright 
 (C) Sapienter Billing Software Corp. All Rights Reserved.
 
 Contributor(s): ______________________________________.
 */
 
 /*
  * Created on Sep 16, 2004
  *
  */
 package com.sapienter.jbilling.server.user;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Hashtable;
 
 import com.sapienter.jbilling.server.entity.ContactDTO;
 
 /**
  * @author Emil
  */
 public class ContactDTOEx extends ContactDTO implements Serializable  {
     
     private Hashtable fields = null; // the entity specific fields
     private Integer type = null; // the contact type
 
     /**
      * 
      */
     public ContactDTOEx() {
         super();
     }
 
     /**
      * @param id
      * @param organizationName
      * @param address1
      * @param address2
      * @param city
      * @param stateProvince
      * @param postalCode
      * @param countryCode
      * @param lastName
      * @param firstName
      * @param initial
      * @param title
      * @param phoneCountryCode
      * @param phoneAreaCode
      * @param phoneNumber
      * @param faxCountryCode
      * @param faxAreaCode
      * @param faxNumber
      * @param email
      * @param createDate
      * @param deleted
      */
     public ContactDTOEx(Integer id, String organizationName, String address1,
             String address2, String city, String stateProvince,
             String postalCode, String countryCode, String lastName,
             String firstName, String initial, String title,
             Integer phoneCountryCode, Integer phoneAreaCode,
             String phoneNumber, Integer faxCountryCode, Integer faxAreaCode,
             String faxNumber, String email, Date createDate, Integer deleted,
             Integer notify) {
         super(id, organizationName, address1, address2, city, stateProvince,
                 postalCode, countryCode, lastName, firstName, initial, title,
                 phoneCountryCode, phoneAreaCode, phoneNumber, faxCountryCode,
                 faxAreaCode, faxNumber, email, createDate, deleted, notify,
                 null);
     }
 
     /**
      * @param otherValue
      */
     public ContactDTOEx(ContactDTO otherValue) {
         super(otherValue);
     }
     
     public ContactDTOEx(ContactWS ws) {
         super(ws);
         // contacts from ws are always included in notifications
         setInclude(new Integer(1));
         // now add the custom fields
         if (ws.getFieldNames() == null || ws.getFieldNames().length == 0) {
             return;
         }
         fields = new Hashtable();
         for(int f = 0; f < ws.getFieldNames().length; f++) {
             fields.put(ws.getFieldNames()[f], new ContactFieldDTOEx(
                     null, ws.getFieldValues()[f], null));
         }
     }
 
     public Hashtable getFields() {
         return fields;
     }
     public void setFields(Hashtable fields) {
         this.fields = fields;
     }
     public Integer getType() {
         return type;
 }
     public void setType(Integer type) {
         this.type = type;
     }
     
     public String toString(){
         return super.toString() + fields.toString(); 
    }

    public String toString(){
        return super.toString() + fields.toString(); 
     }
 }
