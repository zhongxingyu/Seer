 package jig.ironLegends.core;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import jig.engine.ImageResource;
 import jig.engine.RenderingContext;
 import jig.engine.physics.vpe.ConvexPolygon;
 import jig.engine.util.Vector2D;
 import jig.ironLegends.core.ConvexPolyBody;
 
 /**
  * Allows multiple sprites to overlay the body
  * Position is at the top left of the minimal square that contains a circle that circumscribes the polygon's vertices
  * (If we restrict to even sided (e.g. rectangular ) shapes we may be able to simplify this and have the 
  * Sprites always render centered on the polygon (as opposed to from the top left point) (for now)
  * Each sprite may have an orientation different than that of the Polygon
  * Each sprite should be able to be turned on/off independently (if we get to it)
  * NOTE: may need to not extend from Body but instead some other interface since we want to add 
  * sprites on top and not have the concept of a single sprite per body.
  * E.g. just implement viewable and contain data of Polygon (for position) and contain collection of sprites
  * but then would have to change all the layer stuff, so lets finish the game for now even if not elegant
  */
 public class MultiSpriteBody extends ConvexPolyBody 
 {
 	// set to PersonsConvexPolygon because some data required is not visible in ConvexPolygon
 	// later see if can get away with just using ConvexPolygon
 	//protected PersonsConvexPolygon m_shape = null;
 	boolean m_bSpritesInSync = false;
 	
 	Vector<ATSprite> m_sprites = new Vector<ATSprite>();
 
 	public void setRotation(double radians)
 	{
 		m_shape.setRotation(radians);
 	}
 	public double getRotation()
 	{
 		return m_shape.getRotation();
 	}
 	
 	/*
 	 * just require shape to be set during constructor to make it obvious that it is required
 	public void setBackingShape(PersonsConvexPolygon shape) 
 	{
 		// convex polygon's position is the top left corner of the minimal square
 		// that contains a circle which circumscribes the polygon's vertices
 		
 		m_shape = shape;
 		//Vector2D offsetToRotation = m_shape.getOffsetToRotation();
 		setPosition(m_shape.getPosition());
 		
 		// we will set (sync) the position later?
 		// isn't it misleading to set a shape which has a position
 		// and not have it set the position
 		// ok,ok, lets set the position here
 		// need to define the relationship between the position of the MultiSpriteBody and the ConvexPolygon
 		// simple matter, the position of both uses the ConvexPolygon's definition of position
 		// then just need to render the sprites manually to render based on center position
 		// OR .. set each sprites position based on ConvexPolygon's position
 		
 		// summary
 		// - Rendered Sprite positions will be calculated based on ConvexPolygon's center position and the sprite's width/height
 		// - ConvexPolygon's position is: the top left corner of the minimal square
 		// that contains a circle which circumscribes the polygon's vertices
 		// - MultiSpriteBody's position is: the same as the ConvexPolygon's
 		// - The MultiSpriteBody's base sprite can't be used due to the difference in
 		// interpretation of position unless the base sprite ensures to encompass the padding required
 		// - The multisprite body's base sprite can be disabled just like any other 
 	}
 	*/
 	
 	public MultiSpriteBody(ConvexPolygon shape)
 	{
 		super(shape);
 	}
 	
 	public MultiSpriteBody(ConvexPolygon shape, final List<ImageResource> frameset)
 	{
 		super(shape);
 		addSprite(frameset);
 	}
 	/*
 	// create default shape
 	public MultiSpriteBody(final String rsc)
 	{
 		super(null);
 		addSprite(rsc);
 		// create shape from sprites dimensions (but would need polygon factory)
 		
 		setShape(null);
 		
 	}
 	*/
 	public MultiSpriteBody(ConvexPolygon shape, final String rsc)
 	{
 		super(shape);
 		addSprite(rsc);
 	}
 	@Override 
 	public void setCenterPosition(Vector2D shapeCenter)
 	{
 		Vector2D offsetToRotation = getOffsetToRotation();
 		setPosition(shapeCenter.translate(new Vector2D(-offsetToRotation.getX(), -offsetToRotation.getY())));
 	}
 	@Override
 	public Vector2D getCenterPosition()
 	{
 		return getShapeCenter();
 	}
 	public Vector2D getShapeCenter()
 	{
 		return getPosition().translate(getOffsetToRotation());
 	}
 	
 	// returns handle to sprite (can be used later to manipulate sprite (ok, ok, its just an index)
 	public int addSprite(final String rsc)
 	{
 		m_sprites.add(new ATSprite(rsc));
 		m_bSpritesInSync = false;
 		
 		return m_sprites.size()-1;
 	}
 	protected ATSprite getSprite(int spriteHandle)
 	{
 		if (spriteHandle < m_sprites.size() && spriteHandle >= 0)
 			return m_sprites.get(spriteHandle);
 		return null;
 	}

 	public void setSpriteVisible(int spriteHandle, boolean bVisible)
 	{
 		ATSprite sprite = getSprite(spriteHandle);
 		if (sprite != null)
 		{
 			sprite.setActivation(bVisible);
 		}
 	}
 	
 	public void setSpriteRotation(int spriteHandle, double rotRad)
 	{
 		ATSprite sprite = getSprite(spriteHandle);
 		if (sprite != null)
 		{
 			sprite.setRotation(rotRad);
 		}
 	}
 	// returns handle to sprite (can be used later to manipulate sprite (ok, ok, its just an index)
 	public int addSprite(final List<ImageResource> frameset)
 	{
 		m_sprites.add(new ATSprite(frameset));
 		m_bSpritesInSync = false;
 		
 		return m_sprites.size()-1;
 	}
 	
 	@Override
 	public void setPosition(final Vector2D pos)
 	{
 		super.setPosition(pos);
 		m_bSpritesInSync = false;
 	}
 	
 	@Override
 	public void update(long deltaMs) 
 	{
 		// TODO Auto-generated method stub
 
 	}
 	
 	protected void syncSprites()
 	{
 		if (!m_bSpritesInSync)
 		{
 			Vector2D centerPos = new Vector2D(position.getX() + getOffsetToRotation().getX()
 					, position.getY() + getOffsetToRotation().getY());
 			Iterator<ATSprite> iter = m_sprites.iterator();
 			while (iter.hasNext())
 			{
 				ATSprite s = iter.next();
 				if (!s.isActive())
 					continue;
 				//TODO compute appropriate offset (configurable relative to "body" center)
 				s.setCenterPosition(centerPos);
 				//s.setCenterPosition(centerPos.transform(s.getOffset()));
 			}
 		}
 		m_bSpritesInSync = true;
 	}
 	
 	@Override
 	public void render(final RenderingContext rc)
 	{
 		syncSprites();
 		// rc should contain world -> screen transform?
 		// just call render on each sprite (but with each sprites orientation)
 		double selfRotationRad = getRotation();
 		
 		Iterator<ATSprite> iter = m_sprites.iterator();
 		while (iter.hasNext())
 		{
 			ATSprite s = iter.next();
 			if (!s.isActive())
 				continue;
 			
 			s.render(rc, selfRotationRad);
 		}
 	}
 }
