 /*
  * @(#)MissionSystem.java
  *
  * Copyright 2011 Instituto Superior Tecnico
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
 package module.mission.domain;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import module.mission.domain.activity.AuthorizeVehicleItemActivity;
 import module.mission.domain.activity.ItemActivityInformation;
 import module.mission.domain.util.MigratePersonalInformationProcessedSlot;
 import module.mission.domain.util.MigrateVerifiedSlot;
 import module.organization.domain.Accountability;
 import module.organization.domain.AccountabilityType;
 import module.organization.domain.OrganizationalModel;
 import module.organization.domain.Party;
 import module.organization.domain.Unit;
import module.workflow.domain.WorkflowQueue;
 import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
 import pt.ist.bennu.core.domain.MyOrg;
 import pt.ist.bennu.core.domain.User;
 import pt.ist.bennu.core.domain.VirtualHost;
 import pt.ist.bennu.core.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.fenixframework.Atomic;
 
 /**
  * 
  * @author Luis Cruz
  * 
  */
 public class MissionSystem extends MissionSystem_Base {
 
     private static boolean isMigrationInProgress = false;
 
     public static MissionSystem getInstance() {
         final VirtualHost virtualHostForThread = VirtualHost.getVirtualHostForThread();
         if (virtualHostForThread.getMissionSystem() == null) {
             initialize(virtualHostForThread);
         }
 
         migrate();
 
         return virtualHostForThread == null ? null : virtualHostForThread.getMissionSystem();
     }
 
     private static void migrate() {
         if (!isMigrationInProgress) {
             synchronized (MissionSystem.class) {
                 if (!isMigrationInProgress) {
                     isMigrationInProgress = true;
                     MigratePersonalInformationProcessedSlot.migrateForAllVirtualHosts();
                     MigrateVerifiedSlot.migrateForAllVirtualHosts();
                     isMigrationInProgress = false;
                 }
             }
         }
     }
 
     @Atomic
     public synchronized static void initialize(VirtualHost virtualHost) {
         if (virtualHost.getMissionSystem() == null) {
             new MissionSystem(virtualHost);
         }
     }
 
     private MissionSystem(final VirtualHost virtualHost) {
         super();
         addVirtualHost(virtualHost);
         setIsPersonalInformationProcessedSlotMigrated(true);
         setIsVerifiedSlotMigrated(true);
     }
 
     public boolean isPersonalInformationProcessedSlotMigrated() {
         return getIsPersonalInformationProcessedSlotMigrated();
     }
 
     public boolean isVerifiedSlotMigrated() {
         return getIsVerifiedSlotMigrated();
     }
 
     public static boolean canUserVerifyProcesses(User user) {
        WorkflowQueue verificationQueue = getInstance().getVerificationQueue();
        return (verificationQueue != null) ? verificationQueue.isUserAbleToAccessQueue(user) : false;
     }
 
     public ExpenditureTrackingSystem getExpenditureTrackingSystem() {
         return getVirtualHost().iterator().next().getExpenditureTrackingSystem();
     }
 
     public Set<pt.ist.expenditureTrackingSystem.domain.organization.Unit> getTopLevelUnitsFromExpenditureSystem() {
         return getExpenditureTrackingSystem().getTopLevelUnits();
     }
 
     public pt.ist.expenditureTrackingSystem.domain.organization.Unit getFirstTopLevelUnitFromExpenditureSystem() {
         return getExpenditureTrackingSystem().getTopLevelUnits().iterator().next();
     }
 
     public Set<AccountabilityType> getAccountabilityTypesThatAuthorize() {
         final Set<AccountabilityType> accountabilityTypes = new HashSet<AccountabilityType>();
         for (final MissionAuthorizationAccountabilityType missionAuthorizationAccountabilityType : getMissionAuthorizationAccountabilityTypesSet()) {
             accountabilityTypes.addAll(missionAuthorizationAccountabilityType.getAccountabilityTypes());
         }
         return accountabilityTypes;
     }
 
     public Set<AccountabilityType> getAccountabilityTypesRequireingAuthorization() {
         final Set<AccountabilityType> accountabilityTypes = new HashSet<AccountabilityType>();
         for (final MissionAuthorizationAccountabilityType missionAuthorizationAccountabilityType : getMissionAuthorizationAccountabilityTypesSet()) {
             accountabilityTypes.add(missionAuthorizationAccountabilityType.getAccountabilityType());
         }
         return accountabilityTypes;
     }
 
     public Collection<AccountabilityType> getAccountabilityTypesForUnits() {
         final Set<AccountabilityType> modelAccountabilityTypes =
                 hasOrganizationalModel() ? getOrganizationalModel().getAccountabilityTypesSet() : Collections.EMPTY_SET;
         final Set<AccountabilityType> accountabilityTypes = new HashSet<AccountabilityType>(modelAccountabilityTypes);
         for (final MissionAuthorizationAccountabilityType missionAuthorizationAccountabilityType : getMissionAuthorizationAccountabilityTypesSet()) {
             accountabilityTypes.remove(missionAuthorizationAccountabilityType.getAccountabilityType());
             accountabilityTypes.removeAll(missionAuthorizationAccountabilityType.getAccountabilityTypesSet());
         }
         return accountabilityTypes;
     }
 
     public Set<AccountabilityType> getAccountabilityTypesForAuthorization(final AccountabilityType accountabilityType) {
         final MissionAuthorizationAccountabilityType missionAuthorizationAccountabilityType =
                 MissionAuthorizationAccountabilityType.find(accountabilityType);
         return missionAuthorizationAccountabilityType == null ? null : missionAuthorizationAccountabilityType
                 .getAccountabilityTypesSet();
     }
 
     public Collection<DailyPersonelExpenseTable> getCurrentDailyExpenseTables() {
         final Map<String, DailyPersonelExpenseTable> dailyExpenseTableMap = new HashMap<String, DailyPersonelExpenseTable>();
         for (final DailyPersonelExpenseTable dailyPersonelExpenseTable : getDailyPersonelExpenseTablesSet()) {
             final String aplicableToMissionType = dailyPersonelExpenseTable.getAplicableToMissionType();
             final DailyPersonelExpenseTable existing = dailyExpenseTableMap.get(aplicableToMissionType);
             if (existing == null || existing.getAplicableSince().isBefore(dailyPersonelExpenseTable.getAplicableSince())) {
                 dailyExpenseTableMap.put(aplicableToMissionType, dailyPersonelExpenseTable);
             }
         }
         return dailyExpenseTableMap.values();
     }
 
     @Atomic
     @Override
     public void setOrganizationalModel(final OrganizationalModel organizationalModel) {
         super.setOrganizationalModel(organizationalModel);
     }
 
     public boolean isCurrentUserVehicleAuthorizer() {
         return getVehicleAuthorizers().contains(UserView.getCurrentUser());
     }
 
     public Collection<VehiclItem> getVehicleItemsPendingAuthorization() {
         Collection<VehiclItem> pendingVehicles = new HashSet<VehiclItem>();
         for (MissionProcess process : getMissionProcesses()) {
             if (!process.canAuthorizeVehicles()) {
                 continue;
             }
 
             for (VehiclItem vehicle : process.getMission().getVehicleItems()) {
                 if (!vehicle.isAuthorized()) {
                     pendingVehicles.add(vehicle);
                 }
             }
         }
         return pendingVehicles;
     }
 
     public boolean isAccountabilityTypesThatAuthorize(final AccountabilityType accountabilityType) {
         for (final MissionAuthorizationAccountabilityType missionAuthorizationAccountabilityType : getMissionAuthorizationAccountabilityTypesSet()) {
             if (missionAuthorizationAccountabilityType.getAccountabilityTypesSet().contains(accountabilityType)) {
                 return true;
             }
         }
         return false;
     }
 
     @Atomic
     public void addUnitWithResumedAuthorizations(final Unit unit) {
         addUnitsWithResumedAuthorizations(unit);
     }
 
     @Atomic
     public void removeUnitWithResumedAuthorizations(final Unit unit) {
         removeUnitsWithResumedAuthorizations(unit);
     }
 
     public SortedSet<Unit> getOrderedUnitsWithResumedAuthorizations() {
         final SortedSet<Unit> result = new TreeSet<Unit>(Unit.COMPARATOR_BY_PRESENTATION_NAME);
         result.addAll(getUnitsWithResumedAuthorizationsSet());
         return result;
     }
 
     public static String getBundle() {
         return "resources.MissionResources";
     }
 
     public static String getMessage(final String key, String... args) {
         return BundleUtil.getFormattedStringFromResourceBundle(getBundle(), key, args);
     }
 
     public boolean isManagementCouncilMember(final User user) {
         final OrganizationalModel model = getOrganizationalModel();
         for (final Party party : model.getPartiesSet()) {
             if (party.isUnit() && isManagementCouncilMember(user, model, (Unit) party, true)) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean isManagementCouncilMember(final User user, final OrganizationalModel model, final Unit unit,
             final boolean recurseOverChildren) {
         for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
             final AccountabilityType accountabilityType = accountability.getAccountabilityType();
             if (model.getAccountabilityTypesSet().contains(accountabilityType)) {
                 final Party child = accountability.getChild();
                 if ((isAccountabilityTypesThatAuthorize(accountabilityType) && child == user.getPerson())
                         || (recurseOverChildren && child.isUnit() && isManagementCouncilMember(user, model, (Unit) child, false))) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     @Atomic
     @Override
     public void addUsersWhoCanCancelMission(User usersWhoCanCancelMission) {
         super.addUsersWhoCanCancelMission(usersWhoCanCancelMission);
     }
 
     @Atomic
     @Override
     public void removeUsersWhoCanCancelMission(User usersWhoCanCancelMission) {
         super.removeUsersWhoCanCancelMission(usersWhoCanCancelMission);
     }
 
     @Atomic
     @Override
     public void addVehicleAuthorizers(User vehicleAuthorizers) {
         super.addVehicleAuthorizers(vehicleAuthorizers);
     }
 
     @Atomic
     @Override
     public void removeVehicleAuthorizers(User vehicleAuthorizers) {
         super.removeVehicleAuthorizers(vehicleAuthorizers);
     }
 
     public static Set<MissionSystem> readAllMissionSystems() {
         Set<MissionSystem> systems = new HashSet<MissionSystem>();
         for (VirtualHost vh : MyOrg.getInstance().getVirtualHosts()) {
             if (vh.getMissionSystem() != null) {
                 systems.add(vh.getMissionSystem());
             }
         }
         return systems;
     }
 
     public boolean allowGrantOwnerEquivalence() {
         final Boolean b = getAllowGrantOwnerEquivalence();
         return b != null && b.booleanValue();
     }
 
     @Atomic
     public void toggleAllowGrantOwnerEquivalence() {
         final Boolean b = getAllowGrantOwnerEquivalence();
         setAllowGrantOwnerEquivalence(b == null ? Boolean.TRUE : Boolean.valueOf(!b.booleanValue()));
     }
 
     @Atomic
     public static void massAuthorizeVehicles(Collection<VehiclItem> items) {
         for (final VehiclItem item : items) {
             final Mission mission = item.getMission();
             final MissionProcess missionProcess = mission.getMissionProcess();
             final AuthorizeVehicleItemActivity activity =
                     (AuthorizeVehicleItemActivity) missionProcess.getActivity(AuthorizeVehicleItemActivity.class);
             final ItemActivityInformation activityInfo = activity.getActivityInformation(missionProcess);
             activityInfo.setMissionItem(item);
             activity.execute(activityInfo);
         }
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.PersonMissionAuthorization> getPersonMissionAuthorizations() {
         return getPersonMissionAuthorizationsSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.VehiclItemJustification> getVehiclItemJustification() {
         return getVehiclItemJustificationSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.bennu.core.domain.VirtualHost> getVirtualHost() {
         return getVirtualHostSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.MissionVersion> getMissionVersions() {
         return getMissionVersionsSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.TemporaryMissionItemEntry> getTemporaryMissionItemEntries() {
         return getTemporaryMissionItemEntriesSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.MissionAuthorizationAccountabilityType> getMissionAuthorizationAccountabilityTypes() {
         return getMissionAuthorizationAccountabilityTypesSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.DailyPersonelExpenseTable> getDailyPersonelExpenseTables() {
         return getDailyPersonelExpenseTablesSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.bennu.core.domain.User> getVehicleAuthorizers() {
         return getVehicleAuthorizersSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.MissionFinancer> getFinancer() {
         return getFinancerSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.Mission> getMissions() {
         return getMissionsSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.DailyPersonelExpenseCategory> getDailyPersonelExpenseCategories() {
         return getDailyPersonelExpenseCategoriesSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.MissionYear> getMissionYear() {
         return getMissionYearSet();
     }
 
     @Deprecated
     public java.util.Set<module.organization.domain.Unit> getUnitsWithResumedAuthorizations() {
         return getUnitsWithResumedAuthorizationsSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.Salary> getSalaries() {
         return getSalariesSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.MissionProcess> getMissionProcesses() {
         return getMissionProcessesSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.MissionProcessLateJustification> getMissionProcessLateJustification() {
         return getMissionProcessLateJustificationSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.MissionChangeDescription> getMissionChangeDescription() {
         return getMissionChangeDescriptionSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.MissionItemFinancer> getMissionItemFinancers() {
         return getMissionItemFinancersSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.bennu.core.domain.User> getUsersWhoCanCancelMission() {
         return getUsersWhoCanCancelMissionSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.MissionItem> getMissionItems() {
         return getMissionItemsSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.AccountabilityTypeQueue> getAccountabilityTypeQueues() {
         return getAccountabilityTypeQueuesSet();
     }
 
     @Deprecated
     public java.util.Set<module.organization.domain.Person> getGovernmentMembers() {
         return getGovernmentMembersSet();
     }
 
     @Deprecated
     public boolean hasAnyPersonMissionAuthorizations() {
         return !getPersonMissionAuthorizationsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyVehiclItemJustification() {
         return !getVehiclItemJustificationSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyVirtualHost() {
         return !getVirtualHostSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyMissionVersions() {
         return !getMissionVersionsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyTemporaryMissionItemEntries() {
         return !getTemporaryMissionItemEntriesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyMissionAuthorizationAccountabilityTypes() {
         return !getMissionAuthorizationAccountabilityTypesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyDailyPersonelExpenseTables() {
         return !getDailyPersonelExpenseTablesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyVehicleAuthorizers() {
         return !getVehicleAuthorizersSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyFinancer() {
         return !getFinancerSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyMissions() {
         return !getMissionsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyDailyPersonelExpenseCategories() {
         return !getDailyPersonelExpenseCategoriesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyMissionYear() {
         return !getMissionYearSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyUnitsWithResumedAuthorizations() {
         return !getUnitsWithResumedAuthorizationsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnySalaries() {
         return !getSalariesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyMissionProcesses() {
         return !getMissionProcessesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyMissionProcessLateJustification() {
         return !getMissionProcessLateJustificationSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyMissionChangeDescription() {
         return !getMissionChangeDescriptionSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyMissionItemFinancers() {
         return !getMissionItemFinancersSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyUsersWhoCanCancelMission() {
         return !getUsersWhoCanCancelMissionSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyMissionItems() {
         return !getMissionItemsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyAccountabilityTypeQueues() {
         return !getAccountabilityTypeQueuesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyGovernmentMembers() {
         return !getGovernmentMembersSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAllowGrantOwnerEquivalence() {
         return getAllowGrantOwnerEquivalence() != null;
     }
 
     @Deprecated
     public boolean hasMyOrg() {
         return getMyOrg() != null;
     }
 
     @Deprecated
     public boolean hasOrganizationalModel() {
         return getOrganizationalModel() != null;
     }
 
     @Deprecated
     public boolean hasCountry() {
         return getCountry() != null;
     }
 
 }
