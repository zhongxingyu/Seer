 /*
  * @(#)ConfirmInvoices.java
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
 package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import pt.ist.bennu.core.domain.User;
 import pt.ist.bennu.core.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 /**
  * 
  * @author Paulo Abrantes
  * 
  */
 public class ConfirmInvoices extends WorkflowActivity<RefundProcess, ActivityInformation<RefundProcess>> {
 
     @Override
     public boolean isActive(RefundProcess process, User user) {
         Person person = user.getExpenditurePerson();
         return isUserProcessOwner(process, user)
                 && process.isRealValueFullyAttributedToUnits()
                 && ((process.isAccountingEmployee(person) && !process.hasProjectsAsPayingUnits()) || (process
                         .isProjectAccountingEmployee(person) && process.hasProjectsAsPayingUnits()))
                 && process.isPendingInvoicesConfirmation();
     }
 
     @Override
     protected void process(ActivityInformation<RefundProcess> activityInformation) {
         activityInformation.getProcess().confirmInvoicesByPerson(Person.getLoggedPerson());
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
