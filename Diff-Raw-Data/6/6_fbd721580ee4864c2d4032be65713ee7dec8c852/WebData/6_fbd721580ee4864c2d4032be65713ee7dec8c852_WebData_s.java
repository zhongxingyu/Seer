 package org.rsbot.script.background;
 
 import org.rsbot.script.BackgroundScript;
 import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSGameTile;
 import org.rsbot.script.methods.Web;
 import org.rsbot.script.wrappers.RSTile;
 import org.rsbot.service.WebQueue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @ScriptManifest(name = "Web Data Collector", authors = {"Timer"})
 public class WebData extends BackgroundScript {
 	private RSTile lb = null;
 	private int lp = -1;
 	public final List<RSGameTile> rs_map = new ArrayList<RSGameTile>();
 	private static final Object lock = new Object();
 
 	@Override
 	public boolean activateCondition() {
 		final RSTile curr_base = game.getMapBase();
 		final int curr_plane = game.getPlane();
		return game.isLoggedIn() && (lb == null || !lb.equals(curr_base)) || (lp == -1 || lp != curr_plane);
 	}
 
 	@Override
 	public int loop() {
 		try {
 			final RSTile curr_base = game.getMapBase();
 			final int curr_plane = game.getPlane();
 			rs_map.clear();
 			sleep(5000);
 			if (!curr_base.equals(game.getMapBase())) {
 				return -1;
 			}
 			lb = curr_base;
 			lp = curr_plane;
 			Node t;
 			final int flags[][] = walking.getCollisionFlags(curr_plane);
 			for (int i = 3; i < 102; i++) {
 				for (int j = 3; j < 102; j++) {
 					final RSTile start = new RSTile(curr_base.getX() + i, curr_base.getY() + j, curr_plane);
 					final int base_x = game.getBaseX(), base_y = game.getBaseY();
 					final int curr_x = start.getX() - base_x, curr_y = start.getY() - base_y;
 					t = new Node(curr_x, curr_y);
 					final RSTile offset = walking.getCollisionOffset(curr_plane);
 					final int off_x = offset.getX();
 					final int off_y = offset.getY();
 					final int x = t.x, y = t.y;
 					final int f_x = x - off_x, f_y = y - off_y;
 					final int here = flags[f_x][f_y];
 					RSGameTile gameTile = new RSGameTile(start, here);
 					synchronized (lock) {
 						if (!Web.map.contains(start) && !gameTile.walkable()) {
 							rs_map.add(gameTile);
 						} else {
 							try {
 								int indexOf = Web.map.indexOf(start);
 								if (indexOf != -1 && !Web.map.get(indexOf).equals(gameTile)) {
 									WebQueue.Remove(start);
 									lb = null;
 									lp = -1;
 								}
 							} catch (final NullPointerException ignored) {
 							}
 						}
 					}
 				}
 			}
 			WebQueue.Add(rs_map);
 			return -1;
 		} catch (final Exception ignored) {
 		}
 		return -1;
 	}
 
 	@Override
 	public int iterationSleep() {
 		return 1000;
 	}
 
 	private class Node {
 		public int x, y;
 
 		public Node(final int x, final int y) {
 			this.x = x;
 			this.y = y;
 		}
 	}
 }
