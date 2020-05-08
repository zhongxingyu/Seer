 package org.chai.kevin.dashboard;
 
 import javax.persistence.Basic;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import org.chai.kevin.Expression;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 @Entity(name="Calculation")
 @Table(name="dhsst_dashboard_calculation")
 @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
 public class DashboardCalculation {
 
 	private Long id;
 	private String groupUuid;
 	private Expression expression;
 	
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@Basic
 	public String getGroupUuid() {
 		return groupUuid;
 	}
 	public void setGroupUuid(String groupUuid) {
 		this.groupUuid = groupUuid;
 	}
 	
	@ManyToOne(optional=true)
 	public Expression getExpression() {
 		return expression;
 	}
 	public void setExpression(Expression expression) {
 		this.expression = expression;
 	}
 	
 	@Override
 	public String toString() {
 		return "Calculation [groupUuid=" + groupUuid + ", expression=" + expression + "]";
 	}
 	
 }
