 package state;
 
 import graphic.Camera;
 import graphic.Render;
 import graphic.UIListener;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Vector2f;
 
 public class TaskForce implements UIListener, Comparable<TaskForce>
 {
 	enum Type { SHIPS, AGENTS }
 	
 // Internals ==========================================================================================================
 	private String name;
 	private LinkedList<Star> destinations;		///< Route of stars to follow. The first star corresponds to the last star arrival.
 	private int turnsTotal;							///< Number of turns that it takes to move to the next destination.
 	private int turnsTraveled;               	///< Number of turns that have been moved towards that destination already.
 	private float speed;								///< Minimun common speed for the stacks.
 	private Empire owner;							///< Empire that owns this TaskForce.
 	private Type type;
 
 	Map<Design, Integer> stacks;									///< Stacks composing this TaskForce (individual ships and types).
 
 // Public Methods =====================================================================================================
 	TaskForce( String name, Star orbiting, Empire empire, Type type )
 	{
 		// Base values
 		this.speed = 5;
 		this.owner = empire;
 		this.type = type;
 		this.turnsTraveled = 0;
 		this.turnsTotal = 0;
 		this.name = name;
 		this.stacks = new HashMap<Design, Integer>();
 
 		this.destinations = new LinkedList<Star>();
 		this.destinations.add(orbiting);
 		orbiting.arrive(this);
 	}
 
 	/**
 	 * Truncates the taskforce route up to a specific star.
 	 * @param destination Point at which the route is truncated. If the star was not part of the route, nothing happens.
 	 * @return True if the route changed.
 	 */
 	public boolean removeFromRoute(Star destination)
 	{
 		// Find if destination is already included.
 		int index = destinations.indexOf(destination);
 		if(index <= 0)
 			return false;
 		while(destinations.size() > index)
 			destinations.removeLast();
 		return true;
 	}
 
 	/// @return True if the route changed.
 	public boolean addToRoute(Star destination)
 	{
 		// Check if destination is reachable
 		if(Lane.getDistance(destinations.getLast(), destination) <= 0)
 			return false;
 		destinations.addLast(destination);
 		return true;
 	}
 
 	void addShips(Design kind, int number)
 	{
 		Integer current = stacks.get(kind);
 		if(current != null)
 			current = 0;
 		stacks.put(kind, current + number);
 	}
 
 	void turn()
 	{
 		// If no destinations, do nothing.
 		if(destinations.isEmpty())
 			return;
 
 		// Check if we need to leave the current star.
 		if(destinations.size() > 1 && turnsTraveled == 0)
 			destinations.getFirst().leave(this);
 
 		// Move the task force one turn forward.
 		turnsTraveled++;
 		
 		// If we arrived at a star, put ourselves in orbit.
 		if(turnsTraveled == turnsTotal)
 		{
 			turnsTraveled = 0;
 			destinations.removeFirst();
 			destinations.getFirst().arrive(this);
 			
 			if(destinations.size() > 1)
 				turnsTotal = (int) Math.ceil(Lane.getDistance(destinations.getFirst(), destinations.get(1)) / speed); 
 		}
 	}	
 	
 	public void render(GameContainer gc, Graphics g, int flags)
 	{
 		g.setColor(owner.color());
 		
 		if((flags & Render.SELECTED) != 0)
 		{
 			g.setColor(Color.white);
 			
 			// Paint small dots for all our route, but only if the fleet is selected.
			Iterator<Star> i = destinations.descendingIterator();
 			Star to = i.next();
 			Vector2f dir = new Vector2f();
 			Vector2f zero = new Vector2f();
 			
 			while(i.hasNext())
 			{
				Star from = i.next();
 				dir.set(to.getPos());
 				dir.sub(from.getPos());
 				
 				int segments = (int) Math.ceil(Lane.getDistance(from, to) / speed);
 				for(int s=1; s<segments; s++)
 				{
 					drawRoutePoint(dir.copy().scale(1.0f * s / segments).add(from.getPos()), g, zero);
 				}
 			}
 		}
 		
 		// Paint the fleet icon.
 		if(turnsTraveled == 0)
 		{
 			// Paint orbiting the star. In this case, each taskforce is separated by a 30 degree angle.
 			Vector2f pos = new Vector2f(20.0f, 0.0f);
 			pos.setTheta(-30 * destinations.getFirst().getDock(this) - 30);
 			drawIcon(destinations.getFirst().getPos(), g, pos);
 		}
 		else
 		{
 			// Paint on route.
 //			drawIcon(dir, g, new Vector2f());
 		}
 	}
 	
 	private void drawIcon(Vector2f world, Graphics g, Vector2f screenDisp)
 	{
 		Camera.instance().pushLocalTransformation(g, world);
 		g.fillRect(screenDisp.x-4, screenDisp.y-4, 9, 9);
 		g.popTransform();
 	}
 
 	private void drawRoutePoint(Vector2f world, Graphics g, Vector2f screenDisp)
 	{
 		Camera.instance().pushLocalTransformation(g, world);
 		g.fillRect(screenDisp.x-2, screenDisp.y-2, 5, 5);
 		g.popTransform();
 	}
 
 	
 	@Override
 	public boolean screenCLick(float x, float y, int button)
 	{
 		Vector2f screen = new Vector2f(20.0f, 0.0f);
 		if(turnsTraveled == 0)
 		{
 			// Paint orbiting the star. In this case, each taskforce is separated by a 30 degree angle.
 			screen.setTheta(-30 * destinations.getFirst().getDock(this) - 30);
 			screen.add(Camera.instance().worldToScreen(destinations.getFirst().getPos()));
 		}
 		else
 		{
 			// TODO Click of a force in orbit. 
 			return false;
 		}
 
 		// Compare against mouse screen position.
 		Vector2f local = new Vector2f(x, y).sub(screen);
 		return (local.x * local.x <= 25 && local.y * local.y <= 25);
 	}
 
 	/**
 	 * Task forces are ordered in the following way:
 	 * 	1.- A task force which orbits a colony of the same empire always goes first.
 	 * 	2.- Task forces are then ordered by empire, on a fixed order.
 	 * 	3.- Task forces are ordered by type.
 	 * 
 	 * Apart from these characteristics, task forces are considered equivalent for ordering purposes, so this method is inconsistent with equals()
 	 */
 	@Override
 	public int compareTo(TaskForce o)
 	{
 		// Check if one or other is owner of the star.
 		int aux = 0;
 		Colony col = destinations.getFirst().getColony(); 
 		if(col != null)
 		{
 			if(col.owner() == owner)
 				aux += 1;
 			if(col.owner() == o.owner)
 				aux -= 1;
 			
 			if(aux != 0)
 				return aux;
 		}
 
 		// Check if they belong to different empires.
 		aux = this.owner.name().compareTo(o.owner.name());
 		if(aux != 0)
 			return aux;
 		
 		// Check their types.
 		return type.ordinal() - o.type.ordinal();
 	}
 	
 //	void paint(QPainter* painter,  QStyleOptionGraphicsItem*, QWidget*)
 //	{
 //		// Draw route to final destination.
 //		painter.save();
 //		if(true)//isSelected())
 //		{
 //			// Draw lines.
 //			QPointF last = QPoint(0,0);
 //			painter.setPen(QPen(Qt::red, 1, Qt::DashLine));
 //			foreach(Star* s, to)
 //			{
 //				EchoDebug(String("Drawing from (%1,%2) to (%3,%4)").arg(last.x()).arg(last.y()).arg(s.pos().x()).arg(s.pos().y()));
 //				painter.drawLine(last, s.pos() - pos());
 //				last = s.pos() - pos();
 //			}
 //		}
 //		painter.restore();
 //
 //		// Set star position and draw it.
 //		painter.setBrush(owner.color());
 //		painter.drawRect(-5, -5, 10, 10);
 //		if(isSelected())
 //			painter.drawRect(-7, -7, 14, 14);
 //
 //	}
 //
 //	QRectF boundingRect() 
 //	{
 //		return QRectF(-5, -5, 10, 10);
 //	}
 
 //	void mousePressEvent(QGraphicsSceneMouseEvent* event)
 //	{
 	// TODO
 //		if(isSelected())
 //		{
 //			// When a star is clicked, the route needs to be changed for this TaskForce.
 //			Star* star = dynamic_cast<Star*>(scene().itemAt(event.scenePos(), ));
 //			if(star)
 //			{
 //			   if(event.button() == Qt::LeftButton)
 //				   addToRoute(star);
 //			   else
 //				   removeFromRoute(star);
 //			   event.accept();
 //			}
 //		}
 //	}	
 }
