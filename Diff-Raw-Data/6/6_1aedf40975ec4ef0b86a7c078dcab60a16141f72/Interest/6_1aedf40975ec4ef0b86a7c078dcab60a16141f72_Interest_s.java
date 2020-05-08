 package com.bugsprod.geektic;
 
 import java.io.Serializable;
 import java.util.Set;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.SequenceGenerator;
 
 public class Interest implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
 	@Id
 	@SequenceGenerator(name="interest_generator", sequenceName="interest_seq", allocationSize=1)
 	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "interest_generator")
 	private long id;
 	private String interest;
 	
	@ManyToMany(mappedBy = "interest")
 	private Set<Geek> geeks;
 
 	public Interest() {
 		super();
 	}
 
 	public String getInterest() {
 		return interest;
 	}
 
 	public void setInterest(String interest) {
 	}
 	
 }
