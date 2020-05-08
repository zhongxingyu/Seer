 package ZeroFrame.Analysis;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import edu.cmu.sphinx.frontend.*;
 import edu.cmu.sphinx.frontend.util.StreamDataSource;
 import edu.cmu.sphinx.frontend.util.Utterance;
 import edu.cmu.sphinx.recognizer.Recognizer;
 import edu.cmu.sphinx.result.Result;
 import edu.cmu.sphinx.util.props.ConfigurationManager;
 
 public class VoiceAnalyzer {
 	
 	ConfigurationManager configurationManager;
 	Recognizer recognizer;
 	
 	public VoiceAnalyzer(){
 		configurationManager = new ConfigurationManager("sphinxserver.config.xml");
 		recognizer = (Recognizer) configurationManager.lookup("recognizer");
 		recognizer.allocate();
 	}
 	
 	public void analyzeUtterance(byte[] utteranceStream, ZeroFrame.Networking.Client sendingClient){ 
 		ByteArrayInputStream tempByteArrayInputStream = new ByteArrayInputStream(utteranceStream);
		AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
 		AudioInputStream audioStream = null;
		audioStream = new AudioInputStream(tempByteArrayInputStream, format, utteranceStream.length);		
         StreamDataSource recognizerStreamSource = (StreamDataSource) configurationManager.lookup("streamDataSource");
         recognizerStreamSource.setInputStream(audioStream, sendingClient.getClientName() + "-voicestream");
         Result result = recognizer.recognize();
 		if (result != null) {
 			String resultText = result.getBestFinalResultNoFiller();
 			System.out.println("You said: " + resultText + '\n');
 		} else {
 			System.out.println("I can't hear what you said.\n");
		}		
 	}
 	
 }
