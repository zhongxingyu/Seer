 package module.mission.domain.util;
 
 import module.mission.domain.MissionProcess;
 import pt.ist.bennu.core.util.BundleUtil;
 import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;
 
 public enum MissionState implements IPresentableEnum {
 
     APPROVAL {
         @Override
         public MissionStateProgress getStateProgress(MissionProcess missionProcess) {
             if (missionProcess.isApprovedByResponsible()) {
                 return MissionStateProgress.COMPLETED;
             }
             if (missionProcess.isUnderConstruction() || missionProcess.isCanceled()) {
                 return MissionStateProgress.IDLE;
             }
             return MissionStateProgress.PENDING;
         }
     },
 
     VEHICLE_APPROVAL {
         @Override
         public boolean isRequired(MissionProcess missionProcess) {
             return missionProcess.hasAnyVehicleItems();
         }
 
         @Override
         public MissionStateProgress getStateProgress(MissionProcess missionProcess) {
             if (!APPROVAL.isCompleted(missionProcess)) {
                 return MissionStateProgress.IDLE;
             }
 
             if (!isRequired(missionProcess)) {
                 return MissionStateProgress.COMPLETED;
             }
             if (missionProcess.getMission().areAllVehicleItemsAuthorized()) {
                 return MissionStateProgress.COMPLETED;
             }
 
             // is no longer needed after ParticipationAuthorization
             // PARTICIPATION_AUTHORIZATION State cannot be use to avoid a circular dependency
             if (missionProcess.areAllParticipantsAuthorized()) {
                 return MissionStateProgress.COMPLETED;
             }
 
             if (missionProcess.isCanceled()) {
                 return MissionStateProgress.IDLE;
             }
             return MissionStateProgress.PENDING;
         }
     },
 
     FUND_ALLOCATION {
         @Override
         public boolean isRequired(MissionProcess missionProcess) {
             return missionProcess.hasAnyMissionItems();
         }
 
         @Override
         public MissionStateProgress getStateProgress(MissionProcess missionProcess) {
             if (!VEHICLE_APPROVAL.isCompleted(missionProcess)) {
                 return MissionStateProgress.IDLE;
             }
 
             if (missionProcess.isCanceled()) {
                 if (!missionProcess.hasAnyAllocatedFunds() && !missionProcess.hasAnyAllocatedProjectFunds()) {
                     return MissionStateProgress.IDLE;
                 }
                 return MissionStateProgress.PENDING;
             }
 
            if (!isRequired(missionProcess)) {
                return MissionStateProgress.COMPLETED;
            }
             if (missionProcess.hasAllAllocatedFunds() && missionProcess.hasAllCommitmentNumbers()
                     && (!missionProcess.hasAnyProjectFinancer() || missionProcess.hasAllAllocatedProjectFunds())) {
                 return MissionStateProgress.COMPLETED;
             }
 
             return MissionStateProgress.PENDING;
         }
     },
 
     PARTICIPATION_AUTHORIZATION {
         @Override
         public MissionStateProgress getStateProgress(MissionProcess missionProcess) {
             if (!FUND_ALLOCATION.isCompleted(missionProcess)) {
                 return MissionStateProgress.IDLE;
             }
 
             if (missionProcess.areAllParticipantsAuthorized()) {
                 return MissionStateProgress.COMPLETED;
             }
 
             if (missionProcess.isCanceled()) {
                 return MissionStateProgress.IDLE;
             }
             return MissionStateProgress.PENDING;
         }
     },
 
     EXPENSE_AUTHORIZATION {
         @Override
         public boolean isRequired(MissionProcess missionProcess) {
             return missionProcess.hasAnyMissionItems();
         }
 
         @Override
         public MissionStateProgress getStateProgress(MissionProcess missionProcess) {
             if (!PARTICIPATION_AUTHORIZATION.isCompleted(missionProcess)) {
                 return MissionStateProgress.IDLE;
             }
 
             if (!isRequired(missionProcess)) {
                 return MissionStateProgress.COMPLETED;
             }
             if (missionProcess.isAuthorized()) {
                 return MissionStateProgress.COMPLETED;
             }
 
             if (missionProcess.isCanceled()) {
                 return MissionStateProgress.IDLE;
             }
             return MissionStateProgress.PENDING;
         }
     },
 
     PERSONAL_INFORMATION_PROCESSING {
         @Override
         public boolean isRequired(MissionProcess missionProcess) {
             return missionProcess.isPersonalInformationProcessingNeeded();
         }
 
         @Override
         public MissionStateProgress getStateProgress(MissionProcess missionProcess) {
             if (!EXPENSE_AUTHORIZATION.isCompleted(missionProcess)) {
                 return MissionStateProgress.IDLE;
             }
 
             if (!isRequired(missionProcess)) {
                 return MissionStateProgress.COMPLETED;
             }
             if (missionProcess.isPersonalInformationProcessed()) {
                 return MissionStateProgress.COMPLETED;
             }
 
             return MissionStateProgress.PENDING;
         }
     },
 
     ARCHIVED {
         @Override
         public MissionStateProgress getStateProgress(MissionProcess missionProcess) {
             if (missionProcess.isArchived()) {
                 return MissionStateProgress.COMPLETED;
             }
 
             if (!missionProcess.isTerminated()) {
                 return MissionStateProgress.IDLE;
             }
             return MissionStateProgress.PENDING;
         }
     };
 
     private static final String BUNDLE = "resources.MissionResources";
     private static final String KEY_PREFIX = "label.MissionState.";
     private static final String KEY_PREFIX_DESCRIPTION = "label.MissionState.description.";
 
     @Override
     public String getLocalizedName() {
         final String key = KEY_PREFIX + name();
         return BundleUtil.getStringFromResourceBundle(BUNDLE, key);
     }
 
     public String getLocalizedDescription() {
         final String key = KEY_PREFIX_DESCRIPTION + name();
         return BundleUtil.getStringFromResourceBundle(BUNDLE, key);
     }
 
     public boolean isRequired(MissionProcess missionProcess) {
         return true;
     }
 
     public boolean isCompleted(MissionProcess missionProcess) {
         return getStateProgress(missionProcess) == MissionStateProgress.COMPLETED;
     }
 
     public abstract MissionStateProgress getStateProgress(MissionProcess missionProcess);
 }
