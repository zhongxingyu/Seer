 package org.rsbot.script.web;
 
 import org.rsbot.script.methods.Magic;
 import org.rsbot.script.methods.MethodContext;
 import org.rsbot.script.methods.MethodProvider;
 import org.rsbot.script.web.methods.TeleportRunes;
 import org.rsbot.script.web.methods.TeleportTab;
 import org.rsbot.script.wrappers.RSTile;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * The class that handles all activities.
  *
  * @author Timer
  */
 public class TransportationHandler extends MethodProvider {
 	private List<Teleport> teleports = new ArrayList<Teleport>();
 
 	public TransportationHandler(final MethodContext ctx) {
 		super(ctx);
 		Tablets tablets = new Tablets();
 		Runes runes = new Runes();
 		teleports.add(tablets.CAMELOT);
 		teleports.add(tablets.VARROCK);
 		teleports.add(runes.CAMELOT);
 		teleports.add(runes.VARROCK);
 	}
 
 	public boolean canTeleport(final RSTile destination) {
 		Iterator<Teleport> teleportIterator = teleports.listIterator();
 		while (teleportIterator.hasNext()) {
 			Teleport teleport = teleportIterator.next();
 			if (teleport.meetsPrerequisites() && teleport.isApplicable(methods.players.getMyPlayer().getLocation(), destination)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public Teleport getTeleport(final RSTile destination) {
 		Teleport bestTeleport = null;
 		double dist = 0.0D;
 		Iterator<Teleport> teleportIterator = teleports.listIterator();
 		while (teleportIterator.hasNext()) {
 			Teleport teleport = teleportIterator.next();
 			if (teleport.meetsPrerequisites() && teleport.isApplicable(methods.players.getMyPlayer().getLocation(), destination)) {
				if (dist == 0.0D || teleport.getDistance(destination) < dist) {
 					dist = teleport.getDistance(destination);
 					bestTeleport = teleport;
 				}
 			}
 		}
 		if (bestTeleport != null) {
 			return bestTeleport;
 		}
 		return null;
 	}
 
 	private class Tablets {
 		public final TeleportTab VARROCK = new TeleportTab(methods, 8007, new RSTile(3212, 3428, 0));
 		public final TeleportTab CAMELOT = new TeleportTab(methods, 8010, new RSTile(2757, 3478, 0));
 	}
 
 	private class Runes {
 		public static final int LAW_RUNE = 563, FIRE_RUNE = 554, AIR_RUNE = 556;
 		public TeleportRunes VARROCK, CAMELOT;
 
 		public Runes() {
 			try {
				VARROCK = new TeleportRunes(methods, Magic.SPELL_VARROCK_TELEPORT, new RSTile(3212, 3428, 0), new int[]{LAW_RUNE, FIRE_RUNE, AIR_RUNE}, new int[]{1, 1, 3});
				CAMELOT = new TeleportRunes(methods, Magic.SPELL_CAMELOT_TELEPORT, new RSTile(2757, 3478, 0), new int[]{LAW_RUNE, AIR_RUNE}, new int[]{1, 5});
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
