 /**
  * ******************************************************************************************
  * Copyright (c) 2013 Food and Agriculture Organization of the United Nations
  * (FAO) and the Lesotho Land Administration Authority (LAA). All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this
  * list of conditions and the following disclaimer. 2. Redistributions in binary
  * form must reproduce the above copyright notice,this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3. Neither the names of FAO, the LAA nor the names of
  * its contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
  * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sola.services.ejb.search.repository.entities;
 
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import org.sola.services.common.repository.AccessFunctions;
 import org.sola.services.common.repository.CommonSqlProvider;
 import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;
 
 /**
  *
  * @author soladev
  */
 @Table(name = "application", schema = "application")
 public class ApplicationSearchResult extends AbstractReadOnlyEntity {
 
     public static final String QUERY_PARAM_USER_NAME = "userName";
     public static final String QUERY_PARAM_FROM_LODGE_DATE = "fromDate";
     public static final String QUERY_PARAM_TO_LODGE_DATE = "toDate";
     public static final String QUERY_PARAM_APP_NR = "appNr";
     public static final String QUERY_PARAM_AGENT_NAME = "agentName";
     public static final String QUERY_PARAM_CONTACT_NAME = "contactName";
     public static final String QUERY_PARAM_DOCUMENT_NUMBER = "documentNumber";
     public static final String QUERY_PARAM_DOCUMENT_REFERENCE = "documentRef";
     public static final String QUERY_PARAM_LEASE_NUMBER = "leaseNumber";
     public static final String QUERY_FROM =
             "(application.application a LEFT JOIN application.application_status_type ast on a.status_code = ast.code) "
             + "LEFT JOIN system.appuser u ON a.assignee_id = u.id "
             + "LEFT JOIN party.party p ON a.contact_person_id = p.id "
             + "LEFT JOIN party.party p2 ON a.agent_id = p2.id ";
     public static final String QUERY_WHERE_GET_ASSIGNED = "u.username = #{" + QUERY_PARAM_USER_NAME + "} "
             + " AND a.status_code in ('lodged', 'approved')";
     public static final String QUERY_WHERE_GET_ASSIGNED_ALL = "u.username IS NOT NULL AND a.status_code in ('lodged', 'approved')";
     public static final String QUERY_WHERE_GET_UNASSIGNED = "u.username IS NULL "
             + " AND a.status_code in ('lodged', 'approved')";
     /**
      * Uses CASE statements to skip execution of the compare_strings function if
      * the parameter string is empty.
      */
     public static final String QUERY_WHERE_SEARCH_APPLICATIONS =
             "a.lodging_datetime BETWEEN #{" + QUERY_PARAM_FROM_LODGE_DATE + "} AND #{" + QUERY_PARAM_TO_LODGE_DATE + "} "
             + "AND (CASE WHEN #{" + QUERY_PARAM_APP_NR + "} = '' THEN true ELSE "
             + "compare_strings(#{" + QUERY_PARAM_APP_NR + "}, a.nr) END) "
             + "AND (CASE WHEN #{" + QUERY_PARAM_CONTACT_NAME + "} = '' THEN true ELSE "
             + "compare_strings(#{" + QUERY_PARAM_CONTACT_NAME + "}, COALESCE(p.name, '') || ' ' || COALESCE(p.last_name, '')) END) "
             + "AND (CASE WHEN #{" + QUERY_PARAM_AGENT_NAME + "} = '' THEN true ELSE "
             + "compare_strings(#{" + QUERY_PARAM_AGENT_NAME + "}, COALESCE(p2.name, '') || ' ' || COALESCE(p2.last_name, '')) END) "
             + "AND (CASE WHEN #{" + QUERY_PARAM_DOCUMENT_NUMBER + "} || #{" + QUERY_PARAM_DOCUMENT_REFERENCE + "} = '' THEN true ELSE EXISTS ( "
             + "SELECT aus.application_id FROM application.application_uses_source aus "
             + "INNER JOIN source.source src ON aus.source_id = src.id "
             + "WHERE a.id = aus.application_id "
             + "AND (CASE WHEN #{" + QUERY_PARAM_DOCUMENT_NUMBER + "} = '' THEN true ELSE "
             + "compare_strings(#{" + QUERY_PARAM_DOCUMENT_NUMBER + "}, COALESCE(src.la_nr, '')) END) "
             + "AND (CASE WHEN #{" + QUERY_PARAM_DOCUMENT_REFERENCE + "} = '' THEN true ELSE "
             + "compare_strings(#{" + QUERY_PARAM_DOCUMENT_REFERENCE + "}, COALESCE(src.reference_nr, '')) END))END)"
             + "AND (CASE WHEN #{" + QUERY_PARAM_LEASE_NUMBER + "} = '' THEN true ELSE (select count(1)>0 from administrative.rrr r3 "
             + "where compare_strings(#{" + QUERY_PARAM_LEASE_NUMBER + "}, coalesce(r3.lease_number, '')) "
             + "and r3.ba_unit_id in (select r4.ba_unit_id from administrative.rrr r4 inner join "
             + "(transaction.transaction t2 inner join application.service s2 on t2.from_service_id = s2.id) "
             + "on r4.transaction_id = t2.id where s2.application_id=a.id)) END)";
     
     public static final String QUERY_ORDER_BY = "a.lodging_datetime desc";
     @Id
     @Column(name = "id")
     @AccessFunctions(onSelect = "a.id")
     private String id;
     @Column(name = "nr")
     private String nr;
     @AccessFunctions(onSelect = "get_translation(ast.display_value, #{" + CommonSqlProvider.PARAM_LANGUAGE_CODE + "})")
     @Column(name = "status")
     private String status;
     @Column(name = "lodging_datetime")
     private Date lodgingDatetime;
     @Column(name = "expected_completion_date")
     private Date expectedCompletionDate;
     @Column(name = "assigned_datetime")
     private Date assignedDatetime;
     @AccessFunctions(onSelect = "(COALESCE(u.first_name, '') || ' ' || COALESCE(u.last_name, ''))")
     @Column(name = "assignee_name")
     private String assigneeName;
     @Column(name = "assignee_id")
     private String assigneeId;
     @AccessFunctions(onSelect = "(COALESCE(p.name, '') || ' ' || COALESCE(p.last_name, ''))")
     @Column(name = "contact_person")
     private String contactPerson;
     @AccessFunctions(onSelect = "(COALESCE(p2.name, '') || ' ' || COALESCE(p2.last_name, ''))")
     @Column(name = "agent")
     private String agent;
     @Column(name = "agent_id")
     private String agentId;
     @Column(name = "contact_person_id")
     private String contactPersonId;
     @AccessFunctions(onSelect = "(SELECT string_agg(tmp.display_value, ',') FROM "
     + " (SELECT get_translation(display_value, #{" + CommonSqlProvider.PARAM_LANGUAGE_CODE + "}) as display_value "
     + "  FROM application.service aps INNER JOIN application.request_type rt ON aps.request_type_code = rt.code "
     + "  WHERE aps.application_id = a.id AND aps.status_code NOT IN ('cancelled') ORDER BY aps.service_order) tmp) ")
     @Column(name = "service_list")
     private String serviceList;
    @AccessFunctions(onSelect = "(select string_agg(distinct r.lease_number, ', ') from administrative.rrr r "
    + "where r.ba_unit_id in (select r2.ba_unit_id from administrative.rrr r2 inner join (transaction.transaction t "
    + "inner join application.service s on t.from_service_id = s.id) on r.transaction_id=t.id where s.application_id = a.id) "
    + "and r.status_code <> 'previous' and r.lease_number is not null and r.lease_number <> '')")
     @Column(name = "affected_lease_numbers")
     private String affectedLeaseNumbers;
     @Column(name = "fee_paid")
     private Boolean feePaid;
     @Column(name = "rowversion")
     @AccessFunctions(onSelect = "a.rowversion")
     private int rowVersion;
     @Column(name = "change_user")
     @AccessFunctions(onSelect = "a.change_user")
     private String changeUser;
     @Column(name = "rowidentifier")
     @AccessFunctions(onSelect = "a.rowidentifier")
     private String rowId;
 
     public ApplicationSearchResult() {
         super();
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getAgent() {
         return agent;
     }
 
     public void setAgent(String agent) {
         this.agent = agent;
     }
 
     public String getAgentId() {
         return agentId;
     }
 
     public void setAgentId(String agentId) {
         this.agentId = agentId;
     }
 
     public Date getAssignedDatetime() {
         return assignedDatetime;
     }
 
     public void setAssignedDatetime(Date assignedDatetime) {
         this.assignedDatetime = assignedDatetime;
     }
 
     public String getAssigneeId() {
         return assigneeId;
     }
 
     public void setAssigneeId(String assigneeId) {
         this.assigneeId = assigneeId;
     }
 
     public String getAssigneeName() {
         return assigneeName;
     }
 
     public void setAssigneeName(String assigneeName) {
         this.assigneeName = assigneeName;
     }
 
     public String getContactPerson() {
         return contactPerson;
     }
 
     public String getAffectedLeaseNumbers() {
         return affectedLeaseNumbers;
     }
 
     public void setAffectedLeaseNumbers(String affectedLeaseNumbers) {
         this.affectedLeaseNumbers = affectedLeaseNumbers;
     }
 
     public void setContactPerson(String contactPerson) {
         this.contactPerson = contactPerson;
     }
 
     public String getContactPersonId() {
         return contactPersonId;
     }
 
     public void setContactPersonId(String contactPersonId) {
         this.contactPersonId = contactPersonId;
     }
 
     public Date getExpectedCompletionDate() {
         return expectedCompletionDate;
     }
 
     public void setExpectedCompletionDate(Date expectedCompletionDate) {
         this.expectedCompletionDate = expectedCompletionDate;
     }
 
     public Boolean getFeePaid() {
         return feePaid;
     }
 
     public void setFeePaid(Boolean feePaid) {
         this.feePaid = feePaid;
     }
 
     public Date getLodgingDatetime() {
         return lodgingDatetime;
     }
 
     public void setLodgingDatetime(Date lodgingDatetime) {
         this.lodgingDatetime = lodgingDatetime;
     }
 
     public String getNr() {
         return nr;
     }
 
     public void setNr(String nr) {
         this.nr = nr;
     }
 
     public String getServiceList() {
         return serviceList;
     }
 
     public void setServiceList(String serviceList) {
         this.serviceList = serviceList;
     }
 
     public String getStatus() {
         return status;
     }
 
     public void setStatus(String status) {
         this.status = status;
     }
 
     public String getChangeUser() {
         return changeUser;
     }
 
     public void setChangeUser(String changeUser) {
         this.changeUser = changeUser;
     }
 
     public String getRowId() {
         return rowId;
     }
 
     public void setRowId(String rowId) {
         this.rowId = rowId;
     }
 
     public int getRowVersion() {
         return rowVersion;
     }
 
     public void setRowVersion(int rowVersion) {
         this.rowVersion = rowVersion;
     }
 }
