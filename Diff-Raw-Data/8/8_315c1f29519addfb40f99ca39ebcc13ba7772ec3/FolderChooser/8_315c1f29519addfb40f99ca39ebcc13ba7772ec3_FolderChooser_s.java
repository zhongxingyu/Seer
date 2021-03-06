 /*
  * @(#)FolderChooser.java 10/9/2005
  *
  * Copyright 2002 - 2005 JIDE Software Inc. All rights reserved.
  */
 package com.jidesoft.swing;
 
 import com.jidesoft.plaf.LookAndFeelFactory;
 import com.jidesoft.plaf.UIDefaultsLookup;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileSystemView;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * <code>FolderChooser</code> provides a simple mechanism for the user to choose a folder.
  * <p/>
  * In addition to supporting the basic folder choosing function, it also supports create new folder, delete an existing
  * folder. Another useful feature is recent list. It allows you to set a list of recent selected folders so that user
  * can choose them directly instead of navigating to it in the file system tree.
  * <p/>
  * The following code pops up a folder chooser for user to choose a folder.
  * <pre>
  *    FolderChooser chooser = new FolderChooser();
  *    int returnVal = chooser.showOpenDialog(parent);
  *    if(returnVal == FolderChooser.APPROVE_OPTION) {
  *       System.out.println("You chose to open this file: " +
  *            chooser.getSelectedFile().getName());
  *    }
  * </pre>
  */
 public class FolderChooser extends JFileChooser {
 
     private static final String uiClassID = "FolderChooserUI";
 
     private List<String> _recentList;
 
     public final static String PROPERTY_RECENTLIST = "recentList";
 
     public FolderChooser() {
     }
 
     public FolderChooser(String currentDirectoryPath) {
         super(currentDirectoryPath);
     }
 
     public FolderChooser(File currentDirectory) {
         super(currentDirectory);
     }
 
     public FolderChooser(FileSystemView fsv) {
         super(fsv);
     }
 
     public FolderChooser(File currentDirectory, FileSystemView fsv) {
         super(currentDirectory, fsv);
     }
 
     public FolderChooser(String currentDirectoryPath, FileSystemView fsv) {
         super(currentDirectoryPath, fsv);
     }
 
     /**
      * Gets recent selected folder list. The element in the list is {@link File}.
      *
      * @return the recent selected folder list.
      */
     public List<String> getRecentList() {
         return _recentList;
     }
 
     /**
      * Sets the recent folder list. The element in the list should be {@link File}. Property change event on {@link
      * FolderChooser#PROPERTY_RECENTLIST} will be fired when recent folder list is changed.
      *
      * @param recentList the recent folder list.
      */
     public void setRecentList(List<String> recentList) {
         List<String> old = _recentList;
         _recentList = new ArrayList<String>();
         _recentList.addAll(recentList);
         firePropertyChange(PROPERTY_RECENTLIST, old, _recentList);
     }
 
     /**
      * Resets the UI property to a value from the current look and feel.
      *
      * @see JComponent#updateUI
      */
     @Override
     public void updateUI() {
         if (UIDefaultsLookup.get(uiClassID) == null) {
             LookAndFeelFactory.installJideExtension();
         }
         setUI(UIManager.getUI(this));
     }
 
     /**
      * Returns a string that specifies the name of the L&F class that renders this component.
      *
      * @return the string "FolderChooserUI"
      *
      * @see JComponent#getUIClassID
      * @see UIDefaults#getUI
      */
     @Override
     public String getUIClassID() {
         return uiClassID;
     }
 
 // we have to remove these two overridden method because it causes problem in
 // JFileChooser's setSelectedFile method where setCurrentDirectory
 // is called with selected file's parent folder.
 
 //    /**
 //     * Current directory concept doesn't make sense in the case of FolderChooser. So we
 //     * override this method of JFileChooser and delegate to {@link #setSelectedFile(java.io.File)}.
 //     *
 //     * @param dir
 //     */
 //    public void setCurrentDirectory(File dir) {
 //        super.setSelectedFile(dir);
 //    }
 //
 //    /**
 //     * Current directory concept doesn't make sense in the case of FolderChooser. So we
 //     * override this method of JFileChooser and delegate to {@link #getSelectedFile()}.
 //     *
 //     * @return the selected folder.
 //     */
 //    public File getCurrentDirectory() {
 //        return super.getSelectedFile();
 //    }
 
     /*
      * Added on 05/11/2008 in response to http://www.jidesoft.com/forum/viewtopic.php?p=26932#26932
      *
      * The addition below ensures Component#firePropertyChange is called, and thus fires the
      * appropriate 'bound property event' on all folder selection changes.
      *
      * @see BasicFolderChooserUI.FolderChooserSelectionListener#valueChanged
      */
 
     /**
      * Represents the highlighted folder in the 'folder tree' in the UI.
      *
      * @see #getSelectedFolder
      * @see #setSelectedFolder
      */
     private File _selectedFolder;
 
     /**
      * Returns the selected folder. This can be set either by the programmer via <code>setSelectedFolder</code> or by a
      * user action, such as selecting the folder from a 'folder tree' in the UI.
      *
      * @return the selected folder in the <i>folder tree<i>
      *
      * @see #setSelectedFolder
      */
     public File getSelectedFolder() {
         return _selectedFolder;
     }
 
     /**
      * Sets the selected folder.<p> </p> Property change event {@link JFileChooser#SELECTED_FILE_CHANGED_PROPERTY} will
      * be fired when a new folder is selected.
      *
      * @param selectedFolder the selected folder
      * @beaninfo preferred: true bound: true
      * @see #getSelectedFolder
      */
     public void setSelectedFolder(File selectedFolder) {
         File old = _selectedFolder;
         if (!JideSwingUtilities.equals(old, selectedFolder)) {
             _selectedFolder = selectedFolder;
             firePropertyChange(SELECTED_FILE_CHANGED_PROPERTY, old, _selectedFolder);
         }
     }
 
     /*
     * End of addition.
     *
     * Added on 05/11/2008 in response to http://www.jidesoft.com/forum/viewtopic.php?p=26932#26932
     */
 
     /*
     * Added on 05/27/2008 in response to http://www.jidesoft.com/forum/viewtopic.php?p=22885#22885
     *
     * The addition below allows an optional text field and "Go" button to be displayed on the folderChooser.
     * The user can type a path name into the field, and after hitting <Enter> or pressing the "Go" button,
     * the FolderChooser navigates to the specified folder in the tree (the folder viewer).
     */
 
     /**
      * Bound property for <code>_navigationFieldVisible</code>.
      *
      * @see #setNavigationFieldVisible
      */
     public final static String PROPERTY_NAVIGATION_FIELD_VISIBLE = "navigationFieldVisible";
 
     /**
      * Indicates whether the navigation text field is visible.
      *
      * @see #setNavigationFieldVisible
      * @see #isNavigationFieldVisible
      */
     private boolean _navigationFieldVisible;
 
     /**
      * Sets the navigation text fields visibility.
      *
      * @param navigationFieldVisible if true, the navigation text field is displayed; otherwise it is hidden.
      */
     public void setNavigationFieldVisible(boolean navigationFieldVisible) {
         boolean oldValue = _navigationFieldVisible;
         if (!JideSwingUtilities.equals(oldValue, navigationFieldVisible)) {
             _navigationFieldVisible = navigationFieldVisible;
             firePropertyChange(PROPERTY_NAVIGATION_FIELD_VISIBLE, oldValue, _navigationFieldVisible);
         }
     }
 
     /**
      * Determines whether the navigation text field is visible.
      *
      * @return true if the navigation text field is visible; otherwise false.
      */
     public boolean isNavigationFieldVisible() {
         return _navigationFieldVisible;
     }
 
     /*
     * End of addition.
     *
     * Added on 05/27/2008 in response to http://www.jidesoft.com/forum/viewtopic.php?p=22885#22885
     */
 }
