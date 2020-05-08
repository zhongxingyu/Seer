 package system;
 
 import java.util.Vector;
 
 import knowledge.KnowledgeBase;
 import messages.Message;
 import messages.Message.MessageType;
 import messages.payload.Bid;
 import messages.payload.Payload;
 import messages.payload.Solicitation;
 import messages.payload.TaskSpec;
 import structures.HostKnowledge;
 import structures.ScheduleEntry;
 import structures.Task;
 import util.Common;
 import util.Logger;
 
 import comm.CommunicationFactory;
 
 public class MarketPlaceDispatcher extends CiANDispatcher {
 	
 	private Vector<Payload> controls;
 	
 	private Logger log;
 	
 	public MarketPlaceDispatcher() {
 		controls = new Vector<Payload>();
 		log = Logger.getLogger();
 		new Thread(new ControlProcessor(this), "CiANDispatcherControlProcessor").start();
 	}
 
 	@Override
 	public void incomingControl(Payload p) {
 		synchronized(controls) {
 			controls.add(p);
			controls.notifyAll();
 		}
 	}
 	
 	private class ControlProcessor implements Runnable {
 		private CiANDispatcher thisDispatcher;
 		
 		public ControlProcessor(CiANDispatcher thisDispatcher) {
 			this.thisDispatcher = thisDispatcher;
 		}
 		
 		public void run() {
 			Payload p;
 			while(true) {
 				synchronized(controls) {
 					while(controls.isEmpty()) {
 						try {
 							controls.wait();
 						} catch (InterruptedException e) {
 							log.exception(e);
 						}
 					}
 					p = controls.remove(0);
 				}
 				switch(p.getPayloadType()){
 				case CTRL_SOLIC:
 					Solicitation solicitation = (Solicitation)p;
 					Task task = solicitation.getTask();
 					HostKnowledge self = KnowledgeBase.getInstance().getSelf();
 
 					if(self.getServiceList().contains(task.getActivity().getService().getService()) && 
 							Common.noScheduleConflicts(task, self.getSchedule()) &&
 							CiAN.getUI().std_acceptSolicitation(task)) {
 						//TODO Velocities
 						double capFrac = 1d/self.getServiceList().size();
 
 						ScheduleEntry obligation = self.getSchedule().firstElement();
 						/*
 						 * Coordinator computes min(deadline, task start) so a host with
 						 * nothing better to do has an infinite deadline.
 						 */
 						long deadline = Long.MAX_VALUE;
 						if(obligation != null) {
 							double dist = Math.sqrt(
 									(self.getCurrentLocX() - obligation.getStartLocX())^2 + 
 									(self.getCurrentLocY() - obligation.getStartLocY())^2);
 							//assume that velocities stored in dist/sec
 							//TODO extract this calculation to allow different units?
 							double tElapse = dist / self.getMaxVelocity();
 							deadline = obligation.getStartTime() - (long)(1000*tElapse);
 						}
 						Bid b = new Bid(task.getName(),capFrac,0,0,self.getMaxVelocity(),deadline);
 						//send bid
 						CommunicationFactory.getCommModule().unicast(solicitation.getCoordinatorName(), solicitation.getCoordinatorIP(),
 								new Message(MessageType.CONTROL, b), true);
 					}
 					break;
 				case CTRL_TASK_SPEC:
 					TaskSpec t = (TaskSpec) p;
 					if(t.getTask().getAllocationInfo().getName().equals(CiAN.getHostname())) {
 						CiAN.getUI().std_notifyTaskAssigned(t.getTask());
 						CiANSvcMgr manager = new CiANSvcMgr(t.getTask(), t.getAllRouteInfo(), thisDispatcher);
 						router.addLocalTask(t.getTask().getName());
 						managers.put(t.getTask().getName(), manager);
 						manager.start();
 					}
 					break;
 				}
 			}
 		}
 	}
 }
