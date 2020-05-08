 /*
  * Copyright 2010 Ian Hilt
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.eastway.echarts.server;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.eastway.echarts.domain.AssignmentImpl;
 import org.eastway.echarts.domain.DemographicsImpl;
 import org.eastway.echarts.domain.PatientImpl;
 import org.eastway.echarts.shared.DbException;
 import org.eastway.echarts.shared.GetPatientSummary;
 import org.eastway.echarts.shared.GetPatientSummaryResult;
 import org.eastway.echarts.shared.SessionExpiredException;
 
 import net.customware.gwt.dispatch.server.ActionHandler;
 import net.customware.gwt.dispatch.server.ExecutionContext;
 import net.customware.gwt.dispatch.shared.ActionException;
 
 public class GetPatientSummaryHandler implements ActionHandler<GetPatientSummary, GetPatientSummaryResult> {
 
 	@Override
 	public GetPatientSummaryResult execute(GetPatientSummary action, ExecutionContext context) throws ActionException {
 		ServiceUtil util = new ServiceUtil();
 		try {
 			util.checkSessionExpire(action.getSessionId());
 		} catch (SessionExpiredException e) {
 			throw new ActionException(e.getMessage());
 		} catch (DbException e) {
 			throw new ActionException("Database error");
 		}
 		EntityManager em = EchartsEntityManagerFactory.getEntityManagerFactory().createEntityManager();
 		PatientImpl patient = em.createQuery(
 				"SELECT p FROM PatientImpl p WHERE p.caseNumber = '" + action.getCaseNumber() + "'", PatientImpl.class)
 				.getSingleResult();
 		DemographicsImpl demographics = em.createQuery(
 				"SELECT d FROM DemographicsImpl d WHERE d.caseNumber = '" + action.getCaseNumber() + "'", DemographicsImpl.class)
 				.getSingleResult();
 		List<AssignmentImpl> assignments = em.createQuery(
 				"SELECT a From AssignmentImpl a Where a.disposition = 'Open' And a.service Like 'S%' And a.caseNumber = '" + action.getCaseNumber() + "' Order By a.patient.lastName ASC, a.patient.firstName ASC, a.orderDate DESC", AssignmentImpl.class)
 					.getResultList();
		String provider;
 		for (AssignmentImpl a : assignments) {
 			if (a.getStaff().matches(action.getStaffId()))
 				provider = a.getStaffName();
 		}
		if (assignments.size() > 0)
 			provider = assignments.get(0).getStaffName();
		else
 			provider = "";
 		GetPatientSummaryResult result = new GetPatientSummaryResult();
 		result.setPatient(patient.toDto());
 		result.setDemographics(demographics.toDto());
 		result.setProvider(provider);
 		em.close();
 		return result;
 	}
 
 	@Override
 	public Class<GetPatientSummary> getActionType() {
 		return GetPatientSummary.class;
 	}
 
 	@Override
 	public void rollback(GetPatientSummary action, GetPatientSummaryResult result, ExecutionContext context) throws ActionException {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
