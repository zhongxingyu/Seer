 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License. When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 package goBible.views;
 
 import goBible.base.GoBible;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Vector;
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 import javax.microedition.io.file.FileSystemRegistry;
 import javax.microedition.lcdui.*;
 
 /**
  * The
  * <code>FileBrowser</code> custom component lets the user list files and
  * directories. It's uses FileConnection Optional Package (JSR 75). The
  * FileConnection Optional Package APIs give J2ME devices access to file systems
  * residing on mobile devices, primarily access to removable storage media such
  * as external memory cards.
  *
  * @author breh
  */
 public class FileBrowser extends List implements CommandListener {
 
     private GoBible goBible;
     // Commands    
     public static final Command SELECT_FILE_COMMAND = new Command(GoBible.getString("UI-Select"), Command.OK, 1);
     public static final Command CANCEL_COMMAND = new Command(GoBible.getString("UI-Cancel"), Command.CANCEL, 0);
     private String currDirName;
     private String currFile;
     private Image dirIcon;
     private Image fileIcon;
     private Image selectedIcon;
     private Image[] iconList;
     private CommandListener commandListener;
 
     /*
      * special string denotes upper directory
      */
     private static final String UP_DIRECTORY = "..";
 
     /*
      * special string that denotes upper directory accessible by this browser.
      * this virtual directory contains all roots.
      */
     private static final String MEGA_ROOT = "/";
 
     /*
      * separator string as defined by FC specification
      */
     private static final String SEP_STR = "/";
 
     /*
      * separator character as defined by FC specification
      */
     private static final char SEP = '/';
     private Display display;
     private String selectedURL;
     private String filter = null;
     private String title;
 
     /**
      * Creates a new instance of FileBrowser for given
      * <code>Display</code> object.
      *
      * @param goBible reference to main midlet
      */
     public FileBrowser(GoBible goBible) {
         super(GoBible.getString("UI-Change-Translation"), Choice.IMPLICIT);
 
         this.goBible = goBible;
 
         if (goBible.getCurrentBookFolder() != null) {
             currDirName = goBible.getCurrentBookFolder();
         } else {
             currDirName = MEGA_ROOT;
         }
 
         this.display = goBible.display;
         super.setCommandListener(this);
 
         setSelectCommand(SELECT_FILE_COMMAND);
         try {
             dirIcon = Image.createImage("/dir.png");
         } catch (IOException e) {
             dirIcon = null;
         }
         try {
             fileIcon = Image.createImage("/book.png");
         } catch (IOException e) {
             fileIcon = null;
         }
         try {
             selectedIcon = Image.createImage("/selected.png");
         } catch (IOException e) {
             selectedIcon = null;
         }
         iconList = new Image[]{fileIcon, dirIcon, selectedIcon};
 
         showDir();
     }
 
     /**
      * Sets up an initial directory, i.e. possibly different from MEGA_ROOT
      *
      * @param dir
      */
     public void setDir(final String dir) {
 
         if (dir != null) {
             // not necessary to re-check current dir, saves one permission question
             // if current directory is removed, some funnies might happen, though
             if (!dir.equals(currDirName)) {
                 try {
                     FileConnection currDir =
                             (FileConnection) Connector.open(
                             "file:///" + dir, Connector.READ);
                     try {
                         if (currDir.exists() && currDir.isDirectory()) {
                             System.out.println("[FileBrowser.setDir()] called");
                             currDirName = dir;
                             showDir();
                         }
                     } finally {
                         currDir.close();
                     }
                 } catch (IOException ioe) {
                     ioe.printStackTrace();
                 }
             }
         }
     }
 
     /**
      * Data files are truncated in disk, this method tries to append spaces to
      * relevant places and removes file extension
      *
      * @param fileName file name to display
      * @return
      */
     private String formatItemString(String fileName) {
         char[] nameChars = fileName.toCharArray();
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < nameChars.length; i++) {
 
             // not in the beginning or not in the end
             if (i != 0 && i < nameChars.length - 1) {
                 // if current character is not uppercase and                          
                 // if next character is digit, uppercase or bracket, append with char + space 
                 if (Character.isLowerCase(nameChars[i])
                         && (Character.isDigit(nameChars[i + 1])
                         || Character.isUpperCase(nameChars[i + 1])
                         || nameChars[i + 1] == '('
                         || nameChars[i + 1] == ')')) {
                     sb.append(nameChars[i]);
                     sb.append(" ");
                 } else {
                     sb.append(nameChars[i]);
 
                     // special case: if current char is closing bracket, append with space
                     if (nameChars[i] == ')') {
                         sb.append(" ");
                     }
                 }
             } else {
                 sb.append(nameChars[i]);
             }
         }
         // -4 to remove extension
         return sb.toString().substring(0, sb.toString().length() - 4);
     }
 
     /**
      * Show directory
      */
     private void showDir() {
         try {
             showCurrDir();
         } catch (SecurityException e) {
             Alert alert = new Alert("Error", "You are not authorized to access the restricted API", null, AlertType.ERROR);
             alert.setTimeout(2000);
             display.setCurrent(alert, FileBrowser.this);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Indicates that a command event has occurred on Displayable d.
      *
      * @param c a
      * <code>Command</code> object identifying the command. This is either one
      * of the applications have been added to
      * <code>Displayable</code> with
      * <code>addCommand(Command)</code> or is the implicit
      * <code>SELECT_COMMAND</code> of List.
      * @param d the
      * <code>Displayable</code> on which this event has occurred
      */
     public final void commandAction(final Command c, final Displayable d) {
         if (c.equals(SELECT_FILE_COMMAND)) {
             List curr = (List) d;
             currFile = curr.getString(curr.getSelectedIndex());
 
             if (currFile.endsWith(SEP_STR) || currFile.equals(UP_DIRECTORY)) {
                 openDir(currFile);
             } else {
                 // System.out.println("[FileBrowser.commandAction] selected file="+ goBible.translationItemStringToFilename(currFile)+", gobible.getTranslation()="+goBible.getTranslation());
 
                 // if user selected current translation book, just cancel
                 if (currDirName.equals(goBible.bookUrl)
                         && goBible.translationItemStringToFilename(currFile).equals(goBible.getTranslation())) {
                     goBible.display.setCurrent(goBible.bibleCanvas);
                     goBible.showMainScreen();
                     return;
                 }
 
                 goBible.bookUrl = currDirName;
                 goBible.setTranslation(currFile);
                 updateSelectedIcon(curr);
                 goBible.run();
                 goBible.display.setCurrent(goBible.bibleCanvas);
 
                 doDismiss();
             }
         } else if (c.equals(CANCEL_COMMAND)) {
             goBible.display.setCurrent(goBible.bibleCanvas);
             goBible.showMainScreen();
 
         } else {
             commandListener.commandAction(c, d);
         }
     }
 
     /**
      * Updates currently selected item's icon
      *
      * @param items Visible list of items
      */
     private void updateSelectedIcon(List items) {
         int length = items.size();
         for (int i = 0; i < length; i++) {
             if (currDirName.equals(goBible.bookUrl)
                     && items.getString(i).equals(currFile)) {
                 items.set(i, items.getString(i), selectedIcon);
            } else {
                 items.set(i, items.getString(i), fileIcon);
             }
         }
     }
 
     /**
      * Sets component's title.
      *
      * @param title component's title.
      */
     public final void setTitle(final String title) {
         this.title = title;
         super.setTitle(title);
     }
 
     /**
      * Show file list in the current directory .
      */
     private void showCurrDir() {
         if (title == null) {
             super.setTitle(currDirName);
         }
         Enumeration e = null;
         FileConnection currDir = null;
 
         deleteAll();
         if (MEGA_ROOT.equals(currDirName)) {
             e = FileSystemRegistry.listRoots();
         } else {
             try {
                 currDir =
                         (FileConnection) Connector.open(
                         "file:///" + currDirName, Connector.READ);
                 e = currDir.list();
                 System.out.println("[FileBrowser.showCurrDir()] called");
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
             append(UP_DIRECTORY, dirIcon);
         }
 
         if (e == null) {
             try {
                 currDir.close();
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
             return;
         }
 
         final Vector directoriesVector = new Vector();
         final Vector filesVector = new Vector();
 
         while (e.hasMoreElements()) {
             String fileName = (String) e.nextElement();
             if (fileName.charAt(fileName.length() - 1) == SEP) {
                 // This is directory
                 directoriesVector.addElement(fileName);
             } else {
                 // this is regular file
                 boolean append = false;
 
                 final String fileNameLW = fileName.toLowerCase();
 
                 for (int i = 0; i < GoBible.SUPPORTED_FILE_EXTENSIONS.length; i++) {
                     if (fileNameLW.endsWith(GoBible.SUPPORTED_FILE_EXTENSIONS[i])) {
                         append = true;
                         break;
                     }
                 }
 
                 if (append) {
                     // filenames displayed without extension
                     filesVector.addElement(formatItemString(fileName));
                 }
             }
         }
 
         if (!directoriesVector.isEmpty()) {
             final String[] directories = new String[directoriesVector.size()];
             directoriesVector.copyInto(directories);
             sortStringArray(directories);
             int length = directories.length;
             for (int i = 0; i < length; i++) {
                 append(directories[i], dirIcon);
             }
         }
 
         if (!filesVector.isEmpty()) {
             final String[] files = new String[filesVector.size()];
             filesVector.copyInto(files);
             sortStringArray(files);
             int length = files.length;
             for (int i = 0; i < length; i++) {
                 if (currDirName.equals(goBible.bookUrl)
                         && goBible.translationItemStringToFilename(files[i]).equals(goBible.getTranslation())) {
                     append(files[i], selectedIcon);
                 } else {
                     append(files[i], fileIcon);
                 }
             }
         }
 
         if (currDir != null) {
             try {
                 currDir.close();
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
         }
     }
 
     /**
      * Open selected directory
      *
      * @param fileName directory name
      */
     private void openDir(final String fileName) {
         /*
          * In case of directory just change the current directory and show it
          */
         if (currDirName.equals(MEGA_ROOT)) {
             if (fileName.equals(UP_DIRECTORY)) {
                 // can not go up from MEGA_ROOT
                 return;
             }
             currDirName = fileName;
         } else if (fileName.equals(UP_DIRECTORY)) {
             // Go up one directory
             // TODO use setFileConnection when implemented
             int i = currDirName.lastIndexOf(SEP, currDirName.length() - 2);
             if (i != -1) {
                 currDirName = currDirName.substring(0, i + 1);
             } else {
                 currDirName = MEGA_ROOT;
             }
         } else {
             currDirName += fileName;
         }
         showDir();
     }
 
     /**
      * Returns selected file as a
      * <code>FileConnection</code> object.
      *
      * @return non null
      * <code>FileConection</code> object
      */
     public final FileConnection getSelectedFile() throws IOException {
         FileConnection fileConnection =
                 (FileConnection) Connector.open(selectedURL);
         return fileConnection;
     }
 
     /**
      * Returns selected
      * <code>FileURL</code> object.
      *
      * @return non null
      * <code>FileURL</code> object
      */
     public final String getSelectedFileURL() {
         return selectedURL;
     }
 
     /**
      * Sets the file filter.
      *
      * @param filter file filter String object
      */
     public final void setFilter(final String filter) {
         this.filter = filter;
     }
 
     /**
      * Returns command listener.
      *
      * @return non null
      * <code>CommandListener</code> object
      */
     protected final CommandListener getCommandListener() {
         return commandListener;
     }
 
     /**
      * Sets command listener to this component.
      *
      * @param commandListener
      * <code>CommandListener</code> to be used
      */
     public final void setCommandListener(final CommandListener commandListener) {
 
         this.commandListener = commandListener;
     }
 
     /**
      * Sets selected URL handle from <code>currDirName</code> and <code>currFile</code>
      */
     private void doDismiss() {
         selectedURL = "file:///" + currDirName + currFile;
     }
 
     /**
      * Sort list items alphabetically. Uses string array instead of vector for
      * memory reasons even it would be convenient here.
      *
      * @param strings Items to sort
      */
     protected static void sortStringArray(final String[] strings) {
 
         // useful results require lowercase strings
         final String[] lowercaseStrings = new String[strings.length];
         for (int i = 0; i < strings.length; i++) {
             lowercaseStrings[i] = strings[i].toLowerCase();
         }
 
         int n = strings.length;
         String temp;
 
         for (int i = 0; i < n; i++) {
             for (int j = 1; j < (n - i); j++) {
                 if (lowercaseStrings[j - 1].compareTo(lowercaseStrings[j]) > 0) {
                     // swap
                     temp = strings[j - 1];
                     strings[j - 1] = strings[j];
                     strings[j] = temp;
 
                     temp = lowercaseStrings[j - 1];
                     lowercaseStrings[j - 1] = lowercaseStrings[j];
                     lowercaseStrings[j] = temp;
                 }
             }
         }
     }
 }
