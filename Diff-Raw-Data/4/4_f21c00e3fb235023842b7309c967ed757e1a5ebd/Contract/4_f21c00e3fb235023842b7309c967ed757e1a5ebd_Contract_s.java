 package businessLayer;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Access;
 import javax.persistence.AccessType;
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.OrderBy;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.Size;
 
 import org.joda.money.Money;
 
 import util.Config;
 import util.Percent;
 
 /**
  * Entity implementation class for Entity: Contract
  * 
  */
 
 @Entity
 @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
 @Access(AccessType.FIELD)
 public abstract class Contract implements Serializable {
 
 	/**
 	 * 
 	 */
 	protected static final long serialVersionUID = 1L;
 
 	public Contract() {
 		super();
 		installments = new ArrayList<>();
 		attachments = new ArrayList<>();
 		spentAmount = Money.zero(Config.currency);
 		reservedAmount = Money.zero(Config.currency);
 		IVA_amount = Percent.ZERO;
 		wholeTaxableAmount = Money.zero(Config.currency);
 	}
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	protected int id;
 
 	@Size(max = 1000)
 	protected String title;
 	
 
 	protected String protocolNumber;
 
 	@ManyToOne
 	protected ChiefScientist chief;
 
 	protected String contactPerson;
 
 	@ManyToOne
 	protected Company company;
 
 	@ManyToOne
 	protected Department department;
 
 	protected int CIA_projectNumber;
 	protected int inventoryNumber;
 
 	@Embedded
 	@AttributeOverrides({
 			@AttributeOverride(name = "money", column = @Column(name = "SPENT_AMOUNT")),
 			@AttributeOverride(name = "money.currency.code", column = @Column(name = "SPENT_AMOUNT_CODE")),
 			@AttributeOverride(name = "money.currency.decimalPlaces", column = @Column(name = "SPENT_AMOUNT_DECIMAL_PLACES")),
 			@AttributeOverride(name = "money.currency.numericCode", column = @Column(name = "SPENT_AMOUNT_NUMERIC_CODE")),
 			@AttributeOverride(name = "money.amount", column = @Column(name = "SPENT_AMOUNT_AMOUNT")) })
 	protected Money spentAmount;
 
 	@Embedded
 	@AttributeOverrides({
 			@AttributeOverride(name = "money", column = @Column(name = "RESERVED_AMOUNT")),
 			@AttributeOverride(name = "money.currency.code", column = @Column(name = "RESERVED_AMOUNT_CODE")),
 			@AttributeOverride(name = "money.currency.decimalPlaces", column = @Column(name = "RESERVED_AMOUNT_DECIMAL_PLACES")),
 			@AttributeOverride(name = "money.currency.numericCode", column = @Column(name = "RESERVED_AMOUNT_NUMERIC_CODE")),
 			@AttributeOverride(name = "money.amount", column = @Column(name = "RESERVED_AMOUNT_AMOUNT")) })
 	protected Money reservedAmount;
 	
 	
 	@Embedded
 	protected Percent IVA_amount;
 	
 	@Embedded
 	@AttributeOverrides({
 	@AttributeOverride(name="money", column=@Column(name="WHOLE_TAXABLE_AMOUNT")),
 	@AttributeOverride(name="money.currency.code", column=@Column(name="WHOLE_TAXABLE_AMOUNT_CODE")),
 	@AttributeOverride(name="money.currency.decimalPlaces", column=@Column(name="WHOLE_TAXABLE_AMOUNT_DECIMAL_PLACES")),
 	@AttributeOverride(name="money.currency.numericCode", column=@Column(name="WHOLE_TAXABLE_AMOUNT_NUMERIC_CODE")),
 	@AttributeOverride(name="money.amount", column=@Column(name="WHOLE_TAXABLE_AMOUNT_AMOUNT"))})
 	protected Money wholeTaxableAmount;
 
 	@Temporal(TemporalType.DATE)
 	protected Date approvalDate;
 
 	@Temporal(TemporalType.DATE)
 	protected Date beginDate;
 
 	@Temporal(TemporalType.DATE)
 	protected Date deadlineDate;
 
 	protected String note;
 
 	@OneToMany(mappedBy = "contract", cascade = { CascadeType.PERSIST,
 			CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.EAGER)
 	@OrderBy("date DESC")
 	protected List<Installment> installments;
 
 	@OneToMany(cascade = { CascadeType.REMOVE, CascadeType.PERSIST })
 	protected List<Attachment> attachments;
 	
 	@OneToOne(cascade = CascadeType.PERSIST)
 	protected ContractShareTable shareTable;
 	
 	public Money getWholeTaxableAmount() {
 		return wholeTaxableAmount;
 	}
 
 	public void setWholeTaxableAmount(Money wholeTaxableAmount) {
 		this.wholeTaxableAmount = wholeTaxableAmount;
 	}
 	
 	
 	public Money getWholeAmount() {
 		return wholeTaxableAmount.plus(IVA_amount.computeOn(wholeTaxableAmount));
 	}
 
 	public Percent getIVA_amount() {
 		return IVA_amount;
 	}
 
 	public void addInstallment(Installment i) {
 		i.setContract(this);
 		installments.add(i);
 
 	}
 
 	public void removeInstallment(Installment i) {
 		installments.remove(i);
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getProtocolNumber() {
 		return protocolNumber;
 	}
 
 	public void setProtocolNumber(String protocolNumber) {
 		this.protocolNumber = protocolNumber;
 	}
 
 	public String getContactPerson() {
 		return contactPerson;
 	}
 
 	public void setContactPerson(String contactPerson) {
 		this.contactPerson = contactPerson;
 	}
 
 	public ChiefScientist getChief() {
 		return chief;
 	}
 
 	public void setChief(ChiefScientist chief) {
 		this.chief = chief;
 	}
 
 	public Company getCompany() {
 		return company;
 	}
 
 	public void setCompany(Company company) {
 		this.company = company;
 	}
 
 	public Department getDepartment() {
 		return department;
 	}
 
 	public void setDepartment(Department department) {
 		this.department = department;
 	}
 
 	public int getCIA_projectNumber() {
 		return CIA_projectNumber;
 	}
 
 	public void setCIA_projectNumber(int cIA_projectNumber) {
 		CIA_projectNumber = cIA_projectNumber;
 	}
 
 	public int getInventoryNumber() {
 		return inventoryNumber;
 	}
 
 	public void setInventoryNumber(int inventoryNumber) {
 		this.inventoryNumber = inventoryNumber;
 	}
 
 	public Money getSpentAmount() {
 		return spentAmount;
 	}
 
 	public void setSpentAmount(Money spentAmount) {
 		this.spentAmount = spentAmount;
 	}
 
 	public Money getReservedAmount() {
 		return reservedAmount;
 	}
 
 	public void setReservedAmount(Money reservedAmount) {
 		this.reservedAmount = reservedAmount;
 	}
 
 	public Date getApprovalDate() {
 		return approvalDate;
 	}
 
 	public void setApprovalDate(Date approvalDate) {
 		this.approvalDate = approvalDate;
 	}
 
 	public Date getBeginDate() {
 		return beginDate;
 	}
 
 	public void setBeginDate(Date beginDate) {
 		this.beginDate = beginDate;
 	}
 
 	public Date getDeadlineDate() {
 		return deadlineDate;
 	}
 
 	public void setDeadlineDate(Date deadlineDate) {
 		this.deadlineDate = deadlineDate;
 	}
 
 	public String getNote() {
 		return note;
 	}
 
 	public void setNote(String note) {
 		this.note = note;
 	}
 	public List<Installment> getInstallments() {
 
 		// TODO check
 		return new ArrayList<>(installments);
 	}
 
 	public void setInstallments(List<Installment> installments) {
 		this.installments = installments;
 	}
 
 	public List<Attachment> getAttachments() {
 		return attachments;
 	}
 
 	public void setAttachments(List<Attachment> attachments) {
 		this.attachments = attachments;
 	}
 	
 	public ContractShareTable getShareTable() {
 		return shareTable;
 	}
 
 	public void setShareTable(ContractShareTable shareTable) {
 		this.shareTable = shareTable;
 	}
 	
 	// fatturato
 	public Money getTurnOver() {
 		Money sum = Money.zero(Config.currency);
 		for (Installment i : installments) {
 
 			if (i.isPaidInvoice()) {
 				sum.plus(i.getWholeAmount());
 			}
 
 		}
 
 		return sum;
 	}
 	
 	@Access(AccessType.PROPERTY)
 	public boolean isClosed() {
 
 		return getWholeAmount().equals(getTurnOver());
 
 	}
 
 	public void setClosed(boolean closed) {
 		//serve ad Hibernate perchè è stupido
 	}
 	
 	
 	
 	
 
 }
