 package jig.ironLegends;
 
 import jig.engine.Sprite;
 import jig.engine.util.Vector2D;
 import jig.ironLegends.core.MultiSpriteBody;
 import jig.ironLegends.mapEditor.MapItemPersist;
 import jig.misc.sat.PolygonFactory;
 
 public class Obstacle extends MultiSpriteBody {
 	private static final int CRATE_MAX_HEALTH = 20;
	private static final int BASE_MAX_HEALTH = 200;
 	private static int WALL_MAX_HEALTH = 40;
 	private String m_name;
 	private Destructible m_destructible;
 
 	public String name() {
 		return m_name;
 	}
 
 	Obstacle(String cellInfo, PolygonFactory pf) {
 		super(null);
 
 		MapItemPersist item = new MapItemPersist(cellInfo);
 
 		m_name = item.name();
 
 		Vector2D pos = item.centerPosition();
 
 		int h = addSprite(item.resource());
 		Sprite s = getSprite(h);
 		super.setShape(pf.createRectangle(pos, s.getWidth(), s.getHeight()));
 		super.setCenterPosition(pos);
 		setRotation(Math.toRadians(item.rotDeg()));
 		super.setCenterPosition(pos);
 
 		// set destructible data
 		setDestructibleData();
 	}
 
 	@Override
 	public void update(long deltaMs) {
 		if (!isActive()) {
 			return;
 		}
 		
 		if (m_name.equals("wall")) {
 			if (m_destructible.getHealth() < WALL_MAX_HEALTH) {
 				getSprite(0).setFrame(1);
 			}
 		}
 	}
 	
 	// allow caller to set damage? or create wrapper methods?
 	public Destructible getDestructible() {
 		return m_destructible;
 	}
 
 	// TODO: override render and set frame based on health?
 	// TODO: on collision, get destructible and assign damage
 	private void setDestructibleData() {
 		// TODO: lookup from table?
 
 		if (m_name.equals("wall")) {
 			m_destructible = new Destructible(WALL_MAX_HEALTH);
 		} else if (m_name.equals("rock1")) {
 			// m_destructible = new Destructible(WALL_MAX_HEALTH);
 		} else if (m_name.equals("rock2")) {
 			// m_destructible = new Destructible(WALL_MAX_HEALTH);
 		} else if (m_name.equals("tree")) {
 			// m_destructible = new Destructible(WALL_MAX_HEALTH);
 		} else if (m_name.equals("crate")) {
 			m_destructible = new Destructible(CRATE_MAX_HEALTH);
		} else if (m_name.equals("bluebase")) {
			m_destructible = new Destructible(BASE_MAX_HEALTH);
		} else if (m_name.equals("redbase")) {
			m_destructible = new Destructible(BASE_MAX_HEALTH);
 		}
 	}
 	
 	public boolean isTree() {
 		return m_name.equals("tree");
 	}
 }
