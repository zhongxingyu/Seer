 package me.minebuilders.portal.commands;
 
 import me.minebuilders.portal.IP;
 import me.minebuilders.portal.Status;
 import me.minebuilders.portal.Util;
 import me.minebuilders.portal.portals.Portal;
 
 public class ToggleCmd extends BaseCmd {
 
 	public ToggleCmd() {
 		forcePlayer = false;
 		cmdName = "toggle";
 		usage = "<name>";
 		argLength = 2;
 	}
 
 	@Override
 	public boolean run() {
 		IP plugin = IP.instance;
 		String name = args[1];
 		for (Portal p : plugin.portals) {
 			if (p.getName().equalsIgnoreCase(name)) {
 				if (p.getStatus() == Status.RUNNING) {
					Util.msg(player, "&a" + name + " is now &cLOCKED&a!");
 					p.setStatus(Status.NOT_READY);
 				} else {
					Util.msg(player, "&a" + name + " is now &2UNLOCKED&a!");
 					p.setStatus(Status.RUNNING);
 				}
 				return true;
 			}
 		}
		Util.msg(player, "&c" + name + " is not a valid portal!");
 		return true;
 	}
 }
