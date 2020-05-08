 package com.tda.model.applicationuser;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Transient;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 import org.hibernate.annotations.ForeignKey;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.UserDetails;
 
 import com.tda.model.utils.FormType;
 
 @Entity
 public class ApplicationUser implements UserDetails {
 
 	private static final long serialVersionUID = 1L;
 
 	private Long id;
 
 	@NotNull
 	@Size(min = 4, max = 8)
 	private String password;
 
 	@NotNull
 	@Size(min = 4, max = 8)
 	private String confirmPassword;
 
 	@NotNull
 	@Size(min = 5, max = 12)
 	private String username;
 
 	private Collection<Authority> myAuthorities;
 
 	private boolean isAccountNonExpired = true;
 
 	private boolean isAccountNonLocked = true;
 
 	private boolean isCredentialsNonExpired = true;
 
 	private boolean isEnabled = true;
 
 	public ApplicationUser() {
 		super();
 	}
 
 	public ApplicationUser(String username, String password,
 			Collection<Authority> authorities, boolean isAccountNonExpired,
 			boolean isAccountNonLocked, boolean isCredentialsNonExpired,
 			boolean isEnabled) {
 		this.username = username;
 		this.password = password;
 		this.myAuthorities = authorities;
 		this.isAccountNonExpired = isAccountNonExpired;
 		this.isAccountNonLocked = isAccountNonLocked;
 		this.isCredentialsNonExpired = isCredentialsNonExpired;
 		this.isEnabled = isEnabled;
 	}
 
 	@Basic
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	@Column(unique = true)
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	@ManyToMany(fetch = FetchType.EAGER, targetEntity = Authority.class)
 	@Fetch(value = FetchMode.SUBSELECT)
 	@ForeignKey(name = "ID_USER", inverseName = "ID_AUTH")
 	public Collection<Authority> getMyAuthorities() {
 		return myAuthorities;
 	}
 
 	public void setMyAuthorities(Collection<Authority> myAuthorities) {
 		this.myAuthorities = myAuthorities;
 	}
 
 	@Basic
 	public boolean isAccountNonExpired() {
 		return isAccountNonExpired;
 	}
 
 	public void setAccountNonExpired(boolean isAccountNonExpired) {
 		this.isAccountNonExpired = isAccountNonExpired;
 	}
 
 	@Basic
 	public boolean isAccountNonLocked() {
 		return isAccountNonLocked;
 	}
 
 	public void setAccountNonLocked(boolean isAccountNonLocked) {
 		this.isAccountNonLocked = isAccountNonLocked;
 	}
 
 	@Basic
 	public boolean isCredentialsNonExpired() {
 		return isCredentialsNonExpired;
 	}
 
 	public void setCredentialsNonExpired(boolean isCredentialsNonExpired) {
 		this.isCredentialsNonExpired = isCredentialsNonExpired;
 	}
 
 	@Basic
 	public boolean isEnabled() {
 		return isEnabled;
 	}
 
 	public void setEnabled(boolean isEnabled) {
 		this.isEnabled = isEnabled;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	public Long getId() {
 		return id;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + (isAccountNonExpired ? 1231 : 1237);
 		result = prime * result + (isAccountNonLocked ? 1231 : 1237);
 		result = prime * result + (isCredentialsNonExpired ? 1231 : 1237);
 		result = prime * result + (isEnabled ? 1231 : 1237);
 		result = prime * result
 				+ ((myAuthorities == null) ? 0 : myAuthorities.hashCode());
 		result = prime * result
 				+ ((password == null) ? 0 : password.hashCode());
 		result = prime * result
 				+ ((username == null) ? 0 : username.hashCode());
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
 		ApplicationUser other = (ApplicationUser) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (isAccountNonExpired != other.isAccountNonExpired)
 			return false;
 		if (isAccountNonLocked != other.isAccountNonLocked)
 			return false;
 		if (isCredentialsNonExpired != other.isCredentialsNonExpired)
 			return false;
 		if (isEnabled != other.isEnabled)
 			return false;
 		if (myAuthorities == null) {
 			if (other.myAuthorities != null)
 				return false;
 		} else if (!myAuthorities.equals(other.myAuthorities))
 			return false;
 		if (password == null) {
 			if (other.password != null)
 				return false;
 		} else if (!password.equals(other.password))
 			return false;
 		if (username == null) {
 			if (other.username != null)
 				return false;
 		} else if (!username.equals(other.username))
 			return false;
 		return true;
 	}
 
 	@Transient
 	public Collection<GrantedAuthority> getAuthorities() {
 		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
 		for (Authority authority : getMyAuthorities()) {
 			list.add(new GrantedAuthorityImpl(authority.getAuthority()));
 		}
 		return list;
 	}
 
 	public String getConfirmPassword() {
 		return confirmPassword;
 	}
 
 	public void setConfirmPassword(String confirmPassword) {
 		this.confirmPassword = confirmPassword;
 	}
 
 	@Transient
 	public boolean isAdmin() {
 		for (Authority authority : myAuthorities) {
 			if (authority.getId() == 1)
 				return true;
 		}
 		return false;
 	}
 
 	@Transient
 	public boolean isDentist() {
 		for (Authority authority : myAuthorities) {
 			if (authority.getId() == 2)
 				return true;
 		}
 		return false;
 	}
 
 	@Transient
 	public boolean isSocial() {
 		for (Authority authority : myAuthorities) {
 			if (authority.getId() == 4)
 				return true;
 		}
 		return false;
 	}
 
 	@Transient
 	public boolean isNurse() {
 		for (Authority authority : myAuthorities) {
 			if (authority.getId() == 5)
 				return true;
 		}
 		return false;
 	}
 
 	@Transient
 	public boolean isPediatrician() {
 		for (Authority authority : myAuthorities) {
 			if (authority.getId() == 6)
 				return true;
 		}
 		return false;
 	}
 
 	@Transient
 	public FormType getCurrRole() {
 		for (Authority authority : myAuthorities) {
 			if (authority.getId() == 2)
 				return FormType.dentist;
 			if (authority.getId() == 4)
 				return FormType.socialworker;
 			if (authority.getId() == 5)
 				return FormType.nurse;
 			if (authority.getId() == 6)
 				return FormType.pediatrician;
 		}
		return FormType.admin;
 	}
 }
