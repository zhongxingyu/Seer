 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.ReleaseProcess;
 import module.workflow.activities.StealProcess;
 import module.workflow.activities.TakeProcess;
 import module.workflow.activities.WorkflowActivity;
 import module.workflow.domain.ProcessFile;
 import module.workflow.domain.WorkflowProcess;
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProposalDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.CreditNoteDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PurchaseOrderDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.AllocateFundsPermanently;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.AllocateProjectFundsPermanently;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.Authorize;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.FundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.GenericAddPayingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.GenericAssignPayingUnitToItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.GenericRemovePayingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.ProjectFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.RemoveFundsPermanentlyAllocated;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.RemovePermanentProjectFunds;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.UnApprove;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.UnAuthorize;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.CancelAcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.CancelInvoiceConfirmation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.ChangeFinancersAccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.ChangeProcessClassification;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.ConfirmInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.CreateAcquisitionPurchaseOrderDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.CreateAcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.DeleteAcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.DistributeRealValuesForPayingUnits;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.EditAcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.EditAcquisitionRequestItemRealValues;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.EditSimpleContractDescription;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.FundAllocationExpirationDate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.JumpToProcessState;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.LockInvoiceReceiving;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.PayAcquisition;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RejectAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RemoveCancelProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RemoveFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RemoveFundAllocationExpirationDate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RemoveFundAllocationExpirationDateForResponsible;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RemoveProjectFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RevertInvoiceSubmission;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RevertProcessNotConfirmmingFundAllocationExpirationDate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RevertSkipPurchaseOrderDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RevertToInvoiceConfirmation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SelectSupplier;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SendPurchaseOrderToSupplier;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SetSkipSupplierFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SkipPurchaseOrderDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SubmitForApproval;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SubmitForConfirmInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SubmitForFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.UnSubmitForApproval;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.UnlockInvoiceReceiving;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.UnsetSkipSupplierFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.announcements.RCISTAnnouncement;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateAcquisitionProcessBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class SimplifiedProcedureProcess extends SimplifiedProcedureProcess_Base {
 
     public static enum ProcessClassification implements IPresentableEnum {
 
 	CCP(new Money("5000"), true, "RS 5000"), CT10000(new Money("10000"), "CT 10000"), CT75000(new Money("75000"), "CT 75000");
 
 	final private Money value;
 	final private String shortDescription;
 	final private boolean ccp;
 
 	ProcessClassification(Money value, String shortDescription) {
 	    this(value, false, shortDescription);
 	}
 
 	ProcessClassification(Money value, boolean ccp, String shortDescription) {
 	    this.value = value;
 	    this.ccp = ccp;
 	    this.shortDescription = shortDescription;
 	}
 
 	public Money getLimit() {
 	    return value;
 	}
 
 	public boolean isCCP() {
 	    return ccp;
 	}
 
 	public String getShortDescription() {
 	    return shortDescription;
 	}
 
 	@Override
 	public String getLocalizedName() {
 	    return BundleUtil.getFormattedStringFromResourceBundle("resources/ExpenditureResources",
 		    "label.processClassification." + name());
 	}
     }
 
     private static List<AcquisitionProcessStateType> availableStates = new ArrayList<AcquisitionProcessStateType>();
 
     private static List<WorkflowActivity<? extends RegularAcquisitionProcess, ? extends ActivityInformation<? extends RegularAcquisitionProcess>>> activities = new ArrayList<WorkflowActivity<? extends RegularAcquisitionProcess, ? extends ActivityInformation<? extends RegularAcquisitionProcess>>>();
 
     static {
 	activities.add(new CreateAcquisitionPurchaseOrderDocument());
 	activities.add(new SendPurchaseOrderToSupplier());
 	activities.add(new SkipPurchaseOrderDocument());
 	activities.add(new RevertSkipPurchaseOrderDocument());
 	activities.add(new GenericAddPayingUnit<RegularAcquisitionProcess>());
 	activities.add(new GenericRemovePayingUnit<RegularAcquisitionProcess>());
 	activities.add(new SubmitForApproval());
 	activities.add(new SubmitForFundAllocation());
 	activities.add(new RejectAcquisitionProcess());
 	activities.add(new UnApprove<RegularAcquisitionProcess>());
 	activities.add(new FundAllocationExpirationDate());
 	activities.add(new RevertProcessNotConfirmmingFundAllocationExpirationDate());
 	activities.add(new RevertToInvoiceConfirmation());
 	activities.add(new Authorize<RegularAcquisitionProcess>());
 	activities.add(new UnAuthorize<RegularAcquisitionProcess>());
 
 	activities.add(new AllocateProjectFundsPermanently<RegularAcquisitionProcess>());
 	activities.add(new AllocateFundsPermanently<RegularAcquisitionProcess>());
 	activities.add(new RemovePermanentProjectFunds<RegularAcquisitionProcess>());
 	activities.add(new RemoveFundsPermanentlyAllocated<RegularAcquisitionProcess>());
 
 	activities.add(new ProjectFundAllocation<RegularAcquisitionProcess>());
 	activities.add(new FundAllocation<RegularAcquisitionProcess>());
 	activities.add(new RemoveFundAllocation());
 	activities.add(new RemoveProjectFundAllocation());
 	activities.add(new RemoveFundAllocationExpirationDate());
 	activities.add(new RemoveFundAllocationExpirationDateForResponsible());
 	activities.add(new CancelAcquisitionRequest());
 
 	activities.add(new UnlockInvoiceReceiving());
 	activities.add(new LockInvoiceReceiving());
 
 	activities.add(new SubmitForConfirmInvoice());
 	activities.add(new ConfirmInvoice());
 	activities.add(new CancelInvoiceConfirmation());
 
 	activities.add(new UnSubmitForApproval());
 
 	activities.add(new SetSkipSupplierFundAllocation());
 	activities.add(new UnsetSkipSupplierFundAllocation());
 
 	activities.add(new RevertInvoiceSubmission());
 	activities.add(new RemoveCancelProcess());
 
 	activities.add(new GenericAssignPayingUnitToItem<RegularAcquisitionProcess>());
 
 	activities.add(new DistributeRealValuesForPayingUnits());
 
 	activities.add(new ChangeFinancersAccountingUnit());
 	activities.add(new SelectSupplier());
 
 	activities.add(new ChangeProcessClassification());
 	activities.add(new CreateAcquisitionRequestItem());
 	activities.add(new PayAcquisition());
 	activities.add(new DeleteAcquisitionRequestItem());
 	activities.add(new EditAcquisitionRequestItem());
 	activities.add(new EditAcquisitionRequestItemRealValues());
 	activities.add(new TakeProcess<RegularAcquisitionProcess>());
 	activities.add(new ReleaseProcess<RegularAcquisitionProcess>());
 	activities.add(new StealProcess<RegularAcquisitionProcess>());
 	activities.add(new JumpToProcessState());
 	activities.add(new EditSimpleContractDescription());
 
 	availableStates.add(AcquisitionProcessStateType.IN_GENESIS);
 	availableStates.add(AcquisitionProcessStateType.SUBMITTED_FOR_APPROVAL);
 	availableStates.add(AcquisitionProcessStateType.SUBMITTED_FOR_FUNDS_ALLOCATION);
 	availableStates.add(AcquisitionProcessStateType.FUNDS_ALLOCATED_TO_SERVICE_PROVIDER);
 	availableStates.add(AcquisitionProcessStateType.FUNDS_ALLOCATED);
 	availableStates.add(AcquisitionProcessStateType.AUTHORIZED);
 	availableStates.add(AcquisitionProcessStateType.ACQUISITION_PROCESSED);
 	availableStates.add(AcquisitionProcessStateType.INVOICE_RECEIVED);
 	availableStates.add(AcquisitionProcessStateType.SUBMITTED_FOR_CONFIRM_INVOICE);
 	availableStates.add(AcquisitionProcessStateType.INVOICE_CONFIRMED);
 	availableStates.add(AcquisitionProcessStateType.FUNDS_ALLOCATED_PERMANENTLY);
 	availableStates.add(AcquisitionProcessStateType.ACQUISITION_PAYED);
 	availableStates.add(AcquisitionProcessStateType.REJECTED);
 	availableStates.add(AcquisitionProcessStateType.CANCELED);
     }
 
     protected SimplifiedProcedureProcess(final Person requester) {
 	super();
 	inGenesis();
 	new AcquisitionRequest(this, requester);
     }
 
     protected SimplifiedProcedureProcess(Supplier supplier, Person person) {
 	super();
 	inGenesis();
 	new AcquisitionRequest(this, supplier, person);
     }
 
     protected SimplifiedProcedureProcess(ProcessClassification classification, List<Supplier> suppliers, Person person) {
 	super();
 	inGenesis();
 	AcquisitionRequest acquisitionRequest = new AcquisitionRequest(this, suppliers, person);
 	if (suppliers.size() == 0) {
 	    throw new DomainException("acquisitionProcess.message.exception.needsMoreSuppliers");
 	}
 	if (suppliers.size() == 1) {
 	    acquisitionRequest.setSelectedSupplier(suppliers.get(0));
 	}
 	setProcessClassification(classification);
     }
 
     @Service
     public static SimplifiedProcedureProcess createNewAcquisitionProcess(
 	    final CreateAcquisitionProcessBean createAcquisitionProcessBean) {
 	if (!isCreateNewProcessAvailable()) {
 	    throw new DomainException("acquisitionProcess.message.exception.invalidStateToRun.create");
 	}
 	SimplifiedProcedureProcess process = new SimplifiedProcedureProcess(createAcquisitionProcessBean.getClassification(),
 		createAcquisitionProcessBean.getSuppliers(), createAcquisitionProcessBean.getRequester());
 	AcquisitionRequest acquisitionRequest = process.getAcquisitionRequest();
 	acquisitionRequest.setRequestingUnit(createAcquisitionProcessBean.getRequestingUnit());
 	acquisitionRequest.setContractSimpleDescription(createAcquisitionProcessBean.getContractSimpleDescription());
 	if (createAcquisitionProcessBean.isRequestUnitPayingUnit()) {
 	    final Unit unit = createAcquisitionProcessBean.getRequestingUnit();
 	    acquisitionRequest.addFinancers(unit.finance(acquisitionRequest));
 	}
 	if (createAcquisitionProcessBean.isForMission()) {
 	    if (createAcquisitionProcessBean.getMissionProcess() == null) {
 		throw new DomainException("mission.process.is.mandatory");
 	    }
 	    process.setMissionProcess(createAcquisitionProcessBean.getMissionProcess());
 	}
 
 	return process;
     }
 
     public <T extends WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> List<T> getActivities() {
 	return (List<T>) activities;
     }
 
     public boolean isEditRequestItemAvailable() {
 	final Person loggedPerson = getLoggedPerson();
 	return loggedPerson != null && loggedPerson.equals(getRequestor()) && getLastAcquisitionProcessState().isInGenesis();
     }
 
     @Override
     public Money getAcquisitionRequestValueLimit() {
 	return getProcessClassification().getLimit();
     }
 
     @Override
     public boolean isSimplifiedAcquisitionProcess() {
 	return true;
     }
 
     @Override
     public List<AcquisitionProcessStateType> getAvailableStates() {
 	return availableStates;
     }
 
     public static List<AcquisitionProcessStateType> getAvailableStatesForSimplifiedProcedureProcess() {
 	return availableStates;
     }
 
     public boolean isSimplifiedProcedureProcess() {
 	return true;
     }
 
     @Override
     public boolean isPayed() {
 	return getRequest().isPayed();
     }
 
     @Override
     public String getProcessStateDescription() {
 	return getLastAcquisitionProcessState().getLocalizedName();
     }
 
     public boolean isAppiableForYear(final int year) {
 	return Util.isAppiableForYear(year, this);
     }
 
     public boolean isCCP() {
 	return getProcessClassification().isCCP();
     }
 
     @Override
     public void setProcessClassification(ProcessClassification processClassification) {
 	if (getSkipSupplierFundAllocation()) {
 	    unSkipSupplierFundAllocation();
 	}
 	if (processClassification.getLimit().isLessThan(this.getAcquisitionRequest().getCurrentValue())) {
 	    throw new DomainException("error.message.processValueExceedsLimitForClassification", DomainException
 		    .getResourceFor("resources/AcquisitionResources"));
 	}
 	super.setProcessClassification(processClassification);
     }
 
     public void setProcessClassificationWithoutChecks(ProcessClassification processClassification) {
 	if (processClassification.getLimit().isLessThan(this.getAcquisitionRequest().getCurrentValue())) {
 	    System.out.println("Process: " + getAcquisitionProcessId() + " exceed limit with: "
 		    + getAcquisitionRequest().getCurrentValue().toFormatString());
 	}
 	super.setProcessClassification(processClassification);
     }
 
     public boolean isWarnRegardingProcessClassificationNeeded() {
 	return getProcessClassification().isCCP() != getRequestingUnit().getDefaultRegeimIsCCP();
     }
 
     @Override
     public Boolean getShouldSkipSupplierFundAllocation() {
 	return !this.isCCP() || super.getShouldSkipSupplierFundAllocation();
     }
 
     public boolean isWarnForLessSuppliersActive() {
 	return getProcessClassification() == ProcessClassification.CT75000 && getSuppliers().size() < 3;
     }
 
     @Override
     public List<Class<? extends ProcessFile>> getAvailableFileTypes() {
 	List<Class<? extends ProcessFile>> availableFileTypes = new ArrayList<Class<? extends ProcessFile>>();
 	availableFileTypes.add(AcquisitionProposalDocument.class);
 	availableFileTypes.add(PurchaseOrderDocument.class);
 	availableFileTypes.add(AcquisitionInvoice.class);
 	availableFileTypes.add(CreditNoteDocument.class);
 	availableFileTypes.addAll(super.getAvailableFileTypes());
 	return availableFileTypes;
     }
 
     @Override
     public List<Class<? extends ProcessFile>> getUploadableFileTypes() {
 	List<Class<? extends ProcessFile>> uploadableFileTypes = super.getUploadableFileTypes();
 	uploadableFileTypes.remove(PurchaseOrderDocument.class);
 	return uploadableFileTypes;
     }
 
     @Override
     public String getLocalizedName() {
 	return getProcessClassification().getLocalizedName();
     }
 
     public String getTypeDescription() {
 	return getProcessClassification().getLocalizedName();
     }
 
     public String getTypeShortDescription() {
 	return getProcessClassification().getShortDescription();
     }
 
     @Override
     public void processAcquisition() {
 	super.processAcquisition();
 	ProcessClassification processClassification = getProcessClassification();
	if ((processClassification == ProcessClassification.CT75000 || processClassification == ProcessClassification.CT10000)
		&& !getAcquisitionRequest().hasAnnouncement()) {
 	    new RCISTAnnouncement(getAcquisitionRequest());
 	}
     }
 }
