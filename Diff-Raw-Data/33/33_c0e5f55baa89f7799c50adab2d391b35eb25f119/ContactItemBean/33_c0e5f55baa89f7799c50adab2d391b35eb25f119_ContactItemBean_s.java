 package org.businessmanager.web.bean;
 
 /**
  * @author Christian Ternes
  *
  */
 public class ContactItemBean {
 
 	private String scope;
 	
 	private String value;
 	
 	private boolean isDefault;
 
 	public String getScope() {
 		return scope;
 	}
 
 	public void setScope(String scope) {
 		this.scope = scope;
 	}
 
 	public String getValue() {
 		return value;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 
	public boolean isDefault() {
 		return isDefault;
 	}
 
	public void setDefault(boolean isDefault) {
 		this.isDefault = isDefault;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (isDefault ? 1231 : 1237);
 		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
 		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
 		ContactItemBean other = (ContactItemBean) obj;
 		if (isDefault != other.isDefault)
 			return false;
 		if (scope == null) {
 			if (other.scope != null)
 				return false;
 		} else if (!scope.equals(other.scope))
 			return false;
 		if (value == null) {
 			if (other.value != null)
 				return false;
 		} else if (!value.equals(other.value))
 			return false;
 		return true;
 	}
 	
 }
