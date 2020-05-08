 // Copyright 2008 Thiago H. de Paula Figueiredo
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package br.com.arsmachina.authentication.entity;
 
 import java.io.Serializable;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.hibernate.validator.NotNull;
 
 /**
  * Class that represents a role an user can have in the application. It is used to avoid creating
  * {@link User} subclasses, thus solving the problem of the same user having more than one role at
  * the same time.
  * 
  * @author Thiago H. de Paula Figueiredo
  */
 @Entity
 @Table(name = "`role`")
 @Inheritance(strategy = InheritanceType.JOINED)
 public abstract class Role implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	private Integer id;
 
 	private User user;
 
 	/**
 	 * Returns the value of the <code>id</code> property.
 	 * 
 	 * @return a {@link Integer}.
 	 */
 	@Id
 	@GeneratedValue
 	public Integer getId() {
 		return id;
 	}
 
 	/**
 	 * Changes the value of the <code>id</code> property.
 	 * 
 	 * @param id a {@link Integer}.
 	 */
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	/**
 	 * Returns the value of the <code>user</code> property.
 	 * 
 	 * @return a {@link User}.
 	 */
 	@ManyToOne(optional = false)
 	@JoinColumn(nullable = false)
 	@NotNull
 	public User getUser() {
 		return user;
 	}
 
 	/**
 	 * Changes the value of the <code>user</code> property.
 	 * 
 	 * @param user a {@link User}.
 	 */
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return id != null ? id.hashCode() : super.hashCode();
 	}
 
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
		if (obj instanceof Role == false)
 			return false;
 		final Role other = (Role) obj;
 		if (id == null) {
			if (other.getId() != null)
 				return false;
 		}
		else if (!id.equals(other.getId()))
 			return false;
 		return true;
 	}
 
 	/**
 	 * Invokes <code>user.getEmail()<code>.
 	 * @return
 	 * @see br.com.arsmachina.authentication.entity.User#getEmail()
 	 */
 	@Transient
 	public String getEmail() {
 		return user.getEmail();
 	}
 
 	/**
 	 * Invokes <code>user.getLogin()<code>.
 	 * @return
 	 * @see br.com.arsmachina.authentication.entity.User#getLogin()
 	 */
 	@Transient
 	public String getLogin() {
 		return user.getLogin();
 	}
 
 	/**
 	 * Invokes <code>user.getName()<code>.
 	 * @return a {@link String}.
 	 * @see br.com.arsmachina.authentication.entity.User#getName()
 	 */
 	@Transient
 	public String getName() {
 		return user.getName();
 	}
 
 	/**
 	 * Invokes <code>user.isCredentialsExpired()<code>.
 	 * @return a <code>boolean</code>.
 	 * @see br.com.arsmachina.authentication.entity.User#isCredentialsExpired()
 	 */
 	@Transient
 	public boolean isCredentialsExpired() {
 		return user.isCredentialsExpired();
 	}
 
 	/**
 	 * Invokes <code>user.isEnabled()<code>.
 	 * @return a <code>boolean</code>.
 	 * @see br.com.arsmachina.authentication.entity.User#isEnabled()
 	 */
 	@Transient
 	public boolean isEnabled() {
 		return user.isEnabled();
 	}
 
 	/**
 	 * Invokes <code>user.isExpired()<code>.
 	 * @return a <code>boolean</code>.
 	 * @see br.com.arsmachina.authentication.entity.User#isExpired()
 	 */
 	@Transient
 	public boolean isExpired() {
 		return user.isExpired();
 	}
 
 	/**
 	 * Invokes <code>user.isLocked()<code>.
 	 * @return a <code>boolean</code>.
 	 * @see br.com.arsmachina.authentication.entity.User#isLocked()
 	 */
 	@Transient
 	public boolean isLocked() {
 		return user.isLocked();
 	}
 
 	/**
 	 * Invokes <code>user.isLoggedIn()<code>.
 	 * @return a <code>boolean</code>.
 	 * @see br.com.arsmachina.authentication.entity.User#isLoggedIn()
 	 */
 	@Transient
 	public boolean isLoggedIn() {
 		return user.isLoggedIn();
 	}
 
 }
