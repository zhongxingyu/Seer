 package com.jwxicc.cricket.entity;
 
 import java.io.Serializable;
 import javax.persistence.*;
 import java.util.Set;
 
 /**
  * The persistent class for the PARTNERSHIP database table.
  * 
  */
 @Entity
 @Table(name = "PARTNERSHIP")
 public class Partnership implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(unique = true, nullable = false)
 	private int partnershipId;
 
 	@Column
 	private float overs;
 
 	@Column
 	private float oversAtEnd;
 
 	@Column
 	private int runsScored;
 
 	@Column(nullable = false)
 	private int scoreAtEnd;
 
 	@Column(nullable = false)
 	private int wicket;
 
 	// bi-directional many-to-one association to Inning
 	@ManyToOne
 	@JoinColumn(name = "inningsId", nullable = false)
 	private Inning inning;
 
 	// bi-directional many-to-one association to PartnershipPlayers
 	@OneToMany(mappedBy = "partnership", fetch = FetchType.EAGER)
 	@OrderBy("battingPosition ASC")
 	private Set<PartnershipPlayer> partnershipPlayers;
 
 	public Partnership() {
 	}
 
 	public float getOvers() {
 		return this.overs;
 	}
 
 	public void setOvers(float overs) {
 		this.overs = overs;
 	}
 
 	public int getPartnershipId() {
 		return partnershipId;
 	}
 
 	public void setPartnershipId(int partnershipId) {
 		this.partnershipId = partnershipId;
 	}
 
 	public float getOversAtEnd() {
 		return oversAtEnd;
 	}
 
 	public void setOversAtEnd(float oversAtEnd) {
 		this.oversAtEnd = oversAtEnd;
 	}
 
 	public int getRunsScored() {
 		return runsScored;
 	}
 
 	public void setRunsScored(int runsScored) {
 		this.runsScored = runsScored;
 	}
 
 	public int getScoreAtEnd() {
 		return scoreAtEnd;
 	}
 
 	public void setScoreAtEnd(int scoreAtEnd) {
 		this.scoreAtEnd = scoreAtEnd;
 	}
 
 	public Set<PartnershipPlayer> getPartnershipPlayers() {
 		return partnershipPlayers;
 	}
 
 	public void setPartnershipPlayers(Set<PartnershipPlayer> partnershipPlayers) {
 		this.partnershipPlayers = partnershipPlayers;
 	}
 
 	public int getWicket() {
 		return this.wicket;
 	}
 
 	public void setWicket(int wicket) {
 		this.wicket = wicket;
 	}
 
 	public Inning getInning() {
 		return this.inning;
 	}
 
 	public void setInning(Inning inning) {
 		this.inning = inning;
 	}
 
 }
