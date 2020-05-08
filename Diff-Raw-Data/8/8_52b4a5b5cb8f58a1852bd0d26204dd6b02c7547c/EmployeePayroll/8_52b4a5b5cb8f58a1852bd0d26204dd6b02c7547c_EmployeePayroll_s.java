 package com.openhr.data;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 
 import com.openhr.taxengine.DeductionType;
 import com.openhr.taxengine.DeductionsDeclared;
 import com.openhr.taxengine.DeductionsDone;
 import com.openhr.taxengine.ExemptionType;
 import com.openhr.taxengine.ExemptionsDone;
 
 /**
  *
  * @author xmen
  */
 @Entity
 @Table(name = "emp_payroll_view", catalog = "payhumrepo", schema = "")
 @XmlRootElement
 @NamedQueries({
 	@NamedQuery(name = "EmployeePayroll.findByEmployeeId", query = "SELECT e FROM EmployeePayroll e WHERE e.employeeId = ?"),
     @NamedQuery(name = "EmployeePayroll.findAll", query = "SELECT e FROM EmployeePayroll e"),
     @NamedQuery(name = "EmployeePayroll.findByFullName", query = "SELECT e FROM EmployeePayroll e WHERE e.fullName = :fullName"),
     @NamedQuery(name = "EmployeePayroll.findByGrossSalary", query = "SELECT e FROM EmployeePayroll e WHERE e.grossSalary = :grossSalary")})
 public class EmployeePayroll implements Serializable {
 	private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id", nullable = false)
     private Integer id;
     @JoinColumn(name = "employeeId", referencedColumnName = "id", nullable = false)
     @ManyToOne(optional = false)
     private Employee employeeId;
     @Basic(optional = false)
     @Column(name = "FULL_NAME", nullable = false, length = 137)
     private String fullName;
     @Basic(optional = false)
     @Column(name = "GROSS_SALARY")
     private Double grossSalary;
     /*@Column(name = "BENEFIT_TYPE", length = 45)
     private String benefitType;
     // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
     @Column(name = "BENEFIT_AMNT", precision = 22)
     private Double benefitAmnt;*/
 
     @Basic(optional = false)
     @Column(name = "taxableIncome")
     private Double taxableIncome;
     
     @Basic(optional = false)
     @Column(name = "taxAmount")
     private Double taxAmount;
     
     @Basic(optional = false)
     @Column(name = "totalIncome")
     private Double totalIncome;
     
     @Basic(optional = false)
     @Column(name = "taxableOverseasIncome")
     private Double taxableOverseasIncome;
     
     @Basic(optional = false)
     @Column(name = "allowances")
     private Double allowances;
     
     @Basic(optional = false)
     @Column(name = "baseSalary", nullable = false)
     private Double baseSalary;
     
     @Basic(optional = false)
     @Column(name = "bonus")
     private Double bonus;
     
     @Basic(optional = false)
     @Column(name = "accomodationAmount")
     private Double accomodationAmount;
     
     @Basic(optional = false)
     @Column(name = "employerSS")
     private Double employerSS;
     
     @Basic(optional = false)
     @Column(name = "accomodationType", nullable = false)
     private Integer accomodationType;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "employeeId", fetch=FetchType.EAGER)
     @Fetch(FetchMode.SELECT)
     private List<DeductionsDeclared> deductionsDeclared;
     
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "employeeId", fetch=FetchType.EAGER)
     @Fetch(FetchMode.SELECT)
     private List<DeductionsDone> deductionsDone;
     
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "employeeId", fetch=FetchType.EAGER)
     @Fetch(FetchMode.SELECT)
     private List<ExemptionsDone> exemptionsDone;
 
     @Basic(optional = false)
     @Column(name = "totalDeductions")
     private Double totalDeductions;
 
     @Basic(optional = false)
     @Column(name = "netPay")
     private Double netPay;
 
     
     public EmployeePayroll() {
     	this.taxableIncome = 0D;
         this.taxAmount = 0D;
         this.exemptionsDone = new ArrayList<ExemptionsDone>();
         this.totalIncome= 0D;
         this.taxableOverseasIncome = 0D;
         this.allowances = 0D;
         this.deductionsDone = new ArrayList<DeductionsDone>();
         this.baseSalary = 0D;
         this.bonus= 0D ;
         this.accomodationType= null ;
         this.accomodationAmount = 0D;
         this.employerSS = 0D;
         this.deductionsDeclared = new ArrayList<DeductionsDeclared>();
         this.grossSalary = 0D;
         this.netPay = 0D;
         this.totalDeductions = 0D;
     }
     
     public Double getAccomodationAmount() {
 		return accomodationAmount;
 	}
 
 	public Double getEmployerSS() {
 		return employerSS;
 	}
 
     public String getEmployeeid() {
         return employeeId.getEmployeeId();
     }
 
 	public Employee getEmployeeId() {
 		return employeeId;
 	}
 
 	public void setEmployeeId(Employee employeeId) {
 		this.employeeId = employeeId;
 	}
 	
     public String getFullName() {
         return fullName;
     }
 
     public void setFullName(String fullName) {
         this.fullName = fullName;
     }
 
     public Double getGrossSalary() {
         return grossSalary == null ? 0D : grossSalary;
     }
 
     public void setGrossSalary(Double grossSalary) {
         this.grossSalary = grossSalary;
     }
 /*
     public String getBenefitType() {
         return benefitType;
     }
 
     public void setBenefitType(String benefitType) {
         this.benefitType = benefitType;
     }
 
     public Double getBenefitAmnt() {
         return benefitAmnt;
     }
 
     public void setBenefitAmnt(Double benefitAmnt) {
         this.benefitAmnt = benefitAmnt;
     }
 */
 
 	public Double getTaxableIncome() {
 		return this.taxableIncome;
 	}
 
 	public void setTaxAmount(Double taxAmount) {
 		this.taxAmount = taxAmount;
 	}
 
 	public void addExemption(ExemptionType eType, Double exemption) {
 		boolean found = false;
 		Exemptionstype ex=null;
 		for(ExemptionsDone ed: exemptionsDone) {
		 ex=ed.getType();
 			if(ex.getId() == eType.getValue()) {
 				ed.setAmount(ed.getAmount() + exemption);
 				found = true;
 				break;
 			}
 		}
 		
 		if(!found) {
			 ex.setId(eType.getValue());
 			exemptionsDone.add(new ExemptionsDone(this.employeeId, ex, exemption));
 		}
 		
 		totalDeductions += exemption;
 		
 		if(taxableIncome > exemption) {
 			taxableIncome -= exemption;
 		} else {
 			taxableIncome = 0D;
 		}
 	}
 
 	public void addExemption(ExemptionType eType, Double exemption,
 			int multiplier) {
 		Double exemptionAmt = exemption * multiplier;
 		
 		this.addExemption(eType, exemptionAmt);
 	}
 
 	public Double getBaseSalary() {
 		return this.baseSalary;
 	}
 	
 	public void addDeduction(DeductionType entity, Double amount) {
 		boolean found = false;
 		DeductionsType dx=null;
 		for(DeductionsDone dd: deductionsDone) {
 			if(dd.getType().getId() == entity.getValue()) {
 				dd.setAmount(dd.getAmount() + amount);
 				found = true;
 				break;
 			}
 		}
 		
 		if(!found) {
			dx.setId(entity.getValue());
 			deductionsDone.add(new DeductionsDone(this.employeeId, dx, amount));
 		}
 		
 		totalDeductions += amount;
 		
 		if(taxableIncome > amount) {
 			taxableIncome -= amount;
 		} else {
 			taxableIncome = 0D;
 		}
 	}
 
 	public static EmployeePayroll loadEmpPayroll(Integer empId) {
 		if(System.getProperty("DRYRUN") != null 
 		&& System.getProperty("DRYRUN").equalsIgnoreCase("true")) {
 			return populateTestData();
 		}
 		return null;		
 	}
 
 	public Double getAllowancesAmount() {
 		return this.allowances;
 	}
 
 	public Double getBonus() {
 		return this.bonus;
 	}
 
 	public AccomodationType getAccomodationType() {
 		if(this.accomodationType != null) {
 			if(accomodationType == AccomodationType.FREE_ACCOM_FROM_EMPLOYER_FULLY_FURNISHED.getValue()) {
 				return AccomodationType.FREE_ACCOM_FROM_EMPLOYER_FULLY_FURNISHED;
 			} else if(accomodationType == AccomodationType.FREE_ACCOM_FROM_EMPLOYER_NOT_FURNISHED.getValue()) {
 				return AccomodationType.FREE_ACCOM_FROM_EMPLOYER_NOT_FURNISHED;
 			}
 		} 
 		
 		return null;
 	}
 
 	public void setTotalIncome(Double totalIncome) {
 		this.totalIncome = totalIncome;
 		this.taxableIncome = totalIncome;
 	}
     
 	public Double getTotalIncome() {
 		return this.totalIncome;
 	}
 
 	public Double getTaxableOverseasIncome() {
 		return this.taxableOverseasIncome;
 	}
 	
 	public Double getTaxAmount() {
 		return taxAmount;
 	}
 
 	public void setTaxableOverseasIncome(Double taxableOverseasIncome) {
 		this.taxableOverseasIncome = taxableOverseasIncome;
 	}
 
 	public void setAllowances(Double allowances) {
 		this.allowances = allowances;
 	}
 
 	public void setBaseSalary(Double baseSalary) {
 		this.baseSalary = baseSalary;
 	}
 
 	public void setBonus(Double bonus) {
 		this.bonus = bonus;
 	}
 
 	public void setAccomodationType(Integer accomodationType) {
 		this.accomodationType = accomodationType;
 	}
 	
 	//Testing method
 	private static EmployeePayroll populateTestData() {
 		EmployeePayroll ePayroll = new EmployeePayroll();
 		ePayroll.setAccomodationType(AccomodationType.FREE_ACCOM_FROM_EMPLOYER_FULLY_FURNISHED.getValue());
 		ePayroll.setAllowances((double) 50000);
 		ePayroll.setBaseSalary((double) 30000000);
 		ePayroll.setBonus((double) 4000000);
 		
 		Employee emp = new Employee(0, "E1", "John", "", "Lui", "male", new Date(), new Date());
 		emp.setResidentType(ResidentType.LOCAL.getValue());
 		emp.setMarried("true");
 		
 		ePayroll.setEmployeeId(emp);
 		
 		DeductionsDeclared dd = new DeductionsDeclared(emp, DeductionType.EMPLOYEE_SOCIAL_SECURITY.getValue(), 0D);
 		ePayroll.deductionsDeclared.add(dd);
 		
 		return ePayroll;
 	}
 
 	public Double getTotalDeductions() {
 		return totalDeductions;
 	}
 
 	public void setTotalDeductions(Double totalDeductions) {
 		this.totalDeductions = totalDeductions;
 	}
 
 	public Double getNetPay() {
 		return netPay;
 	}
 
 	public void setNetPay(Double netPay) {
 		this.netPay = netPay;
 	}
 
 	public Double getAllowances() {
 		return allowances;
 	}
 
 	public void setTaxableIncome(Double taxableIncome) {
 		this.taxableIncome = taxableIncome;
 	}
 
 	public void setAccomodationAmount(Double accomAmt) {
 		this.accomodationAmount = accomAmt;
 	}
 	
 	public void setEmployerSS(Double employerSS) {
 		this.employerSS = employerSS;
 	}
 	
     public List<ExemptionsDone> getExemptionsDone() {
     	return exemptionsDone;
 	}
 
 	public List<DeductionsDone> getDeductionsDone() {
 		return deductionsDone;
 	}
 	
 	public List<DeductionsDeclared> getDeductionsDeclared() {
 		return deductionsDeclared;
 	}
 }
