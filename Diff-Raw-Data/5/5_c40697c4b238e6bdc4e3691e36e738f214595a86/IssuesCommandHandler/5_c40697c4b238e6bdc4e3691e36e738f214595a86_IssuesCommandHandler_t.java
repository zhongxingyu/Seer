 package com.drtshock.willie.command;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 
 import com.drtshock.willie.GitHubIssue;
 import com.drtshock.willie.JenkinsJob;
 import com.drtshock.willie.Willie;
 
 public class IssuesCommandHandler implements CommandHandler {
 	
 	@Override
 	public void handle(Willie bot, Channel channel, User sender, String[] args){
 		if (args.length == 0){
 			channel.sendMessage(Colors.RED + "Usage: .issues <job_name> [page]");
 			return;
 		}
 		
 		int page = 1;
 		
 		if (args.length > 1){
 			try{
 				page = Integer.parseInt(args[1]);
 			}catch (NumberFormatException e){
 				channel.sendMessage(Colors.RED + "Invalid page number");
 				return;
 			}
 			
 			if (page < 1){
 				channel.sendMessage(Colors.RED + "Invalid page number");
 				return;
 			}
 		}
 		
 		try{
 			JenkinsJob job = bot.jenkins.getJob(args[0]);
 			
 			GitHubIssue[] issues = job.getIssues();
 			
			if (issues.length == 0){
				channel.sendMessage(Colors.GREEN + job.getDisplayName() + " has no open issues \\o/");
				return;
			}
			
 			int perPage = 4;
 			int pages = (int) Math.ceil((double) issues.length / (double) perPage);
 			int start = (page - 1) * perPage;
 			int end = Math.min(issues.length, start + perPage);
 			
 			if (start >= end){
 				channel.sendMessage(Colors.RED + "Invalid page number");
 				return;
 			}
 			
 			channel.sendMessage(Colors.BLUE + job.getDisplayName() + Colors.NORMAL + " has " + Colors.RED + issues.length + Colors.NORMAL + " open issue(s) page " + page + " of " + pages);
 			
 			for (int i = start; i < end; ++i){
 				channel.sendMessage(" #" + issues[i].getNumber() + " - " + issues[i].getTitle());
 			}
 		}catch (FileNotFoundException e){
 			channel.sendMessage(Colors.RED + "No such job");
 		}catch (IOException e){
 			channel.sendMessage(Colors.RED + "Failed: " + e.getMessage());
 		}
 	}
 	
 }
