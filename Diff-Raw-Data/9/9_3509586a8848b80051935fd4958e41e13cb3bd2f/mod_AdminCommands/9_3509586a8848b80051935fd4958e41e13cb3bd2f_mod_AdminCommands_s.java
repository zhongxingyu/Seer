 package btwmod.admincommands;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.minecraft.src.EntityPlayerMP;
 import net.minecraft.src.Packet;
 import net.minecraft.src.Packet102WindowClick;
 import net.minecraft.src.Packet12PlayerLook;
 import net.minecraft.src.Packet13PlayerLookMove;
 import net.minecraft.src.Packet14BlockDig;
 import net.minecraft.src.Packet15Place;
 import net.minecraft.src.Packet3Chat;
 import btwmods.CommandsAPI;
 import btwmods.IMod;
 import btwmods.NetworkAPI;
 import btwmods.io.Settings;
 import btwmods.network.IPacketListener;
 import btwmods.network.PacketEvent;
 
 public class mod_AdminCommands implements IMod, IPacketListener {
 	
 	private long secondsForAFK = 300;
	private final Map<EntityPlayerMP, Long> lastPlayerAction = new HashMap<EntityPlayerMP, Long>();
 	
 	private CommandWho commandWho;
 	//private DumpTrackedCommand dumpTrackedCommand;
 	private CommandReliableUpdates commandReliableUpdates;
 	private CommandDumpEntities commandDumpEntities;
 	private CommandClearEntities commandClearEntities;
 
 	@Override
 	public String getName() throws Exception {
 		return "Admin Commands";
 	}
 	
 	public long getSecondsForAFK() {
 		return secondsForAFK;
 	}
 
 	@Override
 	public IMod getMod() {
 		return this;
 	}
 
 	@Override
 	public void init(Settings settings) throws Exception {
 		NetworkAPI.addListener(this);
 		CommandsAPI.registerCommand(commandWho = new CommandWho(this), this);
 		//CommandsAPI.registerCommand(dumpTrackedCommand = new DumpTrackedCommand(), this);
 		CommandsAPI.registerCommand(commandReliableUpdates = new CommandReliableUpdates(), this);
 		CommandsAPI.registerCommand(commandDumpEntities = new CommandDumpEntities(settings), this);
 		CommandsAPI.registerCommand(commandClearEntities = new CommandClearEntities(), this);
 
 		// Load settings
 		if (settings.isLong("secondsforafk")) {
 			secondsForAFK = settings.getLong("secondsforaFK");
 		}
 	}
 
 	@Override
 	public void unload() throws Exception {
 		NetworkAPI.removeListener(this);
 		CommandsAPI.unregisterCommand(commandWho);
 		//CommandsAPI.unregisterCommand(dumpTrackedCommand);
 		CommandsAPI.unregisterCommand(commandReliableUpdates);
 		CommandsAPI.unregisterCommand(commandDumpEntities);
 		CommandsAPI.unregisterCommand(commandClearEntities);
 	}
 
 	@Override
 	public void packetAction(PacketEvent event) {
 		Packet packet = event.getPacket();
 		if (packet instanceof Packet12PlayerLook
 				|| packet instanceof Packet13PlayerLookMove
 				|| packet instanceof Packet102WindowClick
 				|| packet instanceof Packet14BlockDig
 				|| packet instanceof Packet15Place
 				|| packet instanceof Packet3Chat) {
			
			lastPlayerAction.put(event.getPlayer(), new Long(System.currentTimeMillis()));
 		}
 	}
 	
 	public boolean isPlayerAFK(EntityPlayerMP player) {
 		return getTimeSinceLastPlayerAction(player) >= secondsForAFK;
 	}
 	
 	public long getTimeSinceLastPlayerAction(EntityPlayerMP player) {
		Long timeSinceLastAction = lastPlayerAction.get(player);
 		return timeSinceLastAction == null ? 0 : (System.currentTimeMillis() - timeSinceLastAction.longValue()) / 1000L;
 	}
 }
