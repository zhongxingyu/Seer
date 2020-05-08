 package com.adamki11s.npcs.triggers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 
 import com.adamki11s.io.FileLocator;
 import com.adamki11s.npcs.NPCHandler;
 import com.adamki11s.npcs.triggers.action.*;
 import com.adamki11s.sync.io.configuration.SyncConfiguration;
 
 public class CustomAction {
 
 	final String npcName;
 
 	private ArrayList<Action> actions = new ArrayList<Action>();
 
 	boolean invokesTorQ = false, isTask = false;
 
 	private Action invoke;
 
 	// auto ends conversation using this trigger
 
 	public CustomAction(String npcName) {
 		this.npcName = npcName;
 	}
 
 	public void invokeActions(Player p) {
 		if(this.actions.size() == 0){
 			return;
 		}
 		
 		if (invokesTorQ) {
 			if (isTask) {
 				InvokeTaskAction a = (InvokeTaskAction) invoke;
 				if (!a.canPlayerTriggerTask(p)) {
 					return;
 				}
 			} else {
 				InvokeQuestAction a = (InvokeQuestAction) invoke;
 				if (!a.canPlayerTriggerQuest(p)) {
 					return;
 				}
 			}
 		}
 
 		for (Action a : actions) {
 			if (a.isActive()) {
 				a.implement(p);
 			}
 		}
 	}
 
 	public void load(NPCHandler handle) {
 		File f = FileLocator.getCustomTriggerFile(this.npcName);
 		SyncConfiguration io = new SyncConfiguration(f);
		
 
 		if (!f.exists()) {
 			try {
 				f.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else {
			
			io.read();
 
 			if (io.doesKeyExist("DAMAGE_PLAYER")) {
 				Action a = new DamagePlayerAction(npcName, io.getString("DAMAGE_PLAYER"));
 				if (a.isActive()) {
 					actions.add(a);
 				}
 			}
 
 			if (io.doesKeyExist("INVOKE_QUEST")) {
 				Action a = new InvokeQuestAction(handle, io.getString("INVOKE_QUEST"));
 				if (a.isActive()) {
 					invokesTorQ = true;
 					isTask = false;
 					invoke = a;
 				}
 			}
 
 			if (io.doesKeyExist("INVOKE_TASK")) {
 				Action a = new InvokeTaskAction(npcName);
 				if (a.isActive()) {
 					invokesTorQ = true;
 					isTask = true;
 					invoke = a;
 				}
 			}
 
 			if (io.doesKeyExist("LIGHTNING")) {
 				Action a = new DamagePlayerAction(npcName, io.getString("LIGHTNING"));
 				if (a.isActive()) {
 					actions.add(a);
 				}
 			}
 
 			if (io.doesKeyExist("SPAWN_MOBS") && io.doesKeyExist("SPAWN_MOB_RANGE") && io.doesKeyExist("SPAWN_COOLDOWN_MINUTES") && io.doesKeyExist("DESPAWN_MOB_SECONDS")
 					&& io.doesKeyExist("MOBS_TARGET_PLAYER")) {
 				String[] data = new String[5];
 				data[0] = io.getString("SPAWN_MOBS");
 				data[1] = io.getString("SPAWN_MOB_RANGE");
 				data[2] = io.getString("SPAWN_COOLDOWN_MINUTES");
 				data[3] = io.getString("DESPAWN_MOB_SECONDS");
 				data[4] = io.getString("MOBS_TARGET_PLAYER");
 
 				Action a = new MobSpawnAction(npcName, data);
 				if (a.isActive()) {
 					actions.add(a);
 				}
 			}
 
 			if (io.doesKeyExist("ATTACK_PLAYER")) {
 				Action a = new NPCAttackPlayerAction(handle, npcName, io.getString("ATTACK_PLAYER"));
 				if (a.isActive()) {
 					actions.add(a);
 				}
 			}
 
 			if (io.doesKeyExist("PLAYER_GIVE_ITEMS")) {
 				Action a = new PlayerGiveItemsAction(npcName, io.getString("PLAYER_GIVE_ITEMS"));
 				if (a.isActive()) {
 					actions.add(a);
 				}
 			}
 
 			if (io.doesKeyExist("POTION_EFFECT")) {
 				Action a = new PotionEffectAction(npcName, io.getString("POTION_EFFECT"));
 				if (a.isActive()) {
 					actions.add(a);
 				}
 			}
 
 			if (io.doesKeyExist("TELEPORT_PLAYER")) {
 				Action a = new TeleportAction(npcName, io.getString("TELEPORT_PLAYER"));
 				if (a.isActive()) {
 					actions.add(a);
 				}
 			}
 
 		}
 		/*
 		 * Properties
 		 * 
 		 * SET_VELOCITY:x,y,z
 		 */
 	}
 
 }
