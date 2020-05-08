 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2011 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.simulation.model;
 
 import java.awt.Point;
 import java.awt.Shape;
 import java.awt.geom.Arc2D;
 import java.awt.geom.Area;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.Collection;
 import java.util.logging.Logger;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 import de.uniluebeck.imis.casi.communication.ICommunicationComponent;
 import de.uniluebeck.imis.casi.simulation.engine.ISimulationClockListener;
 import de.uniluebeck.imis.casi.simulation.engine.SimulationEngine;
 import de.uniluebeck.imis.casi.simulation.factory.WorldFactory;
 import de.uniluebeck.imis.casi.simulation.model.Agent.STATE;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.AbstractAction;
 
 /**
  * This class represents components that are able to interact with their
  * surroundings and that have an area in which they are interested.
  * 
  * @author Tobias Mende
  * 
  */
 public abstract class AbstractInteractionComponent extends AbstractComponent
 		implements ICommunicationComponent, IExtendedAgentListener,
 		ISimulationClockListener {
 	/** The development logger */
 	private static final Logger log = Logger
 			.getLogger(AbstractInteractionComponent.class.getName());
 
 	/** Enumeration for possible directions in which this component looks */
 	public enum Face {
 		NORTH(90), SOUTH(270), EAST(0), WEST(180), NORTH_EAST(45), SOUTH_EAST(
 				315), NORTH_WEST(135), SOUTH_WEST(225);
 		/** The degree representation of this face */
 		private double degree;
 
 		/** Private Constructor */
 		private Face(double degree) {
 			this.degree = degree;
 		}
 
 		/**
 		 * Getter for the degree value.
 		 * 
 		 * @return the degree representation
 		 */
 		private double degree() {
 			return degree;
 		}
 
 		/**
 		 * Getter for the radian value.
 		 * 
 		 * @return the radian representation
 		 */
 		private double radian() {
 			return Math.toRadians(degree);
 		}
 
 		/**
 		 * Getter for a normalized direction vector
 		 * 
 		 * @return the direction vector
 		 */
 		private Point2D direction() {
 			return new Point2D.Double(Math.cos(radian()),
 					Math.sin(radian()));
 		}
 	}
 
 	/** Types this component can be */
 	public enum Type {
 		/** This component represents a sensor */
 		SENSOR,
 		/** This component represents an actuator */
 		ACTUATOR,
 		/** The component is as well a sensor as an actuator. */
 		MIXED;
 	}
 
 	/** Counter for created interaction components */
 	private static int idCounter;
 
 	/** The radius of the monitored shape */
 	protected int radius = -1;
 	/** The direction in which this component looks */
 	protected Face direction;
 	/** The extent of the monitored area */
 	protected double opening = -1;
 	/** The action to which this component is connected */
 	protected Agent agent = null;
 	/** is this component a wearable? */
 	protected boolean wearable = false;
 
 	/** List of actions, that can be recognized and vetoed by this component */
 	protected Collection<AbstractAction> interestingActions;
 	/** actual value this sensor has recognized */
 	protected Object lastValue;
 	/** Last message, the actuator has received from the network controller */
 	protected Object lastResponse;
 	/** Time for pulling values from the server in seconds */
 	public static final int PULL_INTERVALL = 10;
 	/** Is pull enabled? */
 	protected boolean pullEnabled = false;
 	/** Counts the ticks of the clock */
 	protected int clockTickCounter = 0;
 	/** The type of this component */
 	protected Type type = Type.SENSOR;
 
 	/** The {@link Arc2D} representation of the monitored area */
 	private Shape shapeRepresentation;
 
 	/**
 	 * id for serialization
 	 */
 	private static final long serialVersionUID = -9016454038144232069L;
 
 	/**
 	 * Constructor for a new interaction component with a given identifier and
 	 * position
 	 * 
 	 * @param identifier
 	 *            the identifier
 	 * @param coordinates
 	 *            the coordinates of this component
 	 */
 	public AbstractInteractionComponent(String identifier, Point2D coordinates) {
 		super(identifier);
 		this.coordinates = coordinates;
 	}
 
 	/**
 	 * Creates a wearable for the provided agent
 	 * 
 	 * @param agent
 	 *            the agent which wears this component
 	 */
 	public AbstractInteractionComponent(Agent agent) {
 		this(agent.getCoordinates());
 		this.agent = agent;
 		wearable = true;
 
 	}
 
 	/**
 	 * Sets the agents which wears this component
 	 * 
 	 * @param agent
 	 *            the agent to set
 	 */
 	public void setAgent(Agent agent) {
 		this.agent = agent;
 	}
 
 	/**
 	 * Getter for the agent which is connected to this component
 	 * 
 	 * @return the agent which may wear this component
 	 */
 	public Agent getAgent() {
 		return agent;
 	}
 
 	/**
 	 * Constructor for a new interaction component with a given position
 	 * 
 	 * @param coordinates
 	 *            the coordinates of this component
 	 */
 	public AbstractInteractionComponent(Point2D coordinates) {
 		this("ioComponent-" + idCounter, coordinates);
 		idCounter++;
 	}
 
 	/**
 	 * Constructor which creates an interaction component with a specified
 	 * monitored area
 	 * 
 	 * @param coordinates
 	 *            the position of this component
 	 * @param radius
 	 *            the radius of the monitored area
 	 * @param direction
 	 *            the direction in which this component "looks"
 	 * @param opening
 	 *            the opening angle of the area
 	 */
 	public AbstractInteractionComponent(Point coordinates, int radius,
 			Face direction, int opening) {
 		this(coordinates);
 		this.radius = radius;
 		this.direction = direction;
 		this.opening = opening;
 	}
 
 	/**
 	 * Checks whether this component actually is weared by an agent
 	 * 
 	 * @return <code>true</code> if the wearing agent is not null,
 	 *         <code>false</code> otherwise.
 	 */
 	public boolean isWeared() {
 		return agent != null && isWearable();
 	}
 
 	/**
 	 * Sets the wearable state of this component
 	 * 
 	 * @param wearable
 	 *            if <code>true</code>, the assigned agent wears this component.
 	 */
 	public void setWearable(boolean wearable) {
 		this.wearable = wearable;
 	}
 
 	/**
 	 * Is this component a wearable?
 	 * 
 	 * @return the wearable
 	 */
 	public boolean isWearable() {
 		return wearable;
 	}
 
 	@Override
 	public Shape getShapeRepresentation() {
 		if (shapeRepresentation != null) {
 			// Shape was calculated before:
 			return shapeRepresentation;
 		}
 		if (radius < 0 && getCurrentPosition() != null) {
 			// Monitor the whole room:
 			shapeRepresentation = getCurrentPosition().getShapeRepresentation();
 			return shapeRepresentation;
 		}
 		// Calculate the new shape:
 		double currentOpening = opening < 0 ? 360 : opening;
 		Face currentDirection = direction == null ? Face.NORTH : direction;
 		double startAngle = currentDirection.degree()
 				- ((double) currentOpening / 2.0);
 		shapeRepresentation = new Arc2D.Double(calculateCircleBounds(),
 				startAngle, currentOpening, Arc2D.PIE);
		int scale = 3;
		Point2D pointInRoom = new Point2D.Double(scale*currentDirection.direction()
				.getX() + getCentralPoint().getX(), scale*currentDirection
 				.direction().getY() + getCentralPoint().getY());
 		Room room = WorldFactory.getRoomsWithPoint(pointInRoom).getFirst();
 		Area area = new Area(shapeRepresentation);
 		area.intersect(new Area(room.getShapeRepresentation()));
 		shapeRepresentation = area;
 		return shapeRepresentation;
 	}
 
 	/**
 	 * Calculates a quadratic area which is exactly big enough to contain the
 	 * circle which is used to calculate the monitored area
 	 * 
 	 * @return the bounds
 	 */
 	private Rectangle2D calculateCircleBounds() {
 		double y = coordinates.getY() - radius;
 		double x = coordinates.getX() - radius;
 		return new Rectangle2D.Double(x, y, 2 * radius, 2 * radius);
 	}
 
 	@Override
 	public Point2D getCentralPoint() {
 		return getCoordinates();
 	}
 
 	/**
 	 * Setter for a new monitored area
 	 * 
 	 * @param direction
 	 *            the direction in which this component "looks"
 	 * @param radius
 	 *            the radius of the monitored area
 	 * @param opening
 	 *            the opening angle
 	 */
 	public void setMonitoredArea(Face direction, int radius, int opening) {
 		shapeRepresentation = null;
 		this.direction = direction;
 		this.radius = radius;
 		this.opening = opening;
 	}
 
 	/**
 	 * Resets the monitored area. Components with unspecified monitored areas
 	 * monitor the whole room in which they are contained.
 	 */
 	public void resetMonitoredArea() {
 		setMonitoredArea(null, -1, -1);
 	}
 
 	/**
 	 * Method for handling an action internal. Overwrite for customized behavior
 	 * 
 	 * @param action
 	 *            the action to handle
 	 * @return <code>true</code> if the action is allowed, <code>false</code>
 	 *         otherwise
 	 */
 	protected abstract boolean handleInternal(AbstractAction action, Agent agent);
 
 	/**
 	 * Getter for human readable version of the current value
 	 * 
 	 * @return the value in a nicer format
 	 */
 	public abstract String getHumanReadableValue();
 
 	/**
 	 * Getter for the type of this sensor
 	 * 
 	 * @return the sensor type
 	 */
 	public String getType() {
 		return getClass().getSimpleName();
 	}
 
 	@Override
 	public String toString() {
 		return super.toString() + " (" + getType() + ", "+getHumanReadableValue()+")";
 	}
 
 	@Override
 	public boolean handle(AbstractAction action, Agent agent) {
 		return this.contains(agent) ? handleInternal(action, agent) : true;
 	}
 
 	@Override
 	public void positionChanged(Point2D oldPosition, Point2D newPosition,
 			Agent agent) {
 		if (isWeared() && this.agent.equals(agent)) {
 			setCoordinates(newPosition);
 			shapeRepresentation = null;
 		}
 	}
 
 	@Override
 	public void timeChanged(SimulationTime newTime) {
 		if ((clockTickCounter = (++clockTickCounter) % PULL_INTERVALL) == 0) {
 			makePullRequest(newTime);
 		}
 	}
 
 	/**
 	 * Overwrite to let the component periodically pull informations from the
 	 * communication handler
 	 */
 	protected void makePullRequest(SimulationTime newTime) {
 		// nothing to do here.
 	}
 
 	@Override
 	public void simulationPaused(boolean pause) {
 		// nothing to do here
 	}
 
 	@Override
 	public void simulationStopped() {
 		// nothing to do here
 	}
 
 	@Override
 	public void simulationStarted() {
 		// nothing to do here
 	}
 
 	/**
 	 * Checks if this component has a veto right when an agent tries to perform
 	 * an action. (Only actuators and mixed components have a veto right)
 	 * 
 	 * @return <code>true</code> if this component is allowed to interrupt an
 	 *         action, <code>false</code> otherwise.
 	 */
 	public final boolean hasVetoRight() {
 		return !type.equals(Type.SENSOR);
 	}
 
 	/**
 	 * Sets a new shape representation of the monitored area
 	 * 
 	 * @param shape
 	 *            the shape do monitor
 	 */
 	public void setShapeRepresentation(Shape shape) {
 		this.shapeRepresentation = shape;
 	}
 
 	/**
 	 * Checks whether this cube is interested in a given action and agent.
 	 * (default: interested in nothing)
 	 * 
 	 * @param action
 	 *            the action
 	 * @param agent
 	 *            the agent
 	 * @return <code>true</code> if the cube is interested, <code>false</code>
 	 *         otherwise.
 	 */
 	protected boolean checkInterest(AbstractAction action, Agent agent) {
 		return false;
 	}
 
 	/**
 	 * Checks whether this cube is interested in a given agent (default:
 	 * interested in nothing)
 	 * 
 	 * @param agent
 	 *            the agent
 	 * @return <code>true</code> if the cube is interested, <code>false</code>
 	 *         otherwise.
 	 */
 	protected boolean checkInterest(Agent agent) {
 		return false;
 	}
 
 	@Override
 	public void stateChanged(STATE newState, Agent agent) {
 		// nothing to do here
 
 	}
 
 	@Override
 	public void interruptibilityChanged(INTERRUPTIBILITY interruptibility,
 			Agent agent) {
 		// nothing to do here
 
 	}
 
 	@Override
 	public void startPerformingAction(AbstractAction action, Agent agent) {
 		// nothing to do here
 
 	}
 
 	@Override
 	public void finishPerformingAction(AbstractAction action, Agent agent) {
 		// nothing to do here
 
 	}
 
 	@Override
 	public void receive(Object message) {
 		/*
 		 * Should be implemented by components that want to receive messages
 		 * from the communication handler. On other components, this call should
 		 * fail because it's an unexpected behavior.
 		 */
 		throw new NotImplementedException();
 	}
 
 	@Override
 	public void init() {
 		SimulationEngine.getInstance().getCommunicationHandler().register(this);
 	}
 
 }
