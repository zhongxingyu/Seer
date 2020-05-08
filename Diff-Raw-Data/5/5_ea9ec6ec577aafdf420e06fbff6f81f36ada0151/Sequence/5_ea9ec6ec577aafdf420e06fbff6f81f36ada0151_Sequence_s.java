 package beatbots.simulation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.tiled.TiledMap;
 
 public class Sequence implements Entity, MetronomeListener {
 	
 	private Metronome metronome;
 	
 	String sequenceReference;
 	
 	private List<NoteColor> sequence;
 	
 	private Queue<NoteColor> recording;
 	
 	private Sound soundRed;
 	private Sound soundBlue;
 	private Sound soundYellow;
 	private Sound soundMagenta;
 	private Sound soundOrange;
 	private Sound soundGreen;
 	
 	private int score;
 	
 	private boolean startedRecording;
 	
 	private int count;
 
 	@Override
 	public boolean isActive() {
 		
 		return true;
 	}
 	
 	public int getScore() {
 		
 		if ((this.count == 0) || (this.sequence.isEmpty())) {
 			
 			return 0;
 		}
 		else {
 			
 			return (this.score * 100) / Math.min(this.count, this.sequence.size());
 		}
 	}
 	
 	public Sequence(Metronome metronome, String sequenceReference) {
 		
 		super();
 		
 		this.metronome = metronome;
 		this.sequenceReference = sequenceReference;
 		
 		this.sequence = new ArrayList<NoteColor>();
 		
 		this.recording = new ConcurrentLinkedQueue<NoteColor>();
 	}
 
 	@Override
 	public void init(GameContainer container) throws SlickException {
 		
 		this.score = 0;
 		
 		this.startedRecording = false;
 		
 		this.count = 0;
 		
 		this.sequence.clear();
 		
 		TiledMap tiledMap = new TiledMap(this.sequenceReference);
 
 		for (int y = 0; y < tiledMap.getHeight(); y++) {
 			
 			for (int x = 0; x < tiledMap.getWidth(); x++) {
 				
 				for (int l = 0; l < tiledMap.getLayerCount(); l++) {
 					
 					int tileId = tiledMap.getTileId(x, y, l);
 					
 					int c = Integer.parseInt(tiledMap.getTileProperty(tileId, "Color", "-1"));
 					
 					this.sequence.add(Utils.getNoteColor(c));
 				}
 			}
 		}
 		
 		this.soundRed = new Sound("assets/sfx/Red.wav");
 		this.soundBlue = new Sound("assets/sfx/Blue.wav");
 		this.soundYellow = new Sound("assets/sfx/Yellow.wav");
 		this.soundMagenta = new Sound("assets/sfx/Magenta.wav");
 		this.soundOrange = new Sound("assets/sfx/Orange.wav");
 		this.soundGreen = new Sound("assets/sfx/Green.wav");
 		
 		this.metronome.addListener(this);
 	}
 
 	@Override
 	public void update(GameContainer container, int delta) throws SlickException {
 		
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics graphics) {
 		
 		int x = 32 * 5;
 		int y = 4;
 		
 		for (int i = this.count; i < Math.min(this.sequence.size(), this.count + 10); i++) {
 			
 			graphics.setColor(Utils.getColor(this.sequence.get(i)));
 			
 			graphics.drawRect(x, y, 28, 28);
 			
 			if (i == this.count) {
 				
 				graphics.fillRect(x + 4, y + 4, 21, 21);
 			}
 			
 			x += 32;
 		}
 		
 		graphics.setColor(Color.white);
 		
 		graphics.drawString("SCORE: " + Integer.toString(this.getScore()) + "%", 4, 4);
 	}
 
 	@Override
 	public void deactivate() {
 		
 	}
 
 	@Override
 	public void destroy(GameContainer gameContainer) {
 		
 		this.metronome.removeListener(this);
 	}
 
 	@Override
 	public void beat(int beatCount) {
 		
 		if (this.startedRecording) {
 			
 			if (!this.recording.isEmpty()) {
 				
 				if (this.recording.peek() == this.sequence.get(this.count)) {
 					
 					this.score++;
 				}
 				
				while (!this.recording.isEmpty()) {
 					
 					switch (this.recording.remove()) {
 					
 					case Red: 
 						
 						this.soundRed.play();
 						
 						break;
 						
 					case Blue: 
 						
 						this.soundBlue.play();
 						
 						break;
 						
 					case Yellow: 
 						
 						this.soundYellow.play();
 						
 						break;
 						
 					case Magenta: 
 						
 						this.soundMagenta.play();
 						
 						break;
 						
 					case Orange:
 						
 						this.soundOrange.play();
 						
 						break;
 						
 					case Green:
 						
 						this.soundGreen.play();
 						
 						break;
 					}
				}
 			}
 			
 			this.count++;
 		}
 	}
 	
 	public void record(NoteColor noteColor) {
 		
 		this.recording.add(noteColor);
 		
 		this.startedRecording = true;
 	}
 }
