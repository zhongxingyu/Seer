 package org.chai.kevin.data;
 
 /* 
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.Translation;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 
 @Entity(name="Enum")
 @Table(name="dhsst_enum")
 @Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public class Enum {
 
 	private Long id;
 
 	private List<EnumOption> enumOptions = new ArrayList<EnumOption>();
 	private String code;
 	private Translation names = new Translation();
 	private Translation descriptions = new Translation();
 
 	@Id
 	@GeneratedValue
 	@Column
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 		
 	@OneToMany(mappedBy="enume", targetEntity=EnumOption.class, fetch=FetchType.EAGER)
 	@Fetch(FetchMode.SELECT)
 	@OrderBy(value="order")
 	public List<EnumOption> getEnumOptions() {
 		return enumOptions;
 	}
 	
 	public void setEnumOptions(List<EnumOption> enumOptions) {
 		this.enumOptions = enumOptions;
 	}
 	
 	public void addEnumOption(EnumOption enumOption) {
 		enumOptions.add(enumOption);
 		enumOption.setEnume(this);
 		Collections.sort(enumOptions);
 	}
	
	@Transient
	public List<EnumOption> getActiveEnumOptions() {
		List<EnumOption> result = new ArrayList<EnumOption>();
		for (EnumOption enumOption : getEnumOptions()) {
			if (!enumOption.getInactive()) result.add(enumOption);
		}
		return result;
	}
 
 	@Embedded
 	@AttributeOverrides({
 		@AttributeOverride(name="jsonText", column=@Column(name="jsonNames", nullable=false))
 	})
 	public Translation getNames() {
 		return names;
 	}
 
 	public void setNames(Translation names) {
 		this.names = names;
 	}
 	
 	@Embedded
 	@AttributeOverrides({
         @AttributeOverride(name="jsonText", column=@Column(name="jsonDescriptions", nullable=false))
 	})
 	public Translation getDescriptions() {
 		return descriptions;
 	}
 
 	public void setDescriptions(Translation descriptions) {
 		this.descriptions = descriptions;
 	}
 
 	@Basic(fetch=FetchType.EAGER)
 	public String getCode() {
 		return code;
 	}
 	
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 	@Transient
 	public EnumOption getOptionForValue(String value) {
 		for (EnumOption enumOption : getEnumOptions()) {
 			if (enumOption.getValue().equals(value)) return enumOption;
 		}
 		return null;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((code == null) ? 0 : code.hashCode());
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
 		Enum other = (Enum) obj;
 		if (code == null) {
 			if (other.code != null)
 				return false;
 		} else if (!code.equals(other.code))
 			return false;
 		return true;
 	}
 	
 	public boolean hasValue(String value) {
 		for (EnumOption enumOption : enumOptions) {
 			if (enumOption.getValue().equals(value)) return true;
 		}
 		return false;
 	}
 	
 }
