 package rel.rogue.ircool;
 
 /**
  *
  * @author Spencer
  */
 public abstract class CommandExec {
     
     public org.pircbotx.PircBotX user = IRCool.getUser();
     
     public abstract void onCommand (String[] args);
     
     public abstract String[] getTriggers();
     
     public abstract String getUsage();
    
    public abstract boolean takesArgs();
    
    public abstract boolean requiresArgs();
 }
