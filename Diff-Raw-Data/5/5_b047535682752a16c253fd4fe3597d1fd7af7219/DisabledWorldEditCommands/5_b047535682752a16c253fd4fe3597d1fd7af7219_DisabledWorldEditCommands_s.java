 package fr.aumgn.dac2.commands.worldedit;
 
 import org.bukkit.command.CommandSender;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.args.CommandArgs;
 import fr.aumgn.dac2.DAC;
 import fr.aumgn.dac2.commands.DACCommands;
 
 @NestedCommands("dac2")
 public class DisabledWorldEditCommands extends DACCommands {
 
     public DisabledWorldEditCommands(DAC dac) {
         super(dac);
     }
 
    @Command(name = "setwe", max = -1, strictFlags = false)
     public void setwe(CommandSender sender, CommandArgs args) {
         sender.sendMessage(msg("disabled"));
     }
 
    @Command(name = "select", max = -1, strictFlags = false)
     public void select(CommandSender sender, CommandArgs args) {
         sender.sendMessage(msg("disabled"));
     }
 }
