 /**
  * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
  * 
  * This is an automatic generated file. It will be regenerated every time 
  * you generate persistence class.
  * 
  * Modifying its content may cause the program not work, or your work may lost.
  */
 
 /**
  * Licensee: Ort Braude College
  * License Type: Academic
  */
 package icm.dao;
 
 import java.io.Serializable;
 import javax.persistence.*;
 @Entity
 @org.hibernate.annotations.Proxy(lazy=false)
 @Table(name="Employee")
 public class Employee implements Serializable {
 	public Employee() {
 	}
 	
 	private java.util.Set this_getSet (int key) {
 		if (key == icm.dao.ORMConstants.KEY_EMPLOYEE_ADMINISTRATEDSYSTEMS) {
 			return ORM_administratedSystems;
 		}
 		else if (key == icm.dao.ORMConstants.KEY_EMPLOYEE_CHANGELOG) {
 			return ORM_changeLog;
 		}
 		else if (key == icm.dao.ORMConstants.KEY_EMPLOYEE_EVALUATEDSTAGES) {
 			return ORM_evaluatedStages;
 		}
 		else if (key == icm.dao.ORMConstants.KEY_EMPLOYEE_SUPERVISEDREQUESTS) {
 			return ORM_supervisedRequests;
 		}
 		else if (key == icm.dao.ORMConstants.KEY_EMPLOYEE_EXECUTEDSTAGES) {
 			return ORM_executedStages;
 		}
 		else if (key == icm.dao.ORMConstants.KEY_EMPLOYEE_MAILS) {
 			return ORM_mails;
 		}
 		
 		return null;
 	}
 	
 	@Transient	
 	org.orm.util.ORMAdapter _ormAdapter = new org.orm.util.AbstractORMAdapter() {
 		public java.util.Set getSet(int key) {
 			return this_getSet(key);
 		}
 		
 	};
 	
 	@Column(name="EmployeeId", nullable=false)	
 	@Id	
 	@GeneratedValue(generator="ICM_DAO_EMPLOYEE_EMPLOYEEID_GENERATOR")	
 	@org.hibernate.annotations.GenericGenerator(name="ICM_DAO_EMPLOYEE_EMPLOYEEID_GENERATOR", strategy="native")	
 	private int employeeId;
 	
 	@Column(name="Password", nullable=true, length=255)	
 	private String password;
 	
 	@Column(name="Name", nullable=true, length=255)	
 	private String name;
 	
 	@Column(name="Surname", nullable=true, length=255)	
 	private String surname;
 	
 	@Column(name="Email", nullable=true, length=255)	
 	private String email;
 	
 	@Column(name="Type", nullable=true, length=10)	
	@org.hibernate.annotations.Type(type="icm.dao.EmployeeTypeUserType")	
 	@Enumerated(EnumType.STRING)
 	private icm.dao.EmployeeType type;
 	
 	@OneToMany(mappedBy="administer", targetEntity=icm.dao.InformationSystem.class)	
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.LOCK})	
 	@org.hibernate.annotations.LazyCollection(org.hibernate.annotations.LazyCollectionOption.TRUE)	
 	private java.util.Set ORM_administratedSystems = new java.util.HashSet();
 	
 	@OneToMany(mappedBy="employee", targetEntity=icm.dao.ChangeLog.class)	
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.LOCK})	
 	@org.hibernate.annotations.LazyCollection(org.hibernate.annotations.LazyCollectionOption.TRUE)	
 	private java.util.Set ORM_changeLog = new java.util.HashSet();
 	
 	@OneToMany(mappedBy="evaluator", targetEntity=icm.dao.EvaluationStage.class)	
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.LOCK})	
 	@org.hibernate.annotations.LazyCollection(org.hibernate.annotations.LazyCollectionOption.TRUE)	
 	private java.util.Set ORM_evaluatedStages = new java.util.HashSet();
 	
 	@OneToMany(mappedBy="supervisor", targetEntity=icm.dao.Request.class)	
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.LOCK})	
 	@org.hibernate.annotations.LazyCollection(org.hibernate.annotations.LazyCollectionOption.TRUE)	
 	private java.util.Set ORM_supervisedRequests = new java.util.HashSet();
 	
 	@OneToMany(mappedBy="executionLead", targetEntity=icm.dao.ExecutionStage.class)	
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.LOCK})	
 	@org.hibernate.annotations.LazyCollection(org.hibernate.annotations.LazyCollectionOption.TRUE)	
 	private java.util.Set ORM_executedStages = new java.util.HashSet();
 	
 	@OneToMany(mappedBy="owner", targetEntity=icm.dao.EmployeeMail.class)	
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.LOCK})	
 	@org.hibernate.annotations.LazyCollection(org.hibernate.annotations.LazyCollectionOption.TRUE)	
 	private java.util.Set ORM_mails = new java.util.HashSet();
 	
 	private void setEmployeeId(int value) {
 		this.employeeId = value;
 	}
 	
 	public int getEmployeeId() {
 		return employeeId;
 	}
 	
 	public int getORMID() {
 		return getEmployeeId();
 	}
 	
 	public void setPassword(String value) {
 		this.password = value;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 	
 	public void setName(String value) {
 		this.name = value;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setSurname(String value) {
 		this.surname = value;
 	}
 	
 	public String getSurname() {
 		return surname;
 	}
 	
 	public void setEmail(String value) {
 		this.email = value;
 	}
 	
 	public String getEmail() {
 		return email;
 	}
 	
 	public void setType(icm.dao.EmployeeType value) {
 		this.type = value;
 	}
 	
 	public icm.dao.EmployeeType getType() {
 		return type;
 	}
 	
 	private void setORM_AdministratedSystems(java.util.Set value) {
 		this.ORM_administratedSystems = value;
 	}
 	
 	private java.util.Set getORM_AdministratedSystems() {
 		return ORM_administratedSystems;
 	}
 	
 	@Transient	
 	public final icm.dao.InformationSystemSetCollection administratedSystems = new icm.dao.InformationSystemSetCollection(this, _ormAdapter, icm.dao.ORMConstants.KEY_EMPLOYEE_ADMINISTRATEDSYSTEMS, icm.dao.ORMConstants.KEY_INFORMATIONSYSTEM_ADMINISTER, icm.dao.ORMConstants.KEY_MUL_ONE_TO_MANY);
 	
 	private void setORM_ChangeLog(java.util.Set value) {
 		this.ORM_changeLog = value;
 	}
 	
 	private java.util.Set getORM_ChangeLog() {
 		return ORM_changeLog;
 	}
 	
 	@Transient	
 	public final icm.dao.ChangeLogSetCollection changeLog = new icm.dao.ChangeLogSetCollection(this, _ormAdapter, icm.dao.ORMConstants.KEY_EMPLOYEE_CHANGELOG, icm.dao.ORMConstants.KEY_CHANGELOG_EMPLOYEE, icm.dao.ORMConstants.KEY_MUL_ONE_TO_MANY);
 	
 	private void setORM_EvaluatedStages(java.util.Set value) {
 		this.ORM_evaluatedStages = value;
 	}
 	
 	private java.util.Set getORM_EvaluatedStages() {
 		return ORM_evaluatedStages;
 	}
 	
 	@Transient	
 	public final icm.dao.EvaluationStageSetCollection evaluatedStages = new icm.dao.EvaluationStageSetCollection(this, _ormAdapter, icm.dao.ORMConstants.KEY_EMPLOYEE_EVALUATEDSTAGES, icm.dao.ORMConstants.KEY_EVALUATIONSTAGE_EVALUATOR, icm.dao.ORMConstants.KEY_MUL_ONE_TO_MANY);
 	
 	private void setORM_SupervisedRequests(java.util.Set value) {
 		this.ORM_supervisedRequests = value;
 	}
 	
 	private java.util.Set getORM_SupervisedRequests() {
 		return ORM_supervisedRequests;
 	}
 	
 	@Transient	
 	public final icm.dao.RequestSetCollection supervisedRequests = new icm.dao.RequestSetCollection(this, _ormAdapter, icm.dao.ORMConstants.KEY_EMPLOYEE_SUPERVISEDREQUESTS, icm.dao.ORMConstants.KEY_REQUEST_SUPERVISOR, icm.dao.ORMConstants.KEY_MUL_ONE_TO_MANY);
 	
 	private void setORM_ExecutedStages(java.util.Set value) {
 		this.ORM_executedStages = value;
 	}
 	
 	private java.util.Set getORM_ExecutedStages() {
 		return ORM_executedStages;
 	}
 	
 	@Transient	
 	public final icm.dao.ExecutionStageSetCollection executedStages = new icm.dao.ExecutionStageSetCollection(this, _ormAdapter, icm.dao.ORMConstants.KEY_EMPLOYEE_EXECUTEDSTAGES, icm.dao.ORMConstants.KEY_EXECUTIONSTAGE_EXECUTIONLEAD, icm.dao.ORMConstants.KEY_MUL_ONE_TO_MANY);
 	
 	private void setORM_Mails(java.util.Set value) {
 		this.ORM_mails = value;
 	}
 	
 	private java.util.Set getORM_Mails() {
 		return ORM_mails;
 	}
 	
 	@Transient	
 	public final icm.dao.EmployeeMailSetCollection mails = new icm.dao.EmployeeMailSetCollection(this, _ormAdapter, icm.dao.ORMConstants.KEY_EMPLOYEE_MAILS, icm.dao.ORMConstants.KEY_EMPLOYEEMAIL_OWNER, icm.dao.ORMConstants.KEY_MUL_ONE_TO_MANY);
 	
 	public String toString() {
 		return String.valueOf(getEmployeeId());
 	}
 	
 }
