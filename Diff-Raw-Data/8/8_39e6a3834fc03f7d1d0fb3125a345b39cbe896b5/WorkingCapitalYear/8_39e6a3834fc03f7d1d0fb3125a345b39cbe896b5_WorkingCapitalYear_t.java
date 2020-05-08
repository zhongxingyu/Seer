 /*
  * @(#)WorkingCapitalYear.java
  *
  * Copyright 2010 Instituto Superior Tecnico
  * Founding Authors: Luis Cruz
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Working Capital Module.
  *
  *   The Working Capital Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Working Capital Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Working Capital Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package module.workingCapital.domain;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import module.organization.domain.Accountability;
 import module.organization.domain.Party;
 import module.organization.domain.Person;
 import module.organization.domain.Unit;
 import module.workflow.domain.WorkflowProcess;
 
 import org.joda.time.DateTime;
 
 import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
 import pt.ist.bennu.core.domain.User;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 
 /**
  * 
  * @author João Neves
  * @author Paulo Abrantes
  * @author Luis Cruz
  * 
  */
 public class WorkingCapitalYear extends WorkingCapitalYear_Base {
 
     public WorkingCapitalYear() {
         super();
         setWorkingCapitalSystem(WorkingCapitalSystem.getInstanceForCurrentHost());
     }
 
     public WorkingCapitalYear(final Integer year) {
         this();
         setYear(year);
     }
 
     public static WorkingCapitalYear findOrCreate(final Integer year) {
         for (final WorkingCapitalYear workingCapitalYear : WorkingCapitalSystem.getInstanceForCurrentHost()
                 .getWorkingCapitalYearsSet()) {
             if (workingCapitalYear.getYear().intValue() == year.intValue()) {
                 return workingCapitalYear;
             }
         }
         return new WorkingCapitalYear(year);
     }
 
     private abstract class WorkingCapitalProcessSearch {
 
         private final SortedSet<WorkingCapitalProcess> result;
 
         abstract boolean shouldAdd(final WorkingCapitalProcess workingCapitalProcess, final User user);
 
         WorkingCapitalProcessSearch() {
             result = new TreeSet<WorkingCapitalProcess>(WorkingCapitalProcess.COMPARATOR_BY_UNIT_NAME);
         }
 
         WorkingCapitalProcessSearch(final SortedSet<WorkingCapitalProcess> result) {
             this.result = result;
         }
 
         SortedSet<WorkingCapitalProcess> search() {
             final User user = UserView.getCurrentUser();
             for (final WorkingCapital workingCapital : getWorkingCapitalsSet()) {
                 final WorkingCapitalProcess workingCapitalProcess = workingCapital.getWorkingCapitalProcess();
                 if (shouldAdd(workingCapitalProcess, user)) {
                     result.add(workingCapitalProcess);
                 }
             }
             return result;
         }
 
     }
 
     private class PendingAprovalSearch extends WorkingCapitalProcessSearch {
 
         PendingAprovalSearch() {
         }
 
         PendingAprovalSearch(final SortedSet<WorkingCapitalProcess> result) {
             super(result);
         }
 
         @Override
         boolean shouldAdd(final WorkingCapitalProcess workingCapitalProcess, final User user) {
             final WorkingCapital workingCapital = workingCapitalProcess.getWorkingCapital();
             return !workingCapital.isCanceledOrRejected()
                     && (workingCapitalProcess.isPendingDirectAproval(user)
                             || workingCapital.hasAcquisitionPendingDirectApproval(user) || workingCapital
                                 .isPendingAcceptResponsability(user));
         }
 
     }
 
     public SortedSet<WorkingCapitalProcess> getPendingAproval() {
         return new PendingAprovalSearch().search();
     }
 
     public SortedSet<WorkingCapitalProcess> getPendingAproval(final SortedSet<WorkingCapitalProcess> result) {
         return new PendingAprovalSearch(result).search();
     }
 
     private class PendingVerificationSearch extends WorkingCapitalProcessSearch {
 
         PendingVerificationSearch() {
         }
 
         PendingVerificationSearch(final SortedSet<WorkingCapitalProcess> result) {
             super(result);
         }
 
         @Override
         boolean shouldAdd(final WorkingCapitalProcess workingCapitalProcess, final User user) {
             final WorkingCapital workingCapital = workingCapitalProcess.getWorkingCapital();
             return workingCapital.isPendingFundUnAllocation(user)
                     || (!workingCapital.isCanceledOrRejected() && workingCapitalProcess.isPendingVerification(user));
         }
 
     }
 
     public SortedSet<WorkingCapitalProcess> getPendingVerification() {
         return new PendingVerificationSearch().search();
     }
 
     public SortedSet<WorkingCapitalProcess> getPendingVerification(final SortedSet<WorkingCapitalProcess> result) {
         return new PendingVerificationSearch(result).search();
     }
 
     private class PendingProcessingSearch extends WorkingCapitalProcessSearch {
 
         PendingProcessingSearch() {
         }
 
         PendingProcessingSearch(final SortedSet<WorkingCapitalProcess> result) {
             super(result);
         }
 
         @Override
         boolean shouldAdd(final WorkingCapitalProcess workingCapitalProcess, final User user) {
             final WorkingCapital workingCapital = workingCapitalProcess.getWorkingCapital();
             return workingCapital.isPendingFundUnAllocation(user)
                     || (!workingCapital.isCanceledOrRejected() && (workingCapital.isPendingFundAllocation(user)
                             || workingCapital.hasAcquisitionPendingVerification(user) || ((workingCapital
                             .isAccountingResponsible(user) || workingCapital.isAccountingEmployee(user)) && workingCapital
                             .canRequestCapitalRefund())));
         }
 
     }
 
     public SortedSet<WorkingCapitalProcess> getPendingProcessing() {
         return new PendingProcessingSearch().search();
     }
 
     public SortedSet<WorkingCapitalProcess> getPendingProcessing(final SortedSet<WorkingCapitalProcess> result) {
         return new PendingProcessingSearch(result).search();
     }
 
     public SortedSet<WorkingCapitalProcess> getPendingDirectVerification() {
         return new WorkingCapitalProcessSearch() {
             @Override
             boolean shouldAdd(final WorkingCapitalProcess workingCapitalProcess, final User user) {
                 final WorkingCapital workingCapital = workingCapitalProcess.getWorkingCapital();
                 return !workingCapital.isCanceledOrRejected()
                         && (workingCapitalProcess.isPendingVerification(user)
                                 || workingCapital.isPendingDirectFundAllocation(user)
                                 || workingCapital.hasAcquisitionPendingDirectVerification(user) || ((workingCapital
                                 .isAccountingResponsible(user) || workingCapital.isDirectAccountingEmployee(user)) && workingCapital
                                 .canRequestCapitalRefund()));
             }
         }.search();
     }
 
     private class PendingAuthorizationSearch extends WorkingCapitalProcessSearch {
 
         PendingAuthorizationSearch() {
         }
 
         PendingAuthorizationSearch(final SortedSet<WorkingCapitalProcess> result) {
             super(result);
         }
 
         @Override
         boolean shouldAdd(WorkingCapitalProcess workingCapitalProcess, User user) {
             if (workingCapitalProcess.isPendingAuthorization(user)) {
                 return true;
             }
 
             for (WorkingCapitalAcquisition transaction : workingCapitalProcess.getWorkingCapital()
                     .getWorkingCapitalAcquisitionsSet()) {
                if (transaction.getWorkingCapitalAcquisitionTransaction().isExceptionalAcquisition()
                        && ((ExceptionalWorkingCapitalAcquisitionTransaction) transaction
                                .getWorkingCapitalAcquisitionTransaction()).isPendingManagementApprovalByUser(user)) {
                    return true;

                 }
             }
             return false;
 
         }
     }
 
     public SortedSet<WorkingCapitalProcess> getPendingAuthorization() {
         return new PendingAuthorizationSearch().search();
     }
 
     public SortedSet<WorkingCapitalProcess> getPendingAuthorization(final SortedSet<WorkingCapitalProcess> result) {
         return new PendingAuthorizationSearch(result).search();
     }
 
     private class PendingPaymentSearch extends WorkingCapitalProcessSearch {
 
         PendingPaymentSearch() {
         }
 
         PendingPaymentSearch(final SortedSet<WorkingCapitalProcess> result) {
             super(result);
         }
 
         @Override
         boolean shouldAdd(final WorkingCapitalProcess workingCapitalProcess, final User user) {
             final WorkingCapital workingCapital = workingCapitalProcess.getWorkingCapital();
             return !workingCapital.isCanceledOrRejected() && (/* (workingCapital.isAccountingResponsible(user) && workingCapital.canRequestCapital()) || */
             (workingCapital.isTreasuryMember(user) && workingCapital.hasWorkingCapitalRequestPendingTreasuryProcessing()));
         }
 
     }
 
     public SortedSet<WorkingCapitalProcess> getPendingPayment() {
         return new PendingPaymentSearch().search();
     }
 
     public SortedSet<WorkingCapitalProcess> getPendingPayment(final SortedSet<WorkingCapitalProcess> result) {
         return new PendingPaymentSearch(result).search();
     }
 
     public SortedSet<WorkingCapitalProcess> getMyWorkingCapital() {
         final SortedSet<WorkingCapitalProcess> result =
                 new TreeSet<WorkingCapitalProcess>(WorkingCapitalProcess.COMPARATOR_BY_UNIT_NAME);
         final User user = UserView.getCurrentUser();
         final Person person = user.getPerson();
         if (person != null) {
             for (final WorkingCapital workingCapital : person.getMovementResponsibleWorkingCapitalsSet()) {
                 if (workingCapital.getWorkingCapitalYear() == this) {
                     final WorkingCapitalProcess workingCapitalProcess = workingCapital.getWorkingCapitalProcess();
                     result.add(workingCapitalProcess);
                 }
             }
         }
         return result;
     }
 
     public SortedSet<WorkingCapitalProcess> getRequestedWorkingCapital() {
         final SortedSet<WorkingCapitalProcess> result =
                 new TreeSet<WorkingCapitalProcess>(WorkingCapitalProcess.COMPARATOR_BY_UNIT_NAME);
         final User user = UserView.getCurrentUser();
         final Person person = user.getPerson();
         if (person != null) {
             for (final WorkingCapitalInitialization workingCapitalInitialization : person
                     .getRequestedWorkingCapitalInitializationsSet()) {
                 final WorkingCapital workingCapital = workingCapitalInitialization.getWorkingCapital();
                 if (workingCapital.getWorkingCapitalYear() == this) {
                     final WorkingCapitalProcess workingCapitalProcess = workingCapital.getWorkingCapitalProcess();
                     result.add(workingCapitalProcess);
                 }
             }
         }
         return result;
     }
 
     public SortedSet<WorkingCapitalProcess> getAprovalResponsibleWorkingCapital() {
         final SortedSet<WorkingCapitalProcess> result =
                 new TreeSet<WorkingCapitalProcess>(WorkingCapitalProcess.COMPARATOR_BY_UNIT_NAME);
         final User user = UserView.getCurrentUser();
         if (user.getExpenditurePerson() != null) {
             final Set<Authorization> authorizations = getAuthorizations(user);
             if (!authorizations.isEmpty()) {
                 for (final WorkingCapital workingCapital : getWorkingCapitalsSet()) {
                     final pt.ist.expenditureTrackingSystem.domain.organization.Unit unit = workingCapital.getUnit();
                     if (isDirectlyResponsibleFor(authorizations, unit)) {
                         result.add(workingCapital.getWorkingCapitalProcess());
                     }
                 }
             }
             //for (final Authorization authorization : user.getExpenditurePerson().getAuthorizationsSet()) {
             //	if (authorization.isValid()) {
             //	    final pt.ist.expenditureTrackingSystem.domain.organization.Unit unit = authorization.getUnit();
             //	    for (final WorkingCapital workingCapital : unit.getWorkingCapitalsSet()) {
             //		if (workingCapital.getWorkingCapitalYear() == this) {
             //		    final WorkingCapitalProcess workingCapitalProcess = workingCapital.getWorkingCapitalProcess();
             //		    result.add(workingCapitalProcess);
             //		}
             //	    }
             //	    addSubUnitWorkingCapitals(result, unit.getUnit());
             //	}
             //}
         }
         return result;
     }
 
     private boolean isDirectlyResponsibleFor(final Set<Authorization> authorizations,
             final pt.ist.expenditureTrackingSystem.domain.organization.Unit unit) {
         final Set<Authorization> authorizationsFromUnit = unit.getAuthorizationsSet();
         if (intersect(authorizations, authorizationsFromUnit)) {
             return true;
         }
         if (hasValidAuthorization(authorizationsFromUnit)) {
             return false;
         }
         final pt.ist.expenditureTrackingSystem.domain.organization.Unit parentUnit = unit.getParentUnit();
         return parentUnit != null && isDirectlyResponsibleFor(authorizations, parentUnit);
     }
 
     private boolean hasValidAuthorization(final Set<Authorization> authorizations) {
         for (final Authorization authorization : authorizations) {
             if (authorization.isValid()) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean intersect(final Set<Authorization> authorizations, final Set<Authorization> authorizationsFromUnit) {
         for (final Authorization authorization : authorizationsFromUnit) {
             if (authorizations.contains(authorization)) {
                 return true;
             }
         }
         return false;
     }
 
     private Set<Authorization> getAuthorizations(final User user) {
         final Set<Authorization> authorizations = new HashSet<Authorization>();
         for (final Authorization authorization : user.getExpenditurePerson().getAuthorizationsSet()) {
             if (authorization.isValid()) {
                 authorizations.add(authorization);
             }
         }
         return authorizations;
     }
 
     private void addSubUnitWorkingCapitals(final SortedSet<WorkingCapitalProcess> result, final Unit unit) {
         for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
             final Party child = accountability.getChild();
             if (child.isUnit()) {
                 final Unit childUnit = (Unit) child;
                 if (childUnit.getExpenditureUnit() != null) {
                     final pt.ist.expenditureTrackingSystem.domain.organization.Unit expenditureUnit =
                             childUnit.getExpenditureUnit();
                     if (!hasValidAuthorization(expenditureUnit)) {
                         for (WorkingCapital workingCapital : expenditureUnit.getWorkingCapitalsSet()) {
                             if (workingCapital.getWorkingCapitalYear() == this) {
                                 result.add(workingCapital.getWorkingCapitalProcess());
                             }
                         }
                     }
                 }
                 addSubUnitWorkingCapitals(result, childUnit);
             }
         }
     }
 
     private boolean hasValidAuthorization(final pt.ist.expenditureTrackingSystem.domain.organization.Unit expenditureUnit) {
         for (final Authorization authorization : expenditureUnit.getAuthorizationsSet()) {
             if (authorization.isValid()) {
                 return true;
             }
         }
         return false;
     }
 
     public SortedSet<WorkingCapitalProcess> getForUnit(final Unit unit) {
         final User user = UserView.getCurrentUser();
         final SortedSet<WorkingCapitalProcess> result =
                 new TreeSet<WorkingCapitalProcess>(WorkingCapitalProcess.COMPARATOR_BY_UNIT_NAME);
         if (unit.getExpenditureUnit() != null) {
             for (final WorkingCapital workingCapital : unit.getExpenditureUnit().getWorkingCapitalsSet()) {
                 if (workingCapital.getWorkingCapitalYear() == this /* && workingCapital.isAvailable(user) */) {
                     final WorkingCapitalProcess workingCapitalProcess = workingCapital.getWorkingCapitalProcess();
                     if (workingCapitalProcess.isAccessibleToCurrentUser()) {
                         result.add(workingCapitalProcess);
                     }
                 }
             }
         }
         return result;
     }
 
     public SortedSet<WorkingCapitalProcess> getForPerson(final Person person) {
         final SortedSet<WorkingCapitalProcess> result =
                 new TreeSet<WorkingCapitalProcess>(WorkingCapitalProcess.COMPARATOR_BY_UNIT_NAME);
         if (person != null) {
             for (final WorkingCapital workingCapital : person.getMovementResponsibleWorkingCapitalsSet()) {
                 if (workingCapital.getWorkingCapitalYear() == this) {
                     final WorkingCapitalProcess workingCapitalProcess = workingCapital.getWorkingCapitalProcess();
                     if (workingCapitalProcess.isAccessibleToCurrentUser()) {
                         result.add(workingCapitalProcess);
                     }
                 }
             }
         }
         return result;
     }
 
     public SortedSet<WorkingCapitalProcess> getForParty(final Party party) {
         return party.isUnit() ? getForUnit((Unit) party) : getForPerson((Person) party);
     }
 
     public static WorkingCapitalYear getCurrentYear() {
         final int year = new DateTime().getYear();
         return findOrCreate(year);
     }
 
     public SortedSet<WorkingCapitalProcess> getTaken() {
         final SortedSet<WorkingCapitalProcess> result =
                 new TreeSet<WorkingCapitalProcess>(WorkingCapitalProcess.COMPARATOR_BY_UNIT_NAME);
         return getTaken(result);
     }
 
     public SortedSet<WorkingCapitalProcess> getTaken(final SortedSet<WorkingCapitalProcess> result) {
         final User user = UserView.getCurrentUser();
         for (final WorkflowProcess workflowProcess : user.getUserProcessesSet()) {
             if (workflowProcess instanceof WorkingCapitalProcess) {
                 final WorkingCapitalProcess workingCapitalProcess = (WorkingCapitalProcess) workflowProcess;
                 if (workingCapitalProcess.getWorkingCapital().getWorkingCapitalYear() == this) {
                     result.add(workingCapitalProcess);
                 }
             }
         }
         return result;
     }
 
     @Override
     public boolean isConnectedToCurrentHost() {
         return getWorkingCapitalSystem() == WorkingCapitalSystem.getInstanceForCurrentHost();
     }
 
     @Deprecated
     public boolean hasYear() {
         return getYear() != null;
     }
 
     @Deprecated
     public boolean hasWorkingCapitalSystem() {
         return getWorkingCapitalSystem() != null;
     }
 
     @Deprecated
     public java.util.Set<module.workingCapital.domain.WorkingCapital> getWorkingCapitals() {
         return getWorkingCapitalsSet();
     }
 
     @Deprecated
     public boolean hasAnyWorkingCapitals() {
         return !getWorkingCapitalsSet().isEmpty();
     }
 
 }
