 package ch.bbw.gallery.slotmachine;
 
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 import javax.ws.rs.core.MediaType;
 
 import ch.bbw.gallery.core.models.Image;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.json.JSONConfiguration;
 
 public class RunGameState implements IGameState {
 	/**
 	 * Reference to the next state
 	 */
 	private StartGameState startGameState;
 	
 	/**
 	 * Web resource
 	 */
 	private WebResource webResource;
 	
 	/**
 	 * Random object ensuring game randomness
 	 */
 	private Random random;
 	
 	/**
 	 * The stage of this state
 	 */
 	//private RunGameStateStage stage;
 	
 	/**
 	 * Image of the slot machine
 	 */
 	private BufferedImage slotMachine;
 	
 	/**
 	 * Image of the hatch
 	 */
 	//private BufferedImage hatch;
 	
 	private int currentReelNumber;
 	
 	private Image[] imageList;
 	
 	private SlotMachineReel[] reels;
 	
 	public RunGameState(String path) {
 		Class<?> loader = getClass();
 		
 		// Set up rest client
 		ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		Client c = Client.create(clientConfig);
 		
 		webResource = c.resource(path);
 		
 		// Initialize random object
 		random = new Random(new Date().getTime());
 		
 		try {
 			this.slotMachine = ImageIO.read(loader.getResourceAsStream("/slot.png"));
 			//this.hatch = ImageIO.read(loader.getResourceAsStream("/hatch.png"));
 		} catch (IOException e) {
 		}
 	}
 	
 	@Override
 	public void init() {
 		//this.stage = RunGameStateStage.OPENING;
 		
 		imageList = webResource.path("/images/random")
 				.path("10")
 				.accept(MediaType.APPLICATION_JSON)
 				.get(Image[].class);
 		
 		RunGameStateReelItem[] items = new RunGameStateReelItem[10];
 		
 		for (int i = 0; i < imageList.length; i++) {
 			RunGameStateReelItem tmp = new RunGameStateReelItem();
 			
 			tmp.setImage(webResource.path("/images/thumbnail")
 					.path(imageList[i].getId())
 					.path("160")
 					.path("160")
 					.get(BufferedImage.class));
 			
 			tmp.setInfo(imageList[i]);
 			
 			items[i] = tmp;
 		}
 		
 		this.reels = new SlotMachineReel[3];
 		this.reels[0] = new SlotMachineReel(items, 160, 277, random);
 		this.reels[1] = new SlotMachineReel(items, 160, 277, random);
 		this.reels[2] = new SlotMachineReel(items, 160, 277, random);
 		
 		// TODO: Hatch up
 		
 		this.currentReelNumber = 0;
 	}
 
 	@Override
 	public void destroy() {
 		// TODO: Hatch down
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e, IStateContext context) {
 		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 			if (this.currentReelNumber > 2) {
 				context.setState(this.startGameState);
 			} else {
 				this.reels[currentReelNumber].stop();
 				this.currentReelNumber++;
 			}
 		}
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		this.reels[0].paint(g.create( 56, 85, 160, 277));
 		this.reels[1].paint(g.create(240, 85, 160, 277));
 		this.reels[2].paint(g.create(424, 85, 160, 277));
 		
 		g.drawImage(slotMachine, 0, 0, null);
 	}
 
 	@Override
 	public void process() {
 		this.reels[0].process();
 		this.reels[1].process();
 		this.reels[2].process();
 	}
 	
 	public StartGameState getStartGameState() {
 		return startGameState;
 	}
 
 	public void setStartGameState(StartGameState startGameState) {
 		this.startGameState = startGameState;
 	}
 }
