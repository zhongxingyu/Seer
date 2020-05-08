 /*******************************************************************************
  * Copyright or  or Copr. Quentin Godron (2011)
  * 
  * cafe.en.grain@gmail.com
  * 
  * This software is a computer program whose purpose is to create zombie 
  * survival games on Bukkit's server. 
  * 
  * This software is governed by the CeCILL-C license under French law and
  * abiding by the rules of distribution of free software.  You can  use, 
  * modify and/ or redistribute the software under the terms of the CeCILL-C
  * license as circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info". 
  * 
  * As a counterpart to the access to the source code and  rights to copy,
  * modify and redistribute granted by the license, users are provided only
  * with a limited warranty  and the software's author,  the holder of the
  * economic rights,  and the successive licensors  have only  limited
  * liability. 
  * 
  * In this respect, the user's attention is drawn to the risks associated
  * with loading,  using,  modifying and/or developing or reproducing the
  * software by the user in light of its specific status of free software,
  * that may mean  that it is complicated to manipulate,  and  that  also
  * therefore means  that it is reserved for developers  and  experienced
  * professionals having in-depth computer knowledge. Users are therefore
  * encouraged to load and test the software's suitability as regards their
  * requirements in conditions enabling the security of their systems and/or 
  * data to be ensured and,  more generally, to use and operate it in the 
  * same conditions as regards security. 
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C license and that you accept its terms.
  ******************************************************************************/
 package graindcafe.tribu.Configuration;
 
 import graindcafe.tribu.Package;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 
 public class TribuDefaultConfiguration {
 	/*
 	 * Configuration
 	 */
 	/*
 	 * Plugin mode
 	 */
 	public boolean PluginModeServerExclusive=false;
	//Not used yet
 	public boolean PluginModeWorldExclusive=false;
	
 	public String PluginModeLanguage="english";
 	public boolean PluginModeAutoStart=false;
 	public String PluginModeDefaultLevel="";
 	
 	/*
 	 * Level related
 	 */
 	public double LevelClearZone=50d;
 	public boolean LevelJail=true;
 	public double LevelJailRadius=5.0;
 	public int LevelStartDelay=10;
 	/*
 	 * Wave related
 	 */
 	public boolean WaveStartSetTime=true;
 	public int WaveStartSetTimeTo=37000;
 	public int WaveStartDelay=10;
 	public boolean WaveStartTeleportPlayers=false;
 	public boolean WaveStartHealPlayers=false;
 	public int WaveStartMinPlayers=1;
 	
 	/*
 	 * Zombies
 	 */
 	public List<Double> ZombiesQuantity=Arrays.asList(0.5, 1.0, 1.0);
 	public List<Double> ZombiesHealth=Arrays.asList(0.5, 4.0);
 	public List<Double> ZombiesDamage=Arrays.asList(4.0);
 	public boolean ZombiesFireResistant=false;
 	public FocusType ZombiesFocus=FocusType.None;
 	public List<Double> ZombiesTimeToSpawn=Arrays.asList(1.0);
 	public boolean ZombiesSpeedRandom = true;
 	public float ZombiesSpeedBase = 1f;
 	public float ZombiesSpeedRush = 1f;
 	/*
 	 * Stats
 	 */
 	public int StatsOnZombieKillMoney=15;
 	public int StatsOnZombieKillPoints=10;
 	public int StatsOnPlayerDeathMoney=10000;
 	public int StatsOnPlayerDeathPoints=50;
 	public String StatsRewardMethod="Best";
 	public boolean StatsRewardOnlyAlive=false;
 	/*
 	 * Players
 	 */
 	public boolean PlayersDontLooseItem = false;
 	public boolean PlayersStoreInventory = false;
 	public boolean PlayersRollback = true;
 	public boolean PlayersAllowBreak = true;
 	public boolean PlayersAllowPlace = true;
 	
 	/* Default Packages */
 	public LinkedList<Package> DefaultPackages=null;
 	/* Advanced */
 	public int AdvancedRestoringSpeed=85;
 	public Map<String, Object> toMap()
 	{
 		HashMap<String, Object> map = new HashMap<String, Object>() {
 			private static final long serialVersionUID = 1L;
 		
 
 			{
 				put("PluginMode.WorldExclusive", PluginModeWorldExclusive);
 				put("PluginMode.ServerExclusive", PluginModeServerExclusive);
 				put("PluginMode.Language", PluginModeLanguage);
 				put("PluginMode.AutoStart", PluginModeAutoStart);
 				put("PluginMode.DefaultLevel", PluginModeDefaultLevel);
 				put("Level.ClearZone", LevelClearZone);
 				put("Level.Jail", LevelJail);
 				put("Level.JailRadius", LevelJailRadius);
 				put("Level.StartDelay", LevelJailRadius);
 				put("WaveStart.SetTime", WaveStartSetTime);
 				put("WaveStart.SetTimeTo", WaveStartSetTimeTo);
 				put("WaveStart.Delay", WaveStartDelay);
 				put("WaveStart.TeleportPlayers", WaveStartTeleportPlayers);
 				put("WaveStart.HealPlayers", WaveStartHealPlayers);
 				put("WaveStart.MinPlayers", WaveStartMinPlayers);
 				put("Zombies.Quantity", ZombiesQuantity);
 				put("Zombies.Health", ZombiesHealth);
 				put("Zombies.Health", ZombiesDamage);
 				put("Zombies.FireResistant", ZombiesFireResistant);
 				put("Zombies.Focus", ZombiesFocus);
 				put("Zombies.TimeToSpawn",ZombiesTimeToSpawn);
 				put("Zombies.Speed.Random",ZombiesSpeedRandom);
 				put("Zombies.Speed.Base",ZombiesSpeedBase);
 				put("Zombies.Speed.Rush",ZombiesSpeedRush);
 				put("Stats.OnZombieKill.Money", StatsOnZombieKillMoney);
 				put("Stats.OnZombieKill.Points", StatsOnZombieKillPoints);
 				put("Stats.OnPlayerDeath.Money", StatsOnPlayerDeathMoney);
 				put("Stats.OnPlayerDeath.Points", StatsOnPlayerDeathPoints);
 				put("Stats.RewardMethod",StatsRewardMethod);
 				put("Stats.RewardOnlyAlive",StatsRewardOnlyAlive);
 				put("Players.DontLooseItem", PlayersDontLooseItem);
 				put("Players.StoreInventory", PlayersStoreInventory);
				put("Players.RevertBlocksChanges", PlayersRollback);
 				put("Players.AllowBreak", PlayersAllowBreak);
 				put("Players.AllowPlace", PlayersAllowPlace);
 				//put("Signs.ShopSign.DropItem", true);
 				put("DefaultPackages", DefaultPackages);
 			}
 		};
 		return map;
 	}
 	
 	
 }
