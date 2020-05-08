 package com.zotyo.diary.pojos;
 
 import java.util.Date;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.JoinColumn;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.apache.solr.analysis.LowerCaseFilterFactory;
 import org.apache.solr.analysis.SnowballPorterFilterFactory;
 import org.apache.solr.analysis.StandardTokenizerFactory;
 import org.apache.solr.analysis.ASCIIFoldingFilterFactory;
 import org.hibernate.annotations.Type;
 import org.hibernate.search.annotations.Analyzer;
 import org.hibernate.search.annotations.DocumentId;
 import org.hibernate.search.annotations.Field;
 import org.hibernate.search.annotations.Index;
 import org.hibernate.search.annotations.Indexed;
 import org.hibernate.search.annotations.Store;
 import org.hibernate.search.annotations.AnalyzerDef;
 import org.hibernate.search.annotations.TokenizerDef;
 import org.hibernate.search.annotations.TokenFilterDef;
 import org.hibernate.search.annotations.Parameter;
 
 @Entity
 @Table(name = "events")
 @NamedQueries({
     @NamedQuery(name = "EventEntity.searchByTerm", query = "SELECT e FROM EventEntity e WHERE e.description LIKE :searchTerm order by e.startTime desc")
     })
 @Indexed
 @AnalyzerDef(name = "customanalyzer",
 	tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
 	filters = {
 		@TokenFilterDef(factory = LowerCaseFilterFactory.class),
		@TokenFilterDef(factory = ASCIIFoldingFilterFactory.class),
 		@TokenFilterDef(factory = SnowballPorterFilterFactory.class, params = {
 			@Parameter(name = "language", value = "Hungarian")
		})
 	})
 public class EventEntity {
 	
 	@Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id")
     @DocumentId
     private Integer id;
 
 	@Lob
     @Column(name = "text")
     @Field(index=Index.TOKENIZED, store=Store.NO)
     @Analyzer(definition = "customanalyzer")
     @Type(type="org.hibernate.type.StringClobType")
 	private String description;
 	
 	@Column(name = "startTime")
 	@Temporal(TemporalType.TIMESTAMP) 
 	private Date startTime;
 	
 	@Column(name = "duration")
 	private long duration;
 	
     @ManyToOne(fetch=FetchType.LAZY)
     @JoinColumn(name="day_id", updatable=false)
     private DayEntity theDay;
     
 	public EventEntity() {}
 	public EventEntity(String description, Date startTime, long duration) {
 		setDescription(description);
 		setStartTime(startTime);
 		setDuration(duration);
 	}
 	
 	public Integer getId() {
 		return id;
 	}
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	public Date getStartTime() {
 		return startTime;
 	}
 	public void setStartTime(Date startTime) {
 		this.startTime = startTime;
 	}
 	public long getDuration() {
 		return duration;
 	}
 	public void setDuration(long duration) {
 		this.duration = duration;
 	}
 
 	public DayEntity getTheDay() {
 		return theDay;
 	}
 	public void setTheDay(DayEntity theDay) {
 		this.theDay = theDay;
 	}
 	
 }
 
