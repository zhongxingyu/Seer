 package linewars.display.sound;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 public class SoundPlayer implements Runnable
 {
 	private static final int SAMPLE_RATE = 44100;
 
 	/*
 	 * TODO the code in this class only supports 8 bit encoding, it will have to
 	 * be modified to handle larger sizes if this number is increased.
 	 */
 	private static final int SAMPLE_SIZE_IN_BYTES = 2;
 	
 	private static final int MIN_LOOP_TIME_MS = 1;
 	
 	private static final double LAG_SPIKE_BUFFER_PERCENTAGE = 1.85;
 	
 	private static final double MIN_BUFFER_WRITE_SIZE = 1.00;
 
 	/*
 	 * TODO if the channels are changed then the sound managers may need to
 	 * change the way that they calculate their volume.
 	 */
 	public enum Channel
 	{
 		LEFT, RIGHT
 	}
 	
 	public enum SoundType
 	{
 		MUSIC, SOUND_EFFECT
 	}
 
 	private static final Object instanceLock = new Object();
 	private static final Object runningLock = new Object();
 	private static SoundPlayer instance;
 
 	private boolean running;
 
 	private HashMap<SoundType, Double> volumes;
 	private HashMap<String, Sound> sounds;
 	private ArrayList<SoundPair> playing;
 	private AudioFormat format;
 	private SourceDataLine line;
 	private long lastTime;
 	private float loopTime;
 
 	private ActionListener buttonClickSound;
 
 	private SoundPlayer()
 	{
 		running = false;
 		volumes = new HashMap<SoundPlayer.SoundType, Double>();
 		for(SoundType t : SoundType.values())
 			volumes.put(t, 1.0);
 		
 		sounds = new HashMap<String, Sound>();
 		playing = new ArrayList<SoundPair>();
 		format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BYTES * 8, Channel.values().length, true, false);
 		loopTime = MIN_LOOP_TIME_MS;
 		
 		buttonClickSound = new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(SoundPlayer.getInstance().isRunning())
 					SoundPlayer.getInstance().playSound(new SoundInfo() {
 						
 						@Override
 						public double getVolume(Channel c) {
 							return 1.0;
 						}
 						
 						@Override
 						public String getURI() {
 							return "Menu_Click.wav";
 						}
 						
 						@Override
 						public SoundType getType() {
 							return SoundType.SOUND_EFFECT;
 						}
 					});
 			}
 		};
 	}
 	
 	public ActionListener getButtonSoundListener()
 	{
 		return buttonClickSound;
 	}
 
 	public static SoundPlayer getInstance()
 	{
 		if(instance == null)
 		{
 			synchronized(instanceLock)
 			{
 				if(instance == null)
 				{
 					instance = new SoundPlayer();
 				}
 			}
 		}
 
 		return instance;
 	}
 	
 	public boolean isRunning()
 	{
 		return running;
 	}
 	
 	public void setVolume(SoundType type, double vol)
 	{
 		if(vol < 0.0)
 			vol = 0.0;
 		if(vol > 1.0)
 			vol = 1.0;
 		
 		volumes.put(type, vol);
 	}
 
 	public void addSound(String uri) throws UnsupportedAudioFileException, IOException
 	{
 		String absURI = System.getProperty("user.dir") + "/resources/sounds/" + uri;
 		absURI = absURI.replace("/", File.separator);
 
 		File file = new File(absURI);
 		
 		AudioInputStream in = AudioSystem.getAudioInputStream(file);
 		AudioInputStream din = AudioSystem.getAudioInputStream(format, in);
 		sounds.put(uri, new Sound(din));
 	}
 
 	public void playSound(SoundInfo playMe)
 	{
 		if(playMe == null)
 			return;
 
 		playing.add(new SoundPair(0, playMe));
 	}
 	
 	public void removeAllPlayingSounds()
 	{
 		playing.clear();
 	}
 	
 	public void stop()
 	{
 		running = false;
 
		if(line == null){
			return;
		}
		
 		line.drain();
 		line.stop();
 		line.close();
 	}
 
 	@Override
 	public void run()
 	{
 		// We only want this to run once
 		if(!running)
 		{
 			synchronized(runningLock)
 			{
 				if(running)
 				{
 					return;
 				}
 				else
 				{
 					running = true;
 				}
 			}
 		}
 		else
 		{
 			return;
 		}
 
 		try
 		{
 			line = getLine(format);
 		}
 		catch(LineUnavailableException e)
 		{
 			running = false;
 			e.printStackTrace();
 			return;
 		}
 		
 		line.start();
 
 		while(running)
 		{
 			lastTime = System.currentTimeMillis();
 			
 			play();
 
 			long curTime = System.currentTimeMillis();
 			long elapsedTime = curTime - lastTime;
 			
 			if(elapsedTime < MIN_LOOP_TIME_MS)
 			{
 				try
 				{
 					Thread.sleep(MIN_LOOP_TIME_MS);
 				}
 				catch(InterruptedException e)
 				{
 					e.printStackTrace();
 				}
 				
 				curTime = System.currentTimeMillis();
 				elapsedTime = curTime - lastTime;
 			}
 			
 			loopTime = (loopTime * 0.875f) + (elapsedTime * 0.125f);
 		}
 	}
 	
 	private void play()
 	{
 		//calculate the number of bytes to write
 		int samples = (int)(SAMPLE_RATE * (loopTime / 1000.0f)) * Channel.values().length;
 		int bytes = samples * SAMPLE_SIZE_IN_BYTES;
 		
 		int calculatedBytes = bytes;
 		int bufferUsed = line.getBufferSize() - line.available();
 		bytes = (int)(calculatedBytes * LAG_SPIKE_BUFFER_PERCENTAGE) - bufferUsed;
 		
 		if(bytes < calculatedBytes * MIN_BUFFER_WRITE_SIZE)
 			bytes = (int)(calculatedBytes * MIN_BUFFER_WRITE_SIZE);
 			
 		bytes = (bytes / (SAMPLE_SIZE_IN_BYTES * Channel.values().length)) * (SAMPLE_SIZE_IN_BYTES * Channel.values().length);
 		
 		if(bytes <= 0)
 			return;
 		
 		//loop over all playing sounds and mix sounds of the same type together
 		byte[][] typeData = new byte[SoundType.values().length][bytes];
 		for(int index = 0; index < playing.size(); ++index)
 		{
 			//get the sound pair
 			SoundPair p = playing.get(index);
 			if(p == null)
 				continue;
 			
 			//get the sound
 			Sound current = sounds.get(p.sound.getURI());
 			if(current == null || p.sound.isDone() || current.isFinished(p.progress))
 			{
 				p.sound.setDone();
 				playing.remove(index);
 				continue;
 			}
 
 			//get the data from the sound
 			byte[] dataFromSource = new byte[bytes];
 			p.progress = current.getNextFrame(dataFromSource, p.progress, bytes);
 
 			//get the sound type
 			int type = p.sound.getType().ordinal();
 			
 			//get the sound volume
 			double vol[] = new double[Channel.values().length];
 			for(Channel channel : Channel.values())
 			{
 				int c = channel.ordinal();
 				vol[c] = p.sound.getVolume(channel);
 			}
 			
 			//mix the sound
 			for(int i = 0; i < bytes / Channel.values().length / SAMPLE_SIZE_IN_BYTES; ++i)
 			{
 				//mix the channel for this sample
 				for(Channel channel : Channel.values())
 				{
 					int c = channel.ordinal();
 					int sampleNum = (i * Channel.values().length) + c;
 
 					//get the wave amplitude value from multiple bytes
 					long channelSample = typeData[type][((sampleNum + 1) * SAMPLE_SIZE_IN_BYTES) - 1];
 					long dataSample = dataFromSource[((sampleNum + 1) * SAMPLE_SIZE_IN_BYTES) - 1];
 					for(int j = SAMPLE_SIZE_IN_BYTES - 2; j >= 0; --j)
 					{
 						int byteNum = (sampleNum * SAMPLE_SIZE_IN_BYTES) + j;
 						
 						//shift the current value over and add the new byte
 						channelSample = (channelSample << 8) | (typeData[type][byteNum] & 255L);
 						dataSample = (dataSample << 8) | (dataFromSource[byteNum] & 255L);
 					}
 					
 					//mix the sample
 					channelSample = (long)((channelSample * ((double)index / (index + 1))) +
 							((dataSample * vol[c]) * (1.0 / (index + 1))));
 					
 					//write the wave amplitude in multiple bytes
 					for(int j = 0; j < SAMPLE_SIZE_IN_BYTES; ++j)
 					{
 						int byteNum = (sampleNum * SAMPLE_SIZE_IN_BYTES) + j;
 						byte toSave = (byte)(channelSample & 255L);
 						typeData[type][byteNum] = toSave;
 						channelSample = channelSample >> 8;
 					}
 				}
 			}
 		}
 		
 		//loop over all sound types and mix them together into one wave
 		byte[] channelData = new byte[bytes];
 		int numTypes = SoundType.values().length;
 		for(SoundType t : SoundType.values())
 		{
 			int type = t.ordinal();
 			double vol = volumes.get(t);
 			
 			//mix the sound type
 			for(int i = 0; i < bytes / SAMPLE_SIZE_IN_BYTES; ++i)
 			{
 				//get the wave amplitude value from multiple bytes
 				long channelSample = channelData[((i + 1) * SAMPLE_SIZE_IN_BYTES) - 1];
 				long dataSample = typeData[type][((i + 1) * SAMPLE_SIZE_IN_BYTES) - 1];
 				for(int j = SAMPLE_SIZE_IN_BYTES - 2; j >= 0; --j)
 				{
 					//shift the current value over and add the new byte
 					int byteNum = (i * SAMPLE_SIZE_IN_BYTES) + j;
 					
 					channelSample = (channelSample << 8) | (channelData[byteNum] & 255L);
 					dataSample = (dataSample << 8) | (typeData[type][byteNum] & 255L);
 				}
 				
 				//mix the sample
 				channelSample = (long)(channelSample + ((dataSample * vol) * (1.0 / numTypes)));
 				
 				//write the wave amplitude in multiple bytes
 				for(int j = 0; j < SAMPLE_SIZE_IN_BYTES; ++j)
 				{
 					int byteNum = (i * SAMPLE_SIZE_IN_BYTES) + j;
 					byte toSave = (byte)(channelSample & 255L);
 					typeData[type][byteNum] = toSave;
 					channelSample = channelSample >> 8;
 					channelData[byteNum] = toSave;
 				}
 			}
 		}
 
 		//write the wave to the speaker line
 		line.write(channelData, 0, channelData.length);
 	}
 
 	private static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
 	{
 		SourceDataLine res = null;
 		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
 		res = (SourceDataLine)AudioSystem.getLine(info);
 		res.open(audioFormat);
 		return res;
 	}
 
 	private class SoundPair
 	{
 		public int progress;
 		public SoundInfo sound;
 
 		public SoundPair(int progress, SoundInfo sound)
 		{
 			this.progress = progress;
 			this.sound = sound;
 		}
 	}
 }
