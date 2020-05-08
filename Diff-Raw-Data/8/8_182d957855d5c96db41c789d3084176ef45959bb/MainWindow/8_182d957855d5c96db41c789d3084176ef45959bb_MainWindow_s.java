 package de.unisiegen.gtitool.ui.logic;
 
 
 import java.io.File;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileFilter;
 
 import de.unisiegen.gtitool.core.entities.Alphabet;
 import de.unisiegen.gtitool.core.exceptions.CoreException.ErrorType;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineValidationException;
 import de.unisiegen.gtitool.core.storage.exceptions.StoreException;
 import de.unisiegen.gtitool.ui.EditorPanel;
 import de.unisiegen.gtitool.ui.Messages;
 import de.unisiegen.gtitool.ui.Version;
 import de.unisiegen.gtitool.ui.model.DefaultMachineModel;
 import de.unisiegen.gtitool.ui.netbeans.MachinesPanelForm;
 import de.unisiegen.gtitool.ui.netbeans.MainWindowForm;
 import de.unisiegen.gtitool.ui.preferences.PreferenceManager;
 import de.unisiegen.gtitool.ui.preferences.listener.LanguageChangedListener;
 import de.unisiegen.gtitool.ui.storage.Storage;
 
 
 /**
  * The main programm window.
  * 
  * @author Benjamin Mies
  * @author Christian Fehler
  * @version $Id$
  */
 public final class MainWindow
 {
 
   /**
    * The number of opened files.
    */
   private static int count = 0;
 
 
   /**
    * The {@link MainWindowForm}.
    */
   private MainWindowForm gui;
 
 
   /**
    * Creates new form <code>MainWindow</code>.
    */
   public MainWindow ()
   {
     this.gui = new MainWindowForm ( this );
     try
     {
       this.gui.setIconImage ( ImageIO.read ( getClass ().getResource (
           "/de/unisiegen/gtitool/ui/icon/gtitool.png" ) ) ); //$NON-NLS-1$
     }
     catch ( Exception e )
     {
       e.printStackTrace ();
     }
     this.gui.setTitle ( "GTI Tool " + Version.VERSION ); //$NON-NLS-1$
     PreferenceManager preferenceManager = PreferenceManager.getInstance ();
     this.gui.setBounds ( preferenceManager.getMainWindowBounds () );
     this.gui.setVisible ( true );
     // Setting the default states
     setGeneralStates ( false );
     // Save
     this.gui.jButtonSave.setEnabled ( false );
     this.gui.jMenuItemSave.setEnabled ( false );
     // Copy
     // Validate
     this.gui.jMenuItemValidate.setEnabled ( false );
     // EnterWord
     this.gui.jMenuItemEnterWord.setEnabled ( false );
     // Preferences
     this.gui.jMenuItemPreferences.setEnabled ( true );
     // RecentlyUsed
     this.gui.jMenuRecentlyUsed.setEnabled ( false );
     
     // toolbar items
     this.gui.jButtonAddState.setEnabled ( false );
     this.gui.jButtonAddTransition.setEnabled( false );
     this.gui.jButtonFinalState.setEnabled ( false );
     this.gui.jButtonMouse.setEnabled ( false );
     this.gui.jButtonStartState.setEnabled ( false );
     
     /*
      * Language changed listener
      */
     PreferenceManager.getInstance ().addLanguageChangedListener (
         new LanguageChangedListener ()
         {
 
           @SuppressWarnings ( "synthetic-access" )
           public void languageChanged ()
           {
             // File
             MainWindow.this.gui.jMenuFile.setText ( Messages
                 .getString ( "MainWindow.File" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuFile.setMnemonic ( Messages.getString (
                 "MainWindow.FileMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // New
             MainWindow.this.gui.jMenuItemNew.setText ( Messages
                 .getString ( "MainWindow.New" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemNew.setMnemonic ( Messages.getString (
                 "MainWindow.NewMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             MainWindow.this.gui.jButtonNew.setToolTipText ( Messages
                 .getString ( "MainWindow.NewToolTip" ) ); //$NON-NLS-1$
             // Open
             MainWindow.this.gui.jMenuItemOpen.setText ( Messages
                 .getString ( "MainWindow.Open" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemOpen.setMnemonic ( Messages.getString (
                 "MainWindow.OpenMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             MainWindow.this.gui.jButtonOpen.setToolTipText ( Messages
                 .getString ( "MainWindow.OpenToolTip" ) ); //$NON-NLS-1$
             // Close
             MainWindow.this.gui.jMenuItemClose.setText ( Messages
                 .getString ( "MainWindow.Close" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemClose.setMnemonic ( Messages
                 .getString ( "MainWindow.CloseMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // Save
             MainWindow.this.gui.jMenuItemSave.setText ( Messages
                 .getString ( "MainWindow.Save" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemSave.setMnemonic ( Messages.getString (
                 "MainWindow.SaveMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             MainWindow.this.gui.jButtonSave.setToolTipText ( Messages
                 .getString ( "MainWindow.SaveToolTip" ) ); //$NON-NLS-1$
             // SaveAs
             MainWindow.this.gui.jMenuItemSaveAs.setText ( Messages
                 .getString ( "MainWindow.SaveAs" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemSaveAs.setMnemonic ( Messages
                 .getString ( "MainWindow.SaveAsMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             MainWindow.this.gui.jButtonSaveAs.setToolTipText ( Messages
                 .getString ( "MainWindow.SaveAsToolTip" ) ); //$NON-NLS-1$
             // SaveAll
             MainWindow.this.gui.jMenuItemSaveAll.setText ( Messages
                 .getString ( "MainWindow.SaveAll" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemSaveAll.setMnemonic ( Messages
                 .getString ( "MainWindow.SaveAllMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // RecentlyUsed
             MainWindow.this.gui.jMenuRecentlyUsed.setText ( Messages
                 .getString ( "MainWindow.RecentlyUsed" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuRecentlyUsed.setMnemonic ( Messages
                 .getString ( "MainWindow.RecentlyUsedMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // Quit
             MainWindow.this.gui.jMenuItemQuit.setText ( Messages
                 .getString ( "MainWindow.Quit" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemQuit.setMnemonic ( Messages.getString (
                 "MainWindow.QuitMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // Edit
             MainWindow.this.gui.jMenuEdit.setText ( Messages
                 .getString ( "MainWindow.Edit" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuEdit.setMnemonic ( Messages.getString (
                 "MainWindow.EditMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // Cut
             MainWindow.this.gui.jMenuItemCut.setText ( Messages
                 .getString ( "MainWindow.Cut" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemCut.setMnemonic ( Messages.getString (
                 "MainWindow.CutMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // Copy
             MainWindow.this.gui.jMenuItemCopy.setText ( Messages
                 .getString ( "MainWindow.Copy" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemCopy.setMnemonic ( Messages.getString (
                 "MainWindow.CopyMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // Paste
             MainWindow.this.gui.jMenuItemPaste.setText ( Messages
                 .getString ( "MainWindow.Paste" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemPaste.setMnemonic ( Messages
                 .getString ( "MainWindow.PasteMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // Undo
             MainWindow.this.gui.jMenuItemUndo.setText ( Messages
                 .getString ( "MainWindow.Undo" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemUndo.setMnemonic ( Messages.getString (
                 "MainWindow.UndoMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // Redo
             MainWindow.this.gui.jMenuItemRedo.setText ( Messages
                 .getString ( "MainWindow.Redo" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemRedo.setMnemonic ( Messages.getString (
                 "MainWindow.RedoMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // Preferences
             MainWindow.this.gui.jMenuItemPreferences.setText ( Messages
                 .getString ( "MainWindow.Preferences" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemPreferences.setMnemonic ( Messages
                 .getString ( "MainWindow.PreferencesMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // Execute
             MainWindow.this.gui.jMenuExecute.setText ( Messages
                 .getString ( "MainWindow.Execute" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuExecute.setMnemonic ( Messages.getString (
                 "MainWindow.ExecuteMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // Validate
             MainWindow.this.gui.jMenuItemValidate.setText ( Messages
                 .getString ( "MainWindow.Validate" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemValidate.setMnemonic ( Messages
                 .getString ( "MainWindow.ValidateMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // EnterWord
             MainWindow.this.gui.jMenuItemEnterWord.setText ( Messages
                 .getString ( "MainWindow.EnterWord" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemEnterWord.setMnemonic ( Messages
                 .getString ( "MainWindow.EnterWordMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // Help
             MainWindow.this.gui.jMenuHelp.setText ( Messages
                 .getString ( "MainWindow.Help" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuHelp.setMnemonic ( Messages.getString (
                 "MainWindow.HelpMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             // About
             MainWindow.this.gui.jMenuItemAbout.setText ( Messages
                 .getString ( "MainWindow.About" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jMenuItemAbout.setMnemonic ( Messages
                 .getString ( "MainWindow.AboutMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
             MainWindow.this.gui.jButtonMouse.setToolTipText ( Messages
                 .getString ( "MachinePanel.Mouse" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jButtonAddState.setToolTipText ( Messages
                 .getString ( "MachinePanel.AddState" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jButtonAddTransition
                 .setToolTipText ( Messages
                     .getString ( "MachinePanel.AddTransition" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jButtonStartState.setToolTipText ( Messages
                 .getString ( "MachinePanel.StartState" ) ); //$NON-NLS-1$
             MainWindow.this.gui.jButtonFinalState.setToolTipText ( Messages
                 .getString ( "MachinePanel.FinalState" ) ); //$NON-NLS-1$
           }
         } );
   }
 
 
   /**
    * Returns the gui.
    * 
    * @return The gui.
    * @see #gui
    */
   public final MainWindowForm getGui ()
   {
     return this.gui;
   }
 
 
   /**
    * Returns the {@link Alphabet} of the selected {@link EditorPanel}.
    * 
    * @return The {@link Alphabet} of the selected {@link EditorPanel}.
    */
   public final Alphabet getSelectedAlphabet ()
   {
     EditorPanel editorPanel = getSelectedEditorPanel ();
     if ( editorPanel == null )
     {
       return null;
     }
     return editorPanel.getAlphabet ();
   }
 
 
   /**
    * Returns the selected {@link EditorPanel}.
    * 
    * @return The selected {@link EditorPanel}.
    */
   public final EditorPanel getSelectedEditorPanel ()
   {
     return ( ( MachinesPanelForm ) this.gui.jTabbedPaneMain
         .getSelectedComponent () ).getLogic ();
   }
 
 
   /**
    * Handle the action event of the about item.
    */
   public final void handleAbout ()
   {
     AboutDialog aboutDialog = new AboutDialog ( this.gui );
     aboutDialog.show ();
   }
 
 
   /**
    * Handle the action event of the enter word item.
    */
   public final void handleEnterWord ()
   {
     // TODO
   }
 
 
   /**
    * Handle the open event.
    */
   public final void handleNew ()
   {
     NewDialog newDialog = new NewDialog ( this.gui );
     // newDialog.setLocationRelativeTo ( window ) ;
     newDialog.show ();
     EditorPanel newEditorPanel = newDialog.getEditorPanel ();
     if ( newEditorPanel != null )
     {
       
       this.gui.jTabbedPaneMain.add ( newEditorPanel.getPanel () );
       this.gui.jTabbedPaneMain.setSelectedComponent ( newEditorPanel
           .getPanel () );
       this.gui.jTabbedPaneMain.setTitleAt ( this.gui.jTabbedPaneMain
           .getSelectedIndex (), "newFile" + count + ".test" ); //$NON-NLS-1$ //$NON-NLS-2$
       count++ ;
       setGeneralStates ( true );
       this.gui.jButtonSave.setEnabled ( true );
       
       
       // toolbar items
       this.gui.jButtonAddState.setEnabled ( true );
       this.gui.jButtonAddTransition.setEnabled( true );
       this.gui.jButtonFinalState.setEnabled ( true );
       this.gui.jButtonMouse.setEnabled ( true );
       this.gui.jButtonStartState.setEnabled ( true );
     }
   }
 
 
   /**
    * Handles the open event.
    */
   public final void handleOpen ()
   {
     PreferenceManager prefmanager = PreferenceManager.getInstance ();
     JFileChooser chooser = new JFileChooser ( prefmanager.getWorkingPath () );
     chooser.setMultiSelectionEnabled ( true );
     chooser.setAcceptAllFileFilterUsed ( false );
     chooser.addChoosableFileFilter ( new FileFilter ( ) {
 
       @Override
       public boolean accept ( File file )
       {
         if ( file.isDirectory ( ) )
         {
           return true ;
         }
           if (file.getName ().matches ( ".+\\.dfa" ) ) //$NON-NLS-1$
             return true;
           return false;
           
       }
 
       @Override
       public String getDescription ()
       {
         return Messages.getString ( "NewDialog.DFA" ) + " (*.dfa)"; //$NON-NLS-1$ //$NON-NLS-2$
       }
       
     });
     int n =chooser.showOpenDialog ( this.gui );
     if ( n == JFileChooser.CANCEL_OPTION || chooser.getSelectedFile ( ) == null )
       return;
     try
     {
      DefaultMachineModel model = (DefaultMachineModel) Storage.getInstance ().load ( chooser.getSelectedFile ( ).toString () );
      EditorPanel newEditorPanel = new MachinePanel ( this.gui, model );
        
        this.gui.jTabbedPaneMain.add ( newEditorPanel.getPanel () );
        this.gui.jTabbedPaneMain.setSelectedComponent ( newEditorPanel
            .getPanel () );
        this.gui.jTabbedPaneMain.setTitleAt ( this.gui.jTabbedPaneMain
            .getSelectedIndex (), chooser.getSelectedFile ( ).getName () ); 
        count++ ;
        setGeneralStates ( true );
        this.gui.jButtonSave.setEnabled ( true );
        
        
        // toolbar items
        this.gui.jButtonAddState.setEnabled ( true );
        this.gui.jButtonAddTransition.setEnabled( true );
        this.gui.jButtonFinalState.setEnabled ( true );
        this.gui.jButtonMouse.setEnabled ( true );
        this.gui.jButtonStartState.setEnabled ( true );
     }
     catch ( StoreException e )
     {
       e.printStackTrace();
       System.exit ( 1 );
     }
     prefmanager.setWorkingPath ( chooser.getCurrentDirectory ()
         .getAbsolutePath () );
   }
 
 
   /**
    * Handle the action event of the preferences item.
    */
   public final void handlePreferences ()
   {
     PreferencesDialog preferencesDialog = new PreferencesDialog ( this.gui );
     preferencesDialog.show ();
   }
 
 
   /**
    * Handles the quit event.
    */
   public final void handleQuit ()
   {
     PreferenceManager preferenceManager = PreferenceManager.getInstance ();
     preferenceManager.setMainWindowPreferences ( this.gui );
     System.exit ( 0 );
   }
 
 
   /**
    * Handle the action event of the enter word item.
    */
   public final void handleValidate ()
   {
     MachinePanel panel =  ( ( MachinesPanelForm ) this.gui.jTabbedPaneMain.getSelectedComponent () ).getLogic ();
    try
   {
      panel.clearValidationMessages ();
      panel.getMachine ().validate ();
   }
   catch ( MachineValidationException e )
   {
     for ( MachineException error : e.getMachineExceptionList () )
     {
       if ( error.getType ().equals ( ErrorType.ERROR ) )
       {
         panel.addError ( error );
       }
       else if ( error.getType ().equals ( ErrorType.WARNING ) )
       {
         panel.addWarning ( error );
       }
     }
   }
   }
 
 
   /**
    * Sets general states for items and buttons.
    * 
    * @param pState The new state.
    */
   private final void setGeneralStates ( boolean pState )
   {
     // SaveAs
     this.gui.jButtonSaveAs.setEnabled ( pState );
     this.gui.jMenuItemSaveAs.setEnabled ( pState );
 
     // SaveAll
     this.gui.jMenuItemSaveAll.setEnabled ( pState );
 
     // Close
     this.gui.jMenuItemClose.setEnabled ( pState );
 
     // Validate
     this.gui.jMenuItemValidate.setEnabled ( pState );
 
     // EnterWord
     this.gui.jMenuItemEnterWord.setEnabled ( pState );
 
     // Cut
     // this.gui.jMenuItemCut.setEnabled ( pState );
     this.gui.jMenuItemCut.setVisible ( false );
 
     // Copy
     // this.gui.jMenuItemCopy.setEnabled ( pState );
     this.gui.jMenuItemCopy.setVisible ( false );
 
     // Paste
     // this.gui.jMenuItemPaste.setEnabled ( pState );
     this.gui.jMenuItemPaste.setVisible ( false );
 
     // Undo
     this.gui.jMenuItemUndo.setVisible ( false );
     setUndoState ( pState );
 
     // Redo
     this.gui.jMenuItemRedo.setVisible ( false );
     setRedoState ( pState );
 
     // Separator
     this.gui.jSeparatorEdit1.setVisible ( false );
     this.gui.jSeparatorEdit2.setVisible ( false );
   }
 
 
   /**
    * Sets the state of the redo button and item.
    * 
    * @param pState The new state for redo.
    */
   private final void setRedoState ( boolean pState )
   {
     this.gui.jMenuItemRedo.setEnabled ( pState );
   }
 
 
   /**
    * Sets the state of the save button and item.
    * 
    * @param pState The new state for save.
    */
   @SuppressWarnings ( "unused" )
   private final void setSaveState ( boolean pState )
   {
     this.gui.jButtonSave.setEnabled ( pState );
     this.gui.jMenuItemSave.setEnabled ( pState );
   }
 
 
   /**
    * Sets the state of the undo button and item.
    * 
    * @param pState The new state for undo.
    */
   private final void setUndoState ( boolean pState )
   {
     this.gui.jMenuItemUndo.setEnabled ( pState );
   }
 
   /**
    * 
    * Handle the save file event
    *
    */
   public void handleSave ()
   {
     MachinePanel panel =  ( ( MachinesPanelForm ) this.gui.jTabbedPaneMain.getSelectedComponent () ).getLogic ();
     panel.handleSave ();
   }
 
   /**
    * Handle Toolbar End button value changed
    * 
    * @param state The new State of the End Toolbar button
    */
   public void handleToolbarEnd ( boolean state )
   {
     MachinePanel panel =  ( ( MachinesPanelForm ) this.gui.jTabbedPaneMain.getSelectedComponent () ).getLogic ();
     panel.handleToolbarEnd ( state );
     
   }
 
   /**
    * Handle Toolbar Start button value changed
    * 
    * @param state The new State of the Start Toolbar button
    */
   public void handleToolbarStart ( boolean state )
   {
     MachinePanel panel =  ( ( MachinesPanelForm ) this.gui.jTabbedPaneMain.getSelectedComponent () ).getLogic ();
     panel.handleToolbarStart ( state );
   }
 
   /**
    * Handle Toolbar Transition button value changed
    * 
    * @param state The new State of the Transition Toolbar button
    */
   public void handleToolbarTransition ( boolean state )
   {
     MachinePanel panel =  ( ( MachinesPanelForm ) this.gui.jTabbedPaneMain.getSelectedComponent () ).getLogic ();
     panel.handleToolbarTransition ( state );
   }
 
   /**
    * Handle Toolbar Add State button value changed
    * 
    * @param state The new State of the Add State Toolbar button
    */
   public void handleToolbarAddState ( boolean state )
   {
     MachinePanel panel =  ( ( MachinesPanelForm ) this.gui.jTabbedPaneMain.getSelectedComponent () ).getLogic ();
     panel.handleToolbarAddState ( state );
   }
 
   /**
    * Handle Toolbar Mouse button value changed
    * 
    * @param state The new State of the Mouse Toolbar button
    */
   public void handleToolbarMouse ( boolean state )
   {
     MachinePanel panel =  ( ( MachinesPanelForm ) this.gui.jTabbedPaneMain.getSelectedComponent () ).getLogic ();
     panel.handleToolbarMouse ( state );
   }
 }
