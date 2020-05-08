 package fr.jvallin.model;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.SequenceGenerator;
 
 
 /**
  * Entity implementation class for Entity: Geek
  */
 @Entity
 public class Geek {
 
 	@Id
	@SequenceGenerator(name = "geek_generator", sequenceName = "GEEK_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "geek_generator")
 	private Long id;
 	
 	private String pseudo;
 	
 	private String nom;
 	
 	private String prenom;
 	
 	private String sexe;
 	
 	private int nbvus;
 	
 	public Geek() {
 	}
 	
 	public Geek(String pseudo, String nom, String sexe) {
 		this.pseudo = pseudo;
 		this.nom = nom;
 		this.sexe = sexe;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getPseudo() {
 		return pseudo;
 	}
 
 	public void setPseudo(String pseudo) {
 		this.pseudo = pseudo;
 	}
 
 	public String getNom() {
 		return nom;
 	}
 
 	public void setNom(String nom) {
 		this.nom = nom;
 	}
 	
 	public String getPrenom() {
 		return prenom;
 	}
 
 	public void setPrenom(String prenom) {
 		this.prenom = prenom;
 	}
 
 	public String getSexe() {
 		return sexe;
 	}
 
 	public void setSexe(String sexe) {
 		this.sexe = sexe;
 	}
 	
 	public int getNbvus() {
 		return nbvus;
 	}
 
 	public void setNbvus(int nbvus) {
 		this.nbvus = nbvus;
 	}
 }
