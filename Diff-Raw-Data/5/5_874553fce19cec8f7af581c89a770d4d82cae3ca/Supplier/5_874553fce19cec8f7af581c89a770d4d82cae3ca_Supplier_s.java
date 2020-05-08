 package pt.ist.expenditureTrackingSystem.domain.organization;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AcquisitionAfterTheFact;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AfterTheFactAcquisitionType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateSupplierBean;
 import pt.ist.expenditureTrackingSystem.domain.util.Address;
 import pt.ist.expenditureTrackingSystem.domain.util.Money;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.pstm.Transaction;
 
 public class Supplier extends Supplier_Base {
 
     private static Money SUPPLIER_LIMIT = new Money("60000");
 
     private Supplier() {
 	super();
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
     }
 
     public Supplier(String fiscalCode) {
 	this();
 	if (fiscalCode == null || fiscalCode.length() == 0) {
 	    throw new DomainException("error.fiscal.code.cannot.be.empty");
 	}
 	setFiscalIdentificationCode(fiscalCode);
     }
 
     public Supplier(String name, String abbreviatedName, String fiscalCode, Address address, String phone, String fax,
 	    String email, String nib) {
 	this(fiscalCode);
 	setName(name);
 	setAbbreviatedName(abbreviatedName);
 	setAddress(address);
 	setPhone(phone);
 	setFax(fax);
 	setEmail(email);
 	setNib(nib);
     }
 
     @Service
     public void delete() {
 	if (checkIfCanBeDeleted()) {
 	    removeExpenditureTrackingSystem();
 	    Transaction.deleteObject(this);
 	}
     }
 
     private boolean checkIfCanBeDeleted() {
 	return !hasAnyAcquisitionRequests();
     }
 
     public static Supplier readSupplierByFiscalIdentificationCode(String fiscalIdentificationCode) {
 	for (Supplier supplier : ExpenditureTrackingSystem.getInstance().getSuppliersSet()) {
 	    if (supplier.getFiscalIdentificationCode().equals(fiscalIdentificationCode)) {
 		return supplier;
 	    }
 	}
 	return null;
     }
 
     public static Supplier readSupplierByName(final String name) {
 	for (Supplier supplier : ExpenditureTrackingSystem.getInstance().getSuppliersSet()) {
 	    if (supplier.getName().equalsIgnoreCase(name)) {
 		return supplier;
 	    }
 	}
 	return null;
     }
 
     public Money getTotalAllocated() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequest acquisitionRequest : getAcquisitionRequestsSet()) {
 	    final AcquisitionProcess acquisitionProcess = acquisitionRequest.getAcquisitionProcess();
	    if (!acquisitionProcess.isActive() && acquisitionProcess.isAllocatedToSupplier()) {
 		result = result.add(acquisitionRequest.getValueAllocated());
 	    }
 	}
 	for (final AcquisitionAfterTheFact acquisitionAfterTheFact : getAcquisitionsAfterTheFactSet()) {
 	    if (!acquisitionAfterTheFact.getDeletedState().booleanValue()) {
 		result = result.add(acquisitionAfterTheFact.getValue());
 	    }
 	}
 	for (final RefundInvoice refundInvoice : getRefundInvoicesSet()) {
 	    final RefundProcess refundProcess = refundInvoice.getRefundItem().getRequest().getProcess();
	    if (!refundProcess.isActive()) {
 		result = result.add(refundInvoice.getRefundableValue());
 	    }
 	}
 	return result;
     }
 
     public Money getTotalAllocatedByAcquisitionProcesses() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequest acquisitionRequest : getAcquisitionRequestsSet()) {
 	    if (acquisitionRequest.getAcquisitionProcess().isAllocatedToSupplier()) {
 		result = result.add(acquisitionRequest.getValueAllocated());
 	    }
 	}
 	return result;
     }
 
     public Money getTotalAllocatedByAfterTheFactAcquisitions(final AfterTheFactAcquisitionType afterTheFactAcquisitionType) {
 	Money result = Money.ZERO;
 	for (final AcquisitionAfterTheFact acquisitionAfterTheFact : getAcquisitionsAfterTheFactSet()) {
 	    if (!acquisitionAfterTheFact.getDeletedState().booleanValue()) {
 		if (acquisitionAfterTheFact.getAfterTheFactAcquisitionType() == afterTheFactAcquisitionType) {
 		    result = result.add(acquisitionAfterTheFact.getValue());
 		}
 	    }
 	}
 	return result;
     }
 
     public Money getTotalAllocatedByPurchases() {
 	return getTotalAllocatedByAfterTheFactAcquisitions(AfterTheFactAcquisitionType.PURCHASE);
     }
 
     public Money getTotalAllocatedByWorkingCapitals() {
 	return getTotalAllocatedByAfterTheFactAcquisitions(AfterTheFactAcquisitionType.WORKING_CAPITAL);
     }
 
     public Money getTotalAllocatedByRefunds() {
 	return getTotalAllocatedByAfterTheFactAcquisitions(AfterTheFactAcquisitionType.REFUND);
     }
 
     @Service
     public static Supplier createNewSupplier(CreateSupplierBean createSupplierBean) {
 	return new Supplier(createSupplierBean.getName(), createSupplierBean.getAbbreviatedName(), createSupplierBean
 		.getFiscalIdentificationCode(), createSupplierBean.getAddress(), createSupplierBean.getPhone(),
 		createSupplierBean.getFax(), createSupplierBean.getEmail(), createSupplierBean.getNib());
     }
 
     public boolean isFundAllocationAllowed(final Money value) {
 	final Money totalAllocated = getTotalAllocated();
 	final Money totalValue = totalAllocated.add(value);
 	return totalValue.isLessThanOrEqual(SUPPLIER_LIMIT);
     }
 
     public String getPresentationName() {
 	return getFiscalIdentificationCode() + " - " + getName();
     }
 
 }
