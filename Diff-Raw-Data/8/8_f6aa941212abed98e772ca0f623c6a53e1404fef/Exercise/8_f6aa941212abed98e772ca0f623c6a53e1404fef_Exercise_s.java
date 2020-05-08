 /**
  * 
  */
 package model;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import utils.DateGC;
 
 /**
  * 
  * @author Emin
  * @author Lieven
  * @version 16/10/2013
  *
  */
 public abstract class Exercise implements Comparable<Exercise>{
 	
 	public enum ExerciseCategory {
 		AARDRIJKSKUNDE ("Aardrijkskunde"),
 		NEDERLANDS ("Nederlands"),
 		WETENSCHAPPEN ("Wetenschappen"),
 		WISKUNDE ("Wiskunde"); 
 		
 		private final String name;
 
 		ExerciseCategory(final String name) {
 		      this.name = name;
 		}
 
 		@Override
 		public String toString() {
 		return name;
	  }
 	}
 	
 	private String question;
 	private String correctAnswer;
 	private String[] answerHints;
 	private int maxNumberOfAttempts;
 	private int maxAnswerTime;
 	private ExerciseCategory category;
 	private Teacher author;
 	private List<Quiz> quizzes;
 	private DateGC dateRegistration;
 	
 	// Constructors
 	
 	/**
 	 * Default constructor
 	 */
 	public Exercise() throws IllegalArgumentException{
 		this.setQuestion("leeg");
 		this.setCorrectAnswer("leeg");
 		this.setAnswerHints(new String[]{});
 		this.setMaxNumberOfAttempts(1);
 		this.setMaxAnswerTime(0);
 		this.setCategory(Exercise.ExerciseCategory.AARDRIJKSKUNDE);
 		this.setAuthor(Teacher.BAKKER);
 		this.setQuizzes(new ArrayList<Quiz>());
 		this.setDateRegistration(new DateGC());
 	}
 	
 	/**
 	 * Constructor with 9 params
 	 * 
 	 * @param question
 	 * @param correctAnswer
 	 * @param answerHints
 	 * @param maxNumberOfAttempts
 	 * @param maxAnswerTime
 	 */
 	public Exercise(String question,String correctAnswer,String[] answerHints,
 			int maxNumberOfAttempts,int maxAnswerTime,ExerciseCategory category,
 			Teacher author,List<Quiz> quizzes,DateGC dateRegistration) throws IllegalArgumentException{
 		this.setQuestion(question);
 		this.setCorrectAnswer(correctAnswer);
 		this.setAnswerHints(answerHints);
 		this.setMaxNumberOfAttempts(maxNumberOfAttempts);
 		this.setMaxAnswerTime(maxAnswerTime);
 		this.setCategory(category);
 		this.setAuthor(author);
 		this.setQuizzes(quizzes);
 		this.setDateRegistration(dateRegistration);
 	}
 
 	// Selectors
 	
 	/**
 	 * @return question
 	 */
 	public String getQuestion() {
 		return question;
 	}
 	
 	/**
 	 * @return correctAnswer
 	 */
 	public String getCorrectAnswer() {
 		return correctAnswer;
 	}
 
 	/**
 	 * @return answerHints
 	 */
 	public String[] getAnswerHints() {
 		return answerHints;
 	}
 
 	/**
 	 * @return maxNumberOfAttempts
 	 */
 	public int getMaxNumberOfAttempts() {
 		return maxNumberOfAttempts;
 	}
 
 	/**
 	 * @return maxAnswerTime
 	 */
 	public int getMaxAnswerTime() {
 		return maxAnswerTime;
 	}
 
 	/**
 	 * @return
 	 */
 	public ExerciseCategory getCategory() {
 		return category;
 	}
 
 	/**
 	 * @return
 	 */
 	public Teacher getAuthor() {
 		return author;
 	}
 
 	/**
 	 * @return
 	 */
 	public List<Quiz> getQuizzes() {
 		return quizzes;
 	}
 
 	/**
 	 * @return
 	 */
 	public DateGC getDateRegistration() {
 		return dateRegistration;
 	}
 
 	// Modifiers
 	
 	/**
 	 * Set question
 	 * 
 	 * @param question
 	 */
 	public void setQuestion(String question) throws IllegalArgumentException{
 		if (question == null)throw new IllegalArgumentException("Vraag is null!");
 		if (question.isEmpty())throw new IllegalArgumentException("Gelieve een vraag in te vullen!");
 		this.question = question;
 	}
 	
 	/**
 	 * Set correctAnswer
 	 * 
 	 * @param correctAnswer
 	 */
 	public void setCorrectAnswer(String correctAnswer) throws IllegalArgumentException{
 		if (correctAnswer == null)throw new IllegalArgumentException("Juiste antwoord is null!");
 		if (correctAnswer.isEmpty())throw new IllegalArgumentException("Gelieve een antwoord in te vullen!");
 		this.correctAnswer = correctAnswer;
 	}
 
 	/**
 	 * Set answerHits
 	 * 
 	 * @param answerHints
 	 */
 	public void setAnswerHints(String[] answerHints) throws IllegalArgumentException{
 		if (answerHints == null)throw new IllegalArgumentException("Antwoord hints verzameling is null");
 		this.answerHints = answerHints;
 	}
 
 	/**
 	 * Set maxNumberOfAttempts
 	 * 
 	 * @param maxNumberOfAttempts
 	 */
 	public void setMaxNumberOfAttempts(int maxNumberOfAttempts) throws IllegalArgumentException{
 		if (maxNumberOfAttempts <= 0)throw new IllegalArgumentException("Aantal pogingen kunnen niet 0 of negatief zijn!");
 		this.maxNumberOfAttempts = maxNumberOfAttempts;
 	}
 
 	/**
 	 * Set maxAnswerTime
 	 * 
 	 * @param maxAnswerTime
 	 */
 	public void setMaxAnswerTime(int maxAnswerTime) throws IllegalArgumentException{
 		if (maxAnswerTime < 0)throw new IllegalArgumentException("Max antwoord tijd kan niet 0 of negatief zijn!");
 		this.maxAnswerTime = maxAnswerTime;
 	}
 
 	/**
 	 * Set category
 	 * 
 	 * @param category
 	 */
 	public void setCategory(ExerciseCategory category) throws IllegalArgumentException{
 		if (category == null)throw new IllegalArgumentException("Categorie is null!");
 		this.category= category;
 	}
 
 	/**
 	 * Set author
 	 * 
 	 * @param auteur
 	 */
 	public void setAuthor(Teacher author) throws IllegalArgumentException{
 		if (author == null)throw new IllegalArgumentException("Auteur is null!");
 		this.author = author;
 	}
 
 	/**
 	 * Set quizzes
 	 * 
 	 * @param quizzes
 	 */
 	public void setQuizzes(List<Quiz> quizzes) throws IllegalArgumentException{
 		if (quizzes == null)throw new IllegalArgumentException("Quizzen verzameling is null!");
 		this.quizzes = quizzes;
 	}
 
 	/**
 	 * Set dateRegistration
 	 * 
 	 * @param dateRegistration
 	 */
 	public void setDateRegistration(DateGC dateRegistration) throws IllegalArgumentException{
 		if (dateRegistration == null)throw new IllegalArgumentException("Datum is null");
 		this.dateRegistration = dateRegistration;
 	}
 	
 	// Comparisons
 	
 	/**
 	 * Compares input answer with correct answer
 	 * 
 	 * @param answer
 	 * @return
 	 */
 	public boolean isCorrectAnswer(String answer){
 		if (this.getCorrectAnswer() == answer)
 			return true;
 		return false;
 	}
 	
 	/**
 	 * Comparable
 	 * 
 	 * @param opdracht
 	 * @return
 	 */
 	public int compareTo(Exercise exercise){
 		return this.getQuestion().compareTo(exercise.getQuestion());
 	}
 	
 	//Overrides
 	
 	@Override
 	public String toString() {
 		return "Opdracht [question=" + getQuestion() + ", correctAnswer="
 				+ getCorrectAnswer() + ", answerHints="
 				+ Arrays.toString(getAnswerHints()) + ", maxNumberOfAttempts="
 				+ getMaxNumberOfAttempts() + ", maxAnswerTime=" + getMaxAnswerTime()
 				+ ", category=" + getCategory() + ", author=" + getAuthor()
 				+ ", quizzen=" + getQuizzes() + ", dateRegistration="
 				+ getDateRegistration() + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + Arrays.hashCode(getAnswerHints());
 		result = prime * result + ((getAuthor() == null) ? 0 : getAuthor().hashCode());
 		result = prime * result
 				+ ((getCategory() == null) ? 0 : getCategory().hashCode());
 		result = prime * result
 				+ ((getCorrectAnswer() == null) ? 0 : getCorrectAnswer().hashCode());
 		result = prime * result + getMaxAnswerTime();
 		result = prime * result + getMaxNumberOfAttempts();
 		result = prime * result
 				+ ((getQuestion() == null) ? 0 : getQuestion().hashCode());
 		result = prime * result + ((getQuizzes() == null) ? 0 : getQuizzes().hashCode());
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
 		Exercise other = (Exercise) obj;
 		if (!Arrays.equals(getAnswerHints(), other.getAnswerHints()))
 			return false;
 		if (getAuthor() != other.getAuthor())
 			return false;
 		if (getCategory() != other.getCategory())
 			return false;
 		if (getCorrectAnswer() == null) {
 			if (other.getCorrectAnswer() != null)
 				return false;
 		} else if (!getCorrectAnswer().equals(other.getCorrectAnswer()))
 			return false;
 		if (getMaxAnswerTime() != other.getMaxAnswerTime())
 			return false;
 		if (getMaxNumberOfAttempts() != other.getMaxNumberOfAttempts())
 			return false;
 		if (getQuestion() == null) {
 			if (other.getQuestion() != null)
 				return false;
 		} else if (!getQuestion().equals(other.getQuestion()))
 			return false;
 		if (getQuizzes() == null) {
 			if (other.getQuizzes() != null)
 				return false;
 		} else if (!getQuizzes().equals(other.getQuizzes()))
 			return false;
 		return true;
 	}
 	
 	public static void main(String[] args) {
 		Exercise ex = new SimpleExercise();
 		
 		System.out.println(ex.toString());
 	}
 }
