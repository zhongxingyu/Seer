 package module.mission.domain.util;
 
 import module.mission.domain.MissionProcess;
 import module.mission.domain.MissionYear;
 import module.mission.domain.NationalMissionProcess;
 import module.workflow.domain.ProcessCounter;
 
 import org.jfree.data.time.Month;
 import org.joda.time.LocalDate;
 
 public class MissionPendingProcessCounter extends ProcessCounter {
 
     public MissionPendingProcessCounter() {
 	super(MissionProcess.class);
     }
 
     @Override
     public Class getProcessClassForForwarding() {
         return NationalMissionProcess.class;
     }
 
     @Override
     public int getCount() {
 	final MissionYear missionYear = MissionYear.getCurrentYear();
 	final LocalDate today = new LocalDate();
	final MissionYear previousYear = today.getMonthOfYear() == Month.JANUARY ? MissionYear.findMissionYear(today.getYear() - 1) : null;
 
 	final int takenByUser = missionYear.getTaken().size() + (previousYear == null ? 0 : previousYear.getTaken().size());
 	final int pendingApprovalCount = missionYear.getPendingAproval().size() + (previousYear == null ? 0 : previousYear.getPendingAproval().size());
 	final int pendingAuthorizationCount = missionYear.getPendingAuthorization().size() + (previousYear == null ? 0 : previousYear.getPendingAuthorization().size());
 	final int pendingFundAllocationCount = missionYear.getPendingFundAllocation().size() + (previousYear == null ? 0 : previousYear.getPendingFundAllocation().size());
     	final int pendingProcessingCount = missionYear.getPendingProcessingPersonelInformation().size() + (previousYear == null ? 0 : previousYear.getPendingProcessingPersonelInformation().size());
 
 	return takenByUser + pendingApprovalCount + pendingAuthorizationCount + pendingFundAllocationCount + pendingProcessingCount;
     }
 
 }
