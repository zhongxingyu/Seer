 /*
  * @(#)ProjectFinancer.java
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
 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 
 import pt.ist.bennu.core.domain.exceptions.DomainException;
 import pt.ist.bennu.core.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Project;
 import pt.ist.expenditureTrackingSystem.domain.organization.SubProject;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.utl.ist.fenix.tools.util.Strings;
 
 /**
  * 
  * @author João Neves
  * @author Paulo Abrantes
  * @author Luis Cruz
  * 
  */
 public class ProjectFinancer extends ProjectFinancer_Base {
 
     protected ProjectFinancer() {
         super();
     }
 
     protected ProjectFinancer(final RequestWithPayment acquisitionRequest, final Unit unit) {
         this();
         if (acquisitionRequest == null || unit == null) {
             throw new DomainException("error.financer.wrong.initial.arguments");
         }
         if (acquisitionRequest.hasPayingUnit(unit)) {
             throw new DomainException("error.financer.acquisition.request.already.has.paying.unit");
         }
 
         setFundedRequest(acquisitionRequest);
         setUnit(unit);
         setAccountingUnit(unit.getAccountingUnit());
     }
 
     public ProjectFinancer(final RequestWithPayment acquisitionRequest, final Project project) {
         this(acquisitionRequest, (Unit) project);
     }
 
     public ProjectFinancer(final RequestWithPayment acquisitionRequest, final SubProject subProject) {
         this(acquisitionRequest, (Unit) subProject);
     }
 
     @Override
     public String getFundAllocationIds() {
         final String financerString = super.getFundAllocationIds();
         return financerString + " " + getAllocationIds(getProjectFundAllocationId(), "financer.label.allocation.id.prefix.mgp");
     }
 
     @Override
     public String getEffectiveFundAllocationIds() {
         final StringBuilder financerString = new StringBuilder(super.getEffectiveFundAllocationIds());
         Strings strings = getEffectiveProjectFundAllocationId();
         if (strings != null && !strings.isEmpty()) {
             for (String allocationId : strings.getUnmodifiableList()) {
                 financerString.append(getAllocationIds(allocationId, "financer.label.allocation.id.prefix.mgp"));
                 financerString.append(' ');
             }
         }
         return financerString.toString();
     }
 
     @Override
     public boolean hasAllocatedFundsForAllProject() {
         return (getProjectFundAllocationId() != null && !getProjectFundAllocationId().isEmpty())
                 || (getAmountAllocated().equals(Money.ZERO) && hasAnyOtherFinancerIsAllocated());
     }
 
     private boolean hasAnyOtherFinancerIsAllocated() {
         for (Financer financer : getFundedRequest().getFinancers()) {
             if (financer != this && financer.isProjectFinancer()) {
                 ProjectFinancer projectFinancer = (ProjectFinancer) financer;
                 if ((projectFinancer.getProjectFundAllocationId() != null && !projectFinancer.getProjectFundAllocationId()
                         .isEmpty())) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     @Override
     public boolean hasAllocatedFundsPermanentlyForAllProjectFinancers() {
         Collection<PaymentProcessInvoice> allocatedInvoicesInProject = getAllocatedInvoicesInProject();
         for (UnitItem unitItem : getUnitItems()) {
             if (!allocatedInvoicesInProject.containsAll(unitItem.getConfirmedInvoices())) {
                 return false;
             }
         }
         return getEffectiveProjectFundAllocationId() != null && !getEffectiveProjectFundAllocationId().isEmpty();
     }
 
     @Override
     public boolean hasAllocatedFundsPermanentlyForAnyProjectFinancers() {
         Collection<PaymentProcessInvoice> allocatedInvoicesInProject = getAllocatedInvoicesInProject();
         for (UnitItem unitItem : getUnitItems()) {
             if (!allocatedInvoicesInProject.isEmpty() && allocatedInvoicesInProject.containsAll(unitItem.getConfirmedInvoices())) {
                 return true;
             }
         }
         return getEffectiveProjectFundAllocationId() != null && !getEffectiveProjectFundAllocationId().isEmpty();
     }
 
     @Override
     public boolean isProjectFinancer() {
         return true;
     }
 
     public void addEffectiveProjectFundAllocationId(String effectiveProjectFundAllocationId) {
         if (StringUtils.isEmpty(effectiveProjectFundAllocationId)) {
             throw new DomainException("acquisitionProcess.message.exception.effectiveFundAllocationCannotBeNull");
         }
         Strings strings = getEffectiveProjectFundAllocationId();
         if (strings == null) {
             strings = new Strings(effectiveProjectFundAllocationId);
         }
         if (!strings.contains(effectiveProjectFundAllocationId)) {
             strings = new Strings(strings, effectiveProjectFundAllocationId);
         }
         setEffectiveProjectFundAllocationId(strings);
 
         allocateInvoicesInProject();
     }
 
     private void allocateInvoicesInProject() {
         getAllocatedInvoicesInProject().clear();
         Set<PaymentProcessInvoice> invoices = new HashSet<PaymentProcessInvoice>();
         for (UnitItem unitItem : getUnitItems()) {
             invoices.addAll(unitItem.getConfirmedInvoices());
         }
         getAllocatedInvoicesInProject().addAll(invoices);
     }
 
     @Override
     public boolean isAccountingEmployee(Person person) {
         return getUnit().isAccountingEmployee(person);
     }
 
     @Override
     public boolean isAccountingEmployeeForOnePossibleUnit(Person person) {
         return false;
     }
 
     @Override
     public boolean hasFundAllocationId() {
         return super.hasFundAllocationId() || getProjectFundAllocationId() != null;
     }
 
     public Set<AccountingUnit> getAccountingUnits() {
         Set<AccountingUnit> res = new HashSet<AccountingUnit>();
        res.add(getUnit().getAccountingUnit());
         final AccountingUnit accountingUnit = AccountingUnit.readAccountingUnitByUnitName("10");
         if (accountingUnit != null) {
             res.add(accountingUnit);
         }
         return res;
     }
 
     @Override
     public boolean isProjectAccountingEmployeeForOnePossibleUnit(final Person person) {
         for (final AccountingUnit accountingUnit : getAccountingUnits()) {
             if (accountingUnit.getPeopleSet().contains(person)) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public boolean isProjectAccountingEmployee(Person person) {
         final AccountingUnit accountingUnit = getAccountingUnit();
         return accountingUnit == null ? false : accountingUnit.getProjectAccountantsSet().contains(person);
     }
 
     @Override
     public boolean isFundAllocationPresent() {
         return getProjectFundAllocationId() != null || super.isFundAllocationPresent();
     }
 
     @Override
     public boolean isEffectiveFundAllocationPresent() {
         return getEffectiveProjectFundAllocationId() != null || super.isEffectiveFundAllocationPresent();
     }
 
     public void resetEffectiveFundAllocation() {
         setEffectiveProjectFundAllocationId(null);
         getAllocatedInvoicesInProject().clear();
     }
 
     @Override
     public boolean hasAllInvoicesAllocatedInProject() {
         Collection<PaymentProcessInvoice> allocatedInvoices = getAllocatedInvoicesInProject();
         for (UnitItem unitItem : getUnitItems()) {
             if (!allocatedInvoices.containsAll(unitItem.getConfirmedInvoices())) {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public void createFundAllocationRequest(final boolean isFinalFundAllocation) {
         for (final UnitItem unitItem : getUnitItemsSet()) {
             final RequestWithPayment fundedRequest = getFundedRequest();
             final PaymentProcess process = fundedRequest.getProcess();
             final Unit unit = getUnit();
             final AccountingUnit accountingUnit = unit.getAccountingUnit();
 
             new ProjectAcquisitionFundAllocationRequest(unitItem, process.getProcessNumber(), process, unit.getUnitNumber(),
                     accountingUnit.getName(), getAmountAllocated(), Boolean.valueOf(isFinalFundAllocation));
         }
     }
 
     public String getProjectFundAllocationIdsForAllUnitItems() {
         final StringBuilder result = new StringBuilder();
         for (final UnitItem unitItem : getUnitItemsSet()) {
             final String projectFundAllocationId = unitItem.getProjectFundAllocationId();
             if (projectFundAllocationId == null) {
                 return null;
             }
             result.append(", ");
             result.append(projectFundAllocationId);
         }
         return result.toString();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessInvoice> getAllocatedInvoicesInProject() {
         return getAllocatedInvoicesInProjectSet();
     }
 
     @Deprecated
     public boolean hasAnyAllocatedInvoicesInProject() {
         return !getAllocatedInvoicesInProjectSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasProjectFundAllocationId() {
         return getProjectFundAllocationId() != null;
     }
 
     @Deprecated
     public boolean hasEffectiveProjectFundAllocationId() {
         return getEffectiveProjectFundAllocationId() != null;
     }
 
 }
