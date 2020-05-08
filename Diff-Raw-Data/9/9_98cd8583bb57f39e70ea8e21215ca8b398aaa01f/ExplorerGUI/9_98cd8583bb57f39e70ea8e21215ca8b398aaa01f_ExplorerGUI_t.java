 package cx.it.hyperbadger.spacezombies.explorer;
 
 import java.awt.Font;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.font.effects.ColorEffect;
 
 import cx.it.hyperbadger.spacezombies.TextureBuffer;
 import cx.it.hyperbadger.spacezombies.TextureName;
 import cx.it.hyperbadger.spacezombies.gui.GUI;
 import cx.it.hyperbadger.spacezombies.gui.GUIButton;
 import cx.it.hyperbadger.spacezombies.gui.GUIComponent;
 import cx.it.hyperbadger.spacezombies.gui.GUIEvent;
 import cx.it.hyperbadger.spacezombies.gui.GUIFont;
 import cx.it.hyperbadger.spacezombies.gui.GUIListener;
 
 public class ExplorerGUI extends GUI implements GUIListener{
 	private TextureBuffer textureBuffer = null;
 	private Ship ship = null;
 	private SolarSystem solarSystem = null;
 	private ArrayList<Waypoint> waypoints = null;
 	private GUIFont guiFont = null;
 	public ExplorerGUI(Ship ship, SolarSystem solarSystem){
 		this.ship = ship;
 		this.solarSystem = solarSystem;
 		waypoints = new ArrayList<Waypoint>();
 		//some useful data
 		double shipVelocity = ship.getVelocity();
 		long shipForce = ship.getForce();
 		// ship.setForce(10000);
 		// ship.setAuto(true/false);
 		// ship.setAutoVelocity(autoVelocity);
 		//create a texture buffer and add textures
 		ArrayList<TextureName> textures = new ArrayList<TextureName>();
 		textures.add(new TextureName("spaceship.png"));
 		textures.add(new TextureName("waypoint.png"));
 
 		textureBuffer = new TextureBuffer(textures);
 		//initialize some components here and add them to an array
 		
 		//example button
 		GUIButton b = new GUIButton(new Point2D.Double(10,10),new Point2D.Double(14,14),textureBuffer.getTexture("spaceship.png"),"simple_button");
 		//set its listener to this
 		b.setListener(this);
 		//add the component to the draw loop
 		this.addComponent(b);
 		
 		//text example
 		/*
 		// load font from a .ttf file
 		try {
 			InputStream inputStream	= ResourceLoader.getResourceAsStream("myfont.ttf");
 
 			Font awtFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
 			awtFont = awtFont.deriveFont(24f); // set font size
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		 */
 		Font awtFont = new Font("Times New Roman", Font.BOLD, 24);
 		guiFont = new GUIFont(awtFont);
 		guiFont.getEffects().add(new ColorEffect(java.awt.Color.white));
 	    guiFont.addAsciiGlyphs();
 	    try {
 	        guiFont.loadGlyphs();
 	    } catch (SlickException ex) {
 	       // Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
 	    }
 
 		//the rest goes in the draw loop
 		
 		//waypoints
 		waypoints.add(new Waypoint(ship, solarSystem.findMass("Sun"),textureBuffer.getTexture("waypoint.png")));
 	}
 	/**
 	 * This is here because it implements listening
 	 */
 	@Override
 	public void actionOccured(GUIEvent event, GUIComponent source) {
 		System.out.println(source.getName()+" - "+event);
 	}
 	@Override
 	public void draw() {
 		for(GUIComponent g: guiComponents){
 			g.draw();
 		}
 		for(Waypoint w: waypoints){
 			w.draw();
 		}
 		//the rest of the text example
 		Point2D topLeft = new Point2D.Double(20,10);
		Point2D bottomRight = new Point2D.Double(26,15);
 		guiFont.drawString(topLeft, bottomRight, "This is an exmaple text");
 	}
 }
