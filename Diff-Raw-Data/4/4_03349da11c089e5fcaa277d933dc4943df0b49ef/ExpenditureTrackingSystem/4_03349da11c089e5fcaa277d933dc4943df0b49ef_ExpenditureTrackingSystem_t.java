 /*
  * @(#)ExpenditureTrackingSystem.java
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
 package pt.ist.expenditureTrackingSystem.domain;
 
import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.servlet.http.HttpServletRequest;
 
 import module.dashBoard.WidgetRegister;
 import module.dashBoard.WidgetRegister.WidgetAditionPredicate;
 import module.dashBoard.domain.DashBoardPanel;
 import module.dashBoard.widgets.WidgetController;
 import module.organization.presentationTier.actions.OrganizationModelAction;
 import module.workflow.widgets.ProcessListWidget;
 import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
 import pt.ist.bennu.core.domain.ModuleInitializer;
 import pt.ist.bennu.core.domain.MyOrg;
 import pt.ist.bennu.core.domain.User;
 import pt.ist.bennu.core.domain.VirtualHost;
 import pt.ist.bennu.core.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessYear;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.SearchProcessValues;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.SearchProcessValuesArray;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess.ProcessClassification;
 import pt.ist.expenditureTrackingSystem.presentationTier.actions.organization.OrganizationModelPlugin.ExpendituresView;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.ActivateEmailNotificationWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.MyProcessesWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.MySearchesWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.MyUnitsWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.PendingRefundWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.PendingSimplifiedWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.PrioritiesWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.SearchByInvoiceWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.TakenProcessesWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.UnreadCommentsWidget;
 import pt.ist.expenditureTrackingSystem.util.AquisitionsPendingProcessCounter;
 import pt.ist.expenditureTrackingSystem.util.RefundPendingProcessCounter;
 import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.RequestChecksumFilter;
 import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.RequestChecksumFilter.ChecksumPredicate;
 import pt.ist.fenixframework.Atomic;
 import pt.ist.fenixframework.dml.runtime.RelationAdapter;
 
 /**
  * 
  * @author Diogo Figueiredo
  * @author Pedro Santos
  * @author João Neves
  * @author Bruno Santos
  * @author Paulo Abrantes
  * @author Luis Cruz
  * @author Pedro Amaral
  * 
  */
 public class ExpenditureTrackingSystem extends ExpenditureTrackingSystem_Base implements ModuleInitializer {
 
     public static class VirtualHostMyOrgRelationListener extends RelationAdapter<VirtualHost, MyOrg> {
 
         @Override
         public void beforeRemove(VirtualHost vh, MyOrg myorg) {
             vh.setExpenditureTrackingSystem(null);
             super.beforeRemove(vh, myorg);
         }
     }
 
     public static WidgetAditionPredicate EXPENDITURE_TRACKING_PANEL_PREDICATE = new WidgetAditionPredicate() {
         @Override
         public boolean canBeAdded(DashBoardPanel panel, User userAdding) {
             return (ExpenditureUserDashBoardPanel.class.isAssignableFrom(panel.getClass()));
         }
     };
 
     public static WidgetAditionPredicate EXPENDITURE_SERVICES_ONLY_PREDICATE = new WidgetAditionPredicate() {
 
         @Override
         public boolean canBeAdded(DashBoardPanel panel, User userAdding) {
             return EXPENDITURE_TRACKING_PANEL_PREDICATE.canBeAdded(panel, userAdding)
                     && (isAcquisitionCentralGroupMember(userAdding)
                             || !userAdding.getExpenditurePerson().getAccountingUnits().isEmpty() || !userAdding
                             .getExpenditurePerson().getProjectAccountingUnits().isEmpty());
         }
     };
 
     static {
         VirtualHost.getRelationMyOrgVirtualHost().addListener(new VirtualHostMyOrgRelationListener());
 
         ProcessListWidget.register(new AquisitionsPendingProcessCounter());
         ProcessListWidget.register(new RefundPendingProcessCounter());
 
         registerWidget(MyUnitsWidget.class);
         registerWidget(MySearchesWidget.class);
         registerWidget(UnreadCommentsWidget.class);
         registerWidget(TakenProcessesWidget.class);
         registerWidget(MyProcessesWidget.class);
         registerWidget(PendingRefundWidget.class);
         registerWidget(PendingSimplifiedWidget.class);
         registerWidget(ActivateEmailNotificationWidget.class);
         registerWidget(SearchByInvoiceWidget.class);
         WidgetRegister.registerWidget(PrioritiesWidget.class, EXPENDITURE_SERVICES_ONLY_PREDICATE);
 
         registerChecksumFilterException();
         OrganizationModelAction.partyViewHookManager.register(new ExpendituresView());
     }
 
     private static boolean isInitialized = false;
 
     public static ExpenditureTrackingSystem getInstance() {
         if (!isInitialized) {
             if (initialize()) {
                 callInitScripts();
             }
         }
         final VirtualHost virtualHostForThread = VirtualHost.getVirtualHostForThread();
         return virtualHostForThread == null ? null : virtualHostForThread.getExpenditureTrackingSystem();
     }
 
     private static synchronized boolean initialize() {
         if (!isInitialized) {
             isInitialized = true;
             return true;
         }
         return false;
     }
 
     private static void callInitScripts() {
         migrateProcessNumbers();
         migrateSuppliers();
         migrateCPVs();
         migratePeople();
         checkISTOptions();
     }
 
     @Atomic
     private static Boolean checkISTOptions() {
         final MyOrg myOrg = MyOrg.getInstance();
         for (final VirtualHost virtualHost : myOrg.getVirtualHostsSet()) {
             final ExpenditureTrackingSystem ets = virtualHost.getExpenditureTrackingSystem();
             if (ets != null) {
                 if (virtualHost.getHostname().equals("dot.ist.utl.pt") || virtualHost.getHostname().equals("compras.ist.utl.pt")) {
                     ets.setRequireFundAllocationPriorToAcquisitionRequest(Boolean.TRUE);
                     ets.setRegisterDiaryNumbersAndTransactionNumbers(Boolean.FALSE);
                     ets.setRequireCommitmentNumber(Boolean.TRUE);
                 } else {
                     ets.setRegisterDiaryNumbersAndTransactionNumbers(Boolean.TRUE);
                     ets.setRequireCommitmentNumber(Boolean.FALSE);
                 }
             }
         }
         return false;
     }
 
     @Atomic
     private static Boolean migratePeople() {
         final MyOrg myOrg = MyOrg.getInstance();
         if (myOrg.getPeopleFromExpenditureTackingSystemSet().isEmpty()) {
             final long start = System.currentTimeMillis();
             System.out.println("Migrating people..");
             for (final VirtualHost virtualHost : myOrg.getVirtualHostsSet()) {
                 final ExpenditureTrackingSystem ets = virtualHost.getExpenditureTrackingSystem();
                 if (ets != null) {
                     myOrg.getPeopleFromExpenditureTackingSystemSet().addAll(ets.getPeopleSet());
                 }
             }
             final long end = System.currentTimeMillis();
             System.out.println("Completed migration in: " + (end - start) + "ms.");
         }
         return Boolean.TRUE;
     }
 
     @Atomic
     private static Boolean migrateCPVs() {
         final MyOrg myOrg = MyOrg.getInstance();
         if (myOrg.getCPVReferencesSet().isEmpty()) {
             final long start = System.currentTimeMillis();
             System.out.println("Migrating cpv references..");
             for (final VirtualHost virtualHost : myOrg.getVirtualHostsSet()) {
                 final ExpenditureTrackingSystem ets = virtualHost.getExpenditureTrackingSystem();
                 if (ets != null) {
                     myOrg.getCPVReferencesSet().addAll(ets.getCPVReferencesSet());
                 }
             }
             final long end = System.currentTimeMillis();
             System.out.println("Completed migration in: " + (end - start) + "ms.");
         }
         return Boolean.TRUE;
     }
 
     @Atomic
     private static Boolean migrateSuppliers() {
         final MyOrg myOrg = MyOrg.getInstance();
         if (myOrg.getSuppliersSet().isEmpty()) {
             final long start = System.currentTimeMillis();
             System.out.println("Migrating suppliers.");
             for (final VirtualHost virtualHost : myOrg.getVirtualHostsSet()) {
                 final ExpenditureTrackingSystem ets = virtualHost.getExpenditureTrackingSystem();
                 if (ets != null) {
                     myOrg.getSuppliersSet().addAll(ets.getSuppliersSet());
                 }
             }
             final long end = System.currentTimeMillis();
             System.out.println("Completed migration in: " + (end - start) + "ms.");
         }
         return Boolean.TRUE;
     }
 
     @Atomic
     private static Boolean migrateProcessNumbers() {
         final VirtualHost virtualHostForThread = VirtualHost.getVirtualHostForThread();
         if (virtualHostForThread == null) {
             return Boolean.FALSE;
         }
         final ExpenditureTrackingSystem expenditureTrackingSystem = virtualHostForThread.getExpenditureTrackingSystem();
         if (expenditureTrackingSystem == null) {
             return Boolean.FALSE;
         }
         final String prefix = expenditureTrackingSystem.getInstitutionalProcessNumberPrefix();
         if (prefix != null && !prefix.isEmpty()) {
             final long start = System.currentTimeMillis();
             System.out.println("Migrating acquisition process numbers.");
             for (final PaymentProcessYear paymentProcessYear : expenditureTrackingSystem.getPaymentProcessYearsSet()) {
                 for (final PaymentProcess paymentProcess : paymentProcessYear.getPaymentProcessSet()) {
                     paymentProcess.migrateProcessNumber();
                 }
             }
             final long end = System.currentTimeMillis();
             System.out.println("Completed migration in: " + (end - start) + "ms.");
         }
         return Boolean.TRUE;
     }
 
     private static void registerChecksumFilterException() {
         RequestChecksumFilter.registerFilterRule(new ChecksumPredicate() {
 
             @Override
             public boolean shouldFilter(HttpServletRequest request) {
                 return !(request.getQueryString() != null && request.getQueryString().contains(
                         "method=calculateShareValuesViaAjax"));
             }
 
         });
 
         RequestChecksumFilter.registerFilterRule(new ChecksumPredicate() {
             @Override
             public boolean shouldFilter(HttpServletRequest httpServletRequest) {
                 return !(httpServletRequest.getRequestURI().endsWith("/acquisitionSimplifiedProcedureProcess.do")
                         && httpServletRequest.getQueryString() != null && httpServletRequest.getQueryString().contains(
                         "method=checkSupplierLimit"));
             }
         });
 
         RequestChecksumFilter.registerFilterRule(new ChecksumPredicate() {
             @Override
             public boolean shouldFilter(HttpServletRequest httpServletRequest) {
                 return !(httpServletRequest.getRequestURI().endsWith("/viewRCISTAnnouncements.do"))
                         && !(httpServletRequest.getRequestURI().endsWith("/viewAcquisitionAnnouncements.do"));
             }
         });
 
     }
 
     private static void initRoles() {
         for (final RoleType roleType : RoleType.values()) {
             Role.getRole(roleType);
         }
     }
 
     private ExpenditureTrackingSystem(final VirtualHost virtualHost) {
         super();
 //	setMyOrg(MyOrg.getInstance());
         setAcquisitionRequestDocumentCounter(0);
         virtualHost.setExpenditureTrackingSystem(this);
 
         new MyOwnProcessesSearch();
 //	final SavedSearch savedSearch = new PendingProcessesSearch();
 //	for (final Person person : getPeopleSet()) {
 //	    person.setDefaultSearch(savedSearch);
 //	}
 
         setAcquisitionCentralGroup(pt.ist.bennu.core.domain.groups.Role.getRole(RoleType.ACQUISITION_CENTRAL));
 
         setFundCommitmentManagerGroup(pt.ist.bennu.core.domain.groups.Role.getRole(RoleType.FUND_COMMITMENT_MANAGER));
 
         setAcquisitionCentralManagerGroup(pt.ist.bennu.core.domain.groups.Role.getRole(RoleType.ACQUISITION_CENTRAL_MANAGER));
 
         setAccountingManagerGroup(pt.ist.bennu.core.domain.groups.Role.getRole(RoleType.ACCOUNTING_MANAGER));
 
         setProjectAccountingManagerGroup(pt.ist.bennu.core.domain.groups.Role.getRole(RoleType.PROJECT_ACCOUNTING_MANAGER));
 
         setTreasuryMemberGroup(pt.ist.bennu.core.domain.groups.Role.getRole(RoleType.TREASURY_MANAGER));
 
         setSupplierManagerGroup(pt.ist.bennu.core.domain.groups.Role.getRole(RoleType.SUPPLIER_MANAGER));
 
         setSupplierFundAllocationManagerGroup(pt.ist.bennu.core.domain.groups.Role
                 .getRole(RoleType.SUPPLIER_FUND_ALLOCATION_MANAGER));
 
         setStatisticsViewerGroup(pt.ist.bennu.core.domain.groups.Role.getRole(RoleType.STATISTICS_VIEWER));
 
         setAcquisitionsUnitManagerGroup(pt.ist.bennu.core.domain.groups.Role.getRole(RoleType.AQUISITIONS_UNIT_MANAGER));
 
         setAcquisitionsProcessAuditorGroup(pt.ist.bennu.core.domain.groups.Role.getRole(RoleType.ACQUISITION_PROCESS_AUDITOR));
 
         setSearchProcessValuesArray(new SearchProcessValuesArray(SearchProcessValues.values()));
 
         setAcquisitionCreationWizardJsp("creationWizardPublicInstitution.jsp");
 
     }
 
     public String nextAcquisitionRequestDocumentID() {
         final String prefix = getInstitutionalRequestDocumentPrefix();
         return prefix + getAndUpdateNextAcquisitionRequestDocumentCountNumber();
     }
 
     public Integer nextAcquisitionRequestDocumentCountNumber() {
         return getAndUpdateNextAcquisitionRequestDocumentCountNumber();
     }
 
     private Integer getAndUpdateNextAcquisitionRequestDocumentCountNumber() {
         setAcquisitionRequestDocumentCounter(getAcquisitionRequestDocumentCounter().intValue() + 1);
         return getAcquisitionRequestDocumentCounter();
     }
 
     @Override
     public void init(final MyOrg root) {
         final ExpenditureTrackingSystem expenditureTrackingSystem = root.getExpenditureTrackingSystem();
         if (expenditureTrackingSystem != null) {
         }
     }
 
     private static void registerWidget(Class<? extends WidgetController> widgetClass) {
         WidgetRegister.registerWidget(widgetClass, EXPENDITURE_TRACKING_PANEL_PREDICATE);
     }
 
     @Atomic
     public static void createSystem(final VirtualHost virtualHost) {
         if (virtualHost.getExpenditureTrackingSystem() == null
                 || virtualHost.getExpenditureTrackingSystem().getVirtualHost().size() > 1) {
             new ExpenditureTrackingSystem(virtualHost);
             initRoles();
         }
     }
 
     public static boolean isAcquisitionCentralGroupMember(final User user) {
         final ExpenditureTrackingSystem system = getInstance();
         return system != null && system.hasAcquisitionCentralGroup() && system.getAcquisitionCentralGroup().isMember(user);
     }
 
     public static boolean isFundCommitmentManagerGroupMember(final User user) {
         final ExpenditureTrackingSystem system = getInstance();
         return system != null && system.hasFundCommitmentManagerGroup() && system.getFundCommitmentManagerGroup().isMember(user);
     }
 
     public static boolean isAcquisitionCentralManagerGroupMember(final User user) {
         final ExpenditureTrackingSystem system = getInstance();
         return system != null && system.hasAcquisitionCentralManagerGroup()
                 && system.getAcquisitionCentralManagerGroup().isMember(user);
     }
 
     public static boolean isAccountingManagerGroupMember(final User user) {
         final ExpenditureTrackingSystem system = getInstance();
         return system != null && system.hasAccountingManagerGroup() && system.getAccountingManagerGroup().isMember(user);
     }
 
     public static boolean isProjectAccountingManagerGroupMember(final User user) {
         final ExpenditureTrackingSystem system = getInstance();
         return system != null && system.hasProjectAccountingManagerGroup()
                 && system.getProjectAccountingManagerGroup().isMember(user);
     }
 
     public static boolean isTreasuryMemberGroupMember(final User user) {
         final ExpenditureTrackingSystem system = getInstance();
         return system != null && system.hasTreasuryMemberGroup() && system.getTreasuryMemberGroup().isMember(user);
     }
 
     public static boolean isSupplierManagerGroupMember(final User user) {
         final ExpenditureTrackingSystem system = getInstance();
         return system != null && system.hasSupplierManagerGroup() && system.getSupplierManagerGroup().isMember(user);
     }
 
     public static boolean isSupplierFundAllocationManagerGroupMember(final User user) {
         final ExpenditureTrackingSystem system = getInstance();
         return system != null && system.hasSupplierFundAllocationManagerGroup()
                 && system.getSupplierFundAllocationManagerGroup().isMember(user);
     }
 
     public static boolean isStatisticsViewerGroupMember(final User user) {
         final ExpenditureTrackingSystem system = getInstance();
         return system != null && system.hasStatisticsViewerGroup() && system.getStatisticsViewerGroup().isMember(user);
     }
 
     public static boolean isAcquisitionsUnitManagerGroupMember(final User user) {
         final ExpenditureTrackingSystem system = getInstance();
         return system != null && system.hasAcquisitionsUnitManagerGroup()
                 && system.getAcquisitionsUnitManagerGroup().isMember(user);
     }
 
     public static boolean isAcquisitionsProcessAuditorGroupMember(final User user) {
         final ExpenditureTrackingSystem system = getInstance();
         return system != null && system.hasAcquisitionsProcessAuditorGroup()
                 && system.getAcquisitionsProcessAuditorGroup().isMember(user);
     }
 
     public static boolean isAcquisitionCentralGroupMember() {
         final User user = UserView.getCurrentUser();
         return isAcquisitionCentralGroupMember(user);
     }
 
     public static boolean isFundCommitmentManagerGroupMember() {
         final User user = UserView.getCurrentUser();
         return isFundCommitmentManagerGroupMember(user);
     }
 
     public static boolean isAcquisitionCentralManagerGroupMember() {
         final User user = UserView.getCurrentUser();
         return isAcquisitionCentralManagerGroupMember(user);
     }
 
     public static boolean isAccountingManagerGroupMember() {
         final User user = UserView.getCurrentUser();
         return isAccountingManagerGroupMember(user);
     }
 
     public static boolean isProjectAccountingManagerGroupMember() {
         final User user = UserView.getCurrentUser();
         return isProjectAccountingManagerGroupMember(user);
     }
 
     public static boolean isTreasuryMemberGroupMember() {
         final User user = UserView.getCurrentUser();
         return isTreasuryMemberGroupMember(user);
     }
 
     public static boolean isSupplierManagerGroupMember() {
         final User user = UserView.getCurrentUser();
         return isSupplierManagerGroupMember(user);
     }
 
     public static boolean isSupplierFundAllocationManagerGroupMember() {
         final User user = UserView.getCurrentUser();
         return isSupplierFundAllocationManagerGroupMember(user);
     }
 
     public static boolean isStatisticsViewerGroupMember() {
         final User user = UserView.getCurrentUser();
         return isStatisticsViewerGroupMember(user);
     }
 
     public static boolean isAcquisitionsUnitManagerGroupMember() {
         final User user = UserView.getCurrentUser();
         return isAcquisitionsUnitManagerGroupMember(user);
     }
 
     public static boolean isAcquisitionsProcessAuditorGroupMember() {
         final User user = UserView.getCurrentUser();
         return isAcquisitionsProcessAuditorGroupMember(user);
     }
 
     public static boolean isManager() {
         final User user = UserView.getCurrentUser();
         final pt.ist.bennu.core.domain.groups.Role role =
                 pt.ist.bennu.core.domain.groups.Role.getRole(pt.ist.bennu.core.domain.RoleType.MANAGER);
         return role.isMember(user);
     }
 
     public boolean contains(final SearchProcessValues values) {
         return getSearchProcessValuesArray() != null && getSearchProcessValuesArray().contains(values);
     }
 
     public SortedSet<ProcessClassification> getAllowdProcessClassifications(final Class processType) {
         final SortedSet<ProcessClassification> classifications = new TreeSet<SimplifiedProcedureProcess.ProcessClassification>();
         for (final SearchProcessValues searchProcessValues : getSearchProcessValuesArray().getSearchProcessValues()) {
             if (processType != null && processType == searchProcessValues.getSearchClass()
                     && searchProcessValues.getSearchClassification() != null) {
                 classifications.add(searchProcessValues.getSearchClassification());
             }
         }
         return classifications;
     }
 
     @Atomic
     public void saveConfiguration(final String institutionalProcessNumberPrefix, final String institutionalRequestDocumentPrefix,
             final String acquisitionCreationWizardJsp, final SearchProcessValuesArray array,
             final Boolean invoiceAllowedToStartAcquisitionProcess, final Boolean requireFundAllocationPriorToAcquisitionRequest,
             final Boolean registerDiaryNumbersAndTransactionNumbers, final Money maxValueStartedWithInvoive,
             final Money valueRequireingTopLevelAuthorization, final String documentationUrl, final String documentationLabel,
             final Boolean requireCommitmentNumber, final Boolean processesNeedToBeReverified) {
         setInstitutionalProcessNumberPrefix(institutionalProcessNumberPrefix);
         setInstitutionalRequestDocumentPrefix(institutionalRequestDocumentPrefix);
         setAcquisitionCreationWizardJsp(acquisitionCreationWizardJsp);
         setSearchProcessValuesArray(array);
         setInvoiceAllowedToStartAcquisitionProcess(invoiceAllowedToStartAcquisitionProcess);
         setRequireFundAllocationPriorToAcquisitionRequest(requireFundAllocationPriorToAcquisitionRequest);
         setRegisterDiaryNumbersAndTransactionNumbers(registerDiaryNumbersAndTransactionNumbers);
         setMaxValueStartedWithInvoive(maxValueStartedWithInvoive);
         setValueRequireingTopLevelAuthorization(valueRequireingTopLevelAuthorization);
         setDocumentationUrl(documentationUrl);
         setDocumentationLabel(documentationLabel);
         setRequireCommitmentNumber(requireCommitmentNumber);
         setProcessesNeedToBeReverified(processesNeedToBeReverified);
     }
 
     @Atomic
     public void setForVirtualHost(final VirtualHost virtualHost) {
         virtualHost.setExpenditureTrackingSystem(this);
     }
 
     public static boolean isInvoiceAllowedToStartAcquisitionProcess() {
         final ExpenditureTrackingSystem system = getInstance();
         final Boolean invoiceAllowedToStartAcquisitionProcess = system.getInvoiceAllowedToStartAcquisitionProcess();
         return invoiceAllowedToStartAcquisitionProcess != null && invoiceAllowedToStartAcquisitionProcess.booleanValue();
     }
 
     public boolean hasProcessPrefix() {
         final String prefix = getInstitutionalProcessNumberPrefix();
         return prefix != null && !prefix.isEmpty();
     }
 
     public boolean checkSupplierLimitsByCPV() {
         return getCheckSupplierLimitsByCPV() != null && getCheckSupplierLimitsByCPV().booleanValue();
     }
 
     public boolean processesNeedToBeReverified() {
         final Boolean b = getProcessesNeedToBeReverified();
         return b != null && b.booleanValue();
     }
 
     public boolean isCommitmentNumberRequired() {
         if (getRequireCommitmentNumber() == null) {
             return false;
         } else {
             return getRequireCommitmentNumber();
         }
 
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.acquisitions.CPVReference> getCPVReferences() {
         return getCPVReferencesSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.SavedSearch> getSystemSearches() {
         return getSystemSearchesSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.organization.DeliveryInfo> getDeliveryInfos() {
         return getDeliveryInfosSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestItem> getRequestItems() {
         return getRequestItemsSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit> getAccountingUnits() {
         return getAccountingUnitsSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.authorizations.AuthorizationLog> getAuthorizationLogs() {
         return getAuthorizationLogsSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.acquisitions.CPVReference> getPriorityCPVReferences() {
         return getPriorityCPVReferencesSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.organization.Person> getPeople() {
         return getPeopleSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessYear> getPaymentProcessYears() {
         return getPaymentProcessYearsSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.Options> getOptions() {
         return getOptionsSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.Role> getRoles() {
         return getRolesSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.Refundee> getRefundees() {
         return getRefundeesSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.SavedSearch> getSavedSearches() {
         return getSavedSearchesSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer> getFinancers() {
         return getFinancersSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.DashBoard> getDashBoards() {
         return getDashBoardsSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.announcements.Announcement> getAnnouncements() {
         return getAnnouncementsSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.processes.GenericProcess> getProcesses() {
         return getProcessesSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.organization.Unit> getTopLevelUnits() {
         return getTopLevelUnitsSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization> getAuthorizations() {
         return getAuthorizationsSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.acquisitions.Acquisition> getAcquisitions() {
         return getAcquisitionsSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.organization.Supplier> getSuppliers() {
         return getSuppliersSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.organization.Unit> getUnits() {
         return getUnitsSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.bennu.core.domain.VirtualHost> getVirtualHost() {
         return getVirtualHostSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.ProcessState> getProcessStates() {
         return getProcessStatesSet();
     }
 
     @Deprecated
     public boolean hasAnyCPVReferences() {
         return !getCPVReferencesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnySystemSearches() {
         return !getSystemSearchesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyDeliveryInfos() {
         return !getDeliveryInfosSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyRequestItems() {
         return !getRequestItemsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyAccountingUnits() {
         return !getAccountingUnitsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyAuthorizationLogs() {
         return !getAuthorizationLogsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyPriorityCPVReferences() {
         return !getPriorityCPVReferencesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyPeople() {
         return !getPeopleSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyPaymentProcessYears() {
         return !getPaymentProcessYearsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyOptions() {
         return !getOptionsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyRoles() {
         return !getRolesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyRefundees() {
         return !getRefundeesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnySavedSearches() {
         return !getSavedSearchesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyFinancers() {
         return !getFinancersSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyDashBoards() {
         return !getDashBoardsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyAnnouncements() {
         return !getAnnouncementsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyProcesses() {
         return !getProcessesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyTopLevelUnits() {
         return !getTopLevelUnitsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyAuthorizations() {
         return !getAuthorizationsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyAcquisitions() {
         return !getAcquisitionsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnySuppliers() {
         return !getSuppliersSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyUnits() {
         return !getUnitsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyVirtualHost() {
         return !getVirtualHostSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyProcessStates() {
         return !getProcessStatesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAcquisitionRequestDocumentCounter() {
         return getAcquisitionRequestDocumentCounter() != null;
     }
 
     @Deprecated
     public boolean hasSearchProcessValuesArray() {
         return getSearchProcessValuesArray() != null;
     }
 
     @Deprecated
     public boolean hasAcquisitionCreationWizardJsp() {
         return getAcquisitionCreationWizardJsp() != null;
     }
 
     @Deprecated
     public boolean hasInstitutionalProcessNumberPrefix() {
         return getInstitutionalProcessNumberPrefix() != null;
     }
 
     @Deprecated
     public boolean hasInstitutionalRequestDocumentPrefix() {
         return getInstitutionalRequestDocumentPrefix() != null;
     }
 
     @Deprecated
     public boolean hasInvoiceAllowedToStartAcquisitionProcess() {
         return getInvoiceAllowedToStartAcquisitionProcess() != null;
     }
 
     @Deprecated
     public boolean hasRequireFundAllocationPriorToAcquisitionRequest() {
         return getRequireFundAllocationPriorToAcquisitionRequest() != null;
     }
 
     @Deprecated
     public boolean hasRegisterDiaryNumbersAndTransactionNumbers() {
         return getRegisterDiaryNumbersAndTransactionNumbers() != null;
     }
 
     @Deprecated
     public boolean hasRequireCommitmentNumber() {
         return getRequireCommitmentNumber() != null;
     }
 
     @Deprecated
     public boolean hasMaxValueStartedWithInvoive() {
         return getMaxValueStartedWithInvoive() != null;
     }
 
     @Deprecated
     public boolean hasValueRequireingTopLevelAuthorization() {
         return getValueRequireingTopLevelAuthorization() != null;
     }
 
     @Deprecated
     public boolean hasDocumentationUrl() {
         return getDocumentationUrl() != null;
     }
 
     @Deprecated
     public boolean hasDocumentationLabel() {
         return getDocumentationLabel() != null;
     }
 
     @Deprecated
     public boolean hasCheckSupplierLimitsByCPV() {
         return getCheckSupplierLimitsByCPV() != null;
     }
 
     @Deprecated
     public boolean hasInstitutionManagementEmail() {
         return getInstitutionManagementEmail() != null;
     }
 
     @Deprecated
     public boolean hasTreasuryMemberGroup() {
         return getTreasuryMemberGroup() != null;
     }
 
     @Deprecated
     public boolean hasAcquisitionCentralGroup() {
         return getAcquisitionCentralGroup() != null;
     }
 
     @Deprecated
     public boolean hasOrganizationalAccountabilityType() {
         return getOrganizationalAccountabilityType() != null;
     }
 
     @Deprecated
     public boolean hasProjectPartyType() {
         return getProjectPartyType() != null;
     }
 
     @Deprecated
     public boolean hasAcquisitionCentralManagerGroup() {
         return getAcquisitionCentralManagerGroup() != null;
     }
 
     @Deprecated
     public boolean hasMyOrg() {
         return getMyOrg() != null;
     }
 
     @Deprecated
     public boolean hasProjectAccountingManagerGroup() {
         return getProjectAccountingManagerGroup() != null;
     }
 
     @Deprecated
     public boolean hasUnitPartyType() {
         return getUnitPartyType() != null;
     }
 
     @Deprecated
     public boolean hasCostCenterPartyType() {
         return getCostCenterPartyType() != null;
     }
 
     @Deprecated
     public boolean hasStatisticsViewerGroup() {
         return getStatisticsViewerGroup() != null;
     }
 
     @Deprecated
     public boolean hasAccountingManagerGroup() {
         return getAccountingManagerGroup() != null;
     }
 
     @Deprecated
     public boolean hasFundCommitmentManagerGroup() {
         return getFundCommitmentManagerGroup() != null;
     }
 
     @Deprecated
     public boolean hasAcquisitionsProcessAuditorGroup() {
         return getAcquisitionsProcessAuditorGroup() != null;
     }
 
     @Deprecated
     public boolean hasOrganizationalMissionAccountabilityType() {
         return getOrganizationalMissionAccountabilityType() != null;
     }
 
     @Deprecated
     public boolean hasAcquisitionsUnitManagerGroup() {
         return getAcquisitionsUnitManagerGroup() != null;
     }
 
     @Deprecated
     public boolean hasSupplierManagerGroup() {
         return getSupplierManagerGroup() != null;
     }
 
     @Deprecated
     public boolean hasSupplierFundAllocationManagerGroup() {
         return getSupplierFundAllocationManagerGroup() != null;
     }
 
     @Deprecated
     public boolean hasSubProjectPartyType() {
         return getSubProjectPartyType() != null;
     }
 
     public interface InfoProvider {
         public String getTitle();
 
         public Map<String, String> getLinks(String page, Object object);

        public List<List<String>> getSummary(String page, Object object);
     }
 
     static private InfoProvider infoProvider;
 
     static public void registerInfoProvider(InfoProvider aInfoProvider) {
         infoProvider = aInfoProvider;
     }
 
     static public InfoProvider getInfoProvider() {
         return infoProvider;
     }
 }
