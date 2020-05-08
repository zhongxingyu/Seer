 /*
  * FileSystem.java
  * Copyright (C) 2011,2012 Wannes De Smet
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
 package org.xenmaster.api.plugins;
 
 import com.google.gson.Gson;
 import java.util.HashMap;
 import org.xenmaster.api.entity.Host;
 import org.xenmaster.controller.BadAPICallException;
 
 /**
  * 
  * @created May 4, 2013
  * @author Jorgen Evens
  */
 public class Groups {
 
    protected final static String GROUPS_PLUGIN = "xm-groups";
     protected static Gson gson = new Gson();
 
 
     public static String[] getAll(Host host) throws BadAPICallException {
        String result = host.callPlugin(GROUPS_PLUGIN, "getAll", new HashMap<String, String>());
        return gson.fromJson(result, String[].class);
     }
 
 }
