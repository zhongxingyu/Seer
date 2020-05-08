 package model;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class MultipleChoice implements Question {
 
 	public static final int type=3;
 	private String statement;
 	private Set<String> wrongAnswers;
 	private Set<String> options;
 	private String answer;
 	private int qID;
 	private String userAnswer;
 	
 	public static String getHTMLInputString(){
 		StringBuilder html = new StringBuilder();
 		
 		html.append("<br />Insert Question Statement: <br /><textarea name=\"statement\" rows=\"8\" cols=\"48\" required></textarea>");
 		html.append("<br />Insert Answer:<br /> <input class=quizobject type=\"text\" name=\"answer1\" required />");
 		html.append("<br />Insert All Incorrect Options, one on each line:");
 		html.append("<br /><textarea name=\"wrongOptions\" cols=\"20\" rows=\"10\" required></textarea>");
 		
 		return html.toString();
 	}
 	public static String getRandomHTMLInputString(){
 		String[] strings = getOtherRandomHTML();
 		ArrayList<String> wrongHashSet =getRandomWrongAnswers();
 		StringBuilder html = new StringBuilder();
 		html.append("<br />Insert Question Statement: <br /><textarea name=\"statement\" rows=\"8\" >Please fill the blank:&#13;&#10;"+strings[1] +"</textarea>");
 		html.append("<br />Insert Answer:<br /> <input class=quizobject type=\"text\" name=\"answer1\" value=\" "+strings[0] +"\"/>");
 		html.append("<br />Insert Wrong Options:<br /><textarea name=\"wrongOptions\" rows=\"10\">");
 		for (String wrongOption : wrongHashSet){
 			html.append(wrongOption + "&#13;&#10;");
 		}
 		html.append("</textarea>");
 		
 		return html.toString();
 		
 	}
 
 	
 	public int getqID() {
 		return qID;
 	}
 
 	public void setqID(int qID) {
 		this.qID = qID;
 	}
 
 	public MultipleChoice(String question, HashSet<String> wrongAns, String ans) { // pushes to database
		if (question.length() < 250) this.statement = question;
		else this.statement = question.substring(0,245) +"...";
 		this.wrongAnswers = wrongAns;
 		wrongAnswers.remove("");
 		
 		this.answer = ans;
 		
 		options = new HashSet<String>();
 		for(String s : wrongAnswers) {
 			options.add(s);
 		}
 		options.add(answer);
 	}
 	
 	public MultipleChoice(Integer id, Connection con) throws SQLException {
 		this.qID = id;
 		
 		PreparedStatement ps = con.prepareStatement("select * from multiple_choice_question where question_id = ?");
 		ps.setInt(1, id);
 		ResultSet rs = ps.executeQuery();
 		
 		String wrong = new String();
 		while(rs.next()) {
 			statement = rs.getString("statement");
 			answer = rs.getString("answer");
 			wrong = rs.getString("wrong_answers");
 		}
 		
 		options = new HashSet<String>();
 		
 		String[] strings = wrong.split(Pattern.quote(" &&& "));
 		wrongAnswers = new HashSet<String>();
 		for (String string : strings) {
 			wrongAnswers.add(string);
 			options.add(string);
 		}
 		options.add(answer);	
 		
 	}
 
 	public String getStatement() {
 		return statement;
 	}
 
 	public void setStatement(String statement) {
 		this.statement = statement;
 	}
 
 	public Set<String> getWrongAnswers() {
 		return wrongAnswers;
 	}
 
 	public void setWrongAnswers(Set<String> wrongAnswers) {
 		this.wrongAnswers = wrongAnswers;
 	}
 	
 
 	@Override
 	public int solve(ArrayList<String> ans) {
 		if (ans.size()!=1) {
 			return 0; // input cleansing
 		}
 		for (String a : ans) {
 			
 			if (answer.equals(a)) {
 				return 1;
 			}
 		
 		}
 		return 0;
 	}
 
 	public String getAnswer() {
 		return answer;
 	}
 
 	public void setAnswer(String answer) {
 		this.answer = answer;
 	}
 
 	@Override
 	public String toHTMLString() {
 		StringBuilder html = new StringBuilder();
 		
         html.append(statement);
         html.append("<br />");
         
         for(String s : options) {
                 html.append("<input type=\"radio\" name=\"");
         		html.append(type);
         		html.append("_");
                 html.append(qID);
 
                 html.append("\" value=\"");
                 html.append(s + "\"> " + s);
                 html.append("<br />");       
         }
         
 		return html.toString();
 	}
 	
 	@Override
 	public String getCorrectAnswers() {
 		StringBuilder correctAnswers = new StringBuilder();
 		answer = answer.trim();
 		correctAnswers.append(answer);
 		
 		return correctAnswers.toString();
 	
 	}
 
 	@Override
 	public void generate(int id, Connection con) {
 		// TODO Auto-generated method stub
 		
 	}
 	public int getType(){
 		return type;
 	}
 
 	@Override
 	public void pushToDB(Connection con) {
 		Statement stmt;
 		
 		try {
 			stmt = con.createStatement();
 			StringBuilder sqlString = new StringBuilder("INSERT INTO multiple_choice_question VALUES(null,\"");
 			
 			statement = statement.trim();
 			sqlString.append(statement);
 			sqlString.append("\",\"");
 			
 			answer = answer.trim();
 			sqlString.append(answer);
 			sqlString.append("\",\"");
 			
 			for (String string : wrongAnswers) {
 				string = string.trim();
 				sqlString.append(string);
 				sqlString.append(" &&& ");
 			}
 			
 			sqlString.replace(sqlString.length()-5, sqlString.length(), "");
 			sqlString.append("\") ");
 			
 			System.out.print(sqlString.toString());
 			stmt.executeUpdate(sqlString.toString());
 			
 			stmt = con.createStatement();
 			sqlString = new StringBuilder("SELECT * FROM multiple_choice_question WHERE statement=\"");
 			sqlString.append(statement);
 			sqlString.append("\" ");
 			
 			System.out.print(sqlString.toString());
 			ResultSet resultSet = stmt.executeQuery(sqlString.toString());
 			
 			
 			while (resultSet.next()) {
 				this.setqID(resultSet.getInt("question_id")); // will always be the last one
 			}
 		}catch(Exception e){
 			
 		}		
 	}
 	
 	public static ArrayList<String> getRandomWrongAnswers(){
 		ArrayList<String> wrongAnswers = new ArrayList<String>();
 		for(int i =0; i <4; i++){
 			try{
 			wrongAnswers.add(getOtherRandomHTML()[0]);
 			}
 			catch(NullPointerException e){
 				e.printStackTrace();
 				wrongAnswers.add("Mening Pinguin");
 			}
 		}
 		return wrongAnswers;
 	}
 	
 	public static String[] getOtherRandomHTML(){
 		Document doc;
 		try {
 			doc = Jsoup.connect("http://en.wikipedia.org/wiki/Special:Random?printable=yes").get();
 			Elements paragraphs = doc.select("p");
 			Element firstParagraph = paragraphs.first();
 			Element respuesta = firstParagraph.select("b").first();
 			//System.out.println(firstParagraph.select("b"));
 			//System.out.println(firstParagraph.html());
 			//System.out.println(firstParagraph.child(0).text());
 			String regex = respuesta.text();
 			String parrafo = firstParagraph.text();
 			String pincheParrafo = parrafo.replace(regex,"__________");
			if(pincheParrafo.length() > 250){
				pincheParrafo = pincheParrafo.substring(0,249) + "...";
			}
 			String[] setup = {regex, pincheParrafo};
 			return setup;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch(NullPointerException e){
 			//watch out for reaaally long loops
 			return getOtherRandomHTML();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public void setUserAnswers(ArrayList<String> ans) {
 		for(String s : ans) {
 			userAnswer = s;
 		}
 	}
 	
 	@Override
 	public String getUserAnswers() {
 		return userAnswer;
 	}
 	
 	@Override
 	public int getTotalQScore() {
 		return 1;
 	}
 	
 	@Override
 	public int getNumAnswers() {
 		return 1;
 	}
 	
 	public String getEditWrongAnswersString() {
 		StringBuilder str = new StringBuilder();
 		
 		for(String s : wrongAnswers) {
 			str.append(s);
 			str.append("\n");
 		}
 		
 		str.replace(str.length()-1, str.length(), "");
 		
 		return str.toString();
 	}
 	
 	@Override
 	public String getEditQuizString() {
 		StringBuilder html = new StringBuilder();
 		
 		html.append("<br />Multiple Choice Question");
 		html.append("<br />Insert Question Statement: <br /><textarea name=\""+type+"_"+qID+"_statement\" rows=\"8\" cols=\"48\" required>"+statement+"</textarea>");
 		html.append("<br />Insert Answer:<br /> <input type=\"text\" name=\""+type+"_"+qID+"_answer\" required value=\""+answer+"\"/>");
 		html.append("<br />Insert All Incorrect Options, one on each line:");
 		html.append("<br /><textarea name=\""+type+"_"+qID+"_wrongOptions\" cols=\"20\" rows=\"10\" required>"+getEditWrongAnswersString()+"</textarea>");
 		
 		return html.toString();
 	}
 	@Override
 	public void updateDB(Connection con) throws SQLException {
 		PreparedStatement ps = con.prepareStatement("UPDATE multiple_choice_question SET statement = ?, answer = ?, wrong_answers = ? WHERE question_id = ?");
 		
 		ps.setString(1, statement);
 		ps.setString(2, answer);
 		
 		StringBuilder wrong = new StringBuilder();
 		for(String s : wrongAnswers) {
 			wrong.append(s);
 			wrong.append(" &&& ");
 		}
 		wrong.replace(wrong.length()-5, wrong.length(), "");
 		
 		ps.setString(3, wrong.toString());
 		ps.setInt(4, qID);
 		
 		ps.executeUpdate();
 	}
 	@Override
 	public void deleteFromDB(Connection con) throws SQLException {
 		PreparedStatement ps = con.prepareStatement("DELETE FROM multiple_choice_question WHERE question_id = ?");
 		ps.setInt(1, qID);
 		ps.executeUpdate();
 		
 		PreparedStatement prep = con.prepareStatement("DELETE FROM quiz_question_mapping WHERE question_id = ? AND question_type = ?");
 		prep.setInt(1, qID);
 		prep.setInt(2, type);
 		prep.executeUpdate();
 	}
 }
