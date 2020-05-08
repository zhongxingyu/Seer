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
 package org.sola.services.ejb.search.repository.entities;
 
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import org.sola.services.common.repository.AccessFunctions;
 import org.sola.services.common.repository.CommonSqlProvider;
 import org.sola.services.common.repository.DefaultSorter;
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
     public static final String QUERY_WHERE_SEARCH_APPLICATIONS =
             "a.lodging_datetime BETWEEN #{" + QUERY_PARAM_FROM_LODGE_DATE + "} AND #{" + QUERY_PARAM_TO_LODGE_DATE + "} "
             + "AND lower(a.nr) LIKE lower(#{" + QUERY_PARAM_APP_NR + "}) "
            + "AND lower(COALESCE(p2.name, '')) LIKE lower(#{" + QUERY_PARAM_AGENT_NAME + "}) "
             + "AND ((lower (COALESCE(p.name, '') || ' ' || COALESCE(p.last_name, '')) LIKE lower(#{" + QUERY_PARAM_CONTACT_NAME + "})) "
             + "    OR (lower (COALESCE(p.name, '')) LIKE lower(#{" + QUERY_PARAM_CONTACT_NAME + "})) "
             + "    OR (lower (COALESCE(p.last_name, '')) LIKE lower(#{" + QUERY_PARAM_CONTACT_NAME + "}))) ";
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
     + "  WHERE aps.application_id = a.id ORDER BY aps.service_order) tmp) ")
     @Column(name = "service_list")
     private String serviceList;
     @Column(name = "fee_paid")
     private Boolean feePaid;
 
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
 }
