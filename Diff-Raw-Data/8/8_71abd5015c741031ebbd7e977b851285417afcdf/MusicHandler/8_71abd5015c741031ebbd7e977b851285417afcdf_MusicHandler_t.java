 package com.musicgame.PumpAndJump.game.sound;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.AudioDevice;
 import com.badlogic.gdx.audio.io.Decoder;
 import com.badlogic.gdx.audio.io.VorbisDecoder;
 import com.badlogic.gdx.audio.io.WavDecoder;
 import com.badlogic.gdx.files.FileHandle;
 import com.musicgame.PumpAndJump.Util.FileFormatException;
 import com.musicgame.PumpAndJump.game.PumpAndJump;
 import com.musicgame.PumpAndJump.objects.Obstacle;
 
 public class MusicHandler extends Thread
 {
 	//for both
 	public static final int arraySampleLength = 6000;
 	public static final int minBufferDistance = 20;
 	public static final int maxBufferDistance = arraySampleLength/2;
 	public static final int frameSize = 256;//1024*8;//256;// 1024/4;
 	public static final int LargeFrameSize = frameSize*4;//1024*8;//256;// 1024/4;
 	public static final int sampleRate = 44100;
 	ArrayList<short[]> musicFile = new ArrayList<short[]>();
 
 
 	public static String fileName= "the_hand_that_feeds.wav";
 //	public String fileName= "the_hand_that_feeds.mp3";
 //	public String fileName= "Skrillex_Cinema.wav";
 //	public String fileName = "Windows_XP_Startup.wav";
 
 
 	//for input streaming
 	short[] buf = new short[frameSize*2];
 	Decoder decoder;
 	int inputFrame;
 	BeatDetector detect;
 
 	//other methods
 	public boolean buffering = true;
 	public boolean doneReading = false;
 	private boolean forceStop = false;
 	public boolean slowingDownBuffer = false;
 
 	//for output streaming
 	int outputFrame = 0;
 	public boolean songFinished = false;
 	AudioDevice device;
 
 	//location objects
 	int outputLocation = 0;
 	int inputLocation = 0;
 	public static double outputTimeReference = 0;
 	public static double inputTimeReference = 0;
 
 	private boolean stopRunning = false;
 
 	public MusicHandler(ArrayList<Obstacle> actualObjects)
 	{
 		detect = new BeatDetector(actualObjects);
 		for(int k = 0;k<arraySampleLength;k++)
 		{
 			musicFile.add(new short[frameSize]);
 		}
 		setUpOutputStream();
 	}
 
 	//input methods
 
 	public void loadSound() throws FileNotFoundException, FileFormatException
 	{
 		buffering = true;
 		outputFrame = 0;
 		inputFrame = 0;
 		outputTimeReference = 0;
 		slowingDownBuffer = false;
 		forceStop = false;
 		FileHandle file = null;
 		try
 		{
 			file = Gdx.files.internal(fileName);
 			if(file == null)
 			{
 				int i = 1/0;
 			}
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			try
 			{
 				file = Gdx.files.absolute(fileName);
 				if(file == null)
 				{
 					int i = 1/0;
 				}
 			}catch(Exception e2)
 			{
 				file = Gdx.files.internal("the_hand_that_feeds.wav");
 				e2.printStackTrace();
 			}
 		}
 		if(file == null||!file.exists())
 		{
 			throw new FileNotFoundException(fileName);
 
 		}
 		//it is not null and it exists
 
 		String extension = file.extension();
 		System.out.println("extesnsion is "+extension);
 		if(extension.equalsIgnoreCase("wav"))
 		{
 			decoder = new WavDecoder(file);
 		}else if(extension.equalsIgnoreCase("mp3"))
 		{
 			System.out.println("Creating MP3 FILE VERSIOn");
 			decoder = PumpAndJump.MP3decoder.getInstance(file);
 		//	decoder = new Mpg123Decoder(file);
 		}else if(extension.equalsIgnoreCase("ogg"))
 		{
 			decoder = new VorbisDecoder(file);
 		}else
 		{
 			throw new FileFormatException("File format not supported "+extension);
 		}
 	}
 
 	/**
 	 * The running thread that decompiles the music
 	 */
 	public void run()
 	{
 		outputFrame = 0;
 		inputFrame = 0;
 		int readSong = 1;
 		slowingDownBuffer = false;
 		buffering = true;
 		System.out.println("Starting reading ");
 
 		while(readSong > 0&&!stopRunning)
 		{
 			if(stopRunning)
 				break;
 			if(!slowingDownBuffer)
 			{
 				//System.out.println(inputFrame+" "+outputFrame);
 				short[] currentFrame = musicFile.get(inputLocation);
 				//has to convert it to mono
 				if(decoder.getChannels() == 2)
 				{
 					readSong = decoder.readSamples(buf,0, frameSize*2);
 					for(int k=0;k<frameSize;k++)
 					{
 						currentFrame[k] = (short) ((buf[k*2]+buf[k*2+1])/2.0);//gets half of the song (maybe because it is stereo?)
 					}
 				}else //we can put it straight in
 				{
 					readSong = decoder.readSamples(currentFrame,0, frameSize);
 				}
 				inputFrame++;
 				if(!stopRunning)
 					inputTimeReference = (inputFrame*MusicHandler.frameSize)/((double)MusicHandler.sampleRate);
 				inputLocation = inputFrame%arraySampleLength;
 				if(inputFrame%4==0)
 					detect.combineArray(musicFile, inputFrame-4);
 				if(bufferDistance()>maxBufferDistance)
 				{
 			//		System.out.println("slow down doggy");
 					slowingDownBuffer = true;
 				}
 
 				if(buffering)
 				{
 					/*
 					try {
 						Thread.sleep(5);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}*/
 
 				}else
 				{
 				//	System.out.println("Music Input");
 					/*
 					try {
 						Thread.sleep(5);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					*/
 				}
 			}else
 			{
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			if(forceStop)
 			{
 				System.out.println("STOPPING THE READING");
 				break;
 			}
 		}
 		System.out.println("FINISHED READING THE MUSIC FILE");
 		doneReading = true;
 	}
 
 
 	/**
 	 * Returns true if the bufferingDistance is less than the bufferDistance value
 	 * it is calculated by: MusicInputStream.currentFrame - OuputStream.currentFrame
 	 * @return
 	 */
 	public boolean bufferingNeeded()
 	{
 		return inputFrame-outputFrame<minBufferDistance&&!doneReading;
 	}
 
 	/**
 	 * calcualtes how far off the minBufferDistance is away from the the actual buffer distance
 	 * @return
 	 */
 	public long minBufferingDistance()
 	{
 		return minBufferDistance - (inputFrame-outputFrame);
 	}
 
 	/**
 	 * calcualtes how far the inputframe is from the output frame
 	 * @return
 	 */
 	public long bufferDistance()
 	{
 		return inputFrame-outputFrame;
 	}
 
 
 
 	//output methods
 
 	public void setUpOutputStream()
 	{
 		device = Gdx.audio.newAudioDevice(44100, true);
 	}
 
 	/**
 	 * Outputs the song to the sound device!
 	 */
 	public void writeSound()
 	{
 		if(!songFinished)
 		{
 			short[] currentFrame = musicFile.get(outputLocation);
 			device.writeSamples(currentFrame, 0, frameSize);
 			if(bufferDistance()<maxBufferDistance)
 			{
 				slowingDownBuffer = false;//speed buffering back up as the input is to close
 			}
 			outputFrame++;
 			outputLocation =(outputFrame%arraySampleLength);//puts the output location at the correct place
 			outputTimeReference = (outputFrame*frameSize)/((double)sampleRate);
 			if(doneReading&&bufferDistance()<2)
 			{
 				System.out.println("DONE READING?");
 				songFinished = true;
 			}
 		}
 
 	}
 
 	//othe
 
 	public void dispose()
 	{
 		stopRunning = true;
 		songFinished = true;
 		musicFile.clear();
		try
		{
			device.dispose();
		}catch(Exception e)
		{

		}
 		fileName = "the_hand_that_feeds.wav";
 	}
 }
