 package personnages;
 
 import jeu.Monde;
 
 import net.phys2d.raw.Collide;
 
 import org.newdawn.slick.GameContainer;
 
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import weapon.Balle;
 import weapon.BalleEnnemiVert;
 
 public abstract class Ennemis extends Personnage {
 	// l'image qui contient le sprite du robot
 	private int deplacementAuto;
 	private boolean deplacementAutoDroite;
 
 	public Ennemis(float x, float y, float masse, float tailleBlockPerso,
 			Monde monde) {
 		super(x, y, masse, tailleBlockPerso, monde);
 		deplacementAuto = 0;
 		deplacementAutoDroite = true;
 	}
 
 	// gere le deplacement automatique des ennemies (a revoir, juste un test)
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		super.update(container, game, delta);
 //		Balle balle;
 //		if (deplacementAuto < 230 && deplacementAutoDroite) {
 //			applyForce(100, getVelY());
 //			deplacementAuto++;
 //		} else {
 //			deplacementAutoDroite = false;
 //			applyForce(-100, getVelY());
 //			deplacementAuto--;
 //			if (deplacementAuto == 0) {
 //				deplacementAutoDroite = true;
 //				balle = new BalleEnnemiVert(getX(), getY(),
 //						getDirectionDroite(), 0.01f, 2);
 //				balle.applyForce(10000, 0);
 //				monde.addBalles(balle);
 //
 //			}
 //		}
 		
 		if (deplacementAutoDroite && monde.estSolPosition((int)(this.getX()+16), (int)(this.getY()+32))) {
 			applyForce(100, getVelY());
 			deplacementAutoDroite = true;
 			if(!monde.estSolPosition((int)(this.getX()+32), (int)(this.getY()+32))) {
 				deplacementAutoDroite = false;
 			}
 		}
		else if (!deplacementAutoDroite && monde.estSolPosition((int)(this.getX()-32), (int)(this.getY()+32))) {
 			applyForce(-100, getVelY());
 			deplacementAutoDroite = false;
			if(monde.estSolPosition((int)(this.getX()-32), (int)(this.getY()-32))) {
 				deplacementAutoDroite = true;
 			}
 		}
 	}
 
 }
