import java.text.DateFormat;
import java.text.SimpleDateFormat;
 import java.util.ConcurrentModificationException;
import java.util.Date;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 class RemindTask extends TimerTask
 {
 	Timer timer;
 	s3HealthListener parent;
 	
 	public RemindTask(s3HealthListener parent)
 	{
 		this.parent = parent;
 	}
 
 	public void DoMobCombat(Mob m, Player p, int basedamage, double mindistance)
 	{
 		double dist = getDistance(p, m);
 		int playerentry = 0;
 		if (dist <= mindistance)
 		{
 			Random generator = new Random();
 			int index = generator.nextInt(basedamage);
 			int thisdmg = index;
 			int thishp = parent.getPlayerHP(p) - thisdmg;
 			if (thishp < 1)
 			{
 				if (m.getHealth() == 0)
 				{
 					// do nothing
 				} else {
 					p.sendMessage("You were hit by a " + m.getName() + " for " + thisdmg + " damage! (HP: 0)");
 	
 					// reset hp and warp to spawn
 					parent.DoPlayerDeath(p);
 				}
 			} else {
 				if (m.getHealth() == 0)
 				{
 					// do nothing
 				} else {
 					parent.setPlayerHP(p, thishp);
 					
 					p.sendMessage("You were hit by a " + m.getName() + " for " + thisdmg + " damage! (HP: " + thishp + ")");
 				}
 			}
 
 		}
 	}
 
 
 	public void run()
 	{
 
 		try {
 
 			for (Player p : etc.getServer().getPlayerList()) {
 				Location l = p.getLocation();
				int id = etc.getServer().getBlockIdAt((int)l.x, (int)l.y, (int)(l.z)-1);
				if (id == 10 || id == 11)
 				{
 					if (parent.getPlayerHP(p) > 2)
 					{
 						parent.setPlayerHP(p, Integer.valueOf(parent.getPlayerHP(p) - 2));
						p.sendMessage("The lava burns you! (HP: " + parent.getPlayerHP(p));
 					} else {
 						parent.DoPlayerDeath(p);
 					}
 				}
 			}
 
 			for (Mob m : etc.getServer().getMobList()) {
 				if (m == null)
 					continue;
 
 				if (m.getName().equals("Spider") == true)
 				{
 					for (Player p : etc.getServer().getPlayerList()) {
 						DoMobCombat(m, p, 2, 2.5D);
 					}
 				}
 	
 				if (m.getName().equals("Zombie") == true)
 				{
 					for (Player p : etc.getServer().getPlayerList()) {
 						DoMobCombat(m, p, 4, 2.5D);
 					}
 				}
 
 				if (m.getName().equals("Creeper") == true)
 				{
 					for (Player p : etc.getServer().getPlayerList()) {
 						DoMobCombat(m, p, 10, 1.5D);
 					}
 				}
 				
 				if (m.getName().equals("Skeleton") == true)
 				{
 					for (Player p : etc.getServer().getPlayerList()) {
 						DoMobCombat(m, p, 3, 2.5D);
 					}
 				}
 			}
 		}
 		catch(ConcurrentModificationException e)
 		{
 			// array modified mid use, skip for next turn
 		}
 	}
 
 	private double getDistance(Player a, Mob b)
 	{
 		double xPart = Math.pow(a.getX() - b.getX(), 2.0D);
 		double yPart = Math.pow(a.getY() - b.getY(), 2.0D);
 		double zPart = Math.pow(a.getZ() - b.getZ(), 2.0D);
 		return Math.sqrt(xPart + yPart + zPart);
 	}
 }
