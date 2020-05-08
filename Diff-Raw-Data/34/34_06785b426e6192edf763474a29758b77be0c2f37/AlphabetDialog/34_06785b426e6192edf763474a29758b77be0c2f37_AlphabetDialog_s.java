 package de.unisiegen.gtitool.ui.logic;
 
 
 import java.util.TreeSet;
 
 import javax.swing.JFrame;
 
 import de.unisiegen.gtitool.core.entities.Alphabet;
 import de.unisiegen.gtitool.core.entities.DefaultRegexAlphabet;
 import de.unisiegen.gtitool.core.entities.Symbol;
 import de.unisiegen.gtitool.core.exceptions.alphabet.AlphabetException;
 import de.unisiegen.gtitool.core.machines.Machine;
 import de.unisiegen.gtitool.core.machines.Machine.MachineType;
 import de.unisiegen.gtitool.core.regex.DefaultRegex;
 import de.unisiegen.gtitool.ui.logic.interfaces.LogicClass;
 import de.unisiegen.gtitool.ui.netbeans.AlphabetDialogForm;
 import de.unisiegen.gtitool.ui.preferences.PreferenceManager;
 import de.unisiegen.gtitool.ui.preferences.item.PDAModeItem;
 import de.unisiegen.gtitool.ui.redoundo.MachineAlphabetChangedItem;
 import de.unisiegen.gtitool.ui.redoundo.RegexUndoItem;
 import de.unisiegen.gtitool.ui.style.listener.ParseableChangedListener;
 
 
 /**
  * The logic class for the create new transition dialog.
  * 
  * @author Christian Fehler
  * @version $Id: AlphabetDialog.java 1372 2008-10-30 08:36:20Z fehler $
  */
 public final class AlphabetDialog implements LogicClass < AlphabetDialogForm >
 {
 
   /**
    * The {@link AlphabetDialogForm}.
    */
   private AlphabetDialogForm gui;
 
 
   /**
    * The {@link Machine} of this dialog.
    */
   private Machine machine;
 
 
   /**
    * The {@link MachinePanel}.
    */
   private MachinePanel machinePanel;
 
 
   /**
    * The parent {@link JFrame}.
    */
   private JFrame parent;
 
 
   /**
    * The {@link DefaultRegex} of this dialog
    */
   private DefaultRegex regex;
 
 
   /**
    * The {@link RegexPanel}
    */
   private RegexPanel regexPanel;
 
 
   /**
    * Create a new {@link AlphabetDialog}
    * 
    * @param parent The parent frame.
    * @param machinePanel The {@link MachinePanel}.
    * @param machine The {@link Machine} of this dialog.
    */
   public AlphabetDialog ( JFrame parent, MachinePanel machinePanel,
       Machine machine )
   {
     this.parent = parent;
     this.machine = machine;
     this.machinePanel = machinePanel;
     this.gui = new AlphabetDialogForm ( this, this.parent );
     this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
         .setText ( this.machine.getAlphabet () );
     this.gui.alphabetPanelForm.styledRegexAlphabetParserPanelInput
         .setVisible ( false );
     this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
         .setNotRemoveableSymbols ( this.machine
             .getNotRemoveableSymbolsFromAlphabet () );
     this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
         .addParseableChangedListener ( new ParseableChangedListener < Alphabet > ()
         {
 
           public void parseableChanged (
               @SuppressWarnings ( "unused" ) Alphabet newAlphabet )
           {
             setButtonStatus ();
           }
         } );
 
     this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
         .setText ( this.machine.getPushDownAlphabet () );
     this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
         .setNotRemoveableSymbols ( this.machine
             .getNotRemoveableSymbolsFromPushDownAlphabet () );
     this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
         .addParseableChangedListener ( new ParseableChangedListener < Alphabet > ()
         {
 
           public void parseableChanged (
               @SuppressWarnings ( "unused" ) Alphabet newAlphabet )
           {
             setButtonStatus ();
           }
         } );
 
     this.gui.alphabetPanelForm.jGTICheckBoxPushDownAlphabet
         .setSelected ( this.machine.isUsePushDownAlphabet () );
 
     if ( !this.machine.getMachineType ().equals ( MachineType.PDA ) )
     {
       if ( PreferenceManager.getInstance ().getPDAModeItem ().equals (
           PDAModeItem.SHOW ) )
       {
         // do nothing
       }
       else if ( PreferenceManager.getInstance ().getPDAModeItem ().equals (
           PDAModeItem.HIDE ) )
       {
         this.gui.alphabetPanelForm.jGTICheckBoxPushDownAlphabet
             .setEnabled ( false );
         this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
             .setEnabled ( false );
       }
       else
       {
         throw new RuntimeException ( "unsupported pda mode" ); //$NON-NLS-1$
       }
     }
 
   }
 
 
   /**
    * Creates a new {@link AlphabetDialog} for a {@link DefaultRegexAlphabet}
    * 
    * @param parent The parnet frame of the Dialog
    * @param regexPanel The {@link RegexPanel}
    * @param regex The {@link DefaultRegex}
    */
   public AlphabetDialog ( JFrame parent, RegexPanel regexPanel,
       DefaultRegex regex )
   {
     this.parent = parent;
     this.regexPanel = regexPanel;
     this.regex = regex;
 
     this.gui = new AlphabetDialogForm ( this, this.parent );
     this.gui.alphabetPanelForm.styledRegexAlphabetParserPanelInput
         .setText ( this.regex.getAlphabet ().toClassPrettyString () );
     this.gui.alphabetPanelForm.styledRegexAlphabetParserPanelInput
         .setNotRemoveableSymbols ( this.regex
             .getNotRemoveableSymbolsFromAlphabet () );
     this.gui.alphabetPanelForm.styledRegexAlphabetParserPanelInput
         .addParseableChangedListener ( new ParseableChangedListener < Alphabet > ()
         {
 
           public void parseableChanged (
               @SuppressWarnings ( "unused" ) Alphabet newAlphabet )
           {
             setButtonStatus ();
           }
         } );
 
     this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
         .setVisible ( false );
     this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
         .setVisible ( false );
     this.gui.alphabetPanelForm.styledRegexAlphabetParserPanelInput
         .setVisible ( true );
     this.gui.alphabetPanelForm.jGTICheckBoxPushDownAlphabet.setVisible ( false );
     this.gui.alphabetPanelForm.jGTILabelRegexAlphabet.setVisible ( false );
 
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see LogicClass#getGUI()
    */
   public final AlphabetDialogForm getGUI ()
   {
     return this.gui;
   }
 
 
   /**
    * Handles cancel button pressed.
    */
   public final void handleCancel ()
   {
     this.gui.dispose ();
   }
 
 
   /**
    * Handle ok button pressed.
    */
   public final void handleOk ()
   {
     this.gui.setVisible ( false );
     if ( this.machine != null )
     {
       if ( ( this.machine.isUsePushDownAlphabet () != this.gui.alphabetPanelForm.jGTICheckBoxPushDownAlphabet
           .isSelected () )
           || !this.machine.getAlphabet ().equals (
               this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
                   .getParsedObject () )
           || !this.machine.getPushDownAlphabet ().equals (
               this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
                   .getParsedObject () ) )
       {
         this.machinePanel.getRedoUndoHandler ().addItem (
             new MachineAlphabetChangedItem ( this.machinePanel, this.machine,
                 this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
                     .getParsedObject (),
                 this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
                     .getParsedObject (), this.machine.isUsePushDownAlphabet (),
                 this.gui.alphabetPanelForm.jGTICheckBoxPushDownAlphabet
                     .isSelected () ) );
       }
       performAlphabetChange ( this.machine.getAlphabet (),
           this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
               .getParsedObject () );
       performAlphabetChange ( this.machine.getPushDownAlphabet (),
           this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
               .getParsedObject () );
       this.machine
           .setUsePushDownAlphabet ( this.gui.alphabetPanelForm.jGTICheckBoxPushDownAlphabet
               .isSelected () );
     }
     else
     {
       try
       {
         this.regexPanel.addUndoItem ( new RegexUndoItem (
             new DefaultRegexAlphabet ( this.regex.getAlphabet ().get () ) ) );
       }
       catch ( AlphabetException exc )
       {
         exc.printStackTrace ();
       }
       this.regexPanel.updateRedoUndoButtons ();
       this.regexPanel.performAlphabetChange ( this.regex.getAlphabet (),
           this.gui.alphabetPanelForm.styledRegexAlphabetParserPanelInput
               .getParsedObject () );
     }
     this.gui.dispose ();
   }
 
 
   /**
    * Preforms the {@link Alphabet} change.
    * 
    * @param oldAlphabet The old {@link Alphabet}.
    * @param newAlphabet The new {@link Alphabet}.
    */
   private final void performAlphabetChange ( Alphabet oldAlphabet,
       Alphabet newAlphabet )
   {
     TreeSet < Symbol > symbolsToAdd = new TreeSet < Symbol > ();
     TreeSet < Symbol > symbolsToRemove = new TreeSet < Symbol > ();
     for ( Symbol current : newAlphabet )
     {
       if ( !oldAlphabet.contains ( current ) )
       {
         symbolsToAdd.add ( current );
       }
     }
     for ( Symbol current : oldAlphabet )
     {
       if ( !newAlphabet.contains ( current ) )
       {
         symbolsToRemove.add ( current );
       }
     }
     try
     {
       oldAlphabet.add ( symbolsToAdd );
       oldAlphabet.remove ( symbolsToRemove );
     }
     catch ( AlphabetException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
     }
   }
 
 
   /**
    * Sets the status of the buttons.
    */
   protected final void setButtonStatus ()
   {
     if ( this.machine != null )
     {
       if ( ( this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
           .getParsedObject () == null )
           || ( this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
               .getParsedObject () == null ) )
       {
         this.gui.jGTIButtonOk.setEnabled ( false );
       }
       else
       {
         this.gui.jGTIButtonOk.setEnabled ( true );
       }
     }
     else
     {
       if ( this.gui.alphabetPanelForm.styledRegexAlphabetParserPanelInput
           .getParsedObject () == null )
       {
         this.gui.jGTIButtonOk.setEnabled ( false );
       }
       else
       {
         this.gui.jGTIButtonOk.setEnabled ( true );
       }
     }
   }
 
 
   /**
    * Show the dialog for creating a new transition
    */
   public final void show ()
   {
     int x = this.parent.getBounds ().x + ( this.parent.getWidth () / 2 )
         - ( this.gui.getWidth () / 2 );
     int y = this.parent.getBounds ().y + ( this.parent.getHeight () / 2 )
         - ( this.gui.getHeight () / 2 );
     this.gui.setBounds ( x, y, this.gui.getWidth (), this.gui.getHeight () );
     this.gui.setVisible ( true );
   }
 }
