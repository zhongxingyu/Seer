 package businessLayer;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 
 import org.joda.money.Money;
 
 import util.Config;
 import util.Percent;
 
 @Entity
 @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
 @NamedQuery(name = "Installment.findInstallmentsWithNearDeadLine", query = "SELECT  i FROM Installment i WHERE i.date <= :date  "
 		+ "AND i.paidInvoice = FALSE AND i.deadlineNotified = FALSE")
 public abstract class Installment implements Serializable {
 
 	/**
 	 * 
 	 */
 	protected static final long serialVersionUID = 1L;
 
 	public Installment() {
 		this.wholeTaxableAmount = Money.zero(Config.currency);
 		this.date = new Date();
 		this.invoiceDate = new Date();
 		this.voucherDate = new Date();
 		this.deadlineNotified = false;
 	}
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	protected int id;
 
 	@ManyToOne
 	protected Contract contract;
 
 	@Temporal(TemporalType.DATE)
 	@NotNull
 	protected Date date;
 
 	@Embedded
 	protected Percent IVA_amount;
 
 	@Embedded
 	protected Money wholeTaxableAmount;
 
 	@Min(0)
 	protected int voucherNumber;
 
 	@Temporal(TemporalType.DATE)
 	protected Date voucherDate;
 
 	@Min(0)
 	protected int pendingNumber;
 	@Min(0)
 	protected int invoiceNumber;
 
 	@Temporal(TemporalType.DATE)
 	protected Date invoiceDate;
 
 	protected boolean paidInvoice;
 	protected boolean reportRequired;
 	protected String note;
 
 	protected boolean deadlineNotified;
 
 	
 
 	public Contract getContract() {
 		return contract;
 	}
 
 	public void setContract(Contract c) {
 		this.contract = c;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public int getVoucherNumber() {
 		return voucherNumber;
 	}
 
 	public void setVoucherNumber(int voucherNumber) {
 		this.voucherNumber = voucherNumber;
 	}
 
 	public Date getVoucherDate() {
 		return voucherDate;
 	}
 
 	public void setVoucherDate(Date voucherDate) {
 		this.voucherDate = voucherDate;
 	}
 
 	public int getPendingNumber() {
 		return pendingNumber;
 	}
 
 	public void setPendingNumber(int pendingNumber) {
 		this.pendingNumber = pendingNumber;
 	}
 
 	public int getInvoiceNumber() {
 		return invoiceNumber;
 	}
 
 	public void setInvoiceNumber(int invoiceNumber) {
 		this.invoiceNumber = invoiceNumber;
 	}
 
 	public Date getInvoiceDate() {
 		return invoiceDate;
 	}
 
 	public void setInvoiceDate(Date invoiceDate) {
 		this.invoiceDate = invoiceDate;
 	}
 
 	public boolean isPaidInvoice() {
 		return paidInvoice;
 	}
 
 	public void setPaidInvoice(boolean paidInvoice) {
 		this.paidInvoice = paidInvoice;
 	}
 
 	public boolean isReportRequired() {
 		return reportRequired;
 	}
 
 	public void setReportRequired(boolean reportRequired) {
 		this.reportRequired = reportRequired;
 	}
 
 	public String getNote() {
 		return note;
 	}
 
 	public void setNote(String note) {
 		this.note = note;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 
 	public Money getWholeAmount() {
 		return wholeTaxableAmount
 				.plus(IVA_amount.computeOn(wholeTaxableAmount));
 	}
 
 	public Money getWholeTaxableAmount() {
 		return wholeTaxableAmount;
 	}
 
 	public void setWholeTaxableAmount(Money wholeTaxableAmount) {
 		this.wholeTaxableAmount = wholeTaxableAmount;
 	}
 
 	public Percent getIVA_amount() {
 		return IVA_amount;
 	}
 
 	public boolean isDeadlineNotified() {
 		return deadlineNotified;
 	}
 
 	public void setDeadlineNotified(boolean deadlineNotified) {
 		this.deadlineNotified = deadlineNotified;
 	}
 
 	public abstract void copy(Installment copy);
 
 	protected void _copy(Installment copy) {
 
 		this.id = copy.id;
 		this.date = new Date(copy.date.getTime());
 		this.voucherNumber = copy.voucherNumber;
 		this.voucherDate = new Date(copy.voucherDate.getTime());
 		this.pendingNumber = copy.pendingNumber;
 		this.invoiceNumber = copy.invoiceNumber;
 		this.invoiceDate = new Date(copy.invoiceDate.getTime());
 		this.paidInvoice = copy.paidInvoice;
 		this.reportRequired = copy.reportRequired;
 		this.note = copy.note;
 		this.contract = copy.getContract();
 		this.wholeTaxableAmount = copy.wholeTaxableAmount;
 
 
 	}
 
 	
 }
