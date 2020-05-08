 package module.workingCapital.domain;
 
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import module.workflow.domain.ProcessFile;
 import module.workflow.domain.WorkflowProcess;
 import module.workflow.domain.utils.WorkflowCommentCounter;
 import module.workflow.util.HasPresentableProcessState;
 import module.workflow.util.PresentableProcessState;
 import module.workflow.widgets.UnreadCommentsWidget;
 import module.workingCapital.domain.activity.AcceptResponsabilityForWorkingCapitalActivity;
 import module.workingCapital.domain.activity.AllocateFundsActivity;
 import module.workingCapital.domain.activity.ApproveActivity;
 import module.workingCapital.domain.activity.ApproveWorkingCapitalAcquisitionActivity;
 import module.workingCapital.domain.activity.AuthorizeActivity;
 import module.workingCapital.domain.activity.CancelReenforceWorkingCapitalInitializationActivity;
 import module.workingCapital.domain.activity.CancelWorkingCapitalAcquisitionActivity;
 import module.workingCapital.domain.activity.CancelWorkingCapitalInitializationActivity;
 import module.workingCapital.domain.activity.ChangeWorkingCapitalAccountingUnitActivity;
 import module.workingCapital.domain.activity.CorrectWorkingCapitalAcquisitionClassificationActivity;
 import module.workingCapital.domain.activity.EditInitializationActivity;
 import module.workingCapital.domain.activity.EditWorkingCapitalActivity;
 import module.workingCapital.domain.activity.ExceptionalCapitalRestitutionActivity;
 import module.workingCapital.domain.activity.PayCapitalActivity;
 import module.workingCapital.domain.activity.ReenforceWorkingCapitalInitializationActivity;
 import module.workingCapital.domain.activity.RegisterCapitalRefundActivity;
 import module.workingCapital.domain.activity.RegisterWorkingCapitalAcquisitionActivity;
 import module.workingCapital.domain.activity.RejectVerifyWorkingCapitalAcquisitionActivity;
 import module.workingCapital.domain.activity.RejectWorkingCapitalAcquisitionActivity;
 import module.workingCapital.domain.activity.RejectWorkingCapitalInitializationActivity;
 import module.workingCapital.domain.activity.RequestCapitalActivity;
 import module.workingCapital.domain.activity.RequestCapitalRestitutionActivity;
 import module.workingCapital.domain.activity.SubmitForValidationActivity;
 import module.workingCapital.domain.activity.TerminateWorkingCapitalActivity;
 import module.workingCapital.domain.activity.UnAllocateFundsActivity;
 import module.workingCapital.domain.activity.UnApproveActivity;
 import module.workingCapital.domain.activity.UnApproveWorkingCapitalAcquisitionActivity;
 import module.workingCapital.domain.activity.UnAuthorizeActivity;
 import module.workingCapital.domain.activity.UnRequestCapitalActivity;
 import module.workingCapital.domain.activity.UnRequestCapitalRestitutionActivity;
 import module.workingCapital.domain.activity.UnTerminateWorkingCapitalActivity;
 import module.workingCapital.domain.activity.UnVerifyActivity;
 import module.workingCapital.domain.activity.UnVerifyWorkingCapitalAcquisitionActivity;
 import module.workingCapital.domain.activity.UndoCancelOrRejectWorkingCapitalInitializationActivity;
 import module.workingCapital.domain.activity.VerifyActivity;
 import module.workingCapital.domain.activity.VerifyWorkingCapitalAcquisitionActivity;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.RoleType;
 import myorg.domain.User;
 import myorg.domain.VirtualHost;
 import myorg.util.BundleUtil;
 import myorg.util.ClassNameBundle;
 import pt.ist.emailNotifier.domain.Email;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 
 @ClassNameBundle(key = "label.module.workingCapital", bundle = "resources/WorkingCapitalResources")
 public class WorkingCapitalProcess extends WorkingCapitalProcess_Base implements HasPresentableProcessState {
 
     public static final Comparator<WorkingCapitalProcess> COMPARATOR_BY_UNIT_NAME = new Comparator<WorkingCapitalProcess>() {
 	@Override
 	public int compare(WorkingCapitalProcess o1, WorkingCapitalProcess o2) {
 	    final int c = Collator.getInstance().compare(o1.getWorkingCapital().getUnit().getName(),
 		    o2.getWorkingCapital().getUnit().getName());
 	    return c == 0 ? o2.hashCode() - o1.hashCode() : c;
 	}
     };
 
     private static final List<WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> activities;
 
     static {
 	final List<WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> activitiesAux = new ArrayList<WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>>();
 	activitiesAux.add(new AcceptResponsabilityForWorkingCapitalActivity());
 	activitiesAux.add(new CancelWorkingCapitalInitializationActivity());
 	activitiesAux.add(new EditInitializationActivity());
 	activitiesAux.add(new ChangeWorkingCapitalAccountingUnitActivity());
 	activitiesAux.add(new ApproveActivity());
 	activitiesAux.add(new UnApproveActivity());
 	activitiesAux.add(new VerifyActivity());
 	activitiesAux.add(new UnVerifyActivity());
 	activitiesAux.add(new AllocateFundsActivity());
 	activitiesAux.add(new UnAllocateFundsActivity());
 	activitiesAux.add(new AuthorizeActivity());
 	activitiesAux.add(new UnAuthorizeActivity());
 	activitiesAux.add(new RejectWorkingCapitalInitializationActivity());
 	activitiesAux.add(new UndoCancelOrRejectWorkingCapitalInitializationActivity());
 	activitiesAux.add(new RequestCapitalActivity());
 	activitiesAux.add(new UnRequestCapitalActivity());
 	activitiesAux.add(new PayCapitalActivity());
 	activitiesAux.add(new RegisterWorkingCapitalAcquisitionActivity());
 	activitiesAux.add(new CancelWorkingCapitalAcquisitionActivity());
 	activitiesAux.add(new EditWorkingCapitalActivity());
 	activitiesAux.add(new CorrectWorkingCapitalAcquisitionClassificationActivity());
 	activitiesAux.add(new ApproveWorkingCapitalAcquisitionActivity());
 	activitiesAux.add(new RejectWorkingCapitalAcquisitionActivity());
 	activitiesAux.add(new UnApproveWorkingCapitalAcquisitionActivity());
 	activitiesAux.add(new VerifyWorkingCapitalAcquisitionActivity());
 	activitiesAux.add(new RejectVerifyWorkingCapitalAcquisitionActivity());
 	activitiesAux.add(new UnVerifyWorkingCapitalAcquisitionActivity());
 	activitiesAux.add(new SubmitForValidationActivity());
 	activitiesAux.add(new RequestCapitalRestitutionActivity());
 	activitiesAux.add(new UnRequestCapitalRestitutionActivity());
 	activitiesAux.add(new TerminateWorkingCapitalActivity());
 	activitiesAux.add(new UnTerminateWorkingCapitalActivity());
 	activitiesAux.add(new RegisterCapitalRefundActivity());
 	activitiesAux.add(new ReenforceWorkingCapitalInitializationActivity());
 	activitiesAux.add(new CancelReenforceWorkingCapitalInitializationActivity());
 	activitiesAux.add(new ExceptionalCapitalRestitutionActivity());
 	activities = Collections.unmodifiableList(activitiesAux);
 
 	UnreadCommentsWidget.register(new WorkflowCommentCounter(WorkingCapitalProcess.class));
     }
 
     public WorkingCapitalProcess() {
 	super();
     }
 
     public WorkingCapitalProcess(final WorkingCapital workingCapital) {
 	this();
 	setWorkingCapital(workingCapital);
     }
 
     @Override
     public <T extends WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> List<T> getActivities() {
 	return (List) activities;
     }
 
     @Override
     public boolean isActive() {
 	return true;
     }
 
     @Override
     public boolean isAccessible(final User user) {
 	final WorkingCapital workingCapital = getWorkingCapital();
 	return user != null
 		&& user.hasPerson()
 		&& (user.hasRoleType(RoleType.MANAGER)
 			|| (user.hasExpenditurePerson() && ExpenditureTrackingSystem
 				.isAcquisitionsProcessAuditorGroupMember(user))
 			|| (workingCapital.hasMovementResponsible() && user.getPerson() == workingCapital
 				.getMovementResponsible()) || workingCapital.isRequester(user)
 			|| workingCapital.getWorkingCapitalSystem().isManagementMember(user)
 			|| workingCapital.isAnyAccountingEmployee(user) || workingCapital.isAccountingResponsible(user)
 			|| workingCapital.isTreasuryMember(user) || workingCapital.isResponsibleFor(user));
     }
 
     public boolean isPendingAproval(final User user) {
 	return getWorkingCapital().isPendingAproval(user);
     }
 
     public boolean isPendingDirectAproval(final User user) {
 	return getWorkingCapital().isPendingDirectAproval(user);
     }
 
     public boolean isPendingVerification(User user) {
 	return getWorkingCapital().isPendingVerification(user);
     }
 
     public boolean isPendingFundAllocation(User user) {
 	return getWorkingCapital().isPendingFundAllocation(user);
     }
 
     public boolean isPendingAuthorization(User user) {
 	return getWorkingCapital().isPendingAuthorization(user);
     }
 
     @Override
     public User getProcessCreator() {
 	return getWorkingCapital().getRequester();
     }
 
     @Override
     public void notifyUserDueToComment(final User user, final String comment) {
 	List<String> toAddress = new ArrayList<String>();
 	toAddress.clear();
 	final String email = user.getExpenditurePerson().getEmail();
 	if (email != null) {
 	    toAddress.add(email);
 
 	    final User loggedUser = UserView.getCurrentUser();
 	    final WorkingCapital workingCapital = getWorkingCapital();
 	    final VirtualHost virtualHost = VirtualHost.getVirtualHostForThread();
 	    new Email(virtualHost.getApplicationSubTitle().getContent(), virtualHost.getSystemEmailAddress(), new String[] {},
 		    toAddress, Collections.EMPTY_LIST, Collections.EMPTY_LIST, BundleUtil.getFormattedStringFromResourceBundle(
 			    "resources/WorkingCapitalResources", "label.email.commentCreated.subject", workingCapital.getUnit()
 				    .getPresentationName(), workingCapital.getWorkingCapitalYear().getYear().toString()),
 		    BundleUtil.getFormattedStringFromResourceBundle("resources/WorkingCapitalResources",
 			    "label.email.commentCreated.body",
 			    loggedUser.getPerson().getName(),
 			    workingCapital.getUnit().getPresentationName(),
			    workingCapital.getWorkingCapitalYear().getYear().toString(), comment),
			    VirtualHost.getVirtualHostForThread().getHostname());
 	}
     }
 
     @Override
     public List<Class<? extends ProcessFile>> getAvailableFileTypes() {
 	List<Class<? extends ProcessFile>> availableFileTypes = super.getAvailableFileTypes();
 	availableFileTypes.add(WorkingCapitalInvoiceFile.class);
 	return availableFileTypes;
     }
 
     @Override
     public List<Class<? extends ProcessFile>> getUploadableFileTypes() {
 	return super.getAvailableFileTypes();
     }
 
     public void submitAcquisitionsForValidation() {
 	final WorkingCapital workingCapital = getWorkingCapital();
 	workingCapital.submitAcquisitionsForValidation();
     }
 
     public void unsubmitAcquisitionsForValidation() {
 	final WorkingCapital workingCapital = getWorkingCapital();
 	workingCapital.unsubmitAcquisitionsForValidation();
     }
 
     @Override
     public List<Class<? extends ProcessFile>> getDisplayableFileTypes() {
 	final List<Class<? extends ProcessFile>> fileTypes = new ArrayList<Class<? extends ProcessFile>>();
 	fileTypes.addAll(super.getDisplayableFileTypes());
 	fileTypes.remove(WorkingCapitalInvoiceFile.class);
 	return fileTypes;
     }
 
     @Override
     public boolean isTicketSupportAvailable() {
 	return false;
     }
 
     @Override
     public List<? extends PresentableProcessState> getAvailablePresentableStates() {
 	return Arrays.asList(WorkingCapitalProcessState.values());
     }
 
     @Override
     public PresentableProcessState getPresentableAcquisitionProcessState() {
 	return getWorkingCapital().getPresentableAcquisitionProcessState();
     }
 
     @Override
     public boolean isConnectedToCurrentHost() {
 	return getWorkingCapitalSystem() == WorkingCapitalSystem.getInstanceForCurrentHost();
     }
 
     public WorkingCapitalSystem getWorkingCapitalSystem() {
 	return getWorkingCapital().getWorkingCapitalSystem();
     }
 }
