 package GUI;
 
 import com.sun.deploy.panel.JavaPanel;
 import instruments.Instrument;
 import messaging.SongFragment;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.*;
 import java.util.Timer;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Tae Kim
  * Date: 5/2/13
  * Time: 12:14 AM
  * To change this template use File | Settings | File Templates.
  */
 public abstract class AbstractInstrument extends JPanel implements SessionListener{
 
     // What is the narrowest we will let a button be?
     protected final static int minimumButtonWidth = 75;
 
     private final static ImageIcon notePlayingLed = new ImageIcon("Resources/images/led-on.jpg");
     private final static ImageIcon noteSilentLed =  new ImageIcon("Resources/images/led-off.jpg");
 
     protected JPanel titlePanel;
     protected JPanel buttonPanel;
     protected JPanel ipPanel;
     protected JPanel backPanel;
     protected JLabel ipLabel;
     protected char[] keyboardKeysToUse = {'a','s','d','f','g','h','j','k'};
     protected Instrument instrumentToPlay;
     final JPanel mainPanel;
     // This holds the Instrument class type
     private AbstractInstrument thisInstrument;
 
     // Holds the action map, to map key board presses to actions.
     protected ActionMap keyboardActionMap;
     protected InputMap  keyboardInputMap;
 
     // Holds the notes, inpsected from the instrument,
     // used to build the GUI later
     protected ArrayList<String> notesForButtons;
 
 
 
     // is the instrument playbable now?
     protected boolean enabled;
 
     /**
      * Constructor, sets up all the graphical elements and actions to take
      * When pressed
      * @param cl this is the card Layout
      * @param mainPanel the main panel for all of this
      */
     public AbstractInstrument(final CardLayout cl, final JPanel mainPanel){
         // Set the layout, size, and color of each instrument
         this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         this.setSize(new Dimension(1000, 800));
         this.setBackground(Color.WHITE);
 
         this.mainPanel = mainPanel;
         this.thisInstrument =  this;
 
         // Set default to piano
         instrumentToPlay = new instruments.Piano();
 
 
         // Set up the keyboard mapping stubs
         keyboardActionMap = new ActionMap();
         keyboardInputMap = new ComponentInputMap(mainPanel);
         // remove keyboard events, from last instrument?
         // I wish Java had a destructor....
 //        cleanUpKeyBindings();
 
         // Panel for the Title
         titlePanel = new JPanel();
         titlePanel.setSize(new Dimension(1000, 400));
         titlePanel.setBackground(Color.WHITE);
 
         // Panel for the buttons
         buttonPanel = new JPanel();
         buttonPanel.setSize(new Dimension(1000, 300));
         buttonPanel.setBackground(Color.WHITE);
 
 
         // Panel for the IP info and back button
         ipPanel = new JPanel();
         ipPanel.setSize(new Dimension(1000, 50));
         ipPanel.setBackground(Color.WHITE);
 
         // Add ip information
         ipLabel = new JLabel();
         ipLabel.setText("You are hosting at: " + Session.songController.getHostingAddress() + ".");
         ipPanel.add(ipLabel);
 
         // Panel for the back button
         backPanel = new JPanel();
         backPanel.setSize(new Dimension(1000, 50));
         backPanel.setBackground(Color.WHITE);
 
         // Add back button
         JButton backButton = new JButton();
         backButton.setText("Back");
         backButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 cl.show(mainPanel, "choose");
 //                ChosenInstrument.instrumentChosen = "";
             }
         });
         backPanel.add(backButton);
 
         // Add save song button
         JButton saveButton = new SaveSongButton(mainPanel);
         backPanel.add(saveButton);
 
 
     }
 
     protected void cleanUpKeyBindings(){
         // Clean up keybindings!
         mainPanel.getActionMap().clear();
         mainPanel.getInputMap().clear();
     }
 
     /**
      * Set up any things that need to get setup when this instrument is activated
      */
     protected void activateInstrument(){
 
        resetKeyboardActions();

         // Load up the keybindings for this instrument
         for(KeyStroke k : this.keyboardInputMap.allKeys()){
             getInputMap().put(k,this.keyboardInputMap.get(k));
         }
         //Load up the action for those keyboard bindings
         for(Object keyForAction : this.keyboardActionMap.allKeys()){
             getActionMap().put(keyForAction,this.keyboardActionMap.get(keyForAction));
         }
        transferFocus();
//        requestFocusInWindow();
     }
 
     /**
      *
      * Inspects the instrument for this interface
      * and will automatically setup the keys for it.
      */
     public void setupNotesForThisInstrument(){
 
         notesForButtons = new ArrayList<String>();
         // Make sure we have an instrument here
         if(this.instrumentToPlay != null){
             // Now get all the supported notes
             for(Instrument.Note note : this.instrumentToPlay.getSupportedNotes()){
                 // Put them in the list of buttons to make
                 notesForButtons.add(note.name());
             }
         }
     }
 
     /**
      * This can be used to pass in a list of notes
      *
      * @param notes
      */
     public void setupNotesForThisInstrument(String[] notes){
         notesForButtons = new ArrayList<String>();
         // Note does not check if it is a legit note here..
         for(int i = 0; i < notes.length; i++)
         {
             notesForButtons.add(notes[i]);
         }
     }
 
     /**
      * This is a generic version, we can do lots of things here
      */
     public void addNotesToButtonPanel(){
 
                            // there is some padding factor...
         int buttonWidth = (buttonPanel.getWidth() - 100) / notesForButtons.size();
         if(buttonWidth < minimumButtonWidth){
             buttonWidth = minimumButtonWidth;
         }
         ArrayList<String> supportedNoteNames = new ArrayList<String>(15);
         for(Instrument.Note note : this.instrumentToPlay.getSupportedNotes()){
             supportedNoteNames.add(note.name());
         }
 
 
         // Create buttons for the instrument
         int i = 0;
         for(String buttonAndNoteName: notesForButtons){
             // If this is a real note name for the instrument
             if(supportedNoteNames.contains(buttonAndNoteName)){
 
                 // Fun grid constraints!
                 GridBagConstraints gbc = new GridBagConstraints ();
                 // place the components top-to-bottom, rather than left-to-right
                 gbc.gridx = 0;
                 gbc.gridy = GridBagConstraints.RELATIVE;
 
                 //Note LED, lit up when toggled, returns to off state after key stroke.
                 JLabel notePlayingLedLabel = new JLabel();
                 notePlayingLedLabel.setIcon(noteSilentLed);
 
                 // Build the button and special indicator in a new panel
                 // The LED should light up when the action for the button is triggered
                 final JPanel noteAndIndicator = new JPanel();
                              noteAndIndicator.setBackground(Color.WHITE);
                              noteAndIndicator.setLayout(new GridBagLayout());
 
                 final JButton key = new JButton(buttonAndNoteName);
 
                 key.setBackground(Color.WHITE);
 
                 // Sized based on the number of notes??
                 key.setPreferredSize(new Dimension(buttonWidth, 175));
 
                 // Now attach the events to these buttons
                 setUpListenersForNoteForKeyAtIndex(i, key, buttonAndNoteName,notePlayingLedLabel);
 
                 noteAndIndicator.add(notePlayingLedLabel,gbc);
                 noteAndIndicator.add(key,gbc);
                 buttonPanel.add(noteAndIndicator);
 
                 // Finally add it into the panel.
 //                buttonPanel.add(key);
                 i++;
             }
         }
     }
 
     protected void setUpListenersForNoteForKeyAtIndex(
                                          int i,
                                          final JButton key,
                                          final String noteName,
                                          final JLabel ledIndicator)
     {
         // Hacking this in. Make sure the key can not gain focus
         key.setFocusable(false);
 
 
 
         // This defines the action to take when the button is pressed
         // ... we also are mapping keys to this
         AbstractAction buttonPressed = new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 SongFragment fragment = new SongFragment(instrumentToPlay.getNoteByName(noteName));
                 // Play the note
                 Session.songController.play(fragment, true);
                 // Set the indicator to on
                 ledIndicator.setIcon(notePlayingLed);
                 // After 150ms, then turn the indicator off
                 javax.swing.Timer returnButtonToOffState = new javax.swing.Timer(150, new ActionListener() {
                     public void actionPerformed(ActionEvent arg0) {
                         ledIndicator.setIcon(noteSilentLed);
                     }
                 });
                 // Only execute once
                 returnButtonToOffState.setRepeats(false);
                 // Go!
                 returnButtonToOffState.start();
             }
         };
         System.out.println(this.instrumentToPlay.getName() + " " + noteName + " action: " + buttonPressed);
         // given the button, attach the action
         key.addActionListener(buttonPressed);
 
         // Now we are also going to try and attach the
         // keyboard (on the computer) the home row keys to be able to play
         // and instrument here too.
         if(i < keyboardKeysToUse.length){
             // This is the map from the key to the action
             String keyboardKeyKey = "key_"+ keyboardKeysToUse[i];
             // NOTE: this is built now, but swapped out when the instrument panel is activated in
             // the call to activateInstrument();
             // -----------------------------------------------------------------------------------
             // Register this key down input, to trigger an action in the map...
             keyboardInputMap.put(KeyStroke.getKeyStroke(keyboardKeysToUse[i]), keyboardKeyKey);
             // ....define the map's action here
             keyboardActionMap.put(keyboardKeyKey,buttonPressed);
         }
     }
 
     public void addComponents(){
         // Add all of the components
         this.add(titlePanel);
         this.add(buttonPanel);
         this.add(ipPanel);
         this.add(backPanel);
     }
 
     @Override
     public void onSessionJoin(final String address, final int port) {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 ipLabel.setText("You're connected to " + address + ":" + port);
             }
         });
     }
 
     public abstract String getInstrumentType();
 
 }
