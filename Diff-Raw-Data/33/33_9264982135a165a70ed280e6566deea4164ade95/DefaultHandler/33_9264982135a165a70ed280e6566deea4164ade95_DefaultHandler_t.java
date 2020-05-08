 /*******************************************************************************
  * This file is part of MPAF.
  * 
  * MPAF is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * MPAF is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with MPAF.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package mpaf.games;
 
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Arrays;
import java.util.LinkedList;
 import java.util.List;
 
 import mpaf.Logger;
 import mpaf.json.ContextJson;
 import mpaf.json.IdentityJson;
 import Ice.BooleanHolder;
 import Murmur.ACL;
 import Murmur.ACLListHolder;
 import Murmur.Channel;
 import Murmur.GroupListHolder;
 import Murmur.InvalidChannelException;
 import Murmur.InvalidSecretException;
 import Murmur.InvalidSessionException;
 import Murmur.ServerBootedException;
 import Murmur.Tree;
 import Murmur.User;
 
 public class DefaultHandler implements GameHandler {
 	Murmur.ServerPrx server;
 	Connection sqlC;
 	int game_channel_id = 0;
 	Tree gameTree = null;
 	boolean active = true;
 	
 	@Override
 	public void init(Connection conn, int gameChannelId) {
 		this.sqlC = conn;
 		this.game_channel_id = gameChannelId;
 		try {
 			updateGameTree();
 		} catch (InvalidSecretException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (ServerBootedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void addToDatabase() {
 		try {
 			PreparedStatement pst = sqlC
 					.prepareStatement("INSERT OR REPLACE INTO game_handlers (serverId, handlerName, gameChannelId, active) VALUES (?, ?, ?, ?)");
 			pst.setInt(1, server.id());
 			pst.setString(2, this.getClass().getName());
 			pst.setInt(3, game_channel_id);
 			pst.setInt(4, 1);
 			pst.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (InvalidSecretException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void activate() {
 		this.active = true;
 		try {
 			PreparedStatement pst = sqlC
 					.prepareStatement("UPDATE game_handlers SET active = 1 WHERE serverId = ? AND handlerName = ? AND gameChannelId = ?");
 			pst.setInt(1, server.id());
 			pst.setString(2, this.getClass().getName());
 			pst.setInt(3, game_channel_id);
 			pst.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (InvalidSecretException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void deactivate() {
 		this.active = false;
 		try {
 			PreparedStatement pst = sqlC
 					.prepareStatement("UPDATE game_handlers SET active = 0 WHERE serverId = '?' AND handlerName = '?' AND gameChannelId = '?'");
 			pst.setInt(1, server.id());
 			pst.setString(2, this.getClass().getName());
 			pst.setInt(3, game_channel_id);
 			pst.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (InvalidSecretException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean isActive() {
 		return this.active;
 	}
 	
 	@Override
 	public void handleUserState(User state) throws InvalidSecretException,
 			ServerBootedException, InvalidChannelException,
 			InvalidSessionException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void handleChannelRemoved(Channel state) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean moveUser(User state, ContextJson context, IdentityJson ijson)
 			throws InvalidSecretException, ServerBootedException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Tree updateGameTree() throws InvalidSecretException,
 			ServerBootedException {
 		_findTree(server.getTree());
 		return this.gameTree;
 	}
 	
 	private void _findTree(Tree parent){
 		for(Tree t : parent.children)
 		{
 			if(t.c.id == game_channel_id)
 				this.gameTree = t;
 			else 
 				_findTree(t);
 		}
 	}
 
 	@Override
 	public boolean isUserInGameChannel(User state) throws InvalidSecretException, ServerBootedException {
 		return _findUser(gameTree,state);
 	}
 	
 	private boolean _findUser(Tree channel, User user) {
 		boolean found = false;
 		if(_isInChannel(channel, user))
 			return true;
 		else {
 			for(Tree t : channel.children)
 			{
 				if(_findUser(t, user))
 					found = true;
 			}
 		}
 		return found;
 	}
 	
 	private boolean _isInChannel(Tree t, User state) {
 		List<User> users = Arrays.asList(t.users);
 		for(User u : users)
 		{
 			if(u.name.equalsIgnoreCase(state.name))
 			{
 				Logger.debug(this.getClass(), "FOUND in channel: "+t.c.name);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void addACLtoChannel(int channelid, ACL acl)
 			throws InvalidSecretException, ServerBootedException {
 		try {
 			// create variable holders
 			ACLListHolder acls = new ACLListHolder();
 			GroupListHolder groups = new GroupListHolder();
 			BooleanHolder inherited = new BooleanHolder();
 
 			// fill variable holders
 			server.getACL(channelid, acls, groups, inherited);
 
 			// append ACL
			List<ACL> aclList = new LinkedList<ACL>(Arrays.asList(acls.value));
 			aclList.add(acl);
 			
 			server.setACL(channelid, aclList.toArray(new ACL[aclList.size()]),
 					groups.value, inherited.value);
 		} catch (InvalidChannelException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public int getGameChannel() {
 		return game_channel_id;
 	}
 
 	@Override
 	public void setGameChannel(int channelId) throws InvalidSecretException, ServerBootedException, InvalidChannelException {
 		this.game_channel_id = channelId;
 		updateGameTree();
 		try {
 			PreparedStatement pst = sqlC
 					.prepareStatement("UPDATE game_handlers SET gameChannelId = ? WHERE serverId = ? AND handlerName = ?");
 			pst.setInt(1, game_channel_id);
 			pst.setInt(2, server.id());
 			pst.setString(3, this.getClass().getName());
 			pst.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (InvalidSecretException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public HandlerType getHandlerType() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
