 package sound;
 
 import game.GameControl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.sound.sampled.AudioFileFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 public class Jukebox {
 
 	private Map<Integer, List<Clip>> soundtrack;
 	private Map<Integer, List<BeatTrack>> beatTrack;
 	private Clip heartbeat;
 	private BeatTrack currentBeatTrack;
 	
 	public Jukebox(GameControl control) {
 		//Populate soundtrack with the music sounds
 		beatTrack = new HashMap<Integer, List<BeatTrack>>();
 		soundtrack = new HashMap<Integer, List<Clip>>();
 		for (int m=1;m<=4;m++) {
 			soundtrack.put(m, new ArrayList<Clip>());
 			for (int t=1;t<=8;t++) {
 				try {
 					File file = new File("res/sounds/EKGm"+m+"t"+t+".aiff");
 					AudioFileFormat format = AudioSystem.getAudioFileFormat(file);
 					DataLine.Info clipInfo = new DataLine.Info(Clip.class, format.getFormat());
 					Clip line = (Clip)AudioSystem.getLine(clipInfo);
 					line.open(new AudioInputStream(new FileInputStream(file), 
 							format.getFormat(), format.getFrameLength()));
 					soundtrack.get(m).add(line);
 				} catch (UnsupportedAudioFileException e) {
 					System.out.println("Unsupported audio file: " +
 							"sounds/EKGm"+m+"t"+t+".aiff");
 				} catch (IOException e) {
 					System.out.println("Failed to access sounds/EKGm"+m+"t"+t+".aiff");
 				} catch (LineUnavailableException e) {
 					System.out.println("Line unavailable: sounds/EKGm"+m+"t"+t+".aiff");
 				}
 			}
 		}
 		
 		//Create heartbeat sound
 		try {
			File heartbeatFile = new File("res/sounds/Heart_beat.aiff");
 			AudioFileFormat format = AudioSystem.getAudioFileFormat(heartbeatFile);
 			DataLine.Info clipInfo = new DataLine.Info(Clip.class, format.getFormat());
 			heartbeat = (Clip)AudioSystem.getLine(clipInfo);
 			heartbeat.open(new AudioInputStream(new FileInputStream(heartbeatFile),
 					format.getFormat(), format.getFrameLength()));
 		} catch (UnsupportedAudioFileException | IOException e) {
 			System.out.println("Unsupported audio file: Heart_beat.wav");
 		} catch (LineUnavailableException e) {
 			System.out.println("Line unavailable for Heart_beat.wav");
 		}
 		
 		//Associate BeatTracks with the motifs
 		ArrayList<BeatTrack> list = new ArrayList<BeatTrack>();
 		list.add(new BeatTrack("10100000101000001010000010100000"));
 		list.add(new BeatTrack("10001000100010001000100010001000"));
 		list.add(new BeatTrack("10001010100010101000101010001010"));
 		list.add(new BeatTrack("11001010110010101100101011001010"));
 		beatTrack.put(1, list);
 		list = new ArrayList<BeatTrack>();
 		list.add(new BeatTrack("11000000110000001100000011000000"));
 		list.add(new BeatTrack("11001100110011001100110011001100"));
 		list.add(new BeatTrack("10000000100000001001100010000000"));
 		list.add(new BeatTrack("11000000110000001101100011001100"));
 		beatTrack.put(2, list);
 		list = new ArrayList<BeatTrack>();
 		list.add(new BeatTrack("10001010100010101000101010001010"));
 		list.add(new BeatTrack("10000010100000101000001010000010"));
 		list.add(new BeatTrack("10001000001010001000100000101000"));
 		list.add(new BeatTrack("10111000101010001010100010111000"));
 		beatTrack.put(3, list);
 		list = new ArrayList<BeatTrack>();
 		list.add(new BeatTrack("10000000100000001000000010001000"));
 		list.add(new BeatTrack("11000000110000001100000011001100"));
 		list.add(new BeatTrack("10001010101000101000101010100010"));
 		list.add(new BeatTrack("11001010101000111000101011001010"));
 		beatTrack.put(4, list);
 		for (List<BeatTrack> l : beatTrack.values()) {
 			for (BeatTrack t : l){
 				t.addBeatListener(control);
 			}
 		}
 	}
 	
 	public void stopMusic() {
 		for (List<Clip> l : soundtrack.values()) {
 			for (Clip c : l) {
 				c.stop();
 			}
 		}
 	}
 	
 	public void playMotif(int motif, int intensity) {
 		List<Clip> m = soundtrack.get(motif);
 		for (int i=0;i<intensity;i++){
 			Clip c = m.get(i);
			c.setMicrosecondPosition(300000);
 			c.start();
 		}			
		//playBeatTrack(motif);
 	}
 	
 	public void playBeatTrack(int motif) {
 		int index = (int)Math.floor(Math.random()*beatTrack.get(motif).size());
 		currentBeatTrack = beatTrack.get(motif).get(index);
 		currentBeatTrack.play(System.currentTimeMillis());
 		System.out.println("Playing beat track " + index + " for motif " + motif);
 	}
 	
 	public void playHeartBeat() {
 		heartbeat.setMicrosecondPosition(0);
 		heartbeat.start();
 	}
 	
 	public void update(float tpf) {
 		if (currentBeatTrack != null) {
 			currentBeatTrack.update(System.currentTimeMillis());
 		}
 	}
 }
