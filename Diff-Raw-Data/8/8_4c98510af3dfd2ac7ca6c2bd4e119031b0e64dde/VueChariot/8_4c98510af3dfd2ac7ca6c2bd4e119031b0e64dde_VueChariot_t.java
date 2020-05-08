 package vues;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import noyau.Aeroport.Mode;
 import noyau.Aeroport;
 import noyau.Chariot;
 import noyau.Noeud;
 
 public class VueChariot extends Vue {
 
 	private Image imageAvecBagage;
 	private Image imageAvecBagageSel;
 	private Chariot chariot;
 	private double alpha;
 
 	/**
 	 * Constructeur de la VueChariot
 	 * 
 	 * @param vueGenerale
 	 * @param image
 	 * @param imageSel
 	 * @param imageAvecBagage
 	 * @param imageAvecBagageSel
 	 * @param chariot
 	 */
 	public VueChariot(VueGenerale vueGenerale, Image image, Image imageSel,
 			Image imageAvecBagage, Image imageAvecBagageSel, Chariot chariot) {
 		super(vueGenerale, image, imageSel);
 		this.imageAvecBagage = imageAvecBagage;
 		this.imageAvecBagageSel = imageAvecBagageSel;
 		this.chariot = chariot;
 		this.alpha=0;
 		posPixel = new Point((int) Math.round(this.chariot.getCoordonnees().x
 				* this.vueGenerale.getEchelle() - imageWidth / 2),
 				(int) Math.round(this.chariot.getCoordonnees().y
 						* this.vueGenerale.getEchelle() - imageHeight / 2));
 		
 		//TODO : refaire rectangle clic
 		rectangle = new Rectangle(posPixel.x, posPixel.y, imageHeight,
 				imageWidth);
 	}
 
 	/**
 	 * Met a jour la position du chariot et du rectangle de selection en
 	 * fonction de la position de l'objet du noyau
 	 */
 	private void updatePos() {
 		posPixel.x = (int) Math.round(chariot.getCoordonnees().x
 				* vueGenerale.getEchelle() - imageWidth / 2);
 		posPixel.y = (int) Math.round(chariot.getCoordonnees().y
 				* vueGenerale.getEchelle() - imageHeight / 2);
 		calculerAngleTour();
 		rectangle = new Rectangle(posPixel.x, posPixel.y, imageWidth,
 				imageHeight);
 		AffineTransform rotation = AffineTransform.getRotateInstance(alpha, posPixel.x + imageWidth / 2, posPixel.y
 				+ imageHeight / 2);
 		rectangle = rotation.createTransformedShape(rectangle);
 	}
 
 	@Override
 	boolean clic(int x, int y) {
 		Point p = new Point(x, y);
 		updatePos();
 		return dansRectangle(p);
 	}
 
 	@Override
 	void dessin(Graphics g) {
 		Graphics2D g2d = (Graphics2D) g;
 		updatePos();
 		g2d.rotate(alpha, posPixel.x + imageWidth / 2, posPixel.y
 					+ imageHeight / 2);
 		
 		if (chariot.getCoordonnees().x != 0
 				|| chariot.getCoordonnees().y != 0) {
 			
 			if (selection) {
 				if (chariot.getBagage() == null) {
 					g2d.drawImage(imageSel, posPixel.x, posPixel.y, imageWidth,
 							imageHeight, vueGenerale);
 				} else {
 					g2d.drawImage(imageAvecBagageSel, posPixel.x, posPixel.y,
 							imageWidth, imageHeight, vueGenerale);
 				}
				if(chariot.getDestination() !=  null){
					vueGenerale.getZoneInfo()
			        .setText("Chariot n" + chariot.getId() + " Destination Noeud n" + 
			        						chariot.getDestination().getId());
				}
 			} else {
 				if (chariot.getBagage() == null) {
 					g2d.drawImage(image, posPixel.x, posPixel.y, imageWidth,
 							imageHeight, vueGenerale);
 				} else {
 					g2d.drawImage(imageAvecBagage, posPixel.x, posPixel.y,
 							imageWidth, imageHeight, vueGenerale);
 				}
 			}
 		}
 		g2d.rotate(-alpha, posPixel.x + imageWidth / 2, posPixel.y
 				+ imageHeight / 2);
 	}
 
 	@Override
 	void action() {
 		this.selectionner();
 		vueGenerale.setChariotCourant(this);
 		vueGenerale.setGuichetCourant(null);
 		vueGenerale.setTobogganCourant(null);
 		vueGenerale.getBandeauVitesseChariot().setNumChariot(
 				this.chariot.getId());
 		vueGenerale.getBandeauVitesseChariot().setVitesseChariot(
 				this.chariot.getVitesse());
 		vueGenerale.getBandeauVitesseChariot().setVisible(true);
 		if (Aeroport.getMode() == Mode.MANUEL) {
 			vueGenerale.getZoneInfo()
 			           .setText("Veuillez selectionner la destination suivante du chariot.");
 		}
 	}
 
 	private void calculerAngleTour() {
 		Noeud prochainNoeud = chariot.getProchainNoeud();
 		if(prochainNoeud != null)
 		{
 			int xAux = (int) Math.round(prochainNoeud.getCoordonnees().x
 					* vueGenerale.getEchelle() - imageWidth / 2);
 			int yAux = (int) Math.round(prochainNoeud.getCoordonnees().y
 					* vueGenerale.getEchelle() - imageHeight / 2);
 			Point prochain = new Point(xAux, yAux);
 			double h = prochain.y - posPixel.y;
 			double b = prochain.x - posPixel.x;
 			alpha=Math.atan2(h, b);
 		}
 		
 	}
 
 	public Chariot getChariot() {
 		return chariot;
 	}
 
 }
