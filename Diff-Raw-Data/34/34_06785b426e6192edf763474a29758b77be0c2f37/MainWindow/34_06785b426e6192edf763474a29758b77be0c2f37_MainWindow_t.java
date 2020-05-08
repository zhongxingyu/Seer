 package de.unisiegen.gtitool.ui.logic;
 
 
 import java.awt.Component;
 import java.awt.Frame;
 import java.awt.event.ItemEvent;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.TreeSet;
 
 import javax.imageio.ImageIO;
 import javax.swing.JScrollBar;
 import javax.swing.event.ChangeEvent;
 import javax.swing.table.JTableHeader;
 
 import de.unisiegen.gtitool.core.entities.Production;
 import de.unisiegen.gtitool.core.entities.Word;
 import de.unisiegen.gtitool.core.entities.InputEntity.EntityType;
 import de.unisiegen.gtitool.core.entities.listener.ModifyStatusChangedListener;
 import de.unisiegen.gtitool.core.exceptions.RegexException;
 import de.unisiegen.gtitool.core.exceptions.RegexValidationException;
 import de.unisiegen.gtitool.core.exceptions.CoreException.ErrorType;
 import de.unisiegen.gtitool.core.exceptions.alphabet.AlphabetException;
 import de.unisiegen.gtitool.core.exceptions.grammar.GrammarException;
 import de.unisiegen.gtitool.core.exceptions.grammar.GrammarValidationException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineValidationException;
 import de.unisiegen.gtitool.core.exceptions.nonterminalsymbolset.NonterminalSymbolSetException;
 import de.unisiegen.gtitool.core.exceptions.state.StateException;
 import de.unisiegen.gtitool.core.exceptions.terminalsymbolset.TerminalSymbolSetException;
 import de.unisiegen.gtitool.core.exceptions.transition.TransitionException;
 import de.unisiegen.gtitool.core.exceptions.transition.TransitionSymbolOnlyOneTimeException;
 import de.unisiegen.gtitool.core.grammars.Grammar.GrammarType;
 import de.unisiegen.gtitool.core.machines.Machine;
 import de.unisiegen.gtitool.core.machines.Machine.MachineType;
 import de.unisiegen.gtitool.core.preferences.listener.LanguageChangedListener;
 import de.unisiegen.gtitool.core.regex.DefaultRegex.RegexType;
 import de.unisiegen.gtitool.core.storage.Element;
 import de.unisiegen.gtitool.core.storage.exceptions.StoreException;
 import de.unisiegen.gtitool.core.util.ObjectPair;
 import de.unisiegen.gtitool.logger.Logger;
 import de.unisiegen.gtitool.ui.Version;
 import de.unisiegen.gtitool.ui.exchange.Exchange;
 import de.unisiegen.gtitool.ui.i18n.Messages;
 import de.unisiegen.gtitool.ui.jgraph.JGTIGraph;
 import de.unisiegen.gtitool.ui.logic.MachinePanel.MachineMode;
 import de.unisiegen.gtitool.ui.logic.interfaces.EditorPanel;
 import de.unisiegen.gtitool.ui.logic.interfaces.LogicClass;
 import de.unisiegen.gtitool.ui.model.DefaultGrammarModel;
 import de.unisiegen.gtitool.ui.model.DefaultMachineModel;
 import de.unisiegen.gtitool.ui.model.DefaultModel;
 import de.unisiegen.gtitool.ui.model.DefaultRegexModel;
 import de.unisiegen.gtitool.ui.netbeans.MainWindowForm;
 import de.unisiegen.gtitool.ui.popup.TabPopupMenu;
 import de.unisiegen.gtitool.ui.popup.TabPopupMenu.TabPopupMenuType;
 import de.unisiegen.gtitool.ui.preferences.PreferenceManager;
 import de.unisiegen.gtitool.ui.preferences.item.OpenedFilesItem;
 import de.unisiegen.gtitool.ui.preferences.item.RecentlyUsedFilesItem;
 import de.unisiegen.gtitool.ui.storage.Storage;
 import de.unisiegen.gtitool.ui.style.editor.StyledParserEditor;
 import de.unisiegen.gtitool.ui.style.sidebar.SideBar;
 import de.unisiegen.gtitool.ui.swing.JGTITabbedPane;
 import de.unisiegen.gtitool.ui.swing.JGTITable;
 import de.unisiegen.gtitool.ui.swing.JGTITextArea;
 import de.unisiegen.gtitool.ui.swing.specialized.JGTIEditorPanelTabbedPane;
 import de.unisiegen.gtitool.ui.swing.specialized.JGTIMainSplitPane;
 import de.unisiegen.gtitool.ui.swing.specialized.JGTIToolBarToggleButton;
 import de.unisiegen.gtitool.ui.swing.specialized.JGTIMainSplitPane.ActiveEditor;
 import de.unisiegen.gtitool.ui.utils.LayoutManager;
 import de.unisiegen.gtitool.ui.utils.RecentlyUsedMenuItem;
 
 
 /**
  * The main programm window.
  * 
  * @author Benjamin Mies
  * @author Christian Fehler
  * @version $Id$
  */
 public final class MainWindow implements LogicClass < MainWindowForm >,
     LanguageChangedListener
 {
 
   /**
    * The {@link ButtonState} enum.
    * 
    * @author Christian Fehler
    */
   public enum ButtonState
   {
     /**
      * The save as enabled button state.
      */
     ENABLED_SAVE_AS,
 
     /**
      * The save all enabled button state.
      */
     ENABLED_SAVE_ALL,
 
     /**
      * The close enabled button state.
      */
     ENABLED_CLOSE,
 
     /**
      * The close all enabled button state.
      */
     ENABLED_CLOSE_ALL,
 
     /**
      * The print enabled button state.
      */
     ENABLED_PRINT,
 
     /**
      * The undo enabled button state.
      */
     ENABLED_UNDO,
 
     /**
      * The redo enabled button state.
      */
     ENABLED_REDO,
 
     /**
      * The history enabled button state.
      */
     ENABLED_HISTORY,
 
     /**
      * The machine table enabled button state.
      */
     ENABLED_MACHINE_TABLE,
 
     /**
      * The console table enabled button state.
      */
     ENABLED_CONSOLE_TABLE,
 
     /**
      * The validate enabled button state.
      */
     ENABLED_VALIDATE,
 
     /**
      * The draft for enabled button state.
      */
     ENABLED_DRAFT_FOR,
 
     /**
      * The draft for machine enabled button state.
      */
     ENABLED_DRAFT_FOR_MACHINE,
 
     /**
      * The draft for grammar enabled button state.
      */
     ENABLED_DRAFT_FOR_GRAMMAR,
 
     /**
      * The edit document enabled button state.
      */
     ENABLED_EDIT_DOCUMENT,
 
     /**
      * The edit machine enabled button state.
      */
     ENABLED_EDIT_MACHINE,
 
     /**
      * The enter word enabled button state.
      */
     ENABLED_ENTER_WORD,
 
     /**
      * The auto layout enabled button state.
      */
     ENABLED_AUTO_LAYOUT,
 
     /**
      * The recently used enabled button state.
      */
     ENABLED_RECENTLY_USED,
 
     /**
      * The machine edit items enabled button state.
      */
     ENABLED_MACHINE_EDIT_ITEMS,
 
     /**
      * The navigation deactive enabled button state.
      */
     ENABLED_NAVIGATION_DEACTIVE,
 
     /**
      * The navigation start enabled button state.
      */
     ENABLED_NAVIGATION_START,
 
     /**
      * The navigation stop enabled button state.
      */
     ENABLED_NAVIGATION_STOP,
 
     /**
      * The navigation auto step enabled button state.
      */
     ENABLED_NAVIGATION_AUTO_STEP,
 
     /**
      * The navigation steps next enabled button state.
      */
     ENABLED_NAVIGATION_STEPS_NEXT,
 
     /**
      * The navigation steps next enabled button state.
      */
     ENABLED_NAVIGATION_STEPS_PREVIOUS,
 
     /**
      * The navigation steps next enabled button state.
      */
     ENABLED_NAVIGATION_STEPS_NEXT_PREVIOUS,
 
     /**
      * The convert to enabled button state.
      */
     ENABLED_CONVERT_TO,
 
     /**
      * The convert to source dfa enabled button state.
      */
     ENABLED_CONVERT_TO_SOURCE_DFA,
 
     /**
      * The convert to source nfa enabled button state.
      */
     ENABLED_CONVERT_TO_SOURCE_NFA,
 
     /**
      * The convert to source enfa enabled button state.
      */
     ENABLED_CONVERT_TO_SOURCE_ENFA,
 
     /**
      * The convert to source pda enabled button state.
      */
     ENABLED_CONVERT_TO_SOURCE_PDA,
 
     /**
      * The convert to source rg enabled button state.
      */
     ENABLED_CONVERT_TO_SOURCE_RG,
 
     /**
      * The convert to source cfg enabled button state.
      */
     ENABLED_CONVERT_TO_SOURCE_CFG,
 
     /**
      * The convert to source regex enabled button state.
      */
     ENABLED_CONVERT_TO_SOURCE_REGEX,
 
     /**
      * The convert to complete enabled button state.
      */
     ENABLED_CONVERT_TO_COMPLETE,
 
     /**
      * The convert to complete source dfa enabled button state.
      */
     ENABLED_CONVERT_TO_COMPLETE_SOURCE_DFA,
 
     /**
      * The convert to complete source nfa enabled button state.
      */
     ENABLED_CONVERT_TO_COMPLETE_SOURCE_NFA,
 
     /**
      * The convert to complete source enfa enabled button state.
      */
     ENABLED_CONVERT_TO_COMPLETE_SOURCE_ENFA,
 
     /**
      * The convert to complete source pda enabled button state.
      */
     ENABLED_CONVERT_TO_COMPLETE_SOURCE_PDA,
 
     /**
      * The convert to complete source rg enabled button state.
      */
     ENABLED_CONVERT_TO_COMPLETE_SOURCE_RG,
 
     /**
      * The convert to complete source cfg enabled button state.
      */
     ENABLED_CONVERT_TO_COMPLETE_SOURCE_CFG,
 
     /**
      * The convert to complete source regex enabled button state.
      */
     ENABLED_CONVERT_TO_COMPLETE_SOURCE_REGEX,
 
     /**
      * The minimize enabled button state.
      */
     ENABLED_MINIMIZE,
 
     /**
      * The convert dfa to regex button state.
      */
     ENABLED_CONVERT_DFA_TO_REGEX,
 
     /**
      * The reachable states enabled button state.
      */
     ENABLED_REACHABLE_STATES,
 
     /**
      * The export picture enabled button state.
      */
     ENABLED_EXPORT_PICTURE,
 
     /**
      * The reorder state names enabled button state.
      */
     ENABLED_REORDER_STATE_NAMES,
 
     /**
      * The save enabled button state.
      */
     ENABLED_SAVE,
 
     /**
      * The toLatexExport button state.
      */
     ENABLED_TO_LATEX,
 
     /**
      * The createRDP button state
      */
     ENABLED_CREATE_RDP,
 
     /**
      * The toCoreSyntax button state.
      */
     ENABLED_TO_CORE_SYNTAX,
 
     /**
      * The eliminate left recursion button state
      */
     ENABLED_ELIMINATE_LEFT_RECURSION,
 
     /**
      * The eliminate entity productions button state
      */
     ENABLED_ELIMINATE_ENTITY_PRODUCTIONS,
 
     /**
      * The eliminate epsilon productions button state
      */
     ENABLED_ELIMINATE_EPSILON_PRODUCTIONS,
 
     /**
      * The left factoring button state
      */
     ENABLED_LEFT_FACTORING,
 
     /**
      * The regex info button state
      */
     ENABLED_REGEX_INFO,
 
     /**
      * The machine table selected button state.
      */
     SELECTED_MACHINE_TABLE,
 
     /**
      * The console table selected button state.
      */
     SELECTED_CONSOLE_TABLE,
 
     /**
      * The mouse selected button state.
      */
     SELECTED_MOUSE,
 
     /**
      * The auto step selected button state.
      */
     SELECTED_AUTO_STEP,
 
     /**
      * The enter word selected button state.
      */
     SELECTED_ENTER_WORD,
 
     /**
      * The machine visible button state.
      */
     VISIBLE_MACHINE,
 
     /**
      * The grammar visible button state.
      */
     VISIBLE_GRAMMAR,
 
     /**
      * The regex visible button state.
      */
     VISIBLE_REGEX;
   }
 
 
   /**
    * The close state enum.
    * 
    * @author Christian Fehler
    */
   private enum CloseState
   {
     /**
      * The confirmed value.
      */
     CONFIRMED,
 
     /**
      * The confirmed all value.
      */
     CONFIRMED_ALL,
 
     /**
      * The not confirmed value.
      */
     NOT_CONFIRMED,
 
     /**
      * The not confirmed all value.
      */
     NOT_CONFIRMED_ALL,
 
     /**
      * The canceled value.
      */
     CANCELED;
   }
 
 
   /**
    * The {@link Logger} for this class.
    */
   private static final Logger logger = Logger.getLogger ( MainWindow.class );
 
 
   /**
    * The {@link MainWindowForm}.
    */
   private MainWindowForm gui;
 
 
   /**
    * The {@link ModifyStatusChangedListener}.
    */
   private ModifyStatusChangedListener modifyStatusChangedListener;
 
 
   /**
    * List contains the recently used files
    */
   private ArrayList < RecentlyUsedMenuItem > recentlyUsedFiles = new ArrayList < RecentlyUsedMenuItem > ();
 
 
   /**
    * The {@link ButtonState} list.
    */
   private ArrayList < ButtonState > buttonStateList = new ArrayList < ButtonState > ();
 
 
   /**
    * The {@link JGTIMainSplitPane}.
    */
   private JGTIMainSplitPane jGTIMainSplitPane;
 
 
   /**
    * Creates new form {@link MainWindow}.
    */
   public MainWindow ()
   {
     this.gui = new MainWindowForm ( this );
 
     // second view
     this.jGTIMainSplitPane = this.gui.getJGTIMainSplitPane ();
     this.jGTIMainSplitPane.setMainWindowForm ( this.gui );
 
     try
     {
       this.gui.setIconImage ( ImageIO.read ( getClass ().getResource (
           "/de/unisiegen/gtitool/ui/icon/large/gtitool.png" ) ) ); //$NON-NLS-1$
     }
     catch ( Exception exc )
     {
       exc.printStackTrace ();
     }
     this.gui.setTitle ( "GTI Tool " + Version.VERSION ); //$NON-NLS-1$
     this.gui.setBounds ( PreferenceManager.getInstance ()
         .getMainWindowBounds () );
 
     removeButtonState ( ButtonState.ENABLED_SAVE_AS );
     removeButtonState ( ButtonState.ENABLED_SAVE_ALL );
     removeButtonState ( ButtonState.ENABLED_CLOSE );
     removeButtonState ( ButtonState.ENABLED_CLOSE_ALL );
     removeButtonState ( ButtonState.ENABLED_PRINT );
     removeButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
     removeButtonState ( ButtonState.ENABLED_CONSOLE_TABLE );
     removeButtonState ( ButtonState.ENABLED_MACHINE_TABLE );
     removeButtonState ( ButtonState.ENABLED_SAVE );
     removeButtonState ( ButtonState.ENABLED_HISTORY );
     removeButtonState ( ButtonState.ENABLED_CONVERT_TO );
     removeButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE );
     removeButtonState ( ButtonState.ENABLED_VALIDATE );
     removeButtonState ( ButtonState.ENABLED_ENTER_WORD );
     removeButtonState ( ButtonState.ENABLED_EDIT_MACHINE );
     removeButtonState ( ButtonState.ENABLED_UNDO );
     removeButtonState ( ButtonState.ENABLED_REDO );
     removeButtonState ( ButtonState.ENABLED_AUTO_LAYOUT );
     removeButtonState ( ButtonState.ENABLED_RECENTLY_USED );
     removeButtonState ( ButtonState.ENABLED_MINIMIZE );
     removeButtonState ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX );
     removeButtonState ( ButtonState.ENABLED_REACHABLE_STATES );
     removeButtonState ( ButtonState.ENABLED_EXPORT_PICTURE );
     removeButtonState ( ButtonState.ENABLED_REORDER_STATE_NAMES );
     removeButtonState ( ButtonState.ENABLED_DRAFT_FOR );
     removeButtonState ( ButtonState.SELECTED_ENTER_WORD );
     removeButtonState ( ButtonState.VISIBLE_MACHINE );
     removeButtonState ( ButtonState.VISIBLE_GRAMMAR );
     removeButtonState ( ButtonState.VISIBLE_REGEX );
     removeButtonState ( ButtonState.ENABLED_REGEX_INFO );
     removeButtonState ( ButtonState.ENABLED_TO_LATEX );
     removeButtonState ( ButtonState.ENABLED_TO_CORE_SYNTAX );
     removeButtonState ( ButtonState.ENABLED_ELIMINATE_LEFT_RECURSION );
     removeButtonState ( ButtonState.ENABLED_ELIMINATE_ENTITY_PRODUCTIONS );
     removeButtonState ( ButtonState.ENABLED_ELIMINATE_EPSILON_PRODUCTIONS );
     removeButtonState ( ButtonState.ENABLED_LEFT_FACTORING );
     removeButtonState ( ButtonState.ENABLED_CREATE_RDP );
 
     // Console and table visibility
     this.gui.getJCheckBoxMenuItemConsole ().setSelected (
         PreferenceManager.getInstance ().getVisibleConsole () );
     if ( PreferenceManager.getInstance ().getVisibleTable () )
     {
       addButtonState ( ButtonState.SELECTED_MACHINE_TABLE );
     }
     else
     {
       removeButtonState ( ButtonState.SELECTED_MACHINE_TABLE );
     }
 
     this.gui.setVisible ( true );
     if ( PreferenceManager.getInstance ().getMainWindowMaximized () )
     {
       this.gui.setExtendedState ( this.gui.getExtendedState ()
           | Frame.MAXIMIZED_BOTH );
     }
     // Language changed listener
     PreferenceManager.getInstance ().addLanguageChangedListener ( this );
 
     for ( File file : PreferenceManager.getInstance ()
         .getRecentlyUsedFilesItem ().getFiles () )
     {
       this.recentlyUsedFiles.add ( new RecentlyUsedMenuItem ( this, file ) );
     }
     organizeRecentlyUsedFilesMenu ();
 
     this.modifyStatusChangedListener = new ModifyStatusChangedListener ()
     {
 
       public void modifyStatusChanged ( boolean modified )
       {
         if ( modified )
         {
           addButtonState ( ButtonState.ENABLED_SAVE );
         }
         else
         {
           removeButtonState ( ButtonState.ENABLED_SAVE );
         }
       }
     };
 
     // second view
     boolean secondViewUsed = PreferenceManager.getInstance ()
         .getSeconsViewUsed ();
     this.gui.getJGTIMainSplitPane ().setDividerLocation ( 0.5 );
     if ( this.gui.getJCheckBoxMenuItemSecondView ().isSelected () == secondViewUsed )
     {
       this.gui.getJCheckBoxMenuItemSecondView ().setSelected ( secondViewUsed );
       handleSecondViewStateChanged ();
     }
     else
     {
       this.gui.getJCheckBoxMenuItemSecondView ().setSelected ( secondViewUsed );
     }
   }
 
 
   /**
    * Adds the given {@link ButtonState}.
    * 
    * @param buttonState The {@link ButtonState} to add.
    */
   public final void addButtonState ( ButtonState buttonState )
   {
     // enabled
     if ( ( buttonState.equals ( ButtonState.ENABLED_SAVE_AS ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_SAVE_AS ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_SAVE_AS );
       this.gui.getJGTIToolBarButtonSaveAs ().setEnabled ( true );
       this.gui.getJMenuItemSaveAs ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_SAVE_ALL ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_SAVE_ALL ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_SAVE_ALL );
       this.gui.getJMenuItemSaveAll ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_CLOSE ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_CLOSE ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_CLOSE );
       this.gui.getJMenuItemClose ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_CLOSE_ALL ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_CLOSE_ALL ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_CLOSE_ALL );
       this.gui.getJMenuItemCloseAll ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_PRINT ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_PRINT ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_PRINT );
       this.gui.getJMenuItemPrint ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_UNDO ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_UNDO ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_UNDO );
       this.gui.getJMenuItemUndo ().setEnabled ( true );
       this.gui.getJGTIToolBarButtonUndo ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_REDO ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_REDO ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_REDO );
       this.gui.getJMenuItemRedo ().setEnabled ( true );
       this.gui.getJGTIToolBarButtonRedo ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_HISTORY ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_HISTORY ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_HISTORY );
       this.gui.getJMenuItemHistory ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_MACHINE_TABLE ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_MACHINE_TABLE ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_MACHINE_TABLE );
       this.gui.getJCheckBoxMenuItemTable ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_CONSOLE_TABLE ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONSOLE_TABLE ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_CONSOLE_TABLE );
       this.gui.getJCheckBoxMenuItemConsole ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_VALIDATE ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_VALIDATE ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_VALIDATE );
       this.gui.getJMenuItemValidate ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_DRAFT_FOR ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_DRAFT_FOR ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_DRAFT_FOR );
       this.gui.getJMenuDraft ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_DRAFT_FOR_MACHINE ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_DRAFT_FOR_MACHINE ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_DRAFT_FOR_MACHINE );
       this.buttonStateList.remove ( ButtonState.ENABLED_DRAFT_FOR_GRAMMAR );
       this.gui.getJMenuItemDFA ().setEnabled ( true );
       this.gui.getJMenuItemNFA ().setEnabled ( true );
       this.gui.getJMenuItemENFA ().setEnabled ( true );
       this.gui.getJMenuItemPDA ().setEnabled ( true );
       this.gui.getJMenuItemRG ().setEnabled ( false );
       this.gui.getJMenuItemCFG ().setEnabled ( false );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_DRAFT_FOR_GRAMMAR ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_DRAFT_FOR_GRAMMAR ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_DRAFT_FOR_GRAMMAR );
       this.buttonStateList.remove ( ButtonState.ENABLED_DRAFT_FOR_MACHINE );
       this.gui.getJMenuItemDFA ().setEnabled ( false );
       this.gui.getJMenuItemNFA ().setEnabled ( false );
       this.gui.getJMenuItemENFA ().setEnabled ( false );
       this.gui.getJMenuItemPDA ().setEnabled ( false );
       this.gui.getJMenuItemRG ().setEnabled ( true );
       this.gui.getJMenuItemCFG ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_EDIT_DOCUMENT ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_EDIT_DOCUMENT ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_EDIT_DOCUMENT );
       this.gui.getJGTIToolBarButtonEditDocument ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_EDIT_MACHINE ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_EDIT_MACHINE ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_EDIT_MACHINE );
       this.gui.getJMenuItemEditMachine ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_ENTER_WORD ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_ENTER_WORD ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_ENTER_WORD );
       this.gui.getJMenuItemEnterWord ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_AUTO_LAYOUT ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_AUTO_LAYOUT ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_AUTO_LAYOUT );
       this.gui.getJMenuItemAutoLayout ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_RECENTLY_USED ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_RECENTLY_USED ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_RECENTLY_USED );
       this.gui.getJMenuRecentlyUsed ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_MACHINE_EDIT_ITEMS ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_MACHINE_EDIT_ITEMS ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_MACHINE_EDIT_ITEMS );
       this.gui.getJGTIToolBarToggleButtonAddState ().setEnabled ( true );
       this.gui.getJGTIToolBarToggleButtonAddTransition ().setEnabled ( true );
       this.gui.getJGTIToolBarToggleButtonFinalState ().setEnabled ( true );
       this.gui.getJGTIToolBarToggleButtonMouse ().setEnabled ( true );
       this.gui.getJGTIToolBarToggleButtonStartState ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_NAVIGATION_DEACTIVE ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_NAVIGATION_DEACTIVE ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_NAVIGATION_DEACTIVE );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_AUTO_STEP );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_START );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_STOP );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_NAVIGATION_STEPS_PREVIOUS );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT_PREVIOUS );
       this.gui.getJGTIToolBarButtonStart ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonPreviousStep ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonNextStep ().setEnabled ( false );
       this.gui.getJGTIToolBarToggleButtonAutoStep ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonStop ().setEnabled ( false );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_NAVIGATION_AUTO_STEP ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_NAVIGATION_AUTO_STEP ) ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_DEACTIVE );
       this.buttonStateList.add ( ButtonState.ENABLED_NAVIGATION_AUTO_STEP );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_START );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_STOP );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_NAVIGATION_STEPS_PREVIOUS );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT_PREVIOUS );
       this.gui.getJGTIToolBarButtonStart ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonPreviousStep ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonNextStep ().setEnabled ( false );
       this.gui.getJGTIToolBarToggleButtonAutoStep ().setEnabled ( true );
       this.gui.getJGTIToolBarButtonStop ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_NAVIGATION_START ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_NAVIGATION_START ) ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_DEACTIVE );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_AUTO_STEP );
       this.buttonStateList.add ( ButtonState.ENABLED_NAVIGATION_START );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_STOP );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_NAVIGATION_STEPS_PREVIOUS );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT_PREVIOUS );
       this.gui.getJGTIToolBarButtonStart ().setEnabled ( true );
       this.gui.getJGTIToolBarButtonPreviousStep ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonNextStep ().setEnabled ( false );
       this.gui.getJGTIToolBarToggleButtonAutoStep ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonStop ().setEnabled ( false );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_NAVIGATION_STOP ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_NAVIGATION_STOP ) ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_DEACTIVE );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_AUTO_STEP );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_START );
       this.buttonStateList.add ( ButtonState.ENABLED_NAVIGATION_STOP );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_NAVIGATION_STEPS_PREVIOUS );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT_PREVIOUS );
       this.gui.getJGTIToolBarButtonStart ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonPreviousStep ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonNextStep ().setEnabled ( false );
       this.gui.getJGTIToolBarToggleButtonAutoStep ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonStop ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT ) ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_DEACTIVE );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_AUTO_STEP );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_START );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_STOP );
       this.buttonStateList.add ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_NAVIGATION_STEPS_PREVIOUS );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT_PREVIOUS );
       this.gui.getJGTIToolBarButtonStart ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonPreviousStep ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonNextStep ().setEnabled ( true );
       this.gui.getJGTIToolBarToggleButtonAutoStep ().setEnabled ( true );
       this.gui.getJGTIToolBarButtonStop ().setEnabled ( true );
     }
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_NAVIGATION_STEPS_PREVIOUS ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_NAVIGATION_STEPS_PREVIOUS ) ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_DEACTIVE );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_AUTO_STEP );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_START );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_STOP );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT );
       this.buttonStateList.add ( ButtonState.ENABLED_NAVIGATION_STEPS_PREVIOUS );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT_PREVIOUS );
       this.gui.getJGTIToolBarButtonStart ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonPreviousStep ().setEnabled ( true );
       this.gui.getJGTIToolBarButtonNextStep ().setEnabled ( false );
       this.gui.getJGTIToolBarToggleButtonAutoStep ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonStop ().setEnabled ( true );
     }
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT_PREVIOUS ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT_PREVIOUS ) ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_DEACTIVE );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_AUTO_STEP );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_START );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_STOP );
       this.buttonStateList.remove ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_NAVIGATION_STEPS_PREVIOUS );
       this.buttonStateList
           .add ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT_PREVIOUS );
       this.gui.getJGTIToolBarButtonStart ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonPreviousStep ().setEnabled ( true );
       this.gui.getJGTIToolBarButtonNextStep ().setEnabled ( true );
       this.gui.getJGTIToolBarToggleButtonAutoStep ().setEnabled ( true );
       this.gui.getJGTIToolBarButtonStop ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_CONVERT_TO ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_CONVERT_TO ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_CONVERT_TO );
       this.gui.getJMenuConvertTo ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_CONVERT_TO_SOURCE_DFA ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_SOURCE_DFA ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_CONVERT_TO_SOURCE_DFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_NFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_ENFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_PDA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_RG );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_CFG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToDFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToNFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToNFACB ().setEnabled ( false );
       this.gui.getJMenuItemConvertToENFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToPDA ().setEnabled ( false );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_CONVERT_TO_SOURCE_NFA ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_SOURCE_NFA ) ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_DFA );
       this.buttonStateList.add ( ButtonState.ENABLED_CONVERT_TO_SOURCE_NFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_ENFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_PDA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_RG );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_CFG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToDFA ().setEnabled ( true );
       this.gui.getJMenuItemConvertToNFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToNFACB ().setEnabled ( false );
       this.gui.getJMenuItemConvertToENFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToPDA ().setEnabled ( false );
     }
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_CONVERT_TO_SOURCE_ENFA ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_SOURCE_ENFA ) ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_DFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_NFA );
       this.buttonStateList.add ( ButtonState.ENABLED_CONVERT_TO_SOURCE_ENFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_PDA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_RG );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_CFG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToDFA ().setEnabled ( true );
       this.gui.getJMenuItemConvertToNFA ().setEnabled ( true );
       this.gui.getJMenuItemConvertToNFACB ().setEnabled ( true );
       this.gui.getJMenuItemConvertToENFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToPDA ().setEnabled ( false );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_CONVERT_TO_SOURCE_PDA ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_SOURCE_PDA ) ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_DFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_NFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_ENFA );
       this.buttonStateList.add ( ButtonState.ENABLED_CONVERT_TO_SOURCE_PDA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_RG );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_CFG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToDFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToNFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToNFACB ().setEnabled ( false );
       this.gui.getJMenuItemConvertToENFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToPDA ().setEnabled ( false );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_CONVERT_TO_SOURCE_RG ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_SOURCE_RG ) ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_DFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_NFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_ENFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_PDA );
       this.buttonStateList.add ( ButtonState.ENABLED_CONVERT_TO_SOURCE_RG );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_CFG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToDFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToNFA ().setEnabled ( true );
       this.gui.getJMenuItemConvertToNFACB ().setEnabled ( false );
       this.gui.getJMenuItemConvertToENFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToPDA ().setEnabled ( false );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_CONVERT_TO_SOURCE_CFG ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_SOURCE_CFG ) ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_DFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_NFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_ENFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_PDA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_RG );
       this.buttonStateList.add ( ButtonState.ENABLED_CONVERT_TO_SOURCE_CFG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToDFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToNFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToNFACB ().setEnabled ( false );
       this.gui.getJMenuItemConvertToENFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToPDA ().setEnabled ( true );
     }
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_CONVERT_TO_SOURCE_REGEX ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_SOURCE_REGEX ) ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_DFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_NFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_ENFA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_PDA );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_RG );
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_SOURCE_CFG );
       this.buttonStateList.add ( ButtonState.ENABLED_CONVERT_TO_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToDFA ().setEnabled ( true );
       this.gui.getJMenuItemConvertToNFA ().setEnabled ( true );
       this.gui.getJMenuItemConvertToNFACB ().setEnabled ( false );
       this.gui.getJMenuItemConvertToENFA ().setEnabled ( true );
       this.gui.getJMenuItemConvertToPDA ().setEnabled ( false );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_CONVERT_TO_COMPLETE ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_COMPLETE ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_CONVERT_TO_COMPLETE );
       this.gui.getJMenuConvertToComplete ().setEnabled ( true );
     }
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_DFA ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_DFA ) ) )
     {
       this.buttonStateList
           .add ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_DFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_NFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_ENFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_PDA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_RG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_CFG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToCompleteDFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompleteNFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompleteENFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompletePDA ().setEnabled ( false );
     }
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_NFA ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_NFA ) ) )
     {
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_DFA );
       this.buttonStateList
           .add ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_NFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_ENFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_PDA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_RG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_CFG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToCompleteDFA ().setEnabled ( true );
       this.gui.getJMenuItemConvertToCompleteNFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompleteENFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompletePDA ().setEnabled ( false );
     }
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_ENFA ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_ENFA ) ) )
     {
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_DFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_NFA );
       this.buttonStateList
           .add ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_ENFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_PDA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_RG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_CFG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToCompleteDFA ().setEnabled ( true );
       this.gui.getJMenuItemConvertToCompleteNFA ().setEnabled ( true );
       this.gui.getJMenuItemConvertToCompleteENFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompletePDA ().setEnabled ( false );
     }
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_PDA ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_PDA ) ) )
     {
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_DFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_NFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_ENFA );
       this.buttonStateList
           .add ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_PDA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_RG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_CFG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToCompleteDFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompleteNFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompleteENFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompletePDA ().setEnabled ( false );
     }
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_RG ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_RG ) ) )
     {
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_DFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_NFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_ENFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_PDA );
       this.buttonStateList
           .add ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_RG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_CFG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToCompleteDFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompleteNFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompleteENFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompletePDA ().setEnabled ( false );
     }
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_CFG ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_CFG ) ) )
     {
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_DFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_NFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_ENFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_PDA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_RG );
       this.buttonStateList
           .add ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_CFG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToCompleteDFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompleteNFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompleteENFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompletePDA ().setEnabled ( false );
     }
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_REGEX ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_REGEX ) ) )
     {
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_DFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_NFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_ENFA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_PDA );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_RG );
       this.buttonStateList
           .remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_CFG );
       this.buttonStateList
           .add ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_REGEX );
       this.gui.getJMenuItemConvertToCompleteDFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompleteNFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompleteENFA ().setEnabled ( false );
       this.gui.getJMenuItemConvertToCompletePDA ().setEnabled ( false );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_MINIMIZE ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_MINIMIZE ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_MINIMIZE );
       this.gui.getJMenuItemMinimize ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX );
       this.gui.getJMenuItemConvertToRegex ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_REACHABLE_STATES ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_REACHABLE_STATES ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_REACHABLE_STATES );
       this.gui.getJMenuItemReachableStates ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_EXPORT_PICTURE ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_EXPORT_PICTURE ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_EXPORT_PICTURE );
       this.gui.getJMenuItemExportPicture ().setEnabled ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.ENABLED_REORDER_STATE_NAMES ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_REORDER_STATE_NAMES ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_REORDER_STATE_NAMES );
       this.gui.getJMenuItemReorderStateNames ().setEnabled ( true );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_SAVE ) )
     {
       if ( !this.buttonStateList.contains ( ButtonState.ENABLED_SAVE ) )
       {
         this.buttonStateList.add ( ButtonState.ENABLED_SAVE );
       }
       logger.debug ( "setSaveState", "set save status to " + Messages.QUOTE //$NON-NLS-1$//$NON-NLS-2$
           + true + Messages.QUOTE );
       EditorPanel panel = this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ();
       if ( panel != null )
       {
         this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
             .setEditorPanelTitle ( panel, "*" //$NON-NLS-1$
                 + panel.getName () );
       }
       else
       {
         throw new IllegalArgumentException (
             "the save status should be false if no panel is selected" ); //$NON-NLS-1$
       }
       this.gui.getJGTIToolBarButtonSave ().setEnabled ( true );
       this.gui.getJMenuItemSave ().setEnabled ( true );
     }
     // to latex
     else if ( ( buttonState.equals ( ButtonState.ENABLED_TO_LATEX ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_TO_LATEX ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_TO_LATEX );
       this.gui.getJMenuItemExportLatex ().setEnabled ( true );
     }
     // to core syntax
     else if ( ( buttonState.equals ( ButtonState.ENABLED_TO_CORE_SYNTAX ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_TO_CORE_SYNTAX ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_TO_CORE_SYNTAX );
       this.gui.getJMenuItemToCoreSyntax ().setEnabled ( true );
     }
     // eliminate left recursion
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_ELIMINATE_LEFT_RECURSION ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_ELIMINATE_LEFT_RECURSION ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_ELIMINATE_LEFT_RECURSION );
       this.gui.getJMenuItemEliminateLeftRecursion ().setEnabled ( true );
     }
     // eliminate entity productions
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_ELIMINATE_ENTITY_PRODUCTIONS ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_ELIMINATE_ENTITY_PRODUCTIONS ) ) )
     {
       this.buttonStateList
           .add ( ButtonState.ENABLED_ELIMINATE_ENTITY_PRODUCTIONS );
       this.gui.getJMenuItemEliminateEntityProductions ().setEnabled ( true );
     }
     // eliminate epsilon productions
     else if ( ( buttonState
         .equals ( ButtonState.ENABLED_ELIMINATE_EPSILON_PRODUCTIONS ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_ELIMINATE_EPSILON_PRODUCTIONS ) ) )
     {
       this.buttonStateList
           .add ( ButtonState.ENABLED_ELIMINATE_EPSILON_PRODUCTIONS );
       this.gui.getJMenuItemEliminateEpsilonProductions ().setEnabled ( true );
     }
     // left factoring
     else if ( ( buttonState.equals ( ButtonState.ENABLED_LEFT_FACTORING ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.ENABLED_LEFT_FACTORING ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_LEFT_FACTORING );
       this.gui.getJMenuItemLeftfactoring ().setEnabled ( true );
     }
     // create rdp
     else if ( ( buttonState.equals ( ButtonState.ENABLED_CREATE_RDP ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_CREATE_RDP ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_CREATE_RDP );
       this.gui.getJMenuItemCreateRDP ().setEnabled ( true );
     }
     // regex info view
     else if ( ( buttonState.equals ( ButtonState.ENABLED_REGEX_INFO ) )
         && ( !this.buttonStateList.contains ( ButtonState.ENABLED_REGEX_INFO ) ) )
     {
       this.buttonStateList.add ( ButtonState.ENABLED_REGEX_INFO );
       this.gui.getJCheckBoxMenuItemRegexInfo ().setEnabled ( true );
     }
     // selected
     else if ( ( buttonState.equals ( ButtonState.SELECTED_MACHINE_TABLE ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.SELECTED_MACHINE_TABLE ) ) )
     {
       this.buttonStateList.add ( ButtonState.SELECTED_MACHINE_TABLE );
       this.gui.getJCheckBoxMenuItemTable ().setSelected ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.SELECTED_CONSOLE_TABLE ) )
         && ( !this.buttonStateList
             .contains ( ButtonState.SELECTED_CONSOLE_TABLE ) ) )
     {
       this.buttonStateList.add ( ButtonState.SELECTED_CONSOLE_TABLE );
       this.gui.getJCheckBoxMenuItemConsole ().setSelected ( true );
     }
     else if ( buttonState.equals ( ButtonState.SELECTED_MOUSE ) )
     {
       if ( !this.buttonStateList.contains ( ButtonState.SELECTED_MOUSE ) )
       {
         this.buttonStateList.add ( ButtonState.SELECTED_MOUSE );
       }
       this.gui.getJGTIToolBarToggleButtonMouse ().setSelected ( true );
     }
     else if ( buttonState.equals ( ButtonState.SELECTED_AUTO_STEP ) )
     {
       if ( !this.buttonStateList.contains ( ButtonState.SELECTED_AUTO_STEP ) )
       {
         this.buttonStateList.add ( ButtonState.SELECTED_AUTO_STEP );
       }
       this.gui.getJGTIToolBarToggleButtonAutoStep ().setSelected ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.SELECTED_ENTER_WORD ) )
         && ( !this.buttonStateList.contains ( ButtonState.SELECTED_ENTER_WORD ) ) )
     {
       this.buttonStateList.add ( ButtonState.SELECTED_ENTER_WORD );
       this.gui.getJGTIToolBarToggleButtonEnterWord ().setSelected ( true );
     }
     // visible
     else if ( ( buttonState.equals ( ButtonState.VISIBLE_MACHINE ) )
         && ( !this.buttonStateList.contains ( ButtonState.VISIBLE_MACHINE ) ) )
     {
       this.buttonStateList.add ( ButtonState.VISIBLE_MACHINE );
       this.gui.getJSeparatorNavigation ().setVisible ( true );
       this.gui.getJGTIToolBarToggleButtonMouse ().setVisible ( true );
       this.gui.getJGTIToolBarToggleButtonAddState ().setVisible ( true );
       this.gui.getJGTIToolBarToggleButtonStartState ().setVisible ( true );
       this.gui.getJGTIToolBarToggleButtonFinalState ().setVisible ( true );
       this.gui.getJGTIToolBarToggleButtonEnterWord ().setVisible ( true );
       this.gui.getJGTIToolBarToggleButtonAddTransition ().setVisible ( true );
       this.gui.getJGTIToolBarButtonStart ().setVisible ( true );
       this.gui.getJGTIToolBarButtonPreviousStep ().setVisible ( true );
       this.gui.getJGTIToolBarButtonNextStep ().setVisible ( true );
       this.gui.getJGTIToolBarToggleButtonAutoStep ().setVisible ( true );
       this.gui.getJGTIToolBarButtonStop ().setVisible ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.VISIBLE_GRAMMAR ) )
         && ( !this.buttonStateList.contains ( ButtonState.VISIBLE_GRAMMAR ) ) )
     {
       this.buttonStateList.add ( ButtonState.VISIBLE_GRAMMAR );
       this.gui.getJGTIToolBarButtonAddProduction ().setVisible ( true );
     }
     else if ( ( buttonState.equals ( ButtonState.VISIBLE_REGEX ) )
         && ( !this.buttonStateList.contains ( ButtonState.VISIBLE_REGEX ) ) )
     {
       this.buttonStateList.add ( ButtonState.VISIBLE_REGEX );
     }
   }
 
 
   /**
    * Handle auto layout action performed.
    */
   public final void doAutoLayout ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel instanceof MachinePanel )
     {
       MachinePanel machinePanel = ( MachinePanel ) panel;
       new LayoutManager ( machinePanel.getModel (), machinePanel
           .getRedoUndoHandler () ).doLayout ();
     }
     else
     {
       throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see LogicClass#getGUI()
    */
   public final MainWindowForm getGUI ()
   {
     return this.gui;
   }
 
 
   /**
    * Getter for the {@link ModifyStatusChangedListener}.
    * 
    * @return The {@link ModifyStatusChangedListener}.
    */
   public final ModifyStatusChangedListener getModifyStatusChangedListener ()
   {
     return this.modifyStatusChangedListener;
   }
 
 
   /**
    * Handles the about event.
    */
   public final void handleAbout ()
   {
     AboutDialog aboutDialog = new AboutDialog ( this.gui );
     aboutDialog.show ();
   }
 
 
   /**
    * Handles the add {@link Production} event.
    */
   public final void handleAddProduction ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel instanceof GrammarPanel )
     {
       GrammarPanel grammarPanel = ( GrammarPanel ) panel;
       grammarPanel.handleAddProduction ();
     }
     else
     {
       throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * Closes the selected {@link EditorPanel}.
    * 
    * @return The {@link CloseState}.
    */
   public final CloseState handleClose ()
   {
     if ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () == null )
     {
       throw new RuntimeException ( "no selected editor panel" ); //$NON-NLS-1$
     }
 
     return handleClose ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel (), this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ()
         .isModified () ? 1 : 0, false, false );
   }
 
 
   /**
    * Closes the {@link EditorPanel}.
    * 
    * @param editorPanel The {@link EditorPanel} to close.
    * @param modifiedFileCount The modified file count.
    * @param confirmedAll The confirmed all value.
    * @param notConfirmedAll The not confirmed all value.
    * @return The {@link CloseState}.
    */
   public final CloseState handleClose ( EditorPanel editorPanel,
       int modifiedFileCount, boolean confirmedAll, boolean notConfirmedAll )
   {
     boolean resultConfirmedAll = false;
     boolean resultNotConfirmedAll = notConfirmedAll;
 
     if ( editorPanel.isModified () )
     {
       if ( notConfirmedAll )
       {
         // do nothing
       }
       else if ( confirmedAll )
       {
         handleSave ( editorPanel );
       }
       else
       {
         ConfirmDialog confirmDialog = new ConfirmDialog ( this.gui, Messages
             .getString ( "MainWindow.CloseModifyMessage", editorPanel //$NON-NLS-1$
                 .getName () ), Messages
             .getString ( "MainWindow.CloseModifyTitle" ), true, //$NON-NLS-1$
             modifiedFileCount > 1, true, modifiedFileCount > 1, true );
         confirmDialog.show ();
 
         if ( confirmDialog.isConfirmed () )
         {
           handleSave ( editorPanel );
         }
         else if ( confirmDialog.isConfirmedAll () )
         {
           handleSave ( editorPanel );
           resultConfirmedAll = true;
         }
         else if ( confirmDialog.isNotConfirmedAll () )
         {
           resultNotConfirmedAll = true;
         }
         else if ( confirmDialog.isCanceled () )
         {
           return CloseState.CANCELED;
         }
       }
     }
 
     this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ().removeEditorPanel (
         editorPanel );
 
     // check if all editor panels are closed now
     handleTabbedPaneStateChanged ();
 
     if ( resultNotConfirmedAll )
     {
       return CloseState.NOT_CONFIRMED_ALL;
     }
 
     if ( resultConfirmedAll )
     {
       return CloseState.CONFIRMED_ALL;
     }
 
     return CloseState.CONFIRMED;
   }
 
 
   /**
    * Handles the close all event.
    */
   public final void handleCloseAll ()
   {
     ActiveEditor activeEditor = this.jGTIMainSplitPane.getActiveEditor ();
 
     boolean yesToAll = false;
     boolean noToAll = false;
 
     // close all right editor panels
     this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
     handleTabbedPaneStateChanged ();
     for ( int i = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
         .getComponentCount () - 1 ; i >= 0 ; i-- )
     {
       EditorPanel current = this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPaneRight ().getEditorPanel ( i );
       this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
           .setSelectedEditorPanel ( current );
 
       // check if the close was canceled
       CloseState closeState = handleClose ( current, getModifiedFileCount (),
           yesToAll, noToAll );
 
       if ( closeState.equals ( CloseState.CANCELED ) )
       {
         return;
       }
       if ( closeState.equals ( CloseState.NOT_CONFIRMED_ALL ) )
       {
         noToAll = true;
       }
       if ( closeState.equals ( CloseState.CONFIRMED_ALL ) )
       {
         yesToAll = true;
       }
     }
 
     // close all left editor panels
     this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
     handleTabbedPaneStateChanged ();
     for ( int i = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
         .getComponentCount () - 1 ; i >= 0 ; i-- )
     {
       EditorPanel current = this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPaneLeft ().getEditorPanel ( i );
       this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
           .setSelectedEditorPanel ( current );
 
       // check if the close was canceled
       CloseState closeState = handleClose ( current, getModifiedFileCount (),
           yesToAll, noToAll );
       if ( closeState.equals ( CloseState.CANCELED ) )
       {
         return;
       }
       if ( closeState.equals ( CloseState.NOT_CONFIRMED_ALL ) )
       {
         noToAll = true;
       }
       if ( closeState.equals ( CloseState.CONFIRMED_ALL ) )
       {
         yesToAll = true;
       }
     }
 
     this.jGTIMainSplitPane.setActiveEditor ( activeEditor );
   }
 
 
   /**
    * Returns the modified file count.
    * 
    * @return The modified file count.
    */
   private final int getModifiedFileCount ()
   {
     int result = 0;
 
     for ( EditorPanel current : this.jGTIMainSplitPane )
     {
       if ( current.isModified () )
       {
         result++ ;
       }
     }
 
     return result;
   }
 
 
   /**
    * Handles console state changes.
    */
   public final void handleConsoleStateChanged ()
   {
     EditorPanel editorPanel = this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ();
 
     boolean selected = this.gui.getJCheckBoxMenuItemConsole ().isSelected ();
     if ( PreferenceManager.getInstance ().getVisibleConsole () != selected )
     {
       if ( selected )
       {
         addButtonState ( ButtonState.SELECTED_CONSOLE_TABLE );
       }
       else
       {
         removeButtonState ( ButtonState.SELECTED_CONSOLE_TABLE );
       }
 
       editorPanel.setVisibleConsole ( selected );
       PreferenceManager.getInstance ().setVisibleConsole ( selected );
     }
   }
 
 
   /**
    * Handles the convert to action performed.
    * 
    * @param entityType The {@link EntityType} to convert to.
    * @param cb True if compilerbau version is used
    */
   public final void handleConvertTo ( EntityType entityType, boolean cb )
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
 
     // if there are no validation errors perform the action
     if ( handleValidate ( false ) )
     {
       // MachinePanel
       if ( panel instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
 
         if ( machinePanel.getMachine ().getMachineType ().equals (
             MachineType.DFA ) )
         {
           panel.getConverter ().convert ( MachineType.DFA, entityType, false,
               false );
         }
         else if ( machinePanel.getMachine ().getMachineType ().equals (
             MachineType.NFA ) )
         {
           panel.getConverter ().convert ( MachineType.NFA, entityType, false,
               false );
         }
         else if ( machinePanel.getMachine ().getMachineType ().equals (
             MachineType.ENFA ) )
         {
           panel.getConverter ().convert ( MachineType.ENFA, entityType, false,
               cb );
         }
         else if ( machinePanel.getMachine ().getMachineType ().equals (
             MachineType.PDA ) )
         {
           panel.getConverter ().convert ( MachineType.PDA, entityType, false,
               false );
         }
         else
         {
           throw new RuntimeException ( "unsupported machine type" ); //$NON-NLS-1$
         }
       }
       // GrammarPanel
       else if ( panel instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) panel;
         if ( grammarPanel.getGrammar ().getGrammarType ().equals (
             GrammarType.RG ) )
         {
           panel.getConverter ().convert ( GrammarType.RG, entityType, false,
               false );
         }
         else if ( grammarPanel.getGrammar ().getGrammarType ().equals (
             GrammarType.CFG ) )
         {
           panel.getConverter ().convert ( GrammarType.CFG, entityType, false,
               false );
         }
         else
         {
           throw new RuntimeException ( "unsupported grammar type" ); //$NON-NLS-1$
         }
       }
       else if ( panel instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) panel;
         regexPanel.getConverter ().convert ( RegexType.REGEX, entityType,
             false, false );
       }
       else
       {
         throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
       }
     }
   }
 
 
   /**
    * Handles the convert to complete action performed.
    * 
    * @param entityType The {@link EntityType} to convert to.
    * @param cb True if compilerbau version is used
    */
   public final void handleConvertToComplete ( EntityType entityType,
       @SuppressWarnings ( "unused" ) boolean cb )
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
 
     // if there are no validation errors perform the action
     if ( handleValidate ( false ) )
     {
       // MachinePanel
       if ( panel instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
 
         if ( machinePanel.getMachine ().getMachineType ().equals (
             MachineType.DFA ) )
         {
           panel.getConverter ().convert ( MachineType.DFA, entityType, true,
               false );
         }
         else if ( machinePanel.getMachine ().getMachineType ().equals (
             MachineType.NFA ) )
         {
           panel.getConverter ().convert ( MachineType.NFA, entityType, true,
               false );
         }
         else if ( machinePanel.getMachine ().getMachineType ().equals (
             MachineType.ENFA ) )
         {
           panel.getConverter ().convert ( MachineType.ENFA, entityType, true,
               false );
         }
         else if ( machinePanel.getMachine ().getMachineType ().equals (
             MachineType.PDA ) )
         {
           panel.getConverter ().convert ( MachineType.PDA, entityType, true,
               false );
         }
         else
         {
           throw new RuntimeException ( "unsupported machine type" ); //$NON-NLS-1$
         }
       }
       else
       {
         throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
       }
     }
   }
 
 
   /**
    * Handle the create RDP button clicked
    */
   public final void handleCreateRDP ()
   {
     if ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () instanceof GrammarPanel )
     {
       GrammarPanel gp = ( GrammarPanel ) this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ();
       gp.handleCreateRDP ();
     }
     else
     {
       throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * Uses the active {@link EditorPanel} as draft for a new file.
    * 
    * @param grammarType The type of the new file.
    */
   public final void handleDraftFor ( GrammarType grammarType )
   {
     try
     {
       DefaultGrammarModel model = new DefaultGrammarModel (
           this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
               .getSelectedEditorPanel ().getModel ().getElement (), grammarType
               .toString () );
       EditorPanel newEditorPanel = new GrammarPanel ( this.gui, model, null );
 
       TreeSet < String > nameList = new TreeSet < String > ();
       int count = 0;
       for ( EditorPanel current : this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane () )
       {
         if ( current.getFile () == null )
         {
           nameList.add ( current.getName () );
           count++ ;
         }
       }
 
       String name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
           + "." + grammarType.toString ().toLowerCase (); //$NON-NLS-1$
       while ( nameList.contains ( name ) )
       {
         count++ ;
         name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
             + "." + grammarType.toString ().toLowerCase (); //$NON-NLS-1$
       }
 
       newEditorPanel.setName ( name );
       this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ().addEditorPanel (
           newEditorPanel );
       newEditorPanel
           .addModifyStatusChangedListener ( this.modifyStatusChangedListener );
       this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
           .setSelectedEditorPanel ( newEditorPanel );
 
       addButtonState ( ButtonState.ENABLED_SAVE_AS );
       addButtonState ( ButtonState.ENABLED_SAVE_ALL );
       addButtonState ( ButtonState.ENABLED_CLOSE );
       addButtonState ( ButtonState.ENABLED_CLOSE_ALL );
       addButtonState ( ButtonState.ENABLED_PRINT );
       addButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
       addButtonState ( ButtonState.ENABLED_VALIDATE );
       addButtonState ( ButtonState.ENABLED_DRAFT_FOR );
     }
     catch ( StoreException exc )
     {
       InfoDialog infoDialog = new InfoDialog ( this.gui, exc.getMessage (),
           Messages.getString ( "MainWindow.ErrorLoad" ) ); //$NON-NLS-1$
       infoDialog.show ();
     }
     catch ( NonterminalSymbolSetException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
     catch ( TerminalSymbolSetException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
   }
 
 
   /**
    * Uses the active {@link EditorPanel} as draft for a new file.
    * 
    * @param machineType The type of the new file.
    */
   public final void handleDraftFor ( MachineType machineType )
   {
     try
     {
       DefaultMachineModel model = new DefaultMachineModel (
           this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
               .getSelectedEditorPanel ().getModel ().getElement (), machineType
               .toString () );
       EditorPanel newEditorPanel = new MachinePanel ( this.gui, model, null );
 
       TreeSet < String > nameList = new TreeSet < String > ();
       int count = 0;
       for ( EditorPanel current : this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane () )
       {
         if ( current.getFile () == null )
         {
           nameList.add ( current.getName () );
           count++ ;
         }
       }
 
       String name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
           + "." + machineType.toString ().toLowerCase (); //$NON-NLS-1$
       while ( nameList.contains ( name ) )
       {
         count++ ;
         name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
             + "." + machineType.toString ().toLowerCase (); //$NON-NLS-1$
       }
 
       newEditorPanel.setName ( name );
       this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ().addEditorPanel (
           newEditorPanel );
       newEditorPanel
           .addModifyStatusChangedListener ( this.modifyStatusChangedListener );
       this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
           .setSelectedEditorPanel ( newEditorPanel );
 
       addButtonState ( ButtonState.ENABLED_SAVE_AS );
       addButtonState ( ButtonState.ENABLED_SAVE_ALL );
       addButtonState ( ButtonState.ENABLED_CLOSE );
       addButtonState ( ButtonState.ENABLED_CLOSE_ALL );
       addButtonState ( ButtonState.ENABLED_PRINT );
       addButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
       addButtonState ( ButtonState.ENABLED_VALIDATE );
       addButtonState ( ButtonState.ENABLED_DRAFT_FOR );
     }
     catch ( StoreException exc )
     {
       InfoDialog infoDialog = new InfoDialog ( this.gui, exc.getMessage (),
           Messages.getString ( "MainWindow.ErrorLoad" ) ); //$NON-NLS-1$
       infoDialog.show ();
     }
     catch ( TransitionSymbolOnlyOneTimeException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
     catch ( StateException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
     catch ( AlphabetException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
     catch ( TransitionException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
   }
 
 
   /**
    * Handles the edit document action in the toolbar.
    */
   public final void handleEditDocument ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     panel.handleToolbarEditDocument ();
   }
 
 
   /**
    * Handles the edit {@link Machine} event.
    */
   public final void handleEditMachine ()
   {
     logger.debug ( "handleEditMachine", //$NON-NLS-1$
         "handle edit machine" ); //$NON-NLS-1$
 
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "unsupported panel" ); //$NON-NLS-1$
     }
 
     MachinePanel machinePanel = ( MachinePanel ) panel;
     machinePanel.handleEditMachine ();
 
     addButtonState ( ButtonState.ENABLED_MACHINE_EDIT_ITEMS );
     addButtonState ( ButtonState.ENABLED_NAVIGATION_DEACTIVE );
     addButtonState ( ButtonState.ENABLED_CONSOLE_TABLE );
     addButtonState ( ButtonState.ENABLED_ENTER_WORD );
     addButtonState ( ButtonState.ENABLED_VALIDATE );
     addButtonState ( ButtonState.ENABLED_AUTO_LAYOUT );
     addButtonState ( ButtonState.ENABLED_CONVERT_TO );
     addButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE );
     addButtonState ( ButtonState.ENABLED_DRAFT_FOR );
     addButtonState ( ButtonState.ENABLED_REACHABLE_STATES );
     addButtonState ( ButtonState.ENABLED_REORDER_STATE_NAMES );
     addButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
     if ( machinePanel.getMachine ().getMachineType ().equals ( MachineType.DFA ) )
     {
       addButtonState ( ButtonState.ENABLED_MINIMIZE );
       addButtonState ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX );
     }
 
     removeButtonState ( ButtonState.SELECTED_ENTER_WORD );
     removeButtonState ( ButtonState.ENABLED_EDIT_MACHINE );
 
     if ( machinePanel.isUndoAble () )
     {
       addButtonState ( ButtonState.ENABLED_UNDO );
     }
     else
     {
       removeButtonState ( ButtonState.ENABLED_UNDO );
     }
 
     if ( machinePanel.isRedoAble () )
     {
       addButtonState ( ButtonState.ENABLED_REDO );
     }
     else
     {
       removeButtonState ( ButtonState.ENABLED_REDO );
     }
   }
 
 
   /**
    * Handles the edit {@link Production} event.
    */
   public final void handleEditProduction ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel instanceof GrammarPanel )
     {
       GrammarPanel grammarPanel = ( GrammarPanel ) panel;
       grammarPanel.handleEditProduction ();
     }
     else
     {
       throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * Handles the enter {@link Word} event.
    */
   public final void handleEnterWord ()
   {
     logger.debug ( "handleEnterWord", //$NON-NLS-1$
         "handle enter word" ); //$NON-NLS-1$
 
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
 
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "unsupported panel" ); //$NON-NLS-1$
     }
 
     MachinePanel machinePanel = ( MachinePanel ) panel;
 
     // if there are no validation errors perform the action
     if ( handleValidate ( false ) )
     {
       machinePanel.handleEnterWord ();
 
       addButtonState ( ButtonState.ENABLED_NAVIGATION_START );
       addButtonState ( ButtonState.ENABLED_EDIT_MACHINE );
       addButtonState ( ButtonState.SELECTED_ENTER_WORD );
 
       removeButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
       removeButtonState ( ButtonState.ENABLED_CONSOLE_TABLE );
       removeButtonState ( ButtonState.ENABLED_MACHINE_EDIT_ITEMS );
       removeButtonState ( ButtonState.ENABLED_ENTER_WORD );
       removeButtonState ( ButtonState.ENABLED_VALIDATE );
       removeButtonState ( ButtonState.ENABLED_AUTO_LAYOUT );
       removeButtonState ( ButtonState.ENABLED_CONVERT_TO );
       removeButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE );
       removeButtonState ( ButtonState.ENABLED_DRAFT_FOR );
       removeButtonState ( ButtonState.ENABLED_REACHABLE_STATES );
       removeButtonState ( ButtonState.ENABLED_REORDER_STATE_NAMES );
       removeButtonState ( ButtonState.ENABLED_MINIMIZE );
       removeButtonState ( ButtonState.ENABLED_UNDO );
       removeButtonState ( ButtonState.ENABLED_REDO );
     }
     else
     {
       removeButtonState ( ButtonState.SELECTED_ENTER_WORD );
     }
   }
 
 
   /**
    * Handles the action event of the enter {@link Word}
    * {@link JGTIToolBarToggleButton}.
    */
   public final void handleEnterWordToggleButton ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
     MachinePanel machinePanel = ( MachinePanel ) panel;
 
     boolean selected = this.gui.getJGTIToolBarToggleButtonEnterWord ()
         .isSelected ();
 
     if ( selected )
     {
       // happens if the enter word menu item state changed
       if ( !machinePanel.getMachineMode ().equals ( MachineMode.EDIT_MACHINE ) )
       {
         return;
       }
 
       logger.debug ( "handleEnterWordToggleButton", //$NON-NLS-1$
           "handle enter word toggle button selected: " + selected ); //$NON-NLS-1$
 
       handleEnterWord ();
     }
     else
     {
       // happens if the enter word menu item state changed
       if ( machinePanel.getMachineMode ().equals ( MachineMode.EDIT_MACHINE ) )
       {
         return;
       }
 
       logger.debug ( "handleEnterWordToggleButton", //$NON-NLS-1$
           "handle enter word toggle button selected: " + selected ); //$NON-NLS-1$
 
       handleEditMachine ();
     }
   }
 
 
   /**
    * Handles the {@link Exchange}.
    */
   public final void handleExchange ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel == null )
     {
       ExchangeDialog exchangeDialog = new ExchangeDialog ( this, null, null );
       exchangeDialog.show ();
     }
     else
     {
       panel.handleExchange ();
     }
   }
 
 
   /**
    * Handles the export picture event.
    */
   public final void handleExportPicture ()
   {
     if ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () instanceof MachinePanel )
     {
       MachinePanel machinePanel = ( MachinePanel ) this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ();
       machinePanel.handleExportPicture ();
     }
     else if ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () instanceof RegexPanel )
     {
      if ( !handleValidate ( false ) )
      {
        return;
      }
       RegexPanel regexPanel = ( RegexPanel ) this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ();
       regexPanel.handleExportPicture ();
     }
     else
     {
       throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * Handles the history event.
    */
   public final void handleHistory ()
   {
     if ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () instanceof MachinePanel )
     {
       MachinePanel machinePanel = ( MachinePanel ) this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ();
       machinePanel.handleHistory ();
     }
     else
     {
       throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * Handles the to Latex event
    */
   public final void handleToLatex ()
   {
     if ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () instanceof RegexPanel )
     {
      if ( !handleValidate ( false ) )
      {
        return;
      }
       RegexPanel regexPanel = ( RegexPanel ) this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ();
       regexPanel.handleToLatexButtonClicked ();
     }
     else
     {
       throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * Handles the minimize {@link Machine} event.
    */
   public final void handleMinimize ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel instanceof MachinePanel )
     {
       // if there are no validation errors perform the action
       if ( handleValidate ( false ) )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
         MinimizeMachineDialog dialog = new MinimizeMachineDialog ( this.gui,
             machinePanel );
         dialog.minimize ();
       }
     }
     else
     {
       throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * Handles the new event.
    */
   public final void handleNew ()
   {
     NewDialog newDialog = new NewDialog ( this.gui );
     newDialog.show ();
     EditorPanel newEditorPanel = newDialog.getEditorPanel ();
     if ( newEditorPanel != null )
     {
       TreeSet < String > nameList = new TreeSet < String > ();
       int count = 0;
       for ( EditorPanel current : this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane () )
       {
         if ( current.getFile () == null )
         {
           nameList.add ( current.getName () );
           count++ ;
         }
       }
 
       String name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
           + newDialog.getEditorPanel ().getFileEnding ();
       while ( nameList.contains ( name ) )
       {
         count++ ;
         name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
             + newDialog.getEditorPanel ().getFileEnding ();
       }
 
       newEditorPanel.setName ( name );
       this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ().addEditorPanel (
           newEditorPanel );
       newEditorPanel
           .addModifyStatusChangedListener ( this.modifyStatusChangedListener );
       this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
           .setSelectedEditorPanel ( newEditorPanel );
 
       addButtonState ( ButtonState.ENABLED_SAVE_AS );
       addButtonState ( ButtonState.ENABLED_SAVE_ALL );
       addButtonState ( ButtonState.ENABLED_CLOSE );
       addButtonState ( ButtonState.ENABLED_CLOSE_ALL );
       addButtonState ( ButtonState.ENABLED_PRINT );
       addButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
       addButtonState ( ButtonState.ENABLED_VALIDATE );
       addButtonState ( ButtonState.ENABLED_DRAFT_FOR );
       addButtonState ( ButtonState.ENABLED_MACHINE_EDIT_ITEMS );
     }
   }
 
 
   /**
    * Handles the new event with a given {@link DefaultModel}.
    * 
    * @param defaultModel The {@link DefaultModel}.
    */
   public void handleNew ( DefaultModel defaultModel )
   {
     if ( defaultModel != null )
     {
       EditorPanel newEditorPanel;
       if ( defaultModel instanceof DefaultMachineModel )
       {
         newEditorPanel = new MachinePanel ( this.gui,
             ( DefaultMachineModel ) defaultModel, null );
       }
       else if ( defaultModel instanceof DefaultGrammarModel )
       {
         newEditorPanel = new GrammarPanel ( this.gui,
             ( DefaultGrammarModel ) defaultModel, null );
       }
       else if ( defaultModel instanceof DefaultRegexModel )
       {
         newEditorPanel = new RegexPanel ( this.gui,
             ( DefaultRegexModel ) defaultModel, null );
       }
       else
       {
         throw new RuntimeException ( "unsupported model" ); //$NON-NLS-1$
       }
 
       TreeSet < String > nameList = new TreeSet < String > ();
       int count = 0;
       for ( EditorPanel current : this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane () )
       {
         if ( current.getFile () == null )
         {
           nameList.add ( current.getName () );
           count++ ;
         }
       }
 
       String name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
           + newEditorPanel.getFileEnding ();
       while ( nameList.contains ( name ) )
       {
         count++ ;
         name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
             + newEditorPanel.getFileEnding ();
       }
 
       newEditorPanel.setName ( name );
       this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ().addEditorPanel (
           newEditorPanel );
       newEditorPanel
           .addModifyStatusChangedListener ( this.modifyStatusChangedListener );
       this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
           .setSelectedEditorPanel ( newEditorPanel );
 
       addButtonState ( ButtonState.ENABLED_SAVE_AS );
       addButtonState ( ButtonState.ENABLED_SAVE_ALL );
       addButtonState ( ButtonState.ENABLED_CLOSE );
       addButtonState ( ButtonState.ENABLED_CLOSE_ALL );
       addButtonState ( ButtonState.ENABLED_PRINT );
       addButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
       addButtonState ( ButtonState.ENABLED_VALIDATE );
       addButtonState ( ButtonState.ENABLED_DRAFT_FOR );
       addButtonState ( ButtonState.ENABLED_MACHINE_EDIT_ITEMS );
     }
   }
 
 
   /**
    * Handles the new event with a given {@link Element}.
    * 
    * @param element The {@link Element}.
    * @param autoLayout The auto layout flag, if true the new file is auto
    *          layouted.
    */
   public final void handleNew ( Element element, boolean autoLayout )
   {
     DefaultModel defaultModel = null;
     try
     {
       if ( element.getName ().equals ( "MachineModel" ) ) //$NON-NLS-1$
       {
         defaultModel = new DefaultMachineModel ( element, null );
 
         if ( autoLayout )
         {
           new LayoutManager ( ( DefaultMachineModel ) defaultModel, null )
               .doLayout ();
         }
       }
       else if ( element.getName ().equals ( "GrammarModel" ) ) //$NON-NLS-1$
       {
         defaultModel = new DefaultGrammarModel ( element, null );
       }
       else if ( element.getName ().equals ( "RegexModel" ) ) //$NON-NLS-1$
       {
         defaultModel = new DefaultRegexModel ( element, true );
       }
       else
       {
         throw new IllegalArgumentException ( "unsupported model" ); //$NON-NLS-1$
       }
     }
     catch ( TransitionSymbolOnlyOneTimeException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
     catch ( StateException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
     catch ( AlphabetException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
     catch ( TransitionException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
     catch ( StoreException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
     catch ( NonterminalSymbolSetException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
     catch ( TerminalSymbolSetException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
     catch ( Exception exc )
     {
       exc.printStackTrace ();
     }
 
     handleNew ( defaultModel );
   }
 
 
   /**
    * Handles the open event.
    */
   public final void handleOpen ()
   {
     OpenDialog openDialog = new OpenDialog ( this.gui, PreferenceManager
         .getInstance ().getWorkingPath () );
     openDialog.show ();
     if ( ( !openDialog.isConfirmed () )
         || ( openDialog.getSelectedFile () == null ) )
     {
       return;
     }
 
     for ( File file : openDialog.getSelectedFiles () )
     {
       openFile ( file, true );
     }
     PreferenceManager.getInstance ().setWorkingPath (
         openDialog.getSelectedFile ().getParentFile ().getAbsolutePath () );
   }
 
 
   /**
    * Handles the preferences event.
    */
   public final void handlePreferences ()
   {
     PreferencesDialog preferencesDialog = new PreferencesDialog ( this.gui );
     preferencesDialog.show ();
   }
 
 
   /**
    * Handles the print event.
    */
   public final void handlePrint ()
   {
     if ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () instanceof MachinePanel )
     {
       MachinePanel machinePanel = ( MachinePanel ) this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ();
       PrintDialog printDialog = new PrintDialog ( this.gui, machinePanel );
       printDialog.show ();
     }
     else if ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () instanceof GrammarPanel )
     {
       GrammarPanel grammarPanel = ( GrammarPanel ) this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ();
       PrintDialog printDialog = new PrintDialog ( this.gui, grammarPanel );
       printDialog.show ();
     }
     else if ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () instanceof RegexPanel )
     {
       RegexPanel regexPanel = ( RegexPanel ) this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ();
       PrintDialog printDialog = new PrintDialog ( this.gui, regexPanel );
       printDialog.show ();
     }
     else
     {
       throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * Handles the quit event.
    */
   public final void handleQuit ()
   {
     // Active file
     File activeFile = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () == null ? null : this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ().getFile ();
 
     // Opened file
     ArrayList < ObjectPair < File, ActiveEditor > > openedFiles = new ArrayList < ObjectPair < File, ActiveEditor > > ();
     for ( EditorPanel current : this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPaneLeft () )
     {
       if ( current.getFile () != null )
       {
         openedFiles.add ( new ObjectPair < File, ActiveEditor > ( current
             .getFile (), ActiveEditor.LEFT_EDITOR ) );
       }
     }
     for ( EditorPanel current : this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPaneRight () )
     {
       if ( current.getFile () != null )
       {
         openedFiles.add ( new ObjectPair < File, ActiveEditor > ( current
             .getFile (), ActiveEditor.RIGHT_EDITOR ) );
       }
     }
 
     boolean yesToAll = false;
     boolean noToAll = false;
 
     // close the right tabs
     this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
     handleTabbedPaneStateChanged ();
     for ( int i = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
         .getComponentCount () - 1 ; i >= 0 ; i-- )
     {
       EditorPanel current = this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPaneRight ().getEditorPanel ( i );
       if ( current.isModified () )
       {
         this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
             .setSelectedEditorPanel ( current );
 
         if ( yesToAll )
         {
           File file = current.handleSave ();
           if ( file != null )
           {
             this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
                 .setEditorPanelTitle ( current, file.getName () );
           }
         }
         else if ( noToAll )
         {
           // do nothing
         }
         else
         {
           int modifiedFileCount = getModifiedFileCount ();
           ConfirmDialog confirmDialog = new ConfirmDialog ( this.gui,
               Messages.getString (
                   "MainWindow.CloseModifyMessage", current.getName () ), //$NON-NLS-1$
               Messages.getString ( "MainWindow.CloseModifyTitle" ), true, //$NON-NLS-1$
               modifiedFileCount > 1, true, modifiedFileCount > 1, true );
           confirmDialog.show ();
 
           if ( confirmDialog.isConfirmed () )
           {
             File file = current.handleSave ();
             if ( file != null )
             {
               this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
                   .setEditorPanelTitle ( current, file.getName () );
             }
           }
           else if ( confirmDialog.isConfirmedAll () )
           {
             File file = current.handleSave ();
             if ( file != null )
             {
               this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
                   .setEditorPanelTitle ( current, file.getName () );
             }
             yesToAll = true;
           }
           else if ( confirmDialog.isNotConfirmedAll () )
           {
             noToAll = true;
           }
           else if ( confirmDialog.isCanceled () )
           {
             return;
           }
         }
       }
       this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
           .removeEditorPanel ( current );
     }
 
     // close the left tabs
     this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
     handleTabbedPaneStateChanged ();
     for ( int i = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
         .getComponentCount () - 1 ; i >= 0 ; i-- )
     {
       EditorPanel current = this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPaneLeft ().getEditorPanel ( i );
       if ( current.isModified () )
       {
         this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
             .setSelectedEditorPanel ( current );
 
         if ( yesToAll )
         {
           File file = current.handleSave ();
           if ( file != null )
           {
             this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
                 .setEditorPanelTitle ( current, file.getName () );
           }
         }
         else if ( noToAll )
         {
           // do nothing
         }
         else
         {
           int modifiedFileCount = getModifiedFileCount ();
           ConfirmDialog confirmDialog = new ConfirmDialog ( this.gui,
               Messages.getString (
                   "MainWindow.CloseModifyMessage", current.getName () ), //$NON-NLS-1$
               Messages.getString ( "MainWindow.CloseModifyTitle" ), true, //$NON-NLS-1$
               modifiedFileCount > 1, true, modifiedFileCount > 1, true );
           confirmDialog.show ();
 
           if ( confirmDialog.isConfirmed () )
           {
             File file = current.handleSave ();
             if ( file != null )
             {
               this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
                   .setEditorPanelTitle ( current, file.getName () );
             }
           }
           else if ( confirmDialog.isConfirmedAll () )
           {
             File file = current.handleSave ();
             if ( file != null )
             {
               this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
                   .setEditorPanelTitle ( current, file.getName () );
             }
             yesToAll = true;
           }
           else if ( confirmDialog.isNotConfirmedAll () )
           {
             noToAll = true;
           }
           else if ( confirmDialog.isCanceled () )
           {
             return;
           }
         }
       }
       this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
           .removeEditorPanel ( current );
     }
 
     PreferenceManager.getInstance ().setMainWindowPreferences ( this.gui );
     PreferenceManager.getInstance ().setOpenedFilesItem (
         new OpenedFilesItem ( openedFiles, activeFile ) );
 
     ArrayList < File > files = new ArrayList < File > ();
     for ( RecentlyUsedMenuItem item : this.recentlyUsedFiles )
     {
       files.add ( item.getFile () );
     }
     PreferenceManager.getInstance ().setRecentlyUsedFilesItem (
         new RecentlyUsedFilesItem ( files ) );
 
     // second view
     boolean selected = this.gui.getJCheckBoxMenuItemSecondView ().isSelected ();
     PreferenceManager.getInstance ().setSecondViewUsed ( selected );
 
     // system exit
     System.exit ( 0 );
   }
 
 
   /**
    * Handles the reachable states event.
    */
   public final void handleReachableStates ()
   {
     if ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () instanceof MachinePanel )
     {
       // if there are no validation errors perform the action
       if ( handleValidate ( false ) )
       {
         MachinePanel machinePanel = ( MachinePanel ) this.jGTIMainSplitPane
             .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ();
         machinePanel.handleReachableStates ();
       }
     }
     else
     {
       throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * Handles the redo event.
    */
   public final void handleRedo ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel != null )
     {
       panel.handleRedo ();
     }
   }
 
 
   /**
    * Handles the reorder state names event.
    */
   public final void handleReorderStateNames ()
   {
     if ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () instanceof MachinePanel )
     {
       MachinePanel machinePanel = ( MachinePanel ) this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ();
       machinePanel.handleReorderStateNames ();
     }
     else
     {
       throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * Handles the save event.
    */
   public final void handleSave ()
   {
     if ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () == null )
     {
       throw new RuntimeException ( "no selected editor panel" ); //$NON-NLS-1$
     }
 
     handleSave ( this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel () );
   }
 
 
   /**
    * Handles the save event.
    * 
    * @param editorPanel The {@link EditorPanel} to be saved.
    */
   public final void handleSave ( EditorPanel editorPanel )
   {
     File file = editorPanel.handleSave ();
     if ( file != null )
     {
       RecentlyUsedMenuItem item = new RecentlyUsedMenuItem ( this, file );
       this.recentlyUsedFiles.remove ( item );
       this.recentlyUsedFiles.add ( 0, item );
       organizeRecentlyUsedFilesMenu ();
 
       for ( EditorPanel current : this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPaneLeft () )
       {
         if ( current == editorPanel )
         {
           this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
               .setEditorPanelTitle ( editorPanel, file.getName () );
           return;
         }
       }
       for ( EditorPanel current : this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPaneRight () )
       {
         if ( current == editorPanel )
         {
           this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
               .setEditorPanelTitle ( editorPanel, file.getName () );
           return;
         }
       }
     }
   }
 
 
   /**
    * Handles the save all event.
    */
   public final void handleSaveAll ()
   {
     // save the left editor panels
     for ( EditorPanel current : this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPaneLeft () )
     {
       handleSave ( current );
     }
 
     // save the right editor panels
     for ( EditorPanel current : this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPaneRight () )
     {
       handleSave ( current );
     }
   }
 
 
   /**
    * Handles the save as event.
    */
   public final void handleSaveAs ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     File file = panel.handleSaveAs ();
     if ( file != null )
     {
       RecentlyUsedMenuItem item = new RecentlyUsedMenuItem ( this, file );
       this.recentlyUsedFiles.remove ( item );
       this.recentlyUsedFiles.add ( 0, item );
       organizeRecentlyUsedFilesMenu ();
       for ( EditorPanel current : this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane () )
       {
         if ( ( !current.equals ( this.jGTIMainSplitPane
             .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel () ) && file
             .equals ( current.getFile () ) ) )
         {
           this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
               .removeEditorPanel ( current );
         }
       }
       this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
           .setEditorPanelTitle ( panel, file.getName () );
     }
   }
 
 
   /**
    * Handles the mouse released event on a second view component.
    * 
    * @param event The {@link MouseEvent}.
    */
   public final void handleSecondViewMouseReleased ( MouseEvent event )
   {
     if ( event.getSource () instanceof JGTITabbedPane )
     {
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft () == event.getSource () )
       {
         if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
             ActiveEditor.LEFT_EDITOR ) )
         {
           JGTITabbedPane tabbedPane = ( JGTITabbedPane ) event.getSource ();
           tabbedPane.requestFocus ();
           this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
           logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
               "handle second view mouse released" );//$NON-NLS-1$
           handleTabbedPaneStateChanged ();
         }
         else
         {
           JGTITabbedPane tabbedPane = ( JGTITabbedPane ) event.getSource ();
           tabbedPane.requestFocus ();
         }
         return;
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight () == event.getSource () )
       {
         if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
             ActiveEditor.RIGHT_EDITOR ) )
         {
           JGTITabbedPane tabbedPane = ( JGTITabbedPane ) event.getSource ();
           tabbedPane.requestFocus ();
           this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
           logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
               "handle second view mouse released" );//$NON-NLS-1$
           handleTabbedPaneStateChanged ();
         }
         else
         {
           JGTITabbedPane tabbedPane = ( JGTITabbedPane ) event.getSource ();
           tabbedPane.requestFocus ();
         }
         return;
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( machinePanel.getJTabbedPaneConsole () == event.getSource () )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             JGTITabbedPane tabbedPane = ( JGTITabbedPane ) event.getSource ();
             tabbedPane.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( machinePanel.getJTabbedPaneConsole () == event.getSource () )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             JGTITabbedPane tabbedPane = ( JGTITabbedPane ) event.getSource ();
             tabbedPane.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( grammarPanel.getJTabbedPaneConsole () == event.getSource () )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             JGTITabbedPane tabbedPane = ( JGTITabbedPane ) event.getSource ();
             tabbedPane.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( grammarPanel.getJTabbedPaneConsole () == event.getSource () )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             JGTITabbedPane tabbedPane = ( JGTITabbedPane ) event.getSource ();
             tabbedPane.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( regexPanel.getJTabbedPaneConsole () == event.getSource () )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             JGTITabbedPane tabbedPane = ( JGTITabbedPane ) event.getSource ();
             tabbedPane.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( regexPanel.getJTabbedPaneConsole () == event.getSource () )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             JGTITabbedPane tabbedPane = ( JGTITabbedPane ) event.getSource ();
             tabbedPane.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
     }
     else if ( event.getSource () instanceof JGTIGraph )
     {
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( machinePanel.getJGTIGraph () == event.getSource () )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( machinePanel.getJGTIGraph () == event.getSource () )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( regexPanel.getJGTIGraph () == event.getSource () )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( regexPanel.getJGTIGraph () == event.getSource () )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
     }
     else if ( event.getSource () instanceof JGTITable )
     {
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( machinePanel.getGUI ().jGTITableMachine == event.getSource () )
             || ( machinePanel.getGUI ().jGTITableMachinePDA == event
                 .getSource () )
             || ( machinePanel.getGUI ().jGTITableErrors == event.getSource () )
             || ( machinePanel.getGUI ().jGTITableWarnings == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             JGTITable table = ( JGTITable ) event.getSource ();
             table.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( machinePanel.getGUI ().jGTITableMachine == event.getSource () )
             || ( machinePanel.getGUI ().jGTITableMachinePDA == event
                 .getSource () )
             || ( machinePanel.getGUI ().jGTITableErrors == event.getSource () )
             || ( machinePanel.getGUI ().jGTITableWarnings == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             JGTITable table = ( JGTITable ) event.getSource ();
             table.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( grammarPanel.getGUI ().jGTITableGrammar == event.getSource () )
             || ( grammarPanel.getGUI ().jGTITableErrors == event.getSource () )
             || ( grammarPanel.getGUI ().jGTITableWarnings == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             JGTITable table = ( JGTITable ) event.getSource ();
             table.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( grammarPanel.getGUI ().jGTITableGrammar == event.getSource () )
             || ( grammarPanel.getGUI ().jGTITableErrors == event.getSource () )
             || ( grammarPanel.getGUI ().jGTITableWarnings == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             JGTITable table = ( JGTITable ) event.getSource ();
             table.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( regexPanel.getGUI ().jGTITableErrors == event.getSource () )
             || ( regexPanel.getGUI ().jGTITableWarnings == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             JGTITable table = ( JGTITable ) event.getSource ();
             table.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( regexPanel.getGUI ().jGTITableErrors == event.getSource () )
             || ( regexPanel.getGUI ().jGTITableWarnings == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             JGTITable table = ( JGTITable ) event.getSource ();
             table.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
     }
     else if ( event.getSource () instanceof JScrollBar )
     {
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( machinePanel.getGUI ().jGTIScrollPaneErrors
             .getHorizontalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneErrors
                 .getVerticalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneGraph
                 .getHorizontalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneGraph
                 .getVerticalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneMachine
                 .getHorizontalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneMachine
                 .getVerticalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneMachinePDA
                 .getHorizontalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneMachinePDA
                 .getVerticalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneWarnings
                 .getHorizontalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneWarnings
                 .getVerticalScrollBar () == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             JScrollBar bar = ( JScrollBar ) event.getSource ();
             bar.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( machinePanel.getGUI ().jGTIScrollPaneErrors
             .getHorizontalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneErrors
                 .getVerticalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneGraph
                 .getHorizontalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneGraph
                 .getVerticalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneMachine
                 .getHorizontalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneMachine
                 .getVerticalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneMachinePDA
                 .getHorizontalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneMachinePDA
                 .getVerticalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneWarnings
                 .getHorizontalScrollBar () == event.getSource () )
             || ( machinePanel.getGUI ().jGTIScrollPaneWarnings
                 .getVerticalScrollBar () == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             JScrollBar bar = ( JScrollBar ) event.getSource ();
             bar.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( grammarPanel.getGUI ().jGTIScrollPaneGrammar
             .getHorizontalScrollBar () == event.getSource () )
             || ( grammarPanel.getGUI ().jGTIScrollPaneGrammar
                 .getVerticalScrollBar () == event.getSource () )
             || ( grammarPanel.getGUI ().jGTIScrollPaneErrors
                 .getHorizontalScrollBar () == event.getSource () )
             || ( grammarPanel.getGUI ().jGTIScrollPaneErrors
                 .getVerticalScrollBar () == event.getSource () )
             || ( grammarPanel.getGUI ().jGTIScrollPaneWarnings
                 .getHorizontalScrollBar () == event.getSource () )
             || ( grammarPanel.getGUI ().jGTIScrollPaneWarnings
                 .getVerticalScrollBar () == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
 
             JScrollBar bar = ( JScrollBar ) event.getSource ();
             bar.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( grammarPanel.getGUI ().jGTIScrollPaneGrammar
             .getHorizontalScrollBar () == event.getSource () )
             || ( grammarPanel.getGUI ().jGTIScrollPaneGrammar
                 .getVerticalScrollBar () == event.getSource () )
             || ( grammarPanel.getGUI ().jGTIScrollPaneErrors
                 .getHorizontalScrollBar () == event.getSource () )
             || ( grammarPanel.getGUI ().jGTIScrollPaneErrors
                 .getVerticalScrollBar () == event.getSource () )
             || ( grammarPanel.getGUI ().jGTIScrollPaneWarnings
                 .getHorizontalScrollBar () == event.getSource () )
             || ( grammarPanel.getGUI ().jGTIScrollPaneWarnings
                 .getVerticalScrollBar () == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             JScrollBar bar = ( JScrollBar ) event.getSource ();
             bar.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( regexPanel.getGUI ().jGTIScrollPaneErrors
             .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneErrors
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneGraph
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneGraph
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneWarnings
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneWarnings
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneNodeInfo
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneNodeInfo
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneFirstpos
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneFirstpos
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneFollowpos
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneFollowpos
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneLastpos
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneLastpos
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneNullable
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneNullable
                 .getVerticalScrollBar () == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             JScrollBar bar = ( JScrollBar ) event.getSource ();
             bar.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( regexPanel.getGUI ().jGTIScrollPaneErrors
             .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneErrors
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneGraph
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneGraph
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneWarnings
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneWarnings
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneNodeInfo
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().jGTIScrollPaneNodeInfo
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneFirstpos
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneFirstpos
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneFollowpos
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneFollowpos
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneLastpos
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneLastpos
                 .getVerticalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneNullable
                 .getHorizontalScrollBar () == event.getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jScrollPaneNullable
                 .getVerticalScrollBar () == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             JScrollBar bar = ( JScrollBar ) event.getSource ();
             bar.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
     }
     else if ( event.getSource () instanceof JTableHeader )
     {
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( machinePanel.getGUI ().jGTITableMachine.getTableHeader () == event
             .getSource () )
             || ( machinePanel.getGUI ().jGTITableMachinePDA.getTableHeader () == event
                 .getSource () )
             || ( machinePanel.getGUI ().jGTITableErrors.getTableHeader () == event
                 .getSource () )
             || ( machinePanel.getGUI ().jGTITableWarnings.getTableHeader () == event
                 .getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             JTableHeader tableHeader = ( JTableHeader ) event.getSource ();
             tableHeader.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( machinePanel.getGUI ().jGTITableMachine.getTableHeader () == event
             .getSource () )
             || ( machinePanel.getGUI ().jGTITableMachinePDA.getTableHeader () == event
                 .getSource () )
             || ( machinePanel.getGUI ().jGTITableErrors.getTableHeader () == event
                 .getSource () )
             || ( machinePanel.getGUI ().jGTITableWarnings.getTableHeader () == event
                 .getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             JTableHeader tableHeader = ( JTableHeader ) event.getSource ();
             tableHeader.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( grammarPanel.getGUI ().jGTITableGrammar.getTableHeader () == event
             .getSource () )
             || ( grammarPanel.getGUI ().jGTITableErrors.getTableHeader () == event
                 .getSource () )
             || ( grammarPanel.getGUI ().jGTITableWarnings.getTableHeader () == event
                 .getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             JTableHeader tableHeader = ( JTableHeader ) event.getSource ();
             tableHeader.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( grammarPanel.getGUI ().jGTITableGrammar.getTableHeader () == event
             .getSource () )
             || ( grammarPanel.getGUI ().jGTITableErrors.getTableHeader () == event
                 .getSource () )
             || ( grammarPanel.getGUI ().jGTITableWarnings.getTableHeader () == event
                 .getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             JTableHeader tableHeader = ( JTableHeader ) event.getSource ();
             tableHeader.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( regexPanel.getGUI ().jGTITableErrors.getTableHeader () == event
             .getSource () )
             || ( regexPanel.getGUI ().jGTITableWarnings.getTableHeader () == event
                 .getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             JTableHeader tableHeader = ( JTableHeader ) event.getSource ();
             tableHeader.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( regexPanel.getGUI ().jGTITableErrors.getTableHeader () == event
             .getSource () )
             || ( regexPanel.getGUI ().jGTITableWarnings.getTableHeader () == event
                 .getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             JTableHeader tableHeader = ( JTableHeader ) event.getSource ();
             tableHeader.requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
 
     }
     else if ( event.getSource () instanceof StyledParserEditor )
     {
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( regexPanel.getGUI ().styledRegexAlphabetParserPanel.getEditor () == event
             .getSource () )
             || ( regexPanel.getGUI ().styledRegexParserPanel.getEditor () == event
                 .getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( regexPanel.getGUI ().styledRegexAlphabetParserPanel.getEditor () == event
             .getSource () )
             || ( regexPanel.getGUI ().styledRegexParserPanel.getEditor () == event
                 .getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( grammarPanel.getGUI ().styledNonterminalSymbolSetParserPanel
             .getEditor () == event.getSource () )
             || ( grammarPanel.getGUI ().styledStartNonterminalSymbolParserPanel
                 .getEditor () == event.getSource () )
             || ( grammarPanel.getGUI ().styledTerminalSymbolSetParserPanel
                 .getEditor () == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( grammarPanel.getGUI ().styledNonterminalSymbolSetParserPanel
             .getEditor () == event.getSource () )
             || ( grammarPanel.getGUI ().styledStartNonterminalSymbolParserPanel
                 .getEditor () == event.getSource () )
             || ( grammarPanel.getGUI ().styledTerminalSymbolSetParserPanel
                 .getEditor () == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
     }
     else if ( event.getSource () instanceof JGTITextArea )
     {
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( regexPanel.getGUI ().regexNodeInfoPanel.jGTITextAreaFirstpos == event
             .getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jGTITextAreaFollowpos == event
                 .getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jGTITextAreaLastpos == event
                 .getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jGTITextAreaNullable == event
                 .getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( regexPanel.getGUI ().regexNodeInfoPanel.jGTITextAreaFirstpos == event
             .getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jGTITextAreaFollowpos == event
                 .getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jGTITextAreaLastpos == event
                 .getSource () )
             || ( regexPanel.getGUI ().regexNodeInfoPanel.jGTITextAreaNullable == event
                 .getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
     }
     else if ( event.getSource () instanceof SideBar )
     {
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( regexPanel.getGUI ().styledRegexAlphabetParserPanel
             .getSideBar () == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             regexPanel.getGUI ().styledRegexAlphabetParserPanel.getEditor ()
                 .requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
         else if ( regexPanel.getGUI ().styledRegexParserPanel.getSideBar () == event
             .getSource () )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             regexPanel.getGUI ().styledRegexParserPanel.getEditor ()
                 .requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( regexPanel.getGUI ().styledRegexAlphabetParserPanel
             .getSideBar () == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             regexPanel.getGUI ().styledRegexAlphabetParserPanel.getEditor ()
                 .requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
 
         if ( regexPanel.getGUI ().styledRegexParserPanel.getSideBar () == event
             .getSource () )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             regexPanel.getGUI ().styledRegexParserPanel.getEditor ()
                 .requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneLeft ()
           .getSelectedEditorPanel () instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
 
         if ( ( grammarPanel.getGUI ().styledNonterminalSymbolSetParserPanel
             .getSideBar () == event.getSource () )
             || ( grammarPanel.getGUI ().styledStartNonterminalSymbolParserPanel
                 .getSideBar () == event.getSource () )
             || ( grammarPanel.getGUI ().styledTerminalSymbolSetParserPanel
                 .getSideBar () == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.LEFT_EDITOR ) )
           {
             grammarPanel.getGUI ().styledNonterminalSymbolSetParserPanel
                 .getEditor ().requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
       if ( this.gui.getJGTIEditorPanelTabbedPaneRight ()
           .getSelectedEditorPanel () instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) this.gui
             .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
         if ( ( grammarPanel.getGUI ().styledNonterminalSymbolSetParserPanel
             .getSideBar () == event.getSource () )
             || ( grammarPanel.getGUI ().styledStartNonterminalSymbolParserPanel
                 .getSideBar () == event.getSource () )
             || ( grammarPanel.getGUI ().styledTerminalSymbolSetParserPanel
                 .getSideBar () == event.getSource () ) )
         {
           if ( !this.jGTIMainSplitPane.getActiveEditor ().equals (
               ActiveEditor.RIGHT_EDITOR ) )
           {
             grammarPanel.getGUI ().styledNonterminalSymbolSetParserPanel
                 .getEditor ().requestFocus ();
             this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
             logger.debug ( "handleSecondViewMouseReleased", //$NON-NLS-1$
                 "handle second view mouse released" );//$NON-NLS-1$
             handleTabbedPaneStateChanged ();
           }
           return;
         }
       }
     }
     else
     {
       throw new IllegalArgumentException ( "unsupported source: " //$NON-NLS-1$
           + event.getSource () );
     }
   }
 
 
   /**
    * Handles the second view move to left event.
    */
   public final void handleSecondViewMoveToLeft ()
   {
     EditorPanel editorPanel = this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
     this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
         .removeEditorPanel ( editorPanel );
     this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
     this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ().addEditorPanel (
         editorPanel );
     this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
         .setSelectedEditorPanel ( editorPanel );
   }
 
 
   /**
    * Handles the second view move to right event.
    */
   public final void handleSecondViewMoveToRight ()
   {
     if ( !this.gui.getJCheckBoxMenuItemSecondView ().isSelected () )
     {
       this.gui.getJCheckBoxMenuItemSecondView ().setSelected ( true );
     }
 
     EditorPanel editorPanel = this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
     this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
         .removeEditorPanel ( editorPanel );
     this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
     this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ().addEditorPanel (
         editorPanel );
     this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
         .setSelectedEditorPanel ( editorPanel );
   }
 
 
   /**
    * Handles second view state changes.
    */
   public final void handleSecondViewStateChanged ()
   {
     boolean selected = this.gui.getJCheckBoxMenuItemSecondView ().isSelected ();
     logger.debug ( "handleSecondViewStateChanged", //$NON-NLS-1$
         "handle second view state change to " + Messages.QUOTE + selected //$NON-NLS-1$
             + Messages.QUOTE );
 
     if ( !selected )
     {
       EditorPanel selectedEditorPanelLeft = null;
       if ( this.jGTIMainSplitPane.getActiveEditor ().equals (
           ActiveEditor.LEFT_EDITOR ) )
       {
         selectedEditorPanelLeft = this.jGTIMainSplitPane
             .getJGTIEditorPanelTabbedPaneLeft ().getSelectedEditorPanel ();
       }
 
       EditorPanel selectedEditorPanelRight = this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPaneRight ().getSelectedEditorPanel ();
 
       int count = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
           .getComponentCount ();
       for ( int i = 0 ; i < count ; i++ )
       {
         EditorPanel editorPanel = this.jGTIMainSplitPane
             .getJGTIEditorPanelTabbedPaneRight ().getEditorPanel ( 0 );
         this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
             .removeEditorPanel ( editorPanel );
         this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
             .addEditorPanel ( editorPanel );
       }
 
       // set the selected editor panel
       if ( this.jGTIMainSplitPane.getActiveEditor ().equals (
           ActiveEditor.RIGHT_EDITOR ) )
       {
         if ( selectedEditorPanelRight != null )
         {
           this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
               .setSelectedEditorPanel ( selectedEditorPanelRight );
         }
         else
         {
           this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
         }
       }
 
       if ( selectedEditorPanelLeft != null )
       {
         this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
             .setSelectedEditorPanel ( selectedEditorPanelLeft );
       }
 
       handleTabbedPaneStateChanged ();
     }
 
     this.jGTIMainSplitPane.setSecondViewActive ( selected );
   }
 
 
   /**
    * Handles the tabbed pane mouse released event.
    * 
    * @param event The {@link MouseEvent}.
    */
   public final void handleTabbedPaneMouseReleased ( MouseEvent event )
   {
     if ( ! ( event.getSource () instanceof JGTIEditorPanelTabbedPane ) )
     {
       throw new IllegalArgumentException ( "unsupported source" ); //$NON-NLS-1$
     }
 
     // second view
     handleSecondViewMouseReleased ( event );
 
     int tabIndex = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getUI ().tabForCoordinate (
             this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane (),
             event.getX (), event.getY () );
 
     if ( ( event.getButton () == MouseEvent.BUTTON1 )
         && ( event.getClickCount () >= 2 ) && ( tabIndex == -1 ) )
     {
       handleNew ();
     }
     else if ( event.getButton () == MouseEvent.BUTTON3 )
     {
       TabPopupMenu popupMenu;
       if ( tabIndex == -1 )
       {
         popupMenu = new TabPopupMenu ( this, TabPopupMenuType.TAB_DEACTIVE,
             this.jGTIMainSplitPane.getActiveEditor () );
       }
       else
       {
         popupMenu = new TabPopupMenu ( this, TabPopupMenuType.TAB_ACTIVE,
             this.jGTIMainSplitPane.getActiveEditor () );
       }
       popupMenu.show ( ( Component ) event.getSource (), event.getX (), event
           .getY () );
     }
   }
 
 
   /**
    * Handles the tabbed pane state changed event.
    */
   public final void handleTabbedPaneStateChanged ()
   {
     handleTabbedPaneStateChanged ( null );
   }
 
 
   /**
    * Handles the tabbed pane state changed event.
    * 
    * @param event The {@link ChangeEvent}.
    */
   public final void handleTabbedPaneStateChanged ( ChangeEvent event )
   {
     logger.debug ( "handleTabbedPaneStateChanged", //$NON-NLS-1$
         "handle tabbed pane state changed" ); //$NON-NLS-1$
 
     // needed because of the drag and drop of editor tabs
     if ( event != null )
     {
       if ( event.getSource () == this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPaneLeft () )
       {
         this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
       }
       else if ( event.getSource () == this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPaneRight () )
       {
         this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
       }
       else
       {
         throw new RuntimeException ( "unsupported source" ); //$NON-NLS-1$
       }
     }
 
     // stop the auto step
     removeButtonState ( ButtonState.SELECTED_AUTO_STEP );
     for ( EditorPanel current : this.jGTIMainSplitPane )
     {
       if ( current instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) current;
         machinePanel.cancelAutoStepTimer ();
       }
     }
 
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
 
     // no panel
     if ( panel == null )
     {
       // no open editor panel
       if ( this.jGTIMainSplitPane.getEditorPanelCount () == 0 )
       {
         removeButtonState ( ButtonState.ENABLED_CLOSE_ALL );
         removeButtonState ( ButtonState.ENABLED_SAVE_ALL );
       }
 
       removeButtonState ( ButtonState.ENABLED_SAVE_AS );
       removeButtonState ( ButtonState.ENABLED_CLOSE );
       removeButtonState ( ButtonState.ENABLED_PRINT );
       removeButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
       removeButtonState ( ButtonState.ENABLED_VALIDATE );
       removeButtonState ( ButtonState.ENABLED_CONSOLE_TABLE );
       removeButtonState ( ButtonState.ENABLED_MACHINE_TABLE );
       removeButtonState ( ButtonState.ENABLED_ENTER_WORD );
       removeButtonState ( ButtonState.ENABLED_EDIT_MACHINE );
       removeButtonState ( ButtonState.ENABLED_UNDO );
       removeButtonState ( ButtonState.ENABLED_REDO );
       removeButtonState ( ButtonState.ENABLED_AUTO_LAYOUT );
       removeButtonState ( ButtonState.VISIBLE_MACHINE );
       removeButtonState ( ButtonState.VISIBLE_GRAMMAR );
       removeButtonState ( ButtonState.VISIBLE_REGEX );
       removeButtonState ( ButtonState.ENABLED_CONVERT_TO );
       removeButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE );
       removeButtonState ( ButtonState.ENABLED_DRAFT_FOR );
       removeButtonState ( ButtonState.ENABLED_HISTORY );
       removeButtonState ( ButtonState.ENABLED_MINIMIZE );
       removeButtonState ( ButtonState.ENABLED_REACHABLE_STATES );
       removeButtonState ( ButtonState.ENABLED_EXPORT_PICTURE );
       removeButtonState ( ButtonState.ENABLED_TO_LATEX );
       removeButtonState ( ButtonState.ENABLED_REORDER_STATE_NAMES );
       removeButtonState ( ButtonState.ENABLED_SAVE );
     }
     // MachinePanel
     else
     {
       if ( panel instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
 
         addButtonState ( ButtonState.VISIBLE_MACHINE );
         removeButtonState ( ButtonState.VISIBLE_GRAMMAR );
         removeButtonState ( ButtonState.VISIBLE_REGEX );
         removeButtonState ( ButtonState.ENABLED_TO_LATEX );
         removeButtonState ( ButtonState.ENABLED_TO_CORE_SYNTAX );
         removeButtonState ( ButtonState.ENABLED_ELIMINATE_LEFT_RECURSION );
         removeButtonState ( ButtonState.ENABLED_ELIMINATE_ENTITY_PRODUCTIONS );
         removeButtonState ( ButtonState.ENABLED_ELIMINATE_EPSILON_PRODUCTIONS );
         removeButtonState ( ButtonState.ENABLED_LEFT_FACTORING );
         removeButtonState ( ButtonState.ENABLED_CREATE_RDP );
         removeButtonState ( ButtonState.ENABLED_REGEX_INFO );
 
         if ( machinePanel.getMachine ().getMachineType ().equals (
             MachineType.DFA ) )
         {
           addButtonState ( ButtonState.ENABLED_CONVERT_TO_SOURCE_DFA );
           addButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_DFA );
           if ( machinePanel.getMachineMode ()
               .equals ( MachineMode.EDIT_MACHINE ) )
           {
             addButtonState ( ButtonState.ENABLED_MINIMIZE );
             addButtonState ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX );
           }
           else
           {
             removeButtonState ( ButtonState.ENABLED_MINIMIZE );
             removeButtonState ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX );
           }
         }
         else if ( machinePanel.getMachine ().getMachineType ().equals (
             MachineType.NFA ) )
         {
           addButtonState ( ButtonState.ENABLED_CONVERT_TO_SOURCE_NFA );
           addButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_NFA );
           removeButtonState ( ButtonState.ENABLED_MINIMIZE );
           removeButtonState ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX );
         }
         else if ( machinePanel.getMachine ().getMachineType ().equals (
             MachineType.ENFA ) )
         {
           addButtonState ( ButtonState.ENABLED_CONVERT_TO_SOURCE_ENFA );
           addButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_ENFA );
           removeButtonState ( ButtonState.ENABLED_MINIMIZE );
           removeButtonState ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX );
         }
         else if ( machinePanel.getMachine ().getMachineType ().equals (
             MachineType.PDA ) )
         {
           addButtonState ( ButtonState.ENABLED_CONVERT_TO_SOURCE_PDA );
           addButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_PDA );
           removeButtonState ( ButtonState.ENABLED_MINIMIZE );
           removeButtonState ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX );
         }
         else
         {
           throw new RuntimeException ( "unsupported machine type" ); //$NON-NLS-1$
         }
 
         machinePanel.setVisibleConsole ( this.gui
             .getJCheckBoxMenuItemConsole ().isSelected ()
             && machinePanel.getMachineMode ()
                 .equals ( MachineMode.EDIT_MACHINE ) );
         machinePanel.setVisibleTable ( this.gui.getJCheckBoxMenuItemTable ()
             .isSelected () );
 
         addButtonState ( ButtonState.ENABLED_SAVE_AS );
         addButtonState ( ButtonState.ENABLED_SAVE_ALL );
         addButtonState ( ButtonState.ENABLED_CLOSE );
         addButtonState ( ButtonState.ENABLED_CLOSE_ALL );
         addButtonState ( ButtonState.ENABLED_PRINT );
         addButtonState ( ButtonState.ENABLED_DRAFT_FOR_MACHINE );
         addButtonState ( ButtonState.ENABLED_MACHINE_TABLE );
         addButtonState ( ButtonState.ENABLED_EXPORT_PICTURE );
 
         // word navigation mode
         if ( machinePanel.getMachineMode ().equals (
             MachineMode.WORD_NAVIGATION ) )
         {
           addButtonState ( ButtonState.ENABLED_HISTORY );
           addButtonState ( ButtonState.ENABLED_EDIT_MACHINE );
 
           removeButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
           removeButtonState ( ButtonState.ENABLED_MACHINE_EDIT_ITEMS );
           removeButtonState ( ButtonState.ENABLED_CONSOLE_TABLE );
           removeButtonState ( ButtonState.ENABLED_VALIDATE );
           removeButtonState ( ButtonState.ENABLED_ENTER_WORD );
           removeButtonState ( ButtonState.ENABLED_AUTO_LAYOUT );
           removeButtonState ( ButtonState.ENABLED_CONVERT_TO );
           removeButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE );
           removeButtonState ( ButtonState.ENABLED_DRAFT_FOR );
           removeButtonState ( ButtonState.ENABLED_REACHABLE_STATES );
           removeButtonState ( ButtonState.ENABLED_REORDER_STATE_NAMES );
           removeButtonState ( ButtonState.ENABLED_UNDO );
           removeButtonState ( ButtonState.ENABLED_REDO );
 
           addButtonState ( ButtonState.SELECTED_ENTER_WORD );
 
           updateWordNavigationStates ();
         }
         // word enter mode
         else if ( machinePanel.getMachineMode ().equals (
             MachineMode.ENTER_WORD ) )
         {
           addButtonState ( ButtonState.ENABLED_EDIT_MACHINE );
 
           removeButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
           removeButtonState ( ButtonState.ENABLED_HISTORY );
           removeButtonState ( ButtonState.ENABLED_MACHINE_EDIT_ITEMS );
           removeButtonState ( ButtonState.ENABLED_CONSOLE_TABLE );
           removeButtonState ( ButtonState.ENABLED_VALIDATE );
           removeButtonState ( ButtonState.ENABLED_ENTER_WORD );
           removeButtonState ( ButtonState.ENABLED_AUTO_LAYOUT );
           removeButtonState ( ButtonState.ENABLED_CONVERT_TO );
           removeButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE );
           removeButtonState ( ButtonState.ENABLED_DRAFT_FOR );
           removeButtonState ( ButtonState.ENABLED_REACHABLE_STATES );
           removeButtonState ( ButtonState.ENABLED_REORDER_STATE_NAMES );
           removeButtonState ( ButtonState.ENABLED_UNDO );
           removeButtonState ( ButtonState.ENABLED_REDO );
 
           addButtonState ( ButtonState.SELECTED_ENTER_WORD );
           addButtonState ( ButtonState.ENABLED_NAVIGATION_START );
         }
         // edit machine mode
         else if ( machinePanel.getMachineMode ().equals (
             MachineMode.EDIT_MACHINE ) )
         {
           removeButtonState ( ButtonState.ENABLED_HISTORY );
           removeButtonState ( ButtonState.ENABLED_EDIT_MACHINE );
           removeButtonState ( ButtonState.SELECTED_ENTER_WORD );
 
           addButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
           addButtonState ( ButtonState.ENABLED_MACHINE_EDIT_ITEMS );
           addButtonState ( ButtonState.ENABLED_CONSOLE_TABLE );
           addButtonState ( ButtonState.ENABLED_VALIDATE );
           addButtonState ( ButtonState.ENABLED_ENTER_WORD );
           addButtonState ( ButtonState.ENABLED_AUTO_LAYOUT );
           addButtonState ( ButtonState.ENABLED_CONVERT_TO );
           addButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE );
           addButtonState ( ButtonState.ENABLED_DRAFT_FOR );
           addButtonState ( ButtonState.ENABLED_REACHABLE_STATES );
           addButtonState ( ButtonState.ENABLED_REORDER_STATE_NAMES );
 
           addButtonState ( ButtonState.ENABLED_NAVIGATION_DEACTIVE );
 
           if ( machinePanel.isUndoAble () )
           {
             addButtonState ( ButtonState.ENABLED_UNDO );
           }
           else
           {
             removeButtonState ( ButtonState.ENABLED_UNDO );
           }
 
           if ( machinePanel.isRedoAble () )
           {
             addButtonState ( ButtonState.ENABLED_REDO );
           }
           else
           {
             removeButtonState ( ButtonState.ENABLED_REDO );
           }
         }
         else
         {
           throw new RuntimeException ( "unsupported machine mode" ); //$NON-NLS-1$
         }
       }
       // GrammarPanel
       else if ( panel instanceof GrammarPanel )
       {
         GrammarPanel grammarPanel = ( GrammarPanel ) panel;
 
         if ( grammarPanel.getGrammar ().getGrammarType ().equals (
             GrammarType.RG ) )
         {
           addButtonState ( ButtonState.ENABLED_CONVERT_TO_SOURCE_RG );
           addButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_RG );
           removeButtonState ( ButtonState.ENABLED_ELIMINATE_LEFT_RECURSION );
           removeButtonState ( ButtonState.ENABLED_ELIMINATE_ENTITY_PRODUCTIONS );
           removeButtonState ( ButtonState.ENABLED_ELIMINATE_EPSILON_PRODUCTIONS );
           removeButtonState ( ButtonState.ENABLED_LEFT_FACTORING );
           removeButtonState ( ButtonState.ENABLED_CREATE_RDP );
         }
         else if ( grammarPanel.getGrammar ().getGrammarType ().equals (
             GrammarType.CFG ) )
         {
           addButtonState ( ButtonState.ENABLED_ELIMINATE_LEFT_RECURSION );
           addButtonState ( ButtonState.ENABLED_ELIMINATE_ENTITY_PRODUCTIONS );
           addButtonState ( ButtonState.ENABLED_ELIMINATE_EPSILON_PRODUCTIONS );
           addButtonState ( ButtonState.ENABLED_LEFT_FACTORING );
           addButtonState ( ButtonState.ENABLED_CREATE_RDP );
           addButtonState ( ButtonState.ENABLED_CONVERT_TO_SOURCE_CFG );
           addButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_CFG );
         }
         else
         {
           throw new RuntimeException ( "unsupported grammar type" ); //$NON-NLS-1$
         }
 
         panel.setVisibleConsole ( this.gui.getJCheckBoxMenuItemConsole ()
             .isSelected () );
 
         removeButtonState ( ButtonState.VISIBLE_MACHINE );
         addButtonState ( ButtonState.VISIBLE_GRAMMAR );
         removeButtonState ( ButtonState.VISIBLE_REGEX );
 
         addButtonState ( ButtonState.ENABLED_SAVE_AS );
         addButtonState ( ButtonState.ENABLED_SAVE_ALL );
         addButtonState ( ButtonState.ENABLED_CLOSE );
         addButtonState ( ButtonState.ENABLED_CLOSE_ALL );
         addButtonState ( ButtonState.ENABLED_PRINT );
         addButtonState ( ButtonState.ENABLED_CONVERT_TO );
         addButtonState ( ButtonState.ENABLED_DRAFT_FOR );
         addButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
         addButtonState ( ButtonState.ENABLED_DRAFT_FOR_GRAMMAR );
         addButtonState ( ButtonState.ENABLED_CONSOLE_TABLE );
 
         removeButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE );
         removeButtonState ( ButtonState.ENABLED_ENTER_WORD );
         removeButtonState ( ButtonState.ENABLED_EDIT_MACHINE );
         removeButtonState ( ButtonState.ENABLED_HISTORY );
         removeButtonState ( ButtonState.ENABLED_AUTO_LAYOUT );
         removeButtonState ( ButtonState.ENABLED_MINIMIZE );
         removeButtonState ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX );
         removeButtonState ( ButtonState.ENABLED_REACHABLE_STATES );
         removeButtonState ( ButtonState.ENABLED_EXPORT_PICTURE );
         removeButtonState ( ButtonState.ENABLED_REORDER_STATE_NAMES );
         removeButtonState ( ButtonState.ENABLED_MACHINE_TABLE );
         removeButtonState ( ButtonState.ENABLED_TO_LATEX );
         removeButtonState ( ButtonState.ENABLED_TO_CORE_SYNTAX );
         removeButtonState ( ButtonState.ENABLED_REGEX_INFO );
 
         if ( grammarPanel.isUndoAble () )
         {
           addButtonState ( ButtonState.ENABLED_UNDO );
         }
         else
         {
           removeButtonState ( ButtonState.ENABLED_UNDO );
         }
 
         if ( grammarPanel.isRedoAble () )
         {
           addButtonState ( ButtonState.ENABLED_REDO );
         }
         else
         {
           removeButtonState ( ButtonState.ENABLED_REDO );
         }
       }
       // RegexPanel
       else if ( panel instanceof RegexPanel )
       {
         RegexPanel regexPanel = ( RegexPanel ) panel;
 
         addButtonState ( ButtonState.ENABLED_CONVERT_TO_SOURCE_REGEX );
         addButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE_SOURCE_REGEX );
 
         panel.setVisibleConsole ( this.gui.getJCheckBoxMenuItemConsole ()
             .isSelected () );
         if ( regexPanel.getRegex ().getRegexNode () == null
             || regexPanel.getRegex ().getRegexNode ().isInCoreSyntax () )
         {
           removeButtonState ( ButtonState.ENABLED_TO_CORE_SYNTAX );
         }
         else
         {
           addButtonState ( ButtonState.ENABLED_TO_CORE_SYNTAX );
         }
         addButtonState ( ButtonState.ENABLED_REGEX_INFO );
 
         removeButtonState ( ButtonState.VISIBLE_MACHINE );
         removeButtonState ( ButtonState.VISIBLE_GRAMMAR );
         addButtonState ( ButtonState.VISIBLE_REGEX );
 
         addButtonState ( ButtonState.ENABLED_SAVE_AS );
         addButtonState ( ButtonState.ENABLED_SAVE_ALL );
         addButtonState ( ButtonState.ENABLED_CLOSE );
         addButtonState ( ButtonState.ENABLED_CLOSE_ALL );
         addButtonState ( ButtonState.ENABLED_PRINT );
         addButtonState ( ButtonState.ENABLED_CONVERT_TO );
         addButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
         addButtonState ( ButtonState.ENABLED_CONSOLE_TABLE );
         addButtonState ( ButtonState.ENABLED_TO_LATEX );
         addButtonState ( ButtonState.ENABLED_EXPORT_PICTURE );
 
         removeButtonState ( ButtonState.ENABLED_CONVERT_TO_COMPLETE );
         removeButtonState ( ButtonState.ENABLED_ENTER_WORD );
         removeButtonState ( ButtonState.ENABLED_EDIT_MACHINE );
         removeButtonState ( ButtonState.ENABLED_HISTORY );
         removeButtonState ( ButtonState.ENABLED_AUTO_LAYOUT );
         removeButtonState ( ButtonState.ENABLED_MINIMIZE );
         removeButtonState ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX );
         removeButtonState ( ButtonState.ENABLED_REACHABLE_STATES );
         removeButtonState ( ButtonState.ENABLED_REORDER_STATE_NAMES );
         removeButtonState ( ButtonState.ENABLED_MACHINE_TABLE );
         removeButtonState ( ButtonState.ENABLED_DRAFT_FOR );
         removeButtonState ( ButtonState.ENABLED_DRAFT_FOR_GRAMMAR );
         removeButtonState ( ButtonState.ENABLED_DRAFT_FOR_MACHINE );
         removeButtonState ( ButtonState.ENABLED_ELIMINATE_LEFT_RECURSION );
         removeButtonState ( ButtonState.ENABLED_ELIMINATE_EPSILON_PRODUCTIONS );
         removeButtonState ( ButtonState.ENABLED_ELIMINATE_ENTITY_PRODUCTIONS );
         removeButtonState ( ButtonState.ENABLED_LEFT_FACTORING );
         removeButtonState ( ButtonState.ENABLED_CREATE_RDP );
 
         if ( regexPanel.isUndoAble () )
         {
           addButtonState ( ButtonState.ENABLED_UNDO );
         }
         else
         {
           removeButtonState ( ButtonState.ENABLED_UNDO );
         }
 
         if ( regexPanel.isRedoAble () )
         {
           addButtonState ( ButtonState.ENABLED_REDO );
         }
         else
         {
           removeButtonState ( ButtonState.ENABLED_REDO );
         }
       }
       else
       {
         throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
       }
       // Save status
       if ( panel.isModified () )
       {
         addButtonState ( ButtonState.ENABLED_SAVE );
       }
       else
       {
         removeButtonState ( ButtonState.ENABLED_SAVE );
       }
     }
   }
 
 
   /**
    * Handles table state changes.
    */
   public final void handleTableStateChanged ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
 
     if ( panel instanceof MachinePanel )
     {
       MachinePanel machinePanel = ( MachinePanel ) panel;
       boolean state = this.gui.getJCheckBoxMenuItemTable ().isSelected ();
       if ( PreferenceManager.getInstance ().getVisibleTable () != state )
       {
         if ( state )
         {
           addButtonState ( ButtonState.SELECTED_MACHINE_TABLE );
         }
         else
         {
           removeButtonState ( ButtonState.SELECTED_MACHINE_TABLE );
         }
 
         PreferenceManager.getInstance ().setVisibleTable ( state );
         machinePanel.setVisibleTable ( state );
       }
     }
   }
 
 
   /**
    * Handles to core syntax for Regex
    */
   public final void handleToCoreSyntax ()
   {
     handleValidate ();
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel instanceof RegexPanel )
     {
       RegexPanel regexPanel = ( RegexPanel ) panel;
       regexPanel.handleToCoreSyntaxButtonClicked ();
     }
     else
     {
       throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * Handles elimination of left recursion in cfg.
    */
   public final void handleEliminateLeftRecursion ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel instanceof GrammarPanel )
     {
       ( ( GrammarPanel ) panel ).handleEliminateLeftRecursion ();
     }
   }
 
 
   /**
    * Handles elimination of left recursion in cfg.
    */
   public final void handleLeftFactoring ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel instanceof GrammarPanel )
     {
       ( ( GrammarPanel ) panel ).handleLeftFactoring ();
     }
   }
 
 
   /**
    * Handles elimination of left recursion in cfg.
    */
   public final void handleEliminateEntityProductions ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel instanceof GrammarPanel )
     {
       ( ( GrammarPanel ) panel ).handleEliminateEntityProductions ();
     }
   }
 
 
   /**
    * Handles elimination of left recursion in cfg.
    */
   public final void handleEliminateEpsilonProductions ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel instanceof GrammarPanel )
     {
       ( ( GrammarPanel ) panel ).handleEliminateEpsilonProductions ();
     }
   }
 
 
   /**
    * Saves the last divider location
    */
   private int lastDividerLocation = 0;
 
 
   /**
    * Handles regex information window state changed
    * 
    * @param b True if window should be shown, else false
    */
   public final void handleRegexInfoChanged ( boolean b )
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel instanceof RegexPanel )
     {
       RegexPanel regexPanel = ( RegexPanel ) panel;
 
       if ( !b )
       {
         this.lastDividerLocation = regexPanel.getGUI ().jGTISplitPaneRegex
             .getDividerLocation ();
       }
       regexPanel.getGUI ().jGTIPanelInfo.setVisible ( b );
       if ( regexPanel.getRegex ().getRegexNode () != null )
       {
         regexPanel.getRegex ().getRegexNode ().setShowPositions ( b );
         regexPanel.initializeJGraph ();
         regexPanel.getGUI ().jGTIScrollPaneGraph.setViewportView ( regexPanel
             .getJGTIGraph () );
         ( ( DefaultRegexModel ) regexPanel.getModel () ).createTree ();
       }
 
       if ( b )
       {
         regexPanel.getGUI ().jGTISplitPaneRegex
             .setDividerLocation ( this.lastDividerLocation );
       }
     }
     else
     {
       throw new RuntimeException ( "unsupported panel" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * Handle Toolbar Add State button value changed
    * 
    * @param state The new State of the Add State Toolbar button
    */
   public final void handleToolbarAddState ( boolean state )
   {
     for ( EditorPanel panel : this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPane () )
     {
       if ( panel instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
         machinePanel.handleToolbarAddState ( state );
       }
     }
   }
 
 
   /**
    * Handle Toolbar End button value changed
    * 
    * @param state The new State of the End Toolbar button
    */
   public final void handleToolbarEnd ( boolean state )
   {
     for ( EditorPanel panel : this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPane () )
     {
       if ( ( panel instanceof MachinePanel ) )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
         machinePanel.handleToolbarEnd ( state );
       }
     }
   }
 
 
   /**
    * Handle Toolbar Mouse button value changed
    * 
    * @param state The new State of the Mouse Toolbar button
    */
   public final void handleToolbarMouse ( boolean state )
   {
     for ( EditorPanel panel : this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPane () )
     {
       if ( ( panel instanceof MachinePanel ) )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
         machinePanel.handleToolbarMouse ( state );
       }
     }
   }
 
 
   /**
    * Handle Toolbar Start button value changed
    * 
    * @param state The new State of the Start Toolbar button
    */
   public final void handleToolbarStart ( boolean state )
   {
     for ( EditorPanel panel : this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPane () )
     {
       if ( ( panel instanceof MachinePanel ) )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
         machinePanel.handleToolbarStart ( state );
       }
     }
   }
 
 
   /**
    * Handle Toolbar Transition button value changed
    * 
    * @param state The new State of the Transition Toolbar button
    */
   public final void handleToolbarTransition ( boolean state )
   {
     for ( EditorPanel panel : this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPane () )
     {
       if ( ( panel instanceof MachinePanel ) )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
 
         machinePanel.handleToolbarTransition ( state );
       }
     }
   }
 
 
   /**
    * Handle undo button pressed
    */
   public final void handleUndo ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel != null )
     {
       panel.handleUndo ();
     }
   }
 
 
   /**
    * Handle the action event of the enter word item.
    */
   public final void handleValidate ()
   {
     handleValidate ( true );
   }
 
 
   /**
    * Handle the action event of the enter word item.
    * 
    * @param showDialogIfWarning If true the dialog is shown if there a no errors
    *          but warnings.
    * @return True if the validating finished without errors, otherwise false.
    */
   public final boolean handleValidate ( boolean showDialogIfWarning )
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
 
     int errorCount = 0;
     int warningCount = 0;
 
     if ( panel instanceof MachinePanel )
     {
       MachinePanel machinePanel = ( MachinePanel ) panel;
       try
       {
         panel.clearValidationMessages ();
         machinePanel.getMachine ().validate ();
       }
       catch ( MachineValidationException e )
       {
 
         for ( MachineException error : e.getMachineException () )
         {
           if ( error.getType ().equals ( ErrorType.ERROR ) )
           {
             machinePanel.addError ( error );
             errorCount++ ;
           }
           else if ( error.getType ().equals ( ErrorType.WARNING ) )
           {
             machinePanel.addWarning ( error );
             warningCount++ ;
           }
         }
       }
     }
     else if ( panel instanceof GrammarPanel )
     {
       GrammarPanel grammarPanel = ( GrammarPanel ) panel;
       try
       {
         panel.clearValidationMessages ();
         grammarPanel.getGrammar ().validate ();
       }
       catch ( GrammarValidationException e )
       {
 
         for ( GrammarException error : e.getGrammarException () )
         {
           if ( error.getType ().equals ( ErrorType.ERROR ) )
           {
             grammarPanel.addError ( error );
             errorCount++ ;
           }
           else if ( error.getType ().equals ( ErrorType.WARNING ) )
           {
             grammarPanel.addWarning ( error );
             warningCount++ ;
           }
         }
       }
     }
     else if ( panel instanceof RegexPanel )
     {
       panel.clearValidationMessages ();
       RegexPanel regexPanel = ( RegexPanel ) panel;
       try
       {
         regexPanel.validate ();
       }
       catch ( RegexValidationException e )
       {
         for ( RegexException error : e.getRegexException () )
         {
           if ( error.getType ().equals ( ErrorType.ERROR ) )
           {
             regexPanel.addError ( error );
             errorCount++ ;
           }
           else if ( error.getType ().equals ( ErrorType.WARNING ) )
           {
             regexPanel.addWarning ( error );
             warningCount++ ;
           }
         }
       }
     }
     else
     {
       throw new RuntimeException (
           "the select panel is not a regex, machine or grammar panel" ); //$NON-NLS-1$
     }
 
     String titleWarningString = ""; //$NON-NLS-1$
     String titleErrorString = ""; //$NON-NLS-1$
     String titleWarningFoundString = ""; //$NON-NLS-1$
     String titleErrorFoundString = ""; //$NON-NLS-1$
     String mwError = "MainWindow.Error"; //$NON-NLS-1$
     String mwWarning = "MainWindow.Warning"; //$NON-NLS-1$
     String mwErrorWarning = "MainWindow.ErrorWarning"; //$NON-NLS-1$
     String mwWarningCountN = "MainWindow."; //$NON-NLS-1$
     String mwWarningCount1 = "MainWindow."; //$NON-NLS-1$
     String mwErrorCountN = "MainWindow."; //$NON-NLS-1$
     String mwErrorCount1 = "MainWindow."; //$NON-NLS-1$
     String mwErrorWarningCount0 = "MainWindow."; //$NON-NLS-1$
     String mwErrorWarningCount1 = "MainWindow."; //$NON-NLS-1$
     String mwErrorWarningCount2 = "MainWindow."; //$NON-NLS-1$
     String mwErrorWarningCount3 = "MainWindow."; //$NON-NLS-1$
     String mwNoErrorNoWarning = "MainWindow."; //$NON-NLS-1$
     String mwNoErrorNoWarningCount = "MainWindow."; //$NON-NLS-1$
     if ( panel instanceof MachinePanel )
     {
       titleErrorString = "MachinePanel.Error"; //$NON-NLS-1$
       titleWarningString = "MachinePanel.Warning"; //$NON-NLS-1$
       titleErrorFoundString = "MachinePanel.ErrorFound"; //$NON-NLS-1$
       titleWarningFoundString = "MachinePanel.WarningFound"; //$NON-NLS-1$
       mwError += "Machine"; //$NON-NLS-1$
       mwWarning += "Machine"; //$NON-NLS-1$
       mwErrorCount1 += "ErrorMachineCountOne"; //$NON-NLS-1$
       mwErrorCountN += "ErrorMachineCount"; //$NON-NLS-1$
       mwWarningCount1 += "WarningMachineCountOne"; //$NON-NLS-1$
       mwWarningCountN += "WarningMachineCount"; //$NON-NLS-1$
       mwErrorWarning += "Machine"; //$NON-NLS-1$
       mwErrorWarningCount0 += "ErrorWarningMachineCount0"; //$NON-NLS-1$
       mwErrorWarningCount1 += "ErrorWarningMachineCount1"; //$NON-NLS-1$
       mwErrorWarningCount2 += "ErrorWarningMachineCount2"; //$NON-NLS-1$
       mwErrorWarningCount3 += "ErrorWarningMachineCount3"; //$NON-NLS-1$
       mwNoErrorNoWarning += "NoErrorNoWarningMachine"; //$NON-NLS-1$
       mwNoErrorNoWarningCount += "NoErrorNoWarningMachineCount"; //$NON-NLS-1$
     }
     else if ( panel instanceof GrammarPanel )
     {
       titleErrorString = "GrammarPanel.Error"; //$NON-NLS-1$
       titleWarningString = "GrammarPanel.Warning"; //$NON-NLS-1$
       titleErrorFoundString = "GrammarPanel.ErrorFound"; //$NON-NLS-1$
       titleWarningFoundString = "GrammarPanel.WarningFound"; //$NON-NLS-1$
       mwError += "Grammar"; //$NON-NLS-1$
       mwWarning += "Grammar"; //$NON-NLS-1$
       mwErrorCount1 += "ErrorGrammarCountOne"; //$NON-NLS-1$
       mwErrorCountN += "ErrorGrammarCount"; //$NON-NLS-1$
       mwWarningCount1 += "WarningGrammarCountOne"; //$NON-NLS-1$
       mwWarningCountN += "WarningGrammarCount"; //$NON-NLS-1$
       mwErrorWarning += "Grammar"; //$NON-NLS-1$
       mwErrorWarningCount0 += "ErrorWarningGrammarCount0"; //$NON-NLS-1$
       mwErrorWarningCount1 += "ErrorWarningGrammarCount1"; //$NON-NLS-1$
       mwErrorWarningCount2 += "ErrorWarningGrammarCount2"; //$NON-NLS-1$
       mwErrorWarningCount3 += "ErrorWarningGrammarCount3"; //$NON-NLS-1$
       mwNoErrorNoWarning += "NoErrorNoWarningGrammar"; //$NON-NLS-1$
       mwNoErrorNoWarningCount += "NoErrorNoWarningGrammarCount"; //$NON-NLS-1$
     }
     else if ( panel instanceof RegexPanel )
     {
       titleErrorString = "RegexPanel.Error"; //$NON-NLS-1$
       titleWarningString = "RegexPanel.Warning"; //$NON-NLS-1$
       titleErrorFoundString = "RegexPanel.ErrorFound"; //$NON-NLS-1$
       titleWarningFoundString = "RegexPanel.WarningFound"; //$NON-NLS-1$
       mwError += "Regex"; //$NON-NLS-1$
       mwWarning += "Regex"; //$NON-NLS-1$
       mwErrorCount1 += "ErrorRegexCountOne"; //$NON-NLS-1$
       mwErrorCountN += "ErrorRegexCount"; //$NON-NLS-1$
       mwWarningCount1 += "WarningRegexCountOne"; //$NON-NLS-1$
       mwWarningCountN += "WarningRegexCount"; //$NON-NLS-1$
       mwErrorWarning += "Regex"; //$NON-NLS-1$
       mwErrorWarningCount0 += "ErrorWarningRegexCount0"; //$NON-NLS-1$
       mwErrorWarningCount1 += "ErrorWarningRegexCount1"; //$NON-NLS-1$
       mwErrorWarningCount2 += "ErrorWarningRegexCount2"; //$NON-NLS-1$
       mwErrorWarningCount3 += "ErrorWarningRegexCount3"; //$NON-NLS-1$
       mwNoErrorNoWarning += "NoErrorNoWarningRegex"; //$NON-NLS-1$
       mwNoErrorNoWarningCount += "NoErrorNoWarningRegexCount"; //$NON-NLS-1$
     }
 
     // Return if only errors should be displayes
     if ( !showDialogIfWarning && ( errorCount == 0 ) )
     {
       if ( warningCount > 0 )
       {
         // Select the warning tab
         panel.getJTabbedPaneConsole ().setSelectedIndex ( 1 );
 
         // Update the titles
         panel.getJTabbedPaneConsole ().setTitleAt ( 0,
             Messages.getString ( titleErrorString ) );
 
         panel.getJTabbedPaneConsole ().setTitleAt (
             1,
             Messages.getString ( titleWarningFoundString, new Integer (
                 warningCount ) ) );
       }
       return true;
     }
 
     InfoDialog infoDialog = null;
     // Error and warning
     if ( ( errorCount > 0 ) && ( warningCount > 0 ) )
     {
       String message = null;
       if ( ( errorCount == 1 ) && ( warningCount == 1 ) )
       {
         message = Messages.getString ( mwErrorWarningCount0 );
       }
       else if ( ( errorCount == 1 ) && ( warningCount > 1 ) )
       {
         message = Messages.getString ( mwErrorWarningCount1, String
             .valueOf ( warningCount ) );
       }
       else if ( ( errorCount > 1 ) && ( warningCount == 1 ) )
       {
         message = Messages.getString ( mwErrorWarningCount2, String
             .valueOf ( errorCount ) );
       }
       else
       {
         message = Messages.getString ( mwErrorWarningCount3, String
             .valueOf ( errorCount ), String.valueOf ( warningCount ) );
       }
 
       // Update the titles
       panel.getJTabbedPaneConsole ().setTitleAt (
           0,
           Messages
               .getString ( titleErrorFoundString, new Integer ( errorCount ) ) );
       panel.getJTabbedPaneConsole ().setTitleAt (
           1,
           Messages.getString ( titleWarningFoundString, new Integer (
               warningCount ) ) );
 
       // Select the error tab
       panel.getJTabbedPaneConsole ().setSelectedIndex ( 0 );
 
       infoDialog = new InfoDialog ( this.gui, message, Messages
           .getString ( mwErrorWarning ) );
     }
     // Only error
     else if ( errorCount > 0 )
     {
       String message;
       if ( errorCount == 1 )
       {
         message = Messages.getString ( mwErrorCount1 );
       }
       else
       {
         message = Messages.getString ( mwErrorCountN, String
             .valueOf ( errorCount ) );
       }
 
       // Update the titles
       panel.getJTabbedPaneConsole ().setTitleAt (
           0,
           Messages
               .getString ( titleErrorFoundString, new Integer ( errorCount ) ) );
       panel.getJTabbedPaneConsole ().setTitleAt ( 1,
           Messages.getString ( titleWarningString ) );
 
       panel.getJTabbedPaneConsole ().setSelectedIndex ( 0 );
 
       infoDialog = new InfoDialog ( this.gui, message, Messages
           .getString ( mwError ) );
     }
     // Only warning
     else if ( warningCount > 0 )
     {
       String message;
       if ( warningCount == 1 )
       {
         message = Messages.getString ( mwWarningCount1 );
       }
       else
       {
         message = Messages.getString ( mwWarningCountN, String
             .valueOf ( warningCount ) );
       }
 
       // Update the titles
       panel.getJTabbedPaneConsole ().setTitleAt ( 0,
           Messages.getString ( titleErrorString ) );
       panel.getJTabbedPaneConsole ().setTitleAt (
           1,
           Messages.getString ( titleWarningFoundString, new Integer (
               warningCount ) ) );
 
       // Select the warning tab
       panel.getJTabbedPaneConsole ().setSelectedIndex ( 1 );
 
       infoDialog = new InfoDialog ( this.gui, message, Messages
           .getString ( mwWarning ) );
     }
     // No error and no warning
     else
     {
       panel.getJTabbedPaneConsole ().setTitleAt ( 0,
           Messages.getString ( titleErrorString ) );
       panel.getJTabbedPaneConsole ().setTitleAt ( 1,
           Messages.getString ( titleWarningString ) );
       infoDialog = new InfoDialog ( this.gui, Messages
           .getString ( mwNoErrorNoWarningCount ), Messages
           .getString ( mwNoErrorNoWarning ) );
     }
     addButtonState ( ButtonState.SELECTED_CONSOLE_TABLE );
     panel.setVisibleConsole ( true );
 
     infoDialog.show ();
     return false;
   }
 
 
   /**
    * Handles the word auto step action in the word enter mode.
    * 
    * @param event The {@link ItemEvent}.
    */
   public final void handleWordAutoStep ( ItemEvent event )
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( panel instanceof MachinePanel )
     {
       MachinePanel machinePanel = ( MachinePanel ) panel;
       machinePanel.handleWordAutoStep ( event );
     }
   }
 
 
   /**
    * Handles the word next step action in the word enter mode.
    */
   public final void handleWordNextStep ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
 
     MachinePanel machinePanel = ( MachinePanel ) panel;
     machinePanel.handleWordNextStep ();
   }
 
 
   /**
    * Handles the word previous step action in the word enter mode.
    */
   public final void handleWordPreviousStep ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
 
     MachinePanel machinePanel = ( MachinePanel ) panel;
     machinePanel.handleWordPreviousStep ();
   }
 
 
   /**
    * Handles the word start action in the word enter mode.
    */
   public final void handleWordStart ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
     MachinePanel machinePanel = ( MachinePanel ) panel;
 
     if ( machinePanel.handleWordStart () )
     {
       addButtonState ( ButtonState.ENABLED_HISTORY );
     }
   }
 
 
   /**
    * Handles the word stop action in the word enter mode.
    */
   public final void handleWordStop ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
     MachinePanel machinePanel = ( MachinePanel ) panel;
 
     addButtonState ( ButtonState.ENABLED_NAVIGATION_START );
     removeButtonState ( ButtonState.ENABLED_HISTORY );
 
     machinePanel.handleWordStop ();
   }
 
 
   /**
    * Returns the close enabled state.
    * 
    * @return The close enabled state.
    */
   public final boolean isEnabledClose ()
   {
     return this.gui.getJMenuItemClose ().isEnabled ();
   }
 
 
   /**
    * Returns the close all enabled state.
    * 
    * @return The close all enabled state.
    */
   public final boolean isEnabledCloseAll ()
   {
     return this.gui.getJMenuItemCloseAll ().isEnabled ();
   }
 
 
   /**
    * Returns the new enabled state.
    * 
    * @return The new enabled state.
    */
   public final boolean isEnabledNew ()
   {
     return this.gui.getJMenuItemNew ().isEnabled ();
   }
 
 
   /**
    * Returns the open enabled state.
    * 
    * @return The open enabled state.
    */
   public final boolean isEnabledOpen ()
   {
     return this.gui.getJMenuItemNew ().isEnabled ();
   }
 
 
   /**
    * Returns the save enabled state.
    * 
    * @return The save enabled state.
    */
   public final boolean isEnabledSave ()
   {
     return this.gui.getJMenuItemSave ().isEnabled ();
   }
 
 
   /**
    * Returns the save as enabled state.
    * 
    * @return The save as enabled state.
    */
   public final boolean isEnabledSaveAs ()
   {
     return this.gui.getJMenuItemSaveAs ().isEnabled ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see LanguageChangedListener#languageChanged()
    */
   public final void languageChanged ()
   {
     /*
      * File
      */
     MainWindow.this.gui.getJMenuFile ().setText (
         Messages.getString ( "MainWindow.File" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuFile ().setMnemonic (
         Messages.getString ( "MainWindow.FileMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // New
     MainWindow.this.gui.getJMenuItemNew ().setText (
         Messages.getString ( "MainWindow.New" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemNew ().setMnemonic (
         Messages.getString ( "MainWindow.NewMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJGTIToolBarButtonNew ().setToolTipText (
         Messages.getString ( "MainWindow.NewToolTip" ) ); //$NON-NLS-1$
     // Open
     MainWindow.this.gui.getJMenuItemOpen ().setText (
         Messages.getString ( "MainWindow.Open" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemOpen ().setMnemonic (
         Messages.getString ( "MainWindow.OpenMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJGTIToolBarButtonOpen ().setToolTipText (
         Messages.getString ( "MainWindow.OpenToolTip" ) ); //$NON-NLS-1$
     // Close
     MainWindow.this.gui.getJMenuItemClose ().setText (
         Messages.getString ( "MainWindow.Close" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemClose ().setMnemonic (
         Messages.getString ( "MainWindow.CloseMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // CloseAll
     MainWindow.this.gui.getJMenuItemCloseAll ().setText (
         Messages.getString ( "MainWindow.CloseAll" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemCloseAll ().setMnemonic (
         Messages.getString ( "MainWindow.CloseAllMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Print
     MainWindow.this.gui.getJMenuItemPrint ().setText (
         Messages.getString ( "MainWindow.Print" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemPrint ().setMnemonic (
         Messages.getString ( "MainWindow.PrintMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Save
     MainWindow.this.gui.getJMenuItemSave ().setText (
         Messages.getString ( "MainWindow.Save" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemSave ().setMnemonic (
         Messages.getString ( "MainWindow.SaveMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJGTIToolBarButtonSave ().setToolTipText (
         Messages.getString ( "MainWindow.SaveToolTip" ) ); //$NON-NLS-1$
     // SaveAs
     MainWindow.this.gui.getJMenuItemSaveAs ().setText (
         Messages.getString ( "MainWindow.SaveAs" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemSaveAs ().setMnemonic (
         Messages.getString ( "MainWindow.SaveAsMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJGTIToolBarButtonSaveAs ().setToolTipText (
         Messages.getString ( "MainWindow.SaveAsToolTip" ) ); //$NON-NLS-1$
     // SaveAll
     MainWindow.this.gui.getJMenuItemSaveAll ().setText (
         Messages.getString ( "MainWindow.SaveAll" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemSaveAll ().setMnemonic (
         Messages.getString ( "MainWindow.SaveAllMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // ExportPicture
     MainWindow.this.gui.getJMenuItemExportPicture ().setText (
         Messages.getString ( "MainWindow.ExportPicture" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemExportPicture ().setMnemonic (
         Messages.getString ( "MainWindow.ExportPictureMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // ExportLatex
     MainWindow.this.gui.getJMenuItemExportLatex ().setText (
         Messages.getString ( "MainWindow.LatexExport" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemExportLatex ().setMnemonic (
         Messages.getString ( "MainWindow.LatexExportMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // RecentlyUsed
     MainWindow.this.gui.getJMenuRecentlyUsed ().setText (
         Messages.getString ( "MainWindow.RecentlyUsed" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuRecentlyUsed ().setMnemonic (
         Messages.getString ( "MainWindow.RecentlyUsedMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Quit
     MainWindow.this.gui.getJMenuItemQuit ().setText (
         Messages.getString ( "MainWindow.Quit" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemQuit ().setMnemonic (
         Messages.getString ( "MainWindow.QuitMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
 
     /*
      * Edit
      */
     MainWindow.this.gui.getJMenuEdit ().setText (
         Messages.getString ( "MainWindow.Edit" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuEdit ().setMnemonic (
         Messages.getString ( "MainWindow.EditMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Undo
     MainWindow.this.gui.getJMenuItemUndo ().setText (
         Messages.getString ( "MainWindow.Undo" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemUndo ().setMnemonic (
         Messages.getString ( "MainWindow.UndoMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJGTIToolBarButtonUndo ().setToolTipText (
         Messages.getString ( "MainWindow.UndoToolTip" ) ); //$NON-NLS-1$
     // Redo
     MainWindow.this.gui.getJMenuItemRedo ().setText (
         Messages.getString ( "MainWindow.Redo" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemRedo ().setMnemonic (
         Messages.getString ( "MainWindow.RedoMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJGTIToolBarButtonRedo ().setToolTipText (
         Messages.getString ( "MainWindow.RedoToolTip" ) ); //$NON-NLS-1$
     // Preferences
     MainWindow.this.gui.getJMenuItemPreferences ().setText (
         Messages.getString ( "MainWindow.Preferences" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemPreferences ().setMnemonic (
         Messages.getString ( "MainWindow.PreferencesMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
 
     /*
      * View
      */
     MainWindow.this.gui.getJMenuView ().setText (
         Messages.getString ( "MainWindow.View" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuView ().setMnemonic (
         Messages.getString ( "MainWindow.ViewMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Console
     MainWindow.this.gui.getJCheckBoxMenuItemConsole ().setText (
         Messages.getString ( "MainWindow.Console" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJCheckBoxMenuItemConsole ().setMnemonic (
         Messages.getString ( "MainWindow.ConsoleMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Table
     MainWindow.this.gui.getJCheckBoxMenuItemTable ().setText (
         Messages.getString ( "MainWindow.Table" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJCheckBoxMenuItemTable ().setMnemonic (
         Messages.getString ( "MainWindow.TableMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // SecondView
     MainWindow.this.gui.getJCheckBoxMenuItemSecondView ().setText (
         Messages.getString ( "MainWindow.SecondView" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJCheckBoxMenuItemSecondView ().setMnemonic (
         Messages.getString ( "MainWindow.SecondViewMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Regex info
     MainWindow.this.gui.getJCheckBoxMenuItemRegexInfo ().setText (
         Messages.getString ( "MainWindow.RegexInfo" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJCheckBoxMenuItemRegexInfo ().setMnemonic (
         Messages.getString ( "MainWindow.RegexInfoMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
 
     /*
      * Execute
      */
     MainWindow.this.gui.getJMenuExecute ().setText (
         Messages.getString ( "MainWindow.Execute" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuExecute ().setMnemonic (
         Messages.getString ( "MainWindow.ExecuteMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // EnterWord
     MainWindow.this.gui.getJMenuItemEnterWord ().setText (
         Messages.getString ( "MainWindow.EnterWord" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemEnterWord ().setMnemonic (
         Messages.getString ( "MainWindow.EnterWordMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // EditMachine
     MainWindow.this.gui.getJMenuItemEditMachine ().setText (
         Messages.getString ( "MainWindow.EditMachine" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemEditMachine ().setMnemonic (
         Messages.getString ( "MainWindow.EditMachineMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Validate
     MainWindow.this.gui.getJMenuItemValidate ().setText (
         Messages.getString ( "MainWindow.Validate" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemValidate ().setMnemonic (
         Messages.getString ( "MainWindow.ValidateMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // ConvertTo
     MainWindow.this.gui.getJMenuConvertTo ().setText (
         Messages.getString ( "MainWindow.ConvertTo" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuConvertTo ().setMnemonic (
         Messages.getString ( "MainWindow.ConvertToMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemConvertToDFA ().setText (
         Messages.getString ( "MainWindow.DFA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemConvertToNFA ().setText (
         Messages.getString ( "MainWindow.NFA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemConvertToNFACB ().setText (
         Messages.getString ( "MainWindow.NFACB" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemConvertToENFA ().setText (
         Messages.getString ( "MainWindow.ENFA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemConvertToPDA ().setText (
         Messages.getString ( "MainWindow.PDA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemConvertToRegex ().setText (
         Messages.getString ( "MainWindow.REGEX" ) ); //$NON-NLS-1$
     // ConvertToComplete
     MainWindow.this.gui.getJMenuConvertToComplete ().setText (
         Messages.getString ( "MainWindow.ConvertToComplete" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuConvertToComplete ().setMnemonic (
         Messages
             .getString ( "MainWindow.ConvertToCompleteMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemConvertToCompleteDFA ().setText (
         Messages.getString ( "MainWindow.DFA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemConvertToCompleteNFA ().setText (
         Messages.getString ( "MainWindow.NFA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemConvertToCompleteENFA ().setText (
         Messages.getString ( "MainWindow.ENFA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemConvertToCompletePDA ().setText (
         Messages.getString ( "MainWindow.PDA" ) ); //$NON-NLS-1$
     // Draft for
     MainWindow.this.gui.getJMenuDraft ().setText (
         Messages.getString ( "MainWindow.DraftFor" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuDraft ().setMnemonic (
         Messages.getString ( "MainWindow.DraftForMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemDFA ().setText (
         Messages.getString ( "MainWindow.DFA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemNFA ().setText (
         Messages.getString ( "MainWindow.NFA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemENFA ().setText (
         Messages.getString ( "MainWindow.ENFA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemPDA ().setText (
         Messages.getString ( "MainWindow.PDA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemCFG ().setText (
         Messages.getString ( "MainWindow.CFG" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemRG ().setText (
         Messages.getString ( "MainWindow.RG" ) ); //$NON-NLS-1$
     // AutoLayout
     MainWindow.this.gui.getJMenuItemAutoLayout ().setText (
         Messages.getString ( "MainWindow.AutoLayout" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemAutoLayout ().setMnemonic (
         Messages.getString ( "MainWindow.AutoLayoutMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Minimize
     MainWindow.this.gui.getJMenuItemMinimize ().setText (
         Messages.getString ( "MainWindow.Minimize" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemMinimize ().setMnemonic (
         Messages.getString ( "MainWindow.MinimizeMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // To Core Syntax
     MainWindow.this.gui.getJMenuItemToCoreSyntax ().setText (
         Messages.getString ( "MainWindow.ToCoreSyntax" ) );//$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemToCoreSyntax ().setMnemonic (
         Messages.getString ( "MainWindow.ToCoreSyntaxMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Eliminate left recursion
     MainWindow.this.gui.getJMenuItemEliminateLeftRecursion ().setText (
         Messages.getString ( "MainWindow.EliminateLeftRecursion" ) );//$NON-NLS-1$
     MainWindow.this.gui
         .getJMenuItemEliminateLeftRecursion ()
         .setMnemonic (
             Messages
                 .getString ( "MainWindow.EliminateLeftRecursionMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Eliminate entity productions
     MainWindow.this.gui.getJMenuItemEliminateEntityProductions ().setText (
         Messages.getString ( "MainWindow.EliminateEntityProductions" ) );//$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemEliminateEntityProductions ()
         .setMnemonic (
             Messages.getString (
                 "MainWindow.EliminateEntityProductionsMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Eliminate epsilon productions
     MainWindow.this.gui.getJMenuItemEliminateEpsilonProductions ().setText (
         Messages.getString ( "MainWindow.EliminateEpsilonProductions" ) );//$NON-NLS-1$
     MainWindow.this.gui
         .getJMenuItemEliminateEpsilonProductions ()
         .setMnemonic (
             Messages.getString (
                 "MainWindow.EliminateEpsilonProductionsMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // left factoring
     MainWindow.this.gui.getJMenuItemLeftfactoring ().setText (
         Messages.getString ( "MainWindow.LeftFactoring" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemLeftfactoring ().setMnemonic (
         Messages.getString ( "MainWindow.LeftFactoringMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // create rdp
     MainWindow.this.gui.getJMenuItemCreateRDP ().setText (
         Messages.getString ( "MainWindow.CreateRDP" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemCreateRDP ().setMnemonic (
         Messages.getString ( "MainWindow.CreateRDPMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     /*
      * Extras
      */
     MainWindow.this.gui.getJMenuExtras ().setText (
         Messages.getString ( "MainWindow.Extras" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuExtras ().setMnemonic (
         Messages.getString ( "MainWindow.ExtrasMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Exchange
     MainWindow.this.gui.getJMenuItemExchange ().setText (
         Messages.getString ( "MainWindow.Exchange" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemExchange ().setMnemonic (
         Messages.getString ( "MainWindow.ExchangeMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // History
     MainWindow.this.gui.getJMenuItemHistory ().setText (
         Messages.getString ( "MainWindow.History" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemHistory ().setMnemonic (
         Messages.getString ( "MainWindow.HistoryMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // ReachableStates
     MainWindow.this.gui.getJMenuItemReachableStates ().setText (
         Messages.getString ( "MainWindow.ReachableStates" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemReachableStates ().setMnemonic (
         Messages.getString ( "MainWindow.ReachableStatesMnemonic" ).charAt ( //$NON-NLS-1$
             0 ) );
     // ReorderStateNames
     MainWindow.this.gui.getJMenuItemReorderStateNames ().setText (
         Messages.getString ( "MainWindow.ReorderStateNames" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemReorderStateNames ().setMnemonic (
         Messages.getString ( "MainWindow.ReorderStateNamesMnemonic" ).charAt ( //$NON-NLS-1$
             0 ) );
 
     /*
      * Help
      */
     MainWindow.this.gui.getJMenuHelp ().setText (
         Messages.getString ( "MainWindow.Help" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuHelp ().setMnemonic (
         Messages.getString ( "MainWindow.HelpMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // About
     MainWindow.this.gui.getJMenuItemAbout ().setText (
         Messages.getString ( "MainWindow.About" ) ); //$NON-NLS-1$
     MainWindow.this.gui.getJMenuItemAbout ().setMnemonic (
         Messages.getString ( "MainWindow.AboutMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
 
     // Mouse
     MainWindow.this.gui.getJGTIToolBarToggleButtonMouse ().setToolTipText (
         Messages.getString ( "MachinePanel.Mouse" ) ); //$NON-NLS-1$
     // Add state
     MainWindow.this.gui.getJGTIToolBarToggleButtonAddState ().setToolTipText (
         Messages.getString ( "MachinePanel.AddState" ) ); //$NON-NLS-1$
     // Add transition
     MainWindow.this.gui.getJGTIToolBarToggleButtonAddTransition ()
         .setToolTipText ( Messages.getString ( "MachinePanel.AddTransition" ) ); //$NON-NLS-1$
     // Start state
     MainWindow.this.gui.getJGTIToolBarToggleButtonStartState ().setToolTipText (
         Messages.getString ( "MachinePanel.StartState" ) ); //$NON-NLS-1$
     // Final state
     MainWindow.this.gui.getJGTIToolBarToggleButtonFinalState ().setToolTipText (
         Messages.getString ( "MachinePanel.FinalState" ) ); //$NON-NLS-1$
     // Edit Document
     MainWindow.this.gui.getJGTIToolBarButtonEditDocument ().setToolTipText (
         Messages.getString ( "MachinePanel.EditDocument" ) ); //$NON-NLS-1$
     // Previous Step
     MainWindow.this.gui.getJGTIToolBarButtonPreviousStep ().setToolTipText (
         Messages.getString ( "MachinePanel.WordModePreviousStep" ) ); //$NON-NLS-1$
     // Start Word
     MainWindow.this.gui.getJGTIToolBarButtonStart ().setToolTipText (
         Messages.getString ( "MachinePanel.WordModeStart" ) ); //$NON-NLS-1$
     // Next Step
     MainWindow.this.gui.getJGTIToolBarButtonNextStep ().setToolTipText (
         Messages.getString ( "MachinePanel.WordModeNextStep" ) ); //$NON-NLS-1$
     // Auto Step
     MainWindow.this.gui.getJGTIToolBarToggleButtonAutoStep ().setToolTipText (
         Messages.getString ( "MachinePanel.WordModeAutoStep" ) ); //$NON-NLS-1$
     // Stop Word
     MainWindow.this.gui.getJGTIToolBarButtonStop ().setToolTipText (
         Messages.getString ( "MachinePanel.WordModeStop" ) ); //$NON-NLS-1$
     // Add production
     MainWindow.this.gui.getJGTIToolBarButtonAddProduction ().setToolTipText (
         Messages.getString ( "GrammarPanel.AddProduction" ) ); //$NON-NLS-1$
   }
 
 
   /**
    * Try to open the given file
    * 
    * @param file The file to open
    * @param addToRecentlyUsed Flag signals if file should be added to recently
    *          used files
    */
   public final void openFile ( File file, boolean addToRecentlyUsed )
   {
     openFile ( file, addToRecentlyUsed, this.jGTIMainSplitPane
         .getActiveEditor () );
   }
 
 
   /**
    * Try to open the given file
    * 
    * @param file The file to open
    * @param addToRecentlyUsed Flag signals if file should be added to recently
    *          used files
    * @param usedEditor The {@link ActiveEditor} which should be used.
    */
   public final void openFile ( File file, boolean addToRecentlyUsed,
       ActiveEditor usedEditor )
   {
     JGTIEditorPanelTabbedPane jGTIEditorPanelTabbedPane;
     switch ( usedEditor )
     {
       case LEFT_EDITOR :
       {
         jGTIEditorPanelTabbedPane = this.jGTIMainSplitPane
             .getJGTIEditorPanelTabbedPaneLeft ();
         break;
       }
       case RIGHT_EDITOR :
       {
         jGTIEditorPanelTabbedPane = this.jGTIMainSplitPane
             .getJGTIEditorPanelTabbedPaneRight ();
         break;
       }
       default :
       {
         throw new RuntimeException ( "unsupported editor" ); //$NON-NLS-1$
       }
     }
 
     // check if we already have an editor panel for the file in the left editor
     for ( EditorPanel current : this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPaneLeft () )
     {
       if ( file.equals ( current.getFile () ) )
       {
         this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
         this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
             .setSelectedEditorPanel ( current );
 
         // reorganize recently used files
         if ( addToRecentlyUsed )
         {
           RecentlyUsedMenuItem item = new RecentlyUsedMenuItem ( this, file );
           this.recentlyUsedFiles.remove ( item );
           this.recentlyUsedFiles.add ( 0, item );
           if ( this.recentlyUsedFiles.size () > 10 )
           {
             this.recentlyUsedFiles.remove ( 10 );
           }
           organizeRecentlyUsedFilesMenu ();
         }
         return;
       }
     }
 
     // check if we already have an editor panel for the file in the right editor
     for ( EditorPanel current : this.jGTIMainSplitPane
         .getJGTIEditorPanelTabbedPaneRight () )
     {
       if ( file.equals ( current.getFile () ) )
       {
         this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
         this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
             .setSelectedEditorPanel ( current );
 
         // reorganize recently used files
         if ( addToRecentlyUsed )
         {
           RecentlyUsedMenuItem item = new RecentlyUsedMenuItem ( this, file );
           this.recentlyUsedFiles.remove ( item );
           this.recentlyUsedFiles.add ( 0, item );
           if ( this.recentlyUsedFiles.size () > 10 )
           {
             this.recentlyUsedFiles.remove ( 10 );
           }
           organizeRecentlyUsedFilesMenu ();
         }
         return;
       }
     }
 
     try
     {
       DefaultModel element = ( DefaultModel ) Storage.getInstance ().load (
           file );
 
       if ( element instanceof DefaultMachineModel )
       {
         DefaultMachineModel model = ( DefaultMachineModel ) element;
         EditorPanel newEditorPanel = new MachinePanel ( this.gui, model, file );
 
         jGTIEditorPanelTabbedPane.addEditorPanel ( newEditorPanel );
         newEditorPanel
             .addModifyStatusChangedListener ( this.modifyStatusChangedListener );
         jGTIEditorPanelTabbedPane.setSelectedEditorPanel ( newEditorPanel );
         jGTIEditorPanelTabbedPane.setEditorPanelTitle ( newEditorPanel, file
             .getName () );
 
         addButtonState ( ButtonState.ENABLED_SAVE_AS );
         addButtonState ( ButtonState.ENABLED_SAVE_ALL );
         addButtonState ( ButtonState.ENABLED_CLOSE );
         addButtonState ( ButtonState.ENABLED_CLOSE_ALL );
         addButtonState ( ButtonState.ENABLED_PRINT );
         addButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
         addButtonState ( ButtonState.ENABLED_VALIDATE );
         addButtonState ( ButtonState.ENABLED_MACHINE_EDIT_ITEMS );
         addButtonState ( ButtonState.ENABLED_DRAFT_FOR );
       }
       else if ( element instanceof DefaultGrammarModel )
       {
         DefaultGrammarModel model = ( DefaultGrammarModel ) element;
         EditorPanel newEditorPanel = new GrammarPanel ( this.gui, model, file );
 
         jGTIEditorPanelTabbedPane.addEditorPanel ( newEditorPanel );
         newEditorPanel
             .addModifyStatusChangedListener ( this.modifyStatusChangedListener );
         jGTIEditorPanelTabbedPane.setSelectedEditorPanel ( newEditorPanel );
         jGTIEditorPanelTabbedPane.setEditorPanelTitle ( newEditorPanel, file
             .getName () );
 
         addButtonState ( ButtonState.ENABLED_SAVE_AS );
         addButtonState ( ButtonState.ENABLED_SAVE_ALL );
         addButtonState ( ButtonState.ENABLED_CLOSE );
         addButtonState ( ButtonState.ENABLED_CLOSE_ALL );
         addButtonState ( ButtonState.ENABLED_PRINT );
         addButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
         addButtonState ( ButtonState.ENABLED_VALIDATE );
         addButtonState ( ButtonState.ENABLED_DRAFT_FOR );
       }
       else if ( element instanceof DefaultRegexModel )
       {
         DefaultRegexModel model = ( DefaultRegexModel ) element;
         EditorPanel newEditorPanel = new RegexPanel ( this.gui, model, file );
 
         jGTIEditorPanelTabbedPane.addEditorPanel ( newEditorPanel );
         newEditorPanel
             .addModifyStatusChangedListener ( this.modifyStatusChangedListener );
         jGTIEditorPanelTabbedPane.setSelectedEditorPanel ( newEditorPanel );
         jGTIEditorPanelTabbedPane.setEditorPanelTitle ( newEditorPanel, file
             .getName () );
 
         addButtonState ( ButtonState.ENABLED_SAVE_AS );
         addButtonState ( ButtonState.ENABLED_SAVE_ALL );
         addButtonState ( ButtonState.ENABLED_CLOSE );
         addButtonState ( ButtonState.ENABLED_CLOSE_ALL );
         addButtonState ( ButtonState.ENABLED_PRINT );
         addButtonState ( ButtonState.ENABLED_EDIT_DOCUMENT );
         addButtonState ( ButtonState.ENABLED_VALIDATE );
       }
       else
       {
         throw new RuntimeException ( "not supported element" ); //$NON-NLS-1$
       }
 
       // reorganize recently used files
       if ( addToRecentlyUsed )
       {
         RecentlyUsedMenuItem item = new RecentlyUsedMenuItem ( this, file );
         this.recentlyUsedFiles.remove ( item );
         this.recentlyUsedFiles.add ( 0, item );
         if ( this.recentlyUsedFiles.size () > 10 )
         {
           this.recentlyUsedFiles.remove ( 10 );
         }
         organizeRecentlyUsedFilesMenu ();
       }
     }
     catch ( StoreException exc )
     {
       InfoDialog infoDialog = new InfoDialog ( this.gui, exc.getMessage (),
           Messages.getString ( "MainWindow.ErrorLoad" ) ); //$NON-NLS-1$
       infoDialog.show ();
     }
     PreferenceManager.getInstance ().setWorkingPath (
         file.getParentFile ().getAbsolutePath () );
   }
 
 
   /**
    * Organizes the recently used files in the menu.
    */
   private final void organizeRecentlyUsedFilesMenu ()
   {
     ArrayList < RecentlyUsedMenuItem > notExistingFiles = new ArrayList < RecentlyUsedMenuItem > ();
 
     this.gui.getJMenuRecentlyUsed ().removeAll ();
 
     for ( RecentlyUsedMenuItem item : this.recentlyUsedFiles )
     {
       if ( item.getFile ().exists () )
       {
         this.gui.getJMenuRecentlyUsed ().add ( item );
       }
       else
       {
         notExistingFiles.add ( item );
       }
     }
 
     this.recentlyUsedFiles.removeAll ( notExistingFiles );
 
     if ( this.recentlyUsedFiles.size () > 0 )
     {
       addButtonState ( ButtonState.ENABLED_RECENTLY_USED );
     }
     else
     {
       removeButtonState ( ButtonState.ENABLED_RECENTLY_USED );
     }
   }
 
 
   /**
    * Removes the given {@link ButtonState}.
    * 
    * @param buttonState The {@link ButtonState} to remove.
    */
   public final void removeButtonState ( ButtonState buttonState )
   {
     if ( buttonState.equals ( ButtonState.ENABLED_SAVE_AS ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_SAVE_AS );
       this.gui.getJGTIToolBarButtonSaveAs ().setEnabled ( false );
       this.gui.getJMenuItemSaveAs ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_SAVE_ALL ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_SAVE_ALL );
       this.gui.getJMenuItemSaveAll ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_CLOSE ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_CLOSE );
       this.gui.getJMenuItemClose ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_CLOSE_ALL ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_CLOSE_ALL );
       this.gui.getJMenuItemCloseAll ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_PRINT ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_PRINT );
       this.gui.getJMenuItemPrint ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_UNDO ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_UNDO );
       this.gui.getJMenuItemUndo ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonUndo ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_REDO ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_REDO );
       this.gui.getJMenuItemRedo ().setEnabled ( false );
       this.gui.getJGTIToolBarButtonRedo ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_HISTORY ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_HISTORY );
       this.gui.getJMenuItemHistory ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_MACHINE_TABLE ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_MACHINE_TABLE );
       this.gui.getJCheckBoxMenuItemTable ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_CONSOLE_TABLE ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_CONSOLE_TABLE );
       this.gui.getJCheckBoxMenuItemConsole ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_VALIDATE ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_VALIDATE );
       this.gui.getJMenuItemValidate ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_DRAFT_FOR ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_DRAFT_FOR );
       this.gui.getJMenuDraft ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_DRAFT_FOR_MACHINE ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_DRAFT_FOR_MACHINE );
       this.gui.getJMenuItemDFA ().setEnabled ( false );
       this.gui.getJMenuItemNFA ().setEnabled ( false );
       this.gui.getJMenuItemENFA ().setEnabled ( false );
       this.gui.getJMenuItemPDA ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_DRAFT_FOR_GRAMMAR ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_DRAFT_FOR_GRAMMAR );
       this.gui.getJMenuItemRG ().setEnabled ( false );
       this.gui.getJMenuItemCFG ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_EDIT_DOCUMENT ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_EDIT_DOCUMENT );
       this.gui.getJGTIToolBarButtonEditDocument ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_EDIT_MACHINE ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_EDIT_MACHINE );
       this.gui.getJMenuItemEditMachine ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_ENTER_WORD ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_ENTER_WORD );
       this.gui.getJMenuItemEnterWord ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_AUTO_LAYOUT ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_AUTO_LAYOUT );
       this.gui.getJMenuItemAutoLayout ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_RECENTLY_USED ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_RECENTLY_USED );
       this.gui.getJMenuRecentlyUsed ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_TO_LATEX ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_TO_LATEX );
       this.gui.getJMenuItemExportLatex ().setEnabled ( false );
     }
     else if ( buttonState
         .equals ( ButtonState.ENABLED_ELIMINATE_LEFT_RECURSION ) )
     {
       this.buttonStateList
           .remove ( ButtonState.ENABLED_ELIMINATE_LEFT_RECURSION );
       this.gui.getJMenuItemEliminateLeftRecursion ().setEnabled ( false );
     }
     else if ( buttonState
         .equals ( ButtonState.ENABLED_ELIMINATE_ENTITY_PRODUCTIONS ) )
     {
       this.buttonStateList
           .remove ( ButtonState.ENABLED_ELIMINATE_ENTITY_PRODUCTIONS );
       this.gui.getJMenuItemEliminateEntityProductions ().setEnabled ( false );
     }
     else if ( buttonState
         .equals ( ButtonState.ENABLED_ELIMINATE_EPSILON_PRODUCTIONS ) )
     {
       this.buttonStateList
           .remove ( ButtonState.ENABLED_ELIMINATE_EPSILON_PRODUCTIONS );
       this.gui.getJMenuItemEliminateEpsilonProductions ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_LEFT_FACTORING ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_LEFT_FACTORING );
       this.gui.getJMenuItemLeftfactoring ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_CREATE_RDP ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_CREATE_RDP );
       this.gui.getJMenuItemCreateRDP ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_TO_CORE_SYNTAX ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_TO_CORE_SYNTAX );
       this.gui.getJMenuItemToCoreSyntax ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_REGEX_INFO ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_REGEX_INFO );
       this.gui.getJCheckBoxMenuItemRegexInfo ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_MACHINE_EDIT_ITEMS ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_MACHINE_EDIT_ITEMS );
       this.gui.getJGTIToolBarToggleButtonAddState ().setEnabled ( false );
       this.gui.getJGTIToolBarToggleButtonAddTransition ().setEnabled ( false );
       this.gui.getJGTIToolBarToggleButtonFinalState ().setEnabled ( false );
       this.gui.getJGTIToolBarToggleButtonMouse ().setEnabled ( false );
       this.gui.getJGTIToolBarToggleButtonStartState ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_NAVIGATION_DEACTIVE ) )
     {
       throw new IllegalArgumentException (
           "remove navigation state not supported, use add instead" );//$NON-NLS-1$
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_NAVIGATION_AUTO_STEP ) )
     {
       throw new IllegalArgumentException (
           "remove navigation state not supported, use add instead" );//$NON-NLS-1$
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_NAVIGATION_START ) )
     {
       throw new IllegalArgumentException (
           "remove navigation state not supported, use add instead" );//$NON-NLS-1$
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_NAVIGATION_STOP ) )
     {
       throw new IllegalArgumentException (
           "remove navigation state not supported, use add instead" );//$NON-NLS-1$
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT ) )
     {
       throw new IllegalArgumentException (
           "remove navigation state not supported, use add instead" ); //$NON-NLS-1$
     }
     else if ( buttonState
         .equals ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT_PREVIOUS ) )
     {
       throw new IllegalArgumentException (
           "remove navigation state not supported, use add instead" ); //$NON-NLS-1$
     }
     else if ( buttonState
         .equals ( ButtonState.ENABLED_NAVIGATION_STEPS_PREVIOUS ) )
     {
       throw new IllegalArgumentException (
           "remove navigation state not supported, use add instead" ); //$NON-NLS-1$
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_CONVERT_TO ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO );
       this.gui.getJMenuConvertTo ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_CONVERT_TO_COMPLETE ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_TO_COMPLETE );
       this.gui.getJMenuConvertToComplete ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_MINIMIZE ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_MINIMIZE );
       this.gui.getJMenuItemMinimize ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_CONVERT_DFA_TO_REGEX );
       this.gui.getJMenuItemConvertToRegex ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_REACHABLE_STATES ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_REACHABLE_STATES );
       this.gui.getJMenuItemReachableStates ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_EXPORT_PICTURE ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_EXPORT_PICTURE );
       this.gui.getJMenuItemExportPicture ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_REORDER_STATE_NAMES ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_REORDER_STATE_NAMES );
       this.gui.getJMenuItemReorderStateNames ().setEnabled ( false );
     }
     else if ( buttonState.equals ( ButtonState.ENABLED_SAVE ) )
     {
       this.buttonStateList.remove ( ButtonState.ENABLED_SAVE );
       logger.debug ( "setSaveState", "set save status to " + Messages.QUOTE //$NON-NLS-1$//$NON-NLS-2$
           + false + Messages.QUOTE );
       EditorPanel panel = this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPane ().getSelectedEditorPanel ();
       if ( panel != null )
       {
         this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
             .setEditorPanelTitle ( panel, panel.getName () );
       }
       this.gui.getJGTIToolBarButtonSave ().setEnabled ( false );
       this.gui.getJMenuItemSave ().setEnabled ( false );
     }
     // selected
     else if ( buttonState.equals ( ButtonState.SELECTED_MACHINE_TABLE ) )
     {
       this.buttonStateList.remove ( ButtonState.SELECTED_MACHINE_TABLE );
       this.gui.getJCheckBoxMenuItemTable ().setSelected ( false );
     }
     else if ( buttonState.equals ( ButtonState.SELECTED_CONSOLE_TABLE ) )
     {
       this.buttonStateList.remove ( ButtonState.SELECTED_CONSOLE_TABLE );
       this.gui.getJCheckBoxMenuItemConsole ().setSelected ( false );
     }
     else if ( buttonState.equals ( ButtonState.SELECTED_MOUSE ) )
     {
       this.buttonStateList.remove ( ButtonState.SELECTED_MOUSE );
       this.gui.getJGTIToolBarToggleButtonMouse ().setSelected ( false );
     }
     else if ( buttonState.equals ( ButtonState.SELECTED_AUTO_STEP ) )
     {
       this.buttonStateList.remove ( ButtonState.SELECTED_AUTO_STEP );
       this.gui.getJGTIToolBarToggleButtonAutoStep ().setSelected ( false );
     }
     else if ( buttonState.equals ( ButtonState.SELECTED_ENTER_WORD ) )
     {
       this.buttonStateList.remove ( ButtonState.SELECTED_ENTER_WORD );
       this.gui.getJGTIToolBarToggleButtonEnterWord ().setSelected ( false );
     }
     // visible
     else if ( buttonState.equals ( ButtonState.VISIBLE_MACHINE ) )
     {
       this.buttonStateList.remove ( ButtonState.VISIBLE_MACHINE );
       this.gui.getJSeparatorNavigation ().setVisible ( false );
       this.gui.getJGTIToolBarToggleButtonMouse ().setVisible ( false );
       this.gui.getJGTIToolBarToggleButtonAddState ().setVisible ( false );
       this.gui.getJGTIToolBarToggleButtonStartState ().setVisible ( false );
       this.gui.getJGTIToolBarToggleButtonFinalState ().setVisible ( false );
       this.gui.getJGTIToolBarToggleButtonEnterWord ().setVisible ( false );
       this.gui.getJGTIToolBarToggleButtonAddTransition ().setVisible ( false );
       this.gui.getJGTIToolBarButtonStart ().setVisible ( false );
       this.gui.getJGTIToolBarButtonPreviousStep ().setVisible ( false );
       this.gui.getJGTIToolBarButtonNextStep ().setVisible ( false );
       this.gui.getJGTIToolBarToggleButtonAutoStep ().setVisible ( false );
       this.gui.getJGTIToolBarButtonStop ().setVisible ( false );
     }
     else if ( buttonState.equals ( ButtonState.VISIBLE_GRAMMAR ) )
     {
       this.buttonStateList.remove ( ButtonState.VISIBLE_GRAMMAR );
       this.gui.getJGTIToolBarButtonAddProduction ().setVisible ( false );
     }
     else if ( buttonState.equals ( ButtonState.VISIBLE_REGEX ) )
     {
       this.buttonStateList.remove ( ButtonState.VISIBLE_REGEX );
     }
     else
     {
       throw new IllegalArgumentException ( "unsupported button state: " //$NON-NLS-1$
           + buttonState );
     }
   }
 
 
   /**
    * Open all files which was open at last session
    */
   public final void restoreOpenFiles ()
   {
     for ( ObjectPair < File, ActiveEditor > current : PreferenceManager
         .getInstance ().getOpenedFilesItem ().getFiles () )
     {
       openFile ( current.getFirst (), false, current.getSecond () );
     }
 
     File activeFile = PreferenceManager.getInstance ().getOpenedFilesItem ()
         .getActiveFile ();
 
     if ( activeFile != null )
     {
       // left editor
       for ( EditorPanel current : this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPaneLeft () )
       {
         if ( current.getFile ().getAbsolutePath ().equals (
             activeFile.getAbsolutePath () ) )
         {
           this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneLeft ()
               .setSelectedEditorPanel ( current );
           this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.LEFT_EDITOR );
           break;
         }
       }
       // right editor
       for ( EditorPanel current : this.jGTIMainSplitPane
           .getJGTIEditorPanelTabbedPaneRight () )
       {
         if ( current.getFile ().getAbsolutePath ().equals (
             activeFile.getAbsolutePath () ) )
         {
           this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPaneRight ()
               .setSelectedEditorPanel ( current );
           this.jGTIMainSplitPane.setActiveEditor ( ActiveEditor.RIGHT_EDITOR );
           break;
         }
       }
     }
   }
 
 
   /**
    * Updates the word navigation states.
    */
   public final void updateWordNavigationStates ()
   {
     EditorPanel panel = this.jGTIMainSplitPane.getJGTIEditorPanelTabbedPane ()
         .getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
 
     if ( this.gui.getJGTIToolBarToggleButtonAutoStep ().isSelected () )
     {
       addButtonState ( ButtonState.ENABLED_NAVIGATION_AUTO_STEP );
       return;
     }
 
     MachinePanel machinePanel = ( MachinePanel ) panel;
     boolean nextAvailable = machinePanel.getMachine ().isNextSymbolAvailable ();
     boolean previousAvailable = machinePanel.getMachine ()
         .isPreviousSymbolAvailable ();
 
     if ( !nextAvailable && !previousAvailable )
     {
       addButtonState ( ButtonState.ENABLED_NAVIGATION_STOP );
     }
     else if ( !nextAvailable && previousAvailable )
     {
       addButtonState ( ButtonState.ENABLED_NAVIGATION_STEPS_PREVIOUS );
     }
     else if ( nextAvailable && !previousAvailable )
     {
       addButtonState ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT );
     }
     else if ( nextAvailable && previousAvailable )
     {
       addButtonState ( ButtonState.ENABLED_NAVIGATION_STEPS_NEXT_PREVIOUS );
     }
   }
 }
