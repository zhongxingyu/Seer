 package weapon;
 
 import java.util.HashMap;
 
 import net.phys2d.math.Vector2f;
 import net.phys2d.raw.Body;
 import net.phys2d.raw.CollisionEvent;
 import net.phys2d.raw.World;
 import net.phys2d.raw.shapes.Box;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import personnages.Personnage;
 
 import interfaces.Drawable;
 import interfaces.SlickAdapter;
 
 public abstract class Balle implements Drawable, SlickAdapter {
 
 	// position x et y de la balle
 	private float x;
 	private float y;
 
 	// represente le corps physique de la balle dans le monde physique
 	private Body body;
 
 	// represente la masse de la balle
 	private float masse;
 
 	// represente le monde physique
 	private World world;
 
	// valeur des dgts
 	int value;
 
 	// image de la balle
 	Image imageBalle;
 
 	// connaitre la direction dans laquelle la balle doit se déplacer, true =
 	// droite, false = gauche
 	private boolean directionDroite;
 
 	// la vélocité de la balle sur l'axe des X
 	private float velx;
 
 	/**
 	 * Constructeur de la classe Balle
 	 * 
 	 * @param x
 	 * @param y
 	 * @param directionDroite
 	 * @param masse
 	 * @throws SlickException
 	 */
 	public Balle(float x, float y, Boolean directionDroite, float masse,
 			int value) throws SlickException {
 		this.directionDroite = directionDroite;
 		if (this.directionDroite)
 			this.x = x + 16;
 		else
 			this.x = x - 16;
 		this.y = y;
 		imageBalle = new Image("res/bullet.png");
 		this.masse = masse;
 
 		// cree la balle pour le monde physique
 		this.body = new Body(new Box(10, 10), this.masse);
 		this.body.setUserData(this);
 		this.body.setRestitution(0);
 		this.body.setFriction(0f);
 		this.body.setMaxVelocity(50, 50);
 		this.body.setRotatable(false);
 		this.setPosition(this.x, this.y);
 		this.value = value;
 	}
 
 	/**
 	 * Initialisation : chargement de l'image de la balle
 	 */
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		imageBalle = new Image("res/bullet.png");
 
 	}
 
 	/**
 	 * Affichage de la balle
 	 */
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		render(g);
 	}
 
 	/**
 	 * Retourne le corps de la balle
 	 * 
 	 * @return
 	 */
 	public Body getBody() {
 		return body;
 	}
 
 	/**
 	 * Retourne la coordonnée X du corps de la balle
 	 * 
 	 * @return
 	 */
 	public float getX() {
 		return body.getPosition().getX();
 	}
 
 	/**
 	 * Retourne la coordonnée Y du corps de la balle
 	 * 
 	 * @return
 	 */
 	public float getY() {
 		return body.getPosition().getY();
 	}
 
 	/**
 	 * Retourne la force à appliquer à la balle sur l'axe des X en fonction de
 	 * la direction dans laquelle la balle doit etre tiré
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public void applyForce(float x, float y) {
		if (directionDroite)
 			body.addForce(new Vector2f(x, 0));
 		else
 			body.addForce(new Vector2f(-x, 0));
 	}
 
 	/**
 	 * Met à jour l'affichage des coordonnées de la balle, sa vitesse, l'effet
 	 * de la gravité...
 	 */
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		velx = getVelX();
 		// si une collision est apparu sur l'axe des X, on met la velocite sur
 		// l'axe des X a 0
 
 		if ((getVelX() > 0) && (!directionDroite) || (getVelX() < 0)
 				&& (directionDroite)) {
 			velx = 0;
 		}
 
 		body.setGravityEffected(false);
 		// velocite constante pendant les appels successifs de update
 		setVelocity(velx, getVelY());
 	}
 
 	/**
 	 * Fonction qui permet de savoir si la balle est rentrée en collision avec
 	 * un autre élément du niveau
 	 * 
 	 * @param personnages
 	 * @return
 	 */
 	public boolean collision(HashMap<Body, Personnage> personnages) {
 		if (world == null)
 			return false;
 		Personnage current;
 		// collision avec l'environnement est apparu?
 		CollisionEvent[] events = world.getContacts(body);
 		float Normal_x;
 		for (int i = 0; i < events.length; i++) {
 			Normal_x = events[i].getNormal().getX();
 			// regarde qu'elle corps est rentre en collision avec quelque chose
 			if (Normal_x < -0.5 || Normal_x > 0.5) {
 				// corps B est rentre en collision, est ce la balle ? si oui,
 				// collision, si touche un ennemi, appelle la fonction toucher
 				// de cet ennemi pour lui enlever 1 pt de vie
 				if (events[i].getBodyB() == body) {
 					current = personnages.get(events[i].getBodyA());	/*
 					 * Current vaut null si le body mis en cause n'est pas un
 					 * body de personnages
 					 */
 					if (current != null && cible(current))
 						current.toucher(value);
 
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Fonction qui permet de connaitre la largeur de la balle
 	 * 
 	 * @return
 	 */
 	public float getWidth() {
 		return 10;
 	}
 
 	/**
 	 * Fonction qui permet de connaitre la hauteur de la balle
 	 * 
 	 * @return
 	 */
 	public float getHeight() {
 		return 10;
 	}
 
 	/**
 	 * Fonction qui permet de donner une vélocité à la balle
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public void setVelocity(float x, float y) {
 		Vector2f vec = new Vector2f(body.getVelocity());
 		vec.scale(-1);
 		body.adjustVelocity(vec);
 		body.adjustVelocity(new Vector2f(x, y));
 	}
 
 	/**
 	 * Fonction qui permet de connaitre la vélocité de la balle sur l'axe des
 	 * X
 	 * 
 	 * @return
 	 */
 	public float getVelX() {
 		return body.getVelocity().getX();
 	}
 
 	/**
 	 * Fonction qui permet de connaitre la vélocité de la balle sur l'axe des
 	 * Y
 	 * 
 	 * @return
 	 */
 	public float getVelY() {
 		return body.getVelocity().getY();
 	}
 
 	/**
 	 * Fonction qui permet de mettre la balle dans un monde physique
 	 * 
 	 * @param world
 	 */
 	public void setWorld(World world) {
 		this.world = world;
 	}
 
 	/**
 	 * Fonction qui permet de fixer la position sur l'axe des X et Y de la balle
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public void setPosition(float x, float y) {
 		body.setPosition(x, y);
 	}
 
 	@Override
 	public void render(Graphics g) {
 		imageBalle.drawCentered(getX(), getY());
 
 	}
 
 	public abstract boolean cible(Personnage p);
 }
