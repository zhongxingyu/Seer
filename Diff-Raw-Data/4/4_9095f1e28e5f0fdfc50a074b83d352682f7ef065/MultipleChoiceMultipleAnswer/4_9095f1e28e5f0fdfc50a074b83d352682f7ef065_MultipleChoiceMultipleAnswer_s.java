 package model;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 public class MultipleChoiceMultipleAnswer implements Question {
 
 	public static final int type=6;
 	private String statement;
 	private Set<String> answers;
 	private Set<String> wrongAnswers;
 	private int qID;
 	private ArrayList<String> userAnswers;
 	private Set<String> options;
 	
 	public static String getHTMLInputString(){
 		StringBuilder html = new StringBuilder();
 		
 		html.append("<br />Insert Question Statement: <br /><input type=\"text\" name=\"question\" size=\"75\" maxlength=\"250\" required />");
 		html.append("<br />Insert All Correct Answers, one on each line:");
 		html.append("<br /><textarea name=\"answers\" cols=\"20\" rows=\"10\" maxlength=\"250\" required></textarea>");
 		html.append("<br />Insert All Incorrect Options, one on each line:");
 		html.append("<br /><textarea name=\"wrongAnswers\" cols=\"20\" rows=\"10\" maxlength=\"250\" ></textarea>");
 		
 		return html.toString();
 	}
 	
 
 	
 	public MultipleChoiceMultipleAnswer(String question, Set<String> ans, Set<String> wrongAns) { // pushes to database
 		this.wrongAnswers = new HashSet<String>();
 		
 		question = question.replace("\"", "");
 		this.statement = (question.length() < 250) ? question : (question.substring(0, 245) + "...");
 		this.answers = new HashSet<String>();
 		
 		options = new HashSet<String>();
 		for(String s : wrongAns) {
 			s = s.replace("\"", "");
 			this.wrongAnswers.add(s);
 			options.add(s);
 		}
 		
 		for(String str : ans) {
 			str = str.replace("\"", "");
 			this.answers.add(str);
 			options.add(str);
 		}
 		
 		answers.remove("");
 		wrongAnswers.remove("");
 		options.remove("");
 	}
 
 	public MultipleChoiceMultipleAnswer(int id, Connection con) { // pulls from database
 		generate(id, con);
 	}
 	
 	public void generate(int id, Connection con) {
 		setqID(id);
 		try {
 			
 			PreparedStatement ps = con.prepareStatement("select * from multiple_choice_multiple_answer_question where question_id = ?");
 			ps.setInt(1, id);
 			ResultSet resultSet = ps.executeQuery();
 		
 			String ans = new String();
 			String wrongAns = new String();
 			
 			while (resultSet.next()) {
 				statement = resultSet.getString("statement");
 				ans = resultSet.getString("answer");
 				wrongAns = resultSet.getString("wrong_answers");
 				
 			}
 			
 			options = new HashSet<String>();
 			
 			String[] strings = ans.split(Pattern.quote(" &&& "));
 			answers = new HashSet<String>();
 			for (String string : strings) {
 				answers.add(string);
 				options.add(string);
 			}
 			
 			strings = wrongAns.split(Pattern.quote(" &&& "));
 			wrongAnswers = new HashSet<String>();
 			for (String string : strings) {
 				wrongAnswers.add(string);
 				options.add(string);
 			}
 		}catch(Exception e){
 			
 		}
 	}
 	
 	public String getStatement() {
 		return statement;
 	}
 
 	public void setStatement(String statement) {
 		statement = statement.replace("\"", "");
 		this.statement = (statement.length() < 250) ? statement : (statement.substring(0, 245) + "...");
 	}
 
 	public Set<String> getAnswers() {
 		return answers;
 	}
 
 	public void setAnswers(Set<String> answers) {
 		this.answers = new HashSet<String>();
 		
 		options.clear();
 		
 		for(String s : answers) {
 			s = s.replace("\"", "");
 			this.answers.add(s);
 			options.add(s);
 		}
 		answers.remove("");
 		
 		for(String str : this.wrongAnswers) {
 			options.add(str);
 		}
 		options.remove("");
 	}
 
 	public int solve(ArrayList<String> answer) {
 		int score =0;
 		for (String string : answer) {
 			if (this.answers.contains(string)) {
 				score++;
 			}else{
 				score--;
 			}
 		}
 		
 		int diff;
 		if(answers.size() > this.answers.size()) {
 			diff = answers.size()-this.answers.size();
 		}
 		else {
 			diff = this.answers.size() - answers.size();
 		}
 		
 		score -= diff;
 		if(score < 0) score = 0;
 		
 		return score;
 	}
 
 	public Set<String> getWrongAnswers() {
 		return wrongAnswers;
 	}
 
 	public void setWrongAnswers(Set<String> wrongAnswers) {
 		this.wrongAnswers = new HashSet<String>();
 		
 		options.clear();
 		
 		for(String s : wrongAnswers) {
 			s = s.replace("\"", "");
 			wrongAnswers.add(s);
 			options.add(s);
 		}
 		
 		options.remove("");
 		wrongAnswers.remove("");
 	}
 
 	@Override
 	public String toHTMLString() {
 		StringBuilder html = new StringBuilder();
 		
         html.append(statement);
         html.append("<br />");
         
         int counter = 0;
         
         for(String s : options) {
                 html.append("<input type=\"checkbox\" name=\"");
         		html.append(type);
         		html.append("_");
                 html.append(qID);
                 html.append("_");
                 html.append(counter);
                 html.append("\" value=\"");
                 html.append(s + "\">" + s);
                 html.append("<br />");
                 counter++;
         }
         
 		return html.toString();
 	}
 
 	public int getqID() {
 		return qID;
 	}
 
 	public void setqID(int qID) {
 		this.qID = qID;
 	}
 
 	@Override
 	public String getCorrectAnswers() {
 		StringBuilder correctAnswers = new StringBuilder();
 		
 		for(String s : answers) {
 			correctAnswers.append(s);
 			correctAnswers.append(",  ");
 		}
 		correctAnswers.replace(correctAnswers.length()-3, correctAnswers.length(), "");
 		
 		return correctAnswers.toString();
 	}
 
 	public int getType(){
 		return type;
 	}
 
 	@Override
 	public void pushToDB(Connection con) throws SQLException {
 		PreparedStatement ps = con.prepareStatement("insert into multiple_choice_multiple_answer_question values(null, ?, ?, ?)");
 		
 		statement = statement.trim();
 		ps.setString(1, statement);
 		
 		StringBuilder answersString = new StringBuilder();
 		for(String ans : answers) {
 			ans = ans.trim();
 			answersString.append(ans);
 			answersString.append(" &&& ");
 		}
 		answersString.replace(answersString.length()-5, answersString.length(), "");
 		ps.setString(2, answersString.toString());
 		
 		StringBuilder wrongAnswersString = new StringBuilder();
 		if(wrongAnswers.size() != 0) {
 			for(String wAns : wrongAnswers) {
 				wAns = wAns.trim();
 				wrongAnswersString.append(wAns);
 				wrongAnswersString.append(" &&& ");
 			}
 			wrongAnswersString.replace(wrongAnswersString.length()-5, wrongAnswersString.length(), "");
 		}
 		ps.setString(3, wrongAnswersString.toString());
 		
 		ps.executeUpdate();
 		
 		PreparedStatement stat = con.prepareStatement("select * from multiple_choice_multiple_answer_question where statement = ?");
 		
 		stat.setString(1, statement);
 		
 		ResultSet rs = stat.executeQuery();
 		
 		while(rs.next()) {
 			this.setqID(rs.getInt("question_id"));
 		}	
 	}
 
 	@Override
 	public void setUserAnswers(ArrayList<String> ans) {
 		userAnswers = new ArrayList<String>();
 		
 		for(String s : ans) {
 			s = s.replace("\"", "");
 			userAnswers.add(s);
 		}
 		
 		HashSet<String> hs = new HashSet<String>();
 		hs.addAll(userAnswers);
 		userAnswers.clear();
 		userAnswers.addAll(hs);
 	}
 
 	@Override
 	public String getUserAnswers() {
 		StringBuilder userAns = new StringBuilder();
 		
 		for(String s : userAnswers) {
 			userAns.append(s);
 			userAns.append(", ");
 		}
 		if (userAns.length()>3) {
 			userAns.replace(userAns.length()-2, userAns.length(), "");
 		}
 		return userAns.toString();
 	}
 
 	@Override
 	public int getTotalQScore() {
 		return answers.size();
 	}
 
 	@Override
 	public int getNumAnswers() {
 		return (answers.size() + wrongAnswers.size());
 	}
 
 	public String getEditAnswersString(Set<String> set) {
 		StringBuilder str = new StringBuilder();
 		
 		for(String s : set) {
 			str.append(s);
 			str.append("\n");
 		}
 		
 		str.replace(str.length()-1, str.length(), "");
 		
 		return str.toString();
 	}
 
 	@Override
 	public String getEditQuizString() {
 		StringBuilder html = new StringBuilder();
 		
 		html.append("<br />Multiple-Choice-Multiple-Answer Question");
 		html.append("<br />Insert Question Statement: <br /><input type=\"text\" name=\""+type+"_"+qID+"_question\" size=\"75\" value=\""+statement+"\" maxlength=\"250\" required />");
 		html.append("<br />Insert All Correct Answers, one on each line:");
 		html.append("<br /><textarea name=\""+type+"_"+qID+"_answers\" cols=\"20\" rows=\"10\" maxlength=\"250\" required>"+getEditAnswersString(answers)+"</textarea>");
 		html.append("<br />Insert All Incorrect Options, one on each line:");
 		html.append("<br /><textarea name=\""+type+"_"+qID+"_wrongAnswers\" cols=\"20\" rows=\"10\" maxlength=\"250\">"+getEditAnswersString(wrongAnswers)+"</textarea>");
 		
 		return html.toString();
 	}
 
 
 
 	@Override
 	public void updateDB(Connection con) throws SQLException {
 		PreparedStatement ps = con.prepareStatement("UPDATE multiple_choice_multiple_answer_question SET statement = ?, answer = ?, wrong_answers = ? WHERE question_id = ?");
 		
 		ps.setString(1, statement);
 		
 		StringBuilder ans = new StringBuilder();
 		for(String a : answers) {
 			ans.append(a);
 			ans.append(" &&& ");
 		}
 		ans.replace(ans.length()-5, ans.length(), "");
 		
 		ps.setString(2, ans.toString());
 		
 		StringBuilder wAns = new StringBuilder();
 		if(wrongAnswers.size() != 0) {
 			for(String wa : wrongAnswers) {
 				wAns.append(wa);
 				wAns.append(" &&& ");
 			}
 			wAns.replace(wAns.length()-5, wAns.length(), "");
 		}
 		
 		ps.setString(3, wAns.toString());
 		
 		ps.setInt(4, qID);
 		
 		ps.executeUpdate();
 	}
 
 
 
 	@Override
 	public void deleteFromDB(Connection con) throws SQLException {
 		PreparedStatement ps = con.prepareStatement("DELETE FROM multiple_choice_multiple_answer_question WHERE question_id = ?");
 		ps.setInt(1, qID);
 		ps.executeUpdate();
 		
 		PreparedStatement prep = con.prepareStatement("DELETE FROM quiz_question_mapping WHERE question_id = ? AND question_type = ?");
 		prep.setInt(1, qID);
 		prep.setInt(2, type);
 		prep.executeUpdate();
 	}
 }
