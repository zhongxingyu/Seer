 package com.evervoid.client.views.solar;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import com.evervoid.client.EVClientEngine;
 import com.evervoid.client.KeyboardKey;
 import com.evervoid.client.graphics.GraphicsUtils;
 import com.evervoid.client.graphics.Grid;
 import com.evervoid.client.graphics.GridNode;
 import com.evervoid.client.graphics.geometry.EightAxisController;
 import com.evervoid.client.views.game.GameView;
 import com.evervoid.client.views.game.turn.TurnListener;
 import com.evervoid.client.views.game.turn.TurnSynchronizer;
 import com.evervoid.client.views.solar.UIProp.PropState;
 import com.evervoid.state.SolarSystem;
 import com.evervoid.state.action.IllegalEVActionException;
 import com.evervoid.state.action.ship.JumpShipIntoPortal;
 import com.evervoid.state.action.ship.MoveShip;
 import com.evervoid.state.action.ship.ShootShip;
 import com.evervoid.state.geometry.Dimension;
 import com.evervoid.state.geometry.GridLocation;
 import com.evervoid.state.geometry.Point;
 import com.evervoid.state.observers.SolarObserver;
 import com.evervoid.state.prop.Planet;
 import com.evervoid.state.prop.Portal;
 import com.evervoid.state.prop.Prop;
 import com.evervoid.state.prop.Ship;
 import com.evervoid.state.prop.Star;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 
 /**
  * This class represents the grid displayed when in the solar system view.
  */
 public class SolarGrid extends Grid implements SolarObserver, TurnListener
 {
 	static final int sCellSize = 64;
 	static final float sKeyboardAutoScrollInterval = 0.075f;
 	private GridLocation aAutoScrollLocation = null;
 	private Dimension aCursorSize = new Dimension(1, 1);
 	private final GridAnimationNode aGridAnimationNode = new GridAnimationNode(this);
 	private final SolarGridSelection aGridCursor;
 	private SolarGridHighlightLocations aHighlightedLocations = null;
 	private boolean aIsAutoScrolling = false;
 	private final EightAxisController aKeyboardControl = new EightAxisController();
 	private boolean aLastAutoScrolled = true;
 	private float aSecondsSinceLastAutoScroll = 0f;
 	private Prop aSelectedProp = null;
 	private final SolarSystem aSolarSystem;
 	private final SolarView aSolarSystemView;
 	private final ColorRGBA aStarGlowColor;
 	private final Map<Prop, UIProp> aUIProps = new HashMap<Prop, UIProp>();
 	/**
 	 * The vector towards which the camera should zoom in/out
 	 */
 	private final Vector2f aZoomFocusLocation = new Vector2f(0, 0);
 
 	/**
 	 * Default constructor generating
 	 * 
 	 * @param view
 	 *            The solar system view to generate the grid in.
 	 * @param ss
 	 *            The solar system represented by this grid.
 	 */
 	public SolarGrid(final SolarView view, final SolarSystem ss)
 	{
 		super(ss.getDimension(), sCellSize, sCellSize, 1, new ColorRGBA(1f, 1f, 1f, 0.2f));
 		aSolarSystemView = view;
 		aSolarSystem = ss;
 		aSolarSystem.registerObserver(this);
 		aStarGlowColor = GraphicsUtils.getColorRGBA(ss.getSunShadowColor());
 		aGridCursor = new SolarGridSelection();
 		addNode(aGridCursor);
 		populateProps();
 	}
 
 	/**
 	 * Adds a GridNode to the Grid. Called by UIProp itself. It is (always) a UIProp that gets added, so add the corresponding
 	 * mapping too.
 	 */
 	@Override
 	protected void addGridNode(final GridNode node)
 	{
 		super.addGridNode(node);
 		if (node instanceof UIProp) {
 			final UIProp uiprop = (UIProp) node;
 			aUIProps.put(uiprop.getProp(), uiprop);
 		}
 	}
 
 	/**
 	 * Autoscrolls the grid with keyboard
 	 * 
 	 * @param tpf
 	 *            Time per frame
 	 * @param gridScale
 	 *            Multiplier to give to the keyboard's movement strength (same as grid scale)
 	 * @param force
 	 *            Force scroll by one now
 	 */
 	void autoscroll(final float tpf, final float gridScale, final boolean force)
 	{
 		if (aIsAutoScrolling) {
 			aSecondsSinceLastAutoScroll += tpf / gridScale;
 		}
 		if (force || aSecondsSinceLastAutoScroll >= sKeyboardAutoScrollInterval) {
 			aSecondsSinceLastAutoScroll = 0f;
 			// Need to scroll now
 			if (aAutoScrollLocation == null) {
 				aAutoScrollLocation = new GridLocation(aGridCursor.getLocation().origin);
 			}
 			aAutoScrollLocation = aAutoScrollLocation.add(aKeyboardControl.getHorizontalDelta(),
 					aKeyboardControl.getVerticalDelta()).constrain(aSolarSystem.getDimension());
 			aZoomFocusLocation.set(getCellCenter(aAutoScrollLocation)); // Update zoom location
 			aGridCursor.goTo(getCursorLocationAt(aZoomFocusLocation));
 			aSolarSystemView.ensureLocationVisible(getCellBounds(aAutoScrollLocation));
 		}
 	}
 
 	@Override
 	public void computeTransforms()
 	{
 		super.computeTransforms();
 		if (aSolarSystemView != null) {
 			aSolarSystemView.computeGridDimensions();
 		}
 	}
 
 	/**
 	 * Deletes a GridNode from the Grid. Called by UIProp itself. It is (always) a UIProp that gets deleted, so delete the
 	 * corresponding mapping too.
 	 */
 	@Override
 	protected void delGridNode(final GridNode node)
 	{
 		super.delGridNode(node);
 		if (node instanceof UIProp) {
 			final UIProp prop = (UIProp) node;
 			aUIProps.remove(prop.getProp());
 		}
 	}
 
 	private void deselectProp()
 	{
 		if (aHighlightedLocations != null) {
 			aHighlightedLocations.fadeOut();
 			aHighlightedLocations = null;
 		}
 		if (aSelectedProp != null) {
 			aUIProps.get(aSelectedProp).setState(PropState.SELECTABLE);
 		}
 		aSelectedProp = null;
 		aCursorSize = new Dimension(1, 1);
 		if (aLastAutoScrolled) {
 			aAutoScrollLocation = new GridLocation(aAutoScrollLocation.origin);
 			aZoomFocusLocation.set(getCellCenter(aAutoScrollLocation));
 			aGridCursor.goTo(aAutoScrollLocation);
 		}
 		else {
 			hover(aZoomFocusLocation);
 		}
 		aSolarSystemView.getPerspective().clearPanel();
 	}
 
 	/**
 	 * Out of a series of Props, returns the one which is the closest to the given Grid-based position. Used for selection
 	 * "spanning" to the closest prop
 	 * 
 	 * @param position
 	 *            The Grid-based position to look at
 	 * @param props
 	 *            The series of props to consider
 	 * @param ignoreSelected
 	 *            Whether to ignore the currently-selected prop or not
 	 * @return The closest prop, or null if the series was empty
 	 */
 	private Prop getClosestPropTo(final Vector2f position, final Iterable<Prop> props, final boolean ignoreSelected)
 	{
 		float minDistance = Float.MAX_VALUE;
 		Prop closest = null;
 		for (final Prop p : props) {
 			if (ignoreSelected && p.equals(aSelectedProp)) {
 				continue;
 			}
 			final float propDistance = getCellBounds(p.getLocation()).getClosestTo(position).length();
 			if (propDistance < minDistance) {
 				closest = p;
 				minDistance = propDistance;
 			}
 		}
 		return closest;
 	}
 
 	/**
 	 * Look up hypothetical cursor GridLocation if the user were to point at the specified grid-based vector
 	 * 
 	 * @param position
 	 *            Grid-based position
 	 * @return Cursor GridLocation at that point, or null if it is out of the grid
 	 */
 	private GridLocation getCursorLocationAt(final Vector2f position)
 	{
 		final GridLocation pointed = getCellAt(position, aCursorSize);
 		if (pointed == null) {
 			// Mouse is out of the grid
 			return null;
 		}
 		final boolean ignoreSelectedProp = aSelectedProp != null && aUIProps.get(aSelectedProp).isMovable();
 		final Prop prop = getClosestPropTo(position, aSolarSystem.getPropsAt(pointed), ignoreSelectedProp);
 		if (prop == null) {
 			return pointed;
 		}
 		return prop.getLocation();
 	}
 
 	/**
 	 * @return A GridAnimationNode for this solar system grid.
 	 */
 	public GridAnimationNode getGridAnimationNode()
 	{
 		return aGridAnimationNode;
 	}
 
 	/**
 	 * Given a ship, returns the set of all locations that should be highlighted when that ship is selected
 	 * 
 	 * @param ship
 	 *            The selected ship
 	 * @return The set of GridLocations to be highlighted
 	 */
 	private Set<GridLocation> getHighlightedShipMoves(final Ship ship)
 	{
 		final Set<GridLocation> moves = ship.getValidDestinations();
 		for (final Portal p : aSolarSystem.getPortals()) {
 			// TODO: This is inefficient but more correct for the user. It will hold for the demo.
 			// Once the demo is done, the ships will jump into the wormhole rather than directly into the solar system, so this
 			// will not be an issue.
 			/*
 			 * if (moves.contains(ship.getLocation().getClosest(p.getJumpingLocations(ship.getDimension())))) {
 			 * moves.add(p.getLocation()); }
 			 */
 			try {
 				final JumpShipIntoPortal tempAction = new JumpShipIntoPortal(ship, p, GameView.getGameState());
 				if (tempAction.isValid()) {
 					moves.add(p.getLocation());
 				}
 			}
 			catch (final IllegalEVActionException e) {
 				// action was not valid, nothing to see here
 			}
 		}
 		return moves;
 	}
 
 	/**
 	 * Finds if there is a UIProp at the given point
 	 * 
 	 * @param position
 	 *            The point to look at
 	 * @return The UIProp at the given point, or null if there is no prop there
 	 */
 	UIProp getPropAt(final Point point)
 	{
 		final Prop found = aSolarSystem.getPropAt(point);
 		if (found == null) {
 			return null;
 		}
 		return aUIProps.get(found);
 	}
 
 	/**
 	 * @return A GridLocation where the sun is located.
 	 */
 	public GridLocation getSunLocation()
 	{
 		return aSolarSystem.getSunLocation();
 	}
 
 	/**
 	 * @return The glow color of the sun
 	 */
 	public ColorRGBA getSunShadowColor()
 	{
 		return aStarGlowColor;
 	}
 
 	public UIProp getUIProp(final Prop prop)
 	{
 		return aUIProps.get(prop);
 	}
 
 	/**
 	 * @return The grid-based vector towards which to zoom in/out
 	 */
 	Vector2f getZoomFocusLocation()
 	{
 		return aZoomFocusLocation;
 	}
 
 	/**
 	 * Handle hover events on the grid
 	 * 
 	 * @param position
 	 *            Grid-based position that was hovered
 	 * @return Whether the cursor was on the grid or not
 	 */
 	boolean hover(final Vector2f position)
 	{
 		aAutoScrollLocation = null; // Invalidate autoscroll cursor
 		aZoomFocusLocation.set(position); // Update zoom location
 		aIsAutoScrolling = false;
 		aLastAutoScrolled = false;
 		final GridLocation pointed = getCursorLocationAt(position);
 		if (pointed == null) {
 			// Mouse is out of the grid
 			aGridCursor.fadeOut();
 			return false;
 		}
 		// Mouse is in the grid
 		aGridCursor.goTo(pointed);
 		aGridCursor.fadeIn();
 		return true;
 	}
 
 	boolean isAutoScrolling()
 	{
 		return aIsAutoScrolling;
 	}
 
 	/**
 	 * @return Whether the last movement method was by using autoscrolling or not
 	 */
 	boolean isLastAutoScrolled()
 	{
 		return aLastAutoScrolled;
 	}
 
 	/**
 	 * Handle left click events on the grid
 	 * 
 	 * @param position
 	 *            The Grid-based position that was clicked
 	 */
 	void leftClick(final Vector2f position)
 	{
 		final GridLocation pointed = getCellAt(position, aCursorSize);
 		if (pointed == null) {
 			deselectProp();
 			return; // User clicked outside of grid, don't go further
 		}
 		final Prop prop = getClosestPropTo(position, aSolarSystem.getPropsAt(pointed), true);
 		leftClickProp(prop);
 	}
 
 	private void leftClickProp(final Prop prop)
 	{
 		// The format of the following comments is:
 		// {List of conditions} -> {Effect on UI}
 		if (aSelectedProp == null && prop == null) {
 			// Nothing selected, clicking on nothing -> Do nothing
 			deselectProp();
 			return;
 		}
 		if (aSelectedProp != null) {
 			// Something selected
 			if (aSelectedProp.equals(prop)) {
 				// Something selected, clicking on same thing -> Do nothing
 				return;
 			}
 			// Something selected, clicking on something else -> Deselect current
 			deselectProp();
 		}
 		// Mark selected prop as null; if the user selects another one and it's selectable, then we'll set it back later
 		aSelectedProp = null;
 		if (prop != null) {
 			// Clicking on other prop -> Select it
 			final UIProp selected = aUIProps.get(prop);
 			if (!selected.isSelectable()) {
 				// Prop isn't selectable (inactive)
 				return;
 			}
 			aSelectedProp = prop;
 			selected.setState(PropState.SELECTED);
 			if (prop.getPlayer().equals(GameView.getPlayer())) {
 				// Stuff that should only be done if this prop belongs to the player
 				if (selected.isMovable()) {
 					aCursorSize = prop.getLocation().dimension;
 					if (prop.getPropType().equals("ship")) {
 						// Clicking on ship -> Show available locations
 						final Ship ship = (Ship) prop;
 						delNode(aHighlightedLocations);
 						aHighlightedLocations = new SolarGridHighlightLocations(this, getHighlightedShipMoves(ship));
 						addNode(aHighlightedLocations);
 					}
 				}
 			}
 			// Update panel view
 			aSolarSystemView.getPerspective().setPanelUI(selected.getPanelUI());
 		}
 		else {
 			// Clicking on empty space -> Reset cursor size to 1x1
 			aCursorSize = new Dimension(1, 1);
 		}
 	}
 
 	public boolean onKeyPress(final KeyboardKey key, final float gridScale)
 	{
 		aKeyboardControl.onKeyPress(key);
 		aIsAutoScrolling = aKeyboardControl.isMoving();
 		if (!aIsAutoScrolling) {
 			aSecondsSinceLastAutoScroll = 0f;
 		}
 		else {
 			if (!aLastAutoScrolled) {
 				// If this is the first keypress, scroll by one right now
 				aSecondsSinceLastAutoScroll = 0f; // Also reset seconds
 				autoscroll(0, gridScale, true);
 			}
 			aLastAutoScrolled = true;
 		}
 		if (key.getLetter().equals("b") && aSelectedProp != null && aSelectedProp instanceof Planet) {
 			/*
 			 * try { GameView.addAction(new IncrementShipConstruction(GameView.getGameState(), (Planet) aSelectedProp,
 			 * aSelectedProp .getPlayer().getRaceData().getShipTypes().iterator().next())); return true; } catch (final
 			 * IllegalEVActionException e) {
 			 * Logger.getLogger(EVClientEngine.class.getName()).warning("Failed To Create IncrementShipConstruction Action."); }
 			 */
 		}
 		return false;
 	}
 
 	public boolean onKeyRelease(final KeyboardKey key)
 	{
 		aKeyboardControl.onKeyRelease(key);
 		aIsAutoScrolling = aKeyboardControl.isMoving();
 		if (!aIsAutoScrolling) {
 			aSecondsSinceLastAutoScroll = 0f;
 		}
 		return false;
 	}
 
 	/**
 	 * Gets all the props in the SolarSystem and adds them to the grid.
 	 */
 	private void populateProps()
 	{
 		// Get all the props
 		// They will all add themselves to the grid
 		for (final Prop p : aSolarSystem.elemIterator()) {
 			if (p.getPropType().equals("ship")) {
 				new UIShip(this, (Ship) p);
 			}
 			else if (p.getPropType().equals("planet")) {
 				new UIPlanet(this, (Planet) p);
 			}
 			else if (p.getPropType().equals("star")) {
 				new UIStar(this, (Star) p);
 			}
 			else if (p.getPropType().equals("portal")) {
 				new UIPortal(this, (Portal) p);
 			}
 		}
 	}
 
 	/**
 	 * Handle right click events on the grid
 	 * 
 	 * @param position
 	 *            The Grid-based position that was clicked
 	 */
 	void rightClick(final Vector2f position)
 	{
 		final GridLocation pointed = getCellAt(position, aCursorSize);
 		if (pointed == null) {
 			return; // User clicked outside of grid, don't go further
 		}
 		final Prop prop = getClosestPropTo(position, aSolarSystem.getPropsAt(pointed), true);
 		if (prop != null) {
 			rightClickProp(prop);
 		}
 		else if (aSelectedProp != null && aUIProps.get(aSelectedProp).isMovable()) {
 			// Player clicked on an empty spot; move selected prop, if it is movable
 			if (aSelectedProp instanceof Ship) {
 				final Ship ship = (Ship) aSelectedProp;
 				final UIShip uiship = (UIShip) aUIProps.get(ship);
 				if (pointed.equals(ship.getLocation())) {
 					// Player clicked on the ship's spot itself -> Pass null action to cancel
 					uiship.setAction(null);
 				}
 				else {
 					// Player clicked elsewhere -> do actual move
 					MoveShip moveAction;
 					try {
 						moveAction = new MoveShip(ship, pointed.origin, GameView.getGameState());
 						if (moveAction.isValid()) {
 							uiship.setAction(moveAction);
 						}
 						else {
 							aGridCursor.flash();
 						}
 					}
 					catch (final IllegalEVActionException e) {
 						Logger.getLogger(EVClientEngine.class.getName()).warning("Failed To Create a MoveShip Action");
 					}
 				}
 				deselectProp();
 			}
 		}
 	}
 
 	/**
 	 * Handle right click events on valid props only
 	 * 
 	 * @param prop
 	 *            The prop that was right-clicked on
 	 */
 	private void rightClickProp(final Prop prop)
 	{
 		if (prop == null || aSelectedProp == null) {
 			return;
 		}
 		if (aSelectedProp instanceof Ship) {
 			// Ship actions
 			final Ship selectedShip = (Ship) aSelectedProp;
 			final UIShip selectedUIShip = (UIShip) aUIProps.get(selectedShip);
 			if (!selectedUIShip.isMovable()) {
 				// If ship can't move, it can't do anything this turn
 				return;
 			}
 			if (prop instanceof Portal) {
 				// Ship action: Jump into portal
 				try {
 					selectedUIShip.setAction(new JumpShipIntoPortal(selectedShip, (Portal) prop, GameView.getGameState()));
 					deselectProp();
 				}
 				catch (final IllegalEVActionException e) {
					Logger.getLogger(EVClientEngine.class.getName()).warning("Failed to create a JumpToSolarSystem action");
 				}
 			}
 			else if (prop instanceof Ship) {
 				// Ship action: Shoot other ship OR enter carrier ship
 				final Ship otherShip = (Ship) prop;
 				// FIXME: In order to test shooting, firing at allied ships is allowed
 				// thus it is impossible to enter carrier ships
 				if (true) { // FIXME: Change this condition to "Prop belongs to enemy"
 					// Ship action: Attack other ship
 					try {
 						// Damage is rolled server-side; input dummy damage value here
 						selectedUIShip.setAction(new ShootShip(selectedShip, otherShip, -1, GameView.getGameState()));
 						deselectProp();
 					}
 					catch (final IllegalEVActionException e) {
 						Logger.getLogger(EVClientEngine.class.getName()).warning("Failed to create a ShootShip action");
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void shipEntered(final Ship newShip)
 	{
 		// Ship will register itself
 		new UIShip(this, newShip);
 	}
 
 	@Override
 	public void shipLeft(final Ship oldShip)
 	{
 		// Do not remove the Ship from the UI; the Ship will take care of that by itself.
 		// Otherwise, animations will fail, as the UIShip gets removed too soon.
 	}
 
 	@Override
 	public void turnPlayedback()
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void turnReceived(final TurnSynchronizer synchronizer)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void turnSent()
 	{
 		// TODO Auto-generated method stub
 	}
 }
