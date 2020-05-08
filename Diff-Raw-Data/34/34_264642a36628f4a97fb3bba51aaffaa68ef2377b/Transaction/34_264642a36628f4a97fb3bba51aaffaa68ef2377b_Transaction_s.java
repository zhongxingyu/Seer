 /**
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
 package org.chai.chwcf.transaction;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.chai.chwcf.organisation.Cooperative;
import org.hibernate.annotations.Index;
 
 /**
  * @author Jean Kahigiso M.
  * 
  */
 @Entity(name = "Transaction")
 @Table(name = "chwcf_transaction")
 public class Transaction implements Serializable {
 	
 	private static final long serialVersionUID = 5434125360035328554L;
 	
 	private Long id;
 	private Cooperative cooperative;
 	private Category category;
	private Integer enteredBy;
 	private Integer validatedBy;
 	private Date transactionDate;
 	private Date recordedDate;
 	private String description;
 	private Integer amount;
 	private boolean approval = false;
 	private Date approvalDate;
 
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@ManyToOne(targetEntity = Cooperative.class, optional = false)
 	@JoinColumn(nullable = false)
 	public Cooperative getCooperative() {
 		return cooperative;
 	}
 
 	public void setCooperative(Cooperative cooperative) {
 		this.cooperative = cooperative;
 	}
 
 	@ManyToOne(targetEntity = Category.class, optional = false)
 	@JoinColumn(nullable = false)
 	public Category getCategory() {
 		return category;
 	}
 
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 	
	public void setEnteredBy(Integer enteredBy) {
 		this.enteredBy = enteredBy;
 	}
     @Basic(optional=false)
	public Integer getEnteredBy() {
 		return enteredBy;
 	}
 
 	public void setValidatedBy(Integer validatedBy) {
 		this.validatedBy = validatedBy;
 	}
 	@Basic(optional=true)
 	public Integer getValidatedBy() {
 		return validatedBy;
 	}
 
 	public void setTransactionDate(Date transactionDate) {
 		this.transactionDate = transactionDate;
 	}
 	
 	@Basic(optional = false)
 	@Temporal(TemporalType.DATE)
 	public Date getTransactionDate() {
 		return transactionDate;
 	}
 
 	@Basic(optional = false)
 	@Temporal(TemporalType.TIMESTAMP)
 	public Date getRecordedDate() {
 		return recordedDate;
 	}
 
 	public void setRecordedDate(Date recordedDate) {
 		this.recordedDate = recordedDate;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getDescription() {
 		return description;
 	}
     @Column(nullable=false)
 	public Integer getAmount() {
 		return amount;
 	}
 
 	public void setAmount(Integer amount) {
 		this.amount = amount;
 	}
 
 	public boolean isApproval() {
 		return approval;
 	}
 
 	public void setApproval(boolean approval) {
 		this.approval = approval;
 	}
 	
 	public void setApprovalDate(Date approvalDate) {
 		this.approvalDate = approvalDate;
 	}
 	@Basic(optional = true)
 	@Temporal(TemporalType.TIMESTAMP)
 	public Date getApprovalDate() {
 		return approvalDate;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
 		Transaction other = (Transaction) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 		return "Transaction [id=" + id + ", cooperative=" + cooperative
 				+ ", category=" + category + ", enteredBy=" + enteredBy
 				+ ", transactionDate=" + transactionDate + ", amount=" + amount
 				+ ", approval=" + approval + "]";
 	}
 		
 }
