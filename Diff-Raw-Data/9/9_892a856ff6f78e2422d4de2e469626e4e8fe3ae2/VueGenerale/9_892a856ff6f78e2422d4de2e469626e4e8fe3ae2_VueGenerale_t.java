 package vues;
 
 import ihm.BandeauAjoutBagages;
 import ihm.BandeauVitesseChariot;
 import ihm.ImagesManager;
 
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import noyau.*;
 import noyau.Aeroport.Mode;
 public class VueGenerale extends JPanel {
 	
 		private Guichet guichetCourant;
 		private Toboggan tobogganCourant;
 		private Chariot chariotCourant;
 		private Aeroport aeroport;
 		private BandeauAjoutBagages bandeauAjoutBagages;
 		private BandeauVitesseChariot bandeauVitesseChariot;
 		private JLabel zoneInfo;
 		private double echelle;
 		private double coefImage;
 		private ArrayList<Vue> listVues;
 
 		public VueGenerale(BandeauAjoutBagages bandeauAjoutBagages, BandeauVitesseChariot bandeauVitesseChariot, 
 				JLabel zoneInfo, Aeroport aeroport, ImagesManager imagesManager){
 			
 			this.aeroport = aeroport;
 			this.bandeauAjoutBagages = bandeauAjoutBagages;
 			this.bandeauVitesseChariot = bandeauVitesseChariot;
 			this.zoneInfo = zoneInfo;
 			this.echelle = Math.max(this.getWidth()/this.aeroport.getLongueur(),
 					this.getHeight()/this.aeroport.getLargeur());
			coefImage = Bagage.TAILLE_BAGAGE*echelle;
 			listVues = new ArrayList<Vue>();
 			
 			List<Chariot> listChariot = aeroport.getListChariots();
 			for(Chariot c: listChariot){
 				listVues.add(new VueChariot(this, imagesManager.getImgChariot(), imagesManager.getImgChariotSel(),
 						imagesManager.getImgChariotB(), imagesManager.getImgChariotBSel(), c));
 			}
 			
 			
 			List<Guichet> listGuichet = aeroport.getListGuichets();
 			for(Guichet g: listGuichet){
 				listVues.add(new VueGuichet(this, imagesManager.getImgGuichet(), imagesManager.getImgGuichetSel(), g));
 			}
 			
 			List<Toboggan> listToboggan = aeroport.getListToboggans();
 			for(Toboggan t: listToboggan){
 				listVues.add(new VueToboggan(this, imagesManager.getImgToboggan(), imagesManager.getImgTobogganSel(), t));
 			}
 			
 			List<Tapis> listTapis = aeroport.getListTapis();
 			for(Tapis p : listTapis){
 				listVues.add(new VueTapis(this, imagesManager.getImgTapis(), imagesManager.getImgTapisSel(), 
 						imagesManager.getImgTapisB(), imagesManager.getImgTapisBSel(), p));
 			}
 			
 			List<Noeud> listNoeuds = aeroport.getListNoeuds();
 			for(Noeud n: listNoeuds){
 				listVues.add(new VueNoeud(this, imagesManager.getImgNode(), imagesManager.getImgNodeSel(),
 						imagesManager.getImgNodeGarage(), imagesManager.getImgNodeGarageSel(), n));
 			}
 			
 			List<Rail> listRail = aeroport.getListRails();
 			for(Rail r : listRail){
 				listVues.add(new VueRail(this,imagesManager.getImgRail(), imagesManager.getImgRailSel(), r));
 			}
 			
 		}
 		
 		public double getCoefImage() {
 			return coefImage;
 		}
 
 		public Toboggan getTobogganCourant() {
 			return tobogganCourant;
 		}
 
 		public void setTobogganCourant(Toboggan tobogganCourant) {
 			this.tobogganCourant = tobogganCourant;
 		}
 
 		public BandeauAjoutBagages getBandeauAjoutBagages() {
 			return bandeauAjoutBagages;
 		}
 
 		public BandeauVitesseChariot getBandeauVitesseChariot() {
 			return bandeauVitesseChariot;
 		}
 
 		public Aeroport getAeroport() {
 			return aeroport;
 		}
 
 		public void setAeroport(Aeroport aeroport) {
 			this.aeroport = aeroport;
 		}
 
 		public JLabel getZoneInfo() {
 			return zoneInfo;
 		}
 		
 		public double getEchelle() {
 			return echelle;
 		}
 
 		public void setEchelle(int echelle) {
 			this.echelle = echelle;
 		}
 
 		public Guichet getGuichetCourant() {
 			return guichetCourant;
 		}
 
 		public void setGuichetCourant(Guichet guichetCourant) {
 			this.guichetCourant = guichetCourant;
 		}
 		
 		public Chariot getChariotCourant() {
 			return chariotCourant;
 		}
 
 		public void setChariotCourant(Chariot chariotCourant) {
 			this.chariotCourant = chariotCourant;
 		}
 		
 		@Override
 		public void paint(Graphics g) {
 			super.paintComponents(g);
 			for (int i = listVues.size()-1 ; i >= 0; i--) {
 				listVues.get(i).dessin(g);
 			}
 		}
 		
 		public void clic(int x, int y) {
 			int i = 0;
 			boolean trouve = false;
 			bandeauAjoutBagages.setVisible(false);
 			bandeauVitesseChariot.setVisible(false);
 			zoneInfo.setText("");
 			for (Vue v : listVues ){
 				v.deselectionner();
 			}
 			while (i < listVues.size() && !trouve) {
 				trouve = listVues.get(i++).clic(x, y);
 			}
 			if(trouve){
 				listVues.get(i-1).action();
 			}
 			else{
 				guichetCourant = null;
 				tobogganCourant = null;
 				chariotCourant = null;
 			}
 		}
 		
 		public void ajouterBagage(){
 			aeroport.ajouterBagage(guichetCourant, tobogganCourant);
 			guichetCourant = null;
 			tobogganCourant = null;
 		}
 		
 		public void setVitesseChariot(float vitesse){
 			chariotCourant.setVitesse(vitesse);
 			chariotCourant = null;
 		}
 		
 		public void avancerTemps(){
 			aeroport.avancerTemps();
 		}
 		
 		public void changerMode(){
 			if(Aeroport.mode == Mode.AUTO){
 				Aeroport.mode = Mode.MANUEL;
 			}
 			else{
 				Aeroport.mode = Mode.AUTO;
 			}
 		}
 		
 		public String getMode(){
 			if(Aeroport.mode == Mode.AUTO){
 				return "Auto";
 			}
 			else{
 				return "Manuel";
 			}
 		}
 		
 }
