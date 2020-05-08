 package br.com.findplaces.jpa.entity;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import src.main.java.br.com.findplaces.jpa.entity.spatial.SequenceGenerator;
 
 
 @Entity
 @Table(name="TB_USER_TYPE")
 @SequenceGenerator(name = "seq_user_type", sequenceName = "seq_user_type")  
public class UserType extends BaseEntity {
 
 	private static final long serialVersionUID = 1L;
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	@Column
 	private Long id;
 	
 	@Column
 	private String name;
 	
 	
 	public void setId(Long id){
 		this.id = id;
 	}
 	
 	public Long getId() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 }
