 package net.daboross.bukkitdev.playerdata.commandreactors;
 
 import java.util.ArrayList;
 import java.util.List;
 import net.daboross.bukkitdev.commandexecutorbase.ColorList;
 import net.daboross.bukkitdev.commandexecutorbase.CommandExecutorBase;
 import net.daboross.bukkitdev.playerdata.PData;
 import net.daboross.bukkitdev.playerdata.PlayerData;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 /**
  *
  * @author daboross
  */
 public class ListPlayersByFirstJoinCommandReactor implements CommandExecutorBase.CommandReactor {
 
     private final PlayerData playerDataMain;
 
     public ListPlayersByFirstJoinCommandReactor(PlayerData playerDataMain) {
         this.playerDataMain = playerDataMain;
     }
 
     @Override
     public void runCommand(CommandSender sender, Command mainCommand, String mainCommandLabel, String subCommand, String subCommandLabel,
             String[] subCommandArgs, CommandExecutorBase.CommandExecutorBridge executorBridge) {
         if (subCommandArgs.length > 1) {
             sender.sendMessage(ColorList.MAIN + "Please Use Only 1 Number After " + ColorList.CMD + "/" + mainCommandLabel + ColorList.SUBCMD + " " + subCommandLabel);
         }
         int pageNumber;
         if (subCommandArgs.length == 0) {
             pageNumber = 1;
         } else {
             try {
                 pageNumber = Integer.valueOf(subCommandArgs[0]);
             } catch (Exception e) {
                 sender.sendMessage(ColorList.ERROR_ARGS + subCommandArgs[0] + ColorList.ERROR + " is not a number.");
                 sender.sendMessage(executorBridge.getHelpMessage(subCommandLabel, mainCommandLabel));
                 return;
             }
             if (pageNumber < 1) {
                 sender.sendMessage(ColorList.ERROR_ARGS + subCommandArgs[0] + ColorList.ERROR + " is not a non-0 positive number.");
                 return;
             }
         }
         List<PData> pDataList = playerDataMain.getPDataHandler().getAllPDatasFirstJoin();
         ArrayList<String> messagesToSend = new ArrayList<String>();
         messagesToSend.add("");
        messagesToSend.add(ColorList.MAIN + "Player List, Page " + ColorList.NUMBER + pageNumber + ColorList.MAIN + "out of " + ColorList.NUMBER + pDataList.size() / 6 + ColorList.MAIN + ":");
         for (int i = ((pageNumber - 1) * 6); i < ((pageNumber - 1) * 6) + 6 & i < pDataList.size(); i++) {
             PData current = pDataList.get(i);
             messagesToSend.add(ColorList.NAME + current.userName() + ColorList.MAIN + " was first seen " + ColorList.NUMBER + PlayerData.getFormattedDDate(System.currentTimeMillis() - current.getFirstLogIn().time()) + ColorList.MAIN + " ago.");
         }
         if (pageNumber < (pDataList.size() / 6.0)) {
             messagesToSend.add(ColorList.MAIN + "To View The Next Page, Type: " + ColorList.CMD + "/" + mainCommandLabel + ColorList.SUBCMD + " " + subCommandLabel + ColorList.ARGS + " " + (pageNumber + 1));
         }
         sender.sendMessage(messagesToSend.toArray(new String[0]));
     }
 }
