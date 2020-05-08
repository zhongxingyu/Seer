 package aeroport.sgbag.views;
 
 import java.util.Iterator;
 
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;
 import lombok.extern.log4j.Log4j;
 
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 
 import aeroport.sgbag.controler.ViewSelector;
 import aeroport.sgbag.kernel.*;
 import aeroport.sgbag.utils.ParticleManager;
 import aeroport.sgbag.utils.Rectangle2D;
 
 @NoArgsConstructor
 @Log4j
 public class VueChariot extends VueElem {
 
 	@Getter
 	@Setter
 	private Chariot chariot;
 	
 	@Getter
 	@Setter
 	private double relativeCoefOffsetX;
 	
 	@Getter
 	@Setter
 	private double relativeCoefOffsetY;
 
 	private float lastRailAngle;
 	
 	private int lastX;
 	private int lastY;
 
 	public VueChariot(Canvas parent, Chariot chariot) {
 		super((VueHall) parent);
 		this.image = new Image(parent.getDisplay(), "data/img/chariot.png");
 
 		this.chariot = chariot;
 		Rectangle rect = image.getBounds();
 		width = chariot.getLength() / 2;
 		height = width * rect.height / rect.width;
 		relativeCoefOffsetX = 1.5;
 		relativeCoefOffsetY = 0;
 		lastX = x;
 		lastY = y;
 
 	}
 
 	public void updateView() {
 		ElementCircuit parent = chariot.getParent();
 		VueElem vueParent = (VueElem) ViewSelector.getInstance()
 				.getViewForKernelObject(parent);
 		
 		if (parent instanceof Noeud) {
 			Noeud noeudParent = (Noeud) parent;
 
 			float ratio = noeudParent.getTicksToUpdate()
 					/ ((float) noeudParent.getTickThresholdToUpdate());
 			if (ratio > 1)
 				ratio = 1;
 
 			// Get the next rail :
 			VueRail next = null;
 			if(chariot.getCheminPrevu() != null) {
 				for (Iterator<ElementCircuit> it = chariot.getCheminPrevu()
 						.iterator(); it.hasNext();) {
 					ElementCircuit e = it.next();
 					if (e instanceof Rail) {
 						next = (VueRail) ViewSelector.getInstance()
 								.getViewForKernelObject(e);
 						break;
 					}
 				}
 			}
 
 			if (next != null) {
 				float delta;
 				float angleDepart = convertToProperAngle(this.lastRailAngle);
 				float angleArrivee = convertToProperAngle(next.getAngle());
 				float diff = Math.abs(angleArrivee - angleDepart);
 				if (angleArrivee > angleDepart)
 					delta = ratio * diff;
 				else
 					delta = -ratio * diff;
 				log.debug(angleDepart + "," + next.getAngle());
 
 				this.angle = lastRailAngle + delta;
 			}

 			this.x = vueParent.x;
 			this.y = vueParent.y;
 		} else if (parent instanceof Rail) {
 			Rail railParent = (Rail) parent;
 
 			// Calculate the Chariot's position :
 
 			Rectangle2D rect = vueParent.getRectangle2D();
 			double rapport = ((double) chariot.getPosition())
 					/ ((double) railParent.getLength());
 
 			int xDebutRail = (int) (rect.getBasGauche().x
 					+ (rect.getHautGauche().x - rect.getBasGauche().x)/2 * (relativeCoefOffsetX + 1));
 			int yDebutRail = (int) (rect.getBasGauche().y
 					+ (rect.getHautGauche().y - rect.getBasGauche().y) / 2 * (relativeCoefOffsetY + 1));
 			int xFinRail = (int) (rect.getBasDroit().x
 					+ (rect.getHautDroit().x - rect.getBasDroit().x) / 2 * (relativeCoefOffsetX + 1));
 			int yFinRail = (int) (rect.getBasDroit().y
 					+ (rect.getHautDroit().y - rect.getBasDroit().y) / 2 * (relativeCoefOffsetY + 1));
 
 			int offsetX = (int) (rapport * (xFinRail - xDebutRail));
 			int offsetY = (int) (rapport * (yFinRail - yDebutRail));
 
 			this.x = xDebutRail + offsetX;
 			this.y = yDebutRail + offsetY;
 
 			// Set the Chariot's angle :
 
 			this.angle = vueParent.angle;
 			this.lastRailAngle = vueParent.angle;
 		}
 		if((x-lastX)*(x-lastX)+(y-lastY)*(y-lastY) < chariot.getMaxMoveDistance()/2) {
 			ParticleManager.getParticleManager().throwParticle(x, y);
 		}
 		lastX = x;
 		lastY = y;
 
 	}
 
 	private float convertToProperAngle(float angle) {
 		return (angle + 360) % 360;
 	}
 }
