 import javax.swing.*;
 import java.awt.*;//BorderLayout;
 import javax.sound.midi.*;
 import java.io.*;
 import java.lang.Math.*;
 import java.util.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /***************************************
 
 ModMIDITuning V1.3
 (c) 2013 Dallin S. Durfee
 
 for help run
 
 java ModMIDITuning -h
 
  ***************************************/
 
 public class ModMIDITuning {
     
     public static String[] notes = {"c","c#","d","d#","e","f","f#","g","g#","a","a#","b"};
     public static Boolean useGUI = false;
     public static Boolean haderror = false;
     
     public static String versionstring = new String("ModMIDITuning 1.3");
     public static String licensestring = new String("Copyright (c) 2013  Dallin S. Durfee\nPermission to modify and redistribute this software is granted under the MIT License (see License.txt)");
     
     public static JFrame frame = new JFrame(versionstring);
     public static JTextField jInfile = new JTextField(30);
     public static JComboBox<String> jNotes = new JComboBox<String>();
     public static JComboBox<String> jTunings = new JComboBox<String>();
     public static JComboBox<Integer> jBendRange = new JComboBox<Integer>();
     public static JCheckBox jBendRangeCommand = new JCheckBox("Send Bend Range Command");
     
     public static String description = new String("This program reads a midi file and then writes a new midi file in which each note (c, c#, d, d#, e, f, etc) or any events that affect one of these notes is put on its own channel - all of the c's and their related events on channel one, the c#'s on channel two, etc. (skipping channel 10 because it's reserved for rhythm).  At the beginning of each track in the new midi file, a pitch bend is applied to effectively \"retune\" the instrument using the selected tuning scheme.  Because of the way this software works, it does not accommodate \"stretch\" tuning or any other type of tuning in which octave intervals are not simple factor-of-two frequency ratios.  All pitch bend events in the input file are ignored (they would destroy the instrument \"retuning\"). Since notes are moved to different channels, any channel-wide events are repeated on all used channels in the new midi file.  Only the first program change encountered will be used (otherwise program changes intended for different channels will overwrite each other) - this software works best on a midi file containing just one instrument.");
     
     
     public static String usage = new String("Usage:\n\nFor standard usage with a GUI, just run\n     java ModMIDITuning\nFor information about using the program in GUI mode, run the program and click on the \"help\" button.\n\nFor command line interface, run\n     java ModMIDITuning infile.mid outfile.mid [[Rootnote [Scheme [Maxpitchbend [WriteMaxpitchbend]]]]\nwhere Rootnote, Scheme, Maxpitchbend, and WriteMaxpitchbend are optional arguments.\n\nRootnote is the root note in the tuning scheme.  Possible values are c,c#,d,d#,e,f,f#,g,g#,a,a#, and b.  The default value is c.  Scheme is the tuning scheme to use.  Possible values are equal_temperament, pythagorean, meantone, and extended_five_limit - additional schemes can be added by opening the file \"ModMIDITuning.tunings\" in a text editor and following the instructions in the file.  The default value is pythagorean.  Maxpitchbend is the maximum number of semitones that the midi device the file will be played on can bend a note.  This is usually two (i.e. the pitch bend signal can change the pitch by as much as +/- two half steps) for a standard software synthesizer on a computer.  The default value is 2.\n\nIf WriteMaxpitchbend is true, midi events will be written to the file to attempt to set the synthesizer's pitch bend range to Maxpitchbend.  If it if false, the pitch bend range won't be adjusted, and it will be assumed that it is equal to the value of Maxpitchbend by default.  The default value is true.\n\nTo get help: \n     java ModMIDITuning -h");
     
     public static String guiusage = new String("Usage: Select the midi file to be used as input, select the tuning scheme to be used, and select the root note of the tuning method.  Then click on \"Generate and save re-tuned midi file\" to make a new midi file with the selected tuning (you will be prompted for the name of the new midi file to save).  If you wish, you can set the number of half steps your synthesizer should use as the maximum bend range, and check or uncheck the \"Send Bend Range Command\" box to select whether to write midi data to try to set the maximum bend range on the synthesizer when the midi file is played.  Most midi devices have a default bend range of 2, and you are safest leaving these options with their default values.  If you want to add additional tuning schemes, open the file \"ModMIDITuning.tunings\" in a text editor and follow the instructions in it.");
     
     public static String tuningfilename = new String("");
     
     public static String lastfile = new String("");  // used to keep track of last file loaded or saved, so that file chooser opens in same directory
     
     public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException, IOException {
 	
 	// print software name, version, licensing info
 	System.out.println(versionstring);
 	System.out.println(licensestring);
 	System.out.println();
 	
 	// save path to tunings file
 	String jarpath = ModMIDITuning.class.getProtectionDomain().getCodeSource().getLocation().getPath();
 	tuningfilename = new String(jarpath+"ModMIDITuning.tunings");
 	// set default directory to load or save files to the working dir
 	lastfile = System.getProperty("user.dir");
 	// Make sure that a valid number of arguments were given.  If asking for help, give it.
 	// If there are no arguments, start up gui mode.  Otherwise do command line mode
 	if(args.length == 0){
 	    useGUI = true;
 	    createAndShowGUI();
 	}
 	else {
 	    // If the number of arguments doesn't make sense, print usage and exit
 	    if(args.length > 6){
 		printUsageAndExit();
 	    }
 	    if(args.length == 1){
 		// If there is only one argument, they either want help or made a mistake
 		if(args[0].equals("-h")){
 		    printHelpAndExit();
 		}
 		else{
 		    printUsageAndExit();
 		}
 	    }
 	    // set the root note that the tuning is based upon
 	    int rootnote = 0;
 	    if(args.length > 2){
 		for(int i=0; i<12; i++){
 		    if(args[2].toLowerCase().contentEquals(notes[i])){
 			rootnote = i;
 			break;
 		    }
 		}
 		if(rootnote > 11){
 		    System.out.println(args[2] + " is not a valid root note"); printUsageAndExit();
 		}
 	    }	
 	    
 	    String temperamentstring = "pythagorean";
 	    if(args.length > 3){
 		temperamentstring = new String(args[3]);
 	    }
 	    
 	    double maxpitchbend = 2.0;
 	    if(args.length > 4){
 		try{
 		    maxpitchbend = Double.parseDouble(args[4]);
 		} catch (NumberFormatException e){
 		    System.out.println(args[4] + " is not a valid number of semitones.");
 		    printUsageAndExit();
 		}
 	    }
 	    Boolean writemaxpitchbend = true;
 	    if(args.length > 5){
 		String wmpbStr = new String("false");
 		if(wmpbStr.equalsIgnoreCase(args[5])){
 		    writemaxpitchbend = false;
 		}
 	    }
 
 	    makethefile(args[0],args[1],rootnote,temperamentstring, maxpitchbend,writemaxpitchbend);
 	}  // end of command line mode code	
     }
 
     public static void displaymessage(String thestring){
 	// a simple function to throw up a message
 	if(useGUI) {
 	    JTextArea textArea = new JTextArea(thestring);
 	    textArea.setColumns(50);
 	    textArea.setLineWrap(true);
 	    textArea.setWrapStyleWord(true);
 	    textArea.setSize(textArea.getPreferredSize().width,1);
 	    JOptionPane.showMessageDialog(frame, textArea);
 	}
 	else{
 	    System.out.println(thestring);
 	}
     }
 
     public static void displayerror(String thestring){
 	// a simple way to display an error message
 	if(useGUI){
 	    JOptionPane.showMessageDialog(frame, thestring);
 	}
 	else{
 	    System.out.println(thestring);
 	    System.out.println();
 	    printUsageAndExit();
 	}
     }
 
     private static void createAndShowGUI() throws  MidiUnavailableException, InvalidMidiDataException, IOException {
 	//Set up the GUI.
 
 	// the main frame
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	
 	// the content pane for the main frame
 	Container contentPane = frame.getContentPane();
 	contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
 
 	contentPane.add(Box.createRigidArea(new Dimension(20,20)));
 
 
 
 	// make panel to select input file
         JPanel infilePanel = new JPanel(new FlowLayout());
 	contentPane.add(infilePanel);
 	infilePanel.add(new JLabel("Input File:"));
 	JButton jInfileButton = new JButton("Select File");
 	infilePanel.add(jInfileButton);
 	jInfileButton.addActionListener(new ActionListener() {
 		// code to run when file button is clicked on
 		public void actionPerformed(ActionEvent e) {
 		    JFileChooser fileChooser = new JFileChooser(lastfile);
 		    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		    int rVal = fileChooser.showOpenDialog(null);
 		    if (rVal == JFileChooser.APPROVE_OPTION) {
 			jInfile.setText(fileChooser.getSelectedFile().toString());
 			lastfile = new String(jInfile.getText());
 		    }
 		}
 	    });
 	infilePanel.add(jInfile);
 
 	contentPane.add(Box.createRigidArea(new Dimension(20,20)));
 
 
 	// create panel to select tuning scheme and root note
 	JPanel tuningschemePanel = new JPanel(new FlowLayout());
 	contentPane.add(tuningschemePanel);
 	JLabel tuningLabel = new JLabel("Tuning Scheme:");
 
 	tuningschemePanel.add(tuningLabel);
 	ArrayList<String> tuninglist = new ArrayList<String>();
 	try{
 	    tuninglist = gettuninglist();
 	} catch (IOException e) {
 	    System.exit(1);
 	}
 
 	while(!tuninglist.isEmpty()){
 	    jTunings.addItem(tuninglist.remove(0));
 	}
 	jTunings.setSelectedIndex(1);
 	tuningschemePanel.add(jTunings);
 
 	tuningschemePanel.add(Box.createRigidArea(new Dimension(20,1)));
 	JPanel rootnotePanel = tuningschemePanel;
 	contentPane.add(rootnotePanel);
 	rootnotePanel.add(new JLabel("Root Note:"));
 	for(int i=0; i<notes.length; i++) {
 	    jNotes.addItem(notes[i]);
 	}
 	rootnotePanel.add(jNotes);
 
 	contentPane.add(Box.createRigidArea(new Dimension(20,20)));
        
 	JPanel bendrangeSuperPanel = new JPanel(new FlowLayout());
 	contentPane.add(bendrangeSuperPanel);
 	JPanel bendrangePanel = new JPanel();
 	
 	bendrangePanel.setLayout(new BoxLayout(bendrangePanel,BoxLayout.Y_AXIS));
 	bendrangePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
         bendrangeSuperPanel.add(bendrangePanel);
 
 	//        bendrangePanel.setBackground(Color.BLUE);
 	JPanel bendrangeLabelPanel = new JPanel(new FlowLayout());
 	bendrangePanel.add(bendrangeLabelPanel);
         bendrangeLabelPanel.add(new JLabel("These settings usually should not be changed - see Help."));
 	
 	JPanel bendrangeSubPanel = new JPanel(new FlowLayout());
 	//	bendrangeSubPanel.setBackground(Color.BLUE);
         
 	bendrangePanel.add(bendrangeSubPanel);
 	for(int i=1; i<12; i++){
 	    jBendRange.addItem(new Integer(i));
 	}
 	jBendRange.setSelectedIndex(1);
 		
 	bendrangeSubPanel.add(new JLabel("Max Bend"));
 	bendrangeSubPanel.add(jBendRange);
 	
 	jBendRangeCommand.setSelected(true);
 	bendrangeSubPanel.add(jBendRangeCommand);
 
 	contentPane.add(Box.createRigidArea(new Dimension(20,20)));
 
 	// panel for help button and button to save modified midi file
 	JPanel goPanel = new JPanel(new FlowLayout());
 	contentPane.add(goPanel);
 	JButton jHelpButton = new JButton("Help");
 	goPanel.add(jHelpButton);
 	jHelpButton.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    displaymessage(versionstring+"\n"+licensestring+"\n\n"+description+"\n\n"+guiusage);
 		}
 	    });
 
 	goPanel.add(Box.createRigidArea(new Dimension(150,1)));
 
 	JButton jGoButton = new JButton("Generate and save re-tuned midi file");
 	goPanel.add(jGoButton);
 	jGoButton.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    JFileChooser fileChooser = new JFileChooser(lastfile);
 		    String suggestedfilename = new String(jInfile.getText());
 		    int lastslash = suggestedfilename.lastIndexOf('/');
 		    if(lastslash >= 0){
 			suggestedfilename = suggestedfilename.substring(lastslash+1);
 		    }
 		    lastslash = suggestedfilename.lastIndexOf('\\');
 		    if(lastslash >=0){
 			suggestedfilename = suggestedfilename.substring(lastslash+1);
 		    }
 		    lastslash = suggestedfilename.lastIndexOf('.');
 		    if(lastslash >=0){
 			suggestedfilename = suggestedfilename.substring(0,lastslash-1);
 		    }		    
 		    suggestedfilename = new String(suggestedfilename+"_"+(String)jTunings.getSelectedItem()+".mid");
 		    fileChooser.setSelectedFile(new File(suggestedfilename));
 		    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		    int rVal = fileChooser.showSaveDialog(null);
 		    if (rVal == JFileChooser.APPROVE_OPTION) {
 			try{
 			    // save selected file in string "lastfile"
 			    lastfile = new String(fileChooser.getSelectedFile().toString());
 			    // make sure it has the proper extension for a midi file
 			    if(!lastfile.toLowerCase().endsWith(".mid")){
 				lastfile = new String(lastfile+".mid");
 			    }
 			    // make and save the re-tuned midi file
 			    makethefile(jInfile.getText(),lastfile,jNotes.getSelectedIndex(),jTunings.getSelectedItem().toString(), (int)jBendRange.getSelectedItem(),jBendRangeCommand.isSelected());
 			    
 			} catch (Exception ee) {}
 		    }
 		}
 	    });
 
 	contentPane.add(Box.createRigidArea(new Dimension(20,20)));
 	
 	// make panel to display software version and licensing info
 	JPanel infoPanel = new JPanel(new FlowLayout());
 	contentPane.add(infoPanel);
 	JTextArea infotextarea = new JTextArea(licensestring);
 	infotextarea.setColumns(70);
 	infotextarea.setLineWrap(true);
 	infotextarea.setWrapStyleWord(true);
 	Font thefont = new Font("Times Roman", Font.ITALIC, 8);
 	infotextarea.setFont(thefont);
 	infoPanel.add(infotextarea);
 
 	contentPane.add(Box.createRigidArea(new Dimension(2,2)));
 	
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
 
 
     public static ArrayList<String> gettuninglist() throws IOException {
 	// reads tuning scheme titles from file
 	ArrayList<String> tuninglist = new ArrayList<String>();
        
 	try{
 	    BufferedReader in = new BufferedReader(new FileReader(tuningfilename));
 	    String line;
 	    while((line=in.readLine()) != null) {
 		line = line.trim();
 		if(line.length()>0){
 		    if(!line.startsWith("#")){
 			tuninglist.add(line);
 			for(int i = 0; i < 12; i++){
 			    line=in.readLine();
 			}
 		    }
 		}
 	    }
 	    in.close();
 	} catch (IOException e) {
 	    displayerror("Problem reading tunings file "+tuningfilename);
 	}
 	return(tuninglist);
     }
 
     public static int[] loadpitchshifts(String tuningscheme, double bendrange) throws IOException {
 	// read tunings file and calculate correct pitch shifts for selected tuning scheme
 	// bendrange is in semitones - ie, if bend goes +/- 2 semitones, bendrange will be 2
 	
 	int pitchshift[] = {0,0,0,0,0,0,0,0,0,0,0,0};
 	double pitchratios[] = {0,0,0,0,0,0,0,0,0,0,0,0};
 
 	try{
 	    BufferedReader in = new BufferedReader(new FileReader(tuningfilename));
 	    String line;
 	    while((line=in.readLine()) != null) {
 		line = line.trim();
 		if(line.equals(tuningscheme)){
 		    for(int i=0; i<12; i++){
 			line = in.readLine();
 			line = line.trim();
 			pitchratios[i] = Double.parseDouble(line);
 		    }
 		    break;
 		}
 	    }
 	    if(line == null){
 		displayerror("Tuning scheme "+tuningscheme+" not found");
 		haderror = true;
 	    }
 	} catch (IOException e) {
 	    displayerror("Problem reading tunings file");
 	    haderror = true;
 	}   
 
 	// midi specs use a numerical value from (0 to 16383) with 8192 meaning no bend
 	
 	for(int nnote=0; nnote < 12; nnote++){
 	    // convert frequency ratio from file into a midi pitch shift parameter
 	    pitchshift[nnote] = 8192+(int)(8191.0*((12.0*Math.log(pitchratios[nnote])/Math.log(2))-nnote)/bendrange);
 	}
 	return(pitchshift);
     }
 
     public static void makethefile(String infile, String outfile, int rootnote, String temperamentstring, double maxpitchbend, Boolean writemaxpitchbend)  throws MidiUnavailableException, InvalidMidiDataException, IOException {
 	// create and save tuned midi file
 	// infile is the midi file to read in, outfile is the path for the modified file to be saved, rootnote is the note to base the tuning scheme on, temperamentstring is a string that tells what tuning scheme to use, maxpitchbend is the number of semitones the target midi synth can bend a note
 
 	// write a string saying what's happening here
 	System.out.println(infile+"   "+outfile+"   "+rootnote+"   "+temperamentstring+"   "+maxpitchbend);
 
 	int pitchshift[] = {0,0,0,0,0,0,0,0,0,0,0,0};
 
 	// load the pitch shift values for each channel
 	try{
 	    pitchshift = loadpitchshifts(temperamentstring,maxpitchbend);
 	} catch (IOException e){}
 	
 	if(haderror){
 	    haderror = false;
 	    return;
 	}
 
 	// open up the input midi file
         Sequence midiFile = null;
         try {
             midiFile = MidiSystem.getSequence(new File(infile));
         } catch (Exception e) {
             displayerror("Error opening midi file "+infile+" for reading");
 	    return;
         }
 
 	// get the tracks from the midi file
 	Track[] tracks = midiFile.getTracks();
 	if(tracks.length == 0){
 	    displayerror("Input file does not appear to contain any tracks!");
 	    return;
 	}
 
 	// make a new midi sequence to store the modified midi stream
 	Sequence newMidi = null;
 	try{
 	    newMidi = new Sequence(midiFile.getDivisionType(),midiFile.getResolution());
 	} catch (Exception e){
 	    displayerror("Error generating new midi sequence");
 	    return;
 	}
     
 	// this will be used to make midi events on new midi sequenc
 	ShortMessage sm = null;
 
 	// go through all of the tracks in the input file
 
 	for(int ntrack = 0; ntrack < tracks.length; ntrack++){
 
 	    // Make a new track in the new midi sequence where we can store events from the corresponding track in the input file
 	    Track thisTrack = newMidi.createTrack();
 
 	    // put pitch bends onto every channel in this track to achieve correct tuning
 	    int realchannel=0;
 	    int fromroot=0;
 	    int nchannel;
 	    for(nchannel = 0; nchannel < 12; nchannel++){
 		// midi channel 10 is reserved for percussion - but the midi "parlance" numbers the channels from 1, while the file
 		// format numbers them from 0.  So in this code the percussion channel is really 9.  We want to avoid that one, so
 		// for any channel greater than 8, we will go up a channel (thereby using channels 0,1,2,3,4,5,6,7,8,10,11)
 		realchannel=nchannel;
 		if(nchannel > 8){
 		    realchannel = realchannel+1;
 		}
 		// We're going to assume that channel 0 has all of the c's, channel 1 has all of the c#'s, etc.
 		// But our array of pitch shifts start at the root.  If the root we selected isn't c, then we don't
 		// want to use pitchshift[0] for channel 0.  The variable "fromroot" tells us how many half steps 
 		// the notes on the current channel are from the root note, so we can use the correct pitch shift.
 		fromroot = nchannel - rootnote;
 		if(fromroot < 0){
 		    fromroot = fromroot + 12;
 		}
 		// Midi pitch shifts are written in two 7-bit bytes.  The code below calculates those two bytes from the pitch shift number
 		int pitchshiftlsb = pitchshift[fromroot] & 0x7F;
 		int pitchshiftmsb = (pitchshift[fromroot]>>7) & 0x7F;
 		// Write the correct pitch shift value to the midi channel
 		if(writemaxpitchbend){
 		    // Set the pitch bend range
 		    sm = new ShortMessage();
 		    sm.setMessage(ShortMessage.CONTROL_CHANGE, realchannel, 101,0);
 		    thisTrack.add(new MidiEvent(sm,0));
 		    sm = new ShortMessage();
 		    sm.setMessage(ShortMessage.CONTROL_CHANGE, realchannel, 100,0);
 		    thisTrack.add(new MidiEvent(sm,0));
 		    sm = new ShortMessage();
 		    sm.setMessage(ShortMessage.CONTROL_CHANGE, realchannel, 6,(int)maxpitchbend);
 		    thisTrack.add(new MidiEvent(sm,0));
 		    sm = new ShortMessage();
 		    sm.setMessage(ShortMessage.CONTROL_CHANGE, realchannel, 38,0);
 		    thisTrack.add(new MidiEvent(sm,0));
 		    // reset controllers 101 and 100 so that future writes to controller 6 don't change pitch bend range
 		    sm = new ShortMessage();		    
 		    sm.setMessage(ShortMessage.CONTROL_CHANGE, realchannel, 100,127);
 		    thisTrack.add(new MidiEvent(sm,0));
 		    sm = new ShortMessage();
 		    sm.setMessage(ShortMessage.CONTROL_CHANGE, realchannel, 101,127);
 		    thisTrack.add(new MidiEvent(sm,0));
 		}
 		sm = new ShortMessage();
 		sm.setMessage(ShortMessage.PITCH_BEND, realchannel, pitchshiftlsb, pitchshiftmsb);
 		thisTrack.add(new MidiEvent(sm,0));	
 	    }
 	    
 	    // go through each event in the current midi track in the input file, adjust the event, and put it into the new track
 	    for(int nevent = 0; nevent < tracks[ntrack].size(); nevent++){	   
 		MidiEvent thisevent = tracks[ntrack].get(nevent);
 		MidiMessage message = thisevent.getMessage();
                 int statusInt = (int)message.getStatus();
                 int channel = (statusInt & 0x000F)+1;
 		
 		// Each event has an event type.  What we do with it depends on what type of event it is.
 		int eventtype = (statusInt & 0x00F0)>>4; 
 		int newchannel = 0;
                 switch (eventtype) {
 		    // First deal with events which only affect one note
 		    // these are the midi events which only affect one note
 		    // hex   midi event        param 1              param 2
 		    // 0x8 = note off          note number          velocity
 		    // 0x9 = note on           note number          velocity
 		    // 0xA = note aftertouch   note number          aftertouch value
 		    
 		case 0x8:
 		    // note off       
 		    int note = (int)message.getMessage()[1] & 0xFF;
 		    sm = new ShortMessage();
 		    // find the new channel based on which note is being turned off, stored in byte 1 of the message
 		    // uses modulo 12, because we don't care what it's octave is
 		    newchannel = ((int)message.getMessage()[1] & 0xFF) % 12;
 		    // move channels 10 and above to avoid using channel 10, which is reserved for percussion
 		    // note that the channels are colloquially labelled 1-16, but the actual values in the midi file go from 0-15
 		    // so the channel we are avoiding, channel 10, is actually channel 9
 		    if(newchannel > 8){
 			newchannel = newchannel + 1;
 		    }
 		    // add adjusted event to new track
 		    sm.setMessage(ShortMessage.NOTE_OFF, newchannel, ((int)message.getMessage()[1] & 0xFF), ((int)message.getMessage()[2] & 0xFF));
 		    thisTrack.add(new MidiEvent(sm,thisevent.getTick()));			
 		    break;
 		case 0x9:
 		    // note on
 		    // see notes for note off above
 		    sm = new ShortMessage();
 		    newchannel = ((int)message.getMessage()[1] & 0xFF) % 12;
 		    if(newchannel > 8){
 			newchannel = newchannel + 1;
 		    }
 		    sm.setMessage(ShortMessage.NOTE_ON, newchannel, ((int)message.getMessage()[1] & 0xFF), ((int)message.getMessage()[2] & 0xFF));
 		    thisTrack.add(new MidiEvent(sm,thisevent.getTick()));
 		    break;
 		case 0xA:		      
 		    // note aftertouch
 		    // see notes for note off above
 		    sm = new ShortMessage();
 		    newchannel = ((int)message.getMessage()[1] & 0xFF) % 12;
 		    if(newchannel > 8){
 			newchannel = newchannel + 1;
 		    }
 		    sm = new ShortMessage();
 		    sm.setMessage(ShortMessage.POLY_PRESSURE, newchannel, ((int)message.getMessage()[1] & 0xFF), ((int)message.getMessage()[2] & 0xFF));
 		    thisTrack.add(new MidiEvent(sm,thisevent.getTick()));
 		    break;
 		    
 		    // Now we'll deal with events that affect the entire channel.  First we need to stop any pitch bend from being copied over
 		    // since that will mess up our tuning.
 		case 0xE:
 		    // pitch bend - we won't copy this one over
 		    break;
 		    
 		    // Now we'll deal with other channel-wide events
 		    // hex   midi event          param 1              param 2
 		    // 0xB = controller change   controller           value
 		    // 0xC = program change      new program          ---
 		    // 0xD = channel pressure    new pressure         ---
 		    
 		    // since we're moving notes into different channels, it's probably best to 
 		    // duplicate these events onto all of the channels used (0,1,2,3,4,5,6,7,8,10,11,12).
 		    
 		    
 		case 0xB:
 		case 0xC:
 		case 0xD:
 		    // copy event to all channels
 		    for(nchannel = 0; nchannel < 12; nchannel++){
 			realchannel = nchannel;
 			if(realchannel > 8){
 			    realchannel++;
 			}
 			sm = new ShortMessage();
 			if(message.getMessage().length < 3){
 			    sm.setMessage(eventtype<<4, realchannel, ((int)message.getMessage()[1] & 0xFF), 0);
 			}
 			else{
 			    sm.setMessage(eventtype<<4, realchannel, ((int)message.getMessage()[1] & 0xFF), ((int)message.getMessage()[2] & 0xFF));
 			}
 			thisTrack.add(new MidiEvent(sm,thisevent.getTick()));			
 		    }
 		    break;
 		    
 		    // Anything else must not be channel specific, so we'll just copy it over
 		default:
 		    thisTrack.add(thisevent);
 		    break;
 		}
 	    }	
 	}
 	// Now write the new midi file
 	try{
 	    MidiSystem.write(newMidi,1,new File(outfile));
 	} catch (IOException e) {
 	    displayerror("Problem writing to file "+outfile);
 	    return;
 	}
 	displaymessage("Done!  New midi file written to "+outfile);
     }
     
 
     public static void printUsageAndExit() {
 	System.out.println(usage);
 	System.exit(1);
     }
     
     public static void printHelpAndExit() {
 	System.out.println(description);
 	System.out.println();
 	printUsageAndExit();
     }
     
 }
