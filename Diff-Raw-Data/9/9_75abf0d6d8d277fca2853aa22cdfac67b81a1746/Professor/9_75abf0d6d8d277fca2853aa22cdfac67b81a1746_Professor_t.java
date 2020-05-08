 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package correction;
 
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.LinkedBlockingDeque;
 
 /**
  *
  * @author bluttrinker
  */
 public class Professor {
 	
 	private CountDownLatch latch; 					  
 	private LinkedList<Assistant> assistants;
 	private static volatile boolean terminate = false; // used later to finish work
 	public static IdleAssitantsCounter waitingAssistants = new IdleAssitantsCounter();
 	private final float distributionFrequency = 0.0f; //TODO using a float here will cause problems, change that
 	private int distributionCounter;    
 	private ExamStack[] stacks;
 	private Deque<Exam> finalstack = new LinkedBlockingDeque<Exam>();
 	private Thread[] threads;
 	
 	public Professor(int assis, int exams){
 		this.latch = new CountDownLatch(assis);
 		this.assistants = new LinkedList<Assistant>();
 		this.stacks = new ExamStack[assis];
 		for(int j=0; j<stacks.length;j++){
 			stacks[j] = new ExamStack(new ArrayList<Exam>());
 		}
 		this.distributionCounter = 1;
 		this.threads = new Thread[assis];
 		for(int i=0; i<assis; i++){
 			// TODO: maybe put this in initialize() 
 			this.assistants.add(new Assistant(i+1, stacks[i], stacks[i-1>=0? i-1 : assis-1], this ));
 		}
 	}
     
 	/**
 	 * @author Robin Burghartz
 	 * 
 	 * this method redistributes the exams of the assistants' stacks from time to time in order to keep the stacks even.
 	 * 'from time to time' means: depending on the distribution frequency the method will distribute or it will return immediately;
 	 * the higher the value distributionFrequency, the more often the prof will even out stacks, value 1 meaning that the prof will do it
 	 * in every iteration of his inner while loop, value 0 meaning that the prof will never do it.
 	 * furthermore, after deciding to distribute, the method will look at the stacks and decide if it really makes sense, and - if it
 	 * does - determine a clever way to do it, i.e. which stack to take exams from etc. pp. yadayada
 	 */
 	private void redistribute(){
 		if(! ((float) (this.distributionCounter) * this.distributionFrequency == 1.0)) return;
 		if(waitingAssistants.getN() < 1) return;
 		
 		// looking for the biggest stack
 		int biggest_stack = 0;
 		int smallest_stack = Integer.MAX_VALUE;
 		for(int i=0; i< stacks.length; i++){
 			if(stacks[i].getSize() > biggest_stack) biggest_stack = i;
 			if(stacks[i].getSize() < smallest_stack) smallest_stack = i;
 		}
 		
		Exam e = stack[biggest_stack].profPull();
		while(e!=null){
			if(e.exercisesToDo().contains(smallest_stack)){
				stacks[smallest_stack].profPush(e);
			}
			
			e=stack[biggest_stack].profPull();
 		}
 		//TODO : distribute the exams here, mayyyyybe do this in a separate distributor class
 		this.distributionCounter = 1;
 		return;
 	}
 	
 	
 	/**
 	 * @author Robin Burghartz
 	 * iterates through the assistants, creates threads for them and starts them
 	 * 
 	 */
 	private void initialize(){
 		int i=0;
 		for(Assistant a: assistants){
 			this.threads[i] = new Thread(a);
 			threads[i].start();
 			i++;
 		}
 	}
 	
 	
 	/**
 	 * @author Robin Burghartz
 	 * divides the exams into equal exam stacks (at the beginning)
 	 */
 	private void divide(int exams, int exerc){
 		
 		
 		int j = 0;
 		for(int i=0; i<exams; i++){
 			stacks[j].profPush(new Exam(exerc));
 			j = (j+1)%stacks.length;
 		}
 	}
 	
 	
 	/**
 	 * @author Robin Burghartz
 	 * @param args
 	 */
 	public static void main(String args[]){
 		
 		int assis, exams;
 		//TODO: remove hardcoded values, implement with arguments
 		if(args.length ==0){
 			assis = 10;
 			exams = 1000;
 			System.out.println("Using default values: 4 exercises, 200 exams");
 			System.out.println(""); //TODO
 		}
 		else if(args.length == 2){
 			assis = Integer.parseInt(args[0]);
 			exams = Integer.parseInt(args[1]);
 		}
 		else{
 			throw new IllegalArgumentException("illegal no of arguments");
 		}
 		
 		Professor prof = new Professor(assis,exams);
 		prof.divide(exams,assis);
 		prof.initialize();
 		
 		// as long as work is not done yet, do this
 		while(!shouldTerminate()){
 			
 			// as soon as all assistants are waiting, this loop will be quit and the professor will check if work is really done
 			while(waitingAssistants.getN() < prof.getAssistants().size()){
 				if(!prof.finalstack.isEmpty()){
 					Exam e = prof.finalstack.removeFirst(); // take the first exam of the final stack
 					e.finish();							// FINISH HIM!...ehm...it.
 				}
 				prof.redistribute();					// from time to time even out stacks
 			}
 			
 			// interrupt all assistants so noone has an exam in their hand 
 			for(int i=0; i<prof.threads.length; i++){
                                 Thread t = prof.threads[i];
                                 prof.stacks[i-1>=0? i-1 : assis-1].assiLock();
 				t.interrupt();
                                 prof.stacks[i-1>=0? i-1 : assis-1].returnAssiLock();
 			}
 			
 			// using a latch to make sure that every assistant is actually interrupted before proceeding
 			try {
 				prof.latch.await();
 			} catch (InterruptedException e1) {
 				throw new IllegalStateException("bla");
 			}
 			
 			// we assume that no exams are left -> we can terminate
 			done(true);
 			for(int i=0; i<prof.stacks.length;i++){
 				// if we find an exam, we were wrong and work is not done yet
 				if(!prof.stacks[i].isEmpty()) done(false);
 			}
 			
 			//TODO: remove hardcoded value
 			// latch has been used, get a new one
 			prof.latch = new CountDownLatch(assis);
 			
 			// wake up threads so they can check whether work is done or not
 			for(Assistant a: prof.assistants){
 				synchronized(a){
 					a.notifyAll();
 				}
 				
 			}
 			
 			// if the assistants have done their work, the professor might still have to finish some exams, we cannot terminate just yet
 			if(shouldTerminate()){
 				while(!prof.finalstack.isEmpty()){
 					Exam e = prof.finalstack.removeFirst();
 					e.finish();
 				}
 			}
 			
 			
 		}
 		
 		// when the professor wants to terminate, wait for other threads to terminate first
 		try {
 			prof.latch.await();
 		} catch (InterruptedException e) {
 			throw new IllegalStateException();
 		}
 		prof.latch = new CountDownLatch(assis);
 		
 		
 		
 		
 	}
 
 	// getters and setters
 	
 	public CountDownLatch getLatch() {
 		return latch;
 	}
 
 	public void setLatch(CountDownLatch latch) {
 		this.latch = latch;
 	}
 
 	public LinkedList<Assistant> getAssistants() {
 		return assistants;
 	}
 
 	public void setAssistants(LinkedList<Assistant> assistants) {
 		this.assistants = assistants;
 	}
 
 
    public static synchronized boolean shouldTerminate(){
        return terminate;
    } 
    
    private static synchronized void done(boolean b){
 	   terminate = b;
    }
    
    public synchronized void countdownLatch(){
 	   latch.countDown();
    }
     
    public void pushFinalStack(Exam e){
 	   finalstack.addLast(e);
    }
     
 }
