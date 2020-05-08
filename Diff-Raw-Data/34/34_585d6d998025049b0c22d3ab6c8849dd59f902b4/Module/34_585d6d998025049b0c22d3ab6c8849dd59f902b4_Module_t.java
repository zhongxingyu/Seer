 public class Module
 {
 	private Games g;
 	private AI ai;
 	private Utils u;
 	private String owner;
 	private String botName;
 	
 	public Module(String own, String Name)
 	{
 		botName = Name;
 		g = new Games();
 		ai = new AI(botName);
 		u = new Utils();
 		owner = own;
 	}
 
	public String doMod(String message, String sender)
 	{
 		/*
 		* Games
 		*/
 
 		if(message.indexOf("roll") > -1)
 		{
 			return g.roll(message);
 		}
 
 		/*
 		* utils
 		*/
 
 		else if(message.indexOf("about") > -1)
 		{
 			return u.about(owner);
 		}
 		else if(message.indexOf("time") > -1)
 		{
 			return u.time(message);
 		}
 		else if(message.indexOf("http") > -1 || message.indexOf("https") > -1)
 		{
 			return u.getTitle(message);
 		}
 		
 		else if(message.indexOf("test") > -1)
 		{
 			return u.test();
 		}
 		/*
 		* AI
 		*/
 		else{
			return ai.getAIResponse(message, sender);
 		}
 
 	}
 
 }
 
 
