 package crescendo.sheetmusic;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import javax.swing.JPanel;
 
 import org.w3c.dom.css.Rect;
 
 import crescendo.base.NoteAction;
 import crescendo.base.ProcessedNoteEvent;
 import crescendo.base.ProcessedNoteEventListener;
 import crescendo.base.EventDispatcher.ActionType;
 import crescendo.base.song.Note;
 import crescendo.base.song.SongModel;
 
 public class MusicEngine extends JPanel implements ProcessedNoteEventListener {
 
 	private boolean isLooping;
 	private SongModel songModel;
 	private Map<Note,ArrayList<DrawableNote>> noteMap;
 	private double currentPosition;
 	private Note sectionStartNote;
 	private Note sectionEndNote;
 	private ArrayList<Drawable> drawables;
 	
 
 	private int activeTrack;
 
 	private Thread timerThread;
 	private MusicEngineTimer timer;
 	private boolean doContinue;
 	private boolean isPaused;
 
 	private long timeStarted;
 
 
 	int measuresPerLine = 4;
 	int sheetMusicWidth = 860;
 	double measureWidth = sheetMusicWidth/measuresPerLine;
 	double xMargin=80;
 	double yMargin=80;
 	double yMeasureDistance=300;
 	double yOffset; //used if bass clef only
 	double noteOffset = 60/measuresPerLine; //so the first note isnt on the measure line
 	private boolean showTitle=true;
 	
 	private LinkedList<Drawable> drawQueue;
 	
 	boolean bassClefNeeded; 
 	boolean trebleClefNeeded; 
 
 	double beatsPerMeasure;
 	double beatNote;
 
 
 	public MusicEngine(SongModel model,int activeTrack,boolean showTitle){
 		this.showTitle = showTitle;
 		setUp(model,activeTrack);
 	}
 	
 	public MusicEngine(SongModel model,int activeTrack){
 		setUp(model,activeTrack);
 	}
 	
 	
 	public void setUp(SongModel model,int activeTrack){
 		this.setPreferredSize(new Dimension(1024, 8000));
 		timerThread = new Thread(new MusicEngineTimer());
 		isLooping = false;
 		songModel = model;
 		noteMap = new HashMap<Note,ArrayList<DrawableNote>>();
 		drawables = new ArrayList<Drawable>();
 		currentPosition = 0.0;
 		sectionStartNote = null;
 		sectionEndNote = null;
 		this.activeTrack = activeTrack;
 		drawQueue = new LinkedList<Drawable>();
 
 		timeStarted=0;
 
 		timer = new MusicEngineTimer();
 		timerThread = new Thread(timer);
 		doContinue = true;
 		
 		//if the title is to be displayed, make room for it by increasing the yMargin
 		if(showTitle)
 		{
 			yMargin+=40;
 		}
 
 
 
 		//Get our drawables ready
 		LinkedList<Note> notes =(LinkedList<Note>) songModel.getTracks().get(activeTrack).getNotes();
 
 
 
 
 
 
 		beatsPerMeasure = songModel.getTimeSignature().getBeatsPerMeasure();
 		beatNote = songModel.getTimeSignature().getBeatNote();
 
 
 
 		double currentBeatCount = 0;
 		double currentMeasure=0;
 
 		double restBeatStartTop=0;
 		double restBeatStartBottom=0;
 
 
 		int x=0;
 		int y=0;
 
 
 		//PREPROCESS 1: Remove Base/Treble Clef if unused
 		bassClefNeeded = false; 
 		trebleClefNeeded = false; 
 		for(int i=0;i<notes.size();i++)
 		{
 			if(notes.get(i).getDynamic()>0)
 			{
 				if(notes.get(i).getPitch()>=60)trebleClefNeeded = true;
 				if(notes.get(i).getPitch()<60)bassClefNeeded = true;
 			}
 		}
 
 		yOffset=0;
 		if((trebleClefNeeded)&&(!bassClefNeeded))
 		{
 			yMeasureDistance=150;
 		}
 		if((bassClefNeeded)&&(!trebleClefNeeded))
 		{
 			yMeasureDistance=150;
 			yOffset = -130;
 		}
 
 		ArrayList<Note> noteQeue = new ArrayList<Note>();
 
 		int drawableStart=0;
 		for(int i=0;i<notes.size();i++)
 		{
 			
 			noteQeue = new ArrayList<Note>();
 			
 			double noteDuration = notes.get(i).getDuration();
 
 			if(notes.get(i).getDuration()>(beatsPerMeasure-currentBeatCount))
 			{
 				//finish off the measure
 				noteQeue.add(new Note(notes.get(i).getPitch(), (beatsPerMeasure-currentBeatCount), notes.get(i).getDynamic(), songModel.getTracks().get(activeTrack)));
 				noteDuration-=(beatsPerMeasure-currentBeatCount);
 				
 				//check for complete measures
 				while(noteDuration>=beatsPerMeasure)
 				{
 					noteQeue.add(new Note(notes.get(i).getPitch(), beatsPerMeasure, notes.get(i).getDynamic(), songModel.getTracks().get(activeTrack)));
 					noteDuration-=beatsPerMeasure;
 				}
 				//after finishing the measure and checking for more full measures, if there is something left add it too
 				if(noteDuration>0.0)
 				{
 					noteQeue.add(new Note(notes.get(i).getPitch(), noteDuration, notes.get(i).getDynamic(), songModel.getTracks().get(activeTrack)));
 					noteDuration=0;
 				}
 				
 				
 			}
 			else noteQeue.add(notes.get(i));
 
 			drawableStart = drawables.size();
 			for(int j=0;j<noteQeue.size();j++)
 			{
 				double noteBeat = noteQeue.get(j).getDuration();
 				
 				x = (int) ((currentMeasure%measuresPerLine * measureWidth) + (((currentBeatCount)/beatsPerMeasure)*measureWidth) + xMargin + noteOffset);
 				y = (int) (yMeasureDistance*((currentMeasure-(currentMeasure%measuresPerLine))/measuresPerLine) + yMargin + yOffset);
 				
 				
 				
 				
 				if(noteQeue.get(j).getDynamic()==0)
 				{
 					//drawables.add(new WholeRest(noteQeue.get(j),x,y));
 				}
 				else 
 				{
 					if(noteBeat==0.125*beatNote)drawables.add(new EighthNote(noteQeue.get(j),x,y));
 					else if(noteBeat==0.25*beatNote)drawables.add(new QuarterNote(noteQeue.get(j),x,y));
 					else if(noteBeat==0.5*beatNote)drawables.add(new HalfNote(noteQeue.get(j),x,y));
 					else if(noteBeat==1*beatNote)drawables.add(new WholeNote(noteQeue.get(j),x,y));
 					else drawables.add(new DrawableNote(noteQeue.get(j),x,y){public void draw(Graphics g){}});
 					
 					if(trebleClefNeeded)
 					{
 						if((restBeatStartTop!=currentBeatCount)&&(noteQeue.get(j).getPitch()>=60))
 						{
 							double restDurationTop = currentBeatCount-restBeatStartTop;
 							
 							x = (int) ((currentMeasure%measuresPerLine * measureWidth) + (((restBeatStartTop)/beatsPerMeasure)*measureWidth) + xMargin + noteOffset);
 							y = (int) (yMeasureDistance*((currentMeasure-(currentMeasure%measuresPerLine))/measuresPerLine) + yMargin);
 							
 							int restLocation=66;
 							
 							if(restDurationTop==0.125*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationTop, 0, songModel.getTracks().get(activeTrack)),x,y));
 							if(restDurationTop==0.25*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationTop, 0, songModel.getTracks().get(activeTrack)),x,y));
 							if(restDurationTop==0.5*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationTop, 0, songModel.getTracks().get(activeTrack)),x,y));
 							if(restDurationTop==1*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationTop, 0, songModel.getTracks().get(activeTrack)),x,y));
 							
 							restBeatStartTop=currentBeatCount + noteBeat;
 						}
 						else if(noteQeue.get(j).getPitch()>=60)restBeatStartTop = currentBeatCount + noteBeat;
 					}
 					
 					if(bassClefNeeded)
 					{
 						if((restBeatStartBottom!=currentBeatCount)&&(noteQeue.get(j).getPitch()<60))
 						{
 							double restDurationBottom = currentBeatCount-restBeatStartBottom;
 							
 							x = (int) ((currentMeasure%measuresPerLine * measureWidth) + (((restBeatStartBottom)/beatsPerMeasure)*measureWidth) + xMargin + noteOffset);
 							y = (int) (yMeasureDistance*((currentMeasure-(currentMeasure%measuresPerLine))/measuresPerLine) + yMargin );
 							
 							int restLocation=53;
 							if(!trebleClefNeeded)restLocation = 66;
 							
 							if(restDurationBottom==0.125*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationBottom, 0, songModel.getTracks().get(activeTrack)),x,(int)(y)));
 							if(restDurationBottom==0.25*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationBottom, 0, songModel.getTracks().get(activeTrack)),x,(int)(y)));
 							if(restDurationBottom==0.5*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationBottom, 0, songModel.getTracks().get(activeTrack)),x,(int)(y)));
 							if(restDurationBottom==1*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationBottom, 0, songModel.getTracks().get(activeTrack)),x,(int)(y)));
 							
 							restBeatStartBottom=currentBeatCount + noteBeat;
 						}
 						else if(noteQeue.get(j).getPitch()<60)restBeatStartBottom = currentBeatCount + noteBeat;
 					}
 				}
 			
 				
 				
 				currentBeatCount+=noteBeat;
 				while(currentBeatCount>=beatsPerMeasure)
 				{
 					
 					if(trebleClefNeeded)
 					{
 						if(restBeatStartTop!=currentBeatCount)
 						{
 							double restDurationTop = currentBeatCount-restBeatStartTop;
 							
 							x = (int) ((currentMeasure%measuresPerLine * measureWidth) + (((restBeatStartTop)/beatsPerMeasure)*measureWidth) + xMargin + noteOffset);
 							y = (int) (yMeasureDistance*((currentMeasure-(currentMeasure%measuresPerLine))/measuresPerLine) + yMargin );
 							
 							int restLocation=66;
 							
 							if(restDurationTop==0.125*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationTop, 0, songModel.getTracks().get(activeTrack)),x,y));
 							if(restDurationTop==0.25*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationTop, 0, songModel.getTracks().get(activeTrack)),x,y));
 							if(restDurationTop==0.5*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationTop, 0, songModel.getTracks().get(activeTrack)),x,y));
 							if(restDurationTop==1*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationTop, 0, songModel.getTracks().get(activeTrack)),x,y));
 							
 						}
 					}
 					
 					if(bassClefNeeded)
 					{
 						if(restBeatStartBottom!=currentBeatCount)
 						{
 							double restDurationBottom = currentBeatCount-restBeatStartBottom;
 							
 							x = (int) ((currentMeasure%measuresPerLine * measureWidth) + (((restBeatStartBottom)/beatsPerMeasure)*measureWidth) + xMargin + noteOffset);
 							y = (int) (yMeasureDistance*((currentMeasure-(currentMeasure%measuresPerLine))/measuresPerLine) + yMargin);
 							
 							int restLocation=53;
 							if(!trebleClefNeeded)restLocation = 66;
 							
 							if(restDurationBottom==0.125*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationBottom, 0, songModel.getTracks().get(activeTrack)),x,(int)(y)));
 							if(restDurationBottom==0.25*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationBottom, 0, songModel.getTracks().get(activeTrack)),x,(int)(y)));
 							if(restDurationBottom==0.5*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationBottom, 0, songModel.getTracks().get(activeTrack)),x,(int)(y)));
 							if(restDurationBottom==1*beatNote)drawables.add(new WholeRest(new Note(restLocation, restDurationBottom, 0, songModel.getTracks().get(activeTrack)),x,(int)(y)));
 							
 						}
 					}
 					
 					
 					currentBeatCount-=beatsPerMeasure;
 					currentMeasure++;
 					
 					restBeatStartTop=0;
 					restBeatStartBottom=0;
 					
 					
 				}
 			}
 			//all notes associated with the last "note" are to be put into the noteMap
 			ArrayList<DrawableNote> dNotes = new ArrayList<DrawableNote>();
 			for(int j=drawableStart;j< drawables.size();j++)
 			{
 				if(drawables.get(j) instanceof DrawableNote)
 				{
 					dNotes.add((DrawableNote) drawables.get(j));
 				}
 			}
 			noteMap.put(notes.get(i), dNotes);
 		}
 	}
 
 	public MusicEngine(SongModel model,int activeTrack, double currentPosition){
 		this(model,activeTrack);
 		this.currentPosition = currentPosition;
 		//use logic for finding the number of beats in a song
 	}
 
 	public MusicEngine(SongModel model,int activeTrack, Note currentNote){
 		this(model,activeTrack);
 		this.sectionStartNote = currentNote;
 	}
 
 	public MusicEngine(SongModel model,int activeTrack, Note currentSectionBeginNote, Note currentSectionEndNote){
 		this(model,activeTrack);
 		sectionStartNote = currentSectionBeginNote;
 		sectionEndNote = currentSectionEndNote;
 	}
 
 	public void setLooping(boolean looping){
 		isLooping = looping;
 	}
 	
 	public void setShowTitle(boolean showTitle){
 		this.showTitle=showTitle;
 	}
 
 	public void setPosition(double position){
 		currentPosition = position;
 	}
 
 	public void setPosition(Note position){
 		sectionStartNote = position;
 	}
 
 	public void setSection(Note sectionStartNote, Note sectionEndNote){
 		this.sectionStartNote = sectionStartNote;
 		this.sectionEndNote = sectionEndNote;
 	}
 
 
 
 
 	@Override
 	public void paint(Graphics g){
 		super.paint(g);
 		if(showTitle){
 			//Song title
 			String title="Untitled";
 			title = songModel.getTitle();
 			g.setFont(new Font("Georgia", Font.PLAIN, 32));
 	
 			g.drawString(title,(int) (xMargin+(measureWidth*measuresPerLine)/2 - title.length()*8), 40);
 			
 			//Song author
 			String creators="";
 			for(int i=0;i<songModel.getCreators().size();i++)
 			{
 				creators += songModel.getCreators().get(i).getName();
 			}
 			if(songModel.getCreators().get(0).getName()==null)
 				creators = "Anonymous";
 			
 			g.setFont(new Font("Georgia", Font.PLAIN, 18));
 	
 			g.drawString(creators,(int) (xMargin+(measureWidth*measuresPerLine)/2 - creators.length()*5), 60);
 		}
 
 		
 		//draw staffs
 		for(int j=0;j<20;j++)
 		{	
 			
 
 
 
 			//draw top staff lines
 			for(int i=0;i<5;i++)
 				g.drawLine((int)(xMargin),(int)(yMargin+(16*i)+(yMeasureDistance*j)),(int)(xMargin+measureWidth*measuresPerLine),(int)(yMargin+(16*i)+(yMeasureDistance*j)));
 			//draw top measure lines
 			for(int k=0;k<=measuresPerLine;k++)
 				g.drawLine((int)(xMargin+measureWidth*k),(int)(yMargin+(yMeasureDistance*j)),(int)(xMargin+measureWidth*k),(int)(yMargin+64+(yMeasureDistance*j)));
 
 			//draw bottom staff if needed 
 			if(trebleClefNeeded&&bassClefNeeded)
 			{
 				//draw the treble clef
 				int x = (int) (xMargin-45);
 				int y =(int) (yMargin+(yMeasureDistance*j)-13);
 				double scale = .75;
 				g.fillOval(x+(int)(25*scale), y+(int)(110*scale), (int)(10*scale), (int)(10*scale));
 				g.drawArc(x+(int)(27*scale), y+(int)(102*scale), (int)(13*scale), (int)(20*scale), (int)(40), (int)(-200));
 				g.drawLine(x+(int)(38*scale), y+(int)(105*scale), x+(int)(27*scale), y+(int)(20*scale));
 				g.drawArc(x+(int)(27*scale),y-(int)(5*scale),(int)(13*scale),(int)(50*scale),(int)(90),(int)(90));
 				g.drawArc(x+(int)(27*scale),y-(int)(5*scale),(int)(13*scale),(int)(30*scale),(int)(90),(int)(-90));
 				g.drawArc(x+(int)(10*scale),y-(int)(22*scale),(int)(30*scale),(int)(70*scale),0,(int)(-80));
 				g.drawArc(x+(int)(7*scale),y+(int)(47*scale),(int)(55*scale),(int)(55*scale),(int)(-50),(int)(-200));
 				g.drawArc(x+(int)(22*scale),y+(int)(65*scale),(int)(34*scale),(int)(35*scale),(int)(160),(int)(-200));
 
 				
 				
 				//draw the bass clef
 				x = (int) (xMargin-65);
 				y =(int) (yMargin+(yMeasureDistance*j)+130);
 				g.fillOval(x+25, y+10, (int)(10), (int)(10));
 				g.drawArc(x+25, y,25,32,0,180);
 				g.drawArc(x, y-16,50,64,270,360-270);
 				g.fillOval(x+55, y+5, (int)(8), (int)(8));
 				g.fillOval(x+55, y+21, (int)(8), (int)(8));
 				
 				
 				//draw bottom staff lines
 				for(int i=0;i<5;i++)
 					g.drawLine((int)(xMargin),(int)(yMargin+130+(16*i)+(yMeasureDistance*j)),(int)(xMargin+measureWidth*measuresPerLine),(int)(yMargin+130+(16*i)+(yMeasureDistance*j)));
 				//draw bottom measure lines
 				for(int k=0;k<=measuresPerLine;k++)
 					g.drawLine((int)(xMargin+measureWidth*k),(int)(yMargin+64+(yMeasureDistance*j)),(int)(xMargin+measureWidth*k),(int)(yMargin+194+(yMeasureDistance*j)));
 			}
 			else
 			{
 				if(trebleClefNeeded)
 				{
 					//draw the treble clef
 					int x = (int) (xMargin-45);
 					int y =(int) (yMargin+(yMeasureDistance*j)-13);
 					double scale = .75;
 					g.fillOval(x+(int)(25*scale), y+(int)(110*scale), (int)(10*scale), (int)(10*scale));
 					g.drawArc(x+(int)(27*scale), y+(int)(102*scale), (int)(13*scale), (int)(20*scale), (int)(40), (int)(-200));
 					g.drawLine(x+(int)(38*scale), y+(int)(105*scale), x+(int)(27*scale), y+(int)(20*scale));
 					g.drawArc(x+(int)(27*scale),y-(int)(5*scale),(int)(13*scale),(int)(50*scale),(int)(90),(int)(90));
 					g.drawArc(x+(int)(27*scale),y-(int)(5*scale),(int)(13*scale),(int)(30*scale),(int)(90),(int)(-90));
 					g.drawArc(x+(int)(10*scale),y-(int)(22*scale),(int)(30*scale),(int)(70*scale),0,(int)(-80));
 					g.drawArc(x+(int)(7*scale),y+(int)(47*scale),(int)(55*scale),(int)(55*scale),(int)(-50),(int)(-200));
 					g.drawArc(x+(int)(22*scale),y+(int)(65*scale),(int)(34*scale),(int)(35*scale),(int)(160),(int)(-200));
 				}
 				else if(bassClefNeeded)
 				{
 					//draw the bass clef
 					int x = (int) (xMargin-65);
 					int y =(int) (yMargin+(yMeasureDistance*j)-3);
 					g.fillOval(x+25, y+10, (int)(10), (int)(10));
 					g.drawArc(x+25, y,25,32,0,180);
 					g.drawArc(x, y-16,50,64,270,360-270);
 					g.fillOval(x+55, y+5, (int)(8), (int)(8));
 					g.fillOval(x+55, y+21, (int)(8), (int)(8));
 				}
 			}
 		}
 
 
 		for(Drawable d:drawQueue){
 			drawables.add(d);
 		}
 		drawQueue.clear();
 		for(Drawable d: drawables)d.draw(g);
 
 
 
 		double timeDelta = System.currentTimeMillis() - timeStarted;
 		double linePerMS = ((double)songModel.getBPM())/(beatsPerMeasure*((double)measuresPerLine))/60.0/1000.0;
 
 		double line = Math.floor(timeDelta*linePerMS);
 		double offset = ((timeDelta-(line/linePerMS))*linePerMS)*measureWidth*measuresPerLine;
 		g.setColor(Color.red);
 		
 		
 
 		
 		g.drawLine((int)((xMargin) + offset -10), (int)((yMargin) + line*yMeasureDistance), (int)((xMargin) + offset -10), (int)((yMargin) + line*yMeasureDistance+64));
 		if(trebleClefNeeded&&bassClefNeeded)
 		{
 			g.drawLine((int)((xMargin) + offset -10), (int)((yMargin) + line*yMeasureDistance)+64, (int)((xMargin) + offset -10), (int)((yMargin) + line*yMeasureDistance+194));
 		}
 		
 		
 		if(!isPaused){
 			Rectangle rv = this.getVisibleRect();
			if(!rv.contains(rv.x,(int)((yMargin) + line*yMeasureDistance+ (194*2)))){
				rv.setLocation(rv.x, (int)((yMargin)  + line*yMeasureDistance - (64)));
			}
 			this.scrollRectToVisible(rv);
 		}
 
 	}
 
 	public void play(){
 		timeStarted = System.currentTimeMillis();
 		isPaused = false;
 		timerThread.start();
 	}
 
 	public void pause(){
 		isPaused = true;
 	}
 
 	public void stop(){
 		doContinue=false;
 	}
 
 	public void resume(){
 		isPaused = false;
 	}
 
 	@Override
 	public void handleProcessedNoteEvent(ProcessedNoteEvent e) {
 		if(e.getExpectedNote()==null){
 			if(e.getPlayedNote().getAction()==ActionType.PRESS){
 				drawWrongNote(e);
 			}
 			//Create a new incorrect note
 		}else if(e.getPlayedNote()==null && e.getExpectedNote().getAction() == NoteAction.BEGIN){
 			if(noteMap.containsKey(e.getExpectedNote().getNote()))
 				noteMap.get(e.getExpectedNote().getNote()).get(0).setCorrect(e.isCorrect());
 		}else if(e.getExpectedNote()!=null && e.getPlayedNote()!=null){
 			if(noteMap.containsKey(e.getExpectedNote().getNote())){
 				noteMap.get(e.getExpectedNote().getNote()).get(0).setCorrect(e.isCorrect());
 			}else{
 				drawWrongNote(e);
 			}
 		}
 	}
 	
 	private void drawWrongNote(ProcessedNoteEvent e){
 		double timeDelta = System.currentTimeMillis() - timeStarted;
 		double linePerMS = ((double)songModel.getBPM())/(beatsPerMeasure*((double)measuresPerLine))/60.0/1000.0;
 
 		double line = Math.floor(timeDelta*linePerMS);
 		double offset = ((timeDelta-(line/linePerMS))*linePerMS)*measureWidth*measuresPerLine;
 		DrawableNote note = new QuarterNote(new Note(e.getPlayedNote().getNote(), 1, e.getPlayedNote().getVelocity(), this.songModel.getTracks().get(activeTrack)),(int)((xMargin) + offset -10), (int)((yMargin) + line*yMeasureDistance));
 		note.setCorrect(false);
 		drawQueue.add(note);
 	}
 
 	private class MusicEngineTimer implements Runnable{
 
 		private final int FRAMES_PER_SECOND = 300;
 
 		private final double MS_DELAY=1000.0/FRAMES_PER_SECOND;
 		/** number of milliseconds from the epoch of when the last frame started */
 		private long lastFrame = 0;
 
 		/**
 		 * Method which get called by the thread, this is running while the song is playing or paused
 		 */
 		@Override
 		public void run() {
 			while(doContinue) {
 				long now = System.currentTimeMillis();
 				if(!isPaused) {
 					if(now > (lastFrame + MS_DELAY)) {
 						repaint();
 						lastFrame = now;	//We want to run at FRAMES_PER_SECOND fps, so use the beginning of the frame to
 						//ensure that we get the correct frames, no matter how long update takes
 					} else {
 						try {
 							Thread.sleep(1); // Dont eat up all the processor
 						} 
 						catch (InterruptedException e) {}
 					}
 				}else{
 					try {
 						Thread.sleep(1); // Dont eat up all the processor
 					} 
 					catch (InterruptedException e) {}
 				}
 			}
 		}
 	}
 
 }
