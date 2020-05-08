 /**
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
 package org.openmrs.module.simplelabentry.web.dwr;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Obs;
 import org.openmrs.Order;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.simplelabentry.util.SimpleLabEntryUtil;
 
 public class LabOrderListItem {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 
 	private Integer orderId;
 	private Integer patientId;
 	private Integer conceptId;
 	private String conceptShortName;
 	private String startDateString;
 	private String encounterDateString;
 	private Integer locationId;
 	private String locationName;
 	private String discontinuedDateString;
 	private String accessionNumber;
 	private String instructions;
 	private List<LabResultListItem> labResults = new ArrayList<LabResultListItem>();
 	private List<String> labOrderIdsForPatient = new ArrayList<String>();
 
 	public LabOrderListItem() { }
 	
 	public LabOrderListItem(Order order) {
 		orderId = order.getOrderId();
 		patientId = order.getPatient().getPatientId();
 		conceptId = order.getConcept().getConceptId();
		conceptShortName = order.getConcept().getBestShortName(Context.getLocale()).toString();
 		startDateString = order.getStartDate() == null ? null : Context.getDateFormat().format(order.getStartDate());
 		encounterDateString = order.getEncounter().getEncounterDatetime() == null ? null : Context.getDateFormat().format(order.getEncounter().getEncounterDatetime());
 		locationId = order.getEncounter().getLocation().getLocationId();
 		locationName = order.getEncounter().getLocation().getName();
 		discontinuedDateString = order.getDiscontinuedDate() == null ? null : Context.getDateFormat().format(order.getDiscontinuedDate());
 		accessionNumber = order.getAccessionNumber();
 		instructions = order.getInstructions();
 		for (Obs o : order.getEncounter().getObs()) {
 			labResults.add(new LabResultListItem(o));
 		}
 		labOrderIdsForPatient = SimpleLabEntryUtil.getLabOrderIDsByPatient(order.getPatient(), 6);
 	}
 
 	public boolean equals(Object obj) {
 		if (obj instanceof LabOrderListItem) {
 			LabOrderListItem ot = (LabOrderListItem)obj;
 			if (ot.getOrderId() == null || getOrderId() == null)
 				return false;
 			return ot.getOrderId().equals(getOrderId());
 		}
 		return false;
 	}
 	
 	public int hashCode() {
 		if (orderId != null)
 			return 31 * orderId.hashCode();
 		else
 			return super.hashCode();
 	}
 
 	public Integer getOrderId() {
 		return orderId;
 	}
 
 	public void setOrderId(Integer orderId) {
 		this.orderId = orderId;
 	}
 
 	public Integer getPatientId() {
 		return patientId;
 	}
 
 	public void setPatientId(Integer patientId) {
 		this.patientId = patientId;
 	}
 
 	public Integer getConceptId() {
 		return conceptId;
 	}
 
 	public void setConceptId(Integer conceptId) {
 		this.conceptId = conceptId;
 	}
 
 	public String getConceptShortName() {
 		return conceptShortName;
 	}
 
 	public void setConceptShortName(String conceptShortName) {
 		this.conceptShortName = conceptShortName;
 	}
 
 	public String getStartDateString() {
 		return startDateString;
 	}
 
 	public void setStartDateString(String startDateString) {
 		this.startDateString = startDateString;
 	}
 
 	public String getEncounterDateString() {
 		return encounterDateString;
 	}
 
 	public void setEncounterDateString(String encounterDateString) {
 		this.encounterDateString = encounterDateString;
 	}
 
 	public String getDiscontinuedDateString() {
 		return discontinuedDateString;
 	}
 
 	public void setDiscontinuedDateString(String discontinuedDateString) {
 		this.discontinuedDateString = discontinuedDateString;
 	}
 
 	public Integer getLocationId() {
 		return locationId;
 	}
 
 	public void setLocationId(Integer locationId) {
 		this.locationId = locationId;
 	}
 
 	public String getLocationName() {
 		return locationName;
 	}
 
 	public void setLocationName(String locationName) {
 		this.locationName = locationName;
 	}
 
 	public String getAccessionNumber() {
 		return accessionNumber;
 	}
 
 	public void setAccessionNumber(String accessionNumber) {
 		this.accessionNumber = accessionNumber;
 	}
 
 	public List<LabResultListItem> getLabResults() {
 		return labResults;
 	}
 
 	public void setLabResults(List<LabResultListItem> labResults) {
 		this.labResults = labResults;
 	}
 	
 	public void addLabResult(LabResultListItem labResult) {
 		this.labResults.add(labResult);
 	}
 
     public String getInstructions() {
         return instructions;
     }
 
     public void setInstructions(String instructions) {
         this.instructions = instructions;
     }
 
     public List<String> getLabOrderIdsForPatient() {
         return labOrderIdsForPatient;
     }
 
     public void setLabOrderIdsForPatient(List<String> labOrderIdsForPatient) {
         this.labOrderIdsForPatient = labOrderIdsForPatient;
     }
 
     public Log getLog() {
         return log;
     }
 }
