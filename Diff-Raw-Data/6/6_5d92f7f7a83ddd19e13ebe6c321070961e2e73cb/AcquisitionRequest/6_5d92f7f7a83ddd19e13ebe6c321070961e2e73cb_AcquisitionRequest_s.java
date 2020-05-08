 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Address;
 import myorg.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess.ProcessClassification;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 public class AcquisitionRequest extends AcquisitionRequest_Base {
 
     public AcquisitionRequest(final AcquisitionProcess acquisitionProcess, final Person person) {
 	super();
 	checkParameters(acquisitionProcess, person);
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
 	setAcquisitionProcess(acquisitionProcess);
 	setRequester(person);
     }
 
     private void checkParameters(AcquisitionProcess acquisitionProcess, Person person) {
 	if (acquisitionProcess == null) {
 	    throw new DomainException("error.acquisition.request.wrong.acquisition.process");
 	}
 	if (person == null) {
 	    throw new DomainException("acquisitionProcess.message.exception.anonymousNotAllowedToCreate");
 	}
     }
 
     public AcquisitionRequest(AcquisitionProcess acquisitionProcess, List<Supplier> suppliers, Person person) {
 	this(acquisitionProcess, person);
 	for (Supplier supplier : suppliers) {
 	    addSuppliers(supplier);
 	}
     }
 
     public AcquisitionRequest(AcquisitionProcess acquisitionProcess, Supplier supplier, Person person) {
 	this(acquisitionProcess, person);
 	addSuppliers(supplier);
     }
 
     public void edit(Supplier supplier, Unit requestingUnit, Boolean isRequestingUnitPayingUnit) {
 	addSuppliers(supplier);
 	setRequestingUnit(requestingUnit);
 	if (isRequestingUnitPayingUnit) {
 	    addFinancers(requestingUnit.finance(this));
 	}
     }
 
     public AcquisitionRequestItem createAcquisitionRequestItem(final AcquisitionRequest acquisitionRequest,
 	    final String description, final Integer quantity, final Money unitValue, final BigDecimal vatValue,
 	    final Money additionalCostValue, final String proposalReference, CPVReference reference, String recipient,
 	    Address address, String phone, String email) {
 	return new AcquisitionRequestItem(acquisitionRequest, description, quantity, unitValue, vatValue, additionalCostValue,
 		proposalReference, reference, recipient, address, phone, email);
     }
 
     public String getFiscalIdentificationCode() {
 	if (getSuppliersCount() == 0) {
 	    return null;
 	}
 	StringBuilder builder = new StringBuilder("");
 	Iterator<Supplier> suppliersIterator = getSuppliersIterator();
 	while (suppliersIterator.hasNext()) {
 	    builder.append(suppliersIterator.next().getFiscalIdentificationCode());
 	    if (suppliersIterator.hasNext()) {
 		builder.append(", ");
 	    }
 	}
 	return builder.toString();
     }
 
     public Money getTotalVatValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getTotalVatValue());
 	}
 	return result;
     }
 
     public Money getRealTotalVatValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    Money totalRealVatValue = acquisitionRequestItem.getTotalRealVatValue();
 	    if (totalRealVatValue == null) {
 		return null;
 	    }
 	    result = result.add(totalRealVatValue);
 	}
 	return result;
     }
 
     public Money getCurrentValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    Money totalRealValue = acquisitionRequestItem.getTotalRealValue();
 	    if (totalRealValue != null) {
 		result = result.add(totalRealValue);
 	    } else {
 		result = result.add(acquisitionRequestItem.getTotalItemValue());
 	    }
 	}
 	return result;
     }
 
     public Money getCurrentVatValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    Money totalRealVatValue = acquisitionRequestItem.getTotalRealVatValue();
 	    if (totalRealVatValue != null) {
 		result.add(totalRealVatValue);
 	    } else {
 		result.add(getTotalValue());
 	    }
 	}
 	return result;
     }
 
     public Money getTotalAdditionalCostsValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    if (acquisitionRequestItem.getAdditionalCostValue() != null) {
 		result = result.add(acquisitionRequestItem.getAdditionalCostValue());
 	    }
 	}
 	return result;
 
     }
 
     public Money getRealTotalAdditionalCostsValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    if (acquisitionRequestItem.getRealAdditionalCostValue() != null) {
 		result = result.add(acquisitionRequestItem.getRealAdditionalCostValue());
 	    }
 	}
 	return result;
     }
 
     public Money getTotalItemValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getTotalItemValue());
 	}
 	return result;
     }
 
     public Money getTotalItemValueWithVat() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.addAndRound(acquisitionRequestItem.getTotalItemValueWithVat());
 	}
 	return result;
     }
 
     public Money getTotalItemValueWithAdditionalCosts() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getTotalItemValueWithAdditionalCosts());
 	}
 	return result;
     }
 
     public Money getTotalItemValueWithAdditionalCostsAndVat() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    Money totalItemValueWithAdditionalCostsAndVat = acquisitionRequestItem.getTotalItemValueWithAdditionalCostsAndVat();
 	    if (totalItemValueWithAdditionalCostsAndVat == null) {
 		return null;
 	    }
 	    result = result.addAndRound(totalItemValueWithAdditionalCostsAndVat);
 	}
 	return result;
     }
 
     public Money getCurrentTotalItemValueWithAdditionalCostsAndVat() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    if (acquisitionRequestItem.getTotalRealValueWithAdditionalCostsAndVat() != null) {
 		result = result.addAndRound(acquisitionRequestItem.getTotalRealValueWithAdditionalCostsAndVat());
 	    } else {
 		result = result.addAndRound(acquisitionRequestItem.getTotalItemValueWithAdditionalCostsAndVat());
 	    }
 	}
 	return result;
     }
 
     public Money getRealTotalValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    if (acquisitionRequestItem.getTotalRealValue() == null) {
 		return null;
 	    }
 	    result = result.add(acquisitionRequestItem.getTotalRealValue());
 	}
 	return result;
     }
 
     public Money getRealTotalValueWithVat() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getTotalRealValueWithVat());
 	}
 	return result;
     }
 
     public Money getRealTotalValueWithAdditionalCosts() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getTotalRealValueWithAdditionalCosts());
 	}
 	return result;
     }
 
     public Money getRealTotalValueWithAdditionalCostsAndVat() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.addAndRound(acquisitionRequestItem.getTotalRealValueWithAdditionalCostsAndVat());
 	}
 	return result;
     }
 
     public Money getCurrentTotalAdditionalCostsValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    if (acquisitionRequestItem.getCurrentAdditionalCostValue() != null) {
 		result = result.add(acquisitionRequestItem.getCurrentAdditionalCostValue());
 	    }
 	}
 	return result;
     }
 
     public Money getCurrentTotalValueWithAdditionalCosts() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getCurrentTotalItemValueWithAdditionalCosts());
 	}
 	return result;
     }
 
     public Money getCurrentTotalVatValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getCurrentTotalVatValue());
 	}
 	return result;
     }
 
     public Money getCurrentSupplierAllocationValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getCurrentSupplierAllocationValue());
 	}
 	return result;
     }
 
     public Money getCurrentTotalValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getCurrentTotalItemValueWithAdditionalCostsAndVat());
 	}
 	return result;
     }
 
     public Money getCurrentTotalRoundedValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.addAndRound(acquisitionRequestItem.getCurrentTotalItemValueWithAdditionalCostsAndVat());
 	}
 	return result;
     }
 
     public void processReceivedInvoice() {
 	if (!isRealCostAvailableForAtLeastOneItem()) {
 	    copyEstimateValuesToRealValues();
 	}
     }
 
     public boolean isRealCostAvailableForAtLeastOneItem() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItemsSet()) {
 	    if (item.getRealQuantity() != null && item.getRealUnitValue() != null) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     private void copyEstimateValuesToRealValues() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItemsSet()) {
 	    item.setRealQuantity(item.getQuantity());
 	    item.setRealUnitValue(item.getUnitValue());
 	    item.setRealVatValue(item.getVatValue());
 	    item.setRealAdditionalCostValue(item.getAdditionalCostValue());
 
 	    for (UnitItem unitItem : item.getUnitItems()) {
 		unitItem.setRealShareValue(unitItem.getShareValue());
 	    }
 	}
 
     }
 
     /*
      * TODO: As soon as we have a decent implementation for the CT75000 this
      * code should be rolled back again.
      */
     public boolean isFilled() {
 	AcquisitionProcess process = getProcess();
 	return hasAnyRequestItems()
 		&& ((!process.isSimplifiedProcedureProcess() && hasAcquisitionProcess()) || (process
 			.isSimplifiedProcedureProcess() && (((SimplifiedProcedureProcess) process).getProcessClassification() == ProcessClassification.CT75000 || process
 			.hasAcquisitionProposalDocument())));
     }
 
     public boolean isEveryItemFullyAttributedToPayingUnits() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItemsSet()) {
 	    if (!item.isValueFullyAttributedToUnits()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean isApprovedByAtLeastOneResponsible() {
 	for (final RequestItem requestItem : getRequestItemsSet()) {
 	    for (final UnitItem unitItem : requestItem.getUnitItems()) {
 		if (unitItem.getSubmitedForFundsAllocation().booleanValue()) {
 		    return true;
 		}
 	    }
 	}
 	return false;
     }
 
     public boolean isAuthorizedByAtLeastOneResponsible() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItemsSet()) {
 	    if (item.hasAtLeastOneResponsibleApproval()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean hasAtLeastOneConfirmation() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItemsSet()) {
 	    if (item.hasAtLeastOneInvoiceConfirmation()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isAnyInvoiceConfirmedBy(Person person) {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItemsSet()) {
 	    if (!item.getConfirmedInvoices(person).isEmpty()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public void confirmInvoiceFor(Person person) {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItemsSet()) {
 	    item.confirmInvoiceBy(person);
 	}
     }
 
     public void unconfirmInvoiceFor(Person person) {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItemsSet()) {
 	    item.unconfirmInvoiceBy(person);
 	}
     }
 
     public void unconfirmInvoiceForAll() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItemsSet()) {
 	    item.unconfirmInvoiceForAll();
 	}
     }
 
     public boolean isInvoiceConfirmedBy() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItemsSet()) {
 	    if (!Money.ZERO.equals(item.getRealValue()) && !item.isConfirmForAllInvoices()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public Money getValueAllocated() {
 	if (getAcquisitionProcess().isActive()) {
 	    return (getAcquisitionProcess().getAcquisitionProcessState().hasBeenAllocatedPermanently()) ? getRealTotalValue()
 		    : getTotalItemValue();
 	}
 	return Money.ZERO;
     }
 
     public boolean isValueAllowed(Money value) {
 	Money totalItemValue = getTotalItemValue();
 	Money totalValue = totalItemValue.add(value);
 	return totalValue.isLessThanOrEqual(getAcquisitionProcess().getAcquisitionRequestValueLimit());
     }
 
     public void unSubmitForFundsAllocation() {
 	for (AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    acquisitionRequestItem.unapprove();
 	}
 
     }
 
     public boolean checkRealValues() {
 	return isRealTotalValueEqualsRealShareValue() && isRealUnitShareValueLessThanUnitShareValue()
 		&& isRealValueLessThanTotalValue();
     }
 
     public boolean isCurrentTotalRealValueFullyDistributed() {
 	for (RequestItem requestItem : getRequestItems()) {
 	    if (!requestItem.isCurrentRealValueFullyAttributedToUnits()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public Money getTotalRealShareValue() {
 	Money res = Money.ZERO;
 	for (Financer financer : getFinancersSet()) {
 	    res = res.add(financer.getRealShareValue());
 	}
 	return res;
     }
 
     public boolean isRealValueLessThanTotalValue() {
 	return getRealTotalValue().isLessThanOrEqual(getTotalItemValue());
     }
 
     public boolean isRealUnitShareValueLessThanUnitShareValue() {
 	for (Financer financer : getFinancersSet()) {
 	    if (!financer.isRealUnitShareValueLessThanUnitShareValue()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean isRealTotalValueEqualsRealShareValue() {
 	return getRealTotalValueWithAdditionalCostsAndVat().equals(getTotalRealShareValue());
     }
 
     public String getAcquisitionProcessId() {
 	return getAcquisitionProcess().getAcquisitionProcessId();
     }
 
     public String getAcquisitionProposalDocumentId() {
 	AcquisitionProcess process = getProcess();
 
 	if (process.hasAcquisitionProposalDocument()) {
 	    return process.getAcquisitionProposalDocument().getProposalId();
 	}
 	return null;
     }
 
     public boolean isFundAllocationAllowed(Money totalValue) {
 	for (Supplier supplier : getSuppliers()) {
 	    if (!supplier.isFundAllocationAllowed(totalValue)) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public Supplier getSupplier() {
 	// if (getSuppliersCount() > 1) {
 	// throw new
 	// DomainException("This method should be used only when 1 supplier is present");
 	// }
 	//
 	// return getSuppliersCount() == 0 ? null : getSuppliers().get(0);
 	return getSelectedSupplier();
     }
 
     public String getRefundeeName() {
 	final Person refundee = getRefundee();
 	return refundee == null ? null : refundee.getName();
     }
 
     public Set<AcquisitionRequestItem> getAcquisitionRequestItemsSet() {
 	return (HashSet<AcquisitionRequestItem>) addAcquisitionRequestItemsSetToArg(new HashSet<AcquisitionRequestItem>());
     }
 
     public Collection<AcquisitionRequestItem> addAcquisitionRequestItemsSetToArg(
 	    final Collection<AcquisitionRequestItem> collection) {
 	for (final RequestItem item : getRequestItems()) {
 	    collection.add((AcquisitionRequestItem) item);
 	}
 	return collection;
     }
 
     @Override
     public AcquisitionProcess getProcess() {
 	return getAcquisitionProcess();
     }
 
     public boolean isPayed() {
 	final String reference = getPaymentReference();
 	return reference != null && !reference.isEmpty();
     }
 
     public boolean hasAdditionalCosts() {
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    final Money additionalCost = acquisitionRequestItem.getAdditionalCostValue();
 	    if (additionalCost != null && additionalCost.isGreaterThan(Money.ZERO)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     @Override
     public SortedSet<AcquisitionRequestItem> getOrderedRequestItemsSet() {
 	return (SortedSet<AcquisitionRequestItem>) addAcquisitionRequestItemsSetToArg(new TreeSet<AcquisitionRequestItem>(
 		AcquisitionRequestItem.COMPARATOR_BY_REFERENCE));
     }
 
     public void validateInvoiceNumber(String invoiceNumber) {
 	for (PaymentProcessInvoice invoice : getInvoices()) {
 	    if (invoice.getInvoiceNumber().equals(invoiceNumber)) {
		throw new DomainException("acquisitionProcess.message.exception.InvoiceWithSameNumber");
 	    }
 	}
     }
 
 }
