 /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  Copyright (C) 2008 CEJUG - Ceará Java Users Group
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  
  This file is part of the CEJUG-CLASSIFIEDS Project - an  open source classifieds system
  originally used by CEJUG - Ceará Java Users Group.
  The project is hosted https://cejug-classifieds.dev.java.net/
  
  You can contact us through the mail dev@cejug-classifieds.dev.java.net
  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
 package net.java.dev.cejug.classifieds.server.ejb3.entity;
 
 import java.util.Collection;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 /**
  * A domain is company or a group of people. The domain should be registered in
  * the Cejug-Classifieds system, and a domain has a unique domain name.
  * 
  * @author $Author$
  * @version $Rev$ ($Date$)
  */
 @Entity
 @Table(name = "DOMAIN")
 public class DomainEntity extends AbstractEntity {
 
 	@Column(name = "NAME", nullable = false, unique = true)
 	private String domainName;
 
 	// TODO: mapear booleano
 	@Column(nullable = false)
 	private Boolean sharedQuota;
 
 	@Column(name = "BRAND", nullable = false)
 	private String brand;
 
 	@OneToMany(mappedBy = "domain")
 	private Collection<QuotaEntity> quotas;
 
	/*@OneToMany(mappedBy = "domain")
	private Collection<CategoryEntity> categories;*/
 
 	public Boolean getSharedQuota() {
 
 		return sharedQuota;
 	}
 
 	public void setSharedQuota(Boolean sharedQuota) {
 
 		this.sharedQuota = sharedQuota;
 	}
 
 	public String getBrand() {
 
 		return brand;
 	}
 
 	public void setBrand(String brand) {
 
 		this.brand = brand;
 	}
 
 	public Collection<QuotaEntity> getQuotas() {
 
 		return quotas;
 	}
 
 	public void setQuotas(Collection<QuotaEntity> quotas) {
 
 		this.quotas = quotas;
 	}
 
 	public String getDomainName() {
 
 		return domainName;
 	}
 
 	public void setDomainName(String domainName) {
 
 		this.domainName = domainName;
 	}
 
 	public Collection<CategoryEntity> getCategories() {
 		return categories;
 	}
 
 	public void setCategories(Collection<CategoryEntity> categories) {
 		this.categories = categories;
 	}
 
 }
