 package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Set;
 
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionItemClassification;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.CPVReference;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestWithPayment;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.UnitItem;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class RefundItem extends RefundItem_Base {
 
     public static final Comparator<RefundItem> COMPARATOR = new Comparator<RefundItem>() {
 
 	@Override
 	public int compare(RefundItem arg0, RefundItem arg1) {
 	    final int c = arg0.getDescription().compareTo(arg1.getDescription());
 	    return c == 0 ? arg0.getExternalId().compareTo(arg1.getExternalId()) : c;
 	}
 
     };
 
     public RefundItem(RefundRequest request, Money valueEstimation, CPVReference reference,
 	    AcquisitionItemClassification classification, String description) {
 	super();
 	if (description == null || reference == null || valueEstimation == null || valueEstimation.equals(Money.ZERO)) {
 	    throw new DomainException("refundProcess.message.exception.refundItem.invalidArguments");
 	}
 	setClassification(classification);
 	setDescription(description);
 	setCPVReference(reference);
 	setValueEstimation(valueEstimation);
 	setRequest(request);
     }
 
     @Override
     public Money getRealValue() {
 	return getValueSpent();
     }
 
     @Override
     public Money getValue() {
 	return getValueEstimation();
     }
 
     @Override
     public BigDecimal getVatValue() {
	return null;
     }
 
     public void edit(Money valueEstimation, CPVReference reference, AcquisitionItemClassification classification,
 	    String description) {
 	setDescription(description);
 	setClassification(classification);
 	setCPVReference(reference);
 	setValueEstimation(valueEstimation);
     }
 
     @Override
     public void delete() {
 	removeRequest();
 	super.delete();
     }
 
     @Override
     public boolean isValueFullyAttributedToUnits() {
 	Money totalValue = Money.ZERO;
 	for (UnitItem unitItem : getUnitItems()) {
 	    totalValue = totalValue.add(unitItem.getShareValue());
 	}
 
 	return totalValue.equals(getValueEstimation());
     }
 
     public boolean hasAtLeastOneResponsibleApproval() {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getItemAuthorized()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     @Override
     public void createUnitItem(Unit unit, Money shareValue) {
 	createUnitItem(getRequest().addPayingUnit(unit), shareValue);
     }
 
     @Service
     public RefundableInvoiceFile createRefundInvoice(String invoiceNumber, LocalDate invoiceDate, Money value,
 	    BigDecimal vatValue, Money refundableValue, byte[] invoiceFile, String filename, Supplier supplier) {
 	RefundableInvoiceFile invoice = new RefundableInvoiceFile(invoiceNumber, invoiceDate, value, vatValue, refundableValue,
 		invoiceFile, filename, this, supplier);
 
 	Set<Unit> payingUnits = getRequest().getPayingUnits();
 	if (payingUnits.size() == 1) {
 	    UnitItem unitItemFor = getUnitItemFor(payingUnits.iterator().next());
 	    Money amount = Money.ZERO;
 	    for (RefundableInvoiceFile invoicesToSum : getRefundableInvoices()) {
 		amount = amount.addAndRound(invoicesToSum.getRefundableValue());
 	    }
 	    clearRealShareValues();
 	    unitItemFor.setRealShareValue(amount);
 	}
 	return invoice;
     }
 
     public Money getValueSpent() {
 	List<PaymentProcessInvoice> invoicesFiles = getInvoicesFiles();
 	if (invoicesFiles.isEmpty()) {
 	    return null;
 	}
 
 	Money spent = Money.ZERO;
 	for (PaymentProcessInvoice invoice : invoicesFiles) {
 	    spent = spent.addAndRound(((RefundableInvoiceFile) invoice).getRefundableValue());
 	}
 	return spent;
     }
 
     @Override
     public boolean isFilledWithRealValues() {
 	return !getRefundableInvoices().isEmpty();
     }
 
     public void getSuppliers(final Set<Supplier> suppliers) {
 	for (final RefundableInvoiceFile refundInvoice : getRefundableInvoices()) {
 	    final Supplier supplier = refundInvoice.getSupplier();
 	    if (supplier != null) {
 		suppliers.add(supplier);
 	    }
 	}
     }
 
     @Override
     public Money getTotalAmountForCPV(final int year) {
 	return isAppliableForCPV(year) ? getCurrentSupplierAllocationValue() : Money.ZERO;
     }
 
     private Money getCurrentSupplierAllocationValue() {
 	Money spent = Money.ZERO;
 	for (RefundableInvoiceFile invoice : getRefundableInvoices()) {
 	    spent = spent.add(invoice.getRefundableValue());
 	}
 	return spent;
     }
 
     private boolean isAppliableForCPV(final int year) {
 	final RequestWithPayment requestWithPayment = getRequest();
 	final RefundProcess refundProcess = requestWithPayment.getProcess();
 	return refundProcess.isActive() && refundProcess.isAppiableForYear(year);
     }
 
     @Override
     public void confirmInvoiceBy(Person person) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (getRequest().getProcess().isAccountingEmployee(person)
 		    || getRequest().getProcess().isProjectAccountingEmployee(person)) {
 		unitItem.getConfirmedInvoices().clear();
 		for (PaymentProcessInvoice invoice : getInvoicesFiles()) {
 		    unitItem.addConfirmedInvoices(invoice);
 		}
 	    }
 	}
     }
 
     @Override
     public void unconfirmInvoiceBy(Person person) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (getRequest().getProcess().isAccountingEmployee(person)
 		    || getRequest().getProcess().isProjectAccountingEmployee(person)) {
 		unitItem.getConfirmedInvoices().clear();
 	    }
 	}
     }
 
     @Override
     public <T extends PaymentProcessInvoice> List<T> getConfirmedInvoices(Person person) {
 	List<T> invoices = new ArrayList<T>();
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (person == null || unitItem.getFinancer().getUnit().isAccountingEmployee(person)
 		    || unitItem.getFinancer().getUnit().isProjectAccountingEmployee(person)) {
 		invoices.addAll((List<T>) unitItem.getConfirmedInvoices());
 	    }
 	}
 	return invoices;
     }
 
     public boolean isRefundValueBiggerThanEstimateValue() {
 	Money refundableValue = Money.ZERO;
 	for (RefundableInvoiceFile invoice : getRefundableInvoices()) {
 	    refundableValue = refundableValue.add(invoice.getRefundableValue());
 	}
 	return refundableValue.isGreaterThan(getValueEstimation());
     }
 
     public boolean isAnyRefundInvoiceAvailable() {
 	return !getRefundableInvoices().isEmpty();
     }
 
     @Override
     public boolean isRealValueFullyAttributedToUnits() {
 	Money realValue = getRealValue();
 	if (realValue == null) {
 	    return getRefundableInvoices().isEmpty();
 	}
 	Money totalValue = Money.ZERO;
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getRealShareValue() != null) {
 		totalValue = totalValue.add(unitItem.getRealShareValue());
 	    }
 	}
 
 	return totalValue.equals(realValue);
     }
 
     public List<RefundableInvoiceFile> getRefundableInvoices() {
 	List<RefundableInvoiceFile> invoices = new ArrayList<RefundableInvoiceFile>();
 	for (PaymentProcessInvoice invoice : getInvoicesFiles()) {
 	    invoices.add((RefundableInvoiceFile) invoice);
 	}
 	return invoices;
     }
 
 }
