 package net.beadsproject.beads.analysis;
 import java.io.File;
 import java.util.Arrays;
 import java.util.Random;
 
 import javax.sound.sampled.AudioFileFormat;
 
 import net.beadsproject.beads.analysis.AudioAnalyser;
 import net.beadsproject.beads.analysis.SegmentListener;
 import net.beadsproject.beads.analysis.featureextractors.PeakDetector;
 import net.beadsproject.beads.core.AudioContext;
 import net.beadsproject.beads.core.TimeStamp;
 import net.beadsproject.beads.data.Sample;
 import net.beadsproject.beads.events.AudioContextStopTrigger;
 import net.beadsproject.beads.ugens.Envelope;
 import net.beadsproject.beads.ugens.Gain;
 import net.beadsproject.beads.ugens.GranularSamplePlayer;
 import net.beadsproject.beads.ugens.Recorder;
 import net.beadsproject.beads.ugens.SamplePlayer;
 
 /**
  * Music Remixer is an experimental project that uses Beads to remix a collection of music files.
  * 
  * @author ben
  */
 public class MusicRemixer {
 	
 	// length of segments
 	static float wantedLength = 200; // desired length
 	static float deviation = 1000; // gaussian deviation allowed
 	
 	// length of final file
 	static final float lengthMs = 300000;
 	
 	final static String musicHome = "/Users/ollie/Music/iTunes/iTunes Music/";
 	
 	// list of directories from which to find the music files
 	final static String[] dirs = {
 		//"D:/Music/Mozart Discography (5 CDs) 320kbps/Mozart - Horn Concerto No.1-3 - Oboe Concerto in C major",
 		//"D:/Music/8-bit/sabrepulse", 
 		musicHome + "/Burnt Friedman & The Nu Dub Players/Just Landed",
 		musicHome + "/Tom Arthurs' Subtopia/Live At Kings Place, 18.10.08_",
 //		musicHome + "/Deerhoof/Offend Maggie",
 		musicHome + "/icarus/Sylt Remixes",
 		musicHome + "/Podcasts/NewsPod"
 		};	
 	final static String outputFile = "/Users/ollie/Desktop/remix_big.wav";	
 	
 	public static void main(String[] args) throws Exception {
 		Sample outputSample = null;		
 		
 		// for each audiofile in directory dir, 
 		//  load the mp3, 
 		//  locate a longish segment on an onset, 
 		//  then write it out to a sample		
 		String[] files = allFilesInDir(dirs[0]);
 		for(int i=1;i<dirs.length;i++)
 		{
 			String[] newfiles = allFilesInDir(dirs[i]);
 			int oldlength = files.length;			
 			String[] tempFiles = new String[newfiles.length+files.length];
 			System.arraycopy(files, 0, tempFiles, 0, files.length);
 			files = tempFiles;
 			System.arraycopy(newfiles, 0, files, oldlength, newfiles.length);
 		}
 		
 		while(true)		
 		{
 			String file = files[(int)(Math.random()*files.length)];
 			System.out.println("Processing file: " + file);
 			System.out.printf("Searching for segment of size approx %fms\n",wantedLength);
 			
 			try {
 				Sample segment = extractSegment(file);
 				if (segment!=null)
 				{
 					if (outputSample==null)
 					{
 						outputSample = segment;
 					}
 					else
 					{
 						// concatenate this sample with the big sample							
 						AudioContext ac = new AudioContext();
 						Recorder r = new Recorder(ac,outputSample,Recorder.Mode.INFINITE);
 						r.setPosition(outputSample.getLength());
 						SamplePlayer sp = Math.random() < 0.8f ? new GranularSamplePlayer(ac,segment) : new SamplePlayer(ac,segment);		
 //						sp.setKillOnEnd(true);
 						float rate = (float)Math.random() * 0.2f + 0.9f;
 						sp.getRateEnvelope().setValue(rate);
 						Envelope gainEnv = new Envelope(ac, 0f);
 						Gain g = new Gain(ac, 2, gainEnv);
 //						sp.setKillListener(new AudioContextStopTrigger(ac));
 						g.addInput(sp);
 						
 						gainEnv.addSegment(1f, 10f);
 						gainEnv.addSegment(1f, segment.getLength() / rate - 40f);
 						gainEnv.addSegment(0f, 10f, new AudioContextStopTrigger(ac));
 						
 						r.addInput(g);
 						ac.out.addDependent(r);
 //						ac.out.addDependent(sp); 
 						
 						ac.runNonRealTime();
 						r.clip();
 					}
 				}
 			}
 			catch (Exception e)
 			{} // ignore exceptions!
 			
 			if (outputSample!=null && outputSample.getLength()>lengthMs)
 				break;
 		}	
 		
 		if (outputSample!=null)
 			outputSample.write(outputFile,AudioFileFormat.Type.WAVE);	
 		else
 			System.out.println("Sorry, could not find any acceptable audio files in that folder.");
 	}
 	
 	
 	public static class SegmentExtractor implements SegmentListener
 	{	
 		float waited;
		final float WAIT_TIME = 100;
 		final float STOP_TIME = 100000;		
 				
 		AudioContext ac;
 		Random r;
 		
 		public boolean foundSegment = false;
 		public TimeStamp start, end;
 				
 		public SegmentExtractor(AudioContext ac)
 		{
 			this.ac = ac;
 			waited = 0;
 			r = new Random();
 		}
 
 		public void newSegment(TimeStamp start, TimeStamp end) {
 			double length = end.getTimeMS() - start.getTimeMS();
 			if (waited > WAIT_TIME)
 			{
 				// depending on the size of the segment, we may or may not choose it
 				double f = Math.abs(r.nextGaussian()); // f defines an acceptable band for length to fall into
 				double upper = f*deviation + wantedLength;
 				double lower = Math.min(wantedLength - f*deviation, 10f);
 								
 				if (length < upper && length > lower)
 				{
 					// then stop the ac, and record the start and end times...
 					ac.stop();
 					this.start = start;
 					this.end = end;
 					foundSegment = true;
 				}			
 			}
 			else if (waited > STOP_TIME)
 			{
 				ac.stop();
 				foundSegment = false;
 			}
 			
 			waited += length;
 		}		
 	}
 	
 	
 	/**
 	 * Looks for a segment (bounded by onsets) in the audio file, and returns it in a Sample. 
 	 * @param filename
 	 * @return
 	 * @throws Exception 
 	 */
 	public static Sample extractSegment(String filename) throws Exception
 	{
 		
 		AudioContext ac = new AudioContext();
 		
 		AudioAnalyser analyser = new AudioAnalyser(ac);
 		ac.out.addDependent(analyser);
 		PeakDetector pd = analyser.peakDetector();
 		pd.setThreshold(0.2f);
 		pd.setAlpha(.9f);
 		
 		Sample audioSample = new Sample(filename, Sample.Regime.newStreamingRegime(100));
 		if (audioSample==null) return null;		
 		float lengthMs = audioSample.getLength();
 		assert lengthMs>0;
 				
 		// skip about halfway, then locate a longish segment ...
 		SamplePlayer sp = new SamplePlayer(ac, audioSample);
 		sp.setPosition(Math.random()*lengthMs);		
 		SegmentExtractor se = new SegmentExtractor(ac);
 		pd.addSegmentListener(se);
 		
 		analyser.addInput(sp);
 		sp.setKillListener(new AudioContextStopTrigger(ac));		
 		ac.runNonRealTime();
 		ac.out.clearInputConnections();
 		
 		// once ac has finished, then we'll have an appropriate segment (hopefully!)
 		if (!se.foundSegment) return null;
 		
 		// else
 		
 		double start = se.start.getTimeMS();
 		double end = se.end.getTimeMS();
 		
 		System.out.printf("Segment at [%.2f,%.2f]\n",start,end);
 		
 		// now extract that segment and return it...
 		Sample output = new Sample(ac.getAudioFormat(), end - start); // 10 ms long initially, will get resized by recorder
 		
 		Recorder r = new Recorder(ac,output);
 		r.setMode(Recorder.Mode.FINITE);
 		ac.out.addDependent(r);
 		
 		audioSample = new Sample(filename, Sample.Regime.newStreamingRegime(100));
 		sp = new SamplePlayer(ac, audioSample);		
 		sp.setPosition(start);
 		sp.start();
 		r.addInput(sp);
 		
 		r.setKillListener(new AudioContextStopTrigger(ac));
 		ac.runNonRealTime();
 		
 		// r.clip();
 		return output;		
 	}
 	
 	public static String[] allFilesInDir(String dir)
 	{
 		File directory = new File(dir);
 		String[] fileNameList = directory.list();
 		for (int i = 0; i < fileNameList.length; i++) {
 			fileNameList[i] = directory.getAbsolutePath() + "/" + fileNameList[i];
 		}
 		return fileNameList;
 	}
 
 }
