 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.util.Collections;
 
 import org.joda.time.DateTime;
 
 import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.fenixWebFramework.security.UserView;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.pstm.Transaction;
 
 public class AcquisitionProcess extends AcquisitionProcess_Base {
 
     protected AcquisitionProcess() {
 	super();
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.IN_GENESIS);
 	new AcquisitionRequest(this);
     }
 
     public static boolean isCreateNewAcquisitionProcessAvailable() {
 	return UserView.getUser() != null;
     }
 
     @Service
     public static AcquisitionProcess createNewAcquisitionProcess() {
 	if (!isCreateNewAcquisitionProcessAvailable()) {
 	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.createNewAcquisitionProcess");
 	}
 	return new AcquisitionProcess();
     }
 
     public boolean isAcquisitionProposalDocumentAvailable() {
 	User user = UserView.getUser();
 	return user != null && isProcessInState(AcquisitionProcessStateType.IN_GENESIS)
 		&& user.getPerson().equals(getRequestor());
     }
 
     @Service
     public void addAcquisitionProposalDocument(final String filename, final byte[] bytes) {
 	if (!isAcquisitionProposalDocumentAvailable()) {
 	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.addAcquisitionProposalDocument");
 	}
 	final AcquisitionRequest acquisitionRequest = getAcquisitionRequest();
 	acquisitionRequest.addAcquisitionProposalDocument(filename, bytes);
     }
 
     public boolean isCreateAcquisitionRequestItemAvailable() {
 	User user = UserView.getUser();
 	return user != null && isProcessInState(AcquisitionProcessStateType.IN_GENESIS)
 		&& user.getPerson().equals(getRequestor());
     }
 
     @Service
     public AcquisitionRequestItem createAcquisitionRequestItem() {
 	if (!isCreateAcquisitionRequestItemAvailable()) {
 	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.createAcquisitionRequestItem");
 	}
 	final AcquisitionRequest acquisitionRequest = getAcquisitionRequest();
 	return acquisitionRequest.createAcquisitionRequestItem();
     }
 
     public boolean isSubmitForApprovalAvailable() {
 	User user = UserView.getUser();
 	return user != null && isProcessInState(AcquisitionProcessStateType.IN_GENESIS)
 		&& user.getPerson().equals(getRequestor()) && getAcquisitionRequest().isFilled();
     }
 
     @Service
     public void submitForApproval() {
 	if (!isSubmitForApprovalAvailable()) {
 	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.submitForApproval");
 	}
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.SUBMITTED_FOR_APPROVAL);
     }
 
     public Person getRequestor() {
 	return getAcquisitionRequest().getRequester();
     }
 
     public boolean isResponsibleForUnit() {
 	User user = UserView.getUser();
 	if (user == null) {
 	    return false;
 	}
 
 	String costCenter = getAcquisitionRequest().getCostCenter();
 
 	for (Authorization authorization : user.getPerson().getAuthorizations()) {
 	    if (authorization.getUnit().getCostCenter().equals((costCenter))) {
 		return true;
 	    }
 	}
 	return false;
 	
     }
 
     public boolean isApproveAvailable() {
 	return isPendingApproval() && isResponsibleForUnit();
     }
 
     @Service
     public void approve() {
 	if (!isApproveAvailable()) {
 	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.approve");
 	}
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.APPROVED);
     }
 
     public boolean isDeleteAvailable() {
 	User user = UserView.getUser();
 	return user != null && user.getPerson().equals(getRequestor())
 		&& isProcessInState(AcquisitionProcessStateType.IN_GENESIS);
     }
 
     @Service
     public void delete() {
 	final AcquisitionRequest acquisitionRequest = getAcquisitionRequest();
 	acquisitionRequest.delete();
 	removeExpenditureTrackingSystem();
 	Transaction.deleteObject(this);
     }
 
     public boolean isFundAllocationIdAvailable() {
 	User user = UserView.getUser();
 	return user != null && user.getPerson().hasRoleType(RoleType.ACCOUNTABILITY)
 		&& isProcessInState(AcquisitionProcessStateType.APPROVED);
     }
 
     @Override
     public void setFundAllocationId(final String fundAllocationId) {
 	if (!isFundAllocationIdAvailable()) {
 	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.setFundAllocationId");
 	}
 	super.setFundAllocationId(fundAllocationId);
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.FUNDS_ALLOCATED);
     }
 
     public boolean isFundAllocationExpirationDateAvailable() {
 	User user = UserView.getUser();
 	return user != null && user.getPerson().hasRoleType(RoleType.ACQUISITION_CENTRAL)
 		&& isProcessInState(AcquisitionProcessStateType.FUNDS_ALLOCATED);
     }
 
     @Override
     public void setFundAllocationExpirationDate(final DateTime fundAllocationExpirationDate) {
 	if (!isFundAllocationExpirationDateAvailable()) {
 	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.setFundAllocationExpirationDate");
 	}
 	super.setFundAllocationExpirationDate(fundAllocationExpirationDate);
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.FUNDS_ALLOCATED_TO_SERVICE_PROVIDER);
     }
 
     public boolean isEditRequestItemAvailable() {
 	User user = UserView.getUser();
 	return user != null && user.getPerson().equals(getRequestor())
 		&& isProcessInState(AcquisitionProcessStateType.IN_GENESIS);
     }
 
     public boolean isPendingApproval() {
 	return isProcessInState(AcquisitionProcessStateType.SUBMITTED_FOR_APPROVAL);
     }
 
     public boolean isCreateAcquisitionRequestAvailable() {
 	User user = UserView.getUser();
 	return user != null && user.getPerson().hasRoleType(RoleType.ACQUISITION_CENTRAL)
 		&& isProcessInState(AcquisitionProcessStateType.FUNDS_ALLOCATED_TO_SERVICE_PROVIDER);
     }
 
     @Service
     public AcquisitionRequestDocument createAcquisitionRequest() {
 	if (getAcquisitionRequest().getAcquisitionRequestDocument() != null) {
 	    return getAcquisitionRequest().getAcquisitionRequestDocument();
 	}
 
 	if (!isCreateAcquisitionRequestAvailable()) {
 	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.createAcquisitionRequest");
 	}
 
 	AcquisitionRequestDocument acquisitionRequestDocument = new AcquisitionRequestDocument(getAcquisitionRequest());
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.ACQUISITION_PROCESSED);
 	return acquisitionRequestDocument;
     }
 
     private boolean isProcessInState(AcquisitionProcessStateType state) {
 	return getLastAcquisitionProcessStateType().equals(state);
     }
 
     protected AcquisitionProcessState getLastAcquisitionProcessState() {
 	return Collections.max(getAcquisitionProcessStates(), AcquisitionProcessState.COMPARATOR_BY_WHEN);
     }
 
     protected AcquisitionProcessStateType getLastAcquisitionProcessStateType() {
 	return getLastAcquisitionProcessState().getAcquisitionProcessStateType();
     }
 
     public AcquisitionProcessState getAcquisitionProcessState() {
 	return getLastAcquisitionProcessState();
     }
 
     public AcquisitionProcessStateType getAcquisitionProcessStateType() {
 	return getLastAcquisitionProcessStateType();
     }
 
     private boolean isProcessInState(AcquisitionProcessState state) {
 	return getAcquisitionProcessState().equals(state);
     }
 
     public boolean isPersonAbleToExecuteActivities() {
 	return isAcquisitionProposalDocumentAvailable()
 		|| isCreateAcquisitionRequestItemAvailable()
 		|| isSubmitForApprovalAvailable()
 		|| isApproveAvailable()
 		|| isDeleteAvailable()
 		|| isFundAllocationIdAvailable()
 		|| isFundAllocationExpirationDateAvailable()
 		|| isCreateAcquisitionRequestAvailable()
 		|| isReceiveInvoiceAvailable()
 		|| isConfirmInvoiceAvailable()
 		|| isPayAcquisitionAvailable()
 		|| isAlocateFundsPermanentlyAvailable();
     }
 
     public boolean isAcquisitionProcessed() {
 	return isProcessInState(AcquisitionProcessStateType.ACQUISITION_PROCESSED);
     }
 
     public boolean isReceiveInvoiceAvailable() {
 	final User user = UserView.getUser();
 	return isAcquisitionProcessed() && user.getPerson().hasRoleType(RoleType.ACCOUNTABILITY);
     }
 
     @Service
     public void receiveInvoice(final String filename, final byte[] bytes, final String invoiceNumber, final DateTime invoiceDate) {
 	if (!isAcquisitionProcessed()) {
 	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.setReceiveInvoice");
 	}
 	final AcquisitionRequest acquisitionRequest = getAcquisitionRequest();
 	acquisitionRequest.receiveInvoice(filename, bytes, invoiceNumber, invoiceDate);
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.INVOICE_RECEIVED);
     }
 
     public boolean isInvoiceReceived() {
 	final AcquisitionRequest acquisitionRequest = getAcquisitionRequest();
 	return isProcessInState(AcquisitionProcessStateType.INVOICE_RECEIVED) && acquisitionRequest.isInvoiceReceived();
     }
 
     public boolean isConfirmInvoiceAvailable() {
 	return isInvoiceReceived() && isResponsibleForUnit();
     }
 
     @Service
     public void confirmInvoice() {
 	if (!isInvoiceReceived()) {
 	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.confirmInvoice");
 	}
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.INVOICE_CONFIRMED);
     }
     
     
     public boolean isPayAcquisitionAvailable() {
 	User user = UserView.getUser();
	return user.getPerson().hasRoleType(RoleType.ACQUISITION_CENTRAL) && isProcessInState(AcquisitionProcessStateType.INVOICE_CONFIRMED);
     }
     
     @Service
     public void payAcquisition() {
 	if (!isPayAcquisitionAvailable()) {
 	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.alocateFundsPermanently");
 	}
 	new AcquisitionProcessState(this,AcquisitionProcessStateType.ACQUISITION_PAYED);
     }
     
     public boolean isAlocateFundsPermanentlyAvailable() {
 	User user = UserView.getUser();
	return user.getPerson().hasRoleType(RoleType.ACCOUNTABILITY) && isProcessInState(AcquisitionProcessStateType.ACQUISITION_PAYED);
     }
     
     @Service
     public void alocateFundsPermanently() {
 	if (!isAlocateFundsPermanentlyAvailable()) {
 	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.alocateFundsPermanently");
 	}
 	new AcquisitionProcessState(this,AcquisitionProcessStateType.FUNDS_ALLOCATED_PERMANENTLY);
     }
 
 }
