 package com.df.idm.entity;
 
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.OneToMany;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import org.eclipse.persistence.annotations.Indexes;
 import org.eclipse.persistence.annotations.Index;
 import org.eclipse.persistence.annotations.JoinFetch;
 import org.eclipse.persistence.annotations.JoinFetchType;
 import org.springframework.security.core.GrantedAuthority;
 
 @Entity
 @Table(name = "ROLES")
 public class Role implements GrantedAuthority {
 
 	private static final long serialVersionUID = 1L;
 
 	public static final String SYS_DOMAIN = "";
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.SEQUENCE)
 	@SequenceGenerator(initialValue = 5000, allocationSize = 1, name = "ROLE_SEQ")
 	@Column(name = "ID")
 	private long id;
 
 	@Column(name = "NAME", length = 256)
 	private String name;
 
 	@Column(name = "DOMAIN", length = 64)
 	private String domain;
 
 	@Column(length = 1025)
 	private String description;
 
 	@OneToMany
 	@JoinFetch(value = JoinFetchType.INNER)
 	@JoinTable(name = "ROLE_PERMISSION", joinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID"), inverseJoinColumns = @JoinColumn(name = "PERMISSION_ID", referencedColumnName = "ID"))
 	private List<Permission> permissions;
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public List<Permission> getPermissions() {
 		return permissions;
 	}
 
 	public void setPermissions(List<Permission> permissions) {
 		this.permissions = permissions;
 	}
 
 	public String getDomain() {
 		return domain;
 	}
 
 	public void setDomain(String domain) {
 		this.domain = domain;
 	}
 
 	@Override
 	public String getAuthority() {
 		return name;
 	}
 
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 
 		if (obj instanceof Role) {
 			Role other = (Role) obj;
 			if (this.getDomain().equals(other.getDomain())) {
 				return this.getName().equals(other.getName());
 			} else {
 				return false;
 			}
 		}
 
 		return false;
 	}
 
 	public int hashCode() {
 		return (this.getDomain() + this.getName()).hashCode();
 	}
 
 	public String toString() {
 		return "Role [domain=" + this.getDomain() + ", name=" + this.getName() + "]";
 	}
 }
