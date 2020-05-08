 package scheduler;
 
 import java.lang.Math;
 
 /**
  * A stub for your Greedy Descent With Restarts scheduler
  */
 public class GreedyDescentWithRestartsScheduler extends Scheduler {
 	private static final boolean ROOM = true;
 	private static final boolean TIME = false;
 
 	/**
 	 * @return names of the authors and their student IDs (1 per line).
 	 */
 	public String authorsAndStudentIDs() {
 		String ret = "NAMES OF THE AUTHORS AND THEIR STUDENT IDs (1 PER LINE)\n";
 		ret += "Yi (Armand) Li 61420048\n";
		ret += "Jeffrey Cheung 66994062\n";
 		return ret;
 	}
 
 	/**
 	 * @throws Exception 
 	 * @see scheduler.Scheduler#schedule(scheduler.SchedulingInstance)
 	 */
 	public ScheduleChoice[] solve(SchedulingInstance pInstance) throws Exception {
 		ScheduleChoice[] bestScheduleFound = randomAssignment(pInstance);
 		int best = Integer.MAX_VALUE, best_course_change = 0, best_val = bestScheduleFound[0].room, temp, eval;
 		boolean is_best_room_change = ROOM, noChange = true;
 		while( (best = evaluator.violatedConstraints(pInstance, bestScheduleFound))>0 && !timeIsUp() ){
 			for (int i = 0; i < pInstance.numCourses; ++i){
 				for (int j = 0; j < pInstance.numRooms; ++j){
 					temp = bestScheduleFound[i].room;
 					bestScheduleFound[i].room = j;
 					eval = evaluator.violatedConstraints(pInstance, bestScheduleFound);
 					if (eval < best){
 						best = eval;
 						best_course_change = i;
 						best_val = j;
 						is_best_room_change = ROOM;
 						noChange = false;
 					}
 					bestScheduleFound[i].room = temp;
 				}
 				for (int j = 0; j < pInstance.numTimeslots; ++j){
 					temp = bestScheduleFound[i].timeslot;
 					bestScheduleFound[i].timeslot = j;
 					eval = evaluator.violatedConstraints(pInstance, bestScheduleFound);
 					if (eval < best){
 						best = eval;
 						best_course_change = i;
 						best_val = j;
 						is_best_room_change = TIME;
 						noChange = false;
 					}
 					bestScheduleFound[i].timeslot = temp;
 				}
 			}
 			if (noChange) // random restart if we have reached to buttom of the pound
 				bestScheduleFound = randomAssignment(pInstance);
 			else {
 				if (is_best_room_change)
 					bestScheduleFound[best_course_change].room = best_val;
 				else 
 					bestScheduleFound[best_course_change].timeslot = best_val;
 			}
 			
 			noChange = true;
 		}
 		return bestScheduleFound;
 	}
 	
 	ScheduleChoice[] randomAssignment(SchedulingInstance in){
 		ScheduleChoice[] choice = new ScheduleChoice[in.numCourses];
 		for (int i = 0; i < in.numCourses; ++i){
 			choice[i] = new ScheduleChoice();
 			choice[i].room = Math.abs(this.r.nextInt()) % in.numRooms;
 			choice[i].timeslot = Math.abs(this.r.nextInt()) % in.numTimeslots;
 		}
 		return choice;
 	}
 
 }
