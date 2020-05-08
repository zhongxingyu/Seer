 package entities;
 
 import java.lang.String;
 
 /**
  * @author Faly Razakarison
  * @version 1.0
  * @since 2013-02-27
  */
 public class Feedback{
 	/**
 	 * Emission date.
 	 */
 	private String date;
 	/**
 	 * Action performed by the robot (move forward, turn, stop, alert, ...).
 	 */
 	private String action;
 	/**
 	 * More information concerning the action (parameters, or error message).
 	 */
 	private String details;
 
 	/**
 	 * Constructor which initialize attributes with information received from the server.
 	 * @param row A row from feedback text. It should be in the following layout : date -> action => details.
 	 */
 	public Feedback(String row){
 		String[] sep1;
 		String[] sep2;
		sep1 = row.split(" -> ");
 		if (sep1.length == 2){
 			this.date = sep1[0];
			sep2 = sep1[1].split(" => ");
 			if(sep2.length == 2){
 				this.action = sep2[0];
 				this.details = sep2[1];
 			}else{
 				this.action = "";
 				this.details = "";
 				// ERROR : INCORRECT LAYOUT 
 			}
 		}else{
 			this.date = "";
 			this.action = "";
 			this.details = "";
 			// ERROR : INCORRECT LAYOUT 
 		}
 	}
 
 	/**
 	 * @return the date of the feedback
 	 */
 	public String getDate(){
 		return this.date;
 	}
 	
 	/**
 	 * @return the action of the feedback
 	 */
 	public String getAction(){
 		return this.action;
 	}
 
 	/**
 	 * @return details of the feedback
 	 */
 	public String getDetails(){
 		return this.details;
 	}
 
 	/**
 	 * @return Feedback string written using the server layout.
 	 */
 	@Override
 	public String toString(){
 		return this.getDate()+" -> "+this.getAction()+" => "+this.getDetails();
 	}
 }
