 package ca.usask.gmcte.currimap.model;
 
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import static javax.persistence.GenerationType.IDENTITY;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import org.hibernate.validator.Length;
 import org.hibernate.validator.NotNull;
 
 @SuppressWarnings("serial")
 @Entity
 @Table(name = "question")
 public class Question implements java.io.Serializable {
 
 	private Integer id;
 	private AnswerSet answerSet;
 	private String display;
 	private QuestionType questionType;
 
 	public Question() {
 	}
 
 	@Id
 	@GeneratedValue(strategy = IDENTITY)
 	@Column(name = "id", unique = true, nullable = false)
 	public Integer getId() {
 		return this.id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinColumn(name = "answer_set_id", nullable = true)
 	public AnswerSet getAnswerSet() {
 		return this.answerSet;
 	}
 
 	public void setAnswerSet(AnswerSet answerSet) {
 		this.answerSet = answerSet;
 	}
 
	@Column(name = "display", nullable = false, length = 100)
 	@NotNull
	@Length(max = 100)
 	public String getDisplay() {
 		return this.display;
 	}
 
 	public void setDisplay(String display) {
 		this.display = display;
 	}
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinColumn(name = "question_type_id")
 	public QuestionType getQuestionType() {
 		return this.questionType;
 	}
 
 	public void setQuestionType(QuestionType questionType) {
 		this.questionType = questionType;
 	}
 
 
 
 }
