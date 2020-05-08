 package aeroport.sgbag.controler;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.graphics.Point;
 
 import lombok.*;
 import lombok.extern.log4j.Log4j;
 import aeroport.sgbag.gui.PropertiesWidget;
 import aeroport.sgbag.kernel.*;
 import aeroport.sgbag.utils.CircuitGenerator;
 import aeroport.sgbag.utils.UtilsCircuit;
 import aeroport.sgbag.views.*;
 import aeroport.sgbag.xml.CircuitArchive;
 import aeroport.sgbag.xml.MalformedCircuitArchiveException;
 
 /**
  * Create things like kernel instances and views.
  * 
  * @author Arnaud Lahache & Stanislas Signoud
  * 
  */
 @RequiredArgsConstructor
 @Log4j
 public class Simulation {
 
 	public enum Mode {
 		MANUEL, AUTO
 	}
 
 	public enum Etat {
 		NORMAL, SELECTION, CHOIX_DESTINATION_BAGAGE, CHOIX_DESTINATION_CHARIOT
 	}
 
 	@Getter
 	@Setter
 	private File xmlFile;
 
 	@Getter
 	@NonNull
 	private VueHall vueHall;
 
 	@Getter
 	private Hall hall;
 
 	@Getter
 	private Mode mode;
 
 	@Getter
 	@Setter
 	private Etat etat;
 
 	@Getter
 	private Clock clock;
 
 	@Getter
 	private VueElem selectedElem;
 	
 	@Getter
 	@Setter
 	private LinkedList<Rail> railsFleches;
 
 	@Setter
 	private PropertiesWidget propertiesWidget;
 
 	public void init() throws MalformedCircuitArchiveException, FileNotFoundException, IOException {
 
 		if(xmlFile != null){
 			CircuitArchive ca = CircuitArchive.readFromXML(xmlFile.getPath());
 		
 			ca.unpackTo(vueHall);
 		}
 
 		vueHall.setSimulation(this);
 		hall = vueHall.getHall();
 
 		if (hall != null) {
 			hall.update();
 		}
 		vueHall.update();
 		vueHall.draw();
 
 		clock = new Clock(100, hall, vueHall);
 		etat = Etat.NORMAL;
 	}
 
 	public void play() {
 		if (clock != null) {
 			clock.run();
 		}
 	}
 
 	public void pause() {
 		if (clock != null) {
 			clock.pause();
 		}
 	}
 
 	public void stop() {
 		if (clock != null) {
 			clock.pause();
 
 			getHall().getBagagesList().clear();
 			
 			for(FileBagage fb : getHall().getFileBagageList()) {
 				fb.removeAllBagage();
 			}
 		}
 	}
 	
 	public void setSpeed(int refreshInterval) {
 		clock.setInterval(refreshInterval);
 		log.debug("Set interval to "+refreshInterval+"ms.");
 	}
 
 	public void setSelectedElem(VueElem _selectedElem) {
 		if (selectedElem != null)
 			selectedElem.setSelected(false);
 		selectedElem = _selectedElem;
 		if (selectedElem != null)
 			selectedElem.setSelected(true);
 
 		propertiesWidget.refresh();
 	}
 
 	public boolean createBagage(VueElem depart, VueElem destination) {
 
 		if (!(destination instanceof VueToboggan)
 				|| !(depart instanceof VueTapisRoulant)) {
 			return false;
 		}
 		VueToboggan vueToboggan = (VueToboggan) destination;
 		VueTapisRoulant vueTapisRoulant = (VueTapisRoulant) depart;
 
 		// Create the bagage in kernel :
 		Bagage b = UtilsCircuit.getUtilsCircuit().generateBagage(
 				vueToboggan.getToboggan().getConnexionCircuit());
 		vueTapisRoulant.getTapisRoulant().addBagage(b);
 		b.setParent(vueTapisRoulant.getTapisRoulant());
 
 		// Create the vueBagage :
 		VueBagage v = new VueBagage(vueHall);
 		v.setBagage(b);
 		vueHall.ajouterVue(v, 4);
 		ViewSelector.getInstance().setKernelView(b, v);
 
 		return true;
 	}
 	
 	public boolean createBagage(VueElem depart) {
 
 		if (!(depart instanceof VueTapisRoulant)) {
 			return false;
 		}
 		
 		VueTapisRoulant vueTapisRoulant = (VueTapisRoulant) depart;
 
 		if(!vueTapisRoulant.getTapisRoulant().hasPlaceForBagage()){
 			return false;
 		}
 		// Create the bagage in kernel :
 		Bagage b = UtilsCircuit.getUtilsCircuit().generateBagage();
 		vueTapisRoulant.getTapisRoulant().addBagage(b);
 		b.setParent(vueTapisRoulant.getTapisRoulant());
 
 		// Create the vueBagage :
 		VueBagage v = new VueBagage(vueHall);
 		v.setBagage(b);
 		vueHall.ajouterVue(v, 4);
 		ViewSelector.getInstance().setKernelView(b, v);
 
 		return true;
 	}
 
 	public boolean createChariot(VueElem depart) {
 
 		if (!(depart instanceof VueRail)) {
 			return false;
 		}
 
 		VueRail vueRail = (VueRail) depart;
 
 		Chariot c = null;
 		
 		// Create the bagage in kernel :
 		if (this.mode == Simulation.Mode.AUTO) {
 			c = UtilsCircuit.getUtilsCircuit().generateChariot();
 			c.setCheminPrevu(this.hall.getCircuit().calculChemin(vueRail.getRail().getNoeudSuivant(), c.getDestination()));
 			
 		} else if (this.mode == Simulation.Mode.MANUEL) {
 			c = UtilsCircuit.getUtilsCircuit().generateChariot(null);
 		} else {
 			return false;
 		}
 
 		vueRail.getRail().registerChariot(c);
 		c.setParent(vueRail.getRail());
 
 		VueChariot v = new VueChariot(vueHall, c);
 		vueHall.ajouterVue(v, 3);
 		ViewSelector.getInstance().setKernelView(c, v);
 		
 		return true;
 	}
 
 	public void setMode(Mode mode) {
 		deleteFleches();
 		
 		if (mode == Mode.MANUEL) {
 			// Passer en mode manuel
 			vueHall.getHall().setAutomatique(false);
 			for (int i = 0; i < vueHall.getHall().getFileBagageList().size(); i++) {
 				if(vueHall.getHall().getFileBagageList().get(i) instanceof TapisRoulant) {
 					TapisRoulant tapis = (TapisRoulant) vueHall.getHall().getFileBagageList().get(i);
 					tapis.setAutoBagageGeneration(false);
 				}
 			}
 			ArrayList<Chariot> chariots = vueHall.getHall().getChariotList();
 			//On arrête les chariots au prochain noeud
 			for (int i = 0; i < chariots.size(); i++) {
				if(chariots.get(i).getCheminPrevu().size() > 0 && chariots.get(i).getCheminPrevu().peek() instanceof Noeud) {
 					//On va arriver sur un noeud
 					LinkedList<ElementCircuit> newChemin = new LinkedList<ElementCircuit>();
 					newChemin.add(chariots.get(i).getCheminPrevu().peek());
 					chariots.get(i).setCheminPrevu(newChemin);
 				} else {
 					//On est sur un noeud
 					chariots.get(i).setCheminPrevu(null);
 				}
 			}
 		} else if (mode == Mode.AUTO) {
 			vueHall.getHall().setAutomatique(true);
 			
 			for (int i = 0; i < vueHall.getHall().getFileBagageList().size(); i++) {
 				if(vueHall.getHall().getFileBagageList().get(i) instanceof TapisRoulant) {
 					TapisRoulant tapis = (TapisRoulant) vueHall.getHall().getFileBagageList().get(i);
 					tapis.setAutoBagageGeneration(true);
 				}
 			}
 			
 			UtilsCircuit.getUtilsCircuit().resetTapisRoulantIncomingChariotsNumber();
 			
 			ArrayList<Chariot> chariots = vueHall.getHall().getChariotList();
 
 			for (int i = 0; i < chariots.size(); i++) {
 				Noeud nouvelleDestination;
 				if (chariots.get(i).getBagage() == null) {
 					// Get prochain tapis
 					nouvelleDestination = UtilsCircuit.getUtilsCircuit()
 							.getTapisRoulantOptimalNext().getConnexionCircuit();
 				} else {
 					// Get prochain toboggan
 					nouvelleDestination = UtilsCircuit.getUtilsCircuit()
 							.getRandomExistingTobogan().getConnexionCircuit();
 				}
 				// Y aller !
 				Noeud noeudChariot = null;
 				if (chariots.get(i).getParent() instanceof Noeud) {
 					noeudChariot = (Noeud) chariots.get(i).getParent();
 				} else {
 					noeudChariot = ((Rail) chariots.get(i).getParent())
 							.getNoeudSuivant();
 				}
 				chariots.get(i)
 						.setCheminPrevu(
 								vueHall.getHall()
 										.getCircuit()
 										.calculChemin(noeudChariot,
 												nouvelleDestination));
 				chariots.get(i).setDestination(nouvelleDestination);
 			}
 
 		} else {
 			log.warn("Mode non existant.");
 			return;
 		}
 		this.mode = mode;
 	}
 	
 	public void refreshProperties(){
 		this.propertiesWidget.refresh();
 	}
 	
 	public void displayFleches(LinkedList<Rail> rails) {
 		railsFleches = rails;
 		for (Rail r : railsFleches) {
 			VueRail vueR = (VueRail) ViewSelector
 					.getInstance()
 					.getViewForKernelObject(r);
 			vueR.setDisplayingArrow(true);
 		}
 	}
 	
 	public void deleteFleches() {
 		if(railsFleches == null) return;
 		for (Rail r : railsFleches) {
 			VueRail vueR = (VueRail) ViewSelector
 					.getInstance()
 					.getViewForKernelObject(r);
 			vueR.setDisplayingArrow(false);
 		}
 		railsFleches = null;
 	}
 
 }
