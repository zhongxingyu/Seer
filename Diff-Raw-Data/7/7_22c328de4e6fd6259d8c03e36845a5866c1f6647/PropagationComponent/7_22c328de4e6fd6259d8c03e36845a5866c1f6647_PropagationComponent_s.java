 package tiles.propagation;
 
 import game.Ray;
 import gui.ImageUtils;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 
 import misc.Direction;
 import tiles.DrawableLayer;
 import tiles.Tile;
 
 public abstract class PropagationComponent {
 
 	private Tile tile;
 	private Ray[] rays = new Ray[4];
 
 	protected PropagationComponent(Tile tile) {
 		this.tile = tile;
 	}
 
 	public void process(Ray ray) {
 	}
 
 	protected final Direction getDirection() {
 		return tile.getDirection();
 	}
 
 	protected final Color getColor() {
 		return tile.getColor();
 	}
 
 	protected void setOrigin(Ray ray) {
 		Direction origin = ray.getDirection().getOpposite();
 		setRay(origin, ray);
 	}
 
 	public void clear() {
 		for (int i = 0; i < 4; i++) {
 			rays[i] = null;
 		}
 	}
 
 	protected final void setRay(Direction d, Ray r) {
 		if (getRay(d) != null) {
			System.out.println(r);
			r.setColor(ImageUtils.mix(r.getColor(), getRay(d).getColor()));
 		}
		Ray ray = r.clone();
 		ray.setDirection(d);
 		rays[d.ordinal()] = ray;
 	}
 
 	protected final Ray getRay(Direction d) {
 		return rays[d.ordinal()];
 	}
 	
 
 
 	public boolean hasRays() {
 		for (Ray ray : rays) {
 			if (ray != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean hasRay(Color color) {
 		for (Ray ray : rays) {
 			if (ray != null && ray.getColor().equals(color)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public Iterable<DrawableLayer> getRays() {
 		// TODO: Fix use of generics
 		List<DrawableLayer> rays = new ArrayList<DrawableLayer>();
 
 		for (Ray ray : this.rays) {
 			rays.add(ray);
 		}
 
 		return rays;
 	}
 
 }
