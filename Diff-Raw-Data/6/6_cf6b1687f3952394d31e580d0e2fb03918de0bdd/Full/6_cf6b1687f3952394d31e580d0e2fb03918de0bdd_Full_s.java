 package com.github.generaldon.Drugs;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.github.generaldon.Druggie;
 import com.github.generaldon.SLAPI;
 import com.github.generaldon.General.ConfigManager;
 
 public class Full {
 	
 	int l = 0;
 	
 	String[][] LANGUAGE_DATA = new String[4][30];
 	  String ROVIE_NAME = "rovie";
 	  int ROVIE_POWER = 10;
 	  int LANGUAGE = 0;
 	  String MURK_NAME = "murk";
 	  String SHROOMS_NAME = "shrooms";
 	  String BATHOL_NAME = "bathol";
 	  String ANTITRIP_NAME = "anti-trip";
 	  String ANTIADDICTION_NAME = "anti-addiction";
 	  int ANTIADDICTION_POWER = 5;
 	  FileConfiguration config;
 	  
 	
 	  public String getDAT(int col, int MULTIPLIER)
 	  {
 	    String RESULT = this.LANGUAGE_DATA[this.LANGUAGE][col];
 	    RESULT = RESULT.replaceAll("%murk%", ChatColor.DARK_GREEN + ConfigManager.Murk() + ChatColor.WHITE);
 	    if (MULTIPLIER > 0)
	    	String.valueOf(MULTIPLIER);
	      RESULT = RESULT.replaceAll("%mult%", MULTIPLIER);
 	    return RESULT;
 	  }
 	  public void FILL_ARRAYS() {
 		    this.LANGUAGE_DATA[0][0] = "You feel amazing as you're instantly no longer feel addicted to %rovie%";
 		    this.LANGUAGE_DATA[0][1] = "You are feeling a bit too wasted to do any drugs right now.";
 		    this.LANGUAGE_DATA[0][2] = "You feel a rush of energy surging through your body as you snort a %mult%g rock of %rovie%.";
 		    this.LANGUAGE_DATA[0][3] = "You feel a tad dizzy as you gulp down a litre of %bathol%.";
 		    this.LANGUAGE_DATA[0][4] = "Oh no, the liquid has somehow made you temporarily blind!";
 		    this.LANGUAGE_DATA[0][5] = "You feel incredibly relaxed, as you smoke %mult% spliff(s) of %murk%.";
 		    this.LANGUAGE_DATA[0][6] = "You found a good batch of %shrooms%.";
 		    this.LANGUAGE_DATA[0][7] = "You found a neutral batch of %shrooms%.";
 		    this.LANGUAGE_DATA[0][8] = "You found a poisonous batch of %shrooms%.";
 		    this.LANGUAGE_DATA[0][9] = "You don't smoke %murk% by right-clicking on a block - click somewhere else to smoke it.";
 		    this.LANGUAGE_DATA[0][10] = "You don't swallow pills by right-clicking on a block - click somewhere else to swallow them.";
 		    this.LANGUAGE_DATA[0][11] = "You don't snort %rovie% by right-clicking on a block - click somewhere else to snort it.";
 		    this.LANGUAGE_DATA[0][12] = "You don't drink %bathol% by right-clicking on a block - click somewhere else to gulp it down.";
 		    this.LANGUAGE_DATA[0][13] = "You are feeling a bit too wasted to smoke any %murk%.";
 		    this.LANGUAGE_DATA[0][14] = "There's really no point in taking %anti-trip% pills when you're not high.";
 		    this.LANGUAGE_DATA[0][15] = "You feel relieved as all the effects of the drugs are removed.";
 		    this.LANGUAGE_DATA[0][16] = "There's really no point in taking %anti-addiction% pills when you're not addicted.";
 		    this.LANGUAGE_DATA[0][17] = "You are feeling a bit too wasted to swallow any %anti-addiction% pills.";
 		    this.LANGUAGE_DATA[0][18] = "You're left with an awful taste in your mouth as you swallow the purple-looking pill, but hurray! -you are no longer addicted to %rovie%.";
 		    this.LANGUAGE_DATA[0][19] = "You're left with an awful taste in your mouth as you swallow the purple-looking pill. You still need to take %mult% additional pills to cure your dependence.";
 		    this.LANGUAGE_DATA[0][20] = "You are feeling a bit too wasted to snort any %rovie%.";
 		    this.LANGUAGE_DATA[0][21] = "You are now a complete junkie. Congratulations!";
 		    this.LANGUAGE_DATA[0][22] = "You are now severely addicted to %rovie%.";
 		    this.LANGUAGE_DATA[0][23] = "You are now moderately addicted to %rovie%.";
 		    this.LANGUAGE_DATA[0][24] = "You are now slightly addicted to %rovie%.";
 		    this.LANGUAGE_DATA[0][25] = "You are feeling a bit too wasted to ingest any %shrooms%.";
 		    this.LANGUAGE_DATA[0][26] = "You are feeling a bit too wasted to gulp down any %bathol%.";
 		    this.LANGUAGE_DATA[0][27] = "You cannot take drugs while in Creative Mode.";
 		    this.LANGUAGE_DATA[0][28] = "You are suffering withdrawals from a lack of %rovie%.";
 		    this.LANGUAGE_DATA[0][29] = "You can't drug people if they're already high.";
 	  }
 	
 	@EventHandler
 	  public void useDrug(PlayerInteractEvent event) {
 	    Druggie.l += 1;
 	    Player p = event.getPlayer();
 	    if (!Druggie.Addict.containsKey(p.getName())) {
 	      Druggie.Addict.put(p.getName(), Integer.valueOf(0));
 	    }
 	    int i = ((Integer)Druggie.Addict.get(p.getName())).intValue();
 	    if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
 	      if (p.getGameMode() == GameMode.SURVIVAL) {
 	        if (Druggie.l == 5) {
 	          Druggie.l = 0;
 	          try {
 	            Druggie.logger.info("Saving Stimulatory data...");
 	            SLAPI.save(Druggie.Addict, "Stimulatory.DAT");
 	            Druggie.logger.info("Save succesful!");
 	          } catch (Exception e) {
 	            e.printStackTrace();
 	          }
 	        }
 	        if ((event.getMaterial() == Material.INK_SACK) && (p.getItemInHand().getData().getData() == 2) && (p.hasPermission("Stimulatory.murk")))
 	          if (p.hasPotionEffect(PotionEffectType.CONFUSION)) {
 	            p.sendMessage(getDAT(13, 0));
 	          } else {
 	            p.getActivePotionEffects().clear();
 	            p.playEffect(p.getLocation(), Effect.SMOKE, 7000);
 	            int MULTIPLIER = p.getInventory().getItemInHand().getAmount();
 	            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int)(750.0D * Math.floor(Math.sqrt(MULTIPLIER))), 1 * (MULTIPLIER / 5)));
 	            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int)(750.0D * Math.floor(Math.sqrt(MULTIPLIER))), 2 * MULTIPLIER));
 	            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 2 * MULTIPLIER));
 	            p.sendMessage(getDAT(5, MULTIPLIER));
	            Druggie.logger.info(p.getName() + " has smoked " + MULTIPLIER + " spliff(s) of " + ConfigManager.getMurky() + ".");
 	            p.setItemInHand(null);
 	          }
 }
 	    }
 	}
 }
