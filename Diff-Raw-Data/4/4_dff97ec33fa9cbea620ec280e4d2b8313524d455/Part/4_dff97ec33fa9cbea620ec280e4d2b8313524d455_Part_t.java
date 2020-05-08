 package net.mms_projects.irc.channel_bots.irc.commands;
 
 import java.util.regex.Matcher;
 
 import net.mms_projects.irc.channel_bots.irc.Command;
 
 public class Part extends Command {
 
 	public String nickname;
 	public String channel;
 	public String reason;
 	
 	public Part() {
		super(":(.+) PART ([^\\s]+)( :(.+))?", ":%s PART %s");
 	}
 
 	@Override
 	public void parse(String rawdata) {
 		Matcher matcher = this.match(rawdata);
 		if (matcher.find()) {
 			this.nickname = matcher.group(1);
 			this.channel = matcher.group(2);
 			this.reason = matcher.group(4);
 		}
 	}
 
 	@Override
 	public String build() {
 		if (reason == null) {
 			return String.format(this.outputPattern, this.nickname,
 					this.channel);
 		} else {
 			return String.format(this.outputPattern + " :%s", this.nickname,
 					this.channel, this.reason);
 		}
 	}
 
 }
