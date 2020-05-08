 package vues;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 
 import noyau.Aeroport;
 import noyau.Bagage;
 import noyau.Tapis;
 import noyau.Aeroport.Mode;
 
 public class VueTapis extends Vue {
 
 	private Tapis tapis;
 	private Point pointA;
 	private Point pointB;
 	private double alpha;
 	private Image imageAvecBagage;
 	private Image imageAvecBagageSel;
 	
 	/**
 	 * Constructeur de la VueTapis
 	 * @param vueGeneral
 	 * @param image
 	 * @param imageSel
 	 * @param imageAvecBagage
 	 * @param imageAvecBagageSel
 	 * @param tapis
 	 */
 	public VueTapis(VueGenerale vueGeneral, Image image, Image imageSel, 
 			Image imageAvecBagage, Image imageAvecBagageSel, Tapis tapis) {
 		super(vueGeneral, image, imageSel);
 		this.tapis = tapis;
 		this.imageAvecBagage = imageAvecBagage;
 		this.imageAvecBagageSel = imageAvecBagageSel;
 		constructionRectangle();
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	void dessin(Graphics g) {
 		Graphics2D g2d = (Graphics2D)g;
 		g2d.rotate(alpha, pointA.x, pointA.y);
 		for (int i = 0; i < tapis.getListBagages().size(); i++) {
 			if(tapis.getListBagages().elementAt(i) == null){
 				g2d.drawImage(image, pointA.x + i*image.getWidth(vueGenerale), pointA.y, imageWidth, imageHeight, vueGenerale);
 				g2d.drawImage(imageSel, pointA.x + i*image.getWidth(vueGenerale), pointA.y, imageWidth, imageHeight, vueGenerale);
 			}
 			else{
 				g2d.drawImage(imageAvecBagage, pointA.x + i*image.getWidth(vueGenerale), pointA.y, imageWidth, imageHeight, vueGenerale);
 				g2d.drawImage(imageAvecBagageSel, pointA.x + i*image.getWidth(vueGenerale), pointA.y, imageWidth, imageHeight, vueGenerale);
 			}
 		}
 		g2d.rotate(-alpha, pointA.x, pointA.y);
 	}
 
 	@Override
 	void action() {
 		this.selectionner();
 		vueGenerale.setGuichetCourant(null);
 		vueGenerale.setTobogganCourant(null);
 		if(vueGenerale.getChariotCourant().noeudElligible(tapis.getNoeud())){
 			vueGenerale.getChariotCourant().ajouterNoeud(tapis.getNoeud());
 			vueGenerale.getZoneInfo().setText("Destination ajoutée");
 			this.deselectionner();
 		}
 		else{
 			vueGenerale.getZoneInfo().setText("Cette destination n'est pas valide!");
 		}
 	}
 
 	@Override
 	boolean clic(int x, int y) {
 		if(Aeroport.getMode() == Mode.MANUEL){
 			Point p = new Point(x, y);
 			return dansRectangle(p);
 		}
 		else{
 			return false;
 		}
 		
 	}
 	
 	/**
 	 * Construit le rectangle permettant la détection de clic
 	 */
 	private void constructionRectangle(){
		pointA = new Point(tapis.getGuichet().getCoordonnees());
		pointB = new Point(tapis.getNoeud().getCoordonnees());
 		pointA.x = (int) Math.round(pointA.getX()*vueGenerale.getEchelle());
 		pointA.y = (int) Math.round(pointA.getY()*vueGenerale.getEchelle());
 		pointB.x = (int) Math.round(pointB.getX()*vueGenerale.getEchelle());
 		pointB.y = (int) Math.round(pointB.getY()*vueGenerale.getEchelle());
 		/**
 		 * Ici on a les deux points...on va commencer les transformations mathematics pour obtenir le bon rectangle 
 		 * 
 		 */
 		double h = pointB.y - pointA.y;
 		double b = pointB.x - pointA.x;
 		alpha = Math.atan2(h,b);
 		rectangle = new Rectangle((int)Math.round(pointA.x - imageWidth/2), (int)Math.round(pointA.y - imageHeight/2),
 				imageWidth, imageHeight);
 		AffineTransform rotation = AffineTransform.getRotateInstance(alpha, pointA.x, pointA.y);
 		rectangle = rotation.createTransformedShape(rectangle);
 		
 	}
 
 }
