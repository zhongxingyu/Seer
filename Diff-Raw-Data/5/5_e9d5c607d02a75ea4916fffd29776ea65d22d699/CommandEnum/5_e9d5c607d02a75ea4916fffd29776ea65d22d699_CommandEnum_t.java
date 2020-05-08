 /*
  * Copyright (c) 2012-2013 Veniamin Isaias.
  *
  * This file is part of web4thejob.
  *
  * Web4thejob is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or any later version.
  *
  * Web4thejob is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with web4thejob.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.web4thejob.command;
 
 import java.util.*;
 
 /**
  * <p>The eligible command ids of the framework.</p>
  *
  * @author Veniamin Isaias
  * @see Command
  * @since 1.0.0
  */
 
 public class CommandEnum implements Comparable<CommandEnum> {
     private static Map<String, CommandEnum> commandsRegistry = new HashMap<String, CommandEnum>();
 
     public static final CommandEnum CONFIGURE_SETTINGS = new CommandEnum("CONFIGURE_SETTINGS",
             CommandEnum.CATEGORY_DEFAULT, true, true);
 
     public static final CommandEnum DESIGN_PANEL_ENTITY_VIEW = new CommandEnum("DESIGN_PANEL_ENTITY_VIEW");
     public static final CommandEnum DESIGN_PANEL_LIST_VIEW = new CommandEnum("DESIGN_PANEL_LIST_VIEW",
             CommandEnum.CATEGORY_DEFAULT, false, true);
 
     public static final CommandEnum DESIGN_PANEL_HTML_VIEW = new CommandEnum("DESIGN_PANEL_HTML_VIEW");
     public static final CommandEnum DESIGN_PANEL_IFRAME_VIEW = new CommandEnum("DESIGN_PANEL_IFRAME_VIEW",
             CommandEnum.CATEGORY_DEFAULT, false, true);
 
     public static final CommandEnum DESIGN_PANEL_BORDERED_VIEW = new CommandEnum("DESIGN_PANEL_BORDERED_VIEW");
     public static final CommandEnum DESIGN_PANEL_TABBED_VIEW = new CommandEnum("DESIGN_PANEL_TABBED_VIEW",
             CommandEnum.CATEGORY_DEFAULT, false, true);
 
     public static final CommandEnum DESIGN_PANEL_OTHER = new CommandEnum("DESIGN_PANEL_OTHER",
             CommandEnum.CATEGORY_DEFAULT, true, false);
 
     public static final CommandEnum SAVE_DESKTOP = new CommandEnum("SAVE_DESKTOP", CommandEnum.CATEGORY_DEFAULT,
             true, true);
     public static final CommandEnum SAVE_PANEL = new CommandEnum("SAVE_PANEL", CommandEnum.CATEGORY_DEFAULT, true,
             false);
     public static final CommandEnum SAVE_PANEL_AS = new CommandEnum("SAVE_PANEL_AS", CommandEnum.CATEGORY_DEFAULT,
             false,
             true);
 
     public static final CommandEnum DESIGN_PANEL = new CommandEnum("DESIGN_PANEL", CommandEnum.CATEGORY_DEFAULT,
             true, true,
             new CommandEnum[]{DESIGN_PANEL_ENTITY_VIEW,
                     DESIGN_PANEL_LIST_VIEW, DESIGN_PANEL_HTML_VIEW, DESIGN_PANEL_IFRAME_VIEW,
                     DESIGN_PANEL_TABBED_VIEW, DESIGN_PANEL_BORDERED_VIEW, DESIGN_PANEL_OTHER});
 
     public static final CommandEnum DESIGN_MODE = new CommandEnum("DESIGN_MODE", false);
     public static final CommandEnum LOCALIZATION_MODE = new CommandEnum("LOCALIZATION_MODE", false);
 
     public static final CommandEnum REFRESH_CONTEXT = new CommandEnum("REFRESH_CONTEXT",
             CommandEnum.CATEGORY_DEFAULT, true, false);
 
     public static final CommandEnum TOOLS_DROPDOWN = new CommandEnum("TOOLS_DROPDOWN", CommandEnum.CATEGORY_DEFAULT,
             false, false,
             new CommandEnum[]{DESIGN_MODE, LOCALIZATION_MODE,
                     DESIGN_PANEL, REFRESH_CONTEXT});
 
     public static final CommandEnum SESSION_INFO = new CommandEnum("SESSION_INFO");
     public static final CommandEnum CHANGE_PASSWORD = new CommandEnum("CHANGE_PASSWORD");
 
    public static final CommandEnum BROWSE_WIKI = new CommandEnum("BROWSE_WIKI",
            CommandEnum.CATEGORY_DEFAULT, false, false, null, "http://wiki.web4thejob.org");
     public static final CommandEnum REQUEST_SUPPORT = new CommandEnum("REQUEST_SUPPORT",
             CommandEnum.CATEGORY_DEFAULT, false, false, null, "https://forum.web4thejob.org");
     public static final CommandEnum REPORT_BUG = new CommandEnum("REPORT_BUG", CommandEnum.CATEGORY_DEFAULT, false,
             false, null, "https://github.com/web4thejob/home/issues");
 
 
     public static final CommandEnum COMMUNITY = new CommandEnum("COMMUNITY", CommandEnum.CATEGORY_DEFAULT, true,
            true, new CommandEnum[]{BROWSE_WIKI, REQUEST_SUPPORT, REPORT_BUG});
 
     public static final CommandEnum ABOUT = new CommandEnum("ABOUT");
     public static final CommandEnum LOGOUT = new CommandEnum("LOGOUT", CommandEnum.CATEGORY_DEFAULT, true, true);
 
     public static final CommandEnum USER_DROPDOWN = new CommandEnum("USER_DROPDOWN", CommandEnum.CATEGORY_DEFAULT,
             false, false,
             new CommandEnum[]{SESSION_INFO, SAVE_DESKTOP,
                     CHANGE_PASSWORD, COMMUNITY, ABOUT, LOGOUT});
 
     public static final CommandEnum QUERY = new CommandEnum("QUERY", CommandEnum.CATEGORY_CRUD);
     public static final CommandEnum REFRESH = new CommandEnum("REFRESH");
     public static final CommandEnum ADDNEW = new CommandEnum("ADDNEW", CommandEnum.CATEGORY_CRUD);
     public static final CommandEnum UPDATE = new CommandEnum("UPDATE", CommandEnum.CATEGORY_CRUD);
     public static final CommandEnum PRINT = new CommandEnum("PRINT");
     public static final CommandEnum DELETE = new CommandEnum("DELETE", CommandEnum.CATEGORY_CRUD, true, true);
 
     public static final CommandEnum QUERY_LOOKUP = new CommandEnum("QUERY_LOOKUP", CommandEnum.CATEGORY_DEFAULT,
             true, true,
             new CommandEnum[]{REFRESH, ADDNEW, UPDATE, DELETE});
     public static final CommandEnum RENDER_SCHEME_LOOKUP = new CommandEnum("RENDER_SCHEME_LOOKUP",
             CommandEnum.CATEGORY_DEFAULT, true, true,
             new CommandEnum[]{REFRESH, ADDNEW, UPDATE, DELETE});
 
     public static final CommandEnum SAVE = new CommandEnum("SAVE");
     public static final CommandEnum SAVE_AS = new CommandEnum("SAVE_AS");
     public static final CommandEnum SAVE_ADDNEW = new CommandEnum("SAVE_ADDNEW");
     public static final CommandEnum VALIDATE = new CommandEnum("VALIDATE");
 
     public static final CommandEnum SELECT = new CommandEnum("SELECT");
     public static final CommandEnum ADD = new CommandEnum("ADD");
     public static final CommandEnum EDIT = new CommandEnum("EDIT");
     public static final CommandEnum MOVE_UP = new CommandEnum("MOVE_UP");
     public static final CommandEnum MOVE_DOWN = new CommandEnum("MOVE_DOWN");
     public static final CommandEnum MOVE_LEFT = new CommandEnum("MOVE_LEFT");
     public static final CommandEnum MOVE_RIGHT = new CommandEnum("MOVE_RIGHT");
     public static final CommandEnum REMOVE = new CommandEnum("REMOVE", CommandEnum.CATEGORY_DEFAULT, true, true);
     public static final CommandEnum CLEAR = new CommandEnum("CLEAR", CommandEnum.CATEGORY_DEFAULT, true, true);
     public static final CommandEnum RELATED_PANELS = new CommandEnum("RELATED_PANELS", CommandEnum.CATEGORY_DEFAULT,
             true, true);
 
     public static final CommandEnum CONFIGURE_HEADERS = new CommandEnum("CONFIGURE_HEADERS");
 
     public static final CommandEnum RENDER_SETTINGS = new CommandEnum("RENDER_SETTINGS");
     public static final CommandEnum HIGHLIGHT_PANEL = new CommandEnum("HIGHLIGHT_PANEL", false);
     public static final CommandEnum CUT = new CommandEnum("CUT", CommandEnum.CATEGORY_DEFAULT,
             true, true);
     public static final CommandEnum PASTE = new CommandEnum("PASTE", CommandEnum.CATEGORY_DEFAULT,
             true, true);
     public static final CommandEnum DESTROY_PANEL = new CommandEnum("DESTROY_PANEL", CommandEnum.CATEGORY_DEFAULT,
             true, true);
     public static final CommandEnum RUNNING_MODE = new CommandEnum("RUNNING_MODE");
 
     public static final CommandEnum DESIGN = new CommandEnum("DESIGN", CommandEnum.CATEGORY_DEFAULT, true, true,
             new CommandEnum[]{CONFIGURE_SETTINGS, SAVE_PANEL, SAVE_PANEL_AS,
                     RENDER_SETTINGS, HIGHLIGHT_PANEL, CUT, PASTE, DESTROY_PANEL, RUNNING_MODE});
 
     // always last
     public static final CommandEnum LOCALIZE = new CommandEnum("LOCALIZE", CommandEnum.CATEGORY_DEFAULT, true, true);
 
     public static final String CATEGORY_DEFAULT = "default";
     public static final String CATEGORY_CRUD = "CRUD";
     private static final SortedSet<CommandEnum> EMPTY_SUBCOMMANDS = Collections.unmodifiableSortedSet(new
             TreeSet<CommandEnum>());
 
     private final String category;
     private final boolean requiresEndSeparator;
     private final boolean requiresStartSeparator;
     private final SortedSet<CommandEnum> subcommands;
     private final Object value;
     private final String name;
     private final int ordinal;
 
     public SortedSet<CommandEnum> getSubcommands() {
         if (subcommands != null) return subcommands;
         else return EMPTY_SUBCOMMANDS;
     }
 
     CommandEnum(String name) {
         this(name, CATEGORY_DEFAULT, false, false, null, null);
     }
 
     CommandEnum(String name, Object value) {
         this(name, CATEGORY_DEFAULT, false, false, null, value);
     }
 
     CommandEnum(String name, String category) {
         this(name, category, false, false, null, null);
     }
 
     CommandEnum(String name, String category, boolean requiresStartSeparator, boolean requiresEndSeparator) {
         this(name, category, requiresStartSeparator, requiresEndSeparator, null, null);
     }
 
     CommandEnum(String name, String category, boolean requiresStartSeparator, boolean requiresEndSeparator,
                 CommandEnum[] subcommands) {
         this(name, category, requiresStartSeparator, requiresEndSeparator, subcommands, null);
     }
 
 
     CommandEnum(String name, String category, boolean requiresStartSeparator, boolean requiresEndSeparator,
                 CommandEnum[] subcommands, Object value) {
         this.name = name;
         this.category = category;
         this.requiresStartSeparator = requiresStartSeparator;
         this.requiresEndSeparator = requiresEndSeparator;
         this.value = value;
         this.ordinal = addToRegistry(this);
 
         if (subcommands != null) {
             SortedSet<CommandEnum> ref = new TreeSet<CommandEnum>();
             Collections.addAll(ref, subcommands);
             this.subcommands = Collections.unmodifiableSortedSet(ref);
         } else {
             this.subcommands = null;
         }
     }
 
     public String getCategory() {
         return category;
     }
 
     public boolean isCrud() {
         return CATEGORY_CRUD.equals(category);
     }
 
     public boolean isRequiresEndSeparator() {
         return requiresEndSeparator;
     }
 
     public boolean isRequiresStartSeparator() {
         return requiresStartSeparator;
     }
 
     public Object getValue() {
         return value;
     }
 
     public String name() {
         return name;
     }
 
     private synchronized static int addToRegistry(CommandEnum commandEnum) {
         int ordinal;
         commandsRegistry.put(commandEnum.name(), commandEnum);
         ordinal = commandsRegistry.size();
 
         if (!commandEnum.getClass().equals(CommandEnum.class)) {
             //this command comes from an extension module so offset its ordinal order
             //so that it always appears after core commands.
             ordinal += 20000;
         }
 
         return ordinal;
     }
 
     @Override
     public int compareTo(CommandEnum o) {
         return Integer.valueOf(ordinal).compareTo(o.ordinal());
     }
 
     public static Collection<CommandEnum> values() {
         List<CommandEnum> list = new ArrayList<CommandEnum>(commandsRegistry.values());
         Collections.sort(list);
         return Collections.unmodifiableList(list);
     }
 
     public static CommandEnum valueOf(String name) {
         return commandsRegistry.get(name);
     }
 
     public int ordinal() {
         return ordinal;
     }
 
     @Override
     public String toString() {
         return name;
     }
 
 }
