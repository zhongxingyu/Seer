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
 import tesla.app.command.helper.RhythmDBHelper;
 import tesla.app.command.provider.IConfigProvider;
 import tesla.app.mediainfo.MediaInfo;
 
 public class RhythmboxConfig implements IConfigProvider {
 
 	public String getCommand(String key) {
 		final String dest = "org.gnome.Rhythmbox";
 		List<String> args = new ArrayList<String>();
 		String out = null;
 		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
 			args.add(new DBusHelper().evaluateArg("false"));
 			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
 				"org.gnome.Rhythmbox.Player.playPause", args);
 		}
 		else if (key.equals(Command.PREV)) {
 			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
 				"org.gnome.Rhythmbox.Player.previous");
 		}
 		else if (key.equals(Command.NEXT)) {
 			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
 				"org.gnome.Rhythmbox.Player.next");
 		}
 		else if (key.equals(Command.VOL_CHANGE)) {
 			args.add(new DBusHelper().evaluateArg("%f"));
 			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
 				"org.gnome.Rhythmbox.Player.setVolume", args);
 		}
 		else if (key.equals(Command.VOL_MUTE)) {
 			args.add(new DBusHelper().evaluateArg("0.0"));
 			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
 				"org.gnome.Rhythmbox.Player.setVolume", args);
 		}
 		else if (key.equals(Command.VOL_CURRENT)) {
 			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
 				"org.gnome.Rhythmbox.Player.getVolume");
 		}
 		else if (key.equals(Command.GET_MEDIA_INFO)) {
 			String uriCommand = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
 				"org.gnome.Rhythmbox.Player.getPlayingUri");
 			out = new RhythmDBHelper().compileQuery(uriCommand);
 		}
 		else if (key.equals(Command.IS_PLAYING)) {
 			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
 				"org.gnome.Rhythmbox.Player.getPlaying");
 		}
 		
 		return out;
 	}
 
 	public Map<String, String> getSettings(String key) {
 		Map<String, String> settings = new HashMap<String, String>();
 		if (key.equals(Command.VOL_CURRENT)) {
 			settings.put("MIN", "0.0");
 			settings.put("MAX", "1.0");
 		}
 		else if (key.equals(Command.GET_MEDIA_INFO)) {
 			settings.put("ENABLED", "true");
 			settings.put("FORMAT", MediaInfo.FORMAT_RHYTHMDB);
 		}
 		else if (key.equals(Command.IS_PLAYING)) {
			settings.put("ENABLED", "true");
 		}
 		return settings;
 	}
 }
