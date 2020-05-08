 package quizsite.models;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import quizsite.models.questions.*;
 import quizsite.util.DatabaseConnection;
 import quizsite.util.PersistentModel;
 
 public abstract class Question extends PersistentModel {
 	
 	protected Set<String> answers;
 	protected String text;
 	protected int quizId;
 	private String type;
 	
 	public enum Type{
 		CHECKBOX("checkbox"), 
 		FILL_BLANK("fill_blank"), 
 		PICTURE("picture"), 
 		RADIO("radio"), 
 		RESPONSE("response");
 		
 		private final String repr;
 		Type(String repr) {
 			this.repr = repr;
 		}
 		
 		@Override
 		public String toString() {
 			return this.repr;
 		}
 		
 		public boolean equals(String repr)
 		{
 			return repr.equals(this.repr);
 		}
 		
 		public static Question instantiate(List<String> row) throws SQLException
 		{
 			if (row == null)
 				return null;
 			
 			String type = row.get(I_TYPE);
 			Question questionObj;
 			if (Type.CHECKBOX.equals(type))
 				questionObj = new CheckboxQuestion();
 			else if (Type.FILL_BLANK.equals(type))
 				questionObj = new FillBlankQuestion();
 			else if (Type.PICTURE.equals(type))
 				questionObj = new PictureQuestion();
 			else if (Type.RESPONSE.equals(type))
 				questionObj = new ResponseQuestion();
 			else if (Type.RADIO.equals(type))
 				questionObj = new RadioQuestion();
 			else throw new IllegalArgumentException("This type doesn't exist : " + type);
 			
 			questionObj.parse(row);	// Pops down to the right parse function
 			return questionObj;
 		}
 	};
 	
 	public static String TABLE_NAME = "Question";
 	public static String[][] SCHEMA = { {"quiz_id", "INTEGER"}, {"body", "TEXT"}, {"type", "TINYTEXT"}, {"answers", "TEXT"}, {"auxiliary", "TEXT"}};
 	public static String[][] FOREIGN_KEYS = { {"quiz_id", "Quiz", "id"} };
 //	public static String[] INDEX = {"type"}; /** Which columns should be indexed for faster search? */
 	protected final static int I_BODY = 3, I_QUIZ_ID = 2, I_ANSWERS = 5, I_TYPE = 4, I_AUXILIARY = 6;
 	
 	@Override
 	public Object[] getFields() {
 		Object[] fields = new Object[] {quizId, text, getType(), serializeAnswers(), getAuxiliary()};
 		return fields;
 	}
 	
 	public Question(String text, Set<String> answers, int quiz_id) throws SQLException
 	{
 		super(TABLE_NAME, SCHEMA, FOREIGN_KEYS);
 		this.text 	 = text;
 		this.answers = answers;
 		this.quizId  = quiz_id;
 	}
 	
 	protected Question() throws SQLException { super(TABLE_NAME, SCHEMA, FOREIGN_KEYS); }
 
 	
 	public static List<Question> index() throws SQLException {
 		List<List<String> > allRows = DatabaseConnection.index(TABLE_NAME);
 		List<Question> questions = parseRows(allRows);
 		
 		return questions;
 	}
 
 	private static List<Question> parseRows(List<List<String>> allRows) throws SQLException {
 		List<Question> questions = new ArrayList<Question>();
 		
 		for (List<String> row : allRows) {
 			Question curr = Type.instantiate(row);
 			questions.add(curr);
 		}
 		
 		return questions;
 	}
 	
 	public static List<Question> indexByQuizID(int quiz_id) throws SQLException {
 		String[][] conditions = { {"quiz_id", "=", Integer.toString(quiz_id)} };
 		List<List<String> > rows = DatabaseConnection.indexWhere(TABLE_NAME, conditions);
 		return parseRows(rows);
 	}
 	
 	/**
 	 * Given user's answers as a set of strings, the total score will be an intersection of two sets   
 	 * @param userAnswers Set of user answers
 	 * @return number of matched answers
 	 */
 	public int getScore(Set<String> userAnswers) {
 		int score = 0;
 		for (Iterator<String> itr = userAnswers.iterator(); itr.hasNext();) {
 			String answ = (String) itr.next();
 			if (answers.contains(answ))
 				score++;
 		}
 		return score;
 	}
 	
 	public String getText()
 	{ return text; }
 	
 	public void setText(String newText)
 	{ text = newText; }
 
 
 	public static Question get(int id) throws SQLException {
 		List<String> entry = DatabaseConnection.get(TABLE_NAME, id);
 		Question obj = Type.instantiate(entry);
 		return obj;
 	}
 
 	@Override
 	public void parse(List<String> dbEntry) throws IllegalArgumentException, SQLException {
 		super.parse(dbEntry);
 		
 		setText(dbEntry.get(I_BODY));
 		setQuizID(Integer.parseInt(dbEntry.get(I_QUIZ_ID)));
 		setType(dbEntry.get(I_TYPE));
 		setAnswers(unserializeAnswers(dbEntry.get(I_ANSWERS)));
 		
 	}
 	
 	/**
 	 * Returns a serialized string of an extra info that a particular type of a question might need
 	 */
 	protected abstract String getAuxiliary();
 
 	/**
 	 * @param type the type to set
 	 */
 	public void setType(String type) {
 		this.type = type;
 	}
 	
 	public void setType(Type type) {
 		setType(type.toString());
 	}
 
 	/**
 	 * @return the type
 	 */
 	public String getType() {
 		return type;
 	}
 	
 	public void setQuizID(int newID)
 	{ quizId = newID; }
 	
 	public int getQuizId() 
 	{ return quizId; }
 	
 	public void setAnswers(Set<String> newAnsw)
 	{ answers = newAnsw; }
 	
	public static String serializeAnswers(Set<String> answers) {
 		String serialized = "";
 		for (Iterator<String> iterator = answers.iterator(); iterator.hasNext();) {
 			String answer = (String) iterator.next();
 			serialized += answer.trim() + "{!~!}"; // have to come up with a good delimiter, this one looks good...
 		}
 		return serialized.substring(0, serialized.length() - 5);
 	}
 	
	public static Set<String> unserializeAnswers(String answ)
 	{
 		String[] unsArr = answ.trim().split("{!~!}");
 		Set<String> answersSet = new HashSet<String>(Arrays.asList(unsArr));
 		return answersSet;
 	}
 }
