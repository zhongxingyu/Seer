 package module.mission.domain.activity;
 
 import module.mission.domain.MissionProcess;
 import module.mission.domain.PersonMissionAuthorization;
 import module.workflow.activities.ActivityInformation;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 
 public class UnAuthoriseParticipantActivity extends MissionProcessActivity<MissionProcess, AuthoriseParticipantActivityInformation> {
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle("resources/MissionResources", "activity." + getClass().getSimpleName());
     }
 
     @Override
     public boolean isActive(final MissionProcess missionProcess, final User user) {
 	return super.isActive(missionProcess, user)
 		&& missionProcess.canUnAuthoriseSomeParticipantActivity()
 		&& missionProcess.hasAnyAuthorizedParticipants()
 		&& ((!missionProcess.hasCurrentQueue() && !missionProcess.areAllParticipantsAuthorized())
 			|| (missionProcess.hasCurrentQueue()))
 		&& !missionProcess.isTerminated();
     }
 
     @Override
     protected void process(final AuthoriseParticipantActivityInformation authoriseParticipantActivityInformation) {
 	final PersonMissionAuthorization personMissionAuthorization = authoriseParticipantActivityInformation.getPersonMissionAuthorization();
 	personMissionAuthorization.setAuthority(null);
 	personMissionAuthorization.setDelegatedAuthority(null);
 	final MissionProcess missionProcess = authoriseParticipantActivityInformation.getProcess();
 	missionProcess.removeFromParticipantInformationQueues();
     }
 
     @Override
     public ActivityInformation<MissionProcess> getActivityInformation(final MissionProcess process) {
 	return new AuthoriseParticipantActivityInformation(process, this);
     }
 
     @Override
     public boolean isDefaultInputInterfaceUsed() {
 	return true;
     }
 
     @Override
     public boolean isVisible() {
 	return false;
     }
 
 }
