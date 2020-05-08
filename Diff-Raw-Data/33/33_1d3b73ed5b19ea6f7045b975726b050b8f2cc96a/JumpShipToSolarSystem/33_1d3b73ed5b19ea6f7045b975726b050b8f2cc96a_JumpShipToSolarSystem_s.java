 package com.evervoid.state.action.ship;
 
 import com.evervoid.client.graphics.geometry.MathUtils;
 import com.evervoid.json.Json;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.action.IllegalEVActionException;
 import com.evervoid.state.geometry.Dimension;
 import com.evervoid.state.geometry.GridLocation;
 import com.evervoid.state.geometry.Point;
 import com.evervoid.state.prop.Portal;
 import com.evervoid.state.prop.Ship;
 
 public class JumpShipToSolarSystem extends ShipAction
 {
 	private final Portal aDestination;
 	private final GridLocation aDestLocation;
 	private final Portal aPortal;
 	private final MoveShip aUnderlyingMove;
 
 	public JumpShipToSolarSystem(final Json j, final EVGameState state) throws IllegalEVActionException
 	{
 		super(j, state);
 		aDestination = (Portal) state.getPropFromID(j.getIntAttribute("destPortal"));
 		aPortal = (Portal) state.getPropFromID(j.getIntAttribute("portal"));
 		aDestLocation = new GridLocation(j.getAttribute("destLoc"));
 		aUnderlyingMove = new MoveShip(j.getAttribute("movement"), state);
 	}
 
 	public JumpShipToSolarSystem(final Ship ship, final Portal portal) throws IllegalEVActionException
 	{
 		super("JumpShip", ship);
 		final Dimension shipDim = ship.getData().getDimension();
 		aPortal = portal;
 		final GridLocation closestJump = ship.getLocation().getClosest(portal.getJumpingLocations(shipDim));
 		aUnderlyingMove = new MoveShip(ship, closestJump.origin);
 		aDestination = aPortal.getWormhole().getOtherPortal(portal);
 		// TODO - decide on a real location
		try {
			GridLocation tempLocation;
			do {
				tempLocation = new GridLocation((Point) MathUtils.getRandomElement(aDestination.getJumpingLocations(ship
						.getDimension())), ship.getDimension());
 			}
			while (!aDestination.getContainer().isOccupied(tempLocation));
			aDestLocation = tempLocation;
		}
		catch (final NullPointerException e) {
			throw new IllegalEVActionException("no valid jump exit locations");
 		}
 	}
 
 	public boolean destinationFree()
 	{
 		return !aDestination.getContainer().isOccupied(aDestLocation);
 	}
 
 	@Override
 	public void execute()
 	{
 		aShip.jumpToSolarSystem(aDestination.getContainer(), aUnderlyingMove.getPath(), aDestLocation, aPortal);
 	}
 
 	@Override
 	public boolean isValid()
 	{
 		return aUnderlyingMove.isValid() && destinationFree();
 	}
 
 	@Override
 	public Json toJson()
 	{
 		final Json j = super.toJson();
 		j.setIntAttribute("destPortal", aDestination.getID());
 		j.setAttribute("destLoc", aDestLocation);
 		j.setAttribute("movement", aUnderlyingMove);
 		j.setIntAttribute("portal", aPortal.getID());
 		return j;
 	}
 }
