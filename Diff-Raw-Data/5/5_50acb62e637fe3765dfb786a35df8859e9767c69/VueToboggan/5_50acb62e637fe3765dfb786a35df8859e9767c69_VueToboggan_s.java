 package vues;
 
 import ihm.ImagesManager;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import noyau.Aeroport;
 import noyau.Toboggan;
 import noyau.Aeroport.Mode;
 
 public class VueToboggan extends Vue {
 
 	private Toboggan toboggan;
 	//TODO : set from xml
 	static private int tailleReelle = 2;
 		
 	/**
 	 * Constructeur de la VueToboggan
 	 * @param vueGenerale
 	 * @param image
 	 * @param imageSel
 	 * @param toboggan
 	 */
 	public VueToboggan(VueGenerale vueGenerale, ImagesManager imagesManager, Toboggan toboggan) {
 		super(vueGenerale, imagesManager);
 		this.toboggan = toboggan;
 		
 		this.imageWidth = (int)Math.round(tailleReelle*vueGenerale.getEchelle());
 		this.imageHeight = (int)Math.round(tailleReelle*vueGenerale.getEchelle());
 		
 		posPixel = new Point((int)Math.round(this.toboggan.getCoordonnees().x * this.vueGenerale.getEchelle() - imageWidth/2)
 				, (int)Math.round(this.toboggan.getCoordonnees().y * this.vueGenerale.getEchelle() - imageHeight/2));
 		
 		rectangle = new Rectangle(posPixel.x, posPixel.y, imageWidth, imageHeight);
 	}
 	
 	@Override
 	void dessin(Graphics g){
 		Graphics2D g2d = (Graphics2D)g;
 		if(selection){
 			g2d.drawImage(imagesManager.getImgTobogganSel(), posPixel.x, posPixel.y, imageWidth, imageHeight, vueGenerale);
 		}
 		else{
 			g2d.drawImage(imagesManager.getImgToboggan(), posPixel.x, posPixel.y, imageWidth, imageHeight, vueGenerale);
 		}
 		if(toboggan.getId() < 10){
 			Font f = new Font("Courier", Font.BOLD, imageWidth);
 			g2d.setFont(f);
 			g2d.setColor(Color.WHITE);
			g2d.drawString(Integer.toString(toboggan.getNoeud().getId()), (float)(posPixel.x + imageWidth/4), (float)(posPixel.y + imageHeight/1.25));
 		}
 		else{
 			Font f = new Font("Courier", Font.BOLD, (int)Math.round(imageWidth/1.5));
 			g2d.setFont(f);
 			g2d.setColor(Color.WHITE);
			g2d.drawString(Integer.toString(toboggan.getNoeud().getId()), (float)(posPixel.x + imageWidth/6), (float)(posPixel.y + imageHeight/1.5));
 		}
 	}
 	
 	@Override
 	void action() {
 		this.selectionner();
 		vueGenerale.setChariotCourant(null);
 		vueGenerale.setTobogganCourant(this);
 		if(vueGenerale.getGuichetCourant() != null){
 			vueGenerale.getGuichetCourant().selectionner();
 			vueGenerale.getZoneInfo().setText("Pour ajouter un bagage cliquez sur Valider");
 			vueGenerale.getBandeauAjoutBagages().setNumeros(vueGenerale.getGuichetCourant().getGuichet().getId(), 
 					vueGenerale.getTobogganCourant().getToboggan().getId());
 			vueGenerale.getBandeauAjoutBagages().setVisible(true);
 		}
 		else{
 			vueGenerale.getZoneInfo().setText("Veuillez selectionner un Guichet");
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
 
 	public Toboggan getToboggan() {
 		return toboggan;
 	}
 	
 }
