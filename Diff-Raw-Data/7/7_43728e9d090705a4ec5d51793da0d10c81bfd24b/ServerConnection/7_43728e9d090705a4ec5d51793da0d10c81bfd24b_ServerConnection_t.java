 package com.cpcookieman.cookieirc;
 
 import java.io.IOException;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.PircBot;
 
 public class ServerConnection extends PircBot
 {
 	public static String serverip;
 	public int serverid;
 	public int servertab;
 	
 	public ServerConnection(String server, int id, int tab)
 	{
 		serverid = id;
 		servertab = tab;
 		serverip = server;
 		setName(Main.user);
 		try
 		{
 			connect(server);
 		}
 		catch (IOException e)
 		{
 			Main.gui.tabs.get(tab).addMessage((e.getMessage()));
 		}
 		catch (IrcException e)
 		{
 			Main.gui.tabs.get(tab).addMessage((e.getMessage()));
 		}
 	}
 	
 	@Override
 	protected void onConnect()
 	{
 		super.onConnect();
 		Main.gui.tabs.get(servertab).addMessage("Connected to " + serverip + ".");
 	}
 	
 	@Override
 	protected void onDisconnect()
 	{
 		super.onDisconnect();
 		Main.gui.tabs.get(servertab).addMessage("Disconnected from " + serverip + ".");
 	}
 	
 	@Override
 	protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) 
 	{
 		super.onKick(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason);
 		int targettab = 0;
 		int i = 0;
 		while(true)
 		{
 			try
 			{
 				if(Main.gui.tabs.get(i).title.equalsIgnoreCase(channel) || Main.gui.tabs.get(i).title.substring(1).equalsIgnoreCase(channel))
 				{
 					targettab = i;
 					break;
 				}
 				else
 				{
 					i++;
 				}
 			}
 			catch(Exception e)
 			{
 				break;
 			}
 		}
 		Main.gui.tabs.get(targettab).addMessage(recipientNick + " has been kicked from " + channel + " by " + kickerNick + " [" + reason + "]");
 		Main.gui.tabs.get(targettab).onUpdate();
 	}
 	
 	@Override
 	protected void onJoin(String channel, String sender, String login, String hostname)
 	{
 		super.onJoin(channel, sender, login, hostname);
 		int targettab = 0;
 		int i = 0;
 		while(true)
 		{
 			try
 			{
 				if(Main.gui.tabs.get(i).title.equalsIgnoreCase(channel) || Main.gui.tabs.get(i).title.substring(1).equalsIgnoreCase(channel))
 				{
 					targettab = i;
 					break;
 				}
 				else
 				{
 					i++;
 				}
 			}
 			catch(Exception e)
 			{
 				break;
 			}
 		}
 		if(!sender.equals(Main.user))
 		{
 			Main.gui.tabs.get(targettab).addMessage(sender + " joins " + channel);
 		}
 		Main.gui.tabs.get(targettab).onUpdate();
 	}
 	
 	@Override
 	protected void onPart(String channel, String sender, String login, String hostname)
 	{
 		super.onPart(channel, sender, login, hostname);
 		int targettab = 0;
 		int i = 0;
 		while(true)
 		{
 			try
 			{
 				if(Main.gui.tabs.get(i).title.equalsIgnoreCase(channel) || Main.gui.tabs.get(i).title.substring(1).equalsIgnoreCase(channel))
 				{
 					targettab = i;
 					break;
 				}
 				else
 				{
 					i++;
 				}
 			}
 			catch(Exception e)
 			{
 				break;
 			}
 		}
 		if(!sender.equals(Main.user))
 		{
 			Main.gui.tabs.get(targettab).addMessage(sender + " leaves " + channel);
 		}
 		Main.gui.tabs.get(targettab).onUpdate();
 	}
 	
 	@Override
 	protected void onAction(String sender, String login, String hostname, String target, String action)
 	{
 		super.onAction(sender, login, hostname, target, action);
 		int i = 0;
 		while(true)
 		{
 			Main.debug("Action check: " + serverid + " - " + Main.gui.tabs.get(i).serverid, false); 
 			if(Main.gui.tabs.get(i).serverid == serverid && Main.gui.tabs.get(i).title.equalsIgnoreCase(target))
 			{
 				Main.gui.tabs.get(i).addMessage("<< " + sender + " " + action + " >>");
 				Main.gui.tabs.get(i).onUpdate();
 				i++;
 			}
 			else
 			{
 				if(i > Main.gui.tabs.size())
 				{
 					break;
 				}
 				i++;
 			}
 		}
 	}
 	
 	@Override
 	protected void onPrivateMessage(String sender, String login, String hostname, String message)
 	{
 		super.onPrivateMessage(sender, login, hostname, message);
 		int targettab = 0;
 		int i = 0;
 		while(true)
 		{
 			try
 			{
 				if(Main.gui.tabs.get(i).title.equalsIgnoreCase(sender) || Main.gui.tabs.get(i).title.substring(1).equalsIgnoreCase(sender))
 				{
 					//TODO Fix Bug Here
 					//     If someone sends us a PM, we open a new window,
 					//     but if they're chat window was already closed
 					//     before, a new one doesn't open for whatever reason.
 					
					//     UPDATE: Maybe this is fixed? Please check.
					
 					if(!Main.gui.tabs.get(i).closed)
 					{
 						targettab = i;
 						break;
 					}
					else
					{
						throw new Exception("Create new PM window.");
					}
 				}
 				else
 				{
 					i++;
 				}
 			}
 			catch(Exception e)
 			{
 				int newtab = Main.gui.addTab(new Tab(sender, false, 2));
 				Main.gui.tabs.get(newtab).server = serverip;
 				Main.gui.tabs.get(newtab).serverid = serverid;
 				Main.gui.tabs.get(newtab).addMessage("Chatting with " + sender + "...");
 				targettab = newtab;
 				break;
 			}
 		}
 		Main.gui.tabs.get(targettab).addMessage(sender, message);
 	}
 	
 	@Override
 	protected void onMessage(String channel, String sender, String login, String hostname, String message)
 	{
 		super.onMessage(channel, sender, login, hostname, message);
 		if(channel == null)
 		{
 			Main.gui.tabs.get(servertab).addMessage(sender, message);
 		}
 		else
 		{
 			int targettab = 0;
 			int i = 0;
 			while(true)
 			{
 				try
 				{
 					if(Main.gui.tabs.get(i).title.equalsIgnoreCase(channel) || Main.gui.tabs.get(i).title.substring(1).equalsIgnoreCase(channel))
 					{
 						targettab = i;
 						break;
 					}
 					else
 					{
 						i++;
 					}
 				}
 				catch(Exception e)
 				{
 					break;
 				}
 			}
 			Main.gui.tabs.get(targettab).addMessage(sender, message);
 		}
 	}
 }
