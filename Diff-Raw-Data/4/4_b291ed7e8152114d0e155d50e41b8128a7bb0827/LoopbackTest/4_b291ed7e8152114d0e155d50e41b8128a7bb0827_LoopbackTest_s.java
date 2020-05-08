 
 import javax.sound.sampled.*;
 
 public class LoopbackTest{
 	
 	public static void main(String[] args) throws Exception{
 
 		// Set the format for the speaker and microphone
 		AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
 
 		// Get the system's default microphone
 		TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
 
 		// Get the system's default speaker
 		SourceDataLine speaker = AudioSystem.getSourceDataLine(format);
 
 		// Open streams to both mic and speaker
 		microphone.open(format);
 		speaker.open(format);
 
 		int numBytesRead;
 		byte[] data = new byte[microphone.getBufferSize() / 5];
 
 		// Start sending/receiving data
 		microphone.start();
 		speaker.start();
 
 		while (true){
 			numBytesRead = microphone.read(data, 0, data.length);
 			speaker.write(data, 0, numBytesRead);
 		}
 
 		// Clean up
//		microphone.start();
//		speaker.start();
 
 	}
 }
