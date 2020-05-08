 package module.mission.domain;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import module.mission.domain.util.MissionStageView;
 import module.organization.domain.Accountability;
 import module.organization.domain.AccountabilityType;
 import module.organization.domain.Person;
 import module.workflow.activities.GiveProcess.NotifyUser;
 import module.workflow.activities.WorkflowActivity;
 import module.workflow.domain.WorkflowProcess;
 import module.workflow.domain.WorkflowProcessComment;
 import module.workflow.domain.WorkflowQueue;
 import module.workflow.domain.utils.WorkflowCommentCounter;
 import module.workflow.widgets.UnreadCommentsWidget;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.RoleType;
 import myorg.domain.User;
 import myorg.domain.exceptions.DomainException;
 import myorg.util.BundleUtil;
 import myorg.util.ClassNameBundle;
 
 import org.joda.time.DateTime;
 
 import pt.ist.emailNotifier.domain.Email;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.ProcessState;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 
 @ClassNameBundle(bundle = "resources/MissionResources")
 public abstract class MissionProcess extends MissionProcess_Base {
 
     static {
 	UnreadCommentsWidget.register(new WorkflowCommentCounter(MissionProcess.class));
     }
 
     public static final Comparator<MissionProcess> COMPARATOR_BY_PROCESS_NUMBER = new Comparator<MissionProcess>() {
 	@Override
 	public int compare(final MissionProcess o1, final MissionProcess o2) {
 	    final MissionYear missionYear1 = o1.getMissionYear();
 	    final MissionYear missionYear2 = o2.getMissionYear();
 
 	    final int year = MissionYear.COMPARATOR_BY_YEAR.compare(missionYear1, missionYear2);
 	    if (year != 0) {
 		return year;
 	    }
 
 	    final int number = compareNumber(o1, o2);
 	    return number == 0 ? o1.getExternalId().compareTo(o2.getExternalId()) : number;
 	}
 
 	private int compareNumber(final MissionProcess o1, final MissionProcess o2) {
 	    final int n1 = toNum(o1.getProcessNumber());
 	    final int n2 = toNum(o2.getProcessNumber());
 	    return n2 - n1;
 	}
 
 	private int toNum(final String processNumber) {
 	    final String relevantPart = Character.isDigit(processNumber.charAt(processNumber.length() - 1))
 	    		? processNumber : processNumber.substring(0, processNumber.length() - 1);
 	    return Integer.parseInt(relevantPart);
 	}
 
     };
 
     protected static class MissionGiveProcessUserNotifier extends NotifyUser {
 
 	@Override
 	public void notifyUser(final User user, final WorkflowProcess process) {
 	    final pt.ist.expenditureTrackingSystem.domain.organization.Person person = user.getExpenditurePerson();
 	    if (person != null) {
 		final String email = person.getEmail();
 		if (email != null && !email.isEmpty()) {
 		    final MissionProcess missionProcess = (MissionProcess) process;
 		    final User currentUser = UserView.getCurrentUser();
 		    final pt.ist.expenditureTrackingSystem.domain.organization.Person currentPerson = currentUser == null ? null
 			    : currentUser.getExpenditurePerson();
 
 		    final StringBuilder body = new StringBuilder("Caro utilizador, foi-lhe passado o processo de missão ");
 		    body.append(missionProcess.getProcessIdentification());
 		    body.append(", que pode ser consultado em http://dot.ist.utl.pt/.");
 		    if (currentPerson != null) {
 			body.append(" A passagem do processo foi efectuado por ");
 			body.append(currentPerson.getName());
 			body.append(".\n\n");
 		    }
 
 		    final Set<WorkflowProcessComment> commentsSet = new TreeSet<WorkflowProcessComment>(
 			    WorkflowProcessComment.REVERSE_COMPARATOR);
 		    commentsSet.addAll(process.getCommentsSet());
 		    if (!commentsSet.isEmpty()) {
 			body.append("O processo contem os seguintes comentários:\n\n");
 			for (final WorkflowProcessComment workflowProcessComment : commentsSet) {
 			    final String comment = workflowProcessComment.getComment();
 			    final DateTime date = workflowProcessComment.getDate();
 			    final User commenter = workflowProcessComment.getCommenter();
 
 			    body.append(date.toString("yyyy-MM-dd HH:mm"));
 			    body.append(" - ");
 			    body.append(commenter.getPresentationName());
 			    body.append("\n");
 			    body.append(comment);
 			    body.append("\n\n");
 			}
 		    }
 
 		    body.append("\n---\n");
 		    body.append("Esta mensagem foi enviada por meio das Aplicações Centrais do IST.\n");
 
 		    final Collection<String> toAddress = Collections.singleton(email);
 		    new Email("Aplicações Centrais do IST", "noreply@ist.utl.pt", new String[] {}, toAddress,
 			    Collections.EMPTY_LIST, Collections.EMPTY_LIST, "Passagem de Processo Pendentes - Missões",
 			    body.toString());
 		}
 	    }
 	}
 
     }
 
     public MissionProcess() {
 	super();
 	setMissionSystem(MissionSystem.getInstance());
 	final MissionYear missionYear = MissionYear.getCurrentYear();
 	setMissionYear(missionYear);
 	setProcessNumber(missionYear.nextNumber().toString());
 	setIsUnderConstruction(Boolean.TRUE);
 	setIsCanceled(Boolean.FALSE);
     }
 
     public String getProcessIdentification() {
 	// TODO : Only uncomment this when ADIST an IST-ID are to be placed in production
 	//final ExpenditureTrackingSystem instance = ExpenditureTrackingSystem.getInstance();
 	//return instance.getInstitutionalProcessNumberPrefix() + "/" + getMissionYear().getYear() + "/M" + getProcessNumber();
 	return getMissionYear().getYear() + "/" + getProcessNumber();
     }
 
     public WorkflowActivity getActivity(Class<? extends WorkflowActivity> clazz) {
 	for (final WorkflowActivity workflowActivity : getActivities()) {
 	    if (workflowActivity.getClass() == clazz) {
 		return workflowActivity;
 	    }
 	}
 	return null;
     }
 
     public boolean isRequestor(final User user) {
 	final Mission mission = getMission();
 	return mission.getRequestingPerson() == user.getPerson();
     }
 
     public boolean isMissionResponsible(User user) {
 	final Mission mission = getMission();
 	return mission.getMissionResponsible() == user.getPerson();
     }
 
     public boolean isUnderConstruction() {
 	return getIsUnderConstruction().booleanValue() && !isProcessCanceled();
     }
 
     public boolean isProcessCanceled() {
 	final Boolean isCanceled = getIsCanceled();
 	return isCanceled != null && isCanceled.booleanValue();
     }
 
     public boolean hasAnyAproval() {
 	final Mission mission = getMission();
 	return mission.hasAnyAproval();
     }
 
     public boolean isPendingApproval() {
 	final Mission mission = getMission();
 	return mission.isPendingApproval();
     }
 
     public boolean isPendingApprovalBy(final User user) {
 	final Mission mission = getMission();
 	return mission.isPendingApprovalBy(user);
     }
 
     public boolean isPendingAuthorizationBy(final User user) {
 	final Mission mission = getMission();
 	return mission.isPendingAuthorizationBy(user);
     }
 
     public boolean isPendingDirectAuthorizationBy(final User user) {
 	final Mission mission = getMission();
 	return mission.isPendingDirectAuthorizationBy(user);
     }
 
     public boolean canRemoveApproval(final User user) {
 	final Mission mission = getMission();
 	return mission.canRemoveApproval(user);
     }
 
     public boolean canRemoveAuthorization(final User user) {
 	final Mission mission = getMission();
 	return mission.canRemoveAuthorization(user);
     }
 
     public void approve(final User user) {
 	final Mission mission = getMission();
 	mission.approve(user);
     }
 
     public void authorize(final User user) {
 	final Mission mission = getMission();
 	mission.authorize(user);
     }
 
     public void unapprove(final User user) {
 	final Mission mission = getMission();
 	mission.unapprove(user);
     }
 
     public void unauthorize(final User user) {
 	final Mission mission = getMission();
 	mission.unauthorize(user);
     }
 
     public boolean isApproved() {
 	final Mission mission = getMission();
 	return mission.isApproved();
     }
 
     public boolean hasAllAllocatedFunds() {
 	final Mission mission = getMission();
 	return mission.hasAllAllocatedFunds();
     }
 
     public boolean hasAllAllocatedProjectFunds() {
 	final Mission mission = getMission();
 	return mission.hasAllAllocatedProjectFunds();
     }
 
     public boolean hasAnyAllocatedFunds() {
 	final Mission mission = getMission();
 	return mission.hasAnyAllocatedFunds();
     }
 
     public void unAllocateFunds(final Person person) {
 	final Mission mission = getMission();
 	mission.unAllocateFunds(person);
     }
 
     public boolean hasAnyAuthorization() {
 	final Mission mission = getMission();
 	return mission.hasAnyAuthorization();
     }
 
     public boolean canAuthoriseParticipantActivity() {
 	final Mission mission = getMission();
 	return mission.canAuthoriseParticipantActivity();
     }
 
     public boolean canUnAuthoriseParticipantActivity() {
 	final Mission mission = getMission();
 	return mission.canUnAuthoriseParticipantActivity();
     }
 
     public boolean canUnAuthoriseSomeParticipantActivity() {
 	final Mission mission = getMission();
 	return mission.canUnAuthoriseSomeParticipantActivity();
     }
 
     public boolean canAllocateFund() {
 	final Mission mission = getMission();
 	return mission.canAllocateFund();
     }
 
     public boolean canAllocateProjectFund() {
 	final Mission mission = getMission();
 	return mission.canAllocateProjectFund();
     }
 
     public boolean isDirectResponsibleForPendingProjectFundAllocation() {
 	final Mission mission = getMission();
 	return mission.isDirectResponsibleForPendingProjectFundAllocation();
     }
 
     public boolean hasAnyAuthorizedParticipants() {
 	final Mission mission = getMission();
 	return mission.hasAnyAuthorizedParticipants();
     }
 
     public boolean hasAnyParticipantes() {
 	final Mission mission = getMission();
 	return mission.hasAnyParticipantes();
     }
 
     public boolean hasAnyMissionItems() {
 	final Mission mission = getMission();
 	return mission.hasAnyMissionItems();
     }
 
     public boolean hasAnyAllocatedProjectFunds() {
 	final Mission mission = getMission();
 	return mission.hasAnyAllocatedProjectFunds();
     }
 
     public void unAllocateProjectFunds(Person person) {
 	final Mission mission = getMission();
 	mission.unAllocateProjectFunds(person);
     }
 
     public boolean hasAnyProjectFinancer() {
 	final Mission mission = getMission();
 	return mission.hasAnyProjectFinancer();
     }
 
     public boolean isConsistent() {
 	final Mission mission = getMission();
 	return mission.isConsistent();
     }
 
     @Override
     public User getProcessCreator() {
 	return getMission().getRequestingPerson().getUser();
     }
 
     @Override
     public void notifyUserDueToComment(final User user, final String comment) {
 	List<String> toAddress = new ArrayList<String>();
 	toAddress.clear();
 	final String email = user.getExpenditurePerson().getEmail();
 	if (email != null) {
 	    toAddress.add(email);
 
 	    final User loggedUser = UserView.getCurrentUser();
 	    new Email("Aplicações Centrais do IST", "noreply@ist.utl.pt", new String[] {}, toAddress, Collections.EMPTY_LIST,
 		    Collections.EMPTY_LIST, BundleUtil.getFormattedStringFromResourceBundle("resources/MissionResources",
 			    "label.email.commentCreated.subject", getProcessIdentification()),
 		    BundleUtil.getFormattedStringFromResourceBundle("resources/MissionResources",
 			    "label.email.commentCreated.body", loggedUser.getPerson().getName(), getProcessIdentification(),
 			    comment));
 	}
     }
 
     public void setProcessParticipantInformationQueue() {
 	final Mission mission = getMission();
 	if (mission.allParticipantsAreAuthorized()) {
 	    addToProcessParticipantInformationQueue();
 	}
     }
 
     public void addToProcessParticipantInformationQueue() {
 	final Mission mission = getMission();
 	final Set<AccountabilityType> accountabilityTypes = new HashSet<AccountabilityType>();
 	for (final Person person : mission.getParticipantesSet()) {
 	    for (final Accountability accountability : person.getParentAccountabilitiesSet()) {
 		if (accountability.isValid()) {
 		    final AccountabilityType accountabilityType = accountability.getAccountabilityType();
 		    accountabilityTypes.add(accountabilityType);
 		}
 	    }
 	}
 	for (final AccountabilityTypeQueue accountabilityTypeQueue : MissionSystem.getInstance().getAccountabilityTypeQueuesSet()) {
 	    if (accountabilityTypes.contains(accountabilityTypeQueue.getAccountabilityType())) {
 		final WorkflowQueue workflowQueue = accountabilityTypeQueue.getWorkflowQueue();
 		workflowQueue.addProcess(this);
 	    }
 	}
     }
 
     public void removeFromParticipantInformationQueues() {
 	for (final AccountabilityTypeQueue accountabilityTypeQueue : MissionSystem.getInstance().getAccountabilityTypeQueuesSet()) {
 	    final WorkflowQueue workflowQueue = accountabilityTypeQueue.getWorkflowQueue();
 	    workflowQueue.removeProcess(this);
 	}
     }
 
     public boolean isOnTime() {
 	return getMission().isOnTime();
     }
 
     public void justifyLateSubmission(final String justification) {
 	if (justification == null || justification.isEmpty()) {
 	    throw new DomainException("justification.cannot.be.null");
 	}
 	final MissionProcessLateJustification lastJustification = getLastMissionProcessLateJustification();
 	if (lastJustification == null || !lastJustification.getJustification().equals(justification)) {
 	    new MissionProcessLateJustification(this, justification);
 	}
     }
 
     public MissionProcessLateJustification getLastMissionProcessLateJustification() {
 	final Set<MissionProcessLateJustification> justifications = getMissionProcessLateJustificationsSet();
 	if (justifications.isEmpty()) {
 	    return null;
 	}
 	return Collections.max(justifications, MissionProcessLateJustification.COMPARATOR_BY_JUSTIFICATION_DATETIME);
     }
 
     public SortedSet<MissionProcessLateJustification> getOrderedMissionProcessLateJustificationsSet() {
 	final SortedSet<MissionProcessLateJustification> result = new TreeSet<MissionProcessLateJustification>(
 		MissionProcessLateJustification.COMPARATOR_BY_JUSTIFICATION_DATETIME);
 	result.addAll(getMissionProcessLateJustificationsSet());
 	return result;
     }
 
     public boolean areAllParticipantsAuthorized() {
 	final Mission mission = getMission();
 	return mission.areAllParticipantsAuthorized();
     }
 
     public boolean getAreAllParticipantsAuthorized() {
 	return areAllParticipantsAuthorized();
     }
 
     public boolean areAllParticipantsAuthorizedForPhaseOne() {
 	final Mission mission = getMission();
 	return mission.areAllParticipantsAuthorizedForPhaseOne();
     }
 
     public int getPersonAuthorizationChainSize(final Person person) {
 	final Mission mission = getMission();
 	return mission.getPersonAuthorizationChainSize(person);
     }
 
     public boolean hasAnyActivePaymentProcess() {
 	for (final PaymentProcess paymentProcess : getPaymentProcessSet()) {
 	    if (paymentProcess.isActive()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isExpenditureAuthorized() {
 	final Mission mission = getMission();
 	return mission.isExpenditureAuthorized();
     }
 
     public void cancel() {
 	setIsCanceled(Boolean.TRUE);
     }
 
     public boolean isCanceled() {
 	return getIsCanceled().booleanValue();
     }
 
     public String getPresentationName() {
 	return getProcessIdentification() + " - ";
     }
 
     public boolean isPendingParticipantAuthorisationBy(final User user) {
 	final Mission mission = getMission();
 	return mission.isPendingParticipantAuthorisationBy(user);
     }
 
     public boolean isAuthorized() {
 	final Mission mission = getMission();
 	return mission.isAuthorized();
     }
 
     public MissionStageView getMissionStageView() {
 	return new MissionStageView(this);
     }
 
     public boolean isAccountingEmployee(final pt.ist.expenditureTrackingSystem.domain.organization.Person expenditurePerson) {
 	final Mission mission = getMission();
 	return mission.isAccountingEmployee(expenditurePerson);
     }
 
     public boolean isProjectAccountingEmployee(final pt.ist.expenditureTrackingSystem.domain.organization.Person expenditurePerson) {
 	final Mission mission = getMission();
 	return mission.isProjectAccountingEmployee(expenditurePerson);
     }
 
     public boolean isDirectProjectAccountingEmployee(final pt.ist.expenditureTrackingSystem.domain.organization.Person expenditurePerson) {
 	final Mission mission = getMission();
 	return mission.isDirectProjectAccountingEmployee(expenditurePerson);
     }
 
     public boolean isCurrentUserAbleToAccessQueue() {
 	for (final WorkflowQueue workflowQueue : getQueueHistorySet()) {
 	    if (workflowQueue.isCurrentUserAbleToAccessQueue()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     @Override
     public boolean isAccessible(final User user) {
 	final Person person = user.getPerson();
 	final Mission mission = getMission();
 	return isTakenByPerson(user)
 		|| getProcessCreator() == user
 		|| mission.getRequestingPerson() == person
 		|| user.hasRoleType(RoleType.MANAGER)
 		|| (user.hasExpenditurePerson()
 			&& ExpenditureTrackingSystem.isAcquisitionCentralGroupMember(user))
 		|| (user.hasExpenditurePerson()
 			&& ExpenditureTrackingSystem.isAcquisitionsProcessAuditorGroupMember(user))
 		|| (person != null && person.getMissionsSet().contains(mission))
 		|| mission.isParticipantResponsible(person)
 		|| mission.isFinancerResponsible(user.getExpenditurePerson())
 		|| mission.isFinancerAccountant(user.getExpenditurePerson())
 		|| mission.isPersonelSectionMember(user)
 		|| mission.getParticipantesSet().contains(person)
 		|| mission.isUnitObserver(user);
     }
 
     public boolean isReadyForMissionTermination() {
 	final Mission mission = getMission();
 	return mission.isReadyForMissionTermination() && (!hasAnyActivePaymentProcess() || allPaymentProcessesAreConcluded());
     }
 
     private boolean allPaymentProcessesAreConcluded() {
 	for (final PaymentProcess paymentProcess : getPaymentProcessSet()) {
 	    if (paymentProcess.isActive()) {
 		final ProcessState currentProcessState = paymentProcess.getCurrentProcessState();
 		if (!currentProcessState.isInFinalStage()) {
 		    return false;
 		}
 	    }
 	}
 	return true;
     }
 
     public boolean isReadyForMissionTermination(final User user) {
	return (isRequestor(user) || isMissionResponsible(user)) && isReadyForMissionTermination();
     }
 
     public void sendForProcessTermination(final String descriptionOfChangesAfterArrival) {
 	final Mission mission = getMission();
 	mission.sendForProcessTermination(descriptionOfChangesAfterArrival);
     }
 
     public boolean isTerminated() {
 	final Mission mission = getMission();
 	return mission.isTerminated();
     }
 
     public boolean isTerminatedWithChanges() {
 	final Mission mission = getMission();
 	return mission.isTerminatedWithChanges();
     }
 
     public boolean isArchived() {
 	final Mission mission = getMission();
 	return mission.isArchived();
     }
 
     public boolean canArchiveMission() {
 	final Mission mission = getMission();
 	return mission.canArchiveMission();
     }
 
     public boolean canArchiveMissionDirect() {
 	final Mission mission = getMission();
 	return mission.canArchiveMissionDirect();
     }
 
     public boolean hasNoItemsAndParticipantesAreAuthorized() {
 	final Mission mission = getMission();
 	return mission.hasNoItemsAndParticipantesAreAuthorized();
     }
 
     public void revertProcessTermination() {
 	final Mission mission = getMission();
 	mission.revertProcessTermination();
     }
 
 }
