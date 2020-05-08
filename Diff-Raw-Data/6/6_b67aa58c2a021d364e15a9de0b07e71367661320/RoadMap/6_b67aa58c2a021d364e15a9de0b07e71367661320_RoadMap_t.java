 package model;
 
 import java.awt.Color;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import Exception.GraphException;
 
 import tsp.GraphDelivery;
 import dijkstra.Dijkstra;
 
 public class RoadMap
 {
 	protected ArrayList<Schedule> timeZones;
 	protected ZoneGeo zoneGeo;
 	protected final int DUREE_LIVRAISON = 15;
 	protected StateRoadMap etat = StateRoadMap.INIT;
 	protected Delivery warehouseDelivery;
 	protected Schedule retourSch;
 	
 	
 	public RoadMap(ArrayList<Schedule> timeZones, ZoneGeo zoneGeo)
 	{
 		super();
 		this.timeZones = timeZones;
 		this.zoneGeo = zoneGeo;
 		
 		//ajout d'un schedule réservé au retour à l'entrepot
 		retourSch = new Schedule(0, 1440, Color.BLACK);
 		warehouseDelivery = new Delivery(zoneGeo.getWarehouse(), retourSch);
 		retourSch.appendDelivery(warehouseDelivery);
 		timeZones.add(retourSch);
 		
 	}
 	
 	public Delivery getWarehouse() {
 		return warehouseDelivery;
 	}
 
 	public ZoneGeo getZoneGeo() {
 		return zoneGeo;
 	}
 	
 	public ArrayList<Schedule> getSchedules()
 	{
 		return timeZones;
 	}
 	
 	
 	/**
 	 * 
 	 * @return liste ordonnée de tous les noeuds (y compris ceux permetant de passer d'une livraison à l'autre)
 	 */
 	public ArrayList<Node> getFullPath()
 	{
 		ArrayList<Node> fullPath = new ArrayList<Node>();
 		for (Schedule s : timeZones)
 		{
 			for (Delivery d : s.getDeliveries())
 			{
 				 fullPath.addAll(d.getPathToDest().getTrajectory());
 				 fullPath.add(d.getDest());
 			}
 		}
 		return fullPath;
 	}
 	
 	public ArrayList<Delivery> getAllDeliveries()
 	{
 		ArrayList<Delivery> retour = new ArrayList<Delivery>();
 		for (Schedule sch : timeZones)
 		{
 			for (Delivery d : sch.getDeliveries())
 			{
 				retour.add(d);
 			}
 		}
 		return retour;
 	}
 	
 	/**
 	 * 	@param n node dont on veut le schedule
 	 * 	@return schedule contenant le node n, null si le node n'est acutellement pas un point de livraison
 	 */
 	public Delivery getDelivery(Node n) {
 		for (Delivery d : getAllDeliveries()) {
 			if (d.getDest().getID() == n.getID()) {
 				return d;
 			}
 		}
 		return null;
 	}
 
 	public StateRoadMap getEtat()
 	{
 		return etat;
 	}
 	
 	
 	/**
 	 * (re)calcule la tournée optimale sur la base des données actuelles de la FeuilleDeRoute (schedules, deliveries).
 	 * @throws GraphException
 	 */
 	public void computeWithTSP() throws GraphException
 	{
 		GraphDelivery gl = new GraphDelivery(this);
 		gl.createGraph();
 		ArrayList<Delivery> result = gl.calcItineraire();
 		
 		// pour chaque Schedule, on cherche les deliveries concernées
 		for (Schedule sch : timeZones)
 		{
 			LinkedList<Delivery> newDelivs = new LinkedList<Delivery>();
 			for (Delivery d : result)
 			{
 				if (d.schedule == sch)
 				{
 					//on les insère, dans l'ordre, dans la nouvelle liste du Schedule
 					
 					newDelivs.add(d);
 					
 				}
 			}
 			sch.setDeliveries(newDelivs);
 		}
 		computeArrivalTimes();
 		etat = StateRoadMap.OPTIM;
 	}
 	
 	public void backToInit()
 	{
 		for (Schedule sch : timeZones)
 		{
 			for (Delivery deliv : sch.getDeliveries())
 			{
 				deliv.resetHeuresEtChemin();
 			}
 		}
 		etat = StateRoadMap.INIT;
 	}
 	
 	
 
 	/**
 	 * calcule les HeurePrevue et RetardPrevu de toutes les delivery actuellement dans la FeuilleDeRoute
 	 */
 	public void computeArrivalTimes()
 	{
 		int theTime = 0;
 		for (Schedule sch : timeZones)
 		{
 			
 			for (Delivery d : sch.getDeliveries())
			{		
				theTime += (int) d.getPathToDest().getDuration();
				
 				//pause attente début livraison
 				if (theTime<sch.getStartTime())
 					theTime = sch.getStartTime();
 				
 				d.setHeurePrevue(theTime);
 				d.setRetardPrevu(theTime > sch.getEndTime());	//si arrive hors schedule
 				theTime += DUREE_LIVRAISON;
 			}
 		}
 	}
 	
 	/**
 	 * gènère un rapport de feuilleDeRoute sous forme de fichier texte
 	 * 
 	 * @param path fichier dans lequel écrire le rapport
 	 * @throws IOException
 	 */
 	public void generateReport(File path) throws IOException
 	{
 	    PrintWriter writer;
 
 	    writer =  new PrintWriter(new BufferedWriter(new FileWriter(path)));
 	   
 	    writer.println("Rapport de la feuille de route pour le livreur.\n");
 	    writer.println("La tournee est divisee en "+timeZones.size()+" creneaux.");
 	    for(Schedule s: timeZones)
 	    {
 		    writer.println("Le prochain creneau s'etend de" + Schedule.timeToString(s.startTime)+  " a "+Schedule.timeToString(s.endTime));
 		    writer.println("Dans ce creneau il y a "+s.getDeliveries().size()+ " livraisons a faire." );
 		    for(Delivery d : s.getDeliveries())
 		    {
 			    writer.println("La prochaine livraison aura lieu a "+d.getDest().getID()+" ." );
 			    writer.println("Arrivee prevue a  "+ Schedule.timeToString(d.getHeurePrevue()));
 		    	for(Arc a :d.getPathToDest().getArcs())
 		    	{
 		    		writer.print("Passer par le point "+ a.getDest().getID());
 					writer.println(" en empruntant la rue "+a.getName()+".");
 		    	}
 			    writer.println("Quand vous serez arrive, vous disposez de 15 minutes pour livrer le colis au client.");
 		    }
 	    }
 	    writer.println("Toutes les livraisons ont ete faites, la tournee est finie.");
 	    writer.close();
 	}
 
 	/**
 	 * ajoute un node dans un schedule donné, utile seulement à l'init, avant le premier appel de computeWithTSP
 	 * 
 	 * @param node noeud à insérer
 	 * @param schedule schedule dans lequel insérer le noeud
 	 * @throws RuntimeException
 	 */
 	public void addNode(Node node, Schedule schedule) throws RuntimeException
 	{
 		if (etat != StateRoadMap.INIT)
 		{
 			throw new RuntimeException("trying to FeuilleDeRoute.addNode() while already initialized");
 		}
 		Delivery deliv = new Delivery(node, schedule);
 		schedule.appendDelivery(deliv);
 	}
 	
 	/**
 	 * supprime la livraison concernant node si elle existe, et recalcule le chemin entre les noeuds englobants
 	 * @param node noeud a supprimer
 	 */
 	public void delNode(Node node)
 	{
 		Delivery deliv = getDelivery(node);
 		if (deliv != null)
 		{
 			if (etat == StateRoadMap.INIT)
 			{
 				deliv.getSchedule().removeDelivery(deliv);
 			}
 			else	//already init
 			{
 				Delivery next = nextDelivery(deliv);
 				deliv.getSchedule().removeDelivery(deliv);
 				recalcPathTo(next);
 				computeArrivalTimes();
 			}
 		}
 
 	}
 	
 	/**
 	 * insère inserted avant reference, dans la meme plage horraire que celui-ci
 	 * @param inserted	noeud a inserer
 	 * @param reference	noeud vis-a-vis du quel on cherche à insérer
 	 */
 	public void insertNodeBefore(Node inserted, Delivery reference)
 	{
 		insertNode(inserted, reference, true);
 	}
 	
 	/**
 	 * insère inserted apres reference, dans la meme plage horraire que celui-ci
 	 * @param inserted	noeud a inserer
 	 * @param reference	noeud vis-a-vis du quel on cherche à insérer
 	 */
 	public void insertNodeAfter(Node inserted, Delivery reference)
 	{
 		insertNode(inserted, reference, false);
 	}
 	
 	
 	/**
 	 * @param inserted	noeud a inserer
 	 * @param reference	noeud vis-a-vis du quel on cherche à insérer inserted
 	 * @param before	insertion de inserted avant reference si true, après sinon
 	 */
 	protected void insertNode(Node inserted, Delivery reference, boolean before)
 	{
 		boolean refFound = false;
 		
 		//recherche du bon schedule
 		for (Schedule sch : timeZones)
 		{
 			LinkedList<Delivery> schDeliveries = sch.getDeliveries();
 
 			if (schDeliveries.contains(reference))	//si le schedule contient la livraison de reference
 			{
 				refFound = true;
 				
 			//insertion du noeud dans le schedule
 				Delivery newDelivery = new Delivery(inserted, sch);
 				int insertIndex;	//futur index du noeud inséré
 				if (before)
 				{
 					insertIndex = schDeliveries.indexOf(reference);
 				}
 				else
 				{
 					insertIndex = schDeliveries.indexOf(reference) +1;
 				}
 				sch.insert(newDelivery, insertIndex);
 				
 				
 				
 			//re-calcul du chemin précedent
 				recalcPathTo(newDelivery);
 			//re-calcul du chemin suivant
 				recalcPathTo(nextDelivery(newDelivery));
 				
 				computeArrivalTimes();
 				
 				etat = StateRoadMap.MODIF;
 			}
 		}
 		if (!refFound)
 			throw new RuntimeException("FeuilleDeRoute.insertNode(): reference delivery not found");
 	}
 	
 	
 	/**
 	 * @param delivery
 	 * @return delivery precedant la delivery passée en parametre
 	 */
 	public Delivery previousDelivery(Delivery delivery)
 	{
 		Delivery returned = null;
 		for (Schedule sch : timeZones)
 		{
 			LinkedList<Delivery> schDeliveries = sch.getDeliveries();
 
 			if (schDeliveries.contains(delivery))	//si le schedule contient la livraison de reference
 			{
 				int idx = schDeliveries.indexOf(delivery);
 				if (idx == 0)	//si premier element du schedule
 				{
 					//recherche du dernier node du schedule précédent non vide	
 					returned = prevNonemptySchedule(sch).getDeliveries().getLast();
 				}
 				else
 				{
 					returned = schDeliveries.get(idx-1);
 				}
 			}
 		}
 		if (returned == null)
 			throw new RuntimeException("FeuilleDeRoute.previousDelivery(): delivery not found");
 		
 		return returned;
 	}
 	
 	/**
 	 * @param delivery
 	 * @return delivery suivant la delivery passée en parametre
 	 */
 	public Delivery nextDelivery(Delivery delivery)
 	{
 		Delivery returned = null;
 		for (Schedule sch : timeZones)
 		{
 			LinkedList<Delivery> schDeliveries = sch.getDeliveries();
 
 			if (schDeliveries.contains(delivery))	//si le schedule contient la livraison de reference
 			{
 				int idx = schDeliveries.indexOf(delivery);
 				if (delivery == schDeliveries.getLast())	//si dernier element du schedule
 				{
 					//recherche du premier node du schedule suivant non vide
 					returned = nextNonemptySchedule(sch).getDeliveries().getFirst();	//dernier node du sch précédent
 				}
 				else
 				{
 					returned = schDeliveries.get(idx+1);
 				}
 			}
 		}
 		if (returned == null)
 			throw new RuntimeException("FeuilleDeRoute.nextDelivery(): delivery not found");
 		return returned;
 	}
 
 	/**
 	 * renvoie le schedule non vide précédent
 	 * 
 	 * @param sch schedule dont on cherche le précédent non vide
 	 * @return schedule non vide précédent. Si tous les schedules précedents sont vides, renvoie le schedule de retour à l'entrepot.
 	 */
 	Schedule prevNonemptySchedule(Schedule sch)
 	{
 		int idx = timeZones.indexOf(sch);
 		Schedule prev = null;
 		if (idx == 0)
 		{
 			prev = retourSch;	//bouclage
 		}
 		else
 		{
 			prev = timeZones.get(idx-1);
 		}
 
 		
 		if (prev.getDeliveries().size() != 0)
 		{
 			return prev;
 		}
 		else
 		{
 			return  prevNonemptySchedule(prev);
 		}
 	}
 	
 	
 	/**
 	 * renvoie le prochain schedule non vide
 	 * 
 	 * @param sch schedule dont on cherche le suivant non vide
 	 * @return prochain schedule non vide. Si tous les schedules suivants sont vides, renvoie le schedule de retour à l'entrepot.
 	 */
 	Schedule nextNonemptySchedule(Schedule sch)
 	{
 		int idx = timeZones.indexOf(sch);
 		Schedule next = null;
 
 		next = timeZones.get(idx+1);
 		
 		if (next.getDeliveries().size() != 0)
 		{
 			return next;
 		}
 		else
 		{
 			return  nextNonemptySchedule(next);
 		}
 	
 	}
 
 	/**
 	 * recalcule le chemin pour aller à delivery. Se base sur l'ordre donné dans les schedules pour le point de départ.
 	 * @param delivery delivery dont le PathToDest doit etre recalculé
 	 */
 	public void recalcPathTo(Delivery delivery)
 	{
 		//recherche chemin
 		ArrayList<Node> singleton = new ArrayList<Node>();
 		singleton.add(delivery.getDest());
 		ArrayList<Path> result = Dijkstra.solve(zoneGeo, previousDelivery(delivery).getDest(), singleton);
 		delivery.pathToDest = result.get(0);
 		
 	}
 
 }
