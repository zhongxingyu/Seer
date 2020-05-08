 package jadex.agent;
 
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import robot.NavRobot;
 
 import data.Board;
 import data.BoardObject;
 import data.Goal;
 import data.Host;
 import data.Position;
 import device.Device;
 import device.DeviceNode;
 import device.external.IDevice;
 import device.external.IGripperListener;
 import device.external.IPlannerListener;
 import jadex.bridge.*;
 import jadex.commons.ChangeEvent;
 import jadex.commons.IChangeListener;
 
 import jadex.micro.MicroAgentMetaInfo;
 import jadex.service.GoalReachedService;
 import jadex.service.HelloService;
 import jadex.service.ReceiveNewGoalService;
 import jadex.service.SendPositionService;
 
 public class CollectAgent extends NavAgent
 {
     /** Data */
     Board bb;
     String curGoalKey = null;
     /** Where to store objects */
     Position depotPose;
     boolean permitGripperOpen = false;
     CollectAgent agent;
 
 	@Override public void agentCreated()
 	{
 		agent = this;
 	    hs = new HelloService(getExternalAccess());
         ps = new SendPositionService(getExternalAccess());
         gs = new ReceiveNewGoalService(getExternalAccess());
         gr = new GoalReachedService(getExternalAccess());
 
         addDirectService(hs);
         addDirectService(ps);
         addDirectService(gs);
         addDirectService(gr);
 
         String host = (String)getArgument("host");
         Integer port = (Integer)getArgument("port");
         Integer robotIdx = (Integer)getArgument("index");
         Boolean hasLaser = (Boolean)getArgument("laser");
         Boolean hasSimu = (Boolean)getArgument("simulation");
         Integer devIdx = (Integer)getArgument("devIndex");
 
         /** Device list */
         CopyOnWriteArrayList<Device> devList = new CopyOnWriteArrayList<Device>();
         devList.add( new Device(IDevice.DEVICE_POSITION2D_CODE,host,port,devIdx) ); // TODO why playerclient blocks if not present?
         if (hasSimu == true)
             devList.add( new Device(IDevice.DEVICE_SIMULATION_CODE,null,-1,-1) );
         
         devList.add( new Device(IDevice.DEVICE_PLANNER_CODE,host,port+1,devIdx) );
         devList.add( new Device(IDevice.DEVICE_LOCALIZE_CODE,host,port+1,devIdx) );
         devList.add( new Device(IDevice.DEVICE_GRIPPER_CODE,host,port,devIdx) );
         devList.add( new Device(IDevice.DEVICE_ACTARRAY_CODE,host,port,devIdx) );
         devList.add( new Device(IDevice.DEVICE_DIO_CODE,host,port,devIdx) );
 
         if (hasLaser == true)
             devList.add( new Device(IDevice.DEVICE_RANGER_CODE,host,port,-1));
 
         /** Host list */
         CopyOnWriteArrayList<Host> hostList = new CopyOnWriteArrayList<Host>();
         hostList.add(new Host(host,port));
         hostList.add(new Host(host,port+1));
         if (port != 6665)
             hostList.add(new Host(host,6665));
         
         /** Get the device node */
         setDeviceNode( new DeviceNode(hostList.toArray(new Host[hostList.size()]), devList.toArray(new Device[devList.size()])));
         deviceNode.runThreaded();
         
         if (deviceNode.getDevice(new Device(IDevice.DEVICE_GRIPPER_CODE,null,-1,-1)) == null)
             throw new IllegalStateException("No gripper device found");
 
         robot = new NavRobot(deviceNode.getDeviceListArray());
         getRobot().setRobotId("r"+robotIdx);
         
         /**
          *  Check if a particular position is set
          */
         Position setPose = new Position(
                 (Double)getArgument("X"),
                 (Double)getArgument("Y"),
                 (Double)getArgument("Angle"));
         
         if ( setPose.equals(new Position(0,0,0)) == false )
             getRobot().setPosition(setPose);         
 
         sendHello();
 		
 		bb = new Board();
 	}
 	
 	@Override public void executeBody()
 	{
 	    super.executeBody();
 
 	    /**
 	     * Register new goal event callback
 	     */
         scheduleStep(new IComponentStep()
         {
             public Object execute(IInternalAccess ia)
             {
                 getReceiveNewGoalService().addChangeListener(new IChangeListener()
                 {
                     public void changeOccurred(ChangeEvent event)
                     {
                         Object[] content = (Object[])event.getValue();
                         
                         String id = (String) content[1];
                         Position newGoal = (Position) content[2];
                         
                         // TODO compare to objects in board
                         if (id.equals("collectGoal") == true)
                         {
                             //TODO check goal radius here
                             String goalKey = ""+newGoal;
                             if (bb.getObject(goalKey) == null)
                             {
                                 /** Create a new board object. */
                                 BoardObject newBo = new BoardObject();
                                 newBo.setTopic(id);
                                 newBo.setPosition(newGoal);
 //                                newBo.setTimeout(10000);
                                 Goal boGoal = new Goal();
                                 /** Goal pose is this agent's depot. */
                                 boGoal.setPosition(depotPose);
                                 newBo.setGoal(boGoal);
                                 bb.addObject(goalKey, newBo);
                                 logger.fine("Added goal "+newGoal);
                                 /** Update this agent's goal */
                                 updateGoal(bb);
                             }
                         }
                     }
                 });
                 return null;
             }
         });
                 
         /**
          *  Register planner callback
          */
         scheduleStep(new IComponentStep()
         {
             public Object execute(IInternalAccess ia)
             {
                 if (getRobot().getPlanner() != null) /** Does it have a planner? */
                 {
                     getRobot().getPlanner().addIsDoneListener(new IPlannerListener()
                     {
                         @Override public void callWhenIsDone()
                         {
                             if (curGoalKey != null)
                             {
                                 if (bb.getObject(curGoalKey).isDone() == false)
                                 {
                                     /** Arrived at the object's position */
                                 	logger.fine("Start lift with object");
 
                                 	getRobot().getGripper().closeLift(new IGripperListener()
                                 	{
 										@Override public void whenOpened() {
 											getRobot().getGripper().removeIsDoneListener(this);											
 										}
 										@Override public void whenClosed() {
 											getRobot().getGripper().removeIsDoneListener(this);											
 										}
 										@Override public void whenLifted() {
 											getRobot().getGripper().removeIsDoneListener(this);											
 										}
 										@Override public void whenReleased() {
 											getRobot().getGripper().removeIsDoneListener(this);											
 										}
 										@Override public void whenClosedLifted() {
 											getRobot().getGripper().removeIsDoneListener(this);
 											logger.fine("Update goal");
 											bb.getObject(curGoalKey).setDone(true);
 		                                    updateGoal(bb);
 										}
 										@Override public void whenReleasedOpened() {
 											getRobot().getGripper().removeIsDoneListener(this);											
 										}
 										@Override public void whenError() {
 											getRobot().getGripper().removeIsDoneListener(this);
 										}                                		
                                 	});
                                 }
                                 else
                                 {
                                     /** Arrived at the objects depot position */
                                     getRobot().getGripper().releaseOpen(new IGripperListener()
                                     {                                    	
                                     	@Override public void whenOpened() {
 											getRobot().getGripper().removeIsDoneListener(this);											
 										}
 										@Override public void whenClosed() {
 											getRobot().getGripper().removeIsDoneListener(this);											
 										}
 										@Override public void whenLifted() {
 											getRobot().getGripper().removeIsDoneListener(this);											
 										}
 										@Override public void whenReleased() {
 											getRobot().getGripper().removeIsDoneListener(this);											
 										}
 										@Override public void whenClosedLifted() {
 											getRobot().getGripper().removeIsDoneListener(this);
 										}
 										@Override public void whenReleasedOpened() {
 											getRobot().getGripper().removeIsDoneListener(this);											
 											bb.removeObject(curGoalKey);
 		                                    curGoalKey = null;
 		                                    updateGoal(bb);
 										}
 										@Override public void whenError() {
 											getRobot().getGripper().removeIsDoneListener(this);
 										}                                		
                                     });
                                 }
                             }
                         }
 
                         @Override public void callWhenAbort()
                         {
                             updateGoal(bb);
                             logger.info("Path aborted");
                         }
 
                         @Override public void callWhenNotValid()
                         {
                             if (curGoalKey != null)
                             {
                                 if (bb.getObject(curGoalKey).isDone() == false)
                                 {
                                     /** We are heading for the object. */
                                     /** Forget it and update the plan */
                                     bb.getObject(curGoalKey).setDone(true);
                                     curGoalKey = null;
                                     updateGoal(bb);
                                 }
                                 else
                                 {
                                     /** We are heading home. */
                                     /** Deposit any object here */
                                 	 getRobot().getGripper().releaseOpen(new IGripperListener()
                                      {                                    	
                                      	@Override public void whenOpened() {
  											getRobot().getGripper().removeIsDoneListener(this);											
  										}
  										@Override public void whenClosed() {
  											getRobot().getGripper().removeIsDoneListener(this);											
  										}
  										@Override public void whenLifted() {
  											getRobot().getGripper().removeIsDoneListener(this);											
  										}
  										@Override public void whenReleased() {
  											getRobot().getGripper().removeIsDoneListener(this);											
  										}
  										@Override public void whenClosedLifted() {
  											getRobot().getGripper().removeIsDoneListener(this);
  										}
  										@Override public void whenReleasedOpened() {
  											getRobot().getGripper().removeIsDoneListener(this);											
  		                                    /** Forget it and update the plan */
  		                                    curGoalKey = null;
  		                                    updateGoal(bb);
  										}
  										@Override public void whenError() {
  											getRobot().getGripper().removeIsDoneListener(this);
  										}                                		
                                      });
                                 }
                             }
                             logger.info("No valid path");
                         }
                     });
                 }
                 return null;
             }
         });        
         /**
          *  Check if near to goal
          */
         final IComponentStep step = new IComponentStep()
         {
             public Object execute(IInternalAccess ia)
             {
                 Position curPose = getRobot().getPosition();
                 Position goalPose = getRobot().getGoal();
 
                 double goalDist = curPose.distanceTo(goalPose);
 
                 if (goalDist < 2.0)
                 {
                     /** Prepare the paddles */
                     if (permitGripperOpen == true)
                     {
                         permitGripperOpen = false;
                         
                         getRobot().getPlanner().stop();
                         getRobot().getGripper().releaseOpen(new IGripperListener()
                         {                                    	
                         	@Override public void whenOpened() {
 								getRobot().getGripper().removeIsDoneListener(this);											
 							}
 							@Override public void whenClosed() {
 								getRobot().getGripper().removeIsDoneListener(this);											
 							}
 							@Override public void whenLifted() {
 								getRobot().getGripper().removeIsDoneListener(this);											
 							}
 							@Override public void whenReleased() {
 								getRobot().getGripper().removeIsDoneListener(this);											
 							}
 							@Override public void whenClosedLifted() {
 								getRobot().getGripper().removeIsDoneListener(this);
 							}
 							@Override public void whenReleasedOpened() {
 								getRobot().getGripper().removeIsDoneListener(this);											
 								double angle = getRobot().getPosition().getYaw(); 
 		                        /** Set the approach angle appropriate*/
 		                        getRobot().setGoal(new Position(
 		                                getRobot().getGoal().getX(),
 		                                getRobot().getGoal().getY(),
 		                                angle));
 
 		                        logger.fine("Updated angle: "+Math.toDegrees(angle));
 							}
 							@Override public void whenError() {
 								getRobot().getGripper().removeIsDoneListener(this);
 							}                                		
                         });                        
                     }
                 }
                 waitFor(1000,this);
                 return null;
             }
         };        
         waitForTick(step);
         
         /** Depot Pose */
        waitFor(1000, new IComponentStep()
         {
 			@Override public Object execute(IInternalAccess ia) {
 		        /** Set current depot pose */
 		        depotPose = new Position(getRobot().getPosition());
 		        logger.fine("Updated depot pose to "+depotPose);
 				return null;
 			}        	
         });
 	}
 	
 	@Override public void agentKilled()
 	{
 		getRobot().getGripper().closeLift(new IGripperListener()
         {                                    	
         	@Override public void whenOpened() {
 				getRobot().getGripper().removeIsDoneListener(this);											
 			}
 			@Override public void whenClosed() {
 				getRobot().getGripper().removeIsDoneListener(this);											
 			}
 			@Override public void whenLifted() {
 				getRobot().getGripper().removeIsDoneListener(this);											
 			}
 			@Override public void whenReleased() {
 				getRobot().getGripper().removeIsDoneListener(this);											
 			}
 			@Override public void whenClosedLifted() {
 				getRobot().getGripper().removeIsDoneListener(this);
 				agent.killNow();				    
 			}
 			@Override public void whenReleasedOpened() {
 				getRobot().getGripper().removeIsDoneListener(this);											
 			}
 			@Override public void whenError() {
 				getRobot().getGripper().removeIsDoneListener(this);
 			}                                		
         });
 		//TODO Wait for agent to be killed
 //		killNow();
 	}
 	public void killNow()
 	{
 		super.agentKilled();
 		bb.clear();
 	}
 	/**
 	 * Updates the current goal of the agent.
 	 * @param bb The board containing all goals.
 	 */
 	void updateGoal(Board bb)
 	{
 	    if (curGoalKey == null)
 	    {
 	        /** Set a new goal. */
 	        curGoalKey = getNextGoal(bb);
 	        if (curGoalKey != null)
 	        {
 	            /** Prepare Gripper for driving */
 	            getRobot().getGripper().closeLift(new IGripperListener()
             	{
 					@Override public void whenOpened() {
 						getRobot().getGripper().removeIsDoneListener(this);											
 					}
 					@Override public void whenClosed() {
 						getRobot().getGripper().removeIsDoneListener(this);											
 					}
 					@Override public void whenLifted() {
 						getRobot().getGripper().removeIsDoneListener(this);											
 					}
 					@Override public void whenReleased() {
 						getRobot().getGripper().removeIsDoneListener(this);											
 					}
 					@Override public void whenClosedLifted() {
 						getRobot().getGripper().removeIsDoneListener(this);
 						Position goal = agent.getBb().getObject(curGoalKey).getPosition();
 			            getRobot().setGoal(goal);
 			            permitGripperOpen = true;
 			            logger.info("Set collect goal "+goal);
 					}
 					@Override public void whenReleasedOpened() {
 						getRobot().getGripper().removeIsDoneListener(this);											
 					}
 					@Override public void whenError() {
 						getRobot().getGripper().removeIsDoneListener(this);
 					}                                		
             	});
 	        }
 	    }
 	    else
 	    {
 	        if (bb.getObject(curGoalKey).isDone() == true)
 	        {
 	            /** We are driving to the depot */
 	            /** Where should it be delivered? */
 	        	Position goal = bb.getObject(curGoalKey).getGoal().getPosition();
 	            getRobot().setGoal(goal);
 	            permitGripperOpen = false;
 	            logger.info("Heading to depot "+goal);
 	        }
 	        else
 	        {
 	            /** Aborted? */
 	            /** Set the goal again. */
 	        	Position goal = bb.getObject(curGoalKey).getPosition();
 	            getRobot().setGoal(goal);
 	            permitGripperOpen = true;
 	            logger.info("Set collect goal again "+goal);
 	        }
 	    }
 	}
 	/**
 	 * Returns the key of the first found unfinished goal.
 	 * @param bb The board to search in.
 	 * @return null if no unfinished goal is found, the key else.
 	 */
 	String getNextGoal(Board bb)
 	{
 	    String newGoalKey = null;
 	    
 	    Iterator<Entry<String, BoardObject>> it = bb.getIterator();
 	    
         /** Search for unfinished goals. */
 	    while (it.hasNext())
 	    {
 	        Entry<String, BoardObject> e = it.next();
 	        if (e.getValue().isDone() == false)
 	        {
 	            newGoalKey = e.getKey();
 	            break;
 	        }
 	    }
 	    return newGoalKey;
 	}
 	
 	public static MicroAgentMetaInfo getMetaInfo()
 	{
 		IArgument[] args = {
                 new Argument("host", "Player", "String", "localhost"),
 				new Argument("port", "Player", "Integer", new Integer(6665)),
                 new Argument("index", "Robot index", "Integer", new Integer(0)),
                 new Argument("devIndex", "Device index", "Integer", new Integer(0)),
 				new Argument("X", "Meter", "Double", new Double(0.0)),
 				new Argument("Y", "Meter", "Double", new Double(0.0)),
 				new Argument("Angle", "Degree", "Double", new Double(0.0)),
                 new Argument("laser", "Laser ranger", "Boolean", new Boolean(true)),
                 new Argument("simulation", "Simulation device", "Boolean", new Boolean(true))
 		};
 		
 		return new MicroAgentMetaInfo("This agent starts up a collect agent.", null, args, null);
 	}
 
 	/**
 	 * @return the bb
 	 */
 	protected Board getBb() {
 		return bb;
 	}
 }
