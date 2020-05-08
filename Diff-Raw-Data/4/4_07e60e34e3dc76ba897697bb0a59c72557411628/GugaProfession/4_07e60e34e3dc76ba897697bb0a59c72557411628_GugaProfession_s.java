 package me.Guga.Guga_SERVER_MOD;
 
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 
 
 public class GugaProfession 
 {
 	public GugaProfession()
 	{
 		xpIncrement = 2;
 		xpNeeded = 500;
 		lvlCap = 10000;
 		xpCap = 4000;
 		skillCap=1000;
 	}
 	public GugaProfession(String pName, int exp, Guga_SERVER_MOD gugaSM)
 	{
 		xpIncrement = 2;
 		xpNeeded = 500;
 		lvlCap = 10000;
 		xpCap = 4000;
 		thisLevel = 500;
 		skillCap=1000;
 		plugin = gugaSM;
 		playerName = pName;
 		level = 1;
 		xp = exp;
 		int xpNeededOld;
 		while (xp >= xpNeeded)
 		{
 			xpNeededOld = xpNeeded;
 			if (level>=lvlCap)
 			{
 				xp=xpNeeded;
 				break;
 			}
 			level++;
 			if (xpNeeded>=xpCap)
 			{
 				xpNeeded +=xpCap;
 			}
 			else
 			{
 			xpNeeded = xpNeeded * xpIncrement;
 			}
 			int diff = xpNeeded - xpNeededOld;
 			if (diff > 0)
 				thisLevel = diff;
 		}
 		UpdateSkills();
 	}
 	protected void LevelUp()
 	{
 		level++;
 		int xpNeededOld = xpNeeded;
 		if (CanLevelUp())
 		{
 			if (xpNeeded>=xpCap)
 			{
 				xpNeeded +=xpCap;
 				
 			}
 			else
 			{
 				xpNeeded = xpNeeded * xpIncrement;
 			}
 		}
 		thisLevel = xpNeeded - xpNeededOld;
 		plugin.getServer().broadcastMessage(plugin.getServer().getPlayer(playerName).getName() + " prekrocil/a level " + level + "!");
 		if(level >= 10 && BasicWorld.IsBasicWorld(plugin.getServer().getPlayer(playerName).getLocation()))
 		{
 			plugin.getServer().getPlayer(playerName).sendMessage(ChatColor.GREEN + "Nyni muzete vstoupit do profesionalniho sveta.");
 			plugin.getServer().getPlayer(playerName).sendMessage(ChatColor.GREEN + "Dokazal jste povahu skveleho hrace.");
 			plugin.getServer().getPlayer(playerName).sendMessage(ChatColor.GREEN + "Pro opusteni zakladniho sveta napiste "+ ChatColor.YELLOW	 + "/world");
 		}
 		//if (level <= 20)
 		//{
 			UpdateSkills();
 		//}
 	}
 	public void UpdateSkills()
 	{
 		int newIron;
 		int newGold;
 		int newDiamond;
 		int newEmerald;
 		double factor;
 		if (level > skillCap)
 		{
 			newIron = skillCap/10;
 			newGold = skillCap/20;
 			newDiamond = skillCap/50;
 			newEmerald = skillCap/1000;
 			
 			factor = (double)1 + ((double)skillCap / (double)1000);
 		}
 		else
 		{
 			newIron = level/10;
 			newGold = level/20;
 			newDiamond = level/50;
 			newEmerald = level/1000;
 			
 			factor = (double)1 + ((double)level / (double)1000);
 		}
 		
 		ironChance = (int)Math.round((double)newIron/factor);
 		goldChance = (int)Math.round((double)newGold/factor);
 		diamondChance = (int)Math.round((double)newDiamond/factor);
 		emeraldChance = (int)Math.round((double)newEmerald/factor);
 	}
 	public GugaBonusDrop CobbleStoneDrop()
 	{
 		Random rnd = new Random();
 		int rNum = rnd.nextInt(1000);
 		if (rNum < emeraldChance)
 		{
 			return GugaBonusDrop.EMERALD;
 		}
 		if (rNum < diamondChance)
 		{
 			return GugaBonusDrop.DIAMOND;
 		}
 		else if (rNum < goldChance)
 		{
 			return GugaBonusDrop.GOLD;
 		}
 		else if (rNum < ironChance)
 		{
 			return GugaBonusDrop.IRON;
 		}
 		else
 		{
 			return GugaBonusDrop.NOTHING;
 		}
 	}
 	public int[] GetChances()
 	{
 		int chances[] = new int[4];
 		chances[0] = ironChance;
 		chances[1] = goldChance;
 		chances[2] = diamondChance;
 		chances[3] = emeraldChance;
 		return chances;
 	}
 	protected boolean ReachedNewLevel()
 	{
 		if (xp >= xpNeeded)
 		{
 			return true;
 		}
 		return false;
 	}
 	protected boolean CanLevelUp()
 	{
 		if (level<lvlCap)
 		{
 			return true;
 		}
 		if (xp>xpNeeded)
 		{
 			xp = xpNeeded;
 		}
 		return false;
 	}
 	public int GetLevel()
 	{
 		return level;
 	}
 	public int GetXp()
 	{
 		return xp;
 	}
 	public int GetXpNeeded()
 	{
 		return xpNeeded;
 	}
 	public int GetLvlCap()
 	{
 		return lvlCap;
 	}
 	public String GetPlayerName()
 	{
 		return playerName;
 	}
 	public void GainExperience(int exp)
 	{
 		if (CanLevelUp())
 		{
 			xp = xp+exp;
 			//MapXpBar();
 			CheckIfDinged();
 		}
 	}
 	public void CheckIfDinged()
 	{
 		if (ReachedNewLevel())
 		{
 			LevelUp();
 		}
 	}
 	public String GetProfession()
 	{
 		return "Profession";
 	}
 	protected int xp;
 	protected int xpNeeded;
 	protected int xpIncrement;
 	protected int xpCap;
 	protected int skillCap;
 	
 	protected int thisLevel;
 	
 	protected String playerName;
 	
 	protected int level;
 	protected int lvlCap;
 	
 	
 	int ironChance;
 	int goldChance;
 	int diamondChance;
 	int emeraldChance;
 	
 	
 	protected Guga_SERVER_MOD plugin;
 }
