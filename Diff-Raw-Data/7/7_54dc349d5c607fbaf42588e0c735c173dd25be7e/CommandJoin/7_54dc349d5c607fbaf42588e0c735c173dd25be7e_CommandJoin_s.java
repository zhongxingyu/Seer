 package me.naithantu.ArenaPVP.Commands;
 
 import me.naithantu.ArenaPVP.ArenaManager;
 import me.naithantu.ArenaPVP.ArenaPVP;
 import me.naithantu.ArenaPVP.Arena.Arena;
 import me.naithantu.ArenaPVP.Arena.ArenaTeam;
 import me.naithantu.ArenaPVP.Arena.ArenaExtras.ArenaState;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CommandJoin extends AbstractCommand {
 
 	protected CommandJoin(CommandSender sender, String[] args, ArenaPVP plugin, ArenaManager arenaManager) {
 		super(sender, args, plugin, arenaManager);
 	}
 
 	@Override
 	public boolean handle() {
 		if(!testPermission(sender, "join") && !testPermission(sender, "player")){
 			this.noPermission(sender);
 			return true;
 		}
 		
 		if (!(sender instanceof Player)) {
 			this.msg(sender, "That command can only be used in-game.");
 			return true;
 		}
 		
 		if(arenaManager.getPlayerByName(sender.getName()) != null){
 			this.msg(sender, "You are already in a game, you can leave with /pvp leave!");
 			return true;
 		}
 
 		Player player = (Player) sender;
 
 		//Check how many arenas the player can join.
 		int availableArenas = 0;
 		for (Arena arena : arenaManager.getArenas().values()) {
 			if (arena.getArenaState() == ArenaState.LOBBY || arena.getArenaState() == ArenaState.WARMUP) {
 				availableArenas++;
 			}
 		}
 		
 		if (availableArenas == 0) {
 			this.msg(sender, "There are currently no arenas that you can join.");
 			return true;
 		}
 
 		if (availableArenas > 1) {
 			// If there are several arenas, find out what arena players want to join.
 			if (args.length == 0) {
 				this.msg(sender, "There are currently several arenas to join, please specify the arena you want to join.");
 				this.msg(sender, "/pvp join <arenaname> <teamname>");
 				return true;
 			}
 
 			if(arenaManager.getArenas().containsKey(args[0])){
 				Arena arena = arenaManager.getArenas().get(args[0]);
 				if (args.length >= 2) {
 					joinArena(arena, player, arena.getTeam(args[1]));
 				} else {
 					joinArena(arena, player, null);
 				}
 			} else {
 				this.msg(sender, "No arena with given name was found, type /pvp arenas to see available arenas.");
 			}
 			return true;
 		} else {
 			//If there is only arena
 			Arena arena = arenaManager.getFirstArena();
 			if (args.length > 0) {
 				try{
 					int teamNumber = Integer.parseInt(args[0]);
 					joinArena(arena, player, arena.getTeam(teamNumber));
 				} catch (NumberFormatException e){
 					joinArena(arena, player, arena.getTeam(args[0]));
 				}
 			} else {
 				joinArena(arena, player, null);
 			}
 		}
 		return true;
 	}
 	
 	public void joinArena(Arena arena, Player player, ArenaTeam team){
 		if(!arena.joinGame(player, team)){
 			this.msg(sender, "You were unable to join arena " + arena.getArenaName() + ", try a different arena!");
 		}
 	}
 }
