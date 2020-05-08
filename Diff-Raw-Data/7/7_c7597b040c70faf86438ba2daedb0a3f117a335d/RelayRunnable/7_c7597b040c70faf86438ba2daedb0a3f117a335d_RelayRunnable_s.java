 package net.slipcor.pvparena.modules.respawnrelay;
 
 import java.util.List;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaPlayer.Status;
 import net.slipcor.pvparena.core.Config.CFG;
 import net.slipcor.pvparena.core.Debug;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.managers.SpawnManager;
 import net.slipcor.pvparena.runnables.ArenaRunnable;
 import net.slipcor.pvparena.runnables.InventoryRefillRunnable;
 
 import org.bukkit.inventory.ItemStack;
 
 public class RelayRunnable extends ArenaRunnable {
 	private ArenaPlayer ap;
 	List<ItemStack> drops;
 	private Debug debug = new Debug(77);
 	private RespawnRelay mod;
 
 	public RelayRunnable(RespawnRelay relay, Arena arena, ArenaPlayer ap, List<ItemStack> drops) {
 		
 		super(MSG.TIMER_STARTING_IN.getNode(), arena.getArenaConfig().getInt(CFG.MODULES_RESPAWNRELAY_INTERVAL), ap.get(), null, false);
 		mod = relay;
 		this.ap = ap;
 		this.drops = drops;
 	}
 
 	@Override
 	protected void commit() {
 		debug.i("RelayRunnable commiting", ap.getName());
		new InventoryRefillRunnable(arena, ap.get(), drops);
 		String spawn = mod.overrideMap.get(ap.getName());
		SpawnManager.respawn(arena,  ap, spawn);
		arena.unKillPlayer(ap.get(), ap.get().getLastDamageCause()==null?null:ap.get().getLastDamageCause().getCause(), ap.get().getKiller());
 		ap.setStatus(Status.FIGHT);
 		mod.getRunnerMap().remove(ap.getName());
 		mod.overrideMap.remove(ap.getName());
 	}
 
 	@Override
 	protected void warn() {
 		PVPArena.instance.getLogger().warning("RelayRunnable not scheduled yet!");
 	}
 }
