 package models;
 
 import java.io.Serializable;
 import javax.persistence.*;
 //import models.CaseTest.CaseTestPK;
 
 
 @Entity
 @Table(name="CASE_TEST_RESULTS_COMMENTS")
 public class CaseTestResultsComments implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@Column(name="case_test_FK", unique=true, nullable=false)
 	private long caseTestFK;
 
 //	@OneToOne(mappedBy = "resultsAndComments")
 ////	@JoinColumn(name="case_test_FK")
 //	private CaseTest caseTest;
 	
 	@Column(length = 8)
 	private String results;
 	
//	@ManyToOne(cascade = CascadeType.ALL)
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
 	@JoinColumn(name="informational_comment", nullable = true)
 	private Comment informationalComment;
 	
 	
//	@ManyToOne(cascade = CascadeType.ALL)
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
 	@JoinColumn(name="actual_comment")
 	private Comment actualComment;
 	
 	
 	//bi-directional many-to-one association to Employee
 	@ManyToOne
 	@JoinColumn(name="employee_entered")
 	private Employee employeeEntered;
 
 
 	public long getCaseTestFK() {
 		return caseTestFK;
 	}
 
 
 	public String getResults() {
 		return results;
 	}
 
 
 	public Comment getInformationalComment() {
 		return informationalComment;
 	}
 
 
 	public Comment getActualComment() {
 		return actualComment;
 	}
 
 
 	public Employee getEmployeeEntered() {
 		return employeeEntered;
 	}
 
 
 	public void setCaseTestFK(long caseTestFK) {
 		this.caseTestFK = caseTestFK;
 	}
 
 
 	public void setResults(String results) {
 		this.results = results;
 	}
 
 
 	public void setInformationalComment(Comment informationalComment) {
 		this.informationalComment = informationalComment;
 	}
 
 
 	public void setActualComment(Comment actualComment) {
 		this.actualComment = actualComment;
 	}
 
 
 	public void setEmployeeEntered(Employee employeeEntered) {
 		this.employeeEntered = employeeEntered;
 	}
 
 
 	
 
 }
