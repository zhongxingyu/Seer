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
 package org.chai.chwcf.organisation;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Basic;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import org.chai.chwcf.person.Member;
 import org.chai.chwcf.transaction.Transaction;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 import org.hisp.dhis.organisationunit.OrganisationUnit;
 
 /**
  * @author Jean Kahigiso M.
  * 
  */
 
 @Entity(name = "Cooperative")
 @Table(name = "chwcf_cooperative")
 public class Cooperative implements Serializable {
 
 	private static final long serialVersionUID = 2480175919930656867L;
 
 	private Long id;
 	private String name;
 	private String description;
 	private OrganisationUnit organisationUnit;
 	private String registrationNumber;
 	private RegistrationLevel registrationLevel;
	private Integer numberOfVillages;
	private Integer numberOfCells;
 	private Date createDate;
 	private List<Member> members = new ArrayList<Member>();
 	private List<Share> shares = new ArrayList<Share>();
 	private List<Activity> activities = new ArrayList<Activity>();
 	private List<Transaction> transactions = new ArrayList<Transaction>();
 	private List<PbfScore> scores = new ArrayList<PbfScore>();
 	private List<Training> trainings = new ArrayList<Training>();
 	private List<Supervision> supervisions = new ArrayList<Supervision>();
 
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Basic(optional = false)
 	public String getName() {
 		return name;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setOrganisationUnit(OrganisationUnit organisationUnit) {
 		this.organisationUnit = organisationUnit;
 	}
 
 	@ManyToOne(targetEntity = OrganisationUnit.class, optional = false)
 	@JoinColumn(nullable = false)
 	public OrganisationUnit getOrganisationUnit() {
 		return organisationUnit;
 	}
 
 	public void setMembers(List<Member> members) {
 		this.members = members;
 	}
 
 	@OneToMany(targetEntity = Member.class, mappedBy = "cooperative")
 	@Cascade({ CascadeType.ALL, CascadeType.DELETE })
 	public List<Member> getMembers() {
 		return members;
 	}
 
 	public void setRegistrationNumber(String registrationNumber) {
 		this.registrationNumber = registrationNumber;
 	}
 
 	public String getRegistrationNumber() {
 		return registrationNumber;
 	}
 
 	public void setRegistrationLevel(RegistrationLevel registrationLevel) {
 		this.registrationLevel = registrationLevel;
 	}
 
 	@ManyToOne(targetEntity = RegistrationLevel.class, optional = false)
 	@JoinColumn(nullable = false)
 	public RegistrationLevel getRegistrationLevel() {
 		return registrationLevel;
 	}
 
	public Integer getNumberOfVillages() {
		return numberOfVillages;
	}

	public void setNumberOfVillages(Integer numberOfVillages) {
		this.numberOfVillages = numberOfVillages;
	}

	public void setNumberOfCells(Integer numberOfCells) {
		this.numberOfCells = numberOfCells;
	}

	public Integer getNumberOfCells() {
		return numberOfCells;
	}

 	@Basic(optional = false)
 	@Temporal(TemporalType.DATE)
 	public Date getCreateDate() {
 		return createDate;
 	}
 
 	public void setCreateDate(Date createDate) {
 		this.createDate = createDate;
 	}
 
 	public void setShares(List<Share> shares) {
 		this.shares = shares;
 	}
 
 	@OneToMany(targetEntity = Share.class, mappedBy = "cooperative")
 	@Cascade({ CascadeType.ALL, CascadeType.DELETE })
 	@OrderBy(value = "order")
 	public List<Share> getShares() {
 		return shares;
 	}
 
 	public void setActivities(List<Activity> activities) {
 		this.activities = activities;
 	}
 
 	@ManyToMany(targetEntity = Activity.class)
 	@OrderBy(value = "order")
 	public List<Activity> getActivities() {
 		return activities;
 	}
 
 	public void setTransactions(List<Transaction> transactions) {
 		this.transactions = transactions;
 	}
 
 	@OneToMany(targetEntity = Transaction.class, mappedBy = "cooperative")
 	@Cascade({ CascadeType.ALL, CascadeType.DELETE })
 	public List<Transaction> getTransactions() {
 		return transactions;
 	}
 
 	public void setScores(List<PbfScore> scores) {
 		this.scores = scores;
 	}
 
 	@OneToMany(targetEntity = PbfScore.class, mappedBy = "cooperative")
 	@Cascade({ CascadeType.ALL, CascadeType.DELETE })
 	@OrderBy(value = "order")
 	public List<PbfScore> getScores() {
 		return scores;
 	}
 
 	public void setTrainings(List<Training> trainings) {
 		this.trainings = trainings;
 	}
 
 	@OneToMany(targetEntity = Training.class, mappedBy = "cooperative")
 	@Cascade({ CascadeType.ALL, CascadeType.DELETE })
 	@OrderBy(value = "order")
 	public List<Training> getTrainings() {
 		return trainings;
 	}
 
 	public void setSupervisions(List<Supervision> supervisions) {
 		this.supervisions = supervisions;
 	}
 
 	@OneToMany(targetEntity = Supervision.class, mappedBy = "cooperative")
 	@Cascade({ CascadeType.ALL, CascadeType.DELETE })
 	@OrderBy(value = "order")
 	public List<Supervision> getSupervisions() {
 		return supervisions;
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
 		Cooperative other = (Cooperative) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Cooperative [id=" + id + ", organisationUnit="
 				+ organisationUnit + "]";
 	}
 
 	@Transient
 	public void addTransaction(Transaction transaction) {
 		transaction.setCooperative(this);
 		transactions.add(transaction);
 	}
 
 	@Transient
 	public void addActivity(Activity activity) {
 		activity.addCooperative(this);
 		activities.add(activity);
 		Collections.sort(activities);
 	}
 
 	@Transient
 	public void addMember(Member member) {
 		member.setCooperative(this);
 		members.add(member);
 
 	}
 
 	@Transient
 	public void addScore(PbfScore score) {
 		score.setCooperative(this);
 		scores.add(score);
 		Collections.sort(scores);
 
 	}
 
 	@Transient
 	public void addShare(Share share) {
 		share.setCooperative(this);
 		shares.add(share);
 		Collections.sort(shares);
 	}
 
 	@Transient
 	public void addTraining(Training training) {
 		training.setCooperative(this);
 		trainings.add(training);
 		Collections.sort(trainings);
 
 	}
 
 	@Transient
 	public void addSupervision(Supervision supervision) {
 		supervision.setCooperative(this);
 		supervisions.add(supervision);
 		Collections.sort(supervisions);
 
 	}
 }
