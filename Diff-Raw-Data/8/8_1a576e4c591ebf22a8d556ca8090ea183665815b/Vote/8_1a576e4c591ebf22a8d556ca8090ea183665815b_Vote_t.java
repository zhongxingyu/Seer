 package com.rau.evoting.beans;
 
 import java.util.ArrayList;
 
import javax.faces.context.FacesContext;

 import com.rau.evoting.data.SqlDataProvider;
 import com.rau.evoting.models.*;
 import com.rau.evoting.utils.Util;
 
 public class Vote {
 	
 	private ArrayList<Answer> answers1;
 	private ArrayList<Answer> answers2;
 	private int selectedAnswer1;
 	private int selectedAnswer2;
 	
 	public Vote() {
 		
 	}
 
 	public ArrayList<Answer> getAnswers1() {
 		return answers1;
 	}
 
 	public void setAnswers1(ArrayList<Answer> answers) {
 		this.answers1 = answers;
 	}
 	
 	public ArrayList<Answer> getAnswers2() {
 		return answers2;
 	}
 
 	public void setAnswers2(ArrayList<Answer> answers2) {
 		this.answers2 = answers2;
 	}
 
	public String fromElection() {
		int elId = Integer.valueOf(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("elId"));
 		answers1 = SqlDataProvider.getInstance().getElectionAnswers(elId);
 		answers2 = SqlDataProvider.getInstance().getElectionAnswers(elId);
 		return "Vote";
 	}
 	
 	public int getSelectedAnswer1() {
 		return selectedAnswer1;
 	}
 
 	public void setSelectedAnswer1(int selectedAnswer1) {
 		this.selectedAnswer1 = selectedAnswer1;
 	}
 
 	public int getSelectedAnswer2() {
 		return selectedAnswer2;
 	}
 
 	public void setSelectedAnswer2(int selectedAnswer2) {
 		this.selectedAnswer2 = selectedAnswer2;
 	}
 
 	public String shuffle() {
 		Util.shuffle(answers1);
 		Util.shuffle(answers2);
 		return null;
 	}
 	
 
 	public String encode()
 	{
 		//showShuffle = false;
 		//codeValue = "decode";
 		return "encode";
 	}
 	
 }
