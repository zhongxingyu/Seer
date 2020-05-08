 /*
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.emr.paperrecord;
 
 import org.openmrs.BaseOpenmrsObject;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.Person;
 import org.openmrs.User;
 
 import java.util.Date;
 
 
 /**
  *
  * Modelling of Paper Records:
  *
  *  Paper Records are not modeled directly by a domain object with the EMR module.  Instead, the existence of a
  *  paper record for patient is noted by the presence of a Patient Identifier of type GP_PAPER_RECORD_IDENTIFIER_TYPE
  *  (which I'll call the record identifier).  Each record identifier has an associated location.  In the initial
  *  deployment of the system, we all record identifiers have the same location, "Mirebalais", but it is intended in
  *  the future to support multiple locations... for instance, if multiple hospitals are handled in the same system,
  *  or if there are multiple sets of patient records within a single hospital (ie, perhaps the records at the
  *  "Outpatient Clinic" and the records at the "Women's Health Clinic").
  *
  *  Within a single location, all the record identifiers should be unique, and a patient should have no more than
  *  one record (since record identifiers are assigned by the EMR, and not manually, we should be able to prevent
  *  the creation of multiple records for the same patient at the same location--BUT we will need to handle the
 *  potential to merging of two patients, each with their own paper record).
  *
  *  Requests for paper records are modeled and tracked via PaperRecordRequest domain object.  Requests are made
  *  via the PaperRecordService API.  A request is made for a certain Patient's record at a specified medical record
  *  Location to be send to some requested Location.  Requests can have the following states:
  *
  *  OPEN--the initial state of a request after it is placed
  *
 *  ASSIGNED_TO_PULL, ASSIGNED_TO_CREATE--after an archivist has claimed responsibility for a record, it is moved
  *  into one of these two states... if the system determines that no record for the specified patient exists at the
  *  specified location, the request transitions into the "CREATE" state and a new record identifier is assigned.
  *  Otherwise, the archivist is prompted with the record identifier of the record to retrieve, and the record
  *  transitioned into the "PULL" state.
  *
  *  SENT--once an archivist retrieves or creates a record and enters/scans the record identifier, the request is
  *  transitioned to the SENT state; a request in the SENT state represents that the associated record has been
  *  "checked out" of the archive room--and therefore the requested Location on the request should be the current
  *  location of the record.
  *
  *  RETURNED--one a chart is returned to the archive room and the archivist enters/scans the record identifier,
  *  the record is transitioned to the RETURNED state, effectively ending the workflow of a Paper Record Request.
  *
  * All the states except for "RETURNED" are considered "active" states.  A single paper record should never have more
  * than one request in an "active" state at any one time.  (This may change in the future if we want to allow a record
  * that has been "SENT" to be requested by another location).
  *
  * A few notes--we don't currently 100% support multiple paper record locations... there are a few API methods within
  * the PaperRecordService that will have to be modified to support filtering by location in order to fully support
  * multiple locations. (These methods should be flagged with TO DOS referencings this point within the code)
  *
 * Also, although we currently don't specifically mandate that record identifiers be unique across *all* locations, but we may
  * want to enforce this as we add additional locations, so that given a record number we can identify the patient
  * the record refers to without having to be in the context of a specific location.  (Since for storage and retrieval,
  * it is convenient for records to have sequential record identifiers, we could accomplish this via an alphanumeric
  * prefix--ie, A0000001 and B0000001. In Mirebalais we are using a prefix like this preparation for eventuality of
  * adding multiple locations).
  **/
 
 
 public class PaperRecordRequest extends BaseOpenmrsObject {
 
     public enum Status{ OPEN, ASSIGNED_TO_PULL, ASSIGNED_TO_CREATE, SENT, RETURNED, CANCELLED }
 
     private Integer requestId;
 
     private Patient patient;
 
     private String identifier;
 
     private Location recordLocation;
 
     private Location requestLocation;
 
     private Person assignee;
 
     private Status status = Status.OPEN;
 
     private User creator;
 
     private Date dateCreated;
 
     private Date dateStatusChanged;
 
     // TODO: we could add a type here at some point if need be
 
     public PaperRecordRequest() {
     }
 
     public void updateStatus(Status status) {
         this.status = status;
         this.dateStatusChanged = new Date();
     }
 
     @Override
     public Integer getId() {
         return requestId;
     }
 
     @Override
     public void setId(Integer requestId) {
        this.requestId = requestId;
     }
 
     public Integer getRequestId() {
         return requestId;
     }
 
     public void setRequestId(Integer requestId) {
         this.requestId = requestId;
     }
 
     public Patient getPatient() {
         return patient;
     }
 
     public void setPatient(Patient patient) {
         this.patient = patient;
     }
 
     public String getIdentifier() {
         return identifier;
     }
 
     public void setIdentifier(String identifier) {
         this.identifier = identifier;
     }
 
     public Location getRecordLocation() {
         return recordLocation;
     }
 
     public void setRecordLocation(Location recordLocation) {
         this.recordLocation = recordLocation;
     }
 
     public Location getRequestLocation() {
         return requestLocation;
     }
 
     public void setRequestLocation(Location requestLocation) {
         this.requestLocation = requestLocation;
     }
 
     public Person getAssignee() {
         return assignee;
     }
 
     public void setAssignee(Person assignee) {
         this.assignee = assignee;
     }
 
     // no setter for status is intentional, so we force use of the updateStatus method
 
     public Status getStatus() {
         return status;
     }
 
     public User getCreator() {
         return creator;
     }
 
     public void setCreator(User creator) {
         this.creator = creator;
     }
 
     public Date getDateCreated() {
         return dateCreated;
     }
 
     public void setDateCreated(Date dateCreated) {
         this.dateCreated = dateCreated;
     }
 
     public Date getDateStatusChanged() {
         return dateStatusChanged;
     }
 
     public void setDateStatusChanged(Date dateStatusChanged) {
         this.dateStatusChanged = dateStatusChanged;
     }
 }
