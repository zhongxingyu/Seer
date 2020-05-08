 package ut.distcomp.playlist;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import ut.distcomp.playlist.TransactionState.STATE;
 
 public class RecoveryCoordinatorTransaction extends Transaction {
 
 	boolean isRecoveryComplete = false;
 	STATE otherState;
 	
 	boolean abortFlag = false;
 	boolean commitFlag = false;
 	HashSet<Integer> committableSet = new HashSet<Integer>(); 
 	HashSet<Integer> uncertainSet = new HashSet<Integer>(); 
 	
 	HashSet<Integer> ackSet = new HashSet<Integer>();
 	
 	public RecoveryCoordinatorTransaction(Process process, Message message, STATE state) {
 		super(process, message);
 		this.state = state;
 		this.otherState = STATE.RESTING; 
 		
 		this.DECISION_TIMEOUT = process.delay + 2000;
 	}
 	
 	public void run() {
 		lock.lock();
 		
 		while (!isRecoveryComplete) {
 			if(otherState == STATE.RESTING) {
 				otherState = STATE.WAIT_DECISION;
 				
 				// We have reached here, means we have to send STATE-REQS to all the active machines.
 				Message msg = new Message(process.processId, MessageType.STATE_REQ, command);
 				Process.waitTillDelay();
 				process.config.logger.info("Sending: " + msg);
 				process.controller.sendMsgs(process.upProcess.keySet(), msg.toString(), -1);
 				
 				Thread th = new Thread() {
 					public void run() {
 						try {
 							Thread.sleep(DECISION_TIMEOUT);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						lock.lock();
 						otherState = STATE.DECISION_RECEIVED;
 						if (state == STATE.COMMIT) {
 							commitFlag = true;
 						}
 						if (state == STATE.ABORT) {
 							abortFlag = true;
 						}
 						nextMessageArrived.signal();
 						lock.unlock();
 					}
 				};
 				th.start();
 			} // END OF MESSAGE SENDING PHASE.
 			else if(otherState == STATE.WAIT_DECISION) {
 				if (message.type == MessageType.STATE_VALUE) {
 					process.config.logger.info("Recieved: " + message.toString());
 					if (message.payLoad.equals(STATE.COMMIT.toString())) {
 						commitFlag = true;
 					} else if (message.payLoad.equals(STATE.ABORT.toString())) {
 						abortFlag = true;
 					} else if (message.payLoad.equals(STATE.COMMITABLE.toString())) {
 						committableSet.add(message.process_id);
 					} else if (message.payLoad.equals(STATE.UNCERTAIN.toString())) {
 						uncertainSet.add(message.process_id);
 					} else {
 						process.config.logger.warning("Unexpected State = " + message.payLoad 
 								+ " received from: " + message.process_id);
 						break;
 					}
 				} else {
 					process.config.logger.warning("Was expecting a STATE value and got: " + message.toString());
 					break;
 				}
 			} else if (otherState == STATE.DECISION_RECEIVED) {
 				if (commitFlag && abortFlag) {
 					process.config.logger.warning("ALERT !! SOMETHING IS WRONG. 3-PC has failed.");
 					break;
 				}
 				if (commitFlag) {
 					process.config.logger.info("Some process has already committed. Let us all commit.");
 					if (state != STATE.COMMIT) {
 						process.dtLogger.write(STATE.COMMIT);
 						state = STATE.COMMIT;
 					}
 					Process.waitTillDelay();
 					isRecoveryComplete = true;
 					Message msg = new Message(process.processId, MessageType.COMMIT, command);
 					process.config.logger.info("Sending COMMIT to all the active processes.");					
 					
 					int partial_count = -1;
 					if (!System.getProperty("PartialCommit").equals("-1")) {
 						partial_count = Integer.parseInt(System.getProperty("PartialCommit"));
 					}
 					process.controller.sendMsgs(process.upProcess.keySet(), msg.toString(), partial_count);
 					break;
 				} else if (abortFlag) {
 					process.config.logger.info("Some process has already aborted. Let us all abort.");
 					if (state != STATE.ABORT) {
 						process.dtLogger.write(STATE.ABORT);
 						state = STATE.ABORT;
 					}
 					Process.waitTillDelay();
 					isRecoveryComplete = true;
 					Message msg = new Message(process.processId, MessageType.ABORT, command);
 					process.config.logger.info("Sending ABORT to all the active processes.");
 					process.controller.sendMsgs(process.upProcess.keySet(), msg.toString(), -1);
 					break;
 				} else if (committableSet.size() == 0) {
 					process.config.logger.info("All the process are uncertain. Let us all abort.");
 					Process.waitTillDelay();
 					process.dtLogger.write(STATE.ABORT);
 					state = STATE.ABORT;
 					isRecoveryComplete = true;
 					Message msg = new Message(process.processId, MessageType.ABORT, command);
 					process.config.logger.info("Sending ABORT to all the active processes.");
 					process.controller.sendMsgs(process.upProcess.keySet(), msg.toString(), -1);
 					break;
 				} else {
 					otherState = STATE.WAIT_ACK; 
					process.config.logger.info("Some process may be uncertain and some are committable.");
 					Process.waitTillDelay();
 					state = STATE.COMMITABLE;
 					Message msg = new Message(process.processId, MessageType.PRE_COMMIT, command);
 					process.config.logger.info("Sending PRE_COMMIT to uncertain processes.");
 							
 					int partial_count = -1;
 					if (!System.getProperty("PartialCommit").equals("-1")) {
 						partial_count = Integer.parseInt(System.getProperty("PartialCommit"));
 					}
 					process.controller.sendMsgs(uncertainSet, msg.toString(), partial_count);
 					
 					Thread th = new Thread() {
 						public void run() {
 							try {
 								Thread.sleep(DECISION_TIMEOUT);
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 							lock.lock();
 							otherState = STATE.ACK_RECEIVED;
 							nextMessageArrived.signal();
 							lock.unlock();
 						}
 					};
					th.start();
 				}
 			} else if (otherState == STATE.WAIT_ACK) {
 				if (message.type == MessageType.ACK) {
 					process.config.logger.info("Received: " + message.toString());
 					ackSet.add(message.process_id);
 				}
 				else {
 					process.config.logger.warning("Was expecting a ACK and got: " + message.toString());
 					break;
 				}
			} else if (otherState == STATE.ACK_RECEIVED) {
 				process.config.logger.info("Let us COMMIT !!");
 				process.dtLogger.write(STATE.COMMIT);
 				state = STATE.COMMIT;
 				isRecoveryComplete = true;
 				Message msg = new Message(process.processId, MessageType.COMMIT, command);
 				Process.waitTillDelay();
 				process.config.logger.info("Sending COMMIT to all the active processes.");
 				
 				int partial_count = -1;
 				if (!System.getProperty("PartialCommit").equals("-1")) {
 					partial_count = Integer.parseInt(System.getProperty("PartialCommit"));
 				}
 				
 				process.controller.sendMsgs(process.upProcess.keySet(), msg.toString(), partial_count);
 				break;
 			}
 			try {
 				// Wait until some other message is arrived.
 				nextMessageArrived.await();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		lock.unlock();
 	}
 	
 	public void update(Message message) {
 		lock.lock();
 		
 		this.message = message;
 		nextMessageArrived.signal();
 		
 		lock.unlock();
 	}
 }
