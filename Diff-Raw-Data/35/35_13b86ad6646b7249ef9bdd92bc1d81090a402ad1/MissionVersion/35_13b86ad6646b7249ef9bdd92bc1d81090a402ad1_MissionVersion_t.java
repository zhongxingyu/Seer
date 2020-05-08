 /*
  * @(#)MissionVersion.java
  *
  * Copyright 2010 Instituto Superior Tecnico
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
 
 import java.util.ArrayList;
import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.joda.time.DateTime;
 
 import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
 import pt.ist.bennu.core.domain.VirtualHost;
 
 /**
  * 
  * @author Luis Cruz
  * 
  */
 public class MissionVersion extends MissionVersion_Base {
 
    public static final Comparator<MissionVersion> COMPARATOR_BY_OLDEST_SINCE = new Comparator<MissionVersion>() {

        @Override
        public int compare(MissionVersion mv1, MissionVersion mv2) {
            return mv1.getSinceDateTime().isBefore(mv2.getSinceDateTime()) ? -1 : 1;
        }
    };

     public MissionVersion(final Mission mission) {
         final MissionVersion previous = mission.getMissionVersion();
 
         setSinceDateTime(new DateTime());
         setMissionSystem(MissionSystem.getInstance());
         setMission(mission);
         setIsArchived(Boolean.FALSE);
 
         copyVersionInformation(previous);
     }
 
     protected void copyVersionInformation(final MissionVersion previous) {
         if (previous != null) {
             setDaparture(previous.getDaparture());
             setArrival(previous.getArrival());
             setChangesAfterArrival(previous.getChangesAfterArrival());
             setSentForTermination(previous.getSentForTermination());
             setDescriptionOfChangesAfterArrival(previous.getDescriptionOfChangesAfterArrival());
             setTerminator(previous.getTerminator());
 
             final Map<MissionFinancer, MissionFinancer> missionFinancerMap = new HashMap<MissionFinancer, MissionFinancer>();
             for (final MissionFinancer previousMissionFinancer : previous.getFinancerSet()) {
                 final MissionFinancer missionFinancer = previousMissionFinancer.createNewVersion(this);
                 missionFinancerMap.put(previousMissionFinancer, missionFinancer);
             }
 
             final Map<MissionItem, MissionItem> missionItemMap = new HashMap<MissionItem, MissionItem>();
             for (final MissionItem previousMissionItem : previous.getMissionItemsSet()) {
                 final MissionItem missionItem = previousMissionItem.createNewVersion(this);
                 missionItemMap.put(previousMissionItem, missionItem);
             }
 
             for (final MissionFinancer previousMissionFinancer : previous.getFinancerSet()) {
                 final MissionFinancer missionFinancer = missionFinancerMap.get(previousMissionFinancer);
                 missionFinancer.setCommitmentNumber(previousMissionFinancer.getCommitmentNumber());
 
                 for (final MissionItemFinancer previousMissionItemFinancer : previousMissionFinancer.getMissionItemFinancersSet()) {
 
                     final MissionItem previousMissionItem = previousMissionItemFinancer.getMissionItem();
                     final MissionItem missionItem = missionItemMap.get(previousMissionItem);
 
                     previousMissionItemFinancer.createNewVersion(missionFinancer, missionItem);
                 }
             }
         }
     }
 
     public void setDates(final DateTime daparture, final DateTime arrival) {
         setDaparture(daparture);
         setArrival(arrival);
     }
 
     @Override
     public void setChangesAfterArrival(final Boolean changesAfterArrival) {
         super.setChangesAfterArrival(changesAfterArrival);
         if (changesAfterArrival != null) {
             setTerminator(UserView.getCurrentUser().getPerson());
             setSentForTermination(new DateTime());
         }
     }
 
     public boolean isTerminatedWithChanges() {
         return getChangesAfterArrival() != null && getChangesAfterArrival().booleanValue();
     }
 
     public void autoArchive() {
         for (final MissionItem missionItem : getMissionItemsSet()) {
             missionItem.autoArchive();
         }
     }
 
     public void unArchive() {
         for (final MissionItem missionItem : getMissionItemsSet()) {
             missionItem.unArchive();
         }
     }
 
     public List<VehiclItem> getVehicleItems() {
         List<VehiclItem> items = new ArrayList<VehiclItem>();
         for (MissionItem item : getMissionItems()) {
             if (item instanceof VehiclItem) {
                 items.add((VehiclItem) item);
             }
         }
         return items;
     }
 
     public boolean hasAnyVehicleItems() {
         return !getVehicleItems().isEmpty();
     }
 
     public boolean isTerminated() {
         return getChangesAfterArrival() != null;
     }
 
     public boolean canArchiveMission() {
         if (hasAnyMissionItems()) {
             for (final MissionItem missionItem : getMissionItemsSet()) {
                 if (missionItem.isAccountantForUnArchivedMissionItemFinancer()) {
                     return true;
                 }
             }
             return !getIsArchived().booleanValue() && areAllItemsArchived() && isAccountantOfProjectAccountant();
         }
         final Mission mission = getMission();
         return mission.isRequestorOrResponsible();
     }
 
     public boolean canArchiveMissionDirect() {
         if (hasAnyMissionItems()) {
             for (final MissionItem missionItem : getMissionItemsSet()) {
                 if (missionItem.isDirectAccountantForUnArchivedMissionItemFinancer()) {
                     return true;
                 }
             }
             return !getIsArchived().booleanValue() && areAllItemsArchived() && isDirectAccountantOfProjectAccountant();
         }
         final Mission mission = getMission();
         return mission.isRequestorOrResponsible();
     }
 
     private boolean isAccountantOfProjectAccountant() {
         for (final MissionItem missionItem : getMissionItemsSet()) {
             for (final MissionItemFinancer missionItemFinancer : missionItem.getMissionItemFinancersSet()) {
                 final MissionFinancer missionFinancer = missionItemFinancer.getMissionFinancer();
                 if (missionFinancer.isCurrentUserAccountant() || missionFinancer.isCurrentUserProjectAccountant()) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private boolean isDirectAccountantOfProjectAccountant() {
         for (final MissionItem missionItem : getMissionItemsSet()) {
             for (final MissionItemFinancer missionItemFinancer : missionItem.getMissionItemFinancersSet()) {
                 final MissionFinancer missionFinancer = missionItemFinancer.getMissionFinancer();
                 if (missionFinancer.isCurrentUserAccountant() || missionFinancer.isCurrentUserDirectProjectAccountant()) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private boolean areAllItemsArchived() {
         for (final MissionItem missionItem : getMissionItemsSet()) {
             for (final MissionItemFinancer missionItemFinancer : missionItem.getMissionItemFinancersSet()) {
                 if (!missionItemFinancer.isArchived()) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     public boolean hasNoItemsAndParticipantesAreAuthorized() {
         return !hasAnyMissionItems() && getMission().allParticipantsAreAuthorized();
     }
 
     @Override
     public boolean isConnectedToCurrentHost() {
         return getMissionSystem() == VirtualHost.getVirtualHostForThread().getMissionSystem();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.MissionItem> getMissionItems() {
         return getMissionItemsSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.MissionFinancer> getFinancer() {
         return getFinancerSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.MissionItemProjectFinancer> getArchivedMissionItemProjectFinancer() {
         return getArchivedMissionItemProjectFinancerSet();
     }
 
     @Deprecated
     public java.util.Set<module.mission.domain.MissionItemFinancer> getArchivedMissionItemFinancer() {
         return getArchivedMissionItemFinancerSet();
     }
 
     @Deprecated
     public boolean hasAnyMissionItems() {
         return !getMissionItemsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyFinancer() {
         return !getFinancerSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyArchivedMissionItemProjectFinancer() {
         return !getArchivedMissionItemProjectFinancerSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyArchivedMissionItemFinancer() {
         return !getArchivedMissionItemFinancerSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasSinceDateTime() {
         return getSinceDateTime() != null;
     }
 
     @Deprecated
     public boolean hasDaparture() {
         return getDaparture() != null;
     }
 
     @Deprecated
     public boolean hasArrival() {
         return getArrival() != null;
     }
 
     @Deprecated
     public boolean hasChangesAfterArrival() {
         return getChangesAfterArrival() != null;
     }
 
     @Deprecated
     public boolean hasSentForTermination() {
         return getSentForTermination() != null;
     }
 
     @Deprecated
     public boolean hasDescriptionOfChangesAfterArrival() {
         return getDescriptionOfChangesAfterArrival() != null;
     }
 
     @Deprecated
     public boolean hasIsArchived() {
         return getIsArchived() != null;
     }
 
     @Deprecated
     public boolean hasMission() {
         return getMission() != null;
     }
 
     @Deprecated
     public boolean hasMissionSystem() {
         return getMissionSystem() != null;
     }
 
     @Deprecated
     public boolean hasTerminator() {
         return getTerminator() != null;
     }
 
 }
