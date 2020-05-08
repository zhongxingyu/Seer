 /**
  * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
  * 
  * This is an automatic generated file. It will be regenerated every time 
  * you generate persistence class.
  * 
  * Modifying its content may cause the program not work, or your work may lost.
  */
 
 /**
  * Licensee: Ort Braude College
  * License Type: Academic
  */
 package icm.dao;
 
 import java.io.Serializable;
 import javax.persistence.*;
 @Entity
 @org.hibernate.annotations.Proxy(lazy=false)
 @Table(name="Stage")
 @Inheritance(strategy=InheritanceType.SINGLE_TABLE)
 @DiscriminatorColumn(name="Discriminator", discriminatorType=DiscriminatorType.STRING)
 @DiscriminatorValue("Stage")
 public abstract class Stage implements Serializable {
 	public Stage() {
 	}
 	
 	private void this_setOwner(Object owner, int key) {
 		if (key == icm.dao.ORMConstants.KEY_STAGE_NEXTSTAGE) {
 			this.nextStage = (icm.dao.Stage) owner;
 		}
 		
 		else if (key == icm.dao.ORMConstants.KEY_STAGE_EXTENSIONREQUEST) {
 			this.extensionRequest = (icm.dao.ExtensionRequest) owner;
 		}
 		
 		else if (key == icm.dao.ORMConstants.KEY_STAGE_PREVIOUSSTAGE) {
 			this.previousStage = (icm.dao.Stage) owner;
 		}
 		
 		else if (key == icm.dao.ORMConstants.KEY_STAGE_PARENTREQUEST) {
 			this.parentRequest = (icm.dao.Request) owner;
 		}
 	}
 	
 	@Transient	
 	org.orm.util.ORMAdapter _ormAdapter = new org.orm.util.AbstractORMAdapter() {
 		public void setOwner(Object owner, int key) {
 			this_setOwner(owner, key);
 		}
 		
 	};
 	
 	@Column(name="StageId", nullable=false)	
 	@Id	
 	@GeneratedValue(generator="ICM_DAO_STAGE_STAGEID_GENERATOR")	
 	@org.hibernate.annotations.GenericGenerator(name="ICM_DAO_STAGE_STAGEID_GENERATOR", strategy="native")	
 	private int stageId;
 	
 	@OneToOne(targetEntity=icm.dao.ExtensionRequest.class, fetch=FetchType.LAZY)	
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.LOCK})	
 	@JoinColumns({ @JoinColumn(name="ExtensionRequestExtensionRequestId") })	
 	@org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.NO_PROXY)	
 	private icm.dao.ExtensionRequest extensionRequest;
 	
 	@ManyToOne(targetEntity=icm.dao.Request.class, fetch=FetchType.LAZY)	
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.LOCK})	
 	@JoinColumns({ @JoinColumn(name="RequestRequestID", referencedColumnName="RequestID", nullable=false) })	
 	@org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.NO_PROXY)	
 	private icm.dao.Request parentRequest;
 	
 	@OneToOne(targetEntity=icm.dao.Stage.class, fetch=FetchType.LAZY)	
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.LOCK})	
 	@JoinColumns({ @JoinColumn(name="StageStageId") })	
 	@org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.NO_PROXY)	
 	private icm.dao.Stage nextStage;
 	
 	@Column(name="StartedAt", nullable=false, length=20)	
 	private long startedAt;
 	
 	@Column(name="FinishedAt", nullable=false, length=20)	
 	private long finishedAt;
 	
 	@Column(name="Deadline", nullable=false, length=20)	
 	private long deadline;
 	
 	@Column(name="Status", nullable=true, length=10)	
	@org.hibernate.annotations.Type(type="icm.dao.StageStatusUserType")	
 	@Enumerated(EnumType.STRING)
 	private icm.dao.StageStatus status;
 	
 	@OneToOne(mappedBy="nextStage", targetEntity=icm.dao.Stage.class, fetch=FetchType.LAZY)	
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.LOCK})	
 	@org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.NO_PROXY)	
 	private icm.dao.Stage previousStage;
 	
 	private void setStageId(int value) {
 		this.stageId = value;
 	}
 	
 	public int getStageId() {
 		return stageId;
 	}
 	
 	public int getORMID() {
 		return getStageId();
 	}
 	
 	public void setStartedAt(long value) {
 		this.startedAt = value;
 	}
 	
 	public long getStartedAt() {
 		return startedAt;
 	}
 	
 	public void setFinishedAt(long value) {
 		this.finishedAt = value;
 	}
 	
 	public long getFinishedAt() {
 		return finishedAt;
 	}
 	
 	public void setDeadline(long value) {
 		this.deadline = value;
 	}
 	
 	public long getDeadline() {
 		return deadline;
 	}
 	
 	public void setStatus(icm.dao.StageStatus value) {
 		this.status = value;
 	}
 	
 	public icm.dao.StageStatus getStatus() {
 		return status;
 	}
 	
 	public void setNextStage(icm.dao.Stage value) {
 		if (this.nextStage != value) {
 			icm.dao.Stage lnextStage = this.nextStage;
 			this.nextStage = value;
 			if (value != null) {
 				nextStage.setPreviousStage(this);
 			}
 			else {
 				lnextStage.setPreviousStage(null);
 			}
 		}
 	}
 	
 	public icm.dao.Stage getNextStage() {
 		return nextStage;
 	}
 	
 	public void setExtensionRequest(icm.dao.ExtensionRequest value) {
 		if (this.extensionRequest != value) {
 			icm.dao.ExtensionRequest lextensionRequest = this.extensionRequest;
 			this.extensionRequest = value;
 			if (value != null) {
 				extensionRequest.setStage(this);
 			}
 			else {
 				lextensionRequest.setStage(null);
 			}
 		}
 	}
 	
 	public icm.dao.ExtensionRequest getExtensionRequest() {
 		return extensionRequest;
 	}
 	
 	public void setPreviousStage(icm.dao.Stage value) {
 		if (this.previousStage != value) {
 			icm.dao.Stage lpreviousStage = this.previousStage;
 			this.previousStage = value;
 			if (value != null) {
 				previousStage.setNextStage(this);
 			}
 			else {
 				lpreviousStage.setNextStage(null);
 			}
 		}
 	}
 	
 	public icm.dao.Stage getPreviousStage() {
 		return previousStage;
 	}
 	
 	public void setParentRequest(icm.dao.Request value) {
 		if (parentRequest != null) {
 			parentRequest.childStages.remove(this);
 		}
 		if (value != null) {
 			value.childStages.add(this);
 		}
 	}
 	
 	public icm.dao.Request getParentRequest() {
 		return parentRequest;
 	}
 	
 	/**
 	 * This method is for internal use only.
 	 */
 	public void setORM_ParentRequest(icm.dao.Request value) {
 		this.parentRequest = value;
 	}
 	
 	private icm.dao.Request getORM_ParentRequest() {
 		return parentRequest;
 	}
 	
 	public String toString() {
 		return String.valueOf(getStageId());
 	}
 	
 }
