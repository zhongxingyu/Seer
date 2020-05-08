 package com.test9.irc.parser;
 
 public class Parser {
 
 	final private String PREFIXES[] = {"ERR", "RPL"};
 	private static boolean init = false;
 
 	final static private String ERR[] = {"401_ERR_NOSUCHNICK", "402_ERR_NOSUCHSERVER", "403_ERR_NOSUCHCHANNEL", 
 		"404_ERR_CANNOTSENDTOCHAN", "405_ERR_TOOMANYCHANNELS", "406_ERR_WASNOSUCHNICK", "407_ERR_TOOMANYTARGETS", 
 		"409_ERR_NOORIGIN", "411_ERR_NORECIPIENT", "412_ERR_NOTEXTTOSEND", "413_ERR_NOTOPLEVEL", "414_ERR_WILDTOPLEVEL",
 		"421_ERR_UNKNOWNCOMMAND", "422_ERR_NOMOTD", "423_ERR_NOADMININFO", "424_ERR_FILEERROR", "431_ERR_NONICKNAMEGIVEN", 
 		"432_ERR_ERRONEUSNICKNAME", "433_ERR_NICKNAMEINUSE", "436_ERR_NICKCOLLISION", "441_ERR_USERNOTINCHANNEL", 
 		"442_ERR_NOTONCHANNEL", "443_ERR_USERONCHANNEL", "444_ERR_NOLOGIN", "445_ERR_SUMMONDISABLED", "446_ERR_USERSDISABLED", 
 		"451_ERR_NOTREGISTERED", "461_ERR_NEEDMOREPARAMS", "462_ERR_ALREADYREGISTRED", "463_ERR_NOPERMFORHOST", "464_ERR_PASSWDMISMATCH", 
 		"465_ERR_YOUREBANNEDCREEP", "467_ERR_KEYSET", "471_ERR_CHANNELISFULL", "472_ERR_UNKNOWNMODE", "473_ERR_INVITEONLYCHAN", 
 		"474_ERR_BANNEDFROMCHAN", "475_ERR_BADCHANNELKEY", "481_ERR_NOPRIVILEGES", "482_ERR_CHANOPRIVSNEEDED",
 		"483_ERR_CANTKILLSERVER", "491_ERR_NOOPERHOST", "501_ERR_UMODEUNKNOWNFLAG", "502_ERR_USERSDONTMATCH"};
 
 	final static private String RPL[] = {"300_RPL_NONE", "302_RPL_USERHOST", "303_RPL_ISON", "301_RPL_AWAY", 
 		"305_RPL_UNAWAY", "306_RPL_NOWAWAY", "311_RPL_WHOISUSER", "312_RPL_WHOISSERVER",
 		"313_RPL_WHOISOPERATOR", "317_RPL_WHOISIDLE", "318_RPL_ENDOFWHOIS", "319_RPL_WHOISCHANNELS", "314_RPL_WHOWASUSER", 
 		"369_RPL_ENDOFWHOWAS", "321_RPL_LISTSTART", "322_RPL_LIST", "323_RPL_LISTEND", "324_RPL_CHANNELMODEIS", 
 		"331_RPL_NOTOPIC", "332_RPL_TOPIC", "341_RPL_INVITING", "342_RPL_SUMMONING", "351_RPL_VERSION, WHOREPLY",
 		"315_RPL_ENDOFWHO", "353_RPL_NAMREPLY", "366_RPL_ENDOFNAMES", "364_RPL_LINKS", "365_RPL_ENDOFLINKS", "367_RPL_BANLIST", 
 		"368_RPL_ENDOFBANLIST", "371_RPL_INFO", "374_RPL_ENDOFINFO", "375_RPL_MOTDSTART", "372_RPL_MOTD", "376_RPL_ENDOFMOTD", 
 		"381_RPL_YOUREOPER", "382_RPL_REHASHING", "391_RPL_TIME", "392_RPL_USERSSTART", "393_RPL_USERS", "394_RPL_ENDOFUSERS",
 		"395_RPL_NOUSERS", "200_RPL_TRACELINK", "201_RPL_TRACECONNECTING", "202_RPL_TRACEHANDSHAKE", "203_RPL_TRACEUNKNOWN", 
 		"204_RPL_TRACEOPERATOR", "205_RPL_TRACEUSER", "206_RPL_TRACESERVER", "208_RPL_TRACENEWTYPE", "261_RPL_TRACELOG", 
 		"211_RPL_STATSLINKINFO", "212_RPL_STATSCOMMANDS", "213_RPL_STATSCLINE", "214_RPL_STATSNLINE", "215_RPL_STATSILINE", 
 		"216_RPL_STATSKLINE", "218_RPL_STATSYLINE", "219_RPL_ENDOFSTATS", "241_RPL_STATSLLINE", "242_RPL_STATSUPTIME", 
 		"243_RPL_STATSOLINE", "244_RPL_STATSHLINE", "221_RPL_UMODEIS", "251_RPL_LUSERCLIENT", "252_RPL_LUSEROP", "253_RPL_LUSERUNKNOWN", 
 		"254_RPL_LUSERCHANNELS", "255_RPL_LUSERME", "256_RPL_ADMINME", "257_RPL_ADMINLOC1", "258_RPL_ADMINLOC2", "259_RPL_ADMINEMAIL"};
 
	public static void main(String[] args)
 	{
 		Parser p = new Parser();
		
 		parse(":irc.ecsig.com 444 jared-test :Welcome to the ECSIG IRC Network jared-test!jared-test@ip68-110-207-195.ri.ri.cox.net");
 	}
 	
 	public Parser() 
 	{
 		init = true;
 	}
 
 	public int parse(String message)
 	{
 		int msg = -1;
 
 		String prefix_msg_seperation[] = message.split(":");
 		String prefix_split[] = prefix_msg_seperation[0].split(" ");
 		String server = prefix_split[0];
 		String irc_code = prefix_split[1];
 		String nick = prefix_split[2];
 
 		if(Integer.parseInt(irc_code) >= 401 || Integer.parseInt(irc_code) <= 502)
 			find_error(irc_code);
 		else if(Integer.parseInt(irc_code) >= 200 || Integer.parseInt(irc_code) <= 395)
 			find_rpl(irc_code);
 
 
 		return msg;
 	}
 
 
 	private String find_error(String irc_code)
 	{
 		String which_error = "";
 
 		boolean found = false;
 		int try_index = 0;
 
 		while(!found && try_index < ERR.length)
 		{
 			if(ERR[try_index].startsWith(irc_code, 0))
 			{
 				found = true;
 				which_error = ERR[try_index];
 			}
 			else
 				try_index++;
 		}
 
 		if(found)
 			return which_error;
 		else
 			return new String("[ERROR]: com.test9.irc.parser/Parser.java Could not find error code.");
 	}
 
 	private String find_rpl(String irc_code)
 	{
 		String which_rpl = "";
 
 		boolean found = false;
 		int try_index = 0;
 
 		while(!found && try_index < RPL.length)
 		{
 			if(RPL[try_index].startsWith(irc_code, 0))
 			{
 				found = true;
 				which_rpl = ERR[try_index];
 			}
 			else
 				try_index++;
 		}
 
 		if(found)
 			return which_rpl;
 		else
 			return new String("[ERROR]: com.test9.irc.parser/Parser.java Could not find reply code.");
 	}
 	
 	/**
 	 * @return the init
 	 */
 	public static boolean isInit() {
 		return init;
 	}
 	/**
 	 * @param init the init to set
 	 */
 	public static void setInit(boolean init) {
 		Parser.init = init;
 	}
 
 }
