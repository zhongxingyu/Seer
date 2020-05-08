 package de.minestar.cok.command;
 
 import java.util.List;
 
 import net.minecraft.command.ICommandSender;
 import de.minestar.cok.game.CoKGame;
 import de.minestar.cok.game.Team;
 import de.minestar.cok.helper.ChatSendHelper;
 import de.minestar.cok.references.Color;
 import de.minestar.cok.references.Reference;
 
 public class CommandTeams extends CoKCommand {
 
 	@Override
 	public String getCommandName() {
 		return Reference.TeamsCommand;
 	}
 
 	@Override
 	public String getCommandUsage(ICommandSender icommandsender) {
 		return getCommandName();
 	}
 
 	@Override
 	public void processCommand(ICommandSender icommandsender, String[] astring) {
 		String captain;
 		for(Team team : CoKGame.teams.values()){
 			ChatSendHelper.sendMessage(icommandsender, "Members of team " + Color.getColorCodeFromChar(team.getColor())
 					+ team.getName());
 			captain = team.getCaptain();
 			ChatSendHelper.sendMessage(icommandsender, "Captain: " + captain);
 			for(String name : team.getAllPlayers()){
 				if(!name.equals(captain)){
 					ChatSendHelper.sendMessage(icommandsender, "   -" + name);
 				}
 			}
 		}
 	}
 
 	@Override
 	public List<?> addTabCompletionOptions(ICommandSender icommandsender,
 			String[] astring) {
 		return null;
 	}
 
 }
