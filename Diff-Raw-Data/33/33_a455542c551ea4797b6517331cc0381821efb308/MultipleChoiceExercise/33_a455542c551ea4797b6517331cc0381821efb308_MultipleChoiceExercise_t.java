 package model;
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import utils.DateGC;
 
 /**
  * 
  * @author Steve
  * @version 25/10/2013
  *
  */
 
 public class MultipleChoiceExercise extends Exercise implements Validatable {
         
        private String multipleChoice;
         
 
         
         //Constructor with multiple parameters
 
     
         //default constructor
         
         public MultipleChoiceExercise()throws IllegalArgumentException{
         	
         }
         
         //Constructor with multiple parameters
 
         public MultipleChoiceExercise(int excerciseId, String question, String correctAnswer, 
     			String[] answerHints, int maxNumberOfAttempts, int maxAnswerTime,
     			   ExerciseCategory category, Teacher author, List<QuizExercise> quizExercises, 
     			   DateGC dateRegistration, char discriminator, String multipleChoice) 
      				   throws IllegalArgumentException {
         	super(excerciseId, question, correctAnswer, answerHints, maxNumberOfAttempts,
       			  maxAnswerTime, category, author, quizExercises, dateRegistration, discriminator);
         	
         	this.multipleChoice = multipleChoice;
         }   
         
         //getters & setters
 
 
         public void setMultipleChoice(String multipleChoice) {
                 this.multipleChoice = multipleChoice;
         }
         
         public String getMultipleChoice() {
                 return multipleChoice;
         }
         
         //methods
 
         public Map<Integer, String>  getListMultipleChoice(){
                 String[] fields = this.multipleChoice.split(";");
                 
                 Map <Integer, String> list = new HashMap <Integer, String>();
                 
                 int i = 0;
                 
                 for (String multipleChoice : fields) {
                         i++;
                         list.put(i, multipleChoice);
                 }
                 
                 return list;
         }
         
         /**
         public HashMap<String, ArrayList<String>> getListMultipleChoice2(){
         	HashMap<String,ArrayList<String>> myHashMap = new HashMap<String,ArrayList<String>>();
             ArrayList<String> correctAnswer = new ArrayList<String>(); // will keeps the correct answer of each question
             Scanner scan = new Scanner();
 			while(scan.hasNext()){
                 String question = scan.nextLine();
                 String line = scan.nextLine();
                 ArrayList<String> answers = new ArrayList<String>();
                 while(line.length()!=0){ // if length == 0 that means we found space!
                     answers.add(line);
                     line = scan.nextLine();
                 }
                 myHashMap.put(question,answers);  // we put all the question with the answers in HashMap
             }
         }
         */
         
         @Override
     	public void setCorrectAnswer(String correctAnswer) throws IllegalArgumentException{
     		if (correctAnswer == null){
     			throw new IllegalArgumentException("Juiste antwoord is null!");
     		}
     		if (correctAnswer.isEmpty()){
     			throw new IllegalArgumentException("Gelieve een antwoord in te vullen!");
     		}
     		
     		correctAnswer = getCorrectAnswer();
     	}
     	
     	
     	
     	@Override
     	public boolean isCorrectAnswer(String answer) throws IllegalArgumentException{
     		
     		if(answer == null){
     			throw new IllegalArgumentException("Geen antwoord gegeven!");
     		}
     			
     		if(answer.equals(getCorrectAnswer())){
     			return true;
     		}
     			
     		return false;
     	}
     	
     	@Override
     	public String getValidateText() {
     		
     		return "Kies het correcte antwoord.";
     	}
     	
     	
     	@Override
     	public String toString() {
     		return "Exercise [getExerciseId()=" + getExerciseId()
     				+ ", getQuestion()=" + getQuestion() 
     				+ ", getCorrectAnswer()=" + getCorrectAnswer() 
     				+ ", getAnswerHints()=" + Arrays.toString(getAnswerHints())
     				+ ", getMaxNumberOfAttempts()=" + getMaxNumberOfAttempts()
     				+ ", getMaxAnswerTime()=" + getMaxAnswerTime()
     				+ ", getCategory()=" + getCategory() 
     				+ ", getAuthor()=" + getAuthor() 
     				+ ", getQuizExercises()=" + getQuizExercises()
     				+ ", getDateRegistration()=" + getDateRegistration()
     				+ ", getDiscriminator()=" + getDiscriminator()
     				+ ", hashCode()=" + hashCode() 
     				+ ", MultipleChoice()=" + getMultipleChoice()
     				+ "]";
     	}
     	
     	@Override
         public boolean isValide(String answer) {
                 StringTokenizer given = new StringTokenizer(answer, ";");
                 StringTokenizer original = new StringTokenizer(this.getCorrectAnswer(), ";");
                 if (given.countTokens() == original.countTokens()){
                         return true;
                 }else{
                         getValidateText();
                         return false;
                 }
         }
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = super.hashCode();
 			result = prime
 					* result
 					+ ((multipleChoice == null) ? 0 : multipleChoice.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (!super.equals(obj))
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			MultipleChoiceExercise other = (MultipleChoiceExercise) obj;
 			if (multipleChoice == null) {
 				if (other.multipleChoice != null)
 					return false;
 			} else if (!multipleChoice.equals(other.multipleChoice))
 				return false;
 			return true;
 		}
 		
 		@Override
 		public MultipleChoiceExercise clone() throws CloneNotSupportedException{
 			GregorianCalendar gc = new GregorianCalendar();
 			gc.set(getDateRegistration().getGregCal().get(Calendar.YEAR), 
 					getDateRegistration().getGregCal().get(Calendar.MONTH), 
 					getDateRegistration().getGregCal().get(Calendar.DATE));
 			
 			DateGC date = new DateGC();
 			date.setGregCal(gc);
 			
 			MultipleChoiceExercise exercise = new MultipleChoiceExercise(getExerciseId(), getQuestion(), getCorrectAnswer(), 
 					getAnswerHints(), getMaxNumberOfAttempts(), getMaxAnswerTime(), getCategory(), 
 					getAuthor(), getQuizExercises(), date, getDiscriminator(), getMultipleChoice());
 			
 			return exercise;
 		}
 
 }
