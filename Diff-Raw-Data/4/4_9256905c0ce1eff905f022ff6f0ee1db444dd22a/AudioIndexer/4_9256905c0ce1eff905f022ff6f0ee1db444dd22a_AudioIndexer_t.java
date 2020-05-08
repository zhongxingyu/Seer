 import java.io.BufferedInputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.sound.sampled.DataLine.Info;
 
 
 public class AudioIndexer {
 	public static void main(String[]args) {
 		AudioIndexer ai = new AudioIndexer();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public AudioIndexer() {
 
 		
 		String[] filenames = new String[12];
 		filenames[0] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo1/vdo1.wav";
 		filenames[1] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo2/vdo2.wav";
 		filenames[2] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo3/vdo3.wav";
 		filenames[3] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo4/vdo4.wav";
 		filenames[4] = "C:/Users/Cauchy/Documents/CSCI576/project/vdo5/vdo5.wav";
 		filenames[5] = "C:/Users/Cauchy/Documents/CSCI576/project/vdo6/vdo6.wav";
 		filenames[6] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo7/vdo7.wav";
 		filenames[7] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo8/vdo8.wav";
 		filenames[8] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo9/vdo9.wav";
 		filenames[9] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo10/vdo10.wav";
 		filenames[10] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo11/vdo11.wav";
 		filenames[11] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo12/vdo12.wav";
 		
 		try {
 			AudioFormat audioFormat;
 			AudioInputStream audioInputStream = null;
 			Info info;
 			SourceDataLine dataLine;
 			int bufferSize;
 			int readBytes = 0;
 			byte[] audioBuffer;
 			
 			FileWriter fw = new FileWriter("audioindex.txt");
 			BufferedWriter bw = new BufferedWriter(fw);
 			FileInputStream inputStream;
 			
 			
 			for (int i = 0; i < 12; i++) {
 				File file = new File(filenames[i]);
 				
 				bw.write("" + i + " ");
 				
 				int audiolen = (int)file.length();
 				bufferSize = (int) Math.round((double) audiolen * 42.0 / 30000.0);
 				int oneFrameSize = audiolen/720;
				System.out.println("index: " + i);
 				System.out.println("oneFrameSize: " + oneFrameSize);
 				System.out.println("audiolen: " + audiolen);
 				System.out.println("bufferSize: " + bufferSize);
 				audioBuffer = new byte[audiolen];
 						
 				inputStream = new FileInputStream(file);
 				InputStream bufferedIn = new BufferedInputStream(inputStream);
 				audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
 				
 				// Obtain the information about the AudioInputStream
 				audioFormat = audioInputStream.getFormat();
 				//System.out.println(audioFormat);
 				info = new Info(SourceDataLine.class, audioFormat);
 				
 				// opens the audio channel
 				dataLine = null;
 				try {
 					dataLine = (SourceDataLine) AudioSystem.getLine(info);
 					dataLine.open(audioFormat, bufferSize);
 				} catch (LineUnavailableException e1) {
 					System.out.println(e1);
 					//throw new PlayWaveException(e1);
 				}
 				
 				readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
 				
 				int temp[] = new int[oneFrameSize];
 				int index = 0;
 				int count = 0;
 				int tempMax = 0;
 				
 				for (int audioByte = 0; audioByte < audioBuffer.length;)
 			    {
 			        //for (int channel = 0; channel < nbChannels; channel++)
 			        //{
 			            // Do the byte to sample conversion.
 			            int low = (int) audioBuffer[audioByte];
 			            audioByte++;
 			            if (audioByte < audioBuffer.length) {
 				            int high = (int) audioBuffer[audioByte];
 				            audioByte++;
 				            int sample = (high << 8) + (low & 0x00ff);
 				            temp[count] = sample;
 				            //bw.write("" + sample + " ");
 			            }
 			            //System.out.println(sample);
 			            //toReturn[index] = sample;
 			        //}
 			        count++;
			        if ((audioByte) % oneFrameSize == 0 || (audioByte + 1) % oneFrameSize == 0) {
 			        	count = 0;
 			        	int maxVal = maxValue(temp);
 			        	if (maxVal > tempMax)
 			        		tempMax = maxVal;
 			        	
 			        	//System.out.println(maxVal);
 			        	
 			        	bw.write("" + maxVal/1024 + " ");
 			        }
 			        
 			        index++;
 			    }
 				System.out.println(tempMax);
 				bw.write("\n");
 				
 			}
 			bw.close();
 		} catch (UnsupportedAudioFileException e1) {
 		    System.out.println(e1);
 		} catch (IOException e1) {
 		    System.out.println(e1);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 	public int maxValue(int[] chars) {
         int max = chars[0];
         for (int ktr = 0; ktr < chars.length; ktr++) {
                 if (chars[ktr] > max) {
                         max = chars[ktr];
                 }
         }
         return max;
 	}
 	
 	public int averageValue(int[] nums) {
 		int sum = 0;
 		for (int i = 0; i < nums.length; i++) {
 			sum += nums[i];
 		}
 		return sum / nums.length;
 	}
 	
 	public int[] getUnscaledAmplitude(byte[] eightBitByteArray)
 	{
 	    int[] toReturn = new int[eightBitByteArray.length / 2];
 	    int index = 0;
 
 	    for (int audioByte = 0; audioByte < eightBitByteArray.length;)
 	    {
 	        //for (int channel = 0; channel < nbChannels; channel++)
 	        //{
 	            // Do the byte to sample conversion.
 	            int low = (int) eightBitByteArray[audioByte];
 	            audioByte++;
 	            int high = (int) eightBitByteArray[audioByte];
 	            audioByte++;
 	            int sample = (high << 8) + (low & 0x00ff);
 	            //System.out.println(sample);
 	            toReturn[index] = sample;
 	        //}
 	        index++;
 	    }
 
 	    return toReturn;
 	}
 }
