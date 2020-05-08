 package info.bytecraft.commands;
 
 import org.bukkit.GameMode;
 
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
 
 public class GameModeCommand extends AbstractCommand
 {
 
     public GameModeCommand(Bytecraft instance, String command)
     {
         super(instance, command);
     }
     
     public boolean handlePlayer(BytecraftPlayer player, String[] args)
     {
         if(!player.canFill())return true;
         if("gamemode".equalsIgnoreCase(getCommand())){
             if(args.length == 0){
                 if(player.getGameMode() == GameMode.CREATIVE){
                     player.setGameMode(GameMode.SURVIVAL);
                 }else{
                     player.setGameMode(GameMode.CREATIVE);
                 }
             }else if(args.length == 1){
                GameMode gm = GameMode.SURVIVAL;
                try{
                    gm = GameMode.getByValue(Integer.parseInt(args[0]));
                }catch(NumberFormatException e){
                    gm = GameMode.valueOf(args[0].toUpperCase());
                }
                if(gm != null && gm != GameMode.ADVENTURE){
                     player.setGameMode(gm);
                 }
             }
         }else if("creative".equalsIgnoreCase(getCommand())){
             player.setGameMode(GameMode.CREATIVE);
         }else if("survival".equalsIgnoreCase(getCommand())){
             player.setGameMode(GameMode.SURVIVAL);
         }
         return true;
     }
 }
