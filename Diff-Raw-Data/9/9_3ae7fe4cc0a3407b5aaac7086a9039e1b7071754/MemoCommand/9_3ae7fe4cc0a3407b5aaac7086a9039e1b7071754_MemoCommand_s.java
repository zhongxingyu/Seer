 package commands;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 import org.pircbotx.hooks.events.JoinEvent;
 import org.pircbotx.hooks.events.NickChangeEvent;
 import org.pircbotx.hooks.events.UserListEvent;
 
 import backend.Bot;
 import backend.Database;
 import backend.Util;
 
 
 public class MemoCommand extends Command
 {
 	@Override
 	protected void initialize()
 	{
 		this.setHelpText("Save a message for someone who is not in the channel. They will receive the message once they enter the channel.");
 		this.setName("Memo");
 		addAlias("memo");
 		setTableName("memos");
 		try
 		{
 			Database.createTable(getFormattedTableName(), "sender VARCHAR(30), recipient VARCHAR(30), time TIMESTAMP, msg VARCHAR(600)");
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	protected String format()
 	{
 		return super.format() + " [nick] [message]";
 	}
 
 	@Override
 	public void execute(Bot bot, Channel chan, User user, String message)
 	{
 		if(getTarget(chan, user).equals(user.getNick()))
 		{
 			passMessage(bot, chan, user, "Sorry, I only work in channels! Use memoserv if you want private memos.");
 			return;
 		}
 		if(Util.hasArgs(message, 3))
 		{
 			String[] args = message.split(" ",3);
 			String nick = args[1];
 			String msg = args[2];
 			if(chan.getUsers().contains(bot.getUser(nick)))
 				passMessage(bot, chan, user, "Nice try, "+nick+" is already in this channel. Man up and talk to him/her yourself!");
 			else
 			{
 				try
 				{
 					Database.insert(getFormattedTableName(), "sender, recipient, time, msg",
 									Database.getEnclosedString(user.getNick())+"," +
 									Database.getEnclosedString(nick)+"," +
 									Database.formatTimestamp(System.currentTimeMillis())+"," +
 									Database.getEnclosedString(msg.replaceAll("'", "''")));
 					passMessage(bot, chan, user, "Okay, i'll let "+nick+" know.");
 
 				} catch (SQLException e)
 				{
 					e.printStackTrace();
 					passMessage(bot, chan, user, "Freek sucks ass at SQL.");
 
 				}
 								
 
 			}
 			
 			
 		}
 		else
 		{
 			invalidFormat(bot, chan, user);
 		}
 	}
 
 	@Override
 	public void onJoin(JoinEvent<Bot> e) throws Exception
 	{
 		super.onJoin(e);
		e.getBot().waitFor(UserListEvent.class);
 		String nick = e.getUser().getNick();

 		//if bot is joining channel, check for memos for people in that channel
 		if(nick.equals(e.getBot().getNick()))
 		{
 			checkChanMemos(e.getChannel(), e.getBot());
 		}
 		else //check if entering user has a memo
 		{
 			checkNickMemos(e.getUser(),e.getBot());
 		}
 	}
 	
 	@Override
 	public void onNickChange(NickChangeEvent<Bot> e) throws Exception
 	{
 		super.onNickChange(e);
 		//check if new nick has a memo
 		checkNickMemos(e.getUser(),e.getBot());
 	}
 	
 	private void checkNickMemos(User user, Bot b)
 	{
 		try
 		{
 		String nick = user.getNick();
 		List<HashMap<String,Object>> returned = Database.select("SELECT * FROM "+getFormattedTableName()+" WHERE lower(RECIPIENT)="+Database.getEnclosedString(nick.toLowerCase()));
 		if(returned.isEmpty())
 			return;
 		for(HashMap<String,Object> memo : returned)
 		{
 			String sender = memo.get("SENDER").toString();
 			//String chan = memo.get("CHAN").toString();
 			String timestamp = memo.get("TIME").toString();
 			String msg = memo.get("MSG").toString();
 //			passMessage(e.getBot(),e.getBot().getChannel(chan),null,"A memo for "+nick+"! "+sender+" said, \""+msg+"\" at "+timestamp);
 			b.sendMessage(user, "A memo for "+nick+"! "+sender+" said, \""+msg+"\" at "+timestamp);
 		}
 		Database.execRaw("DELETE FROM "+getFormattedTableName()+" WHERE lower(RECIPIENT)="+Database.getEnclosedString(nick.toLowerCase()));
 	
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void checkChanMemos(Channel chan, Bot b)
 	{
 		try
 		{
 		for(User user:chan.getUsers())
 		{
 			String nick = user.getNick();
 			List<HashMap<String,Object>> returned = Database.select("SELECT * FROM "+getFormattedTableName()+" WHERE lower(RECIPIENT)="+Database.getEnclosedString(nick.toLowerCase()));
			//System.out.println(returned);
			//System.out.println(getFormattedTableName());
 			if(returned.isEmpty())
 				continue;
 			for(HashMap<String,Object> memo : returned)
 			{
 				String sender = memo.get("SENDER").toString();
 				//String chan = memo.get("CHAN").toString();
 				String timestamp = memo.get("TIME").toString();
 				String msg = memo.get("MSG").toString();
 //				passMessage(e.getBot(),e.getBot().getChannel(chan),null,"A memo for "+nick+"! "+sender+" said, \""+msg+"\" at "+timestamp);
 				b.sendMessage(user, "A memo for "+nick+"! "+sender+" said, \""+msg+"\" at "+timestamp);
 			}
 			Database.execRaw("DELETE FROM "+getFormattedTableName()+" WHERE lower(RECIPIENT)="+Database.getEnclosedString(nick.toLowerCase()));
 		}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 }
