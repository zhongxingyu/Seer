 import java.util.Random;
 import java.util.concurrent.CountDownLatch;
 
 /**
  * @author Curtis Burtner and Emma Nelson
  * 
  *         The main class. Contains the main method, system constants, ability
  *         to populate teams and print statistics.
  */
 public class Main {
 
 	/*
 	 * System constants
 	 */
 
 	/**
 	 * The total number of employees in the system. 3 teams + ProjectManager
 	 */
 	public static final int NUMBER_OF_EMPLOYEES = 13;
 
 	/**
 	 * The total number of teams in the system. Team is defined as 1 team lead
 	 * and 3 developers.
 	 */
 	public static final int NUMBER_OF_TEAMS = 3;
 
 	/**
 	 * The total number of employees on each team.
 	 */
 	public static final int MEMBERS_PER_TEAM = 4;
 
 	/**
 	 * This latch will be passed to each Employee (And the ProjectManager) and
 	 * the Timer in order to start all Employee threads and the Timer thread at
 	 * the same time.
 	 */
 	private static final CountDownLatch cd = new CountDownLatch(
 			NUMBER_OF_EMPLOYEES + 1);
 
 	/**
 	 * Latch that ensures the morning meeting starts right when everyone
 	 * arrives.
 	 */
 	private static final CountDownLatch firstMeeting = new CountDownLatch(
 			NUMBER_OF_TEAMS + 1);
 
 	/**
 	 * Latch that makes sure the end of day meeting starts when all employees
 	 * have arrived.
 	 */
 	private static final CountDownLatch lastMeeting = new CountDownLatch(
 			NUMBER_OF_EMPLOYEES);
 
 	/**
 	 * Latch that releases employees to start going home after the end of day
 	 * meeting.
 	 */
 	private static final CountDownLatch lastMeetingOver = new CountDownLatch(
 			NUMBER_OF_EMPLOYEES);
 
 	/**
 	 * Conference room that Team leads must obtain a lock on before hosting
 	 * meeting.
 	 */
 	private final static Object confRoom = new Object();
 
 	/**
 	 * The ProjectManager.
 	 */
 	private final static ProjectManager pm = new ProjectManager(cd,
 			lastMeeting, lastMeetingOver, firstMeeting);
 
 	/**
 	 * A random Random class constant.
 	 */
 	private final static Random r = new Random();
 
 	/**
 	 * The list of teams is represented by a 2d Array. Each array of employees
 	 * represents a single team, and the array of arrays represents all the
 	 * teams in this firm.
 	 */
 	private final static Employee[][] teams = Main.populateTeams(
 			NUMBER_OF_TEAMS, MEMBERS_PER_TEAM);
 
 	/**
 	 * The timer that each thread will use to keep track of time.
 	 */
 	private final static FirmTime timer = new FirmTime(cd);
 
 	/**
 	 * Initializes a 2D array of Employees. Also starts each thread as it's
 	 * created.
 	 */
 	private static Employee[][] populateTeams(int numberOfTeams,
 			int membersPerTeam) {
 		Employee[][] teams = new Employee[numberOfTeams][membersPerTeam];
 
 		// Creates each team
 		for (int i = 0; i < numberOfTeams; i++) {
 
 			// Creates team members (For each team)
 			for (int j = 0; j < membersPerTeam; j++) {
 				// The first member of a team will always be the team lead
 				if (j == 0) {
 					TeamLead tL = new TeamLead(j, i, cd, lastMeeting,
 							lastMeetingOver, firstMeeting);
 					teams[i][j] = tL;
 					tL.start();
 				}
 				// Everyone else is a regular employee
 				else {
 					Employee e = new Employee(j, i, cd, lastMeeting,
 							lastMeetingOver);
 					teams[i][j] = e;
 					e.start();
 				}
 			}
 		}
 		return teams;
 	}
 
 	/**
 	 * Gets the collection of all the employees.
 	 * 
 	 * @return 2D array of employees
 	 */
 	public static Employee[][] getAllEmployees() {
 		return teams;
 	}
 
 	/**
 	 * Getter for conference room lock.
 	 * 
 	 * @return confRoom
 	 */
 	public static Object getConferenceRoom() {
 		return confRoom;
 	}
 
 	/**
 	 * Returns the leader of a passed Employee's team
 	 * 
 	 * @param lead
 	 *            The Team Lead whose leader you want to find
 	 */
 	public static TeamLead getLead(int teamID) {
 		TeamLead lead = (TeamLead) teams[teamID][0];
 
 		return lead;
 	}
 
 	/**
 	 * Getter for the firm's FirmTime
 	 * 
 	 * @return timer
 	 */
 	public static FirmTime getFirmTime() {
 		return timer;
 	}
 
 	/**
 	 * Getter for the firm's ProjectManager
 	 * 
 	 * @return pm
 	 */
 	public static ProjectManager getProjectManager() {
 		return pm;
 	}
 
 	/**
 	 * Getter for Random r
 	 * 
 	 * @return r
 	 */
 	public static Random getRandom() {
 		return r;
 	}
 
 	/**
 	 * Prints the final statistics across ALL employees (PM included)
 	 */
 	public static void printStatistics() {
 		int totalMeetingTime = 0;
 		int totalLunchTime = 0;
 		int totalWaitTime = 0;
 		int totalWorkTime = 0;
 
 		for (int i = 0; i < NUMBER_OF_TEAMS; i++) {
 			for (int j = 0; j < MEMBERS_PER_TEAM; j++) {
 				totalMeetingTime += teams[i][j].getTimeInMeetings();
 				totalLunchTime += teams[i][j].getTimeAtLunch();
 				totalWaitTime += teams[i][j].getTimeWaitingForPm();
 				totalWorkTime += teams[i][j].getTimeWorking();
 			}
 		}
 		totalMeetingTime += pm.getTimeInMeetings();
 		totalLunchTime += pm.getTimeAtLunch();
 		totalWaitTime += pm.getTimeWaitingForPm();
 		totalWorkTime += pm.getTimeWorking();
 
 		System.out
 				.println("Total time spent in meetings = " + totalMeetingTime);
		System.out.println("Total time spent eating lunch = " + totalLunchTime);
 		System.out
				.println("Total time spent waiting to get questions answered = "
 						+ totalWaitTime);
 		System.out.println("Total time spent actually working = "
 				+ totalWorkTime);
 	}
 
 	/**
 	 * Main method. Starts pm and timer, sets the system in motion and closes
 	 * everything down at the end of the day.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		pm.start();
 		timer.start();
 
 		for (int i = 0; i < NUMBER_OF_TEAMS; i++) {
 			for (int j = 0; j < MEMBERS_PER_TEAM; j++) {
 				try {
 					teams[i][j].join();
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 
 		try {
 			pm.join();
 		} catch (InterruptedException e) {
 		}
 
 		Main.getFirmTime().cancel();
 		Main.printStatistics();
 		return;
 	}
 }
