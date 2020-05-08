 package applicationmeasurement;
 
 import java.awt.Dimension;
 
 /**
  * This class contains project wide configuration settings.
  * @author Roland van der Linden
  *
  */
 public class MeasurementConfig
 {
 	public final static String appname = "Affect Based Speech Synthesis Measurement";
 	public final static Dimension appsize = new Dimension(1000, 750);
 	public final static int outerBorderSize = 2;
 	
 	public final static String freeTTSSpeakerName = "kevin16"; 
 	
 	public final static long waitTimeForNextTest = 1000;
 	
 	//Parameter settings.
 	public final static float[] pitchSettings = { 60, 120, 180 };
 	public final static float[] pitchRangeSettings = { 1, 25, 50 };
 	public final static float[] speedSettings = { 80, 160, 240 };
 	public final static int sampleSize = pitchSettings.length * pitchRangeSettings.length * speedSettings.length;
 	
 	public final static String outputFilepath = "output.txt";
 	public final static String emailAddress = "[TODO INSERT EMAILADDRESS]";
 	public final static String standardExperimentText = "A device made entirely with such gates will make calculations if everything moves forward.";
 	public final static String explanatoryText = 
 			"Dear participant,\n\n"
 			+ "We would like you to listen to the affective state (the emotion) of the synthetic voice, and then indicate which affective state you recognized by selecting the associated facial expression on the affectbutton."
 			+ "Please do not base your selection on the emotion contained in the words, but base it on the emotion contained in the speech itself.\n\n"
			+ "When you press the 'Save & Continue' button, we will automatically save your result to a file. In order to process your results, we would like you to send this file to us when you are done."
 			+ "The file can be found in the location where you are running the program from. The filename is '" + outputFilepath + "'. Please send this file to '" + emailAddress + "'.\n\n"
 			+ "Some remarks to help you get started:\n"
 			+ " - Press the 'Start' button to start with the first test.\n"
 			+ " - Move over the affectbutton (the one with a face on it) to change the facial expression of the face. Left-mouse press on the affectbutton to record your selection. Pressing it again overwrites your selection.\n"
 			+ " - Press the 'Save & Continue' button if you think the selection fits with the affective state of the voice.\n"
 			+ " - Press the 'Repeat test' button if you would like to hear the same voice again.\n"
 			+ " - Press the 'Compare with neutral' button if you would like to hear a (synthetic) affective neutral voice.\n"
 			+ " - If you find it hard to select an affective state on the affectbutton; It does not record your selection if you move your mouse in between-mouse press and mouse-release.\n"
 			+ " - If you don't really recognize the affective state in the voice, please listen to it again and make the best guess you can. Please don't pick randomly.\n\n"
 			+ "This application makes use of the FreeTTS library and the AffectButton library";
 	public final static String finishedText = 
 			"Dear participant,\n\n"
 			+ "Thank you very much for your participation. We have recorded your results to a file. Please send us this file so we can process your results.\n\n"
 			+ "The name of the file is '" + outputFilepath + "', and it should have been created in the exact same directory as where you are running this program from."
 			+ "Please send the file as an attachment to emailaddress '" + emailAddress + "'.\n\n"
 			+ "Thank you!";
 }
