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
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sola.services.ejb.administrative.repository.entities;
 
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import org.sola.services.common.LocalInfo;
 import org.sola.services.common.repository.RepositoryUtility;
 import org.sola.services.common.repository.entities.AbstractVersionedEntity;
 import org.sola.services.ejb.system.br.Result;
 import org.sola.services.ejb.system.businesslogic.SystemEJBLocal;
 import org.sola.services.ejb.transaction.businesslogic.TransactionEJBLocal;
 import org.sola.services.ejb.transaction.repository.entities.Transaction;
 import org.sola.services.ejb.transaction.repository.entities.TransactionBasic;
 import org.sola.services.ejb.transaction.repository.entities.TransactionStatusType;
 
 /**
  * Entity representing the administrative.notation table. 
  * @author soladev
  */
 @Table(name = "notation", schema = "administrative")
 public class BaUnitNotation extends AbstractVersionedEntity {
 
     public static final String QUERY_PARAMETER_TRANSACTIONID = "transactionId";
     public static final String QUERY_WHERE_BYTRANSACTIONID = "transaction_id = "
             + "#{" + QUERY_PARAMETER_TRANSACTIONID + "}";
     public static final String QUERY_ORDER_BY = " status_code, reference_nr";
     
     @Id
     @Column(name = "id")
     private String id;
     @Column(name = "ba_unit_id")
     private String baUnitId;
     @Column(name = "status_code", insertable = false, updatable = false)
     private String statusCode;
     @Column(name = "transaction_id", updatable = false)
     private String transactionId;
     @Column(name = "rrr_id")
     private String rrrId;
     @Column(name = "reference_nr")
     private String referenceNr;
     @Column(name = "notation_text")
     private String notationText;
     private Boolean locked = null;
     @Column(name = "change_time")
     private Date changeTime;
 
     public Date getChangeTime() {
         return changeTime;
     }
 
     public void setChangeTime(Date changeTime) {
         this.changeTime = changeTime;
     }
     
     public BaUnitNotation() {
         super();
     }
 
     private Transaction getTransaction() {
         Transaction result = null;
         TransactionEJBLocal transactionEJB = RepositoryUtility.tryGetEJB(TransactionEJBLocal.class);
         if (transactionEJB != null) {
             result = transactionEJB.getTransactionById(getTransactionId(), Transaction.class);
         }
         return result;
     }
 
     private String generateNotationNumber() {
         String result = "";
         SystemEJBLocal systemEJB = RepositoryUtility.tryGetEJB(SystemEJBLocal.class);
         if (systemEJB != null) {
             Result newNumberResult = systemEJB.checkRuleGetResultSingle("generate-notation-reference-nr", null);
             if (newNumberResult != null && newNumberResult.getValue() != null) {
                 result = newNumberResult.getValue().toString();
             }
         }
         return result;
     }
 
     public String getId() {
         id = id == null ? generateId() : id;
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getBaUnitId() {
         return baUnitId;
     }
 
     public void setBaUnitId(String baUnitId) {
         this.baUnitId = baUnitId;
     }
 
     public String getNotationText() {
         return notationText;
     }
 
     public void setNotationText(String notationText) {
         this.notationText = notationText;
     }
 
     public String getReferenceNr() {
         return referenceNr;
     }
 
     public void setReferenceNr(String referenceNr) {
         this.referenceNr = referenceNr;
     }
 
     public String getRrrId() {
         return rrrId;
     }
 
     public void setRrrId(String rrrId) {
         this.rrrId = rrrId;
     }
 
     public String getStatusCode() {
         return statusCode;
     }
 
     public void setStatusCode(String statusCode) {
         this.statusCode = statusCode;
     }
 
     public String getTransactionId() {
         return transactionId;
     }
 
     public void setTransactionId(String transactionId) {
         this.transactionId = transactionId;
     }
 
     public Boolean isLocked() {
         if (locked == null) {
             locked = false;
             Transaction transaction = getTransaction();
             if (transaction != null
                     && transaction.getStatusCode().equals(TransactionStatusType.COMPLETED)) {
                 locked = true;
             }
         }
         return locked;
     }
 
     @Override
     public void preSave() {
 
         if (this.isNew()) {
             setTransactionId(LocalInfo.getTransactionId());
         }
 
         if (isNew() && getReferenceNr() == null) {
             // Assign a generated number to the notation if it is not currently set. 
             setReferenceNr(generateNotationNumber());
         }
 
         super.preSave();
     }
 }
