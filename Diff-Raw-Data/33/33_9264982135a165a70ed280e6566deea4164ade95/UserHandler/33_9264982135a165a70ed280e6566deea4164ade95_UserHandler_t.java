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
 package mpaf.ice;
 
 import java.util.Arrays;
 import java.util.Date;
import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import Murmur.Ban;
 import Murmur.InvalidSecretException;
 import Murmur.InvalidSessionException;
 import Murmur.ServerBootedException;
 import Murmur.User;
 
 public class UserHandler {
 	IceModel im;
 	
 	public UserHandler(IceModel im) {
 		this.im = im;
 	}
 	
 	public Map<Integer, User> getUsers(Integer serverId) throws InvalidSecretException, ServerBootedException {
 		Murmur.ServerPrx server = this.im.getMeta().getServer(serverId);
 		if(server != null)
 			return server.getUsers();
 		else
 			return null;
 	}
 	
 	public void kickUser(Integer serverId, User user, String reason) throws InvalidSecretException, InvalidSessionException, ServerBootedException {
 		Murmur.ServerPrx server = this.im.getMeta().getServer(serverId);
 		if(server != null)
 			server.kickUser(user.session, reason);
 	}
 	
 	public void banUser(Integer serverId, User user, String reason) throws InvalidSecretException, ServerBootedException {
 		Murmur.ServerPrx server = this.im.getMeta().getServer(serverId);
 		if(server == null)
 			return;
 		Ban[] bans = server.getBans();
		List<Ban> banlist = new LinkedList<Ban>(Arrays.asList(bans));		
 		Murmur.Ban ban = new Murmur.Ban();
 		ban.address = user.address;
 		ban.name = user.name;
 		ban.start = (int)new Date().getTime();
 		ban.reason = reason;
 		banlist.add(ban);
 		server.setBans((Ban[]) banlist.toArray());
 	}
 	
 	public void removeBan(Integer serverId, String name) throws InvalidSecretException, ServerBootedException {
 		Murmur.ServerPrx server = this.im.getMeta().getServer(serverId);
 		if(server == null)
 			return;
 		Ban[] bans = server.getBans();
		List<Ban> banlist = new LinkedList<Ban>(Arrays.asList(bans));
 		for(Murmur.Ban ban : banlist) {
 			if(ban.name == name) {
 				banlist.remove(ban);
 				server.setBans((Ban[])banlist.toArray());
 			}
 		}
 	}
 	
 	public List<Ban> getBans(Integer serverId) throws InvalidSecretException, ServerBootedException{
 		Murmur.ServerPrx server = this.im.getMeta().getServer(serverId);
 		if(server == null)
 			return null;
 		Ban[] bans = server.getBans();
		List<Ban> banlist = new LinkedList<Ban>(Arrays.asList(bans));
 		return banlist;
 	}
 }
