 package vues;
 import ihm.ImagesManager;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
 
 import noyau.*;
 import noyau.Aeroport.Mode;
 public class VueNoeud extends Vue{
 
 	private Noeud noeud;
 	//TODO : set from xml
 	static private int tailleReelle = 2;
 	
 	/**
 	 * Constructeur de la vueNoeud
 	 * @param vueGeneral
 	 * @param image
 	 * @param imageSel
 	 * @param imageGarage
 	 * @param imageGarageSel
 	 * @param noeud
 	 */
 	public VueNoeud(VueGenerale vueGenerale, ImagesManager imagesManager, Noeud noeud) {	
 		super(vueGenerale, imagesManager);
 
 		this.imageWidth = (int)Math.round(tailleReelle*vueGenerale.getEchelle());
 		this.imageHeight = (int)Math.round(tailleReelle*vueGenerale.getEchelle());
 		
 		this.noeud = noeud;
 		
 		posPixel = new Point((int)Math.round(this.noeud.getCoordonnees().x * this.vueGenerale.getEchelle() - imageWidth/2)
 				, (int)Math.round(this.noeud.getCoordonnees().y * this.vueGenerale.getEchelle() - imageHeight/2));
 		
 		rectangle = new Rectangle(posPixel.x, posPixel.y, imageWidth, imageHeight);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	boolean clic(int x, int y) {
 		Point p = new Point(x, y);
 		return dansRectangle(p);
 	}
 
 	@Override
 	void dessin(Graphics g) {
 		Graphics2D g2d = (Graphics2D)g;
 		if(selection){
 			if(noeud instanceof NoeudGarage){
 				g2d.drawImage(imagesManager.getImgNodeGarageSel(), posPixel.x, posPixel.y, imageWidth, imageHeight, vueGenerale);
 				vueGenerale.getZoneInfo().setText("<html>Chariots presents : " + 
 						Aeroport.garage.getListChariotsVides().size()  + 
 						"<br>" + "Chariots en attente de depart : " + 
 						Aeroport.garage.getListChariotsPourPartir().size()+
 						"</html>");
 			}
 			else{
 				g2d.drawImage(imagesManager.getImgNodeSel(), posPixel.x, posPixel.y, imageWidth, imageHeight, vueGenerale);
 			}
 		}
 		else{
 			if(noeud instanceof NoeudGarage){
 				g2d.drawImage(imagesManager.getImgNodeGarage(), posPixel.x, posPixel.y, imageWidth, imageHeight, vueGenerale);
 			}
 			else{
 				g2d.drawImage(imagesManager.getImgNode(), posPixel.x, posPixel.y, imageWidth, imageHeight, vueGenerale);
 			}
 		}
 		if(noeud.getId() < 10){
 			Font f = new Font("Courier", Font.BOLD, imageWidth);
 			g2d.setFont(f);
 			g2d.setColor(Color.WHITE);
 			g2d.drawString(Integer.toString(noeud.getId()), (float)(posPixel.x + imageWidth/4), (float)(posPixel.y + imageHeight/1.25));
 		}
 		else{
 			Font f = new Font("Courier", Font.BOLD, (int)Math.round(imageWidth/1.5));
 			g2d.setFont(f);
 			g2d.setColor(Color.WHITE);
 			g2d.drawString(Integer.toString(noeud.getId()), (float)(posPixel.x + imageWidth/6), (float)(posPixel.y + imageHeight/1.5));
 		}
 	}
 
 	@Override
 	void action() {
 		if(noeud instanceof NoeudGarage){
 			this.selectionner();
 			if(Aeroport.getMode() == Mode.MANUEL){
 				vueGenerale.getBandeauSortirChariot().setVisible(true);
 			}
 		}
 		
 	}
 
 	public Noeud getNoeud() {
 		return noeud;
 	}
 	
 
 }
