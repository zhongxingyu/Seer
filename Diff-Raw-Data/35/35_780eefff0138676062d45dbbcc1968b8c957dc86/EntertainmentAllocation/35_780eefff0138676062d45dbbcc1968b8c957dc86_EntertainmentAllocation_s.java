 /**
  * 
  */
 package se.sics.tac.aw;
 
 /**
  * @author Lee Stone
  *
  */
 public class EntertainmentAllocation {
 
 	private int funBonus;
	private int assignedDay=0;
 	
 	/**
 	 * @param funBonus
 	 * @param assignedDay
 	 */
 	public EntertainmentAllocation(int funBonus, int assignedDay) {
 		this.funBonus = funBonus;
 		this.assignedDay = assignedDay;
 	}
 
 	/**
 	 * @return the funBonus
 	 */
 	public int getFunBonus() {
 		return funBonus;
 	}
 
 	/**
 	 * @param funBonus the funBonus to set
 	 */
 	public void setFunBonus(int funBonus) {
 		this.funBonus = funBonus;
 	}
 
 	/**
 	 * @return the assignedDay
 	 */
 	public int getAssignedDay() {
 		return assignedDay;
 	}
 
 	/**
 	 * @param assignedDay the assignedDay to set
 	 */
 	public void setAssignedDay(int assignedDay) {
 		this.assignedDay = assignedDay;
 	}
 	
 	
 }
