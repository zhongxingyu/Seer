 package com.Zolli.EnderCore.Commands.command;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.command.CommandSender;
 
 import com.Zolli.EnderCore.EnderCore;
 import com.Zolli.EnderCore.Commands.ECCommand;
 import com.Zolli.EnderCore.Updater.updateChecker.updateResult;
 
 public class updateCommand implements ECCommand {
 	
 	List<String> permissions = new ArrayList<String>();
 	List<String> examples = new ArrayList<String>();
 	EnderCore plugin;
 	
 	public updateCommand(EnderCore instance) {
 		this.plugin = instance;
 		
 		this.examples.add("/ec update <status|download|channel|changelog>");
 		this.permissions.add("ec.update");
 		this.permissions.add("ec.admin");
 	}
 	
 	@Override
 	public boolean execute(CommandSender sender, String[] args,String chainedParams) {
 		this.plugin.updater.checkUpdate();
 		
 		/* STATUS subcommand */
 		if(args[1].equalsIgnoreCase("status")) {
 			if(this.plugin.updater.getResult().equals(updateResult.SUCCESS)) {
 				sender.sendMessage(this.plugin.local.getLocalizedString("updater.sucessDownload"));
 			} else if(this.plugin.updater.getResult().equals(updateResult.FAILED_TO_DOWNLOAD)) {
 				sender.sendMessage(this.plugin.local.getLocalizedString("updater.failedDownload"));
 			} else if(this.plugin.updater.getResult().equals(updateResult.FAILED_CHECK)) {
 				sender.sendMessage(this.plugin.local.getLocalizedString("updater.failedCheck"));
 			} else if(this.plugin.updater.getResult().equals(updateResult.UPDATE_AVAILABLE)) {
 				sender.sendMessage(this.plugin.local.getLocalizedString("updater.updateAvailable1"));
 				sender.sendMessage(this.plugin.local.getLocalizedString("updater.updateAvailable2"));
 			} else {
 				sender.sendMessage(this.plugin.local.getLocalizedString("updater.noUpdate"));
 			}
 		/* DOWNLOAD subcommand */
 		} else if(args[1].equalsIgnoreCase("download")) {
 			if(this.plugin.updater.getResult().equals(updateResult.UPDATE_AVAILABLE)) {
 				this.plugin.updater.downloadUpdate();
				
				if(this.plugin.updater.getResult().equals(updateResult.SUCCESS)) {
					sender.sendMessage(this.plugin.local.getLocalizedString("updater.sucessDownloadMessage"));
				}
 			} else {
 				sender.sendMessage(this.plugin.local.getLocalizedString("updater.noUpdate"));
 			}
 		/* CHANGELOG subcommand */
 		} else if(args[1].equalsIgnoreCase("changelog")) {
 			sender.sendMessage(this.plugin.updater.getChangelog());
 		/* Channel subcommand */
 		} else if(args[1].equalsIgnoreCase("channel")) {
 			sender.sendMessage(this.plugin.local.getLocalizedString("updater.channelInfo").replace("#CHANNEL#", this.plugin.updater.getChannel()));
 		}
 		return false;
 	}
 
 	@Override
 	public String getName() {
 		return "update";
 	}
 
 	@Override
 	public boolean isAccessibleFromConsole() {
 		return true;
 	}
 
 	@Override
 	public List<String> getPermission() {
 		return this.permissions;
 	}
 
 	@Override
 	public int getArgsLength() {
 		return 1;
 	}
 
 	@Override
 	public List<String> getExample() {
 		return this.examples;
 	}
 
 }
