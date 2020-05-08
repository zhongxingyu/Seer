 /**
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  */
 
 package truelauncher.client;
 
 import java.io.DataOutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 
 import truelauncher.utils.LauncherUtils;
 
 public class Auth {
 
 	public static void sendAuth1(String hostname, int port, String nick, String token, String password)
 	{
 		try {
 			//establish connection
 			Socket socket = new Socket();
 			socket.setSoTimeout(6000);
 			socket.setTcpNoDelay(true);
 			socket.connect(new InetSocketAddress(hostname, port), 6000);
 			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			//write handshake packet( format: packetid + authpacket(format:AuthMeSocketLoginSystem|nick|token|password) + host + port)
			String packetstring = "2" + "AuthMeSocketLoginSystem|" + nick + "|" + token + "|" + password + hostname + port;
             dos.write(packetstring.getBytes());
             socket.close();
 		} catch (Exception e) {
 			LauncherUtils.logError(e);
 		}
 	}
 	
 	public static void sendAuth2(String hostname, int port, String nick, String token, String password)
 	{
 		
 	}
 	
 }
