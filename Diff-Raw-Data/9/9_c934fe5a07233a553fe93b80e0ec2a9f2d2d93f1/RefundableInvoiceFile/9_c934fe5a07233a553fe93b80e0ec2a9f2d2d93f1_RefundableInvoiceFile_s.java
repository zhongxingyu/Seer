 package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestItem;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 
 public class RefundableInvoiceFile extends RefundableInvoiceFile_Base {
 
     public RefundableInvoiceFile(String invoiceNumber, LocalDate invoiceDate, Money value, BigDecimal vatValue,
 	    Money refundableValue, byte[] invoiceFile, String filename, RefundItem item, Supplier supplier) {
 	super();
 	check(item, supplier, value, vatValue, refundableValue);
 	this.setInvoiceNumber(invoiceNumber);
 	this.setInvoiceDate(invoiceDate);
 	this.setValue(value);
 	this.setVatValue(vatValue);
 	this.setRefundableValue(refundableValue);
 	this.addRequestItems(item);
 	this.setSupplier(supplier);
 	this.setFilename(filename);
 	this.setContent(invoiceFile);
     }
 
     public void delete() {
 	for (RequestItem item : getRequestItems()) {
 	    item.clearRealShareValues();
 	}
 	getRequestItems().clear();
 	removeSupplier();
 	super.delete();
     }
 
     private void check(RequestItem item, Supplier supplier, Money value, BigDecimal vatValue, Money refundableValue) {
 	RefundProcess process = item.getRequest().getProcess();
 	if (!process.getSkipSupplierFundAllocation() && !supplier.isFundAllocationAllowed(value)) {
 	    throw new DomainException("acquisitionRequestItem.message.exception.fundAllocationNotAllowed", DomainException
 		    .getResourceFor("resources/AcquisitionResources"));
 	}
 	Money realValue = item.getRealValue();
 	Money estimatedValue = item.getValue();
 
 	if ((realValue != null && realValue.add(refundableValue).isGreaterThan(estimatedValue)) || realValue == null
		&& refundableValue.isGreaterThan(estimatedValue)) {
 	    throw new DomainException("refundItem.message.info.realValueLessThanRefundableValue", DomainException
 		    .getResourceFor("resources/AcquisitionResources"));
 	}
 
 	if (new Money(value.addPercentage(vatValue).getRoundedValue()).isLessThan(refundableValue)) {
 	    throw new DomainException("refundItem.message.info.refundableValueCannotBeBiggerThanInvoiceValue", DomainException
 		    .getResourceFor("resources/AcquisitionResources"));
 	}
     }
 
     public void editValues(Money value, BigDecimal vatValue, Money refundableValue) {
 	check(getRefundItem(), getSupplier(), value, vatValue, refundableValue);
 	this.setValue(value);
 	this.setVatValue(vatValue);
 	this.setRefundableValue(refundableValue);
     }
 
     public void resetValues() {
 	this.setValue(Money.ZERO);
 	this.setVatValue(BigDecimal.ZERO);
 	this.setRefundableValue(Money.ZERO);
     }
 
     public Money getValueWithVat() {
 	return getValue().addPercentage(getVatValue());
     }
 
     public RefundItem getRefundItem() {
 	List<RequestItem> items = getRequestItems();
 	if (items.size() > 1) {
 	    throw new DomainException("acquisitionRequestItem.message.exception.thereShouldBeOnlyOneRefundItemAssociated");
 	}
 	return items != null ? (RefundItem) items.get(0) : null;
     }
 
 }
