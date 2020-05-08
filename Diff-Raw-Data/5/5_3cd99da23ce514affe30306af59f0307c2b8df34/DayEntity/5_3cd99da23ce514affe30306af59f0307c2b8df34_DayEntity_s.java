 package com.zotyo.diary.pojos;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.UniqueConstraint;
 import javax.persistence.Column;
 import javax.persistence.Lob;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 
 @Entity
 @Table(name = "days", uniqueConstraints={@UniqueConstraint(columnNames={"the_day"})})
 @NamedQueries({
     @NamedQuery(name = "DayEntity.findByTheDay", query = "SELECT d FROM DayEntity d WHERE d.theDay = :theDay"),
     @NamedQuery(name = "DayEntity.findByMonth", query = "SELECT d FROM DayEntity d WHERE d.theDay >= :startDay and d.theDay <= :endDay order by d.theDay desc")
     })
 public class DayEntity {
 	
 	@Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id")
     private Integer id;
 	
 	
 	// It should have been just a date, TemporalType.DATE :-(
 	@Column(name = "the_day")
 	@Temporal(TemporalType.TIMESTAMP) 
 	private Date theDay;
 	
     @OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.PERSIST, mappedBy = "theDay")
 	private List<EventEntity> eventsOfTheDay = new ArrayList<EventEntity>();
 	
 	@Lob
    @Column(name = "text")
 	private String descriptionOfTheDay;
 	
 	public DayEntity() { }
 	public DayEntity(Date theDay, EventEntity initialEvent, String descriptionOfTheDay) {
 		this();
 		setTheDay(theDay);
 		addEvent(initialEvent);
 		setDescriptionOfTheDay(descriptionOfTheDay);
 	}
 	
 	public void addEvent(EventEntity event) {
 		eventsOfTheDay.add(event);
 	}
 	
 	public Integer getId() {
 		return id;
 	}
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	public Date getTheDay() {
 		return theDay;
 	}
 	public void setTheDay(Date theDay) {
 		this.theDay = theDay;
 	}
 	public List<EventEntity> getEventsOfTheDay() {
 		return eventsOfTheDay;
 	}
 	public void setEventsOfTheDay(List<EventEntity> eventsOfTheDay) {
 		this.eventsOfTheDay = eventsOfTheDay;
 	}
 	public String getDescriptionOfTheDay() {
 		return descriptionOfTheDay;
 	}
 	public void setDescriptionOfTheDay(String descriptionOfTheDay) {
 		this.descriptionOfTheDay = descriptionOfTheDay;
 	}
 	
 }
