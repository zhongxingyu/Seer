 /**
  * 
  */
 package de.findus.cydonia.player;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.util.Collection;
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.material.Material;
 import com.jme3.material.RenderState.BlendMode;
 import com.jme3.renderer.queue.RenderQueue.Bucket;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.texture.Image;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture2D;
 import com.jme3.texture.plugins.AWTLoader;
 
 import de.findus.cydonia.events.EventMachine;
 import de.findus.cydonia.level.WorldController;
 
 /**
  * @author Findus
  *
  */
 public class PlayerController {
 	
 	private AssetManager assetManager;
     
    private WorldController worldController;

	private EventMachine eventMachine;
 
 	private ConcurrentHashMap<Integer, Player> players;
 
	public PlayerController(AssetManager assetManager, WorldController worldController, EventMachine eventMachine) {
 		this.assetManager = assetManager;
		this.worldController = worldController;
		this.eventMachine = eventMachine;
         
         players = new ConcurrentHashMap<Integer, Player>();
 	}
 	
 	public Player createNew(int id) {
 		Player p = new Player(id);
		p.getEquips().add(new Picker("defaultPicker1", 15, 1, p, this.worldController, this.eventMachine));
		p.getEquips().add(new Picker("defaultPicker3", 5, 3, p, this.worldController, this.eventMachine));
 		
 		players.put(p.getId(), p);
 		
 		return p;
 	}
 	
 	public Player getPlayer(int id) {
 		return players.get(id);
 	}
 	
 	public Collection<Player> getAllPlayers() {
 		return players.values();
 	}
 	
 	public void removePlayer(int id) {
 		players.remove(id);
 	}
 	
 	public void removeAllPlayers() {
 		players.clear();
 	}
 	
 	public void updateModel(Player p) {
 		if(p == null) return;
 		
 		Spatial model;
 		if(p.getTeam() == 1) {
 			model = (Node) assetManager.loadModel("de/findus/cydonia/models/blue/Sinbad.mesh.xml");
 		}else if(p.getTeam() == 2) {
 			model = (Node) assetManager.loadModel("de/findus/cydonia/models/red/Sinbad.mesh.xml");
 		}else {
 			model = (Node) assetManager.loadModel("de/findus/cydonia/models/green/Sinbad.mesh.xml");
 		}
 		model.setName("player" + p.getId());
 		model.setLocalScale(0.2f);
 		model.addControl(p.getControl());
 		model.setShadowMode(ShadowMode.Cast);
 		model.setQueueBucket(Bucket.Transparent);
 		
 		p.setModel(model);
 		setTransparency(p, 0.5f);
 	}
 	
 	public void setTransparency(Player p, float transparency) {
 		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
 		int cw = Math.round(255*transparency);
 		Color c = new Color(cw, cw, cw);
 		img.setRGB(0, 0, c.getRGB());
 		
 		AWTLoader loader =new AWTLoader();
 		Image imageJME = loader.load(img, true);
 		Texture t = new Texture2D(imageJME);
 		
 		Node n = (Node) p.getModel();
 		for(Spatial s : n.getChildren()) {
 			if(s instanceof Geometry) {
 				Material m = ((Geometry) s).getMaterial();
 				m.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
 				m.setTexture("AlphaMap", t);
 			}
 		}
 	}
 	
 	public void setTeam(Player p, int team) {
 		if(p == null) return;
 		
 		p.setTeam(team);
 		updateModel(p);
 	}
 
 	public int getPlayerCount() {
 		return players.size();
 	}
 }
