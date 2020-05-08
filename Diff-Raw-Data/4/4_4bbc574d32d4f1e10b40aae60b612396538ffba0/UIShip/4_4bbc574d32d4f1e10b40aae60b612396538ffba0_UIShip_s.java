 package com.evervoid.client.views.solar;
 
 import java.util.List;
 
 import com.evervoid.client.graphics.Colorable;
 import com.evervoid.client.graphics.GraphicsUtils;
 import com.evervoid.client.graphics.Shade;
 import com.evervoid.client.graphics.Sprite;
 import com.evervoid.client.graphics.geometry.AnimatedTransform.DurationMode;
 import com.evervoid.client.graphics.geometry.MathUtils;
 import com.evervoid.state.EVContainer;
 import com.evervoid.state.data.SpriteData;
 import com.evervoid.state.data.TrailData;
 import com.evervoid.state.geometry.GridLocation;
 import com.evervoid.state.geometry.Point;
 import com.evervoid.state.observers.ShipObserver;
 import com.evervoid.state.prop.Portal;
 import com.evervoid.state.prop.Prop;
 import com.evervoid.state.prop.Ship;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 
 public class UIShip extends UIShadedProp implements Colorable, ShipObserver
 {
 	private SpriteData aBaseSprite;
 	private Sprite aColorableSprite;
 	private final Ship aShip;
 	/**
 	 * Trail of the ship. The trail auto-attaches to the ship (the method for that depends on the trail type), so no need to
 	 * attach it manually in UIShip
 	 */
 	private UIShipTrail aTrail;
 
 	public UIShip(final SolarGrid grid, final Ship ship)
 	{
 		super(grid, ship.getLocation(), ship);
 		aShip = ship;
 		buildProp();
 		aGridTranslation.setDuration(ship.getData().getMovingTime());
 		// Set rotation speed and mode:
 		aFaceTowards.setSpeed(ship.getData().getRotationSpeed()).setDurationMode(DurationMode.CONTINUOUS);
 		setHue(GraphicsUtils.getColorRGBA(ship.getColor()));
 		ship.registerObserver(this);
 	}
 
 	@Override
 	protected void buildSprite()
 	{
 		aBaseSprite = aShip.getData().getBaseSprite();
 		final Sprite baseSprite = new Sprite(aBaseSprite);
 		addSprite(baseSprite);
 		aColorableSprite = new Sprite(aShip.getData().getColorOverlay());
 		addSprite(aColorableSprite);
 		final TrailData trailInfo = aShip.getTrailData();
 		switch (trailInfo.trailKind) {
 			case BUBBLE:
 				aTrail = new UIShipBubbleTrail(this, trailInfo.baseSprite, trailInfo.distanceInterval, trailInfo.decayTime);
 				break;
 			case GRADUAL:
 				aTrail = new UIShipLinearTrail(this, trailInfo.trailSprites);
 				break;
 		}
 		final Point engineOffset = aShip.getData().getEngineOffset();
 		addSprite(new Sprite(trailInfo.engineSprite, engineOffset.x, engineOffset.y));
 		final Shade shade = new Shade(aShip.getData().getBaseSprite());
 		shade.setGradientPortion(0.6f);
 		addSprite(shade);
 		setShade(shade);
 		enableFloatingAnimation(1f, 2f);
 	}
 
 	@Override
 	public void delFromGrid()
 	{
 		aShip.deregisterObserver(this);
 		aTrail.removeFromParent();
 		super.delFromGrid();
 	}
 
 	@Override
 	protected void finishedMoving()
 	{
 		if (aSpriteReady) {
 			aTrail.shipMoveEnd();
 		}
 	}
 
 	public float getMovingSpeed()
 	{
 		return aGridTranslation.getMovingSpeed();
 	}
 
 	public Vector2f getTrailAttachPoint()
 	{
 		return MathUtils.getVector2fFromPoint(aShip.getData().getTrailOffset()).mult(aBaseSprite.scale);
 	}
 
 	@Override
 	boolean isMovable()
 	{
 		// TODO: Make this dependent on player (if the user is click on an enemy ship, this should return false)
 		return getPropState().equals(PropState.SELECTED);
 	}
 
 	public void moveShip(final List<GridLocation> path, final Runnable callback)
 	{
		if (path.isEmpty()) {
			System.err.println("Warning: UIShip " + this + " got an empty list of locations as path.");
			return;
		}
 		smoothMoveTo(path, callback);
 	}
 
 	@Override
 	public void populateTransforms()
 	{
 		super.populateTransforms();
 		if (aSpriteReady && getPropState().equals(PropState.MOVING)) {
 			aTrail.shipMove();
 		}
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
 	public void shipBombed(final GridLocation bombLocation)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void shipDestroyed(final Ship ship)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void shipJumped(final EVContainer<Prop> oldContainer, final List<GridLocation> leavingMove,
 			final EVContainer<Prop> newContainer, final Portal portal)
 	{
 		// Warning, hardcore animations ahead
 		moveShip(leavingMove, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				final UIProp uip = aSolarSystemGrid.getUIProp(portal);
 				if (uip != null) {
 					faceTowards(uip.getLocation(), new Runnable()
 					{
 						@Override
 						public void run()
 						{
 							// The whole node is going to get destroyed at the end of this animation, so we can afford to
 							// override the animation parameters here
 							final Vector2f origin = getCellCenter();
 							final Vector2f portalVec = uip.getCellCenter();
 							final Vector2f multDelta = portalVec.subtract(origin).mult(10);
 							aGridTranslation.setDuration(1);
 							aPropAlpha.setDuration(0.35f);
 							aGridTranslation.smoothMoveBy(multDelta).start(new Runnable()
 							{
 								@Override
 								public void run()
 								{
 									removeFromParent();
 								}
 							});
 							aPropAlpha.setTargetAlpha(0).start();
 						}
 					});
 				}
 			}
 		});
 	}
 
 	@Override
 	public void shipMoved(final Ship ship, final GridLocation oldLocation, final List<GridLocation> path)
 	{
 		moveShip(path, null);
 	}
 
 	@Override
 	public void shipShot(final GridLocation shootLocation)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void shipTookDamage(final int damageAmount)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void smoothMoveTo(final List<GridLocation> moves, final Runnable callback)
 	{
 		super.smoothMoveTo(moves, callback);
 		if (aSpriteReady) {
 			aTrail.shipMoveStart();
 		}
 	}
 }
