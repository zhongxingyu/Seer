 /*
  * Copyright (C) 2013 Lord_Ralex
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.ae97.ralexbot.permissions;
 
 import net.ae97.ralexbot.RalexBot;
 import net.ae97.ralexbot.api.events.PermissionEvent;
 import net.ae97.ralexbot.api.users.User;
 import net.ae97.ralexbot.settings.Settings;
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @version 1.0
  * @author Lord_Ralex
  */
 public class PermissionManager {
 
     private final Settings permFile;
 
     public PermissionManager() {
         permFile = new Settings(new File("permissions", "permFile.yml"));
     }
 
     public void reloadFile() {
         permFile.load();
     }
 
     public void runPermissionEvent(PermissionEvent event) {
         User user = event.getUser();
        RalexBot.log("Updating perms for " + user.getNick());
         String ver = user.isVerified();
        RalexBot.log("Is verified: " + ver);
         if (ver == null || ver.isEmpty()) {
             return;
         }
         Map<String, Set<Permission>> existing = user.getPermissions();
        for (String key : existing.keySet()) {
            for (Permission perm : existing.get(key)) {
                 user.removePermission(key, perm.getName());
             }
         }
         List<String> list = permFile.getStringList(ver);
         for (String line : list) {
             String chan = line.split("\\|")[0];
             String perm = line.split("\\|")[1];
             if (chan.isEmpty()) {
                 chan = null;
             }
             user.addPermission(chan, perm);
         }
     }
 }
