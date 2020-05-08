 package edu.njucs.timer;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.JWindow;
 
 import ch.swingfx.twinkle.NotificationBuilder;
 import ch.swingfx.twinkle.event.INotificationEventListener;
 import ch.swingfx.twinkle.event.NotificationEvent;
 import ch.swingfx.twinkle.manager.SequentialNotificationManager;
 import ch.swingfx.twinkle.manager.WindowOpenListener;
 import ch.swingfx.twinkle.style.INotificationStyle;
 import ch.swingfx.twinkle.style.theme.DarkDefaultNotification;
 import ch.swingfx.twinkle.window.DefaultNotificationWindow;
 import ch.swingfx.twinkle.window.IPosition;
 
 
 public class GSEClock implements SwitchableTimerListener, WindowOpenListener, INotificationEventListener{
 	public static final String SETTING_FILE="setting.ini";
 	public static final int STATER=0;
 	public static final int ATTACKER=1;
 	public static final int LEFT_UP=0;
 	public static final int RIGHT_UP=1;
 	public static final int LEFT_BOTTOM=2;
 	public static final int RIGHT_BOTTOM=3;
 	
 	int currentSpeaker;
 	DefaultNotificationWindow window;
 	SwitchableTimer timer;
 	int phase=0;
 	int[] restTimes;
 	int[][] limits={{60*20,60*12,60*3},{0,60*10,60*3}};
 	boolean started=false;
 	String[] soundFiles={"phase0.wav","phase1.wav","phase2.wav","1minleft.wav","3minleft.wav","switch.wav"};
 	boolean paused=false;
 	boolean didSomething=false;
 	GSEClockListener listener;
 	boolean end=false;
 	
 	int location=LEFT_UP;
 	
 	
 	public GSEClock()
 	{
 		readSettingFile();
 		GSEDefaultNotification style=new GSEDefaultNotification();
 		SequentialNotificationManager.setListener(this);
 		new NotificationBuilder()
 		.withStyle(style)
 		.withTitle(getTitle())
 		.withMessage("˫ʼ")
 		.withDisplayTime(Integer.MAX_VALUE)
 		.withListener(this)
 		.withPosition(new IPosition() {
 			
 			@Override
 			public Point getPosition(Dimension screenSize, Insets screenInsets,
 					JWindow window, INotificationStyle style) {
 				int x=0;
 				int y=0;
 				switch (location) {
 				case LEFT_UP:
 					break;
 				case RIGHT_UP:
 					x = screenSize.width - window.getWidth();
 					break;
 				case LEFT_BOTTOM:
 					y=screenSize.height-window.getHeight();
 					break;
 				case RIGHT_BOTTOM:
 					x = screenSize.width - window.getWidth();
 					y=screenSize.height-window.getHeight();
 					break;
 				default:
 					break;
 				}
 				return new Point(x, y);
 			}
 		})
 		.showNotification();
 		timer=new SwitchableTimer();
 		timer.setListener(this);
 		currentSpeaker=STATER;
 		restTimes=new int[2];
 	}
 	
 	public void start()
 	{
 		started=true;
 		setPhase(0);
 		timer.setId(currentSpeaker);
 		timer.start();
 	}
 	
 	public void togglePause()
 	{
 		this.paused=!this.paused;
 		if (this.paused)
 			timer.pause();
 		else
 			timer.resume();
 	}
 	
 	public void setPhase(int phase)
 	{
 		restTimes[ATTACKER]=limits[ATTACKER][phase];
 		restTimes[STATER]=limits[STATER][phase];
 		this.phase=phase;
 		timer.clear();
 		if (phase!=0)
 		{
 			currentSpeaker=ATTACKER;
 		}
 		else
 		{
 			currentSpeaker=STATER;
 		}
 		timer.setId(currentSpeaker);
 		if (window!=null)
 		{
 			window.setTitle(getTitle());
 			window.setMessage(getDisplayableTime(restTimes[currentSpeaker]));
 		}
 		timer.resume();
 		playSound(soundFiles[phase]);
 		if (listener!=null)
 			listener.phaseChanged(phase);
 	}
 	
 	public void setEnd()
 	{
 		restTimes[ATTACKER]=0;
 		restTimes[STATER]=0;
 		if (window!=null)
 		{
 			window.setTitle("׶");
 			window.setMessage("ɱ");
 		}
 		timer.destory();
 		end=true;
 		if (listener!=null)
 			listener.end();
 	}
 	
 	public void setListener(GSEClockListener listener)
 	{
 		this.listener=listener;
 	}
 	
 	private String getDisplayableTime(int seconds)
 	{
 		int min=seconds/60;
 		int second=seconds%60;
 		String minStr=min<10?("0"+min):(""+min);
 		String secondStr=second<10?("0"+second):(""+second);
 		return minStr+":"+secondStr;
 	}
 	
 
 
 	private void refreshTime(long currentTimeInMs)
 	{
 		restTimes[currentSpeaker] = (int) (limits[currentSpeaker][phase]-currentTimeInMs/1000);
 		if (restTimes[currentSpeaker] <0)
 			restTimes[currentSpeaker] =0;
 		else if (restTimes[currentSpeaker]==60*3+1)
 		{
 			playSound(soundFiles[3]);
 		}
 		else if (restTimes[currentSpeaker]==60+1)
 		{
 			playSound(soundFiles[4]);
 		}
 		if (window!=null)
 			window.setMessage(getDisplayableTime(restTimes[currentSpeaker]));
 	}
 	
 	private String getTitle()
 	{
 		String title="";
 		switch (phase) {
 		case 0:
 			title+="";
 			break;
 		case 1:
 			title+="";
 			break;
 		case 2:
 			title+="ܽ";
 			break;
 		default:
 			break;
 		}
 		if (currentSpeaker==STATER)
 		{
 			title+="-";
 		}
 		else
 		{
 			title+="-";
 		} 
 		return title;
 	}
 
 	public void switchSpeaker()
 	{
 		int nextSpeaker=currentSpeaker;
 		if (currentSpeaker==STATER)
 		{
 			nextSpeaker=ATTACKER;
 		}
 		else
 		{
 			nextSpeaker=STATER;
 		}
 		if (restTimes[nextSpeaker]!=0)
 		{
 			currentSpeaker=nextSpeaker;
 			window.setTitle(getTitle());
 			timer.setId(currentSpeaker);
 			timer.resume();
 			playSound(soundFiles[5]);
 		}
 		else
 		{
 //			timer.togglePause();
 		}
 			
 	}
 	
 	public void nextPhase()
 	{
 		if (!started)
 			start();
 		else
 		{
 			if (phase<limits[STATER].length-1)
 				setPhase(phase+1);
 			else
 				setEnd();
 		}
 	}
 
 	public void playSound(String filename) { 
 		try {
 			AudioInputStream sound=AudioSystem.getAudioInputStream(new File(filename));
 			 AudioFormat format = sound.getFormat();
 
 		        DataLine.Info info = new DataLine.Info(
 		            Clip.class,
 		            sound.getFormat(),
 		            ( (int) sound.getFrameLength() *
 		             format.getFrameSize()));
 
 		        Clip clip = (Clip) AudioSystem.getLine(info);
 		        clip.open(sound);
 		        clip.start();
 		} catch (UnsupportedAudioFileException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (LineUnavailableException e) {
 			e.printStackTrace();
 		}
 	} 
 	
 	private void readSettingFile()
 	{
 		try {
			BufferedReader reader=new BufferedReader(new FileReader(new File(SETTING_FILE)));
 			String line=reader.readLine();
 			while (line!=null)
 			{
 				String[] strs=line.split("=");
 				String left=strs[0].trim().toLowerCase();
 				if (left.startsWith("#"))
 				{
 				}
 				else if (left.equals("staterphase0"))
 				{
 					try
 					{
 						int time=(int) (Double.parseDouble(strs[1].trim())*60);
 						limits[STATER][0]=time;
 					}
 					catch (NumberFormatException e)
 					{
 					}
 				}
 				else if (left.equals("staterphase1"))
 				{
 					try
 					{
 						int time=(int) (Double.parseDouble(strs[1].trim())*60);
 						limits[STATER][1]=time;
 					}
 					catch (NumberFormatException e)
 					{
 					}
 				}
 				else if (left.equals("staterphase2"))
 				{
 					try
 					{
 						int time=(int) (Double.parseDouble(strs[1].trim())*60);
 						limits[STATER][2]=time;
 					}
 					catch (NumberFormatException e)
 					{
 					}
 				}
 				else if (left.equals("attackerphase0"))
 				{
 					try
 					{
 						int time=(int) (Double.parseDouble(strs[1].trim())*60);
 						limits[ATTACKER][0]=time;
 					}
 					catch (NumberFormatException e)
 					{
 					}
 				}
 				else if (left.equals("attackerphase1"))
 				{
 					try
 					{
 						int time=(int) (Double.parseDouble(strs[1].trim())*60);
 						limits[ATTACKER][1]=time;
 					}
 					catch (NumberFormatException e)
 					{
 					}
 				}
 				else if (left.equals("attackerphase2"))
 				{
 					try
 					{
 						int time=(int) (Double.parseDouble(strs[1].trim())*60);
 						limits[ATTACKER][2]=time;
 					}
 					catch (NumberFormatException e)
 					{
 					}
 				}
 				else if (left.equals("soundphase0"))
 				{
 					soundFiles[0]=strs[1].trim();
 
 				}
 				else if (left.equals("soundphase1"))
 				{
 					soundFiles[1]=strs[1].trim();
 
 				}
 				else if (left.equals("soundphase2"))
 				{
 					soundFiles[2]=strs[1].trim();
 
 				}
 				else if (left.equals("sound1min"))
 				{
 					soundFiles[3]=strs[1].trim();
 
 				}
 				else if (left.equals("sound3min"))
 				{
 					soundFiles[4]=strs[1].trim();
 
 				}
 				else if (left.equals("soundswitch"))
 				{
 					soundFiles[5]=strs[1].trim();
 				}
 				else if (left.equals("location"))
 				{
 					if (strs[1].trim().toLowerCase().equals("leftup"))
 					{
 						location=LEFT_UP;
 					}
 					else if (strs[1].trim().toLowerCase().equals("rightup"))
 					{
 						location=RIGHT_UP;
 					}
 					else if (strs[1].trim().toLowerCase().equals("leftbottom"))
 					{
 						location=LEFT_BOTTOM;
 					}
 					else if (strs[1].trim().toLowerCase().equals("leftbottom"))
 					{
 						location=RIGHT_BOTTOM;
 					}
 				}
 				else if (left.equals("titlefont"))
 				{
 					GSEDefaultNotification.titleFont=strs[1].trim();
 				}
 				else if (left.equals("titlefontsize"))
 				{
 					GSEDefaultNotification.titleFontSize=strs[1].trim();
 				}
 				else if (left.equals("titlefontcolor"))
 				{
 					GSEDefaultNotification.titleFontColor=strs[1].trim();
 				}
 				else if (left.equals("messagefont"))
 				{
 					GSEDefaultNotification.messageFont=strs[1].trim();
 				}
 				else if (left.equals("messagefontsize"))
 				{
 					GSEDefaultNotification.messageFontSize=strs[1].trim();
 				}
 				else if (left.equals("messagefontcolor"))
 				{
 					GSEDefaultNotification.messageFontColor=strs[1].trim();
 				}
 				else if (left.equals("backgroundcolor"))
 				{
 					GSEDefaultNotification.backgroundColor=strs[1].trim();
 				}
 				else if (left.equals("backgroundalpha"))
 				{
 					GSEDefaultNotification.backgroundAlpha=strs[1].trim();
 				}
 				else if (left.equals("width"))
 				{
 					GSEDefaultNotification.width=strs[1].trim();
 				}
 				else if (left.equals("cornerradius"))
 				{
 					GSEDefaultNotification.cornerRadius=strs[1].trim();
 				}
 				line=reader.readLine();
 			}
 			reader.close();
 		} catch (FileNotFoundException e) {
 		} catch (IOException e) {
 		}
 	}
 	
 	@Override
 	public void timeTicked(SwitchableTimer sender,int id, long currentTimeInMs) {
 		if (id==currentSpeaker)
 		{
 			refreshTime(currentTimeInMs);
 		}
 		else
 		{
 			sender.setId(currentSpeaker);
 		}
 		if (window!=null)
 			window.toFront();
 	}
 	
 	
 	@Override
 	public void switched(SwitchableTimer sender,int toId, long currentTimeInMs)
 	{
 		if (toId==currentSpeaker)
 		{
 			refreshTime(currentTimeInMs);
 		}
 		else
 		{
 			sender.setId(currentSpeaker);
 		}
 	}
 	
 	@Override
 	public void pauseChanged(SwitchableTimer sender, boolean paused) {
 		if (paused) {
 			if (window != null) {
 				window.setMessageColor(new Color(0x2E9AFE));
 			}
 			if(listener!=null)
 				listener.paused();
 		} else {
 			if (window != null) {
 				window.setMessageColor(Color.white);
 			}
 			if(listener!=null)
 				listener.resumed();
 		}
 	}
 
 	@Override
 	public void windowsOpened(JWindow window) {
 		if (window instanceof DefaultNotificationWindow)
 			this.window=(DefaultNotificationWindow)window;
 	}
 	
 
 	@Override
 	public void opened(NotificationEvent event) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void clicked(NotificationEvent event) {
 		togglePause();
 		didSomething=true;
 	}
 
 	@Override
 	public void mouseOver(NotificationEvent event) {
 	}
 
 	@Override
 	public void mouseOut(NotificationEvent event) {
 //		switchSpeaker();
 		if (started && !paused && !didSomething)
 		{
 			switchSpeaker();
 		}
 		didSomething=false;
 	}
 
 	@Override
 	public void closed(NotificationEvent event) {
 		if (listener!=null)
 		{
 			listener.exit();
 		}
 		System.exit(0);
 	}
 
 
 	@Override
 	public void listenedKeysPressed() {
 		if (started)
 		{
 			timer.togglePause();
 		}
 	}
 
 
 	@Override
 	public void doubleClicked() {
 		if (!started){
 			start();
 		}
 		else
 		{
 			nextPhase();
 		}
 		didSomething=true;
 	}
 	
 }
