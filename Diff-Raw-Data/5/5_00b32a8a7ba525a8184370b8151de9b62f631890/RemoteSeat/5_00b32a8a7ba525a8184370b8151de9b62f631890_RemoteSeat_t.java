 package aufgabe4.models;
 
 import java.rmi.RemoteException;
 import java.util.Queue;
 import java.util.concurrent.ArrayBlockingQueue;
 
 import aufgabe4.PhilosopherWorker.PhilWorker;
 
 public class RemoteSeat extends AbstractRemoteObject implements RemoteSeatIF {
 	
 	private int rightForkID = -1;
 	private int leftForkID = -1;
 	private int philID = -1;
 	private int seatNumber;
 	//the capacity specifies the max number of philosophers waiting for this seat
 	private Queue<Integer> waitingQueue = new ArrayBlockingQueue<Integer>(100, true);
 	
 	public RemoteSeat(int leftForkID, int rightForkID, int number) {
 		super();
 		this.rightForkID = rightForkID;
 		this.leftForkID = leftForkID;
 		this.seatNumber = number;
 	}
 
 	public RemoteSeat(RemoteForkIF leftFork, RemoteForkIF rightFork, int number) throws RemoteException {
 		super();
 		this.setRightFork(rightFork);
 		this.setLeftFork(leftFork);
 		this.seatNumber = number;
 	}
 	
 	public RemoteSeat(int objectID, RemoteForkIF leftFork, RemoteForkIF rightFork, int number) throws RemoteException {
 		super(objectID);
 		this.setRightFork(rightFork);
 		this.setLeftFork(leftFork);
 		this.seatNumber = number;
 	}
 	
 	public RemoteObjectType getObjectType() throws RemoteException {
 		return RemoteObjectType.SEAT;
 	}
 	
 	/*
 	public static ArrayList<Integer> buildTable(final int size) {
 		if (size < 3) {
 			System.err.println("A Table should have at least 3 seats. Trouble ahead.");
 			return null;
 		}
 		ArrayList<Integer> table = new ArrayList<Integer>(size);
 		
 		
 		Seat previousSeat = new Seat(new Fork(), new Fork(), 0);
 		table.add(previousSeat);
 		
 		for (int i = 0; i < size-1; i++) {
 			Seat newSeat = new Seat(previousSeat.rightFork, new Fork(), i+1);
 			
 			table.add(newSeat);
 			
 			previousSeat = newSeat;
 		}
 		
 		assert(table.size() == size);
 		
 		table.get(0).setLeftFork(table.get(size-1).rightFork);
 		
 		return table;
 	}
 */
 	public boolean canBeTaken() throws RemoteException {
 		return this.getRightFork().isFree() && this.getLeftFork().isFree();
 	}
 	
 	public boolean isTaken() {
 		return this.philID != -1;
 	}
 
 	public boolean setPhil(final RemotePhilosopherIF phil) throws RemoteException {
 		boolean success;
 		
 		/**
 		 * Change order of locks for one philosopher because of... avoid deadlock problem
 		 */
 		AbstractRemoteObjectIF firstLockObject = (phil != null && phil.getPhilNumber() == 1) ? this.getLeftFork() : this.getRightFork();
 		AbstractRemoteObjectIF secondLockObject = (phil != null && phil.getPhilNumber() == 1) ? this.getRightFork() : this.getLeftFork();
 		
 		if(firstLockObject.tryLock()) {
 			if (secondLockObject.tryLock()) {
 				if (phil == null || this.philID == -1 && this.canBeTaken()) { //1st case: reset. 2nd case: this seat is not occupied and will be taken from the new philosopher
 					this.philID = phil.getObjectID();
 					success = true;
 				}
 				else { //this seat is already occupied. (prevent race condition)
 					success = false;
 				}
 				
 				/**
 				 * Check if this seat is empty now and if we have a philosopher waiting for this seat.
 				 * If true, we put the next waiting phil on the queue
 				 */
 				if (this.philID == -1 && this.canBeTaken() && !waitingQueue.isEmpty()) {
 					RemotePhilosopherIF nextPhil = this.getNextPhilosopherWaiting();
 					this.philID = nextPhil.getObjectID();
 					nextPhil.setWaitingForSeat(false);
 					
 					System.out.println("Seat " + this.seatNumber + " became free. Will seat the next queued philosopher (" + nextPhil.getPhilNumber() + ") now.");
 				}
 				secondLockObject.unlock();
 				firstLockObject.unlock();
 			} 
 			else {
 				firstLockObject.unlock();
 				//success is false here because we could not acquire the lock
 				success = false;
 			}
 		} else {
 			//success is false here because we could not acquire the lock
 			success = false;
 		}
 		
 		return success;
 	}
 	
 	/**
 	 * Put a philosopher on the waiting queue. Makes only sense if this seat is currently occupied
 	 * @param phil
 	 * @return
 	 * @throws RemoteException 
 	 */
 	public boolean queuePhil(final RemotePhilosopherIF phil) throws RemoteException {
 		boolean success;
 		
 		phil.setWaitingForSeat(true);
 		success = waitingQueue.offer(phil.getObjectID());
 		
 		return success;
 	}
 	
 	public int getNumberOfPhilosophersWaiting() {
 		return ((ArrayBlockingQueue<Integer>) waitingQueue).size();
 	}
 	
 	public boolean isPhilosopherWaitingQueueFull() {
 		return ((ArrayBlockingQueue<Integer>) waitingQueue).remainingCapacity() < 1;
 	}
 	
 	public RemotePhilosopherIF getNextPhilosopherWaiting() {
 		Integer nextPhilID = waitingQueue.poll();
 		if (nextPhilID != null) 
 			return (RemotePhilosopherIF) PhilWorker.getPhilWorkerInstance().getObjectWithID(nextPhilID);
 		else
 			return null;
 	}
 
 	public RemoteForkIF getRightFork() {
 		if (this.rightForkID != -1)
 			return (RemoteForkIF) PhilWorker.getPhilWorkerInstance().getObjectWithID(this.rightForkID);
 		else
 			return null;
 	}
 	
 	public RemoteForkIF getLeftFork() {
 		if (this.leftForkID != -1)
 			return (RemoteForkIF) PhilWorker.getPhilWorkerInstance().getObjectWithID(this.leftForkID);
 		else
 			return null;
 	}
 	
 	public int getRightForkID() {
		return this.rightForkID;
 	}
 	
 	public int getLeftForkID() {
		return this.leftForkID;
 	}
 	
 	public void setLeftForkID(final int objID) {
 		this.leftForkID = objID;
 	}
 	
 	public void setRightForkID(final int objID) {
 		this.rightForkID = objID;
 	}
 
 	public void setRightFork(RemoteForkIF rightFork) throws RemoteException {
 		if (this.rightForkID != -1)
 			this.getRightFork().setLeftSeatSimple(null);
 		
 		rightFork.setLeftSeatSimple(this);
 		this.setRightForkSimple(rightFork);
 	}
 	
 	public void setLeftFork(RemoteForkIF leftFork) throws RemoteException {
 		if (this.leftForkID != -1) {
 			RemoteForkIF currentLeftFork = null;
 			currentLeftFork = this.getLeftFork();
 			currentLeftFork.setRightSeatSimple(null);
 		}
 		
 		leftFork.setRightSeatSimple(this);
 		this.setLeftForkSimple(leftFork);
 	}
 
 	public void setRightForkSimple(RemoteForkIF rightFork) throws RemoteException {
 		if (rightFork != null) {
 			this.rightForkID = rightFork.getObjectID();
 		}
 		else
 			this.rightForkID = -1;
 		
 	}
 	
 	public void setLeftForkSimple(RemoteForkIF leftFork) throws RemoteException {
 		if (leftFork != null) {
 			this.leftForkID = leftFork.getObjectID();
 		}
 		else
 			this.leftForkID = -1;
 	}
 	
 
 	public RemotePhilosopherIF getPhil() {
 		if (this.philID != -1)
 			return (RemotePhilosopherIF) PhilWorker.getPhilWorkerInstance().getObjectWithID(this.philID);
 		else
 			return null;
 	}
 	
 	public RemoteSeatIF righthandSeat() throws RemoteException {
 		AbstractRemoteObjectIF obj = null;
 		obj = this.getRightFork();
 		obj = ((RemoteForkIF)obj).getRightSeat();
 		return (RemoteSeatIF)obj;
 	}
 	
 	public RemoteSeatIF lefthandSeat() throws RemoteException {
 		AbstractRemoteObjectIF obj = null;
 		obj = this.getLeftFork();
 		obj = ((RemoteForkIF)obj).getLeftSeat();
 		return (RemoteSeatIF)obj;
 	}
 
 	public int getSeatNumber() {
 		return seatNumber;
 	}
 
 	public void setSeatNumber(int seatNumber) {
 		this.seatNumber = seatNumber;
 	}
 
 	@Override
 	public String toString() {
 		return "Seat [seatNumber=" + seatNumber + "]";
 	}
 }
