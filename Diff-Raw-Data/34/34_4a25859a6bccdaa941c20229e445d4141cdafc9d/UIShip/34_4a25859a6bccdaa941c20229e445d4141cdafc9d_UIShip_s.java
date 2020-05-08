 package com.evervoid.client.views.solar;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.evervoid.client.graphics.Colorable;
 import com.evervoid.client.graphics.EverNode;
 import com.evervoid.client.graphics.GraphicsUtils;
 import com.evervoid.client.graphics.MultiSprite;
 import com.evervoid.client.graphics.Shade;
 import com.evervoid.client.graphics.Sprite;
 import com.evervoid.client.graphics.geometry.AnimatedAlpha;
 import com.evervoid.client.graphics.geometry.AnimatedTransform.DurationMode;
 import com.evervoid.client.graphics.geometry.Animation;
 import com.evervoid.client.ui.ButtonControl;
 import com.evervoid.client.ui.ClickObserver;
 import com.evervoid.client.ui.RescalableControl;
 import com.evervoid.client.ui.UIControl;
 import com.evervoid.client.ui.UIControl.BoxDirection;
 import com.evervoid.client.views.game.GameView;
 import com.evervoid.client.views.game.turn.TurnListener;
 import com.evervoid.client.views.game.turn.TurnSynchronizer;
 import com.evervoid.state.EVContainer;
 import com.evervoid.state.action.ship.BombPlanet;
 import com.evervoid.state.action.ship.CapturePlanet;
 import com.evervoid.state.action.ship.EnterCargo;
 import com.evervoid.state.action.ship.JumpShipIntoPortal;
 import com.evervoid.state.action.ship.MoveShip;
 import com.evervoid.state.action.ship.ShipAction;
 import com.evervoid.state.action.ship.ShootShip;
 import com.evervoid.state.data.SpriteData;
 import com.evervoid.state.data.TrailData;
 import com.evervoid.state.geometry.GridLocation;
 import com.evervoid.state.geometry.Point;
 import com.evervoid.state.observers.ShipObserver;
 import com.evervoid.state.player.Player;
 import com.evervoid.state.prop.Planet;
 import com.evervoid.state.prop.Prop;
 import com.evervoid.state.prop.Ship;
 import com.evervoid.state.prop.ShipPath;
 import com.evervoid.utils.MathUtils;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector2f;
 
 public class UIShip extends UIShadedProp implements Colorable, ShipObserver, TurnListener, ClickObserver
 {
 	private static final float sActionUIIndicationDuration = 0.7f;
 	/**
 	 * Constant that all ship shields alpha will be multiplied by, because full-opacity shields don't look good.
 	 */
 	private static final float sShieldFullAlpha = 0.35f;
 	public static final float sUIShipAppearTime = 0.4f;
 	private EverNode aActionNode = null;
 	private ShipAction aActionToCommit = null;
 	private final SpriteData aBaseSprite;
 	private final ButtonControl aCancelActionButton;
 	private final Map<Ship, ShipAction> aCargoActions;
 	private Sprite aColorableSprite;
 	private AnimatedAlpha aShieldAlpha;
 	private final Ship aShip;
 	/**
 	 * Trail of the ship. The trail auto-attaches to the ship (the method for that depends on the trail type), so no need to
 	 * attach it manually in UIShip
 	 */
 	private UIShipTrail aTrail = null;
 
 	public UIShip(final SolarGrid grid, final Ship ship)
 	{
 		super(grid, ship.getLocation(), ship);
 		aShip = ship;
 		aBaseSprite = aShip.getData().getBaseSprite();
 		buildProp();
 		aGridTranslation.setDuration(ship.getData().getMovingTime());
 		// Set rotation speed and mode:
 		aFaceTowards.setSpeed(ship.getData().getRotationSpeed()).setDurationMode(DurationMode.CONTINUOUS);
 		setHue(GraphicsUtils.getColorRGBA(ship.getColor()));
 		ship.registerObserver(this);
 		GameView.registerTurnListener(this);
 		// created cancel button
 		aCancelActionButton = new ButtonControl("Cancel");
 		aCancelActionButton.registerClickObserver(this);
 		aCargoActions = new HashMap<Ship, ShipAction>();
 		smoothAppear(sUIShipAppearTime);
 	}
 
 	public void bomb(final GridLocation target, final Runnable callback)
 	{
 		if (isHiddenByFogOfWar()) { // Not visible, skip animation
 			if (callback != null) {
 				callback.run();
 			}
 			faceTowards(target);
 			return;
 		}
 		faceTowards(target, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				// TODO: Make race-specific bombs
 				new UIShipBomb(getSolarSystemGrid().getGridAnimationNode(), getCellCenter(), aGrid.getCellBounds(target), 0.4,
 						new SpriteData("explosions/bomb.png"), callback);
 			}
 		});
 	}
 
 	/**
 	 * Builds a ship sprite and attaches it to a MultiSprite.
 	 * 
 	 * @param base
 	 *            The MultiSprite to build inside
 	 * @param bindToInstance
 	 *            Whether to attach the sprites created to the current UIShip instance
 	 * @param bottomLeftAsOrigin
 	 *            Whether to use the bottom left corner as origin or not
 	 * @return
 	 */
 	private MultiSprite buildShipSprite(final MultiSprite base, final boolean bindToInstance)
 	{
 		final Sprite baseSprite = new Sprite(aBaseSprite);
 		base.addSprite(baseSprite);
 		final Sprite colorOverlay = new Sprite(aShip.getData().getColorOverlay());
 		base.addSprite(colorOverlay);
 		if (bindToInstance) {
 			aColorableSprite = colorOverlay;
 		}
 		colorOverlay.setHue(aColorableSprite.getHue());
 		final Point engineOffset = aShip.getData().getEngineOffset();
 		final TrailData trailInfo = aShip.getTrailData();
 		base.addSprite(new Sprite(trailInfo.engineSprite, engineOffset.x, engineOffset.y));
 		for (final SpriteData turret : aShip.getWeaponSprites()) {
 			base.addSprite(new Sprite(turret));
 		}
 		final Sprite shieldOverlay = new Sprite(aShip.getShieldSprite());
 		shieldOverlay.getNewTransform().setScale(aShip.getData().getShieldScale());
 		final AnimatedAlpha shieldAlpha = shieldOverlay.getNewAlphaAnimation();
		shieldAlpha.setDuration(0.5).setAlpha(sShieldFullAlpha * aShip.getShieldsPercentage());
 		base.addSprite(shieldOverlay);
 		if (bindToInstance) {
 			aShieldAlpha = shieldAlpha;
 		}
 		return base;
 	}
 
 	@Override
 	protected void buildSprite()
 	{
 		buildShipSprite(aSprite, true);
 		final TrailData trailInfo = aShip.getTrailData();
 		switch (trailInfo.trailKind) {
 			case BUBBLE:
 				aTrail = new UIShipBubbleTrail(this, trailInfo.baseSprite, trailInfo.distanceInterval, trailInfo.decayTime);
 				break;
 			case GRADUAL:
 				aTrail = new UIShipLinearTrail(this, trailInfo.trailSprites);
 				break;
 		}
 		final Shade shade = new Shade(aShip.getData().getBaseSprite());
 		shade.setGradientPortion(0.6f);
 		addSprite(shade);
 		setShade(shade);
 		enableFloatingAnimation(1f, 2f);
 	}
 
 	public boolean canShoot()
 	{
 		return aShip.canShoot() && aShip.getPlayer().equals(GameView.getLocalPlayer());
 	}
 
 	public void capture(final List<GridLocation> move, final Planet planet, final Runnable callback)
 	{
 		if (isHiddenByFogOfWar()) { // Not visible, skip animation
 			moveTo(planet.getLocation());
 			if (callback != null) {
 				callback.run();
 			}
 			return;
 		}
 		smoothMoveTo(move, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				faceTowards(planet.getLocation(), callback);
 			}
 		});
 	}
 
 	@Override
 	public void delFromGrid()
 	{
 		aShip.deregisterObserver(this);
 		GameView.deregisterTurnListener(this);
 		aTrail.removeFromParent();
 		if (aActionNode != null) {
 			aActionNode.smoothDisappear(sActionUIIndicationDuration);
 		}
 		super.delFromGrid();
 	}
 
 	public void enterCargo(final List<GridLocation> moves, final GridLocation destination, final Runnable callback)
 	{
 		if (isHiddenByFogOfWar()) { // Not visible, skip animation
 			delFromGrid();
 			if (callback != null) {
 				callback.run();
 			}
 			return;
 		}
 		smoothMoveTo(moves, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				faceTowards(destination, new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						// The UIShip is going to be destroyed, so we can safely override some animation stuff here
 						aPropAlpha.setDuration(0.35);
 						aPropAlpha.setTargetAlpha(0).start(new Runnable()
 						{
 							@Override
 							public void run()
 							{
 								delFromGrid();
 								if (callback != null) {
 									callback.run();
 								}
 							}
 						});
 					}
 				});
 			}
 		});
 	}
 
 	@Override
 	protected void finishedMoving()
 	{
 		if (aSpriteReady) {
 			aTrail.shipMoveEnd();
 		}
 	}
 
 	public ShipAction getCargoAction(final Ship ship)
 	{
 		return aCargoActions.get(ship);
 	}
 
 	@Override
 	protected Collection<EverNode> getEffectiveChildren()
 	{
 		final Collection<EverNode> direct = super.getEffectiveChildren();
 		if (aTrail == null) {
 			return direct;
 		}
 		final Collection<EverNode> children = new ArrayList<EverNode>(direct.size() + 1);
 		children.addAll(direct);
 		if (aTrail != null) {
 			children.add(aTrail);
 		}
 		return children;
 	}
 
 	public EverNode getGridAnimationNode()
 	{
 		return getSolarSystemGrid().getGridAnimationNode();
 	}
 
 	public float getMovingSpeed()
 	{
 		return aGridTranslation.getMovingSpeed();
 	}
 
 	/**
 	 * Panel is structured in 3 columns Base | Stats | Abilities Base contains basic info (health, shields, rad) Stats contains
 	 * stats (lasers, speed, etc) Abilities contains buttons for executing abilities
 	 */
 	@Override
 	public UIControl getPanelUI()
 	{
 		// create all controls
 		final UIControl root = new UIControl(BoxDirection.HORIZONTAL);
 		final UIControl base = new UIControl(BoxDirection.VERTICAL);
 		final UIControl status = new UIControl(BoxDirection.VERTICAL);
 		final UIControl abilities = new UIControl(BoxDirection.VERTICAL);
 		final UIControl action = new UIControl(BoxDirection.VERTICAL);
 		// fill base stats
 		base.addUI(new RescalableControl(buildShipSprite(new MultiSprite(), false)), 1);
 		base.addString(aShip.getData().getTitle(), ColorRGBA.White, BoxDirection.HORIZONTAL);
 		final Player owner = aShip.getPlayer();
 		if (owner.isNullPlayer()) {
 			base.addString("Neutral", ColorRGBA.LightGray, BoxDirection.HORIZONTAL);
 		}
 		else {
 			base.addString("Owned by:\n " + owner.getNickname(), GraphicsUtils.getColorRGBA(aShip.getPlayer().getColor()),
 					BoxDirection.HORIZONTAL);
 		}
 		status.addString("Health: " + aShip.getHealth() + "/" + aShip.getMaxHealth(), ColorRGBA.Red);
 		status.addString("Shields: " + aShip.getShields() + "/" + aShip.getMaxShields(), ColorRGBA.Red);
 		status.addString("Radiation: " + aShip.getRadiation() + "/" + aShip.getMaxRadiation(), ColorRGBA.Red);
 		status.addFlexSpacer(1);
 		if (aShip.getPlayer().equals(GameView.getLocalPlayer()) || GameView.isGameOver()) {
 			// this is player sensitive information, only display it if the prop belongs to local player
 			// TODO maybe add an isGameOver clause to the above
 			// abilities
 			if (aShip.getCargoCapacity() > 0) {
 				final UIControl cargo = new UIControl(BoxDirection.HORIZONTAL);
 				cargo.addString("Cargo Hold: " + aShip.getCurrentCargoSize() + "/" + aShip.getCargoCapacity());
 				cargo.addFlexSpacer(1);
 				cargo.addSpacer(10, cargo.getMinimumHeight());
 				status.addUI(cargo);
 			}
 			abilities.addFlexSpacer(1);
 			// current Action
 			action.addString("Current Action:", ColorRGBA.White);
 			action.addString(aActionToCommit != null ? aActionToCommit.getDescription() : "None", ColorRGBA.Red);
 			aCancelActionButton.setEnabled(aActionToCommit != null);
 			action.addUI(new UIControl(BoxDirection.HORIZONTAL));
 			action.addUI(aCancelActionButton);
 			action.addFlexSpacer(1);
 		}
 		// add them all to the root
 		root.addFlexSpacer(1);
 		root.addUI(base);
 		root.addFlexSpacer(1);
 		root.addUI(status);
 		root.addUI(abilities);
 		root.addFlexSpacer(1);
 		root.addUI(action);
 		root.addFlexSpacer(1);
 		return root;
 	}
 
 	public Ship getShip()
 	{
 		return aShip;
 	}
 
 	public Vector2f getTrailAttachPoint()
 	{
 		return MathUtils.getVector2fFromPoint(aShip.getData().getTrailOffset()).mult(aBaseSprite.scale);
 	}
 
 	@Override
 	boolean isMovable()
 	{
 		return !aFrozen && getPropState().equals(PropState.SELECTED) && aShip.getPlayer().equals(GameView.getLocalPlayer());
 	}
 
 	public void jump(final List<GridLocation> leavingMove, final GridLocation portalLoc, final Runnable callback)
 	{
 		// Warning, hardcore animations ahead
 		if (isHiddenByFogOfWar()) { // Not visible, skip animation
 			delFromGrid();
 			if (callback != null) {
 				callback.run();
 			}
 			return;
 		}
 		smoothMoveTo(leavingMove, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				faceTowards(portalLoc, new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						// The whole node is going to get destroyed at the end of this animation, so we can afford to
 						// override the animation parameters here
 						final Vector2f origin = getCellCenter();
 						final Vector2f portalVec = aGrid.getCellCenter(portalLoc);
 						final Vector2f multDelta = portalVec.subtract(origin).mult(10);
 						aGridTranslation.setDuration(1);
 						aPropAlpha.setDuration(0.35);
 						aGridTranslation.smoothMoveBy(multDelta).start(new Runnable()
 						{
 							@Override
 							public void run()
 							{
 								delFromGrid();
 								if (callback != null) {
 									callback.run();
 								}
 							}
 						});
 						aPropAlpha.setTargetAlpha(0).start();
 					}
 				});
 			}
 		});
 	}
 
 	@Override
 	public void populateTransforms()
 	{
 		super.populateTransforms();
 		if (aSpriteReady) {
 			aTrail.shipMove();
 		}
 	}
 
 	boolean setAction(final ShipAction action)
 	{
 		// Check if action being committed is the same as the one we already had
 		if ((action == null && aActionToCommit == null)
 				|| (action != null && aActionToCommit != null && action.equals(aActionToCommit))) {
 			return true; // same action, not really a fail
 		}
 		if (action != null && !action.isValid()) {
 			return false; // Invalid action
 		}
 		// If it's not, then let's update the action
 		if (aActionToCommit != null) {
 			// If there was an action previously, remove it
 			GameView.delAction(aActionToCommit);
 			aActionToCommit = null;
 		}
 		if (aActionNode != null) {
 			// If there were UI indications of the action, delete them
 			aActionNode.smoothDisappear(sActionUIIndicationDuration);
 			aActionNode = null;
 		}
 		// Now put the new action in place
 		aActionToCommit = action;
 		// show the action in the UIPanel
 		refreshUI();
 		if (aActionToCommit == null) {
 			// Putting a null action -> do nothing
 			return true;// we actually succeeded here
 		}
 		// Putting a non-null action -> Add it to GameView
 		GameView.addAction(aActionToCommit);
 		if (aActionToCommit instanceof MoveShip) {
 			final List<GridLocation> path = ((MoveShip) aActionToCommit).getSamplePath();
 			aActionNode = new ActionLine(aGrid, 1f, new ColorRGBA(.8f, .8f, 1f, 0.5f), aGridLocation, path);
 			faceTowards(path.get(0));
 		}
 		else if (aActionToCommit instanceof JumpShipIntoPortal) {
 			final GridLocation portal = ((JumpShipIntoPortal) aActionToCommit).getPortal().getLocation();
 			final List<GridLocation> path = ((JumpShipIntoPortal) aActionToCommit).getUnderlyingMove().getSamplePath();
 			aActionNode = new ActionLine(aGrid, 1f, new ColorRGBA(.8f, 1, .8f, .5f), aGridLocation, path, portal);
 			if (path.isEmpty()) { // If there's no path (ship is already next to a portal) then just face the portal
 				faceTowards(portal);
 			}
 			else { // Else face the first point of the path
 				faceTowards(path.get(0));
 			}
 		}
 		else if (aActionToCommit instanceof ShootShip) {
 			final GridLocation enemy = ((ShootShip) aActionToCommit).getTarget().getLocation();
 			aActionNode = new ActionLine(aGrid, 1f, new ColorRGBA(1, .8f, .8f, .5f), aGridLocation, enemy);
 			faceTowards(enemy);
 		}
 		else if (aActionToCommit instanceof EnterCargo) {
 			final GridLocation targetShip = ((EnterCargo) aActionToCommit).getTarget().getLocation();
 			aActionNode = new ActionLine(aGrid, 1f, new ColorRGBA(1, .8f, .8f, .5f), aGridLocation, targetShip);
 			faceTowards(targetShip);
 		}
 		else if (aActionToCommit instanceof CapturePlanet) {
 			final GridLocation targetPlanet = ((CapturePlanet) aActionToCommit).getTarget().getLocation();
 			aActionNode = new ActionLine(aGrid, 1f, new ColorRGBA(1, .8f, .8f, .5f), aGridLocation, targetPlanet);
 			faceTowards(targetPlanet);
 		}
 		else if (aActionToCommit instanceof BombPlanet) {
 			final GridLocation targetPlanet = ((BombPlanet) aActionToCommit).getTarget().getLocation();
 			aActionNode = new ActionLine(aGrid, 1f, new ColorRGBA(1, .8f, .8f, .5f), aGridLocation, targetPlanet);
 			faceTowards(targetPlanet);
 		}
 		// TODO: Add more actions here
 		if (aActionNode != null) {
 			getGridAnimationNode().addNode(aActionNode);
 			aActionNode.smoothAppear(sActionUIIndicationDuration);
 		}
 		return true;
 	}
 
 	public void setCargoAction(final Ship ship, final ShipAction action)
 	{
 		if (action == null) {
 			// purely remove the action
 			GameView.delAction(aCargoActions.remove(ship));
 			return;
 		}
 		if (!action.isValid()) {
 			// don't bother
 			return;
 		}
 		else if (aShip.containsElem(ship)) {
 			// ths ship is in the cargo
 			if (aCargoActions.containsKey(ship)) {
 				// remove old action
 				GameView.delAction(aCargoActions.get(ship));
 			}
 			// put new action
 			aCargoActions.put(ship, action);
 			GameView.addAction(action);
 		}
 	}
 
 	@Override
 	void setFogOfWarAlpha(final boolean visible)
 	{
 		setFogOfWarAlpha(visible ? 1 : 0);
 	}
 
 	@Override
 	public void setHue(final ColorRGBA hue)
 	{
 		aColorableSprite.setHue(hue);
 	}
 
 	@Override
 	public void setHue(final ColorRGBA hue, final float multiplier)
 	{
 		aColorableSprite.setHue(hue, multiplier);
 	}
 
 	@Override
 	public void shipBombed(final Ship ship, final GridLocation bombLocation)
 	{
 		// Do nothing! The TurnSynchronizer will take care of the jump using UIShip.shoot()
 	}
 
 	@Override
 	public void shipCapturedPlanet(final Ship ship, final Planet planet, final ShipPath underlyingPath)
 	{
 		// Do nothing! The TurnSynchronizer will take care of the capture using UIShip.capture()
 	}
 
 	@Override
 	public void shipDestroyed(final Ship ship)
 	{
 		new MultiExplosion(aSolarGrid.getGridAnimationNode(), FastMath.sqr(getLocation().getPoints().size()),
 				aSolarGrid.getCellBounds(getLocation()), new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						smoothDisappear(0.2f, new Runnable()
 						{
 							@Override
 							public void run()
 							{
 								delFromGrid();
 							}
 						});
 					}
 				});
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void shipEnteredCargo(final Ship container, final Ship docker, final ShipPath shipPath)
 	{
 		// Do nothing! The TurnSynchronizer will take care of the jump using UIShip.enterCargo()
 		refreshUI();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void shipEnteredThis(final Ship containerShip, final Ship ship)
 	{
 		refreshUI();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void shipExitedCargo(final Ship container, final Ship docker)
 	{
 		refreshUI();
 	}
 
 	@Override
 	public void shipHealthChanged(final Ship ship, final int damageAmount)
 	{
 		refreshUI();
 	}
 
 	@Override
 	public void shipJumped(final Ship ship, final EVContainer<Prop> oldContainer, final ShipPath leavingMove,
 			final EVContainer<Prop> newContainer)
 	{
 		// Do nothing! The TurnSynchronizer will take care of the jump using UIShip.jump()
 	}
 
 	@Override
 	public void shipLeftContainer(final Ship ship, final EVContainer<Prop> container, final ShipPath exitPath)
 	{
 		// Do nothing; the TurnSynchronizer should cover all cases where this happens and do the removal manually.
 	}
 
 	@Override
 	public void shipMoved(final Ship ship, final GridLocation oldLocation, final ShipPath path)
 	{
 		// Do nothing! The TurnSynchronizer will take care of the movement using UIShip.smoothMoveTo()
 	}
 
 	@Override
 	public void shipShieldsChanged(final Ship ship, final int shields)
 	{
 		if (aShieldAlpha != null) {
			aShieldAlpha.setTargetAlpha(sShieldFullAlpha * aShip.getShieldsPercentage()).start();
 		}
 		refreshUI();
 	}
 
 	@Override
 	public void shipShot(final Ship ship, final GridLocation shootLocation)
 	{
 		// Do nothing! The TurnSynchronizer will take care of the shooting using UIShip.shoot()
 	}
 
 	public void shoot(final GridLocation target, final Runnable callback)
 	{
 		final Runnable finalCallback = new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				if (callback != null) {
 					callback.run();
 				}
 			}
 		};
 		if (isHiddenByFogOfWar()) { // Not visible, skip animation
 			finalCallback.run();
 			faceTowards(target);
 			return;
 		}
 		final List<Point> lasers = aShip.getWeaponSlots();
 		if (lasers.isEmpty()) {
 			// Ship has no visible weapons; just call callback directly, can't play animation
 			finalCallback.run();
 			return;
 		}
 		// Else, it's animation time~
 		final Animation animation = new Animation(finalCallback);
 		final EverNode animationNode = getSolarSystemGrid().getGridAnimationNode();
 		final Vector2f cellCenter = getCellCenter();
 		final float shots = aShip.getWeaponData().getShots();
 		final float interval = aShip.getWeaponData().getInterval();
 		final float duration = aShip.getWeaponData().getSpeed();
 		final SpriteData laserSprite = aShip.getWeaponData().getLaserSprite();
 		for (final Point laser : lasers) {
 			final Vector2f offset = new Vector2f(laser.x, laser.y);
 			offset.rotateAroundOrigin(-getFacingDirection(), true);
 			final Vector2f targetVector = aGrid.getRandomVectorInCell(target, true);
 			float delay = 0;
 			for (int shot = 0; shot < shots; shot++) {
 				final Vector2f randomShot = new Vector2f(MathUtils.getRandomFloatBetween(-4, 4),
 						MathUtils.getRandomFloatBetween(-4, 4));
 				animation.addStep(delay, new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						new UIShipLaser(animationNode, cellCenter.add(offset), targetVector.add(randomShot), duration,
 								laserSprite);
 					}
 				});
 				delay += interval;
 			}
 		}
 		faceTowards(target, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				animation.start();
 			}
 		});
 	}
 
 	@Override
 	public void smoothMoveTo(final List<GridLocation> moves, final Runnable callback)
 	{
 		if (isHiddenByFogOfWar()) { // Not visible, skip animation
 			final GridLocation last = moves.get(moves.size() - 1);
 			moveTo(last);
 			if (callback != null) {
 				callback.run();
 			}
 			faceTowards(last);
 			return;
 		}
 		super.smoothMoveTo(moves, callback);
 		if (aSpriteReady) {
 			aTrail.shipMoveStart();
 		}
 	}
 
 	@Override
 	public void turnPlayedback()
 	{
 		aFrozen = false;
 		refreshUI();
 	}
 
 	@Override
 	public void turnReceived(final TurnSynchronizer synchronizer)
 	{
 		aCargoActions.clear(); // Remove cargo actions
 		setAction(null);
 		if (aActionNode != null) {
 			aActionNode.smoothDisappear(sActionUIIndicationDuration);
 			aActionNode = null;
 		}
 		synchronizer.registerShip(aShip, this);
 	}
 
 	@Override
 	public void turnSent()
 	{
 		aFrozen = true;
 	}
 
 	@Override
 	public void uiClicked(final UIControl clicked)
 	{
 		if (clicked.equals(aCancelActionButton)) {
 			setAction(null);
 		}
 	}
 }
