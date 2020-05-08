 package module.mission.domain.activity;
 
 import module.mission.domain.MissionProcess;
 import module.workflow.activities.ActivityInformation;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 
 public class UnAuthorizeActivity extends MissionProcessActivity<MissionProcess, ActivityInformation<MissionProcess>> {
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle("resources/MissionResources", "activity." + getClass().getSimpleName());
     }
 
     @Override
     public boolean isActive(final MissionProcess missionProcess, final User user) { 
	return super.isActive(missionProcess, user)
		&& !missionProcess.getIsCanceled()
		&& missionProcess.canRemoveAuthorization(user)
 		&& !missionProcess.areAllParticipantsAuthorized()
 		&& !missionProcess.hasAnyActivePaymentProcess();
     }
 
     @Override
     protected void process(final ActivityInformation activityInformation) {
 	final MissionProcess missionProcess = (MissionProcess) activityInformation.getProcess();
 	final User user = UserView.getCurrentUser();
 	missionProcess.unauthorize(user);
     }
 
 }
