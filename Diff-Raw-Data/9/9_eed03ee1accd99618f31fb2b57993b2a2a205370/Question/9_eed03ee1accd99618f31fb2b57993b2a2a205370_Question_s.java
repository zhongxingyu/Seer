 package com.piotrnowicki.exam.simulator.entity;
 
 import java.io.Serializable;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderColumn;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 @Entity
 @NamedQuery(name = Question.READ_ALL, query="SELECT q FROM Question q ORDER BY q.number")
 public class Question implements Serializable {
 
 	public final static String READ_ALL = "Question.ReadAll";
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue
 	private Long id;
 
 	@NotNull
 	@Size(min=1)
 	private String number;
 
 	@NotNull
 	@Size(min=1)
 	private String summary;
 
 	@NotNull
 	@Size(min=1)
 	@Column(length=16000)
 	private String content;
 
 	@Column(length=16000)
 	private String explanaition;
 
 	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
 	@OrderColumn(updatable=false)
 	private List<Answer> answers = new LinkedList<>();
 
 	public Question() {
 	}
 
 	public Question(String number, String summary, String content) {
 		this.number = number;
 		this.summary = summary;
 		this.content = content;
 	}
 
 	public String getNumber() {
 		return number;
 	}
 
 	public void setNumber(String number) {
 		this.number = number;
 	}
 
 	public String getSummary() {
 		return summary;
 	}
 
 	public void setSummary(String summary) {
 		this.summary = summary;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public List<Answer> getAnswers() {
 		return answers;
 	}
 
 	public void setAnswers(List<Answer> answers) {
 		this.answers = answers;
 	}
 
 	public String getExplanaition() {
 		return explanaition;
 	}
 
 	public void setExplanaition(String explanaition) {
 		this.explanaition = explanaition;
 	}
 
 	public void addAnswers(Answer... answers) {
 		for (Answer answer : answers) {
 			this.answers.add(answer);
 		}
 	}
 	
 	@Override
 	public int hashCode() {
 		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
 
 		hashCodeBuilder.append(id);
 		hashCodeBuilder.append(number);
 		hashCodeBuilder.append(summary);
 
 		return hashCodeBuilder.toHashCode();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 
 		if (obj == null) {
 			return false;
 		}
 
 		if (!(obj instanceof Question)) {
 			return false;
 		}
 
 		Question other = (Question) obj;
 		EqualsBuilder equalsBuilder = new EqualsBuilder();
 		
 		equalsBuilder.append(number, other.number);
 		equalsBuilder.append(summary, other.summary);
 	
 		return equalsBuilder.isEquals();
 	}
 	
 	@Override
 	public String toString() {
 		ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
 		builder.append("id", id);
 		builder.append("number", number);
 		builder.append("summary", summary);
 		return builder.toString();
 	}
 }
