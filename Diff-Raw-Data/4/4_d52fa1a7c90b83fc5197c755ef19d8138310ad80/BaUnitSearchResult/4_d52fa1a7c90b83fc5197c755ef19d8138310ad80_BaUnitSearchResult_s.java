 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.ejb.search.repository.entities;
 
 import javax.persistence.Column;
 import javax.persistence.Id;
 import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;
 
 public class BaUnitSearchResult extends AbstractReadOnlyEntity {
 
     public static final String SEARCH_QUERY =
             "SELECT b.id, b.name, b.name_firstpart, b.name_lastpart, b.status_code, "
             + "(SELECT string_agg(COALESCE(p.name, '') || ' ' || COALESCE(p.last_name, ''), '::::') "
             + "FROM administrative.rrr rrr INNER JOIN (administrative.party_for_rrr pr "
             + "INNER JOIN party.party p ON pr.party_id = p.id) ON rrr.id = pr.rrr_id "
             + "WHERE rrr.status_code = 'current' AND "
             + "(POSITION(LOWER(#{ownerName}) IN LOWER(COALESCE(p.name, ''))) > 0 OR "
             + "POSITION(LOWER(#{ownerName}) IN LOWER(COALESCE(p.last_name, ''))) > 0)) AS rightholders "
             + "FROM administrative.ba_unit b "
            + "WHERE (b.name_firstpart = '' OR #{nameFirstPart} = '') "
            + "AND (b.name_lastpart = '' OR #{nameLastPart} = '') "
             + "LIMIT 101";
     @Id
     @Column
     private String id;
     @Column
     private String name;
     @Column(name = "name_firstpart")
     private String nameFirstPart;
     @Column(name = "name_lastpart")
     private String nameLastPart;
     @Column(name = "status_code")
     private String statusCode;
     @Column
     private String rightholders;
 
     public BaUnitSearchResult() {
         super();
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getNameFirstPart() {
         return nameFirstPart;
     }
 
     public void setNameFirstPart(String nameFirstPart) {
         this.nameFirstPart = nameFirstPart;
     }
 
     public String getNameLastPart() {
         return nameLastPart;
     }
 
     public void setNameLastPart(String nameLastPart) {
         this.nameLastPart = nameLastPart;
     }
 
     public String getRightholders() {
         return rightholders;
     }
 
     public void setRightholders(String rightholders) {
         this.rightholders = rightholders;
     }
 
     public String getStatusCode() {
         return statusCode;
     }
 
     public void setStatusCode(String statusCode) {
         this.statusCode = statusCode;
     }
 }
