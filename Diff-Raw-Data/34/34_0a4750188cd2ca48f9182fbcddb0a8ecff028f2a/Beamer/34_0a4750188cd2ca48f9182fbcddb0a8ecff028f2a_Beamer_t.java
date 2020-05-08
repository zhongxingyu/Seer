 /**
  * 
  */
 package de.findus.cydonia.player;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import com.jme3.effect.ParticleEmitter;
 import com.jme3.effect.ParticleMesh.Type;
 import com.jme3.effect.shapes.EmitterSphereShape;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial.CullHint;
 
 import de.findus.cydonia.main.MainController;
 import de.findus.cydonia.messages.BeamerInfo;
 import de.findus.cydonia.messages.EquipmentInfo;
 
 /**
  * @author Findus
  *
  */
 public abstract class Beamer extends AbstractEquipment {
 
 	private static final String TYPENAME = "Beamer"; 
 	
 	private static BufferedImage hudImg;
 	
 	private String name;
 	
 	private float range;
 	
 	private boolean beaming;
 	
 	private Node geom = new Node("Beamer");
 	
 	private ParticleEmitter beam;
 	
 	public Beamer() {
 		initHUDImgs();
 	}
 	
 	public Beamer(String name, float range, Player player, MainController mainController) {
 		super(mainController);
 		
 		this.name = name;
 		this.range = range;
 		this.player = player;
 		
 		initHUDImgs();
 		
 		initGeometry();
 	}
 	
 	@Override
 	public void initGeometry() {
 		beam = new ParticleEmitter("Beam", Type.Triangle, 30);
         Material mat_red = new Material(getMainController().getAssetManager(), 
                 "Common/MatDefs/Misc/Particle.j3md");
         mat_red.setTexture("Texture", getMainController().getAssetManager().loadTexture(
                 "Effects/Explosion/flame.png"));
         beam.setMaterial(mat_red);
         beam.setImagesX(2); 
         beam.setImagesY(2); // 2x2 texture animation
         beam.setEndColor(  new ColorRGBA(6f, 6f, 1f, 0.5f));
         beam.setStartColor(new ColorRGBA(3f, 3f, 1f, 1f));
         beam.setStartSize(0.01f);
         beam.setEndSize(0.005f);
         beam.setGravity(0, 0, 0);
         beam.setNumParticles(2000);
         beam.getParticleInfluencer().setVelocityVariation(1f);
 	    beam.setEnabled(false);
 	    update();
 	    
 	    this.geom.attachChild(beam);
 	}
 
 	private void initHUDImgs() {
 		try {
 			if(hudImg == null) {
				hudImg = ImageIO.read(this.getClass().getResourceAsStream("/de/findus/cydonia/gui/hud/beamer.png"));
 			}
 		} catch (IOException e) {
 		}
 	}
 	
 	@Override
 	public void usePrimary(boolean activate) {
 		this.setBeaming(activate);
 	}
 
 	@Override
 	public void useSecondary(boolean activate) {
 		// no action yet
 	}
 
 	@Override
 	public void reset() {
 		this.beaming = false;
 	}
 
 	@Override
 	public BufferedImage getHUDImage() {
 		BufferedImage tmpimg = new BufferedImage(35, 35, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D gr = (Graphics2D) tmpimg.getGraphics();
 		
 		gr.drawImage(hudImg, 0, 0, new Color(0, 0, 0, 0), null);
 
 		return tmpimg;
 	}
 
 	@Override
 	public EquipmentInfo getInfo() {
 		return new BeamerInfo(this);
 	}
 
 	@Override
 	public void loadInfo(EquipmentInfo info) {
 		if(info instanceof BeamerInfo) {
 			BeamerInfo i = (BeamerInfo) info;
 			this.name = i.getName();
 			this.range = i.getRange();
 		}
 	}
 	
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the range
 	 */
 	public float getRange() {
 		return range;
 	}
 
 	/**
 	 * @param range the range to set
 	 */
 	public void setRange(float range) {
 		this.range = range;
 	}
 
 	/**
 	 * @return the beaming
 	 */
 	public boolean isBeaming() {
 		return beaming;
 	}
 
 	/**
 	 * @param beaming the beaming to set
 	 */
 	public void setBeaming(boolean beaming) {
 		this.beaming = beaming;
 		update();
 	}
 	
 	public void setActive(boolean active) {
 		beam.setEnabled(active);
 		geom.setLocalTranslation(0, 0.3f, 0.5f);
 		geom.setCullHint(CullHint.Dynamic);
 	}
 	
 	public void update() {
 		if(beaming) {
 			geom.setLocalTranslation(0, 0.3f, 0.2f);
 			float angle = player.getViewDir().normalize().angleBetween(player.getControl().getViewDirection());
 			if(player.getViewDir().normalize().getY() > 0) angle = -angle;
 			beam.setLocalRotation(new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_X));
 			beam.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 80f));
 			beam.getParticleInfluencer().setVelocityVariation(0.01f);
 			beam.setParticlesPerSec(2000);
 			beam.setShape(new EmitterSphereShape(Vector3f.ZERO, 0.01f));
 			beam.setLowLife(0.06f);
 			beam.setHighLife(0.1f);
 		}else {
 			geom.setLocalTranslation(0, 0.3f, 0.5f);
 			beam.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 0.1f));
 			beam.getParticleInfluencer().setVelocityVariation(1f);
 			beam.setParticlesPerSec(200);
 			beam.setShape(new EmitterSphereShape(Vector3f.ZERO, 0.05f));
 			beam.setLowLife(0.05f);
 	        beam.setHighLife(0.13f);
 		}
 	}
 	
 	public Node getGeometry() {
 		return this.geom;
 	}
 
 	@Override
 	public String getTypeName() {
 		return TYPENAME;
 	}
 
 }
