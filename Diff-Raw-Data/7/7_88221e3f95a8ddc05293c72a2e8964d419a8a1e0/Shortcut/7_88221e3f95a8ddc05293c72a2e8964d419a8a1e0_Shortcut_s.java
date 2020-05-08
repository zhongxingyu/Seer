 /*
  * PackJacket - GUI frontend to IzPack to make Java-based installers
  * Copyright (C) 2008 - 2009  Amandeep Grewal, Manodasan Wignarajah
  *
  * PackJacket is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * PackJacket is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with PackJacket.  If not, see <http://www.gnu.org/licenses/>.
  */
 package packjacket.xml;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * Contains fields for the Shortcut Database
  * @author Manodasan Wignarajah
  */
 public class Shortcut implements XMLInterface {
 
     /**
      * Used for PJC file backwards compability
      */
     public static final long serialVersionUID = 4L;
     /**
      * Stores the name of the shortcut
      */
     public String shortcuts_shortcut_name;
     /**
      * Stores the file which this shortcut points to
      */
     public String shortcuts_shortcut_target;
     /**
      * Stores the command line arguments for the target file if this shortcut is clicked
      */
     public String shortcuts_shortcut_commandLine;
     /**
      * Stores the working drirectory
      */
     public String shortcuts_shortcut_workingDirectory;
     /**
      * Stores the description of the shortcut
      */
     public String shortcuts_shortcut_description;
     /**
      * Stores the icon file for this shortcut
      */
     public String shortcuts_shortcut_iconFile;
     /**
      * Stores the icon index for the shortcut icon
      */
     public int shortcuts_shortcut_iconIndex;
     /**
      * Stores the initial state
      */
     public String shortcuts_shortcut_initialState;
     /**
      * Represents whether to put the shortcut file at that place
      */
     public boolean shortcuts_shortcut_programGroup;
     /**
      * Represents whether to put the shortcut file at that place
      */
     public boolean shortcuts_shortcut_desktop;
     /**
      * Represents whether to put the shortcut file at that place
      */
     public boolean shortcuts_shortcut_application;
     /**
      * Represents whether to put the shortcut file at that place
      */
     public boolean shortcuts_shortcut_startMenu;
     /**
      * Represents whether to put the shortcut file at that place
      */
     public boolean shortcuts_shortcut_startup;
     /**
      * Stores whether command line arguments are specified
      */
     public boolean shortcuts_shortcut_commandLineOption;
     /**
      * Stores whether a working directory is specified
      */
     public boolean shortcuts_shortcut_workingDirectoryOption;
     /**
      * Stores whether an icon file is specified
      */
     public boolean shortcuts_shortcut_iconFileOption;
     /**
      * Stores whether the terminal option is chosen
      */
     public boolean shortcuts_shortcut_terminal;
     /**
      * Stores whether a target file is specified
      */
     public boolean targetOption;
     /**
      * Stores whether a url is specified
      */
     public boolean urlOption;
     /**
      * Stores the url
      */
     public String url;
     /**
      * Stores whether the shortcut requires to be root as a boolean
      */
     public boolean requiresSudo;
     /**
      * Stores whether the shortcut is to be installed for all users or root only
      */
     public boolean allUsers;
     /**
      * The user with all the right permission, usually root
      */
     public String rootUser;
     /**
      * Stores all the packs which this shortcut is to be created for
      */
     public Collection<String> createForPacks = new ArrayList<String>();
 
     @Override
     public String toString() {
         return shortcuts_shortcut_name;
     }
 
     @Override
     public Shortcut clone() {
         Shortcut s = new Shortcut();
         s.shortcuts_shortcut_name = shortcuts_shortcut_name;
         s.shortcuts_shortcut_target = shortcuts_shortcut_target;
         s.shortcuts_shortcut_commandLine = shortcuts_shortcut_commandLine;
         s.shortcuts_shortcut_workingDirectory = shortcuts_shortcut_workingDirectory;
         s.shortcuts_shortcut_description = shortcuts_shortcut_description;
         s.shortcuts_shortcut_iconFile = shortcuts_shortcut_iconFile;
         s.shortcuts_shortcut_iconIndex = shortcuts_shortcut_iconIndex;
         s.shortcuts_shortcut_initialState = shortcuts_shortcut_initialState;
         s.shortcuts_shortcut_programGroup = shortcuts_shortcut_programGroup;
         s.shortcuts_shortcut_desktop = shortcuts_shortcut_desktop;
         s.shortcuts_shortcut_application = shortcuts_shortcut_application;
         s.shortcuts_shortcut_startMenu = shortcuts_shortcut_startMenu;
         s.shortcuts_shortcut_startup = shortcuts_shortcut_startup;
         s.shortcuts_shortcut_commandLineOption = shortcuts_shortcut_commandLineOption;
         s.shortcuts_shortcut_workingDirectoryOption = shortcuts_shortcut_workingDirectoryOption;
         s.shortcuts_shortcut_iconFileOption = shortcuts_shortcut_iconFileOption;
         s.shortcuts_shortcut_terminal = shortcuts_shortcut_terminal;
         s.targetOption = targetOption;
         s.urlOption = urlOption;
         s.url = url;
         s.requiresSudo = requiresSudo;
         s.allUsers = allUsers;
         s.rootUser = rootUser;
        s.createForPacks = new ArrayList<String>(createForPacks);
         return s;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         final Shortcut other = (Shortcut) obj;
         if ((this.shortcuts_shortcut_name == null) ? (other.shortcuts_shortcut_name != null) : !this.shortcuts_shortcut_name.equals(other.shortcuts_shortcut_name))
             return false;
         if ((this.shortcuts_shortcut_target == null) ? (other.shortcuts_shortcut_target != null) : !this.shortcuts_shortcut_target.equals(other.shortcuts_shortcut_target))
             return false;
         if ((this.shortcuts_shortcut_commandLine == null) ? (other.shortcuts_shortcut_commandLine != null) : !this.shortcuts_shortcut_commandLine.equals(other.shortcuts_shortcut_commandLine))
             return false;
         if ((this.shortcuts_shortcut_workingDirectory == null) ? (other.shortcuts_shortcut_workingDirectory != null) : !this.shortcuts_shortcut_workingDirectory.equals(other.shortcuts_shortcut_workingDirectory))
             return false;
         if ((this.shortcuts_shortcut_description == null) ? (other.shortcuts_shortcut_description != null) : !this.shortcuts_shortcut_description.equals(other.shortcuts_shortcut_description))
             return false;
         if ((this.shortcuts_shortcut_iconFile == null) ? (other.shortcuts_shortcut_iconFile != null) : !this.shortcuts_shortcut_iconFile.equals(other.shortcuts_shortcut_iconFile))
             return false;
         if (this.shortcuts_shortcut_iconIndex != other.shortcuts_shortcut_iconIndex)
             return false;
         if ((this.shortcuts_shortcut_initialState == null) ? (other.shortcuts_shortcut_initialState != null) : !this.shortcuts_shortcut_initialState.equals(other.shortcuts_shortcut_initialState))
             return false;
         if (this.shortcuts_shortcut_programGroup != other.shortcuts_shortcut_programGroup)
             return false;
         if (this.shortcuts_shortcut_desktop != other.shortcuts_shortcut_desktop)
             return false;
         if (this.shortcuts_shortcut_application != other.shortcuts_shortcut_application)
             return false;
         if (this.shortcuts_shortcut_startMenu != other.shortcuts_shortcut_startMenu)
             return false;
         if (this.shortcuts_shortcut_startup != other.shortcuts_shortcut_startup)
             return false;
         if (this.shortcuts_shortcut_commandLineOption != other.shortcuts_shortcut_commandLineOption)
             return false;
         if (this.shortcuts_shortcut_workingDirectoryOption != other.shortcuts_shortcut_workingDirectoryOption)
             return false;
         if (this.shortcuts_shortcut_iconFileOption != other.shortcuts_shortcut_iconFileOption)
             return false;
         if (this.shortcuts_shortcut_terminal != other.shortcuts_shortcut_terminal)
             return false;
         if (this.targetOption != other.targetOption)
             return false;
         if (this.urlOption != other.urlOption)
             return false;
         if ((this.url == null) ? (other.url != null) : !this.url.equals(other.url))
             return false;
         if (this.requiresSudo != other.requiresSudo)
             return false;
         if (this.allUsers != other.allUsers)
             return false;
         if ((this.rootUser == null) ? (other.rootUser != null) : !this.rootUser.equals(other.rootUser))
             return false;
         if (this.createForPacks != other.createForPacks && (this.createForPacks == null || !this.createForPacks.equals(other.createForPacks)))
             return false;
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 23 * hash + (this.shortcuts_shortcut_name != null ? this.shortcuts_shortcut_name.hashCode() : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_target != null ? this.shortcuts_shortcut_target.hashCode() : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_commandLine != null ? this.shortcuts_shortcut_commandLine.hashCode() : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_workingDirectory != null ? this.shortcuts_shortcut_workingDirectory.hashCode() : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_description != null ? this.shortcuts_shortcut_description.hashCode() : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_iconFile != null ? this.shortcuts_shortcut_iconFile.hashCode() : 0);
         hash = 23 * hash + this.shortcuts_shortcut_iconIndex;
         hash = 23 * hash + (this.shortcuts_shortcut_initialState != null ? this.shortcuts_shortcut_initialState.hashCode() : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_programGroup ? 1 : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_desktop ? 1 : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_application ? 1 : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_startMenu ? 1 : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_startup ? 1 : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_commandLineOption ? 1 : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_workingDirectoryOption ? 1 : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_iconFileOption ? 1 : 0);
         hash = 23 * hash + (this.shortcuts_shortcut_terminal ? 1 : 0);
         hash = 23 * hash + (this.targetOption ? 1 : 0);
         hash = 23 * hash + (this.urlOption ? 1 : 0);
         hash = 23 * hash + (this.url != null ? this.url.hashCode() : 0);
         hash = 23 * hash + (this.requiresSudo ? 1 : 0);
         hash = 23 * hash + (this.allUsers ? 1 : 0);
         hash = 23 * hash + (this.rootUser != null ? this.rootUser.hashCode() : 0);
         hash = 23 * hash + (this.createForPacks != null ? this.createForPacks.hashCode() : 0);
         return hash;
     }
 }
