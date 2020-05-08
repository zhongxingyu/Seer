 package fr.istic.synthlab;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import fr.istic.synthlab.abstraction.module.out.IModuleOUT;
 import fr.istic.synthlab.abstraction.synthesizer.ISynthesizer;
 import fr.istic.synthlab.command.ICommand;
 import fr.istic.synthlab.controller.synthesizer.ICSynthesizer;
 import fr.istic.synthlab.factory.impl.PACFactory;
 import fr.istic.synthlab.util.ReadXMLFile;
 import fr.istic.synthlab.util.WriteXMLFile;
 
 /**
  * Application
  */
 public class SynthApp implements ISynthApp {
 
 	private List<ICSynthesizer> synths;
 	private ICSynthesizer currentSynth;
 	private ISynthFrame frame;
 	private ICommand displayCmd;
 	private ICommand undisplayCmd;
 	private int untitledIndex = 1;
 
 	public SynthApp(ISynthFrame frame) {
 		this.frame = frame;
 		synths = new ArrayList<ICSynthesizer>();
 
 		// Create a new synthesizer instance
 		newSynthInstance();
 	}
 
 	// Create a new synthesizer instance, set its path and frame, and add it to
 	// the list
 	private void newSynthInstance() {
 		if (synths.size() > 0)
 			currentSynth.stop();
 		ICSynthesizer newSynth = (ICSynthesizer) (PACFactory.getFactory()).newSynthesizer();
 		this.synths.add(newSynth);
 		newSynth.setPath(null, "untitled" + untitledIndex);
 		newSynth.setFrame(frame);
 		setSynthesizer(newSynth.getPath());
 		untitledIndex++;
 	}
 
 	@Override
 	public void newSynth() {
 		// Create a new instance
 		newSynthInstance();
 
 		// Display the new instance and add an OUT module
 		displayNewDefaultSynth();
 
 		// Add this new instance to the menu
 		frame.addInMenu(currentSynth);
 	}
 
 	@Override
 	public void startSynth() {
 		currentSynth.start();
 	}
 
 	@Override
 	public void quitSynth() {
 		currentSynth.stop();
 		this.currentSynth = null;
 		undisplayCmd.execute();
 		System.exit(0);
 	}
 
 	@Override
 	public void saveToXML(String fileDir, String filename) {
 		// Create a new writter
 		WriteXMLFile writeToXML = new WriteXMLFile(new File(fileDir + filename));
 
 		// Save all modules
 		writeToXML.saveModules(currentSynth.getModules());
 
 		// Set the path of this instance
 		currentSynth.setPath(fileDir, filename);

 		// Reload this instance in the menu
		frame.removeInMenu(currentSynth);
 		frame.addInMenu(currentSynth);
 	}
 
 	@Override
 	public void loadFromXML(String dir, String file) {
 		// If this file is already open, return
 		for (ICSynthesizer s : synths) {
 			String st = s.getPath()[0]+s.getPath()[1];
 			if (st.equals(dir + file)) {
 				return;
 			}
 		}
 
 		// Stop the current instance
 		currentSynth.stop();
 
 		// Create a new instance
 		newSynthInstance();
 
 		// Display the new instance
 		displayCmd.execute();
 
 		// Create a new reader
 		ReadXMLFile readXML = new ReadXMLFile(currentSynth, new File(dir + file));
 
 		// Populate the current synthesizer with the file
 		readXML.loadSynthesizer();
 
 		// Set the current path
 		currentSynth.setPath(dir, file);
 
 		// Add this instance to the menu and set the title bar
 		frame.addInMenu(currentSynth);
 	}
 
 	@Override
 	public void displayNewDefaultSynth() {
 		// Display the current instance
 		displayCmd.execute();
 
 		// Add an OUT module
 		IModuleOUT out = (PACFactory.getFactory()).newOUT(currentSynth);
 		currentSynth.add(out);
 	}
 
 	@Override
 	public void remove(ISynthesizer cSynth) {
 		// Stop the synthesizer
 		cSynth.stop();
 
 		// Remove the synthesizer from the list
 		this.synths.remove(cSynth);
 
 		// Remove the synthesizer in the menu
 		frame.removeInMenu((ICSynthesizer) cSynth);
 
 		// If we have another synthesizer loaded, we display the last one, else
 		// we create a new one
 		if (this.synths.size() > 0) {
 			setSynthesizer(this.synths.get(this.synths.size() - 1).getPath());
 			frame.displaySynth();
 		} else {
 			newSynth();
 		}
 
 		frame.selectInMenu(currentSynth);
 	}
 
 	@Override
 	public void setSynthesizer(String[] strings) {
 		// If the current synthesizer is running, stop it
 		if (currentSynth != null)
 			currentSynth.stop();
 
 		// Set the current synthesizer instance from its path
 		for (ISynthesizer synth : this.synths) {
 			if (((ICSynthesizer) synth).getPath()[0] == null) {
 				if (strings[0] == null) {
 					if (((ICSynthesizer) synth).getPath()[1].equals(strings[1])) {
 						currentSynth = (ICSynthesizer) synth;
 						return;
 					}
 				}
 			} else if ((((ICSynthesizer) synth).getPath()[0].equals(strings[0])) && ((((ICSynthesizer) synth).getPath()[1].equals(strings[1])))) {
 				currentSynth = (ICSynthesizer) synth;
 				return;
 			}
 		}
 	}
 
 	@Override
 	public ICSynthesizer getSynthesizer() {
 		if (currentSynth == null)
 			newSynthInstance();
 		return currentSynth;
 	}
 
 	public void setFrame(SynthFrame frame2) {
 		this.frame = frame2;
 	}
 
 	@Override
 	public void setDisplaySynthCommand(ICommand displaySynthCommand) {
 		this.displayCmd = displaySynthCommand;
 	}
 
 	@Override
 	public void setUndisplaySynthCommand(ICommand undisplaySynthCommand) {
 		this.undisplayCmd = undisplaySynthCommand;
 	}
 }
