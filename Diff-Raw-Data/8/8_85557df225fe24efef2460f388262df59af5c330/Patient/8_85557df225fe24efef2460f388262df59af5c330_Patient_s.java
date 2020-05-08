 package com.tda.model.patient;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
import javax.persistence.UniqueConstraint;
 
 @Entity
 public class Patient implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private Long id;
 
 	private String dni;
 
 	private String name;
 
 	private Sex sex;
 
 	private Date birthdate;
 
 	private String fatherName;
 
 	private String motherName;
 
 	public Patient() {
 		super();
 	}
 
 	public Patient(String dni, String name, Sex sex, Date birthdate,
 			String fatherName, String motherName) {
 		this.dni = dni;
 		this.name = name;
 		this.sex = sex;
 		this.birthdate = birthdate;
 		this.fatherName = fatherName;
 		this.motherName = motherName;
 	}
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@Column(unique = true)
 	public String getDni() {
 		return dni;
 	}
 
 	public void setDni(String dni) {
 		this.dni = dni;
 	}
 
 	@Basic
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Enumerated
 	public Sex getSex() {
 		return sex;
 	}
 
 	public void setSex(Sex sex) {
 		this.sex = sex;
 	}
 
 	@Basic
 	public Date getBirthdate() {
 		return birthdate;
 	}
 
 	public void setBirthdate(Date birthdate) {
 		this.birthdate = birthdate;
 	}
 
 	@Basic
 	public String getFatherName() {
 		return fatherName;
 	}
 
 	public void setFatherName(String fathersName) {
 		this.fatherName = fathersName;
 	}
 
 	@Basic
 	public String getMotherName() {
 		return motherName;
 	}
 
 	public void setMotherName(String mothersName) {
 		this.motherName = mothersName;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((birthdate == null) ? 0 : birthdate.hashCode());
 		result = prime * result + ((dni == null) ? 0 : dni.hashCode());
 		result = prime * result
 				+ ((fatherName == null) ? 0 : fatherName.hashCode());
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result
 				+ ((motherName == null) ? 0 : motherName.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((sex == null) ? 0 : sex.hashCode());
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
 		Patient other = (Patient) obj;
 		if (birthdate == null) {
 			if (other.birthdate != null)
 				return false;
 		} else if (!birthdate.equals(other.birthdate))
 			return false;
 		if (dni == null) {
 			if (other.dni != null)
 				return false;
 		} else if (!dni.equals(other.dni))
 			return false;
 		if (fatherName == null) {
 			if (other.fatherName != null)
 				return false;
 		} else if (!fatherName.equals(other.fatherName))
 			return false;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (motherName == null) {
 			if (other.motherName != null)
 				return false;
 		} else if (!motherName.equals(other.motherName))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (sex != other.sex)
 			return false;
 		return true;
 	}
 
 }
