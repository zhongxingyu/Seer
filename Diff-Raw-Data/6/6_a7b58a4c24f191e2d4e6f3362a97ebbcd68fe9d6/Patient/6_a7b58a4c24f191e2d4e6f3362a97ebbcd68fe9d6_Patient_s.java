 package com.tda.model.patient;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.validation.constraints.Digits;
 import javax.validation.constraints.Size;
 
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Past;

 @Entity
 public class Patient implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private Long id;
 
 	@Size(min = 0, max = 10)
 	@Digits(fraction = 0, integer = 10)
 	private String dni;
 
 	@NotNull
 	@Size(min = 1, max = 100)
 	private String firstName;
 
 	@NotNull
 	@Size(min = 1, max = 100)
 	private String lastName;
 
 	@NotNull
 	private Sex sex;
 
 	@NotNull
 	@Past
 	private Date birthdate;
 
 	public Patient() {
 		super();
 	}
 
 	public Patient(String dni, String firstName, String lastName, Sex sex,
 			Date birthdate) {
 		this.dni = dni;
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.sex = sex;
 		this.birthdate = birthdate;
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
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	@Enumerated
 	public Sex getSex() {
 		return sex;
 	}
 
 	public void setSex(Sex sex) {
 		this.sex = sex;
 	}
 
 	public Date getBirthdate() {
 		return birthdate;
 	}
 
 	public void setBirthdate(Date birthdate) {
 		this.birthdate = birthdate;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((birthdate == null) ? 0 : birthdate.hashCode());
 		result = prime * result + ((dni == null) ? 0 : dni.hashCode());
 		result = prime * result
 				+ ((firstName == null) ? 0 : firstName.hashCode());
 		result = prime * result
 				+ ((lastName == null) ? 0 : lastName.hashCode());
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
 		if (firstName == null) {
 			if (other.firstName != null)
 				return false;
 		} else if (!firstName.equals(other.firstName))
 			return false;
 		if (lastName == null) {
 			if (other.lastName != null)
 				return false;
 		} else if (!lastName.equals(other.lastName))
 			return false;
 		if (sex != other.sex)
 			return false;
 		return true;
 	}
 
 }
