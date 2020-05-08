 /*
  * @(#)SubmitForApproval.java
  *
  * Copyright 2009 Instituto Superior Tecnico
  * Founding Authors: Luis Cruz, Nuno Ochoa, Paulo Abrantes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Expenditure Tracking Module.
  *
  *   The Expenditure Tracking Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Expenditure Tracking Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Expenditure Tracking Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.domain.User;
 import myorg.domain.exceptions.DomainException;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess.ProcessClassification;
 
 /**
  * 
  * @author Paulo Abrantes
  * @author Luis Cruz
  * 
  */
 public class SubmitForApproval extends
 	WorkflowActivity<RegularAcquisitionProcess, ActivityInformation<RegularAcquisitionProcess>> {
 
     @Override
     public boolean isActive(RegularAcquisitionProcess process, User user) {
 	return user.getExpenditurePerson() == process.getRequestor()
 		&& isUserProcessOwner(process, user)
 		&& process.getAcquisitionProcessState().isInGenesis()
 		&& process.getAcquisitionRequest().isFilled()
 		&& process.getAcquisitionRequest().isEveryItemFullyAttributedToPayingUnits();
     }
 
     @Override
     protected void process(ActivityInformation<RegularAcquisitionProcess> activityInformation) {
 	final RegularAcquisitionProcess process = activityInformation.getProcess();
 	if (process.isSimplifiedProcedureProcess()
		//&& ((SimplifiedProcedureProcess) process).getProcessClassification() != ProcessClassification.CT75000
		//&& !process.hasAcquisitionProposalDocument()
 		&& ((SimplifiedProcedureProcess) process).hasInvoiceFile()
 		&& process.getTotalValue().isGreaterThan(ExpenditureTrackingSystem.getInstance().getMaxValueStartedWithInvoive())
 		) {
 	    final String message = BundleUtil.getStringFromResourceBundle(getUsedBundle(),
 		    "activities.message.exception.exceeded.limit.to.start.process.with.invoice");
 	    throw new DomainException(message);
 	}
 
 	process.submitForApproval();
     }
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle(getUsedBundle(), "label." + getClass().getName());
     }
 
     @Override
     public String getUsedBundle() {
 	return "resources/AcquisitionResources";
     }
 }
