 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.process;
 
 
 
 import com.sapienter.jbilling.server.process.db.BillingProcessConfigurationDAS;
 import com.sapienter.jbilling.server.process.db.BillingProcessConfigurationDTO;
 import com.sapienter.jbilling.server.user.db.CompanyDAS;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.audit.EventLogger;
 
 public class ConfigurationBL {
 	private BillingProcessConfigurationDAS configurationDas = null;
 	private BillingProcessConfigurationDTO configuration = null;
 	private EventLogger eLogger = null;
 
 	public ConfigurationBL(Integer entityId)  {
 		init();
         configuration = configurationDas.findByEntity(new CompanyDAS().find(entityId));
 	}
 
 	public ConfigurationBL() {
 		init();
 	}
 
 	public ConfigurationBL(BillingProcessConfigurationDTO cfg) {
 		init();
 		configuration = cfg;
 	}
 
 	private void init() {
 		eLogger = EventLogger.getInstance();
 		configurationDas = new BillingProcessConfigurationDAS();
 
 	}
 
 	public BillingProcessConfigurationDTO getEntity() {
 		return configuration;
 	}
 
	public void set(Integer id) {
		configuration = configurationDas.find(id);
 	}
 
 	public Integer createUpdate(Integer executorId,
 			BillingProcessConfigurationDTO dto) {
 		configuration = configurationDas.findByEntity(dto.getEntity());
 		if (configuration != null) {
 
 			if (!configuration.getGenerateReport().equals(
 					dto.getGenerateReport())) {
 				eLogger.audit(executorId,
 						Constants.TABLE_BILLING_PROCESS_CONFIGURATION,
 						configuration.getId(),
 						EventLogger.MODULE_BILLING_PROCESS,
 						EventLogger.ROW_UPDATED, new Integer(configuration
 								.getGenerateReport()), null, null);
 				configuration.setGenerateReport(dto.getGenerateReport());
 				configuration
 						.setReviewStatus(dto.getGenerateReport() == 1 ? Constants.REVIEW_STATUS_GENERATED
 								: Constants.REVIEW_STATUS_APPROVED);
 			} else {
 				eLogger.audit(executorId,
 						Constants.TABLE_BILLING_PROCESS_CONFIGURATION,
 						configuration.getId(),
 						EventLogger.MODULE_BILLING_PROCESS,
 						EventLogger.ROW_UPDATED, null, null, null);
 			}
 
 			configuration.setNextRunDate(dto.getNextRunDate());
 		} else {
 			configuration = configurationDas.create(dto.getEntity(), dto
 					.getNextRunDate(), dto.getGenerateReport());
 		}
 
 		configuration.setDaysForReport(dto.getDaysForReport());
 		configuration.setDaysForRetry(dto.getDaysForRetry());
 		configuration.setRetries(dto.getRetries());
 		configuration.setPeriodUnit(dto.getPeriodUnit());
 		configuration.setPeriodValue(dto.getPeriodValue());
 		configuration.setDueDateUnitId(dto.getDueDateUnitId());
 		configuration.setDueDateValue(dto.getDueDateValue());
 		configuration.setDfFm(dto.getDfFm());
 		configuration.setOnlyRecurring(dto.getOnlyRecurring());
 		configuration.setInvoiceDateProcess(dto.getInvoiceDateProcess());
 		configuration.setAutoPayment(dto.getAutoPayment());
 		configuration
 				.setAutoPaymentApplication(dto.getAutoPaymentApplication());
 		configuration.setMaximumPeriods(dto.getMaximumPeriods());
 
 		return configuration.getId();
 	}
 
 	public BillingProcessConfigurationDTO getDTO() {
 		BillingProcessConfigurationDTO dto = new BillingProcessConfigurationDTO();
 
 		dto.setDaysForReport(configuration.getDaysForReport());
 		dto.setDaysForRetry(configuration.getDaysForRetry());
 		dto.setEntity(configuration.getEntity());
 		dto.setGenerateReport(configuration.getGenerateReport());
 		dto.setId(configuration.getId());
 		dto.setNextRunDate(configuration.getNextRunDate());
 		dto.setRetries(configuration.getRetries());
 		dto.setPeriodUnit(configuration.getPeriodUnit());
 		dto.setPeriodValue(configuration.getPeriodValue());
 		dto.setReviewStatus(configuration.getReviewStatus());
 		dto.setDueDateUnitId(configuration.getDueDateUnitId());
 		dto.setDueDateValue(configuration.getDueDateValue());
 		dto.setDfFm(configuration.getDfFm());
 		dto.setOnlyRecurring(configuration.getOnlyRecurring());
 		dto.setInvoiceDateProcess(configuration.getInvoiceDateProcess());
 		dto.setAutoPayment(configuration.getAutoPayment());
 		dto.setMaximumPeriods(configuration.getMaximumPeriods());
 		dto
 				.setAutoPaymentApplication(configuration
 						.getAutoPaymentApplication());
 
 		return dto;
 	}
 
 	public void setReviewApproval(Integer executorId, boolean flag) {
 
 		eLogger.audit(executorId,
 				Constants.TABLE_BILLING_PROCESS_CONFIGURATION, configuration
 						.getId(), EventLogger.MODULE_BILLING_PROCESS,
 				EventLogger.ROW_UPDATED, configuration.getReviewStatus(), null,
 				null);
 		configuration.setReviewStatus(flag ? Constants.REVIEW_STATUS_APPROVED
 				: Constants.REVIEW_STATUS_DISAPPROVED);
 
 	}
 
 }
