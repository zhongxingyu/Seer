 package pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import module.workflow.domain.LabelLog;
import module.workflow.domain.LabelLog_Base;
 import module.workflow.domain.ProcessFile;
 import module.workflow.domain.WorkflowProcess;
import myorg.domain.User;
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 import myorg.util.BundleUtil;
 import myorg.util.ClassNameBundle;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Invoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.activities.DeleteAfterTheFactAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.activities.EditAfterTheFactAcquisition;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.activities.EditAfterTheFactProcessActivityInformation.AfterTheFactAcquisitionProcessBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.fenixWebFramework.services.Service;
 
 @ClassNameBundle(bundle = "resources/ExpenditureResources", key = "label.process.afterTheFactAcquisition")
 public class AfterTheFactAcquisitionProcess extends AfterTheFactAcquisitionProcess_Base {
 
     private static List<WorkflowActivity<AfterTheFactAcquisitionProcess, ? extends ActivityInformation<AfterTheFactAcquisitionProcess>>> activities = new ArrayList<WorkflowActivity<AfterTheFactAcquisitionProcess, ? extends ActivityInformation<AfterTheFactAcquisitionProcess>>>();
 
     static {
 	activities.add(new EditAfterTheFactAcquisition());
 	activities.add(new DeleteAfterTheFactAcquisitionProcess());
     }
 
     protected AfterTheFactAcquisitionProcess() {
 	super();
 	new AcquisitionAfterTheFact(this);
     }
 
     private static final ThreadLocal<AfterTheFactAcquisitionProcessBean> threadLocal = new ThreadLocal<AfterTheFactAcquisitionProcessBean>();
 
     @Service
     public static AfterTheFactAcquisitionProcess createNewAfterTheFactAcquisitionProcess(
 	    AfterTheFactAcquisitionProcessBean afterTheFactAcquisitionProcessBean) {
 	threadLocal.set(afterTheFactAcquisitionProcessBean);
 	final AfterTheFactAcquisitionProcess afterTheFactAcquisitionProcess = new AfterTheFactAcquisitionProcess();
 	afterTheFactAcquisitionProcess.edit(afterTheFactAcquisitionProcessBean.getAfterTheFactAcquisitionType(),
 		afterTheFactAcquisitionProcessBean.getValue(), afterTheFactAcquisitionProcessBean.getVatValue(),
 		afterTheFactAcquisitionProcessBean.getSupplier(), afterTheFactAcquisitionProcessBean.getDescription());
 	final Person loggedPerson = Person.getLoggedPerson();
 	new LabelLog(afterTheFactAcquisitionProcess, loggedPerson.getUser(), "label."
 		+ afterTheFactAcquisitionProcess.getClass().getName() + ".Create", "resources/AcquisitionResources");
 	return afterTheFactAcquisitionProcess;
     }
 
     protected int getYearForConstruction() {
 	return threadLocal.get().getYear().intValue();
     }
 
     public void edit(AfterTheFactAcquisitionType type, Money value, BigDecimal vatValue, Supplier supplier, String description) {
 	final AcquisitionAfterTheFact acquisitionAfterTheFact = getAcquisitionAfterTheFact();
 	acquisitionAfterTheFact.edit(type, value, vatValue, supplier, description);
     }
 
     public void delete() {
 	final AcquisitionAfterTheFact acquisitionAfterTheFact = getAcquisitionAfterTheFact();
 	acquisitionAfterTheFact.delete();
     }
 
     @Override
     public boolean isAvailableForPerson(Person person) {
 	return person.hasRoleType(RoleType.ACQUISITION_CENTRAL) || person.hasRoleType(RoleType.ACQUISITION_CENTRAL_MANAGER);
     }
 
     public void cancel() {
 	getAcquisitionAfterTheFact().setDeletedState(Boolean.TRUE);
     }
 
     public void renable() {
 	getAcquisitionAfterTheFact().setDeletedState(Boolean.FALSE);
     }
 
     @Override
     public boolean hasAnyAvailableActivitity() {
 	List<WorkflowActivity<WorkflowProcess, ActivityInformation<WorkflowProcess>>> activeActivities = getActiveActivities();
 	return !activeActivities.isEmpty();
     }
 
     @Override
     public void allocateFundsToUnit() {
 	// do nothing
     }
 
     @Override
     public void submitForApproval() {
 	// nothing to do here...
     }
 
     @Override
     public void submitForFundAllocation() {
 	// nothing to do here...
     }
 
     @Override
     public boolean isInAllocatedToUnitState() {
 	return false;
     }
 
     @Override
     protected void authorize() {
 	// nothing to do here...
     }
 
     public boolean isAppiableForYear(final int year) {
 	return getAcquisitionAfterTheFact().isAppiableForYear(year);
     }
 
     public AfterTheFactInvoice receiveInvoice(String filename, byte[] bytes, String invoiceNumber, LocalDate invoiceDate) {
 
 	final AfterTheFactInvoice invoice = hasInvoice() ? getInvoice() : new AfterTheFactInvoice(this);
 	invoice.setFilename(filename);
 	invoice.setContent(bytes);
 	invoice.setInvoiceNumber(invoiceNumber);
 	invoice.setInvoiceDate(invoiceDate);
 
 	return invoice;
     }
 
     public String getInvoiceNumber() {
 	Invoice invoice = getInvoice();
 	return invoice != null ? invoice.getInvoiceNumber() : null;
     }
 
     public LocalDate getInvoiceDate() {
 	Invoice invoice = getInvoice();
 	return invoice != null ? invoice.getInvoiceDate() : null;
     }
 
     public AfterTheFactInvoice getInvoice() {
 	List<AfterTheFactInvoice> files = getFiles(AfterTheFactInvoice.class);
 	if (files.size() > 1) {
 	    throw new DomainException("error.should.only.have.one.invoice");
 	}
 	return files.isEmpty() ? null : files.get(0);
     }
 
     public boolean hasInvoice() {
 	return !getFiles(Invoice.class).isEmpty();
     }
 
     @Override
     public <T extends WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> List<T> getActivities() {
 	return (List<T>) activities;
     }
 
     @Override
     public boolean isTicketSupportAvailable() {
 	return false;
     }
 
     @Override
     public boolean isFileSupportAvailable() {
 	return true;
     }
 
     @Override
     public boolean isObserverSupportAvailable() {
 	return false;
     }
 
     @Override
     public boolean isCommentsSupportAvailable() {
 	return false;
     }
 
     @Override
     public List<Class<? extends ProcessFile>> getAvailableFileTypes() {
 	List<Class<? extends ProcessFile>> availableFileTypes = super.getAvailableFileTypes();
 	availableFileTypes.add(AfterTheFactInvoice.class);
 	return availableFileTypes;
     }
 
     @Override
     public boolean isActive() {
 	return !isCanceled();
     }
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle("resources/AcquisitionResources", "label.AfterTheFactAcquisitionProcess");
     }
 
    @Override
    public User getProcessCreator() {
	return getExecutionLogs(LabelLog.class).iterator().next().getActivityExecutor();
    }

 }
