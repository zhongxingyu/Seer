 package com.buglabs.bug.module.gps;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
import sun.io.MalformedInputException;

 import com.buglabs.bug.module.gps.pub.INMEASentenceProvider;
 import com.buglabs.nmea.NMEAParser;
 import com.buglabs.nmea.sentences.NMEAParserException;
 import com.buglabs.nmea.sentences.RMC;
 
 public class NMEASentenceProvider extends Thread implements INMEASentenceProvider {
 
 	private InputStream nmeaStream;
 
 	private RMC cachedRMC;
 
 	public NMEASentenceProvider(InputStream nmeaStream) {
 		this.nmeaStream = nmeaStream;
 	}
 
 	public RMC getRMC() {
 		return cachedRMC;
 	}
 
 	public void run() {
 		BufferedReader br = null;
 		
 		try {
 			br = new BufferedReader(new InputStreamReader(nmeaStream));
 			String sentence;
 			NMEAParser parser = new NMEAParser();
 			
 			do {
 				 try {
 					 sentence = br.readLine(); 
				 } catch (MalformedInputException e) {
 					 sentence = "";
 					 continue;
 				 }
 				 
 				 try {
 						Object objSentence = parser.parse(sentence);
 						
 						if(objSentence != null && objSentence instanceof RMC) {
 							cachedRMC = (RMC) objSentence;
 						}
 					} catch (NMEAParserException e) {
 						//TODO: Handle parser exceptions, atleast log it
 					}
 			} while(!Thread.currentThread().isInterrupted() && (sentence != null));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			try {
 				if(br != null) {
 					br.close();
 				} else {
 					if(nmeaStream != null) {
 						nmeaStream.close();
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
