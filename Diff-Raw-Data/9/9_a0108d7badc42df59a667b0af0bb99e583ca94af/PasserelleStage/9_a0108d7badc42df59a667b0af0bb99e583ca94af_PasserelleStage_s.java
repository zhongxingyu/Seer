 package pack;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 
 /**
  * 
  * @author BERON Jean-Sbastien
  *
  */
 public class PasserelleStage {
 	
 	private static boolean good;
 	private static final String pathObj = Config.get("data.obj");
 	private static final String pathExport = Config.get("imp.delia");
 	private static final String filterPat = Config.get("imp.delia.filter.stage");
 	private static final boolean filterCancel = Config.getB("imp.delia.filter.cancel");
 	private static final String filterCancelPat = Config.get("imp.delia.filter.cancel.pat");
	
 	private static SimpleDateFormat fmtDate   = new SimpleDateFormat("dd/MM/yyyy");
 
 	/**
 	 * procedure de mise a jour des stages
 	 * fait l'importation des donnes de delia
 	 */
 	@SuppressWarnings("deprecation")
 	public static void importationDonnes(){
 		
 		//attributs
 		good = true;
 		
 		//chargement de le liste des Stages de lundi a J
 		ArrayList<Stage> stageList;
 		stageList = lectureStageObj();
 		
 		//importation des donnes de Export.txt : stages de J+1
 		ArrayList<Stage> stageExportList;
 		stageExportList = importExportDelia();
 		
 		Date dateactuelle = new Date();
 		if(!Config.getB("imp.test") && 
 				(stageExportList.get(0).getDateDt().equals(dateactuelle) || stageExportList.get(0).getDateDt().before(dateactuelle)) )
 		{
 			JOptionPane.showMessageDialog(null, "<html>ERREUR ! la date des stages que vous essayez d'importer n'est pas celle de demain !<br> veuillez refaire l'exportation DELIA</html>"
 					,"Erreur",JOptionPane.YES_NO_OPTION);
 		}else{
 			//importation des stagiaires PNC de J+1
 			ArrayList<Stagiaire> stagiairePNCList = PasserelleStagiaire.chargerTousStagiairesPNC();
 			//importation des stagiaires PNT de J+1
 			ArrayList<Stagiaire> stagiairePNTList = PasserelleStagiaire.chargerTousStagiairesPNT();
 			
 			//ajout des stagiaires aux stages
 			stageExportList = PasserelleStagiaire.ajoutPnc(stageExportList, stagiairePNCList);
 			stageExportList = PasserelleStagiaire.ajoutPnt(stageExportList, stagiairePNTList);
 			
 			Date dateActuelle = new Date();
 			
 			//concatenation des deux listes
 			stageList = concatenationCollection(stageList, stageExportList);
 			if (good) {
 				
 				//si on est vendredi
 				if (dateActuelle.getDay() == 5) {
 					ecritureStageObj(stageExportList);
 				}else{
 					ecritureStageObj(stageList);
 				}
 				
 				//si on est jeudi
 				if (dateActuelle.getDay() == 4) {
 					ecritureArchiveObj(dateActuelle, stageList);
 				}
 			}
 			
 			if (good) {
 				JOptionPane.showMessageDialog(null, "<html>Operation termine !" +
 						"<br/>les donnes DELIA ont bien t importes</html>", "Termine", JOptionPane.INFORMATION_MESSAGE);
 			}
 		}//finsi
 		
 		
 		
 	}//fin miseAJourStage()
 	
 	/**
 	 * procedure d'archivage
 	 */
 	public static void archivage(){
 		
 	}//fin archivage()
 	
 	/**
 	 * lecture du fichier et recuperation des donnes dans une liste de stages
 	 * @return stageList
 	 */
 	@SuppressWarnings("unchecked")
 	public static ArrayList<Stage> lectureStageObj(){
 		
 		//instanciation de la liste de stage
 		ArrayList<Stage> stageList = new ArrayList<Stage>();
 			FileInputStream fichier;
 			try {
 				fichier = new FileInputStream(pathObj);
 				ObjectInputStream stream = new ObjectInputStream(fichier);
 				stageList = (ArrayList<Stage>) stream.readObject();
 				stream.close();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 			
 		return stageList;
 		
 	}//fin lectureStageAffichage()
 	
 	/**
 	 * methodes supprimant les stages ne datant pas d'aujourd'hui
 	 * @param stageList
 	 * @return stageList
 	 */
 	public static ArrayList<Stage> suppresionStage(ArrayList<Stage> stageList, boolean keepnonJ){
 		//false => suppresion stage non J
 		//true => suppresion stage J
 		
 		ArrayList<Stage> stageListnew = stageList;
 		
 		//recuperation de la date
 		Date dateActuelle = new Date();
 		String datedujour = fmtDate.format(dateActuelle);
 		//recuperation des stages a enlever
 		ArrayList<Stage> stageListRem = new ArrayList<Stage>();
 		for (Stage stage : stageListnew) {
 			if(stage.getDateStr().equals(datedujour) == keepnonJ){
 				stageListRem.add(stage);
 			}
 		}
 		
 		//suppresion des stages a enlever
 		stageListnew.removeAll(stageListRem);
 		
 		//retour
 		return stageListnew;
 		
 	}//fin suppresionStagenonJ()
 	
 	/**
 	 * methode qui recupere les information dans export.txt
 	 * @return
 	 */
 	private static ArrayList<Stage> importExportDelia(){
 		
 		//instanciation des listes
 		ArrayList<Stage> stageExportList = new ArrayList<Stage>();
 		ArrayList<Module> moduleList = new ArrayList<Module>();
 		FileReader fichier;
 		Map<String, Integer> hsCode = new HashMap<String, Integer>();
 		Map<Long, Integer> hsId = new HashMap<Long, Integer>();
 		Map<String, Stage> hsStages = new HashMap<String,Stage>();
 		
 		String site = Config.get("app.site");
 		
 			try {
 				fichier = new FileReader(pathExport);
 				BufferedReader reader = new BufferedReader(fichier);
 				String ligne;
 				ArrayList<String> infoLigne = new ArrayList<String>();
 				String chaine;
 				
 				Module newmodule = null;
 							    
 				//lecture de la premiere ligne car ligne d'entete
 				reader.readLine();
 				//lecture des chaque ligne jusqu'a la fin du fichier
 				while ((ligne = reader.readLine()) != null){
 					
 					//recuperation de toutes les informations
 					chaine = "";
 					for (int i = 0; i < ligne.length(); i++) {
 						//la tabulation est le separatuer de Export.txt
 						if(ligne.substring(i, i+1).equals("\t")){
 							infoLigne.add(chaine.trim());
 							chaine = "";
 						}else{
 							chaine = chaine + ligne.substring(i, i+1);
 						}
 					}
 					//infoLigne.add(chaine);//recup de la derniere information
 					// juste  31 cellules ! bis ! ter
 					if (infoLigne.size() < 29) {
 						//System.out.println("Bug");
 						while (infoLigne.size() < 31) {
 							//chaine = "";
 							ligne = reader.readLine();
 							for (int i = 0; i < ligne.length(); i++) {
 								//la tabulation est le separatuer de Export.txt
 								if(ligne.substring(i, i+1).equals("\t")){
 									infoLigne.add(chaine.trim());
 									chaine = "";
 								}else{
 									chaine = chaine + ligne.substring(i, i+1);
 								}
 							}
 						}
 					}
 					infoLigne.add(chaine.trim());//recup de la derniere information
 
 					//ajout des modules
 					if(infoLigne.get(3).equalsIgnoreCase("activit")) {
 						Long id  = Long.parseLong(infoLigne.get(0));
 						String code = infoLigne.get(4);
						if (code.matches("P[123].*")) {
 							code = code.replaceFirst(
 								Config.get("imp.p123.pat."+site),
 								Config.get("imp.p123.rep."+site));
 						}
 						// 4S
 						if (!code.matches(filterCancelPat)) {
 							code = code.replaceAll(Config.get("imp.delia.s2.pat"), "$1");
 						}
 						//System.out.println(" ? "+id +"/"+code+" => " + hsCode.get(code));
 						if (hsCode.containsKey(code)) {
 							if (hsId.containsKey(id)) {
 								//System.out.println(" - "+id +"/"+code+" => " + hsCode.get(code));
 							}
 							else {
 								Integer c = hsCode.get(code); c++; hsCode.put(code, c);
 								hsId.put(id, hsCode.get(code));
 								//System.out.println(" + "+id +"/"+code+" => " + hsCode.get(code));
 							}
 						}
 						else {
 							hsCode.put(code, new Integer(1));
 							hsId.put(id, new Integer(1));
 							//System.out.println(" N "+id +"/"+code+" => " + hsCode.get(code));
 						}
 						
 						newmodule = new Module(id, code, infoLigne.get(22), infoLigne.get(29).substring(0, 10)
 								, infoLigne.get(29).substring(11,16), infoLigne.get(30).substring(11,16));
 						
 						newmodule.setCompagnie(infoLigne.get(7));
 						if(infoLigne.get(2).equalsIgnoreCase("salle")){
 							String salle = "Salle "+infoLigne.get(1);
 							String s = salle.substring(salle.lastIndexOf(" ")+1);
 							String n = Config.get("salle."+site+"."+s);
 							if (n != null) {
 								salle = n;
 							}
 							newmodule.setSalle(salle);
 						}
 						if(infoLigne.get(2).equalsIgnoreCase("moyen-bepn")){
 							newmodule.setSalle(infoLigne.get(1));
 						}
 						if(infoLigne.get(2).equalsIgnoreCase("instructeur")){
 							if(infoLigne.get(31).equalsIgnoreCase("oui")){
 								newmodule.setNomLeader(infoLigne.get(1));
 							}else{
 								newmodule.setNomAide(infoLigne.get(1));
 							}
 						}
 						if(infoLigne.get(2).equalsIgnoreCase("intervenant")){
 							newmodule.setNomIntervenant(infoLigne.get(1));
 						}
 						moduleList.add(newmodule);
 					}
 					
 					infoLigne.clear();
 					
 				}//fin tantque non fin fichier
 				reader.close();
 			} catch (FileNotFoundException e) {
 				good = false;
 				JOptionPane.showMessageDialog(null, "<html>le fichier suivant n'existe pas :" +
 						"<br/>dataImport\\Export.txt</html>", "Erreur", JOptionPane.ERROR_MESSAGE);
 			} catch (IOException e) {
 				good = false;
 				JOptionPane.showMessageDialog(null, "<html>probleme de lecture de" +
 						"<br/>dataImport\\Export.txt</html>", "Erreur", JOptionPane.ERROR_MESSAGE);
 			}
 			
 		boolean good;
 		for (Module module : moduleList) {
 			good = false;
 			for (Stage stage : stageExportList) {
 				if(module.getId() == stage.getId()){
 					good = true;
 					int indexmod = stage.exist(module);
 					Module mm;
 					if(indexmod != -1){
 						mm = stage.getEltModuleList(indexmod);
 					} else {
 						mm = module;
 					}
 					mm.setStage(stage);
 					mm.setCodeStage(stage.getCode());
 					if(! module.getSalle().equalsIgnoreCase("")){
 						mm.setSalle(module.getSalle());
 					}
 					if(! module.getNomLeader().equalsIgnoreCase("")){
 						mm.setNomLeader(module.getNomLeader());
 						//System.out.println("  "+ stage.getCode()+" Mod Module:" + mm.getLibelle()+" L:" + mm.getNomLeader()+"/"+stage.getLeader() + " s:"+mm.getStage());
 					}
 					if(! module.getNomAide().equalsIgnoreCase("")){
 						mm.setNomAide(module.getNomAide());
 						//System.out.println("  "+stage.getCode()+" Mod Module:" + mm.getLibelle()+" A:" + mm.getNomAide() + " s:"+mm.getStage());
 					}
 					if(! module.getNomIntervenant().equalsIgnoreCase("") &&  
 							mm.getNomAide().equalsIgnoreCase("")) {
 						mm.setNomAide(module.getNomIntervenant());
 						System.out.println("  "+stage.getCode()+" Mod Module:" + mm.getLibelle()+" I>A:" + mm.getNomAide() + " s:"+mm.getStage());
 					}
 					//}else{
 					if(indexmod == -1){
 						//module.setCodeStage(stage.getCode());
 						stage.ajoutModule(module);
 						System.out.println(" +"+stage.getCode()+" Add Module:" + module.getLibelle() + " S:" + module.getSalle() + " L:" + module.getNomLeader()+"/"+stage.getLeader() + " s:"+module.getStage());
 					}
 				}
 			}
 
 			// nouveau stage 
 			if(! good){
 				// filter
 				/*
 				if(module.getCodeStage().equalsIgnoreCase("dry")
 				|| module.getCodeStage().equalsIgnoreCase("rserve")
 				|| module.getCodeStage().equalsIgnoreCase("non instruction")
 				|| module.getCodeStage().equalsIgnoreCase("mts")){
 				*/
 				if ( (filterCancel && module.getCodeStage().matches(filterCancelPat))
 						|| module.getCodeStage().matches(filterPat) ) {
 					System.out.println("- "+module.getCodeStage());
 					//nothing
 				} else {
 					Stage s = new Stage(module);
 					s.setIdx(hsId.get(s.getId()), hsCode.get(s.getCodeI()));
 					stageExportList.add(s);
 					module.setCodeStage(s.getCode());
 					if (s.getIdx() > 1 && hsStages.containsKey(s.getCodeI()+"-1")) {
 						s.setCoStage(hsStages.get(s.getCodeI()+"-1"));
 						System.out.println("* "+s.getCode()+" has coStage:"+s.getCoStage().getCode());
 					}
 					hsStages.put(s.getCode(), s);
 					//System.out.println("Add stage "+s.getCode()+":"+hsId.get(module.getId())+"/"+hsCode.get(module.getCodeStage()));
					System.out.println("+ "+s.getCode()+":"+s.getIdx()+"/"+s.getIdxMax()+ "M:" + module.getLibelle()+" L:"+s.getLeader() + " s:"+module.getStage());
 				}
 			}
 		}
 		
 		return stageExportList;
 	}//fin importExportDelia()
 	
 	/**
 	 * fait la concatenation des 2 Arraylist passes en parametres
 	 * @param stageList
 	 * @param stageExportList
 	 * @return
 	 */
 	public static ArrayList<Stage> concatenationCollection(ArrayList<Stage> stageList, ArrayList<Stage> stageExportList){
 		
 		ArrayList<Stage> stageListnew = stageList;
 		stageListnew.addAll(stageExportList);
 		return stageListnew;
 		
 	}//fin concatenationCollection()
 	
 	/**
 	 * ecriture des stages dans stageAffichage.obj
 	 * @param stageList
 	 */
 	public static void ecritureStageObj(ArrayList<Stage> stageList){
 		
 			try {
 				FileOutputStream fichier;
 				fichier = new FileOutputStream(pathObj);
 				ObjectOutputStream stream = new ObjectOutputStream(fichier);
 				stream.writeObject(stageList);
 				stream.flush();
 				stream.close();
 			//traitement des exceptions
 			} catch (FileNotFoundException e) {
 				good = false;
 				JOptionPane.showMessageDialog(null, "<html>le fichier suivant n'est pas trouv" +
 						"<br/>"+pathObj+"</html>", "Erreur", JOptionPane.ERROR_MESSAGE);
 			} catch (IOException e) {
 				good = false;
 				JOptionPane.showMessageDialog(null, "<html>probleme de lecture de" +
 						"<br/>"+pathObj+"</html>", "Erreur", JOptionPane.ERROR_MESSAGE);
 			}
 			
 	}//fin ecritureStage()
 	
 	/**
 	 * ecriture des stages dans archive.obj
 	 * @param stageList
 	 */
 	@SuppressWarnings({ "deprecation", "static-access" })
 	public static void ecritureArchiveObj(Date ladate, ArrayList<Stage> stageList){
 		
 		//creation du dossier
 		String pathDossier = "Archive de l'anne "+(ladate.getYear()+1900);
 		new File("dataSystem\\"+pathDossier).mkdir();
 		
 		//rcuperation du nombre de la semaine
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(ladate);
 		int week = cal.get(cal.WEEK_OF_YEAR);
 		
 			try {
 				FileOutputStream fichier;
 				fichier = new FileOutputStream("dataSystem\\"+pathDossier+"\\archiveSemaine"+week+".obj");
 				ObjectOutputStream stream = new ObjectOutputStream(fichier);
 				stream.writeObject(stageList);
 				stream.flush();
 				stream.close();
 			//traitement des exceptions
 			} catch (FileNotFoundException e) {
 				good = false;
 				JOptionPane.showMessageDialog(null, "<html>le fichier n'est pas trouv</html>", "Erreur", JOptionPane.ERROR_MESSAGE);
 			} catch (IOException e) {
 				good = false;
 				JOptionPane.showMessageDialog(null, "<html>probleme de lecture</html>", "Erreur", JOptionPane.ERROR_MESSAGE);
 			}
 			
 	}//fin ecritureStage()
 	
 	/**
 	 * chargement de la liste des stages pour le teleAffichage
 	 * @return
 	 */
 	public static ArrayList<Stage> chargerStageList(Boolean...params){
 	    boolean all = params.length > 0 ? params[0].booleanValue() : false;
 		//declaration de la liste
 		ArrayList<Stage> stageList;
 		stageList = lectureStageObj();
 		if (!all) {
 			stageList = suppresionStage(stageList,false);
 		}
 		stageList = triHoraire(stageList);
 		//retour
 		return stageList;
 	}//fin chargerstageList()
 	
 	/**
 	 * tri la liste des stages enfonction de l'heure de debut des stages
 	 * @param stageListnotorder
 	 * @return
 	 */
 	private static ArrayList<Stage> triHoraire(ArrayList<Stage> stageListnotorder){
 		ArrayList<Stage> stageListorder = stageListnotorder;
 		int indice;
 		Stage stageTemp;
 		//debut du tri
 		for (int i = 0; i < stageListorder.size(); i++) {
 			indice = indicemin(stageListorder, i);
 			if (i != indice) {
 				stageTemp = stageListorder.get(i);
 				stageListorder.set(i, stageListorder.get(indice));
 				stageListorder.set(indice,stageTemp);
 			}
 		}
 		
 		return stageListnotorder;
 	}//fin triHoraire()
 	
 	/**
 	 * retourne l'indice du stage commencant en premier
 	 * @param stageList
 	 * @param rang
 	 * @return
 	 */
 	private static int indicemin(ArrayList<Stage> stageList, int rang){
 		//recuperation du rang
 		int indiceCherche = rang;
 		for (int i = rang+1; i <= stageList.size()-1; i++) {
 			if(stageList.get(i).getnbMin() < stageList.get(indiceCherche).getnbMin()){
 				indiceCherche = i;
 			}
 		}
 		//retour
 		return indiceCherche;
 	}//fin indicemin()
 	
 }//fin class
