 package module.mission.domain.activity;
 
 import module.mission.domain.MissionProcess;
 import module.workflow.activities.ActivityInformation;
 import pt.ist.bennu.core.domain.User;
 import pt.ist.bennu.core.util.BundleUtil;
 
 public class UnCommitFundsActivity extends MissionProcessActivity<MissionProcess, UnCommitFundsActivityInformation> {
 
     @Override
     public String getLocalizedName() {
         return BundleUtil.getStringFromResourceBundle("resources/MissionResources", "activity." + getClass().getSimpleName());
     }
 
     @Override
     public boolean isActive(final MissionProcess missionProcess, final User user) {
        return super.isActive(missionProcess, user) && !missionProcess.getIsCanceled().booleanValue()
                && missionProcess.hasCommitmentNumber() && !missionProcess.hasAnyAuthorization()
                && missionProcess.isAccountingEmployee(user.getExpenditurePerson());

     }
 
     @Override
     protected void process(final UnCommitFundsActivityInformation unCommitFundsActivityInformation) {
         unCommitFundsActivityInformation.getMissionFinancer().setCommitmentNumber(null);
     }
 
     @Override
     public ActivityInformation<MissionProcess> getActivityInformation(final MissionProcess process) {
         return new UnCommitFundsActivityInformation(process, this);
     }
 
     @Override
     public boolean isVisible() {
         return false;
     }
 
 }
