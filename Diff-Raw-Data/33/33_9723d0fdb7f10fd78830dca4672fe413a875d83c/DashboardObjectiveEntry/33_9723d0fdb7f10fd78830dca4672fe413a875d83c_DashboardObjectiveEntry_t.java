 package org.chai.kevin.dashboard;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import org.chai.kevin.Translatable;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.OnDelete;
 
 @Entity(name="WeightedObjective")
 @Table(name="dhsst_dashboard_objective_entry", 
 		uniqueConstraints=@UniqueConstraint(columnNames={"entry"}))
 public class DashboardObjectiveEntry implements Comparable<DashboardObjectiveEntry> {
 
 	private Long id;
 	private DashboardObjective parent;
 	private DashboardEntry entry;
 	private Integer weight;
 	private Integer order;
 	
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@OneToOne(targetEntity=DashboardEntry.class)
	@Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
 	public DashboardEntry getEntry() {
 		return entry;
 	}
 	public void setEntry(DashboardEntry entry) {
 		this.entry = entry;
 	}
 	
 	@Basic
 	public Integer getWeight() {
 		return weight;
 	}
 	public void setWeight(Integer weight) {
 		this.weight = weight;
 	}
 	
 	@Basic
 	@Column(name="ordering")
 	public Integer getOrder() {
 		return order;
 	}
 	public void setOrder(Integer order) {
 		this.order = order;
 	}
 	
 	@ManyToOne
	@JoinColumn
 	public DashboardObjective getParent() {
 		return parent;
 	}
 	public void setParent(DashboardObjective parent) {
 		this.parent = parent;
 	}	
 	
 	@Override
 	public String toString() {
 		return "DashboardObjectiveEntry [id=" + id + ", parent=" + parent
 				+ ", entry=" + entry + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((entry == null) ? 0 : entry.hashCode());
 		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
 		return result;
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		DashboardObjectiveEntry other = (DashboardObjectiveEntry) obj;
 		if (entry == null) {
 			if (other.entry != null)
 				return false;
 		} else if (!entry.equals(other.entry))
 			return false;
 		if (parent == null) {
 			if (other.parent != null)
 				return false;
 		} else if (!parent.equals(other.parent))
 			return false;
 		return true;
 	}
 	
 	@Override
 	public int compareTo(DashboardObjectiveEntry o) {
 		if (this.getOrder() == null) {
 			if (o.getOrder() == null) return 0;
 			else return 1;
 		}
 		if (o.getOrder() == null) return -1;
 		return this.getOrder().compareTo(o.getOrder());
 	}
 
 }
