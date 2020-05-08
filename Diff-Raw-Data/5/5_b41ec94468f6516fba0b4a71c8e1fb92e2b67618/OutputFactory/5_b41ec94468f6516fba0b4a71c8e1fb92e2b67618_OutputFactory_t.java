 package com.test9.irc.parser;
 
 /**
  *target     =  nickname / server
   msgtarget  =  msgto *( "," msgto )
   msgto      =  channel / ( user [ "%" host ] "@" servername )
   msgto      =/ ( user "%" host ) / targetmask
   msgto      =/ nickname / ( nickname "!" user "@" host )
   channel    =  ( "#" / "+" / ( "!" channelid ) / "&" ) chanstring
                 [ ":" chanstring ]
   servername =  hostname
   host       =  hostname / hostaddr
   hostname   =  shortname *( "." shortname )
   shortname  =  ( letter / digit ) *( letter / digit / "-" )
  *( letter / digit )
                   ; as specified in RFC 1123 [HNAME]
   hostaddr   =  ip4addr / ip6addr
   ip4addr    =  1*3digit "." 1*3digit "." 1*3digit "." 1*3digit
   ip6addr    =  1*hexdigit 7( ":" 1*hexdigit )
   ip6addr    =/ "0:0:0:0:0:" ( "0" / "FFFF" ) ":" ip4addr
   nickname   =  ( letter / special ) *8( letter / digit / special / "-" )
   targetmask =  ( "$" / "#" ) mask
                   ; see details on allowed masks in section 3.3.1
   chanstring =  %x01-07 / %x08-09 / %x0B-0C / %x0E-1F / %x21-2B
   chanstring =/ %x2D-39 / %x3B-FF
                   ; any octet except NUL, BELL, CR, LF, " ", "," and ":"
   channelid  = 5( %x41-5A / digit )   ; 5( A-Z / 0-9 )  
   user       =  1*( %x01-09 / %x0B-0C / %x0E-1F / %x21-3F / %x41-FF )
                   ; any octet except NUL, CR, LF, " " and "@"
   key        =  1*23( %x01-05 / %x07-08 / %x0C / %x0E-1F / %x21-7F )
                   ; any 7-bit US_ASCII character,
                   ; except NUL, CR, LF, FF, h/v TABs, and " "
   letter     =  %x41-5A / %x61-7A       ; A-Z / a-z
   digit      =  %x30-39                 ; 0-9
   hexdigit   =  digit / "A" / "B" / "C" / "D" / "E" / "F"
   special    =  %x5B-60 / %x7B-7D
                    ; "[", "]", "\", "`", "_", "^", "{", "|", "}"
  * @author Jared Patton
  *
  */
 public class OutputFactory {
 	private static boolean init = false;
 
 	public OutputFactory() {
 		init = true;
 	}
 
 	public String format_message(String message, String target)
 	{
 		String formatted_message = "";
 
 		if(message.startsWith("/"))
 		{
 			String command = message.substring(1, message.indexOf(" "));
 
 			switch(command) {
 			case "/PASS": case "/NICK": case "/OPER":
 			case "/INVITE": case "/MOTD": case "/LUSERS":
 			case "/VERSION": case "/STATS": case "/LINKS":
 			case "/TIME": case "/CONNECT": case "/TRACE":
 			case "/ADMIN": case "/INFO": case "/WHO":
 			case "/WHOIS": case "/WHOWAS": case "/PING":
 			case "/REHASH": case "/DIE": case "/RESTART":
 			case "/SUMMON": case "/USERS": case "/ISON":
 			case "/JOIN": case "/PART": case "/PONG":
 				formatted_message = message.substring(1, message.length());
 				break;
 			case "/QUIT":
 				formatted_message = make_quit(message);
 				break;
 			}
 		} 
 
 		else 
 		{
 			switch (message.substring(0, message.indexOf(" "))) {
 			case "PRIVMSG": case "SQUERY": case "AWAY":
 				formatted_message = make_privmsg(message, target);
 				break;
 			}
 		}
 
 		/*	
 			case "USER":
 				//formatted_message = make_user()
 			case "MODE":
 			case "SERVICE":
 		 * 
 
 		case "SQUIT":
 			//case "MODE":
 			//TODO check difference in modes
 		case "TOPIC":
 		case "NAMES":
 		case "LIST":
 		case "KICK":
 
 		case "NOTICE":
 		case "SERVLIST":
 			//TODO should case "KILL": be included
 
 			break;
 			//TODO look into this shiz case "ERROR":
 		case "WALLOPS":
 		case "USERHOST":	
 		}*/
 
 		return formatted_message;
 
 	}
 
 	/**
 	 * 
 	 * This constructs a private message that can go to either of several targets.
 	 * @param msgtarget
 	 * @param message
 	 * @return privmsg
 	 */
	private String make_privmsg(String message, String target)
 	{
 		String privmsg = "";
 
 		privmsg.concat("PRIVMSG ");
		privmsg.concat(target);
 		privmsg.concat(" :");
 		privmsg.concat(message);
 
 		return privmsg;
 
 	}	
 	
 	/**
 	 * Makes a quit message for when the user quits.
 	 * @param message
 	 * @return
 	 */
 	private String make_quit(String message)
 	{
 		String quit_message = "";
 
 		quit_message.concat("QUIT :");
 		quit_message.concat(message);
 
 		return quit_message;
 	}
 
 
 	/**
 	 * @return the init
 	 */
 	public static boolean isInit() {
 		return init;
 	}
 }
