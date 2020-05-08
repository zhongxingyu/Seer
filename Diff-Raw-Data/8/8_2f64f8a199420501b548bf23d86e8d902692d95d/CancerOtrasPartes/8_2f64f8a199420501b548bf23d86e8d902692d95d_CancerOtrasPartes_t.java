 /**
  * 
  */
 package com.isesalud.model;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.validator.constraints.Length;
 
 import com.isesalud.support.components.BaseModel;
 
 /**
  * @author Ing. Ari Gerardo Sela M.
  *
  */
 
 @Entity
 @Table(name="cancerOtrasPartes")
 
 public class CancerOtrasPartes extends BaseModel{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7685088043306810304L;
 
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	@NotNull
 	@Column(name="id", nullable=false, unique=true)
 	private Long id;
 	
 	@Column(name="parteCuerpo", nullable = false, length = 100)
 	@NotNull
 	@Length(max=100)
 	private String parteCuerpo;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name="paciente", nullable = false)
 	private Paciente paciente;
 	
 	public CancerOtrasPartes() {
 		this.id = new Long(0L);
 	}
 	
 	public CancerOtrasPartes(String parteCuerpo){
 		this.id = new Long(0L);
 		this.parteCuerpo = parteCuerpo;
 	}
 	
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
	public String getParteCuerpo() {
		return parteCuerpo;
	}
	
	public void setParteCuerpo(String parteCuerpo) {
		this.parteCuerpo = parteCuerpo;
	}
	
 }
