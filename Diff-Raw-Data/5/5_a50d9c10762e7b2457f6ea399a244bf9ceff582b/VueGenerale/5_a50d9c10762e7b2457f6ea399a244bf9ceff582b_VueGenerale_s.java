 package vues;
 
 import ihm.BandeauAjoutBagages;
 import ihm.BandeauVitesseChariot;
 
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import org.w3c.dom.Element;
 
 import noyau.*;
 public class VueGenerale extends JPanel {
 	
 		private Guichet guichetCourant;
 		private Toboggan tobogganCourant;
 		private Chariot chariotCourant;
 		private Aeroport aeroport;
 		private BandeauAjoutBagages bandeauAjoutBagages;
 		private BandeauVitesseChariot bandeauVitesseChariot;
 		private JLabel zoneInfo;
 		private double echelle;
 		private Image image;
 		private ArrayList<Vue> listVues;
 
 		@SuppressWarnings("null")
 		public VueGenerale(BandeauAjoutBagages bandeauAjoutBagages, BandeauVitesseChariot bandeauVitesseChariot, 
				JLabel zoneInfo, Aeroport aeroport, Image imageChariot, Image imageNode, Image imageGuichet, 
				Image imageToboggan, Image imageTapis, Image imageRail){
 			
 			this.aeroport = aeroport;
 			this.bandeauAjoutBagages = bandeauAjoutBagages;
 			this.bandeauVitesseChariot = bandeauVitesseChariot;
 			this.zoneInfo = zoneInfo;			
 			
 			listVues = new ArrayList<Vue>();
 			
 			List<Chariot> listChariot = aeroport.getListChariots();
 			for(Chariot c: listChariot){
 				listVues.add(new VueChariot(this, imageChariot, imageChariot, c));
 			}
 			
 			
 			List<Guichet> listGuichet = aeroport.getListGuichets();
 			for(Guichet g: listGuichet){
 				listVues.add(new VueGuichet(this,image,g));
 			}
 			
 			List<Toboggan> listToboggan = aeroport.getListToboggans();
 			for(Toboggan t: listToboggan){
 				listVues.add(new VueToboggan(this,imageToboggan,t));
 			}
 			
 			List<Tapis> listTapis = aeroport.getListTapis();
 			for(Tapis p : listTapis){
 				listVues.add(new VueTapis(this,imageTapis,p));
 			}
 			
 			List<Noeud> listNoeuds = aeroport.getListNoeuds();
 			for(Noeud n: listNoeuds){
 				listVues.add(new VueNoeud(this,imageNode,n));
 			}
 			
 			List<Rail> listRail = aeroport.getListRails();
 			for(Rail r : listRail){
 				listVues.add(new VueRail(this,imageRail,r));
 			}
 			
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
 				
 		public void redessiner(){
 			for (int i = listVues.size(); i >= 0; i--) {
 				listVues.get(i).dessin(this.getGraphics());
 			}			
 		}
 		
 		public void clic(int x, int y){
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
 		
 		public int construireToutAPartirDeXML(Element aeroportElement){
 		// On cree l'element Aeroport et la vue qui lui est associee
 		//Aeroport aeroport = new Aeroport(null, null, null, null, null);
 		/* TODO
 
 		 	* Crer la vue generale avec le constructeur complet de VueGeneral
 			* Apres avoir tout charge depuis le fichier XML
 			* 
 			* vueGeneral = new VueGeneral(bandeauAjoutBagages,bandeauVitesseChariot,
 			  labelInfo, ....., ...., .....);
 			  vueGenerale.addMouseListener(clicVueGenerale);
 		 	*/
 			
 			/*this.vueGenerale = null;
 			if (aeroport.construireAPartirDeXML(aeroportElement) != Aeroport.PARSE_OK){
 				return Aeroport.PARSE_ERROR;
 			}
 		
 			VueGenerale vueGenerale = new VueGenerale(aeroport, null, null, null, null, null, null);
 		
 			vueGenerale.addMouseListener(clicVueGenerale);
 		
 			this.vueGenerale = vueGenerale;
 		 	*/
 			return Aeroport.PARSE_OK;
 		}
 }
