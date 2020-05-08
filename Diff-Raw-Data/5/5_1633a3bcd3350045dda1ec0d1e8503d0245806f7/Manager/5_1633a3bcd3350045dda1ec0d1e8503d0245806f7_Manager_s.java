 package edu.se.se441.threads;
 
 import java.util.concurrent.*;
 
 import edu.se.se441.Office;
 
 public class Manager extends Thread {
 	
 	final int NUMQUESTIONS = 10;
 	
 	private Office office;
 	private CountDownLatch startSignal;
 	private BlockingQueue<Employee> hasQuestion = new ArrayBlockingQueue<Employee>(NUMQUESTIONS);
 	private volatile boolean attendedMeeting1 = false;
 	private volatile boolean attendedMeeting2 = false;
 	private volatile boolean ateLunch = false;
 	private volatile boolean attendedFinalMeeting = false;
 	private Object questionLock = new Object();
 	private long timeSpentAnsweringQuestions = 0;
 	private long timeSpentInMeetings = 0;
 	private long timeSpentWorking = 0;
 	private long timeSpentAtLunch = 0;
 
 	
 	public Manager(Office office){
 		this.office = office;
 		office.setManager(this);
 	}
 	
 	public void run(){
 		office.addTimeEvent(1000);	// 10AM Meeting
 		office.addTimeEvent(1200);	// Lunch Time
 		office.addTimeEvent(1200);	// 2PM Meeting
 		office.addTimeEvent(1600);	// 4PM Meeting
 		office.addTimeEvent(1700);	// 5PM end of day
 		
 		try {
 			// Starting all threads at the same time (clock == 0 / "8:00AM").
 			startSignal.await();
 			Thread.yield();
 			if(office.getTime() == 800){
 				System.out.println(office.getStringTime() + " Manager arrives at office");
 			} else {
 				System.out.println("8:00 Manager arrives at office");
 			}
 			long startCheck = System.currentTimeMillis();
 			// Waiting for team leads for the meeting.
 			office.waitForStandupMeeting();
 			long endCheck = System.currentTimeMillis();
 			
 			System.out.println("Worked: " + (int)((endCheck - startCheck)/10));
 			timeSpentWorking =+ (endCheck - startCheck)/10; 
 			
 			startCheck = System.currentTimeMillis();
 			Thread.sleep(150);
 			endCheck = System.currentTimeMillis();
 			
 			timeSpentInMeetings += (endCheck - startCheck)/10;
 			
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		
 		while(office.getTime() < 1700){
 			long startCheck = System.currentTimeMillis();
 			office.startWorking();
 			long endCheck = System.currentTimeMillis();
 			
 			timeSpentWorking += (endCheck - startCheck)/10; 
 			
 			while(!hasQuestion.isEmpty()){
 				checkConditions();
 				startCheck = System.currentTimeMillis();
 				answerQuestion();
 				endCheck = System.currentTimeMillis();
 				
 				timeSpentAnsweringQuestions += (endCheck - startCheck)/10;				
 			}
 			checkConditions();
 			
 		}
 		System.out.println(office.getStringTime() + " Manager leaves");
 		
 		System.out.println("Manager report: a) " + timeSpentWorking + " b) " + timeSpentAtLunch +
 				" c) " + timeSpentInMeetings + " c) " + timeSpentAnsweringQuestions);
 	}
 	
 	public void setStartSignal(CountDownLatch startSignal) {
 		this.startSignal = startSignal;
 	}
 	
 	public void checkConditions(){
 		if(office.getTime() >= 1000 && !attendedMeeting1){
 			 try {
 				 long startCheck = System.currentTimeMillis();
 				System.out.println(office.getStringTime() + " Manager goes to meeting");
 				sleep(600);
 				long endCheck = System.currentTimeMillis();
 				
 				timeSpentInMeetings += (endCheck - startCheck)/10;
 						
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			 attendedMeeting1 = true;
 		}
 		if(office.getTime() >= 1200 && !ateLunch){
 			try {
 				long startCheck = System.currentTimeMillis();
 				System.out.println(office.getStringTime() + " Manager goes to lunch");
 				sleep(600);
 				long endCheck = System.currentTimeMillis();
 				
 				timeSpentAtLunch += (endCheck - startCheck)/10;
 				
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}				
 			ateLunch = true;
 		}
 		if(office.getTime() >= 1400 && !attendedMeeting2){
 			try {
 				long startCheck = System.currentTimeMillis();
 				System.out.println(office.getStringTime() + " Manager goes to meeting");
 				sleep(600);
 				long endCheck = System.currentTimeMillis();
 				
				timeSpentAtLunch += (endCheck - startCheck)/10;
 				
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			attendedMeeting2 = true;
 		}
 		
 		// Is it time for the 4 oclock meeting?
 		if(office.getTime() >= 1600 && !attendedFinalMeeting){
 			office.waitForEndOfDayMeeting();
 			try {
 				long startCheck = System.currentTimeMillis();				
 				System.out.println(office.getStringTime() + " Manager heads to end of day meeting");
 				sleep(150);
 				long endCheck = System.currentTimeMillis();
 
				timeSpentAtLunch += (endCheck - startCheck)/10;
 				
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			attendedFinalMeeting = true;
 		}
 	}
 	
 	public void askQuestion(Employee employee){
 		// Add question to queue
 		synchronized(hasQuestion){
 			hasQuestion.add(employee);
 		}
 		
 		// Waiting until question can be answered
 		synchronized(questionLock){
 			while(hasQuestion.contains(employee)){
 				// Is it time for the 4 o'clock meeting?
 				try {
 					if(office.getTime() >= 1600 && !employee.isAttendedEndOfDayMeeting()){
 						System.out.println("Starting end of day meeting.");
 						office.waitForEndOfDayMeeting();
 						System.out.println("Everyone is ready for end of day meeting.");
 						sleep(150);
 						employee.setAttendedEndOfDayMeeting(true);
 					}
 					
 					// Tell the Manager there is a question.
 					office.notifyWorking();
 					questionLock.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			// Question is being answered
 			while(employee.isWaitingQuestion()){
 				try {
 					questionLock.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public boolean isLeadAsking(Employee lead){
 		return hasQuestion.contains(lead);
 	}
 	
 	public Object getQuestionLock(){
 		return questionLock;
 	}
 	
 	private void answerQuestion(){
 		Employee employee = hasQuestion.poll();
 		if(office.getTime() < 1700){
 			synchronized(questionLock){
 				questionLock.notifyAll();
 			}
 			
 			try {
 				System.out.println(office.getStringTime() + " Manager starts answering question.  Queue depth: " + hasQuestion.size());
 				sleep(100);
 				System.out.println(office.getStringTime() + " Manager ends answering question.  Queue depth: " + hasQuestion.size());
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			employee.questionAnswered();
 			synchronized(questionLock){
 				questionLock.notifyAll();
 			}
 		} else{
 			while(!hasQuestion.isEmpty()){
 				hasQuestion.poll();
 			}
 			synchronized(questionLock){
 				questionLock.notifyAll();
 			}
 		}
 	}
 
 }
