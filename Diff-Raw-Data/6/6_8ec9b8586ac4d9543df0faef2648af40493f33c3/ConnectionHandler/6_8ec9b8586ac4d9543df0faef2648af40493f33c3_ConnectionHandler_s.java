 package mods.learncraft.common;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.NetLoginHandler;
 import net.minecraft.network.packet.NetHandler;
 import net.minecraft.network.packet.Packet1Login;
 import net.minecraft.server.MinecraftServer;
 import cpw.mods.fml.common.network.IConnectionHandler;
 import cpw.mods.fml.common.network.Player;
 
 public class ConnectionHandler implements IConnectionHandler
 {
 	@Override
 	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
 	{
 		EntityPlayer player1 = (EntityPlayer) player;
 		Common.currentPlayers.addPlayer(player1);
 		Common.playerlist.add(player1);
 
 		if(!Common.inProgress)
 		{
 			player1.addChatMessage("Welcome.  Please choose a team by activating a block.  Then execute the /ready command.");
 
 			if(null == Common.dbqueries)
 			{
 				player1.addChatMessage("The dbqueries is null.  Cannot start checkTeamStatus thread");
 			} else
 			{
 				Common.dbqueries.insertPlayerLoggedIn(player1.username);
 				Common.teleportPlayerTo(player1, "choose_team", true);
 			}
		} else
 		{
 			Team team = Common.getTeam(player1);
			player1.addChatMessage("Welcome back! You are on the " + team.teamcolor + " team.");
 			PacketHandler.sendOutTeamScoresPacket(0, "");
 		}
 	}
 
 	@Override
 	public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void connectionClosed(INetworkManager manager)
 	{
 	}
 
 	@Override
 	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
 	{
 		// TODO Auto-generated method stub
 	}
 }
