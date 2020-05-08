 package jk_5.nailed.server.command;
 
 import com.google.common.collect.Lists;
 import jk_5.nailed.api.NailedAPI;
 import jk_5.nailed.api.map.Map;
 import jk_5.nailed.api.map.Mappack;
 import jk_5.nailed.api.map.Spawnpoint;
 import jk_5.nailed.api.map.teleport.TeleportOptions;
 import jk_5.nailed.api.player.Player;
 import jk_5.nailed.map.teleport.TeleportHelper;
 import net.minecraft.command.CommandBase;
 import net.minecraft.command.CommandException;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.ChatComponentTranslation;
 import org.apache.commons.lang3.tuple.ImmutablePair;
 import org.apache.commons.lang3.tuple.Pair;
 
 import java.util.List;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class CommandTP extends NailedCommand {
 
     public CommandTP(){
         super("tp");
     }
 
     @Override
     public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] args){
         if(args.length == 1) return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getConfigurationManager().getAllUsernames());
         if(args.length == 2) return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getConfigurationManager().getAllUsernames());
         return null;
     }
 
     @Override
     public void processCommandPlayer(Player sender, Map map, String[] args){
         super.processCommandPlayer(sender, map, args);
     }
 
     @Override
     public void process(ICommandSender sender, String[] args){
         List<Pair<Entity, TeleportOptions>> options = Lists.newArrayList();
         try{
             if(args.length == 1){
                 //  /tp destinationPlayer
                 EntityPlayer teleporting;
                 if(sender instanceof EntityPlayer){
                     teleporting = (EntityPlayer) sender;
                 }else{
                     throw new CommandException("commands.nailed.tp.fail.notarget");
                 }
                 Player target = NailedAPI.getPlayerRegistry().getPlayerByUsername(args[0]);
                 if(target == null){
                     throw new CommandException("commands.nailed.tp.fail.notarget");
                 }
                 TeleportOptions option = new TeleportOptions();
                 option.setCoordinates(target.getLocation());
                 option.setDestination(target.getCurrentMap());
                 options.add(new ImmutablePair<Entity, TeleportOptions>(teleporting, option));
             }else if(args.length == 2){
                 //  /tp teleportingPlayer destinationPlayer | destinationMap
                 EntityPlayerMP[] players = getPlayersList(sender, args[0]);
                 TeleportOptions option = getDestination(sender, args[1]);
                 for(EntityPlayerMP player : players){
                     options.add(new ImmutablePair<Entity, TeleportOptions>(player, option));
                 }
             }else if(args.length == 3){
                 //  /tp x y z
                 EntityPlayer teleporting;
                 if(sender instanceof EntityPlayer){
                     teleporting = (EntityPlayer) sender;
                 }else{
                     throw new CommandException("commands.nailed.tp.fail.notarget");
                 }
                 int x = (int) Math.floor(handleRelativeNumber(sender, teleporting.posX, args[0]));
                 int y = (int) Math.floor(handleRelativeNumber(sender, teleporting.posY, args[1], 0, 0));
                 int z = (int) Math.floor(handleRelativeNumber(sender, teleporting.posZ, args[2]));
                 TeleportOptions option = new TeleportOptions();
                 option.setCoordinates(new Spawnpoint(x, y, z, teleporting.rotationYaw, teleporting.rotationPitch));
                 option.setDestination(NailedAPI.getMapLoader().getMap(teleporting.worldObj));
                 options.add(new ImmutablePair<Entity, TeleportOptions>(teleporting, option));
             }else if(args.length == 4){
                 //  /tp teleportingPlayer x y z
                Player target = NailedAPI.getPlayerRegistry().getPlayerByUsername(args[0]);
                if(target == null){
                    throw new CommandException("commands.nailed.tp.fail.notarget");
                }
                 EntityPlayerMP[] players = getPlayersList(sender, args[0]);
                 for(EntityPlayerMP player : players){
                     int x = (int) Math.floor(handleRelativeNumber(sender, player.posX, args[1]));
                     int y = (int) Math.floor(handleRelativeNumber(sender, player.posY, args[2], 0, 0));
                     int z = (int) Math.floor(handleRelativeNumber(sender, player.posZ, args[3]));
                     TeleportOptions option = new TeleportOptions();
                     option.setCoordinates(new Spawnpoint(x, y, z, player.rotationYaw, player.rotationPitch));
                    option.setDestination(target.getCurrentMap());
                     options.add(new ImmutablePair<Entity, TeleportOptions>(player, option));
                 }
             }
             for(Pair<Entity, TeleportOptions> p : options){
                 TeleportHelper.travelEntity(p.getKey(), p.getValue());
             }
         }catch(CommandException e){
             sender.addChatMessage(new ChatComponentTranslation(e.getMessage()));
             sender.addChatMessage(new ChatComponentTranslation(this.getCommandUsage(sender)));
         }
     }
 
     private static TeleportOptions getDestination(ICommandSender sender, String data){
         TeleportOptions dest = new TeleportOptions();
         Player p = NailedAPI.getPlayerRegistry().getPlayerByUsername(data);
         if(p != null){
             dest.setCoordinates(p.getLocation());
             dest.setDestination(p.getCurrentMap());
         }else{
             Map map = NailedAPI.getMapLoader().getMap(data);
             if(map != null){
                 Mappack mappack = map.getMappack();
                 if(mappack != null){
                     dest.setCoordinates(mappack.getMappackMetadata().getSpawnPoint());
                 }
                 dest.setDestination(map);
             }else{
                 map = NailedAPI.getMapLoader().getMap(CommandBase.parseInt(sender, data));
                 if(map != null){
                     Mappack mappack = map.getMappack();
                     if(mappack != null){
                         dest.setCoordinates(mappack.getMappackMetadata().getSpawnPoint());
                     }
                     dest.setDestination(map);
                 }else{
                     throw new CommandException("commands.nailed.tp.fail.nodest");
                 }
             }
         }
         return dest;
     }
 }
