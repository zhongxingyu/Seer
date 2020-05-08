 package cwcore;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import javax.swing.JDialog;
 
 import cobweb.Environment;
 import cobweb.Environment.Location;
 import cobweb.TickScheduler.Client;
 import cwcore.ComplexEnvironment.Product;
 
 public class ProductionMapper {
 
 	private ComplexEnvironment e;
 	private float[][] vals;
 	private float maxValue;
 
 	public ProductionMapper(Environment e) {
 		this(500, e);
 	}
 
 	public ProductionMapper(int numProducts, Environment e) {
 		this.e = (ComplexEnvironment) e;
 		vals = new float[this.e.getWidth()][this.e.getHeight()];
 	}
 
 	public boolean addProduct(Product p, Environment.Location loc) {
 		maxValue = 0;
 		for (int x = 0; x < vals.length; x++) {
 			for (int y = 0; y < vals[x].length; y++) {
 				float value = getDifAtLoc(p, loc, e.getLocation(x, y));
 				vals[x][y] += value;
 				if (vals[x][y] > maxValue) {
 					maxValue = vals[x][y];
 				}
 			}
 		}
 		return true;
 	}
 
 	public boolean remProduct(Product p, Location loc) {
 		maxValue = 0;
 		for (int x = 0; x < vals.length; x++) {
 			for (int y = 0; y < vals[x].length; y++) {
 				float value = getDifAtLoc(p, loc, e.getLocation(x, y));
 				vals[x][y] -= value;
 				if (vals[x][y] > maxValue) {
 					maxValue = vals[x][y];
 				}				
 			}
 		}
 		return true;
 	}
 
 	private float getDifAtLoc(Product source, Location loc, Location loc2) {
 		float val = source.getValue();
 		val /= Math.max(1, loc.distanceSquare(loc2));
 		return val;
 	}
 
 	/**
 	 * @param loc - the location whose "Productivity value" we are querying
 	 * @return the total "Productivity value" of the parameter Location.
 	 * 
 	 *         It is most efficient to place products on tiles that have prod.
 	 *         vals. that indicate that a sufficient number of products are
 	 *         nearby in order to attract agent's business, but not enough are
 	 *         around so that there is too much competition. Therefore:
 	 * 
 	 *         -An agent's probability of dropping a product on a tile with low
 	 *         (~0) prod. val. should be low. (but not non-existant because then
 	 *         initially agents would never drop products) -An agent's
 	 *         probability of dropping a product on a tile with a very high
 	 *         prod. val. should be infinitesimal. -An agent should have a high
 	 *         chance of dropping a product on a tile with a moderate prob. val.
 	 * 
 	 */
 	public float getValueAtLocation(Environment.Location loc) {
 		return vals[loc.v[0]][loc.v[1]];
 	}
 
 	public float getValueAtLocation(int x, int y) {
 		Environment.Location loc = e.getLocation(x, y);
 		return getValueAtLocation(loc);
 	}
 
 	private Color[][] getTileColors(int x, int y) {
 		Color[][] ret = new Color[x][y];
 		for (int i = 0; i < x; i++) {
 			for (int j = 0; j < y; j++) {
 				float val = getValueAtLocation(i, j);
 				int amount = 255 - (int) ((Math.min(val, maxValue) / maxValue) * 255f);
				// FIXME: threading bug when speed set to max simulation speed, amount ends up out of range
				if (amount < 0 || amount > 255) {
					amount = 128;
				}
 				ret[i][j] = new Color(amount, amount, 255);
 			}
 		}
 		return ret;
 	}
 
 	public JDialog createDialog(final int x, final int y) {
 		return new Disp(x, y);
 	}
 
 	private class Disp extends JDialog implements Client {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 8153897860751883610L;
 		int x;
 		int y;
 
 		private Disp(int x, int y) {
 			super();
 			this.x = x;
 			this.y = y;
 			setModal(false);
 			setAlwaysOnTop(false);
 			setSize(400, 400);
 			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			setVisible(true);
 		}
 
 		@Override
 		public void paint(Graphics g) {
 			int w = getWidth();
 			int h = getHeight();
 			final float tw = (float) w / (float) x;
 			final float th = (float) h / (float) y;			
 
 			final Color[][] tiles = getTileColors(x, y);
 			for (int xc = 0; xc < x; xc++) {
 				for (int yc = 0; yc < y; yc++) {
 					g.setColor(tiles[xc][yc]);
 					g.fillRect((int) (xc * tw), (int) (yc * th), (int) tw, (int) th);
 				}
 			}
 		}
 
 		public void tickNotification(long time) {
 			repaint();
 		}
 
 		public void tickZero() {
 		}
 	}
 }
