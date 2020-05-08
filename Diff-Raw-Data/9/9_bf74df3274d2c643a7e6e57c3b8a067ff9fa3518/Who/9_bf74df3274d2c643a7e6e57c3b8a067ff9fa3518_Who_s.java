 /*
    Copyright 2011 James Cowgill
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package uk.org.cowgill.james.jircd.commands;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 import uk.org.cowgill.james.jircd.Channel;
 import uk.org.cowgill.james.jircd.ChannelMemberMode;
 import uk.org.cowgill.james.jircd.Client;
 import uk.org.cowgill.james.jircd.Command;
 import uk.org.cowgill.james.jircd.IRCMask;
 import uk.org.cowgill.james.jircd.Message;
 import uk.org.cowgill.james.jircd.Permissions;
 import uk.org.cowgill.james.jircd.Server;
 
 /**
  * The WHO command - displays information about clients
  * 
  * @author James
  */
 public class Who implements Command
 {
 	@Override
 	public void run(Client client, Message msg)
 	{
 		//Get mask
 		String mask;
 		boolean operOnly = false;
 		
 		if(msg.paramCount() == 0)
 		{
 			//Force everyone
 			mask = "*";
 		}
 		else
 		{
 			//Use given mask
 			mask = msg.getParam(0);
 			
 			if(mask.equals("0"))
 			{
 				mask = "*";
 			}
 			
 			//Check opers only
 			operOnly = (msg.paramCount() >= 2 && msg.getParam(1).equals("o"));
 		}
 
 		//See invisible peoples
 		boolean seeInvisible = client.hasPermission(Permissions.seeInvisible);
 		
 		//Check for chanel
 		if(mask.charAt(0) == '#')
 		{
 			//Print channel members
 			Channel channel = Server.getServer().getChannel(mask);
 			
 			if(channel != null)
 			{
 				//Print channel members
 				boolean allSeeing = (channel.lookupMember(client) != null) || seeInvisible;
 
 				//Send replies
 				for(Map.Entry<Client, ChannelMemberMode> other : channel.getMembers().entrySet())
 				{
 					//Can see client?
 					if(allSeeing || findCommonChannel(client, other.getKey()) != null)
 						Who.sendWhoMsg(client, other.getKey(), channel, other.getValue());
 				}
 			}
 		}
 		else
 		{
 			//Search all clients
 			Collection<Client> clients;
 			
 			if(operOnly)
			{
				clients = Server.getServer().getRegisteredClients();
			}
			else
			{
 				clients = Server.getServer().getIRCOperators();
			}
 			
 			//Do windcard test on all clients in the list
 			for(Client other : clients)
 			{
 				//Check visibility
 				Channel commonChannel = findCommonChannel(client, other);
 				
 				if(commonChannel != null || !other.isModeSet('i') || client == other || seeInvisible)
 				{
 					if(IRCMask.wildcardCompare(other.id.nick, mask) ||
 						IRCMask.wildcardCompare(other.id.user, mask) ||
 						IRCMask.wildcardCompare(other.id.host, mask) ||
 						IRCMask.wildcardCompare(other.realName, mask))
 					{
 						//Send this client
 						sendWhoMsg(client, other, commonChannel, null);
 					}
 				}
 			}
 		}
 		
 		//Send end reply
 		client.send(client.newNickMessage("315").
 				appendParam(mask).appendParam("End of /WHO list"));
 	}
 
 
 	@Override
 	public int getMinParameters()
 	{
 		return 0;
 	}
 
 	@Override
 	public String getName()
 	{
 		return "WHO";
 	}
 
 	@Override
 	public int getFlags() { return FLAG_NORMAL; }
 
 	/**
 	 * Sends a WHO reply to client
 	 * 
 	 * @param client client to reply to
 	 * @param other client information is read from
 	 * @param channel common channel (or null if no common channel)
 	 * @param chanMode other's channel mode (or null to find mode)
 	 */
 	private static void sendWhoMsg(Client client, Client other, Channel channel, ChannelMemberMode chanMode)
 	{
 		//Calculate channel name and mode
 		String chanName = "*";
 		if(channel != null)
 		{
 			//Get name
 			chanName = channel.getName();
 			
 			//Get mode
 			if(chanMode == null)
 			{
 				chanMode = channel.lookupMember(other);
 			}
 		}
 		
 		//Send reply
 		StringBuilder info = new StringBuilder();
 		
 		if(other.isAway())
 		{
 			info.append('G');
 		}
 		else
 		{
 			info.append('H');
 		}
 		
 		//IRC op prefix
 		if(other.isModeSet('o') || other.isModeSet('O'))
 		{
 			info.append('*');
 		}
 
 		//Chan op prefix
 		if(chanMode != null)
 		{
 			info.append(chanMode.toPrefixString(true));
 		}
 		
 		//Final send
 		client.send(client.newNickMessage("352").
 				appendParam(chanName).
 				appendParam(other.id.user).
 				appendParam(other.id.host).
 				appendParam(Server.getServer().getConfig().serverName).
 				appendParam(other.id.nick).
 				appendParam(info.toString()).
 				appendParam("0 " + other.realName));
 	}
 
 	/**
 	 * Attempts to find a common channel between 2 clients
 	 *
 	 * @param clientA first client
 	 * @param clientB second client
 	 * @return the common channel or null if there is no common channel
 	 */
 	private static Channel findCommonChannel(Client clientA, Client clientB)
 	{
 		Set<Channel> chansA = clientA.getChannels();
 		Set<Channel> chansB = clientB.getChannels();
 
 		//Swap so clientB has most channels
 		if(chansA.size() > chansB.size())
 		{
 			Set<Channel> tmp = chansA;
 			chansB = chansA;
 			chansA = tmp;
 		}
 
 		//Search for common channels
 		for(Channel channel : chansA)
 		{
 			if(chansB.contains(channel))
 			{
 				return channel;
 			}
 		}
 
 		return null;
 	}
 }
