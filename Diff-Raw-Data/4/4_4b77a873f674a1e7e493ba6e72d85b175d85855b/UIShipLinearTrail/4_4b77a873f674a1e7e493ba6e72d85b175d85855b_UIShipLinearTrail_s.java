 package com.evervoid.client.views.solar;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.evervoid.client.graphics.EverNode;
 import com.evervoid.client.graphics.Sprite;
 import com.evervoid.client.graphics.geometry.Transform;
 import com.evervoid.state.data.SpriteData;
 import com.evervoid.utils.MathUtils;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 
 public class UIShipLinearTrail extends UIShipTrail
 {
 	protected List<Sprite> aGradualSprites = new ArrayList<Sprite>();
 	private float aGradualState = 0f;
 	private final Map<EverNode, Transform> aGradualTransforms = new HashMap<EverNode, Transform>();
 
 	public UIShipLinearTrail(final UIShip ship, final Vector2f trailOffset, final Iterable<SpriteData> sprites)
 	{
 		super(ship, trailOffset);
 		ship.addNode(this);
		getNewTransform().translate(trailOffset); // Attach point offset
 		for (final SpriteData s : sprites) {
			addSprite(s);
 		}
 	}
 
 	protected void computeGradual()
 	{
 		final float grad = aGradualState * getNumberOfSprites() - 1;
 		for (final Transform t : aGradualTransforms.values()) {
 			t.setAlpha(0);
 		}
 		final int currentSprite = (int) grad;
 		setAlphaOfFrame(currentSprite - 1, 1 - grad + currentSprite);
 		setAlphaOfFrame(currentSprite, 1);
 		setAlphaOfFrame(currentSprite + 1, grad - currentSprite);
 	}
 
 	private void setAlphaOfFrame(final int index, final float alpha)
 	{
 		if (index >= 0 && index < getNumberOfSprites()) {
 			aGradualTransforms.get(aGradualSprites.get(index)).setAlpha(alpha);
 		}
 	}
 
 	public void setGradualState(final float gradualState)
 	{
 		aGradualState = MathUtils.clampFloat(0, gradualState, 1);
 		computeGradual();
 	}
 
 	@Override
 	public void setHue(final ColorRGBA hue)
 	{
 		for (final Sprite spr : aGradualSprites) {
 			spr.setHue(hue);
 		}
 	}
 
 	@Override
 	public void setHue(final ColorRGBA hue, final float multiplier)
 	{
 		for (final Sprite spr : aGradualSprites) {
 			spr.setHue(hue, multiplier);
 		}
 	}
 
 	@Override
 	void shipMove()
 	{
 		setGradualState(aShip.getMovingSpeed());
 	}
 
 	@Override
 	protected void spriteAdded(final EverNode sprite)
 	{
 		aGradualSprites.add((Sprite) sprite);
 		aGradualTransforms.put(sprite, sprite.getNewAlphaAnimation());
 		computeGradual();
 	}
 }
