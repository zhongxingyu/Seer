 package robot;
 
 import java.util.ArrayList;
 
 import data.Position;
 import de.unihamburg.informatik.tams.project.communication.Barrel;
 import de.unihamburg.informatik.tams.project.communication.BarrelColor;
 import de.unihamburg.informatik.tams.project.communication.MapPosition;
 import de.unihamburg.informatik.tams.project.communication.State;
 import de.unihamburg.informatik.tams.project.communication.exploration.Exploration;
 import de.unihamburg.informatik.tams.project.communication.exploration.Exploration.RobotState;
 import device.Device;
 import device.Planner;
 import device.external.IGripperListener;
 import device.external.IPlannerListener;
 
 public abstract class PatrolRobot extends NavRobot implements Exploration {
 
 	protected MapPosition position;
 
 	protected RobotState state;
 
 	protected RobotState plannerState;
 
 	protected RobotState gripperState;
 
 	protected Planner planner;
 	
 	protected ArrayList<double[]> barrelPositions;
 	
 	protected ArrayList<Barrel> knownBarrels;
 
 	protected data.Position ownPosition = this.getPosition();
 
 	public PatrolRobot(Device[] devList) {
 		super(devList);
 	}
 
 	public boolean hasGripper() {
 		return super.getGripper() != null;
 	}
 
 	public State getState() {
 		State result = null;
 		if (state == RobotState.NEEDS_NEW_GOAL
 				|| state == RobotState.ON_THE_WAY) {
 			result = State.EXPLORING;
 		} else if (state == RobotState.TRANSPORTING_BARREL) {
 			result = State.TRANSPORTING;
 		}
 		return result;
 	}
 
 	public void setState(RobotState state) {
 		this.state = state;
 	}
 
 	public MapPosition getMapPosition() {
 		return position;
 	}
 	
 	/*
 	 * Auf bestimmten gripperState warten
 	 */
 	public void waitForGripperState(RobotState rs) {
 		while (gripperState != rs) {
 			try {
 				Thread.sleep(500); // 500ms warten bis planerState erneut abgefragt wird
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/*
 	 * Auf bestimmten plannerState warten
 	 */
 	public void waitForPlannerState(RobotState rs) {
 		while (plannerState != rs) {
 			try {
 				Thread.sleep(500); // 500ms warten bis gripperState erneut abgefragt wird
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void transportBarrelTo(Barrel barrel, MapPosition targetPositionOfBarrel) {
 		// state setzen
 		state = RobotState.TRANSPORTING_BARREL;
 		MapPosition currentPositionOfBarrel = barrel.getPosition();
 		
 		// Position der Barrel und des Ziels erzeugen
 		Position barrelPos = new Position(currentPositionOfBarrel.getxPosition(), currentPositionOfBarrel.getyPosition(), 0);
 		Position targetPos = new Position(targetPositionOfBarrel.getxPosition(), targetPositionOfBarrel.getyPosition(), 0);
 
 		// IGripperListener für den Gripper
 		IGripperListener gl = new IGripperListener() {
 			@Override
 			public void whenOpened() {
 				gripperState = RobotState.GRIPPER_OPEN;
 				gripper.removeIsDoneListener(this);
 			}
 			@Override
 			public void whenClosed() {}
 			@Override
 			public void whenLifted() {}
 			@Override
 			public void whenReleased() {}
 			@Override
 			public void whenClosedLifted() {
 				gripperState = RobotState.GRIPPER_CLOSE;
 				gripper.removeIsDoneListener(this);
 			}
 			@Override
 			public void whenReleasedOpened() {
 				gripperState = RobotState.GRIPPER_OPEN;
 				gripper.removeIsDoneListener(this);
 			}
 			@Override
 			public void whenError() {}
 		};
 		// Gripper öffnen
 		gripperState = RobotState.OPENING_GRIPPER;
 		getGripper().open(gl);
 		// Warten bis Gripper offen
 		waitForGripperState(RobotState.GRIPPER_OPEN);
 
 		// Zur Barrel fahren
 		plannerState = RobotState.DRIVING_TO_BARREL;
 		planner.addIsDoneListener(new IPlannerListener() {
 			@Override
 			public void callWhenIsDone() {
 				plannerState = RobotState.BARREL_REACHED;
 			}
 			@Override
 			public void callWhenAbort() {}
 			@Override
 			public void callWhenNotValid() {}
 		});
 		planner.setGoal(barrelPos);
 		// Warten bis Barrel erreicht wurde
 		waitForPlannerState(RobotState.BARREL_REACHED);
 
 		// Gripper schliessen
 		gripperState = RobotState.CLOSING_GRIPPER;
 		getGripper().closeLift(gl);
 		// Warten bis Gripper geschlossen
 		waitForGripperState(RobotState.GRIPPER_CLOSE);
 
 		// Barrel zum Ziel fahren
 		plannerState = RobotState.TRANSPORTING_BARREL;
 		planner.addIsDoneListener(new IPlannerListener() {
 			@Override
 			public void callWhenIsDone() {
 				plannerState = RobotState.BARREL_TARGET_REACHED;
 			}
 			@Override
 			public void callWhenAbort() {}
 			@Override
 			public void callWhenNotValid() {
 				logger.info("No valid path");
 			}
 		});
 		planner.setGoal(targetPos);
 		// Warten bis Ziel erreicht wurde
 		waitForPlannerState(RobotState.BARREL_TARGET_REACHED);
 
 		// Barrel absetzen
 		gripperState = RobotState.OPENING_GRIPPER;
 		getGripper().releaseOpen(gl);
 		// Warten bis Gripper offen
 		waitForGripperState(RobotState.GRIPPER_OPEN);
 
 		// Etwas Abstand von der Barrel nehmen
 		Position newPos = new Position(); // TODO Abstand zur Barrel als Position berechnen!
 		planner.addIsDoneListener(new IPlannerListener() {
 			@Override
 			public void callWhenIsDone() {
 				state = RobotState.NEEDS_NEW_GOAL;
 			}
 			@Override
 			public void callWhenAbort() {}
 			@Override
 			public void callWhenNotValid() {
 				logger.info("No valid path");
 				state = RobotState.NEEDS_NEW_GOAL;
 			}
 		});
 		planner.setGoal(newPos);
 	}
 
 	public abstract void doStep();
 	
 	// TODO Aus den relativen Position des Barrels müssen die Weltkoordinaten berechnet werden.
 	// Barrelobject muss aus dem Informationen im Barrelarray gebaut werden. Angaben in cm. Könnte schwierig  sein
 	private MapPosition barrelCoordToWorldCoord(double xcoord, double ycoord) {
 		Position ownPosition = this.getPosition();
 
 		// Drehung um ownPosition als Drehzentrum, um den Winkel ownPosition.getYawn()
 		double x0 = ownPosition.getX();
 		double y0 = ownPosition.getY();
 		double a = ownPosition.getYaw();
		double x = ownPosition.getX() + ycoord/100;
		double y = ownPosition.getY() + (-xcoord/100);
 		MapPosition barrelPosition = new MapPosition(x0 + (x - x0)*Math.cos(a) - (y - y0)*Math.sin(a),
 																								 y0 + (x - x0)*Math.sin(a) + (y - y0)*Math.cos(a));
 		return barrelPosition;
 	}
 	
 	// TODO implementieren
 	private void checkForNewBarrels() {
 		if(barrelPositions.size() != 0) {
 			for(double[] barrel : barrelPositions) {
 				Barrel currentBarrel;
 				BarrelColor color = null;
 				MapPosition position = barrelCoordToWorldCoord(barrel[1], barrel[2]);
 				
 				switch((int)barrel[0]) {
 				case 0:
 					color = BarrelColor.BLUE;
 					break;
 				case 1:
 					color = BarrelColor.GREEN;
 					break;
 				case 2:
 					color = BarrelColor.YELLOW;
 					break;
 				default:
 					break;
 				}
 
 				currentBarrel = new Barrel(color, position);
 				// Barrels werden im Moment nur anhand ihrer Farbe verglichen,
 				// dass ist aber auch erstmal ok, da nur ein exemplar pro
 				// Farbe existiert.
 				if(!knownBarrels.contains(currentBarrel)) {
 					knownBarrels.add(currentBarrel);
 				}
 			}
 		}
 	}
 	
 }
