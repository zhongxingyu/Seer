 package edu.uci.lighthouse.model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 
 @Entity
 @Inheritance(strategy = InheritanceType.JOINED)
 public abstract class LighthouseEntity {
 
 	@Id
//	@Column(columnDefinition = "VARCHAR(300)")
 	private String fullyQualifiedName = "";
 
 	public LighthouseEntity(String fqn) {
 		this.fullyQualifiedName = fqn;
 	}
 
 	protected LighthouseEntity() {
 	}
 
 	public String getProjectName() {
 		return fullyQualifiedName.replaceAll("\\..*", "");
 	}
 	
 	public String getPackageName(){
 		//FIXME: Right now this method just work for classes and interfaces.
 		String result = getFullyQualifiedName().replace("."+getShortName(), "").replace(getProjectName()+".", "");
 		return result.equals(getProjectName()) ? "" : result;
 	}
 	
 	public String getFullyQualifiedName() {
 		return fullyQualifiedName;
 	}
 
 	protected void setFullyQualifiedName(String fullyQualifiedName) {
 		this.fullyQualifiedName = fullyQualifiedName;
 	}
 
 	public String getShortName() {
 		return fullyQualifiedName.replaceAll("(\\w+\\.)*", "");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return getFullyQualifiedName();
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime
 				* result
 				+ ((fullyQualifiedName == null) ? 0 : fullyQualifiedName
 						.hashCode());
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
 		LighthouseEntity other = (LighthouseEntity) obj;
 		if (fullyQualifiedName == null) {
 			if (other.fullyQualifiedName != null)
 				return false;
 		} else if (!fullyQualifiedName.equals(other.fullyQualifiedName))
 			return false;
 		return true;
 	}
 
 }
