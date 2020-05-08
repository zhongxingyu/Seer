 
 
 /**
  * This class implements the barber's part of the
  * Barbershop thread synchronization example.
  */
 public class Barber extends Thread{
 	/**
 	 * Creates a new barber.
 	 * @param queue		The customer queue.
 	 * @param gui		The GUI.
 	 * @param pos		The position of this barber's chair
 	 */
 	
 	private int pos = 0;
 	private CustomerQueue queue = null;
 	private Gui gui = null;
 	private boolean working = false;
 	
 	public Barber(CustomerQueue queue, Gui gui, int pos) { 
 		this.pos = pos;
 		this.queue = queue;
 		this.gui = gui;
 	}
 
 	/**
 	 * Starts the barber running as a separate thread.
 	 */
 	public void startThread() {
 		gui.println("barber is starting barbering people");
 		working = true;
 		setName("Barber " + pos); // Names the Threads so it will be easier to identify error sources
 		start();
 	}
 
 	/**
 	 * Stops the barber thread.
 	 */
 	public void stopThread() {
 		working = false;
 	}
 
 	public void run() {
 		while(working) {
 			gui.barberIsSleeping(pos);
 			try {
 				sleep(Globals.barberSleep);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			gui.println("barber barbering");
			gui.barberIsAwake(pos);
 			gui.fillBarberChair(pos, queue.getCustomer()); //Temp
 			try {
 				sleep(Globals.barberWork);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
			gui.emptyBarberChair(pos);
 		}
 	}
 }
