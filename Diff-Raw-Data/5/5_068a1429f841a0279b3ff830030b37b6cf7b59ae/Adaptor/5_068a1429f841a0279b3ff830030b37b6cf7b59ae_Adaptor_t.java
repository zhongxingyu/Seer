 package loadbalance;
 
 import policy.SenderInitTransferPolicy;
 import policy.TransferPolicy;
 import jobs.Job;
 import jobs.TransferManager;
 import jobs.WorkerThread;
 import state.HardwareMonitor;
 import state.State;
 import state.StateManager;
 
 
 public class Adaptor {
 	State state, remoteState;
 	StateManager stateManager;
 	TransferManager transferManager;
 	TransferChecker transferChecker;
 	HardwareMonitor hardwareMonitor;
 	WorkerThread workerThread;
 	final int THRESHOLD = 3;
 	final int POLL_LIM = 1;
 	
 	public Adaptor(){
 		stateManager = new StateManager();
 		transferManager = new TransferManager();
 		transferChecker = new TransferChecker();
 		// hardwareMonitor = new HardwareManager();
 		workerThread = new WorkerThread();
 		workerThread.start();
 	}
 	
 	public class TransferChecker extends Thread {
 		private int SLEEP_TIME;
 		
 		public TransferChecker(){
 			this(1000);
 		}
 		
 		public TransferChecker(int sleep_time){
 			this.SLEEP_TIME = sleep_time;
 			start();
 		}
 		
		public void checkForAvailableTransfer(){
 			TransferPolicy transferPolicy = (new SenderInitTransferPolicy(workerThread.getJobQueueSize()));
 			if(transferPolicy.isTransferable()){
 				
 				if(remoteState.job_queue_length < THRESHOLD){
 					Job job = workerThread.getJobQueue().pop();
 					if(job != null)
 						transferManager.sendJob(job);
 				}
 				//if(node != null)
 				//transferManager.sendJob(job);
 			}
 		}
 		
 		@Override
 		public void run() {
 			while(true){
				this.checkForAvailableTransfer();
 				try {
 					sleep(this.SLEEP_TIME);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }
