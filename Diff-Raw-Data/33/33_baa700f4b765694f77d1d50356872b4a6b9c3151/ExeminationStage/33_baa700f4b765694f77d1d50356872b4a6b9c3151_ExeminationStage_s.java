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
 @Inheritance(strategy=InheritanceType.SINGLE_TABLE)
 @DiscriminatorValue("ExeminationStage")
 public class ExeminationStage extends icm.dao.Stage implements Serializable {
 	public ExeminationStage() {
 	}
 	
 	@Column(name="Result", nullable=true, length=10)	
	@org.hibernate.annotations.Type(type="icm.dao.ExaminationStageResult")	
 	@Enumerated(EnumType.STRING)
 	private icm.dao.ExaminationStageResult result;
 	
 	public void setResult(icm.dao.ExaminationStageResult value) {
 		this.result = value;
 	}
 	
 	public icm.dao.ExaminationStageResult getResult() {
 		return result;
 	}
 	
 	public String toString() {
 		return super.toString();
 	}
 	
 }
