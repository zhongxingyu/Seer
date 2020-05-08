 /*
  * This file is part of Taboo.
  *
  * Copyright (c) 2013 CraftFire <http://www.craftfire.com/>
  * Taboo is licensed under the GNU Lesser General Public License.
  *
  * Taboo is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Taboo is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.taboo;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import com.craftfire.commons.yaml.YamlException;
 import com.craftfire.commons.yaml.YamlNode;
 
 public class Taboo {
     //private static Random random = new Random(); // We were planning random replacements or something, but that can wait.
     private final TabooManager manager;
     private final String name;
     private final List<Pattern> patterns = new ArrayList<Pattern>();
     private final List<String> actions = new ArrayList<String>();
     private final String includePermission, excludePermission, replacement;
 
     public Taboo(TabooManager manager, YamlNode desc) throws YamlException, TabooException {
         this.manager = manager;
         this.name = desc.getName();
 
         if (!desc.hasChild("patterns") || !desc.getChild("patterns").isList()) {
             throw new TabooException("Can't create taboo \"" + this.name + "\": no patterns specified (or patterns property is not a list)");
         }
         parsePatterns(desc.getChild("patterns"));
 
         if (desc.hasChild("includePermission")) {
             this.includePermission = desc.getChild("includePermission").getString();
         } else {
             this.includePermission = null;
         }
 
         if (desc.hasChild("excludePermission")) {
             this.excludePermission = desc.getChild("excludePermission").getString();
         } else {
             this.excludePermission = null;
         }
 
         if (desc.hasChild("replacement")) {
             this.replacement = desc.getChild("replacement").getString();
         } else {
             this.replacement = null;
         }
 
         if (desc.hasChild("actions")) {
             YamlNode actions = desc.getChild("actions");
             if (actions.isList()) {
                 for (YamlNode node : actions.getChildrenList()) {
                     this.actions.add(node.getString());
                 }
             } else {
                 manager.getLogger().warning("Property \"actions\" of taboo \"" + this.name + "\" is not a list! Ignoring it.");
             }
         }
     }
 
     public String getName() {
         return this.name;
     }
 
     public boolean matches(String message, TabooPlayer player) {
         if (this.includePermission != null && !player.checkPermission(this.includePermission)) {
             this.manager.getLogger().debug("Player doesn't have the include permission: " + this.includePermission);
             return false;
         }
         if (this.excludePermission != null && player.checkPermission(this.excludePermission)) {
             this.manager.getLogger().debug("Player has the exclude permission: " + this.excludePermission);
             return false;
         }
         for (Pattern pattern : this.patterns) {
             this.manager.getLogger().debug("Checking pattern: " + pattern.toString());
             if (pattern.matcher(message).find()) {
                 return true;
             }
         }
         return false;
     }
 
     public String replace(String message) {
        if (this.replacement != null) {
            for (Pattern pattern : this.patterns) {
                message = pattern.matcher(message).replaceAll(this.replacement);
            }
         }
         return message;
     }
 
     public List<String> getActions() {
         return new ArrayList<String>(this.actions);
     }
 
     public String getReplacement() {
         return this.replacement;
     }
 
     public TabooManager getManager() {
         return this.manager;
     }
 
     private void parsePatterns(YamlNode patterns) throws YamlException {
         for (YamlNode node : patterns.getChildrenList()) {
             String str = node.getString();
             if (!str.startsWith("/") || !str.endsWith("/")) {
                 str = Pattern.quote(str);
             } else {
                 str = str.substring(1, str.length() - 1);
             }
             this.patterns.add(Pattern.compile(str));
         }
     }
 }
