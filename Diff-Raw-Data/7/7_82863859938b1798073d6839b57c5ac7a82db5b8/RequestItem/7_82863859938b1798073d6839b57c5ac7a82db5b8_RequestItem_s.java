 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import myorg.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixframework.pstm.Transaction;
 
 public abstract class RequestItem extends RequestItem_Base {
 
     public RequestItem() {
 	super();
 	setOjbConcreteClass(getClass().getName());
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
     }
 
     public abstract Money getValue();
 
     public abstract Money getRealValue();
 
     public abstract BigDecimal getVatValue();
 
     public Money getTotalAssigned() {
 	Money sum = Money.ZERO;
 	for (UnitItem unitItem : getUnitItems()) {
 	    sum = sum.add(unitItem.getShareValue());
 	}
 	return sum;
     }
 
     public Money getTotalRealAssigned() {
 	Money sum = Money.ZERO;
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getRealShareValue() != null) {
 		sum = sum.add(unitItem.getRealShareValue());
 	    }
 	}
 	return sum;
     }
 
     protected void delete() {
 	Transaction.deleteObject(this);
     }
 
     public UnitItem getUnitItemFor(Unit unit) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit() == unit) {
 		return unitItem;
 	    }
 	}
 	return null;
     }
 
     public void createUnitItem(Financer financer, Money shareValue) {
 	new UnitItem(financer, this, shareValue, Boolean.FALSE, Boolean.FALSE);
     }
 
     public abstract void createUnitItem(Unit unit, Money shareValue);
 
     public boolean hasBeenApprovedBy(final Person person) {
 	for (final UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit().isResponsible(person) && !unitItem.isApproved()) {
 		return false;
 	    }
 	}
 	return !getUnitItemsSet().isEmpty();
     }
 
     public void approve(final Person person) {
 	modifySubmittedForFundsAllocationStateFor(person, Boolean.TRUE);
     }
 
     public void unapprove(final Person person) {
 	modifySubmittedForFundsAllocationStateFor(person, Boolean.FALSE);
     }
 
     public void unapprove() {
 	for (UnitItem unitItem : getUnitItems()) {
 	    unitItem.setSubmitedForFundsAllocation(Boolean.FALSE);
 	}
     }
 
     private void modifySubmittedForFundsAllocationStateFor(final Person person, final Boolean value) {
 	for (final UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit().isResponsible(person)) {
 		unitItem.setSubmitedForFundsAllocation(value);
 	    }
 	}
     }
 
     public boolean isPartiallyApproved() {
 	if (getUnitItemsCount() == 0) {
 	    return false;
 	}
 
 	Boolean value = null;
 	for (final UnitItem unitItem : getUnitItems()) {
 	    Boolean approved = unitItem.isApproved();
 	    if (value == null) {
 		value = approved;
 	    }
 	    if (value != approved) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isApproved() {
 	if (getUnitItemsCount() == 0) {
 	    return false;
 	}
 	for (final UnitItem unitItem : getUnitItems()) {
 	    if (!unitItem.isApproved()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean isPartiallyAuthorized() {
 	if (getUnitItemsCount() == 0) {
 	    return false;
 	}
 
 	Boolean value = null;
 	for (final UnitItem unitItem : getUnitItems()) {
 	    Boolean authorized = unitItem.getItemAuthorized();
 	    if (value == null) {
 		value = authorized;
 	    }
 	    if (value != authorized) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isResponsible(final Person person) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit().isResponsible(person)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean hasBeenAuthorizedBy(final Person person) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit().isResponsible(person) && !unitItem.getItemAuthorized()) {
 		return false;
 	    }
 	}
 	return !getUnitItems().isEmpty();
     }
 
     public void authorizeBy(Person person) {
 	modifyAuthorizationStateFor(person, Boolean.TRUE);
     }
 
     public void unathorizeBy(Person person) {
 	modifyAuthorizationStateFor(person, Boolean.FALSE);
     }
 
     private void modifyAuthorizationStateFor(Person person, Boolean value) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit().isResponsible(person)) {
 		unitItem.setItemAuthorized(value);
 	    }
 	}
     }
 
     public boolean isAuthorized() {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (!unitItem.getItemAuthorized()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public List<UnitItem> getSortedUnitItems() {
 	List<UnitItem> unitItems = new ArrayList<UnitItem>(getUnitItems());
 	Collections.sort(unitItems, new Comparator<UnitItem>() {
 
 	    public int compare(UnitItem unitItem1, UnitItem unitItem2) {
 		return unitItem1.getUnit().getPresentationName().compareTo(unitItem2.getUnit().getPresentationName());
 	    }
 
 	});
 
 	return unitItems;
     }
 
     public void clearRealShareValues() {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getRealShareValue() != null) {
 		unitItem.setRealShareValue(null);
 	    }
 	}
     }
 
     public abstract boolean isFilledWithRealValues();
 
     public boolean isValueFullyAttributedToUnits() {
 	Money totalValue = Money.ZERO;
 	for (UnitItem unitItem : getUnitItems()) {
 	    totalValue = totalValue.add(unitItem.getShareValue());
 	}
 
 	return totalValue.equals(getValue());
     }
 
     public boolean isRealValueFullyAttributedToUnits() {
 	Money realValue = getRealValue();
 	if (realValue == null) {
 	    return false;
 	}
 	Money totalValue = Money.ZERO;
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getRealShareValue() != null) {
 		totalValue = totalValue.add(unitItem.getRealShareValue());
 	    }
 	}
 
 	return totalValue.equals(realValue);
     }
 
     public abstract Money getTotalAmountForCPV(final int year);
 
     public void confirmInvoiceBy(Person person) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit().isResponsible(person)) {
 		unitItem.getConfirmedInvoices().clear();
 		for (PaymentProcessInvoice invoice : getInvoicesFiles()) {
 		    unitItem.addConfirmedInvoices(invoice);
 		}
 	    }
 	}
     }
 
     public void unconfirmInvoiceBy(Person person) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit().isResponsible(person)) {
 		unitItem.getConfirmedInvoices().clear();
 	    }
 	}
     }
 
     public void unconfirmInvoiceForAll() {
 	for (UnitItem unitItem : getUnitItems()) {
 	    unitItem.getConfirmedInvoices().clear();
 	}
     }
 
     public boolean hasAtLeastOneInvoiceConfirmation() {
 	return !getConfirmedInvoices().isEmpty();
     }
 
     public boolean isWithInvoicesPartiallyConfirmed() {
 	return hasAtLeastOneInvoiceConfirmation() && !isConfirmForAllInvoices();
     }
 
     public <T extends PaymentProcessInvoice> List<T> getConfirmedInvoices() {
 	return getConfirmedInvoices(null);
     }
 
     public <T extends PaymentProcessInvoice> List<T> getConfirmedInvoices(Person person) {
 	List<T> invoices = new ArrayList<T>();
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (person == null || unitItem.getFinancer().getUnit().isResponsible(person)) {
 		invoices.addAll((List<T>) unitItem.getConfirmedInvoices());
 	    }
 	}
 	return invoices;
     }
 
     public <T extends PaymentProcessInvoice> List<T> getUnconfirmedInvoices(Person person) {
 	List<T> invoices = new ArrayList<T>();
 	invoices.addAll((List<T>) getInvoicesFiles());
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (person == null || unitItem.getFinancer().getUnit().isResponsible(person)) {
 		invoices.removeAll((List<T>) unitItem.getConfirmedInvoices());
 	    }
 	}
 	return invoices;
     }
 
     public boolean isConfirmedForAllInvoices(Person person) {
 	List<PaymentProcessInvoice> allInvoices = getInvoicesFiles();
 
 	if (allInvoices.isEmpty()) {
 	    return false;
 	}
 
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (person == null || unitItem.getFinancer().getUnit().isResponsible(person)) {
 		if (!unitItem.getConfirmedInvoices().containsAll(allInvoices)) {
 		    return false;
 		}
 	    }
 	}
 
 	return true;
     }
 
     public boolean isConfirmForAllInvoices() {
 	return isConfirmedForAllInvoices(null);
     }
 
     public boolean isCurrentRealValueFullyAttributedToUnits() {
 	return getInvoicesFiles().isEmpty() ? true : isRealValueFullyAttributedToUnits();
     }
 
 }
