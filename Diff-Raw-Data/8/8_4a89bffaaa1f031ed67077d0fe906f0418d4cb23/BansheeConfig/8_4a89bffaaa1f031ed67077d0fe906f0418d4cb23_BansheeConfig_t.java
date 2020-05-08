 /* Copyright 2009 Sean Hodges <seanhodges@bluebottle.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package tesla.app.command.provider.app;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import tesla.app.command.Command;
 import tesla.app.command.helper.DBusHelper;
 import tesla.app.command.provider.IConfigProvider;
 
 public class BansheeConfig implements IConfigProvider {
 
 	public String getCommand(String key) {
 		final String dest = "org.bansheeproject.Banshee";
 		List<String> args = new ArrayList<String>();
 		String out = null;
 		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
 			out = new DBusHelper().compileMethodCall(dest, "/org/bansheeproject/Banshee/PlayerEngine", 
 				"org.bansheeproject.Banshee.PlayerEngine.TogglePlaying");
 		}
 		else if (key.equals(Command.PREV)) {
 			args.add(new DBusHelper().evaluateArg("false"));
 			out = new DBusHelper().compileMethodCall(dest, "/org/bansheeproject/Banshee/PlaybackController", 
 				"org.bansheeproject.Banshee.PlaybackController.Previous", args);
 		}
 		else if (key.equals(Command.NEXT)) {
 			args.add(new DBusHelper().evaluateArg("false"));
 			out = new DBusHelper().compileMethodCall(dest, "/org/bansheeproject/Banshee/PlaybackController", 
 				"org.bansheeproject.Banshee.PlaybackController.Next", args);
 		}
 		else if (key.equals(Command.VOL_CHANGE)) {
 			args.add(new DBusHelper().evaluateArg("%u16"));
 			out = new DBusHelper().compileMethodCall(dest, "/org/bansheeproject/Banshee/PlayerEngine", 
 				"org.bansheeproject.Banshee.PlayerEngine.SetVolume", args);
 		}
 		else if (key.equals(Command.VOL_MUTE)) {
 			args.add(new DBusHelper().evaluateArg("uint16:0.0"));
 			out = new DBusHelper().compileMethodCall(dest, "/org/bansheeproject/Banshee/PlayerEngine", 
 				"org.bansheeproject.Banshee.PlayerEngine.SetVolume", args);
 		}
 		else if (key.equals(Command.VOL_CURRENT)) {
 			out = new DBusHelper().compileMethodCall(dest, "/org/bansheeproject/Banshee/PlayerEngine", 
 				"org.bansheeproject.Banshee.PlayerEngine.GetVolume");
 		}
 		else if (key.equals(Command.GET_MEDIA_INFO)) {
 			out = new DBusHelper().compileMethodCall(dest, "/org/bansheeproject/Banshee/PlayerEngine", 
 				"org.bansheeproject.Banshee.PlayerEngine.GetCurrentTrack");
 		}
 		else if (key.equals(Command.IS_PLAYING)) {
 			out = new DBusHelper().compileMethodCall(dest, "/org/bansheeproject/Banshee/PlayerEngine", 
 				"org.bansheeproject.Banshee.PlayerEngine.GetCurrentState");
 		}
 		else if (key.equals(Command.GET_MEDIA_POSITION)) {
 			out = new DBusHelper().compileMethodCall(dest, "/org/bansheeproject/Banshee/PlayerEngine", 
 				"org.bansheeproject.Banshee.PlayerEngine.GetPosition");
 		}
 		else if (key.equals(Command.GET_MEDIA_LENGTH)) {
 			out = new DBusHelper().compileMethodCall(dest, "/org/bansheeproject/Banshee/PlayerEngine", 
 			"org.bansheeproject.Banshee.PlayerEngine.GetLength");
 		}
 		else if (key.equals(Command.SET_MEDIA_POSITION)) {
 			args.add(new DBusHelper().evaluateArg("%u32"));
 			out = new DBusHelper().compileMethodCall(dest, "/org/bansheeproject/Banshee/PlayerEngine", 
 				"org.bansheeproject.Banshee.PlayerEngine.SetPosition", args);
 		}
 		
 		return out;
 	}
 
 	public Map<String, String> getSettings(String key) {
 		Map<String, String> settings = new HashMap<String, String>();
 		if (key.equals(Command.VOL_CURRENT)) {
 			settings.put("MIN", "0.0");
 			settings.put("MAX", "100.0");
 		}
 		else if (key.equals(Command.GET_MEDIA_INFO)) {
 			settings.put("ENABLED", "true");
 		}
 		else if (key.equals(Command.IS_PLAYING)) {
 			settings.put("ENABLED", "true");
 		}
 		else if (key.equals(Command.GET_MEDIA_POSITION)) {
 			settings.put("ENABLED", "true");
 		}
 		return settings;
 	}
 
 	public String getLaunchAppCommand() {
		StringBuilder builder = new StringBuilder();
		builder.append("dbus-send --print-reply --dest=org.bansheeproject.Banshee /org/bansheeproject/Banshee/ClientWindow org.bansheeproject.Banshee.ClientWindow.Present &>/dev/null; ");
		builder.append("if [[ $? != 0 ]]; then ");
		builder.append("DISPLAY=:0 banshee-1 &>/dev/null & sleep 5; ");
		builder.append("fi; ");
		builder.append("echo success");
		return builder.toString();
 	}
 }
