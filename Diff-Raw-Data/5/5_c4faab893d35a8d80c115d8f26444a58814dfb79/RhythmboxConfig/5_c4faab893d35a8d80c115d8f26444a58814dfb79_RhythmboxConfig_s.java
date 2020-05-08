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
 				"org.gnome.Rhythmbox.Player.getPlayingUri", false);
 			out = new RhythmDBHelper().compileQuery(uriCommand);
 		}
 		else if (key.equals(Command.IS_PLAYING)) {
 			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
 				"org.gnome.Rhythmbox.Player.getPlaying");
 		}
 		else if (key.equals(Command.GET_MEDIA_POSITION)) {
			String isPlaying = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
			"org.gnome.Rhythmbox.Player.getPlaying", false) + " | awk \"/true$/{print \\\"true\\\"}\" ";
 			String getElapsed = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
 				"org.gnome.Rhythmbox.Player.getElapsed");
 			String outputZero = "method return sender=:1.74 -> dest=:1.83 reply_serial=2\n   uint32 0";
			out = "if [[ $(" + isPlaying + ") == 'true' ]]; then " + getElapsed + "; else echo -e '" + outputZero + "'; fi";
 		}
 		else if (key.equals(Command.GET_MEDIA_LENGTH)) {
 			String isPlaying = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
 			"org.gnome.Rhythmbox.Player.getPlaying", false) + " | awk \"/true$/{print \\\"true\\\"}\" ";
 			String uriCommand = "\"$(" + new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
 				"org.gnome.Rhythmbox.Player.getPlayingUri", false) + " | grep string | cut -d '\"' -f 2)\"";
 			args.add(new DBusHelper().evaluateArg(uriCommand));
 			String songQueryCommand = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Shell", 
 					"org.gnome.Rhythmbox.Shell.getSongProperties", args, false);
 			String getSongLength = songQueryCommand + " | grep duration -A 1 | grep variant | sed -e \"s/[^0-9]*//\" | cut -d ' ' -f 2";
 			String outputZero = "0";
 			out = "if [[ $(" + isPlaying + ") == 'true' ]]; then " + getSongLength + "; else echo -e '" + outputZero + "'; fi";
 		}
 		else if (key.equals(Command.SET_MEDIA_POSITION)) {
 			args.add(new DBusHelper().evaluateArg("%u32"));
 			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
 				"org.gnome.Rhythmbox.Player.setElapsed", args);
 		}
 		else if (key.equals(Command.GET_PLAYLIST)) {
 			// Get artist and album.. somehow
 			// Query RhythmDB for matching songs
 			//out = new RhythmDBHelper().compileQuery(artist, album);
 			// Add getOutputAsList() impl for RhythmDB helper
 		}
 		else if (key.equals(Command.GET_PLAYLIST_SELECTION)) {
 			// Remove this command, and match selection against currently playing song title
 		}
 		else if (key.equals(Command.SET_PLAYLIST_SELECTION)) {
 			args.add(new DBusHelper().evaluateArg("%s"));
 			args.add(new DBusHelper().evaluateArg("false"));
 			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Shell", 
 				"org.gnome.Rhythmbox.Shell.loadURI", args);
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
 		}
 		else if (key.equals(Command.IS_PLAYING)) {
 			settings.put("ENABLED", "true");
 		}
 		else if (key.equals(Command.GET_MEDIA_POSITION)) {
 			settings.put("ENABLED", "true");
 		}
 		else if (key.equals(Command.GET_PLAYLIST)) {
 			settings.put("ENABLED", "false");
 		}
 		return settings;
 	}
 
 	public String getLaunchAppCommand() {
 		return "pidof rhythmbox 1>/dev/null || DISPLAY=:0 rhythmbox &>/dev/null & sleep 5 && echo success";
 	}
 }
