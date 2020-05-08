 package data.bonus;
 
 import data.me.json.JSONArray;
 import data.me.json.JSONException;
 import data.me.json.JSONObject;
 
 /**
  * BonusQuestion is the class that will deal with the numerous bonus questions
  * that users can answer at their leisure during the competition.
  * 
  * @author Graem Littleton, Kevin Brightwell, Jonathan Demelo, Ramesh Raj,
  *         Justin McDonald
  * 
  */
 
 public class BonusQuestion {
 	
 	protected int bonusType;//0=short 1=mc
 
 	protected String prompt;
 	protected String answer;
 	protected String[] choices; // TODO: can we somehow implement short answer
 								// and mc together without this?
 	protected int week;
 
 	public static final String FILE_PATH = "res/data/bonus.dat";
 	protected static final String KEY_TYPE = "type";
 	protected static final String KEY_PROMPT = "prompt";
 	protected static final String KEY_ANSWER = "answer";
 	protected static final String KEY_CHOICES = "mc_choices";// TODO:need a
 																// better name
 																// for type
 	protected static final String KEY_WEEK = "week";
 
 	/**
 	 * Default constructor for Bonus Question
 	 * 
 	 * @param prompt
 	 *            The question(required)
 	 * @param answer
 	 *            The answer(required)
 	 * @param choices
 	 *            The possible choices(null from short answer, and actual values
 	 *            for MC)
 	 */
 	public BonusQuestion(String prompt, String answer, String[] choices,int week) {
 		this.prompt = prompt;
 		this.answer = answer;
 		this.choices = choices;
 		bonusType = choices == null ? 0 : 1;
 		this.week = week;
 		Bonus.addNewQuestion(this);
 		// TODO: do we need to check if answer is in choices?
 	}
 
 	/**
 	 * Only used for fromJsonObject
 	 */
 	public BonusQuestion() {
 	}
 
 	/**
 	 * Get the type of question.
 	 * 0=short, 1=mc
 	 * @return
 	 */
 	public int getBonusType() {
 		return bonusType;
 	}
 
 	/**
 	 * Set the bonus type
 	 * 0=short, 1=multi
 	 * @param bonusType
 	 */
 	public void setBonusType(int bonusType) {
 		this.bonusType = bonusType;
 	}
 
 	/**
 	 * Get the question prompt
 	 * 
 	 * @return String of prompt
 	 */
 	public String getPrompt() {
 		return prompt;
 	}
 
 	/**
 	 * Set the question prompt
 	 * 
 	 * @param prompt
 	 */
 	public void setPrompt(String prompt) {
 		this.prompt = prompt;
 	}
 
 	/**
 	 * Get the answer to the question
 	 * 
 	 * @return
 	 */
 	public String getAnswer() {
 		return answer;
 	}
 
 	/**
 	 * Set answer to question
 	 * 
 	 * @param answer
 	 */
 	public void setAnswer(String answer) {
 		this.answer = answer;
 	}
 
 	/**
 	 * Get possible choices or null if question is short answre
 	 * 
 	 * @return
 	 */
 	public String[] getChoices() {
 		return choices;
 	}
 
 	/**
 	 * Set choices. CHANGES TYPE TO MULTIPLE CHOICE!
 	 * 
 	 * @param choices
 	 */
 	public void setChoices(String[] choices) {
 		this.choices = choices;
		bonusType = 1;
 	}
 
 	/**
 	 * Get the week this question was asked
 	 * 
 	 * @return int
 	 */
 	public int getWeek() {
 		return week;
 	}
 
 	/**
 	 * Set the week this question was asked
 	 * 
 	 * @param week
 	 */
 	public void setWeek(int week) {
 		this.week = week;
 	}
 
 	/**
 	 * Converts Contestant object to a json object
 	 * 
 	 * @return a JSON object containing all the data needed
 	 * @throws JSONException
 	 */
 	public JSONObject toJSONObject() throws JSONException {
 		JSONObject obj = new JSONObject();
 		obj.put(KEY_PROMPT, prompt);
 		obj.put(KEY_ANSWER, answer);
 		
 		if (choices != null) {
 			JSONArray sChoice = new JSONArray();
 			for(int i =0;i<choices.length;i++){
 				String s = choices[i];
 				sChoice.put(s);
 			}
 		} else {
 			obj.put(KEY_CHOICES, null);
 		}
 
 		obj.put(KEY_WEEK, week);
 		return obj;
 	}
 
 	public void fromJSONObject(JSONObject o) throws JSONException {
 
 		setPrompt(o.getString(KEY_PROMPT));
 		setAnswer(o.getString(KEY_ANSWER));
 
 		JSONArray jChoices = o.getJSONArray(KEY_CHOICES);
		if (jChoices == null) {
 			setChoices(null);
 		} else {
 			String[] choice = new String[jChoices.length()];
 			for (int i = 0; i < jChoices.length(); i++) {
 				choice[i] = jChoices.getString(i);
 			}
 			setChoices(choice);
 		}
 		setWeek(o.getInt(KEY_WEEK));
 
 	}
 
 }
