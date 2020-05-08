 package br.com.findplaces.jpa.entity;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 
 @Entity
 @Table(name="TB_COMENT")
 public class Coment extends BaseEntity implements Serializable {
 
 	private static final long serialVersionUID = -3227586023644898530L;
 	
 	@Column
 	private Long id;
 	
 	@OneToOne
 	private User user;
 	
 	@OneToOne
 	private Coment answer;
 	
 	@ManyToOne
 	private Place place;
 	
 	@Column
 	private String text;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public Coment getAnswer() {
 		return answer;
 	}
 
 	public void setAnswer(Coment answer) {
 		this.answer = answer;
 	}
 
 	public Place getPlace() {
 		return place;
 	}
 
 	public void setPlace(Place place) {
 		this.place = place;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 
 }
